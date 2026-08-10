package com.friday.cultivation.worldgen;

import com.friday.cultivation.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

/**
 * 修仙建筑生成器（严格照搬原模组 com.xiaoxiang.cultivation.worldgen.Buildings）
 * 10种建筑类型：HUT, PAVILION, TOWER, LIBRARY, SHRINE, RUINED_HUT, RUINED_TOWER, ANCIENT_TOMB, FALLEN_ALTAR, BURNT_CAMP
 */
public final class Buildings {

    public enum Type {
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

        /** 是否为废墟类型（照搬原模组 Type.isRuined：ordinal>=5 为废墟） */
        public boolean isRuined() {
            return this.ordinal() >= 5;
        }

        public ResourceLocation lootTable() {
            return this.isRuined() ? LOOT_RUINED : LOOT_COMPLETE;
        }
    }

    public static final ResourceLocation LOOT_COMPLETE = new ResourceLocation("friday_cultivation", "chests/cultivation_complete");
    public static final ResourceLocation LOOT_RUINED = new ResourceLocation("friday_cultivation", "chests/cultivation_ruined");

    private Buildings() {}

    public static void build(WorldGenLevel level, BlockPos origin, RandomSource rand, Type type) {
        switch (type) {
            case HUT -> buildHut(level, origin, rand);
            case PAVILION -> buildPavilion(level, origin, rand);
            case TOWER -> buildTower(level, origin, rand);
            case LIBRARY -> buildLibrary(level, origin, rand);
            case SHRINE -> buildShrine(level, origin, rand);
            case RUINED_HUT -> buildRuinedHut(level, origin, rand);
            case RUINED_TOWER -> buildRuinedTower(level, origin, rand);
            case ANCIENT_TOMB -> buildAncientTomb(level, origin, rand);
            case FALLEN_ALTAR -> buildFallenAltar(level, origin, rand);
            case BURNT_CAMP -> buildBurntCamp(level, origin, rand);
        }
    }

    private static void buildHut(WorldGenLevel l, BlockPos o, RandomSource r) {
        fill(l, o.offset(0, 0, 0), o.offset(4, 0, 4), Blocks.STONE_BRICKS.defaultBlockState());
        for (int y = 1; y <= 3; ++y) {
            for (int i = 0; i <= 4; ++i) {
                set(l, o.offset(0, y, i), Blocks.OAK_PLANKS.defaultBlockState());
                set(l, o.offset(4, y, i), Blocks.OAK_PLANKS.defaultBlockState());
                set(l, o.offset(i, y, 0), Blocks.OAK_PLANKS.defaultBlockState());
                set(l, o.offset(i, y, 4), Blocks.OAK_PLANKS.defaultBlockState());
            }
        }
        set(l, o.offset(2, 1, 0), Blocks.AIR.defaultBlockState());
        set(l, o.offset(2, 2, 0), Blocks.AIR.defaultBlockState());
        set(l, o.offset(4, 2, 2), Blocks.GLASS_PANE.defaultBlockState());
        for (int dx = 0; dx <= 4; ++dx) {
            for (int dz = 0; dz <= 4; ++dz) {
                set(l, o.offset(dx, 4, dz), Blocks.OAK_SLAB.defaultBlockState());
            }
        }
        set(l, o.offset(2, 1, 2), ModBlocks.CUSHION.get().defaultBlockState());
        placeChest(l, o.offset(1, 1, 3), r, Type.HUT.lootTable(), Direction.SOUTH);
        set(l, o.offset(3, 3, 2), Blocks.LANTERN.defaultBlockState());
    }

    private static void buildPavilion(WorldGenLevel l, BlockPos o, RandomSource r) {
        fill(l, o.offset(0, 0, 0), o.offset(6, 0, 6), Blocks.STONE_BRICK_SLAB.defaultBlockState());
        for (int y = 1; y <= 3; ++y) {
            set(l, o.offset(0, y, 0), Blocks.OAK_WOOD.defaultBlockState());
            set(l, o.offset(6, y, 0), Blocks.OAK_WOOD.defaultBlockState());
            set(l, o.offset(0, y, 6), Blocks.OAK_WOOD.defaultBlockState());
            set(l, o.offset(6, y, 6), Blocks.OAK_WOOD.defaultBlockState());
        }
        for (int dx = 0; dx <= 6; ++dx) {
            for (int dz = 0; dz <= 6; ++dz) {
                set(l, o.offset(dx, 4, dz), Blocks.OAK_SLAB.defaultBlockState());
            }
        }
        set(l, o.offset(3, 3, 3), Blocks.LANTERN.defaultBlockState());
        set(l, o.offset(3, 1, 3), Blocks.SOUL_LANTERN.defaultBlockState());
        placeChest(l, o.offset(3, 1, 5), r, Type.PAVILION.lootTable(), Direction.SOUTH);
    }

