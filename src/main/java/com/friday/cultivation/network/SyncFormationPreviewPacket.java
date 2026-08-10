/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.BlockPos
 *  net.minecraft.network.FriendlyByteBuf
 *  net.minecraftforge.api.distmarker.Dist
 *  net.minecraftforge.fml.DistExecutor
 *  net.minecraftforge.network.NetworkEvent$Context
 */
package com.friday.cultivation.network;

import com.friday.cultivation.client.ClientFormationRangePreview;
import java.util.function.Supplier;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

public class SyncFormationPreviewPacket {
    private final BlockPos corePos;
    private final BlockPos flagPos;
    private final int radius;
    private final int typeOrdinal;
    private final boolean visible;

    public SyncFormationPreviewPacket(BlockPos corePos, BlockPos flagPos, int radius, int typeOrdinal, boolean visible) {
        this.corePos = corePos.east();
        this.flagPos = flagPos.east();
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

