package com.friday.cultivation.realm;

import net.minecraft.network.chat.Component;

/**
 * 妖兽境界（严格照搬原模组 com.xiaoxiang.cultivation.cultivation.realm.BeastRealm）
 */
public enum BeastRealm {
    MORTAL_BEAST("mortal_beast", 0L),
    SPIRIT_SOLDIER("spirit_soldier", 500L),
    SPIRIT_GENERAL("spirit_general", 5000L),
    SPIRIT_MARSHAL("spirit_marshal", 50000L),
    SPIRIT_KING("spirit_king", 500000L),
    SPIRIT_EMPEROR("spirit_emperor", 5000000L),
    SPIRIT_LORD("spirit_lord", 50000000L),
    SPIRIT_SAINT("spirit_saint", Long.MAX_VALUE);

    private final String id;
    private final long advanceCost;

    BeastRealm(String id, long advanceCost) {
        this.id = id;
        this.advanceCost = advanceCost;
    }

    public String id() { return this.id; }
    public long advanceCost() { return this.advanceCost; }

    public String translationKey() {
        return "beast_realm.friday_cultivation." + this.id;
    }

    public Component displayName() {
        return Component.translatable(this.translationKey());
    }

    public BeastRealm next() {
        int idx = this.ordinal();
        if (idx >= BeastRealm.values().length - 1) {
            return this;
        }
        return BeastRealm.values()[idx + 1];
    }

    public static BeastRealm byId(String id) {
        for (BeastRealm r : BeastRealm.values()) {
            if (!r.id.equals(id)) continue;
            return r;
        }
        return MORTAL_BEAST;
    }
}