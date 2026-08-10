package com.friday.cultivation;

import net.minecraft.core.Holder;
import net.minecraft.tags.BiomeTags;
import net.minecraft.world.level.biome.Biome;

/**
 * 群系灵气属性 - 不同群系对应的灵气密度与主导五行。
 * 完全照搬原 mod: xiaoxiang.cultivation.cultivation.BiomeQiProfile
 */
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
        if (biome.is(BiomeTags.IS_NETHER)) return FIRE_RICH;
        if (biome.is(BiomeTags.IS_END)) return END_PURE;
        if (biome.value().getBaseTemperature() < 0.2f) return ICE_RICH;
        if (biome.is(BiomeTags.IS_BADLANDS) || biome.is(BiomeTags.IS_BADLANDS)) return SPARSE;
        if (biome.is(BiomeTags.IS_OCEAN) || biome.is(BiomeTags.IS_DEEP_OCEAN) || biome.is(BiomeTags.IS_RIVER)) return WATER_RICH;
        if (biome.is(BiomeTags.IS_FOREST) || biome.is(BiomeTags.IS_JUNGLE)) return WOOD_RICH;
        if (biome.is(BiomeTags.IS_HILL)) return EARTH_RICH;
        return NORMAL;
    }
}
