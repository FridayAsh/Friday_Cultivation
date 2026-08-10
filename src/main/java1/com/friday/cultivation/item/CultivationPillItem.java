package com.friday.cultivation.item;

import com.friday.cultivation.alchemy.PillTier;
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
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * 修为丹 — 完整复刻原模组 CultivationPillItem。
 * 食用后增加修为（境界进度）。
 */
public class CultivationPillItem extends PillItem {
    public CultivationPillItem(Item.Properties props, PillTier tier, int qiAmount) {
        super(props, tier, qiAmount);
    }

    @Override
    @NotNull
    public ItemStack finishUsingItem(@NotNull ItemStack stack, @NotNull Level level, @NotNull LivingEntity user) {
        if (!level.isClientSide && user instanceof ServerPlayer) {
            ServerPlayer player = (ServerPlayer) user;
            CultivationData ic = CultivationCapability.get(player).orElse(null);
            if (ic != null) {
                long gain = switch (this.tier()) {
                    case LOW -> 100L;
                    case MID -> 1000L;
                    case HIGH -> 10000L;
                    case SUPREME -> 100000L;
                    case IMMORTAL -> 1000000L;
                };
                ic.addCultivationProgress(gain);
                CapabilityEvents.syncToClient(player);
                player.displayClientMessage(Component.translatable("message.friday_cultivation.pill.cultivation.consumed", this.tier().displayName()).withStyle(ChatFormatting.GOLD), true);
            }
            level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.PLAYER_LEVELUP, SoundSource.PLAYERS, 0.5f, 1.6f);
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
        tooltip.add(Component.translatable("pill_tier.friday_cultivation." + this.tier().id()).withStyle(this.tier().color()));
        TooltipUtils.addBlank(tooltip);
        TooltipUtils.addSection(tooltip, Component.translatable("tooltip.friday_cultivation.section.effect"));
        long gain = switch (this.tier()) {
            case LOW -> 100L;
            case MID -> 1000L;
            case HIGH -> 10000L;
            case SUPREME -> 100000L;
            case IMMORTAL -> 1000000L;
        };
        tooltip.add(TooltipUtils.positiveLine(Component.translatable("tooltip.friday_cultivation.pill.cultivation.gain", gain)));
        TooltipUtils.addBlank(tooltip);
        tooltip.add(TooltipUtils.hintLine(Component.translatable("tooltip.friday_cultivation.pill.hint")));
    }
}
