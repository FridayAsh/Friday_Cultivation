/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.network.FriendlyByteBuf
 *  net.minecraft.network.chat.Component
 *  net.minecraft.server.level.ServerLevel
 *  net.minecraft.server.level.ServerPlayer
 *  net.minecraft.sounds.SoundEvents
 *  net.minecraft.sounds.SoundSource
 *  net.minecraft.world.effect.MobEffect
 *  net.minecraft.world.effect.MobEffectCategory
 *  net.minecraft.world.effect.MobEffectInstance
 *  net.minecraft.world.effect.MobEffects
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.entity.projectile.Arrow
 *  net.minecraft.world.entity.projectile.Snowball
 *  net.minecraft.world.item.SwordItem
 *  net.minecraft.world.level.ClipContext
 *  net.minecraft.world.level.ClipContext$Block
 *  net.minecraft.world.level.ClipContext$Fluid
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.level.Level$ExplosionInteraction
 *  net.minecraft.world.phys.AABB
 *  net.minecraft.world.phys.BlockHitResult
 *  net.minecraft.world.phys.HitResult$Type
 *  net.minecraft.world.phys.Vec3
 *  net.minecraftforge.network.NetworkEvent$Context
 */
package com.friday.cultivation.network;

import com.friday.cultivation.cultivation.CultivationCapability;
import com.friday.cultivation.cultivation.PhysiqueBonusHelper;
import com.friday.cultivation.cultivation.spell.Spell;
import com.friday.cultivation.cultivation.spell.SpellType;
import com.friday.cultivation.cultivation.technique.TechniqueBonusHelper;
import com.friday.cultivation.entity.GreatFireballEntity;
import com.friday.cultivation.entity.HeavenPiercingConeEntity;
import com.friday.cultivation.entity.MeteorEntity;
import com.friday.cultivation.entity.SkySplittingSwordAuraEntity;
import com.friday.cultivation.entity.StoneBulletEntity;
import com.friday.cultivation.entity.SwordProjectileEntity;
import com.friday.cultivation.entity.XiaoxiangFireballEntity;
import com.friday.cultivation.event.CapabilityEvents;
import com.friday.cultivation.event.CoreSelfDestructHandler;
import com.friday.cultivation.event.DharmaBodyManifestationHandler;
import com.friday.cultivation.event.DivineSenseHandler;
import com.friday.cultivation.event.LifeBalanceHandler;
import com.friday.cultivation.event.NascentSoulOutOfBodyHandler;
import com.friday.cultivation.event.PalmThunderHandler;
import com.friday.cultivation.event.RealmPressureHandler;
import com.friday.cultivation.event.SectCombatHandler;
import com.friday.cultivation.event.SoulHookHandler;
import com.friday.cultivation.event.SoulStateHandler;
import com.friday.cultivation.event.SpiritLockHandler;
import com.friday.cultivation.event.TimeStasisHandler;
import com.friday.cultivation.event.VoidEscapeHandler;
import com.friday.cultivation.item.weapon.SoulHookItem;
import com.friday.cultivation.network.ClientOnlyGlowPacket;
import com.friday.cultivation.registry.ModEffects;
import com.friday.cultivation.util.SpellDamageSourceHelper;
import com.friday.cultivation.util.SpellLightningHelper;
import com.friday.cultivation.util.SpellScalingHelper;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Arrow;
import net.minecraft.world.entity.projectile.Snowball;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkEvent;

public class CastSpellPacket {
    public static void encode(CastSpellPacket msg, FriendlyByteBuf buf) {
    }

    public static CastSpellPacket decode(FriendlyByteBuf buf) {
        return new CastSpellPacket();
    }

