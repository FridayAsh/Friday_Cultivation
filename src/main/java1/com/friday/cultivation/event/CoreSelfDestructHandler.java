package com.friday.cultivation.event;

import com.friday.cultivation.CultivationCapability;
import com.friday.cultivation.entity.spell.ShockwaveEntity;
import com.friday.cultivation.realm.Realm;
import com.friday.cultivation.spell.Spell;
import com.friday.cultivation.util.SpellDamageSourceHelper;
import com.friday.cultivation.util.SpellScalingHelper;
import com.friday.cultivation.util.SpellTerrainDestructionHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseFireBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/** 金丹自爆处理器 — 完全照搬原模组 com.xiaoxiang.cultivation.event.CoreSelfDestructHandler */
@Mod.EventBusSubscriber(modid = "friday_cultivation")
public final class CoreSelfDestructHandler {
    private static final int DEATH_DELAY_TICKS = 100;
    private static final int CAMERA_PULLBACK_TICKS = 16;
    private static final Map<UUID, PendingDeath> PENDING_DEATHS = new ConcurrentHashMap<>();

    private CoreSelfDestructHandler() {
    }

    public static void cast(ServerPlayer player) {
        if (PENDING_DEATHS.containsKey(player.getUUID())) {
            return;
        }
        Realm realm = CultivationCapability.get((Player) player).map(data -> data.getRealm()).orElse(Realm.GOLDEN_CORE);
        ServerLevel level = player.serverLevel();
        Vec3 center = player.position().add(0.0, (double) player.getBbHeight() * 0.5, 0.0);
        int realmPower = Math.max(1, realm.ordinal() - Realm.GOLDEN_CORE.ordinal() + 1);
        float radius = Math.min(100.0f, 24.0f + (float) realmPower * 9.0f);
        float baseDamage = 320.0f + (float) (realmPower * realmPower) * 170.0f;
        float scaledDamage = SpellScalingHelper.scaledDamageFloat((LivingEntity) player, Spell.CORE_SELF_DESTRUCT, baseDamage);
        player.displayClientMessage((Component) Component.translatable("message.friday_cultivation.core_self_destruct.cast", new Object[]{Spell.CORE_SELF_DESTRUCT.displayNameForRealm(realm)}), true);
        CoreSelfDestructHandler.beginAfterglow(player, center, radius);
        AABB box = new AABB(center, center).inflate((double) radius);
        for (LivingEntity target : level.getEntitiesOfClass(LivingEntity.class, box, e -> e != player && e.isAlive())) {
            double dist;
            if (SectProtectionDomeHandler.isEntityProtectedByOwnDome((Entity) target) || !SoulStateHandler.canOrdinaryAffect((Entity) player, (Entity) target) || !SectCombatHandler.canApplyOffensiveEffect((LivingEntity) player, target) || (dist = Math.max(0.0, target.position().add(0.0, (double) target.getBbHeight() * 0.5, 0.0).distanceTo(center))) > (double) radius) continue;
            float falloff = (float) Math.max(0.35, 1.0 - dist / (double) radius);
            target.hurt(SpellDamageSourceHelper.directSpell((LivingEntity) player), scaledDamage * falloff);
            target.setSecondsOnFire(Math.max(target.getRemainingFireTicks() / 20, 6 + (int) (falloff * 12.0f)));
            Vec3 push = target.position().subtract(player.position());
            if (!(push.lengthSqr() > 1.0E-4)) continue;
            Vec3 normal = push.normalize();
            target.push(normal.x * (1.6 + (double) falloff * 2.2), 0.75 + (double) falloff * 1.1, normal.z * (1.6 + (double) falloff * 2.2));
        }
        SectProtectionDomeHandler.onSpellAreaTouchedBarrier(level, center, radius, (Entity) player, scaledDamage);
        if (SpellTerrainDestructionHelper.canModifyBlocks(level, (Entity) player)) {
            CoreSelfDestructHandler.shatterInnerCrater(level, center, radius, player);
        }
        float explosionPower = Math.min(34.0f, radius * 0.48f);
        level.explode((Entity) player, center.x, center.y, center.z, explosionPower, SpellTerrainDestructionHelper.explosionInteraction(level, (Entity) player, Level.ExplosionInteraction.TNT));
        if (SpellTerrainDestructionHelper.canModifyBlocks(level, (Entity) player)) {
            CoreSelfDestructHandler.igniteArea(level, center, radius, player);
        }
        ShockwaveEntity shockwave = new ShockwaveEntity(level, center, player.getUUID());
        shockwave.setDamageMultiplier(Math.max(2.5, Math.min(14.0, (double) scaledDamage / 115.0)));
        level.addFreshEntity((Entity) shockwave);
        level.playSound(null, center.x, center.y, center.z, SoundEvents.GENERIC_EXPLODE, SoundSource.PLAYERS, 7.0f, 0.42f);
        level.playSound(null, center.x, center.y, center.z, SoundEvents.LIGHTNING_BOLT_THUNDER, SoundSource.PLAYERS, 5.5f, 0.65f);
    }

