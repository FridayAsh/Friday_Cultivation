package com.friday.cultivation.network;

import com.friday.cultivation.client.LifeBalanceVisualHandler;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * 命元天平视觉同步包（服务端→客户端）— 严格复刻自原模组
 * com.xiaoxiang.cultivation.network.LifeBalanceVisualPacket
 * <p>
 * mode 区分两种同步：
 * <ul>
 *   <li>MARK (mode=0) — 头顶方块特效（{@link #mark}）</li>
 *   <li>LINK (mode=1) — 链状缠绕 caster↔target（{@link #link}）</li>
 * </ul>
 */
public class LifeBalanceVisualPacket {
    private static final int MODE_MARK = 0;
    private static final int MODE_LINK = 1;

    private final int mode;
    private final int entityId;
    private final int casterId;
    private final int targetId;
    private final int durationTicks;
    private final boolean active;

    private LifeBalanceVisualPacket(int mode, int entityId, int casterId, int targetId, int durationTicks, boolean active) {
        this.mode = mode;
        this.entityId = entityId;
        this.casterId = casterId;
        this.targetId = targetId;
        this.durationTicks = durationTicks;
        this.active = active;
    }

    public static LifeBalanceVisualPacket mark(int entityId, int durationTicks, boolean active) {
        return new LifeBalanceVisualPacket(MODE_MARK, entityId, 0, 0, durationTicks, active);
    }

    public static LifeBalanceVisualPacket link(int casterId, int targetId, int durationTicks, boolean active) {
        return new LifeBalanceVisualPacket(MODE_LINK, 0, casterId, targetId, durationTicks, active);
    }

    public static void encode(LifeBalanceVisualPacket msg, FriendlyByteBuf buf) {
        buf.writeInt(msg.mode);
        buf.writeBoolean(msg.active);
        buf.writeInt(msg.entityId);
        buf.writeInt(msg.casterId);
        buf.writeInt(msg.targetId);
        buf.writeInt(msg.durationTicks);
    }

    public static LifeBalanceVisualPacket decode(FriendlyByteBuf buf) {
        int mode = buf.readInt();
        boolean active = buf.readBoolean();
        int entityId = buf.readInt();
        int casterId = buf.readInt();
        int targetId = buf.readInt();
        int durationTicks = buf.readInt();
        return new LifeBalanceVisualPacket(mode, entityId, casterId, targetId, durationTicks, active);
    }

    public static void handle(LifeBalanceVisualPacket msg, Supplier<NetworkEvent.Context> ctxSup) {
        NetworkEvent.Context ctx = ctxSup.get();
        ctx.enqueueWork(() -> {
            if (msg.mode == MODE_LINK) {
                DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> LifeBalanceVisualHandler.onLinkSync(msg.casterId, msg.targetId, msg.durationTicks, msg.active));
            } else {
                DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> LifeBalanceVisualHandler.onMarkSync(msg.entityId, msg.durationTicks, msg.active));
            }
        });
        ctx.setPacketHandled(true);
    }
}
