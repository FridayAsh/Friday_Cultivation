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
        ItemStack sword = data.getSwordFlightStack().copy();
        int originalSlot = data.getSwordFlightOriginalSlot();
        data.clearSwordFlight();
        SwordFlightHandler.returnSword(player, sword, originalSlot);
        if (!(player.isCreative() || player.isSpectator() || !RealmPressureHandler.isSuppressed((LivingEntity)player) && (data.isSpellEnabled(Spell.QI_FLIGHT) || data.isVoidEscapeActive() || data.isSoulState()))) {
            player.setNoGravity(false);
        }
        player.fallDistance = 0.0f;
        player.containerMenu.broadcastChanges();
        CapabilityEvents.syncToClient(player);
        if (announceStopped) {
            player.displayClientMessage((Component)Component.translatable((String)"message.friday_cultivation.sword_flight.stopped"), true);
        }
    }

    private static void enableFlight(ServerPlayer player) {
        // 自写飞行：不依赖 mayfly/flying（绕过 Caelus 飞行管理），
        // 用 setNoGravity 悬浮 + FlightInputPacket 控制运动
        player.setNoGravity(true);
        player.fallDistance = 0.0f;
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

