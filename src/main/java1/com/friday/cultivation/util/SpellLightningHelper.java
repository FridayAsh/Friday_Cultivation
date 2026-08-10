package com.friday.cultivation.util;

import com.friday.cultivation.event.SectCombatHandler;
import com.friday.cultivation.event.SoulStateHandler;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/**
 * 法术闪电辅助 — 完整复刻原模组 SpellLightningHelper
 * 在指定位置生成视觉闪电并对范围内实体造成法术伤害
 */
public final class SpellLightningHelper {
    private static final double STRIKE_RADIUS = 3.0;

    private SpellLightningHelper() {}

    /** 在指定位置生成闪电并造成范围伤害 */
    public static void strike(ServerLevel level, LivingEntity caster, Vec3 pos, float damage) {
        spawnVisualBolt(level, caster, pos);
        AABB box = new AABB(pos, pos).inflate(3.0);
        for (LivingEntity target : level.getEntitiesOfClass(LivingEntity.class, box, LivingEntity::isAlive)) {
            if (target == caster) continue;
            if (!SoulStateHandler.canOrdinaryAffect(caster, target)) continue;
            if (!SectCombatHandler.canApplyOffensiveEffect(caster, target)) continue;
            Vec3 targetCenter = target.position().add(0.0, target.getBbHeight() * 0.5, 0.0);
            if (targetCenter.distanceToSqr(pos) > 9.0) continue;
            target.hurt(SpellDamageSourceHelper.directSpell(caster), damage);
        }
    }

    /** 生成视觉闪电（无伤害） */
    private static void spawnVisualBolt(ServerLevel level, LivingEntity caster, Vec3 pos) {
        LightningBolt bolt = EntityType.LIGHTNING_BOLT.create(level);
        if (bolt == null) return;
        bolt.moveTo(pos.x, pos.y, pos.z);
        bolt.setVisualOnly(true);
        bolt.setDamage(0.0f);
        if (caster instanceof ServerPlayer player) {
            bolt.setCause(player);
        }
        level.addFreshEntity(bolt);
    }
}
