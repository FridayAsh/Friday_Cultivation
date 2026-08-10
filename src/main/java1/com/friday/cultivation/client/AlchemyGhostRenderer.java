package com.friday.cultivation.client;

import com.friday.cultivation.block.alchemy.AlchemyFurnaceStructure;
import com.friday.cultivation.registry.ModBlocks;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.MultiBufferSource;
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

/**
 * 炼丹Ghost渲染器 — 完整复刻原模组 AlchemyGhostRenderer。
 * 当玩家切换 ghost 模式时，以半透明渲染炼丹炉结构中缺失的方块，便于玩家查看还需放置哪些方块。
 *
 * 注意：依赖 GhostBufferSource（后续 Phase 创建）和 AlchemyFurnaceStructure.missingBlocks。
 */
@Mod.EventBusSubscriber(modid = "friday_cultivation", bus = Mod.EventBusSubscriber.Bus.FORGE, value = {Dist.CLIENT})
public final class AlchemyGhostRenderer {
    private static final int GHOST_ALPHA = 90;
    private static final int CLEANUP_INTERVAL_TICKS = 5;
    private static final Set<BlockPos> ACTIVE_CORES = new HashSet<>();
    private static int tickCounter = 0;

    private AlchemyGhostRenderer() {}

    public static void toggleCore(BlockPos corePos) {
        BlockPos immutable = corePos.immutable();
        if (ACTIVE_CORES.contains(immutable)) {
            ACTIVE_CORES.remove(immutable);
        } else {
            ACTIVE_CORES.add(immutable);
        }
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) {
            ACTIVE_CORES.clear();
            return;
        }
        if (++tickCounter < CLEANUP_INTERVAL_TICKS) return;
        tickCounter = 0;
        ClientLevel level = mc.level;
        Block coreBlock = ModBlocks.ALCHEMY_CORE.get();
        Iterator<BlockPos> it = ACTIVE_CORES.iterator();
        while (it.hasNext()) {
            BlockPos pos = it.next();
            if (!level.isLoaded(pos)) continue;
            if (!level.getBlockState(pos).is(coreBlock)) {
                it.remove();
                continue;
            }
            if (!AlchemyFurnaceStructure.isComplete((LevelReader) level, pos)) continue;
            it.remove();
        }
    }

    @SubscribeEvent
    public static void onRenderLevel(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_TRANSLUCENT_BLOCKS) return;
        if (ACTIVE_CORES.isEmpty()) return;
        Minecraft mc = Minecraft.getInstance();
        ClientLevel level = mc.level;
        if (level == null) return;
        Frustum frustum = event.getFrustum();
        Vec3 camPos = event.getCamera().getPosition();
        BlockRenderDispatcher dispatcher = mc.getBlockRenderer();
        ArrayList<MissingEntry> toRender = new ArrayList<>();
        for (BlockPos corePos : ACTIVE_CORES) {
            if (!level.isLoaded(corePos)) continue;
            for (AlchemyFurnaceStructure.MissingBlock missingBlock : AlchemyFurnaceStructure.missingBlocks((LevelReader) level, corePos)) {
                BlockPos pos = missingBlock.pos();
                if (!frustum.isVisible(new AABB(pos))) continue;
                toRender.add(new MissingEntry(pos, missingBlock.expected().defaultBlockState()));
            }
        }
        if (toRender.isEmpty()) return;
        com.mojang.blaze3d.vertex.PoseStack pose = event.getPoseStack();
        MultiBufferSource.BufferSource source = mc.renderBuffers().bufferSource();
        com.friday.cultivation.client.GhostBufferSource ghostSource = new com.friday.cultivation.client.GhostBufferSource((MultiBufferSource) source, GHOST_ALPHA);
        pose.pushPose();
        pose.translate(-camPos.x, -camPos.y, -camPos.z);
        for (MissingEntry e : toRender) {
            pose.pushPose();
            pose.translate(e.pos.getX(), e.pos.getY(), e.pos.getZ());
            dispatcher.renderSingleBlock(e.state, pose, (MultiBufferSource) ghostSource, 0xF000F0, OverlayTexture.NO_OVERLAY, ModelData.EMPTY, null);
            pose.popPose();
        }
        source.endBatch(net.minecraft.client.renderer.RenderType.translucent());
        com.mojang.blaze3d.vertex.VertexConsumer vertexConsumer = source.getBuffer(net.minecraft.client.renderer.RenderType.lines());
        for (MissingEntry e : toRender) {
            BlockPos pos = e.pos;
            net.minecraft.client.renderer.LevelRenderer.renderLineBox(pose, vertexConsumer, pos.getX(), pos.getY(), pos.getZ(), pos.getX() + 1, pos.getY() + 1, pos.getZ() + 1, 1.0f, 1.0f, 1.0f, 0.5f);
        }
        source.endBatch(net.minecraft.client.renderer.RenderType.lines());
        pose.popPose();
    }

    private record MissingEntry(BlockPos pos, BlockState state) {}
}
