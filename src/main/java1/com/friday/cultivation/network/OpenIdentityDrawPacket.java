package com.friday.cultivation.network;

import com.friday.cultivation.client.ClientIdentityDrawHooks;
import com.friday.cultivation.identity.draw.IdentityDrawDeck;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * 服务端→客户端: 打开/更新身份抽卡界面
 * 复刻自原模组 com.xiaoxiang.cultivation.network.OpenIdentityDrawPacket
 */
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
