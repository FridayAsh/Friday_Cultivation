/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.BlockPos
 *  net.minecraft.core.BlockPos$MutableBlockPos
 *  net.minecraft.core.Vec3i
 *  net.minecraft.nbt.CompoundTag
 *  net.minecraft.nbt.ListTag
 *  net.minecraft.nbt.Tag
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.server.level.ServerLevel
 *  net.minecraft.server.level.ServerPlayer
 *  net.minecraft.util.Mth
 *  net.minecraft.world.level.ChunkPos
 *  net.minecraft.world.level.ServerLevelAccessor
 *  net.minecraft.world.level.block.Blocks
 *  net.minecraft.world.level.block.ChestBlock
 *  net.minecraft.world.level.block.ShulkerBoxBlock
 *  net.minecraft.world.level.block.state.BlockState
 *  net.minecraft.world.level.levelgen.Heightmap$Types
 *  net.minecraft.world.level.levelgen.structure.templatesystem.BlockIgnoreProcessor
 *  net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings
 *  net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessor
 *  net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate
 *  net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager
 *  net.minecraft.world.level.saveddata.SavedData
 */
package com.friday.cultivation.worldgen;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.ShulkerBoxBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.structure.templatesystem.BlockIgnoreProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;
import net.minecraft.world.level.saveddata.SavedData;

public final class NaiheBridgeBuilder {
    private static final ResourceLocation HEAD_ID = new ResourceLocation((String)"friday_cultivation", (String)"naihe_bridge_1");
    private static final ResourceLocation TAIL_ID = new ResourceLocation((String)"friday_cultivation", (String)"naihe_bridge_2");
    private static final int HEAD_LENGTH = 48;
    private static final int TAIL_LENGTH = 43;
    private static final int TOTAL_LENGTH = 91;
    private static final int STRUCTURE_WIDTH = 20;
    private static final int ROAD_TOP_REL_Y = 5;
    private static final int LAVA_RIVER_START_X = 31;
    private static final int LAVA_RIVER_END_X = 74;
    private static final int LAVA_DEPTH = 3;
    private static final int TERRAIN_MARGIN_X = 12;
    private static final int LOCAL_BANK_MARGIN_Z = 20;
    private static final int HEIGHT_SAMPLE_MARGIN_Z = 28;
    private static final int RIVER_CONTINUATION_Z = 80;
    private static final int RIVER_END_BLEND_Z = 20;
    private static final int RIVER_BANK_MARGIN_X = 10;
    private static final int RIVER_BLEND_MARGIN_X = 18;
    private static final int LOCAL_BLEND_START = 4;
    private static final int PLAYER_BRIDGE_SPACING = 192;
    private static final int BRIDGE_GRID_SIZE = 512;
    private static final int VISIT_X_STEP = 37;
    private static final int VISIT_Z_STEP = 53;
    private static final int MAX_ORIGIN_PROBES = 32;
    private static final String VISIT_DATA_NAME = "friday_cultivation_naihe_bridge_visits";
    private static final int HEAD_CHEST_REL_X = 1;
    private static final int HEAD_CHEST_REL_Y = 4;
    private static final int HEAD_CHEST_REL_Z = 8;
    private static final int ARRIVAL_JOB_CHUNKS_PER_TICK = 10;
    private static final int ARRIVAL_JOB_SHAPE_COLUMNS_PER_TICK = 1600;
    private static final int ARRIVAL_JOB_LOOT_SCAN_BLOCKS_PER_TICK = 16384;
    private static final int ARRIVAL_JOB_FINAL_DRAIN_STEPS = 4096;
    private static final long ARRIVAL_JOB_NANOS_PER_TICK = 8000000L;
    private static final int EMERGENCY_LANDING_RADIUS = 2;
    private static final int EMERGENCY_MIN_ROAD_Y = 36;
    private static final int EMERGENCY_MAX_ROAD_Y = 92;
    private static final int EMERGENCY_DEFAULT_ROAD_Y = 64;
    private static final int[] NETHERRACK_TOP_REL_Y = new int[]{5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 4, 4, 3, 3, 2, 2, 1, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 2, 4, 5, 5, 5, 4, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5};

    private NaiheBridgeBuilder() {
    }

    public static BlockPos place(ServerLevel difu, ServerPlayer player) {
        return NaiheBridgeBuilder.place(difu, player.getUUID(), true);
    }

    public static ArrivalPreparation beginArrivalPreparation(ServerLevel difu, ServerPlayer player) {
        return NaiheBridgeBuilder.beginArrivalPreparation(difu, player.getUUID(), true);
    }

    public static BlockPos place(ServerLevel difu, UUID soulId) {
        return NaiheBridgeBuilder.place(difu, soulId, false);
    }

    public static ArrivalPreparation beginArrivalPreparation(ServerLevel difu, UUID soulId, boolean allowFirstLootContainers) {
        return new ArrivalPreparation(difu, soulId, NaiheBridgeBuilder.reserveVisitData(difu, soulId, allowFirstLootContainers));
    }