    private static void buildTower(WorldGenLevel l, BlockPos o, RandomSource r) {
        for (int y = 0; y <= 7; ++y) {
            for (int dx = 0; dx <= 2; ++dx) {
                for (int dz = 0; dz <= 2; ++dz) {
                    boolean edge = dx == 0 || dx == 2 || dz == 0 || dz == 2;
                    if (edge) {
                        set(l, o.offset(dx, y, dz), Blocks.STONE_BRICKS.defaultBlockState());
                    } else if (y == 0) {
                        set(l, o.offset(dx, y, dz), Blocks.STONE_BRICKS.defaultBlockState());
                    } else {
                        set(l, o.offset(dx, y, dz), Blocks.AIR.defaultBlockState());
                    }
                }
            }
        }
        set(l, o.offset(1, 1, 0), Blocks.AIR.defaultBlockState());
        set(l, o.offset(1, 2, 0), Blocks.AIR.defaultBlockState());
        set(l, o.offset(0, 4, 1), Blocks.GLASS_PANE.defaultBlockState());
        set(l, o.offset(2, 4, 1), Blocks.GLASS_PANE.defaultBlockState());
        for (int dx = 0; dx <= 2; ++dx) {
            for (int dz = 0; dz <= 2; ++dz) {
                set(l, o.offset(dx, 8, dz), Blocks.OAK_SLAB.defaultBlockState());
            }
        }
        set(l, o.offset(1, 8, 1), Blocks.AIR.defaultBlockState());
        set(l, o.offset(1, 6, 1), Blocks.COMPOSTER.defaultBlockState());
        set(l, o.offset(1, 5, 1), Blocks.LANTERN.defaultBlockState());
        placeChest(l, o.offset(1, 1, 1), r, Type.TOWER.lootTable(), Direction.NORTH);
    }

    private static void buildLibrary(WorldGenLevel l, BlockPos o, RandomSource r) {
        fill(l, o.offset(0, 0, 0), o.offset(4, 0, 4), Blocks.BOOKSHELF.defaultBlockState());
        for (int i = 0; i <= 4; ++i) {
            set(l, o.offset(0, 1, i), Blocks.OAK_PLANKS.defaultBlockState());
            set(l, o.offset(4, 1, i), Blocks.OAK_PLANKS.defaultBlockState());
            set(l, o.offset(i, 1, 0), Blocks.OAK_PLANKS.defaultBlockState());
            set(l, o.offset(i, 1, 4), Blocks.OAK_PLANKS.defaultBlockState());
            for (int y = 2; y <= 3; ++y) {
                set(l, o.offset(0, y, i), Blocks.COBBLESTONE.defaultBlockState());
                set(l, o.offset(4, y, i), Blocks.COBBLESTONE.defaultBlockState());
                set(l, o.offset(i, y, 0), Blocks.COBBLESTONE.defaultBlockState());
                set(l, o.offset(i, y, 4), Blocks.COBBLESTONE.defaultBlockState());
            }
        }
        set(l, o.offset(2, 1, 0), Blocks.AIR.defaultBlockState());
        set(l, o.offset(2, 2, 0), Blocks.AIR.defaultBlockState());
        set(l, o.offset(2, 3, 0), Blocks.AIR.defaultBlockState());
        for (int dx = 0; dx <= 4; ++dx) {
            for (int dz = 0; dz <= 4; ++dz) {
                set(l, o.offset(dx, 4, dz), Blocks.OAK_SLAB.defaultBlockState());
            }
        }
        set(l, o.offset(2, 1, 2), Blocks.ENCHANTING_TABLE.defaultBlockState());
        placeChest(l, o.offset(2, 1, 3), r, Type.LIBRARY.lootTable(), Direction.SOUTH);
        set(l, o.offset(2, 3, 2), Blocks.LANTERN.defaultBlockState());
    }

