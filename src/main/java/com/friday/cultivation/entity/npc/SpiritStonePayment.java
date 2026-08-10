/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.world.SimpleContainer
 *  net.minecraft.world.item.Item
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.level.ItemLike
 */
package com.friday.cultivation.entity.npc;

import com.friday.cultivation.registry.ModItems;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;

public final class SpiritStonePayment {
    public static final int DECOMPOSE_RATIO = 9;

    private SpiritStonePayment() {
    }

    public static boolean tryPay(SimpleContainer inv, Item targetItem, int targetCount) {
        if (targetCount <= 0) {
            return true;
        }
        ItemStack[] snapshot = SpiritStonePayment.snapshot(inv);
        int remaining = targetCount;
        int safety = 100;
        while (remaining > 0 && safety-- > 0) {
            int taken;
            if ((remaining -= (taken = SpiritStonePayment.takeMatching(inv, targetItem, remaining))) == 0) {
                return true;
            }
            Item higher = SpiritStonePayment.findNearestHigherInInv(inv, targetItem);
            if (higher == null) {
                SpiritStonePayment.restore(inv, snapshot);
                return false;
            }
            SpiritStonePayment.takeMatching(inv, higher, 1);
            Item nextLower = SpiritStonePayment.nextLowerTier(higher);
            if (nextLower == null) continue;
            inv.addItem(new ItemStack((ItemLike)nextLower, 9));
        }
        if (remaining > 0) {
            SpiritStonePayment.restore(inv, snapshot);
            return false;
        }
        return true;
    }

    private static int takeMatching(SimpleContainer inv, Item kind, int wanted) {
        int taken = 0;
        for (int i = 0; i < inv.getContainerSize() && taken < wanted; ++i) {
            ItemStack slot = inv.getItem(i);
            if (slot.isEmpty() || slot.getItem() != kind) continue;
            int n = Math.min(slot.getCount(), wanted - taken);
            slot.shrink(n);
            taken += n;
        }
        return taken;
    }

    private static Item findNearestHigherInInv(SimpleContainer inv, Item base) {
        Item cursor = SpiritStonePayment.nextHigherTier(base);
        while (cursor != null) {
            for (int i = 0; i < inv.getContainerSize(); ++i) {
                ItemStack slot = inv.getItem(i);
                if (slot.isEmpty() || slot.getItem() != cursor || slot.getCount() <= 0) continue;
                return cursor;
            }
            cursor = SpiritStonePayment.nextHigherTier(cursor);
        }
        return null;
    }

    public static Item nextHigherTier(Item current) {
        if (current == ModItems.LOW_SPIRIT_STONE.get()) {
            return (Item)ModItems.MID_SPIRIT_STONE.get();
        }
        if (current == ModItems.MID_SPIRIT_STONE.get()) {
            return (Item)ModItems.HIGH_SPIRIT_STONE.get();
        }
        if (current == ModItems.HIGH_SPIRIT_STONE.get()) {
            return (Item)ModItems.SUPREME_SPIRIT_STONE.get();
        }
        return null;
    }

    public static Item nextLowerTier(Item current) {
        if (current == ModItems.SUPREME_SPIRIT_STONE.get()) {
            return (Item)ModItems.HIGH_SPIRIT_STONE.get();
        }
        if (current == ModItems.HIGH_SPIRIT_STONE.get()) {
            return (Item)ModItems.MID_SPIRIT_STONE.get();
        }
        if (current == ModItems.MID_SPIRIT_STONE.get()) {
            return (Item)ModItems.LOW_SPIRIT_STONE.get();
        }
        return null;
    }

    private static ItemStack[] snapshot(SimpleContainer inv) {
        ItemStack[] s = new ItemStack[inv.getContainerSize()];
        for (int i = 0; i < s.length; ++i) {
            s[i] = inv.getItem(i).copy();
        }
        return s;
    }

    private static void restore(SimpleContainer inv, ItemStack[] s) {
        for (int i = 0; i < s.length; ++i) {
            inv.setItem(i, s[i]);
        }
    }
}

