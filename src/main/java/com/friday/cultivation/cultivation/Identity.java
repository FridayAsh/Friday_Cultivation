/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.world.item.Item
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.item.Items
 *  net.minecraft.world.level.ItemLike
 */
package com.friday.cultivation.cultivation;

import com.friday.cultivation.cultivation.ItemTier;
import com.friday.cultivation.registry.ModItems;
import com.friday.cultivation.util.CultivationRandomPools;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Supplier;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;

public enum Identity {
    LONE_CULTIVATOR("lone_cultivator", "", () -> new ItemStack((ItemLike)ModItems.LOW_SPIRIT_STONE.get()), () -> List.of(new ItemStack((ItemLike)ModItems.TECHNIQUE_BOOK_FRAGMENT.get(), 1), new ItemStack((ItemLike)Items.IRON_SWORD, 1), new ItemStack((ItemLike)ModItems.LOW_SPIRIT_STONE.get(), 8))),
    MORTAL_CHILD("mortal_child", "", () -> new ItemStack((ItemLike)Items.BREAD), () -> List.of()),
    FALLEN_NOBLE("fallen_noble", "", () -> new ItemStack((ItemLike)Items.IRON_SWORD), () -> List.of(new ItemStack((ItemLike)Items.IRON_SWORD), new ItemStack((ItemLike)Items.BREAD, 4))),
    MERCHANT_SON("merchant_son", "", () -> new ItemStack((ItemLike)Items.EMERALD), () -> List.of(new ItemStack((ItemLike)Items.EMERALD, 32), new ItemStack((ItemLike)Items.BREAD, 8))),
    BANDIT_LEADER("bandit_leader", "", () -> new ItemStack((ItemLike)Items.STONE_AXE), () -> List.of(new ItemStack((ItemLike)Items.STONE_AXE), new ItemStack((ItemLike)Items.COOKED_BEEF, 6))),
    HUNTER("hunter", "", () -> new ItemStack((ItemLike)Items.BOW), () -> List.of(new ItemStack((ItemLike)Items.BOW), new ItemStack((ItemLike)Items.ARROW, 32), new ItemStack((ItemLike)Items.BONE, 8), new ItemStack((ItemLike)Items.LEATHER, 6), new ItemStack((ItemLike)Items.COOKED_RABBIT, 4))),
    SMITH_APPRENTICE("smith_apprentice", "", () -> new ItemStack((ItemLike)Items.ANVIL), () -> List.of(new ItemStack((ItemLike)Items.BOOK), new ItemStack((ItemLike)Items.COPPER_INGOT, 16), new ItemStack((ItemLike)Items.IRON_INGOT, 8))),
    DOCTOR_HEIR("doctor_heir", "", () -> new ItemStack((ItemLike)ModItems.HERB.get()), () -> List.of(new ItemStack((ItemLike)ModItems.HERB.get(), 16), new ItemStack((ItemLike)ModItems.LOW_SPIRIT_STONE.get(), 4))),
    HERMIT_DISCIPLE("hermit_disciple", "", () -> new ItemStack((ItemLike)ModItems.CUSHION.get()), () -> List.of(new ItemStack((ItemLike)ModItems.CUSHION.get()), new ItemStack((ItemLike)ModItems.LOW_SPIRIT_STONE.get(), 16))),
    FISHERMAN("fisherman", "", () -> new ItemStack((ItemLike)Items.FISHING_ROD), () -> List.of(new ItemStack((ItemLike)Items.FISHING_ROD), new ItemStack((ItemLike)Items.COOKED_COD, 8))),
    FARMER("farmer", "", () -> new ItemStack((ItemLike)Items.WHEAT), () -> List.of(new ItemStack((ItemLike)Items.WOODEN_HOE), new ItemStack((ItemLike)Items.WHEAT_SEEDS, 16), new ItemStack((ItemLike)ModItems.HERB.get(), 3), new ItemStack((ItemLike)Items.BREAD, 6))),
    ABANDONED_INFANT("abandoned_infant", "", () -> new ItemStack((ItemLike)Items.EGG), () -> List.of()),
    GENERAL_SON("general_son", "", () -> new ItemStack((ItemLike)Items.IRON_CHESTPLATE), () -> List.of(new ItemStack((ItemLike)Items.IRON_SWORD), new ItemStack((ItemLike)Items.LEATHER_HELMET), new ItemStack((ItemLike)Items.LEATHER_CHESTPLATE), new ItemStack((ItemLike)Items.LEATHER_LEGGINGS), new ItemStack((ItemLike)Items.LEATHER_BOOTS))),
    EXILED_PRINCESS("exiled_princess", "", () -> new ItemStack((ItemLike)Items.GOLDEN_APPLE), () -> List.of(new ItemStack((ItemLike)Items.GOLD_INGOT, 16), new ItemStack((ItemLike)ModItems.MID_SPIRIT_STONE.get(), 1))),
    PIRATE("pirate", "", () -> new ItemStack((ItemLike)Items.OAK_BOAT), () -> List.of(new ItemStack((ItemLike)Items.OAK_BOAT), new ItemStack((ItemLike)Items.IRON_SWORD), new ItemStack((ItemLike)Items.MILK_BUCKET))),
    BEAST_DESCENDANT("beast_descendant", "", () -> new ItemStack((ItemLike)Items.LEATHER), () -> List.of(new ItemStack((ItemLike)Items.COOKED_BEEF, 16), new ItemStack((ItemLike)Items.LEATHER, 8))),
    TAOIST("taoist", "", () -> new ItemStack((ItemLike)Items.PAPER), () -> List.of(new ItemStack((ItemLike)Items.PAPER, 8), new ItemStack((ItemLike)ModItems.LOW_SPIRIT_STONE.get(), 4))),
    MONK("monk", "", () -> new ItemStack((ItemLike)Items.BOWL), () -> List.of(new ItemStack((ItemLike)Items.BOWL, 1), new ItemStack((ItemLike)Items.BREAD, 8))),
    QINGYUN_OUTER_DISCIPLE("qingyun_outer_disciple", "qingyun", () -> new ItemStack((ItemLike)Items.LEATHER_CHESTPLATE), () -> List.of(new ItemStack((ItemLike)ModItems.SECT_TOKEN.get()), new ItemStack((ItemLike)ModItems.TECHNIQUE_BOOK_FRAGMENT.get()), Identity.randomLowSpellBook(), new ItemStack((ItemLike)ModItems.LOW_SPIRIT_STONE.get(), 12))),
    WANJIAN_OUTER_DISCIPLE("wanjian_outer_disciple", "wanjian", () -> new ItemStack((ItemLike)Items.IRON_SWORD), () -> List.of(new ItemStack((ItemLike)Items.IRON_SWORD), new ItemStack((ItemLike)ModItems.LOW_SPIRIT_STONE.get(), 12))),
    DANDING_OUTER_DISCIPLE("danding_outer_disciple", "danding", () -> new ItemStack((ItemLike)Items.FURNACE), () -> List.of(new ItemStack((ItemLike)ModItems.HERB.get(), 16), new ItemStack((ItemLike)Items.FURNACE), new ItemStack((ItemLike)ModItems.LOW_SPIRIT_STONE.get(), 4))),
    FORMATION_APPRENTICE("formation_apprentice", "", () -> new ItemStack((ItemLike)ModItems.LOW_FORMATION_CORE_PLATE.get()), () -> List.of(new ItemStack((ItemLike)ModItems.LOW_FORMATION_CORE_PLATE.get(), 1), Identity.randomLowFormationFlag())),
    ACADEMY_STUDENT("academy_student", "", () -> new ItemStack((ItemLike)ModItems.TALISMAN_PAPER.get()), () -> List.of(new ItemStack((ItemLike)Items.PAPER, 16), new ItemStack((ItemLike)ModItems.INK.get(), 4), new ItemStack((ItemLike)ModItems.TALISMAN_PAPER.get(), 8), Identity.randomLowSpellBook()));

