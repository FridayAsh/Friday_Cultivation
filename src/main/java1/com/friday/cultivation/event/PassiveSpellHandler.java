package com.friday.cultivation.event;

import com.friday.cultivation.CultivationCapability;
import com.friday.cultivation.capability.CultivationData;
import com.friday.cultivation.spell.Spell;
import com.friday.cultivation.technique.TechniqueBonusHelper;
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

import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 被动法术处理器 — 完整复刻原模组 PassiveSpellHandler
 * 6个被动法术：SLOW_REGEN(缓慢回血) / FROST_WALKER(冰霜行者) / QI_FLIGHT(灵气飞行) / BIGU(辟谷) / QI_MENDING(灵气修补) / POISON_IMMUNITY(毒免疫) / GHOST_FLIGHT(幽灵飞行)
 */
@Mod.EventBusSubscriber(modid = "friday_cultivation")
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

    private static final Map<UUID, Integer> GROUNDED_FLIGHT_TICKS = new ConcurrentHashMap<>();
    private static final Map<UUID, Integer> QI_FLIGHT_FLYING_TICKS = new ConcurrentHashMap<>();
    private static final Map<UUID, Integer> BIGU_PAID_UNTIL_TICK = new ConcurrentHashMap<>();

    private PassiveSpellHandler() {}

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        Player player = event.player;
        if (!(player instanceof ServerPlayer player2)) return;

        CultivationData ic = CultivationCapability.get(player2).orElse(null);
        if (ic == null) return;

        // 被锁灵术锁定时取消飞行
        if (SpiritLockHandler.isEntityLocked(player2)) {
            revokeQiFlight(player2);
            SwordFlightHandler.stopIfActive(player2, ic);
            return;
        }
        if (SoulHookHandler.isActionLocked(player2)) {
            QI_FLIGHT_FLYING_TICKS.remove(player2.getUUID());
            return;
        }
        if (RealmPressureHandler.isSuppressed(player2)) {
            RealmPressureHandler.forceGrounded(player2);
            revokeQiFlight(player2);
            SwordFlightHandler.stopIfActive(player2, ic, false);
        }

        handleBigu(player2, ic);
        SwordFlightHandler.tick(player2, ic);
        handleQiMending(player2, ic);

        // 缓慢回血：每100tick消耗5灵气回1血
        if (ic.isSpellEnabled(Spell.SLOW_REGEN) && player2.tickCount % SLOW_REGEN_INTERVAL == 0 && player2.getHealth() < player2.getMaxHealth()) {
            long cost = TechniqueBonusHelper.applySpellQiCostMultiplier(player2, Spell.SLOW_REGEN, SLOW_REGEN_QI_COST);
            if (ic.getCurrentQi() >= cost) {
                ic.setCurrentQi(ic.getCurrentQi() - cost);
                player2.heal(1.0f);
                CapabilityEvents.syncToClient(player2);
            }
        }

        // 冰霜行者：地面结冰（照搬原模组直接调用 FrostWalkerEnchantment.onEntityMoved）
        if (ic.isSpellEnabled(Spell.FROST_WALKER) && player2.onGround()) {
            BlockPos pos = player2.blockPosition();
            FrostWalkerEnchantment.onEntityMoved(player2, player2.level(), pos, FROST_WALKER_RADIUS);
        }

        handleQiFlight(player2, ic);
    }

    /** 灵气飞行处理（照搬原模组 handleQiFlight） */
    private static void handleQiFlight(ServerPlayer player, CultivationData data) {
        if (player.isSpectator() || player.isCreative()) {
            QI_FLIGHT_FLYING_TICKS.remove(player.getUUID());
            return;
        }
        if (RealmPressureHandler.isSuppressed(player)) {
            revokeQiFlight(player);
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
        boolean ghostFlightEnabled = isGhostFlightActive(data);
        boolean hasQi = data.getCurrentQi() > 0L;
        boolean shouldAllowFly = hasEnabledPassiveFlight(data);
        boolean currentlyAllowed = player.getAbilities().mayfly;
        boolean currentlyFlying = player.getAbilities().flying;
        boolean abilitiesDirty = false;

        if (shouldAllowFly != currentlyAllowed) {
            player.getAbilities().mayfly = shouldAllowFly;
            if (!shouldAllowFly && currentlyFlying) {
                player.getAbilities().flying = false;
            }
            abilitiesDirty = true;
        }
        if (shouldAllowFly && Math.abs(player.getAbilities().getFlyingSpeed() - QI_FLIGHT_BASE_FLYING_SPEED) > 1.0E-4f) {
            player.getAbilities().setFlyingSpeed(QI_FLIGHT_BASE_FLYING_SPEED);
            abilitiesDirty = true;
        }
        if (abilitiesDirty) {
            player.onUpdateAbilities();
        }
        if (!shouldAllowFly && player.getAbilities().flying) {
            player.getAbilities().flying = false;
            player.onUpdateAbilities();
        }

        // 着地4tick后自动取消飞行
        if (shouldAllowFly && player.getAbilities().flying) {
            if (player.onGround()) {
                int groundedTicks = GROUNDED_FLIGHT_TICKS.merge(player.getUUID(), 1, Integer::sum);
                if (groundedTicks >= QI_FLIGHT_GROUND_RELEASE_TICKS) {
                    player.getAbilities().flying = false;
                    player.fallDistance = 0.0f;
                    player.onUpdateAbilities();
                    GROUNDED_FLIGHT_TICKS.remove(player.getUUID());
                }
            } else {
                GROUNDED_FLIGHT_TICKS.remove(player.getUUID());
            }
        } else {
            GROUNDED_FLIGHT_TICKS.remove(player.getUUID());
        }

        currentlyFlying = player.getAbilities().flying;
        // 灵气飞行消耗灵气
        if (qiFlightEnabled && !ghostFlightEnabled && currentlyFlying && hasQi) {
            int flyingTicks = QI_FLIGHT_FLYING_TICKS.merge(player.getUUID(), 1, Integer::sum);
            if (flyingTicks < QI_FLIGHT_DRAIN_INTERVAL_TICKS) return;
            QI_FLIGHT_FLYING_TICKS.remove(player.getUUID());
            long base = TechniqueBonusHelper.applySpellQiCostMultiplier(player, Spell.QI_FLIGHT, QI_FLIGHT_DRAIN_PER_SECOND);
            long drain = Math.min(base, data.getCurrentQi());
            data.setCurrentQi(data.getCurrentQi() - drain);
            CapabilityEvents.syncToClient(player);
            if (data.getCurrentQi() <= 0L) {
                player.getAbilities().mayfly = false;
                player.getAbilities().flying = false;
                player.onUpdateAbilities();
                CapabilityEvents.syncToClient(player);
            }
        } else {
            QI_FLIGHT_FLYING_TICKS.remove(player.getUUID());
        }
    }

    private static boolean isGhostFlightActive(CultivationData data) {
        return data.isSoulState() && data.isSpellEnabled(Spell.GHOST_FLIGHT);
    }

    public static boolean hasEnabledPassiveFlight(CultivationData data) {
        if (data == null) return false;
        return isGhostFlightActive(data) || (data.isSpellEnabled(Spell.QI_FLIGHT) && data.getCurrentQi() > 0L);
    }

    private static void revokeQiFlight(ServerPlayer player) {
        if (player.isSpectator() || player.isCreative()) return;
        if (player.getAbilities().mayfly || player.getAbilities().flying) {
            player.getAbilities().mayfly = false;
            player.getAbilities().flying = false;
            player.onUpdateAbilities();
        }
        QI_FLIGHT_FLYING_TICKS.remove(player.getUUID());
    }

    /** 辟谷：每1200tick消耗10灵气维持饱食度（照搬原模组 handleBigu） */
    private static void handleBigu(ServerPlayer player, CultivationData data) {
        UUID id = player.getUUID();
        if (!data.isSpellEnabled(Spell.BIGU)) {
            BIGU_PAID_UNTIL_TICK.remove(id);
            return;
        }
        int paidUntil = BIGU_PAID_UNTIL_TICK.getOrDefault(id, Integer.MIN_VALUE);
        if (player.tickCount > paidUntil) {
            long cost = TechniqueBonusHelper.applySpellQiCostMultiplier(player, Spell.BIGU, BIGU_QI_COST);
            if (data.getCurrentQi() < cost) return;
            data.setCurrentQi(data.getCurrentQi() - cost);
            BIGU_PAID_UNTIL_TICK.put(id, player.tickCount + BIGU_INTERVAL);
            CapabilityEvents.syncToClient(player);
        }
        if (player.getFoodData().getFoodLevel() < 20) {
            player.getFoodData().setFoodLevel(20);
        }
        if (player.getFoodData().getSaturationLevel() < BIGU_SATURATION) {
            player.getFoodData().setSaturation(BIGU_SATURATION);
        }
    }

    /** 灵气修补：每20tick消耗灵气修复装备耐久（照搬原模组 handleQiMending） */
    private static void handleQiMending(ServerPlayer player, CultivationData data) {
        if (!data.isSpellEnabled(Spell.QI_MENDING)) return;
        if (player.isSpectator() || player.isCreative()) return;
        if (player.tickCount % QI_MENDING_INTERVAL_TICKS != 0) return;
        long remainingQi = data.getCurrentQi();
        if (remainingQi <= 0L) return;

        Set<ItemStack> seen = Collections.newSetFromMap(new IdentityHashMap<>());
        RepairResult result = repairStack(player, player.getMainHandItem(), remainingQi, seen);
        result = repairStack(player, player.getOffhandItem(), result.remainingQi(), seen, result.repaired());
        Iterator<ItemStack> iterator = player.getArmorSlots().iterator();
        while (iterator.hasNext() && (result = repairStack(player, iterator.next(), result.remainingQi(), seen, result.repaired())).remainingQi() > 0L) {
        }
        if (result.remainingQi() > 0L) {
            iterator = player.getInventory().items.iterator();
            while (iterator.hasNext() && (result = repairStack(player, iterator.next(), result.remainingQi(), seen, result.repaired())).remainingQi() > 0L) {
            }
        }
        if (!result.repaired()) return;
        data.setCurrentQi(result.remainingQi());
        player.getInventory().setChanged();
        if (player.containerMenu != null) player.containerMenu.broadcastChanges();
        player.level().playSound(null, player.blockPosition(), SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 0.28f, 1.35f + player.getRandom().nextFloat() * 0.2f);
        CapabilityEvents.syncToClient(player);
    }

    private static RepairResult repairStack(ServerPlayer player, ItemStack stack, long remainingQi, Set<ItemStack> seen) {
        return repairStack(player, stack, remainingQi, seen, false);
    }

    private static RepairResult repairStack(ServerPlayer player, ItemStack stack, long remainingQi, Set<ItemStack> seen, boolean repairedBefore) {
        if (remainingQi <= 0L || stack.isEmpty() || !seen.add(stack)) return new RepairResult(remainingQi, repairedBefore);
        if (!stack.isDamageableItem()) return new RepairResult(remainingQi, repairedBefore);
        int damage = stack.getDamageValue();
        if (damage <= 0) return new RepairResult(remainingQi, repairedBefore);
        int repair = affordableQiMendingRepair(player, damage, remainingQi);
        if (repair <= 0) return new RepairResult(remainingQi, repairedBefore);
        long cost = qiMendingCost(player, repair);
        stack.setDamageValue(Math.max(0, damage - repair));
        return new RepairResult(Math.max(0L, remainingQi - cost), true);
    }

    /** 二分查找可用灵气能修复的最大耐久（照搬原模组 affordableQiMendingRepair） */
    private static int affordableQiMendingRepair(ServerPlayer player, int maxRepair, long availableQi) {
        int low = 0;
        int high = maxRepair;
        while (low < high) {
            int mid = low + (high - low + 1) / 2;
            if (qiMendingCost(player, mid) <= availableQi) {
                low = mid;
                continue;
            }
            high = mid - 1;
        }
        return low;
    }

    /** 修复指定耐久的灵气消耗（照搬原模组 qiMendingCost：1耐久=1灵气 × 功法消耗倍率） */
    private static long qiMendingCost(ServerPlayer player, int durability) {
        if (durability <= 0) {
            return 0L;
        }
        long baseCost = Math.multiplyExact((long) durability, QI_MENDING_QI_PER_DURABILITY);
        return TechniqueBonusHelper.applySpellQiCostMultiplier(player, Spell.QI_MENDING, baseCost);
    }

    @SubscribeEvent
    public static void onPlayerLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        BIGU_PAID_UNTIL_TICK.remove(event.getEntity().getUUID());
    }

    /** 毒免疫：拒绝中毒效果（照搬原模组 onMobEffectApplicable） */
    @SubscribeEvent
    public static void onMobEffectApplicable(MobEffectEvent.Applicable event) {
        LivingEntity livingEntity = event.getEntity();
        if (!(livingEntity instanceof ServerPlayer player)) return;
        if (event.getEffectInstance().getEffect() != MobEffects.POISON) return;
        CultivationData ic = CultivationCapability.get(player).orElse(null);
        if (ic != null) {
            if (!SpiritLockHandler.isEntityLocked(player) && ic.isSpellEnabled(Spell.POISON_IMMUNITY)) {
                event.setResult(Event.Result.DENY);
            }
        }
    }

    private record RepairResult(long remainingQi, boolean repaired) {}
}
