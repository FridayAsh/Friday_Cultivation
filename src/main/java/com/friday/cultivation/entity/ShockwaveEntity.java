/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.BlockPos
 *  net.minecraft.core.BlockPos$MutableBlockPos
 *  net.minecraft.nbt.CompoundTag
 *  net.minecraft.network.protocol.Packet
 *  net.minecraft.network.protocol.game.ClientGamePacketListener
 *  net.minecraft.network.syncher.EntityDataAccessor
 *  net.minecraft.network.syncher.EntityDataSerializer
 *  net.minecraft.network.syncher.EntityDataSerializers
 *  net.minecraft.network.syncher.SynchedEntityData
 *  net.minecraft.server.level.ServerLevel
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.EntityType
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.level.block.Blocks
 *  net.minecraft.world.level.block.state.BlockState
 *  net.minecraft.world.phys.AABB
 *  net.minecraft.world.phys.Vec3
 *  net.minecraftforge.network.NetworkHooks
 *  org.jetbrains.annotations.NotNull
 */
package com.friday.cultivation.entity;

import com.friday.cultivation.event.SectCombatHandler;
import com.friday.cultivation.event.SectProtectionDomeHandler;
import com.friday.cultivation.event.SoulStateHandler;
import com.friday.cultivation.event.TimeStasisHandler;
import com.friday.cultivation.registry.ModEntities;
import com.friday.cultivation.util.SpellDamageSourceHelper;
import com.friday.cultivation.util.SpellTerrainDestructionHelper;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializer;
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

