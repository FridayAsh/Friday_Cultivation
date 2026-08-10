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
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.phys.Vec3
 *  net.minecraftforge.api.distmarker.Dist
 *  net.minecraftforge.client.event.RenderLevelStageEvent
 *  net.minecraftforge.client.event.RenderLevelStageEvent$Stage
 *  net.minecraftforge.event.TickEvent$ClientTickEvent
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
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.joml.Matrix4f;

@Mod.EventBusSubscriber(modid="friday_cultivation", value={Dist.CLIENT})
public final class TribulationCloudClientEffects {
    private static final Map<Integer, ActiveCloud> CLOUDS = new ConcurrentHashMap<Integer, ActiveCloud>();
    private static final long FADE_IN_MS = 900L;
    private static final CloudBlock[] STORM_BLOCKS = new CloudBlock[]{new CloudBlock(-5.35, 0.0, -1.65, 2.2, 0.3, 1.15, 5857644, 0.78f, 0.0), new CloudBlock(-2.85, 0.08, -1.82, 2.55, 0.32, 1.22, 0x444B55, 0.82f, 0.8), new CloudBlock(-0.15, 0.03, -1.72, 2.35, 0.3, 1.1, 6252659, 0.76f, 1.7), new CloudBlock(2.42, 0.11, -1.58, 2.7, 0.34, 1.26, 4080975, 0.84f, 2.5), new CloudBlock(5.18, 0.02, -1.36, 1.95, 0.28, 0.96, 6450038, 0.72f, 3.1), new CloudBlock(-4.18, 0.22, 0.05, 2.05, 0.28, 1.05, 4804698, 0.8f, 3.8), new CloudBlock(-1.7, 0.28, -0.02, 2.85, 0.34, 1.22, 6844796, 0.74f, 4.7), new CloudBlock(1.3, 0.24, 0.1, 2.5, 0.32, 1.18, 5265507, 0.8f, 5.4), new CloudBlock(4.08, 0.3, 0.22, 2.2, 0.3, 1.08, 3883595, 0.83f, 6.3), new CloudBlock(-5.05, 0.05, 1.72, 1.8, 0.26, 0.9, 6976382, 0.7f, 7.0), new CloudBlock(-2.58, 0.15, 1.72, 2.3, 0.3, 1.02, 5594728, 0.78f, 7.9), new CloudBlock(0.12, 0.2, 1.62, 2.75, 0.34, 1.16, 4278353, 0.84f, 8.6), new CloudBlock(3.18, 0.1, 1.78, 2.35, 0.3, 1.06, 6318452, 0.74f, 9.5), new CloudBlock(5.62, 0.17, 1.58, 1.58, 0.26, 0.86, 4870491, 0.78f, 10.4), new CloudBlock(-0.75, 0.54, -0.18, 2.0, 0.26, 0.92, 7634312, 0.54f, 11.2), new CloudBlock(1.75, 0.58, 0.02, 1.85, 0.24, 0.86, 5660521, 0.56f, 12.0)};

    private TribulationCloudClientEffects() {
    }

