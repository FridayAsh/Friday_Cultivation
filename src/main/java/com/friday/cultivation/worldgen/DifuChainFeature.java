/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.serialization.Codec
 *  net.minecraft.core.BlockPos
 *  net.minecraft.core.BlockPos$MutableBlockPos
 *  net.minecraft.util.RandomSource
 *  net.minecraft.world.level.WorldGenLevel
 *  net.minecraft.world.level.block.Blocks
 *  net.minecraft.world.level.block.state.BlockState
 *  net.minecraft.world.level.levelgen.Heightmap$Types
 *  net.minecraft.world.level.levelgen.feature.Feature
 *  net.minecraft.world.level.levelgen.feature.FeaturePlaceContext
 *  net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration
 *  org.jetbrains.annotations.NotNull
 */
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
import org.jetbrains.annotations.NotNull;

public class DifuChainFeature
extends Feature<NoneFeatureConfiguration> {
    public DifuChainFeature(Codec<NoneFeatureConfiguration> codec) {
        super(codec);
    }

    public boolean place(@NotNull FeaturePlaceContext<NoneFeatureConfiguration> ctx) {
        int topY;
        int z;
        WorldGenLevel level = ctx.level();
        RandomSource random = ctx.random();
        BlockPos origin = ctx.origin();
        int x = origin.getX();
        int groundY = level.getHeight(Heightmap.Types.WORLD_SURFACE_WG, x, z = origin.getZ());
        if (groundY >= (topY = level.getMaxBuildHeight() - 2) - 8) {
            return false;
        }
        BlockState brick = Blocks.NETHER_BRICKS.defaultBlockState();
        BlockState fence = Blocks.NETHER_BRICK_FENCE.defaultBlockState();
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        int curX = x;
        int curZ = z;
        boolean placedAny = false;
        for (int y = groundY - 2; y <= topY; ++y) {
            pos.set(curX, y, curZ);
            BlockState existing = level.getBlockState((BlockPos)pos);
            if (DifuChainFeature.canReplace(existing)) {
                level.setBlock((BlockPos)pos, Math.floorMod(y, 4) == 0 ? fence : brick, 2);
                placedAny = true;
            }
            if (random.nextInt(10) != 0) continue;
            curX += random.nextInt(3) - 1;
            curZ += random.nextInt(3) - 1;
        }
        return placedAny;
    }

    private static boolean canReplace(BlockState state) {
        return state.isAir() || state.is(Blocks.NETHERRACK) || state.is(Blocks.LAVA) || state.is(Blocks.SOUL_SAND) || state.is(Blocks.SOUL_SOIL) || state.is(Blocks.BONE_BLOCK) || state.is(Blocks.NETHER_BRICKS) || state.is(Blocks.NETHER_BRICK_FENCE);
    }
}

