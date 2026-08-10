package com.friday.cultivation.network;

import com.friday.cultivation.refining.RefiningRecipe;
import com.friday.cultivation.refining.RefiningRecipes;
import com.friday.cultivation.ItemTier;
import com.friday.cultivation.block.refining.RefiningCoreBlockEntity;
import com.friday.cultivation.CultivationCapability;
import com.friday.cultivation.capability.CultivationData;
import com.friday.cultivation.event.CapabilityEvents;
import com.friday.cultivation.inventory.RefiningMenu;
import com.friday.cultivation.physique.PhysiqueBonusHelper;
import com.friday.cultivation.technique.TechniqueBonusHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * 炼器执行请求包 — 客户端 → 服务端
 * 完整复刻原模组 ExecuteRefiningPacket。
 * 客户端在 RefiningScreen 点击炼器按钮时发送，服务端校验后启动炼器进程。
 */
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
            if (player == null) return;
            AbstractContainerMenu menu = player.containerMenu;
            if (!(menu instanceof RefiningMenu)) return;
            RefiningMenu refiningMenu = (RefiningMenu) menu;
            RefiningCoreBlockEntity be = refiningMenu.getBlockEntity();
            if (be == null) return;
            if (be.isCrafting()) {
                player.displayClientMessage(Component.translatable("message.friday_cultivation.refining.already_crafting").withStyle(ChatFormatting.GOLD), true);
                return;
            }
            RefiningRecipe recipe = RefiningRecipes.byId(player.level(), msg.recipeId).orElse(null);
            if (recipe == null) return;
            RefiningRecipe matchedByMaterials = refiningMenu.findRecipeByMaterials();
            if (matchedByMaterials == null || !matchedByMaterials.id().equals(recipe.id())) {
                player.displayClientMessage(Component.translatable("message.friday_cultivation.refining.insufficient").withStyle(ChatFormatting.RED), true);
                return;
            }
            ItemStack outputSlot = refiningMenu.getIoContainer().getItem(6);
            if (!outputSlot.isEmpty()) {
                player.displayClientMessage(Component.translatable("message.friday_cultivation.refining.output_not_empty").withStyle(ChatFormatting.GOLD), true);
                return;
            }
            double qiCostMultiplier = 1.0; // 体质炼器加成（项目未实现，固定1.0）
            long effectiveQi = qiCostMultiplier <= 0.0 ? be.getCurrentQi() : (long) Math.floor((double) be.getCurrentQi() / qiCostMultiplier);
            int pillCount = recipe.countPossibleItems(refiningMenu.getIoContainer(), effectiveQi);
            if (pillCount <= 0) {
                player.displayClientMessage(Component.translatable("message.friday_cultivation.refining.insufficient").withStyle(ChatFormatting.RED), true);
                return;
            }
            CultivationData ic = CultivationCapability.get(player).orElse(null);
            if (ic != null) {
                com.friday.cultivation.refining.RefiningRank rank = ic.getRefiningRank();
                ItemTier rolled = rank.rollItemResult(player.getRandom());
                double upChance = TechniqueBonusHelper.refiningTierUpChance((Player) player);
                if (upChance > 0.0 && player.getRandom().nextDouble() < upChance) {
                    rolled = upgradeItemTier(rolled);
                }
                // 体质炼器成功加成（项目未实现，跳过）
                int tierOrdinal = rolled == null ? -1 : rolled.ordinal();
                recipe.deductIngredients(refiningMenu.getIoContainer(), pillCount);
                long qiCost = Math.max(0L, (long) Math.ceil((double) (recipe.qiCostPerItem() * pillCount) * qiCostMultiplier));
                be.deductQi(qiCost);
                be.beginCrafting(recipe.id(), pillCount, tierOrdinal, player.getUUID());
                player.displayClientMessage(Component.translatable("message.friday_cultivation.refining.started", pillCount, recipe.displayName()).withStyle(ChatFormatting.GOLD), true);
            }
            refiningMenu.broadcastChanges();
        });
        ctx.setPacketHandled(true);
    }

    private static ItemTier upgradeItemTier(ItemTier t) {
        if (t == null) return ItemTier.LOW;
        int idx = t.ordinal();
        ItemTier[] vals = ItemTier.values();
        return idx + 1 < vals.length ? vals[idx + 1] : t;
    }
}
