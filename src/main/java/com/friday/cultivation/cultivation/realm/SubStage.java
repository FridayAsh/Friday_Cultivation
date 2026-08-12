/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.network.chat.Component
 */
package com.friday.cultivation.cultivation.realm;

import net.minecraft.network.chat.Component;

/**
 * 不可变子阶段值类。
 * - 4 档境界（筑基及以上）使用预定义常量 EARLY(0)/MIDDLE(1)/LATE(2)/PEAK(3)
 * - 锻体境界使用 level 1..10（由 Realm.subStageAt 创建）
 * - 练气境界使用 level 1..9（由 Realm.subStageAt 创建）
 */
public final class SubStage {
    public static final SubStage EARLY = new SubStage("early", 0);
    public static final SubStage MIDDLE = new SubStage("middle", 1);
    public static final SubStage LATE = new SubStage("late", 2);
    public static final SubStage PEAK = new SubStage("peak", 3);

    private final String id;
    private final int level;

    SubStage(String id, int level) {
        this.id = id;
        this.level = level;
    }

    public String id() {
        return this.id;
    }

    /** 内部序号：4 档境界 = 0-3；数字层境界 = 1-based（第几层） */
    public int level() {
        return this.level;
    }

    public String translationKey() {
        return "sub_stage.friday_cultivation." + this.id;
    }

    public Component displayName() {
        return Component.translatable(this.translationKey());
    }

    /** 向后兼容：仅 4 档境界正确（本档是否 PEAK） */
    public boolean isPeak() {
        return this == PEAK;
    }

    /** realm 感知：该子阶段是否为所属境界的最高档（数字层=最高层；4 档=PEAK） */
    public boolean isPeakFor(Realm realm) {
        return realm.usesNumericLevels() ? this.level >= realm.subStageCount() : this.level >= realm.subStageCount() - 1;
    }

    /** realm 驱动推进（4 档：EARLY→MIDDLE→LATE→PEAK；数字层：level+1） */
    public SubStage nextFor(Realm realm) {
        return realm.subStageAt(this.level + 1);
    }

    /** realm 驱动回退 */
    public SubStage prevFor(Realm realm) {
        return realm.subStageAt(this.level - 1);
    }

    /** 向后兼容 next（仅 4 档正确） */
    public SubStage next() {
        if (this == EARLY) return MIDDLE;
        if (this == MIDDLE) return LATE;
        if (this == LATE) return PEAK;
        return PEAK;
    }

    /** 向后兼容 prev（仅 4 档正确） */
    public SubStage prev() {
        if (this == PEAK) return LATE;
        if (this == LATE) return MIDDLE;
        if (this == MIDDLE) return EARLY;
        return EARLY;
    }

    /** 向后兼容 byId（无 realm 上下文，仅 4 档） */
    public static SubStage byId(String id) {
        if ("early".equals(id)) return EARLY;
        if ("middle".equals(id)) return MIDDLE;
        if ("late".equals(id)) return LATE;
        if ("peak".equals(id)) return PEAK;
        return EARLY;
    }

    /** realm 感知 byId：支持数字层 id（"1".."10"）及命名 id（"turn_1"/"heaven_1"/"dao_*"） */
    public static SubStage byId(String id, Realm realm) {
        if ("early".equals(id)) return EARLY;
        if ("middle".equals(id)) return MIDDLE;
        if ("late".equals(id)) return LATE;
        if ("peak".equals(id)) return PEAK;
        if (realm != null) {
            try {
                if (id != null && (id.startsWith("turn_") || id.startsWith("heaven_"))) {
                    int lvl = Integer.parseInt(id.substring(id.indexOf('_') + 1));
                    SubStage s = realm.subStageAt(lvl);
                    if (s != null) return s;
                }
                if (id != null && id.startsWith("dao_")) {
                    for (int i = 1; i <= realm.subStageCount(); ++i) {
                        SubStage s = realm.subStageAt(i);
                        if (s != null && id.equals(s.id())) return s;
                    }
                }
                int lvl = Integer.parseInt(id);
                SubStage s = realm.subStageAt(lvl);
                if (s != null) return s;
            }
            catch (NumberFormatException numberFormatException) {
                // ignore
            }
            SubStage first = realm.firstSubStage();
            return first != null ? first : EARLY;
        }
        return EARLY;
    }

    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SubStage)) return false;
        SubStage s = (SubStage)o;
        return this.level == s.level && this.id.equals(s.id);
    }

    public int hashCode() {
        return 31 * this.id.hashCode() + this.level;
    }

    public String toString() {
        return "SubStage{" + this.id + ",level=" + this.level + "}";
    }
}
