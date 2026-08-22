/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.gui.screens.Screen
 *  net.minecraft.core.BlockPos
 *  net.minecraft.network.FriendlyByteBuf
 *  net.minecraftforge.api.distmarker.Dist
 *  net.minecraftforge.fml.DistExecutor
 *  net.minecraftforge.network.NetworkEvent$Context
 */
package com.friday.cultivation.network;

import com.friday.cultivation.block.formation.FormationCorePlateBlockEntity;
import com.friday.cultivation.client.ClientFormationSyncHooks;
import com.friday.cultivation.cultivation.ItemTier;
import com.friday.cultivation.cultivation.qi.formation.FormationType;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

public class SyncFormationFlagsPacket {
    private final BlockPos corePos;
    private final List<Entry> entries;

    public SyncFormationFlagsPacket(BlockPos corePos, List<Entry> entries) {
        this.corePos = corePos.east();
        this.entries = List.copyOf(entries);
    }

    public BlockPos corePos() {
        return this.corePos;
    }

    public List<Entry> entries() {
        return this.entries;
    }

    public static SyncFormationFlagsPacket fromViews(BlockPos corePos, List<FormationCorePlateBlockEntity.FlagLinkView> views) {
        ArrayList<Entry> entries = new ArrayList<Entry>();
        for (FormationCorePlateBlockEntity.FlagLinkView view : views) {
            entries.add(new Entry(view.pos(), view.type().ordinal(), view.tier().ordinal(), view.radius(), view.directLinked(), view.runeLinked(), view.manualLinked()));
        }
        return new SyncFormationFlagsPacket(corePos, entries);
    }

    public static void encode(SyncFormationFlagsPacket msg, FriendlyByteBuf buf) {
        buf.writeBlockPos(msg.corePos);
        buf.writeVarInt(msg.entries.size());
        for (Entry entry : msg.entries) {
            buf.writeBlockPos(entry.pos());
            buf.writeVarInt(entry.typeOrdinal());
            buf.writeVarInt(entry.tierOrdinal());
            buf.writeVarInt(entry.radius());
            buf.writeBoolean(entry.directLinked());
            buf.writeBoolean(entry.runeLinked());
            buf.writeBoolean(entry.manualLinked());
        }
    }

    public static SyncFormationFlagsPacket decode(FriendlyByteBuf buf) {
        BlockPos corePos = buf.readBlockPos();
        int size = buf.readVarInt();
        ArrayList<Entry> entries = new ArrayList<Entry>(size);
        for (int i = 0; i < size; ++i) {
            entries.add(new Entry(buf.readBlockPos(), buf.readVarInt(), buf.readVarInt(), buf.readVarInt(), buf.readBoolean(), buf.readBoolean(), buf.readBoolean()));
        }
        return new SyncFormationFlagsPacket(corePos, entries);
    }

    public static void handle(SyncFormationFlagsPacket msg, Supplier<NetworkEvent.Context> ctxSup) {
        NetworkEvent.Context ctx = ctxSup.get();
        ctx.enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT,
                () -> () -> ClientFormationSyncHooks.applyFlags(msg)));
        ctx.setPacketHandled(true);
    }

    public record Entry(BlockPos pos, int typeOrdinal, int tierOrdinal, int radius, boolean directLinked, boolean runeLinked, boolean manualLinked) {
        public Entry(BlockPos pos, int typeOrdinal, int tierOrdinal, int radius, boolean directLinked, boolean runeLinked, boolean manualLinked) {
            this.pos = pos = pos.east();
            this.typeOrdinal = typeOrdinal;
            this.tierOrdinal = tierOrdinal;
            this.radius = radius;
            this.directLinked = directLinked;
            this.runeLinked = runeLinked;
            this.manualLinked = manualLinked;
        }

        public FormationType type() {
            FormationType[] values = FormationType.values();
            return this.typeOrdinal >= 0 && this.typeOrdinal < values.length ? values[this.typeOrdinal] : FormationType.QI_GATHERING;
        }

        public ItemTier tier() {
            ItemTier[] values = ItemTier.values();
            return this.tierOrdinal >= 0 && this.tierOrdinal < values.length ? values[this.tierOrdinal] : ItemTier.LOW;
        }
    }
}
