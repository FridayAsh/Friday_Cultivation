package com.friday.cultivation.event;

import com.friday.cultivation.block.refining.RefiningFurnaceStructure;
import com.friday.cultivation.registry.ModBlocks;
import net.minecraft.core.BlockPos;
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

/**
 * 炼器炉结构监听器 — 完整复刻原模组 RefiningStructureWatcher。
 * 当玩家放置方块时，检查附近的炼器核心是否完成了结构，若完成则触发粒子+音效。
 */
@Mod.EventBusSubscriber(modid = "friday_cultivation", bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class RefiningStructureWatcher {
    private RefiningStructureWatcher() {}

    @SubscribeEvent
    public static void onBlockPlaced(BlockEvent.EntityPlaceEvent event) {
        if (event.getLevel().isClientSide()) return;
        LevelAccessor levelAccessor = event.getLevel();
        if (!(levelAccessor instanceof ServerLevel)) return;
        ServerLevel level = (ServerLevel) levelAccessor;
        BlockPos placedPos = event.getPos();
        Block coreBlock = ModBlocks.REFINING_CORE.get();
        for (int dx = -3; dx <= 3; dx++) {
            for (int dy = -3; dy <= 3; dy++) {
                block2:
                for (int dz = -3; dz <= 3; dz++) {
                    BlockPos candidate = placedPos.offset(dx, dy, dz);
                    if (!level.getBlockState(candidate).is(coreBlock)) continue;
                    for (RefiningFurnaceStructure.RequiredBlock rb : RefiningFurnaceStructure.requiredBlocks()) {
                        if (!candidate.offset(rb.offset()).equals(placedPos)) continue;
                        if (!rb.matches(level.getBlockState(placedPos)) || !RefiningFurnaceStructure.isComplete((LevelReader) level, candidate)) continue block2;
                        fireCompletionEffects(level, candidate);
                        continue block2;
                    }
                }
            }
        }
    }

    private static void fireCompletionEffects(ServerLevel level, BlockPos corePos) {
        double cx = corePos.getX() + 0.5;
        double cy = corePos.getY() + 0.5;
        double cz = corePos.getZ() + 0.5;
        level.sendParticles(ParticleTypes.END_ROD, cx, cy, cz, 80, 2.5, 2.5, 2.5, 0.5);
        level.sendParticles(ParticleTypes.FLAME, cx, cy + 1.5, cz, 40, 2.0, 0.8, 2.0, 0.05);
        level.sendParticles(ParticleTypes.SMOKE, cx, cy + 0.2, cz, 30, 1.5, 0.1, 1.5, 0.08);
        level.sendParticles(ParticleTypes.GLOW, cx, cy + 1.0, cz, 25, 2.0, 1.0, 2.0, 0.02);
        level.playSound(null, cx, cy, cz, SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.BLOCKS, 1.0f, 0.8f);
        level.playSound(null, cx, cy, cz, SoundEvents.PLAYER_LEVELUP, SoundSource.BLOCKS, 1.0f, 1.2f);
        level.playSound(null, cx, cy, cz, SoundEvents.BEACON_ACTIVATE, SoundSource.BLOCKS, 1.0f, 1.0f);
    }
}
