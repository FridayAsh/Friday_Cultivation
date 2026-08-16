package com.friday.cultivation.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.HashMap;
import java.util.Map;

/**
 * 生物头顶血条渲染器。
 *
 * 渲染管线与玩家血条（CultivationHud.renderTextureBar）保持一致：
 * 使用无光照的 position_tex_color shader（玩家 GuiGraphics.blit 同款），
 * 通过 MultiBufferSource 批次渲染，由 RenderType 统一管理 blend/depth/光照状态，
 * 不继承粒子系统残留的 ColorModulator 或世界光照——因此无论光源如何血条都保持不透明。
 */
@Mod.EventBusSubscriber(modid = "friday_cultivation", value = Dist.CLIENT)
public class EntityStatusHudRenderer {
    private static final ResourceLocation BLOOD_EMPTY = new ResourceLocation("friday_cultivation", "textures/gui/blood_empty.png");
    private static final ResourceLocation BLOOD_FILL = new ResourceLocation("friday_cultivation", "textures/gui/blood_fill.png");
    private static final ResourceLocation VANILLA_ICONS = new ResourceLocation("textures/gui/icons.png");
    private static final ResourceLocation OVERFLOWING_ICONS = new ResourceLocation("friday_cultivation", "textures/gui/overflowing_icons.png");

    private static final int BAR_INNER_BG = 0xFF1A1A1A;
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
    // 与玩家血条一致：填充条左端固定圆角段在贴图中所占像素（blood_fill 左端圆角宽度）
    private static final int CLIP_PX = 3;

    /** 每个纹理一个无光照 RenderType（position_tex_color，与玩家 GuiGraphics.blit 同款 shader） */
    private static final Map<ResourceLocation, RenderType> RENDER_TYPES = new HashMap<>();

    private EntityStatusHudRenderer() {
    }

    /** 访问 RenderStateShard 中 protected 字段用的子类（1.20.1 字段均为 protected） */
    private static final class Shards extends RenderStateShard {
        static final ShaderStateShard POSITION_TEX_COLOR = new ShaderStateShard(GameRenderer::getPositionTexColorShader);
        static final TransparencyStateShard TRANSLUCENT = TRANSLUCENT_TRANSPARENCY;
        static final DepthTestStateShard NO_DEPTH = NO_DEPTH_TEST;
        static final WriteMaskStateShard COLOR = COLOR_WRITE;

        private Shards() {
            super("entity_status_hud_shards", () -> {
            }, () -> {
            });
        }
    }

    /** 构建无光照、无雾、blend 透明、不写深度的 RenderType（与玩家 GUI 血条同管线） */
    private static RenderType renderType(ResourceLocation texture) {
        return RENDER_TYPES.computeIfAbsent(texture, tex -> RenderType.create(
                "friday_cultivation_entity_status",
                com.mojang.blaze3d.vertex.DefaultVertexFormat.POSITION_COLOR_TEX,
                com.mojang.blaze3d.vertex.VertexFormat.Mode.QUADS,
                256, false, false,
                RenderType.CompositeState.builder()
                        .setShaderState(Shards.POSITION_TEX_COLOR)
                        .setTextureState(new RenderStateShard.TextureStateShard(tex, false, false))
                        .setTransparencyState(Shards.TRANSLUCENT)
                        .setDepthTestState(Shards.NO_DEPTH)
                        .setWriteMaskState(Shards.COLOR)
                        .createCompositeState(false)));
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

        AABB box = player.getBoundingBox().inflate(MAX_DISTANCE, MAX_DISTANCE, MAX_DISTANCE);
        for (Entity e : mc.level.getEntities(player, box, EntityStatusHudRenderer::canShowStatus)) {
            LivingEntity living = (LivingEntity) e;
            // 视线遮挡隐藏：玩家看不到（射线先被方块挡住）的生物不显示血条
            if (!isVisibleToPlayer(player, living, partial)) {
                continue;
            }
            renderEntityStatus(event, living, cam, partial);
        }

        mc.renderBuffers().bufferSource().endBatch();
    }

    private static boolean canShowStatus(Entity e) {
        return e instanceof LivingEntity && e.isAlive();
    }

