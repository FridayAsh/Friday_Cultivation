/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.vertex.PoseStack
 *  com.mojang.blaze3d.vertex.VertexConsumer
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.multiplayer.ClientLevel
 *  net.minecraft.client.renderer.MultiBufferSource
 *  net.minecraft.client.renderer.RenderType
 *  net.minecraft.sounds.SoundEvents
 *  net.minecraft.sounds.SoundSource
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.entity.player.Player
 *  net.minecraftforge.api.distmarker.Dist
 *  net.minecraftforge.client.event.RenderLivingEvent$Post
 *  net.minecraftforge.event.TickEvent$ClientTickEvent
 *  net.minecraftforge.event.TickEvent$Phase
 *  net.minecraftforge.eventbus.api.SubscribeEvent
 *  net.minecraftforge.fml.common.Mod$EventBusSubscriber
 *  org.joml.Matrix4f
 */
package com.friday.cultivation.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.friday.cultivation.network.QiShieldHitPacket;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderLivingEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.joml.Matrix4f;

@Mod.EventBusSubscriber(modid="friday_cultivation", value={Dist.CLIENT})
public final class QiShieldVisualHandler {
    private static final Map<UUID, ShieldFlash> activeShields = new ConcurrentHashMap<UUID, ShieldFlash>();
    private static double lastHitDirX;
    private static double lastHitDirY;
    private static double lastHitDirZ;
    private static int firstPersonIndicatorTicks;
    private static int firstPersonIndicatorTotal;
    private static final int STACKS = 8;
    private static final int SECTORS = 16;
    private static final float[][] SPHERE_VERTICES;

    private QiShieldVisualHandler() {
    }

    public static int getIndicatorTicks() {
        return firstPersonIndicatorTicks;
    }

    public static int getIndicatorTotal() {
        return firstPersonIndicatorTotal;
    }

    public static double getIndicatorDirX() {
        return lastHitDirX;
    }

    public static double getIndicatorDirY() {
        return lastHitDirY;
    }

    public static double getIndicatorDirZ() {
        return lastHitDirZ;
    }

