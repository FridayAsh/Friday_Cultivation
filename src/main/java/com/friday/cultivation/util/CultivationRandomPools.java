/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.util.RandomSource
 *  net.minecraft.world.item.Item
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.item.Items
 *  net.minecraft.world.item.SpawnEggItem
 *  net.minecraftforge.registries.ForgeRegistries
 */
package com.friday.cultivation.util;

import com.friday.cultivation.cultivation.ItemTier;
import com.friday.cultivation.cultivation.alchemy.PillTier;
import com.friday.cultivation.cultivation.realm.Realm;
import com.friday.cultivation.cultivation.spell.Spell;
import com.friday.cultivation.cultivation.technique.Technique;
import com.friday.cultivation.entity.npc.NpcSpellCaster;
import com.friday.cultivation.item.PillItem;
import com.friday.cultivation.item.RealmTokenItem;
import com.friday.cultivation.item.weapon.TieredWeapon;
import com.friday.cultivation.registry.ModItems;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraftforge.registries.ForgeRegistries;

public final class CultivationRandomPools {
    private CultivationRandomPools() {
    }

    public static List<Technique> techniquesForTier(ItemTier tier) {
        ArrayList<Technique> result = new ArrayList<Technique>();
        for (Technique technique : ModItems.orderedTechniqueBookTechniques()) {
            if (technique == Technique.FRAGMENT || technique.tier() != tier) continue;
            result.add(technique);
        }
        return result;
    }

    public static List<Spell> npcLearnableSpellsForTier(ItemTier tier) {
        ArrayList<Spell> result = new ArrayList<Spell>();
        for (Spell spell : Spell.values()) {
            if (spell.tier() != tier || !CultivationRandomPools.isRandomNpcSpellCandidate(spell)) continue;
            result.add(spell);
        }
        return result;
    }

    public static List<Item> techniqueBookItemsForTier(ItemTier tier) {
        ArrayList<Item> result = new ArrayList<Item>();
        for (Technique technique : ModItems.orderedTechniqueBookTechniques()) {
            Item item;
            if (technique.tier() != tier || (item = ModItems.techniqueBookItem(technique)) == null) continue;
            result.add(item);
        }
        return result;
    }

    public static List<Item> spellBookItemsForTier(ItemTier tier) {
        ArrayList<Item> result = new ArrayList<Item>();
        for (Spell spell : ModItems.orderedSpellBookSpells()) {
            Item item;
            if (spell.tier() != tier || (item = ModItems.spellBookItem(spell)) == null) continue;
            result.add(item);
        }
        return result;
    }

    public static List<Item> swordWeaponsForTier(ItemTier tier) {
        ArrayList<Item> result = new ArrayList<Item>();
        for (Item item : ForgeRegistries.ITEMS.getValues()) {
            TieredWeapon weapon;
            if (!(item instanceof TieredWeapon) || !(weapon = (TieredWeapon)item).isSwordWeapon() || weapon.tier() != tier) continue;
            result.add(item);
        }
        return result;
    }

    public static Optional<Item> randomSwordForRealm(Realm realm, RandomSource random) {
        List<Item> candidates = CultivationRandomPools.swordWeaponsForTier(CultivationRandomPools.weaponTierForRealm(realm));
        if (candidates.isEmpty() && CultivationRandomPools.weaponTierForRealm(realm) == ItemTier.LOW) {
            candidates = List.of(Items.WOODEN_SWORD, Items.STONE_SWORD);
        }
        return CultivationRandomPools.randomFrom(candidates, random);
    }

    public static List<Item> pillsForTier(PillTier tier) {
        ArrayList<Item> result = new ArrayList<Item>();
        for (Item item : ForgeRegistries.ITEMS.getValues()) {
            PillItem pill;
            if (!(item instanceof PillItem) || (pill = (PillItem)item).tier() != tier) continue;
            result.add(item);
        }
        return result;
    }

    public static Optional<Item> randomPillForTier(PillTier tier, RandomSource random) {
        return CultivationRandomPools.randomFrom(CultivationRandomPools.pillsForTier(tier), random);
    }

