package com.friday.cultivation.beast;

import com.friday.cultivation.realm.BeastRealm;
import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.common.util.INBTSerializable;

/**
 * 妖兽修炼数据 - 记录妖兽当前境界与累计灵气。
 * 完全照搬原 mod: xiaoxiang.cultivation.cultivation.beast.BeastCultivationData
 */
public class BeastCultivationData implements INBTSerializable<CompoundTag> {
    private BeastRealm realm = BeastRealm.MORTAL_BEAST;
    private long qiAccumulated = 0L;

    public BeastRealm getRealm() {
        return this.realm;
    }

    public void setRealm(BeastRealm realm) {
        this.realm = realm;
    }

    public long getQiAccumulated() {
        return this.qiAccumulated;
    }

    public void addQi(long amount) {
        if (amount <= 0L) return;
        this.qiAccumulated += amount;
    }

    public boolean canAdvance() {
        return this.qiAccumulated >= this.realm.advanceCost() && this.realm != BeastRealm.SPIRIT_SAINT;
    }

    public void advance() {
        if (this.canAdvance()) {
            this.realm = this.realm.next();
        }
    }

    @Override
    public CompoundTag serializeNBT() {
        CompoundTag tag = new CompoundTag();
        tag.putString("realm", this.realm.id());
        tag.putLong("qiAccumulated", this.qiAccumulated);
        return tag;
    }

    @Override
    public void deserializeNBT(CompoundTag tag) {
        if (tag.contains("realm", 8)) {
            this.realm = BeastRealm.byId(tag.getString("realm"));
        }
        this.qiAccumulated = tag.getLong("qiAccumulated");
    }
}
