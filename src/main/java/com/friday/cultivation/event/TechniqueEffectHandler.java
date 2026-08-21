/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.server.level.ServerPlayer
 *  net.minecraft.tags.BlockTags
 *  net.minecraft.world.damagesource.DamageSource
 *  net.minecraft.world.damagesource.DamageTypes
 *  net.minecraft.world.effect.MobEffect
 *  net.minecraft.world.effect.MobEffectInstance
 *  net.minecraft.world.effect.MobEffects
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.entity.MobType
 *  net.minecraft.world.entity.ai.attributes.Attribute
 *  net.minecraft.world.entity.ai.attributes.AttributeInstance
 *  net.minecraft.world.entity.ai.attributes.AttributeModifier
 *  net.minecraft.world.entity.ai.attributes.AttributeModifier$Operation
 *  net.minecraft.world.entity.ai.attributes.Attributes
 *  net.minecraft.world.entity.monster.Illusioner
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.entity.raid.Raider
 *  net.minecraft.world.level.block.Blocks
 *  net.minecraft.world.level.block.state.BlockState
 *  net.minecraft.world.phys.Vec3
 *  net.minecraftforge.event.TickEvent$Phase
 *  net.minecraftforge.event.TickEvent$PlayerTickEvent
 *  net.minecraftforge.event.entity.living.LivingFallEvent
 *  net.minecraftforge.event.entity.living.LivingHealEvent
 *  net.minecraftforge.event.entity.living.LivingHurtEvent
 *  net.minecraftforge.event.entity.living.LivingKnockBackEvent
 *  net.minecraftforge.event.entity.living.MobEffectEvent$Applicable
 *  net.minecraftforge.eventbus.api.Event$Result
 *  net.minecraftforge.eventbus.api.EventPriority
 *  net.minecraftforge.eventbus.api.SubscribeEvent
 *  net.minecraftforge.fml.common.Mod$EventBusSubscriber
 *  net.minecraftforge.network.PacketDistributor
 */
package com.friday.cultivation.event;

import com.friday.cultivation.cultivation.CultivationBonusCategory;
import com.friday.cultivation.cultivation.CultivationCapability;
import com.friday.cultivation.cultivation.CultivationData;
import com.friday.cultivation.cultivation.realm.Realm;
import com.friday.cultivation.cultivation.FoundationDaoBonusHelper;
import com.friday.cultivation.cultivation.GoldenCoreDaoBonusHelper;
import com.friday.cultivation.cultivation.LooseImmortalBonusHelper;
import com.friday.cultivation.cultivation.PhysiqueBonusHelper;
import com.friday.cultivation.cultivation.SpiritRoot;
import com.friday.cultivation.cultivation.SpiritRootBonusHelper;
import com.friday.cultivation.cultivation.ZhenyuanBonusHelper;
import com.friday.cultivation.cultivation.technique.Technique;
import com.friday.cultivation.cultivation.technique.TechniqueBonusHelper;
import com.friday.cultivation.event.SectCombatHandler;
import com.friday.cultivation.event.SoulStateHandler;
import com.friday.cultivation.event.TribulationHandler;
import com.friday.cultivation.network.ModNetwork;
import com.friday.cultivation.network.SyncImmortalDarkVisionPacket;
import java.lang.reflect.Field;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobType;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Illusioner;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.raid.Raider;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingFallEvent;
import net.minecraftforge.event.entity.living.LivingHealEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.living.LivingKnockBackEvent;
import net.minecraftforge.event.entity.living.MobEffectEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.PacketDistributor;

