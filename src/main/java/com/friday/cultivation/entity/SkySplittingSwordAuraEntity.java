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
 *  net.minecraft.sounds.SoundEvents
 *  net.minecraft.sounds.SoundSource
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

import com.friday.cultivation.entity.npc.WanderingCultivatorEntity;
import com.friday.cultivation.event.BloodthirstCurseHandler;
import com.friday.cultivation.event.SectCombatHandler;
import com.friday.cultivation.event.SectProtectionDomeHandler;
import com.friday.cultivation.event.SoulStateHandler;
import com.friday.cultivation.event.TimeStasisHandler;
import com.friday.cultivation.registry.ModEntities;
import com.friday.cultivation.util.SpellDamageSourceHelper;
import com.friday.cultivation.util.SpellTerrainDestructionHelper;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashSet;
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
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
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

public class SkySplittingSwordAuraEntity
extends Entity {
    private static final EntityDataAccessor<Boolean> DATA_IS_MEGA = SynchedEntityData.defineId(SkySplittingSwordAuraEntity.class, (EntityDataSerializer)EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Float> DATA_SCALE = SynchedEntityData.defineId(SkySplittingSwordAuraEntity.class, (EntityDataSerializer)EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> DATA_ROLL_RAD = SynchedEntityData.defineId(SkySplittingSwordAuraEntity.class, (EntityDataSerializer)EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Boolean> DATA_HAS_RENDER_FORWARD = SynchedEntityData.defineId(SkySplittingSwordAuraEntity.class, (EntityDataSerializer)EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Float> DATA_RENDER_FORWARD_X = SynchedEntityData.defineId(SkySplittingSwordAuraEntity.class, (EntityDataSerializer)EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> DATA_RENDER_FORWARD_Y = SynchedEntityData.defineId(SkySplittingSwordAuraEntity.class, (EntityDataSerializer)EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> DATA_RENDER_FORWARD_Z = SynchedEntityData.defineId(SkySplittingSwordAuraEntity.class, (EntityDataSerializer)EntityDataSerializers.FLOAT);
    private static final float TAP_SCALE = 3.0f;
    private static final double TAP_SPEED = 1.5;
    private static final int TAP_LIFETIME = 60;
    private static final float TAP_DAMAGE = 1000.0f;
    private static final float MEGA_SCALE = 30.0f;
    private static final double MEGA_SPEED = 6.0;
    private static final int MEGA_LIFETIME = 25;
    private static final int MEGA_MAX_BLOCKS_PER_TICK = 12000;
    private static final int MEGA_MAX_BLOCK_CHECKS_PER_TICK = 220000;
    private static final int NPC_MEGA_MAX_BLOCKS_PER_TICK = 2500;
    private static final int NPC_MEGA_MAX_BLOCK_CHECKS_PER_TICK = 80000;
    private static final float MEGA_DAMAGE = 2000.0f;
    private static final float OVERCHARGED_TRUE_DAMAGE_RATIO = 0.1f;
    private static final int FADE_OUT_TICKS = 8;
    private static final double CRES_OUTER_R = 1.0;
    private static final double CRES_INNER_R = 1.0;
    private static final double CRES_OFFSET = 0.6;
    private static final double CRES_MAX_THICK = 0.12;
    private static final double CRES_Y_PAD = 0.7;
    private static final double CRES_Z_MIN = -0.4;
    private static final double CRES_Z_MAX = 1.0;
    private int lifetime = 0;
    private int maxLifetime;
    private float damage;
    private float trueDamageRatio;
    private UUID ownerUuid;
    private Vec3 spawnPos;
    private final Set<Integer> processedEntities = new HashSet<Integer>();

    public SkySplittingSwordAuraEntity(EntityType<? extends SkySplittingSwordAuraEntity> type, Level level) {
        super(type, level);
        this.noPhysics = true;
    }

    public SkySplittingSwordAuraEntity(Level level, LivingEntity owner, Vec3 spawn, Vec3 dirNormalized, boolean isMega) {
        this((EntityType<? extends SkySplittingSwordAuraEntity>)((EntityType)ModEntities.SKY_SPLITTING_SWORD_AURA.get()), level);
        this.ownerUuid = owner.getUUID();
        this.spawnPos = spawn;
        this.setPos(spawn.x, spawn.y, spawn.z);
        this.entityData.set(DATA_IS_MEGA, isMega);
        Vec3 forward = SkySplittingSwordAuraEntity.safeRenderForward(dirNormalized);
        this.setRenderForward(forward);
        if (isMega) {
            this.entityData.set(DATA_SCALE, Float.valueOf(30.0f));
            this.maxLifetime = 25;
            this.damage = 2000.0f;
            this.setDeltaMovement(forward.scale(6.0));
        } else {
            this.entityData.set(DATA_SCALE, Float.valueOf(3.0f));
            this.maxLifetime = 60;
            this.damage = 1000.0f;
            this.setDeltaMovement(forward.scale(1.5));
        }
        this.entityData.set(DATA_ROLL_RAD, Float.valueOf((float)(this.random.nextDouble() * Math.PI * 2.0)));
    }

    protected void defineSynchedData() {
        this.entityData.define(DATA_IS_MEGA, false);
        this.entityData.define(DATA_SCALE, Float.valueOf(3.0f));
        this.entityData.define(DATA_ROLL_RAD, Float.valueOf(0.0f));
        this.entityData.define(DATA_HAS_RENDER_FORWARD, false);
        this.entityData.define(DATA_RENDER_FORWARD_X, Float.valueOf(0.0f));
        this.entityData.define(DATA_RENDER_FORWARD_Y, Float.valueOf(0.0f));
        this.entityData.define(DATA_RENDER_FORWARD_Z, Float.valueOf(1.0f));
    }

    public boolean isMega() {
        return (Boolean)this.entityData.get(DATA_IS_MEGA);
    }

    public float scale() {
        return ((Float)this.entityData.get(DATA_SCALE)).floatValue();
    }

    public float rollRad() {
        return ((Float)this.entityData.get(DATA_ROLL_RAD)).floatValue();
    }

    public Vec3 renderForward() {
        if (!((Boolean)this.entityData.get(DATA_HAS_RENDER_FORWARD)).booleanValue()) {
            return SkySplittingSwordAuraEntity.safeRenderForward(this.getDeltaMovement());
        }
        return SkySplittingSwordAuraEntity.safeRenderForward(new Vec3((double)((Float)this.entityData.get(DATA_RENDER_FORWARD_X)).floatValue(), (double)((Float)this.entityData.get(DATA_RENDER_FORWARD_Y)).floatValue(), (double)((Float)this.entityData.get(DATA_RENDER_FORWARD_Z)).floatValue()));
    }

    private void setRenderForward(Vec3 forward) {
        Vec3 normalized = SkySplittingSwordAuraEntity.safeRenderForward(forward);
        this.entityData.set(DATA_HAS_RENDER_FORWARD, true);
        this.entityData.set(DATA_RENDER_FORWARD_X, Float.valueOf((float)normalized.x));
        this.entityData.set(DATA_RENDER_FORWARD_Y, Float.valueOf((float)normalized.y));
        this.entityData.set(DATA_RENDER_FORWARD_Z, Float.valueOf((float)normalized.z));
    }

    private static Vec3 safeRenderForward(Vec3 forward) {
        if (forward == null || !Double.isFinite(forward.x) || !Double.isFinite(forward.y) || !Double.isFinite(forward.z) || forward.lengthSqr() < 1.0E-8) {
            return new Vec3(0.0, 0.0, 1.0);
        }
        return forward.normalize();
    }

    public void setDamage(float damage) {
        this.damage = damage;
    }

    public float getDamage() {
        return this.damage;
    }

    public void setTrueDamageRatio(float trueDamageRatio) {
        this.trueDamageRatio = Math.max(0.0f, trueDamageRatio);
    }

    public float getBarrierDamageEquivalent() {
        return Math.max(1.0f, this.damage * (1.0f + this.trueDamageRatio));
    }

    public LivingEntity getOwnerEntity(ServerLevel server) {
        LivingEntity le;
        Entity entity;
        return this.ownerUuid != null && (entity = server.getEntity(this.ownerUuid)) instanceof LivingEntity ? (le = (LivingEntity)entity) : null;
    }

    public float getRenderAlpha(float partialTick) {
        float life = (float)this.tickCount + partialTick;
        int maxLife = this.isMega() ? 25 : 60;
        int fadeStart = maxLife - 8;
        if (life < (float)fadeStart) {
            return 1.0f;
        }
        if (life >= (float)maxLife) {
            return 0.0f;
        }
        return Math.max(0.0f, 1.0f - (life - (float)fadeStart) / 8.0f);
    }

    public void tick() {
        if (TimeStasisHandler.pauseEntityTickInStoppedTime(this)) {
            return;
        }
        super.tick();
        if (!this.level().isClientSide) {
            ++this.lifetime;
            if (this.lifetime > this.maxLifetime) {
                this.onExpire();
                return;
            }
        }
        Vec3 vel = this.getDeltaMovement();
        Vec3 oldPos = this.position();
        Vec3 newPos = oldPos.add(vel);
        Level level = this.level();
        if (level instanceof ServerLevel) {
            ServerLevel server = (ServerLevel)level;
            double speedSqr = vel.lengthSqr();
            if (speedSqr < 1.0E-7) {
                this.setPos(newPos.x, newPos.y, newPos.z);
                return;
            }
            Vec3 forward = vel.normalize();
            Vec3[] axes = SkySplittingSwordAuraEntity.computeAxes(forward, this.rollRad());
            Vec3 right = axes[0];
            Vec3 up = axes[1];
            float scale = this.scale();
            this.setPos(newPos.x, newPos.y, newPos.z);
            this.applyCrescentSweep(server, oldPos, newPos, forward, right, up, scale, this.lifetime == 1);
        } else {
            this.setPos(newPos.x, newPos.y, newPos.z);
        }
    }

    private static Vec3[] computeAxes(Vec3 forward, float roll) {
        double horizontal = Math.sqrt(forward.x * forward.x + forward.z * forward.z);
        Vec3 right0 = horizontal < 1.0E-6 ? new Vec3(1.0, 0.0, 0.0) : new Vec3(forward.z / horizontal, 0.0, -forward.x / horizontal);
        Vec3 up0 = forward.cross(right0).normalize();
        double cos = Math.cos(roll);
        double sin = Math.sin(roll);
        Vec3 right = right0.scale(cos).add(up0.scale(sin)).normalize();
        Vec3 up = forward.cross(right).normalize();
        return new Vec3[]{right, up};
    }

    private static boolean insideCrescent(double lxNorm, double lyAbs, double lzNorm, double yHalfMax) {
        return SkySplittingSwordAuraEntity.insideCrescent(lxNorm, lyAbs, lzNorm, yHalfMax, false);
    }

    private static boolean insideCrescent(double lxNorm, double lyAbs, double lzNorm, double yHalfMax, boolean skipInnerCutout) {
        double dzInner;
        if (lxNorm * lxNorm + lzNorm * lzNorm > 1.0) {
            return false;
        }
        if (!skipInnerCutout && lxNorm * lxNorm + (dzInner = lzNorm + 0.6) * dzInner < 1.0) {
            return false;
        }
        return !(lyAbs > yHalfMax);
    }

    private void applyCrescentSweep(ServerLevel server, Vec3 oldPos, Vec3 newPos, Vec3 forward, Vec3 right, Vec3 up, float scale, boolean includeStart) {
        int firstStep;
        BlockDestructionContext blockContext = this.createBlockDestructionContext(server);
        double distance = oldPos.distanceTo(newPos);
        double forwardSpan = 1.4 * (double)scale;
        double sampleSpacing = Math.max(1.0, forwardSpan * 0.5);
        int steps = Math.max(1, (int)Math.ceil(distance / sampleSpacing));
        for (int step = firstStep = includeStart ? 0 : 1; step <= steps; ++step) {
            double t = (double)step / (double)steps;
            Vec3 samplePos = oldPos.lerp(newPos, t);
            this.damageEntitiesInCrescent(server, samplePos, forward, right, up, scale);
            if (!blockContext.canContinue()) continue;
            this.destroyBlocksInCrescent(server, samplePos, forward, right, up, scale, blockContext);
        }
        if (!blockContext.barrierHits.isEmpty()) {
            SectProtectionDomeHandler.onBarrierAreaTouched(server, blockContext.barrierHits, this.getBarrierDamageEquivalent(), (Entity)this.getOwnerEntity(server));
        }
    }

    private BlockDestructionContext createBlockDestructionContext(ServerLevel server) {
        int maxChecks;
        int maxPerTick;
        boolean npcOwnedMega;
        LivingEntity owner = this.getOwnerEntity(server);
        boolean mega = this.isMega();
        boolean bl = npcOwnedMega = mega && owner instanceof WanderingCultivatorEntity;
        maxPerTick = mega ? (npcOwnedMega ? 2500 : 12000) : Integer.MAX_VALUE;
        maxChecks = mega ? (npcOwnedMega ? 80000 : 220000) : Integer.MAX_VALUE;
        Vec3 protectionRef = owner != null ? owner.position() : (this.spawnPos != null ? this.spawnPos : this.position());
        return new BlockDestructionContext(protectionRef, maxPerTick, maxChecks);
    }

    private void destroyBlocksInCrescent(ServerLevel server, Vec3 entityPos, Vec3 forward, Vec3 right, Vec3 up, float scale, BlockDestructionContext context) {
        LivingEntity owner = this.getOwnerEntity(server);
        boolean canModifyBlocks = SpellTerrainDestructionHelper.canModifyBlocks(server, (Entity)owner);
        double xExt = 1.0 * (double)scale;
        double yExt = 0.06 * (double)scale + 0.7;
        boolean isMega = this.isMega();
        double zMin = (isMega ? -1.0 : -0.4) * (double)scale;
        double zMax = 1.0 * (double)scale;
        double minWX = Double.POSITIVE_INFINITY;
        double maxWX = Double.NEGATIVE_INFINITY;
        double minWY = Double.POSITIVE_INFINITY;
        double maxWY = Double.NEGATIVE_INFINITY;
        double minWZ = Double.POSITIVE_INFINITY;
        double maxWZ = Double.NEGATIVE_INFINITY;
        double[] xs = new double[]{-xExt, xExt};
        double[] ys = new double[]{-yExt, yExt};
        double[] zs = new double[]{zMin, zMax};
        for (double lx : xs) {
            for (double ly : ys) {
                for (double lz : zs) {
                    double wx = entityPos.x + forward.x * lz + right.x * lx + up.x * ly;
                    double wy = entityPos.y + forward.y * lz + right.y * lx + up.y * ly;
                    double wz = entityPos.z + forward.z * lz + right.z * lx + up.z * ly;
                    if (wx < minWX) {
                        minWX = wx;
                    }
                    if (wx > maxWX) {
                        maxWX = wx;
                    }
                    if (wy < minWY) {
                        minWY = wy;
                    }
                    if (wy > maxWY) {
                        maxWY = wy;
                    }
                    if (wz < minWZ) {
                        minWZ = wz;
                    }
                    if (!(wz > maxWZ)) continue;
                    maxWZ = wz;
                }
            }
        }
        int bxMin = (int)Math.floor(minWX);
        int byMin = (int)Math.floor(minWY);
        int bzMin = (int)Math.floor(minWZ);
        int bxMax = (int)Math.floor(maxWX);
        int byMax = (int)Math.floor(maxWY);
        int bzMax = (int)Math.floor(maxWZ);
        BlockPos.MutableBlockPos mbp = new BlockPos.MutableBlockPos();
        BlockState air = Blocks.AIR.defaultBlockState();
        ArrayList<BlockPos> candidates = new ArrayList<BlockPos>();
        block3: for (int bx = bxMin; bx <= bxMax; ++bx) {
            for (int by = byMin; by <= byMax; ++by) {
                for (int bz = bzMin; bz <= bzMax; ++bz) {
                    if (!context.canCheck()) break block3;
                    ++context.checked;
                    mbp.set(bx, by, bz);
                    BlockState state = server.getBlockState((BlockPos)mbp);
                    if (state.isAir() || state.is(Blocks.BEDROCK) || state.is(Blocks.OBSIDIAN) || state.is(Blocks.CRYING_OBSIDIAN) || state.is(Blocks.REINFORCED_DEEPSLATE) || state.is(Blocks.END_PORTAL_FRAME)) continue;
                    double cx = (double)bx + 0.5;
                    double cy = (double)by + 0.5;
                    double cz = (double)bz + 0.5;
                    double rx = cx - entityPos.x;
                    double ry = cy - entityPos.y;
                    double rz = cz - entityPos.z;
                    double lx = rx * right.x + ry * right.y + rz * right.z;
                    double ly = rx * up.x + ry * up.y + rz * up.z;
                    double lz = rx * forward.x + ry * forward.y + rz * forward.z;
                    if (!SkySplittingSwordAuraEntity.insideCrescent(lx / (double)scale, Math.abs(ly), lz / (double)scale, yExt, isMega)) continue;
                    if (SectProtectionDomeHandler.domeOwningProtectedShell((Level)server, (BlockPos)mbp) != null) {
                        context.barrierHits.add(mbp.east());
                        continue;
                    }
                    if (SectProtectionDomeHandler.isProtectedFromExternal((Level)server, (BlockPos)mbp, context.protectionRef.x, context.protectionRef.y, context.protectionRef.z) || !canModifyBlocks) continue;
                    candidates.add(mbp.east());
                }
            }
        }
        if (candidates.isEmpty() || !context.canDestroy()) {
            return;
        }
        candidates.sort(Comparator.comparingDouble(pos -> SkySplittingSwordAuraEntity.blockDestructionOrder(pos, entityPos, forward, right, up)));
        for (BlockPos pos2 : candidates) {
            if (!context.canDestroy()) break;
            BlockState state = server.getBlockState(pos2);
            if (state.isAir() || !SpellTerrainDestructionHelper.setBlock(server, pos2, air, 2, (Entity)owner)) continue;
            ++context.destroyed;
        }
    }

    private static double blockDestructionOrder(BlockPos pos, Vec3 entityPos, Vec3 forward, Vec3 right, Vec3 up) {
        double cx = (double)pos.getX() + 0.5;
        double cy = (double)pos.getY() + 0.5;
        double cz = (double)pos.getZ() + 0.5;
        double rx = cx - entityPos.x;
        double ry = cy - entityPos.y;
        double rz = cz - entityPos.z;
        double lx = rx * right.x + ry * right.y + rz * right.z;
        double ly = rx * up.x + ry * up.y + rz * up.z;
        double lz = rx * forward.x + ry * forward.y + rz * forward.z;
        long hash = pos.asLong() * -7046029254386353131L;
        double tieBreaker = (double)(hash & 0xFFFFL) * 1.0E-9;
        return lx * lx + lz * lz + ly * ly * 0.15 + tieBreaker;
    }

    private boolean damageEntitiesInCrescent(ServerLevel server, Vec3 entityPos, Vec3 forward, Vec3 right, Vec3 up, float scale) {
        double r = 1.0 * (double)scale * 1.5;
        AABB box = new AABB(entityPos.subtract(r, r, r), entityPos.add(r, r, r));
        LivingEntity owner = this.getOwnerEntity(server);
        boolean anyHit = false;
        double yExt = 0.06 * (double)scale + 0.7;
        double outerSqInflated = 1.2100000000000002;
        double innerSqShrunk = 0.81;
        boolean megaMode = this.isMega();
        for (Entity e : server.getEntities((Entity)this, box, ent -> ent != this && ent.isAlive())) {
            double dzInner;
            double nz;
            double nx;
            if (this.processedEntities.contains(e.getId()) || owner != null && e.getUUID().equals(owner.getUUID())) continue;
            Vec3 ec = e.getBoundingBox().getCenter();
            double rx = ec.x - entityPos.x;
            double ry = ec.y - entityPos.y;
            double rz = ec.z - entityPos.z;
            double lx = rx * right.x + ry * right.y + rz * right.z;
            double ly = rx * up.x + ry * up.y + rz * up.z;
            double lz = rx * forward.x + ry * forward.y + rz * forward.z;
            double yTolerance = yExt + (double)e.getBbHeight() * 0.5 + 0.3;
            if (Math.abs(ly) > yTolerance || (nx = lx / (double)scale) * nx + (nz = lz / (double)scale) * nz > outerSqInflated || !megaMode && nx * nx + (dzInner = nz + 0.6) * dzInner < innerSqShrunk) continue;
            if (e instanceof LivingEntity) {
                LivingEntity living = (LivingEntity)e;
                if (!SoulStateHandler.canOrdinaryAffect((Entity)owner, (Entity)living) || !SectCombatHandler.canApplyOffensiveEffect(owner, living)) continue;
                living.hurt(SpellDamageSourceHelper.indirectSpell(this, owner), this.damage);
                this.applyOverchargedTrueDamage(living, owner);
                Vec3 push = forward.scale(megaMode ? 4.0 : 1.5);
                living.setDeltaMovement(living.getDeltaMovement().add(push.x, 0.5, push.z));
                living.hurtMarked = true;
            }
            this.processedEntities.add(e.getId());
            anyHit = true;
        }
        return anyHit;
    }

    private void applyOverchargedTrueDamage(LivingEntity target, LivingEntity owner) {
        ServerLevel server;
        Level level;
        if (this.trueDamageRatio <= 0.0f || target == null || !target.isAlive()) {
            return;
        }
        if (!SoulStateHandler.canOrdinaryAffect((Entity)owner, (Entity)target)) {
            return;
        }
        if (!SectCombatHandler.canApplyOffensiveEffect(owner, target)) {
            return;
        }
        if (owner != null && (level = target.level()) instanceof ServerLevel && SectProtectionDomeHandler.isProtectedFromExternal((Level)(server = (ServerLevel)level), target.blockPosition(), owner.getX(), owner.getY(), owner.getZ())) {
            return;
        }
        if (owner == null && SectProtectionDomeHandler.isEntityProtectedByOwnDome((Entity)target)) {
            return;
        }
        float amount = this.damage * this.trueDamageRatio;
        if (amount <= 0.0f) {
            return;
        }
        float before = target.getHealth();
        if (before > amount) {
            target.setHealth(Math.max(0.0f, before - amount));
        } else {
            target.invulnerableTime = 0;
            target.setHealth(Math.max(1.0f, before));
            BloodthirstCurseHandler.hurtWithoutEventReward(target, owner != null ? SpellDamageSourceHelper.directSpell(owner) : this.damageSources().magic(), Math.max(1000.0f, amount + before));
        }
        float actualLost = Math.max(0.0f, before - Math.max(0.0f, target.getHealth()));
        BloodthirstCurseHandler.rewardFromActualDamage(owner, target, actualLost);
    }

    private void onExpire() {
        Level level = this.level();
        if (level instanceof ServerLevel) {
            ServerLevel server = (ServerLevel)level;
            server.playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.PLAYER_ATTACK_SWEEP, SoundSource.PLAYERS, 1.0f, 0.6f);
        }
        this.discard();
    }

    protected void readAdditionalSaveData(@NotNull CompoundTag tag) {
        this.lifetime = tag.getInt("lifetime");
        if (tag.contains("owner")) {
            this.ownerUuid = tag.getUUID("owner");
        }
        this.entityData.set(DATA_IS_MEGA, tag.getBoolean("isMega"));
        this.entityData.set(DATA_SCALE, Float.valueOf(tag.getFloat("scale")));
        this.entityData.set(DATA_ROLL_RAD, Float.valueOf(tag.getFloat("roll")));
        if (tag.contains("spawnX")) {
            this.spawnPos = new Vec3(tag.getDouble("spawnX"), tag.getDouble("spawnY"), tag.getDouble("spawnZ"));
        }
        if (tag.contains("renderForwardX")) {
            this.setRenderForward(new Vec3(tag.getDouble("renderForwardX"), tag.getDouble("renderForwardY"), tag.getDouble("renderForwardZ")));
        } else {
            this.setRenderForward(this.getDeltaMovement());
        }
        this.trueDamageRatio = tag.getFloat("trueDamageRatio");
        boolean mega = this.isMega();
        int n = this.maxLifetime = mega ? 25 : 60;
        this.damage = tag.contains("damage") ? tag.getFloat("damage") : (mega ? 2000.0f : 1000.0f);
    }

    protected void addAdditionalSaveData(@NotNull CompoundTag tag) {
        tag.putInt("lifetime", this.lifetime);
        if (this.ownerUuid != null) {
            tag.putUUID("owner", this.ownerUuid);
        }
        tag.putBoolean("isMega", this.isMega());
        tag.putFloat("scale", this.scale());
        tag.putFloat("roll", this.rollRad());
        if (this.spawnPos != null) {
            tag.putDouble("spawnX", this.spawnPos.x);
            tag.putDouble("spawnY", this.spawnPos.y);
            tag.putDouble("spawnZ", this.spawnPos.z);
        }
        Vec3 forward = this.renderForward();
        tag.putDouble("renderForwardX", forward.x);
        tag.putDouble("renderForwardY", forward.y);
        tag.putDouble("renderForwardZ", forward.z);
        tag.putFloat("damage", this.damage);
        tag.putFloat("trueDamageRatio", this.trueDamageRatio);
    }

    @NotNull
    public Packet<ClientGamePacketListener> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket((Entity)this);
    }

    protected AABB makeBoundingBox() {
        float r = Math.max(2.0f, this.scale());
        return new AABB(this.getX() - (double)r, this.getY() - (double)r, this.getZ() - (double)r, this.getX() + (double)r, this.getY() + (double)r, this.getZ() + (double)r);
    }

    private static final class BlockDestructionContext {
        private final Vec3 protectionRef;
        private final int maxPerTick;
        private final int maxChecks;
        private final Set<BlockPos> barrierHits = new LinkedHashSet<BlockPos>();
        private int destroyed;
        private int checked;

        private BlockDestructionContext(Vec3 protectionRef, int maxPerTick, int maxChecks) {
            this.protectionRef = protectionRef;
            this.maxPerTick = maxPerTick;
            this.maxChecks = maxChecks;
        }

        private boolean canContinue() {
            return this.destroyed < this.maxPerTick && this.checked < this.maxChecks;
        }

        private boolean canDestroy() {
            return this.destroyed < this.maxPerTick;
        }

        private boolean canCheck() {
            return this.checked < this.maxChecks;
        }
    }
}

