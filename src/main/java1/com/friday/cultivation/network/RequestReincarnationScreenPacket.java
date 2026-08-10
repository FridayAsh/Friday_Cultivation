package com.friday.cultivation.network;

import com.friday.cultivation.CultivationCapability;
import com.friday.cultivation.capability.CultivationData;
import com.friday.cultivation.event.ReincarnationManager;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * 请求转世选择界面包 — 客户端 → 服务端（照搬原模组 RequestReincarnationScreenPacket）
 * 灵魂状态且转世就绪时，弹出转世选择。
 */
public class RequestReincarnationScreenPacket {
    public static void encode(RequestReincarnationScreenPacket msg, FriendlyByteBuf buf) {
    }

    public static RequestReincarnationScreenPacket decode(FriendlyByteBuf buf) {
        return new RequestReincarnationScreenPacket();
    }

    public static void handle(RequestReincarnationScreenPacket msg, Supplier<NetworkEvent.Context> ctxSup) {
        NetworkEvent.Context ctx = ctxSup.get();
        ctx.enqueueWork(() -> {
            ServerPlayer player = ctx.getSender();
            if (player == null) {
                return;
            }
            CultivationData ic = CultivationCapability.get(player).orElse(null);
            if (ic == null) {
                return;
            }
            if (!ic.isSoulState() || !ic.isReincarnationReady()) {
                return;
            }
            ReincarnationManager.prompt(player);
        });
        ctx.setPacketHandled(true);
    }
}
