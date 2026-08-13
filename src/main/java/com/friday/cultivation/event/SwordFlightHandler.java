/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.network.chat.Component
 *  net.minecraft.server.level.ServerPlayer
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.item.SwordItem
 *  net.minecraft.world.phys.Vec3
 */
package com.friday.cultivation.event;

import com.friday.cultivation.cultivation.CultivationCapability;
import com.friday.cultivation.cultivation.CultivationData;
import com.friday.cultivation.cultivation.spell.Spell;
import com.friday.cultivation.cultivation.technique.TechniqueBonusHelper;
import com.friday.cultivation.event.CapabilityEvents;
import com.friday.cultivation.event.RealmPressureHandler;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.phys.Vec3;

public final class SwordFlightHandler {
    private static final float FLYING_SPEED = 0.05f;
    private static final double TAKEOFF_UPWARD_SPEED = 0.42;
    /** 记录玩家是否已真正离地（用于落地判定，避免起飞瞬间误停） */
    private static final java.util.Set<java.util.UUID> AIRBORNE = java.util.Collections.newSetFromMap(new java.util.concurrent.ConcurrentHashMap<>());

    private SwordFlightHandler() {
    }

    public static boolean isActive(CultivationData data) {
        return data != null && data.isSwordFlightActive();
    }

    public static boolean start(ServerPlayer player) {
        CultivationData data = CultivationCapability.get((Player)player).orElse(null);
        if (data == null) {
            return false;
        }
        if (RealmPressureHandler.isSuppressed((LivingEntity)player)) {
            player.displayClientMessage((Component)Component.translatable((String)"message.friday_cultivation.realm_pressure.flight_blocked"), true);
            return false;
        }
        if (data.isSwordFlightActive()) {
            SwordFlightHandler.stop(player, data);
            return true;
        }
        int slot = player.getInventory().selected;
        ItemStack sword = player.getInventory().getItem(slot);
        if (!(sword.getItem() instanceof SwordItem) || sword.isEmpty()) {
            player.displayClientMessage((Component)Component.translatable((String)"message.friday_cultivation.sword_flight.no_sword"), true);
            return false;
        }
        ItemStack ridingSword = sword.copy();
        player.getInventory().setItem(slot, ItemStack.EMPTY);
        data.startSwordFlight(ridingSword, slot);
        SwordFlightHandler.enableFlight(player);
        SwordFlightHandler.liftPlayerIntoFlight(player);
        if (!player.onGround()) {
            SwordFlightHandler.AIRBORNE.add(player.getUUID());
        }
        player.containerMenu.broadcastChanges();
        CapabilityEvents.syncToClient(player);
        player.displayClientMessage((Component)Component.translatable((String)"message.friday_cultivation.sword_flight.started"), true);
        return true;
    }

    public static void tick(ServerPlayer player, CultivationData data) {
        if (data == null || !data.isSwordFlightActive()) {
            return;
        }
        if (player.isCreative() || player.isSpectator()) {
            return;
        }
        if (RealmPressureHandler.isSuppressed((LivingEntity)player)) {
            SwordFlightHandler.stop(player, data, false);
            return;
        }
        // 御剑飞行独立判定：激活（剑被取走）即授权 mayfly，MC 原生按空格飞行
        SwordFlightHandler.enableFlight(player);
        player.fallDistance = 0.0f;
        if (data.getCurrentQi() <= 0L) {
            player.displayClientMessage((Component)Component.translatable((String)"message.friday_cultivation.sword_flight.no_qi"), true);
            SwordFlightHandler.stop(player, data, false);
            return;
        }
        if (player.tickCount % 20 != 0) {
            return;
        }
        long upkeep = TechniqueBonusHelper.applySpellQiCostMultiplier((Player)player, Spell.SWORD_FLIGHT, 20L);
        if (upkeep <= 0L) {
            return;
        }
        if (data.getCurrentQi() < upkeep) {
            player.displayClientMessage((Component)Component.translatable((String)"message.friday_cultivation.sword_flight.no_qi"), true);
            SwordFlightHandler.stop(player, data, false);
            return;
        }
        data.setCurrentQi(data.getCurrentQi() - upkeep);
        CapabilityEvents.syncToClient(player);
    }

    public static void stopIfActive(ServerPlayer player, CultivationData data) {
        SwordFlightHandler.stopIfActive(player, data, true);
    }

    public static void stopIfActive(ServerPlayer player, CultivationData data, boolean announceStopped) {
        if (data != null && data.isSwordFlightActive()) {
            SwordFlightHandler.stop(player, data, announceStopped);
        }
    }

    public static void stop(ServerPlayer player, CultivationData data) {
        SwordFlightHandler.stop(player, data, true);
    }

    private static void stop(ServerPlayer player, CultivationData data, boolean announceStopped) {
        if (data == null || !data.isSwordFlightActive()) {
            return;
        }
        SwordFlightHandler.AIRBORNE.remove(player.getUUID());
        ItemStack sword = data.getSwordFlightStack().copy();
        int originalSlot = data.getSwordFlightOriginalSlot();
        data.clearSwordFlight();
        SwordFlightHandler.returnSword(player, sword, originalSlot);
        if (!(player.isCreative() || player.isSpectator() || !RealmPressureHandler.isSuppressed((LivingEntity)player) && (data.isSpellEnabled(Spell.QI_FLIGHT) || data.isVoidEscapeActive() || data.isSoulState()))) {
            player.getAbilities().mayfly = false;
            player.getAbilities().flying = false;
            player.onUpdateAbilities();
        }
        player.fallDistance = 0.0f;
        player.containerMenu.broadcastChanges();
        CapabilityEvents.syncToClient(player);
        if (announceStopped) {
            player.displayClientMessage((Component)Component.translatable((String)"message.friday_cultivation.sword_flight.stopped"), true);
        }
    }

    private static void enableFlight(ServerPlayer player) {
        player.getAbilities().flying = true;
        player.getAbilities().mayfly = true;
        if (Math.abs(player.getAbilities().getFlyingSpeed() - 0.05f) > 1.0E-4f) {
            player.getAbilities().setFlyingSpeed(0.05f);
        }
        player.onUpdateAbilities();
    }

    private static void liftPlayerIntoFlight(ServerPlayer player) {
        Vec3 motion = player.getDeltaMovement();
        if (motion.y < 0.42) {
            player.setDeltaMovement(motion.x, 0.42, motion.z);
            player.hurtMarked = true;
        }
        player.fallDistance = 0.0f;
    }

    private static void returnSword(ServerPlayer player, ItemStack sword, int originalSlot) {
        if (sword.isEmpty()) {
            return;
        }
        if (originalSlot >= 0 && originalSlot < player.getInventory().items.size() && player.getInventory().getItem(originalSlot).isEmpty()) {
            player.getInventory().setItem(originalSlot, sword);
            return;
        }
        if (!player.getInventory().add(sword)) {
            player.drop(sword, false);
            player.displayClientMessage((Component)Component.translatable((String)"message.friday_cultivation.sword_flight.return_failed"), true);
        }
    }
}

