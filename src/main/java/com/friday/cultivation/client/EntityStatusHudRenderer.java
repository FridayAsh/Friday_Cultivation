package com.friday.cultivation.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
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
import org.joml.Vector3f;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * 生物头顶血条渲染器（GUI 空间版）。
 *
 * 在 GUI 空间（RenderGuiOverlayEvent.Pre，与玩家血条同一 overlay 阶段）渲染，
 * 完全绕开光影包接管的世界渲染管线——光影包对 GUI 渲染有专门处理路径，
 * 因此血条不会像世界空间 Tesselator 立即模式那样被光影重写 blend/alpha 而变透明。
 *
 * 血条贴图与玩家 CultivationHud.renderTextureBar 完全一致（blood_empty 白底条
 * + blood_fill 染深灰底槽 + clip 渐变填充），用 GuiGraphics.blit 绘制。
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

    // 血条基准屏幕尺寸（按 1 格距离换算）
    private static final float BASE_BAR_W = 48.0f;
    private static final float BAR_H = 6.0f;
    private static final float ICON_SIZE = 8.0f;
    private static final float TEXT_SCALE = 0.5f;
    private static final double MAX_DISTANCE = 24.0;
    // 与玩家血条一致：填充条左端固定圆角段在贴图中所占像素（blood_fill 左端圆角宽度）
    private static final int CLIP_PX = 3;

    private EntityStatusHudRenderer() {
    }

    @SubscribeEvent
    public static void onRenderOverlay(RenderGuiOverlayEvent.Pre event) {
        if (event.getOverlay() != VanillaGuiOverlay.PLAYER_HEALTH.type()) {
            return;
        }
        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;
        if (player == null || mc.level == null || mc.options.hideGui) {
            return;
        }
        GuiGraphics graphics = event.getGuiGraphics();
        float partial = mc.getFrameTime();

        AABB box = player.getBoundingBox().inflate(MAX_DISTANCE, MAX_DISTANCE, MAX_DISTANCE);
        for (Entity e : mc.level.getEntities(player, box, EntityStatusHudRenderer::canShowStatus)) {
            LivingEntity living = (LivingEntity) e;
            if (!isVisibleToPlayer(player, living, partial)) {
                continue;
            }
            renderEntityStatus(graphics, player, living, partial);
        }
    }

    private static boolean canShowStatus(Entity e) {
        return e instanceof LivingEntity && e.isAlive();
    }

    /**
     * 视线遮挡判断：从玩家眼睛到生物身体中心做方块射线，若射线先命中方块（命中点比目标点更近），
     * 说明该生物被方块遮挡，玩家看不到，则不渲染其血条。
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

    /**
     * 世界坐标 → 屏幕坐标（相机正交基投影，使用 MC Camera.getLookVector/getUpVector/getLeftVector）。
     * 返回 null 表示在相机后方（不可见）。
     */
    private static Vec2 projectToScreen(Minecraft mc, Vec3 worldPos, float partial) {
        net.minecraft.client.Camera camera = mc.gameRenderer.getMainCamera();
        Vec3 camPos = camera.getPosition();
        org.joml.Vector3f look = camera.getLookVector();
        org.joml.Vector3f up = camera.getUpVector();
        org.joml.Vector3f left = camera.getLeftVector();

        Vec3 rel = worldPos.subtract(camPos);
        double fwdDist = rel.x * look.x + rel.y * look.y + rel.z * look.z;
        if (fwdDist <= 0.1) {
            return null;
        }
        // right = -left
        double rightDist = -(rel.x * left.x + rel.y * left.y + rel.z * left.z);
        double upDist = rel.x * up.x + rel.y * up.y + rel.z * up.z;

        double fov = mc.options.fov().get();
        int guiW = mc.getWindow().getGuiScaledWidth();
        int guiH = mc.getWindow().getGuiScaledHeight();
        double scale = (guiH / 2.0) / Math.tan(Math.toRadians(fov / 2.0));
        int sx = guiW / 2 + (int) Math.round(rightDist / fwdDist * scale);
        int sy = guiH / 2 - (int) Math.round(upDist / fwdDist * scale);
        float dist = (float) fwdDist;
        return new Vec2(sx, sy, dist);
    }

    private static void renderEntityStatus(GuiGraphics graphics, Player player, LivingEntity living, float partial) {
        Minecraft mc = Minecraft.getInstance();
        Vec3 head = living.getPosition(partial).add(0.0, living.getBbHeight() + 0.6, 0.0);
        Vec2 proj = projectToScreen(mc, head, partial);
        if (proj == null) {
            return;
        }
        int sx = proj.x;
        int sy = proj.y;

        float hp = living.getHealth();
        float maxHp = living.getMaxHealth();
        double ratio = maxHp <= 0.0f ? 0.0 : (double) hp / (double) maxHp;

        // 体型自适应宽度 + 距离缩放（1 格距离基准 48px，越远越小）；整体缩小至 65%
        float bodyScale = Math.max(0.5f, Math.min(2.0f, (float) living.getBbWidth() / 0.6f));
        float distScale = (float) (BASE_BAR_W * 0.4 * 0.65 / Math.max(1.0, proj.dist));
        float barW = BASE_BAR_W * bodyScale * distScale;
        float barH = BAR_H * distScale;
        if (barW < 12.0f || barH < 2.0f) {
            return;
        }

        float bx = (float) sx - barW / 2.0f;
        float by = (float) sy - 12.0f;

        // 与玩家血条 renderTextureBar 完全一致的三层贴图结构（GuiGraphics.blit）：
        // ① 底条：blood_empty 整张贴图等比缩放（白色 tint）
        graphics.setColor(1.0f, 1.0f, 1.0f, 1.0f);
        graphics.blit(BLOOD_EMPTY, (int) bx, (int) by, Math.round(barW), Math.round(barH),
                0.0f, 0.0f, 96, 6, 96, 6);
        // ② 内部深黑灰底槽：blood_fill 染 BAR_INNER_BG 铺满全条
        setBarColor(graphics, BAR_INNER_BG);
        graphics.blit(BLOOD_FILL, (int) bx, (int) by, Math.round(barW), Math.round(barH),
                0.0f, 0.0f, 96, 6, 96, 6);
        graphics.setColor(1.0f, 1.0f, 1.0f, 1.0f);

        // ③ 填充：电池护盾 clip（左端固定圆角 + 右侧从贴图尾部滑入）
        int targetW = (int) Math.round(barW * ratio);
        if (targetW > 0) {
            int x0 = (int) bx;
            int y0 = (int) by;
            int w = Math.round(barW);
            int h = Math.round(barH);
            int halfH = Math.max(1, h / 2);
            int clipScreen = Math.max(1, Math.round(barW * CLIP_PX / 96.0f));
            if (targetW >= w) {
                setBarColor(graphics, HEALTH_TOP);
                graphics.blit(BLOOD_FILL, x0, y0, w, halfH, 0.0f, 0.0f, 96, 3, 96, 6);
                setBarColor(graphics, HEALTH_BOTTOM);
                graphics.blit(BLOOD_FILL, x0, y0 + halfH, w, h - halfH, 0.0f, 3.0f, 96, 3, 96, 6);
            } else if (targetW <= clipScreen) {
                setBarColor(graphics, HEALTH_TOP);
                graphics.blit(BLOOD_FILL, x0, y0, targetW, halfH, 0.0f, 0.0f, 96, 3, 96, 6);
                setBarColor(graphics, HEALTH_BOTTOM);
                graphics.blit(BLOOD_FILL, x0, y0 + halfH, targetW, h - halfH, 0.0f, 3.0f, 96, 3, 96, 6);
            } else {
                int rightScreen = targetW - clipScreen;
                setBarColor(graphics, HEALTH_TOP);
                graphics.blit(BLOOD_FILL, x0, y0, clipScreen, halfH, 0.0f, 0.0f, CLIP_PX, 3, 96, 6);
                setBarColor(graphics, HEALTH_BOTTOM);
                graphics.blit(BLOOD_FILL, x0, y0 + halfH, clipScreen, h - halfH, 0.0f, 3.0f, CLIP_PX, 3, 96, 6);
                int rightSrc = Math.max(1, (int) Math.round((double) rightScreen * 96.0 / (double) w));
                setBarColor(graphics, HEALTH_TOP);
                graphics.blit(BLOOD_FILL, x0 + clipScreen, y0, rightScreen, halfH, 96.0f - rightSrc, 0.0f, rightSrc, 3, 96, 6);
                setBarColor(graphics, HEALTH_BOTTOM);
                graphics.blit(BLOOD_FILL, x0 + clipScreen, y0 + halfH, rightScreen, h - halfH, 96.0f - rightSrc, 3.0f, rightSrc, 3, 96, 6);
            }
            graphics.setColor(1.0f, 1.0f, 1.0f, 1.0f);
        }

        // 条内居中显示当前/最大生命值文本（随条缩放）
        Component text = Component.literal(String.format("%.0f/%.0f", hp, maxHp));
        float textScale = 0.5f * distScale;
        if (textScale >= 0.22f) {
            float rawW = mc.font.width(text);
            float scale = Math.min(textScale, barW / rawW);
            float textW = rawW * scale;
            float textX = bx + (barW - textW) / 2.0f;
            float textY = by + (barH - mc.font.lineHeight * scale) / 2.0f;
            graphics.pose().pushPose();
            graphics.pose().translate(textX, textY, 0.0f);
            graphics.pose().scale(scale, scale, 1.0f);
            graphics.drawString(mc.font, text, 0, 0, 0xFFFFFF, true);
            graphics.pose().popPose();
        }

        // 盔甲 / 韧性（水平排列在血条右侧，随条缩放）
        int armor = living.getArmorValue();
        double toughness = living.getAttribute(Attributes.ARMOR_TOUGHNESS).getValue();
        boolean showArmor = armor > 0;
        boolean showToughness = toughness > 0.0;
        if ((showArmor || showToughness) && distScale >= 0.22f) {
            float gx = bx + barW + 2.0f * distScale;
            float gy = by + (barH - ICON_SIZE * distScale) / 2.0f;
            float icon = ICON_SIZE * distScale;
            float gap = 4.0f * distScale;
            if (showArmor) {
                graphics.setColor(1.0f, 1.0f, 1.0f, 1.0f);
                graphics.blit(VANILLA_ICONS, (int) gx, (int) gy, Math.round(icon), Math.round(icon),
                        ARMOR_ICON_U, ARMOR_ICON_V, 9, 9, 256, 256);
                drawScaledText(graphics, mc, Component.literal(String.valueOf(armor)),
                        gx + icon + 1.0f * distScale, gy, 0.5f * distScale, ARMOR_COLOR);
                float armorW = icon + 1.0f * distScale + mc.font.width(Component.literal(String.valueOf(armor))) * 0.5f * distScale;
                gx += armorW + gap;
            }
            if (showToughness) {
                graphics.setColor(1.0f, 1.0f, 1.0f, 1.0f);
                graphics.blit(OVERFLOWING_ICONS, (int) gx, (int) gy, Math.round(icon), Math.round(icon),
                        TOUGH_ICON_U, TOUGH_ICON_V, 9, 9, 256, 256);
                drawScaledText(graphics, mc, Component.literal(String.format("%.0f", toughness)),
                        gx + icon + 1.0f * distScale, gy, 0.5f * distScale, TOUGH_COLOR);
            }
        }
    }

    /** 缩放绘制文本（坐标=缩放前） */
    private static void drawScaledText(GuiGraphics graphics, Minecraft mc, Component text, float x, float y, float scale, int color) {
        if (scale <= 0.0f) {
            return;
        }
        graphics.pose().pushPose();
        graphics.pose().translate(x, y, 0.0f);
        graphics.pose().scale(scale, scale, 1.0f);
        graphics.drawString(mc.font, text, 0, 0, color, true);
        graphics.pose().popPose();
    }

    /** 与玩家 CultivationHud.setBarColor 相同：染贴图颜色（含 alpha） */
    private static void setBarColor(GuiGraphics graphics, int color) {
        float a = ((color >> 24) & 0xFF) / 255.0f;
        float r = ((color >> 16) & 0xFF) / 255.0f;
        float g = ((color >> 8) & 0xFF) / 255.0f;
        float b = (color & 0xFF) / 255.0f;
        graphics.setColor(r, g, b, a);
    }

    /** 屏幕坐标 + 距离 */
    private record Vec2(int x, int y, float dist) {
    }
}
