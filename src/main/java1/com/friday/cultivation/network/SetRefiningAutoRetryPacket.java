package com.friday.cultivation.network;

import com.friday.cultivation.block.refining.RefiningCoreBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * 切换炼器自动重试包 — 客户端 → 服务端
 * 客户端点击"自动重试"开关时发送，服务端更新核心实体的自动重试标志位。
 *
 * 复刻自原模组 com.xiaoxiang.cultivation.network.SetRefiningAutoRetryPacket。
 */
public class SetRefiningAutoRetryPacket {

    private final BlockPos pos;
    private final boolean autoRetry;

    public SetRefiningAutoRetryPacket(BlockPos pos, boolean autoRetry) {
        this.pos = pos;
        this.autoRetry = autoRetry;
    }

    public static void encode(SetRefiningAutoRetryPacket msg, FriendlyByteBuf buf) {
        buf.writeBlockPos(msg.pos);
        buf.writeBoolean(msg.autoRetry);
    }

    public static SetRefiningAutoRetryPacket decode(FriendlyByteBuf buf) {
        return new SetRefiningAutoRetryPacket(buf.readBlockPos(), buf.readBoolean());
    }

    public static void handle(SetRefiningAutoRetryPacket msg, Supplier<NetworkEvent.Context> ctxSup) {
        NetworkEvent.Context ctx = ctxSup.get();
        ctx.enqueueWork(() -> {
            ServerPlayer player = ctx.getSender();
            if (player == null) return;
            ServerLevel level = (ServerLevel) player.level();
            if (!player.blockPosition().closerThan(msg.pos, 8.0)) return;
            if (!(level.getBlockEntity(msg.pos) instanceof RefiningCoreBlockEntity core)) return;

            core.setAutoRetryUntilSuccess(msg.autoRetry);
        });
        ctx.setPacketHandled(true);
    }
}
