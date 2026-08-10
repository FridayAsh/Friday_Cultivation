package com.friday.cultivation.network;

import com.friday.cultivation.client.SoulHookVisualHandler;
import java.util.function.Supplier;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

/**
 * 勾魂视觉效果包（服务端→客户端）— 完整复刻原模组 SoulHookVisualPacket。
 * 同步勾魂状态（施法者/目标/持续时间/锁链数/漩涡阶段/激活/已用tick/漩涡锚点），
 * 客户端 SoulHookVisualHandler 据此渲染锁链或漩涡。
 */
public class SoulHookVisualPacket {
    private final int casterId;
    private final int targetId;
    private final int durationTicks;
    private final int chainCount;
    private final boolean vortexPhase;
    private final boolean active;
    private final int elapsedTicks;
    private final boolean hasVortexAnchor;
    private final double vortexX;
    private final double vortexY;
    private final double vortexZ;

    public SoulHookVisualPacket(int casterId, int targetId, int durationTicks, int chainCount, boolean vortexPhase, boolean active, int elapsedTicks, boolean hasVortexAnchor, double vortexX, double vortexY, double vortexZ) {
        this.casterId = casterId;
        this.targetId = targetId;
        this.durationTicks = durationTicks;
        this.chainCount = chainCount;
        this.vortexPhase = vortexPhase;
        this.active = active;
        this.elapsedTicks = elapsedTicks;
        this.hasVortexAnchor = hasVortexAnchor;
        this.vortexX = vortexX;
        this.vortexY = vortexY;
        this.vortexZ = vortexZ;
    }

    public static void encode(SoulHookVisualPacket msg, FriendlyByteBuf buf) {
        buf.writeInt(msg.casterId);
        buf.writeInt(msg.targetId);
        buf.writeInt(msg.durationTicks);
        buf.writeInt(msg.chainCount);
        buf.writeBoolean(msg.vortexPhase);
        buf.writeBoolean(msg.active);
        buf.writeInt(msg.elapsedTicks);
        buf.writeBoolean(msg.hasVortexAnchor);
        if (msg.hasVortexAnchor) {
            buf.writeDouble(msg.vortexX);
            buf.writeDouble(msg.vortexY);
            buf.writeDouble(msg.vortexZ);
        }
    }

    public static SoulHookVisualPacket decode(FriendlyByteBuf buf) {
        int casterId = buf.readInt();
        int targetId = buf.readInt();
        int durationTicks = buf.readInt();
        int chainCount = buf.readInt();
        boolean vortexPhase = buf.readBoolean();
        boolean active = buf.readBoolean();
        int elapsedTicks = buf.readInt();
        boolean hasVortexAnchor = buf.readBoolean();
        double vortexX = 0.0;
        double vortexY = 0.0;
        double vortexZ = 0.0;
        if (hasVortexAnchor) {
            vortexX = buf.readDouble();
            vortexY = buf.readDouble();
            vortexZ = buf.readDouble();
        }
        return new SoulHookVisualPacket(casterId, targetId, durationTicks, chainCount, vortexPhase, active, elapsedTicks, hasVortexAnchor, vortexX, vortexY, vortexZ);
    }

    public static void handle(SoulHookVisualPacket msg, Supplier<NetworkEvent.Context> ctxSup) {
        NetworkEvent.Context ctx = ctxSup.get();
        ctx.enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> SoulHookVisualHandler.onSync(msg.casterId, msg.targetId, msg.durationTicks, msg.chainCount, msg.vortexPhase, msg.active, msg.elapsedTicks, msg.hasVortexAnchor, msg.vortexX, msg.vortexY, msg.vortexZ)));
        ctx.setPacketHandled(true);
    }

    public int casterId() { return this.casterId; }
    public int targetId() { return this.targetId; }
    public int durationTicks() { return this.durationTicks; }
    public int chainCount() { return this.chainCount; }
    public boolean vortexPhase() { return this.vortexPhase; }
    public boolean active() { return this.active; }
    public int elapsedTicks() { return this.elapsedTicks; }
    public boolean hasVortexAnchor() { return this.hasVortexAnchor; }
    public double vortexX() { return this.vortexX; }
    public double vortexY() { return this.vortexY; }
    public double vortexZ() { return this.vortexZ; }
}
