package com.friday.cultivation.worldgen;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

public class DifuChainFeature extends Feature<NoneFeatureConfiguration> {
    public DifuChainFeature(Codec<NoneFeatureConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> ctx) {
        WorldGenLevel level = ctx.level();
        RandomSource random = ctx.random();
        BlockPos origin = ctx.origin();
        int x = origin.getX();
        int z = origin.getZ();
        int groundY = level.getHeight(Heightmap.Types.WORLD_SURFACE_WG, x, z);
        int topY = level.getMaxBuildHeight() - 2;
        if (groundY >= topY - 8) return false;
        BlockState stone = Blocks.STONE_BRICKS.defaultBlockState();
        BlockState wall = Blocks.STONE_BRICK_WALL.defaultBlockState();
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        int curX = x, curZ = z;
        boolean placedAny = false;
        for (int y = groundY - 2; y <= topY; ++y) {
            pos.set(curX, y, curZ);
            if (canReplace(level.getBlockState(pos))) {
                level.setBlock(pos, Math.floorMod(y, 4) == 0 ? wall : stone, 2);
                placedAny = true;
            }
            if (random.nextInt(10) != 0) continue;
            curX += random.nextInt(3) - 1;
            curZ += random.nextInt(3) - 1;
        }
        return placedAny;
    }

    private static boolean canReplace(BlockState state) {
        return state.isAir() || state.is(Blocks.STONE) || state.is(Blocks.DEEPSLATE)
                || state.is(Blocks.COBBLESTONE) || state.is(Blocks.MOSSY_COBBLESTONE)
                || state.is(Blocks.BONE_BLOCK) || state.is(Blocks.STONE_BRICKS)
                || state.is(Blocks.STONE_BRICK_WALL);
    }
}
