/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.server.level.ServerPlayer
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.player.Player
 *  net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent
 *  net.minecraftforge.event.AttachCapabilitiesEvent
 *  net.minecraftforge.event.entity.player.PlayerEvent$Clone
 *  net.minecraftforge.event.entity.player.PlayerEvent$PlayerChangedDimensionEvent
 *  net.minecraftforge.event.entity.player.PlayerEvent$PlayerLoggedInEvent
 *  net.minecraftforge.event.entity.player.PlayerEvent$PlayerRespawnEvent
 *  net.minecraftforge.eventbus.api.SubscribeEvent
 *  net.minecraftforge.fml.common.Mod$EventBusSubscriber
 *  net.minecraftforge.network.PacketDistributor
 */
package com.friday.cultivation.event;

import com.friday.cultivation.config.ModCommonConfig;
import com.friday.cultivation.cultivation.CultivationCapability;
import com.friday.cultivation.cultivation.CultivationData;
import com.friday.cultivation.cultivation.sect.SectSavedData;
import com.friday.cultivation.flight.CultivationFlightHandler;
import com.friday.cultivation.network.ModNetwork;
import com.friday.cultivation.network.SyncCultivationDataPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.PacketDistributor;

@Mod.EventBusSubscriber(modid="friday_cultivation")
public final class CapabilityEvents {
    private CapabilityEvents() {
    }

    @SubscribeEvent
    public static void onAttachCapabilities(AttachCapabilitiesEvent<Entity> event) {
        if (event.getObject() instanceof Player) {
            event.addCapability(CultivationCapability.ID, CultivationCapability.createProvider());
        }
    }

    @SubscribeEvent
    public static void onPlayerClone(PlayerEvent.Clone event) {
        Player original = event.getOriginal();
        Player clone = event.getEntity();
        original.reviveCaps();
        try {
            CultivationCapability.get(original).ifPresent(oldData -> CultivationCapability.get(clone).ifPresent(newData -> newData.copyFrom((CultivationData)oldData)));
        }
        finally {
            original.invalidateCaps();
        }
    }

    @SubscribeEvent
    public static void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        Player player = event.getEntity();
        if (player instanceof ServerPlayer) {
            ServerPlayer player2 = (ServerPlayer)player;
            CultivationCapability.get((Player)player2).ifPresent(data -> {
                data.clearCharging();
                data.applyZhenyuanMajorAutoRebalanceMigration();
                CapabilityEvents.applySpellTerrainRuleSnapshot(data, true);
            });
            CultivationFlightHandler.restoreAfterLogin(player2);
            CapabilityEvents.syncToClient(player2);
        }
    }

    @SubscribeEvent
    public static void onPlayerChangedDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        Player player = event.getEntity();
        if (player instanceof ServerPlayer) {
            ServerPlayer player2 = (ServerPlayer)player;
            CultivationCapability.get((Player)player2).ifPresent(CapabilityEvents::applySpellTerrainRuleSnapshot);
            CapabilityEvents.syncToClient(player2);
        }
    }

    @SubscribeEvent
    public static void onPlayerRespawn(PlayerEvent.PlayerRespawnEvent event) {
        Player player = event.getEntity();
        if (player instanceof ServerPlayer) {
            ServerPlayer player2 = (ServerPlayer)player;
            CultivationCapability.get((Player)player2).ifPresent(CapabilityEvents::applySpellTerrainRuleSnapshot);
            CapabilityEvents.syncToClient(player2);
        }
    }

    public static void syncToClient(ServerPlayer player) {
        SectSavedData.get(player.serverLevel()).syncPlayerSectDisplay(player);
        CultivationCapability.get((Player)player).ifPresent(data -> ModNetwork.CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), (Object)new SyncCultivationDataPacket((CultivationData)data)));
    }

    public static boolean applySpellTerrainRuleSnapshot(CultivationData data) {
        return CapabilityEvents.applySpellTerrainRuleSnapshot(data, false);
    }

    public static boolean applySpellTerrainRuleSnapshot(CultivationData data, boolean resetToServerDefault) {
        boolean changed = false;
        changed |= data.setSpellTerrainDestructionForcedOffByServer(ModCommonConfig.spellTerrainDestructionForceDisabled());
        boolean defaultEnabled = ModCommonConfig.spellTerrainDestructionDefaultEnabled();
        if (resetToServerDefault) {
            changed |= data.isSpellTerrainDestructionEnabled() != defaultEnabled;
            data.setSpellTerrainDestructionEnabled(defaultEnabled);
        } else {
            changed |= data.initializeSpellTerrainDestructionPreference(defaultEnabled);
        }
        return changed;
    }

    public static void refreshSpellTerrainRuleSnapshot(ServerPlayer player) {
        CultivationCapability.get((Player)player).ifPresent(data -> {
            if (CapabilityEvents.applySpellTerrainRuleSnapshot(data)) {
                CapabilityEvents.syncToClient(player);
            }
        });
    }

    public static void registerCapability(RegisterCapabilitiesEvent event) {
        event.register(CultivationData.class);
    }
}

