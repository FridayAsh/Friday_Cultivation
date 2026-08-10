package com.friday.cultivation.realm;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

/**
 * 修仙境界枚举 — 12个境界
 * 严格复刻原模组 com.xiaoxiang.cultivation.cultivation.realm.Realm
 */
public enum Realm {
    MORTAL("mortal", "凡人"),
    QI_REFINING("qi_refining", "炼气"),
    FOUNDATION_BUILDING("foundation_building", "筑基"),
    GOLDEN_CORE("golden_core", "金丹"),
    NASCENT_SOUL("nascent_soul", "元婴"),
    SOUL_FORMATION("soul_formation", "化神"),
    VOID_REFINING("void_refining", "炼虚"),
    BODY_INTEGRATION("body_integration", "合体"),
    MAHAYANA("mahayana", "大乘"),
    TRIBULATION_TRANSCENDENCE("tribulation_transcendence", "渡劫"),
    TRUE_IMMORTAL("true_immortal", "真仙"),
    LOOSE_IMMORTAL("loose_immortal", "散仙");

    private final String id;
    private final String chineseName;

    Realm(String id, String chineseName) {
        this.id = id;
        this.chineseName = chineseName;
    }

    public String id() { return id; }

    public ResourceLocation registryName() {
        return new ResourceLocation("friday_cultivation", this.id);
    }

    /** 境界进度索引（照搬原模组：MORTAL=0、真仙=37、散仙=38、其余=1+(ordinal-1)*4+子阶段） */
    public int progressIndex(SubStage subStage) {
        if (this == MORTAL) {
            return 0;
        }
        if (this == TRUE_IMMORTAL) {
            return 37;
        }
        if (this == LOOSE_IMMORTAL) {
            return 38;
        }
        return 1 + (this.ordinal() - 1) * 4 + subStage.ordinal();
    }

    /** 境界最大灵气（照搬原模组各境界硬编码值） */
    public int maxQi(SubStage subStage) {
        if (this == MORTAL) {
            return 100;
        }
        if (this == TRUE_IMMORTAL) {
            return 20900;
        }
        if (this == LOOSE_IMMORTAL) {
            return 18000;
        }
        int prevPeak = this == QI_REFINING ? 100 : 500 + (this.ordinal() - 2) * 1300;
        int delta;
        if (this == QI_REFINING) {
            delta = switch (subStage) {
                case EARLY -> 100;
                case MIDDLE -> 200;
                case LATE -> 300;
                case PEAK -> 400;
            };
        } else {
            delta = switch (subStage) {
                case EARLY -> 1000;
                case MIDDLE -> 1100;
                case LATE -> 1200;
                case PEAK -> 1300;
            };
        }
        return prevPeak + delta;
    }

    /** 基础寿命（照搬原模组） */
    public int baseLifespan() {
        return switch (this) {
            case MORTAL -> 0;
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
        };
    }

    public String translationKey() {
        return "realm.friday_cultivation." + this.id;
    }

    public Component displayName() {
        return Component.translatableWithFallback(this.translationKey(), chineseName);
    }

    /** NPC分类翻译键（照搬原模组：凡人/修士/仙人） */
    public String npcCategoryTranslationKey() {
        String category = this == MORTAL ? "mortal" : (this.ordinal() >= TRUE_IMMORTAL.ordinal() ? "immortal" : "cultivator");
        return "npc_category.friday_cultivation." + category;
    }

    public Component npcCategoryName() {
        return Component.translatable(this.npcCategoryTranslationKey());
    }

    /** 是否为修士（照搬原模组：凡人不算修士） */
    public boolean isCultivator() {
        return this != MORTAL;
    }

    /** 基础吸收倍率（照搬原模组：= 境界ordinal） */
    public int baseAbsorbMult() {
        return this.ordinal();
    }

    /** 灵气护盾减伤百分比（照搬原模组） */
    public int qiShieldReductionPercent() {
        return switch (this) {
            case MORTAL -> 0;
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

    /** 天劫波数（照搬原模组：金丹~合体/渡劫=9、筑基圆满=3、大乘=0、真仙/散仙=-1） */
    public int tribulationCount(SubStage stage) {
        return switch (this) {
            case MORTAL -> 0;
            case QI_REFINING -> 0;
            case FOUNDATION_BUILDING -> stage == SubStage.PEAK ? 3 : 1;
            case GOLDEN_CORE, NASCENT_SOUL, SOUL_FORMATION, VOID_REFINING, BODY_INTEGRATION, TRIBULATION_TRANSCENDENCE -> 9;
            case MAHAYANA -> 0;
            case LOOSE_IMMORTAL, TRUE_IMMORTAL -> -1;
        };
    }

    /** 每波天劫雷数（照搬原模组） */
    public int tribulationBoltsPerWave(SubStage stage) {
        return switch (this) {
            case GOLDEN_CORE -> stage == SubStage.PEAK ? 3 : 1;
            case NASCENT_SOUL -> stage == SubStage.PEAK ? 6 : 3;
            case SOUL_FORMATION -> 6;
            case VOID_REFINING -> stage == SubStage.PEAK ? 8 : 6;
            case BODY_INTEGRATION -> 8;
            case TRIBULATION_TRANSCENDENCE -> 9;
            default -> 1;
        };
    }

    /** 天劫每击伤害（照搬原模组） */
    public int tribulationStrikeDamage() {
        return switch (this) {
            case MORTAL -> 0;
            case QI_REFINING -> 30;
            case FOUNDATION_BUILDING -> 40;
            case GOLDEN_CORE -> 50;
            case NASCENT_SOUL -> 60;
            case SOUL_FORMATION -> 70;
            case VOID_REFINING -> 80;
            case BODY_INTEGRATION -> 90;
            case MAHAYANA -> 0;
            case TRIBULATION_TRANSCENDENCE -> 150;
            case LOOSE_IMMORTAL, TRUE_IMMORTAL -> 0;
        };
    }

    /** 格式化天劫计数（照搬原模组：单雷=波数，多雷=波数x雷数） */
    public static String formatTribulationCount(int waves, int boltsPerWave) {
        if (waves <= 0) {
            return "0";
        }
        int bolts = Math.max(1, boltsPerWave);
        return bolts == 1 ? Integer.toString(waves) : waves + "x" + bolts;
    }

    public static Realm byId(String id) {
        for (Realm r : values()) {
            if (r.id.equals(id)) return r;
        }
        return MORTAL;
    }
}
