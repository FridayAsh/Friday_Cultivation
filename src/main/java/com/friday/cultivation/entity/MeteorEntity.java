/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.BlockPos
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
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.entity.projectile.ProjectileUtil
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.level.Level$ExplosionInteraction
 *  net.minecraft.world.level.block.Blocks
 *  net.minecraft.world.phys.AABB
 *  net.minecraft.world.phys.EntityHitResult
 *  net.minecraft.world.phys.HitResult
 *  net.minecraft.world.phys.HitResult$Type
 *  net.minecraft.world.phys.Vec3
 *  net.minecraftforge.network.NetworkHooks
 *  org.jetbrains.annotations.NotNull
 *  org.jetbrains.annotations.Nullable
 */
package com.friday.cultivation.entity;

import com.friday.cultivation.entity.ShockwaveEntity;
import com.friday.cultivation.event.MeteorCraterCarver;
import com.friday.cultivation.event.SectCombatHandler;
import com.friday.cultivation.event.SectProtectionDomeHandler;
import com.friday.cultivation.event.SoulStateHandler;
import com.friday.cultivation.event.TimeStasisHandler;
import com.friday.cultivation.registry.ModEntities;
import com.friday.cultivation.util.SpellDamageSourceHelper;
import com.friday.cultivation.util.SpellTerrainDestructionHelper;
import java.util.UUID;
import net.minecraft.core.BlockPos;
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
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkHooks;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class MeteorEntity
extends Entity {
    private static final EntityDataAccessor<Integer> DATA_MODE = SynchedEntityData.defineId(MeteorEntity.class, (EntityDataSerializer)EntityDataSerializers.INT);
    private static final EntityDataAccessor<Float> DATA_DIAMETER = SynchedEntityData.defineId(MeteorEntity.class, (EntityDataSerializer)EntityDataSerializers.FLOAT);
    public static final int MODE_SMALL = 0;
    public static final int MODE_RAIN = 1;
    public static final int MODE_MEGA = 2;
    private static final int MAX_LIFETIME = 600;
    private static final int MEGA_SLOW_DESCENT_TICKS = 60;
    private int lifetime = 0;
    private UUID ownerUuid;
    private float damageMultiplier = 1.0f;

    public MeteorEntity(EntityType<? extends MeteorEntity> type, Level level) {
        super(type, level);
        this.noPhysics = true;
    }

    public MeteorEntity(Level level, LivingEntity owner, Vec3 spawnPos, Vec3 targetPos, int mode, float diameter) {
        this((EntityType<? extends MeteorEntity>)((EntityType)ModEntities.METEOR.get()), level);
        this.ownerUuid = owner.getUUID();
        this.setPos(spawnPos.x, spawnPos.y, spawnPos.z);
        this.entityData.set(DATA_MODE, mode);
        this.entityData.set(DATA_DIAMETER, Float.valueOf(diameter));
        Vec3 dir = targetPos.multiply(spawnPos).normalize();
        if (mode == 2) {
            this.setDeltaMovement(dir.scale(0.05));
        } else {
            this.setDeltaMovement(dir.scale(3.5));
        }
    }

    protected void defineSynchedData() {
        this.entityData.define(DATA_MODE, 0);
        this.entityData.define(DATA_DIAMETER, Float.valueOf(2.0f));
    }

    public int mode() {
        return (Integer)this.entityData.get(DATA_MODE);
    }

    public float diameter() {
        return ((Float)this.entityData.get(DATA_DIAMETER)).floatValue();
    }

    public boolean isMega() {
        return this.mode() == 2;
    }

    public void setDamageMultiplier(double multiplier) {
        if (!Double.isFinite(multiplier)) {
            multiplier = 1.0;
        }
        this.damageMultiplier = (float)Math.max(0.0, multiplier);
    }

    public float megaSlowProgress() {
        if (this.mode() != 2) {
            return 1.0f;
        }
        return Math.min(1.0f, (float)this.lifetime / 60.0f);
    }

    public int lifetime() {
        return this.lifetime;
    }

    public void tick() {
        if (TimeStasisHandler.pauseEntityTickInStoppedTime(this)) {
            return;
        }
        super.tick();
        if (this.level().isClientSide) {
            this.spawnTrailParticles();
            return;
        }
        ++this.lifetime;
        if (this.lifetime > 600) {
            this.discard();
            return;
        }
        double newVy = 0.0;
        Vec3 vel = this.getDeltaMovement();
        if (this.mode() == 2) {
            if (this.lifetime < 60) {
                vel = new Vec3(vel.x * 0.99, -0.05, vel.z * 0.99);
            } else {
                newVy = Math.max(-4.0, vel.y * 1.05 - 0.3);
                vel = new Vec3(vel.x * 0.99, newVy, vel.z * 0.99);
            }
        } else {
            newVy = Math.max(-3.0, vel.y - 0.06);
            vel = new Vec3(vel.x, newVy, vel.z);
        }
        this.setDeltaMovement(vel);
        Vec3 oldPos = this.position();
        Vec3 newPos = oldPos.add(vel);
        HitResult hit = ProjectileUtil.getHitResultOnMoveVector((Entity)this, this::canHitEntity);
        if (hit.getType() != HitResult.Type.MISS) {
            this.onHit(hit);
            return;
        }
        this.setPos(this.getX() + vel.x, this.getY() + vel.y, this.getZ() + vel.z);
        if (this.getY() < (double)(this.level().getMinBuildHeight() - 10)) {
            this.discard();
        }
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
        float base = this.mode() == 2 ? 200.0f : 30.0f;
        return base * this.damageMultiplier;
    }

    public float getBarrierDamageEquivalent() {
        return this.barrierDamageEquivalent();
    }

    private void doImpact(ServerLevel server, Vec3 pos, @Nullable LivingEntity directTarget, boolean touchBarrierArea) {
        LivingEntity owner = this.getOwner(server);
        if (directTarget != null && directTarget.isAlive() && SoulStateHandler.canOrdinaryAffect((Entity)owner, (Entity)directTarget) && SectCombatHandler.canApplyOffensiveEffect(owner, directTarget) && !SectProtectionDomeHandler.isEntityProtectedByOwnDome((Entity)directTarget)) {
            float directDmg = this.isMega() ? 2000.0f : (this.mode() == 1 ? 12.0f : 25.0f);
            directTarget.hurt(SpellDamageSourceHelper.indirectSpell(this, owner), directDmg *= this.damageMultiplier);
        }
        float basePower = switch (this.mode()) {
            case 1 -> 3.0f;
            case 2 -> 8.0f;
            default -> 5.0f;
        };
        float power = MeteorEntity.scaledExplosionPower(basePower, this.damageMultiplier, this.isMega() ? 16.0f : 10.0f);
        if (touchBarrierArea) {
            double barrierRadius = this.isMega() ? 35.0 : Math.max(3.0, (double)power + 2.0);
            SectProtectionDomeHandler.onSpellAreaTouchedBarrier(server, pos, barrierRadius, (Entity)(owner != null ? owner : this), this.barrierDamageEquivalent());
        }
        boolean canModifyTerrain = SpellTerrainDestructionHelper.canModifyBlocks(server, (Entity)owner);
        server.explode((Entity)(owner != null ? owner : this), pos.x, pos.y, pos.z, power, !this.isMega() && canModifyTerrain, this.isMega() ? Level.ExplosionInteraction.NONE : SpellTerrainDestructionHelper.explosionInteraction(server, (Entity)owner, Level.ExplosionInteraction.MOB));
        if (this.isMega()) {
            this.spawnMegaShockwave(server, pos);
            if (canModifyTerrain) {
                MeteorCraterCarver.schedule(server, pos, 20, 35, (Entity)owner);
            }
            ShockwaveEntity shockwave = new ShockwaveEntity(server, pos, this.ownerUuid);
            shockwave.setDamageMultiplier(this.damageMultiplier);
            server.addFreshEntity((Entity)shockwave);
        }
        this.igniteNearby(server, pos, this.isMega() ? 10 : 3, owner);
        this.discard();
    }

    private static float scaledExplosionPower(float basePower, float damageMultiplier, float maxPower) {
        double scaled = (double)basePower * Math.sqrt(Math.max(0.0f, damageMultiplier));
        return (float)Math.min((double)maxPower, Math.max(0.0, scaled));
    }

    private void spawnMegaShockwave(ServerLevel server, Vec3 center) {
        double R = 100.0;
        AABB box = new AABB(center.subtract(R, R, R), center.add(R, R, R));
        LivingEntity owner = this.getOwner(server);
        for (Entity e : server.getEntities((Entity)this, box, ent -> ent != this && ent.isAlive())) {
            Vec3 dir;
            double dist;
            LivingEntity living;
            if (SectProtectionDomeHandler.isEntityProtectedByOwnDome(e) || e instanceof LivingEntity && (!SoulStateHandler.canOrdinaryAffect((Entity)owner, (Entity)(living = (LivingEntity)e)) || !SectCombatHandler.canApplyOffensiveEffect(owner, living)) || (dist = (dir = e.position().multiply(center)).length()) > R) continue;
            double force = (1.0 - dist / R) * 3.0;
            Vec3 push = dir.normalize().scale(force);
            e.setDeltaMovement(e.getDeltaMovement().add(push.x, force * 1.5, push.z));
            e.hurtMarked = true;
            if (!(e instanceof LivingEntity)) continue;
            LivingEntity living2 = (LivingEntity)e;
            living2.hurt(SpellDamageSourceHelper.indirectSpell(this, owner), 200.0f * this.damageMultiplier);
            living2.setRemainingFireTicks(600);
        }
        server.playSound(null, center.x, center.y, center.z, SoundEvents.LIGHTNING_BOLT_THUNDER, SoundSource.HOSTILE, 120.0f, 0.3f);
        server.playSound(null, center.x, center.y, center.z, SoundEvents.GENERIC_EXPLODE, SoundSource.HOSTILE, 120.0f, 0.5f);
    }

    private void igniteNearby(ServerLevel server, Vec3 center, int radius, @Nullable LivingEntity owner) {
        if (!SpellTerrainDestructionHelper.canModifyBlocks(server, (Entity)owner)) {
            return;
        }
        int count = radius * 8;
        block0: for (int i = 0; i < count; ++i) {
            double angle = this.random.nextDouble() * Math.PI * 2.0;
            double r = this.random.nextDouble() * (double)radius;
            int x = (int)(center.x + Math.cos(angle) * r);
            int z = (int)(center.z + Math.sin(angle) * r);
            int y = (int)center.y;
            for (int dy = 5; dy >= -5; --dy) {
                BlockPos check = new BlockPos(x, y + dy, z);
                BlockPos firePos = check.above();
                if (server.getBlockState(check).isAir() || !server.getBlockState(firePos).isAir()) continue;
                if (SectProtectionDomeHandler.isProtectedByAnySectProtectionDome((Level)server, check) || SectProtectionDomeHandler.isProtectedByAnySectProtectionDome((Level)server, firePos)) continue block0;
                SpellTerrainDestructionHelper.setBlockAndUpdate(server, firePos, Blocks.FIRE.defaultBlockState(), (Entity)owner);
                continue block0;
            }
        }
    }

    private Player getOwnerPlayer(ServerLevel server) {
        Player p;
        LivingEntity le = this.getOwner(server);
        return le instanceof Player ? (p = (Player)le) : null;
    }

    private LivingEntity getOwner(ServerLevel server) {
        LivingEntity le;
        if (this.ownerUuid == null) {
            return null;
        }
        Entity e = server.getEntity(this.ownerUuid);
        return e instanceof LivingEntity ? (le = (LivingEntity)e) : null;
    }

    public LivingEntity getOwnerEntity(ServerLevel server) {
        return this.getOwner(server);
    }

    private void spawnTrailParticles() {
        Vec3 vel = this.getDeltaMovement();
        double sp = vel.length();
        if (sp < 0.01) {
            return;
        }
        int particleCount = this.isMega() ? 8 : 3;
        for (int i = 0; i < particleCount; ++i) {
            double offX = (this.random.nextDouble() - 0.5) * ((double)this.diameter() * 0.3);
            double offY = (this.random.nextDouble() - 0.5) * ((double)this.diameter() * 0.3);
            double offZ = (this.random.nextDouble() - 0.5) * ((double)this.diameter() * 0.3);
            this.level().addParticle((ParticleOptions)ParticleTypes.FLAME, this.getX() + offX, this.getY() + offY + (double)this.diameter() * 0.3, this.getZ() + offZ, -vel.x * 0.1, -vel.y * 0.1, -vel.z * 0.1);
            if (i < particleCount / 2) {
                this.level().addParticle((ParticleOptions)ParticleTypes.LARGE_SMOKE, this.getX() + offX, this.getY() + offY + (double)this.diameter() * 0.5, this.getZ() + offZ, -vel.x * 0.05, 0.0, -vel.z * 0.05);
            }
            if (!this.isMega() || i % 2 != 0) continue;
            this.level().addParticle((ParticleOptions)ParticleTypes.LAVA, this.getX() + offX * 2.0, this.getY() + offY, this.getZ() + offZ * 2.0, 0.0, 0.0, 0.0);
        }
    }

    protected void readAdditionalSaveData(@NotNull CompoundTag tag) {
        this.lifetime = tag.getInt("lifetime");
        if (tag.contains("owner")) {
            this.ownerUuid = tag.getUUID("owner");
        }
        this.entityData.set(DATA_MODE, tag.getInt("mode"));
        this.entityData.set(DATA_DIAMETER, Float.valueOf(tag.getFloat("diameter")));
        this.damageMultiplier = tag.contains("damageMultiplier") ? Math.max(0.0f, tag.getFloat("damageMultiplier")) : 1.0f;
    }

    protected void addAdditionalSaveData(@NotNull CompoundTag tag) {
        tag.putInt("lifetime", this.lifetime);
        if (this.ownerUuid != null) {
            tag.putUUID("owner", this.ownerUuid);
        }
        tag.putInt("mode", this.mode());
        tag.putFloat("diameter", this.diameter());
        tag.putFloat("damageMultiplier", this.damageMultiplier);
    }

    @NotNull
    public Packet<ClientGamePacketListener> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket((Entity)this);
    }

    protected AABB makeBoundingBox() {
        float r = Math.max(0.5f, this.diameter() * 0.5f);
        return new AABB(this.getX() - (double)r, this.getY() - (double)r, this.getZ() - (double)r, this.getX() + (double)r, this.getY() + (double)r, this.getZ() + (double)r);
    }
}

