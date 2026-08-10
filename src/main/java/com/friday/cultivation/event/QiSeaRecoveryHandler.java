/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.server.level.ServerPlayer
 *  net.minecraft.world.entity.player.Player
 *  net.minecraftforge.event.TickEvent$Phase
 *  net.minecraftforge.event.TickEvent$PlayerTickEvent
 *  net.minecraftforge.eventbus.api.SubscribeEvent
 *  net.minecraftforge.fml.common.Mod$EventBusSubscriber
 */
package com.friday.cultivation.event;

import com.friday.cultivation.cultivation.CultivationCapability;
import com.friday.cultivation.cultivation.GoldenCoreDaoBonusHelper;
import com.friday.cultivation.cultivation.QiElement;
import com.friday.cultivation.cultivation.qi.consumer.PlayerQiConsumer;
import com.friday.cultivation.event.CapabilityEvents;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid="friday_cultivation")
public final class QiSeaRecoveryHandler {
    private QiSeaRecoveryHandler() {
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
        if (player2.tickCount % 20 != 0) {
            return;
        }
        CultivationCapability.get((Player)player2).ifPresent(data -> {
            long recovery = PlayerQiConsumer.nominalQiRecoveryPerSecond((Player)player2, data, QiElement.PURE);
            if ((recovery = GoldenCoreDaoBonusHelper.applyQiRecoveryMultiplier(player2, data, recovery)) <= 0L) {
                return;
            }
            long before = data.getCurrentQi();
            if (before >= data.getMaxQi()) {
                return;
            }
            data.setCurrentQi(before + recovery);
            if (data.getCurrentQi() != before) {
                CapabilityEvents.syncToClient(player2);
            }
        });
    }
}

