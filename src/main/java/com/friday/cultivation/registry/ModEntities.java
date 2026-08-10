/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.world.entity.EntityType
 *  net.minecraft.world.entity.EntityType$Builder
 *  net.minecraft.world.entity.MobCategory
 *  net.minecraftforge.eventbus.api.IEventBus
 *  net.minecraftforge.registries.DeferredRegister
 *  net.minecraftforge.registries.ForgeRegistries
 *  net.minecraftforge.registries.IForgeRegistry
 *  net.minecraftforge.registries.RegistryObject
 */
package com.friday.cultivation.registry;

import com.friday.cultivation.entity.BuddhaFireLotusEntity;
import com.friday.cultivation.entity.GreatFireballEntity;
import com.friday.cultivation.entity.HeavenPiercingConeEntity;
import com.friday.cultivation.entity.IceShellEntity;
import com.friday.cultivation.entity.MeteorEntity;
import com.friday.cultivation.entity.MushroomCloudEntity;
import com.friday.cultivation.entity.PalmThunderOrbEntity;
import com.friday.cultivation.entity.QiOrbEntity;
import com.friday.cultivation.entity.SeatEntity;
import com.friday.cultivation.entity.ShockwaveEntity;
import com.friday.cultivation.entity.SkySplittingSwordAuraEntity;
import com.friday.cultivation.entity.SkyTrailEntity;
import com.friday.cultivation.entity.StoneBulletEntity;
import com.friday.cultivation.entity.SwordAuraEntity;
import com.friday.cultivation.entity.SwordProjectileEntity;
import com.friday.cultivation.entity.XiaoxiangFireballEntity;
import com.friday.cultivation.entity.npc.CorpseEntity;
import com.friday.cultivation.entity.npc.SoulReaperEntity;
import com.friday.cultivation.entity.npc.WanderingCultivatorEntity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.RegistryObject;

