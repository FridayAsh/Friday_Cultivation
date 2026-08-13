/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.BlockPos
 *  net.minecraft.server.level.ServerPlayer
 *  net.minecraft.sounds.SoundEvents
 *  net.minecraft.sounds.SoundSource
 *  net.minecraft.world.effect.MobEffects
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.item.enchantment.FrostWalkerEnchantment
 *  net.minecraft.world.level.Level
 *  net.minecraftforge.event.TickEvent$Phase
 *  net.minecraftforge.event.TickEvent$PlayerTickEvent
 *  net.minecraftforge.event.entity.living.MobEffectEvent$Applicable
 *  net.minecraftforge.event.entity.player.PlayerEvent$PlayerLoggedOutEvent
 *  net.minecraftforge.eventbus.api.Event$Result
 *  net.minecraftforge.eventbus.api.SubscribeEvent
 *  net.minecraftforge.fml.common.Mod$EventBusSubscriber
 */
package com.friday.cultivation.event;

import com.friday.cultivation.cultivation.CultivationCapability;
import com.friday.cultivation.cultivation.CultivationData;
import com.friday.cultivation.cultivation.spell.Spell;
import com.friday.cultivation.cultivation.technique.TechniqueBonusHelper;
import com.friday.cultivation.event.CapabilityEvents;
import com.friday.cultivation.event.RealmPressureHandler;
import com.friday.cultivation.event.SoulHookHandler;
import com.friday.cultivation.event.SpiritLockHandler;
import com.friday.cultivation.event.SwordFlightHandler;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.FrostWalkerEnchantment;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.MobEffectEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid="friday_cultivation")
public final class PassiveSpellHandler {
    private static final int SLOW_REGEN_INTERVAL = 100;
    private static final long SLOW_REGEN_QI_COST = 5L;
    private static final int FROST_WALKER_RADIUS = 2;
    private static final int BIGU_INTERVAL = 1200;
    private static final long BIGU_QI_COST = 10L;
    private static final float BIGU_SATURATION = 5.0f;
    private static final int QI_MENDING_INTERVAL_TICKS = 20;
    private static final long QI_MENDING_QI_PER_DURABILITY = 1L;
    private static final int QI_FLIGHT_DRAIN_INTERVAL_TICKS = 20;
    private static final long QI_FLIGHT_DRAIN_PER_SECOND = 25L;
    private static final float QI_FLIGHT_BASE_FLYING_SPEED = 0.05f;
    private static final int QI_FLIGHT_GROUND_RELEASE_TICKS = 4;
    private static final Map<UUID, Integer> QI_FLIGHT_FLYING_TICKS = new ConcurrentHashMap<UUID, Integer>();
    private static final Map<UUID, Integer> BIGU_PAID_UNTIL_TICK = new ConcurrentHashMap<UUID, Integer>();

    private PassiveSpellHandler() {
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
        ServerPlayer player2 = (ServerPlayer)player;
        if (player2.tickCount % 100 == 0) {
        }
        CultivationData data = CultivationCapability.get((Player)player2).orElse(null);
        if (data == null) {
            return;
        }
        if (SpiritLockHandler.isEntityLocked((Entity)player2)) {
            PassiveSpellHandler.revokeQiFlight(player2);
            SwordFlightHandler.stopIfActive(player2, data);
            return;
        }
        if (SoulHookHandler.isActionLocked((Entity)player2)) {
            QI_FLIGHT_FLYING_TICKS.remove(player2.getUUID());
            return;
        }
        if (RealmPressureHandler.isSuppressed((LivingEntity)player2)) {
            RealmPressureHandler.forceGrounded((LivingEntity)player2);
            PassiveSpellHandler.revokeQiFlight(player2);
            SwordFlightHandler.stopIfActive(player2, data, false);
        }
        PassiveSpellHandler.handleBigu(player2, data);
        SwordFlightHandler.tick(player2, data);
        PassiveSpellHandler.handleQiMending(player2, data);
        if (data.isSpellEnabled(Spell.SLOW_REGEN) && player2.tickCount % 100 == 0 && player2.getHealth() < player2.getMaxHealth()) {
            long cost = TechniqueBonusHelper.applySpellQiCostMultiplier((Player)player2, Spell.SLOW_REGEN, 5L);
            if (data.getCurrentQi() >= cost) {
                data.setCurrentQi(data.getCurrentQi() - cost);
                player2.heal(1.0f);
                CapabilityEvents.syncToClient(player2);
            }
        }
        if (data.isSpellEnabled(Spell.FROST_WALKER) && player2.onGround()) {
            BlockPos pos = player2.blockPosition();
            FrostWalkerEnchantment.onEntityMoved((LivingEntity)player2, (Level)player2.level(), (BlockPos)pos, (int)2);
        }
        PassiveSpellHandler.handleQiFlight(player2, data);
    }

