package com.friday.cultivation.spell;

import com.friday.cultivation.ItemTier;
import com.friday.cultivation.realm.Realm;
import com.friday.cultivation.util.TooltipUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.List;

/**
 * 法术系统 — 58种法术
 * 复刻自原模组 com.xiaoxiang.cultivation.cultivation.spell.Spell
 * 属性: id / type / element / tier / damage / qiCost
 */
public enum Spell {
    // ── 被动法术 ──
    QI_SHIELD("qi_shield", SpellType.PASSIVE, SpellElement.NONE, ItemTier.LOW, 0, 0, "灵气护盾"),
    SPIRIT_VISION("spirit_vision", SpellType.PASSIVE, SpellElement.NONE, ItemTier.LOW, 0, 0, "灵气视野"),
    YIN_YANG_EYE("yin_yang_eye", SpellType.PASSIVE, SpellElement.NONE, ItemTier.MID, 0, 0, "阴阳眼"),
    SLOW_REGEN("slow_regen", SpellType.PASSIVE, SpellElement.NONE, ItemTier.HIGH, 0, 0, "灵气自愈"),
    POISON_IMMUNITY("poison_immunity", SpellType.PASSIVE, SpellElement.EARTH, ItemTier.HIGH, 0, 0, "御毒体质"),
    FROST_WALKER("frost_walker", SpellType.PASSIVE, SpellElement.ICE, ItemTier.LOW, 0, 0, "寒冰筑路"),
    SWORD_AURA("sword_aura", SpellType.PASSIVE, SpellElement.METAL, ItemTier.SUPREME, 100, 0, "剑气"),
    CLEAR_MIND_INCANTATION("clear_mind_incantation", SpellType.PASSIVE, SpellElement.NONE, ItemTier.SUPREME, 0, 0, "清心神咒"),
    IMMORTAL_INCANTATION("immortal_incantation", SpellType.PASSIVE, SpellElement.NONE, ItemTier.IMMORTAL, 0, 0, "无垢仙诀"),
    QI_FLIGHT("qi_flight", SpellType.PASSIVE, SpellElement.NONE, ItemTier.SUPREME, 0, 0, "灵气飞行"),
    GHOST_FLIGHT("ghost_flight", SpellType.PASSIVE, SpellElement.NONE, ItemTier.LOW, 0, 0, "幽灵飞行"),
    SOUL_MARK("soul_mark", SpellType.PASSIVE, SpellElement.NONE, ItemTier.HIGH, 0, 0, "追魂印"),
    BLOODTHIRST_CURSE("bloodthirst_curse", SpellType.PASSIVE, SpellElement.NONE, ItemTier.SUPREME, 0, 0, "嗜血咒"),
    BIGU("bigu", SpellType.PASSIVE, SpellElement.NONE, ItemTier.LOW, 0, 0, "辟谷"),
    QI_MENDING("qi_mending", SpellType.PASSIVE, SpellElement.NONE, ItemTier.HIGH, 0, 0, "灵息养器"),
    DHARMA_BODY_MANIFESTATION("dharma_body_manifestation", SpellType.PASSIVE, SpellElement.NONE, ItemTier.SUPREME, 0, 0, "法身显化"),
    VOID_STEP("void_step", SpellType.PASSIVE, SpellElement.NONE, ItemTier.SUPREME, 0, 0, "虚空踏步"),

