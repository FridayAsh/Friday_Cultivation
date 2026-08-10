package com.friday.cultivation.network;

import com.friday.cultivation.CultivationCapability;
import com.friday.cultivation.entity.spell.GreatFireballEntity;
import com.friday.cultivation.entity.spell.HeavenPiercingConeEntity;
import com.friday.cultivation.entity.spell.MeteorEntity;
import com.friday.cultivation.entity.spell.SkySplittingSwordAuraEntity;
import com.friday.cultivation.entity.spell.StoneBulletEntity;
import com.friday.cultivation.entity.spell.SwordProjectileEntity;
import com.friday.cultivation.entity.spell.XiaoxiangFireballEntity;
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
import com.friday.cultivation.event.SwordFlightHandler;
import com.friday.cultivation.event.TimeStasisHandler;
import com.friday.cultivation.event.VoidEscapeHandler;
import com.friday.cultivation.item.weapon.SoulHookItem;
import com.friday.cultivation.physique.PhysiqueBonusHelper;
import com.friday.cultivation.registry.ModEffects;
import com.friday.cultivation.spell.Spell;
import com.friday.cultivation.spell.SpellType;
import com.friday.cultivation.technique.TechniqueBonusHelper;
import com.friday.cultivation.util.SpellDamageSourceHelper;
import com.friday.cultivation.util.SpellLightningHelper;
import com.friday.cultivation.util.SpellScalingHelper;
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

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

/**
 * 施放法术包 — 客户端 → 服务端。
 * 完整复刻原模组 com.xiaoxiang.cultivation.network.CastSpellPacket 的 handle 逻辑
 * （前置校验 / 灵气扣除 / 音效 / 同步 + executeActiveSpell 全部法术分支）。
 * 保留项目现有公共 API：{@link #CastSpellPacket(String)} 携带法术 ID，
 * handle 中优先使用包内法术 ID，为空时回退到 capability 的当前选中法术（照搬原模组）。
 */
public class CastSpellPacket {
    private final String spellId;

    public CastSpellPacket(String spellId) {
        this.spellId = spellId;
    }

    public static void encode(CastSpellPacket msg, FriendlyByteBuf buf) {
        buf.writeUtf(msg.spellId == null ? "" : msg.spellId);
    }

    public static CastSpellPacket decode(FriendlyByteBuf buf) {
        return new CastSpellPacket(buf.readUtf(32767));
    }

