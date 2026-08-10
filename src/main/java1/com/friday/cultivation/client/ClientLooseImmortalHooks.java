package com.friday.cultivation.client;

import com.friday.cultivation.client.screen.LooseImmortalChoiceScreen;
import net.minecraft.client.Minecraft;

/**
 * 客户端散仙选择钩子（严格照搬原模组 com.xiaoxiang.cultivation.client.ClientLooseImmortalHooks）。
 * <p>由 OpenLooseImmortalChoicePacket 触发打开 {@link LooseImmortalChoiceScreen}。</p>
 */
public final class ClientLooseImmortalHooks {
    private ClientLooseImmortalHooks() {
    }

    public static void openChoice() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.getConnection() == null) {
            return;
        }
        mc.setScreen(new LooseImmortalChoiceScreen());
    }
}
