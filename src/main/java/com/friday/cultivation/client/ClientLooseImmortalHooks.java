/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.gui.screens.Screen
 */
package com.friday.cultivation.client;

import com.friday.cultivation.client.screen.LooseImmortalChoiceScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;

public final class ClientLooseImmortalHooks {
    private ClientLooseImmortalHooks() {
    }

    public static void openChoice() {
        Minecraft mc = Minecraft.getInstance();
        if (!(mc.screen instanceof LooseImmortalChoiceScreen)) {
            mc.setScreen((Screen)new LooseImmortalChoiceScreen());
        }
    }
}

