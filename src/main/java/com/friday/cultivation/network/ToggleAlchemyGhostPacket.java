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

import com.friday.cultivation.client.AlchemyGhostRenderer;
import java.util.function.Supplier;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

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
        ctx.enqueueWork(() -> DistExecutor.unsafeRunWhenOn((Dist)Dist.CLIENT, () -> () -> AlchemyGhostRenderer.toggleCore(msg.corePos)));
        ctx.setPacketHandled(true);
    }
}

