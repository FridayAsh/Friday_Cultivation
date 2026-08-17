/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.BlockPos
 *  net.minecraft.core.BlockPos$MutableBlockPos
 *  net.minecraft.core.Holder
 *  net.minecraft.core.registries.Registries
 *  net.minecraft.nbt.CompoundTag
 *  net.minecraft.network.chat.Component
 *  net.minecraft.network.protocol.Packet
 *  net.minecraft.network.protocol.game.ClientboundSoundPacket
 *  net.minecraft.resources.ResourceKey
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.server.MinecraftServer
 *  net.minecraft.server.level.ServerLevel
 *  net.minecraft.server.level.ServerPlayer
 *  net.minecraft.sounds.SoundEvent
 *  net.minecraft.sounds.SoundEvents
 *  net.minecraft.sounds.SoundSource
 *  net.minecraft.util.Mth
 *  net.minecraft.util.RandomSource
 *  net.minecraft.world.damagesource.DamageSource
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.EntityType
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.entity.Mob
 *  net.minecraft.world.entity.NeutralMob
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.entity.projectile.Projectile
 *  net.minecraft.world.level.BlockGetter
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.level.block.Blocks
 *  net.minecraft.world.level.block.state.BlockState
 *  net.minecraft.world.level.levelgen.Heightmap$Types
 *  net.minecraft.world.phys.EntityHitResult
 *  net.minecraft.world.phys.HitResult
 *  net.minecraft.world.phys.Vec3
 *  net.minecraftforge.event.TickEvent$Phase
 *  net.minecraftforge.event.TickEvent$PlayerTickEvent
 *  net.minecraftforge.event.entity.ProjectileImpactEvent
 *  net.minecraftforge.event.entity.living.LivingAttackEvent
 *  net.minecraftforge.event.entity.living.LivingChangeTargetEvent
 *  net.minecraftforge.event.entity.living.LivingDeathEvent
 *  net.minecraftforge.event.entity.living.LivingHurtEvent
 *  net.minecraftforge.event.entity.player.AttackEntityEvent
 *  net.minecraftforge.event.entity.player.PlayerEvent$PlayerChangedDimensionEvent
 *  net.minecraftforge.event.entity.player.PlayerEvent$PlayerLoggedInEvent
 *  net.minecraftforge.event.entity.player.PlayerEvent$PlayerLoggedOutEvent
 *  net.minecraftforge.event.entity.player.PlayerEvent$PlayerRespawnEvent
 *  net.minecraftforge.eventbus.api.EventPriority
 *  net.minecraftforge.eventbus.api.SubscribeEvent
 *  net.minecraftforge.fml.common.Mod$EventBusSubscriber
 *  net.minecraftforge.network.PacketDistributor
 */
package com.friday.cultivation.event;

import com.friday.cultivation.cultivation.CultivationCapability;
import com.friday.cultivation.cultivation.CultivationData;
import com.friday.cultivation.cultivation.realm.Realm;
import com.friday.cultivation.cultivation.sect.SectSavedData;
import com.friday.cultivation.cultivation.spell.Spell;
import com.friday.cultivation.cultivation.technique.TechniqueLoadoutHelper;
import com.friday.cultivation.entity.npc.CorpseEntity;
import com.friday.cultivation.entity.npc.SoulReaperEntity;
import com.friday.cultivation.entity.npc.WanderingCultivatorEntity;
import com.friday.cultivation.event.CapabilityEvents;
import com.friday.cultivation.event.LooseImmortalHandler;
import com.friday.cultivation.event.SoulHookHandler;
import com.friday.cultivation.event.SoulReaperOrderHandler;
import com.friday.cultivation.event.TribulationHandler;
import com.friday.cultivation.network.DeathChoicePacket;
import com.friday.cultivation.network.ModNetwork;
import com.friday.cultivation.network.OpenDeathChoicePacket;
import com.friday.cultivation.network.SoulHookVisualPacket;
import com.friday.cultivation.network.SoulStatePacket;
import com.friday.cultivation.registry.ModDimensions;
import com.friday.cultivation.registry.ModEntities;
import com.friday.cultivation.worldgen.NaiheBridgeBuilder;
import java.util.ArrayList;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundSoundPacket;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.NeutralMob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.ProjectileImpactEvent;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingChangeTargetEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.PacketDistributor;

@Mod.EventBusSubscriber(modid="friday_cultivation")
public final class SoulStateHandler {
    public static final int SOUL_TO_REINCARNATION_TICKS = 1200;
    public static final int DEATH_TITLE_TICKS = 100;
    public static final int DEATH_COUNTDOWN_TICKS = 100;
    public static final int ESCORT_DELAY_TICKS = 200;
    public static final int REAPER_RESPAWN_TICKS = 24000;
    public static final int REAPER_WAVE_COUNT = 1;
    private static final int DIFU_TRANSFER_COUNTDOWN_TICKS = 60;
    private static final int VOLUNTARY_DIFU_TRANSFER_DELAY_TICKS = 60;
    private static final int VOLUNTARY_DIFU_PREPARE_DELAY_TICKS = 0;
    private static final int VOLUNTARY_DIFU_VISUAL_REFRESH_TICKS = 5;
    private static final int VOLUNTARY_DIFU_VISUAL_LINGER_TICKS = 18;
    private static final double VOLUNTARY_DIFU_VORTEX_HEIGHT = 2.55;
    private static final double VOLUNTARY_DIFU_VORTEX_SOUND_RANGE = 32.0;
    private static final double VOLUNTARY_DIFU_PULL_HORIZONTAL = 0.045;
    private static final int NPC_DIFU_RANDOM_MIN_RADIUS = 384;
    private static final int NPC_DIFU_RANDOM_MAX_RADIUS = 4096;
    private static final int NPC_DIFU_RANDOM_ATTEMPTS = 48;
    private static final String SOUL_PHASE_APPLIED = "xxcSoulPhaseApplied";
    private static final String SOUL_PHASE_PREV_NO_GRAVITY = "xxcSoulPhasePrevNoGravity";
    private static final String SOUL_PHASE_PREV_NO_PHYSICS = "xxcSoulPhasePrevNoPhysics";
    private static final String DEATH_CHOICE_DIMENSION = "xxcSoulDeathChoiceDimension";
    private static final String DEATH_CHOICE_X = "xxcSoulDeathChoiceX";
    private static final String DEATH_CHOICE_Y = "xxcSoulDeathChoiceY";
    private static final String DEATH_CHOICE_Z = "xxcSoulDeathChoiceZ";
    private static final String DEATH_CHOICE_Y_ROT = "xxcSoulDeathChoiceYRot";
    private static final Map<UUID, VoluntaryDifuTransfer> VOLUNTARY_DIFU_TRANSFERS = new ConcurrentHashMap<UUID, VoluntaryDifuTransfer>();
    private static final Set<UUID> VANILLA_DEATH_BYPASS = ConcurrentHashMap.newKeySet();
    private static final Map<UUID, DamageSource> PENDING_VANILLA_DAMAGE_SOURCES = new ConcurrentHashMap<UUID, DamageSource>();

    private SoulStateHandler() {
    }

    public static int upcomingReaperCount(CultivationData data) {
        return 1;
    }

