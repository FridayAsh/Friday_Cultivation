package com.friday.cultivation.client;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.client.event.RenderLivingEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.joml.Matrix4f;
import org.joml.Quaternionf;

@Mod.EventBusSubscriber(modid = "friday_cultivation", value = Dist.CLIENT)
public final class RealmPressureClientEffects {
    private static final int MODE_TARGET = 0;
    private static final int MODE_CASTER = 1;
    private static final int MODE_EXPANSION = 2;
    private static final int WHITE = 0xFFFFFF;
    private static final int COOL_WHITE = 15398143;
    private static final int RUNE_BLUE = 12572927;
    private static final int RUNE_JADE = 11010024;
    private static final int RUNE_GOLD = 16769954;
    private static final int RUNE_CINNABAR = 16740936;
    private static final int SPHERE_STACKS = 12;
    private static final int SPHERE_SECTORS = 24;
    private static final float[][] SPHERE_VERTICES = RealmPressureClientEffects.buildSphereVertices();
    private static final int SHELL_LATITUDES = 22;
    private static final int SHELL_LONGITUDES = 44;
    private static final double MIN_EXPANSION_RADIUS = 1.25;
    private static final Map<Integer, Long> SUPPRESSED_TARGETS = new ConcurrentHashMap<Integer, Long>();
    private static final Map<Integer, Long> CASTER_HALOS = new ConcurrentHashMap<Integer, Long>();
    private static final Map<Integer, ExpansionVisual> EXPANSIONS = new ConcurrentHashMap<Integer, ExpansionVisual>();
    private static ResourceKey<Level> currentDimension;

    private RealmPressureClientEffects() {
    }

    public static void onSync(int mode, boolean active, int entityId, int casterId, int durationTicks, float radius) {
        if (mode == 0) {
            RealmPressureClientEffects.syncTimed(SUPPRESSED_TARGETS, entityId, durationTicks, active);
        } else if (mode == 1) {
            RealmPressureClientEffects.syncTimed(CASTER_HALOS, entityId, durationTicks, active);
        } else if (mode == 2) {
            if (active) {
                EXPANSIONS.put(casterId, new ExpansionVisual(Math.max(0.0f, radius), System.currentTimeMillis() + (long)Math.max(1, durationTicks) * 50L));
            } else {
                EXPANSIONS.remove(casterId);
            }
        }
    }

