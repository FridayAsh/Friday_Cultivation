/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.BlockPos
 *  net.minecraft.network.FriendlyByteBuf
 *  net.minecraftforge.api.distmarker.Dist
 *  net.minecraftforge.fml.DistExecutor
 *  net.minecraftforge.network.NetworkEvent$Context
 */
package com.friday.cultivation.network;

import com.friday.cultivation.block.formation.FormationCorePlateBlockEntity;
import com.friday.cultivation.client.ClientDomeRegistry;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

public class SyncDomePacket {
    private final BlockPos corePos;
    private final int radius;
    private final List<Sphere> spheres;

    public SyncDomePacket(BlockPos corePos, int radius) {
        this.corePos = corePos;
        this.radius = radius;
        this.spheres = radius > 0 ? List.of(new Sphere(corePos, radius)) : List.of();
    }

    public SyncDomePacket(BlockPos corePos, List<FormationCorePlateBlockEntity.FormationSphere> spheres) {
        this.corePos = corePos;
        this.radius = spheres.isEmpty() ? 0 : 1;
        ArrayList<Sphere> converted = new ArrayList<Sphere>();
        for (FormationCorePlateBlockEntity.FormationSphere sphere : spheres) {
            converted.add(new Sphere(sphere.center(), sphere.radius()));
        }
        this.spheres = List.copyOf(converted);
    }

    public static void encode(SyncDomePacket msg, FriendlyByteBuf buf) {
        buf.writeBlockPos(msg.corePos);
        buf.writeVarInt(msg.radius);
        buf.writeVarInt(msg.spheres.size());
        for (Sphere sphere : msg.spheres) {
            buf.writeBlockPos(sphere.center());
            buf.writeVarInt(sphere.radius());
        }
    }

    public static SyncDomePacket decode(FriendlyByteBuf buf) {
        BlockPos corePos = buf.readBlockPos();
        int radius = buf.readVarInt();
        int count = buf.readVarInt();
        ArrayList<Sphere> spheres = new ArrayList<Sphere>(count);
        for (int i = 0; i < count; ++i) {
            spheres.add(new Sphere(buf.readBlockPos(), buf.readVarInt()));
        }
        return new SyncDomePacket(corePos, radius, spheres);
    }

    private SyncDomePacket(BlockPos corePos, int radius, List<Sphere> spheres) {
        this.corePos = corePos;
        this.radius = radius;
        this.spheres = List.copyOf(spheres);
    }

    public static void handle(SyncDomePacket msg, Supplier<NetworkEvent.Context> ctxSup) {
        NetworkEvent.Context ctx = ctxSup.get();
        ctx.enqueueWork(() -> DistExecutor.unsafeRunWhenOn((Dist)Dist.CLIENT, () -> () -> {
            if (msg.radius < 0) {
                ClientDomeRegistry.clear();
            } else if (msg.radius == 0) {
                ClientDomeRegistry.remove(msg.corePos);
            } else {
                ClientDomeRegistry.put(msg.corePos, msg.spheres);
            }
        }));
        ctx.setPacketHandled(true);
    }

    public record Sphere(BlockPos center, int radius) {
        public Sphere(BlockPos center, int radius) {
            this.center = center = center.east();
            this.radius = radius;
        }
    }
}

