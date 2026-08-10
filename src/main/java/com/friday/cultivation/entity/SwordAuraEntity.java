/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.BlockPos
 *  net.minecraft.core.particles.BlockParticleOption
 *  net.minecraft.core.particles.ParticleOptions
 *  net.minecraft.core.particles.ParticleTypes
 *  net.minecraft.core.particles.SimpleParticleType
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
 *  net.minecraft.world.effect.MobEffectInstance
 *  net.minecraft.world.effect.MobEffects
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.EntityType
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.entity.projectile.ProjectileUtil
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.level.block.Blocks
 *  net.minecraft.world.phys.AABB
 *  net.minecraft.world.phys.BlockHitResult
 *  net.minecraft.world.phys.EntityHitResult
 *  net.minecraft.world.phys.HitResult
 *  net.minecraft.world.phys.HitResult$Type
 *  net.minecraft.world.phys.Vec3
 *  net.minecraftforge.network.NetworkHooks
 *  org.jetbrains.annotations.NotNull
 */
package com.friday.cultivation.entity;

import com.friday.cultivation.block.formation.SectProtectionBarrierBlock;
import com.friday.cultivation.cultivation.QiElement;
import com.friday.cultivation.entity.IceShellEntity;
import com.friday.cultivation.event.SectCombatHandler;
import com.friday.cultivation.event.SectProtectionDomeHandler;
import com.friday.cultivation.event.SoulStateHandler;
import com.friday.cultivation.event.TimeStasisHandler;
import com.friday.cultivation.registry.ModEntities;
import com.friday.cultivation.util.SpellDamageSourceHelper;
import com.friday.cultivation.util.SpellTerrainDestructionHelper;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.particles.SimpleParticleType;
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
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkHooks;
import org.jetbrains.annotations.NotNull;

