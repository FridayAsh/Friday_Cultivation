package com.friday.cultivation;

import com.friday.cultivation.alchemy.datapack.PillEffectSpecLoader;
import com.friday.cultivation.client.ClientKeybindings;
import com.friday.cultivation.client.CultivationHud;
import com.friday.cultivation.config.ModClientConfig;
import com.friday.cultivation.config.ModCommonConfig;
import com.friday.cultivation.entity.npc.WanderingCultivatorEntity;
import com.friday.cultivation.event.CapabilityEvents;
import com.friday.cultivation.item.ModItems;
import com.friday.cultivation.network.ModNetwork;
import com.friday.cultivation.qi.datapack.BlockQiSpecLoader;
import com.friday.cultivation.registry.ModEffects;
import com.friday.cultivation.registry.ModEntities;
import com.mojang.logging.LogUtils;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.world.entity.SpawnPlacements;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.event.entity.SpawnPlacementRegisterEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.config.ModConfigEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLEnvironment;
import org.slf4j.Logger;

@Mod(FridayCultivationMod.MOD_ID)
public class FridayCultivationMod {

    public static final String MOD_ID = "friday_cultivation";
    public static final Logger LOGGER = LogUtils.getLogger();

    public FridayCultivationMod() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        IEventBus forgeEventBus = MinecraftForge.EVENT_BUS;

        // ── 配置注册 ──
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, ModCommonConfig.SPEC);
        ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, ModClientConfig.SPEC);

        // ── Capability 注册 ──
        modEventBus.addListener(this::registerCapabilities);
        modEventBus.addListener(FridayCultivationMod::onModConfigLoading);
        modEventBus.addListener(FridayCultivationMod::onSpawnPlacementRegister);

        // ── 物品注册 ──
        ModItems.ITEMS.register(modEventBus);
        com.friday.cultivation.registry.ModCreativeTabs.register(modEventBus);

        // ── 方块/菜单注册 ──
        com.friday.cultivation.registry.ModBlocks.register(modEventBus);
        com.friday.cultivation.registry.ModBlockEntities.register(modEventBus);
        com.friday.cultivation.registry.ModMenuTypes.register(modEventBus);

        // ── 药水效果/配方/粒子注册 ──
        ModEffects.EFFECTS.register(modEventBus);
        com.friday.cultivation.registry.ModRecipes.register(modEventBus);
        com.friday.cultivation.registry.ModParticles.register(modEventBus);

        // ── 实体/世界生成/战利品注册 ──
        ModEntities.register(modEventBus);
        com.friday.cultivation.registry.ModFeatures.register(modEventBus);
        com.friday.cultivation.registry.ModLootModifiers.register(modEventBus);
        modEventBus.addListener(FridayCultivationMod::registerEntityAttributes);

        // ── 通用设置 ──
        modEventBus.addListener(this::commonSetup);

        // ── Forge 事件注册 ──
        // 客户端注册（渲染器/按键/overlay/粒子 Provider/菜单屏幕）全部由 ClientSetup
        // 的 @Mod.EventBusSubscriber(bus=MOD, value=CLIENT) 自动处理，无需在主类手动 addListener。
        forgeEventBus.register(CapabilityEvents.class);
        forgeEventBus.register(this);
        // IdentityDrawHandler 使用 @Mod.EventBusSubscriber 自动注册

        LOGGER.info("[{}] 小翔的修仙世界 已初始化完成", MOD_ID);
    }

    private void registerCapabilities(RegisterCapabilitiesEvent event) {
        event.register(com.friday.cultivation.capability.CultivationData.class);
        event.register(com.friday.cultivation.beast.BeastCultivationData.class);
        com.friday.cultivation.qi.state.ChunkQiCapability.register(event);
    }

    private static void onModConfigLoading(ModConfigEvent.Loading event) {
        if (event.getConfig().getType() != ModConfig.Type.COMMON || !MOD_ID.equals(event.getConfig().getModId())) {
            return;
        }
        if (ModCommonConfig.migrateLegacySectSpawnDensityDefaults()) {
            LOGGER.info("[{}] Migrated legacy sect spawn density config defaults to 0.34/0.60", MOD_ID);
        }
    }

    /** 注册实体属性 */
    private static void registerEntityAttributes(net.minecraftforge.event.entity.EntityAttributeCreationEvent event) {
        event.put(ModEntities.SOUL_REAPER.get(), com.friday.cultivation.entity.npc.SoulReaperEntity.createAttributes().build());
        event.put(ModEntities.WANDERING_CULTIVATOR.get(), WanderingCultivatorEntity.createAttributes().build());
    }

    private static void onSpawnPlacementRegister(SpawnPlacementRegisterEvent event) {
        event.register(ModEntities.WANDERING_CULTIVATOR.get(), SpawnPlacements.Type.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, WanderingCultivatorEntity::checkCultivatorSpawnRules, SpawnPlacementRegisterEvent.Operation.REPLACE);
    }

    @SubscribeEvent
    public void onAddReloadListeners(AddReloadListenerEvent event) {
        event.addListener((PreparableReloadListener) new BlockQiSpecLoader());
        event.addListener((PreparableReloadListener) new PillEffectSpecLoader());
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            ModNetwork.register();
            // MenuScreens 注册（ALCHEMY/REFINING/FORMATION/WANDERING_CULTIVATOR）全部由
            // ClientSetup.onClientSetup 统一处理，此处不再重复注册，避免 Duplicate registration。
        });
    }
}
