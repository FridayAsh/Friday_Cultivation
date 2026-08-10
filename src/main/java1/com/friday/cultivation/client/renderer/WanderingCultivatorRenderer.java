package com.friday.cultivation.client.renderer;

import com.friday.cultivation.client.SoulVisibilityClient;
import com.friday.cultivation.entity.npc.WanderingCultivatorEntity;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.ItemInHandLayer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix4f;

/**
 * 散修渲染器 - 严格 1:1 复刻原模组 com.xiaoxiang.cultivation.client.renderer.WanderingCultivatorRenderer
 */
public class WanderingCultivatorRenderer
extends HumanoidMobRenderer<WanderingCultivatorEntity, HumanoidModel<WanderingCultivatorEntity>> {
    private static final ResourceLocation[] SKINS = WanderingCultivatorRenderer.createSkins();
    private static final float SWORD_FORWARD_YAW_OFFSET = 90.0f;
    private static final double SWORD_VERTICAL_OFFSET = -0.1;
    private static final ResourceLocation WHITE = new ResourceLocation("minecraft:textures/misc/white.png");
    private static final int TRAIL_HISTORY_LIMIT = 35;
    private static final float TRAIL_HEAD_HALF_WIDTH = 0.18f;
    private static final int TRAIL_HEAD_ALPHA = 235;
    private static final double POMMEL_OFFSET = 1.5;
    private static final int TRAIL_SUBDIVISIONS = 4;
    private static final double TRAIL_TELEPORT_THRESHOLD = 8.0;
    private static final long TRAIL_STALE_TICKS = 80L;
    private static final float NPC_SOUL_ALPHA = 0.45f;
    private static final float SOUL_LOWER_FADE_START_Y = 0.72f;
    private static final float SOUL_LOWER_FADE_END_Y = 1.52f;
    private static final float SOUL_LOWER_FADE_MIN_MULTIPLIER = 0.08f;
    private static final Map<UUID, TrailState> NPC_SWORD_TRAILS = new HashMap<UUID, TrailState>();
    private static final ResourceLocation DIFU_SKIN = new ResourceLocation("friday_cultivation", "textures/entity/soul_reaper.png");

    public WanderingCultivatorRenderer(EntityRendererProvider.Context ctx) {
        super(ctx, new HumanoidModel(ctx.bakeLayer(ModelLayers.PLAYER)), 0.5f);
        this.layers.removeIf(layer -> layer instanceof ItemInHandLayer);
        this.addLayer(new ItemInHandLayer<WanderingCultivatorEntity, HumanoidModel<WanderingCultivatorEntity>>((RenderLayerParent<WanderingCultivatorEntity, HumanoidModel<WanderingCultivatorEntity>>) this, ctx.getItemInHandRenderer()){

            @Override
            public void render(@NotNull PoseStack poseStack, @NotNull MultiBufferSource buffer, int packedLight, @NotNull WanderingCultivatorEntity entity, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch) {
                if (entity.isNpcSwordFlightActive()) {
                    return;
                }
                super.render(poseStack, buffer, packedLight, entity, limbSwing, limbSwingAmount, partialTicks, ageInTicks, netHeadYaw, headPitch);
            }
        });
    }

    @NotNull
    @Override
    public ResourceLocation getTextureLocation(@NotNull WanderingCultivatorEntity entity) {
        if (entity.isDifuReaper()) {
            return DIFU_SKIN;
        }
        return SKINS[Math.floorMod(entity.getSkinVariant(), SKINS.length)];
    }

    @Override
    public void render(@NotNull WanderingCultivatorEntity entity, float entityYaw, float partialTicks, @NotNull PoseStack poseStack, @NotNull MultiBufferSource buffer, int packedLight) {
        if (entity.isNpcSoulState() && !SoulVisibilityClient.localCanSeeSouls()) {
            NPC_SWORD_TRAILS.remove(entity.getUUID());
            return;
        }
        if (entity.isNpcSwordFlightActive()) {
            entity.walkAnimation.setSpeed(0.0f);
        }
        MultiBufferSource renderBuffer = entity.isNpcSoulState() ? new NpcSoulBufferSource(buffer, 0.45f) : buffer;
        super.render(entity, entityYaw, partialTicks, poseStack, renderBuffer, packedLight);
        WanderingCultivatorRenderer.renderSwordFlightBlade(entity, partialTicks, poseStack, buffer);
    }

    @Override
    protected RenderType getRenderType(@NotNull WanderingCultivatorEntity entity, boolean bodyVisible, boolean translucent, boolean glowing) {
        if (entity.isNpcSoulState()) {
            return RenderType.entityTranslucent((ResourceLocation) this.getTextureLocation(entity));
        }
        return super.getRenderType(entity, bodyVisible, translucent, glowing);
    }

    private static void renderSwordFlightBlade(WanderingCultivatorEntity entity, float partialTicks, PoseStack poseStack, MultiBufferSource buffer) {
        if (!entity.isNpcSwordFlightActive()) {
            NPC_SWORD_TRAILS.remove(entity.getUUID());
            return;
        }
        ItemStack sword = entity.getItemBySlot(EquipmentSlot.MAINHAND);
        if (sword.isEmpty() || !(sword.getItem() instanceof SwordItem)) {
            NPC_SWORD_TRAILS.remove(entity.getUUID());
            return;
        }
        float yaw = Mth.rotLerp((float) partialTicks, (float) entity.yHeadRot, (float) entity.yBodyRot);
        Vec3 renderPos = WanderingCultivatorRenderer.interpolatedEntityPosition(entity, partialTicks);
        TrailState trail = NPC_SWORD_TRAILS.computeIfAbsent(entity.getUUID(), ignored -> new TrailState());
        trail.lastSeenGameTime = entity.level().getGameTime();
        WanderingCultivatorRenderer.addNpcTrailSample(entity, renderPos, yaw, trail);
        WanderingCultivatorRenderer.pruneStaleNpcTrails(entity.level().getGameTime());
        Minecraft mc = Minecraft.getInstance();
        Vec3 camera = mc.gameRenderer.getMainCamera().getPosition();
        WanderingCultivatorRenderer.renderSwordTrail(poseStack, buffer, camera, renderPos, trail.history, new TrailSample(renderPos.x, renderPos.y, renderPos.z, yaw));
        poseStack.pushPose();
        poseStack.translate(0.0, -0.1, 0.0);
        poseStack.mulPose(Axis.YP.rotationDegrees(-yaw + 90.0f));
        poseStack.mulPose(Axis.XP.rotationDegrees(90.0f));
        poseStack.mulPose(Axis.ZP.rotationDegrees(45.0f));
        poseStack.scale(2.4f, 2.4f, 2.4f);
        mc.getItemRenderer().renderStatic(sword, ItemDisplayContext.FIXED, 0xF000F0, OverlayTexture.NO_OVERLAY, poseStack, buffer, entity.level(), entity.getId());
        poseStack.popPose();
    }

    private static Vec3 interpolatedEntityPosition(WanderingCultivatorEntity entity, float partialTicks) {
        return new Vec3(Mth.lerp((double) partialTicks, (double) entity.xo, (double) entity.getX()), Mth.lerp((double) partialTicks, (double) entity.yo, (double) entity.getY()), Mth.lerp((double) partialTicks, (double) entity.zo, (double) entity.getZ()));
    }

    private static void addNpcTrailSample(WanderingCultivatorEntity entity, Vec3 renderPos, float yaw, TrailState trail) {
        if (trail.lastSampleTick == entity.tickCount) {
            return;
        }
        trail.lastSampleTick = entity.tickCount;
        TrailSample sample = new TrailSample(renderPos.x, renderPos.y, renderPos.z, yaw);
        TrailSample previous = trail.history.peekFirst();
        if (previous != null) {
            double dx = sample.x() - previous.x();
            double dy = sample.y() - previous.y();
            double dz = sample.z() - previous.z();
            double distanceSqr = dx * dx + dy * dy + dz * dz;
            float yawDelta = Math.abs(Mth.wrapDegrees((float) (sample.yaw() - previous.yaw())));
            if (distanceSqr < 1.0E-4 && yawDelta < 1.0f) {
                return;
            }
        }
        trail.history.addFirst(sample);
        while (trail.history.size() > 35) {
            trail.history.removeLast();
        }
    }

    private static void pruneStaleNpcTrails(long gameTime) {
        Iterator<Map.Entry<UUID, TrailState>> it = NPC_SWORD_TRAILS.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<UUID, TrailState> entry = it.next();
            if (gameTime - entry.getValue().lastSeenGameTime <= 80L) continue;
            it.remove();
        }
    }

    private static void renderSwordTrail(PoseStack pose, MultiBufferSource buffer, Vec3 cameraPos, Vec3 entityRenderPos, ArrayDeque<TrailSample> history, TrailSample headNow) {
        float t;
        if (history.isEmpty()) {
            return;
        }
        int historySize = history.size();
        TrailSample[] samples = new TrailSample[historySize + 1];
        samples[0] = headNow;
        int idx = 1;
        for (TrailSample sample : history) {
            samples[idx++] = sample;
        }
        int count = samples.length;
        if (count < 2) {
            return;
        }
        Vec3[] pommelPoints = new Vec3[count];
        for (int i = 0; i < count; ++i) {
            pommelPoints[i] = WanderingCultivatorRenderer.pommelWorldFor(samples[i]);
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
                t = (float) sub / 4.0f;
                interp[interpIdx++] = WanderingCultivatorRenderer.catmullRom(p0, p1, p2, p3, t);
            }
        }
        Vec3[] segDirs = new Vec3[interpCount - 1];
        boolean[] segValid = new boolean[interpCount - 1];
        for (int i = 0; i < interpCount - 1; ++i) {
            Vec3 seg = interp[i + 1].subtract(interp[i]);
            double sl2 = seg.lengthSqr();
            segValid[i] = sl2 >= 1.0E-8 && sl2 <= 64.0;
            segDirs[i] = segValid[i] ? seg.normalize() : Vec3.ZERO;
        }
        Vec3[] vertPerp = new Vec3[interpCount];
        for (int i = 0; i < interpCount; ++i) {
            Vec3 avg;
            Vec3 axis = i == 0 ? segDirs[0] : (i == interpCount - 1 ? segDirs[interpCount - 2] : ((avg = segDirs[i - 1].add(segDirs[i])).lengthSqr() < 1.0E-8 ? segDirs[i - 1] : avg.normalize()));
            Vec3 toCam = cameraPos.subtract(interp[i]);
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
            lp[i] = interp[i].subtract(entityRenderPos).add(0.0, -0.1, 0.0);
            t = (float) i / (float) (interpCount - 1);
            w[i] = 0.18f * (1.0f - t);
            a[i] = (int) (235.0f * (1.0f - t));
        }
        RenderSystem.enableBlend();
        VertexConsumer vc = buffer.getBuffer(RenderType.entityCutoutNoCull((ResourceLocation) WHITE));
        Matrix4f m = pose.last().pose();
        for (int i = 0; i < interpCount - 1; ++i) {
            if (!segValid[i]) continue;
            Vec3 lp0 = lp[i];
            Vec3 lp1 = lp[i + 1];
            Vec3 perp0 = vertPerp[i];
            Vec3 perp1 = vertPerp[i + 1];
            float w0 = w[i];
            float w1 = w[i + 1];
            int a0 = a[i];
            int a1 = a[i + 1];
            WanderingCultivatorRenderer.addTrailVert(vc, m, (float) (lp0.x + perp0.x * (double) w0), (float) (lp0.y + perp0.y * (double) w0), (float) (lp0.z + perp0.z * (double) w0), 1.0f, 0.0f, a0);
            WanderingCultivatorRenderer.addTrailVert(vc, m, (float) (lp1.x + perp1.x * (double) w1), (float) (lp1.y + perp1.y * (double) w1), (float) (lp1.z + perp1.z * (double) w1), 1.0f, 1.0f, a1);
            WanderingCultivatorRenderer.addTrailVert(vc, m, (float) (lp1.x - perp1.x * (double) w1), (float) (lp1.y - perp1.y * (double) w1), (float) (lp1.z - perp1.z * (double) w1), 0.0f, 1.0f, a1);
            WanderingCultivatorRenderer.addTrailVert(vc, m, (float) (lp0.x - perp0.x * (double) w0), (float) (lp0.y - perp0.y * (double) w0), (float) (lp0.z - perp0.z * (double) w0), 0.0f, 0.0f, a0);
        }
        RenderSystem.disableBlend();
    }

    private static void addTrailVert(VertexConsumer vc, Matrix4f m, float x, float y, float z, float u, float v, int alpha) {
        vc.vertex(m, x, y, z).color(255, 255, 255, alpha).uv(u, v).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(0xF000F0).normal(0.0f, 1.0f, 0.0f).endVertex();
    }

    private static Vec3 pommelWorldFor(TrailSample sample) {
        float yawRad = sample.yaw() * ((float) Math.PI / 180);
        double sinY = Math.sin(yawRad);
        double cosY = Math.cos(yawRad);
        return new Vec3(sample.x() + sinY * 1.5, sample.y(), sample.z() - cosY * 1.5);
    }

    private static Vec3 catmullRom(Vec3 p0, Vec3 p1, Vec3 p2, Vec3 p3, float t) {
        float t2 = t * t;
        float t3 = t2 * t;
        double x = 0.5 * (2.0 * p1.x + (-p0.x + p2.x) * (double) t + (2.0 * p0.x - 5.0 * p1.x + 4.0 * p2.x - p3.x) * (double) t2 + (-p0.x + 3.0 * p1.x - 3.0 * p2.x + p3.x) * (double) t3);
        double y = 0.5 * (2.0 * p1.y + (-p0.y + p2.y) * (double) t + (2.0 * p0.y - 5.0 * p1.y + 4.0 * p2.y - p3.y) * (double) t2 + (-p0.y + 3.0 * p1.y - 3.0 * p2.y + p3.y) * (double) t3);
        double z = 0.5 * (2.0 * p1.z + (-p0.z + p2.z) * (double) t + (2.0 * p0.z - 5.0 * p1.z + 4.0 * p2.z - p3.z) * (double) t2 + (-p0.z + 3.0 * p1.z - 3.0 * p2.z + p3.z) * (double) t3);
        return new Vec3(x, y, z);
    }

    private static ResourceLocation[] createSkins() {
        ResourceLocation[] result = new ResourceLocation[48];
        for (int i = 0; i < result.length; ++i) {
            result[i] = new ResourceLocation("friday_cultivation", "textures/entity/wandering_cultivator_" + i + ".png");
        }
        return result;
    }

    private static float soulLowerBodyAlphaMultiplier(float modelY) {
        if (modelY <= 0.72f) {
            return 1.0f;
        }
        if (modelY >= 1.52f) {
            return 0.08f;
        }
        float t = (modelY - 0.72f) / 0.79999995f;
        return Mth.lerp(t, 1.0f, 0.08f);
    }

    private static final class NpcSoulBufferSource
    implements MultiBufferSource {
        private final MultiBufferSource parent;
        private final float alpha;

        private NpcSoulBufferSource(MultiBufferSource parent, float alpha) {
            this.parent = parent;
            this.alpha = Mth.clamp(alpha, 0.0f, 1.0f);
        }

        @NotNull
        @Override
        public VertexConsumer getBuffer(@NotNull RenderType type) {
            return new NpcSoulVertexConsumer(this.parent.getBuffer(type), this.alpha);
        }
    }

    private static final class TrailState {
        private final ArrayDeque<TrailSample> history = new ArrayDeque<TrailSample>();
        private int lastSampleTick = Integer.MIN_VALUE;
        private long lastSeenGameTime = 0L;

        private TrailState() {
        }
    }

    private record TrailSample(double x, double y, double z, float yaw) {
    }

    private static final class NpcSoulVertexConsumer
    implements VertexConsumer {
        private final VertexConsumer parent;
        private final float alpha;
        private float modelY = 0.0f;

        private NpcSoulVertexConsumer(VertexConsumer parent, float alpha) {
            this.parent = parent;
            this.alpha = alpha;
        }

        @NotNull
        @Override
        public VertexConsumer vertex(double x, double y, double z) {
            this.modelY = (float) y;
            this.parent.vertex(x, y, z);
            return this;
        }

        @NotNull
        @Override
        public VertexConsumer vertex(Matrix4f matrix, float x, float y, float z) {
            this.modelY = y;
            this.parent.vertex(matrix, x, y, z);
            return this;
        }

        @NotNull
        @Override
        public VertexConsumer color(int r, int g, int b, int a) {
            int soulR = Math.min(255, Math.round((float) r * 0.78f + 34.0f));
            int soulG = Math.min(255, Math.round((float) g * 0.88f + 26.0f));
            int soulB = Math.min(255, Math.round((float) b * 0.98f + 18.0f));
            this.parent.color(soulR, soulG, soulB, Math.round((float) a * this.alpha * WanderingCultivatorRenderer.soulLowerBodyAlphaMultiplier(this.modelY)));
            return this;
        }

        @NotNull
        @Override
        public VertexConsumer uv(float u, float v) {
            this.parent.uv(u, v);
            return this;
        }

        @NotNull
        @Override
        public VertexConsumer overlayCoords(int u, int v) {
            this.parent.overlayCoords(u, v);
            return this;
        }

        @NotNull
        @Override
        public VertexConsumer uv2(int u, int v) {
            this.parent.uv2(u, v);
            return this;
        }

        @NotNull
        @Override
        public VertexConsumer normal(float x, float y, float z) {
            this.parent.normal(x, y, z);
            return this;
        }

        @Override
        public void endVertex() {
            this.parent.endVertex();
        }

        @Override
        public void defaultColor(int r, int g, int b, int a) {
            this.parent.defaultColor(r, g, b, Math.round((float) a * this.alpha));
        }

        @Override
        public void unsetDefaultColor() {
            this.parent.unsetDefaultColor();
        }
    }
}
