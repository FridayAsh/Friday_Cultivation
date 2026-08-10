/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.nbt.CompoundTag
 *  net.minecraft.network.FriendlyByteBuf
 *  net.minecraftforge.api.distmarker.Dist
 *  net.minecraftforge.fml.DistExecutor
 *  net.minecraftforge.network.NetworkEvent$Context
 */
package com.friday.cultivation.network;

import com.friday.cultivation.client.SectClientHooks;
import java.util.function.Supplier;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

public class OpenSectJoinDialoguePacket {
    private final int targetEntityId;
    private final String sectName;
    private final String npcName;
    private final CompoundTag snapshot;

    public OpenSectJoinDialoguePacket(int targetEntityId, String sectName, String npcName, CompoundTag snapshot) {
        this.targetEntityId = targetEntityId;
        this.sectName = sectName == null ? "" : sectName;
        this.npcName = npcName == null ? "" : npcName;
        this.snapshot = snapshot == null ? new CompoundTag() : snapshot.copy();
    }

    public static void encode(OpenSectJoinDialoguePacket msg, FriendlyByteBuf buf) {
        buf.writeVarInt(msg.targetEntityId);
        buf.writeUtf(msg.sectName, 128);
        buf.writeUtf(msg.npcName, 128);
        buf.writeNbt(msg.snapshot);
    }

    public static OpenSectJoinDialoguePacket decode(FriendlyByteBuf buf) {
        return new OpenSectJoinDialoguePacket(buf.readVarInt(), buf.readUtf(128), buf.readUtf(128), buf.readNbt());
    }

    public static void handle(OpenSectJoinDialoguePacket msg, Supplier<NetworkEvent.Context> ctxSup) {
        NetworkEvent.Context ctx = ctxSup.get();
        ctx.enqueueWork(() -> DistExecutor.unsafeRunWhenOn((Dist)Dist.CLIENT, () -> () -> SectClientHooks.openJoinDialogue(msg.targetEntityId, msg.sectName, msg.npcName, msg.snapshot)));
        ctx.setPacketHandled(true);
    }
}

