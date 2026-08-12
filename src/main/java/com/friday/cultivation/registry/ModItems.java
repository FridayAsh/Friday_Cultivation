/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.world.item.BlockItem
 *  net.minecraft.world.item.Item
 *  net.minecraft.world.item.Item$Properties
 *  net.minecraft.world.item.Rarity
 *  net.minecraft.world.level.block.Block
 *  net.minecraftforge.eventbus.api.IEventBus
 *  net.minecraftforge.registries.DeferredRegister
 *  net.minecraftforge.registries.ForgeRegistries
 *  net.minecraftforge.registries.IForgeRegistry
 *  net.minecraftforge.registries.RegistryObject
 */
package com.friday.cultivation.registry;

import com.friday.cultivation.cultivation.ItemTier;
import com.friday.cultivation.cultivation.alchemy.PillTier;
import com.friday.cultivation.cultivation.qi.SpiritVeinCoreTier;
import com.friday.cultivation.cultivation.realm.Realm;
import com.friday.cultivation.cultivation.spell.Spell;
import com.friday.cultivation.cultivation.technique.Technique;
import com.friday.cultivation.item.BloodBurnPillItem;
import com.friday.cultivation.item.ClearMindPillItem;
import com.friday.cultivation.item.CultivationCompendiumItem;
import com.friday.cultivation.item.CultivationPillItem;
import com.friday.cultivation.item.CultivatorSpawnEggItem;
import com.friday.cultivation.item.DifuReaperSpawnEggItem;
import com.friday.cultivation.item.DivinationCompassItem;
import com.friday.cultivation.item.DivineStridePillItem;
import com.friday.cultivation.item.FormationCompassItem;
import com.friday.cultivation.item.FormationInscriptionKnifeItem;
import com.friday.cultivation.item.FoundationMaterialItem;
import com.friday.cultivation.item.FoundationSecretItem;
import com.friday.cultivation.item.GoldenCoreMaterialItem;
import com.friday.cultivation.item.LifeCreationPillItem;
import com.friday.cultivation.item.OriginReconfigurationTokenItem;
import com.friday.cultivation.item.PillItem;
import com.friday.cultivation.item.RealmTokenItem;
import com.friday.cultivation.item.RecallPillItem;
import com.friday.cultivation.item.ReincarnationFatePlateItem;
import com.friday.cultivation.item.RejuvenationPillItem;
import com.friday.cultivation.item.SectTokenItem;
import com.friday.cultivation.item.SexChangePillItem;
import com.friday.cultivation.item.ShadowStepPillItem;
import com.friday.cultivation.item.SoulReaperTokenItem;
import com.friday.cultivation.item.SpellBookItem;
import com.friday.cultivation.item.SpiritStoneItem;
import com.friday.cultivation.item.TechniqueBookItem;
import com.friday.cultivation.item.YouthPillItem;
import com.friday.cultivation.item.weapon.ChiYanSwordItem;
import com.friday.cultivation.item.weapon.HanBingSwordItem;
import com.friday.cultivation.item.weapon.QingMuSwordItem;
import com.friday.cultivation.item.weapon.SoulHookItem;
import com.friday.cultivation.item.weapon.XuanIronSwordItem;
import com.friday.cultivation.registry.ModBlocks;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.RegistryObject;

