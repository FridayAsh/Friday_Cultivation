/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.gui.screens.Screen
 */
package com.friday.cultivation.client;

import com.friday.cultivation.client.screen.ReincarnationScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;

public final class ClientReincarnationHooks {
    private ClientReincarnationHooks() {
    }

    public static void open() {
        Minecraft mc = Minecraft.getInstance();
        if (!(mc.screen instanceof ReincarnationScreen)) {
            mc.setScreen((Screen)new ReincarnationScreen());
        }
    }
}

