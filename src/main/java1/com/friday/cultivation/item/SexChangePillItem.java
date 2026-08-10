package com.friday.cultivation.item;

import com.friday.cultivation.CultivationCapability;
import com.friday.cultivation.capability.CultivationData;
import com.friday.cultivation.event.CapabilityEvents;
import com.friday.cultivation.util.TooltipUtils;
import java.util.List;
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

/**
 * 变性丹 — 完整复刻原模组 SexChangePillItem。
 * 右键使用，切换玩家性别。
 */
public class SexChangePillItem extends Item {
    public SexChangePillItem(Item.Properties properties) {
        super(properties);
    }

    @NotNull
    @Override
    public InteractionResultHolder<ItemStack> use(@NotNull Level level, @NotNull Player player, @NotNull InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (level.isClientSide) {
            return InteractionResultHolder.success(stack);
        }
        if (!(player instanceof ServerPlayer)) {
            return InteractionResultHolder.consume(stack);
        }
        ServerPlayer sp = (ServerPlayer) player;
        CultivationData ic = CultivationCapability.get(player).orElse(null);
        if (ic != null) {
            ic.setGender(ic.getGender() == 2 ? 1 : 2);
            CapabilityEvents.syncToClient(sp);
            stack.shrink(1);
            level.playSound(null, sp.getX(), sp.getY(), sp.getZ(), SoundEvents.PLAYER_LEVELUP, SoundSource.PLAYERS, 0.8f, 1.1f);
            String genderKey = ic.getGender() == 2 ? "screen.friday_cultivation.gender.female" : "screen.friday_cultivation.gender.male";
            sp.displayClientMessage(Component.translatable("message.friday_cultivation.sex_change_pill.used", Component.translatable(genderKey)).withStyle(ChatFormatting.LIGHT_PURPLE), true);
            return InteractionResultHolder.consume(stack);
        }
        return InteractionResultHolder.consume(stack);
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, @Nullable Level level, @NotNull List<Component> tooltip, @NotNull TooltipFlag flag) {
        TooltipUtils.addSection(tooltip, Component.translatable("tooltip.friday_cultivation.section.usage"));
        tooltip.add(TooltipUtils.descriptionLine(Component.translatable("tooltip.friday_cultivation.sex_change_pill")));
    }
}