    // ── 主动法术 ──
    FIREBALL("fireball", SpellType.ACTIVE, SpellElement.FIRE, ItemTier.LOW, 10, 10, "火球术"),
    GREAT_FIREBALL("great_fireball", SpellType.ACTIVE, SpellElement.FIRE, ItemTier.SUPREME, 30, 100, "大火球术"),
    SWORD_CONVERGENCE("sword_convergence", SpellType.ACTIVE, SpellElement.METAL, ItemTier.SUPREME, 6, 100, "万剑归宗"),
    STAR_FALL("star_fall", SpellType.ACTIVE, SpellElement.FIRE, ItemTier.IMMORTAL, 50, 1000, "天星坠"),
    ICE_LANCE("ice_lance", SpellType.ACTIVE, SpellElement.ICE, ItemTier.LOW, 8, 12, "寒冰箭"),
    LIGHTNING_BOLT("lightning_bolt", SpellType.ACTIVE, SpellElement.LIGHTNING, ItemTier.MID, 0, 30, "雷霆术"),
    PALM_THUNDER("palm_thunder", SpellType.ACTIVE, SpellElement.LIGHTNING, ItemTier.SUPREME, 30, 100, "掌心雷"),
    WIND_BLADE("wind_blade", SpellType.ACTIVE, SpellElement.WOOD, ItemTier.LOW, 6, 8, "风刃斩"),
    POISON_MIST("poison_mist", SpellType.ACTIVE, SpellElement.EARTH, ItemTier.LOW, 0, 15, "毒雾术"),
    WITHER_TOUCH("wither_touch", SpellType.ACTIVE, SpellElement.NONE, ItemTier.LOW, 0, 25, "凋零之触"),
    ARROW_VOLLEY("arrow_volley", SpellType.ACTIVE, SpellElement.METAL, ItemTier.LOW, 0, 12, "飞矢三叠"),
    EARTH_SPIKE("earth_spike", SpellType.ACTIVE, SpellElement.EARTH, ItemTier.MID, 0, 20, "起爆术"),
    STONE_BULLET("stone_bullet", SpellType.ACTIVE, SpellElement.EARTH, ItemTier.MID, 20, 10, "石弹术"),
    HEAVEN_PIERCING_CONE("heaven_piercing_cone", SpellType.ACTIVE, SpellElement.EARTH, ItemTier.SUPREME, 60, 100, "穿天锥"),
    SUN_FLARE("sun_flare", SpellType.ACTIVE, SpellElement.FIRE, ItemTier.LOW, 0, 10, "烈日闪焰"),
    SHADOW_STEP("shadow_step", SpellType.ACTIVE, SpellElement.NONE, ItemTier.MID, 0, 20, "缩地术"),
    SOARING("soaring", SpellType.ACTIVE, SpellElement.WOOD, ItemTier.LOW, 0, 15, "御风术"),
    SPEED_BURST("speed_burst", SpellType.ACTIVE, SpellElement.WOOD, ItemTier.LOW, 0, 8, "疾风步"),
    INVISIBILITY("invisibility", SpellType.ACTIVE, SpellElement.NONE, ItemTier.MID, 0, 18, "隐遁术"),
    HEALING_TOUCH("healing_touch", SpellType.ACTIVE, SpellElement.WOOD, ItemTier.LOW, 0, 15, "回春术"),
    IRON_BODY("iron_body", SpellType.ACTIVE, SpellElement.METAL, ItemTier.LOW, 0, 12, "金钟罩"),
    WATER_AFFINITY("water_affinity", SpellType.ACTIVE, SpellElement.WATER, ItemTier.LOW, 0, 6, "水中行"),
    NIGHT_EYE("night_eye", SpellType.ACTIVE, SpellElement.NONE, ItemTier.LOW, 0, 5, "夜视术"),
    FIRE_PROTECTION("fire_protection", SpellType.ACTIVE, SpellElement.FIRE, ItemTier.LOW, 0, 8, "火体不侵"),
    SKY_SPLITTING_SWORD_AURA("sky_splitting_sword_aura", SpellType.ACTIVE, SpellElement.METAL, ItemTier.IMMORTAL, 1000, 1000, "裂天剑气"),
    FLYING_SWORD("flying_sword", SpellType.ACTIVE, SpellElement.METAL, ItemTier.MID, 20, 15, "飞剑术"),
    CLEAR_MIND("clear_mind", SpellType.ACTIVE, SpellElement.NONE, ItemTier.LOW, 0, 50, "清心术"),
    QI_TRANSFER("qi_transfer", SpellType.ACTIVE, SpellElement.NONE, ItemTier.LOW, 0, 5, "灵气传输"),
    TRUTH_SIGHT_EYE("truth_sight_eye", SpellType.ACTIVE, SpellElement.NONE, ItemTier.MID, 0, 50, "破妄法眼"),
    TIME_STASIS("time_stasis", SpellType.ACTIVE, SpellElement.NONE, ItemTier.IMMORTAL, 0, 1000, "时间停滞"),
    TAISHANG_LIFE_BALANCE("taishang_life_balance", SpellType.ACTIVE, SpellElement.NONE, ItemTier.IMMORTAL, 0, 1000, "太上均命"),
    BUDDHA_FIRE_LOTUS("buddha_fire_lotus", SpellType.ACTIVE, SpellElement.WOOD_FIRE, ItemTier.IMMORTAL, 1000, 1000, "佛怒火莲"),
    SPIRIT_LOCK("spirit_lock", SpellType.ACTIVE, SpellElement.NONE, ItemTier.SUPREME, 0, 500, "锁灵术"),
    SPIRIT_UNLOCK("spirit_unlock", SpellType.ACTIVE, SpellElement.NONE, ItemTier.SUPREME, 0, 100, "解灵术"),
    SWORD_FLIGHT("sword_flight", SpellType.ACTIVE, SpellElement.NONE, ItemTier.MID, 0, 0, "御剑飞行"),
    CORE_SELF_DESTRUCT("core_self_destruct", SpellType.ACTIVE, SpellElement.NONE, ItemTier.MID, 120, 0, "金丹自爆"),
    NASCENT_SOUL_OUT_OF_BODY("nascent_soul_out_of_body", SpellType.ACTIVE, SpellElement.NONE, ItemTier.HIGH, 0, 500, "元婴出窍"),
    DIVINE_SENSE("divine_sense", SpellType.ACTIVE, SpellElement.NONE, ItemTier.SUPREME, 0, 100, "神识外放"),
    VOID_ESCAPE("void_escape", SpellType.ACTIVE, SpellElement.NONE, ItemTier.SUPREME, 0, 0, "虚遁"),
    REALM_PRESSURE("realm_pressure", SpellType.ACTIVE, SpellElement.NONE, ItemTier.SUPREME, 0, 0, "境界威压"),
    SOUL_HOOK("soul_hook", SpellType.ACTIVE, SpellElement.NONE, ItemTier.SUPREME, 0, 200, "勾魂");

