/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.BlockPos
 *  net.minecraft.core.Direction
 *  net.minecraft.nbt.CompoundTag
 *  net.minecraft.resources.ResourceKey
 *  net.minecraft.server.level.ServerLevel
 *  net.minecraft.world.level.BlockGetter
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.level.block.entity.BlockEntity
 *  net.minecraft.world.level.block.entity.BlockEntityType
 *  net.minecraft.world.level.block.state.BlockState
 *  net.minecraft.world.level.block.state.properties.Property
 *  org.jetbrains.annotations.NotNull
 */
package com.friday.cultivation.block.formation;

import com.friday.cultivation.block.formation.FormationCorePlateBlockEntity;
import com.friday.cultivation.block.formation.FormationFlagBlock;
import com.friday.cultivation.block.formation.FormationRuneBlock;
import com.friday.cultivation.registry.ModBlockEntities;
import com.friday.cultivation.util.QiStorageBlocks;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import org.jetbrains.annotations.NotNull;

public class FormationRuneBlockEntity
extends BlockEntity {
    private static final Map<ResourceKey<Level>, Set<FormationRuneBlockEntity>> ACTIVE_RUNES = new ConcurrentHashMap<ResourceKey<Level>, Set<FormationRuneBlockEntity>>();
    private static final Map<ResourceKey<Level>, Map<BlockPos, Set<BlockPos>>> CONNECTED_TARGETS = new ConcurrentHashMap<ResourceKey<Level>, Map<BlockPos, Set<BlockPos>>>();
    private static final Map<ResourceKey<Level>, Map<BlockPos, Map<BlockPos, List<BlockPos>>>> CONNECTED_TARGET_PATHS = new ConcurrentHashMap<ResourceKey<Level>, Map<BlockPos, Map<BlockPos, List<BlockPos>>>>();
    private static final Map<ResourceKey<Level>, Map<BlockPos, Set<BlockPos>>> CONNECTED_FLAGS = new ConcurrentHashMap<ResourceKey<Level>, Map<BlockPos, Set<BlockPos>>>();
    private static final Map<ResourceKey<Level>, Map<BlockPos, Map<BlockPos, List<BlockPos>>>> CONNECTED_FLAG_PATHS = new ConcurrentHashMap<ResourceKey<Level>, Map<BlockPos, Map<BlockPos, List<BlockPos>>>>();
    private boolean registered = false;

    public FormationRuneBlockEntity(BlockPos pos, BlockState state) {
        super((BlockEntityType)ModBlockEntities.FORMATION_RUNE.get(), pos, state);
    }

    public void serverTick() {
        this.ensureRegistered();
    }

    public void onBlockRemoved() {
        this.unregister();
    }

    public void setRemoved() {
        this.unregister();
        super.setRemoved();
    }

    public static void tickNetworks(ServerLevel level) {
        Set<FormationRuneBlockEntity> set = ACTIVE_RUNES.get(level.dimension());
        if (set == null || set.isEmpty()) {
            CONNECTED_TARGETS.remove(level.dimension());
            CONNECTED_TARGET_PATHS.remove(level.dimension());
            CONNECTED_FLAGS.remove(level.dimension());
            CONNECTED_FLAG_PATHS.remove(level.dimension());
            return;
        }
        HashMap<BlockPos, FormationRuneBlockEntity> runes = new HashMap<BlockPos, FormationRuneBlockEntity>();
        Iterator<FormationRuneBlockEntity> it = set.iterator();
        while (it.hasNext()) {
            FormationRuneBlockEntity rune = it.next();
            if (!rune.isValidFor(level)) {
                it.remove();
                continue;
            }
            runes.put(rune.getBlockPos(), rune);
        }
        if (runes.isEmpty()) {
            CONNECTED_TARGETS.remove(level.dimension());
            CONNECTED_TARGET_PATHS.remove(level.dimension());
            CONNECTED_FLAGS.remove(level.dimension());
            CONNECTED_FLAG_PATHS.remove(level.dimension());
            return;
        }
        HashSet<BlockPos> visited = new HashSet<BlockPos>();
        HashMap<BlockPos, Set<BlockPos>> nextConnections = new HashMap<BlockPos, Set<BlockPos>>();
        HashMap<BlockPos, Map<BlockPos, List<BlockPos>>> nextConnectionPaths = new HashMap<BlockPos, Map<BlockPos, List<BlockPos>>>();
        HashMap<BlockPos, Set<BlockPos>> nextFlagConnections = new HashMap<BlockPos, Set<BlockPos>>();
        HashMap<BlockPos, Map<BlockPos, List<BlockPos>>> nextFlagConnectionPaths = new HashMap<BlockPos, Map<BlockPos, List<BlockPos>>>();
        for (BlockPos start : runes.keySet()) {
            if (!visited.add(start)) continue;
            List<BlockPos> component = FormationRuneBlockEntity.collectComponent(level, runes, start, visited);
            FormationRuneBlockEntity.updateComponent(level, component, nextConnections, nextConnectionPaths, nextFlagConnections, nextFlagConnectionPaths);
        }
        if (nextConnections.isEmpty()) {
            CONNECTED_TARGETS.remove(level.dimension());
        } else {
            HashMap frozen = new HashMap();
            nextConnections.forEach((corePos, targets) -> {
                if (!targets.isEmpty()) {
                    frozen.put(corePos, Set.copyOf(targets));
                }
            });
            CONNECTED_TARGETS.put((ResourceKey<Level>)level.dimension(), Map.copyOf(frozen));
        }
        if (nextConnectionPaths.isEmpty()) {
            CONNECTED_TARGET_PATHS.remove(level.dimension());
        } else {
            HashMap frozenPaths = new HashMap();
            nextConnectionPaths.forEach((corePos, targetPaths) -> {
                HashMap frozenTargetPaths = new HashMap();
                targetPaths.forEach((targetPos, path) -> {
                    if (!path.isEmpty()) {
                        frozenTargetPaths.put(targetPos, List.copyOf(path));
                    }
                });
                if (!frozenTargetPaths.isEmpty()) {
                    frozenPaths.put(corePos, Map.copyOf(frozenTargetPaths));
                }
            });
            CONNECTED_TARGET_PATHS.put((ResourceKey<Level>)level.dimension(), Map.copyOf(frozenPaths));
        }
        if (nextFlagConnections.isEmpty()) {
            CONNECTED_FLAGS.remove(level.dimension());
        } else {
            HashMap frozen = new HashMap();
            nextFlagConnections.forEach((corePos, flags) -> {
                if (!flags.isEmpty()) {
                    frozen.put(corePos, Set.copyOf(flags));
                }
            });
            CONNECTED_FLAGS.put((ResourceKey<Level>)level.dimension(), Map.copyOf(frozen));
        }
        if (nextFlagConnectionPaths.isEmpty()) {
            CONNECTED_FLAG_PATHS.remove(level.dimension());
        } else {
            HashMap frozenPaths = new HashMap();
            nextFlagConnectionPaths.forEach((corePos, targetPaths) -> {
                HashMap frozenTargetPaths = new HashMap();
                targetPaths.forEach((targetPos, path) -> {
                    if (!path.isEmpty()) {
                        frozenTargetPaths.put(targetPos, List.copyOf(path));
                    }
                });
                if (!frozenTargetPaths.isEmpty()) {
                    frozenPaths.put(corePos, Map.copyOf(frozenTargetPaths));
                }
            });
            CONNECTED_FLAG_PATHS.put((ResourceKey<Level>)level.dimension(), Map.copyOf(frozenPaths));
        }
    }

    public static Set<BlockPos> connectedStorageTargets(ServerLevel level, BlockPos corePos) {
        Map<BlockPos, Set<BlockPos>> byCore = CONNECTED_TARGETS.get(level.dimension());
        if (byCore == null || byCore.isEmpty()) {
            return Collections.emptySet();
        }
        return byCore.getOrDefault(corePos, Collections.emptySet());
    }

    public static Map<BlockPos, List<BlockPos>> connectedStorageTargetPaths(ServerLevel level, BlockPos corePos) {
        Map<BlockPos, Map<BlockPos, List<BlockPos>>> byCore = CONNECTED_TARGET_PATHS.get(level.dimension());
        if (byCore == null || byCore.isEmpty()) {
            return Collections.emptyMap();
        }
        return byCore.getOrDefault(corePos, Collections.emptyMap());
    }

    public static Set<BlockPos> connectedFormationFlags(ServerLevel level, BlockPos corePos) {
        Map<BlockPos, Set<BlockPos>> byCore = CONNECTED_FLAGS.get(level.dimension());
        if (byCore == null || byCore.isEmpty()) {
            return Collections.emptySet();
        }
        return byCore.getOrDefault(corePos, Collections.emptySet());
    }

    public static Map<BlockPos, List<BlockPos>> connectedFormationFlagPaths(ServerLevel level, BlockPos corePos) {
        Map<BlockPos, Map<BlockPos, List<BlockPos>>> byCore = CONNECTED_FLAG_PATHS.get(level.dimension());
        if (byCore == null || byCore.isEmpty()) {
            return Collections.emptyMap();
        }
        return byCore.getOrDefault(corePos, Collections.emptyMap());
    }

    private static List<BlockPos> collectComponent(ServerLevel level, Map<BlockPos, FormationRuneBlockEntity> runes, BlockPos start, Set<BlockPos> visited) {
        ArrayList<BlockPos> component = new ArrayList<BlockPos>();
        ArrayDeque<BlockPos> queue = new ArrayDeque<BlockPos>();
        queue.add(start);
        while (!queue.isEmpty()) {
            BlockPos pos = (BlockPos)queue.removeFirst();
            component.add(pos);
            BlockState state = level.getBlockState(pos);
            for (Direction dir : Direction.values()) {
                BlockState nextState;
                BlockPos next = pos.relative(dir);
                if (!runes.containsKey(next) || visited.contains(next) || !FormationRuneBlock.canRuneConnect(state, pos, nextState = level.getBlockState(next), next)) continue;
                visited.add(next);
                queue.addLast(next);
            }
        }
        return component;
    }

    private static void updateComponent(ServerLevel level, List<BlockPos> component, Map<BlockPos, Set<BlockPos>> nextConnections, Map<BlockPos, Map<BlockPos, List<BlockPos>>> nextConnectionPaths, Map<BlockPos, Set<BlockPos>> nextFlagConnections, Map<BlockPos, Map<BlockPos, List<BlockPos>>> nextFlagConnectionPaths) {
        boolean hasPoweredFormationCircuit;
        HashSet<BlockPos> sourceCores = new HashSet<BlockPos>();
        HashSet<BlockPos> storageTargets = new HashSet<BlockPos>();
        HashSet<BlockPos> litBefore = new HashSet<BlockPos>();
        HashMap<BlockPos, Set<BlockPos>> sourceRunesByCore = new HashMap<BlockPos, Set<BlockPos>>();
        HashSet<BlockPos> formationCores = new HashSet<BlockPos>();
        HashSet<BlockPos> poweredFormationCores = new HashSet<BlockPos>();
        HashSet<BlockPos> formationFlags = new HashSet<BlockPos>();
        HashMap<BlockPos, Set<BlockPos>> formationRunesByCore = new HashMap<BlockPos, Set<BlockPos>>();
        HashMap<BlockPos, Set<BlockPos>> flagRunesByFlag = new HashMap<BlockPos, Set<BlockPos>>();
        for (BlockPos pos : component) {
            BlockState state = level.getBlockState(pos);
            if (!(state.getBlock() instanceof FormationRuneBlock)) continue;
            Set<BlockPos> set = FormationRuneBlockEntity.collectTouchedEndpoints(level, pos, state, true);
            sourceCores.addAll(set);
            for (BlockPos blockPos : set) {
                sourceRunesByCore.computeIfAbsent(blockPos, ignored -> new HashSet()).add(pos);
            }
            storageTargets.addAll(FormationRuneBlockEntity.collectTouchedEndpoints(level, pos, state, false));
            Set<BlockPos> touchedFormationCores = FormationRuneBlockEntity.collectTouchedFormationCores(level, pos, state);
            formationCores.addAll(touchedFormationCores);
            Iterator iterator = touchedFormationCores.iterator();
            while (iterator.hasNext()) {
                BlockPos core = (BlockPos)iterator.next();
                formationRunesByCore.computeIfAbsent(core, ignored -> new HashSet()).add(pos);
                if (!FormationRuneBlockEntity.formationCoreHasQi(level, core)) continue;
                poweredFormationCores.add(core);
            }
            Set<BlockPos> set2 = FormationRuneBlockEntity.collectTouchedFormationFlags(level, pos, state);
            formationFlags.addAll(set2);
            for (BlockPos flag : set2) {
                flagRunesByFlag.computeIfAbsent(flag, ignored -> new HashSet()).add(pos);
            }
            if (!((Boolean)state.getValue(FormationRuneBlock.LIT)).booleanValue()) continue;
            litBefore.add(pos);
        }
        if (!formationCores.isEmpty() && !formationFlags.isEmpty()) {
            for (BlockPos core : formationCores) {
                Set sourceRunes = formationRunesByCore.getOrDefault(core, Collections.emptySet());
                Map map = nextFlagConnectionPaths.computeIfAbsent(core, ignored -> new HashMap());
                for (BlockPos blockPos : formationFlags) {
                    Set<BlockPos> targetRunes2 = flagRunesByFlag.getOrDefault(blockPos, Collections.emptySet());
                    List<BlockPos> path = FormationRuneBlockEntity.shortestPath(level, Set.copyOf(component), sourceRunes, targetRunes2);
                    if (path.isEmpty()) continue;
                    map.put(blockPos, path);
                    nextFlagConnections.computeIfAbsent(core, ignored -> new HashSet()).add(blockPos);
                }
            }
        }
        boolean hasStorageCircuit = !sourceCores.isEmpty() && !storageTargets.isEmpty();
        boolean bl = hasPoweredFormationCircuit = !poweredFormationCores.isEmpty() && !formationFlags.isEmpty();
        if (!hasStorageCircuit && !hasPoweredFormationCircuit) {
            for (BlockPos blockPos : component) {
                FormationRuneBlockEntity.setLit(level, blockPos, false);
            }
            return;
        }
        HashSet<BlockPos> shouldLight = new HashSet<BlockPos>(litBefore);
        for (BlockPos pos : component) {
            BlockState blockState = level.getBlockState(pos);
            if (!(blockState.getBlock() instanceof FormationRuneBlock)) continue;
            if (FormationRuneBlock.touchesSpiritVeinCore((BlockGetter)level, pos, blockState)) {
                shouldLight.add(pos);
                continue;
            }
            if (FormationRuneBlockEntity.touchesPoweredFormationCore(level, pos, blockState)) {
                shouldLight.add(pos);
                continue;
            }
            if (!FormationRuneBlockEntity.hasLitNeighbor(level, pos, blockState, litBefore)) continue;
            shouldLight.add(pos);
        }
        for (BlockPos pos : component) {
            FormationRuneBlockEntity.setLit(level, pos, shouldLight.contains(pos));
        }
        HashSet<BlockPos> hashSet = new HashSet<BlockPos>();
        HashMap<BlockPos, Set<BlockPos>> litStorageRunesByTarget = new HashMap<BlockPos, Set<BlockPos>>();
        for (BlockPos pos : component) {
            BlockState state;
            if (!shouldLight.contains(pos) || !((state = level.getBlockState(pos)).getBlock() instanceof FormationRuneBlock)) continue;
            Set<BlockPos> touchedTargets = FormationRuneBlockEntity.collectTouchedEndpoints(level, pos, state, false);
            hashSet.addAll(touchedTargets);
            for (BlockPos target2 : touchedTargets) {
                litStorageRunesByTarget.computeIfAbsent(target2, ignored -> new HashSet<BlockPos>()).add(pos);
            }
        }
        if (hashSet.isEmpty()) {
            return;
        }
        for (BlockPos sourceCore : sourceCores) {
            Set<BlockPos> sourceRunes = sourceRunesByCore.getOrDefault(sourceCore, Collections.emptySet());
            Map<BlockPos, List<BlockPos>> paths = nextConnectionPaths.computeIfAbsent(sourceCore, ignored -> new HashMap<BlockPos, List<BlockPos>>());
            litStorageRunesByTarget.forEach((target, targetRunes) -> {
                List<BlockPos> path = FormationRuneBlockEntity.shortestLitPath(level, shouldLight, sourceRunes, targetRunes);
                if (!path.isEmpty()) {
                    paths.put(target, path);
                    nextConnections.computeIfAbsent(sourceCore, ignored -> new HashSet<BlockPos>()).add(target);
                }
            });
        }
    }

    private static List<BlockPos> shortestPath(ServerLevel level, Set<BlockPos> allowedRunes, Set<BlockPos> sourceRunes, Set<BlockPos> targetRunes) {
        if (sourceRunes.isEmpty() || targetRunes.isEmpty()) {
            return Collections.emptyList();
        }
        ArrayDeque<BlockPos> queue = new ArrayDeque<BlockPos>();
        HashMap<BlockPos, BlockPos> parent = new HashMap<BlockPos, BlockPos>();
        HashSet<BlockPos> goals = new HashSet<BlockPos>(targetRunes);
        for (BlockPos sourceRune : sourceRunes) {
            if (!allowedRunes.contains(sourceRune)) continue;
            queue.addLast(sourceRune);
            parent.put(sourceRune, sourceRune);
            if (!goals.contains(sourceRune)) continue;
            return List.of(sourceRune);
        }
        while (!queue.isEmpty()) {
            BlockPos pos = (BlockPos)queue.removeFirst();
            BlockState state = level.getBlockState(pos);
            for (Direction dir : Direction.values()) {
                BlockState nextState;
                BlockPos next = pos.relative(dir);
                if (!allowedRunes.contains(next) || parent.containsKey(next) || !FormationRuneBlock.canRuneConnect(state, pos, nextState = level.getBlockState(next), next)) continue;
                parent.put(next, pos);
                if (goals.contains(next)) {
                    return FormationRuneBlockEntity.rebuildPath(parent, next);
                }
                queue.addLast(next);
            }
        }
        return Collections.emptyList();
    }

    private static List<BlockPos> shortestLitPath(ServerLevel level, Set<BlockPos> litRunes, Set<BlockPos> sourceRunes, Set<BlockPos> targetRunes) {
        HashSet<BlockPos> goals = new HashSet<BlockPos>();
        for (BlockPos targetRune : targetRunes) {
            if (!litRunes.contains(targetRune)) continue;
            goals.add(targetRune);
        }
        if (goals.isEmpty()) {
            return Collections.emptyList();
        }
        ArrayDeque<BlockPos> queue = new ArrayDeque<BlockPos>();
        HashMap<BlockPos, BlockPos> parent = new HashMap<BlockPos, BlockPos>();
        for (BlockPos sourceRune : sourceRunes) {
            if (!litRunes.contains(sourceRune)) continue;
            queue.addLast(sourceRune);
            parent.put(sourceRune, sourceRune);
            if (!goals.contains(sourceRune)) continue;
            return List.of(sourceRune);
        }
        while (!queue.isEmpty()) {
            BlockPos pos = (BlockPos)queue.removeFirst();
            BlockState state = level.getBlockState(pos);
            for (Direction dir : Direction.values()) {
                BlockState nextState;
                BlockPos next = pos.relative(dir);
                if (!litRunes.contains(next) || parent.containsKey(next) || !FormationRuneBlock.canRuneConnect(state, pos, nextState = level.getBlockState(next), next)) continue;
                parent.put(next, pos);
                if (goals.contains(next)) {
                    return FormationRuneBlockEntity.rebuildPath(parent, next);
                }
                queue.addLast(next);
            }
        }
        return Collections.emptyList();
    }

    private static List<BlockPos> rebuildPath(Map<BlockPos, BlockPos> parent, BlockPos end) {
        ArrayList<BlockPos> reversed = new ArrayList<BlockPos>();
        BlockPos cursor = end;
        while (true) {
            reversed.add(cursor);
            BlockPos previous = parent.get(cursor);
            if (previous == null || previous.equals((Object)cursor)) break;
            cursor = previous;
        }
        Collections.reverse(reversed);
        return reversed;
    }

    private static Set<BlockPos> collectTouchedEndpoints(ServerLevel level, BlockPos pos, BlockState state, boolean source) {
        HashSet<BlockPos> endpoints = new HashSet<BlockPos>();
        if (!(state.getBlock() instanceof FormationRuneBlock)) {
            return endpoints;
        }
        Direction facing = (Direction)state.getValue(FormationRuneBlock.FACING);
        FormationRuneBlockEntity.addEndpoint(level, endpoints, pos.relative(facing.getOpposite()), source);
        for (Direction dir : Direction.values()) {
            FormationRuneBlockEntity.addEndpoint(level, endpoints, pos.relative(dir), source);
        }
        return endpoints;
    }

    private static Set<BlockPos> collectTouchedFormationCores(ServerLevel level, BlockPos pos, BlockState state) {
        HashSet<BlockPos> endpoints = new HashSet<BlockPos>();
        if (!(state.getBlock() instanceof FormationRuneBlock)) {
            return endpoints;
        }
        Direction facing = (Direction)state.getValue(FormationRuneBlock.FACING);
        FormationRuneBlockEntity.addFormationCoreEndpoint(level, endpoints, pos.relative(facing.getOpposite()));
        for (Direction dir : Direction.values()) {
            FormationRuneBlockEntity.addFormationCoreEndpoint(level, endpoints, pos.relative(dir));
        }
        return endpoints;
    }

    private static Set<BlockPos> collectTouchedFormationFlags(ServerLevel level, BlockPos pos, BlockState state) {
        HashSet<BlockPos> endpoints = new HashSet<BlockPos>();
        if (!(state.getBlock() instanceof FormationRuneBlock)) {
            return endpoints;
        }
        Direction facing = (Direction)state.getValue(FormationRuneBlock.FACING);
        FormationRuneBlockEntity.addFormationFlagEndpoint(level, endpoints, pos.relative(facing.getOpposite()));
        for (Direction dir : Direction.values()) {
            FormationRuneBlockEntity.addFormationFlagEndpoint(level, endpoints, pos.relative(dir));
        }
        return endpoints;
    }

    private static void addEndpoint(ServerLevel level, Set<BlockPos> endpoints, BlockPos endpointPos, boolean source) {
        boolean matches;
        if (!level.isLoaded(endpointPos)) {
            return;
        }
        BlockEntity be = level.getBlockEntity(endpointPos);
        boolean bl = matches = source ? QiStorageBlocks.isUnlockedSpiritVeinCore(be) : QiStorageBlocks.isUnlockedStorageTarget(be);
        if (matches) {
            endpoints.add(endpointPos);
        }
    }

    private static void addFormationCoreEndpoint(ServerLevel level, Set<BlockPos> endpoints, BlockPos endpointPos) {
        if (!level.isLoaded(endpointPos)) {
            return;
        }
        if (level.getBlockEntity(endpointPos) instanceof FormationCorePlateBlockEntity) {
            endpoints.add(endpointPos);
        }
    }

    private static void addFormationFlagEndpoint(ServerLevel level, Set<BlockPos> endpoints, BlockPos endpointPos) {
        if (!level.isLoaded(endpointPos)) {
            return;
        }
        if (level.getBlockState(endpointPos).getBlock() instanceof FormationFlagBlock) {
            endpoints.add(endpointPos);
        }
    }

    private static boolean formationCoreHasQi(ServerLevel level, BlockPos corePos) {
        FormationCorePlateBlockEntity core;
        BlockEntity blockEntity = level.getBlockEntity(corePos);
        return blockEntity instanceof FormationCorePlateBlockEntity && (core = (FormationCorePlateBlockEntity)blockEntity).getCurrentQi() > 0L;
    }

    private static boolean touchesPoweredFormationCore(ServerLevel level, BlockPos pos, BlockState state) {
        for (BlockPos core : FormationRuneBlockEntity.collectTouchedFormationCores(level, pos, state)) {
            if (!FormationRuneBlockEntity.formationCoreHasQi(level, core)) continue;
            return true;
        }
        return false;
    }

    private static boolean hasLitNeighbor(ServerLevel level, BlockPos pos, BlockState state, Set<BlockPos> litBefore) {
        for (Direction dir : Direction.values()) {
            BlockState nextState;
            BlockPos next = pos.relative(dir);
            if (!litBefore.contains(next) || !FormationRuneBlock.canRuneConnect(state, pos, nextState = level.getBlockState(next), next)) continue;
            return true;
        }
        return false;
    }

    private static void setLit(ServerLevel level, BlockPos pos, boolean lit) {
        BlockState state = level.getBlockState(pos);
        if (!(state.getBlock() instanceof FormationRuneBlock)) {
            return;
        }
        if ((Boolean)state.getValue(FormationRuneBlock.LIT) == lit) {
            return;
        }
        BlockState updated = (BlockState)state.setValue((Property)FormationRuneBlock.LIT, (Comparable)Boolean.valueOf(lit));
        level.setBlock(pos, updated, 3);
    }

    private boolean isValidFor(ServerLevel server) {
        return !this.isRemoved() && this.level == server && this.level.getBlockEntity(this.getBlockPos()) == this && this.getBlockState().getBlock() instanceof FormationRuneBlock;
    }

    private void ensureRegistered() {
        ServerLevel sl;
        block3: {
            block2: {
                Level level = this.level;
                if (!(level instanceof ServerLevel)) break block2;
                sl = (ServerLevel)level;
                if (!this.registered) break block3;
            }
            return;
        }
        ACTIVE_RUNES.computeIfAbsent((ResourceKey<Level>)sl.dimension(), k -> ConcurrentHashMap.newKeySet()).add(this);
        this.registered = true;
    }

    private void unregister() {
        ServerLevel sl;
        Set<FormationRuneBlockEntity> runes;
        Level level = this.level;
        if (level instanceof ServerLevel && (runes = ACTIVE_RUNES.get((sl = (ServerLevel)level).dimension())) != null) {
            runes.remove((Object)this);
            if (runes.isEmpty()) {
                ACTIVE_RUNES.remove(sl.dimension());
            }
        }
        this.registered = false;
    }

    public void onLoad() {
        super.onLoad();
        this.ensureRegistered();
    }

}

