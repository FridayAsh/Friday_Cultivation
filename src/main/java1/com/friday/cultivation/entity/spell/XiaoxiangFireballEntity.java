package com.friday.cultivation.entity.spell;

import com.friday.cultivation.event.SectCombatHandler;
import com.friday.cultivation.event.SectProtectionDomeHandler;
import com.friday.cultivation.event.SoulStateHandler;
import com.friday.cultivation.event.TimeStasisHandler;
import com.friday.cultivation.registry.ModEntities;
import com.friday.cultivation.util.SpellTerrainDestructionHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Fireball;
import net.minecraft.world.entity.projectile.LargeFireball;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

/**
 * 火球术实体 — 完整复刻原模组 XiaoxiangFireballEntity
 * 继承 LargeFireball，附带额外伤害和受控爆炸
 */
public class XiaoxiangFireballEntity extends LargeFireball {
    private int extraDamage = 0;
    private int configuredExplosionPower = 1;

    public XiaoxiangFireballEntity(EntityType<? extends LargeFireball> type, Level level) {
        super(type, level);
    }

    public XiaoxiangFireballEntity(Level level, LivingEntity owner, double dx, double dy, double dz, int explosionPower) {
        super(level, owner, dx, dy, dz, explosionPower);
        this.configuredExplosionPower = Math.max(0, explosionPower);
    }

    public void setExtraDamage(int v) { this.extraDamage = Math.max(0, v); }
    public int getExtraDamage() { return this.extraDamage; }

    @Override
    public void tick() {
        if (TimeStasisHandler.pauseEntityTickInStoppedTime(this)) return;
        super.tick();
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        Entity owner = this.getOwner();
        Entity entity = result.getEntity();
        if (!this.level().isClientSide && entity instanceof LivingEntity target
                && (!SoulStateHandler.canOrdinaryAffect(owner, target)
                    || owner instanceof LivingEntity ownerLiving && !SectCombatHandler.canApplyOffensiveEffect(ownerLiving, target))) {
            return;
        }
        super.onHitEntity(result);
        if (this.level().isClientSide) return;
        if (this.extraDamage <= 0) return;
        entity = result.getEntity();
        if (entity instanceof LivingEntity target && target.isAlive()
                && (!(owner instanceof LivingEntity) || SectCombatHandler.canApplyOffensiveEffect((LivingEntity) owner, target))) {
            target.hurt(this.damageSources().fireball(this, this.getOwner()), (float) this.extraDamage);
        }
    }

    @Override
    protected void onHit(HitResult result) {
        if (result instanceof EntityHitResult entityHit) {
            this.onHitEntity(entityHit);
        } else if (result instanceof BlockHitResult blockHit) {
            this.onHitBlock(blockHit);
        }
        if (this.level().isClientSide) return;
        if (!(this.level() instanceof ServerLevel server)) return;
        boolean mobGriefing = server.getGameRules().getBoolean(GameRules.RULE_MOBGRIEFING);
        Entity terrainCaster = this.getOwner() != null ? this.getOwner() : this;
        SectProtectionDomeHandler.onSpellAreaTouchedBarrier(server, result.getLocation(),
                Math.max(2.0, this.configuredExplosionPower + 2.0), terrainCaster, Math.max(1.0f, this.getExtraDamage()));
        server.explode(terrainCaster, this.getX(), this.getY(), this.getZ(), this.configuredExplosionPower, false,
                mobGriefing ? SpellTerrainDestructionHelper.explosionInteraction(server, terrainCaster, Level.ExplosionInteraction.MOB) : Level.ExplosionInteraction.NONE);
        this.discard();
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        if (tag.contains("XiaoxiangExplosionPower")) {
            this.configuredExplosionPower = Math.max(0, tag.getInt("XiaoxiangExplosionPower"));
        } else if (tag.contains("ExplosionPower")) {
            this.configuredExplosionPower = Math.max(0, tag.getByte("ExplosionPower"));
        }
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("XiaoxiangExplosionPower", this.configuredExplosionPower);
    }

    public static XiaoxiangFireballEntity create(EntityType<XiaoxiangFireballEntity> type, Level level) {
        return new XiaoxiangFireballEntity(type, level);
    }

    public static EntityType<XiaoxiangFireballEntity> typeRef() {
        return ModEntities.XIAOXIANG_FIREBALL.get();
    }
}