    private static BlockPos place(ServerLevel difu, UUID soulId, boolean allowFirstLootContainers) {
        BlockPos arrival;
        PlacementPlan plan = NaiheBridgeBuilder.reservePlacement(difu, soulId, allowFirstLootContainers);
        BridgeOrigin origin = plan.origin();
        BlockPos structureOrigin = new BlockPos(origin.x(), origin.roadY() - 5, origin.z());
        if (!plan.needsPlacement()) {
            BlockPos arrival2 = NaiheBridgeBuilder.defaultArrival(structureOrigin);
            NaiheBridgeBuilder.clearPlayerSpace(difu, arrival2);
            return arrival2;
        }
        StructureTemplateManager manager = difu.getStructureManager();
        StructureTemplate head = manager.get(HEAD_ID).orElse(null);
        StructureTemplate tail = manager.get(TAIL_ID).orElse(null);
        if (head == null) {
            return NaiheBridgeBuilder.buildFallbackPlatform(difu, new BlockPos(origin.x(), origin.roadY(), origin.z()));
        }
        Vec3i headSize = head.getSize();
        Vec3i tailSize = tail != null ? tail.getSize() : Vec3i.ZERO;
        int length = headSize.getX() + tailSize.getX();
        int width = Math.max(headSize.getZ(), tailSize.getZ());
        int maxHeight = Math.max(headSize.getY(), tailSize.getY());
        int lavaY = origin.roadY() - 5;
        int clearTop = Math.min(difu.getMaxBuildHeight() - 2, Math.max(origin.clearTop(), lavaY + maxHeight + 6));
        int bottom = Math.max(difu.getMinBuildHeight() + 1, origin.foundationBottom());
        NaiheBridgeBuilder.carveAndShapeRiverValley(difu, structureOrigin, length, width, bottom, clearTop);
        NaiheBridgeBuilder.placeTemplates(difu, head, tail, structureOrigin, headSize);
        if (!plan.keepLootContainers()) {
            NaiheBridgeBuilder.removeLootContainers(difu, structureOrigin, length, width, maxHeight);
        }
        BlockPos blockPos = arrival = plan.keepLootContainers() ? NaiheBridgeBuilder.findChestArrival(difu, structureOrigin, headSize) : NaiheBridgeBuilder.defaultArrival(structureOrigin);
        if (arrival != null) {
            NaiheBridgeBuilder.clearPlayerSpace(difu, arrival);
            return arrival;
        }
        return NaiheBridgeBuilder.buildFallbackChest(difu, structureOrigin, width);
    }

    private static PlacementPlan reservePlacement(ServerLevel difu, UUID soulId, boolean allowFirstLootContainers) {
        PlacementReservation reservation = NaiheBridgeBuilder.reserveVisitData(difu, soulId, allowFirstLootContainers);
        ResolvedOrigin resolved = NaiheBridgeBuilder.resolveOriginSynchronously(difu, soulId, reservation);
        if (!reservation.reuseStableVisit()) {
            reservation.commit(resolved.visitIndex());
        }
        boolean needsPlacement = reservation.reuseStableVisit() ? !resolved.alreadyExists() : true;
        return new PlacementPlan(resolved.origin(), reservation.keepLootContainers(), needsPlacement);
    }

    private static PlacementReservation reserveVisitData(ServerLevel difu, UUID soulId, boolean allowFirstLootContainers) {
        BridgeVisitData data = BridgeVisitData.get(difu);
        BridgeRecord record = data.getRecord(soulId);
        if (record.phase() >= 2) {
            int visitIndex = Math.max(0, record.activeIndex());
            return new PlacementReservation(data, soulId, visitIndex, false, false, true);
        }
        boolean firstPhase = record.phase() <= 0;
        int visitIndex = firstPhase ? Math.max(0, record.nextIndex()) : Math.max(record.nextIndex(), record.activeIndex() + 1);
        return new PlacementReservation(data, soulId, visitIndex, firstPhase, firstPhase && allowFirstLootContainers, false);
    }

    private static ResolvedOrigin resolveOriginSynchronously(ServerLevel difu, UUID soulId, PlacementReservation reservation) {
        int visitIndex = reservation.visitIndex();
        int probes = 0;
        while (true) {
            OriginSeed seed = NaiheBridgeBuilder.originSeed(soulId, visitIndex);
            NaiheBridgeBuilder.forceChunks(difu, seed);
            BridgeOrigin origin = NaiheBridgeBuilder.chooseOriginFromLoadedTerrain(difu, seed);
            boolean exists = NaiheBridgeBuilder.bridgeAlreadyExists(difu, origin);
            if (reservation.reuseStableVisit() || !exists || probes >= 32) {
                return new ResolvedOrigin(visitIndex, origin, exists);
            }
            ++visitIndex;
            ++probes;
        }
    }

    private static OriginSeed originSeed(UUID soulId, int visitIndex) {
        int hash = soulId.hashCode();
        int gridX = Math.floorMod((hash & 0xFF) + visitIndex * 37, 512);
        int gridZ = Math.floorMod((hash >>> 8 & 0xFF) + visitIndex * 53, 512);
        int ox = gridX * 192 + 64;
        int oz = gridZ * 192 + 64;
        return new OriginSeed(ox, oz);
    }

    private static BridgeOrigin chooseOriginFromLoadedTerrain(ServerLevel difu, OriginSeed seed) {
        ArrayList<Integer> heights = new ArrayList<Integer>();
        int terrainMax = difu.getMinBuildHeight();
        int terrainMin = difu.getMaxBuildHeight();
        for (int sx : new int[]{0, 8, 16, 24, 67, 75, 83, 90}) {
            for (int sz : new int[]{-28, 2, 10, 17, 47}) {
                int h = difu.getHeight(Heightmap.Types.WORLD_SURFACE_WG, seed.x() + sx, seed.z() + sz);
                heights.add(h);
                terrainMax = Math.max(terrainMax, h);
                terrainMin = Math.min(terrainMin, h);
            }
        }
        Collections.sort(heights);
        int localRoadY = (Integer)heights.get(heights.size() / 2);
        int lavaY = Mth.clamp((int)(localRoadY - 5), (int)32, (int)92);
        int roadY = lavaY + 5;
        int bottom = Math.min(lavaY - 8, terrainMin - 4);
        int clearTop = Math.max(roadY + 30, terrainMax + 6);
        return new BridgeOrigin(seed.x(), seed.z(), roadY, bottom, clearTop);
    }

