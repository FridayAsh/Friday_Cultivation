package com.friday.cultivation.client;

import com.friday.cultivation.client.screen.DeathChoiceScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

/**
 * 客户端死亡选项钩子（严格照搬原模组 com.xiaoxiang.cultivation.client.ClientDeathChoiceHooks）。
 */
public final class ClientDeathChoiceHooks {
    private ClientDeathChoiceHooks() {
    }

    public static void open(@Nullable Component deathMessage) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.getConnection() == null) {
            return;
        }
        mc.setScreen(new DeathChoiceScreen(deathMessage));
    }
}
