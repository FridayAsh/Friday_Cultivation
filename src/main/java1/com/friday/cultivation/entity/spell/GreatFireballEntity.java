package com.friday.cultivation.entity.spell;

import com.friday.cultivation.event.SectCombatHandler;
import com.friday.cultivation.event.SectProtectionDomeHandler;
import com.friday.cultivation.event.SoulStateHandler;
import com.friday.cultivation.event.TimeStasisHandler;
import com.friday.cultivation.registry.ModEntities;
import com.friday.cultivation.util.SpellTerrainDestructionHelper;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Fireball;
import net.minecraft.world.entity.projectile.LargeFireball;
import net.minecraft.world.entity.projectile.SmallFireball;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

/**
 * 大火球术实体 — 完整复刻原模组 GreatFireballEntity
 * 充能灵气控制爆炸威力/额外伤害/火雨数量 + 客户端粒子拖尾 + SynchedEntityData同步
 */
public class GreatFireballEntity extends LargeFireball {
    private static final EntityDataAccessor<Integer> DATA_CHARGED_QI = SynchedEntityData.defineId(GreatFireballEntity.class, EntityDataSerializers.INT);
    private static final float MIN_RENDER_DIAMETER = 0.5f;
    private static final float MAX_RENDER_DIAMETER = 80.0f;
    private static final double RENDER_BASE_QI = 100.0;
    private static final double RENDER_SIZE_EXPONENT = 0.85;
    private static final int FULL_POWER_CHARGE_QI = 10000;
    private static final float MIN_EXPLOSION_POWER = 8.0f;
    private static final float MAX_EXPLOSION_POWER = 52.0f;
    private static final double EXPLOSION_POWER_EXPONENT = 0.65;
    private static final float MIN_TERRAIN_MULTIPLIER = 0.75f;
    private static final float MAX_TERRAIN_MULTIPLIER = 1.25f;
    private static final float EXTRA_DAMAGE_QI_DIVISOR = 20.0f;

    private float configuredExplosionPower = 1.0f;
    private int configuredExtraDamage = 0;
    private int configuredRainCount = 0;
    private float damageMultiplier = 1.0f;

    public GreatFireballEntity(EntityType<? extends LargeFireball> type, Level level) {
        super(type, level);
    }

    public GreatFireballEntity(Level level, LivingEntity owner, double dx, double dy, double dz, int chargedQi) {
        super(ModEntities.GREAT_FIREBALL.get(), level);
        this.setOwner(owner);
        this.setRot(owner.getYRot(), owner.getXRot());
        this.setPos(owner.getX(), owner.getEyeY(), owner.getZ());
        this.reapplyPosition();
        double d0 = Math.sqrt(dx * dx + dy * dy + dz * dz);
        if (d0 != 0.0) {
            this.xPower = dx / d0 * 0.1;
            this.yPower = dy / d0 * 0.1;
            this.zPower = dz / d0 * 0.1;
        }
        this.entityData.set(DATA_CHARGED_QI, chargedQi);
        this.configuredExplosionPower = computeExplosionPower(chargedQi, this.damageMultiplier);
        this.configuredExtraDamage = computeExtraDamage(chargedQi, this.damageMultiplier);
        this.configuredRainCount = computeRainCount(chargedQi);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(DATA_CHARGED_QI, 0);
    }

    public int getChargedQi() {
        return this.entityData.get(DATA_CHARGED_QI);
    }

    public static float renderDiameterForCharge(int chargedQi) {
        if (chargedQi <= 0) {
            return MIN_RENDER_DIAMETER;
        }
        return (float) Math.min(MAX_RENDER_DIAMETER, Math.pow((double) chargedQi / RENDER_BASE_QI, RENDER_SIZE_EXPONENT));
    }

    public float getRenderScale() {
        return renderDiameterForCharge(this.getChargedQi());
    }

    public float computedExplosionPower() {
        return this.configuredExplosionPower;
    }

    public int computedExtraDamage() {
        return this.configuredExtraDamage;
    }

