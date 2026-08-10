package com.friday.cultivation.realm;

import net.minecraft.network.chat.Component;

/**
 * 境界子阶段：初期/中期/后期/圆满
 */
public enum SubStage {
    EARLY("early", "初期"),
    MIDDLE("middle", "中期"),
    LATE("late", "后期"),
    PEAK("peak", "圆满");

    private final String id;
    private final String chineseName;

    SubStage(String id, String chineseName) {
        this.id = id;
        this.chineseName = chineseName;
    }

    public String id() { return id; }

    public String translationKey() {
        return "substage.friday_cultivation." + id;
    }

    public Component displayName() {
        return Component.translatableWithFallback(translationKey(), chineseName);
    }

    public boolean isPeak() {
        return this == PEAK;
    }

    public SubStage next() {
        int idx = this.ordinal() + 1;
        if (idx >= values().length) return PEAK;
        return values()[idx];
    }

    public SubStage prev() {
        int idx = this.ordinal() - 1;
        if (idx < 0) return EARLY;
        return values()[idx];
    }

    public static SubStage byId(String id) {
        for (SubStage s : values()) {
            if (s.id.equals(id)) return s;
        }
        return EARLY;
    }
}
