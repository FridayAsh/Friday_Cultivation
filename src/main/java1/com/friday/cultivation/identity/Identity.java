package com.friday.cultivation.identity;

import com.friday.cultivation.ItemTier;
import com.friday.cultivation.item.ModItems;
import com.friday.cultivation.util.CultivationRandomPools;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Supplier;

/**
 * 身份系统 — 23种初始身份
 * （严格照搬原模组 com.xiaoxiang.cultivation.cultivation.Identity，仅包路径不同）
 */
public enum Identity {
    LONE_CULTIVATOR("lone_cultivator", "",
            () -> new ItemStack(ModItems.LOW_SPIRIT_STONE.get()),
            () -> List.of(new ItemStack(ModItems.TECHNIQUE_BOOK_FRAGMENT.get(), 1),
                    new ItemStack(Items.IRON_SWORD, 1),
                    new ItemStack(ModItems.LOW_SPIRIT_STONE.get(), 8))),
    MORTAL_CHILD("mortal_child", "",
            () -> new ItemStack(Items.BREAD),
            () -> List.of()),
    FALLEN_NOBLE("fallen_noble", "",
            () -> new ItemStack(Items.IRON_SWORD),
            () -> List.of(new ItemStack(Items.IRON_SWORD),
                    new ItemStack(Items.BREAD, 4))),
    MERCHANT_SON("merchant_son", "",
            () -> new ItemStack(Items.EMERALD),
            () -> List.of(new ItemStack(Items.EMERALD, 32),
                    new ItemStack(Items.BREAD, 8))),
    BANDIT_LEADER("bandit_leader", "",
            () -> new ItemStack(Items.IRON_AXE),
            () -> List.of(new ItemStack(Items.IRON_AXE),
                    new ItemStack(Items.COOKED_BEEF, 6))),
    HUNTER("hunter", "",
            () -> new ItemStack(Items.BOW),
            () -> List.of(new ItemStack(Items.BOW),
                    new ItemStack(Items.ARROW, 32),
                    new ItemStack(Items.LEATHER, 8),
                    new ItemStack(Items.BONE, 6),
                    new ItemStack(Items.FEATHER, 4))),
    SMITH_APPRENTICE("smith_apprentice", "",
            () -> new ItemStack(Items.IRON_PICKAXE),
            () -> List.of(new ItemStack(Items.IRON_PICKAXE),
                    new ItemStack(Items.IRON_INGOT, 16),
                    new ItemStack(Items.COAL, 8))),
    DOCTOR_HEIR("doctor_heir", "",
            () -> new ItemStack(ModItems.HERB.get()),
            () -> List.of(new ItemStack(ModItems.HERB.get(), 16),
                    new ItemStack(ModItems.LOW_SPIRIT_STONE.get(), 4))),
    HERMIT_DISCIPLE("hermit_disciple", "",
            () -> new ItemStack(ModItems.CUSHION.get()),
            () -> List.of(new ItemStack(ModItems.CUSHION.get()),
                    new ItemStack(ModItems.LOW_SPIRIT_STONE.get(), 16))),
    FISHERMAN("fisherman", "",
            () -> new ItemStack(Items.FISHING_ROD),
            () -> List.of(new ItemStack(Items.FISHING_ROD),
                    new ItemStack(Items.COD, 8))),
    FARMER("farmer", "",
            () -> new ItemStack(Items.IRON_HOE),
            () -> List.of(new ItemStack(Items.IRON_HOE),
                    new ItemStack(Items.WHEAT_SEEDS, 16),
                    new ItemStack(ModItems.HERB.get(), 3),
                    new ItemStack(Items.BREAD, 6))),
    ABANDONED_INFANT("abandoned_infant", "",
            () -> new ItemStack(Items.APPLE),
            () -> List.of()),
    GENERAL_SON("general_son", "",
            () -> new ItemStack(Items.DIAMOND_SWORD),
            () -> List.of(new ItemStack(Items.IRON_SWORD),
                    new ItemStack(Items.IRON_HELMET),
                    new ItemStack(Items.IRON_CHESTPLATE),
                    new ItemStack(Items.IRON_LEGGINGS),
                    new ItemStack(Items.IRON_BOOTS))),
    EXILED_PRINCESS("exiled_princess", "",
            () -> new ItemStack(Items.GOLD_INGOT),
            () -> List.of(new ItemStack(Items.GOLD_INGOT, 16),
                    new ItemStack(ModItems.MID_SPIRIT_STONE.get(), 1))),
    PIRATE("pirate", "",
            () -> new ItemStack(Items.IRON_AXE),
            () -> List.of(new ItemStack(Items.IRON_AXE),
                    new ItemStack(Items.IRON_SWORD),
                    new ItemStack(Items.CROSSBOW))),
    BEAST_DESCENDANT("beast_descendant", "",
            () -> new ItemStack(Items.BONE),
            () -> List.of(new ItemStack(Items.COOKED_BEEF, 16),
                    new ItemStack(Items.BONE, 8))),
    TAOIST("taoist", "",
            () -> new ItemStack(Items.PAPER),
            () -> List.of(new ItemStack(Items.PAPER, 8),
                    new ItemStack(ModItems.LOW_SPIRIT_STONE.get(), 4))),
    MONK("monk", "",
            () -> new ItemStack(Items.BOOK),
            () -> List.of(new ItemStack(Items.BOOK, 1),
                    new ItemStack(Items.BREAD, 8))),
    QINGYUN_OUTER_DISCIPLE("qingyun_outer_disciple", "qingyun",
            () -> new ItemStack(Items.IRON_CHESTPLATE),
            () -> List.of(new ItemStack(ModItems.SECT_TOKEN.get()),
                    new ItemStack(ModItems.TECHNIQUE_BOOK_FRAGMENT.get()),
                    randomLowSpellBook(),
                    new ItemStack(ModItems.LOW_SPIRIT_STONE.get(), 12))),
    WANJIAN_OUTER_DISCIPLE("wanjian_outer_disciple", "wanjian",
            () -> new ItemStack(Items.IRON_SWORD),
            () -> List.of(new ItemStack(Items.IRON_SWORD),
                    new ItemStack(ModItems.LOW_SPIRIT_STONE.get(), 12))),
    DANDING_OUTER_DISCIPLE("danding_outer_disciple", "danding",
            () -> new ItemStack(Items.BREWING_STAND),
            () -> List.of(new ItemStack(ModItems.HERB.get(), 16),
                    new ItemStack(Items.BREWING_STAND),
                    new ItemStack(ModItems.LOW_SPIRIT_STONE.get(), 4))),
    FORMATION_APPRENTICE("formation_apprentice", "",
            () -> new ItemStack(ModItems.LOW_FORMATION_CORE_PLATE.get()),
            () -> List.of(new ItemStack(ModItems.LOW_FORMATION_CORE_PLATE.get(), 1),
                    randomLowFormationFlag())),
    ACADEMY_STUDENT("academy_student", "",
            () -> new ItemStack(ModItems.TALISMAN_PAPER.get()),
            () -> List.of(new ItemStack(Items.PAPER, 16),
                    new ItemStack(ModItems.INK.get(), 4),
                    new ItemStack(ModItems.TALISMAN_PAPER.get(), 8),
                    randomLowSpellBook()));

