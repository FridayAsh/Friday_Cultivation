package com.friday.cultivation.network;

import com.friday.cultivation.cultivation.CultivationCapability;
import com.friday.cultivation.cultivation.CultivationData;
import com.friday.cultivation.cultivation.spell.Spell;
import com.friday.cultivation.event.CapabilityEvents;
import java.util.function.Supplier;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkEvent;

/**
 * 客户端 → 服务端：切换灵气飞行状态（双击空格触发）。
 * 服务端更新 CultivationData.qiFlightActive 并同步回客户端。
 */
public class QiFlightTogglePacket {
    public QiFlightTogglePacket() {
    }

    public static void encode(QiFlightTogglePacket msg, FriendlyByteBuf buf) {
    }

    public static QiFlightTogglePacket decode(FriendlyByteBuf buf) {
        return new QiFlightTogglePacket();
    }

    public static void handle(QiFlightTogglePacket msg, Supplier<NetworkEvent.Context> ctxSup) {
        NetworkEvent.Context ctx = ctxSup.get();
        ctx.enqueueWork(() -> {
            ServerPlayer player = ctx.getSender();
            if (player == null) {
                return;
            }
            CultivationCapability.get((Player)player).ifPresent(data -> {
                if (!data.hasSpell(Spell.QI_FLIGHT)) {
                    return;
                }
                boolean activating = !data.isQiFlightActive();
                data.setQiFlightActive(activating);
                if (activating) {
                    // 激活：重置垂直速度为 0（原地悬浮，不弹跳；上升需按空格）
                    Vec3 motion = player.getDeltaMovement();
                    player.setDeltaMovement(motion.x, 0.0, motion.z);
                    player.hurtMarked = true;
                }
                CapabilityEvents.syncToClient(player);
            });
        });
        ctx.setPacketHandled(true);
    }
}
