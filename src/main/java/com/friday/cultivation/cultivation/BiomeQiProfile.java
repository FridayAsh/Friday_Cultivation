/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.Holder
 *  net.minecraft.tags.BiomeTags
 *  net.minecraft.world.level.biome.Biome
 */
package com.friday.cultivation.cultivation;

import com.friday.cultivation.cultivation.QiElement;
import net.minecraft.core.Holder;
import net.minecraft.tags.BiomeTags;
import net.minecraft.world.level.biome.Biome;

public record BiomeQiProfile(double density, QiElement element) {
    public static final BiomeQiProfile SPARSE = new BiomeQiProfile(0.1, QiElement.EARTH);
    public static final BiomeQiProfile NORMAL = new BiomeQiProfile(0.35, QiElement.PURE);
    public static final BiomeQiProfile WOOD_RICH = new BiomeQiProfile(0.55, QiElement.WOOD);
    public static final BiomeQiProfile WATER_RICH = new BiomeQiProfile(0.5, QiElement.WATER);
    public static final BiomeQiProfile FIRE_RICH = new BiomeQiProfile(0.6, QiElement.FIRE);
    public static final BiomeQiProfile EARTH_RICH = new BiomeQiProfile(0.45, QiElement.EARTH);
    public static final BiomeQiProfile ICE_RICH = new BiomeQiProfile(0.5, QiElement.ICE);
    public static final BiomeQiProfile END_PURE = new BiomeQiProfile(0.45, QiElement.PURE);

    public static BiomeQiProfile of(Holder<Biome> biome) {
        if (biome.is(BiomeTags.IS_NETHER)) {
            return FIRE_RICH;
        }
        if (biome.is(BiomeTags.IS_END)) {
            return END_PURE;
        }
        if (biome.value().getBaseTemperature() < 0.2f) {
            return ICE_RICH;
        }
        if (biome.is(BiomeTags.IS_BADLANDS) || biome.is(BiomeTags.HAS_DESERT_PYRAMID)) {
            return SPARSE;
        }
        if (biome.is(BiomeTags.IS_DEEP_OCEAN) || biome.is(BiomeTags.IS_OCEAN) || biome.is(BiomeTags.IS_RIVER)) {
            return WATER_RICH;
        }
        if (biome.is(BiomeTags.IS_JUNGLE) || biome.is(BiomeTags.IS_FOREST)) {
            return WOOD_RICH;
        }
        if (biome.is(BiomeTags.IS_MOUNTAIN)) {
            return EARTH_RICH;
        }
        return NORMAL;
    }
}