    /**
     * 视线遮挡判断：从玩家眼睛到生物身体中心做方块射线，若射线先命中方块（命中点比目标点更近），
     * 说明该生物被方块遮挡，玩家看不到，则不渲染其血条。只考虑方块遮挡，不因其他实体挡在前面而隐藏。
     */
    private static boolean isVisibleToPlayer(Player player, LivingEntity living, float partial) {
        Vec3 eye = player.getEyePosition(partial);
        Vec3 target = living.getPosition(partial).add(0.0, living.getBbHeight() * 0.5, 0.0);
        ClipContext ctx = new ClipContext(eye, target, ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, player);
        BlockHitResult hit = player.level().clip(ctx);
        if (hit.getType() == HitResult.Type.MISS) {
            return true;
        }
        return hit.getLocation().distanceToSqr(eye) >= target.distanceToSqr(eye) - 1.0E-4;
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

        // 体型自适应：仅宽度按体型等比缩放，高度与玩家血条一致固定（BAR_H=6，避免贴图 1px 透明边占比过大透出天空）
        float bodyScale = Math.max(0.5f, Math.min(2.0f, (float) living.getBbWidth() / 0.6f));
        float barW = BAR_W * bodyScale;
        float barH = BAR_H;

        MultiBufferSource.BufferSource buffers = Minecraft.getInstance().renderBuffers().bufferSource();

        // 与玩家血条 renderTextureBar 完全一致的两层贴图结构（玩家调用什么贴图，这里就用什么贴图）：
        // ① 底条：blood_empty 整张贴图（96x6）等比缩放到目标宽高（白色 tint）
        renderTexturedQuad(buffers, BLOOD_EMPTY, mat, -barW / 2.0f, -10.0f, barW, barH,
                0.0f, 0.0f, 96.0f, 6.0f, 96, 6, 1.0f, 1.0f, 1.0f);
        // ② 内部深黑灰底槽：blood_fill 贴图染 BAR_INNER_BG 铺满全条（玩家同款：fillTex 染深灰铺满）
        renderTexturedQuad(buffers, BLOOD_FILL, mat, -barW / 2.0f, -10.0f, barW, barH,
                0.0f, 0.0f, 96.0f, 6.0f, 96, 6, BAR_INNER_BG);
        // ③ 填充：与玩家 renderTextureBar 相同的电池护盾 clip 逻辑——
        //    左端固定圆角段（采样贴图左端 CLIP_PX 像素等比缩放）+ 右侧从贴图尾部滑入，
        //    不把整张含左右圆角边框的贴图等比缩放到当前宽度（避免多渲染左右边框）
        float filledW = (float) (barW * ratio);
        if (filledW > 0.0f) {
            float clipScreen = Math.max(1.0f, barW * CLIP_PX / 96.0f);
            if (filledW >= barW) {
                // 满/接近满：全宽整图等比缩放（与底槽一致，圆角完整）
                renderTexturedTintedQuad(buffers, BLOOD_FILL, mat, -barW / 2.0f, -10.0f, barW, barH,
                        0.0f, 0.0f, 96.0f, 6.0f, 96, 6, HEALTH_TOP, HEALTH_BOTTOM);
            } else if (filledW <= clipScreen) {
                // 很小：整图等比缩放到 filledW（极小段直接整图缩放）
                renderTexturedTintedQuad(buffers, BLOOD_FILL, mat, -barW / 2.0f, -10.0f, filledW, barH,
                        0.0f, 0.0f, 96.0f, 6.0f, 96, 6, HEALTH_TOP, HEALTH_BOTTOM);
            } else {
                // 左端：采样贴图左端 CLIP_PX 像素，等比缩放到 clipScreen（圆角固定不变形）
                renderTexturedTintedQuad(buffers, BLOOD_FILL, mat, -barW / 2.0f, -10.0f, clipScreen, barH,
                        0.0f, 0.0f, CLIP_PX, 6.0f, 96, 6, HEALTH_TOP, HEALTH_BOTTOM);
                // 右侧：从贴图尾部取 rightScreen/barW 比例像素，等比缩放到 rightScreen（无右端圆角）
                float rightScreen = filledW - clipScreen;
                float rightSrc = Math.max(1.0f, rightScreen * 96.0f / barW);
                renderTexturedTintedQuad(buffers, BLOOD_FILL, mat, -barW / 2.0f + clipScreen, -10.0f, rightScreen, barH,
                        96.0f - rightSrc, 0.0f, rightSrc, 6.0f, 96, 6, HEALTH_TOP, HEALTH_BOTTOM);
            }
        }

        // 条内居中显示当前/最大生命值文本
        renderHealthText(mat, hp, maxHp, barW, barH, bodyScale);

        // 盔甲 / 韧性
        int armor = living.getArmorValue();
        double toughness = living.getAttribute(Attributes.ARMOR_TOUGHNESS).getValue();
        boolean showArmor = armor > 0;
        boolean showToughness = toughness > 0.0;
        if (showArmor || showToughness) {
            renderArmorToughness(event, mat, showArmor, showToughness, armor, toughness, barW);
        }

        pose.popPose();
    }

