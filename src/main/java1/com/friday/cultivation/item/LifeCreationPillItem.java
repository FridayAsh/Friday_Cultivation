package com.friday.cultivation.item;

import com.friday.cultivation.alchemy.PillTier;
import com.friday.cultivation.util.TooltipUtils;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * 造化丹 — 完整复刻原模组 LifeCreationPillItem。
 * 不可食用，右键时显示提示。被动效果由其他系统处理（复活/造化）。
 */
public class LifeCreationPillItem extends PillItem {
    public LifeCreationPillItem(Item.Properties props) {
        super(props, PillTier.IMMORTAL, 0);
    }

    @Override
    @NotNull
    public InteractionResultHolder<ItemStack> use(@NotNull Level level, @NotNull Player player, @NotNull InteractionHand hand) {
        if (!level.isClientSide) {
            player.displayClientMessage(Component.translatable("message.friday_cultivation.pill.life_creation.passive_hint").withStyle(ChatFormatting.GOLD), true);
        }
        return InteractionResultHolder.pass(player.getItemInHand(hand));
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, @Nullable Level level, @NotNull List<Component> tooltip, @NotNull TooltipFlag flag) {
        tooltip.add(Component.translatable("pill_tier.friday_cultivation." + this.tier().id()).withStyle(this.tier().color()));
        TooltipUtils.addBlank(tooltip);
        TooltipUtils.addSection(tooltip, Component.translatable("tooltip.friday_cultivation.section.effect"));
        tooltip.add(TooltipUtils.effectLine(Component.translatable("tooltip.friday_cultivation.pill.life_creation.passive")));
        tooltip.add(TooltipUtils.positiveLine(Component.translatable("tooltip.friday_cultivation.pill.life_creation.heal_full")));
        tooltip.add(TooltipUtils.statsLine(Component.translatable("tooltip.friday_cultivation.pill.life_creation.qi_restore")));
        TooltipUtils.addBlank(tooltip);
        tooltip.add(TooltipUtils.hintLine(Component.translatable("tooltip.friday_cultivation.pill.life_creation.no_eat")));
    }
}
