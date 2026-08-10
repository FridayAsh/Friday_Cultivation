package com.friday.cultivation.worldgen;

import com.mojang.serialization.Codec;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.world.level.levelgen.structure.templatesystem.BlockIgnoreProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;

public class DifuBigChainFeature extends Feature<NoneFeatureConfiguration> {
    private static final ResourceLocation CHAIN_ID = new ResourceLocation("xiaoxiang_cultivation", "chain_structure");

    public DifuBigChainFeature(Codec<NoneFeatureConfiguration> codec) { super(codec); }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> ctx) {
        WorldGenLevel level = ctx.level();
        ServerLevel server = level.getLevel();
        StructureTemplateManager mgr = server.getStructureManager();
        Optional<StructureTemplate> opt = mgr.get(CHAIN_ID);
        if (opt.isEmpty()) return false;
        StructureTemplate template = opt.get();
        Vec3i size = template.getSize();
        int segH = size.getY();
        if (segH <= 0) return false;
        RandomSource random = ctx.random();
        BlockPos origin = ctx.origin();
        int x = origin.getX(), z = origin.getZ();
        int startY = level.getHeight(Heightmap.Types.WORLD_SURFACE_WG, x, z) - segH;
        int topY = level.getMaxBuildHeight() - segH - 1;
        if (startY >= topY) return false;
        StructurePlaceSettings settings = new StructurePlaceSettings()
                .setIgnoreEntities(true)
                .addProcessor(BlockIgnoreProcessor.STRUCTURE_BLOCK);
        for (int y = startY; y <= topY; y += segH) {
            BlockPos at = new BlockPos(x, y, z);
            template.placeInWorld(level, at, at, settings, random, 2);
        }
        return true;
    }
}
