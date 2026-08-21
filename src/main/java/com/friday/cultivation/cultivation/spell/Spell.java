/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.ChatFormatting
 *  net.minecraft.network.chat.Component
 *  net.minecraft.network.chat.MutableComponent
 *  net.minecraft.resources.ResourceLocation
 */
package com.friday.cultivation.cultivation.spell;

import com.friday.cultivation.cultivation.ItemTier;
import com.friday.cultivation.cultivation.realm.Realm;
import com.friday.cultivation.cultivation.realm.RealmTopology;
import com.friday.cultivation.cultivation.spell.SpellElement;
import com.friday.cultivation.cultivation.spell.SpellType;
import com.friday.cultivation.util.TooltipUtils;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;

public enum Spell {
    QI_SHIELD("qi_shield", SpellType.PASSIVE, SpellElement.NONE, ItemTier.LOW, 0, 0),
    SPIRIT_VISION("spirit_vision", SpellType.PASSIVE, SpellElement.NONE, ItemTier.LOW, 0, 0),
    YIN_YANG_EYE("yin_yang_eye", SpellType.PASSIVE, SpellElement.NONE, ItemTier.MID, 0, 0),
    FIREBALL("fireball", SpellType.ACTIVE, SpellElement.FIRE, ItemTier.LOW, 10, 10),
    GREAT_FIREBALL("great_fireball", SpellType.ACTIVE, SpellElement.FIRE, ItemTier.SUPREME, 30, 100),
    SWORD_CONVERGENCE("sword_convergence", SpellType.ACTIVE, SpellElement.METAL, ItemTier.SUPREME, 6, 100),
    STAR_FALL("star_fall", SpellType.ACTIVE, SpellElement.FIRE, ItemTier.IMMORTAL, 50, 1000),
    ICE_LANCE("ice_lance", SpellType.ACTIVE, SpellElement.ICE, ItemTier.LOW, 8, 12),
    LIGHTNING_BOLT("lightning_bolt", SpellType.ACTIVE, SpellElement.LIGHTNING, ItemTier.MID, 0, 30),
    PALM_THUNDER("palm_thunder", SpellType.ACTIVE, SpellElement.LIGHTNING, ItemTier.SUPREME, 30, 100),
    WIND_BLADE("wind_blade", SpellType.ACTIVE, SpellElement.WOOD, ItemTier.LOW, 6, 8),
    POISON_MIST("poison_mist", SpellType.ACTIVE, SpellElement.EARTH, ItemTier.LOW, 0, 15),
    WITHER_TOUCH("wither_touch", SpellType.ACTIVE, SpellElement.NONE, ItemTier.LOW, 0, 25),
    ARROW_VOLLEY("arrow_volley", SpellType.ACTIVE, SpellElement.METAL, ItemTier.LOW, 0, 12),
    EARTH_SPIKE("earth_spike", SpellType.ACTIVE, SpellElement.EARTH, ItemTier.MID, 0, 20),
    STONE_BULLET("stone_bullet", SpellType.ACTIVE, SpellElement.EARTH, ItemTier.MID, 20, 10),
    HEAVEN_PIERCING_CONE("heaven_piercing_cone", SpellType.ACTIVE, SpellElement.EARTH, ItemTier.SUPREME, 60, 100),
    SUN_FLARE("sun_flare", SpellType.ACTIVE, SpellElement.FIRE, ItemTier.LOW, 0, 10),
    SHADOW_STEP("shadow_step", SpellType.ACTIVE, SpellElement.NONE, ItemTier.MID, 0, 20),
    SOARING("soaring", SpellType.ACTIVE, SpellElement.WOOD, ItemTier.LOW, 0, 15),
    SPEED_BURST("speed_burst", SpellType.ACTIVE, SpellElement.WOOD, ItemTier.LOW, 0, 8),
    INVISIBILITY("invisibility", SpellType.ACTIVE, SpellElement.NONE, ItemTier.MID, 0, 18),
    HEALING_TOUCH("healing_touch", SpellType.ACTIVE, SpellElement.WOOD, ItemTier.LOW, 0, 15),
    IRON_BODY("iron_body", SpellType.ACTIVE, SpellElement.METAL, ItemTier.LOW, 0, 12),
    WATER_AFFINITY("water_affinity", SpellType.ACTIVE, SpellElement.WATER, ItemTier.LOW, 0, 6),
    NIGHT_EYE("night_eye", SpellType.ACTIVE, SpellElement.NONE, ItemTier.LOW, 0, 5),
    FIRE_PROTECTION("fire_protection", SpellType.ACTIVE, SpellElement.FIRE, ItemTier.LOW, 0, 8),
    SLOW_REGEN("slow_regen", SpellType.PASSIVE, SpellElement.NONE, ItemTier.HIGH, 0, 0),
    POISON_IMMUNITY("poison_immunity", SpellType.PASSIVE, SpellElement.EARTH, ItemTier.HIGH, 0, 0),
    FROST_WALKER("frost_walker", SpellType.PASSIVE, SpellElement.ICE, ItemTier.LOW, 0, 0),
    SWORD_AURA("sword_aura", SpellType.PASSIVE, SpellElement.METAL, ItemTier.SUPREME, 100, 0),
    SKY_SPLITTING_SWORD_AURA("sky_splitting_sword_aura", SpellType.ACTIVE, SpellElement.METAL, ItemTier.IMMORTAL, 1000, 1000),
    FLYING_SWORD("flying_sword", SpellType.ACTIVE, SpellElement.METAL, ItemTier.MID, 20, 15),
    CLEAR_MIND("clear_mind", SpellType.ACTIVE, SpellElement.NONE, ItemTier.LOW, 0, 50),
    CLEAR_MIND_INCANTATION("clear_mind_incantation", SpellType.PASSIVE, SpellElement.NONE, ItemTier.SUPREME, 0, 0),
    IMMORTAL_INCANTATION("immortal_incantation", SpellType.PASSIVE, SpellElement.NONE, ItemTier.IMMORTAL, 0, 0),
    QI_FLIGHT("qi_flight", SpellType.PASSIVE, SpellElement.NONE, ItemTier.SUPREME, 0, 0),
    GHOST_FLIGHT("ghost_flight", SpellType.PASSIVE, SpellElement.NONE, ItemTier.LOW, 0, 0),
    QI_TRANSFER("qi_transfer", SpellType.ACTIVE, SpellElement.NONE, ItemTier.LOW, 0, 5),
    TRUTH_SIGHT_EYE("truth_sight_eye", SpellType.ACTIVE, SpellElement.NONE, ItemTier.MID, 0, 50),
    TIME_STASIS("time_stasis", SpellType.ACTIVE, SpellElement.NONE, ItemTier.IMMORTAL, 0, 1000),
    TAISHANG_LIFE_BALANCE("taishang_life_balance", SpellType.ACTIVE, SpellElement.NONE, ItemTier.IMMORTAL, 0, 1000),
    BUDDHA_FIRE_LOTUS("buddha_fire_lotus", SpellType.ACTIVE, SpellElement.WOOD_FIRE, ItemTier.IMMORTAL, 1000, 1000),
    SPIRIT_LOCK("spirit_lock", SpellType.ACTIVE, SpellElement.NONE, ItemTier.SUPREME, 0, 500),
    SPIRIT_UNLOCK("spirit_unlock", SpellType.ACTIVE, SpellElement.NONE, ItemTier.SUPREME, 0, 100),
    SOUL_MARK("soul_mark", SpellType.PASSIVE, SpellElement.NONE, ItemTier.HIGH, 0, 0),
    BLOODTHIRST_CURSE("bloodthirst_curse", SpellType.PASSIVE, SpellElement.NONE, ItemTier.SUPREME, 0, 0),
    SWORD_FLIGHT("sword_flight", SpellType.ACTIVE, SpellElement.NONE, ItemTier.MID, 0, 0),
    BIGU("bigu", SpellType.PASSIVE, SpellElement.NONE, ItemTier.LOW, 0, 0),
    QI_MENDING("qi_mending", SpellType.PASSIVE, SpellElement.NONE, ItemTier.HIGH, 0, 0),
    CORE_SELF_DESTRUCT("core_self_destruct", SpellType.ACTIVE, SpellElement.NONE, ItemTier.MID, 120, 0),
    NASCENT_SOUL_OUT_OF_BODY("nascent_soul_out_of_body", SpellType.ACTIVE, SpellElement.NONE, ItemTier.HIGH, 0, 500),
    DIVINE_SENSE("divine_sense", SpellType.ACTIVE, SpellElement.NONE, ItemTier.SUPREME, 0, 100),
    DHARMA_BODY_MANIFESTATION("dharma_body_manifestation", SpellType.PASSIVE, SpellElement.NONE, ItemTier.SUPREME, 0, 0),
    VOID_ESCAPE("void_escape", SpellType.ACTIVE, SpellElement.NONE, ItemTier.SUPREME, 0, 0),
    VOID_STEP("void_step", SpellType.PASSIVE, SpellElement.NONE, ItemTier.SUPREME, 0, 0),
    REALM_PRESSURE("realm_pressure", SpellType.ACTIVE, SpellElement.NONE, ItemTier.SUPREME, 0, 0),
    SOUL_HOOK("soul_hook", SpellType.ACTIVE, SpellElement.NONE, ItemTier.SUPREME, 0, 200);

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

