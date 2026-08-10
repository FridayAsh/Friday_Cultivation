/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.logging.LogUtils
 *  com.mojang.serialization.Codec
 *  net.minecraft.core.BlockPos
 *  net.minecraft.core.BlockPos$MutableBlockPos
 *  net.minecraft.core.Direction
 *  net.minecraft.core.Direction$Axis
 *  net.minecraft.core.Holder
 *  net.minecraft.core.QuartPos
 *  net.minecraft.core.Vec3i
 *  net.minecraft.nbt.CompoundTag
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.server.level.ServerLevel
 *  net.minecraft.tags.BiomeTags
 *  net.minecraft.tags.BlockTags
 *  net.minecraft.util.Mth
 *  net.minecraft.util.RandomSource
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.EntityType
 *  net.minecraft.world.entity.MobSpawnType
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.level.LevelAccessor
 *  net.minecraft.world.level.LevelHeightAccessor
 *  net.minecraft.world.level.ServerLevelAccessor
 *  net.minecraft.world.level.WorldGenLevel
 *  net.minecraft.world.level.biome.Biome
 *  net.minecraft.world.level.block.BedBlock
 *  net.minecraft.world.level.block.Block
 *  net.minecraft.world.level.block.Blocks
 *  net.minecraft.world.level.block.LeavesBlock
 *  net.minecraft.world.level.block.Rotation
 *  net.minecraft.world.level.block.SlabBlock
 *  net.minecraft.world.level.block.StairBlock
 *  net.minecraft.world.level.block.entity.BlockEntity
 *  net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity
 *  net.minecraft.world.level.block.state.BlockState
 *  net.minecraft.world.level.block.state.properties.BedPart
 *  net.minecraft.world.level.block.state.properties.Property
 *  net.minecraft.world.level.block.state.properties.SlabType
 *  net.minecraft.world.level.block.state.properties.StairsShape
 *  net.minecraft.world.level.levelgen.Heightmap$Types
 *  net.minecraft.world.level.levelgen.feature.Feature
 *  net.minecraft.world.level.levelgen.feature.FeaturePlaceContext
 *  net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration
 *  net.minecraft.world.level.levelgen.structure.BoundingBox
 *  net.minecraft.world.level.levelgen.structure.templatesystem.BlockIgnoreProcessor
 *  net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings
 *  net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessor
 *  net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate
 *  net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager
 *  net.minecraftforge.event.TickEvent$Phase
 *  net.minecraftforge.event.TickEvent$ServerTickEvent
 *  net.minecraftforge.event.server.ServerStoppingEvent
 *  net.minecraftforge.eventbus.api.SubscribeEvent
 *  net.minecraftforge.fml.common.Mod$EventBusSubscriber
 *  org.jetbrains.annotations.NotNull
 *  org.jetbrains.annotations.Nullable
 *  org.slf4j.Logger
 */
package com.friday.cultivation.worldgen;

import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.friday.cultivation.block.CushionBlock;
import com.friday.cultivation.block.formation.FormationCorePlateBlock;
import com.friday.cultivation.block.formation.FormationCorePlateBlockEntity;
import com.friday.cultivation.block.spirit.SpiritVeinCoreBlockEntity;
import com.friday.cultivation.config.ModCommonConfig;
import com.friday.cultivation.cultivation.realm.Realm;
import com.friday.cultivation.cultivation.sect.SectRole;
import com.friday.cultivation.cultivation.sect.SectSavedData;
import com.friday.cultivation.entity.npc.WanderingCultivatorEntity;
import com.friday.cultivation.registry.ModBlocks;
import com.friday.cultivation.registry.ModEntities;
import com.friday.cultivation.worldgen.CultivationChestLoot;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.QuartPos;
import net.minecraft.core.Vec3i;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BiomeTags;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BedPart;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.block.state.properties.SlabType;
import net.minecraft.world.level.block.state.properties.StairsShape;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.templatesystem.BlockIgnoreProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.server.ServerStoppingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