    private static boolean bridgeAlreadyExists(ServerLevel difu, BridgeOrigin origin) {
        BlockPos structureOrigin = new BlockPos(origin.x(), origin.roadY() - 5, origin.z());
        int[][] samples = new int[][]{{1, 4, 8}, {8, 5, 8}, {24, 5, 10}, {45, 1, 10}, {83, 5, 10}};
        BlockPos.MutableBlockPos p = new BlockPos.MutableBlockPos();
        for (int[] sample : samples) {
            int worldX = structureOrigin.getX() + sample[0];
            int worldY = structureOrigin.getY() + sample[1];
            int worldZ = structureOrigin.getZ() + sample[2];
            for (int dy = -2; dy <= 2; ++dy) {
                if (!NaiheBridgeBuilder.isBridgeMarkerBlock(difu.getBlockState((BlockPos)p.set(worldX, worldY + dy, worldZ)))) continue;
                return true;
            }
        }
        return false;
    }

    private static boolean isBridgeMarkerBlock(BlockState state) {
        return state.is(Blocks.CHEST) || state.getBlock() instanceof ShulkerBoxBlock || state.is(Blocks.STONE_BRICKS) || state.is(Blocks.STONE_BRICK_SLAB) || state.is(Blocks.STONE_BRICK_STAIRS) || state.is(Blocks.STONE_BRICK_WALL) || state.is(Blocks.NETHER_BRICKS) || state.is(Blocks.NETHER_BRICK_SLAB) || state.is(Blocks.NETHER_BRICK_STAIRS) || state.is(Blocks.NETHER_BRICK_WALL) || state.is(Blocks.RED_NETHER_BRICKS) || state.is(Blocks.CHISELED_NETHER_BRICKS) || state.is(Blocks.SEA_LANTERN) || state.is(Blocks.SOUL_LANTERN);
    }

    private static void forceChunks(ServerLevel difu, OriginSeed seed) {
        for (ChunkPos chunk : NaiheBridgeBuilder.placementChunks(seed)) {
            difu.getChunk(chunk.x, chunk.z);
        }
    }

    private static List<ChunkPos> placementChunks(OriginSeed seed) {
        int minX = seed.x() - 12 - 1;
        int maxX = seed.x() + 91 + 12 + 1;
        int minZ = seed.z() - 80 - 20 - 20 - 1;
        int maxZ = seed.z() + 20 + 80 + 20 + 20 + 1;
        ArrayList<ChunkPos> chunks = new ArrayList<ChunkPos>();
        for (int cx = minX >> 4; cx <= maxX >> 4; ++cx) {
            for (int cz = minZ >> 4; cz <= maxZ >> 4; ++cz) {
                chunks.add(new ChunkPos(cx, cz));
            }
        }
        return chunks;
    }

    private static void carveAndShapeRiverValley(ServerLevel difu, BlockPos origin, int length, int width, int bottom, int clearTop) {
        BlockState netherrack = Blocks.NETHERRACK.defaultBlockState();
        BlockState lava = Blocks.LAVA.defaultBlockState();
        BlockState air = Blocks.AIR.defaultBlockState();
        BlockPos.MutableBlockPos p = new BlockPos.MutableBlockPos();
        int minZ = NaiheBridgeBuilder.shapeMinZ();
        int maxZ = NaiheBridgeBuilder.shapeMaxZ(width);
        for (int x = NaiheBridgeBuilder.shapeMinX(); x <= NaiheBridgeBuilder.shapeMaxX(length); ++x) {
            for (int z = minZ; z <= maxZ; ++z) {
                NaiheBridgeBuilder.shapeRiverValleyColumn(difu, origin, length, width, bottom, clearTop, netherrack, lava, air, p, x, z);
            }
        }
    }

    private static void shapeRiverValleyColumn(ServerLevel difu, BlockPos origin, int length, int width, int bottom, int clearTop, BlockState netherrack, BlockState lava, BlockState air, BlockPos.MutableBlockPos p, int x, int z) {
        if (!NaiheBridgeBuilder.shouldShapeColumn(x, z, length, width)) {
            return;
        }
        int worldX = origin.getX() + x;
        int worldZ = origin.getZ() + z;
        int naturalTopY = Math.max(difu.getMinBuildHeight() + 1, difu.getHeight(Heightmap.Types.WORLD_SURFACE_WG, worldX, worldZ) - 1);
        int columnClearTop = Math.min(difu.getMaxBuildHeight() - 2, Math.max(clearTop, naturalTopY + 4));
        if (NaiheBridgeBuilder.isRiverChannel(x, z, width)) {
            NaiheBridgeBuilder.fill(difu, p, worldX, bottom, origin.getY() - 3, worldZ, netherrack);
            NaiheBridgeBuilder.fill(difu, p, worldX, origin.getY() - 3 + 1, origin.getY(), worldZ, lava);
            NaiheBridgeBuilder.clear(difu, p, worldX, origin.getY() + 1, columnClearTop, worldZ, air);
        } else {
            int desiredTopY = origin.getY() + NaiheBridgeBuilder.terrainTopRelY(x, z, width);
            int topY = NaiheBridgeBuilder.blendedTopY(desiredTopY, naturalTopY, x, z, length, width);
            NaiheBridgeBuilder.fill(difu, p, worldX, bottom, topY, worldZ, netherrack);
            NaiheBridgeBuilder.clear(difu, p, worldX, topY + 1, columnClearTop, worldZ, air);
        }
    }

