package com.friday.cultivation.util;

import com.friday.cultivation.CultivationCapability;
import com.friday.cultivation.config.ModCommonConfig;
import com.friday.cultivation.event.SectCombatHandler;
import com.friday.cultivation.sect.SectSavedData;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public final class SpellTerrainDestructionHelper {
    private SpellTerrainDestructionHelper() {
    }

    public static boolean canModifyBlocks(ServerLevel level) {
        return SpellTerrainDestructionHelper.canModifyBlocks(level, null);
    }

    public static boolean canModifyBlocks(ServerLevel level, @Nullable Entity caster) {
        if (level == null || level.isClientSide) {
            return false;
        }
        if (ModCommonConfig.spellTerrainDestructionForceDisabled()) {
            return false;
        }
        if (caster instanceof ServerPlayer) {
            ServerPlayer player = (ServerPlayer)caster;
            return CultivationCapability.get((Player)player).map(data -> data.isSpellTerrainDestructionEnabled()).orElse(ModCommonConfig.spellTerrainDestructionDefaultEnabled());
        }
        return ModCommonConfig.spellTerrainDestructionDefaultEnabled();
    }

    public static Level.ExplosionInteraction explosionInteraction(Level.ExplosionInteraction enabledInteraction) {
        return !ModCommonConfig.spellTerrainDestructionForceDisabled() && ModCommonConfig.spellTerrainDestructionDefaultEnabled() ? enabledInteraction : Level.ExplosionInteraction.NONE;
    }

    public static Level.ExplosionInteraction explosionInteraction(ServerLevel level, @Nullable Entity caster, Level.ExplosionInteraction enabledInteraction) {
        return SpellTerrainDestructionHelper.canModifyBlocks(level, caster) ? enabledInteraction : Level.ExplosionInteraction.NONE;
    }

    public static boolean shouldCreateExplosionFire(ServerLevel level) {
        return SpellTerrainDestructionHelper.canModifyBlocks(level);
    }

    public static boolean shouldCreateExplosionFire(ServerLevel level, @Nullable Entity caster) {
        return SpellTerrainDestructionHelper.canModifyBlocks(level, caster);
    }

    public static boolean setBlock(ServerLevel level, BlockPos pos, BlockState state, int flags) {
        if (!SpellTerrainDestructionHelper.canModifyBlocks(level)) {
            return false;
        }
        BlockState before = level.getBlockState(pos);
        boolean changed = !before.equals(state) && level.setBlock(pos, state, flags);
        return changed;
    }

    public static boolean setBlock(ServerLevel level, BlockPos pos, BlockState state, int flags, @Nullable Entity caster) {
        boolean changed;
        if (!SpellTerrainDestructionHelper.canModifyBlocks(level, caster)) {
            return false;
        }
        BlockState before = level.getBlockState(pos);
        boolean bl = changed = !before.equals(state) && level.setBlock(pos, state, flags);
        if (changed) {
            SpellTerrainDestructionHelper.markArraySaboteur(level, pos, before, caster);
        }
        return changed;
    }

    public static boolean setBlockAndUpdate(ServerLevel level, BlockPos pos, BlockState state) {
        if (!SpellTerrainDestructionHelper.canModifyBlocks(level)) {
            return false;
        }
        BlockState before = level.getBlockState(pos);
        return !before.equals(state) && level.setBlockAndUpdate(pos, state);
    }

    public static boolean setBlockAndUpdate(ServerLevel level, BlockPos pos, BlockState state, @Nullable Entity caster) {
        boolean changed;
        if (!SpellTerrainDestructionHelper.canModifyBlocks(level, caster)) {
            return false;
        }
        BlockState before = level.getBlockState(pos);
        boolean bl = changed = !before.equals(state) && level.setBlockAndUpdate(pos, state);
        if (changed) {
            SpellTerrainDestructionHelper.markArraySaboteur(level, pos, before, caster);
        }
        return changed;
    }

    public static boolean destroyBlock(ServerLevel level, BlockPos pos, boolean dropBlock, Entity breaker) {
        boolean changed;
        if (!SpellTerrainDestructionHelper.canModifyBlocks(level, breaker)) {
            return false;
        }
        BlockState before = level.getBlockState(pos);
        boolean bl = changed = !before.isAir() && level.destroyBlock(pos, dropBlock, breaker);
        if (changed) {
            SpellTerrainDestructionHelper.markArraySaboteur(level, pos, before, breaker);
        }
        return changed;
    }

    public static boolean destroyBlock(ServerLevel level, BlockPos pos, boolean dropBlock, @Nullable Entity breaker, @Nullable Entity caster) {
        boolean changed;
        if (!SpellTerrainDestructionHelper.canModifyBlocks(level, caster)) {
            return false;
        }
        BlockState before = level.getBlockState(pos);
        boolean bl = changed = !before.isAir() && level.destroyBlock(pos, dropBlock, breaker);
        if (changed) {
            SpellTerrainDestructionHelper.markArraySaboteur(level, pos, before, caster != null ? caster : breaker);
        }
        return changed;
    }

    private static void markArraySaboteur(ServerLevel level, BlockPos pos, BlockState brokenState, @Nullable Entity actor) {
        if (!(actor instanceof LivingEntity)) {
            return;
        }
        LivingEntity living = (LivingEntity)actor;
        SectCombatHandler.markArraySaboteur(level, SectSavedData.get(level), pos, brokenState, living, living instanceof ServerPlayer);
    }
}
