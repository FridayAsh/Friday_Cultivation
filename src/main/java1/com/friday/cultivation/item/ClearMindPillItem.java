package com.friday.cultivation.item;

import com.friday.cultivation.alchemy.PillTier;
import com.friday.cultivation.registry.ModEffects;
import com.friday.cultivation.util.TooltipUtils;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * 清心丹 — 完整复刻原模组 ClearMindPillItem。
 * 食用后清除所有负面效果，并施加清心效果（防止再次中负面效果）。
 */
public class ClearMindPillItem extends PillItem {
    public ClearMindPillItem(Item.Properties props, PillTier tier) {
        super(props, tier, 0);
    }

    @Override
    @NotNull
    public ItemStack finishUsingItem(@NotNull ItemStack stack, @NotNull Level level, @NotNull LivingEntity user) {
        if (!level.isClientSide && user instanceof ServerPlayer) {
            ServerPlayer player = (ServerPlayer) user;
            int durationSec = switch (this.tier().ordinal()) {
                case 0 -> 30; case 1 -> 120; case 2 -> 300; case 3 -> 600; default -> 0;
            };
            int durationTicks = durationSec * 20;
            player.removeAllEffects();
            player.addEffect(new MobEffectInstance(ModEffects.CLEAR_MIND.get(), durationTicks, 0));
            level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 0.8f, 1.5f);
            player.displayClientMessage(Component.translatable("message.friday_cultivation.pill.clear_mind.consumed", this.tier().displayName()).withStyle(ChatFormatting.AQUA), true);
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
        int tierIdx = this.tier().ordinal();
        String key = switch (tierIdx) {
            case 0 -> ".pill.clear_mind.low";
            case 1 -> ".pill.clear_mind.mid";
            case 2 -> ".pill.clear_mind.high";
            case 3 -> ".pill.clear_mind.supreme";
            default -> "";
        };
        tooltip.add(TooltipUtils.positiveLine(Component.translatable("tooltip.friday_cultivation" + key)));
        TooltipUtils.addBlank(tooltip);
        tooltip.add(TooltipUtils.hintLine(Component.translatable("tooltip.friday_cultivation.pill.hint")));
    }
}