    private static void beginAfterglow(ServerPlayer player, Vec3 center, float radius) {
        float yawRad = player.getYRot() * ((float) Math.PI / 180);
        Vec3 forward = new Vec3((double) (-Mth.sin((float) yawRad)), 0.0, (double) Mth.cos((float) yawRad));
        if (forward.lengthSqr() < 1.0E-4) {
            forward = new Vec3(0.0, 0.0, 1.0);
        }
        double backDistance = Mth.clamp((double) ((double) radius * 0.72), (double) 18.0, (double) 56.0);
        double upDistance = Mth.clamp((double) ((double) radius * 0.3), (double) 8.0, (double) 24.0);
        Vec3 cameraTarget = center.subtract(forward.normalize().scale(backDistance)).add(0.0, upDistance, 0.0);
        PendingDeath state = new PendingDeath((ResourceKey<Level>) player.level().dimension(), center, cameraTarget, player.gameMode.getGameModeForPlayer(), 100, 0);
        PENDING_DEATHS.put(player.getUUID(), state);
        if (player.gameMode.getGameModeForPlayer() != GameType.SPECTATOR) {
            player.setGameMode(GameType.SPECTATOR);
        }
        player.setInvisible(true);
        player.fallDistance = 0.0f;
        player.setDeltaMovement(Vec3.ZERO);
        player.teleportTo(player.serverLevel(), center.x, center.y, center.z, player.getYRot(), player.getXRot());
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
        ServerPlayer player2 = (ServerPlayer) player;
        PendingDeath state = PENDING_DEATHS.get(player2.getUUID());
        if (state == null) {
            return;
        }
        if (!player2.isAlive()) {
            PENDING_DEATHS.remove(player2.getUUID());
            return;
        }
        ServerLevel level = player2.server.getLevel(state.dimension());
        if (level == null) {
            PENDING_DEATHS.remove(player2.getUUID());
            CoreSelfDestructHandler.finishDeath(player2, state);
            return;
        }
        if (player2.gameMode.getGameModeForPlayer() != GameType.SPECTATOR) {
            player2.setGameMode(GameType.SPECTATOR);
        }
        PendingDeath next = state.tickDown();
        player2.setInvisible(true);
        player2.fallDistance = 0.0f;
        player2.setDeltaMovement(Vec3.ZERO);
        Vec3 cameraPos = next.cameraPos();
        player2.teleportTo(level, cameraPos.x, cameraPos.y, cameraPos.z, player2.getYRot(), player2.getXRot());
        int secondsLeft = Math.max(1, Mth.ceil((float) ((float) next.ticksLeft() / 20.0f)));
        player2.displayClientMessage((Component) Component.translatable("message.friday_cultivation.core_self_destruct.death_countdown", new Object[]{secondsLeft}).withStyle(ChatFormatting.GOLD), true);
        if (next.ticksLeft() <= 0) {
            PENDING_DEATHS.remove(player2.getUUID());
            CoreSelfDestructHandler.finishDeath(player2, state);
        } else {
            PENDING_DEATHS.put(player2.getUUID(), next);
        }
    }

