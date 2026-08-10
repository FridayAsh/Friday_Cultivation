/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.gui.screens.Screen
 *  net.minecraft.nbt.CompoundTag
 */
package com.friday.cultivation.client;

import com.friday.cultivation.client.screen.SectJoinDialogueScreen;
import com.friday.cultivation.client.screen.SectScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.nbt.CompoundTag;

public final class SectClientHooks {
    private SectClientHooks() {
    }

    public static void open(CompoundTag snapshot) {
        Minecraft.getInstance().setScreen((Screen)new SectScreen(snapshot));
    }

    public static void openJoinDialogue(int targetEntityId, String sectName, String npcName, CompoundTag snapshot) {
        Minecraft.getInstance().setScreen((Screen)new SectJoinDialogueScreen(targetEntityId, sectName, npcName, snapshot));
    }
}

