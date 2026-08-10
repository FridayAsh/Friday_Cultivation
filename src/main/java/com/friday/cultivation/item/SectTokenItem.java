/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.ChatFormatting
 *  net.minecraft.core.BlockPos
 *  net.minecraft.nbt.CompoundTag
 *  net.minecraft.network.chat.Component
 *  net.minecraft.world.InteractionResult
 *  net.minecraft.world.SimpleContainer
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.player.Inventory
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.item.Item
 *  net.minecraft.world.item.Item$Properties
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.item.TooltipFlag
 *  net.minecraft.world.item.context.UseOnContext
 *  net.minecraft.world.level.ItemLike
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.level.block.entity.BlockEntity
 *  net.minecraft.world.level.block.state.BlockState
 *  org.jetbrains.annotations.NotNull
 *  org.jetbrains.annotations.Nullable
 */
package com.friday.cultivation.item;

import com.friday.cultivation.block.formation.FormationCorePlateBlock;
import com.friday.cultivation.block.formation.FormationCorePlateBlockEntity;
import com.friday.cultivation.entity.npc.WanderingCultivatorEntity;
import com.friday.cultivation.registry.ModItems;
import com.friday.cultivation.util.TooltipUtils;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class SectTokenItem
extends Item {
    public static final String TAG_CORE_POS = "linkedCorePos";
    public static final String TAG_CORE_DIM = "linkedCoreDim";
    public static final String TAG_CORE_NAME = "linkedCoreName";
    public static final String TAG_OWNER_NAME = "ownerName";
    public static final String TAG_TEMPORARY = "temporarySectToken";

    public SectTokenItem(Item.Properties properties) {
        super(properties);
    }

    @NotNull
    public InteractionResult playSound(@NotNull UseOnContext ctx) {
        BlockPos pos;
        Level level = ctx.getLevel();
        BlockState state = level.getBlockState(pos = ctx.getClickedPos());
        if (!(state.getBlock() instanceof FormationCorePlateBlock)) {
            return InteractionResult.PASS;
        }
        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        }
        Player player = ctx.getPlayer();
        if (player == null) {
            return InteractionResult.PASS;
        }
        BlockEntity be = level.getBlockEntity(pos);
        if (!(be instanceof FormationCorePlateBlockEntity)) {
            return InteractionResult.PASS;
        }
        FormationCorePlateBlockEntity core = (FormationCorePlateBlockEntity)be;
        String coreName = core.getCustomName();
        if (coreName.isEmpty()) {
            player.displayClientMessage((Component)Component.translatable((String)"message.friday_cultivation.sect_token.unnamed").withStyle(ChatFormatting.RED), true);
            return InteractionResult.FAIL;
        }
        String ownerName = player.getName().getString();
        ItemStack stack = ctx.getItemInHand();
        CompoundTag tag = stack.getOrCreateTag();
        tag.putLong(TAG_CORE_POS, pos.asLong());
        tag.putString(TAG_CORE_DIM, level.dimension().location().toString());
        tag.putString(TAG_CORE_NAME, coreName);
        tag.putString(TAG_OWNER_NAME, ownerName);
        tag.remove(TAG_TEMPORARY);
        player.displayClientMessage((Component)Component.translatable((String)"message.friday_cultivation.sect_token.linked", (Object[])new Object[]{coreName}).withStyle(ChatFormatting.GOLD), true);
        return InteractionResult.CONSUME;
    }

    @NotNull
    public Component getName(@NotNull ItemStack stack) {
        if (!SectTokenItem.isLinked(stack)) {
            return super.getName(stack);
        }
        CompoundTag tag = stack.getTag();
        Component coreName = SectTokenItem.displayCoreName(tag);
        if (tag.getBoolean(TAG_TEMPORARY)) {
            return Component.translatable((String)"item.friday_cultivation.sect_token.temporary", (Object[])new Object[]{coreName}).withStyle(ChatFormatting.GOLD);
        }
        Component ownerName = SectTokenItem.displayOwnerName(tag);
        return Component.translatable((String)"item.friday_cultivation.sect_token.linked", (Object[])new Object[]{coreName, ownerName}).withStyle(ChatFormatting.GOLD);
    }

    public boolean isFoil(@NotNull ItemStack stack) {
        return SectTokenItem.isLinked(stack) || super.isFoil(stack);
    }

    public void appendHoverText(@NotNull ItemStack stack, @Nullable Level level, @NotNull List<Component> tooltip, @NotNull TooltipFlag flag) {
        super.appendHoverText(stack, level, tooltip, flag);
        if (SectTokenItem.isLinked(stack)) {
            CompoundTag tag = stack.getTag();
            String dim = tag.getString(TAG_CORE_DIM);
            BlockPos pos = BlockPos.of((long)tag.getLong(TAG_CORE_POS));
            boolean temporary = tag.getBoolean(TAG_TEMPORARY);
            TooltipUtils.addSection(tooltip, "tooltip.friday_cultivation.section.status");
            tooltip.add((Component)TooltipUtils.statsLine((Component)Component.translatable((String)(temporary ? "tooltip.friday_cultivation.sect_token.credential.temporary" : "tooltip.friday_cultivation.sect_token.credential.personal"))));
            tooltip.add((Component)TooltipUtils.statsLine((Component)Component.translatable((String)"tooltip.friday_cultivation.sect_token.sect", (Object[])new Object[]{SectTokenItem.displayCoreName(tag)})));
            tooltip.add((Component)TooltipUtils.descriptionLine((Component)Component.translatable((String)"tooltip.friday_cultivation.sect_token.location", (Object[])new Object[]{dim, pos.getX(), pos.getY(), pos.getZ()})));
            if (!temporary) {
                tooltip.add((Component)TooltipUtils.descriptionLine((Component)Component.translatable((String)"tooltip.friday_cultivation.sect_token.owner", (Object[])new Object[]{SectTokenItem.displayOwnerName(tag)})));
            }
            TooltipUtils.addBlank(tooltip);
            TooltipUtils.addSection(tooltip, "tooltip.friday_cultivation.section.effect");
            tooltip.add((Component)TooltipUtils.positiveLine((Component)Component.translatable((String)"tooltip.friday_cultivation.sect_token.effect.passage")));
            tooltip.add((Component)TooltipUtils.hintLine((Component)Component.translatable((String)"tooltip.friday_cultivation.sect_token.effect.scope")));
            TooltipUtils.addBlank(tooltip);
            TooltipUtils.addSection(tooltip, "tooltip.friday_cultivation.section.warning");
            if (temporary) {
                tooltip.add((Component)TooltipUtils.warningLine((Component)Component.translatable((String)"tooltip.friday_cultivation.sect_token.warning.temporary")));
                tooltip.add((Component)TooltipUtils.warningLine((Component)Component.translatable((String)"tooltip.friday_cultivation.sect_token.warning.temporary_lost")));
            } else {
                tooltip.add((Component)TooltipUtils.warningLine((Component)Component.translatable((String)"tooltip.friday_cultivation.sect_token.warning.personal")));
                tooltip.add((Component)TooltipUtils.warningLine((Component)Component.translatable((String)"tooltip.friday_cultivation.sect_token.warning.personal_transfer")));
            }
        } else {
            TooltipUtils.addSection(tooltip, "tooltip.friday_cultivation.section.usage");
            tooltip.add((Component)TooltipUtils.descriptionLine((Component)Component.translatable((String)"tooltip.friday_cultivation.sect_token.unlinked")));
            tooltip.add((Component)TooltipUtils.hintLine((Component)Component.translatable((String)"tooltip.friday_cultivation.sect_token.obtain")));
            TooltipUtils.addBlank(tooltip);
            TooltipUtils.addSection(tooltip, "tooltip.friday_cultivation.section.warning");
            tooltip.add((Component)TooltipUtils.warningLine((Component)Component.translatable((String)"tooltip.friday_cultivation.sect_token.warning.unlinked")));
        }
    }

    public static boolean isLinked(ItemStack stack) {
        if (!(stack.getItem() instanceof SectTokenItem)) {
            return false;
        }
        CompoundTag tag = stack.getTag();
        return tag != null && tag.contains(TAG_CORE_POS);
    }

    public static boolean isTemporaryLinked(ItemStack stack) {
        if (!SectTokenItem.isLinked(stack)) {
            return false;
        }
        CompoundTag tag = stack.getTag();
        return tag != null && tag.getBoolean(TAG_TEMPORARY);
    }

    public static boolean isUsableBy(ItemStack stack, Entity entity) {
        if (!SectTokenItem.isLinked(stack)) {
            return false;
        }
        CompoundTag tag = stack.getTag();
        if (tag == null) {
            return false;
        }
        if (tag.getBoolean(TAG_TEMPORARY)) {
            return true;
        }
        String ownerName = tag.getString(TAG_OWNER_NAME);
        if (ownerName.isBlank()) {
            return false;
        }
        if (entity instanceof Player) {
            Player player = (Player)entity;
            return ownerName.equals(player.getName().getString());
        }
        if (entity instanceof WanderingCultivatorEntity) {
            WanderingCultivatorEntity npc = (WanderingCultivatorEntity)entity;
            return ownerName.equals(npc.getCultivatorName().getString());
        }
        return false;
    }

    public static ItemStack createLinked(Level level, BlockPos corePos, String coreName, String ownerName, boolean temporary, int count) {
        ItemStack stack = new ItemStack((ItemLike)ModItems.SECT_TOKEN.get(), Math.max(1, count));
        CompoundTag tag = stack.getOrCreateTag();
        tag.putLong(TAG_CORE_POS, corePos.asLong());
        tag.putString(TAG_CORE_DIM, level.dimension().location().toString());
        tag.putString(TAG_CORE_NAME, coreName == null ? "" : coreName);
        tag.putString(TAG_OWNER_NAME, ownerName == null ? "" : ownerName);
        tag.putBoolean(TAG_TEMPORARY, temporary);
        return stack;
    }

    public static boolean isLinkedToCore(ItemStack stack, Level level, BlockPos corePos) {
        if (!SectTokenItem.isLinked(stack)) {
            return false;
        }
        CompoundTag tag = stack.getTag();
        if (tag.getLong(TAG_CORE_POS) != corePos.asLong()) {
            return false;
        }
        return level.dimension().location().toString().equals(tag.getString(TAG_CORE_DIM));
    }

    public static boolean grantsPassageToCore(ItemStack stack, Entity bearer, Level level, BlockPos corePos) {
        return SectTokenItem.isLinkedToCore(stack, level, corePos) && SectTokenItem.isUsableBy(stack, bearer);
    }

    public static boolean playerHasTokenForCore(Player player, Level level, BlockPos corePos) {
        if (SectTokenItem.grantsPassageToCore(player.containerMenu.getCarried(), (Entity)player, level, corePos)) {
            return true;
        }
        Inventory inv = player.getInventory();
        for (int i = 0; i < inv.getContainerSize(); ++i) {
            if (!SectTokenItem.grantsPassageToCore(inv.getItem(i), (Entity)player, level, corePos)) continue;
            return true;
        }
        return false;
    }

    public static boolean entityHasTokenForCore(Entity entity, Level level, BlockPos corePos) {
        if (entity instanceof Player) {
            Player player = (Player)entity;
            return SectTokenItem.playerHasTokenForCore(player, level, corePos);
        }
        if (entity instanceof WanderingCultivatorEntity) {
            WanderingCultivatorEntity npc = (WanderingCultivatorEntity)entity;
            SimpleContainer inv = npc.getInventory();
            for (int i = 0; i < inv.getContainerSize(); ++i) {
                if (!SectTokenItem.grantsPassageToCore(inv.getItem(i), entity, level, corePos)) continue;
                return true;
            }
        }
        return false;
    }

    private static Component displayCoreName(@Nullable CompoundTag tag) {
        if (tag == null) {
            return Component.translatable((String)"item.friday_cultivation.sect_token.unknown_core");
        }
        String coreName = tag.getString(TAG_CORE_NAME);
        return coreName.isBlank() ? Component.translatable((String)"item.friday_cultivation.sect_token.unknown_core") : Component.literal((String)coreName);
    }

    private static Component displayOwnerName(@Nullable CompoundTag tag) {
        if (tag == null) {
            return Component.translatable((String)"item.friday_cultivation.sect_token.unknown_owner");
        }
        String ownerName = tag.getString(TAG_OWNER_NAME);
        return ownerName.isBlank() ? Component.translatable((String)"item.friday_cultivation.sect_token.unknown_owner") : Component.literal((String)ownerName);
    }
}

