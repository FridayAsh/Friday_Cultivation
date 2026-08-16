package com.friday.cultivation.client;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = "friday_cultivation", value = Dist.CLIENT)
public class EntityStatusHudRenderer {
    private static final ResourceLocation BLOOD_EMPTY = new ResourceLocation("friday_cultivation", "textures/gui/blood_empty.png");
    private static final ResourceLocation BLOOD_FILL = new ResourceLocation("friday_cultivation", "textures/gui/blood_fill.png");
    private static final ResourceLocation VANILLA_ICONS = new ResourceLocation("textures/gui/icons.png");
    private static final ResourceLocation OVERFLOWING_ICONS = new ResourceLocation("friday_cultivation", "textures/gui/overflowing_icons.png");

    private static final int BAR_BG = 0xFF1A1A1A;
    private static final int ARMOR_ICON_U = 34, ARMOR_ICON_V = 9;
    private static final int TOUGH_ICON_U = 18, TOUGH_ICON_V = 0;
    private static final int ARMOR_COLOR = 0xAAAAAA;
    private static final int TOUGH_COLOR = 0x40E0D0;
    private static final int HEALTH_TOP = -1944235;
    private static final int HEALTH_BOTTOM = -5758944;

    private static final float BAR_W = 48.0f;
    private static final float BAR_H = 6.0f;
    private static final float ICON_SIZE = 8.0f;
    private static final float TEXT_SCALE = 0.5f;
    private static final float WORLD_SCALE = -0.025f;
    private static final double MAX_DISTANCE = 24.0;

    private EntityStatusHudRenderer() {
    }

