package com.friday.cultivation.network;

import com.friday.cultivation.event.IdentityDrawHandler;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * 显示下一张身份卡数据包（严格照搬原模组 com.xiaoxiang.cultivation.network.RevealNextCardPacket）
 */
public class RevealNextCardPacket {

    public RevealNextCardPacket() {}

    public static void encode(RevealNextCardPacket msg, FriendlyByteBuf buf) {}

    public static RevealNextCardPacket decode(FriendlyByteBuf buf) {
        return new RevealNextCardPacket();
    }

    public static void handle(RevealNextCardPacket msg, Supplier<NetworkEvent.Context> ctxSup) {
        NetworkEvent.Context ctx = ctxSup.get();
        ctx.enqueueWork(() -> {
            ServerPlayer player = ctx.getSender();
            if (player == null) return;
            IdentityDrawHandler.handleReveal(player);
        });
        ctx.setPacketHandled(true);
    }
}