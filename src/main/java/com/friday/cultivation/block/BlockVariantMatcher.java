/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.world.level.block.Block
 *  net.minecraft.world.level.block.Blocks
 *  net.minecraft.world.level.block.state.BlockState
 */
package com.friday.cultivation.block;

import java.util.Map;
import java.util.Set;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

public final class BlockVariantMatcher {
    private static final Set<Block> COPPER_BLOCK_FAMILY = Set.of(Blocks.COPPER_BLOCK, Blocks.EXPOSED_COPPER, Blocks.WEATHERED_COPPER, Blocks.OXIDIZED_COPPER, Blocks.WAXED_COPPER_BLOCK, Blocks.WAXED_EXPOSED_COPPER, Blocks.WAXED_WEATHERED_COPPER, Blocks.WAXED_OXIDIZED_COPPER);
    private static final Set<Block> CUT_COPPER_FAMILY = Set.of(Blocks.CUT_COPPER, Blocks.EXPOSED_CUT_COPPER, Blocks.WEATHERED_CUT_COPPER, Blocks.OXIDIZED_CUT_COPPER, Blocks.WAXED_CUT_COPPER, Blocks.WAXED_EXPOSED_CUT_COPPER, Blocks.WAXED_WEATHERED_CUT_COPPER, Blocks.WAXED_OXIDIZED_CUT_COPPER);
    private static final Set<Block> CUT_COPPER_SLAB_FAMILY = Set.of(Blocks.CUT_COPPER_SLAB, Blocks.EXPOSED_CUT_COPPER_SLAB, Blocks.WEATHERED_CUT_COPPER_SLAB, Blocks.OXIDIZED_CUT_COPPER_SLAB, Blocks.WAXED_CUT_COPPER_SLAB, Blocks.WAXED_EXPOSED_CUT_COPPER_SLAB, Blocks.WAXED_WEATHERED_CUT_COPPER_SLAB, Blocks.WAXED_OXIDIZED_CUT_COPPER_SLAB);
    private static final Set<Block> CUT_COPPER_STAIRS_FAMILY = Set.of(Blocks.CUT_COPPER_STAIRS, Blocks.EXPOSED_CUT_COPPER_STAIRS, Blocks.WEATHERED_CUT_COPPER_STAIRS, Blocks.OXIDIZED_CUT_COPPER_STAIRS, Blocks.WAXED_CUT_COPPER_STAIRS, Blocks.WAXED_EXPOSED_CUT_COPPER_STAIRS, Blocks.WAXED_WEATHERED_CUT_COPPER_STAIRS, Blocks.WAXED_OXIDIZED_CUT_COPPER_STAIRS);
    private static final Map<Block, Set<Block>> FAMILIES = Map.of(Blocks.COPPER_BLOCK, COPPER_BLOCK_FAMILY, Blocks.CUT_COPPER, CUT_COPPER_FAMILY, Blocks.CUT_COPPER_SLAB, CUT_COPPER_SLAB_FAMILY, Blocks.CUT_COPPER_STAIRS, CUT_COPPER_STAIRS_FAMILY);

    private BlockVariantMatcher() {
    }

    public static boolean matches(BlockState state, Block expected) {
        if (state.is(expected)) {
            return true;
        }
        Set<Block> family = FAMILIES.get(expected);
        if (family == null) {
            return false;
        }
        for (Block variant : family) {
            if (!state.is(variant)) continue;
            return true;
        }
        return false;
    }
}

