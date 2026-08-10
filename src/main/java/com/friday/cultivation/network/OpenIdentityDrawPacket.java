/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.network.FriendlyByteBuf
 *  net.minecraftforge.network.NetworkEvent$Context
 */
package com.friday.cultivation.network;

import com.friday.cultivation.client.ClientIdentityDrawHooks;
import com.friday.cultivation.cultivation.draw.IdentityDrawDeck;
import java.util.function.Supplier;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

public class OpenIdentityDrawPacket {
    private final IdentityDrawDeck deck;
    private final boolean reconfigureMode;

    public OpenIdentityDrawPacket(IdentityDrawDeck deck) {
        this(deck, false);
    }

    public OpenIdentityDrawPacket(IdentityDrawDeck deck, boolean reconfigureMode) {
        this.deck = deck;
        this.reconfigureMode = reconfigureMode;
    }

    public static void encode(OpenIdentityDrawPacket msg, FriendlyByteBuf buf) {
        msg.deck.encode(buf);
        buf.writeBoolean(msg.reconfigureMode);
    }

    public static OpenIdentityDrawPacket decode(FriendlyByteBuf buf) {
        IdentityDrawDeck deck = IdentityDrawDeck.decode(buf);
        return new OpenIdentityDrawPacket(deck, buf.readBoolean());
    }

    public static void handle(OpenIdentityDrawPacket msg, Supplier<NetworkEvent.Context> ctxSup) {
        NetworkEvent.Context ctx = ctxSup.get();
        ctx.enqueueWork(() -> ClientIdentityDrawHooks.openOrUpdate(msg.deck, msg.reconfigureMode));
        ctx.setPacketHandled(true);
    }
}

