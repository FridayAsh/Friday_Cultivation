/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.serialization.Codec
 *  net.minecraft.core.BlockPos
 *  net.minecraft.core.BlockPos$MutableBlockPos
 *  net.minecraft.core.Direction$Axis
 *  net.minecraft.util.RandomSource
 *  net.minecraft.world.level.WorldGenLevel
 *  net.minecraft.world.level.block.Blocks
 *  net.minecraft.world.level.block.RotatedPillarBlock
 *  net.minecraft.world.level.block.state.BlockState
 *  net.minecraft.world.level.block.state.properties.Property
 *  net.minecraft.world.level.levelgen.Heightmap$Types
 *  net.minecraft.world.level.levelgen.feature.Feature
 *  net.minecraft.world.level.levelgen.feature.FeaturePlaceContext
 *  net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration
 *  org.jetbrains.annotations.NotNull
 */
package com.friday.cultivation.worldgen;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import org.jetbrains.annotations.NotNull;

public class DifuNetherFloraFeature
extends Feature<NoneFeatureConfiguration> {
    public DifuNetherFloraFeature(Codec<NoneFeatureConfiguration> codec) {
        super(codec);
    }

    public boolean place(@NotNull FeaturePlaceContext<NoneFeatureConfiguration> ctx) {
        WorldGenLevel level = ctx.level();
        RandomSource rand = ctx.random();
        BlockPos o = ctx.origin();
        boolean warped = rand.nextBoolean();
        BlockState nylium = (warped ? Blocks.WARPED_NYLIUM : Blocks.CRIMSON_NYLIUM).defaultBlockState();
        BlockState roots = (warped ? Blocks.WARPED_ROOTS : Blocks.CRIMSON_ROOTS).defaultBlockState();
        BlockState fungus = (warped ? Blocks.WARPED_FUNGUS : Blocks.CRIMSON_FUNGUS).defaultBlockState();
        BlockState stem = (BlockState)(warped ? Blocks.WARPED_STEM : Blocks.CRIMSON_STEM).defaultBlockState().setValue((Property)RotatedPillarBlock.AXIS, (Comparable)Direction.Axis.Y);
        BlockState wart = (warped ? Blocks.WARPED_WART_BLOCK : Blocks.NETHER_WART_BLOCK).defaultBlockState();
        BlockState sprouts = Blocks.NETHER_SPROUTS.defaultBlockState();
        BlockPos.MutableBlockPos p = new BlockPos.MutableBlockPos();
        int patchR = 2 + rand.nextInt(3);
        boolean placed = false;
        for (int dx = -patchR; dx <= patchR; ++dx) {
            for (int dz = -patchR; dz <= patchR; ++dz) {
                int z;
                int topSolid;
                int x;
                BlockState ground;
                if (dx * dx + dz * dz > patchR * patchR || !(ground = level.getBlockState((BlockPos)p.set(x = o.getX() + dx, topSolid = level.getHeight(Heightmap.Types.WORLD_SURFACE_WG, x, z = o.getZ() + dz) - 1, z))).is(Blocks.NETHERRACK) && !ground.is(Blocks.SOUL_SAND) && !ground.is(Blocks.SOUL_SOIL)) continue;
                level.setBlock((BlockPos)p.set(x, topSolid, z), nylium, 2);
                placed = true;
                if (!level.getBlockState((BlockPos)p.set(x, topSolid + 1, z)).isAir()) continue;
                int r = rand.nextInt(12);
                if (r < 4) {
                    level.setBlock((BlockPos)p.set(x, topSolid + 1, z), roots, 2);
                    continue;
                }
                if (r < 6) {
                    level.setBlock((BlockPos)p.set(x, topSolid + 1, z), sprouts, 2);
                    continue;
                }
                if (r != 6) continue;
                level.setBlock((BlockPos)p.set(x, topSolid + 1, z), fungus, 2);
            }
        }
        if (placed && rand.nextInt(3) == 0) {
            int x = o.getX();
            int z = o.getZ();
            int base = level.getHeight(Heightmap.Types.WORLD_SURFACE_WG, x, z);
            int h = 4 + rand.nextInt(4);
            for (int i = 0; i < h; ++i) {
                level.setBlock((BlockPos)p.set(x, base + i, z), stem, 2);
            }
            int capY = base + h;
            for (int dx = -1; dx <= 1; ++dx) {
                for (int dz = -1; dz <= 1; ++dz) {
                    level.setBlock((BlockPos)p.set(x + dx, capY, z + dz), wart, 2);
                }
            }
            level.setBlock((BlockPos)p.set(x, capY + 1, z), wart, 2);
            level.setBlock((BlockPos)p.set(x, capY - 1, z), Blocks.SHROOMLIGHT.defaultBlockState(), 2);
        }
        return placed;
    }
}

