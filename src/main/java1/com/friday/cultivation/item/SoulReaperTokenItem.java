package com.friday.cultivation.item;

import com.friday.cultivation.CultivationCapability;
import com.friday.cultivation.capability.CultivationData;
import com.friday.cultivation.event.CapabilityEvents;
import com.friday.cultivation.event.SoulReaperOrderHandler;
import com.friday.cultivation.realm.Realm;
import com.friday.cultivation.util.TooltipUtils;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class SoulReaperTokenItem
extends Item {
    public static final String TAG_SOUL_CALL = "SoulCall";

    public SoulReaperTokenItem(Item.Properties properties) {
        super(properties);
    }

    public static boolean hasSoulCall(ItemStack stack) {
        return stack.hasTag() && stack.getTag().contains(TAG_SOUL_CALL);
    }

    public static void setSoulCall(ItemStack stack, boolean active) {
        if (!(stack.getItem() instanceof SoulReaperTokenItem)) {
            return;
        }
        if (active) {
            stack.getOrCreateTag().putBoolean(TAG_SOUL_CALL, true);
            return;
        }
        if (!stack.hasTag()) {
            return;
        }
        stack.getTag().remove(TAG_SOUL_CALL);
        if (stack.getTag().isEmpty()) {
            stack.setTag(null);
        }
    }

    @Override
    public boolean isFoil(@NotNull ItemStack stack) {
        return SoulReaperTokenItem.hasSoulCall(stack) || super.isFoil(stack);
    }

    @NotNull
    @Override
    public InteractionResultHolder<ItemStack> use(@NotNull Level level, @NotNull Player player, @NotNull InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (level.isClientSide) {
            return InteractionResultHolder.success(stack);
        }
        if (!(player instanceof ServerPlayer)) {
            return InteractionResultHolder.fail(stack);
        }
        ServerPlayer serverPlayer = (ServerPlayer) player;
        CultivationData data = CultivationCapability.get(serverPlayer).orElse(null);
        if (data == null || !data.isSoulState()) {
            serverPlayer.displayClientMessage(Component.translatable("message.friday_cultivation.soul_reaper_token.requires_soul"), true);
            return InteractionResultHolder.consume(stack);
        }
        if (data.getRealm().ordinal() < Realm.QI_REFINING.ordinal()) {
            serverPlayer.displayClientMessage(Component.translatable("message.friday_cultivation.soul_reaper_token.requires_realm", Realm.QI_REFINING.displayName()), true);
            return InteractionResultHolder.consume(stack);
        }
        serverPlayer.getCooldowns().addCooldown(this, 20);
        if (!data.isSoulReaperIdentity()) {
            data.setSoulReaperIdentity(true);
            data.setGhostCultivator(true);
            CapabilityEvents.syncToClient(serverPlayer);
            level.playSound(null, serverPlayer.blockPosition(), SoundEvents.SOUL_ESCAPE, SoundSource.PLAYERS, 0.85f, 0.55f);
            serverPlayer.displayClientMessage(Component.translatable("message.friday_cultivation.soul_reaper_token.identity_granted").withStyle(ChatFormatting.DARK_RED), true);
            return InteractionResultHolder.consume(stack);
        }
        SoulReaperOrderHandler.openTargetScreen(serverPlayer);
        level.playSound(null, serverPlayer.blockPosition(), SoundEvents.BOOK_PAGE_TURN, SoundSource.PLAYERS, 0.55f, 0.8f);
        return InteractionResultHolder.consume(stack);
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, @Nullable Level level, @NotNull List<Component> tooltip, @NotNull TooltipFlag flag) {
        TooltipUtils.addSection(tooltip, "tooltip.friday_cultivation.section.usage");
        tooltip.add(TooltipUtils.descriptionLine(Component.translatable("tooltip.friday_cultivation.soul_reaper_token.identity")));
        tooltip.add(TooltipUtils.descriptionLine(Component.translatable("tooltip.friday_cultivation.soul_reaper_token.targets")));
        TooltipUtils.addBlank(tooltip);
        TooltipUtils.addSection(tooltip, "tooltip.friday_cultivation.section.effect");
        tooltip.add(TooltipUtils.positiveLine(Component.translatable("tooltip.friday_cultivation.soul_reaper_token.glow")));
        tooltip.add(TooltipUtils.hintLine(Component.translatable("tooltip.friday_cultivation.soul_reaper_token.hint")));
    }
}
