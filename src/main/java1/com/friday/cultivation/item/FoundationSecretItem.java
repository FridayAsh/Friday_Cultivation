package com.friday.cultivation.item;

import com.friday.cultivation.CultivationCapability;
import com.friday.cultivation.capability.CultivationData;
import com.friday.cultivation.event.CapabilityEvents;
import com.friday.cultivation.util.TooltipUtils;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
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
 * 筑基秘籍 — 完整复刻原模组 FoundationSecretItem
 */
public class FoundationSecretItem extends Item {
    public static final int USE_TICKS = 32;

    public FoundationSecretItem(Item.Properties properties) {
        super(properties);
    }

    @Override
    public int getUseDuration(@NotNull ItemStack stack) { return USE_TICKS; }

    @NotNull
    @Override
    public UseAnim getUseAnimation(@NotNull ItemStack stack) { return UseAnim.BOW; }

    @NotNull
    @Override
    public InteractionResultHolder<ItemStack> use(@NotNull Level level, @NotNull Player player, @NotNull InteractionHand hand) {
        player.startUsingItem(hand);
        return InteractionResultHolder.consume(player.getItemInHand(hand));
    }

    @Override
    public void onUseTick(@NotNull Level level, @NotNull LivingEntity entity, @NotNull ItemStack stack, int remainingUseDuration) {
        if (!(level instanceof ServerLevel)) return;
        ServerLevel sl = (ServerLevel) level;
        double elapsed = (double)(USE_TICKS - remainingUseDuration) * 0.35;
        for (int i = 0; i < 2; ++i) {
            double ang = elapsed + (double)i * Math.PI;
            double r = 0.9;
            sl.sendParticles(ParticleTypes.ENCHANT, entity.getX() + Math.cos(ang) * r, entity.getY() + 1.1, entity.getZ() + Math.sin(ang) * r, 1, 0.0, 0.0, 0.0, 0.0);
        }
    }

    @NotNull
    @Override
    public ItemStack finishUsingItem(@NotNull ItemStack stack, @NotNull Level level, @NotNull LivingEntity entity) {
        if (!level.isClientSide && entity instanceof ServerPlayer) {
            ServerPlayer sp = (ServerPlayer) entity;
            CultivationData ic = CultivationCapability.get(sp).orElse(null);
            if (ic != null) {
                ic.setZhujiSecretUsed(true);
                CapabilityEvents.syncToClient(sp);
            }
            sp.displayClientMessage(Component.translatable("message.friday_cultivation.foundation.secret_used").withStyle(ChatFormatting.LIGHT_PURPLE), true);
            level.playSound(null, sp.blockPosition(), SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.PLAYERS, 0.8f, 1.0f);
            stack.shrink(1);
        }
        return stack;
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, @Nullable Level level, @NotNull List<Component> tooltip, @NotNull TooltipFlag flag) {
        TooltipUtils.addSection(tooltip, "tooltip.friday_cultivation.section.usage");
        tooltip.add(TooltipUtils.descriptionLine(Component.translatable("tooltip.friday_cultivation.foundation_secret")));
        TooltipUtils.addSection(tooltip, "tooltip.friday_cultivation.section.obtain");
        tooltip.add(TooltipUtils.hintLine(Component.translatable("tooltip.friday_cultivation.foundation_secret.obtain")));
    }
}
