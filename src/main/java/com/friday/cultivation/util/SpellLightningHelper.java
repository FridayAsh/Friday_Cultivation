/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.server.level.ServerLevel
 *  net.minecraft.server.level.ServerPlayer
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.EntityType
 *  net.minecraft.world.entity.LightningBolt
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.phys.AABB
 *  net.minecraft.world.phys.Vec3
 */
package com.friday.cultivation.util;

import com.friday.cultivation.event.SectCombatHandler;
import com.friday.cultivation.event.SoulStateHandler;
import com.friday.cultivation.util.SpellDamageSourceHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public final class SpellLightningHelper {
    private static final double STRIKE_RADIUS = 3.0;

    private SpellLightningHelper() {
    }

    public static void strike(ServerLevel level, LivingEntity caster, Vec3 pos, float damage) {
        SpellLightningHelper.spawnVisualBolt(level, caster, pos);
        AABB box = new AABB(pos, pos).inflate(3.0);
        for (LivingEntity target : level.getEntitiesOfClass(LivingEntity.class, box, LivingEntity::isAlive)) {
            Vec3 targetCenter;
            if (target == caster || !SoulStateHandler.canOrdinaryAffect((Entity)caster, (Entity)target) || !SectCombatHandler.canApplyOffensiveEffect(caster, target) || (targetCenter = target.position().add(0.0, (double)target.getBbHeight() * 0.5, 0.0)).distanceToSqr(pos) > 9.0) continue;
            target.hurt(SpellDamageSourceHelper.directSpell(caster), damage);
        }
    }

    private static void spawnVisualBolt(ServerLevel level, LivingEntity caster, Vec3 pos) {
        LightningBolt bolt = (LightningBolt)EntityType.LIGHTNING_BOLT.create((Level)level);
        if (bolt == null) {
            return;
        }
        bolt.moveTo(pos.x, pos.y, pos.z);
        bolt.setVisualOnly(true);
        bolt.setDamage(0.0f);
        if (caster instanceof ServerPlayer) {
            ServerPlayer player = (ServerPlayer)caster;
            bolt.setCause(player);
        }
        level.addFreshEntity((Entity)bolt);
    }
}