public final class ModItems {
    public static final DeferredRegister<Item> ITEMS;
    public static final RegistryObject<Item> LOW_SPIRIT_STONE;
    public static final RegistryObject<Item> MID_SPIRIT_STONE;
    public static final RegistryObject<Item> HIGH_SPIRIT_STONE;
    public static final RegistryObject<Item> SUPREME_SPIRIT_STONE;
    public static final RegistryObject<Item> DIVINATION_COMPASS;
    public static final RegistryObject<Item> FORMATION_COMPASS;
    public static final RegistryObject<Item> FORMATION_INSCRIPTION_KNIFE;
    public static final RegistryObject<Item> CULTIVATION_COMPENDIUM;
    public static final RegistryObject<Item> INK;
    public static final RegistryObject<Item> TALISMAN_PAPER;
    public static final RegistryObject<Item> ORIGIN_RECONFIGURATION_TOKEN;
    public static final RegistryObject<Item> REINCARNATION_FATE_PLATE;
    public static final RegistryObject<Item> RECALL_PILL;
    public static final RegistryObject<Item> SOUL_HOOK;
    public static final RegistryObject<Item> SOUL_REAPER_TOKEN;
    public static final RegistryObject<Item> YOUTH_PILL;
    public static final RegistryObject<Item> SEX_CHANGE_PILL;
    public static final RegistryObject<Item> ZHUJI_DAN;
    public static final RegistryObject<Item> BLOOD_SPIRIT_PILL;
    public static final RegistryObject<Item> DAO_FOUNDATION_FRUIT;
    public static final RegistryObject<Item> FOUNDATION_SECRET;
    public static final RegistryObject<Item> JIEDAN_PILL;
    public static final RegistryObject<Item> BLOOD_JIEDAN_PILL;
    public static final RegistryObject<Item> ALL_CREATURES_TRUE_BLOOD;
    public static final RegistryObject<Item> EARTH_EVIL_QI;
    public static final RegistryObject<Item> HEAVEN_CLEAR_QI;
    public static final RegistryObject<Item> NINGZHEN_CREATION_FRUIT;
    public static final RegistryObject<Item> BLOOD_TRANSFORMATION_TALISMAN;
    public static final RegistryObject<Item> SPELL_BOOK_QI_SHIELD;
    public static final RegistryObject<Item> SPELL_BOOK_SPIRIT_VISION;
    public static final RegistryObject<Item> SPELL_BOOK_FIREBALL;
    public static final RegistryObject<Item> LEGACY_SPELL_BOOK_IMMORTAL_INCANTATION;
    public static final RegistryObject<Item> LEGACY_TECHNIQUE_BOOK_IMMORTAL_BODY;
    public static final Map<Spell, RegistryObject<Item>> SPELL_BOOKS_EXTRA;
    private static final Comparator<Spell> SPELL_BOOK_ORDER;
    public static final RegistryObject<Item> TECHNIQUE_BOOK_FRAGMENT;
    public static final Map<Technique, RegistryObject<Item>> TECHNIQUE_BOOKS_EXTRA;
    private static final Comparator<Technique> TECHNIQUE_BOOK_ORDER;
    public static final RegistryObject<Item> CUSHION;
    public static final RegistryObject<Item> BONE_BLOCK;
    public static final RegistryObject<Item> LOW_SPIRIT_STONE_ORE;
    public static final RegistryObject<Item> MID_SPIRIT_STONE_ORE;
    public static final RegistryObject<Item> HIGH_SPIRIT_STONE_ORE;
    public static final RegistryObject<Item> SUPREME_SPIRIT_STONE_ORE;
    public static final RegistryObject<Item> SPIRIT_VEIN_SPRING;
    public static final RegistryObject<Item> ALCHEMY_CORE;
    public static final RegistryObject<Item> REFINING_CORE;
    public static final RegistryObject<Item> LOW_FORMATION_CORE_PLATE;
    public static final RegistryObject<Item> MID_FORMATION_CORE_PLATE;
    public static final RegistryObject<Item> HIGH_FORMATION_CORE_PLATE;
    public static final RegistryObject<Item> SUPREME_FORMATION_CORE_PLATE;
    public static final RegistryObject<Item> IMMORTAL_FORMATION_CORE_PLATE;
    public static final RegistryObject<Item> LOW_QI_GATHERING_FLAG;
    public static final RegistryObject<Item> MID_QI_GATHERING_FLAG;
    public static final RegistryObject<Item> HIGH_QI_GATHERING_FLAG;
    public static final RegistryObject<Item> SUPREME_QI_GATHERING_FLAG;
    public static final RegistryObject<Item> IMMORTAL_QI_GATHERING_FLAG;
    public static final RegistryObject<Item> LOW_SECT_PROTECTION_FLAG;
    public static final RegistryObject<Item> MID_SECT_PROTECTION_FLAG;
    public static final RegistryObject<Item> HIGH_SECT_PROTECTION_FLAG;
    public static final RegistryObject<Item> SUPREME_SECT_PROTECTION_FLAG;
    public static final RegistryObject<Item> IMMORTAL_SECT_PROTECTION_FLAG;
    public static final RegistryObject<Item> LOW_WITHER_GROWTH_FLAG;
    public static final RegistryObject<Item> MID_WITHER_GROWTH_FLAG;
    public static final RegistryObject<Item> HIGH_WITHER_GROWTH_FLAG;
    public static final RegistryObject<Item> SUPREME_WITHER_GROWTH_FLAG;
    public static final RegistryObject<Item> IMMORTAL_WITHER_GROWTH_FLAG;
    public static final RegistryObject<Item> LOW_REJUVENATION_FLAG;
    public static final RegistryObject<Item> MID_REJUVENATION_FLAG;
    public static final RegistryObject<Item> HIGH_REJUVENATION_FLAG;
    public static final RegistryObject<Item> SUPREME_REJUVENATION_FLAG;
    public static final RegistryObject<Item> IMMORTAL_REJUVENATION_FLAG;
    public static final RegistryObject<Item> LOW_FLIGHT_BAN_FLAG;
    public static final RegistryObject<Item> MID_FLIGHT_BAN_FLAG;
    public static final RegistryObject<Item> HIGH_FLIGHT_BAN_FLAG;
    public static final RegistryObject<Item> SUPREME_FLIGHT_BAN_FLAG;
    public static final RegistryObject<Item> IMMORTAL_FLIGHT_BAN_FLAG;
    public static final RegistryObject<Item> LOW_MAZE_FLAG;
    public static final RegistryObject<Item> MID_MAZE_FLAG;
    public static final RegistryObject<Item> HIGH_MAZE_FLAG;
    public static final RegistryObject<Item> SUPREME_MAZE_FLAG;
    public static final RegistryObject<Item> IMMORTAL_MAZE_FLAG;
    public static final RegistryObject<Item> LOW_FARM_HARVEST_FLAG;
    public static final RegistryObject<Item> MID_FARM_HARVEST_FLAG;
    public static final RegistryObject<Item> HIGH_FARM_HARVEST_FLAG;
    public static final RegistryObject<Item> SUPREME_FARM_HARVEST_FLAG;
    public static final RegistryObject<Item> IMMORTAL_FARM_HARVEST_FLAG;
    public static final RegistryObject<Item> SECT_TOKEN;
    public static final RegistryObject<Item> LOW_SPIRIT_VEIN_CORE;
    public static final RegistryObject<Item> MID_SPIRIT_VEIN_CORE;
    public static final RegistryObject<Item> HIGH_SPIRIT_VEIN_CORE;
    public static final RegistryObject<Item> SUPREME_SPIRIT_VEIN_CORE;
    public static final RegistryObject<Item> IMMORTAL_SPIRIT_VEIN_CORE;
    public static final RegistryObject<Item> HERB;
    public static final RegistryObject<Item> PILL_QI_RECOVERY_LOW;
    public static final RegistryObject<Item> PILL_QI_RECOVERY_MID;
    public static final RegistryObject<Item> PILL_QI_RECOVERY_HIGH;
    public static final RegistryObject<Item> PILL_QI_RECOVERY_SUPREME;
    public static final RegistryObject<Item> PILL_QI_RECOVERY_IMMORTAL;
    public static final RegistryObject<Item> PILL_CULTIVATION_LOW;
    public static final RegistryObject<Item> PILL_CULTIVATION_MID;
    public static final RegistryObject<Item> PILL_CULTIVATION_HIGH;
    public static final RegistryObject<Item> PILL_CULTIVATION_SUPREME;
    public static final RegistryObject<Item> PILL_CULTIVATION_IMMORTAL;
    public static final RegistryObject<Item> PILL_BLOOD_BURN_LOW;
    public static final RegistryObject<Item> PILL_BLOOD_BURN_MID;
    public static final RegistryObject<Item> PILL_BLOOD_BURN_HIGH;
    public static final RegistryObject<Item> PILL_BLOOD_BURN_SUPREME;
    public static final RegistryObject<Item> PILL_BLOOD_BURN_IMMORTAL;
    public static final RegistryObject<Item> PILL_CLEAR_MIND_LOW;
    public static final RegistryObject<Item> PILL_CLEAR_MIND_MID;
    public static final RegistryObject<Item> PILL_CLEAR_MIND_HIGH;
    public static final RegistryObject<Item> PILL_CLEAR_MIND_SUPREME;
    public static final RegistryObject<Item> PILL_CLEAR_MIND_IMMORTAL;
    public static final RegistryObject<Item> PILL_REJUVENATION_LOW;
    public static final RegistryObject<Item> PILL_REJUVENATION_MID;
    public static final RegistryObject<Item> PILL_REJUVENATION_HIGH;
    public static final RegistryObject<Item> PILL_REJUVENATION_SUPREME;
    public static final RegistryObject<Item> PILL_REJUVENATION_IMMORTAL;
    public static final RegistryObject<Item> PILL_DIVINE_STRIDE_LOW;
    public static final RegistryObject<Item> PILL_DIVINE_STRIDE_MID;
    public static final RegistryObject<Item> PILL_DIVINE_STRIDE_HIGH;
    public static final RegistryObject<Item> PILL_DIVINE_STRIDE_SUPREME;
    public static final RegistryObject<Item> PILL_DIVINE_STRIDE_IMMORTAL;
    public static final RegistryObject<Item> XUAN_IRON_SWORD_LOW;
    public static final RegistryObject<Item> XUAN_IRON_SWORD_MID;
    public static final RegistryObject<Item> XUAN_IRON_SWORD_HIGH;
    public static final RegistryObject<Item> XUAN_IRON_SWORD_SUPREME;
    public static final RegistryObject<Item> XUAN_IRON_SWORD_IMMORTAL;
    public static final RegistryObject<Item> XUAN_IRON_SWORD_GREAT_EMPEROR;
    public static final RegistryObject<Item> QING_MU_SWORD_LOW;
    public static final RegistryObject<Item> QING_MU_SWORD_MID;
    public static final RegistryObject<Item> QING_MU_SWORD_HIGH;
    public static final RegistryObject<Item> QING_MU_SWORD_SUPREME;
    public static final RegistryObject<Item> QING_MU_SWORD_IMMORTAL;
    public static final RegistryObject<Item> QING_MU_SWORD_GREAT_EMPEROR;
    public static final RegistryObject<Item> CHI_YAN_SWORD_LOW;
    public static final RegistryObject<Item> CHI_YAN_SWORD_MID;
    public static final RegistryObject<Item> CHI_YAN_SWORD_HIGH;
    public static final RegistryObject<Item> CHI_YAN_SWORD_SUPREME;
    public static final RegistryObject<Item> CHI_YAN_SWORD_IMMORTAL;
    public static final RegistryObject<Item> CHI_YAN_SWORD_GREAT_EMPEROR;
    public static final RegistryObject<Item> HAN_BING_SWORD_LOW;
    public static final RegistryObject<Item> HAN_BING_SWORD_MID;
    public static final RegistryObject<Item> HAN_BING_SWORD_HIGH;
    public static final RegistryObject<Item> HAN_BING_SWORD_SUPREME;
    public static final RegistryObject<Item> HAN_BING_SWORD_IMMORTAL;
    public static final RegistryObject<Item> HAN_BING_SWORD_GREAT_EMPEROR;
    public static final Map<Realm, RegistryObject<Item>> REALM_TOKENS;
    public static final Map<Integer, RegistryObject<Item>> LOOSE_IMMORTAL_REALM_TOKENS;
    public static final Map<Realm, RegistryObject<Item>> CULTIVATOR_SPAWN_EGGS;
    public static final RegistryObject<Item> SOUL_REAPER_SPAWN_EGG;

