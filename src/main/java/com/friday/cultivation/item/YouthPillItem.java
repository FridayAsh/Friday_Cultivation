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
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.item.Item
 *  net.minecraft.world.item.Item$Properties
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.item.TooltipFlag
 *  net.minecraft.world.item.UseAnim
 *  net.minecraft.world.level.Level
 *  org.jetbrains.annotations.NotNull
 *  org.jetbrains.annotations.Nullable
 */
package com.friday.cultivation.item;

import com.friday.cultivation.cultivation.CultivationCapability;
import com.friday.cultivation.cultivation.CultivationData;
import com.friday.cultivation.cultivation.LifespanHelper;
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
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class YouthPillItem
extends Item {
    public static final int MIN_BONE_AGE = 17;
    private static final int USE_TICKS = 32;

    public YouthPillItem(Item.Properties properties) {
        super(properties);
    }

    public int getUseDuration(@NotNull ItemStack stack) {
        return 32;
    }

    @NotNull
    public UseAnim getUseAnimation(@NotNull ItemStack stack) {
        return UseAnim.EAT;
    }

    @NotNull
    public InteractionResultHolder<ItemStack> use(@NotNull Level level, @NotNull Player player, @NotNull InteractionHand hand) {
        ServerPlayer sp;
        ItemStack stack = player.getItemInHand(hand);
        if (!level.isClientSide && player instanceof ServerPlayer && !this.canUseYouthPill(sp = (ServerPlayer)player)) {
            return InteractionResultHolder.fail(stack);
        }
        player.startUsingItem(hand);
        return InteractionResultHolder.consume(stack);
    }

    @NotNull
    public ItemStack finishUsingItem(@NotNull ItemStack stack, @NotNull Level level, @NotNull LivingEntity entity) {
        if (level.isClientSide || !(entity instanceof ServerPlayer)) {
            return stack;
        }
        ServerPlayer sp = (ServerPlayer)entity;
        if (!this.canUseYouthPill(sp)) {
            return stack;
        }
        CultivationData data = CultivationCapability.get((Player)sp).orElse(null);
        if (data == null) {
            return stack;
        }
        data.setBoneAge(Math.max(0.0, data.getBoneAge() - 1.0));
        CapabilityEvents.syncToClient(sp);
        if (!sp.getAbilities().instabuild) {
            stack.shrink(1);
        }
        level.playSound(null, sp.blockPosition(), SoundEvents.GENERIC_EAT, SoundSource.PLAYERS, 0.8f, 1.0f);
        sp.displayClientMessage((Component)Component.translatable((String)"message.friday_cultivation.youth_pill.used", (Object[])new Object[]{LifespanHelper.displayBoneAge(data)}).withStyle(ChatFormatting.GREEN), true);
        return stack;
    }

    private boolean canUseYouthPill(ServerPlayer sp) {
        CultivationData data = CultivationCapability.get((Player)sp).orElse(null);
        if (data == null) {
            return false;
        }
        int age = LifespanHelper.displayBoneAge(data);
        if (age <= 17) {
            sp.displayClientMessage((Component)Component.translatable((String)"message.friday_cultivation.youth_pill.too_young", (Object[])new Object[]{17}).withStyle(ChatFormatting.RED), true);
            return false;
        }
        return true;
    }

    public void appendHoverText(@NotNull ItemStack stack, @Nullable Level level, @NotNull List<Component> tooltip, @NotNull TooltipFlag flag) {
        TooltipUtils.addSection(tooltip, "tooltip.friday_cultivation.section.usage");
        tooltip.add((Component)TooltipUtils.descriptionLine((Component)Component.translatable((String)"tooltip.friday_cultivation.youth_pill")));
    }
}

