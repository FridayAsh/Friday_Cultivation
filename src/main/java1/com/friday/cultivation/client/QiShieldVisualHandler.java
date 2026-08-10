package com.friday.cultivation.client;

import com.friday.cultivation.network.QiShieldHitPacket;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderLivingEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.joml.Matrix4f;

import java.util.Iterator;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 灵气护盾视觉处理器（服务端→客户端接收）— 严格复刻自原模组
 * com.xiaoxiang.cultivation.client.QiShieldVisualHandler
 * <p>
 * 当玩家护盾被攻击时：
 * <ul>
 *   <li>渲染持续 8-14 tick 的发光球体包裹玩家（按 intensity 缩放 0.3）</li>
 *   <li>第一人称视角：在 HUD 显示攻击方向指示器（持续 duration+4 tick）</li>
 *   <li>播放 {@code SoundEvents.PLAYER_HURT_SWEET} 0.6 音量 / 1.2+随机*0.3 音调</li>
 * </ul>
 */
@Mod.EventBusSubscriber(modid = "friday_cultivation", value = Dist.CLIENT)
public final class QiShieldVisualHandler {

    private static final Map<UUID, ShieldFlash> activeShields = new ConcurrentHashMap<>();
    private static double lastHitDirX;
    private static double lastHitDirY;
    private static double lastHitDirZ;
    private static int firstPersonIndicatorTicks;
    private static int firstPersonIndicatorTotal;
    private static final int STACKS = 8;
    private static final int SECTORS = 16;
    private static final float[][] SPHERE_VERTICES = buildSphereVertices();

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
        if (!(level.getEntity(msg.playerId()) instanceof Player player)) {
            return;
        }
        int duration = (int) Math.max(8.0, Math.min(14.0, 8.0 + msg.intensity() * 0.5));
        activeShields.put(player.getUUID(), new ShieldFlash(duration, msg.intensity()));
        if (mc.player != null && mc.player.getUUID().equals(player.getUUID())) {
            lastHitDirX = msg.dirX();
            lastHitDirY = msg.dirY();
            lastHitDirZ = msg.dirZ();
            firstPersonIndicatorTotal = firstPersonIndicatorTicks = duration + 4;
        }
        level.playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.PLAYER_HURT, SoundSource.PLAYERS, 0.6f,
                1.2f + level.random.nextFloat() * 0.3f);
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
        if (!(livingEntity instanceof Player player)) {
            return;
        }
        ShieldFlash flash = activeShields.get(player.getUUID());
        if (flash == null) {
            return;
        }
        float partial = event.getPartialTick();
        float t = Math.max(0.0f, (flash.ticksRemaining - partial) / (float) flash.totalDuration);
        if (t <= 0.0f) {
            return;
        }
        float alpha = t * 0.65f;
        float scaleMul = 1.0f + (1.0f - t) * 0.3f;
        float radius = (player.getBbHeight() / 2.0f + 0.25f) * scaleMul;
        PoseStack pose = event.getPoseStack();
        pose.pushPose();
        pose.translate(0.0f, player.getBbHeight() / 2.0f, 0.0f);
        renderShieldSphere(pose, event.getMultiBufferSource(), radius, alpha);
        pose.popPose();
    }

    private static void renderShieldSphere(PoseStack pose, MultiBufferSource buf, float radius, float alpha) {
        VertexConsumer vc = buf.getBuffer(RenderType.debugFilledBox());
        Matrix4f mat = pose.last().pose();
        int a = (int) (alpha * 255.0f) & 0xFF;
        int r = 255;
        int g = 255;
        int b = 255;
        for (int i = 0; i < SPHERE_VERTICES.length; i += 4) {
            float[] v0 = SPHERE_VERTICES[i];
            float[] v1 = SPHERE_VERTICES[i + 1];
            float[] v2 = SPHERE_VERTICES[i + 2];
            float[] v3 = SPHERE_VERTICES[i + 3];
            addVertex(vc, mat, v0, radius, r, g, b, a);
            addVertex(vc, mat, v1, radius, r, g, b, a);
            addVertex(vc, mat, v2, radius, r, g, b, a);
            addVertex(vc, mat, v0, radius, r, g, b, a);
            addVertex(vc, mat, v2, radius, r, g, b, a);
            addVertex(vc, mat, v3, radius, r, g, b, a);
        }
    }

    private static void addVertex(VertexConsumer vc, Matrix4f mat, float[] v, float radius, int r, int g, int b, int a) {
        vc.vertex(mat, v[0] * radius, v[1] * radius, v[2] * radius).color(r, g, b, a).endVertex();
    }

    private static float[][] buildSphereVertices() {
        int count = STACKS * SECTORS * 4;
        float[][] verts = new float[count][3];
        int idx = 0;
        for (int stack = 0; stack < STACKS; ++stack) {
            float phi1 = (float) ((double) stack / STACKS * Math.PI);
            float phi2 = (float) ((double) (stack + 1) / STACKS * Math.PI);
            float y1 = (float) Math.cos(phi1);
            float y2 = (float) Math.cos(phi2);
            float r1 = (float) Math.sin(phi1);
            float r2 = (float) Math.sin(phi2);
            for (int sector = 0; sector < SECTORS; ++sector) {
                float t1 = (float) ((double) sector / SECTORS * Math.PI * 2.0);
                float t2 = (float) ((double) (sector + 1) / SECTORS * Math.PI * 2.0);
                float c1 = (float) Math.cos(t1);
                float s1 = (float) Math.sin(t1);
                float c2 = (float) Math.cos(t2);
                float s2 = (float) Math.sin(t2);
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
    }

    /** 护盾闪光 visual record（ticksRemaining / totalDuration / intensity） */
    public static final class ShieldFlash {
        public int ticksRemaining;
        public final int totalDuration;
        public final float intensity;

        public ShieldFlash(int totalDuration, float intensity) {
            this.ticksRemaining = totalDuration;
            this.totalDuration = totalDuration;
            this.intensity = intensity;
        }
    }
}
