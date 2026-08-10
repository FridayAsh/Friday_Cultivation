package com.friday.cultivation.entity.spell;

import com.friday.cultivation.event.SectCombatHandler;
import com.friday.cultivation.event.SectProtectionDomeHandler;
import com.friday.cultivation.event.SoulStateHandler;
import com.friday.cultivation.event.TimeStasisHandler;
import com.friday.cultivation.registry.ModEntities;
import com.friday.cultivation.util.SpellDamageSourceHelper;
import com.friday.cultivation.util.SpellTerrainDestructionHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkHooks;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * 冲击波实体 — 完整复刻原模组 ShockwaveEntity
 * 球壳扩散+实体推离伤害+方块破坏+护罩检测
 */
public class ShockwaveEntity extends Entity {
    private static final EntityDataAccessor<Float> DATA_RADIUS = SynchedEntityData.defineId(ShockwaveEntity.class, EntityDataSerializers.FLOAT);
    private static final float MAX_RADIUS = 120.0f;
    private static final int EXPAND_TICKS = 60;
    private static final int FADE_TICKS = 15;
    public static final int TOTAL_LIFETIME_TICKS = 75;
    private static final double PUSH_FORCE = 4.0;
    private static final double BLOCK_DAMAGE_CENTER_PROB = 0.45;
    private static final int MAX_BLOCK_SAMPLES_PER_TICK = 6000;
    private static final int MAX_BLOCK_CHANGES_PER_TICK = 600;

    private int age = 0;
    private UUID ownerUuid;
    private float damageMultiplier = 1.0f;
    private final Set<Integer> processedEntities = new HashSet<>();
    private final Set<BlockPos> processedBarrierCores = new HashSet<>();

    public ShockwaveEntity(EntityType<? extends ShockwaveEntity> type, Level level) {
        super(type, level);
        this.noPhysics = true;
    }

    public ShockwaveEntity(ServerLevel level, Vec3 center, UUID ownerUuid) {
        this((EntityType<? extends ShockwaveEntity>) ModEntities.SHOCKWAVE.get(), level);
        this.setPos(center.x, center.y, center.z);
        this.ownerUuid = ownerUuid;
    }

    public void setDamageMultiplier(double multiplier) {
        if (!Double.isFinite(multiplier)) multiplier = 1.0;
        this.damageMultiplier = (float) Math.max(0.0, multiplier);
    }

    @Override
    protected void defineSynchedData() {
        this.entityData.define(DATA_RADIUS, 0.0f);
    }

    public float radius() { return this.entityData.get(DATA_RADIUS); }
    public float visualProgress() { return Math.min(1.0f, this.radius() / MAX_RADIUS); }
    public float fadeProgress() {
        if (this.age <= EXPAND_TICKS) return 0.0f;
        return Math.min(1.0f, (float)(this.age - EXPAND_TICKS) / FADE_TICKS);
    }

    @Override
    public void tick() {
        if (TimeStasisHandler.pauseEntityTickInStoppedTime(this)) return;
        super.tick();
        this.age++;
        if (this.age > TOTAL_LIFETIME_TICKS) { this.discard(); return; }
        float prevR = this.radius();
        float newR = Math.min(MAX_RADIUS, MAX_RADIUS * ((float) this.age / EXPAND_TICKS));
        this.entityData.set(DATA_RADIUS, newR);
        Level level = this.level();
        if (level instanceof ServerLevel server) {
            this.processEntitiesInShell(server, prevR, newR);
            SectProtectionDomeHandler.onSpellShellTouchedBarrier(server, this.position(), prevR, newR, this.terrainCaster(server), Math.max(1.0f, 50.0f * this.damageMultiplier), this.processedBarrierCores);
            if (this.age <= EXPAND_TICKS && this.age % 2 == 0) {
                float t2prev = (float)(this.age - 2) / EXPAND_TICKS;
                float prev2R = Math.max(0.0f, MAX_RADIUS * t2prev);
                this.damageBlocksInShell(server, prev2R, newR);
            }
        }
    }

