/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.BlockPos
 *  net.minecraft.core.Direction
 *  net.minecraft.core.Vec3i
 *  net.minecraft.world.level.BlockGetter
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.level.LevelAccessor
 *  net.minecraft.world.level.LevelReader
 *  net.minecraft.world.level.block.Block
 *  net.minecraft.world.level.block.Blocks
 *  net.minecraft.world.level.block.EntityBlock
 *  net.minecraft.world.level.block.RenderShape
 *  net.minecraft.world.level.block.entity.BlockEntity
 *  net.minecraft.world.level.block.entity.BlockEntityTicker
 *  net.minecraft.world.level.block.entity.BlockEntityType
 *  net.minecraft.world.level.block.state.BlockBehaviour$Properties
 *  net.minecraft.world.level.block.state.BlockState
 *  net.minecraft.world.level.block.state.properties.BlockStateProperties
 *  net.minecraft.world.level.block.state.properties.BooleanProperty
 *  net.minecraft.world.level.block.state.properties.DirectionProperty
 *  net.minecraft.world.level.block.state.properties.Property
 *  net.minecraft.world.level.pathfinder.PathComputationType
 *  net.minecraft.world.phys.shapes.CollisionContext
 *  net.minecraft.world.phys.shapes.Shapes
 *  net.minecraft.world.phys.shapes.VoxelShape
 *  org.jetbrains.annotations.NotNull
 *  org.jetbrains.annotations.Nullable
 */
package com.friday.cultivation.block.formation;

