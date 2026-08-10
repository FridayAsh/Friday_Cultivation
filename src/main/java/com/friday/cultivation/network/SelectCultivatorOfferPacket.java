/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.network.FriendlyByteBuf
 *  net.minecraftforge.network.NetworkEvent$Context
 */
package com.friday.cultivation.network;

import java.util.function.Supplier;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

public class SelectCultivatorOfferPacket {
    private final int hint;

    public SelectCultivatorOfferPacket(int hint) {
        this.hint = hint;
    }

    public static void encode(SelectCultivatorOfferPacket msg, FriendlyByteBuf buf) {
        buf.writeVarInt(msg.hint);
    }

    public static SelectCultivatorOfferPacket decode(FriendlyByteBuf buf) {
        return new SelectCultivatorOfferPacket(buf.readVarInt());
    }

    public static void handle(SelectCultivatorOfferPacket msg, Supplier<NetworkEvent.Context> ctxSup) {
        NetworkEvent.Context ctx = ctxSup.get();
        ctx.setPacketHandled(true);
    }
}

