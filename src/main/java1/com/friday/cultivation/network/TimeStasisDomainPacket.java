package com.friday.cultivation.network;

import com.friday.cultivation.client.TimeStasisClientEffects;
import java.util.function.Supplier;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

/**
 * 时间凝滞·领域同步包 — 完整复刻原模组 TimeStasisDomainPacket。
 * <p>
 * 服务端 → 客户端：携带领域中心 (x, y, z)、半径 radius、持续 durationTicks、施法者 entityId。
 * 客户端通过 {@link TimeStasisClientEffects#onDomain(double, double, double, double, int, int)} 启动领域视觉。
 * </p>
 */
public class TimeStasisDomainPacket {
    private final double x;
    private final double y;
    private final double z;
    private final double radius;
    private final int durationTicks;
    private final int casterEntityId;

    public TimeStasisDomainPacket(double x, double y, double z, double radius, int durationTicks, int casterEntityId) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.radius = radius;
        this.durationTicks = durationTicks;
        this.casterEntityId = casterEntityId;
    }

    public static void encode(TimeStasisDomainPacket msg, FriendlyByteBuf buf) {
        buf.writeDouble(msg.x);
        buf.writeDouble(msg.y);
        buf.writeDouble(msg.z);
        buf.writeDouble(msg.radius);
        buf.writeVarInt(msg.durationTicks);
        buf.writeVarInt(msg.casterEntityId);
    }

    public static TimeStasisDomainPacket decode(FriendlyByteBuf buf) {
        return new TimeStasisDomainPacket(buf.readDouble(), buf.readDouble(), buf.readDouble(), buf.readDouble(), buf.readVarInt(), buf.readVarInt());
    }

    public static void handle(TimeStasisDomainPacket msg, Supplier<NetworkEvent.Context> ctxSup) {
        NetworkEvent.Context ctx = ctxSup.get();
        ctx.enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> TimeStasisClientEffects.onDomain(msg.x, msg.y, msg.z, msg.radius, msg.durationTicks, msg.casterEntityId)));
        ctx.setPacketHandled(true);
    }
}
