package com.friday.cultivation.qi;

import com.friday.cultivation.QiElement;
import com.friday.cultivation.registry.ModBlocks;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.IdentityHashMap;
import java.util.Map;

/**
 * 灵气方块规格表 — 完全照搬原 mod: xiaoxiang.cultivation.cultivation.qi.BlockQiSpecs
 */
public final class BlockQiSpecs {
    private static final Map<Block, BlockQiSpec> SPECS = new IdentityHashMap<>();

    private static BlockQiSpec wood_micro() {
        return BlockQiSpec.of(QiElement.WOOD, 20, 0.02, 0.005);
    }

    private static BlockQiSpec wood_weak() {
        return BlockQiSpec.of(QiElement.WOOD, 30, 0.04, 0.01);
    }

    private static BlockQiSpec wood_mid() {
        return BlockQiSpec.of(QiElement.WOOD, 50, 0.05, 0.015);
    }

    private static BlockQiSpec wood_sub() {
        return BlockQiSpec.of(QiElement.WOOD, 80, 0.08, 0.02);
    }

    private static BlockQiSpec wood_strong() {
        return BlockQiSpec.of(QiElement.WOOD, 100, 0.1, 0.025);
    }

    private static BlockQiSpec earth_weak() {
        return BlockQiSpec.of(QiElement.EARTH, 30, 0.04, 0.01);
    }

    private static BlockQiSpec earth_mid() {
        return BlockQiSpec.of(QiElement.EARTH, 50, 0.05, 0.015);
    }

    private static BlockQiSpec earth_sub() {
        return BlockQiSpec.of(QiElement.EARTH, 80, 0.08, 0.02);
    }

    private static BlockQiSpec water_sub() {
        return BlockQiSpec.of(QiElement.WATER, 80, 0.08, 0.02);
    }

    private static BlockQiSpec water_normal() {
        return BlockQiSpec.of(QiElement.WATER, 100, 0.1, 0.02);
    }

    private static BlockQiSpec water_strong() {
        return BlockQiSpec.of(QiElement.WATER, 100, 0.1, 0.025);
    }

    private static BlockQiSpec water_high() {
        return BlockQiSpec.of(QiElement.WATER, 200, 0.2, 0.04);
    }

    private static BlockQiSpec fire_sub() {
        return BlockQiSpec.of(QiElement.FIRE, 80, 0.08, 0.02);
    }

    private static BlockQiSpec fire_strong() {
        return BlockQiSpec.of(QiElement.FIRE, 100, 0.1, 0.025);
    }

    private static BlockQiSpec fire_high() {
        return BlockQiSpec.of(QiElement.FIRE, 150, 0.15, 0.03);
    }

    private static BlockQiSpec fire_extreme() {
        return BlockQiSpec.of(QiElement.FIRE, 200, 0.2, 0.04);
    }

    private static BlockQiSpec ice_sub() {
        return BlockQiSpec.of(QiElement.ICE, 80, 0.08, 0.02);
    }

    private static BlockQiSpec ice_strong() {
        return BlockQiSpec.of(QiElement.ICE, 100, 0.1, 0.025);
    }

    private static BlockQiSpec metal_sub() {
        return BlockQiSpec.of(QiElement.METAL, 80, 0.08, 0.02);
    }

    private static BlockQiSpec metal_strong() {
        return BlockQiSpec.of(QiElement.METAL, 100, 0.1, 0.025);
    }

    private static BlockQiSpec metal_high() {
        return BlockQiSpec.of(QiElement.METAL, 150, 0.15, 0.03);
    }

    private static BlockQiSpec metal_artifact() {
        return BlockQiSpec.of(QiElement.METAL, 200, 0.2, 0.04);
    }

    private static BlockQiSpec pure_strong() {
        return BlockQiSpec.of(QiElement.PURE, 100, 0.1, 0.025);
    }

    private static BlockQiSpec pure_high() {
        return BlockQiSpec.of(QiElement.PURE, 200, 0.2, 0.04);
    }

