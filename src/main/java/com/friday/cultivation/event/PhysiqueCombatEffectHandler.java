/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.world.effect.MobEffectInstance
 *  net.minecraft.world.effect.MobEffects
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.entity.player.Player
 *  net.minecraftforge.event.entity.living.LivingHurtEvent
 *  net.minecraftforge.eventbus.api.SubscribeEvent
 *  net.minecraftforge.fml.common.Mod$EventBusSubscriber
 */
package com.friday.cultivation.event;

import com.friday.cultivation.cultivation.Physique;
import com.friday.cultivation.cultivation.PhysiqueBonusHelper;
import com.friday.cultivation.entity.npc.WanderingCultivatorEntity;
import com.friday.cultivation.event.SectCombatHandler;
import com.friday.cultivation.event.SoulStateHandler;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid="friday_cultivation")
public final class PhysiqueCombatEffectHandler {
    private PhysiqueCombatEffectHandler() {
    }

    @SubscribeEvent
    public static void onLivingHurt(LivingHurtEvent event) {
        LivingEntity livingAttacker;
        if (event.isCanceled()) {
            return;
        }
        if (event.getAmount() <= 0.0f) {
            return;
        }
        LivingEntity target = event.getEntity();
        Entity attacker = event.getSource().getEntity();
        if (attacker == null || attacker == target) {
            return;
        }
        if (!SoulStateHandler.canOrdinaryAffect(attacker, (Entity)target)) {
            return;
        }
        if (attacker instanceof LivingEntity && !SectCombatHandler.canApplyOffensiveEffect(livingAttacker = (LivingEntity)attacker, target)) {
            return;
        }
        Physique physique = PhysiqueCombatEffectHandler.attackerPhysique(attacker);
        if (physique == Physique.HEAVENLY_FIRE_BODY) {
            target.setSecondsOnFire(Math.max(5, target.getRemainingFireTicks() / 20));
        } else if (physique == Physique.MYSTIC_ICE_BODY) {
            target.setTicksFrozen(Math.max(target.getTicksFrozen(), 160));
            target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 100, 1));
        }
    }

    private static Physique attackerPhysique(Entity attacker) {
        if (attacker instanceof Player) {
            Player player = (Player)attacker;
            return PhysiqueBonusHelper.physiqueOf(player);
        }
        if (attacker instanceof WanderingCultivatorEntity) {
            WanderingCultivatorEntity npc = (WanderingCultivatorEntity)attacker;
            if (npc.isNpcSoulState()) {
                return Physique.MORTAL_BODY;
            }
            return npc.getPhysique();
        }
        return Physique.MORTAL_BODY;
    }
}

