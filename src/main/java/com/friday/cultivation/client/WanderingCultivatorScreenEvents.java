/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.gui.components.events.GuiEventListener
 *  net.minecraftforge.api.distmarker.Dist
 *  net.minecraftforge.client.event.ScreenEvent$Init$Post
 *  net.minecraftforge.eventbus.api.EventPriority
 *  net.minecraftforge.eventbus.api.SubscribeEvent
 *  net.minecraftforge.fml.common.Mod$EventBusSubscriber
 *  net.minecraftforge.fml.common.Mod$EventBusSubscriber$Bus
 */
package com.friday.cultivation.client;

import com.friday.cultivation.client.screen.WanderingCultivatorScreen;
import java.util.ArrayList;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid="friday_cultivation", bus=Mod.EventBusSubscriber.Bus.FORGE, value={Dist.CLIENT})
public final class WanderingCultivatorScreenEvents {
    private WanderingCultivatorScreenEvents() {
    }

    @SubscribeEvent(priority=EventPriority.LOWEST)
    public static void onScreenInitPost(ScreenEvent.Init.Post event) {
        if (!(event.getScreen() instanceof WanderingCultivatorScreen)) {
            return;
        }
        ArrayList<GuiEventListener> foreign = new ArrayList<GuiEventListener>();
        for (GuiEventListener listener : event.getListenersList()) {
            if (!WanderingCultivatorScreenEvents.isForeignWidget(listener)) continue;
            foreign.add(listener);
        }
        for (GuiEventListener listener : foreign) {
            event.removeListener(listener);
        }
    }

    private static boolean isForeignWidget(GuiEventListener listener) {
        String cls = listener.getClass().getName();
        return !cls.startsWith("net.minecraft.") && !cls.startsWith("net.minecraftforge.") && !cls.startsWith("com.friday.");
    }
}

