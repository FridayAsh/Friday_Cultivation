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
 */
package com.friday.cultivation.network;

import com.friday.cultivation.block.formation.FormationCorePlateBlockEntity;
import com.friday.cultivation.inventory.FormationMenu;
import java.util.function.Supplier;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraftforge.network.NetworkEvent;

public class SetFormationFlagRadiusPacket {
    private final BlockPos flagPos;
    private final int radius;

    public SetFormationFlagRadiusPacket(BlockPos flagPos, int radius) {
        this.flagPos = flagPos.east();
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
            FormationMenu menu = (FormationMenu)patt1403$temp;
            FormationCorePlateBlockEntity be = menu.getBlockEntity();
            if (be == null) {
                return;
            }
            int clamped = FormationCorePlateBlockEntity.clampFlagEffectRadius(msg.radius);
            boolean changed = be.setFlagEffectRadius(msg.flagPos, clamped);
            if (changed) {
                player.displayClientMessage((Component)Component.translatable((String)"message.friday_cultivation.formation.flag_radius_saved", (Object[])new Object[]{clamped}).withStyle(ChatFormatting.GOLD), true);
                menu.broadcastChanges();
                menu.sendFlagSync();
            } else {
                player.displayClientMessage((Component)Component.translatable((String)"message.friday_cultivation.formation.flag_invalid").withStyle(ChatFormatting.RED), true);
            }
        });
        ctx.setPacketHandled(true);
    }
}

