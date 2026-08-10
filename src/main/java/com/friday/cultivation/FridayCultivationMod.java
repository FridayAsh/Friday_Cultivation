/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.logging.LogUtils
 *  net.minecraft.server.packs.resources.PreparableReloadListener
 *  net.minecraft.world.entity.EntityType
 *  net.minecraft.world.entity.SpawnPlacements$Type
 *  net.minecraft.world.level.levelgen.Heightmap$Types
 *  net.minecraftforge.api.distmarker.Dist
 *  net.minecraftforge.common.MinecraftForge
 *  net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent
 *  net.minecraftforge.event.AddReloadListenerEvent
 *  net.minecraftforge.event.entity.EntityAttributeCreationEvent
 *  net.minecraftforge.event.entity.SpawnPlacementRegisterEvent
 *  net.minecraftforge.event.entity.SpawnPlacementRegisterEvent$Operation
 *  net.minecraftforge.eventbus.api.IEventBus
 *  net.minecraftforge.eventbus.api.SubscribeEvent
 *  net.minecraftforge.fml.DistExecutor
 *  net.minecraftforge.fml.ModLoadingContext
 *  net.minecraftforge.fml.common.Mod
 *  net.minecraftforge.fml.config.IConfigSpec
 *  net.minecraftforge.fml.config.ModConfig$Type
 *  net.minecraftforge.fml.event.config.ModConfigEvent$Loading
 *  net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent
 *  net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext
 *  org.slf4j.Logger
 */
package com.friday.cultivation;

import com.mojang.logging.LogUtils;
import com.friday.cultivation.client.ClientConfigScreenRegistrar;
import com.friday.cultivation.config.ModClientConfig;
import com.friday.cultivation.config.ModCommonConfig;
import com.friday.cultivation.cultivation.CultivationData;
import com.friday.cultivation.cultivation.alchemy.datapack.PillEffectSpecLoader;
import com.friday.cultivation.cultivation.beast.BeastCultivationData;
import com.friday.cultivation.cultivation.qi.datapack.BlockQiSpecLoader;
import com.friday.cultivation.cultivation.qi.state.ChunkQiPool;
import com.friday.cultivation.entity.npc.SoulReaperEntity;
import com.friday.cultivation.entity.npc.WanderingCultivatorEntity;
import com.friday.cultivation.network.ModNetwork;
import com.friday.cultivation.registry.ModBlockEntities;
import com.friday.cultivation.registry.ModBlocks;
import com.friday.cultivation.registry.ModCreativeTabs;
import com.friday.cultivation.registry.ModEffects;
import com.friday.cultivation.registry.ModEntities;
import com.friday.cultivation.registry.ModFeatures;
import com.friday.cultivation.registry.ModItems;
import com.friday.cultivation.registry.ModLootModifiers;
import com.friday.cultivation.registry.ModMenuTypes;
import com.friday.cultivation.registry.ModParticles;
import com.friday.cultivation.registry.ModRecipes;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.SpawnPlacements;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.event.entity.SpawnPlacementRegisterEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.IConfigSpec;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.config.ModConfigEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;

@Mod(value="friday_cultivation")
public class FridayCultivationMod {
    public static final String MOD_ID = "friday_cultivation";
    public static final Logger LOGGER = LogUtils.getLogger();

    public FridayCultivationMod(FMLJavaModLoadingContext context) {
        IEventBus modEventBus = context.getModEventBus();
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, (IConfigSpec)ModCommonConfig.SPEC);
        ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, (IConfigSpec)ModClientConfig.SPEC);
        DistExecutor.unsafeRunWhenOn((Dist)Dist.CLIENT, () -> ClientConfigScreenRegistrar::register);
        ModBlocks.register(modEventBus);
        ModItems.register(modEventBus);
        ModCreativeTabs.register(modEventBus);
        ModParticles.register(modEventBus);
        ModRecipes.register(modEventBus);
        ModEntities.register(modEventBus);
        ModFeatures.register(modEventBus);
        ModMenuTypes.register(modEventBus);
        ModBlockEntities.register(modEventBus);
        ModEffects.register(modEventBus);
        ModLootModifiers.register(modEventBus);
        modEventBus.addListener(this::onCommonSetup);
        modEventBus.addListener(FridayCultivationMod::onModConfigLoading);
        modEventBus.addListener(FridayCultivationMod::onRegisterCapabilities);
        modEventBus.addListener(FridayCultivationMod::onEntityAttributeCreation);
        modEventBus.addListener(FridayCultivationMod::onSpawnPlacementRegister);
        MinecraftForge.EVENT_BUS.register((Object)this);
        LOGGER.info("[{}] \u5c0f\u7fd4\u7684\u4fee\u4ed9\u4e16\u754c \u5df2\u521d\u59cb\u5316\u5b8c\u6210", (Object)MOD_ID);
    }

    private void onCommonSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(ModNetwork::register);
    }

    private static void onModConfigLoading(ModConfigEvent.Loading event) {
        if (event.getConfig().getType() != ModConfig.Type.COMMON || !MOD_ID.equals(event.getConfig().getModId())) {
            return;
        }
        if (ModCommonConfig.migrateLegacySectSpawnDensityDefaults()) {
            LOGGER.info("[{}] Migrated legacy sect spawn density config defaults to 0.34/0.60", (Object)MOD_ID);
        }
    }

    private static void onRegisterCapabilities(RegisterCapabilitiesEvent event) {
        event.register(CultivationData.class);
        event.register(BeastCultivationData.class);
        event.register(ChunkQiPool.class);
    }

    private static void onEntityAttributeCreation(EntityAttributeCreationEvent event) {
        event.put((EntityType)ModEntities.WANDERING_CULTIVATOR.get(), WanderingCultivatorEntity.createAttributes().build());
        event.put((EntityType)ModEntities.SOUL_REAPER.get(), SoulReaperEntity.createAttributes().build());
    }

    private static void onSpawnPlacementRegister(SpawnPlacementRegisterEvent event) {
        event.register((EntityType)ModEntities.WANDERING_CULTIVATOR.get(), SpawnPlacements.Type.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, WanderingCultivatorEntity::checkCultivatorSpawnRules, SpawnPlacementRegisterEvent.Operation.REPLACE);
    }

    @SubscribeEvent
    public void onAddReloadListeners(AddReloadListenerEvent event) {
        event.addListener((PreparableReloadListener)new BlockQiSpecLoader());
        event.addListener((PreparableReloadListener)new PillEffectSpecLoader());
    }
}

