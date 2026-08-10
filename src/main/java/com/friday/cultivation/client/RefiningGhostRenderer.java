/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.vertex.PoseStack
 *  com.mojang.blaze3d.vertex.VertexConsumer
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.multiplayer.ClientLevel
 *  net.minecraft.client.renderer.LevelRenderer
 *  net.minecraft.client.renderer.MultiBufferSource
 *  net.minecraft.client.renderer.MultiBufferSource$BufferSource
 *  net.minecraft.client.renderer.RenderType
 *  net.minecraft.client.renderer.block.BlockRenderDispatcher
 *  net.minecraft.client.renderer.culling.Frustum
 *  net.minecraft.client.renderer.texture.OverlayTexture
 *  net.minecraft.core.BlockPos
 *  net.minecraft.world.level.LevelReader
 *  net.minecraft.world.level.block.Block
 *  net.minecraft.world.level.block.state.BlockState
 *  net.minecraft.world.phys.AABB
 *  net.minecraft.world.phys.Vec3
 *  net.minecraftforge.api.distmarker.Dist
 *  net.minecraftforge.client.event.RenderLevelStageEvent
 *  net.minecraftforge.client.event.RenderLevelStageEvent$Stage
 *  net.minecraftforge.client.model.data.ModelData
 *  net.minecraftforge.event.TickEvent$ClientTickEvent
 *  net.minecraftforge.event.TickEvent$Phase
 *  net.minecraftforge.eventbus.api.SubscribeEvent
 *  net.minecraftforge.fml.common.Mod$EventBusSubscriber
 *  net.minecraftforge.fml.common.Mod$EventBusSubscriber$Bus
 */
package com.friday.cultivation.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.friday.cultivation.block.refining.RefiningFurnaceStructure;
import com.friday.cultivation.client.GhostBufferSource;
import com.friday.cultivation.registry.ModBlocks;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.client.model.data.ModelData;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid="friday_cultivation", bus=Mod.EventBusSubscriber.Bus.FORGE, value={Dist.CLIENT})
public final class RefiningGhostRenderer {
    private static final int GHOST_ALPHA = 90;
    private static final int CLEANUP_INTERVAL_TICKS = 5;
    private static final Set<BlockPos> ACTIVE_CORES = new HashSet<BlockPos>();
    private static int tickCounter = 0;

    private RefiningGhostRenderer() {
    }

    public static void toggleCore(BlockPos corePos) {
        BlockPos immutable = corePos.east();
        if (ACTIVE_CORES.contains(immutable)) {
            ACTIVE_CORES.remove(immutable);
        } else {
            ACTIVE_CORES.add(immutable);
        }
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) {
            ACTIVE_CORES.clear();
            return;
        }
        if (++tickCounter < 5) {
            return;
        }
        tickCounter = 0;
        ClientLevel level = mc.level;
        Block coreBlock = (Block)ModBlocks.REFINING_CORE.get();
        Iterator<BlockPos> it = ACTIVE_CORES.iterator();
        while (it.hasNext()) {
            BlockPos pos = it.next();
            if (!level.isLoaded(pos)) continue;
            if (!level.getBlockState(pos).is(coreBlock)) {
                it.remove();
                continue;
            }
            if (!RefiningFurnaceStructure.isComplete((LevelReader)level, pos)) continue;
            it.remove();
        }
    }

    @SubscribeEvent
    public static void onRenderLevel(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_TRANSLUCENT_BLOCKS) {
            return;
        }
        if (ACTIVE_CORES.isEmpty()) {
            return;
        }
        Minecraft mc = Minecraft.getInstance();
        ClientLevel level = mc.level;
        if (level == null) {
            return;
        }
        Frustum frustum = event.getFrustum();
        Vec3 camPos = event.getCamera().getPosition();
        BlockRenderDispatcher dispatcher = mc.getBlockRenderer();
        ArrayList<MissingEntry> toRender = new ArrayList<MissingEntry>();
        for (BlockPos corePos : ACTIVE_CORES) {
            if (!level.isLoaded(corePos)) continue;
            for (RefiningFurnaceStructure.MissingBlock missingBlock : RefiningFurnaceStructure.missingBlocks((LevelReader)level, corePos)) {
                BlockPos pos = missingBlock.pos();
                if (!frustum.isVisible(new AABB(pos))) continue;
                toRender.add(new MissingEntry(pos, missingBlock.expected().defaultBlockState()));
            }
        }
        if (toRender.isEmpty()) {
            return;
        }
        PoseStack pose = event.getPoseStack();
        MultiBufferSource.BufferSource source = mc.renderBuffers().bufferSource();
        GhostBufferSource ghostSource = new GhostBufferSource((MultiBufferSource)source, 90);
        pose.pushPose();
        pose.translate(-camPos.x, -camPos.y, -camPos.z);
        for (MissingEntry e : toRender) {
            pose.pushPose();
            pose.translate((float)e.pos.getX(), (float)e.pos.getY(), (float)e.pos.getZ());
            dispatcher.renderSingleBlock(e.state, pose, (MultiBufferSource)ghostSource, 0xF000F0, OverlayTexture.NO_OVERLAY, ModelData.EMPTY, null);
            pose.popPose();
        }
        source.endBatch(RenderType.translucent());
        VertexConsumer vertexConsumer = source.getBuffer(RenderType.lines());
        for (MissingEntry e : toRender) {
            BlockPos pos = e.pos;
            LevelRenderer.renderLineBox((PoseStack)pose, (VertexConsumer)vertexConsumer, (double)pos.getX(), (double)pos.getY(), (double)pos.getZ(), (double)(pos.getX() + 1), (double)(pos.getY() + 1), (double)(pos.getZ() + 1), (float)1.0f, (float)1.0f, (float)1.0f, (float)0.5f);
        }
        source.endBatch(RenderType.lines());
        pose.popPose();
    }

    private record MissingEntry(BlockPos pos, BlockState state) {
    }
}

