package com.friday.cultivation.registry;

import com.friday.cultivation.entity.*;
import com.friday.cultivation.entity.npc.CorpseEntity;
import com.friday.cultivation.entity.npc.SoulReaperEntity;
import com.friday.cultivation.entity.npc.WanderingCultivatorEntity;
import com.friday.cultivation.entity.spell.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

/**
 * 修仙模组实体注册 — 复刻原模组 ModEntities
 * 注册法术实体（火球/剑气/陨石/穿天锥/掌心雷等）
 */
public class ModEntities {
    public static final DeferredRegister<EntityType<?>> ENTITIES =
            DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, "friday_cultivation");

    // 火球术实体
    public static final RegistryObject<EntityType<XiaoxiangFireballEntity>> XIAOXIANG_FIREBALL =
            ENTITIES.register("xiaoxiang_fireball",
                    () -> EntityType.Builder.<XiaoxiangFireballEntity>of(
                            (type, level) -> new XiaoxiangFireballEntity(type, level), MobCategory.MISC)
                            .sized(0.5f, 0.5f).clientTrackingRange(4).updateInterval(10)
                            .build("friday_cultivation:xiaoxiang_fireball"));

    // 大火球术实体
    public static final RegistryObject<EntityType<GreatFireballEntity>> GREAT_FIREBALL =
            ENTITIES.register("great_fireball",
                    () -> EntityType.Builder.<GreatFireballEntity>of(
                            (type, level) -> new GreatFireballEntity(type, level), MobCategory.MISC)
                            .sized(0.5f, 0.5f).clientTrackingRange(4).updateInterval(10)
                            .build("friday_cultivation:great_fireball"));

    // 裂天剑气实体
    public static final RegistryObject<EntityType<SkySplittingSwordAuraEntity>> SKY_SPLITTING_SWORD_AURA =
            ENTITIES.register("sky_splitting_sword_aura",
                    () -> EntityType.Builder.<SkySplittingSwordAuraEntity>of(
                            (type, level) -> new SkySplittingSwordAuraEntity(type, level), MobCategory.MISC)
                            .sized(1.0f, 1.0f).clientTrackingRange(4).updateInterval(10)
                            .build("friday_cultivation:sky_splitting_sword_aura"));

    // 灵气球（投射物）
    public static final RegistryObject<EntityType<QiOrbEntity>> QI_ORB =
            ENTITIES.register("qi_orb",
                    () -> EntityType.Builder.<QiOrbEntity>of(
                            (type, level) -> new QiOrbEntity(type, level), MobCategory.MISC)
                            .sized(0.4f, 0.4f).clientTrackingRange(6).updateInterval(4)
                            .build("friday_cultivation:qi_orb"));

    // 蘑菇云视觉实体
    public static final RegistryObject<EntityType<MushroomCloudEntity>> MUSHROOM_CLOUD =
            ENTITIES.register("mushroom_cloud",
                    () -> EntityType.Builder.<MushroomCloudEntity>of(
                            (type, level) -> new MushroomCloudEntity(type, level), MobCategory.MISC)
                            .sized(1.0f, 1.0f).clientTrackingRange(8).noSave().fireImmune().updateInterval(2)
                            .build("friday_cultivation:mushroom_cloud"));

    // 剑气实体
    public static final RegistryObject<EntityType<SwordAuraEntity>> SWORD_AURA =
            ENTITIES.register("sword_aura",
                    () -> EntityType.Builder.<SwordAuraEntity>of(
                            (type, level) -> new SwordAuraEntity(type, level), MobCategory.MISC)
                            .sized(0.6f, 0.6f).clientTrackingRange(8).updateInterval(2)
                            .build("friday_cultivation:sword_aura"));

    // 飞剑术实体
    public static final RegistryObject<EntityType<SwordProjectileEntity>> SWORD_PROJECTILE =
            ENTITIES.register("sword_projectile",
                    () -> EntityType.Builder.<SwordProjectileEntity>of(
                            (type, level) -> new SwordProjectileEntity(type, level), MobCategory.MISC)
                            .sized(0.5f, 0.5f).clientTrackingRange(4).updateInterval(10)
                            .build("friday_cultivation:sword_projectile"));

    // 天星坠实体
    public static final RegistryObject<EntityType<MeteorEntity>> METEOR =
            ENTITIES.register("meteor",
                    () -> EntityType.Builder.<MeteorEntity>of(
                            (type, level) -> new MeteorEntity(type, level), MobCategory.MISC)
                            .sized(1.5f, 1.5f).clientTrackingRange(4).updateInterval(10)
                            .build("friday_cultivation:meteor"));

    // 石弹术实体
    public static final RegistryObject<EntityType<StoneBulletEntity>> STONE_BULLET =
            ENTITIES.register("stone_bullet",
                    () -> EntityType.Builder.<StoneBulletEntity>of(
                            (type, level) -> new StoneBulletEntity(type, level), MobCategory.MISC)
                            .sized(0.5f, 0.5f).clientTrackingRange(4).updateInterval(10)
                            .build("friday_cultivation:stone_bullet"));

    // 穿天锥实体
    public static final RegistryObject<EntityType<HeavenPiercingConeEntity>> HEAVEN_PIERCING_CONE =
            ENTITIES.register("heaven_piercing_cone",
                    () -> EntityType.Builder.<HeavenPiercingConeEntity>of(
                            (type, level) -> new HeavenPiercingConeEntity(type, level), MobCategory.MISC)
                            .sized(0.5f, 0.5f).clientTrackingRange(4).updateInterval(10)
                            .build("friday_cultivation:heaven_piercing_cone"));

    // 掌心雷实体
    public static final RegistryObject<EntityType<PalmThunderOrbEntity>> PALM_THUNDER_ORB =
            ENTITIES.register("palm_thunder_orb",
                    () -> EntityType.Builder.<PalmThunderOrbEntity>of(
                            (type, level) -> new PalmThunderOrbEntity(type, level), MobCategory.MISC)
                            .sized(0.3f, 0.3f).clientTrackingRange(4).updateInterval(10)
                            .build("friday_cultivation:palm_thunder_orb"));

    // 佛怒火莲实体
    public static final RegistryObject<EntityType<BuddhaFireLotusEntity>> BUDDHA_FIRE_LOTUS =
            ENTITIES.register("buddha_fire_lotus",
                    () -> EntityType.Builder.<BuddhaFireLotusEntity>of(
                            (type, level) -> new BuddhaFireLotusEntity(type, level), MobCategory.MISC)
                            .sized(0.6f, 0.6f).clientTrackingRange(4).updateInterval(10)
                            .build("friday_cultivation:buddha_fire_lotus"));

    // 冲击波实体
    public static final RegistryObject<EntityType<ShockwaveEntity>> SHOCKWAVE =
            ENTITIES.register("shockwave",
                    () -> EntityType.Builder.<ShockwaveEntity>of(
                            (type, level) -> new ShockwaveEntity(type, level), MobCategory.MISC)
                            .sized(1.0f, 1.0f).clientTrackingRange(6).updateInterval(10)
                            .build("friday_cultivation:shockwave"));

    // 尸体实体
    public static final RegistryObject<EntityType<CorpseEntity>> CORPSE =
            ENTITIES.register("corpse",
                    () -> EntityType.Builder.<CorpseEntity>of(
                            (type, level) -> new CorpseEntity(type, level), MobCategory.MISC)
                            .sized(0.6f, 0.2f).clientTrackingRange(6).updateInterval(20)
                            .build("friday_cultivation:corpse"));

    // 牛头马面实体
    public static final RegistryObject<EntityType<SoulReaperEntity>> SOUL_REAPER =
            ENTITIES.register("soul_reaper",
                    () -> EntityType.Builder.<SoulReaperEntity>of(
                            SoulReaperEntity::new, MobCategory.MONSTER)
                            .sized(0.6f, 1.8f).clientTrackingRange(8).updateInterval(3)
                            .build("friday_cultivation:soul_reaper"));

    // 游历修士NPC实体
    public static final RegistryObject<EntityType<WanderingCultivatorEntity>> WANDERING_CULTIVATOR =
            ENTITIES.register("wandering_cultivator",
                    () -> EntityType.Builder.<WanderingCultivatorEntity>of(
                            WanderingCultivatorEntity::new, MobCategory.CREATURE)
                            .sized(0.6f, 1.8f).clientTrackingRange(8).updateInterval(3)
                            .build("friday_cultivation:wandering_cultivator"));

    // 天际轨迹实体
    public static final RegistryObject<EntityType<SkyTrailEntity>> SKY_TRAIL =
            ENTITIES.register("sky_trail",
                    () -> EntityType.Builder.<SkyTrailEntity>of(SkyTrailEntity::new, MobCategory.MISC)
                            .sized(0.5f, 0.5f).noSave().fireImmune().build("sky_trail"));

    // 冰壳禁锢实体
    public static final RegistryObject<EntityType<IceShellEntity>> ICE_SHELL =
            ENTITIES.register("ice_shell",
                    () -> EntityType.Builder.<IceShellEntity>of(IceShellEntity::new, MobCategory.MISC)
                            .sized(1.5f, 2.5f).noSave().fireImmune().build("ice_shell"));

    // 坐垫实体
    public static final RegistryObject<EntityType<SeatEntity>> SEAT =
            ENTITIES.register("seat",
                    () -> EntityType.Builder.<SeatEntity>of(SeatEntity::new, MobCategory.MISC)
                            .sized(0.001f, 0.001f).clientTrackingRange(8).updateInterval(20).noSummon().fireImmune()
                            .build("seat"));

    public static void register(IEventBus modEventBus) {
        ENTITIES.register(modEventBus);
    }
}