    private static void buildShrine(WorldGenLevel l, BlockPos o, RandomSource r) {
        fill(l, o.offset(0, 0, 0), o.offset(4, 0, 4), Blocks.STONE_BRICKS.defaultBlockState());
        for (int y = 1; y <= 4; ++y) {
            set(l, o.offset(0, y, 0), Blocks.STONE.defaultBlockState());
            set(l, o.offset(4, y, 0), Blocks.STONE.defaultBlockState());
            set(l, o.offset(0, y, 4), Blocks.STONE.defaultBlockState());
            set(l, o.offset(4, y, 4), Blocks.STONE.defaultBlockState());
        }
        for (int dx = 0; dx <= 4; ++dx) {
            for (int dz = 0; dz <= 4; ++dz) {
                set(l, o.offset(dx, 5, dz), Blocks.POLISHED_BLACKSTONE_SLAB.defaultBlockState());
            }
        }
        set(l, o.offset(2, 1, 2), Blocks.SOUL_LANTERN.defaultBlockState());
        set(l, o.offset(2, 2, 2), Blocks.LANTERN.defaultBlockState());
        placeChest(l, o.offset(2, 1, 3), r, Type.SHRINE.lootTable(), Direction.SOUTH);
    }

    private static void buildRuinedHut(WorldGenLevel l, BlockPos o, RandomSource r) {
        fill(l, o.offset(0, 0, 0), o.offset(4, 0, 4), Blocks.COBBLESTONE.defaultBlockState());
        for (int y = 1; y <= 2; ++y) {
            for (int i = 0; i <= 4; ++i) {
                if (r.nextFloat() < 0.7f) set(l, o.offset(0, y, i), Blocks.COBBLESTONE.defaultBlockState());
                if (r.nextFloat() < 0.7f) set(l, o.offset(4, y, i), Blocks.COBBLESTONE.defaultBlockState());
                if (r.nextFloat() < 0.7f) set(l, o.offset(i, y, 0), Blocks.COBBLESTONE.defaultBlockState());
                if (r.nextFloat() < 0.7f) set(l, o.offset(i, y, 4), Blocks.COBBLESTONE.defaultBlockState());
            }
        }
        set(l, o.offset(1, 2, 1), Blocks.ANVIL.defaultBlockState());
        set(l, o.offset(3, 1, 3), Blocks.ANVIL.defaultBlockState());
        placeChest(l, o.offset(2, 1, 2), r, Type.RUINED_HUT.lootTable(), Direction.SOUTH);
    }

    private static void buildRuinedTower(WorldGenLevel l, BlockPos o, RandomSource r) {
        fill(l, o.offset(0, 0, 0), o.offset(2, 0, 2), Blocks.STONE_BRICK_WALL.defaultBlockState());
        for (int y = 1; y <= 2; ++y) {
            for (int dx = 0; dx <= 2; ++dx) {
                for (int dz = 0; dz <= 2; ++dz) {
                    if (dx != 0 && dx != 2 && dz != 0 && dz != 2) continue;
                    set(l, o.offset(dx, y, dz), Blocks.CRACKED_STONE_BRICKS.defaultBlockState());
                }
            }
        }
        for (int y = 3; y <= 4; ++y) {
            for (int dx = 0; dx <= 2; ++dx) {
                for (int dz = 0; dz <= 2; ++dz) {
                    if ((dx != 0 && dx != 2 && dz != 0 && dz != 2) || !(r.nextFloat() < 0.5f)) continue;
                    set(l, o.offset(dx, y, dz), Blocks.COBBLESTONE.defaultBlockState());
                }
            }
        }
        set(l, o.offset(1, 1, 0), Blocks.AIR.defaultBlockState());
        set(l, o.offset(1, 2, 0), Blocks.AIR.defaultBlockState());
        placeChest(l, o.offset(1, 1, 1), r, Type.RUINED_TOWER.lootTable(), Direction.NORTH);
    }

