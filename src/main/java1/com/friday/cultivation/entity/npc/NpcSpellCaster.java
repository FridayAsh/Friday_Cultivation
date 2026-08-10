package com.friday.cultivation.entity.npc;

import com.friday.cultivation.physique.PhysiqueBonusHelper;
import com.friday.cultivation.spirit.QiElement;
import com.friday.cultivation.spirit.SpiritRoot;
import com.friday.cultivation.spirit.SpiritRootBonusHelper;
import com.friday.cultivation.spell.Spell;
import com.friday.cultivation.technique.Technique;
import com.friday.cultivation.entity.spell.BuddhaFireLotusEntity;
import com.friday.cultivation.entity.spell.GreatFireballEntity;
import com.friday.cultivation.entity.spell.HeavenPiercingConeEntity;
import com.friday.cultivation.entity.spell.MeteorEntity;
import com.friday.cultivation.entity.spell.MushroomCloudEntity;
import com.friday.cultivation.entity.spell.ShockwaveEntity;
import com.friday.cultivation.entity.spell.SkySplittingSwordAuraEntity;
import com.friday.cultivation.entity.spell.StoneBulletEntity;
import com.friday.cultivation.entity.spell.SwordProjectileEntity;
import com.friday.cultivation.entity.spell.XiaoxiangFireballEntity;
import com.friday.cultivation.event.DharmaBodyManifestationHandler;
import com.friday.cultivation.event.LifeBalanceHandler;
import com.friday.cultivation.event.PalmThunderHandler;
import com.friday.cultivation.event.SectCombatHandler;
import com.friday.cultivation.event.SoulHookHandler;
import com.friday.cultivation.event.SoulStateHandler;
import com.friday.cultivation.event.SpiritLockHandler;
import com.friday.cultivation.event.TimeStasisHandler;
import com.friday.cultivation.item.weapon.TieredWeapon;
import com.friday.cultivation.registry.ModEffects;
import com.friday.cultivation.util.SpellDamageSourceHelper;
import com.friday.cultivation.util.SpellLightningHelper;
import com.friday.cultivation.util.SpellScalingHelper;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Arrow;
import net.minecraft.world.entity.projectile.Snowball;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.Set;

/**
 * NPC 法术施放器 - 严格 1:1 复刻原模组 NpcSpellCaster。
 */
public final class NpcSpellCaster {
    public static final Set<Spell> SUPPORTED = EnumSet.of(Spell.ICE_LANCE, Spell.LIGHTNING_BOLT, Spell.PALM_THUNDER, Spell.WIND_BLADE, Spell.POISON_MIST, Spell.WITHER_TOUCH, Spell.ARROW_VOLLEY, Spell.EARTH_SPIKE, Spell.STONE_BULLET, Spell.HEAVEN_PIERCING_CONE, Spell.SUN_FLARE, Spell.FLYING_SWORD, Spell.SWORD_CONVERGENCE, Spell.SKY_SPLITTING_SWORD_AURA, Spell.STAR_FALL, Spell.SOARING, Spell.SPEED_BURST, Spell.INVISIBILITY, Spell.HEALING_TOUCH, Spell.IRON_BODY, Spell.WATER_AFFINITY, Spell.NIGHT_EYE, Spell.FIRE_PROTECTION, Spell.CLEAR_MIND, Spell.QI_TRANSFER, Spell.TRUTH_SIGHT_EYE, Spell.DIVINE_SENSE, Spell.VOID_ESCAPE, Spell.TIME_STASIS, Spell.TAISHANG_LIFE_BALANCE, Spell.BUDDHA_FIRE_LOTUS, Spell.SPIRIT_LOCK, Spell.SPIRIT_UNLOCK, Spell.SOUL_HOOK);
    public static final Set<Spell> NON_COMBAT = EnumSet.of(Spell.QI_TRANSFER, Spell.SPIRIT_UNLOCK);
    public static final Set<Spell> PASSIVE_COMBAT = EnumSet.of(Spell.SWORD_AURA, Spell.REALM_PRESSURE);
    public static final Set<Spell> REQUIRES_SWORD = EnumSet.of(Spell.FLYING_SWORD, Spell.SWORD_CONVERGENCE, Spell.SKY_SPLITTING_SWORD_AURA, Spell.SWORD_AURA);
    public static final Set<Spell> CHARGEABLE_SUPPORTED = EnumSet.of(Spell.GREAT_FIREBALL, Spell.SWORD_CONVERGENCE, Spell.SKY_SPLITTING_SWORD_AURA, Spell.STAR_FALL, Spell.HEAVEN_PIERCING_CONE, Spell.BUDDHA_FIRE_LOTUS);
    public static final Set<Spell> HIGH_IMPACT_MEGA = EnumSet.of(Spell.GREAT_FIREBALL, Spell.SWORD_CONVERGENCE, Spell.SKY_SPLITTING_SWORD_AURA, Spell.STAR_FALL, Spell.HEAVEN_PIERCING_CONE, Spell.BUDDHA_FIRE_LOTUS);
    public static final Set<Spell> SELF_BUFF = EnumSet.of(Spell.SOARING, Spell.SPEED_BURST, Spell.INVISIBILITY, Spell.HEALING_TOUCH, Spell.IRON_BODY, Spell.WATER_AFFINITY, Spell.NIGHT_EYE, Spell.FIRE_PROTECTION, Spell.CLEAR_MIND, Spell.DIVINE_SENSE, Spell.VOID_ESCAPE);

