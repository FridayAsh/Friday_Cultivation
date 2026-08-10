/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.network.chat.Component
 */
package com.friday.cultivation.cultivation.realm;

import net.minecraft.network.chat.Component;

public enum SubStage {
    EARLY("early"),
    MIDDLE("middle"),
    LATE("late"),
    PEAK("peak");

    private final String id;

    private SubStage(String id) {
        this.id = id;
    }

    public String id() {
        return this.id;
    }

    public String translationKey() {
        return "sub_stage.friday_cultivation." + this.id;
    }

    public Component displayName() {
        return Component.translatable((String)this.translationKey());
    }

    public boolean isPeak() {
        return this == PEAK;
    }

    public SubStage next() {
        return this.ordinal() < SubStage.values().length - 1 ? SubStage.values()[this.ordinal() + 1] : this;
    }

    public SubStage prev() {
        return this.ordinal() > 0 ? SubStage.values()[this.ordinal() - 1] : this;
    }

    public static SubStage byId(String id) {
        for (SubStage s : SubStage.values()) {
            if (!s.id.equals(id)) continue;
            return s;
        }
        return EARLY;
    }
}

