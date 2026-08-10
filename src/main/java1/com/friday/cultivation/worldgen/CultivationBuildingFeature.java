package com.friday.cultivation.worldgen;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import org.jetbrains.annotations.NotNull;

/**
 * 修仙建筑生成器Feature（严格照搬原模组 com.xiaoxiang.cultivation.worldgen.CultivationBuildingFeature）
 * 在世界生成时放置修仙建筑。
 */
public class CultivationBuildingFeature extends Feature<NoneFeatureConfiguration> {

    public CultivationBuildingFeature(Codec<NoneFeatureConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(@NotNull FeaturePlaceContext<NoneFeatureConfiguration> ctx) {
        WorldGenLevel level = ctx.level();
        RandomSource rand = ctx.random();
        BlockPos origin = ctx.origin();
        int typeOrd = rand.nextInt(Buildings.Type.values().length);
        Buildings.Type type = Buildings.Type.values()[typeOrd];
        Buildings.build(level, origin, rand, type);
        return true;
    }
}