    private static void renderHealthText(Matrix4f mat, float hp, float maxHp, float barW, float barH, float bodyScale) {
        Minecraft mc = Minecraft.getInstance();
        Component text = Component.literal(String.format("%.0f/%.0f", hp, maxHp));
        float rawW = mc.font.width(text);
        float rawH = mc.font.lineHeight;
        // 文本基础字号随体型缩放，再限制为不超过条宽
        float textScale = TEXT_SCALE * bodyScale;
        if (rawW > 0.0f) {
            textScale = Math.min(textScale, barW / rawW);
        }
        // 字号下限保证可读性（barW 下限 24px 时实际所需比例远高于 0.25，正常不会触发）
        textScale = Math.max(textScale, 0.25f);
        float textW = rawW * textScale;
        float textH = rawH * textScale;
        float textX = -barW / 2.0f + (barW - textW) / 2.0f;
        float textY = -10.0f + (barH - textH) / 2.0f;
        // 文本必须与条同步缩放：drawInBatch 绘制的是原始字号，需把 textScale 乘进矩阵，
        // 并以缩放前坐标系（textX/textScale）传坐标，文本实际尺寸 = 原始字号 * textScale
        Matrix4f textMat = new Matrix4f(mat);
        textMat.scale(textScale, textScale, 1.0f);
        mc.font.drawInBatch(text, textX / textScale, textY / textScale, 0xFFFFFF, true, textMat,
                mc.renderBuffers().bufferSource(), Font.DisplayMode.NORMAL, 0, 15728880);
    }

    private static void renderArmorToughness(RenderLevelStageEvent event, Matrix4f mat, boolean showArmor, boolean showToughness, int armor, double toughness, float barW) {
        Minecraft mc = Minecraft.getInstance();
        Component armorText = Component.literal(String.valueOf(armor));
        Component toughText = Component.literal(String.format("%.0f", toughness));

        float armorW = showArmor ? ICON_SIZE + 1.0f + mc.font.width(armorText) * TEXT_SCALE : 0.0f;
        float toughW = showToughness ? ICON_SIZE + 1.0f + mc.font.width(toughText) * TEXT_SCALE : 0.0f;
        float gap = 4.0f;
        // 盔甲 → 韧性 水平排列在血条右侧（与玩家 renderAttributeRow 一致：生命条右侧同 y）
        float gx = barW / 2.0f + 2.0f;
        float gy = -10.0f + (BAR_H - ICON_SIZE) / 2.0f;

        if (showArmor) {
            renderIconValue(event, mat, gx, gy, VANILLA_ICONS, ARMOR_ICON_U, ARMOR_ICON_V, ARMOR_COLOR, armorText);
            gx += armorW + gap;
        }
        if (showToughness) {
            renderIconValue(event, mat, gx, gy, OVERFLOWING_ICONS, TOUGH_ICON_U, TOUGH_ICON_V, TOUGH_COLOR, toughText);
        }
    }