    private NpcSpellCaster() {
    }

    public static boolean isLearnableByNpc(Spell spell) {
        return spell != null && spell != Spell.IMMORTAL_INCANTATION && spell != Spell.GHOST_FLIGHT && spell != Spell.QI_MENDING;
    }

    public static boolean isCombatSpell(Spell spell) {
        return spell != null && SUPPORTED.contains(spell) && !SELF_BUFF.contains(spell) && !NON_COMBAT.contains(spell);
    }

    public static boolean isHighImpactMega(Spell spell) {
        return spell != null && HIGH_IMPACT_MEGA.contains(spell);
    }

    public static boolean hasNearbyHighImpactSpell(WanderingCultivatorEntity npc, double range) {
        Level level = npc.level();
        if (!(level instanceof ServerLevel)) {
            return false;
        }
        ServerLevel level2 = (ServerLevel) level;
        AABB box = npc.getBoundingBox().inflate(range);
        return !level2.getEntitiesOfClass(MeteorEntity.class, box, MeteorEntity::isMega).isEmpty() || !level2.getEntitiesOfClass(SkySplittingSwordAuraEntity.class, box, SkySplittingSwordAuraEntity::isMega).isEmpty() || !level2.getEntitiesOfClass(ShockwaveEntity.class, box, entity -> true).isEmpty() || !level2.getEntitiesOfClass(MushroomCloudEntity.class, box, entity -> true).isEmpty() || !level2.getEntitiesOfClass(GreatFireballEntity.class, box, entity -> true).isEmpty() || !level2.getEntitiesOfClass(HeavenPiercingConeEntity.class, box, entity -> true).isEmpty() || !level2.getEntitiesOfClass(BuddhaFireLotusEntity.class, box, entity -> true).isEmpty();
    }

    public static boolean isPassiveCombatSpell(Spell spell) {
        return spell != null && PASSIVE_COMBAT.contains(spell);
    }

    public static boolean hasCombatSpell(WanderingCultivatorEntity npc) {
        if (npc.isNpcSoulState()) {
            return false;
        }
        for (String id : npc.getSpellIds()) {
            Spell spell = Spell.byId(id);
            if (!NpcSpellCaster.isCombatSpell(spell) && !NpcSpellCaster.isPassiveCombatSpell(spell)) continue;
            return true;
        }
        return false;
    }

    public static boolean knows(WanderingCultivatorEntity npc, Spell spell) {
        return spell != null && npc.getSpellIds().contains(spell.id());
    }

    private static boolean hasSwordLikeWeapon(WanderingCultivatorEntity npc) {
        Item item = npc.getMainHandItem().getItem();
        return item instanceof SwordItem || item instanceof TieredWeapon && ((TieredWeapon) item).isSwordWeapon();
    }

    public static boolean trySelfRescue(WanderingCultivatorEntity npc) {
        if (npc.isNpcSoulState()) {
            return false;
        }
        if (SoulHookHandler.isActionLocked(npc)) {
            return false;
        }
        Level level = npc.level();
        if (!(level instanceof ServerLevel)) {
            return false;
        }
        ServerLevel level2 = (ServerLevel) level;
        if (SpiritLockHandler.isEntityLocked(npc) && NpcSpellCaster.knows(npc, Spell.SPIRIT_UNLOCK) && npc.getCurrentQi() >= NpcSpellCaster.spellCost(npc, Spell.SPIRIT_UNLOCK) && SpiritLockHandler.unlockEntity(npc)) {
            npc.deductQi(NpcSpellCaster.spellCost(npc, Spell.SPIRIT_UNLOCK));
            level2.playSound(null, npc.getX(), npc.getY(), npc.getZ(), SoundEvents.CHAIN_BREAK, SoundSource.HOSTILE, 0.85f, 1.35f);
            level2.sendParticles(ParticleTypes.ENCHANT, npc.getX(), npc.getY() + (double) npc.getBbHeight() * 0.55, npc.getZ(), 18, 0.35, 0.45, 0.35, 0.08);
            return true;
        }
        if (!SpiritLockHandler.isEntityLocked(npc) && TimeStasisHandler.isEntityStopped(npc) && NpcSpellCaster.knows(npc, Spell.TIME_STASIS) && npc.getCurrentQi() >= NpcSpellCaster.spellCost(npc, Spell.TIME_STASIS) && TimeStasisHandler.releaseStoppedEntity(npc)) {
            npc.deductQi(NpcSpellCaster.spellCost(npc, Spell.TIME_STASIS));
            level2.playSound(null, npc.getX(), npc.getY(), npc.getZ(), SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.HOSTILE, 0.85f, 1.55f);
            return true;
        }
        return false;
    }