    public static Realm upcomingReaperRealm(CultivationData data) {
        int kills = data == null ? 0 : data.getSoulReaperKills();
        return SoulReaperEntity.realmForKills(kills);
    }

    public static int reaperWaitMinutes(int ticks) {
        return Math.max(1, (ticks + 1200 - 1) / 1200);
    }

    public static int nextScheduledReaperTick(CultivationData data) {
        if (data == null || !data.isSoulState()) {
            return -1;
        }
        if (data.isSoulDeathChoicePending() || !data.isSoulReaperPursuitEnabled()) {
            return -1;
        }
        return data.getNextReaperTick();
    }

    public static boolean sendToDifu(ServerPlayer player) {
        MinecraftServer server = player.getServer();
        if (server == null) {
            return false;
        }
        ServerLevel difu = server.getLevel(ModDimensions.DIFU);
        if (difu == null) {
            return false;
        }
        player.removeAllEffects();
        BlockPos arrival = NaiheBridgeBuilder.place(difu, player);
        player.teleportTo(difu, (double)arrival.getX() + 0.5, (double)arrival.getY(), (double)arrival.getZ() + 0.5, player.getYRot(), player.getXRot());
        SoulStateHandler.applyDifuReincarnationEntryRules(player);
        return true;
    }

    public static BlockPos prepareDifuArrival(ServerPlayer player) {
        MinecraftServer server = player.getServer();
        if (server == null) {
            return null;
        }
        ServerLevel difu = server.getLevel(ModDimensions.DIFU);
        if (difu == null) {
            return null;
        }
        player.removeAllEffects();
        return NaiheBridgeBuilder.place(difu, player);
    }

    public static NaiheBridgeBuilder.ArrivalPreparation beginDifuArrivalPreparation(ServerPlayer player) {
        MinecraftServer server = player.getServer();
        if (server == null) {
            return null;
        }
        ServerLevel difu = server.getLevel(ModDimensions.DIFU);
        if (difu == null) {
            return null;
        }
        player.removeAllEffects();
        return NaiheBridgeBuilder.beginArrivalPreparation(difu, player);
    }

    public static BlockPos completeDifuArrivalPreparation(NaiheBridgeBuilder.ArrivalPreparation preparation) {
        if (preparation == null) {
            return null;
        }
        if (!preparation.isDone() && !preparation.finishNow()) {
            return null;
        }
        return preparation.arrival();
    }

    public static BlockPos prepareEmergencyDifuArrival(ServerPlayer player) {
        NaiheBridgeBuilder.ArrivalPreparation preparation = SoulStateHandler.beginDifuArrivalPreparation(player);
        return preparation == null ? null : preparation.prepareEmergencyArrival();
    }

    public static boolean sendToDifuAt(ServerPlayer player, BlockPos arrival) {
        if (arrival == null) {
            return false;
        }
        MinecraftServer server = player.getServer();
        if (server == null) {
            return false;
        }
        ServerLevel difu = server.getLevel(ModDimensions.DIFU);
        if (difu == null) {
            return false;
        }
        player.teleportTo(difu, (double)arrival.getX() + 0.5, (double)arrival.getY(), (double)arrival.getZ() + 0.5, player.getYRot(), player.getXRot());
        SoulStateHandler.applyDifuReincarnationEntryRules(player);
        return true;
    }

    private static void applyDifuReincarnationEntryRules(ServerPlayer player) {
        CultivationData data = CultivationCapability.get((Player)player).orElse(null);
        if (data == null || !data.isSoulState()) {
            return;
        }
        int entries = data.recordDifuReincarnationEntry();
        if (entries >= 2) {
            data.setDifuTicks(Math.max(data.getDifuTicks(), 1200));
            data.setReincarnationReady(true);
        } else if (data.getDifuTicks() < 1200) {
            data.setReincarnationReady(false);
        }
        CapabilityEvents.syncToClient(player);
    }

    public static boolean beginVoluntaryDifuTransfer(ServerPlayer player) {
        return SoulStateHandler.beginVoluntaryDifuTransfer(player, false);
    }

    private static boolean beginVoluntaryDifuTransfer(ServerPlayer player, boolean fromDeathChoice) {
        CultivationData data = CultivationCapability.get((Player)player).orElse(null);
        if (data == null || !data.isSoulState()) {
            return false;
        }
        Level level = player.level();
        if (!(level instanceof ServerLevel)) {
            return false;
        }
        ServerLevel level2 = (ServerLevel)level;
        if (level2.dimension() == ModDimensions.DIFU) {
            return false;
        }
        data.setSoulDeathChoicePending(false);
        data.setSoulReaperPursuitEnabled(false);
        data.setNextReaperTick(-1);
        if (!fromDeathChoice) {
            PENDING_VANILLA_DAMAGE_SOURCES.remove(player.getUUID());
            SoulStateHandler.clearPendingDeathLocation(player);
        }
        long now = level2.getGameTime();
        VoluntaryDifuTransfer existing = VOLUNTARY_DIFU_TRANSFERS.get(player.getUUID());
        if (existing != null && existing.dimension.equals((Object)level2.dimension())) {
            SoulStateHandler.syncVoluntaryDifuVortex(level2, player, existing, SoulStateHandler.remainingVoluntaryVisualTicks(existing, now), true);
            SoulStateHandler.sendVoluntaryDifuCountdown(player, existing, now);
            return true;
        }
        VoluntaryDifuTransfer state = VoluntaryDifuTransfer.create((ResourceKey<Level>)level2.dimension(), player, now, fromDeathChoice);
        VOLUNTARY_DIFU_TRANSFERS.put(player.getUUID(), state);
        SoulStateHandler.prepareVoluntaryDifuArrival(player, state);
        SoulStateHandler.syncVoluntaryDifuVortex(level2, player, state, 78, true);
        SoulStateHandler.playVoluntaryDifuSound(level2, player, SoundEvents.PORTAL_AMBIENT, 0.86f, 0.54f);
        player.displayClientMessage((Component)Component.translatable((String)"message.friday_cultivation.go_difu.opening"), true);
        SoulStateHandler.sendVoluntaryDifuCountdown(player, state, now);
        return true;
    }

    public static boolean sendNpcSoulToDifu(WanderingCultivatorEntity npc) {
        MinecraftServer server = npc.getServer();
        if (server == null) {
            return false;
        }
        ServerLevel difu = server.getLevel(ModDimensions.DIFU);
        if (difu == null) {
            return false;
        }
        BlockPos arrival = SoulStateHandler.prepareRandomNpcDifuArrival(difu, npc.getUUID());
        Entity moved = npc.changeDimension(difu);
        if (moved instanceof WanderingCultivatorEntity) {
            WanderingCultivatorEntity movedNpc = (WanderingCultivatorEntity)moved;
            SoulStateHandler.finishNpcSoulDifuArrival(movedNpc, arrival);
            return true;
        }
        return !npc.isRemoved() && SoulStateHandler.cloneNpcSoulToDifu(npc, difu, arrival);
    }

