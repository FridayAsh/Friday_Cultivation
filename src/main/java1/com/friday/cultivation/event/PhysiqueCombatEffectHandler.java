package com.friday.cultivation.event;

import com.friday.cultivation.CultivationCapability;
import com.friday.cultivation.capability.CultivationData;
import com.friday.cultivation.physique.Physique;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * 体质战斗特效处理器
 * 复刻自原模组 com.xiaoxiang.cultivation.event.PhysiqueCombatEffectHandler
 *
 * 效果:
 * - 天火之体: 攻击附加燃烧 (set remaining fire ticks)
 * - 玄冰之体: 攻击减速目标 (movement slowdown + slowness effect)
 */
@Mod.EventBusSubscriber(modid = "friday_cultivation")
public final class PhysiqueCombatEffectHandler {

    private PhysiqueCombatEffectHandler() {}

    @SubscribeEvent
    public static void onLivingHurt(LivingHurtEvent event) {
        if (event.isCanceled()) return;
        if (event.getAmount() <= 0.0f) return;

        LivingEntity target = event.getEntity();
        Entity attackerEntity = event.getSource().getEntity();
        if (attackerEntity == null || attackerEntity == target) return;

        Physique physique = attackerPhysique(attackerEntity);
        if (physique == Physique.HEAVENLY_FIRE_BODY) {
            // 着火时长 = max(5秒, 目标当前着火时间/20)
            target.setRemainingFireTicks(Math.max(5 * 20, target.getRemainingFireTicks() / 20 * 20));
        } else if (physique == Physique.MYSTIC_ICE_BODY) {
            // 减速：移动速度设为 max(当前速度, 160 ticks = 8秒)
            target.setTicksFrozen(Math.max(target.getTicksFrozen(), 160));
            target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 100, 1));
        }
    }

    private static Physique attackerPhysique(Entity attacker) {
        if (attacker instanceof Player player) {
            CultivationData ic = CultivationCapability.get(player).orElse(null);
            if (ic != null) return ic.getPhysique();
        }
        return Physique.MORTAL_BODY;
    }
}
