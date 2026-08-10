/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.network.FriendlyByteBuf
 *  net.minecraft.server.level.ServerPlayer
 *  net.minecraft.world.entity.player.Player
 *  net.minecraftforge.network.NetworkEvent$Context
 */
package com.friday.cultivation.network;

import com.friday.cultivation.cultivation.CultivationCapability;
import com.friday.cultivation.event.TimeAccelerationHandler;
import java.util.function.Supplier;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.network.NetworkEvent;

public class SetTimeAccelerationPacket {
    private final int multiplier;

    public SetTimeAccelerationPacket(int multiplier) {
        this.multiplier = multiplier;
    }

    public static void encode(SetTimeAccelerationPacket msg, FriendlyByteBuf buf) {
        buf.writeInt(msg.multiplier);
    }

    public static SetTimeAccelerationPacket decode(FriendlyByteBuf buf) {
        return new SetTimeAccelerationPacket(buf.readInt());
    }

    public static void handle(SetTimeAccelerationPacket msg, Supplier<NetworkEvent.Context> ctxSup) {
        NetworkEvent.Context ctx = ctxSup.get();
        ctx.enqueueWork(() -> {
            ServerPlayer player = ctx.getSender();
            if (player == null) {
                return;
            }
            CultivationCapability.get((Player)player).ifPresent(data -> {
                if (msg.multiplier <= 1) {
                    TimeAccelerationHandler.stop(player, data, true);
                } else {
                    TimeAccelerationHandler.start(player, data, msg.multiplier);
                }
            });
        });
        ctx.setPacketHandled(true);
    }
}

