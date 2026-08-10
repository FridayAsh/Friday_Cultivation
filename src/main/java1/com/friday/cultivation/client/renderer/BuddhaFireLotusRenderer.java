package com.friday.cultivation.client.renderer;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import com.friday.cultivation.entity.spell.BuddhaFireLotusEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
public class BuddhaFireLotusRenderer
extends EntityRenderer<BuddhaFireLotusEntity> {
    private static final ResourceLocation WHITE = new ResourceLocation("friday_cultivation", "textures/entity/buddha_fire_lotus.png");
    public BuddhaFireLotusRenderer(EntityRendererProvider.Context ctx) {
        super(ctx);
        this.shadowRadius = 0.0f;
    }
    public void render(@NotNull BuddhaFireLotusEntity entity, float yaw, float partialTick, @NotNull PoseStack pose, @NotNull MultiBufferSource buffers, int packedLight) {
        float age = (float)entity.tickCount + partialTick;
        float scale = entity.visualScale();
        BuddhaFireLotusRenderer.renderFlowTrail(pose, buffers, entity.getDeltaMovement(), age, scale, 1.0f);
        BuddhaFireLotusRenderer.renderLotus(pose, buffers, entity.rootFlags(), entity.chargedQi(), age, scale, 1.0f, age * 10.0f, 1.0f);
        super.render(entity, yaw, partialTick, pose, buffers, packedLight);
    }
    public static void renderLotus(PoseStack pose, MultiBufferSource buffers, int rootFlags, int chargedQi, float ageTicks, float scale, float alphaMultiplier) {
        BuddhaFireLotusRenderer.renderLotus(pose, buffers, rootFlags, chargedQi, ageTicks, scale, alphaMultiplier, ageTicks * 0.85f, 1.0f);
    }
    public static void renderLotus(PoseStack pose, MultiBufferSource buffers, int rootFlags, int chargedQi, float ageTicks, float scale, float alphaMultiplier, float rotationDegrees, float bloomProgress) {
        RenderSystem.enableBlend();
        pose.pushPose();
        pose.scale(scale, scale, scale);
        pose.mulPose(Axis.YP.rotationDegrees(rotationDegrees));
        VertexConsumer vc = buffers.getBuffer(RenderType.entityCutoutNoCull((ResourceLocation)WHITE));
        Matrix4f matrix = pose.last().pose();
        Matrix3f normal = pose.last().normal();
        int alpha = (int)(Mth.clamp((float)alphaMultiplier, (float)0.0f, (float)1.0f) * 235.0f);
        float bloom = Mth.clamp((float)bloomProgress, (float)0.0f, (float)1.0f);
        float charge = Mth.clamp((float)((float)chargedQi / 10000.0f), (float)0.15f, (float)4.0f);
        int deepBlue = BuddhaFireLotusRenderer.deepen(1941503, charge * 0.88f);
        int cyan = BuddhaFireLotusRenderer.deepen(4257279, charge);
        int paleCyan = BuddhaFireLotusRenderer.deepen(0xB7FFFF, charge * 0.74f);
        int violet = BuddhaFireLotusRenderer.deepen(15303423, charge * 0.72f);
        int white = BuddhaFireLotusRenderer.deepen(0xF4FEFF, charge * 0.66f);
        int accent = BuddhaFireLotusRenderer.colorForFlags(rootFlags, cyan, paleCyan, violet);
        BuddhaFireLotusRenderer.emitCrystalPetalLayer(vc, matrix, normal, 12, 0.3f, 0.86f, 1.32f, 0.48f, -0.24f, -0.1f, 0.02f, deepBlue, alpha - 38, 10.0f, bloom);
        BuddhaFireLotusRenderer.emitCrystalPetalLayer(vc, matrix, normal, 10, 0.2f, 0.7f, 1.08f, 0.4f, -0.06f, 0.12f, 0.34f, accent, alpha - 24, 28.0f, bloom);
        BuddhaFireLotusRenderer.emitCrystalPetalLayer(vc, matrix, normal, 8, 0.1f, 0.52f, 0.82f, 0.3f, 0.12f, 0.34f, 0.68f, cyan, alpha - 8, 7.0f, bloom);
        BuddhaFireLotusRenderer.emitCrystalPetalLayer(vc, matrix, normal, 6, 0.04f, 0.34f, 0.58f, 0.22f, 0.32f, 0.6f, 0.98f, paleCyan, alpha, 35.0f, bloom);
        BuddhaFireLotusRenderer.emitCrystalPetalLayer(vc, matrix, normal, 5, 0.02f, 0.22f, 0.36f, 0.14f, 0.52f, 0.88f, 1.18f, white, Math.min(255, alpha + 8), 0.0f, bloom);
        BuddhaFireLotusRenderer.emitLotusBowl(vc, matrix, normal, 10, 0.36f, 0.58f, 0.18f, 0.48f, accent, (int)((float)(alpha - 42) * Mth.lerp((float)bloom, (float)0.22f, (float)1.0f)));
        BuddhaFireLotusRenderer.emitCore(vc, matrix, normal, cyan, violet, white, Math.min(255, alpha + 20), ageTicks);
        pose.popPose();
    }
    public static void renderFlowTrail(PoseStack pose, MultiBufferSource buffers, Vec3 motion, float ageTicks, float scale, float alphaMultiplier) {
        if (motion.lengthSqr() < 1.0E-5) {
            return;
        }
        RenderSystem.enableBlend();
        VertexConsumer vc = buffers.getBuffer(RenderType.entityCutoutNoCull((ResourceLocation)WHITE));
        Matrix4f matrix = pose.last().pose();
        Matrix3f normal = pose.last().normal();
        Vec3 forward = motion.normalize();
        Vec3 referenceUp = Math.abs(forward.dot(new Vec3(0.0, 1.0, 0.0))) > 0.94 ? new Vec3(1.0, 0.0, 0.0) : new Vec3(0.0, 1.0, 0.0);
        Vec3 right = forward.cross(referenceUp).normalize();
        Vec3 up = right.cross(forward).normalize();
        int alpha = (int)(Mth.clamp((float)alphaMultiplier, (float)0.0f, (float)1.0f) * 220.0f);
        float length = 4.85f * Mth.clamp((float)scale, (float)0.8f, (float)1.8f);
        float width = 0.26f * Mth.clamp((float)scale, (float)0.8f, (float)1.8f);
        BuddhaFireLotusRenderer.emitTrailRibbon(vc, matrix, normal, forward, right, up, ageTicks, -width * 0.85f, width, length, 4257279, alpha);
        BuddhaFireLotusRenderer.emitTrailRibbon(vc, matrix, normal, forward, right, up, ageTicks + 11.0f, width * 0.85f, width * 0.82f, length * 0.86f, 0xF4FEFF, (int)((float)alpha * 0.82f));
    }
    public static void renderLotusLightning(PoseStack pose, MultiBufferSource buffers, float radius, float progress, float ageTicks, float alphaMultiplier) {
        RenderSystem.enableBlend();
        VertexConsumer vc = buffers.getBuffer(RenderType.entityCutoutNoCull((ResourceLocation)WHITE));
        Matrix4f matrix = pose.last().pose();
        Matrix3f normal = pose.last().normal();
        float open = Mth.clamp((float)(progress / 0.16f), (float)0.0f, (float)1.0f);
        float visible = Mth.clamp((float)alphaMultiplier, (float)0.0f, (float)1.0f) * open;
        int alpha = (int)(visible * 245.0f);
        if (alpha <= 3) {
            return;
        }
        float outerOrbitRadius = Math.min(radius * 0.86f, 66.0f);
        float innerOrbitRadius = Math.max(outerOrbitRadius * 0.78f, outerOrbitRadius - 12.0f);
        float verticalSpan = Math.min(radius * 0.18f, 14.0f);
        int arcs = 9;
        for (int i = 0; i < arcs; ++i) {
            float pulse;
            float localAge = ageTicks + (float)i * 6.25f;
            float pulsePeriod = 39.0f;
            float activeTicks = 8.0f;
            float pulseTick = BuddhaFireLotusRenderer.positiveModulo(localAge, pulsePeriod);
            if (pulseTick > activeTicks) continue;
            float f = pulse = pulseTick < 1.8f ? 1.0f : Mth.clamp((float)(1.0f - (pulseTick - 1.8f) / (activeTicks - 1.8f)), (float)0.0f, (float)1.0f);
            if (pulse <= 0.035f) continue;
            int seed = i * 734287 + Mth.floor((float)(localAge / pulsePeriod)) * 19349663;
            int color = switch (i % 3) {
                case 0 -> 16727975;
                case 1 -> 4257279;
                default -> 0xF4FEFF;
            };
            float direction = (i & 1) == 0 ? 1.0f : -1.0f;
            float base = (float)(Math.PI * 2 * (double)i / (double)arcs + (double)(direction * BuddhaFireLotusRenderer.signedHash(seed + 11) * 0.28f));
            float ringRadius = Mth.lerp((float)BuddhaFireLotusRenderer.hash01(seed + 23), (float)innerOrbitRadius, (float)outerOrbitRadius);
            float sweep = direction * (0.36f + BuddhaFireLotusRenderer.hash01(seed + 31) * 0.28f);
            float baseY = 2.8f + verticalSpan * 0.34f + BuddhaFireLotusRenderer.signedHash(seed + 41) * verticalSpan * 0.54f;
            float[] previous = BuddhaFireLotusRenderer.lightningPoint(base, ringRadius, baseY, seed, 0, outerOrbitRadius * 0.03f, verticalSpan * 0.22f);
            int segments = 7;
            int arcAlpha = (int)((float)alpha * pulse);
            for (int s = 1; s <= segments; ++s) {
                float t = (float)s / (float)segments;
                float zigzag = ((s & 1) == 0 ? 1.0f : -1.0f) * (0.03f + BuddhaFireLotusRenderer.hash01(seed + s * 17) * 0.034f);
                float r = ringRadius + zigzag * outerOrbitRadius + BuddhaFireLotusRenderer.signedHash(seed + s * 43) * outerOrbitRadius * 0.025f;
                float angle = base + sweep * t + BuddhaFireLotusRenderer.signedHash(seed + s * 59) * 0.09f;
                float yStep = baseY + BuddhaFireLotusRenderer.signedHash(seed + s * 71) * verticalSpan * 0.26f + ((s & 1) == 0 ? 0.08f : -0.08f) * verticalSpan;
                float[] next = BuddhaFireLotusRenderer.lightningPoint(angle, r, yStep, seed, s, outerOrbitRadius * 0.016f, verticalSpan * 0.1f);
                float taper = 1.0f - t * 0.18f;
                BuddhaFireLotusRenderer.emitLightningSegment(vc, matrix, normal, previous, next, 0.13f, color, (int)((float)arcAlpha * 0.3f * taper));
                BuddhaFireLotusRenderer.emitLightningSegment(vc, matrix, normal, previous, next, 0.04f, 0xF8FFFF, (int)((float)arcAlpha * 0.92f * taper));
                if (s == 2 || s == 5 && BuddhaFireLotusRenderer.hash01(seed + 503) > 0.42f) {
                    float branchDir = direction * (BuddhaFireLotusRenderer.hash01(seed + s * 101) > 0.5f ? 1.0f : -1.0f);
                    float branchAngle = angle + branchDir * (0.3f + BuddhaFireLotusRenderer.hash01(seed + s * 113) * 0.22f);
                    float branchRadius = r + outerOrbitRadius * (0.055f + BuddhaFireLotusRenderer.hash01(seed + s * 127) * 0.065f);
                    float branchY = yStep + BuddhaFireLotusRenderer.signedHash(seed + s * 139) * verticalSpan * 0.28f;
                    float[] branch = BuddhaFireLotusRenderer.lightningPoint(branchAngle, branchRadius, branchY, seed, s + 30, outerOrbitRadius * 0.014f, verticalSpan * 0.08f);
                    BuddhaFireLotusRenderer.emitLightningSegment(vc, matrix, normal, next, branch, 0.07f, color, (int)((float)arcAlpha * 0.22f * taper));
                    BuddhaFireLotusRenderer.emitLightningSegment(vc, matrix, normal, next, branch, 0.024f, 0xF8FFFF, (int)((float)arcAlpha * 0.62f * taper));
                }
                previous = next;
            }
        }
    }
    private static int colorForFlags(int flags, int cyan, int paleCyan, int violet) {
        if ((flags & 8) != 0 && (flags & 2) != 0) {
            return cyan;
        }
        if ((flags & 8) != 0) {
            return 0x77F8FF;
        }
        if ((flags & 2) != 0) {
            return cyan;
        }
        if ((flags & 4) != 0) {
            return paleCyan;
        }
        if ((flags & 0x20) != 0) {
            return violet;
        }
        return cyan;
    }
    private static float positiveModulo(float value, float mod) {
        float result = value % mod;
        return result < 0.0f ? result + mod : result;
    }
    private static void emitPetalLayer(VertexConsumer vc, Matrix4f matrix, Matrix3f normal, int petals, float innerRadius, float outerRadius, float y, int color, int alpha, float phaseDegrees) {
        int a = Mth.clamp((int)alpha, (int)0, (int)255);
        for (int i = 0; i < petals; ++i) {
            float angle = (float)(Math.PI * 2 * (double)i / (double)petals + Math.toRadians(phaseDegrees));
            float half = (float)(Math.PI / (double)petals * 0.62);
            float[] left = BuddhaFireLotusRenderer.point(angle - half, innerRadius, y);
            float[] right = BuddhaFireLotusRenderer.point(angle + half, innerRadius, y);
            float[] tip = BuddhaFireLotusRenderer.point(angle, outerRadius, y + 0.08f);
            float[] mid = BuddhaFireLotusRenderer.point(angle, innerRadius * 0.28f, y + 0.02f);
            BuddhaFireLotusRenderer.triangle(vc, matrix, normal, mid, left, tip, color, a);
            BuddhaFireLotusRenderer.triangle(vc, matrix, normal, mid, tip, right, color, a);
        }
    }
    private static void emitCore(VertexConsumer vc, Matrix4f matrix, Matrix3f normal, int color, int alpha) {
        for (int i = 0; i < 8; ++i) {
            float a0 = (float)(Math.PI * 2 * (double)i / 8.0);
            float a1 = (float)(Math.PI * 2 * (double)(i + 1) / 8.0);
            float[] center = new float[]{0.0f, 0.14f, 0.0f};
            float[] p0 = BuddhaFireLotusRenderer.point(a0, 0.25f, 0.08f);
            float[] p1 = BuddhaFireLotusRenderer.point(a1, 0.25f, 0.08f);
            BuddhaFireLotusRenderer.triangle(vc, matrix, normal, center, p0, p1, color, alpha);
        }
    }
    private static void emitCrystalPetalLayer(VertexConsumer vc, Matrix4f matrix, Matrix3f normal, int petals, float innerRadius, float midRadius, float outerRadius, float halfWidth, float baseY, float midY, float tipY, int color, int alpha, float phaseDegrees) {
        BuddhaFireLotusRenderer.emitCrystalPetalLayer(vc, matrix, normal, petals, innerRadius, midRadius, outerRadius, halfWidth, baseY, midY, tipY, color, alpha, phaseDegrees, 1.0f);
    }
    private static void emitCrystalPetalLayer(VertexConsumer vc, Matrix4f matrix, Matrix3f normal, int petals, float innerRadius, float midRadius, float outerRadius, float halfWidth, float baseY, float midY, float tipY, int color, int alpha, float phaseDegrees, float bloomProgress) {
        int a = Mth.clamp((int)alpha, (int)0, (int)255);
        int ridgeColor = BuddhaFireLotusRenderer.deepen(color, 1.12f);
        float bloom = Mth.clamp((float)bloomProgress, (float)0.0f, (float)1.0f);
        float openedMidRadius = Mth.lerp((float)bloom, (float)(innerRadius + (midRadius - innerRadius) * 0.22f), (float)midRadius);
        float openedOuterRadius = Mth.lerp((float)bloom, (float)(innerRadius + (outerRadius - innerRadius) * 0.34f), (float)outerRadius);
        float openedHalfWidth = halfWidth * Mth.lerp((float)bloom, (float)0.26f, (float)1.0f);
        float openedBaseY = Mth.lerp((float)bloom, (float)(baseY + 0.1f), (float)baseY);
        float openedMidY = Mth.lerp((float)bloom, (float)(midY + 0.34f), (float)midY);
        float openedTipY = Mth.lerp((float)bloom, (float)(tipY + 0.56f), (float)tipY);
        for (int i = 0; i < petals; ++i) {
            float angle = (float)(Math.PI * 2 * (double)i / (double)petals + Math.toRadians(phaseDegrees));
            float[] base = BuddhaFireLotusRenderer.petalPoint(angle, innerRadius, 0.0f, openedBaseY);
            float[] left = BuddhaFireLotusRenderer.petalPoint(angle, openedMidRadius, openedHalfWidth, openedMidY);
            float[] right = BuddhaFireLotusRenderer.petalPoint(angle, openedMidRadius, -openedHalfWidth, openedMidY);
            float[] tip = BuddhaFireLotusRenderer.petalPoint(angle, openedOuterRadius, 0.0f, openedTipY);
            float[] ridge = BuddhaFireLotusRenderer.petalPoint(angle, openedMidRadius * 0.92f, 0.0f, openedMidY + 0.05f);
            BuddhaFireLotusRenderer.triangle(vc, matrix, normal, base, left, ridge, color, a);
            BuddhaFireLotusRenderer.triangle(vc, matrix, normal, base, ridge, right, color, a);
            BuddhaFireLotusRenderer.triangle(vc, matrix, normal, ridge, left, tip, ridgeColor, Math.max(0, a - 8));
            BuddhaFireLotusRenderer.triangle(vc, matrix, normal, ridge, tip, right, ridgeColor, Math.max(0, a - 8));
            float[] highlight = BuddhaFireLotusRenderer.petalPoint(angle, openedMidRadius * 0.52f, 0.0f, (openedBaseY + openedMidY) * 0.5f + 0.03f);
            BuddhaFireLotusRenderer.triangle(vc, matrix, normal, base, highlight, ridge, 0xF4FEFF, Math.max(0, a - 70));
        }
    }
    private static void emitTrailRibbon(VertexConsumer vc, Matrix4f matrix, Matrix3f normal, Vec3 forward, Vec3 right, Vec3 up, float ageTicks, float sideOffset, float width, float length, int color, int alpha) {
        double wave = (double)Mth.cos((float)(ageTicks * 0.3f)) * 0.24;
        Vec3 nearCenter = right.scale((double)sideOffset);
        Vec3 farCenter = forward.scale((double)(-length)).add(right.scale((double)sideOffset * 0.24)).add(up.scale(wave));
        Vec3 nearLeft = nearCenter.add(right.scale((double)width));
        Vec3 nearRight = nearCenter.subtract(right.scale((double)width));
        Vec3 farLeft = farCenter.add(right.scale((double)width * 0.25));
        Vec3 farRight = farCenter.subtract(right.scale((double)width * 0.25));
        BuddhaFireLotusRenderer.quad(vc, matrix, normal, BuddhaFireLotusRenderer.vec(nearLeft), BuddhaFireLotusRenderer.vec(farLeft), BuddhaFireLotusRenderer.vec(farRight), BuddhaFireLotusRenderer.vec(nearRight), color, Mth.clamp((int)alpha, (int)0, (int)255));
    }
    private static float[] lightningPoint(float angle, float radius, float y, int seed, int segment, float radiusJitter, float yJitter) {
        float r = radius + BuddhaFireLotusRenderer.signedHash(seed + segment * 251) * radiusJitter;
        return new float[]{Mth.sin((float)angle) * r, y + BuddhaFireLotusRenderer.signedHash(seed + segment * 311) * yJitter, Mth.cos((float)angle) * r};
    }
    private static float hash01(int value) {
        int x = value;
        x ^= x >>> 16;
        x *= 2146121005;
        x ^= x >>> 15;
        x *= -2073254261;
        x ^= x >>> 16;
        return (float)(x & 0xFFFF) / 65535.0f;
    }
    private static float signedHash(int value) {
        return BuddhaFireLotusRenderer.hash01(value) * 2.0f - 1.0f;
    }
    private static void emitLightningSegment(VertexConsumer vc, Matrix4f matrix, Matrix3f normal, float[] a, float[] b, float thickness, int color, int alpha) {
        Vec3 end = new Vec3((double)b[0], (double)b[1], (double)b[2]);
        Vec3 start = new Vec3((double)a[0], (double)a[1], (double)a[2]);
        Vec3 dir = end.subtract(start);
        if (dir.lengthSqr() < 1.0E-5) {
            return;
        }
        Vec3 side = dir.normalize().cross(new Vec3(0.0, 1.0, 0.0));
        side = side.lengthSqr() < 1.0E-5 ? new Vec3(1.0, 0.0, 0.0) : side.normalize();
        Vec3 upSide = dir.normalize().cross(side).normalize();
        Vec3 s = side.scale((double)thickness);
        Vec3 u = upSide.scale((double)(thickness * 0.72f));
        BuddhaFireLotusRenderer.quad(vc, matrix, normal, BuddhaFireLotusRenderer.vec(start.add(s)), BuddhaFireLotusRenderer.vec(end.add(s)), BuddhaFireLotusRenderer.vec(end.subtract(s)), BuddhaFireLotusRenderer.vec(start.subtract(s)), color, Mth.clamp((int)alpha, (int)0, (int)255));
        BuddhaFireLotusRenderer.quad(vc, matrix, normal, BuddhaFireLotusRenderer.vec(start.add(u)), BuddhaFireLotusRenderer.vec(end.add(u)), BuddhaFireLotusRenderer.vec(end.subtract(u)), BuddhaFireLotusRenderer.vec(start.subtract(u)), color, Mth.clamp((int)((int)((float)alpha * 0.72f)), (int)0, (int)255));
    }
    private static void emitLotusBowl(VertexConsumer vc, Matrix4f matrix, Matrix3f normal, int segments, float innerRadius, float outerRadius, float y0, float y1, int color, int alpha) {
        int a = Mth.clamp((int)alpha, (int)0, (int)255);
        for (int i = 0; i < segments; ++i) {
            float a0 = (float)(Math.PI * 2 * (double)i / (double)segments);
            float a1 = (float)(Math.PI * 2 * (double)(i + 1) / (double)segments);
            float[] inner0 = BuddhaFireLotusRenderer.point(a0, innerRadius, y0);
            float[] outer0 = BuddhaFireLotusRenderer.point(a0, outerRadius, y1);
            float[] outer1 = BuddhaFireLotusRenderer.point(a1, outerRadius, y1);
            float[] inner1 = BuddhaFireLotusRenderer.point(a1, innerRadius, y0);
            BuddhaFireLotusRenderer.triangle(vc, matrix, normal, inner0, outer0, outer1, color, a);
            BuddhaFireLotusRenderer.triangle(vc, matrix, normal, inner0, outer1, inner1, color, a);
        }
    }
    private static void emitCore(VertexConsumer vc, Matrix4f matrix, Matrix3f normal, int cyan, int violet, int white, int alpha, float ageTicks) {
        BuddhaFireLotusRenderer.emitCore(vc, matrix, normal, cyan, alpha);
        for (int i = 0; i < 6; ++i) {
            float angle = (float)(Math.PI * 2 * (double)i / 6.0 + (double)ageTicks * 0.035);
            float[] base = new float[]{0.0f, 0.22f, 0.0f};
            float[] left = BuddhaFireLotusRenderer.point(angle - 0.1f, 0.09f, 0.18f);
            float[] right = BuddhaFireLotusRenderer.point(angle + 0.1f, 0.09f, 0.18f);
            float[] tip = new float[]{(float)Math.cos(angle) * 0.18f, 0.74f, (float)Math.sin(angle) * 0.18f};
            int color = i % 2 == 0 ? white : violet;
            BuddhaFireLotusRenderer.triangle(vc, matrix, normal, base, left, tip, color, alpha);
            BuddhaFireLotusRenderer.triangle(vc, matrix, normal, base, tip, right, color, alpha);
        }
    }
    public static void renderCyanShockwave(PoseStack pose, MultiBufferSource buffers, float radius, float thickness, float alphaMultiplier) {
        VertexConsumer vc = buffers.getBuffer(RenderType.entityCutoutNoCull((ResourceLocation)WHITE));
        Matrix4f matrix = pose.last().pose();
        Matrix3f normal = pose.last().normal();
        int alpha = (int)(Mth.clamp((float)alphaMultiplier, (float)0.0f, (float)1.0f) * 190.0f);
        int segments = 72;
        for (int band = -3; band <= 3; ++band) {
            float verticalFactor = (float)band / 3.0f * 0.82f;
            float horizontalFactor = Mth.sqrt((float)Math.max(0.0f, 1.0f - verticalFactor * verticalFactor));
            float y = radius * verticalFactor;
            float ringRadius = Math.max(0.1f, radius * horizontalFactor);
            float bandThickness = thickness * (0.52f + horizontalFactor * 0.48f);
            int bandAlpha = (int)((float)alpha * (0.32f + horizontalFactor * 0.68f));
            BuddhaFireLotusRenderer.emitCyanShockwaveBand(vc, matrix, normal, segments, ringRadius, y, bandThickness, bandAlpha);
        }
    }
    private static void emitCyanShockwaveBand(VertexConsumer vc, Matrix4f matrix, Matrix3f normal, int segments, float radius, float y, float thickness, int alpha) {
        float inner = Math.max(0.0f, radius - thickness);
        float outer = radius + thickness;
        for (int i = 0; i < segments; ++i) {
            float a0 = (float)(Math.PI * 2 * (double)i / (double)segments);
            float a1 = (float)(Math.PI * 2 * (double)(i + 1) / (double)segments);
            float wave0 = Mth.cos((float)(a0 * 4.0f)) * thickness * 0.16f;
            float wave1 = Mth.cos((float)(a1 * 4.0f)) * thickness * 0.16f;
            float[] inner0 = new float[]{Mth.sin((float)a0) * inner, y + wave0, Mth.cos((float)a0) * inner};
            float[] outer0 = new float[]{Mth.sin((float)a0) * outer, y + wave0 + thickness * 0.1f, Mth.cos((float)a0) * outer};
            float[] outer1 = new float[]{Mth.sin((float)a1) * outer, y + wave1 + thickness * 0.1f, Mth.cos((float)a1) * outer};
            float[] inner1 = new float[]{Mth.sin((float)a1) * inner, y + wave1, Mth.cos((float)a1) * inner};
            BuddhaFireLotusRenderer.quad(vc, matrix, normal, inner0, outer0, outer1, inner1, 4257279, alpha);
        }
    }
    private static float[] point(float angle, float radius, float y) {
        return new float[]{(float)Math.cos(angle) * radius, y, (float)Math.sin(angle) * radius};
    }
    private static float[] petalPoint(float angle, float radius, float tangentOffset, float y) {
        float cos = (float)Math.cos(angle);
        float sin = (float)Math.sin(angle);
        return new float[]{cos * radius - sin * tangentOffset, y, sin * radius + cos * tangentOffset};
    }
    private static float[] vec(Vec3 value) {
        return new float[]{(float)value.x, (float)value.y, (float)value.z};
    }
    private static int deepen(int color, float charge) {
        float factor = Mth.clamp((float)(0.72f + charge * 0.16f), (float)0.72f, (float)1.35f);
        int r = Mth.clamp((int)((int)((float)(color >> 16 & 0xFF) * factor)), (int)0, (int)255);
        int g = Mth.clamp((int)((int)((float)(color >> 8 & 0xFF) * factor)), (int)0, (int)255);
        int b = Mth.clamp((int)((int)((float)(color & 0xFF) * factor)), (int)0, (int)255);
        return r << 16 | g << 8 | b;
    }
    private static void triangle(VertexConsumer vc, Matrix4f matrix, Matrix3f normal, float[] a, float[] b, float[] c, int color, int alpha) {
        BuddhaFireLotusRenderer.vertex(vc, matrix, normal, a[0], a[1], a[2], color, alpha);
        BuddhaFireLotusRenderer.vertex(vc, matrix, normal, b[0], b[1], b[2], color, alpha);
        BuddhaFireLotusRenderer.vertex(vc, matrix, normal, c[0], c[1], c[2], color, alpha);
        BuddhaFireLotusRenderer.vertex(vc, matrix, normal, c[0], c[1], c[2], color, alpha);
    }
    private static void quad(VertexConsumer vc, Matrix4f matrix, Matrix3f normal, float[] a, float[] b, float[] c, float[] d, int color, int alpha) {
        BuddhaFireLotusRenderer.vertex(vc, matrix, normal, a[0], a[1], a[2], color, alpha);
        BuddhaFireLotusRenderer.vertex(vc, matrix, normal, b[0], b[1], b[2], color, alpha);
        BuddhaFireLotusRenderer.vertex(vc, matrix, normal, c[0], c[1], c[2], color, alpha);
        BuddhaFireLotusRenderer.vertex(vc, matrix, normal, d[0], d[1], d[2], color, alpha);
    }
    private static void vertex(VertexConsumer vc, Matrix4f matrix, Matrix3f normal, float x, float y, float z, int color, int alpha) {
        vc.vertex(matrix, x, y, z).color(color >> 16 & 0xFF, color >> 8 & 0xFF, color & 0xFF, alpha).uv(0.0f, 0.0f).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(0xF000F0).normal(normal, 0.0f, 1.0f, 0.0f).endVertex();
    }

    @NotNull
    public ResourceLocation getTextureLocation(@NotNull BuddhaFireLotusEntity entity) {
        return WHITE;
    }
}
