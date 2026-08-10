/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.serialization.Codec
 *  net.minecraft.world.level.levelgen.feature.Feature
 *  net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration
 *  net.minecraftforge.eventbus.api.IEventBus
 *  net.minecraftforge.registries.DeferredRegister
 *  net.minecraftforge.registries.ForgeRegistries
 *  net.minecraftforge.registries.IForgeRegistry
 *  net.minecraftforge.registries.RegistryObject
 */
package com.friday.cultivation.registry;

import com.mojang.serialization.Codec;
import com.friday.cultivation.worldgen.CultivationBuildingFeature;
import com.friday.cultivation.worldgen.DifuBigChainFeature;
import com.friday.cultivation.worldgen.DifuBoneFeature;
import com.friday.cultivation.worldgen.DifuChainFeature;
import com.friday.cultivation.worldgen.DifuNetherFloraFeature;
import com.friday.cultivation.worldgen.DifuVillageFeature;
import com.friday.cultivation.worldgen.SectSettlementFeature;
import com.friday.cultivation.worldgen.SpiritVeinSpringFeature;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.RegistryObject;

public final class ModFeatures {
    public static final DeferredRegister<Feature<?>> FEATURES = DeferredRegister.create((IForgeRegistry)ForgeRegistries.FEATURES, (String)"friday_cultivation");
    public static final RegistryObject<Feature<NoneFeatureConfiguration>> CULTIVATION_BUILDING = FEATURES.register("cultivation_building", () -> new CultivationBuildingFeature((Codec<NoneFeatureConfiguration>)NoneFeatureConfiguration.CODEC));
    public static final RegistryObject<Feature<NoneFeatureConfiguration>> SECT_SETTLEMENT = FEATURES.register("sect_settlement", () -> new SectSettlementFeature((Codec<NoneFeatureConfiguration>)NoneFeatureConfiguration.CODEC));
    public static final RegistryObject<Feature<NoneFeatureConfiguration>> SPIRIT_VEIN_SPRING = FEATURES.register("spirit_vein_spring", () -> new SpiritVeinSpringFeature((Codec<NoneFeatureConfiguration>)NoneFeatureConfiguration.CODEC));
    public static final RegistryObject<Feature<NoneFeatureConfiguration>> DIFU_CHAIN = FEATURES.register("difu_chain", () -> new DifuChainFeature((Codec<NoneFeatureConfiguration>)NoneFeatureConfiguration.CODEC));
    public static final RegistryObject<Feature<NoneFeatureConfiguration>> DIFU_BONE = FEATURES.register("difu_bone", () -> new DifuBoneFeature((Codec<NoneFeatureConfiguration>)NoneFeatureConfiguration.CODEC));
    public static final RegistryObject<Feature<NoneFeatureConfiguration>> DIFU_BIG_CHAIN = FEATURES.register("difu_big_chain", () -> new DifuBigChainFeature((Codec<NoneFeatureConfiguration>)NoneFeatureConfiguration.CODEC));
    public static final RegistryObject<Feature<NoneFeatureConfiguration>> DIFU_VILLAGE = FEATURES.register("difu_village", () -> new DifuVillageFeature((Codec<NoneFeatureConfiguration>)NoneFeatureConfiguration.CODEC));
    public static final RegistryObject<Feature<NoneFeatureConfiguration>> DIFU_FLORA = FEATURES.register("difu_flora", () -> new DifuNetherFloraFeature((Codec<NoneFeatureConfiguration>)NoneFeatureConfiguration.CODEC));

    private ModFeatures() {
    }

    public static void register(IEventBus bus) {
        FEATURES.register(bus);
    }
}