    private final String id;
    private final String defaultSectId;
    private final Supplier<ItemStack> portraitItemSupplier;
    private final Supplier<List<ItemStack>> starterItemsSupplier;

    Identity(String id, String defaultSectId, Supplier<ItemStack> portraitItemSupplier, Supplier<List<ItemStack>> starterItemsSupplier) {
        this.id = id;
        this.defaultSectId = defaultSectId;
        this.portraitItemSupplier = portraitItemSupplier;
        this.starterItemsSupplier = starterItemsSupplier;
    }

    public String id() { return this.id; }

    public String defaultSectId() { return this.defaultSectId; }

    public boolean isSolo() { return this.defaultSectId == null || this.defaultSectId.isEmpty(); }

    public ItemStack portraitItem() { return this.portraitItemSupplier.get(); }

    public ResourceLocation portraitTexture() {
        return new ResourceLocation("friday_cultivation", "textures/identity/" + this.id + ".png");
    }

    public List<ItemStack> starterItems() { return this.starterItemsSupplier.get(); }

    public String translationKey() { return "identity.friday_cultivation." + this.id; }

    public String descriptionKey() { return "identity.friday_cultivation." + this.id + ".desc"; }

    public Component displayName() { return Component.translatable(translationKey()); }

    public static Identity byId(String id) {
        if (id == null || id.isEmpty()) return LONE_CULTIVATOR;
        for (Identity i : Identity.values()) {
            if (i.id.equals(id)) return i;
        }
        return LONE_CULTIVATOR;
    }

    public static List<Identity> selectableOrigins() { return Arrays.asList(Identity.values()); }

    public int[] lifespanRange() {
        return switch (this) {
            case BEAST_DESCENDANT, GENERAL_SON, HUNTER, BANDIT_LEADER, PIRATE ->
                    new int[]{90, 110};
            case MERCHANT_SON, EXILED_PRINCESS, ACADEMY_STUDENT, TAOIST ->
                    new int[]{60, 85};
            case HERMIT_DISCIPLE, MONK, DOCTOR_HEIR, LONE_CULTIVATOR ->
                    new int[]{80, 105};
            case ABANDONED_INFANT ->
                    new int[]{55, 90};
            default ->
                    new int[]{70, 100};
        };
    }

    private static ItemStack randomLowSpellBook() {
        List<net.minecraft.world.item.Item> books = CultivationRandomPools.spellBookItemsForTier(ItemTier.LOW);
        if (books.isEmpty()) {
            return new ItemStack(ModItems.SPELL_BOOK_FIREBALL.get());
        }
        int pick = ThreadLocalRandom.current().nextInt(books.size());
        return new ItemStack(books.get(pick));
    }

    private static ItemStack randomLowFormationFlag() {
        int pick = ThreadLocalRandom.current().nextInt(3);
        return switch (pick) {
            case 0 -> new ItemStack(ModItems.LOW_QI_GATHERING_FLAG.get());
            case 1 -> new ItemStack(ModItems.LOW_WITHER_GROWTH_FLAG.get());
            default -> new ItemStack(ModItems.LOW_REJUVENATION_FLAG.get());
        };
    }
}
