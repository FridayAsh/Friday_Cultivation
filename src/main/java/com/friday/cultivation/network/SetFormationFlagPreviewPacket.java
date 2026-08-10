/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.ChatFormatting
 *  net.minecraft.core.BlockPos
 *  net.minecraft.network.FriendlyByteBuf
 *  net.minecraft.network.chat.Component
 *  net.minecraft.server.level.ServerPlayer
 *  net.minecraft.world.inventory.AbstractContainerMenu
 *  net.minecraftforge.network.NetworkEvent$Context
 *  net.minecraftforge.network.PacketDistributor
 */
package com.friday.cultivation.network;

import com.friday.cultivation.block.formation.FormationCorePlateBlockEntity;
import com.friday.cultivation.inventory.FormationMenu;
import com.friday.cultivation.network.ModNetwork;
import com.friday.cultivation.network.SyncFormationPreviewPacket;
import java.util.function.Supplier;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.PacketDistributor;

public class SetFormationFlagPreviewPacket {
    private final BlockPos flagPos;
    private final boolean visible;

    public SetFormationFlagPreviewPacket(BlockPos flagPos, boolean visible) {
        this.flagPos = flagPos.east();
        this.visible = visible;
    }

    public static void encode(SetFormationFlagPreviewPacket msg, FriendlyByteBuf buf) {
        buf.writeBlockPos(msg.flagPos);
        buf.writeBoolean(msg.visible);
    }

    public static SetFormationFlagPreviewPacket decode(FriendlyByteBuf buf) {
        return new SetFormationFlagPreviewPacket(buf.readBlockPos(), buf.readBoolean());
    }

    public static void handle(SetFormationFlagPreviewPacket msg, Supplier<NetworkEvent.Context> ctxSup) {
        NetworkEvent.Context ctx = ctxSup.get();
        ctx.enqueueWork(() -> {
            ServerPlayer player = ctx.getSender();
            if (player == null) {
                return;
            }
            AbstractContainerMenu patt1477$temp = player.containerMenu;
            if (!(patt1477$temp instanceof FormationMenu)) {
                return;
            }
            FormationMenu menu = (FormationMenu)patt1477$temp;
            FormationCorePlateBlockEntity be = menu.getBlockEntity();
            if (be == null) {
                return;
            }
            FormationCorePlateBlockEntity.FlagLinkView matched = null;
            for (FormationCorePlateBlockEntity.FlagLinkView view : be.getConnectedFlagViews()) {
                if (!view.pos().equals((Object)msg.flagPos)) continue;
                matched = view;
                break;
            }
            if (matched == null) {
                player.displayClientMessage((Component)Component.translatable((String)"message.friday_cultivation.formation.flag_invalid").withStyle(ChatFormatting.RED), true);
                return;
            }
            ModNetwork.CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), (Object)new SyncFormationPreviewPacket(be.getBlockPos(), matched.pos(), matched.radius(), matched.type().ordinal(), msg.visible));
            player.displayClientMessage((Component)Component.translatable((String)(msg.visible ? "message.friday_cultivation.formation.preview_shown" : "message.friday_cultivation.formation.preview_hidden")).withStyle(msg.visible ? ChatFormatting.AQUA : ChatFormatting.GRAY), true);
        });
        ctx.setPacketHandled(true);
    }
}