public final class ModEntities {
    public static final DeferredRegister<EntityType<?>> ENTITIES = DeferredRegister.create((IForgeRegistry)ForgeRegistries.ENTITY_TYPES, (String)"friday_cultivation");
    public static final RegistryObject<EntityType<QiOrbEntity>> QI_ORB = ENTITIES.register("qi_orb", () -> EntityType.Builder.<QiOrbEntity>of(QiOrbEntity::new, (MobCategory)MobCategory.MISC).sized(0.25f, 0.25f).clientTrackingRange(16).updateInterval(2).build("qi_orb"));
    public static final RegistryObject<EntityType<XiaoxiangFireballEntity>> XIAOXIANG_FIREBALL = ENTITIES.register("xiaoxiang_fireball", () -> EntityType.Builder.<XiaoxiangFireballEntity>of(XiaoxiangFireballEntity::new, (MobCategory)MobCategory.MISC).sized(1.0f, 1.0f).clientTrackingRange(64).updateInterval(10).build("xiaoxiang_fireball"));
    public static final RegistryObject<EntityType<SwordProjectileEntity>> SWORD_PROJECTILE = ENTITIES.register("sword_projectile", () -> EntityType.Builder.<SwordProjectileEntity>of(SwordProjectileEntity::new, (MobCategory)MobCategory.MISC).sized(0.5f, 0.5f).clientTrackingRange(96).updateInterval(1).build("sword_projectile"));
    public static final RegistryObject<EntityType<StoneBulletEntity>> STONE_BULLET = ENTITIES.register("stone_bullet", () -> EntityType.Builder.<StoneBulletEntity>of(StoneBulletEntity::new, (MobCategory)MobCategory.MISC).sized(0.35f, 0.35f).clientTrackingRange(96).updateInterval(1).build("stone_bullet"));
    public static final RegistryObject<EntityType<HeavenPiercingConeEntity>> HEAVEN_PIERCING_CONE = ENTITIES.register("heaven_piercing_cone", () -> EntityType.Builder.<HeavenPiercingConeEntity>of(HeavenPiercingConeEntity::new, (MobCategory)MobCategory.MISC).sized(0.7f, 0.7f).clientTrackingRange(160).updateInterval(1).fireImmune().build("heaven_piercing_cone"));
    public static final RegistryObject<EntityType<BuddhaFireLotusEntity>> BUDDHA_FIRE_LOTUS = ENTITIES.register("buddha_fire_lotus", () -> EntityType.Builder.<BuddhaFireLotusEntity>of(BuddhaFireLotusEntity::new, (MobCategory)MobCategory.MISC).sized(0.8f, 0.8f).clientTrackingRange(192).updateInterval(1).fireImmune().build("buddha_fire_lotus"));
    public static final RegistryObject<EntityType<PalmThunderOrbEntity>> PALM_THUNDER_ORB = ENTITIES.register("palm_thunder_orb", () -> EntityType.Builder.<PalmThunderOrbEntity>of(PalmThunderOrbEntity::new, (MobCategory)MobCategory.MISC).sized(0.55f, 0.55f).clientTrackingRange(96).updateInterval(1).fireImmune().build("palm_thunder_orb"));
    public static final RegistryObject<EntityType<GreatFireballEntity>> GREAT_FIREBALL = ENTITIES.register("great_fireball", () -> EntityType.Builder.<GreatFireballEntity>of(GreatFireballEntity::new, (MobCategory)MobCategory.MISC).sized(1.0f, 1.0f).clientTrackingRange(96).updateInterval(1).build("great_fireball"));
    public static final RegistryObject<EntityType<MeteorEntity>> METEOR = ENTITIES.register("meteor", () -> EntityType.Builder.<MeteorEntity>of(MeteorEntity::new, (MobCategory)MobCategory.MISC).sized(2.0f, 2.0f).clientTrackingRange(160).updateInterval(1).build("meteor"));
    public static final RegistryObject<EntityType<MushroomCloudEntity>> MUSHROOM_CLOUD = ENTITIES.register("mushroom_cloud", () -> EntityType.Builder.<MushroomCloudEntity>of(MushroomCloudEntity::new, (MobCategory)MobCategory.MISC).sized(1.0f, 1.0f).clientTrackingRange(160).updateInterval(20).fireImmune().build("mushroom_cloud"));
    public static final RegistryObject<EntityType<SwordAuraEntity>> SWORD_AURA = ENTITIES.register("sword_aura", () -> EntityType.Builder.<SwordAuraEntity>of(SwordAuraEntity::new, (MobCategory)MobCategory.MISC).sized(0.6f, 0.6f).clientTrackingRange(64).updateInterval(1).build("sword_aura"));
    public static final RegistryObject<EntityType<IceShellEntity>> ICE_SHELL = ENTITIES.register("ice_shell", () -> EntityType.Builder.<IceShellEntity>of(IceShellEntity::new, (MobCategory)MobCategory.MISC).sized(1.5f, 2.5f).clientTrackingRange(64).updateInterval(1).fireImmune().build("ice_shell"));
    public static final RegistryObject<EntityType<SkySplittingSwordAuraEntity>> SKY_SPLITTING_SWORD_AURA = ENTITIES.register("sky_splitting_sword_aura", () -> EntityType.Builder.<SkySplittingSwordAuraEntity>of(SkySplittingSwordAuraEntity::new, (MobCategory)MobCategory.MISC).sized(2.0f, 2.0f).clientTrackingRange(160).updateInterval(1).fireImmune().build("sky_splitting_sword_aura"));
    public static final RegistryObject<EntityType<SkyTrailEntity>> SKY_TRAIL = ENTITIES.register("sky_trail", () -> EntityType.Builder.<SkyTrailEntity>of(SkyTrailEntity::new, (MobCategory)MobCategory.MISC).sized(1.0f, 1.0f).clientTrackingRange(256).updateInterval(20).fireImmune().build("sky_trail"));
    public static final RegistryObject<EntityType<ShockwaveEntity>> SHOCKWAVE = ENTITIES.register("shockwave", () -> EntityType.Builder.<ShockwaveEntity>of(ShockwaveEntity::new, (MobCategory)MobCategory.MISC).sized(2.0f, 2.0f).clientTrackingRange(160).updateInterval(1).fireImmune().build("shockwave"));
    public static final RegistryObject<EntityType<WanderingCultivatorEntity>> WANDERING_CULTIVATOR = ENTITIES.register("wandering_cultivator", () -> EntityType.Builder.<WanderingCultivatorEntity>of(WanderingCultivatorEntity::new, (MobCategory)MobCategory.CREATURE).sized(0.6f, 1.95f).clientTrackingRange(10).build("wandering_cultivator"));
    public static final RegistryObject<EntityType<SoulReaperEntity>> SOUL_REAPER = ENTITIES.register("soul_reaper", () -> EntityType.Builder.<SoulReaperEntity>of(SoulReaperEntity::new, (MobCategory)MobCategory.MISC).sized(0.6f, 1.95f).clientTrackingRange(64).updateInterval(2).fireImmune().build("soul_reaper"));
    public static final RegistryObject<EntityType<CorpseEntity>> CORPSE = ENTITIES.register("corpse", () -> EntityType.Builder.<CorpseEntity>of(CorpseEntity::new, (MobCategory)MobCategory.MISC).sized(1.8f, 0.5f).clientTrackingRange(48).updateInterval(20).noSave().fireImmune().build("corpse"));
    public static final RegistryObject<EntityType<SeatEntity>> SEAT = ENTITIES.register("seat", () -> EntityType.Builder.<SeatEntity>of(SeatEntity::new, (MobCategory)MobCategory.MISC).sized(0.001f, 0.001f).clientTrackingRange(8).updateInterval(20).noSave().canSpawnFarFromPlayer().build("seat"));

    private ModEntities() {
    }

    public static void register(IEventBus bus) {
        ENTITIES.register(bus);
    }
}

