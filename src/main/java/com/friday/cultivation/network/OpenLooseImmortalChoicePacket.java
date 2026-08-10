/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.network.FriendlyByteBuf
 *  net.minecraftforge.api.distmarker.Dist
 *  net.minecraftforge.fml.DistExecutor
 *  net.minecraftforge.network.NetworkEvent$Context
 */
package com.friday.cultivation.network;

import com.friday.cultivation.client.ClientLooseImmortalHooks;
import java.util.function.Supplier;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

public class OpenLooseImmortalChoicePacket {
    public static void encode(OpenLooseImmortalChoicePacket msg, FriendlyByteBuf buf) {
    }

    public static OpenLooseImmortalChoicePacket decode(FriendlyByteBuf buf) {
        return new OpenLooseImmortalChoicePacket();
    }

    public static void handle(OpenLooseImmortalChoicePacket msg, Supplier<NetworkEvent.Context> ctxSup) {
        NetworkEvent.Context ctx = ctxSup.get();
        ctx.enqueueWork(() -> DistExecutor.unsafeRunWhenOn((Dist)Dist.CLIENT, () -> () -> ClientLooseImmortalHooks.openChoice()));
        ctx.setPacketHandled(true);
    }
}

