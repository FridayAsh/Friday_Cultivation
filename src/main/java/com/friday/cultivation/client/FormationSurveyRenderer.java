/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.systems.RenderSystem
 *  com.mojang.blaze3d.vertex.BufferBuilder
 *  com.mojang.blaze3d.vertex.DefaultVertexFormat
 *  com.mojang.blaze3d.vertex.PoseStack
 *  com.mojang.blaze3d.vertex.Tesselator
 *  com.mojang.blaze3d.vertex.VertexFormat$Mode
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.renderer.GameRenderer
 *  net.minecraft.core.BlockPos
 *  net.minecraft.core.Vec3i
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.item.Item
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.level.block.Block
 *  net.minecraft.world.level.block.entity.BlockEntity
 *  net.minecraft.world.level.chunk.LevelChunk
 *  net.minecraft.world.phys.Vec3
 *  net.minecraftforge.api.distmarker.Dist
 *  net.minecraftforge.client.event.RenderLevelStageEvent
 *  net.minecraftforge.client.event.RenderLevelStageEvent$Stage
 *  net.minecraftforge.eventbus.api.SubscribeEvent
 *  net.minecraftforge.fml.common.Mod$EventBusSubscriber
 *  org.joml.Matrix4f
 */
package com.friday.cultivation.client;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.friday.cultivation.block.formation.FormationCorePlateBlockEntity;
import com.friday.cultivation.block.formation.FormationFlagBlock;
import com.friday.cultivation.cultivation.qi.formation.FormationType;
import com.friday.cultivation.item.FormationCompassItem;
import com.friday.cultivation.registry.ModItems;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.joml.Matrix4f;

@Mod.EventBusSubscriber(modid="friday_cultivation", value={Dist.CLIENT})
public final class FormationSurveyRenderer {
    private static final int MIN_CHUNK_SCAN_RADIUS = 8;

    private FormationSurveyRenderer() {
    }

