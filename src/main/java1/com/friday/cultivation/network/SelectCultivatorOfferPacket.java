package com.friday.cultivation.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * 选择修仙者交易数据包（严格照搬原模组 com.xiaoxiang.cultivation.network.SelectCultivatorOfferPacket）
 */
public class SelectCultivatorOfferPacket {
    private final int hint;

    public SelectCultivatorOfferPacket(int hint) {
        this.hint = hint;
    }

    public static void encode(SelectCultivatorOfferPacket msg, FriendlyByteBuf buf) {
        buf.writeInt(msg.hint);
    }

    public static SelectCultivatorOfferPacket decode(FriendlyByteBuf buf) {
        return new SelectCultivatorOfferPacket(buf.readInt());
    }

    public static void handle(SelectCultivatorOfferPacket msg, Supplier<NetworkEvent.Context> ctxSup) {
        NetworkEvent.Context ctx = ctxSup.get();
        ctx.setPacketHandled(true);
    }
}