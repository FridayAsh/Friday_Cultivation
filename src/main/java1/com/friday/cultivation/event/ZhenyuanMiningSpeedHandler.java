package com.friday.cultivation.event;

import com.friday.cultivation.ZhenyuanBonusHelper;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * 真元挖掘加速处理器（严格照搬原模组 com.xiaoxiang.cultivation.event.ZhenyuanMiningSpeedHandler）
 */
@Mod.EventBusSubscriber(modid = "friday_cultivation")
public final class ZhenyuanMiningSpeedHandler {
    private ZhenyuanMiningSpeedHandler() {}

    @SubscribeEvent
    public static void onBreakSpeed(PlayerEvent.BreakSpeed event) {
        Player player = event.getEntity();
        if (player == null) return;
        float current = event.getNewSpeed();
        if (current <= 0.0f) return;
        double bonus = ZhenyuanBonusHelper.physiqueMiningSpeedBonus(player);
        if (bonus <= 0.0) return;
        event.setNewSpeed((float)((double)current * (1.0 + bonus)));
    }
}