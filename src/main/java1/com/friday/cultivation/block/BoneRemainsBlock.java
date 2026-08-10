package com.friday.cultivation.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;

public class BoneRemainsBlock extends Block {
    private static final VoxelShape SHAPE = Shapes.box(0.2, 0.0, 0.2, 0.8, 0.6, 0.8);

    public BoneRemainsBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @NotNull
    @Override
    public VoxelShape getShape(@NotNull BlockState state, @NotNull BlockGetter level,
                                @NotNull BlockPos pos, @NotNull CollisionContext context) {
        return SHAPE;
    }
}
