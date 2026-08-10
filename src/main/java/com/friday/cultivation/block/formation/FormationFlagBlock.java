/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.BlockPos
 *  net.minecraft.core.Direction
 *  net.minecraft.network.chat.Component
 *  net.minecraft.world.InteractionHand
 *  net.minecraft.world.InteractionResult
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.item.TooltipFlag
 *  net.minecraft.world.item.context.BlockPlaceContext
 *  net.minecraft.world.level.BlockGetter
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.level.LevelAccessor
 *  net.minecraft.world.level.LevelReader
 *  net.minecraft.world.level.block.Block
 *  net.minecraft.world.level.block.Blocks
 *  net.minecraft.world.level.block.HorizontalDirectionalBlock
 *  net.minecraft.world.level.block.state.BlockBehaviour$Properties
 *  net.minecraft.world.level.block.state.BlockState
 *  net.minecraft.world.level.block.state.properties.Property
 *  net.minecraft.world.phys.BlockHitResult
 *  net.minecraft.world.phys.shapes.CollisionContext
 *  net.minecraft.world.phys.shapes.Shapes
 *  net.minecraft.world.phys.shapes.VoxelShape
 *  org.jetbrains.annotations.NotNull
 */
package com.friday.cultivation.block.formation;

import com.friday.cultivation.cultivation.ItemTier;
import com.friday.cultivation.cultivation.qi.formation.FormationType;
import com.friday.cultivation.item.FormationCompassItem;
import com.friday.cultivation.util.TooltipUtils;
import java.util.List;
import java.util.Locale;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;

public abstract class FormationFlagBlock
extends HorizontalDirectionalBlock {
    public static final VoxelShape SHAPE = Shapes.box((double)0.375, (double)0.0, (double)0.375, (double)0.625, (double)1.0, (double)0.625);

    protected FormationFlagBlock(BlockBehaviour.Properties properties) {
        super(properties);
        this.registerDefaultState((BlockState)((BlockState)this.stateDefinition.any()).setValue((Property)FACING, (Comparable)Direction.NORTH));
    }

    public abstract FormationType formationType();

    public abstract ItemTier flagTier();

    public void appendHoverText(@NotNull ItemStack stack, BlockGetter level, @NotNull List<Component> tooltip, @NotNull TooltipFlag flag) {
        super.appendHoverText(stack, level, tooltip, flag);
        String baseKey = "tooltip.friday_cultivation.formation_flag." + this.formationType().id();
        String drain = String.format(Locale.ROOT, "%.0f", this.formationType().drainPerBlockPerHour(this.flagTier()));
        tooltip.add((Component)TooltipUtils.tieredName((Component)Component.translatable((String)this.formationType().translationKey()), this.flagTier()));
        TooltipUtils.addBlank(tooltip);
        TooltipUtils.addSection(tooltip, "tooltip.friday_cultivation.section.effect");
        tooltip.add((Component)TooltipUtils.descriptionLine((Component)Component.translatable((String)(baseKey + ".summary"))));
        tooltip.add((Component)TooltipUtils.effectLine((Component)Component.translatable((String)(baseKey + "." + this.flagTier().id()))));
        TooltipUtils.addSection(tooltip, "tooltip.friday_cultivation.section.stats");
        tooltip.add((Component)TooltipUtils.statsLine((Component)Component.translatable((String)"tooltip.friday_cultivation.formation_flag.common.drain", (Object[])new Object[]{drain})));
        TooltipUtils.addBlank(tooltip);
        tooltip.add((Component)TooltipUtils.hintLine((Component)Component.translatable((String)"tooltip.friday_cultivation.formation_flag.common.link")));
    }

    protected void createBlockStateDefinition(     @NotNull StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(new Property[]{FACING});
    }

    public BlockState getStateForPlacement(BlockPlaceContext ctx) {
        return (BlockState)this.defaultBlockState().setValue((Property)FACING, (Comparable)ctx.getHorizontalDirection().getOpposite());
    }

    @NotNull
    public VoxelShape canSurvive(@NotNull BlockState state, @NotNull BlockGetter level, @NotNull BlockPos pos, @NotNull CollisionContext ctx) {
        return SHAPE;
    }

    @NotNull
    public InteractionResult use(@NotNull BlockState state, @NotNull Level level, @NotNull BlockPos pos, @NotNull Player player, @NotNull InteractionHand hand, @NotNull BlockHitResult hit) {
        ItemStack stack = player.getItemInHand(hand);
        if (stack.getItem() instanceof FormationCompassItem) {
            if (!level.isClientSide) {
                FormationCompassItem.tryLinkFlag(stack, level, pos, player, this);
            }
            return InteractionResult.sidedSuccess((boolean)level.isClientSide);
        }
        return super.use(state, level, pos, player, hand, hit);
    }

    public boolean canSurvive(@NotNull BlockState state, @NotNull LevelReader level, @NotNull BlockPos pos) {
        BlockPos below = pos.below();
        return level.getBlockState(below).isFaceSturdy(level, below, Direction.UP);
    }

    @NotNull
    public BlockState updateShape(@NotNull BlockState state, @NotNull Direction direction, @NotNull BlockState neighborState, @NotNull LevelAccessor level, @NotNull BlockPos pos, @NotNull BlockPos neighborPos) {
        if (direction == Direction.DOWN && !this.canSurvive(state, (LevelReader)level, pos)) {
            return Blocks.AIR.defaultBlockState();
        }
        return super.updateShape(state, direction, neighborState, level, pos, neighborPos);
    }
}

