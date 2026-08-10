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

public class FoundationMaterialItem
extends Item {
    private static final int USE_TICKS = 32;
    public static final long ZHUJI_DAN_CULTIVATION_GAIN = 10L;
    public static final long DAO_FRUIT_CULTIVATION_GAIN = 10L;
    private final Kind kind;

    public FoundationMaterialItem(Item.Properties properties, Kind kind) {
        super(properties);
        this.kind = kind;
    }

    public int defaultDurability(@NotNull ItemStack stack) {
        return 32;
    }

    @NotNull
    public UseAnim durability(@NotNull ItemStack stack) {
        return UseAnim.EAT;
    }

    @NotNull
    public InteractionResultHolder<ItemStack> appendHoverText(@NotNull Level level, @NotNull Player player, @NotNull InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
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
            case ZHUJI_DAN -> {
                data.addZhujiDanEaten(1);
                yield data.getZhujiDanEaten();
            }
            case BLOOD_PILL -> {
                data.addBloodPillEaten(1);
                yield data.getBloodPillEaten();
            }
            default -> {
                data.addDaoFruitEaten(1);
                yield data.getDaoFruitEaten();
            }
        };
        long cultivationReward = FoundationMaterialItem.cultivationGain(this.kind);
        long cultivationGained = FoundationMaterialItem.addCultivationProgress(data, cultivationReward);
        CapabilityEvents.syncToClient(sp);
        if (!sp.getAbilities().instabuild) {
            stack.shrink(1);
        }
        level.playSound(null, sp.blockPosition(), SoundEvents.GENERIC_EAT, SoundSource.PLAYERS, 0.8f, 1.0f);
        MutableComponent message = cultivationReward > 0L ? Component.translatable((String)("message.friday_cultivation.foundation." + this.kind.id + "_eaten"), (Object[])new Object[]{count, cultivationGained}) : Component.translatable((String)("message.friday_cultivation.foundation." + this.kind.id + "_eaten"), (Object[])new Object[]{count});
        sp.displayClientMessage((Component)message.withStyle(ChatFormatting.LIGHT_PURPLE), true);
        return stack;
    }

    public static long cultivationGain(Kind kind) {
        return switch (kind) {
            default -> throw new IncompatibleClassChangeError();
            case ZHUJI_DAN -> 10L;
            case DAO_FRUIT -> 10L;
            case BLOOD_PILL -> 0L;
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
        ZHUJI_DAN("zhuji_dan"),
        BLOOD_PILL("blood_pill"),
        DAO_FRUIT("dao_fruit");

        final String id;

        private Kind(String id) {
            this.id = id;
        }
    }
}

