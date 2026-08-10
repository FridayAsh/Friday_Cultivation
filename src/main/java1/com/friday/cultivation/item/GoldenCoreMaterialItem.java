package com.friday.cultivation.item;

import com.friday.cultivation.CultivationCapability;
import com.friday.cultivation.capability.CultivationData;
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

/**
 * 金丹材料物品（筑基丹/血筑基丹/万灵真血/地煞阴气/天清气/凝真造化果/血化符箓）— 完整复刻原模组 GoldenCoreMaterialItem
 */
public class GoldenCoreMaterialItem extends Item {
    private static final int USE_TICKS = 32;
    public static final long JIEDAN_PILL_CULTIVATION_GAIN = 100L;
    public static final long CREATION_FRUIT_CULTIVATION_GAIN = 100L;
    private final Kind kind;

    public GoldenCoreMaterialItem(Item.Properties properties, Kind kind) {
        super(properties);
        this.kind = kind;
    }

    @Override
    public int getUseDuration(@NotNull ItemStack stack) {
        return USE_TICKS;
    }

    @NotNull
    @Override
    public UseAnim getUseAnimation(@NotNull ItemStack stack) {
        return UseAnim.EAT;
    }

    @NotNull
    @Override
    public InteractionResultHolder<ItemStack> use(@NotNull Level level, @NotNull Player player, @NotNull InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (this.kind == Kind.BLOOD_TRANSFORMATION_TALISMAN) {
            if (!level.isClientSide && player instanceof ServerPlayer) {
                ServerPlayer sp = (ServerPlayer) player;
                sp.displayClientMessage(Component.translatable("message.friday_cultivation.golden_core.blood_transformation_talisman_hint").withStyle(ChatFormatting.LIGHT_PURPLE), true);
            }
            return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
        }
        player.startUsingItem(hand);
        return InteractionResultHolder.consume(stack);
    }

    @NotNull
    @Override
    public ItemStack finishUsingItem(@NotNull ItemStack stack, @NotNull Level level, @NotNull LivingEntity entity) {
        if (level.isClientSide || !(entity instanceof ServerPlayer)) {
            return stack;
        }
        ServerPlayer sp = (ServerPlayer) entity;
        CultivationData ic = CultivationCapability.get(sp).orElse(null);
        if (ic == null) {
            return stack;
        }
        int count = switch (this.kind) {
            case JIEDAN_PILL -> ic.addJiedanPillUsed(1);
            case BLOOD_JIEDAN_PILL -> ic.addBloodJiedanPillUsed(1);
            case TRUE_BLOOD -> ic.addTrueBloodUsed(1);
            case EARTH_EVIL_QI -> ic.addEarthEvilQiUsed(1);
            case HEAVEN_CLEAR_QI -> ic.addHeavenClearQiUsed(1);
            case CREATION_FRUIT -> ic.addCreationFruitEaten(1);
            case BLOOD_TRANSFORMATION_TALISMAN -> 0;
        };
        long cultivationReward = cultivationGain(this.kind);
        long cultivationGained = addCultivationProgress(ic, cultivationReward);
        CapabilityEvents.syncToClient(sp);
        if (!sp.getAbilities().instabuild) {
            stack.shrink(1);
        }
        level.playSound(null, sp.blockPosition(), SoundEvents.PLAYER_LEVELUP, SoundSource.PLAYERS, 0.8f, 1.0f);
        MutableComponent message = cultivationReward > 0L
                ? Component.translatable("message.friday_cultivation.golden_core." + this.kind.id + "_used", count, cultivationGained)
                : Component.translatable("message.friday_cultivation.golden_core." + this.kind.id + "_used", count);
        sp.displayClientMessage(message.withStyle(ChatFormatting.LIGHT_PURPLE), true);
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

    @Override
    public void appendHoverText(@NotNull ItemStack stack, @Nullable Level level, @NotNull List<Component> tooltip, @NotNull TooltipFlag flag) {
        TooltipUtils.addSection(tooltip, "tooltip.friday_cultivation.section.usage");
        tooltip.add(TooltipUtils.descriptionLine(Component.translatable("tooltip.friday_cultivation." + this.kind.id)));
        TooltipUtils.addSection(tooltip, "tooltip.friday_cultivation.section.obtain");
        tooltip.add(TooltipUtils.hintLine(Component.translatable("tooltip.friday_cultivation." + this.kind.id + ".obtain")));
    }

    public enum Kind {
        JIEDAN_PILL("jiedan_pill"),
        BLOOD_JIEDAN_PILL("blood_jiedan_pill"),
        TRUE_BLOOD("all_creatures_true_blood"),
        EARTH_EVIL_QI("earth_evil_qi"),
        HEAVEN_CLEAR_QI("heaven_clear_qi"),
        CREATION_FRUIT("ningzhen_creation_fruit"),
        BLOOD_TRANSFORMATION_TALISMAN("blood_transformation_talisman");

        final String id;

        Kind(String id) {
            this.id = id;
        }
    }
}
