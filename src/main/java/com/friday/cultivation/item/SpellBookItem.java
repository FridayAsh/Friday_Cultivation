/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.ChatFormatting
 *  net.minecraft.network.chat.Component
 *  net.minecraft.network.chat.MutableComponent
 *  net.minecraft.server.level.ServerPlayer
 *  net.minecraft.sounds.SoundEvents
 *  net.minecraft.sounds.SoundSource
 *  net.minecraft.world.InteractionHand
 *  net.minecraft.world.InteractionResultHolder
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.item.Item
 *  net.minecraft.world.item.Item$Properties
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.item.TooltipFlag
 *  net.minecraft.world.level.Level
 *  org.jetbrains.annotations.NotNull
 *  org.jetbrains.annotations.Nullable
 */
package com.friday.cultivation.item;

import com.friday.cultivation.cultivation.CultivationCapability;
import com.friday.cultivation.cultivation.spell.Spell;
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
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class SpellBookItem
extends Item {
    private final Spell spell;

    public SpellBookItem(Item.Properties props, Spell spell) {
        super(props);
        this.spell = spell;
    }

    public Spell spell() {
        return this.spell;
    }

    @NotNull
    public InteractionResultHolder<ItemStack> appendHoverText(@NotNull Level level, @NotNull Player player, @NotNull InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (level.isClientSide) {
            return InteractionResultHolder.success(stack);
        }
        if (!(player instanceof ServerPlayer)) {
            return InteractionResultHolder.pass(stack);
        }
        ServerPlayer sp = (ServerPlayer)player;
        boolean[] success = new boolean[]{false};
        CultivationCapability.get((Player)sp).ifPresent(data -> {
            if (data.hasSpell(this.spell)) {
                sp.displayClientMessage((Component)Component.translatable((String)"message.friday_cultivation.spell_book.already_learned", (Object[])new Object[]{this.spell.displayNameForRealm(data.getRealm())}), true);
                return;
            }
            if (this.spell == Spell.SOUL_HOOK && !data.isGhostCultivator() && !data.isSoulState()) {
                sp.displayClientMessage((Component)Component.translatable((String)"message.friday_cultivation.soul_hook_spell.requirement"), true);
                return;
            }
            data.learnSpell(this.spell);
            CapabilityEvents.syncToClient(sp);
            sp.displayClientMessage((Component)Component.translatable((String)"message.friday_cultivation.spell_book.learned", (Object[])new Object[]{this.spell.displayNameForRealm(data.getRealm())}), false);
            success[0] = true;
        });
        if (success[0]) {
            level.playSound(null, sp.getX(), sp.getY(), sp.getZ(), SoundEvents.PLAYER_LEVELUP, SoundSource.PLAYERS, 0.5f, 1.4f);
            if (!sp.getAbilities().instabuild) {
                stack.shrink(1);
            }
        }
        player.getCooldowns().addCooldown((Item)this, 10);
        return InteractionResultHolder.consume(stack);
    }

    public void appendHoverText(@NotNull ItemStack stack, @Nullable Level level, @NotNull List<Component> tooltip, @NotNull TooltipFlag flag) {
        MutableComponent typeLabel = this.spell.type().displayName().copy().withStyle(ChatFormatting.GRAY);
        tooltip.add((Component)TooltipUtils.tierElementLine(this.spell.tier(), this.spell.element()).append((Component)Component.literal((String)"  |  ").withStyle(ChatFormatting.DARK_GRAY)).append((Component)typeLabel));
        if (this.spell.isSwordSpell()) {
            tooltip.add((Component)TooltipUtils.effectLine((Component)Component.translatable((String)"spell.friday_cultivation.tag.sword_spell")));
        }
        TooltipUtils.addBlank(tooltip);
        TooltipUtils.addSection(tooltip, "tooltip.friday_cultivation.section.contents");
        tooltip.add((Component)TooltipUtils.statsLine((Component)Component.translatable((String)"tooltip.friday_cultivation.spell_book.contains", (Object[])new Object[]{TooltipUtils.tieredName(this.spell.displayName(), this.spell.tier())})));
        TooltipUtils.addSection(tooltip, "tooltip.friday_cultivation.section.effect");
        tooltip.add((Component)TooltipUtils.descriptionLine(this.spell.description()));
        this.spell.appendExtraEffectTooltip(tooltip);
        TooltipUtils.addSection(tooltip, "tooltip.friday_cultivation.section.cast");
        this.spell.appendCastTooltip(tooltip);
        TooltipUtils.addBlank(tooltip);
        tooltip.add((Component)TooltipUtils.hintLine((Component)Component.translatable((String)"tooltip.friday_cultivation.spell_book.use_hint")));
    }
}