@Mod.EventBusSubscriber(modid="friday_cultivation")
public final class TechniqueEffectHandler {
    private static final int REGEN_TICK_INTERVAL = 1200;
    private static final int FOREST_SHELTER_REFRESH_INTERVAL = 10;
    private static final int FOREST_SHELTER_REGEN_DURATION = 30;
    private static final int FOREST_SHELTER_REGEN_AMP = 2;
    private static final String TAG_IMMORTAL_DARKVISION_SYNCED = "xiaoxiang_immortal_darkvision_synced";
    private static final int INFINITE_EFFECT_AMP = 0;
    private static final UUID UUID_MAX_HP = UUID.nameUUIDFromBytes("xiaoxiang.tech.maxHp".getBytes());
    private static final UUID UUID_MOVE_SPEED = UUID.nameUUIDFromBytes("xiaoxiang.tech.moveSpeed".getBytes());
    private static final UUID UUID_KB_RESIST = UUID.nameUUIDFromBytes("xiaoxiang.tech.kbResist".getBytes());
    private static final UUID UUID_SPIRIT_HP = UUID.nameUUIDFromBytes("xiaoxiang.spiritroot.hp".getBytes());
    private static final UUID UUID_ZHENYUAN_HP = UUID.nameUUIDFromBytes("xiaoxiang.zhenyuan.hp".getBytes());
    private static final UUID UUID_ZHENYUAN_ARMOR = UUID.nameUUIDFromBytes("xiaoxiang.zhenyuan.armor".getBytes());
    private static final UUID UUID_ZHENYUAN_TOUGHNESS = UUID.nameUUIDFromBytes("xiaoxiang.zhenyuan.toughness".getBytes());
    private static final UUID UUID_ZHENYUAN_SPEED = UUID.nameUUIDFromBytes("xiaoxiang.zhenyuan.speed".getBytes());
    private static final UUID UUID_FOUNDATION_HP = UUID.nameUUIDFromBytes("xiaoxiang.foundation.hp".getBytes());
    private static final UUID UUID_GOLDEN_CORE_HP = UUID.nameUUIDFromBytes("xiaoxiang.goldenCore.hp".getBytes());
    private static final UUID UUID_LOOSE_IMMORTAL_HP = UUID.nameUUIDFromBytes("xiaoxiang.looseImmortal.hp".getBytes());
    private static final UUID UUID_BODY_TEMPERING_HP = UUID.nameUUIDFromBytes("xiaoxiang.bodyTempering.hp".getBytes());
    /** 突破累计生命加成（每次大/小境界突破累加） */
    private static final UUID UUID_BREAKTHROUGH_HP = UUID.nameUUIDFromBytes("xiaoxiang.breakthrough.hp".getBytes());
    /** 渡劫固定快照生命加成（只作为一次 ADDITION 应用） */
    private static final UUID UUID_TRIBULATION_HP_SNAPSHOT = UUID.nameUUIDFromBytes("xiaoxiang.tribulation.hpSnapshot".getBytes());
    /** 境界标准生命基础（把原版基础 20 补到 standardMaxHealth） */
    private static final UUID UUID_REALM_BASE_HP = UUID.nameUUIDFromBytes("xiaoxiang.realm.baseHp".getBytes());
    /** 全局生命倍率（×4，含所有加成）：境界标准生命整体放大 */
    private static final UUID UUID_REALM_HP_MULT = UUID.nameUUIDFromBytes("xiaoxiang.realm.hpMult".getBytes());

