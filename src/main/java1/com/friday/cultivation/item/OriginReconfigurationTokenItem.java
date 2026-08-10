package com.friday.cultivation.item;

import com.friday.cultivation.event.IdentityDrawHandler;
import com.friday.cultivation.util.TooltipUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * 出身重置令牌（严格照搬原模组 com.xiaoxiang.cultivation.item.OriginReconfigurationTokenItem）
 */
public class OriginReconfigurationTokenItem extends Item {

    public OriginReconfigurationTokenItem(Item.Properties properties) {
        super(properties);
    }

    @NotNull
    @Override
    public InteractionResultHolder<ItemStack> use(@NotNull Level level, @NotNull Player player, @NotNull InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (level.isClientSide()) {
            return InteractionResultHolder.pass(stack);
        }
        if (!(player instanceof ServerPlayer)) {
            return InteractionResultHolder.fail(stack);
        }
        ServerPlayer serverPlayer = (ServerPlayer)player;
        IdentityDrawHandler.openReconfiguration(serverPlayer);
        level.playSound(null, serverPlayer.blockPosition(), SoundEvents.ENDER_DRAGON_GROWL, SoundSource.PLAYERS, 0.65f, 1.35f);
        serverPlayer.getCooldowns().addCooldown((Item)this, 20);
        serverPlayer.sendSystemMessage(Component.translatable("message.friday_cultivation.origin_reconfiguration_token.opened").withStyle(ChatFormatting.AQUA));
        return InteractionResultHolder.success(stack);
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, @Nullable Level level, @NotNull List<Component> tooltip, @NotNull TooltipFlag flag) {
        TooltipUtils.addSection(tooltip, "tooltip.friday_cultivation.section.usage");
        tooltip.add(TooltipUtils.descriptionLine(Component.translatable("tooltip.friday_cultivation.origin_reconfiguration_token")));
        TooltipUtils.addBlank(tooltip);
        TooltipUtils.addSection(tooltip, "tooltip.friday_cultivation.section.warning");
        tooltip.add(TooltipUtils.warningLine(Component.translatable("tooltip.friday_cultivation.origin_reconfiguration_token.warning")));
        tooltip.add(TooltipUtils.hintLine(Component.translatable("tooltip.friday_cultivation.origin_reconfiguration_token.hint")));
    }
}