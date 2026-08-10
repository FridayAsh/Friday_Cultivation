package com.friday.cultivation.qi.field;

import com.friday.cultivation.qi.BlockQiSpec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 灵气场注册表 - 按维度分实例，按区块索引活跃灵气场。
 * 完全照搬原 mod: xiaoxiang.cultivation.cultivation.qi.field.QiFieldRegistry
 */
public final class QiFieldRegistry {
    private static final Map<ResourceKey<Level>, QiFieldRegistry> INSTANCES = new ConcurrentHashMap<>();
    private final Map<Long, Set<IQiFieldEffect>> fieldsByChunk = new ConcurrentHashMap<>();

    public static QiFieldRegistry of(ServerLevel level) {
        return INSTANCES.computeIfAbsent(level.dimension(), k -> new QiFieldRegistry());
    }

    private QiFieldRegistry() {
    }

    public void register(IQiFieldEffect field) {
        for (long chunkKey : this.overlappingChunks(field)) {
            this.fieldsByChunk.computeIfAbsent(chunkKey, k -> Collections.newSetFromMap(new ConcurrentHashMap<>())).add(field);
        }
    }

    public void unregister(IQiFieldEffect field) {
        for (long chunkKey : this.overlappingChunks(field)) {
            Set<IQiFieldEffect> set = this.fieldsByChunk.get(chunkKey);
            if (set == null) continue;
            set.remove(field);
            if (!set.isEmpty()) continue;
            this.fieldsByChunk.remove(chunkKey);
        }
    }

    public List<IQiFieldEffect> activeFieldsAt(BlockPos pos) {
        long chunkKey = ChunkPos.asLong(pos.getX() >> 4, pos.getZ() >> 4);
        Set<IQiFieldEffect> candidates = this.fieldsByChunk.get(chunkKey);
        if (candidates == null || candidates.isEmpty()) return Collections.emptyList();
        ArrayList<IQiFieldEffect> hits = new ArrayList<>(2);
        for (IQiFieldEffect f : candidates) {
            if (!f.isActive()) continue;
            double rsq = (double) f.radius() * (double) f.radius();
            if (pos.distSqr((Vec3i) f.origin()) > rsq) continue;
            hits.add(f);
        }
        return hits;
    }

    public QiModifier composedModifierAt(BlockPos pos, BlockQiSpec spec) {
        List<IQiFieldEffect> fields = this.activeFieldsAt(pos);
        if (fields.isEmpty()) return QiModifier.IDENTITY;
        QiModifier result = QiModifier.IDENTITY;
        for (IQiFieldEffect f : fields) result = result.compose(f.modifyAt(pos, spec));
        return result;
    }

    private long[] overlappingChunks(IQiFieldEffect field) {
        BlockPos o = field.origin();
        int r = field.radius();
        int minCX = (o.getX() - r) >> 4;
        int maxCX = (o.getX() + r) >> 4;
        int minCZ = (o.getZ() - r) >> 4;
        int maxCZ = (o.getZ() + r) >> 4;
        int n = (maxCX - minCX + 1) * (maxCZ - minCZ + 1);
        long[] keys = new long[n];
        int i = 0;
        for (int cx = minCX; cx <= maxCX; ++cx) {
            for (int cz = minCZ; cz <= maxCZ; ++cz) {
                keys[i++] = ChunkPos.asLong(cx, cz);
            }
        }
        return keys;
    }

    public int totalRegisteredCount() {
        HashSet<IQiFieldEffect> all = new HashSet<>();
        for (Set<IQiFieldEffect> s : this.fieldsByChunk.values()) all.addAll(s);
        return all.size();
    }
}
