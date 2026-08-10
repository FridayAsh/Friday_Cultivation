package com.friday.cultivation.network;

import com.friday.cultivation.client.ClientSoulRegistry;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.function.Supplier;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

/**
 * 灵魂状态同步包（服务端→客户端）— 完整复刻原模组 SoulStatePacket。
 * 同步当前服务器上所有处于灵魂状态的玩家UUID列表到客户端 ClientSoulRegistry。
 */
public class SoulStatePacket {
    private final List<UUID> souls;

    public SoulStatePacket(Collection<UUID> souls) {
        this.souls = new ArrayList<>(souls);
    }

    public static void encode(SoulStatePacket msg, FriendlyByteBuf buf) {
        buf.writeInt(msg.souls.size());
        for (UUID id : msg.souls) {
            buf.writeUUID(id);
        }
    }

    public static SoulStatePacket decode(FriendlyByteBuf buf) {
        int n = buf.readInt();
        ArrayList<UUID> list = new ArrayList<>(n);
        for (int i = 0; i < n; ++i) {
            list.add(buf.readUUID());
        }
        return new SoulStatePacket(list);
    }

    public static void handle(SoulStatePacket msg, Supplier<NetworkEvent.Context> ctxSup) {
        NetworkEvent.Context ctx = ctxSup.get();
        ctx.enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> ClientSoulRegistry.replaceAll(msg.souls)));
        ctx.setPacketHandled(true);
    }

    public List<UUID> souls() { return this.souls; }
}
