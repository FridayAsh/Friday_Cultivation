package com.friday.cultivation.network;

import com.friday.cultivation.CultivationCapability;
import com.friday.cultivation.capability.CultivationData;
import com.friday.cultivation.event.SoulStateHandler;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * 请求前往地府包 — 客户端 → 服务端（照搬原模组 RequestGoDifuPacket）
 * 灵魂状态玩家请求主动前往地府。
 */
public class RequestGoDifuPacket {
    public static void encode(RequestGoDifuPacket msg, FriendlyByteBuf buf) {
    }

    public static RequestGoDifuPacket decode(FriendlyByteBuf buf) {
        return new RequestGoDifuPacket();
    }

    public static void handle(RequestGoDifuPacket msg, Supplier<NetworkEvent.Context> ctxSup) {
        NetworkEvent.Context ctx = ctxSup.get();
        ctx.enqueueWork(() -> {
            ServerPlayer player = ctx.getSender();
            if (player == null) {
                return;
            }
            CultivationData ic = CultivationCapability.get(player).orElse(null);
            if (ic == null || !ic.isSoulState()) {
                return;
            }
            // 已在 / 或 目标维度为地府时不再重复传送
            if (player.level().dimension().location().getNamespace().equals("friday_cultivation")
                    && player.level().dimension().location().getPath().equals("difu")) {
                return;
            }
            if (ic.isSoulDeathChoicePending()) {
                SoulStateHandler.resolveDeathChoice(player, DeathChoicePacket.Choice.GO_DIFU);
                return;
            }
            SoulStateHandler.beginVoluntaryDifuTransfer(player);
        });
        ctx.setPacketHandled(true);
    }
}
