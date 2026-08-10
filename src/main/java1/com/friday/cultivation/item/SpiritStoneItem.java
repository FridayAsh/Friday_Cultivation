package com.friday.cultivation.item;

import com.friday.cultivation.CultivationCapability;
import com.friday.cultivation.ItemTier;
import com.friday.cultivation.event.CapabilityEvents;
import com.friday.cultivation.spirit.QiElement;
import com.friday.cultivation.util.TooltipUtils;
import java.util.List;
import net.minecraft.network.chat.Component;
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

public class SpiritStoneItem
extends Item {
    private static final int USE_TICKS = 32;
    private final ItemTier tier;
    private final int qiAmount;

    public SpiritStoneItem(Item.Properties props, ItemTier tier, int qiAmount) {
        super(props);
        this.tier = tier;
        this.qiAmount = qiAmount;
    }

    public ItemTier tier() {
        return this.tier;
    }

    public int qiAmount() {
        return this.qiAmount;
    }

    @Override
    public int getUseDuration(@NotNull ItemStack stack) {
        return 32;
    }

    @NotNull
    @Override
    public UseAnim getUseAnimation(@NotNull ItemStack stack) {
        return UseAnim.EAT;
    }

    @NotNull
    @Override
    public InteractionResultHolder<ItemStack> use(@NotNull Level level, @NotNull Player player, @NotNull InteractionHand hand) {
        player.startUsingItem(hand);
        return InteractionResultHolder.consume(player.getItemInHand(hand));
    }

    @NotNull
    @Override
    public ItemStack finishUsingItem(@NotNull ItemStack stack, @NotNull Level level, @NotNull LivingEntity user) {
        if (!level.isClientSide() && user instanceof ServerPlayer) {
            ServerPlayer player = (ServerPlayer) user;
            CultivationCapability.get(player).ifPresent(data -> {
                long beforeCultivation = data.getCultivationProgress();
                int qiGained = data.absorbQi(this.qiAmount, QiElement.PURE);
                long cultivationGained = data.getCultivationProgress() - beforeCultivation;
                CapabilityEvents.syncToClient(player);
                player.displayClientMessage(Component.translatable("message.friday_cultivation.spirit_stone.consumed", cultivationGained, qiGained, this.tier.displayName()), true);
            });
            level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.PLAYER_LEVELUP, SoundSource.PLAYERS, 0.4f, 1.6f);
        }
        if (user instanceof Player) {
            Player p = (Player) user;
            if (!p.getAbilities().instabuild) {
                stack.shrink(1);
            }
        }
        return stack;
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, @Nullable Level level, @NotNull List<Component> tooltip, @NotNull TooltipFlag flag) {
        tooltip.add(TooltipUtils.tierElementLine(this.tier, QiElement.PURE));
        TooltipUtils.addBlank(tooltip);
        TooltipUtils.addSection(tooltip, "tooltip.friday_cultivation.section.effect");
        tooltip.add(TooltipUtils.positiveLine(Component.translatable("tooltip.friday_cultivation.spirit_stone.eat", this.qiAmount)));
        TooltipUtils.addBlank(tooltip);
        tooltip.add(TooltipUtils.hintLine(Component.translatable("tooltip.friday_cultivation.spirit_stone.hint")));
    }
}