    private static void syncTimed(Map<Integer, Long> map, int entityId, int durationTicks, boolean active) {
        if (entityId <= 0) {
            return;
        }
        if (active) {
            map.put(entityId, System.currentTimeMillis() + (long)Math.max(1, durationTicks) * 50L);
        } else {
            map.remove(entityId);
        }
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) {
            RealmPressureClientEffects.clear();
            return;
        }
        ResourceKey<Level> dimension = mc.level.dimension();
        if (currentDimension == null) {
            currentDimension = dimension;
        } else if (!currentDimension.equals(dimension)) {
            RealmPressureClientEffects.clear();
            currentDimension = dimension;
            return;
        }
        if (event.phase != TickEvent.Phase.END) {
            return;
        }
        long now = System.currentTimeMillis();
        SUPPRESSED_TARGETS.entrySet().removeIf(entry -> entry.getValue() <= now);
        CASTER_HALOS.entrySet().removeIf(entry -> entry.getValue() <= now);
        EXPANSIONS.entrySet().removeIf(entry -> entry.getValue().endMs <= now);
    }

    @SubscribeEvent
    public static void onRenderLiving(RenderLivingEvent.Post<?, ?> event) {
        LivingEntity entity = event.getEntity();
        long now = System.currentTimeMillis();
        long suppressEnd = SUPPRESSED_TARGETS.getOrDefault(entity.getId(), 0L);
        long haloEnd = CASTER_HALOS.getOrDefault(entity.getId(), 0L);
        if (suppressEnd <= now && haloEnd <= now) {
            return;
        }
        float age = (float)entity.tickCount + event.getPartialTick();
        PoseStack pose = event.getPoseStack();
        MultiBufferSource buffers = event.getMultiBufferSource();
        if (haloEnd > now) {
            pose.pushPose();
            pose.translate(0.0, (double)entity.getBbHeight() * 0.52, 0.0);
            float radius = entity.getBbHeight() * 0.54f + entity.getBbWidth() * 0.45f + 0.2f;
            float alpha = RealmPressureClientEffects.alpha(haloEnd, 600L) * (0.2f + 0.07f * Mth.sin(age * 0.11f));
            RealmPressureClientEffects.renderHaloSphere(pose, buffers, radius, alpha);
            pose.popPose();
        }
        if (suppressEnd > now) {
            pose.pushPose();
            RealmPressureClientEffects.renderDescendingRunes(pose, buffers, entity, age, RealmPressureClientEffects.alpha(suppressEnd, 900L));
            pose.popPose();
        }
    }

    @SubscribeEvent
    public static void onRenderLevelStage(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_TRANSLUCENT_BLOCKS) {
            return;
        }
        if (EXPANSIONS.isEmpty()) {
            return;
        }
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.player == null) {
            return;
        }
        long now = System.currentTimeMillis();
        Vec3 camera = event.getCamera().getPosition();
        PoseStack pose = event.getPoseStack();
        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder buffer = tesselator.getBuilder();
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableCull();
        RenderSystem.enableDepthTest();
        RenderSystem.depthMask(false);
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        pose.pushPose();
        pose.translate(-camera.x, -camera.y, -camera.z);
        Matrix4f mat = pose.last().pose();
        buffer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
        for (Map.Entry<Integer, ExpansionVisual> entry : EXPANSIONS.entrySet()) {
            ExpansionVisual visual = entry.getValue();
            if (visual.endMs <= now) {
                EXPANSIONS.remove(entry.getKey());
                continue;
            }
            Entity entity = mc.level.getEntity(entry.getKey());
            if (!(entity instanceof LivingEntity) || !((LivingEntity)entity).isAlive() || mc.player.distanceToSqr(entity) > 25600.0) continue;
            LivingEntity caster = (LivingEntity)entity;
            double radius = Math.max(1.25, (double)visual.radius);
            Vec3 center = caster.getEyePosition(event.getPartialTick()).add(0.0, (double)caster.getBbHeight() * 0.55, 0.0);
            float life = RealmPressureClientEffects.alpha(visual.endMs, 250L);
            float pulse = 0.8f + 0.2f * Mth.sin((float)(((float)caster.tickCount + event.getPartialTick()) * 0.17f));
            RealmPressureClientEffects.drawExpansionShell(buffer, mat, center, radius, 0.075f * life * pulse);
        }
        tesselator.end();
        pose.popPose();
        RenderSystem.depthMask(true);
        RenderSystem.enableDepthTest();
        RenderSystem.enableCull();
        RenderSystem.disableBlend();
    }

    private static void renderHaloSphere(PoseStack pose, MultiBufferSource buffers, float radius, float alpha) {
        if (alpha <= 0.0f) {
            return;
        }
        VertexConsumer vc = buffers.getBuffer(RenderType.debugFilledBox());
        Matrix4f mat = pose.last().pose();
        int a = Mth.clamp((int)(alpha * 255.0f), 0, 255);
        for (int i = 0; i < SPHERE_VERTICES.length; i += 4) {
            float[] v0 = SPHERE_VERTICES[i];
            float[] v1 = SPHERE_VERTICES[i + 1];
            float[] v2 = SPHERE_VERTICES[i + 2];
            float[] v3 = SPHERE_VERTICES[i + 3];
            RealmPressureClientEffects.addVertex(vc, mat, v0, radius, 0xFFFFFF, a);
            RealmPressureClientEffects.addVertex(vc, mat, v1, radius, 0xFFFFFF, a);
            RealmPressureClientEffects.addVertex(vc, mat, v2, radius, 0xFFFFFF, a);
            RealmPressureClientEffects.addVertex(vc, mat, v0, radius, 0xFFFFFF, a);
            RealmPressureClientEffects.addVertex(vc, mat, v2, radius, 0xFFFFFF, a);
            RealmPressureClientEffects.addVertex(vc, mat, v3, radius, 0xFFFFFF, a);
        }
    }

    private static void renderDescendingRunes(PoseStack pose, MultiBufferSource buffers, LivingEntity entity, float age, float alpha) {
        if (alpha <= 0.0f) {
            return;
        }
        VertexConsumer vc = buffers.getBuffer(RenderType.debugFilledBox());
        float height = Math.max(0.9f, entity.getBbHeight());
        float radius = Math.max(0.38f, entity.getBbWidth() * 0.72f);
        int baseAlpha = Mth.clamp((int)(alpha * 185.0f), 0, 185);
        for (int strand = 0; strand < 5; ++strand) {
            float strandPhase = (float)strand * ((float)Math.PI * 2) / 5.0f;
            for (int i = 0; i < 6; ++i) {
                float fade;
                float scroll = (age * 0.015f + (float)i * 0.18f + (float)strand * 0.071f) % 1.0f;
                float y = height + 0.34f - scroll * (height + 0.78f);
                float angle = strandPhase + (float)i * 0.82f + age * 0.024f;
                float terminal = y <= 0.16f ? Mth.clamp((0.16f - y) / 0.48f, 0.0f, 1.0f) : 0.0f;
                float terminalEase = RealmPressureClientEffects.smooth(terminal);
                float outward = terminal > 0.0f ? 0.1f + terminalEase * 0.38f : 0.0f;
                float x = Mth.cos(angle) * (radius + outward);
                float z = Mth.sin(angle) * (radius + outward);
                float flicker = 0.72f + 0.28f * Mth.sin(age * 0.21f + (float)i * 1.7f + (float)strand);
                int a = Mth.clamp((int)((float)baseAlpha * flicker * (fade = terminal > 0.0f ? 1.0f - terminalEase * 0.88f : 1.0f)), 0, 220);
                if (a <= 4) continue;
                float glyphWidth = 0.15f + entity.getBbWidth() * 0.06f;
                float glyphHeight = 0.25f + entity.getBbHeight() * 0.032f;
                if (terminal > 0.0f) {
                    RealmPressureClientEffects.drawGroundRuneGlyph(pose, vc, x, Mth.lerp(terminalEase, 0.16f, 0.035f), z, angle + 1.5707964f, glyphWidth * (1.05f + terminalEase * 0.45f), glyphHeight * (0.82f + terminalEase * 0.2f), i + strand, a);
                    continue;
                }
                RealmPressureClientEffects.drawRuneGlyph(pose, vc, x, y, z, -angle + 1.5707964f, glyphWidth, glyphHeight, i + strand, a);
            }
        }
    }

    private static void drawRuneGlyph(PoseStack pose, VertexConsumer vc, float x, float y, float z, float yaw, float width, float height, int variant, int alpha) {
        pose.pushPose();
        pose.translate(x, y, z);
        pose.mulPose(new Quaternionf().rotationY(yaw));
        Matrix4f mat = pose.last().pose();
        RealmPressureClientEffects.drawSealGlyph(vc, mat, width, height, variant, alpha);
        pose.popPose();
    }

    private static void drawGroundRuneGlyph(PoseStack pose, VertexConsumer vc, float x, float y, float z, float yaw, float width, float height, int variant, int alpha) {
        pose.pushPose();
        pose.translate(x, y, z);
        pose.mulPose(new Quaternionf().rotationY(yaw));
        pose.mulPose(new Quaternionf().rotationX(1.5707964f));
        Matrix4f mat = pose.last().pose();
        RealmPressureClientEffects.drawSealGlyph(vc, mat, width, height, variant, alpha);
        pose.popPose();
    }

    private static void drawSealGlyph(VertexConsumer vc, Matrix4f mat, float width, float height, int variant, int alpha) {
        float w = width * 0.5f;
        float h = height * 0.5f;
        float stroke = Math.max(0.01f, Math.min(width, height) * 0.075f);
        int frame = (variant & 1) == 0 ? 16769954 : 15398143;
        int main = (variant & 2) == 0 ? 0xFFFFFF : 11010024;
        int accent = (variant & 3) == 0 ? 16740936 : 12572927;
        int frameAlpha = Mth.clamp((int)((float)alpha * 0.82f), 0, 255);
        RealmPressureClientEffects.stroke(vc, mat, -w * 0.72f, h * 0.48f, w * 0.72f, h * 0.48f, stroke, frame, frameAlpha);
        RealmPressureClientEffects.stroke(vc, mat, -w * 0.72f, -h * 0.48f, w * 0.72f, -h * 0.48f, stroke, frame, frameAlpha);
        RealmPressureClientEffects.stroke(vc, mat, -w * 0.72f, -h * 0.48f, -w * 0.72f, h * 0.48f, stroke, frame, frameAlpha);
        RealmPressureClientEffects.stroke(vc, mat, w * 0.72f, -h * 0.48f, w * 0.72f, h * 0.48f, stroke, frame, frameAlpha);
        RealmPressureClientEffects.stroke(vc, mat, 0.0f, h * 0.38f, w * 0.42f, 0.0f, stroke, main, alpha);
        RealmPressureClientEffects.stroke(vc, mat, w * 0.42f, 0.0f, 0.0f, -h * 0.38f, stroke, main, alpha);
        RealmPressureClientEffects.stroke(vc, mat, 0.0f, -h * 0.38f, -w * 0.42f, 0.0f, stroke, main, alpha);
        RealmPressureClientEffects.stroke(vc, mat, -w * 0.42f, 0.0f, 0.0f, h * 0.38f, stroke, main, alpha);
        RealmPressureClientEffects.rect(vc, mat, -stroke * 0.52f, -h * 0.3f, stroke * 0.52f, h * 0.3f, accent, alpha);
        RealmPressureClientEffects.stroke(vc, mat, -w * 0.32f, h * 0.14f, w * 0.32f, h * 0.14f, stroke, frame, alpha);
        RealmPressureClientEffects.stroke(vc, mat, -w * 0.24f, -h * 0.14f, w * 0.24f, -h * 0.14f, stroke, frame, alpha);
        if ((variant & 1) == 0) {
            RealmPressureClientEffects.stroke(vc, mat, -w * 0.34f, -h * 0.02f, -w * 0.08f, -h * 0.26f, stroke, 12572927, alpha);
            RealmPressureClientEffects.stroke(vc, mat, w * 0.08f, h * 0.26f, w * 0.34f, h * 0.02f, stroke, 12572927, alpha);
        } else {
            RealmPressureClientEffects.stroke(vc, mat, -w * 0.3f, h * 0.28f, -w * 0.05f, h * 0.05f, stroke, 11010024, alpha);
            RealmPressureClientEffects.stroke(vc, mat, w * 0.05f, -h * 0.05f, w * 0.3f, -h * 0.28f, stroke, 11010024, alpha);
        }
        RealmPressureClientEffects.rect(vc, mat, -stroke * 0.95f, -stroke * 0.95f, stroke * 0.95f, stroke * 0.95f, 16740936, Mth.clamp((int)((float)alpha * 0.92f), 0, 255));
    }

    private static void rect(VertexConsumer vc, Matrix4f mat, float minX, float minY, float maxX, float maxY, int color, int alpha) {
        RealmPressureClientEffects.vertex(vc, mat, minX, minY, 0.0f, color, alpha);
        RealmPressureClientEffects.vertex(vc, mat, maxX, minY, 0.0f, color, alpha);
        RealmPressureClientEffects.vertex(vc, mat, maxX, maxY, 0.0f, color, alpha);
        RealmPressureClientEffects.vertex(vc, mat, minX, minY, 0.0f, color, alpha);
        RealmPressureClientEffects.vertex(vc, mat, maxX, maxY, 0.0f, color, alpha);
        RealmPressureClientEffects.vertex(vc, mat, minX, maxY, 0.0f, color, alpha);
    }

    private static void stroke(VertexConsumer vc, Matrix4f mat, float x1, float y1, float x2, float y2, float thickness, int color, int alpha) {
        float dx = x2 - x1;
        float dy = y2 - y1;
        float len = Mth.sqrt(dx * dx + dy * dy);
        if (len <= 1.0E-4f) {
            return;
        }
        float nx = -dy / len * thickness * 0.5f;
        float ny = dx / len * thickness * 0.5f;
        RealmPressureClientEffects.vertex(vc, mat, x1 + nx, y1 + ny, 0.0f, color, alpha);
        RealmPressureClientEffects.vertex(vc, mat, x2 + nx, y2 + ny, 0.0f, color, alpha);
        RealmPressureClientEffects.vertex(vc, mat, x2 - nx, y2 - ny, 0.0f, color, alpha);
        RealmPressureClientEffects.vertex(vc, mat, x1 + nx, y1 + ny, 0.0f, color, alpha);
        RealmPressureClientEffects.vertex(vc, mat, x2 - nx, y2 - ny, 0.0f, color, alpha);
        RealmPressureClientEffects.vertex(vc, mat, x1 - nx, y1 - ny, 0.0f, color, alpha);
    }

    private static void drawExpansionShell(BufferBuilder buffer, Matrix4f mat, Vec3 center, double radius, float alpha) {
        if (alpha <= 0.0f) {
            return;
        }
        for (int lat = 0; lat < 22; ++lat) {
            double theta0 = Math.PI * (double)lat / 22.0;
            double theta1 = Math.PI * (double)(lat + 1) / 22.0;
            for (int lon = 0; lon < 44; ++lon) {
                double phi0 = Math.PI * 2 * (double)lon / 44.0;
                double phi1 = Math.PI * 2 * (double)(lon + 1) / 44.0;
                RealmPressureClientEffects.addQuad(buffer, mat, RealmPressureClientEffects.spherePoint(center, radius, theta0, phi0), RealmPressureClientEffects.spherePoint(center, radius, theta0, phi1), RealmPressureClientEffects.spherePoint(center, radius, theta1, phi1), RealmPressureClientEffects.spherePoint(center, radius, theta1, phi0), 0xFFFFFF, alpha);
            }
        }
    }

    private static void addQuad(BufferBuilder buffer, Matrix4f mat, Vec3 p0, Vec3 p1, Vec3 p2, Vec3 p3, int color, float alpha) {
        RealmPressureClientEffects.addVertex(buffer, mat, p0, color, alpha);
        RealmPressureClientEffects.addVertex(buffer, mat, p1, color, alpha);
        RealmPressureClientEffects.addVertex(buffer, mat, p2, color, alpha);
        RealmPressureClientEffects.addVertex(buffer, mat, p3, color, alpha);
    }

    private static Vec3 spherePoint(Vec3 center, double radius, double theta, double phi) {
        double sin = Math.sin(theta);
        return new Vec3(center.x + radius * sin * Math.cos(phi), center.y + radius * Math.cos(theta), center.z + radius * sin * Math.sin(phi));
    }

    private static void addVertex(VertexConsumer vc, Matrix4f mat, float[] v, float radius, int color, int alpha) {
        RealmPressureClientEffects.vertex(vc, mat, v[0] * radius, v[1] * radius, v[2] * radius, color, alpha);
    }

    private static void vertex(VertexConsumer vc, Matrix4f mat, float x, float y, float z, int color, int alpha) {
        vc.vertex(mat, x, y, z).color(color >> 16 & 0xFF, color >> 8 & 0xFF, color & 0xFF, alpha).endVertex();
    }

    private static void addVertex(BufferBuilder buffer, Matrix4f mat, Vec3 p, int color, float alpha) {
        int a = Mth.clamp((int)(alpha * 255.0f), 0, 255);
        buffer.vertex(mat, (float)p.x, (float)p.y, (float)p.z).color(color >> 16 & 0xFF, color >> 8 & 0xFF, color & 0xFF, a).endVertex();
    }

    private static float[][] buildSphereVertices() {
        int count = 1152;
        float[][] verts = new float[count][3];
        int idx = 0;
        for (int stack = 0; stack < 12; ++stack) {
            float phi1 = (float)((double)stack / 12.0 * Math.PI);
            float phi2 = (float)((double)(stack + 1) / 12.0 * Math.PI);
            float y1 = (float)Math.cos(phi1);
            float y2 = (float)Math.cos(phi2);
            float r1 = (float)Math.sin(phi1);
            float r2 = (float)Math.sin(phi2);
            for (int sector = 0; sector < 24; ++sector) {
                float t1 = (float)((double)sector / 24.0 * Math.PI * 2.0);
                float t2 = (float)((double)(sector + 1) / 24.0 * Math.PI * 2.0);
                float c1 = Mth.cos(t1);
                float s1 = Mth.sin(t1);
                float c2 = Mth.cos(t2);
                float s2 = Mth.sin(t2);
                verts[idx++] = new float[]{r1 * c1, y1, r1 * s1};
                verts[idx++] = new float[]{r2 * c1, y2, r2 * s1};
                verts[idx++] = new float[]{r2 * c2, y2, r2 * s2};
                verts[idx++] = new float[]{r1 * c2, y1, r1 * s2};
            }
        }
        return verts;
    }

    private static float alpha(long endMs, long fadeMs) {
        long remaining = endMs - System.currentTimeMillis();
        if (remaining <= 0L) {
            return 0.0f;
        }
        if (remaining < fadeMs) {
            return Mth.clamp((float)remaining / (float)fadeMs, 0.0f, 1.0f);
        }
        return 1.0f;
    }

    private static float smooth(float value) {
        float t = Mth.clamp(value, 0.0f, 1.0f);
        return t * t * (3.0f - 2.0f * t);
    }

    private static void clear() {
        SUPPRESSED_TARGETS.clear();
        CASTER_HALOS.clear();
        EXPANSIONS.clear();
        currentDimension = null;
    }

    private static final class ExpansionVisual {
        final float radius;
        final long endMs;

        ExpansionVisual(float radius, long endMs) {
            this.radius = radius;
            this.endMs = endMs;
        }
    }
}
