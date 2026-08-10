package com.friday.cultivation.event;

import com.friday.cultivation.BodyDefenseHelper;
import com.friday.cultivation.CultivationCapability;
import com.friday.cultivation.capability.CultivationData;
import com.friday.cultivation.realm.Realm;
import com.friday.cultivation.spell.Spell;
import com.friday.cultivation.technique.TechniqueBonusHelper;
import com.friday.cultivation.entity.npc.NpcSpellCaster;
import com.friday.cultivation.entity.npc.WanderingCultivatorEntity;
import com.friday.cultivation.event.CapabilityEvents;
import com.friday.cultivation.event.DharmaBodyManifestationHandler;
import com.friday.cultivation.event.RealmPressureHandler;
import com.friday.cultivation.event.SpiritLockHandler;
import com.friday.cultivation.network.ModNetwork;
import com.friday.cultivation.network.QiShieldHitPacket;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.PacketDistributor;

/**
 * 灵气护盾处理器 - 用灵气抵消玩家/散修的伤害。
 * 完全照搬原 mod: xiaoxiang.cultivation.event.QiShieldHandler
 */
@Mod.EventBusSubscriber(modid = "friday_cultivation")
public final class QiShieldHandler {
    private static final long QI_PER_DAMAGE = 10L;

    private QiShieldHandler() {
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void onLivingAttack(LivingAttackEvent event) {
        LivingEntity livingEntity = event.getEntity();
        if (livingEntity instanceof WanderingCultivatorEntity) {
            WanderingCultivatorEntity npc = (WanderingCultivatorEntity) livingEntity;
            QiShieldHandler.handleNpcAttack(event, npc);
            return;
        }
        livingEntity = event.getEntity();
        if (!(livingEntity instanceof ServerPlayer)) {
            return;
        }
        ServerPlayer player = (ServerPlayer) livingEntity;
        float incoming = event.getAmount();
        if (incoming <= 0.0f) {
            return;
        }
        CultivationData data = CultivationCapability.get((Player) player).orElse(null);
        if (data == null) {
            return;
        }
        DharmaBodyManifestationHandler.trigger(player);
        if (QiShieldHandler.shouldSkipQiShieldForPreImmuneDamage((LivingEntity) player, event.getSource())) {
            return;
        }
        if (QiShieldHandler.canUsePlayerQiShield(player, data)) {
            float maxAbsorb = QiShieldHandler.maxAbsorbableDamage(player, incoming, data.getRealm());
            long currentQi = data.getCurrentQi();
            if (maxAbsorb >= incoming - 1.0E-4f && currentQi >= QiShieldHandler.qiNeededForDamage(player, incoming)) {
                long qiNeeded = QiShieldHandler.qiNeededForDamage(player, incoming);
                data.setCurrentQi(currentQi - qiNeeded);
                event.setCanceled(true);
                QiShieldHandler.broadcastShieldHit(player, event.getSource(), incoming);
                CapabilityEvents.syncToClient(player);
                return;
            }
            if (maxAbsorb > 0.0f) {
                return;
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void onLivingHurt(LivingHurtEvent event) {
        LivingEntity livingEntity = event.getEntity();
        if (livingEntity instanceof WanderingCultivatorEntity) {
            WanderingCultivatorEntity npc = (WanderingCultivatorEntity) livingEntity;
            QiShieldHandler.handleNpcHurt(event, npc);
            return;
        }
        livingEntity = event.getEntity();
        if (!(livingEntity instanceof ServerPlayer)) {
            return;
        }
        ServerPlayer player = (ServerPlayer) livingEntity;
        float incoming = event.getAmount();
        if (incoming <= 0.0f) {
            return;
        }
        CultivationData data = CultivationCapability.get((Player) player).orElse(null);
        if (data == null) {
            return;
        }
        DharmaBodyManifestationHandler.trigger(player);
        if (QiShieldHandler.shouldSkipQiShieldForPreImmuneDamage((LivingEntity) player, event.getSource())) {
            return;
        }
        if (QiShieldHandler.canUsePlayerQiShield(player, data)) {
            float maxAbsorb = QiShieldHandler.maxAbsorbableDamage(player, incoming, data.getRealm());
            long currentQi = data.getCurrentQi();
            long qiToSpend = Math.min(currentQi, QiShieldHandler.qiNeededForDamage(player, maxAbsorb));
            float absorbedDamage = QiShieldHandler.absorbedDamageFromQi(qiToSpend, maxAbsorb, QiShieldHandler.qiPerDamage(player));
            if (absorbedDamage > 0.0f) {
                data.setCurrentQi(currentQi - qiToSpend);
                QiShieldHandler.broadcastShieldHit(player, event.getSource(), absorbedDamage);
                CapabilityEvents.syncToClient(player);
                incoming = Math.max(0.0f, incoming - absorbedDamage);
                event.setAmount(incoming);
                if (incoming <= 0.0f) {
                    event.setCanceled(true);
                    return;
                }
            }
        }
        QiShieldHandler.applyPlayerDefenseAtHurt(event, player, data, incoming);
    }

    private static void applyPlayerDefenseAtHurt(LivingHurtEvent event, ServerPlayer player, CultivationData data, float incoming) {
        float afterDef = QiShieldHandler.afterPlayerDefense(player, data, incoming);
        if (afterDef <= 0.0f) {
            event.setCanceled(true);
            return;
        }
        event.setAmount(afterDef);
    }

    private static float afterPlayerDefense(ServerPlayer player, CultivationData data, float incoming) {
        int bodyDef = BodyDefenseHelper.playerEffectiveBodyDefense((Player) player);
        float effectiveDefense = (float) ((double) bodyDef * RealmPressureHandler.bodyDefenseMultiplier((LivingEntity) player) * DharmaBodyManifestationHandler.defenseMultiplier(player));
        return Math.max(0.0f, incoming - effectiveDefense);
    }

    private static void broadcastShieldHit(ServerPlayer player, DamageSource src, float intensity) {
        Vec3 dir;
        double n;
        Vec3 sourcePos = null;
        if (src != null) {
            Entity direct = src.getDirectEntity();
            Entity attacker = src.getEntity();
            if (direct != null) {
                sourcePos = direct.position().add(0.0, (double) direct.getBbHeight() * 0.5, 0.0);
            } else if (attacker != null) {
                sourcePos = attacker.position().add(0.0, (double) attacker.getBbHeight() * 0.5, 0.0);
            } else if (src.getSourcePosition() != null) {
                sourcePos = src.getSourcePosition();
            }
        }
        Vec3 playerPos = player.position().add(0.0, (double) player.getBbHeight() * 0.55, 0.0);
        dir = sourcePos != null ? ((n = (dir = playerPos.subtract(sourcePos)).length()) > 1.0E-4 ? dir.scale(1.0 / n) : new Vec3(0.0, -1.0, 0.0)) : new Vec3(0.0, -1.0, 0.0);
        QiShieldHitPacket packet = new QiShieldHitPacket(player.getId(), (float) dir.x, (float) dir.y, (float) dir.z, intensity);
        ModNetwork.CHANNEL.send(PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> player), packet);
    }

    private static long qiNeededForDamage(float damage) {
        if (damage <= 0.0f) {
            return 0L;
        }
        return Math.max(1L, (long) Math.ceil(damage * 10.0f));
    }

    private static long qiNeededForDamage(ServerPlayer player, float damage) {
        if (damage <= 0.0f) {
            return 0L;
        }
        return Math.max(1L, (long) Math.ceil((double) damage * QiShieldHandler.qiPerDamage(player)));
    }

    private static long qiNeededForDamage(WanderingCultivatorEntity npc, float damage) {
        if (damage <= 0.0f) {
            return 0L;
        }
        return Math.max(1L, (long) Math.ceil((double) damage * QiShieldHandler.qiPerDamage(npc)));
    }

    private static double qiPerDamage(ServerPlayer player) {
        if (DharmaBodyManifestationHandler.grantsPerfectQiShield(player)) {
            return 1.0;
        }
        return Math.max(1.0, 10.0 * TechniqueBonusHelper.generalQiCostMultiplier((Player) player));
    }

    private static double qiPerDamage(WanderingCultivatorEntity npc) {
        if (DharmaBodyManifestationHandler.grantsPerfectQiShield(npc)) {
            return 1.0;
        }
        return Math.max(1.0, 10.0 * NpcSpellCaster.generalQiCostMultiplier(npc));
    }

    private static boolean canUsePlayerQiShield(ServerPlayer player, CultivationData data) {
        return data != null && !SpiritLockHandler.isEntityLocked((Entity) player) && data.isSpellEnabled(Spell.QI_SHIELD) && data.getCurrentQi() > 0L;
    }

    private static float maxAbsorbableDamage(float incoming, Realm realm) {
        if (incoming <= 0.0f || realm == null) {
            return 0.0f;
        }
        int percent = realm.qiShieldReductionPercent();
        if (percent <= 0) {
            return 0.0f;
        }
        return Math.min(incoming, incoming * (float) percent / 100.0f);
    }

    private static float maxAbsorbableDamage(ServerPlayer player, float incoming, Realm realm) {
        if (DharmaBodyManifestationHandler.grantsPerfectQiShield(player)) {
            return Math.max(0.0f, incoming);
        }
        return QiShieldHandler.maxAbsorbableDamage(incoming, realm);
    }

    private static float maxAbsorbableDamage(WanderingCultivatorEntity npc, float incoming, Realm realm) {
        if (DharmaBodyManifestationHandler.grantsPerfectQiShield(npc)) {
            return Math.max(0.0f, incoming);
        }
        return QiShieldHandler.maxAbsorbableDamage(incoming, realm);
    }

    private static float absorbedDamageFromQi(long qiSpent, float incoming, double qiPerDamage) {
        if (qiSpent <= 0L || incoming <= 0.0f) {
            return 0.0f;
        }
        if (!Double.isFinite(qiPerDamage) || qiPerDamage <= 0.0) {
            qiPerDamage = 10.0;
        }
        return Math.min(incoming, (float) ((double) qiSpent / qiPerDamage));
    }

    private static boolean shouldSkipQiShieldForPreImmuneDamage(LivingEntity target, DamageSource source) {
        Player player;
        if (target == null || source == null) {
            return false;
        }
        if (target.level().isClientSide || target.isRemoved()) {
            return true;
        }
        if (target.isInvulnerableTo(source)) {
            return true;
        }
        if (target instanceof Player) {
            player = (Player) target;
            if (player.getAbilities().invulnerable && !source.is(DamageTypeTags.BYPASSES_INVULNERABILITY)) {
                return true;
            }
        }
        if (source.is(DamageTypeTags.IS_FIRE) && target.hasEffect(MobEffects.FIRE_RESISTANCE)) {
            return true;
        }
        if (QiShieldHandler.isFullyNegatedByResistanceEffect(target, source)) {
            return true;
        }
        return target instanceof Player && TechniqueBonusHelper.fireImmune(player = (Player) target) && QiShieldHandler.isTechniqueFireImmuneDamage(source);
    }

    private static boolean isFullyNegatedByResistanceEffect(LivingEntity target, DamageSource source) {
        if (source.is(DamageTypeTags.BYPASSES_EFFECTS) || source.is(DamageTypeTags.BYPASSES_RESISTANCE)) {
            return false;
        }
        MobEffectInstance resistance = target.getEffect(MobEffects.DAMAGE_RESISTANCE);
        return resistance != null && resistance.getAmplifier() >= 4;
    }

    private static boolean isTechniqueFireImmuneDamage(DamageSource source) {
        return source.is(DamageTypes.IN_FIRE) || source.is(DamageTypes.ON_FIRE) || source.is(DamageTypes.LAVA) || source.is(DamageTypes.HOT_FLOOR) || source.is(DamageTypes.IN_WALL) || source.is(DamageTypes.UNATTRIBUTED_FIREBALL) || source.is(DamageTypes.FIREBALL) || source.is(DamageTypeTags.IS_FIRE);
    }

    private static boolean npcHasQiShield(WanderingCultivatorEntity npc) {
        return npc.getSpellIds().contains(Spell.QI_SHIELD.id()) || npc.getRealm().isCultivator();
    }

    private static void handleNpcAttack(LivingAttackEvent event, WanderingCultivatorEntity npc) {
        float incoming = event.getAmount();
        if (incoming <= 0.0f) {
            return;
        }
        DharmaBodyManifestationHandler.trigger(npc);
        if (QiShieldHandler.shouldSkipQiShieldForPreImmuneDamage((LivingEntity) npc, event.getSource())) {
            return;
        }
        if (QiShieldHandler.canUseNpcQiShield(npc)) {
            float maxAbsorb = QiShieldHandler.maxAbsorbableDamage(npc, incoming, npc.getRealm());
            if (maxAbsorb >= incoming - 1.0E-4f && npc.getCurrentQi() >= QiShieldHandler.qiNeededForDamage(npc, incoming)) {
                long qiNeeded = QiShieldHandler.qiNeededForDamage(npc, incoming);
                npc.deductQi(qiNeeded);
                event.setCanceled(true);
                QiShieldHandler.spawnNpcShieldFx(npc, incoming);
                QiShieldHandler.notifyNpcAttacker(npc, event.getSource());
                return;
            }
            if (maxAbsorb > 0.0f) {
                return;
            }
        }
    }

    private static void handleNpcHurt(LivingHurtEvent event, WanderingCultivatorEntity npc) {
        float incoming = event.getAmount();
        if (incoming <= 0.0f) {
            return;
        }
        DharmaBodyManifestationHandler.trigger(npc);
        if (QiShieldHandler.shouldSkipQiShieldForPreImmuneDamage((LivingEntity) npc, event.getSource())) {
            return;
        }
        if (QiShieldHandler.canUseNpcQiShield(npc)) {
            float maxAbsorb = QiShieldHandler.maxAbsorbableDamage(npc, incoming, npc.getRealm());
            long currentQi = npc.getCurrentQi();
            long qiToSpend = Math.min(currentQi, QiShieldHandler.qiNeededForDamage(npc, maxAbsorb));
            float absorbedDamage = QiShieldHandler.absorbedDamageFromQi(qiToSpend, maxAbsorb, QiShieldHandler.qiPerDamage(npc));
            if (absorbedDamage > 0.0f) {
                npc.deductQi(qiToSpend);
                QiShieldHandler.spawnNpcShieldFx(npc, absorbedDamage);
                incoming = Math.max(0.0f, incoming - absorbedDamage);
                event.setAmount(incoming);
                if (incoming <= 0.0f) {
                    event.setCanceled(true);
                    QiShieldHandler.notifyNpcAttacker(npc, event.getSource());
                    return;
                }
            }
        }
        QiShieldHandler.applyNpcDefenseAtHurt(event, npc, incoming);
    }

    private static void applyNpcDefenseAtHurt(LivingHurtEvent event, WanderingCultivatorEntity npc, float incoming) {
        float afterDef = QiShieldHandler.afterNpcDefense(npc, incoming);
        if (afterDef <= 0.0f) {
            event.setCanceled(true);
            QiShieldHandler.notifyNpcAttacker(npc, event.getSource());
            return;
        }
        event.setAmount(afterDef);
    }

    private static float afterNpcDefense(WanderingCultivatorEntity npc, float incoming) {
        float effectiveDefense = (float) ((double) npc.getBodyDefense() * RealmPressureHandler.bodyDefenseMultiplier((LivingEntity) npc) * DharmaBodyManifestationHandler.defenseMultiplier(npc));
        return Math.max(0.0f, incoming - effectiveDefense);
    }

    private static void notifyNpcAttacker(WanderingCultivatorEntity npc, DamageSource source) {
        LivingEntity le;
        if (source == null) {
            return;
        }
        Entity attacker = source.getEntity();
        if (attacker instanceof LivingEntity && (le = (LivingEntity) attacker) != npc) {
            npc.rememberCombatThreat(le);
        }
    }

    private static boolean canUseNpcQiShield(WanderingCultivatorEntity npc) {
        return QiShieldHandler.npcHasQiShield(npc) && !SpiritLockHandler.isEntityLocked((Entity) npc) && npc.getCurrentQi() > 0L;
    }

    private static void spawnNpcShieldFx(WanderingCultivatorEntity npc, float intensity) {
        Level level = npc.level();
        if (!(level instanceof ServerLevel)) {
            return;
        }
        ServerLevel sl = (ServerLevel) level;
        int count = Math.min(40, 8 + (int) intensity);
        sl.sendParticles((ParticleOptions) ParticleTypes.ENCHANT, npc.getX(), npc.getY() + (double) npc.getBbHeight() * 0.6, npc.getZ(), count, 0.4, 0.4, 0.4, 0.6);
        sl.sendParticles((ParticleOptions) ParticleTypes.ENCHANTED_HIT, npc.getX(), npc.getY() + (double) npc.getBbHeight() * 0.5, npc.getZ(), 4, 0.3, 0.3, 0.3, 0.1);
    }
}