    private void processEntitiesInShell(ServerLevel server, float innerR, float outerR) {
        Vec3 center = this.position();
        AABB box = new AABB(center.x - outerR, center.y - outerR, center.z - outerR, center.x + outerR, center.y + outerR, center.z + outerR);
        LivingEntity owner = getOwner(server);
        for (Entity e : server.getEntities(this, box, ent -> ent != this && ent.isAlive())) {
            if (this.processedEntities.contains(e.getId())) continue;
            if (SectProtectionDomeHandler.isEntityProtectedByOwnDome(e)) continue;
            Vec3 dir = e.position().subtract(center);
            double dist = dir.length();
            if (dist < innerR || dist > outerR) continue;
            if (e instanceof LivingEntity living) {
                if (!SoulStateHandler.canOrdinaryAffect(owner, living) || !SectCombatHandler.canApplyOffensiveEffect(owner, living)) continue;
            }
            double t = 1.0 - dist / MAX_RADIUS;
            double force = PUSH_FORCE * t;
            Vec3 push = dist > 0.01 ? dir.normalize().scale(force) : new Vec3(0.0, force, 0.0);
            e.setDeltaMovement(e.getDeltaMovement().add(push.x, force * 0.6, push.z));
            e.hurtMarked = true;
            if (e instanceof LivingEntity living) {
                float dmg = (float)(50.0 * t) * this.damageMultiplier;
                living.hurt(SpellDamageSourceHelper.indirectSpell(this, owner), dmg);
                living.setRemainingFireTicks(60);
            }
            this.processedEntities.add(e.getId());
        }
    }

    private void damageBlocksInShell(ServerLevel server, float innerR, float outerR) {
        if (outerR < 1.0f) return;
        Entity terrainCaster = this.terrainCaster(server);
        if (!SpellTerrainDestructionHelper.canModifyBlocks(server, terrainCaster)) return;
        Vec3 center = this.position();
        BlockState air = Blocks.AIR.defaultBlockState();
        double avgR = (innerR + outerR) * 0.5;
        double thickness = Math.max(1.0, outerR - innerR);
        int sampleCount = (int) Math.max(250.0, Math.PI * 4 * avgR * avgR * thickness * 0.025);
        sampleCount = Math.min(MAX_BLOCK_SAMPLES_PER_TICK, sampleCount);
        BlockPos.MutableBlockPos mbp = new BlockPos.MutableBlockPos();
        int changed = 0;
        for (int i = 0; i < sampleCount && changed < MAX_BLOCK_CHANGES_PER_TICK; i++) {
            double u = this.random.nextDouble() * 2.0 - 1.0;
            double phi = Math.acos(u);
            double theta = this.random.nextDouble() * Math.PI * 2.0;
            double r = innerR + this.random.nextDouble() * (outerR - innerR);
            double t = r / MAX_RADIUS;
            double prob = BLOCK_DAMAGE_CENTER_PROB * (1.0 - t);
            if (this.random.nextDouble() > prob) continue;
            double sx = center.x + r * Math.sin(phi) * Math.cos(theta);
            double sy = center.y + r * Math.cos(phi);
            double sz = center.z + r * Math.sin(phi) * Math.sin(theta);
            mbp.set((int) sx, (int) sy, (int) sz);
            BlockState state = server.getBlockState(mbp);
            if (state.isAir() || SectProtectionDomeHandler.isProtectedByAnySectProtectionDome(server, mbp)
                    || state.is(Blocks.BEDROCK) || state.is(Blocks.END_PORTAL_FRAME) || state.is(Blocks.OBSIDIAN)
                    || state.is(Blocks.CRYING_OBSIDIAN) || state.is(Blocks.REINFORCED_DEEPSLATE)) continue;
            if (!SpellTerrainDestructionHelper.setBlock(server, mbp, air, 2, terrainCaster)) continue;
            changed++;
        }
    }

    private Entity terrainCaster(ServerLevel server) {
        return this.ownerUuid == null ? this : server.getEntity(this.ownerUuid);
    }

    private LivingEntity getOwner(ServerLevel server) {
        if (this.ownerUuid == null) return null;
        Entity e = server.getEntity(this.ownerUuid);
        return e instanceof LivingEntity le ? le : null;
    }

    @Override
    protected void readAdditionalSaveData(@NotNull CompoundTag tag) {
        this.age = tag.getInt("age");
        if (tag.hasUUID("owner")) this.ownerUuid = tag.getUUID("owner");
        this.damageMultiplier = tag.contains("damageMultiplier") ? Math.max(0.0f, tag.getFloat("damageMultiplier")) : 1.0f;
    }

    @Override
    protected void addAdditionalSaveData(@NotNull CompoundTag tag) {
        tag.putInt("age", this.age);
        if (this.ownerUuid != null) tag.putUUID("owner", this.ownerUuid);
        tag.putFloat("damageMultiplier", this.damageMultiplier);
    }

    @NotNull
    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }

    @Override
    public AABB makeBoundingBox() {
        float r = Math.max(2.0f, this.radius());
        return new AABB(this.getX() - r, this.getY() - r, this.getZ() - r, this.getX() + r, this.getY() + r, this.getZ() + r);
    }
}