    @SubscribeEvent
    public static void onRenderLevelStage(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_PARTICLES) {
            return;
        }
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.player == null || mc.options.hideGui) {
            return;
        }
        Player player = mc.player;
        Vec3 cam = event.getCamera().getPosition();
        float partial = event.getPartialTick();

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableDepthTest();
        RenderSystem.depthMask(false);

        AABB box = player.getBoundingBox().inflate(MAX_DISTANCE, MAX_DISTANCE, MAX_DISTANCE);
        for (Entity e : mc.level.getEntities(player, box, EntityStatusHudRenderer::canShowStatus)) {
            LivingEntity living = (LivingEntity) e;
            renderEntityStatus(event, living, cam, partial);
        }

        mc.renderBuffers().bufferSource().endBatch();
        RenderSystem.depthMask(true);
        RenderSystem.enableDepthTest();
        RenderSystem.disableBlend();
    }

    private static boolean canShowStatus(Entity e) {
        return e instanceof LivingEntity && e.isAlive();
    }

    private static void renderEntityStatus(RenderLevelStageEvent event, LivingEntity living, Vec3 cam, float partial) {
        PoseStack pose = event.getPoseStack();
        pose.pushPose();

        Vec3 pos = living.getPosition(partial).add(0.0, living.getBbHeight() + 0.6, 0.0);
        pose.translate(-cam.x, -cam.y, -cam.z);
        pose.translate(pos.x, pos.y, pos.z);
        // 标准 billboard：与实体名牌（EntityRenderer.renderNameTag）相同的相机朝向四元数，
        // 任何观察角度下平面均正对相机。负缩放（X/Y 双负）仅翻转面朝向，与原版名牌
        // scale(-0.025F, -0.025F, 0.025F) 一致，不产生贴图/文字镜像，故保持 WORLD_SCALE = -0.025f。
        pose.mulPose(Minecraft.getInstance().getEntityRenderDispatcher().cameraOrientation());
        pose.scale(WORLD_SCALE, WORLD_SCALE, 1.0f);

        Matrix4f mat = pose.last().pose();

        float hp = living.getHealth();
        float maxHp = living.getMaxHealth();
        double ratio = maxHp <= 0.0f ? 0.0 : (double) hp / (double) maxHp;

        // 按体型等比缩放条长（标准玩家宽 0.6f 为基准，clamp 0.5~2.0），高度随缩放微调以容纳条内文本
        float bodyScale = Math.max(0.5f, Math.min(2.0f, (float) living.getBbWidth() / 0.6f));
        float barW = BAR_W * bodyScale;
        float barH = BAR_H * bodyScale;

        // 深黑灰背景
        renderSolidQuad(mat, -barW / 2.0f - 1.0f, -11.0f, barW + 2.0f, barH + 2.0f, BAR_BG);
        // 空血条
        renderTexturedQuad(mat, BLOOD_EMPTY, -barW / 2.0f, -10.0f, barW, barH,
                0.0f, 0.0f, 96.0f, 6.0f, 96, 6, 1.0f, 1.0f, 1.0f);
        // 血条填充
        float filledW = (float) (barW * ratio);
        if (filledW > 0.0f) {
            renderTexturedTintedQuad(mat, BLOOD_FILL, -barW / 2.0f, -10.0f, filledW, barH,
                    0.0f, 0.0f, 96.0f, 6.0f, 96, 6, HEALTH_TOP, HEALTH_BOTTOM);
        }

        // 条内居中显示当前/最大生命值文本
        renderHealthText(mat, hp, maxHp, barW, barH, bodyScale);

        // 盔甲 / 韧性
        int armor = living.getArmorValue();
        double toughness = living.getAttribute(Attributes.ARMOR_TOUGHNESS).getValue();
        boolean showArmor = armor > 0;
        boolean showToughness = toughness > 0.0;
        if (showArmor || showToughness) {
            renderArmorToughness(event, mat, showArmor, showToughness, armor, toughness);
        }

        pose.popPose();
    }

    private static void renderHealthText(Matrix4f mat, float hp, float maxHp, float barW, float barH, float bodyScale) {
        Minecraft mc = Minecraft.getInstance();
        Component text = Component.literal(String.format("%.0f/%.0f", hp, maxHp));
        float textScale = TEXT_SCALE * bodyScale;
        float textW = mc.font.width(text) * textScale;
        float textH = mc.font.lineHeight * textScale;
        float textX = -barW / 2.0f + (barW - textW) / 2.0f;
        float textY = -10.0f + (barH - textH) / 2.0f;
        mc.font.drawInBatch(text, textX, textY, 0xFFFFFF, true, mat,
                mc.renderBuffers().bufferSource(), Font.DisplayMode.NORMAL, 0, 15728880);
    }

    private static void renderArmorToughness(RenderLevelStageEvent event, Matrix4f mat, boolean showArmor, boolean showToughness, int armor, double toughness) {
        Minecraft mc = Minecraft.getInstance();
        Component armorText = Component.literal(String.valueOf(armor));
        Component toughText = Component.literal(String.format("%.0f", toughness));

        float armorW = showArmor ? ICON_SIZE + 1.0f + mc.font.width(armorText) * TEXT_SCALE : 0.0f;
        float toughW = showToughness ? ICON_SIZE + 1.0f + mc.font.width(toughText) * TEXT_SCALE : 0.0f;
        float gap = 4.0f;
        float totalW = armorW + ((showArmor && showToughness) ? gap : 0.0f) + toughW;
        float gx = -totalW / 2.0f;
        float gy = -22.0f;

        if (showArmor) {
            renderIconValue(event, mat, gx, gy, VANILLA_ICONS, ARMOR_ICON_U, ARMOR_ICON_V, ARMOR_COLOR, armorText);
            gx += armorW + gap;
        }
        if (showToughness) {
            renderIconValue(event, mat, gx, gy, OVERFLOWING_ICONS, TOUGH_ICON_U, TOUGH_ICON_V, TOUGH_COLOR, toughText);
        }
    }

    private static void renderIconValue(RenderLevelStageEvent event, Matrix4f mat, float x, float y, ResourceLocation texture, int u, int v, int color, Component text) {
        renderTexturedQuad(mat, texture, x, y, ICON_SIZE, ICON_SIZE,
                (float) u, (float) v, 9.0f, 9.0f, 256, 256,
                ((color >> 16) & 0xFF) / 255.0f,
                ((color >> 8) & 0xFF) / 255.0f,
                (color & 0xFF) / 255.0f);
        Minecraft.getInstance().font.drawInBatch(text, x + ICON_SIZE + 1.0f, y + 1.0f, color, true, mat,
                Minecraft.getInstance().renderBuffers().bufferSource(), Font.DisplayMode.NORMAL, 0, 15728880);
    }

    private static void renderSolidQuad(Matrix4f mat, float x, float y, float w, float h, int color) {
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        float a = ((color >> 24) & 0xFF) / 255.0f;
        float r = ((color >> 16) & 0xFF) / 255.0f;
        float g = ((color >> 8) & 0xFF) / 255.0f;
        float b = (color & 0xFF) / 255.0f;
        Tesselator t = Tesselator.getInstance();
        BufferBuilder buffer = t.getBuilder();
        buffer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
        buffer.vertex(mat, x, y, 0.0f).color(r, g, b, a).endVertex();
        buffer.vertex(mat, x, y + h, 0.0f).color(r, g, b, a).endVertex();
        buffer.vertex(mat, x + w, y + h, 0.0f).color(r, g, b, a).endVertex();
        buffer.vertex(mat, x + w, y, 0.0f).color(r, g, b, a).endVertex();
        t.end();
    }

    private static void renderTexturedQuad(Matrix4f mat, ResourceLocation texture, float x, float y, float w, float h,
                                           float u, float v, float texW, float texH, int texWidth, int texHeight,
                                           float cr, float cg, float cb) {
        RenderSystem.setShaderTexture(0, texture);
        RenderSystem.setShader(GameRenderer::getPositionTexColorShader);
        float u0 = u / texWidth;
        float v0 = v / texHeight;
        float u1 = (u + texW) / texWidth;
        float v1 = (v + texH) / texHeight;
        Tesselator t = Tesselator.getInstance();
        BufferBuilder buffer = t.getBuilder();
        buffer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);
        buffer.vertex(mat, x, y, 0.0f).uv(u0, v0).color(cr, cg, cb, 1.0f).endVertex();
        buffer.vertex(mat, x, y + h, 0.0f).uv(u0, v1).color(cr, cg, cb, 1.0f).endVertex();
        buffer.vertex(mat, x + w, y + h, 0.0f).uv(u1, v1).color(cr, cg, cb, 1.0f).endVertex();
        buffer.vertex(mat, x + w, y, 0.0f).uv(u1, v0).color(cr, cg, cb, 1.0f).endVertex();
        t.end();
    }

    private static void renderTexturedTintedQuad(Matrix4f mat, ResourceLocation texture, float x, float y, float w, float h,
                                                float u, float v, float texW, float texH, int texWidth, int texHeight,
                                                int topColor, int bottomColor) {
        RenderSystem.setShaderTexture(0, texture);
        RenderSystem.setShader(GameRenderer::getPositionTexColorShader);
        float u0 = u / texWidth;
        float u1 = (u + texW) / texWidth;
        float v0 = v / texHeight;
        float vMid = (v + texH / 2.0f) / texHeight;
        float v1 = (v + texH) / texHeight;

        float tr = ((topColor >> 16) & 0xFF) / 255.0f;
        float tg = ((topColor >> 8) & 0xFF) / 255.0f;
        float tb = (topColor & 0xFF) / 255.0f;
        float br = ((bottomColor >> 16) & 0xFF) / 255.0f;
        float bg = ((bottomColor >> 8) & 0xFF) / 255.0f;
        float bb = (bottomColor & 0xFF) / 255.0f;

        Tesselator t = Tesselator.getInstance();
        BufferBuilder buffer = t.getBuilder();
        buffer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);
        float midY = y + h / 2.0f;
        // 上半（顶色）
        buffer.vertex(mat, x, y, 0.0f).uv(u0, v0).color(tr, tg, tb, 1.0f).endVertex();
        buffer.vertex(mat, x, midY, 0.0f).uv(u0, vMid).color(tr, tg, tb, 1.0f).endVertex();
        buffer.vertex(mat, x + w, midY, 0.0f).uv(u1, vMid).color(tr, tg, tb, 1.0f).endVertex();
        buffer.vertex(mat, x + w, y, 0.0f).uv(u1, v0).color(tr, tg, tb, 1.0f).endVertex();
        // 下半（底色）
        buffer.vertex(mat, x, midY, 0.0f).uv(u0, vMid).color(br, bg, bb, 1.0f).endVertex();
        buffer.vertex(mat, x, y + h, 0.0f).uv(u0, v1).color(br, bg, bb, 1.0f).endVertex();
        buffer.vertex(mat, x + w, y + h, 0.0f).uv(u1, v1).color(br, bg, bb, 1.0f).endVertex();
        buffer.vertex(mat, x + w, midY, 0.0f).uv(u1, vMid).color(br, bg, bb, 1.0f).endVertex();
        t.end();
    }
}
