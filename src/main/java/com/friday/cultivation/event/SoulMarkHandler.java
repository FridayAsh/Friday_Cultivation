/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.server.level.ServerPlayer
 *  net.minecraft.world.damagesource.DamageTypes
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.entity.player.Player
 *  net.minecraftforge.event.entity.living.LivingAttackEvent
 *  net.minecraftforge.eventbus.api.EventPriority
 *  net.minecraftforge.eventbus.api.SubscribeEvent
 *  net.minecraftforge.fml.common.Mod$EventBusSubscriber
 */
package com.friday.cultivation.event;

import com.friday.cultivation.cultivation.CultivationCapability;
import com.friday.cultivation.cultivation.spell.Spell;
import com.friday.cultivation.cultivation.technique.TechniqueBonusHelper;
import com.friday.cultivation.entity.npc.WanderingCultivatorEntity;
import com.friday.cultivation.event.CapabilityEvents;
import com.friday.cultivation.event.SoulStateHandler;
import com.friday.cultivation.event.SpiritLockHandler;
import com.friday.cultivation.network.ClientOnlyGlowPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid="friday_cultivation")
public final class SoulMarkHandler {
    private static final long QI_COST_PER_HIT = 10L;
    private static final int GLOWING_DURATION_TICKS = 1200;

    private SoulMarkHandler() {
    }

    @SubscribeEvent(priority=EventPriority.LOWEST)
    public static void onLivingAttack(LivingAttackEvent event) {
        if (event.isCanceled()) {
            return;
        }
        LivingEntity target = event.getEntity();
        if (target == null || !target.isAlive()) {
            return;
        }
        if (!SoulStateHandler.canOrdinaryAffect(event.getSource().getEntity(), (Entity)target)) {
            return;
        }
        Entity entity = event.getSource().getEntity();
        if (entity instanceof WanderingCultivatorEntity) {
            WanderingCultivatorEntity npc = (WanderingCultivatorEntity)entity;
            if (npc.isNpcSoulState()) {
                return;
            }
            if (!event.getSource().is(DamageTypes.MOB_ATTACK)) {
                return;
            }
            if (target == npc || SpiritLockHandler.isEntityLocked((Entity)npc)) {
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
        if (!event.getSource().is(DamageTypes.PLAYER_ATTACK)) {
            return;
        }
        entity = event.getSource().getEntity();
        if (!(entity instanceof ServerPlayer)) {
            return;
        }
        ServerPlayer attacker = (ServerPlayer)entity;
        if (SpiritLockHandler.isEntityLocked((Entity)attacker)) {
            return;
        }
        if (target == attacker) {
            return;
        }
        CultivationCapability.get((Player)attacker).ifPresent(data -> {
            if (!data.isSpellEnabled(Spell.SOUL_MARK)) {
                return;
            }
            long actualCost = TechniqueBonusHelper.applySpellQiCostMultiplier((Player)attacker, Spell.SOUL_MARK, 10L);
            if (data.getCurrentQi() < actualCost) {
                return;
            }
            data.setCurrentQi(data.getCurrentQi() - actualCost);
            CapabilityEvents.syncToClient(attacker);
            ClientOnlyGlowPacket.send(attacker, target.getId(), 1200);
        });
    }
}

