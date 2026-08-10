/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.network.FriendlyByteBuf
 *  net.minecraft.network.chat.Component
 *  net.minecraft.server.level.ServerPlayer
 *  net.minecraft.world.entity.Entity
 *  net.minecraftforge.network.NetworkEvent$Context
 *  net.minecraftforge.network.PacketDistributor
 */
package com.friday.cultivation.network;

import com.friday.cultivation.cultivation.sect.SectSavedData;
import com.friday.cultivation.entity.npc.WanderingCultivatorEntity;
import com.friday.cultivation.network.ModNetwork;
import com.friday.cultivation.network.OpenSectScreenPacket;
import java.util.function.Supplier;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.PacketDistributor;

public class SectTaskActionPacket {
    private final int issuerEntityId;
    private final String taskId;
    private final boolean turnIn;

    public SectTaskActionPacket(int issuerEntityId, String taskId, boolean turnIn) {
        this.issuerEntityId = issuerEntityId;
        this.taskId = taskId == null ? "" : taskId;
        this.turnIn = turnIn;
    }

    public static void encode(SectTaskActionPacket msg, FriendlyByteBuf buf) {
        buf.writeVarInt(msg.issuerEntityId);
        buf.writeUtf(msg.taskId, 160);
        buf.writeBoolean(msg.turnIn);
    }

    public static SectTaskActionPacket decode(FriendlyByteBuf buf) {
        return new SectTaskActionPacket(buf.readVarInt(), buf.readUtf(160), buf.readBoolean());
    }

    public static void handle(SectTaskActionPacket msg, Supplier<NetworkEvent.Context> ctxSup) {
        NetworkEvent.Context ctx = ctxSup.get();
        ctx.enqueueWork(() -> {
            WanderingCultivatorEntity issuer;
            ServerPlayer player = ctx.getSender();
            if (player == null) {
                return;
            }
            Entity entity = player.serverLevel().getEntity(msg.issuerEntityId);
            if (!(entity instanceof WanderingCultivatorEntity) || !(issuer = (WanderingCultivatorEntity)entity).isAlive() || player.distanceToSqr((Entity)issuer) > 100.0) {
                player.displayClientMessage((Component)Component.translatable((String)"message.friday_cultivation.sect.task.wrong_issuer"), true);
                return;
            }
            SectSavedData data = SectSavedData.get(player.serverLevel());
            SectSavedData.TaskActionResult result = msg.turnIn ? data.turnInTask(player, issuer, msg.taskId) : data.acceptTask(player, issuer, msg.taskId);
            player.displayClientMessage(result.message(), true);
            ModNetwork.CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), (Object)new OpenSectScreenPacket(data.snapshot(player, issuer)));
        });
        ctx.setPacketHandled(true);
    }
}