    @SubscribeEvent
    public static void onRenderLevelStage(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_TRANSLUCENT_BLOCKS) {
            return;
        }
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.player == null || !FormationSurveyRenderer.holdsFormationCompass((Player)mc.player)) {
            return;
        }
        Vec3 cam = event.getCamera().getPosition();
        PoseStack pose = event.getPoseStack();
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableCull();
        RenderSystem.depthMask((boolean)false);
        RenderSystem.lineWidth((float)2.25f);
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder buffer = tesselator.getBuilder();
        buffer.begin(VertexFormat.Mode.LINES, DefaultVertexFormat.POSITION_COLOR);
        pose.pushPose();
        pose.translate(-cam.x, -cam.y, -cam.z);
        Matrix4f mat = pose.last().pose();
        BlockPos playerPos = mc.player.blockPosition();
        BlockPos lockedCorePos = FormationSurveyRenderer.lockedCorePos((Player)mc.player, (Level)mc.level);
        int centerChunkX = playerPos.getX() >> 4;
        int centerChunkZ = playerPos.getZ() >> 4;
        int scanRadius = Math.max(8, (Integer)mc.options.renderDistance().get());
        for (int cx = centerChunkX - scanRadius; cx <= centerChunkX + scanRadius; ++cx) {
            for (int cz = centerChunkZ - scanRadius; cz <= centerChunkZ + scanRadius; ++cz) {
                if (!mc.level.isLoaded(new BlockPos(cx * 16, 0, cz * 16))) continue;
                LevelChunk chunk = mc.level.getChunk(cx, cz);
                for (BlockEntity be : chunk.getBlockEntities().values()) {
                    if (!(be instanceof FormationCorePlateBlockEntity)) continue;
                    FormationCorePlateBlockEntity core = (FormationCorePlateBlockEntity)be;
                    FormationSurveyRenderer.renderCoreSurvey(mc, buffer, mat, core, lockedCorePos);
                }
            }
        }
        tesselator.end();
        pose.popPose();
        RenderSystem.lineWidth((float)1.0f);
        RenderSystem.enableCull();
        RenderSystem.depthMask((boolean)true);
        RenderSystem.disableBlend();
    }

    private static boolean holdsFormationCompass(Player player) {
        ItemStack main = player.getMainHandItem();
        ItemStack off = player.getOffhandItem();
        return main.is((Item)ModItems.FORMATION_COMPASS.get()) || off.is((Item)ModItems.FORMATION_COMPASS.get());
    }

    private static BlockPos lockedCorePos(Player player, Level level) {
        BlockPos mainLocked;
        ItemStack main = player.getMainHandItem();
        BlockPos blockPos = mainLocked = main.is((Item)ModItems.FORMATION_COMPASS.get()) ? FormationCompassItem.getLockedCorePos(main, level) : null;
        if (mainLocked != null) {
            return mainLocked;
        }
        ItemStack off = player.getOffhandItem();
        return off.is((Item)ModItems.FORMATION_COMPASS.get()) ? FormationCompassItem.getLockedCorePos(off, level) : null;
    }

    private static void renderCoreSurvey(Minecraft mc, BufferBuilder buffer, Matrix4f mat, FormationCorePlateBlockEntity core, BlockPos lockedCorePos) {
        List<BlockPos> flags = core.getLinkedFlagPositions();
        BlockPos corePos = core.getBlockPos();
        Vec3 coreCenter = Vec3.atCenterOf((Vec3i)corePos).add(0.0, 0.2, 0.0);
        if (lockedCorePos != null && lockedCorePos.equals((Object)corePos) || core.isCompassLockGlowing(mc.level.getGameTime())) {
            FormationSurveyRenderer.renderLockedCoreHighlight(buffer, mat, coreCenter, 7926222);
        }
        if (flags.isEmpty()) {
            return;
        }
        EnumMap<FormationType, List> flagsByType = new EnumMap<FormationType, List>(FormationType.class);
        for (BlockPos blockPos : flags) {
            Block block = mc.level.getBlockState(blockPos).getBlock();
            if (!(block instanceof FormationFlagBlock)) continue;
            FormationFlagBlock flag = (FormationFlagBlock)block;
            FormationType type = flag.formationType();
            flagsByType.computeIfAbsent(type, ignored -> new ArrayList()).add(blockPos);
            int color = type.visualColor();
            Vec3 flagCenter = Vec3.atCenterOf((Vec3i)blockPos).add(0.0, 0.72, 0.0);
            FormationSurveyRenderer.addBeam(buffer, mat, flagCenter, coreCenter, color);
        }
        for (Map.Entry entry : flagsByType.entrySet()) {
            FormationType type = (FormationType)((Object)entry.getKey());
            int radius = core.getActiveRadius(type) > 0 ? core.getActiveRadius(type) : FormationSurveyRenderer.computeLinkedRadius(corePos, (List)entry.getValue(), type);
            if (radius <= 0) continue;
            FormationSurveyRenderer.renderBoundary(buffer, mat, coreCenter, radius, type.visualColor(), core.hasActiveFormation(type));
        }
    }

    private static int computeLinkedRadius(BlockPos corePos, List<BlockPos> flags, FormationType type) {
        int maxDistSq = 0;
        for (BlockPos flag : flags) {
            maxDistSq = Math.max(maxDistSq, (int)flag.distSqr((Vec3i)corePos));
        }
        int radius = (int)Math.ceil(Math.sqrt(maxDistSq));
        if (type == FormationType.SECT_PROTECTION) {
            ++radius;
        }
        return Math.max(1, radius);
    }

    private static void renderLockedCoreHighlight(BufferBuilder buffer, Matrix4f mat, Vec3 center, int color) {
        double x0 = center.x - 0.62;
        double x1 = center.x + 0.62;
        double y0 = center.y - 0.14;
        double y1 = center.y + 0.36;
        double z0 = center.z - 0.62;
        double z1 = center.z + 0.62;
        Vec3 p000 = new Vec3(x0, y0, z0);
        Vec3 p001 = new Vec3(x0, y0, z1);
        Vec3 p010 = new Vec3(x0, y1, z0);
        Vec3 p011 = new Vec3(x0, y1, z1);
        Vec3 p100 = new Vec3(x1, y0, z0);
        Vec3 p101 = new Vec3(x1, y0, z1);
        Vec3 p110 = new Vec3(x1, y1, z0);
        Vec3 p111 = new Vec3(x1, y1, z1);
        FormationSurveyRenderer.addLine(buffer, mat, p000, p100, color, 0.95f);
        FormationSurveyRenderer.addLine(buffer, mat, p001, p101, color, 0.95f);
        FormationSurveyRenderer.addLine(buffer, mat, p010, p110, color, 0.72f);
        FormationSurveyRenderer.addLine(buffer, mat, p011, p111, color, 0.72f);
        FormationSurveyRenderer.addLine(buffer, mat, p000, p001, color, 0.95f);
        FormationSurveyRenderer.addLine(buffer, mat, p100, p101, color, 0.95f);
        FormationSurveyRenderer.addLine(buffer, mat, p010, p011, color, 0.72f);
        FormationSurveyRenderer.addLine(buffer, mat, p110, p111, color, 0.72f);
        FormationSurveyRenderer.addLine(buffer, mat, p000, p010, color, 0.82f);
        FormationSurveyRenderer.addLine(buffer, mat, p001, p011, color, 0.82f);
        FormationSurveyRenderer.addLine(buffer, mat, p100, p110, color, 0.82f);
        FormationSurveyRenderer.addLine(buffer, mat, p101, p111, color, 0.82f);
    }

    private static void addBeam(BufferBuilder buffer, Matrix4f mat, Vec3 from, Vec3 to, int color) {
        FormationSurveyRenderer.addLine(buffer, mat, from, to, color, 0.92f);
        FormationSurveyRenderer.addLine(buffer, mat, from.add(0.04, 0.0, 0.0), to.add(0.04, 0.0, 0.0), 0xFFFFFF, 0.32f);
        FormationSurveyRenderer.addLine(buffer, mat, from.add(0.0, 0.04, 0.0), to.add(0.0, 0.04, 0.0), color, 0.42f);
    }

    private static void renderBoundary(BufferBuilder buffer, Matrix4f mat, Vec3 center, int radius, int color, boolean active) {
        int segments = Math.max(48, Math.min(192, radius * 6));
        float alpha = active ? 0.46f : 0.25f;
        for (int i = 0; i < segments; ++i) {
            double a0 = Math.PI * 2 * (double)i / (double)segments;
            double a1 = Math.PI * 2 * (double)(i + 1) / (double)segments;
            FormationSurveyRenderer.addCircleSegment(buffer, mat, center, radius, color, alpha, a0, a1, 0);
            FormationSurveyRenderer.addCircleSegment(buffer, mat, center, radius, color, alpha, a0, a1, 1);
            FormationSurveyRenderer.addCircleSegment(buffer, mat, center, radius, color, alpha, a0, a1, 2);
        }
    }

    private static void addCircleSegment(BufferBuilder buffer, Matrix4f mat, Vec3 center, int radius, int color, float alpha, double a0, double a1, int plane) {
        Vec3 p1;
        Vec3 p0;
        double c0 = Math.cos(a0) * (double)radius;
        double s0 = Math.sin(a0) * (double)radius;
        double c1 = Math.cos(a1) * (double)radius;
        double s1 = Math.sin(a1) * (double)radius;
        if (plane == 0) {
            p0 = center.add(c0, 0.0, s0);
            p1 = center.add(c1, 0.0, s1);
        } else if (plane == 1) {
            p0 = center.add(c0, s0, 0.0);
            p1 = center.add(c1, s1, 0.0);
        } else {
            p0 = center.add(0.0, c0, s0);
            p1 = center.add(0.0, c1, s1);
        }
        FormationSurveyRenderer.addLine(buffer, mat, p0, p1, color, alpha);
    }

    private static void addLine(BufferBuilder buffer, Matrix4f mat, Vec3 from, Vec3 to, int rgb, float alpha) {
        float r = (float)(rgb >> 16 & 0xFF) / 255.0f;
        float g = (float)(rgb >> 8 & 0xFF) / 255.0f;
        float b = (float)(rgb & 0xFF) / 255.0f;
        buffer.vertex(mat, (float)from.x, (float)from.y, (float)from.z).color(r, g, b, alpha).endVertex();
        buffer.vertex(mat, (float)to.x, (float)to.y, (float)to.z).color(r, g, b, alpha).endVertex();
    }
}

