/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.BlockPos
 *  net.minecraft.core.BlockPos$MutableBlockPos
 *  net.minecraft.core.Position
 *  net.minecraft.core.Vec3i
 *  net.minecraft.core.particles.ParticleOptions
 *  net.minecraft.core.particles.ParticleTypes
 *  net.minecraft.nbt.CompoundTag
 *  net.minecraft.network.protocol.Packet
 *  net.minecraft.network.protocol.game.ClientGamePacketListener
 *  net.minecraft.network.syncher.EntityDataAccessor
 *  net.minecraft.network.syncher.EntityDataSerializer
 *  net.minecraft.network.syncher.EntityDataSerializers
 *  net.minecraft.network.syncher.SynchedEntityData
 *  net.minecraft.server.level.ServerLevel
 *  net.minecraft.sounds.SoundEvents
 *  net.minecraft.sounds.SoundSource
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.EntityType
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.entity.Mob
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.entity.projectile.ProjectileUtil
 *  net.minecraft.world.level.BlockGetter
 *  net.minecraft.world.level.ClipContext
 *  net.minecraft.world.level.ClipContext$Block
 *  net.minecraft.world.level.ClipContext$Fluid
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.level.block.Blocks
 *  net.minecraft.world.level.block.state.BlockState
 *  net.minecraft.world.phys.AABB
 *  net.minecraft.world.phys.BlockHitResult
 *  net.minecraft.world.phys.EntityHitResult
 *  net.minecraft.world.phys.HitResult
 *  net.minecraft.world.phys.HitResult$Type
 *  net.minecraft.world.phys.Vec3
 *  net.minecraftforge.network.NetworkHooks
 *  org.jetbrains.annotations.NotNull
 *  org.jetbrains.annotations.Nullable
 */
package com.friday.cultivation.entity;

