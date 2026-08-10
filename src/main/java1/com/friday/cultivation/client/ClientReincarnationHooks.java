package com.friday.cultivation.client;

import com.friday.cultivation.client.screen.ReincarnationScreen;
import net.minecraft.client.Minecraft;

/**
 * 客户端轮回钩子（严格照搬原模组 com.xiaoxiang.cultivation.client.ClientReincarnationHooks）。
 * <p>由 ReincarnationPacket 触发打开 {@link ReincarnationScreen}。</p>
 */
public final class ClientReincarnationHooks {
    private ClientReincarnationHooks() {
    }

    public static void open() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.getConnection() == null) {
            return;
        }
        mc.setScreen(new ReincarnationScreen());
    }
}
