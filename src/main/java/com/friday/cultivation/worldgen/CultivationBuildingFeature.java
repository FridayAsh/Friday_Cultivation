/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.serialization.Codec
 *  net.minecraft.core.BlockPos
 *  net.minecraft.util.RandomSource
 *  net.minecraft.world.level.WorldGenLevel
 *  net.minecraft.world.level.levelgen.Heightmap$Types
 *  net.minecraft.world.level.levelgen.feature.Feature
 *  net.minecraft.world.level.levelgen.feature.FeaturePlaceContext
 *  net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration
 *  org.jetbrains.annotations.NotNull
 */
package com.friday.cultivation.worldgen;

import com.mojang.serialization.Codec;
import com.friday.cultivation.worldgen.Buildings;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import org.jetbrains.annotations.NotNull;

public class CultivationBuildingFeature
extends Feature<NoneFeatureConfiguration> {
    public CultivationBuildingFeature(Codec<NoneFeatureConfiguration> codec) {
        super(codec);
    }

    public boolean place(@NotNull FeaturePlaceContext<NoneFeatureConfiguration> ctx) {
        WorldGenLevel level = ctx.level();
        BlockPos origin = ctx.origin();
        RandomSource rand = ctx.random();
        int surfaceY = level.getHeight(Heightmap.Types.WORLD_SURFACE_WG, origin.getX(), origin.getZ());
        BlockPos pos = new BlockPos(origin.getX(), surfaceY, origin.getZ());
        Buildings.Type[] types = Buildings.Type.values();
        Buildings.Type type = types[rand.nextInt(types.length)];
        Buildings.build(level, pos, rand, type);
        return true;
    }
}

