package com.friday.cultivation.network;

import com.friday.cultivation.client.ClientOnlyGlowHandler;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.PacketDistributor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Supplier;

/**
 * 客户端仅高亮包 - 严格 1:1 复刻原模组 com.xiaoxiang.cultivation.network.ClientOnlyGlowPacket
 * 混淆名映射: m_130130_=writeVarInt, m_130242_=readVarInt
 */
public class ClientOnlyGlowPacket {
    private static final int MAX_ENTITY_IDS = 512;
    private final List<Integer> entityIds;
    private final int durationTicks;

    public ClientOnlyGlowPacket(Collection<Integer> entityIds, int durationTicks) {
        this.entityIds = entityIds.stream().limit(512L).toList();
        this.durationTicks = durationTicks;
    }

    public static void send(ServerPlayer viewer, int entityId, int durationTicks) {
        ClientOnlyGlowPacket.send(viewer, List.of(Integer.valueOf(entityId)), durationTicks);
    }

    public static void send(ServerPlayer viewer, Collection<Integer> entityIds, int durationTicks) {
        if (viewer == null || entityIds == null || entityIds.isEmpty()) {
            return;
        }
        ModNetwork.CHANNEL.send(PacketDistributor.PLAYER.with(() -> viewer), new ClientOnlyGlowPacket(entityIds, durationTicks));
    }

    public static void encode(ClientOnlyGlowPacket msg, FriendlyByteBuf buf) {
        buf.writeVarInt(msg.durationTicks);
        buf.writeVarInt(msg.entityIds.size());
        for (int entityId : msg.entityIds) {
            buf.writeVarInt(entityId);
        }
    }

    public static ClientOnlyGlowPacket decode(FriendlyByteBuf buf) {
        int durationTicks = buf.readVarInt();
        int encodedCount = Math.max(0, buf.readVarInt());
        int usedCount = Math.min(512, encodedCount);
        ArrayList<Integer> ids = new ArrayList<Integer>(usedCount);
        for (int i = 0; i < encodedCount; ++i) {
            int entityId = buf.readVarInt();
            if (i >= usedCount) continue;
            ids.add(entityId);
        }
        return new ClientOnlyGlowPacket(ids, durationTicks);
    }

    public static void handle(ClientOnlyGlowPacket msg, Supplier<NetworkEvent.Context> ctxSup) {
        NetworkEvent.Context ctx = ctxSup.get();
        ctx.enqueueWork(() -> DistExecutor.unsafeRunWhenOn((Dist)Dist.CLIENT, () -> () -> ClientOnlyGlowHandler.show(msg.entityIds, msg.durationTicks)));
        ctx.setPacketHandled(true);
    }
}
