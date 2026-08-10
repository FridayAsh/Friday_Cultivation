package com.friday.cultivation.network;

import com.friday.cultivation.block.formation.FormationCorePlateBlockEntity;
import com.friday.cultivation.inventory.FormationMenu;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * 设置阵法旗面半径包 — 严格 1:1 复刻原 mod com.xiaoxiang.cultivation.network.SetFormationFlagRadiusPacket。
 * 玩家在阵法控制台调整单个旗面的生效半径。
 */
public class SetFormationFlagRadiusPacket {
    private final BlockPos flagPos;
    private final int radius;

    public SetFormationFlagRadiusPacket(BlockPos flagPos, int radius) {
        this.flagPos = flagPos.immutable();
        this.radius = radius;
    }

    public static void encode(SetFormationFlagRadiusPacket msg, FriendlyByteBuf buf) {
        buf.writeBlockPos(msg.flagPos);
        buf.writeVarInt(msg.radius);
    }

    public static SetFormationFlagRadiusPacket decode(FriendlyByteBuf buf) {
        return new SetFormationFlagRadiusPacket(buf.readBlockPos(), buf.readVarInt());
    }

    public static void handle(SetFormationFlagRadiusPacket msg, Supplier<NetworkEvent.Context> ctxSup) {
        NetworkEvent.Context ctx = ctxSup.get();
        ctx.enqueueWork(() -> {
            ServerPlayer player = ctx.getSender();
            if (player == null) {
                return;
            }
            AbstractContainerMenu patt1403$temp = player.containerMenu;
            if (!(patt1403$temp instanceof FormationMenu)) {
                return;
            }
            FormationMenu menu = (FormationMenu) patt1403$temp;
            FormationCorePlateBlockEntity be = menu.getBlockEntity();
            if (be == null) {
                return;
            }
            int clamped = FormationCorePlateBlockEntity.clampFlagEffectRadius(msg.radius);
            boolean changed = be.setFlagEffectRadius(msg.flagPos, clamped);
            if (changed) {
                player.displayClientMessage(Component.translatable("message.friday_cultivation.formation.flag_radius_saved", clamped).withStyle(ChatFormatting.GOLD), true);
                menu.broadcastChanges();
                menu.sendFlagSync();
            } else {
                player.displayClientMessage(Component.translatable("message.friday_cultivation.formation.flag_invalid").withStyle(ChatFormatting.RED), true);
            }
        });
        ctx.setPacketHandled(true);
    }
}