    public int computedRainCount() {
        return this.configuredRainCount;
    }

    public void setDamageMultiplier(double multiplier) {
        if (!Double.isFinite(multiplier)) {
            multiplier = 1.0;
        }
        this.damageMultiplier = (float) Math.max(0.0, multiplier);
        this.configuredExplosionPower = computeExplosionPower(this.getChargedQi(), this.damageMultiplier);
        this.configuredExtraDamage = computeExtraDamage(this.getChargedQi(), this.damageMultiplier);
    }

    public void setManualPower(double px, double py, double pz) {
        this.xPower = px;
        this.yPower = py;
        this.zPower = pz;
    }

    private static float computeExplosionPower(int chargedQi, float damageMultiplier) {
        double chargeRatio = Math.min(1.0, Math.max(0.0, (double) chargedQi / FULL_POWER_CHARGE_QI));
        double basePower = MIN_EXPLOSION_POWER + Math.pow(chargeRatio, EXPLOSION_POWER_EXPONENT) * (MAX_EXPLOSION_POWER - MIN_EXPLOSION_POWER);
        double terrainMultiplier = Math.sqrt(Math.max(0.0f, damageMultiplier));
        terrainMultiplier = Math.max(MIN_TERRAIN_MULTIPLIER, Math.min(MAX_TERRAIN_MULTIPLIER, terrainMultiplier));
        return clampExplosionPower(basePower * terrainMultiplier);
    }

    private static int computeExtraDamage(int chargedQi, float damageMultiplier) {
        return Math.max(0, Math.round((float) chargedQi / EXTRA_DAMAGE_QI_DIVISOR * Math.max(0.0f, damageMultiplier)));
    }

    private static float clampExplosionPower(double power) {
        return (float) Math.max(MIN_EXPLOSION_POWER, Math.min(MAX_EXPLOSION_POWER, power));
    }

    private static int computeRainCount(int chargedQi) {
        if (chargedQi < 200) {
            return 0;
        }
        return (int) Math.min(80.0, Math.sqrt(chargedQi) * 0.5);
    }

    @Override
    public void tick() {
        if (TimeStasisHandler.pauseEntityTickInStoppedTime(this)) {
            return;
        }
        super.tick();
        if (this.level().isClientSide) {
            float visualR = this.getRenderScale() * 0.6f;
            int sparkCount = (int) Math.max(4.0f, Math.min(30.0f, visualR * 1.5f));
            for (int i = 0; i < sparkCount; ++i) {
                double a = this.random.nextFloat() * Math.PI * 2.0;
                double b = (this.random.nextFloat() - 0.5) * Math.PI;
                double sx = Math.cos(a) * Math.cos(b);
                double sy = Math.sin(b);
                double sz = Math.sin(a) * Math.cos(b);
                double speed = 0.04 + this.random.nextFloat() * 0.06;
                int t = this.random.nextInt(4);
                SimpleParticleType p = t == 0 ? ParticleTypes.LAVA : (t == 1 ? ParticleTypes.SMALL_FLAME : ParticleTypes.FLAME);
                this.level().addParticle(p, this.getX() + sx * (double) visualR, this.getY() + sy * (double) visualR, this.getZ() + sz * (double) visualR, sx * speed, sy * speed + 0.02, sz * speed);
            }
            Vec3 vel = this.getDeltaMovement();
            double speedLen = vel.length();
            if (speedLen > 0.01) {
                Vec3 dir = vel.scale(1.0 / speedLen);
                int trailCount = (int) Math.max(3.0f, Math.min(15.0f, visualR * 0.5f));
                for (int i = 0; i < trailCount; ++i) {
                    double ox = (this.random.nextFloat() - 0.5) * (double) visualR * 0.6;
                    double oy = (this.random.nextFloat() - 0.5) * (double) visualR * 0.6;
                    double oz = (this.random.nextFloat() - 0.5) * (double) visualR * 0.6;
                    double tailDist = (double) visualR + this.random.nextFloat() * (double) visualR;
                    this.level().addParticle(ParticleTypes.FLAME, this.getX() - dir.x * tailDist + ox, this.getY() - dir.y * tailDist + oy, this.getZ() - dir.z * tailDist + oz, -dir.x * 0.1, -dir.y * 0.1, -dir.z * 0.1);
                }
            }
        }
    }