    private static int shapeMinX() {
        return -12;
    }

    private static int shapeMaxX(int length) {
        return length + 12;
    }

    private static int shapeMinZ() {
        return -120;
    }

    private static int shapeMaxZ(int width) {
        return width + 80 + 20 + 20 - 1;
    }

    private static boolean shouldShapeColumn(int x, int z, int length, int width) {
        if (z >= -20 && z < width + 20) {
            return true;
        }
        int riverOuterMinZ = -100;
        int riverOuterMaxZ = width + 80 + 20 - 1;
        if (z < riverOuterMinZ || z > riverOuterMaxZ) {
            return false;
        }
        int riverSideDistance = NaiheBridgeBuilder.riverSideDistance(x, z, width);
        return riverSideDistance <= 28;
    }

    private static int terrainTopRelY(int x, int z, int width) {
        int xTop;
        int profileX = Mth.clamp((int)x, (int)0, (int)(NETHERRACK_TOP_REL_Y.length - 1));
        int n = xTop = x >= 0 && x < NETHERRACK_TOP_REL_Y.length ? NETHERRACK_TOP_REL_Y[profileX] : 5;
        if (z >= -20 && z < width + 20) {
            if (z >= 0 && z < width) {
                return xTop;
            }
            int bankDistance = z < 0 ? -z : z - width + 1;
            int bankTop = Math.min(5, 1 + (bankDistance + 1) / 2);
            return Math.max(xTop, bankTop);
        }
        int riverSideDistance = NaiheBridgeBuilder.riverSideDistance(x, z, width);
        if (riverSideDistance <= 0) {
            return 0;
        }
        return Mth.clamp((int)(1 + (riverSideDistance - 1) / 2), (int)0, (int)5);
    }

    private static int blendedTopY(int desiredTopY, int naturalTopY, int x, int z, int length, int width) {
        double blend = NaiheBridgeBuilder.naturalBlend(x, z, length, width);
        return (int)Math.round(Mth.lerp((double)blend, (double)desiredTopY, (double)naturalTopY));
    }

    private static double naturalBlend(int x, int z, int length, int width) {
        if (NaiheBridgeBuilder.isRiverChannel(x, z, width)) {
            return 0.0;
        }
        int localDistance = NaiheBridgeBuilder.distanceOutsideRect(x, z, 0, length - 1, 0, width - 1);
        boolean inLocalBand = z >= -20 && z < width + 20;
        double localBlend = inLocalBand ? NaiheBridgeBuilder.smoothStep((double)(localDistance - 4) / (double)(Math.max(12, 20) - 4)) : 1.0;
        int riverSideDistance = NaiheBridgeBuilder.riverSideDistance(x, z, width);
        int riverEndDistance = NaiheBridgeBuilder.distanceOutsideRange(z, -80, width + 80 - 1);
        boolean inRiverCorridor = riverEndDistance <= 20 && riverSideDistance <= 28;
        double riverSideBlend = NaiheBridgeBuilder.smoothStep((double)(riverSideDistance - 10) / 18.0);
        double riverEndBlend = NaiheBridgeBuilder.smoothStep((double)riverEndDistance / 20.0);
        double riverBlend = inRiverCorridor ? Math.max(riverSideBlend, riverEndBlend) : 1.0;
        return Math.min(localBlend, riverBlend);
    }

    private static int distanceOutsideRect(int x, int z, int minX, int maxX, int minZ, int maxZ) {
        int dx = NaiheBridgeBuilder.distanceOutsideRange(x, minX, maxX);
        int dz = NaiheBridgeBuilder.distanceOutsideRange(z, minZ, maxZ);
        return Math.max(dx, dz);
    }

    private static int distanceOutsideRange(int value, int min, int max) {
        if (value < min) {
            return min - value;
        }
        if (value > max) {
            return value - max;
        }
        return 0;
    }

    private static double smoothStep(double value) {
        double t = Mth.clamp((double)value, (double)0.0, (double)1.0);
        return t * t * (3.0 - 2.0 * t);
    }

    private static boolean isRiverChannel(int x, int z, int width) {
        int localX = x - NaiheBridgeBuilder.riverMeanderOffsetX(z, width);
        if (localX < 31 || localX > 74) {
            return false;
        }
        int minZ = NaiheBridgeBuilder.lavaMinZ(localX, width) - 80;
        int maxZ = NaiheBridgeBuilder.lavaMaxZ(localX, width) + 80;
        return z >= minZ && z <= maxZ;
    }

    private static int riverSideDistance(int x, int z, int width) {
        int offset = NaiheBridgeBuilder.riverMeanderOffsetX(z, width);
        return NaiheBridgeBuilder.distanceOutsideRange(x, 31 + offset, 74 + offset);
    }

    private static int riverMeanderOffsetX(int z, int width) {
        int outside = NaiheBridgeBuilder.distanceOutsideRange(z, 0, width - 1);
        if (outside <= 20) {
            return 0;
        }
        double fade = NaiheBridgeBuilder.smoothStep((double)(outside - 20) / (double)Math.max(1, 60));
        double wave = Math.sin((double)z * 0.083) * 3.0 + Math.sin((double)z * 0.037 + 1.7) * 2.0;
        return Mth.clamp((int)((int)Math.round(wave * fade)), (int)-5, (int)5);
    }

