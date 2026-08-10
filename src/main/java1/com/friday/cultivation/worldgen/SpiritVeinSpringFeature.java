package com.friday.cultivation.worldgen;

import com.friday.cultivation.registry.ModBlocks;
import com.mojang.serialization.Codec;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.function.Supplier;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import org.jetbrains.annotations.NotNull;

/**
 * 灵脉涌泉生成（严格照搬原模组 com.xiaoxiang.cultivation.worldgen.SpiritVeinSpringFeature）
 */
public class SpiritVeinSpringFeature extends Feature<NoneFeatureConfiguration> {
    private static final int SEARCH_RADIUS_XZ = 8;
    private static final int SEARCH_RADIUS_Y = 4;

    public SpiritVeinSpringFeature(Codec<NoneFeatureConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(@NotNull FeaturePlaceContext<NoneFeatureConfiguration> ctx) {
        BlockPos origin;
        RandomSource random = ctx.random();
        WorldGenLevel level = ctx.level();
        List<OreAnchor> anchors = SpiritVeinSpringFeature.findOreAnchors(level, origin = ctx.origin());
        if (anchors.isEmpty()) {
            return false;
        }
        ArrayList<Direction> directions = new ArrayList<>(List.of(Direction.values()));
        for (OreTier tier : OreTier.values()) {
            List<BlockPos> ores = SpiritVeinSpringFeature.positionsForTier(anchors, tier);
            if (ores.isEmpty() || random.nextFloat() >= tier.springChance) continue;
            Collections.shuffle(ores, new Random(random.nextLong()));
            for (BlockPos orePos : ores) {
                Collections.shuffle(directions, new Random(random.nextLong()));
                for (Direction dir : directions) {
                    BlockPos target = orePos.relative(dir);
                    if (!SpiritVeinSpringFeature.canReplace(level.getBlockState(target))) continue;
                    level.setBlock(target, ModBlocks.SPIRIT_VEIN_SPRING.get().defaultBlockState(), 2);
                    return true;
                }
            }
        }
        return false;
    }

    private static List<OreAnchor> findOreAnchors(WorldGenLevel level, BlockPos origin) {
        ArrayList<OreAnchor> anchors = new ArrayList<>();
        BlockPos min = origin.offset(-8, -4, -8);
        BlockPos max = origin.offset(8, 4, 8);
        for (BlockPos pos : BlockPos.betweenClosed(min, max)) {
            BlockState state = level.getBlockState(pos);
            OreTier tier = OreTier.match(state);
            if (tier == null) continue;
            anchors.add(new OreAnchor(pos.immutable(), tier));
        }
        return anchors;
    }

    private static List<BlockPos> positionsForTier(List<OreAnchor> anchors, OreTier tier) {
        ArrayList<BlockPos> positions = new ArrayList<>();
        for (OreAnchor anchor : anchors) {
            if (anchor.tier != tier) continue;
            positions.add(anchor.pos);
        }
        return positions;
    }

    private static boolean canReplace(BlockState state) {
        return state.is(BlockTags.BASE_STONE_OVERWORLD) || state.is(BlockTags.BASE_STONE_NETHER) || state.is(BlockTags.STONE_ORE_REPLACEABLES);
    }

    private static enum OreTier {
        SUPREME(() -> (Block) ModBlocks.SUPREME_SPIRIT_STONE_ORE.get(), 0.1f),
        HIGH(() -> (Block) ModBlocks.HIGH_SPIRIT_STONE_ORE.get(), 0.06f),
        MID(() -> (Block) ModBlocks.MID_SPIRIT_STONE_ORE.get(), 0.03f),
        LOW(() -> (Block) ModBlocks.LOW_SPIRIT_STONE_ORE.get(), 0.01f);

        private final Supplier<Block> block;
        private final float springChance;

        private OreTier(Supplier<Block> block, float springChance) {
            this.block = block;
            this.springChance = springChance;
        }

        private boolean matches(BlockState state) {
            return state.is(this.block.get());
        }

        private static OreTier match(BlockState state) {
            for (OreTier tier : OreTier.values()) {
                if (!tier.matches(state)) continue;
                return tier;
            }
            return null;
        }
    }

    private record OreAnchor(BlockPos pos, OreTier tier) {
    }
}
