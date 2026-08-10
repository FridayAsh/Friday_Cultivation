/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.serialization.Codec
 *  net.minecraft.core.BlockPos
 *  net.minecraft.core.Vec3i
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.server.level.ServerLevel
 *  net.minecraft.util.RandomSource
 *  net.minecraft.world.level.ServerLevelAccessor
 *  net.minecraft.world.level.WorldGenLevel
 *  net.minecraft.world.level.levelgen.Heightmap$Types
 *  net.minecraft.world.level.levelgen.feature.Feature
 *  net.minecraft.world.level.levelgen.feature.FeaturePlaceContext
 *  net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration
 *  net.minecraft.world.level.levelgen.structure.templatesystem.BlockIgnoreProcessor
 *  net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings
 *  net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessor
 *  net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate
 *  net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager
 *  org.jetbrains.annotations.NotNull
 */
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
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;
import org.jetbrains.annotations.NotNull;

public class DifuBigChainFeature
extends Feature<NoneFeatureConfiguration> {
    private static final ResourceLocation CHAIN_ID = new ResourceLocation((String)"friday_cultivation", (String)"chain_structure");

    public DifuBigChainFeature(Codec<NoneFeatureConfiguration> codec) {
        super(codec);
    }

    public boolean place(@NotNull FeaturePlaceContext<NoneFeatureConfiguration> ctx) {
        int topY;
        int z;
        WorldGenLevel level = ctx.level();
        ServerLevel server = level.getLevel();
        StructureTemplateManager mgr = server.getStructureManager();
        Optional opt = mgr.get(CHAIN_ID);
        if (opt.isEmpty()) {
            return false;
        }
        StructureTemplate template = (StructureTemplate)opt.get();
        Vec3i size = template.getSize();
        int segH = size.getY();
        if (segH <= 0) {
            return false;
        }
        RandomSource random = ctx.random();
        BlockPos origin = ctx.origin();
        int x = origin.getX();
        int startY = level.getHeight(Heightmap.Types.WORLD_SURFACE_WG, x, z = origin.getZ()) - segH;
        if (startY >= (topY = level.getMaxBuildHeight() - segH - 1)) {
            return false;
        }
        StructurePlaceSettings settings = new StructurePlaceSettings().setIgnoreEntities(true).addProcessor((StructureProcessor)BlockIgnoreProcessor.STRUCTURE_AND_AIR);
        for (int y = startY; y <= topY; y += segH) {
            BlockPos at = new BlockPos(x, y, z);
            template.placeInWorld((ServerLevelAccessor)level, at, at, settings, random, 2);
        }
        return true;
    }
}