    public static void handle(CastSpellPacket msg, Supplier<NetworkEvent.Context> ctxSup) {
        NetworkEvent.Context ctx = ctxSup.get();
        ctx.enqueueWork(() -> {
            ServerPlayer player = ctx.getSender();
            if (player == null) {
                return;
            }
            if (SoulHookHandler.isActionLocked(player)) {
                player.displayClientMessage(Component.translatable("message.friday_cultivation.soul_hook_spell.action_locked"), true);
                return;
            }
            CultivationCapability.get(player).ifPresent(data -> {
                if (data.isVoidEscapeActive()) {
                    VoidEscapeHandler.tryManualExitIfActive(player);
                    return;
                }
                String sid = msg.spellId != null && !msg.spellId.isEmpty() ? msg.spellId : data.getSelectedSpellId();
                if (sid.isEmpty()) {
                    player.displayClientMessage(Component.translatable("message.friday_cultivation.cast.no_selection"), true);
                    return;
                }
                Spell sp = Spell.byId(sid);
                if (sp == null || !data.hasSpell(sp)) {
                    player.displayClientMessage(Component.translatable("message.friday_cultivation.cast.no_selection"), true);
                    return;
                }
                if (!TimeStasisHandler.canPerformStoppedTimeAction(player, sp)) {
                    return;
                }
                if (data.isInTribulation() && sp == Spell.VOID_ESCAPE) {
                    player.displayClientMessage(Component.translatable("message.friday_cultivation.void_escape.blocked_tribulation"), true);
                    return;
                }
                if (data.isInTribulation() && sp == Spell.NASCENT_SOUL_OUT_OF_BODY) {
                    NascentSoulOutOfBodyHandler.stopIfActive(player, false);
                    CapabilityEvents.syncToClient(player);
                    player.displayClientMessage(Component.translatable("message.friday_cultivation.nascent_soul_out_of_body.blocked"), true);
                    return;
                }
                if (NascentSoulOutOfBodyHandler.isActive(player) && sp != Spell.NASCENT_SOUL_OUT_OF_BODY) {
                    player.displayClientMessage(Component.translatable("message.friday_cultivation.nascent_soul_out_of_body.blocked"), true);
                    return;
                }
                if (player.hasEffect(ModEffects.MERIDIAN_FROZEN.get())) {
                    player.displayClientMessage(Component.translatable("message.friday_cultivation.meridian_frozen.caster_locked"), true);
                    return;
                }
                if (SpiritLockHandler.isEntityLocked(player) && SpiritLockHandler.tryCastSelfUnlock(player, data)) {
                    return;
                }
                if (SpiritLockHandler.isEntityLocked(player) && sp != Spell.SPIRIT_UNLOCK) {
                    player.displayClientMessage(Component.translatable("message.friday_cultivation.spirit_lock.caster_locked"), true);
                    return;
                }
                if (sp == Spell.SWORD_FLIGHT && SwordFlightHandler.isActive(data)) {
                    SwordFlightHandler.stop(player, data);
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
                    player.displayClientMessage(Component.translatable(newEnabled ? "message.friday_cultivation.cast.passive_on" : "message.friday_cultivation.cast.passive_off", sp.displayNameForRealm(data.getRealm())), true);
                    return;
                }
                if (sp == Spell.SKY_SPLITTING_SWORD_AURA && !(player.getMainHandItem().getItem() instanceof SwordItem)) {
                    player.displayClientMessage(Component.translatable("message.friday_cultivation.sky_splitting_sword_aura.no_sword"), true);
                    return;
                }
                if (sp == Spell.FLYING_SWORD && !(player.getMainHandItem().getItem() instanceof SwordItem)) {
                    player.displayClientMessage(Component.translatable("message.friday_cultivation.flying_sword.no_sword"), true);
                    return;
                }
                if (sp == Spell.SWORD_FLIGHT && !(player.getMainHandItem().getItem() instanceof SwordItem)) {
                    player.displayClientMessage(Component.translatable("message.friday_cultivation.sword_flight.no_sword"), true);
                    return;
                }
                if (sp == Spell.SOUL_HOOK) {
                    boolean ghost = data.isGhostCultivator() || data.isSoulState();
                    boolean holdingHook = player.getMainHandItem().getItem() instanceof SoulHookItem;
                    if (!ghost || !holdingHook) {
                        player.displayClientMessage(Component.translatable("message.friday_cultivation.soul_hook_spell.requirement"), true);
                        return;
                    }
                    if (!SoulHookHandler.hasTarget(player)) {
                        player.displayClientMessage(Component.translatable("message.friday_cultivation.soul_hook_spell.no_target"), true);
                        return;
                    }
                }
                if (sp == Spell.SWORD_FLIGHT) {
                    DharmaBodyManifestationHandler.trigger(player);
                    SwordFlightHandler.start(player);
                    return;
                }
                if (sp == Spell.SPIRIT_LOCK && !SpiritLockHandler.hasLockTarget(player)) {
                    player.displayClientMessage(Component.translatable("message.friday_cultivation.spirit_lock.no_target"), true);
                    return;
                }
                if (sp == Spell.SPIRIT_UNLOCK && !SpiritLockHandler.hasUnlockTarget(player)) {
                    player.displayClientMessage(Component.translatable("message.friday_cultivation.spirit_unlock.no_locked_target"), true);
                    return;
                }
                if (sp == Spell.TAISHANG_LIFE_BALANCE && !LifeBalanceHandler.hasTapTarget(player)) {
                    player.displayClientMessage(Component.translatable("message.friday_cultivation.taishang_life_balance.no_target"), true);
                    return;
                }
                if (sp == Spell.REALM_PRESSURE && !RealmPressureHandler.hasTapTarget(player)) {
                    player.displayClientMessage(Component.translatable("message.friday_cultivation.realm_pressure.no_target"), true);
                    return;
                }
                if (sp == Spell.BUDDHA_FIRE_LOTUS) {
                    player.displayClientMessage(Component.translatable("message.friday_cultivation.buddha_fire_lotus.must_charge"), true);
                    return;
                }
                if (sp == Spell.CORE_SELF_DESTRUCT) {
                    player.displayClientMessage(Component.translatable("message.friday_cultivation.core_self_destruct.must_charge"), true);
                    return;
                }
                if (sp == Spell.PALM_THUNDER && PalmThunderHandler.dismissIfArmed(player)) {
                    return;
                }
                DharmaBodyManifestationHandler.trigger(player);
                long actualCost = TechniqueBonusHelper.applySpellQiCostMultiplier(player, sp, sp.qiCost());
                if (data.getCurrentQi() < actualCost) {
                    player.displayClientMessage(Component.translatable("message.friday_cultivation.cast.no_qi", sp.displayNameForRealm(data.getRealm())), true);
                    return;
                }
                data.setCurrentQi(data.getCurrentQi() - actualCost);
                CapabilityEvents.syncToClient(player);
                int powerPct = SpellScalingHelper.powerBonusPercent(player, sp);
                executeActiveSpell(player, sp, powerPct);
                PhysiqueBonusHelper.onSpellCast(player, sp);
                if (sp != Spell.DIVINE_SENSE && sp != Spell.VOID_ESCAPE && sp != Spell.SOUL_HOOK && sp != Spell.REALM_PRESSURE) {
                    player.level().playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.BLAZE_SHOOT, SoundSource.PLAYERS, 0.4f, 1.6f);
                    player.displayClientMessage(Component.translatable("message.friday_cultivation.cast.active_done", sp.displayNameForRealm(data.getRealm())), true);
                }
            });
        });
        ctx.setPacketHandled(true);
    }

    private static void castSoulHook(ServerPlayer player, ServerLevel level) {
        SoulHookHandler.cast(player);
    }

    /** 执行瞬发法术效果 — 完整照搬原模组 executeActiveSpell 全部 37 分支 */
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
                int extraOnHit = SpellScalingHelper.scaledDamage(player, sp);
                XiaoxiangFireballEntity ball = new XiaoxiangFireballEntity(level, player, look.x, look.y, look.z, explosionPower);
                ball.setExtraDamage(extraOnHit);
                ball.moveTo(player.getX() + look.x, player.getEyeY() + look.y * 0.5, player.getZ() + look.z);
                level.addFreshEntity(ball);
                break;
            }
            case GREAT_FIREBALL: {
                int basicQi = sp.qiCost();
                GreatFireballEntity ball = new GreatFireballEntity(level, player, look.x, look.y, look.z, basicQi);
                ball.setDamageMultiplier(SpellScalingHelper.damageMultiplier(player, sp));
                ball.moveTo(player.getX() + look.x * 1.5, player.getEyeY() + look.y * 0.5, player.getZ() + look.z * 1.5);
                level.addFreshEntity(ball);
                break;
            }
            case SKY_SPLITTING_SWORD_AURA: {
                Vec3 spawnPos = player.getEyePosition().add(player.getLookAngle().scale(1.5));
                SkySplittingSwordAuraEntity aura = new SkySplittingSwordAuraEntity(level, player, spawnPos, player.getLookAngle().normalize(), false);
                aura.setDamage(SpellScalingHelper.scaledDamageFloat(player, sp, 1000.0f));
                level.addFreshEntity(aura);
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
                SwordFlightHandler.start(player);
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
                castSoulHook(player, level);
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
                SwordProjectileEntity sword = new SwordProjectileEntity(level, player, spawnPos, targetPos, false, true);
                sword.setDirectHitDamage(SpellScalingHelper.scaledDamage(player, sp));
                level.addFreshEntity(sword);
                break;
            }
            case STAR_FALL: {
                Vec3 eyePos = player.getEyePosition();
                Vec3 target = eyePos.add(player.getLookAngle().scale(1000.0));
                BlockHitResult hr = level.clip(new ClipContext(eyePos, target, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, player));
                Vec3 actualTarget = hr.getType() != HitResult.Type.MISS ? hr.getLocation() : target;
                Vec3 spawnPos = new Vec3(player.getX(), player.getY() + 60.0, player.getZ());
                MeteorEntity meteor = new MeteorEntity(level, player, spawnPos, actualTarget, 0, 2.0f);
                meteor.setDamageMultiplier(SpellScalingHelper.damageMultiplier(player, sp));
                level.addFreshEntity(meteor);
                break;
            }
            case ICE_LANCE: {
                Snowball ball = new Snowball(level, player);
                ball.shootFromRotation(player, player.getXRot(), player.getYRot(), 0.0f, 1.6f, 0.0f);
                level.addFreshEntity(ball);
                LivingEntity target = raycastEntity(player, 20.0);
                if (target == null || !SectCombatHandler.canApplyOffensiveEffect(player, target)) break;
                target.hurt(SpellDamageSourceHelper.directSpell(player), (float) SpellScalingHelper.scaledDamage(player, sp));
                target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 60 + powerPctBonus / 2, 1));
                break;
            }
            case LIGHTNING_BOLT: {
                BlockHitResult hit = raycastBlock(player, 30.0);
                Vec3 pos = hit.getType() == HitResult.Type.MISS ? eye.add(look.scale(20.0)) : hit.getLocation();
                SpellLightningHelper.strike(level, player, pos, SpellScalingHelper.scaledDamageFloat(player, sp, 5.0f));
                break;
            }
            case PALM_THUNDER: {
                PalmThunderHandler.spawnProjectile(player, look, SpellScalingHelper.scaledDamageFloat(player, sp, sp.damage()));
                break;
            }
            case WIND_BLADE: {
                AABB box = player.getBoundingBox().inflate(5.0);
                List<LivingEntity> targets = level.getEntitiesOfClass(LivingEntity.class, box, e -> e != player);
                for (LivingEntity t : targets) {
                    Vec3 dir;
                    if (!SoulStateHandler.canOrdinaryAffect(player, t) || !SectCombatHandler.canApplyOffensiveEffect(player, t) || (dir = t.position().subtract(player.position()).normalize()).dot(look) < 0.3) continue;
                    t.hurt(SpellDamageSourceHelper.directSpell(player), (float) SpellScalingHelper.scaledDamage(player, sp));
                    t.knockback(1.5 + (double) powerPctBonus / 100.0, -dir.x, -dir.z);
                }
                break;
            }
            case POISON_MIST: {
                AABB box = player.getBoundingBox().inflate(4.0);
                int duration = 100 + powerPctBonus * 2;
                for (LivingEntity t : level.getEntitiesOfClass(LivingEntity.class, box, e -> e != player)) {
                    if (!SoulStateHandler.canOrdinaryAffect(player, t) || !SectCombatHandler.canApplyOffensiveEffect(player, t)) continue;
                    t.addEffect(new MobEffectInstance(MobEffects.POISON, duration, 1));
                }
                break;
            }
            case WITHER_TOUCH: {
                AABB box = player.getBoundingBox().inflate(3.0);
                int duration = 80 + powerPctBonus * 2;
                for (LivingEntity t : level.getEntitiesOfClass(LivingEntity.class, box, e -> e != player)) {
                    if (!SoulStateHandler.canOrdinaryAffect(player, t) || !SectCombatHandler.canApplyOffensiveEffect(player, t)) continue;
                    t.addEffect(new MobEffectInstance(MobEffects.WITHER, duration, 0));
                }
                break;
            }
            case ARROW_VOLLEY: {
                for (int i = -1; i <= 1; ++i) {
                    Arrow arrow = new Arrow(level, player);
                    Vec3 spread = look.add((double) i * 0.05, 0.0, (double) i * 0.05);
                    arrow.shoot(spread.x, spread.y, spread.z, 2.5f, 1.0f);
                    arrow.setBaseDamage(SpellScalingHelper.scaledDamageDouble(player, sp, Math.max(4.0, arrow.getBaseDamage())));
                    level.addFreshEntity(arrow);
                }
                break;
            }
            case EARTH_SPIKE: {
                BlockHitResult hit = raycastBlock(player, 20.0);
                if (hit.getType() == HitResult.Type.MISS) break;
                Vec3 p = hit.getLocation();
                int power = Math.min(4, 2 + powerPctBonus / 50);
                level.explode(player, p.x, p.y, p.z, power, Level.ExplosionInteraction.NONE);
                break;
            }
            case STONE_BULLET: {
                StoneBulletEntity bullet = new StoneBulletEntity(level, player);
                bullet.setDamage(SpellScalingHelper.scaledDamage(player, sp));
                bullet.moveTo(player.getX() + look.x * 0.8, player.getY() - 0.05 + look.y * 0.4, player.getZ() + look.z * 0.8);
                bullet.shoot(look.x, look.y, look.z, 2.2f, 0.1f);
                level.addFreshEntity(bullet);
                break;
            }
            case HEAVEN_PIERCING_CONE: {
                Vec3 dir = HeavenPiercingConeEntity.safeDirection(look);
                HeavenPiercingConeEntity cone = new HeavenPiercingConeEntity(level, player);
                cone.configure(SpellScalingHelper.scaledDamage(player, sp), 3.4, 1, 0, true);
                Vec3 spawnPos = HeavenPiercingConeEntity.safeSideLaunchPosition(level, player, dir);
                Vec3 targetPos = raycastCrosshairTarget(player, 1000.0);
                Vec3 launchDir = HeavenPiercingConeEntity.aimDirectionFromSide(spawnPos, targetPos, dir);
                cone.moveTo(spawnPos.x, spawnPos.y, spawnPos.z);
                cone.setDeltaMovement(launchDir.scale(3.4));
                level.addFreshEntity(cone);
                break;
            }
            case SUN_FLARE: {
                AABB box = player.getBoundingBox().inflate(8.0);
                int duration = 80 + powerPctBonus * 2;
                for (LivingEntity t : level.getEntitiesOfClass(LivingEntity.class, box, e -> e != player)) {
                    if (!SoulStateHandler.canOrdinaryAffect(player, t) || !SectCombatHandler.canApplyOffensiveEffect(player, t)) continue;
                    t.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, duration, 0));
                }
                break;
            }
            case SHADOW_STEP: {
                Vec3 dest;
                BlockHitResult hit = raycastBlock(player, 30.0);
                if (hit.getType() == HitResult.Type.MISS) {
                    dest = eye.add(look.scale(30.0));
                } else {
                    Vec3 hitPos = hit.getLocation();
                    dest = hitPos.subtract(look.scale(0.6));
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
                player.heal(SpellScalingHelper.scaledDamageFloat(player, sp, 6.0f));
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
                AABB box = new AABB(player.getX() - (double) radius, player.getY() - (double) radius, player.getZ() - (double) radius, player.getX() + (double) radius, player.getY() + (double) radius, player.getZ() + (double) radius);
                ArrayList<Integer> revealed = new ArrayList<Integer>();
                for (LivingEntity t : level.getEntitiesOfClass(LivingEntity.class, box, LivingEntity::isAlive)) {
                    if (t == player || !SoulStateHandler.canOrdinaryAffect(player, t)) continue;
                    revealed.add(t.getId());
                }
                ClientOnlyGlowPacket.send(player, revealed, localGlowDurationTicks);
                if (player.hasEffect(MobEffects.BLINDNESS)) break;
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
        for (LivingEntity e : player.serverLevel().getEntitiesOfClass(LivingEntity.class, scan, ent -> ent != player && ent.isAlive() && SoulStateHandler.canOrdinaryAffect(player, ent) && SectCombatHandler.canTargetOffensiveEffect(player, ent))) {
            Vec3 closest;
            Vec3 toE = e.position().subtract(eye);
            double along = toE.dot(look);
            if (along < 0.0 || along > maxDist || (closest = eye.add(look.scale(along))).distanceToSqr(e.position()) > 1.5 || !(along < bestT)) continue;
            bestT = along;
            best = e;
        }
        return best;
    }

    private static BlockHitResult raycastBlock(Player player, double maxDist) {
        Vec3 eye = player.getEyePosition();
        Vec3 look = player.getLookAngle();
        Vec3 end = eye.add(look.scale(maxDist));
        return player.level().clip(new ClipContext(eye, end, ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, player));
    }

    private static Vec3 raycastCrosshairTarget(Player player, double maxDist) {
        BlockHitResult hit = raycastBlock(player, maxDist);
        if (hit.getType() != HitResult.Type.MISS) {
            return hit.getLocation();
        }
        return player.getEyePosition().add(player.getLookAngle().scale(maxDist));
    }
}
