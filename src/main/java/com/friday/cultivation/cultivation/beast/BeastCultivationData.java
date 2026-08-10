/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.nbt.CompoundTag
 *  net.minecraftforge.common.util.INBTSerializable
 */
package com.friday.cultivation.cultivation.beast;

import com.friday.cultivation.cultivation.realm.BeastRealm;
import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.common.util.INBTSerializable;

public class BeastCultivationData
implements INBTSerializable<CompoundTag> {
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
        if (amount <= 0L) {
            return;
        }
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

    public CompoundTag serializeNBT() {
        CompoundTag tag = new CompoundTag();
        tag.putString("realm", this.realm.id());
        tag.putLong("qiAccumulated", this.qiAccumulated);
        return tag;
    }

    public void deserializeNBT(CompoundTag tag) {
        if (tag.contains("realm", 8)) {
            this.realm = BeastRealm.byId(tag.getString("realm"));
        }
        this.qiAccumulated = tag.getLong("qiAccumulated");
    }
}