    public static void onCloud(int entityId, int durationTicks) {
        if (durationTicks <= 0) {
            CLOUDS.remove(entityId);
            return;
        }
        long now = System.currentTimeMillis();
        CLOUDS.put(entityId, new ActiveCloud(now, now + (long)durationTicks * 50L));
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.player == null) {
            CLOUDS.clear();
            return;
        }
        long now = System.currentTimeMillis();
        Iterator<Map.Entry<Integer, ActiveCloud>> it = CLOUDS.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<Integer, ActiveCloud> entry = it.next();
            if (now < entry.getValue().endMs() && mc.level.getEntity(entry.getKey().intValue()) != null) continue;
            it.remove();
        }
    }

    @SubscribeEvent
    public static void onRenderLevelStage(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_TRANSLUCENT_BLOCKS) {
            return;
        }
        if (CLOUDS.isEmpty()) {
            return;
        }
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.player == null) {
            return;
        }
        Vec3 cam = event.getCamera().getPosition();
        PoseStack pose = event.getPoseStack();
        long now = System.currentTimeMillis();
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableCull();
        RenderSystem.depthMask((boolean)false);
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder buffer = tesselator.getBuilder();
        pose.pushPose();
        pose.translate(-cam.x, -cam.y, -cam.z);
        Matrix4f mat = pose.last().pose();
        buffer.begin(VertexFormat.Mode.TRIANGLES, DefaultVertexFormat.POSITION_COLOR);
        for (Map.Entry<Integer, ActiveCloud> entry : CLOUDS.entrySet()) {
            Entity entity = mc.level.getEntity(entry.getKey().intValue());
            if (entity == null || mc.player.distanceToSqr(entity) > 16384.0) continue;
            float fade = TribulationCloudClientEffects.fadeIn(entry.getValue(), now);
            double x = entity.getX();
            double y = entity.getY();
            double z = entity.getZ();
            float age = (float)(now - entry.getValue().startMs()) / 1000.0f;
            TribulationCloudClientEffects.drawStormCloud(buffer, mat, x, y + 7.85, z, fade, age);
        }
        tesselator.end();
        pose.popPose();
        RenderSystem.enableCull();
        RenderSystem.depthMask((boolean)true);
        RenderSystem.disableBlend();
    }

    private static float fadeIn(ActiveCloud cloud, long now) {
        return Math.max(0.12f, Math.min(1.0f, (float)(now - cloud.startMs()) / 900.0f));
    }

    private static void drawStormCloud(BufferBuilder buffer, Matrix4f mat, double x, double y, double z, float fade, float age) {
        for (CloudBlock block : STORM_BLOCKS) {
            double driftX = Math.cos((double)age * 0.18 + block.phase()) * 0.035;
            double driftY = Math.sin((double)age * 0.22 + block.phase() * 1.6) * 0.018;
            double driftZ = Math.sin((double)age * 0.16 + block.phase()) * 0.035;
            double pulse = 1.0 + Math.sin((double)age * 0.22 + block.phase()) * 0.006;
            TribulationCloudClientEffects.drawCloudBlock(buffer, mat, x + block.x() + driftX, y + block.y() + driftY, z + block.z() + driftZ, block.sx() * pulse, block.sy() * (1.0 + Math.cos((double)age * 0.16 + block.phase()) * 0.006), block.sz() * pulse, block.color(), block.alpha() * fade);
        }
    }

    private static void drawCloudBlock(BufferBuilder buffer, Matrix4f mat, double x, double y, double z, double sx, double sy, double sz, int color, float alpha) {
        double minX = x - sx * 0.5;
        double maxX = x + sx * 0.5;
        double minY = y - sy * 0.5;
        double maxY = y + sy * 0.5;
        double minZ = z - sz * 0.5;
        double maxZ = z + sz * 0.5;
        TribulationCloudClientEffects.addQuad(buffer, mat, minX, maxY, minZ, maxX, maxY, minZ, maxX, maxY, maxZ, minX, maxY, maxZ, color, alpha, 1.08);
        TribulationCloudClientEffects.addQuad(buffer, mat, minX, minY, maxZ, maxX, minY, maxZ, maxX, minY, minZ, minX, minY, minZ, color, alpha * 0.82f, 0.74);
        TribulationCloudClientEffects.addQuad(buffer, mat, minX, minY, minZ, minX, maxY, minZ, minX, maxY, maxZ, minX, minY, maxZ, color, alpha * 0.9f, 0.86);
        TribulationCloudClientEffects.addQuad(buffer, mat, maxX, minY, maxZ, maxX, maxY, maxZ, maxX, maxY, minZ, maxX, minY, minZ, color, alpha * 0.9f, 0.84);
        TribulationCloudClientEffects.addQuad(buffer, mat, maxX, minY, minZ, maxX, maxY, minZ, minX, maxY, minZ, minX, minY, minZ, color, alpha * 0.86f, 0.92);
        TribulationCloudClientEffects.addQuad(buffer, mat, minX, minY, maxZ, minX, maxY, maxZ, maxX, maxY, maxZ, maxX, minY, maxZ, color, alpha * 0.86f, 0.82);
    }

    private static void addQuad(BufferBuilder buffer, Matrix4f mat, double ax, double ay, double az, double bx, double by, double bz, double cx, double cy, double cz, double dx, double dy, double dz, int color, float alpha, double shade) {
        int r = Math.max(0, Math.min(255, (int)((double)(color >> 16 & 0xFF) * shade)));
        int g = Math.max(0, Math.min(255, (int)((double)(color >> 8 & 0xFF) * shade)));
        int bl = Math.max(0, Math.min(255, (int)((double)(color & 0xFF) * shade)));
        int al = Math.max(0, Math.min(255, (int)(alpha * 255.0f)));
        TribulationCloudClientEffects.addVertex(buffer, mat, ax, ay, az, r, g, bl, al);
        TribulationCloudClientEffects.addVertex(buffer, mat, bx, by, bz, r, g, bl, al);
        TribulationCloudClientEffects.addVertex(buffer, mat, cx, cy, cz, r, g, bl, al);
        TribulationCloudClientEffects.addVertex(buffer, mat, ax, ay, az, r, g, bl, al);
        TribulationCloudClientEffects.addVertex(buffer, mat, cx, cy, cz, r, g, bl, al);
        TribulationCloudClientEffects.addVertex(buffer, mat, dx, dy, dz, r, g, bl, al);
    }

    private static void addVertex(BufferBuilder buffer, Matrix4f mat, double x, double y, double z, int r, int g, int b, int a) {
        buffer.vertex(mat, (float)x, (float)y, (float)z).color(r, g, b, a).endVertex();
    }

    private record ActiveCloud(long startMs, long endMs) {
    }

    private record CloudBlock(double x, double y, double z, double sx, double sy, double sz, int color, float alpha, double phase) {
    }
}

