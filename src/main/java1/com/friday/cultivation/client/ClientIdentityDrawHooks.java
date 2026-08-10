package com.friday.cultivation.client;

import com.friday.cultivation.client.screen.IdentityDrawScreen;
import com.friday.cultivation.identity.draw.IdentityDrawDeck;
import net.minecraft.client.Minecraft;

/**
 * 客户端灵根抽卡钩子（严格照搬原模组 com.xiaoxiang.cultivation.client.ClientIdentityDrawHooks）。
 * <p>由服务端 CycleGenderPacket / IdentityDrawPacket 调用。如果已有屏则更新 deck，否则新建。</p>
 */
public final class ClientIdentityDrawHooks {
    private ClientIdentityDrawHooks() {
    }

    public static void openOrUpdate(IdentityDrawDeck deck) {
        openOrUpdate(deck, false);
    }

    public static void openOrUpdate(IdentityDrawDeck deck, boolean reconfigureMode) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.getConnection() == null) {
            return;
        }
        if (mc.screen instanceof IdentityDrawScreen existing) {
            existing.updateDeck(deck, reconfigureMode);
        } else {
            mc.setScreen(new IdentityDrawScreen(deck, reconfigureMode));
        }
    }
}
