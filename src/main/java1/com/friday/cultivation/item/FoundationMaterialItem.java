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
 * 筑基期材料物品（筑基丹/血灵丹/道基果）— 完整复刻原模组 FoundationMaterialItem
 */
public class FoundationMaterialItem extends Item {
    private static final int USE_TICKS = 32;
    public static final long ZHUJI_DAN_CULTIVATION_GAIN = 10L;
    public static final long DAO_FRUIT_CULTIVATION_GAIN = 10L;
    private final Kind kind;

    public FoundationMaterialItem(Item.Properties properties, Kind kind) {
        super(properties);
        this.kind = kind;
    }

    @Override
    public int getUseDuration(@NotNull ItemStack stack) { return USE_TICKS; }

    @NotNull
    @Override
    public UseAnim getUseAnimation(@NotNull ItemStack stack) { return UseAnim.EAT; }

    @NotNull
    @Override
    public InteractionResultHolder<ItemStack> use(@NotNull Level level, @NotNull Player player, @NotNull InteractionHand hand) {
        player.startUsingItem(hand);
        return InteractionResultHolder.consume(player.getItemInHand(hand));
    }

    @NotNull
    @Override
    public ItemStack finishUsingItem(@NotNull ItemStack stack, @NotNull Level level, @NotNull LivingEntity entity) {
        if (level.isClientSide || !(entity instanceof ServerPlayer)) return stack;
        ServerPlayer sp = (ServerPlayer) entity;
        CultivationData ic = CultivationCapability.get(sp).orElse(null);
        if (ic == null) return stack;
        int count = switch (this.kind) {
            case ZHUJI_DAN -> { ic.addZhujiDanEaten(1); yield ic.getZhujiDanEaten(); }
            case BLOOD_PILL -> { ic.addBloodPillEaten(1); yield ic.getBloodPillEaten(); }
            case DAO_FRUIT -> { ic.addDaoFruitEaten(1); yield ic.getDaoFruitEaten(); }
        };
        long cultivationReward = cultivationGain(this.kind);
        long cultivationGained = addCultivationProgress(ic, cultivationReward);
        CapabilityEvents.syncToClient(sp);
        if (!sp.getAbilities().instabuild) stack.shrink(1);
        level.playSound(null, sp.blockPosition(), SoundEvents.PLAYER_LEVELUP, SoundSource.PLAYERS, 0.8f, 1.0f);
        MutableComponent message = cultivationReward > 0L
                ? Component.translatable("message.friday_cultivation.foundation." + this.kind.id + "_eaten", count, cultivationGained)
                : Component.translatable("message.friday_cultivation.foundation." + this.kind.id + "_eaten", count);
        sp.displayClientMessage(message.withStyle(ChatFormatting.LIGHT_PURPLE), true);
        return stack;
    }

    public static long cultivationGain(Kind kind) {
        return switch (kind) {
            case ZHUJI_DAN -> 10L;
            case DAO_FRUIT -> 10L;
            case BLOOD_PILL -> 0L;
        };
    }

    private static long addCultivationProgress(CultivationData data, long amount) {
        if (amount <= 0L) return 0L;
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
        ZHUJI_DAN("zhuji_dan"),
        BLOOD_PILL("blood_pill"),
        DAO_FRUIT("dao_fruit");

        final String id;
        Kind(String id) { this.id = id; }
    }
}
