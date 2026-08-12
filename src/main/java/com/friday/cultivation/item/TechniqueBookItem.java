/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.network.chat.Component
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
import com.friday.cultivation.cultivation.SpiritRootBonusHelper;
import com.friday.cultivation.cultivation.technique.Technique;
import com.friday.cultivation.cultivation.technique.TechniqueLoadoutHelper;
import com.friday.cultivation.event.CapabilityEvents;
import com.friday.cultivation.util.ShimmerColors;
import com.friday.cultivation.util.TooltipUtils;
import java.util.List;
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

public class TechniqueBookItem
extends Item {
    private final Technique technique;

    public TechniqueBookItem(Item.Properties props, Technique technique) {
        super(props);
        this.technique = technique;
    }

    public Technique technique() {
        return this.technique;
    }

    private static final String MYSTERY_NAME = "????????"; // 帝法未自创：乱码动态闪烁（§k 混淆 + 金红流动色）

    @NotNull
    public Component getName(@NotNull ItemStack stack) {
        if (this.technique == Technique.IMPERIAL_ART) {
            return ShimmerColors.buildShimmeringName(MYSTERY_NAME, ShimmerColors.DIVINE_MYSTERY, true);
        }
        return Component.translatable((String)this.getDescriptionId(stack));
    }

    public void appendHoverText(@NotNull ItemStack stack, @Nullable Level level, @NotNull List<Component> tooltip, @NotNull TooltipFlag flag) {
        if (this.technique == Technique.IMPERIAL_ART) {
            tooltip.add(ShimmerColors.buildShimmeringName(Component.translatable((String)"item_tier.friday_cultivation.great_emperor").getString(), ShimmerColors.DIVINE_MYSTERY));
            TooltipUtils.addBlank(tooltip);
            TooltipUtils.addSection(tooltip, "tooltip.friday_cultivation.section.contents");
            tooltip.add((Component)TooltipUtils.statsLine((Component)Component.translatable((String)"tooltip.friday_cultivation.technique_book.contains", (Object[])new Object[]{ShimmerColors.buildShimmeringName(MYSTERY_NAME, ShimmerColors.DIVINE_MYSTERY, true)})));
            TooltipUtils.addSection(tooltip, "tooltip.friday_cultivation.section.effect");
            tooltip.add((Component)ShimmerColors.buildShimmeringName(Component.translatable((String)"tooltip.friday_cultivation.technique_book.imperial_art_mystery").getString(), ShimmerColors.DIVINE_MYSTERY));
            TooltipUtils.addBlank(tooltip);
            tooltip.add((Component)TooltipUtils.hintLine((Component)Component.translatable((String)"tooltip.friday_cultivation.technique_book.use_hint")));
            return;
        }
        tooltip.add((Component)TooltipUtils.tierElementLine(this.technique.tier(), this.technique.primaryElement()));
        TooltipUtils.addBlank(tooltip);
        TooltipUtils.addSection(tooltip, "tooltip.friday_cultivation.section.contents");
        tooltip.add((Component)TooltipUtils.statsLine((Component)Component.translatable((String)"tooltip.friday_cultivation.technique_book.contains", (Object[])new Object[]{TooltipUtils.tieredName(this.technique.displayName(), this.technique.tier())})));
        tooltip.add((Component)TooltipUtils.statsLine((Component)Component.translatable((String)"tooltip.friday_cultivation.technique_book.path", (Object[])new Object[]{Component.translatable((String)this.technique.daoPathTranslationKey())})));
        TooltipUtils.addSection(tooltip, "tooltip.friday_cultivation.section.effect");
        tooltip.add((Component)TooltipUtils.descriptionLine(this.technique.description()));
        TooltipUtils.addBlank(tooltip);
        tooltip.add((Component)TooltipUtils.hintLine((Component)Component.translatable((String)"tooltip.friday_cultivation.technique_book.use_hint")));
    }

    @NotNull
    public InteractionResultHolder<ItemStack> use(@NotNull Level level, @NotNull Player player, @NotNull InteractionHand hand) {
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
            boolean autoEquipped;
            if (data.getLearnedTechniques().contains(this.technique.id())) {
                sp.displayClientMessage((Component)Component.translatable((String)"message.friday_cultivation.technique_book.already_learned", (Object[])new Object[]{this.technique.displayName()}), true);
                return;
            }
            if (!SpiritRootBonusHelper.canLearnTechnique((Player)sp, this.technique)) {
                sp.displayClientMessage((Component)Component.translatable((String)"message.friday_cultivation.technique_book.requirement_not_met", (Object[])new Object[]{this.technique.displayName()}), true);
                return;
            }
            data.learnTechnique(this.technique.id());
            Technique current = Technique.byId(data.getEquippedTechniqueId());
            boolean currentCompatible = current != null && data.getLearnedTechniques().contains(current.id()) && TechniqueLoadoutHelper.canEquipForCurrentState(data, current);
            boolean canEquipNow = TechniqueLoadoutHelper.canEquipForCurrentState(data, this.technique);
            boolean bl = autoEquipped = canEquipNow && (!data.hasEquippedTechnique() || !currentCompatible);
            if (autoEquipped) {
                data.setEquippedTechniqueId(this.technique.id());
            }
            CapabilityEvents.syncToClient(sp);
            String messageKey = autoEquipped ? "message.friday_cultivation.technique_book.learned_equipped" : (canEquipNow ? "message.friday_cultivation.technique_book.learned" : "message.friday_cultivation.technique_book.learned_incompatible_state");
            sp.displayClientMessage((Component)Component.translatable((String)messageKey, (Object[])new Object[]{this.technique.displayName()}), false);
            success[0] = true;
        });
        if (success[0]) {
            level.playSound(null, sp.getX(), sp.getY(), sp.getZ(), SoundEvents.PLAYER_LEVELUP, SoundSource.PLAYERS, 0.5f, 1.2f);
            if (!sp.getAbilities().instabuild) {
                stack.shrink(1);
            }
        }
        player.getCooldowns().addCooldown((Item)this, 10);
        return InteractionResultHolder.consume(stack);
    }
}

