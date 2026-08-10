package com.friday.cultivation.network;

import com.friday.cultivation.client.ClientFormationRangePreview;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * 同步阵法预览包 - 严格 1:1 复刻原模组
 * 混淆名映射: m_130064_=writeBlockPos, m_130135_=readBlockPos,
 *             m_130130_=writeVarInt, m_130242_=readVarInt, m_7949_=immutable
 */
public class SyncFormationPreviewPacket {
    private final BlockPos corePos;
    private final BlockPos flagPos;
    private final int radius;
    private final int typeOrdinal;
    private final boolean visible;

    public SyncFormationPreviewPacket(BlockPos corePos, BlockPos flagPos, int radius, int typeOrdinal, boolean visible) {
        this.corePos = corePos.immutable();
        this.flagPos = flagPos.immutable();
        this.radius = radius;
        this.typeOrdinal = typeOrdinal;
        this.visible = visible;
    }

    public static void encode(SyncFormationPreviewPacket msg, FriendlyByteBuf buf) {
        buf.writeBlockPos(msg.corePos);
        buf.writeBlockPos(msg.flagPos);
        buf.writeVarInt(msg.radius);
        buf.writeVarInt(msg.typeOrdinal);
        buf.writeBoolean(msg.visible);
    }

    public static SyncFormationPreviewPacket decode(FriendlyByteBuf buf) {
        return new SyncFormationPreviewPacket(buf.readBlockPos(), buf.readBlockPos(), buf.readVarInt(), buf.readVarInt(), buf.readBoolean());
    }

    public static void handle(SyncFormationPreviewPacket msg, Supplier<NetworkEvent.Context> ctxSup) {
        NetworkEvent.Context ctx = ctxSup.get();
        ctx.enqueueWork(() -> DistExecutor.unsafeRunWhenOn((Dist)Dist.CLIENT, () -> () -> {
            if (msg.visible) {
                ClientFormationRangePreview.show(msg.corePos, msg.flagPos, msg.radius, msg.typeOrdinal);
            } else {
                ClientFormationRangePreview.hide(msg.corePos, msg.flagPos);
            }
        }));
        ctx.setPacketHandled(true);
    }
}
