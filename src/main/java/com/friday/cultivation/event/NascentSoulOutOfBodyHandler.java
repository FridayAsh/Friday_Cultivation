/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.BlockPos
 *  net.minecraft.network.chat.Component
 *  net.minecraft.resources.ResourceKey
 *  net.minecraft.server.level.ServerLevel
 *  net.minecraft.server.level.ServerPlayer
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.level.BlockGetter
 *  net.minecraft.world.level.ChunkPos
 *  net.minecraft.world.level.GameType
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.level.block.state.BlockState
 *  net.minecraft.world.phys.Vec3
 *  net.minecraftforge.common.world.ForgeChunkManager
 *  net.minecraftforge.event.TickEvent$Phase
 *  net.minecraftforge.event.TickEvent$PlayerTickEvent
 *  net.minecraftforge.event.entity.player.PlayerEvent$PlayerLoggedOutEvent
 *  net.minecraftforge.eventbus.api.SubscribeEvent
 *  net.minecraftforge.fml.common.Mod$EventBusSubscriber
 *  net.minecraftforge.network.PacketDistributor
 */
package com.friday.cultivation.event;

import com.friday.cultivation.cultivation.CultivationCapability;
import com.friday.cultivation.cultivation.CultivationData;
import com.friday.cultivation.cultivation.spell.Spell;
import com.friday.cultivation.cultivation.technique.TechniqueBonusHelper;
import com.friday.cultivation.event.CapabilityEvents;
import com.friday.cultivation.network.ModNetwork;
import com.friday.cultivation.network.NascentSoulBodyPacket;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.world.ForgeChunkManager;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.PacketDistributor;

@Mod.EventBusSubscriber(modid="friday_cultivation")
public final class NascentSoulOutOfBodyHandler {
    private static final long BASE_DRAIN_PER_SECOND = 50L;
    private static final long BLOCK_CLIP_EXTRA_DRAIN = 250L;
    private static final double DISTANCE_DRAIN_DIVISOR = 8.0;
    private static final int BODY_VISUAL_DURATION_TICKS = 72000;
    private static final Map<UUID, State> ACTIVE = new ConcurrentHashMap<UUID, State>();

    private NascentSoulOutOfBodyHandler() {
    }

    public static boolean isActive(ServerPlayer player) {
        return player != null && ACTIVE.containsKey(player.getUUID());
    }

    public static void stopIfActive(ServerPlayer player, boolean notify) {
        if (NascentSoulOutOfBodyHandler.isActive(player)) {
            NascentSoulOutOfBodyHandler.stop(player, notify);
        }
    }

    public static Vec3 qiAbsorptionPosition(ServerPlayer player) {
        if (player == null) {
            return Vec3.ZERO;
        }
        State state = ACTIVE.get(player.getUUID());
        if (state != null && player.level().dimension().equals(state.dimension())) {
            return state.bodyPos().add(0.0, (double)player.getEyeHeight() * 0.6, 0.0);
        }
        return player.position().add(0.0, (double)player.getEyeHeight() * 0.6, 0.0);
    }

    public static boolean toggle(ServerPlayer player) {
        if (NascentSoulOutOfBodyHandler.isActive(player)) {
            NascentSoulOutOfBodyHandler.stop(player, true);
            return true;
        }
        NascentSoulOutOfBodyHandler.start(player);
        return true;
    }

    private static void start(ServerPlayer player) {
        GameType previous = player.gameMode.getGameModeForPlayer();
        ChunkPos bodyChunk = new ChunkPos(player.blockPosition());
        State state = new State((ResourceKey<Level>)player.level().dimension(), player.position(), player.getYRot(), player.getXRot(), previous, bodyChunk.x, bodyChunk.z, bodyChunk.x, bodyChunk.z);
        ACTIVE.put(player.getUUID(), state);
        NascentSoulOutOfBodyHandler.forceChunk(player.serverLevel(), player.getUUID(), bodyChunk.x, bodyChunk.z, true);
        NascentSoulOutOfBodyHandler.syncBodyVisual(player, state, true);
        player.setGameMode(GameType.SPECTATOR);
        player.fallDistance = 0.0f;
        player.displayClientMessage((Component)Component.translatable((String)"message.friday_cultivation.nascent_soul_out_of_body.started"), true);
    }

