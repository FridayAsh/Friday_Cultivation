package com.friday.cultivation.registry;

import com.mojang.serialization.Codec;
import com.friday.cultivation.worldgen.*;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

/**
 * 修仙世界生成 Feature 注册表（严格照搬原模组 com.xiaoxiang.cultivation.registry.ModFeatures）
 */
public final class ModFeatures {
    public static final DeferredRegister<Feature<?>> FEATURES =
            DeferredRegister.create(ForgeRegistries.FEATURES, "friday_cultivation");

    public static final RegistryObject<Feature<NoneFeatureConfiguration>> CULTIVATION_BUILDING =
            FEATURES.register("cultivation_building",
                    () -> new CultivationBuildingFeature(NoneFeatureConfiguration.CODEC));
    public static final RegistryObject<Feature<NoneFeatureConfiguration>> SECT_SETTLEMENT =
            FEATURES.register("sect_settlement",
                    () -> new SectSettlementFeature(NoneFeatureConfiguration.CODEC));
    public static final RegistryObject<Feature<NoneFeatureConfiguration>> SPIRIT_VEIN_SPRING =
            FEATURES.register("spirit_vein_spring",
                    () -> new SpiritVeinSpringFeature(NoneFeatureConfiguration.CODEC));
    public static final RegistryObject<Feature<NoneFeatureConfiguration>> DIFU_CHAIN =
            FEATURES.register("difu_chain",
                    () -> new DifuChainFeature(NoneFeatureConfiguration.CODEC));
    public static final RegistryObject<Feature<NoneFeatureConfiguration>> DIFU_BONE =
            FEATURES.register("difu_bone",
                    () -> new DifuBoneFeature(NoneFeatureConfiguration.CODEC));
    public static final RegistryObject<Feature<NoneFeatureConfiguration>> DIFU_BIG_CHAIN =
            FEATURES.register("difu_big_chain",
                    () -> new DifuBigChainFeature(NoneFeatureConfiguration.CODEC));
    public static final RegistryObject<Feature<NoneFeatureConfiguration>> DIFU_VILLAGE =
            FEATURES.register("difu_village",
                    () -> new DifuVillageFeature(NoneFeatureConfiguration.CODEC));
    public static final RegistryObject<Feature<NoneFeatureConfiguration>> DIFU_FLORA =
            FEATURES.register("difu_flora",
                    () -> new DifuNetherFloraFeature(NoneFeatureConfiguration.CODEC));

    private ModFeatures() {}

    public static void register(IEventBus bus) {
        FEATURES.register(bus);
    }
}