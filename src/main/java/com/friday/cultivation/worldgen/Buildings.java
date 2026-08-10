/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.BlockPos
 *  net.minecraft.core.BlockPos$MutableBlockPos
 *  net.minecraft.core.Direction
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.util.RandomSource
 *  net.minecraft.world.level.WorldGenLevel
 *  net.minecraft.world.level.block.Block
 *  net.minecraft.world.level.block.Blocks
 *  net.minecraft.world.level.block.CampfireBlock
 *  net.minecraft.world.level.block.HorizontalDirectionalBlock
 *  net.minecraft.world.level.block.entity.BlockEntity
 *  net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity
 *  net.minecraft.world.level.block.state.BlockState
 *  net.minecraft.world.level.block.state.properties.Property
 */
package com.friday.cultivation.worldgen;

import com.friday.cultivation.registry.ModBlocks;
import com.friday.cultivation.worldgen.CultivationChestLoot;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;

public final class Buildings {
    public static final ResourceLocation LOOT_COMPLETE = new ResourceLocation("friday_cultivation", "chests/cultivation_complete");
    public static final ResourceLocation LOOT_RUINED = new ResourceLocation("friday_cultivation", "chests/cultivation_ruined");

    private Buildings() {
    }

    public static void build(WorldGenLevel level, BlockPos origin, RandomSource rand, Type type) {
        switch (type) {
            case HUT: {
                Buildings.buildHut(level, origin, rand);
                break;
            }
            case PAVILION: {
                Buildings.buildPavilion(level, origin, rand);
                break;
            }
            case TOWER: {
                Buildings.buildTower(level, origin, rand);
                break;
            }
            case LIBRARY: {
                Buildings.buildLibrary(level, origin, rand);
                break;
            }
            case SHRINE: {
                Buildings.buildShrine(level, origin, rand);
                break;
            }
            case RUINED_HUT: {
                Buildings.buildRuinedHut(level, origin, rand);
                break;
            }
            case RUINED_TOWER: {
                Buildings.buildRuinedTower(level, origin, rand);
                break;
            }
            case ANCIENT_TOMB: {
                Buildings.buildAncientTomb(level, origin, rand);
                break;
            }
            case FALLEN_ALTAR: {
                Buildings.buildFallenAltar(level, origin, rand);
                break;
            }
            case BURNT_CAMP: {
                Buildings.buildBurntCamp(level, origin, rand);
            }
        }
    }

    private static void buildHut(WorldGenLevel l, BlockPos o, RandomSource r) {
        Type t = Type.HUT;
        Buildings.fill(l, o.offset(0, 0, 0), o.offset(4, 0, 4), Blocks.STONE_BRICKS.defaultBlockState());
        for (int y = 1; y <= 3; ++y) {
            for (int i = 0; i <= 4; ++i) {
                Buildings.set(l, o.offset(0, y, i), Blocks.COBBLESTONE.defaultBlockState());
                Buildings.set(l, o.offset(4, y, i), Blocks.COBBLESTONE.defaultBlockState());
                Buildings.set(l, o.offset(i, y, 0), Blocks.COBBLESTONE.defaultBlockState());
                Buildings.set(l, o.offset(i, y, 4), Blocks.COBBLESTONE.defaultBlockState());
            }
        }
        Buildings.set(l, o.offset(2, 1, 0), Blocks.AIR.defaultBlockState());
        Buildings.set(l, o.offset(2, 2, 0), Blocks.AIR.defaultBlockState());
        Buildings.set(l, o.offset(4, 2, 2), Blocks.GLASS_PANE.defaultBlockState());
        for (int dx = 0; dx <= 4; ++dx) {
            for (int dz = 0; dz <= 4; ++dz) {
                Buildings.set(l, o.offset(dx, 4, dz), Blocks.DARK_OAK_PLANKS.defaultBlockState());
            }
        }
        Buildings.set(l, o.offset(2, 1, 2), ((Block)ModBlocks.CUSHION.get()).defaultBlockState());
        Buildings.placeChest(l, o.offset(1, 1, 3), r, t.lootTable(), Direction.SOUTH);
        Buildings.set(l, o.offset(3, 3, 2), Blocks.LANTERN.defaultBlockState());
    }

