/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.network.FriendlyByteBuf
 *  net.minecraft.server.level.ServerPlayer
 *  net.minecraft.world.inventory.AbstractContainerMenu
 *  net.minecraftforge.network.NetworkEvent$Context
 */
package com.friday.cultivation.network;

import com.friday.cultivation.block.refining.RefiningCoreBlockEntity;
import com.friday.cultivation.inventory.RefiningMenu;
import java.util.function.Supplier;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraftforge.network.NetworkEvent;

public class SetRefiningAutoRetryPacket {
    private final boolean enabled;

    public SetRefiningAutoRetryPacket(boolean enabled) {
        this.enabled = enabled;
    }

    public static void encode(SetRefiningAutoRetryPacket msg, FriendlyByteBuf buf) {
        buf.writeBoolean(msg.enabled);
    }

    public static SetRefiningAutoRetryPacket decode(FriendlyByteBuf buf) {
        return new SetRefiningAutoRetryPacket(buf.readBoolean());
    }

    public static void handle(SetRefiningAutoRetryPacket msg, Supplier<NetworkEvent.Context> ctxSup) {
        NetworkEvent.Context ctx = ctxSup.get();
        ctx.enqueueWork(() -> {
            ServerPlayer player = ctx.getSender();
            if (player == null) {
                return;
            }
            AbstractContainerMenu patt1295$temp = player.containerMenu;
            if (!(patt1295$temp instanceof RefiningMenu)) {
                return;
            }
            RefiningMenu menu = (RefiningMenu)patt1295$temp;
            RefiningCoreBlockEntity be = menu.getBlockEntity();
            if (be == null) {
                return;
            }
            be.setAutoRetryUntilSuccess(msg.enabled);
            menu.broadcastChanges();
        });
        ctx.setPacketHandled(true);
    }
}

