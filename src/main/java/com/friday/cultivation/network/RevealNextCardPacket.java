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

public class RevealNextCardPacket {
    public static void encode(RevealNextCardPacket msg, FriendlyByteBuf buf) {
    }

    public static RevealNextCardPacket decode(FriendlyByteBuf buf) {
        return new RevealNextCardPacket();
    }

    public static void handle(RevealNextCardPacket msg, Supplier<NetworkEvent.Context> ctxSup) {
        NetworkEvent.Context ctx = ctxSup.get();
        ctx.enqueueWork(() -> {
            ServerPlayer player = ctx.getSender();
            if (player == null) {
                return;
            }
            IdentityDrawHandler.handleReveal(player);
        });
        ctx.setPacketHandled(true);
    }
}

