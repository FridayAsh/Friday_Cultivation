package com.friday.cultivation.block.formation;

import com.friday.cultivation.ItemTier;
import com.friday.cultivation.entity.npc.WanderingCultivatorEntity;
import com.friday.cultivation.event.SectProtectionDomeHandler;
import com.friday.cultivation.event.SpiritLockHandler;
import com.friday.cultivation.item.SectTokenItem;
import com.friday.cultivation.qi.BlockQiSpec;
import com.friday.cultivation.qi.BlockQiSpecs;
import com.friday.cultivation.qi.field.IQiFieldEffect;
import com.friday.cultivation.qi.field.QiFieldRegistry;
import com.friday.cultivation.qi.field.QiModifier;
import com.friday.cultivation.qi.formation.CoreTier;
import com.friday.cultivation.qi.formation.FormationType;
import com.friday.cultivation.registry.ModBlockEntities;
import com.friday.cultivation.registry.ModBlocks;
import com.friday.cultivation.registry.ModParticles;
import com.friday.cultivation.sect.SectSavedData;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.Vec3i;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.network.Connection;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.NeutralMob;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.AttachedStemBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.DoublePlantBlock;
import net.minecraft.world.level.block.NetherWartBlock;
import net.minecraft.world.level.block.StemBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.IPlantable;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.items.IItemHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 阵法核心盘 BlockEntity - 完整 1:1 复刻原模组 com.xiaoxiang.cultivation.block.formation.FormationCorePlateBlockEntity。
 * 实现 IQiFieldEffect：灵气场效果（origin/radius/isActive/modifyAt）。
 */
