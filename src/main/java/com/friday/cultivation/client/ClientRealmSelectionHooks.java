package com.friday.cultivation.client;

import com.friday.cultivation.client.screen.RealmSelectionScreen;
import net.minecraft.client.Minecraft;

/** Client-only adapter for opening the realm selector UI. */
public final class ClientRealmSelectionHooks {
    private ClientRealmSelectionHooks() {
    }

    public static void open() {
        Minecraft.getInstance().setScreen(new RealmSelectionScreen());
    }
}