    public static final int BUDDHA_FIRE_LOTUS_READY_QI = 10000;
    public static final int CORE_SELF_DESTRUCT_READY_QI = 1000;
    public static final int SWORD_FLIGHT_UPKEEP_QI_PER_SECOND = 20;
    public static final int VOID_STEP_AIR_JUMP_QI_COST = 15;
    public static final int VOID_STEP_DASH_QI_COST = 60;
    public static final int VOID_STEP_SLOW_FALL_QI_COST = 30;
    public static final int PALM_THUNDER_CHANNEL_QI_PER_SECOND = 50;
    public static final int PALM_THUNDER_ARMING_TICKS = 40;
    public static final int VOID_ESCAPE_CHARGE_QI_PER_TICK = 10;
    public static final int VOID_ESCAPE_CHARGE_TICKS = 100;
    public static final int VOID_ESCAPE_ACTIVE_QI_PER_TICK = 5;
    public static final int VOID_ESCAPE_INITIAL_STABILITY = 10;

    private final String id;
    private final SpellType type;
    private final SpellElement element;
    private final ItemTier tier;
    private final int damage;
    private final int qiCost;
    private final String chineseName;

    Spell(String id, SpellType type, SpellElement element, ItemTier tier, int damage, int qiCost, String chineseName) {
        this.id = id;
        this.type = type;
        this.element = element;
        this.tier = tier;
        this.damage = damage;
        this.qiCost = qiCost;
        this.chineseName = chineseName;
    }

    public String id() { return id; }
    public SpellType type() { return type; }
    public SpellElement element() { return element; }
    public ItemTier tier() { return tier; }
    public int damage() { return damage; }
    public int qiCost() { return qiCost; }