    private static void handleQiFlight(ServerPlayer player, CultivationData data) {
        if (player.isCreative() || player.isSpectator()) {
            QI_FLIGHT_FLYING_TICKS.remove(player.getUUID());
            return;
        }
        if (RealmPressureHandler.isSuppressed((LivingEntity)player)) {
            PassiveSpellHandler.revokeQiFlight(player);
            return;
        }
        if (data.isVoidEscapeActive()) {
            QI_FLIGHT_FLYING_TICKS.remove(player.getUUID());
            return;
        }
        if (SwordFlightHandler.isActive(data)) {
            QI_FLIGHT_FLYING_TICKS.remove(player.getUUID());
            return;
        }
        boolean qiFlightEnabled = data.isSpellEnabled(Spell.QI_FLIGHT);
        boolean ghostFlightEnabled = PassiveSpellHandler.isGhostFlightActive(data);
        boolean hasQi = data.getCurrentQi() > 0L;
        // 自写飞行：不依赖 mayfly/flying（绕过 Caelus 飞行管理），
        // 灵气飞行启用且有灵气时悬浮（setNoGravity），客户端输入包控制运动
        boolean shouldHover = qiFlightEnabled || ghostFlightEnabled;
        if (shouldHover && hasQi) {
            // 落地检测：已离地过（曾悬浮）且回到地面且无上升动量 → 停止悬浮恢复重力
            boolean wasFlying = QI_FLIGHT_FLYING_TICKS.containsKey(player.getUUID());
            if (wasFlying && player.onGround() && player.getDeltaMovement().y <= 0.0) {
                QI_FLIGHT_FLYING_TICKS.remove(player.getUUID());
                if (player.isNoGravity()) {
                    player.setNoGravity(false);
                    player.onUpdateAbilities();
                }
                return;
            }
            if (!player.isNoGravity()) {
                player.setNoGravity(true);
                player.onUpdateAbilities();
            }
            player.fallDistance = 0.0f;
            if (qiFlightEnabled && !ghostFlightEnabled) {
                int flyingTicks = QI_FLIGHT_FLYING_TICKS.merge(player.getUUID(), 1, Integer::sum);
                if (flyingTicks >= 20) {
                    QI_FLIGHT_FLYING_TICKS.remove(player.getUUID());
                    long base = TechniqueBonusHelper.applySpellQiCostMultiplier((Player)player, Spell.QI_FLIGHT, 25L);
                    long drain = Math.min(base, data.getCurrentQi());
                    data.setCurrentQi(data.getCurrentQi() - drain);
                    CapabilityEvents.syncToClient(player);
                    if (data.getCurrentQi() <= 0L) {
                        player.setNoGravity(false);
                        player.onUpdateAbilities();
                        CapabilityEvents.syncToClient(player);
                    }
                }
            }
        } else {
            QI_FLIGHT_FLYING_TICKS.remove(player.getUUID());
            if (player.isNoGravity() && !SwordFlightHandler.isActive(data) && !data.isVoidEscapeActive()) {
                player.setNoGravity(false);
                player.onUpdateAbilities();
            }
        }
    }

    private static boolean isGhostFlightActive(CultivationData data) {
        return data.isSoulState() && data.isSpellEnabled(Spell.GHOST_FLIGHT);
    }

    public static boolean hasEnabledPassiveFlight(CultivationData data) {
        if (data == null) {
            return false;
        }
        return PassiveSpellHandler.isGhostFlightActive(data) || data.isSpellEnabled(Spell.QI_FLIGHT) && data.getCurrentQi() > 0L;
    }

    /**
     * 灵气飞行起飞（由客户端输入包触发）：授权飞行。
     */
    private static void revokeQiFlight(ServerPlayer player) {
        if (player.isCreative() || player.isSpectator()) {
            return;
        }
        if (player.isNoGravity()) {
            player.setNoGravity(false);
            player.onUpdateAbilities();
        }
        QI_FLIGHT_FLYING_TICKS.remove(player.getUUID());
    }

    private static void handleBigu(ServerPlayer player, CultivationData data) {
        UUID id = player.getUUID();
        if (!data.isSpellEnabled(Spell.BIGU)) {
            BIGU_PAID_UNTIL_TICK.remove(id);
            return;
        }
        int paidUntil = BIGU_PAID_UNTIL_TICK.getOrDefault(id, Integer.MIN_VALUE);
        if (player.tickCount > paidUntil) {
            long cost = TechniqueBonusHelper.applySpellQiCostMultiplier((Player)player, Spell.BIGU, 10L);
            if (data.getCurrentQi() < cost) {
                return;
            }
            data.setCurrentQi(data.getCurrentQi() - cost);
            BIGU_PAID_UNTIL_TICK.put(id, player.tickCount + 1200);
            CapabilityEvents.syncToClient(player);
        }
        if (player.getFoodData().getFoodLevel() < 20) {
            player.getFoodData().setFoodLevel(20);
        }
        if (player.getFoodData().getSaturationLevel() < 5.0f) {
            player.getFoodData().setSaturation(5.0f);
        }
    }

