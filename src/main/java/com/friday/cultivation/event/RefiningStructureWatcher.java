/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.BlockPos
 *  net.minecraft.core.Vec3i
 *  net.minecraft.core.particles.ParticleOptions
 *  net.minecraft.core.particles.ParticleTypes
 *  net.minecraft.server.level.ServerLevel
 *  net.minecraft.sounds.SoundEvents
 *  net.minecraft.sounds.SoundSource
 *  net.minecraft.world.level.LevelAccessor
 *  net.minecraft.world.level.LevelReader
 *  net.minecraft.world.level.block.Block
 *  net.minecraftforge.event.level.BlockEvent$EntityPlaceEvent
 *  net.minecraftforge.eventbus.api.SubscribeEvent
 *  net.minecraftforge.fml.common.Mod$EventBusSubscriber
 *  net.minecraftforge.fml.common.Mod$EventBusSubscriber$Bus
 */
package com.friday.cultivation.event;

import com.friday.cultivation.block.refining.RefiningFurnaceStructure;
import com.friday.cultivation.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid="friday_cultivation", bus=Mod.EventBusSubscriber.Bus.FORGE)
public final class RefiningStructureWatcher {
    private RefiningStructureWatcher() {
    }

    @SubscribeEvent
    public static void onBlockPlaced(BlockEvent.EntityPlaceEvent event) {
        if (event.getLevel().isClientSide()) {
            return;
        }
        LevelAccessor levelAccessor = event.getLevel();
        if (!(levelAccessor instanceof ServerLevel)) {
            return;
        }
        ServerLevel level = (ServerLevel)levelAccessor;
        BlockPos placedPos = event.getPos();
        Block coreBlock = (Block)ModBlocks.REFINING_CORE.get();
        for (int dx = -2; dx <= 2; ++dx) {
            for (int dy = -2; dy <= 2; ++dy) {
                block2: for (int dz = -2; dz <= 2; ++dz) {
                    BlockPos candidate = placedPos.offset(dx, dy, dz);
                    if (!level.getBlockState(candidate).is(coreBlock)) continue;
                    for (RefiningFurnaceStructure.RequiredBlock rb : RefiningFurnaceStructure.requiredBlocks()) {
                        if (!candidate.offset((Vec3i)rb.offset()).equals((Object)placedPos)) continue;
                        if (!rb.matches(level.getBlockState(placedPos)) || !RefiningFurnaceStructure.isComplete((LevelReader)level, candidate)) continue block2;
                        RefiningStructureWatcher.fireCompletionEffects(level, candidate);
                        continue block2;
                    }
                }
            }
        }
    }

    private static void fireCompletionEffects(ServerLevel level, BlockPos corePos) {
        double cx = (double)corePos.getX() + 0.5;
        double cy = (double)corePos.getY() + 0.5;
        double cz = (double)corePos.getZ() + 0.5;
        level.sendParticles((ParticleOptions)ParticleTypes.ENCHANT, cx, cy, cz, 80, 2.0, 2.0, 2.0, 0.5);
        level.sendParticles((ParticleOptions)ParticleTypes.HAPPY_VILLAGER, cx, cy + 1.5, cz, 40, 1.5, 0.8, 1.5, 0.05);
        level.sendParticles((ParticleOptions)ParticleTypes.SOUL_FIRE_FLAME, cx, cy + 0.2, cz, 30, 1.2, 0.1, 1.2, 0.08);
        level.sendParticles((ParticleOptions)ParticleTypes.GLOW, cx, cy + 1.0, cz, 25, 1.5, 1.0, 1.5, 0.02);
        level.playSound(null, cx, cy, cz, SoundEvents.BEACON_ACTIVATE, SoundSource.BLOCKS, 1.0f, 0.8f);
        level.playSound(null, cx, cy, cz, SoundEvents.PLAYER_LEVELUP, SoundSource.BLOCKS, 1.0f, 1.2f);
        level.playSound(null, cx, cy, cz, SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.BLOCKS, 1.0f, 1.0f);
    }
}

