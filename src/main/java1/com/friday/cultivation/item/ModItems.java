package com.friday.cultivation.item;

import com.friday.cultivation.FridayCultivationMod;
import com.friday.cultivation.ItemTier;
import com.friday.cultivation.alchemy.PillTier;
import com.friday.cultivation.qi.SpiritVeinCoreTier;
import com.friday.cultivation.realm.Realm;
import com.friday.cultivation.registry.ModBlocks;
import com.friday.cultivation.spell.Spell;
import com.friday.cultivation.item.weapon.ChiYanSwordItem;
import com.friday.cultivation.item.weapon.HanBingSwordItem;
import com.friday.cultivation.item.weapon.QingMuSwordItem;
import com.friday.cultivation.item.weapon.SoulHookItem;
import com.friday.cultivation.item.weapon.XuanIronSwordItem;
import com.friday.cultivation.technique.Technique;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

/**
 * 物品注册中心
 */
public final class ModItems {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, FridayCultivationMod.MOD_ID);

    // ── 功法书（27种） ──
    public static final RegistryObject<TechniqueBookItem> TECHNIQUE_BOOK_FRAGMENT            = registerTechniqueBook("technique_book_fragment", Technique.FRAGMENT);
    public static final RegistryObject<TechniqueBookItem> TECHNIQUE_BOOK_BASIC_BODY           = registerTechniqueBook("technique_book_basic_body", Technique.BASIC_BODY);
    public static final RegistryObject<TechniqueBookItem> TECHNIQUE_BOOK_BASIC_MIND           = registerTechniqueBook("technique_book_basic_mind", Technique.BASIC_MIND);
    public static final RegistryObject<TechniqueBookItem> TECHNIQUE_BOOK_IRON_SKIN            = registerTechniqueBook("technique_book_iron_skin", Technique.IRON_SKIN);
    public static final RegistryObject<TechniqueBookItem> TECHNIQUE_BOOK_METAL_SWORD          = registerTechniqueBook("technique_book_metal_sword", Technique.METAL_SWORD);
    public static final RegistryObject<TechniqueBookItem> TECHNIQUE_BOOK_WOOD_SPRING          = registerTechniqueBook("technique_book_wood_spring", Technique.WOOD_SPRING);
    public static final RegistryObject<TechniqueBookItem> TECHNIQUE_BOOK_DEADWOOD_REBIRTH     = registerTechniqueBook("technique_book_deadwood_rebirth", Technique.DEADWOOD_REBIRTH);
    public static final RegistryObject<TechniqueBookItem> TECHNIQUE_BOOK_WATER_STREAM         = registerTechniqueBook("technique_book_water_stream", Technique.WATER_STREAM);
    public static final RegistryObject<TechniqueBookItem> TECHNIQUE_BOOK_FIRE_YANG            = registerTechniqueBook("technique_book_fire_yang", Technique.FIRE_YANG);
    public static final RegistryObject<TechniqueBookItem> TECHNIQUE_BOOK_EARTH_MOUNTAIN       = registerTechniqueBook("technique_book_earth_mountain", Technique.EARTH_MOUNTAIN);
    public static final RegistryObject<TechniqueBookItem> TECHNIQUE_BOOK_ICE_FROST            = registerTechniqueBook("technique_book_ice_frost", Technique.ICE_FROST);
    public static final RegistryObject<TechniqueBookItem> TECHNIQUE_BOOK_WIND_STEP            = registerTechniqueBook("technique_book_wind_step", Technique.WIND_STEP);
    public static final RegistryObject<TechniqueBookItem> TECHNIQUE_BOOK_SHADOW_CLOAK         = registerTechniqueBook("technique_book_shadow_cloak", Technique.SHADOW_CLOAK);
    public static final RegistryObject<TechniqueBookItem> TECHNIQUE_BOOK_FIRE_IMMORTAL        = registerTechniqueBook("technique_book_fire_immortal", Technique.FIRE_IMMORTAL);
    public static final RegistryObject<TechniqueBookItem> TECHNIQUE_BOOK_VAJRA_BODY           = registerTechniqueBook("technique_book_vajra_body", Technique.VAJRA_BODY);
    public static final RegistryObject<TechniqueBookItem> TECHNIQUE_BOOK_HEART_SUTRA          = registerTechniqueBook("technique_book_heart_sutra", Technique.HEART_SUTRA);
    public static final RegistryObject<TechniqueBookItem> TECHNIQUE_BOOK_SWORD_HEART          = registerTechniqueBook("technique_book_sword_heart", Technique.SWORD_HEART);
    public static final RegistryObject<TechniqueBookItem> TECHNIQUE_BOOK_NINE_ABYSS           = registerTechniqueBook("technique_book_nine_abyss", Technique.NINE_ABYSS);
    public static final RegistryObject<TechniqueBookItem> TECHNIQUE_BOOK_DEMON_SLAYER         = registerTechniqueBook("technique_book_demon_slayer", Technique.DEMON_SLAYER);
    public static final RegistryObject<TechniqueBookItem> TECHNIQUE_BOOK_TURTLE_SHELL         = registerTechniqueBook("technique_book_turtle_shell", Technique.TURTLE_SHELL);
    public static final RegistryObject<TechniqueBookItem> TECHNIQUE_BOOK_DIVINE_FORGE         = registerTechniqueBook("technique_book_divine_forge", Technique.DIVINE_FORGE);
    public static final RegistryObject<TechniqueBookItem> TECHNIQUE_BOOK_HEAVENLY_ELIXIR      = registerTechniqueBook("technique_book_heavenly_elixir", Technique.HEAVENLY_ELIXIR);
    public static final RegistryObject<TechniqueBookItem> TECHNIQUE_BOOK_FIVE_ELEMENT         = registerTechniqueBook("technique_book_five_element", Technique.FIVE_ELEMENT);
    public static final RegistryObject<TechniqueBookItem> TECHNIQUE_BOOK_CELESTIAL_IMMORTAL   = registerTechniqueBook("technique_book_celestial_immortal", Technique.CELESTIAL_IMMORTAL);
    public static final RegistryObject<TechniqueBookItem> TECHNIQUE_BOOK_SKY_DEVOURING        = registerTechniqueBook("technique_book_sky_devouring", Technique.SKY_DEVOURING);
    public static final RegistryObject<TechniqueBookItem> TECHNIQUE_BOOK_FIVE_ELEMENT_CHAOS   = registerTechniqueBook("technique_book_five_element_chaos_art", Technique.FIVE_ELEMENT_CHAOS_ART);
    public static final RegistryObject<TechniqueBookItem> TECHNIQUE_BOOK_IMMORTAL_INCANTATION = registerTechniqueBook("technique_book_immortal_incantation", Technique.IMMORTAL_INCANTATION);
    public static final RegistryObject<TechniqueBookItem> TECHNIQUE_BOOK_QINGDI_LONGEVITY     = registerTechniqueBook("technique_book_qingdi_longevity", Technique.QINGDI_LONGEVITY);
    public static final RegistryObject<TechniqueBookItem> TECHNIQUE_BOOK_GHOST_DAO_BASIC      = registerTechniqueBook("technique_book_ghost_dao_basic", Technique.GHOST_DAO_BASIC);

    // 原模组 L501：LEGACY_TECHNIQUE_BOOK_IMMORTAL_BODY（"technique_book_immortal_body" 也是 IMMORTAL_INCANTATION 功法书）
    public static final RegistryObject<TechniqueBookItem> LEGACY_TECHNIQUE_BOOK_IMMORTAL_BODY = ITEMS.register("technique_book_immortal_body", () -> new TechniqueBookItem(new Item.Properties().stacksTo(16).rarity(Rarity.EPIC), Technique.IMMORTAL_INCANTATION));

    private static RegistryObject<TechniqueBookItem> registerTechniqueBook(String name, Technique technique) {
        return ITEMS.register(name, () -> new TechniqueBookItem(new Item.Properties(), technique));
    }

    // ── 法术书（58种） ──
    public static final RegistryObject<SpellBookItem> SPELL_BOOK_QI_SHIELD                     = registerSpellBook("spell_book_qi_shield", Spell.QI_SHIELD);
    public static final RegistryObject<SpellBookItem> SPELL_BOOK_SPIRIT_VISION                 = registerSpellBook("spell_book_spirit_vision", Spell.SPIRIT_VISION);
    public static final RegistryObject<SpellBookItem> SPELL_BOOK_YIN_YANG_EYE                  = registerSpellBook("spell_book_yin_yang_eye", Spell.YIN_YANG_EYE);
    public static final RegistryObject<SpellBookItem> SPELL_BOOK_SLOW_REGEN                    = registerSpellBook("spell_book_slow_regen", Spell.SLOW_REGEN);
    public static final RegistryObject<SpellBookItem> SPELL_BOOK_POISON_IMMUNITY               = registerSpellBook("spell_book_poison_immunity", Spell.POISON_IMMUNITY);
    public static final RegistryObject<SpellBookItem> SPELL_BOOK_FROST_WALKER                  = registerSpellBook("spell_book_frost_walker", Spell.FROST_WALKER);
    public static final RegistryObject<SpellBookItem> SPELL_BOOK_SWORD_AURA                    = registerSpellBook("spell_book_sword_aura", Spell.SWORD_AURA);
    public static final RegistryObject<SpellBookItem> SPELL_BOOK_CLEAR_MIND_INCANTATION        = registerSpellBook("spell_book_clear_mind_incantation", Spell.CLEAR_MIND_INCANTATION);
    public static final RegistryObject<SpellBookItem> SPELL_BOOK_IMMORTAL_INCANTATION          = registerSpellBook("spell_book_immortal_incantation", Spell.IMMORTAL_INCANTATION);
    public static final RegistryObject<SpellBookItem> SPELL_BOOK_QI_FLIGHT                     = registerSpellBook("spell_book_qi_flight", Spell.QI_FLIGHT);
    public static final RegistryObject<SpellBookItem> SPELL_BOOK_GHOST_FLIGHT                  = registerSpellBook("spell_book_ghost_flight", Spell.GHOST_FLIGHT);
    public static final RegistryObject<SpellBookItem> SPELL_BOOK_SOUL_MARK                     = registerSpellBook("spell_book_soul_mark", Spell.SOUL_MARK);
    public static final RegistryObject<SpellBookItem> SPELL_BOOK_BLOODTHIRST_CURSE             = registerSpellBook("spell_book_bloodthirst_curse", Spell.BLOODTHIRST_CURSE);
    public static final RegistryObject<SpellBookItem> SPELL_BOOK_BIGU                          = registerSpellBook("spell_book_bigu", Spell.BIGU);
    public static final RegistryObject<SpellBookItem> SPELL_BOOK_QI_MENDING                    = registerSpellBook("spell_book_qi_mending", Spell.QI_MENDING);
    public static final RegistryObject<SpellBookItem> SPELL_BOOK_DHARMA_BODY_MANIFESTATION     = registerSpellBook("spell_book_dharma_body_manifestation", Spell.DHARMA_BODY_MANIFESTATION);
    public static final RegistryObject<SpellBookItem> SPELL_BOOK_VOID_STEP                     = registerSpellBook("spell_book_void_step", Spell.VOID_STEP);
    public static final RegistryObject<SpellBookItem> SPELL_BOOK_FIREBALL                      = registerSpellBook("spell_book_fireball", Spell.FIREBALL);
    public static final RegistryObject<SpellBookItem> SPELL_BOOK_GREAT_FIREBALL                = registerSpellBook("spell_book_great_fireball", Spell.GREAT_FIREBALL);
    public static final RegistryObject<SpellBookItem> SPELL_BOOK_SWORD_CONVERGENCE             = registerSpellBook("spell_book_sword_convergence", Spell.SWORD_CONVERGENCE);
    public static final RegistryObject<SpellBookItem> SPELL_BOOK_STAR_FALL                     = registerSpellBook("spell_book_star_fall", Spell.STAR_FALL);
    public static final RegistryObject<SpellBookItem> SPELL_BOOK_ICE_LANCE                     = registerSpellBook("spell_book_ice_lance", Spell.ICE_LANCE);
    public static final RegistryObject<SpellBookItem> SPELL_BOOK_LIGHTNING_BOLT                = registerSpellBook("spell_book_lightning_bolt", Spell.LIGHTNING_BOLT);
    public static final RegistryObject<SpellBookItem> SPELL_BOOK_PALM_THUNDER                  = registerSpellBook("spell_book_palm_thunder", Spell.PALM_THUNDER);
    public static final RegistryObject<SpellBookItem> SPELL_BOOK_WIND_BLADE                    = registerSpellBook("spell_book_wind_blade", Spell.WIND_BLADE);
    public static final RegistryObject<SpellBookItem> SPELL_BOOK_POISON_MIST                   = registerSpellBook("spell_book_poison_mist", Spell.POISON_MIST);
    public static final RegistryObject<SpellBookItem> SPELL_BOOK_WITHER_TOUCH                  = registerSpellBook("spell_book_wither_touch", Spell.WITHER_TOUCH);
    public static final RegistryObject<SpellBookItem> SPELL_BOOK_ARROW_VOLLEY                  = registerSpellBook("spell_book_arrow_volley", Spell.ARROW_VOLLEY);
    public static final RegistryObject<SpellBookItem> SPELL_BOOK_EARTH_SPIKE                   = registerSpellBook("spell_book_earth_spike", Spell.EARTH_SPIKE);
    public static final RegistryObject<SpellBookItem> SPELL_BOOK_STONE_BULLET                  = registerSpellBook("spell_book_stone_bullet", Spell.STONE_BULLET);
    public static final RegistryObject<SpellBookItem> SPELL_BOOK_HEAVEN_PIERCING_CONE          = registerSpellBook("spell_book_heaven_piercing_cone", Spell.HEAVEN_PIERCING_CONE);
    public static final RegistryObject<SpellBookItem> SPELL_BOOK_SUN_FLARE                     = registerSpellBook("spell_book_sun_flare", Spell.SUN_FLARE);
    public static final RegistryObject<SpellBookItem> SPELL_BOOK_SHADOW_STEP                   = registerSpellBook("spell_book_shadow_step", Spell.SHADOW_STEP);
    public static final RegistryObject<SpellBookItem> SPELL_BOOK_SOARING                       = registerSpellBook("spell_book_soaring", Spell.SOARING);
    public static final RegistryObject<SpellBookItem> SPELL_BOOK_SPEED_BURST                   = registerSpellBook("spell_book_speed_burst", Spell.SPEED_BURST);
    public static final RegistryObject<SpellBookItem> SPELL_BOOK_INVISIBILITY                  = registerSpellBook("spell_book_invisibility", Spell.INVISIBILITY);
    public static final RegistryObject<SpellBookItem> SPELL_BOOK_HEALING_TOUCH                 = registerSpellBook("spell_book_healing_touch", Spell.HEALING_TOUCH);
    public static final RegistryObject<SpellBookItem> SPELL_BOOK_IRON_BODY                     = registerSpellBook("spell_book_iron_body", Spell.IRON_BODY);
    public static final RegistryObject<SpellBookItem> SPELL_BOOK_WATER_AFFINITY                = registerSpellBook("spell_book_water_affinity", Spell.WATER_AFFINITY);
    public static final RegistryObject<SpellBookItem> SPELL_BOOK_NIGHT_EYE                     = registerSpellBook("spell_book_night_eye", Spell.NIGHT_EYE);
    public static final RegistryObject<SpellBookItem> SPELL_BOOK_FIRE_PROTECTION               = registerSpellBook("spell_book_fire_protection", Spell.FIRE_PROTECTION);
    public static final RegistryObject<SpellBookItem> SPELL_BOOK_SKY_SPLITTING_SWORD_AURA      = registerSpellBook("spell_book_sky_splitting_sword_aura", Spell.SKY_SPLITTING_SWORD_AURA);
    public static final RegistryObject<SpellBookItem> SPELL_BOOK_FLYING_SWORD                  = registerSpellBook("spell_book_flying_sword", Spell.FLYING_SWORD);
    public static final RegistryObject<SpellBookItem> SPELL_BOOK_CLEAR_MIND                    = registerSpellBook("spell_book_clear_mind", Spell.CLEAR_MIND);
    public static final RegistryObject<SpellBookItem> SPELL_BOOK_QI_TRANSFER                   = registerSpellBook("spell_book_qi_transfer", Spell.QI_TRANSFER);
    public static final RegistryObject<SpellBookItem> SPELL_BOOK_TRUTH_SIGHT_EYE               = registerSpellBook("spell_book_truth_sight_eye", Spell.TRUTH_SIGHT_EYE);
    public static final RegistryObject<SpellBookItem> SPELL_BOOK_TIME_STASIS                   = registerSpellBook("spell_book_time_stasis", Spell.TIME_STASIS);
    public static final RegistryObject<SpellBookItem> SPELL_BOOK_TAISHANG_LIFE_BALANCE         = registerSpellBook("spell_book_taishang_life_balance", Spell.TAISHANG_LIFE_BALANCE);
    public static final RegistryObject<SpellBookItem> SPELL_BOOK_BUDDHA_FIRE_LOTUS             = registerSpellBook("spell_book_buddha_fire_lotus", Spell.BUDDHA_FIRE_LOTUS);
    public static final RegistryObject<SpellBookItem> SPELL_BOOK_SPIRIT_LOCK                   = registerSpellBook("spell_book_spirit_lock", Spell.SPIRIT_LOCK);
    public static final RegistryObject<SpellBookItem> SPELL_BOOK_SPIRIT_UNLOCK                 = registerSpellBook("spell_book_spirit_unlock", Spell.SPIRIT_UNLOCK);
    public static final RegistryObject<SpellBookItem> SPELL_BOOK_SWORD_FLIGHT                  = registerSpellBook("spell_book_sword_flight", Spell.SWORD_FLIGHT);
    public static final RegistryObject<SpellBookItem> SPELL_BOOK_CORE_SELF_DESTRUCT            = registerSpellBook("spell_book_core_self_destruct", Spell.CORE_SELF_DESTRUCT);
    public static final RegistryObject<SpellBookItem> SPELL_BOOK_NASCENT_SOUL_OUT_OF_BODY      = registerSpellBook("spell_book_nascent_soul_out_of_body", Spell.NASCENT_SOUL_OUT_OF_BODY);
    public static final RegistryObject<SpellBookItem> SPELL_BOOK_DIVINE_SENSE                  = registerSpellBook("spell_book_divine_sense", Spell.DIVINE_SENSE);
    public static final RegistryObject<SpellBookItem> SPELL_BOOK_VOID_ESCAPE                   = registerSpellBook("spell_book_void_escape", Spell.VOID_ESCAPE);
    public static final RegistryObject<SpellBookItem> SPELL_BOOK_REALM_PRESSURE                = registerSpellBook("spell_book_realm_pressure", Spell.REALM_PRESSURE);
    public static final RegistryObject<SpellBookItem> SPELL_BOOK_SOUL_HOOK                     = registerSpellBook("spell_book_soul_hook", Spell.SOUL_HOOK);

    private static RegistryObject<SpellBookItem> registerSpellBook(String name, Spell spell) {
        return ITEMS.register(name, () -> new SpellBookItem(new Item.Properties(), spell));
    }

    // ── 宗门令牌 ──
    public static final RegistryObject<SectTokenItem> SECT_TOKEN = ITEMS.register("sect_token",
            () -> new SectTokenItem(new Item.Properties().stacksTo(64).rarity(Rarity.UNCOMMON)));

    // ── 炼丹核心方块 ──
    public static final RegistryObject<BlockItem> ALCHEMY_CORE_ITEM =
            ITEMS.register("alchemy_core", () ->
                    new BlockItem(ModBlocks.ALCHEMY_CORE.get(),
                            new Item.Properties().rarity(Rarity.UNCOMMON)));

    // ── 炼器核心方块 ──
    public static final RegistryObject<BlockItem> REFINING_CORE_ITEM =
            ITEMS.register("refining_core", () ->
                    new BlockItem(ModBlocks.REFINING_CORE.get(),
                            new Item.Properties().rarity(Rarity.UNCOMMON)));

    // ── 丹药（6种×5品级=30个） ──
    public static final RegistryObject<PillItem> PILL_QI_RECOVERY_LOW = ITEMS.register("pill_qi_recovery_low", () -> new PillItem(new Item.Properties().rarity(Rarity.COMMON), PillTier.LOW, 10));
    public static final RegistryObject<PillItem> PILL_QI_RECOVERY_MID = ITEMS.register("pill_qi_recovery_mid", () -> new PillItem(new Item.Properties().rarity(Rarity.UNCOMMON), PillTier.MID, 100));
    public static final RegistryObject<PillItem> PILL_QI_RECOVERY_HIGH = ITEMS.register("pill_qi_recovery_high", () -> new PillItem(new Item.Properties().rarity(Rarity.RARE), PillTier.HIGH, 1000));
    public static final RegistryObject<PillItem> PILL_QI_RECOVERY_SUPREME = ITEMS.register("pill_qi_recovery_supreme", () -> new PillItem(new Item.Properties().rarity(Rarity.EPIC), PillTier.SUPREME, 10000));
    public static final RegistryObject<PillItem> PILL_QI_RECOVERY_IMMORTAL = ITEMS.register("pill_qi_recovery_immortal", () -> new PillItem(new Item.Properties().rarity(Rarity.EPIC), PillTier.IMMORTAL, -1));
    public static final RegistryObject<PillItem> PILL_CULTIVATION_LOW = ITEMS.register("pill_cultivation_low", () -> new CultivationPillItem(new Item.Properties().rarity(Rarity.COMMON), PillTier.LOW, 10));
    public static final RegistryObject<PillItem> PILL_CULTIVATION_MID = ITEMS.register("pill_cultivation_mid", () -> new CultivationPillItem(new Item.Properties().rarity(Rarity.UNCOMMON), PillTier.MID, 100));
    public static final RegistryObject<PillItem> PILL_CULTIVATION_HIGH = ITEMS.register("pill_cultivation_high", () -> new CultivationPillItem(new Item.Properties().rarity(Rarity.RARE), PillTier.HIGH, 1000));
    public static final RegistryObject<PillItem> PILL_CULTIVATION_SUPREME = ITEMS.register("pill_cultivation_supreme", () -> new CultivationPillItem(new Item.Properties().rarity(Rarity.EPIC), PillTier.SUPREME, 10000));
    public static final RegistryObject<PillItem> PILL_CULTIVATION_IMMORTAL = ITEMS.register("pill_cultivation_immortal", () -> new CultivationPillItem(new Item.Properties().rarity(Rarity.EPIC), PillTier.IMMORTAL, -1));
    public static final RegistryObject<PillItem> PILL_BLOOD_BURN_LOW = ITEMS.register("pill_blood_burn_low", () -> new BloodBurnPillItem(new Item.Properties().rarity(Rarity.COMMON), PillTier.LOW));
    public static final RegistryObject<PillItem> PILL_BLOOD_BURN_MID = ITEMS.register("pill_blood_burn_mid", () -> new BloodBurnPillItem(new Item.Properties().rarity(Rarity.UNCOMMON), PillTier.MID));
    public static final RegistryObject<PillItem> PILL_BLOOD_BURN_HIGH = ITEMS.register("pill_blood_burn_high", () -> new BloodBurnPillItem(new Item.Properties().rarity(Rarity.RARE), PillTier.HIGH));
    public static final RegistryObject<PillItem> PILL_BLOOD_BURN_SUPREME = ITEMS.register("pill_blood_burn_supreme", () -> new BloodBurnPillItem(new Item.Properties().rarity(Rarity.EPIC), PillTier.SUPREME));
    public static final RegistryObject<PillItem> PILL_BLOOD_BURN_IMMORTAL = ITEMS.register("pill_blood_burn_immortal", () -> new BloodBurnPillItem(new Item.Properties().rarity(Rarity.EPIC), PillTier.IMMORTAL));
    public static final RegistryObject<PillItem> PILL_CLEAR_MIND_LOW = ITEMS.register("pill_clear_mind_low", () -> new ClearMindPillItem(new Item.Properties().rarity(Rarity.COMMON), PillTier.LOW));
    public static final RegistryObject<PillItem> PILL_CLEAR_MIND_MID = ITEMS.register("pill_clear_mind_mid", () -> new ClearMindPillItem(new Item.Properties().rarity(Rarity.UNCOMMON), PillTier.MID));
    public static final RegistryObject<PillItem> PILL_CLEAR_MIND_HIGH = ITEMS.register("pill_clear_mind_high", () -> new ClearMindPillItem(new Item.Properties().rarity(Rarity.RARE), PillTier.HIGH));
    public static final RegistryObject<PillItem> PILL_CLEAR_MIND_SUPREME = ITEMS.register("pill_clear_mind_supreme", () -> new ClearMindPillItem(new Item.Properties().rarity(Rarity.EPIC), PillTier.SUPREME));
    public static final RegistryObject<PillItem> PILL_CLEAR_MIND_IMMORTAL = ITEMS.register("pill_clear_mind_immortal", () -> new ClearMindPillItem(new Item.Properties().rarity(Rarity.EPIC), PillTier.IMMORTAL));
    public static final RegistryObject<PillItem> PILL_REJUVENATION_LOW = ITEMS.register("pill_rejuvenation_low", () -> new RejuvenationPillItem(new Item.Properties().rarity(Rarity.COMMON), PillTier.LOW));
    public static final RegistryObject<PillItem> PILL_REJUVENATION_MID = ITEMS.register("pill_rejuvenation_mid", () -> new RejuvenationPillItem(new Item.Properties().rarity(Rarity.UNCOMMON), PillTier.MID));
    public static final RegistryObject<PillItem> PILL_REJUVENATION_HIGH = ITEMS.register("pill_rejuvenation_high", () -> new RejuvenationPillItem(new Item.Properties().rarity(Rarity.RARE), PillTier.HIGH));
    public static final RegistryObject<PillItem> PILL_REJUVENATION_SUPREME = ITEMS.register("pill_rejuvenation_supreme", () -> new RejuvenationPillItem(new Item.Properties().rarity(Rarity.EPIC), PillTier.SUPREME));
    public static final RegistryObject<PillItem> PILL_REJUVENATION_IMMORTAL = ITEMS.register("pill_rejuvenation_immortal", () -> new LifeCreationPillItem(new Item.Properties().rarity(Rarity.EPIC)));
    public static final RegistryObject<PillItem> PILL_DIVINE_STRIDE_LOW = ITEMS.register("pill_divine_stride_low", () -> new DivineStridePillItem(new Item.Properties().rarity(Rarity.COMMON), PillTier.LOW));
    public static final RegistryObject<PillItem> PILL_DIVINE_STRIDE_MID = ITEMS.register("pill_divine_stride_mid", () -> new DivineStridePillItem(new Item.Properties().rarity(Rarity.UNCOMMON), PillTier.MID));
    public static final RegistryObject<PillItem> PILL_DIVINE_STRIDE_HIGH = ITEMS.register("pill_divine_stride_high", () -> new DivineStridePillItem(new Item.Properties().rarity(Rarity.RARE), PillTier.HIGH));
    public static final RegistryObject<PillItem> PILL_DIVINE_STRIDE_SUPREME = ITEMS.register("pill_divine_stride_supreme", () -> new DivineStridePillItem(new Item.Properties().rarity(Rarity.EPIC), PillTier.SUPREME));
    public static final RegistryObject<PillItem> PILL_DIVINE_STRIDE_IMMORTAL = ITEMS.register("pill_divine_stride_immortal", () -> new ShadowStepPillItem(new Item.Properties().rarity(Rarity.EPIC)));

    public static final RegistryObject<RecallPillItem> RECALL_PILL = ITEMS.register("recall_pill", () -> new RecallPillItem(new Item.Properties().stacksTo(1).rarity(Rarity.EPIC)));
    public static final RegistryObject<YouthPillItem> YOUTH_PILL = ITEMS.register("youth_pill", () -> new YouthPillItem(new Item.Properties().rarity(Rarity.RARE)));
    public static final RegistryObject<SexChangePillItem> SEX_CHANGE_PILL = ITEMS.register("sex_change_pill", () -> new SexChangePillItem(new Item.Properties().rarity(Rarity.UNCOMMON)));

    // ── 金丹材料（筑基丹/血筑基丹等）─ 完整复刻原模组 GoldenCoreMaterialItem ──
    public static final RegistryObject<GoldenCoreMaterialItem> JIEDAN_PILL = ITEMS.register("jiedan_pill", () -> new GoldenCoreMaterialItem(new Item.Properties().rarity(Rarity.RARE), GoldenCoreMaterialItem.Kind.JIEDAN_PILL));
    public static final RegistryObject<GoldenCoreMaterialItem> BLOOD_JIEDAN_PILL = ITEMS.register("blood_jiedan_pill", () -> new GoldenCoreMaterialItem(new Item.Properties().rarity(Rarity.EPIC), GoldenCoreMaterialItem.Kind.BLOOD_JIEDAN_PILL));
    public static final RegistryObject<GoldenCoreMaterialItem> ALL_CREATURES_TRUE_BLOOD = ITEMS.register("all_creatures_true_blood", () -> new GoldenCoreMaterialItem(new Item.Properties().rarity(Rarity.EPIC), GoldenCoreMaterialItem.Kind.TRUE_BLOOD));
    public static final RegistryObject<GoldenCoreMaterialItem> EARTH_EVIL_QI = ITEMS.register("earth_evil_qi", () -> new GoldenCoreMaterialItem(new Item.Properties().rarity(Rarity.RARE), GoldenCoreMaterialItem.Kind.EARTH_EVIL_QI));
    public static final RegistryObject<GoldenCoreMaterialItem> HEAVEN_CLEAR_QI = ITEMS.register("heaven_clear_qi", () -> new GoldenCoreMaterialItem(new Item.Properties().rarity(Rarity.EPIC), GoldenCoreMaterialItem.Kind.HEAVEN_CLEAR_QI));
    public static final RegistryObject<GoldenCoreMaterialItem> NINGZHEN_CREATION_FRUIT = ITEMS.register("ningzhen_creation_fruit", () -> new GoldenCoreMaterialItem(new Item.Properties().rarity(Rarity.EPIC), GoldenCoreMaterialItem.Kind.CREATION_FRUIT));
    public static final RegistryObject<GoldenCoreMaterialItem> BLOOD_TRANSFORMATION_TALISMAN = ITEMS.register("blood_transformation_talisman", () -> new GoldenCoreMaterialItem(new Item.Properties().stacksTo(16).rarity(Rarity.EPIC), GoldenCoreMaterialItem.Kind.BLOOD_TRANSFORMATION_TALISMAN));

    // ── 筑基期材料 — 完整复刻原模组 FoundationMaterialItem ──
    public static final RegistryObject<FoundationMaterialItem> ZHUJI_DAN = ITEMS.register("zhuji_dan", () -> new FoundationMaterialItem(new Item.Properties().rarity(Rarity.UNCOMMON), FoundationMaterialItem.Kind.ZHUJI_DAN));
    public static final RegistryObject<FoundationMaterialItem> BLOOD_SPIRIT_PILL = ITEMS.register("blood_spirit_pill", () -> new FoundationMaterialItem(new Item.Properties().rarity(Rarity.RARE), FoundationMaterialItem.Kind.BLOOD_PILL));
    public static final RegistryObject<FoundationMaterialItem> DAO_FOUNDATION_FRUIT = ITEMS.register("dao_foundation_fruit", () -> new FoundationMaterialItem(new Item.Properties().rarity(Rarity.EPIC), FoundationMaterialItem.Kind.DAO_FRUIT));
    public static final RegistryObject<FoundationSecretItem> FOUNDATION_SECRET = ITEMS.register("foundation_secret", () -> new FoundationSecretItem(new Item.Properties().stacksTo(1).rarity(Rarity.EPIC)));

    // ── 灵石 — 完整复刻原模组 SpiritStoneItem ──
    public static final RegistryObject<SpiritStoneItem> LOW_SPIRIT_STONE = ITEMS.register("low_spirit_stone", () -> new SpiritStoneItem(new Item.Properties(), ItemTier.LOW, 10));
    public static final RegistryObject<SpiritStoneItem> MID_SPIRIT_STONE = ITEMS.register("mid_spirit_stone", () -> new SpiritStoneItem(new Item.Properties().rarity(Rarity.UNCOMMON), ItemTier.MID, 100));
    public static final RegistryObject<SpiritStoneItem> HIGH_SPIRIT_STONE = ITEMS.register("high_spirit_stone", () -> new SpiritStoneItem(new Item.Properties().rarity(Rarity.RARE), ItemTier.HIGH, 1000));
    public static final RegistryObject<SpiritStoneItem> SUPREME_SPIRIT_STONE = ITEMS.register("supreme_spirit_stone", () -> new SpiritStoneItem(new Item.Properties().rarity(Rarity.EPIC), ItemTier.SUPREME, 10000));

    // ── 灵剑 — 完整复刻原模组 4种剑×5品质=20个 ──
    public static final RegistryObject<XuanIronSwordItem> XUAN_IRON_SWORD_LOW = ITEMS.register("xuan_iron_sword_low", () -> new XuanIronSwordItem(ItemTier.LOW, 10, 10));
    public static final RegistryObject<XuanIronSwordItem> XUAN_IRON_SWORD_MID = ITEMS.register("xuan_iron_sword_mid", () -> new XuanIronSwordItem(ItemTier.MID, 12, 20));
    public static final RegistryObject<XuanIronSwordItem> XUAN_IRON_SWORD_HIGH = ITEMS.register("xuan_iron_sword_high", () -> new XuanIronSwordItem(ItemTier.HIGH, 15, 30));
    public static final RegistryObject<XuanIronSwordItem> XUAN_IRON_SWORD_SUPREME = ITEMS.register("xuan_iron_sword_supreme", () -> new XuanIronSwordItem(ItemTier.SUPREME, 17, 40));
    public static final RegistryObject<XuanIronSwordItem> XUAN_IRON_SWORD_IMMORTAL = ITEMS.register("xuan_iron_sword_immortal", () -> new XuanIronSwordItem(ItemTier.IMMORTAL, 20, 50));
    public static final RegistryObject<QingMuSwordItem> QING_MU_SWORD_LOW = ITEMS.register("qing_mu_sword_low", () -> new QingMuSwordItem(ItemTier.LOW, 10, 10));
    public static final RegistryObject<QingMuSwordItem> QING_MU_SWORD_MID = ITEMS.register("qing_mu_sword_mid", () -> new QingMuSwordItem(ItemTier.MID, 12, 20));
    public static final RegistryObject<QingMuSwordItem> QING_MU_SWORD_HIGH = ITEMS.register("qing_mu_sword_high", () -> new QingMuSwordItem(ItemTier.HIGH, 15, 30));
    public static final RegistryObject<QingMuSwordItem> QING_MU_SWORD_SUPREME = ITEMS.register("qing_mu_sword_supreme", () -> new QingMuSwordItem(ItemTier.SUPREME, 17, 40));
    public static final RegistryObject<QingMuSwordItem> QING_MU_SWORD_IMMORTAL = ITEMS.register("qing_mu_sword_immortal", () -> new QingMuSwordItem(ItemTier.IMMORTAL, 20, 50));
    public static final RegistryObject<ChiYanSwordItem> CHI_YAN_SWORD_LOW = ITEMS.register("chi_yan_sword_low", () -> new ChiYanSwordItem(ItemTier.LOW, 10, 10));
    public static final RegistryObject<ChiYanSwordItem> CHI_YAN_SWORD_MID = ITEMS.register("chi_yan_sword_mid", () -> new ChiYanSwordItem(ItemTier.MID, 12, 20));
    public static final RegistryObject<ChiYanSwordItem> CHI_YAN_SWORD_HIGH = ITEMS.register("chi_yan_sword_high", () -> new ChiYanSwordItem(ItemTier.HIGH, 15, 30));
    public static final RegistryObject<ChiYanSwordItem> CHI_YAN_SWORD_SUPREME = ITEMS.register("chi_yan_sword_supreme", () -> new ChiYanSwordItem(ItemTier.SUPREME, 17, 40));
    public static final RegistryObject<ChiYanSwordItem> CHI_YAN_SWORD_IMMORTAL = ITEMS.register("chi_yan_sword_immortal", () -> new ChiYanSwordItem(ItemTier.IMMORTAL, 20, 50));
    public static final RegistryObject<HanBingSwordItem> HAN_BING_SWORD_LOW = ITEMS.register("han_bing_sword_low", () -> new HanBingSwordItem(ItemTier.LOW, 10, 10));
    public static final RegistryObject<HanBingSwordItem> HAN_BING_SWORD_MID = ITEMS.register("han_bing_sword_mid", () -> new HanBingSwordItem(ItemTier.MID, 12, 20));
    public static final RegistryObject<HanBingSwordItem> HAN_BING_SWORD_HIGH = ITEMS.register("han_bing_sword_high", () -> new HanBingSwordItem(ItemTier.HIGH, 15, 30));
    public static final RegistryObject<HanBingSwordItem> HAN_BING_SWORD_SUPREME = ITEMS.register("han_bing_sword_supreme", () -> new HanBingSwordItem(ItemTier.SUPREME, 17, 40));
    public static final RegistryObject<HanBingSwordItem> HAN_BING_SWORD_IMMORTAL = ITEMS.register("han_bing_sword_immortal", () -> new HanBingSwordItem(ItemTier.IMMORTAL, 20, 50));

    // ═══════════════════════════════════════════
    // 抽卡与身份相关物品（复刻自原模组 ModItems）
    // ═══════════════════════════════════════════

    // 灵草
    public static final RegistryObject<Item> HERB = ITEMS.register("herb",
            () -> new BlockItem(ModBlocks.HERB.get(), new Item.Properties()));

    // 蒲团
    public static final RegistryObject<BlockItem> CUSHION = ITEMS.register("cushion",
            () -> new BlockItem(ModBlocks.CUSHION.get(), new Item.Properties()));

    // 轮回命盘（首次身份选择）
    public static final RegistryObject<Item> REINCARNATION_FATE_PLATE = ITEMS.register("reincarnation_fate_plate",
            () -> new ReincarnationFatePlateItem(new Item.Properties().stacksTo(1).rarity(Rarity.EPIC)));

    // 转世令（重配身份）
    public static final RegistryObject<Item> ORIGIN_RECONFIGURATION_TOKEN = ITEMS.register("origin_reconfiguration_token",
            () -> new OriginReconfigurationTokenItem(new Item.Properties().stacksTo(1).rarity(Rarity.EPIC)));

    // 符纸
    public static final RegistryObject<Item> TALISMAN_PAPER = ITEMS.register("talisman_paper",
            () -> new Item(new Item.Properties()));

    // 墨水
    public static final RegistryObject<Item> INK = ITEMS.register("ink",
            () -> new Item(new Item.Properties()));

    // 下品阵盘
    public static final RegistryObject<BlockItem> LOW_FORMATION_CORE_PLATE = ITEMS.register("low_formation_core_plate",
            () -> new BlockItem(ModBlocks.FORMATION_CORE_PLATE_LOW.get(), new Item.Properties().rarity(Rarity.UNCOMMON)));

    // ── 聚灵阵旗 / 枯萎生长阵旗 / 复苏阵旗（BlockItem 照原模组） ──
    public static final RegistryObject<BlockItem> LOW_QI_GATHERING_FLAG = ITEMS.register("low_qi_gathering_flag",
            () -> new BlockItem(ModBlocks.LOW_QI_GATHERING_FLAG.get(), new Item.Properties().rarity(Rarity.COMMON)));
    public static final RegistryObject<BlockItem> LOW_WITHER_GROWTH_FLAG = ITEMS.register("low_wither_growth_flag",
            () -> new BlockItem(ModBlocks.LOW_WITHER_GROWTH_FLAG.get(), new Item.Properties().rarity(Rarity.COMMON)));
    public static final RegistryObject<BlockItem> LOW_REJUVENATION_FLAG = ITEMS.register("low_rejuvenation_flag",
            () -> new BlockItem(ModBlocks.LOW_REJUVENATION_FLAG.get(), new Item.Properties().rarity(Rarity.COMMON)));

    // ── 完整复刻：5 品级阵法核心板（BlockItem 照原模组） ──
    public static final RegistryObject<BlockItem> MID_FORMATION_CORE_PLATE = ITEMS.register("mid_formation_core_plate", () -> new BlockItem(ModBlocks.FORMATION_CORE_PLATE_MID.get(), new Item.Properties().rarity(Rarity.RARE)));
    public static final RegistryObject<BlockItem> HIGH_FORMATION_CORE_PLATE = ITEMS.register("high_formation_core_plate", () -> new BlockItem(ModBlocks.FORMATION_CORE_PLATE_HIGH.get(), new Item.Properties().rarity(Rarity.RARE)));
    public static final RegistryObject<BlockItem> SUPREME_FORMATION_CORE_PLATE = ITEMS.register("supreme_formation_core_plate", () -> new BlockItem(ModBlocks.FORMATION_CORE_PLATE_SUPREME.get(), new Item.Properties().rarity(Rarity.EPIC)));
    public static final RegistryObject<BlockItem> IMMORTAL_FORMATION_CORE_PLATE = ITEMS.register("immortal_formation_core_plate", () -> new BlockItem(ModBlocks.FORMATION_CORE_PLATE_IMMORTAL.get(), new Item.Properties().rarity(Rarity.EPIC)));

    // ── 灵脉核心 × 5 品级（BlockItem 照原模组，稀有度用 SpiritVeinCoreTier.rarity()） ──
    public static final RegistryObject<BlockItem> LOW_SPIRIT_VEIN_CORE = ITEMS.register("low_spirit_vein_core", () -> new BlockItem(ModBlocks.LOW_SPIRIT_VEIN_CORE.get(), new Item.Properties().rarity(SpiritVeinCoreTier.LOW.rarity())));
    public static final RegistryObject<BlockItem> MID_SPIRIT_VEIN_CORE = ITEMS.register("mid_spirit_vein_core", () -> new BlockItem(ModBlocks.MID_SPIRIT_VEIN_CORE.get(), new Item.Properties().rarity(SpiritVeinCoreTier.MID.rarity())));
    public static final RegistryObject<BlockItem> HIGH_SPIRIT_VEIN_CORE = ITEMS.register("high_spirit_vein_core", () -> new BlockItem(ModBlocks.HIGH_SPIRIT_VEIN_CORE.get(), new Item.Properties().rarity(SpiritVeinCoreTier.HIGH.rarity())));
    public static final RegistryObject<BlockItem> SUPREME_SPIRIT_VEIN_CORE = ITEMS.register("supreme_spirit_vein_core", () -> new BlockItem(ModBlocks.SUPREME_SPIRIT_VEIN_CORE.get(), new Item.Properties().rarity(SpiritVeinCoreTier.SUPREME.rarity())));
    public static final RegistryObject<BlockItem> IMMORTAL_SPIRIT_VEIN_CORE = ITEMS.register("immortal_spirit_vein_core", () -> new BlockItem(ModBlocks.IMMORTAL_SPIRIT_VEIN_CORE.get(), new Item.Properties().rarity(SpiritVeinCoreTier.IMMORTAL.rarity())));

    // ── 聚灵阵旗 mid/high/supreme/immortal（BlockItem 照原模组） ──
    public static final RegistryObject<BlockItem> MID_QI_GATHERING_FLAG = ITEMS.register("mid_qi_gathering_flag", () -> new BlockItem(ModBlocks.MID_QI_GATHERING_FLAG.get(), new Item.Properties().rarity(Rarity.UNCOMMON)));
    public static final RegistryObject<BlockItem> HIGH_QI_GATHERING_FLAG = ITEMS.register("high_qi_gathering_flag", () -> new BlockItem(ModBlocks.HIGH_QI_GATHERING_FLAG.get(), new Item.Properties().rarity(Rarity.RARE)));
    public static final RegistryObject<BlockItem> SUPREME_QI_GATHERING_FLAG = ITEMS.register("supreme_qi_gathering_flag", () -> new BlockItem(ModBlocks.SUPREME_QI_GATHERING_FLAG.get(), new Item.Properties().rarity(Rarity.EPIC)));
    public static final RegistryObject<BlockItem> IMMORTAL_QI_GATHERING_FLAG = ITEMS.register("immortal_qi_gathering_flag", () -> new BlockItem(ModBlocks.IMMORTAL_QI_GATHERING_FLAG.get(), new Item.Properties().rarity(Rarity.EPIC)));

    public static final RegistryObject<BlockItem> MID_WITHER_GROWTH_FLAG = ITEMS.register("mid_wither_growth_flag", () -> new BlockItem(ModBlocks.MID_WITHER_GROWTH_FLAG.get(), new Item.Properties().rarity(Rarity.UNCOMMON)));
    public static final RegistryObject<BlockItem> HIGH_WITHER_GROWTH_FLAG = ITEMS.register("high_wither_growth_flag", () -> new BlockItem(ModBlocks.HIGH_WITHER_GROWTH_FLAG.get(), new Item.Properties().rarity(Rarity.RARE)));
    public static final RegistryObject<BlockItem> SUPREME_WITHER_GROWTH_FLAG = ITEMS.register("supreme_wither_growth_flag", () -> new BlockItem(ModBlocks.SUPREME_WITHER_GROWTH_FLAG.get(), new Item.Properties().rarity(Rarity.EPIC)));
    public static final RegistryObject<BlockItem> IMMORTAL_WITHER_GROWTH_FLAG = ITEMS.register("immortal_wither_growth_flag", () -> new BlockItem(ModBlocks.IMMORTAL_WITHER_GROWTH_FLAG.get(), new Item.Properties().rarity(Rarity.EPIC)));

    public static final RegistryObject<BlockItem> MID_REJUVENATION_FLAG = ITEMS.register("mid_rejuvenation_flag", () -> new BlockItem(ModBlocks.MID_REJUVENATION_FLAG.get(), new Item.Properties().rarity(Rarity.UNCOMMON)));
    public static final RegistryObject<BlockItem> HIGH_REJUVENATION_FLAG = ITEMS.register("high_rejuvenation_flag", () -> new BlockItem(ModBlocks.HIGH_REJUVENATION_FLAG.get(), new Item.Properties().rarity(Rarity.RARE)));
    public static final RegistryObject<BlockItem> SUPREME_REJUVENATION_FLAG = ITEMS.register("supreme_rejuvenation_flag", () -> new BlockItem(ModBlocks.SUPREME_REJUVENATION_FLAG.get(), new Item.Properties().rarity(Rarity.EPIC)));
    public static final RegistryObject<BlockItem> IMMORTAL_REJUVENATION_FLAG = ITEMS.register("immortal_rejuvenation_flag", () -> new BlockItem(ModBlocks.IMMORTAL_REJUVENATION_FLAG.get(), new Item.Properties().rarity(Rarity.EPIC)));

    // ── 灵田收割阵旗（5 品级，BlockItem 照原模组） ──
    public static final RegistryObject<BlockItem> LOW_FARM_HARVEST_FLAG = ITEMS.register("low_farm_harvest_flag", () -> new BlockItem(ModBlocks.LOW_FARM_HARVEST_FLAG.get(), new Item.Properties().rarity(Rarity.COMMON)));
    public static final RegistryObject<BlockItem> MID_FARM_HARVEST_FLAG = ITEMS.register("mid_farm_harvest_flag", () -> new BlockItem(ModBlocks.MID_FARM_HARVEST_FLAG.get(), new Item.Properties().rarity(Rarity.UNCOMMON)));
    public static final RegistryObject<BlockItem> HIGH_FARM_HARVEST_FLAG = ITEMS.register("high_farm_harvest_flag", () -> new BlockItem(ModBlocks.HIGH_FARM_HARVEST_FLAG.get(), new Item.Properties().rarity(Rarity.RARE)));
    public static final RegistryObject<BlockItem> SUPREME_FARM_HARVEST_FLAG = ITEMS.register("supreme_farm_harvest_flag", () -> new BlockItem(ModBlocks.SUPREME_FARM_HARVEST_FLAG.get(), new Item.Properties().rarity(Rarity.EPIC)));
    public static final RegistryObject<BlockItem> IMMORTAL_FARM_HARVEST_FLAG = ITEMS.register("immortal_farm_harvest_flag", () -> new BlockItem(ModBlocks.IMMORTAL_FARM_HARVEST_FLAG.get(), new Item.Properties().rarity(Rarity.EPIC)));

    public static final RegistryObject<BlockItem> LOW_FLIGHT_BAN_FLAG = ITEMS.register("low_flight_ban_flag", () -> new BlockItem(ModBlocks.LOW_FLIGHT_BAN_FLAG.get(), new Item.Properties().rarity(Rarity.COMMON)));
    public static final RegistryObject<BlockItem> MID_FLIGHT_BAN_FLAG = ITEMS.register("mid_flight_ban_flag", () -> new BlockItem(ModBlocks.MID_FLIGHT_BAN_FLAG.get(), new Item.Properties().rarity(Rarity.UNCOMMON)));
    public static final RegistryObject<BlockItem> HIGH_FLIGHT_BAN_FLAG = ITEMS.register("high_flight_ban_flag", () -> new BlockItem(ModBlocks.HIGH_FLIGHT_BAN_FLAG.get(), new Item.Properties().rarity(Rarity.RARE)));
    public static final RegistryObject<BlockItem> SUPREME_FLIGHT_BAN_FLAG = ITEMS.register("supreme_flight_ban_flag", () -> new BlockItem(ModBlocks.SUPREME_FLIGHT_BAN_FLAG.get(), new Item.Properties().rarity(Rarity.EPIC)));
    public static final RegistryObject<BlockItem> IMMORTAL_FLIGHT_BAN_FLAG = ITEMS.register("immortal_flight_ban_flag", () -> new BlockItem(ModBlocks.IMMORTAL_FLIGHT_BAN_FLAG.get(), new Item.Properties().rarity(Rarity.EPIC)));

    public static final RegistryObject<BlockItem> LOW_MAZE_FLAG = ITEMS.register("low_maze_flag", () -> new BlockItem(ModBlocks.LOW_MAZE_FLAG.get(), new Item.Properties().rarity(Rarity.COMMON)));
    public static final RegistryObject<BlockItem> MID_MAZE_FLAG = ITEMS.register("mid_maze_flag", () -> new BlockItem(ModBlocks.MID_MAZE_FLAG.get(), new Item.Properties().rarity(Rarity.UNCOMMON)));
    public static final RegistryObject<BlockItem> HIGH_MAZE_FLAG = ITEMS.register("high_maze_flag", () -> new BlockItem(ModBlocks.HIGH_MAZE_FLAG.get(), new Item.Properties().rarity(Rarity.RARE)));
    public static final RegistryObject<BlockItem> SUPREME_MAZE_FLAG = ITEMS.register("supreme_maze_flag", () -> new BlockItem(ModBlocks.SUPREME_MAZE_FLAG.get(), new Item.Properties().rarity(Rarity.EPIC)));
    public static final RegistryObject<BlockItem> IMMORTAL_MAZE_FLAG = ITEMS.register("immortal_maze_flag", () -> new BlockItem(ModBlocks.IMMORTAL_MAZE_FLAG.get(), new Item.Properties().rarity(Rarity.EPIC)));

    public static final RegistryObject<BlockItem> LOW_SECT_PROTECTION_FLAG = ITEMS.register("low_sect_protection_flag", () -> new BlockItem(ModBlocks.LOW_SECT_PROTECTION_FLAG.get(), new Item.Properties().rarity(Rarity.COMMON)));
    public static final RegistryObject<BlockItem> MID_SECT_PROTECTION_FLAG = ITEMS.register("mid_sect_protection_flag", () -> new BlockItem(ModBlocks.MID_SECT_PROTECTION_FLAG.get(), new Item.Properties().rarity(Rarity.UNCOMMON)));
    public static final RegistryObject<BlockItem> HIGH_SECT_PROTECTION_FLAG = ITEMS.register("high_sect_protection_flag", () -> new BlockItem(ModBlocks.HIGH_SECT_PROTECTION_FLAG.get(), new Item.Properties().rarity(Rarity.RARE)));
    public static final RegistryObject<BlockItem> SUPREME_SECT_PROTECTION_FLAG = ITEMS.register("supreme_sect_protection_flag", () -> new BlockItem(ModBlocks.SUPREME_SECT_PROTECTION_FLAG.get(), new Item.Properties().rarity(Rarity.EPIC)));
    public static final RegistryObject<BlockItem> IMMORTAL_SECT_PROTECTION_FLAG = ITEMS.register("immortal_sect_protection_flag", () -> new BlockItem(ModBlocks.IMMORTAL_SECT_PROTECTION_FLAG.get(), new Item.Properties().rarity(Rarity.EPIC)));

    // 第 5 品级灵石
    

    // 杂项补充
    public static final RegistryObject<Item> CULTIVATION_COMPENDIUM = ITEMS.register("cultivation_compendium", () -> new CultivationCompendiumItem(new Item.Properties().stacksTo(1).rarity(Rarity.UNCOMMON)));
    public static final RegistryObject<Item> DIVINATION_COMPASS = ITEMS.register("divination_compass", () -> new DivinationCompassItem(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> FORMATION_COMPASS = ITEMS.register("formation_compass", () -> new FormationCompassItem(new Item.Properties().stacksTo(1).rarity(Rarity.UNCOMMON)));
    public static final RegistryObject<Item> FORMATION_INSCRIPTION_KNIFE = ITEMS.register("formation_inscription_knife", () -> new FormationInscriptionKnifeItem(new Item.Properties().stacksTo(1).durability(384).rarity(Rarity.UNCOMMON)));
    
    public static final RegistryObject<Item> SOUL_REAPER_TOKEN = ITEMS.register("soul_reaper_token", () -> new SoulReaperTokenItem(new Item.Properties().stacksTo(1).rarity(Rarity.RARE).fireResistant()));

    // === Round 21 补全：原 mod ModItems 中缺失的 Item（BlockItem 照原模组） ===
    public static final RegistryObject<BlockItem> BONE_BLOCK = ITEMS.register("bone_block",
            () -> new BlockItem(ModBlocks.BONE_BLOCK.get(), new Item.Properties()));
    public static final RegistryObject<BlockItem> SPIRIT_VEIN_SPRING = ITEMS.register("spirit_vein_spring",
            () -> new BlockItem(ModBlocks.SPIRIT_VEIN_SPRING.get(), new Item.Properties().rarity(Rarity.RARE)));
    public static final RegistryObject<BlockItem> LOW_SPIRIT_STONE_ORE = ITEMS.register("low_spirit_stone_ore",
            () -> new BlockItem(ModBlocks.LOW_SPIRIT_STONE_ORE.get(), new Item.Properties()));
    public static final RegistryObject<BlockItem> MID_SPIRIT_STONE_ORE = ITEMS.register("mid_spirit_stone_ore",
            () -> new BlockItem(ModBlocks.MID_SPIRIT_STONE_ORE.get(), new Item.Properties()));
    public static final RegistryObject<BlockItem> HIGH_SPIRIT_STONE_ORE = ITEMS.register("high_spirit_stone_ore",
            () -> new BlockItem(ModBlocks.HIGH_SPIRIT_STONE_ORE.get(), new Item.Properties()));
    public static final RegistryObject<BlockItem> SUPREME_SPIRIT_STONE_ORE = ITEMS.register("supreme_spirit_stone_ore",
            () -> new BlockItem(ModBlocks.SUPREME_SPIRIT_STONE_ORE.get(), new Item.Properties()));
    public static final RegistryObject<Item> SOUL_HOOK = ITEMS.register("soul_hook", () -> new SoulHookItem(new Item.Properties().stacksTo(1).rarity(Rarity.EPIC).fireResistant()));
    public static final RegistryObject<Item> SOUL_REAPER_SPAWN_EGG = ITEMS.register("soul_reaper_spawn_egg", () -> new DifuReaperSpawnEggItem(1316382, 5691082, new Item.Properties().rarity(Rarity.RARE)));

    // ── 法术书/功法书 EXTRA 查找表（照搬原模组 ModItems L502-532 填充逻辑，适配项目已有字段） ──
    private static final java.util.Map<Spell, RegistryObject<Item>> SPELL_BOOKS_EXTRA = new java.util.EnumMap<>(Spell.class);
    private static final java.util.Map<Technique, RegistryObject<Item>> TECHNIQUE_BOOKS_EXTRA = new java.util.EnumMap<>(Technique.class);
    private static final Comparator<Spell> SPELL_BOOK_ORDER = Comparator.comparingInt((Spell spell) -> spell.tier().ordinal()).thenComparingInt(Enum::ordinal);
    private static final Comparator<Technique> TECHNIQUE_BOOK_ORDER = Comparator.comparingInt((Technique technique) -> technique.tier().ordinal()).thenComparingInt(Enum::ordinal);

    // 境界令牌 / 散仙劫波令牌 / 修士生怪蛋（照搬原模组 ModItems L639-660）
    public static final Map<Realm, RegistryObject<Item>> REALM_TOKENS;
    public static final Map<Integer, RegistryObject<Item>> LOOSE_IMMORTAL_REALM_TOKENS;
    public static final Map<Realm, RegistryObject<Item>> CULTIVATOR_SPAWN_EGGS;

    static {
        // 1.20.1 DeferredRegister.getEntries() 返回 Collection<RegistryObject<Item>>，先按注册名建索引
        java.util.Map<net.minecraft.resources.ResourceLocation, RegistryObject<Item>> byId = new java.util.HashMap<>();
        for (RegistryObject<Item> ro : ITEMS.getEntries()) {
            byId.put(ro.getId(), ro);
        }
        // 原模组：除 dedicated(QI_SHIELD/SPIRIT_VISION/FIREBALL/IMMORTAL_INCANTATION) 和 runtime-only(GHOST_FLIGHT) 外全部 Spell
        for (Spell s : Spell.values()) {
            if (s == Spell.QI_SHIELD || s == Spell.SPIRIT_VISION
                    || s == Spell.FIREBALL || s == Spell.IMMORTAL_INCANTATION
                    || s == Spell.GHOST_FLIGHT) continue;
            RegistryObject<Item> ro = byId.get(new net.minecraft.resources.ResourceLocation(FridayCultivationMod.MOD_ID, "spell_book_" + s.id()));
            if (ro != null) SPELL_BOOKS_EXTRA.put(s, ro);
        }
        for (Technique t : Technique.values()) {
            if (t == Technique.FRAGMENT) continue;
            RegistryObject<Item> ro = byId.get(new net.minecraft.resources.ResourceLocation(FridayCultivationMod.MOD_ID, "technique_book_" + t.id()));
            if (ro != null) TECHNIQUE_BOOKS_EXTRA.put(t, ro);
        }
        // 境界令牌：realm_token_<id>（照搬原模组 L639-643）
        REALM_TOKENS = new EnumMap<>(Realm.class);
        for (Realm realm : Realm.values()) {
            Realm capturedRealm = realm;
            REALM_TOKENS.put(realm, ITEMS.register("realm_token_" + realm.id(), () -> new RealmTokenItem(new Item.Properties(), capturedRealm)));
        }
        // 散仙劫波令牌：realm_token_loose_immortal_1..9（照搬原模组 L644-650）
        LOOSE_IMMORTAL_REALM_TOKENS = new LinkedHashMap<>();
        int level = 1;
        while (level <= 9) {
            int tokenLevel = level++;
            LOOSE_IMMORTAL_REALM_TOKENS.put(tokenLevel, ITEMS.register("realm_token_loose_immortal_" + tokenLevel, () -> new RealmTokenItem(new Item.Properties(), Realm.LOOSE_IMMORTAL, tokenLevel)));
        }
        // 修士生怪蛋：spawn_egg_cultivator_<id>（照搬原模组 L651-659）
        CULTIVATOR_SPAWN_EGGS = new EnumMap<>(Realm.class);
        for (Realm realm : Realm.values()) {
            Realm capturedRealm = realm;
            int[] colors = ModItems.realmEggColors(realm);
            int bg = colors[0];
            int hl = colors[1];
            Rarity eggRarity = ModItems.realmEggRarity(realm);
            CULTIVATOR_SPAWN_EGGS.put(realm, ITEMS.register("spawn_egg_cultivator_" + realm.id(), () -> new CultivatorSpawnEggItem(capturedRealm, bg, hl, new Item.Properties().rarity(eggRarity))));
        }
    }

    /** 根据法术返回对应法术书物品（照搬原模组 ModItems.spellBookItem）。 */
    public static Item spellBookItem(Spell spell) {
        if (spell == null) {
            return null;
        }
        if (spell == Spell.QI_SHIELD) {
            return SPELL_BOOK_QI_SHIELD.get();
        }
        if (spell == Spell.SPIRIT_VISION) {
            return SPELL_BOOK_SPIRIT_VISION.get();
        }
        if (spell == Spell.FIREBALL) {
            return SPELL_BOOK_FIREBALL.get();
        }
        if (spell == Spell.IMMORTAL_INCANTATION) {
            return null;
        }
        RegistryObject<Item> ro = SPELL_BOOKS_EXTRA.get(spell);
        return ro != null ? ro.get() : null;
    }

    /** 根据功法返回对应功法书物品（照搬原模组 ModItems.techniqueBookItem）。 */
    public static Item techniqueBookItem(Technique technique) {
        if (technique == null) {
            return null;
        }
        if (technique == Technique.FRAGMENT) {
            return TECHNIQUE_BOOK_FRAGMENT.get();
        }
        RegistryObject<Item> ro = TECHNIQUE_BOOKS_EXTRA.get(technique);
        return ro != null ? ro.get() : null;
    }

    /** 照搬原模组 ModItems.orderedSpellBookSpells。 */
    public static List<Spell> orderedSpellBookSpells() {
        ArrayList<Spell> result = new ArrayList<Spell>();
        for (Spell spell : Spell.values()) {
            if (ModItems.spellBookItem(spell) == null) continue;
            result.add(spell);
        }
        result.sort(SPELL_BOOK_ORDER);
        return List.copyOf(result);
    }

    /** 照搬原模组 ModItems.orderedSpellBookItems。 */
    public static List<Item> orderedSpellBookItems() {
        ArrayList<Item> result = new ArrayList<Item>();
        for (Spell spell : ModItems.orderedSpellBookSpells()) {
            Item item = ModItems.spellBookItem(spell);
            if (item == null) continue;
            result.add(item);
        }
        return List.copyOf(result);
    }

    /** 照搬原模组 ModItems.orderedTechniqueBookTechniques。 */
    public static List<Technique> orderedTechniqueBookTechniques() {
        ArrayList<Technique> result = new ArrayList<Technique>();
        for (Technique technique : Technique.values()) {
            if (ModItems.techniqueBookItem(technique) == null) continue;
            result.add(technique);
        }
        result.sort(TECHNIQUE_BOOK_ORDER);
        return List.copyOf(result);
    }

    /** 照搬原模组 ModItems.orderedTechniqueBookItems。 */
    public static List<Item> orderedTechniqueBookItems() {
        ArrayList<Item> result = new ArrayList<Item>();
        for (Technique technique : ModItems.orderedTechniqueBookTechniques()) {
            Item item = ModItems.techniqueBookItem(technique);
            if (item == null) continue;
            result.add(item);
        }
        return List.copyOf(result);
    }

    /** 照搬原模组 ModItems.realmEggColors（12 境界背景/高光色）。 */
    private static int[] realmEggColors(Realm realm) {
        return switch (realm) {
            case MORTAL -> new int[]{0x888890, 3816000};
            case QI_REFINING -> new int[]{6014919, 2055774};
            case FOUNDATION_BUILDING -> new int[]{10910008, 4861454};
            case GOLDEN_CORE -> new int[]{16767290, 8017152};
            case NASCENT_SOUL -> new int[]{16756936, 9121872};
            case SOUL_FORMATION -> new int[]{11434213, 4857472};
            case VOID_REFINING -> new int[]{7035868, 2299228};
            case BODY_INTEGRATION -> new int[]{0xD8D8E8, 0x6A6A7A};
            case MAHAYANA -> new int[]{16739160, 8002064};
            case TRIBULATION_TRANSCENDENCE -> new int[]{4865704, 656674};
            case TRUE_IMMORTAL -> new int[]{16774876, 13215850};
            case LOOSE_IMMORTAL -> new int[]{13227775, 4926056};
        };
    }

    /** 照搬原模组 ModItems.realmEggRarity（12 境界生怪蛋稀有度）。 */
    private static Rarity realmEggRarity(Realm realm) {
        return switch (realm) {
            case MORTAL, QI_REFINING -> Rarity.COMMON;
            case FOUNDATION_BUILDING, GOLDEN_CORE -> Rarity.UNCOMMON;
            case NASCENT_SOUL, SOUL_FORMATION -> Rarity.RARE;
            case VOID_REFINING, BODY_INTEGRATION, MAHAYANA, TRIBULATION_TRANSCENDENCE, TRUE_IMMORTAL, LOOSE_IMMORTAL -> Rarity.EPIC;
        };
    }
}