    private static int lavaMinZ(int x, int width) {
        if (x == 31) {
            return Math.min(3, width - 1);
        }
        if (x == 32) {
            return Math.min(1, width - 1);
        }
        return 0;
    }

    private static int lavaMaxZ(int x, int width) {
        int full = width - 1;
        if (x == 31) {
            return Math.min(11, full);
        }
        if (x == 32) {
            return Math.min(14, full);
        }
        if (x == 33) {
            return Math.min(15, full);
        }
        if (x == 34 || x == 72) {
            return Math.min(17, full);
        }
        if (x == 73) {
            return Math.min(12, full);
        }
        if (x == 74) {
            return Math.min(8, full);
        }
        return full;
    }

    private static void placeTemplates(ServerLevel difu, StructureTemplate head, StructureTemplate tail, BlockPos origin, Vec3i headSize) {
        StructurePlaceSettings settings = new StructurePlaceSettings().addProcessor((StructureProcessor)BlockIgnoreProcessor.STRUCTURE_AND_AIR);
        head.placeInWorld((ServerLevelAccessor)difu, origin, origin, settings, difu.getRandom(), 2);
        if (tail != null) {
            BlockPos tailOrigin = origin.offset(headSize.getX(), 0, 0);
            tail.placeInWorld((ServerLevelAccessor)difu, tailOrigin, tailOrigin, settings, difu.getRandom(), 2);
        }
    }

    private static BlockPos findChestArrival(ServerLevel difu, BlockPos origin, Vec3i headSize) {
        BlockPos.MutableBlockPos p = new BlockPos.MutableBlockPos();
        for (int x = 0; x < headSize.getX(); ++x) {
            for (int y = 0; y < headSize.getY(); ++y) {
                for (int z = 0; z < headSize.getZ(); ++z) {
                    if (!(difu.getBlockState((BlockPos)p.set(origin.getX() + x, origin.getY() + y, origin.getZ() + z)).getBlock() instanceof ChestBlock)) continue;
                    return new BlockPos(origin.getX() + x, origin.getY() + y + 3, origin.getZ() + z);
                }
            }
        }
        return null;
    }

    private static void removeLootContainers(ServerLevel difu, BlockPos origin, int length, int width, int maxHeight) {
        BlockPos.MutableBlockPos p = new BlockPos.MutableBlockPos();
        BlockState air = Blocks.AIR.defaultBlockState();
        for (int x = 0; x < length; ++x) {
            for (int y = 0; y < maxHeight; ++y) {
                for (int z = 0; z < width; ++z) {
                    p.set(origin.getX() + x, origin.getY() + y, origin.getZ() + z);
                    BlockState state = difu.getBlockState((BlockPos)p);
                    if (!(state.getBlock() instanceof ChestBlock) && !(state.getBlock() instanceof ShulkerBoxBlock)) continue;
                    difu.removeBlockEntity((BlockPos)p);
                    difu.setBlock((BlockPos)p, air, 2);
                }
            }
        }
    }

    private static BlockPos defaultArrival(BlockPos origin) {
        return origin.offset(1, 7, 8);
    }

    private static BlockPos buildFallbackChest(ServerLevel difu, BlockPos origin, int width) {
        BlockPos chest = new BlockPos(origin.getX() + 1, origin.getY() + 5, origin.getZ() + width / 2);
        difu.setBlock(chest.below(), Blocks.NETHERRACK.defaultBlockState(), 2);
        difu.setBlock(chest, Blocks.CHEST.defaultBlockState(), 2);
        BlockPos arrival = chest.above(3);
        NaiheBridgeBuilder.clearPlayerSpace(difu, arrival);
        return arrival;
    }

    private static BlockPos buildFallbackPlatform(ServerLevel difu, BlockPos origin) {
        for (int dx = -2; dx <= 2; ++dx) {
            for (int dz = -2; dz <= 2; ++dz) {
                difu.setBlock(origin.offset(dx, -1, dz), Blocks.SOUL_SOIL.defaultBlockState(), 2);
                for (int dy = 0; dy <= 2; ++dy) {
                    difu.setBlock(origin.offset(dx, dy, dz), Blocks.AIR.defaultBlockState(), 2);
                }
            }
        }
        return origin.above();
    }

    private static void buildEmergencyLanding(ServerLevel difu, BlockPos arrival) {
        BlockState floor = Blocks.SOUL_SOIL.defaultBlockState();
        BlockState air = Blocks.AIR.defaultBlockState();
        BlockPos.MutableBlockPos p = new BlockPos.MutableBlockPos();
        for (int dx = -2; dx <= 2; ++dx) {
            for (int dz = -2; dz <= 2; ++dz) {
                NaiheBridgeBuilder.setBlockIfDifferent(difu, p, arrival.getX() + dx, arrival.getY() - 1, arrival.getZ() + dz, floor);
                for (int dy = 0; dy <= 2; ++dy) {
                    NaiheBridgeBuilder.setBlockIfDifferent(difu, p, arrival.getX() + dx, arrival.getY() + dy, arrival.getZ() + dz, air);
                }
            }
        }
    }

    private static void clearPlayerSpace(ServerLevel difu, BlockPos arrival) {
        BlockState air = Blocks.AIR.defaultBlockState();
        difu.setBlock(arrival, air, 2);
        difu.setBlock(arrival.above(), air, 2);
    }

    private static void fill(ServerLevel difu, BlockPos.MutableBlockPos p, int x, int minY, int maxY, int z, BlockState state) {
        if (maxY < minY) {
            return;
        }
        for (int y = minY; y <= maxY; ++y) {
            NaiheBridgeBuilder.setBlockIfDifferent(difu, p, x, y, z, state);
        }
    }

