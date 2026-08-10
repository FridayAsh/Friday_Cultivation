package com.friday.cultivation.event;

import com.friday.cultivation.capability.CultivationData;
import com.friday.cultivation.CultivationCapability;
import com.friday.cultivation.qi.field.QiFieldRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.List;

/**
 * 灵气监测 tick 处理器 - 在玩家 HUD 上显示当前位置活跃的灵气场数量。
 * 完全照搬原 mod: xiaoxiang.cultivation.event.QiWatchTickHandler
 */
@Mod.EventBusSubscriber(modid = "friday_cultivation")
public final class QiWatchTickHandler {
    private static final int CHECK_INTERVAL = 20;

    private QiWatchTickHandler() {
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        Player player = event.player;
        if (!(player instanceof ServerPlayer sp)) return;
        if (sp.tickCount % CHECK_INTERVAL != 0) return;
        if (!(sp.level() instanceof ServerLevel level)) return;
        BlockPos pos = sp.blockPosition();
        List<com.friday.cultivation.qi.field.IQiFieldEffect> fields = QiFieldRegistry.of(level).activeFieldsAt(pos);
        CultivationCapability.get((Player) sp).ifPresent(data -> data.setNearbyQiFieldCount(Math.max(0, fields.size() & 0x7F)));
    }
}