    private final String id;
    private final String defaultSectId;
    private final Supplier<ItemStack> portraitItemSupplier;
    private final Supplier<List<ItemStack>> starterItemsSupplier;

    private Identity(String id, String defaultSectId, Supplier<ItemStack> portraitItemSupplier, Supplier<List<ItemStack>> starterItemsSupplier) {
        this.id = id;
        this.defaultSectId = defaultSectId;
        this.portraitItemSupplier = portraitItemSupplier;
        this.starterItemsSupplier = starterItemsSupplier;
    }

    public String id() {
        return this.id;
    }

    public String defaultSectId() {
        return this.defaultSectId;
    }

    public boolean isSolo() {
        return this.defaultSectId == null || this.defaultSectId.isEmpty();
    }

    public ItemStack portraitItem() {
        return this.portraitItemSupplier.get();
    }

    public ResourceLocation portraitTexture() {
        return new ResourceLocation("friday_cultivation", "textures/identity/" + this.id + ".png");
    }

    public List<ItemStack> starterItems() {
        return this.starterItemsSupplier.get();
    }

    public String translationKey() {
        return "identity.friday_cultivation." + this.id;
    }

    public String descriptionKey() {
        return "identity.friday_cultivation." + this.id + ".desc";
    }

    public static Identity byId(String id) {
        if (id == null || id.isEmpty()) {
            return LONE_CULTIVATOR;
        }
        for (Identity i : Identity.values()) {
            if (!i.id.equals(id)) continue;
            return i;
        }
        return LONE_CULTIVATOR;
    }