    private static void buildPavilion(WorldGenLevel l, BlockPos o, RandomSource r) {
        Type t = Type.PAVILION;
        Buildings.fill(l, o.offset(0, 0, 0), o.offset(6, 0, 6), Blocks.SMOOTH_STONE.defaultBlockState());
        for (int y = 1; y <= 3; ++y) {
            Buildings.set(l, o.offset(0, y, 0), Blocks.COBBLESTONE_WALL.defaultBlockState());
            Buildings.set(l, o.offset(6, y, 0), Blocks.COBBLESTONE_WALL.defaultBlockState());
            Buildings.set(l, o.offset(0, y, 6), Blocks.COBBLESTONE_WALL.defaultBlockState());
            Buildings.set(l, o.offset(6, y, 6), Blocks.COBBLESTONE_WALL.defaultBlockState());
        }
        for (int dx = 0; dx <= 6; ++dx) {
            for (int dz = 0; dz <= 6; ++dz) {
                Buildings.set(l, o.offset(dx, 4, dz), Blocks.DARK_OAK_PLANKS.defaultBlockState());
            }
        }
        Buildings.set(l, o.offset(3, 3, 3), Blocks.LANTERN.defaultBlockState());
        Buildings.set(l, o.offset(3, 1, 3), Blocks.CHISELED_STONE_BRICKS.defaultBlockState());
        Buildings.placeChest(l, o.offset(3, 1, 5), r, t.lootTable(), Direction.SOUTH);
    }

    private static void buildTower(WorldGenLevel l, BlockPos o, RandomSource r) {
        Type t = Type.TOWER;
        for (int y = 0; y <= 7; ++y) {
            for (int dx = 0; dx <= 2; ++dx) {
                for (int dz = 0; dz <= 2; ++dz) {
                    boolean edge;
                    boolean bl = edge = dx == 0 || dx == 2 || dz == 0 || dz == 2;
                    if (edge) {
                        Buildings.set(l, o.offset(dx, y, dz), Blocks.STONE_BRICKS.defaultBlockState());
                        continue;
                    }
                    if (y == 0) {
                        Buildings.set(l, o.offset(dx, y, dz), Blocks.STONE_BRICKS.defaultBlockState());
                        continue;
                    }
                    Buildings.set(l, o.offset(dx, y, dz), Blocks.AIR.defaultBlockState());
                }
            }
        }
        Buildings.set(l, o.offset(1, 1, 0), Blocks.AIR.defaultBlockState());
        Buildings.set(l, o.offset(1, 2, 0), Blocks.AIR.defaultBlockState());
        Buildings.set(l, o.offset(0, 4, 1), Blocks.GLASS_PANE.defaultBlockState());
        Buildings.set(l, o.offset(2, 4, 1), Blocks.GLASS_PANE.defaultBlockState());
        for (int dx = 0; dx <= 2; ++dx) {
            for (int dz = 0; dz <= 2; ++dz) {
                Buildings.set(l, o.offset(dx, 8, dz), Blocks.DARK_OAK_PLANKS.defaultBlockState());
            }
        }
        Buildings.set(l, o.offset(1, 8, 1), Blocks.AIR.defaultBlockState());
        Buildings.set(l, o.offset(1, 6, 1), Blocks.BREWING_STAND.defaultBlockState());
        Buildings.set(l, o.offset(1, 5, 1), Blocks.LANTERN.defaultBlockState());
        Buildings.placeChest(l, o.offset(1, 1, 1), r, t.lootTable(), Direction.NORTH);
    }

