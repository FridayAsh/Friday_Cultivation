package com.friday.cultivation.technique;

import com.friday.cultivation.ItemTier;
import com.friday.cultivation.spirit.QiElement;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

/**
 * 功法系统 — 27种功法
 * 复刻自原模组 com.xiaoxiang.cultivation.cultivation.technique.Technique
 */
public enum Technique {

    FRAGMENT("fragment", Tier.LOW, QiElement.PURE, Bonus.NONE, "残篇"),
    BASIC_BODY("basic_body", Tier.LOW, QiElement.PURE, Bonus.builder().attack(1).maxHp(2).build(), "炼体术"),
    BASIC_MIND("basic_mind", Tier.LOW, QiElement.PURE, Bonus.builder().qiAbsorbMult(1.5).build(), "静心诀"),
    IRON_SKIN("iron_skin", Tier.LOW, QiElement.PURE, Bonus.builder().defense(2).build(), "铁皮功"),

    METAL_SWORD("metal_sword", Tier.MID, QiElement.METAL,
            Bonus.builder().attack(3).elementSpellMult(QiElement.METAL, 1.2).build(), "金剑诀"),
    WOOD_SPRING("wood_spring", Tier.MID, QiElement.WOOD,
            Bonus.builder().maxHp(4).autoRegenPerMinute(2).elementSpellMult(QiElement.WOOD, 1.15).build(), "木春诀"),
    DEADWOOD_REBIRTH("deadwood_rebirth", Tier.MID, QiElement.WOOD,
            Bonus.builder().elementSpellMult(QiElement.WOOD, 1.3).woodSpellCostMult(0.8).photosynthesis(true).forestShelter(true).build(), "枯木逢春"),
    WATER_STREAM("water_stream", Tier.MID, QiElement.WATER,
            Bonus.builder().waterBreathing(true).elementSpellMult(QiElement.WATER, 1.2).build(), "流水诀"),
    FIRE_YANG("fire_yang", Tier.MID, QiElement.FIRE,
            Bonus.builder().attack(4).elementSpellMult(QiElement.FIRE, 1.25).build(), "火阳功"),
    EARTH_MOUNTAIN("earth_mountain", Tier.MID, QiElement.EARTH,
            Bonus.builder().defense(5).knockbackResist(true).build(), "土山诀"),
    ICE_FROST("ice_frost", Tier.MID, QiElement.ICE,
            Bonus.builder().elementSpellMult(QiElement.ICE, 1.25).build(), "冰霜诀"),
    WIND_STEP("wind_step", Tier.MID, QiElement.WOOD,
            Bonus.builder().moveSpeed(0.2).fallDamageReduce(0.5).build(), "风行步"),
    SHADOW_CLOAK("shadow_cloak", Tier.MID, QiElement.PURE,
            Bonus.builder().nightVision(true).attack(2).build(), "暗影斗篷"),
    FIRE_IMMORTAL("fire_immortal", Tier.MID, QiElement.FIRE,
            Bonus.builder().attack(3).fireResistance(true).elementSpellMult(QiElement.FIRE, 1.15).build(), "火仙诀"),

    VAJRA_BODY("vajra_body", Tier.HIGH, QiElement.PURE,
            Bonus.builder().defense(10).knockbackResist(true).maxHp(6).build(), "金刚体"),
    HEART_SUTRA("heart_sutra", Tier.HIGH, QiElement.PURE,
            Bonus.builder().qiAbsorbMult(2.5).qiAbsorbRange(6).build(), "心经"),
    SWORD_HEART("sword_heart", Tier.HIGH, QiElement.METAL,
            Bonus.builder().critRate(20).attack(5).elementSpellMult(QiElement.METAL, 1.2).build(), "剑心"),
    NINE_ABYSS("nine_abyss", Tier.HIGH, QiElement.WATER,
            Bonus.builder().qiAbsorbMult(3.0).qiAbsorbRange(4).build(), "九渊"),
    DEMON_SLAYER("demon_slayer", Tier.HIGH, QiElement.METAL,
            Bonus.builder().attack(4).undeadBonusDamage(6).build(), "斩魔"),
    TURTLE_SHELL("turtle_shell", Tier.HIGH, QiElement.EARTH,
            Bonus.builder().defense(4).waterBreathing(true).maxHp(4).build(), "龟甲"),
    DIVINE_FORGE("divine_forge", Tier.HIGH, QiElement.FIRE,
            Bonus.builder().attack(2).refiningTierUpChance(0.25).build(), "神锻"),
    HEAVENLY_ELIXIR("heavenly_elixir", Tier.HIGH, QiElement.WOOD,
            Bonus.builder().maxHp(4).alchemyTierUpChance(0.25).build(), "天丹"),

