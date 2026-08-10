package com.friday.cultivation.item;

import com.friday.cultivation.CultivationCapability;
import com.friday.cultivation.alchemy.PillEffectSpecs;
import com.friday.cultivation.alchemy.PillTier;
import com.friday.cultivation.event.CapabilityEvents;
import com.friday.cultivation.util.TooltipUtils;
import java.util.List;
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
 * 丹药基类 — 完整复刻原模组 PillItem。
 * 使用长按右键食用（20 ticks = 1秒），食用后：
 *   - 增加灵气（qiAmount > 0 时补充灵气，qiAmount < 0 时补满）
 *   - 扣 1 个物品（创造模式不扣）
 *   - 播放食用音效
 *   - 显示消息
 *
 * 子类可以重写 finishUsingItem 实现特殊效果（血燃/神行/清心等）。
 */
public class PillItem extends Item {
    private static final int USE_TICKS = 20;
    private final PillTier tier;
    private final int qiAmount;

    public PillItem(Item.Properties props, PillTier tier, int qiAmount) {
        super(props);
        this.tier = tier;
        this.qiAmount = qiAmount;
    }

    public PillTier tier() {
        return this.tier;
    }

    public int qiAmount() {
        return this.qiAmount;
    }

    @Override
    public int getUseDuration(ItemStack stack) {
        return 20;
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.EAT;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        player.startUsingItem(hand);
        return InteractionResultHolder.consume(player.getItemInHand(hand));
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity user) {
        if (!level.isClientSide() && user instanceof ServerPlayer) {
            ServerPlayer player = (ServerPlayer) user;
            CultivationCapability.get(player).ifPresent(data -> {
                int gained;
                int effectiveQiAmount = PillEffectSpecs.qiAmount(this, this.qiAmount);
                if (effectiveQiAmount < 0) {
                    long need = data.getMaxQi() - data.getCurrentQi();
                    gained = (int) Math.min(Integer.MAX_VALUE, need);
                    data.setCurrentQi(data.getMaxQi());
                } else {
                    long before = data.getCurrentQi();
                    data.setCurrentQi(before + (long) effectiveQiAmount);
                    gained = (int) Math.min(Integer.MAX_VALUE, data.getCurrentQi() - before);
                }
                CapabilityEvents.syncToClient(player);
                player.sendSystemMessage(Component.translatable("message.friday_cultivation.pill.consumed", gained, this.tier.displayName()), true);
            });
            level.playSound(null, user.getX(), user.getY(), user.getZ(), SoundEvents.GENERIC_EAT, SoundSource.PLAYERS, 0.5f, 1.4f);
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
    public Component getName(ItemStack stack) {
        return Component.translatable(this.getDescriptionId(stack)).withStyle(this.tier.color());
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("pill_tier.friday_cultivation." + this.tier.id()).withStyle(this.tier.color()));
        TooltipUtils.addBlank(tooltip);
        TooltipUtils.addSection(tooltip, "tooltip.friday_cultivation.section.effect");
        int effectiveQiAmount = PillEffectSpecs.qiAmount(this, this.qiAmount);
        MutableComponent statLine = effectiveQiAmount < 0
                ? Component.translatable("tooltip.friday_cultivation.pill.refill_full")
                : Component.translatable("tooltip.friday_cultivation.pill.qi_gain", effectiveQiAmount);
        tooltip.add(TooltipUtils.positiveLine(statLine));
        TooltipUtils.addBlank(tooltip);
        tooltip.add(TooltipUtils.hintLine(Component.translatable("tooltip.friday_cultivation.pill.hint")));
    }
}