    public static void onShieldHit(QiShieldHitPacket msg) {
        Minecraft mc = Minecraft.getInstance();
        ClientLevel level = mc.level;
        if (level == null) {
            return;
        }
        Entity e = level.getEntity(msg.playerId());
        if (!(e instanceof Player)) {
            return;
        }
        Player player = (Player)e;
        int duration = (int)Math.max(8.0, Math.min(14.0, 8.0 + (double)msg.intensity() * 0.5));
        activeShields.put(player.getUUID(), new ShieldFlash(duration, msg.intensity()));
        if (mc.player != null && mc.player.getUUID().equals(player.getUUID())) {
            lastHitDirX = msg.dirX();
            lastHitDirY = msg.dirY();
            lastHitDirZ = msg.dirZ();
            firstPersonIndicatorTotal = firstPersonIndicatorTicks = duration + 4;
        }
        level.playLocalSound(player.getX(), player.getY(), player.getZ(), SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 0.6f, 1.2f + level.random.nextFloat() * 0.3f, false);
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }
        if (firstPersonIndicatorTicks > 0) {
            --firstPersonIndicatorTicks;
        }
        if (activeShields.isEmpty()) {
            return;
        }
        Iterator<Map.Entry<UUID, ShieldFlash>> it = activeShields.entrySet().iterator();
        while (it.hasNext()) {
            ShieldFlash f = it.next().getValue();
            --f.ticksRemaining;
            if (f.ticksRemaining > 0) continue;
            it.remove();
        }
    }

    @SubscribeEvent
    public static void onRenderLiving(RenderLivingEvent.Post<?, ?> event) {
        LivingEntity livingEntity = event.getEntity();
        if (!(livingEntity instanceof Player)) {
            return;
        }
        Player player = (Player)livingEntity;
        ShieldFlash flash = activeShields.get(player.getUUID());
        if (flash == null) {
            return;
        }
        float partial = event.getPartialTick();
        float t = Math.max(0.0f, ((float)flash.ticksRemaining - partial) / (float)flash.totalDuration);
        if (t <= 0.0f) {
            return;
        }
        float alpha = t * 0.65f;
        float scaleMul = 1.0f + (1.0f - t) * 0.3f;
        float radius = (player.getBbHeight() / 2.0f + 0.25f) * scaleMul;
        PoseStack pose = event.getPoseStack();
        pose.pushPose();
        pose.translate(0.0f, player.getBbHeight() / 2.0f, 0.0f);
        QiShieldVisualHandler.renderShieldSphere(pose, event.getMultiBufferSource(), radius, alpha);
        pose.popPose();
    }

    private static void renderShieldSphere(PoseStack pose, MultiBufferSource buf, float radius, float alpha) {
        VertexConsumer vc = buf.getBuffer(RenderType.lightning());
        Matrix4f mat = pose.last().pose();
        int a = (int)(alpha * 255.0f) & 0xFF;
        int r = 255;
        int g = 255;
        int b = 255;
        for (int i = 0; i < SPHERE_VERTICES.length; i += 4) {
            float[] v0 = SPHERE_VERTICES[i];
            float[] v1 = SPHERE_VERTICES[i + 1];
            float[] v2 = SPHERE_VERTICES[i + 2];
            float[] v3 = SPHERE_VERTICES[i + 3];
            QiShieldVisualHandler.addVertex(vc, mat, v0, radius, r, g, b, a);
            QiShieldVisualHandler.addVertex(vc, mat, v1, radius, r, g, b, a);
            QiShieldVisualHandler.addVertex(vc, mat, v2, radius, r, g, b, a);
            QiShieldVisualHandler.addVertex(vc, mat, v0, radius, r, g, b, a);
            QiShieldVisualHandler.addVertex(vc, mat, v2, radius, r, g, b, a);
            QiShieldVisualHandler.addVertex(vc, mat, v3, radius, r, g, b, a);
        }
    }

    private static void addVertex(VertexConsumer vc, Matrix4f mat, float[] v, float radius, int r, int g, int b, int a) {
        vc.vertex(mat, v[0] * radius, v[1] * radius, v[2] * radius).color(r, g, b, a).endVertex();
    }

    private static float[][] buildSphereVertices() {
        int count = 512;
        float[][] verts = new float[count][3];
        int idx = 0;
        for (int stack = 0; stack < 8; ++stack) {
            float phi1 = (float)((double)stack / 8.0 * Math.PI);
            float phi2 = (float)((double)(stack + 1) / 8.0 * Math.PI);
            float y1 = (float)Math.cos(phi1);
            float y2 = (float)Math.cos(phi2);
            float r1 = (float)Math.sin(phi1);
            float r2 = (float)Math.sin(phi2);
            for (int sector = 0; sector < 16; ++sector) {
                float t1 = (float)((double)sector / 16.0 * Math.PI * 2.0);
                float t2 = (float)((double)(sector + 1) / 16.0 * Math.PI * 2.0);
                float c1 = (float)Math.cos(t1);
                float s1 = (float)Math.sin(t1);
                float c2 = (float)Math.cos(t2);
                float s2 = (float)Math.sin(t2);
                verts[idx++] = new float[]{r1 * c1, y1, r1 * s1};
                verts[idx++] = new float[]{r2 * c1, y2, r2 * s1};
                verts[idx++] = new float[]{r2 * c2, y2, r2 * s2};
                verts[idx++] = new float[]{r1 * c2, y1, r1 * s2};
            }
        }
        return verts;
    }

    static {
        firstPersonIndicatorTicks = 0;
        firstPersonIndicatorTotal = 0;
        SPHERE_VERTICES = QiShieldVisualHandler.buildSphereVertices();
    }

    private static class ShieldFlash {
        int ticksRemaining;
        int totalDuration;
        float intensity;

        ShieldFlash(int duration, float intensity) {
            this.ticksRemaining = duration;
            this.totalDuration = duration;
            this.intensity = intensity;
        }
    }
}