    private static void stop(ServerPlayer player, boolean notify) {
        State state = ACTIVE.remove(player.getUUID());
        if (state == null) {
            return;
        }
        NascentSoulOutOfBodyHandler.syncBodyVisual(player, state, false);
        ServerLevel level = player.server.getLevel(state.dimension());
        if (level != null) {
            NascentSoulOutOfBodyHandler.releaseForcedChunks(level, player.getUUID(), state);
            player.teleportTo(level, state.bodyPos().x, state.bodyPos().y, state.bodyPos().z, state.yRot(), state.xRot());
        }
        GameType restore = state.previousMode() == GameType.SPECTATOR ? GameType.SURVIVAL : state.previousMode();
        player.setGameMode(restore);
        player.setNoGravity(false);
        player.fallDistance = 0.0f;
        if (notify) {
            player.displayClientMessage((Component)Component.translatable((String)"message.friday_cultivation.nascent_soul_out_of_body.stopped"), true);
        }
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
        State state = ACTIVE.get(player2.getUUID());
        if (state == null) {
            return;
        }
        state = NascentSoulOutOfBodyHandler.updateSoulChunk(player2, state);
        if (player2.tickCount % 20 != 0) {
            return;
        }
        CultivationData data = CultivationCapability.get((Player)player2).orElse(null);
        if (data == null) {
            NascentSoulOutOfBodyHandler.stop(player2, false);
            return;
        }
        long drain = 50L + Math.round(state.bodyPos().distanceTo(player2.position()) / 8.0);
        if (NascentSoulOutOfBodyHandler.isInsideBlock(player2)) {
            drain += 250L;
        }
        drain = TechniqueBonusHelper.applySpellQiCostMultiplier((Player)player2, Spell.NASCENT_SOUL_OUT_OF_BODY, drain);
        if (data.getCurrentQi() < drain) {
            player2.displayClientMessage((Component)Component.translatable((String)"message.friday_cultivation.nascent_soul_out_of_body.no_qi"), true);
            NascentSoulOutOfBodyHandler.stop(player2, true);
            CapabilityEvents.syncToClient(player2);
            return;
        }
        data.setCurrentQi(data.getCurrentQi() - drain);
        CapabilityEvents.syncToClient(player2);
    }

    @SubscribeEvent
    public static void onPlayerLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        Player player = event.getEntity();
        if (player instanceof ServerPlayer) {
            ServerPlayer player2 = (ServerPlayer)player;
            NascentSoulOutOfBodyHandler.stop(player2, false);
        }
    }

    private static boolean isInsideBlock(ServerPlayer player) {
        BlockPos pos = player.blockPosition();
        BlockState state = player.level().getBlockState(pos);
        return !state.isAir() && state.isSolidRender(player.level(), pos);
    }

    private static State updateSoulChunk(ServerPlayer player, State state) {
        if (!player.level().dimension().equals(state.dimension())) {
            return state;
        }
        ChunkPos current = new ChunkPos(player.blockPosition());
        if (current.x == state.soulChunkX() && current.z == state.soulChunkZ()) {
            return state;
        }
        ServerLevel level = player.serverLevel();
        if (state.soulChunkX() != state.bodyChunkX() || state.soulChunkZ() != state.bodyChunkZ()) {
            NascentSoulOutOfBodyHandler.forceChunk(level, player.getUUID(), state.soulChunkX(), state.soulChunkZ(), false);
        }
        NascentSoulOutOfBodyHandler.forceChunk(level, player.getUUID(), current.x, current.z, true);
        State updated = state.withSoulChunk(current);
        ACTIVE.put(player.getUUID(), updated);
        return updated;
    }

    private static void releaseForcedChunks(ServerLevel level, UUID owner, State state) {
        NascentSoulOutOfBodyHandler.forceChunk(level, owner, state.bodyChunkX(), state.bodyChunkZ(), false);
        if (state.soulChunkX() != state.bodyChunkX() || state.soulChunkZ() != state.bodyChunkZ()) {
            NascentSoulOutOfBodyHandler.forceChunk(level, owner, state.soulChunkX(), state.soulChunkZ(), false);
        }
    }

    private static void forceChunk(ServerLevel level, UUID owner, int chunkX, int chunkZ, boolean forced) {
        ForgeChunkManager.forceChunk((ServerLevel)level, (String)"friday_cultivation", (UUID)owner, (int)chunkX, (int)chunkZ, (boolean)forced, (boolean)true);
    }

    private static void syncBodyVisual(ServerPlayer player, State state, boolean active) {
        int duration = active ? 72000 : 0;
        NascentSoulBodyPacket packet = new NascentSoulBodyPacket(active, player.getId(), state.bodyPos().x, state.bodyPos().y, state.bodyPos().z, state.yRot(), state.xRot(), duration);
        ServerLevel bodyLevel = player.server.getLevel(state.dimension());
        if (bodyLevel != null) {
            ModNetwork.CHANNEL.send(PacketDistributor.DIMENSION.with(() -> ((ServerLevel)bodyLevel).dimension()), (Object)packet);
        }
        if (player.connection != null) {
            ModNetwork.CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), (Object)packet);
        }
    }

    private record State(ResourceKey<Level> dimension, Vec3 bodyPos, float yRot, float xRot, GameType previousMode, int bodyChunkX, int bodyChunkZ, int soulChunkX, int soulChunkZ) {
        private State withSoulChunk(ChunkPos chunk) {
            return new State(this.dimension, this.bodyPos, this.yRot, this.xRot, this.previousMode, this.bodyChunkX, this.bodyChunkZ, chunk.x, chunk.z);
        }
    }
}

