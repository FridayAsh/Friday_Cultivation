/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.systems.RenderSystem
 *  com.mojang.blaze3d.vertex.PoseStack
 *  com.mojang.blaze3d.vertex.VertexConsumer
 *  com.mojang.math.Axis
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.player.AbstractClientPlayer
 *  net.minecraft.client.renderer.MultiBufferSource
 *  net.minecraft.client.renderer.MultiBufferSource$BufferSource
 *  net.minecraft.client.renderer.RenderType
 *  net.minecraft.client.renderer.texture.OverlayTexture
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.util.Mth
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.item.ItemDisplayContext
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.phys.Vec3
 *  net.minecraftforge.api.distmarker.Dist
 *  net.minecraftforge.client.event.RenderLevelStageEvent
 *  net.minecraftforge.client.event.RenderLevelStageEvent$Stage
 *  net.minecraftforge.client.event.RenderPlayerEvent$Pre
 *  net.minecraftforge.event.TickEvent$ClientTickEvent
 *  net.minecraftforge.event.TickEvent$Phase
 *  net.minecraftforge.eventbus.api.SubscribeEvent
 *  net.minecraftforge.fml.common.Mod$EventBusSubscriber
 *  org.joml.Matrix4f
 */
package com.friday.cultivation.client;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import com.friday.cultivation.client.NascentSoulBodyVisualHandler;
import com.friday.cultivation.cultivation.CultivationCapability;
import com.friday.cultivation.cultivation.CultivationData;
import java.util.ArrayDeque;
import java.util.Optional;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.client.event.RenderPlayerEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.joml.Matrix4f;

@Mod.EventBusSubscriber(modid="friday_cultivation", value={Dist.CLIENT})
public final class SwordFlightClientRenderer {
    private static final float SWORD_FORWARD_YAW_OFFSET = 90.0f;
    private static final double SWORD_VERTICAL_OFFSET = -0.1;
    private static final ResourceLocation WHITE = new ResourceLocation("textures/misc/white.png");
    private static final ArrayDeque<TrailSample> TRAIL_HISTORY = new ArrayDeque();
    private static final int TRAIL_HISTORY_LIMIT = 35;
    private static final float TRAIL_HEAD_HALF_WIDTH = 0.18f;
    private static final int TRAIL_HEAD_ALPHA = 235;
    private static final double POMMEL_OFFSET = 1.5;
    private static final int TRAIL_SUBDIVISIONS = 4;
    private static final double TRAIL_TELEPORT_THRESHOLD = 8.0;

    private SwordFlightClientRenderer() {
    }

