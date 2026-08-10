/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.BlockPos
 *  net.minecraft.network.chat.Component
 *  net.minecraft.world.InteractionHand
 *  net.minecraft.world.InteractionResult
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.item.TooltipFlag
 *  net.minecraft.world.level.BlockGetter
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.level.block.Block
 *  net.minecraft.world.level.block.state.BlockBehaviour$Properties
 *  net.minecraft.world.level.block.state.BlockState
 *  net.minecraft.world.phys.AABB
 *  net.minecraft.world.phys.BlockHitResult
 *  net.minecraft.world.phys.shapes.CollisionContext
 *  net.minecraft.world.phys.shapes.Shapes
 *  net.minecraft.world.phys.shapes.VoxelShape
 *  org.jetbrains.annotations.NotNull
 *  org.jetbrains.annotations.Nullable
 */
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

public class CushionBlock
extends Block {
    private static final VoxelShape SHAPE = Shapes.box((double)0.0, (double)0.0, (double)0.0, (double)1.0, (double)0.25, (double)1.0);

    public CushionBlock(BlockBehaviour.Properties props) {
        super(props);
    }

    @NotNull
    public VoxelShape canSurvive(@NotNull BlockState state, @NotNull BlockGetter level, @NotNull BlockPos pos, @NotNull CollisionContext ctx) {
        return SHAPE;
    }

    @NotNull
    public InteractionResult createBlockStateDefinition(@NotNull BlockState state, @NotNull Level level, @NotNull BlockPos pos, @NotNull Player player, @NotNull InteractionHand hand, @NotNull BlockHitResult hit) {
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
        level.addFreshEntity((Entity)seat);
        player.startRiding((Entity)seat, true);
        return InteractionResult.CONSUME;
    }

    public void appendHoverText(@NotNull ItemStack stack, @Nullable BlockGetter level, @NotNull List<Component> tooltip, @NotNull TooltipFlag flag) {
        super.appendHoverText(stack, level, tooltip, flag);
        TooltipUtils.addSection(tooltip, "tooltip.friday_cultivation.section.effect");
        tooltip.add((Component)TooltipUtils.positiveLine((Component)Component.translatable((String)"tooltip.friday_cultivation.cushion.absorb_mult")));
        tooltip.add((Component)TooltipUtils.positiveLine((Component)Component.translatable((String)"tooltip.friday_cultivation.cushion.absorb_range")));
        TooltipUtils.addBlank(tooltip);
        tooltip.add((Component)TooltipUtils.hintLine((Component)Component.translatable((String)"tooltip.friday_cultivation.cushion.use_hint")));
    }

    public void onRemove(@NotNull BlockState state, @NotNull Level level, @NotNull BlockPos pos, @NotNull BlockState newState, boolean moved) {
        if (!state.is(newState.getBlock())) {
            List<SeatEntity> seats = level.getEntitiesOfClass(SeatEntity.class, new AABB(pos).inflate(0.5));
            for (SeatEntity seat : seats) {
                seat.ejectPassengers();
                seat.discard();
            }
        }
        super.onRemove(state, level, pos, newState, moved);
    }
}

