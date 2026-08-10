/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.player.LocalPlayer
 *  net.minecraft.network.FriendlyByteBuf
 *  net.minecraft.world.inventory.AbstractContainerMenu
 *  net.minecraft.world.item.trading.MerchantOffers
 *  net.minecraftforge.network.NetworkEvent$Context
 */
package com.friday.cultivation.network;

import com.friday.cultivation.inventory.WanderingCultivatorMenu;
import java.util.function.Supplier;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.trading.MerchantOffers;
import net.minecraftforge.network.NetworkEvent;

public class SyncCultivatorOffersPacket {
    private final int containerId;
    private final MerchantOffers offers;

    public SyncCultivatorOffersPacket(int containerId, MerchantOffers offers) {
        this.containerId = containerId;
        this.offers = offers;
    }

    public static void encode(SyncCultivatorOffersPacket msg, FriendlyByteBuf buf) {
        buf.writeVarInt(msg.containerId);
        buf.writeNbt(msg.offers.createTag());
    }

    public static SyncCultivatorOffersPacket decode(FriendlyByteBuf buf) {
        int id = buf.readVarInt();
        MerchantOffers offers = new MerchantOffers(buf.readNbt());
        return new SyncCultivatorOffersPacket(id, offers);
    }

    public static void handle(SyncCultivatorOffersPacket msg, Supplier<NetworkEvent.Context> ctxSup) {
        NetworkEvent.Context ctx = ctxSup.get();
        ctx.enqueueWork(() -> {
            AbstractContainerMenu patt1818$temp;
            LocalPlayer player = Minecraft.getInstance().player;
            if (player == null) {
                return;
            }
            if (player.containerMenu.containerId == msg.containerId && (patt1818$temp = player.containerMenu) instanceof WanderingCultivatorMenu) {
                WanderingCultivatorMenu menu = (WanderingCultivatorMenu)patt1818$temp;
                menu.setOffers(msg.offers);
            }
        });
        ctx.setPacketHandled(true);
    }
}

