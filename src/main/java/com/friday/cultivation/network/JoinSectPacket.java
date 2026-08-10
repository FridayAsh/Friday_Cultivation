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

import com.friday.cultivation.cultivation.sect.SectRole;
import com.friday.cultivation.cultivation.sect.SectSavedData;
import com.friday.cultivation.entity.npc.WanderingCultivatorEntity;
import com.friday.cultivation.event.CapabilityEvents;
import com.friday.cultivation.network.ModNetwork;
import com.friday.cultivation.network.OpenSectScreenPacket;
import java.util.function.Supplier;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.PacketDistributor;

public class JoinSectPacket {
    private final int masterEntityId;

    public JoinSectPacket(int masterEntityId) {
        this.masterEntityId = masterEntityId;
    }

    public static void encode(JoinSectPacket msg, FriendlyByteBuf buf) {
        buf.writeVarInt(msg.masterEntityId);
    }

    public static JoinSectPacket decode(FriendlyByteBuf buf) {
        return new JoinSectPacket(buf.readVarInt());
    }

    public static void handle(JoinSectPacket msg, Supplier<NetworkEvent.Context> ctxSup) {
        NetworkEvent.Context ctx = ctxSup.get();
        ctx.enqueueWork(() -> {
            WanderingCultivatorEntity master;
            ServerPlayer player = ctx.getSender();
            if (player == null) {
                return;
            }
            Entity entity = player.serverLevel().getEntity(msg.masterEntityId);
            if (!(entity instanceof WanderingCultivatorEntity) || (master = (WanderingCultivatorEntity)entity).getSectRole() != SectRole.MASTER || player.distanceToSqr((Entity)master) > 100.0) {
                player.displayClientMessage((Component)Component.translatable((String)"message.friday_cultivation.sect.join.master_only"), true);
                return;
            }
            SectSavedData data = SectSavedData.get(player.serverLevel());
            if (data.isRegisteredMember(player)) {
                player.displayClientMessage((Component)Component.translatable((String)"message.friday_cultivation.sect.join.already_member"), true);
                return;
            }
            String sectId = master.getSectId();
            if (sectId.isBlank()) {
                player.displayClientMessage((Component)Component.translatable((String)"message.friday_cultivation.sect.join.no_sect"), true);
                return;
            }
            if (data.byId(sectId).isEmpty()) {
                player.displayClientMessage((Component)Component.translatable((String)"message.friday_cultivation.sect.join.no_sect"), true);
                return;
            }
            if (data.isEnemyOf(player, sectId)) {
                player.displayClientMessage((Component)Component.translatable((String)"message.friday_cultivation.sect.join.hostile", (Object[])new Object[]{master.getSectName()}), true);
                return;
            }
            data.joinPlayer(player, sectId);
            CapabilityEvents.syncToClient(player);
            player.displayClientMessage((Component)Component.translatable((String)"message.friday_cultivation.sect.joined", (Object[])new Object[]{master.getSectName()}), true);
            ModNetwork.CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), (Object)new OpenSectScreenPacket(data.snapshot(player, master)));
        });
        ctx.setPacketHandled(true);
    }
}