    @SubscribeEvent
    public static void onPlayerLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        Player player = event.getEntity();
        if (!(player instanceof ServerPlayer)) {
            return;
        }
        ServerPlayer player2 = (ServerPlayer) player;
        PendingDeath state = PENDING_DEATHS.remove(player2.getUUID());
        if (state != null) {
            CoreSelfDestructHandler.finishDeath(player2, state);
        }
    }

    private static void finishDeath(ServerPlayer player, PendingDeath state) {
        GameType restore;
        player.setInvisible(false);
        GameType gameType = restore = state.previousMode() == GameType.SPECTATOR ? GameType.SURVIVAL : state.previousMode();
        if (player.gameMode.getGameModeForPlayer() != restore) {
            player.setGameMode(restore);
        }
        player.hurt(player.damageSources().genericKill(), Float.MAX_VALUE);
    }

    private static void shatterInnerCrater(ServerLevel level, Vec3 center, float radius, ServerPlayer caster) {
        RandomSource random = level.getRandom();
        double innerRadius = Math.min(28.0, (double) radius * 0.42);
        int samples = Math.min(18000, Math.max(2500, (int) (radius * radius * 4.5f)));
        int maxChanges = Math.min(6200, Math.max(1400, (int) (radius * 82.0f)));
        int changed = 0;
        for (int i = 0; i < samples && changed < maxChanges; ++i) {
            double z;
            double y;
            double r = innerRadius * Math.cbrt(random.nextDouble());
            double yaw = random.nextDouble() * Math.PI * 2.0;
            double pitch = Math.asin(random.nextDouble() * 2.0 - 1.0);
            double x = center.x + Math.cos(yaw) * Math.cos(pitch) * r;
            BlockPos pos = BlockPos.containing((double) x, (double) (y = center.y + Math.sin(pitch) * r * 0.78), (double) (z = center.z + Math.sin(yaw) * Math.cos(pitch) * r));
            if (!CoreSelfDestructHandler.tryRemoveBlock(level, pos, caster)) continue;
            ++changed;
        }
    }

    private static boolean tryRemoveBlock(ServerLevel level, BlockPos pos, ServerPlayer caster) {
        if (!SpellTerrainDestructionHelper.canModifyBlocks(level, (Entity) caster)) {
            return false;
        }
        if (SectProtectionDomeHandler.isProtectedByAnySectProtectionDome((Level) level, pos)) {
            return false;
        }
        BlockState state = level.getBlockState(pos);
        if (state.isAir() || state.is(Blocks.BEDROCK) || state.is(Blocks.END_PORTAL_FRAME) || state.is(Blocks.COMMAND_BLOCK) || state.is(Blocks.CHAIN_COMMAND_BLOCK) || state.is(Blocks.REPEATING_COMMAND_BLOCK) || state.getDestroySpeed((BlockGetter) level, pos) < 0.0f) {
            return false;
        }
        return SpellTerrainDestructionHelper.setBlock(level, pos, Blocks.AIR.defaultBlockState(), 2, (Entity) caster);
    }

    private static void igniteArea(ServerLevel level, Vec3 center, float radius, ServerPlayer caster) {
        if (!SpellTerrainDestructionHelper.canModifyBlocks(level, (Entity) caster)) {
            return;
        }
        RandomSource random = level.getRandom();
        int samples = Math.min(1700, Math.max(420, (int) (radius * radius * 0.38f)));
        int verticalScan = 10 + (int) Math.min(12.0f, radius * 0.18f);
        block0:
        for (int i = 0; i < samples; ++i) {
            double r = (double) radius * Math.sqrt(random.nextDouble());
            double angle = random.nextDouble() * Math.PI * 2.0;
            int x = (int) Math.floor(center.x + Math.cos(angle) * r);
            int z = (int) Math.floor(center.z + Math.sin(angle) * r);
            int top = (int) Math.floor(center.y + (double) verticalScan * 0.5);
            int bottom = (int) Math.floor(center.y - (double) verticalScan);
            for (int y = top; y >= bottom; --y) {
                BlockPos base = new BlockPos(x, y, z);
                if (level.getBlockState(base).isAir()) continue;
                BlockPos firePos = base.above();
                if (!level.getBlockState(firePos).isAir() || SectProtectionDomeHandler.isProtectedByAnySectProtectionDome((Level) level, firePos)) continue block0;
                SpellTerrainDestructionHelper.setBlock(level, firePos, BaseFireBlock.getState((BlockGetter) level, (BlockPos) firePos), 3, (Entity) caster);
                continue block0;
            }
        }
    }

    private record PendingDeath(ResourceKey<Level> dimension, Vec3 center, Vec3 cameraTarget, GameType previousMode, int ticksLeft, int elapsedTicks) {
        PendingDeath tickDown() {
            return new PendingDeath(this.dimension, this.center, this.cameraTarget, this.previousMode, this.ticksLeft - 1, this.elapsedTicks + 1);
        }

        Vec3 cameraPos() {
            double progress = Mth.clamp((double) ((double) this.elapsedTicks / 16.0), (double) 0.0, (double) 1.0);
            double eased = 1.0 - Math.pow(1.0 - progress, 3.0);
            return this.center.lerp(this.cameraTarget, eased);
        }
    }
}
