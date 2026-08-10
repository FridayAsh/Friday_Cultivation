/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.world.item.Item
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.item.Items
 *  net.minecraft.world.level.ItemLike
 */
package com.friday.cultivation.entity.npc;

import com.friday.cultivation.registry.ModItems;
import com.friday.cultivation.util.CultivationRandomPools;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;

public final class SundryPricing {
    private static final Map<Item, ItemStack> UNIT_PRICE = new HashMap<Item, ItemStack>();

    private static void put(Item item, Item priceItem, int priceCount) {
        UNIT_PRICE.put(item, new ItemStack((ItemLike)priceItem, priceCount));
    }

    private SundryPricing() {
    }

    public static ItemStack priceFor(ItemStack stack) {
        if (stack.isEmpty()) {
            return ItemStack.EMPTY;
        }
        if (CultivationRandomPools.isForbiddenNaturalLootStack(stack)) {
            return ItemStack.EMPTY;
        }
        ItemStack unit = UNIT_PRICE.get(stack.getItem());
        if (unit == null) {
            return ItemStack.EMPTY;
        }
        int totalValue = unit.getCount() * stack.getCount();
        if (totalValue <= 0) {
            return ItemStack.EMPTY;
        }
        int cappedCount = Math.min(64, totalValue);
        return new ItemStack((ItemLike)unit.getItem(), cappedCount);
    }

    public static boolean isAccepted(Item item) {
        return !CultivationRandomPools.isForbiddenNaturalLootItem(item) && UNIT_PRICE.containsKey(item);
    }

    public static Set<Item> allAccepted() {
        HashSet<Item> result = new HashSet<Item>();
        for (Item item : UNIT_PRICE.keySet()) {
            if (CultivationRandomPools.isForbiddenNaturalLootItem(item)) continue;
            result.add(item);
        }
        return result;
    }

    static {
        SundryPricing.put(Items.IRON_INGOT, (Item)ModItems.LOW_SPIRIT_STONE.get(), 1);
        SundryPricing.put(Items.IRON_BLOCK, (Item)ModItems.LOW_SPIRIT_STONE.get(), 9);
        SundryPricing.put(Items.RAW_IRON, (Item)ModItems.LOW_SPIRIT_STONE.get(), 1);
        SundryPricing.put(Items.GOLD_INGOT, (Item)ModItems.LOW_SPIRIT_STONE.get(), 2);
        SundryPricing.put(Items.GOLD_BLOCK, (Item)ModItems.LOW_SPIRIT_STONE.get(), 18);
        SundryPricing.put(Items.RAW_GOLD, (Item)ModItems.LOW_SPIRIT_STONE.get(), 2);
        SundryPricing.put(Items.DIAMOND, (Item)ModItems.MID_SPIRIT_STONE.get(), 1);
        SundryPricing.put(Items.DIAMOND_BLOCK, (Item)ModItems.MID_SPIRIT_STONE.get(), 9);
        SundryPricing.put(Items.EMERALD, (Item)ModItems.LOW_SPIRIT_STONE.get(), 1);
        SundryPricing.put(Items.EMERALD_BLOCK, (Item)ModItems.MID_SPIRIT_STONE.get(), 1);
        SundryPricing.put(Items.NETHERITE_INGOT, (Item)ModItems.SUPREME_SPIRIT_STONE.get(), 1);
        SundryPricing.put(Items.NETHERITE_SCRAP, (Item)ModItems.HIGH_SPIRIT_STONE.get(), 1);
        SundryPricing.put(Items.NETHERITE_BLOCK, (Item)ModItems.SUPREME_SPIRIT_STONE.get(), 9);
        SundryPricing.put(Items.REDSTONE, (Item)ModItems.LOW_SPIRIT_STONE.get(), 1);
        SundryPricing.put(Items.REDSTONE_BLOCK, (Item)ModItems.LOW_SPIRIT_STONE.get(), 9);
        SundryPricing.put(Items.LAPIS_LAZULI, (Item)ModItems.LOW_SPIRIT_STONE.get(), 1);
        SundryPricing.put(Items.LAPIS_BLOCK, (Item)ModItems.LOW_SPIRIT_STONE.get(), 9);
        SundryPricing.put(Items.GLOWSTONE_DUST, (Item)ModItems.LOW_SPIRIT_STONE.get(), 1);
        SundryPricing.put(Items.GLOWSTONE, (Item)ModItems.LOW_SPIRIT_STONE.get(), 4);
        SundryPricing.put(Items.ENDER_PEARL, (Item)ModItems.LOW_SPIRIT_STONE.get(), 5);
        SundryPricing.put(Items.ENDER_EYE, (Item)ModItems.MID_SPIRIT_STONE.get(), 1);
        SundryPricing.put(Items.AMETHYST_SHARD, (Item)ModItems.LOW_SPIRIT_STONE.get(), 1);
        SundryPricing.put(Items.CHORUS_FRUIT, (Item)ModItems.LOW_SPIRIT_STONE.get(), 2);
        SundryPricing.put(Items.NETHER_STAR, (Item)ModItems.SUPREME_SPIRIT_STONE.get(), 5);
        SundryPricing.put(Items.DRAGON_BREATH, (Item)ModItems.HIGH_SPIRIT_STONE.get(), 1);
        SundryPricing.put(Items.DRAGON_EGG, (Item)ModItems.SUPREME_SPIRIT_STONE.get(), 64);
        SundryPricing.put(Items.HEART_OF_THE_SEA, (Item)ModItems.SUPREME_SPIRIT_STONE.get(), 1);
        SundryPricing.put(Items.TOTEM_OF_UNDYING, (Item)ModItems.HIGH_SPIRIT_STONE.get(), 5);
        SundryPricing.put(Items.NAUTILUS_SHELL, (Item)ModItems.LOW_SPIRIT_STONE.get(), 3);
        SundryPricing.put(Items.BLAZE_POWDER, (Item)ModItems.LOW_SPIRIT_STONE.get(), 2);
        SundryPricing.put(Items.BLAZE_ROD, (Item)ModItems.LOW_SPIRIT_STONE.get(), 4);
        SundryPricing.put(Items.GHAST_TEAR, (Item)ModItems.LOW_SPIRIT_STONE.get(), 5);
        SundryPricing.put(Items.WITHER_SKELETON_SKULL, (Item)ModItems.HIGH_SPIRIT_STONE.get(), 1);
        SundryPricing.put(Items.PHANTOM_MEMBRANE, (Item)ModItems.LOW_SPIRIT_STONE.get(), 2);
        SundryPricing.put(Items.SHULKER_SHELL, (Item)ModItems.LOW_SPIRIT_STONE.get(), 5);
        SundryPricing.put(Items.GOLDEN_APPLE, (Item)ModItems.LOW_SPIRIT_STONE.get(), 5);
        SundryPricing.put(Items.ENCHANTED_GOLDEN_APPLE, (Item)ModItems.HIGH_SPIRIT_STONE.get(), 1);
    }
}

