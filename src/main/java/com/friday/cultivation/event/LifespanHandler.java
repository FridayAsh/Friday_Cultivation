/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.network.chat.Component
 *  net.minecraft.server.level.ServerPlayer
 *  net.minecraft.world.entity.player.Player
 *  net.minecraftforge.event.TickEvent$Phase
 *  net.minecraftforge.event.TickEvent$PlayerTickEvent
 *  net.minecraftforge.eventbus.api.SubscribeEvent
 *  net.minecraftforge.fml.common.Mod$EventBusSubscriber
 */
package com.friday.cultivation.event;

import com.friday.cultivation.cultivation.CultivationCapability;
import com.friday.cultivation.cultivation.CultivationData;
import com.friday.cultivation.cultivation.LifespanHelper;
import com.friday.cultivation.event.CapabilityEvents;
import com.friday.cultivation.event.SoulStateHandler;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid="friday_cultivation")
public final class LifespanHandler {
    private LifespanHandler() {
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
        ServerPlayer player2 = (ServerPlayer)player;
        if (player2.isCreative() || player2.isSpectator()) {
            return;
        }
        CultivationData data = CultivationCapability.get((Player)player2).orElse(null);
        if (data == null) {
            return;
        }
        boolean visibleAgeChanged = LifespanHandler.advanceBoneAge(player2, data, 1L);
        if (visibleAgeChanged || player2.tickCount % 200 == 0) {
            CapabilityEvents.syncToClient(player2);
        }
    }

    public static boolean advanceBoneAge(ServerPlayer player, CultivationData data, long ticks) {
        if (player == null || data == null || ticks <= 0L) {
            return false;
        }
        if (player.isCreative() || player.isSpectator()) {
            return false;
        }
        if (!data.hasChosenIdentity()) {
            return false;
        }
        int beforeDisplay = LifespanHelper.displayBoneAge(data);
        double inc = 1.0 * (double)ticks / 24000.0;
        data.addBoneAge(inc);
        if (LifespanHelper.isExhausted(data) && !data.isSoulState()) {
            player.sendSystemMessage((Component)Component.translatable((String)"message.friday_cultivation.lifespan.exhausted_death"));
            SoulStateHandler.enterSoulState(player);
        }
        return LifespanHelper.displayBoneAge(data) != beforeDisplay;
    }
}

