package com.friday.cultivation.block.formation;

import com.friday.cultivation.block.formation.FormationFlagBlock;
import com.friday.cultivation.block.formation.FormationRuneBlock;
import com.friday.cultivation.block.formation.FormationRuneBlockEntity;
import com.friday.cultivation.registry.ModBlockEntities;
import com.friday.cultivation.registry.ModBlocks;
import com.friday.cultivation.util.QiStorageBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
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
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * 阵法雾符方块（贴附于六面） - 自动检测与相邻雾符/灵脉核心/存储目标/旗子的连接，
 * 并通过 FACING/LIT/NORTH/EAST/SOUTH/WEST/UP/DOWN 八属性可视化连通。
 * 严格 1:1 复刻原 mod FormationRuneBlock。
 */
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

    private static final VoxelShape SHAPE_UP = Block.box(0.0, 0.0, 0.0, 16.0, 1.0, 16.0);
    private static final VoxelShape SHAPE_DOWN = Block.box(0.0, 15.0, 0.0, 16.0, 16.0, 16.0);
    private static final VoxelShape SHAPE_NORTH = Block.box(0.0, 0.0, 15.0, 16.0, 16.0, 16.0);
    private static final VoxelShape SHAPE_SOUTH = Block.box(0.0, 0.0, 0.0, 16.0, 16.0, 1.0);
    private static final VoxelShape SHAPE_EAST = Block.box(0.0, 0.0, 0.0, 1.0, 16.0, 16.0);
    private static final VoxelShape SHAPE_WEST = Block.box(15.0, 0.0, 0.0, 16.0, 16.0, 16.0);

    public FormationRuneBlock(BlockBehaviour.Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(FACING, Direction.UP)
                .setValue(LIT, false)
                .setValue(NORTH, false)
                .setValue(EAST, false)
                .setValue(SOUTH, false)
                .setValue(WEST, false)
                .setValue(UP, false)
                .setValue(DOWN, false));
    }

    public static BlockState stateForFace(Direction face) {
        return ModBlocks.FORMATION_RUNE.get().defaultBlockState().setValue(FACING, face);
    }

    public static BlockState updateConnections(BlockState state, BlockGetter level, BlockPos pos) {
        if (!(state.getBlock() instanceof FormationRuneBlock)) {
            return state;
        }
        Direction facing = state.getValue(FACING);
        BlockState updated = state;
        for (Direction dir : Direction.values()) {
            boolean connected = isPlaneDirection(facing, dir) && connectsTo(level, pos, facing, dir);
            updated = updated.setValue(propertyFor(dir), connected);
        }
        return updated;
    }

    public static boolean canRuneConnect(BlockState first, BlockPos firstPos, BlockState second, BlockPos secondPos) {
        if (!(first.getBlock() instanceof FormationRuneBlock) || !(second.getBlock() instanceof FormationRuneBlock)) {
            return false;
        }
        Direction facing = first.getValue(FACING);
        if (second.getValue(FACING) != facing) {
            return false;
        }
        Direction step = directionBetween(firstPos, secondPos);
        return step != null && isPlaneDirection(facing, step);
    }

    public static boolean touchesSpiritVeinCore(BlockGetter level, BlockPos pos, BlockState state) {
        return touches(level, pos, state, true);
    }

    public static boolean touchesStorageTarget(BlockGetter level, BlockPos pos, BlockState state) {
        return touches(level, pos, state, false);
    }

    private static boolean touches(BlockGetter level, BlockPos pos, BlockState state, boolean source) {
        if (!(state.getBlock() instanceof FormationRuneBlock)) {
            return false;
        }
        Direction facing = state.getValue(FACING);
        if (matchesEndpoint(level.getBlockEntity(pos.relative(facing.getOpposite())), source)) {
            return true;
        }
        for (Direction dir : Direction.values()) {
            if (matchesEndpoint(level.getBlockEntity(pos.relative(dir)), source)) {
                return true;
            }
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
        return QiStorageBlocks.isUnlockedSpiritVeinCore(be)
                || QiStorageBlocks.isUnlockedStorageTarget(be)
                || neighbor.getBlock() instanceof FormationFlagBlock;
    }

    private static boolean isPlaneDirection(Direction facing, Direction dir) {
        return dir.getAxis() != facing.getAxis();
    }

    private static Direction directionBetween(BlockPos from, BlockPos to) {
        BlockPos delta = to.subtract(from);
        for (Direction dir : Direction.values()) {
            if (!delta.equals(dir.getNormal())) continue;
            return dir;
        }
        return null;
    }

    private static BooleanProperty propertyFor(Direction dir) {
        return switch (dir) {
            case NORTH -> NORTH;
            case EAST -> EAST;
            case SOUTH -> SOUTH;
            case WEST -> WEST;
            case UP -> UP;
            case DOWN -> DOWN;
        };
    }

    @Override
    protected void createBlockStateDefinition(@NotNull StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, LIT, NORTH, EAST, SOUTH, WEST, UP, DOWN);
    }

    @NotNull
    @Override
    public BlockState updateShape(@NotNull BlockState state, @NotNull Direction direction, @NotNull BlockState neighborState, @NotNull LevelAccessor level, @NotNull BlockPos pos, @NotNull BlockPos neighborPos) {
        if (direction == state.getValue(FACING).getOpposite() && !this.canSurvive(state, (LevelReader) level, pos)) {
            return Blocks.AIR.defaultBlockState();
        }
        return updateConnections(state, (BlockGetter) level, pos);
    }

    @Override
    public void onPlace(@NotNull BlockState state, @NotNull Level level, @NotNull BlockPos pos, @NotNull BlockState oldState, boolean moved) {
        super.onPlace(state, level, pos, oldState, moved);
        if (!level.isClientSide && !state.is(oldState.getBlock())) {
            BlockState updated = updateConnections(state, (BlockGetter) level, pos);
            if (updated != state) {
                level.setBlock(pos, updated, 3);
            }
            for (Direction dir : Direction.values()) {
                level.blockUpdated(pos.relative(dir), this);
            }
        }
    }

    @Override
    public boolean canSurvive(@NotNull BlockState state, @NotNull LevelReader level, @NotNull BlockPos pos) {
        Direction facing = state.getValue(FACING);
        BlockPos supportPos = pos.relative(facing.getOpposite());
        return level.getBlockState(supportPos).isFaceSturdy((BlockGetter) level, supportPos, facing);
    }

    @NotNull
    @Override
    public VoxelShape getShape(@NotNull BlockState state, @NotNull BlockGetter level, @NotNull BlockPos pos, @NotNull CollisionContext ctx) {
        return switch (state.getValue(FACING)) {
            case UP -> SHAPE_UP;
            case DOWN -> SHAPE_DOWN;
            case NORTH -> SHAPE_NORTH;
            case SOUTH -> SHAPE_SOUTH;
            case EAST -> SHAPE_EAST;
            case WEST -> SHAPE_WEST;
        };
    }

    @NotNull
    @Override
    public VoxelShape getCollisionShape(@NotNull BlockState state, @NotNull BlockGetter level, @NotNull BlockPos pos, @NotNull CollisionContext ctx) {
        return Shapes.empty();
    }

    @Override
    public boolean isPathfindable(@NotNull BlockState state, @NotNull BlockGetter level, @NotNull BlockPos pos, @NotNull PathComputationType type) {
        return true;
    }

    @NotNull
    @Override
    public RenderShape getRenderShape(@NotNull BlockState state) {
        return RenderShape.MODEL;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(@NotNull BlockPos pos, @NotNull BlockState state) {
        return new FormationRuneBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(@NotNull Level level, @NotNull BlockState state, @NotNull BlockEntityType<T> type) {
        if (level.isClientSide()) {
            return null;
        }
        return type == ModBlockEntities.FORMATION_RUNE.get() ? (lvl, pos, st, be) -> ((FormationRuneBlockEntity) be).serverTick() : null;
    }

    @Override
    public void onRemove(BlockState oldState, @NotNull Level level, @NotNull BlockPos pos, BlockState newState, boolean isMoving) {
        if (!oldState.is(newState.getBlock())) {
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof FormationRuneBlockEntity rune) {
                rune.onBlockRemoved();
            }
        }
        super.onRemove(oldState, level, pos, newState, isMoving);
    }
}
