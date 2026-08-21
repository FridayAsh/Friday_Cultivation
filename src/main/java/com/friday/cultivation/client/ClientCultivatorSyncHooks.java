package com.friday.cultivation.client;

import com.friday.cultivation.inventory.WanderingCultivatorMenu;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.item.trading.MerchantOffers;

/** 仅客户端处理 NPC 菜单同步包。 */
public final class ClientCultivatorSyncHooks {
    private ClientCultivatorSyncHooks() {
    }

    public static void applyOffers(int containerId, MerchantOffers offers) {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player != null && player.containerMenu.containerId == containerId
                && player.containerMenu instanceof WanderingCultivatorMenu menu) {
            menu.setOffers(offers);
        }
    }

    public static void applyInventory(int containerId, net.minecraft.nbt.ListTag inventoryTag) {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player != null && player.containerMenu.containerId == containerId
                && player.containerMenu instanceof WanderingCultivatorMenu menu) {
            menu.setNpcInventoryTag(inventoryTag);
        }
    }
}
