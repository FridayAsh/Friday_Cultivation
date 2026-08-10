/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.network.FriendlyByteBuf
 *  net.minecraft.network.chat.Component
 *  net.minecraft.server.level.ServerPlayer
 *  net.minecraft.world.SimpleContainer
 *  net.minecraft.world.inventory.AbstractContainerMenu
 *  net.minecraft.world.item.ItemStack
 *  net.minecraftforge.network.NetworkEvent$Context
 *  net.minecraftforge.network.PacketDistributor
 */
package com.friday.cultivation.network;

import com.friday.cultivation.entity.npc.SpiritStonePayment;
import com.friday.cultivation.entity.npc.SundryPricing;
import com.friday.cultivation.entity.npc.WanderingCultivatorEntity;
import com.friday.cultivation.inventory.WanderingCultivatorMenu;
import com.friday.cultivation.network.ModNetwork;
import com.friday.cultivation.network.SyncCultivatorInventoryPacket;
import com.friday.cultivation.network.SyncCultivatorOffersPacket;
import com.friday.cultivation.util.CultivationRandomPools;
import java.util.function.Supplier;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.PacketDistributor;

public class ExecuteSundryTradePacket {
    public static void encode(ExecuteSundryTradePacket msg, FriendlyByteBuf buf) {
    }

    public static ExecuteSundryTradePacket decode(FriendlyByteBuf buf) {
        return new ExecuteSundryTradePacket();
    }

    public static void handle(ExecuteSundryTradePacket msg, Supplier<NetworkEvent.Context> ctxSup) {
        NetworkEvent.Context ctx = ctxSup.get();
        ctx.enqueueWork(() -> {
            ServerPlayer player = ctx.getSender();
            if (player == null) {
                return;
            }
            AbstractContainerMenu patt1595$temp = player.containerMenu;
            if (!(patt1595$temp instanceof WanderingCultivatorMenu)) {
                return;
            }
            WanderingCultivatorMenu menu = (WanderingCultivatorMenu)patt1595$temp;
            WanderingCultivatorEntity npc = menu.getCultivator();
            if (npc == null || !npc.isAlive()) {
                return;
            }
            SimpleContainer sellContainer = menu.getSellContainer();
            ItemStack offered = sellContainer.getItem(0);
            if (offered.isEmpty()) {
                return;
            }
            if (CultivationRandomPools.isForbiddenNaturalLootStack(offered)) {
                player.displayClientMessage((Component)Component.translatable((String)"message.friday_cultivation.cultivator.sundry_not_accepted"), true);
                return;
            }
            if (!npc.getFavoriteItems().contains(offered.getItem())) {
                player.displayClientMessage((Component)Component.translatable((String)"message.friday_cultivation.cultivator.sundry_disliked"), true);
                return;
            }
            ItemStack price = SundryPricing.priceFor(offered);
            if (price.isEmpty()) {
                player.displayClientMessage((Component)Component.translatable((String)"message.friday_cultivation.cultivator.sundry_not_accepted"), true);
                return;
            }
            SimpleContainer npcInv = npc.getInventory();
            boolean paid = SpiritStonePayment.tryPay(npcInv, price.getItem(), price.getCount());
            if (!paid) {
                player.displayClientMessage((Component)Component.translatable((String)"message.friday_cultivation.cultivator.sundry_no_funds"), true);
                return;
            }
            npcInv.addItem(offered.copy());
            sellContainer.setItem(0, offered.copy());
            ItemStack priceCopy = price.copy();
            if (!player.getInventory().add(priceCopy)) {
                player.drop(priceCopy, false);
            }
            npc.regenerateOffers();
            player.displayClientMessage((Component)Component.translatable((String)"message.friday_cultivation.cultivator.trade_done", (Object[])new Object[]{npc.getCultivatorName()}), true);
            ModNetwork.CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), (Object)new SyncCultivatorInventoryPacket(menu.containerId, SyncCultivatorInventoryPacket.toTag(npc.getInventory())));
            ModNetwork.CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), (Object)new SyncCultivatorOffersPacket(menu.containerId, npc.getOffers()));
        });
        ctx.setPacketHandled(true);
    }
}

