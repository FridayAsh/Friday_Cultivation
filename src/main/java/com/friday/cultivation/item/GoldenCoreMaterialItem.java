/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.ChatFormatting
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
import com.friday.cultivation.cultivation.CultivationData;
import com.friday.cultivation.event.CapabilityEvents;
import com.friday.cultivation.util.TooltipUtils;
import java.util.List;
import net.minecraft.ChatFormatting;
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

public class GoldenCoreMaterialItem
extends Item {
    private static final int USE_TICKS = 32;
    public static final long JIEDAN_PILL_CULTIVATION_GAIN = 100L;
    public static final long CREATION_FRUIT_CULTIVATION_GAIN = 100L;
    private final Kind kind;

    public GoldenCoreMaterialItem(Item.Properties properties, Kind kind) {
        super(properties);
        this.kind = kind;
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
        ItemStack stack = player.getItemInHand(hand);
        if (this.kind == Kind.BLOOD_TRANSFORMATION_TALISMAN) {
            if (!level.isClientSide && player instanceof ServerPlayer) {
                ServerPlayer sp = (ServerPlayer)player;
                sp.displayClientMessage((Component)Component.translatable((String)"message.friday_cultivation.golden_core.blood_transformation_talisman_hint").withStyle(ChatFormatting.LIGHT_PURPLE), true);
            }
            return InteractionResultHolder.sidedSuccess(stack, (boolean)level.isClientSide);
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
        CultivationData data = CultivationCapability.get((Player)sp).orElse(null);
        if (data == null) {
            return stack;
        }
        int count = switch (this.kind) {
            default -> throw new IncompatibleClassChangeError();
            case JIEDAN_PILL -> data.addJiedanPillUsed(1);
            case BLOOD_JIEDAN_PILL -> data.addBloodJiedanPillUsed(1);
            case TRUE_BLOOD -> data.addTrueBloodUsed(1);
            case EARTH_EVIL_QI -> data.addEarthEvilQiUsed(1);
            case HEAVEN_CLEAR_QI -> data.addHeavenClearQiUsed(1);
            case CREATION_FRUIT -> data.addCreationFruitEaten(1);
            case BLOOD_TRANSFORMATION_TALISMAN -> 0;
        };
        long cultivationReward = GoldenCoreMaterialItem.cultivationGain(this.kind);
        long cultivationGained = GoldenCoreMaterialItem.addCultivationProgress(data, cultivationReward);
        CapabilityEvents.syncToClient(sp);
        if (!sp.getAbilities().instabuild) {
            stack.shrink(1);
        }
        level.playSound(null, sp.blockPosition(), SoundEvents.GENERIC_EAT, SoundSource.PLAYERS, 0.8f, 1.0f);
        MutableComponent message = cultivationReward > 0L ? Component.translatable((String)("message.friday_cultivation.golden_core." + this.kind.id + "_used"), (Object[])new Object[]{count, cultivationGained}) : Component.translatable((String)("message.friday_cultivation.golden_core." + this.kind.id + "_used"), (Object[])new Object[]{count});
        sp.displayClientMessage((Component)message.withStyle(ChatFormatting.LIGHT_PURPLE), true);
        return stack;
    }

    public static long cultivationGain(Kind kind) {
        return switch (kind) {
            case JIEDAN_PILL -> 100L;
            case CREATION_FRUIT -> 100L;
            default -> 0L;
        };
    }

    private static long addCultivationProgress(CultivationData data, long amount) {
        if (amount <= 0L) {
            return 0L;
        }
        long before = data.getCultivationProgress();
        data.setCultivationProgress(before + amount);
        return data.getCultivationProgress() - before;
    }

    public void appendHoverText(@NotNull ItemStack stack, @Nullable Level level, @NotNull List<Component> tooltip, @NotNull TooltipFlag flag) {
        TooltipUtils.addSection(tooltip, "tooltip.friday_cultivation.section.usage");
        tooltip.add((Component)TooltipUtils.descriptionLine((Component)Component.translatable((String)("tooltip.friday_cultivation." + this.kind.id))));
        TooltipUtils.addSection(tooltip, "tooltip.friday_cultivation.section.obtain");
        tooltip.add((Component)TooltipUtils.hintLine((Component)Component.translatable((String)("tooltip.friday_cultivation." + this.kind.id + ".obtain"))));
    }

    public static enum Kind {
        JIEDAN_PILL("jiedan_pill"),
        BLOOD_JIEDAN_PILL("blood_jiedan_pill"),
        TRUE_BLOOD("all_creatures_true_blood"),
        EARTH_EVIL_QI("earth_evil_qi"),
        HEAVEN_CLEAR_QI("heaven_clear_qi"),
        CREATION_FRUIT("ningzhen_creation_fruit"),
        BLOOD_TRANSFORMATION_TALISMAN("blood_transformation_talisman");

        final String id;

        private Kind(String id) {
            this.id = id;
        }
    }
}

