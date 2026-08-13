package com.friday.cultivation.flight;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * 修仙飞行服务端事件：每 tick 调用飞行判定器（御剑/灵气飞行授权 mayfly）。
 */
@Mod.EventBusSubscriber(modid="friday_cultivation")
public final class CultivationFlightEvents {
    private CultivationFlightEvents() {
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
        CultivationFlightHandler.tickFlight((ServerPlayer)player);
    }
}
