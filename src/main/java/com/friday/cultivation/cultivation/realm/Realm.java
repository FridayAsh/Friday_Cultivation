/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.network.chat.Component
 *  net.minecraft.resources.ResourceLocation
 */
package com.friday.cultivation.cultivation.realm;

import com.friday.cultivation.cultivation.realm.SubStage;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public enum Realm {
    MORTAL("mortal"),
    BODY_TEMPERING("body_tempering"),
    QI_REFINING("qi_refining"),
    FOUNDATION_BUILDING("foundation_building"),
    GOLDEN_CORE("golden_core"),
    NASCENT_SOUL("nascent_soul"),
    SOUL_FORMATION("soul_formation"),
    VOID_REFINING("void_refining"),
    BODY_INTEGRATION("body_integration"),
    MAHAYANA("mahayana"),
    TRIBULATION_TRANSCENDENCE("tribulation_transcendence"),
    TRUE_IMMORTAL("true_immortal"),
    LOOSE_IMMORTAL("loose_immortal"),
    GREAT_EMPEROR("great_emperor");

    private final String id;

    private Realm(String id) {
        this.id = id;
    }

    public String id() {
        return this.id;
    }

    public ResourceLocation registryName() {
        return new ResourceLocation("friday_cultivation", this.id);
    }

    /** 该境界的子阶段档数 */
    public int subStageCount() {
        return switch (this) {
            case MORTAL, LOOSE_IMMORTAL -> 1;
            case BODY_TEMPERING -> 10;
            case QI_REFINING -> 9;
            case GOLDEN_CORE -> 9;
            case BODY_INTEGRATION -> 5;
            case TRUE_IMMORTAL -> 9;
            case GREAT_EMPEROR -> 9;
            case FOUNDATION_BUILDING, NASCENT_SOUL, SOUL_FORMATION, VOID_REFINING, MAHAYANA, TRIBULATION_TRANSCENDENCE -> 4;
        };
    }

    /** 是否使用数字层（1-based） */
    public boolean usesNumericLevels() {
        return this == BODY_TEMPERING || this == QI_REFINING || this == GOLDEN_CORE || this == BODY_INTEGRATION || this == TRUE_IMMORTAL || this == GREAT_EMPEROR;
    }

    /** 获取该境界第 level 层的 SubStage（数字层 1-based；4 档 0-3；越界返回 null） */
    public SubStage subStageAt(int level) {
        if (this == BODY_TEMPERING) {
            return level >= 1 && level <= 10 ? new SubStage(Integer.toString(level), level) : null;
        }
        if (this == QI_REFINING) {
            return level >= 1 && level <= 9 ? new SubStage(Integer.toString(level), level) : null;
        }
        if (this == GOLDEN_CORE) {
            return level >= 1 && level <= 9 ? new SubStage("turn_" + level, level) : null;
        }
        if (this == BODY_INTEGRATION) {
            return switch (level) {
                case 1 -> new SubStage("dao_entry", 1);
                case 2 -> new SubStage("dao_imperial", 2);
                case 3 -> new SubStage("dao_union", 3);
                case 4 -> new SubStage("dao_domain", 4);
                case 5 -> new SubStage("dao_boundary", 5);
                default -> null;
            };
        }
        if (this == TRUE_IMMORTAL) {
            return level >= 1 && level <= 9 ? new SubStage("heaven_" + level, level) : null;
        }
        if (this == GREAT_EMPEROR) {
            return level >= 1 && level <= 9 ? new SubStage("emperor_" + level, level) : null;
        }
        if (this == MORTAL || this == LOOSE_IMMORTAL) {
            return SubStage.EARLY;
        }
        return switch (level) {
            case 0 -> SubStage.EARLY;
            case 1 -> SubStage.MIDDLE;
            case 2 -> SubStage.LATE;
            case 3 -> SubStage.PEAK;
            default -> null;
        };
    }

    /** 初始子阶段（数字层为第 1 层；4 档为 EARLY；单档为 EARLY） */
    public SubStage firstSubStage() {
        return this.subStageAt(this.usesNumericLevels() ? 1 : 0);
    }

    /** 最高子阶段（数字层为最顶层；4 档为 PEAK；单档为 EARLY） */
    public SubStage lastSubStage() {
        int n = this.subStageCount();
        if (n <= 1) {
            return SubStage.EARLY;
        }
        return this.subStageAt(this.usesNumericLevels() ? n : n - 1);
    }

    public int progressIndex(SubStage subStage) {
        if (this == MORTAL) {
            return 0;
        }
        if (this == LOOSE_IMMORTAL) {
            return 38;
        }
        int acc = 0;
        for (Realm r : Realm.values()) {
            if (r == this) {
                break;
            }
            if (r == MORTAL || r == LOOSE_IMMORTAL) {
                acc += 1;
            } else {
                acc += r.subStageCount();
            }
        }
        int idx = subStage == null ? 0 : subStage.level();
        if (this.usesNumericLevels()) {
            idx = Math.max(0, idx - 1);
        }
        return 1 + acc + idx;
    }

    public int maxQi(SubStage subStage) {
        if (this == MORTAL) {
            return 100;
        }
        if (this == BODY_TEMPERING) {
            return 100;
        }
        if (this == QI_REFINING) {
            int lvl = subStage == null ? 1 : Math.max(1, subStage.level());
            return 100 * lvl;
        }
        if (this == GOLDEN_CORE) {
            int lvl = subStage == null ? 1 : Math.max(1, subStage.level());
            return 2200 + 1000 + (lvl - 1) * 100;
        }
        if (this == BODY_INTEGRATION) {
            int lvl = subStage == null ? 1 : Math.max(1, subStage.level());
            return 7400 + 1000 + (lvl - 1) * 100;
        }
        if (this == TRUE_IMMORTAL) {
            int lvl = subStage == null ? 1 : Math.max(1, subStage.level());
            return 19000 + lvl * 200;
        }
        if (this == GREAT_EMPEROR) {
            int lvl = subStage == null ? 1 : Math.max(1, subStage.level());
            return 25000 + lvl * 300;
        }
        if (this == LOOSE_IMMORTAL) {
            return 18000;
        }
        int prevPeak = Realm.MAX_QI_PREV_PEAK.getOrDefault(this, 500);
        int delta = 1000;
        if (subStage != null) {
            if (subStage == SubStage.EARLY) {
                delta = 1000;
            } else if (subStage == SubStage.MIDDLE) {
                delta = 1100;
            } else if (subStage == SubStage.LATE) {
                delta = 1200;
            } else if (subStage == SubStage.PEAK) {
                delta = 1300;
            }
        }
        return prevPeak + delta;
    }

    public int baseLifespan() {
        return switch (this) {
            case MORTAL -> 0;
            case BODY_TEMPERING -> 100;
            case QI_REFINING -> 200;
            case FOUNDATION_BUILDING -> 200;
            case GOLDEN_CORE -> 1000;
            case NASCENT_SOUL -> 3000;
            case SOUL_FORMATION -> 10000;
            case VOID_REFINING -> 30000;
            case BODY_INTEGRATION -> 100000;
            case MAHAYANA -> 300000;
            case TRIBULATION_TRANSCENDENCE -> 600000;
            case LOOSE_IMMORTAL -> 1000000;
            case TRUE_IMMORTAL -> 1000000;
            case GREAT_EMPEROR -> 10000000;
        };
    }

    public String translationKey() {
        return "realm.friday_cultivation." + this.id;
    }

    public Component displayName() {
        return Component.translatable((String)this.translationKey());
    }

    public String npcCategoryTranslationKey() {
        String category = this == MORTAL ? "mortal" : (this.ordinal() >= TRUE_IMMORTAL.ordinal() ? "immortal" : "cultivator");
        return "npc_category.friday_cultivation." + category;
    }

    public Component npcCategoryName() {
        return Component.translatable((String)this.npcCategoryTranslationKey());
    }

    public boolean isCultivator() {
        return this != MORTAL;
    }

    /** 吸收倍率（查表，消除 ordinal 数值） */
    public int absorbMultiplier() {
        return switch (this) {
            case MORTAL -> 0;
            case BODY_TEMPERING -> 1;
            case QI_REFINING -> 2;
            case FOUNDATION_BUILDING -> 3;
            case GOLDEN_CORE -> 4;
            case NASCENT_SOUL -> 5;
            case SOUL_FORMATION -> 6;
            case VOID_REFINING -> 7;
            case BODY_INTEGRATION -> 8;
            case MAHAYANA -> 9;
            case TRIBULATION_TRANSCENDENCE -> 10;
            case TRUE_IMMORTAL -> 11;
            case LOOSE_IMMORTAL -> 12;
            case GREAT_EMPEROR -> 13;
        };
    }

    /** 向后兼容 baseAbsorbMult */
    public int baseAbsorbMult() {
        return this.absorbMultiplier();
    }

    /** 境界压制排名（查表，消除 ordinal*10） */
    public int pressureRank() {
        return this.absorbMultiplier() * 10;
    }

    /** NPC 基础血量按境界查表（消除 20+ordinal*22） */
    public double baseHealthForNpc() {
        return switch (this) {
            case MORTAL -> 20.0;
            case BODY_TEMPERING -> 42.0;
            case QI_REFINING -> 64.0;
            case FOUNDATION_BUILDING -> 86.0;
            case GOLDEN_CORE -> 108.0;
            case NASCENT_SOUL -> 130.0;
            case SOUL_FORMATION -> 152.0;
            case VOID_REFINING -> 174.0;
            case BODY_INTEGRATION -> 196.0;
            case MAHAYANA -> 218.0;
            case TRIBULATION_TRANSCENDENCE -> 240.0;
            case TRUE_IMMORTAL -> 262.0;
            case LOOSE_IMMORTAL -> 284.0;
            case GREAT_EMPEROR -> 400.0;
        };
    }

    public int qiShieldReductionPercent() {
        return switch (this) {
            case MORTAL -> 0;
            case BODY_TEMPERING -> 20;
            case QI_REFINING -> 30;
            case FOUNDATION_BUILDING -> 40;
            case GOLDEN_CORE -> 50;
            case NASCENT_SOUL -> 60;
            case SOUL_FORMATION -> 70;
            case VOID_REFINING -> 80;
            case BODY_INTEGRATION -> 85;
            case MAHAYANA -> 90;
            case TRIBULATION_TRANSCENDENCE -> 95;
            case TRUE_IMMORTAL -> 100;
            case LOOSE_IMMORTAL -> 95;
            case GREAT_EMPEROR -> 100;
        };
    }

    public Realm next() {
        int idx = this.ordinal();
        if (idx >= Realm.values().length - 1) {
            return this;
        }
        return Realm.values()[idx + 1];
    }

    public Realm prev() {
        int idx = this.ordinal();
        if (idx <= 0) {
            return this;
        }
        return Realm.values()[idx - 1];
    }

    public int tribulationCount(SubStage stage) {
        return switch (this) {
            case MORTAL -> 0;
            case BODY_TEMPERING -> 0;
            case QI_REFINING -> 0;
            case FOUNDATION_BUILDING -> {
                if (stage != null && stage == SubStage.PEAK) {
                    yield 3;
                }
                yield 1;
            }
            case GOLDEN_CORE, NASCENT_SOUL, SOUL_FORMATION, VOID_REFINING, TRIBULATION_TRANSCENDENCE -> 9;
            // 合道五境按小境界分配波数：入境 5 波起，每境 +1 波（入境5/御境6/合境7/域境8/界境9）
            case BODY_INTEGRATION -> {
                if (stage != null && stage.level() >= 1 && stage.level() <= 5) {
                    yield 4 + stage.level();
                }
                yield 9;
            }
            case MAHAYANA -> 0;
            case LOOSE_IMMORTAL -> -1;
            case TRUE_IMMORTAL -> {
                // 真仙每 3 重天渡劫一次：三重天→四重天 3波×9道，六重天→七重天 6波×9道
                // 九重天圆满 9波×9道：当前九重天封顶无法突破，暂不生效；
                // 预留接口——后续接入新境界（放开 canBreakthrough/advanceOnSuccess 封顶）后自动生效
                if (stage != null && stage.level() == 3) {
                    yield 3;
                }
                if (stage != null && stage.level() == 6) {
                    yield 6;
                }
                if (stage != null && stage.level() == 9) {
                    yield 9;
                }
                yield 0;
            }
            // 大帝九帝界：每个帝界突破均渡劫，第一帝界 9 波起每界 +1（9~17 波）
            case GREAT_EMPEROR -> {
                if (stage != null && stage.level() >= 1 && stage.level() <= 9) {
                    yield 8 + stage.level();
                }
                yield 9;
            }
        };
    }

    public int tribulationBoltsPerWave(SubStage stage) {
        return switch (this) {
            case GOLDEN_CORE -> {
                // 金丹九转（圆满）时每波 3 道，其余各转每波 1 道
                if (stage != null && stage.isPeakFor(this)) {
                    yield 3;
                }
                yield 1;
            }
            case NASCENT_SOUL -> {
                if (stage != null && stage == SubStage.PEAK) {
                    yield 6;
                }
                yield 3;
            }
            case SOUL_FORMATION -> 6;
            case VOID_REFINING -> {
                if (stage != null && stage == SubStage.PEAK) {
                    yield 8;
                }
                yield 6;
            }
            case BODY_INTEGRATION -> 8;
            case TRIBULATION_TRANSCENDENCE -> 9;
            // 真仙渡劫每波 9 道（配合 tribulationCount：3重天 3波、6重天 6波、9重天 9波 → 27/54/81 道）
            case TRUE_IMMORTAL -> 9;
            // 大帝渡劫每波 9 道（配合 tribulationCount：5~13 波 → 45~117 道）
            case GREAT_EMPEROR -> 9;
            default -> 1;
        };
    }

    public int tribulationStrikeDamage() {
        return switch (this) {
            case MORTAL -> 0;
            case BODY_TEMPERING -> 0;
            case QI_REFINING -> 30;
            case FOUNDATION_BUILDING -> 40;
            case GOLDEN_CORE -> 50;
            case NASCENT_SOUL -> 60;
            case SOUL_FORMATION -> 70;
            case VOID_REFINING -> 80;
            case BODY_INTEGRATION -> 90;
            case MAHAYANA -> 0;
            case TRIBULATION_TRANSCENDENCE -> 150;
            case TRUE_IMMORTAL -> 180;
            case GREAT_EMPEROR -> 200;
            case LOOSE_IMMORTAL -> 0;
        };
    }

    public static String formatTribulationCount(int waves, int boltsPerWave) {
        if (waves <= 0) {
            return "0";
        }
        int bolts = Math.max(1, boltsPerWave);
        return bolts == 1 ? Integer.toString(waves) : waves + "x" + bolts;
    }

    public static Realm byId(String id) {
        for (Realm r : Realm.values()) {
            if (!r.id.equals(id)) continue;
            return r;
        }
        return MORTAL;
    }

    private static final java.util.Map<Realm, Integer> MAX_QI_PREV_PEAK = java.util.Map.ofEntries(
        java.util.Map.entry(FOUNDATION_BUILDING, 900),
        java.util.Map.entry(GOLDEN_CORE, 2200),
        java.util.Map.entry(NASCENT_SOUL, 3500),
        java.util.Map.entry(SOUL_FORMATION, 4800),
        java.util.Map.entry(VOID_REFINING, 6100),
        java.util.Map.entry(BODY_INTEGRATION, 7400),
        java.util.Map.entry(MAHAYANA, 8700),
        java.util.Map.entry(TRIBULATION_TRANSCENDENCE, 10000)
    );
}
