/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.particles.BlockParticleOption
 *  net.minecraft.core.particles.DustParticleOptions
 *  net.minecraft.core.particles.ParticleOptions
 *  net.minecraft.core.particles.ParticleTypes
 *  net.minecraft.network.chat.Component
 *  net.minecraft.network.protocol.Packet
 *  net.minecraft.network.protocol.game.ClientboundLevelParticlesPacket
 *  net.minecraft.server.level.ServerLevel
 *  net.minecraft.server.level.ServerPlayer
 *  net.minecraft.sounds.SoundEvent
 *  net.minecraft.sounds.SoundEvents
 *  net.minecraft.sounds.SoundSource
 *  net.minecraft.world.effect.MobEffect
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.level.ClipContext
 *  net.minecraft.world.level.ClipContext$Block
 *  net.minecraft.world.level.ClipContext$Fluid
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.level.block.Blocks
 *  net.minecraft.world.phys.AABB
 *  net.minecraft.world.phys.BlockHitResult
 *  net.minecraft.world.phys.HitResult$Type
 *  net.minecraft.world.phys.Vec3
 *  net.minecraftforge.event.TickEvent$Phase
 *  net.minecraftforge.event.TickEvent$PlayerTickEvent
 *  net.minecraftforge.eventbus.api.SubscribeEvent
 *  net.minecraftforge.fml.common.Mod$EventBusSubscriber
 *  org.jetbrains.annotations.Nullable
 *  org.joml.Vector3f
 */
package com.friday.cultivation.event;

import com.friday.cultivation.cultivation.CultivationCapability;
import com.friday.cultivation.cultivation.CultivationData;
import com.friday.cultivation.cultivation.PhysiqueBonusHelper;
import com.friday.cultivation.cultivation.QiElement;
import com.friday.cultivation.cultivation.SpiritRootBonusHelper;
import com.friday.cultivation.cultivation.spell.Spell;
import com.friday.cultivation.cultivation.technique.TechniqueBonusHelper;
import com.friday.cultivation.entity.BuddhaFireLotusEntity;
import com.friday.cultivation.entity.GreatFireballEntity;
import com.friday.cultivation.entity.HeavenPiercingConeEntity;
import com.friday.cultivation.entity.MeteorEntity;
import com.friday.cultivation.entity.SkySplittingSwordAuraEntity;
import com.friday.cultivation.entity.SwordProjectileEntity;
import com.friday.cultivation.event.CapabilityEvents;
import com.friday.cultivation.event.CoreSelfDestructHandler;
import com.friday.cultivation.event.LifeBalanceHandler;
import com.friday.cultivation.event.PalmThunderHandler;
import com.friday.cultivation.event.QiTransferTickHandler;
import com.friday.cultivation.event.RealmPressureHandler;
import com.friday.cultivation.event.SectCombatHandler;
import com.friday.cultivation.event.SoulHookHandler;
import com.friday.cultivation.event.SoulStateHandler;
import com.friday.cultivation.event.SpiritLockHandler;
import com.friday.cultivation.event.TimeStasisHandler;
import com.friday.cultivation.event.VoidEscapeHandler;
import com.friday.cultivation.network.ClientOnlyGlowPacket;
import com.friday.cultivation.registry.ModEffects;
import com.friday.cultivation.util.SpellScalingHelper;
import java.util.List;
import java.util.Optional;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundLevelParticlesPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

@Mod.EventBusSubscriber(modid="friday_cultivation")
public final class ChargeableSpellHandler {
    private static final int BASE_DRAIN = 5;
    private static final int SOUND_INTERVAL = 15;
    private static final int TIME_STASIS_DRAIN_MULTIPLIER = 20;
    private static final long BUDDHA_FIRE_LOTUS_READY_QI = 10000L;
    private static final long CORE_SELF_DESTRUCT_READY_QI = 1000L;
    private static final double BUDDHA_FIRE_LOTUS_TARGET_RANGE = 96.0;
    private static final double BUDDHA_FIRE_LOTUS_TARGET_INFLATE = 0.75;
    private static final DustParticleOptions BUDDHA_CYAN_FIRE_DUST = new DustParticleOptions(new Vector3f(0.12f, 1.0f, 0.78f), 1.2f);
    private static final DustParticleOptions BUDDHA_WHITE_FIRE_DUST = new DustParticleOptions(new Vector3f(0.95f, 0.98f, 1.0f), 1.1f);
    private static final DustParticleOptions CORE_GOLD_DUST = new DustParticleOptions(new Vector3f(1.0f, 0.75f, 0.12f), 1.35f);

    private ChargeableSpellHandler() {
    }

    private static long computeDrain(int chargingTicks) {
        long t = (long)chargingTicks / 20L;
        return 5L + t * t;
    }

    private static long computeHeavenPiercingConeDrain(int chargingTicks) {
        long seconds = Math.max(0L, (long)chargingTicks / 20L);
        return 8L + (long)Math.max(0, chargingTicks) / 10L + seconds * seconds * 8L;
    }

    private static long computeBuddhaFireLotusDrain(int chargingTicks) {
        long seconds = Math.max(0L, (long)chargingTicks / 20L);
        return 45L + (long)Math.max(0, chargingTicks) * 2L + seconds * seconds * 18L;
    }

