package com.friday.cultivation.entity.npc;

import com.friday.cultivation.item.ModItems;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

/**
 * 灵石支付辅助（严格照搬原模组 com.xiaoxiang.cultivation.entity.npc.SpiritStonePayment）
 * 支持高阶灵石自动拆分为低阶（9:1）进行支付
 */
public final class SpiritStonePayment {
    public static final int DECOMPOSE_RATIO = 9;

    private SpiritStonePayment() {}

    public static boolean tryPay(SimpleContainer inv, Item targetItem, int targetCount) {
        if (targetCount <= 0) {
            return true;
        }
        ItemStack[] snapshot = snapshot(inv);
        int remaining = targetCount;
        int safety = 100;
        while (remaining > 0 && safety-- > 0) {
            int taken = takeMatching(inv, targetItem, remaining);
            if ((remaining -= taken) == 0) {
                return true;
            }
            Item higher = findNearestHigherInInv(inv, targetItem);
            if (higher == null) {
                restore(inv, snapshot);
                return false;
            }
            takeMatching(inv, higher, 1);
            Item nextLower = nextLowerTier(higher);
            if (nextLower == null) continue;
            inv.addItem(new ItemStack(nextLower, DECOMPOSE_RATIO));
        }
        if (remaining > 0) {
            restore(inv, snapshot);
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
        Item cursor = nextHigherTier(base);
        while (cursor != null) {
            for (int i = 0; i < inv.getContainerSize(); ++i) {
                ItemStack slot = inv.getItem(i);
                if (slot.isEmpty() || slot.getItem() != cursor || slot.getCount() <= 0) continue;
                return cursor;
            }
            cursor = nextHigherTier(cursor);
        }
        return null;
    }

    public static Item nextHigherTier(Item current) {
        if (current == ModItems.LOW_SPIRIT_STONE.get()) return ModItems.MID_SPIRIT_STONE.get();
        if (current == ModItems.MID_SPIRIT_STONE.get()) return ModItems.HIGH_SPIRIT_STONE.get();
        if (current == ModItems.HIGH_SPIRIT_STONE.get()) return ModItems.SUPREME_SPIRIT_STONE.get();
        return null;
    }

    public static Item nextLowerTier(Item current) {
        if (current == ModItems.SUPREME_SPIRIT_STONE.get()) return ModItems.HIGH_SPIRIT_STONE.get();
        if (current == ModItems.HIGH_SPIRIT_STONE.get()) return ModItems.MID_SPIRIT_STONE.get();
        if (current == ModItems.MID_SPIRIT_STONE.get()) return ModItems.LOW_SPIRIT_STONE.get();
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