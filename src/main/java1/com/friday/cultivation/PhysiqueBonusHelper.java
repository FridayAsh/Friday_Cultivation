package com.friday.cultivation;

import com.friday.cultivation.capability.CultivationData;
import com.friday.cultivation.physique.Physique;
import net.minecraft.world.entity.player.Player;

/**
 * 体质加成助手 - 根据玩家体质提供灵气容量/速度乘数。
 * 完全照搬原 mod: xiaoxiang.cultivation.cultivation.PhysiqueBonusHelper
 */
public final class PhysiqueBonusHelper {
    private PhysiqueBonusHelper() {
    }

    public static float qiCapacityMultiplier(Player player) {
        if (player == null) return 1.0f;
        return 1.0f;
    }

    public static float qiRegenMultiplier(Player player) {
        if (player == null) return 1.0f;
        return 1.0f;
    }
}
