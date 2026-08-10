package com.friday.cultivation.client.renderer;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.friday.cultivation.entity.spell.MeteorEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
public class MeteorRenderer
extends EntityRenderer<MeteorEntity> {
    private static final ResourceLocation METEOR_TEXTURE = new ResourceLocation("friday_cultivation", "textures/entity/star_fall_meteor.png");
    private static final ResourceLocation HALO_TEXTURE = new ResourceLocation("friday_cultivation", "textures/entity/great_fireball.png");
    private static final int STACKS = 12;
    private static final int SECTORS = 18;
    private static final SphereVertex[][] SPHERE_QUADS = MeteorRenderer.buildSphere();
    public MeteorRenderer(EntityRendererProvider.Context ctx) {
        super(ctx);
    }
    public void render(@NotNull MeteorEntity entity, float yaw, float partialTick, @NotNull PoseStack pose, @NotNull MultiBufferSource buf, int light) {
        pose.pushPose();
        float diameter = entity.diameter();
        float age = (float)entity.tickCount + partialTick;
        RenderSystem.enableBlend();
        pose.pushPose();
        pose.scale(diameter, diameter, diameter);
        pose.mulPose(new Quaternionf().rotationY(age * 0.06f));
        pose.mulPose(new Quaternionf().rotationX(age * 0.04f));
        VertexConsumer rockVc = buf.getBuffer(RenderType.entityCutoutNoCullZOffset((ResourceLocation)METEOR_TEXTURE));
        Matrix4f m1 = pose.last().pose();
        Matrix3f n1 = pose.last().normal();
        SphereVertex[][] sphereVertexArray = SPHERE_QUADS;
        int n = sphereVertexArray.length;
        for (int i = 0; i < n; ++i) {
            SphereVertex[] quad;
            for (SphereVertex v : quad = sphereVertexArray[i]) {
                rockVc.vertex(m1, v.x, v.y, v.z).color(255, 255, 255, 255).uv(v.u, v.v).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(0xF000F0).normal(n1, v.x, v.y, v.z).endVertex();
            }
        }
        pose.popPose();
        pose.pushPose();
        float haloScale = entity.isMega() ? diameter * 1.5f : diameter * 1.3f;
        pose.scale(haloScale, haloScale, haloScale);
        float pulseScale = 1.0f + (float)Math.sin(age * 0.15f) * 0.05f;
        pose.scale(pulseScale, pulseScale, pulseScale);
        pose.mulPose(new Quaternionf().rotationY(age * -0.03f));
        VertexConsumer haloVc = buf.getBuffer(RenderType.entityCutoutNoCull((ResourceLocation)HALO_TEXTURE));
        Matrix4f m2 = pose.last().pose();
        Matrix3f n2 = pose.last().normal();
        int alpha = entity.isMega() ? 180 : 130;
        int rCol = 255;
        int gCol = entity.isMega() ? 130 : 160;
        int bCol = 60;
        SphereVertex[][] sphereVertexArray2 = SPHERE_QUADS;
        int n3 = sphereVertexArray2.length;
        for (int i = 0; i < n3; ++i) {
            SphereVertex[] quad;
            for (SphereVertex v : quad = sphereVertexArray2[i]) {
                haloVc.vertex(m2, v.x, v.y, v.z).color(rCol, gCol, bCol, alpha).uv(v.u, v.v).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(0xF000F0).normal(n2, v.x, v.y, v.z).endVertex();
            }
        }
        pose.popPose();
        RenderSystem.disableBlend();
        pose.popPose();
        super.render(entity, yaw, partialTick, pose, buf, light);
    }
    @NotNull
    public ResourceLocation getTextureLocation(@NotNull MeteorEntity entity) {
        return METEOR_TEXTURE;
    }
    private static SphereVertex[][] buildSphere() {
        SphereVertex[][] quads = new SphereVertex[216][4];
        int idx = 0;
        for (int stack = 0; stack < 12; ++stack) {
            float phi1 = (float)((double)stack / 12.0 * Math.PI);
            float phi2 = (float)((double)(stack + 1) / 12.0 * Math.PI);
            float v1 = (float)stack / 12.0f;
            float v2 = (float)(stack + 1) / 12.0f;
            float y1 = (float)Math.cos(phi1) * 0.5f;
            float y2 = (float)Math.cos(phi2) * 0.5f;
            float r1 = (float)Math.sin(phi1) * 0.5f;
            float r2 = (float)Math.sin(phi2) * 0.5f;
            for (int sector = 0; sector < 18; ++sector) {
                float t1 = (float)((double)sector / 18.0 * Math.PI * 2.0);
                float t2 = (float)((double)(sector + 1) / 18.0 * Math.PI * 2.0);
                float u1 = (float)sector / 18.0f;
                float u2 = (float)(sector + 1) / 18.0f;
                float c1 = (float)Math.cos(t1);
                float s1 = (float)Math.sin(t1);
                float c2 = (float)Math.cos(t2);
                float s2 = (float)Math.sin(t2);
                quads[idx][0] = new SphereVertex(r1 * c1, y1, r1 * s1, u1, v1);
                quads[idx][1] = new SphereVertex(r2 * c1, y2, r2 * s1, u1, v2);
                quads[idx][2] = new SphereVertex(r2 * c2, y2, r2 * s2, u2, v2);
                quads[idx][3] = new SphereVertex(r1 * c2, y1, r1 * s2, u2, v1);
                ++idx;
            }
        }
        return quads;
    }
    private static final class SphereVertex {
        final float x;
        final float y;
        final float z;
        final float u;
        final float v;
        SphereVertex(float x, float y, float z, float u, float v) {
            this.x = x;
            this.y = y;
            this.z = z;
            this.u = u;
            this.v = v;
        }
    }
}
