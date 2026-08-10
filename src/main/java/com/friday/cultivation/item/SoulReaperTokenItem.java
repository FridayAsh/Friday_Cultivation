/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.ChatFormatting
 *  net.minecraft.network.chat.Component
 *  net.minecraft.server.level.ServerPlayer
 *  net.minecraft.sounds.SoundEvents
 *  net.minecraft.sounds.SoundSource
 *  net.minecraft.world.InteractionHand
 *  net.minecraft.world.InteractionResultHolder
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.item.Item
 *  net.minecraft.world.item.Item$Properties
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.item.TooltipFlag
 *  net.minecraft.world.level.Level
 *  org.jetbrains.annotations.NotNull
 *  org.jetbrains.annotations.Nullable
 */
package com.friday.cultivation.item;

import com.friday.cultivation.cultivation.CultivationCapability;
import com.friday.cultivation.cultivation.CultivationData;
import com.friday.cultivation.cultivation.realm.Realm;
import com.friday.cultivation.event.CapabilityEvents;
import com.friday.cultivation.event.SoulReaperOrderHandler;
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
        return stack.hasTag() && stack.getOrCreateTag().getBoolean(TAG_SOUL_CALL);
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

    public boolean isFoil(@NotNull ItemStack stack) {
        return SoulReaperTokenItem.hasSoulCall(stack) || super.isFoil(stack);
    }

    @NotNull
    public InteractionResultHolder<ItemStack> use(@NotNull Level level, @NotNull Player player, @NotNull InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (level.isClientSide) {
            return InteractionResultHolder.success(stack);
        }
        if (!(player instanceof ServerPlayer)) {
            return InteractionResultHolder.pass(stack);
        }
        ServerPlayer serverPlayer = (ServerPlayer)player;
        CultivationData data = CultivationCapability.get((Player)serverPlayer).orElse(null);
        if (data == null || !data.isSoulState()) {
            serverPlayer.displayClientMessage((Component)Component.translatable((String)"message.friday_cultivation.soul_reaper_token.requires_soul"), true);
            return InteractionResultHolder.consume(stack);
        }
        if (data.getRealm().ordinal() < Realm.QI_REFINING.ordinal()) {
            serverPlayer.displayClientMessage((Component)Component.translatable((String)"message.friday_cultivation.soul_reaper_token.requires_realm", (Object[])new Object[]{Realm.QI_REFINING.displayName()}), true);
            return InteractionResultHolder.consume(stack);
        }
        serverPlayer.getCooldowns().addCooldown((Item)this, 20);
        if (!data.isSoulReaperIdentity()) {
            data.setSoulReaperIdentity(true);
            data.setGhostCultivator(true);
            CapabilityEvents.syncToClient(serverPlayer);
            level.playSound(null, serverPlayer.blockPosition(), SoundEvents.SOUL_ESCAPE, SoundSource.PLAYERS, 0.85f, 0.55f);
            serverPlayer.displayClientMessage((Component)Component.translatable((String)"message.friday_cultivation.soul_reaper_token.identity_granted").withStyle(ChatFormatting.DARK_RED), true);
            return InteractionResultHolder.consume(stack);
        }
        SoulReaperOrderHandler.openTargetScreen(serverPlayer);
        level.playSound(null, serverPlayer.blockPosition(), SoundEvents.BOOK_PAGE_TURN, SoundSource.PLAYERS, 0.55f, 0.8f);
        return InteractionResultHolder.consume(stack);
    }

    public void appendHoverText(@NotNull ItemStack stack, @Nullable Level level, @NotNull List<Component> tooltip, @NotNull TooltipFlag flag) {
        TooltipUtils.addSection(tooltip, "tooltip.friday_cultivation.section.usage");
        tooltip.add((Component)TooltipUtils.descriptionLine((Component)Component.translatable((String)"tooltip.friday_cultivation.soul_reaper_token.identity")));
        tooltip.add((Component)TooltipUtils.descriptionLine((Component)Component.translatable((String)"tooltip.friday_cultivation.soul_reaper_token.targets")));
        TooltipUtils.addBlank(tooltip);
        TooltipUtils.addSection(tooltip, "tooltip.friday_cultivation.section.effect");
        tooltip.add((Component)TooltipUtils.positiveLine((Component)Component.translatable((String)"tooltip.friday_cultivation.soul_reaper_token.glow")));
        tooltip.add((Component)TooltipUtils.hintLine((Component)Component.translatable((String)"tooltip.friday_cultivation.soul_reaper_token.hint")));
    }
}

