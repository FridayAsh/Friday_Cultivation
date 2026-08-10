package com.friday.cultivation.network;

import com.friday.cultivation.client.ClientSoulReaperTargetHooks;
import com.friday.cultivation.network.SoulReaperTargetEntry;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

/**
 * 勾魂目标列表同步包 — 完整复刻原模组 SoulReaperTargetsPacket。
 * <p>
 * 服务端 → 客户端：携带一组 {@link SoulReaperTargetEntry}（玩家/NPC 的可勾魂目标）。
 * 客户端调用 {@link ClientSoulReaperTargetHooks#open(List)} 自动打开或更新 SoulReaperTargetScreen。
 * </p>
 */
public class SoulReaperTargetsPacket {
    private final List<SoulReaperTargetEntry> targets;

    public SoulReaperTargetsPacket(List<SoulReaperTargetEntry> targets) {
        this.targets = List.copyOf(targets);
    }

    public static void encode(SoulReaperTargetsPacket msg, FriendlyByteBuf buf) {
        buf.writeVarInt(msg.targets.size());
        for (SoulReaperTargetEntry entry : msg.targets) {
            entry.encode(buf);
        }
    }

    public static SoulReaperTargetsPacket decode(FriendlyByteBuf buf) {
        int count = buf.readVarInt();
        ArrayList<SoulReaperTargetEntry> targets = new ArrayList<SoulReaperTargetEntry>(count);
        for (int i = 0; i < count; ++i) {
            targets.add(SoulReaperTargetEntry.decode(buf));
        }
        return new SoulReaperTargetsPacket(targets);
    }

    public static void handle(SoulReaperTargetsPacket msg, Supplier<NetworkEvent.Context> ctxSup) {
        NetworkEvent.Context ctx = ctxSup.get();
        ctx.enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> ClientSoulReaperTargetHooks.open(msg.targets)));
        ctx.setPacketHandled(true);
    }

    public List<SoulReaperTargetEntry> targets() {
        return this.targets;
    }
}
