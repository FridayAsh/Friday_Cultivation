package com.friday.cultivation.network;

import com.friday.cultivation.CultivationCapability;
import com.friday.cultivation.alchemy.AlchemyRank;
import com.friday.cultivation.alchemy.AlchemyRecipe;
import com.friday.cultivation.alchemy.AlchemyRecipes;
import com.friday.cultivation.alchemy.PillTier;
import com.friday.cultivation.block.alchemy.AlchemyCoreBlockEntity;
import com.friday.cultivation.inventory.AlchemyMenu;
import com.friday.cultivation.physique.PhysiqueBonusHelper;
import com.friday.cultivation.technique.TechniqueBonusHelper;
import java.util.function.Supplier;
import net.minecraft.ChatFormatting;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;

public class ExecuteAlchemyPacket {
    private final String recipeId;

    public ExecuteAlchemyPacket(String recipeId) {
        this.recipeId = recipeId;
    }

    public static void encode(ExecuteAlchemyPacket msg, FriendlyByteBuf buf) {
        buf.writeUtf(msg.recipeId, 256);
    }

    public static ExecuteAlchemyPacket decode(FriendlyByteBuf buf) {
        return new ExecuteAlchemyPacket(buf.readUtf(256));
    }

    public static void handle(ExecuteAlchemyPacket msg, Supplier<NetworkEvent.Context> ctxSup) {
        NetworkEvent.Context ctx = ctxSup.get();
        ctx.enqueueWork(() -> {
            ServerPlayer player = ctx.getSender();
            if (player == null) {
                return;
            }
            AbstractContainerMenu currentMenu = player.containerMenu;
            if (!(currentMenu instanceof AlchemyMenu)) {
                return;
            }
            AlchemyMenu menu = (AlchemyMenu)currentMenu;
            AlchemyCoreBlockEntity be = menu.getBlockEntity();
            if (be == null) {
                return;
            }
            if (be.isCrafting()) {
                player.sendSystemMessage(Component.translatable("message.friday_cultivation.alchemy.already_crafting").withStyle(ChatFormatting.GOLD), true);
                return;
            }
            AlchemyRecipe recipe = AlchemyRecipes.byId(player.level(), msg.recipeId).orElse(null);
            if (recipe == null) {
                return;
            }
            AlchemyRecipe matchedByMaterials = menu.findRecipeByMaterials();
            if (matchedByMaterials == null || !matchedByMaterials.id().equals(recipe.id())) {
                player.sendSystemMessage(Component.translatable("message.friday_cultivation.alchemy.insufficient").withStyle(ChatFormatting.RED), true);
                return;
            }
            ItemStack outputSlot = menu.getIoContainer().getItem(6);
            if (!outputSlot.isEmpty()) {
                player.sendSystemMessage(Component.translatable("message.friday_cultivation.alchemy.output_not_empty").withStyle(ChatFormatting.GOLD), true);
                return;
            }
            double qiCostMultiplier = PhysiqueBonusHelper.alchemyQiCostMultiplier(player);
            long effectiveQi = qiCostMultiplier <= 0.0 ? be.getCurrentQi() : (long)Math.floor((double)be.getCurrentQi() / qiCostMultiplier);
            int pillCount = recipe.countPossiblePills(menu.getIoContainer(), effectiveQi);
            if (pillCount <= 0) {
                player.sendSystemMessage(Component.translatable("message.friday_cultivation.alchemy.insufficient").withStyle(ChatFormatting.RED), true);
                return;
            }
            CultivationCapability.get(player).ifPresent(data -> {
                AlchemyRank rank = data.getAlchemyRank();
                PillTier rolled = rank.rollPillResult(player.getRandom());
                double upChance = TechniqueBonusHelper.alchemyTierUpChance(player);
                if (upChance > 0.0 && player.getRandom().nextFloat() < upChance) {
                    rolled = ExecuteAlchemyPacket.upgradePillTier(rolled);
                }
                double physiqueSuccessBonus = PhysiqueBonusHelper.alchemySuccessChanceBonus(player);
                if (rolled == null && physiqueSuccessBonus > 0.0 && player.getRandom().nextDouble() < physiqueSuccessBonus) {
                    rolled = PillTier.LOW;
                }
                int tierOrdinal = rolled == null ? -1 : rolled.ordinal();
                recipe.deductIngredients(menu.getIoContainer(), pillCount);
                long qiCost = Math.max(0L, (long)Math.ceil((double)(recipe.qiCostPerPill() * pillCount) * qiCostMultiplier));
                be.deductQi(qiCost);
                be.beginCrafting(recipe.id(), pillCount, tierOrdinal, player.getUUID());
                player.sendSystemMessage(Component.translatable("message.friday_cultivation.alchemy.started", pillCount, recipe.displayName()).withStyle(ChatFormatting.GOLD), true);
            });
            menu.broadcastChanges();
        });
        ctx.setPacketHandled(true);
    }

    private static PillTier upgradePillTier(PillTier t) {
        if (t == null) {
            return PillTier.LOW;
        }
        int idx = t.ordinal();
        PillTier[] vals = PillTier.values();
        return idx + 1 < vals.length ? vals[idx + 1] : t;
    }
}
