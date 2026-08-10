package com.friday.cultivation.network;

import com.friday.cultivation.client.ImmortalNightVisionHandler;
import java.util.function.Supplier;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

/**
 * 不朽暗视同步包（服务端→客户端）— 完整复刻原模组 SyncImmortalDarkVisionPacket。
 * 不朽经+不朽体质组合时，服务端同步暗视状态到客户端 ImmortalNightVisionHandler。
 */
public class SyncImmortalDarkVisionPacket {
    private final boolean enabled;

    public SyncImmortalDarkVisionPacket(boolean enabled) {
        this.enabled = enabled;
    }

    public static void encode(SyncImmortalDarkVisionPacket msg, FriendlyByteBuf buf) {
        buf.writeBoolean(msg.enabled);
    }

    public static SyncImmortalDarkVisionPacket decode(FriendlyByteBuf buf) {
        return new SyncImmortalDarkVisionPacket(buf.readBoolean());
    }

    public static void handle(SyncImmortalDarkVisionPacket msg, Supplier<NetworkEvent.Context> ctxSup) {
        NetworkEvent.Context ctx = ctxSup.get();
        ctx.enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> ImmortalNightVisionHandler.setServerDarkVision(msg.enabled)));
        ctx.setPacketHandled(true);
    }

    public boolean enabled() { return this.enabled; }
}
