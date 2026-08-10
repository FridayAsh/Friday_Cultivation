package com.friday.cultivation.network;

import com.friday.cultivation.entity.npc.WanderingCultivatorEntity;
import com.friday.cultivation.inventory.WanderingCultivatorMenu;
import com.friday.cultivation.item.ModItems;
import com.friday.cultivation.util.CultivationRandomPools;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.item.trading.MerchantOffers;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.PacketDistributor;

import java.util.function.Supplier;

/**
 * 执行散修交易包 — 严格 1:1 复刻原 mod com.xiaoxiang.cultivation.network.ExecuteCultivatorTradePacket。
 * 玩家在散修交易菜单购买商品，服务端校验价格/库存/知识交易后完成结算。
 */
public class ExecuteCultivatorTradePacket {
    private final int entityId;
    private final int offerIndex;

    public ExecuteCultivatorTradePacket(int entityId, int offerIndex) {
        this.entityId = entityId;
        this.offerIndex = offerIndex;
    }

    public static void encode(ExecuteCultivatorTradePacket msg, FriendlyByteBuf buf) {
        buf.writeVarInt(msg.entityId);
        buf.writeVarInt(msg.offerIndex);
    }

    public static ExecuteCultivatorTradePacket decode(FriendlyByteBuf buf) {
        return new ExecuteCultivatorTradePacket(buf.readVarInt(), buf.readVarInt());
    }

