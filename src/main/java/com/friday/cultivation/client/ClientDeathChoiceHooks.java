/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.annotation.Nullable
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.gui.screens.Screen
 *  net.minecraft.network.chat.Component
 */
package com.friday.cultivation.client;

import com.friday.cultivation.client.DeathSequenceClientEffects;
import com.friday.cultivation.client.screen.DeathChoiceScreen;
import javax.annotation.Nullable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public final class ClientDeathChoiceHooks {
    private ClientDeathChoiceHooks() {
    }

    public static void open(@Nullable Component deathMessage) {
        Minecraft mc = Minecraft.getInstance();
        if (!(mc.screen instanceof DeathChoiceScreen)) {
            DeathSequenceClientEffects.clear();
            mc.setScreen((Screen)new DeathChoiceScreen(deathMessage));
        }
    }
}