    private static void renderIconValue(RenderLevelStageEvent event, Matrix4f mat, float x, float y, ResourceLocation texture, int u, int v, int color, Component text) {
        Minecraft mc = Minecraft.getInstance();
        renderTexturedQuad(mc.renderBuffers().bufferSource(), texture, mat, x, y, ICON_SIZE, ICON_SIZE,
                (float) u, (float) v, 9.0f, 9.0f, 256, 256,
                ((color >> 16) & 0xFF) / 255.0f,
                ((color >> 8) & 0xFF) / 255.0f,
                (color & 0xFF) / 255.0f);
        // 文本按 TEXT_SCALE 矩阵缩放绘制（与玩家 drawScaled 一致），实际宽度 = font.width * TEXT_SCALE，
        // 与 renderArmorToughness 的宽度计算匹配，避免间隔重叠
        float rawW = mc.font.width(text);
        Matrix4f textMat = new Matrix4f(mat);
        textMat.scale(TEXT_SCALE, TEXT_SCALE, 1.0f);
        float tx = (x + ICON_SIZE + 1.0f) / TEXT_SCALE;
        float ty = (y + 1.0f) / TEXT_SCALE;
        mc.font.drawInBatch(text, tx, ty, color, true, textMat,
                mc.renderBuffers().bufferSource(), Font.DisplayMode.NORMAL, 0, 15728880);
    }

    /** 无光照贴图 quad（position_tex_color，与玩家 blit 同管线） */
    private static void renderTexturedQuad(MultiBufferSource.BufferSource buffers, ResourceLocation texture, Matrix4f mat,
                                           float x, float y, float w, float h,
                                           float u, float v, float texW, float texH, int texWidth, int texHeight,
                                           float cr, float cg, float cb) {
        VertexConsumer consumer = buffers.getBuffer(renderType(texture));
        float u0 = u / texWidth;
        float v0 = v / texHeight;
        float u1 = (u + texW) / texWidth;
        float v1 = (v + texH) / texHeight;
        consumer.vertex(mat, x, y, 0.0f).color(cr, cg, cb, 1.0f).uv(u0, v0).endVertex();
        consumer.vertex(mat, x, y + h, 0.0f).color(cr, cg, cb, 1.0f).uv(u0, v1).endVertex();
        consumer.vertex(mat, x + w, y + h, 0.0f).color(cr, cg, cb, 1.0f).uv(u1, v1).endVertex();
        consumer.vertex(mat, x + w, y, 0.0f).color(cr, cg, cb, 1.0f).uv(u1, v0).endVertex();
    }

    private static void renderTexturedQuad(MultiBufferSource.BufferSource buffers, ResourceLocation texture, Matrix4f mat,
                                           float x, float y, float w, float h,
                                           float u, float v, float texW, float texH, int texWidth, int texHeight,
                                           int color) {
        float r = ((color >> 16) & 0xFF) / 255.0f;
        float g = ((color >> 8) & 0xFF) / 255.0f;
        float b = (color & 0xFF) / 255.0f;
        renderTexturedQuad(buffers, texture, mat, x, y, w, h, u, v, texW, texH, texWidth, texHeight, r, g, b);
    }

    /** 上下两半渐变贴图 quad（顶色/底色），无光照 */
    private static void renderTexturedTintedQuad(MultiBufferSource.BufferSource buffers, ResourceLocation texture, Matrix4f mat,
                                                 float x, float y, float w, float h,
                                                 float u, float v, float texW, float texH, int texWidth, int texHeight,
                                                 int topColor, int bottomColor) {
        VertexConsumer consumer = buffers.getBuffer(renderType(texture));
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

        float midY = y + h / 2.0f;
        // 上半（顶色）
        consumer.vertex(mat, x, y, 0.0f).color(tr, tg, tb, 1.0f).uv(u0, v0).endVertex();
        consumer.vertex(mat, x, midY, 0.0f).color(tr, tg, tb, 1.0f).uv(u0, vMid).endVertex();
        consumer.vertex(mat, x + w, midY, 0.0f).color(tr, tg, tb, 1.0f).uv(u1, vMid).endVertex();
        consumer.vertex(mat, x + w, y, 0.0f).color(tr, tg, tb, 1.0f).uv(u1, v0).endVertex();
        // 下半（底色）
        consumer.vertex(mat, x, midY, 0.0f).color(br, bg, bb, 1.0f).uv(u0, vMid).endVertex();
        consumer.vertex(mat, x, y + h, 0.0f).color(br, bg, bb, 1.0f).uv(u0, v1).endVertex();
        consumer.vertex(mat, x + w, y + h, 0.0f).color(br, bg, bb, 1.0f).uv(u1, v1).endVertex();
        consumer.vertex(mat, x + w, midY, 0.0f).color(br, bg, bb, 1.0f).uv(u1, vMid).endVertex();
    }
}
