package com.friday.cultivation.item;

import com.friday.cultivation.block.formation.FormationCorePlateBlock;
import com.friday.cultivation.block.formation.FormationCorePlateBlockEntity;
import com.friday.cultivation.entity.npc.WanderingCultivatorEntity;
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
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * 宗门令牌 — 完全照搬原模组 com.xiaoxiang.cultivation.item.SectTokenItem
 */
public class SectTokenItem extends Item {
    public static final String TAG_CORE_POS = "linkedCorePos";
    public static final String TAG_CORE_DIM = "linkedCoreDim";
    public static final String TAG_CORE_NAME = "linkedCoreName";
    public static final String TAG_OWNER_NAME = "ownerName";
    public static final String TAG_TEMPORARY = "temporarySectToken";

    public SectTokenItem(Item.Properties properties) {
        super(properties);
    }

    @NotNull
    @Override
    public InteractionResult useOn(@NotNull UseOnContext ctx) {
        BlockPos pos = ctx.getClickedPos();
        Level level = ctx.getLevel();
        BlockState state = level.getBlockState(pos);
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
        FormationCorePlateBlockEntity core = (FormationCorePlateBlockEntity) be;
        String coreName = core.getCustomName();
        if (coreName.isEmpty()) {
            player.displayClientMessage(Component.translatable("message.friday_cultivation.sect_token.unnamed").withStyle(ChatFormatting.RED), true);
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
        player.displayClientMessage(Component.translatable("message.friday_cultivation.sect_token.linked", coreName).withStyle(ChatFormatting.GOLD), true);
        return InteractionResult.CONSUME;
    }

    @NotNull
    @Override
    public Component getName(@NotNull ItemStack stack) {
        if (!SectTokenItem.isLinked(stack)) {
            return super.getName(stack);
        }
        CompoundTag tag = stack.getTag();
        Component coreName = SectTokenItem.displayCoreName(tag);
        if (tag.getBoolean(TAG_TEMPORARY)) {
            return Component.translatable("item.friday_cultivation.sect_token.temporary", coreName).withStyle(ChatFormatting.GOLD);
        }
        Component ownerName = SectTokenItem.displayOwnerName(tag);
        return Component.translatable("item.friday_cultivation.sect_token.linked", coreName, ownerName).withStyle(ChatFormatting.GOLD);
    }

    @Override
    public boolean isFoil(@NotNull ItemStack stack) {
        return SectTokenItem.isLinked(stack) || super.isFoil(stack);
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, @Nullable Level level, @NotNull List<Component> tooltip, @NotNull TooltipFlag flag) {
        super.appendHoverText(stack, level, tooltip, flag);
        if (SectTokenItem.isLinked(stack)) {
            CompoundTag tag = stack.getTag();
            String dim = tag.getString(TAG_CORE_DIM);
            BlockPos pos = BlockPos.of(tag.getLong(TAG_CORE_POS));
            boolean temporary = tag.getBoolean(TAG_TEMPORARY);
            TooltipUtils.addSection(tooltip, "tooltip.friday_cultivation.section.status");
            tooltip.add(TooltipUtils.statsLine(Component.translatable(temporary ? "tooltip.friday_cultivation.sect_token.credential.temporary" : "tooltip.friday_cultivation.sect_token.credential.personal")));
            tooltip.add(TooltipUtils.statsLine(Component.translatable("tooltip.friday_cultivation.sect_token.sect", SectTokenItem.displayCoreName(tag))));
            tooltip.add(TooltipUtils.descriptionLine(Component.translatable("tooltip.friday_cultivation.sect_token.location", dim, pos.getX(), pos.getY(), pos.getZ())));
            if (!temporary) {
                tooltip.add(TooltipUtils.descriptionLine(Component.translatable("tooltip.friday_cultivation.sect_token.owner", SectTokenItem.displayOwnerName(tag))));
            }
            TooltipUtils.addBlank(tooltip);
            TooltipUtils.addSection(tooltip, "tooltip.friday_cultivation.section.effect");
            tooltip.add(TooltipUtils.positiveLine(Component.translatable("tooltip.friday_cultivation.sect_token.effect.passage")));
            tooltip.add(TooltipUtils.hintLine(Component.translatable("tooltip.friday_cultivation.sect_token.effect.scope")));
            TooltipUtils.addBlank(tooltip);
            TooltipUtils.addSection(tooltip, "tooltip.friday_cultivation.section.warning");
            if (temporary) {
                tooltip.add(TooltipUtils.warningLine(Component.translatable("tooltip.friday_cultivation.sect_token.warning.temporary")));
                tooltip.add(TooltipUtils.warningLine(Component.translatable("tooltip.friday_cultivation.sect_token.warning.temporary_lost")));
            } else {
                tooltip.add(TooltipUtils.warningLine(Component.translatable("tooltip.friday_cultivation.sect_token.warning.personal")));
                tooltip.add(TooltipUtils.warningLine(Component.translatable("tooltip.friday_cultivation.sect_token.warning.personal_transfer")));
            }
        } else {
            TooltipUtils.addSection(tooltip, "tooltip.friday_cultivation.section.usage");
            tooltip.add(TooltipUtils.descriptionLine(Component.translatable("tooltip.friday_cultivation.sect_token.unlinked")));
            tooltip.add(TooltipUtils.hintLine(Component.translatable("tooltip.friday_cultivation.sect_token.obtain")));
            TooltipUtils.addBlank(tooltip);
            TooltipUtils.addSection(tooltip, "tooltip.friday_cultivation.section.warning");
            tooltip.add(TooltipUtils.warningLine(Component.translatable("tooltip.friday_cultivation.sect_token.warning.unlinked")));
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
        if (entity instanceof Player player) {
            return ownerName.equals(player.getName().getString());
        }
        if (entity instanceof WanderingCultivatorEntity npc) {
            return ownerName.equals(npc.getCultivatorName().getString());
        }
        return false;
    }

    public static ItemStack createLinked(Level level, BlockPos corePos, String coreName, String ownerName, boolean temporary, int count) {
        ItemStack stack = new ItemStack(ModItems.SECT_TOKEN.get(), Math.max(1, count));
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
        if (SectTokenItem.grantsPassageToCore(player.getInventory().getSelected(), player, level, corePos)) {
            return true;
        }
        Inventory inv = player.getInventory();
        for (int i = 0; i < inv.getContainerSize(); ++i) {
            if (!SectTokenItem.grantsPassageToCore(inv.getItem(i), player, level, corePos)) continue;
            return true;
        }
        return false;
    }

    public static boolean entityHasTokenForCore(Entity entity, Level level, BlockPos corePos) {
        if (entity instanceof Player player) {
            return SectTokenItem.playerHasTokenForCore(player, level, corePos);
        }
        if (entity instanceof WanderingCultivatorEntity npc) {
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
            return Component.translatable("item.friday_cultivation.sect_token.unknown_core");
        }
        String coreName = tag.getString(TAG_CORE_NAME);
        return coreName.isBlank() ? Component.translatable("item.friday_cultivation.sect_token.unknown_core") : Component.literal(coreName);
    }

    private static Component displayOwnerName(@Nullable CompoundTag tag) {
        if (tag == null) {
            return Component.translatable("item.friday_cultivation.sect_token.unknown_owner");
        }
        String ownerName = tag.getString(TAG_OWNER_NAME);
        return ownerName.isBlank() ? Component.translatable("item.friday_cultivation.sect_token.unknown_owner") : Component.literal(ownerName);
    }
}
