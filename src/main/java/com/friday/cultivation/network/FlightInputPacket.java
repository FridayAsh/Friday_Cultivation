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
 * 客户端 → 服务端：灵气飞行输入状态（跳跃/疾跑/潜行按住）。
 * 对照 DivineArsenal FlightInputPacket 完全一致。
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
            if (!player.getAbilities().flying) {
                return;
            }
            CultivationCapability.get((Player)player).ifPresent(data -> {
                if (!data.isQiFlightActive() || data.getCurrentQi() <= 0L) {
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
        });
        ctx.setPacketHandled(true);
    }
}
