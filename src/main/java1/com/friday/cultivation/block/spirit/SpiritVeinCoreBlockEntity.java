package com.friday.cultivation.block.spirit;

import com.friday.cultivation.QiElement;
import com.friday.cultivation.block.alchemy.AlchemyCoreBlockEntity;
import com.friday.cultivation.block.formation.FormationCorePlateBlockEntity;
import com.friday.cultivation.block.formation.FormationRuneBlock;
import com.friday.cultivation.block.formation.FormationRuneBlockEntity;
import com.friday.cultivation.block.refining.RefiningCoreBlockEntity;
import com.friday.cultivation.event.SpiritLockHandler;
import com.friday.cultivation.qi.IQiConsumer;
import com.friday.cultivation.qi.SpiritVeinCoreTier;
import com.friday.cultivation.registry.ModBlockEntities;
import com.friday.cultivation.registry.ModParticles;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * 灵脉核心 BlockEntity — 完全照搬原模组 com.xiaoxiang.cultivation.block.spirit.SpiritVeinCoreBlockEntity
 */
public class SpiritVeinCoreBlockEntity extends BlockEntity implements IQiConsumer {
    public static final double ATTRACT_RADIUS = 14.0;
    public static final int SUPPLY_RADIUS = 16;
    private static final int MIN_RUNE_TRANSFER_RANDOM_PARTICLES = 4;
    private static final int MAX_RUNE_TRANSFER_RANDOM_PARTICLES = 18;
    private static final double RUNE_TRANSFER_PARTICLES_PER_SQRT_BLOCK = 2.0;
    private static final double RUNE_TRANSFER_SURFACE_JITTER = 0.28;
    private static final double RUNE_TRANSFER_PARTICLE_OFFSET = 0.035;
    private static final double RUNE_TRANSFER_PARTICLE_SPEED = 0.012;
    private static final Map<ResourceKey<Level>, Set<SpiritVeinCoreBlockEntity>> ACTIVE_CORES = new ConcurrentHashMap<ResourceKey<Level>, Set<SpiritVeinCoreBlockEntity>>();
    private long currentQi = 0L;
    private boolean registered = false;
    /** 项目兼容：原模组无解锁概念，核心恒为解锁（QiStorageBlocks.isUnlockedSpiritVeinCore 使用）。 */
    private boolean unlocked = true;

