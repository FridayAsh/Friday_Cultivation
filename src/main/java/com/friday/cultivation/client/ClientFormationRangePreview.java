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
 *  net.minecraft.core.Vec3i
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.phys.Vec3
 *  net.minecraftforge.api.distmarker.Dist
 *  net.minecraftforge.client.event.RenderLevelStageEvent
 *  net.minecraftforge.client.event.RenderLevelStageEvent$Stage
 *  net.minecraftforge.event.TickEvent$ClientTickEvent
 *  net.minecraftforge.event.TickEvent$Phase
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
import com.friday.cultivation.block.formation.FormationCorePlateBlock;
import com.friday.cultivation.cultivation.qi.formation.FormationType;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.joml.Matrix4f;

@Mod.EventBusSubscriber(modid="friday_cultivation", value={Dist.CLIENT})
public final class ClientFormationRangePreview {
    private static final Map<PreviewKey, PreviewSphere> PREVIEWS = new ConcurrentHashMap<PreviewKey, PreviewSphere>();
    private static final double MAX_RENDER_DISTANCE_SQ = 36864.0;
    private static final int SHELL_LATITUDES = 24;
    private static final int SHELL_LONGITUDES = 48;
    private static final float SHELL_ALPHA = 0.2f;
    private static final int CLEANUP_INTERVAL_TICKS = 5;
    private static int cleanupTickCounter = 0;

    private ClientFormationRangePreview() {
    }

    public static void show(BlockPos corePos, BlockPos flagPos, int radius, int typeOrdinal) {
        FormationType[] values = FormationType.values();
        FormationType type = typeOrdinal >= 0 && typeOrdinal < values.length ? values[typeOrdinal] : FormationType.QI_GATHERING;
        PREVIEWS.put(new PreviewKey(corePos, flagPos), new PreviewSphere(corePos.east(), Math.max(1, radius), type.visualColor()));
    }

    public static void hide(BlockPos corePos, BlockPos flagPos) {
        PREVIEWS.remove(new PreviewKey(corePos, flagPos));
    }

    public static boolean isVisible(BlockPos corePos, BlockPos flagPos) {
        return PREVIEWS.containsKey(new PreviewKey(corePos, flagPos));
    }

    public static void updateIfVisible(BlockPos corePos, BlockPos flagPos, int radius, int typeOrdinal) {
        if (ClientFormationRangePreview.isVisible(corePos, flagPos)) {
            ClientFormationRangePreview.show(corePos, flagPos, radius, typeOrdinal);
        }
    }

