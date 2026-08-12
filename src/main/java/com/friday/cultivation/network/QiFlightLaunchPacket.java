package com.friday.cultivation.network;

import com.friday.cultivation.cultivation.CultivationCapability;
import com.friday.cultivation.cultivation.CultivationData;
import com.friday.cultivation.cultivation.spell.Spell;
import com.friday.cultivation.event.PassiveSpellHandler;
import java.util.function.Supplier;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.network.NetworkEvent;

/**
 * 灵气飞行起飞请求：客户端检测到双击空格时发送，
 * 服务端校验后可立即进入飞行状态（mayfly 由被动飞行逻辑授权）。
 */
public class QiFlightLaunchPacket {
    public QiFlightLaunchPacket() {
    }

    public static void encode(QiFlightLaunchPacket msg, FriendlyByteBuf buf) {
    }

    public static QiFlightLaunchPacket decode(FriendlyByteBuf buf) {
        return new QiFlightLaunchPacket();
    }

    public static void handle(QiFlightLaunchPacket msg, Supplier<NetworkEvent.Context> ctxSup) {
        NetworkEvent.Context ctx = ctxSup.get();
        ctx.enqueueWork(() -> {
            ServerPlayer player = ctx.getSender();
            if (player == null || player.isCreative() || player.isSpectator()) {
                return;
            }
            CultivationCapability.get((Player)player).ifPresent(data -> {
                if (!PassiveSpellHandler.hasEnabledPassiveFlight(data)) {
                    return;
                }
                if (!data.isSpellEnabled(Spell.QI_FLIGHT) || data.getCurrentQi() <= 0L) {
                    return;
                }
                if (data.isSwordFlightActive() || data.isVoidEscapeActive()) {
                    return;
                }
                player.getAbilities().mayfly = true;
                player.getAbilities().flying = true;
                player.getAbilities().setFlyingSpeed(0.05f);
                player.fallDistance = 0.0f;
                player.onUpdateAbilities();
            });
        });
        ctx.setPacketHandled(true);
    }
}
