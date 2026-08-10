package com.friday.cultivation.event;

import com.friday.cultivation.capability.CultivationData;
import com.friday.cultivation.CultivationCapability;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * 气海自动恢复处理器 - 每秒根据玩家基础回复量自动补充气海灵气。
 * 完全照搬原 mod: xiaoxiang.cultivation.event.QiSeaRecoveryHandler
 */
@Mod.EventBusSubscriber(modid = "friday_cultivation")
public final class QiSeaRecoveryHandler {
    private static final int CHECK_INTERVAL = 20;
    private static final float REBIRTH_QI_FRACTION = 0.5f;

    private QiSeaRecoveryHandler() {
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        Player player = event.player;
        if (!(player instanceof ServerPlayer sp)) return;
        if (sp.tickCount % CHECK_INTERVAL != 0) return;
        CultivationData data = CultivationCapability.get((Player) sp).orElse(null);
        if (data == null) return;
        long max = data.getMaxQi();
        if (max <= 0L) return;
        long cur = data.getCurrentQi();
        if (cur >= max) return;
        long baseRecovery = Math.max(1L, max / 120L);
        long recovery = baseRecovery;
        if (TimeStasisHandler.isEntityStopped(sp)) recovery = recovery * 2;
        if (SpiritLockHandler.isEntityLocked((net.minecraft.world.entity.LivingEntity) sp)) recovery = recovery * 3;
        long newCur = Math.min(max, cur + recovery);
        data.setCurrentQi(newCur);
        CapabilityEvents.syncToClient(sp);
    }

    public static long computeRebirthQiFraction(CultivationData data) {
        if (data == null) return 0L;
        return (long) (data.getMaxQi() * REBIRTH_QI_FRACTION);
    }
}
