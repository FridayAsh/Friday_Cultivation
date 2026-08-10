/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.nbt.CompoundTag
 *  net.minecraft.network.FriendlyByteBuf
 *  net.minecraft.network.chat.Component
 *  net.minecraft.server.level.ServerPlayer
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.inventory.AbstractContainerMenu
 *  net.minecraftforge.network.NetworkEvent$Context
 *  net.minecraftforge.network.PacketDistributor
 */
package com.friday.cultivation.network;

import com.friday.cultivation.cultivation.sect.SectSavedData;
import com.friday.cultivation.entity.npc.WanderingCultivatorEntity;
import com.friday.cultivation.inventory.WanderingCultivatorMenu;
import com.friday.cultivation.network.ModNetwork;
import com.friday.cultivation.network.OpenSectJoinDialoguePacket;
import java.util.function.Supplier;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.PacketDistributor;

public class RequestSectJoinDialoguePacket {
    private final int targetEntityId;

    public RequestSectJoinDialoguePacket(int targetEntityId) {
        this.targetEntityId = targetEntityId;
    }

    public static void encode(RequestSectJoinDialoguePacket msg, FriendlyByteBuf buf) {
        buf.writeVarInt(msg.targetEntityId);
    }

    public static RequestSectJoinDialoguePacket decode(FriendlyByteBuf buf) {
        return new RequestSectJoinDialoguePacket(buf.readVarInt());
    }

    public static void handle(RequestSectJoinDialoguePacket msg, Supplier<NetworkEvent.Context> ctxSup) {
        NetworkEvent.Context ctx = ctxSup.get();
        ctx.enqueueWork(() -> {
            WanderingCultivatorMenu menu;
            WanderingCultivatorEntity target;
            ServerPlayer player = ctx.getSender();
            if (player == null) {
                return;
            }
            Entity entity = player.serverLevel().getEntity(msg.targetEntityId);
            if (!(entity instanceof WanderingCultivatorEntity) || !(target = (WanderingCultivatorEntity)entity).isAlive() || player.distanceToSqr((Entity)target) > 100.0) {
                player.displayClientMessage((Component)Component.translatable((String)"message.friday_cultivation.sect.dialogue.unavailable"), true);
                return;
            }
            AbstractContainerMenu patt1941$temp = player.containerMenu;
            if (patt1941$temp instanceof WanderingCultivatorMenu && (menu = (WanderingCultivatorMenu)patt1941$temp).getEntityId() == target.getId()) {
                player.closeContainer();
            }
            SectSavedData data = SectSavedData.get(player.serverLevel());
            if (target.getSectId().isBlank()) {
                player.displayClientMessage((Component)Component.translatable((String)"message.friday_cultivation.sect.dialogue.no_sect"), true);
                return;
            }
            CompoundTag snapshot = data.snapshot(player, target);
            ModNetwork.CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), (Object)new OpenSectJoinDialoguePacket(target.getId(), target.getSectName(), target.getCultivatorName().getString(), snapshot));
        });
        ctx.setPacketHandled(true);
    }
}

