/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.nbt.CompoundTag
 *  net.minecraft.network.FriendlyByteBuf
 *  net.minecraft.world.entity.player.Player
 *  net.minecraftforge.network.NetworkEvent$Context
 */
package com.friday.cultivation.network;

import com.friday.cultivation.client.ClientCultivationHooks;
import com.friday.cultivation.cultivation.CultivationCapability;
import com.friday.cultivation.cultivation.CultivationData;
import java.util.function.Supplier;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.network.NetworkEvent;

public class SyncCultivationDataPacket {
    private final CompoundTag data;

    public SyncCultivationDataPacket(CompoundTag data) {
        this.data = data;
    }

    public SyncCultivationDataPacket(CultivationData data) {
        this.data = data.serializeNBT();
    }

    public static void encode(SyncCultivationDataPacket msg, FriendlyByteBuf buf) {
        buf.writeNbt(msg.data);
    }

    public static SyncCultivationDataPacket decode(FriendlyByteBuf buf) {
        return new SyncCultivationDataPacket(buf.readNbt());
    }

    public static void handle(SyncCultivationDataPacket msg, Supplier<NetworkEvent.Context> ctxSup) {
        NetworkEvent.Context ctx = ctxSup.get();
        ctx.enqueueWork(() -> ClientCultivationHooks.applySync(msg.data));
        ctx.setPacketHandled(true);
    }

    public CompoundTag getData() {
        return this.data;
    }

    public static void applyToClientPlayer(Player player, CompoundTag data) {
        CultivationCapability.get(player).ifPresent(d -> d.deserializeNBT(data));
    }
}

