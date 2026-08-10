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
    LOOSE_IMMORTAL("loose_immortal");

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

    public int maxQi(SubStage subStage) {
        int delta;
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
        if (this == QI_REFINING) {
            delta = switch (subStage) {
                default -> throw new IncompatibleClassChangeError();
                case EARLY -> 100;
                case MIDDLE -> 200;
                case LATE -> 300;
                case PEAK -> 400;
            };
        } else {
            delta = switch (subStage) {
                default -> throw new IncompatibleClassChangeError();
                case EARLY -> 1000;
                case MIDDLE -> 1100;
                case LATE -> 1200;
                case PEAK -> 1300;
            };
        }
        return prevPeak + delta;
    }

    public int baseLifespan() {
        return switch (this) {
            default -> throw new IncompatibleClassChangeError();
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

    public int baseAbsorbMult() {
        return this.ordinal();
    }

    public int qiShieldReductionPercent() {
        return switch (this) {
            default -> throw new IncompatibleClassChangeError();
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

    public int tribulationCount(SubStage stage) {
        return switch (this) {
            default -> throw new IncompatibleClassChangeError();
            case MORTAL -> 0;
            case QI_REFINING -> 0;
            case FOUNDATION_BUILDING -> {
                if (stage == SubStage.PEAK) {
                    yield 3;
                }
                yield 1;
            }
            case GOLDEN_CORE, NASCENT_SOUL, SOUL_FORMATION, VOID_REFINING, BODY_INTEGRATION, TRIBULATION_TRANSCENDENCE -> 9;
            case MAHAYANA -> 0;
            case LOOSE_IMMORTAL, TRUE_IMMORTAL -> -1;
        };
    }

    public int tribulationBoltsPerWave(SubStage stage) {
        return switch (this) {
            case GOLDEN_CORE -> {
                if (stage == SubStage.PEAK) {
                    yield 3;
                }
                yield 1;
            }
            case NASCENT_SOUL -> {
                if (stage == SubStage.PEAK) {
                    yield 6;
                }
                yield 3;
            }
            case SOUL_FORMATION -> 6;
            case VOID_REFINING -> {
                if (stage == SubStage.PEAK) {
                    yield 8;
                }
                yield 6;
            }
            case BODY_INTEGRATION -> 8;
            case TRIBULATION_TRANSCENDENCE -> 9;
            default -> 1;
        };
    }

    public int tribulationStrikeDamage() {
        return switch (this) {
            default -> throw new IncompatibleClassChangeError();
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
}