    FIVE_ELEMENT("five_element", Tier.SUPREME, QiElement.PURE,
            Bonus.builder().attack(3).defense(3)
                    .elementSpellMult(QiElement.METAL, 1.15).elementSpellMult(QiElement.WOOD, 1.15)
                    .elementSpellMult(QiElement.WATER, 1.15).elementSpellMult(QiElement.FIRE, 1.15)
                    .elementSpellMult(QiElement.EARTH, 1.15).elementSpellMult(QiElement.ICE, 1.15).build(), "五行诀"),
    CELESTIAL_IMMORTAL("celestial_immortal", Tier.SUPREME, QiElement.PURE,
            Bonus.builder().attack(5).defense(5).critRate(15).maxHp(10)
                    .qiAbsorbMult(2.0).qiAbsorbRange(4)
                    .nightVision(true).fireResistance(true).waterBreathing(true).build(), "天仙诀"),
    SKY_DEVOURING("sky_devouring", Tier.SUPREME, QiElement.PURE,
            Bonus.builder().qiAbsorbMult(10.0).qiAbsorbRange(10).build(), "吞天功"),
    FIVE_ELEMENT_CHAOS_ART("five_element_chaos_art", Tier.SUPREME, QiElement.PURE, Bonus.NONE, "五行混沌功"),

    IMMORTAL_INCANTATION("immortal_incantation", Tier.IMMORTAL, QiElement.PURE,
            Bonus.builder().qiAbsorbMult(10.0).qiAbsorbRange(10).maxHp(10)
                    .qiCostHalve(true).damageHalve(true).build(), "不朽经"),
    QINGDI_LONGEVITY("qingdi_longevity", Tier.IMMORTAL, QiElement.WOOD,
            Bonus.builder().elementSpellMult(QiElement.WOOD, 2.0).woodSpellCostHalve(true).build(), "青帝长生诀"),
    GHOST_DAO_BASIC("ghost_dao_basic", Tier.SUPREME, QiElement.PURE, Bonus.NONE, "鬼道基础");

    private final String id;
    private final Tier tier;
    private final QiElement primaryElement;
    private final Bonus bonus;
    private final String chineseName;

    Technique(String id, Tier tier, QiElement primaryElement, Bonus bonus, String chineseName) {
        this.id = id;
        this.tier = tier;
        this.primaryElement = primaryElement;
        this.bonus = bonus;
        this.chineseName = chineseName;
    }

    public String id() { return id; }
    public Tier tier() { return tier; }
    public QiElement primaryElement() { return primaryElement; }
    public Bonus bonus() { return bonus; }

    public DaoPath daoPath() {
        return this == GHOST_DAO_BASIC ? DaoPath.GHOST : DaoPath.HUMAN;
    }

    public boolean isGhostDao() { return daoPath() == DaoPath.GHOST; }
    public boolean isHumanDao() { return daoPath() == DaoPath.HUMAN; }

    public String daoPathTranslationKey() {
        return "technique_path.friday_cultivation." + this.daoPath().id();
    }

    public boolean isGhostOnly() {
        return this.isGhostDao();
    }

    public String translationKey() { return "technique.friday_cultivation." + id; }
    public String descriptionKey() { return "technique.friday_cultivation." + id + ".desc"; }

    public Component displayName() { return Component.translatableWithFallback(translationKey(), chineseName); }
    public Component description() { return Component.translatable(descriptionKey()); }

    public ResourceLocation iconTexture() {
        if (this == IMMORTAL_INCANTATION)
            return new ResourceLocation("friday_cultivation", "textures/gui/technique_immortal_body.png");
        return new ResourceLocation("friday_cultivation", "textures/gui/technique_" + id + ".png");
    }

    public static Technique byId(String id) {
        id = normalizeId(id);
        for (Technique t : values()) if (t.id.equals(id)) return t;
        return null;
    }

    public static String normalizeId(String id) {
        if (id == null || id.isEmpty()) return "";
        return "immortal_body".equals(id) ? IMMORTAL_INCANTATION.id : id;
    }

    // ═══════════════════════════════════════════
    // Tier 枚举
    // ═══════════════════════════════════════════
    public enum Tier {
        LOW, MID, HIGH, SUPREME, IMMORTAL;

        /** 品阶显示色 — 照搬原模组 ItemTier.rgb 语义（Tier 与 ItemTier 同序同位） */
        public int rgb() {
            return ItemTier.valueOf(name()).rgb();
        }

        /** 品阶显示名 — 照搬原模组 ItemTier.displayName 语义 */
        public Component displayName() {
            return ItemTier.valueOf(name()).displayName();
        }
    }

    // ═══════════════════════════════════════════
    // DaoPath 枚举
    // ═══════════════════════════════════════════
    public enum DaoPath {
        HUMAN("human_dao"),
        GHOST("ghost_dao");

