/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.network.chat.Component
 *  net.minecraft.server.level.ServerPlayer
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.player.Player
 *  net.minecraftforge.event.TickEvent$Phase
 *  net.minecraftforge.event.TickEvent$PlayerTickEvent
 *  net.minecraftforge.event.entity.EntityMountEvent
 *  net.minecraftforge.eventbus.api.SubscribeEvent
 *  net.minecraftforge.fml.common.Mod$EventBusSubscriber
 */
package com.friday.cultivation.event;

import com.friday.cultivation.cultivation.CultivationCapability;
import com.friday.cultivation.entity.SeatEntity;
import com.friday.cultivation.event.CapabilityEvents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.EntityMountEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid="friday_cultivation")
public final class CushionMountHandler {
    private CushionMountHandler() {
    }

    @SubscribeEvent
    public static void onMount(EntityMountEvent event) {
        Entity entity = event.getEntityMounting();
        if (!(entity instanceof ServerPlayer)) {
            return;
        }
        ServerPlayer player = (ServerPlayer)entity;
        if (!(event.getEntityBeingMounted() instanceof SeatEntity)) {
            return;
        }
        boolean meditating = event.isMounting();
        CultivationCapability.get((Player)player).ifPresent(data -> {
            if (meditating && !data.hasEquippedTechnique()) {
                player.displayClientMessage((Component)Component.translatable((String)"message.friday_cultivation.meditation.no_technique"), true);
                return;
            }
            if (data.isMeditating() == meditating) {
                return;
            }
            data.setMeditating(meditating);
            CapabilityEvents.syncToClient(player);
            String key = meditating ? "message.friday_cultivation.meditation.start" : "message.friday_cultivation.meditation.stop";
            player.displayClientMessage((Component)Component.translatable((String)key), true);
        });
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
        if (player2.tickCount % 10 != 0) {
            return;
        }
        CultivationCapability.get((Player)player2).ifPresent(data -> {
            if (data.isMeditating() && !(player2.getVehicle() instanceof SeatEntity)) {
                data.setMeditating(false);
                CapabilityEvents.syncToClient(player2);
            }
        });
    }
}