public class FormationCorePlateBlockEntity
extends BlockEntity
implements IQiFieldEffect {
    private static final int FORMATION_EFFECT_INTERVAL_TICKS = 10;
    private static final int GENERATED_ARRAY_SYNC_INTERVAL_TICKS = 200;
    private static final int FORMATION_RELOAD_FLAG_VALIDATION_GRACE_TICKS = 100;
    private static final int FORMATION_EFFECT_DURATION_TICKS = 16;
    private static final int GROWTH_TICK_INTERVAL_TICKS = 20;
    private static final int FARM_HARVEST_MIN_CHECKS = 4096;
    private static final int FARM_HARVEST_CHECKS_PER_TARGET = 1024;
    private static final int FARM_HARVEST_MAX_CHECKS = 16384;
    private static final String TAG_ACTIVE_FORMATIONS = "activeFormations";
    public static final int MIN_FLAG_EFFECT_RADIUS = 1;
    public static final int DEFAULT_FLAG_EFFECT_RADIUS = 8;
    public static final int MAX_FLAG_EFFECT_RADIUS = 255;
    private long currentQi = 0L;
    private boolean activated = false;
    @Nullable
    private FormationType activeFormation = null;
    @Nullable
    private ItemTier activeFlagTier = null;
    private int activeRadius = 0;
    private double drainAccumulator = 0.0;
    private int cachedSourcesInRange = -1;
    private long cachedCoveredBlocksInRange = -1L;
    private final Map<Integer, Integer> mazeIntruderTicks = new HashMap<Integer, Integer>();
    private final Set<Long> linkedFlags = new LinkedHashSet<Long>();
    private final Map<Long, Integer> flagEffectRadii = new HashMap<Long, Integer>();
    private boolean generatedArrayPlayerEdited = false;
    private final Map<FormationType, ActiveFormationState> activeFormations = new EnumMap<FormationType, ActiveFormationState>(FormationType.class);
    private long compassLockGlowUntil = -1L;
    private int farmHarvestCursor = 0;
    private boolean spiritLockSuppressed = false;
    private final Set<Long> placedBarriers = new LinkedHashSet<Long>();
    private final Map<Long, BlockState> savedShellStates = new HashMap<Long, BlockState>();
    private int passThroughShellBlockCount = 0;
    private boolean legacySolidShellRepairChecked = false;
    private String customName = "";
    public static final int CUSTOM_NAME_MAX_LENGTH = 32;
    private int cachedFlagCount = -1;
    private long cachedFlagCountAt = -1L;
    private long lastSectProtectionRadiusCheckAt = -1L;
    private long skipFlagValidationUntil = -1L;
    private boolean lazyRegistered = false;

    public FormationCorePlateBlockEntity(BlockPos pos, BlockState state) {
        super((BlockEntityType)ModBlockEntities.FORMATION_CORE_PLATE.get(), pos, state);
    }

    public long getCurrentQi() {
        return this.currentQi;
    }

    public long getMaxQi() {
        return this.coreTier().maxQi();
    }

    public CoreTier coreTier() {
        BlockState ownState = this.getBlockState();
        Block block = ownState.getBlock();
        if (block instanceof FormationCorePlateBlock) {
            FormationCorePlateBlock cp = (FormationCorePlateBlock)block;
            return cp.coreTier();
        }
        if (this.level == null) {
            return CoreTier.LOW;
        }
        BlockState state = this.level.getBlockState(this.getBlockPos());
        Block block2 = state.getBlock();
        if (block2 instanceof FormationCorePlateBlock) {
            FormationCorePlateBlock cp = (FormationCorePlateBlock)block2;
            return cp.coreTier();
        }
        return CoreTier.LOW;
    }

    public boolean isActivated() {
        return this.activated && !this.spiritLockSuppressed;
    }

    @Nullable
    public FormationType getActiveFormation() {
        return this.activeFormation;
    }

    @Nullable
    public ItemTier getActiveFlagTier() {
        return this.activeFlagTier;
    }

    public int getActiveRadius() {
        return this.activeRadius;
    }

    public int getActiveFormationCount() {
        return this.activeFormations.size();
    }

    public boolean hasActiveFormation(FormationType type) {
        return this.activated && !this.spiritLockSuppressed && this.activeFormations.containsKey(type);
    }

    @Nullable
    public ItemTier getActiveFlagTier(FormationType type) {
        ActiveFormationState state = this.activeFormations.get(type);
        return state == null ? null : state.flagTier;
    }

    public int getActiveRadius(FormationType type) {
        ActiveFormationState state = this.activeFormations.get(type);
        return state == null ? 0 : state.radius;
    }

    public List<FormationSphere> getActiveSpheres(FormationType type) {
        ActiveFormationState state = this.activeFormations.get(type);
        return state == null ? Collections.emptyList() : state.spheres;
    }

    public List<FormationSphere> getSectProtectionSpheres() {
        return this.getActiveSpheres(FormationType.SECT_PROTECTION);
    }

    public boolean containsActiveFormation(FormationType type, double x, double y, double z) {
        ActiveFormationState state = this.activeFormations.get(type);
        return state != null && FormationCorePlateBlockEntity.containsAnySphere(state.spheres, x, y, z);
    }

    public boolean containsActiveFormation(FormationType type, BlockPos pos) {
        return this.containsActiveFormation(type, (double)pos.getX() + 0.5, (double)pos.getY() + 0.5, (double)pos.getZ() + 0.5);
    }

    public int maxLinkedFlags() {
        return switch (this.coreTier()) {
            case LOW -> 1;
            case MID -> 5;
            case HIGH -> 10;
            case SUPREME -> 30;
            case IMMORTAL -> 50;
        };
    }

    public boolean isCompassLockGlowing(long gameTime) {
        return this.compassLockGlowUntil >= gameTime;
    }

    public int getCachedSourcesInRange() {
        return this.cachedSourcesInRange;
    }

    public boolean isOwnedBarrier(long packed) {
        return this.placedBarriers.contains(packed);
    }

    public boolean isProtectedShellBlock(long packed) {
        return this.placedBarriers.contains(packed) || this.savedShellStates.containsKey(packed);
    }

    public boolean isPassThroughProtectedShellBlock(long packed) {
        if (!this.savedShellStates.containsKey(packed) || this.placedBarriers.contains(packed)) {
            return false;
        }
        if (this.level == null) {
            return false;
        }
        BlockPos pos = BlockPos.of(packed);
        return !this.hasPhysicalCollision(this.level.getBlockState(pos), pos);
    }

    public boolean hasPassThroughProtectedShellBlocks() {
        if (this.passThroughShellBlockCount < 0) {
            this.passThroughShellBlockCount = this.countPassThroughProtectedShellBlocks();
        }
        return this.passThroughShellBlockCount > 0;
    }

    public boolean touchesPassThroughProtectedShellBlock(LivingEntity entity) {
        AABB box = entity.getBoundingBox().inflate(0.05);
        int minX = Mth.floor(box.minX);
        int maxX = Mth.floor(box.maxX);
        int minY = Mth.floor(box.minY);
        int maxY = Mth.floor(box.maxY);
        int minZ = Mth.floor(box.minZ);
        int maxZ = Mth.floor(box.maxZ);
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        for (int x = minX; x <= maxX; ++x) {
            for (int y = minY; y <= maxY; ++y) {
                for (int z = minZ; z <= maxZ; ++z) {
                    if (!this.isPassThroughProtectedShellBlock(cursor.set(x, y, z).asLong())) continue;
                    return true;
                }
            }
        }
        return false;
    }

    private int countPassThroughProtectedShellBlocks() {
        if (this.level == null || this.savedShellStates.isEmpty()) {
            return 0;
        }
        int count = 0;
        for (Long packed : this.savedShellStates.keySet()) {
            if (!this.isPassThroughProtectedShellBlock(packed)) continue;
            ++count;
        }
        return count;
    }

    public String getCustomName() {
        return this.customName == null ? "" : this.customName;
    }

    public void setCustomName(String name) {
        if (name == null) {
            name = "";
        }
        if ((name = name.trim()).length() > 32) {
            name = name.substring(0, 32);
        }
        if (!name.equals(this.customName)) {
            this.customName = name;
            this.setChanged();
        }
    }

    public double getDrainPerSec() {
        if (!this.activated || this.spiritLockSuppressed || this.activeFormations.isEmpty()) {
            return 0.0;
        }
        double total = 0.0;
        for (Map.Entry<FormationType, ActiveFormationState> entry : this.activeFormations.entrySet()) {
            FormationType type = entry.getKey();
            ActiveFormationState state = entry.getValue();
            total += (double)this.drainUnitCount(type, state) * type.drainPerBlockPerHour(state.flagTier) / 3600.0;
        }
        return total;
    }

    private long drainUnitCount(FormationType type, ActiveFormationState state) {
        if (type.drainsByCoveredBlocks()) {
            if (state.coveredBlocksInRange < 0L) {
                state.coveredBlocksInRange = this.estimateCoveredBlocksInRange(state.spheres);
                this.syncLegacyActiveView();
                this.setChanged();
            }
            return state.coveredBlocksInRange;
        }
        return Math.max(0, state.sourcesInRange);
    }

    private long estimateCoveredBlocksInRange(int radius) {
        double r = Math.max(1, radius);
        return Math.max(1L, Math.round(Math.PI * 4 * r * r * r / 3.0));
    }

    private long estimateCoveredBlocksInRange(Collection<FormationSphere> spheres) {
        if (spheres == null || spheres.isEmpty()) {
            return 0L;
        }
        long total = 0L;
        for (FormationSphere sphere : spheres) {
            total += this.estimateCoveredBlocksInRange(sphere.radius());
        }
        return Math.max(1L, total);
    }

    public long addQi(long amount) {
        long actual;
        if (amount <= 0L) {
            return 0L;
        }
        boolean clamped = this.clampCurrentQiToMax();
        if (SpiritLockHandler.isBlockLocked(this.level, this.getBlockPos())) {
            if (clamped) {
                this.setChanged();
            }
            return 0L;
        }
        long max = this.getMaxQi();
        long room = max - this.currentQi;
        if (room < 0L) {
            room = 0L;
        }
        if ((actual = Math.min(amount, room)) <= 0L) {
            if (clamped) {
                this.setChanged();
            }
            return 0L;
        }
        this.currentQi += actual;
        this.setChanged();
        return actual;
    }

    public long consumeQi(long amount) {
        if (amount <= 0L) {
            return 0L;
        }
        boolean clamped = this.clampCurrentQiToMax();
        if (SpiritLockHandler.isBlockLocked(this.level, this.getBlockPos())) {
            if (clamped) {
                this.setChanged();
            }
            return 0L;
        }
        long actual = Math.min(amount, this.currentQi);
        if (actual <= 0L) {
            if (clamped) {
                this.setChanged();
            }
            return 0L;
        }
        this.currentQi -= actual;
        this.setChanged();
        return actual;
    }

    public List<BlockPos> getLinkedFlagPositions() {
        ArrayList<BlockPos> result = new ArrayList<BlockPos>();
        for (FlagLinkView view : this.getConnectedFlagViews()) {
            result.add(view.pos());
        }
        return result;
    }

    public List<FlagLinkView> getConnectedFlagViews() {
        return this.scanConnectedFlagViews();
    }

    public boolean hasStoredLinkedFlags() {
        return !this.linkedFlags.isEmpty();
    }

    public int getFlagEffectRadius(BlockPos flagPos) {
        return this.radiusForFlag(flagPos);
    }

    public boolean setFlagEffectRadius(BlockPos flagPos, int radius) {
        if (this.level == null || flagPos == null) {
            return false;
        }
        BlockPos immutable = flagPos.immutable();
        boolean connected = false;
        for (FlagLinkView view : this.scanConnectedFlagViews()) {
            if (!view.pos().equals((Object)immutable)) continue;
            connected = true;
            break;
        }
        if (!connected) {
            return false;
        }
        int clamped = FormationCorePlateBlockEntity.clampFlagEffectRadius(radius);
        long packed = immutable.asLong();
        Integer previous = this.flagEffectRadii.put(packed, clamped);
        if (previous != null && previous == clamped) {
            return true;
        }
        this.generatedArrayPlayerEdited = true;
        this.cachedFlagCount = -1;
        this.cachedFlagCountAt = -1L;
        if (this.activated && !this.level.isClientSide) {
            this.refreshActiveFormationsFromConnectedFlags(true);
        }
        this.markDirtyAndSync();
        return true;
    }

    public boolean isGeneratedArrayPlayerEdited() {
        return this.generatedArrayPlayerEdited;
    }

    public boolean configureGeneratedSectArray(String name, Collection<BlockPos> flags, int radius) {
        return this.configureGeneratedSectArray(name, FormationCorePlateBlockEntity.uniformFlagRadii(flags, radius), radius);
    }

    public boolean configureGeneratedSectArray(String name, Map<BlockPos, Integer> flagRadii, int radius) {
        if (this.level == null || this.level.isClientSide || flagRadii == null || flagRadii.isEmpty()) {
            return false;
        }
        if (!this.getCustomName().equals(name)) {
            this.setCustomName(name);
        }
        int fallbackRadius = FormationCorePlateBlockEntity.clampFlagEffectRadius(radius);
        boolean linkedAny = false;
        for (Map.Entry<BlockPos, Integer> entry : flagRadii.entrySet()) {
            BlockState state;
            BlockPos flagPos = entry.getKey();
            if (flagPos == null || !((state = this.level.getBlockState(flagPos)).getBlock() instanceof FormationFlagBlock)) continue;
            long packed = flagPos.asLong();
            this.linkedFlags.add(packed);
            int clamped = FormationCorePlateBlockEntity.clampFlagEffectRadius(entry.getValue() == null ? fallbackRadius : entry.getValue());
            this.flagEffectRadii.put(packed, clamped);
            linkedAny = true;
        }
        if (!linkedAny) {
            return false;
        }
        this.cachedFlagCount = -1;
        this.cachedFlagCountAt = -1L;
        this.addQi(this.getMaxQi());
        if (this.activated) {
            this.refreshActiveFormationsFromConnectedFlags(true);
            this.markDirtyAndSync();
            return true;
        }
        return this.tryActivate().kind() == ActivationResultKind.SUCCESS;
    }

    public boolean generatedSectArrayMatches(String name, Collection<BlockPos> flags, int radius) {
        return this.generatedSectArrayMatches(name, FormationCorePlateBlockEntity.uniformFlagRadii(flags, radius), radius);
    }

    public boolean generatedSectArrayMatches(String name, Map<BlockPos, Integer> flagRadii, int radius) {
        if (this.level == null || this.level.isClientSide || flagRadii == null || flagRadii.isEmpty()) {
            return false;
        }
        if (!this.getCustomName().equals(name)) {
            return false;
        }
        int fallbackRadius = FormationCorePlateBlockEntity.clampFlagEffectRadius(radius);
        for (Map.Entry<BlockPos, Integer> entry : flagRadii.entrySet()) {
            BlockPos flagPos = entry.getKey();
            if (flagPos == null) {
                return false;
            }
            BlockState state = this.level.getBlockState(flagPos);
            Block block = state.getBlock();
            if (!(block instanceof FormationFlagBlock)) {
                return false;
            }
            FormationFlagBlock flag = (FormationFlagBlock)block;
            long packed = flagPos.asLong();
            if (!this.linkedFlags.contains(packed)) {
                return false;
            }
            int clamped = FormationCorePlateBlockEntity.clampFlagEffectRadius(entry.getValue() == null ? fallbackRadius : entry.getValue());
            if (FormationCorePlateBlockEntity.clampFlagEffectRadius(this.flagEffectRadii.getOrDefault(packed, 0)) != clamped) {
                return false;
            }
            ActiveFormationState active = this.activeFormations.get(flag.formationType());
            if (active != null && FormationCorePlateBlockEntity.hasSphere(active.spheres, this.getBlockPos(), clamped)) continue;
            return false;
        }
        return true;
    }

    private static Map<BlockPos, Integer> uniformFlagRadii(Collection<BlockPos> flags, int radius) {
        if (flags == null || flags.isEmpty()) {
            return Collections.emptyMap();
        }
        int clamped = FormationCorePlateBlockEntity.clampFlagEffectRadius(radius);
        LinkedHashMap<BlockPos, Integer> radii = new LinkedHashMap<BlockPos, Integer>();
        for (BlockPos flagPos : flags) {
            if (flagPos == null) continue;
            radii.put(flagPos.immutable(), clamped);
        }
        return radii;
    }

    private static boolean hasSphere(List<FormationSphere> spheres, BlockPos center, int radius) {
        if (spheres == null || center == null) {
            return false;
        }
        int clamped = FormationCorePlateBlockEntity.clampFlagEffectRadius(radius);
        for (FormationSphere sphere : spheres) {
            if (!sphere.center().equals((Object)center) || sphere.radius() != clamped) continue;
            return true;
        }
        return false;
    }

    public void showCompassLockGlowUntil(long gameTime) {
        if (gameTime > this.compassLockGlowUntil) {
            this.compassLockGlowUntil = gameTime;
        }
        this.markDirtyAndSync();
    }

    public LinkFlagResult linkFlag(BlockPos flagPos, FormationFlagBlock clickedFlag) {
        if (this.level == null) {
            return LinkFlagResult.INVALID_FLAG;
        }
        if (SpiritLockHandler.isBlockLocked(this.level, this.getBlockPos()) || SpiritLockHandler.isBlockLocked(this.level, flagPos)) {
            return LinkFlagResult.INVALID_FLAG;
        }
        if (this.activated) {
            return LinkFlagResult.CORE_ACTIVE;
        }
        Block block = this.level.getBlockState(flagPos).getBlock();
        if (!(block instanceof FormationFlagBlock)) {
            return LinkFlagResult.INVALID_FLAG;
        }
        FormationFlagBlock actualFlag = (FormationFlagBlock)block;
        long packed = flagPos.asLong();
        if (this.linkedFlags.contains(packed)) {
            return LinkFlagResult.ALREADY_LINKED;
        }
        if (this.scanManualFlags().size() >= this.maxLinkedFlags()) {
            return LinkFlagResult.MAX_LINKS;
        }
        boolean added = this.linkedFlags.add(packed);
        this.flagEffectRadii.putIfAbsent(packed, 8);
        this.cachedFlagCount = -1;
        this.cachedFlagCountAt = -1L;
        this.markDirtyAndSync();
        return added ? LinkFlagResult.SUCCESS : LinkFlagResult.ALREADY_LINKED;
    }

    private int radiusForFlag(BlockPos flagPos) {
        int fallback = this.activeRadius > 0 ? Math.min(this.activeRadius, 255) : 8;
        return FormationCorePlateBlockEntity.clampFlagEffectRadius(this.flagEffectRadii.getOrDefault(flagPos.asLong(), fallback));
    }

    public static int clampFlagEffectRadius(int radius) {
        return Math.max(1, Math.min(255, radius));
    }

    private void markDirtyAndSync() {
        this.setChanged();
        if (this.level != null && !this.level.isClientSide) {
            this.level.sendBlockUpdated(this.getBlockPos(), this.getBlockState(), this.getBlockState(), 3);
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

    public ActivationResult tryActivate() {
        if (this.level == null || this.level.isClientSide) {
            return ActivationResult.fail((ActivationResultKind)ActivationResultKind.NO_FLAGS);
        }
        if (SpiritLockHandler.isBlockLocked(this.level, this.getBlockPos())) {
            return ActivationResult.fail((ActivationResultKind)ActivationResultKind.NO_QI);
        }
        if (this.currentQi <= 0L) {
            return ActivationResult.fail((ActivationResultKind)ActivationResultKind.NO_QI);
        }
        List<FlagLinkView> flags = this.scanConnectedFlagViews();
        if (flags.isEmpty()) {
            return ActivationResult.fail((ActivationResultKind)ActivationResultKind.NO_FLAGS);
        }
        EnumMap<FormationType, List> flagsByType = new EnumMap<FormationType, List>(FormationType.class);
        EnumMap<FormationType, ItemTier> bestTierByType = new EnumMap<FormationType, ItemTier>(FormationType.class);
        for (FlagLinkView flagLinkView : flags) {
            FormationType formationType = flagLinkView.type();
            flagsByType.computeIfAbsent(formationType, ignored -> new ArrayList()).add(flagLinkView);
            bestTierByType.merge(formationType, flagLinkView.tier(), FormationCorePlateBlockEntity::higherTier);
        }
        if (flagsByType.isEmpty()) {
            return ActivationResult.fail((ActivationResultKind)ActivationResultKind.NO_FLAGS);
        }
        for (Map.Entry entry : flagsByType.entrySet()) {
            FormationType formationType = (FormationType)entry.getKey();
            if (((List)entry.getValue()).size() >= formationType.minFlagsRequired()) continue;
            return ActivationResult.tooFewFlags((int)((List)entry.getValue()).size(), (int)formationType.minFlagsRequired());
        }
        EnumMap<FormationType, ActiveFormationState> nextActiveFormations = new EnumMap<FormationType, ActiveFormationState>(FormationType.class);
        for (Map.Entry entry : flagsByType.entrySet()) {
            FormationType type2 = (FormationType)entry.getKey();
            List<FormationSphere> spheres = this.spheresForFlags((List)entry.getValue());
            int radius = FormationCorePlateBlockEntity.maxConfiguredRadius(spheres);
            int sourcesInRange = this.scanSourcesInRange(spheres);
            long coveredBlocksInRange = type2.drainsByCoveredBlocks() ? this.estimateCoveredBlocksInRange(spheres) : -1L;
            ItemTier flagTier = bestTierByType.getOrDefault(type2, ItemTier.LOW);
            nextActiveFormations.put(type2, new ActiveFormationState(flagTier, radius, sourcesInRange, coveredBlocksInRange, spheres));
        }
        this.activated = true;
        this.activeFormations.clear();
        this.activeFormations.putAll(nextActiveFormations);
        this.drainAccumulator = 0.0;
        this.mazeIntruderTicks.clear();
        this.farmHarvestCursor = 0;
        this.syncLegacyActiveView();
        QiFieldRegistry.of((ServerLevel)((ServerLevel)this.level)).register((IQiFieldEffect)this);
        if (this.hasActiveFormation(FormationType.SECT_PROTECTION)) {
            this.placeSectProtectionBarriers();
            SectProtectionDomeHandler.registerDome(this);
        }
        this.markDirtyAndSync();
        FormationType formationType = this.activeFormations.size() == 1 ? this.activeFormation : null;
        return ActivationResult.success((FormationType)formationType, (int)flags.size(), (int)this.cachedSourcesInRange);
    }

    private static ItemTier higherTier(ItemTier a, ItemTier b) {
        return a.ordinal() >= b.ordinal() ? a : b;
    }

    private void syncLegacyActiveView() {
        if (this.activeFormations.isEmpty()) {
            this.activeFormation = null;
            this.activeFlagTier = null;
            this.activeRadius = 0;
            this.cachedSourcesInRange = -1;
            this.cachedCoveredBlocksInRange = -1L;
            return;
        }
        this.activeFormation = this.activeFormations.keySet().iterator().next();
        ActiveFormationState primary = this.activeFormations.get(this.activeFormation);
        this.activeFlagTier = primary == null ? null : primary.flagTier;
        int broadphaseRadius = 0;
        int totalSources = 0;
        long totalCoveredBlocks = 0L;
        boolean hasCoveredBlocks = false;
        for (ActiveFormationState state : this.activeFormations.values()) {
            broadphaseRadius = Math.max(broadphaseRadius, this.broadphaseRadiusFor(state.spheres));
            totalSources += Math.max(0, state.sourcesInRange);
            if (state.coveredBlocksInRange < 0L) continue;
            totalCoveredBlocks += state.coveredBlocksInRange;
            hasCoveredBlocks = true;
        }
        this.activeRadius = broadphaseRadius;
        this.cachedSourcesInRange = totalSources;
        this.cachedCoveredBlocksInRange = hasCoveredBlocks ? totalCoveredBlocks : -1L;
    }

    private int broadphaseRadiusFor(Collection<FormationSphere> spheres) {
        double max = 0.0;
        Vec3 origin = Vec3.atLowerCornerOf(this.getBlockPos());
        for (FormationSphere sphere : spheres) {
            double edgeDistance = origin.distanceTo(sphere.centerVec()) + (double)sphere.radius();
            max = Math.max(max, edgeDistance);
        }
        return Math.max(1, (int)Math.ceil(max));
    }

    private List<FormationSphere> spheresForFlags(List<FlagLinkView> flags) {
        LinkedHashSet<FormationSphere> spheres = new LinkedHashSet<FormationSphere>();
        BlockPos center = this.getBlockPos();
        for (FlagLinkView flag : flags) {
            spheres.add(new FormationSphere(center, flag.radius()));
        }
        return new ArrayList<FormationSphere>(spheres);
    }

    private static int maxConfiguredRadius(Collection<FormationSphere> spheres) {
        int max = 0;
        for (FormationSphere sphere : spheres) {
            max = Math.max(max, sphere.radius());
        }
        return Math.max(1, max);
    }

    private void placeSectProtectionBarriers() {
        if (this.level == null) {
            return;
        }
        ActiveFormationState state = this.activeFormations.get(FormationType.SECT_PROTECTION);
        if (state == null || state.spheres.isEmpty()) {
            return;
        }
        for (FormationSphere sphere : state.spheres) {
            this.placeBarrierSphere(sphere.center(), sphere.radius());
        }
    }

    private void placeBarrierSphere(BlockPos center, int radius) {
        if (this.level == null) {
            return;
        }
        Block barrierBlock = (Block)ModBlocks.SECT_PROTECTION_BARRIER.get();
        BlockState barrierState = barrierBlock.defaultBlockState();
        long rsq = (long)radius * (long)radius;
        long innerSq = (long)(radius - 1) * (long)(radius - 1);
        BlockPos.MutableBlockPos cur = new BlockPos.MutableBlockPos();
        int placedCount = 0;
        int protectedSolidCount = 0;
        for (int dx = -radius; dx <= radius; ++dx) {
            long dxsq = (long)dx * (long)dx;
            for (int dy = -radius; dy <= radius; ++dy) {
                long dxysq = dxsq + (long)dy * (long)dy;
                for (int dz = -radius; dz <= radius; ++dz) {
                    BlockPos immutable;
                    long packed;
                    long dsq = dxysq + (long)dz * (long)dz;
                    if (dsq > rsq || dsq <= innerSq) continue;
                    cur.set(center.getX() + dx, center.getY() + dy, center.getZ() + dz);
                    BlockState curState = this.level.getBlockState((BlockPos)cur);
                    if (curState.getBlock() instanceof FormationCorePlateBlock || curState.getBlock() instanceof FormationFlagBlock || curState.is(barrierBlock) || this.placedBarriers.contains(packed = (immutable = cur.immutable()).asLong()) || this.savedShellStates.containsKey(packed)) continue;
                    if (!curState.isAir()) {
                        this.savedShellStates.put(packed, curState);
                        if (this.shouldPreserveVisualShellState(curState, immutable)) {
                            if (this.passThroughShellBlockCount < 0) {
                                this.passThroughShellBlockCount = 0;
                            }
                            ++this.passThroughShellBlockCount;
                            ++protectedSolidCount;
                            continue;
                        }
                        if (this.hasPhysicalCollision(curState, immutable)) {
                            ++protectedSolidCount;
                            continue;
                        }
                    }
                    this.level.setBlock(immutable, barrierState, 2);
                    this.placedBarriers.add(packed);
                    ++placedCount;
                }
            }
        }
        if (placedCount > 0 || protectedSolidCount > 0) {
            this.setChanged();
        }
    }

    private void removeBarriers() {
        if (this.level == null || this.placedBarriers.isEmpty()) {
            this.placedBarriers.clear();
            this.savedShellStates.clear();
            this.passThroughShellBlockCount = 0;
            return;
        }
        Block barrierBlock = (Block)ModBlocks.SECT_PROTECTION_BARRIER.get();
        BlockState airState = Blocks.AIR.defaultBlockState();
        for (long packed : this.placedBarriers) {
            BlockPos pos = BlockPos.of(packed);
            BlockState st = this.level.getBlockState(pos);
            if (!st.is(barrierBlock)) continue;
            BlockState restoreTo = this.savedShellStates.getOrDefault(packed, airState);
            this.level.setBlock(pos, restoreTo, 2);
        }
        this.placedBarriers.clear();
        this.savedShellStates.clear();
        this.passThroughShellBlockCount = 0;
        this.setChanged();
    }

    private boolean hasPhysicalCollision(BlockState state, BlockPos pos) {
        return this.level != null && !state.getCollisionShape(this.level, pos).isEmpty();
    }

    private boolean shouldPreserveVisualShellState(BlockState state, BlockPos pos) {
        if (state.isAir()) {
            return false;
        }
        if (!state.getFluidState().isEmpty()) {
            return true;
        }
        return this.isFarmlandCropShellState(state, pos);
    }

    private boolean isFarmlandCropShellState(BlockState state, BlockPos pos) {
        Block block = state.getBlock();
        if (state.is(BlockTags.CROPS) || block instanceof CropBlock || block instanceof StemBlock || block instanceof AttachedStemBlock || block instanceof NetherWartBlock || state.is((Block)ModBlocks.HERB.get())) {
            return true;
        }
        if (this.level == null || !this.level.getBlockState(pos.below()).is(Blocks.FARMLAND)) {
            return false;
        }
        return block instanceof BonemealableBlock || block instanceof IPlantable;
    }

    private void repairLegacySolidShellBarriers() {
        if (this.legacySolidShellRepairChecked) {
            return;
        }
        this.legacySolidShellRepairChecked = true;
        if (this.level == null || this.savedShellStates.isEmpty() || this.placedBarriers.isEmpty()) {
            return;
        }
        Block barrierBlock = (Block)ModBlocks.SECT_PROTECTION_BARRIER.get();
        ArrayList<Long> restored = new ArrayList<Long>();
        for (Map.Entry<Long, BlockState> entry : this.savedShellStates.entrySet()) {
            long packed = entry.getKey();
            if (!this.placedBarriers.contains(packed)) continue;
            BlockPos pos = BlockPos.of(packed);
            BlockState original = entry.getValue();
            if (original.isAir() || !this.hasPhysicalCollision(original, pos) && !this.shouldPreserveVisualShellState(original, pos) || !this.level.getBlockState(pos).is(barrierBlock)) continue;
            this.level.setBlock(pos, original, 2);
            restored.add(packed);
        }
        if (restored.isEmpty()) {
            return;
        }
        for (Long packed : restored) {
            this.placedBarriers.remove(packed);
        }
        this.passThroughShellBlockCount = -1;
        this.setChanged();
    }

    private int scanSourcesInRange(int radius) {
        if (this.level == null) {
            return 0;
        }
        return this.scanSourcesInRange(List.of(new FormationSphere(this.getBlockPos(), radius)));
    }

    private int scanSourcesInRange(Collection<FormationSphere> spheres) {
        if (this.level == null || spheres == null || spheres.isEmpty()) {
            return 0;
        }
        int count = 0;
        BlockPos.MutableBlockPos cur = new BlockPos.MutableBlockPos();
        HashSet<Long> seen = new HashSet<Long>();
        for (FormationSphere sphere : spheres) {
            int radius = Math.max(1, sphere.radius());
            BlockPos center = sphere.center();
            long rsq = (long)radius * (long)radius;
            for (int dx = -radius; dx <= radius; ++dx) {
                long dxsq = (long)dx * (long)dx;
                for (int dy = -radius; dy <= radius; ++dy) {
                    long dxysq = dxsq + (long)dy * (long)dy;
                    for (int dz = -radius; dz <= radius; ++dz) {
                        if (dxysq + (long)dz * (long)dz > rsq) continue;
                        cur.set(center.getX() + dx, center.getY() + dy, center.getZ() + dz);
                        if (!seen.add(cur.asLong()) || BlockQiSpecs.of((BlockState)this.level.getBlockState((BlockPos)cur)) == null) continue;
                        ++count;
                    }
                }
            }
        }
        return count;
    }

    public void deactivate() {
        if (!this.activated) {
            return;
        }
        boolean hadSectProtection = this.hasActiveFormation(FormationType.SECT_PROTECTION);
        Level level = this.level;
        if (level instanceof ServerLevel) {
            ServerLevel sl = (ServerLevel)level;
            QiFieldRegistry.of((ServerLevel)sl).unregister((IQiFieldEffect)this);
        }
        SectProtectionDomeHandler.unregisterDome(this);
        if (hadSectProtection) {
            this.removeBarriers();
        }
        this.activated = false;
        this.activeFormations.clear();
        this.activeFormation = null;
        this.activeFlagTier = null;
        this.activeRadius = 0;
        this.drainAccumulator = 0.0;
        this.cachedSourcesInRange = -1;
        this.cachedCoveredBlocksInRange = -1L;
        this.mazeIntruderTicks.clear();
        this.farmHarvestCursor = 0;
        this.lazyRegistered = false;
        this.spiritLockSuppressed = false;
        this.skipFlagValidationUntil = -1L;
        this.markDirtyAndSync();
    }

    public void onBlockRemoved() {
        this.deactivate();
    }

    public void setRemoved() {
        Level level;
        super.setRemoved();
        this.lazyRegistered = false;
        this.deferActiveFlagValidation();
        if (this.activated && (level = this.level) instanceof ServerLevel) {
            ServerLevel sl = (ServerLevel)level;
            QiFieldRegistry.of((ServerLevel)sl).unregister((IQiFieldEffect)this);
            SectProtectionDomeHandler.unregisterDome(this);
        }
    }

    public void clearRemoved() {
        super.clearRemoved();
        this.lazyRegistered = false;
        this.deferActiveFlagValidation();
    }

    public void onLoad() {
        super.onLoad();
        if (this.level != null && !this.level.isClientSide) {
            this.lazyRegistered = false;
            this.deferActiveFlagValidation();
            this.ensureRegistered();
        }
    }

    public int scanFlagsCount() {
        if (this.level == null) {
            return 0;
        }
        boolean hasUnloadedStoredLinks = this.hasUnloadedStoredFlagLinks();
        long now = this.level.getGameTime();
        if (!hasUnloadedStoredLinks && this.cachedFlagCount >= 0 && now - this.cachedFlagCountAt < 60L) {
            return this.cachedFlagCount;
        }
        int detected = this.scanConnectedFlagViews().size();
        if (hasUnloadedStoredLinks) {
            this.cachedFlagCount = -1;
            this.cachedFlagCountAt = -1L;
            return detected;
        }
        this.cachedFlagCount = detected;
        this.cachedFlagCountAt = now;
        return detected;
    }

    private List<BlockPos> scanManualFlags() {
        ArrayList<BlockPos> result = new ArrayList<BlockPos>();
        if (this.level == null) {
            return result;
        }
        ArrayList<Long> stale = new ArrayList<Long>();
        for (long packed : this.linkedFlags) {
            BlockPos pos = BlockPos.of(packed);
            if (!this.level.isLoaded(pos)) continue;
            BlockState state = this.level.getBlockState(pos);
            if (state.getBlock() instanceof FormationFlagBlock) {
                result.add(pos);
                continue;
            }
            stale.add(packed);
        }
        if (!stale.isEmpty()) {
            this.linkedFlags.removeAll(stale);
            this.cachedFlagCount = -1;
            this.cachedFlagCountAt = -1L;
            this.markDirtyAndSync();
        }
        return result;
    }

    private boolean hasUnloadedStoredFlagLinks() {
        if (this.level == null) {
            return false;
        }
        for (long packed : this.linkedFlags) {
            if (this.level.isLoaded(BlockPos.of(packed))) continue;
            return true;
        }
        return false;
    }

    private boolean canValidateActiveFlagsNow() {
        ServerLevel sl;
        if (this.level == null) {
            return false;
        }
        if (this.hasUnloadedStoredFlagLinks()) {
            return false;
        }
        Level level = this.level;
        return !(level instanceof ServerLevel) || (sl = (ServerLevel)level).getGameTime() >= this.skipFlagValidationUntil;
    }

    private void deferActiveFlagValidation() {
        Level level = this.level;
        if (level instanceof ServerLevel) {
            ServerLevel sl = (ServerLevel)level;
            this.skipFlagValidationUntil = Math.max(this.skipFlagValidationUntil, sl.getGameTime() + 100L);
        }
        this.cachedFlagCount = -1;
        this.cachedFlagCountAt = -1L;
    }

    private List<FlagLinkView> scanConnectedFlagViews() {
        if (this.level == null) {
            return Collections.emptyList();
        }
        LinkedHashMap<Long, MutableFlagLink> merged = new LinkedHashMap<Long, MutableFlagLink>();
        for (BlockPos pos : this.scanManualFlags()) {
            FormationCorePlateBlockEntity.addFlagCandidate(merged, pos, false, false, true);
        }
        for (Direction dir : Direction.values()) {
            BlockPos pos = this.getBlockPos().relative(dir);
            if (!(this.level.getBlockState(pos).getBlock() instanceof FormationFlagBlock)) continue;
            FormationCorePlateBlockEntity.addFlagCandidate(merged, pos, true, false, false);
        }
        Level level = this.level;
        if (level instanceof ServerLevel) {
            ServerLevel sl = (ServerLevel)level;
            for (BlockPos pos : FormationRuneBlockEntity.connectedFormationFlags((ServerLevel)sl, (BlockPos)this.getBlockPos())) {
                if (!(this.level.getBlockState(pos).getBlock() instanceof FormationFlagBlock)) continue;
                FormationCorePlateBlockEntity.addFlagCandidate(merged, pos, false, true, false);
            }
        }
        ArrayList<FlagLinkView> result = new ArrayList<FlagLinkView>();
        for (MutableFlagLink link : merged.values()) {
            BlockState state = this.level.getBlockState(link.pos);
            Block block = state.getBlock();
            if (!(block instanceof FormationFlagBlock)) continue;
            FormationFlagBlock flag = (FormationFlagBlock)block;
            if (SpiritLockHandler.isBlockLocked(this.level, link.pos)) continue;
            int radius = this.radiusForFlag(link.pos);
            this.flagEffectRadii.putIfAbsent(link.pos.asLong(), radius);
            result.add(new FlagLinkView(link.pos, flag.formationType(), flag.flagTier(), radius, link.directLinked, link.runeLinked, link.manualLinked));
        }
        return result;
    }

    private static void addFlagCandidate(Map<Long, MutableFlagLink> merged, BlockPos pos, boolean direct, boolean rune, boolean manual) {
        MutableFlagLink link = merged.computeIfAbsent(pos.asLong(), ignored -> new MutableFlagLink(pos.immutable()));
        link.directLinked |= direct;
        link.runeLinked |= rune;
        link.manualLinked |= manual;
    }

    public void serverTick() {
        Level level;
        Level level2;
        this.tickCompassLockGlow();
        if (this.level != null && !this.level.isClientSide && this.clampCurrentQiToMax()) {
            this.markDirtyAndSync();
        }
        if (!this.activated && (level2 = this.level) instanceof ServerLevel) {
            ServerLevel sl = (ServerLevel)level2;
            SectSavedData.get((ServerLevel)sl).configureGeneratedArrayCoreIfReady(sl, this);
        } else if (this.activated && (level = this.level) instanceof ServerLevel) {
            ServerLevel sl = (ServerLevel)level;
            if (this.coreTier() == CoreTier.IMMORTAL && this.level.getGameTime() % 200L == 0L) {
                SectSavedData.get((ServerLevel)sl).configureGeneratedArrayCoreIfReady(sl, this);
            }
        }
        if (!this.activated || this.activeFormations.isEmpty() || this.level == null) {
            return;
        }
        if (this.isSpiritLockSuppressedNow()) {
            this.suppressForSpiritLock();
            return;
        }
        if (this.spiritLockSuppressed) {
            this.resumeFromSpiritLock();
            if (this.spiritLockSuppressed) {
                return;
            }
        }
        this.ensureRegistered();
        if (this.hasActiveFormation(FormationType.SECT_PROTECTION)) {
            this.repairLegacySolidShellBarriers();
        }
        boolean refreshedCachedState = false;
        for (Map.Entry entry : this.activeFormations.entrySet()) {
            FormationType type = (FormationType)entry.getKey();
            ActiveFormationState state = (ActiveFormationState)entry.getValue();
            if (state.sourcesInRange < 0) {
                state.sourcesInRange = this.scanSourcesInRange(state.spheres);
                refreshedCachedState = true;
            }
            if (!type.drainsByCoveredBlocks() || state.coveredBlocksInRange >= 0L) continue;
            state.coveredBlocksInRange = this.estimateCoveredBlocksInRange(state.spheres);
            refreshedCachedState = true;
        }
        if (refreshedCachedState) {
            this.syncLegacyActiveView();
            this.setChanged();
        }
        if (this.canValidateActiveFlagsNow() && this.scanFlagsCount() == 0) {
            this.deactivate();
            return;
        }
        double drainPerSecond = this.getDrainPerSec();
        this.drainAccumulator += drainPerSecond / 20.0;
        if (this.drainAccumulator >= 1.0) {
            long deduct = (long)this.drainAccumulator;
            this.drainAccumulator -= (double)deduct;
            this.currentQi -= deduct;
            if (this.currentQi <= 0L) {
                this.currentQi = 0L;
                this.deactivate();
                return;
            }
            this.setChanged();
        }
        if (this.level.getGameTime() % 10L == 0L) {
            this.revalidateActiveFormationsAgainstFlags();
            if (!this.activated || this.activeFormations.isEmpty()) {
                return;
            }
        }
        this.applyActiveFormationEffects();
    }

    private void revalidateActiveFormationsAgainstFlags() {
        this.refreshActiveFormationsFromConnectedFlags();
    }

    private void refreshActiveFormationsFromConnectedFlags() {
        this.refreshActiveFormationsFromConnectedFlags(false);
    }

    private void refreshActiveFormationsFromConnectedFlags(boolean forceValidation) {
        if (this.level == null || this.activeFormations.isEmpty()) {
            return;
        }
        if (!forceValidation && !this.canValidateActiveFlagsNow()) {
            return;
        }
        List<FlagLinkView> currentFlags = this.scanConnectedFlagViews();
        EnumMap<FormationType, List<FlagLinkView>> flagsByType = new EnumMap<FormationType, List<FlagLinkView>>(FormationType.class);
        EnumMap<FormationType, ItemTier> bestTierByType = new EnumMap<FormationType, ItemTier>(FormationType.class);
        for (FlagLinkView flag : currentFlags) {
            flagsByType.computeIfAbsent(flag.type(), ignored -> new ArrayList<FlagLinkView>()).add(flag);
            bestTierByType.merge(flag.type(), flag.tier(), FormationCorePlateBlockEntity::higherTier);
        }
        boolean changed = false;
        boolean removedSectProtection = false;
        boolean refreshedSectProtection = false;
        Iterator<Map.Entry<FormationType, ActiveFormationState>> it = this.activeFormations.entrySet().iterator();
        while (it.hasNext()) {
            boolean stateChanged;
            Map.Entry<FormationType, ActiveFormationState> entry = it.next();
            FormationType type = entry.getKey();
            List<FlagLinkView> flags = flagsByType.getOrDefault(type, Collections.emptyList());
            if (flags.size() < type.minFlagsRequired()) {
                Level level = this.level;
                if (level instanceof ServerLevel) {
                    ServerLevel sl = (ServerLevel)level;
                    this.cleanupShortLivedFormationEffects(sl, type, entry.getValue());
                }
                if (type == FormationType.SECT_PROTECTION) {
                    removedSectProtection = true;
                }
                it.remove();
                changed = true;
                continue;
            }
            List<FormationSphere> spheres = this.spheresForFlags(flags);
            ActiveFormationState state = entry.getValue();
            ItemTier nextTier = bestTierByType.getOrDefault(type, ItemTier.LOW);
            boolean bl = stateChanged = state.flagTier != nextTier || state.radius != FormationCorePlateBlockEntity.maxConfiguredRadius(spheres) || !FormationCorePlateBlockEntity.sameSpheres(state.spheres, spheres);
            if (!stateChanged) continue;
            state.flagTier = nextTier;
            state.radius = FormationCorePlateBlockEntity.maxConfiguredRadius(spheres);
            state.sourcesInRange = this.scanSourcesInRange(spheres);
            state.coveredBlocksInRange = type.drainsByCoveredBlocks() ? this.estimateCoveredBlocksInRange(spheres) : -1L;
            state.spheres = List.copyOf(spheres);
            changed = true;
            if (type != FormationType.SECT_PROTECTION) continue;
            refreshedSectProtection = true;
        }
        if (changed) {
            if (removedSectProtection || refreshedSectProtection) {
                this.removeBarriers();
                if (!removedSectProtection && this.activeFormations.containsKey(FormationType.SECT_PROTECTION)) {
                    this.placeSectProtectionBarriers();
                    if (this.level instanceof ServerLevel) {
                        SectProtectionDomeHandler.registerDome(this);
                    }
                } else {
                    SectProtectionDomeHandler.unregisterDome(this);
                }
            }
            if (this.activeFormations.isEmpty()) {
                this.deactivate();
            } else {
                this.syncLegacyActiveView();
                this.markDirtyAndSync();
            }
        }
    }

    private static boolean sameSpheres(List<FormationSphere> left, List<FormationSphere> right) {
        if (left.size() != right.size()) {
            return false;
        }
        for (int i = 0; i < left.size(); ++i) {
            FormationSphere a = left.get(i);
            FormationSphere b = right.get(i);
            if (a.center().equals((Object)b.center()) && a.radius() == b.radius()) continue;
            return false;
        }
        return true;
    }

    private void cleanupShortLivedFormationEffects(ServerLevel sl, FormationType removedType, ActiveFormationState removedState) {
        for (LivingEntity entity : this.livingEntitiesInFormation(sl, removedState)) {
            switch (removedType) {
                case REJUVENATION -> {
                    FormationCorePlateBlockEntity.tryRemoveShortEffect(entity, MobEffects.REGENERATION);
                }
                case FLIGHT_BAN -> {
                    FormationCorePlateBlockEntity.tryRemoveShortEffect(entity, MobEffects.MOVEMENT_SLOWDOWN);
                }
                case MAZE -> {
                    FormationCorePlateBlockEntity.tryRemoveShortEffect(entity, MobEffects.BLINDNESS);
                    FormationCorePlateBlockEntity.tryRemoveShortEffect(entity, MobEffects.CONFUSION);
                }
                default -> {
                }
            }
        }
    }

    private static void tryRemoveShortEffect(LivingEntity entity, MobEffect effect) {
        MobEffectInstance eff = entity.getEffect(effect);
        if (eff == null || eff.isInfiniteDuration()) {
            return;
        }
        if (eff.getDuration() <= 17) {
            entity.removeEffect(effect);
        }
    }

    private boolean isSpiritLockSuppressedNow() {
        return SpiritLockHandler.isBlockLocked(this.level, this.getBlockPos()) || this.hasLockedLinkedFlag();
    }

    private boolean hasLockedLinkedFlag() {
        if (this.level == null) {
            return false;
        }
        for (FlagLinkView flag : this.scanConnectedFlagViews()) {
            if (!SpiritLockHandler.isBlockLocked(this.level, flag.pos())) continue;
            return true;
        }
        return false;
    }

    private void suppressForSpiritLock() {
        if (this.spiritLockSuppressed) {
            return;
        }
        boolean hadSectProtection = this.activeFormations.containsKey(FormationType.SECT_PROTECTION);
        Level level = this.level;
        if (level instanceof ServerLevel) {
            ServerLevel sl = (ServerLevel)level;
            QiFieldRegistry.of((ServerLevel)sl).unregister((IQiFieldEffect)this);
        }
        SectProtectionDomeHandler.unregisterDome(this);
        if (hadSectProtection) {
            this.removeBarriers();
        }
        this.spiritLockSuppressed = true;
        this.lazyRegistered = false;
        this.markDirtyAndSync();
    }

    private void resumeFromSpiritLock() {
        if (!this.spiritLockSuppressed || this.level == null || this.isSpiritLockSuppressedNow()) {
            return;
        }
        this.spiritLockSuppressed = false;
        Level level = this.level;
        if (level instanceof ServerLevel) {
            ServerLevel sl = (ServerLevel)level;
            QiFieldRegistry.of((ServerLevel)sl).register((IQiFieldEffect)this);
            this.lazyRegistered = true;
            if (this.activeFormations.containsKey(FormationType.SECT_PROTECTION)) {
                this.placeSectProtectionBarriers();
                SectProtectionDomeHandler.registerDome(this);
            }
        }
        this.markDirtyAndSync();
    }

    private void tickCompassLockGlow() {
        Level level = this.level;
        if (!(level instanceof ServerLevel)) {
            return;
        }
        ServerLevel sl = (ServerLevel)level;
        long now = sl.getGameTime();
        if (this.compassLockGlowUntil < now) {
            return;
        }
        if (now % 10L != 0L) {
            return;
        }
        Vec3 center = Vec3.atLowerCornerOf(this.getBlockPos());
        sl.sendParticles((ParticleOptions)((SimpleParticleType)ModParticles.AMBIENT_QI.get()), center.x, center.y + 0.25, center.z, 18, 0.42, 0.14, 0.42, 0.012);
        sl.sendParticles((ParticleOptions)ParticleTypes.END_ROD, center.x, center.y + 0.35, center.z, 4, 0.28, 0.08, 0.28, 0.004);
    }

    private void applyActiveFormationEffects() {
        ActiveFormationState sectProtection;
        ActiveFormationState maze;
        ActiveFormationState flightBan;
        int interval;
        ActiveFormationState farmHarvest;
        ActiveFormationState witherGrowth;
        ServerLevel sl;
        Level level = this.level;
        if (!(level instanceof ServerLevel)) {
            return;
        }
        sl = (ServerLevel)level;
        if (this.activeFormations.isEmpty()) {
            return;
        }
        long gameTime = sl.getGameTime();
        if (!this.activeFormations.containsKey(FormationType.MAZE) && !this.mazeIntruderTicks.isEmpty()) {
            this.mazeIntruderTicks.clear();
        }
        if ((witherGrowth = this.activeFormations.get(FormationType.WITHER_GROWTH)) != null && gameTime % 20L == 0L) {
            this.accelerateGrowth(sl, witherGrowth);
        }
        if ((farmHarvest = this.activeFormations.get(FormationType.FARM_HARVEST)) != null && (interval = FormationType.FARM_HARVEST.harvestIntervalTicks(farmHarvest.flagTier)) > 0 && gameTime % (long)interval == 0L) {
            this.applyFarmHarvest(sl, farmHarvest);
        }
        if (gameTime % 10L != 0L) {
            return;
        }
        ActiveFormationState rejuvenation = this.activeFormations.get(FormationType.REJUVENATION);
        if (rejuvenation != null) {
            this.applyRejuvenation(sl, rejuvenation);
        }
        if ((flightBan = this.activeFormations.get(FormationType.FLIGHT_BAN)) != null) {
            this.applyFlightBan(sl, flightBan);
        }
        if ((maze = this.activeFormations.get(FormationType.MAZE)) != null) {
            this.applyMaze(sl, maze);
        }
        if ((sectProtection = this.activeFormations.get(FormationType.SECT_PROTECTION)) != null) {
            this.applySectProtectionShellDamage(sl, sectProtection);
        }
    }

    private void accelerateGrowth(ServerLevel sl, ActiveFormationState formationState) {
        double multiplier = FormationType.WITHER_GROWTH.growthMultiplier(formationState.flagTier);
        int attempts = Math.min(4096, Math.max(32, (int)Math.ceil((double)(FormationCorePlateBlockEntity.maxConfiguredRadius(formationState.spheres) * FormationCorePlateBlockEntity.maxConfiguredRadius(formationState.spheres)) * Math.max(1.0, multiplier - 1.0) / 2.0)));
        RandomSource random = sl.random;
        for (int i = 0; i < attempts; ++i) {
            BlockPos pos = this.randomPosInFormation(random, formationState);
            BlockState state = sl.getBlockState(pos);
            if (!state.isRandomlyTicking()) continue;
            state.randomTick(sl, pos, random);
        }
    }

    private void applyFarmHarvest(ServerLevel sl, ActiveFormationState formationState) {
        int checked;
        int targetCount = Math.max(1, FormationType.FARM_HARVEST.harvestBatchSize(formationState.flagTier));
        long volume = this.estimateCoveredBlocksInRange(formationState.spheres);
        int maxChecks = (int)Math.min(Math.max(1L, volume), (long)Math.min(16384, Math.max(4096, targetCount * 1024)));
        int harvested = 0;
        RandomSource random = sl.random;
        for (checked = 0; checked < maxChecks && harvested < targetCount; ++checked) {
            if (!this.tryFarmHarvestAt(sl, this.randomPosInFormation(random, formationState), formationState)) continue;
            ++harvested;
        }
        this.farmHarvestCursor += checked;
    }

    @Nullable
    private BlockPos indexedPosInFormation(int index, int radius, int diameter) {
        int xIndex = index % diameter;
        int dx = xIndex - radius;
        int yIndex = index / diameter % diameter;
        int dy = yIndex - radius;
        int zIndex = index / (diameter * diameter);
        int dz = zIndex - radius;
        if ((long)dx * (long)dx + (long)dy * (long)dy + (long)dz * (long)dz > (long)radius * (long)radius) {
            return null;
        }
        return this.getBlockPos().offset(dx, dy, dz);
    }

    private boolean tryFarmHarvestAt(ServerLevel sl, BlockPos pos, ActiveFormationState formationState) {
        BlockState state = sl.getBlockState(pos);
        HarvestPlan plan = this.harvestPlan(sl, pos, state);
        if (plan == null) {
            return false;
        }
        if (plan.replantState() != null && plan.seedStack().isEmpty()) {
            return false;
        }
        if (plan.requiresSeed() && !plan.replantState().canSurvive((LevelReader)sl, pos)) {
            return false;
        }
        BlockEntity blockEntity = state.hasBlockEntity() ? sl.getBlockEntity(pos) : null;
        ArrayList<ItemStack> drops = new ArrayList<ItemStack>();
        for (ItemStack drop : Block.getDrops((BlockState)state, (ServerLevel)sl, (BlockPos)pos, (BlockEntity)blockEntity)) {
            if (drop.isEmpty()) continue;
            drops.add(drop.copy());
        }
        if (FormationType.FARM_HARVEST.harvestDoublesDrops(formationState.flagTier)) {
            for (ItemStack drop : drops) {
                drop.setCount(drop.getCount() * 2);
            }
        }
        List<BlockEntity> storages = this.findStoragesInFormation(sl, pos, formationState);
        boolean seedFromStorage = false;
        if (plan.requiresSeed() && !FormationCorePlateBlockEntity.consumeSeedFromDrops(drops, plan.seedStack())) {
            if (!FormationCorePlateBlockEntity.canExtractSeed(storages, plan.seedStack())) {
                return false;
            }
            seedFromStorage = true;
        }
        if (!drops.isEmpty() && !FormationCorePlateBlockEntity.canInsertAll(storages, drops)) {
            return false;
        }
        FormationCorePlateBlockEntity.removeHarvestedBlock(sl, pos, state);
        if (seedFromStorage) {
            FormationCorePlateBlockEntity.extractSeed(storages, plan.seedStack());
        }
        if (plan.requiresSeed()) {
            sl.setBlock(pos, plan.replantState(), 3);
            this.sendFarmReplantEffect(sl, pos);
        }
        if (!drops.isEmpty()) {
            FormationCorePlateBlockEntity.insertDrops(storages, drops);
        }
        this.sendFarmHarvestEffect(sl, pos);
        return true;
    }

    @Nullable
    private HarvestPlan harvestPlan(ServerLevel sl, BlockPos pos, BlockState state) {
        Block block = state.getBlock();
        if (block instanceof StemBlock || block instanceof AttachedStemBlock) {
            return null;
        }
        if (block instanceof CropBlock) {
            CropBlock crop = (CropBlock)block;
            if (!crop.isMaxAge(state)) {
                return null;
            }
            return HarvestPlan.replant((BlockState)crop.getStateForAge(0), (ItemStack)FormationCorePlateBlockEntity.seedStack(sl, pos, state));
        }
        if (block instanceof NetherWartBlock) {
            IntegerProperty age = FormationCorePlateBlockEntity.ageProperty(state);
            if (age == null || (Integer)state.getValue(age) < FormationCorePlateBlockEntity.maxAge(age)) {
                return null;
            }
            return HarvestPlan.replant((BlockState)state.setValue(age, FormationCorePlateBlockEntity.minAge(age)), (ItemStack)FormationCorePlateBlockEntity.seedStack(sl, pos, state));
        }
        if (state.is(BlockTags.CROPS)) {
            IntegerProperty age = FormationCorePlateBlockEntity.ageProperty(state);
            if (age == null || (Integer)state.getValue(age) < FormationCorePlateBlockEntity.maxAge(age)) {
                return null;
            }
            return HarvestPlan.replant((BlockState)state.setValue(age, FormationCorePlateBlockEntity.minAge(age)), (ItemStack)FormationCorePlateBlockEntity.seedStack(sl, pos, state));
        }
        if (state.is(Blocks.MELON) || state.is(Blocks.PUMPKIN) || FormationCorePlateBlockEntity.isHarvestableGrass(state)) {
            return HarvestPlan.breakOnly();
        }
        if (state.is((Block)ModBlocks.HERB.get())) {
            return HarvestPlan.replant((BlockState)((Block)ModBlocks.HERB.get()).defaultBlockState(), (ItemStack)FormationCorePlateBlockEntity.seedStack(sl, pos, state));
        }
        return null;
    }

    private static boolean isHarvestableGrass(BlockState state) {
        return state.is(Blocks.GRASS) || state.is(Blocks.TALL_GRASS) || state.is(Blocks.FERN) || state.is(Blocks.LARGE_FERN);
    }

    private static void removeHarvestedBlock(ServerLevel sl, BlockPos pos, BlockState state) {
        if (state.hasProperty(DoublePlantBlock.HALF)) {
            BlockPos other;
            DoubleBlockHalf half = (DoubleBlockHalf)state.getValue(DoublePlantBlock.HALF);
            BlockPos blockPos = other = half == DoubleBlockHalf.LOWER ? pos.above() : pos.below();
            if (sl.getBlockState(other).is(state.getBlock())) {
                sl.setBlock(other, Blocks.AIR.defaultBlockState(), 3);
            }
        }
        sl.setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
    }

    private static ItemStack seedStack(ServerLevel sl, BlockPos pos, BlockState state) {
        ItemStack picked = state.getBlock().getCloneItemStack((BlockGetter)sl, pos, state);
        if (!picked.isEmpty()) {
            picked.setCount(1);
            return picked;
        }
        ItemStack fallback = new ItemStack((ItemLike)state.getBlock().asItem());
        if (!fallback.isEmpty()) {
            fallback.setCount(1);
        }
        return fallback;
    }

    @Nullable
    private static IntegerProperty ageProperty(BlockState state) {
        for (Property property : state.getProperties()) {
            IntegerProperty integerProperty;
            if (!(property instanceof IntegerProperty) || !"age".equals((integerProperty = (IntegerProperty)property).getName())) continue;
            return integerProperty;
        }
        return null;
    }

    private static int minAge(IntegerProperty property) {
        int min = Integer.MAX_VALUE;
        for (Integer value : property.getPossibleValues()) {
            min = Math.min(min, value);
        }
        return min == Integer.MAX_VALUE ? 0 : min;
    }

    private static int maxAge(IntegerProperty property) {
        int max = Integer.MIN_VALUE;
        for (Integer value : property.getPossibleValues()) {
            max = Math.max(max, value);
        }
        return max == Integer.MIN_VALUE ? 0 : max;
    }

    private List<BlockEntity> findStoragesInFormation(ServerLevel sl, BlockPos harvestPos, ActiveFormationState formationState) {
        ArrayList<BlockEntity> storages = new ArrayList<BlockEntity>();
        HashSet<BlockEntity> seen = new HashSet<BlockEntity>();
        for (FormationSphere sphere : formationState.spheres) {
            BlockPos center = sphere.center();
            int radius = Math.max(1, sphere.radius());
            int minChunkX = center.getX() - radius >> 4;
            int maxChunkX = center.getX() + radius >> 4;
            int minChunkZ = center.getZ() - radius >> 4;
            int maxChunkZ = center.getZ() + radius >> 4;
            for (int cx = minChunkX; cx <= maxChunkX; ++cx) {
                for (int cz = minChunkZ; cz <= maxChunkZ; ++cz) {
                    if (!sl.hasChunkAt(cx, cz)) continue;
                    LevelChunk chunk = sl.getChunk(cx, cz);
                    for (BlockEntity be2 : chunk.getBlockEntities().values()) {
                        if (be2 == this || seen.contains(be2) || !FormationCorePlateBlockEntity.containsAnySphere(formationState.spheres, be2.getBlockPos()) || FormationCorePlateBlockEntity.firstItemHandler(be2) == null) continue;
                        seen.add(be2);
                        storages.add(be2);
                    }
                }
            }
        }
        storages.sort(Comparator.comparingDouble(be -> be.getBlockPos().distSqr((Vec3i)harvestPos)));
        return storages;
    }

    @Nullable
    private static IItemHandler firstItemHandler(BlockEntity be) {
        IItemHandler direct = be.getCapability(ForgeCapabilities.ITEM_HANDLER, null).resolve().orElse(null);
        if (direct != null) {
            return direct;
        }
        for (Direction direction : Direction.values()) {
            IItemHandler sided = be.getCapability(ForgeCapabilities.ITEM_HANDLER, direction).resolve().orElse(null);
            if (sided == null) continue;
            return sided;
        }
        return null;
    }

    private static boolean consumeSeedFromDrops(List<ItemStack> drops, ItemStack seed) {
        if (seed.isEmpty()) {
            return false;
        }
        for (ItemStack drop : drops) {
            if (!FormationCorePlateBlockEntity.sameItem(drop, seed) || drop.getCount() <= 0) continue;
            drop.shrink(1);
            drops.removeIf(ItemStack::isEmpty);
            return true;
        }
        return false;
    }

    private static boolean canExtractSeed(List<BlockEntity> storages, ItemStack seed) {
        if (seed.isEmpty()) {
            return false;
        }
        for (BlockEntity be : storages) {
            IItemHandler handler = FormationCorePlateBlockEntity.firstItemHandler(be);
            if (handler == null) continue;
            for (int slot = 0; slot < handler.getSlots(); ++slot) {
                ItemStack extracted;
                ItemStack inSlot = handler.getStackInSlot(slot);
                if (!FormationCorePlateBlockEntity.sameItem(inSlot, seed) || (extracted = handler.extractItem(slot, 1, true)).isEmpty() || !FormationCorePlateBlockEntity.sameItem(extracted, seed)) continue;
                return true;
            }
        }
        return false;
    }

    private static boolean extractSeed(List<BlockEntity> storages, ItemStack seed) {
        if (seed.isEmpty()) {
            return false;
        }
        for (BlockEntity be : storages) {
            IItemHandler handler = FormationCorePlateBlockEntity.firstItemHandler(be);
            if (handler == null) continue;
            for (int slot = 0; slot < handler.getSlots(); ++slot) {
                ItemStack extracted;
                ItemStack inSlot = handler.getStackInSlot(slot);
                if (!FormationCorePlateBlockEntity.sameItem(inSlot, seed) || (extracted = handler.extractItem(slot, 1, false)).isEmpty() || !FormationCorePlateBlockEntity.sameItem(extracted, seed)) continue;
                return true;
            }
        }
        return false;
    }

    private static boolean canInsertAll(List<BlockEntity> storages, List<ItemStack> drops) {
        ArrayList<StorageSnapshot> snapshots = new ArrayList<StorageSnapshot>();
        for (BlockEntity be : storages) {
            IItemHandler handler = FormationCorePlateBlockEntity.firstItemHandler(be);
            if (handler == null) continue;
            snapshots.add(new StorageSnapshot(handler));
        }
        for (ItemStack drop : drops) {
            StorageSnapshot snapshot;
            ItemStack remaining = drop.copy();
            Iterator iterator = snapshots.iterator();
            while (iterator.hasNext() && !(remaining = (snapshot = (StorageSnapshot)iterator.next()).insert(remaining)).isEmpty()) {
            }
            if (remaining.isEmpty()) continue;
            return false;
        }
        return true;
    }

    private static void insertDrops(List<BlockEntity> storages, List<ItemStack> drops) {
        for (ItemStack drop : drops) {
            BlockEntity be;
            IItemHandler handler;
            ItemStack remaining = drop.copy();
            Iterator<BlockEntity> iterator = storages.iterator();
            while (iterator.hasNext() && ((handler = FormationCorePlateBlockEntity.firstItemHandler(be = iterator.next())) == null || !(remaining = FormationCorePlateBlockEntity.insertIntoHandler(handler, remaining, false)).isEmpty())) {
            }
        }
    }

    private static ItemStack insertIntoHandler(IItemHandler handler, ItemStack stack, boolean simulate) {
        ItemStack remaining = stack;
        for (int slot = 0; slot < handler.getSlots() && !remaining.isEmpty(); ++slot) {
            remaining = handler.insertItem(slot, remaining, simulate);
        }
        return remaining;
    }

    private static boolean sameItem(ItemStack left, ItemStack right) {
        return !left.isEmpty() && !right.isEmpty() && ItemStack.isSameItem((ItemStack)left, (ItemStack)right);
    }

    private void sendFarmHarvestEffect(ServerLevel sl, BlockPos pos) {
        Vec3 center = Vec3.atLowerCornerOf(pos);
        sl.sendParticles((ParticleOptions)((SimpleParticleType)ModParticles.AMBIENT_QI_WOOD.get()), center.x, center.y + 0.08, center.z, 18, 0.38, 0.06, 0.38, 0.018);
        sl.playSound(null, pos, SoundEvents.CROP_BREAK, SoundSource.BLOCKS, 0.45f, 1.15f + sl.random.nextFloat() * 0.15f);
    }

    private void sendFarmReplantEffect(ServerLevel sl, BlockPos pos) {
        Vec3 center = Vec3.atLowerCornerOf(pos);
        sl.sendParticles((ParticleOptions)((SimpleParticleType)ModParticles.AMBIENT_QI_WOOD.get()), center.x, center.y + 0.02, center.z, 10, 0.22, 0.02, 0.22, 0.012);
    }

    private BlockPos randomPosInFormation(RandomSource random, ActiveFormationState formationState) {
        if (formationState.spheres.isEmpty()) {
            return this.getBlockPos();
        }
        FormationSphere sphere = (FormationSphere)formationState.spheres.get(random.nextInt(formationState.spheres.size()));
        BlockPos center = sphere.center();
        int radius = Math.max(1, sphere.radius());
        long rsq = (long)radius * (long)radius;
        for (int attempt = 0; attempt < 16; ++attempt) {
            int dz;
            int dy;
            int dx = random.nextInt(radius * 2 + 1) - radius;
            if ((long)dx * (long)dx + (long)(dy = random.nextInt(radius * 2 + 1) - radius) * (long)dy + (long)(dz = random.nextInt(radius * 2 + 1) - radius) * (long)dz > rsq) continue;
            return center.offset(dx, dy, dz);
        }
        return center;
    }

    @Nullable
    private static FormationSphere nearestSphere(List<FormationSphere> spheres, Vec3 point) {
        FormationSphere best = null;
        double bestDistance = Double.MAX_VALUE;
        for (FormationSphere sphere : spheres) {
            double distance = sphere.centerVec().distanceToSqr(point);
            if (!(distance < bestDistance)) continue;
            bestDistance = distance;
            best = sphere;
        }
        return best;
    }

    private static boolean containsAnySphere(Collection<FormationSphere> spheres, BlockPos pos) {
        return FormationCorePlateBlockEntity.containsAnySphere(spheres, (double)pos.getX() + 0.5, (double)pos.getY() + 0.5, (double)pos.getZ() + 0.5);
    }

    private static boolean containsAnySphere(Collection<FormationSphere> spheres, double x, double y, double z) {
        for (FormationSphere sphere : spheres) {
            if (!sphere.contains(x, y, z)) continue;
            return true;
        }
        return false;
    }

    private void applyRejuvenation(ServerLevel sl, ActiveFormationState formationState) {
        int amplifier = FormationType.REJUVENATION.rejuvenationAmplifier(formationState.flagTier);
        if (amplifier < 0) {
            return;
        }
        for (LivingEntity entity : this.livingEntitiesInFormation(sl, formationState)) {
            if (!this.shouldReceiveRejuvenation(entity)) continue;
            entity.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 16, amplifier, true, false, true));
        }
    }

    private void applyFlightBan(ServerLevel sl, ActiveFormationState formationState) {
        boolean pullAirborneHostiles = FormationType.FLIGHT_BAN.flightBanPullsAirborneHostiles(formationState.flagTier);
        int slownessAmplifier = FormationType.FLIGHT_BAN.flightBanSlownessAmplifier(formationState.flagTier);
        for (LivingEntity entity : this.livingEntitiesInFormation(sl, formationState)) {
            if (!this.shouldAffectIntruder(entity)) continue;
            this.cancelFlight(entity);
            if (pullAirborneHostiles && entity instanceof Enemy && !entity.onGround()) {
                this.pullDownAirborneHostile(entity);
            }
            if (slownessAmplifier < 0) continue;
            entity.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 16, slownessAmplifier, true, false, true));
        }
    }

    private void applyMaze(ServerLevel sl, ActiveFormationState formationState) {
        boolean nausea = FormationType.MAZE.mazeAppliesNausea(formationState.flagTier);
        int teleportDelayTicks = FormationType.MAZE.mazeTeleportDelayTicks(formationState.flagTier);
        ActiveFormationState protection = this.activeFormations.get(FormationType.SECT_PROTECTION);
        HashSet<Integer> presentIntruders = new HashSet<Integer>();
        for (LivingEntity entity : this.livingEntitiesInFormation(sl, formationState)) {
            int ticks;
            if (!this.shouldAffectIntruder(entity) || protection != null && !FormationCorePlateBlockEntity.containsAnySphere(protection.spheres, entity.getX(), entity.getY(), entity.getZ())) continue;
            presentIntruders.add(entity.getId());
            entity.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 16, 0, true, false, true));
            if (nausea) {
                entity.addEffect(new MobEffectInstance(MobEffects.CONFUSION, 16, 0, true, false, true));
            }
            if (teleportDelayTicks <= 0 || (ticks = this.mazeIntruderTicks.merge(entity.getId(), 10, Integer::sum).intValue()) < teleportDelayTicks) continue;
            this.teleportOutsideFormation(sl, entity, formationState);
            this.mazeIntruderTicks.remove(entity.getId());
        }
        this.mazeIntruderTicks.keySet().removeIf(id -> !presentIntruders.contains(id));
    }

    private void applySectProtectionShellDamage(ServerLevel sl, ActiveFormationState formationState) {
        double damagePerSecond = FormationType.SECT_PROTECTION.sectProtectionBarrierDamagePerSecond(formationState.flagTier);
        if (damagePerSecond <= 0.0) {
            return;
        }
        float damage = (float)(damagePerSecond * 10.0 / 20.0);
        if (damage <= 0.0f) {
            return;
        }
        LinkedHashSet<LivingEntity> targets = new LinkedHashSet<LivingEntity>();
        for (FormationSphere sphere : formationState.spheres) {
            for (LivingEntity entity : sl.getEntitiesOfClass(LivingEntity.class, sphere.bounds(2.0), LivingEntity::isAlive)) {
                if (!this.touchesDomeWall(entity) || !this.shouldBarrierDamage(entity)) continue;
                targets.add(entity);
            }
        }
        if (targets.isEmpty()) {
            return;
        }
        DamageSource source = FormationCorePlateBlockEntity.barrierDamageSource(sl);
        for (LivingEntity entity : targets) {
            entity.hurtMarked = false;
            entity.hurt(source, damage);
        }
    }

    private boolean touchesDomeWall(LivingEntity entity) {
        AABB box = entity.getBoundingBox().inflate(0.05);
        int minX = Mth.floor(box.minX);
        int maxX = Mth.floor(box.maxX);
        int minY = Mth.floor(box.minY);
        int maxY = Mth.floor(box.maxY);
        int minZ = Mth.floor(box.minZ);
        int maxZ = Mth.floor(box.maxZ);
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        for (int x = minX; x <= maxX; ++x) {
            for (int y = minY; y <= maxY; ++y) {
                for (int z = minZ; z <= maxZ; ++z) {
                    if (!this.isProtectedShellBlock(cursor.set(x, y, z).asLong())) continue;
                    return true;
                }
            }
        }
        return false;
    }

    private boolean shouldBarrierDamage(LivingEntity entity) {
        if (this.isFriendlyToCore(entity)) {
            return false;
        }
        if (entity instanceof Enemy) {
            return true;
        }
        return this.isSectHostileEntity(entity);
    }

    public boolean isProtectedByDome(LivingEntity entity) {
        return !this.shouldBarrierDamage(entity);
    }

    private boolean isSectHostileEntity(LivingEntity entity) {
        Level level = this.level;
        if (!(level instanceof ServerLevel)) {
            return false;
        }
        ServerLevel sl = (ServerLevel)level;
        return SectSavedData.get((ServerLevel)sl).isPlayerHostileToCore(this.getBlockPos(), (Entity)entity);
    }

    private static DamageSource barrierDamageSource(ServerLevel sl) {
        Holder.Reference<net.minecraft.world.damagesource.DamageType> magic = sl.registryAccess().registryOrThrow(Registries.DAMAGE_TYPE).getHolderOrThrow(DamageTypes.MAGIC);
        return new DamageSource((Holder)magic);
    }

    private List<LivingEntity> livingEntitiesInFormation(ServerLevel sl, ActiveFormationState formationState) {
        LinkedHashSet result = new LinkedHashSet();
        for (FormationSphere sphere : formationState.spheres) {
            AABB box = sphere.bounds(0.0);
            Vec3 center = sphere.centerVec();
            double rsq = (double)sphere.radius() * (double)sphere.radius();
            result.addAll(sl.getEntitiesOfClass(LivingEntity.class, box, entity -> entity.isAlive() && entity.distanceToSqr(center) <= rsq));
        }
        return new ArrayList<LivingEntity>(result);
    }

    private boolean isFriendlyToCore(LivingEntity entity) {
        if (this.level == null) {
            return false;
        }
        if (entity instanceof Player) {
            Player player = (Player)entity;
            return SectTokenItem.playerHasTokenForCore((Player)player, (Level)this.level, (BlockPos)this.getBlockPos());
        }
        if (entity instanceof WanderingCultivatorEntity) {
            WanderingCultivatorEntity npc = (WanderingCultivatorEntity)entity;
            return this.npcHasTokenForCore(npc);
        }
        return false;
    }

    private boolean shouldReceiveRejuvenation(LivingEntity entity) {
        if (this.isFriendlyToCore(entity)) {
            return true;
        }
        if (entity instanceof Player || entity instanceof WanderingCultivatorEntity) {
            return false;
        }
        if (entity instanceof NeutralMob) {
            Mob mob;
            return !(entity instanceof Mob) || (mob = (Mob)entity).getTarget() == null;
        }
        return !(entity instanceof Enemy);
    }

    private boolean npcHasTokenForCore(WanderingCultivatorEntity npc) {
        if (this.level == null) {
            return false;
        }
        return SectTokenItem.entityHasTokenForCore((Entity)npc, (Level)this.level, (BlockPos)this.getBlockPos());
    }

    private boolean shouldAffectIntruder(LivingEntity entity) {
        if (this.isFriendlyToCore(entity)) {
            return false;
        }
        return entity instanceof Player || entity instanceof WanderingCultivatorEntity || entity instanceof Enemy;
    }

    private void cancelFlight(LivingEntity entity) {
        entity.setNoGravity(false);
        if (entity instanceof Player) {
            Player player = (Player)entity;
            if (player.isFallFlying()) {
                player.stopFallFlying();
            }
            player.getAbilities().mayfly = false;
            if (player instanceof ServerPlayer) {
                ServerPlayer serverPlayer = (ServerPlayer)player;
                serverPlayer.onUpdateAbilities();
            }
        }
        if (!entity.onGround()) {
            Vec3 velocity = entity.getDeltaMovement();
            if (velocity.y > -0.35) {
                entity.setDeltaMovement(velocity.x * 0.8, -0.35, velocity.z * 0.8);
                entity.hasImpulse = true;
            }
        }
    }

    private void pullDownAirborneHostile(LivingEntity entity) {
        Vec3 velocity = entity.getDeltaMovement();
        double y = Math.min(velocity.y - 0.45, -0.9);
        entity.setDeltaMovement(velocity.x * 0.45, y, velocity.z * 0.45);
        entity.hasImpulse = true;
        entity.fallDistance = Math.max(entity.fallDistance, 6.0f);
    }

    private void teleportOutsideFormation(ServerLevel sl, LivingEntity entity, ActiveFormationState formationState) {
        RandomSource random = sl.random;
        FormationSphere source = FormationCorePlateBlockEntity.nearestSphere(formationState.spheres, entity.position());
        BlockPos center = source == null ? this.getBlockPos() : source.center();
        int radius = Math.max(1, source == null ? formationState.radius : source.radius());
        for (int attempt = 0; attempt < 16; ++attempt) {
            double tz;
            int z;
            int y;
            double ty;
            double angle = random.nextDouble() * Math.PI * 2.0;
            double distance = (double)radius + 3.0 + (double)random.nextInt(6);
            int x = center.getX() + (int)Math.round(Math.cos(angle) * distance);
            double tx = (double)x + 0.5;
            if (!this.isOutsideActiveFormation(tx, ty = (double)(y = sl.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z = center.getZ() + (int)Math.round(Math.sin(angle) * distance))), tz = (double)z + 0.5, formationState)) continue;
            this.performTeleport(sl, entity, tx, ty, tz);
            return;
        }
        double tx = (double)(center.getX() + radius) + 4.5;
        double tz = (double)center.getZ() + 0.5;
        int y = sl.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, (int)Math.floor(tx), (int)Math.floor(tz));
        this.performTeleport(sl, entity, tx, y, tz);
    }

    private boolean isOutsideActiveFormation(double x, double y, double z, ActiveFormationState formationState) {
        for (FormationSphere sphere : formationState.spheres) {
            Vec3 center = sphere.centerVec();
            double r = Math.max(1.0, (double)sphere.radius()) + 1.0;
            if (!(center.distanceToSqr(x, y, z) <= r * r)) continue;
            return false;
        }
        return true;
    }

    private void performTeleport(ServerLevel sl, LivingEntity entity, double x, double y, double z) {
        if (entity instanceof ServerPlayer) {
            ServerPlayer player = (ServerPlayer)entity;
            player.teleportTo(sl, x, y, z, player.getYRot(), player.getXRot());
        } else {
            entity.teleportTo(x, y, z);
        }
        entity.fallDistance = 0.0f;
        sl.sendParticles((ParticleOptions)ParticleTypes.PORTAL, x, y + 0.8, z, 24, 0.35, 0.5, 0.35, 0.05);
    }

    public void sendStatusTo(Player player) {
        MutableComponent line1 = this.customName != null && !this.customName.isEmpty() ? Component.translatable("formation.friday_cultivation.status.title_named", (Object[])new Object[]{this.customName}).withStyle(ChatFormatting.GOLD) : Component.translatable("formation.friday_cultivation.status.title").withStyle(ChatFormatting.GOLD);
        MutableComponent line2 = Component.translatable("formation.friday_cultivation.status.qi", (Object[])new Object[]{this.currentQi, this.getMaxQi()}).withStyle(ChatFormatting.AQUA);
        player.sendSystemMessage((Component)line1);
        player.sendSystemMessage((Component)line2);
        if (this.isActivated() && !this.activeFormations.isEmpty()) {
            MutableComponent formationName = this.activeFormations.size() == 1 && this.activeFormation != null ? Component.translatable(this.activeFormation.translationKey()) : Component.translatable("formation.friday_cultivation.multiple");
            MutableComponent line3 = Component.translatable("formation.friday_cultivation.status.active", (Object[])new Object[]{formationName, this.scanFlagsCount()}).withStyle(ChatFormatting.GREEN);
            player.sendSystemMessage((Component)line3);
        } else {
            player.sendSystemMessage((Component)Component.translatable("formation.friday_cultivation.status.inactive").withStyle(ChatFormatting.GRAY));
            if (this.level != null) {
                player.sendSystemMessage((Component)Component.translatable("formation.friday_cultivation.status.flags_detected", (Object[])new Object[]{this.scanFlagsCount()}).withStyle(ChatFormatting.GRAY));
            }
        }
    }

    public BlockPos origin() {
        return this.getBlockPos();
    }

    public int radius() {
        return this.activeRadius;
    }

    public boolean isActive() {
        return this.activated && !this.spiritLockSuppressed && this.currentQi > 0L;
    }

    public QiModifier modifyAt(BlockPos pos, BlockQiSpec baseSpec) {
        if (!this.isActive() || this.activeFormations.isEmpty()) {
            return QiModifier.IDENTITY;
        }
        QiModifier result = QiModifier.IDENTITY;
        for (Map.Entry<FormationType, ActiveFormationState> entry : this.activeFormations.entrySet()) {
            ActiveFormationState state = entry.getValue();
            if (!FormationCorePlateBlockEntity.containsAnySphere(state.spheres, pos)) continue;
            result = result.compose(entry.getKey().modifierForTier(state.flagTier));
        }
        return result;
    }

    protected void saveAdditional(@NotNull CompoundTag tag) {
        ListTag list;
        int i;
        super.saveAdditional(tag);
        tag.putLong("currentQi", this.currentQi);
        tag.putBoolean("activated", this.activated);
        tag.putBoolean("genArrayPlayerEdited", this.generatedArrayPlayerEdited);
        if (this.activeFormation != null) {
            tag.putString("activeFormation", this.activeFormation.id());
        }
        if (this.activeFlagTier != null) {
            tag.putString("activeFlagTier", this.activeFlagTier.id());
        }
        tag.putInt("activeRadius", this.activeRadius);
        tag.putDouble("drainAccumulator", this.drainAccumulator);
        tag.putInt("cachedSourcesInRange", this.cachedSourcesInRange);
        tag.putLong("cachedCoveredBlocksInRange", this.cachedCoveredBlocksInRange);
        this.saveActiveFormations(tag);
        if (!this.linkedFlags.isEmpty()) {
            long[] flags = new long[this.linkedFlags.size()];
            i = 0;
            for (long packed : this.linkedFlags) {
                flags[i++] = packed;
            }
            tag.putLongArray("linkedFlags", flags);
        }
        if (!this.flagEffectRadii.isEmpty()) {
            list = new ListTag();
            for (Map.Entry<Long, Integer> entry : this.flagEffectRadii.entrySet()) {
                CompoundTag item = new CompoundTag();
                item.putLong("p", entry.getKey().longValue());
                item.putInt("r", FormationCorePlateBlockEntity.clampFlagEffectRadius(entry.getValue()));
                list.add(item);
            }
            tag.put("flagEffectRadii", list);
        }
        if (!this.placedBarriers.isEmpty()) {
            long[] arr = new long[this.placedBarriers.size()];
            i = 0;
            for (long l : this.placedBarriers) {
                arr[i++] = l;
            }
            tag.putLongArray("placedBarriers", arr);
        }
        if (!this.savedShellStates.isEmpty()) {
            list = new ListTag();
            for (Map.Entry<Long, BlockState> entry : this.savedShellStates.entrySet()) {
                CompoundTag entry2 = new CompoundTag();
                entry2.putLong("p", entry.getKey().longValue());
                entry2.put("s", NbtUtils.writeBlockState((BlockState)entry.getValue()));
                list.add(entry2);
            }
            tag.put("savedShellStates", list);
        }
        if (this.customName != null && !this.customName.isEmpty()) {
            tag.putString("customName", this.customName);
        }
    }

    public void load(@NotNull CompoundTag tag) {
        FormationType t;
        int n;
        int n2;
        FormationType[] formationTypeArray;
        super.load(tag);
        this.currentQi = tag.getLong("currentQi");
        this.clampCurrentQiToMax();
        this.activated = tag.getBoolean("activated");
        this.generatedArrayPlayerEdited = tag.getBoolean("genArrayPlayerEdited");
        this.activeFormation = null;
        this.activeFlagTier = null;
        this.activeFormations.clear();
        if (tag.contains("activeFormation")) {
            String string = tag.getString("activeFormation");
            formationTypeArray = FormationType.values();
            n2 = formationTypeArray.length;
            for (n = 0; n < n2; ++n) {
                t = formationTypeArray[n];
                if (!t.id().equals(string)) continue;
                this.activeFormation = t;
                break;
            }
        }
        if (tag.contains("activeFlagTier")) {
            String string = tag.getString("activeFlagTier");
            ItemTier[] itemTierArray = ItemTier.values();
            n2 = itemTierArray.length;
            for (n = 0; n < n2; ++n) {
                ItemTier tier = itemTierArray[n];
                if (!tier.id().equals(string)) continue;
                this.activeFlagTier = tier;
                break;
            }
        }
        this.activeRadius = tag.getInt("activeRadius");
        this.drainAccumulator = tag.getDouble("drainAccumulator");
        this.cachedSourcesInRange = tag.contains("cachedSourcesInRange") ? tag.getInt("cachedSourcesInRange") : -1;
        this.cachedCoveredBlocksInRange = tag.contains("cachedCoveredBlocksInRange") ? tag.getLong("cachedCoveredBlocksInRange") : -1L;
        this.loadActiveFormations(tag);
        this.syncLegacyActiveView();
        this.linkedFlags.clear();
        if (tag.contains("linkedFlags", 12)) {
            for (long packed : tag.getLongArray("linkedFlags")) {
                this.linkedFlags.add(packed);
            }
        }
        this.flagEffectRadii.clear();
        if (tag.contains("flagEffectRadii", 9)) {
            ListTag listTag = tag.getList("flagEffectRadii", 10);
            for (int i = 0; i < listTag.size(); ++i) {
                CompoundTag item = listTag.getCompound(i);
                this.flagEffectRadii.put(item.getLong("p"), FormationCorePlateBlockEntity.clampFlagEffectRadius(item.getInt("r")));
            }
        }
        this.placedBarriers.clear();
        if (tag.contains("placedBarriers", 12)) {
            for (long l : tag.getLongArray("placedBarriers")) {
                this.placedBarriers.add(l);
            }
        }
        this.savedShellStates.clear();
        if (tag.contains("savedShellStates", 9)) {
            ListTag listTag = tag.getList("savedShellStates", 10);
            HolderLookup.RegistryLookup lookup = BuiltInRegistries.BLOCK.asLookup();
            for (int i = 0; i < listTag.size(); ++i) {
                CompoundTag entry = listTag.getCompound(i);
                long p = entry.getLong("p");
                BlockState s = NbtUtils.readBlockState((HolderGetter)lookup, (CompoundTag)entry.get("s"));
                this.savedShellStates.put(p, s);
            }
        }
        this.passThroughShellBlockCount = this.savedShellStates.isEmpty() ? 0 : -1;
        this.customName = tag.contains("customName") ? tag.getString("customName") : "";
        this.lazyRegistered = false;
        this.skipFlagValidationUntil = -1L;
        this.cachedFlagCount = -1;
        this.cachedFlagCountAt = -1L;
    }

    private void saveActiveFormations(CompoundTag tag) {
        if (this.activeFormations.isEmpty()) {
            return;
        }
        ListTag list = new ListTag();
        for (Map.Entry<FormationType, ActiveFormationState> entry : this.activeFormations.entrySet()) {
            ActiveFormationState state = entry.getValue();
            CompoundTag item = new CompoundTag();
            item.putString("type", entry.getKey().id());
            item.putString("tier", state.flagTier.id());
            item.putInt("radius", state.radius);
            item.putInt("sources", state.sourcesInRange);
            item.putLong("covered", state.coveredBlocksInRange);
            ListTag spheres = new ListTag();
            for (FormationSphere sphere : state.spheres) {
                CompoundTag sphereTag = new CompoundTag();
                sphereTag.putLong("p", sphere.center().asLong());
                sphereTag.putInt("r", sphere.radius());
                spheres.add(sphereTag);
            }
            item.put("spheres", spheres);
            list.add(item);
        }
        tag.put("activeFormations", list);
    }

    private void loadActiveFormations(CompoundTag tag) {
        this.activeFormations.clear();
        if (tag.contains("activeFormations", 9)) {
            ListTag list = tag.getList("activeFormations", 10);
            for (int i = 0; i < list.size(); ++i) {
                CompoundTag item = list.getCompound(i);
                FormationType type = FormationCorePlateBlockEntity.formationTypeFromId(item.getString("type"));
                ItemTier tier = FormationCorePlateBlockEntity.itemTierFromId(item.getString("tier"));
                if (type == null || tier == null) continue;
                List<FormationSphere> spheres = this.loadFormationSpheres(item);
                this.activeFormations.put(type, new ActiveFormationState(tier, FormationCorePlateBlockEntity.clampFlagEffectRadius(item.getInt("radius")), item.contains("sources") ? item.getInt("sources") : -1, item.contains("covered") ? item.getLong("covered") : -1L, spheres));
            }
        } else if (this.activated && this.activeFormation != null && this.activeFlagTier != null && this.activeRadius > 0) {
            this.activeFormations.put(this.activeFormation, new ActiveFormationState(this.activeFlagTier, FormationCorePlateBlockEntity.clampFlagEffectRadius(this.activeRadius), this.cachedSourcesInRange, this.cachedCoveredBlocksInRange, List.of(new FormationSphere(this.getBlockPos(), this.activeRadius))));
        }
    }

    private List<FormationSphere> loadFormationSpheres(CompoundTag item) {
        ArrayList<FormationSphere> spheres = new ArrayList<FormationSphere>();
        if (item.contains("spheres", 9)) {
            ListTag list = item.getList("spheres", 10);
            for (int i = 0; i < list.size(); ++i) {
                CompoundTag sphereTag = list.getCompound(i);
                BlockPos center = sphereTag.contains("p", 4) ? BlockPos.of(sphereTag.getLong("p")) : this.getBlockPos();
                spheres.add(new FormationSphere(center, sphereTag.getInt("r")));
            }
        }
        if (spheres.isEmpty()) {
            spheres.add(new FormationSphere(this.getBlockPos(), item.getInt("radius")));
        }
        return spheres;
    }

    @Nullable
    private static FormationType formationTypeFromId(String id) {
        for (FormationType type : FormationType.values()) {
            if (!type.id().equals(id)) continue;
            return type;
        }
        return null;
    }

    @Nullable
    private static ItemTier itemTierFromId(String id) {
        for (ItemTier tier : ItemTier.values()) {
            if (!tier.id().equals(id)) continue;
            return tier;
        }
        return null;
    }

    @NotNull
    public CompoundTag getUpdateTag() {
        CompoundTag tag = new CompoundTag();
        this.saveClientSync(tag);
        return tag;
    }

    public void handleUpdateTag(@NotNull CompoundTag tag) {
        this.loadClientSync(tag);
    }

    @Nullable
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create((BlockEntity)this);
    }

    public void onDataPacket(@NotNull Connection net, @NotNull ClientboundBlockEntityDataPacket pkt) {
        CompoundTag tag = pkt.getTag();
        if (tag != null) {
            this.loadClientSync(tag);
        }
    }

    private void saveClientSync(CompoundTag tag) {
        tag.putLong("currentQi", this.currentQi);
        tag.putBoolean("activated", this.activated);
        if (this.activeFormation != null) {
            tag.putString("activeFormation", this.activeFormation.id());
        }
        if (this.activeFlagTier != null) {
            tag.putString("activeFlagTier", this.activeFlagTier.id());
        }
        tag.putInt("activeRadius", this.activeRadius);
        tag.putInt("cachedSourcesInRange", this.cachedSourcesInRange);
        tag.putLong("cachedCoveredBlocksInRange", this.cachedCoveredBlocksInRange);
        tag.putLong("compassLockGlowUntil", this.compassLockGlowUntil);
        this.saveActiveFormations(tag);
        if (this.customName != null && !this.customName.isEmpty()) {
            tag.putString("customName", this.customName);
        }
        if (!this.linkedFlags.isEmpty()) {
            long[] flags = new long[this.linkedFlags.size()];
            int i = 0;
            for (long packed : this.linkedFlags) {
                flags[i++] = packed;
            }
            tag.putLongArray("linkedFlags", flags);
        }
    }

    private void loadClientSync(CompoundTag tag) {
        FormationType t;
        int n;
        int n2;
        FormationType[] formationTypeArray;
        String id;
        this.currentQi = tag.getLong("currentQi");
        this.clampCurrentQiToMax();
        this.activated = tag.getBoolean("activated");
        this.activeFormation = null;
        this.activeFormations.clear();
        if (tag.contains("activeFormation")) {
            id = tag.getString("activeFormation");
            formationTypeArray = FormationType.values();
            n2 = formationTypeArray.length;
            for (n = 0; n < n2; ++n) {
                t = formationTypeArray[n];
                if (!t.id().equals(id)) continue;
                this.activeFormation = t;
                break;
            }
        }
        this.activeFlagTier = null;
        if (tag.contains("activeFlagTier")) {
            id = tag.getString("activeFlagTier");
            ItemTier[] itemTierArray = ItemTier.values();
            n2 = itemTierArray.length;
            for (n = 0; n < n2; ++n) {
                ItemTier tier = itemTierArray[n];
                if (!tier.id().equals(id)) continue;
                this.activeFlagTier = tier;
                break;
            }
        }
        this.activeRadius = tag.getInt("activeRadius");
        this.cachedSourcesInRange = tag.contains("cachedSourcesInRange") ? tag.getInt("cachedSourcesInRange") : -1;
        this.cachedCoveredBlocksInRange = tag.contains("cachedCoveredBlocksInRange") ? tag.getLong("cachedCoveredBlocksInRange") : -1L;
        this.compassLockGlowUntil = tag.contains("compassLockGlowUntil") ? tag.getLong("compassLockGlowUntil") : -1L;
        this.loadActiveFormations(tag);
        this.syncLegacyActiveView();
        this.customName = tag.contains("customName") ? tag.getString("customName") : "";
        this.linkedFlags.clear();
        if (tag.contains("linkedFlags", 12)) {
            for (long packed : tag.getLongArray("linkedFlags")) {
                this.linkedFlags.add(packed);
            }
        }
    }

    private void ensureRegistered() {
        Level level;
        if (!this.activated || this.spiritLockSuppressed || this.lazyRegistered || !((level = this.level) instanceof ServerLevel)) {
            return;
        }
        ServerLevel sl = (ServerLevel)level;
        QiFieldRegistry.of((ServerLevel)sl).register((IQiFieldEffect)this);
        SectProtectionDomeHandler.registerDome(this);
        this.lazyRegistered = true;
    }

    // ===== 内嵌类型（照搬原模组内部类结构） =====

    public record FlagLinkView(BlockPos pos, FormationType type, ItemTier tier, int radius, boolean directLinked, boolean runeLinked, boolean manualLinked) {
        public FlagLinkView {
            pos = pos.immutable();
        }

        public boolean connected() {
            return this.directLinked || this.runeLinked || this.manualLinked;
        }
    }

    static final class MutableFlagLink {
        private final BlockPos pos;
        private boolean directLinked;
        private boolean runeLinked;
        private boolean manualLinked;

        private MutableFlagLink(BlockPos pos) {
            this.pos = pos.immutable();
        }
    }

    public enum LinkFlagResult {
        SUCCESS,
        ALREADY_LINKED,
        CORE_ACTIVE,
        MAX_LINKS,
        INVALID_FLAG
    }

    public enum ActivationResultKind {
        SUCCESS,
        NO_FLAGS,
        TOO_FEW_FLAGS,
        NO_QI
    }

    public record ActivationResult(ActivationResultKind kind, FormationType formationType, int flagCount, int detected, int required, int sourcesInRange) {
        static ActivationResult success(FormationType formationType, int flagCount, int sourcesInRange) {
            return new ActivationResult(ActivationResultKind.SUCCESS, formationType, flagCount, 0, 0, sourcesInRange);
        }

        static ActivationResult fail(ActivationResultKind kind) {
            return new ActivationResult(kind, null, 0, 0, 0, 0);
        }

        static ActivationResult tooFewFlags(int detected, int required) {
            return new ActivationResult(ActivationResultKind.TOO_FEW_FLAGS, null, 0, detected, required, 0);
        }
    }

    public record FormationSphere(BlockPos center, int radius) {
        public FormationSphere {
            center = center.immutable();
            radius = FormationCorePlateBlockEntity.clampFlagEffectRadius(radius);
        }

        public Vec3 centerVec() {
            return Vec3.atLowerCornerOf(this.center);
        }

        public boolean contains(double x, double y, double z) {
            Vec3 c = this.centerVec();
            double dx = x - c.x;
            double dy = y - c.y;
            double dz = z - c.z;
            return dx * dx + dy * dy + dz * dz <= (double)this.radius * (double)this.radius;
        }

        public boolean contains(BlockPos pos) {
            return this.contains(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5);
        }

        public AABB bounds(double margin) {
            Vec3 c = this.centerVec();
            double r = (double)this.radius + margin;
            return new AABB(c.x - r, c.y - r, c.z - r, c.x + r, c.y + r, c.z + r);
        }
    }

    static final class ActiveFormationState {
        private ItemTier flagTier;
        private int radius;
        private int sourcesInRange;
        private long coveredBlocksInRange;
        private List<FormationSphere> spheres;

        private ActiveFormationState(ItemTier flagTier, int radius, int sourcesInRange, long coveredBlocksInRange, List<FormationSphere> spheres) {
            this.flagTier = flagTier;
            this.radius = radius;
            this.sourcesInRange = sourcesInRange;
            this.coveredBlocksInRange = coveredBlocksInRange;
            this.spheres = spheres;
        }
    }

    record HarvestPlan(BlockState replantState, ItemStack seedStack) {
        static HarvestPlan breakOnly() {
            return new HarvestPlan(null, ItemStack.EMPTY);
        }

        static HarvestPlan replant(BlockState replantState, ItemStack seedStack) {
            return new HarvestPlan(replantState, seedStack);
        }

        boolean requiresSeed() {
            return this.replantState != null && !this.seedStack.isEmpty();
        }
    }

    static final class StorageSnapshot {
        private final IItemHandler handler;
        private final ItemStack[] stacks;

        private StorageSnapshot(IItemHandler handler) {
            this.handler = handler;
            this.stacks = new ItemStack[handler.getSlots()];
            for (int i = 0; i < this.stacks.length; ++i) {
                this.stacks[i] = handler.getStackInSlot(i).copy();
            }
        }

        private ItemStack insert(ItemStack stack) {
            ItemStack remaining = stack.copy();
            for (int i = 0; i < this.stacks.length && !remaining.isEmpty(); ++i) {
                if (!this.handler.isItemValid(i, remaining)) continue;
                ItemStack current = this.stacks[i];
                int slotLimit = Math.min(this.handler.getSlotLimit(i), remaining.getMaxStackSize());
                if (current.isEmpty()) {
                    int canPlace = Math.min(slotLimit, remaining.getCount());
                    ItemStack placed = remaining.copy();
                    placed.setCount(canPlace);
                    this.stacks[i] = placed;
                    remaining.shrink(canPlace);
                    continue;
                }
                if (!FormationCorePlateBlockEntity.sameItem(current, remaining)) continue;
                int room = Math.max(0, Math.min(slotLimit, current.getMaxStackSize()) - current.getCount());
                if (room <= 0) continue;
                int toAdd = Math.min(room, remaining.getCount());
                current.grow(toAdd);
                remaining.shrink(toAdd);
            }
            return remaining;
        }
    }
}