        private final String id;
        DaoPath(String id) { this.id = id; }
        public String id() { return id; }
    }

    // ═══════════════════════════════════════════
    // Bonus 加成数据类 + Builder
    // ═══════════════════════════════════════════
    public static final class Bonus {
        public static final Bonus NONE = new Builder().build();

        public final int attack, defense, critRate, maxHp, qiAbsorbRange, refining, alchemy,
                autoRegenPerMinute, undeadBonusDamage;
        public final double qiAbsorbMult, moveSpeed, fallDamageReduce;
        public final double[] elementSpellMult;
        public final boolean nightVision, waterBreathing, fireResistance, knockbackResist,
                blockAllPotions, fireImmune, qiCostHalve, woodSpellCostHalve;
        public final double woodSpellCostMult;
        public final boolean photosynthesis, forestShelter, damageHalve;
        public final double alchemyTierUpChance, refiningTierUpChance;

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
            this.elementSpellMult = b.elementSpellMult.clone();
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
            if (el == null) return 1.0;
            return elementSpellMult[el.ordinal()];
        }

        /** 根包 QiElement 重载 — 照搬原模组 spellMultFor 语义（两包 QiElement 常量同序） */
        public double spellMultFor(com.friday.cultivation.QiElement el) {
            if (el == null) return 1.0;
            return spellMultFor(QiElement.values()[el.ordinal()]);
        }

        public static Builder builder() { return new Builder(); }

        public static final class Builder {
            int attack = 0, defense = 0, critRate = 0, maxHp = 0, qiAbsorbRange = 0,
                refining = 0, alchemy = 0, autoRegenPerMinute = 0, undeadBonusDamage = 0;
            double qiAbsorbMult = 1.0, moveSpeed = 0.0, fallDamageReduce = 0.0;
            final double[] elementSpellMult = new double[QiElement.values().length];
            boolean nightVision = false, waterBreathing = false, fireResistance = false,
                    knockbackResist = false, blockAllPotions = false, fireImmune = false,
                    qiCostHalve = false, woodSpellCostHalve = false;
            double woodSpellCostMult = 1.0;
            boolean photosynthesis = false, forestShelter = false, damageHalve = false;
            double alchemyTierUpChance = 0.0, refiningTierUpChance = 0.0;

            Builder() {
                for (int i = 0; i < elementSpellMult.length; i++) elementSpellMult[i] = 1.0;
            }

            public Builder attack(int v) { attack = v; return this; }
            public Builder defense(int v) { defense = v; return this; }
            public Builder critRate(int v) { critRate = v; return this; }
            public Builder maxHp(int v) { maxHp = v; return this; }
            public Builder qiAbsorbRange(int v) { qiAbsorbRange = v; return this; }
            public Builder refining(int v) { refining = v; return this; }
            public Builder alchemy(int v) { alchemy = v; return this; }
            public Builder autoRegenPerMinute(int v) { autoRegenPerMinute = v; return this; }
            public Builder undeadBonusDamage(int v) { undeadBonusDamage = v; return this; }
            public Builder qiAbsorbMult(double v) { qiAbsorbMult = v; return this; }
            public Builder moveSpeed(double v) { moveSpeed = v; return this; }
            public Builder fallDamageReduce(double v) { fallDamageReduce = v; return this; }
            public Builder elementSpellMult(QiElement el, double v) { elementSpellMult[el.ordinal()] = v; return this; }
            public Builder nightVision(boolean v) { nightVision = v; return this; }
            public Builder waterBreathing(boolean v) { waterBreathing = v; return this; }
            public Builder fireResistance(boolean v) { fireResistance = v; return this; }
            public Builder knockbackResist(boolean v) { knockbackResist = v; return this; }
            public Builder blockAllPotions(boolean v) { blockAllPotions = v; return this; }
            public Builder fireImmune(boolean v) { fireImmune = v; return this; }
            public Builder qiCostHalve(boolean v) { qiCostHalve = v; return this; }
            public Builder woodSpellCostHalve(boolean v) { woodSpellCostHalve = v; return this; }
            public Builder woodSpellCostMult(double v) { woodSpellCostMult = v; return this; }
            public Builder photosynthesis(boolean v) { photosynthesis = v; return this; }
            public Builder forestShelter(boolean v) { forestShelter = v; return this; }
            public Builder damageHalve(boolean v) { damageHalve = v; return this; }
            public Builder alchemyTierUpChance(double v) { alchemyTierUpChance = v; return this; }
            public Builder refiningTierUpChance(double v) { refiningTierUpChance = v; return this; }
            public Bonus build() { return new Bonus(this); }
        }
    }
}
