/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.gui.screens.Screen
 */
package com.friday.cultivation.client;

import com.friday.cultivation.client.screen.IdentityDrawScreen;
import com.friday.cultivation.cultivation.draw.IdentityDrawDeck;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;

public final class ClientIdentityDrawHooks {
    private ClientIdentityDrawHooks() {
    }

    public static void openOrUpdate(IdentityDrawDeck deck) {
        ClientIdentityDrawHooks.openOrUpdate(deck, false);
    }

    public static void openOrUpdate(IdentityDrawDeck deck, boolean reconfigureMode) {
        Minecraft mc = Minecraft.getInstance();
        Screen screen = mc.screen;
        if (screen instanceof IdentityDrawScreen) {
            IdentityDrawScreen current = (IdentityDrawScreen)screen;
            current.updateDeck(deck, reconfigureMode);
        } else {
            mc.setScreen((Screen)new IdentityDrawScreen(deck, reconfigureMode));
        }
    }
}

