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

public class DifuBoneFeature
extends Feature<NoneFeatureConfiguration> {
    public DifuBoneFeature(Codec<NoneFeatureConfiguration> codec) {
        super(codec);
    }

    public boolean place(@NotNull FeaturePlaceContext<NoneFeatureConfiguration> ctx) {
        WorldGenLevel level = ctx.level();
        RandomSource random = ctx.random();
        BlockPos origin = ctx.origin();
        boolean alongX = random.nextBoolean();
        Direction.Axis spineAxis = alongX ? Direction.Axis.X : Direction.Axis.Z;
        int len = 8 + random.nextInt(17);
        int ribSpacing = 2 + random.nextInt(3);
        int ribHeight = 4 + random.nextInt(6);
        int maxOut = 2 + random.nextInt(3);
        boolean hasSkull = random.nextInt(3) != 0;
        boolean sparse = random.nextBoolean();
        BlockState spineBone = (BlockState)Blocks.BONE_BLOCK.defaultBlockState().setValue((Property)RotatedPillarBlock.AXIS, (Comparable)spineAxis);
        BlockState ribBone = (BlockState)Blocks.BONE_BLOCK.defaultBlockState().setValue((Property)RotatedPillarBlock.AXIS, (Comparable)Direction.Axis.Y);
        int ox = origin.getX();
        int oz = origin.getZ();
        int baseY = level.getHeight(Heightmap.Types.WORLD_SURFACE_WG, ox, oz) - 1 - random.nextInt(2);
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        boolean placed = false;
        for (int i = 0; i < len; ++i) {
            int sz;
            int sx = alongX ? ox + i : ox;
            int n = sz = alongX ? oz : oz + i;
            if (this.placeBoneGrounded(level, (BlockPos)pos.set(sx, baseY, sz), spineBone)) {
                placed = true;
            }
            if (i <= 0 || i >= len - 1 || i % ribSpacing != 0 || sparse && random.nextInt(3) == 0) continue;
            for (int side = -1; side <= 1; side += 2) {
                for (int h = 1; h <= ribHeight; ++h) {
                    int rz;
                    double frac = (double)h / (double)ribHeight;
                    int out = (int)Math.round((double)maxOut * Math.sin(Math.PI * frac));
                    int rx = alongX ? sx : sx + side * out;
                    int n2 = rz = alongX ? sz + side * out : sz;
                    if (!this.placeBoneGrounded(level, (BlockPos)pos.set(rx, baseY + h, rz), ribBone)) continue;
                    placed = true;
                }
            }
        }
        if (hasSkull) {
            int skx = alongX ? ox - 2 : ox;
            int skz = alongX ? oz : oz - 2;
            for (int dx = -1; dx <= 1; ++dx) {
                for (int dz = -1; dz <= 1; ++dz) {
                    for (int dy = 0; dy <= 1; ++dy) {
                        if (dx == 0 && dz == 0 && dy == 0 || !this.placeBoneGrounded(level, (BlockPos)pos.set(skx + dx, baseY + dy, skz + dz), ribBone)) continue;
                        placed = true;
                    }
                }
            }
        }
        return placed;
    }

    private boolean placeBoneGrounded(WorldGenLevel level, BlockPos pos, BlockState state) {
        int surface = level.getHeight(Heightmap.Types.WORLD_SURFACE_WG, pos.getX(), pos.getZ());
        if (pos.getY() > surface + 1) {
            return false;
        }
        BlockState existing = level.getBlockState(pos);
        if (existing.isAir() || existing.is(Blocks.NETHERRACK) || existing.is(Blocks.LAVA) || existing.is(Blocks.SOUL_SAND) || existing.is(Blocks.SOUL_SOIL) || existing.is(Blocks.BONE_BLOCK)) {
            level.setBlock(pos, state, 2);
            return true;
        }
        return false;
    }
}