    /** 是否为被动法术（被动法术学会后时刻生效，不可装备到使用槽位） */
    public boolean isPassive() {
        return type == SpellType.PASSIVE;
    }

    /** 需要蓄力的法术 */
    public boolean chargeable() {
        return this == GREAT_FIREBALL || this == SWORD_CONVERGENCE || this == STAR_FALL
                || this == SKY_SPLITTING_SWORD_AURA || this == HEAVEN_PIERCING_CONE
                || this == QI_TRANSFER || this == TIME_STASIS || this == TAISHANG_LIFE_BALANCE
                || this == PALM_THUNDER || this == BUDDHA_FIRE_LOTUS || this == CORE_SELF_DESTRUCT
                || this == REALM_PRESSURE || this == VOID_ESCAPE;
    }

    public boolean isSwordSpell() {
        return this == FLYING_SWORD || this == SWORD_FLIGHT || this == SWORD_CONVERGENCE
                || this == SWORD_AURA || this == SKY_SPLITTING_SWORD_AURA;
    }

    public boolean isBloodSpell() {
        return this == BLOODTHIRST_CURSE || this == TAISHANG_LIFE_BALANCE;
    }

    public Component displayName() {
        return Component.translatableWithFallback(translationKey(), chineseName);
    }

    /** 金丹自爆在特定境界有专属名称 */
    public Component displayNameForRealm(Realm realm) {
        if (this == CORE_SELF_DESTRUCT && realm != null) {
            String key = switch (realm) {
                case NASCENT_SOUL -> "nascent_soul_self_destruct";
                case SOUL_FORMATION -> "soul_formation_self_destruct";
                case VOID_REFINING -> "void_refining_self_destruct";
                case BODY_INTEGRATION -> "body_integration_self_destruct";
                case MAHAYANA -> "mahayana_self_destruct";
                case TRIBULATION_TRANSCENDENCE -> "tribulation_self_destruct";
                case TRUE_IMMORTAL -> "true_immortal_self_destruct";
                case LOOSE_IMMORTAL -> "loose_immortal_self_destruct";
                default -> null;
            };
            if (key != null) {
                return Component.translatableWithFallback("spell.friday_cultivation." + key, chineseName);
            }
        }
        return displayName();
    }

    public Component description() {
        return Component.translatableWithFallback(translationKey() + ".desc", chineseName);
    }

    public ResourceLocation iconTexture() {
        return new ResourceLocation("friday_cultivation", "textures/gui/spell_" + id + ".png");
    }

    public int iconTextureSize() { return 32; }

    public String translationKey() { return "spell.friday_cultivation." + id; }

    /** 法术tooltip行（严格照搬原模组 Spell.tooltipLines） */
    public List<Component> tooltipLines(boolean enabledIfPassive) {
        List<Component> lines = new ArrayList<>();
        lines.add(TooltipUtils.tieredName(this.displayName(), this.tier));
        MutableComponent typeLabel = this.type.displayName().copy().withStyle(ChatFormatting.GRAY);
        lines.add(TooltipUtils.tierElementLine(this.tier, this.element)
                .append(Component.literal("  |  ").withStyle(ChatFormatting.DARK_GRAY))
                .append(typeLabel));
        if (this.isSwordSpell()) {
            lines.add(TooltipUtils.effectLine(Component.translatable("spell.friday_cultivation.tag.sword_spell")));
        }
        if (this.isBloodSpell()) {
            lines.add(TooltipUtils.effectLine(Component.translatable("spell.friday_cultivation.tag.blood_spell")));
        }
        TooltipUtils.addBlank(lines);
        TooltipUtils.addSection(lines, "tooltip.friday_cultivation.section.effect");
        lines.add(TooltipUtils.descriptionLine(this.description()));
        this.appendExtraEffectTooltip(lines);
        TooltipUtils.addSection(lines, "tooltip.friday_cultivation.section.cast");
        this.appendCastTooltip(lines);
        TooltipUtils.addSection(lines, this.type == SpellType.PASSIVE ? "tooltip.friday_cultivation.section.status" : "tooltip.friday_cultivation.section.stats");
        if (this.type == SpellType.PASSIVE) {
            lines.add(TooltipUtils.statsLine(Component.translatable(enabledIfPassive ? "screen.friday_cultivation.spell.status.enabled" : "screen.friday_cultivation.spell.status.disabled")));
        } else {
            lines.add(TooltipUtils.statsLine(this == BUDDHA_FIRE_LOTUS ? Component.translatable("spell.friday_cultivation.cost.charge_100", this.damage, 10000)
                    : this == CORE_SELF_DESTRUCT ? Component.translatable("spell.friday_cultivation.core_self_destruct.cost", 1000)
                    : this == SWORD_FLIGHT ? Component.translatable("spell.friday_cultivation.sword_flight.cost", 20)
                    : this == PALM_THUNDER ? Component.translatable("spell.friday_cultivation.palm_thunder.cost", this.damage, this.qiCost, 50)
                    : this == REALM_PRESSURE ? Component.translatable("spell.friday_cultivation.realm_pressure.cost")
                    : Component.translatable("spell.friday_cultivation.cost", this.damage, this.qiCost)));
        }
        return lines;
    }