    private static boolean hasDedicatedSpellBookRegistry(Spell spell) {
        return spell == Spell.QI_SHIELD || spell == Spell.SPIRIT_VISION || spell == Spell.FIREBALL || spell == Spell.IMMORTAL_INCANTATION;
    }

    private static boolean isRuntimeOnlySpell(Spell spell) {
        return spell == Spell.GHOST_FLIGHT;
    }

    public static Item spellBookItem(Spell spell) {
        if (spell == null) {
            return null;
        }
        if (spell == Spell.QI_SHIELD) {
            return (Item)SPELL_BOOK_QI_SHIELD.get();
        }
        if (spell == Spell.SPIRIT_VISION) {
            return (Item)SPELL_BOOK_SPIRIT_VISION.get();
        }
        if (spell == Spell.FIREBALL) {
            return (Item)SPELL_BOOK_FIREBALL.get();
        }
        if (spell == Spell.IMMORTAL_INCANTATION) {
            return null;
        }
        RegistryObject<Item> ro = SPELL_BOOKS_EXTRA.get((Object)spell);
        return ro != null ? (Item)ro.get() : null;
    }

    public static Item techniqueBookItem(Technique technique) {
        if (technique == null) {
            return null;
        }
        if (technique == Technique.FRAGMENT) {
            return (Item)TECHNIQUE_BOOK_FRAGMENT.get();
        }
        RegistryObject<Item> ro = TECHNIQUE_BOOKS_EXTRA.get((Object)technique);
        return ro != null ? (Item)ro.get() : null;
    }

    public static List<Spell> orderedSpellBookSpells() {
        ArrayList<Spell> result = new ArrayList<Spell>();
        for (Spell spell : Spell.values()) {
            if (ModItems.spellBookItem(spell) == null) continue;
            result.add(spell);
        }
        result.sort(SPELL_BOOK_ORDER);
        return List.copyOf(result);
    }

    public static List<Item> orderedSpellBookItems() {
        ArrayList<Item> result = new ArrayList<Item>();
        for (Spell spell : ModItems.orderedSpellBookSpells()) {
            Item item = ModItems.spellBookItem(spell);
            if (item == null) continue;
            result.add(item);
        }
        return List.copyOf(result);
    }

    public static List<Technique> orderedTechniqueBookTechniques() {
        ArrayList<Technique> result = new ArrayList<Technique>();
        for (Technique technique : Technique.values()) {
            if (ModItems.techniqueBookItem(technique) == null) continue;
            result.add(technique);
        }
        result.sort(TECHNIQUE_BOOK_ORDER);
        return List.copyOf(result);
    }

    public static List<Item> orderedTechniqueBookItems() {
        ArrayList<Item> result = new ArrayList<Item>();
        for (Technique technique : ModItems.orderedTechniqueBookTechniques()) {
            Item item = ModItems.techniqueBookItem(technique);
            if (item == null) continue;
            result.add(item);
        }
        return List.copyOf(result);
    }

    private static int[] realmEggColors(Realm realm) {
        int[] nArray;
        switch (realm) {
            default: {
                throw new IncompatibleClassChangeError();
            }
            case MORTAL: {
                int[] nArray2 = new int[2];
                nArray2[0] = 0x888890;
                nArray = nArray2;
                nArray2[1] = 3816000;
                break;
            }
            case BODY_TEMPERING: {
                int[] nArrayBody = new int[2];
                nArrayBody[0] = 0x9A8A6A;
                nArray = nArrayBody;
                nArrayBody[1] = 3682366;
                break;
            }
            case QI_REFINING: {
                int[] nArray3 = new int[2];
                nArray3[0] = 6014919;
                nArray = nArray3;
                nArray3[1] = 2055774;
                break;
            }
            case FOUNDATION_BUILDING: {
                int[] nArray4 = new int[2];
                nArray4[0] = 10910008;
                nArray = nArray4;
                nArray4[1] = 4861454;
                break;
            }
            case GOLDEN_CORE: {
                int[] nArray5 = new int[2];
                nArray5[0] = 16767290;
                nArray = nArray5;
                nArray5[1] = 8017152;
                break;
            }
            case NASCENT_SOUL: {
                int[] nArray6 = new int[2];
                nArray6[0] = 16756936;
                nArray = nArray6;
                nArray6[1] = 9121872;
                break;
            }
            case SOUL_FORMATION: {
                int[] nArray7 = new int[2];
                nArray7[0] = 11434213;
                nArray = nArray7;
                nArray7[1] = 4857472;
                break;
            }
            case VOID_REFINING: {
                int[] nArray8 = new int[2];
                nArray8[0] = 7035868;
                nArray = nArray8;
                nArray8[1] = 2299228;
                break;
            }
            case BODY_INTEGRATION: {
                int[] nArray9 = new int[2];
                nArray9[0] = 0xD8D8E8;
                nArray = nArray9;
                nArray9[1] = 0x6A6A7A;
                break;
            }
            case MAHAYANA: {
                int[] nArray10 = new int[2];
                nArray10[0] = 16739160;
                nArray = nArray10;
                nArray10[1] = 8002064;
                break;
            }
            case TRIBULATION_TRANSCENDENCE: {
                int[] nArray11 = new int[2];
                nArray11[0] = 4865704;
                nArray = nArray11;
                nArray11[1] = 656674;
                break;
            }
            case TRUE_IMMORTAL: {
                int[] nArray12 = new int[2];
                nArray12[0] = 16774876;
                nArray = nArray12;
                nArray12[1] = 13215850;
                break;
            }
            case LOOSE_IMMORTAL: {
                int[] nArray13 = new int[2];
                nArray13[0] = 13227775;
                nArray = nArray13;
                nArray13[1] = 4926056;
                break;
            }
            case GREAT_EMPEROR: {
                int[] nArray14 = new int[2];
                nArray14[0] = 16755200;
                nArray = nArray14;
                nArray14[1] = 32896;
            }
        }
        return nArray;
    }

    private static Rarity realmEggRarity(Realm realm) {
        return switch (realm) {
            default -> throw new IncompatibleClassChangeError();
            case MORTAL, BODY_TEMPERING, QI_REFINING -> Rarity.COMMON;
            case FOUNDATION_BUILDING, GOLDEN_CORE -> Rarity.UNCOMMON;
            case NASCENT_SOUL, SOUL_FORMATION -> Rarity.RARE;
            case VOID_REFINING, BODY_INTEGRATION, MAHAYANA, TRIBULATION_TRANSCENDENCE, TRUE_IMMORTAL, LOOSE_IMMORTAL, GREAT_EMPEROR -> Rarity.EPIC;
        };
    }

