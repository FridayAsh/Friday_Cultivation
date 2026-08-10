/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.network.FriendlyByteBuf
 *  net.minecraft.server.level.ServerPlayer
 *  net.minecraftforge.network.NetworkEvent$Context
 */
package com.friday.cultivation.network;

import com.friday.cultivation.event.IdentityDrawHandler;
import java.util.function.Supplier;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

public class ConfirmIdentityDrawPacket {
    private final int cardIndex;

    public ConfirmIdentityDrawPacket(int cardIndex) {
        this.cardIndex = cardIndex;
    }

    public static void encode(ConfirmIdentityDrawPacket msg, FriendlyByteBuf buf) {
        buf.writeVarInt(msg.cardIndex);
    }

    public static ConfirmIdentityDrawPacket decode(FriendlyByteBuf buf) {
        return new ConfirmIdentityDrawPacket(buf.readVarInt());
    }

    public static void handle(ConfirmIdentityDrawPacket msg, Supplier<NetworkEvent.Context> ctxSup) {
        NetworkEvent.Context ctx = ctxSup.get();
        ctx.enqueueWork(() -> {
            ServerPlayer player = ctx.getSender();
            if (player == null) {
                return;
            }
            IdentityDrawHandler.handleConfirm(player, msg.cardIndex);
        });
        ctx.setPacketHandled(true);
    }
}

