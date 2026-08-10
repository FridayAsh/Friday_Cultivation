package com.friday.cultivation.event;

import com.friday.cultivation.CultivationCapability;
import com.friday.cultivation.capability.CultivationData;
import com.friday.cultivation.spell.Spell;
import com.friday.cultivation.technique.TechniqueBonusHelper;
import com.friday.cultivation.entity.npc.WanderingCultivatorEntity;
import com.friday.cultivation.network.ClientOnlyGlowPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * 灵魂标记 handler - 玩家/NPC 攻击时，对目标施加 ClientOnlyGlow 高亮；
 * 消耗 10 qi/击，命中后只能玩家自己可见高亮持续 60s。
 * 严格 1:1 复刻原 mod SoulMarkHandler。
 */
@Mod.EventBusSubscriber(modid = "friday_cultivation")
public final class SoulMarkHandler {
    private static final long QI_COST_PER_HIT = 10L;
    private static final int GLOWING_DURATION_TICKS = 1200;

    private SoulMarkHandler() {
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onLivingAttack(LivingAttackEvent event) {
        if (event.isCanceled()) {
            return;
        }
        LivingEntity target = event.getEntity();
        if (target == null || !target.isAlive()) {
            return;
        }
        DamageSource src = event.getSource();
        if (!SoulStateHandler.canOrdinaryAffect((Entity) src.getDirectEntity(), (Entity) target)) {
            return;
        }
        Entity entity = src.getDirectEntity();
        if (entity instanceof WanderingCultivatorEntity npc) {
            if (npc.isNpcSoulState()) {
                return;
            }
            if (!src.is(DamageTypes.MOB_ATTACK)) {
                return;
            }
            if (target == npc || SpiritLockHandler.isEntityLocked((Entity) npc)) {
                return;
            }
            if (!npc.getSpellIds().contains(Spell.SOUL_MARK.id())) {
                return;
            }
            if (npc.getCurrentQi() < 10L) {
                return;
            }
            npc.deductQi(10L);
            return;
        }
        if (!src.is(DamageTypes.PLAYER_ATTACK)) {
            return;
        }
        entity = src.getDirectEntity();
        if (!(entity instanceof ServerPlayer attacker)) {
            return;
        }
        if (SpiritLockHandler.isEntityLocked((Entity) attacker)) {
            return;
        }
        if (target == attacker) {
            return;
        }
        CultivationData iData = CultivationCapability.get((Player) attacker).orElse(null);
        if (iData == null) {
            return;
        }
        if (!iData.isSpellEnabled(Spell.SOUL_MARK)) {
            return;
        }
        long actualCost = TechniqueBonusHelper.applySpellQiCostMultiplier((Player) attacker, Spell.SOUL_MARK, QI_COST_PER_HIT);
        if (iData.getCurrentQi() < actualCost) {
            return;
        }
        iData.setCurrentQi(iData.getCurrentQi() - actualCost);
        CapabilityEvents.syncToClient(attacker);
        ClientOnlyGlowPacket.send(attacker, java.util.List.of(target.getId()), GLOWING_DURATION_TICKS);
    }
}