    private static void handleQiMending(ServerPlayer player, CultivationData data) {
        ItemStack stack;
        if (!data.isSpellEnabled(Spell.QI_MENDING)) {
            return;
        }
        if (player.isCreative() || player.isSpectator()) {
            return;
        }
        if (player.tickCount % 20 != 0) {
            return;
        }
        long remainingQi = data.getCurrentQi();
        if (remainingQi <= 0L) {
            return;
        }
        Set<ItemStack> seen = Collections.newSetFromMap(new IdentityHashMap());
        RepairResult result = PassiveSpellHandler.repairStack(player, player.getMainHandItem(), remainingQi, seen);
        result = PassiveSpellHandler.repairStack(player, player.getOffhandItem(), result.remainingQi(), seen, result.repaired());
        Iterator iterator = player.getArmorSlots().iterator();
        while (iterator.hasNext() && (result = PassiveSpellHandler.repairStack(player, stack = (ItemStack)iterator.next(), result.remainingQi(), seen, result.repaired())).remainingQi() > 0L) {
        }
        if (result.remainingQi() > 0L) {
            iterator = player.getInventory().items.iterator();
            while (iterator.hasNext() && (result = PassiveSpellHandler.repairStack(player, stack = (ItemStack)iterator.next(), result.remainingQi(), seen, result.repaired())).remainingQi() > 0L) {
            }
        }
        if (!result.repaired()) {
            return;
        }
        data.setCurrentQi(result.remainingQi());
        player.getInventory().setChanged();
        player.containerMenu.broadcastChanges();
        player.level().playSound(null, player.blockPosition(), SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 0.28f, 1.35f + player.getRandom().nextFloat() * 0.2f);
        CapabilityEvents.syncToClient(player);
    }

    private static RepairResult repairStack(ServerPlayer player, ItemStack stack, long remainingQi, Set<ItemStack> seen) {
        return PassiveSpellHandler.repairStack(player, stack, remainingQi, seen, false);
    }

    private static RepairResult repairStack(ServerPlayer player, ItemStack stack, long remainingQi, Set<ItemStack> seen, boolean repairedBefore) {
        if (remainingQi <= 0L || stack.isEmpty() || !seen.add(stack)) {
            return new RepairResult(remainingQi, repairedBefore);
        }
        if (!stack.isDamageableItem()) {
            return new RepairResult(remainingQi, repairedBefore);
        }
        int damage = stack.getDamageValue();
        if (damage <= 0) {
            return new RepairResult(remainingQi, repairedBefore);
        }
        int repair = PassiveSpellHandler.affordableQiMendingRepair(player, damage, remainingQi);
        if (repair <= 0) {
            return new RepairResult(remainingQi, repairedBefore);
        }
        long cost = PassiveSpellHandler.qiMendingCost(player, repair);
        stack.setDamageValue(Math.max(0, damage - repair));
        return new RepairResult(Math.max(0L, remainingQi - cost), true);
    }

    private static int affordableQiMendingRepair(ServerPlayer player, int maxRepair, long availableQi) {
        int low = 0;
        int high = maxRepair;
        while (low < high) {
            int mid = low + (high - low + 1) / 2;
            if (PassiveSpellHandler.qiMendingCost(player, mid) <= availableQi) {
                low = mid;
                continue;
            }
            high = mid - 1;
        }
        return low;
    }

    private static long qiMendingCost(ServerPlayer player, int durability) {
        if (durability <= 0) {
            return 0L;
        }
        long baseCost = Math.multiplyExact((long)durability, 1L);
        return TechniqueBonusHelper.applySpellQiCostMultiplier((Player)player, Spell.QI_MENDING, baseCost);
    }

    @SubscribeEvent
    public static void onPlayerLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        BIGU_PAID_UNTIL_TICK.remove(event.getEntity().getUUID());
    }

    @SubscribeEvent
    public static void onMobEffectApplicable(MobEffectEvent.Applicable event) {
        LivingEntity livingEntity = event.getEntity();
        if (!(livingEntity instanceof ServerPlayer)) {
            return;
        }
        ServerPlayer player = (ServerPlayer)livingEntity;
        if (event.getEffectInstance().getEffect() != MobEffects.POISON) {
            return;
        }
        CultivationCapability.get((Player)player).ifPresent(data -> {
            if (!SpiritLockHandler.isEntityLocked((Entity)player) && data.isSpellEnabled(Spell.POISON_IMMUNITY)) {
                event.setResult(Event.Result.DENY);
            }
        });
    }

    private record RepairResult(long remainingQi, boolean repaired) {
    }
}