public class SectSettlementFeature
extends Feature<NoneFeatureConfiguration> {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final int CELL_SIZE_CHUNKS = 32;
    private static final int CELL_MARGIN_CHUNKS = 13;
    private static final int PLAN_CHUNK_RADIUS = 13;
    private static final int BUILDING_CLEAR_EXTRA = 5;
    private static final int ROAD_CLEAR_HEIGHT = 5;
    private static final int FOUNDATION_EDGE_MARGIN = 2;
    private static final int BUILDING_BASE_DEPTH = 6;
    private static final int BUILDING_WATER_SUPPORT_DEPTH = 14;
    private static final int ROAD_BASE_DEPTH = 3;
    private static final int ROAD_WATER_SUPPORT_DEPTH = 10;
    private static final int SMALL_FOUNDATION_BASE_DEPTH = 3;
    private static final int SMALL_FOUNDATION_WATER_SUPPORT_DEPTH = 8;
    private static final int MIN_STREETLIGHTS = 5;
    private static final int PIECE_PADDING = 3;
    private static final int STREETLIGHT_ROAD_OFFSET = 4;
    private static final int STREETLIGHT_MIN_SPACING = 40;
    private static final int BUILDING_VEGETATION_CLEAR_RADIUS = 10;
    private static final int BUILDING_VEGETATION_CLEAR_EXTRA_ABOVE = 32;
    private static final int ROAD_VEGETATION_CLEAR_RADIUS = 6;
    private static final int ROAD_VEGETATION_CLEAR_HEIGHT = 32;
    private static final int STREETLIGHT_VEGETATION_CLEAR_RADIUS = 5;
    private static final int STREETLIGHT_VEGETATION_CLEAR_HEIGHT = 24;
    private static final int TREE_CLUSTER_CLEAR_RADIUS = 28;
    private static final int TREE_CLUSTER_VERTICAL_RADIUS = 72;
    private static final int TREE_CLUSTER_MAX_BLOCKS = 4096;
    private static final int VEGETATION_SCAN_AIR_GRACE = 16;
    private static final int SECT_GROUNDS_CLEAR_MARGIN = 8;
    private static final int SECT_GROUNDS_CLEAR_EXTRA_ABOVE = 48;
    private static final int SECT_TREE_EDGE_PADDING = 14;
    private static final int SECT_TREE_MIN_COUNT = 6;
    private static final int SECT_TREE_MAX_COUNT = 14;
    private static final int SECT_TREE_ATTEMPT_MULTIPLIER = 18;
    private static final int SECT_TREE_FOOTPRINT_RADIUS = 5;
    private static final int SECT_TREE_ROAD_CLEAR_RADIUS = 9;
    private static final int SECT_TREE_STREETLIGHT_CLEAR_RADIUS = 9;
    private static final int SECT_TREE_MIN_SPACING = 24;
    private static final int SECT_TREE_CANOPY_RADIUS = 2;
    private static final int ROAD_ANCHOR_FORCE_FLAT_RADIUS = 3;
    private static final int ROAD_ANCHOR_FLAT_RADIUS = 6;
    private static final int ROAD_ANCHOR_FLAT_HEIGHT_TOLERANCE = 1;
    private static final int ROAD_ANCHOR_APPROACH_RADIUS = 8;
    private static final int ROAD_HEIGHT_SMOOTH_PASSES = 4;
    private static final int ROAD_HEIGHT_SMOOTHABLE_DELTA = 2;
    private static final int PLAN_MIN_LOCAL = -112;
    private static final int PLAN_MAX_LOCAL = 112;
    private static final int GATE_SUPPORT_X = 4;
    private static final int[] GATE_SUPPORT_ZS = new int[]{4, 10, 22, 28};
    private static final int[][] GATE_SUPPORT_OFFSETS = new int[][]{{0, 0}, {-1, 0}, {1, 0}, {0, -1}, {0, 1}};
    private static final int GENERATED_ARRAY_FLAG_COUNT = SectSavedData.ArrayReplacement.flagCount();
    private static final int GENERATED_ARRAY_RADIUS_BUFFER = 28;
    private static final int LOW_TREE_SITE_SAMPLE_Y = 64;
    private static final int LOW_TREE_SITE_MIN_RING_RADIUS = 48;
    private static final int LOW_TREE_SITE_MAX_RING_RADIUS = 112;
    private static final int LOW_TREE_SITE_RING_SAMPLE_COUNT = 8;
    private static final int LOW_TREE_SITE_MAX_RING_REJECTS = 2;
    private static final int RECEPTION_GUARD_COUNT = 2;
    private static final int RECEPTION_GUARD_DOME_MARGIN = 10;
    private static final int RECEPTION_GUARD_SIDE_SPACING = 3;
    private static final int RECEPTION_GUARD_GATE_FRONT_MIN = 8;
    private static final int RECEPTION_GUARD_FRONT_SCAN_LIMIT = 256;
    private static final int ANCESTOR_CAVE_MIN_GAP = 1;
    private static final int ANCESTOR_CAVE_MAX_GAP = 2;
    private static final int DEFERRED_NPC_SPAWNS_PER_TICK = 2;
    private static final int DEFERRED_NPC_SPAWN_SCAN_PER_TICK = 24;
    private static final int DEFERRED_NPC_SPAWN_MAX_ATTEMPTS = 2400;
    private static final BlockState ROAD_STAIR = Blocks.STONE_BRICK_STAIRS.defaultBlockState();
    private static final BlockState ROAD_SLAB = (BlockState)Blocks.STONE_BRICK_SLAB.defaultBlockState().setValue((Property)SlabBlock.TYPE, (Comparable)SlabType.BOTTOM);
    private static final BlockState PAVING_STONE = Blocks.STONE.defaultBlockState();
    private static final BlockState PAVING_ANDESITE = Blocks.ANDESITE.defaultBlockState();
    private static final BlockState PAVING_MOSSY_STONE_BRICKS = Blocks.MOSSY_STONE_BRICKS.defaultBlockState();
    private static final BlockState PAVING_STONE_BRICKS = Blocks.STONE_BRICKS.defaultBlockState();
    private static final BlockState FOUNDATION_EDGE_FLAT = Blocks.POLISHED_ANDESITE.defaultBlockState();
    private static final BlockState FOUNDATION_EDGE_STAIR = Blocks.POLISHED_ANDESITE_STAIRS.defaultBlockState();
    private static final BlockState AIR = Blocks.AIR.defaultBlockState();
    private static final PieceSpec GATE = SectSettlementFeature.piece("sect_gate", 9, 24, 33, 1);
    private static final PieceSpec MAIN_HALL = SectSettlementFeature.piece("sect_main_hall", 27, 19, 24, 1);
    private static final PieceSpec ANCESTOR_CAVE = SectSettlementFeature.piece("ancestor_cave", 19, 12, 19);
    private static final PieceSpec DISCIPLE_RESIDENCE = SectSettlementFeature.piece("disciple_residence", 13, 10, 11);
    private static final PieceSpec SERVANTS_RESIDENCE = SectSettlementFeature.piece("servants_residence", 11, 10, 11);
    private static final PieceSpec PROTECTION_ARRAY = SectSettlementFeature.piece("sect_protection_array", 27, 3, 27, 1);
    private static final PieceSpec PAVILION = SectSettlementFeature.piece("chinese_style_pavilion", 13, 16, 13);
    private static final PieceSpec STREETLIGHT = SectSettlementFeature.piece("chinese_style_streetlight", 5, 8, 5, 1);
    private static final PieceSpec ABANDONED_ALCHEMY_FURNACE = SectSettlementFeature.piece("abandoned_alchemy_furnace", 13, 6, 13);
    private static final PieceSpec OLD_ALCHEMY_FURNACE = SectSettlementFeature.piece("old_alchemy_furnace", 13, 6, 13);
    private static final PieceSpec BRAND_NEW_ALCHEMY_FURNACE = SectSettlementFeature.piece("brand_new_alchemy_furnace", 13, 6, 13);
    private static final PieceSpec ABANDONED_WEAPON_FORGING_FURNACE = SectSettlementFeature.piece("abandoned_weapon_forging_furnace", 13, 6, 13);
    private static final PieceSpec OLD_WEAPON_FORGING_FURNACE = SectSettlementFeature.piece("old_weapon_forging_furnace", 13, 6, 13);
    private static final PieceSpec BRAND_NEW_WEAPON_FORGING_FURNACE = SectSettlementFeature.piece("brand_new_weapon_forging_furnace", 13, 6, 13);
    private static final PieceSpec[] ALCHEMY_FURNACE_VARIANTS = new PieceSpec[]{ABANDONED_ALCHEMY_FURNACE, OLD_ALCHEMY_FURNACE, BRAND_NEW_ALCHEMY_FURNACE};
    private static final PieceSpec[] WEAPON_FORGING_FURNACE_VARIANTS = new PieceSpec[]{ABANDONED_WEAPON_FORGING_FURNACE, OLD_WEAPON_FORGING_FURNACE, BRAND_NEW_WEAPON_FORGING_FURNACE};
    private static final PieceSpec[] REQUIRED = new PieceSpec[]{GATE, MAIN_HALL, ANCESTOR_CAVE, DISCIPLE_RESIDENCE, SERVANTS_RESIDENCE, PROTECTION_ARRAY, PAVILION, STREETLIGHT, ABANDONED_ALCHEMY_FURNACE, OLD_ALCHEMY_FURNACE, BRAND_NEW_ALCHEMY_FURNACE, ABANDONED_WEAPON_FORGING_FURNACE, OLD_WEAPON_FORGING_FURNACE, BRAND_NEW_WEAPON_FORGING_FURNACE};
    private static final int CELL_CACHE_CAP = 48;
    private static final Map<Long, CachedCell> CELL_CACHE = new ConcurrentHashMap<Long, CachedCell>();
    private static volatile long cellCacheWorldSeed = Long.MIN_VALUE;
    private static final Object CELL_CACHE_SEED_LOCK = new Object();
    private static volatile Boolean templatesValidCache = null;
    private static volatile StructureTemplateManager templatesValidManager = null;
    private static final Object TEMPLATE_VALIDATION_LOCK = new Object();

    public SectSettlementFeature(Codec<NoneFeatureConfiguration> codec) {
        super(codec);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public boolean place(@NotNull FeaturePlaceContext<NoneFeatureConfiguration> ctx) {
        long startedNanos = System.nanoTime();
        boolean placed = false;
        try {
            boolean bl = placed = this.placeInternal(ctx);
            return bl;
        }
        finally {
            SectSettlementFeature.logSlowPlacement(ctx, startedNanos, placed);
        }
    }

    private boolean placeInternal(@NotNull FeaturePlaceContext<NoneFeatureConfiguration> ctx) {
        WorldGenLevel level = ctx.level();
        ServerLevel server = level.getLevel();
        if (server.dimension() != Level.OVERWORLD) {
            return false;
        }
        BlockPos origin = ctx.origin();
        int chunkX = origin.getX() >> 4;
        int chunkZ = origin.getZ() >> 4;
        CellPlan cellPlan = SectSettlementFeature.chooseCellPlan(level.getSeed(), chunkX, chunkZ);
        if (!cellPlan.shouldGenerateIn(chunkX, chunkZ)) {
            return false;
        }
        BlockPos center = new BlockPos((cellPlan.targetChunkX() << 4) + 8, origin.getY(), (cellPlan.targetChunkZ() << 4) + 8);
        CachedCell cell = SectSettlementFeature.cachedCell(level, cellPlan, center);
        if (!cell.biomeGatePass()) {
            return false;
        }
        SectPlan plan = cell.plan();
        boolean hasArray = cell.hasArray();
        Set<Long> roadBlockingFootprint = cell.roadBlockingFootprint();
        Set<Long> foundationConnectorFootprint = cell.foundationConnectorFootprint();
        Set<Long> allBuildingFootprint = cell.allBuildingFootprint();
        Set<Long> roadFootprint = cell.roadFootprint();
        Map<PlacedPiece, Integer> pieceBaseYs = cell.pieceBaseYs();
        List<RoadAnchor> roadAnchors = cell.roadAnchors();
        List<StreetlightCandidate> streetlightCandidates = cell.streetlightCandidates();
        int sectRadius = cell.sectRadius();
        BoundingBox chunkBounds = new BoundingBox(chunkX << 4, level.getMinBuildHeight(), chunkZ << 4, (chunkX << 4) + 15, level.getMaxBuildHeight() - 1, (chunkZ << 4) + 15);
        boolean planTouchesChunk = SectSettlementFeature.planIntersectsChunk(plan, center, chunkBounds);
        boolean outerGuardsTouchChunk = SectSettlementFeature.outerGuardDisciplesIntersectChunk(cellPlan.seed(), center, chunkBounds, sectRadius, hasArray);
        boolean receptionGuardsTouchChunk = SectSettlementFeature.receptionGuardDisciplesIntersectChunk(center, chunkBounds, plan, sectRadius, hasArray);
        if (!(planTouchesChunk || outerGuardsTouchChunk || receptionGuardsTouchChunk)) {
            return false;
        }
        StructureTemplateManager manager = server.getStructureManager();
        if (!SectSettlementFeature.validateTemplatesCached(manager)) {
            return false;
        }
        SectGeneration sect = null;
        boolean placed = false;
        if (!planTouchesChunk) {
            if (outerGuardsTouchChunk || receptionGuardsTouchChunk) {
                sect = SectSettlementFeature.prepareSect(server, cellPlan.seed(), center, hasArray, sectRadius);
            }
            if (outerGuardsTouchChunk && sect != null) {
                placed |= SectSettlementFeature.spawnOuterGuardDisciples(level, server, cellPlan.seed(), center, chunkBounds, plan, sect, pieceBaseYs);
            }
            if (receptionGuardsTouchChunk && sect != null) {
                placed |= SectSettlementFeature.spawnReceptionGuardDisciples(level, server, cellPlan.seed(), center, chunkBounds, plan, sect);
            }
            return placed;
        }
        sect = SectSettlementFeature.prepareSect(server, cellPlan.seed(), center, hasArray, sectRadius);
        for (PlacedPiece piece : plan.pieces()) {
            int baseY = SectSettlementFeature.cachedPieceBaseY(pieceBaseYs, server, cellPlan.seed(), center, plan, piece);
            boolean piecePlaced = SectSettlementFeature.placePiece(level, server, manager, cellPlan.seed(), center, chunkBounds, plan, piece, baseY, foundationConnectorFootprint, allBuildingFootprint);
            if (piecePlaced) {
                SectSettlementFeature.initializeSectPiece(level, server, cellPlan.seed(), center, chunkBounds, plan, piece, sect, baseY, allBuildingFootprint, roadFootprint, streetlightCandidates);
            }
            placed |= piecePlaced;
        }
        LinkedHashMap<Long, RoadCell> roadCells = new LinkedHashMap<Long, RoadCell>();
        for (RoadLine road : plan.roads()) {
            SectSettlementFeature.collectRoadCells(level, center, road, chunkBounds, roadAnchors, cellPlan.seed(), roadCells);
        }
        placed |= SectSettlementFeature.paveRoadCells(level, chunkBounds, roadBlockingFootprint, allBuildingFootprint, cellPlan.seed(), roadCells.values());
        for (PlacedPiece piece : plan.pieces()) {
            int baseY = SectSettlementFeature.cachedPieceBaseY(pieceBaseYs, server, cellPlan.seed(), center, plan, piece);
            placed |= SectSettlementFeature.restoreGateSupports(level, cellPlan.seed(), center, chunkBounds, piece, baseY);
        }
        placed |= SectSettlementFeature.placeStreetlights(level, manager, cellPlan.seed(), center, chunkBounds, plan.roads(), roadAnchors, streetlightCandidates, allBuildingFootprint);
        placed |= SectSettlementFeature.spawnOuterGuardDisciples(level, server, cellPlan.seed(), center, chunkBounds, plan, sect, pieceBaseYs);
        return placed |= SectSettlementFeature.spawnReceptionGuardDisciples(level, server, cellPlan.seed(), center, chunkBounds, plan, sect);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private static CachedCell cachedCell(WorldGenLevel level, CellPlan cellPlan, BlockPos center) {
        CachedCell prior;
        long key;
        CachedCell cached;
        long worldSeed = level.getSeed();
        if (worldSeed != cellCacheWorldSeed) {
            Object object = CELL_CACHE_SEED_LOCK;
            synchronized (object) {
                if (worldSeed != cellCacheWorldSeed) {
                    CELL_CACHE.clear();
                    cellCacheWorldSeed = worldSeed;
                }
            }
        }
        if ((cached = CELL_CACHE.get(key = cellPlan.seed())) != null) {
            return cached;
        }
        CachedCell built = SectSettlementFeature.buildCachedCell(level, cellPlan, center);
        if (CELL_CACHE.size() >= 48) {
            CELL_CACHE.clear();
        }
        return (prior = CELL_CACHE.putIfAbsent(key, built)) != null ? prior : built;
    }

    private static CachedCell buildCachedCell(WorldGenLevel level, CellPlan cellPlan, BlockPos center) {
        SectPlan plan = SectSettlementFeature.createPlan(RandomSource.create((long)SectSettlementFeature.seedFor(cellPlan.seed(), 0, 0, 6211354)));
        boolean hasArray = SectSettlementFeature.planHasProtectionArray(plan);
        HashSet<Long> roadBlockingFootprint = new HashSet<Long>();
        SectSettlementFeature.markPlanFootprints(plan, center, roadBlockingFootprint);
        SectSettlementFeature.markGateSupportFootprints(plan, center, roadBlockingFootprint);
        HashSet<Long> foundationConnectorFootprint = new HashSet<Long>();
        SectSettlementFeature.markFoundationConnectorFootprints(plan, center, foundationConnectorFootprint);
        HashSet<Long> allBuildingFootprint = new HashSet<Long>();
        SectSettlementFeature.markAllPlanFootprints(plan, center, allBuildingFootprint);
        HashSet<Long> roadFootprint = new HashSet<Long>();
        SectSettlementFeature.markRoadFootprints(plan.roads(), center, roadBlockingFootprint, roadFootprint);
        List<StreetlightCandidate> streetlightCandidates = SectSettlementFeature.planStreetlightCandidates(cellPlan.seed(), center, plan.roads(), allBuildingFootprint, roadFootprint);
        int sectRadius = SectSettlementFeature.plannedSectRadius(plan, cellPlan.seed(), center, allBuildingFootprint, roadFootprint, streetlightCandidates);
        boolean biomeGatePass = SectSettlementFeature.passesSectSiteBiomeGate(level, center, sectRadius, cellPlan.seed());
        Map<PlacedPiece, Integer> pieceBaseYs = biomeGatePass ? SectSettlementFeature.collectPieceBaseYs(level.getLevel(), cellPlan.seed(), center, plan) : Map.of();
        List<RoadAnchor> roadAnchors = biomeGatePass ? SectSettlementFeature.collectRoadAnchors(level.getLevel(), cellPlan.seed(), center, plan, pieceBaseYs) : List.of();
        return new CachedCell(plan, hasArray, roadBlockingFootprint, foundationConnectorFootprint, allBuildingFootprint, roadFootprint, pieceBaseYs, roadAnchors, streetlightCandidates, sectRadius, biomeGatePass);
    }

    private static void logSlowPlacement(FeaturePlaceContext<NoneFeatureConfiguration> ctx, long startedNanos, boolean placed) {
        int thresholdMs = ModCommonConfig.sectWorldgenSlowLogMs();
        if (thresholdMs <= 0) {
            return;
        }
        long elapsedMs = (System.nanoTime() - startedNanos) / 1000000L;
        if (elapsedMs < (long)thresholdMs) {
            return;
        }
        BlockPos origin = ctx.origin();
        LOGGER.warn("Slow sect settlement worldgen: {} ms at {},{},{} chunk [{}, {}], placed={}", new Object[]{elapsedMs, origin.getX(), origin.getY(), origin.getZ(), origin.getX() >> 4, origin.getZ() >> 4, placed});
    }

    private static boolean passesSectSiteBiomeGate(WorldGenLevel level, BlockPos center, int sectRadius, long cellSeed) {
        Holder<Biome> centerBiome = SectSettlementFeature.noiseBiomeAt(level, center.getX(), center.getZ());
        int total = 1;
        int open = SectSettlementFeature.isLowTreeSectBiome(centerBiome) ? 1 : 0;
        int sampleRadius = Mth.clamp((int)(sectRadius - 8), (int)48, (int)112);
        for (int i = 0; i < 8; ++i) {
            double angle = Math.PI * 2 * (double)i / 8.0;
            int sampleX = center.getX() + Mth.floor((double)(Math.cos(angle) * (double)sampleRadius));
            int sampleZ = center.getZ() + Mth.floor((double)(Math.sin(angle) * (double)sampleRadius));
            ++total;
            if (!SectSettlementFeature.isLowTreeSectBiome(SectSettlementFeature.noiseBiomeAt(level, sampleX, sampleZ))) continue;
            ++open;
        }
        float openFraction = (float)open / (float)total;
        float treedChance = (float)Mth.clamp((double)ModCommonConfig.sectTreedBiomeSpawnChance(), (double)0.0, (double)1.0);
        float acceptChance = Mth.lerp(openFraction, treedChance, 1.0f);
        float roll = RandomSource.create((long)(cellSeed ^ 0x5EC7B107E57E51L)).nextFloat();
        return roll < acceptChance;
    }

    private static Holder<Biome> noiseBiomeAt(WorldGenLevel level, int x, int z) {
        return level.getUncachedNoiseBiome(QuartPos.fromBlock((int)x), QuartPos.fromBlock((int)64), QuartPos.fromBlock((int)z));
    }

    private static boolean isLowTreeSectBiome(Holder<Biome> biome) {
        if (biome.is(BiomeTags.IS_FOREST) || biome.is(BiomeTags.IS_JUNGLE) || biome.is(BiomeTags.IS_TAIGA) || biome.is(BiomeTags.IS_OCEAN) || biome.is(BiomeTags.IS_RIVER)) {
            return false;
        }
        String path = biome.unwrapKey().map(key -> key.location().getPath()).orElse("");
        return !path.contains("forest") && !path.contains("jungle") && !path.contains("taiga") && !path.contains("swamp") && !path.contains("mangrove") && !path.contains("bamboo") && !path.contains("cherry") && !path.contains("grove") && !path.contains("mushroom") && !path.contains("beach") && !path.contains("shore");
    }

    private static boolean validateTemplates(StructureTemplateManager manager) {
        for (PieceSpec piece : REQUIRED) {
            Optional template = manager.get(piece.id());
            if (template.isEmpty()) {
                return false;
            }
            Vec3i actual = ((StructureTemplate)template.get()).getSize();
            if (actual.getX() == piece.sizeX() && actual.getY() == piece.sizeY() && actual.getZ() == piece.sizeZ()) continue;
            return false;
        }
        return true;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private static boolean validateTemplatesCached(StructureTemplateManager manager) {
        Boolean cached = templatesValidCache;
        if (cached != null && templatesValidManager == manager) {
            return cached;
        }
        Object object = TEMPLATE_VALIDATION_LOCK;
        synchronized (object) {
            cached = templatesValidCache;
            if (cached != null && templatesValidManager == manager) {
                return cached;
            }
            boolean valid = SectSettlementFeature.validateTemplates(manager);
            if (valid) {
                templatesValidManager = manager;
                templatesValidCache = true;
            }
            return valid;
        }
    }

    private static CellPlan chooseCellPlan(long worldSeed, int chunkX, int chunkZ) {
        int cellZ;
        int cellX = Math.floorDiv(chunkX, 32);
        long seed = worldSeed ^ (long)cellX * 341873128712L ^ (long)(cellZ = Math.floorDiv(chunkZ, 32)) * 132897987541L ^ 0x5ECA71C0A57E77L;
        RandomSource random = RandomSource.create((long)seed);
        boolean enabled = random.nextFloat() < SectSettlementFeature.sectSettlementCellSpawnChance();
        int span = Math.max(1, 6);
        int targetChunkX = cellX * 32 + 13 + random.nextInt(span);
        int targetChunkZ = cellZ * 32 + 13 + random.nextInt(span);
        return new CellPlan(enabled, targetChunkX, targetChunkZ, seed);
    }

    private static float sectSettlementCellSpawnChance() {
        return Mth.clamp((float)((float)ModCommonConfig.sectSettlementCellSpawnChance()), (float)0.0f, (float)1.0f);
    }

    private static SectPlan createPlan(RandomSource random) {
        int scale = SectSettlementFeature.chooseScale(random);
        int spineX = SectSettlementFeature.randomBetween(random, -8, 8);
        int northEnd = -SectSettlementFeature.randomBetween(random, 54 + scale * 8, 68 + scale * 10);
        int southEnd = SectSettlementFeature.randomBetween(random, 42 + scale * 8, 54 + scale * 12);
        boolean hasProtectionArray = SectSettlementFeature.shouldAddProtectionArray(random, scale);
        PlanBuilder builder = new PlanBuilder();
        ArrayList<RoadTarget> branchTargets = new ArrayList<RoadTarget>();
        if (hasProtectionArray) {
            builder.addPiece(PROTECTION_ARRAY, -13, -13, 0, -14, true, true);
            builder.addRoad(spineX, northEnd - 8, spineX, -18);
            builder.addRoad(spineX, -18, 0, -18);
            builder.addRoad(0, -18, 0, -14);
            builder.addRoad(0, 14, spineX, 14);
        } else {
            builder.addRoad(spineX, northEnd - 8, spineX, 12);
        }
        int mainX = Mth.clamp((int)(spineX - MAIN_HALL.sizeX() / 2 + SectSettlementFeature.randomBetween(random, -7, 7)), (int)-40, (int)18);
        int mainZ = SectSettlementFeature.randomBetween(random, hasProtectionArray ? 20 : 10, hasProtectionArray ? 32 : 22);
        int mainConnectorX = mainX + MAIN_HALL.sizeX() / 2;
        int mainConnectorZ = mainZ - 1;
        builder.addPiece(MAIN_HALL, mainX, mainZ, mainConnectorX, mainConnectorZ, true, true);
        builder.addRoad(spineX, hasProtectionArray ? 14 : 12, spineX, mainConnectorZ);
        builder.addRoad(spineX, mainConnectorZ, mainConnectorX, mainConnectorZ);
        SectSettlementFeature.addAncestorCave(random, scale, builder, mainX, mainZ);
        SectSettlementFeature.addSecondMainHall(random, scale, builder, spineX, mainX, mainZ, southEnd);
        SectSettlementFeature.addGates(random, scale, builder, spineX, northEnd, southEnd);
        SectSettlementFeature.addResidences(random, scale, builder, branchTargets, spineX, northEnd, mainZ, southEnd);
        SectSettlementFeature.addPavilions(random, scale, builder, branchTargets, spineX, northEnd, mainZ, southEnd);
        SectSettlementFeature.addFurnaceBuildings(random, scale, builder, branchTargets, spineX, northEnd, mainZ, southEnd);
        SectSettlementFeature.addBranchedTargetRoads(random, builder, spineX, branchTargets);
        return builder.toPlan();
    }

    private static int chooseScale(RandomSource random) {
        int roll = random.nextInt(100);
        if (roll < 35) {
            return 0;
        }
        if (roll < 78) {
            return 1;
        }
        return 2;
    }

    private static boolean shouldAddProtectionArray(RandomSource random, int scale) {
        return switch (scale) {
            case 0 -> {
                if (random.nextFloat() < 0.2f) {
                    yield true;
                }
                yield false;
            }
            case 1 -> {
                if (random.nextFloat() < 0.65f) {
                    yield true;
                }
                yield false;
            }
            default -> random.nextFloat() < 0.95f;
        };
    }

    private static void addAncestorCave(RandomSource random, int scale, PlanBuilder builder, int mainX, int mainZ) {
        if (random.nextFloat() >= SectSettlementFeature.ancestorCaveSpawnChance(scale)) {
            return;
        }
        int x = mainX + MAIN_HALL.sizeX() / 2 - ANCESTOR_CAVE.sizeX() / 2;
        int z = mainZ + MAIN_HALL.sizeZ() / 2 - ANCESTOR_CAVE.sizeZ() / 2;
        builder.addPiece(ANCESTOR_CAVE, x, z, x + ANCESTOR_CAVE.sizeX() / 2, z + ANCESTOR_CAVE.sizeZ() / 2, false, true);
    }

    private static float ancestorCaveSpawnChance(int scale) {
        return switch (scale) {
            case 0 -> 0.18f;
            case 1 -> 0.45f;
            default -> 0.78f;
        };
    }

    private static void addSecondMainHall(RandomSource random, int scale, PlanBuilder builder, int spineX, int firstHallX, int firstHallZ, int southEnd) {
        int connectorZ;
        int connectorX;
        int z;
        if (scale != 2 || random.nextFloat() >= 0.28f) {
            return;
        }
        int x = Mth.clamp((int)(firstHallX + SectSettlementFeature.randomBetween(random, -14, 14)), (int)-42, (int)28);
        if (builder.tryAddPiece(MAIN_HALL, x, z = Math.min(southEnd - MAIN_HALL.sizeZ() - 2, firstHallZ + SectSettlementFeature.randomBetween(random, 24, 36)), connectorX = x + MAIN_HALL.sizeX() / 2, connectorZ = z - 1, true, true)) {
            builder.addRoad(spineX, connectorZ, connectorX, connectorZ);
        }
    }

    private static void addGates(RandomSource random, int scale, PlanBuilder builder, int spineX, int northEnd, int southEnd) {
        int gateCount = switch (scale) {
            case 0 -> random.nextFloat() < 0.55f ? 1 : 0;
            case 1 -> 1 + random.nextInt(2);
            default -> 2 + random.nextInt(3);
        };
        if (gateCount <= 0) {
            return;
        }
        Rotation mainGateRotation = SectSettlementFeature.rotationForGateFacing(Direction.NORTH);
        int mainGateX = spineX - SectSettlementFeature.placedSizeX(GATE, mainGateRotation) / 2 + SectSettlementFeature.randomBetween(random, -2, 2);
        SectSettlementFeature.addGate(builder, mainGateX, northEnd, Direction.NORTH, spineX);
        for (int i = 1; i < gateCount; ++i) {
            int gateZ;
            int gateX;
            Direction facing = SectSettlementFeature.chooseSideGateFacing(random, scale);
            Rotation rotation = SectSettlementFeature.rotationForGateFacing(facing);
            int sizeX = SectSettlementFeature.placedSizeX(GATE, rotation);
            int sizeZ = SectSettlementFeature.placedSizeZ(GATE, rotation);
            if (facing == Direction.SOUTH) {
                gateX = spineX - sizeX / 2 + SectSettlementFeature.randomBetween(random, -2, 2);
                gateZ = southEnd - sizeZ + SectSettlementFeature.randomBetween(random, -2, 2);
            } else {
                int distance = SectSettlementFeature.randomBetween(random, 42, 70);
                int connectorZ = SectSettlementFeature.randomBetween(random, northEnd + 8, Math.min(-30, southEnd - sizeZ - 8));
                gateX = facing == Direction.WEST ? spineX - distance - sizeX : spineX + distance;
                gateZ = connectorZ - sizeZ / 2 + SectSettlementFeature.randomBetween(random, -2, 2);
            }
            SectSettlementFeature.addGate(builder, gateX, gateZ, facing, spineX);
        }
    }

    private static Direction chooseSideGateFacing(RandomSource random, int scale) {
        if (scale >= 1 && random.nextInt(5) == 0) {
            return Direction.SOUTH;
        }
        return random.nextBoolean() ? Direction.WEST : Direction.EAST;
    }

    private static void addGate(PlanBuilder builder, int x, int z, Direction facing, int spineX) {
        int connectorZ;
        Rotation rotation = SectSettlementFeature.rotationForGateFacing(facing);
        GatePass pass = SectSettlementFeature.gatePass(rotation);
        int connectorX = x + pass.x();
        if (builder.tryAddPiece(GATE, x, z, connectorX, connectorZ = z + pass.z(), false, false, rotation)) {
            int sizeX = SectSettlementFeature.placedSizeX(GATE, rotation);
            int sizeZ = SectSettlementFeature.placedSizeZ(GATE, rotation);
            if (pass.axis() == Direction.Axis.X) {
                int westConnectorX = x - 7;
                int eastConnectorX = x + sizeX + 5;
                int insideX = facing == Direction.EAST ? westConnectorX : eastConnectorX;
                builder.addRoad(westConnectorX, connectorZ, eastConnectorX, connectorZ);
                builder.addRoad(insideX, connectorZ, spineX, connectorZ);
            } else {
                int northConnectorZ = z - 7;
                int southConnectorZ = z + sizeZ + 5;
                int insideZ = facing == Direction.SOUTH ? northConnectorZ : southConnectorZ;
                builder.addRoad(connectorX, northConnectorZ, connectorX, southConnectorZ);
                builder.addRoad(connectorX, insideZ, spineX, insideZ);
            }
        }
    }

    private static void addResidences(RandomSource random, int scale, PlanBuilder builder, List<RoadTarget> branchTargets, int spineX, int northEnd, int mainZ, int southEnd) {
        int discipleCount = switch (scale) {
            case 0 -> 2 + random.nextInt(2);
            case 1 -> 4 + random.nextInt(4);
            default -> 7 + random.nextInt(6);
        };
        int servantCount = switch (scale) {
            case 0 -> 2 + random.nextInt(2);
            case 1 -> 3 + random.nextInt(4);
            default -> 5 + random.nextInt(6);
        };
        SectSettlementFeature.addLivingPlots(random, builder, branchTargets, DISCIPLE_RESIDENCE, discipleCount, spineX, northEnd, mainZ, southEnd, 18, 54);
        SectSettlementFeature.addLivingPlots(random, builder, branchTargets, SERVANTS_RESIDENCE, servantCount, spineX, northEnd, mainZ, southEnd, 22, 62);
    }

    private static void addLivingPlots(RandomSource random, PlanBuilder builder, List<RoadTarget> branchTargets, PieceSpec piece, int count, int spineX, int northEnd, int mainZ, int southEnd, int minDistance, int maxDistance) {
        int placed = 0;
        int tries = 0;
        int minZ = northEnd + 18;
        int maxZ = Math.max(minZ + 10, Math.min(southEnd - 8, mainZ + 18));
        while (placed < count && tries++ < count * 28) {
            int actualConnectorZ;
            int connectorX;
            int z;
            int side = random.nextBoolean() ? -1 : 1;
            int distance = SectSettlementFeature.randomBetween(random, minDistance, maxDistance);
            int connectorZ = SectSettlementFeature.randomBetween(random, minZ, maxZ);
            int jitterZ = SectSettlementFeature.randomBetween(random, -3, 3);
            int x = side < 0 ? spineX - distance - piece.sizeX() : spineX + distance;
            if (!builder.tryAddPiece(piece, x, z = connectorZ - piece.sizeZ() / 2 + jitterZ, connectorX = x + SectSettlementFeature.livingDoorX(piece), actualConnectorZ = z - 1, true, false)) continue;
            branchTargets.add(new RoadTarget(connectorX, actualConnectorZ, side));
            ++placed;
        }
    }

    private static int livingDoorX(PieceSpec piece) {
        if (piece == DISCIPLE_RESIDENCE) {
            return 8;
        }
        if (piece == SERVANTS_RESIDENCE) {
            return 5;
        }
        return piece.sizeX() / 2;
    }

    private static void addPavilions(RandomSource random, int scale, PlanBuilder builder, List<RoadTarget> branchTargets, int spineX, int northEnd, int mainZ, int southEnd) {
        int count = scale == 0 ? (random.nextFloat() < 0.35f ? 1 : 0) : (scale == 1 ? 1 + random.nextInt(2) : 2 + random.nextInt(2));
        int placed = 0;
        int tries = 0;
        while (placed < count && tries++ < 30) {
            int connectorZ;
            int connectorX;
            int z;
            int side = random.nextBoolean() ? -1 : 1;
            int x = spineX + side * SectSettlementFeature.randomBetween(random, 16, 38) - PAVILION.sizeX() / 2;
            if (!builder.tryAddPiece(PAVILION, x, z = SectSettlementFeature.randomBetween(random, northEnd + 22, Math.min(southEnd - 14, mainZ + 10)), connectorX = side < 0 ? x + PAVILION.sizeX() : x - 1, connectorZ = z + PAVILION.sizeZ() / 2, true, false)) continue;
            branchTargets.add(new RoadTarget(connectorX, connectorZ, side));
            ++placed;
        }
    }

    private static void addFurnaceBuildings(RandomSource random, int scale, PlanBuilder builder, List<RoadTarget> branchTargets, int spineX, int northEnd, int mainZ, int southEnd) {
        int alchemySide = random.nextBoolean() ? -1 : 1;
        boolean placedAlchemy = SectSettlementFeature.addFurnaceBuilding(random, scale, builder, branchTargets, ALCHEMY_FURNACE_VARIANTS, SectSettlementFeature.workshopSpawnChance(scale, true), spineX, northEnd, mainZ, southEnd, alchemySide);
        int forgingSide = placedAlchemy ? -alchemySide : (random.nextBoolean() ? -1 : 1);
        SectSettlementFeature.addFurnaceBuilding(random, scale, builder, branchTargets, WEAPON_FORGING_FURNACE_VARIANTS, SectSettlementFeature.workshopSpawnChance(scale, false), spineX, northEnd, mainZ, southEnd, forgingSide);
    }

    private static boolean addFurnaceBuilding(RandomSource random, int scale, PlanBuilder builder, List<RoadTarget> branchTargets, PieceSpec[] variants, float spawnChance, int spineX, int northEnd, int mainZ, int southEnd, int preferredSide) {
        if (random.nextFloat() >= spawnChance) {
            return false;
        }
        PieceSpec piece = SectSettlementFeature.chooseFurnaceVariant(random, scale, variants);
        int placed = 0;
        int tries = 0;
        int minZ = northEnd + 24;
        int maxZ = Math.max(minZ + 8, Math.min(southEnd - piece.sizeZ() - 6, mainZ + 28 + scale * 10));
        int minDistance = 28;
        int maxDistance = 50 + scale * 10;
        while (placed < 1 && tries++ < 36) {
            int actualConnectorZ;
            int connectorX;
            int connectorZ;
            int z;
            int side = tries == 1 ? preferredSide : (tries == 2 ? -preferredSide : (random.nextBoolean() ? -1 : 1));
            int distance = SectSettlementFeature.randomBetween(random, minDistance, maxDistance);
            int x = side < 0 ? spineX - distance - piece.sizeX() : spineX + distance;
            if (!builder.tryAddPiece(piece, x, z = (connectorZ = SectSettlementFeature.randomBetween(random, minZ, maxZ)) - piece.sizeZ() / 2 + SectSettlementFeature.randomBetween(random, -4, 4), connectorX = side < 0 ? x + piece.sizeX() : x - 1, actualConnectorZ = z + piece.sizeZ() / 2, true, false)) continue;
            branchTargets.add(new RoadTarget(connectorX, actualConnectorZ, side));
            ++placed;
        }
        return placed > 0;
    }

    private static float workshopSpawnChance(int scale, boolean alchemy) {
        return switch (scale) {
            case 0 -> {
                if (alchemy) {
                    yield 0.32f;
                }
                yield 0.28f;
            }
            case 1 -> {
                if (alchemy) {
                    yield 0.58f;
                }
                yield 0.52f;
            }
            default -> alchemy ? 0.82f : 0.78f;
        };
    }

    private static PieceSpec chooseFurnaceVariant(RandomSource random, int scale, PieceSpec[] variants) {
        int roll = random.nextInt(100);
        return switch (scale) {
            case 0 -> {
                if (roll < 70) {
                    yield variants[0];
                }
                if (roll < 95) {
                    yield variants[1];
                }
                yield variants[2];
            }
            case 1 -> {
                if (roll < 20) {
                    yield variants[0];
                }
                if (roll < 80) {
                    yield variants[1];
                }
                yield variants[2];
            }
            default -> roll < 8 ? variants[0] : (roll < 38 ? variants[1] : variants[2]);
        };
    }

    private static void addBranchedTargetRoads(RandomSource random, PlanBuilder builder, int spineX, List<RoadTarget> targets) {
        SectSettlementFeature.addBranchedTargetRoads(random, builder, spineX, targets, -1);
        SectSettlementFeature.addBranchedTargetRoads(random, builder, spineX, targets, 1);
    }

    private static void addBranchedTargetRoads(RandomSource random, PlanBuilder builder, int spineX, List<RoadTarget> targets, int side) {
        ArrayList<RoadTarget> sideTargets = new ArrayList<RoadTarget>();
        for (RoadTarget target : targets) {
            if (target.side() != side) continue;
            sideTargets.add(target);
        }
        sideTargets.sort((a, b) -> Integer.compare(a.z(), b.z()));
        int start = 0;
        while (start < sideTargets.size()) {
            int end;
            for (end = start + 1; end < sideTargets.size() && end - start < 4 && ((RoadTarget)sideTargets.get(end)).z() - ((RoadTarget)sideTargets.get(end - 1)).z() <= 30; ++end) {
            }
            SectSettlementFeature.addTargetRoadCluster(random, builder, spineX, sideTargets.subList(start, end), side);
            start = end;
        }
    }

    private static void addTargetRoadCluster(RandomSource random, PlanBuilder builder, int spineX, List<RoadTarget> cluster, int side) {
        if (cluster.isEmpty()) {
            return;
        }
        int minZ = Integer.MAX_VALUE;
        int maxZ = Integer.MIN_VALUE;
        int sumZ = 0;
        int closestTargetX = side < 0 ? Integer.MIN_VALUE : Integer.MAX_VALUE;
        for (RoadTarget target : cluster) {
            minZ = Math.min(minZ, target.z());
            maxZ = Math.max(maxZ, target.z());
            sumZ += target.z();
            closestTargetX = side < 0 ? Math.max(closestTargetX, target.x()) : Math.min(closestTargetX, target.x());
        }
        int junctionZ = cluster.size() == 1 ? cluster.get(0).z() : Mth.clamp((int)(sumZ / cluster.size() + SectSettlementFeature.randomBetween(random, -4, 4)), (int)-104, (int)104);
        int branchX = side < 0 ? Math.min(spineX - 8, closestTargetX + SectSettlementFeature.randomBetween(random, 5, 9)) : Math.max(spineX + 8, closestTargetX - SectSettlementFeature.randomBetween(random, 5, 9));
        branchX = Mth.clamp((int)branchX, (int)-104, (int)104);
        builder.addWindingRoad(random, spineX, junctionZ, branchX, junctionZ);
        if (junctionZ < minZ || junctionZ > maxZ) {
            int nearestClusterZ = Math.abs(junctionZ - minZ) < Math.abs(junctionZ - maxZ) ? minZ : maxZ;
            builder.addWindingRoad(random, branchX, junctionZ, branchX, nearestClusterZ);
        }
        if (cluster.size() > 1) {
            builder.addWindingRoad(random, branchX, minZ, branchX, maxZ);
        }
        for (RoadTarget target : cluster) {
            builder.addWindingRoad(random, branchX, target.z(), target.x(), target.z());
        }
    }

    private static boolean placePiece(WorldGenLevel level, ServerLevel server, StructureTemplateManager manager, long seed, BlockPos center, BoundingBox chunkBounds, SectPlan plan, PlacedPiece placed, int baseY, Set<Long> foundationConnectorFootprint, Set<Long> allBuildingFootprint) {
        if (!SectSettlementFeature.pieceIntersectsChunk(center, placed, chunkBounds)) {
            return false;
        }
        StructureTemplate template = (StructureTemplate)manager.get(placed.piece().id()).orElseThrow();
        int worldX = center.getX() + placed.localX();
        int worldZ = center.getZ() + placed.localZ();
        if (!SectSettlementFeature.isAncestorCave(placed.piece())) {
            SectSettlementFeature.prepareFootprint(level, worldX, baseY, worldZ, placed, chunkBounds, seed, foundationConnectorFootprint, allBuildingFootprint);
        }
        StructurePlaceSettings settings = new StructurePlaceSettings().setIgnoreEntities(true).setKnownShape(true).setRotation(placed.rotation()).setRotationPivot(BlockPos.ZERO).setBoundingBox(chunkBounds).addProcessor((StructureProcessor)BlockIgnoreProcessor.STRUCTURE_BLOCK);
        int templateY = SectSettlementFeature.templateBaseY(baseY, placed.piece());
        BlockPos at = SectSettlementFeature.placementOrigin(worldX, templateY, worldZ, placed.piece(), placed.rotation());
        RandomSource random = RandomSource.create((long)SectSettlementFeature.seedFor(seed, placed.localX(), placed.localZ(), 335559));
        template.placeInWorld((ServerLevelAccessor)level, at, at, settings, random, 2);
        if (!SectSettlementFeature.isAncestorCave(placed.piece())) {
            SectSettlementFeature.removeTemplateTerrainBlocks(level, worldX, baseY, worldZ, placed, chunkBounds, seed);
            SectSettlementFeature.repairFoundationSurface(level, worldX, baseY, worldZ, placed, chunkBounds, seed);
        }
        SectSettlementFeature.fillSectContainers(level, seed, placed, chunkBounds, worldX, baseY, worldZ);
        return true;
    }

    private static boolean planHasProtectionArray(SectPlan plan) {
        return plan.pieces().stream().anyMatch(piece -> piece.piece().equals(PROTECTION_ARRAY));
    }

    private static SectGeneration prepareSect(ServerLevel server, long seed, BlockPos center, boolean hasArray, int radius) {
        SectSavedData data = SectSavedData.get(server);
        SectSavedData.SectRecord record = data.getOrCreateGeneratedSect(server, seed, center, hasArray, radius);
        return new SectGeneration(record.id, record.name, record.radius, record.hasProtectionArray, data);
    }

    private static int plannedSectRadius(SectPlan plan, long seed, BlockPos center, Set<Long> allBuildingFootprint, Set<Long> roadFootprint) {
        return SectSettlementFeature.plannedSectRadius(plan, seed, center, allBuildingFootprint, roadFootprint, SectSettlementFeature.planStreetlightCandidates(seed, center, plan.roads(), allBuildingFootprint, roadFootprint));
    }

    private static int plannedSectRadius(SectPlan plan, long seed, BlockPos center, Set<Long> allBuildingFootprint, Set<Long> roadFootprint, List<StreetlightCandidate> streetlightCandidates) {
        List<BlockPos> targets = SectSettlementFeature.plannedSectRadiusTargets(plan, seed, center, allBuildingFootprint, roadFootprint, streetlightCandidates);
        ArrayList<BlockPos> origins = new ArrayList<BlockPos>();
        origins.add(center);
        for (PlacedPiece piece : plan.pieces()) {
            if (!piece.piece().equals(PROTECTION_ARRAY)) continue;
            int x0 = center.getX() + piece.localX();
            int z0 = center.getZ() + piece.localZ();
            int x1 = x0 + SectSettlementFeature.placedSizeX(piece) - 1;
            int z1 = z0 + SectSettlementFeature.placedSizeZ(piece) - 1;
            origins.add(new BlockPos(x0, center.getY(), z0));
            origins.add(new BlockPos(x0, center.getY(), z1));
            origins.add(new BlockPos(x1, center.getY(), z0));
            origins.add(new BlockPos(x1, center.getY(), z1));
        }
        return SectSettlementFeature.plannedRadiusFromOrigins(origins, targets);
    }

    private static int plannedGeneratedArrayRadius(SectPlan plan, long seed, BlockPos center, Set<Long> allBuildingFootprint, Set<Long> roadFootprint, Collection<BlockPos> flagPositions) {
        return SectSettlementFeature.plannedGeneratedArrayRadius(plan, seed, center, allBuildingFootprint, roadFootprint, flagPositions, SectSettlementFeature.planStreetlightCandidates(seed, center, plan.roads(), allBuildingFootprint, roadFootprint));
    }

    private static int plannedGeneratedArrayRadius(SectPlan plan, long seed, BlockPos center, Set<Long> allBuildingFootprint, Set<Long> roadFootprint, Collection<BlockPos> flagPositions, List<StreetlightCandidate> streetlightCandidates) {
        if (flagPositions == null || flagPositions.size() < GENERATED_ARRAY_FLAG_COUNT) {
            return SectSettlementFeature.plannedSectRadius(plan, seed, center, allBuildingFootprint, roadFootprint, streetlightCandidates);
        }
        return SectSettlementFeature.plannedRadiusFromOrigins(flagPositions, SectSettlementFeature.plannedSectRadiusTargets(plan, seed, center, allBuildingFootprint, roadFootprint, streetlightCandidates));
    }

    private static List<BlockPos> plannedSectRadiusTargets(SectPlan plan, long seed, BlockPos center, Set<Long> allBuildingFootprint, Set<Long> roadFootprint) {
        return SectSettlementFeature.plannedSectRadiusTargets(plan, seed, center, allBuildingFootprint, roadFootprint, SectSettlementFeature.planStreetlightCandidates(seed, center, plan.roads(), allBuildingFootprint, roadFootprint));
    }

    private static List<BlockPos> plannedSectRadiusTargets(SectPlan plan, long seed, BlockPos center, Set<Long> allBuildingFootprint, Set<Long> roadFootprint, List<StreetlightCandidate> streetlightCandidates) {
        int z1;
        int x1;
        int z0;
        int x0;
        ArrayList<BlockPos> targets = new ArrayList<BlockPos>();
        for (PlacedPiece piece : plan.pieces()) {
            x0 = center.getX() + piece.localX();
            z0 = center.getZ() + piece.localZ();
            x1 = x0 + SectSettlementFeature.placedSizeX(piece) - 1;
            z1 = z0 + SectSettlementFeature.placedSizeZ(piece) - 1;
            targets.add(new BlockPos(x0, center.getY(), z0));
            targets.add(new BlockPos(x0, center.getY(), z1));
            targets.add(new BlockPos(x1, center.getY(), z0));
            targets.add(new BlockPos(x1, center.getY(), z1));
        }
        for (StreetlightCandidate candidate : streetlightCandidates) {
            x0 = candidate.x() - 2;
            z0 = candidate.z() - 2;
            x1 = x0 + STREETLIGHT.sizeX() - 1;
            z1 = z0 + STREETLIGHT.sizeZ() - 1;
            targets.add(new BlockPos(x0, center.getY(), z0));
            targets.add(new BlockPos(x0, center.getY(), z1));
            targets.add(new BlockPos(x1, center.getY(), z0));
            targets.add(new BlockPos(x1, center.getY(), z1));
        }
        return targets;
    }

    private static int plannedRadiusFromOrigins(Collection<BlockPos> origins, Collection<BlockPos> targets) {
        int max = 48;
        if (origins == null || origins.isEmpty() || targets == null || targets.isEmpty()) {
            return FormationCorePlateBlockEntity.clampFlagEffectRadius(max + 28);
        }
        for (BlockPos origin : origins) {
            if (origin == null) continue;
            for (BlockPos target : targets) {
                if (target == null) continue;
                max = Math.max(max, SectSettlementFeature.horizontalDistance(origin.getX(), origin.getZ(), target.getX(), target.getZ()));
            }
        }
        return FormationCorePlateBlockEntity.clampFlagEffectRadius(max + 28);
    }

    private static int horizontalDistance(int originX, int originZ, int x, int z) {
        int dx = x - originX;
        int dz = z - originZ;
        return (int)Math.ceil(Math.sqrt((double)dx * (double)dx + (double)dz * (double)dz));
    }

    private static void initializeSectPiece(WorldGenLevel level, ServerLevel server, long seed, BlockPos center, BoundingBox bounds, SectPlan plan, PlacedPiece piece, SectGeneration sect, int baseY, Set<Long> allBuildingFootprint, Set<Long> roadFootprint, List<StreetlightCandidate> streetlightCandidates) {
        int worldX = center.getX() + piece.localX();
        int worldZ = center.getZ() + piece.localZ();
        sect.data().registerBuilding(sect.id(), SectSettlementFeature.pieceType(piece.piece()), new BlockPos(worldX, baseY, worldZ), SectSettlementFeature.placedSizeX(piece), SectSettlementFeature.placedSizeZ(piece));
        if (piece.piece().equals(PROTECTION_ARRAY)) {
            SectSettlementFeature.initializeProtectionArray(level, server, seed, center, plan, allBuildingFootprint, roadFootprint, sect, worldX, baseY, worldZ, bounds, piece, streetlightCandidates);
        }
        SectSettlementFeature.spawnPieceMembers(level, server, seed, sect, worldX, baseY, worldZ, bounds, plan, piece);
    }

    private static String pieceType(PieceSpec piece) {
        String path = piece.id().getPath();
        int slash = path.lastIndexOf(47);
        return slash >= 0 ? path.substring(slash + 1) : path;
    }

    private static void initializeProtectionArray(WorldGenLevel level, ServerLevel server, long seed, BlockPos center, SectPlan plan, Set<Long> allBuildingFootprint, Set<Long> roadFootprint, SectGeneration sect, int worldX, int baseY, int worldZ, BoundingBox bounds, PlacedPiece piece, List<StreetlightCandidate> streetlightCandidates) {
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        int minX = Math.max(worldX, bounds.minX());
        int maxX = Math.min(worldX + SectSettlementFeature.placedSizeX(piece) - 1, bounds.maxX());
        int minZ = Math.max(worldZ, bounds.minZ());
        int maxZ = Math.min(worldZ + SectSettlementFeature.placedSizeZ(piece) - 1, bounds.maxZ());
        int minY = Math.max(level.getMinBuildHeight(), SectSettlementFeature.templateBaseY(baseY, piece.piece()));
        int maxY = Math.min(level.getMaxBuildHeight() - 1, minY + piece.piece().sizeY() + 3);
        for (int x = minX; x <= maxX; ++x) {
            for (int z = minZ; z <= maxZ; ++z) {
                for (int y = minY; y <= maxY; ++y) {
                    pos.set(x, y, z);
                    BlockState state = level.getBlockState((BlockPos)pos);
                    if (state.is((Block)ModBlocks.SUPREME_SPIRIT_VEIN_CORE.get())) {
                        SectSettlementFeature.replaceArrayCore(level, sect, pos.east());
                        continue;
                    }
                    if (!(state.getBlock() instanceof FormationCorePlateBlock)) continue;
                    BlockPos corePos = pos.east();
                    int arrayRadius = SectSettlementFeature.plannedGeneratedArrayRadius(plan, seed, center, allBuildingFootprint, roadFootprint, sect.data().arrayFlagPositions(sect.id()), streetlightCandidates);
                    sect.data().registerCore(sect.id(), corePos, arrayRadius);
                    sect.data().backfillLoadedNpcCoreTokens(server, sect.id());
                }
            }
        }
        SectSettlementFeature.tryConfigureStoredArrayCore(server, seed, center, plan, allBuildingFootprint, roadFootprint, sect, streetlightCandidates);
    }

    private static void replaceArrayCore(WorldGenLevel level, SectGeneration sect, BlockPos pos) {
        SectSavedData.ArrayReplacement replacement = sect.data().claimArrayReplacement(sect.id(), pos);
        if (replacement == null) {
            return;
        }
        BlockState state = switch (replacement) {
            case IMMORTAL_SPIRIT_CORE, IMMORTAL_SPIRIT_CORE_REJUVENATION_SLOT -> ((Block)ModBlocks.IMMORTAL_SPIRIT_VEIN_CORE.get()).defaultBlockState();
            case IMMORTAL_SECT_PROTECTION_FLAG -> ((Block)ModBlocks.IMMORTAL_SECT_PROTECTION_FLAG.get()).defaultBlockState();
            case IMMORTAL_MAZE_FLAG -> ((Block)ModBlocks.IMMORTAL_MAZE_FLAG.get()).defaultBlockState();
            case IMMORTAL_REJUVENATION_FLAG -> ((Block)ModBlocks.IMMORTAL_REJUVENATION_FLAG.get()).defaultBlockState();
        };
        if (replacement.isSpiritCore() && level.getBlockEntity(pos) instanceof SpiritVeinCoreBlockEntity) {
            level.setBlock(pos, AIR, 2);
        }
        SectSettlementFeature.set(level, pos, state);
    }

    private static void tryConfigureStoredArrayCore(ServerLevel server, long seed, BlockPos center, SectPlan plan, Set<Long> allBuildingFootprint, Set<Long> roadFootprint, SectGeneration sect, List<StreetlightCandidate> streetlightCandidates) {
        BlockPos corePos = sect.data().corePos(sect.id());
        if (corePos == null) {
            return;
        }
        List<BlockPos> flags = sect.data().arrayFlagPositions(sect.id());
        if (flags.size() < GENERATED_ARRAY_FLAG_COUNT) {
            return;
        }
        BlockEntity be = server.getBlockEntity(corePos);
        if (!(be instanceof FormationCorePlateBlockEntity)) {
            return;
        }
        FormationCorePlateBlockEntity core = (FormationCorePlateBlockEntity)be;
        int arrayRadius = SectSettlementFeature.plannedGeneratedArrayRadius(plan, seed, center, allBuildingFootprint, roadFootprint, flags, streetlightCandidates);
        sect.data().registerCore(sect.id(), corePos, arrayRadius);
        sect.data().configureGeneratedArrayCoreIfReady(server, core);
    }

    private static void spawnPieceMembers(WorldGenLevel level, ServerLevel server, long seed, SectGeneration sect, int worldX, int baseY, int worldZ, BoundingBox bounds, SectPlan plan, PlacedPiece piece) {
        RandomSource random = RandomSource.create((long)SectSettlementFeature.seedFor(seed, piece.localX(), piece.localZ(), -1071817017));
        if (piece.piece().equals(GATE)) {
            int count = 1 + random.nextInt(2);
            for (int i = 0; i < count; ++i) {
                SectSettlementFeature.spawnMember(level, server, seed, sect, piece, bounds, worldX, baseY, worldZ, SectRole.GUARD_DISCIPLE, "gate_guard_" + i, i - count / 2, 1, true, true);
            }
            return;
        }
        if (piece.piece().equals(MAIN_HALL)) {
            if (SectSettlementFeature.isPrimaryMainHall(plan, piece)) {
                SectSettlementFeature.spawnMember(level, server, seed, sect, piece, bounds, worldX, baseY, worldZ, SectRole.MASTER, "master", 0, 4, false, true);
            }
            int elders = 3 + random.nextInt(4);
            for (int i = 0; i < elders; ++i) {
                SectSettlementFeature.spawnMember(level, server, seed, sect, piece, bounds, worldX, baseY, worldZ, SectRole.ELDER, "elder_" + i, i % 3 - 1, 7 + i / 3, false, true);
            }
            return;
        }
        if (piece.piece().equals(DISCIPLE_RESIDENCE)) {
            SectRole role = random.nextBoolean() ? SectRole.INNER_DISCIPLE : SectRole.OUTER_DISCIPLE;
            SectSettlementFeature.spawnMember(level, server, seed, sect, piece, bounds, worldX, baseY, worldZ, role, "disciple", 0, 2, false, true);
            return;
        }
        if (piece.piece().equals(SERVANTS_RESIDENCE)) {
            SectSettlementFeature.spawnMember(level, server, seed, sect, piece, bounds, worldX, baseY, worldZ, SectRole.SERVANT, "servant", 0, 2, false, true);
            return;
        }
        if (piece.piece().equals(PROTECTION_ARRAY) || piece.piece().equals(PAVILION)) {
            SectRole role = random.nextBoolean() ? SectRole.INNER_DISCIPLE : SectRole.OUTER_DISCIPLE;
            SectSettlementFeature.spawnMember(level, server, seed, sect, piece, bounds, worldX, baseY, worldZ, role, "stationed_disciple", 0, 0, false, true);
            return;
        }
        if (piece.piece().equals(ANCESTOR_CAVE)) {
            SectSettlementFeature.spawnMember(level, server, seed, sect, piece, bounds, worldX, baseY, worldZ, SectRole.ANCESTOR, "ancestor", 0, SectSettlementFeature.placedSizeZ(piece) / 2, false, true);
        }
    }

    private static boolean isPrimaryMainHall(SectPlan plan, PlacedPiece piece) {
        if (plan == null || piece == null || !piece.piece().equals(MAIN_HALL)) {
            return false;
        }
        return piece.equals(SectSettlementFeature.primaryMainHall(plan));
    }

    @Nullable
    private static PlacedPiece primaryMainHall(SectPlan plan) {
        PlacedPiece primary = null;
        for (PlacedPiece candidate : plan.pieces()) {
            if (!candidate.piece().equals(MAIN_HALL) || primary != null && candidate.localZ() >= primary.localZ() && (candidate.localZ() != primary.localZ() || candidate.localX() >= primary.localX())) continue;
            primary = candidate;
        }
        return primary;
    }

    private static void spawnMember(WorldGenLevel level, ServerLevel server, long seed, SectGeneration sect, PlacedPiece piece, BoundingBox bounds, int worldX, int baseY, int worldZ, SectRole role, String keyPart, int localOffsetX, int localOffsetZ, boolean addTemporaryTokens, boolean deferSpawn) {
        int z;
        int x = worldX + SectSettlementFeature.placedSizeX(piece) / 2 + localOffsetX;
        if (!SectSettlementFeature.pointInsideChunk(x, z = worldZ + Math.max(1, Math.min(SectSettlementFeature.placedSizeZ(piece) - 2, localOffsetZ)), bounds)) {
            return;
        }
        String key = SectSettlementFeature.pieceType(piece.piece()) + ":" + piece.localX() + "," + piece.localZ() + ":" + keyPart;
        BlockPos spawnPos = new BlockPos(x, baseY + piece.piece().templateYOffset() + 1, z);
        BlockPos homePos = new BlockPos(worldX + SectSettlementFeature.placedSizeX(piece) / 2, baseY + 1, worldZ + SectSettlementFeature.placedSizeZ(piece) / 2);
        DeferredNpcSpawnTask task = DeferredNpcSpawnTask.piece(server, key, SectSettlementFeature.seedFor(seed, x, z, role.rank()), sect, role, spawnPos, homePos, addTemporaryTokens, piece, bounds, worldX, baseY, worldZ);
        if (deferSpawn) {
            SectSettlementFeature.queueDeferredNpcSpawn(task);
            return;
        }
        task.spawnNow();
    }

    @Nullable
    private static BlockPos findRoutineTargetInPiece(SectGeneration sect, List<BlockPos> candidates, boolean cushion) {
        for (BlockPos candidate : candidates) {
            if (sect.data().isRoutineTargetClaimed(sect.id(), candidate, cushion)) continue;
            return candidate;
        }
        return null;
    }

    private static List<BlockPos> collectRoutineTargetsInPiece(LevelAccessor level, PlacedPiece piece, BoundingBox bounds, int worldX, int baseY, int worldZ, boolean cushion) {
        ArrayList<BlockPos> candidates = new ArrayList<BlockPos>();
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        int minX = Math.max(worldX, bounds.minX());
        int maxX = Math.min(worldX + SectSettlementFeature.placedSizeX(piece) - 1, bounds.maxX());
        int minZ = Math.max(worldZ, bounds.minZ());
        int maxZ = Math.min(worldZ + SectSettlementFeature.placedSizeZ(piece) - 1, bounds.maxZ());
        int minY = Math.max(level.getMinBuildHeight(), SectSettlementFeature.templateBaseY(baseY, piece.piece()));
        int maxY = Math.min(level.getMaxBuildHeight() - 1, minY + piece.piece().sizeY() - 1);
        for (int y = minY; y <= maxY; ++y) {
            for (int x = minX; x <= maxX; ++x) {
                for (int z = minZ; z <= maxZ; ++z) {
                    pos.set(x, y, z);
                    BlockState state = level.getBlockState((BlockPos)pos);
                    if (!(cushion ? state.getBlock() instanceof CushionBlock : SectSettlementFeature.isUsableBedState(state))) continue;
                    candidates.add(pos.east());
                }
            }
        }
        return candidates;
    }

    private static void fillSectContainers(WorldGenLevel level, long seed, PlacedPiece piece, BoundingBox bounds, int worldX, int baseY, int worldZ) {
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        int minX = Math.max(worldX, bounds.minX());
        int maxX = Math.min(worldX + SectSettlementFeature.placedSizeX(piece) - 1, bounds.maxX());
        int minZ = Math.max(worldZ, bounds.minZ());
        int maxZ = Math.min(worldZ + SectSettlementFeature.placedSizeZ(piece) - 1, bounds.maxZ());
        int minY = Math.max(level.getMinBuildHeight(), SectSettlementFeature.templateBaseY(baseY, piece.piece()));
        int maxY = Math.min(level.getMaxBuildHeight() - 1, minY + piece.piece().sizeY() - 1);
        for (int y = minY; y <= maxY; ++y) {
            for (int x = minX; x <= maxX; ++x) {
                for (int z = minZ; z <= maxZ; ++z) {
                    BlockEntity blockEntity;
                    pos.set(x, y, z);
                    if (!SectSettlementFeature.isSectLootContainer(level.getBlockState((BlockPos)pos)) || !((blockEntity = level.getBlockEntity((BlockPos)pos)) instanceof RandomizableContainerBlockEntity)) continue;
                    RandomizableContainerBlockEntity container = (RandomizableContainerBlockEntity)blockEntity;
                    RandomSource random = RandomSource.create((long)SectSettlementFeature.seedFor(seed, x, z, 0x5EC1007 ^ y));
                    CultivationChestLoot.fillSectContainer(container, random);
                }
            }
        }
    }

    private static boolean isSectLootContainer(BlockState state) {
        return state.is(Blocks.CHEST) || state.is(Blocks.TRAPPED_CHEST) || state.is(Blocks.BARREL);
    }

    private static int compareRoutineTarget(BlockPos a, BlockPos b, BlockPos preferred) {
        int cmp = Integer.compare(SectSettlementFeature.manhattan(a, preferred), SectSettlementFeature.manhattan(b, preferred));
        if (cmp != 0) {
            return cmp;
        }
        cmp = Integer.compare(a.getY(), b.getY());
        if (cmp != 0) {
            return cmp;
        }
        cmp = Integer.compare(a.getX(), b.getX());
        if (cmp != 0) {
            return cmp;
        }
        return Integer.compare(a.getZ(), b.getZ());
    }

    private static int manhattan(BlockPos a, BlockPos b) {
        return Math.abs(a.getX() - b.getX()) + Math.abs(a.getY() - b.getY()) + Math.abs(a.getZ() - b.getZ());
    }

    private static boolean isUsableBedState(BlockState state) {
        return state.is(BlockTags.BEDS) && (!state.hasProperty((Property)BedBlock.PART) || state.getValue((Property)BedBlock.PART) == BedPart.FOOT);
    }

    private static boolean spawnOuterGuardDisciples(WorldGenLevel level, ServerLevel server, long seed, BlockPos center, BoundingBox bounds, SectPlan plan, SectGeneration sect, Map<PlacedPiece, Integer> pieceBaseYs) {
        RandomSource random = RandomSource.create((long)SectSettlementFeature.seedFor(seed, center.getX(), center.getZ(), 6322813));
        int count = 1 + random.nextInt(5);
        boolean spawned = false;
        int distance = Math.max(sect.radius() + 12, SectSettlementFeature.plannedOuterGuardDistance(sect.radius(), sect.hasArray()));
        for (int i = 0; i < count; ++i) {
            int z;
            double angle = Math.PI * 2 * (double)i / (double)count + random.nextDouble() * 0.5;
            int x = center.getX() + Mth.floor((double)(Math.cos(angle) * (double)distance));
            if (!SectSettlementFeature.pointInsideChunk(x, z = center.getZ() + Mth.floor((double)(Math.sin(angle) * (double)distance)), bounds)) continue;
            String key = "outer_guard:" + i;
            int y = SectSettlementFeature.height(level, x, z);
            BlockPos homePos = SectSettlementFeature.outerGuardHomePos(server, seed, center, plan, x, z, i, pieceBaseYs);
            DeferredNpcSpawnTask task = DeferredNpcSpawnTask.simple(server, key, SectSettlementFeature.seedFor(seed, x, z, i), sect, SectRole.GUARD_DISCIPLE, new BlockPos(x, y, z), homePos, null, null, true, null);
            spawned |= SectSettlementFeature.queueDeferredNpcSpawn(task);
        }
        return spawned;
    }

    private static boolean outerGuardDisciplesIntersectChunk(long seed, BlockPos center, BoundingBox bounds, int sectRadius, boolean hasArray) {
        RandomSource random = RandomSource.create((long)SectSettlementFeature.seedFor(seed, center.getX(), center.getZ(), 6322813));
        int count = 1 + random.nextInt(5);
        int distance = Math.max(sectRadius + 12, SectSettlementFeature.plannedOuterGuardDistance(sectRadius, hasArray));
        for (int i = 0; i < count; ++i) {
            int z;
            double angle = Math.PI * 2 * (double)i / (double)count + random.nextDouble() * 0.5;
            int x = center.getX() + Mth.floor((double)(Math.cos(angle) * (double)distance));
            if (!SectSettlementFeature.pointInsideChunk(x, z = center.getZ() + Mth.floor((double)(Math.sin(angle) * (double)distance)), bounds)) continue;
            return true;
        }
        return false;
    }

    private static int plannedOuterGuardDistance(int sectRadius, boolean hasArray) {
        return hasArray ? sectRadius + 16 : 92;
    }

    private static boolean spawnReceptionGuardDisciples(WorldGenLevel level, ServerLevel server, long seed, BlockPos center, BoundingBox bounds, SectPlan plan, SectGeneration sect) {
        if (!sect.hasArray()) {
            return false;
        }
        boolean spawned = false;
        for (ReceptionGuardPost post : SectSettlementFeature.plannedReceptionGuardPosts(center, plan, sect.radius())) {
            if (!SectSettlementFeature.pointInsideChunk(post.x(), post.z(), bounds)) continue;
            String key = "reception_guard:" + post.index();
            int y = SectSettlementFeature.height(level, post.x(), post.z());
            BlockPos anchor = new BlockPos(post.x(), y, post.z());
            DeferredNpcSpawnTask task = DeferredNpcSpawnTask.simple(server, key, SectSettlementFeature.seedFor(seed, post.x(), post.z(), post.index()), sect, SectRole.GUARD_DISCIPLE, anchor, anchor, null, null, true, anchor);
            spawned |= SectSettlementFeature.queueDeferredNpcSpawn(task);
        }
        return spawned;
    }

    private static boolean receptionGuardDisciplesIntersectChunk(BlockPos center, BoundingBox bounds, SectPlan plan, int sectRadius, boolean hasArray) {
        if (!hasArray) {
            return false;
        }
        for (ReceptionGuardPost post : SectSettlementFeature.plannedReceptionGuardPosts(center, plan, sectRadius)) {
            if (!SectSettlementFeature.pointInsideChunk(post.x(), post.z(), bounds)) continue;
            return true;
        }
        return false;
    }

    static List<BlockPos> plannedReceptionGuardPositionsForTest(long seed, BlockPos center, int sectRadius) {
        SectPlan plan = SectSettlementFeature.createPlan(RandomSource.create((long)SectSettlementFeature.seedFor(seed, 0, 0, 6211354)));
        ArrayList<BlockPos> positions = new ArrayList<BlockPos>();
        for (ReceptionGuardPost post : SectSettlementFeature.plannedReceptionGuardPosts(center, plan, sectRadius)) {
            positions.add(new BlockPos(post.x(), center.getY(), post.z()));
        }
        return positions;
    }

    private static List<ReceptionGuardPost> plannedReceptionGuardPosts(BlockPos center, SectPlan plan, int sectRadius) {
        PlacedPiece gate = SectSettlementFeature.primaryGate(plan);
        Direction facing = Direction.NORTH;
        int gateX = center.getX();
        int gateZ = center.getZ();
        if (gate != null) {
            GatePass pass = SectSettlementFeature.gatePass(gate.rotation());
            gateX = center.getX() + gate.localX() + pass.x();
            gateZ = center.getZ() + gate.localZ() + pass.z();
            facing = SectSettlementFeature.gateFacing(gate.rotation());
        }
        ArrayList<ReceptionGuardPost> posts = new ArrayList<ReceptionGuardPost>(2);
        for (int i = 0; i < 2; ++i) {
            Direction side = SectSettlementFeature.perpendicular(facing, i == 0);
            int baseX = gateX + side.getStepX() * 3;
            int baseZ = gateZ + side.getStepZ() * 3;
            int x = baseX + facing.getStepX() * 8;
            int z = baseZ + facing.getStepZ() * 8;
            int requiredRadius = sectRadius + 10;
            for (int distance = 8; distance <= 256; ++distance) {
                int candidateX = baseX + facing.getStepX() * distance;
                int candidateZ = baseZ + facing.getStepZ() * distance;
                x = candidateX;
                z = candidateZ;
                if (SectSettlementFeature.outsideHorizontalRadius(center, candidateX, candidateZ, requiredRadius)) break;
            }
            if (!SectSettlementFeature.outsideHorizontalRadius(center, x, z, requiredRadius)) {
                x = center.getX() + facing.getStepX() * requiredRadius + side.getStepX() * 3;
                z = center.getZ() + facing.getStepZ() * requiredRadius + side.getStepZ() * 3;
            }
            posts.add(new ReceptionGuardPost(i, x, z));
        }
        return posts;
    }

    private static boolean outsideHorizontalRadius(BlockPos center, int x, int z, int radius) {
        long r;
        long dz;
        long dx = (long)x - (long)center.getX();
        return dx * dx + (dz = (long)z - (long)center.getZ()) * dz >= (r = (long)Math.max(1, radius)) * r;
    }

    @Nullable
    private static PlacedPiece primaryGate(SectPlan plan) {
        PlacedPiece best = null;
        for (PlacedPiece piece : plan.pieces()) {
            if (!piece.piece().equals(GATE) || best != null && piece.localZ() >= best.localZ() && (piece.localZ() != best.localZ() || piece.localX() >= best.localX())) continue;
            best = piece;
        }
        return best;
    }

    private static BlockPos outerGuardHomePos(ServerLevel server, long seed, BlockPos center, SectPlan plan, int guardX, int guardZ, int guardIndex, Map<PlacedPiece, Integer> pieceBaseYs) {
        ArrayList<PlacedPiece> homes = new ArrayList<PlacedPiece>();
        for (PlacedPiece piece : plan.pieces()) {
            if (!piece.piece().equals(DISCIPLE_RESIDENCE) && !piece.piece().equals(SERVANTS_RESIDENCE)) continue;
            homes.add(piece);
        }
        if (homes.isEmpty()) {
            return center;
        }
        homes.sort((a, b) -> {
            int cmp = Integer.compare(SectSettlementFeature.outerGuardHomeDistance(center, a, guardX, guardZ), SectSettlementFeature.outerGuardHomeDistance(center, b, guardX, guardZ));
            if (cmp != 0) {
                return cmp;
            }
            cmp = Integer.compare(a.localZ(), b.localZ());
            if (cmp != 0) {
                return cmp;
            }
            return Integer.compare(a.localX(), b.localX());
        });
        PlacedPiece home = (PlacedPiece)homes.get(Math.floorMod(guardIndex, homes.size()));
        int worldX = center.getX() + home.localX();
        int worldZ = center.getZ() + home.localZ();
        int baseY = SectSettlementFeature.cachedPieceBaseY(pieceBaseYs, server, seed, center, plan, home);
        return new BlockPos(worldX + SectSettlementFeature.placedSizeX(home) / 2, baseY + 1, worldZ + SectSettlementFeature.placedSizeZ(home) / 2);
    }

    private static int outerGuardHomeDistance(BlockPos center, PlacedPiece home, int guardX, int guardZ) {
        int homeX = center.getX() + home.localX() + SectSettlementFeature.placedSizeX(home) / 2;
        int homeZ = center.getZ() + home.localZ() + SectSettlementFeature.placedSizeZ(home) / 2;
        return Math.abs(homeX - guardX) + Math.abs(homeZ - guardZ);
    }

    @Nullable
    private static WanderingCultivatorEntity spawnSectNpc(ServerLevel server, long seed, SectGeneration sect, SectRole role, BlockPos pos, BlockPos homePos, @Nullable BlockPos bedPos, @Nullable BlockPos cushionPos, boolean addTemporaryTokens) {
        WanderingCultivatorEntity npc = (WanderingCultivatorEntity)((EntityType)ModEntities.WANDERING_CULTIVATOR.get()).create((Level)server);
        if (npc == null) {
            return null;
        }
        RandomSource random = RandomSource.create((long)seed);
        npc.moveTo((double)pos.getX() + 0.5, pos.getY(), (double)pos.getZ() + 0.5, random.nextFloat() * 360.0f, 0.0f);
        CompoundTag spawnTag = new CompoundTag();
        Realm memberRealm = SectSettlementFeature.sectMemberRealm(sect, role);
        spawnTag.putString("forcedRealmId", memberRealm.id());
        int looseImmortalTribulations = SectSettlementFeature.sectMemberLooseImmortalTribulations(sect, role, memberRealm);
        if (looseImmortalTribulations > 0) {
            spawnTag.putInt("forcedLooseImmortalTribulations", looseImmortalTribulations);
        }
        npc.finalizeSpawn((ServerLevelAccessor)server, server.getCurrentDifficultyAt(npc.blockPosition()), MobSpawnType.STRUCTURE, null, spawnTag);
        BlockPos corePos = sect.data().corePos(sect.id());
        npc.assignSectMembership(sect.id(), sect.name(), role, homePos, bedPos, cushionPos, corePos, corePos != null);
        if (addTemporaryTokens && corePos != null) {
            npc.ensureSectCoreLink(corePos, true);
        }
        npc.setPersistenceRequired();
        server.addFreshEntity((Entity)npc);
        sect.data().registerNpcMember(npc, sect.id(), role, homePos, bedPos, cushionPos, corePos);
        sect.data().ensureBasicTask(sect.id(), npc);
        return npc;
    }

    private static boolean queueDeferredNpcSpawn(DeferredNpcSpawnTask task) {
        return DeferredSectNpcSpawner.queue(task);
    }

    private static Realm sectMemberRealm(SectGeneration sect, SectRole role) {
        long sectSeed = (long)sect.id().hashCode() << 21 ^ 0x5EC73EA1B107L;
        RandomSource random = RandomSource.create((long)sectSeed);
        int masterOrd = SectSettlementFeature.rollSectMasterRealmOrdinal(random, SectSettlementFeature.sectPowerScore(sect));
        if (role == SectRole.ANCESTOR) {
            return Realm.values()[SectSettlementFeature.rollSectAncestorRealmOrdinal(random, masterOrd, SectSettlementFeature.sectPowerScore(sect))];
        }
        int offset = Math.min(5, Math.max(0, role.rank() - SectRole.MASTER.rank()));
        int ord = Mth.clamp((int)(masterOrd - offset), (int)Realm.QI_REFINING.ordinal(), (int)masterOrd);
        return Realm.values()[ord];
    }

    private static int rollSectAncestorRealmOrdinal(RandomSource random, int masterOrd, int powerScore) {
        int ord = Mth.clamp((int)(masterOrd + 1 + random.nextInt(2)), (int)Realm.FOUNDATION_BUILDING.ordinal(), (int)Realm.TRIBULATION_TRANSCENDENCE.ordinal());
        float immortalChance = switch (Mth.clamp((int)powerScore, (int)0, (int)4)) {
            case 0 -> 0.02f;
            case 1 -> 0.05f;
            case 2 -> 0.12f;
            case 3 -> 0.24f;
            default -> 0.38f;
        };
        if (masterOrd >= Realm.TRIBULATION_TRANSCENDENCE.ordinal() || random.nextFloat() < immortalChance) {
            return random.nextFloat() < SectSettlementFeature.looseImmortalAncestorChance(powerScore) ? Realm.LOOSE_IMMORTAL.ordinal() : Realm.TRUE_IMMORTAL.ordinal();
        }
        return ord;
    }

    private static float looseImmortalAncestorChance(int powerScore) {
        return switch (Mth.clamp((int)powerScore, (int)0, (int)4)) {
            case 0 -> 0.1f;
            case 1 -> 0.16f;
            case 2 -> 0.24f;
            case 3 -> 0.34f;
            default -> 0.46f;
        };
    }

    private static int sectMemberLooseImmortalTribulations(SectGeneration sect, SectRole role, Realm realm) {
        if (role != SectRole.ANCESTOR || realm != Realm.LOOSE_IMMORTAL) {
            return 0;
        }
        int powerScore = Mth.clamp((int)SectSettlementFeature.sectPowerScore(sect), (int)0, (int)4);
        int min = switch (powerScore) {
            case 0, 1 -> 1;
            case 2 -> 2;
            case 3 -> 3;
            default -> 5;
        };
        int max = switch (powerScore) {
            case 0 -> 3;
            case 1 -> 4;
            case 2 -> 6;
            case 3 -> 8;
            default -> 9;
        };
        RandomSource random = RandomSource.create((long)((long)sect.id().hashCode() << 17 ^ 0xAACE57B107L));
        int span = Math.max(1, max - min + 1);
        int roll = powerScore >= 3 ? Math.max(random.nextInt(span), random.nextInt(span)) : random.nextInt(span);
        return Mth.clamp((int)(min + roll), (int)1, (int)9);
    }

    private static int sectPowerScore(SectGeneration sect) {
        int score;
        int n = score = sect.hasArray() ? 2 : 0;
        if (sect.radius() >= 130) {
            ++score;
        }
        if (sect.radius() >= 175) {
            ++score;
        }
        return score;
    }

    private static int rollSectMasterRealmOrdinal(RandomSource random, int powerScore) {
        int[] centerByPower = new int[]{Realm.GOLDEN_CORE.ordinal(), Realm.NASCENT_SOUL.ordinal(), Realm.SOUL_FORMATION.ordinal(), Realm.VOID_REFINING.ordinal(), Realm.MAHAYANA.ordinal()};
        int center = centerByPower[Mth.clamp((int)powerScore, (int)0, (int)(centerByPower.length - 1))];
        int jitter = random.nextInt(3) - 1;
        return Mth.clamp((int)(center + jitter), (int)Realm.FOUNDATION_BUILDING.ordinal(), (int)Realm.TRIBULATION_TRANSCENDENCE.ordinal());
    }

    private static int medianTerrainY(ServerLevel server, int x, int z, int sx, int sz) {
        int[] heights = new int[]{SectSettlementFeature.generatedPlacementY(server, x, z), SectSettlementFeature.generatedPlacementY(server, x + sx - 1, z), SectSettlementFeature.generatedPlacementY(server, x, z + sz - 1), SectSettlementFeature.generatedPlacementY(server, x + sx - 1, z + sz - 1), SectSettlementFeature.generatedPlacementY(server, x + sx / 2, z + sz / 2)};
        Arrays.sort(heights);
        return heights[2];
    }

    private static int pieceBaseY(ServerLevel server, long seed, BlockPos center, @Nullable SectPlan plan, PlacedPiece piece, int worldX, int worldZ) {
        int hallBaseY;
        PlacedPiece mainHall;
        if (!SectSettlementFeature.isAncestorCave(piece.piece())) {
            return SectSettlementFeature.medianTerrainY(server, worldX, worldZ, SectSettlementFeature.placedSizeX(piece), SectSettlementFeature.placedSizeZ(piece));
        }
        PlacedPiece placedPiece = mainHall = plan == null ? null : SectSettlementFeature.primaryMainHall(plan);
        if (mainHall != null) {
            int hallX = center.getX() + mainHall.localX();
            int hallZ = center.getZ() + mainHall.localZ();
            hallBaseY = SectSettlementFeature.medianTerrainY(server, hallX, hallZ, SectSettlementFeature.placedSizeX(mainHall), SectSettlementFeature.placedSizeZ(mainHall));
        } else {
            hallBaseY = SectSettlementFeature.medianTerrainY(server, worldX, worldZ, MAIN_HALL.sizeX(), MAIN_HALL.sizeZ());
        }
        int gap = 1 + Math.floorMod(SectSettlementFeature.seedFor(seed, piece.localX(), piece.localZ(), 11193943), 2);
        int baseY = hallBaseY - ANCESTOR_CAVE.sizeY() - gap;
        return Mth.clamp((int)baseY, (int)(server.getMinBuildHeight() + 2), (int)(server.getMaxBuildHeight() - ANCESTOR_CAVE.sizeY() - 2));
    }

    private static boolean isAncestorCave(PieceSpec piece) {
        return piece.equals(ANCESTOR_CAVE);
    }

    private static void prepareFootprint(WorldGenLevel level, int x0, int baseY, int z0, PlacedPiece placed, BoundingBox bounds, long seed, Set<Long> foundationConnectorFootprint, Set<Long> allBuildingFootprint) {
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        int sizeX = SectSettlementFeature.placedSizeX(placed);
        int sizeZ = SectSettlementFeature.placedSizeZ(placed);
        int sizeY = placed.piece().sizeY() + placed.piece().templateYOffset();
        int minX = Math.max(x0, bounds.minX());
        int maxX = Math.min(x0 + sizeX - 1, bounds.maxX());
        int minZ = Math.max(z0, bounds.minZ());
        int maxZ = Math.min(z0 + sizeZ - 1, bounds.maxZ());
        for (int x = minX; x <= maxX; ++x) {
            for (int z = minZ; z <= maxZ; ++z) {
                int bottom;
                int naturalY = SectSettlementFeature.height(level, x, z);
                boolean waterSurface = SectSettlementFeature.isFluidSurface(level, pos, x, naturalY - 1, z);
                int supportDepth = waterSurface ? 14 : 6;
                for (int y = bottom = Math.max(level.getMinBuildHeight(), Math.min(naturalY, baseY) - supportDepth); y < baseY; ++y) {
                    SectSettlementFeature.set(level, (BlockPos)pos.set(x, y, z), SectSettlementFeature.chooseFoundation(seed, x, z));
                }
                SectSettlementFeature.set(level, (BlockPos)pos.set(x, baseY, z), SectSettlementFeature.chooseFoundation(seed, x, z));
                int clearTop = Math.min(level.getMaxBuildHeight() - 1, baseY + sizeY + 32);
                for (int y = baseY + 1; y <= clearTop; ++y) {
                    SectSettlementFeature.set(level, (BlockPos)pos.set(x, y, z), AIR);
                }
            }
        }
        SectSettlementFeature.placeFoundationEdgeStairs(level, x0, baseY, z0, sizeX, sizeZ, bounds, seed, foundationConnectorFootprint);
    }

    private static void placeFoundationEdgeStairs(WorldGenLevel level, int x0, int baseY, int z0, int sizeX, int sizeZ, BoundingBox bounds, long seed, Set<Long> roadFootprint) {
        for (int x = x0; x < x0 + sizeX; ++x) {
            SectSettlementFeature.placeFoundationEdgeStair(level, bounds, seed, roadFootprint, x, baseY, z0 - 1, Direction.SOUTH);
            SectSettlementFeature.placeFoundationEdgeStair(level, bounds, seed, roadFootprint, x, baseY, z0 + sizeZ, Direction.NORTH);
        }
        for (int z = z0; z < z0 + sizeZ; ++z) {
            SectSettlementFeature.placeFoundationEdgeStair(level, bounds, seed, roadFootprint, x0 - 1, baseY, z, Direction.EAST);
            SectSettlementFeature.placeFoundationEdgeStair(level, bounds, seed, roadFootprint, x0 + sizeX, baseY, z, Direction.WEST);
        }
        SectSettlementFeature.placeFoundationCorner(level, bounds, seed, roadFootprint, x0 - 1, baseY, z0 - 1, Direction.EAST, Direction.SOUTH);
        SectSettlementFeature.placeFoundationCorner(level, bounds, seed, roadFootprint, x0 + sizeX, baseY, z0 - 1, Direction.WEST, Direction.SOUTH);
        SectSettlementFeature.placeFoundationCorner(level, bounds, seed, roadFootprint, x0 - 1, baseY, z0 + sizeZ, Direction.EAST, Direction.NORTH);
        SectSettlementFeature.placeFoundationCorner(level, bounds, seed, roadFootprint, x0 + sizeX, baseY, z0 + sizeZ, Direction.WEST, Direction.NORTH);
    }

    private static void placeFoundationEdgeStair(WorldGenLevel level, BoundingBox bounds, long seed, Set<Long> roadFootprint, int x, int y, int z, Direction facing) {
        int bottom;
        if (x < bounds.minX() || x > bounds.maxX() || z < bounds.minZ() || z > bounds.maxZ()) {
            return;
        }
        if (SectSettlementFeature.foundationEdgeTouchesRoad(x, z, roadFootprint)) {
            return;
        }
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        int naturalY = SectSettlementFeature.height(level, x, z);
        boolean waterSurface = SectSettlementFeature.isFluidSurface(level, pos, x, naturalY - 1, z);
        int supportDepth = waterSurface ? 14 : 6;
        for (int fillY = bottom = Math.max(level.getMinBuildHeight(), Math.min(naturalY, y) - supportDepth); fillY < y; ++fillY) {
            SectSettlementFeature.set(level, (BlockPos)pos.set(x, fillY, z), SectSettlementFeature.chooseEdgeSupport(seed, x, z));
        }
        BlockState edge = SectSettlementFeature.foundationEdgeNeedsStair(naturalY, y) ? (BlockState)FOUNDATION_EDGE_STAIR.setValue((Property)StairBlock.FACING, (Comparable)facing) : FOUNDATION_EDGE_FLAT;
        SectSettlementFeature.set(level, (BlockPos)pos.set(x, y, z), edge);
        for (int clearY = y + 1; clearY <= y + 5 && clearY < level.getMaxBuildHeight(); ++clearY) {
            SectSettlementFeature.set(level, (BlockPos)pos.set(x, clearY, z), AIR);
        }
    }

    private static void placeFoundationCorner(WorldGenLevel level, BoundingBox bounds, long seed, Set<Long> roadFootprint, int x, int y, int z, Direction xEdgeFacing, Direction zEdgeFacing) {
        int bottom;
        if (x < bounds.minX() || x > bounds.maxX() || z < bounds.minZ() || z > bounds.maxZ()) {
            return;
        }
        if (SectSettlementFeature.foundationEdgeTouchesRoad(x, z, roadFootprint)) {
            return;
        }
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        int naturalY = SectSettlementFeature.height(level, x, z);
        boolean waterSurface = SectSettlementFeature.isFluidSurface(level, pos, x, naturalY - 1, z);
        int supportDepth = waterSurface ? 14 : 6;
        for (int fillY = bottom = Math.max(level.getMinBuildHeight(), Math.min(naturalY, y) - supportDepth); fillY < y; ++fillY) {
            SectSettlementFeature.set(level, (BlockPos)pos.set(x, fillY, z), SectSettlementFeature.chooseEdgeSupport(seed, x, z));
        }
        boolean xEdgeDrop = SectSettlementFeature.foundationEdgeNeedsStair(SectSettlementFeature.sampledSurfaceY(level, bounds, pos, x + xEdgeFacing.getStepX(), z + xEdgeFacing.getStepZ(), naturalY), y);
        boolean zEdgeDrop = SectSettlementFeature.foundationEdgeNeedsStair(SectSettlementFeature.sampledSurfaceY(level, bounds, pos, x + zEdgeFacing.getStepX(), z + zEdgeFacing.getStepZ(), naturalY), y);
        SectSettlementFeature.set(level, (BlockPos)pos.set(x, y, z), SectSettlementFeature.foundationCornerState(xEdgeFacing, zEdgeFacing, xEdgeDrop, zEdgeDrop));
        for (int clearY = y + 1; clearY <= y + 5 && clearY < level.getMaxBuildHeight(); ++clearY) {
            SectSettlementFeature.set(level, (BlockPos)pos.set(x, clearY, z), AIR);
        }
    }

    private static int sampledSurfaceY(WorldGenLevel level, BoundingBox bounds, BlockPos.MutableBlockPos pos, int x, int z, int fallbackY) {
        if (x < bounds.minX() || x > bounds.maxX() || z < bounds.minZ() || z > bounds.maxZ()) {
            return fallbackY;
        }
        int naturalY = SectSettlementFeature.height(level, x, z);
        return SectSettlementFeature.isFluidSurface(level, pos, x, naturalY - 1, z) ? naturalY - 1 : naturalY;
    }

    private static BlockState foundationCornerState(Direction xEdgeFacing, Direction zEdgeFacing, boolean xEdgeDrop, boolean zEdgeDrop) {
        if (xEdgeDrop && zEdgeDrop) {
            return (BlockState)((BlockState)FOUNDATION_EDGE_STAIR.setValue((Property)StairBlock.FACING, (Comparable)xEdgeFacing)).setValue((Property)StairBlock.SHAPE, (Comparable)SectSettlementFeature.cornerStairShape(xEdgeFacing, zEdgeFacing));
        }
        if (xEdgeDrop) {
            return (BlockState)FOUNDATION_EDGE_STAIR.setValue((Property)StairBlock.FACING, (Comparable)xEdgeFacing);
        }
        if (zEdgeDrop) {
            return (BlockState)FOUNDATION_EDGE_STAIR.setValue((Property)StairBlock.FACING, (Comparable)zEdgeFacing);
        }
        return FOUNDATION_EDGE_FLAT;
    }

    private static StairsShape cornerStairShape(Direction facing, Direction side) {
        return side == facing.getClockWise() ? StairsShape.OUTER_RIGHT : StairsShape.OUTER_LEFT;
    }

    private static boolean foundationEdgeNeedsStair(int naturalY, int baseY) {
        return naturalY < baseY;
    }

    private static boolean foundationEdgeTouchesRoad(int x, int z, Set<Long> roadFootprint) {
        return roadFootprint.contains(SectSettlementFeature.packXZ(x, z));
    }

    private static void removeTemplateTerrainBlocks(WorldGenLevel level, int x0, int baseY, int z0, PlacedPiece placed, BoundingBox bounds, long seed) {
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        int sizeX = SectSettlementFeature.placedSizeX(placed);
        int sizeZ = SectSettlementFeature.placedSizeZ(placed);
        int minX = Math.max(x0, bounds.minX());
        int maxX = Math.min(x0 + sizeX - 1, bounds.maxX());
        int minZ = Math.max(z0, bounds.minZ());
        int maxZ = Math.min(z0 + sizeZ - 1, bounds.maxZ());
        int templateY = SectSettlementFeature.templateBaseY(baseY, placed.piece());
        int maxY = Math.min(templateY + placed.piece().sizeY() - 1, bounds.maxY());
        for (int x = minX; x <= maxX; ++x) {
            for (int z = minZ; z <= maxZ; ++z) {
                for (int y = baseY; y <= maxY; ++y) {
                    BlockPos.MutableBlockPos at = pos.set(x, y, z);
                    BlockState state = level.getBlockState((BlockPos)at);
                    if (!SectSettlementFeature.isTemplateTerrainBlock(state)) continue;
                    SectSettlementFeature.set(level, (BlockPos)at, y <= baseY ? SectSettlementFeature.chooseFoundation(seed, x, z) : AIR);
                }
            }
        }
    }

    private static void repairFoundationSurface(WorldGenLevel level, int x0, int baseY, int z0, PlacedPiece placed, BoundingBox bounds, long seed) {
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        int sizeX = SectSettlementFeature.placedSizeX(placed);
        int sizeZ = SectSettlementFeature.placedSizeZ(placed);
        int minX = Math.max(x0, bounds.minX());
        int maxX = Math.min(x0 + sizeX - 1, bounds.maxX());
        int minZ = Math.max(z0, bounds.minZ());
        int maxZ = Math.min(z0 + sizeZ - 1, bounds.maxZ());
        for (int x = minX; x <= maxX; ++x) {
            for (int z = minZ; z <= maxZ; ++z) {
                BlockPos.MutableBlockPos at = pos.set(x, baseY, z);
                BlockState state = level.getBlockState((BlockPos)at);
                if (!state.isAir() && state.getFluidState().isEmpty() && !SectSettlementFeature.shouldClearVegetation(state) && !SectSettlementFeature.isTemplateTerrainBlock(state)) continue;
                SectSettlementFeature.set(level, (BlockPos)at, SectSettlementFeature.chooseFoundation(seed, x, z));
            }
        }
    }

    private static void clearVegetationAroundPiece(WorldGenLevel level, int x0, int baseY, int z0, int sizeX, int sizeY, int sizeZ, BoundingBox bounds, Set<Long> allBuildingFootprint) {
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        HashSet<Long> protectedFootprint = new HashSet<Long>(allBuildingFootprint);
        SectSettlementFeature.markFootprint(protectedFootprint, x0, z0, sizeX, sizeZ);
        int minX = Math.max(x0 - 10, bounds.minX());
        int maxX = Math.min(x0 + sizeX + 10 - 1, bounds.maxX());
        int minZ = Math.max(z0 - 10, bounds.minZ());
        int maxZ = Math.min(z0 + sizeZ + 10 - 1, bounds.maxZ());
        int minY = Math.max(level.getMinBuildHeight(), baseY - 2);
        for (int x = minX; x <= maxX; ++x) {
            for (int z = minZ; z <= maxZ; ++z) {
                SurfaceColumn column = SectSettlementFeature.surfaceColumn(level, pos, x, z);
                int maxY = SectSettlementFeature.vegetationScanTop(level, baseY + sizeY, column.topY(), 32);
                int hardMaxY = SectSettlementFeature.vegetationHardScanTop(level, baseY + sizeY, 32);
                SectSettlementFeature.clearVegetationColumn(level, pos, x, z, minY, maxY, hardMaxY, bounds, protectedFootprint);
            }
        }
    }

    private static boolean clearSectGrounds(WorldGenLevel level, BlockPos center, int radius, BoundingBox bounds, Set<Long> allBuildingFootprint) {
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        int clearRadius = radius + 8;
        int clearRadiusSq = clearRadius * clearRadius;
        boolean changed = false;
        for (int x = bounds.minX(); x <= bounds.maxX(); ++x) {
            int dx = x - center.getX();
            for (int z = bounds.minZ(); z <= bounds.maxZ(); ++z) {
                int dz = z - center.getZ();
                if (dx * dx + dz * dz > clearRadiusSq) continue;
                SurfaceColumn column = SectSettlementFeature.surfaceColumn(level, pos, x, z);
                int surfaceY = column.groundY();
                int minY = Math.max(level.getMinBuildHeight(), surfaceY - 2);
                int maxY = SectSettlementFeature.vegetationScanTop(level, surfaceY, column.topY(), 48);
                int hardMaxY = SectSettlementFeature.vegetationHardScanTop(level, surfaceY, 48);
                changed |= SectSettlementFeature.clearVegetationColumn(level, pos, x, z, minY, maxY, hardMaxY, bounds, allBuildingFootprint);
            }
        }
        return changed;
    }

    private static boolean clearVegetationColumn(WorldGenLevel level, BlockPos.MutableBlockPos pos, int x, int z, int minY, int maxY, BoundingBox bounds, Set<Long> protectedFootprint) {
        return SectSettlementFeature.clearVegetationColumn(level, pos, x, z, minY, maxY, maxY, bounds, protectedFootprint);
    }

    private static boolean clearVegetationColumn(WorldGenLevel level, BlockPos.MutableBlockPos pos, int x, int z, int minY, int softMaxY, int hardMaxY, BoundingBox bounds, Set<Long> protectedFootprint) {
        boolean changed = false;
        int emptyAirAboveSoftLimit = 0;
        for (int y = minY; y <= hardMaxY; ++y) {
            BlockPos.MutableBlockPos at = pos.set(x, y, z);
            BlockState state = level.getBlockState((BlockPos)at);
            if (!SectSettlementFeature.shouldClearVegetation(state)) {
                if (y > softMaxY && state.isAir()) {
                    if (++emptyAirAboveSoftLimit < 16) continue;
                    break;
                }
                if (y <= softMaxY) continue;
                emptyAirAboveSoftLimit = 0;
                continue;
            }
            emptyAirAboveSoftLimit = 0;
            if (SectSettlementFeature.shouldClearAsConnectedCluster(state)) {
                changed |= SectSettlementFeature.clearConnectedVegetation(level, x, y, z, minY, hardMaxY, bounds, protectedFootprint);
                continue;
            }
            SectSettlementFeature.set(level, (BlockPos)at, AIR);
            changed = true;
        }
        return changed;
    }

    private static boolean clearConnectedVegetation(WorldGenLevel level, int startX, int startY, int startZ, int minY, int maxY, BoundingBox bounds, Set<Long> protectedFootprint) {
        ArrayDeque<BlockPos> queue = new ArrayDeque<BlockPos>();
        HashSet<Long> visited = new HashSet<Long>();
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        queue.add(new BlockPos(startX, startY, startZ));
        int cleared = 0;
        while (!queue.isEmpty() && cleared < 4096) {
            boolean protectedStructureWood;
            BlockState state;
            long key;
            BlockPos current = (BlockPos)queue.removeFirst();
            int x = current.getX();
            int y = current.getY();
            int z = current.getZ();
            if (x < bounds.minX() || x > bounds.maxX() || z < bounds.minZ() || z > bounds.maxZ() || y < minY || y > maxY || Math.abs(x - startX) > 28 || Math.abs(z - startZ) > 28 || Math.abs(y - startY) > 72 || !visited.add(key = BlockPos.asLong((int)x, (int)y, (int)z)) || !SectSettlementFeature.shouldClearVegetation(state = level.getBlockState((BlockPos)pos.set(x, y, z)))) continue;
            boolean bl = protectedStructureWood = protectedFootprint.contains(SectSettlementFeature.packXZ(x, z)) && SectSettlementFeature.isStructureWood(state);
            if (!protectedStructureWood) {
                SectSettlementFeature.set(level, (BlockPos)pos.set(x, y, z), AIR);
                ++cleared;
            }
            SectSettlementFeature.enqueueVegetationNeighbors(queue, x, y, z);
        }
        return cleared > 0;
    }

    private static void enqueueVegetationNeighbors(ArrayDeque<BlockPos> queue, int x, int y, int z) {
        for (int dx = -1; dx <= 1; ++dx) {
            for (int dy = -1; dy <= 1; ++dy) {
                for (int dz = -1; dz <= 1; ++dz) {
                    if (dx == 0 && dy == 0 && dz == 0) continue;
                    queue.add(new BlockPos(x + dx, y + dy, z + dz));
                }
            }
        }
    }

    private static boolean isTemplateTerrainBlock(BlockState state) {
        return state.is(Blocks.GRASS_BLOCK) || state.is(Blocks.DIRT) || state.is(Blocks.COARSE_DIRT) || state.is(Blocks.ROOTED_DIRT) || state.is(Blocks.PODZOL) || state.is(Blocks.MYCELIUM);
    }

    private static boolean shouldClearVegetation(BlockState state) {
        return state.is(BlockTags.LOGS) || state.is(BlockTags.LEAVES) || state.is(BlockTags.SAPLINGS) || state.is(BlockTags.FLOWERS) || state.is(BlockTags.CROPS) || state.is(Blocks.GRASS) || state.is(Blocks.TALL_GRASS) || state.is(Blocks.FERN) || state.is(Blocks.LARGE_FERN) || state.is(Blocks.VINE) || state.is(Blocks.WEEPING_VINES) || state.is(Blocks.WEEPING_VINES_PLANT) || state.is(Blocks.TWISTING_VINES) || state.is(Blocks.TWISTING_VINES_PLANT) || state.is(Blocks.LILY_PAD) || state.is(Blocks.SNOW) || state.is(Blocks.POWDER_SNOW) || state.is(Blocks.PINK_PETALS) || state.is(Blocks.SPORE_BLOSSOM) || state.is(Blocks.TORCHFLOWER) || state.is(Blocks.PITCHER_PLANT) || state.is(Blocks.SUGAR_CANE) || state.is(Blocks.BAMBOO) || state.is(Blocks.BAMBOO_SAPLING) || state.is(Blocks.CACTUS) || state.is(Blocks.DEAD_BUSH) || state.is(Blocks.BEE_NEST) || state.is(Blocks.BEEHIVE) || state.is(Blocks.COCOA) || state.is(Blocks.PUMPKIN) || state.is(Blocks.MELON) || state.is(Blocks.PUMPKIN_STEM) || state.is(Blocks.MELON_STEM) || state.is(Blocks.ATTACHED_PUMPKIN_STEM) || state.is(Blocks.ATTACHED_MELON_STEM) || state.is(Blocks.SWEET_BERRY_BUSH) || state.is(Blocks.AZALEA) || state.is(Blocks.FLOWERING_AZALEA) || state.is(Blocks.MOSS_CARPET) || state.is(Blocks.HANGING_ROOTS) || state.is(Blocks.CAVE_VINES) || state.is(Blocks.CAVE_VINES_PLANT) || state.is(Blocks.GLOW_LICHEN) || state.is(Blocks.SMALL_DRIPLEAF) || state.is(Blocks.BIG_DRIPLEAF) || state.is(Blocks.BIG_DRIPLEAF_STEM) || state.is(Blocks.MANGROVE_ROOTS) || state.is(Blocks.MUDDY_MANGROVE_ROOTS) || state.is(Blocks.MANGROVE_PROPAGULE) || state.is(Blocks.SEAGRASS) || state.is(Blocks.TALL_SEAGRASS) || state.is(Blocks.KELP) || state.is(Blocks.KELP_PLANT) || state.is(Blocks.BROWN_MUSHROOM) || state.is(Blocks.RED_MUSHROOM) || state.is(Blocks.BROWN_MUSHROOM_BLOCK) || state.is(Blocks.RED_MUSHROOM_BLOCK) || state.is(Blocks.MUSHROOM_STEM);
    }

    private static boolean shouldClearAsConnectedCluster(BlockState state) {
        return state.is(BlockTags.LOGS) || state.is(BlockTags.LEAVES) || state.is(Blocks.VINE) || state.is(Blocks.WEEPING_VINES) || state.is(Blocks.WEEPING_VINES_PLANT) || state.is(Blocks.TWISTING_VINES) || state.is(Blocks.TWISTING_VINES_PLANT) || state.is(Blocks.CAVE_VINES) || state.is(Blocks.CAVE_VINES_PLANT) || state.is(Blocks.MANGROVE_ROOTS) || state.is(Blocks.MUDDY_MANGROVE_ROOTS) || state.is(Blocks.BROWN_MUSHROOM_BLOCK) || state.is(Blocks.RED_MUSHROOM_BLOCK) || state.is(Blocks.MUSHROOM_STEM);
    }

    private static boolean isStructureWood(BlockState state) {
        return state.is(BlockTags.LOGS);
    }

    private static void collectRoadCells(WorldGenLevel level, BlockPos center, RoadLine road, BoundingBox bounds, List<RoadAnchor> anchors, long seed, Map<Long, RoadCell> roadCells) {
        List<RoadPoint> points = SectSettlementFeature.collectRoadPoints(road);
        Map<Integer, Integer> centerHeights = SectSettlementFeature.collectRoadCenterHeights(level, center, points, bounds, anchors);
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        for (int i = 0; i < points.size(); ++i) {
            int worldZ;
            RoadPoint point = points.get(i);
            int worldX = center.getX() + point.x();
            if (!SectSettlementFeature.roadPointTouchesChunk(worldX, worldZ = center.getZ() + point.z(), bounds)) continue;
            boolean centerInsideChunk = SectSettlementFeature.pointInsideChunk(worldX, worldZ, bounds);
            RoadStep climb = centerInsideChunk ? SectSettlementFeature.climbDirection(center, points, i, centerHeights, bounds) : null;
            RoadShape shape = SectSettlementFeature.roadShape(points, i);
            Integer centerY = centerInsideChunk ? centerHeights.get(i) : null;
            SectSettlementFeature.collectRoadShapeCells(level, worldX, worldZ, bounds, anchors, seed, climb, shape, centerY, pos, roadCells);
        }
    }

    private static Map<Integer, Integer> collectRoadCenterHeights(WorldGenLevel level, BlockPos center, List<RoadPoint> points, BoundingBox bounds, List<RoadAnchor> anchors) {
        LinkedHashMap<Integer, Integer> heights = new LinkedHashMap<Integer, Integer>();
        for (int i = 0; i < points.size(); ++i) {
            int worldZ;
            RoadPoint point = points.get(i);
            int worldX = center.getX() + point.x();
            if (!SectSettlementFeature.pointInsideChunk(worldX, worldZ = center.getZ() + point.z(), bounds)) continue;
            heights.put(i, SectSettlementFeature.roadBlockY(level, worldX, worldZ, bounds, anchors));
        }
        SectSettlementFeature.smoothRoadCenterHeights(heights);
        return heights;
    }

    private static void smoothRoadCenterHeights(Map<Integer, Integer> heights) {
        if (heights.size() < 2) {
            return;
        }
        ArrayList<Integer> indices = new ArrayList<Integer>(heights.keySet());
        for (int pass = 0; pass < 4; ++pass) {
            int currentY;
            int i;
            for (i = 1; i < indices.size(); ++i) {
                int previousIndex = (Integer)indices.get(i - 1);
                int currentIndex = (Integer)indices.get(i);
                if (currentIndex != previousIndex + 1) continue;
                int previousY = heights.get(previousIndex);
                currentY = heights.get(currentIndex);
                if (Math.abs(currentY - previousY) > 2) continue;
                heights.put(currentIndex, SectSettlementFeature.clampRoadStep(currentY, previousY));
            }
            for (i = indices.size() - 2; i >= 0; --i) {
                int currentIndex = (Integer)indices.get(i);
                int nextIndex = (Integer)indices.get(i + 1);
                if (nextIndex != currentIndex + 1) continue;
                int nextY = heights.get(nextIndex);
                currentY = heights.get(currentIndex);
                if (Math.abs(currentY - nextY) > 2) continue;
                heights.put(currentIndex, SectSettlementFeature.clampRoadStep(currentY, nextY));
            }
            SectSettlementFeature.flattenSmallRoadUndulations(heights, indices);
        }
    }

    private static void flattenSmallRoadUndulations(Map<Integer, Integer> heights, List<Integer> indices) {
        LinkedHashMap<Integer, Integer> updates = new LinkedHashMap<Integer, Integer>();
        for (int i = 1; i < indices.size() - 1; ++i) {
            int previousIndex = indices.get(i - 1);
            int currentIndex = indices.get(i);
            int nextIndex = indices.get(i + 1);
            if (currentIndex != previousIndex + 1 || nextIndex != currentIndex + 1) continue;
            int previousY = heights.get(previousIndex);
            int currentY = heights.get(currentIndex);
            int nextY = heights.get(nextIndex);
            if (previousY != nextY || Math.abs(currentY - previousY) > 1) continue;
            updates.put(currentIndex, previousY);
        }
        for (Map.Entry update : updates.entrySet()) {
            heights.put((Integer)update.getKey(), (Integer)update.getValue());
        }
    }

    private static int clampRoadStep(int value, int reference) {
        if (value > reference + 1) {
            return reference + 1;
        }
        if (value < reference - 1) {
            return reference - 1;
        }
        return value;
    }

    private static RoadStep climbDirection(BlockPos center, List<RoadPoint> points, int index, Map<Integer, Integer> centerHeights, BoundingBox bounds) {
        int currentZ;
        RoadPoint here = points.get(index);
        int currentX = center.getX() + here.x();
        if (!SectSettlementFeature.pointInsideChunk(currentX, currentZ = center.getZ() + here.z(), bounds)) {
            return null;
        }
        Integer currentY = centerHeights.get(index);
        if (currentY == null) {
            return null;
        }
        if (index + 1 < points.size()) {
            Integer nextY;
            int nextZ;
            RoadPoint next = points.get(index + 1);
            int nextX = center.getX() + next.x();
            if (SectSettlementFeature.pointInsideChunk(nextX, nextZ = center.getZ() + next.z(), bounds) && (nextY = centerHeights.get(index + 1)) > currentY) {
                Direction direction = SectSettlementFeature.directionTo(here, next);
                return new RoadStep(direction, nextY - currentY > 1 || SectSettlementFeature.isContinuedRoadClimb(center, points, index, index + 1, currentY, centerHeights, bounds));
            }
        }
        if (index > 0) {
            Integer previousY;
            int previousZ;
            RoadPoint previous = points.get(index - 1);
            int previousX = center.getX() + previous.x();
            if (SectSettlementFeature.pointInsideChunk(previousX, previousZ = center.getZ() + previous.z(), bounds) && (previousY = centerHeights.get(index - 1)) > currentY) {
                Direction direction = SectSettlementFeature.directionTo(here, previous);
                return new RoadStep(direction, previousY - currentY > 1 || SectSettlementFeature.isContinuedRoadClimb(center, points, index, index - 1, currentY, centerHeights, bounds));
            }
        }
        return null;
    }

    private static boolean isContinuedRoadClimb(BlockPos center, List<RoadPoint> points, int index, int higherIndex, int currentY, Map<Integer, Integer> centerHeights, BoundingBox bounds) {
        int fartherHighIndex;
        int direction = Integer.compare(higherIndex, index);
        int lowerSideIndex = index - direction;
        if (SectSettlementFeature.isRoadPointInside(center, points, lowerSideIndex, bounds)) {
            Integer y = centerHeights.get(lowerSideIndex);
            if (y == null) {
                return false;
            }
            if (currentY - y >= 1) {
                return true;
            }
        }
        if (SectSettlementFeature.isRoadPointInside(center, points, fartherHighIndex = higherIndex + direction, bounds)) {
            Integer y = centerHeights.get(fartherHighIndex);
            if (y == null) {
                return false;
            }
            return y - currentY >= 2;
        }
        return false;
    }

    private static boolean isRoadPointInside(BlockPos center, List<RoadPoint> points, int index, BoundingBox bounds) {
        if (index < 0 || index >= points.size()) {
            return false;
        }
        RoadPoint point = points.get(index);
        int x = center.getX() + point.x();
        int z = center.getZ() + point.z();
        return SectSettlementFeature.pointInsideChunk(x, z, bounds);
    }

    private static boolean roadPointTouchesChunk(int x, int z, BoundingBox bounds) {
        return x + 1 >= bounds.minX() && x - 1 <= bounds.maxX() && z + 1 >= bounds.minZ() && z - 1 <= bounds.maxZ();
    }

    private static boolean restoreGateSupports(WorldGenLevel level, long seed, BlockPos center, BoundingBox bounds, PlacedPiece piece, int baseY) {
        if (!piece.piece().equals(GATE) || !SectSettlementFeature.pieceIntersectsChunk(center, piece, bounds)) {
            return false;
        }
        int worldX = center.getX() + piece.localX();
        int worldZ = center.getZ() + piece.localZ();
        int supportBaseY = SectSettlementFeature.templateBaseY(baseY, piece.piece());
        boolean placed = false;
        for (int supportZOffset : GATE_SUPPORT_ZS) {
            LocalXZ support = SectSettlementFeature.rotateLocal(GATE, piece.rotation(), 4, supportZOffset);
            placed |= SectSettlementFeature.restoreGateSupport(level, bounds, seed, worldX + support.x(), supportBaseY, worldZ + support.z());
        }
        return placed;
    }

    private static boolean restoreGateSupport(WorldGenLevel level, BoundingBox bounds, long seed, int x, int baseY, int z) {
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        boolean placed = false;
        placed |= SectSettlementFeature.setIfInBounds(level, bounds, pos, x, baseY - 1, z, SectSettlementFeature.chooseFoundation(seed, x, z));
        placed |= SectSettlementFeature.setIfInBounds(level, bounds, pos, x, baseY, z, Blocks.STONE_BRICKS.defaultBlockState());
        placed |= SectSettlementFeature.setIfInBounds(level, bounds, pos, x - 1, baseY, z, SectSettlementFeature.gateSupportStair(Direction.EAST));
        placed |= SectSettlementFeature.setIfInBounds(level, bounds, pos, x + 1, baseY, z, SectSettlementFeature.gateSupportStair(Direction.WEST));
        placed |= SectSettlementFeature.setIfInBounds(level, bounds, pos, x, baseY, z - 1, SectSettlementFeature.gateSupportStair(Direction.SOUTH));
        placed |= SectSettlementFeature.setIfInBounds(level, bounds, pos, x, baseY, z + 1, SectSettlementFeature.gateSupportStair(Direction.NORTH));
        placed |= SectSettlementFeature.setIfInBounds(level, bounds, pos, x, baseY + 1, z, Blocks.STONE_BRICKS.defaultBlockState());
        placed |= SectSettlementFeature.setIfInBounds(level, bounds, pos, x, baseY + 2, z, Blocks.STONE_BRICKS.defaultBlockState());
        placed |= SectSettlementFeature.setIfInBounds(level, bounds, pos, x - 1, baseY + 2, z, SectSettlementFeature.gateSupportStair(Direction.EAST));
        placed |= SectSettlementFeature.setIfInBounds(level, bounds, pos, x + 1, baseY + 2, z, SectSettlementFeature.gateSupportStair(Direction.WEST));
        placed |= SectSettlementFeature.setIfInBounds(level, bounds, pos, x, baseY + 2, z - 1, SectSettlementFeature.gateSupportStair(Direction.SOUTH));
        placed |= SectSettlementFeature.setIfInBounds(level, bounds, pos, x, baseY + 2, z + 1, SectSettlementFeature.gateSupportStair(Direction.NORTH));
        return placed |= SectSettlementFeature.setIfInBounds(level, bounds, pos, x, baseY + 3, z, Blocks.STONE_BRICKS.defaultBlockState());
    }

    private static BlockState gateSupportStair(Direction facing) {
        return (BlockState)Blocks.STONE_BRICK_STAIRS.defaultBlockState().setValue((Property)StairBlock.FACING, (Comparable)facing);
    }

    private static boolean setIfInBounds(WorldGenLevel level, BoundingBox bounds, BlockPos.MutableBlockPos pos, int x, int y, int z, BlockState state) {
        if (x < bounds.minX() || x > bounds.maxX() || y < bounds.minY() || y > bounds.maxY() || z < bounds.minZ() || z > bounds.maxZ()) {
            return false;
        }
        SectSettlementFeature.set(level, (BlockPos)pos.set(x, y, z), state);
        return true;
    }

    private static boolean pointInsideChunk(int x, int z, BoundingBox bounds) {
        return x >= bounds.minX() && x <= bounds.maxX() && z >= bounds.minZ() && z <= bounds.maxZ();
    }

    private static Direction directionTo(RoadPoint from, RoadPoint to) {
        if (to.x() > from.x()) {
            return Direction.EAST;
        }
        if (to.x() < from.x()) {
            return Direction.WEST;
        }
        if (to.z() > from.z()) {
            return Direction.SOUTH;
        }
        return Direction.NORTH;
    }

    private static Rotation rotationForGateFacing(Direction facing) {
        return switch (facing) {
            case NORTH -> Rotation.CLOCKWISE_90;
            case SOUTH -> Rotation.COUNTERCLOCKWISE_90;
            case WEST -> Rotation.CLOCKWISE_180;
            default -> Rotation.NONE;
        };
    }

    private static Direction gateFacing(Rotation rotation) {
        return switch (rotation) {
            case CLOCKWISE_90 -> Direction.NORTH;
            case COUNTERCLOCKWISE_90 -> Direction.SOUTH;
            case CLOCKWISE_180 -> Direction.WEST;
            default -> Direction.EAST;
        };
    }

    private static GatePass gatePass(Rotation rotation) {
        int passZ = GATE.sizeZ() / 2;
        LocalXZ start = SectSettlementFeature.rotateLocal(GATE, rotation, 0, passZ);
        LocalXZ middle = SectSettlementFeature.rotateLocal(GATE, rotation, GATE.sizeX() / 2, passZ);
        LocalXZ end = SectSettlementFeature.rotateLocal(GATE, rotation, GATE.sizeX() - 1, passZ);
        Direction.Axis axis = start.x() != end.x() ? Direction.Axis.X : Direction.Axis.Z;
        return new GatePass(axis, middle.x(), middle.z());
    }

    private static int placedSizeX(PlacedPiece piece) {
        return SectSettlementFeature.placedSizeX(piece.piece(), piece.rotation());
    }

    private static int placedSizeZ(PlacedPiece piece) {
        return SectSettlementFeature.placedSizeZ(piece.piece(), piece.rotation());
    }

    private static int placedSizeX(PieceSpec piece, Rotation rotation) {
        return SectSettlementFeature.swapsAxes(rotation) ? piece.sizeZ() : piece.sizeX();
    }

    private static int placedSizeZ(PieceSpec piece, Rotation rotation) {
        return SectSettlementFeature.swapsAxes(rotation) ? piece.sizeX() : piece.sizeZ();
    }

    private static boolean swapsAxes(Rotation rotation) {
        return rotation == Rotation.CLOCKWISE_90 || rotation == Rotation.COUNTERCLOCKWISE_90;
    }

    private static LocalXZ rotateLocal(PieceSpec piece, Rotation rotation, int x, int z) {
        return switch (rotation) {
            case CLOCKWISE_90 -> new LocalXZ(piece.sizeZ() - 1 - z, x);
            case CLOCKWISE_180 -> new LocalXZ(piece.sizeX() - 1 - x, piece.sizeZ() - 1 - z);
            case COUNTERCLOCKWISE_90 -> new LocalXZ(z, piece.sizeX() - 1 - x);
            default -> new LocalXZ(x, z);
        };
    }

    private static BlockPos placementOrigin(int worldX, int baseY, int worldZ, PieceSpec piece, Rotation rotation) {
        return switch (rotation) {
            case CLOCKWISE_90 -> new BlockPos(worldX + piece.sizeZ() - 1, baseY, worldZ);
            case CLOCKWISE_180 -> new BlockPos(worldX + piece.sizeX() - 1, baseY, worldZ + piece.sizeZ() - 1);
            case COUNTERCLOCKWISE_90 -> new BlockPos(worldX, baseY, worldZ + piece.sizeX() - 1);
            default -> new BlockPos(worldX, baseY, worldZ);
        };
    }

    private static int templateBaseY(int foundationY, PieceSpec piece) {
        return foundationY + piece.templateYOffset();
    }

    private static void collectRoadShapeCells(WorldGenLevel level, int worldX, int worldZ, BoundingBox bounds, List<RoadAnchor> anchors, long seed, RoadStep climb, RoadShape shape, Integer centerY, BlockPos.MutableBlockPos pos, Map<Long, RoadCell> roadCells) {
        for (int dx = -1; dx <= 1; ++dx) {
            for (int dz = -1; dz <= 1; ++dz) {
                if (!SectSettlementFeature.roadShapeIncludes(shape, dx, dz)) continue;
                int x = worldX + dx;
                int z = worldZ + dz;
                if (x < bounds.minX() || x > bounds.maxX() || z < bounds.minZ() || z > bounds.maxZ()) continue;
                int baseY = centerY != null ? centerY : SectSettlementFeature.roadBlockY(level, x, z, bounds, anchors);
                int naturalY = SectSettlementFeature.height(level, x, z) - 1;
                boolean waterSurface = SectSettlementFeature.isFluidSurface(level, pos, x, naturalY, z);
                int cellY = waterSurface ? Math.max(baseY, naturalY) : baseY;
                RoadStep step = climb;
                if (step != null && !SectSettlementFeature.isRampCell(dx, dz, step.direction(), shape)) {
                    step = null;
                }
                int surfaceY = step == null ? cellY : cellY + 1;
                BlockState road = SectSettlementFeature.roadStateFor(seed, x, z, step);
                SectSettlementFeature.mergeRoadCell(roadCells, new RoadCell(x, surfaceY, z, road, step != null, waterSurface));
            }
        }
    }

    private static boolean paveRoadCells(WorldGenLevel level, BoundingBox bounds, Set<Long> roadBlockingFootprint, Set<Long> allBuildingFootprint, long seed, Collection<RoadCell> roadCells) {
        boolean placed = false;
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        for (RoadCell cell : roadCells) {
            if (roadBlockingFootprint.contains(SectSettlementFeature.packXZ(cell.x(), cell.z()))) continue;
            SectSettlementFeature.stabilizeRoadBase(level, cell.x(), cell.y(), cell.z(), bounds, seed, cell.waterSurface());
            if (cell.step() && cell.y() - 1 >= level.getMinBuildHeight()) {
                SectSettlementFeature.set(level, (BlockPos)pos.set(cell.x(), cell.y() - 1, cell.z()), SectSettlementFeature.chooseFoundation(seed, cell.x(), cell.z()));
            }
            SectSettlementFeature.set(level, (BlockPos)pos.set(cell.x(), cell.y(), cell.z()), cell.state());
            for (int clear = 1; clear <= 5; ++clear) {
                SectSettlementFeature.set(level, (BlockPos)pos.set(cell.x(), cell.y() + clear, cell.z()), AIR);
            }
            placed = true;
        }
        return placed;
    }

    private static void mergeRoadCell(Map<Long, RoadCell> roadCells, RoadCell candidate) {
        long key = SectSettlementFeature.packXZ(candidate.x(), candidate.z());
        RoadCell existing = roadCells.get(key);
        if (existing == null || candidate.y() < existing.y() || candidate.y() == existing.y() && existing.step() && !candidate.step()) {
            roadCells.put(key, candidate);
        }
    }

    private static int roadBlockY(WorldGenLevel level, int x, int z, BoundingBox bounds, List<RoadAnchor> anchors) {
        int naturalY;
        int terrainY = naturalY = SectSettlementFeature.height(level, x, z) - 1;
        RoadAnchor nearest = null;
        int nearestDistance = Integer.MAX_VALUE;
        for (RoadAnchor anchor : anchors) {
            int distance = Math.abs(x - anchor.x()) + Math.abs(z - anchor.z());
            if (distance >= nearestDistance) continue;
            nearestDistance = distance;
            nearest = anchor;
        }
        if (nearest == null) {
            return terrainY;
        }
        int heightDelta = nearest.y() - terrainY;
        if (nearestDistance <= 3) {
            return nearest.y();
        }
        if (nearestDistance <= 6 && Math.abs(heightDelta) <= 1) {
            return nearest.y();
        }
        if (nearestDistance <= 8 && heightDelta != 0) {
            int maxOffset = 8 - nearestDistance;
            int offset = Mth.clamp((int)Math.abs(heightDelta), (int)0, (int)Math.max(0, maxOffset));
            return terrainY + Integer.signum(heightDelta) * offset;
        }
        return terrainY;
    }

    private static BlockState roadStateFor(long seed, int x, int z, RoadStep step) {
        if (step != null) {
            return step.steep() ? (BlockState)ROAD_STAIR.setValue((Property)StairBlock.FACING, (Comparable)step.direction()) : ROAD_SLAB;
        }
        return SectSettlementFeature.chooseFoundation(seed, x, z);
    }

    private static boolean isRampCell(int dx, int dz, Direction climb, RoadShape shape) {
        return switch (shape) {
            default -> throw new IncompatibleClassChangeError();
            case X_AXIS -> {
                if (climb.getAxis() == Direction.Axis.X) {
                    yield true;
                }
                yield false;
            }
            case Z_AXIS -> {
                if (climb.getAxis() == Direction.Axis.Z) {
                    yield true;
                }
                yield false;
            }
            case CORNER -> {
                if (climb.getAxis() == Direction.Axis.X) {
                    if (dx == 0) {
                        yield true;
                    }
                    yield false;
                }
                yield dz == 0;
            }
        };
    }

    private static RoadShape roadShape(List<RoadPoint> points, int index) {
        Direction.Axis axis;
        Direction.Axis next;
        Direction.Axis previous = index > 0 ? SectSettlementFeature.roadAxis(points.get(index - 1), points.get(index)) : null;
        Direction.Axis axis2 = next = index + 1 < points.size() ? SectSettlementFeature.roadAxis(points.get(index), points.get(index + 1)) : null;
        if (previous != null && next != null && previous != next) {
            return RoadShape.CORNER;
        }
        Direction.Axis axis3 = axis = next != null ? next : previous;
        if (axis == Direction.Axis.X) {
            return RoadShape.X_AXIS;
        }
        if (axis == Direction.Axis.Z) {
            return RoadShape.Z_AXIS;
        }
        return RoadShape.CORNER;
    }

    private static Direction.Axis roadAxis(RoadPoint from, RoadPoint to) {
        if (from.x() != to.x()) {
            return Direction.Axis.X;
        }
        if (from.z() != to.z()) {
            return Direction.Axis.Z;
        }
        return null;
    }

    private static boolean roadShapeIncludes(RoadShape shape, int dx, int dz) {
        return switch (shape) {
            default -> throw new IncompatibleClassChangeError();
            case X_AXIS -> {
                if (dx == 0) {
                    yield true;
                }
                yield false;
            }
            case Z_AXIS -> {
                if (dz == 0) {
                    yield true;
                }
                yield false;
            }
            case CORNER -> true;
        };
    }

    private static void clearRoadVegetation(WorldGenLevel level, BlockPos.MutableBlockPos pos, int x, int y, int z, BoundingBox bounds, Set<Long> allBuildingFootprint) {
        int minY = Math.max(level.getMinBuildHeight(), y - 2);
        for (int dx = -6; dx <= 6; ++dx) {
            for (int dz = -6; dz <= 6; ++dz) {
                int xx = x + dx;
                int zz = z + dz;
                if (xx < bounds.minX() || xx > bounds.maxX() || zz < bounds.minZ() || zz > bounds.maxZ() || allBuildingFootprint.contains(SectSettlementFeature.packXZ(xx, zz))) continue;
                SurfaceColumn column = SectSettlementFeature.surfaceColumn(level, pos, xx, zz);
                int maxY = SectSettlementFeature.vegetationScanTop(level, y, column.topY(), 32);
                int hardMaxY = SectSettlementFeature.vegetationHardScanTop(level, y, 32);
                SectSettlementFeature.clearVegetationColumn(level, pos, xx, zz, minY, maxY, hardMaxY, bounds, allBuildingFootprint);
            }
        }
    }

    private static void stabilizeRoadBase(WorldGenLevel level, int x, int y, int z, BoundingBox bounds, long seed, boolean waterSurface) {
        int fillY;
        if (x < bounds.minX() || x > bounds.maxX() || z < bounds.minZ() || z > bounds.maxZ()) {
            return;
        }
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        int depth = waterSurface ? 10 : 3;
        for (int fill = 1; fill <= depth && (fillY = y - fill) >= level.getMinBuildHeight() && fillY >= bounds.minY(); ++fill) {
            BlockPos.MutableBlockPos at = pos.set(x, y - fill, z);
            BlockState state = level.getBlockState((BlockPos)at);
            if (!SectSettlementFeature.shouldReplaceSupport(state, waterSurface)) continue;
            SectSettlementFeature.set(level, (BlockPos)at, SectSettlementFeature.chooseFoundation(seed, x, z));
        }
    }

    private static boolean placeStreetlights(WorldGenLevel level, StructureTemplateManager manager, long seed, BlockPos center, BoundingBox chunkBounds, List<RoadLine> roads, List<RoadAnchor> anchors, List<StreetlightCandidate> candidates, Set<Long> allBuildingFootprint) {
        StructureTemplate template = (StructureTemplate)manager.get(STREETLIGHT.id()).orElseThrow();
        StructurePlaceSettings settings = new StructurePlaceSettings().setIgnoreEntities(true).setKnownShape(true).setBoundingBox(chunkBounds).addProcessor((StructureProcessor)BlockIgnoreProcessor.STRUCTURE_AND_AIR);
        boolean wroteAny = false;
        for (StreetlightCandidate candidate : candidates) {
            if (!SectSettlementFeature.pointInsideChunk(candidate.roadX(), candidate.roadZ(), chunkBounds)) continue;
            int roadY = SectSettlementFeature.roadBlockY(level, candidate.roadX(), candidate.roadZ(), chunkBounds, anchors);
            wroteAny |= SectSettlementFeature.placeStreetlight(level, template, settings, chunkBounds, seed, candidate.index(), candidate.x(), roadY, candidate.z(), allBuildingFootprint);
        }
        return wroteAny;
    }

    private static List<StreetlightCandidate> planStreetlightCandidates(long seed, BlockPos center, List<RoadLine> roads, Set<Long> allBuildingFootprint, Set<Long> roadFootprint) {
        ArrayList<StreetlightCandidate> candidates = new ArrayList<StreetlightCandidate>();
        int planned = 0;
        RandomSource random = RandomSource.create((long)(seed ^ 0x57AEE71L));
        ArrayList<RoadPoint> accepted = new ArrayList<RoadPoint>();
        int roadIndex = 0;
        for (RoadLine road : roads) {
            int start;
            List<RoadPoint> points = SectSettlementFeature.collectRoadPoints(road);
            int stride = 36 + random.nextInt(17);
            for (int i = start = Math.min(points.size() - 1, 10 + random.nextInt(12)); i < points.size(); i += stride) {
                if (planned >= 5 && random.nextFloat() > 0.72f) continue;
                RoadPoint local = points.get(i);
                Direction pathDirection = SectSettlementFeature.roadDirectionAt(points, i);
                if (pathDirection == null) continue;
                boolean leftFirst = (roadIndex + i / Math.max(1, stride) & 1) == 0;
                StreetlightCandidate candidate = SectSettlementFeature.tryStreetlightCandidate(center, local, pathDirection, leftFirst, roadIndex * 1000 + i, allBuildingFootprint, roadFootprint, accepted);
                if (candidate == null) {
                    candidate = SectSettlementFeature.tryStreetlightCandidate(center, local, pathDirection, !leftFirst, roadIndex * 1000 + i + 491, allBuildingFootprint, roadFootprint, accepted);
                }
                if (candidate == null) continue;
                accepted.add(new RoadPoint(candidate.x(), candidate.z()));
                candidates.add(candidate);
                ++planned;
            }
            ++roadIndex;
        }
        return candidates;
    }

    private static StreetlightCandidate tryStreetlightCandidate(BlockPos center, RoadPoint roadPoint, Direction pathDirection, boolean leftSide, int index, Set<Long> allBuildingFootprint, Set<Long> roadFootprint, List<RoadPoint> accepted) {
        int z;
        Direction side = SectSettlementFeature.perpendicular(pathDirection, leftSide);
        int roadX = center.getX() + roadPoint.x();
        int roadZ = center.getZ() + roadPoint.z();
        int x = roadX + side.getStepX() * 4;
        if (!SectSettlementFeature.canPlaceStreetlightAt(x, z = roadZ + side.getStepZ() * 4, allBuildingFootprint, roadFootprint, accepted)) {
            return null;
        }
        return new StreetlightCandidate(x, z, roadX, roadZ, index);
    }

    private static boolean canPlaceStreetlightAt(int x, int z, Set<Long> allBuildingFootprint, Set<Long> roadFootprint, List<RoadPoint> accepted) {
        int x0 = x - 2;
        int z0 = z - 2;
        for (int xx = x0; xx <= x0 + STREETLIGHT.sizeX() - 1; ++xx) {
            for (int zz = z0; zz <= z0 + STREETLIGHT.sizeZ() - 1; ++zz) {
                long key = SectSettlementFeature.packXZ(xx, zz);
                if (!roadFootprint.contains(key) && !allBuildingFootprint.contains(key)) continue;
                return false;
            }
        }
        for (RoadPoint existing : accepted) {
            int dz;
            int dx = existing.x() - x;
            if (dx * dx + (dz = existing.z() - z) * dz >= 1600) continue;
            return false;
        }
        return true;
    }

    private static Direction roadDirectionAt(List<RoadPoint> points, int index) {
        Direction direction;
        if (index + 1 < points.size() && (direction = SectSettlementFeature.directionTo(points.get(index), points.get(index + 1))).getAxis().isHorizontal()) {
            return direction;
        }
        if (index > 0 && (direction = SectSettlementFeature.directionTo(points.get(index - 1), points.get(index))).getAxis().isHorizontal()) {
            return direction;
        }
        return null;
    }

    private static Direction perpendicular(Direction direction, boolean leftSide) {
        return switch (direction) {
            case NORTH -> {
                if (leftSide) {
                    yield Direction.WEST;
                }
                yield Direction.EAST;
            }
            case SOUTH -> {
                if (leftSide) {
                    yield Direction.EAST;
                }
                yield Direction.WEST;
            }
            case EAST -> {
                if (leftSide) {
                    yield Direction.NORTH;
                }
                yield Direction.SOUTH;
            }
            case WEST -> {
                if (leftSide) {
                    yield Direction.SOUTH;
                }
                yield Direction.NORTH;
            }
            default -> Direction.NORTH;
        };
    }

    private static boolean placeStreetlight(WorldGenLevel level, StructureTemplate template, StructurePlaceSettings settings, BoundingBox bounds, long seed, int index, int x, int y, int z, Set<Long> allBuildingFootprint) {
        int x0 = x - 2;
        int z0 = z - 2;
        if (!SectSettlementFeature.pointInsideChunk(x, z, bounds) || !SectSettlementFeature.rectIntersectsChunk(x0, z0, STREETLIGHT.sizeX(), STREETLIGHT.sizeZ(), bounds)) {
            return false;
        }
        SectSettlementFeature.prepareSmallFoundation(level, x0, y, z0, STREETLIGHT.sizeX(), STREETLIGHT.sizeZ(), bounds, seed);
        BlockPos at = new BlockPos(x0, SectSettlementFeature.templateBaseY(y, STREETLIGHT), z0);
        RandomSource random = RandomSource.create((long)SectSettlementFeature.seedFor(seed, x, z, index));
        template.placeInWorld((ServerLevelAccessor)level, at, at, settings, random, 2);
        return true;
    }

    private static void clearVegetationAroundStreetlight(WorldGenLevel level, int x0, int baseY, int z0, BoundingBox bounds, Set<Long> allBuildingFootprint) {
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        int minX = Math.max(x0 - 5, bounds.minX());
        int maxX = Math.min(x0 + STREETLIGHT.sizeX() + 5 - 1, bounds.maxX());
        int minZ = Math.max(z0 - 5, bounds.minZ());
        int maxZ = Math.min(z0 + STREETLIGHT.sizeZ() + 5 - 1, bounds.maxZ());
        int minY = Math.max(level.getMinBuildHeight(), baseY - 2);
        for (int x = minX; x <= maxX; ++x) {
            for (int z = minZ; z <= maxZ; ++z) {
                SurfaceColumn column = SectSettlementFeature.surfaceColumn(level, pos, x, z);
                int maxY = SectSettlementFeature.vegetationScanTop(level, baseY, column.topY(), 24);
                int hardMaxY = SectSettlementFeature.vegetationHardScanTop(level, baseY, 24);
                SectSettlementFeature.clearVegetationColumn(level, pos, x, z, minY, maxY, hardMaxY, bounds, allBuildingFootprint);
            }
        }
    }

    private static void prepareSmallFoundation(WorldGenLevel level, int x0, int baseY, int z0, int sx, int sz, BoundingBox bounds, long seed) {
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        int minX = Math.max(x0, bounds.minX());
        int maxX = Math.min(x0 + sx - 1, bounds.maxX());
        int minZ = Math.max(z0, bounds.minZ());
        int maxZ = Math.min(z0 + sz - 1, bounds.maxZ());
        for (int x = minX; x <= maxX; ++x) {
            for (int z = minZ; z <= maxZ; ++z) {
                int bottom;
                int naturalY = SectSettlementFeature.height(level, x, z);
                boolean waterSurface = SectSettlementFeature.isFluidSurface(level, pos, x, naturalY - 1, z);
                int depth = waterSurface ? 8 : 3;
                for (int y = bottom = Math.max(level.getMinBuildHeight(), baseY - depth); y < baseY; ++y) {
                    SectSettlementFeature.set(level, (BlockPos)pos.set(x, y, z), SectSettlementFeature.chooseFoundation(seed, x, z));
                }
                SectSettlementFeature.set(level, (BlockPos)pos.set(x, baseY, z), SectSettlementFeature.chooseFoundation(seed, x, z));
            }
        }
    }

    private static boolean placeSectTreeDecorations(WorldGenLevel level, long seed, BlockPos center, BoundingBox bounds, SectPlan plan, Set<Long> allBuildingFootprint, Set<Long> roadFootprint) {
        boolean placed = false;
        for (TreeDecorationCandidate candidate : SectSettlementFeature.planSectTreeDecorations(seed, center, plan, allBuildingFootprint, roadFootprint)) {
            if (!SectSettlementFeature.pointInsideChunk(candidate.x(), candidate.z(), bounds)) continue;
            placed |= SectSettlementFeature.placeSectDecorationTree(level, candidate, bounds, allBuildingFootprint, roadFootprint);
        }
        return placed;
    }

    private static List<TreeDecorationCandidate> planSectTreeDecorations(long seed, BlockPos center, SectPlan plan, Set<Long> allBuildingFootprint, Set<Long> roadFootprint) {
        int radius = Math.max(24, SectSettlementFeature.plannedSectRadius(plan, seed, center, allBuildingFootprint, roadFootprint) - 14);
        int radiusSq = radius * radius;
        int target = Mth.clamp((int)(4 + plan.pieces().size() / 2), (int)6, (int)14);
        int attempts = target * 18;
        RandomSource random = RandomSource.create((long)(seed ^ 0x51EC7A55L));
        List<RoadPoint> roadCenters = SectSettlementFeature.collectRoadCenters(center, plan.roads());
        Set<Long> streetlightAvoidance = SectSettlementFeature.plannedStreetlightAvoidance(seed, center, plan.roads(), allBuildingFootprint, roadFootprint);
        ArrayList<TreeDecorationCandidate> candidates = new ArrayList<TreeDecorationCandidate>();
        for (int i = 0; i < attempts && candidates.size() < target; ++i) {
            int z;
            int x;
            int dz;
            int dx = SectSettlementFeature.randomBetween(random, -radius, radius);
            if (dx * dx + (dz = SectSettlementFeature.randomBetween(random, -radius, radius)) * dz > radiusSq || !SectSettlementFeature.canPlanSectTreeAt(x = center.getX() + dx, z = center.getZ() + dz, allBuildingFootprint, roadFootprint, streetlightAvoidance, roadCenters, candidates)) continue;
            candidates.add(new TreeDecorationCandidate(x, z, 4 + random.nextInt(3), i));
        }
        return candidates;
    }

    private static Set<Long> plannedStreetlightAvoidance(long seed, BlockPos center, List<RoadLine> roads, Set<Long> allBuildingFootprint, Set<Long> roadFootprint) {
        HashSet<Long> footprint = new HashSet<Long>();
        for (StreetlightCandidate candidate : SectSettlementFeature.planStreetlightCandidates(seed, center, roads, allBuildingFootprint, roadFootprint)) {
            int diameter = 19;
            SectSettlementFeature.markFootprint(footprint, candidate.x() - 9, candidate.z() - 9, diameter, diameter);
        }
        return footprint;
    }

    private static boolean canPlanSectTreeAt(int x, int z, Set<Long> allBuildingFootprint, Set<Long> roadFootprint, Set<Long> streetlightAvoidance, List<RoadPoint> roadCenters, List<TreeDecorationCandidate> accepted) {
        for (int dx = -5; dx <= 5; ++dx) {
            for (int dz = -5; dz <= 5; ++dz) {
                long key = SectSettlementFeature.packXZ(x + dx, z + dz);
                if (!allBuildingFootprint.contains(key) && !roadFootprint.contains(key) && !streetlightAvoidance.contains(key)) continue;
                return false;
            }
        }
        int roadRadiusSq = 81;
        for (RoadPoint road : roadCenters) {
            int dz;
            int dx = road.x() - x;
            if (dx * dx + (dz = road.z() - z) * dz >= roadRadiusSq) continue;
            return false;
        }
        int spacingSq = 576;
        for (TreeDecorationCandidate candidate : accepted) {
            int dz;
            int dx = candidate.x() - x;
            if (dx * dx + (dz = candidate.z() - z) * dz >= spacingSq) continue;
            return false;
        }
        return true;
    }

    private static boolean placeSectDecorationTree(WorldGenLevel level, TreeDecorationCandidate candidate, BoundingBox bounds, Set<Long> allBuildingFootprint, Set<Long> roadFootprint) {
        int z;
        int x = candidate.x();
        if (!SectSettlementFeature.treeVolumeInsideChunk(x, z = candidate.z(), bounds)) {
            return false;
        }
        for (int dx = -5; dx <= 5; ++dx) {
            for (int dz = -5; dz <= 5; ++dz) {
                long key = SectSettlementFeature.packXZ(x + dx, z + dz);
                if (!allBuildingFootprint.contains(key) && !roadFootprint.contains(key)) continue;
                return false;
            }
        }
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        int surfaceY = SectSettlementFeature.height(level, x, z);
        int groundY = surfaceY - 1;
        if (groundY < level.getMinBuildHeight() || SectSettlementFeature.isFluidSurface(level, pos, x, surfaceY - 1, z)) {
            return false;
        }
        BlockState ground = level.getBlockState((BlockPos)pos.set(x, groundY, z));
        if (!SectSettlementFeature.canGrowSectTreeOn(ground)) {
            return false;
        }
        TreePalette palette = SectSettlementFeature.selectSectTreePalette(level, (BlockPos)pos.set(x, surfaceY, z));
        if (palette == null) {
            return false;
        }
        int baseY = groundY + 1;
        int trunkHeight = candidate.trunkHeight() + (palette.spruceShape() ? 1 : 0);
        if (!SectSettlementFeature.canPlaceSectTreeVolume(level, pos, x, baseY, z, trunkHeight, palette, bounds)) {
            return false;
        }
        for (int y = baseY; y < baseY + trunkHeight; ++y) {
            SectSettlementFeature.placeTreeBlock(level, pos, x, y, z, bounds, palette.log());
        }
        int topY = baseY + trunkHeight - 1;
        if (palette.spruceShape()) {
            SectSettlementFeature.placeSpruceTreeCanopy(level, pos, x, topY, z, bounds, palette.leaves());
        } else {
            SectSettlementFeature.placeGenericTreeCanopy(level, pos, x, topY, z, bounds, palette.leaves());
        }
        return true;
    }

    private static boolean treeVolumeInsideChunk(int x, int z, BoundingBox bounds) {
        return x - 2 >= bounds.minX() && x + 2 <= bounds.maxX() && z - 2 >= bounds.minZ() && z + 2 <= bounds.maxZ();
    }

    private static boolean canPlaceSectTreeVolume(WorldGenLevel level, BlockPos.MutableBlockPos pos, int x, int baseY, int z, int trunkHeight, TreePalette palette, BoundingBox bounds) {
        for (int y = baseY; y < baseY + trunkHeight; ++y) {
            if (SectSettlementFeature.canReplaceTreeDecoration(level, pos, x, y, z, bounds)) continue;
            return false;
        }
        int topY = baseY + trunkHeight - 1;
        if (palette.spruceShape()) {
            for (int y = topY - 3; y <= topY + 1; ++y) {
                if (SectSettlementFeature.canPlaceLeafLayer(level, pos, x, y, z, SectSettlementFeature.spruceLayerRadius(y, topY), topY, bounds)) continue;
                return false;
            }
            return true;
        }
        for (int y = topY - 2; y <= topY + 1; ++y) {
            int radius;
            int n = radius = y == topY + 1 ? 1 : 2;
            if (SectSettlementFeature.canPlaceLeafLayer(level, pos, x, y, z, radius, topY, bounds)) continue;
            return false;
        }
        return true;
    }

    private static boolean canPlaceLeafLayer(WorldGenLevel level, BlockPos.MutableBlockPos pos, int x, int y, int z, int radius, int trunkTopY, BoundingBox bounds) {
        for (int dx = -radius; dx <= radius; ++dx) {
            for (int dz = -radius; dz <= radius; ++dz) {
                if (!SectSettlementFeature.usesLeafCell(radius, dx, dz) || dx == 0 && dz == 0 && y <= trunkTopY || SectSettlementFeature.canReplaceTreeDecoration(level, pos, x + dx, y, z + dz, bounds)) continue;
                return false;
            }
        }
        return true;
    }

    private static void placeGenericTreeCanopy(WorldGenLevel level, BlockPos.MutableBlockPos pos, int x, int topY, int z, BoundingBox bounds, BlockState leaves) {
        for (int y = topY - 2; y <= topY + 1; ++y) {
            int radius = y == topY + 1 ? 1 : 2;
            SectSettlementFeature.placeLeafLayer(level, pos, x, y, z, radius, topY, bounds, leaves);
        }
    }

    private static void placeSpruceTreeCanopy(WorldGenLevel level, BlockPos.MutableBlockPos pos, int x, int topY, int z, BoundingBox bounds, BlockState leaves) {
        for (int y = topY - 3; y <= topY + 1; ++y) {
            SectSettlementFeature.placeLeafLayer(level, pos, x, y, z, SectSettlementFeature.spruceLayerRadius(y, topY), topY, bounds, leaves);
        }
    }

    private static void placeLeafLayer(WorldGenLevel level, BlockPos.MutableBlockPos pos, int x, int y, int z, int radius, int trunkTopY, BoundingBox bounds, BlockState leaves) {
        for (int dx = -radius; dx <= radius; ++dx) {
            for (int dz = -radius; dz <= radius; ++dz) {
                if (!SectSettlementFeature.usesLeafCell(radius, dx, dz) || dx == 0 && dz == 0 && y <= trunkTopY) continue;
                SectSettlementFeature.placeTreeBlock(level, pos, x + dx, y, z + dz, bounds, leaves);
            }
        }
    }

    private static int spruceLayerRadius(int y, int topY) {
        if (y == topY + 1) {
            return 0;
        }
        return y >= topY - 1 ? 1 : 2;
    }

    private static boolean usesLeafCell(int radius, int dx, int dz) {
        if (radius == 0) {
            return dx == 0 && dz == 0;
        }
        if (Math.abs(dx) == radius && Math.abs(dz) == radius && radius >= 2) {
            return false;
        }
        return Math.abs(dx) + Math.abs(dz) <= radius + 1;
    }

    private static boolean canReplaceTreeDecoration(WorldGenLevel level, BlockPos.MutableBlockPos pos, int x, int y, int z, BoundingBox bounds) {
        if (y < level.getMinBuildHeight() || y >= level.getMaxBuildHeight() || !SectSettlementFeature.pointInsideChunk(x, z, bounds)) {
            return false;
        }
        BlockState state = level.getBlockState((BlockPos)pos.set(x, y, z));
        return state.isAir() || SectSettlementFeature.shouldClearVegetation(state);
    }

    private static boolean placeTreeBlock(WorldGenLevel level, BlockPos.MutableBlockPos pos, int x, int y, int z, BoundingBox bounds, BlockState state) {
        if (!SectSettlementFeature.canReplaceTreeDecoration(level, pos, x, y, z, bounds)) {
            return false;
        }
        SectSettlementFeature.set(level, (BlockPos)pos.set(x, y, z), state);
        return true;
    }

    private static boolean canGrowSectTreeOn(BlockState ground) {
        return ground.is(Blocks.GRASS_BLOCK) || ground.is(Blocks.DIRT) || ground.is(Blocks.COARSE_DIRT) || ground.is(Blocks.ROOTED_DIRT) || ground.is(Blocks.PODZOL) || ground.is(Blocks.MYCELIUM) || ground.is(Blocks.MOSS_BLOCK) || ground.is(Blocks.MUD);
    }

    @Nullable
    private static TreePalette selectSectTreePalette(WorldGenLevel level, BlockPos pos) {
        String biomePath = level.getBiome(pos).unwrapKey().map(key -> key.location().getPath()).orElse("");
        if (biomePath.contains("desert") || biomePath.contains("badlands") || biomePath.contains("ocean") || biomePath.contains("river") || biomePath.contains("beach") || biomePath.contains("mushroom")) {
            return null;
        }
        if (biomePath.contains("cherry")) {
            return new TreePalette(Blocks.CHERRY_LOG.defaultBlockState(), SectSettlementFeature.persistentLeaves(Blocks.CHERRY_LEAVES.defaultBlockState()), false);
        }
        if (biomePath.contains("taiga") || biomePath.contains("grove") || biomePath.contains("snowy")) {
            return new TreePalette(Blocks.SPRUCE_LOG.defaultBlockState(), SectSettlementFeature.persistentLeaves(Blocks.SPRUCE_LEAVES.defaultBlockState()), true);
        }
        if (biomePath.contains("birch")) {
            return new TreePalette(Blocks.BIRCH_LOG.defaultBlockState(), SectSettlementFeature.persistentLeaves(Blocks.BIRCH_LEAVES.defaultBlockState()), false);
        }
        if (biomePath.contains("jungle") || biomePath.contains("bamboo")) {
            return new TreePalette(Blocks.JUNGLE_LOG.defaultBlockState(), SectSettlementFeature.persistentLeaves(Blocks.JUNGLE_LEAVES.defaultBlockState()), false);
        }
        if (biomePath.contains("savanna")) {
            return new TreePalette(Blocks.ACACIA_LOG.defaultBlockState(), SectSettlementFeature.persistentLeaves(Blocks.ACACIA_LEAVES.defaultBlockState()), false);
        }
        if (biomePath.contains("mangrove")) {
            return new TreePalette(Blocks.MANGROVE_LOG.defaultBlockState(), SectSettlementFeature.persistentLeaves(Blocks.MANGROVE_LEAVES.defaultBlockState()), false);
        }
        if (biomePath.contains("dark_forest")) {
            return new TreePalette(Blocks.DARK_OAK_LOG.defaultBlockState(), SectSettlementFeature.persistentLeaves(Blocks.DARK_OAK_LEAVES.defaultBlockState()), false);
        }
        return new TreePalette(Blocks.OAK_LOG.defaultBlockState(), SectSettlementFeature.persistentLeaves(Blocks.OAK_LEAVES.defaultBlockState()), false);
    }

    private static BlockState persistentLeaves(BlockState state) {
        return state.hasProperty((Property)LeavesBlock.PERSISTENT) ? (BlockState)state.setValue((Property)LeavesBlock.PERSISTENT, (Comparable)Boolean.valueOf(true)) : state;
    }

    private static int height(WorldGenLevel level, int x, int z) {
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        return SectSettlementFeature.surfaceColumn(level, pos, x, z).groundY();
    }

    private static SurfaceColumn surfaceColumn(WorldGenLevel level, BlockPos.MutableBlockPos pos, int x, int z) {
        BlockState below;
        int y;
        int min = level.getMinBuildHeight() + 4;
        int max = level.getMaxBuildHeight() - 32;
        int topY = y = Mth.clamp((int)level.getHeight(Heightmap.Types.WORLD_SURFACE_WG, x, z), (int)min, (int)max);
        while (y > min && ((below = level.getBlockState((BlockPos)pos.set(x, y - 1, z))).isAir() || SectSettlementFeature.shouldClearVegetation(below))) {
            --y;
        }
        return new SurfaceColumn(Mth.clamp((int)y, (int)min, (int)max), topY);
    }

    private static int vegetationScanTop(WorldGenLevel level, int baselineTopY, int heightmapTopY, int maxExtraAbove) {
        int min = level.getMinBuildHeight();
        int max = level.getMaxBuildHeight() - 1;
        int naturalTop = Math.max(baselineTopY, heightmapTopY) + 2;
        int hardCap = baselineTopY + maxExtraAbove;
        return Mth.clamp((int)Math.min(naturalTop, hardCap), (int)min, (int)max);
    }

    private static int vegetationHardScanTop(WorldGenLevel level, int baselineTopY, int maxExtraAbove) {
        return Mth.clamp((int)(baselineTopY + maxExtraAbove), (int)level.getMinBuildHeight(), (int)(level.getMaxBuildHeight() - 1));
    }

    private static int generatedPlacementY(ServerLevel server, int x, int z) {
        int oceanFloorY;
        int surfaceY = SectSettlementFeature.generatedHeight(server, x, z, Heightmap.Types.WORLD_SURFACE_WG);
        if (surfaceY > (oceanFloorY = SectSettlementFeature.generatedHeight(server, x, z, Heightmap.Types.OCEAN_FLOOR_WG))) {
            return Math.max(server.getMinBuildHeight() + 4, surfaceY - 1);
        }
        return surfaceY;
    }

    private static int generatedHeight(ServerLevel server, int x, int z, Heightmap.Types heightmap) {
        return Mth.clamp((int)server.getChunkSource().getGenerator().getBaseHeight(x, z, heightmap, (LevelHeightAccessor)server, server.getChunkSource().randomState()), (int)(server.getMinBuildHeight() + 4), (int)(server.getMaxBuildHeight() - 32));
    }

    private static void set(WorldGenLevel level, BlockPos pos, BlockState state) {
        level.setBlock(pos, state, 2);
    }

    private static boolean isFluidSurface(WorldGenLevel level, BlockPos.MutableBlockPos pos, int x, int surfaceY, int z) {
        return SectSettlementFeature.isFluidAt(level, pos, x, surfaceY, z) || SectSettlementFeature.isFluidAt(level, pos, x, surfaceY - 1, z);
    }

    private static boolean isFluidAt(WorldGenLevel level, BlockPos.MutableBlockPos pos, int x, int y, int z) {
        if (y < level.getMinBuildHeight() || y >= level.getMaxBuildHeight()) {
            return false;
        }
        return !level.getBlockState((BlockPos)pos.set(x, y, z)).getFluidState().isEmpty();
    }

    private static boolean shouldReplaceSupport(BlockState state, boolean waterSupport) {
        return state.isAir() || !state.getFluidState().isEmpty() || waterSupport && SectSettlementFeature.shouldClearVegetation(state);
    }

    private static boolean planIntersectsChunk(SectPlan plan, BlockPos center, BoundingBox bounds) {
        for (PlacedPiece piece : plan.pieces()) {
            if (!SectSettlementFeature.pieceIntersectsChunk(center, piece, bounds)) continue;
            return true;
        }
        for (RoadLine road : plan.roads()) {
            if (!SectSettlementFeature.roadIntersectsChunk(center, road, bounds)) continue;
            return true;
        }
        return false;
    }

    private static boolean sectGroundsIntersectsChunk(BlockPos center, int radius, BoundingBox bounds) {
        int dz;
        int clearRadius = radius + 8;
        int closestX = Mth.clamp((int)center.getX(), (int)bounds.minX(), (int)bounds.maxX());
        int closestZ = Mth.clamp((int)center.getZ(), (int)bounds.minZ(), (int)bounds.maxZ());
        int dx = closestX - center.getX();
        return dx * dx + (dz = closestZ - center.getZ()) * dz <= clearRadius * clearRadius;
    }

    private static boolean pieceIntersectsChunk(BlockPos center, PlacedPiece piece, BoundingBox bounds) {
        int x0 = center.getX() + piece.localX();
        int z0 = center.getZ() + piece.localZ();
        int margin = 2;
        return SectSettlementFeature.rectIntersectsChunk(x0 - margin, z0 - margin, SectSettlementFeature.placedSizeX(piece) + margin * 2, SectSettlementFeature.placedSizeZ(piece) + margin * 2, bounds);
    }

    private static boolean roadIntersectsChunk(BlockPos center, RoadLine road, BoundingBox bounds) {
        int margin = 2;
        int minX = center.getX() + Math.min(road.x1(), road.x2()) - margin;
        int maxX = center.getX() + Math.max(road.x1(), road.x2()) + margin;
        int minZ = center.getZ() + Math.min(road.z1(), road.z2()) - margin;
        int maxZ = center.getZ() + Math.max(road.z1(), road.z2()) + margin;
        return minX <= bounds.maxX() && maxX >= bounds.minX() && minZ <= bounds.maxZ() && maxZ >= bounds.minZ();
    }

    private static boolean rectIntersectsChunk(int x0, int z0, int sx, int sz, BoundingBox bounds) {
        return x0 <= bounds.maxX() && x0 + sx - 1 >= bounds.minX() && z0 <= bounds.maxZ() && z0 + sz - 1 >= bounds.minZ();
    }

    private static void markPlanFootprints(SectPlan plan, BlockPos center, Set<Long> footprint) {
        for (PlacedPiece piece : plan.pieces()) {
            if (!piece.blocksRoads()) continue;
            int x0 = center.getX() + piece.localX();
            int z0 = center.getZ() + piece.localZ();
            int sizeX = SectSettlementFeature.placedSizeX(piece);
            int sizeZ = SectSettlementFeature.placedSizeZ(piece);
            SectSettlementFeature.markFootprint(footprint, x0 - 1, z0 - 1, sizeX + 2, sizeZ + 2);
            SectSettlementFeature.unmarkConnectorApproach(footprint, x0, z0, sizeX, sizeZ, center.getX() + piece.connectorX(), center.getZ() + piece.connectorZ());
            if (!piece.piece().equals(PROTECTION_ARRAY)) continue;
            SectSettlementFeature.unmarkConnectorApproach(footprint, x0, z0, sizeX, sizeZ, x0 + sizeX / 2, z0 + sizeZ);
        }
    }

    private static void markFoundationConnectorFootprints(SectPlan plan, BlockPos center, Set<Long> footprint) {
        for (PlacedPiece piece : plan.pieces()) {
            int x0 = center.getX() + piece.localX();
            int z0 = center.getZ() + piece.localZ();
            int sizeX = SectSettlementFeature.placedSizeX(piece);
            int sizeZ = SectSettlementFeature.placedSizeZ(piece);
            if (piece.piece().equals(GATE)) {
                SectSettlementFeature.markGateConnectorOpenings(footprint, piece, x0, z0, sizeX, sizeZ);
                continue;
            }
            if (!piece.blocksRoads()) continue;
            SectSettlementFeature.markConnectorEdgeOpening(footprint, x0, z0, sizeX, sizeZ, center.getX() + piece.connectorX(), center.getZ() + piece.connectorZ());
            if (!piece.piece().equals(PROTECTION_ARRAY)) continue;
            SectSettlementFeature.markConnectorEdgeOpening(footprint, x0, z0, sizeX, sizeZ, x0 + sizeX / 2, z0 + sizeZ);
        }
    }

    private static void markGateConnectorOpenings(Set<Long> footprint, PlacedPiece piece, int x0, int z0, int sizeX, int sizeZ) {
        GatePass pass = SectSettlementFeature.gatePass(piece.rotation());
        if (pass.axis() == Direction.Axis.X) {
            int centerZ = SectSettlementFeature.clampedConnectorZ(z0, sizeZ, z0 + pass.z());
            SectSettlementFeature.markFootprint(footprint, x0 - 1, centerZ - 1, 1, 3);
            SectSettlementFeature.markFootprint(footprint, x0 + sizeX, centerZ - 1, 1, 3);
        } else {
            int centerX = SectSettlementFeature.clampedConnectorX(x0, sizeX, x0 + pass.x());
            SectSettlementFeature.markFootprint(footprint, centerX - 1, z0 - 1, 3, 1);
            SectSettlementFeature.markFootprint(footprint, centerX - 1, z0 + sizeZ, 3, 1);
        }
    }

    private static void markConnectorEdgeOpening(Set<Long> footprint, int x0, int z0, int sizeX, int sizeZ, int connectorX, int connectorZ) {
        if (connectorZ < z0) {
            int centerX = SectSettlementFeature.clampedConnectorX(x0, sizeX, connectorX);
            SectSettlementFeature.markFootprint(footprint, centerX - 1, z0 - 1, 3, 1);
            return;
        }
        if (connectorZ >= z0 + sizeZ) {
            int centerX = SectSettlementFeature.clampedConnectorX(x0, sizeX, connectorX);
            SectSettlementFeature.markFootprint(footprint, centerX - 1, z0 + sizeZ, 3, 1);
            return;
        }
        if (connectorX < x0) {
            int centerZ = SectSettlementFeature.clampedConnectorZ(z0, sizeZ, connectorZ);
            SectSettlementFeature.markFootprint(footprint, x0 - 1, centerZ - 1, 1, 3);
            return;
        }
        if (connectorX >= x0 + sizeX) {
            int centerZ = SectSettlementFeature.clampedConnectorZ(z0, sizeZ, connectorZ);
            SectSettlementFeature.markFootprint(footprint, x0 + sizeX, centerZ - 1, 1, 3);
            return;
        }
        int north = Math.abs(connectorZ - z0);
        int south = Math.abs(connectorZ - (z0 + sizeZ - 1));
        int west = Math.abs(connectorX - x0);
        int east = Math.abs(connectorX - (x0 + sizeX - 1));
        int closest = Math.min(Math.min(north, south), Math.min(west, east));
        if (closest == north) {
            int centerX = SectSettlementFeature.clampedConnectorX(x0, sizeX, connectorX);
            SectSettlementFeature.markFootprint(footprint, centerX - 1, z0 - 1, 3, 1);
        } else if (closest == south) {
            int centerX = SectSettlementFeature.clampedConnectorX(x0, sizeX, connectorX);
            SectSettlementFeature.markFootprint(footprint, centerX - 1, z0 + sizeZ, 3, 1);
        } else if (closest == west) {
            int centerZ = SectSettlementFeature.clampedConnectorZ(z0, sizeZ, connectorZ);
            SectSettlementFeature.markFootprint(footprint, x0 - 1, centerZ - 1, 1, 3);
        } else {
            int centerZ = SectSettlementFeature.clampedConnectorZ(z0, sizeZ, connectorZ);
            SectSettlementFeature.markFootprint(footprint, x0 + sizeX, centerZ - 1, 1, 3);
        }
    }

    private static void unmarkConnectorApproach(Set<Long> footprint, int x0, int z0, int sizeX, int sizeZ, int connectorX, int connectorZ) {
        if (connectorZ < z0) {
            int centerX = SectSettlementFeature.clampedConnectorX(x0, sizeX, connectorX);
            SectSettlementFeature.unmarkFootprint(footprint, centerX - 1, z0 - 2, 3, 2);
            return;
        }
        if (connectorZ >= z0 + sizeZ) {
            int centerX = SectSettlementFeature.clampedConnectorX(x0, sizeX, connectorX);
            SectSettlementFeature.unmarkFootprint(footprint, centerX - 1, z0 + sizeZ, 3, 2);
            return;
        }
        if (connectorX < x0) {
            int centerZ = SectSettlementFeature.clampedConnectorZ(z0, sizeZ, connectorZ);
            SectSettlementFeature.unmarkFootprint(footprint, x0 - 2, centerZ - 1, 2, 3);
            return;
        }
        if (connectorX >= x0 + sizeX) {
            int centerZ = SectSettlementFeature.clampedConnectorZ(z0, sizeZ, connectorZ);
            SectSettlementFeature.unmarkFootprint(footprint, x0 + sizeX, centerZ - 1, 2, 3);
        }
    }

    private static int clampedConnectorX(int x0, int sizeX, int connectorX) {
        if (sizeX <= 5) {
            return x0 + sizeX / 2;
        }
        return Mth.clamp((int)connectorX, (int)(x0 + 2), (int)(x0 + sizeX - 3));
    }

    private static int clampedConnectorZ(int z0, int sizeZ, int connectorZ) {
        if (sizeZ <= 5) {
            return z0 + sizeZ / 2;
        }
        return Mth.clamp((int)connectorZ, (int)(z0 + 2), (int)(z0 + sizeZ - 3));
    }

    private static void markGateSupportFootprints(SectPlan plan, BlockPos center, Set<Long> footprint) {
        for (PlacedPiece piece : plan.pieces()) {
            if (!piece.piece().equals(GATE)) continue;
            for (int supportZOffset : GATE_SUPPORT_ZS) {
                LocalXZ support = SectSettlementFeature.rotateLocal(GATE, piece.rotation(), 4, supportZOffset);
                int supportX = center.getX() + piece.localX() + support.x();
                int supportZ = center.getZ() + piece.localZ() + support.z();
                for (int[] offset : GATE_SUPPORT_OFFSETS) {
                    footprint.add(SectSettlementFeature.packXZ(supportX + offset[0], supportZ + offset[1]));
                }
            }
        }
    }

    private static void markAllPlanFootprints(SectPlan plan, BlockPos center, Set<Long> footprint) {
        for (PlacedPiece piece : plan.pieces()) {
            SectSettlementFeature.markFootprint(footprint, center.getX() + piece.localX(), center.getZ() + piece.localZ(), SectSettlementFeature.placedSizeX(piece), SectSettlementFeature.placedSizeZ(piece));
        }
    }

    private static void markRoadFootprints(List<RoadLine> roads, BlockPos center, Set<Long> roadBlockingFootprint, Set<Long> footprint) {
        for (RoadLine road : roads) {
            for (RoadPoint point : SectSettlementFeature.collectRoadPoints(road)) {
                int worldX = center.getX() + point.x();
                int worldZ = center.getZ() + point.z();
                for (int x = worldX - 1; x <= worldX + 1; ++x) {
                    for (int z = worldZ - 1; z <= worldZ + 1; ++z) {
                        long key = SectSettlementFeature.packXZ(x, z);
                        if (roadBlockingFootprint.contains(key)) continue;
                        footprint.add(key);
                    }
                }
            }
        }
    }

    private static List<RoadPoint> collectRoadCenters(BlockPos center, List<RoadLine> roads) {
        ArrayList<RoadPoint> centers = new ArrayList<RoadPoint>();
        for (RoadLine road : roads) {
            for (RoadPoint point : SectSettlementFeature.collectRoadPoints(road)) {
                centers.add(new RoadPoint(center.getX() + point.x(), center.getZ() + point.z()));
            }
        }
        return centers;
    }

    private static Map<PlacedPiece, Integer> collectPieceBaseYs(ServerLevel server, long seed, BlockPos center, SectPlan plan) {
        LinkedHashMap<PlacedPiece, Integer> baseYs = new LinkedHashMap<PlacedPiece, Integer>();
        for (PlacedPiece piece : plan.pieces()) {
            int worldX = center.getX() + piece.localX();
            int worldZ = center.getZ() + piece.localZ();
            baseYs.put(piece, SectSettlementFeature.pieceBaseY(server, seed, center, plan, piece, worldX, worldZ));
        }
        return Map.copyOf(baseYs);
    }

    private static int cachedPieceBaseY(Map<PlacedPiece, Integer> pieceBaseYs, ServerLevel server, long seed, BlockPos center, SectPlan plan, PlacedPiece piece) {
        Integer cached = pieceBaseYs.get(piece);
        if (cached != null) {
            return cached;
        }
        int worldX = center.getX() + piece.localX();
        int worldZ = center.getZ() + piece.localZ();
        return SectSettlementFeature.pieceBaseY(server, seed, center, plan, piece, worldX, worldZ);
    }

    private static List<RoadAnchor> collectRoadAnchors(ServerLevel server, long seed, BlockPos center, SectPlan plan, Map<PlacedPiece, Integer> pieceBaseYs) {
        ArrayList<RoadAnchor> anchors = new ArrayList<RoadAnchor>();
        for (PlacedPiece piece : plan.pieces()) {
            if (SectSettlementFeature.isAncestorCave(piece.piece())) continue;
            int worldX = center.getX() + piece.localX();
            int worldZ = center.getZ() + piece.localZ();
            int baseY = SectSettlementFeature.cachedPieceBaseY(pieceBaseYs, server, seed, center, plan, piece);
            anchors.add(new RoadAnchor(center.getX() + piece.connectorX(), center.getZ() + piece.connectorZ(), baseY));
        }
        return List.copyOf(anchors);
    }

    private static List<RoadPoint> collectRoadPoints(RoadLine road) {
        int x;
        ArrayList<RoadPoint> centers = new ArrayList<RoadPoint>();
        int z = road.z1();
        int dx = Integer.compare(road.x2(), road.x1());
        for (x = road.x1(); x != road.x2(); x += dx) {
            centers.add(new RoadPoint(x, z));
        }
        int dz = Integer.compare(road.z2(), road.z1());
        while (z != road.z2()) {
            centers.add(new RoadPoint(x, z));
            z += dz;
        }
        centers.add(new RoadPoint(x, z));
        return centers;
    }

    private static BlockState chooseFoundation(long seed, int x, int z) {
        int roll = Math.floorMod(SectSettlementFeature.seedFor(seed, x, z, 3850), 100);
        if (roll < 42) {
            return PAVING_STONE;
        }
        if (roll < 80) {
            return PAVING_ANDESITE;
        }
        if (roll < 93) {
            return PAVING_MOSSY_STONE_BRICKS;
        }
        return PAVING_STONE_BRICKS;
    }

    private static BlockState chooseEdgeSupport(long seed, int x, int z) {
        int roll = Math.floorMod(SectSettlementFeature.seedFor(seed, x, z, 3694), 100);
        return roll < 24 ? PAVING_MOSSY_STONE_BRICKS : PAVING_STONE_BRICKS;
    }

    private static int randomBetween(RandomSource random, int min, int max) {
        return min + random.nextInt(Math.max(1, max - min + 1));
    }

    private static long seedFor(long seed, int x, int z, int salt) {
        long value = seed ^ (long)x * 341873128712L ^ (long)z * 132897987541L ^ (long)salt * -7046029254386353131L;
        value ^= value >>> 33;
        value *= -49064778989728563L;
        value ^= value >>> 33;
        value *= -4265267296055464877L;
        value ^= value >>> 33;
        return value;
    }

    private static void markFootprint(Set<Long> footprint, int x0, int z0, int sx, int sz) {
        for (int x = x0; x < x0 + sx; ++x) {
            for (int z = z0; z < z0 + sz; ++z) {
                footprint.add(SectSettlementFeature.packXZ(x, z));
            }
        }
    }

    private static void unmarkFootprint(Set<Long> footprint, int x0, int z0, int sx, int sz) {
        for (int x = x0; x < x0 + sx; ++x) {
            for (int z = z0; z < z0 + sz; ++z) {
                footprint.remove(SectSettlementFeature.packXZ(x, z));
            }
        }
    }

    private static long packXZ(int x, int z) {
        return (long)x << 32 ^ (long)z & 0xFFFFFFFFL;
    }

    private static PieceSpec piece(String path, int sizeX, int sizeY, int sizeZ) {
        return SectSettlementFeature.piece(path, sizeX, sizeY, sizeZ, 0);
    }

    private static PieceSpec piece(String path, int sizeX, int sizeY, int sizeZ, int templateYOffset) {
        return new PieceSpec(new ResourceLocation((String)"friday_cultivation", (String)("sect/" + path)), sizeX, sizeY, sizeZ, templateYOffset);
    }

    private record CellPlan(boolean enabled, int targetChunkX, int targetChunkZ, long seed) {
        boolean shouldGenerateIn(int chunkX, int chunkZ) {
            return this.enabled && Math.abs(chunkX - this.targetChunkX) <= 13 && Math.abs(chunkZ - this.targetChunkZ) <= 13;
        }
    }

    private record CachedCell(SectPlan plan, boolean hasArray, Set<Long> roadBlockingFootprint, Set<Long> foundationConnectorFootprint, Set<Long> allBuildingFootprint, Set<Long> roadFootprint, Map<PlacedPiece, Integer> pieceBaseYs, List<RoadAnchor> roadAnchors, List<StreetlightCandidate> streetlightCandidates, int sectRadius, boolean biomeGatePass) {
    }

    private record SectPlan(List<PlacedPiece> pieces, List<RoadLine> roads) {
    }

    private record SectGeneration(String id, String name, int radius, boolean hasArray, SectSavedData data) {
    }

    private record PlacedPiece(PieceSpec piece, int localX, int localZ, int connectorX, int connectorZ, boolean blocksRoads, boolean required, Rotation rotation) {
    }

    private record RoadLine(int x1, int z1, int x2, int z2) {
    }

    private record PieceSpec(ResourceLocation id, int sizeX, int sizeY, int sizeZ, int templateYOffset) {
    }

    private static final class PlanBuilder {
        private final List<PlacedPiece> pieces = new ArrayList<PlacedPiece>();
        private final List<RoadLine> roads = new ArrayList<RoadLine>();
        private final List<Rect> occupied = new ArrayList<Rect>();

        private PlanBuilder() {
        }

        void addPiece(PieceSpec piece, int x, int z, int connectorX, int connectorZ, boolean blocksRoads, boolean required) {
            this.addPiece(piece, x, z, connectorX, connectorZ, blocksRoads, required, Rotation.NONE);
        }

        void addPiece(PieceSpec piece, int x, int z, int connectorX, int connectorZ, boolean blocksRoads, boolean required, Rotation rotation) {
            this.pieces.add(new PlacedPiece(piece, x, z, connectorX, connectorZ, blocksRoads, required, rotation));
            this.occupied.add(new Rect(x, z, SectSettlementFeature.placedSizeX(piece, rotation), SectSettlementFeature.placedSizeZ(piece, rotation)).expand(3));
        }

        boolean tryAddPiece(PieceSpec piece, int x, int z, int connectorX, int connectorZ, boolean blocksRoads, boolean required) {
            return this.tryAddPiece(piece, x, z, connectorX, connectorZ, blocksRoads, required, Rotation.NONE);
        }

        boolean tryAddPiece(PieceSpec piece, int x, int z, int connectorX, int connectorZ, boolean blocksRoads, boolean required, Rotation rotation) {
            Rect rect = new Rect(x, z, SectSettlementFeature.placedSizeX(piece, rotation), SectSettlementFeature.placedSizeZ(piece, rotation));
            if (!rect.insidePlanBounds()) {
                return false;
            }
            Rect padded = rect.expand(3);
            for (Rect existing : this.occupied) {
                if (!padded.intersects(existing)) continue;
                return false;
            }
            this.addPiece(piece, x, z, connectorX, connectorZ, blocksRoads, required, rotation);
            return true;
        }

        void addRoad(int x1, int z1, int x2, int z2) {
            if (x1 == x2 && z1 == z2) {
                return;
            }
            this.roads.add(new RoadLine(x1, z1, x2, z2));
        }

        void addWindingRoad(RandomSource random, int x1, int z1, int x2, int z2) {
            if (x1 == x2 || z1 == z2) {
                this.addWindingAxisRoad(random, x1, z1, x2, z2);
                return;
            }
            if (random.nextBoolean()) {
                int bendX = PlanBuilder.safeMiddle(random, x1, x2);
                this.addWindingAxisRoad(random, x1, z1, bendX, z1);
                this.addWindingAxisRoad(random, bendX, z1, bendX, z2);
                this.addWindingAxisRoad(random, bendX, z2, x2, z2);
            } else {
                int bendZ = PlanBuilder.safeMiddle(random, z1, z2);
                this.addWindingAxisRoad(random, x1, z1, x1, bendZ);
                this.addWindingAxisRoad(random, x1, bendZ, x2, bendZ);
                this.addWindingAxisRoad(random, x2, bendZ, x2, z2);
            }
        }

        private void addWindingAxisRoad(RandomSource random, int x1, int z1, int x2, int z2) {
            int length = Math.abs(x2 - x1) + Math.abs(z2 - z1);
            if (length < 18 || random.nextFloat() < 0.45f) {
                this.addRoad(x1, z1, x2, z2);
                return;
            }
            if (z1 == z2) {
                int dir = Integer.signum(x2 - x1);
                int firstX = x1 + dir * Math.max(5, length / 3);
                int secondX = x1 + dir * Math.max(9, length * 2 / 3);
                int offsetZ = Mth.clamp((int)(z1 + PlanBuilder.randomOffset(random)), (int)-106, (int)106);
                this.addRoad(x1, z1, firstX, z1);
                this.addRoad(firstX, z1, firstX, offsetZ);
                this.addRoad(firstX, offsetZ, secondX, offsetZ);
                this.addRoad(secondX, offsetZ, secondX, z2);
                this.addRoad(secondX, z2, x2, z2);
                return;
            }
            if (x1 == x2) {
                int dir = Integer.signum(z2 - z1);
                int firstZ = z1 + dir * Math.max(5, length / 3);
                int secondZ = z1 + dir * Math.max(9, length * 2 / 3);
                int offsetX = Mth.clamp((int)(x1 + PlanBuilder.randomOffset(random)), (int)-106, (int)106);
                this.addRoad(x1, z1, x1, firstZ);
                this.addRoad(x1, firstZ, offsetX, firstZ);
                this.addRoad(offsetX, firstZ, offsetX, secondZ);
                this.addRoad(offsetX, secondZ, x2, secondZ);
                this.addRoad(x2, secondZ, x2, z2);
            }
        }

        private static int safeMiddle(RandomSource random, int a, int b) {
            int min = Math.min(a, b);
            int max = Math.max(a, b);
            if (max - min < 8) {
                return (a + b) / 2;
            }
            return Mth.clamp((int)((a + b) / 2 + SectSettlementFeature.randomBetween(random, -5, 5)), (int)(min + 3), (int)(max - 3));
        }

        private static int randomOffset(RandomSource random) {
            int amount = SectSettlementFeature.randomBetween(random, 2, 4);
            return random.nextBoolean() ? amount : -amount;
        }

        SectPlan toPlan() {
            return new SectPlan(List.copyOf(this.pieces), List.copyOf(this.roads));
        }
    }

    private record GatePass(Direction.Axis axis, int x, int z) {
    }

    private record RoadTarget(int x, int z, int side) {
    }

    private record StreetlightCandidate(int x, int z, int roadX, int roadZ, int index) {
    }

    private static final class DeferredNpcSpawnTask {
        private final ServerLevel server;
        private final String spawnKey;
        private final long seed;
        private final SectGeneration sect;
        private final SectRole role;
        private final BlockPos spawnPos;
        private final BlockPos homePos;
        @Nullable
        private final BlockPos fixedBedPos;
        @Nullable
        private final BlockPos fixedCushionPos;
        private final boolean addTemporaryTokens;
        @Nullable
        private final BlockPos receptionGuardPos;
        @Nullable
        private final PlacedPiece routinePiece;
        @Nullable
        private final BoundingBox routineBounds;
        private final int routineWorldX;
        private final int routineBaseY;
        private final int routineWorldZ;
        private int attempts;

        private DeferredNpcSpawnTask(ServerLevel server, String spawnKey, long seed, SectGeneration sect, SectRole role, BlockPos spawnPos, BlockPos homePos, @Nullable BlockPos fixedBedPos, @Nullable BlockPos fixedCushionPos, boolean addTemporaryTokens, @Nullable BlockPos receptionGuardPos, @Nullable PlacedPiece routinePiece, @Nullable BoundingBox routineBounds, int routineWorldX, int routineBaseY, int routineWorldZ) {
            this.server = server;
            this.spawnKey = spawnKey;
            this.seed = seed;
            this.sect = sect;
            this.role = role;
            this.spawnPos = spawnPos.east();
            this.homePos = homePos.east();
            this.fixedBedPos = fixedBedPos == null ? null : fixedBedPos.east();
            this.fixedCushionPos = fixedCushionPos == null ? null : fixedCushionPos.east();
            this.addTemporaryTokens = addTemporaryTokens;
            this.receptionGuardPos = receptionGuardPos == null ? null : receptionGuardPos.east();
            this.routinePiece = routinePiece;
            this.routineBounds = routineBounds;
            this.routineWorldX = routineWorldX;
            this.routineBaseY = routineBaseY;
            this.routineWorldZ = routineWorldZ;
        }

        private static DeferredNpcSpawnTask piece(ServerLevel server, String spawnKey, long seed, SectGeneration sect, SectRole role, BlockPos spawnPos, BlockPos homePos, boolean addTemporaryTokens, PlacedPiece routinePiece, BoundingBox routineBounds, int routineWorldX, int routineBaseY, int routineWorldZ) {
            return new DeferredNpcSpawnTask(server, spawnKey, seed, sect, role, spawnPos, homePos, null, null, addTemporaryTokens, null, routinePiece, routineBounds, routineWorldX, routineBaseY, routineWorldZ);
        }

        private static DeferredNpcSpawnTask simple(ServerLevel server, String spawnKey, long seed, SectGeneration sect, SectRole role, BlockPos spawnPos, BlockPos homePos, @Nullable BlockPos bedPos, @Nullable BlockPos cushionPos, boolean addTemporaryTokens, @Nullable BlockPos receptionGuardPos) {
            return new DeferredNpcSpawnTask(server, spawnKey, seed, sect, role, spawnPos, homePos, bedPos, cushionPos, addTemporaryTokens, receptionGuardPos, null, null, 0, 0, 0);
        }

        private String pendingKey() {
            return String.valueOf(this.server.dimension().location()) + "|" + this.sect.id() + "|" + this.spawnKey;
        }

        private int incrementAttempts() {
            ++this.attempts;
            return this.attempts;
        }

        private int attempts() {
            return this.attempts;
        }

        private Result trySpawn() {
            if (!this.server.hasChunkAt(this.spawnPos)) {
                return Result.RETRY;
            }
            return this.spawnNow();
        }

        private Result spawnNow() {
            WanderingCultivatorEntity npc;
            if (!this.sect.data().claimSpawn(this.sect.id(), this.spawnKey)) {
                return Result.DONE;
            }
            BlockPos bedPos = this.fixedBedPos;
            BlockPos cushionPos = this.fixedCushionPos;
            if (this.routinePiece != null && this.routineBounds != null) {
                RoutineTargetLookup routineTargets = new RoutineTargetLookup((LevelAccessor)this.server, this.routinePiece, this.routineBounds, this.routineWorldX, this.routineBaseY, this.routineWorldZ);
                bedPos = SectSettlementFeature.findRoutineTargetInPiece(this.sect, routineTargets.candidates(false, this.homePos), false);
                cushionPos = SectSettlementFeature.findRoutineTargetInPiece(this.sect, routineTargets.candidates(true, this.homePos), true);
            }
            if ((npc = SectSettlementFeature.spawnSectNpc(this.server, this.seed, this.sect, this.role, this.spawnPos, this.homePos, bedPos, cushionPos, this.addTemporaryTokens)) == null) {
                return Result.DONE;
            }
            if (this.receptionGuardPos != null) {
                npc.configureSectReceptionGuard(this.receptionGuardPos);
            }
            return Result.SPAWNED;
        }

        private static enum Result {
            RETRY,
            DONE,
            SPAWNED;

        }
    }

    private record ReceptionGuardPost(int index, int x, int z) {
    }

    @Mod.EventBusSubscriber(modid="friday_cultivation")
    public static final class DeferredSectNpcSpawner {
        private static final Queue<DeferredNpcSpawnTask> QUEUE = new ConcurrentLinkedQueue<DeferredNpcSpawnTask>();
        private static final Set<String> PENDING_KEYS = ConcurrentHashMap.newKeySet();

        private DeferredSectNpcSpawner() {
        }

        private static boolean queue(DeferredNpcSpawnTask task) {
            if (task == null || !PENDING_KEYS.add(task.pendingKey())) {
                return false;
            }
            QUEUE.add(task);
            return true;
        }

        @SubscribeEvent
        public static void onServerTick(TickEvent.ServerTickEvent event) {
            if (event.phase != TickEvent.Phase.END) {
                return;
            }
            int spawned = 0;
            int scanned = 0;
            while (spawned < 2 && scanned < 24) {
                DeferredNpcSpawnTask task = QUEUE.poll();
                if (task == null) {
                    return;
                }
                ++scanned;
                try {
                    DeferredNpcSpawnTask.Result result = task.trySpawn();
                    if (result == DeferredNpcSpawnTask.Result.RETRY) {
                        if (task.incrementAttempts() <= 2400) {
                            QUEUE.add(task);
                            continue;
                        }
                        PENDING_KEYS.remove(task.pendingKey());
                        LOGGER.warn("Dropped deferred sect NPC spawn after {} attempts: {}", (Object)task.attempts(), (Object)task.pendingKey());
                        continue;
                    }
                    PENDING_KEYS.remove(task.pendingKey());
                    if (result != DeferredNpcSpawnTask.Result.SPAWNED) continue;
                    ++spawned;
                }
                catch (Exception e) {
                    PENDING_KEYS.remove(task.pendingKey());
                    LOGGER.error("Deferred sect NPC spawn failed: {}", (Object)task.pendingKey(), (Object)e);
                }
            }
        }

        @SubscribeEvent
        public static void onServerStopping(ServerStoppingEvent event) {
            QUEUE.clear();
            PENDING_KEYS.clear();
        }
    }

    private record SurfaceColumn(int groundY, int topY) {
    }

    private record RoadPoint(int x, int z) {
    }

    private record RoadStep(Direction direction, boolean steep) {
    }

    private static enum RoadShape {
        X_AXIS,
        Z_AXIS,
        CORNER;

    }

    private record LocalXZ(int x, int z) {
    }

    private record RoadCell(int x, int y, int z, BlockState state, boolean step, boolean waterSurface) {
    }

    private record RoadAnchor(int x, int z, int y) {
    }

    private record TreeDecorationCandidate(int x, int z, int trunkHeight, int index) {
    }

    private record TreePalette(BlockState log, BlockState leaves, boolean spruceShape) {
    }

    private record Rect(int x, int z, int sx, int sz) {
        Rect expand(int amount) {
            return new Rect(this.x - amount, this.z - amount, this.sx + amount * 2, this.sz + amount * 2);
        }

        boolean intersects(Rect other) {
            return this.x <= other.x + other.sx - 1 && this.x + this.sx - 1 >= other.x && this.z <= other.z + other.sz - 1 && this.z + this.sz - 1 >= other.z;
        }

        boolean insidePlanBounds() {
            return this.x >= -112 && this.z >= -112 && this.x + this.sx <= 112 && this.z + this.sz <= 112;
        }
    }

    private static final class RoutineTargetLookup {
        private final LevelAccessor level;
        private final PlacedPiece piece;
        private final BoundingBox bounds;
        private final int worldX;
        private final int baseY;
        private final int worldZ;
        private List<BlockPos> bedCandidates;
        private List<BlockPos> cushionCandidates;

        private RoutineTargetLookup(LevelAccessor level, PlacedPiece piece, BoundingBox bounds, int worldX, int baseY, int worldZ) {
            this.level = level;
            this.piece = piece;
            this.bounds = bounds;
            this.worldX = worldX;
            this.baseY = baseY;
            this.worldZ = worldZ;
        }

        private List<BlockPos> candidates(boolean cushion, BlockPos preferred) {
            if (cushion) {
                if (this.cushionCandidates == null) {
                    this.cushionCandidates = this.sortedCandidates(true, preferred);
                }
                return this.cushionCandidates;
            }
            if (this.bedCandidates == null) {
                this.bedCandidates = this.sortedCandidates(false, preferred);
            }
            return this.bedCandidates;
        }

        private List<BlockPos> sortedCandidates(boolean cushion, BlockPos preferred) {
            List<BlockPos> candidates = SectSettlementFeature.collectRoutineTargetsInPiece(this.level, this.piece, this.bounds, this.worldX, this.baseY, this.worldZ, cushion);
            candidates.sort((a, b) -> SectSettlementFeature.compareRoutineTarget(a, b, preferred));
            return candidates;
        }
    }
}

