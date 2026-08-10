/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.ChatFormatting
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
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraftforge.network.NetworkEvent;

public class SetFormationNamePacket {
    private final String name;

    public SetFormationNamePacket(String name) {
        this.name = name == null ? "" : name;
    }

    public static void encode(SetFormationNamePacket msg, FriendlyByteBuf buf) {
        buf.writeUtf(msg.name, 32);
    }

    public static SetFormationNamePacket decode(FriendlyByteBuf buf) {
        return new SetFormationNamePacket(buf.readUtf(32));
    }

    public static void handle(SetFormationNamePacket msg, Supplier<NetworkEvent.Context> ctxSup) {
        NetworkEvent.Context ctx = ctxSup.get();
        ctx.enqueueWork(() -> {
            ServerPlayer player = ctx.getSender();
            if (player == null) {
                return;
            }
            AbstractContainerMenu patt1453$temp = player.containerMenu;
            if (!(patt1453$temp instanceof FormationMenu)) {
                return;
            }
            FormationMenu menu = (FormationMenu)patt1453$temp;
            FormationCorePlateBlockEntity be = menu.getBlockEntity();
            if (be == null) {
                return;
            }
            be.setCustomName(msg.name);
            String saved = be.getCustomName();
            if (saved.isEmpty()) {
                player.displayClientMessage((Component)Component.translatable((String)"message.friday_cultivation.formation.name_cleared").withStyle(ChatFormatting.GRAY), true);
            } else {
                player.displayClientMessage((Component)Component.translatable((String)"message.friday_cultivation.formation.name_saved", (Object[])new Object[]{saved}).withStyle(ChatFormatting.GOLD), true);
            }
        });
        ctx.setPacketHandled(true);
    }
}