    public SpiritVeinCoreBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.SPIRIT_VEIN_CORE.get(), pos, state);
    }

    /** 项目兼容：是否已解锁（原模组无此概念）。 */
    public boolean isUnlocked() {
        return this.unlocked;
    }

    public void setUnlocked(boolean unlocked) {
        this.unlocked = unlocked;
        this.setChanged();
    }

    public SpiritVeinCoreTier tier() {
        if (this.level != null) {
            BlockState liveState = this.level.getBlockState(this.getBlockPos());
            Block block = liveState.getBlock();
            if (block instanceof SpiritVeinCoreBlock) {
                SpiritVeinCoreBlock coreBlock = (SpiritVeinCoreBlock) block;
                return coreBlock.tier();
            }
            return SpiritVeinCoreTier.LOW;
        }
        BlockState state = this.getBlockState();
        Block block = state.getBlock();
        if (block instanceof SpiritVeinCoreBlock) {
            SpiritVeinCoreBlock coreBlock = (SpiritVeinCoreBlock) block;
            return coreBlock.tier();
        }
        return SpiritVeinCoreTier.LOW;
    }

    public long getCurrentQi() {
        return this.currentQi;
    }

    public long getMaxQi() {
        return this.tier().maxQi();
    }

    public Component maxQiText() {
        return Component.literal(Long.toString(this.getMaxQi()));
    }

    public long addQi(long amount) {
        if (amount <= 0L) {
            return 0L;
        }
        boolean clamped = this.clampCurrentQiToMax();
        if (SpiritLockHandler.isBlockLocked(this.level, this.getBlockPos())) {
            if (clamped) {
                this.markDirtyAndSync();
            }
            return 0L;
        }
        long max = this.getMaxQi();
        long room = Math.max(0L, max - this.currentQi);
        long actual = Math.min(amount, room);
        if (actual <= 0L) {
            if (clamped) {
                this.markDirtyAndSync();
            }
            return 0L;
        }
        this.currentQi += actual;
        this.markDirtyAndSync();
        return actual;
    }

    public long consumeQi(long amount) {
        if (amount <= 0L) {
            return 0L;
        }
        boolean clamped = this.clampCurrentQiToMax();
        if (SpiritLockHandler.isBlockLocked(this.level, this.getBlockPos())) {
            if (clamped) {
                this.markDirtyAndSync();
            }
            return 0L;
        }
        long actual = Math.min(amount, this.currentQi);
        if (actual <= 0L) {
            if (clamped) {
                this.markDirtyAndSync();
            }
            return 0L;
        }
        this.currentQi -= actual;
        this.markDirtyAndSync();
        return actual;
    }

    public void serverTick() {
        if (this.level == null || this.level.isClientSide) {
            return;
        }
        if (this.clampCurrentQiToMax()) {
            this.markDirtyAndSync();
        }
        this.ensureRegistered();
        if (SpiritLockHandler.isBlockLocked(this.level, this.getBlockPos())) {
            return;
        }
        long offset = Math.floorMod(this.getBlockPos().asLong(), 20);
        if ((this.level.getGameTime() + offset) % 20L != 0L) {
            return;
        }
        this.addQi(this.tier().supplyPerSecond());
        this.supplyLowestQiBlock();
    }

    private void supplyLowestQiBlock() {
        Level level = this.level;
        if (!(level instanceof ServerLevel)) {
            return;
        }
        ServerLevel sl = (ServerLevel) level;
        if (this.currentQi <= 0L) {
            return;
        }
        QiStorageTarget target = this.findLowestQiTarget();
        if (target == null) {
            return;
        }
        long amount = Math.min(this.tier().supplyPerSecond(), this.currentQi);
        if ((amount = Math.min(amount, target.missingQi())) <= 0L) {
            return;
        }
        long accepted = target.addQi(amount);
        if (accepted <= 0L) {
            return;
        }
        this.consumeQi(accepted);
        this.spawnTransferParticles(sl, target);
    }

    @Nullable
    private QiStorageTarget findLowestQiTarget() {
        if (this.level == null) {
            return null;
        }
        BlockPos center = this.getBlockPos();
        int radius = 16;
        long radiusSq = (long) radius * (long) radius;
        QiStorageTarget best = null;
        for (BlockPos pos : BlockPos.betweenClosed(center.offset(-radius, -radius, -radius), center.offset(radius, radius, radius))) {
            if (pos.equals(center) || center.distSqr(pos) > (double) radiusSq || !this.level.isLoaded(pos)) continue;
            QiStorageTarget qiStorageTarget = QiStorageTarget.wrap(this.level.getBlockEntity(pos));
            best = SpiritVeinCoreBlockEntity.betterTarget(best, qiStorageTarget);
        }
        Level level = this.level;
        if (level instanceof ServerLevel) {
            ServerLevel sl = (ServerLevel) level;
            for (Map.Entry<BlockPos, List<BlockPos>> entry : FormationRuneBlockEntity.connectedStorageTargetPaths(sl, center).entrySet()) {
                BlockPos pos = entry.getKey();
                if (pos.equals(center) || !this.level.isLoaded(pos)) continue;
                QiStorageTarget target = QiStorageTarget.wrap(this.level.getBlockEntity(pos));
                if (target != null) {
                    target = target.withRunePath(entry.getValue());
                }
                best = SpiritVeinCoreBlockEntity.betterTarget(best, target);
            }
        }
        return best;
    }

    @Nullable
    private static QiStorageTarget betterTarget(@Nullable QiStorageTarget best, @Nullable QiStorageTarget candidate) {
        if (candidate == null || candidate.missingQi() <= 0L) {
            return best;
        }
        if (best != null && candidate.pos().equals(best.pos()) && candidate.hasRunePath() && !best.hasRunePath()) {
            return candidate;
        }
        if (best == null || candidate.currentQi() < best.currentQi() || candidate.currentQi() == best.currentQi() && candidate.missingQi() > best.missingQi()) {
            return candidate;
        }
        return best;
    }

    private void spawnTransferParticles(ServerLevel sl, QiStorageTarget target) {
        if (target.hasRunePath()) {
            this.spawnRuneTransferParticles(sl, target);
        } else {
            this.spawnDirectTransferParticles(sl, target.pos());
        }
    }

    private void spawnDirectTransferParticles(ServerLevel sl, BlockPos targetPos) {
        Vec3 from = Vec3.atCenterOf(this.getBlockPos()).add(0.0, 0.35, 0.0);
        Vec3 to = Vec3.atCenterOf(targetPos).add(0.0, 0.35, 0.0);
        Vec3 delta = to.subtract(from);
        int steps = Math.max(8, Math.min(28, (int) Math.ceil(delta.length() * 2.0)));
        for (int i = 0; i <= steps; ++i) {
            double t = (double) i / (double) steps;
            Vec3 p = from.add(delta.scale(t));
            sl.sendParticles((SimpleParticleType) ModParticles.AMBIENT_QI.get(), p.x, p.y, p.z, 1, 0.025, 0.025, 0.025, 0.005);
        }
        sl.sendParticles((SimpleParticleType) ModParticles.QI_ABSORB.get(), to.x, to.y, to.z, 8, 0.15, 0.12, 0.15, 0.04);
        sl.playSound(null, this.getBlockPos(), SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.BLOCKS, 0.35f, 1.65f);
    }

    private void spawnRuneTransferParticles(ServerLevel sl, QiStorageTarget target) {
        ArrayList<RuneParticlePoint> points = new ArrayList<RuneParticlePoint>();
        for (BlockPos runePos : target.runePath()) {
            BlockState state;
            if (!sl.isLoaded(runePos) || !((state = sl.getBlockState(runePos)).getBlock() instanceof FormationRuneBlock)) continue;
            points.add(new RuneParticlePoint(SpiritVeinCoreBlockEntity.runeParticlePosition(runePos, state), state.getValue(FormationRuneBlock.FACING)));
        }
        if (points.isEmpty()) {
            this.spawnTargetAbsorbParticles(sl, target.pos());
            return;
        }
        double totalLength = SpiritVeinCoreBlockEntity.runePathLength(points);
        int count = SpiritVeinCoreBlockEntity.randomRuneParticleCount(sl.random, totalLength);
        for (int i = 0; i < count; ++i) {
            RuneParticlePoint point = SpiritVeinCoreBlockEntity.randomRunePathPoint(sl.random, points, totalLength);
            Vec3 p = SpiritVeinCoreBlockEntity.jitterOnRuneSurface(point.pos(), point.facing(), sl.random);
            sl.sendParticles((SimpleParticleType) ModParticles.AMBIENT_QI.get(), p.x, p.y, p.z, 1, 0.035, 0.035, 0.035, 0.012);
        }
        this.spawnTargetAbsorbParticles(sl, target.pos());
        sl.playSound(null, this.getBlockPos(), SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.BLOCKS, 0.35f, 1.65f);
    }

    private static Vec3 runeParticlePosition(BlockPos runePos, BlockState state) {
        DirectionOffset offset = DirectionOffset.forFacing(state.getValue(FormationRuneBlock.FACING));
        return new Vec3((double) runePos.getX() + offset.x(), (double) runePos.getY() + offset.y(), (double) runePos.getZ() + offset.z());
    }

    private static int randomRuneParticleCount(RandomSource random, double totalLength) {
        int base = 4 + (int) Math.ceil(Math.sqrt(Math.max(1.0, totalLength)) * 2.0);
        int varied = base + random.nextInt(3);
        return Math.max(4, Math.min(18, varied));
    }

    private static RuneParticlePoint randomRunePathPoint(RandomSource random, List<RuneParticlePoint> points, double totalLength) {
        if (points.size() == 1 || totalLength <= 0.0) {
            return points.get(random.nextInt(points.size()));
        }
        double targetDistance = random.nextDouble() * totalLength;
        double walked = 0.0;
        for (int i = 0; i < points.size() - 1; ++i) {
            RuneParticlePoint from = points.get(i);
            RuneParticlePoint to = points.get(i + 1);
            double length = from.pos().distanceTo(to.pos());
            if (length <= 0.001) continue;
            if (walked + length >= targetDistance) {
                double t = (targetDistance - walked) / length;
                return new RuneParticlePoint(from.pos().lerp(to.pos(), t), from.facing());
            }
            walked += length;
        }
        return points.get(points.size() - 1);
    }

    private static Vec3 jitterOnRuneSurface(Vec3 base, Direction facing, RandomSource random) {
        double a = SpiritVeinCoreBlockEntity.randomRange(random, 0.28);
        double b = SpiritVeinCoreBlockEntity.randomRange(random, 0.28);
        return switch (facing.getAxis()) {
            case Y -> base.add(a, 0.0, b);
            case X -> base.add(0.0, a, b);
            case Z -> base.add(a, b, 0.0);
        };
    }

    private static double randomRange(RandomSource random, double radius) {
        return (random.nextDouble() * 2.0 - 1.0) * radius;
    }

    private static double runePathLength(List<RuneParticlePoint> points) {
        double total = 0.0;
        for (int i = 0; i < points.size() - 1; ++i) {
            total += points.get(i).pos().distanceTo(points.get(i + 1).pos());
        }
        return total;
    }

    private void spawnTargetAbsorbParticles(ServerLevel sl, BlockPos targetPos) {
        Vec3 to = Vec3.atCenterOf(targetPos).add(0.0, 0.35, 0.0);
        sl.sendParticles((SimpleParticleType) ModParticles.QI_ABSORB.get(), to.x, to.y, to.z, 8, 0.15, 0.12, 0.15, 0.04);
    }

    @Override
    public Vec3 position() {
        return Vec3.atCenterOf(this.getBlockPos()).add(0.0, 0.35, 0.0);
    }

    @Override
    public double attractRadius() {
        return 14.0;
    }

    @Override
    public boolean wantsMore() {
        return !SpiritLockHandler.isBlockLocked(this.level, this.getBlockPos()) && this.currentQi < this.getMaxQi();
    }

    @Override
    public int receiveQi(QiElement element, int baseAmount) {
        if (baseAmount <= 0) {
            return 0;
        }
        if (SpiritLockHandler.isBlockLocked(this.level, this.getBlockPos())) {
            return 0;
        }
        long requested = SpiritVeinCoreBlockEntity.safeMultiply(baseAmount, this.tier().orbGain());
        long accepted = this.addQi(requested);
        return (int) Math.min(accepted, Integer.MAX_VALUE);
    }

    private static long safeMultiply(long a, long b) {
        if (a <= 0L || b <= 0L) {
            return 0L;
        }
        if (a > Long.MAX_VALUE / b) {
            return Long.MAX_VALUE;
        }
        return a * b;
    }

    @Nullable
    public static IQiConsumer findNearestConsumer(ServerLevel server, Vec3 orbPos, double maxDistSq) {
        Set<SpiritVeinCoreBlockEntity> cores = ACTIVE_CORES.get(server.dimension());
        if (cores == null || cores.isEmpty()) {
            return null;
        }
        SpiritVeinCoreBlockEntity best = null;
        double bestDistSq = maxDistSq;
        Iterator<SpiritVeinCoreBlockEntity> it = cores.iterator();
        while (it.hasNext()) {
            SpiritVeinCoreBlockEntity core = it.next();
            if (!core.isValidFor(server)) {
                it.remove();
                continue;
            }
            if (SpiritLockHandler.isBlockLocked(server, core.getBlockPos()) || !core.wantsMore()) continue;
            double radius = core.attractRadius();
            double distSq = core.position().distanceToSqr(orbPos);
            if (distSq > radius * radius || distSq >= bestDistSq) continue;
            bestDistSq = distSq;
            best = core;
        }
        return best;
    }

    private boolean isValidFor(ServerLevel server) {
        return !this.isRemoved() && this.level == server && this.level.getBlockEntity(this.getBlockPos()) == this && this.level.getBlockState(this.getBlockPos()).getBlock() instanceof SpiritVeinCoreBlock;
    }

    private void ensureRegistered() {
        Level level = this.level;
        if (!(level instanceof ServerLevel)) {
            return;
        }
        ServerLevel sl = (ServerLevel) level;
        if (this.registered) {
            return;
        }
        ACTIVE_CORES.computeIfAbsent(sl.dimension(), k -> ConcurrentHashMap.newKeySet()).add(this);
        this.registered = true;
    }

    private void unregister() {
        Level level = this.level;
        if (level instanceof ServerLevel) {
            ServerLevel sl = (ServerLevel) level;
            Set<SpiritVeinCoreBlockEntity> cores = ACTIVE_CORES.get(sl.dimension());
            if (cores != null) {
                cores.remove(this);
                if (cores.isEmpty()) {
                    ACTIVE_CORES.remove(sl.dimension());
                }
            }
        }
        this.registered = false;
    }

    public void onBlockRemoved() {
        this.unregister();
    }

    @Override
    public void setRemoved() {
        this.unregister();
        super.setRemoved();
    }

    private void markDirtyAndSync() {
        this.setChanged();
        if (this.level != null && !this.level.isClientSide) {
            BlockState liveState = this.level.getBlockState(this.getBlockPos());
            this.level.sendBlockUpdated(this.getBlockPos(), liveState, liveState, 3);
        }
    }

    private boolean clampCurrentQiToMax() {
        long clamped = Math.max(0L, Math.min(this.currentQi, this.getMaxQi()));
        if (clamped == this.currentQi) {
            return false;
        }
        this.currentQi = clamped;
        return true;
    }

    @Override
    protected void saveAdditional(@NotNull CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putLong("currentQi", this.currentQi);
    }

    @Override
    public void load(@NotNull CompoundTag tag) {
        super.load(tag);
        this.currentQi = Math.max(0L, tag.getLong("currentQi"));
        this.clampCurrentQiToMax();
    }

    private static final class QiStorageTarget {
        private final BlockEntity blockEntity;
        private final BlockPos pos;
        private final long currentQi;
        private final long maxQi;
        @Nullable
        private final List<BlockPos> runePath;

        private QiStorageTarget(BlockEntity blockEntity, BlockPos pos, long currentQi, long maxQi, @Nullable List<BlockPos> runePath) {
            this.blockEntity = blockEntity;
            this.pos = pos;
            this.currentQi = currentQi;
            this.maxQi = maxQi;
            this.runePath = runePath == null || runePath.isEmpty() ? null : List.copyOf(runePath);
        }

        @Nullable
        static QiStorageTarget wrap(@Nullable BlockEntity be) {
            if (be instanceof AlchemyCoreBlockEntity) {
                AlchemyCoreBlockEntity core = (AlchemyCoreBlockEntity) be;
                if (SpiritLockHandler.isBlockLocked(core.getLevel(), core.getBlockPos())) {
                    return null;
                }
                return new QiStorageTarget(core, core.getBlockPos(), core.getCurrentQi(), core.getMaxQi(), null);
            }
            if (be instanceof RefiningCoreBlockEntity) {
                RefiningCoreBlockEntity core = (RefiningCoreBlockEntity) be;
                if (SpiritLockHandler.isBlockLocked(core.getLevel(), core.getBlockPos())) {
                    return null;
                }
                return new QiStorageTarget(core, core.getBlockPos(), core.getCurrentQi(), core.getMaxQi(), null);
            }
            if (be instanceof FormationCorePlateBlockEntity) {
                FormationCorePlateBlockEntity core = (FormationCorePlateBlockEntity) be;
                if (SpiritLockHandler.isBlockLocked(core.getLevel(), core.getBlockPos())) {
                    return null;
                }
                return new QiStorageTarget(core, core.getBlockPos(), core.getCurrentQi(), core.getMaxQi(), null);
            }
            return null;
        }

        QiStorageTarget withRunePath(List<BlockPos> path) {
            return new QiStorageTarget(this.blockEntity, this.pos, this.currentQi, this.maxQi, path);
        }

        BlockPos pos() {
            return this.pos;
        }

        boolean hasRunePath() {
            return this.runePath != null && !this.runePath.isEmpty();
        }

        List<BlockPos> runePath() {
            return this.runePath == null ? List.of() : this.runePath;
        }

        long currentQi() {
            return this.currentQi;
        }

        long missingQi() {
            return Math.max(0L, this.maxQi - this.currentQi);
        }

        long addQi(long amount) {
            BlockEntity blockEntity = this.blockEntity;
            if (blockEntity instanceof AlchemyCoreBlockEntity) {
                AlchemyCoreBlockEntity core = (AlchemyCoreBlockEntity) blockEntity;
                return core.addQi(amount);
            }
            blockEntity = this.blockEntity;
            if (blockEntity instanceof RefiningCoreBlockEntity) {
                RefiningCoreBlockEntity core = (RefiningCoreBlockEntity) blockEntity;
                return core.addQi(amount);
            }
            blockEntity = this.blockEntity;
            if (blockEntity instanceof FormationCorePlateBlockEntity) {
                FormationCorePlateBlockEntity core = (FormationCorePlateBlockEntity) blockEntity;
                return core.addQi(amount);
            }
            return 0L;
        }
    }

    private record RuneParticlePoint(Vec3 pos, Direction facing) {
    }

    private record DirectionOffset(double x, double y, double z) {
        static DirectionOffset forFacing(Direction facing) {
            return switch (facing) {
                case UP -> new DirectionOffset(0.5, 0.08, 0.5);
                case DOWN -> new DirectionOffset(0.5, 0.92, 0.5);
                case NORTH -> new DirectionOffset(0.5, 0.5, 0.92);
                case SOUTH -> new DirectionOffset(0.5, 0.5, 0.08);
                case EAST -> new DirectionOffset(0.08, 0.5, 0.5);
                case WEST -> new DirectionOffset(0.92, 0.5, 0.5);
            };
        }
    }
}
