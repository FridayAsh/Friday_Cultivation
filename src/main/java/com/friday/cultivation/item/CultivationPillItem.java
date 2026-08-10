/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.network.chat.Component
 *  net.minecraft.network.chat.MutableComponent
 *  net.minecraft.server.level.ServerPlayer
 *  net.minecraft.sounds.SoundEvents
 *  net.minecraft.sounds.SoundSource
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

import com.friday.cultivation.cultivation.CultivationCapability;
import com.friday.cultivation.cultivation.alchemy.PillEffectSpecs;
import com.friday.cultivation.cultivation.alchemy.PillTier;
import com.friday.cultivation.event.CapabilityEvents;
import com.friday.cultivation.item.PillItem;
import com.friday.cultivation.util.TooltipUtils;
import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class CultivationPillItem
extends PillItem {
    public CultivationPillItem(Item.Properties props, PillTier tier, int cultivationAmount) {
        super(props, tier, cultivationAmount);
    }

    @Override
    @NotNull
    public ItemStack finishUsingItem(@NotNull ItemStack stack, @NotNull Level level, @NotNull LivingEntity user) {
        if (!level.isClientSide && user instanceof ServerPlayer) {
            ServerPlayer player = (ServerPlayer)user;
            CultivationCapability.get((Player)player).ifPresent(data -> {
                int effectiveAmount = PillEffectSpecs.qiAmount(this, this.qiAmount());
                long before = data.getCultivationProgress();
                if (effectiveAmount < 0) {
                    data.setCultivationProgress(data.getMaxCultivation());
                } else {
                    data.setCultivationProgress(before + (long)effectiveAmount);
                }
                long gained = data.getCultivationProgress() - before;
                CapabilityEvents.syncToClient(player);
                player.displayClientMessage((Component)Component.translatable((String)"message.friday_cultivation.cultivation_pill.consumed", (Object[])new Object[]{gained, this.tier().displayName()}), true);
            });
            level.playSound(null, user.getX(), user.getY(), user.getZ(), SoundEvents.PLAYER_LEVELUP, SoundSource.PLAYERS, 0.5f, 1.25f);
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
        int effectiveAmount = PillEffectSpecs.qiAmount(this, this.qiAmount());
        MutableComponent statLine = effectiveAmount < 0 ? Component.translatable((String)"tooltip.friday_cultivation.cultivation_pill.fill_full") : Component.translatable((String)"tooltip.friday_cultivation.cultivation_pill.gain", (Object[])new Object[]{effectiveAmount});
        tooltip.add((Component)TooltipUtils.positiveLine((Component)statLine));
        TooltipUtils.addBlank(tooltip);
        tooltip.add((Component)TooltipUtils.hintLine((Component)Component.translatable((String)"tooltip.friday_cultivation.pill.hint")));
    }
}

