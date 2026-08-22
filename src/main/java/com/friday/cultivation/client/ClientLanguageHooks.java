package com.friday.cultivation.client;

import net.minecraft.client.Minecraft;

/** 仅客户端读取语言环境的 Seam。 */
public final class ClientLanguageHooks {
    private ClientLanguageHooks() {
    }

    public static String selectedLanguage() {
        return Minecraft.getInstance().getLanguageManager().getSelected();
    }
}
