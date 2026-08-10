package com.friday.cultivation.client;

import com.friday.cultivation.client.particle.AmbientQiParticle;
import com.friday.cultivation.client.particle.BreakthroughParticle;
import com.friday.cultivation.client.particle.QiAbsorbParticle;
import com.friday.cultivation.client.renderer.BuddhaFireLotusRenderer;
import com.friday.cultivation.client.renderer.CorpseRenderer;
import com.friday.cultivation.client.renderer.GreatFireballRenderer;
import com.friday.cultivation.client.renderer.HeavenPiercingConeRenderer;
import com.friday.cultivation.client.renderer.IceShellRenderer;
import com.friday.cultivation.client.renderer.MeteorRenderer;
import com.friday.cultivation.client.renderer.NoopEntityRenderer;
import com.friday.cultivation.client.renderer.PalmThunderOrbRenderer;
import com.friday.cultivation.client.renderer.QiOrbRenderer;
import com.friday.cultivation.client.renderer.ShockwaveRenderer;
import com.friday.cultivation.client.renderer.SkySplittingSwordAuraRenderer;
import com.friday.cultivation.client.renderer.SkyTrailRenderer;
import com.friday.cultivation.client.renderer.SoulReaperRenderer;
import com.friday.cultivation.client.renderer.StoneBulletRenderer;
import com.friday.cultivation.client.renderer.SwordAuraRenderer;
import com.friday.cultivation.client.renderer.SwordProjectileRenderer;
import com.friday.cultivation.client.renderer.WanderingCultivatorRenderer;
import com.friday.cultivation.client.screen.AlchemyScreen;
import com.friday.cultivation.client.screen.FormationScreen;
import com.friday.cultivation.client.screen.RefiningScreen;
import com.friday.cultivation.client.screen.WanderingCultivatorScreen;
import com.friday.cultivation.item.ModItems;
import com.friday.cultivation.registry.ModBlocks;
import com.friday.cultivation.registry.ModEntities;
import com.friday.cultivation.registry.ModMenuTypes;
import com.friday.cultivation.registry.ModParticles;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.ThrownItemRenderer;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.IItemDecorator;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.client.event.RegisterGuiOverlaysEvent;
import net.minecraftforge.client.event.RegisterItemDecorationsEvent;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.client.event.RegisterParticleProvidersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

/**
 * 客户端初始化 — 完全照搬原模组 com.xiaoxiang.cultivation.client.ClientSetup。
 * 注册实体渲染器 / 按键 / 粒子 / 菜单与渲染层 / 物品装饰 / GUI 覆盖层。
 */
@Mod.EventBusSubscriber(modid = "friday_cultivation", bus = Mod.EventBusSubscriber.Bus.MOD, value = {Dist.CLIENT})
public final class ClientSetup {
    private ClientSetup() {
    }

