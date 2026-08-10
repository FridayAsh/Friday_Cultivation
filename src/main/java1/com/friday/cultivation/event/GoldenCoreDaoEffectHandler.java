package com.friday.cultivation.event;

import com.friday.cultivation.CultivationCapability;
import com.friday.cultivation.capability.CultivationData;
import com.friday.cultivation.dao.GoldenCoreDao;
import com.friday.cultivation.realm.Realm;
import com.friday.cultivation.util.SpellDamageSourceHelper;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.jetbrains.annotations.Nullable;

import java.util.Locale;

/**
 * 金丹道效果处理器 - 完全照搬原 mod: xiaoxiang.cultivation.event.GoldenCoreDaoEffectHandler（281 行完整版）。
 * <p>地道（致命伤护盾/免死）/ 天道（法术附带 10% 真实伤害）/ 血道（击杀回血回灵）。</p>
 */
@Mod.EventBusSubscriber(modid = "friday_cultivation")
public final class GoldenCoreDaoEffectHandler {
    private static final String TAG_EARTH_SHIELD_READY_TICK = "friday_cultivation.earth_golden_core_shield_ready";
    private static final int EARTH_SHIELD_QI_COST = 100;
    private static final long EARTH_SHIELD_COOLDOWN_TICKS = 3600L;
    private static final float HEAVEN_TRUE_DAMAGE_RATIO = 0.1f;
    private static final ThreadLocal<Boolean> SUPPRESS_TRUE_DAMAGE = ThreadLocal.withInitial(() -> false);