public class ShockwaveEntity
extends Entity {
    private static final EntityDataAccessor<Float> DATA_RADIUS = SynchedEntityData.defineId(ShockwaveEntity.class, (EntityDataSerializer)EntityDataSerializers.FLOAT);
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
    private final Set<Integer> processedEntities = new HashSet<Integer>();
    private final Set<BlockPos> processedBarrierCores = new HashSet<BlockPos>();

    public ShockwaveEntity(EntityType<? extends ShockwaveEntity> type, Level level) {
        super(type, level);
        this.noPhysics = true;
    }

    public ShockwaveEntity(ServerLevel level, Vec3 center, UUID ownerUuid) {
        this((EntityType<? extends ShockwaveEntity>)((EntityType)ModEntities.SHOCKWAVE.get()), (Level)level);
        this.setPos(center.x, center.y, center.z);
        this.ownerUuid = ownerUuid;
    }

    public void setDamageMultiplier(double multiplier) {
        if (!Double.isFinite(multiplier)) {
            multiplier = 1.0;
        }
        this.damageMultiplier = (float)Math.max(0.0, multiplier);
    }

    protected void defineSynchedData() {
        this.entityData.define(DATA_RADIUS, Float.valueOf(0.0f));
    }

    public float radius() {
        return ((Float)this.entityData.get(DATA_RADIUS)).floatValue();
    }

    public float visualProgress() {
        float r = this.radius();
        return Math.min(1.0f, r / 120.0f);
    }

    public float fadeProgress() {
        if (this.age <= 60) {
            return 0.0f;
        }
        return Math.min(1.0f, (float)(this.age - 60) / 15.0f);
    }

    public void tick() {
        if (TimeStasisHandler.pauseEntityTickInStoppedTime(this)) {
            return;
        }
        super.tick();
        ++this.age;
        if (this.age > 75) {
            this.discard();
            return;
        }
        float prevR = this.radius();
        float newR = Math.min(120.0f, 120.0f * ((float)this.age / 60.0f));
        this.entityData.set(DATA_RADIUS, Float.valueOf(newR));
        Level level = this.level();
        if (level instanceof ServerLevel) {
            ServerLevel server = (ServerLevel)level;
            this.processEntitiesInShell(server, prevR, newR);
            SectProtectionDomeHandler.onSpellShellTouchedBarrier(server, this.position(), prevR, newR, this.terrainCaster(server), Math.max(1.0f, 50.0f * this.damageMultiplier), this.processedBarrierCores);
            if (this.age <= 60 && this.age % 2 == 0) {
                float t2prev = (float)(this.age - 2) / 60.0f;
                float prev2R = Math.max(0.0f, 120.0f * t2prev);
                this.damageBlocksInShell(server, prev2R, newR);
            }
        }
    }

    private void processEntitiesInShell(ServerLevel server, float innerR, float outerR) {
        LivingEntity living;
        Entity entity;
        Vec3 center = this.position();
        AABB box = new AABB(center.x - (double)outerR, center.y - (double)outerR, center.z - (double)outerR, center.x + (double)outerR, center.y + (double)outerR, center.z + (double)outerR);
        LivingEntity owner = this.ownerUuid != null && (entity = server.getEntity(this.ownerUuid)) instanceof LivingEntity ? (living = (LivingEntity)entity) : null;
        for (Entity e : server.getEntities((Entity)this, box, ent -> ent != this && ent.isAlive())) {
            LivingEntity living2;
            Vec3 dir;
            double dist;
            if (this.processedEntities.contains(e.getId()) || SectProtectionDomeHandler.isEntityProtectedByOwnDome(e) || (dist = (dir = e.position().multiply(center)).length()) < (double)innerR || dist > (double)outerR || e instanceof LivingEntity && (!SoulStateHandler.canOrdinaryAffect((Entity)owner, (Entity)(living2 = (LivingEntity)e)) || !SectCombatHandler.canApplyOffensiveEffect(owner, living2))) continue;
            double t = 1.0 - dist / 120.0;
            double force = 4.0 * t;
            Vec3 push = dist > 0.01 ? dir.normalize().scale(force) : new Vec3(0.0, force, 0.0);
            e.setDeltaMovement(e.getDeltaMovement().add(push.x, force * 0.6, push.z));
            e.hurtMarked = true;
            if (e instanceof LivingEntity) {
                LivingEntity living3 = (LivingEntity)e;
                float dmg = (float)(50.0 * t) * this.damageMultiplier;
                living3.hurt(SpellDamageSourceHelper.indirectSpell(this, owner), dmg);
                living3.setRemainingFireTicks(60);
            }
            this.processedEntities.add(e.getId());
        }
    }

    private void damageBlocksInShell(ServerLevel server, float innerR, float outerR) {
        if (outerR < 1.0f) {
            return;
        }
        Entity terrainCaster = this.terrainCaster(server);
        if (!SpellTerrainDestructionHelper.canModifyBlocks(server, terrainCaster)) {
            return;
        }
        Vec3 center = this.position();
        BlockState air = Blocks.AIR.defaultBlockState();
        double avgR = (double)(innerR + outerR) * 0.5;
        double thickness = Math.max(1.0, (double)(outerR - innerR));
        int sampleCount = (int)Math.max(250.0, Math.PI * 4 * avgR * avgR * thickness * 0.025);
        sampleCount = Math.min(6000, sampleCount);
        BlockPos.MutableBlockPos mbp = new BlockPos.MutableBlockPos();
        int changed = 0;
        for (int i = 0; i < sampleCount && changed < 600; ++i) {
            double u = this.random.nextDouble() * 2.0 - 1.0;
            double phi = Math.acos(u);
            double theta = this.random.nextDouble() * Math.PI * 2.0;
            double r = (double)innerR + this.random.nextDouble() * (double)(outerR - innerR);
            double t = r / 120.0;
            double prob = 0.45 * (1.0 - t);
            if (this.random.nextDouble() > prob) continue;
            double sx = center.x + r * Math.sin(phi) * Math.cos(theta);
            double sy = center.y + r * Math.cos(phi);
            double sz = center.z + r * Math.sin(phi) * Math.sin(theta);
            mbp.set((int)sx, (int)sy, (int)sz);
            BlockState state = server.getBlockState((BlockPos)mbp);
            if (state.isAir() || SectProtectionDomeHandler.isProtectedByAnySectProtectionDome((Level)server, (BlockPos)mbp) || state.is(Blocks.BEDROCK) || state.is(Blocks.END_PORTAL_FRAME) || state.is(Blocks.OBSIDIAN) || state.is(Blocks.CRYING_OBSIDIAN) || state.is(Blocks.REINFORCED_DEEPSLATE) || !SpellTerrainDestructionHelper.setBlock(server, (BlockPos)mbp, air, 2, terrainCaster)) continue;
            ++changed;
        }
    }

    private Entity terrainCaster(ServerLevel server) {
        return this.ownerUuid == null ? this : server.getEntity(this.ownerUuid);
    }

    protected void readAdditionalSaveData(@NotNull CompoundTag tag) {
        this.age = tag.getInt("age");
        if (tag.contains("owner")) {
            this.ownerUuid = tag.getUUID("owner");
        }
        this.damageMultiplier = tag.contains("damageMultiplier") ? Math.max(0.0f, tag.getFloat("damageMultiplier")) : 1.0f;
    }

    protected void addAdditionalSaveData(@NotNull CompoundTag tag) {
        tag.putInt("age", this.age);
        if (this.ownerUuid != null) {
            tag.putUUID("owner", this.ownerUuid);
        }
        tag.putFloat("damageMultiplier", this.damageMultiplier);
    }

    @NotNull
    public Packet<ClientGamePacketListener> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket((Entity)this);
    }

    protected AABB makeBoundingBox() {
        float r = Math.max(2.0f, this.radius());
        return new AABB(this.getX() - (double)r, this.getY() - (double)r, this.getZ() - (double)r, this.getX() + (double)r, this.getY() + (double)r, this.getZ() + (double)r);
    }
}

