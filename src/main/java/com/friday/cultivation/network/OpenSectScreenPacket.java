/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.nbt.CompoundTag
 *  net.minecraft.network.FriendlyByteBuf
 *  net.minecraftforge.api.distmarker.Dist
 *  net.minecraftforge.fml.DistExecutor
 *  net.minecraftforge.network.NetworkEvent$Context
 */
package com.friday.cultivation.network;

import com.friday.cultivation.client.SectClientHooks;
import java.util.function.Supplier;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

public class OpenSectScreenPacket {
    private final CompoundTag snapshot;

    public OpenSectScreenPacket(CompoundTag snapshot) {
        this.snapshot = snapshot == null ? new CompoundTag() : snapshot;
    }

    public static void encode(OpenSectScreenPacket msg, FriendlyByteBuf buf) {
        buf.writeNbt(msg.snapshot);
    }

    public static OpenSectScreenPacket decode(FriendlyByteBuf buf) {
        return new OpenSectScreenPacket(buf.readNbt());
    }

    public static void handle(OpenSectScreenPacket msg, Supplier<NetworkEvent.Context> ctxSup) {
        NetworkEvent.Context ctx = ctxSup.get();
        ctx.enqueueWork(() -> DistExecutor.unsafeRunWhenOn((Dist)Dist.CLIENT, () -> () -> SectClientHooks.open(msg.snapshot)));
        ctx.setPacketHandled(true);
    }
}

