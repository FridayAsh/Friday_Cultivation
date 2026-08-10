package com.friday.cultivation.network;

import com.friday.cultivation.client.ClientShieldRippleHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * 宗门护盾涟漪包 - 严格 1:1 复刻原模组
 * 混淆名映射: m_130064_=writeBlockPos, m_130135_=readBlockPos
 */
public class SectShieldRipplePacket {
    private final BlockPos hitPos;

    public SectShieldRipplePacket(BlockPos hitPos) {
        this.hitPos = hitPos;
    }

    public static void encode(SectShieldRipplePacket msg, FriendlyByteBuf buf) {
        buf.writeBlockPos(msg.hitPos);
    }

    public static SectShieldRipplePacket decode(FriendlyByteBuf buf) {
        return new SectShieldRipplePacket(buf.readBlockPos());
    }

    public static void handle(SectShieldRipplePacket msg, Supplier<NetworkEvent.Context> ctxSup) {
        NetworkEvent.Context ctx = ctxSup.get();
        ctx.enqueueWork(() -> DistExecutor.unsafeRunWhenOn((Dist)Dist.CLIENT, () -> () -> ClientShieldRippleHandler.onHit(msg.hitPos)));
        ctx.setPacketHandled(true);
    }
}
