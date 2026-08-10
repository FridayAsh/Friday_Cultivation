package com.friday.cultivation.network;

import com.friday.cultivation.client.DivineSenseClientEffects;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

/**
 * 神识扫描包 — 完整复刻原模组 DivineSenseScanPacket。
 * <p>
 * 服务端 → 客户端：携带扫描中心、半径、扩散/发光 ticks、扫描到的 entityIds + blockPositions。
 * 客户端通过 {@link DivineSenseClientEffects#onScan} 渲染扩散波 + 高亮实体/方块。
 * </p>
 */
public class DivineSenseScanPacket {
    private final double x;
    private final double y;
    private final double z;
    private final double radius;
    private final int expansionTicks;
    private final int glowTicks;
    private final List<Integer> entityIds;
    private final List<BlockPos> blockPositions;

    public DivineSenseScanPacket(double x, double y, double z, double radius, int expansionTicks, int glowTicks, List<Integer> entityIds, List<BlockPos> blockPositions) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.radius = radius;
        this.expansionTicks = expansionTicks;
        this.glowTicks = glowTicks;
        this.entityIds = List.copyOf(entityIds);
        this.blockPositions = List.copyOf(blockPositions);
    }

    public static void encode(DivineSenseScanPacket msg, FriendlyByteBuf buf) {
        buf.writeDouble(msg.x);
        buf.writeDouble(msg.y);
        buf.writeDouble(msg.z);
        buf.writeDouble(msg.radius);
        buf.writeVarInt(msg.expansionTicks);
        buf.writeVarInt(msg.glowTicks);
        buf.writeVarInt(msg.entityIds.size());
        for (int entityId : msg.entityIds) {
            buf.writeVarInt(entityId);
        }
        buf.writeVarInt(msg.blockPositions.size());
        for (BlockPos pos : msg.blockPositions) {
            buf.writeBlockPos(pos);
        }
    }

    public static DivineSenseScanPacket decode(FriendlyByteBuf buf) {
        double x = buf.readDouble();
        double y = buf.readDouble();
        double z = buf.readDouble();
        double radius = buf.readDouble();
        int expansionTicks = buf.readVarInt();
        int glowTicks = buf.readVarInt();
        int entityCount = buf.readVarInt();
        ArrayList<Integer> entityIds = new ArrayList<Integer>(entityCount);
        for (int i = 0; i < entityCount; ++i) {
            entityIds.add(buf.readVarInt());
        }
        int blockCount = buf.readVarInt();
        ArrayList<BlockPos> blockPositions = new ArrayList<BlockPos>(blockCount);
        for (int i = 0; i < blockCount; ++i) {
            blockPositions.add(buf.readBlockPos());
        }
        return new DivineSenseScanPacket(x, y, z, radius, expansionTicks, glowTicks, entityIds, blockPositions);
    }

    public static void handle(DivineSenseScanPacket msg, Supplier<NetworkEvent.Context> ctxSup) {
        NetworkEvent.Context ctx = ctxSup.get();
        ctx.enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> DivineSenseClientEffects.onScan(msg.x, msg.y, msg.z, msg.radius, msg.expansionTicks, msg.glowTicks, msg.entityIds, msg.blockPositions)));
        ctx.setPacketHandled(true);
    }
}