    private static void clear(ServerLevel difu, BlockPos.MutableBlockPos p, int x, int minY, int maxY, int z, BlockState air) {
        NaiheBridgeBuilder.fill(difu, p, x, minY, maxY, z, air);
    }

    private static void setBlockIfDifferent(ServerLevel difu, BlockPos.MutableBlockPos p, int x, int y, int z, BlockState state) {
        p.set(x, y, z);
        if (!difu.getBlockState((BlockPos)p).equals(state)) {
            difu.setBlock((BlockPos)p, state, 2);
        }
    }

    public static final class ArrivalPreparation {
        private final ServerLevel difu;
        private final UUID soulId;
        private final PlacementReservation reservation;
        private int visitIndex;
        private int probes;
        private Stage stage = Stage.START_ORIGIN;
        private OriginSeed seed;
        private List<ChunkPos> chunks = Collections.emptyList();
        private int chunkIndex;
        private BridgeOrigin origin;
        private PlacementPlan plan;
        private StructureTemplate head;
        private StructureTemplate tail;
        private Vec3i headSize = Vec3i.ZERO;
        private BlockPos structureOrigin;
        private int length;
        private int width;
        private int maxHeight;
        private int bottom;
        private int clearTop;
        private int shapeX;
        private int shapeZ;
        private int lootX;
        private int lootY;
        private int lootZ;
        private BlockPos arrival;
        private boolean committed;

        private ArrivalPreparation(ServerLevel difu, UUID soulId, PlacementReservation reservation) {
            this.difu = difu;
            this.soulId = soulId;
            this.reservation = reservation;
            this.visitIndex = reservation.visitIndex();
        }

        public boolean tick() {
            boolean continueNow;
            if (this.isDone()) {
                return true;
            }
            long deadline = System.nanoTime() + 8000000L;
            int guard = 0;
            while (!this.isDone() && guard++ < 16 && (continueNow = this.advance(deadline))) {
            }
            return this.isDone();
        }

        public boolean isDone() {
            return this.stage == Stage.DONE;
        }

        public BlockPos arrival() {
            return this.arrival;
        }

        public boolean finishNow() {
            int guard = 0;
            while (!this.isDone() && guard++ < 4096) {
                this.advance(Long.MAX_VALUE);
            }
            return this.isDone();
        }

        public BlockPos prepareEmergencyArrival() {
            if (this.arrival != null) {
                return this.arrival;
            }
            OriginSeed emergencySeed = this.seed != null ? this.seed : NaiheBridgeBuilder.originSeed(this.soulId, this.visitIndex);
            int arrivalX = emergencySeed.x() + 1;
            int arrivalZ = emergencySeed.z() + 8;
            this.difu.getChunk(arrivalX >> 4, arrivalZ >> 4);
            int sampledSurface = this.difu.getHeight(Heightmap.Types.WORLD_SURFACE_WG, arrivalX, arrivalZ);
            int roadY = Mth.clamp((int)(sampledSurface <= this.difu.getMinBuildHeight() ? 64 : sampledSurface), (int)36, (int)92);
            BlockPos emergencyOrigin = new BlockPos(emergencySeed.x(), roadY - 5, emergencySeed.z());
            BlockPos emergencyArrival = NaiheBridgeBuilder.defaultArrival(emergencyOrigin);
            NaiheBridgeBuilder.buildEmergencyLanding(this.difu, emergencyArrival);
            return emergencyArrival;
        }

        private boolean advance(long deadline) {
            return switch (this.stage) {
                default -> throw new IncompatibleClassChangeError();
                case START_ORIGIN -> {
                    this.seed = NaiheBridgeBuilder.originSeed(this.soulId, this.visitIndex);
                    this.chunks = NaiheBridgeBuilder.placementChunks(this.seed);
                    this.chunkIndex = 0;
                    this.stage = Stage.LOAD_CHUNKS;
                    yield true;
                }
                case LOAD_CHUNKS -> this.loadChunks(deadline);
                case SAMPLE_ORIGIN -> {
                    this.origin = NaiheBridgeBuilder.chooseOriginFromLoadedTerrain(this.difu, this.seed);
                    this.stage = Stage.CHECK_EXISTS;
                    yield true;
                }
                case CHECK_EXISTS -> this.checkExistingBridge();
                case PREPARE_TEMPLATES -> this.prepareTemplates();
                case SHAPE_TERRAIN -> this.shapeTerrain(deadline);
                case PLACE_HEAD -> this.placeHead();
                case PLACE_TAIL -> this.placeTail();
                case REMOVE_LOOT -> this.removeLoot(deadline);
                case FINALIZE -> {
                    this.finalizeArrival();
                    yield false;
                }
                case DONE -> false;
            };
        }

        private boolean loadChunks(long deadline) {
            for (int loaded = 0; this.chunkIndex < this.chunks.size() && loaded < 10 && (loaded == 0 || System.nanoTime() < deadline); ++loaded) {
                ChunkPos chunk = this.chunks.get(this.chunkIndex++);
                this.difu.getChunk(chunk.x, chunk.z);
            }
            if (this.chunkIndex >= this.chunks.size()) {
                this.stage = Stage.SAMPLE_ORIGIN;
                return true;
            }
            return false;
        }

        private boolean checkExistingBridge() {
            boolean exists = NaiheBridgeBuilder.bridgeAlreadyExists(this.difu, this.origin);
            if (this.reservation.reuseStableVisit()) {
                this.plan = new PlacementPlan(this.origin, false, !exists);
                this.stage = Stage.PREPARE_TEMPLATES;
                return true;
            }
            if (exists && this.probes < 32) {
                ++this.visitIndex;
                ++this.probes;
                this.stage = Stage.START_ORIGIN;
                return true;
            }
            this.plan = new PlacementPlan(this.origin, this.reservation.keepLootContainers(), true);
            this.stage = Stage.PREPARE_TEMPLATES;
            return true;
        }

