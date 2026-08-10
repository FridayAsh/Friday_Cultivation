package com.friday.cultivation.block;

import com.friday.cultivation.registry.ModParticles;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.BushBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;

public class HerbBlock extends BushBlock {
    private static final VoxelShape SHAPE = box(3.2, 0.0, 3.2, 12.8, 9.6, 12.8);

    public HerbBlock(BlockBehaviour.Properties props) {
        super(props);
    }

    @NotNull
    @Override
    public VoxelShape getShape(@NotNull BlockState state, @NotNull BlockGetter level,
                                @NotNull BlockPos pos, @NotNull CollisionContext ctx) {
        return SHAPE;
    }

    @Override
    protected boolean mayPlaceOn(@NotNull BlockState state, @NotNull BlockGetter level, @NotNull BlockPos pos) {
        return state.is(Blocks.GRASS_BLOCK) || state.is(Blocks.DIRT) || state.is(Blocks.COARSE_DIRT)
                || state.is(Blocks.PODZOL) || state.is(Blocks.FARMLAND)
                || state.is(Blocks.MOSS_BLOCK) || state.is(Blocks.MYCELIUM) || state.is(Blocks.ROOTED_DIRT);
    }

    @Override
    public void animateTick(@NotNull BlockState state, @NotNull Level level,
                             @NotNull BlockPos pos, @NotNull RandomSource random) {
        if (random.nextInt(20) != 0) return;
        double x = pos.getX() + 0.5 + (random.nextDouble() - 0.5) * 0.8;
        double y = pos.getY() + 0.4 + random.nextDouble() * 0.4;
        double z = pos.getZ() + 0.5 + (random.nextDouble() - 0.5) * 0.8;
        level.addParticle((ParticleOptions) ModParticles.AMBIENT_QI_WOOD.get(), x, y, z, 0.0, 0.02, 0.0);
    }
}