    public static List<Identity> selectableOrigins() {
        return Arrays.asList(Identity.values());
    }

    public int[] lifespanRange() {
        int[] nArray;
        switch (this) {
            case BEAST_DESCENDANT: 
            case GENERAL_SON: 
            case HUNTER: 
            case BANDIT_LEADER: 
            case PIRATE: {
                int[] nArray2 = new int[2];
                nArray2[0] = 90;
                nArray = nArray2;
                nArray2[1] = 110;
                break;
            }
            case MERCHANT_SON: 
            case EXILED_PRINCESS: 
            case ACADEMY_STUDENT: 
            case TAOIST: {
                int[] nArray3 = new int[2];
                nArray3[0] = 60;
                nArray = nArray3;
                nArray3[1] = 85;
                break;
            }
            case HERMIT_DISCIPLE: 
            case MONK: 
            case DOCTOR_HEIR: 
            case LONE_CULTIVATOR: {
                int[] nArray4 = new int[2];
                nArray4[0] = 80;
                nArray = nArray4;
                nArray4[1] = 105;
                break;
            }
            case ABANDONED_INFANT: {
                int[] nArray5 = new int[2];
                nArray5[0] = 55;
                nArray = nArray5;
                nArray5[1] = 90;
                break;
            }
            default: {
                int[] nArray6 = new int[2];
                nArray6[0] = 70;
                nArray = nArray6;
                nArray6[1] = 100;
            }
        }
        return nArray;
    }

    private static ItemStack randomLowSpellBook() {
        List<Item> books = CultivationRandomPools.spellBookItemsForTier(ItemTier.LOW);
        if (books.isEmpty()) {
            return new ItemStack((ItemLike)ModItems.SPELL_BOOK_FIREBALL.get());
        }
        int pick = ThreadLocalRandom.current().nextInt(books.size());
        return new ItemStack((ItemLike)books.get(pick));
    }

    private static ItemStack randomLowFormationFlag() {
        int pick = ThreadLocalRandom.current().nextInt(3);
        return switch (pick) {
            case 0 -> new ItemStack((ItemLike)ModItems.LOW_QI_GATHERING_FLAG.get());
            case 1 -> new ItemStack((ItemLike)ModItems.LOW_WITHER_GROWTH_FLAG.get());
            default -> new ItemStack((ItemLike)ModItems.LOW_REJUVENATION_FLAG.get());
        };
    }
}

