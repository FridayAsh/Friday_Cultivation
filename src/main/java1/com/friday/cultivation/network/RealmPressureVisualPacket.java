package com.friday.cultivation.network;

import com.friday.cultivation.client.RealmPressureClientEffects;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * 境界压制视觉同步包（服务端→客户端）— 严格复刻自原模组
 * com.xiaoxiang.cultivation.network.RealmPressureVisualPacket
 * <p>
 * 3 个 mode 区分：
 * <ul>
 *   <li>TARGET (0) — 目标生物：{@link #target(int, int, boolean)}</li>
 *   <li>CASTER (1) — 施法者光环：{@link #caster(int, int, boolean)}</li>
 *   <li>EXPANSION (2) — 扩散球壳：{@link #expansion(int, float, int, boolean)}</li>
 * </ul>
 */
public class RealmPressureVisualPacket {
    private static final int MODE_TARGET = 0;
    private static final int MODE_CASTER = 1;
    private static final int MODE_EXPANSION = 2;

    private final int mode;
    private final boolean active;
    private final int entityId;
    private final int casterId;
    private final int durationTicks;
    private final float radius;

    private RealmPressureVisualPacket(int mode, boolean active, int entityId, int casterId, int durationTicks, float radius) {
        this.mode = mode;
        this.active = active;
        this.entityId = entityId;
        this.casterId = casterId;
        this.durationTicks = durationTicks;
        this.radius = radius;
    }

    public static RealmPressureVisualPacket target(int entityId, int durationTicks, boolean active) {
        return new RealmPressureVisualPacket(MODE_TARGET, active, entityId, 0, durationTicks, 0.0f);
    }

    public static RealmPressureVisualPacket caster(int entityId, int durationTicks, boolean active) {
        return new RealmPressureVisualPacket(MODE_CASTER, active, entityId, 0, durationTicks, 0.0f);
    }

    public static RealmPressureVisualPacket expansion(int casterId, float radius, int durationTicks, boolean active) {
        return new RealmPressureVisualPacket(MODE_EXPANSION, active, 0, casterId, durationTicks, radius);
    }

    public static void encode(RealmPressureVisualPacket msg, FriendlyByteBuf buf) {
        buf.writeInt(msg.mode);
        buf.writeBoolean(msg.active);
        buf.writeInt(msg.entityId);
        buf.writeInt(msg.casterId);
        buf.writeInt(msg.durationTicks);
        buf.writeFloat(msg.radius);
    }

    public static RealmPressureVisualPacket decode(FriendlyByteBuf buf) {
        int mode = buf.readInt();
        boolean active = buf.readBoolean();
        int entityId = buf.readInt();
        int casterId = buf.readInt();
        int durationTicks = buf.readInt();
        float radius = buf.readFloat();
        return new RealmPressureVisualPacket(mode, active, entityId, casterId, durationTicks, radius);
    }

    public static void handle(RealmPressureVisualPacket msg, Supplier<NetworkEvent.Context> ctxSup) {
        NetworkEvent.Context ctx = ctxSup.get();
        ctx.enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> RealmPressureClientEffects.onSync(msg.mode, msg.active, msg.entityId, msg.casterId, msg.durationTicks, msg.radius)));
        ctx.setPacketHandled(true);
    }
}
