package com.friday.cultivation.item;

import com.friday.cultivation.CultivationCapability;
import com.friday.cultivation.capability.CultivationData;
import com.friday.cultivation.event.CapabilityEvents;
import com.friday.cultivation.technique.Technique;
import com.friday.cultivation.technique.TechniqueLoadoutHelper;
import com.friday.cultivation.spirit.QiElement;
import com.friday.cultivation.spirit.SpiritRootBonusHelper;
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
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * 功法书物品 — 右键学习功法
 * 复刻自原模组 com.xiaoxiang.cultivation.item.TechniqueBookItem
 */
public class TechniqueBookItem extends Item {
    private final Technique technique;

    public TechniqueBookItem(Item.Properties props, Technique technique) {
        super(props.stacksTo(1));
        this.technique = technique;
    }

    public Technique technique() {
        return this.technique;
    }

    @Override
    public @NotNull Rarity getRarity(@NotNull ItemStack stack) {
        return switch (technique.tier()) {
            case SUPREME -> Rarity.EPIC;
            case IMMORTAL -> Rarity.EPIC;
            case HIGH -> Rarity.RARE;
            default -> Rarity.UNCOMMON;
        };
    }

    @Override
    public @NotNull InteractionResultHolder<ItemStack> use(@NotNull Level level, @NotNull Player player, @NotNull InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (level.isClientSide()) {
            return InteractionResultHolder.success(stack);
        }
        if (!(player instanceof ServerPlayer sp)) {
            return InteractionResultHolder.fail(stack);
        }

        CultivationData ic = CultivationCapability.get(sp).orElse(null);
        if (ic == null) {
            return InteractionResultHolder.fail(stack);
        }

        if (ic.getLearnedTechniques().contains(this.technique.id())) {
            sp.displayClientMessage(
                    Component.translatable("message.friday_cultivation.technique_book.already_learned",
                            this.technique.displayName()), true);
            return InteractionResultHolder.fail(stack);
        }

        if (!SpiritRootBonusHelper.canLearnTechnique(sp, this.technique)) {
            sp.displayClientMessage(
                    Component.translatable("message.friday_cultivation.technique_book.requirement_not_met",
                            this.technique.displayName()), true);
            return InteractionResultHolder.fail(stack);
        }

        ic.learnTechnique(this.technique.id());

        Technique current = Technique.byId(ic.getEquippedTechniqueId());
        boolean currentCompatible = current != null && ic.getLearnedTechniques().contains(current.id())
                && TechniqueLoadoutHelper.canEquipForCurrentState(ic, current);
        boolean canEquipNow = TechniqueLoadoutHelper.canEquipForCurrentState(ic, this.technique);
        boolean autoEquipped = canEquipNow && (!ic.hasEquippedTechnique() || !currentCompatible);

        if (autoEquipped) {
            ic.setEquippedTechniqueId(this.technique.id());
        }

        CapabilityEvents.syncToClient(sp);

        String messageKey = autoEquipped
                ? "message.friday_cultivation.technique_book.learned_equipped"
                : (canEquipNow
                    ? "message.friday_cultivation.technique_book.learned"
                    : "message.friday_cultivation.technique_book.learned_incompatible_state");
        sp.sendSystemMessage(Component.translatable(messageKey, this.technique.displayName()));

        level.playSound(null, sp.getX(), sp.getY(), sp.getZ(),
                SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.PLAYERS, 0.5f, 1.2f);

        if (!sp.getAbilities().instabuild) {
            stack.shrink(1);
        }

        player.getCooldowns().addCooldown(this, 10);
        return InteractionResultHolder.consume(stack);
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, @Nullable Level level,
                                @NotNull List<Component> tooltip, @NotNull TooltipFlag flag) {
        // 品阶 + 元素（PURE 不显示元素）
        Component tierComp = Component.translatable("tooltip.friday_cultivation.tier." + technique.tier().name().toLowerCase())
                .withStyle(getTierColor(technique.tier()));
        if (technique.primaryElement() == QiElement.PURE) {
            tooltip.add(tierComp);
        } else {
            tooltip.add(tierComp.copy()
                    .append(Component.literal(" · ").withStyle(ChatFormatting.GRAY))
                    .append(Component.translatable("element.friday_cultivation." + technique.primaryElement().name().toLowerCase())
                            .withStyle(ChatFormatting.GRAY)));
        }
        tooltip.add(Component.empty());
        // 内容
        tooltip.add(Component.translatable("tooltip.friday_cultivation.section.contents").withStyle(ChatFormatting.YELLOW));
        tooltip.add(Component.literal("  ")
                .append(Component.translatable("tooltip.friday_cultivation.technique_book.contains",
                        technique.displayName().copy().withStyle(getTierColor(technique.tier())))));
        tooltip.add(Component.literal("  ")
                .append(Component.translatable("tooltip.friday_cultivation.technique_book.path",
                        Component.translatable("dao_path.friday_cultivation." + technique.daoPath().id()))));
        // 效果
        tooltip.add(Component.empty());
        tooltip.add(Component.translatable("tooltip.friday_cultivation.section.effect").withStyle(ChatFormatting.YELLOW));
        tooltip.add(Component.literal("  ").append(technique.description().copy().withStyle(ChatFormatting.GRAY)));
        tooltip.add(Component.empty());
        tooltip.add(Component.translatable("tooltip.friday_cultivation.technique_book.use_hint")
                .withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC));
    }

    private static ChatFormatting getTierColor(Technique.Tier tier) {
        return switch (tier) {
            case IMMORTAL -> ChatFormatting.GOLD;
            case SUPREME -> ChatFormatting.RED;
            case HIGH -> ChatFormatting.LIGHT_PURPLE;
            case MID -> ChatFormatting.AQUA;
            case LOW -> ChatFormatting.GREEN;
        };
    }
}
