package com.friday.cultivation.worldgen;

import com.friday.cultivation.entity.npc.WanderingCultivatorEntity;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;

/**
 * 全存档大帝统计：
 * - 当前存活的大帝 NPC 数量（上限 10，死亡后释放名额）；
 * - 曾亲手击杀过大帝的玩家 UUID 集合（突破大帝的前置条件之一）。
 */
public class GreatEmperorTracker
extends SavedData {
    private static final String DATA_NAME = "friday_cultivation_great_emperors";
    public static final int MAX_EMPERORS = 10;
    private int emperorCount = 0;
    private final Set<UUID> emperorSlayers = new HashSet<UUID>();

    public static GreatEmperorTracker get(ServerLevel level) {
        return (GreatEmperorTracker)level.getDataStorage().computeIfAbsent(GreatEmperorTracker::load, GreatEmperorTracker::new, DATA_NAME);
    }

    public static GreatEmperorTracker load(CompoundTag tag) {
        GreatEmperorTracker data = new GreatEmperorTracker();
        data.emperorCount = tag.getInt("emperorCount");
        ListTag slayers = tag.getList("emperorSlayers", 8);
        for (int i = 0; i < slayers.size(); ++i) {
            try {
                data.emperorSlayers.add(UUID.fromString(slayers.getString(i)));
            }
            catch (IllegalArgumentException illegalArgumentException) {
                // 忽略损坏条目
            }
        }
        return data;
    }

    public CompoundTag save(CompoundTag tag) {
        tag.putInt("emperorCount", this.emperorCount);
        ListTag slayers = new ListTag();
        for (UUID id : this.emperorSlayers) {
            slayers.add((Tag)StringTag.valueOf(id.toString()));
        }
        tag.put("emperorSlayers", (Tag)slayers);
        return tag;
    }

    /** 当前存活大帝数量 */
    public int emperorCount() {
        return this.emperorCount;
    }

    /** 是否还有名额生成新大帝 */
    public boolean canSpawnEmperor() {
        return this.emperorCount < MAX_EMPERORS;
    }

    /** 新大帝生成时占用一个名额（满则拒绝） */
    public boolean claimEmperor() {
        if (!this.canSpawnEmperor()) {
            return false;
        }
        ++this.emperorCount;
        this.setDirty();
        return true;
    }

    /** 大帝死亡/消失时释放名额 */
    public void releaseEmperor() {
        if (this.emperorCount > 0) {
            --this.emperorCount;
            this.setDirty();
        }
    }

    /** 记录玩家亲手击杀大帝 */
    public void recordEmperorSlayer(UUID playerId) {
        if (playerId == null) {
            return;
        }
        this.emperorSlayers.add(playerId);
        this.setDirty();
    }

    /** 玩家是否曾亲手击杀过大帝 */
    public boolean hasSlainEmperor(UUID playerId) {
        return playerId != null && this.emperorSlayers.contains(playerId);
    }

    /** 便于日志/调试 */
    public int slayerCount() {
        return this.emperorSlayers.size();
    }
}
