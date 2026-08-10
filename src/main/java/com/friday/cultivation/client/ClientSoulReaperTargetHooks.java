/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.gui.screens.Screen
 */
package com.friday.cultivation.client;

import com.friday.cultivation.client.screen.SoulReaperTargetScreen;
import com.friday.cultivation.network.SoulReaperTargetEntry;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;

public final class ClientSoulReaperTargetHooks {
    private ClientSoulReaperTargetHooks() {
    }

    public static void open(List<SoulReaperTargetEntry> targets) {
        Minecraft mc = Minecraft.getInstance();
        Screen screen = mc.screen;
        if (screen instanceof SoulReaperTargetScreen) {
            SoulReaperTargetScreen current = (SoulReaperTargetScreen)screen;
            current.updateTargets(targets);
        } else {
            mc.setScreen((Screen)new SoulReaperTargetScreen(targets));
        }
    }
}

