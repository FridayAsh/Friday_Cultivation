/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.BlockPos
 *  net.minecraft.core.Vec3i
 *  net.minecraft.world.level.LevelReader
 *  net.minecraft.world.level.block.Block
 *  net.minecraft.world.level.block.Blocks
 *  net.minecraft.world.level.block.state.BlockState
 */
package com.friday.cultivation.block.refining;

import com.friday.cultivation.block.BlockVariantMatcher;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

public final class RefiningFurnaceStructure {
    private static final List<RequiredBlock> REQUIRED = RefiningFurnaceStructure.buildRequired();

    private RefiningFurnaceStructure() {
    }

    public static List<RequiredBlock> requiredBlocks() {
        return REQUIRED;
    }

    public static int totalBlockCount() {
        return REQUIRED.size();
    }

    private static List<RequiredBlock> buildRequired() {
        int[][] y0Copper;
        ArrayList<RequiredBlock> list = new ArrayList<RequiredBlock>();
        list.add(new RequiredBlock(new BlockPos(-1, -2, -2), Blocks.CUT_COPPER_STAIRS));
        list.add(new RequiredBlock(new BlockPos(-1, -2, 2), Blocks.CUT_COPPER_STAIRS));
        list.add(new RequiredBlock(new BlockPos(1, -2, -2), Blocks.CUT_COPPER_STAIRS));
        list.add(new RequiredBlock(new BlockPos(1, -2, 2), Blocks.CUT_COPPER_STAIRS));
        list.add(new RequiredBlock(new BlockPos(0, -2, 0), Blocks.SOUL_CAMPFIRE));
        list.add(new RequiredBlock(new BlockPos(0, -1, -1), Blocks.COPPER_BLOCK));
        list.add(new RequiredBlock(new BlockPos(0, -1, 0), Blocks.COPPER_BLOCK));
        list.add(new RequiredBlock(new BlockPos(0, -1, 1), Blocks.COPPER_BLOCK));
        list.add(new RequiredBlock(new BlockPos(-1, -1, -1), Blocks.CUT_COPPER_STAIRS));
        list.add(new RequiredBlock(new BlockPos(-1, -1, 0), Blocks.CUT_COPPER_STAIRS));
        list.add(new RequiredBlock(new BlockPos(-1, -1, 1), Blocks.CUT_COPPER_STAIRS));
        list.add(new RequiredBlock(new BlockPos(1, -1, -1), Blocks.CUT_COPPER_STAIRS));
        list.add(new RequiredBlock(new BlockPos(1, -1, 0), Blocks.CUT_COPPER_STAIRS));
        list.add(new RequiredBlock(new BlockPos(1, -1, 1), Blocks.CUT_COPPER_STAIRS));
        list.add(new RequiredBlock(new BlockPos(0, -1, -2), Blocks.CUT_COPPER_STAIRS));
        list.add(new RequiredBlock(new BlockPos(0, -1, 2), Blocks.CUT_COPPER_STAIRS));
        for (int[] p : y0Copper = new int[][]{{-1, 0, -2}, {-1, 0, -1}, {-1, 0, 1}, {-1, 0, 2}, {0, 0, -2}, {0, 0, 2}, {1, 0, -2}, {1, 0, -1}, {1, 0, 1}, {1, 0, 2}}) {
            list.add(new RequiredBlock(new BlockPos(p[0], p[1], p[2]), Blocks.COPPER_BLOCK));
        }
        list.add(new RequiredBlock(new BlockPos(-1, 1, -2), Blocks.CUT_COPPER_STAIRS));
        list.add(new RequiredBlock(new BlockPos(-1, 1, 2), Blocks.CUT_COPPER_STAIRS));
        list.add(new RequiredBlock(new BlockPos(1, 1, -2), Blocks.CUT_COPPER_STAIRS));
        list.add(new RequiredBlock(new BlockPos(1, 1, 2), Blocks.CUT_COPPER_STAIRS));
        list.add(new RequiredBlock(new BlockPos(-1, 1, -1), Blocks.CUT_COPPER_SLAB));
        list.add(new RequiredBlock(new BlockPos(-1, 1, 0), Blocks.CUT_COPPER_SLAB));
        list.add(new RequiredBlock(new BlockPos(-1, 1, 1), Blocks.CUT_COPPER_SLAB));
        list.add(new RequiredBlock(new BlockPos(0, 1, -2), Blocks.CUT_COPPER_SLAB));
        list.add(new RequiredBlock(new BlockPos(0, 1, 2), Blocks.CUT_COPPER_SLAB));
        list.add(new RequiredBlock(new BlockPos(1, 1, -1), Blocks.CUT_COPPER_SLAB));
        list.add(new RequiredBlock(new BlockPos(1, 1, 0), Blocks.CUT_COPPER_SLAB));
        list.add(new RequiredBlock(new BlockPos(1, 1, 1), Blocks.CUT_COPPER_SLAB));
        return Collections.unmodifiableList(list);
    }

    public static boolean isComplete(LevelReader level, BlockPos corePos) {
        for (RequiredBlock rb : REQUIRED) {
            if (rb.matches(level.getBlockState(corePos.offset((Vec3i)rb.offset)))) continue;
            return false;
        }
        return true;
    }

    public static List<MissingBlock> missingBlocks(LevelReader level, BlockPos corePos) {
        ArrayList<MissingBlock> missing = new ArrayList<MissingBlock>();
        for (RequiredBlock rb : REQUIRED) {
            BlockPos abs;
            if (rb.matches(level.getBlockState(abs = corePos.offset((Vec3i)rb.offset)))) continue;
            missing.add(new MissingBlock(abs, rb.block));
        }
        return missing;
    }

    public record RequiredBlock(BlockPos offset, Block block) {
        public boolean matches(BlockState state) {
            return BlockVariantMatcher.matches(state, this.block);
        }
    }

    public record MissingBlock(BlockPos pos, Block expected) {
    }
}

