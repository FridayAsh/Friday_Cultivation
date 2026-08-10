package com.friday.cultivation.client;

import com.friday.cultivation.network.SyncDomePacket;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.core.BlockPos;
import org.jetbrains.annotations.Nullable;

/**
 * 客户端穹顶注册表 - 完全照搬原模组 com.xiaoxiang.cultivation.client.ClientDomeRegistry。
 * 以 corePos.asLong() 为键存储 DomeSphere 列表，提供 domeContaining/isNearShell 查询。
 */
public final class ClientDomeRegistry {
    private static final Map<Long, List<DomeSphere>> DOMES = new ConcurrentHashMap<>();

    private ClientDomeRegistry() {
    }

    public static void put(BlockPos corePos, int radius) {
        DOMES.put(corePos.asLong(), List.of(new DomeSphere(corePos, radius)));
    }

    public static void put(BlockPos corePos, List<SyncDomePacket.Sphere> spheres) {
        if (spheres == null || spheres.isEmpty()) {
            ClientDomeRegistry.remove(corePos);
            return;
        }
        DOMES.put(corePos.asLong(), spheres.stream().map(sphere -> new DomeSphere(sphere.center(), sphere.radius())).toList());
    }

    public static void remove(BlockPos corePos) {
        DOMES.remove(corePos.asLong());
    }

    public static void clear() {
        DOMES.clear();
    }

    @Nullable
    public static BlockPos domeContaining(double x, double y, double z) {
        for (Map.Entry<Long, List<DomeSphere>> entry : DOMES.entrySet()) {
            for (DomeSphere sphere : entry.getValue()) {
                if (!sphere.contains(x, y, z)) continue;
                return BlockPos.of(entry.getKey());
            }
        }
        return null;
    }

    public static boolean isNearShell(BlockPos pos, double toleranceBlocks) {
        double x = (double) pos.getX() + 0.5;
        double y = (double) pos.getY() + 0.5;
        double z = (double) pos.getZ() + 0.5;
        for (List<DomeSphere> spheres : DOMES.values()) {
            for (DomeSphere sphere : spheres) {
                double dist = sphere.distanceToCenter(x, y, z);
                if (!(Math.abs(dist - (double) sphere.radius()) <= toleranceBlocks)) continue;
                return true;
            }
        }
        return false;
    }

    private record DomeSphere(BlockPos center, int radius) {
        private DomeSphere(BlockPos center, int radius) {
            this.center = center.immutable();
            this.radius = radius;
        }

        private boolean contains(double x, double y, double z) {
            double dz;
            double dy;
            double dx = x - ((double) this.center.getX() + 0.5);
            return dx * dx + (dy = y - ((double) this.center.getY() + 0.5)) * dy + (dz = z - ((double) this.center.getZ() + 0.5)) * dz <= (double) this.radius * (double) this.radius;
        }

        private double distanceToCenter(double x, double y, double z) {
            double dx = x - ((double) this.center.getX() + 0.5);
            double dy = y - ((double) this.center.getY() + 0.5);
            double dz = z - ((double) this.center.getZ() + 0.5);
            return Math.sqrt(dx * dx + dy * dy + dz * dz);
        }
    }
}
