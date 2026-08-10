package com.friday.cultivation.entity.npc;

import com.friday.cultivation.item.ModItems;
import com.friday.cultivation.util.CultivationRandomPools;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * 杂项交易定价表 - 把 Minecraft 原版物品映射成对应灵石价格。
 * 严格 1:1 复刻原 mod SundryPricing（1.20.1 Mojang 映射）。
 */
public final class SundryPricing {
    private static final Map<Item, ItemStack> UNIT_PRICE = new HashMap<>();

    private SundryPricing() {
    }

    private static void put(Item item, Item priceItem, int priceCount) {
        UNIT_PRICE.put(item, new ItemStack((ItemLike) priceItem, priceCount));
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
        return new ItemStack((ItemLike) unit.getItem(), cappedCount);
    }

    public static boolean isAccepted(Item item) {
        return !CultivationRandomPools.isForbiddenNaturalLootItem(item) && UNIT_PRICE.containsKey(item);
    }

    public static Set<Item> allAccepted() {
        HashSet<Item> result = new HashSet<>();
        for (Item item : UNIT_PRICE.keySet()) {
            if (CultivationRandomPools.isForbiddenNaturalLootItem(item)) continue;
            result.add(item);
        }
        return result;
    }

    static {
        put(Items.COMPASS, ModItems.LOW_SPIRIT_STONE.get(), 1);
        put(Items.IRON_INGOT, ModItems.LOW_SPIRIT_STONE.get(), 9);
        put(Items.GOLD_INGOT, ModItems.LOW_SPIRIT_STONE.get(), 1);
        put(Items.DIAMOND, ModItems.LOW_SPIRIT_STONE.get(), 2);
        put(Items.REDSTONE, ModItems.LOW_SPIRIT_STONE.get(), 18);
        put(Items.LAPIS_LAZULI, ModItems.LOW_SPIRIT_STONE.get(), 2);
        put(Items.EMERALD, ModItems.MID_SPIRIT_STONE.get(), 1);
        put(Items.QUARTZ, ModItems.MID_SPIRIT_STONE.get(), 9);
        put(Items.NETHERITE_INGOT, ModItems.LOW_SPIRIT_STONE.get(), 1);
        put(Items.AMETHYST_SHARD, ModItems.MID_SPIRIT_STONE.get(), 1);
        put(Items.NETHER_STAR, ModItems.SUPREME_SPIRIT_STONE.get(), 1);
        put(Items.DRAGON_HEAD, ModItems.HIGH_SPIRIT_STONE.get(), 1);
        put(Items.NETHERITE_UPGRADE_SMITHING_TEMPLATE, ModItems.SUPREME_SPIRIT_STONE.get(), 9);
        put(Items.BREAD, ModItems.LOW_SPIRIT_STONE.get(), 1);
        put(Items.WHEAT, ModItems.LOW_SPIRIT_STONE.get(), 9);
        put(Items.PUMPKIN, ModItems.LOW_SPIRIT_STONE.get(), 1);
        put(Items.SUGAR, ModItems.LOW_SPIRIT_STONE.get(), 9);
        put(Items.CAKE, ModItems.LOW_SPIRIT_STONE.get(), 1);
        put(Items.GOLDEN_APPLE, ModItems.LOW_SPIRIT_STONE.get(), 4);
        put(Items.ENCHANTED_GOLDEN_APPLE, ModItems.LOW_SPIRIT_STONE.get(), 5);
        put(Items.DIAMOND_SWORD, ModItems.MID_SPIRIT_STONE.get(), 1);
        put(Items.GOLDEN_SWORD, ModItems.LOW_SPIRIT_STONE.get(), 1);
        put(Items.NETHERITE_SWORD, ModItems.LOW_SPIRIT_STONE.get(), 2);
        put(Items.DIAMOND_PICKAXE, ModItems.SUPREME_SPIRIT_STONE.get(), 5);
        put(Items.GOLDEN_PICKAXE, ModItems.HIGH_SPIRIT_STONE.get(), 1);
        put(Items.SCULK_CATALYST, ModItems.SUPREME_SPIRIT_STONE.get(), 64);
        put(Items.ECHO_SHARD, ModItems.SUPREME_SPIRIT_STONE.get(), 1);
        put(Items.DISC_FRAGMENT_5, ModItems.HIGH_SPIRIT_STONE.get(), 5);
        put(Items.PHANTOM_MEMBRANE, ModItems.LOW_SPIRIT_STONE.get(), 3);
        put(Items.BLAZE_POWDER, ModItems.LOW_SPIRIT_STONE.get(), 2);
        put(Items.BLAZE_ROD, ModItems.LOW_SPIRIT_STONE.get(), 4);
        put(Items.NETHER_WART, ModItems.LOW_SPIRIT_STONE.get(), 5);
        put(Items.SHULKER_SHELL, ModItems.HIGH_SPIRIT_STONE.get(), 1);
        put(Items.SHULKER_BOX, ModItems.LOW_SPIRIT_STONE.get(), 2);
        put(Items.NETHERITE_SCRAP, ModItems.LOW_SPIRIT_STONE.get(), 5);
        put(Items.LEATHER, ModItems.LOW_SPIRIT_STONE.get(), 5);
        put(Items.ENDER_PEARL, ModItems.HIGH_SPIRIT_STONE.get(), 1);
    }
}