    private static void buildLibrary(WorldGenLevel l, BlockPos o, RandomSource r) {
        Type t = Type.LIBRARY;
        Buildings.fill(l, o.offset(0, 0, 0), o.offset(4, 0, 4), Blocks.OAK_PLANKS.defaultBlockState());
        for (int i = 0; i <= 4; ++i) {
            Buildings.set(l, o.offset(0, 1, i), Blocks.COBBLESTONE.defaultBlockState());
            Buildings.set(l, o.offset(4, 1, i), Blocks.COBBLESTONE.defaultBlockState());
            Buildings.set(l, o.offset(i, 1, 0), Blocks.COBBLESTONE.defaultBlockState());
            Buildings.set(l, o.offset(i, 1, 4), Blocks.COBBLESTONE.defaultBlockState());
            for (int y = 2; y <= 3; ++y) {
                Buildings.set(l, o.offset(0, y, i), Blocks.BOOKSHELF.defaultBlockState());
                Buildings.set(l, o.offset(4, y, i), Blocks.BOOKSHELF.defaultBlockState());
                Buildings.set(l, o.offset(i, y, 0), Blocks.BOOKSHELF.defaultBlockState());
                Buildings.set(l, o.offset(i, y, 4), Blocks.BOOKSHELF.defaultBlockState());
            }
        }
        Buildings.set(l, o.offset(2, 1, 0), Blocks.AIR.defaultBlockState());
        Buildings.set(l, o.offset(2, 2, 0), Blocks.AIR.defaultBlockState());
        Buildings.set(l, o.offset(2, 3, 0), Blocks.AIR.defaultBlockState());
        for (int dx = 0; dx <= 4; ++dx) {
            for (int dz = 0; dz <= 4; ++dz) {
                Buildings.set(l, o.offset(dx, 4, dz), Blocks.DARK_OAK_PLANKS.defaultBlockState());
            }
        }
        Buildings.set(l, o.offset(2, 1, 2), Blocks.LECTERN.defaultBlockState());
        Buildings.placeChest(l, o.offset(2, 1, 3), r, t.lootTable(), Direction.SOUTH);
        Buildings.set(l, o.offset(2, 3, 2), Blocks.LANTERN.defaultBlockState());
    }

    private static void buildShrine(WorldGenLevel l, BlockPos o, RandomSource r) {
        Type t = Type.SHRINE;
        Buildings.fill(l, o.offset(0, 0, 0), o.offset(4, 0, 4), Blocks.STONE_BRICKS.defaultBlockState());
        for (int y = 1; y <= 4; ++y) {
            Buildings.set(l, o.offset(0, y, 0), Blocks.STONE_BRICK_WALL.defaultBlockState());
            Buildings.set(l, o.offset(4, y, 0), Blocks.STONE_BRICK_WALL.defaultBlockState());
            Buildings.set(l, o.offset(0, y, 4), Blocks.STONE_BRICK_WALL.defaultBlockState());
            Buildings.set(l, o.offset(4, y, 4), Blocks.STONE_BRICK_WALL.defaultBlockState());
        }
        for (int dx = 0; dx <= 4; ++dx) {
            for (int dz = 0; dz <= 4; ++dz) {
                Buildings.set(l, o.offset(dx, 5, dz), Blocks.STONE_BRICK_SLAB.defaultBlockState());
            }
        }
        Buildings.set(l, o.offset(2, 1, 2), Blocks.CHISELED_STONE_BRICKS.defaultBlockState());
        Buildings.set(l, o.offset(2, 2, 2), Blocks.LANTERN.defaultBlockState());
        Buildings.placeChest(l, o.offset(2, 1, 3), r, t.lootTable(), Direction.SOUTH);
    }

    private static void buildRuinedHut(WorldGenLevel l, BlockPos o, RandomSource r) {
        Type t = Type.RUINED_HUT;
        Buildings.fill(l, o.offset(0, 0, 0), o.offset(4, 0, 4), Blocks.MOSSY_COBBLESTONE.defaultBlockState());
        for (int y = 1; y <= 2; ++y) {
            for (int i = 0; i <= 4; ++i) {
                if (r.nextFloat() < 0.7f) {
                    Buildings.set(l, o.offset(0, y, i), Blocks.MOSSY_COBBLESTONE.defaultBlockState());
                }
                if (r.nextFloat() < 0.7f) {
                    Buildings.set(l, o.offset(4, y, i), Blocks.MOSSY_COBBLESTONE.defaultBlockState());
                }
                if (r.nextFloat() < 0.7f) {
                    Buildings.set(l, o.offset(i, y, 0), Blocks.MOSSY_COBBLESTONE.defaultBlockState());
                }
                if (!(r.nextFloat() < 0.7f)) continue;
                Buildings.set(l, o.offset(i, y, 4), Blocks.MOSSY_COBBLESTONE.defaultBlockState());
            }
        }
        Buildings.set(l, o.offset(1, 2, 1), Blocks.COBWEB.defaultBlockState());
        Buildings.set(l, o.offset(3, 1, 3), Blocks.COBWEB.defaultBlockState());
        Buildings.placeChest(l, o.offset(2, 1, 2), r, t.lootTable(), Direction.SOUTH);
    }

