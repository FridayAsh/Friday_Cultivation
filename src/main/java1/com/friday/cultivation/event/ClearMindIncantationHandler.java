package com.friday.cultivation.event;

import com.friday.cultivation.CultivationCapability;
import com.friday.cultivation.capability.CultivationData;
import com.friday.cultivation.spell.Spell;
import com.friday.cultivation.technique.TechniqueBonusHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.ArrayList;

/**
 * 清心神咒事件 handler - 每 20 tick 检测玩家身上有害效果，
 * 主动移除（消耗灵气 100/效果）。严格 1:1 复刻原 mod ClearMindIncantationHandler。
 */
@Mod.EventBusSubscriber(modid = "friday_cultivation")
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
        ServerPlayer sp = (ServerPlayer) player;
        if (sp.tickCount % CHECK_INTERVAL_TICKS != 0) {
            return;
        }
        CultivationData data;
        CultivationData iData = CultivationCapability.get((Player) sp).orElse(null);
        if (!(iData instanceof CultivationData) || (iData = (CultivationData) iData) == null) {
            return;
        }
        if (SpiritLockHandler.isEntityLocked((net.minecraft.world.entity.Entity) sp)) {
            return;
        }
        if (!iData.isSpellEnabled(Spell.CLEAR_MIND_INCANTATION)) {
            return;
        }
        ArrayList<MobEffect> harmful = new ArrayList<>();
        for (MobEffectInstance eff : sp.getActiveEffects()) {
            if (eff.getEffect().getCategory() != MobEffectCategory.HARMFUL) continue;
            harmful.add(eff.getEffect());
        }
        if (harmful.isEmpty()) {
            return;
        }
        long curQi = iData.getCurrentQi();
        boolean changed = false;
        for (MobEffect e : harmful) {
            long cost = TechniqueBonusHelper.applySpellQiCostMultiplier((Player) sp, Spell.CLEAR_MIND_INCANTATION, QI_PER_EFFECT);
            if (curQi < cost) break;
            curQi -= cost;
            sp.removeEffect(e);
            changed = true;
        }
        if (changed) {
            iData.setCurrentQi(curQi);
            CapabilityEvents.syncToClient(sp);
        }
    }
}
