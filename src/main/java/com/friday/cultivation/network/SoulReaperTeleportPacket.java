/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.network.FriendlyByteBuf
 *  net.minecraft.server.level.ServerPlayer
 *  net.minecraftforge.network.NetworkEvent$Context
 */
package com.friday.cultivation.network;

import com.friday.cultivation.event.SoulReaperOrderHandler;
import java.util.UUID;
import java.util.function.Supplier;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

public class SoulReaperTeleportPacket {
    private final UUID targetId;

    public SoulReaperTeleportPacket(UUID targetId) {
        this.targetId = targetId;
    }

    public static void encode(SoulReaperTeleportPacket msg, FriendlyByteBuf buf) {
        buf.writeUUID(msg.targetId);
    }

    public static SoulReaperTeleportPacket decode(FriendlyByteBuf buf) {
        return new SoulReaperTeleportPacket(buf.readUUID());
    }

    public static void handle(SoulReaperTeleportPacket msg, Supplier<NetworkEvent.Context> ctxSup) {
        NetworkEvent.Context ctx = ctxSup.get();
        ctx.enqueueWork(() -> {
            ServerPlayer player = ctx.getSender();
            if (player != null) {
                SoulReaperOrderHandler.teleportToTarget(player, msg.targetId);
            }
        });
        ctx.setPacketHandled(true);
    }
}