    private Spell(String id, SpellType type, SpellElement element, ItemTier tier, int damage, int qiCost) {
        this.id = id;
        this.type = type;
        this.element = element;
        this.tier = tier;
        this.damage = damage;
        this.qiCost = qiCost;
    }

    public String id() {
        return this.id;
    }

    public SpellType type() {
        return this.type;
    }

    public SpellElement element() {
        return this.element;
    }

    public ItemTier tier() {
        return this.tier;
    }

    public int damage() {
        return this.damage;
    }

    public int qiCost() {
        return this.qiCost;
    }

    public boolean chargeable() {
        return this == GREAT_FIREBALL || this == SWORD_CONVERGENCE || this == STAR_FALL || this == SKY_SPLITTING_SWORD_AURA || this == HEAVEN_PIERCING_CONE || this == QI_TRANSFER || this == TIME_STASIS || this == TAISHANG_LIFE_BALANCE || this == PALM_THUNDER || this == BUDDHA_FIRE_LOTUS || this == CORE_SELF_DESTRUCT || this == REALM_PRESSURE || this == VOID_ESCAPE;
    }

    public boolean isSwordSpell() {
        return this == FLYING_SWORD || this == SWORD_FLIGHT || this == SWORD_CONVERGENCE || this == SWORD_AURA || this == SKY_SPLITTING_SWORD_AURA;
    }

