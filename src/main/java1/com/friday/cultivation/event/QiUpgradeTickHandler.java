package com.friday.cultivation.event;

import com.friday.cultivation.capability.CultivationData;
import com.friday.cultivation.CultivationCapability;
import com.friday.cultivation.qi.BlockQiSpec;
import com.friday.cultivation.qi.QiEcosystem;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * 灵气提升 tick 处理器 - 玩家在灵气浓郁群系中每 tick 尝试从脚下吸取灵气池。
 * 完全照搬原 mod: xiaoxiang.cultivation.event.QiUpgradeTickHandler
 */
@Mod.EventBusSubscriber(modid = "friday_cultivation")
public final class QiUpgradeTickHandler {
    private static final int CHECK_INTERVAL = 4;

    private QiUpgradeTickHandler() {
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        Player player = event.player;
        if (!(player instanceof ServerPlayer sp)) return;
        if (sp.tickCount % CHECK_INTERVAL != 0) return;
        if (player.isSpectator() || !player.isAlive()) return;
        CultivationData data = CultivationCapability.get((Player) sp).orElse(null);
        if (data == null) return;
        if (data.getCurrentQi() >= data.getMaxQi()) return;
        if (!(sp.level() instanceof ServerLevel level)) return;
        BlockPos pos = sp.blockPosition();
        int radius = 2;
        int absorbed = 0;
        for (int dx = -radius; dx <= radius && absorbed < 20; ++dx) {
            for (int dy = -1; dy <= 2 && absorbed < 20; ++dy) {
                for (int dz = -radius; dz <= radius && absorbed < 20; ++dz) {
                    BlockPos target = pos.offset(dx, dy, dz);
                    BlockState state = level.getBlockState(target);
                    BlockQiSpec spec = QiEcosystem.specOf(level, target);
                    if (spec == null) continue;
                    int peek = QiEcosystem.peekBlock(level, target);
                    if (peek <= 0) continue;
                    int drained = QiEcosystem.tryDrainBlock(level, target, 1);
                    if (drained > 0) {
                        absorbed += drained;
                    }
                }
            }
        }
        if (absorbed > 0) {
            data.setCurrentQi(data.getCurrentQi() + absorbed);
            com.friday.cultivation.event.CapabilityEvents.syncToClient(sp);
        }
    }
}
