/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.util.RandomSource
 *  net.minecraft.world.Container
 *  net.minecraft.world.item.Item
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.item.Items
 *  net.minecraft.world.level.ItemLike
 *  net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity
 */
package com.friday.cultivation.worldgen;

import com.friday.cultivation.cultivation.ItemTier;
import com.friday.cultivation.cultivation.alchemy.PillTier;
import com.friday.cultivation.registry.ModItems;
import com.friday.cultivation.util.CultivationRandomPools;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Container;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;

final class CultivationChestLoot {
    private CultivationChestLoot() {
    }

    static void fill(RandomizableContainerBlockEntity chest, RandomSource random, boolean ruined) {
        chest.clearContent();
        int vanillaRolls = ruined ? CultivationChestLoot.randomBetween(random, 2, 4) : CultivationChestLoot.randomBetween(random, 3, 6);
        int cultivationRolls = ruined ? random.nextInt(3) : CultivationChestLoot.randomBetween(random, 1, 3);
        CultivationChestLoot.addRolls((Container)chest, random, CultivationChestLoot.vanillaEntries(random, ruined), vanillaRolls);
        CultivationChestLoot.addRolls((Container)chest, random, CultivationChestLoot.cultivationEntries(random, ruined), cultivationRolls);
        chest.setChanged();
    }

    static void fillSectContainer(RandomizableContainerBlockEntity container, RandomSource random) {
        if (container == null) {
            return;
        }
        boolean removedForbidden = CultivationChestLoot.removeForbiddenNaturalLoot((Container)container);
        if (!container.isEmpty()) {
            if (removedForbidden) {
                container.setChanged();
            }
            return;
        }
        CultivationChestLoot.fill(container, random, false);
    }

    private static List<LootEntry> vanillaEntries(RandomSource random, boolean ruined) {
        ArrayList<LootEntry> entries = new ArrayList<LootEntry>();
        CultivationChestLoot.add(entries, 10, Items.BREAD, ruined ? 1 : 2, ruined ? 3 : 5, random);
        CultivationChestLoot.add(entries, 9, Items.PAPER, 1, ruined ? 3 : 6, random);
        CultivationChestLoot.add(entries, 7, Items.BOOK, 1, ruined ? 2 : 4, random);
        CultivationChestLoot.add(entries, 7, Items.COAL, 1, ruined ? 4 : 8, random);
        CultivationChestLoot.add(entries, 5, Items.IRON_INGOT, 1, ruined ? 2 : 4, random);
        CultivationChestLoot.add(entries, 4, Items.GOLD_INGOT, 1, ruined ? 2 : 3, random);
        CultivationChestLoot.add(entries, 3, Items.EMERALD, 1, ruined ? 2 : 5, random);
        if (ruined) {
            CultivationChestLoot.add(entries, 8, Items.BONE, 1, 5, random);
            CultivationChestLoot.add(entries, 6, Items.ROTTEN_FLESH, 1, 4, random);
            CultivationChestLoot.add(entries, 5, Items.IRON_NUGGET, 2, 10, random);
        } else {
            CultivationChestLoot.add(entries, 2, Items.DIAMOND, 1, 2, random);
            CultivationChestLoot.add(entries, 3, Items.LAPIS_LAZULI, 3, 10, random);
        }
        return entries;
    }

