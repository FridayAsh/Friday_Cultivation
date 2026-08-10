/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.network.FriendlyByteBuf
 *  net.minecraft.server.level.ServerPlayer
 *  net.minecraftforge.network.NetworkEvent$Context
 */
package com.friday.cultivation.network;

import com.friday.cultivation.cultivation.sect.SectSavedData;
import com.friday.cultivation.network.ClientOnlyGlowPacket;
import java.util.function.Supplier;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

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
                ClientOnlyGlowPacket.send(player, result.entityId(), 240);
            }
            player.displayClientMessage(result.message(), true);
        });
        ctx.setPacketHandled(true);
    }
}

