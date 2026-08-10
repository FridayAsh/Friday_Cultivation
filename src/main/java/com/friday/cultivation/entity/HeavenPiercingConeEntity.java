/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.BlockPos
 *  net.minecraft.core.BlockPos$MutableBlockPos
 *  net.minecraft.core.Position
 *  net.minecraft.core.Vec3i
 *  net.minecraft.core.particles.BlockParticleOption
 *  net.minecraft.core.particles.ParticleOptions
 *  net.minecraft.core.particles.ParticleTypes
 *  net.minecraft.nbt.CompoundTag
 *  net.minecraft.network.FriendlyByteBuf
 *  net.minecraft.network.protocol.Packet
 *  net.minecraft.network.protocol.game.ClientGamePacketListener
 *  net.minecraft.network.syncher.EntityDataAccessor
 *  net.minecraft.network.syncher.EntityDataSerializer
 *  net.minecraft.network.syncher.EntityDataSerializers
 *  net.minecraft.network.syncher.SynchedEntityData
 *  net.minecraft.server.level.ServerLevel
 *  net.minecraft.sounds.SoundEvents
 *  net.minecraft.sounds.SoundSource
 *  net.minecraft.util.Mth
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.EntityType
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.entity.projectile.ProjectileUtil
 *  net.minecraft.world.level.BlockGetter
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.level.Level$ExplosionInteraction
 *  net.minecraft.world.level.block.Blocks
 *  net.minecraft.world.level.block.state.BlockState
 *  net.minecraft.world.phys.BlockHitResult
 *  net.minecraft.world.phys.EntityHitResult
 *  net.minecraft.world.phys.HitResult
 *  net.minecraft.world.phys.HitResult$Type
 *  net.minecraft.world.phys.Vec3
 *  net.minecraftforge.entity.IEntityAdditionalSpawnData
 *  net.minecraftforge.network.NetworkHooks
 *  org.jetbrains.annotations.NotNull
 *  org.jetbrains.annotations.Nullable
 */
package com.friday.cultivation.entity;