    public static boolean cast(WanderingCultivatorEntity npc, Spell sp, LivingEntity target) {
        if (npc.isNpcSoulState()) {
            return false;
        }
        if (npc.isTradingFreeze()) {
            return false;
        }
        if (SoulHookHandler.isActionLocked(npc)) {
            return false;
        }
        if (!SUPPORTED.contains(sp)) {
            return false;
        }
        if (SpiritLockHandler.isEntityLocked(npc) && sp != Spell.SPIRIT_UNLOCK) {
            return false;
        }
        if (npc.hasEffect(ModEffects.MERIDIAN_FROZEN.get()) && sp != Spell.SPIRIT_UNLOCK) {
            return false;
        }
        if (sp == Spell.BUDDHA_FIRE_LOTUS && !NpcSpellCaster.canNpcCastBuddhaFireLotus(npc)) {
            return false;
        }
        if (REQUIRES_SWORD.contains(sp) && !NpcSpellCaster.hasSwordLikeWeapon(npc)) {
            return false;
        }
        long cost = NpcSpellCaster.spellCost(npc, sp);
        if (npc.getCurrentQi() < cost) {
            return false;
        }
        Level level = npc.level();
        if (!(level instanceof ServerLevel)) {
            return false;
        }
        ServerLevel level2 = (ServerLevel) level;
        boolean isAttack = NpcSpellCaster.isCombatSpell(sp);
        if (isAttack && (target == null || !target.isAlive())) {
            return false;
        }
        if (sp == Spell.SOUL_HOOK) {
            if (!SoulStateHandler.canSoulHookTarget(target)) {
                return false;
            }
            if (!SectCombatHandler.canApplyOffensiveEffect(npc, target)) {
                return false;
            }
        } else if (!(!isAttack || SoulStateHandler.canOrdinaryAffect(npc, target) && SectCombatHandler.canApplyOffensiveEffect(npc, target))) {
            return false;
        }
        if (target != null) {
            npc.getLookControl().setLookAt(target, 30.0f, 30.0f);
        }
        Vec3 eye = npc.getEyePosition();
        Vec3 look = target != null ? target.getEyePosition().subtract(eye).normalize() : npc.getLookAngle();
        DharmaBodyManifestationHandler.trigger(npc);
        cost = NpcSpellCaster.spellCost(npc, sp);
        NpcSpellCaster.dispatch(npc, sp, target, level2, eye, look);
        npc.deductQi(cost);
        if (sp != Spell.SOUL_HOOK) {
            level2.playSound(null, npc.getX(), npc.getY(), npc.getZ(), SoundEvents.BLAZE_SHOOT, SoundSource.HOSTILE, 0.6f, 1.4f);
        }
        if (sp != Spell.PALM_THUNDER && sp != Spell.SOUL_HOOK) {
            NpcSpellCaster.spawnCastFx(npc, level2, look);
        }
        return true;
    }

    public static boolean castMega(WanderingCultivatorEntity npc, Spell sp, LivingEntity target) {
        if (npc.isNpcSoulState()) {
            return false;
        }
        if (npc.isTradingFreeze()) {
            return false;
        }
        if (SoulHookHandler.isActionLocked(npc)) {
            return false;
        }
        if (!CHARGEABLE_SUPPORTED.contains(sp)) {
            return false;
        }
        if (SpiritLockHandler.isEntityLocked(npc)) {
            return false;
        }
        if (npc.hasEffect(ModEffects.MERIDIAN_FROZEN.get())) {
            return false;
        }
        if (sp == Spell.BUDDHA_FIRE_LOTUS && !NpcSpellCaster.canNpcCastBuddhaFireLotus(npc)) {
            return false;
        }
        if (!sp.chargeable()) {
            return false;
        }
        if (NpcSpellCaster.isHighImpactMega(sp) && NpcSpellCaster.hasNearbyHighImpactSpell(npc, 160.0)) {
            return false;
        }
        long cost = NpcSpellCaster.megaCost(npc, sp);
        if (npc.getCurrentQi() < cost) {
            return false;
        }
        Level level = npc.level();
        if (!(level instanceof ServerLevel)) {
            return false;
        }
        ServerLevel level2 = (ServerLevel) level;
        if (target == null || !target.isAlive()) {
            return false;
        }
        if (!SoulStateHandler.canOrdinaryAffect(npc, target) || !SectCombatHandler.canApplyOffensiveEffect(npc, target)) {
            return false;
        }
        if (REQUIRES_SWORD.contains(sp) && !NpcSpellCaster.hasSwordLikeWeapon(npc)) {
            return false;
        }
        npc.getLookControl().setLookAt(target, 30.0f, 30.0f);
        Vec3 eye = npc.getEyePosition();
        Vec3 look = target.getEyePosition().subtract(eye).normalize();
        DharmaBodyManifestationHandler.trigger(npc);
        cost = NpcSpellCaster.megaCost(npc, sp);
        NpcSpellCaster.dispatchMega(npc, sp, target, level2, eye, look);
        npc.deductQi(cost);
        level2.playSound(null, npc.getX(), npc.getY(), npc.getZ(), SoundEvents.WITHER_SPAWN, SoundSource.HOSTILE, 0.6f, 1.6f);
        NpcSpellCaster.spawnMegaCastFx(npc, level2, look);
        return true;
    }

