package com.friday.cultivation.network;

import com.friday.cultivation.client.VoidEscapeClientEffects;
import java.util.function.Supplier;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

/**
 * 虚空遁·入场特效包 — 完整复刻原模组 VoidEscapeEntryEffectPacket。
 * <p>
 * 服务端 → 客户端：携带目标 entityId。
 * 客户端通过 {@link VoidEscapeClientEffects#startEntryEffect} 启动入场的虚空裂缝视觉。
 * </p>
 */
public class VoidEscapeEntryEffectPacket {
    private final int entityId;

    public VoidEscapeEntryEffectPacket(int entityId) {
        this.entityId = entityId;
    }

    public static void encode(VoidEscapeEntryEffectPacket msg, FriendlyByteBuf buf) {
        buf.writeVarInt(msg.entityId);
    }

    public static VoidEscapeEntryEffectPacket decode(FriendlyByteBuf buf) {
        return new VoidEscapeEntryEffectPacket(buf.readVarInt());
    }

    public static void handle(VoidEscapeEntryEffectPacket msg, Supplier<NetworkEvent.Context> ctxSup) {
        NetworkEvent.Context ctx = ctxSup.get();
        ctx.enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> VoidEscapeClientEffects.startEntryEffect(msg.entityId)));
        ctx.setPacketHandled(true);
    }
}