    public static PillTier[] pillTiersForRealm(Realm realm) {
        PillTier[] pillTierArray;
        switch (realm) {
            default: {
                throw new IncompatibleClassChangeError();
            }
            case MORTAL: {
                pillTierArray = new PillTier[]{};
                break;
            }
            case BODY_TEMPERING: {
                PillTier[] pillTierBody = new PillTier[1];
                pillTierBody[0] = PillTier.LOW;
                pillTierArray = pillTierBody;
                break;
            }
            case QI_REFINING: {
                PillTier[] pillTierArray2 = new PillTier[1];
                pillTierArray = pillTierArray2;
                pillTierArray2[0] = PillTier.LOW;
                break;
            }
            case FOUNDATION_BUILDING: 
            case GOLDEN_CORE: {
                PillTier[] pillTierArray3 = new PillTier[2];
                pillTierArray3[0] = PillTier.LOW;
                pillTierArray = pillTierArray3;
                pillTierArray3[1] = PillTier.MID;
                break;
            }
            case NASCENT_SOUL: 
            case SOUL_FORMATION: {
                PillTier[] pillTierArray4 = new PillTier[3];
                pillTierArray4[0] = PillTier.MID;
                pillTierArray4[1] = PillTier.MID;
                pillTierArray = pillTierArray4;
                pillTierArray4[2] = PillTier.HIGH;
                break;
            }
            case VOID_REFINING: 
            case BODY_INTEGRATION: {
                PillTier[] pillTierArray5 = new PillTier[3];
                pillTierArray5[0] = PillTier.HIGH;
                pillTierArray5[1] = PillTier.HIGH;
                pillTierArray = pillTierArray5;
                pillTierArray5[2] = PillTier.SUPREME;
                break;
            }
            case MAHAYANA: 
            case TRIBULATION_TRANSCENDENCE: {
                PillTier[] pillTierArray6 = new PillTier[3];
                pillTierArray6[0] = PillTier.SUPREME;
                pillTierArray6[1] = PillTier.SUPREME;
                pillTierArray = pillTierArray6;
                pillTierArray6[2] = PillTier.SUPREME;
                break;
            }
            case TRUE_IMMORTAL: {
                PillTier[] pillTierTrueImmortal = new PillTier[3];
                pillTierTrueImmortal[0] = PillTier.SUPREME;
                pillTierTrueImmortal[1] = PillTier.SUPREME;
                pillTierArray = pillTierTrueImmortal;
                pillTierTrueImmortal[2] = PillTier.IMMORTAL;
                break;
            }
            case MYSTIC_IMMORTAL:
            case IMMORTAL_LORD:
            case IMMORTAL_VENERABLE:
            case IMMORTAL_KING: {
                // 玄仙/仙君/仙尊/仙王：介于真仙与半圣之间，仙级品阶
                PillTier[] pillTierXian = new PillTier[3];
                pillTierXian[0] = PillTier.SUPREME;
                pillTierXian[1] = PillTier.SUPREME;
                pillTierArray = pillTierXian;
                pillTierXian[2] = PillTier.IMMORTAL;
                break;
            }
            case HALF_SAGE: 
            case SAGE:
            case HALF_EMPEROR: {
                // 半圣/圣人/半帝：圣级品阶（SAGE，介于仙 IMMORTAL 与帝 GREAT_EMPEROR 之间）
                PillTier[] pillTierSage = new PillTier[3];
                pillTierSage[0] = PillTier.SUPREME;
                pillTierSage[1] = PillTier.SUPREME;
                pillTierArray = pillTierSage;
                pillTierSage[2] = PillTier.SUPREME;
                break;
            }
            case LOOSE_IMMORTAL:
            case GREAT_EMPEROR: {
                PillTier[] pillTierArray7 = new PillTier[3];
                pillTierArray7[0] = PillTier.SUPREME;
                pillTierArray7[1] = PillTier.SUPREME;
                pillTierArray = pillTierArray7;
                pillTierArray7[2] = PillTier.IMMORTAL;
            }
        }
        return pillTierArray;
    }

    public static ItemTier techniqueTierForRealm(Realm realm) {
        return switch (realm) {
            default -> throw new IncompatibleClassChangeError();
            case MORTAL, BODY_TEMPERING, QI_REFINING, FOUNDATION_BUILDING -> ItemTier.LOW;
            case GOLDEN_CORE, NASCENT_SOUL -> ItemTier.MID;
            case SOUL_FORMATION, VOID_REFINING -> ItemTier.HIGH;
            case BODY_INTEGRATION, MAHAYANA, TRIBULATION_TRANSCENDENCE -> ItemTier.SUPREME;
            case TRUE_IMMORTAL -> ItemTier.IMMORTAL;
            case MYSTIC_IMMORTAL, IMMORTAL_LORD, IMMORTAL_VENERABLE, IMMORTAL_KING -> ItemTier.IMMORTAL;
            case HALF_SAGE, SAGE, HALF_EMPEROR -> ItemTier.SAGE;
            case LOOSE_IMMORTAL, GREAT_EMPEROR -> ItemTier.IMMORTAL;
        };
    }