    private GoldenCoreDaoEffectHandler() {
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onLivingHurt(LivingHurtEvent event) {
        if (event.isCanceled()) {
            return;
        }
        if (TribulationHandler.isTribulationDamage(event.getSource())) {
            return;
        }
        LivingEntity livingEntity = event.getEntity();
        if (!(livingEntity instanceof ServerPlayer)) {
            return;
        }
        ServerPlayer player = (ServerPlayer) livingEntity;
        if (event.getAmount() < player.getHealth()) {
            return;
        }
        if (GoldenCoreDaoEffectHandler.tryTriggerEarthShield(player)) {
            event.setCanceled(true);
            event.setAmount(0.0f);
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onLivingDamage(LivingDamageEvent event) {
        if (event.isCanceled()) {
            return;
        }
        if (SUPPRESS_TRUE_DAMAGE.get().booleanValue()) {
            return;
        }
        if (event.getAmount() <= 0.0f) {
            return;
        }
        LivingEntity target = event.getEntity();
        LivingEntity attacker = GoldenCoreDaoEffectHandler.resolveAttacker(event.getSource());
        if (!(attacker instanceof ServerPlayer)) {
            return;
        }
        ServerPlayer player = (ServerPlayer) attacker;
        if (attacker == target) {
            return;
        }
        if (!SoulStateHandler.canOrdinaryAffect((Entity) player, (Entity) target)) {
            return;
        }
        if (!SectCombatHandler.canApplyOffensiveEffect((LivingEntity) player, target)) {
            return;
        }
        if (!GoldenCoreDaoEffectHandler.isSpellLike(event.getSource())) {
            return;
        }
        boolean heavenCore = CultivationCapability.get((Player) player).map(data -> data.getRealm().ordinal() >= Realm.GOLDEN_CORE.ordinal() && data.getGoldenCoreDao() == GoldenCoreDao.HEAVEN).orElse(false);
        if (!heavenCore) {
            return;
        }
        float actualLost = Math.min(event.getAmount(), Math.max(0.0f, target.getHealth()));
        float trueDamage = actualLost * HEAVEN_TRUE_DAMAGE_RATIO;
        GoldenCoreDaoEffectHandler.applyTrueDamage(target, trueDamage, (LivingEntity) player);
    }

    @SubscribeEvent
    public static void onLivingDeath(LivingDeathEvent event) {
        LivingEntity attacker = GoldenCoreDaoEffectHandler.resolveAttacker(event.getSource());
        if (!(attacker instanceof ServerPlayer)) {
            return;
        }
        ServerPlayer player = (ServerPlayer) attacker;
        if (attacker == event.getEntity()) {
            return;
        }
        if (!SoulStateHandler.canOrdinaryAffect((Entity) player, (Entity) event.getEntity())) {
            return;
        }
        if (!SectCombatHandler.canApplyOffensiveEffect((LivingEntity) player, event.getEntity())) {
            return;
        }
        boolean bloodCore = CultivationCapability.get((Player) player).map(data -> data.getRealm().ordinal() >= Realm.GOLDEN_CORE.ordinal() && data.getGoldenCoreDao() == GoldenCoreDao.BLOOD).orElse(false);
        if (!bloodCore) {
            return;
        }
        player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 120, 1, false, true, true));
        player.heal(4.0f);
        if (player.serverLevel() != null) {
            player.serverLevel().sendParticles(ParticleTypes.HEART, player.getX(), player.getY() + (double) player.getBbHeight() * 0.75, player.getZ(), 8, 0.35, 0.35, 0.35, 0.04);
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onPlayerDeathPrevention(LivingDeathEvent event) {
        if (event.isCanceled()) {
            return;
        }
        LivingEntity livingEntity = event.getEntity();
        if (!(livingEntity instanceof ServerPlayer)) {
            return;
        }
        ServerPlayer player = (ServerPlayer) livingEntity;
        if (!GoldenCoreDaoEffectHandler.tryTriggerEarthShieldFromDeath(player, event.getSource())) {
            return;
        }
        event.setCanceled(true);
    }

    public static boolean tryTriggerEarthShieldFromDeath(ServerPlayer player, DamageSource source) {
        if (player == null) {
            return false;
        }
        if (source != null && source.is(DamageTypeTags.BYPASSES_INVULNERABILITY)) {
            return false;
        }
        if (TribulationHandler.isTribulationDamage(source)) {
            return false;
        }
        return GoldenCoreDaoEffectHandler.tryTriggerEarthShield(player);
    }

    private static boolean tryTriggerEarthShield(ServerPlayer player) {
        long ready;
        CultivationData data = CultivationCapability.get((Player) player).orElse(null);
        if (data == null || data.getRealm().ordinal() < Realm.GOLDEN_CORE.ordinal() || data.getGoldenCoreDao() != GoldenCoreDao.EARTH) {
            return false;
        }
        long now = player.serverLevel().getGameTime();
        ready = player.getPersistentData().getLong(TAG_EARTH_SHIELD_READY_TICK);
        if (now < ready || data.getCurrentQi() < (long) EARTH_SHIELD_QI_COST) {
            return false;
        }
        data.setCurrentQi(data.getCurrentQi() - (long) EARTH_SHIELD_QI_COST);
        player.getPersistentData().putLong(TAG_EARTH_SHIELD_READY_TICK, now + EARTH_SHIELD_COOLDOWN_TICKS);
        player.setHealth(Math.max(1.0f, player.getHealth()));
        player.invulnerableTime = 20;
        CapabilityEvents.syncToClient(player);
        player.sendSystemMessage(Component.translatable("message.friday_cultivation.golden_core.earth_shield_triggered"), true);
        GoldenCoreDaoEffectHandler.spawnEarthShieldFeedback(player);
        return true;
    }

    private static void spawnEarthShieldFeedback(ServerPlayer player) {
        ServerLevel level = player.serverLevel();
        level.sendParticles(ParticleTypes.TOTEM_OF_UNDYING, player.getX(), player.getY() + 1.0, player.getZ(), 28, 0.55, 0.65, 0.55, 0.08);
        level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.SHIELD_BLOCK, SoundSource.PLAYERS, 1.0f, 0.72f);
        level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 0.55f, 0.62f);
    }

    private static boolean isSpellLike(DamageSource source) {
        if (source == null) {
            return false;
        }
        String id = source.getMsgId();
        return id != null && id.toLowerCase(Locale.ROOT).contains("magic");
    }

    private static float applyTrueDamage(LivingEntity target, float amount, LivingEntity caster) {
        if (amount <= 0.0f || target == null || !target.isAlive()) {
            return 0.0f;
        }
        float before = target.getHealth();
        SUPPRESS_TRUE_DAMAGE.set(true);
        try {
            if (before > amount) {
                target.setHealth(Math.max(0.0f, before - amount));
            } else {
                target.invulnerableTime = 0;
                target.setHealth(Math.max(1.0f, before));
                BloodthirstCurseHandler.hurtWithoutEventReward(target, SpellDamageSourceHelper.directSpell(caster), Math.max(1000.0f, amount + before));
            }
        } finally {
            SUPPRESS_TRUE_DAMAGE.set(false);
        }
        float actualLost = Math.max(0.0f, before - Math.max(0.0f, target.getHealth()));
        BloodthirstCurseHandler.rewardFromActualDamage(caster, target, actualLost);
        return actualLost;
    }

    @Nullable
    private static LivingEntity resolveAttacker(DamageSource source) {
        if (source == null) {
            return null;
        }
        Entity cause = source.getEntity();
        if (cause instanceof LivingEntity) {
            LivingEntity living = (LivingEntity) cause;
            return living;
        }
        Entity direct = source.getDirectEntity();
        if (direct instanceof Projectile) {
            Projectile projectile = (Projectile) direct;
            Entity entity = projectile.getOwner();
            if (entity instanceof LivingEntity) {
                LivingEntity owner = (LivingEntity) entity;
                return owner;
            }
        }
        if (direct instanceof LivingEntity) {
            LivingEntity living = (LivingEntity) direct;
            return living;
        }
        return null;
    }
}