    private ModItems() {
    }

    public static void register(IEventBus bus) {
        ITEMS.register(bus);
    }

    private static /* synthetic */ Item lambda$static$144(Realm realm) {
        return new RealmTokenItem(new Item.Properties(), realm);
    }

    static {
        Rarity rarity;
        String id;
        ITEMS = DeferredRegister.create((IForgeRegistry)ForgeRegistries.ITEMS, (String)"friday_cultivation");
        LOW_SPIRIT_STONE = ITEMS.register("low_spirit_stone", () -> new SpiritStoneItem(new Item.Properties(), ItemTier.LOW, 10));
        MID_SPIRIT_STONE = ITEMS.register("mid_spirit_stone", () -> new SpiritStoneItem(new Item.Properties().rarity(Rarity.UNCOMMON), ItemTier.MID, 100));
        HIGH_SPIRIT_STONE = ITEMS.register("high_spirit_stone", () -> new SpiritStoneItem(new Item.Properties().rarity(Rarity.RARE), ItemTier.HIGH, 1000));
        SUPREME_SPIRIT_STONE = ITEMS.register("supreme_spirit_stone", () -> new SpiritStoneItem(new Item.Properties().rarity(Rarity.EPIC), ItemTier.SUPREME, 10000));
        DIVINATION_COMPASS = ITEMS.register("divination_compass", () -> new DivinationCompassItem(new Item.Properties().stacksTo(1)));
        FORMATION_COMPASS = ITEMS.register("formation_compass", () -> new FormationCompassItem(new Item.Properties().stacksTo(1).rarity(Rarity.UNCOMMON)));
        FORMATION_INSCRIPTION_KNIFE = ITEMS.register("formation_inscription_knife", () -> new FormationInscriptionKnifeItem(new Item.Properties().stacksTo(1).durability(384).rarity(Rarity.UNCOMMON)));
        CULTIVATION_COMPENDIUM = ITEMS.register("cultivation_compendium", () -> new CultivationCompendiumItem(new Item.Properties().stacksTo(1).rarity(Rarity.UNCOMMON)));
        INK = ITEMS.register("ink", () -> new Item(new Item.Properties()));
        TALISMAN_PAPER = ITEMS.register("talisman_paper", () -> new Item(new Item.Properties()));
        ORIGIN_RECONFIGURATION_TOKEN = ITEMS.register("origin_reconfiguration_token", () -> new OriginReconfigurationTokenItem(new Item.Properties().stacksTo(1).rarity(Rarity.EPIC)));
        REINCARNATION_FATE_PLATE = ITEMS.register("reincarnation_fate_plate", () -> new ReincarnationFatePlateItem(new Item.Properties().stacksTo(1).rarity(Rarity.EPIC)));
        RECALL_PILL = ITEMS.register("recall_pill", () -> new RecallPillItem(new Item.Properties().stacksTo(1).rarity(Rarity.EPIC)));
        SOUL_HOOK = ITEMS.register("soul_hook", () -> new SoulHookItem(new Item.Properties().stacksTo(1).rarity(Rarity.EPIC).fireResistant()));
        SOUL_REAPER_TOKEN = ITEMS.register("soul_reaper_token", () -> new SoulReaperTokenItem(new Item.Properties().stacksTo(1).rarity(Rarity.RARE).fireResistant()));
        YOUTH_PILL = ITEMS.register("youth_pill", () -> new YouthPillItem(new Item.Properties().rarity(Rarity.RARE)));
        SEX_CHANGE_PILL = ITEMS.register("sex_change_pill", () -> new SexChangePillItem(new Item.Properties().rarity(Rarity.UNCOMMON)));
        ZHUJI_DAN = ITEMS.register("zhuji_dan", () -> new FoundationMaterialItem(new Item.Properties().rarity(Rarity.UNCOMMON), FoundationMaterialItem.Kind.ZHUJI_DAN));
        BLOOD_SPIRIT_PILL = ITEMS.register("blood_spirit_pill", () -> new FoundationMaterialItem(new Item.Properties().rarity(Rarity.RARE), FoundationMaterialItem.Kind.BLOOD_PILL));
        DAO_FOUNDATION_FRUIT = ITEMS.register("dao_foundation_fruit", () -> new FoundationMaterialItem(new Item.Properties().rarity(Rarity.EPIC), FoundationMaterialItem.Kind.DAO_FRUIT));
        FOUNDATION_SECRET = ITEMS.register("foundation_secret", () -> new FoundationSecretItem(new Item.Properties().stacksTo(1).rarity(Rarity.EPIC)));
        JIEDAN_PILL = ITEMS.register("jiedan_pill", () -> new GoldenCoreMaterialItem(new Item.Properties().rarity(Rarity.RARE), GoldenCoreMaterialItem.Kind.JIEDAN_PILL));
        BLOOD_JIEDAN_PILL = ITEMS.register("blood_jiedan_pill", () -> new GoldenCoreMaterialItem(new Item.Properties().rarity(Rarity.EPIC), GoldenCoreMaterialItem.Kind.BLOOD_JIEDAN_PILL));
        ALL_CREATURES_TRUE_BLOOD = ITEMS.register("all_creatures_true_blood", () -> new GoldenCoreMaterialItem(new Item.Properties().rarity(Rarity.EPIC), GoldenCoreMaterialItem.Kind.TRUE_BLOOD));
        EARTH_EVIL_QI = ITEMS.register("earth_evil_qi", () -> new GoldenCoreMaterialItem(new Item.Properties().rarity(Rarity.RARE), GoldenCoreMaterialItem.Kind.EARTH_EVIL_QI));
        HEAVEN_CLEAR_QI = ITEMS.register("heaven_clear_qi", () -> new GoldenCoreMaterialItem(new Item.Properties().rarity(Rarity.EPIC), GoldenCoreMaterialItem.Kind.HEAVEN_CLEAR_QI));
        NINGZHEN_CREATION_FRUIT = ITEMS.register("ningzhen_creation_fruit", () -> new GoldenCoreMaterialItem(new Item.Properties().rarity(Rarity.EPIC), GoldenCoreMaterialItem.Kind.CREATION_FRUIT));
        BLOOD_TRANSFORMATION_TALISMAN = ITEMS.register("blood_transformation_talisman", () -> new GoldenCoreMaterialItem(new Item.Properties().stacksTo(16).rarity(Rarity.EPIC), GoldenCoreMaterialItem.Kind.BLOOD_TRANSFORMATION_TALISMAN));
        SPELL_BOOK_QI_SHIELD = ITEMS.register("spell_book_qi_shield", () -> new SpellBookItem(new Item.Properties().stacksTo(16).rarity(Rarity.COMMON), Spell.QI_SHIELD));
        SPELL_BOOK_SPIRIT_VISION = ITEMS.register("spell_book_spirit_vision", () -> new SpellBookItem(new Item.Properties().stacksTo(16).rarity(Rarity.UNCOMMON), Spell.SPIRIT_VISION));
        SPELL_BOOK_FIREBALL = ITEMS.register("spell_book_fireball", () -> new SpellBookItem(new Item.Properties().stacksTo(16).rarity(Rarity.UNCOMMON), Spell.FIREBALL));
        LEGACY_SPELL_BOOK_IMMORTAL_INCANTATION = ITEMS.register("spell_book_immortal_incantation", () -> new TechniqueBookItem(new Item.Properties().stacksTo(16).rarity(Rarity.EPIC), Technique.IMMORTAL_INCANTATION));
        LEGACY_TECHNIQUE_BOOK_IMMORTAL_BODY = ITEMS.register("technique_book_immortal_body", () -> new TechniqueBookItem(new Item.Properties().stacksTo(16).rarity(Rarity.EPIC), Technique.IMMORTAL_INCANTATION));
        SPELL_BOOKS_EXTRA = new LinkedHashMap<Spell, RegistryObject<Item>>();
        SPELL_BOOK_ORDER = Comparator.comparingInt((Spell spell) -> spell.tier().ordinal()).thenComparingInt(Enum::ordinal);
        for (Spell spell2 : Spell.values()) {
            if (ModItems.hasDedicatedSpellBookRegistry(spell2) || ModItems.isRuntimeOnlySpell(spell2)) continue;
            id = "spell_book_" + spell2.id();
            rarity = switch (spell2.tier()) {
                default -> throw new IncompatibleClassChangeError();
                case LOW -> Rarity.COMMON;
                case MID -> Rarity.UNCOMMON;
                case HIGH -> Rarity.RARE;
                case SUPREME -> Rarity.EPIC;
                case IMMORTAL -> Rarity.EPIC;
                case GREAT_EMPEROR -> Rarity.EPIC;
            };
            final Spell spellFinal = spell2;
            final Rarity rarityFinal = rarity;
            SPELL_BOOKS_EXTRA.put(spell2, ITEMS.register(id, () -> new SpellBookItem(new Item.Properties().stacksTo(16).rarity(rarityFinal), spellFinal)));
        }
        TECHNIQUE_BOOK_FRAGMENT = ITEMS.register("technique_book_fragment", () -> new TechniqueBookItem(new Item.Properties().stacksTo(16).rarity(Rarity.UNCOMMON), Technique.FRAGMENT));
        TECHNIQUE_BOOKS_EXTRA = new EnumMap<Technique, RegistryObject<Item>>(Technique.class);
        TECHNIQUE_BOOK_ORDER = Comparator.comparingInt((Technique technique) -> technique.tier().ordinal()).thenComparingInt(Enum::ordinal);
        for (Technique tech : Technique.values()) {
            if (tech == Technique.FRAGMENT) continue;
            id = "technique_book_" + tech.id();
            rarity = switch (tech.tier()) {
                default -> throw new IncompatibleClassChangeError();
                case LOW -> Rarity.COMMON;
                case MID -> Rarity.UNCOMMON;
                case HIGH -> Rarity.RARE;
                case SUPREME -> Rarity.EPIC;
                case IMMORTAL -> Rarity.EPIC;
                case GREAT_EMPEROR -> Rarity.EPIC;
            };
            final Technique techFinal = tech;
            final Rarity rarityFinal = rarity;
            TECHNIQUE_BOOKS_EXTRA.put(tech, ITEMS.register(id, () -> new TechniqueBookItem(new Item.Properties().stacksTo(16).rarity(rarityFinal), techFinal)));
        }
        CUSHION = ITEMS.register("cushion", () -> new BlockItem((Block)ModBlocks.CUSHION.get(), new Item.Properties()));
        BONE_BLOCK = ITEMS.register("bone_block", () -> new BlockItem((Block)ModBlocks.BONE_BLOCK.get(), new Item.Properties()));
        LOW_SPIRIT_STONE_ORE = ITEMS.register("low_spirit_stone_ore", () -> new BlockItem((Block)ModBlocks.LOW_SPIRIT_STONE_ORE.get(), new Item.Properties()));
        MID_SPIRIT_STONE_ORE = ITEMS.register("mid_spirit_stone_ore", () -> new BlockItem((Block)ModBlocks.MID_SPIRIT_STONE_ORE.get(), new Item.Properties()));
        HIGH_SPIRIT_STONE_ORE = ITEMS.register("high_spirit_stone_ore", () -> new BlockItem((Block)ModBlocks.HIGH_SPIRIT_STONE_ORE.get(), new Item.Properties()));
        SUPREME_SPIRIT_STONE_ORE = ITEMS.register("supreme_spirit_stone_ore", () -> new BlockItem((Block)ModBlocks.SUPREME_SPIRIT_STONE_ORE.get(), new Item.Properties()));
        SPIRIT_VEIN_SPRING = ITEMS.register("spirit_vein_spring", () -> new BlockItem((Block)ModBlocks.SPIRIT_VEIN_SPRING.get(), new Item.Properties().rarity(Rarity.RARE)));
        ALCHEMY_CORE = ITEMS.register("alchemy_core", () -> new BlockItem((Block)ModBlocks.ALCHEMY_CORE.get(), new Item.Properties().rarity(Rarity.UNCOMMON)));
        REFINING_CORE = ITEMS.register("refining_core", () -> new BlockItem((Block)ModBlocks.REFINING_CORE.get(), new Item.Properties().rarity(Rarity.UNCOMMON)));
        LOW_FORMATION_CORE_PLATE = ITEMS.register("low_formation_core_plate", () -> new BlockItem((Block)ModBlocks.LOW_FORMATION_CORE_PLATE.get(), new Item.Properties().rarity(Rarity.UNCOMMON)));
        MID_FORMATION_CORE_PLATE = ITEMS.register("mid_formation_core_plate", () -> new BlockItem((Block)ModBlocks.MID_FORMATION_CORE_PLATE.get(), new Item.Properties().rarity(Rarity.RARE)));
        HIGH_FORMATION_CORE_PLATE = ITEMS.register("high_formation_core_plate", () -> new BlockItem((Block)ModBlocks.HIGH_FORMATION_CORE_PLATE.get(), new Item.Properties().rarity(Rarity.RARE)));
        SUPREME_FORMATION_CORE_PLATE = ITEMS.register("supreme_formation_core_plate", () -> new BlockItem((Block)ModBlocks.SUPREME_FORMATION_CORE_PLATE.get(), new Item.Properties().rarity(Rarity.EPIC)));
        IMMORTAL_FORMATION_CORE_PLATE = ITEMS.register("immortal_formation_core_plate", () -> new BlockItem((Block)ModBlocks.IMMORTAL_FORMATION_CORE_PLATE.get(), new Item.Properties().rarity(Rarity.EPIC)));
        LOW_QI_GATHERING_FLAG = ITEMS.register("low_qi_gathering_flag", () -> new BlockItem((Block)ModBlocks.LOW_QI_GATHERING_FLAG.get(), new Item.Properties().rarity(Rarity.COMMON)));
        MID_QI_GATHERING_FLAG = ITEMS.register("mid_qi_gathering_flag", () -> new BlockItem((Block)ModBlocks.MID_QI_GATHERING_FLAG.get(), new Item.Properties().rarity(Rarity.UNCOMMON)));
        HIGH_QI_GATHERING_FLAG = ITEMS.register("high_qi_gathering_flag", () -> new BlockItem((Block)ModBlocks.HIGH_QI_GATHERING_FLAG.get(), new Item.Properties().rarity(Rarity.RARE)));
        SUPREME_QI_GATHERING_FLAG = ITEMS.register("supreme_qi_gathering_flag", () -> new BlockItem((Block)ModBlocks.SUPREME_QI_GATHERING_FLAG.get(), new Item.Properties().rarity(Rarity.EPIC)));
        IMMORTAL_QI_GATHERING_FLAG = ITEMS.register("immortal_qi_gathering_flag", () -> new BlockItem((Block)ModBlocks.IMMORTAL_QI_GATHERING_FLAG.get(), new Item.Properties().rarity(Rarity.EPIC)));
        LOW_SECT_PROTECTION_FLAG = ITEMS.register("low_sect_protection_flag", () -> new BlockItem((Block)ModBlocks.LOW_SECT_PROTECTION_FLAG.get(), new Item.Properties().rarity(Rarity.COMMON)));
        MID_SECT_PROTECTION_FLAG = ITEMS.register("mid_sect_protection_flag", () -> new BlockItem((Block)ModBlocks.MID_SECT_PROTECTION_FLAG.get(), new Item.Properties().rarity(Rarity.UNCOMMON)));
        HIGH_SECT_PROTECTION_FLAG = ITEMS.register("high_sect_protection_flag", () -> new BlockItem((Block)ModBlocks.HIGH_SECT_PROTECTION_FLAG.get(), new Item.Properties().rarity(Rarity.RARE)));
        SUPREME_SECT_PROTECTION_FLAG = ITEMS.register("supreme_sect_protection_flag", () -> new BlockItem((Block)ModBlocks.SUPREME_SECT_PROTECTION_FLAG.get(), new Item.Properties().rarity(Rarity.EPIC)));
        IMMORTAL_SECT_PROTECTION_FLAG = ITEMS.register("immortal_sect_protection_flag", () -> new BlockItem((Block)ModBlocks.IMMORTAL_SECT_PROTECTION_FLAG.get(), new Item.Properties().rarity(Rarity.EPIC)));
        LOW_WITHER_GROWTH_FLAG = ITEMS.register("low_wither_growth_flag", () -> new BlockItem((Block)ModBlocks.LOW_WITHER_GROWTH_FLAG.get(), new Item.Properties().rarity(Rarity.COMMON)));
        MID_WITHER_GROWTH_FLAG = ITEMS.register("mid_wither_growth_flag", () -> new BlockItem((Block)ModBlocks.MID_WITHER_GROWTH_FLAG.get(), new Item.Properties().rarity(Rarity.UNCOMMON)));
        HIGH_WITHER_GROWTH_FLAG = ITEMS.register("high_wither_growth_flag", () -> new BlockItem((Block)ModBlocks.HIGH_WITHER_GROWTH_FLAG.get(), new Item.Properties().rarity(Rarity.RARE)));
        SUPREME_WITHER_GROWTH_FLAG = ITEMS.register("supreme_wither_growth_flag", () -> new BlockItem((Block)ModBlocks.SUPREME_WITHER_GROWTH_FLAG.get(), new Item.Properties().rarity(Rarity.EPIC)));
        IMMORTAL_WITHER_GROWTH_FLAG = ITEMS.register("immortal_wither_growth_flag", () -> new BlockItem((Block)ModBlocks.IMMORTAL_WITHER_GROWTH_FLAG.get(), new Item.Properties().rarity(Rarity.EPIC)));
        LOW_REJUVENATION_FLAG = ITEMS.register("low_rejuvenation_flag", () -> new BlockItem((Block)ModBlocks.LOW_REJUVENATION_FLAG.get(), new Item.Properties().rarity(Rarity.COMMON)));
        MID_REJUVENATION_FLAG = ITEMS.register("mid_rejuvenation_flag", () -> new BlockItem((Block)ModBlocks.MID_REJUVENATION_FLAG.get(), new Item.Properties().rarity(Rarity.UNCOMMON)));
        HIGH_REJUVENATION_FLAG = ITEMS.register("high_rejuvenation_flag", () -> new BlockItem((Block)ModBlocks.HIGH_REJUVENATION_FLAG.get(), new Item.Properties().rarity(Rarity.RARE)));
        SUPREME_REJUVENATION_FLAG = ITEMS.register("supreme_rejuvenation_flag", () -> new BlockItem((Block)ModBlocks.SUPREME_REJUVENATION_FLAG.get(), new Item.Properties().rarity(Rarity.EPIC)));
        IMMORTAL_REJUVENATION_FLAG = ITEMS.register("immortal_rejuvenation_flag", () -> new BlockItem((Block)ModBlocks.IMMORTAL_REJUVENATION_FLAG.get(), new Item.Properties().rarity(Rarity.EPIC)));
        LOW_FLIGHT_BAN_FLAG = ITEMS.register("low_flight_ban_flag", () -> new BlockItem((Block)ModBlocks.LOW_FLIGHT_BAN_FLAG.get(), new Item.Properties().rarity(Rarity.COMMON)));
        MID_FLIGHT_BAN_FLAG = ITEMS.register("mid_flight_ban_flag", () -> new BlockItem((Block)ModBlocks.MID_FLIGHT_BAN_FLAG.get(), new Item.Properties().rarity(Rarity.UNCOMMON)));
        HIGH_FLIGHT_BAN_FLAG = ITEMS.register("high_flight_ban_flag", () -> new BlockItem((Block)ModBlocks.HIGH_FLIGHT_BAN_FLAG.get(), new Item.Properties().rarity(Rarity.RARE)));
        SUPREME_FLIGHT_BAN_FLAG = ITEMS.register("supreme_flight_ban_flag", () -> new BlockItem((Block)ModBlocks.SUPREME_FLIGHT_BAN_FLAG.get(), new Item.Properties().rarity(Rarity.EPIC)));
        IMMORTAL_FLIGHT_BAN_FLAG = ITEMS.register("immortal_flight_ban_flag", () -> new BlockItem((Block)ModBlocks.IMMORTAL_FLIGHT_BAN_FLAG.get(), new Item.Properties().rarity(Rarity.EPIC)));
        LOW_MAZE_FLAG = ITEMS.register("low_maze_flag", () -> new BlockItem((Block)ModBlocks.LOW_MAZE_FLAG.get(), new Item.Properties().rarity(Rarity.COMMON)));
        MID_MAZE_FLAG = ITEMS.register("mid_maze_flag", () -> new BlockItem((Block)ModBlocks.MID_MAZE_FLAG.get(), new Item.Properties().rarity(Rarity.UNCOMMON)));
        HIGH_MAZE_FLAG = ITEMS.register("high_maze_flag", () -> new BlockItem((Block)ModBlocks.HIGH_MAZE_FLAG.get(), new Item.Properties().rarity(Rarity.RARE)));
        SUPREME_MAZE_FLAG = ITEMS.register("supreme_maze_flag", () -> new BlockItem((Block)ModBlocks.SUPREME_MAZE_FLAG.get(), new Item.Properties().rarity(Rarity.EPIC)));
        IMMORTAL_MAZE_FLAG = ITEMS.register("immortal_maze_flag", () -> new BlockItem((Block)ModBlocks.IMMORTAL_MAZE_FLAG.get(), new Item.Properties().rarity(Rarity.EPIC)));
        LOW_FARM_HARVEST_FLAG = ITEMS.register("low_farm_harvest_flag", () -> new BlockItem((Block)ModBlocks.LOW_FARM_HARVEST_FLAG.get(), new Item.Properties().rarity(Rarity.COMMON)));
        MID_FARM_HARVEST_FLAG = ITEMS.register("mid_farm_harvest_flag", () -> new BlockItem((Block)ModBlocks.MID_FARM_HARVEST_FLAG.get(), new Item.Properties().rarity(Rarity.UNCOMMON)));
        HIGH_FARM_HARVEST_FLAG = ITEMS.register("high_farm_harvest_flag", () -> new BlockItem((Block)ModBlocks.HIGH_FARM_HARVEST_FLAG.get(), new Item.Properties().rarity(Rarity.RARE)));
        SUPREME_FARM_HARVEST_FLAG = ITEMS.register("supreme_farm_harvest_flag", () -> new BlockItem((Block)ModBlocks.SUPREME_FARM_HARVEST_FLAG.get(), new Item.Properties().rarity(Rarity.EPIC)));
        IMMORTAL_FARM_HARVEST_FLAG = ITEMS.register("immortal_farm_harvest_flag", () -> new BlockItem((Block)ModBlocks.IMMORTAL_FARM_HARVEST_FLAG.get(), new Item.Properties().rarity(Rarity.EPIC)));
        SECT_TOKEN = ITEMS.register("sect_token", () -> new SectTokenItem(new Item.Properties().stacksTo(64).rarity(Rarity.UNCOMMON)));
        LOW_SPIRIT_VEIN_CORE = ITEMS.register("low_spirit_vein_core", () -> new BlockItem((Block)ModBlocks.LOW_SPIRIT_VEIN_CORE.get(), new Item.Properties().rarity(SpiritVeinCoreTier.LOW.rarity())));
        MID_SPIRIT_VEIN_CORE = ITEMS.register("mid_spirit_vein_core", () -> new BlockItem((Block)ModBlocks.MID_SPIRIT_VEIN_CORE.get(), new Item.Properties().rarity(SpiritVeinCoreTier.MID.rarity())));
        HIGH_SPIRIT_VEIN_CORE = ITEMS.register("high_spirit_vein_core", () -> new BlockItem((Block)ModBlocks.HIGH_SPIRIT_VEIN_CORE.get(), new Item.Properties().rarity(SpiritVeinCoreTier.HIGH.rarity())));
        SUPREME_SPIRIT_VEIN_CORE = ITEMS.register("supreme_spirit_vein_core", () -> new BlockItem((Block)ModBlocks.SUPREME_SPIRIT_VEIN_CORE.get(), new Item.Properties().rarity(SpiritVeinCoreTier.SUPREME.rarity())));
        IMMORTAL_SPIRIT_VEIN_CORE = ITEMS.register("immortal_spirit_vein_core", () -> new BlockItem((Block)ModBlocks.IMMORTAL_SPIRIT_VEIN_CORE.get(), new Item.Properties().rarity(SpiritVeinCoreTier.IMMORTAL.rarity())));
        HERB = ITEMS.register("herb", () -> new BlockItem((Block)ModBlocks.HERB.get(), new Item.Properties()));
        PILL_QI_RECOVERY_LOW = ITEMS.register("pill_qi_recovery_low", () -> new PillItem(new Item.Properties().rarity(Rarity.COMMON), PillTier.LOW, 10));
        PILL_QI_RECOVERY_MID = ITEMS.register("pill_qi_recovery_mid", () -> new PillItem(new Item.Properties().rarity(Rarity.UNCOMMON), PillTier.MID, 100));
        PILL_QI_RECOVERY_HIGH = ITEMS.register("pill_qi_recovery_high", () -> new PillItem(new Item.Properties().rarity(Rarity.RARE), PillTier.HIGH, 1000));
        PILL_QI_RECOVERY_SUPREME = ITEMS.register("pill_qi_recovery_supreme", () -> new PillItem(new Item.Properties().rarity(Rarity.EPIC), PillTier.SUPREME, 10000));
        PILL_QI_RECOVERY_IMMORTAL = ITEMS.register("pill_qi_recovery_immortal", () -> new PillItem(new Item.Properties().rarity(Rarity.EPIC), PillTier.IMMORTAL, -1));
        PILL_CULTIVATION_LOW = ITEMS.register("pill_cultivation_low", () -> new CultivationPillItem(new Item.Properties().rarity(Rarity.COMMON), PillTier.LOW, 10));
        PILL_CULTIVATION_MID = ITEMS.register("pill_cultivation_mid", () -> new CultivationPillItem(new Item.Properties().rarity(Rarity.UNCOMMON), PillTier.MID, 100));
        PILL_CULTIVATION_HIGH = ITEMS.register("pill_cultivation_high", () -> new CultivationPillItem(new Item.Properties().rarity(Rarity.RARE), PillTier.HIGH, 1000));
        PILL_CULTIVATION_SUPREME = ITEMS.register("pill_cultivation_supreme", () -> new CultivationPillItem(new Item.Properties().rarity(Rarity.EPIC), PillTier.SUPREME, 10000));
        PILL_CULTIVATION_IMMORTAL = ITEMS.register("pill_cultivation_immortal", () -> new CultivationPillItem(new Item.Properties().rarity(Rarity.EPIC), PillTier.IMMORTAL, -1));
        PILL_BLOOD_BURN_LOW = ITEMS.register("pill_blood_burn_low", () -> new BloodBurnPillItem(new Item.Properties().rarity(Rarity.COMMON), PillTier.LOW));
        PILL_BLOOD_BURN_MID = ITEMS.register("pill_blood_burn_mid", () -> new BloodBurnPillItem(new Item.Properties().rarity(Rarity.UNCOMMON), PillTier.MID));
        PILL_BLOOD_BURN_HIGH = ITEMS.register("pill_blood_burn_high", () -> new BloodBurnPillItem(new Item.Properties().rarity(Rarity.RARE), PillTier.HIGH));
        PILL_BLOOD_BURN_SUPREME = ITEMS.register("pill_blood_burn_supreme", () -> new BloodBurnPillItem(new Item.Properties().rarity(Rarity.EPIC), PillTier.SUPREME));
        PILL_BLOOD_BURN_IMMORTAL = ITEMS.register("pill_blood_burn_immortal", () -> new BloodBurnPillItem(new Item.Properties().rarity(Rarity.EPIC), PillTier.IMMORTAL));
        PILL_CLEAR_MIND_LOW = ITEMS.register("pill_clear_mind_low", () -> new ClearMindPillItem(new Item.Properties().rarity(Rarity.COMMON), PillTier.LOW));
        PILL_CLEAR_MIND_MID = ITEMS.register("pill_clear_mind_mid", () -> new ClearMindPillItem(new Item.Properties().rarity(Rarity.UNCOMMON), PillTier.MID));
        PILL_CLEAR_MIND_HIGH = ITEMS.register("pill_clear_mind_high", () -> new ClearMindPillItem(new Item.Properties().rarity(Rarity.RARE), PillTier.HIGH));
        PILL_CLEAR_MIND_SUPREME = ITEMS.register("pill_clear_mind_supreme", () -> new ClearMindPillItem(new Item.Properties().rarity(Rarity.EPIC), PillTier.SUPREME));
        PILL_CLEAR_MIND_IMMORTAL = ITEMS.register("pill_clear_mind_immortal", () -> new ClearMindPillItem(new Item.Properties().rarity(Rarity.EPIC), PillTier.IMMORTAL));
        PILL_REJUVENATION_LOW = ITEMS.register("pill_rejuvenation_low", () -> new RejuvenationPillItem(new Item.Properties().rarity(Rarity.COMMON), PillTier.LOW));
        PILL_REJUVENATION_MID = ITEMS.register("pill_rejuvenation_mid", () -> new RejuvenationPillItem(new Item.Properties().rarity(Rarity.UNCOMMON), PillTier.MID));
        PILL_REJUVENATION_HIGH = ITEMS.register("pill_rejuvenation_high", () -> new RejuvenationPillItem(new Item.Properties().rarity(Rarity.RARE), PillTier.HIGH));
        PILL_REJUVENATION_SUPREME = ITEMS.register("pill_rejuvenation_supreme", () -> new RejuvenationPillItem(new Item.Properties().rarity(Rarity.EPIC), PillTier.SUPREME));
        PILL_REJUVENATION_IMMORTAL = ITEMS.register("pill_rejuvenation_immortal", () -> new LifeCreationPillItem(new Item.Properties().rarity(Rarity.EPIC)));
        PILL_DIVINE_STRIDE_LOW = ITEMS.register("pill_divine_stride_low", () -> new DivineStridePillItem(new Item.Properties().rarity(Rarity.COMMON), PillTier.LOW));
        PILL_DIVINE_STRIDE_MID = ITEMS.register("pill_divine_stride_mid", () -> new DivineStridePillItem(new Item.Properties().rarity(Rarity.UNCOMMON), PillTier.MID));
        PILL_DIVINE_STRIDE_HIGH = ITEMS.register("pill_divine_stride_high", () -> new DivineStridePillItem(new Item.Properties().rarity(Rarity.RARE), PillTier.HIGH));
        PILL_DIVINE_STRIDE_SUPREME = ITEMS.register("pill_divine_stride_supreme", () -> new DivineStridePillItem(new Item.Properties().rarity(Rarity.EPIC), PillTier.SUPREME));
        PILL_DIVINE_STRIDE_IMMORTAL = ITEMS.register("pill_divine_stride_immortal", () -> new ShadowStepPillItem(new Item.Properties().rarity(Rarity.EPIC)));
        XUAN_IRON_SWORD_LOW = ITEMS.register("xuan_iron_sword_low", () -> new XuanIronSwordItem(ItemTier.LOW, 10, 10));
        XUAN_IRON_SWORD_MID = ITEMS.register("xuan_iron_sword_mid", () -> new XuanIronSwordItem(ItemTier.MID, 12, 20));
        XUAN_IRON_SWORD_HIGH = ITEMS.register("xuan_iron_sword_high", () -> new XuanIronSwordItem(ItemTier.HIGH, 15, 30));
        XUAN_IRON_SWORD_SUPREME = ITEMS.register("xuan_iron_sword_supreme", () -> new XuanIronSwordItem(ItemTier.SUPREME, 17, 40));
        XUAN_IRON_SWORD_IMMORTAL = ITEMS.register("xuan_iron_sword_immortal", () -> new XuanIronSwordItem(ItemTier.IMMORTAL, 20, 50));
        XUAN_IRON_SWORD_GREAT_EMPEROR = ITEMS.register("xuan_iron_sword_great_emperor", () -> new XuanIronSwordItem(ItemTier.GREAT_EMPEROR, 30, 100));
        QING_MU_SWORD_LOW = ITEMS.register("qing_mu_sword_low", () -> new QingMuSwordItem(ItemTier.LOW, 10, 10));
        QING_MU_SWORD_MID = ITEMS.register("qing_mu_sword_mid", () -> new QingMuSwordItem(ItemTier.MID, 12, 20));
        QING_MU_SWORD_HIGH = ITEMS.register("qing_mu_sword_high", () -> new QingMuSwordItem(ItemTier.HIGH, 15, 30));
        QING_MU_SWORD_SUPREME = ITEMS.register("qing_mu_sword_supreme", () -> new QingMuSwordItem(ItemTier.SUPREME, 17, 40));
        QING_MU_SWORD_IMMORTAL = ITEMS.register("qing_mu_sword_immortal", () -> new QingMuSwordItem(ItemTier.IMMORTAL, 20, 50));
        QING_MU_SWORD_GREAT_EMPEROR = ITEMS.register("qing_mu_sword_great_emperor", () -> new QingMuSwordItem(ItemTier.GREAT_EMPEROR, 30, 100));
        CHI_YAN_SWORD_LOW = ITEMS.register("chi_yan_sword_low", () -> new ChiYanSwordItem(ItemTier.LOW, 10, 10));
        CHI_YAN_SWORD_MID = ITEMS.register("chi_yan_sword_mid", () -> new ChiYanSwordItem(ItemTier.MID, 12, 20));
        CHI_YAN_SWORD_HIGH = ITEMS.register("chi_yan_sword_high", () -> new ChiYanSwordItem(ItemTier.HIGH, 15, 30));
        CHI_YAN_SWORD_SUPREME = ITEMS.register("chi_yan_sword_supreme", () -> new ChiYanSwordItem(ItemTier.SUPREME, 17, 40));
        CHI_YAN_SWORD_IMMORTAL = ITEMS.register("chi_yan_sword_immortal", () -> new ChiYanSwordItem(ItemTier.IMMORTAL, 20, 50));
        CHI_YAN_SWORD_GREAT_EMPEROR = ITEMS.register("chi_yan_sword_great_emperor", () -> new ChiYanSwordItem(ItemTier.GREAT_EMPEROR, 30, 100));
        HAN_BING_SWORD_LOW = ITEMS.register("han_bing_sword_low", () -> new HanBingSwordItem(ItemTier.LOW, 10, 10));
        HAN_BING_SWORD_MID = ITEMS.register("han_bing_sword_mid", () -> new HanBingSwordItem(ItemTier.MID, 12, 20));
        HAN_BING_SWORD_HIGH = ITEMS.register("han_bing_sword_high", () -> new HanBingSwordItem(ItemTier.HIGH, 15, 30));
        HAN_BING_SWORD_SUPREME = ITEMS.register("han_bing_sword_supreme", () -> new HanBingSwordItem(ItemTier.SUPREME, 17, 40));
        HAN_BING_SWORD_IMMORTAL = ITEMS.register("han_bing_sword_immortal", () -> new HanBingSwordItem(ItemTier.IMMORTAL, 20, 50));
        HAN_BING_SWORD_GREAT_EMPEROR = ITEMS.register("han_bing_sword_great_emperor", () -> new HanBingSwordItem(ItemTier.GREAT_EMPEROR, 30, 100));
        REALM_TOKENS = new EnumMap<Realm, RegistryObject<Item>>(Realm.class);
        for (Enum enum_ : Realm.values()) {
            id = "realm_token_" + ((Realm)enum_).id();
            REALM_TOKENS.put((Realm)enum_, (RegistryObject<Item>)ITEMS.register(id, () -> ModItems.lambda$static$144((Realm)enum_)));
        }
        LOOSE_IMMORTAL_REALM_TOKENS = new LinkedHashMap<Integer, RegistryObject<Item>>();
        int level = 1;
        while (level <= 9) {
            int tokenLevel = level++;
            String id2 = "realm_token_loose_immortal_" + tokenLevel;
            LOOSE_IMMORTAL_REALM_TOKENS.put(tokenLevel, ITEMS.register(id2, () -> new RealmTokenItem(new Item.Properties(), Realm.LOOSE_IMMORTAL, tokenLevel)));
        }
        CULTIVATOR_SPAWN_EGGS = new EnumMap<Realm, RegistryObject<Item>>(Realm.class);
        for (Realm realm : Realm.values()) {
            id = "spawn_egg_cultivator_" + realm.id();
            int[] colors = ModItems.realmEggColors(realm);
            int bg = colors[0];
            int hl = colors[1];
            Rarity rarity2 = ModItems.realmEggRarity(realm);
            CULTIVATOR_SPAWN_EGGS.put(realm, ITEMS.register(id, () -> new CultivatorSpawnEggItem(realm, bg, hl, new Item.Properties().rarity(rarity2))));
        }
        SOUL_REAPER_SPAWN_EGG = ITEMS.register("soul_reaper_spawn_egg", () -> new DifuReaperSpawnEggItem(1316382, 5691082, new Item.Properties().rarity(Rarity.RARE)));
    }
}