    /** 额外效果tooltip（照搬原模组 Spell.appendExtraEffectTooltip） */
    public void appendExtraEffectTooltip(List<Component> lines) {
        if (this == QI_SHIELD) {
            TooltipUtils.addSection(lines, "tooltip.friday_cultivation.section.stats");
            lines.add(TooltipUtils.costLine(Component.translatable("spell.friday_cultivation.qi_shield.qi_cost_line")));
            for (Realm realm : Realm.values()) {
                int percent = realm.qiShieldReductionPercent();
                if (percent <= 0) continue;
                lines.add(TooltipUtils.statsLine(Component.translatable("spell.friday_cultivation.qi_shield.reduction_line", realm.displayName(), percent)));
            }
            lines.add(TooltipUtils.warningLine(Component.translatable("spell.friday_cultivation.qi_shield.tribulation_exception")));
            return;
        }
        if (this == QI_MENDING) {
            TooltipUtils.addSection(lines, "tooltip.friday_cultivation.section.stats");
            lines.add(TooltipUtils.costLine(Component.translatable("spell.friday_cultivation.qi_mending.cost_line")));
        }
    }

    /** 施法方式tooltip（照搬原模组 Spell.appendCastTooltip） */
    public void appendCastTooltip(List<Component> lines) {
        if (this.type == SpellType.PASSIVE) {
            lines.add(TooltipUtils.positiveLine(Component.translatable("spell.friday_cultivation.cast.passive")));
            return;
        }
        if (this == BUDDHA_FIRE_LOTUS || this == CORE_SELF_DESTRUCT) {
            lines.add(TooltipUtils.effectLine(Component.translatable("spell.friday_cultivation.cast.hold_required")));
            return;
        }
        if (this == PALM_THUNDER) {
            lines.add(TooltipUtils.statsLine(Component.translatable("spell.friday_cultivation.cast.tap")));
            lines.add(TooltipUtils.effectLine(Component.translatable("spell.friday_cultivation.palm_thunder.cast_hold")));
            return;
        }
        if (this == REALM_PRESSURE) {
            lines.add(TooltipUtils.statsLine(Component.translatable("spell.friday_cultivation.realm_pressure.cast_tap")));
            lines.add(TooltipUtils.effectLine(Component.translatable("spell.friday_cultivation.realm_pressure.cast_hold")));
            lines.add(TooltipUtils.effectLine(Component.translatable("spell.friday_cultivation.realm_pressure.cast_passive")));
            return;
        }
        lines.add(TooltipUtils.statsLine(Component.translatable("spell.friday_cultivation.cast.tap")));
        if (this.chargeable()) {
            lines.add(TooltipUtils.effectLine(Component.translatable("spell.friday_cultivation.cast.hold")));
        }
    }

    public static Spell byId(String id) {
        for (Spell s : values()) if (s.id.equals(id)) return s;
        return null;
    }
}
