/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.ChatFormatting
 *  net.minecraft.network.FriendlyByteBuf
 *  net.minecraft.network.chat.Component
 *  net.minecraft.server.level.ServerPlayer
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.inventory.AbstractContainerMenu
 *  net.minecraft.world.item.ItemStack
 *  net.minecraftforge.network.NetworkEvent$Context
 */
package com.friday.cultivation.network;

import com.friday.cultivation.block.refining.RefiningCoreBlockEntity;
import com.friday.cultivation.cultivation.CultivationCapability;
import com.friday.cultivation.cultivation.ItemTier;
import com.friday.cultivation.cultivation.refining.RefiningRank;
import com.friday.cultivation.cultivation.refining.RefiningRecipe;
import com.friday.cultivation.cultivation.refining.RefiningRecipes;
import com.friday.cultivation.cultivation.technique.TechniqueBonusHelper;
import com.friday.cultivation.inventory.RefiningMenu;
import java.util.function.Supplier;
import net.minecraft.ChatFormatting;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;

public class ExecuteRefiningPacket {
    private final String recipeId;

    public ExecuteRefiningPacket(String recipeId) {
        this.recipeId = recipeId;
    }

    public static void encode(ExecuteRefiningPacket msg, FriendlyByteBuf buf) {
        buf.writeUtf(msg.recipeId, 256);
    }

    public static ExecuteRefiningPacket decode(FriendlyByteBuf buf) {
        return new ExecuteRefiningPacket(buf.readUtf(256));
    }

    public static void handle(ExecuteRefiningPacket msg, Supplier<NetworkEvent.Context> ctxSup) {
        NetworkEvent.Context ctx = ctxSup.get();
        ctx.enqueueWork(() -> {
            ServerPlayer player = ctx.getSender();
            if (player == null) {
                return;
            }
            AbstractContainerMenu patt1649$temp = player.containerMenu;
            if (!(patt1649$temp instanceof RefiningMenu)) {
                return;
            }
            RefiningMenu menu = (RefiningMenu)patt1649$temp;
            RefiningCoreBlockEntity be = menu.getBlockEntity();
            if (be == null) {
                return;
            }
            if (be.isCrafting()) {
                player.displayClientMessage((Component)Component.translatable((String)"message.friday_cultivation.refining.already_crafting").withStyle(ChatFormatting.GOLD), true);
                return;
            }
            RefiningRecipe recipe = RefiningRecipes.byId(player.level(), msg.recipeId).orElse(null);
            if (recipe == null) {
                return;
            }
            RefiningRecipe matchedByMaterials = menu.findRecipeByMaterials();
            if (matchedByMaterials == null || !matchedByMaterials.id().equals(recipe.id())) {
                player.displayClientMessage((Component)Component.translatable((String)"message.friday_cultivation.refining.insufficient").withStyle(ChatFormatting.RED), true);
                return;
            }
            ItemStack outputSlot = menu.getIoContainer().getItem(6);
            if (!outputSlot.isEmpty()) {
                player.displayClientMessage((Component)Component.translatable((String)"message.friday_cultivation.refining.output_not_empty").withStyle(ChatFormatting.GOLD), true);
                return;
            }
            int itemCount = recipe.countPossibleItems(menu.getIoContainer(), be.getCurrentQi());
            if (itemCount <= 0) {
                player.displayClientMessage((Component)Component.translatable((String)"message.friday_cultivation.refining.insufficient").withStyle(ChatFormatting.RED), true);
                return;
            }
            CultivationCapability.get((Player)player).ifPresent(data -> {
                RefiningRank rank = data.getRefiningRank();
                ItemTier rolled = rank.rollItemResult(player.getRandom());
                double upChance = TechniqueBonusHelper.refiningTierUpChance((Player)player);
                if (upChance > 0.0 && (double)player.getRandom().nextFloat() < upChance) {
                    rolled = ExecuteRefiningPacket.upgradeItemTier(rolled);
                }
                int tierOrdinal = rolled == null ? -1 : rolled.ordinal();
                recipe.deductIngredients(menu.getIoContainer(), itemCount);
                be.deductQi((long)recipe.qiCostPerItem() * (long)itemCount);
                be.beginCrafting(recipe.id(), itemCount, tierOrdinal, player.getUUID());
                player.displayClientMessage((Component)Component.translatable((String)"message.friday_cultivation.refining.started", (Object[])new Object[]{itemCount, recipe.displayName()}).withStyle(ChatFormatting.GOLD), true);
            });
            menu.broadcastChanges();
        });
        ctx.setPacketHandled(true);
    }

    private static ItemTier upgradeItemTier(ItemTier t) {
        if (t == null) {
            return ItemTier.LOW;
        }
        return switch (t) {
            default -> throw new IncompatibleClassChangeError();
            case LOW -> ItemTier.MID;
            case MID -> ItemTier.HIGH;
            case HIGH -> ItemTier.SUPREME;
            case SUPREME -> ItemTier.SUPREME;
            case IMMORTAL -> ItemTier.IMMORTAL;
        };
    }
}