    public static long megaCost(Spell sp) {
        return switch (sp) {
            case GREAT_FIREBALL -> 3000L;
            case SWORD_CONVERGENCE -> 3000L;
            case SKY_SPLITTING_SWORD_AURA -> 3000L;
            case STAR_FALL -> 10000L;
            case HEAVEN_PIERCING_CONE -> 6000L;
            case BUDDHA_FIRE_LOTUS -> 12000L;
            default -> sp.qiCost();
        };
    }

    public static long megaCost(WanderingCultivatorEntity npc, Spell sp) {
        return NpcSpellCaster.applyNpcQiCostMultipliers(npc, sp, NpcSpellCaster.megaCost(sp));
    }

    public static long spellCost(WanderingCultivatorEntity npc, Spell sp) {
        return NpcSpellCaster.applyNpcQiCostMultipliers(npc, sp, sp == null ? 0L : (long) sp.qiCost());
    }

    public static long generalQiCost(WanderingCultivatorEntity npc, long baseCost) {
        return NpcSpellCaster.applyNpcQiCostMultipliers(npc, null, baseCost);
    }

    public static double generalQiCostMultiplier(WanderingCultivatorEntity npc) {
        if (npc == null) {
            return 1.0;
        }
        double multiplier = 1.0;
        if (Technique.IMMORTAL_INCANTATION.id().equals(npc.getTechniqueId())) {
            multiplier *= 0.5;
        }
        return Double.isFinite(multiplier *= PhysiqueBonusHelper.generalQiCostMultiplier(npc.getPhysique())) ? Math.max(0.0, multiplier) : 1.0;
    }

    private static long applyNpcQiCostMultipliers(WanderingCultivatorEntity npc, Spell spell, long baseCost) {
        if (baseCost <= 0L) {
            return 0L;
        }
        if (npc == null) {
            return baseCost;
        }
        double multiplier = NpcSpellCaster.generalQiCostMultiplier(npc);
        multiplier *= SpiritRootBonusHelper.spellQiCostMultiplier(npc.getSpiritRoot(), spell);
        multiplier *= PhysiqueBonusHelper.spellQiCostMultiplier(npc.getPhysique(), spell);
        if (spell != null) {
            multiplier *= DharmaBodyManifestationHandler.spellQiCostMultiplier(npc);
        }
        if (!Double.isFinite(multiplier)) {
            multiplier = 1.0;
        }
        return Math.max(1L, (long) Math.ceil((double) baseCost * Math.max(0.0, multiplier)));
    }

