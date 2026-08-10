package com.friday.cultivation.network;

import com.friday.cultivation.CultivationCapability;
import com.friday.cultivation.capability.CultivationData;
import com.friday.cultivation.event.SoulStateHandler;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/** 死亡选择包 — 玩家选择普通死亡/前往地府/游魂 */
public class DeathChoicePacket {
    public enum Choice { VANILLA_DEATH, GO_DIFU, WANDERING_SOUL }

    private final Choice choice;

    public DeathChoicePacket(Choice choice) { this.choice = choice; }

    public static void encode(DeathChoicePacket msg, FriendlyByteBuf buf) { buf.writeEnum(msg.choice); }
    public static DeathChoicePacket decode(FriendlyByteBuf buf) { return new DeathChoicePacket(buf.readEnum(Choice.class)); }

    public static void handle(DeathChoicePacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player == null) return;
            SoulStateHandler.resolveDeathChoice(player, msg.choice);
        });
        ctx.get().setPacketHandled(true);
    }
}