    public static void handle(ExecuteCultivatorTradePacket msg, Supplier<NetworkEvent.Context> ctxSup) {
        NetworkEvent.Context ctx = ctxSup.get();
        ctx.enqueueWork(() -> {
            boolean isSoulReaperTokenTrade;
            WanderingCultivatorMenu tradeMenu;
            WanderingCultivatorEntity npc;
            ServerPlayer player = ctx.getSender();
            if (player == null) {
                return;
            }
            Entity e = player.serverLevel().getEntity(msg.entityId);
            if (!(e instanceof WanderingCultivatorEntity) || !(npc = (WanderingCultivatorEntity) e).isAlive()) {
                return;
            }
            AbstractContainerMenu patt2648$temp = player.containerMenu;
            if (!(patt2648$temp instanceof WanderingCultivatorMenu) || (tradeMenu = (WanderingCultivatorMenu) patt2648$temp).getNpcEntityId() != npc.getId()) {
                return;
            }
            MerchantOffers offers = npc.getOffers();
            if (offers == null || msg.offerIndex < 0 || msg.offerIndex >= offers.size()) {
                return;
            }
            MerchantOffer offer = offers.get(msg.offerIndex);
            if (offer.isOutOfStock()) {
                return;
            }
            if (!ExecuteCultivatorTradePacket.playerHasItems(player.getInventory(), offer.getCostA()) || !offer.getCostB().isEmpty() && !ExecuteCultivatorTradePacket.playerHasItems(player.getInventory(), offer.getCostB())) {
                player.displayClientMessage(Component.translatable("message.friday_cultivation.cultivator.trade_no_qty"), true);
                return;
            }
            ItemStack resultTemplate = offer.getResult();
            if (CultivationRandomPools.isForbiddenNaturalLootStack(resultTemplate)) {
                return;
            }
            boolean isKnowledge = offer.getSpecialPriceDiff() >= 2147482647;
            boolean isHeldSwordTrade = !isKnowledge && npc.isHeldSwordTradeResult(resultTemplate);
            boolean npcHasResultInInventory = !isKnowledge && !isHeldSwordTrade && ExecuteCultivatorTradePacket.npcInventoryHasEnough(npc, resultTemplate);
            boolean bl = isSoulReaperTokenTrade = !isKnowledge && !npcHasResultInInventory && npc.isDifuReaper() && npc.isSoulReaperTokenTradeAvailable() && resultTemplate.is(ModItems.SOUL_REAPER_TOKEN.get());
            if (!(isKnowledge || isHeldSwordTrade || isSoulReaperTokenTrade || npcHasResultInInventory)) {
                player.displayClientMessage(Component.translatable("message.friday_cultivation.cultivator.item_sold_out", resultTemplate.getHoverName()), true);
                return;
            }
            ExecuteCultivatorTradePacket.consumePlayerItems(player.getInventory(), offer.getCostA());
            ExecuteCultivatorTradePacket.putIntoNpcInventory(npc, offer.getCostA());
            if (!offer.getCostB().isEmpty()) {
                ExecuteCultivatorTradePacket.consumePlayerItems(player.getInventory(), offer.getCostB());
                ExecuteCultivatorTradePacket.putIntoNpcInventory(npc, offer.getCostB());
            }
            ItemStack result = resultTemplate.copy();
            if (!player.getInventory().add(result)) {
                player.drop(result, false);
            }
            if (isHeldSwordTrade) {
                npc.removeHeldSwordIfSold(resultTemplate);
            } else if (isSoulReaperTokenTrade) {
                npc.consumeSoulReaperTokenTrade();
            } else if (!isKnowledge) {
                ExecuteCultivatorTradePacket.consumeFromNpcInventory(npc, resultTemplate);
            }
            offer.increaseUses();
            npc.regenerateOffers();
            player.displayClientMessage(Component.translatable("message.friday_cultivation.cultivator.trade_done", npc.getCultivatorName()), true);
            AbstractContainerMenu patt6396$temp = player.containerMenu;
            if (patt6396$temp instanceof WanderingCultivatorMenu) {
                WanderingCultivatorMenu menu = (WanderingCultivatorMenu) patt6396$temp;
                ModNetwork.CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), new SyncCultivatorInventoryPacket(menu.containerId, SyncCultivatorInventoryPacket.toTag(npc.getInventory())));
                ModNetwork.CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), new SyncCultivatorOffersPacket(menu.containerId, npc.getOffers()));
            }
        });
        ctx.setPacketHandled(true);
    }

    private static boolean playerHasItems(Inventory inv, ItemStack required) {
        if (required.isEmpty()) {
            return true;
        }
        int needed = required.getCount();
        for (int i = 0; i < inv.getContainerSize(); ++i) {
            ItemStack slot = inv.getItem(i);
            if (!ItemStack.isSameItemSameTags(slot, required) || (needed -= slot.getCount()) > 0) continue;
            return true;
        }
        return false;
    }

    private static void consumePlayerItems(Inventory inv, ItemStack required) {
        if (required.isEmpty()) {
            return;
        }
        int toRemove = required.getCount();
        for (int i = 0; i < inv.getContainerSize() && toRemove > 0; ++i) {
            ItemStack slot = inv.getItem(i);
            if (!ItemStack.isSameItemSameTags(slot, required)) continue;
            int takeFromSlot = Math.min(slot.getCount(), toRemove);
            slot.shrink(takeFromSlot);
            toRemove -= takeFromSlot;
        }
    }

    private static void putIntoNpcInventory(WanderingCultivatorEntity npc, ItemStack stack) {
        if (stack.isEmpty()) {
            return;
        }
        if (CultivationRandomPools.isForbiddenNaturalLootStack(stack)) {
            return;
        }
        npc.getInventory().addItem(stack.copy());
    }

    private static boolean npcInventoryHasEnough(WanderingCultivatorEntity npc, ItemStack required) {
        if (required.isEmpty()) {
            return true;
        }
        SimpleContainer inv = npc.getInventory();
        int needed = required.getCount();
        for (int i = 0; i < inv.getContainerSize(); ++i) {
            ItemStack slot = inv.getItem(i);
            if (!ItemStack.isSameItemSameTags(slot, required) || (needed -= slot.getCount()) > 0) continue;
            return true;
        }
        return false;
    }

    private static void consumeFromNpcInventory(WanderingCultivatorEntity npc, ItemStack result) {
        if (result.isEmpty()) {
            return;
        }
        SimpleContainer inv = npc.getInventory();
        int toRemove = result.getCount();
        for (int i = 0; i < inv.getContainerSize() && toRemove > 0; ++i) {
            ItemStack slot = inv.getItem(i);
            if (!ItemStack.isSameItemSameTags(slot, result)) continue;
            int takeFromSlot = Math.min(slot.getCount(), toRemove);
            slot.shrink(takeFromSlot);
            toRemove -= takeFromSlot;
        }
    }
}
