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

/**
 * 炼器炉结构校验 — 完整复刻原模组 RefiningFurnaceStructure。
 * 5×5×5 多方块结构，含4根铜柱+灵魂沙核心+切制铜顶部。
 */
public final class RefiningFurnaceStructure {
    private static final List<RequiredBlock> REQUIRED = buildRequired();

    private RefiningFurnaceStructure() {}

    public static List<RequiredBlock> requiredBlocks() { return REQUIRED; }

    public static int copperBlockCount() {
        int n = 0;
        for (RequiredBlock rb : REQUIRED) { if (rb.block() != Blocks.COPPER_BLOCK) continue; ++n; }
        return n;
    }

    public static int totalBlockCount() { return REQUIRED.size(); }

    private static List<RequiredBlock> buildRequired() {
        ArrayList<RequiredBlock> list = new ArrayList<>();
        // 底层4角铜块 + 中心灵魂沙
        list.add(new RequiredBlock(new BlockPos(-2, -2, -2), Blocks.COPPER_BLOCK));
        list.add(new RequiredBlock(new BlockPos(-2, -2, 2), Blocks.COPPER_BLOCK));
        list.add(new RequiredBlock(new BlockPos(2, -2, -2), Blocks.COPPER_BLOCK));
        list.add(new RequiredBlock(new BlockPos(2, -2, 2), Blocks.COPPER_BLOCK));
        list.add(new RequiredBlock(new BlockPos(0, -2, 0), Blocks.SOUL_SAND));
        // 第二层3×3铜块
        for (int dx = -1; dx <= 1; ++dx) {
            for (int dz = -1; dz <= 1; ++dz) {
                list.add(new RequiredBlock(new BlockPos(dx, -1, dz), Blocks.COPPER_BLOCK));
            }
        }
        // 第三层铜块环
        int[][] y0Copper = {{-1, 0, -1}, {-1, 0, 1}, {0, 0, -2}, {0, 0, -1}, {0, 0, 1}, {0, 0, 2}, {1, 0, -1}, {1, 0, 1}};
        for (int[] p : y0Copper) list.add(new RequiredBlock(new BlockPos(p[0], p[1], p[2]), Blocks.COPPER_BLOCK));
        // 第四层铜块环
        int[][] y1Copper = {{-1, 1, -1}, {-1, 1, 0}, {-1, 1, 1}, {0, 1, -3}, {0, 1, -1}, {0, 1, 1}, {0, 1, 3}, {1, 1, -1}, {1, 1, 0}, {1, 1, 1}};
        for (int[] p : y1Copper) list.add(new RequiredBlock(new BlockPos(p[0], p[1], p[2]), Blocks.COPPER_BLOCK));
        // 顶层4个切制铜
        list.add(new RequiredBlock(new BlockPos(-1, 2, 0), Blocks.CUT_COPPER));
        list.add(new RequiredBlock(new BlockPos(0, 2, -1), Blocks.CUT_COPPER));
        list.add(new RequiredBlock(new BlockPos(0, 2, 1), Blocks.CUT_COPPER));
        list.add(new RequiredBlock(new BlockPos(1, 2, 0), Blocks.CUT_COPPER));
        return Collections.unmodifiableList(list);
    }

    public static boolean isComplete(LevelReader level, BlockPos corePos) {
        for (RequiredBlock rb : REQUIRED) {
            BlockPos abs = corePos.offset((Vec3i) rb.offset());
            if (rb.matches(level.getBlockState(abs))) continue;
            return false;
        }
        return true;
    }

    public static List<MissingBlock> missingBlocks(LevelReader level, BlockPos corePos) {
        ArrayList<MissingBlock> missing = new ArrayList<>();
        for (RequiredBlock rb : REQUIRED) {
            BlockPos abs = corePos.offset((Vec3i) rb.offset());
            if (rb.matches(level.getBlockState(abs))) continue;
            missing.add(new MissingBlock(abs, rb.block()));
        }
        return missing;
    }

    public record RequiredBlock(BlockPos offset, Block block) {
        public boolean matches(BlockState state) { return BlockVariantMatcher.matches(state, this.block); }
    }

    public record MissingBlock(BlockPos pos, Block expected) {}
}
