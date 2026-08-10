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
 *  net.minecraft.client.gui.Font
 *  net.minecraft.client.gui.GuiGraphics
 *  net.minecraft.client.renderer.GameRenderer
 *  net.minecraft.core.BlockPos
 *  net.minecraft.core.Vec3i
 *  net.minecraft.network.chat.Component
 *  net.minecraft.network.chat.FormattedText
 *  net.minecraft.network.chat.MutableComponent
 *  net.minecraft.util.Mth
 *  net.minecraft.world.phys.AABB
 *  net.minecraft.world.phys.Vec3
 *  net.minecraftforge.api.distmarker.Dist
 *  net.minecraftforge.client.event.RenderLevelStageEvent
 *  net.minecraftforge.client.event.RenderLevelStageEvent$Stage
 *  net.minecraftforge.client.gui.overlay.IGuiOverlay
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
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.client.gui.overlay.IGuiOverlay;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.joml.Matrix4f;

@Mod.EventBusSubscriber(modid="friday_cultivation", value={Dist.CLIENT})
public final class DivineSenseClientEffects {
    private static final int SHELL_COLOR = 0xFFFFFF;
    private static final int BLOCK_COLOR = 12885503;
    private static final long POST_EXPANSION_FADE_MS = 600L;
    private static final double MIN_RENDER_RADIUS = 2.0;
    private static final int SHELL_LATITUDES = 32;
    private static final int SHELL_LONGITUDES = 64;
    private static final double SHELL_THICKNESS = 1.0;
    private static final float SHELL_OUTER_ALPHA = 0.15f;
    private static final float SHELL_INNER_ALPHA = 0.09f;
    private static final int WAVE_RING_SEGMENTS = 128;
    private static final List<ActiveScan> ACTIVE_SCANS = new ArrayList<ActiveScan>();
    private static final Map<BlockPos, Long> REVEALED_BLOCKS = new ConcurrentHashMap<BlockPos, Long>();
    private static long countdownEndMs = 0L;
    public static final IGuiOverlay COUNTDOWN_OVERLAY = (gui, graphics, partialTick, screenWidth, screenHeight) -> DivineSenseClientEffects.renderCountdown(graphics, screenWidth, screenHeight);

    private DivineSenseClientEffects() {
    }