    public static ItemTier spellTierForRealm(Realm realm) {
        return switch (realm) {
            default -> throw new IncompatibleClassChangeError();
            case MORTAL, BODY_TEMPERING, QI_REFINING, FOUNDATION_BUILDING -> ItemTier.LOW;
            case GOLDEN_CORE, NASCENT_SOUL -> ItemTier.MID;
            case SOUL_FORMATION, VOID_REFINING -> ItemTier.HIGH;
            case BODY_INTEGRATION, MAHAYANA, TRIBULATION_TRANSCENDENCE, TRUE_IMMORTAL -> ItemTier.SUPREME;
            case MYSTIC_IMMORTAL, IMMORTAL_LORD, IMMORTAL_VENERABLE, IMMORTAL_KING -> ItemTier.SUPREME;
            case HALF_SAGE, SAGE, HALF_EMPEROR -> ItemTier.SAGE;
            case LOOSE_IMMORTAL, GREAT_EMPEROR -> ItemTier.SUPREME;
        };
    }

    public static ItemTier weaponTierForRealm(Realm realm) {
        return switch (realm) {
            default -> throw new IncompatibleClassChangeError();
            case MORTAL, BODY_TEMPERING, QI_REFINING, FOUNDATION_BUILDING -> ItemTier.LOW;
            case GOLDEN_CORE, NASCENT_SOUL -> ItemTier.MID;
            case SOUL_FORMATION, VOID_REFINING -> ItemTier.HIGH;
            case BODY_INTEGRATION, MAHAYANA, TRIBULATION_TRANSCENDENCE -> ItemTier.SUPREME;
            case TRUE_IMMORTAL -> ItemTier.IMMORTAL;
            case MYSTIC_IMMORTAL, IMMORTAL_LORD, IMMORTAL_VENERABLE, IMMORTAL_KING -> ItemTier.IMMORTAL;
            case HALF_SAGE, SAGE, HALF_EMPEROR -> ItemTier.SAGE;
            case LOOSE_IMMORTAL, GREAT_EMPEROR -> ItemTier.IMMORTAL;
        };
    }

    public static int lootWeight(ItemTier tier, boolean ruined) {
        return switch (tier) {
            default -> throw new IncompatibleClassChangeError();
            case LOW -> {
                if (ruined) {
                    yield 12;
                }
                yield 10;
            }
            case MID -> {
                if (ruined) {
                    yield 6;
                }
                yield 8;
            }
            case HIGH -> {
                if (ruined) {
                    yield 2;
                }
                yield 5;
            }
            case SUPREME -> {
                if (ruined) {
                    yield 1;
                }
                yield 2;
            }
            case SAGE -> ruined ? 1 : 1;
            case IMMORTAL -> ruined ? 1 : 1;
            case GREAT_EMPEROR -> ruined ? 1 : 1;
        };
    }

    public static int lootWeight(PillTier tier, boolean ruined) {
        return switch (tier) {
            default -> throw new IncompatibleClassChangeError();
            case LOW -> {
                if (ruined) {
                    yield 12;
                }
                yield 10;
            }
            case MID -> {
                if (ruined) {
                    yield 6;
                }
                yield 8;
            }
            case HIGH -> {
                if (ruined) {
                    yield 2;
                }
                yield 5;
            }
            case SUPREME -> {
                if (ruined) {
                    yield 1;
                }
                yield 2;
            }
            case IMMORTAL -> ruined ? 1 : 1;
        };
    }

    public static boolean isRandomNpcSpellCandidate(Spell spell) {
        return spell != null && spell != Spell.IMMORTAL_INCANTATION && !CultivationRandomPools.isRealmAutomaticSpell(spell) && NpcSpellCaster.isLearnableByNpc(spell);
    }

    public static boolean isRealmAutomaticSpell(Spell spell) {
        return spell == Spell.SPIRIT_VISION || spell == Spell.QI_TRANSFER || spell == Spell.QI_SHIELD || spell == Spell.REALM_PRESSURE || spell == Spell.SWORD_AURA || spell == Spell.SWORD_FLIGHT || spell == Spell.BIGU || spell == Spell.CORE_SELF_DESTRUCT || spell == Spell.NASCENT_SOUL_OUT_OF_BODY || spell == Spell.DIVINE_SENSE || spell == Spell.VOID_STEP || spell == Spell.VOID_ESCAPE || spell == Spell.DHARMA_BODY_MANIFESTATION || spell == Spell.QI_FLIGHT || spell == Spell.GHOST_FLIGHT;
    }

    public static boolean isForbiddenNaturalLootStack(ItemStack stack) {
        return stack != null && !stack.isEmpty() && CultivationRandomPools.isForbiddenNaturalLootItem(stack.getItem());
    }

    public static boolean isForbiddenNaturalLootItem(Item item) {
        if (item == null || item == Items.AIR) {
            return false;
        }
        if (item instanceof SpawnEggItem) {
            return true;
        }
        if (item instanceof RealmTokenItem) {
            return true;
        }
        return item == ModItems.ORIGIN_RECONFIGURATION_TOKEN.get() || item == ModItems.REINCARNATION_FATE_PLATE.get();
    }

    private static <T> Optional<T> randomFrom(List<T> candidates, RandomSource random) {
        if (candidates.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(candidates.get(random.nextInt(candidates.size())));
    }
}

