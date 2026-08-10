/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.ChatFormatting
 *  net.minecraft.network.chat.Component
 *  net.minecraft.server.level.ServerPlayer
 *  net.minecraft.sounds.SoundEvents
 *  net.minecraft.sounds.SoundSource
 *  net.minecraft.world.effect.MobEffect
 *  net.minecraft.world.effect.MobEffectInstance
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.item.Item$Properties
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.item.TooltipFlag
 *  net.minecraft.world.level.Level
 *  org.jetbrains.annotations.NotNull
 *  org.jetbrains.annotations.Nullable
 */
package com.friday.cultivation.item;

import com.friday.cultivation.cultivation.alchemy.PillTier;
import com.friday.cultivation.item.PillItem;
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
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class BloodBurnPillItem
extends PillItem {
    public static final String NBT_SKIP_PENALTY = "xc_blood_burn_skip_penalty";

    public BloodBurnPillItem(Item.Properties props, PillTier tier) {
        super(props, tier, 0);
    }

    public static double spellDamageMultiplier(Player player) {
        if (player == null) {
            return 1.0;
        }
        MobEffectInstance effect = player.getEffect((MobEffect)ModEffects.BLOOD_BURN.get());
        if (effect == null) {
            return 1.0;
        }
        return switch (Math.max(0, effect.getAmplifier())) {
            case 0 -> 1.1;
            case 1 -> 1.2;
            case 2 -> 1.3;
            case 3 -> 2.0;
            default -> 3.0;
        };
    }

    @Override
    @NotNull
    public ItemStack finishUsingItem(@NotNull ItemStack stack, @NotNull Level level, @NotNull LivingEntity user) {
        if (!level.isClientSide && user instanceof ServerPlayer) {
            ServerPlayer player = (ServerPlayer)user;
            int tierIdx = this.tier().ordinal();
            float damage = switch (tierIdx) {
                case 0 -> 4.0f;
                case 1 -> 6.0f;
                case 2 -> 8.0f;
                case 3 -> 10.0f;
                case 4 -> 20.0f;
                default -> 0.0f;
            };
            player.setHealth(Math.max(0.5f, player.getHealth() - damage));
            int newDuration = 1200;
            MobEffectInstance existing = player.getEffect((MobEffect)ModEffects.BLOOD_BURN.get());
            if (existing != null) {
                if (existing.getAmplifier() == tierIdx) {
                    newDuration = existing.getDuration() + newDuration;
                } else {
                    player.getPersistentData().putBoolean(NBT_SKIP_PENALTY, true);
                    player.removeEffect(ModEffects.BLOOD_BURN.get());
                    player.getPersistentData().remove(NBT_SKIP_PENALTY);
                }
            }
            player.addEffect(new MobEffectInstance((MobEffect)ModEffects.BLOOD_BURN.get(), newDuration, tierIdx));
            level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.WITHER_SHOOT, SoundSource.PLAYERS, 0.6f, 1.4f);
            player.displayClientMessage((Component)Component.translatable((String)"message.friday_cultivation.pill.blood_burn.consumed", (Object[])new Object[]{this.tier().displayName()}).withStyle(ChatFormatting.RED), false);
        }
        if (user instanceof Player) {
            Player p = (Player)user;
            if (!p.getAbilities().instabuild) {
                stack.shrink(1);
            }
        }
        return stack;
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, @Nullable Level level, @NotNull List<Component> tooltip, @NotNull TooltipFlag flag) {
        tooltip.add((Component)Component.translatable((String)("pill_tier.friday_cultivation." + this.tier().id())).withStyle(this.tier().color()));
        TooltipUtils.addBlank(tooltip);
        TooltipUtils.addSection(tooltip, "tooltip.friday_cultivation.section.effect");
        int tierIdx = this.tier().ordinal();
        String[] hearts = new String[]{"2", "3", "4", "5", "10"};
        String[] mins = new String[]{"1", "1", "1", "1", "1"};
        String[] qiPerSec = new String[]{"10", "100", "1000", "10000", Component.translatable((String)"tooltip.friday_cultivation.pill.blood_burn.qi_locked").getString()};
        String[] dmgBoost = new String[]{"10%", "20%", "30%", "100%", "200%"};
        tooltip.add((Component)TooltipUtils.costLine((Component)Component.translatable((String)"tooltip.friday_cultivation.pill.blood_burn.cost", (Object[])new Object[]{hearts[tierIdx]})));
        tooltip.add((Component)TooltipUtils.statsLine((Component)Component.translatable((String)"tooltip.friday_cultivation.pill.blood_burn.duration", (Object[])new Object[]{mins[tierIdx]})));
        tooltip.add((Component)TooltipUtils.positiveLine((Component)Component.translatable((String)"tooltip.friday_cultivation.pill.blood_burn.qi_gain", (Object[])new Object[]{qiPerSec[tierIdx]})));
        tooltip.add((Component)TooltipUtils.effectLine((Component)Component.translatable((String)"tooltip.friday_cultivation.pill.blood_burn.dmg_boost", (Object[])new Object[]{dmgBoost[tierIdx]})));
        TooltipUtils.addSection(tooltip, "tooltip.friday_cultivation.section.warning");
        tooltip.add((Component)TooltipUtils.warningLine((Component)Component.translatable((String)"tooltip.friday_cultivation.pill.blood_burn.penalty")));
        if (tierIdx == 4) {
            tooltip.add((Component)TooltipUtils.warningLine((Component)Component.translatable((String)"tooltip.friday_cultivation.pill.blood_burn.immortal_penalty")));
        }
        TooltipUtils.addBlank(tooltip);
        tooltip.add((Component)TooltipUtils.hintLine((Component)Component.translatable((String)"tooltip.friday_cultivation.pill.hint")));
    }
}

