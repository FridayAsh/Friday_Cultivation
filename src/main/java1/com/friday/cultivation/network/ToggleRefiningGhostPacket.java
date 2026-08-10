package com.friday.cultivation.network;

import com.friday.cultivation.client.RefiningGhostRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * 切换炼器Ghost显示包 — 服务端 → 客户端
 * 完整复刻原模组 ToggleRefiningGhostPacket。
 * 玩家右键炼器核心时（结构不完整），服务端发送此包到客户端，
 * 客户端收到后调用 RefiningGhostRenderer.toggleCore 切换该核心的 Ghost 渲染状态。
 */
public class ToggleRefiningGhostPacket {

    private final BlockPos corePos;

    public ToggleRefiningGhostPacket(BlockPos corePos) {
        this.corePos = corePos;
    }

    public static void encode(ToggleRefiningGhostPacket msg, FriendlyByteBuf buf) {
        buf.writeBlockPos(msg.corePos);
    }

    public static ToggleRefiningGhostPacket decode(FriendlyByteBuf buf) {
        return new ToggleRefiningGhostPacket(buf.readBlockPos());
    }

    public static void handle(ToggleRefiningGhostPacket msg, Supplier<NetworkEvent.Context> ctxSup) {
        NetworkEvent.Context ctx = ctxSup.get();
        ctx.enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> RefiningGhostRenderer.toggleCore(msg.corePos)));
        ctx.setPacketHandled(true);
    }
}
