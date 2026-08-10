package com.friday.cultivation.network;

import com.friday.cultivation.client.ClientLooseImmortalHooks;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * 打开散仙选择数据包（严格照搬原模组 com.xiaoxiang.cultivation.network.OpenLooseImmortalChoicePacket）
 */
public class OpenLooseImmortalChoicePacket {
    public OpenLooseImmortalChoicePacket() {}

    public static void encode(OpenLooseImmortalChoicePacket msg, FriendlyByteBuf buf) {}

    public static OpenLooseImmortalChoicePacket decode(FriendlyByteBuf buf) {
        return new OpenLooseImmortalChoicePacket();
    }

    public static void handle(OpenLooseImmortalChoicePacket msg, Supplier<NetworkEvent.Context> ctxSup) {
        NetworkEvent.Context ctx = ctxSup.get();
        ctx.enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> ClientLooseImmortalHooks::openChoice));
        ctx.setPacketHandled(true);
    }
}