    private static List<LootEntry> cultivationEntries(RandomSource random, boolean ruined) {
        int weight;
        ArrayList<LootEntry> entries = new ArrayList<LootEntry>();
        CultivationChestLoot.add(entries, 12, (Item)ModItems.LOW_SPIRIT_STONE.get(), ruined ? 1 : 3, ruined ? 5 : 12, random);
        CultivationChestLoot.add(entries, 6, (Item)ModItems.MID_SPIRIT_STONE.get(), 1, ruined ? 2 : 5, random);
        CultivationChestLoot.add(entries, 3, (Item)ModItems.HIGH_SPIRIT_STONE.get(), 1, ruined ? 1 : 3, random);
        CultivationChestLoot.add(entries, 1, (Item)ModItems.SUPREME_SPIRIT_STONE.get(), 1, ruined ? 1 : 2, random);
        CultivationChestLoot.add(entries, 8, (Item)ModItems.HERB.get(), ruined ? 1 : 3, ruined ? 4 : 10, random);
        CultivationChestLoot.add(entries, 5, (Item)ModItems.TECHNIQUE_BOOK_FRAGMENT.get(), 1, ruined ? 1 : 2, random);
        CultivationChestLoot.add(entries, ruined ? 2 : 4, (Item)ModItems.ZHUJI_DAN.get(), 1, ruined ? 1 : 2, random);
        CultivationChestLoot.add(entries, ruined ? 1 : 2, (Item)ModItems.BLOOD_SPIRIT_PILL.get(), 1, 1, random);
        CultivationChestLoot.add(entries, 1, (Item)ModItems.FOUNDATION_SECRET.get(), 1, 1, random);
        if (!ruined) {
            CultivationChestLoot.add(entries, 1, (Item)ModItems.DAO_FOUNDATION_FRUIT.get(), 1, 1, random);
        }
        CultivationChestLoot.add(entries, ruined ? 1 : 2, (Item)ModItems.JIEDAN_PILL.get(), 1, 1, random);
        CultivationChestLoot.add(entries, ruined ? 1 : 2, (Item)ModItems.BLOOD_JIEDAN_PILL.get(), 1, 1, random);
        CultivationChestLoot.add(entries, 1, (Item)ModItems.ALL_CREATURES_TRUE_BLOOD.get(), 1, 1, random);
        CultivationChestLoot.add(entries, 1, (Item)ModItems.EARTH_EVIL_QI.get(), 1, 1, random);
        if (!ruined) {
            CultivationChestLoot.add(entries, 1, (Item)ModItems.HEAVEN_CLEAR_QI.get(), 1, 1, random);
            CultivationChestLoot.add(entries, 1, (Item)ModItems.NINGZHEN_CREATION_FRUIT.get(), 1, 1, random);
            CultivationChestLoot.add(entries, 1, (Item)ModItems.BLOOD_TRANSFORMATION_TALISMAN.get(), 1, 1, random);
        }
        if (!ruined) {
            CultivationChestLoot.add(entries, 2, (Item)ModItems.CUSHION.get(), 1, 1, random);
            CultivationChestLoot.add(entries, 1, (Item)ModItems.DIVINATION_COMPASS.get(), 1, 1, random);
        }
        for (ItemTier itemTier : ItemTier.values()) {
            weight = CultivationRandomPools.lootWeight(itemTier, ruined);
            for (Item item : CultivationRandomPools.techniqueBookItemsForTier(itemTier)) {
                CultivationChestLoot.add(entries, weight, item, 1, 1, random);
            }
            for (Item item : CultivationRandomPools.spellBookItemsForTier(itemTier)) {
                CultivationChestLoot.add(entries, weight, item, 1, 1, random);
            }
            for (Item item : CultivationRandomPools.swordWeaponsForTier(itemTier)) {
                CultivationChestLoot.add(entries, Math.max(1, weight / 2), item, 1, 1, random);
            }
        }
        for (Enum enum_ : PillTier.values()) {
            weight = CultivationRandomPools.lootWeight((PillTier)enum_, ruined);
            for (Item item : CultivationRandomPools.pillsForTier((PillTier)enum_)) {
                CultivationChestLoot.add(entries, weight, item, 1, ruined ? 1 : 2, random);
            }
        }
        return entries;
    }

    private static void add(List<LootEntry> entries, int weight, Item item, int minCount, int maxCount, RandomSource random) {
        if (weight <= 0 || item == null) {
            return;
        }
        if (CultivationRandomPools.isForbiddenNaturalLootItem(item)) {
            return;
        }
        entries.add(new LootEntry(weight, () -> new ItemStack((ItemLike)item, CultivationChestLoot.randomBetween(random, minCount, maxCount))));
    }

    private static void addRolls(Container container, RandomSource random, List<LootEntry> entries, int rolls) {
        if (entries.isEmpty() || rolls <= 0) {
            return;
        }
        for (int i = 0; i < rolls; ++i) {
            ItemStack stack = CultivationChestLoot.pick(entries, random).create();
            if (stack.isEmpty()) continue;
            CultivationChestLoot.addToRandomEmptySlot(container, stack, random);
        }
    }

    private static LootEntry pick(List<LootEntry> entries, RandomSource random) {
        int total = 0;
        for (LootEntry entry : entries) {
            total += entry.weight();
        }
        int roll = random.nextInt(Math.max(1, total));
        int acc = 0;
        for (LootEntry entry : entries) {
            if (roll >= (acc += entry.weight())) continue;
            return entry;
        }
        return entries.get(entries.size() - 1);
    }

    private static void addToRandomEmptySlot(Container container, ItemStack stack, RandomSource random) {
        if (CultivationRandomPools.isForbiddenNaturalLootStack(stack)) {
            return;
        }
        ArrayList<Integer> emptySlots = new ArrayList<Integer>();
        for (int slot = 0; slot < container.getContainerSize(); ++slot) {
            if (!container.getItem(slot).isEmpty()) continue;
            emptySlots.add(slot);
        }
        if (emptySlots.isEmpty()) {
            return;
        }
        container.setItem(((Integer)emptySlots.get(random.nextInt(emptySlots.size()))).intValue(), stack);
    }

    private static boolean removeForbiddenNaturalLoot(Container container) {
        boolean removed = false;
        for (int slot = 0; slot < container.getContainerSize(); ++slot) {
            ItemStack stack = container.getItem(slot);
            if (!CultivationRandomPools.isForbiddenNaturalLootStack(stack)) continue;
            container.setItem(slot, ItemStack.EMPTY);
            removed = true;
        }
        return removed;
    }

    private static int randomBetween(RandomSource random, int min, int max) {
        if (max <= min) {
            return min;
        }
        return min + random.nextInt(max - min + 1);
    }

    private record LootEntry(int weight, Supplier<ItemStack> factory) {
        ItemStack create() {
            return this.factory.get();
        }
    }
}

