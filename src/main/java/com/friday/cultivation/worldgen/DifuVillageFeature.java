/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.serialization.Codec
 *  net.minecraft.core.BlockPos
 *  net.minecraft.core.BlockPos$MutableBlockPos
 *  net.minecraft.util.RandomSource
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.EntityType
 *  net.minecraft.world.entity.MobSpawnType
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.level.ServerLevelAccessor
 *  net.minecraft.world.level.WorldGenLevel
 *  net.minecraft.world.level.block.Blocks
 *  net.minecraft.world.level.block.state.BlockState
 *  net.minecraft.world.level.levelgen.Heightmap$Types
 *  net.minecraft.world.level.levelgen.feature.Feature
 *  net.minecraft.world.level.levelgen.feature.FeaturePlaceContext
 *  net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration
 *  org.jetbrains.annotations.NotNull
 */
package com.friday.cultivation.worldgen;

import com.mojang.serialization.Codec;
import com.friday.cultivation.entity.npc.WanderingCultivatorEntity;
import com.friday.cultivation.registry.ModEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import org.jetbrains.annotations.NotNull;

public class DifuVillageFeature
extends Feature<NoneFeatureConfiguration> {
    private static final BlockState BRICK = Blocks.NETHER_BRICKS.defaultBlockState();
    private static final BlockState RED_BRICK = Blocks.RED_NETHER_BRICKS.defaultBlockState();
    private static final BlockState CHISELED = Blocks.CHISELED_NETHER_BRICKS.defaultBlockState();
    private static final BlockState CRACKED = Blocks.CRACKED_NETHER_BRICKS.defaultBlockState();
    private static final BlockState SLAB = Blocks.NETHER_BRICK_SLAB.defaultBlockState();
    private static final BlockState FENCE = Blocks.NETHER_BRICK_FENCE.defaultBlockState();
    private static final BlockState SOUL_TORCH = Blocks.SOUL_TORCH.defaultBlockState();
    private static final BlockState ROAD = Blocks.CRIMSON_NYLIUM.defaultBlockState();

    public DifuVillageFeature(Codec<NoneFeatureConfiguration> codec) {
        super(codec);
    }

    public boolean place(@NotNull FeaturePlaceContext<NoneFeatureConfiguration> ctx) {
        int s;
        WorldGenLevel level = ctx.level();
        RandomSource rand = ctx.random();
        BlockPos o = ctx.origin();
        int cx = o.getX();
        int cz = o.getZ();
        int[][] spots = new int[][]{{-9, -2}, {7, -6}, {6, 5}, {-8, 6}, {1, -10}, {-3, 9}};
        BlockPos.MutableBlockPos p = new BlockPos.MutableBlockPos();
        int built = 0;
        int[][] doorPts = new int[spots.length][2];
        for (s = 0; s < spots.length; ++s) {
            int bz;
            int bx;
            int gy;
            if (rand.nextInt(4) == 0 || (gy = level.getHeight(Heightmap.Types.WORLD_SURFACE_WG, bx = cx + spots[s][0], bz = cz + spots[s][1])) <= level.getMinBuildHeight() + 4 || gy >= level.getMaxBuildHeight() - 14) continue;
            int variant = rand.nextInt(4);
            int[] door = this.buildVariant(level, rand, p, bx, gy, bz, variant);
            doorPts[s] = new int[]{bx + door[0], bz + door[1]};
            ++built;
        }
        if (built == 0) {
            return false;
        }
        for (s = 0; s < spots.length; ++s) {
            if (doorPts[s] == null || doorPts[s].length != 2 || doorPts[s][0] == 0 && doorPts[s][1] == 0) continue;
            this.layPath(level, p, cx, cz, doorPts[s][0], doorPts[s][1]);
        }
        int ccy = level.getHeight(Heightmap.Types.WORLD_SURFACE_WG, cx, cz);
        this.lampPost(level, p, cx, ccy, cz);
        int n = 4 + rand.nextInt(3);
        for (int i = 0; i < n; ++i) {
            int sx = cx + rand.nextInt(15) - 7;
            int sz = cz + rand.nextInt(15) - 7;
            int sy = level.getHeight(Heightmap.Types.WORLD_SURFACE_WG, sx, sz);
            this.spawnReaper(level, sx, sy, sz);
        }
        return true;
    }

    private int[] buildVariant(WorldGenLevel l, RandomSource rand, BlockPos.MutableBlockPos p, int x0, int y0, int z0, int variant) {
        switch (variant) {
            case 1: {
                this.house(l, p, x0, y0, z0, 5, 5, 3, SLAB);
                return new int[]{2, 0};
            }
            case 2: {
                this.tower(l, p, x0, y0, z0);
                return new int[]{1, 0};
            }
            case 3: {
                this.house(l, p, x0, y0, z0, 6, 4, 3, RED_BRICK);
                return new int[]{3, 0};
            }
        }
        this.house(l, p, x0, y0, z0, 4, 4, 3, RED_BRICK);
        return new int[]{2, 0};
    }

    private void house(WorldGenLevel l, BlockPos.MutableBlockPos p, int x0, int y0, int z0, int w, int d, int h, BlockState roof) {
        int x1 = x0 + w - 1;
        int z1 = z0 + d - 1;
        for (int x = x0; x <= x1; ++x) {
            for (int z = z0; z <= z1; ++z) {
                boolean corner;
                int g = l.getHeight(Heightmap.Types.WORLD_SURFACE_WG, x, z);
                for (int fy = Math.min(g, y0) - 1; fy <= y0; ++fy) {
                    DifuVillageFeature.set(l, (BlockPos)p.set(x, fy, z), BRICK);
                }
                for (int dy = 1; dy <= h + 2; ++dy) {
                    DifuVillageFeature.clear(l, (BlockPos)p.set(x, y0 + dy, z));
                }
                boolean edge = x == x0 || x == x1 || z == z0 || z == z1;
                boolean bl = corner = !(x != x0 && x != x1 || z != z0 && z != z1);
                if (edge) {
                    for (int dy = 1; dy <= h; ++dy) {
                        DifuVillageFeature.set(l, (BlockPos)p.set(x, y0 + dy, z), corner ? CHISELED : BRICK);
                    }
                }
                DifuVillageFeature.set(l, (BlockPos)p.set(x, y0 + h + 1, z), roof);
            }
        }
        int doorX = x0 + w / 2;
        DifuVillageFeature.clear(l, (BlockPos)p.set(doorX, y0 + 1, z0));
        DifuVillageFeature.clear(l, (BlockPos)p.set(doorX, y0 + 2, z0));
        DifuVillageFeature.clear(l, (BlockPos)p.set(x1, y0 + 2, z0 + d / 2));
        DifuVillageFeature.clear(l, (BlockPos)p.set(x0, y0 + 2, z0 + d / 2));
        DifuVillageFeature.set(l, (BlockPos)p.set(x0 + 1, y0 + 1, z0 + 1), SOUL_TORCH);
        DifuVillageFeature.set(l, (BlockPos)p.set(x1 - 1, y0 + 1, z1 - 1), SOUL_TORCH);
        this.lampPost(l, p, doorX, y0, z0 - 1);
    }

    private void tower(WorldGenLevel l, BlockPos.MutableBlockPos p, int x0, int y0, int z0) {
        int w = 4;
        int h = 6;
        int x1 = x0 + w - 1;
        int z1 = z0 + w - 1;
        for (int x = x0; x <= x1; ++x) {
            for (int z = z0; z <= z1; ++z) {
                boolean corner;
                int g = l.getHeight(Heightmap.Types.WORLD_SURFACE_WG, x, z);
                for (int fy = Math.min(g, y0) - 1; fy <= y0; ++fy) {
                    DifuVillageFeature.set(l, (BlockPos)p.set(x, fy, z), BRICK);
                }
                for (int dy = 1; dy <= h + 2; ++dy) {
                    DifuVillageFeature.clear(l, (BlockPos)p.set(x, y0 + dy, z));
                }
                boolean edge = x == x0 || x == x1 || z == z0 || z == z1;
                boolean bl = corner = !(x != x0 && x != x1 || z != z0 && z != z1);
                if (edge) {
                    for (int dy = 1; dy <= h; ++dy) {
                        DifuVillageFeature.set(l, (BlockPos)p.set(x, y0 + dy, z), corner ? CRACKED : BRICK);
                    }
                    DifuVillageFeature.set(l, (BlockPos)p.set(x, y0 + h + 1, z), FENCE);
                }
                DifuVillageFeature.set(l, (BlockPos)p.set(x, y0 + h, z), x == x0 + 1 || x == x0 + 2 ? SLAB : BRICK);
            }
        }
        DifuVillageFeature.clear(l, (BlockPos)p.set(x0 + 1, y0 + 1, z0));
        DifuVillageFeature.clear(l, (BlockPos)p.set(x0 + 1, y0 + 2, z0));
        DifuVillageFeature.set(l, (BlockPos)p.set(x0 + 1, y0 + h, z0 + 1), SOUL_TORCH);
        DifuVillageFeature.set(l, (BlockPos)p.set(x0 + 1, y0 + 1, z0 + 1), SOUL_TORCH);
        this.lampPost(l, p, x0 + 1, y0, z0 - 1);
    }

    private void lampPost(WorldGenLevel l, BlockPos.MutableBlockPos p, int x, int y, int z) {
        DifuVillageFeature.set(l, (BlockPos)p.set(x, y, z), BRICK);
        DifuVillageFeature.set(l, (BlockPos)p.set(x, y + 1, z), FENCE);
        DifuVillageFeature.set(l, (BlockPos)p.set(x, y + 2, z), FENCE);
        DifuVillageFeature.set(l, (BlockPos)p.set(x, y + 3, z), SOUL_TORCH);
    }

    private void layPath(WorldGenLevel l, BlockPos.MutableBlockPos p, int x1, int z1, int x2, int z2) {
        int steps = Math.max(Math.abs(x2 - x1), Math.abs(z2 - z1));
        if (steps == 0) {
            return;
        }
        for (int i = 0; i <= steps; ++i) {
            double t = (double)i / (double)steps;
            int px = (int)Math.round((double)x1 + (double)(x2 - x1) * t);
            int pz = (int)Math.round((double)z1 + (double)(z2 - z1) * t);
            this.paveAt(l, p, px, pz);
            this.paveAt(l, p, px + 1, pz);
        }
    }

    private void paveAt(WorldGenLevel l, BlockPos.MutableBlockPos p, int x, int z) {
        int top = l.getHeight(Heightmap.Types.WORLD_SURFACE_WG, x, z) - 1;
        BlockState g = l.getBlockState((BlockPos)p.set(x, top, z));
        if (g.is(Blocks.NETHERRACK) || g.is(Blocks.SOUL_SAND) || g.is(Blocks.SOUL_SOIL) || g.is(Blocks.NETHER_BRICKS)) {
            DifuVillageFeature.set(l, (BlockPos)p.set(x, top, z), ROAD);
            DifuVillageFeature.clear(l, (BlockPos)p.set(x, top + 1, z));
        }
    }

    private void spawnReaper(WorldGenLevel level, int x, int y, int z) {
        WanderingCultivatorEntity reaper = (WanderingCultivatorEntity)((EntityType)ModEntities.WANDERING_CULTIVATOR.get()).create((Level)level.getLevel());
        if (reaper == null) {
            return;
        }
        reaper.moveTo((double)x + 0.5, y, (double)z + 0.5, level.getRandom().nextFloat() * 360.0f, 0.0f);
        reaper.finalizeSpawn((ServerLevelAccessor)level, level.getCurrentDifficultyAt(reaper.blockPosition()), MobSpawnType.STRUCTURE, null, null);
        reaper.setPersistenceRequired();
        level.addFreshEntity((Entity)reaper);
    }

    private static void set(WorldGenLevel l, BlockPos pos, BlockState s) {
        l.setBlock(pos, s, 2);
    }

    private static void clear(WorldGenLevel l, BlockPos pos) {
        l.setBlock(pos, Blocks.AIR.defaultBlockState(), 2);
    }
}

