package com.friday.cultivation.network;

import com.friday.cultivation.entity.npc.SpiritStonePayment;
import com.friday.cultivation.entity.npc.SundryPricing;
import com.friday.cultivation.entity.npc.WanderingCultivatorEntity;
import com.friday.cultivation.inventory.WanderingCultivatorMenu;
import com.friday.cultivation.util.CultivationRandomPools;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.PacketDistributor;

import java.util.function.Supplier;

/**
 * 执行杂货交易包 — 严格 1:1 复刻原 mod com.xiaoxiang.cultivation.network.ExecuteSundryTradePacket。
 * 玩家在散修交易菜单出售杂货，服务端校验喜爱/定价/资金后完成结算。
 */
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
            WanderingCultivatorMenu menu = (WanderingCultivatorMenu) patt1595$temp;
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
                player.displayClientMessage(Component.translatable("message.friday_cultivation.cultivator.sundry_not_accepted"), true);
                return;
            }
            if (!npc.getFavoriteItems().contains(offered.getItem())) {
                player.displayClientMessage(Component.translatable("message.friday_cultivation.cultivator.sundry_disliked"), true);
                return;
            }
            ItemStack price = SundryPricing.priceFor(offered);
            if (price.isEmpty()) {
                player.displayClientMessage(Component.translatable("message.friday_cultivation.cultivator.sundry_not_accepted"), true);
                return;
            }
            SimpleContainer npcInv = npc.getInventory();
            boolean paid = SpiritStonePayment.tryPay(npcInv, price.getItem(), price.getCount());
            if (!paid) {
                player.displayClientMessage(Component.translatable("message.friday_cultivation.cultivator.sundry_no_funds"), true);
                return;
            }
            npcInv.addItem(offered.copy());
            sellContainer.removeItem(0, offered.getCount());
            ItemStack priceCopy = price.copy();
            if (!player.getInventory().add(priceCopy)) {
                player.drop(priceCopy, false);
            }
            npc.regenerateOffers();
            player.displayClientMessage(Component.translatable("message.friday_cultivation.cultivator.trade_done", npc.getCultivatorName()), true);
            ModNetwork.CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), new SyncCultivatorInventoryPacket(menu.containerId, SyncCultivatorInventoryPacket.toTag(npc.getInventory())));
            ModNetwork.CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), new SyncCultivatorOffersPacket(menu.containerId, npc.getOffers()));
        });
        ctx.setPacketHandled(true);
    }
}