import com.friday.cultivation.event.SectCombatHandler;
import com.friday.cultivation.event.SectProtectionDomeHandler;
import com.friday.cultivation.event.SoulStateHandler;
import com.friday.cultivation.event.TimeStasisHandler;
import com.friday.cultivation.registry.ModEntities;
import com.friday.cultivation.util.SpellDamageSourceHelper;
import com.friday.cultivation.util.SpellTerrainDestructionHelper;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Position;
import net.minecraft.core.Vec3i;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializer;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkHooks;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class SwordProjectileEntity
extends Entity {
    private static final EntityDataAccessor<Boolean> DATA_IMMORTAL = SynchedEntityData.defineId(SwordProjectileEntity.class, (EntityDataSerializer)EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> DATA_NO_TERRAIN = SynchedEntityData.defineId(SwordProjectileEntity.class, (EntityDataSerializer)EntityDataSerializers.BOOLEAN);
    private static final int MAX_LIFETIME = 200;
    private static final float DEFAULT_DIRECT_HIT_DAMAGE = 6.0f;
    private static final double IMPACT_BURST_RADIUS = 2.25;
    private static final float CONVERGENCE_IMPACT_MIN_DAMAGE = 20.0f;
    private static final double IMPACT_BLOCK_BREAK_RADIUS = 1.45;
    private static final int IMPACT_BLOCK_BREAK_LIMIT = 8;
    private static final double FLIGHT_SPEED = 1.5;
    private int lifetime = 0;
    private UUID ownerUuid;
    private float directHitDamage = 6.0f;
    private final ArrayDeque<Vec3> trailHistory = new ArrayDeque();
    public static final int TRAIL_HISTORY_LIMIT = 15;

    public SwordProjectileEntity(EntityType<? extends SwordProjectileEntity> type, Level level) {
        super(type, level);
        this.noPhysics = true;
    }

    public SwordProjectileEntity(Level level, LivingEntity owner, Vec3 spawnPos, Vec3 targetPos, boolean immortal) {
        this(level, owner, spawnPos, targetPos, immortal, false);
    }

    public SwordProjectileEntity(Level level, LivingEntity owner, Vec3 spawnPos, Vec3 targetPos, boolean immortal, boolean noTerrain) {
        this((EntityType<? extends SwordProjectileEntity>)((EntityType)ModEntities.SWORD_PROJECTILE.get()), level);
        this.ownerUuid = owner.getUUID();
        this.setPos(spawnPos.x, spawnPos.y, spawnPos.z);
        Vec3 dir = SwordProjectileEntity.directionTowardTarget(owner, spawnPos, targetPos);
        this.setDeltaMovement(dir.scale(1.5));
        this.entityData.set(DATA_IMMORTAL, immortal);
        this.entityData.set(DATA_NO_TERRAIN, noTerrain);
    }

    public void setDirectHitDamage(float damage) {
        this.directHitDamage = damage;
    }

    public float getDirectHitDamage() {
        return this.directHitDamage;
    }

    public static float scaledConvergenceDamage(double configuredDamage, double multiplier) {
        return Math.max(20.0f, (float)(Math.max(20.0, configuredDamage) * multiplier));
    }

    protected void defineSynchedData() {
        this.entityData.define(DATA_IMMORTAL, false);
        this.entityData.define(DATA_NO_TERRAIN, false);
    }

    public boolean isImmortal() {
        return (Boolean)this.entityData.get(DATA_IMMORTAL);
    }

    public void setImmortal(boolean v) {
        this.entityData.set(DATA_IMMORTAL, v);
    }

    public boolean isNoTerrain() {
        return (Boolean)this.entityData.get(DATA_NO_TERRAIN);
    }

    public Deque<Vec3> getTrailHistory() {
        return this.trailHistory;
    }

    public UUID getOwnerUuid() {
        return this.ownerUuid;
    }

    private Player getOwnerPlayer() {
        Player p;
        LivingEntity owner = this.getOwner();
        return owner instanceof Player ? (p = (Player)owner) : null;
    }

    private LivingEntity getOwner() {
        ServerLevel server;
        Entity e;
        if (this.ownerUuid == null) {
            return null;
        }
        Level level = this.level();
        if (level instanceof ServerLevel && (e = (server = (ServerLevel)level).getEntity(this.ownerUuid)) instanceof LivingEntity) {
            LivingEntity le = (LivingEntity)e;
            return le;
        }
        return null;
    }

    public void tick() {
        Vec3 aimPos;
        LivingEntity owner;
        if (TimeStasisHandler.pauseEntityTickInStoppedTime(this)) {
            return;
        }
        super.tick();
        if (this.level().isClientSide) {
            this.spawnTrailParticles();
            this.recordTrailHistory();
            return;
        }
        ++this.lifetime;
        if (this.lifetime > 200) {
            this.discard();
            return;
        }
        if (this.isImmortal() && (owner = this.getOwner()) != null && (aimPos = SwordProjectileEntity.computeImmortalAim(owner)) != null) {
            Vec3 direction = SwordProjectileEntity.directionTowardTarget(owner, this.position(), aimPos);
            this.setDeltaMovement(direction.scale(1.5));
        }
        Vec3 vel = this.getDeltaMovement();
        Vec3 oldPos = this.position();
        Vec3 newPos = oldPos.add(vel);
        HitResult hit = ProjectileUtil.getHitResultOnMoveVector((Entity)this, this::canHitEntity);
        if (hit.getType() != HitResult.Type.MISS) {
            this.onHit(hit);
            return;
        }
        this.setPos(newPos.x, newPos.y, newPos.z);
    }

    private static Vec3 computeImmortalAim(LivingEntity owner) {
        Mob mob;
        LivingEntity tgt;
        if (owner instanceof Player) {
            Player p = (Player)owner;
            Vec3 eye = p.getEyePosition();
            Vec3 end = eye.add(p.getLookAngle().scale(1000.0));
            BlockHitResult hit = p.level().clip(new ClipContext(eye, end, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, (Entity)p));
            return hit.getType() == HitResult.Type.MISS ? end : hit.getLocation();
        }
        if (owner instanceof Mob && (tgt = (mob = (Mob)owner).getTarget()) != null && tgt.isAlive()) {
            return tgt.getEyePosition();
        }
        return owner.getEyePosition().add(owner.getLookAngle().scale(30.0));
    }

    private boolean canHitEntity(Entity e) {
        if (e == this) {
            return false;
        }
        if (this.ownerUuid != null && e.getUUID().equals(this.ownerUuid)) {
            return false;
        }
        if (!e.isAlive()) {
            return false;
        }
        return e.isPickable();
    }

    private void onHit(HitResult result) {
        LivingEntity target;
        EntityHitResult ent;
        Entity entity;
        Level level = this.level();
        if (!(level instanceof ServerLevel)) {
            return;
        }
        ServerLevel server = (ServerLevel)level;
        Vec3 pos = result.getLocation();
        LivingEntity directTarget = null;
        if (result instanceof EntityHitResult && (entity = (ent = (EntityHitResult)result).getEntity()) instanceof LivingEntity && (target = (LivingEntity)entity).isAlive()) {
            directTarget = target;
        }
        this.doImpact(server, pos, directTarget, true);
    }

    public void impactOnBarrier(Vec3 hitPos) {
        Level level = this.level();
        if (!(level instanceof ServerLevel)) {
            return;
        }
        ServerLevel server = (ServerLevel)level;
        this.doImpact(server, hitPos, null, false);
    }

    private float barrierDamageEquivalent() {
        return this.impactDamage();
    }

    public float getBarrierDamageEquivalent() {
        return this.barrierDamageEquivalent();
    }

    private void doImpact(ServerLevel server, Vec3 pos, @Nullable LivingEntity directTarget, boolean touchBarrierArea) {
        LivingEntity owner = this.getOwner();
        if (touchBarrierArea) {
            SectProtectionDomeHandler.onSpellAreaTouchedBarrier(server, pos, 1.45, (Entity)(owner != null ? owner : this), this.impactDamage());
        }
        if (directTarget != null && directTarget.isAlive() && SoulStateHandler.canOrdinaryAffect((Entity)owner, (Entity)directTarget) && SectCombatHandler.canApplyOffensiveEffect(owner, directTarget) && !SectProtectionDomeHandler.isEntityProtectedByOwnDome((Entity)directTarget)) {
            double originalY = directTarget.getDeltaMovement().y;
            directTarget.hurt(SpellDamageSourceHelper.indirectSpell(this, owner), this.impactDamage());
            SwordProjectileEntity.suppressUpwardKnockback(directTarget, originalY);
        }
        if (!this.isNoTerrain()) {
            this.spawnImpactBurst(server, pos);
            this.destroyImpactBlocks(server, pos, owner);
            this.damageImpactBurst(server, pos, owner, directTarget);
        }
        if (this.isImmortal() && owner != null) {
            this.respawnNearOwner(owner);
        } else {
            this.discard();
        }
    }

    private float impactDamage() {
        return Math.max(20.0f, this.directHitDamage);
    }

    private void spawnImpactBurst(ServerLevel server, Vec3 pos) {
        server.sendParticles((ParticleOptions)ParticleTypes.EXPLOSION, pos.x, pos.y, pos.z, 1, 0.0, 0.0, 0.0, 0.0);
        server.sendParticles((ParticleOptions)ParticleTypes.ENCHANTED_HIT, pos.x, pos.y, pos.z, 18, 0.35, 0.25, 0.35, 0.08);
        server.sendParticles((ParticleOptions)ParticleTypes.CRIT, pos.x, pos.y, pos.z, 10, 0.25, 0.18, 0.25, 0.05);
        server.playSound(null, pos.x, pos.y, pos.z, SoundEvents.PLAYER_ATTACK_SWEEP, SoundSource.PLAYERS, 0.8f, 1.25f + this.random.nextFloat() * 0.25f);
    }

    private void damageImpactBurst(ServerLevel server, Vec3 pos, @Nullable LivingEntity owner, @Nullable LivingEntity directTarget) {
        AABB area = new AABB(pos.x - 2.25, pos.y - 2.25, pos.z - 2.25, pos.x + 2.25, pos.y + 2.25, pos.z + 2.25);
        double radiusSqr = 5.0625;
        for (LivingEntity target : server.getEntitiesOfClass(LivingEntity.class, area, e -> e.isAlive() && e != owner && e != directTarget && SoulStateHandler.canOrdinaryAffect((Entity)owner, (Entity)e) && SectCombatHandler.canApplyOffensiveEffect(owner, e) && !SectProtectionDomeHandler.isEntityProtectedByOwnDome((Entity)e) && e.distanceToSqr(pos) <= radiusSqr)) {
            double originalY = target.getDeltaMovement().y;
            target.hurt(SpellDamageSourceHelper.indirectSpell(this, owner), this.impactDamage());
            SwordProjectileEntity.suppressUpwardKnockback(target, originalY);
        }
    }

    private void destroyImpactBlocks(ServerLevel server, Vec3 pos, @Nullable LivingEntity owner) {
        if (!SpellTerrainDestructionHelper.canModifyBlocks(server, (Entity)owner)) {
            return;
        }
        int radius = (int)Math.ceil(1.45);
        double radiusSqr = 2.1025;
        BlockPos center = BlockPos.containing((Position)pos);
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        int broken = 0;
        for (int x = -radius; x <= radius; ++x) {
            for (int y = -radius; y <= radius; ++y) {
                for (int z = -radius; z <= radius; ++z) {
                    BlockPos targetPos;
                    cursor.set(center.getX() + x, center.getY() + y, center.getZ() + z);
                    if (Vec3.atCenterOf((Vec3i)cursor).distanceToSqr(pos) > radiusSqr || !SwordProjectileEntity.canBreakImpactBlock(server, (BlockPos)cursor) || !SpellTerrainDestructionHelper.destroyBlock(server, targetPos = cursor.east(), true, (Entity)owner, (Entity)owner) || ++broken < 8) continue;
                    return;
                }
            }
        }
    }

    private static boolean canBreakImpactBlock(ServerLevel server, BlockPos pos) {
        if (SectProtectionDomeHandler.isProtectedByAnySectProtectionDome((Level)server, pos)) {
            return false;
        }
        BlockState state = server.getBlockState(pos);
        if (state.isAir()) {
            return false;
        }
        if (server.getBlockEntity(pos) != null) {
            return false;
        }
        if (state.getDestroySpeed(server, pos) < 0.0f) {
            return false;
        }
        return !state.is(Blocks.BEDROCK) && !state.is(Blocks.END_PORTAL_FRAME) && !state.is(Blocks.OBSIDIAN) && !state.is(Blocks.CRYING_OBSIDIAN) && !state.is(Blocks.REINFORCED_DEEPSLATE);
    }

    private static void suppressUpwardKnockback(LivingEntity target, double originalY) {
        Vec3 motion = target.getDeltaMovement();
        if (motion.y > originalY) {
            target.setDeltaMovement(motion.x, originalY, motion.z);
        }
    }

    private void respawnNearOwner(LivingEntity owner) {
        double angle = this.random.nextDouble() * Math.PI * 2.0;
        double dist = 3.0 + this.random.nextDouble() * 2.0;
        double newX = owner.getX() + Math.cos(angle) * dist;
        double newY = owner.getY() + 1.0 + this.random.nextDouble() * 2.0;
        double newZ = owner.getZ() + Math.sin(angle) * dist;
        this.setPos(newX, newY, newZ);
        Vec3 aimPos = SwordProjectileEntity.computeImmortalAim(owner);
        if (aimPos != null) {
            Vec3 direction = SwordProjectileEntity.directionTowardTarget(owner, this.position(), aimPos);
            this.setDeltaMovement(direction.scale(1.5));
        }
    }

    private static Vec3 directionTowardTarget(LivingEntity owner, Vec3 from, Vec3 target) {
        Vec3 delta = target.multiply(from);
        if (delta.lengthSqr() > 1.0E-6) {
            return delta.normalize();
        }
        Vec3 look = owner.getLookAngle();
        if (look.lengthSqr() > 1.0E-6) {
            return look.normalize();
        }
        return new Vec3(0.0, 0.0, 1.0);
    }

    private void recordTrailHistory() {
        if (!this.isNoTerrain()) {
            return;
        }
        this.trailHistory.addFirst(new Vec3(this.getX(), this.getY(), this.getZ()));
        while (this.trailHistory.size() > 15) {
            this.trailHistory.removeLast();
        }
    }

    private void spawnTrailParticles() {
        if (this.isNoTerrain()) {
            return;
        }
        Vec3 vel = this.getDeltaMovement();
        double sp = vel.length();
        if (sp < 0.01) {
            return;
        }
        Vec3 dir = vel.scale(1.0 / sp);
        double tailX = this.getX() - dir.x * 0.5;
        double tailY = this.getY() - dir.y * 0.5;
        double tailZ = this.getZ() - dir.z * 0.5;
        this.level().addParticle((ParticleOptions)ParticleTypes.ENCHANTED_HIT, tailX, tailY, tailZ, 0.0, 0.0, 0.0);
        this.level().addParticle((ParticleOptions)ParticleTypes.CRIT, tailX + (this.random.nextDouble() - 0.5) * 0.2, tailY + (this.random.nextDouble() - 0.5) * 0.2, tailZ + (this.random.nextDouble() - 0.5) * 0.2, -dir.x * 0.05, -dir.y * 0.05, -dir.z * 0.05);
    }

    protected void readAdditionalSaveData(@NotNull CompoundTag tag) {
        this.lifetime = tag.getInt("lifetime");
        if (tag.contains("owner")) {
            this.ownerUuid = tag.getUUID("owner");
        }
        this.entityData.set(DATA_IMMORTAL, tag.getBoolean("immortal"));
        this.entityData.set(DATA_NO_TERRAIN, tag.getBoolean("noTerrain"));
        if (tag.contains("directHitDamage")) {
            this.directHitDamage = tag.getFloat("directHitDamage");
        }
    }

    protected void addAdditionalSaveData(@NotNull CompoundTag tag) {
        tag.putInt("lifetime", this.lifetime);
        if (this.ownerUuid != null) {
            tag.putUUID("owner", this.ownerUuid);
        }
        tag.putBoolean("immortal", this.isImmortal());
        tag.putBoolean("noTerrain", this.isNoTerrain());
        tag.putFloat("directHitDamage", this.directHitDamage);
    }

    @NotNull
    public Packet<ClientGamePacketListener> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket((Entity)this);
    }

    protected AABB makeBoundingBox() {
        return super.makeBoundingBox();
    }
}

