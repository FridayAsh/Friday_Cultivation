/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.network.FriendlyByteBuf
 *  net.minecraft.server.level.ServerPlayer
 *  net.minecraftforge.network.NetworkEvent$Context
 */
package com.friday.cultivation.network;

import com.friday.cultivation.event.SoulStateHandler;
import java.util.function.Supplier;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

public class DeathChoicePacket {
    private final Choice choice;

    public DeathChoicePacket(Choice choice) {
        this.choice = choice == null ? Choice.WANDERING_SOUL : choice;
    }

    public static void encode(DeathChoicePacket msg, FriendlyByteBuf buf) {
        buf.writeVarInt(msg.choice.ordinal());
    }

    public static DeathChoicePacket decode(FriendlyByteBuf buf) {
        int ordinal = buf.readVarInt();
        Choice[] values = Choice.values();
        if (ordinal < 0 || ordinal >= values.length) {
            ordinal = Choice.WANDERING_SOUL.ordinal();
        }
        return new DeathChoicePacket(values[ordinal]);
    }

    public static void handle(DeathChoicePacket msg, Supplier<NetworkEvent.Context> ctxSup) {
        NetworkEvent.Context ctx = ctxSup.get();
        ctx.enqueueWork(() -> {
            ServerPlayer player = ctx.getSender();
            if (player != null) {
                SoulStateHandler.resolveDeathChoice(player, msg.choice);
            }
        });
        ctx.setPacketHandled(true);
    }

    public static enum Choice {
        VANILLA_DEATH,
        GO_DIFU,
        WANDERING_SOUL;

    }
}

