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
 *  net.minecraft.client.multiplayer.ClientLevel
 *  net.minecraft.client.renderer.GameRenderer
 *  net.minecraft.core.BlockPos
 *  net.minecraft.world.level.block.state.BlockState
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
import com.friday.cultivation.block.formation.SectProtectionBarrierBlock;
import com.friday.cultivation.client.ClientDomeRegistry;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.joml.Matrix4f;

@Mod.EventBusSubscriber(modid="friday_cultivation", value={Dist.CLIENT})
public final class ClientShieldRippleHandler {
    private static final Map<Long, Long> HIT_CENTERS = new ConcurrentHashMap<Long, Long>();
    private static final long RIPPLE_DURATION_MS = 300L;
    private static final int RIPPLE_RADIUS = 2;

    private ClientShieldRippleHandler() {
    }

    public static void onHit(BlockPos hitPos) {
        HIT_CENTERS.put(hitPos.asLong(), System.currentTimeMillis());
    }

    private static float baseAlphaForDistance(int dist) {
        return switch (dist) {
            case 0 -> 0.5f;
            case 1 -> 0.3f;
            case 2 -> 0.1f;
            default -> 0.0f;
        };
    }

    @SubscribeEvent
    public static void onRenderLevelStage(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_TRANSLUCENT_BLOCKS) {
            return;
        }
        if (HIT_CENTERS.isEmpty()) {
            return;
        }
        Minecraft mc = Minecraft.getInstance();
        ClientLevel level = mc.level;
        if (level == null) {
            HIT_CENTERS.clear();
            return;
        }
        long now = System.currentTimeMillis();
        Vec3 camPos = event.getCamera().getPosition();
        PoseStack pose = event.getPoseStack();
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableCull();
        RenderSystem.depthMask((boolean)false);
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder buffer = tesselator.getBuilder();
        buffer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
        pose.pushPose();
        pose.translate(-camPos.x, -camPos.y, -camPos.z);
        Matrix4f mat = pose.last().pose();
        Iterator<Map.Entry<Long, Long>> it = HIT_CENTERS.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<Long, Long> e = it.next();
            long elapsed = now - e.getValue();
            if (elapsed > 300L) {
                it.remove();
                continue;
            }
            float timeFade = 1.0f - (float)elapsed / 300.0f;
            BlockPos center = BlockPos.of((long)e.getKey());
            for (int dx = -2; dx <= 2; ++dx) {
                for (int dy = -2; dy <= 2; ++dy) {
                    for (int dz = -2; dz <= 2; ++dz) {
                        float alpha;
                        BlockPos p;
                        BlockState st;
                        int dist = Math.abs(dx) + Math.abs(dy) + Math.abs(dz);
                        if (dist > 2 || !ClientShieldRippleHandler.shouldRenderRippleBlock(st = level.getBlockState(p = center.offset(dx, dy, dz)), p) || (alpha = ClientShieldRippleHandler.baseAlphaForDistance(dist) * timeFade) < 0.01f) continue;
                        ClientShieldRippleHandler.addCubeFaces(buffer, mat, p.getX(), p.getY(), p.getZ(), alpha);
                    }
                }
            }
        }
        tesselator.end();
        pose.popPose();
        RenderSystem.enableCull();
        RenderSystem.depthMask((boolean)true);
        RenderSystem.disableBlend();
    }

    private static boolean shouldRenderRippleBlock(BlockState state, BlockPos pos) {
        if (state.getBlock() instanceof SectProtectionBarrierBlock) {
            return true;
        }
        return !state.isAir() && ClientDomeRegistry.isNearShell(pos, 1.25);
    }

    private static void addCubeFaces(BufferBuilder buf, Matrix4f mat, int ix, int iy, int iz, float alpha) {
        float x0 = ix;
        float y0 = iy;
        float z0 = iz;
        float x1 = ix + 1;
        float y1 = iy + 1;
        float z1 = iz + 1;
        float r = 1.0f;
        float g = 1.0f;
        float b = 1.0f;
        float a = alpha;
        buf.vertex(mat, x0, y0, z0).color(r, g, b, a).endVertex();
        buf.vertex(mat, x0, y0, z1).color(r, g, b, a).endVertex();
        buf.vertex(mat, x1, y0, z1).color(r, g, b, a).endVertex();
        buf.vertex(mat, x1, y0, z0).color(r, g, b, a).endVertex();
        buf.vertex(mat, x0, y1, z0).color(r, g, b, a).endVertex();
        buf.vertex(mat, x1, y1, z0).color(r, g, b, a).endVertex();
        buf.vertex(mat, x1, y1, z1).color(r, g, b, a).endVertex();
        buf.vertex(mat, x0, y1, z1).color(r, g, b, a).endVertex();
        buf.vertex(mat, x0, y0, z0).color(r, g, b, a).endVertex();
        buf.vertex(mat, x1, y0, z0).color(r, g, b, a).endVertex();
        buf.vertex(mat, x1, y1, z0).color(r, g, b, a).endVertex();
        buf.vertex(mat, x0, y1, z0).color(r, g, b, a).endVertex();
        buf.vertex(mat, x0, y0, z1).color(r, g, b, a).endVertex();
        buf.vertex(mat, x0, y1, z1).color(r, g, b, a).endVertex();
        buf.vertex(mat, x1, y1, z1).color(r, g, b, a).endVertex();
        buf.vertex(mat, x1, y0, z1).color(r, g, b, a).endVertex();
        buf.vertex(mat, x0, y0, z0).color(r, g, b, a).endVertex();
        buf.vertex(mat, x0, y1, z0).color(r, g, b, a).endVertex();
        buf.vertex(mat, x0, y1, z1).color(r, g, b, a).endVertex();
        buf.vertex(mat, x0, y0, z1).color(r, g, b, a).endVertex();
        buf.vertex(mat, x1, y0, z0).color(r, g, b, a).endVertex();
        buf.vertex(mat, x1, y0, z1).color(r, g, b, a).endVertex();
        buf.vertex(mat, x1, y1, z1).color(r, g, b, a).endVertex();
        buf.vertex(mat, x1, y1, z0).color(r, g, b, a).endVertex();
    }
}

