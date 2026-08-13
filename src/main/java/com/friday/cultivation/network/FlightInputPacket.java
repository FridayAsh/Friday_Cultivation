package com.friday.cultivation.network;

import com.friday.cultivation.cultivation.CultivationCapability;
import com.friday.cultivation.cultivation.CultivationData;
import java.util.function.Supplier;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkEvent;

/**
 * 客户端 → 服务端：修仙飞行输入状态（跳跃/疾跑/潜行 + WASD 水平轴向）。
 * 服务端据此直接控制玩家运动（垂直上升/下降、水平移动），
 * 实现如创造模式的自由飞行，不依赖 mayfly/flying（绕过 Caelus 飞行管理）。
 */
public class FlightInputPacket {
    private final boolean jumpHeld;
    private final boolean sprintHeld;
    private final boolean sneakHeld;
    private final float forward;
    private final float strafe;

    public FlightInputPacket(boolean jumpHeld, boolean sprintHeld, boolean sneakHeld, float forward, float strafe) {
        this.jumpHeld = jumpHeld;
        this.sprintHeld = sprintHeld;
        this.sneakHeld = sneakHeld;
        this.forward = forward;
        this.strafe = strafe;
    }

    public static void encode(FlightInputPacket msg, FriendlyByteBuf buf) {
        buf.writeBoolean(msg.jumpHeld);
        buf.writeBoolean(msg.sprintHeld);
        buf.writeBoolean(msg.sneakHeld);
        buf.writeFloat(msg.forward);
        buf.writeFloat(msg.strafe);
    }

    public static FlightInputPacket decode(FriendlyByteBuf buf) {
        return new FlightInputPacket(buf.readBoolean(), buf.readBoolean(), buf.readBoolean(), buf.readFloat(), buf.readFloat());
    }

    public static void handle(FlightInputPacket msg, Supplier<NetworkEvent.Context> ctxSup) {
        NetworkEvent.Context ctx = ctxSup.get();
        ctx.enqueueWork(() -> {
            ServerPlayer player = ctx.getSender();
            if (player == null) {
                return;
            }
            CultivationCapability.get((Player)player).ifPresent(data -> {
                boolean swordFlight = data.isSwordFlightActive();
                boolean qiFlight = data.isQiFlightActive() && data.getCurrentQi() > 0L;
                if (!swordFlight && !qiFlight) {
                    return;
                }
                // === 原版创造模式飞行逻辑（1.20.1） ===
                // 基础飞行速度（约 0.4，对应 FLYING_SPEED 属性），疾跑 ×1.5 加速
                double speed = 0.4;
                if (msg.sprintHeld) {
                    speed *= 1.5;
                }
                // 垂直速度 = 水平速度 × 3（原版 d4 = speed × 3）
                double vertical = speed * 3.0;
                // 垂直输入：空格=+1 上升，Shift=-1 下降
                double up = (msg.jumpHeld ? 1.0 : 0.0) - (msg.sneakHeld ? 1.0 : 0.0);
                // 视角水平方向（yaw 旋转）：W 始终朝视角看的方向飞，A/D 侧移
                Vec3 look = player.getLookAngle();
                Vec3 fwdH = new Vec3(look.x, 0.0, look.z);
                double hLen = fwdH.length();
                Vec3 fwd = hLen > 0.001 ? fwdH.scale(1.0 / hLen) : new Vec3(0.0, 0.0, 0.0);
                Vec3 right = new Vec3(-look.z, 0.0, look.x);
                double rLen = right.length();
                Vec3 side = rLen > 0.001 ? right.scale(1.0 / rLen) : new Vec3(0.0, 0.0, 0.0);
                // 水平输入：WASD（forward=前后, strafe=左右）
                double forwardImpulse = msg.forward;
                double leftImpulse = msg.strafe;
                // 疾跑且无 WASD 输入：沿视线方向自动前进（更符合飞行直觉）
                if (msg.sprintHeld && forwardImpulse == 0.0 && leftImpulse == 0.0) {
                    forwardImpulse = 1.0;
                }
                // 最终速度：直接设置（无惯性，输入即动、松手即停，与原版一致）
                double vx = fwd.x * forwardImpulse * speed + side.x * leftImpulse * speed;
                double vy = up * vertical;
                double vz = fwd.z * forwardImpulse * speed + side.z * leftImpulse * speed;
                player.setDeltaMovement(new Vec3(vx, vy, vz));
                player.fallDistance = 0.0f;
                player.hurtMarked = true;
            });
        });
        ctx.setPacketHandled(true);
    }
}
