package com.friday.cultivation.network;

import com.friday.cultivation.event.IdentityDrawHandler;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * 客户端→服务端: 确认选中卡牌索引（旧版抽卡模式）
 * 复刻自原模组 com.xiaoxiang.cultivation.network.ConfirmIdentityDrawPacket
 */
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
