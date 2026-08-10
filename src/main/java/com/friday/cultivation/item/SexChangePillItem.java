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
import com.friday.cultivation.event.CapabilityEvents;
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

public class SexChangePillItem
extends Item {
    public SexChangePillItem(Item.Properties properties) {
        super(properties);
    }

    @NotNull
    public InteractionResultHolder<ItemStack> appendHoverText(@NotNull Level level, @NotNull Player player, @NotNull InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (level.isClientSide) {
            return InteractionResultHolder.success(stack);
        }
        if (!(player instanceof ServerPlayer)) {
            return InteractionResultHolder.pass(stack);
        }
        ServerPlayer sp = (ServerPlayer)player;
        CultivationData data = CultivationCapability.get((Player)sp).orElse(null);
        if (data == null) {
            return InteractionResultHolder.pass(stack);
        }
        data.setGender(data.getGender() == 2 ? 1 : 2);
        CapabilityEvents.syncToClient(sp);
        stack.shrink(1);
        level.playSound(null, sp.blockPosition(), SoundEvents.GENERIC_DRINK, SoundSource.PLAYERS, 0.8f, 1.1f);
        String genderKey = data.getGender() == 2 ? "screen.friday_cultivation.gender.female" : "screen.friday_cultivation.gender.male";
        sp.displayClientMessage((Component)Component.translatable((String)"message.friday_cultivation.sex_change_pill.used", (Object[])new Object[]{Component.translatable((String)genderKey)}).withStyle(ChatFormatting.LIGHT_PURPLE), true);
        return InteractionResultHolder.consume(stack);
    }

    public void appendHoverText(@NotNull ItemStack stack, @Nullable Level level, @NotNull List<Component> tooltip, @NotNull TooltipFlag flag) {
        TooltipUtils.addSection(tooltip, "tooltip.friday_cultivation.section.usage");
        tooltip.add((Component)TooltipUtils.descriptionLine((Component)Component.translatable((String)"tooltip.friday_cultivation.sex_change_pill")));
    }
}

