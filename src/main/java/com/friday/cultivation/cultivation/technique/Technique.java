/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.network.chat.Component
 *  net.minecraft.resources.ResourceLocation
 */
package com.friday.cultivation.cultivation.technique;

import com.friday.cultivation.cultivation.ItemTier;
import com.friday.cultivation.cultivation.QiElement;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public enum Technique {
    FRAGMENT("fragment", ItemTier.LOW, QiElement.PURE, Bonus.NONE),
    BASIC_BODY("basic_body", ItemTier.LOW, QiElement.PURE, Bonus.builder().attack(1).maxHp(2).build()),
    BASIC_MIND("basic_mind", ItemTier.LOW, QiElement.PURE, Bonus.builder().qiAbsorbMult(1.5).build()),
    IRON_SKIN("iron_skin", ItemTier.LOW, QiElement.PURE, Bonus.builder().defense(2).build()),
    METAL_SWORD("metal_sword", ItemTier.MID, QiElement.METAL, Bonus.builder().attack(3).elementSpellMult(QiElement.METAL, 1.2).build()),
    WOOD_SPRING("wood_spring", ItemTier.MID, QiElement.WOOD, Bonus.builder().maxHp(4).autoRegenPerMinute(2).elementSpellMult(QiElement.WOOD, 1.15).build()),
    DEADWOOD_REBIRTH("deadwood_rebirth", ItemTier.MID, QiElement.WOOD, Bonus.builder().elementSpellMult(QiElement.WOOD, 1.3).woodSpellCostMult(0.8).photosynthesis(true).forestShelter(true).build()),
    WATER_STREAM("water_stream", ItemTier.MID, QiElement.WATER, Bonus.builder().waterBreathing(true).elementSpellMult(QiElement.WATER, 1.2).build()),
    FIRE_YANG("fire_yang", ItemTier.MID, QiElement.FIRE, Bonus.builder().attack(4).elementSpellMult(QiElement.FIRE, 1.25).build()),
    EARTH_MOUNTAIN("earth_mountain", ItemTier.MID, QiElement.EARTH, Bonus.builder().defense(5).knockbackResist(true).build()),
    ICE_FROST("ice_frost", ItemTier.MID, QiElement.ICE, Bonus.builder().elementSpellMult(QiElement.ICE, 1.25).build()),
    WIND_STEP("wind_step", ItemTier.MID, QiElement.WOOD, Bonus.builder().moveSpeed(0.2).fallDamageReduce(0.5).build()),
    SHADOW_CLOAK("shadow_cloak", ItemTier.MID, QiElement.PURE, Bonus.builder().nightVision(true).attack(2).build()),
    FIRE_IMMORTAL("fire_immortal", ItemTier.MID, QiElement.FIRE, Bonus.builder().attack(3).fireResistance(true).elementSpellMult(QiElement.FIRE, 1.15).build()),
    VAJRA_BODY("vajra_body", ItemTier.HIGH, QiElement.PURE, Bonus.builder().defense(10).knockbackResist(true).maxHp(6).build()),
    HEART_SUTRA("heart_sutra", ItemTier.HIGH, QiElement.PURE, Bonus.builder().qiAbsorbMult(2.5).qiAbsorbRange(6).build()),
    SWORD_HEART("sword_heart", ItemTier.HIGH, QiElement.METAL, Bonus.builder().critRate(20).attack(5).elementSpellMult(QiElement.METAL, 1.2).build()),
    NINE_ABYSS("nine_abyss", ItemTier.HIGH, QiElement.WATER, Bonus.builder().qiAbsorbMult(3.0).qiAbsorbRange(4).build()),
    DEMON_SLAYER("demon_slayer", ItemTier.HIGH, QiElement.METAL, Bonus.builder().attack(4).undeadBonusDamage(6).build()),
    TURTLE_SHELL("turtle_shell", ItemTier.HIGH, QiElement.EARTH, Bonus.builder().defense(4).waterBreathing(true).maxHp(4).build()),
    DIVINE_FORGE("divine_forge", ItemTier.HIGH, QiElement.FIRE, Bonus.builder().attack(2).refiningTierUpChance(0.25).build()),
    HEAVENLY_ELIXIR("heavenly_elixir", ItemTier.HIGH, QiElement.WOOD, Bonus.builder().maxHp(4).alchemyTierUpChance(0.25).build()),
    FIVE_ELEMENT("five_element", ItemTier.SUPREME, QiElement.PURE, Bonus.builder().attack(3).defense(3).elementSpellMult(QiElement.METAL, 1.15).elementSpellMult(QiElement.WOOD, 1.15).elementSpellMult(QiElement.WATER, 1.15).elementSpellMult(QiElement.FIRE, 1.15).elementSpellMult(QiElement.EARTH, 1.15).elementSpellMult(QiElement.ICE, 1.15).build()),
    CELESTIAL_IMMORTAL("celestial_immortal", ItemTier.SUPREME, QiElement.PURE, Bonus.builder().attack(5).defense(5).critRate(15).maxHp(10).qiAbsorbMult(2.0).qiAbsorbRange(4).nightVision(true).fireResistance(true).waterBreathing(true).build()),
    SKY_DEVOURING("sky_devouring", ItemTier.SUPREME, QiElement.PURE, Bonus.builder().qiAbsorbMult(10.0).qiAbsorbRange(10).build()),
    FIVE_ELEMENT_CHAOS_ART("five_element_chaos_art", ItemTier.SUPREME, QiElement.PURE, Bonus.NONE),
    IMMORTAL_INCANTATION("immortal_incantation", ItemTier.IMMORTAL, QiElement.PURE, Bonus.builder().qiAbsorbMult(10.0).qiAbsorbRange(10).maxHp(10).qiCostHalve(true).damageHalve(true).build()),
    QINGDI_LONGEVITY("qingdi_longevity", ItemTier.IMMORTAL, QiElement.WOOD, Bonus.builder().elementSpellMult(QiElement.WOOD, 2.0).woodSpellCostHalve(true).build()),
    GHOST_DAO_BASIC("ghost_dao_basic", ItemTier.SUPREME, QiElement.PURE, Bonus.NONE);

    private final String id;
    private final ItemTier tier;
    private final QiElement primaryElement;
    private final Bonus bonus;

    private Technique(String id, ItemTier tier, QiElement primaryElement, Bonus bonus) {
        this.id = id;
        this.tier = tier;
        this.primaryElement = primaryElement;
        this.bonus = bonus;
    }

    public String id() {
        return this.id;
    }

    public ItemTier tier() {
        return this.tier;
    }

    public QiElement primaryElement() {
        return this.primaryElement;
    }

    public Bonus bonus() {
        return this.bonus;
    }

    public DaoPath daoPath() {
        return this == GHOST_DAO_BASIC ? DaoPath.GHOST : DaoPath.HUMAN;
    }

    public boolean isGhostDao() {
        return this.daoPath() == DaoPath.GHOST;
    }

    public boolean isHumanDao() {
        return this.daoPath() == DaoPath.HUMAN;
    }

    public String daoPathTranslationKey() {
        return "technique_path.friday_cultivation." + this.daoPath().id();
    }

    public boolean isGhostOnly() {
        return this.isGhostDao();
    }

    public Component displayName() {
        return Component.translatable((String)("technique.friday_cultivation." + this.id));
    }

    public Component description() {
        return Component.translatable((String)("technique.friday_cultivation." + this.id + ".desc"));
    }

    public ResourceLocation iconTexture() {
        if (this == IMMORTAL_INCANTATION) {
            return new ResourceLocation("friday_cultivation", "textures/gui/technique_immortal_body.png");
        }
        return new ResourceLocation("friday_cultivation", "textures/gui/technique_" + this.id + ".png");
    }

    public static Technique byId(String id) {
        id = Technique.normalizeId(id);
        for (Technique t : Technique.values()) {
            if (!t.id.equals(id)) continue;
            return t;
        }
        return null;
    }

    public static String normalizeId(String id) {
        if (id == null || id.isEmpty()) {
            return "";
        }
        return "immortal_body".equals(id) ? Technique.IMMORTAL_INCANTATION.id : id;
    }

    public static final class Bonus {
        public static final Bonus NONE = new Builder().build();
        public final int attack;
        public final int defense;
        public final int critRate;
        public final int maxHp;
        public final int qiAbsorbRange;
        public final int refining;
        public final int alchemy;
        public final int autoRegenPerMinute;
        public final int undeadBonusDamage;
        public final double qiAbsorbMult;
        public final double moveSpeed;
        public final double fallDamageReduce;
        public final double[] elementSpellMult;
        public final boolean nightVision;
        public final boolean waterBreathing;
        public final boolean fireResistance;
        public final boolean knockbackResist;
        public final boolean blockAllPotions;
        public final boolean fireImmune;
        public final boolean qiCostHalve;
        public final boolean woodSpellCostHalve;
        public final double woodSpellCostMult;
        public final boolean photosynthesis;
        public final boolean forestShelter;
        public final boolean damageHalve;
        public final double alchemyTierUpChance;
        public final double refiningTierUpChance;

        private Bonus(Builder b) {
            this.attack = b.attack;
            this.defense = b.defense;
            this.critRate = b.critRate;
            this.maxHp = b.maxHp;
            this.qiAbsorbRange = b.qiAbsorbRange;
            this.refining = b.refining;
            this.alchemy = b.alchemy;
            this.autoRegenPerMinute = b.autoRegenPerMinute;
            this.undeadBonusDamage = b.undeadBonusDamage;
            this.qiAbsorbMult = b.qiAbsorbMult;
            this.moveSpeed = b.moveSpeed;
            this.fallDamageReduce = b.fallDamageReduce;
            this.elementSpellMult = (double[])b.elementSpellMult.clone();
            this.nightVision = b.nightVision;
            this.waterBreathing = b.waterBreathing;
            this.fireResistance = b.fireResistance;
            this.knockbackResist = b.knockbackResist;
            this.blockAllPotions = b.blockAllPotions;
            this.fireImmune = b.fireImmune;
            this.qiCostHalve = b.qiCostHalve;
            this.woodSpellCostHalve = b.woodSpellCostHalve;
            this.woodSpellCostMult = b.woodSpellCostMult;
            this.photosynthesis = b.photosynthesis;
            this.forestShelter = b.forestShelter;
            this.damageHalve = b.damageHalve;
            this.alchemyTierUpChance = b.alchemyTierUpChance;
            this.refiningTierUpChance = b.refiningTierUpChance;
        }

        public double spellMultFor(QiElement el) {
            if (el == null) {
                return 1.0;
            }
            return this.elementSpellMult[el.ordinal()];
        }

        public static Builder builder() {
            return new Builder();
        }

        public static final class Builder {
            int attack = 0;
            int defense = 0;
            int critRate = 0;
            int maxHp = 0;
            int qiAbsorbRange = 0;
            int refining = 0;
            int alchemy = 0;
            int autoRegenPerMinute = 0;
            int undeadBonusDamage = 0;
            double qiAbsorbMult = 1.0;
            double moveSpeed = 0.0;
            double fallDamageReduce = 0.0;
            final double[] elementSpellMult = new double[QiElement.values().length];
            boolean nightVision = false;
            boolean waterBreathing = false;
            boolean fireResistance = false;
            boolean knockbackResist = false;
            boolean blockAllPotions = false;
            boolean fireImmune = false;
            boolean qiCostHalve = false;
            boolean woodSpellCostHalve = false;
            double woodSpellCostMult = 1.0;
            boolean photosynthesis = false;
            boolean forestShelter = false;
            boolean damageHalve = false;
            double alchemyTierUpChance = 0.0;
            double refiningTierUpChance = 0.0;

            Builder() {
                for (int i = 0; i < this.elementSpellMult.length; ++i) {
                    this.elementSpellMult[i] = 1.0;
                }
            }

            public Builder attack(int v) {
                this.attack = v;
                return this;
            }

            public Builder defense(int v) {
                this.defense = v;
                return this;
            }

            public Builder critRate(int v) {
                this.critRate = v;
                return this;
            }

            public Builder maxHp(int v) {
                this.maxHp = v;
                return this;
            }

            public Builder qiAbsorbRange(int v) {
                this.qiAbsorbRange = v;
                return this;
            }

            public Builder refining(int v) {
                this.refining = v;
                return this;
            }

            public Builder alchemy(int v) {
                this.alchemy = v;
                return this;
            }

            public Builder autoRegenPerMinute(int v) {
                this.autoRegenPerMinute = v;
                return this;
            }

            public Builder undeadBonusDamage(int v) {
                this.undeadBonusDamage = v;
                return this;
            }

            public Builder qiAbsorbMult(double v) {
                this.qiAbsorbMult = v;
                return this;
            }

            public Builder moveSpeed(double v) {
                this.moveSpeed = v;
                return this;
            }

            public Builder fallDamageReduce(double v) {
                this.fallDamageReduce = v;
                return this;
            }

            public Builder elementSpellMult(QiElement el, double v) {
                this.elementSpellMult[el.ordinal()] = v;
                return this;
            }

            public Builder nightVision(boolean v) {
                this.nightVision = v;
                return this;
            }

            public Builder waterBreathing(boolean v) {
                this.waterBreathing = v;
                return this;
            }

            public Builder fireResistance(boolean v) {
                this.fireResistance = v;
                return this;
            }

            public Builder knockbackResist(boolean v) {
                this.knockbackResist = v;
                return this;
            }

            public Builder blockAllPotions(boolean v) {
                this.blockAllPotions = v;
                return this;
            }

            public Builder fireImmune(boolean v) {
                this.fireImmune = v;
                return this;
            }

            public Builder qiCostHalve(boolean v) {
                this.qiCostHalve = v;
                return this;
            }

            public Builder woodSpellCostHalve(boolean v) {
                this.woodSpellCostHalve = v;
                return this;
            }

            public Builder woodSpellCostMult(double v) {
                this.woodSpellCostMult = v;
                return this;
            }

            public Builder photosynthesis(boolean v) {
                this.photosynthesis = v;
                return this;
            }

            public Builder forestShelter(boolean v) {
                this.forestShelter = v;
                return this;
            }

            public Builder damageHalve(boolean v) {
                this.damageHalve = v;
                return this;
            }

            public Builder alchemyTierUpChance(double v) {
                this.alchemyTierUpChance = v;
                return this;
            }

            public Builder refiningTierUpChance(double v) {
                this.refiningTierUpChance = v;
                return this;
            }

            public Bonus build() {
                return new Bonus(this);
            }
        }
    }

    public static enum DaoPath {
        HUMAN("human_dao"),
        GHOST("ghost_dao");

        private final String id;

        private DaoPath(String id) {
            this.id = id;
        }

        public String id() {
            return this.id;
        }
    }
}

