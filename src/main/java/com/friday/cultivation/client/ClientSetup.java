/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.gui.screens.MenuScreens
 *  net.minecraft.client.renderer.ItemBlockRenderTypes
 *  net.minecraft.client.renderer.RenderType
 *  net.minecraft.client.renderer.entity.ThrownItemRenderer
 *  net.minecraft.core.particles.ParticleType
 *  net.minecraft.world.entity.EntityType
 *  net.minecraft.world.inventory.MenuType
 *  net.minecraft.world.level.ItemLike
 *  net.minecraft.world.level.block.Block
 *  net.minecraftforge.api.distmarker.Dist
 *  net.minecraftforge.client.IItemDecorator
 *  net.minecraftforge.client.event.EntityRenderersEvent$RegisterRenderers
 *  net.minecraftforge.client.event.RegisterGuiOverlaysEvent
 *  net.minecraftforge.client.event.RegisterItemDecorationsEvent
 *  net.minecraftforge.client.event.RegisterKeyMappingsEvent
 *  net.minecraftforge.client.event.RegisterParticleProvidersEvent
 *  net.minecraftforge.eventbus.api.SubscribeEvent
 *  net.minecraftforge.fml.common.Mod$EventBusSubscriber
 *  net.minecraftforge.fml.common.Mod$EventBusSubscriber$Bus
 *  net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent
 */
package com.friday.cultivation.client;

import com.friday.cultivation.client.BuddhaFireLotusClientEffects;
import com.friday.cultivation.client.ClientKeybindings;
import com.friday.cultivation.client.CultivationHud;
import com.friday.cultivation.client.DeathSequenceClientEffects;
import com.friday.cultivation.client.DharmaBodyClientEffects;
import com.friday.cultivation.client.DivineSenseClientEffects;
import com.friday.cultivation.client.FlagGlowDecorator;
import com.friday.cultivation.client.OriginRandomStartAnimation;
import com.friday.cultivation.client.PillGlowDecorator;
import com.friday.cultivation.client.QiShieldHudOverlay;
import com.friday.cultivation.client.SoulHookProgressHud;
import com.friday.cultivation.client.StarFallProgressHud;
import com.friday.cultivation.client.TimeStasisClientEffects;
import com.friday.cultivation.client.VoidEscapeClientEffects;
import com.friday.cultivation.client.WeaponGlowDecorator;
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
import com.friday.cultivation.registry.ModBlocks;
import com.friday.cultivation.registry.ModEntities;
import com.friday.cultivation.registry.ModItems;
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

@Mod.EventBusSubscriber(modid="friday_cultivation", bus=Mod.EventBusSubscriber.Bus.MOD, value={Dist.CLIENT})
public final class ClientSetup {
    private ClientSetup() {
    }