    private TechniqueEffectHandler() {
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }
        Player player = event.player;
        if (!(player instanceof ServerPlayer)) {
            return;
        }
        ServerPlayer sp = (ServerPlayer)player;
        CultivationData data = CultivationCapability.get((Player)sp).orElse(null);
        Technique t = TechniqueBonusHelper.equippedOf((Player)sp);
        Technique.Bonus bonus = t == null ? Technique.Bonus.NONE : t.bonus();
        boolean immortalCombo = TechniqueBonusHelper.hasImmortalCombo((Player)sp);
        TechniqueEffectHandler.syncImmortalDarkVision(sp, immortalCombo);
        if (Optional.ofNullable(data).map(d -> d.getSpiritRoot() == SpiritRoot.MUTANT_ICE).orElse(false).booleanValue()) {
            sp.setTicksFrozen(0);
        }
        boolean movementBonusEnabled = data == null || data.isBonusCategoryEnabled(CultivationBonusCategory.MOVEMENT_SPEED);
        TechniqueEffectHandler.applyAttributeModifier((Player)sp, Attributes.MAX_HEALTH, UUID_MAX_HP, "xiaoxiang_tech_maxHp", TechniqueBonusHelper.maxHpBonus((Player)sp), AttributeModifier.Operation.ADDITION);
        TechniqueEffectHandler.applyAttributeModifier((Player)sp, Attributes.MOVEMENT_SPEED, UUID_MOVE_SPEED, "xiaoxiang_tech_moveSpeed", movementBonusEnabled ? bonus.moveSpeed : 0.0, AttributeModifier.Operation.MULTIPLY_BASE);
        TechniqueEffectHandler.applyAttributeModifier((Player)sp, Attributes.KNOCKBACK_RESISTANCE, UUID_KB_RESIST, "xiaoxiang_tech_kbResist", TechniqueBonusHelper.knockbackResist((Player)sp) ? 1.0 : 0.0, AttributeModifier.Operation.ADDITION);
        double spiritHp = SpiritRootBonusHelper.hpBonus((Player)sp);
        TechniqueEffectHandler.applyAttributeModifier((Player)sp, Attributes.MAX_HEALTH, UUID_SPIRIT_HP, "xiaoxiang_spirit_hp", spiritHp, AttributeModifier.Operation.ADDITION);
        double zhenyuanHp = ZhenyuanBonusHelper.constitutionHpBonus((Player)sp);
        // 真元 HP 加成排除在全局 ×4 之外：值 ÷4，×4 后恰好还原原值
        TechniqueEffectHandler.applyAttributeModifier((Player)sp, Attributes.MAX_HEALTH, UUID_ZHENYUAN_HP, "xiaoxiang_zhenyuan_hp", zhenyuanHp, AttributeModifier.Operation.ADDITION);
        // 真元体质加成：每点 +8 盔甲、+3 韧性
        double zhenyuanArmor = ZhenyuanBonusHelper.constitutionArmorBonus((Player)sp);
        TechniqueEffectHandler.applyAttributeModifier((Player)sp, Attributes.ARMOR, UUID_ZHENYUAN_ARMOR, "xiaoxiang_zhenyuan_armor", zhenyuanArmor, AttributeModifier.Operation.ADDITION);
        double zhenyuanToughness = ZhenyuanBonusHelper.constitutionToughnessBonus((Player)sp);
        TechniqueEffectHandler.applyAttributeModifier((Player)sp, Attributes.ARMOR_TOUGHNESS, UUID_ZHENYUAN_TOUGHNESS, "xiaoxiang_zhenyuan_toughness", zhenyuanToughness, AttributeModifier.Operation.ADDITION);
        double foundationHp = FoundationDaoBonusHelper.maxHpMultiplyTotal((Player)sp);
        TechniqueEffectHandler.applyAttributeModifier((Player)sp, Attributes.MAX_HEALTH, UUID_FOUNDATION_HP, "xiaoxiang_foundation_hp", foundationHp, AttributeModifier.Operation.MULTIPLY_TOTAL);
        double goldenCoreHp = GoldenCoreDaoBonusHelper.maxHpMultiplyTotal((Player)sp);
        TechniqueEffectHandler.applyAttributeModifier((Player)sp, Attributes.MAX_HEALTH, UUID_GOLDEN_CORE_HP, "xiaoxiang_golden_core_hp", goldenCoreHp, AttributeModifier.Operation.MULTIPLY_TOTAL);
        double looseImmortalHp = LooseImmortalBonusHelper.maxHpMultiplyTotal((Player)sp);
        TechniqueEffectHandler.applyAttributeModifier((Player)sp, Attributes.MAX_HEALTH, UUID_LOOSE_IMMORTAL_HP, "xiaoxiang_loose_immortal_hp", looseImmortalHp, AttributeModifier.Operation.MULTIPLY_TOTAL);
        double bodyTemperingHp = TechniqueEffectHandler.bodyTemperingHpBonus(sp, data);
        // 锻体 HP 加成排除在全局 ×4 之外：值 ÷4，×4 后恰好还原原值
        TechniqueEffectHandler.applyAttributeModifier((Player)sp, Attributes.MAX_HEALTH, UUID_BODY_TEMPERING_HP, "xiaoxiang_body_tempering_hp", bodyTemperingHp, AttributeModifier.Operation.ADDITION);
        // 境界标准生命基础（原版基础 20 补到 standardMaxHealth）
        double realmBaseHp = data == null ? 0.0 : data.getRealm().standardMaxHealth() - 20.0;
        TechniqueEffectHandler.applyAttributeModifier((Player)sp, Attributes.MAX_HEALTH, UUID_REALM_BASE_HP, "xiaoxiang_realm_base_hp", realmBaseHp, AttributeModifier.Operation.ADDITION);
        // 突破累计生命加成（每次大/小境界突破累加；data 可能为 null，如死亡/复活瞬间 capability 未附加）
        double breakthroughHp = data == null ? 0.0 : (double)data.getBreakthroughHpBonus();
        TechniqueEffectHandler.applyAttributeModifier((Player)sp, Attributes.MAX_HEALTH, UUID_BREAKTHROUGH_HP, "xiaoxiang_breakthrough_hp", breakthroughHp, AttributeModifier.Operation.ADDITION);
        // 渡劫固定快照生命加成：只通过这一条 ADDITION 应用，禁止再次乘算。
        double tribulationHp = data == null ? 0.0 : data.getTribulationHealthBonus();
        TechniqueEffectHandler.applyAttributeModifier((Player)sp, Attributes.MAX_HEALTH, UUID_TRIBULATION_HP_SNAPSHOT, "xiaoxiang_tribulation_hp_snapshot", tribulationHp, AttributeModifier.Operation.ADDITION);
        double zhenyuanSpeed = movementBonusEnabled ? ZhenyuanBonusHelper.agilityMoveSpeedMult((Player)sp) : 0.0;
        TechniqueEffectHandler.applyAttributeModifier((Player)sp, Attributes.MOVEMENT_SPEED, UUID_ZHENYUAN_SPEED, "xiaoxiang_zhenyuan_speed", zhenyuanSpeed, AttributeModifier.Operation.MULTIPLY_BASE);
        TechniqueEffectHandler.syncInfiniteEffect(sp, MobEffects.NIGHT_VISION, bonus.nightVision);
        TechniqueEffectHandler.syncInfiniteEffect(sp, MobEffects.WATER_BREATHING, bonus.waterBreathing);
        TechniqueEffectHandler.syncInfiniteEffect(sp, MobEffects.FIRE_RESISTANCE, bonus.fireResistance);
        boolean brokenVeinBody = PhysiqueBonusHelper.grantsResistanceRegen((Player)sp);
        TechniqueEffectHandler.syncInfiniteEffect(sp, MobEffects.DAMAGE_RESISTANCE, brokenVeinBody);
        TechniqueEffectHandler.syncInfiniteEffect(sp, MobEffects.REGENERATION, brokenVeinBody);
        TechniqueEffectHandler.applyForestShelter(sp);
        if (bonus.autoRegenPerMinute > 0 && sp.tickCount % 1200 == 0 && sp.getHealth() < sp.getMaxHealth()) {
            sp.heal((float)bonus.autoRegenPerMinute);
        }
        if (TechniqueBonusHelper.fireImmune((Player)sp) && sp.getRemainingFireTicks() > 0) {
            sp.setRemainingFireTicks(0);
        }
        if (immortalCombo) {
            if (sp.hasEffect(MobEffects.MOVEMENT_SLOWDOWN)) {
                sp.removeEffect(MobEffects.BLINDNESS);
            }
            if (sp.hasEffect(MobEffects.LEVITATION)) {
                sp.removeEffect(MobEffects.BLINDNESS);
            }
            TechniqueEffectHandler.resetStuckSpeed((LivingEntity)sp);
        }
        // 生命值上限检测：境界/加成变化（如高级境界调回低级）导致最大生命上限降低时，
        // 当前生命值若超过新上限则回落，避免"当前生命值 > 最大生命上限"异常
        if (sp.getHealth() > sp.getMaxHealth()) {
            sp.setHealth(sp.getMaxHealth());
        }
    }

    private static void syncImmortalDarkVision(ServerPlayer sp, boolean enabled) {
        boolean previous;
        boolean known = sp.getPersistentData().contains(TAG_IMMORTAL_DARKVISION_SYNCED);
        boolean bl = previous = known && sp.getPersistentData().getBoolean(TAG_IMMORTAL_DARKVISION_SYNCED);
        if (!known || previous != enabled || sp.tickCount % 20 == 0) {
            sp.getPersistentData().putBoolean(TAG_IMMORTAL_DARKVISION_SYNCED, enabled);
            ModNetwork.CHANNEL.send(PacketDistributor.PLAYER.with(() -> sp), (Object)new SyncImmortalDarkVisionPacket(enabled));
        }
    }

    private static void resetStuckSpeed(LivingEntity entity) {
        try {
            Field f = Entity.class.getDeclaredField("stuckSpeedMultiplier");
            f.setAccessible(true);
            f.set(entity, new Vec3(1.0, 1.0, 1.0));
        }
        catch (Throwable throwable) {
            // empty catch block
        }
    }

    @SubscribeEvent
    public static void onPotionApplicable(MobEffectEvent.Applicable event) {
        LivingEntity livingEntity = event.getEntity();
        if (!(livingEntity instanceof Player)) {
            return;
        }
        Player p = (Player)livingEntity;
        MobEffectInstance inst = event.getEffectInstance();
        if (inst != null && inst.isAmbient() && inst.isInfiniteDuration() && inst.getAmplifier() == 0) {
            return;
        }
        if (TechniqueBonusHelper.blockAllPotions(p)) {
            event.setResult(Event.Result.DENY);
        }
    }

    @SubscribeEvent(priority=EventPriority.LOWEST)
    public static void onDamageHalve(LivingHurtEvent event) {
        LivingEntity livingEntity = event.getEntity();
        if (!(livingEntity instanceof Player)) {
            return;
        }
        Player p = (Player)livingEntity;
        if (TribulationHandler.isTribulationDamage(event.getSource())) {
            return;
        }
        double multiplier = TechniqueBonusHelper.damageTakenMultiplier(p);
        if (multiplier != 1.0) {
            event.setAmount((float)((double)event.getAmount() * multiplier));
        }
    }

    @SubscribeEvent(priority=EventPriority.HIGH)
    public static void onFireImmune(LivingHurtEvent event) {
        LivingEntity livingEntity = event.getEntity();
        if (!(livingEntity instanceof Player)) {
            return;
        }
        Player p = (Player)livingEntity;
        if (!TechniqueBonusHelper.fireImmune(p)) {
            return;
        }
        DamageSource src = event.getSource();
        if (src == null) {
            return;
        }
        if (src.is(DamageTypes.IN_FIRE) || src.is(DamageTypes.ON_FIRE) || src.is(DamageTypes.LAVA) || src.is(DamageTypes.HOT_FLOOR) || src.is(DamageTypes.IN_WALL) || src.is(DamageTypes.UNATTRIBUTED_FIREBALL) || src.is(DamageTypes.FIREBALL)) {
            event.setCanceled(true);
            p.setRemainingFireTicks(0);
        }
    }

    @SubscribeEvent(priority=EventPriority.HIGHEST)
    public static void onKnockbackImmune(LivingKnockBackEvent event) {
        LivingEntity livingEntity = event.getEntity();
        if (!(livingEntity instanceof Player)) {
            return;
        }
        Player p = (Player)livingEntity;
        if (!TechniqueBonusHelper.knockbackResist(p)) {
            return;
        }
        event.setCanceled(true);
    }

    private static void syncInfiniteEffect(ServerPlayer sp, MobEffect effect, boolean shouldHave) {
        boolean hasOtherEffect;
        MobEffectInstance current = sp.getEffect(effect);
        boolean isOurInfiniteEffect = current != null && current.isAmbient() && current.isInfiniteDuration() && current.getAmplifier() == 0;
        boolean bl = hasOtherEffect = current != null && !isOurInfiniteEffect;
        if (shouldHave) {
            if (current == null) {
                sp.addEffect(new MobEffectInstance(effect, -1, 0, true, false, false));
            }
        } else if (isOurInfiniteEffect) {
            sp.removeEffect(effect);
        }
    }

    private static final String TAG_LEGACY_MAX_BODY_TEMPERING_LEVEL = "friday_cultivation_max_body_tempering_level";

    /**
     * 生命值上限检测/强制重算：按当前境界与真元设定重算全部 MAX_HEALTH 加成，
     * 并将当前生命值 clamp 到新上限（用于境界令牌调整境界后立即生效，
     * 避免"切回低境界仍保留高境界血量"）。
     */
    public static void refreshMaxHealth(ServerPlayer sp) {
        if (sp == null) {
            return;
        }
        CultivationData data = CultivationCapability.get((Player)sp).orElse(null);
        if (data == null) {
            return;
        }
        // 与 onPlayerTick 完全一致的 MAX_HEALTH 加成重算
        TechniqueEffectHandler.applyAttributeModifier((Player)sp, Attributes.MAX_HEALTH, UUID_MAX_HP, "xiaoxiang_tech_maxHp", TechniqueBonusHelper.maxHpBonus((Player)sp), AttributeModifier.Operation.ADDITION);
        double spiritHp = SpiritRootBonusHelper.hpBonus((Player)sp);
        TechniqueEffectHandler.applyAttributeModifier((Player)sp, Attributes.MAX_HEALTH, UUID_SPIRIT_HP, "xiaoxiang_spirit_hp", spiritHp, AttributeModifier.Operation.ADDITION);
        double zhenyuanHp = ZhenyuanBonusHelper.constitutionHpBonus((Player)sp);
        // 真元 HP 加成排除在全局 ×4 之外：值 ÷4，×4 后恰好还原原值
        TechniqueEffectHandler.applyAttributeModifier((Player)sp, Attributes.MAX_HEALTH, UUID_ZHENYUAN_HP, "xiaoxiang_zhenyuan_hp", zhenyuanHp, AttributeModifier.Operation.ADDITION);
        // 真元体质加成：每点 +8 盔甲、+3 韧性
        double zhenyuanArmor = ZhenyuanBonusHelper.constitutionArmorBonus((Player)sp);
        TechniqueEffectHandler.applyAttributeModifier((Player)sp, Attributes.ARMOR, UUID_ZHENYUAN_ARMOR, "xiaoxiang_zhenyuan_armor", zhenyuanArmor, AttributeModifier.Operation.ADDITION);
        double zhenyuanToughness = ZhenyuanBonusHelper.constitutionToughnessBonus((Player)sp);
        TechniqueEffectHandler.applyAttributeModifier((Player)sp, Attributes.ARMOR_TOUGHNESS, UUID_ZHENYUAN_TOUGHNESS, "xiaoxiang_zhenyuan_toughness", zhenyuanToughness, AttributeModifier.Operation.ADDITION);
        double foundationHp = FoundationDaoBonusHelper.maxHpMultiplyTotal((Player)sp);
        TechniqueEffectHandler.applyAttributeModifier((Player)sp, Attributes.MAX_HEALTH, UUID_FOUNDATION_HP, "xiaoxiang_foundation_hp", foundationHp, AttributeModifier.Operation.MULTIPLY_TOTAL);
        double goldenCoreHp = GoldenCoreDaoBonusHelper.maxHpMultiplyTotal((Player)sp);
        TechniqueEffectHandler.applyAttributeModifier((Player)sp, Attributes.MAX_HEALTH, UUID_GOLDEN_CORE_HP, "xiaoxiang_golden_core_hp", goldenCoreHp, AttributeModifier.Operation.MULTIPLY_TOTAL);
        double looseImmortalHp = LooseImmortalBonusHelper.maxHpMultiplyTotal((Player)sp);
        TechniqueEffectHandler.applyAttributeModifier((Player)sp, Attributes.MAX_HEALTH, UUID_LOOSE_IMMORTAL_HP, "xiaoxiang_loose_immortal_hp", looseImmortalHp, AttributeModifier.Operation.MULTIPLY_TOTAL);
        double bodyTemperingHp = TechniqueEffectHandler.bodyTemperingHpBonus(sp, data);
        // 锻体 HP 加成排除在全局 ×4 之外：值 ÷4，×4 后恰好还原原值
        TechniqueEffectHandler.applyAttributeModifier((Player)sp, Attributes.MAX_HEALTH, UUID_BODY_TEMPERING_HP, "xiaoxiang_body_tempering_hp", bodyTemperingHp, AttributeModifier.Operation.ADDITION);
        // 境界标准生命基础（原版基础 20 补到 standardMaxHealth）
        double realmBaseHp = data.getRealm().standardMaxHealth() - 20.0;
        TechniqueEffectHandler.applyAttributeModifier((Player)sp, Attributes.MAX_HEALTH, UUID_REALM_BASE_HP, "xiaoxiang_realm_base_hp", realmBaseHp, AttributeModifier.Operation.ADDITION);
        // 突破累计生命加成（每次大/小境界突破累加）
        TechniqueEffectHandler.applyAttributeModifier((Player)sp, Attributes.MAX_HEALTH, UUID_BREAKTHROUGH_HP, "xiaoxiang_breakthrough_hp", (double)data.getBreakthroughHpBonus(), AttributeModifier.Operation.ADDITION);
        // 渡劫固定快照生命加成：只通过这一条 ADDITION 应用，禁止再次乘算。
        double tribulationHp = data == null ? 0.0 : data.getTribulationHealthBonus();
        TechniqueEffectHandler.applyAttributeModifier((Player)sp, Attributes.MAX_HEALTH, UUID_TRIBULATION_HP_SNAPSHOT, "xiaoxiang_tribulation_hp_snapshot", tribulationHp, AttributeModifier.Operation.ADDITION);
        // clamp 当前生命值到新上限
        if (sp.getHealth() > sp.getMaxHealth()) {
            sp.setHealth(sp.getMaxHealth());
        }
    }

    /**
     * 清除锻体最高层数记录（转世重生时调用，重生后为全新凡人）。
     */
    public static void clearBodyTemperingHpBonus(ServerPlayer sp) {
        if (sp != null) {
            // 仅清理一次性旧档迁移键；新逻辑不再读写 PersistentData。
            sp.getPersistentData().remove(TAG_LEGACY_MAX_BODY_TEMPERING_LEVEL);
        }
    }

    /**
     * 锻体生命加成（固定继承值）：
     * - 新逻辑：使用 CultivationData.bodyTemperingHpInherited（锻体境界时按标准值150×锻体百分比计算一次，永久继承）
     * - 旧档迁移：若继承值为 0 且玩家已过锻体（或当前锻体），按旧层数记录折算一次并写入继承值
     */
    private static double bodyTemperingHpBonus(ServerPlayer sp, CultivationData data) {
        if (data == null) {
            return 0.0;
        }
        // 低于锻体的境界（凡人）不享受锻体加成；锻体及更高境界享受
        if (data.getRealm() == Realm.MORTAL) {
            return 0.0;
        }
        // 旧 PersistentData 只在首次遇到旧键时迁移一次，随后立即删除。
        if (data.getBodyTemperingHpInherited() <= 0.0) {
            int legacyLevel = sp.getPersistentData().getInt(TAG_LEGACY_MAX_BODY_TEMPERING_LEVEL);
            if (legacyLevel > 0) {
                data.setBodyTemperingHpInherited(150.0 * CultivationData.bodyTemperingPercent(legacyLevel));
            }
        }
        sp.getPersistentData().remove(TAG_LEGACY_MAX_BODY_TEMPERING_LEVEL);
        return data.getBodyTemperingHpInherited();
    }

    private static void applyAttributeModifier(Player p, Attribute attr, UUID uuid, String name, double value, AttributeModifier.Operation op) {
        AttributeInstance inst = p.getAttribute(attr);
        if (inst == null) {
            return;
        }
        AttributeModifier existing = inst.getModifier(uuid);
        if (value == 0.0) {
            if (existing != null) {
                inst.removeModifier(uuid);
            }
            return;
        }
        if (existing != null && existing.getAmount() == value && existing.getOperation() == op) {
            return;
        }
        if (existing != null) {
            inst.removeModifier(uuid);
        }
        inst.addPermanentModifier(new AttributeModifier(uuid, name, value, op));
    }

    @SubscribeEvent(priority=EventPriority.HIGH)
    public static void onFall(LivingFallEvent event) {
        LivingEntity livingEntity = event.getEntity();
        if (!(livingEntity instanceof ServerPlayer)) {
            return;
        }
        ServerPlayer sp = (ServerPlayer)livingEntity;
        double reduce = TechniqueBonusHelper.fallDamageReduce((Player)sp);
        if (reduce <= 0.0) {
            return;
        }
        float curDistance = event.getDistance();
        event.setDistance((float)((double)curDistance * (1.0 - reduce)));
    }

    @SubscribeEvent(priority=EventPriority.LOW)
    public static void onUndeadBonus(LivingHurtEvent event) {
        if (event.isCanceled()) {
            return;
        }
        DamageSource src = event.getSource();
        if (src == null) {
            return;
        }
        if (!src.is(DamageTypes.PLAYER_ATTACK)) {
            return;
        }
        Entity entity = src.getEntity();
        if (!(entity instanceof ServerPlayer)) {
            return;
        }
        ServerPlayer sp = (ServerPlayer)entity;
        LivingEntity target = event.getEntity();
        if (!SoulStateHandler.canOrdinaryAffect((Entity)sp, (Entity)target)) {
            return;
        }
        if (!SectCombatHandler.canApplyOffensiveEffect((LivingEntity)sp, target)) {
            return;
        }
        int extra = TechniqueBonusHelper.undeadBonusDamage((Player)sp);
        if (extra <= 0) {
            return;
        }
        if (TechniqueEffectHandler.isEvilCreature(target)) {
            event.setAmount(event.getAmount() + (float)extra);
        }
    }

    @SubscribeEvent(priority=EventPriority.LOW)
    public static void onPhotosynthesisHeal(LivingHealEvent event) {
        LivingEntity livingEntity = event.getEntity();
        if (!(livingEntity instanceof ServerPlayer)) {
            return;
        }
        ServerPlayer sp = (ServerPlayer)livingEntity;
        if (event.getAmount() <= 0.0f) {
            return;
        }
        if (TechniqueBonusHelper.photosynthesisActive((Player)sp)) {
            event.setAmount(event.getAmount() * 1.5f);
        }
    }

    private static void applyForestShelter(ServerPlayer sp) {
        if (sp.tickCount % 10 != 0) {
            return;
        }
        if (!TechniqueBonusHelper.forestShelter((Player)sp)) {
            return;
        }
        if (!TechniqueEffectHandler.isStandingOnNaturalBlock(sp)) {
            return;
        }
        sp.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 30, 2, true, true, true));
    }

    private static boolean isStandingOnNaturalBlock(ServerPlayer sp) {
        BlockState state = sp.level().getBlockState(sp.blockPosition().below());
        return state.is(BlockTags.DIRT) || state.is(BlockTags.LOGS) || state.is(BlockTags.LEAVES) || state.is(Blocks.GRASS_BLOCK) || state.is(Blocks.DIRT) || state.is(Blocks.COARSE_DIRT) || state.is(Blocks.ROOTED_DIRT) || state.is(Blocks.PODZOL) || state.is(Blocks.MYCELIUM) || state.is(Blocks.MUD) || state.is(Blocks.MOSS_BLOCK) || state.is(Blocks.FARMLAND);
    }

    private static boolean isEvilCreature(LivingEntity target) {
        if (target == null) {
            return false;
        }
        if (target.getMobType() == MobType.UNDEAD) {
            return true;
        }
        if (target instanceof Raider) {
            return true;
        }
        return target instanceof Illusioner;
    }
}

