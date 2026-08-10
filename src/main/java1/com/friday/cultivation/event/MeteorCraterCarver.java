package com.friday.cultivation.event;

import com.friday.cultivation.util.SpellTerrainDestructionHelper;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.server.ServerStoppingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.jetbrains.annotations.Nullable;

@Mod.EventBusSubscriber(modid = "friday_cultivation")
public final class MeteorCraterCarver {
    private static final int MAX_VISITED_PER_TICK = 18000;
    private static final int MAX_BLOCK_CHANGES_PER_TICK = 1200;
    private static final Deque<Task> TASKS = new ArrayDeque<Task>();

    private MeteorCraterCarver() {
    }

    public static void schedule(ServerLevel level, Vec3 center, int innerR, int outerR) {
        MeteorCraterCarver.schedule(level, center, innerR, outerR, null);
    }

    public static void schedule(ServerLevel level, Vec3 center, int innerR, int outerR, @Nullable Entity caster) {
        if (!SpellTerrainDestructionHelper.canModifyBlocks(level, caster)) {
            return;
        }
        TASKS.addLast(new Task(level, center, innerR, outerR, caster == null ? null : caster.getUUID()));
    }

    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        Task task;
        if (event.phase != TickEvent.Phase.END || TASKS.isEmpty()) {
            return;
        }
        int visited = 0;
        int changed = 0;
        int taskCount = TASKS.size();
        for (int i = 0; i < taskCount && visited < 18000 && changed < 1200 && (task = TASKS.pollFirst()) != null; ++i) {
            if (!SpellTerrainDestructionHelper.canModifyBlocks(task.level, task.caster())) continue;
            StepResult result = task.process(18000 - visited, 1200 - changed);
            visited += result.visited();
            changed += result.changed();
            if (task.done()) continue;
            TASKS.addLast(task);
        }
    }

    @SubscribeEvent
    public static void onServerStopping(ServerStoppingEvent event) {
        TASKS.clear();
    }

    private static final class Task {
        private final ServerLevel level;
        private final int cx;
        private final int cy;
        private final int cz;
        private final int minX;
        private final int minY;
        private final int minZ;
        private final int maxX;
        private final int maxY;
        private final int maxZ;
        private final int innerR;
        private final int innerR2;
        private final int outerR;
        private final int outerR2;
        private int x;
        private int y;
        private int z;
        private boolean done;
        private final UUID casterUuid;

        private Task(ServerLevel level, Vec3 center, int innerR, int outerR, @Nullable UUID casterUuid) {
            this.level = level;
            this.cx = (int) center.x;
            this.cy = (int) center.y;
            this.cz = (int) center.z;
            this.casterUuid = casterUuid;
            this.innerR = innerR;
            this.innerR2 = innerR * innerR;
            this.outerR = outerR;
            this.outerR2 = outerR * outerR;
            this.minX = this.cx - outerR;
            this.minY = Math.max(level.getMinBuildHeight(), this.cy - outerR);
            this.minZ = this.cz - outerR;
            this.maxX = this.cx + outerR;
            this.maxY = Math.min(level.getMaxBuildHeight() - 1, this.cy + outerR);
            this.maxZ = this.cz + outerR;
            this.x = this.minX;
            this.y = this.minY;
            this.z = this.minZ;
        }

        private StepResult process(int visitBudget, int changeBudget) {
            int visited = 0;
            int changed = 0;
            BlockState air = Blocks.AIR.defaultBlockState();
            BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
            while (!this.done && visited < visitBudget && changed < changeBudget) {
                BlockState state;
                int dy;
                int d2;
                int dz;
                int dxz2;
                if (this.x > this.maxX) {
                    this.done = true;
                    break;
                }
                if (this.z > this.maxZ) {
                    ++this.x;
                    this.z = this.minZ;
                    this.y = this.minY;
                    continue;
                }
                if (this.y > this.maxY) {
                    ++this.z;
                    this.y = this.minY;
                    continue;
                }
                int px = this.x;
                int py = this.y++;
                int pz = this.z;
                int dx = px - this.cx;
                int dx2 = dx * dx;
                if (dx2 > this.outerR2 || (dxz2 = dx2 + (dz = pz - this.cz) * dz) > this.outerR2 || (d2 = dxz2 + (dy = py - this.cy) * dy) > this.outerR2) continue;
                ++visited;
                if (!this.shouldClear(d2)) continue;
                pos.set(px, py, pz);
                if (!this.level.isLoaded(pos) || (state = this.level.getBlockState(pos)).isAir() || state.is(Blocks.BEDROCK) || state.is(Blocks.END_PORTAL_FRAME) || SectProtectionDomeHandler.isProtectedByAnySectProtectionDome(this.level, pos) || !SpellTerrainDestructionHelper.setBlock(this.level, pos, air, 2, this.caster())) continue;
                ++changed;
            }
            return new StepResult(visited, changed);
        }

        @Nullable
        private Entity caster() {
            return this.casterUuid == null ? null : this.level.getEntity(this.casterUuid);
        }

        private boolean shouldClear(int distanceSqr) {
            if (distanceSqr <= this.innerR2) {
                return true;
            }
            double t = (Math.sqrt(distanceSqr) - (double) this.innerR) / (double) (this.outerR - this.innerR);
            double probability = (1.0 - t) * (1.0 - t);
            return this.level.random.nextDouble() < probability;
        }

        private boolean done() {
            return this.done;
        }
    }

    private record StepResult(int visited, int changed) {
    }
}