    public static void onScan(double x, double y, double z, double radius, int expansionTicks, int glowTicks, List<Integer> entityIds, List<BlockPos> blockPositions) {
        long now = System.currentTimeMillis();
        ACTIVE_SCANS.clear();
        ACTIVE_SCANS.add(new ActiveScan(new Vec3(x, y, z), radius, now, now + (long)Math.max(1, expansionTicks) * 50L, (long)Math.max(1, glowTicks) * 50L, Math.max(1, glowTicks), new ArrayList<BlockPos>(blockPositions)));
        countdownEndMs = now + (long)Math.max(1, glowTicks) * 50L;
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.player == null) {
            ACTIVE_SCANS.clear();
            REVEALED_BLOCKS.clear();
            return;
        }
        long now = System.currentTimeMillis();
        REVEALED_BLOCKS.entrySet().removeIf(entry -> now >= (Long)entry.getValue());
        Iterator<ActiveScan> scanIt = ACTIVE_SCANS.iterator();
        while (scanIt.hasNext()) {
            ActiveScan scan = scanIt.next();
            double radius = scan.currentRadius(now);
            long revealUntil = now + scan.glowMs;
            Iterator<BlockPos> blockIt = scan.pendingBlockPositions.iterator();
            while (blockIt.hasNext()) {
                BlockPos pos = blockIt.next();
                if (!(Vec3.atCenterOf((Vec3i)pos).distanceTo(scan.center) <= radius)) continue;
                REVEALED_BLOCKS.put(pos.east(), revealUntil);
                blockIt.remove();
            }
            if (now <= scan.endMs + 600L || !scan.pendingBlockPositions.isEmpty()) continue;
            scanIt.remove();
        }
    }

    @SubscribeEvent
    public static void onRenderLevelStage(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_TRANSLUCENT_BLOCKS) {
            return;
        }
        if (ACTIVE_SCANS.isEmpty() && REVEALED_BLOCKS.isEmpty()) {
            return;
        }
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.player == null) {
            return;
        }
        long now = System.currentTimeMillis();
        Vec3 cam = event.getCamera().getPosition();
        PoseStack pose = event.getPoseStack();
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableCull();
        RenderSystem.enableDepthTest();
        RenderSystem.depthMask((boolean)false);
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder buffer = tesselator.getBuilder();
        pose.pushPose();
        pose.translate(-cam.x, -cam.y, -cam.z);
        Matrix4f mat = pose.last().pose();
        if (!ACTIVE_SCANS.isEmpty()) {
            buffer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
            for (ActiveScan scan : ACTIVE_SCANS) {
                double progress = scan.progress(now);
                if (progress > 1.0 && now > scan.endMs + 600L) continue;
                double radius = scan.radius * Math.min(1.0, progress);
                float fade = now <= scan.endMs ? 1.0f : 1.0f - Mth.clamp((float)((float)(now - scan.endMs) / 600.0f), (float)0.0f, (float)1.0f);
                DivineSenseClientEffects.drawMembraneSurface(buffer, mat, scan.center, radius, fade, now);
            }
            tesselator.end();
        }
        if (!REVEALED_BLOCKS.isEmpty()) {
            RenderSystem.lineWidth((float)2.2f);
            buffer.begin(VertexFormat.Mode.LINES, DefaultVertexFormat.POSITION_COLOR);
            for (BlockPos pos : REVEALED_BLOCKS.keySet()) {
                AABB box = new AABB(pos).inflate(0.04);
                DivineSenseClientEffects.drawBox(buffer, mat, box, 12885503, 0.85f);
            }
            tesselator.end();
            RenderSystem.lineWidth((float)1.0f);
        }
        pose.popPose();
        RenderSystem.depthMask((boolean)true);
        RenderSystem.enableDepthTest();
        RenderSystem.enableCull();
        RenderSystem.disableBlend();
    }

    private static void drawMembraneSurface(BufferBuilder buffer, Matrix4f mat, Vec3 center, double radius, float fade, long now) {
        if (radius < 2.0 || fade <= 0.0f) {
            return;
        }
        float pulse = DivineSenseClientEffects.pulseFactor(now);
        DivineSenseClientEffects.drawThickShellSurface(buffer, mat, center, radius, DivineSenseClientEffects.innerShellRadius(radius), 0.15f * fade * pulse, 0.09f * fade * pulse);
    }

    private static double innerShellRadius(double radius) {
        return Math.max(0.65, radius - 1.0);
    }

    private static void drawThickShellSurface(BufferBuilder buffer, Matrix4f mat, Vec3 center, double outerRadius, double innerRadius, float outerAlpha, float innerAlpha) {
        if (outerAlpha <= 0.0f && innerAlpha <= 0.0f) {
            return;
        }
        for (int lat = 0; lat < 32; ++lat) {
            double theta0 = Math.PI * (double)lat / 32.0;
            double theta1 = Math.PI * (double)(lat + 1) / 32.0;
            for (int lon = 0; lon < 64; ++lon) {
                double phi0 = Math.PI * 2 * (double)lon / 64.0;
                double phi1 = Math.PI * 2 * (double)(lon + 1) / 64.0;
                if (outerAlpha > 0.0f) {
                    DivineSenseClientEffects.addQuad(buffer, mat, DivineSenseClientEffects.spherePoint(center, outerRadius, theta0, phi0), DivineSenseClientEffects.spherePoint(center, outerRadius, theta0, phi1), DivineSenseClientEffects.spherePoint(center, outerRadius, theta1, phi1), DivineSenseClientEffects.spherePoint(center, outerRadius, theta1, phi0), 0xFFFFFF, outerAlpha);
                }
                if (!(innerAlpha > 0.0f)) continue;
                DivineSenseClientEffects.addQuad(buffer, mat, DivineSenseClientEffects.spherePoint(center, innerRadius, theta0, phi1), DivineSenseClientEffects.spherePoint(center, innerRadius, theta0, phi0), DivineSenseClientEffects.spherePoint(center, innerRadius, theta1, phi0), DivineSenseClientEffects.spherePoint(center, innerRadius, theta1, phi1), 0xFFFFFF, innerAlpha);
            }
        }
    }

    private static void drawThickShellGrid(BufferBuilder buffer, Matrix4f mat, Vec3 center, double outerRadius, double innerRadius, float alpha) {
        int lon;
        double theta;
        int lat;
        if (alpha <= 0.0f) {
            return;
        }
        for (lat = 2; lat < 32; lat += 3) {
            theta = Math.PI * (double)lat / 32.0;
            for (lon = 0; lon < 64; ++lon) {
                double phi0 = Math.PI * 2 * (double)lon / 64.0;
                double phi1 = Math.PI * 2 * (double)(lon + 1) / 64.0;
                DivineSenseClientEffects.addSegment(buffer, mat, DivineSenseClientEffects.spherePoint(center, outerRadius, theta, phi0), DivineSenseClientEffects.spherePoint(center, outerRadius, theta, phi1), 0xFFFFFF, alpha);
                DivineSenseClientEffects.addSegment(buffer, mat, DivineSenseClientEffects.spherePoint(center, innerRadius, theta, phi0), DivineSenseClientEffects.spherePoint(center, innerRadius, theta, phi1), 0xFFFFFF, alpha * 0.48f);
            }
        }
        for (int lon2 = 0; lon2 < 64; lon2 += 5) {
            double phi = Math.PI * 2 * (double)lon2 / 64.0;
            for (int lat2 = 1; lat2 < 32; ++lat2) {
                double theta0 = Math.PI * (double)lat2 / 32.0;
                double theta1 = Math.PI * (double)(lat2 + 1) / 32.0;
                DivineSenseClientEffects.addSegment(buffer, mat, DivineSenseClientEffects.spherePoint(center, outerRadius, theta0, phi), DivineSenseClientEffects.spherePoint(center, outerRadius, theta1, phi), 0xFFFFFF, alpha * 0.82f);
                DivineSenseClientEffects.addSegment(buffer, mat, DivineSenseClientEffects.spherePoint(center, innerRadius, theta0, phi), DivineSenseClientEffects.spherePoint(center, innerRadius, theta1, phi), 0xFFFFFF, alpha * 0.36f);
            }
        }
        for (lat = 3; lat < 30; lat += 5) {
            theta = Math.PI * (double)lat / 32.0;
            for (lon = 0; lon < 64; lon += 5) {
                double phi = Math.PI * 2 * (double)lon / 64.0;
                DivineSenseClientEffects.addSegment(buffer, mat, DivineSenseClientEffects.spherePoint(center, innerRadius, theta, phi), DivineSenseClientEffects.spherePoint(center, outerRadius, theta, phi), 0xFFFFFF, alpha * 0.55f);
            }
        }
    }

    private static void addSegment(BufferBuilder buffer, Matrix4f mat, Vec3 p0, Vec3 p1, int color, float alpha) {
        DivineSenseClientEffects.addVertex(buffer, mat, p0, color, alpha);
        DivineSenseClientEffects.addVertex(buffer, mat, p1, color, alpha);
    }

    private static void drawWaveFrontRings(BufferBuilder buffer, Matrix4f mat, Vec3 center, double radius, float alpha) {
        if (radius < 2.0 || alpha <= 0.0f) {
            return;
        }
        DivineSenseClientEffects.addCircle(buffer, mat, center, radius, 0, alpha);
        DivineSenseClientEffects.addCircle(buffer, mat, center, radius, 1, alpha);
        DivineSenseClientEffects.addCircle(buffer, mat, center, radius, 2, alpha);
    }

    private static void addCircle(BufferBuilder buffer, Matrix4f mat, Vec3 center, double radius, int plane, float alpha) {
        for (int i = 0; i < 128; ++i) {
            double a0 = Math.PI * 2 * (double)i / 128.0;
            double a1 = Math.PI * 2 * (double)(i + 1) / 128.0;
            DivineSenseClientEffects.addVertex(buffer, mat, DivineSenseClientEffects.circlePoint(center, radius, plane, a0), 0xFFFFFF, alpha);
            DivineSenseClientEffects.addVertex(buffer, mat, DivineSenseClientEffects.circlePoint(center, radius, plane, a1), 0xFFFFFF, alpha);
        }
    }

    private static Vec3 circlePoint(Vec3 center, double radius, int plane, double angle) {
        double c = Math.cos(angle) * radius;
        double s = Math.sin(angle) * radius;
        return switch (plane) {
            case 1 -> center.add(c, s, 0.0);
            case 2 -> center.add(0.0, c, s);
            default -> center.add(c, 0.0, s);
        };
    }

    private static float pulseFactor(long now) {
        return 1.0f + 0.08f * (float)Math.sin((double)now * 0.003);
    }

    private static void renderCountdown(GuiGraphics gfx, int screenWidth, int screenHeight) {
        long now = System.currentTimeMillis();
        if (now >= countdownEndMs) {
            return;
        }
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.options.hideGui) {
            return;
        }
        long remainMs = countdownEndMs - now;
        long totalMs = 30000L;
        float progress = Mth.clamp((float)((float)remainMs / (float)totalMs), (float)0.0f, (float)1.0f);
        int remainSec = (int)Math.ceil((double)remainMs / 1000.0);
        int barW = 120;
        int barH = 4;
        int barX = (screenWidth - 120) / 2;
        int barY = 21;
        Font font = mc.font;
        MutableComponent label = Component.translatable((String)"hud.friday_cultivation.divine_sense.countdown", (Object[])new Object[]{String.valueOf(remainSec)});
        int textColor = -2569985;
        int labelW = font.width((FormattedText)label);
        gfx.drawString(font, (Component)label, (screenWidth - labelW) / 2, 10, textColor, true);
        int INK = -15067628;
        int TRACK = -869653472;
        int FILL = -6125344;
        gfx.fill(barX - 1, barY - 1, barX + 120 + 1, barY + 4 + 1, -15067628);
        gfx.fill(barX, barY, barX + 120, barY + 4, -869653472);
        int filled = (int)(120.0f * progress);
        if (filled > 0) {
            gfx.fill(barX, barY, barX + filled, barY + 4, -6125344);
        }
    }

    private static void addQuad(BufferBuilder buffer, Matrix4f mat, Vec3 p0, Vec3 p1, Vec3 p2, Vec3 p3, int color, float alpha) {
        DivineSenseClientEffects.addVertex(buffer, mat, p0, color, alpha);
        DivineSenseClientEffects.addVertex(buffer, mat, p1, color, alpha);
        DivineSenseClientEffects.addVertex(buffer, mat, p2, color, alpha);
        DivineSenseClientEffects.addVertex(buffer, mat, p3, color, alpha);
    }

    private static Vec3 spherePoint(Vec3 center, double radius, double theta, double phi) {
        double sin = Math.sin(theta);
        return new Vec3(center.x + radius * sin * Math.cos(phi), center.y + radius * Math.cos(theta), center.z + radius * sin * Math.sin(phi));
    }

    private static void drawBox(BufferBuilder buffer, Matrix4f mat, AABB box, int color, float alpha) {
        DivineSenseClientEffects.addLine(buffer, mat, box.minX, box.minY, box.minZ, box.maxX, box.minY, box.minZ, color, alpha);
        DivineSenseClientEffects.addLine(buffer, mat, box.minX, box.minY, box.maxZ, box.maxX, box.minY, box.maxZ, color, alpha);
        DivineSenseClientEffects.addLine(buffer, mat, box.minX, box.maxY, box.minZ, box.maxX, box.maxY, box.minZ, color, alpha);
        DivineSenseClientEffects.addLine(buffer, mat, box.minX, box.maxY, box.maxZ, box.maxX, box.maxY, box.maxZ, color, alpha);
        DivineSenseClientEffects.addLine(buffer, mat, box.minX, box.minY, box.minZ, box.minX, box.minY, box.maxZ, color, alpha);
        DivineSenseClientEffects.addLine(buffer, mat, box.maxX, box.minY, box.minZ, box.maxX, box.minY, box.maxZ, color, alpha);
        DivineSenseClientEffects.addLine(buffer, mat, box.minX, box.maxY, box.minZ, box.minX, box.maxY, box.maxZ, color, alpha);
        DivineSenseClientEffects.addLine(buffer, mat, box.maxX, box.maxY, box.minZ, box.maxX, box.maxY, box.maxZ, color, alpha);
        DivineSenseClientEffects.addLine(buffer, mat, box.minX, box.minY, box.minZ, box.minX, box.maxY, box.minZ, color, alpha);
        DivineSenseClientEffects.addLine(buffer, mat, box.maxX, box.minY, box.minZ, box.maxX, box.maxY, box.minZ, color, alpha);
        DivineSenseClientEffects.addLine(buffer, mat, box.minX, box.minY, box.maxZ, box.minX, box.maxY, box.maxZ, color, alpha);
        DivineSenseClientEffects.addLine(buffer, mat, box.maxX, box.minY, box.maxZ, box.maxX, box.maxY, box.maxZ, color, alpha);
    }

    private static void addLine(BufferBuilder buffer, Matrix4f mat, double x1, double y1, double z1, double x2, double y2, double z2, int color, float alpha) {
        DivineSenseClientEffects.addVertex(buffer, mat, new Vec3(x1, y1, z1), color, alpha);
        DivineSenseClientEffects.addVertex(buffer, mat, new Vec3(x2, y2, z2), color, alpha);
    }

    private static void addVertex(BufferBuilder buffer, Matrix4f mat, Vec3 pos, int color, float alpha) {
        int r = color >> 16 & 0xFF;
        int g = color >> 8 & 0xFF;
        int b = color & 0xFF;
        int a = Math.max(0, Math.min(255, (int)(alpha * 255.0f)));
        buffer.vertex(mat, (float)pos.x, (float)pos.y, (float)pos.z).color(r, g, b, a).endVertex();
    }

    private static final class ActiveScan {
        private final Vec3 center;
        private final double radius;
        private final long startMs;
        private final long endMs;
        private final long glowMs;
        private final int glowTicks;
        private final List<BlockPos> pendingBlockPositions;

        private ActiveScan(Vec3 center, double radius, long startMs, long endMs, long glowMs, int glowTicks, List<BlockPos> pendingBlockPositions) {
            this.center = center;
            this.radius = radius;
            this.startMs = startMs;
            this.endMs = endMs;
            this.glowMs = glowMs;
            this.glowTicks = glowTicks;
            this.pendingBlockPositions = pendingBlockPositions;
        }

        private double progress(long now) {
            return Mth.clamp((double)((double)(now - this.startMs) / (double)Math.max(1L, this.endMs - this.startMs)), (double)0.0, (double)1.0);
        }

        private double currentRadius(long now) {
            return this.radius * this.progress(now);
        }
    }
}