    private static void buildAncientTomb(WorldGenLevel l, BlockPos o, RandomSource r) {
        for (int y = -3; y <= -1; ++y) {
            for (int dx = 0; dx <= 4; ++dx) {
                for (int dz = 0; dz <= 4; ++dz) {
                    boolean edge = dx == 0 || dx == 4 || dz == 0 || dz == 4 || y == -3;
                    set(l, o.offset(dx, y, dz), edge ? Blocks.STONE_BRICK_WALL.defaultBlockState() : Blocks.AIR.defaultBlockState());
                }
            }
        }
        for (int dx = 0; dx <= 4; ++dx) {
            for (int dz = 0; dz <= 4; ++dz) {
                set(l, o.offset(dx, 0, dz), Blocks.STONE_BRICK_WALL.defaultBlockState());
            }
        }
        set(l, o.offset(2, -2, 2), Blocks.SOUL_LANTERN.defaultBlockState());
        set(l, o.offset(2, -2, 1), Blocks.SOUL_LANTERN.defaultBlockState());
        set(l, o.offset(2, -2, 3), Blocks.SOUL_LANTERN.defaultBlockState());
        set(l, o.offset(1, -1, 1), Blocks.ANVIL.defaultBlockState());
        set(l, o.offset(3, -1, 3), Blocks.ANVIL.defaultBlockState());
        placeChest(l, o.offset(1, -2, 2), r, Type.ANCIENT_TOMB.lootTable(), Direction.EAST);
        set(l, o.offset(2, 0, 0), Blocks.AIR.defaultBlockState());
        set(l, o.offset(2, -1, 0), Blocks.AIR.defaultBlockState());
        set(l, o.offset(2, -1, 1), Blocks.AIR.defaultBlockState());
    }

    private static void buildFallenAltar(WorldGenLevel l, BlockPos o, RandomSource r) {
        for (int dx = 0; dx <= 4; ++dx) {
            for (int dz = 0; dz <= 4; ++dz) {
                if (!(r.nextFloat() < 0.6f)) continue;
                BlockState b = r.nextFloat() < 0.5f ? Blocks.COBBLESTONE.defaultBlockState() : Blocks.OAK_PLANKS.defaultBlockState();
                set(l, o.offset(dx, 0, dz), b);
            }
        }
        set(l, o.offset(2, 0, 2), Blocks.SOUL_LANTERN.defaultBlockState());
        set(l, o.offset(1, 0, 2), Blocks.COBBLESTONE.defaultBlockState());
        set(l, o.offset(3, 0, 2), Blocks.COBBLESTONE.defaultBlockState());
        set(l, o.offset(2, 1, 2), Blocks.STONE.defaultBlockState());
        placeChest(l, o.offset(2, 1, 1), r, Type.FALLEN_ALTAR.lootTable(), Direction.SOUTH);
    }

    private static void buildBurntCamp(WorldGenLevel l, BlockPos o, RandomSource r) {
        for (int dx = 0; dx <= 4; ++dx) {
            for (int dz = 0; dz <= 4; ++dz) {
                if (!(r.nextFloat() < 0.7f)) continue;
                set(l, o.offset(dx, 0, dz), Blocks.COARSE_DIRT.defaultBlockState());
            }
        }
        int[][] corners = {{0, 0}, {4, 0}, {0, 4}, {4, 4}};
        for (int[] c : corners) {
            int h = 1 + r.nextInt(2);
            for (int y = 1; y <= h; ++y) {
                set(l, o.offset(c[0], y, c[1]), Blocks.ACACIA_LOG.defaultBlockState());
            }
        }
        set(l, o.offset(2, 1, 2), Blocks.CAMPFIRE.defaultBlockState().setValue(CampfireBlock.LIT, false));
        placeChest(l, o.offset(1, 1, 2), r, Type.BURNT_CAMP.lootTable(), Direction.EAST);
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
                    l.setBlock(m.set(x, y, z), state, 2);
                }
            }
        }
    }

    private static void placeChest(WorldGenLevel l, BlockPos pos, RandomSource r, ResourceLocation lootTable, Direction facing) {
        BlockState chestState = Blocks.CHEST.defaultBlockState().setValue(HorizontalDirectionalBlock.FACING, facing);
        l.setBlock(pos, chestState, 2);
        BlockEntity be = l.getBlockEntity(pos);
        if (be instanceof RandomizableContainerBlockEntity chest) {
            CultivationChestLoot.fill(chest, r, LOOT_RUINED.equals(lootTable));
        }
    }
}
