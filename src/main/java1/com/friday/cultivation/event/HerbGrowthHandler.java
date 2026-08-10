package com.friday.cultivation.event;

import com.friday.cultivation.qi.BlockQiSpec;
import com.friday.cultivation.qi.BlockQiSpecs;
import com.friday.cultivation.qi.QiEcosystem;
import com.friday.cultivation.qi.field.QiFieldRegistry;
import com.friday.cultivation.qi.field.QiModifier;
import com.friday.cultivation.registry.ModBlocks;
import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = "friday_cultivation", bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class HerbGrowthHandler {
    private static final int SAMPLE_INTERVAL_TICKS = 200;
    private static final int SAMPLES_PER_TICK = 6;
    private static final int SAMPLE_RADIUS = 48;
    private static final double BASE_GROW_CHANCE = 0.02;
    private static final Random RNG = new Random();

    private HerbGrowthHandler() {
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
        ServerPlayer sp = (ServerPlayer) player;
        Level level = sp.level();
        if (!(level instanceof ServerLevel)) {
            return;
        }
        ServerLevel sl = (ServerLevel) level;
        if (!sl.dimension().equals((Object) Level.OVERWORLD)) {
            return;
        }
        if (sp.tickCount % 200 != 0) {
            return;
        }
        Block herbBlock = (Block) ModBlocks.HERB.get();
        BlockState herbState = herbBlock.defaultBlockState();
        BlockPos pCenter = sp.blockPosition();
        for (int i = 0; i < 6; ++i) {
            BlockQiSpec rawSpec;
            int y;
            int dx = RNG.nextInt(97) - 48;
            int dz = RNG.nextInt(97) - 48;
            int x = pCenter.getX() + dx;
            int z = pCenter.getZ() + dz;
            if (!sl.isInWorldBounds(new BlockPos(x, sl.getMinBuildHeight(), z)) || (y = sl.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z)) <= sl.getMinBuildHeight() + 1 || y >= sl.getMaxBuildHeight()) continue;
            BlockPos placePos = new BlockPos(x, y, z);
            BlockPos groundPos = placePos.below();
            BlockState above = sl.getBlockState(placePos);
            BlockState ground = sl.getBlockState(groundPos);
            if (!above.isAir() || !HerbGrowthHandler.isPlantableGround(ground) || (rawSpec = BlockQiSpecs.of(ground)) == null) continue;
            double currentQi = QiEcosystem.peekBlock(sl, groundPos);
            double rawMax = rawSpec.baseMaxQi();
            double ratio = rawMax > 0.0 ? currentQi / rawMax : 0.0;
            QiModifier mod = QiFieldRegistry.of(sl).composedModifierAt(groundPos, rawSpec);
            double emitMult = mod.emitMult();
            double chance = 0.02 * ratio * emitMult;
            if (RNG.nextDouble() >= chance || !herbState.canSurvive((LevelReader) sl, placePos)) continue;
            sl.setBlock(placePos, herbState, 3);
        }
    }

    private static boolean isPlantableGround(BlockState state) {
        return state.is(Blocks.GRASS_BLOCK) || state.is(Blocks.PODZOL) || state.is(Blocks.MYCELIUM) || state.is(Blocks.MOSS_BLOCK);
    }
}
