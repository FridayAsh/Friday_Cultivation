/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.BlockPos
 *  net.minecraft.core.particles.BlockParticleOption
 *  net.minecraft.core.particles.ParticleOptions
 *  net.minecraft.core.particles.ParticleTypes
 *  net.minecraft.server.level.ServerLevel
 *  net.minecraft.sounds.SoundEvents
 *  net.minecraft.sounds.SoundSource
 *  net.minecraft.world.level.block.state.BlockState
 */
package com.friday.cultivation.cultivation.qi.state;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.block.state.BlockState;

public final class UpgradeFx {
    private UpgradeFx() {
    }

    public static void play(ServerLevel level, BlockPos pos, BlockState oldState, BlockState newState) {
        double cx = (double)pos.getX() + 0.5;
        double cy = (double)pos.getY() + 0.5;
        double cz = (double)pos.getZ() + 0.5;
        level.sendParticles((ParticleOptions)new BlockParticleOption(ParticleTypes.BLOCK, newState), cx, cy + 0.5, cz, 15, 0.3, 0.2, 0.3, 0.05);
        level.sendParticles((ParticleOptions)ParticleTypes.COMPOSTER, cx, cy + 0.6, cz, 12, 0.3, 0.3, 0.3, 0.05);
        level.playSound(null, pos, SoundEvents.MOSS_PLACE, SoundSource.BLOCKS, 0.7f, 1.2f);
    }
}