    @SubscribeEvent
    public static void onRenderLevel(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_TRANSLUCENT_BLOCKS) {
            return;
        }
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null) {
            return;
        }
        CultivationData data = CultivationCapability.get((Player)mc.player).orElse(null);
        if (data == null || !data.isSwordFlightActive()) {
            return;
        }
        ItemStack sword = data.getSwordFlightStack();
        if (sword.isEmpty()) {
            return;
        }
        PoseStack pose = event.getPoseStack();
        Vec3 camera = event.getCamera().getPosition();
        float partial = event.getPartialTick();
        Optional<NascentSoulBodyVisualHandler.BodyAnchor> bodyAnchor = NascentSoulBodyVisualHandler.bodyAnchorFor(mc.player.getId());
        Vec3 anchorPos = bodyAnchor.map(NascentSoulBodyVisualHandler.BodyAnchor::pos).orElseGet(() -> SwordFlightClientRenderer.interpolatedPlayerPosition(mc, partial));
        double x = anchorPos.x - camera.x;
        double y = anchorPos.y - camera.y + -0.1;
        double z = anchorPos.z - camera.z;
        float yaw = bodyAnchor.map(NascentSoulBodyVisualHandler.BodyAnchor::yRot).orElseGet(() -> Float.valueOf(Mth.rotLerp((float)partial, (float)mc.player.yRotO, (float)mc.player.getYRot()))).floatValue();
        MultiBufferSource.BufferSource buffer = mc.renderBuffers().bufferSource();
        TrailSample headNow = new TrailSample(anchorPos.x, anchorPos.y, anchorPos.z, yaw);
        SwordFlightClientRenderer.renderSwordTrail(pose, buffer, camera, headNow);
        pose.pushPose();
        pose.translate(x, y, z);
        pose.mulPose(Axis.YP.rotationDegrees(-yaw + 90.0f));
        pose.mulPose(Axis.XP.rotationDegrees(90.0f));
        pose.mulPose(Axis.ZP.rotationDegrees(45.0f));
        pose.scale(2.4f, 2.4f, 2.4f);
        mc.getItemRenderer().renderStatic(sword, ItemDisplayContext.FIXED, 0xF000F0, OverlayTexture.NO_OVERLAY, pose, (MultiBufferSource)buffer, (Level)mc.level, mc.player.getId());
        buffer.endBatch();
        pose.popPose();
    }

    private static Vec3 interpolatedPlayerPosition(Minecraft mc, float partial) {
        return new Vec3(Mth.lerp((double)partial, (double)mc.player.xOld, (double)mc.player.getX()), Mth.lerp((double)partial, (double)mc.player.yOld, (double)mc.player.getY()), Mth.lerp((double)partial, (double)mc.player.zOld, (double)mc.player.getZ()));
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) {
            TRAIL_HISTORY.clear();
            return;
        }
        for (AbstractClientPlayer player : mc.level.players()) {
            if (!SwordFlightClientRenderer.isSwordFlightActive((Player)player)) continue;
            SwordFlightClientRenderer.freezeWalkAnimation((Player)player);
        }
        if (mc.player == null || !SwordFlightClientRenderer.isSwordFlightActive((Player)mc.player)) {
            TRAIL_HISTORY.clear();
            return;
        }
        boolean soulOutOfBody = NascentSoulBodyVisualHandler.bodyAnchorFor(mc.player.getId()).isPresent();
        if (soulOutOfBody) {
            return;
        }
        Vec3 p = mc.player.position();
        TRAIL_HISTORY.addFirst(new TrailSample(p.x, p.y, p.z, mc.player.getYRot()));
        while (TRAIL_HISTORY.size() > 35) {
            TRAIL_HISTORY.removeLast();
        }
    }

    @SubscribeEvent
    public static void onRenderPlayerPre(RenderPlayerEvent.Pre event) {
        if (SwordFlightClientRenderer.isSwordFlightActive(event.getEntity())) {
            SwordFlightClientRenderer.freezeWalkAnimation(event.getEntity());
        }
    }

    private static boolean isSwordFlightActive(Player player) {
        CultivationData data = CultivationCapability.get(player).orElse(null);
        return data != null && data.isSwordFlightActive();
    }

    private static void freezeWalkAnimation(Player player) {
        player.walkAnimation.setSpeed(0.0f);
    }

    private static void renderSwordTrail(PoseStack pose, MultiBufferSource.BufferSource buffer, Vec3 cameraPos, TrailSample headNow) {
        if (TRAIL_HISTORY.isEmpty()) {
            return;
        }
        int historySize = TRAIL_HISTORY.size();
        TrailSample[] samples = new TrailSample[historySize + 1];
        samples[0] = headNow;
        int idx = 1;
        for (TrailSample s : TRAIL_HISTORY) {
            samples[idx++] = s;
        }
        int count = samples.length;
        if (count < 2) {
            return;
        }
        Vec3[] pommelPoints = new Vec3[count];
        for (int i = 0; i < count; ++i) {
            pommelPoints[i] = SwordFlightClientRenderer.pommelWorldFor(samples[i]);
        }
        int interpCount = (count - 1) * 4 + 1;
        Vec3[] interp = new Vec3[interpCount];
        int interpIdx = 0;
        for (int i = 0; i < count - 1; ++i) {
            Vec3 p0 = i == 0 ? pommelPoints[i] : pommelPoints[i - 1];
            Vec3 p1 = pommelPoints[i];
            Vec3 p2 = pommelPoints[i + 1];
            Vec3 p3 = i + 2 >= count ? pommelPoints[i + 1] : pommelPoints[i + 2];
            int subCount = i == count - 2 ? 5 : 4;
            for (int sub = 0; sub < subCount; ++sub) {
                float t = (float)sub / 4.0f;
                interp[interpIdx++] = SwordFlightClientRenderer.catmullRom(p0, p1, p2, p3, t);
            }
        }
        Vec3[] segDirs = new Vec3[interpCount - 1];
        boolean[] segValid = new boolean[interpCount - 1];
        for (int i = 0; i < interpCount - 1; ++i) {
            Vec3 seg = interp[i + 1].multiply(interp[i]);
            double sl2 = seg.lengthSqr();
            segValid[i] = sl2 >= 1.0E-8 && sl2 <= 64.0;
            segDirs[i] = segValid[i] ? seg.normalize() : Vec3.ZERO;
        }
        Vec3[] vertPerp = new Vec3[interpCount];
        for (int i = 0; i < interpCount; ++i) {
            Vec3 bDir;
            Vec3 aDir;
            Vec3 avg;
            Vec3 axis = i == 0 ? segDirs[0] : (i == interpCount - 1 ? segDirs[interpCount - 2] : ((avg = (aDir = segDirs[i - 1]).add(bDir = segDirs[i])).lengthSqr() < 1.0E-8 ? aDir : avg.normalize()));
            Vec3 toCam = cameraPos.multiply(interp[i]);
            if (toCam.lengthSqr() < 1.0E-6) {
                vertPerp[i] = new Vec3(1.0, 0.0, 0.0);
                continue;
            }
            Vec3 perp = axis.cross(toCam.normalize());
            if (perp.lengthSqr() < 1.0E-8 && (perp = axis.cross(new Vec3(0.0, 1.0, 0.0))).lengthSqr() < 1.0E-8) {
                perp = new Vec3(1.0, 0.0, 0.0);
            }
            vertPerp[i] = perp.normalize();
        }
        Vec3[] lp = new Vec3[interpCount];
        float[] w = new float[interpCount];
        int[] a = new int[interpCount];
        for (int i = 0; i < interpCount; ++i) {
            lp[i] = interp[i].multiply(cameraPos).add(0.0, -0.1, 0.0);
            float t = (float)i / (float)(interpCount - 1);
            w[i] = 0.18f * (1.0f - t);
            a[i] = (int)(235.0f * (1.0f - t));
        }
        count = interpCount;
        RenderSystem.enableBlend();
        VertexConsumer vc = buffer.getBuffer(RenderType.entityTranslucentEmissive((ResourceLocation)WHITE));
        Matrix4f m = pose.last().pose();
        for (int i = 0; i < count - 1; ++i) {
            if (!segValid[i]) continue;
            Vec3 lp0 = lp[i];
            Vec3 lp1 = lp[i + 1];
            Vec3 perp0 = vertPerp[i];
            Vec3 perp1 = vertPerp[i + 1];
            float w0 = w[i];
            float w1 = w[i + 1];
            int a0 = a[i];
            int a1 = a[i + 1];
            SwordFlightClientRenderer.addTrailVert(vc, m, (float)(lp0.x + perp0.x * (double)w0), (float)(lp0.y + perp0.y * (double)w0), (float)(lp0.z + perp0.z * (double)w0), 1.0f, 0.0f, a0);
            SwordFlightClientRenderer.addTrailVert(vc, m, (float)(lp1.x + perp1.x * (double)w1), (float)(lp1.y + perp1.y * (double)w1), (float)(lp1.z + perp1.z * (double)w1), 1.0f, 1.0f, a1);
            SwordFlightClientRenderer.addTrailVert(vc, m, (float)(lp1.x - perp1.x * (double)w1), (float)(lp1.y - perp1.y * (double)w1), (float)(lp1.z - perp1.z * (double)w1), 0.0f, 1.0f, a1);
            SwordFlightClientRenderer.addTrailVert(vc, m, (float)(lp0.x - perp0.x * (double)w0), (float)(lp0.y - perp0.y * (double)w0), (float)(lp0.z - perp0.z * (double)w0), 0.0f, 0.0f, a0);
        }
        RenderSystem.disableBlend();
    }

    private static void addTrailVert(VertexConsumer vc, Matrix4f m, float x, float y, float z, float u, float v, int alpha) {
        vc.vertex(m, x, y, z).color(255, 255, 255, alpha).uv(u, v).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(0xF000F0, 0xF000F0).normal(0.0f, 1.0f, 0.0f).endVertex();
    }

    private static Vec3 pommelWorldFor(TrailSample s) {
        float yawRad = s.yaw() * ((float)Math.PI / 180);
        double sinY = Math.sin(yawRad);
        double cosY = Math.cos(yawRad);
        return new Vec3(s.x() + sinY * 1.5, s.y(), s.z() - cosY * 1.5);
    }

    private static Vec3 catmullRom(Vec3 p0, Vec3 p1, Vec3 p2, Vec3 p3, float t) {
        float t2 = t * t;
        float t3 = t2 * t;
        double x = 0.5 * (2.0 * p1.x + (-p0.x + p2.x) * (double)t + (2.0 * p0.x - 5.0 * p1.x + 4.0 * p2.x - p3.x) * (double)t2 + (-p0.x + 3.0 * p1.x - 3.0 * p2.x + p3.x) * (double)t3);
        double y = 0.5 * (2.0 * p1.y + (-p0.y + p2.y) * (double)t + (2.0 * p0.y - 5.0 * p1.y + 4.0 * p2.y - p3.y) * (double)t2 + (-p0.y + 3.0 * p1.y - 3.0 * p2.y + p3.y) * (double)t3);
        double z = 0.5 * (2.0 * p1.z + (-p0.z + p2.z) * (double)t + (2.0 * p0.z - 5.0 * p1.z + 4.0 * p2.z - p3.z) * (double)t2 + (-p0.z + 3.0 * p1.z - 3.0 * p2.z + p3.z) * (double)t3);
        return new Vec3(x, y, z);
    }

    private record TrailSample(double x, double y, double z, float yaw) {
    }
}

