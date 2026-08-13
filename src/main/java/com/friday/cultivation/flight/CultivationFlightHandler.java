package com.friday.cultivation.flight;

import com.friday.cultivation.cultivation.CultivationCapability;
import com.friday.cultivation.cultivation.CultivationData;
import com.friday.cultivation.cultivation.spell.Spell;
import com.friday.cultivation.event.CapabilityEvents;
import com.friday.cultivation.event.RealmPressureHandler;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;

/**
 * 独立修仙飞行系统（御剑飞行 + 灵气飞行）。
 * 自写实现，完全独立于其他系统：
 * - 御剑飞行：施放后取出主手剑，激活飞行；再施放归还。
 * - 灵气飞行：被动法术启用（isSpellEnabled）且有灵气时自动可飞。
 * - 服务端每 tick 强制授权 mayfly+flying（覆盖其他模组/库的覆盖），
 *   客户端按空格自然起飞（MC 原生飞行行为），输入包控制运动。
 * 判定条件独立：御剑=已激活；灵气=已启用且灵气>0。
 */
public final class CultivationFlightHandler {
    private static final float FLYING_SPEED = 0.05f;
    private static final Map<UUID, ItemStack> SWORD_FLIGHT = new ConcurrentHashMap<>();
    private static final Map<UUID, Integer> SWORD_FLIGHT_SLOT = new ConcurrentHashMap<>();

    private CultivationFlightHandler() {
    }

    /** 御剑是否激活 */
    public static boolean isSwordFlightActive(Player player) {
        return player != null && SWORD_FLIGHT.containsKey(player.getUUID());
    }

    /** 灵气飞行是否可用（已启用且灵气>0） */
    public static boolean canQiFlight(Player player) {
        if (player == null) {
            return false;
        }
        CultivationData data = CultivationCapability.get((Player)player).orElse(null);
        return data != null && data.isSpellEnabled(Spell.QI_FLIGHT) && data.getCurrentQi() > 0L;
    }

    /** 施放/切换御剑飞行 */
    public static void toggleSwordFlight(ServerPlayer player) {
        if (isSwordFlightActive(player)) {
            stopSwordFlight(player);
            return;
        }
        if (RealmPressureHandler.isSuppressed((LivingEntity)player)) {
            player.displayClientMessage((Component)Component.translatable((String)"message.friday_cultivation.realm_pressure.flight_blocked"), true);
            return;
        }
        int slot = player.getInventory().selected;
        ItemStack sword = player.getInventory().getItem(slot);
        if (!(sword.getItem() instanceof SwordItem) || sword.isEmpty()) {
            player.displayClientMessage((Component)Component.translatable((String)"message.friday_cultivation.sword_flight.no_sword"), true);
            return;
        }
        ItemStack riding = sword.copy();
        player.getInventory().setItem(slot, ItemStack.EMPTY);
        SWORD_FLIGHT.put(player.getUUID(), riding);
        SWORD_FLIGHT_SLOT.put(player.getUUID(), slot);
        grantFlight(player);
        liftPlayer(player);
        player.containerMenu.broadcastChanges();
        CapabilityEvents.syncToClient(player);
        player.displayClientMessage((Component)Component.translatable((String)"message.friday_cultivation.sword_flight.started"), true);
    }

    /** 停止御剑飞行并归还剑 */
    public static void stopSwordFlight(ServerPlayer player) {
        UUID id = player.getUUID();
        ItemStack sword = SWORD_FLIGHT.remove(id);
        Integer slot = SWORD_FLIGHT_SLOT.remove(id);
        if (sword != null && !sword.isEmpty()) {
            if (slot != null && slot >= 0 && slot < player.getInventory().items.size() && player.getInventory().getItem(slot).isEmpty()) {
                player.getInventory().setItem(slot, sword);
            } else if (!player.getInventory().add(sword)) {
                player.drop(sword, false);
            }
        }
        revokeFlightIfNotQi(player);
        player.containerMenu.broadcastChanges();
        CapabilityEvents.syncToClient(player);
        player.displayClientMessage((Component)Component.translatable((String)"message.friday_cultivation.sword_flight.stopped"), true);
    }

    /**
     * 服务端每 tick 飞行判定（由主类事件调用）：
     * 御剑激活 或 灵气飞行可用时，强制授权 mayfly+flying（覆盖 Caelus 等外部覆盖）。
     */
    public static void tickFlight(ServerPlayer player) {
        CultivationData data = CultivationCapability.get((Player)player).orElse(null);
        if (data == null || player.isCreative() || player.isSpectator()) {
            return;
        }
        if (RealmPressureHandler.isSuppressed((LivingEntity)player)) {
            if (isSwordFlightActive(player)) {
                stopSwordFlight(player);
            }
            revokeFlight(player);
            return;
        }
        boolean sword = isSwordFlightActive(player);
        boolean qi = canQiFlight(player);
        boolean shouldFly = sword || qi;
        if (shouldFly) {
            grantFlight(player);
            player.fallDistance = 0.0f;
            // 灵气消耗（御剑也在 consumeQi 分支处理）
            if (qi && !sword && player.tickCount % 20 == 0) {
                long cost = 25L;
                if (data.getCurrentQi() >= cost) {
                    data.setCurrentQi(data.getCurrentQi() - cost);
                } else {
                    data.setCurrentQi(0L);
                    revokeFlight(player);
                }
                CapabilityEvents.syncToClient(player);
            }
        } else {
            revokeFlight(player);
        }
    }

    /** 灵气飞行是否激活（供客户端判定） */
    public static boolean isAnyFlightActive(ServerPlayer player) {
        return isSwordFlightActive(player) || canQiFlight(player);
    }

    private static void grantFlight(ServerPlayer player) {
        if (!player.getAbilities().mayfly || !player.getAbilities().flying) {
            player.getAbilities().mayfly = true;
            player.getAbilities().flying = true;
        }
        if (Math.abs(player.getAbilities().getFlyingSpeed() - FLYING_SPEED) > 1.0E-4f) {
            player.getAbilities().setFlyingSpeed(FLYING_SPEED);
        }
        player.onUpdateAbilities();
    }

    private static void revokeFlight(ServerPlayer player) {
        if (player.isCreative() || player.isSpectator()) {
            return;
        }
        if (player.getAbilities().mayfly || player.getAbilities().flying) {
            player.getAbilities().mayfly = false;
            player.getAbilities().flying = false;
            player.onUpdateAbilities();
        }
    }

    private static void revokeFlightIfNotQi(ServerPlayer player) {
        if (canQiFlight(player)) {
            grantFlight(player);
            return;
        }
        revokeFlight(player);
    }

    private static void liftPlayer(ServerPlayer player) {
        if (!player.onGround()) {
            return;
        }
        player.setDeltaMovement(player.getDeltaMovement().add(0.0, 0.5, 0.0));
        player.hurtMarked = true;
    }
}
