/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.ChatFormatting
 *  net.minecraft.network.chat.Component
 *  net.minecraft.util.RandomSource
 */
package com.friday.cultivation.cultivation.refining;

import com.friday.cultivation.cultivation.ItemTier;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.util.RandomSource;

public enum RefiningRank {
    APPRENTICE("apprentice", ChatFormatting.GRAY, 100, new int[]{80, 10, 4, 3, 2, 1}),
    RANK_1("rank_1", ChatFormatting.WHITE, 250, new int[]{60, 25, 8, 4, 2, 1}),
    RANK_2("rank_2", ChatFormatting.WHITE, 500, new int[]{45, 35, 12, 5, 2, 1}),
    RANK_3("rank_3", ChatFormatting.AQUA, 1000, new int[]{30, 40, 18, 8, 3, 1}),
    RANK_4("rank_4", ChatFormatting.AQUA, 2000, new int[]{20, 35, 28, 12, 4, 1}),
    RANK_5("rank_5", ChatFormatting.GREEN, 4000, new int[]{12, 25, 35, 20, 6, 2}),
    RANK_6("rank_6", ChatFormatting.GREEN, 8000, new int[]{8, 15, 35, 30, 10, 2}),
    RANK_7("rank_7", ChatFormatting.LIGHT_PURPLE, 16000, new int[]{5, 8, 25, 40, 18, 4}),
    RANK_8("rank_8", ChatFormatting.LIGHT_PURPLE, 32000, new int[]{3, 5, 15, 40, 30, 7}),
    RANK_9("rank_9", ChatFormatting.GOLD, 64000, new int[]{2, 3, 8, 25, 45, 17}),
    IMMORTAL("immortal", ChatFormatting.RED, Integer.MAX_VALUE, new int[]{1, 2, 3, 4, 10, 80});

    private final String id;
    private final ChatFormatting color;
    private final int xpToNext;
    private final int[] probabilities;

    private RefiningRank(String id, ChatFormatting color, int xpToNext, int[] probabilities) {
        this.id = id;
        this.color = color;
        this.xpToNext = xpToNext;
        this.probabilities = probabilities;
    }

    public String id() {
        return this.id;
    }

    public ChatFormatting color() {
        return this.color;
    }

    public int xpToNext() {
        return this.xpToNext;
    }

    public int[] probabilities() {
        return this.probabilities;
    }

    public Component displayName() {
        return Component.translatable((String)("refining_rank.friday_cultivation." + this.id));
    }

    public boolean isMax() {
        return this == IMMORTAL;
    }

    public RefiningRank next() {
        RefiningRank[] vals = RefiningRank.values();
        int idx = this.ordinal();
        return idx + 1 < vals.length ? vals[idx + 1] : this;
    }

    public ItemTier rollItemResult(RandomSource rng) {
        int roll = rng.nextInt(100);
        int cum = 0;
        for (int i = 0; i < this.probabilities.length; ++i) {
            if (roll >= (cum += this.probabilities[i])) continue;
            if (i == 0) {
                return null;
            }
            return ItemTier.values()[i - 1];
        }
        return null;
    }

    public static int xpGainFor(ItemTier tier) {
        return switch (tier) {
            default -> throw new IncompatibleClassChangeError();
            case LOW -> 5;
            case MID -> 15;
            case HIGH -> 50;
            case SUPREME -> 150;
            case IMMORTAL -> 500;
        };
    }

    public static int xpGainForFailure() {
        return 2;
    }

    public static RefiningRank byId(String id) {
        for (RefiningRank r : RefiningRank.values()) {
            if (!r.id.equals(id)) continue;
            return r;
        }
        return APPRENTICE;
    }
}

