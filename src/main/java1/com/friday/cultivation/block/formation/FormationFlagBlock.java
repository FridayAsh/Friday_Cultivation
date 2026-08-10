package com.friday.cultivation.block.formation;

import com.friday.cultivation.ItemTier;
import com.friday.cultivation.item.FormationCompassItem;
import com.friday.cultivation.qi.formation.FormationType;
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
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;

/**
 * 阵旗方块（严格照搬原模组 com.xiaoxiang.cultivation.block.formation.FormationFlagBlock）
 * 7 种阵旗的抽象父类：水平朝向、支撑检测、阵法罗盘链接。
 */
public abstract class FormationFlagBlock extends HorizontalDirectionalBlock {
    public static final VoxelShape SHAPE = Shapes.box(0.375, 0.0, 0.375, 0.625, 1.0, 0.625);

    protected FormationFlagBlock(BlockBehaviour.Properties properties) {
        super(properties);
        this.registerDefaultState((BlockState) this.stateDefinition.any().setValue(FACING, Direction.NORTH));
    }

    public abstract FormationType formationType();

    public abstract ItemTier flagTier();

    @Override
    public void appendHoverText(@NotNull ItemStack stack, @org.jetbrains.annotations.Nullable BlockGetter level, @NotNull List<Component> tooltip, @NotNull TooltipFlag flag) {
        super.appendHoverText(stack, level, tooltip, flag);
        String baseKey = "tooltip.friday_cultivation.formation_flag." + this.formationType().id();
        String drain = String.format(Locale.ROOT, "%.0f", this.formationType().drainPerBlockPerHour(this.flagTier()));
        tooltip.add(TooltipUtils.tieredName(Component.translatable(this.formationType().translationKey()), this.flagTier()));
        TooltipUtils.addBlank(tooltip);
        TooltipUtils.addSection(tooltip, "tooltip.friday_cultivation.section.effect");
        tooltip.add(TooltipUtils.descriptionLine(Component.translatable(baseKey + ".summary")));
        tooltip.add(TooltipUtils.effectLine(Component.translatable(baseKey + "." + this.flagTier().id())));
        TooltipUtils.addSection(tooltip, "tooltip.friday_cultivation.section.stats");
        tooltip.add(TooltipUtils.statsLine(Component.translatable("tooltip.friday_cultivation.formation_flag.common.drain", drain)));
        TooltipUtils.addBlank(tooltip);
        tooltip.add(TooltipUtils.hintLine(Component.translatable("tooltip.friday_cultivation.formation_flag.common.link")));
    }

    @Override
    protected void createBlockStateDefinition(@NotNull StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext ctx) {
        return (BlockState) this.defaultBlockState().setValue(FACING, ctx.getHorizontalDirection().getOpposite());
    }

    @NotNull
    @Override
    public VoxelShape getShape(@NotNull BlockState state, @NotNull BlockGetter level, @NotNull BlockPos pos, @NotNull CollisionContext ctx) {
        return SHAPE;
    }

    @NotNull
    @Override
    public InteractionResult use(@NotNull BlockState state, @NotNull Level level, @NotNull BlockPos pos, @NotNull Player player, @NotNull InteractionHand hand, @NotNull BlockHitResult hit) {
        ItemStack stack = player.getItemInHand(hand);
        if (stack.getItem() instanceof FormationCompassItem) {
            if (!level.isClientSide) {
                FormationCompassItem.tryLinkFlag(stack, level, pos, player, this);
            }
            return InteractionResult.sidedSuccess(level.isClientSide);
        }
        return super.use(state, level, pos, player, hand, hit);
    }

    @Override
    public boolean canSurvive(@NotNull BlockState state, @NotNull LevelReader level, @NotNull BlockPos pos) {
        BlockPos below = pos.below();
        return level.getBlockState(below).isFaceSturdy(level, below, Direction.UP);
    }

    @NotNull
    @Override
    public BlockState updateShape(@NotNull BlockState state, @NotNull Direction direction, @NotNull BlockState neighborState, @NotNull LevelAccessor level, @NotNull BlockPos pos, @NotNull BlockPos neighborPos) {
        if (direction == Direction.DOWN && !this.canSurvive(state, level, pos)) {
            return Blocks.AIR.defaultBlockState();
        }
        return super.updateShape(state, direction, neighborState, level, pos, neighborPos);
    }
}
