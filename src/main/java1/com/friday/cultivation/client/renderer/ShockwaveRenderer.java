package com.friday.cultivation.client.renderer;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.friday.cultivation.entity.spell.ShockwaveEntity;
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
public class ShockwaveRenderer
extends EntityRenderer<ShockwaveEntity> {
    private static final ResourceLocation WHITE_TEXTURE = new ResourceLocation("textures/misc/white.png");
    private static final int STACKS = 12;
    private static final int SECTORS = 18;
    private static final SphereVertex[][] SPHERE_QUADS = ShockwaveRenderer.buildSphere();
    public ShockwaveRenderer(EntityRendererProvider.Context ctx) {
        super(ctx);
        this.shadowRadius = 0.0f;
    }
    public void render(@NotNull ShockwaveEntity entity, float yaw, float partialTick, @NotNull PoseStack pose, @NotNull MultiBufferSource buf, int light) {
        float fade;
        float radius = entity.radius();
        if (radius <= 0.01f) {
            return;
        }
        float progress = entity.visualProgress();
        float baseAlpha = 0.6f * (1.0f - progress * 0.7f);
        float finalAlpha = baseAlpha * (1.0f - (fade = entity.fadeProgress()));
        if (finalAlpha < 0.01f) {
            return;
        }
        int alphaByte = Math.max(0, Math.min(255, (int)(finalAlpha * 255.0f)));
        pose.pushPose();
        pose.scale(radius * 2.0f, radius * 2.0f, radius * 2.0f);
        RenderSystem.enableBlend();
        VertexConsumer vc = buf.getBuffer(RenderType.entityCutoutNoCull((ResourceLocation)WHITE_TEXTURE));
        Matrix4f m = pose.last().pose();
        Matrix3f n = pose.last().normal();
        SphereVertex[][] sphereVertexArray = SPHERE_QUADS;
        int n2 = sphereVertexArray.length;
        for (int i = 0; i < n2; ++i) {
            SphereVertex[] quad;
            for (SphereVertex v : quad = sphereVertexArray[i]) {
                vc.vertex(m, v.x, v.y, v.z).color(255, 255, 255, alphaByte).uv(v.u, v.v).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(0xF000F0).normal(n, v.x, v.y, v.z).endVertex();
            }
        }
        RenderSystem.disableBlend();
        pose.popPose();
        super.render(entity, yaw, partialTick, pose, buf, light);
    }
    @NotNull
    public ResourceLocation getTextureLocation(@NotNull ShockwaveEntity entity) {
        return WHITE_TEXTURE;
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
