/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.network.chat.Component
 */
package com.friday.cultivation.cultivation.sect;

import net.minecraft.network.chat.Component;

public enum SectRole {
    ANCESTOR("ancestor", 0),
    MASTER("master", 1),
    ELDER("elder", 2),
    INNER_DISCIPLE("inner_disciple", 3),
    OUTER_DISCIPLE("outer_disciple", 4),
    GUARD_DISCIPLE("guard_disciple", 5),
    SERVANT("servant", 6),
    NONE("none", 99);

    private final String id;
    private final int rank;

    private SectRole(String id, int rank) {
        this.id = id;
        this.rank = rank;
    }

    public String id() {
        return this.id;
    }

    public int rank() {
        return this.rank;
    }

    public String translationKey() {
        return "sect.friday_cultivation.role." + this.id;
    }

    public Component displayName() {
        return Component.translatable((String)this.translationKey());
    }

    public Component identity(String sectName) {
        if (this == NONE || sectName == null || sectName.isBlank()) {
            return Component.translatable((String)"sect.friday_cultivation.none");
        }
        return Component.translatable((String)"sect.friday_cultivation.identity", (Object[])new Object[]{sectName, this.displayName()});
    }

    public boolean sameOrHigherThan(SectRole other) {
        if (other == null) {
            return false;
        }
        return this.rank <= other.rank;
    }

    public static SectRole byId(String id) {
        if (id == null || id.isBlank()) {
            return NONE;
        }
        for (SectRole role : SectRole.values()) {
            if (!role.id.equals(id)) continue;
            return role;
        }
        return NONE;
    }
}

