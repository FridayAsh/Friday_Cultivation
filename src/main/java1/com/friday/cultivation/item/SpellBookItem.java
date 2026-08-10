package com.friday.cultivation.item;

import com.friday.cultivation.CultivationCapability;
import com.friday.cultivation.capability.CultivationData;
import com.friday.cultivation.ItemTier;
import com.friday.cultivation.spell.Spell;
import com.friday.cultivation.spell.SpellElement;
import com.friday.cultivation.spell.SpellType;
import com.friday.cultivation.event.CapabilityEvents;
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
 * 法术书物品 — 右键学习法术
 * 复刻自原模组 com.xiaoxiang.cultivation.item.SpellBookItem
 */
public class SpellBookItem extends Item {
    private final Spell spell;

    public SpellBookItem(Item.Properties props, Spell spell) {
        super(props.stacksTo(1));
        this.spell = spell;
    }

    public Spell spell() { return this.spell; }

    @Override
    public @NotNull Rarity getRarity(@NotNull ItemStack stack) {
        return switch (spell.tier()) {
            case IMMORTAL -> Rarity.EPIC;
            case SUPREME -> Rarity.RARE;
            case HIGH -> Rarity.UNCOMMON;
            default -> Rarity.COMMON;
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

        if (ic.hasSpell(this.spell.id())) {
            if (this.spell.isPassive()) {
                // 被动法术：学会后时刻生效，不可装备到使用槽位
                sp.displayClientMessage(
                        Component.translatable("message.friday_cultivation.spell_book.passive_auto",
                                this.spell.displayName()), true);
            } else {
                // 主动法术：已学会 → 装备到当前选中槽位
                ic.setEquippedSpellAt(ic.getSelectedSpellSlot(), this.spell.id());
                CapabilityEvents.syncToClient(sp);
                sp.displayClientMessage(
                        Component.translatable("message.friday_cultivation.spell_book.equipped",
                                this.spell.displayName(), ic.getSelectedSpellSlot() + 1), true);
            }
            player.getCooldowns().addCooldown(this, 10);
            return InteractionResultHolder.consume(stack);
        }

        ic.learnSpell(this.spell.id());
        CapabilityEvents.syncToClient(sp);
        sp.sendSystemMessage(Component.translatable(
                "message.friday_cultivation.spell_book.learned", this.spell.displayName()));

        level.playSound(null, sp.getX(), sp.getY(), sp.getZ(),
                SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.PLAYERS, 0.5f, 1.4f);

        if (!sp.getAbilities().instabuild) {
            stack.shrink(1);
        }

        player.getCooldowns().addCooldown(this, 10);
        return InteractionResultHolder.consume(stack);
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, @Nullable Level level,
                                @NotNull List<Component> tooltip, @NotNull TooltipFlag flag) {
        // 品阶 + 元素 + 类型
        Component tierComp = Component.translatable("tooltip.friday_cultivation.tier." + spell.tier().id())
                .withStyle(tierColor(spell.tier()));
        Component typeComp = Component.translatable("spell_type.friday_cultivation." + spell.type().id())
                .withStyle(ChatFormatting.GRAY);
        if (spell.element() == SpellElement.NONE) {
            tooltip.add(tierComp.copy().append(Component.literal("  |  ").withStyle(ChatFormatting.DARK_GRAY)).append(typeComp));
        } else {
            tooltip.add(tierComp.copy()
                    .append(Component.literal("  |  ").withStyle(ChatFormatting.DARK_GRAY))
                    .append(Component.translatable("spell_element.friday_cultivation." + spell.element().id())
                            .withStyle(ChatFormatting.GRAY))
                    .append(Component.literal("  |  ").withStyle(ChatFormatting.DARK_GRAY))
                    .append(typeComp));
        }
        if (spell.isSwordSpell()) {
            tooltip.add(Component.literal("  ")
                    .append(Component.translatable("spell.friday_cultivation.tag.sword_spell"))
                    .withStyle(ChatFormatting.LIGHT_PURPLE));
        }
        if (spell.isBloodSpell()) {
            tooltip.add(Component.literal("  ")
                    .append(Component.translatable("spell.friday_cultivation.tag.blood_spell"))
                    .withStyle(ChatFormatting.LIGHT_PURPLE));
        }
        tooltip.add(Component.empty());
        // 内容
        tooltip.add(Component.translatable("tooltip.friday_cultivation.section.contents").withStyle(ChatFormatting.YELLOW));
        tooltip.add(Component.literal("  ")
                .append(Component.translatable("tooltip.friday_cultivation.spell_book.contains",
                        spell.displayName().copy().withStyle(tierColor(spell.tier())))));
        // 效果
        tooltip.add(Component.empty());
        tooltip.add(Component.translatable("tooltip.friday_cultivation.section.effect").withStyle(ChatFormatting.YELLOW));
        tooltip.add(Component.literal("  ").append(spell.description().copy().withStyle(ChatFormatting.GRAY)));
        // 施法
        if (spell.type() == SpellType.ACTIVE && spell.qiCost() > 0) {
            tooltip.add(Component.empty());
            tooltip.add(Component.translatable("tooltip.friday_cultivation.section.cast").withStyle(ChatFormatting.YELLOW));
            tooltip.add(Component.literal("  ")
                    .append(Component.translatable("tooltip.friday_cultivation.spell_book.qi_cost", spell.qiCost()))
                    .withStyle(ChatFormatting.AQUA));
            if (spell.damage() > 0) {
                tooltip.add(Component.literal("  ")
                        .append(Component.translatable("tooltip.friday_cultivation.spell_book.damage", spell.damage()))
                        .withStyle(ChatFormatting.AQUA));
            }
        }
        tooltip.add(Component.empty());
        tooltip.add(Component.translatable("tooltip.friday_cultivation.spell_book.use_hint")
                .withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC));
        tooltip.add(Component.translatable("tooltip.friday_cultivation.spell_book.equip_hint")
                .withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC));
    }

    private static ChatFormatting tierColor(ItemTier tier) {
        return switch (tier) {
            case IMMORTAL -> ChatFormatting.GOLD;
            case SUPREME -> ChatFormatting.RED;
            case HIGH -> ChatFormatting.LIGHT_PURPLE;
            case MID -> ChatFormatting.AQUA;
            case LOW -> ChatFormatting.GREEN;
        };
    }
}
