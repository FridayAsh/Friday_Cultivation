package com.friday.cultivation.client;

import com.friday.cultivation.client.screen.SoulReaperTargetScreen;
import com.friday.cultivation.network.SoulReaperTargetEntry;
import net.minecraft.client.Minecraft;

import java.util.List;

/**
 * 客户端勾魂目标钩子（严格照搬原模组 com.xiaoxiang.cultivation.client.ClientSoulReaperTargetHooks）。
 * <p>由 SetSoulReaperTargetsPacket 触发打开 {@link SoulReaperTargetScreen}。</p>
 */
public final class ClientSoulReaperTargetHooks {
    private ClientSoulReaperTargetHooks() {
    }

    public static void open(List<SoulReaperTargetEntry> targets) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.getConnection() == null) {
            return;
        }
        if (mc.screen instanceof SoulReaperTargetScreen existing) {
            existing.updateTargets(targets);
        } else {
            mc.setScreen(new SoulReaperTargetScreen(targets));
        }
    }
}
