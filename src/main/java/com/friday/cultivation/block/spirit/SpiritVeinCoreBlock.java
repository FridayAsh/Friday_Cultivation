/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.ChatFormatting
 *  net.minecraft.core.BlockPos
 *  net.minecraft.network.chat.Component
 *  net.minecraft.network.chat.MutableComponent
 *  net.minecraft.world.InteractionHand
 *  net.minecraft.world.InteractionResult
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.item.TooltipFlag
 *  net.minecraft.world.level.BlockGetter
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.level.block.Block
 *  net.minecraft.world.level.block.EntityBlock
 *  net.minecraft.world.level.block.entity.BlockEntity
 *  net.minecraft.world.level.block.entity.BlockEntityTicker
 *  net.minecraft.world.level.block.entity.BlockEntityType
 *  net.minecraft.world.level.block.state.BlockBehaviour$Properties
 *  net.minecraft.world.level.block.state.BlockState
 *  net.minecraft.world.phys.BlockHitResult
 *  org.jetbrains.annotations.NotNull
 *  org.jetbrains.annotations.Nullable
 */
package com.friday.cultivation.block.spirit;

import com.friday.cultivation.block.spirit.SpiritVeinCoreBlockEntity;
import com.friday.cultivation.cultivation.qi.SpiritVeinCoreTier;
import com.friday.cultivation.registry.ModBlockEntities;
import com.friday.cultivation.util.TooltipUtils;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class SpiritVeinCoreBlock
extends Block
implements EntityBlock {
    private final SpiritVeinCoreTier tier;

    public SpiritVeinCoreBlock(BlockBehaviour.Properties properties, SpiritVeinCoreTier tier) {
        super(properties);
        this.tier = tier;
    }

    public SpiritVeinCoreTier tier() {
        return this.tier;
    }

    @Nullable
    public BlockEntity newBlockEntity(@NotNull BlockPos pos, @NotNull BlockState state) {
        return new SpiritVeinCoreBlockEntity(pos, state);
    }

    @Nullable
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(@NotNull Level level, @NotNull BlockState state, @NotNull BlockEntityType<T> type) {
        if (level.isClientSide) {
            return null;
        }
        return type == ModBlockEntities.SPIRIT_VEIN_CORE.get() ? (lvl, pos, st, be) -> ((SpiritVeinCoreBlockEntity)be).serverTick() : null;
    }

    @NotNull
    public InteractionResult use(@NotNull BlockState state, @NotNull Level level, @NotNull BlockPos pos, @NotNull Player player, @NotNull InteractionHand hand, @NotNull BlockHitResult hit) {
        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        }
        BlockEntity be = level.getBlockEntity(pos);
        if (!(be instanceof SpiritVeinCoreBlockEntity)) {
            return InteractionResult.PASS;
        }
        SpiritVeinCoreBlockEntity core = (SpiritVeinCoreBlockEntity)be;
        Component maxText = core.maxQiText();
        player.displayClientMessage((Component)Component.translatable((String)"message.friday_cultivation.spirit_vein_core.status", (Object[])new Object[]{state.getBlock().getName(), core.getCurrentQi(), maxText, core.tier().supplyPerSecond(), 16}).withStyle(ChatFormatting.AQUA), false);
        return InteractionResult.CONSUME;
    }

    public void appendHoverText(@NotNull ItemStack stack, @Nullable BlockGetter level, @NotNull List<Component> tooltip, @NotNull TooltipFlag flag) {
        super.appendHoverText(stack, level, tooltip, flag);
        MutableComponent maxText = Component.literal((String)Long.toString(this.tier.maxQi()));
        TooltipUtils.addSection(tooltip, "tooltip.friday_cultivation.section.stats");
        tooltip.add((Component)TooltipUtils.statsLine((Component)Component.translatable((String)"tooltip.friday_cultivation.spirit_vein_core.capacity", (Object[])new Object[]{maxText})));
        tooltip.add((Component)TooltipUtils.positiveLine((Component)Component.translatable((String)"tooltip.friday_cultivation.spirit_vein_core.absorb", (Object[])new Object[]{this.tier.orbGain()})));
        TooltipUtils.addSection(tooltip, "tooltip.friday_cultivation.section.effect");
        tooltip.add((Component)TooltipUtils.effectLine((Component)Component.translatable((String)"tooltip.friday_cultivation.spirit_vein_core.supply", (Object[])new Object[]{this.tier.supplyPerSecond(), 16})));
    }

    public void onRemove(@NotNull BlockState oldState, @NotNull Level level, @NotNull BlockPos pos, @NotNull BlockState newState, boolean moved) {
        BlockEntity be;
        if (!oldState.is(newState.getBlock()) && (be = level.getBlockEntity(pos)) instanceof SpiritVeinCoreBlockEntity) {
            SpiritVeinCoreBlockEntity core = (SpiritVeinCoreBlockEntity)be;
            core.onBlockRemoved();
        }
        super.onRemove(oldState, level, pos, newState, moved);
    }
}

