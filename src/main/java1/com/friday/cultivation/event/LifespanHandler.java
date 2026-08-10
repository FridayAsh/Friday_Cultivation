package com.friday.cultivation.event;

import com.friday.cultivation.CultivationCapability;
import com.friday.cultivation.capability.CultivationData;
import com.friday.cultivation.LifespanHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * 寿元处理 handler - 每帧递增玩家骨龄；耗尽时进入 soul state。
 * 严格 1:1 复刻原 mod LifespanHandler。
 */
@Mod.EventBusSubscriber(modid = "friday_cultivation")
public final class LifespanHandler {
    private LifespanHandler() {
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        Player player = event.player;
        if (!(player instanceof ServerPlayer sp)) return;
        if (sp.isSleeping() || sp.isDeadOrDying()) return;
        CultivationData iData = CultivationCapability.get((Player) sp).orElse(null);
        if (iData == null) return;
        boolean visibleAgeChanged = advanceBoneAge(sp, iData, 1L);
        if (visibleAgeChanged || sp.tickCount % 200 == 0) {
            CapabilityEvents.syncToClient(sp);
        }
    }

    public static boolean advanceBoneAge(ServerPlayer player, CultivationData data, long ticks) {
        if (player == null || data == null || ticks <= 0L) return false;
        if (player.isSleeping() || player.isDeadOrDying()) return false;
        if (!data.hasChosenIdentity()) return false;
        int beforeDisplay = LifespanHelper.displayBoneAge(data);
        double inc = 1.0 * (double) ticks / 24000.0;
        data.addBoneAge(inc);
        if (LifespanHelper.isExhausted(data) && !data.isSoulState()) {
            player.displayClientMessage(Component.translatable("message.friday_cultivation.lifespan.exhausted_death"), true);
            SoulStateHandler.enterSoulState(player);
        }
        return LifespanHelper.displayBoneAge(data) != beforeDisplay;
    }
}
