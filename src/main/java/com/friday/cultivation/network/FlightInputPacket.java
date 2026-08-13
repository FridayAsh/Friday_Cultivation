package com.friday.cultivation.network;

import com.friday.cultivation.cultivation.CultivationCapability;
import com.friday.cultivation.cultivation.CultivationData;
import com.friday.cultivation.cultivation.spell.Spell;
import java.util.function.Supplier;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkEvent;

/**
 * 客户端 → 服务端：修仙飞行输入状态（跳跃/疾跑/潜行按住）。
 * 服务端据此直接控制玩家运动（垂直上升/水平加速/减速），
 * 实现如创造模式的自由飞行，不依赖 mayfly/flying（绕过 Caelus 飞行管理）。
 */
public class FlightInputPacket {
    private final boolean jumpHeld;
    private final boolean sprintHeld;
    private final boolean sneakHeld;

    public FlightInputPacket(boolean jumpHeld, boolean sprintHeld, boolean sneakHeld) {
        this.jumpHeld = jumpHeld;
        this.sprintHeld = sprintHeld;
        this.sneakHeld = sneakHeld;
    }

    public static void encode(FlightInputPacket msg, FriendlyByteBuf buf) {
        buf.writeBoolean(msg.jumpHeld);
        buf.writeBoolean(msg.sprintHeld);
        buf.writeBoolean(msg.sneakHeld);
    }

    public static FlightInputPacket decode(FriendlyByteBuf buf) {
        return new FlightInputPacket(buf.readBoolean(), buf.readBoolean(), buf.readBoolean());
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
                boolean qiFlight = data.isSpellEnabled(Spell.QI_FLIGHT) && data.getCurrentQi() > 0L;
                if (!swordFlight && !qiFlight) {
                    return;
                }
                Vec3 motion = player.getDeltaMovement();
                // 按住跳跃：上升（每次基于最新垂直速度，避免旧值叠加失真）
                if (msg.jumpHeld) {
                    motion = motion.add(0.0, 0.12, 0.0);
                } else {
                    // 松开跳跃：垂直速度缓慢归零，实现稳定悬浮
                    double vy = motion.y;
                    if (Math.abs(vy) > 0.001) {
                        motion = motion.add(0.0, -vy * 0.4, 0.0);
                    }
                }
                // 按住潜行：主动下降（每次基于最新垂直速度）
                if (msg.sneakHeld) {
                    motion = motion.add(0.0, -0.12, 0.0);
                }
                // 按住疾跑：沿视线方向加速（基于最新速度）
                if (msg.sprintHeld) {
                    Vec3 look = player.getLookAngle();
                    motion = motion.add(look.x * 0.8, look.y * 0.8, look.z * 0.8);
                }
                // 下落速度钳制，防止突然坠地
                if (motion.y < -0.5) {
                    motion = new Vec3(motion.x, -0.5, motion.z);
                }
                player.setDeltaMovement(motion);
                player.fallDistance = 0.0f;
                player.hurtMarked = true;
            });
        });
        ctx.setPacketHandled(true);
    }
}
