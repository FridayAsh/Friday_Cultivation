/*
 * Decompiled with CFR 0.152.
 */
package com.friday.cultivation.cultivation.qi.formation;

import com.friday.cultivation.cultivation.ItemTier;
import com.friday.cultivation.cultivation.qi.field.QiModifier;

/*
 * Uses 'sealed' constructs - enablewith --sealed true
 */
public enum FormationType {
    QI_GATHERING("qi_gathering", 1){

        @Override
        public QiModifier modifierForTier(ItemTier flagTier) {
            double mult = switch (flagTier) {
                default -> throw new IncompatibleClassChangeError();
                case LOW -> 2.0;
                case MID -> 2.5;
                case HIGH -> 3.0;
                case SUPREME -> 4.0;
                case IMMORTAL -> 5.0;
            };
            return new QiModifier(1.5, mult, mult, 1.0);
        }

        @Override
        public double drainPerBlockPerHour(ItemTier flagTier) {
            return switch (flagTier) {
                default -> throw new IncompatibleClassChangeError();
                case LOW -> 1.0;
                case MID -> 2.0;
                case HIGH -> 3.0;
                case SUPREME -> 4.0;
                case IMMORTAL -> 5.0;
            };
        }
    }
    ,
    SECT_PROTECTION("sect_protection", 1){

        @Override
        public QiModifier modifierForTier(ItemTier flagTier) {
            return QiModifier.IDENTITY;
        }

        @Override
        public double drainPerBlockPerHour(ItemTier flagTier) {
            return switch (flagTier) {
                default -> throw new IncompatibleClassChangeError();
                case LOW -> 1.0;
                case MID -> 2.0;
                case HIGH -> 3.0;
                case SUPREME -> 4.0;
                case IMMORTAL -> 5.0;
            };
        }

        @Override
        public long sectProtectionQiPerDamage(ItemTier flagTier) {
            return switch (flagTier) {
                default -> throw new IncompatibleClassChangeError();
                case LOW -> 100L;
                case MID -> 50L;
                case HIGH -> 20L;
                case SUPREME -> 5L;
                case IMMORTAL -> 1L;
            };
        }

        @Override
        public double sectProtectionBarrierDamagePerSecond(ItemTier flagTier) {
            return flagTier == ItemTier.IMMORTAL ? 20.0 : 0.0;
        }
    }
    ,
    WITHER_GROWTH("wither_growth", 1){

        @Override
        public QiModifier modifierForTier(ItemTier flagTier) {
            return QiModifier.IDENTITY;
        }

        @Override
        public double drainPerBlockPerHour(ItemTier flagTier) {
            return FormationType.standardDrain(flagTier);
        }

        @Override
        public boolean drainsByCoveredBlocks() {
            return true;
        }

        @Override
        public double growthMultiplier(ItemTier flagTier) {
            return switch (flagTier) {
                default -> throw new IncompatibleClassChangeError();
                case LOW -> 2.0;
                case MID -> 4.0;
                case HIGH -> 6.0;
                case SUPREME -> 8.0;
                case IMMORTAL -> 10.0;
            };
        }
    }
    ,
    REJUVENATION("rejuvenation", 1){

        @Override
        public QiModifier modifierForTier(ItemTier flagTier) {
            return QiModifier.IDENTITY;
        }

        @Override
        public double drainPerBlockPerHour(ItemTier flagTier) {
            return FormationType.standardDrain(flagTier);
        }

        @Override
        public boolean drainsByCoveredBlocks() {
            return true;
        }

        @Override
        public int rejuvenationAmplifier(ItemTier flagTier) {
            return switch (flagTier) {
                default -> throw new IncompatibleClassChangeError();
                case LOW -> 0;
                case MID -> 1;
                case HIGH -> 2;
                case SUPREME -> 3;
                case IMMORTAL -> 4;
            };
        }
    }
    ,
    FLIGHT_BAN("flight_ban", 1){

        @Override
        public QiModifier modifierForTier(ItemTier flagTier) {
            return QiModifier.IDENTITY;
        }

        @Override
        public double drainPerBlockPerHour(ItemTier flagTier) {
            return FormationType.standardDrain(flagTier);
        }

        @Override
        public boolean drainsByCoveredBlocks() {
            return true;
        }

        @Override
        public boolean flightBanPullsAirborneHostiles(ItemTier flagTier) {
            return flagTier != ItemTier.LOW;
        }

        @Override
        public int flightBanSlownessAmplifier(ItemTier flagTier) {
            return switch (flagTier) {
                case HIGH -> 0;
                case SUPREME -> 2;
                case IMMORTAL -> 6;
                default -> -1;
            };
        }
    }
    ,
    MAZE("maze", 1){

        @Override
        public QiModifier modifierForTier(ItemTier flagTier) {
            return QiModifier.IDENTITY;
        }

        @Override
        public double drainPerBlockPerHour(ItemTier flagTier) {
            return FormationType.standardDrain(flagTier);
        }

        @Override
        public boolean drainsByCoveredBlocks() {
            return true;
        }

        @Override
        public boolean mazeAppliesNausea(ItemTier flagTier) {
            return flagTier != ItemTier.LOW;
        }

        @Override
        public int mazeTeleportDelayTicks(ItemTier flagTier) {
            return switch (flagTier) {
                case HIGH -> 1200;
                case SUPREME -> 600;
                case IMMORTAL -> 20;
                default -> -1;
            };
        }
    }
    ,
    FARM_HARVEST("farm_harvest", 1){

        @Override
        public QiModifier modifierForTier(ItemTier flagTier) {
            return QiModifier.IDENTITY;
        }

        @Override
        public double drainPerBlockPerHour(ItemTier flagTier) {
            return FormationType.standardDrain(flagTier);
        }

        @Override
        public boolean drainsByCoveredBlocks() {
            return true;
        }

        @Override
        public int harvestIntervalTicks(ItemTier flagTier) {
            return switch (flagTier) {
                default -> throw new IncompatibleClassChangeError();
                case LOW -> 1200;
                case MID -> 600;
                case HIGH -> 200;
                case SUPREME, IMMORTAL -> 20;
            };
        }

        @Override
        public int harvestBatchSize(ItemTier flagTier) {
            return flagTier == ItemTier.IMMORTAL ? 10 : 1;
        }

        @Override
        public boolean harvestDoublesDrops(ItemTier flagTier) {
            return flagTier == ItemTier.IMMORTAL;
        }
    };

