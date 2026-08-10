package com.friday.cultivation.network;

import com.friday.cultivation.ItemTier;
import com.friday.cultivation.block.formation.FormationCorePlateBlockEntity;
import com.friday.cultivation.client.screen.FormationScreen;
import com.friday.cultivation.qi.formation.FormationType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

/**
 * 同步阵法旗面列表 — 严格 1:1 复刻原 mod com.xiaoxiang.cultivation.network.SyncFormationFlagsPacket。
 * 服务端把当前阵法的所有旗面数据同步到客户端，客户端转交给打开的 FormationScreen。
 */
public class SyncFormationFlagsPacket {
    private final BlockPos corePos;
    private final List<Entry> entries;

    public SyncFormationFlagsPacket(BlockPos corePos, List<Entry> entries) {
        this.corePos = corePos.immutable();
        this.entries = List.copyOf(entries);
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
        ctx.enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {
            Screen patt2837$temp = Minecraft.getInstance().screen;
            if (patt2837$temp instanceof FormationScreen) {
                FormationScreen screen = (FormationScreen) patt2837$temp;
                screen.setFlagEntries(msg.corePos, msg.entries);
            }
        }));
        ctx.setPacketHandled(true);
    }

    public record Entry(BlockPos pos, int typeOrdinal, int tierOrdinal, int radius, boolean directLinked, boolean runeLinked, boolean manualLinked) {
        public Entry(BlockPos pos, int typeOrdinal, int tierOrdinal, int radius, boolean directLinked, boolean runeLinked, boolean manualLinked) {
            this.pos = pos.immutable();
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