    private static void dispatchMega(WanderingCultivatorEntity npc, Spell sp, LivingEntity target, ServerLevel level, Vec3 eye, Vec3 look) {
        switch (sp) {
            case GREAT_FIREBALL: {
                GreatFireballEntity ball = new GreatFireballEntity(level, npc, look.x, look.y, look.z, 3000);
                ball.setDamageMultiplier(SpellScalingHelper.damageMultiplier(npc, sp));
                ball.setPos(npc.getX() + look.x * 2.0, npc.getEyeY() + look.y * 0.5, npc.getZ() + look.z * 2.0);
                level.addFreshEntity(ball);
                break;
            }
            case SWORD_CONVERGENCE: {
                Vec3 baseSpawn = new Vec3(npc.getX(), npc.getEyeY(), npc.getZ());
                for (int i = 0; i < 8; ++i) {
                    double angleOffset = ((double) i - 3.5) * 0.1;
                    Vec3 spread = look.yRot((float) angleOffset);
                    double yJitter = 0.2 + (double) (i % 3) * 0.25;
                    Vec3 spawnPos = baseSpawn.add(spread.x * 1.2, yJitter, spread.z * 1.2);
                    SwordProjectileEntity sword = new SwordProjectileEntity(level, npc, spawnPos, target.getEyePosition(), true, false);
                    sword.setDirectHitDamage(SwordProjectileEntity.scaledConvergenceDamage(sp.damage(), SpellScalingHelper.damageMultiplier(npc, sp)));
                    level.addFreshEntity(sword);
                }
                break;
            }
            case SKY_SPLITTING_SWORD_AURA: {
                Vec3 spawnPos = npc.getEyePosition().add(look.scale(2.0));
                SkySplittingSwordAuraEntity aura = new SkySplittingSwordAuraEntity(level, npc, spawnPos, look, true);
                aura.setDamage(SpellScalingHelper.scaledDamageFloat(npc, sp, 2000.0f));
                level.addFreshEntity(aura);
                break;
            }
            case STAR_FALL: {
                Vec3 targetPos = target.position();
                Vec3 spawnPos = new Vec3(targetPos.x, targetPos.y + 80.0, targetPos.z);
                MeteorEntity meteor = new MeteorEntity(level, npc, spawnPos, targetPos, 2, 10.0f);
                meteor.setDamageMultiplier(SpellScalingHelper.damageMultiplier(npc, sp));
                level.addFreshEntity(meteor);
                break;
            }
            case HEAVEN_PIERCING_CONE: {
                int chargeTicks = 100;
                int stage = HeavenPiercingConeEntity.stageForChargeTicks(chargeTicks);
                double velocity = HeavenPiercingConeEntity.velocityForChargeTicks(chargeTicks);
                double baseDamage = (double) sp.damage() * Math.pow(velocity, 1.5);
                HeavenPiercingConeEntity cone = new HeavenPiercingConeEntity(level, npc);
                cone.configure((float) Math.max(1.0, SpellScalingHelper.scaledDamageDouble(npc, sp, baseDamage)), velocity, stage, chargeTicks, false);
                Vec3 spawnPos = HeavenPiercingConeEntity.safeSideLaunchPosition(level, npc, look);
                Vec3 targetPos = target == null ? npc.getEyePosition().add(look.scale(80.0)) : target.getEyePosition();
                Vec3 launchDir = HeavenPiercingConeEntity.aimDirectionFromSide(spawnPos, targetPos, look);
                cone.setPos(spawnPos.x, spawnPos.y, spawnPos.z);
                cone.setDeltaMovement(launchDir.scale(velocity));
                level.addFreshEntity(cone);
                break;
            }
            case BUDDHA_FIRE_LOTUS: {
                NpcSpellCaster.spawnNpcBuddhaFireLotus(npc, target, level, look, 12000, 1.2f);
                break;
            }
        }
    }

    private static void spawnMegaCastFx(WanderingCultivatorEntity npc, ServerLevel level, Vec3 look) {
        for (int i = 0; i < 30; ++i) {
            double a = (double) i * 0.41887902047863906;
            double r = 1.5;
            double dx = Math.cos(a) * r;
            double dz = Math.sin(a) * r;
            level.sendParticles(ParticleTypes.FLAME, npc.getX() + dx, npc.getY() + (double) npc.getBbHeight() * 0.6, npc.getZ() + dz, 1, 0.0, 0.05, 0.0, 0.08);
        }
        level.sendParticles(ParticleTypes.DRAGON_BREATH, npc.getX(), npc.getY() + (double) npc.getBbHeight() * 0.7, npc.getZ(), 10, 0.3, 0.3, 0.3, 0.05);
    }

    private static void spawnCastFx(WanderingCultivatorEntity npc, ServerLevel level, Vec3 look) {
        int i;
        for (i = 0; i < 12; ++i) {
            double a = (double) i * 0.5235987755982988;
            double r = 0.7;
            double dx = Math.cos(a) * r;
            double dz = Math.sin(a) * r;
            level.sendParticles(ParticleTypes.ENCHANT, npc.getX() + dx, npc.getY() + (double) npc.getBbHeight() * 0.55, npc.getZ() + dz, 1, 0.0, 0.05, 0.0, 0.06);
        }
        for (i = 1; i <= 6; ++i) {
            double t = (double) i * 0.3;
            level.sendParticles(ParticleTypes.PORTAL, npc.getX() + look.x * t, npc.getEyeY() + look.y * t, npc.getZ() + look.z * t, 2, 0.06, 0.06, 0.06, 0.0);
        }
    }

    private static int scaledDamage(WanderingCultivatorEntity npc, Spell sp) {
        return SpellScalingHelper.scaledDamage(npc, sp);
    }

