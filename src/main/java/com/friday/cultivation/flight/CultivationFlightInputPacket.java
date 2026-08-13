package com.friday.cultivation.flight;

import java.util.function.Supplier;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkEvent;

/**
 * 客户端 → 服务端：修仙飞行输入状态（跳跃/疾跑/潜行）。
 * 服务端据此直接控制飞行运动（上升/加速/减速）。
 */
public class CultivationFlightInputPacket {
    private final boolean jumpHeld;
    private final boolean sprintHeld;
    private final boolean sneakHeld;

    public CultivationFlightInputPacket(boolean jumpHeld, boolean sprintHeld, boolean sneakHeld) {
        this.jumpHeld = jumpHeld;
        this.sprintHeld = sprintHeld;
        this.sneakHeld = sneakHeld;
    }

    public static void encode(CultivationFlightInputPacket msg, FriendlyByteBuf buf) {
        buf.writeBoolean(msg.jumpHeld);
        buf.writeBoolean(msg.sprintHeld);
        buf.writeBoolean(msg.sneakHeld);
    }

    public static CultivationFlightInputPacket decode(FriendlyByteBuf buf) {
        return new CultivationFlightInputPacket(buf.readBoolean(), buf.readBoolean(), buf.readBoolean());
    }

    public static void handle(CultivationFlightInputPacket msg, Supplier<NetworkEvent.Context> ctxSup) {
        NetworkEvent.Context ctx = ctxSup.get();
        ctx.enqueueWork(() -> {
            ServerPlayer player = ctx.getSender();
            if (player == null) {
                return;
            }
            if (!player.getAbilities().flying) {
                return;
            }
            if (!CultivationFlightHandler.isSwordFlightActive(player) && !CultivationFlightHandler.canQiFlight(player)) {
                return;
            }
            Vec3 motion = player.getDeltaMovement();
            if (msg.jumpHeld) {
                player.setDeltaMovement(motion.add(0.0, 0.12, 0.0));
            }
            if (msg.sprintHeld) {
                Vec3 look = player.getLookAngle();
                player.setDeltaMovement(motion.add(look.x * 0.8, look.y * 0.8, look.z * 0.8));
            }
            if (msg.sneakHeld) {
                player.setDeltaMovement(motion.multiply(0.5, 0.3, 0.5));
            }
            if (motion.y < -0.5) {
                player.setDeltaMovement(motion.x, -0.5, motion.z);
            }
            player.hurtMarked = true;
        });
        ctx.setPacketHandled(true);
    }
}
