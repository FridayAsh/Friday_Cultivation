/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.registries.Registries
 *  net.minecraft.resources.ResourceKey
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.level.dimension.DimensionType
 */
package com.friday.cultivation.registry;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.dimension.DimensionType;

public final class ModDimensions {
    public static final ResourceKey<Level> DIFU = ResourceKey.create((ResourceKey)Registries.DIMENSION, (ResourceLocation)new ResourceLocation("friday_cultivation", "difu"));
    public static final ResourceKey<DimensionType> DIFU_TYPE = ResourceKey.create((ResourceKey)Registries.DIMENSION_TYPE, (ResourceLocation)new ResourceLocation("friday_cultivation", "difu"));

    private ModDimensions() {
    }
}