    public boolean isBloodSpell() {
        return this == BLOODTHIRST_CURSE || this == TAISHANG_LIFE_BALANCE;
    }

    public Component displayName() {
        return Component.translatable((String)("spell.friday_cultivation." + this.id));
    }

    public Component displayNameForRealm(Realm realm) {
        if (this == CORE_SELF_DESTRUCT && realm != null) {
            String key;
            switch (realm) {
                case NASCENT_SOUL: {
                    key = "nascent_soul_self_destruct";
                    break;
                }
                case SOUL_FORMATION: {
                    key = "soul_formation_self_destruct";
                    break;
                }
                case VOID_REFINING: {
                    key = "void_refining_self_destruct";
                    break;
                }
                case BODY_INTEGRATION: {
                    key = "body_integration_self_destruct";
                    break;
                }
                case MAHAYANA: {
                    key = "mahayana_self_destruct";
                    break;
                }
                case TRIBULATION_TRANSCENDENCE: {
                    key = "tribulation_self_destruct";
                    break;
                }
                case TRUE_IMMORTAL: {
                    key = "true_immortal_self_destruct";
                    break;
                }
                case GREAT_EMPEROR: {
                    key = "great_emperor_self_destruct";
                    break;
                }
                case LOOSE_IMMORTAL: {
                    key = "loose_immortal_self_destruct";
                    break;
                }
                default: {
                    key = null;
                }
            }
            if (key != null) {
                return Component.translatable((String)("spell.friday_cultivation." + key));
            }
        }
        return this.displayName();
    }

    public Component description() {
        return Component.translatable((String)("spell.friday_cultivation." + this.id + ".desc"));
    }

    public ResourceLocation iconTexture() {
        return new ResourceLocation("friday_cultivation", "textures/gui/spell_" + this.id + ".png");
    }

    public int iconTextureSize() {
        return 32;
    }