    private static BlockQiSpec pure_artifact() {
        return BlockQiSpec.of(QiElement.PURE, 1000, 1.0, 0.1);
    }

    private static void applyHardcodedDefaults() {
        BlockQiSpecs.put(Blocks.GRASS_BLOCK, BlockQiSpec.ofWithDegrade(QiElement.WOOD, 50, 0.05, 0.015, BlockDegradeRule.of(200, Blocks.DIRT, 0.6)));
        BlockQiSpecs.put(Blocks.DIRT_PATH, BlockQiSpecs.wood_weak());
        BlockQiSpecs.put(Blocks.MYCELIUM, BlockQiSpecs.wood_mid());
        BlockQiSpecs.put(Blocks.PODZOL, BlockQiSpecs.wood_mid());
        BlockQiSpecs.put(Blocks.MOSS_BLOCK, BlockQiSpecs.wood_sub());
        BlockQiSpecs.put(Blocks.MOSS_CARPET, BlockQiSpecs.wood_weak());
        BlockQiSpecs.put(Blocks.GRASS, BlockQiSpecs.wood_weak());
        BlockQiSpecs.put(Blocks.TALL_GRASS, BlockQiSpecs.wood_weak());
        BlockQiSpecs.put(Blocks.FERN, BlockQiSpecs.wood_weak());
        BlockQiSpecs.put(Blocks.LARGE_FERN, BlockQiSpecs.wood_weak());
        BlockQiSpecs.put(Blocks.SUGAR_CANE, BlockQiSpecs.wood_mid());
        BlockQiSpecs.put(Blocks.BAMBOO, BlockQiSpecs.wood_mid());
        BlockQiSpecs.put(Blocks.BAMBOO_SAPLING, BlockQiSpecs.wood_weak());
        for (Block flower : new Block[]{Blocks.DANDELION, Blocks.POPPY, Blocks.BLUE_ORCHID, Blocks.ALLIUM, Blocks.AZURE_BLUET, Blocks.RED_TULIP, Blocks.ORANGE_TULIP, Blocks.WHITE_TULIP, Blocks.PINK_TULIP, Blocks.OXEYE_DAISY, Blocks.CORNFLOWER, Blocks.LILY_OF_THE_VALLEY, Blocks.WITHER_ROSE, Blocks.SUNFLOWER, Blocks.LILAC, Blocks.ROSE_BUSH, Blocks.PEONY, Blocks.TORCHFLOWER, Blocks.PITCHER_PLANT, Blocks.SPORE_BLOSSOM}) {
            BlockQiSpecs.put(flower, BlockQiSpecs.wood_sub());
        }
        for (Block crop : new Block[]{Blocks.WHEAT, Blocks.CARROTS, Blocks.POTATOES, Blocks.BEETROOTS, Blocks.PUMPKIN, Blocks.MELON, Blocks.PUMPKIN_STEM, Blocks.MELON_STEM, Blocks.COCOA, Blocks.SWEET_BERRY_BUSH, Blocks.GLOW_LICHEN, Blocks.HONEY_BLOCK, Blocks.HONEYCOMB_BLOCK, Blocks.CACTUS, Blocks.NETHER_SPROUTS, Blocks.WARPED_ROOTS, Blocks.WARPED_FUNGUS, Blocks.SEAGRASS, Blocks.TALL_SEAGRASS, Blocks.AZALEA, Blocks.FLOWERING_AZALEA, Blocks.HANGING_ROOTS, Blocks.SMALL_DRIPLEAF, Blocks.BIG_DRIPLEAF, Blocks.PINK_PETALS}) {
            BlockQiSpecs.put(crop, BlockQiSpecs.wood_mid());
        }
        for (Block sapling : new Block[]{Blocks.OAK_SAPLING, Blocks.SPRUCE_SAPLING, Blocks.BIRCH_SAPLING, Blocks.JUNGLE_SAPLING, Blocks.ACACIA_SAPLING, Blocks.DARK_OAK_SAPLING, Blocks.CHERRY_SAPLING, Blocks.MANGROVE_PROPAGULE}) {
            BlockQiSpecs.put(sapling, BlockQiSpecs.wood_mid());
        }
        for (Block leaves : new Block[]{Blocks.OAK_LEAVES, Blocks.SPRUCE_LEAVES, Blocks.BIRCH_LEAVES, Blocks.JUNGLE_LEAVES, Blocks.ACACIA_LEAVES, Blocks.DARK_OAK_LEAVES, Blocks.MANGROVE_LEAVES, Blocks.CHERRY_LEAVES, Blocks.AZALEA_LEAVES, Blocks.FLOWERING_AZALEA_LEAVES}) {
            BlockQiSpecs.put(leaves, BlockQiSpecs.wood_micro());
        }
        for (Block log : new Block[]{Blocks.OAK_LOG, Blocks.SPRUCE_LOG, Blocks.BIRCH_LOG, Blocks.JUNGLE_LOG, Blocks.ACACIA_LOG, Blocks.DARK_OAK_LOG, Blocks.MANGROVE_LOG, Blocks.CHERRY_LOG, Blocks.BAMBOO_BLOCK, Blocks.STRIPPED_OAK_LOG, Blocks.STRIPPED_SPRUCE_LOG, Blocks.STRIPPED_BIRCH_LOG, Blocks.STRIPPED_JUNGLE_LOG, Blocks.STRIPPED_ACACIA_LOG, Blocks.STRIPPED_DARK_OAK_LOG, Blocks.STRIPPED_MANGROVE_LOG, Blocks.STRIPPED_CHERRY_LOG, Blocks.STRIPPED_BAMBOO_BLOCK, Blocks.OAK_WOOD, Blocks.SPRUCE_WOOD, Blocks.BIRCH_WOOD, Blocks.JUNGLE_WOOD, Blocks.ACACIA_WOOD, Blocks.DARK_OAK_WOOD, Blocks.MANGROVE_WOOD, Blocks.CHERRY_WOOD, Blocks.STRIPPED_OAK_WOOD, Blocks.STRIPPED_SPRUCE_WOOD, Blocks.STRIPPED_BIRCH_WOOD, Blocks.STRIPPED_JUNGLE_WOOD, Blocks.STRIPPED_ACACIA_WOOD, Blocks.STRIPPED_DARK_OAK_WOOD, Blocks.STRIPPED_MANGROVE_WOOD, Blocks.STRIPPED_CHERRY_WOOD, Blocks.WARPED_STEM, Blocks.STRIPPED_WARPED_STEM, Blocks.WARPED_HYPHAE, Blocks.STRIPPED_WARPED_HYPHAE, Blocks.WARPED_NYLIUM}) {
            BlockQiSpecs.put(log, BlockQiSpecs.wood_strong());
        }
        for (Block mush : new Block[]{Blocks.BROWN_MUSHROOM, Blocks.RED_MUSHROOM, Blocks.BROWN_MUSHROOM_BLOCK, Blocks.RED_MUSHROOM_BLOCK, Blocks.MUSHROOM_STEM, Blocks.WARPED_WART_BLOCK}) {
            BlockQiSpecs.put(mush, BlockQiSpecs.wood_sub());
        }
        BlockQiSpecs.put(Blocks.DIRT, BlockQiSpec.ofWithDegrade(QiElement.EARTH, 30, 0.03, 0.01, BlockDegradeRule.of(300, Blocks.COARSE_DIRT, 0.4)));
        BlockQiSpecs.put(Blocks.COARSE_DIRT, BlockQiSpec.ofFull(QiElement.EARTH, 20, 0.02, 0.008, BlockDegradeRule.of(500, Blocks.GRAVEL, 0.3), BlockUpgradeRule.of(400, Blocks.DIRT, 0.3)));
        BlockQiSpecs.put(Blocks.ROOTED_DIRT, BlockQiSpecs.earth_weak());
        BlockQiSpecs.put(Blocks.GRAVEL, BlockQiSpec.ofFull(QiElement.EARTH, 10, 0.01, 0.005, BlockDegradeRule.of(800, Blocks.SAND, 0.2), BlockUpgradeRule.of(600, Blocks.COARSE_DIRT, 0.25)));
        for (Block earth : new Block[]{Blocks.MUD, Blocks.PACKED_MUD, Blocks.MUDDY_MANGROVE_ROOTS, Blocks.MUD_BRICKS, Blocks.MANGROVE_ROOTS}) {
            BlockQiSpecs.put(earth, BlockQiSpecs.earth_mid());
        }
        BlockQiSpecs.put(Blocks.SAND, BlockQiSpec.ofFull(QiElement.EARTH, 5, 0.005, 0.003, BlockDegradeRule.of(1000, Blocks.STONE, 0.1), BlockUpgradeRule.of(600, Blocks.GRAVEL, 0.25)));
        BlockQiSpecs.put(Blocks.RED_SAND, BlockQiSpec.ofFull(QiElement.EARTH, 5, 0.005, 0.003, BlockDegradeRule.of(1000, Blocks.STONE, 0.1), BlockUpgradeRule.of(600, Blocks.GRAVEL, 0.25)));
        BlockQiSpecs.put(Blocks.SUSPICIOUS_SAND, BlockQiSpec.of(QiElement.EARTH, 20, 0.02, 0.005));
        BlockQiSpecs.put(Blocks.SUSPICIOUS_GRAVEL, BlockQiSpec.of(QiElement.EARTH, 20, 0.02, 0.005));
        for (Block earth : new Block[]{Blocks.SANDSTONE, Blocks.SMOOTH_SANDSTONE, Blocks.CHISELED_SANDSTONE, Blocks.CUT_SANDSTONE, Blocks.RED_SANDSTONE, Blocks.SMOOTH_RED_SANDSTONE, Blocks.CHISELED_RED_SANDSTONE, Blocks.CUT_RED_SANDSTONE}) {
            BlockQiSpecs.put(earth, BlockQiSpecs.earth_mid());
        }
        BlockQiSpecs.put(Blocks.CLAY, BlockQiSpecs.earth_sub());
        BlockQiSpecs.put(Blocks.DRIED_KELP_BLOCK, BlockQiSpecs.earth_weak());
        for (Block earth : new Block[]{Blocks.TERRACOTTA, Blocks.WHITE_TERRACOTTA, Blocks.ORANGE_TERRACOTTA, Blocks.MAGENTA_TERRACOTTA, Blocks.LIGHT_BLUE_TERRACOTTA, Blocks.YELLOW_TERRACOTTA, Blocks.LIME_TERRACOTTA, Blocks.PINK_TERRACOTTA, Blocks.GRAY_TERRACOTTA, Blocks.LIGHT_GRAY_TERRACOTTA, Blocks.CYAN_TERRACOTTA, Blocks.PURPLE_TERRACOTTA, Blocks.BLUE_TERRACOTTA, Blocks.BROWN_TERRACOTTA, Blocks.GREEN_TERRACOTTA, Blocks.RED_TERRACOTTA, Blocks.BLACK_TERRACOTTA}) {
            BlockQiSpecs.put(earth, BlockQiSpecs.earth_mid());
        }
        BlockQiSpecs.put(Blocks.WATER, BlockQiSpecs.water_normal());
        BlockQiSpecs.put(Blocks.BUBBLE_COLUMN, BlockQiSpecs.water_sub());
        BlockQiSpecs.put(Blocks.KELP, BlockQiSpecs.water_sub());
        BlockQiSpecs.put(Blocks.KELP_PLANT, BlockQiSpecs.water_sub());
        BlockQiSpecs.put(Blocks.WET_SPONGE, BlockQiSpecs.water_strong());
        BlockQiSpecs.put(Blocks.SEA_PICKLE, BlockQiSpecs.water_sub());
        for (Block water : new Block[]{Blocks.PRISMARINE, Blocks.PRISMARINE_BRICKS, Blocks.DARK_PRISMARINE}) {
            BlockQiSpecs.put(water, BlockQiSpecs.water_strong());
        }
        for (Block water : new Block[]{Blocks.TUBE_CORAL_BLOCK, Blocks.BRAIN_CORAL_BLOCK, Blocks.BUBBLE_CORAL_BLOCK, Blocks.FIRE_CORAL_BLOCK, Blocks.HORN_CORAL_BLOCK, Blocks.TUBE_CORAL, Blocks.BRAIN_CORAL, Blocks.BUBBLE_CORAL, Blocks.FIRE_CORAL, Blocks.HORN_CORAL, Blocks.TUBE_CORAL_FAN, Blocks.BRAIN_CORAL_FAN, Blocks.BUBBLE_CORAL_FAN, Blocks.FIRE_CORAL_FAN, Blocks.HORN_CORAL_FAN, Blocks.TUBE_CORAL_WALL_FAN, Blocks.BRAIN_CORAL_WALL_FAN, Blocks.BUBBLE_CORAL_WALL_FAN, Blocks.FIRE_CORAL_WALL_FAN, Blocks.HORN_CORAL_WALL_FAN}) {
            BlockQiSpecs.put(water, BlockQiSpecs.water_sub());
        }
        BlockQiSpecs.put(Blocks.CONDUIT, BlockQiSpecs.water_high());
        BlockQiSpecs.put(Blocks.TURTLE_EGG, BlockQiSpecs.water_sub());
        BlockQiSpecs.put(Blocks.SNIFFER_EGG, BlockQiSpecs.water_sub());
        BlockQiSpecs.put(Blocks.LAVA, BlockQiSpecs.fire_extreme());
        BlockQiSpecs.put(Blocks.MAGMA_BLOCK, BlockQiSpecs.fire_extreme());
        BlockQiSpecs.put(Blocks.FIRE, BlockQiSpecs.fire_high());
        BlockQiSpecs.put(Blocks.SOUL_FIRE, BlockQiSpecs.fire_high());
        BlockQiSpecs.put(Blocks.NETHERRACK, BlockQiSpecs.fire_sub());
        BlockQiSpecs.put(Blocks.GLOWSTONE, BlockQiSpecs.fire_high());
        BlockQiSpecs.put(Blocks.SHROOMLIGHT, BlockQiSpecs.fire_high());
        BlockQiSpecs.put(Blocks.SOUL_SAND, BlockQiSpecs.fire_sub());
        BlockQiSpecs.put(Blocks.SOUL_SOIL, BlockQiSpecs.fire_sub());
        BlockQiSpecs.put(Blocks.NETHER_WART, BlockQiSpecs.fire_sub());
        BlockQiSpecs.put(Blocks.NETHER_WART_BLOCK, BlockQiSpecs.fire_strong());
        BlockQiSpecs.put(Blocks.CRIMSON_NYLIUM, BlockQiSpecs.fire_sub());
        for (Block crim : new Block[]{Blocks.CRIMSON_STEM, Blocks.STRIPPED_CRIMSON_STEM, Blocks.CRIMSON_HYPHAE, Blocks.STRIPPED_CRIMSON_HYPHAE}) {
            BlockQiSpecs.put(crim, BlockQiSpecs.fire_strong());
        }
        for (Block crim : new Block[]{Blocks.CRIMSON_FUNGUS, Blocks.CRIMSON_ROOTS, Blocks.WEEPING_VINES, Blocks.WEEPING_VINES_PLANT, Blocks.TWISTING_VINES, Blocks.TWISTING_VINES_PLANT}) {
            BlockQiSpecs.put(crim, BlockQiSpecs.fire_sub());
        }
        BlockQiSpecs.put(Blocks.RESPAWN_ANCHOR, BlockQiSpecs.fire_high());
        BlockQiSpecs.put(Blocks.NETHER_GOLD_ORE, BlockQiSpecs.fire_high());
        BlockQiSpecs.put(Blocks.CAMPFIRE, BlockQiSpecs.fire_strong());
        BlockQiSpecs.put(Blocks.SOUL_CAMPFIRE, BlockQiSpecs.fire_strong());
        BlockQiSpecs.put(Blocks.JACK_O_LANTERN, BlockQiSpecs.fire_sub());
        BlockQiSpecs.put(Blocks.TORCH, BlockQiSpecs.fire_sub());
        BlockQiSpecs.put(Blocks.WALL_TORCH, BlockQiSpecs.fire_sub());
        BlockQiSpecs.put(Blocks.SOUL_TORCH, BlockQiSpecs.fire_sub());
        BlockQiSpecs.put(Blocks.SOUL_WALL_TORCH, BlockQiSpecs.fire_sub());
        BlockQiSpecs.put(Blocks.LANTERN, BlockQiSpecs.fire_strong());
        BlockQiSpecs.put(Blocks.SOUL_LANTERN, BlockQiSpecs.fire_strong());
        for (Block nb : new Block[]{Blocks.NETHER_BRICKS, Blocks.CRACKED_NETHER_BRICKS, Blocks.CHISELED_NETHER_BRICKS, Blocks.RED_NETHER_BRICKS, Blocks.NETHER_BRICK_FENCE, Blocks.NETHER_BRICK_STAIRS, Blocks.RED_NETHER_BRICK_STAIRS, Blocks.NETHER_BRICK_SLAB, Blocks.RED_NETHER_BRICK_SLAB, Blocks.NETHER_BRICK_WALL, Blocks.RED_NETHER_BRICK_WALL}) {
            BlockQiSpecs.put(nb, BlockQiSpecs.fire_sub());
        }
        BlockQiSpecs.put(Blocks.NETHER_QUARTZ_ORE, BlockQiSpecs.fire_high());
        for (Block q : new Block[]{Blocks.QUARTZ_BLOCK, Blocks.SMOOTH_QUARTZ, Blocks.CHISELED_QUARTZ_BLOCK, Blocks.QUARTZ_PILLAR, Blocks.QUARTZ_BRICKS, Blocks.QUARTZ_STAIRS, Blocks.SMOOTH_QUARTZ_STAIRS, Blocks.QUARTZ_SLAB, Blocks.SMOOTH_QUARTZ_SLAB}) {
            BlockQiSpecs.put(q, BlockQiSpecs.fire_strong());
        }
        for (Block bs : new Block[]{Blocks.BLACKSTONE, Blocks.GILDED_BLACKSTONE, Blocks.POLISHED_BLACKSTONE, Blocks.POLISHED_BLACKSTONE_BRICKS, Blocks.CHISELED_POLISHED_BLACKSTONE, Blocks.CRACKED_POLISHED_BLACKSTONE_BRICKS, Blocks.BLACKSTONE_STAIRS, Blocks.POLISHED_BLACKSTONE_STAIRS, Blocks.POLISHED_BLACKSTONE_BRICK_STAIRS, Blocks.BLACKSTONE_SLAB, Blocks.POLISHED_BLACKSTONE_SLAB, Blocks.POLISHED_BLACKSTONE_BRICK_SLAB, Blocks.BLACKSTONE_WALL, Blocks.POLISHED_BLACKSTONE_WALL, Blocks.POLISHED_BLACKSTONE_BRICK_WALL, Blocks.POLISHED_BLACKSTONE_PRESSURE_PLATE, Blocks.POLISHED_BLACKSTONE_BUTTON}) {
            BlockQiSpecs.put(bs, BlockQiSpecs.fire_sub());
        }
        for (Block bs : new Block[]{Blocks.BASALT, Blocks.SMOOTH_BASALT, Blocks.POLISHED_BASALT}) {
            BlockQiSpecs.put(bs, BlockQiSpecs.fire_sub());
        }
        BlockQiSpecs.put(Blocks.ICE, BlockQiSpecs.ice_sub());
        BlockQiSpecs.put(Blocks.PACKED_ICE, BlockQiSpecs.ice_strong());
        BlockQiSpecs.put(Blocks.BLUE_ICE, BlockQiSpecs.ice_strong());
        BlockQiSpecs.put(Blocks.FROSTED_ICE, BlockQiSpecs.ice_sub());
        BlockQiSpecs.put(Blocks.SNOW, BlockQiSpec.of(QiElement.ICE, 30, 0.04, 0.01));
        BlockQiSpecs.put(Blocks.SNOW_BLOCK, BlockQiSpecs.ice_sub());
        BlockQiSpecs.put(Blocks.POWDER_SNOW, BlockQiSpecs.ice_strong());
        BlockQiSpecs.put(Blocks.IRON_BLOCK, BlockQiSpecs.metal_strong());
        BlockQiSpecs.put(Blocks.GOLD_BLOCK, BlockQiSpecs.metal_high());
        BlockQiSpecs.put(Blocks.COPPER_BLOCK, BlockQiSpecs.metal_strong());
        BlockQiSpecs.put(Blocks.NETHERITE_BLOCK, BlockQiSpecs.metal_artifact());
        BlockQiSpecs.put(Blocks.RAW_IRON_BLOCK, BlockQiSpecs.metal_sub());
        BlockQiSpecs.put(Blocks.RAW_GOLD_BLOCK, BlockQiSpecs.metal_strong());
        BlockQiSpecs.put(Blocks.RAW_COPPER_BLOCK, BlockQiSpecs.metal_sub());
        BlockQiSpecs.put(Blocks.IRON_ORE, BlockQiSpecs.metal_high());
        BlockQiSpecs.put(Blocks.DEEPSLATE_IRON_ORE, BlockQiSpecs.metal_high());
        BlockQiSpecs.put(Blocks.GOLD_ORE, BlockQiSpecs.metal_high());
        BlockQiSpecs.put(Blocks.DEEPSLATE_GOLD_ORE, BlockQiSpecs.metal_high());
        BlockQiSpecs.put(Blocks.COPPER_ORE, BlockQiSpecs.metal_high());
        BlockQiSpecs.put(Blocks.DEEPSLATE_COPPER_ORE, BlockQiSpecs.metal_high());
        BlockQiSpecs.put(Blocks.ANCIENT_DEBRIS, BlockQiSpecs.metal_artifact());
        BlockQiSpecs.put(Blocks.IRON_BARS, BlockQiSpecs.metal_sub());
        BlockQiSpecs.put(Blocks.CHAIN, BlockQiSpecs.metal_sub());
        BlockQiSpecs.put(Blocks.ANVIL, BlockQiSpecs.metal_strong());
        BlockQiSpecs.put(Blocks.CHIPPED_ANVIL, BlockQiSpecs.metal_sub());
        BlockQiSpecs.put(Blocks.DAMAGED_ANVIL, BlockQiSpecs.metal_sub());
        BlockQiSpecs.put(Blocks.LIGHTNING_ROD, BlockQiSpecs.metal_high());
        for (Block c : new Block[]{Blocks.EXPOSED_COPPER, Blocks.WEATHERED_COPPER, Blocks.OXIDIZED_COPPER, Blocks.WAXED_COPPER_BLOCK, Blocks.WAXED_EXPOSED_COPPER, Blocks.WAXED_WEATHERED_COPPER, Blocks.WAXED_OXIDIZED_COPPER, Blocks.CUT_COPPER, Blocks.EXPOSED_CUT_COPPER, Blocks.WEATHERED_CUT_COPPER, Blocks.OXIDIZED_CUT_COPPER}) {
            BlockQiSpecs.put(c, BlockQiSpecs.metal_strong());
        }
        BlockQiSpecs.put(Blocks.END_STONE, BlockQiSpecs.pure_high());
        BlockQiSpecs.put(Blocks.END_STONE_BRICKS, BlockQiSpecs.pure_high());
        BlockQiSpecs.put(Blocks.PURPUR_BLOCK, BlockQiSpecs.pure_high());
        BlockQiSpecs.put(Blocks.PURPUR_PILLAR, BlockQiSpecs.pure_high());
        BlockQiSpecs.put(Blocks.PURPUR_STAIRS, BlockQiSpecs.pure_strong());
        BlockQiSpecs.put(Blocks.PURPUR_SLAB, BlockQiSpecs.pure_strong());
        BlockQiSpecs.put(Blocks.DRAGON_EGG, BlockQiSpecs.pure_artifact());
        BlockQiSpecs.put(Blocks.END_PORTAL_FRAME, BlockQiSpecs.pure_artifact());
        BlockQiSpecs.put(Blocks.END_GATEWAY, BlockQiSpecs.pure_artifact());
        BlockQiSpecs.put(Blocks.BEACON, BlockQiSpecs.pure_artifact());
        BlockQiSpecs.put(Blocks.AMETHYST_BLOCK, BlockQiSpecs.pure_artifact());
        BlockQiSpecs.put(Blocks.BUDDING_AMETHYST, BlockQiSpecs.pure_artifact());
        BlockQiSpecs.put(Blocks.SMALL_AMETHYST_BUD, BlockQiSpecs.pure_high());
        BlockQiSpecs.put(Blocks.MEDIUM_AMETHYST_BUD, BlockQiSpecs.pure_high());
        BlockQiSpecs.put(Blocks.LARGE_AMETHYST_BUD, BlockQiSpecs.pure_artifact());
        BlockQiSpecs.put(Blocks.AMETHYST_CLUSTER, BlockQiSpecs.pure_artifact());
        BlockQiSpecs.put(ModBlocks.LOW_SPIRIT_STONE_ORE.get(), BlockQiSpec.of(QiElement.PURE, 2000, 5.0, 0.1));
        BlockQiSpecs.put(ModBlocks.MID_SPIRIT_STONE_ORE.get(), BlockQiSpec.of(QiElement.PURE, 4000, 10.0, 0.15));
        BlockQiSpecs.put(ModBlocks.HIGH_SPIRIT_STONE_ORE.get(), BlockQiSpec.of(QiElement.PURE, 8000, 20.0, 0.2));
        BlockQiSpecs.put(ModBlocks.SUPREME_SPIRIT_STONE_ORE.get(), BlockQiSpec.of(QiElement.PURE, 20000, 50.0, 0.3));
        BlockQiSpecs.put(ModBlocks.SPIRIT_VEIN_SPRING.get(), BlockQiSpec.of(QiElement.PURE, 50000, 80.0, 0.45));
        BlockQiSpecs.put(ModBlocks.CUSHION.get(), BlockQiSpecs.wood_strong());
    }

