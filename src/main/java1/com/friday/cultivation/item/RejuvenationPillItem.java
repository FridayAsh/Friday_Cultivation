package com.friday.cultivation.item;

import com.friday.cultivation.alchemy.PillEffectSpecs;
import com.friday.cultivation.alchemy.PillTier;
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
 * 回春丹 — 完整复刻原模组 RejuvenationPillItem。
 * 食用后恢复生命值，高级别额外施加生命恢复和伤害吸收效果。
 */
public class RejuvenationPillItem extends PillItem {
    public RejuvenationPillItem(Item.Properties props, PillTier tier) {
        super(props, tier, 0);
    }

    @Override
    @NotNull
    public ItemStack finishUsingItem(@NotNull ItemStack stack, @NotNull Level level, @NotNull LivingEntity user) {
        if (!level.isClientSide && user instanceof ServerPlayer) {
            ServerPlayer player = (ServerPlayer) user;
            float healAmount = PillEffectSpecs.rejuvenationHeal(this, this.tier(), player.getMaxHealth());
            float beforeHp = player.getHealth();
            player.heal(healAmount);
            float actualHealed = player.getHealth() - beforeHp;
            int regenTicks = PillEffectSpecs.regenerationTicks(this, this.tier());
            if (regenTicks > 0) {
                player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, regenTicks, PillEffectSpecs.regenerationAmplifier(this, this.tier())));
            }
            int absorptionTicks = PillEffectSpecs.absorptionTicks(this, this.tier());
            if (absorptionTicks > 0) {
                player.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, absorptionTicks, PillEffectSpecs.absorptionAmplifier(this, this.tier())));
            }
            level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.PLAYER_LEVELUP, SoundSource.PLAYERS, 0.4f, 1.6f);
            player.displayClientMessage(Component.translatable("message.friday_cultivation.pill.rejuvenation.consumed", this.tier().displayName(), (int) actualHealed).withStyle(ChatFormatting.GREEN), true);
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
        if (PillEffectSpecs.rejuvenationHealFull(this, this.tier())) {
            tooltip.add(TooltipUtils.positiveLine(Component.translatable("tooltip.friday_cultivation.pill.rejuvenation.heal_full")));
        } else {
            int healAmount = Math.round(PillEffectSpecs.rejuvenationHeal(this, this.tier(), 0.0f));
            tooltip.add(TooltipUtils.positiveLine(Component.translatable("tooltip.friday_cultivation.pill.rejuvenation.heal_amount", healAmount)));
        }
        if (PillEffectSpecs.regenerationTicks(this, this.tier()) > 0 || PillEffectSpecs.absorptionTicks(this, this.tier()) > 0) {
            tooltip.add(TooltipUtils.effectLine(Component.translatable("tooltip.friday_cultivation.pill.rejuvenation.supreme_buff")));
        }
        TooltipUtils.addBlank(tooltip);
        tooltip.add(TooltipUtils.hintLine(Component.translatable("tooltip.friday_cultivation.pill.hint")));
    }
}