    public static void retainForCore(BlockPos corePos, Collection<BlockPos> validFlagPositions) {
        if (PREVIEWS.isEmpty()) {
            return;
        }
        BlockPos immutableCore = corePos.east();
        HashSet<BlockPos> validFlags = new HashSet<BlockPos>();
        for (BlockPos flagPos : validFlagPositions) {
            validFlags.add(flagPos.east());
        }
        PREVIEWS.keySet().removeIf(key -> key.corePos().equals((Object)immutableCore) && !validFlags.contains(key.flagPos()));
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }
        if (PREVIEWS.isEmpty()) {
            return;
        }
        Minecraft mc = Minecraft.getInstance();
        ClientLevel level = mc.level;
        if (level == null) {
            PREVIEWS.clear();
            cleanupTickCounter = 0;
            return;
        }
        if (++cleanupTickCounter < 5) {
            return;
        }
        cleanupTickCounter = 0;
        ClientFormationRangePreview.removeMissingCorePreviews((Level)level);
    }

    @SubscribeEvent
    public static void onRenderLevelStage(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_TRANSLUCENT_BLOCKS) {
            return;
        }
        if (PREVIEWS.isEmpty()) {
            return;
        }
        Minecraft mc = Minecraft.getInstance();
        ClientLevel level = mc.level;
        if (level == null) {
            PREVIEWS.clear();
            return;
        }
        ClientFormationRangePreview.removeMissingCorePreviews((Level)level);
        if (PREVIEWS.isEmpty()) {
            return;
        }
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
        for (PreviewSphere preview : PREVIEWS.values()) {
            Vec3 center = Vec3.atCenterOf((Vec3i)preview.center());
            if (center.distanceToSqr(camPos) > 36864.0) continue;
            ClientFormationRangePreview.renderParametricShell(buffer, mat, preview);
        }
        tesselator.end();
        pose.popPose();
        RenderSystem.enableCull();
        RenderSystem.depthMask((boolean)true);
        RenderSystem.disableBlend();
    }

    private static void removeMissingCorePreviews(Level level) {
        PREVIEWS.keySet().removeIf(key -> !ClientFormationRangePreview.hasCorePlate(level, key.corePos()));
    }

    private static boolean hasCorePlate(Level level, BlockPos corePos) {
        return level.isLoaded(corePos) && level.getBlockState(corePos).getBlock() instanceof FormationCorePlateBlock;
    }

    private static void renderParametricShell(BufferBuilder buffer, Matrix4f mat, PreviewSphere preview) {
        double radius = preview.radius();
        Vec3 c = Vec3.atCenterOf((Vec3i)preview.center());
        float red = (float)(preview.color() >> 16 & 0xFF) / 255.0f;
        float green = (float)(preview.color() >> 8 & 0xFF) / 255.0f;
        float blue = (float)(preview.color() & 0xFF) / 255.0f;
        for (int lat = 0; lat < 24; ++lat) {
            double theta0 = Math.PI * (double)lat / 24.0;
            double theta1 = Math.PI * (double)(lat + 1) / 24.0;
            for (int lon = 0; lon < 48; ++lon) {
                double phi0 = Math.PI * 2 * (double)lon / 48.0;
                double phi1 = Math.PI * 2 * (double)(lon + 1) / 48.0;
                ClientFormationRangePreview.addQuad(buffer, mat, ClientFormationRangePreview.spherePoint(c, radius, theta0, phi0), ClientFormationRangePreview.spherePoint(c, radius, theta0, phi1), ClientFormationRangePreview.spherePoint(c, radius, theta1, phi1), ClientFormationRangePreview.spherePoint(c, radius, theta1, phi0), red, green, blue, 0.2f);
            }
        }
    }

    private static Vec3 spherePoint(Vec3 center, double radius, double theta, double phi) {
        double sin = Math.sin(theta);
        return new Vec3(center.x + radius * sin * Math.cos(phi), center.y + radius * Math.cos(theta), center.z + radius * sin * Math.sin(phi));
    }

    private static void addQuad(BufferBuilder buf, Matrix4f mat, Vec3 p0, Vec3 p1, Vec3 p2, Vec3 p3, float r, float g, float b, float a) {
        ClientFormationRangePreview.addVertex(buf, mat, p0, r, g, b, a);
        ClientFormationRangePreview.addVertex(buf, mat, p1, r, g, b, a);
        ClientFormationRangePreview.addVertex(buf, mat, p2, r, g, b, a);
        ClientFormationRangePreview.addVertex(buf, mat, p3, r, g, b, a);
    }

    private static void addVertex(BufferBuilder buf, Matrix4f mat, Vec3 p, float r, float g, float b, float a) {
        buf.vertex(mat, (float)p.x, (float)p.y, (float)p.z).color(r, g, b, a).endVertex();
    }

    private record PreviewKey(BlockPos corePos, BlockPos flagPos) {
        private PreviewKey {
            corePos = corePos.east();
            flagPos = flagPos.east();
        }
    }

    private record PreviewSphere(BlockPos center, int radius, int color) {
        private PreviewSphere(BlockPos center, int radius, int color) {
            this.center = center = center.east();
            this.radius = radius;
            this.color = color;
        }
    }
}

