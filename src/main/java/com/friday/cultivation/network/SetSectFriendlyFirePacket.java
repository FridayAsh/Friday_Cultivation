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

public class SetSectFriendlyFirePacket {
    private final boolean sameSectImmunity;
    private final int targetEntityId;

    public SetSectFriendlyFirePacket(boolean sameSectImmunity) {
        this(sameSectImmunity, -1);
    }

    public SetSectFriendlyFirePacket(boolean sameSectImmunity, int targetEntityId) {
        this.sameSectImmunity = sameSectImmunity;
        this.targetEntityId = targetEntityId;
    }

    public static void encode(SetSectFriendlyFirePacket msg, FriendlyByteBuf buf) {
        buf.writeBoolean(msg.sameSectImmunity);
        buf.writeVarInt(msg.targetEntityId);
    }

    public static SetSectFriendlyFirePacket decode(FriendlyByteBuf buf) {
        return new SetSectFriendlyFirePacket(buf.readBoolean(), buf.readVarInt());
    }

    public static void handle(SetSectFriendlyFirePacket msg, Supplier<NetworkEvent.Context> ctxSup) {
        NetworkEvent.Context ctx = ctxSup.get();
        ctx.enqueueWork(() -> {
            boolean sameSectScreen;
            ServerPlayer player = ctx.getSender();
            if (player == null) {
                return;
            }
            SectSavedData data = SectSavedData.get(player.serverLevel());
            WanderingCultivatorEntity target = SetSectFriendlyFirePacket.resolveTarget(player, msg.targetEntityId);
            String playerSectId = data.sectIdOf((Entity)player);
            boolean bl = sameSectScreen = target == null || playerSectId != null && !playerSectId.isBlank() && playerSectId.equals(target.getSectId());
            if (sameSectScreen && data.isRegisteredMember(player)) {
                data.setSameSectImmunity(player, msg.sameSectImmunity);
                player.displayClientMessage((Component)Component.translatable((String)(msg.sameSectImmunity ? "message.friday_cultivation.sect.same_immunity_on" : "message.friday_cultivation.sect.same_immunity_off")), true);
            }
            ModNetwork.CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), (Object)new OpenSectScreenPacket(data.snapshot(player, target)));
        });
        ctx.setPacketHandled(true);
    }

    private static WanderingCultivatorEntity resolveTarget(ServerPlayer player, int targetEntityId) {
        WanderingCultivatorEntity npc;
        if (targetEntityId < 0) {
            return null;
        }
        Entity entity = player.serverLevel().getEntity(targetEntityId);
        if (entity instanceof WanderingCultivatorEntity && (npc = (WanderingCultivatorEntity)entity).isAlive() && player.distanceToSqr((Entity)npc) <= 100.0) {
            return npc;
        }
        return null;
    }
}

