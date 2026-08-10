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
import com.friday.cultivation.event.CapabilityEvents;
import java.util.function.Supplier;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.network.NetworkEvent;

public class SelectSpellSlotPacket {
    private final int slot;

    public SelectSpellSlotPacket(int slot) {
        this.slot = slot;
    }

    public static void encode(SelectSpellSlotPacket msg, FriendlyByteBuf buf) {
        buf.writeVarInt(msg.slot);
    }

    public static SelectSpellSlotPacket decode(FriendlyByteBuf buf) {
        return new SelectSpellSlotPacket(buf.readVarInt());
    }

    public static void handle(SelectSpellSlotPacket msg, Supplier<NetworkEvent.Context> ctxSup) {
        NetworkEvent.Context ctx = ctxSup.get();
        ctx.enqueueWork(() -> {
            ServerPlayer player = ctx.getSender();
            if (player == null) {
                return;
            }
            CultivationCapability.get((Player)player).ifPresent(data -> {
                data.setSelectedSpellSlot(msg.slot);
                CapabilityEvents.syncToClient(player);
            });
        });
        ctx.setPacketHandled(true);
    }
}

