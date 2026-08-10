package com.friday.cultivation.network;

import com.friday.cultivation.sect.SectSavedData;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * 追踪宗门任务发布者包 - 严格 1:1 复刻原模组
 * 混淆名映射: m_130072_=writeUtf, m_130136_=readUtf, m_284548_=getServer,
 *             m_5661_=sendSystemMessage
 */
public class TrackSectTaskIssuerPacket {
    private static final int GLOW_TICKS = 240;
    private final String taskId;

    public TrackSectTaskIssuerPacket(String taskId) {
        this.taskId = taskId == null ? "" : taskId;
    }

    public static void encode(TrackSectTaskIssuerPacket msg, FriendlyByteBuf buf) {
        buf.writeUtf(msg.taskId, 160);
    }

    public static TrackSectTaskIssuerPacket decode(FriendlyByteBuf buf) {
        return new TrackSectTaskIssuerPacket(buf.readUtf(160));
    }

    public static void handle(TrackSectTaskIssuerPacket msg, Supplier<NetworkEvent.Context> ctxSup) {
        NetworkEvent.Context ctx = ctxSup.get();
        ctx.enqueueWork(() -> {
            ServerPlayer player = ctx.getSender();
            if (player == null) {
                return;
            }
            SectSavedData.TaskIssuerTrackResult result = SectSavedData.get(player.serverLevel()).trackTaskIssuer(player, msg.taskId);
            if (result.found() && result.entityId() >= 0) {
                ClientOnlyGlowPacket.send(player, java.util.List.of(Integer.valueOf(result.entityId())), 240);
            }
            player.sendSystemMessage(result.message());
        });
        ctx.setPacketHandled(true);
    }
}