    @SubscribeEvent
    public static void onRegisterRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer((EntityType)ModEntities.QI_ORB.get(), QiOrbRenderer::new);
        event.registerEntityRenderer((EntityType)ModEntities.SEAT.get(), NoopEntityRenderer::new);
        event.registerEntityRenderer((EntityType)ModEntities.XIAOXIANG_FIREBALL.get(), ctx -> new ThrownItemRenderer(ctx, 3.0f, true));
        event.registerEntityRenderer((EntityType)ModEntities.GREAT_FIREBALL.get(), GreatFireballRenderer::new);
        event.registerEntityRenderer((EntityType)ModEntities.SWORD_PROJECTILE.get(), SwordProjectileRenderer::new);
        event.registerEntityRenderer((EntityType)ModEntities.STONE_BULLET.get(), StoneBulletRenderer::new);
        event.registerEntityRenderer((EntityType)ModEntities.HEAVEN_PIERCING_CONE.get(), HeavenPiercingConeRenderer::new);
        event.registerEntityRenderer((EntityType)ModEntities.BUDDHA_FIRE_LOTUS.get(), BuddhaFireLotusRenderer::new);
        event.registerEntityRenderer((EntityType)ModEntities.PALM_THUNDER_ORB.get(), PalmThunderOrbRenderer::new);
        event.registerEntityRenderer((EntityType)ModEntities.METEOR.get(), MeteorRenderer::new);
        event.registerEntityRenderer((EntityType)ModEntities.MUSHROOM_CLOUD.get(), NoopEntityRenderer::new);
        event.registerEntityRenderer((EntityType)ModEntities.SHOCKWAVE.get(), ShockwaveRenderer::new);
        event.registerEntityRenderer((EntityType)ModEntities.SWORD_AURA.get(), SwordAuraRenderer::new);
        event.registerEntityRenderer((EntityType)ModEntities.ICE_SHELL.get(), IceShellRenderer::new);
        event.registerEntityRenderer((EntityType)ModEntities.SKY_SPLITTING_SWORD_AURA.get(), SkySplittingSwordAuraRenderer::new);
        event.registerEntityRenderer((EntityType)ModEntities.SKY_TRAIL.get(), SkyTrailRenderer::new);
        event.registerEntityRenderer((EntityType)ModEntities.WANDERING_CULTIVATOR.get(), WanderingCultivatorRenderer::new);
        event.registerEntityRenderer((EntityType)ModEntities.SOUL_REAPER.get(), SoulReaperRenderer::new);
        event.registerEntityRenderer((EntityType)ModEntities.CORPSE.get(), CorpseRenderer::new);
    }

    @SubscribeEvent
    public static void onRegisterKeys(RegisterKeyMappingsEvent event) {
        ClientKeybindings.onRegisterKeys(event);
    }

    @SubscribeEvent
    public static void onRegisterParticleProviders(RegisterParticleProvidersEvent event) {
        event.registerSpriteSet((ParticleType)ModParticles.AMBIENT_QI.get(), set -> new AmbientQiParticle.Provider(set, 1.0f, 0.95f, 0.65f));
        event.registerSpriteSet((ParticleType)ModParticles.AMBIENT_QI_METAL.get(), set -> new AmbientQiParticle.Provider(set, 0.85f, 0.85f, 0.95f));
        event.registerSpriteSet((ParticleType)ModParticles.AMBIENT_QI_WOOD.get(), set -> new AmbientQiParticle.Provider(set, 0.4f, 0.85f, 0.4f));
        event.registerSpriteSet((ParticleType)ModParticles.AMBIENT_QI_WATER.get(), set -> new AmbientQiParticle.Provider(set, 0.4f, 0.7f, 1.0f));
        event.registerSpriteSet((ParticleType)ModParticles.AMBIENT_QI_FIRE.get(), set -> new AmbientQiParticle.Provider(set, 1.0f, 0.4f, 0.3f));
        event.registerSpriteSet((ParticleType)ModParticles.AMBIENT_QI_EARTH.get(), set -> new AmbientQiParticle.Provider(set, 0.85f, 0.7f, 0.4f));
        event.registerSpriteSet((ParticleType)ModParticles.AMBIENT_QI_ICE.get(), set -> new AmbientQiParticle.Provider(set, 0.72f, 0.88f, 1.0f));
        event.registerSpriteSet((ParticleType)ModParticles.AMBIENT_QI_LIGHTNING.get(), set -> new AmbientQiParticle.Provider(set, 0.88f, 0.9f, 1.0f));
        event.registerSpriteSet((ParticleType)ModParticles.AMBIENT_QI_LOTUS.get(), set -> new AmbientQiParticle.Provider(set, 0.95f, 0.2f, 0.95f));
        event.registerSpriteSet((ParticleType)ModParticles.YIN_QI.get(), set -> new AmbientQiParticle.Provider(set, 0.55f, 0.5f, 0.8f));
        event.registerSpriteSet((ParticleType)ModParticles.BREAKTHROUGH.get(), BreakthroughParticle.Provider::new);
        event.registerSpriteSet((ParticleType)ModParticles.QI_ABSORB.get(), QiAbsorbParticle.Provider::new);
    }

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            MenuScreens.register((MenuType)((MenuType)ModMenuTypes.WANDERING_CULTIVATOR.get()), WanderingCultivatorScreen::new);
            MenuScreens.register((MenuType)((MenuType)ModMenuTypes.ALCHEMY.get()), AlchemyScreen::new);
            MenuScreens.register((MenuType)((MenuType)ModMenuTypes.REFINING.get()), RefiningScreen::new);
            MenuScreens.register((MenuType)((MenuType)ModMenuTypes.FORMATION.get()), FormationScreen::new);
            ItemBlockRenderTypes.setRenderLayer((Block)((Block)ModBlocks.HERB.get()), (RenderType)RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer((Block)((Block)ModBlocks.FORMATION_RUNE.get()), (RenderType)RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer((Block)((Block)ModBlocks.BONE_REMAINS.get()), (RenderType)RenderType.cutout());
        });
    }

    @SubscribeEvent
    public static void onRegisterItemDecorations(RegisterItemDecorationsEvent event) {
        PillGlowDecorator decorator = new PillGlowDecorator();
        event.register((ItemLike)ModItems.PILL_QI_RECOVERY_LOW.get(), (IItemDecorator)decorator);
        event.register((ItemLike)ModItems.PILL_QI_RECOVERY_MID.get(), (IItemDecorator)decorator);
        event.register((ItemLike)ModItems.PILL_QI_RECOVERY_HIGH.get(), (IItemDecorator)decorator);
        event.register((ItemLike)ModItems.PILL_QI_RECOVERY_SUPREME.get(), (IItemDecorator)decorator);
        event.register((ItemLike)ModItems.PILL_QI_RECOVERY_IMMORTAL.get(), (IItemDecorator)decorator);
        event.register((ItemLike)ModItems.PILL_CULTIVATION_LOW.get(), (IItemDecorator)decorator);
        event.register((ItemLike)ModItems.PILL_CULTIVATION_MID.get(), (IItemDecorator)decorator);
        event.register((ItemLike)ModItems.PILL_CULTIVATION_HIGH.get(), (IItemDecorator)decorator);
        event.register((ItemLike)ModItems.PILL_CULTIVATION_SUPREME.get(), (IItemDecorator)decorator);
        event.register((ItemLike)ModItems.PILL_CULTIVATION_IMMORTAL.get(), (IItemDecorator)decorator);
        event.register((ItemLike)ModItems.PILL_BLOOD_BURN_LOW.get(), (IItemDecorator)decorator);
        event.register((ItemLike)ModItems.PILL_BLOOD_BURN_MID.get(), (IItemDecorator)decorator);
        event.register((ItemLike)ModItems.PILL_BLOOD_BURN_HIGH.get(), (IItemDecorator)decorator);
        event.register((ItemLike)ModItems.PILL_BLOOD_BURN_SUPREME.get(), (IItemDecorator)decorator);
        event.register((ItemLike)ModItems.PILL_BLOOD_BURN_IMMORTAL.get(), (IItemDecorator)decorator);
        event.register((ItemLike)ModItems.PILL_CLEAR_MIND_LOW.get(), (IItemDecorator)decorator);
        event.register((ItemLike)ModItems.PILL_CLEAR_MIND_MID.get(), (IItemDecorator)decorator);
        event.register((ItemLike)ModItems.PILL_CLEAR_MIND_HIGH.get(), (IItemDecorator)decorator);
        event.register((ItemLike)ModItems.PILL_CLEAR_MIND_SUPREME.get(), (IItemDecorator)decorator);
        event.register((ItemLike)ModItems.PILL_CLEAR_MIND_IMMORTAL.get(), (IItemDecorator)decorator);
        event.register((ItemLike)ModItems.PILL_REJUVENATION_LOW.get(), (IItemDecorator)decorator);
        event.register((ItemLike)ModItems.PILL_REJUVENATION_MID.get(), (IItemDecorator)decorator);
        event.register((ItemLike)ModItems.PILL_REJUVENATION_HIGH.get(), (IItemDecorator)decorator);
        event.register((ItemLike)ModItems.PILL_REJUVENATION_SUPREME.get(), (IItemDecorator)decorator);
        event.register((ItemLike)ModItems.PILL_REJUVENATION_IMMORTAL.get(), (IItemDecorator)decorator);
        event.register((ItemLike)ModItems.PILL_DIVINE_STRIDE_LOW.get(), (IItemDecorator)decorator);
        event.register((ItemLike)ModItems.PILL_DIVINE_STRIDE_MID.get(), (IItemDecorator)decorator);
        event.register((ItemLike)ModItems.PILL_DIVINE_STRIDE_HIGH.get(), (IItemDecorator)decorator);
        event.register((ItemLike)ModItems.PILL_DIVINE_STRIDE_SUPREME.get(), (IItemDecorator)decorator);
        event.register((ItemLike)ModItems.PILL_DIVINE_STRIDE_IMMORTAL.get(), (IItemDecorator)decorator);
        WeaponGlowDecorator weaponDeco = new WeaponGlowDecorator();
        event.register((ItemLike)ModItems.XUAN_IRON_SWORD_LOW.get(), (IItemDecorator)weaponDeco);
        event.register((ItemLike)ModItems.XUAN_IRON_SWORD_MID.get(), (IItemDecorator)weaponDeco);
        event.register((ItemLike)ModItems.XUAN_IRON_SWORD_HIGH.get(), (IItemDecorator)weaponDeco);
        event.register((ItemLike)ModItems.XUAN_IRON_SWORD_SUPREME.get(), (IItemDecorator)weaponDeco);
        event.register((ItemLike)ModItems.XUAN_IRON_SWORD_IMMORTAL.get(), (IItemDecorator)weaponDeco);
        event.register((ItemLike)ModItems.QING_MU_SWORD_LOW.get(), (IItemDecorator)weaponDeco);
        event.register((ItemLike)ModItems.QING_MU_SWORD_MID.get(), (IItemDecorator)weaponDeco);
        event.register((ItemLike)ModItems.QING_MU_SWORD_HIGH.get(), (IItemDecorator)weaponDeco);
        event.register((ItemLike)ModItems.QING_MU_SWORD_SUPREME.get(), (IItemDecorator)weaponDeco);
        event.register((ItemLike)ModItems.QING_MU_SWORD_IMMORTAL.get(), (IItemDecorator)weaponDeco);
        event.register((ItemLike)ModItems.CHI_YAN_SWORD_LOW.get(), (IItemDecorator)weaponDeco);
        event.register((ItemLike)ModItems.CHI_YAN_SWORD_MID.get(), (IItemDecorator)weaponDeco);
        event.register((ItemLike)ModItems.CHI_YAN_SWORD_HIGH.get(), (IItemDecorator)weaponDeco);
        event.register((ItemLike)ModItems.CHI_YAN_SWORD_SUPREME.get(), (IItemDecorator)weaponDeco);
        event.register((ItemLike)ModItems.CHI_YAN_SWORD_IMMORTAL.get(), (IItemDecorator)weaponDeco);
        event.register((ItemLike)ModItems.HAN_BING_SWORD_LOW.get(), (IItemDecorator)weaponDeco);
        event.register((ItemLike)ModItems.HAN_BING_SWORD_MID.get(), (IItemDecorator)weaponDeco);
        event.register((ItemLike)ModItems.HAN_BING_SWORD_HIGH.get(), (IItemDecorator)weaponDeco);
        event.register((ItemLike)ModItems.HAN_BING_SWORD_SUPREME.get(), (IItemDecorator)weaponDeco);
        event.register((ItemLike)ModItems.HAN_BING_SWORD_IMMORTAL.get(), (IItemDecorator)weaponDeco);
        FlagGlowDecorator flagDeco = new FlagGlowDecorator();
        event.register((ItemLike)ModItems.LOW_QI_GATHERING_FLAG.get(), (IItemDecorator)flagDeco);
        event.register((ItemLike)ModItems.MID_QI_GATHERING_FLAG.get(), (IItemDecorator)flagDeco);
        event.register((ItemLike)ModItems.HIGH_QI_GATHERING_FLAG.get(), (IItemDecorator)flagDeco);
        event.register((ItemLike)ModItems.SUPREME_QI_GATHERING_FLAG.get(), (IItemDecorator)flagDeco);
        event.register((ItemLike)ModItems.IMMORTAL_QI_GATHERING_FLAG.get(), (IItemDecorator)flagDeco);
        event.register((ItemLike)ModItems.LOW_SECT_PROTECTION_FLAG.get(), (IItemDecorator)flagDeco);
        event.register((ItemLike)ModItems.MID_SECT_PROTECTION_FLAG.get(), (IItemDecorator)flagDeco);
        event.register((ItemLike)ModItems.HIGH_SECT_PROTECTION_FLAG.get(), (IItemDecorator)flagDeco);
        event.register((ItemLike)ModItems.SUPREME_SECT_PROTECTION_FLAG.get(), (IItemDecorator)flagDeco);
        event.register((ItemLike)ModItems.IMMORTAL_SECT_PROTECTION_FLAG.get(), (IItemDecorator)flagDeco);
        event.register((ItemLike)ModItems.LOW_WITHER_GROWTH_FLAG.get(), (IItemDecorator)flagDeco);
        event.register((ItemLike)ModItems.MID_WITHER_GROWTH_FLAG.get(), (IItemDecorator)flagDeco);
        event.register((ItemLike)ModItems.HIGH_WITHER_GROWTH_FLAG.get(), (IItemDecorator)flagDeco);
        event.register((ItemLike)ModItems.SUPREME_WITHER_GROWTH_FLAG.get(), (IItemDecorator)flagDeco);
        event.register((ItemLike)ModItems.IMMORTAL_WITHER_GROWTH_FLAG.get(), (IItemDecorator)flagDeco);
        event.register((ItemLike)ModItems.LOW_REJUVENATION_FLAG.get(), (IItemDecorator)flagDeco);
        event.register((ItemLike)ModItems.MID_REJUVENATION_FLAG.get(), (IItemDecorator)flagDeco);
        event.register((ItemLike)ModItems.HIGH_REJUVENATION_FLAG.get(), (IItemDecorator)flagDeco);
        event.register((ItemLike)ModItems.SUPREME_REJUVENATION_FLAG.get(), (IItemDecorator)flagDeco);
        event.register((ItemLike)ModItems.IMMORTAL_REJUVENATION_FLAG.get(), (IItemDecorator)flagDeco);
        event.register((ItemLike)ModItems.LOW_FLIGHT_BAN_FLAG.get(), (IItemDecorator)flagDeco);
        event.register((ItemLike)ModItems.MID_FLIGHT_BAN_FLAG.get(), (IItemDecorator)flagDeco);
        event.register((ItemLike)ModItems.HIGH_FLIGHT_BAN_FLAG.get(), (IItemDecorator)flagDeco);
        event.register((ItemLike)ModItems.SUPREME_FLIGHT_BAN_FLAG.get(), (IItemDecorator)flagDeco);
        event.register((ItemLike)ModItems.IMMORTAL_FLIGHT_BAN_FLAG.get(), (IItemDecorator)flagDeco);
        event.register((ItemLike)ModItems.LOW_MAZE_FLAG.get(), (IItemDecorator)flagDeco);
        event.register((ItemLike)ModItems.MID_MAZE_FLAG.get(), (IItemDecorator)flagDeco);
        event.register((ItemLike)ModItems.HIGH_MAZE_FLAG.get(), (IItemDecorator)flagDeco);
        event.register((ItemLike)ModItems.SUPREME_MAZE_FLAG.get(), (IItemDecorator)flagDeco);
        event.register((ItemLike)ModItems.IMMORTAL_MAZE_FLAG.get(), (IItemDecorator)flagDeco);
        event.register((ItemLike)ModItems.LOW_FARM_HARVEST_FLAG.get(), (IItemDecorator)flagDeco);
        event.register((ItemLike)ModItems.MID_FARM_HARVEST_FLAG.get(), (IItemDecorator)flagDeco);
        event.register((ItemLike)ModItems.HIGH_FARM_HARVEST_FLAG.get(), (IItemDecorator)flagDeco);
        event.register((ItemLike)ModItems.SUPREME_FARM_HARVEST_FLAG.get(), (IItemDecorator)flagDeco);
        event.register((ItemLike)ModItems.IMMORTAL_FARM_HARVEST_FLAG.get(), (IItemDecorator)flagDeco);
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