    public static void handle(CastSpellPacket msg, Supplier<NetworkEvent.Context> ctxSup) {
        NetworkEvent.Context ctx = ctxSup.get();
        ctx.enqueueWork(() -> {
            ServerPlayer player = ctx.getSender();
            if (player == null) {
                return;
            }
            if (SoulHookHandler.isActionLocked((Entity)player)) {
                player.displayClientMessage((Component)Component.translatable((String)"message.friday_cultivation.soul_hook_spell.action_locked"), true);
                return;
            }
            CultivationCapability.get((Player)player).ifPresent(data -> {
                if (data.isVoidEscapeActive()) {
                    VoidEscapeHandler.tryManualExitIfActive(player);
                    return;
                }
                String sid = data.getSelectedSpellId();
                if (sid.isEmpty()) {
                    player.displayClientMessage((Component)Component.translatable((String)"message.friday_cultivation.cast.no_selection"), true);
                    return;
                }
                Spell sp = Spell.byId(sid);
                if (sp == null || !data.hasSpell(sp)) {
                    player.displayClientMessage((Component)Component.translatable((String)"message.friday_cultivation.cast.no_selection"), true);
                    return;
                }
                if (!TimeStasisHandler.canPerformStoppedTimeAction(player, sp)) {
                    return;
                }
                if (data.isInTribulation() && sp == Spell.VOID_ESCAPE) {
                    player.displayClientMessage((Component)Component.translatable((String)"message.friday_cultivation.void_escape.blocked_tribulation"), true);
                    return;
                }
                if (data.isInTribulation() && sp == Spell.NASCENT_SOUL_OUT_OF_BODY) {
                    NascentSoulOutOfBodyHandler.stopIfActive(player, false);
                    CapabilityEvents.syncToClient(player);
                    player.displayClientMessage((Component)Component.translatable((String)"message.friday_cultivation.nascent_soul_out_of_body.blocked"), true);
                    return;
                }
                if (NascentSoulOutOfBodyHandler.isActive(player) && sp != Spell.NASCENT_SOUL_OUT_OF_BODY) {
                    player.displayClientMessage((Component)Component.translatable((String)"message.friday_cultivation.nascent_soul_out_of_body.blocked"), true);
                    return;
                }
                if (player.hasEffect((MobEffect)ModEffects.MERIDIAN_FROZEN.get())) {
                    player.displayClientMessage((Component)Component.translatable((String)"message.friday_cultivation.meridian_frozen.caster_locked"), true);
                    return;
                }
                if (SpiritLockHandler.isEntityLocked((Entity)player) && SpiritLockHandler.tryCastSelfUnlock(player, data)) {
                    return;
                }
                if (SpiritLockHandler.isEntityLocked((Entity)player) && sp != Spell.SPIRIT_UNLOCK) {
                    player.displayClientMessage((Component)Component.translatable((String)"message.friday_cultivation.spirit_lock.caster_locked"), true);
                    return;
                }
                if (sp == Spell.NASCENT_SOUL_OUT_OF_BODY && NascentSoulOutOfBodyHandler.isActive(player)) {
                    NascentSoulOutOfBodyHandler.toggle(player);
                    return;
                }
                if (sp.type() == SpellType.PASSIVE) {
                    boolean newEnabled = !data.isSpellEnabled(sp);
                    data.setSpellEnabled(sp, newEnabled);
                    CapabilityEvents.syncToClient(player);
                    player.displayClientMessage((Component)Component.translatable((String)(newEnabled ? "message.friday_cultivation.cast.passive_on" : "message.friday_cultivation.cast.passive_off"), (Object[])new Object[]{sp.displayNameForRealm(data.getRealm())}), true);
                    return;
                }
                if (sp == Spell.SKY_SPLITTING_SWORD_AURA && !(player.getMainHandItem().getItem() instanceof SwordItem)) {
                    player.displayClientMessage((Component)Component.translatable((String)"message.friday_cultivation.sky_splitting_sword_aura.no_sword"), true);
                    return;
                }
                if (sp == Spell.FLYING_SWORD && !(player.getMainHandItem().getItem() instanceof SwordItem)) {
                    player.displayClientMessage((Component)Component.translatable((String)"message.friday_cultivation.flying_sword.no_sword"), true);
                    return;
                }
                if (sp == Spell.SWORD_FLIGHT && !(player.getMainHandItem().getItem() instanceof SwordItem) && !com.friday.cultivation.flight.CultivationFlightHandler.isSwordFlightActive(player)) {
                    player.displayClientMessage((Component)Component.translatable((String)"message.friday_cultivation.sword_flight.no_sword"), true);
                    return;
                }
                if (sp == Spell.SOUL_HOOK) {
                    boolean ghost = data.isGhostCultivator() || data.isSoulState();
                    boolean holdingHook = player.getMainHandItem().getItem() instanceof SoulHookItem;
                    if (!ghost || !holdingHook) {
                        player.displayClientMessage((Component)Component.translatable((String)"message.friday_cultivation.soul_hook_spell.requirement"), true);
                        return;
                    }
                    if (!SoulHookHandler.hasTarget(player)) {
                        player.displayClientMessage((Component)Component.translatable((String)"message.friday_cultivation.soul_hook_spell.no_target"), true);
                        return;
                    }
                }
                if (sp == Spell.SWORD_FLIGHT) {
                    DharmaBodyManifestationHandler.trigger(player);
                    com.friday.cultivation.flight.CultivationFlightHandler.toggleSwordFlight(player);
                    return;
                }
                if (sp == Spell.SPIRIT_LOCK && !SpiritLockHandler.hasLockTarget(player)) {
                    player.displayClientMessage((Component)Component.translatable((String)"message.friday_cultivation.spirit_lock.no_target"), true);
                    return;
                }
                if (sp == Spell.SPIRIT_UNLOCK && !SpiritLockHandler.hasUnlockTarget(player)) {
                    player.displayClientMessage((Component)Component.translatable((String)"message.friday_cultivation.spirit_unlock.no_locked_target"), true);
                    return;
                }
                if (sp == Spell.TAISHANG_LIFE_BALANCE && !LifeBalanceHandler.hasTapTarget(player)) {
                    player.displayClientMessage((Component)Component.translatable((String)"message.friday_cultivation.taishang_life_balance.no_target"), true);
                    return;
                }
                if (sp == Spell.REALM_PRESSURE && !RealmPressureHandler.hasTapTarget(player)) {
                    player.displayClientMessage((Component)Component.translatable((String)"message.friday_cultivation.realm_pressure.no_target"), true);
                    return;
                }
                if (sp == Spell.BUDDHA_FIRE_LOTUS) {
                    player.displayClientMessage((Component)Component.translatable((String)"message.friday_cultivation.buddha_fire_lotus.must_charge"), true);
                    return;
                }
                if (sp == Spell.CORE_SELF_DESTRUCT) {
                    player.displayClientMessage((Component)Component.translatable((String)"message.friday_cultivation.core_self_destruct.must_charge"), true);
                    return;
                }
                if (sp == Spell.PALM_THUNDER && PalmThunderHandler.dismissIfArmed(player)) {
                    return;
                }
                DharmaBodyManifestationHandler.trigger(player);
                long actualCost = TechniqueBonusHelper.applySpellQiCostMultiplier((Player)player, sp, sp.qiCost());
                if (data.getCurrentQi() < actualCost) {
                    player.displayClientMessage((Component)Component.translatable((String)"message.friday_cultivation.cast.no_qi", (Object[])new Object[]{sp.displayNameForRealm(data.getRealm())}), true);
                    return;
                }
                data.setCurrentQi(data.getCurrentQi() - actualCost);
                CapabilityEvents.syncToClient(player);
                int powerPct = SpellScalingHelper.powerBonusPercent((LivingEntity)player, sp);
                CastSpellPacket.executeActiveSpell(player, sp, powerPct);
                PhysiqueBonusHelper.onSpellCast(player, sp);
                if (sp != Spell.DIVINE_SENSE && sp != Spell.VOID_ESCAPE && sp != Spell.SOUL_HOOK && sp != Spell.REALM_PRESSURE) {
                    player.level().playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.BLAZE_SHOOT, SoundSource.PLAYERS, 0.4f, 1.6f);
                    player.displayClientMessage((Component)Component.translatable((String)"message.friday_cultivation.cast.active_done", (Object[])new Object[]{sp.displayNameForRealm(data.getRealm())}), true);
                }
            });
        });
        ctx.setPacketHandled(true);
    }

    private static void castSoulHook(ServerPlayer player, ServerLevel level) {
        SoulHookHandler.cast(player);
    }

    private static void executeActiveSpell(ServerPlayer player, Spell sp, int powerPctBonus) {
        ServerLevel level = player.serverLevel();
        if (level == null) {
            return;
        }
        Vec3 look = player.getLookAngle();
        Vec3 eye = player.getEyePosition();
        switch (sp) {
            case FIREBALL: {
                int explosionPower = Math.min(5, Math.max(1, 1 + powerPctBonus / 30));
                int extraOnHit = SpellScalingHelper.scaledDamage((LivingEntity)player, sp);
                XiaoxiangFireballEntity ball = new XiaoxiangFireballEntity((Level)level, (LivingEntity)player, look.x, look.y, look.z, explosionPower);
                ball.setExtraDamage(extraOnHit);
                ball.setPos(player.getX() + look.x, player.getEyeY() + look.y * 0.5, player.getZ() + look.z);
                level.addFreshEntity((Entity)ball);
                break;
            }
            case GREAT_FIREBALL: {
                int basicQi = sp.qiCost();
                GreatFireballEntity ball = new GreatFireballEntity((Level)level, (LivingEntity)player, look.x, look.y, look.z, basicQi);
                ball.setDamageMultiplier(SpellScalingHelper.damageMultiplier((LivingEntity)player, sp));
                ball.setPos(player.getX() + look.x * 1.5, player.getEyeY() + look.y * 0.5, player.getZ() + look.z * 1.5);
                level.addFreshEntity((Entity)ball);
                break;
            }
            case SKY_SPLITTING_SWORD_AURA: {
                Vec3 spawnPos = player.getEyePosition().add(player.getLookAngle().scale(1.5));
                SkySplittingSwordAuraEntity aura = new SkySplittingSwordAuraEntity((Level)level, (LivingEntity)player, spawnPos, player.getLookAngle().normalize(), false);
                aura.setDamage(SpellScalingHelper.scaledDamageFloat((LivingEntity)player, sp, 1000.0f));
                level.addFreshEntity((Entity)aura);
                level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.TRIDENT_THROW, SoundSource.PLAYERS, 1.0f, 1.3f);
                break;
            }
            case CLEAR_MIND: {
                ArrayList<MobEffect> harmful = new ArrayList<MobEffect>();
                for (MobEffectInstance eff : player.getActiveEffects()) {
                    if (eff.getEffect().getCategory() != MobEffectCategory.HARMFUL) continue;
                    harmful.add(eff.getEffect());
                }
                for (MobEffect e2 : harmful) {
                    player.removeEffect(e2);
                }
                player.setRemainingFireTicks(0);
                break;
            }
            case TIME_STASIS: {
                TimeStasisHandler.castSingleOrRelease(player);
                break;
            }
            case SWORD_FLIGHT: {
                com.friday.cultivation.flight.CultivationFlightHandler.toggleSwordFlight(player);
                break;
            }
            case CORE_SELF_DESTRUCT: {
                CoreSelfDestructHandler.cast(player);
                break;
            }
            case NASCENT_SOUL_OUT_OF_BODY: {
                NascentSoulOutOfBodyHandler.toggle(player);
                break;
            }
            case DIVINE_SENSE: {
                DivineSenseHandler.singleScan(player);
                break;
            }
            case TAISHANG_LIFE_BALANCE: {
                LifeBalanceHandler.castTap(player);
                break;
            }
            case REALM_PRESSURE: {
                RealmPressureHandler.castTap(player);
                break;
            }
            case SOUL_HOOK: {
                CastSpellPacket.castSoulHook(player, level);
                break;
            }
            case SPIRIT_LOCK: {
                SpiritLockHandler.castLock(player);
                break;
            }
            case SPIRIT_UNLOCK: {
                SpiritLockHandler.castUnlock(player);
                break;
            }
            case FLYING_SWORD: {
                Vec3 spawnPos = new Vec3(player.getX() + look.x * 1.0, player.getEyeY() - 0.1, player.getZ() + look.z * 1.0);
                Vec3 targetPos = eye.add(look.scale(50.0));
                SwordProjectileEntity sword = new SwordProjectileEntity((Level)level, (LivingEntity)player, spawnPos, targetPos, false, true);
                sword.setDirectHitDamage(SpellScalingHelper.scaledDamage((LivingEntity)player, sp));
                level.addFreshEntity((Entity)sword);
                break;
            }
            case STAR_FALL: {
                Vec3 eyePos = player.getEyePosition();
                Vec3 target = eyePos.add(player.getLookAngle().scale(1000.0));
                BlockHitResult hr = level.clip(new ClipContext(eyePos, target, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, (Entity)player));
                Vec3 actualTarget = hr.getType() != HitResult.Type.MISS ? hr.getLocation() : target;
                Vec3 spawnPos = new Vec3(player.getX(), player.getEyeY() + 60.0, player.getZ());
                MeteorEntity meteor = new MeteorEntity((Level)level, (LivingEntity)player, spawnPos, actualTarget, 0, 2.0f);
                meteor.setDamageMultiplier(SpellScalingHelper.damageMultiplier((LivingEntity)player, sp));
                level.addFreshEntity((Entity)meteor);
                break;
            }
            case ICE_LANCE: {
                Snowball ball = new Snowball((Level)level, (LivingEntity)player);
                ball.shootFromRotation((Entity)player, player.getXRot(), player.getYRot(), 0.0f, 1.6f, 0.0f);
                level.addFreshEntity((Entity)ball);
                LivingEntity target = CastSpellPacket.raycastEntity(player, 20.0);
                if (target == null || !SectCombatHandler.canApplyOffensiveEffect((LivingEntity)player, target)) break;
                target.hurt(SpellDamageSourceHelper.directSpell((LivingEntity)player), (float)SpellScalingHelper.scaledDamage((LivingEntity)player, sp));
                target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 60 + powerPctBonus / 2, 1));
                break;
            }
            case LIGHTNING_BOLT: {
                BlockHitResult hit = CastSpellPacket.raycastBlock((Player)player, 30.0);
                Vec3 pos = hit.getType() == HitResult.Type.MISS ? eye.add(look.scale(20.0)) : hit.getLocation();
                SpellLightningHelper.strike(level, (LivingEntity)player, pos, SpellScalingHelper.scaledDamageFloat((LivingEntity)player, sp, 5.0f));
                break;
            }
            case PALM_THUNDER: {
                PalmThunderHandler.spawnProjectile((LivingEntity)player, look, SpellScalingHelper.scaledDamageFloat((LivingEntity)player, sp, sp.damage()));
                break;
            }
            case WIND_BLADE: {
                AABB box = player.getBoundingBox().inflate(5.0);
                List<LivingEntity> targets = level.getEntitiesOfClass(LivingEntity.class, box, e -> e != player);
                for (LivingEntity t : targets) {
                    Vec3 dir;
                    if (!SoulStateHandler.canOrdinaryAffect((Entity)player, (Entity)t) || !SectCombatHandler.canApplyOffensiveEffect((LivingEntity)player, t) || (dir = t.position().multiply(player.position()).normalize()).dot(look) < 0.3) continue;
                    t.hurt(SpellDamageSourceHelper.directSpell((LivingEntity)player), (float)SpellScalingHelper.scaledDamage((LivingEntity)player, sp));
                    t.knockback(1.5 + (double)powerPctBonus / 100.0, -dir.x, -dir.z);
                }
                break;
            }
            case POISON_MIST: {
                AABB box = player.getBoundingBox().inflate(4.0);
                int duration = 100 + powerPctBonus * 2;
                for (LivingEntity t : level.getEntitiesOfClass(LivingEntity.class, box, e -> e != player)) {
                    if (!SoulStateHandler.canOrdinaryAffect((Entity)player, (Entity)t) || !SectCombatHandler.canApplyOffensiveEffect((LivingEntity)player, t)) continue;
                    t.addEffect(new MobEffectInstance(MobEffects.POISON, duration, 1));
                }
                break;
            }
            case WITHER_TOUCH: {
                AABB box = player.getBoundingBox().inflate(3.0);
                int duration = 80 + powerPctBonus * 2;
                for (LivingEntity t : level.getEntitiesOfClass(LivingEntity.class, box, e -> e != player)) {
                    if (!SoulStateHandler.canOrdinaryAffect((Entity)player, (Entity)t) || !SectCombatHandler.canApplyOffensiveEffect((LivingEntity)player, t)) continue;
                    t.addEffect(new MobEffectInstance(MobEffects.WITHER, duration, 0));
                }
                break;
            }
            case ARROW_VOLLEY: {
                for (int i = -1; i <= 1; ++i) {
                    Arrow arrow = new Arrow((Level)level, (LivingEntity)player);
                    Vec3 spread = look.add((double)i * 0.05, 0.0, (double)i * 0.05);
                    arrow.shoot(spread.x, spread.y, spread.z, 2.5f, 1.0f);
                    arrow.setBaseDamage(SpellScalingHelper.scaledDamageDouble((LivingEntity)player, sp, Math.max(4.0, arrow.getBaseDamage())));
                    level.addFreshEntity((Entity)arrow);
                }
                break;
            }
            case EARTH_SPIKE: {
                BlockHitResult hit = CastSpellPacket.raycastBlock((Player)player, 20.0);
                if (hit.getType() == HitResult.Type.MISS) break;
                Vec3 p = hit.getLocation();
                int power = Math.min(4, 2 + powerPctBonus / 50);
                level.explode((Entity)player, p.x, p.y, p.z, (float)power, Level.ExplosionInteraction.NONE);
                break;
            }
            case STONE_BULLET: {
                StoneBulletEntity bullet = new StoneBulletEntity((Level)level, (LivingEntity)player);
                bullet.setDamage(SpellScalingHelper.scaledDamage((LivingEntity)player, sp));
                bullet.setPos(player.getX() + look.x * 0.8, player.getEyeY() - 0.05 + look.y * 0.4, player.getZ() + look.z * 0.8);
                bullet.shoot(look.x, look.y, look.z, 2.2f, 0.1f);
                level.addFreshEntity((Entity)bullet);
                break;
            }
            case HEAVEN_PIERCING_CONE: {
                Vec3 dir = HeavenPiercingConeEntity.safeDirection(look);
                HeavenPiercingConeEntity cone = new HeavenPiercingConeEntity((Level)level, (LivingEntity)player);
                cone.configure(SpellScalingHelper.scaledDamage((LivingEntity)player, sp), 3.4, 1, 0, true);
                Vec3 spawnPos = HeavenPiercingConeEntity.safeSideLaunchPosition((Level)level, (LivingEntity)player, dir);
                Vec3 targetPos = CastSpellPacket.raycastCrosshairTarget((Player)player, 1000.0);
                Vec3 launchDir = HeavenPiercingConeEntity.aimDirectionFromSide(spawnPos, targetPos, dir);
                cone.setPos(spawnPos.x, spawnPos.y, spawnPos.z);
                cone.setDeltaMovement(launchDir.scale(3.4));
                level.addFreshEntity((Entity)cone);
                break;
            }
            case SUN_FLARE: {
                AABB box = player.getBoundingBox().inflate(8.0);
                int duration = 80 + powerPctBonus * 2;
                for (LivingEntity t : level.getEntitiesOfClass(LivingEntity.class, box, e -> e != player)) {
                    if (!SoulStateHandler.canOrdinaryAffect((Entity)player, (Entity)t) || !SectCombatHandler.canApplyOffensiveEffect((LivingEntity)player, t)) continue;
                    t.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, duration, 0));
                }
                break;
            }
            case SHADOW_STEP: {
                Vec3 dest;
                BlockHitResult hit = CastSpellPacket.raycastBlock((Player)player, 30.0);
                if (hit.getType() == HitResult.Type.MISS) {
                    dest = eye.add(look.scale(30.0));
                } else {
                    Vec3 hitPos = hit.getLocation();
                    dest = hitPos.multiply(look.scale(0.6));
                }
                player.teleportTo(dest.x, dest.y, dest.z);
                player.fallDistance = 0.0f;
                break;
            }
            case SOARING: {
                int duration = 100 + powerPctBonus * 2;
                player.addEffect(new MobEffectInstance(MobEffects.LEVITATION, duration, 1));
                break;
            }
            case SPEED_BURST: {
                int duration = 200 + powerPctBonus * 2;
                player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, duration, 1));
                break;
            }
            case INVISIBILITY: {
                int duration = 200 + powerPctBonus * 4;
                player.addEffect(new MobEffectInstance(MobEffects.INVISIBILITY, duration, 0));
                break;
            }
            case HEALING_TOUCH: {
                player.heal(SpellScalingHelper.scaledDamageFloat((LivingEntity)player, sp, 6.0f));
                break;
            }
            case IRON_BODY: {
                int duration = 200 + powerPctBonus * 2;
                player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, duration, 1));
                break;
            }
            case WATER_AFFINITY: {
                int duration = 600 + powerPctBonus * 6;
                player.addEffect(new MobEffectInstance(MobEffects.WATER_BREATHING, duration, 0));
                break;
            }
            case NIGHT_EYE: {
                int duration = 1200 + powerPctBonus * 12;
                player.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, duration, 0));
                break;
            }
            case FIRE_PROTECTION: {
                int duration = 400 + powerPctBonus * 4;
                player.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, duration, 0));
                break;
            }
            case TRUTH_SIGHT_EYE: {
                int radius = 200;
                int localGlowDurationTicks = 1200;
                AABB box = new AABB(player.getX() - (double)radius, player.getY() - (double)radius, player.getZ() - (double)radius, player.getX() + (double)radius, player.getY() + (double)radius, player.getZ() + (double)radius);
                ArrayList<Integer> revealed = new ArrayList<Integer>();
                for (LivingEntity t : level.getEntitiesOfClass(LivingEntity.class, box, e -> e.isAlive())) {
                    if (t == player || !SoulStateHandler.canOrdinaryAffect((Entity)player, (Entity)t)) continue;
                    revealed.add(t.getId());
                }
                ClientOnlyGlowPacket.send(player, revealed, localGlowDurationTicks);
                if (!player.hasEffect(MobEffects.BLINDNESS)) break;
                player.removeEffect(MobEffects.BLINDNESS);
                break;
            }
        }
    }

    private static LivingEntity raycastEntity(ServerPlayer player, double maxDist) {
        Vec3 eye = player.getEyePosition();
        Vec3 look = player.getLookAngle();
        Vec3 end = eye.add(look.scale(maxDist));
        AABB scan = new AABB(eye, end).inflate(1.0);
        LivingEntity best = null;
        double bestT = maxDist;
        for (LivingEntity e : player.serverLevel().getEntitiesOfClass(LivingEntity.class, scan, ent -> ent != player && ent.isAlive() && SoulStateHandler.canOrdinaryAffect((Entity)player, (Entity)ent) && SectCombatHandler.canTargetOffensiveEffect((LivingEntity)player, ent))) {
            Vec3 closest;
            Vec3 toE = e.position().multiply(eye);
            double along = toE.dot(look);
            if (along < 0.0 || along > maxDist || (closest = eye.add(look.scale(along))).distanceTo(e.position()) > 1.5 || !(along < bestT)) continue;
            bestT = along;
            best = e;
        }
        return best;
    }

    private static BlockHitResult raycastBlock(Player player, double maxDist) {
        Vec3 eye = player.getEyePosition();
        Vec3 look = player.getLookAngle();
        Vec3 end = eye.add(look.scale(maxDist));
        return player.level().clip(new ClipContext(eye, end, ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, (Entity)player));
    }

    private static Vec3 raycastCrosshairTarget(Player player, double maxDist) {
        BlockHitResult hit = CastSpellPacket.raycastBlock(player, maxDist);
        if (hit.getType() != HitResult.Type.MISS) {
            return hit.getLocation();
        }
        return player.getEyePosition().add(player.getLookAngle().scale(maxDist));
    }
}