        private boolean prepareTemplates() {
            this.structureOrigin = new BlockPos(this.origin.x(), this.origin.roadY() - 5, this.origin.z());
            if (!this.plan.needsPlacement()) {
                this.arrival = NaiheBridgeBuilder.defaultArrival(this.structureOrigin);
                NaiheBridgeBuilder.clearPlayerSpace(this.difu, this.arrival);
                this.stage = Stage.DONE;
                return false;
            }
            StructureTemplateManager manager = this.difu.getStructureManager();
            this.head = manager.get(HEAD_ID).orElse(null);
            this.tail = manager.get(TAIL_ID).orElse(null);
            if (this.head == null) {
                this.commitReservationIfNeeded();
                this.arrival = NaiheBridgeBuilder.buildFallbackPlatform(this.difu, new BlockPos(this.origin.x(), this.origin.roadY(), this.origin.z()));
                this.stage = Stage.DONE;
                return false;
            }
            this.headSize = this.head.getSize();
            Vec3i tailSize = this.tail != null ? this.tail.getSize() : Vec3i.ZERO;
            this.length = this.headSize.getX() + tailSize.getX();
            this.width = Math.max(this.headSize.getZ(), tailSize.getZ());
            this.maxHeight = Math.max(this.headSize.getY(), tailSize.getY());
            int lavaY = this.origin.roadY() - 5;
            this.clearTop = Math.min(this.difu.getMaxBuildHeight() - 2, Math.max(this.origin.clearTop(), lavaY + this.maxHeight + 6));
            this.bottom = Math.max(this.difu.getMinBuildHeight() + 1, this.origin.foundationBottom());
            this.shapeX = NaiheBridgeBuilder.shapeMinX();
            this.shapeZ = NaiheBridgeBuilder.shapeMinZ();
            this.stage = Stage.SHAPE_TERRAIN;
            return true;
        }

        private boolean shapeTerrain(long deadline) {
            BlockState netherrack = Blocks.NETHERRACK.defaultBlockState();
            BlockState lava = Blocks.LAVA.defaultBlockState();
            BlockState air = Blocks.AIR.defaultBlockState();
            BlockPos.MutableBlockPos p = new BlockPos.MutableBlockPos();
            int maxX = NaiheBridgeBuilder.shapeMaxX(this.length);
            int minZ = NaiheBridgeBuilder.shapeMinZ();
            int maxZ = NaiheBridgeBuilder.shapeMaxZ(this.width);
            int processed = 0;
            while (this.shapeX <= maxX) {
                while (this.shapeZ <= maxZ) {
                    boolean shouldShape = NaiheBridgeBuilder.shouldShapeColumn(this.shapeX, this.shapeZ, this.length, this.width);
                    if (shouldShape) {
                        NaiheBridgeBuilder.shapeRiverValleyColumn(this.difu, this.structureOrigin, this.length, this.width, this.bottom, this.clearTop, netherrack, lava, air, p, this.shapeX, this.shapeZ);
                        ++processed;
                    }
                    ++this.shapeZ;
                    if ((processed <= 0 || processed < 1600) && System.nanoTime() < deadline) continue;
                    return false;
                }
                ++this.shapeX;
                this.shapeZ = minZ;
            }
            this.commitReservationIfNeeded();
            this.stage = Stage.PLACE_HEAD;
            return true;
        }

        private void commitReservationIfNeeded() {
            if (this.committed || this.reservation.reuseStableVisit()) {
                return;
            }
            this.reservation.commit(this.visitIndex);
            this.committed = true;
        }

        private boolean placeHead() {
            StructurePlaceSettings settings = new StructurePlaceSettings().addProcessor((StructureProcessor)BlockIgnoreProcessor.STRUCTURE_AND_AIR);
            this.head.placeInWorld((ServerLevelAccessor)this.difu, this.structureOrigin, this.structureOrigin, settings, this.difu.getRandom(), 2);
            this.stage = Stage.PLACE_TAIL;
            return false;
        }

        private boolean placeTail() {
            if (this.tail != null) {
                StructurePlaceSettings settings = new StructurePlaceSettings().addProcessor((StructureProcessor)BlockIgnoreProcessor.STRUCTURE_AND_AIR);
                BlockPos tailOrigin = this.structureOrigin.offset(this.headSize.getX(), 0, 0);
                this.tail.placeInWorld((ServerLevelAccessor)this.difu, tailOrigin, tailOrigin, settings, this.difu.getRandom(), 2);
            }
            this.stage = this.plan.keepLootContainers() ? Stage.FINALIZE : Stage.REMOVE_LOOT;
            return false;
        }

        private boolean removeLoot(long deadline) {
            BlockPos.MutableBlockPos p = new BlockPos.MutableBlockPos();
            BlockState air = Blocks.AIR.defaultBlockState();
            int scanned = 0;
            while (this.lootX < this.length) {
                while (this.lootY < this.maxHeight) {
                    while (this.lootZ < this.width) {
                        p.set(this.structureOrigin.getX() + this.lootX, this.structureOrigin.getY() + this.lootY, this.structureOrigin.getZ() + this.lootZ);
                        BlockState state = this.difu.getBlockState((BlockPos)p);
                        if (state.getBlock() instanceof ChestBlock || state.getBlock() instanceof ShulkerBoxBlock) {
                            this.difu.removeBlockEntity((BlockPos)p);
                            this.difu.setBlock((BlockPos)p, air, 2);
                        }
                        ++this.lootZ;
                        if (++scanned < 16384 && System.nanoTime() < deadline) continue;
                        return false;
                    }
                    this.lootZ = 0;
                    ++this.lootY;
                }
                this.lootY = 0;
                ++this.lootX;
            }
            this.stage = Stage.FINALIZE;
            return true;
        }

