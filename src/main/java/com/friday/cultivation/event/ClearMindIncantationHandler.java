/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.server.level.ServerPlayer
 *  net.minecraft.world.effect.MobEffect
 *  net.minecraft.world.effect.MobEffectCategory
 *  net.minecraft.world.effect.MobEffectInstance
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.player.Player
 *  net.minecraftforge.event.TickEvent$Phase
 *  net.minecraftforge.event.TickEvent$PlayerTickEvent
 *  net.minecraftforge.eventbus.api.SubscribeEvent
 *  net.minecraftforge.fml.common.Mod$EventBusSubscriber
 */
package com.friday.cultivation.event;

import com.friday.cultivation.cultivation.CultivationCapability;
import com.friday.cultivation.cultivation.CultivationData;
import com.friday.cultivation.cultivation.spell.Spell;
import com.friday.cultivation.cultivation.technique.TechniqueBonusHelper;
import com.friday.cultivation.event.CapabilityEvents;
import com.friday.cultivation.event.SpiritLockHandler;
import java.util.ArrayList;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid="friday_cultivation")
public final class ClearMindIncantationHandler {
    public static final long QI_PER_EFFECT = 100L;
    private static final int CHECK_INTERVAL_TICKS = 20;

    private ClearMindIncantationHandler() {
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }
        Player player = event.player;
        if (!(player instanceof ServerPlayer)) {
            return;
        }
        ServerPlayer sp = (ServerPlayer)player;
        if (sp.tickCount % 20 != 0) {
            return;
        }
        CultivationData data = CultivationCapability.get((Player)sp).orElse(null);
        if (data == null) {
            return;
        }
        if (SpiritLockHandler.isEntityLocked((Entity)sp)) {
            return;
        }
        if (!data.isSpellEnabled(Spell.CLEAR_MIND_INCANTATION)) {
            return;
        }
        ArrayList<MobEffect> harmful = new ArrayList<MobEffect>();
        for (MobEffectInstance eff : sp.getActiveEffects()) {
            if (eff.getEffect().getCategory() != MobEffectCategory.HARMFUL) continue;
            harmful.add(eff.getEffect());
        }
        if (harmful.isEmpty()) {
            return;
        }
        long curQi = data.getCurrentQi();
        boolean changed = false;
        for (MobEffect e : harmful) {
            long cost = TechniqueBonusHelper.applySpellQiCostMultiplier((Player)sp, Spell.CLEAR_MIND_INCANTATION, 100L);
            if (curQi < cost) break;
            curQi -= cost;
            sp.removeEffect(e);
            changed = true;
        }
        if (changed) {
            data.setCurrentQi(curQi);
            CapabilityEvents.syncToClient(sp);
        }
    }
}

