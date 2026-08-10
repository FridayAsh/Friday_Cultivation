package com.friday.cultivation.network;

import com.friday.cultivation.client.ClientReincarnationHooks;
import java.util.function.Supplier;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

/**
 * 打开转世选择界面包（服务端→客户端）— 完整复刻原模组 OpenReincarnationPacket。
 * 服务端 ReincarnationManager.prompt 发送此包，客户端 ClientReincarnationHooks.open 打开 ReincarnationScreen。
 */
public class OpenReincarnationPacket {

    public static void encode(OpenReincarnationPacket msg, FriendlyByteBuf buf) {}
    public static OpenReincarnationPacket decode(FriendlyByteBuf buf) { return new OpenReincarnationPacket(); }

    public static void handle(OpenReincarnationPacket msg, Supplier<NetworkEvent.Context> ctxSup) {
        NetworkEvent.Context ctx = ctxSup.get();
        ctx.enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> ClientReincarnationHooks.open()));
        ctx.setPacketHandled(true);
    }
}
