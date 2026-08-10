package com.friday.cultivation.item;

import com.friday.cultivation.CultivationCapability;
import com.friday.cultivation.capability.CultivationData;
import com.friday.cultivation.LifespanHelper;
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

/**
 * 驻颜丹 — 完整复刻原模组 YouthPillItem。
 * 食用后降低骨龄1岁，最低17岁。32 tick 使用时间。
 */
public class YouthPillItem extends Item {
    public static final int MIN_BONE_AGE = 17;
    private static final int USE_TICKS = 32;

    public YouthPillItem(Item.Properties properties) {
        super(properties);
    }

    @Override
    public int getUseDuration(@NotNull ItemStack stack) {
        return USE_TICKS;
    }

    @NotNull
    @Override
    public UseAnim getUseAnimation(@NotNull ItemStack stack) {
        return UseAnim.EAT;
    }

    @NotNull
    @Override
    public InteractionResultHolder<ItemStack> use(@NotNull Level level, @NotNull Player player, @NotNull InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!level.isClientSide && player instanceof ServerPlayer) {
            ServerPlayer sp = (ServerPlayer) player;
            if (!canUseYouthPill(sp)) {
                return InteractionResultHolder.fail(stack);
            }
        }
        player.startUsingItem(hand);
        return InteractionResultHolder.consume(stack);
    }

    @NotNull
    @Override
    public ItemStack finishUsingItem(@NotNull ItemStack stack, @NotNull Level level, @NotNull LivingEntity entity) {
        if (level.isClientSide || !(entity instanceof ServerPlayer)) {
            return stack;
        }
        ServerPlayer sp = (ServerPlayer) entity;
        if (!canUseYouthPill(sp)) {
            return stack;
        }
        CultivationData ic = CultivationCapability.get(sp).orElse(null);
        if (ic != null) {
            ic.setBoneAge(Math.max(0.0, ic.getBoneAge() - 1.0));
            CapabilityEvents.syncToClient(sp);
            if (!sp.getAbilities().instabuild) {
                stack.shrink(1);
            }
            level.playSound(null, sp.blockPosition(), SoundEvents.PLAYER_LEVELUP, SoundSource.PLAYERS, 0.8f, 1.0f);
            sp.displayClientMessage(Component.translatable("message.friday_cultivation.youth_pill.used", LifespanHelper.displayBoneAge(ic)).withStyle(ChatFormatting.GREEN), true);
        }
        return stack;
    }

    private boolean canUseYouthPill(ServerPlayer sp) {
        CultivationData ic = CultivationCapability.get(sp).orElse(null);
        if (ic != null) {
            int age = LifespanHelper.displayBoneAge(ic);
            if (age <= MIN_BONE_AGE) {
                sp.displayClientMessage(Component.translatable("message.friday_cultivation.youth_pill.too_young", MIN_BONE_AGE).withStyle(ChatFormatting.RED), true);
                return false;
            }
            return true;
        }
        return false;
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, @Nullable Level level, @NotNull List<Component> tooltip, @NotNull TooltipFlag flag) {
        TooltipUtils.addSection(tooltip, Component.translatable("tooltip.friday_cultivation.section.usage"));
        tooltip.add(TooltipUtils.descriptionLine(Component.translatable("tooltip.friday_cultivation.youth_pill")));
    }
}
