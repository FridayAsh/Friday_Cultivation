/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.BlockPos
 *  net.minecraft.core.Vec3i
 *  net.minecraft.resources.ResourceKey
 *  net.minecraft.server.level.ServerLevel
 *  net.minecraft.world.level.ChunkPos
 *  net.minecraft.world.level.Level
 */
package com.friday.cultivation.cultivation.qi.field;

import com.friday.cultivation.cultivation.qi.BlockQiSpec;
import com.friday.cultivation.cultivation.qi.field.IQiFieldEffect;
import com.friday.cultivation.cultivation.qi.field.QiModifier;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;

public final class QiFieldRegistry {
    private static final Map<ResourceKey<Level>, QiFieldRegistry> INSTANCES = new ConcurrentHashMap<ResourceKey<Level>, QiFieldRegistry>();
    private final Map<Long, Set<IQiFieldEffect>> fieldsByChunk = new ConcurrentHashMap<Long, Set<IQiFieldEffect>>();

    public static QiFieldRegistry of(ServerLevel level) {
        return INSTANCES.computeIfAbsent((ResourceKey<Level>)level.dimension(), k -> new QiFieldRegistry());
    }

    /** Removes the registry for an unloading server level. */
    public static void clear(ServerLevel level) {
        if (level != null) {
            INSTANCES.remove(level.dimension());
        }
    }

    /** Clears every level registry when the integrated/dedicated server stops. */
    public static void clearAll() {
        INSTANCES.clear();
    }

    private QiFieldRegistry() {
    }

    public void register(IQiFieldEffect field) {
        for (long chunkKey : this.overlappingChunks(field)) {
            this.fieldsByChunk.computeIfAbsent(chunkKey, k -> Collections.newSetFromMap(new ConcurrentHashMap())).add(field);
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
        long chunkKey = ChunkPos.asLong((int)(pos.getX() >> 4), (int)(pos.getZ() >> 4));
        Set<IQiFieldEffect> candidates = this.fieldsByChunk.get(chunkKey);
        if (candidates == null || candidates.isEmpty()) {
            return Collections.emptyList();
        }
        ArrayList<IQiFieldEffect> hits = new ArrayList<IQiFieldEffect>(2);
        for (IQiFieldEffect f : candidates) {
            if (!f.isActive()) continue;
            double rsq = (double)f.radius() * (double)f.radius();
            if (pos.distSqr((Vec3i)f.origin()) > rsq) continue;
            hits.add(f);
        }
        return hits;
    }

    public QiModifier composedModifierAt(BlockPos pos, BlockQiSpec spec) {
        List<IQiFieldEffect> fields = this.activeFieldsAt(pos);
        if (fields.isEmpty()) {
            return QiModifier.IDENTITY;
        }
        QiModifier result = QiModifier.IDENTITY;
        for (IQiFieldEffect f : fields) {
            result = result.compose(f.modifyAt(pos, spec));
        }
        return result;
    }

    private long[] overlappingChunks(IQiFieldEffect field) {
        BlockPos o = field.origin();
        int r = field.radius();
        int minCX = o.getX() - r >> 4;
        int maxCX = o.getX() + r >> 4;
        int minCZ = o.getZ() - r >> 4;
        int maxCZ = o.getZ() + r >> 4;
        int n = (maxCX - minCX + 1) * (maxCZ - minCZ + 1);
        long[] keys = new long[n];
        int i = 0;
        for (int cx = minCX; cx <= maxCX; ++cx) {
            for (int cz = minCZ; cz <= maxCZ; ++cz) {
                keys[i++] = ChunkPos.asLong((int)cx, (int)cz);
            }
        }
        return keys;
    }

    public int totalRegisteredCount() {
        HashSet<IQiFieldEffect> all = new HashSet<IQiFieldEffect>();
        for (Set<IQiFieldEffect> s : this.fieldsByChunk.values()) {
            all.addAll(s);
        }
        return all.size();
    }
}
