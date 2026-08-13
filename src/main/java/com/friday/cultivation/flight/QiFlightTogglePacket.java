package com.friday.cultivation.flight;

import java.util.function.Supplier;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

/**
 * 客户端 → 服务端：双击空格切换灵气飞行开关。
 */
public class QiFlightTogglePacket {
    public QiFlightTogglePacket() {
    }

    public static void encode(QiFlightTogglePacket msg, FriendlyByteBuf buf) {
    }

    public static QiFlightTogglePacket decode(FriendlyByteBuf buf) {
        return new QiFlightTogglePacket();
    }

    public static void handle(QiFlightTogglePacket msg, Supplier<NetworkEvent.Context> ctxSup) {
        NetworkEvent.Context ctx = ctxSup.get();
        ctx.enqueueWork(() -> {
            ServerPlayer player = ctx.getSender();
            if (player == null) {
                return;
            }
            CultivationFlightHandler.toggleQiFlight(player);
        });
        ctx.setPacketHandled(true);
    }
}
