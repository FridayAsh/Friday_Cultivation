package com.friday.cultivation.network;

import com.friday.cultivation.client.AlchemyGhostRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * 切换炼丹预览(Ghost)模式包 — 服务端 → 客户端
 * 完整复刻原模组 ToggleAlchemyGhostPacket。
 * 客户端收到后调用 AlchemyGhostRenderer.toggleCore 切换核心方块的 ghost 渲染状态。
 */
public class ToggleAlchemyGhostPacket {
    private final BlockPos corePos;

    public ToggleAlchemyGhostPacket(BlockPos corePos) {
        this.corePos = corePos;
    }

    public static void encode(ToggleAlchemyGhostPacket msg, FriendlyByteBuf buf) {
        buf.writeBlockPos(msg.corePos);
    }

    public static ToggleAlchemyGhostPacket decode(FriendlyByteBuf buf) {
        return new ToggleAlchemyGhostPacket(buf.readBlockPos());
    }

    public static void handle(ToggleAlchemyGhostPacket msg, Supplier<NetworkEvent.Context> ctxSup) {
        NetworkEvent.Context ctx = ctxSup.get();
        ctx.enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> AlchemyGhostRenderer.toggleCore(msg.corePos)));
        ctx.setPacketHandled(true);
    }
}
