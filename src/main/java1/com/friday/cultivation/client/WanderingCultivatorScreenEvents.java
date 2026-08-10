package com.friday.cultivation.client;

import com.friday.cultivation.client.screen.WanderingCultivatorScreen;
import java.util.ArrayList;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * 散修屏幕事件 - 打开散修交易屏后移除第三方（非原版/Forge/本 mod）添加的外来控件。
 * 严格 1:1 复刻原 mod WanderingCultivatorScreenEvents。
 */
@Mod.EventBusSubscriber(modid = "friday_cultivation", bus = Mod.EventBusSubscriber.Bus.FORGE, value = {Dist.CLIENT})
public final class WanderingCultivatorScreenEvents {
    private WanderingCultivatorScreenEvents() {
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
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