    private static void tickTimeStasisCharge(ServerPlayer player, CultivationData data) {
        long cap = TimeStasisHandler.domainChargeQi();
        long curQi = data.getCurrentQi();
        if (curQi <= 0L) {
            ChargeableSpellHandler.fireChargedSpell(player);
            return;
        }
        long remaining = Math.max(0L, cap - data.getChargedQi());
        if (remaining <= 0L) {
            data.setChargedQi(cap);
            ChargeableSpellHandler.fireChargedSpell(player);
            return;
        }
        long requested = Math.min(ChargeableSpellHandler.computeDrain(data.getChargingTicks()) * 20L, remaining);
        long actualDrain = TechniqueBonusHelper.applySpellQiCostMultiplier((Player)player, Spell.TIME_STASIS, requested);
        long drain = Math.min(actualDrain, curQi);
        if (actualDrain <= 0L) {
            drain = 0L;
        }
        data.setCurrentQi(curQi - drain);
        long charged = actualDrain <= 0L ? requested : requested * drain / actualDrain;
        data.addChargedQi(Math.max(1L, charged));
        data.incrementChargingTicks();
        ChargeableSpellHandler.spawnTimeStasisChargeVisuals(player, data);
        if (data.getChargedQi() >= cap) {
            data.setChargedQi(cap);
            ChargeableSpellHandler.fireChargedSpell(player);
            return;
        }
        CapabilityEvents.syncToClient(player);
    }

