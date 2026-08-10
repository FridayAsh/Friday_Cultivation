package com.friday.cultivation.block.spirit;

import com.friday.cultivation.qi.SpiritVeinCoreTier;
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

/**
 * 灵脉核心方块 — 完全照搬原模组 com.xiaoxiang.cultivation.block.spirit.SpiritVeinCoreBlock
 */
public class SpiritVeinCoreBlock extends Block implements EntityBlock {
    private final SpiritVeinCoreTier tier;

    public SpiritVeinCoreBlock(BlockBehaviour.Properties properties, SpiritVeinCoreTier tier) {
        super(properties);
        this.tier = tier;
    }

    /** 项目兼容：单方块注册（ModBlocks 仅注册一个 spirit_vein_core，代表仙品灵脉核心） */
    public SpiritVeinCoreBlock(BlockBehaviour.Properties properties) {
        this(properties, SpiritVeinCoreTier.IMMORTAL);
    }

    public SpiritVeinCoreTier tier() {
        return this.tier;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(@NotNull BlockPos pos, @NotNull BlockState state) {
        return new SpiritVeinCoreBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(@NotNull Level level, @NotNull BlockState state, @NotNull BlockEntityType<T> type) {
        if (level.isClientSide) {
            return null;
        }
        return type == ModBlockEntities.SPIRIT_VEIN_CORE.get() ? (lvl, pos, st, be) -> ((SpiritVeinCoreBlockEntity) be).serverTick() : null;
    }

    @NotNull
    @Override
    public InteractionResult use(@NotNull BlockState state, @NotNull Level level, @NotNull BlockPos pos, @NotNull Player player, @NotNull InteractionHand hand, @NotNull BlockHitResult hit) {
        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        }
        BlockEntity be = level.getBlockEntity(pos);
        if (!(be instanceof SpiritVeinCoreBlockEntity)) {
            return InteractionResult.PASS;
        }
        SpiritVeinCoreBlockEntity core = (SpiritVeinCoreBlockEntity) be;
        Component maxText = core.maxQiText();
        player.displayClientMessage(Component.translatable("message.friday_cultivation.spirit_vein_core.status", state.getBlock().getName(), core.getCurrentQi(), maxText, core.tier().supplyPerSecond(), 16).withStyle(ChatFormatting.AQUA), false);
        return InteractionResult.CONSUME;
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, @Nullable BlockGetter level, @NotNull List<Component> tooltip, @NotNull TooltipFlag flag) {
        super.appendHoverText(stack, level, tooltip, flag);
        MutableComponent maxText = Component.literal(Long.toString(this.tier.maxQi()));
        TooltipUtils.addSection(tooltip, "tooltip.friday_cultivation.section.stats");
        tooltip.add(TooltipUtils.statsLine(Component.translatable("tooltip.friday_cultivation.spirit_vein_core.capacity", maxText)));
        tooltip.add(TooltipUtils.positiveLine(Component.translatable("tooltip.friday_cultivation.spirit_vein_core.absorb", this.tier.orbGain())));
        TooltipUtils.addSection(tooltip, "tooltip.friday_cultivation.section.effect");
        tooltip.add(TooltipUtils.effectLine(Component.translatable("tooltip.friday_cultivation.spirit_vein_core.supply", this.tier.supplyPerSecond(), 16)));
    }

    @Override
    public void onRemove(@NotNull BlockState oldState, @NotNull Level level, @NotNull BlockPos pos, @NotNull BlockState newState, boolean moved) {
        BlockEntity be;
        if (!oldState.is(newState.getBlock()) && (be = level.getBlockEntity(pos)) instanceof SpiritVeinCoreBlockEntity) {
            SpiritVeinCoreBlockEntity core = (SpiritVeinCoreBlockEntity) be;
            core.onBlockRemoved();
        }
        super.onRemove(oldState, level, pos, newState, moved);
    }
}