public class SwordAuraEntity
extends Entity {
    private static final EntityDataAccessor<Integer> DATA_ELEMENT = SynchedEntityData.defineId(SwordAuraEntity.class, (EntityDataSerializer)EntityDataSerializers.INT);
    private static final EntityDataAccessor<Float> DATA_ROLL_RAD = SynchedEntityData.defineId(SwordAuraEntity.class, (EntityDataSerializer)EntityDataSerializers.FLOAT);
    private static final int MAX_LIFETIME = 60;
    private static final double FLIGHT_SPEED = 1.5;
    private static final float BASE_DAMAGE = 100.0f;
    private static final float METAL_DAMAGE = 200.0f;
    private static final double BLOCK_HIT_AOE_RADIUS = 3.0;
    private static final float BLOCK_HIT_AOE_DAMAGE = 50.0f;
    private int lifetime = 0;
    private UUID ownerUuid;
    private float damageMultiplier = 1.0f;

    public void setDamageMultiplier(float m) {
        this.damageMultiplier = m;
    }

    public float getDamageMultiplier() {
        return this.damageMultiplier;
    }

    public SwordAuraEntity(EntityType<? extends SwordAuraEntity> type, Level level) {
        super(type, level);
        this.noPhysics = true;
    }

    public SwordAuraEntity(Level level, Player owner, Vec3 spawnPos, Vec3 dirNormalized, QiElement element) {
        this(level, (LivingEntity)owner, spawnPos, dirNormalized, element);
    }

    public SwordAuraEntity(Level level, LivingEntity owner, Vec3 spawnPos, Vec3 dirNormalized, QiElement element) {
        this((EntityType<? extends SwordAuraEntity>)((EntityType)ModEntities.SWORD_AURA.get()), level);
        this.ownerUuid = owner.getUUID();
        this.setPos(spawnPos.x, spawnPos.y, spawnPos.z);
        this.setDeltaMovement(dirNormalized.scale(1.5));
        this.entityData.set(DATA_ELEMENT, element.ordinal());
        this.entityData.set(DATA_ROLL_RAD, Float.valueOf((float)(this.random.nextDouble() * Math.PI * 2.0)));
    }

    protected void defineSynchedData() {
        this.entityData.define(DATA_ELEMENT, QiElement.PURE.ordinal());
        this.entityData.define(DATA_ROLL_RAD, Float.valueOf(0.0f));
    }

    public float rollRad() {
        return ((Float)this.entityData.get(DATA_ROLL_RAD)).floatValue();
    }

    public QiElement element() {
        int ord = (Integer)this.entityData.get(DATA_ELEMENT);
        QiElement[] vals = QiElement.values();
        return ord >= 0 && ord < vals.length ? vals[ord] : QiElement.PURE;
    }

    public void tick() {
        ServerLevel server;
        SectProtectionDomeHandler.BarrierHit barrierHit;
        if (TimeStasisHandler.pauseEntityTickInStoppedTime(this)) {
            return;
        }
        super.tick();
        if (this.level().isClientSide) {
            return;
        }
        ++this.lifetime;
        if (this.lifetime > 60) {
            this.discard();
            return;
        }
        Vec3 vel = this.getDeltaMovement();
        Vec3 oldPos = this.position();
        Vec3 newPos = oldPos.add(vel);
        Level level = this.level();
        if (level instanceof ServerLevel && (barrierHit = SectProtectionDomeHandler.touchProjectileBarrier(server = (ServerLevel)level, this, oldPos, newPos, this.getOwnerEntity(server), this.barrierDamageEquivalent(), 1.25)) != null) {
            this.impactOnBarrier(barrierHit.hitPos());
            return;
        }
        HitResult hit = ProjectileUtil.getHitResultOnMoveVector((Entity)this, this::canHitEntity);
        if (hit.getType() != HitResult.Type.MISS) {
            this.onHit(hit);
            return;
        }
        this.setPos(newPos.x, newPos.y, newPos.z);
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
        LivingEntity owner = this.getOwnerEntity(server);
        QiElement el = this.element();
        Vec3 hitPos = result.getLocation();
        if (result instanceof EntityHitResult && (entity = (ent = (EntityHitResult)result).getEntity()) instanceof LivingEntity && (target = (LivingEntity)entity).isAlive()) {
            this.applyEntityHit(server, target, owner, el);
        } else if (result instanceof BlockHitResult) {
            BlockHitResult bhr = (BlockHitResult)result;
            this.applyBlockHit(server, bhr, hitPos, owner, el);
        }
        this.playImpactSounds(server, hitPos);
        this.discard();
    }

    public void impactOnBarrier(Vec3 hitPos) {
        Level level = this.level();
        if (!(level instanceof ServerLevel)) {
            return;
        }
        ServerLevel server = (ServerLevel)level;
        LivingEntity owner = this.getOwnerEntity(server);
        this.applyBlockImpactEffects(server, hitPos, owner, this.element(), null);
        this.playImpactSounds(server, hitPos);
        this.discard();
    }

    private void playImpactSounds(ServerLevel server, Vec3 hitPos) {
        server.playSound(null, hitPos.x, hitPos.y, hitPos.z, SoundEvents.PLAYER_ATTACK_STRONG, SoundSource.PLAYERS, 0.8f, 1.2f);
        server.playSound(null, hitPos.x, hitPos.y, hitPos.z, SoundEvents.LIGHTNING_BOLT_IMPACT, SoundSource.PLAYERS, 0.4f, 1.8f);
    }

    private void applyEntityHit(ServerLevel server, LivingEntity target, LivingEntity owner, QiElement el) {
        if (!SoulStateHandler.canOrdinaryAffect((Entity)owner, (Entity)target)) {
            return;
        }
        if (!SectCombatHandler.canApplyOffensiveEffect(owner, target)) {
            return;
        }
        float dmg = el == QiElement.METAL ? 200.0f : 100.0f;
        target.hurt(SpellDamageSourceHelper.indirectSpell(this, owner), dmg *= this.damageMultiplier);
        switch (el) {
            case PURE: {
                break;
            }
            case METAL: {
                break;
            }
            case WOOD: {
                target.addEffect(new MobEffectInstance(MobEffects.POISON, 200, 1));
                break;
            }
            case WATER: {
                target.addEffect(new MobEffectInstance(MobEffects.WITHER, 200, 0));
                target.addEffect(new MobEffectInstance(MobEffects.CONFUSION, 200, 0));
                target.setAirSupply(-200);
                break;
            }
            case FIRE: {
                target.setRemainingFireTicks(200);
                break;
            }
            case EARTH: {
                target.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 200, 1));
                break;
            }
            case ICE: {
                IceShellEntity shell = new IceShellEntity(server, target);
                server.addFreshEntity((Entity)shell);
                break;
            }
        }
    }

    private void applyBlockHit(ServerLevel server, BlockHitResult bhr, Vec3 hitPos, LivingEntity owner, QiElement el) {
        BlockPos hitBlock = bhr.getBlockPos();
        if (server.getBlockState(hitBlock).getBlock() instanceof SectProtectionBarrierBlock || SectProtectionDomeHandler.domeOwningProtectedShell((Level)server, hitBlock) != null) {
            SectProtectionDomeHandler.onBarrierTouched((Level)server, hitBlock, hitPos, this.barrierDamageEquivalent());
            this.applyBlockImpactEffects(server, hitPos, owner, el, null);
            return;
        }
        this.applyBlockImpactEffects(server, hitPos, owner, el, hitBlock);
    }

    private void applyBlockImpactEffects(ServerLevel server, Vec3 hitPos, LivingEntity owner, QiElement el, BlockPos fireBaseBlock) {
        BlockPos above;
        AABB box = new AABB(hitPos.subtract(3.0, 3.0, 3.0), hitPos.add(3.0, 3.0, 3.0));
        for (Entity e : server.getEntitiesOfClass(Entity.class, box)) {
            double dist;
            LivingEntity living;
            if (e == this || owner != null && e.getUUID().equals(owner.getUUID()) || !(e instanceof LivingEntity) || !(living = (LivingEntity)e).isAlive() || !SoulStateHandler.canOrdinaryAffect((Entity)owner, (Entity)living) || !SectCombatHandler.canApplyOffensiveEffect(owner, living) || (dist = e.position().distanceTo(hitPos)) > 3.0) continue;
            float aoeDmg = (float)(50.0 * (1.0 - dist / 3.0));
            living.hurt(SpellDamageSourceHelper.indirectSpell(this, owner), aoeDmg *= this.damageMultiplier);
            switch (el) {
                case WOOD: {
                    living.addEffect(new MobEffectInstance(MobEffects.POISON, 100, 0));
                    break;
                }
                case WATER: {
                    living.addEffect(new MobEffectInstance(MobEffects.DIG_SLOWDOWN, 100, 0));
                    break;
                }
                case FIRE: {
                    living.setRemainingFireTicks(100);
                    break;
                }
                case EARTH: {
                    living.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 100, 0));
                    break;
                }
            }
        }
        if (el == QiElement.FIRE && fireBaseBlock != null && SpellTerrainDestructionHelper.canModifyBlocks(server, (Entity)owner) && server.getBlockState(above = fireBaseBlock.above()).isAir()) {
            SpellTerrainDestructionHelper.setBlockAndUpdate(server, above, Blocks.FIRE.defaultBlockState(), (Entity)owner);
        }
        for (int i = 0; i < 8; ++i) {
            double dx = (this.random.nextDouble() - 0.5) * 0.8;
            double dy = (this.random.nextDouble() - 0.5) * 0.8;
            double dz = (this.random.nextDouble() - 0.5) * 0.8;
            server.sendParticles((ParticleOptions)ParticleTypes.CRIT, hitPos.x, hitPos.y, hitPos.z, 1, dx, dy, dz, 0.3);
        }
    }

    private LivingEntity getOwner(ServerLevel server) {
        LivingEntity living;
        if (this.ownerUuid == null) {
            return null;
        }
        Entity e = server.getEntity(this.ownerUuid);
        return e instanceof LivingEntity ? (living = (LivingEntity)e) : null;
    }

    public LivingEntity getOwnerEntity(ServerLevel server) {
        return this.getOwner(server);
    }

    private float barrierDamageEquivalent() {
        float dmg = this.element() == QiElement.METAL ? 200.0f : 100.0f;
        return dmg * this.damageMultiplier;
    }

    public float getBarrierDamageEquivalent() {
        return this.barrierDamageEquivalent();
    }

    private void spawnTrailParticles() {
        Vec3 vel = this.getDeltaMovement();
        if (vel.lengthSqr() < 0.001) {
            return;
        }
        QiElement el = this.element();
        if (el == QiElement.EARTH) {
            for (int i = 0; i < 2; ++i) {
                this.level().addParticle((ParticleOptions)new BlockParticleOption(ParticleTypes.FALLING_DUST, Blocks.DIRT.defaultBlockState()), this.getX(), this.getY(), this.getZ(), 0.0, 0.0, 0.0);
            }
            return;
        }
        SimpleParticleType particleType = switch (el) {
            case FIRE -> ParticleTypes.FLAME;
            case WATER -> ParticleTypes.SPLASH;
            case ICE -> ParticleTypes.SNOWFLAKE;
            case WOOD -> ParticleTypes.HAPPY_VILLAGER;
            case METAL -> ParticleTypes.ELECTRIC_SPARK;
            default -> ParticleTypes.END_ROD;
        };
        for (int i = 0; i < 3; ++i) {
            this.level().addParticle((ParticleOptions)particleType, this.getX() + (this.random.nextDouble() - 0.5) * 0.3, this.getY() + (this.random.nextDouble() - 0.5) * 0.3, this.getZ() + (this.random.nextDouble() - 0.5) * 0.3, -vel.x * 0.1, -vel.y * 0.1, -vel.z * 0.1);
        }
    }

    protected void readAdditionalSaveData(@NotNull CompoundTag tag) {
        this.lifetime = tag.getInt("lifetime");
        if (tag.contains("owner")) {
            this.ownerUuid = tag.getUUID("owner");
        }
        this.entityData.set(DATA_ELEMENT, tag.getInt("element"));
        this.entityData.set(DATA_ROLL_RAD, Float.valueOf(tag.getFloat("roll")));
        if (tag.contains("damageMultiplier")) {
            this.damageMultiplier = Math.max(0.0f, tag.getFloat("damageMultiplier"));
        }
    }

    protected void addAdditionalSaveData(@NotNull CompoundTag tag) {
        tag.putInt("lifetime", this.lifetime);
        if (this.ownerUuid != null) {
            tag.putUUID("owner", this.ownerUuid);
        }
        tag.putInt("element", this.element().ordinal());
        tag.putFloat("roll", this.rollRad());
        tag.putFloat("damageMultiplier", this.damageMultiplier);
    }

    @NotNull
    public Packet<ClientGamePacketListener> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket((Entity)this);
    }

    protected AABB makeBoundingBox() {
        return super.makeBoundingBox();
    }
}