    private static void buildRuinedTower(WorldGenLevel l, BlockPos o, RandomSource r) {
        int dz;
        int dx;
        int y;
        Type t = Type.RUINED_TOWER;
        Buildings.fill(l, o.offset(0, 0, 0), o.offset(2, 0, 2), Blocks.MOSSY_STONE_BRICKS.defaultBlockState());
        for (y = 1; y <= 2; ++y) {
            for (dx = 0; dx <= 2; ++dx) {
                for (dz = 0; dz <= 2; ++dz) {
                    if (dx != 0 && dx != 2 && dz != 0 && dz != 2) continue;
                    Buildings.set(l, o.offset(dx, y, dz), Blocks.CRACKED_STONE_BRICKS.defaultBlockState());
                }
            }
        }
        for (y = 3; y <= 4; ++y) {
            for (dx = 0; dx <= 2; ++dx) {
                for (dz = 0; dz <= 2; ++dz) {
                    if (dx != 0 && dx != 2 && dz != 0 && dz != 2 || !(r.nextFloat() < 0.5f)) continue;
                    Buildings.set(l, o.offset(dx, y, dz), Blocks.MOSSY_COBBLESTONE.defaultBlockState());
                }
            }
        }
        Buildings.set(l, o.offset(1, 1, 0), Blocks.AIR.defaultBlockState());
        Buildings.set(l, o.offset(1, 2, 0), Blocks.AIR.defaultBlockState());
        Buildings.placeChest(l, o.offset(1, 1, 1), r, t.lootTable(), Direction.NORTH);
    }

    private static void buildAncientTomb(WorldGenLevel l, BlockPos o, RandomSource r) {
        Type t = Type.ANCIENT_TOMB;
        for (int y = -3; y <= -1; ++y) {
            for (int dx = 0; dx <= 4; ++dx) {
                for (int dz = 0; dz <= 4; ++dz) {
                    boolean edge = dx == 0 || dx == 4 || dz == 0 || dz == 4 || y == -3;
                    Buildings.set(l, o.offset(dx, y, dz), edge ? Blocks.MOSSY_STONE_BRICKS.defaultBlockState() : Blocks.AIR.defaultBlockState());
                }
            }
        }
        for (int dx = 0; dx <= 4; ++dx) {
            for (int dz = 0; dz <= 4; ++dz) {
                Buildings.set(l, o.offset(dx, 0, dz), Blocks.MOSSY_STONE_BRICKS.defaultBlockState());
            }
        }
        Buildings.set(l, o.offset(2, -2, 2), Blocks.CHISELED_STONE_BRICKS.defaultBlockState());
        Buildings.set(l, o.offset(2, -2, 1), Blocks.CHISELED_STONE_BRICKS.defaultBlockState());
        Buildings.set(l, o.offset(2, -2, 3), Blocks.CHISELED_STONE_BRICKS.defaultBlockState());
        Buildings.set(l, o.offset(1, -1, 1), Blocks.COBWEB.defaultBlockState());
        Buildings.set(l, o.offset(3, -1, 3), Blocks.COBWEB.defaultBlockState());
        Buildings.placeChest(l, o.offset(1, -2, 2), r, t.lootTable(), Direction.EAST);
        Buildings.set(l, o.offset(2, 0, 0), Blocks.AIR.defaultBlockState());
        Buildings.set(l, o.offset(2, -1, 0), Blocks.AIR.defaultBlockState());
        Buildings.set(l, o.offset(2, -1, 1), Blocks.AIR.defaultBlockState());
    }

