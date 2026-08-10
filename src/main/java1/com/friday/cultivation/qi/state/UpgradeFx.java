package com.friday.cultivation.qi.state;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.block.state.BlockState;

/**
 * 方块灵气升级特效（严格照搬原模组 com.xiaoxiang.cultivation.cultivation.qi.state.UpgradeFx）
 */
public final class UpgradeFx {
    private UpgradeFx() {}

    public static void play(ServerLevel level, BlockPos pos, BlockState oldState, BlockState newState) {
        double cx = pos.getX() + 0.5;
        double cy = pos.getY() + 0.5;
        double cz = pos.getZ() + 0.5;
        level.sendParticles((ParticleOptions)new BlockParticleOption(ParticleTypes.BLOCK, newState), cx, cy + 0.5, cz, 15, 0.3, 0.2, 0.3, 0.05);
        level.sendParticles((ParticleOptions)ParticleTypes.HAPPY_VILLAGER, cx, cy + 0.6, cz, 12, 0.3, 0.3, 0.3, 0.05);
        level.playSound(null, pos, SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.BLOCKS, 0.7f, 1.2f);
    }
}