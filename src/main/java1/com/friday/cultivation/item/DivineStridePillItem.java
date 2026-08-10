package com.friday.cultivation.item;

import com.friday.cultivation.alchemy.PillTier;
import com.friday.cultivation.registry.ModEffects;
import com.friday.cultivation.util.TooltipUtils;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * 神行丹 — 完整复刻原模组 DivineStridePillItem。
 * 食用后施加速度、跳跃提升和神行效果。
 */
public class DivineStridePillItem extends PillItem {
    public DivineStridePillItem(Item.Properties props, PillTier tier) {
        super(props, tier, 0);
    }

    @Override
    @NotNull
    public ItemStack finishUsingItem(@NotNull ItemStack stack, @NotNull Level level, @NotNull LivingEntity user) {
        if (!level.isClientSide && user instanceof ServerPlayer) {
            ServerPlayer player = (ServerPlayer) user;
            int tierIdx = this.tier().ordinal();
            int durationSec = switch (tierIdx) {
                case 0 -> 180; case 1 -> 480; case 2 -> 600; case 3 -> 1800; default -> 0;
            };
            int durationTicks = durationSec * 20;
            int speedAmp = switch (tierIdx) {
                case 0 -> 0; case 1 -> 1; case 2 -> 3; case 3 -> 3; default -> 0;
            };
            player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, durationTicks, speedAmp));
            if (tierIdx <= 1) {
                player.addEffect(new MobEffectInstance(MobEffects.JUMP, durationTicks, tierIdx));
            }
            player.addEffect(new MobEffectInstance((MobEffect) ModEffects.DIVINE_STRIDE.get(), durationTicks, tierIdx));
            level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.WITHER_SPAWN, SoundSource.PLAYERS, 0.6f, 1.4f);
            player.displayClientMessage(Component.translatable("message.friday_cultivation.pill.divine_stride.consumed", this.tier().displayName()).withStyle(ChatFormatting.AQUA), true);
        }
        if (user instanceof Player) {
            Player p = (Player) user;
            if (!p.getAbilities().instabuild) {
                stack.shrink(1);
            }
        }
        return stack;
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, @Nullable Level level, @NotNull List<Component> tooltip, @NotNull TooltipFlag flag) {
        tooltip.add(Component.translatable("pill_tier.friday_cultivation." + this.tier().id()).withStyle(this.tier().color()));
        TooltipUtils.addBlank(tooltip);
        TooltipUtils.addSection(tooltip, Component.translatable("tooltip.friday_cultivation.section.effect"));
        int tierIdx = this.tier().ordinal();
        String key = switch (tierIdx) {
            case 0 -> ".pill.divine_stride.low";
            case 1 -> ".pill.divine_stride.mid";
            case 2 -> ".pill.divine_stride.high";
            case 3 -> ".pill.divine_stride.supreme";
            default -> "";
        };
        tooltip.add(TooltipUtils.effectLine(Component.translatable("tooltip.friday_cultivation" + key)));
        TooltipUtils.addBlank(tooltip);
        tooltip.add(TooltipUtils.hintLine(Component.translatable("tooltip.friday_cultivation.pill.hint")));
    }
}
