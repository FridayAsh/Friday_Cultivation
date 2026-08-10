package com.friday.cultivation.block;

import com.friday.cultivation.entity.SeatEntity;
import com.friday.cultivation.util.TooltipUtils;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class CushionBlock extends Block {
    private static final VoxelShape SHAPE = Shapes.box(0.0, 0.0, 0.0, 1.0, 0.25, 1.0);

    public CushionBlock(BlockBehaviour.Properties props) {
        super(props);
    }

    @NotNull
    @Override
    public VoxelShape getShape(@NotNull BlockState state, @NotNull BlockGetter level, @NotNull BlockPos pos, @NotNull CollisionContext ctx) {
        return SHAPE;
    }

    @NotNull
    @Override
    public InteractionResult use(@NotNull BlockState state, @NotNull Level level, @NotNull BlockPos pos, @NotNull Player player, @NotNull InteractionHand hand, @NotNull BlockHitResult hit) {
        if (player.isPassenger()) {
            return InteractionResult.PASS;
        }
        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }
        List<SeatEntity> existing = level.getEntitiesOfClass(SeatEntity.class, new AABB(pos).inflate(0.5));
        for (SeatEntity seat : existing) {
            if (seat.getPassengers().isEmpty()) continue;
            return InteractionResult.PASS;
        }
        SeatEntity seat = new SeatEntity(level, pos);
        level.addFreshEntity(seat);
        player.startRiding(seat, true);
        return InteractionResult.CONSUME;
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, @Nullable BlockGetter level, @NotNull List<Component> tooltip, @NotNull TooltipFlag flag) {
        super.appendHoverText(stack, level, tooltip, flag);
        TooltipUtils.addSection(tooltip, "tooltip.friday_cultivation.section.effect");
        tooltip.add(TooltipUtils.positiveLine(Component.translatable("tooltip.friday_cultivation.cushion.absorb_mult")));
        tooltip.add(TooltipUtils.positiveLine(Component.translatable("tooltip.friday_cultivation.cushion.absorb_range")));
        TooltipUtils.addBlank(tooltip);
        tooltip.add(TooltipUtils.hintLine(Component.translatable("tooltip.friday_cultivation.cushion.use_hint")));
    }

    @Override
    public void onRemove(@NotNull BlockState state, @NotNull Level level, @NotNull BlockPos pos, @NotNull BlockState newState, boolean moved) {
        if (!state.is(newState.getBlock())) {
            List<SeatEntity> seats = level.getEntitiesOfClass(SeatEntity.class, new AABB(pos).inflate(0.5));
            for (SeatEntity seat : seats) {
                seat.stopRiding();
                seat.discard();
            }
        }
        super.onRemove(state, level, pos, newState, moved);
    }
}
