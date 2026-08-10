package com.friday.cultivation.event;

import com.friday.cultivation.CultivationBonusCategory;
import com.friday.cultivation.CultivationCapability;
import com.friday.cultivation.capability.CultivationData;
import com.friday.cultivation.dao.FoundationDaoBonusHelper;
import com.friday.cultivation.dao.GoldenCoreDaoBonusHelper;
import com.friday.cultivation.dao.LooseImmortalBonusHelper;
import com.friday.cultivation.entity.npc.WanderingCultivatorEntity;
import com.friday.cultivation.spirit.SpiritRootBonusHelper;
import com.friday.cultivation.technique.TechniqueBonusHelper;
import com.friday.cultivation.ZhenyuanBonusHelper;
import java.util.concurrent.ThreadLocalRandom;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.SwordItem;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = "friday_cultivation")
public final class AttackBonusHandler {
    private AttackBonusHandler() {
    }

    @SubscribeEvent(priority = EventPriority.LOW)
    public static void onLivingHurt(LivingHurtEvent event) {
        int techCrit;
        int baseCrit;
        int critTotal;
        double dharmaBodyMult;
        double swordMult;
        double meleeMult;
        double srMult;
        if (event.isCanceled()) {
            return;
        }
        if (!event.getSource().is(DamageTypes.PLAYER_ATTACK)) {
            return;
        }
        Entity entity = event.getSource().getEntity();
        if (!(entity instanceof ServerPlayer)) {
            return;
        }
        ServerPlayer attacker = (ServerPlayer) entity;
        if (!SoulStateHandler.canOrdinaryAffect((Entity) attacker, (Entity) event.getEntity())) {
            return;
        }
        CultivationData attackerData = CultivationCapability.get((Player) attacker).orElse(null);
        if (attackerData != null && !attackerData.isBonusCategoryEnabled(CultivationBonusCategory.MELEE_DAMAGE)) {
            return;
        }
        int baseBonus = attackerData == null ? 0 : attackerData.getAttack();
        int techAttack = TechniqueBonusHelper.attackBonus((Player) attacker);
        int zhenyuanAttack = ZhenyuanBonusHelper.physiqueAttackBonus((Player) attacker);
        int routeAttack = FoundationDaoBonusHelper.meleeDamageBonus((Player) attacker) + GoldenCoreDaoBonusHelper.meleeDamageBonus((Player) attacker) + LooseImmortalBonusHelper.meleeDamageBonus((Player) attacker);
        int totalBonus = baseBonus + techAttack + zhenyuanAttack + routeAttack;
        float amount = event.getAmount();
        if (totalBonus > 0) {
            amount += (float) totalBonus;
        }
        if ((srMult = (meleeMult = SpiritRootBonusHelper.meleeDamageMultiplier((Player) attacker)) * (swordMult = SpiritRootBonusHelper.swordDamageMultiplier((Player) attacker))) != 1.0) {
            amount = (float) ((double) amount * srMult);
        }
        if ((dharmaBodyMult = DharmaBodyManifestationHandler.meleeDamageMultiplier(attacker)) != 1.0) {
            amount = (float) ((double) amount * dharmaBodyMult);
        }
        if ((critTotal = Math.min(100, (baseCrit = attackerData == null ? 0 : attackerData.getCritRate()) + (techCrit = TechniqueBonusHelper.critRateBonus((Player) attacker)))) > 0 && ThreadLocalRandom.current().nextInt(100) < critTotal) {
            amount *= 1.5f;
        }
        amount = (float) ((double) amount * RealmPressureHandler.outgoingDamageMultiplier((LivingEntity) attacker));
        event.setAmount(amount);
    }

    @SubscribeEvent(priority = EventPriority.LOW)
    public static void onNpcLivingHurt(LivingHurtEvent event) {
        if (event.isCanceled()) {
            return;
        }
        if (!event.getSource().is(DamageTypes.MOB_ATTACK)) {
            return;
        }
        Entity entity = event.getSource().getEntity();
        if (!(entity instanceof WanderingCultivatorEntity)) {
            return;
        }
        WanderingCultivatorEntity npc = (WanderingCultivatorEntity) entity;
        if (npc.isNpcSoulState()) {
            return;
        }
        if (!SoulStateHandler.canOrdinaryAffect((Entity) npc, (Entity) event.getEntity())) {
            return;
        }
        double multiplier = npc.getPhysique().bonus().meleeDmgMult();
        if (npc.getMainHandItem().getItem() instanceof SwordItem) {
            multiplier *= npc.getPhysique().bonus().swordSpellMult();
        }
        multiplier *= DharmaBodyManifestationHandler.meleeDamageMultiplier(npc);
        if ((multiplier *= RealmPressureHandler.outgoingDamageMultiplier((LivingEntity) npc)) != 1.0) {
            event.setAmount((float) ((double) event.getAmount() * multiplier));
        }
    }

    @SubscribeEvent
    public static void onAttackEntity(AttackEntityEvent event) {
    }
}
