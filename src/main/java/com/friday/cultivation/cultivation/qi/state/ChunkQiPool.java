/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.BlockPos
 *  net.minecraft.nbt.CompoundTag
 *  net.minecraft.nbt.ListTag
 *  net.minecraft.nbt.Tag
 *  net.minecraft.util.RandomSource
 *  net.minecraftforge.common.util.INBTSerializable
 */
package com.friday.cultivation.cultivation.qi.state;

import com.friday.cultivation.cultivation.qi.BlockDegradeRule;
import com.friday.cultivation.cultivation.qi.BlockQiSpec;
import com.friday.cultivation.cultivation.qi.state.BlockQiState;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.util.RandomSource;
import net.minecraftforge.common.util.INBTSerializable;

public final class ChunkQiPool
implements INBTSerializable<CompoundTag> {
    private final Map<Long, BlockQiState> entries = new HashMap<Long, BlockQiState>();

    public DrainResult tryDrain(BlockPos pos, BlockQiSpec spec, int amount, long now, RandomSource random) {
        long key = pos.asLong();
        BlockQiState st = this.entries.get(key);
        if (st == null) {
            st = new BlockQiState(spec.baseMaxQi(), 0, now);
        } else {
            this.applyRegen(st, spec, now);
        }
        int drained = Math.min(amount, st.currentQi);
        if (drained <= 0) {
            st.lastTouchTime = now;
            this.entries.put(key, st);
            return new DrainResult(0, false);
        }
        st.currentQi -= drained;
        st.totalDrained += drained;
        st.lastTouchTime = now;
        boolean shouldDegrade = false;
        BlockDegradeRule rule = spec.degradeRule();
        if (rule != null && st.totalDrained >= rule.drainThreshold()) {
            if (random.nextDouble() < rule.chancePerCheck()) {
                shouldDegrade = true;
                this.entries.remove(key);
                return new DrainResult(drained, true);
            }
            st.totalDrained = 0;
        }
        if (st.currentQi >= spec.baseMaxQi()) {
            this.entries.remove(key);
        } else {
            this.entries.put(key, st);
        }
        return new DrainResult(drained, false);
    }

    public int peek(BlockPos pos, BlockQiSpec spec, long now) {
        long key = pos.asLong();
        BlockQiState st = this.entries.get(key);
        if (st == null) {
            return spec.baseMaxQi();
        }
        this.applyRegen(st, spec, now);
        if (st.currentQi >= spec.baseMaxQi()) {
            this.entries.remove(key);
            return spec.baseMaxQi();
        }
        return st.currentQi;
    }

    private void applyRegen(BlockQiState st, BlockQiSpec spec, long now) {
        int newQi;
        long elapsedTicks = now - st.lastTouchTime;
        if (elapsedTicks <= 0L) {
            return;
        }
        double regenAmount = spec.baseRegenPerSec() * (double)elapsedTicks / 20.0;
        if (regenAmount <= 0.0) {
            return;
        }
        st.currentQi = newQi = (int)Math.min((double)spec.baseMaxQi(), (double)st.currentQi + regenAmount);
        st.lastTouchTime = now;
    }

    public int trackedCount() {
        return this.entries.size();
    }

    public void clear() {
        this.entries.clear();
    }

    public void removeEntry(BlockPos pos) {
        this.entries.remove(pos.asLong());
    }

    public CompoundTag serializeNBT() {
        CompoundTag tag = new CompoundTag();
        ListTag list = new ListTag();
        for (Map.Entry<Long, BlockQiState> e : this.entries.entrySet()) {
            BlockQiState st = e.getValue();
            CompoundTag entry = new CompoundTag();
            entry.putLong("pos", e.getKey().longValue());
            entry.putInt("qi", st.currentQi);
            entry.putInt("drained", st.totalDrained);
            entry.putLong("time", st.lastTouchTime);
            list.add(entry);
        }
        tag.put("entries", (Tag)list);
        return tag;
    }

    public void deserializeNBT(CompoundTag nbt) {
        this.entries.clear();
        if (!nbt.contains("entries", 9)) {
            return;
        }
        ListTag list = nbt.getList("entries", 10);
        for (Tag t : list) {
            CompoundTag entry = (CompoundTag)t;
            BlockQiState st = new BlockQiState(entry.getInt("qi"), entry.getInt("drained"), entry.getLong("time"));
            this.entries.put(entry.getLong("pos"), st);
        }
    }

    public Iterator<Map.Entry<Long, BlockQiState>> iterator() {
        return this.entries.entrySet().iterator();
    }

    public record DrainResult(int drained, boolean shouldDegrade) {
        public static final DrainResult ZERO = new DrainResult(0, false);
    }
}

