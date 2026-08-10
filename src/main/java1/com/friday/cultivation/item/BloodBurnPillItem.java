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
 * 燃血丹 — 完整复刻原模组 BloodBurnPillItem。
 * 食用后扣血+施加燃血效果（大幅增加攻击力），效果结束后灵气归零+虚弱。
 */
public class BloodBurnPillItem extends PillItem {
    public BloodBurnPillItem(Item.Properties props, PillTier tier) {
        super(props, tier, 0);
    }

    /** 燃血法术伤害倍率（照搬原模组 BloodBurnPillItem.spellDamageMultiplier）。 */
    public static double spellDamageMultiplier(Player player) {
        if (player == null) {
            return 1.0;
        }
        MobEffectInstance effect = player.getEffect(ModEffects.BLOOD_BURN.get());
        if (effect == null) {
            return 1.0;
        }
        return switch (Math.max(0, effect.getAmplifier())) {
            case 0 -> 1.1;
            case 1 -> 1.2;
            case 2 -> 1.3;
            case 3 -> 2.0;
            default -> 1.0;
        };
    }

    @Override
    @NotNull
    public ItemStack finishUsingItem(@NotNull ItemStack stack, @NotNull Level level, @NotNull LivingEntity user) {
        if (!level.isClientSide && user instanceof ServerPlayer) {
            ServerPlayer player = (ServerPlayer) user;
            int tierIdx = this.tier().ordinal();
            int durationSec = switch (tierIdx) {
                case 0 -> 30; case 1 -> 60; case 2 -> 120; case 3 -> 300; default -> 0;
            };
            int durationTicks = durationSec * 20;
            float hpCost = switch (tierIdx) {
                case 0 -> 2.0f; case 1 -> 6.0f; case 2 -> 20.0f; case 3 -> 60.0f; default -> 0.0f;
            };
            if (hpCost > 0 && player.getHealth() > hpCost) {
                player.hurt(player.damageSources().magic(), hpCost);
            }
            player.addEffect(new MobEffectInstance(ModEffects.BLOOD_BURN.get(), durationTicks, tierIdx));
            level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.WITHER_SPAWN, SoundSource.PLAYERS, 0.4f, 1.2f);
            player.displayClientMessage(Component.translatable("message.friday_cultivation.pill.blood_burn.consumed", this.tier().displayName()).withStyle(ChatFormatting.RED), true);
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
            case 0 -> ".pill.blood_burn.low";
            case 1 -> ".pill.blood_burn.mid";
            case 2 -> ".pill.blood_burn.high";
            case 3 -> ".pill.blood_burn.supreme";
            default -> "";
        };
        tooltip.add(TooltipUtils.effectLine(Component.translatable("tooltip.friday_cultivation" + key)));
        TooltipUtils.addBlank(tooltip);
        tooltip.add(TooltipUtils.warningLine(Component.translatable("tooltip.friday_cultivation.pill.blood_burn.warning")));
        TooltipUtils.addBlank(tooltip);
        tooltip.add(TooltipUtils.hintLine(Component.translatable("tooltip.friday_cultivation.pill.hint")));
    }
}
