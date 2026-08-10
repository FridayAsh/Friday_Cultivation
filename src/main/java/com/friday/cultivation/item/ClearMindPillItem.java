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
 *  net.minecraft.world.effect.MobEffectCategory
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
import java.util.ArrayList;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ClearMindPillItem
extends PillItem {
    public ClearMindPillItem(Item.Properties props, PillTier tier) {
        super(props, tier, 0);
    }

    @Override
    @NotNull
    public ItemStack finishUsingItem(@NotNull ItemStack stack, @NotNull Level level, @NotNull LivingEntity user) {
        if (!level.isClientSide && user instanceof ServerPlayer) {
            ServerPlayer player = (ServerPlayer)user;
            int tierIdx = this.tier().ordinal();
            ArrayList<MobEffect> harmful = new ArrayList<MobEffect>();
            for (MobEffectInstance inst : player.getActiveEffects()) {
                if (inst.getEffect().getCategory() != MobEffectCategory.HARMFUL) continue;
                harmful.add(inst.getEffect());
            }
            int removedCount = 0;
            if (!harmful.isEmpty()) {
                if (tierIdx == 0) {
                    MobEffect chosen = (MobEffect)harmful.get(player.getRandom().nextInt(harmful.size()));
                    player.removeEffect(chosen);
                    removedCount = 1;
                } else {
                    for (MobEffect e : harmful) {
                        player.removeEffect(e);
                    }
                    removedCount = harmful.size();
                }
            }
            int clearMindDuration = switch (tierIdx) {
                case 2 -> 1200;
                case 3 -> 3600;
                case 4 -> Integer.MAX_VALUE;
                default -> 0;
            };
            if (clearMindDuration > 0) {
                player.addEffect(new MobEffectInstance((MobEffect)ModEffects.CLEAR_MIND.get(), clearMindDuration, 0));
            }
            level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 0.7f, 1.5f);
            player.displayClientMessage((Component)Component.translatable((String)"message.friday_cultivation.pill.clear_mind.consumed", (Object[])new Object[]{this.tier().displayName(), removedCount}).withStyle(ChatFormatting.AQUA), false);
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
        String cleanseKey = tierIdx == 0 ? "tooltip.friday_cultivation.pill.clear_mind.cleanse_random" : "tooltip.friday_cultivation.pill.clear_mind.cleanse_all";
        tooltip.add((Component)TooltipUtils.positiveLine((Component)Component.translatable((String)cleanseKey)));
        if (tierIdx >= 2) {
            String durationStr = switch (tierIdx) {
                case 2 -> "1";
                case 3 -> "3";
                case 4 -> Component.translatable((String)"tooltip.friday_cultivation.pill.clear_mind.permanent").getString();
                default -> "";
            };
            tooltip.add((Component)TooltipUtils.effectLine((Component)Component.translatable((String)"tooltip.friday_cultivation.pill.clear_mind.buff", (Object[])new Object[]{durationStr})));
        }
        TooltipUtils.addBlank(tooltip);
        tooltip.add((Component)TooltipUtils.hintLine((Component)Component.translatable((String)"tooltip.friday_cultivation.pill.hint")));
    }
}

