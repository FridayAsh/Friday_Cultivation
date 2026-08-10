package com.friday.cultivation.event;

import com.friday.cultivation.CultivationBonusCategory;
import com.friday.cultivation.CultivationCapability;
import com.friday.cultivation.capability.CultivationData;
import com.friday.cultivation.dao.FoundationDaoBonusHelper;
import com.friday.cultivation.dao.GoldenCoreDaoBonusHelper;
import com.friday.cultivation.dao.LooseImmortalBonusHelper;
import com.friday.cultivation.network.ModNetwork;
import com.friday.cultivation.network.SyncImmortalDarkVisionPacket;
import com.friday.cultivation.physique.PhysiqueBonusHelper;
import com.friday.cultivation.spirit.SpiritRoot;
import com.friday.cultivation.spirit.SpiritRootBonusHelper;
import com.friday.cultivation.technique.Technique;
import com.friday.cultivation.technique.TechniqueBonusHelper;
import com.friday.cultivation.ZhenyuanBonusHelper;
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

@Mod.EventBusSubscriber(modid = "friday_cultivation")
public final class TechniqueEffectHandler {
    private static final int REGEN_TICK_INTERVAL = 1200;
    private static final int FOREST_SHELTER_REFRESH_INTERVAL = 10;
    private static final int FOREST_SHELTER_REGEN_DURATION = 30;
    private static final int FOREST_SHELTER_REGEN_AMP = 2;
    private static final String TAG_IMMORTAL_DARKVISION_SYNCED = "friday_immortal_darkvision_synced";
    private static final int INFINITE_EFFECT_AMP = 0;
    private static final UUID UUID_MAX_HP = UUID.nameUUIDFromBytes("friday.tech.maxHp".getBytes());
    private static final UUID UUID_MOVE_SPEED = UUID.nameUUIDFromBytes("friday.tech.moveSpeed".getBytes());
    private static final UUID UUID_KB_RESIST = UUID.nameUUIDFromBytes("friday.tech.kbResist".getBytes());
    private static final UUID UUID_SPIRIT_HP = UUID.nameUUIDFromBytes("friday.spiritroot.hp".getBytes());
    private static final UUID UUID_ZHENYUAN_HP = UUID.nameUUIDFromBytes("friday.zhenyuan.hp".getBytes());
    private static final UUID UUID_ZHENYUAN_SPEED = UUID.nameUUIDFromBytes("friday.zhenyuan.speed".getBytes());
    private static final UUID UUID_FOUNDATION_HP = UUID.nameUUIDFromBytes("friday.foundation.hp".getBytes());
    private static final UUID UUID_GOLDEN_CORE_HP = UUID.nameUUIDFromBytes("friday.goldenCore.hp".getBytes());
    private static final UUID UUID_LOOSE_IMMORTAL_HP = UUID.nameUUIDFromBytes("friday.looseImmortal.hp".getBytes());

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
        ServerPlayer sp = (ServerPlayer) player;
        CultivationData data = CultivationCapability.get((Player) sp).orElse(null);
        Technique t = TechniqueBonusHelper.equippedOf((Player) sp);
        Technique.Bonus bonus = t == null ? Technique.Bonus.NONE : t.bonus();
        boolean immortalCombo = TechniqueBonusHelper.hasImmortalCombo((Player) sp);
        TechniqueEffectHandler.syncImmortalDarkVision(sp, immortalCombo);
        if (Optional.ofNullable(data).map(d -> d.getSpiritRoot() == SpiritRoot.MUTANT_ICE).orElse(false).booleanValue()) {
            sp.setTicksFrozen(0);
        }
        boolean movementBonusEnabled = data == null || data.isBonusCategoryEnabled(CultivationBonusCategory.MOVEMENT_SPEED);
        TechniqueEffectHandler.applyAttributeModifier((Player) sp, Attributes.MAX_HEALTH, UUID_MAX_HP, "friday_tech_maxHp", TechniqueBonusHelper.maxHpBonus((Player) sp), AttributeModifier.Operation.ADDITION);
        TechniqueEffectHandler.applyAttributeModifier((Player) sp, Attributes.MOVEMENT_SPEED, UUID_MOVE_SPEED, "friday_tech_moveSpeed", movementBonusEnabled ? bonus.moveSpeed : 0.0, AttributeModifier.Operation.MULTIPLY_BASE);
        TechniqueEffectHandler.applyAttributeModifier((Player) sp, Attributes.KNOCKBACK_RESISTANCE, UUID_KB_RESIST, "friday_tech_kbResist", TechniqueBonusHelper.knockbackResist((Player) sp) ? 1.0 : 0.0, AttributeModifier.Operation.ADDITION);
        double spiritHp = SpiritRootBonusHelper.hpBonus((Player) sp);
        TechniqueEffectHandler.applyAttributeModifier((Player) sp, Attributes.MAX_HEALTH, UUID_SPIRIT_HP, "friday_spirit_hp", spiritHp, AttributeModifier.Operation.ADDITION);
        double zhenyuanHp = ZhenyuanBonusHelper.constitutionHpBonus((Player) sp);
        TechniqueEffectHandler.applyAttributeModifier((Player) sp, Attributes.MAX_HEALTH, UUID_ZHENYUAN_HP, "friday_zhenyuan_hp", zhenyuanHp, AttributeModifier.Operation.ADDITION);
        double foundationHp = FoundationDaoBonusHelper.maxHpMultiplyTotal((Player) sp);
        TechniqueEffectHandler.applyAttributeModifier((Player) sp, Attributes.MAX_HEALTH, UUID_FOUNDATION_HP, "friday_foundation_hp", foundationHp, AttributeModifier.Operation.MULTIPLY_TOTAL);
        double goldenCoreHp = GoldenCoreDaoBonusHelper.maxHpMultiplyTotal((Player) sp);
        TechniqueEffectHandler.applyAttributeModifier((Player) sp, Attributes.MAX_HEALTH, UUID_GOLDEN_CORE_HP, "friday_golden_core_hp", goldenCoreHp, AttributeModifier.Operation.MULTIPLY_TOTAL);
        double looseImmortalHp = LooseImmortalBonusHelper.maxHpMultiplyTotal((Player) sp);
        TechniqueEffectHandler.applyAttributeModifier((Player) sp, Attributes.MAX_HEALTH, UUID_LOOSE_IMMORTAL_HP, "friday_loose_immortal_hp", looseImmortalHp, AttributeModifier.Operation.MULTIPLY_TOTAL);
        double zhenyuanSpeed = movementBonusEnabled ? ZhenyuanBonusHelper.agilityMoveSpeedMult((Player) sp) : 0.0;
        TechniqueEffectHandler.applyAttributeModifier((Player) sp, Attributes.MOVEMENT_SPEED, UUID_ZHENYUAN_SPEED, "friday_zhenyuan_speed", zhenyuanSpeed, AttributeModifier.Operation.MULTIPLY_BASE);
        TechniqueEffectHandler.syncInfiniteEffect(sp, MobEffects.NIGHT_VISION, bonus.nightVision);
        TechniqueEffectHandler.syncInfiniteEffect(sp, MobEffects.WATER_BREATHING, bonus.waterBreathing);
        TechniqueEffectHandler.syncInfiniteEffect(sp, MobEffects.FIRE_RESISTANCE, bonus.fireResistance);
        boolean brokenVeinBody = PhysiqueBonusHelper.grantsResistanceRegen((Player) sp);
        TechniqueEffectHandler.syncInfiniteEffect(sp, MobEffects.DAMAGE_RESISTANCE, brokenVeinBody);
        TechniqueEffectHandler.syncInfiniteEffect(sp, MobEffects.REGENERATION, brokenVeinBody);
        TechniqueEffectHandler.applyForestShelter(sp);
        if (bonus.autoRegenPerMinute > 0 && sp.tickCount % 1200 == 0 && sp.getHealth() < sp.getMaxHealth()) {
            sp.heal((float) bonus.autoRegenPerMinute);
        }
        if (TechniqueBonusHelper.fireImmune((Player) sp) && sp.getRemainingFireTicks() > 0) {
            sp.setRemainingFireTicks(0);
        }
        if (immortalCombo) {
            if (sp.hasEffect(MobEffects.MOVEMENT_SLOWDOWN)) {
                sp.removeEffect(MobEffects.MOVEMENT_SLOWDOWN);
            }
            if (sp.hasEffect(MobEffects.LEVITATION)) {
                sp.removeEffect(MobEffects.LEVITATION);
            }
            TechniqueEffectHandler.resetStuckSpeed((LivingEntity) sp);
        }
    }

    private static void syncImmortalDarkVision(ServerPlayer sp, boolean enabled) {
        boolean previous;
        boolean known = sp.getPersistentData().contains(TAG_IMMORTAL_DARKVISION_SYNCED);
        boolean bl = previous = known && sp.getPersistentData().getBoolean(TAG_IMMORTAL_DARKVISION_SYNCED);
        if (!known || previous != enabled || sp.tickCount % 20 == 0) {
            sp.getPersistentData().putBoolean(TAG_IMMORTAL_DARKVISION_SYNCED, enabled);
            ModNetwork.CHANNEL.send(PacketDistributor.PLAYER.with(() -> sp), new SyncImmortalDarkVisionPacket(enabled));
        }
    }

    private static void resetStuckSpeed(LivingEntity entity) {
        try {
            Field f = Entity.class.getDeclaredField("stuckSpeedMultiplier");
            f.setAccessible(true);
            f.set(entity, new Vec3(1.0, 1.0, 1.0));
        } catch (Throwable throwable) {
            // empty catch block
        }
    }

    @SubscribeEvent
    public static void onPotionApplicable(MobEffectEvent.Applicable event) {
        LivingEntity livingEntity = event.getEntity();
        if (!(livingEntity instanceof Player)) {
            return;
        }
        Player p = (Player) livingEntity;
        MobEffectInstance inst = event.getEffectInstance();
        if (inst != null && inst.isInfiniteDuration() && inst.isAmbient() && inst.getAmplifier() == 0) {
            return;
        }
        if (TechniqueBonusHelper.blockAllPotions(p)) {
            event.setResult(Event.Result.DENY);
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onDamageHalve(LivingHurtEvent event) {
        LivingEntity livingEntity = event.getEntity();
        if (!(livingEntity instanceof Player)) {
            return;
        }
        Player p = (Player) livingEntity;
        if (TribulationHandler.isTribulationDamage(event.getSource())) {
            return;
        }
        double multiplier = TechniqueBonusHelper.damageTakenMultiplier(p);
        if (multiplier != 1.0) {
            event.setAmount((float) ((double) event.getAmount() * multiplier));
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void onFireImmune(LivingHurtEvent event) {
        LivingEntity livingEntity = event.getEntity();
        if (!(livingEntity instanceof Player)) {
            return;
        }
        Player p = (Player) livingEntity;
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

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onKnockbackImmune(LivingKnockBackEvent event) {
        LivingEntity livingEntity = event.getEntity();
        if (!(livingEntity instanceof Player)) {
            return;
        }
        Player p = (Player) livingEntity;
        if (!TechniqueBonusHelper.knockbackResist(p)) {
            return;
        }
        event.setCanceled(true);
    }

    private static void syncInfiniteEffect(ServerPlayer sp, MobEffect effect, boolean shouldHave) {
        boolean hasOtherEffect;
        MobEffectInstance current = sp.getEffect(effect);
        boolean isOurInfiniteEffect = current != null && current.isInfiniteDuration() && current.isAmbient() && current.getAmplifier() == 0;
        boolean bl = hasOtherEffect = current != null && !isOurInfiniteEffect;
        if (shouldHave) {
            if (current == null) {
                sp.addEffect(new MobEffectInstance(effect, -1, 0, true, false, false));
            }
        } else if (isOurInfiniteEffect) {
            sp.removeEffect(effect);
        }
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

    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void onFall(LivingFallEvent event) {
        LivingEntity livingEntity = event.getEntity();
        if (!(livingEntity instanceof ServerPlayer)) {
            return;
        }
        ServerPlayer sp = (ServerPlayer) livingEntity;
        double reduce = TechniqueBonusHelper.fallDamageReduce((Player) sp);
        if (reduce <= 0.0) {
            return;
        }
        float curDistance = event.getDistance();
        event.setDistance((float) ((double) curDistance * (1.0 - reduce)));
    }

    @SubscribeEvent(priority = EventPriority.LOW)
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
        ServerPlayer sp = (ServerPlayer) entity;
        LivingEntity target = event.getEntity();
        if (!SoulStateHandler.canOrdinaryAffect((Entity) sp, (Entity) target)) {
            return;
        }
        if (!SectCombatHandler.canApplyOffensiveEffect((LivingEntity) sp, target)) {
            return;
        }
        int extra = TechniqueBonusHelper.undeadBonusDamage((Player) sp);
        if (extra <= 0) {
            return;
        }
        if (TechniqueEffectHandler.isEvilCreature(target)) {
            event.setAmount(event.getAmount() + (float) extra);
        }
    }

    @SubscribeEvent(priority = EventPriority.LOW)
    public static void onPhotosynthesisHeal(LivingHealEvent event) {
        LivingEntity livingEntity = event.getEntity();
        if (!(livingEntity instanceof ServerPlayer)) {
            return;
        }
        ServerPlayer sp = (ServerPlayer) livingEntity;
        if (event.getAmount() <= 0.0f) {
            return;
        }
        if (TechniqueBonusHelper.photosynthesisActive((Player) sp)) {
            event.setAmount(event.getAmount() * 1.5f);
        }
    }

    private static void applyForestShelter(ServerPlayer sp) {
        if (sp.tickCount % 10 != 0) {
            return;
        }
        if (!TechniqueBonusHelper.forestShelter((Player) sp)) {
            return;
        }
        if (!TechniqueEffectHandler.isStandingOnNaturalBlock(sp)) {
            return;
        }
        sp.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 30, 2, true, true, true));
    }

    private static boolean isStandingOnNaturalBlock(ServerPlayer sp) {
        BlockState state = sp.level().getBlockState(sp.blockPosition().below());
        return state.is(BlockTags.DIRT) || state.is(BlockTags.LOGS) || state.is(BlockTags.LEAVES) || state.is(Blocks.GRASS_BLOCK) || state.is(Blocks.DIRT) || state.is(Blocks.COARSE_DIRT) || state.is(Blocks.ROOTED_DIRT) || state.is(Blocks.PODZOL) || state.is(Blocks.MYCELIUM) || state.is(Blocks.MOSS_BLOCK) || state.is(Blocks.MUD) || state.is(Blocks.FARMLAND);
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
