/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.BlockPos
 *  net.minecraft.network.chat.Component
 *  net.minecraft.server.MinecraftServer
 *  net.minecraft.server.level.ServerLevel
 *  net.minecraft.server.level.ServerPlayer
 *  net.minecraft.world.entity.player.Player
 *  net.minecraftforge.network.PacketDistributor
 */
package com.friday.cultivation.event;

import com.friday.cultivation.cultivation.CultivationCapability;
import com.friday.cultivation.cultivation.CultivationData;
import com.friday.cultivation.cultivation.technique.TechniqueLoadoutHelper;
import com.friday.cultivation.event.CapabilityEvents;
import com.friday.cultivation.event.IdentityDrawHandler;
import com.friday.cultivation.event.SoulStateHandler;
import com.friday.cultivation.network.ModNetwork;
import com.friday.cultivation.network.OpenReincarnationPacket;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.network.PacketDistributor;

public final class ReincarnationManager {
    private ReincarnationManager() {
    }

    public static void prompt(ServerPlayer player) {
        CultivationData data = CultivationCapability.get((Player)player).orElse(null);
        if (data == null) {
            return;
        }
        data.setReincarnationPending(true);
        CapabilityEvents.syncToClient(player);
        ModNetwork.CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), (Object)new OpenReincarnationPacket());
    }

    public static void resolve(ServerPlayer player, boolean reincarnate) {
        CultivationData data = CultivationCapability.get((Player)player).orElse(null);
        if (data == null || !data.isReincarnationPending()) {
            return;
        }
        if (reincarnate) {
            ReincarnationManager.doReincarnate(player, data);
        } else {
            ReincarnationManager.doReturnIntact(player, data);
        }
    }

    private static void doReincarnate(ServerPlayer player, CultivationData data) {
        int difuReincarnationEntries = data.getDifuReincarnationEntries();
        data.copyFrom(new CultivationData());
        data.setDifuReincarnationEntries(difuReincarnationEntries);
        TechniqueEffectHandler.clearBodyTemperingHpBonus(player);
        player.stopRiding();
        player.getInventory().clearContent();
        player.removeAllEffects();
        IdentityDrawHandler.grantReincarnationFatePlate(player);
        SoulStateHandler.clearDeathHostility(player);
        ReincarnationManager.teleportToOverworldSpawn(player);
        player.setHealth(player.getMaxHealth());
        player.sendSystemMessage((Component)Component.translatable((String)"message.friday_cultivation.reincarnation.reincarnated"));
        CapabilityEvents.syncToClient(player);
        SoulStateHandler.broadcastSouls(player.getServer());
    }

    private static void doReturnIntact(ServerPlayer player, CultivationData data) {
        data.setSoulState(false);
        data.setReincarnationPending(false);
        data.setReincarnationReady(false);
        data.setSoulTicks(0);
        data.setMeditating(false);
        TechniqueLoadoutHelper.NormalizationResult techniqueResult = TechniqueLoadoutHelper.normalizeForCurrentState(data, player.getRandom());
        player.stopRiding();
        player.removeAllEffects();
        player.setHealth(player.getMaxHealth());
        SoulStateHandler.clearDeathHostility(player);
        ReincarnationManager.teleportToOverworldSpawn(player);
        TechniqueLoadoutHelper.notifyNormalization(player, data, techniqueResult);
        player.sendSystemMessage((Component)Component.translatable((String)"message.friday_cultivation.reincarnation.returned"));
        CapabilityEvents.syncToClient(player);
        SoulStateHandler.broadcastSouls(player.getServer());
    }

    private static void teleportToOverworldSpawn(ServerPlayer player) {
        MinecraftServer server = player.getServer();
        if (server == null) {
            return;
        }
        ServerLevel overworld = server.overworld();
        BlockPos spawn = player.getRespawnPosition() != null && player.getRespawnDimension() == overworld.dimension() ? player.getRespawnPosition() : overworld.getSharedSpawnPos();
        player.teleportTo(overworld, (double)spawn.getX() + 0.5, (double)spawn.getY(), (double)spawn.getZ() + 0.5, player.getYRot(), player.getXRot());
    }
}

