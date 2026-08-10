/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.gui.screens.Screen
 *  net.minecraftforge.client.ConfigScreenHandler$ConfigScreenFactory
 *  net.minecraftforge.fml.ModLoadingContext
 */
package com.friday.cultivation.client;

import com.friday.cultivation.client.screen.XiaoxiangConfigScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraftforge.client.ConfigScreenHandler;
import net.minecraftforge.fml.ModLoadingContext;

public final class ClientConfigScreenRegistrar {
    private ClientConfigScreenRegistrar() {
    }

    public static void register() {
        ModLoadingContext.get().registerExtensionPoint(ConfigScreenHandler.ConfigScreenFactory.class, () -> new ConfigScreenHandler.ConfigScreenFactory((minecraft, parent) -> new XiaoxiangConfigScreen((Screen)parent)));
    }
}