    private static boolean cloneNpcSoulToDifu(WanderingCultivatorEntity npc, ServerLevel difu, BlockPos arrival) {
        WanderingCultivatorEntity copy = (WanderingCultivatorEntity)((EntityType)ModEntities.WANDERING_CULTIVATOR.get()).create((Level)difu);
        if (copy == null) {
            return false;
        }
        CompoundTag tag = new CompoundTag();
        npc.saveWithoutId(tag);
        tag.remove("UUID");
        tag.remove("UUIDMost");
        tag.remove("UUIDLeast");
        tag.remove("Passengers");
        tag.remove("Leash");
        copy.load(tag);
        copy.setUUID(UUID.randomUUID());
        SoulStateHandler.finishNpcSoulDifuArrival(copy, arrival);
        if (!difu.addFreshEntity((Entity)copy)) {
            return false;
        }
        npc.stopRiding();
        npc.ejectPassengers();
        npc.discard();
        return true;
    }

    private static void finishNpcSoulDifuArrival(WanderingCultivatorEntity npc, BlockPos arrival) {
        npc.moveTo((double)arrival.getX() + 0.5, arrival.getY(), (double)arrival.getZ() + 0.5, npc.getYRot(), npc.getXRot());
        npc.setDeltaMovement(0.0, 0.0, 0.0);
        npc.fallDistance = 0.0f;
        npc.enterNpcSoulState();
    }

    public static boolean spawnReaperForNpcSoul(WanderingCultivatorEntity npc) {
        Level level = npc.level();
        if (!(level instanceof ServerLevel)) {
            return false;
        }
        ServerLevel sl = (ServerLevel)level;
        if (!npc.isNpcSoulState() || sl.dimension() == ModDimensions.DIFU) {
            return false;
        }
        if (SoulHookHandler.hasActiveTarget((Entity)npc) || SoulStateHandler.hasActiveReaper((LivingEntity)npc)) {
            return false;
        }
        SoulReaperEntity reaper = (SoulReaperEntity)((EntityType)ModEntities.SOUL_REAPER.get()).create((Level)sl);
        if (reaper == null) {
            return false;
        }
        reaper.setRealm(SoulReaperEntity.realmForKills(0));
        double angle = npc.getRandom().nextDouble() * Math.PI * 2.0;
        reaper.moveTo(npc.getX() + Math.cos(angle) * 4.0, npc.getY() + 2.5, npc.getZ() + Math.sin(angle) * 4.0, npc.getYRot(), 0.0f);
        reaper.assignTargetSoul((LivingEntity)npc);
        return sl.addFreshEntity((Entity)reaper);
    }

    private static BlockPos prepareRandomNpcDifuArrival(ServerLevel difu, UUID soulId) {
        long seed = difu.getSeed() ^ soulId.getMostSignificantBits() ^ Long.rotateLeft(soulId.getLeastSignificantBits(), 21) ^ difu.getGameTime();
        RandomSource random = RandomSource.create((long)seed);
        for (int attempt = 0; attempt < 48; ++attempt) {
            int z;
            double angle = random.nextDouble() * Math.PI * 2.0;
            int radius = 384 + random.nextInt(3713);
            int x = (int)Math.round(Math.cos(angle) * (double)radius);
            BlockPos candidate = SoulStateHandler.findSafeNpcDifuSurface(difu, x, z = (int)Math.round(Math.sin(angle) * (double)radius));
            if (candidate == null) continue;
            SoulStateHandler.clearNpcDifuArrival(difu, candidate);
            return candidate;
        }
        return SoulStateHandler.buildFallbackNpcDifuLanding(difu, random);
    }

