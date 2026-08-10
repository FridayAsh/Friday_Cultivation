/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.network.FriendlyByteBuf
 *  net.minecraft.server.level.ServerPlayer
 *  net.minecraftforge.network.NetworkEvent$Context
 */
package com.friday.cultivation.network;

import com.friday.cultivation.event.ReincarnationManager;
import java.util.function.Supplier;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

public class ReincarnationChoicePacket {
    private final boolean reincarnate;

    public ReincarnationChoicePacket(boolean reincarnate) {
        this.reincarnate = reincarnate;
    }

    public static void encode(ReincarnationChoicePacket msg, FriendlyByteBuf buf) {
        buf.writeBoolean(msg.reincarnate);
    }

    public static ReincarnationChoicePacket decode(FriendlyByteBuf buf) {
        return new ReincarnationChoicePacket(buf.readBoolean());
    }

    public static void handle(ReincarnationChoicePacket msg, Supplier<NetworkEvent.Context> ctxSup) {
        NetworkEvent.Context ctx = ctxSup.get();
        ctx.enqueueWork(() -> {
            ServerPlayer player = ctx.getSender();
            if (player == null) {
                return;
            }
            ReincarnationManager.resolve(player, msg.reincarnate);
        });
        ctx.setPacketHandled(true);
    }
}