    public List<Component> tooltipLines(boolean enabledIfPassive) {
        ArrayList<Component> lines = new ArrayList<Component>();
        lines.add((Component)TooltipUtils.tieredName(this.displayName(), this.tier));
        MutableComponent typeLabel = this.type.displayName().copy().withStyle(ChatFormatting.GRAY);
        lines.add((Component)TooltipUtils.tierElementLine(this.tier, this.element).append((Component)Component.literal((String)"  |  ").withStyle(ChatFormatting.DARK_GRAY)).append((Component)typeLabel));
        if (this.isSwordSpell()) {
            lines.add((Component)TooltipUtils.effectLine((Component)Component.translatable((String)"spell.friday_cultivation.tag.sword_spell")));
        }
        if (this.isBloodSpell()) {
            lines.add((Component)TooltipUtils.effectLine((Component)Component.translatable((String)"spell.friday_cultivation.tag.blood_spell")));
        }
        TooltipUtils.addBlank(lines);
        TooltipUtils.addSection(lines, "tooltip.friday_cultivation.section.effect");
        lines.add((Component)TooltipUtils.descriptionLine(this.description()));
        this.appendExtraEffectTooltip(lines);
        TooltipUtils.addSection(lines, "tooltip.friday_cultivation.section.cast");
        this.appendCastTooltip(lines);
        TooltipUtils.addSection(lines, this.type == SpellType.PASSIVE ? "tooltip.friday_cultivation.section.status" : "tooltip.friday_cultivation.section.stats");
        if (this.type == SpellType.PASSIVE) {
            lines.add((Component)TooltipUtils.statsLine((Component)Component.translatable((String)(enabledIfPassive ? "screen.friday_cultivation.spell.status.enabled" : "screen.friday_cultivation.spell.status.disabled"))));
        } else {
            lines.add((Component)TooltipUtils.statsLine((Component)(this == BUDDHA_FIRE_LOTUS ? Component.translatable((String)"spell.friday_cultivation.cost.charge_100", (Object[])new Object[]{this.damage, 10000}) : (this == CORE_SELF_DESTRUCT ? Component.translatable((String)"spell.friday_cultivation.core_self_destruct.cost", (Object[])new Object[]{1000}) : (this == SWORD_FLIGHT ? Component.translatable((String)"spell.friday_cultivation.sword_flight.cost", (Object[])new Object[]{20}) : (this == PALM_THUNDER ? Component.translatable((String)"spell.friday_cultivation.palm_thunder.cost", (Object[])new Object[]{this.damage, this.qiCost, 50}) : (this == REALM_PRESSURE ? Component.translatable((String)"spell.friday_cultivation.realm_pressure.cost") : Component.translatable((String)"spell.friday_cultivation.cost", (Object[])new Object[]{this.damage, this.qiCost}))))))));
        }
        return lines;
    }

    public void appendExtraEffectTooltip(List<Component> lines) {
        if (this == QI_SHIELD) {
            TooltipUtils.addSection(lines, "tooltip.friday_cultivation.section.stats");
            lines.add((Component)TooltipUtils.costLine((Component)Component.translatable((String)"spell.friday_cultivation.qi_shield.qi_cost_line")));
            for (Realm realm : RealmTopology.selectionOrder()) {
                int percent = realm.qiShieldReductionPercent();
                if (percent <= 0) continue;
                lines.add((Component)TooltipUtils.statsLine((Component)Component.translatable((String)"spell.friday_cultivation.qi_shield.reduction_line", (Object[])new Object[]{realm.displayName(), percent})));
            }
            lines.add((Component)TooltipUtils.warningLine((Component)Component.translatable((String)"spell.friday_cultivation.qi_shield.tribulation_exception")));
            return;
        }
        if (this == QI_MENDING) {
            TooltipUtils.addSection(lines, "tooltip.friday_cultivation.section.stats");
            lines.add((Component)TooltipUtils.costLine((Component)Component.translatable((String)"spell.friday_cultivation.qi_mending.cost_line")));
        }
    }

    public void appendCastTooltip(List<Component> lines) {
        if (this.type == SpellType.PASSIVE) {
            lines.add((Component)TooltipUtils.positiveLine((Component)Component.translatable((String)"spell.friday_cultivation.cast.passive")));
            return;
        }
        if (this == BUDDHA_FIRE_LOTUS || this == CORE_SELF_DESTRUCT) {
            lines.add((Component)TooltipUtils.effectLine((Component)Component.translatable((String)"spell.friday_cultivation.cast.hold_required")));
            return;
        }
        if (this == PALM_THUNDER) {
            lines.add((Component)TooltipUtils.statsLine((Component)Component.translatable((String)"spell.friday_cultivation.cast.tap")));
            lines.add((Component)TooltipUtils.effectLine((Component)Component.translatable((String)"spell.friday_cultivation.palm_thunder.cast_hold")));
            return;
        }
        if (this == REALM_PRESSURE) {
            lines.add((Component)TooltipUtils.statsLine((Component)Component.translatable((String)"spell.friday_cultivation.realm_pressure.cast_tap")));
            lines.add((Component)TooltipUtils.effectLine((Component)Component.translatable((String)"spell.friday_cultivation.realm_pressure.cast_hold")));
            lines.add((Component)TooltipUtils.effectLine((Component)Component.translatable((String)"spell.friday_cultivation.realm_pressure.cast_passive")));
            return;
        }
        lines.add((Component)TooltipUtils.statsLine((Component)Component.translatable((String)"spell.friday_cultivation.cast.tap")));
        if (this.chargeable()) {
            lines.add((Component)TooltipUtils.effectLine((Component)Component.translatable((String)"spell.friday_cultivation.cast.hold")));
        }
    }

    public static Spell byId(String id) {
        for (Spell s : Spell.values()) {
            if (!s.id.equals(id)) continue;
            return s;
        }
        return null;
    }
}

