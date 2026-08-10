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

import com.friday.cultivation.client.ClientShieldRippleHandler;
import java.util.function.Supplier;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

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