        private void finalizeArrival() {
            BlockPos selected;
            BlockPos blockPos = selected = this.plan.keepLootContainers() ? NaiheBridgeBuilder.findChestArrival(this.difu, this.structureOrigin, this.headSize) : NaiheBridgeBuilder.defaultArrival(this.structureOrigin);
            if (selected != null) {
                NaiheBridgeBuilder.clearPlayerSpace(this.difu, selected);
                this.arrival = selected;
            } else {
                this.arrival = NaiheBridgeBuilder.buildFallbackChest(this.difu, this.structureOrigin, this.width);
            }
            this.stage = Stage.DONE;
        }

        private static enum Stage {
            START_ORIGIN,
            LOAD_CHUNKS,
            SAMPLE_ORIGIN,
            CHECK_EXISTS,
            PREPARE_TEMPLATES,
            SHAPE_TERRAIN,
            PLACE_HEAD,
            PLACE_TAIL,
            REMOVE_LOOT,
            FINALIZE,
            DONE;

        }
    }

    private record PlacementReservation(BridgeVisitData data, UUID soulId, int visitIndex, boolean firstPhase, boolean keepLootContainers, boolean reuseStableVisit) {
        private void commit(int finalVisitIndex) {
            if (this.reuseStableVisit) {
                return;
            }
            if (this.firstPhase) {
                this.data.recordFirstVisit(this.soulId, finalVisitIndex);
            } else {
                this.data.recordStableVisit(this.soulId, finalVisitIndex);
            }
        }
    }

    private record PlacementPlan(BridgeOrigin origin, boolean keepLootContainers, boolean needsPlacement) {
    }

    private record BridgeOrigin(int x, int z, int roadY, int foundationBottom, int clearTop) {
    }

    private record ResolvedOrigin(int visitIndex, BridgeOrigin origin, boolean alreadyExists) {
    }

    private static final class BridgeVisitData
    extends SavedData {
        private final Map<UUID, BridgeRecord> visits = new HashMap<UUID, BridgeRecord>();

        private BridgeVisitData() {
        }

        private static BridgeVisitData get(ServerLevel level) {
            return (BridgeVisitData)level.getDataStorage().computeIfAbsent(BridgeVisitData::load, BridgeVisitData::new, NaiheBridgeBuilder.VISIT_DATA_NAME);
        }

        private static BridgeVisitData load(CompoundTag tag) {
            BridgeVisitData data = new BridgeVisitData();
            ListTag list = tag.getList("visits", 10);
            for (int i = 0; i < list.size(); ++i) {
                CompoundTag row = list.getCompound(i);
                if (!row.contains("id")) continue;
                BridgeRecord legacy = BridgeRecord.fromLegacyCount(row.getInt("count"));
                int phase = row.contains("phase", 3) ? row.getInt("phase") : legacy.phase();
                int nextIndex = row.contains("nextIndex", 3) ? row.getInt("nextIndex") : legacy.nextIndex();
                int activeIndex = row.contains("activeIndex", 3) ? row.getInt("activeIndex") : legacy.activeIndex();
                data.visits.put(UUID.fromString(row.getString("id")), new BridgeRecord(phase, nextIndex, activeIndex));
            }
            return data;
        }

        public CompoundTag save(CompoundTag tag) {
            ListTag list = new ListTag();
            for (Map.Entry<UUID, BridgeRecord> entry : this.visits.entrySet()) {
                BridgeRecord record = entry.getValue();
                CompoundTag row = new CompoundTag();
                row.putUUID("id", entry.getKey());
                row.putInt("count", Math.max(0, record.nextIndex()));
                row.putInt("phase", record.phase());
                row.putInt("nextIndex", record.nextIndex());
                row.putInt("activeIndex", record.activeIndex());
                list.add(row);
            }
            tag.put("visits", (Tag)list);
            return tag;
        }

        private BridgeRecord getRecord(UUID id) {
            return this.visits.getOrDefault(id, BridgeRecord.empty());
        }

        private void recordFirstVisit(UUID id, int visitIndex) {
            this.visits.put(id, new BridgeRecord(1, visitIndex + 1, visitIndex));
            this.setDirty();
        }

        private void recordStableVisit(UUID id, int visitIndex) {
            this.visits.put(id, new BridgeRecord(2, visitIndex + 1, visitIndex));
            this.setDirty();
        }
    }

    private record BridgeRecord(int phase, int nextIndex, int activeIndex) {
        private BridgeRecord {
            phase = Math.max(0, Math.min(2, phase));
            nextIndex = Math.max(0, nextIndex);
            activeIndex = Math.max(-1, activeIndex);
        }

        private static BridgeRecord empty() {
            return new BridgeRecord(0, 0, -1);
        }

        private static BridgeRecord fromLegacyCount(int count) {
            int safeCount = Math.max(0, count);
            if (safeCount <= 0) {
                return BridgeRecord.empty();
            }
            int phase = safeCount == 1 ? 1 : 2;
            return new BridgeRecord(phase, safeCount, safeCount - 1);
        }
    }

    private record OriginSeed(int x, int z) {
    }
}

