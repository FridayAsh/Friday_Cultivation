package com.friday.cultivation.network;

import com.friday.cultivation.client.TimeStasisClientEffects;
import java.util.function.Supplier;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

/**
 * 时间凝滞·单体目标同步包 — 完整复刻原模组 TimeStasisTargetPacket。
 * <p>
 * 服务端 → 客户端：携带目标 entityId、持续 durationTicks、是否冻结 frozen。
 * 客户端通过 {@link TimeStasisClientEffects#onTargetStasis(int, int, boolean)} 切换单体的冰冻视觉状态。
 * </p>
 */
public class TimeStasisTargetPacket {
    private final int entityId;
    private final int durationTicks;
    private final boolean frozen;

    public TimeStasisTargetPacket(int entityId, int durationTicks, boolean frozen) {
        this.entityId = entityId;
        this.durationTicks = durationTicks;
        this.frozen = frozen;
    }

    public static void encode(TimeStasisTargetPacket msg, FriendlyByteBuf buf) {
        buf.writeVarInt(msg.entityId);
        buf.writeVarInt(msg.durationTicks);
        buf.writeBoolean(msg.frozen);
    }

    public static TimeStasisTargetPacket decode(FriendlyByteBuf buf) {
        return new TimeStasisTargetPacket(buf.readVarInt(), buf.readVarInt(), buf.readBoolean());
    }

    public static void handle(TimeStasisTargetPacket msg, Supplier<NetworkEvent.Context> ctxSup) {
        NetworkEvent.Context ctx = ctxSup.get();
        ctx.enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> TimeStasisClientEffects.onTargetStasis(msg.entityId, msg.durationTicks, msg.frozen)));
        ctx.setPacketHandled(true);
    }
}