    @SubscribeEvent
    public static void onRegisterRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(ModEntities.QI_ORB.get(), QiOrbRenderer::new);
        event.registerEntityRenderer(ModEntities.SEAT.get(), NoopEntityRenderer::new);
        event.registerEntityRenderer(ModEntities.XIAOXIANG_FIREBALL.get(), ctx -> new ThrownItemRenderer(ctx, 3.0f, true));
        event.registerEntityRenderer(ModEntities.GREAT_FIREBALL.get(), GreatFireballRenderer::new);
        event.registerEntityRenderer(ModEntities.SWORD_PROJECTILE.get(), SwordProjectileRenderer::new);
        event.registerEntityRenderer(ModEntities.STONE_BULLET.get(), StoneBulletRenderer::new);
        event.registerEntityRenderer(ModEntities.HEAVEN_PIERCING_CONE.get(), HeavenPiercingConeRenderer::new);
        event.registerEntityRenderer(ModEntities.BUDDHA_FIRE_LOTUS.get(), BuddhaFireLotusRenderer::new);
        event.registerEntityRenderer(ModEntities.PALM_THUNDER_ORB.get(), PalmThunderOrbRenderer::new);
        event.registerEntityRenderer(ModEntities.METEOR.get(), MeteorRenderer::new);
        event.registerEntityRenderer(ModEntities.MUSHROOM_CLOUD.get(), NoopEntityRenderer::new);
        event.registerEntityRenderer(ModEntities.SHOCKWAVE.get(), ShockwaveRenderer::new);
        event.registerEntityRenderer(ModEntities.SWORD_AURA.get(), SwordAuraRenderer::new);
        event.registerEntityRenderer(ModEntities.ICE_SHELL.get(), IceShellRenderer::new);
        event.registerEntityRenderer(ModEntities.SKY_SPLITTING_SWORD_AURA.get(), SkySplittingSwordAuraRenderer::new);
        event.registerEntityRenderer(ModEntities.SKY_TRAIL.get(), SkyTrailRenderer::new);
        event.registerEntityRenderer(ModEntities.WANDERING_CULTIVATOR.get(), WanderingCultivatorRenderer::new);
        event.registerEntityRenderer(ModEntities.SOUL_REAPER.get(), SoulReaperRenderer::new);
        event.registerEntityRenderer(ModEntities.CORPSE.get(), CorpseRenderer::new);
    }

    @SubscribeEvent
    public static void onRegisterKeys(RegisterKeyMappingsEvent event) {
        ClientKeybindings.register(event);
    }

    @SubscribeEvent
    public static void onRegisterParticleProviders(RegisterParticleProvidersEvent event) {
        event.registerSpriteSet(ModParticles.AMBIENT_QI.get(), set -> new AmbientQiParticle.Provider(set, 1.0f, 0.95f, 0.65f));
        event.registerSpriteSet(ModParticles.AMBIENT_QI_METAL.get(), set -> new AmbientQiParticle.Provider(set, 0.85f, 0.85f, 0.95f));
        event.registerSpriteSet(ModParticles.AMBIENT_QI_WOOD.get(), set -> new AmbientQiParticle.Provider(set, 0.4f, 0.85f, 0.4f));
        event.registerSpriteSet(ModParticles.AMBIENT_QI_WATER.get(), set -> new AmbientQiParticle.Provider(set, 0.4f, 0.7f, 1.0f));
        event.registerSpriteSet(ModParticles.AMBIENT_QI_FIRE.get(), set -> new AmbientQiParticle.Provider(set, 1.0f, 0.4f, 0.3f));
        event.registerSpriteSet(ModParticles.AMBIENT_QI_EARTH.get(), set -> new AmbientQiParticle.Provider(set, 0.85f, 0.7f, 0.4f));
        event.registerSpriteSet(ModParticles.AMBIENT_QI_ICE.get(), set -> new AmbientQiParticle.Provider(set, 0.72f, 0.88f, 1.0f));
        event.registerSpriteSet(ModParticles.AMBIENT_QI_LIGHTNING.get(), set -> new AmbientQiParticle.Provider(set, 0.88f, 0.9f, 1.0f));
        event.registerSpriteSet(ModParticles.AMBIENT_QI_LOTUS.get(), set -> new AmbientQiParticle.Provider(set, 0.95f, 0.2f, 0.95f));
        event.registerSpriteSet(ModParticles.YIN_QI.get(), set -> new AmbientQiParticle.Provider(set, 0.55f, 0.5f, 0.8f));
        event.registerSpriteSet(ModParticles.BREAKTHROUGH.get(), BreakthroughParticle.Provider::new);
        event.registerSpriteSet(ModParticles.QI_ABSORB.get(), QiAbsorbParticle.Provider::new);
    }

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            MenuScreens.register(ModMenuTypes.WANDERING_CULTIVATOR.get(), WanderingCultivatorScreen::new);
            MenuScreens.register(ModMenuTypes.ALCHEMY.get(), AlchemyScreen::new);
            MenuScreens.register(ModMenuTypes.REFINING.get(), RefiningScreen::new);
            MenuScreens.register(ModMenuTypes.FORMATION_MENU.get(), FormationScreen::new);
            ItemBlockRenderTypes.setRenderLayer(ModBlocks.HERB.get(), RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer(ModBlocks.FORMATION_RUNE.get(), RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer(ModBlocks.BONE_REMAINS.get(), RenderType.cutout());
        });
    }

    @SubscribeEvent
    public static void onRegisterItemDecorations(RegisterItemDecorationsEvent event) {
        PillGlowDecorator decorator = new PillGlowDecorator();
        event.register(ModItems.PILL_QI_RECOVERY_LOW.get(), decorator);
        event.register(ModItems.PILL_QI_RECOVERY_MID.get(), decorator);
        event.register(ModItems.PILL_QI_RECOVERY_HIGH.get(), decorator);
        event.register(ModItems.PILL_QI_RECOVERY_SUPREME.get(), decorator);
        event.register(ModItems.PILL_QI_RECOVERY_IMMORTAL.get(), decorator);
        event.register(ModItems.PILL_CULTIVATION_LOW.get(), decorator);
        event.register(ModItems.PILL_CULTIVATION_MID.get(), decorator);
        event.register(ModItems.PILL_CULTIVATION_HIGH.get(), decorator);
        event.register(ModItems.PILL_CULTIVATION_SUPREME.get(), decorator);
        event.register(ModItems.PILL_CULTIVATION_IMMORTAL.get(), decorator);
        event.register(ModItems.PILL_BLOOD_BURN_LOW.get(), decorator);
        event.register(ModItems.PILL_BLOOD_BURN_MID.get(), decorator);
        event.register(ModItems.PILL_BLOOD_BURN_HIGH.get(), decorator);
        event.register(ModItems.PILL_BLOOD_BURN_SUPREME.get(), decorator);
        event.register(ModItems.PILL_BLOOD_BURN_IMMORTAL.get(), decorator);
        event.register(ModItems.PILL_CLEAR_MIND_LOW.get(), decorator);
        event.register(ModItems.PILL_CLEAR_MIND_MID.get(), decorator);
        event.register(ModItems.PILL_CLEAR_MIND_HIGH.get(), decorator);
        event.register(ModItems.PILL_CLEAR_MIND_SUPREME.get(), decorator);
        event.register(ModItems.PILL_CLEAR_MIND_IMMORTAL.get(), decorator);
        event.register(ModItems.PILL_REJUVENATION_LOW.get(), decorator);
        event.register(ModItems.PILL_REJUVENATION_MID.get(), decorator);
        event.register(ModItems.PILL_REJUVENATION_HIGH.get(), decorator);
        event.register(ModItems.PILL_REJUVENATION_SUPREME.get(), decorator);
        event.register(ModItems.PILL_REJUVENATION_IMMORTAL.get(), decorator);
        event.register(ModItems.PILL_DIVINE_STRIDE_LOW.get(), decorator);
        event.register(ModItems.PILL_DIVINE_STRIDE_MID.get(), decorator);
        event.register(ModItems.PILL_DIVINE_STRIDE_HIGH.get(), decorator);
        event.register(ModItems.PILL_DIVINE_STRIDE_SUPREME.get(), decorator);
        event.register(ModItems.PILL_DIVINE_STRIDE_IMMORTAL.get(), decorator);
        WeaponGlowDecorator weaponDeco = new WeaponGlowDecorator();
        event.register(ModItems.XUAN_IRON_SWORD_LOW.get(), weaponDeco);
        event.register(ModItems.XUAN_IRON_SWORD_MID.get(), weaponDeco);
        event.register(ModItems.XUAN_IRON_SWORD_HIGH.get(), weaponDeco);
        event.register(ModItems.XUAN_IRON_SWORD_SUPREME.get(), weaponDeco);
        event.register(ModItems.XUAN_IRON_SWORD_IMMORTAL.get(), weaponDeco);
        event.register(ModItems.QING_MU_SWORD_LOW.get(), weaponDeco);
        event.register(ModItems.QING_MU_SWORD_MID.get(), weaponDeco);
        event.register(ModItems.QING_MU_SWORD_HIGH.get(), weaponDeco);
        event.register(ModItems.QING_MU_SWORD_SUPREME.get(), weaponDeco);
        event.register(ModItems.QING_MU_SWORD_IMMORTAL.get(), weaponDeco);
        event.register(ModItems.CHI_YAN_SWORD_LOW.get(), weaponDeco);
        event.register(ModItems.CHI_YAN_SWORD_MID.get(), weaponDeco);
        event.register(ModItems.CHI_YAN_SWORD_HIGH.get(), weaponDeco);
        event.register(ModItems.CHI_YAN_SWORD_SUPREME.get(), weaponDeco);
        event.register(ModItems.CHI_YAN_SWORD_IMMORTAL.get(), weaponDeco);
        event.register(ModItems.HAN_BING_SWORD_LOW.get(), weaponDeco);
        event.register(ModItems.HAN_BING_SWORD_MID.get(), weaponDeco);
        event.register(ModItems.HAN_BING_SWORD_HIGH.get(), weaponDeco);
        event.register(ModItems.HAN_BING_SWORD_SUPREME.get(), weaponDeco);
        event.register(ModItems.HAN_BING_SWORD_IMMORTAL.get(), weaponDeco);
        FlagGlowDecorator flagDeco = new FlagGlowDecorator();
        event.register(ModItems.LOW_QI_GATHERING_FLAG.get(), flagDeco);
        event.register(ModItems.MID_QI_GATHERING_FLAG.get(), flagDeco);
        event.register(ModItems.HIGH_QI_GATHERING_FLAG.get(), flagDeco);
        event.register(ModItems.SUPREME_QI_GATHERING_FLAG.get(), flagDeco);
        event.register(ModItems.IMMORTAL_QI_GATHERING_FLAG.get(), flagDeco);
        event.register(ModItems.LOW_SECT_PROTECTION_FLAG.get(), flagDeco);
        event.register(ModItems.MID_SECT_PROTECTION_FLAG.get(), flagDeco);
        event.register(ModItems.HIGH_SECT_PROTECTION_FLAG.get(), flagDeco);
        event.register(ModItems.SUPREME_SECT_PROTECTION_FLAG.get(), flagDeco);
        event.register(ModItems.IMMORTAL_SECT_PROTECTION_FLAG.get(), flagDeco);
        event.register(ModItems.LOW_WITHER_GROWTH_FLAG.get(), flagDeco);
        event.register(ModItems.MID_WITHER_GROWTH_FLAG.get(), flagDeco);
        event.register(ModItems.HIGH_WITHER_GROWTH_FLAG.get(), flagDeco);
        event.register(ModItems.SUPREME_WITHER_GROWTH_FLAG.get(), flagDeco);
        event.register(ModItems.IMMORTAL_WITHER_GROWTH_FLAG.get(), flagDeco);
        event.register(ModItems.LOW_REJUVENATION_FLAG.get(), flagDeco);
        event.register(ModItems.MID_REJUVENATION_FLAG.get(), flagDeco);
        event.register(ModItems.HIGH_REJUVENATION_FLAG.get(), flagDeco);
        event.register(ModItems.SUPREME_REJUVENATION_FLAG.get(), flagDeco);
        event.register(ModItems.IMMORTAL_REJUVENATION_FLAG.get(), flagDeco);
        event.register(ModItems.LOW_FLIGHT_BAN_FLAG.get(), flagDeco);
        event.register(ModItems.MID_FLIGHT_BAN_FLAG.get(), flagDeco);
        event.register(ModItems.HIGH_FLIGHT_BAN_FLAG.get(), flagDeco);
        event.register(ModItems.SUPREME_FLIGHT_BAN_FLAG.get(), flagDeco);
        event.register(ModItems.IMMORTAL_FLIGHT_BAN_FLAG.get(), flagDeco);
        event.register(ModItems.LOW_MAZE_FLAG.get(), flagDeco);
        event.register(ModItems.MID_MAZE_FLAG.get(), flagDeco);
        event.register(ModItems.HIGH_MAZE_FLAG.get(), flagDeco);
        event.register(ModItems.SUPREME_MAZE_FLAG.get(), flagDeco);
        event.register(ModItems.IMMORTAL_MAZE_FLAG.get(), flagDeco);
        event.register(ModItems.LOW_FARM_HARVEST_FLAG.get(), flagDeco);
        event.register(ModItems.MID_FARM_HARVEST_FLAG.get(), flagDeco);
        event.register(ModItems.HIGH_FARM_HARVEST_FLAG.get(), flagDeco);
        event.register(ModItems.SUPREME_FARM_HARVEST_FLAG.get(), flagDeco);
        event.register(ModItems.IMMORTAL_FARM_HARVEST_FLAG.get(), flagDeco);
    }

    @SubscribeEvent
    public static void onRegisterOverlays(RegisterGuiOverlaysEvent event) {
        event.registerAboveAll("cultivation_hud", CultivationHud.OVERLAY);
        event.registerAboveAll("qi_shield_hit_indicator", QiShieldHudOverlay.OVERLAY);
        event.registerAboveAll("star_fall_progress", StarFallProgressHud.OVERLAY);
        event.registerAboveAll("soul_hook_progress", SoulHookProgressHud.OVERLAY);
        event.registerAboveAll("time_stasis", TimeStasisClientEffects.OVERLAY);
        event.registerAboveAll("buddha_fire_lotus", BuddhaFireLotusClientEffects.OVERLAY);
        event.registerAboveAll("void_escape_stability", VoidEscapeClientEffects.STABILITY_OVERLAY);
        event.registerAboveAll("divine_sense_countdown", DivineSenseClientEffects.COUNTDOWN_OVERLAY);
        event.registerAboveAll("dharma_body_manifestation", DharmaBodyClientEffects.OVERLAY);
        event.registerAboveAll("origin_random_start", OriginRandomStartAnimation.OVERLAY);
        event.registerAboveAll("death_sequence", DeathSequenceClientEffects.OVERLAY);
    }
}
