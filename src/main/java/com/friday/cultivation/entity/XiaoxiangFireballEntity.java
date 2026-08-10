/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.nbt.CompoundTag
 *  net.minecraft.server.level.ServerLevel
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.EntityType
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.entity.projectile.Fireball
 *  net.minecraft.world.entity.projectile.LargeFireball
 *  net.minecraft.world.level.GameRules
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.level.Level$ExplosionInteraction
 *  net.minecraft.world.phys.BlockHitResult
 *  net.minecraft.world.phys.EntityHitResult
 *  net.minecraft.world.phys.HitResult
 *  org.jetbrains.annotations.NotNull
 */
package com.friday.cultivation.entity;

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
import org.jetbrains.annotations.NotNull;

public class XiaoxiangFireballEntity
extends LargeFireball {
    private int extraDamage = 0;
    private int configuredExplosionPower = 1;

    public XiaoxiangFireballEntity(EntityType<? extends LargeFireball> type, Level level) {
        super(type, level);
    }

    public XiaoxiangFireballEntity(Level level, LivingEntity owner, double dx, double dy, double dz, int explosionPower) {
        super(level, owner, dx, dy, dz, explosionPower);
        this.configuredExplosionPower = Math.max(0, explosionPower);
    }

    public void setExtraDamage(int v) {
        this.extraDamage = Math.max(0, v);
    }

    public int getExtraDamage() {
        return this.extraDamage;
    }

    public void tick() {
        if (TimeStasisHandler.pauseEntityTickInStoppedTime((Entity)this)) {
            return;
        }
        super.tick();
    }

    protected void onHit(@NotNull EntityHitResult result) {
        LivingEntity ownerLiving;
        LivingEntity target;
        Entity entity;
        Entity owner = this.getOwner();
        if (!this.level().isClientSide && (entity = result.getEntity()) instanceof LivingEntity && (!SoulStateHandler.canOrdinaryAffect(owner, (Entity)(target = (LivingEntity)entity)) || owner instanceof LivingEntity && !SectCombatHandler.canApplyOffensiveEffect(ownerLiving = (LivingEntity)owner, target))) {
            return;
        }
        super.onHit(result);
        if (this.level().isClientSide) {
            return;
        }
        if (this.extraDamage <= 0) {
            return;
        }
        entity = result.getEntity();
        if (entity instanceof LivingEntity && (target = (LivingEntity)entity).isAlive() && (!(owner instanceof LivingEntity) || SectCombatHandler.canApplyOffensiveEffect(ownerLiving = (LivingEntity)owner, target))) {
            target.hurt(this.damageSources().fireball((Fireball)this, this.getOwner()), (float)this.extraDamage);
        }
    }

    protected void onHit(@NotNull HitResult result) {
        BlockHitResult blockHit;
        if (result instanceof EntityHitResult) {
            EntityHitResult entityHit = (EntityHitResult)result;
            this.onHit(entityHit);
        } else if (result instanceof BlockHitResult) {
            blockHit = (BlockHitResult)result;
            this.onHitBlock(blockHit);
        }
        if (this.level().isClientSide) {
            return;
        }
        Level level = this.level();
        if (!(level instanceof ServerLevel)) {
            return;
        }
        ServerLevel server = (ServerLevel)level;
        boolean mobGriefing = server.getGameRules().getBoolean(GameRules.RULE_MOBGRIEFING);
        Entity terrainCaster = this.getOwner() != null ? this.getOwner() : this;
        SectProtectionDomeHandler.onSpellAreaTouchedBarrier(server, result.getLocation(), Math.max(2.0, (double)this.configuredExplosionPower + 2.0), terrainCaster, Math.max(1.0f, (float)this.getExtraDamage()));
        server.explode(terrainCaster, this.getX(), this.getY(), this.getZ(), (float)this.configuredExplosionPower, false, mobGriefing ? SpellTerrainDestructionHelper.explosionInteraction(server, terrainCaster, Level.ExplosionInteraction.MOB) : Level.ExplosionInteraction.NONE);
        this.discard();
    }

    public void readAdditionalSaveData(@NotNull CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        if (tag.contains("XiaoxiangExplosionPower")) {
            this.configuredExplosionPower = Math.max(0, tag.getInt("XiaoxiangExplosionPower"));
        } else if (tag.contains("ExplosionPower")) {
            this.configuredExplosionPower = Math.max(0, tag.getByte("ExplosionPower"));
        }
    }

    public void saveAdditional(@NotNull CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("XiaoxiangExplosionPower", this.configuredExplosionPower);
    }

    public static XiaoxiangFireballEntity create(EntityType<XiaoxiangFireballEntity> type, Level level) {
        return new XiaoxiangFireballEntity(type, level);
    }

    public static EntityType<XiaoxiangFireballEntity> typeRef() {
        return (EntityType)ModEntities.XIAOXIANG_FIREBALL.get();
    }
}

