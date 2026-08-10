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

import com.friday.cultivation.client.RefiningGhostRenderer;
import java.util.function.Supplier;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

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
        ctx.enqueueWork(() -> DistExecutor.unsafeRunWhenOn((Dist)Dist.CLIENT, () -> () -> RefiningGhostRenderer.toggleCore(msg.corePos)));
        ctx.setPacketHandled(true);
    }
}