import com.friday.cultivation.block.formation.SectProtectionBarrierBlock;
import com.friday.cultivation.cultivation.spell.Spell;
import com.friday.cultivation.event.SectCombatHandler;
import com.friday.cultivation.event.SectProtectionDomeHandler;
import com.friday.cultivation.event.SoulStateHandler;
import com.friday.cultivation.event.TimeStasisHandler;
import com.friday.cultivation.registry.ModEntities;
import com.friday.cultivation.util.SpellDamageSourceHelper;
import com.friday.cultivation.util.SpellTerrainDestructionHelper;
import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Position;
import net.minecraft.core.Vec3i;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializer;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.entity.IEntityAdditionalSpawnData;
import net.minecraftforge.network.NetworkHooks;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class HeavenPiercingConeEntity
extends Entity
implements IEntityAdditionalSpawnData {
    private static final EntityDataAccessor<Integer> DATA_STAGE = SynchedEntityData.defineId(HeavenPiercingConeEntity.class, (EntityDataSerializer)EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DATA_CHARGE_TICKS = SynchedEntityData.defineId(HeavenPiercingConeEntity.class, (EntityDataSerializer)EntityDataSerializers.INT);
    public static final double BASE_SPEED = 3.2;
    public static final double TAP_SPEED = 3.4;
    public static final double MAX_SPEED = 36.0;
    public static final double PREVIEW_FORWARD_OFFSET = 2.35;
    public static final double PREVIEW_RIGHT_OFFSET = 0.85;
    public static final double PREVIEW_DOWN_OFFSET = 0.45;
    public static final double LAUNCH_FORWARD_OFFSET = 1.35;
    public static final double LAUNCH_RIGHT_OFFSET = 0.85;
    public static final double LAUNCH_DOWN_OFFSET = 0.35;
    private static final int MAX_LIFETIME = 420;
    private static final double BARRIER_REACH_RADIUS = 0.75;
    private static final double DRILL_RADIUS_MULTIPLIER = 5.0;
    private static final double DRILL_TICKS_FOR_100_PERCENT = 100.0;
    private static final double DRILL_MIN_DEPTH = 2.5;
    private static final double DRILL_MAX_DEPTH = 160.0;
    private static final double DRILL_SAMPLE_SPACING = 0.55;
    private static final double DRILL_TAPER_START = 0.68;
    private static final int AIRWAVE_RING_POINTS = 18;
    private static final Vec3 WORLD_UP = new Vec3(0.0, 1.0, 0.0);
    private static final int MAX_BLOCK_BREAKS_STAGE_ONE = 250;
    private static final int MAX_BLOCK_BREAKS_STAGE_TWO = 800;
    private static final int MAX_BLOCK_BREAKS_STAGE_THREE = 2150;
    private static final int MAX_BLOCK_BREAKS_STAGE_FOUR = 3750;
    private UUID ownerUuid;
    private float damage = 60.0f;
    private double configuredSpeed = 3.4;
    private boolean tapShot = true;
    private int lifetime;
    private boolean drilling;
    private Vec3 drillOrigin = Vec3.ZERO;
    private Vec3 drillDirection = Vec3.ZERO;
    private double drillProgress;
    private double drillDepth;
    private int drillBreakLimit;
    private int drillBroken;
    private final Set<BlockPos> drillVisited = new HashSet<BlockPos>();
    private final ArrayDeque<BlockPos> drillPendingBlocks = new ArrayDeque();

    public HeavenPiercingConeEntity(EntityType<? extends HeavenPiercingConeEntity> type, Level level) {
        super(type, level);
        this.noPhysics = true;
    }

    public HeavenPiercingConeEntity(Level level, LivingEntity owner) {
        this((EntityType<? extends HeavenPiercingConeEntity>)((EntityType)ModEntities.HEAVEN_PIERCING_CONE.get()), level);
        this.ownerUuid = owner.getUUID();
    }

    public void configure(float damage, double speed, int stage, int chargeTicks, boolean tapShot) {
        this.damage = Math.max(0.0f, damage);
        this.configuredSpeed = Math.max(0.0, speed);
        this.tapShot = tapShot;
        this.entityData.set(DATA_STAGE, Mth.clamp((int)stage, (int)1, (int)4));
        this.entityData.set(DATA_CHARGE_TICKS, Math.max(0, chargeTicks));
    }

    public static int stageForChargeTicks(int ticks) {
        if (ticks < 20) {
            return 1;
        }
        if (ticks < 60) {
            return 2;
        }
        if (ticks < 100) {
            return 3;
        }
        return 4;
    }

    public static double velocityForChargeTicks(int ticks) {
        return Math.min(36.0, 3.2 + (double)Math.max(0, ticks) * 0.1);
    }

    public static Vec3 safeDirection(Vec3 direction) {
        if (direction.lengthSqr() < 1.0E-6) {
            return new Vec3(0.0, 0.0, 1.0);
        }
        return direction.normalize();
    }

    public static Vec3 rightSideOffset(Vec3 direction, double rightDistance, double downDistance) {
        Vec3 forward = HeavenPiercingConeEntity.safeDirection(direction);
        Vec3 right = forward.cross(WORLD_UP);
        right = right.lengthSqr() < 1.0E-6 ? new Vec3(1.0, 0.0, 0.0) : right.normalize();
        Vec3 up = right.cross(forward);
        up = up.lengthSqr() < 1.0E-6 ? WORLD_UP : up.normalize();
        return right.scale(rightDistance).multiply(up.scale(downDistance));
    }

    public static Vec3 previewPosition(LivingEntity caster, Vec3 direction) {
        Vec3 forward = HeavenPiercingConeEntity.safeDirection(direction);
        return caster.getEyePosition().add(forward.scale(2.35)).add(HeavenPiercingConeEntity.rightSideOffset(forward, 0.85, 0.45));
    }

    public static Vec3 sideLaunchPosition(LivingEntity caster, Vec3 direction) {
        Vec3 forward = HeavenPiercingConeEntity.safeDirection(direction);
        return caster.getEyePosition().add(forward.scale(1.35)).add(HeavenPiercingConeEntity.rightSideOffset(forward, 0.85, 0.35));
    }

    public static Vec3 safeSideLaunchPosition(Level level, LivingEntity caster, Vec3 direction) {
        Vec3 forward = HeavenPiercingConeEntity.safeDirection(direction);
        Vec3 sidePos = HeavenPiercingConeEntity.sideLaunchPosition(caster, forward);
        if (!HeavenPiercingConeEntity.isLaunchBlocked(level, sidePos)) {
            return sidePos;
        }
        Vec3 closeForwardPos = caster.getEyePosition().add(forward.scale(0.85)).add(0.0, -0.18, 0.0);
        if (!HeavenPiercingConeEntity.isLaunchBlocked(level, closeForwardPos)) {
            return closeForwardPos;
        }
        return caster.getEyePosition().add(forward.scale(0.35));
    }

    public static Vec3 aimDirectionFromSide(Vec3 spawnPos, Vec3 targetPos, Vec3 fallbackDirection) {
        Vec3 fallback = HeavenPiercingConeEntity.safeDirection(fallbackDirection);
        Vec3 aimed = targetPos.multiply(spawnPos);
        if (aimed.lengthSqr() < 0.25) {
            return fallback;
        }
        Vec3 aimedDirection = aimed.normalize();
        return aimedDirection.dot(fallback) < 0.18 ? fallback : aimedDirection;
    }

    private static boolean isLaunchBlocked(Level level, Vec3 position) {
        BlockPos blockPos = BlockPos.containing((Position)position);
        return !level.getBlockState(blockPos).getCollisionShape(level, blockPos).isEmpty();
    }

    public int stage() {
        return (Integer)this.entityData.get(DATA_STAGE);
    }

    public int visualStage() {
        return Mth.clamp((int)Math.max(this.stage(), HeavenPiercingConeEntity.stageForChargeTicks(this.chargeTicks())), (int)1, (int)4);
    }

    public int chargeTicks() {
        return (Integer)this.entityData.get(DATA_CHARGE_TICKS);
    }

    public float getDamage() {
        return this.damage;
    }

    public UUID getOwnerUuid() {
        return this.ownerUuid;
    }

    @Nullable
    public LivingEntity getOwnerEntity(ServerLevel level) {
        LivingEntity living;
        if (this.ownerUuid == null) {
            return null;
        }
        Entity entity = level.getEntity(this.ownerUuid);
        return entity instanceof LivingEntity ? (living = (LivingEntity)entity) : null;
    }

    public float rollRad(float partialTick) {
        float spin = 0.26f + (float)this.visualStage() * 0.28f + (float)this.chargeTicks() * 0.004f;
        return ((float)this.tickCount + partialTick) * spin;
    }

    public void impactOnBarrier(Vec3 hitPos) {
        Level level = this.level();
        if (level instanceof ServerLevel) {
            ServerLevel server = (ServerLevel)level;
            this.spawnImpactFx(server, hitPos, false);
        }
        this.discard();
    }

    protected void defineSynchedData() {
        this.entityData.define(DATA_STAGE, 1);
        this.entityData.define(DATA_CHARGE_TICKS, 0);
    }

    public void canEat() {
        LivingEntity owner;
        if (TimeStasisHandler.pauseEntityTickInStoppedTime(this)) {
            return;
        }
        super.tick();
        if (this.level().isClientSide) {
            this.spawnTrailParticles();
            return;
        }
        Level level = this.level();
        if (!(level instanceof ServerLevel)) {
            return;
        }
        ServerLevel server = (ServerLevel)level;
        ++this.lifetime;
        if (this.lifetime > 420) {
            this.discard();
            return;
        }
        if (this.drilling) {
            this.processDrilling(server);
            return;
        }
        Vec3 oldPos = this.position();
        Vec3 movement = this.getDeltaMovement();
        if (movement.lengthSqr() < 1.0E-6) {
            this.discard();
            return;
        }
        Vec3 newPos = oldPos.add(movement);
        SectProtectionDomeHandler.BarrierHit barrierHit = SectProtectionDomeHandler.touchProjectileBarrier(server, this, oldPos, newPos, owner = this.getOwnerEntity(server), this.damage, 0.75);
        if (barrierHit != null) {
            this.impactOnBarrier(barrierHit.hitPos());
            return;
        }
        HitResult hit = ProjectileUtil.getHitResultOnMoveVector((Entity)this, this::canHitEntity);
        if (hit.getType() != HitResult.Type.MISS) {
            this.onImpact(server, hit);
            return;
        }
        this.setPos(newPos.x, newPos.y, newPos.z);
    }

    private boolean canHitEntity(Entity entity) {
        if (entity == this) {
            return false;
        }
        if (!entity.isAlive()) {
            return false;
        }
        if (this.ownerUuid != null && this.ownerUuid.equals(entity.getUUID())) {
            return false;
        }
        return entity.isPickable();
    }

    private void onImpact(ServerLevel server, HitResult result) {
        LivingEntity target;
        EntityHitResult entityHit;
        Entity entity;
        BlockHitResult blockHit;
        BlockPos blockPos;
        Vec3 hitPos = result.getLocation();
        LivingEntity owner = this.getOwnerEntity(server);
        if (result instanceof BlockHitResult && (SectProtectionDomeHandler.domeOwningProtectedShell((Level)server, blockPos = (blockHit = (BlockHitResult)result).getBlockPos()) != null || server.getBlockState(blockPos).getBlock() instanceof SectProtectionBarrierBlock)) {
            SectProtectionDomeHandler.onBarrierTouched((Level)server, blockPos, hitPos, this.damage);
            this.impactOnBarrier(hitPos);
            return;
        }
        if (result instanceof EntityHitResult && (entity = (entityHit = (EntityHitResult)result).getEntity()) instanceof LivingEntity && (target = (LivingEntity)entity).isAlive() && SoulStateHandler.canOrdinaryAffect((Entity)owner, (Entity)target) && SectCombatHandler.canApplyOffensiveEffect(owner, target) && !SectProtectionDomeHandler.isEntityProtectedByOwnDome((Entity)target)) {
            target.hurt(SpellDamageSourceHelper.indirectSpell(this, owner), this.damage);
        }
        this.spawnImpactFx(server, hitPos, true);
        if (this.stage() >= 3) {
            float explosionPower = this.scaledExplosionPower(this.stage() >= 4 ? 2.4f : 1.6f);
            SectProtectionDomeHandler.onSpellAreaTouchedBarrier(server, hitPos, Math.max(2.0, (double)explosionPower + 2.0), (Entity)(owner != null ? owner : this), this.damage);
            SectProtectionDomeHandler.recordExternalBarrierExplosion(server, SectProtectionDomeHandler.domeContaining((Level)server, hitPos.x, hitPos.y, hitPos.z), hitPos);
            server.explode((Entity)this, hitPos.x, hitPos.y, hitPos.z, explosionPower, Level.ExplosionInteraction.NONE);
        }
        this.beginDrilling(server, hitPos, this.getDeltaMovement());
    }

    private float scaledExplosionPower(float basePower) {
        double multiplier = this.damage / Math.max(1.0f, (float)Spell.HEAVEN_PIERCING_CONE.damage());
        return (float)Math.min(5.0, (double)basePower * Math.sqrt(Math.max(0.0, multiplier)));
    }

    private void beginDrilling(ServerLevel server, Vec3 hitPos, Vec3 impactVelocity) {
        LivingEntity owner = this.getOwnerEntity(server);
        if (!SpellTerrainDestructionHelper.canModifyBlocks(server, (Entity)owner)) {
            this.discard();
            return;
        }
        Vec3 dir = HeavenPiercingConeEntity.safeDirection(impactVelocity);
        this.drillOrigin = hitPos;
        this.drillDirection = dir;
        this.drillProgress = 0.2;
        this.drillDepth = this.computeDrillDepth();
        this.drillBreakLimit = this.computeDrillBreakLimit(this.drillDepth);
        this.drillBroken = 0;
        this.drillVisited.clear();
        this.drillPendingBlocks.clear();
        this.drilling = this.drillDepth > 0.0;
        this.setDeltaMovement(Vec3.ZERO);
        this.setPos(hitPos.x, hitPos.y, hitPos.z);
        if (!this.drilling) {
            this.discard();
        }
    }

    private void processDrilling(ServerLevel server) {
        LivingEntity owner = this.getOwnerEntity(server);
        int remainingBudget = this.destroyPendingDrillBlocks(server, owner, this.drillBlocksPerTick());
        for (int slices = 0; remainingBudget > 0 && this.drillPendingBlocks.isEmpty() && this.drillProgress <= this.drillDepth && this.drillBroken < this.drillBreakLimit && slices < this.drillSlicesPerTick(); ++slices) {
            this.enqueueDrillDisk(server, this.drillProgress);
            this.drillProgress += 0.55;
            remainingBudget = this.destroyPendingDrillBlocks(server, owner, remainingBudget);
        }
        Vec3 head = this.drillOrigin.add(this.drillDirection.scale(Math.min(this.drillProgress, this.drillDepth)));
        this.setPos(head.x, head.y, head.z);
        this.spawnDrillFx(server, head);
        if (this.drillProgress > this.drillDepth && this.drillPendingBlocks.isEmpty() || this.drillBroken >= this.drillBreakLimit) {
            this.drilling = false;
            this.discard();
        }
    }

    private int drillBlocksPerTick() {
        return switch (this.visualStage()) {
            case 1 -> 160;
            case 2 -> 288;
            case 3 -> 512;
            default -> 768;
        };
    }

    private int drillSlicesPerTick() {
        return switch (this.visualStage()) {
            case 1 -> 3;
            case 2 -> 4;
            case 3 -> 5;
            default -> 6;
        };
    }

    private int destroyPendingDrillBlocks(ServerLevel server, @Nullable LivingEntity owner, int budget) {
        int broken = 0;
        while (broken < budget && this.drillBroken < this.drillBreakLimit && !this.drillPendingBlocks.isEmpty()) {
            BlockPos targetPos = this.drillPendingBlocks.removeFirst();
            if (!HeavenPiercingConeEntity.canBreakImpactBlock(server, targetPos) || !SpellTerrainDestructionHelper.setBlock(server, targetPos, Blocks.AIR.defaultBlockState(), 3, (Entity)owner)) continue;
            ++broken;
            ++this.drillBroken;
        }
        return budget - broken;
    }

    private void enqueueDrillDisk(ServerLevel server, double distance) {
        double radius = this.drillRadiusAt(distance);
        if (radius <= 0.01) {
            return;
        }
        Vec3 centerVec = this.drillOrigin.add(this.drillDirection.scale(distance));
        BlockPos center = BlockPos.containing((Position)centerVec);
        int r = Math.max(1, (int)Math.ceil(radius));
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        for (int shell = 0; shell <= r; ++shell) {
            for (int x = -shell; x <= shell; ++x) {
                for (int y = -shell; y <= shell; ++y) {
                    for (int z = -shell; z <= shell; ++z) {
                        BlockPos targetPos;
                        double radialSqr;
                        if (Math.max(Math.max(Math.abs(x), Math.abs(y)), Math.abs(z)) != shell) continue;
                        cursor.set(center.getX() + x, center.getY() + y, center.getZ() + z);
                        Vec3 delta = Vec3.atCenterOf((Vec3i)cursor).multiply(centerVec);
                        double axial = delta.dot(this.drillDirection);
                        if (Math.abs(axial) > 0.41250000000000003 || (radialSqr = delta.lengthSqr() - axial * axial) > radius * radius + 0.12 || !HeavenPiercingConeEntity.canBreakImpactBlock(server, targetPos = cursor.east()) || !this.drillVisited.add(targetPos)) continue;
                        this.drillPendingBlocks.addLast(targetPos);
                    }
                }
            }
        }
    }

    private double computeDrillDepth() {
        if (this.tapShot) {
            return 2.5;
        }
        double chargePercent = (double)Math.max(0, this.chargeTicks()) / 100.0;
        return Mth.clamp((double)(6.0 + chargePercent * 20.0), (double)6.0, (double)160.0);
    }

    private int computeDrillBreakLimit(double depth) {
        int baseLimit = switch (this.visualStage()) {
            case 1 -> 250;
            case 2 -> 800;
            case 3 -> 2150;
            default -> 3750;
        };
        double depthScale = Math.max(1.0, depth / 26.0);
        return (int)Math.min(20000.0, Math.ceil((double)baseLimit * depthScale));
    }

    private double drillRadiusAt(double distance) {
        double radius = (switch (this.visualStage()) {
            case 1 -> 0.35;
            case 2 -> 0.7;
            case 3 -> 1.05;
            default -> 1.25;
        }) * 5.0;
        if (this.drillDepth <= 0.0) {
            return radius;
        }
        double progress = Mth.clamp((double)(distance / this.drillDepth), (double)0.0, (double)1.0);
        if (progress <= 0.68) {
            return radius;
        }
        double taper = (1.0 - progress) / 0.31999999999999995;
        return Math.max(0.18, radius * Mth.clamp((double)taper, (double)0.0, (double)1.0));
    }

    private void spawnDrillFx(ServerLevel server, Vec3 pos) {
        server.sendParticles((ParticleOptions)new BlockParticleOption(ParticleTypes.BLOCK, Blocks.DEEPSLATE.defaultBlockState()), pos.x, pos.y, pos.z, this.visualStage() >= 3 ? 10 : 6, 0.16, 0.16, 0.16, 0.04);
        if (this.visualStage() >= 3) {
            server.sendParticles((ParticleOptions)ParticleTypes.CLOUD, pos.x, pos.y, pos.z, 5, 0.18, 0.18, 0.18, 0.02);
        }
        this.spawnServerAirwaveRing(server, pos, this.drillDirection);
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

    private void spawnTrailParticles() {
        Vec3 velocity = this.getDeltaMovement();
        if (velocity.lengthSqr() < 1.0E-6) {
            return;
        }
        Vec3 dir = velocity.normalize();
        Vec3 tail = this.position().multiply(dir.scale(0.55));
        int stage = this.visualStage();
        this.level().addParticle((ParticleOptions)new BlockParticleOption(ParticleTypes.FALLING_DUST, Blocks.DEEPSLATE.defaultBlockState()), tail.x, tail.y, tail.z, 0.0, 0.0, 0.0);
        if (stage >= 2) {
            this.level().addParticle((ParticleOptions)ParticleTypes.CLOUD, tail.x, tail.y, tail.z, -dir.x * 0.06, -dir.y * 0.06, -dir.z * 0.06);
        }
        if (stage >= 3) {
            this.level().addParticle((ParticleOptions)ParticleTypes.FLAME, tail.x, tail.y, tail.z, -dir.x * 0.04, -dir.y * 0.04, -dir.z * 0.04);
        }
        if (stage >= 4) {
            this.level().addParticle((ParticleOptions)ParticleTypes.LAVA, tail.x, tail.y, tail.z, 0.0, 0.0, 0.0);
        }
        if ((this.tickCount & 1) == 0) {
            this.spawnClientAirwaveRing(this.position().add(dir.scale(0.15)), dir);
        }
    }

    private void spawnImpactFx(ServerLevel server, Vec3 pos, boolean heavy) {
        server.sendParticles((ParticleOptions)new BlockParticleOption(ParticleTypes.BLOCK, Blocks.DEEPSLATE.defaultBlockState()), pos.x, pos.y, pos.z, heavy ? 28 : 12, 0.25, 0.25, 0.25, 0.09);
        server.sendParticles((ParticleOptions)(this.visualStage() >= 3 ? ParticleTypes.FLAME : ParticleTypes.CRIT), pos.x, pos.y, pos.z, heavy ? 18 : 8, 0.22, 0.2, 0.22, 0.05);
        server.playSound(null, pos.x, pos.y, pos.z, this.visualStage() >= 3 ? SoundEvents.GENERIC_EXPLODE : SoundEvents.STONE_BREAK, SoundSource.PLAYERS, heavy ? 0.85f : 0.45f, this.visualStage() >= 4 ? 1.45f : 1.15f);
        this.spawnServerAirwaveRing(server, pos, HeavenPiercingConeEntity.safeDirection(this.getDeltaMovement()));
    }

    private void spawnClientAirwaveRing(Vec3 center, Vec3 direction) {
        Vec3 forward = HeavenPiercingConeEntity.safeDirection(direction);
        Vec3 right = HeavenPiercingConeEntity.perpendicularRight(forward);
        Vec3 up = HeavenPiercingConeEntity.perpendicularUp(forward, right);
        double radius = this.airwaveRadius();
        for (int i = 0; i < 18; ++i) {
            double angle = (double)i * Math.PI * 2.0 / 18.0;
            Vec3 outward = right.scale(Math.cos(angle)).add(up.scale(Math.sin(angle)));
            Vec3 point = center.add(outward.scale(radius));
            this.level().addParticle((ParticleOptions)ParticleTypes.CLOUD, point.x, point.y, point.z, outward.x * 0.035, outward.y * 0.035, outward.z * 0.035);
        }
    }

    private void spawnServerAirwaveRing(ServerLevel server, Vec3 center, Vec3 direction) {
        Vec3 forward = HeavenPiercingConeEntity.safeDirection(direction);
        Vec3 right = HeavenPiercingConeEntity.perpendicularRight(forward);
        Vec3 up = HeavenPiercingConeEntity.perpendicularUp(forward, right);
        double radius = this.airwaveRadius();
        for (int i = 0; i < 18; ++i) {
            double angle = (double)i * Math.PI * 2.0 / 18.0;
            Vec3 outward = right.scale(Math.cos(angle)).add(up.scale(Math.sin(angle)));
            Vec3 point = center.add(outward.scale(radius));
            server.sendParticles((ParticleOptions)ParticleTypes.CLOUD, point.x, point.y, point.z, 1, 0.01, 0.01, 0.01, 0.02);
        }
    }

    private double airwaveRadius() {
        return (switch (this.visualStage()) {
            case 1 -> 0.55;
            case 2 -> 0.78;
            case 3 -> 1.08;
            default -> 1.35;
        }) + Math.min(0.65, (double)this.chargeTicks() * 0.004);
    }

    private static Vec3 perpendicularRight(Vec3 forward) {
        Vec3 right = HeavenPiercingConeEntity.safeDirection(forward).cross(WORLD_UP);
        if (right.lengthSqr() < 1.0E-6) {
            return new Vec3(1.0, 0.0, 0.0);
        }
        return right.normalize();
    }

    private static Vec3 perpendicularUp(Vec3 forward, Vec3 right) {
        Vec3 up = right.cross(HeavenPiercingConeEntity.safeDirection(forward));
        if (up.lengthSqr() < 1.0E-6) {
            return WORLD_UP;
        }
        return up.normalize();
    }

    public void readAdditionalSaveData(@NotNull CompoundTag tag) {
        this.lifetime = tag.getInt("lifetime");
        this.damage = tag.contains("damage") ? tag.getFloat("damage") : this.damage;
        this.configuredSpeed = tag.contains("speed") ? tag.getDouble("speed") : this.configuredSpeed;
        this.tapShot = tag.getBoolean("tapShot");
        if (tag.contains("owner")) {
            this.ownerUuid = tag.getUUID("owner");
        }
        this.entityData.set(DATA_STAGE, Mth.clamp((int)tag.getInt("stage"), (int)1, (int)4));
        this.entityData.set(DATA_CHARGE_TICKS, Math.max(0, tag.getInt("chargeTicks")));
        this.drilling = tag.getBoolean("drilling");
        if (this.drilling) {
            this.drillOrigin = new Vec3(tag.getDouble("drillOriginX"), tag.getDouble("drillOriginY"), tag.getDouble("drillOriginZ"));
            this.drillDirection = HeavenPiercingConeEntity.safeDirection(new Vec3(tag.getDouble("drillDirectionX"), tag.getDouble("drillDirectionY"), tag.getDouble("drillDirectionZ")));
            this.drillProgress = tag.getDouble("drillProgress");
            this.drillDepth = Math.max(0.0, tag.getDouble("drillDepth"));
            this.drillBreakLimit = Math.max(1, tag.getInt("drillBreakLimit"));
            this.drillBroken = Math.max(0, tag.getInt("drillBroken"));
            this.drillVisited.clear();
            this.drillPendingBlocks.clear();
        }
    }

    public void addAdditionalSaveData(@NotNull CompoundTag tag) {
        tag.putInt("lifetime", this.lifetime);
        tag.putFloat("damage", this.damage);
        tag.putDouble("speed", this.configuredSpeed);
        tag.putBoolean("tapShot", this.tapShot);
        tag.putInt("stage", this.stage());
        tag.putInt("chargeTicks", this.chargeTicks());
        if (this.ownerUuid != null) {
            tag.putUUID("owner", this.ownerUuid);
        }
        tag.putBoolean("drilling", this.drilling);
        if (this.drilling) {
            tag.putDouble("drillOriginX", this.drillOrigin.x);
            tag.putDouble("drillOriginY", this.drillOrigin.y);
            tag.putDouble("drillOriginZ", this.drillOrigin.z);
            tag.putDouble("drillDirectionX", this.drillDirection.x);
            tag.putDouble("drillDirectionY", this.drillDirection.y);
            tag.putDouble("drillDirectionZ", this.drillDirection.z);
            tag.putDouble("drillProgress", this.drillProgress);
            tag.putDouble("drillDepth", this.drillDepth);
            tag.putInt("drillBreakLimit", this.drillBreakLimit);
            tag.putInt("drillBroken", this.drillBroken);
        }
    }

    public void writeSpawnData(FriendlyByteBuf buffer) {
        buffer.writeFloat(this.damage);
        buffer.writeDouble(this.configuredSpeed);
        buffer.writeBoolean(this.tapShot);
        buffer.writeInt(this.stage());
        buffer.writeInt(this.chargeTicks());
        buffer.writeBoolean(this.ownerUuid != null);
        if (this.ownerUuid != null) {
            buffer.writeUUID(this.ownerUuid);
        }
    }

    public void readSpawnData(FriendlyByteBuf additionalData) {
        this.damage = additionalData.readFloat();
        this.configuredSpeed = additionalData.readDouble();
        this.tapShot = additionalData.readBoolean();
        this.entityData.set(DATA_STAGE, Mth.clamp((int)additionalData.readInt(), (int)1, (int)4));
        this.entityData.set(DATA_CHARGE_TICKS, Math.max(0, additionalData.readInt()));
        this.ownerUuid = additionalData.readBoolean() ? additionalData.readUUID() : null;
    }

    public boolean fireImmune() {
        return true;
    }

    public boolean isPickable() {
        return true;
    }

    @NotNull
    public Packet<ClientGamePacketListener> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket((Entity)this);
    }
}