    private static void spawnTimeStasisChargeVisuals(ServerPlayer player, CultivationData data) {
        ServerLevel level = player.serverLevel();
        ClientOnlyGlowPacket.send(player, player.getId(), 5);
        if (player.tickCount % 3 == 0) {
            double spread = 0.35 + Math.min(1.0, (double)data.getChargedQi() / (double)TimeStasisHandler.domainChargeQi());
            level.sendParticles((ParticleOptions)ParticleTypes.ASH, player.getX(), player.getY() + (double)player.getBbHeight() * 0.55, player.getZ(), 8, spread, (double)player.getBbHeight() * 0.25, spread, 0.01);
        }
        if (player.tickCount % 20 == 0) {
            float progress = Math.min(1.0f, (float)data.getChargedQi() / (float)TimeStasisHandler.domainChargeQi());
            level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.AMETHYST_BLOCK_RESONATE, SoundSource.PLAYERS, 0.45f, 0.65f + progress * 0.65f);
        }
    }

    private static void tickCoreSelfDestructCharge(ServerPlayer player, CultivationData data) {
        long remaining = Math.max(0L, 1000L - data.getChargedQi());
        if (remaining <= 0L) {
            data.setChargedQi(1000L);
            ChargeableSpellHandler.fireChargedSpell(player);
            return;
        }
        long gain = Math.min(remaining, Math.max(1L, 10L));
        data.addChargedQi(gain);
        data.incrementChargingTicks();
        ChargeableSpellHandler.spawnCoreSelfDestructChargeVisuals(player, data);
        if (data.getChargedQi() >= 1000L) {
            data.setChargedQi(1000L);
            ChargeableSpellHandler.fireChargedSpell(player);
            return;
        }
        CapabilityEvents.syncToClient(player);
    }

    private static void spawnCoreSelfDestructChargeVisuals(ServerPlayer player, CultivationData data) {
        ServerLevel level = player.serverLevel();
        float progress = Math.min(1.0f, (float)data.getChargedQi() / 1000.0f);
        Vec3 look = player.getLookAngle().normalize();
        Vec3 chest = player.position().add(0.0, (double)player.getBbHeight() * 0.58, 0.0).add(look.scale(0.18 + (double)progress * 0.22));
        int dustCount = 4 + (int)(progress * 10.0f);
        level.sendParticles((ParticleOptions)CORE_GOLD_DUST, chest.x, chest.y, chest.z, dustCount, 0.12 + (double)progress * 0.2, 0.1 + (double)progress * 0.18, 0.12 + (double)progress * 0.2, 0.01);
        if (progress > 0.35f) {
            int rayCount = 1 + (int)(progress * 4.0f);
            for (int i = 0; i < rayCount; ++i) {
                double angle = (double)player.tickCount * 0.27 + (double)i * Math.PI * 2.0 / (double)rayCount;
                double radius = 0.16 + (double)progress * 0.45;
                level.sendParticles((ParticleOptions)ParticleTypes.END_ROD, chest.x + Math.cos(angle) * radius, chest.y + (player.getRandom().nextDouble() - 0.5) * radius, chest.z + Math.sin(angle) * radius, 1, 0.02, 0.02, 0.02, 0.01 + (double)progress * 0.02);
            }
        }
        if (player.tickCount % 12 == 0) {
            level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.AMETHYST_BLOCK_RESONATE, SoundSource.PLAYERS, 0.42f + progress * 0.32f, 0.65f + progress * 0.95f);
        }
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        int interval;
        int ticks;
        if (event.phase != TickEvent.Phase.END) {
            return;
        }
        Player player = event.player;
        if (!(player instanceof ServerPlayer)) {
            return;
        }
        ServerPlayer player2 = (ServerPlayer)player;
        CultivationData data = CultivationCapability.get((Player)player2).orElse(null);
        if (data == null || !data.isCharging()) {
            return;
        }
        Spell spell = Spell.byId(data.getChargingSpellId());
        if (spell == null || !spell.chargeable()) {
            data.clearCharging();
            CapabilityEvents.syncToClient(player2);
            return;
        }
        if (SoulHookHandler.isActionLocked((Entity)player2)) {
            data.clearCharging();
            player2.displayClientMessage((Component)Component.translatable((String)"message.friday_cultivation.soul_hook_spell.action_locked"), true);
            CapabilityEvents.syncToClient(player2);
            return;
        }
        if (SpiritLockHandler.isEntityLocked((Entity)player2)) {
            data.clearCharging();
            player2.displayClientMessage((Component)Component.translatable((String)"message.friday_cultivation.spirit_lock.caster_locked"), true);
            CapabilityEvents.syncToClient(player2);
            return;
        }
        if (player2.hasEffect((MobEffect)ModEffects.MERIDIAN_FROZEN.get())) {
            data.clearCharging();
            player2.displayClientMessage((Component)Component.translatable((String)"message.friday_cultivation.meridian_frozen.caster_locked"), true);
            CapabilityEvents.syncToClient(player2);
            return;
        }
        if (spell == Spell.QI_TRANSFER) {
            QiTransferTickHandler.tick(player2, data);
            CapabilityEvents.syncToClient(player2);
            return;
        }
        if (spell == Spell.TIME_STASIS) {
            ChargeableSpellHandler.tickTimeStasisCharge(player2, data);
            return;
        }
        if (spell == Spell.TAISHANG_LIFE_BALANCE) {
            LifeBalanceHandler.tickChannel(player2, data);
            return;
        }
        if (spell == Spell.PALM_THUNDER) {
            PalmThunderHandler.tickChannel(player2, data);
            return;
        }
        if (spell == Spell.REALM_PRESSURE) {
            RealmPressureHandler.tickExpansion(player2, data);
            return;
        }
        if (spell == Spell.CORE_SELF_DESTRUCT) {
            ChargeableSpellHandler.tickCoreSelfDestructCharge(player2, data);
            return;
        }
        if (spell == Spell.VOID_ESCAPE) {
            VoidEscapeHandler.tickCharge(player2, data);
            return;
        }
        long curQi = data.getCurrentQi();
        if (curQi <= 0L) {
            if (spell == Spell.BUDDHA_FIRE_LOTUS) {
                if (data.getChargedQi() >= 10000L) {
                    ChargeableSpellHandler.fireChargedSpell(player2);
                    return;
                }
                data.clearCharging();
                player2.displayClientMessage((Component)Component.translatable((String)"message.friday_cultivation.buddha_fire_lotus.no_qi_channel"), true);
                CapabilityEvents.syncToClient(player2);
                return;
            }
            ChargeableSpellHandler.fireChargedSpell(player2);
            return;
        }
        long requested = spell == Spell.HEAVEN_PIERCING_CONE ? ChargeableSpellHandler.computeHeavenPiercingConeDrain(data.getChargingTicks()) : (spell == Spell.BUDDHA_FIRE_LOTUS ? ChargeableSpellHandler.computeBuddhaFireLotusDrain(data.getChargingTicks()) : ChargeableSpellHandler.computeDrain(data.getChargingTicks()));
        if (spell == Spell.SKY_SPLITTING_SWORD_AURA) {
            requested *= 20L;
        }
        if (spell == Spell.STAR_FALL) {
            requested *= 2L;
        }
        long actualDrain = TechniqueBonusHelper.applySpellQiCostMultiplier((Player)player2, spell, requested);
        long drain = Math.min(actualDrain, curQi);
        data.setCurrentQi(curQi - drain);
        long charge = actualDrain == 0L ? 0L : requested * drain / actualDrain;
        data.addChargedQi(charge);
        data.incrementChargingTicks();
        if (spell == Spell.BUDDHA_FIRE_LOTUS && data.getCurrentQi() <= 0L && data.getChargedQi() >= 10000L) {
            ChargeableSpellHandler.fireChargedSpell(player2);
            return;
        }
        if (player2.tickCount % 15 == 0) {
            player2.serverLevel().playSound(null, player2.getX(), player2.getY(), player2.getZ(), SoundEvents.LAVA_POP, SoundSource.PLAYERS, 0.5f, 0.6f + Math.min(0.8f, (float)data.getChargedQi() / 5000.0f));
        }
        if (spell == Spell.SWORD_CONVERGENCE && (ticks = data.getChargingTicks()) % (interval = Math.max(1, 20 - ticks / 8)) == 0) {
            double spawnRadius = Math.min(50.0, 2.0 + (double)ticks * 0.15);
            ChargeableSpellHandler.spawnSwordNearPlayer(player2, spawnRadius);
        }
        if (spell == Spell.STAR_FALL) {
            ticks = data.getChargingTicks();
            if (data.getChargedQi() >= 10000L) {
                ChargeableSpellHandler.fireChargedSpell(player2);
                return;
            }
            if (ticks % 8 == 0) {
                ChargeableSpellHandler.spawnRainMeteor(player2);
            }
        }
        if (spell == Spell.SKY_SPLITTING_SWORD_AURA) {
            ChargeableSpellHandler.spawnChargingVisuals(player2, data, 3000L);
        }
        if (spell == Spell.HEAVEN_PIERCING_CONE) {
            ChargeableSpellHandler.spawnHeavenPiercingConeChargeVisuals(player2, data);
        }
        if (spell == Spell.BUDDHA_FIRE_LOTUS) {
            ChargeableSpellHandler.spawnBuddhaFireLotusChargeVisuals(player2, data);
        }
        CapabilityEvents.syncToClient(player2);
    }

    public static void fireChargedSpell(ServerPlayer player) {
        ChargeableSpellHandler.fireChargedSpell(player, -1);
    }

    public static void fireChargedSpell(ServerPlayer player, int buddhaFireLotusTargetId) {
        CultivationData data = CultivationCapability.get((Player)player).orElse(null);
        if (data == null) {
            return;
        }
        Spell spell = Spell.byId(data.getChargingSpellId());
        long charged = data.getChargedQi();
        if (spell == null) {
            data.clearCharging();
            CapabilityEvents.syncToClient(player);
            return;
        }
        if (spell == Spell.QI_TRANSFER) {
            data.clearCharging();
            CapabilityEvents.syncToClient(player);
            return;
        }
        if (spell == Spell.TIME_STASIS) {
            TimeStasisHandler.release(player, charged);
            data.clearCharging();
            CapabilityEvents.syncToClient(player);
            return;
        }
        if (spell == Spell.TAISHANG_LIFE_BALANCE) {
            LifeBalanceHandler.finishChannel(player, data);
            return;
        }
        if (spell == Spell.PALM_THUNDER) {
            PalmThunderHandler.finishChannelOrTap(player, data);
            return;
        }
        if (spell == Spell.REALM_PRESSURE) {
            RealmPressureHandler.finishExpansionOrTap(player, data);
            return;
        }
        if (spell == Spell.CORE_SELF_DESTRUCT) {
            if (charged < 1000L) {
                data.clearCharging();
                CapabilityEvents.syncToClient(player);
                player.displayClientMessage((Component)Component.translatable((String)"message.friday_cultivation.core_self_destruct.not_ready"), true);
                return;
            }
            data.clearCharging();
            CapabilityEvents.syncToClient(player);
            CoreSelfDestructHandler.cast(player);
            return;
        }
        if (spell == Spell.VOID_ESCAPE) {
            data.clearCharging();
            CapabilityEvents.syncToClient(player);
            return;
        }
        ServerLevel level = player.serverLevel();
        Vec3 look = player.getLookAngle();
        if (spell == Spell.GREAT_FIREBALL) {
            QiElement fire = QiElement.FIRE;
            double techMult = TechniqueBonusHelper.spellElementMultiplier((Player)player, fire);
            double mult = techMult;
            long effective = Math.max(charged, Math.round((double)charged * mult));
            GreatFireballEntity ball = new GreatFireballEntity((Level)level, (LivingEntity)player, look.x, look.y, look.z, (int)Math.min(Integer.MAX_VALUE, effective));
            ball.setDamageMultiplier(SpellScalingHelper.damageMultiplier((LivingEntity)player, spell));
            float diameter = ball.getRenderScale();
            double spawnDist = Math.max(3.0, (double)diameter * 0.7);
            ball.setPos(player.getX() + look.x * spawnDist, player.getEyeY() + look.y * spawnDist - 0.3, player.getZ() + look.z * spawnDist);
            double initialSpeed = Math.min(6.0, 1.0 + Math.sqrt(diameter) * 0.5);
            ball.setDeltaMovement(look.scale(initialSpeed));
            double n = look.length();
            if (n > 0.0) {
                ball.setManualPower(look.x / n * 0.1, look.y / n * 0.1, look.z / n * 0.1);
            }
            level.addFreshEntity((Entity)ball);
            PhysiqueBonusHelper.onSpellCast(player, spell);
            player.displayClientMessage((Component)Component.translatable((String)"message.friday_cultivation.cast.charged_fired", (Object[])new Object[]{Spell.GREAT_FIREBALL.displayName(), charged}), true);
        }
        if (spell == Spell.HEAVEN_PIERCING_CONE) {
            ChargeableSpellHandler.fireHeavenPiercingCone(player, data.getChargingTicks());
            PhysiqueBonusHelper.onSpellCast(player, spell);
            player.displayClientMessage((Component)Component.translatable((String)"message.friday_cultivation.cast.charged_fired", (Object[])new Object[]{Spell.HEAVEN_PIERCING_CONE.displayName(), charged}), true);
        }
        if (spell == Spell.SKY_SPLITTING_SWORD_AURA) {
            boolean isMega = charged >= 3000L;
            Vec3 spawnPos = isMega ? player.getEyePosition().add(look.scale(-1.5)) : player.getEyePosition().add(look.scale(1.5));
            SkySplittingSwordAuraEntity aura = new SkySplittingSwordAuraEntity((Level)level, (LivingEntity)player, spawnPos, look.normalize(), isMega);
            if (isMega) {
                double pct = (double)charged / 3000.0;
                aura.setDamage(SpellScalingHelper.scaledDamageFloat((LivingEntity)player, spell, (float)(2000.0 * pct)));
                if (charged >= 30000L) {
                    aura.setTrueDamageRatio(0.1f);
                }
            } else {
                aura.setDamage(SpellScalingHelper.scaledDamageFloat((LivingEntity)player, spell, 1000.0f));
            }
            level.addFreshEntity((Entity)aura);
            PhysiqueBonusHelper.onSpellCast(player, spell);
            level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.TRIDENT_THROW, SoundSource.PLAYERS, isMega ? 1.5f : 1.0f, isMega ? 0.7f : 1.2f);
        }
        if (spell == Spell.STAR_FALL) {
            if (charged >= 10000L) {
                ChargeableSpellHandler.spawnMegaMeteor(player);
                PhysiqueBonusHelper.onSpellCast(player, spell);
                player.displayClientMessage((Component)Component.translatable((String)"message.friday_cultivation.cast.charged_fired", (Object[])new Object[]{Spell.STAR_FALL.displayName(), charged}), true);
            } else if (charged <= (long)Spell.STAR_FALL.qiCost() + 10L) {
                ChargeableSpellHandler.spawnSingleMeteorAtCrosshair(player, 0, 2.0f);
                PhysiqueBonusHelper.onSpellCast(player, spell);
            }
        }
        if (spell == Spell.SWORD_CONVERGENCE) {
            ChargeableSpellHandler.releasePlayerSwords(player);
            if (charged <= (long)Spell.SWORD_CONVERGENCE.qiCost() + 10L) {
                Vec3 spawnPos = new Vec3(player.getX() + look.x * 1.5 + (player.getRandom().nextDouble() - 0.5), player.getEyeY() + 0.5, player.getZ() + look.z * 1.5 + (player.getRandom().nextDouble() - 0.5));
                Vec3 targetPos = ChargeableSpellHandler.raycastCrosshairTarget(player);
                SwordProjectileEntity sword = new SwordProjectileEntity((Level)level, (LivingEntity)player, spawnPos, targetPos, false);
                double swordMult = SpellScalingHelper.damageMultiplier((LivingEntity)player, spell);
                sword.setDirectHitDamage(SwordProjectileEntity.scaledConvergenceDamage(spell.damage(), swordMult));
                level.addFreshEntity((Entity)sword);
            }
            PhysiqueBonusHelper.onSpellCast(player, spell);
        }
        if (spell == Spell.BUDDHA_FIRE_LOTUS) {
            if (charged < 10000L) {
                player.displayClientMessage((Component)Component.translatable((String)"message.friday_cultivation.buddha_fire_lotus.not_ready"), true);
            } else {
                ChargeableSpellHandler.fireBuddhaFireLotus(player, charged, buddhaFireLotusTargetId);
                player.displayClientMessage((Component)Component.translatable((String)"message.friday_cultivation.cast.charged_fired", (Object[])new Object[]{Spell.BUDDHA_FIRE_LOTUS.displayName(), charged}), true);
            }
        }
        data.clearCharging();
        CapabilityEvents.syncToClient(player);
        level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.GENERIC_EXPLODE, SoundSource.PLAYERS, 1.0f, 0.7f);
    }

    private static void fireHeavenPiercingCone(ServerPlayer player, int chargeTicks) {
        ServerLevel level = player.serverLevel();
        Vec3 look = player.getLookAngle().normalize();
        if (look.lengthSqr() < 1.0E-6) {
            look = new Vec3(0.0, 0.0, 1.0);
        }
        boolean tapShot = chargeTicks <= 3;
        int effectiveChargeTicks = tapShot ? 0 : chargeTicks;
        int stage = tapShot ? 1 : HeavenPiercingConeEntity.stageForChargeTicks(effectiveChargeTicks);
        double velocity = tapShot ? 3.4 : HeavenPiercingConeEntity.velocityForChargeTicks(effectiveChargeTicks);
        double baseDamage = tapShot ? (double)Spell.HEAVEN_PIERCING_CONE.damage() : (double)Spell.HEAVEN_PIERCING_CONE.damage() * Math.pow(velocity, 1.5);
        float damage = (float)Math.max(1.0, SpellScalingHelper.scaledDamageDouble((LivingEntity)player, Spell.HEAVEN_PIERCING_CONE, baseDamage));
        HeavenPiercingConeEntity cone = new HeavenPiercingConeEntity((Level)level, (LivingEntity)player);
        cone.configure(damage, velocity, stage, effectiveChargeTicks, tapShot);
        Vec3 spawnPos = HeavenPiercingConeEntity.safeSideLaunchPosition((Level)level, (LivingEntity)player, look);
        Vec3 targetPos = ChargeableSpellHandler.raycastCrosshairTarget(player);
        Vec3 launchDir = HeavenPiercingConeEntity.aimDirectionFromSide(spawnPos, targetPos, look);
        cone.setPos(spawnPos.x, spawnPos.y, spawnPos.z);
        cone.setDeltaMovement(launchDir.scale(velocity));
        level.addFreshEntity((Entity)cone);
        level.playSound(null, player.getX(), player.getY(), player.getZ(), stage >= 4 ? SoundEvents.BLAZE_SHOOT : SoundEvents.TRIDENT_THROW, SoundSource.PLAYERS, stage >= 4 ? 1.15f : 0.75f, stage >= 4 ? 1.65f : 1.25f);
    }

    private static void fireBuddhaFireLotus(ServerPlayer player, long chargedQi, int targetId) {
        ServerLevel level = player.serverLevel();
        Vec3 look = player.getLookAngle().normalize();
        if (look.lengthSqr() < 1.0E-6) {
            look = new Vec3(0.0, 0.0, 1.0);
        }
        float chargePct = Math.max(1.0f, (float)chargedQi / 10000.0f);
        float baseDamage = (float)Spell.BUDDHA_FIRE_LOTUS.damage() * chargePct;
        float damage = SpellScalingHelper.scaledDamageFloat((LivingEntity)player, Spell.BUDDHA_FIRE_LOTUS, baseDamage);
        float radius = ChargeableSpellHandler.MthClamp(24.0f + (float)Math.sqrt(chargePct) * 11.0f, 30.0f, 72.0f);
        LivingEntity target = ChargeableSpellHandler.resolveBuddhaFireLotusTarget(player, targetId);
        BuddhaFireLotusEntity lotus = new BuddhaFireLotusEntity((Level)level, (LivingEntity)player);
        lotus.configure(damage, radius, (int)Math.min(Integer.MAX_VALUE, chargedQi), ChargeableSpellHandler.buddhaFireLotusRootFlags(player), target);
        Vec3 spawnPos = player.getEyePosition().add(look.scale(1.15)).add(0.0, -0.18, 0.0);
        lotus.setPos(spawnPos.x, spawnPos.y, spawnPos.z);
        Vec3 targetPos = target == null ? ChargeableSpellHandler.raycastCrosshairTarget(player) : target.getEyePosition();
        Vec3 launch = targetPos.multiply(spawnPos);
        if (launch.lengthSqr() < 1.0E-6) {
            launch = look;
        }
        lotus.setDeltaMovement(launch.normalize().scale(0.68));
        level.addFreshEntity((Entity)lotus);
        level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 1.0f, 0.55f);
    }

    private static int buddhaFireLotusRootFlags(ServerPlayer player) {
        int flags = 0;
        if (SpiritRootBonusHelper.hasRootElement((Player)player, QiElement.METAL)) {
            flags |= 1;
        }
        if (SpiritRootBonusHelper.hasRootElement((Player)player, QiElement.WOOD)) {
            flags |= 2;
        }
        if (SpiritRootBonusHelper.hasRootElement((Player)player, QiElement.WATER)) {
            flags |= 4;
        }
        if (SpiritRootBonusHelper.hasRootElement((Player)player, QiElement.FIRE)) {
            flags |= 8;
        }
        if (SpiritRootBonusHelper.hasRootElement((Player)player, QiElement.EARTH)) {
            flags |= 0x10;
        }
        if (SpiritRootBonusHelper.hasPureRoot((Player)player)) {
            flags |= 0x20;
        }
        return flags;
    }

    private static float MthClamp(float value, float min, float max) {
        return Math.max(min, Math.min(max, value));
    }

    @Nullable
    private static LivingEntity resolveBuddhaFireLotusTarget(ServerPlayer player, int targetId) {
        LivingEntity living;
        Entity entity;
        if (targetId >= 0 && (entity = player.serverLevel().getEntity(targetId)) instanceof LivingEntity && ChargeableSpellHandler.isValidBuddhaFireLotusTarget(player, living = (LivingEntity)entity)) {
            return living;
        }
        return ChargeableSpellHandler.raycastLivingTarget(player, 96.0);
    }

    private static boolean isValidBuddhaFireLotusTarget(ServerPlayer player, LivingEntity target) {
        if (target == player || !target.isAlive() || !target.isPickable()) {
            return false;
        }
        if (!SoulStateHandler.canOrdinaryAffect((Entity)player, (Entity)target)) {
            return false;
        }
        if (!SectCombatHandler.canTargetOffensiveEffect((LivingEntity)player, target)) {
            return false;
        }
        Vec3 eye = player.getEyePosition();
        Vec3 look = player.getLookAngle().normalize();
        if (look.lengthSqr() < 1.0E-6) {
            return false;
        }
        Vec3 end = eye.add(look.scale(96.0));
        double blockDist = ChargeableSpellHandler.blockDistance(player, eye, end, 96.0);
        Optional hit = target.getBoundingBox().inflate(0.75).clip(eye, end);
        return hit.isPresent() && eye.distanceTo((Vec3)hit.get()) <= blockDist + 0.35;
    }

    @Nullable
    private static LivingEntity raycastLivingTarget(ServerPlayer player, double maxDist) {
        Vec3 eye = player.getEyePosition();
        Vec3 end = eye.add(player.getLookAngle().scale(maxDist));
        double blockDist = ChargeableSpellHandler.blockDistance(player, eye, end, maxDist);
        AABB scan = new AABB(eye, end).inflate(2.75);
        LivingEntity best = null;
        double bestDist = blockDist;
        for (LivingEntity entity : player.serverLevel().getEntitiesOfClass(LivingEntity.class, scan, e -> e != player && e.isAlive() && e.isPickable() && SoulStateHandler.canOrdinaryAffect((Entity)player, (Entity)e) && SectCombatHandler.canTargetOffensiveEffect((LivingEntity)player, e))) {
            double dist;
            Optional hit = entity.getBoundingBox().inflate(0.75).clip(eye, end);
            if (hit.isEmpty() || !((dist = eye.distanceTo((Vec3)hit.get())) < bestDist)) continue;
            bestDist = dist;
            best = entity;
        }
        return best;
    }

    private static double blockDistance(ServerPlayer player, Vec3 eye, Vec3 end, double maxDist) {
        BlockHitResult hit = player.serverLevel().clip(new ClipContext(eye, end, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, (Entity)player));
        return hit.getType() == HitResult.Type.MISS ? maxDist : eye.distanceTo(hit.getLocation());
    }

    private static void spawnHeavenPiercingConeChargeVisuals(ServerPlayer player, CultivationData data) {
        ServerLevel level = player.serverLevel();
        Vec3 look = player.getLookAngle().normalize();
        if (look.lengthSqr() < 1.0E-6) {
            return;
        }
        Vec3 center = HeavenPiercingConeEntity.previewPosition((LivingEntity)player, look);
        int ticks = data.getChargingTicks();
        int stage = HeavenPiercingConeEntity.stageForChargeTicks(ticks);
        level.sendParticles((ParticleOptions)new BlockParticleOption(ParticleTypes.FALLING_DUST, Blocks.DEEPSLATE.defaultBlockState()), center.x, center.y, center.z, stage >= 3 ? 3 : 1, 0.18, 0.16, 0.18, 0.02);
        if (stage == 1) {
            level.sendParticles((ParticleOptions)ParticleTypes.DRIPPING_WATER, center.x, center.y - 0.1, center.z, 2, 0.18, 0.12, 0.18, 0.0);
        } else {
            level.sendParticles((ParticleOptions)ParticleTypes.CLOUD, center.x, center.y, center.z, stage >= 3 ? 8 : 5, 0.22, 0.18, 0.22, 0.02);
        }
        if (stage >= 3) {
            level.sendParticles((ParticleOptions)ParticleTypes.CLOUD, center.x + look.x * 0.35, center.y + look.y * 0.35, center.z + look.z * 0.35, stage >= 4 ? 7 : 3, 0.12, 0.1, 0.12, 0.015);
        }
        if (player.tickCount % 15 == 0) {
            level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.AMETHYST_BLOCK_RESONATE, SoundSource.PLAYERS, stage >= 4 ? 0.58f : 0.42f, 0.65f + Math.min(1.2f, (float)ticks / 80.0f));
        }
    }

    private static void spawnBuddhaFireLotusChargeVisuals(ServerPlayer player, CultivationData data) {
        ServerLevel level = player.serverLevel();
        Vec3 look = player.getLookAngle().normalize();
        if (look.lengthSqr() < 1.0E-6) {
            return;
        }
        Vec3 center = player.getEyePosition().add(look.scale(1.2)).add(0.0, -0.18, 0.0);
        Vec3 right = new Vec3(-look.z, 0.0, look.x);
        if (right.lengthSqr() < 1.0E-6) {
            right = new Vec3(1.0, 0.0, 0.0);
        }
        right = right.normalize();
        float progress = Math.min(1.0f, (float)data.getChargedQi() / 10000.0f);
        double handOffset = progress >= 1.0f ? 0.32 : 0.74 * (1.0 - (double)progress) + 0.16;
        Vec3 handBase = center.add(0.0, -0.42, 0.0);
        Vec3 leftFlame = handBase.multiply(right.scale(handOffset));
        Vec3 rightFlame = handBase.add(right.scale(handOffset));
        level.sendParticles((ParticleOptions)BUDDHA_CYAN_FIRE_DUST, leftFlame.x, leftFlame.y + 0.04, leftFlame.z, 4, 0.07, 0.12, 0.07, 0.0);
        level.sendParticles((ParticleOptions)ParticleTypes.SOUL_FIRE_FLAME, leftFlame.x, leftFlame.y, leftFlame.z, 5, 0.07, 0.12, 0.07, 0.012);
        level.sendParticles((ParticleOptions)BUDDHA_WHITE_FIRE_DUST, rightFlame.x, rightFlame.y + 0.04, rightFlame.z, 4, 0.07, 0.12, 0.07, 0.0);
        level.sendParticles((ParticleOptions)ParticleTypes.SMALL_FLAME, rightFlame.x, rightFlame.y, rightFlame.z, 4, 0.07, 0.1, 0.07, 0.01);
        level.sendParticles((ParticleOptions)ParticleTypes.END_ROD, rightFlame.x, rightFlame.y, rightFlame.z, 2, 0.06, 0.09, 0.06, 0.008);
        if (progress >= 1.0f) {
            level.sendParticles((ParticleOptions)ParticleTypes.SOUL_FIRE_FLAME, center.x, center.y, center.z, 5, 0.16, 0.12, 0.16, 0.015);
            level.sendParticles((ParticleOptions)ParticleTypes.ENCHANT, center.x, center.y, center.z, 12, 0.2, 0.12, 0.2, 0.04);
        }
        if (player.tickCount % 15 == 0) {
            level.playSound(null, player.getX(), player.getY(), player.getZ(), progress >= 1.0f ? SoundEvents.BEACON_POWER_SELECT : SoundEvents.AMETHYST_BLOCK_RESONATE, SoundSource.PLAYERS, progress >= 1.0f ? 0.65f : 0.42f, 0.55f + progress * 0.65f);
        }
    }

    private static void spawnSwordNearPlayer(ServerPlayer sp, double spawnRadius) {
        ServerLevel level = sp.serverLevel();
        double angle = sp.getRandom().nextDouble() * Math.PI * 2.0;
        double dist = spawnRadius * (0.5 + sp.getRandom().nextDouble() * 0.5);
        double yOffset = (sp.getRandom().nextDouble() - 0.3) * spawnRadius * 0.8;
        Vec3 spawnPos = new Vec3(sp.getX() + Math.cos(angle) * dist, sp.getY() + 1.5 + yOffset, sp.getZ() + Math.sin(angle) * dist);
        Vec3 targetPos = ChargeableSpellHandler.raycastCrosshairTarget(sp);
        SwordProjectileEntity sword = new SwordProjectileEntity((Level)level, (LivingEntity)sp, spawnPos, targetPos, true);
        double swordMult = SpellScalingHelper.damageMultiplier((LivingEntity)sp, Spell.SWORD_CONVERGENCE);
        sword.setDirectHitDamage(SwordProjectileEntity.scaledConvergenceDamage(Spell.SWORD_CONVERGENCE.damage(), swordMult));
        level.addFreshEntity((Entity)sword);
    }

    private static void spawnChargingVisuals(ServerPlayer sp, CultivationData data, long cap) {
        ServerLevel server = sp.serverLevel();
        long chargedQi = data.getChargedQi();
        float frac = Math.min(1.0f, (float)chargedQi / (float)cap);
        boolean ready = chargedQi >= cap;
        int t = sp.tickCount;
        ClientOnlyGlowPacket.send(sp, sp.getId(), 5);
        if (!ready && frac > 0.0f) {
            int flashCount = (int)(frac * 6.0f);
            float bodyHeight = sp.getBbHeight();
            for (int i = 0; i < flashCount; ++i) {
                double dx = (sp.getRandom().nextDouble() - 0.5) * 0.5;
                double dy = sp.getRandom().nextDouble() * (double)bodyHeight;
                double dz = (sp.getRandom().nextDouble() - 0.5) * 0.5;
                ChargeableSpellHandler.broadcastParticleExcept(server, sp, (ParticleOptions)ParticleTypes.FLASH, sp.getX() + dx, sp.getY() + dy, sp.getZ() + dz);
            }
            if (frac > 0.3f) {
                Vec3 look = sp.getLookAngle();
                Vec3 right = new Vec3(-look.z, 0.0, look.x).normalize();
                double swordX = sp.getX() + look.x * 0.5 + right.x * 0.4;
                double swordY = sp.getY() + (double)sp.getEyeHeight() - 0.3;
                double swordZ = sp.getZ() + look.z * 0.5 + right.z * 0.4;
                ChargeableSpellHandler.broadcastParticleExcept(server, sp, (ParticleOptions)ParticleTypes.FLASH, swordX, swordY, swordZ);
            }
        }
        if (ready) {
            if (t % 4 == 0) {
                float bodyHeight = sp.getBbHeight();
                for (int i = 0; i < 8; ++i) {
                    double dx = (sp.getRandom().nextDouble() - 0.5) * 0.6;
                    double dy = sp.getRandom().nextDouble() * (double)bodyHeight;
                    double dz = (sp.getRandom().nextDouble() - 0.5) * 0.6;
                    ChargeableSpellHandler.broadcastParticleExcept(server, sp, (ParticleOptions)ParticleTypes.FLASH, sp.getX() + dx, sp.getY() + dy, sp.getZ() + dz);
                }
            }
            if (t % 20 == 0) {
                server.playSound(null, sp.getX(), sp.getY(), sp.getZ(), SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 0.8f, 1.6f);
            }
        } else if (t % 30 == 0) {
            server.playSound(null, sp.getX(), sp.getY(), sp.getZ(), SoundEvents.BEACON_AMBIENT, SoundSource.PLAYERS, 0.3f, 1.5f + frac * 0.5f);
        }
    }

    private static void broadcastParticleExcept(ServerLevel level, ServerPlayer except, ParticleOptions particle, double x, double y, double z) {
        ClientboundLevelParticlesPacket packet = new ClientboundLevelParticlesPacket(particle, true, x, y, z, 0.0f, 0.0f, 0.0f, 0.0f, 1);
        for (ServerPlayer p : level.players()) {
            if (p == except || !(p.distanceToSqr(x, y, z) < 1024.0)) continue;
            p.connection.send((Packet)packet);
        }
    }

    private static Vec3 raycastCrosshairTarget(ServerPlayer player) {
        Vec3 eye = player.getEyePosition();
        Vec3 end = eye.add(player.getLookAngle().scale(1000.0));
        BlockHitResult hr = player.serverLevel().clip(new ClipContext(eye, end, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, (Entity)player));
        return hr.getType() != HitResult.Type.MISS ? hr.getLocation() : end;
    }

    private static void spawnSingleMeteorAtCrosshair(ServerPlayer player, int mode, float diameter) {
        Vec3 target = ChargeableSpellHandler.raycastCrosshairTarget(player);
        Vec3 spawnPos = new Vec3(player.getX(), player.getEyeY() + 60.0, player.getZ());
        MeteorEntity meteor = new MeteorEntity((Level)player.serverLevel(), (LivingEntity)player, spawnPos, target, mode, diameter);
        meteor.setDamageMultiplier(SpellScalingHelper.damageMultiplier((LivingEntity)player, Spell.STAR_FALL));
        player.serverLevel().addFreshEntity((Entity)meteor);
    }

    private static void spawnRainMeteor(ServerPlayer player) {
        Vec3 target = ChargeableSpellHandler.raycastCrosshairTarget(player);
        int ticks = CultivationCapability.get((Player)player).map(CultivationData::getChargingTicks).orElse(0);
        double scatter = 5.0 + Math.min(15.0, (double)ticks * 0.05);
        double dx = (player.getRandom().nextDouble() - 0.5) * 2.0 * scatter;
        double dz = (player.getRandom().nextDouble() - 0.5) * 2.0 * scatter;
        Vec3 actualTarget = target.add(dx, 0.0, dz);
        double spawnDx = (player.getRandom().nextDouble() - 0.5) * 4.0;
        double spawnDz = (player.getRandom().nextDouble() - 0.5) * 4.0;
        Vec3 spawnPos = new Vec3(player.getX() + spawnDx, player.getEyeY() + 60.0, player.getZ() + spawnDz);
        MeteorEntity meteor = new MeteorEntity((Level)player.serverLevel(), (LivingEntity)player, spawnPos, actualTarget, 1, 1.5f);
        meteor.setDamageMultiplier(SpellScalingHelper.damageMultiplier((LivingEntity)player, Spell.STAR_FALL));
        player.serverLevel().addFreshEntity((Entity)meteor);
    }

    private static void spawnMegaMeteor(ServerPlayer player) {
        Vec3 target = ChargeableSpellHandler.raycastCrosshairTarget(player);
        Vec3 spawnPos = new Vec3(target.x, target.y + 200.0, target.z);
        MeteorEntity meteor = new MeteorEntity((Level)player.serverLevel(), (LivingEntity)player, spawnPos, target, 2, 20.0f);
        meteor.setDamageMultiplier(SpellScalingHelper.damageMultiplier((LivingEntity)player, Spell.STAR_FALL));
        player.serverLevel().addFreshEntity((Entity)meteor);
        ChargeableSpellHandler.broadcastFarSound(player.serverLevel(), target, SoundEvents.LIGHTNING_BOLT_THUNDER, SoundSource.HOSTILE, 120.0f, 0.3f);
    }

    public static void broadcastFarSound(ServerLevel level, Vec3 pos, SoundEvent sound, SoundSource source, float volume, float pitch) {
        level.playSound(null, pos.x, pos.y, pos.z, sound, source, volume, pitch);
    }

    private static void releasePlayerSwords(ServerPlayer player) {
        ServerLevel level = player.serverLevel();
        AABB searchBox = player.getBoundingBox().inflate(100.0);
        List<SwordProjectileEntity> swords = level.getEntitiesOfClass(SwordProjectileEntity.class, searchBox, s -> s.isImmortal() && s.getOwnerUuid() != null && s.getOwnerUuid().equals(player.getUUID()));
        for (SwordProjectileEntity s2 : swords) {
            s2.setImmortal(false);
        }
    }
}
