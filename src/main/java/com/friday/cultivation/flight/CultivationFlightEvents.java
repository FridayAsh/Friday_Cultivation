package com.friday.cultivation.flight;

import com.friday.cultivation.cultivation.CultivationCapability;
import com.friday.cultivation.cultivation.CultivationData;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * 修仙飞行服务端事件：每 tick 调用飞行判定器（御剑/灵气飞行授权 mayfly）。
 */
@Mod.EventBusSubscriber(modid="friday_cultivation")
public final class CultivationFlightEvents {
    private static final Logger LOGGER = LogManager.getLogger("CultivationFlight");
    private static int tickCounter = 0;

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
        if (++CultivationFlightEvents.tickCounter % 20 == 0) {
            CultivationData data = CultivationCapability.get((Player)player).orElse(null);
            CultivationFlightEvents.LOGGER.info("[FlightTick] sword={} qi={} qiToggled={} currentQi={}", CultivationFlightHandler.isSwordFlightActive(player), CultivationFlightHandler.canQiFlight(player), data != null ? data.isQiFlightToggled() : "no-data", data != null ? data.getCurrentQi() : -1L);
        }
        CultivationFlightHandler.tickFlight((ServerPlayer)player);
    }
}