    private static BlockPos findSafeNpcDifuSurface(ServerLevel difu, int x, int z) {
        difu.getChunk(x >> 4, z >> 4);
        int y = difu.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z);
        int minY = difu.getMinBuildHeight() + 2;
        int maxY = difu.getMaxBuildHeight() - 3;
        for (int dy = 0; dy <= 8; ++dy) {
            int checkY = Math.max(minY, Math.min(maxY, y + dy));
            BlockPos pos = new BlockPos(x, checkY, z);
            if (!SoulStateHandler.isSafeNpcDifuArrival(difu, pos)) continue;
            return pos;
        }
        return null;
    }

    private static boolean isSafeNpcDifuArrival(ServerLevel difu, BlockPos pos) {
        BlockState below = difu.getBlockState(pos.below());
        BlockState feet = difu.getBlockState(pos);
        BlockState head = difu.getBlockState(pos.above());
        return !below.isAir() && below.getFluidState().isEmpty() && feet.getFluidState().isEmpty() && head.getFluidState().isEmpty() && feet.getCollisionShape(difu, pos).isEmpty() && head.getCollisionShape(difu, pos.above()).isEmpty();
    }

    private static BlockPos buildFallbackNpcDifuLanding(ServerLevel difu, RandomSource random) {
        double angle = random.nextDouble() * Math.PI * 2.0;
        int radius = 384 + random.nextInt(3713);
        int x = (int)Math.round(Math.cos(angle) * (double)radius);
        int z = (int)Math.round(Math.sin(angle) * (double)radius);
        difu.getChunk(x >> 4, z >> 4);
        int y = Math.max(difu.getMinBuildHeight() + 4, Math.min(difu.getMaxBuildHeight() - 4, difu.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z)));
        BlockPos arrival = new BlockPos(x, y, z);
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        for (int dx = -1; dx <= 1; ++dx) {
            for (int dz = -1; dz <= 1; ++dz) {
                difu.setBlock((BlockPos)cursor.set(x + dx, y - 1, z + dz), Blocks.SOUL_SOIL.defaultBlockState(), 2);
            }
        }
        SoulStateHandler.clearNpcDifuArrival(difu, arrival);
        return arrival;
    }

    private static void clearNpcDifuArrival(ServerLevel difu, BlockPos arrival) {
        BlockState air = Blocks.AIR.defaultBlockState();
        difu.setBlock(arrival, air, 2);
        difu.setBlock(arrival.above(), air, 2);
    }

    private static void prepareVoluntaryDifuArrival(ServerPlayer player, VoluntaryDifuTransfer state) {
        if (state.difuArrivalAttempted) {
            return;
        }
        state.difuArrivalAttempted = true;
        state.difuArrivalPreparation = SoulStateHandler.beginDifuArrivalPreparation(player);
        if (state.difuArrivalPreparation == null) {
            state.difuFallbackArrival = SoulStateHandler.prepareEmergencyDifuArrival(player);
        }
    }

    private static boolean tickVoluntaryDifuTransfer(ServerPlayer player, CultivationData data, boolean inDifu) {
        boolean sent;
        ServerLevel level;
        VoluntaryDifuTransfer state;
        block17: {
            block16: {
                state = VOLUNTARY_DIFU_TRANSFERS.get(player.getUUID());
                if (state == null) {
                    return false;
                }
                Level level2 = player.level();
                if (!(level2 instanceof ServerLevel)) break block16;
                level = (ServerLevel)level2;
                if (!inDifu && data.isSoulState() && state.dimension.equals((Object)level.dimension())) break block17;
            }
            VOLUNTARY_DIFU_TRANSFERS.remove(player.getUUID());
            Level level3 = player.level();
            if (level3 instanceof ServerLevel) {
                ServerLevel currentLevel = (ServerLevel)level3;
                SoulStateHandler.syncVoluntaryDifuVortex(currentLevel, player, state, 1, false);
            }
            if (state.fromDeathChoice && data.isSoulState() && !inDifu) {
                data.setSoulDeathChoicePending(true);
                data.setSoulReaperPursuitEnabled(false);
                data.setNextReaperTick(-1);
                CapabilityEvents.syncToClient(player);
                SoulStateHandler.openDeathChoice(player);
            }
            return false;
        }
        long now = level.getGameTime();
        SoulStateHandler.sendVoluntaryDifuCountdown(player, state, now);
        SoulStateHandler.pullTowardVoluntaryDifuVortex(player, state, now);
        if (now - state.lastVisualTick >= 5L) {
            state.lastVisualTick = now;
            SoulStateHandler.syncVoluntaryDifuVortex(level, player, state, SoulStateHandler.remainingVoluntaryVisualTicks(state, now), true);
        }
        if (!state.difuArrivalAttempted && now - state.startTick >= 0L) {
            SoulStateHandler.prepareVoluntaryDifuArrival(player, state);
        }
        if (state.difuArrivalPreparation != null && state.difuArrival == null && state.difuArrivalPreparation.tick()) {
            state.difuArrival = state.difuArrivalPreparation.arrival();
            state.difuArrivalPreparation = null;
        }
        if (now < state.transferTick || !state.difuArrivalAttempted) {
            return true;
        }
        VOLUNTARY_DIFU_TRANSFERS.remove(player.getUUID(), state);
        player.displayClientMessage((Component)Component.translatable((String)"message.friday_cultivation.go_difu.teleporting"), true);
        BlockPos arrival = state.difuArrival;
        if (arrival == null && state.difuArrivalPreparation != null && (arrival = SoulStateHandler.completeDifuArrivalPreparation(state.difuArrivalPreparation)) != null) {
            state.difuArrival = arrival;
            state.difuArrivalPreparation = null;
        }
        if (arrival == null) {
            arrival = state.difuFallbackArrival;
        }
        if (arrival == null) {
            arrival = SoulStateHandler.prepareEmergencyDifuArrival(player);
        }
        boolean bl = sent = arrival != null && SoulStateHandler.sendToDifuAt(player, arrival);
        if (!sent) {
            SoulStateHandler.syncVoluntaryDifuVortex(level, player, state, 1, false);
            if (state.fromDeathChoice) {
                data.setSoulDeathChoicePending(true);
                data.setSoulReaperPursuitEnabled(false);
                data.setNextReaperTick(-1);
                CapabilityEvents.syncToClient(player);
                SoulStateHandler.openDeathChoice(player);
            }
            player.displayClientMessage((Component)Component.translatable((String)"message.friday_cultivation.go_difu.failed"), true);
            return true;
        }
        if (state.fromDeathChoice) {
            SoulStateHandler.spawnCorpseAtPendingDeathLocation(player);
            PENDING_VANILLA_DAMAGE_SOURCES.remove(player.getUUID());
        }
        return true;
    }

    private static int remainingVoluntaryVisualTicks(VoluntaryDifuTransfer state, long now) {
        long remaining = Math.max(1L, state.transferTick - now + 18L);
        return (int)Math.min(Integer.MAX_VALUE, remaining);
    }

    private static void sendVoluntaryDifuCountdown(ServerPlayer player, VoluntaryDifuTransfer state, long now) {
        int remainingTicks = Math.max(0, (int)(state.transferTick - now));
        int seconds = (remainingTicks + 19) / 20;
        if (seconds == state.lastCountdownSecond) {
            return;
        }
        state.lastCountdownSecond = seconds;
        if (seconds <= 0) {
            player.displayClientMessage((Component)Component.translatable((String)"message.friday_cultivation.go_difu.teleporting"), true);
        } else {
            player.displayClientMessage((Component)Component.translatable((String)"message.friday_cultivation.go_difu.countdown", (Object[])new Object[]{seconds}), true);
        }
    }

    private static void pullTowardVoluntaryDifuVortex(ServerPlayer player, VoluntaryDifuTransfer state, long now) {
        double total = Math.max(1.0, (double)(state.transferTick - state.startTick));
        double progress = Mth.clamp((double)((double)(now - state.startTick) / total), (double)0.0, (double)1.0);
        Vec3 delta = new Vec3(state.vortexX - player.getX(), 0.0, state.vortexZ - player.getZ());
        double horizontal = 0.045 * (0.55 + progress * 0.45);
        Vec3 current = player.getDeltaMovement();
        player.setDeltaMovement(current.x * 0.35 + delta.x * horizontal, 0.0, current.z * 0.35 + delta.z * horizontal);
        player.hurtMarked = true;
        player.fallDistance = 0.0f;
    }

    private static void syncVoluntaryDifuVortex(ServerLevel level, ServerPlayer player, VoluntaryDifuTransfer state, int durationTicks, boolean active) {
        SoulHookVisualPacket packet = new SoulHookVisualPacket(player.getId(), player.getId(), Math.max(1, durationTicks), 1, true, active, 200, true, state.vortexX, state.vortexY, state.vortexZ);
        double radiusSq = 1024.0;
        boolean sentToTarget = false;
        Vec3 center = new Vec3(state.vortexX, state.vortexY, state.vortexZ);
        for (ServerPlayer viewer : level.players()) {
            if (viewer.distanceToSqr(center.x, center.y, center.z) > radiusSq || active && !SoulStateHandler.canPerceiveSoulSystem(viewer)) continue;
            ModNetwork.CHANNEL.send(PacketDistributor.PLAYER.with(() -> viewer), (Object)packet);
            if (viewer != player) continue;
            sentToTarget = true;
        }
        if (active && !sentToTarget) {
            ModNetwork.CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), (Object)packet);
        }
    }

    private static void playVoluntaryDifuSound(ServerLevel level, ServerPlayer target, SoundEvent sound, float volume, float pitch) {
        ClientboundSoundPacket packet = new ClientboundSoundPacket(Holder.direct(sound), SoundSource.PLAYERS, target.getX(), target.getY(), target.getZ(), volume, pitch, level.random.nextLong());
        double rangeSq = 1024.0;
        boolean sentToTarget = false;
        for (ServerPlayer viewer : level.players()) {
            if (!SoulStateHandler.canPerceiveSoulSystem(viewer) || viewer.distanceToSqr((Entity)target) > rangeSq) continue;
            viewer.connection.send((Packet)packet);
            if (viewer != target) continue;
            sentToTarget = true;
        }
        if (!sentToTarget) {
            target.connection.send((Packet)packet);
        }
    }

    private static void spawnReaperFor(ServerPlayer player) {
        Level level = player.level();
        if (!(level instanceof ServerLevel)) {
            return;
        }
        ServerLevel sl = (ServerLevel)level;
        CultivationData data = CultivationCapability.get((Player)player).orElse(null);
        Realm realm = SoulStateHandler.upcomingReaperRealm(data);
        int count = SoulStateHandler.upcomingReaperCount(data);
        boolean spawned = false;
        double baseAngle = player.getRandom().nextDouble() * Math.PI * 2.0;
        for (int i = 0; i < count; ++i) {
            SoulReaperEntity reaper = (SoulReaperEntity)((EntityType)ModEntities.SOUL_REAPER.get()).create((Level)sl);
            if (reaper == null) continue;
            reaper.setRealm(realm);
            double angle = baseAngle + Math.PI * 2 * (double)i / (double)Math.max(1, count);
            reaper.moveTo(player.getX() + Math.cos(angle) * 4.0, player.getY() + 2.5, player.getZ() + Math.sin(angle) * 4.0, player.getYRot(), 0.0f);
            reaper.assignTargetSoul((LivingEntity)player);
            spawned |= sl.addFreshEntity((Entity)reaper);
        }
        if (spawned) {
            player.sendSystemMessage((Component)Component.translatable((String)"message.friday_cultivation.soul_reaper.arrives", (Object[])new Object[]{count, realm.displayName()}));
        }
    }

    private static boolean hasActiveReaper(ServerPlayer player) {
        return SoulStateHandler.hasActiveReaper((LivingEntity)player);
    }

    private static boolean hasActiveReaper(LivingEntity target) {
        Level level = target.level();
        if (!(level instanceof ServerLevel)) {
            return false;
        }
        ServerLevel sl = (ServerLevel)level;
        for (SoulReaperEntity r : sl.getEntitiesOfClass(SoulReaperEntity.class, target.getBoundingBox().inflate(160.0))) {
            if (!r.isAlive() || !r.isAssignedTo((Entity)target)) continue;
            return true;
        }
        return false;
    }

    private static void spawnCorpse(ServerPlayer player) {
        Level level = player.level();
        if (!(level instanceof ServerLevel)) {
            return;
        }
        ServerLevel sl = (ServerLevel)level;
        SoulStateHandler.spawnCorpseAt(player, sl, player.getX(), player.getY(), player.getZ(), player.getYRot());
    }

    private static void spawnCorpseAt(ServerPlayer player, ServerLevel sl, double x, double y, double z, float yRot) {
        CorpseEntity corpse = (CorpseEntity)((EntityType)ModEntities.CORPSE.get()).create((Level)sl);
        if (corpse == null) {
            return;
        }
        corpse.moveTo(x, y, z, yRot, 0.0f);
        corpse.setupCorpse(player);
        corpse.settleOnGround();
        sl.addFreshEntity((Entity)corpse);
    }

    private static void rememberPendingDeathLocation(ServerPlayer player) {
        CompoundTag tag = player.getPersistentData();
        tag.putString(DEATH_CHOICE_DIMENSION, player.level().dimension().location().toString());
        tag.putDouble(DEATH_CHOICE_X, player.getX());
        tag.putDouble(DEATH_CHOICE_Y, player.getY());
        tag.putDouble(DEATH_CHOICE_Z, player.getZ());
        tag.putFloat(DEATH_CHOICE_Y_ROT, player.getYRot());
    }

    private static PendingDeathLocation pendingDeathLocation(ServerPlayer player) {
        ResourceLocation location;
        CompoundTag tag = player.getPersistentData();
        ResourceKey dimension = player.level().dimension();
        if (tag.contains(DEATH_CHOICE_DIMENSION, 8) && (location = ResourceLocation.tryParse((String)tag.getString(DEATH_CHOICE_DIMENSION))) != null) {
            dimension = ResourceKey.create((ResourceKey)Registries.DIMENSION, (ResourceLocation)location);
        }
        double x = tag.contains(DEATH_CHOICE_X, 6) ? tag.getDouble(DEATH_CHOICE_X) : player.getX();
        double y = tag.contains(DEATH_CHOICE_Y, 6) ? tag.getDouble(DEATH_CHOICE_Y) : player.getY();
        double z = tag.contains(DEATH_CHOICE_Z, 6) ? tag.getDouble(DEATH_CHOICE_Z) : player.getZ();
        float yRot = tag.contains(DEATH_CHOICE_Y_ROT, 5) ? tag.getFloat(DEATH_CHOICE_Y_ROT) : player.getYRot();
        return new PendingDeathLocation((ResourceKey<Level>)dimension, x, y, z, yRot);
    }

    private static void spawnCorpseAtPendingDeathLocation(ServerPlayer player) {
        Level level;
        ServerLevel deathLevel;
        PendingDeathLocation death = SoulStateHandler.pendingDeathLocation(player);
        MinecraftServer server = player.getServer();
        ServerLevel serverLevel = deathLevel = server == null ? null : server.getLevel(death.dimension());
        if (deathLevel == null && (level = player.level()) instanceof ServerLevel) {
            ServerLevel fallback;
            deathLevel = fallback = (ServerLevel)level;
        }
        if (deathLevel != null) {
            SoulStateHandler.spawnCorpseAt(player, deathLevel, death.x(), death.y(), death.z(), death.yRot());
        }
        SoulStateHandler.clearPendingDeathLocation(player);
    }

    private static void clearPendingDeathLocation(ServerPlayer player) {
        CompoundTag tag = player.getPersistentData();
        tag.remove(DEATH_CHOICE_DIMENSION);
        tag.remove(DEATH_CHOICE_X);
        tag.remove(DEATH_CHOICE_Y);
        tag.remove(DEATH_CHOICE_Z);
        tag.remove(DEATH_CHOICE_Y_ROT);
    }

    private static void openDeathChoice(ServerPlayer player) {
        ModNetwork.CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), (Object)new OpenDeathChoicePacket(player.getCombatTracker().getDeathMessage()));
    }

    public static void resolveDeathChoice(ServerPlayer player, DeathChoicePacket.Choice choice) {
        if (player == null || choice == null) {
            return;
        }
        CultivationData data = CultivationCapability.get((Player)player).orElse(null);
        if (data == null || !data.isSoulState() || !data.isSoulDeathChoicePending()) {
            player.displayClientMessage((Component)Component.translatable((String)"message.friday_cultivation.death_choice.invalid"), true);
            return;
        }
        switch (choice) {
            case VANILLA_DEATH: {
                SoulStateHandler.chooseVanillaDeath(player, data);
                break;
            }
            case GO_DIFU: {
                SoulStateHandler.chooseDifuDeath(player, data);
                break;
            }
            case WANDERING_SOUL: {
                SoulStateHandler.chooseWanderingSoul(player, data);
            }
        }
    }

    private static void clearDeathChoiceState(ServerPlayer player, CultivationData data, boolean clearPursuit) {
        data.setSoulDeathChoicePending(false);
        if (clearPursuit) {
            data.setSoulReaperPursuitEnabled(false);
            data.setNextReaperTick(-1);
        }
        PENDING_VANILLA_DAMAGE_SOURCES.remove(player.getUUID());
    }

    private static void chooseVanillaDeath(ServerPlayer player, CultivationData data) {
        UUID id = player.getUUID();
        DamageSource source = PENDING_VANILLA_DAMAGE_SOURCES.remove(id);
        if (source == null) {
            source = player.damageSources().genericKill();
        }
        SoulStateHandler.clearDeathChoiceState(player, data, true);
        data.setReincarnationPending(false);
        data.setReincarnationReady(false);
        data.setDifuTicks(0);
        data.setSoulTicks(0);
        data.setSoulState(false);
        SoulStateHandler.clearPendingDeathLocation(player);
        SoulStateHandler.restoreSoulPhaseMovement(player);
        CapabilityEvents.syncToClient(player);
        SoulStateHandler.broadcastSouls(player.getServer());
        VANILLA_DEATH_BYPASS.add(id);
        player.invulnerableTime = 0;
        player.setHealth(Math.max(1.0f, player.getHealth()));
        // 用足够大的有限伤害击杀（避免 Float.MAX_VALUE 溢出为 NaN）
        player.hurt(source, Math.max(1.0f, player.getMaxHealth()) * 100.0f);
        if (player.isAlive() && !player.isDeadOrDying()) {
            VANILLA_DEATH_BYPASS.remove(id);
        }
    }

    private static void chooseDifuDeath(ServerPlayer player, CultivationData data) {
        boolean started = SoulStateHandler.beginVoluntaryDifuTransfer(player, true);
        if (!started) {
            data.setSoulDeathChoicePending(true);
            data.setSoulReaperPursuitEnabled(false);
            data.setNextReaperTick(-1);
            CapabilityEvents.syncToClient(player);
            player.displayClientMessage((Component)Component.translatable((String)"message.friday_cultivation.go_difu.failed"), true);
            SoulStateHandler.openDeathChoice(player);
        }
    }

    private static void chooseWanderingSoul(ServerPlayer player, CultivationData data) {
        SoulStateHandler.clearDeathChoiceState(player, data, false);
        data.setSoulReaperPursuitEnabled(true);
        data.setNextReaperTick(data.getSoulTicks() + 24000);
        SoulStateHandler.spawnCorpseAtPendingDeathLocation(player);
        CapabilityEvents.syncToClient(player);
        SoulReaperOrderHandler.notifyDeath(player);
        int minutes = SoulStateHandler.reaperWaitMinutes(24000);
        int count = SoulStateHandler.upcomingReaperCount(data);
        Realm realm = SoulStateHandler.upcomingReaperRealm(data);
        player.sendSystemMessage((Component)Component.translatable((String)"message.friday_cultivation.death_choice.wandering_confirmed", (Object[])new Object[]{minutes, count, realm.displayName()}));
    }

    public static void enterSoulState(ServerPlayer player) {
        SoulStateHandler.enterSoulState(player, true, true);
    }

    public static void enterSoulState(ServerPlayer player, boolean showDeathSequence, boolean notifyDeath) {
        CultivationData data = CultivationCapability.get((Player)player).orElse(null);
        if (data == null || data.isSoulState()) {
            return;
        }
        SoulStateHandler.enterSoulState(player, data, showDeathSequence, notifyDeath);
    }

    private static void enterSoulState(ServerPlayer player, CultivationData data) {
        SoulStateHandler.enterSoulState(player, data, true, true);
    }

    private static void enterSoulState(ServerPlayer player, CultivationData data, boolean showDeathSequence, boolean notifyDeath) {
        boolean requiresDeathChoice = showDeathSequence && player.level().dimension() != ModDimensions.DIFU;
        player.stopRiding();
        data.setMeditating(false);
        data.setSoulState(true);
        TechniqueLoadoutHelper.NormalizationResult techniqueResult = TechniqueLoadoutHelper.normalizeForCurrentState(data, player.getRandom());
        TechniqueLoadoutHelper.notifyNormalization(player, data, techniqueResult);
        data.setSoulTicks(0);
        data.setDifuTicks(0);
        data.setReincarnationReady(false);
        data.setSoulReaperKills(0);
        data.setNextReaperTick(-1);
        data.setSoulDeathChoicePending(requiresDeathChoice);
        data.setSoulReaperPursuitEnabled(false);
        if (requiresDeathChoice) {
            SoulStateHandler.rememberPendingDeathLocation(player);
        }
        if (!requiresDeathChoice) {
            SoulStateHandler.clearPendingDeathLocation(player);
            SoulStateHandler.spawnCorpse(player);
        }
        player.setHealth(player.getMaxHealth());
        player.getFoodData().setFoodLevel(20);
        player.setAirSupply(player.getMaxAirSupply());
        player.removeAllEffects();
        SoulStateHandler.clearDeathHostility(player);
        player.sendSystemMessage((Component)Component.translatable((String)"message.friday_cultivation.soul.entered"));
        CapabilityEvents.syncToClient(player);
        if (requiresDeathChoice) {
            SoulStateHandler.openDeathChoice(player);
        }
        SoulStateHandler.broadcastSouls(player.getServer());
        if (notifyDeath && !requiresDeathChoice) {
            SoulReaperOrderHandler.notifyDeath(player);
        }
    }

    public static void clearDeathHostility(ServerPlayer player) {
        if (player == null) {
            return;
        }
        MinecraftServer server = player.getServer();
        if (server != null) {
            UUID playerId = player.getUUID();
            for (ServerLevel level : server.getAllLevels()) {
                SectSavedData.get(level).clearPlayerHostility(playerId);
                for (Entity entity : level.getAllEntities()) {
                    if (!(entity instanceof WanderingCultivatorEntity)) continue;
                    WanderingCultivatorEntity npc = (WanderingCultivatorEntity)entity;
                    npc.clearHostilityTowardPlayer(playerId);
                }
            }
        }
        SoulStateHandler.clearHostileAggro(player);
    }

    private static void clearHostileAggro(ServerPlayer player) {
        Level level = player.level();
        if (!(level instanceof ServerLevel)) {
            return;
        }
        ServerLevel sl = (ServerLevel)level;
        for (Mob mob : sl.getEntitiesOfClass(Mob.class, player.getBoundingBox().inflate(64.0), m -> m.getTarget() == player)) {
            mob.setTarget(null);
            mob.setLastHurtByMob(null);
            if (!(mob instanceof NeutralMob)) continue;
            NeutralMob nm = (NeutralMob)mob;
            nm.stopBeingAngry();
        }
    }

    @SubscribeEvent(priority=EventPriority.LOWEST)
    public static void onLivingDeath(LivingDeathEvent event) {
        if (event.isCanceled()) {
            return;
        }
        LivingEntity livingEntity = event.getEntity();
        if (!(livingEntity instanceof ServerPlayer)) {
            return;
        }
        ServerPlayer player = (ServerPlayer)livingEntity;
        if (VANILLA_DEATH_BYPASS.remove(player.getUUID())) {
            PENDING_VANILLA_DAMAGE_SOURCES.remove(player.getUUID());
            return;
        }
        CultivationData data = CultivationCapability.get((Player)player).orElse(null);
        if (data == null) {
            return;
        }
        if (LooseImmortalHandler.tryOpenTribulationDeathChoice(event, player, data)) {
            return;
        }
        boolean wasInTribulation = data.isInTribulation();
        event.setCanceled(true);
        player.setHealth(player.getMaxHealth());
        if (!data.isSoulState()) {
            boolean tribulationFailureApplied = false;
            if (wasInTribulation) {
                tribulationFailureApplied = TribulationHandler.failCurrentTribulation(player, data);
            }
            if (!tribulationFailureApplied) {
                data.demoteOnFailure();
            }
            PENDING_VANILLA_DAMAGE_SOURCES.put(player.getUUID(), event.getSource());
            SoulStateHandler.enterSoulState(player, data);
        }
    }

    @SubscribeEvent(priority=EventPriority.HIGHEST)
    public static void onLivingHurt(LivingHurtEvent event) {
        if (SoulStateHandler.shouldBlockOrdinaryDamage(event.getSource(), event.getEntity())) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent(priority=EventPriority.HIGHEST)
    public static void onLivingAttack(LivingAttackEvent event) {
        if (SoulStateHandler.shouldBlockOrdinaryDamage(event.getSource(), event.getEntity())) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent(priority=EventPriority.HIGHEST)
    public static void onAttackEntity(AttackEntityEvent event) {
        if (!SoulStateHandler.canOrdinaryAffect((Entity)event.getEntity(), event.getTarget())) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent(priority=EventPriority.HIGHEST)
    public static void onProjectileImpact(ProjectileImpactEvent event) {
        HitResult hitResult = event.getRayTraceResult();
        if (!(hitResult instanceof EntityHitResult)) {
            return;
        }
        EntityHitResult hit = (EntityHitResult)hitResult;
        Entity entity = hit.getEntity();
        if (!(entity instanceof LivingEntity)) {
            return;
        }
        LivingEntity target = (LivingEntity)entity;
        Projectile projectile = event.getProjectile();
        Entity owner = projectile.getOwner();
        if (!SoulStateHandler.canOrdinaryAffect(owner, (Entity)target)) {
            event.setImpactResult(ProjectileImpactEvent.ImpactResult.STOP_AT_CURRENT);
            projectile.discard();
        }
    }

    @SubscribeEvent
    public static void onChangeTarget(LivingChangeTargetEvent event) {
        if (event != null) {
            WanderingCultivatorEntity npc;
            LivingEntity actor = event.getEntity();
            LivingEntity target = event.getNewTarget();
            if (!(!SoulStateHandler.isSoulPlayer((Entity)target) && !SoulStateHandler.isNpcSoul((Entity)target) || actor instanceof WanderingCultivatorEntity && (npc = (WanderingCultivatorEntity)actor).isDifuReaper())) {
                event.setNewTarget(null);
                return;
            }
            if (target instanceof SoulReaperEntity && !SoulStateHandler.isSoulPlayer((Entity)actor)) {
                event.setNewTarget(null);
                return;
            }
            if (SoulStateHandler.isNpcSoul((Entity)actor) || actor instanceof SoulReaperEntity) {
                event.setNewTarget(null);
            }
            return;
        }
        if (SoulStateHandler.isSoulPlayer((Entity)event.getNewTarget()) || SoulStateHandler.isNpcSoul((Entity)event.getNewTarget()) || event.getNewTarget() instanceof SoulReaperEntity) {
            event.setNewTarget(null);
            return;
        }
        if (SoulStateHandler.isNpcSoul((Entity)event.getEntity()) || event.getEntity() instanceof SoulReaperEntity) {
            event.setNewTarget(null);
        }
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }
        Player player = event.player;
        if (!(player instanceof ServerPlayer)) {
            return;
        }
        ServerPlayer player2 = (ServerPlayer)player;
        CultivationData data = CultivationCapability.get((Player)player2).orElse(null);
        if (data == null) {
            SoulStateHandler.restoreSoulPhaseMovement(player2);
            return;
        }
        if (data.isLooseImmortal()) {
            SoulStateHandler.handleLooseImmortalPhaseMovement(player2);
            if (player2.tickCount % 20 == 0) {
                CapabilityEvents.syncToClient(player2);
            }
            return;
        }
        if (!data.isSoulState()) {
            SoulStateHandler.restoreSoulPhaseMovement(player2);
            return;
        }
        if (player2.getHealth() < player2.getMaxHealth()) {
            player2.setHealth(player2.getMaxHealth());
        }
        boolean inDifu = player2.level().dimension() == ModDimensions.DIFU;
        SoulStateHandler.handleSoulPhaseMovement(player2, inDifu, data);
        if (player2.tickCount % 20 == 0) {
            SoulStateHandler.clearHostileAggro(player2);
            CapabilityEvents.syncToClient(player2);
        }
        data.incrementSoulTicks();
        if (inDifu) {
            data.incrementDifuTicks();
        }
        if (!inDifu && SoulStateHandler.tickVoluntaryDifuTransfer(player2, data, inDifu)) {
            return;
        }
        if (!inDifu && data.isSoulDeathChoicePending()) {
            if (player2.tickCount % 40 == 0) {
                SoulStateHandler.openDeathChoice(player2);
            }
            return;
        }
        if (!inDifu && data.isSoulReaperPursuitEnabled() && data.getNextReaperTick() >= 0 && data.getSoulTicks() >= data.getNextReaperTick() && !SoulStateHandler.hasActiveReaper(player2)) {
            SoulStateHandler.spawnReaperFor(player2);
            data.setNextReaperTick(-1);
            CapabilityEvents.syncToClient(player2);
        }
        if (inDifu && data.getDifuTicks() >= 1200 && !data.isReincarnationReady()) {
            data.setReincarnationReady(true);
            player2.sendSystemMessage((Component)Component.translatable((String)"message.friday_cultivation.reincarnation.ready"));
            CapabilityEvents.syncToClient(player2);
        }
    }

    private static void handleSoulPhaseMovement(ServerPlayer player, boolean inDifu, CultivationData data) {
        if (player.isCreative() || player.isSpectator()) {
            SoulStateHandler.restoreSoulPhaseMovement(player);
            return;
        }
        if (inDifu || !data.isSpellEnabled(Spell.GHOST_FLIGHT) || !player.getAbilities().flying) {
            SoulStateHandler.restoreSoulPhaseMovement(player);
            return;
        }
        SoulStateHandler.applySoulPhaseMovement(player);
    }

    private static void handleLooseImmortalPhaseMovement(ServerPlayer player) {
        SoulStateHandler.restoreSoulPhaseMovement(player);
    }

    private static void applySoulPhaseMovement(ServerPlayer player) {
        CompoundTag tag = player.getPersistentData();
        if (!tag.getBoolean(SOUL_PHASE_APPLIED)) {
            tag.putBoolean(SOUL_PHASE_APPLIED, true);
            tag.putBoolean(SOUL_PHASE_PREV_NO_GRAVITY, player.isNoGravity());
            tag.putBoolean(SOUL_PHASE_PREV_NO_PHYSICS, player.noPhysics);
        }
        player.noPhysics = true;
        player.setNoGravity(true);
        player.fallDistance = 0.0f;
    }

    private static void restoreSoulPhaseMovement(ServerPlayer player) {
        CompoundTag tag = player.getPersistentData();
        if (!tag.getBoolean(SOUL_PHASE_APPLIED)) {
            return;
        }
        boolean prevNoGravity = tag.getBoolean(SOUL_PHASE_PREV_NO_GRAVITY);
        boolean prevNoPhysics = tag.getBoolean(SOUL_PHASE_PREV_NO_PHYSICS);
        SoulStateHandler.clearSoulPhaseSnapshot(tag);
        player.fallDistance = 0.0f;
        if (player.isSpectator()) {
            player.noPhysics = true;
            return;
        }
        player.noPhysics = prevNoPhysics;
        player.setNoGravity(prevNoGravity);
    }

    private static void clearSoulPhaseSnapshot(CompoundTag tag) {
        tag.remove(SOUL_PHASE_APPLIED);
        tag.remove(SOUL_PHASE_PREV_NO_GRAVITY);
        tag.remove(SOUL_PHASE_PREV_NO_PHYSICS);
    }

    @SubscribeEvent
    public static void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        Player player = event.getEntity();
        if (player instanceof ServerPlayer) {
            ServerPlayer player2 = (ServerPlayer)player;
            CultivationData data = CultivationCapability.get((Player)player2).orElse(null);
            if (data != null) {
                TechniqueLoadoutHelper.NormalizationResult result = TechniqueLoadoutHelper.normalizeForCurrentState(data, player2.getRandom());
                if (result.changed()) {
                    TechniqueLoadoutHelper.notifyNormalization(player2, data, result);
                    CapabilityEvents.syncToClient(player2);
                }
                if (data.isSoulState() && data.isSoulDeathChoicePending() && player2.level().dimension() != ModDimensions.DIFU) {
                    SoulStateHandler.openDeathChoice(player2);
                }
            }
            SoulStateHandler.broadcastSouls(player2.getServer());
        }
    }

    @SubscribeEvent
    public static void onPlayerLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        Player player = event.getEntity();
        if (player instanceof ServerPlayer) {
            ServerPlayer player2 = (ServerPlayer)player;
            VoluntaryDifuTransfer transfer = VOLUNTARY_DIFU_TRANSFERS.remove(player2.getUUID());
            VANILLA_DEATH_BYPASS.remove(player2.getUUID());
            CultivationData data = CultivationCapability.get((Player)player2).orElse(null);
            if (transfer != null && transfer.fromDeathChoice && data != null && data.isSoulState() && player2.level().dimension() != ModDimensions.DIFU) {
                data.setSoulDeathChoicePending(true);
                data.setSoulReaperPursuitEnabled(false);
                data.setNextReaperTick(-1);
            }
            if (data == null || !data.isSoulState() || !data.isSoulDeathChoicePending()) {
                PENDING_VANILLA_DAMAGE_SOURCES.remove(player2.getUUID());
                SoulStateHandler.clearPendingDeathLocation(player2);
            }
        }
    }

    @SubscribeEvent
    public static void onPlayerRespawn(PlayerEvent.PlayerRespawnEvent event) {
        Player player = event.getEntity();
        if (player instanceof ServerPlayer) {
            ServerPlayer player2 = (ServerPlayer)player;
            SoulStateHandler.clearDeathHostility(player2);
        }
    }

    @SubscribeEvent
    public static void onPlayerChangedDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        Player player = event.getEntity();
        if (player instanceof ServerPlayer) {
            ServerPlayer player2 = (ServerPlayer)player;
            VOLUNTARY_DIFU_TRANSFERS.remove(player2.getUUID());
            SoulStateHandler.broadcastSouls(player2.getServer());
        }
    }

    public static boolean isSoulPlayer(Entity entity) {
        if (!(entity instanceof ServerPlayer)) {
            return false;
        }
        ServerPlayer player = (ServerPlayer)entity;
        CultivationData data = CultivationCapability.get((Player)player).orElse(null);
        return data != null && data.isSoulState();
    }

    public static boolean isNpcSoul(Entity entity) {
        WanderingCultivatorEntity npc;
        return entity instanceof WanderingCultivatorEntity && (npc = (WanderingCultivatorEntity)entity).isNpcSoulState();
    }

    public static boolean isSoulRealmEntity(Entity entity) {
        return SoulStateHandler.isSoulPlayer(entity) || SoulStateHandler.isNpcSoul(entity);
    }

    private static boolean isSeparatedActor(Entity entity) {
        WanderingCultivatorEntity npc;
        return SoulStateHandler.isSoulRealmEntity(entity) || entity instanceof SoulReaperEntity || entity instanceof WanderingCultivatorEntity && (npc = (WanderingCultivatorEntity)entity).isDifuReaper();
    }

    public static boolean canSoulHookTarget(Entity target) {
        return SoulStateHandler.isSoulPlayer(target) || SoulStateHandler.isNpcSoul(target);
    }

    public static boolean canPerceiveSoulSystem(ServerPlayer player) {
        return CultivationCapability.get((Player)player).map(data -> data.isSpellEnabled(Spell.YIN_YANG_EYE)).orElse(false);
    }

    public static boolean canOrdinaryAffect(Entity actor, Entity target) {
        if (target == null) {
            return true;
        }
        if (target instanceof SoulReaperEntity) {
            return SoulStateHandler.isSoulPlayer(actor);
        }
        if (SoulStateHandler.isDifuSoulPlayerActor(actor) && target.level().dimension() == ModDimensions.DIFU) {
            return true;
        }
        if (SoulStateHandler.isSeparatedActor(actor) || SoulStateHandler.isSoulRealmEntity(target)) {
            return false;
        }
        if (actor != null && actor == target) {
            return true;
        }
        return true;
    }

    private static boolean isDifuSoulPlayerActor(Entity actor) {
        return SoulStateHandler.isSoulPlayer(actor) && actor.level().dimension() == ModDimensions.DIFU;
    }

    public static boolean shouldBlockOrdinaryDamage(DamageSource source, LivingEntity target) {
        if (target == null) {
            return false;
        }
        Entity actor = SoulStateHandler.resolveDamageActor(source);
        return !SoulStateHandler.canOrdinaryAffect(actor, (Entity)target);
    }

    public static Entity resolveDamageActor(DamageSource source) {
        Projectile projectile;
        Entity owner;
        if (source == null) {
            return null;
        }
        Entity actor = source.getEntity();
        if (actor != null) {
            return actor;
        }
        Entity direct = source.getDirectEntity();
        if (direct instanceof Projectile && (owner = (projectile = (Projectile)direct).getOwner()) != null) {
            return owner;
        }
        return direct;
    }

    public static void broadcastSouls(MinecraftServer server) {
        if (server == null) {
            return;
        }
        ArrayList<UUID> souls = new ArrayList<UUID>();
        for (ServerPlayer p : server.getPlayerList().getPlayers()) {
            CultivationData d = CultivationCapability.get((Player)p).orElse(null);
            if (d == null || !d.isSoulState()) continue;
            souls.add(p.getUUID());
        }
        ModNetwork.CHANNEL.send(PacketDistributor.ALL.noArg(), (Object)new SoulStatePacket(souls));
    }

    private static final class VoluntaryDifuTransfer {
        private final ResourceKey<Level> dimension;
        private final long startTick;
        private final long transferTick;
        private final boolean fromDeathChoice;
        private long lastVisualTick;
        private final double vortexX;
        private final double vortexY;
        private final double vortexZ;
        private boolean difuArrivalAttempted;
        private NaiheBridgeBuilder.ArrivalPreparation difuArrivalPreparation;
        private BlockPos difuArrival;
        private BlockPos difuFallbackArrival;
        private int lastCountdownSecond = Integer.MIN_VALUE;

        private VoluntaryDifuTransfer(ResourceKey<Level> dimension, long startTick, ServerPlayer player, boolean fromDeathChoice) {
            this.dimension = dimension;
            this.startTick = startTick;
            this.transferTick = startTick + 60L;
            this.fromDeathChoice = fromDeathChoice;
            this.lastVisualTick = startTick;
            this.vortexX = player.getX();
            this.vortexY = player.getY() + (double)player.getBbHeight() * 0.52 + 2.55;
            this.vortexZ = player.getZ();
        }

        private static VoluntaryDifuTransfer create(ResourceKey<Level> dimension, ServerPlayer player, long startTick, boolean fromDeathChoice) {
            return new VoluntaryDifuTransfer(dimension, startTick, player, fromDeathChoice);
        }
    }

    private record PendingDeathLocation(ResourceKey<Level> dimension, double x, double y, double z, float yRot) {
    }
}