    private final String id;
    private final int minFlagsRequired;

    private FormationType(String id, int minFlagsRequired) {
        this.id = id;
        this.minFlagsRequired = minFlagsRequired;
    }

    private static double standardDrain(ItemTier flagTier) {
        return switch (flagTier) {
            default -> throw new IncompatibleClassChangeError();
            case LOW -> 1.0;
            case MID -> 2.0;
            case HIGH -> 3.0;
            case SUPREME -> 4.0;
            case IMMORTAL -> 5.0;
        };
    }

    public String id() {
        return this.id;
    }

    public int minFlagsRequired() {
        return this.minFlagsRequired;
    }

    public String translationKey() {
        return "formation.friday_cultivation." + this.id;
    }

    public int visualColor() {
        return switch (this) {
            default -> throw new IncompatibleClassChangeError();
            case QI_GATHERING -> 9169528;
            case SECT_PROTECTION -> 11701503;
            case WITHER_GROWTH -> 6213470;
            case REJUVENATION -> 16751798;
            case FLIGHT_BAN -> 7981055;
            case MAZE -> 12616956;
            case FARM_HARVEST -> 15909198;
        };
    }

    public abstract QiModifier modifierForTier(ItemTier var1);

    public abstract double drainPerBlockPerHour(ItemTier var1);

    public boolean drainsByCoveredBlocks() {
        return false;
    }

    public double growthMultiplier(ItemTier flagTier) {
        return 1.0;
    }

    public int rejuvenationAmplifier(ItemTier flagTier) {
        return -1;
    }

    public boolean flightBanPullsAirborneHostiles(ItemTier flagTier) {
        return false;
    }

    public int flightBanSlownessAmplifier(ItemTier flagTier) {
        return -1;
    }

    public boolean mazeAppliesNausea(ItemTier flagTier) {
        return false;
    }

    public int mazeTeleportDelayTicks(ItemTier flagTier) {
        return -1;
    }

    public int harvestIntervalTicks(ItemTier flagTier) {
        return -1;
    }

    public int harvestBatchSize(ItemTier flagTier) {
        return 0;
    }

    public boolean harvestDoublesDrops(ItemTier flagTier) {
        return false;
    }

    public long sectProtectionQiPerDamage(ItemTier flagTier) {
        throw new UnsupportedOperationException("Only SECT_PROTECTION supports this query");
    }

    public double sectProtectionBarrierDamagePerSecond(ItemTier flagTier) {
        return 0.0;
    }
}

