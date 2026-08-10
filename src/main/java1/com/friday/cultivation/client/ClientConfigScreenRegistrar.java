package com.friday.cultivation.client;

import net.minecraft.client.gui.screens.Screen;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.ConfigScreenHandler;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;

/**
 * 客户端配置屏注册器 - 注册 ModConfig 入口到主菜单按钮。
 * 完全照搬原 mod: xiaoxiang.cultivation.client.ClientConfigScreenRegistrar
 */
@Mod.EventBusSubscriber(modid = "friday_cultivation", value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class ClientConfigScreenRegistrar {
    private ClientConfigScreenRegistrar() {
    }

    public static void register() {
        ModLoadingContext.get().registerExtensionPoint(ConfigScreenHandler.ConfigScreenFactory.class,
                () -> new ConfigScreenHandler.ConfigScreenFactory((mc, parent) -> create(parent)));
    }

    public static Screen create(Screen parent) {
        // 后续批次: 替换为 new XiaoxiangConfigScreen(parent)
        return parent;
    }
}