    private BlockQiSpecs() {
    }

    private static void put(Block block, BlockQiSpec spec) {
        if (block != null) {
            SPECS.put(block, spec);
        }
    }

    public static void resetToDefaults() {
        SPECS.clear();
        BlockQiSpecs.applyHardcodedDefaults();
    }

    public static void override(Block block, BlockQiSpec spec) {
        if (block != null && spec != null) {
            SPECS.put(block, spec);
        }
    }

    @Nullable
    public static BlockQiSpec of(BlockState state) {
        if (state == null || state.isAir()) {
            return null;
        }
        Block block = state.getBlock();
        BlockQiSpec direct = SPECS.get(block);
        if (direct != null) {
            return direct;
        }
        if (state.is(BlockTags.LEAVES)) {
            return BlockQiSpecs.wood_micro();
        }
        if (state.is(BlockTags.LOGS)) {
            return BlockQiSpecs.wood_strong();
        }
        if (state.is(BlockTags.SAPLINGS)) {
            return BlockQiSpecs.wood_mid();
        }
        if (state.is(BlockTags.FLOWERS)) {
            return BlockQiSpecs.wood_sub();
        }
        if (state.is(BlockTags.CROPS)) {
            return BlockQiSpecs.wood_mid();
        }
        if (state.is(BlockTags.DIRT)) {
            return BlockQiSpecs.earth_weak();
        }
        if (state.is(BlockTags.SAND)) {
            return BlockQiSpec.of(QiElement.EARTH, 20, 0.02, 0.005);
        }
        if (state.is(BlockTags.ICE)) {
            return BlockQiSpecs.ice_sub();
        }
        return null;
    }

    @Nullable
    public static QiElement elementOf(BlockState state) {
        BlockQiSpec spec = BlockQiSpecs.of(state);
        return spec != null ? spec.element() : null;
    }

    static {
        BlockQiSpecs.applyHardcodedDefaults();
    }
}
