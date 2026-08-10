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

public class DifuNetherFloraFeature extends Feature<NoneFeatureConfiguration> {
    public DifuNetherFloraFeature(Codec<NoneFeatureConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(@NotNull FeaturePlaceContext<NoneFeatureConfiguration> ctx) {
        WorldGenLevel level = ctx.level();
        RandomSource rand = ctx.random();
        BlockPos o = ctx.origin();
        boolean warped = rand.nextBoolean();
        BlockState nylium = (warped ? Blocks.WARPED_NYLIUM : Blocks.CRIMSON_NYLIUM).defaultBlockState();
        BlockState roots = (warped ? Blocks.WARPED_ROOTS : Blocks.CRIMSON_ROOTS).defaultBlockState();
        BlockState fungus = (warped ? Blocks.WARPED_FUNGUS : Blocks.CRIMSON_FUNGUS).defaultBlockState();
        BlockState stem = (warped ? Blocks.WARPED_STEM : Blocks.CRIMSON_STEM).defaultBlockState().setValue(RotatedPillarBlock.AXIS, Direction.Axis.Y);
        BlockState wart = (warped ? Blocks.WARPED_WART_BLOCK : Blocks.NETHER_WART_BLOCK).defaultBlockState();
        BlockState sprouts = Blocks.NETHER_SPROUTS.defaultBlockState();
        BlockPos.MutableBlockPos p = new BlockPos.MutableBlockPos();
        int patchR = 2 + rand.nextIntBetweenInclusive(0, 2);
        boolean placed = false;
        for (int dx = -patchR; dx <= patchR; ++dx) {
            for (int dz = -patchR; dz <= patchR; ++dz) {
                if (dx * dx + dz * dz > patchR * patchR) continue;
                int x = o.getX() + dx;
                int z = o.getZ() + dz;
                int topSolid = level.getHeight(Heightmap.Types.WORLD_SURFACE_WG, x, z) - 1;
                BlockState ground = level.getBlockState(p.set(x, topSolid, z));
                if (!ground.is(Blocks.NETHERRACK) && !ground.is(Blocks.SOUL_SAND) && !ground.is(Blocks.SOUL_SOIL)) continue;
                level.setBlock(p.set(x, topSolid, z), nylium, 2);
                placed = true;
                if (!level.getBlockState(p.set(x, topSolid + 1, z)).isAir()) continue;
                int r = rand.nextIntBetweenInclusive(0, 11);
                if (r < 4) {
                    level.setBlock(p.set(x, topSolid + 1, z), roots, 2);
                    continue;
                }
                if (r < 6) {
                    level.setBlock(p.set(x, topSolid + 1, z), sprouts, 2);
                    continue;
                }
                if (r != 6) continue;
                level.setBlock(p.set(x, topSolid + 1, z), fungus, 2);
            }
        }
        if (placed && rand.nextIntBetweenInclusive(0, 2) == 0) {
            int x = o.getX();
            int z = o.getZ();
            int base = level.getHeight(Heightmap.Types.WORLD_SURFACE_WG, x, z);
            int h = 4 + rand.nextIntBetweenInclusive(0, 3);
            for (int i = 0; i < h; ++i) {
                level.setBlock(p.set(x, base + i, z), stem, 2);
            }
            int capY = base + h;
            for (int dx = -1; dx <= 1; ++dx) {
                for (int dz = -1; dz <= 1; ++dz) {
                    level.setBlock(p.set(x + dx, capY, z + dz), wart, 2);
                }
            }
            level.setBlock(p.set(x, capY + 1, z), wart, 2);
            level.setBlock(p.set(x, capY - 1, z), Blocks.SHROOMLIGHT.defaultBlockState(), 2);
        }
        return placed;
    }
}
