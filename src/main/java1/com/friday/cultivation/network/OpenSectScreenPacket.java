package com.friday.cultivation.network;

import com.friday.cultivation.client.SectClientHooks;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * 打开宗门界面包 - 严格 1:1 复刻原模组
 * 混淆名映射: m_130079_=writeNbt, m_130260_=readNbt
 */
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