import com.friday.cultivation.block.formation.FormationFlagBlock;
import com.friday.cultivation.block.formation.FormationRuneBlockEntity;
import com.friday.cultivation.registry.ModBlockEntities;
import com.friday.cultivation.registry.ModBlocks;
import com.friday.cultivation.util.QiStorageBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class FormationRuneBlock
extends Block
implements EntityBlock {
    public static final DirectionProperty FACING = BlockStateProperties.FACING;
    public static final BooleanProperty LIT = BlockStateProperties.LIT;
    public static final BooleanProperty NORTH = BlockStateProperties.NORTH;
    public static final BooleanProperty EAST = BlockStateProperties.EAST;
    public static final BooleanProperty SOUTH = BlockStateProperties.SOUTH;
    public static final BooleanProperty WEST = BlockStateProperties.WEST;
    public static final BooleanProperty UP = BlockStateProperties.UP;
    public static final BooleanProperty DOWN = BlockStateProperties.DOWN;
    private static final VoxelShape SHAPE_UP = Block.box((double)0.0, (double)0.0, (double)0.0, (double)16.0, (double)1.0, (double)16.0);
    private static final VoxelShape SHAPE_DOWN = Block.box((double)0.0, (double)15.0, (double)0.0, (double)16.0, (double)16.0, (double)16.0);
    private static final VoxelShape SHAPE_NORTH = Block.box((double)0.0, (double)0.0, (double)15.0, (double)16.0, (double)16.0, (double)16.0);
    private static final VoxelShape SHAPE_SOUTH = Block.box((double)0.0, (double)0.0, (double)0.0, (double)16.0, (double)16.0, (double)1.0);
    private static final VoxelShape SHAPE_EAST = Block.box((double)0.0, (double)0.0, (double)0.0, (double)1.0, (double)16.0, (double)16.0);
    private static final VoxelShape SHAPE_WEST = Block.box((double)15.0, (double)0.0, (double)0.0, (double)16.0, (double)16.0, (double)16.0);

    public FormationRuneBlock(BlockBehaviour.Properties properties) {
        super(properties);
        this.registerDefaultState((BlockState)((BlockState)((BlockState)((BlockState)((BlockState)((BlockState)((BlockState)((BlockState)((BlockState)this.stateDefinition.any()).setValue((Property)FACING, (Comparable)Direction.UP)).setValue((Property)LIT, (Comparable)Boolean.valueOf(false))).setValue((Property)NORTH, (Comparable)Boolean.valueOf(false))).setValue((Property)EAST, (Comparable)Boolean.valueOf(false))).setValue((Property)SOUTH, (Comparable)Boolean.valueOf(false))).setValue((Property)WEST, (Comparable)Boolean.valueOf(false))).setValue((Property)UP, (Comparable)Boolean.valueOf(false))).setValue((Property)DOWN, (Comparable)Boolean.valueOf(false)));
    }

    public static BlockState stateForFace(Direction face) {
        return (BlockState)((Block)ModBlocks.FORMATION_RUNE.get()).defaultBlockState().setValue((Property)FACING, (Comparable)face);
    }

    public static BlockState updateConnections(BlockState state, BlockGetter level, BlockPos pos) {
        if (!(state.getBlock() instanceof FormationRuneBlock)) {
            return state;
        }
        Direction facing = (Direction)state.getValue(FACING);
        BlockState updated = state;
        for (Direction dir : Direction.values()) {
            boolean connected = FormationRuneBlock.isPlaneDirection(facing, dir) && FormationRuneBlock.connectsTo(level, pos, facing, dir);
            updated = (BlockState)updated.setValue((Property)FormationRuneBlock.propertyFor(dir), (Comparable)Boolean.valueOf(connected));
        }
        return updated;
    }

    public static boolean canRuneConnect(BlockState first, BlockPos firstPos, BlockState second, BlockPos secondPos) {
        if (!(first.getBlock() instanceof FormationRuneBlock) || !(second.getBlock() instanceof FormationRuneBlock)) {
            return false;
        }
        Direction facing = (Direction)first.getValue(FACING);
        if (second.getValue(FACING) != facing) {
            return false;
        }
        Direction step = FormationRuneBlock.directionBetween(firstPos, secondPos);
        return step != null && FormationRuneBlock.isPlaneDirection(facing, step);
    }

    public static boolean touchesSpiritVeinCore(BlockGetter level, BlockPos pos, BlockState state) {
        return FormationRuneBlock.touches(level, pos, state, true);
    }

    public static boolean touchesStorageTarget(BlockGetter level, BlockPos pos, BlockState state) {
        return FormationRuneBlock.touches(level, pos, state, false);
    }

    private static boolean touches(BlockGetter level, BlockPos pos, BlockState state, boolean source) {
        if (!(state.getBlock() instanceof FormationRuneBlock)) {
            return false;
        }
        Direction facing = (Direction)state.getValue(FACING);
        if (FormationRuneBlock.matchesEndpoint(level.getBlockEntity(pos.relative(facing.getOpposite())), source)) {
            return true;
        }
        for (Direction dir : Direction.values()) {
            if (!FormationRuneBlock.matchesEndpoint(level.getBlockEntity(pos.relative(dir)), source)) continue;
            return true;
        }
        return false;
    }

    private static boolean matchesEndpoint(@Nullable BlockEntity be, boolean source) {
        return source ? QiStorageBlocks.isUnlockedSpiritVeinCore(be) : QiStorageBlocks.isUnlockedStorageTarget(be);
    }

    private static boolean connectsTo(BlockGetter level, BlockPos pos, Direction facing, Direction dir) {
        BlockPos neighborPos = pos.relative(dir);
        BlockState neighbor = level.getBlockState(neighborPos);
        if (neighbor.getBlock() instanceof FormationRuneBlock && neighbor.getValue(FACING) == facing) {
            return true;
        }
        BlockEntity be = level.getBlockEntity(neighborPos);
        return QiStorageBlocks.isUnlockedSpiritVeinCore(be) || QiStorageBlocks.isUnlockedStorageTarget(be) || neighbor.getBlock() instanceof FormationFlagBlock;
    }

    private static boolean isPlaneDirection(Direction facing, Direction dir) {
        return dir.getAxis() != facing.getAxis();
    }

    private static Direction directionBetween(BlockPos from, BlockPos to) {
        BlockPos delta = to.subtract((Vec3i)from);
        for (Direction dir : Direction.values()) {
            if (!delta.equals((Object)dir.getNormal())) continue;
            return dir;
        }
        return null;
    }

    private static BooleanProperty propertyFor(Direction dir) {
        return switch (dir) {
            default -> throw new IncompatibleClassChangeError();
            case NORTH -> NORTH;
            case EAST -> EAST;
            case SOUTH -> SOUTH;
            case WEST -> WEST;
            case UP -> UP;
            case DOWN -> DOWN;
        };
    }

    protected void createBlockStateDefinition(     @NotNull StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(new Property[]{FACING, LIT, NORTH, EAST, SOUTH, WEST, UP, DOWN});
    }

    @NotNull
    public BlockState updateShape(@NotNull BlockState state, @NotNull Direction direction, @NotNull BlockState neighborState, @NotNull LevelAccessor level, @NotNull BlockPos pos, @NotNull BlockPos neighborPos) {
        if (direction == ((Direction)state.getValue(FACING)).getOpposite() && !this.canSurvive(state, (LevelReader)level, pos)) {
            return Blocks.AIR.defaultBlockState();
        }
        return FormationRuneBlock.updateConnections(state, (BlockGetter)level, pos);
    }

    public void onPlace(@NotNull BlockState state, @NotNull Level level, @NotNull BlockPos pos, @NotNull BlockState oldState, boolean moved) {
        super.onPlace(state, level, pos, oldState, moved);
        if (!level.isClientSide && !state.is(oldState.getBlock())) {
            BlockState updated = FormationRuneBlock.updateConnections(state, (BlockGetter)level, pos);
            if (updated != state) {
                level.setBlock(pos, updated, 3);
            }
            for (Direction dir : Direction.values()) {
                level.updateNeighborsAt(pos.relative(dir), (Block)this);
            }
        }
    }

    public boolean canSurvive(@NotNull BlockState state, @NotNull LevelReader level, @NotNull BlockPos pos) {
        Direction facing = (Direction)state.getValue(FACING);
        BlockPos supportPos = pos.relative(facing.getOpposite());
        return level.getBlockState(supportPos).isFaceSturdy(level, supportPos, facing);
    }

    @NotNull
    public VoxelShape getShape(@NotNull BlockState state, @NotNull BlockGetter level, @NotNull BlockPos pos, @NotNull CollisionContext ctx) {
        return switch ((Direction)state.getValue(FACING)) {
            default -> throw new IncompatibleClassChangeError();
            case UP -> SHAPE_UP;
            case DOWN -> SHAPE_DOWN;
            case NORTH -> SHAPE_NORTH;
            case SOUTH -> SHAPE_SOUTH;
            case EAST -> SHAPE_EAST;
            case WEST -> SHAPE_WEST;
        };
    }

    @NotNull
    public VoxelShape getCollisionShape(@NotNull BlockState state, @NotNull BlockGetter level, @NotNull BlockPos pos, @NotNull CollisionContext ctx) {
        return Shapes.empty();
    }

    public boolean isPathfindable(@NotNull BlockState state, @NotNull BlockGetter level, @NotNull BlockPos pos, @NotNull PathComputationType type) {
        return true;
    }

    @NotNull
    public RenderShape getRenderShape(@NotNull BlockState state) {
        return RenderShape.MODEL;
    }

    @Nullable
    public BlockEntity newBlockEntity(@NotNull BlockPos pos, @NotNull BlockState state) {
        return new FormationRuneBlockEntity(pos, state);
    }

    @Nullable
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(@NotNull Level level, @NotNull BlockState state, @NotNull BlockEntityType<T> type) {
        if (level.isClientSide) {
            return null;
        }
        return type == ModBlockEntities.FORMATION_RUNE.get() ? (lvl, pos, st, be) -> ((FormationRuneBlockEntity)be).serverTick() : null;
    }

    public void onRemove(@NotNull BlockState oldState, @NotNull Level level, @NotNull BlockPos pos, @NotNull BlockState newState, boolean moved) {
        BlockEntity be;
        if (!oldState.is(newState.getBlock()) && (be = level.getBlockEntity(pos)) instanceof FormationRuneBlockEntity) {
            FormationRuneBlockEntity rune = (FormationRuneBlockEntity)be;
            rune.onBlockRemoved();
        }
        super.onRemove(oldState, level, pos, newState, moved);
    }
}

