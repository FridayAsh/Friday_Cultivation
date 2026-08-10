/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.network.chat.Component
 *  net.minecraft.network.chat.MutableComponent
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
import com.friday.cultivation.cultivation.alchemy.PillEffectSpecs;
import com.friday.cultivation.cultivation.alchemy.PillTier;
import com.friday.cultivation.event.CapabilityEvents;
import com.friday.cultivation.util.TooltipUtils;
import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
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

public class PillItem
extends Item {
    private static final int USE_TICKS = 20;
    private final PillTier tier;
    private final int qiAmount;

    public PillItem(Item.Properties props, PillTier tier, int qiAmount) {
        super(props);
        this.tier = tier;
        this.qiAmount = qiAmount;
    }

    public PillTier tier() {
        return this.tier;
    }

    public int qiAmount() {
        return this.qiAmount;
    }

    public int getUseDuration(@NotNull ItemStack stack) {
        return 20;
    }

    @NotNull
    public UseAnim getUseAnimation(@NotNull ItemStack stack) {
        return UseAnim.EAT;
    }

    @NotNull
    public InteractionResultHolder<ItemStack> use(@NotNull Level level, @NotNull Player player, @NotNull InteractionHand hand) {
        player.startUsingItem(hand);
        return InteractionResultHolder.consume(player.getItemInHand(hand));
    }

    @NotNull
    public ItemStack finishUsingItem(@NotNull ItemStack stack, @NotNull Level level, @NotNull LivingEntity user) {
        if (!level.isClientSide && user instanceof ServerPlayer) {
            ServerPlayer player = (ServerPlayer)user;
            CultivationCapability.get((Player)player).ifPresent(data -> {
                int gained;
                int effectiveQiAmount = PillEffectSpecs.qiAmount(this, this.qiAmount);
                if (effectiveQiAmount < 0) {
                    long need = data.getMaxQi() - data.getCurrentQi();
                    gained = (int)Math.min(Integer.MAX_VALUE, need);
                    data.setCurrentQi(data.getMaxQi());
                } else {
                    long before = data.getCurrentQi();
                    data.setCurrentQi(before + (long)effectiveQiAmount);
                    gained = (int)Math.min(Integer.MAX_VALUE, data.getCurrentQi() - before);
                }
                CapabilityEvents.syncToClient(player);
                player.displayClientMessage((Component)Component.translatable((String)"message.friday_cultivation.pill.consumed", (Object[])new Object[]{gained, this.tier.displayName()}), true);
            });
            level.playSound(null, user.getX(), user.getY(), user.getZ(), SoundEvents.PLAYER_LEVELUP, SoundSource.PLAYERS, 0.5f, 1.4f);
        }
        if (user instanceof Player) {
            Player p = (Player)user;
            if (!p.getAbilities().instabuild) {
                stack.shrink(1);
            }
        }
        return stack;
    }

    @NotNull
    public Component getName(@NotNull ItemStack stack) {
        return Component.translatable((String)this.getDescriptionId(stack)).copy().withStyle(this.tier.color());
    }

    public void appendHoverText(@NotNull ItemStack stack, @Nullable Level level, @NotNull List<Component> tooltip, @NotNull TooltipFlag flag) {
        tooltip.add((Component)Component.translatable((String)("pill_tier.friday_cultivation." + this.tier.id())).withStyle(this.tier.color()));
        TooltipUtils.addBlank(tooltip);
        TooltipUtils.addSection(tooltip, "tooltip.friday_cultivation.section.effect");
        int effectiveQiAmount = PillEffectSpecs.qiAmount(this, this.qiAmount);
        MutableComponent statLine = effectiveQiAmount < 0 ? Component.translatable((String)"tooltip.friday_cultivation.pill.refill_full") : Component.translatable((String)"tooltip.friday_cultivation.pill.qi_gain", (Object[])new Object[]{effectiveQiAmount});
        tooltip.add((Component)TooltipUtils.positiveLine((Component)statLine));
        TooltipUtils.addBlank(tooltip);
        tooltip.add((Component)TooltipUtils.hintLine((Component)Component.translatable((String)"tooltip.friday_cultivation.pill.hint")));
    }
}