    private static void buildFallenAltar(WorldGenLevel l, BlockPos o, RandomSource r) {
        Type t = Type.FALLEN_ALTAR;
        for (int dx = 0; dx <= 4; ++dx) {
            for (int dz = 0; dz <= 4; ++dz) {
                if (!(r.nextFloat() < 0.6f)) continue;
                BlockState b = r.nextFloat() < 0.5f ? Blocks.MOSSY_COBBLESTONE.defaultBlockState() : Blocks.COBBLESTONE.defaultBlockState();
                Buildings.set(l, o.offset(dx, 0, dz), b);
            }
        }
        Buildings.set(l, o.offset(2, 0, 2), Blocks.CHISELED_STONE_BRICKS.defaultBlockState());
        Buildings.set(l, o.offset(1, 0, 2), Blocks.MOSSY_COBBLESTONE.defaultBlockState());
        Buildings.set(l, o.offset(3, 0, 2), Blocks.MOSSY_COBBLESTONE.defaultBlockState());
        Buildings.set(l, o.offset(2, 1, 2), Blocks.STONE_BRICK_WALL.defaultBlockState());
        Buildings.placeChest(l, o.offset(2, 1, 1), r, t.lootTable(), Direction.SOUTH);
    }

    private static void buildBurntCamp(WorldGenLevel l, BlockPos o, RandomSource r) {
        int[][] corners;
        Type t = Type.BURNT_CAMP;
        for (int dx = 0; dx <= 4; ++dx) {
            for (int dz = 0; dz <= 4; ++dz) {
                if (!(r.nextFloat() < 0.7f)) continue;
                Buildings.set(l, o.offset(dx, 0, dz), Blocks.GRAY_CONCRETE_POWDER.defaultBlockState());
            }
        }
        for (int[] c : corners = new int[][]{{0, 0}, {4, 0}, {0, 4}, {4, 4}}) {
            int h = 1 + r.nextInt(2);
            for (int y = 1; y <= h; ++y) {
                Buildings.set(l, o.offset(c[0], y, c[1]), Blocks.DARK_OAK_LOG.defaultBlockState());
            }
        }
        Buildings.set(l, o.offset(2, 1, 2), (BlockState)Blocks.CAMPFIRE.defaultBlockState().setValue((Property)CampfireBlock.LIT, (Comparable)Boolean.valueOf(false)));
        Buildings.placeChest(l, o.offset(1, 1, 2), r, t.lootTable(), Direction.EAST);
    }

    private static void set(WorldGenLevel l, BlockPos pos, BlockState state) {
        l.setBlock(pos, state, 2);
    }

    private static void fill(WorldGenLevel l, BlockPos a, BlockPos b, BlockState state) {
        int x0 = Math.min(a.getX(), b.getX());
        int x1 = Math.max(a.getX(), b.getX());
        int y0 = Math.min(a.getY(), b.getY());
        int y1 = Math.max(a.getY(), b.getY());
        int z0 = Math.min(a.getZ(), b.getZ());
        int z1 = Math.max(a.getZ(), b.getZ());
        BlockPos.MutableBlockPos m = new BlockPos.MutableBlockPos();
        for (int x = x0; x <= x1; ++x) {
            for (int y = y0; y <= y1; ++y) {
                for (int z = z0; z <= z1; ++z) {
                    l.setBlock((BlockPos)m.set(x, y, z), state, 2);
                }
            }
        }
    }

    private static void placeChest(WorldGenLevel l, BlockPos pos, RandomSource r, ResourceLocation lootTable, Direction facing) {
        BlockState chestState = (BlockState)Blocks.CHEST.defaultBlockState().setValue((Property)HorizontalDirectionalBlock.FACING, (Comparable)facing);
        l.setBlock(pos, chestState, 2);
        BlockEntity be = l.getBlockEntity(pos);
        if (be instanceof RandomizableContainerBlockEntity) {
            RandomizableContainerBlockEntity randomizable = (RandomizableContainerBlockEntity)be;
            CultivationChestLoot.fill(randomizable, r, LOOT_RUINED.equals((Object)lootTable));
        }
    }

    public static enum Type {
        HUT,
        PAVILION,
        TOWER,
        LIBRARY,
        SHRINE,
        RUINED_HUT,
        RUINED_TOWER,
        ANCIENT_TOMB,
        FALLEN_ALTAR,
        BURNT_CAMP;


        public boolean isRuined() {
            return this.ordinal() >= 5;
        }

        public ResourceLocation lootTable() {
            return this.isRuined() ? LOOT_RUINED : LOOT_COMPLETE;
        }
    }
}

