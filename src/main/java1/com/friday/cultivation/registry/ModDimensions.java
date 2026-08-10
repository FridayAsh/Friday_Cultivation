package com.friday.cultivation.registry;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.dimension.DimensionType;

/**
 * 模组维度 - 地府维度注册键。
 * 完全照搬原 mod: xiaoxiang.cultivation.registry.ModDimensions
 */
public final class ModDimensions {
    public static final ResourceKey<Level> DIFU = ResourceKey.create(Registries.DIMENSION, new ResourceLocation("friday_cultivation", "difu"));
    public static final ResourceKey<DimensionType> DIFU_TYPE = ResourceKey.create(Registries.DIMENSION_TYPE, new ResourceLocation("friday_cultivation", "difu"));

    private ModDimensions() {
    }
}
