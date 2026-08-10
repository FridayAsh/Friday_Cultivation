package com.friday.cultivation.network;

import com.friday.cultivation.client.DharmaBodyClientEffects;
import java.util.function.Supplier;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

/**
 * 法相显现包 — 完整复刻原模组 DharmaBodyManifestationPacket。
 * <p>
 * 服务端 → 客户端：携带 active、entityId、x、y、z、yaw、pitch、durationTicks。
 * 客户端通过 {@link DharmaBodyClientEffects#onBodySync} 启动/停止法相视觉。
 * </p>
 */
public class DharmaBodyManifestationPacket {
    private final boolean active;
    private final int entityId;
    private final double x;
    private final double y;
    private final double z;
    private final float yaw;
    private final float pitch;
    private final int durationTicks;

    public DharmaBodyManifestationPacket(boolean active, int entityId, double x, double y, double z, float yaw, float pitch, int durationTicks) {
        this.active = active;
        this.entityId = entityId;
        this.x = x;
        this.y = y;
        this.z = z;
        this.yaw = yaw;
        this.pitch = pitch;
        this.durationTicks = durationTicks;
    }

    public static void encode(DharmaBodyManifestationPacket msg, FriendlyByteBuf buf) {
        buf.writeBoolean(msg.active);
        buf.writeVarInt(msg.entityId);
        buf.writeDouble(msg.x);
        buf.writeDouble(msg.y);
        buf.writeDouble(msg.z);
        buf.writeFloat(msg.yaw);
        buf.writeFloat(msg.pitch);
        buf.writeVarInt(msg.durationTicks);
    }

    public static DharmaBodyManifestationPacket decode(FriendlyByteBuf buf) {
        return new DharmaBodyManifestationPacket(buf.readBoolean(), buf.readVarInt(), buf.readDouble(), buf.readDouble(), buf.readDouble(), buf.readFloat(), buf.readFloat(), buf.readVarInt());
    }

    public static void handle(DharmaBodyManifestationPacket msg, Supplier<NetworkEvent.Context> ctxSup) {
        NetworkEvent.Context ctx = ctxSup.get();
        ctx.enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> DharmaBodyClientEffects.onBodySync(msg.active, msg.entityId, msg.x, msg.y, msg.z, msg.yaw, msg.pitch, msg.durationTicks)));
        ctx.setPacketHandled(true);
    }
}