    @Override
    protected void onHit(@NotNull HitResult result) {
        if (this.level().isClientSide) {
            return;
        }
        Level level = this.level();
        if (!(level instanceof ServerLevel server)) {
            return;
        }
        Vec3 pos = result.getLocation();
        Entity owner = this.getOwner();
        if (result instanceof EntityHitResult entityHit) {
            Entity entity = entityHit.getEntity();
            if (entity instanceof LivingEntity victim && victim.isAlive()
                    && SoulStateHandler.canOrdinaryAffect(owner, victim)
                    && (!(owner instanceof LivingEntity ownerLiving) || SectCombatHandler.canApplyOffensiveEffect(ownerLiving, victim))
                    && this.configuredExtraDamage > 0) {
                victim.hurt(this.damageSources().fireball(this, owner), (float) this.configuredExtraDamage);
            }
        }
        Entity terrainCaster = owner != null ? owner : this;
        boolean canModifyTerrain = SpellTerrainDestructionHelper.canModifyBlocks(server, terrainCaster);
        SectProtectionDomeHandler.onSpellAreaTouchedBarrier(server, pos, Math.max(2.0, (double) this.configuredExplosionPower + 2.0), terrainCaster, Math.max(1.0f, (float) this.computedExtraDamage()));
        server.explode(terrainCaster, pos.x, pos.y, pos.z, this.configuredExplosionPower, canModifyTerrain, SpellTerrainDestructionHelper.explosionInteraction(server, terrainCaster, Level.ExplosionInteraction.MOB));
        int rainCount = this.configuredRainCount;
        if (rainCount > 0 && canModifyTerrain) {
            double rainRadius = this.configuredExplosionPower;
            Entity ownerEntity = this.getOwner();
            LivingEntity ownerLiving = ownerEntity instanceof LivingEntity le ? le : null;
            for (int i = 0; i < rainCount; ++i) {
                double angle = this.random.nextFloat() * Math.PI * 2.0;
                double dist = this.random.nextFloat() * rainRadius;
                double rx = pos.x + Math.cos(angle) * dist;
                double rz = pos.z + Math.sin(angle) * dist;
                double ry = pos.y + 8.0 + this.random.nextFloat() * 6.0;
                SmallFireball child = new SmallFireball(this.level(), ownerLiving, (this.random.nextFloat() - 0.5) * 0.2, -1.0, (this.random.nextFloat() - 0.5) * 0.2);
                child.setPos(rx, ry, rz);
                server.addFreshEntity(child);
            }
        }
        this.discard();
    }

    @Override
    public void readAdditionalSaveData(@NotNull CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        if (tag.contains("chargedQi")) {
            this.entityData.set(DATA_CHARGED_QI, tag.getInt("chargedQi"));
        }
        this.damageMultiplier = tag.contains("damageMultiplier") ? Math.max(0.0f, tag.getFloat("damageMultiplier")) : 1.0f;
        int chargedQi = this.getChargedQi();
        this.configuredExplosionPower = computeExplosionPower(chargedQi, this.damageMultiplier);
        this.configuredExtraDamage = computeExtraDamage(chargedQi, this.damageMultiplier);
        this.configuredRainCount = computeRainCount(chargedQi);
    }

    @Override
    public void addAdditionalSaveData(@NotNull CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("chargedQi", this.getChargedQi());
        tag.putFloat("damageMultiplier", this.damageMultiplier);
    }

    public static GreatFireballEntity create(EntityType<GreatFireballEntity> type, Level level) {
        return new GreatFireballEntity(type, level);
    }

    public static EntityType<GreatFireballEntity> typeRef() {
        return ModEntities.GREAT_FIREBALL.get();
    }
}