    private static void dispatch(WanderingCultivatorEntity npc, Spell sp, LivingEntity target, ServerLevel level, Vec3 eye, Vec3 look) {
        switch (sp) {
            case FIREBALL: {
                XiaoxiangFireballEntity ball = new XiaoxiangFireballEntity(level, npc, look.x, look.y, look.z, 1);
                ball.setExtraDamage(NpcSpellCaster.scaledDamage(npc, sp));
                ball.setPos(npc.getX() + look.x, npc.getEyeY() + look.y * 0.5, npc.getZ() + look.z);
                level.addFreshEntity(ball);
                break;
            }
            case GREAT_FIREBALL: {
                GreatFireballEntity ball = new GreatFireballEntity(level, npc, look.x, look.y, look.z, sp.qiCost());
                ball.setDamageMultiplier(SpellScalingHelper.damageMultiplier(npc, sp));
                ball.setPos(npc.getX() + look.x * 1.5, npc.getEyeY() + look.y * 0.5, npc.getZ() + look.z * 1.5);
                level.addFreshEntity(ball);
                break;
            }
            case ICE_LANCE: {
                Snowball ball = new Snowball(level, npc);
                ball.shootFromRotation(npc, npc.getXRot(), npc.getYRot(), 0.0f, 1.6f, 0.0f);
                level.addFreshEntity(ball);
                if (target == null || !((double) target.distanceTo(npc) <= 20.0) || !SoulStateHandler.canOrdinaryAffect(npc, target) || !SectCombatHandler.canApplyOffensiveEffect(npc, target)) break;
                target.hurt(SpellDamageSourceHelper.directSpell(npc), (float) NpcSpellCaster.scaledDamage(npc, sp));
                target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 60, 1));
                break;
            }
            case LIGHTNING_BOLT: {
                Vec3 pos = target != null ? target.position() : eye.add(look.scale(20.0));
                SpellLightningHelper.strike(level, npc, pos, SpellScalingHelper.scaledDamageFloat(npc, sp, 5.0f));
                break;
            }
            case PALM_THUNDER: {
                PalmThunderHandler.spawnProjectile(npc, look, SpellScalingHelper.scaledDamageFloat(npc, sp, sp.damage()));
                break;
            }
            case WIND_BLADE: {
                AABB box = npc.getBoundingBox().inflate(5.0);
                for (LivingEntity t : level.getEntitiesOfClass(LivingEntity.class, box, e -> e != npc && !NpcSpellCaster.isFriendlyToNpc(e))) {
                    Vec3 dir;
                    if (!SoulStateHandler.canOrdinaryAffect(npc, t) || !SectCombatHandler.canApplyOffensiveEffect(npc, t) || (dir = t.position().subtract(npc.position()).normalize()).dot(look) < 0.3) continue;
                    t.hurt(SpellDamageSourceHelper.directSpell(npc), (float) NpcSpellCaster.scaledDamage(npc, sp));
                    t.knockback(1.5, -dir.x, -dir.z);
                }
                break;
            }
            case POISON_MIST: {
                AABB box = npc.getBoundingBox().inflate(4.0);
                for (LivingEntity t : level.getEntitiesOfClass(LivingEntity.class, box, e -> e != npc && !NpcSpellCaster.isFriendlyToNpc(e))) {
                    if (!SoulStateHandler.canOrdinaryAffect(npc, t) || !SectCombatHandler.canApplyOffensiveEffect(npc, t)) continue;
                    t.addEffect(new MobEffectInstance(MobEffects.POISON, 100, 1));
                }
                break;
            }
            case WITHER_TOUCH: {
                AABB box = npc.getBoundingBox().inflate(3.0);
                for (LivingEntity t : level.getEntitiesOfClass(LivingEntity.class, box, e -> e != npc && !NpcSpellCaster.isFriendlyToNpc(e))) {
                    if (!SoulStateHandler.canOrdinaryAffect(npc, t) || !SectCombatHandler.canApplyOffensiveEffect(npc, t)) continue;
                    t.addEffect(new MobEffectInstance(MobEffects.WITHER, 80, 0));
                }
                break;
            }
            case ARROW_VOLLEY: {
                for (int i = -1; i <= 1; ++i) {
                    Arrow arrow = new Arrow(level, npc);
                    Vec3 spread = look.add((double) i * 0.05, 0.0, (double) i * 0.05);
                    arrow.shoot(spread.x, spread.y, spread.z, 2.5f, 1.0f);
                    arrow.setBaseDamage(SpellScalingHelper.scaledDamageDouble(npc, sp, Math.max(4.0, arrow.getBaseDamage())));
                    level.addFreshEntity(arrow);
                }
                break;
            }
            case EARTH_SPIKE: {
                Vec3 p = target != null ? target.position() : eye.add(look.scale(15.0));
                float power = (float) Math.max(0.5, Math.min(6.0, 2.5 * Math.sqrt(SpellScalingHelper.damageMultiplier(npc, sp))));
                level.explode(npc, p.x, p.y, p.z, power, Level.ExplosionInteraction.NONE);
                break;
            }
            case STONE_BULLET: {
                StoneBulletEntity bullet = new StoneBulletEntity(level, npc);
                bullet.setDamage(NpcSpellCaster.scaledDamage(npc, sp));
                bullet.setPos(npc.getX() + look.x * 0.8, npc.getY() - 0.05 + look.y * 0.4, npc.getZ() + look.z * 0.8);
                bullet.shoot(look.x, look.y, look.z, 2.2f, 0.15f);
                level.addFreshEntity(bullet);
                break;
            }
            case HEAVEN_PIERCING_CONE: {
                HeavenPiercingConeEntity cone = new HeavenPiercingConeEntity(level, npc);
                cone.configure(NpcSpellCaster.scaledDamage(npc, sp), 3.4, 1, 0, true);
                Vec3 spawnPos = HeavenPiercingConeEntity.safeSideLaunchPosition(level, npc, look);
                Vec3 targetPos = target == null ? npc.getEyePosition().add(look.scale(80.0)) : target.getEyePosition();
                Vec3 launchDir = HeavenPiercingConeEntity.aimDirectionFromSide(spawnPos, targetPos, look);
                cone.setPos(spawnPos.x, spawnPos.y, spawnPos.z);
                cone.setDeltaMovement(launchDir.scale(3.4));
                level.addFreshEntity(cone);
                break;
            }
            case SUN_FLARE: {
                AABB box = npc.getBoundingBox().inflate(8.0);
                for (LivingEntity t : level.getEntitiesOfClass(LivingEntity.class, box, e -> e != npc && !NpcSpellCaster.isFriendlyToNpc(e))) {
                    if (!SoulStateHandler.canOrdinaryAffect(npc, t) || !SectCombatHandler.canApplyOffensiveEffect(npc, t)) continue;
                    t.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 80, 0));
                }
                break;
            }
            case FLYING_SWORD: {
                Vec3 spawnPos = new Vec3(npc.getX() + look.x * 1.0, npc.getEyeY() - 0.1, npc.getZ() + look.z * 1.0);
                Vec3 targetPos = target != null ? target.getEyePosition() : eye.add(look.scale(50.0));
                SwordProjectileEntity sword = new SwordProjectileEntity(level, npc, spawnPos, targetPos, false, true);
                sword.setDirectHitDamage(NpcSpellCaster.scaledDamage(npc, sp));
                level.addFreshEntity(sword);
                break;
            }
            case SWORD_CONVERGENCE: {
                Vec3 baseSpawn = new Vec3(npc.getX(), npc.getEyeY(), npc.getZ());
                for (int i = 0; i < 3; ++i) {
                    double angleOffset = (double) (i - 1) * 0.15;
                    Vec3 spread = look.yRot((float) angleOffset);
                    Vec3 sp2 = baseSpawn.add(spread.x * 1.0, 0.3, spread.z * 1.0);
                    Vec3 targetPos = target != null ? target.getEyePosition() : eye.add(spread.scale(30.0));
                    SwordProjectileEntity sword = new SwordProjectileEntity(level, npc, sp2, targetPos, false, false);
                    sword.setDirectHitDamage(SwordProjectileEntity.scaledConvergenceDamage(sp.damage(), SpellScalingHelper.damageMultiplier(npc, sp)));
                    level.addFreshEntity(sword);
                }
                break;
            }
            case SKY_SPLITTING_SWORD_AURA: {
                Vec3 spawnPos = npc.getEyePosition().add(look.scale(1.5));
                SkySplittingSwordAuraEntity aura = new SkySplittingSwordAuraEntity(level, npc, spawnPos, look, false);
                aura.setDamage(SpellScalingHelper.scaledDamageFloat(npc, sp, 1000.0f));
                level.addFreshEntity(aura);
                break;
            }
            case STAR_FALL: {
                if (target == null) {
                    return;
                }
                Vec3 targetPos = target.position();
                Vec3 spawnPos = new Vec3(npc.getX(), npc.getY() + 60.0, npc.getZ());
                MeteorEntity meteor = new MeteorEntity(level, npc, spawnPos, targetPos, 0, 2.0f);
                meteor.setDamageMultiplier(SpellScalingHelper.damageMultiplier(npc, sp));
                level.addFreshEntity(meteor);
                break;
            }
            case BUDDHA_FIRE_LOTUS: {
                if (target == null) {
                    return;
                }
                NpcSpellCaster.spawnNpcBuddhaFireLotus(npc, target, level, look, 10000, 1.0f);
                break;
            }
            case SOARING: {
                npc.addEffect(new MobEffectInstance(MobEffects.LEVITATION, 60, 1));
                break;
            }
            case SPEED_BURST: {
                npc.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 200, 1));
                break;
            }
            case INVISIBILITY: {
                npc.addEffect(new MobEffectInstance(MobEffects.INVISIBILITY, 200, 0));
                break;
            }
            case HEALING_TOUCH: {
                npc.heal(SpellScalingHelper.scaledDamageFloat(npc, sp, 6.0f));
                break;
            }
            case IRON_BODY: {
                npc.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 200, 1));
                break;
            }
            case WATER_AFFINITY: {
                npc.addEffect(new MobEffectInstance(MobEffects.WATER_BREATHING, 600, 0));
                break;
            }
            case NIGHT_EYE: {
                npc.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, 1200, 0));
                break;
            }
            case FIRE_PROTECTION: {
                npc.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 400, 0));
                break;
            }
            case CLEAR_MIND: {
                ArrayList<MobEffect> harmful = new ArrayList<MobEffect>();
                for (MobEffectInstance eff : npc.getActiveEffects()) {
                    if (eff.getEffect().getCategory() != MobEffectCategory.HARMFUL) continue;
                    harmful.add(eff.getEffect());
                }
                for (MobEffect e2 : harmful) {
                    npc.removeEffect(e2);
                }
                npc.setRemainingFireTicks(0);
                break;
            }
            case TRUTH_SIGHT_EYE: {
                npc.removeEffect(MobEffects.BLINDNESS);
                break;
            }
            case DIVINE_SENSE: {
                npc.removeEffect(MobEffects.BLINDNESS);
                break;
            }
            case VOID_ESCAPE: {
                npc.addEffect(new MobEffectInstance(MobEffects.INVISIBILITY, 160, 0, false, false, true));
                npc.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 160, 1, false, false, true));
                npc.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 80, 0, false, false, true));
                break;
            }
            case TIME_STASIS: {
                if (target == null || !SoulStateHandler.canOrdinaryAffect(npc, target) || !SectCombatHandler.canApplyOffensiveEffect(npc, target)) break;
                TimeStasisHandler.stopEntity(target, 600);
                break;
            }
            case TAISHANG_LIFE_BALANCE: {
                if (target == null || !SoulStateHandler.canOrdinaryAffect(npc, target) || !SectCombatHandler.canApplyOffensiveEffect(npc, target)) break;
                LifeBalanceHandler.castMark(npc, target);
                break;
            }
            case SOUL_HOOK: {
                if (target == null) break;
                SoulHookHandler.start(npc, target);
                break;
            }
            case SPIRIT_LOCK: {
                if (target == null || !SoulStateHandler.canOrdinaryAffect(npc, target) || !SectCombatHandler.canApplyOffensiveEffect(npc, target)) break;
                SpiritLockHandler.lockEntity(target);
                break;
            }
            case SPIRIT_UNLOCK: {
                LivingEntity unlockTarget = SpiritLockHandler.isEntityLocked(npc) ? npc : target;
                if (unlockTarget == null) break;
                SpiritLockHandler.unlockEntity(unlockTarget);
                break;
            }
        }
    }

    private static boolean isFriendlyToNpc(LivingEntity other) {
        return other instanceof WanderingCultivatorEntity;
    }

    private static boolean canNpcCastBuddhaFireLotus(WanderingCultivatorEntity npc) {
        return true;
    }

    private static int buddhaFireLotusRootFlags(WanderingCultivatorEntity npc) {
        int flags = 0;
        SpiritRoot root = npc.getSpiritRoot();
        if (SpiritRootBonusHelper.hasRootElement(root, QiElement.METAL)) {
            flags |= 1;
        }
        if (SpiritRootBonusHelper.hasRootElement(root, QiElement.WOOD)) {
            flags |= 2;
        }
        if (SpiritRootBonusHelper.hasRootElement(root, QiElement.WATER)) {
            flags |= 4;
        }
        if (SpiritRootBonusHelper.hasRootElement(root, QiElement.FIRE)) {
            flags |= 8;
        }
        if (SpiritRootBonusHelper.hasRootElement(root, QiElement.EARTH)) {
            flags |= 0x10;
        }
        if (SpiritRootBonusHelper.hasPureRoot(root)) {
            flags |= 0x20;
        }
        return flags;
    }

    private static void spawnNpcBuddhaFireLotus(WanderingCultivatorEntity npc, LivingEntity target, ServerLevel level, Vec3 look, int chargedQi, float chargeMultiplier) {
        float damage = SpellScalingHelper.scaledDamageFloat(npc, Spell.BUDDHA_FIRE_LOTUS, (float) Spell.BUDDHA_FIRE_LOTUS.damage() * chargeMultiplier);
        float radius = chargeMultiplier >= 1.2f ? 40.0f : 32.0f;
        BuddhaFireLotusEntity lotus = new BuddhaFireLotusEntity(level, npc);
        lotus.configure(damage, radius, chargedQi, NpcSpellCaster.buddhaFireLotusRootFlags(npc), target);
        Vec3 launchLook = look.lengthSqr() < 1.0E-6 ? npc.getLookAngle() : look.normalize();
        Vec3 spawnPos = npc.getEyePosition().add(launchLook.scale(1.2)).add(0.0, -0.12, 0.0);
        lotus.setPos(spawnPos.x, spawnPos.y, spawnPos.z);
        Vec3 launch = target.getEyePosition().subtract(spawnPos);
        if (launch.lengthSqr() < 1.0E-6) {
            launch = launchLook;
        }
        lotus.setDeltaMovement(launch.normalize().scale(0.68));
        level.addFreshEntity(lotus);
    }
}
