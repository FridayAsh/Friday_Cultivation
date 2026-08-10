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
 * 降阶特效 - 方块灵气耗尽降阶时播放粒子和音效。
 * 完全照搬原 mod: xiaoxiang.cultivation.cultivation.qi.state.DegradeFx
 */
public final class DegradeFx {
    private DegradeFx() {
    }

    public static void play(ServerLevel level, BlockPos pos, BlockState oldState, BlockState newState) {
        double cx = pos.getX() + 0.5;
        double cy = pos.getY() + 0.5;
        double cz = pos.getZ() + 0.5;
        level.sendParticles((ParticleOptions) new BlockParticleOption(ParticleTypes.BLOCK, oldState), cx, cy + 0.5, cz, 15, 0.3, 0.2, 0.3, 0.1);
        level.sendParticles((ParticleOptions) ParticleTypes.CAMPFIRE_COSY_SMOKE, cx, cy + 1.0, cz, 8, 0.2, 0.2, 0.2, 0.02);
        level.playSound(null, pos, SoundEvents.STONE_BREAK, SoundSource.BLOCKS, 0.8f, 0.7f);
    }
}
