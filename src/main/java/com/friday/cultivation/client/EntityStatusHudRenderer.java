package com.friday.cultivation.client;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import net.minecraft.Util;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderGuiEvent;
import net.minecraftforge.client.event.RenderNameTagEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;

/**
 * 生物头顶状态牌渲染器。
 *
 * <p>世界阶段只从实体实际 PoseStack 与当前投影矩阵捕获头顶锚点；最终贴图和字体在
 * {@link RenderGuiEvent.Post} 中绘制。这样仍使用真实世界投影和正常透视，同时避开
 * iterationT 等光影对世界材质施加的昼夜光照、色温、自动曝光和后处理。</p>
 */
@Mod.EventBusSubscriber(modid = "friday_cultivation", value = Dist.CLIENT)
public final class EntityStatusHudRenderer {
    private static final ResourceLocation BLOOD_EMPTY = new ResourceLocation(
            "friday_cultivation", "textures/gui/blood_empty.png");
    private static final ResourceLocation BLOOD_FILL = new ResourceLocation(
            "friday_cultivation", "textures/gui/blood_fill.png");
    private static final ResourceLocation VANILLA_ICONS = new ResourceLocation(
            "textures/gui/icons.png");
    private static final ResourceLocation OVERFLOWING_ICONS = new ResourceLocation(
            "friday_cultivation", "textures/gui/overflowing_icons.png");

    private static final int BAR_INNER_BG = 0xFF1A1A1A;
    private static final int ARMOR_ICON_U = 34;
    private static final int ARMOR_ICON_V = 9;
    private static final int TOUGH_ICON_U = 18;
    private static final int TOUGH_ICON_V = 0;
    private static final int ARMOR_COLOR = 0xFFAAAAAA;
    private static final int TOUGH_COLOR = 0xFF40E0D0;
    private static final int HEALTH_TOP = -1944235;
    private static final int HEALTH_BOTTOM = -5758944;

    private static final double MAX_DISTANCE = 24.0D;
    private static final double MAX_DISTANCE_SQUARED = MAX_DISTANCE * MAX_DISTANCE;
    private static final double HEAD_ANCHOR_OFFSET = 0.6D;
    private static final long HURT_SHOW_TICKS = 60L;
    private static final long HURT_DETECT_COOLDOWN_TICKS = 20L;
    private static final long TRACKING_EXPIRE_TICKS = 40L;

    private static final Map<UUID, HealthTrack> HEALTH_TRACKS = new HashMap<>();
    private static final Map<UUID, PendingPlate> PENDING_PLATES = new HashMap<>();
    private static final HudBarAnimator ANIMATOR = new HudBarAnimator();
    private static final ShadowPassGuard SHADOW_PASS_GUARD = ShadowPassGuard.create();
    private static Object trackedLevel;

    private EntityStatusHudRenderer() {
    }

    /** 在客户端 tick 更新生命基线和动画目标，保证首次受伤也能产生拖影。 */
    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }

        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.player == null) {
            clearTracking();
            trackedLevel = null;
            return;
        }
        if (trackedLevel != mc.level) {
            clearTracking();
            trackedLevel = mc.level;
        }

        Player player = mc.player;
        long nowTick = mc.level.getGameTime();
        long nowMillis = Util.getMillis();
        Set<UUID> seen = new HashSet<>();
        AABB broadphase = player.getBoundingBox().inflate(MAX_DISTANCE);

        for (Entity entity : mc.level.getEntities(player, broadphase, EntityStatusHudRenderer::canTrack)) {
            LivingEntity living = (LivingEntity) entity;
            if (player.distanceToSqr(living) > MAX_DISTANCE_SQUARED) {
                continue;
            }

            UUID id = living.getUUID();
            seen.add(id);
            float health = living.getHealth();
            HealthTrack track = HEALTH_TRACKS.get(id);
            if (track == null) {
                track = new HealthTrack(health, nowTick);
                HEALTH_TRACKS.put(id, track);
            } else {
                if (nowTick - track.firstSeenTick >= HURT_DETECT_COOLDOWN_TICKS
                        && health < track.health - 0.01F) {
                    track.hurtUntilTick = nowTick + HURT_SHOW_TICKS;
                }
                track.health = health;
            }
            track.lastSeenTick = nowTick;

            ANIMATOR.sample(id, HudBarAnimator.BarId.HEALTH,
                    health, living.getMaxHealth(), 0L, nowMillis);
        }

        HEALTH_TRACKS.entrySet().removeIf(entry -> {
            HealthTrack track = entry.getValue();
            if (nowTick - track.lastSeenTick > TRACKING_EXPIRE_TICKS || !seen.contains(entry.getKey())) {
                ANIMATOR.reset(entry.getKey());
                PENDING_PLATES.remove(entry.getKey());
                return true;
            }
            return false;
        });
    }

    /**
     * 实体世界渲染阶段只捕获真实矩阵投影结果，不在光影世界材质管线中绘制最终像素。
     */
    @SubscribeEvent
    public static void onRenderNameTag(RenderNameTagEvent event) {
        if (!(event.getEntity() instanceof LivingEntity living)) {
            return;
        }

        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;
        if (player == null || mc.level == null || mc.options.hideGui
                || SHADOW_PASS_GUARD.isRenderingShadowPass()
                || living == player || !living.isAlive()) {
            return;
        }

        HealthTrack track = HEALTH_TRACKS.get(living.getUUID());
        long nowTick = mc.level.getGameTime();
        if (track == null || nowTick >= track.hurtUntilTick
                || player.distanceToSqr(living) > MAX_DISTANCE_SQUARED
                || !isVisibleToPlayer(player, living, event.getPartialTick())) {
            return;
        }

        Vec3 anchor = healthBarAnchor(living, event.getPartialTick());
        if (computeLayout(mc, anchor) == null) {
            return;
        }
        EntityStatusScreenProjection.Projected projected = projectPlate(event, living, mc);
        if (projected == null) {
            return;
        }

        HudBarAnimator.Visual visual = ANIMATOR.sample(living.getUUID(), HudBarAnimator.BarId.HEALTH,
                living.getHealth(), living.getMaxHealth(), 0L, Util.getMillis());
        AttributeInstance toughnessAttribute = living.getAttribute(Attributes.ARMOR_TOUGHNESS);
        double toughness = toughnessAttribute == null ? 0.0D : toughnessAttribute.getValue();
        PENDING_PLATES.put(living.getUUID(), new PendingPlate(projected,
                living.getHealth(), living.getMaxHealth(), visual.primaryRatio(), visual.trailingRatio(),
                living.getArmorValue(), toughness));
    }

    /** 在所有世界光影与 HUD 合成完成后绘制固定颜色状态牌。 */
    @SubscribeEvent
    public static void onRenderGui(RenderGuiEvent.Post event) {
        if (PENDING_PLATES.isEmpty()) {
            return;
        }

        List<PendingPlate> plates = new ArrayList<>(PENDING_PLATES.values());
        PENDING_PLATES.clear();

        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.player == null || mc.options.hideGui) {
            return;
        }

        plates.sort(Comparator.comparingDouble((PendingPlate plate) -> plate.projected().depth()).reversed());
        GuiGraphics graphics = event.getGuiGraphics();
        graphics.flush();
        RenderSystem.disableDepthTest();
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        try {
            for (PendingPlate plate : plates) {
                renderProjectedStatus(graphics, mc.font, plate);
            }
            graphics.flush();
        } finally {
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
            RenderSystem.disableBlend();
            RenderSystem.enableDepthTest();
        }
    }

    private static boolean canTrack(Entity entity) {
        return entity instanceof LivingEntity living && living.isAlive();
    }

    private static void clearTracking() {
        HEALTH_TRACKS.clear();
        PENDING_PLATES.clear();
        ANIMATOR.reset();
    }

    private static Vec3 healthBarAnchor(LivingEntity living, float partialTick) {
        return living.getPosition(partialTick).add(0.0D,
                living.getBbHeight() + HEAD_ANCHOR_OFFSET, 0.0D);
    }

    private static EntityStatusPlateLayout.Layout computeLayout(Minecraft mc, Vec3 anchor) {
        Camera camera = mc.gameRenderer.getMainCamera();
        Vec3 delta = anchor.subtract(camera.getPosition());
        Vector3f look = camera.getLookVector();
        double depth = delta.x * look.x() + delta.y * look.y() + delta.z * look.z();
        return EntityStatusPlateLayout.compute(depth);
    }

    private static EntityStatusScreenProjection.Projected projectPlate(
            RenderNameTagEvent event, LivingEntity living, Minecraft mc) {
        Vector4f clip = new Vector4f(0.0F,
                living.getBbHeight() + (float) HEAD_ANCHOR_OFFSET, 0.0F, 1.0F);
        event.getPoseStack().last().pose().transform(clip);

        Matrix4f projection = new Matrix4f(RenderSystem.getProjectionMatrix());
        projection.transform(clip);
        return EntityStatusScreenProjection.project(clip.x, clip.y, clip.w,
                Math.abs(projection.m11()), mc.getWindow().getGuiScaledWidth(),
                mc.getWindow().getGuiScaledHeight());
    }

    /** 玩家眼睛到生物身体中心之间若先命中方块，则隐藏状态牌。 */
    private static boolean isVisibleToPlayer(Player player, LivingEntity living, float partialTick) {
        Vec3 eye = player.getEyePosition(partialTick);
        Vec3 target = living.getPosition(partialTick).add(0.0D,
                living.getBbHeight() * 0.5D, 0.0D);
        ClipContext context = new ClipContext(eye, target,
                ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, player);
        BlockHitResult hit = player.level().clip(context);
        if (hit.getType() == HitResult.Type.MISS) {
            return true;
        }
        return hit.getLocation().distanceToSqr(eye)
                >= target.distanceToSqr(eye) - 1.0E-4D;
    }

    private static void renderProjectedStatus(GuiGraphics graphics, Font font, PendingPlate plate) {
        EntityStatusScreenProjection.Projected projected = plate.projected();
        PoseStack pose = graphics.pose();
        pose.pushPose();
        pose.translate(projected.screenX(), projected.screenY(), 0.0F);
        pose.scale(projected.localScale(), projected.localScale(), 1.0F);

        float barLeft = -EntityStatusPlateLayout.BAR_WIDTH_PIXELS * 0.5F;
        float barTop = -EntityStatusPlateLayout.BAR_HEAD_GAP_PIXELS
                - EntityStatusPlateLayout.BAR_HEIGHT_PIXELS;
        float barRight = EntityStatusPlateLayout.BAR_WIDTH_PIXELS * 0.5F;
        float barBottom = -EntityStatusPlateLayout.BAR_HEAD_GAP_PIXELS;

        drawTextureQuad(graphics, BLOOD_EMPTY, barLeft, barTop, barRight, barBottom,
                0.0F, 0.0F, EntityStatusPlateLayout.TEXTURE_WIDTH,
                EntityStatusPlateLayout.TEXTURE_HEIGHT, 0xFFFFFFFF,
                EntityStatusPlateLayout.TEXTURE_WIDTH, EntityStatusPlateLayout.TEXTURE_HEIGHT);
        drawTextureQuad(graphics, BLOOD_FILL, barLeft, barTop, barRight, barBottom,
                0.0F, 0.0F, EntityStatusPlateLayout.TEXTURE_WIDTH,
                EntityStatusPlateLayout.TEXTURE_HEIGHT, BAR_INNER_BG,
                EntityStatusPlateLayout.TEXTURE_WIDTH, EntityStatusPlateLayout.TEXTURE_HEIGHT);

        double primary = EntityStatusPlateLayout.clampRatio(plate.primaryRatio());
        double trailing = Math.max(primary,
                EntityStatusPlateLayout.clampRatio(plate.trailingRatio()));
        if (trailing > primary + 0.0001D) {
            drawFill(graphics, trailing,
                    scaleColor(HEALTH_TOP, 0.58D), scaleColor(HEALTH_BOTTOM, 0.58D));
        }
        drawFill(graphics, primary, HEALTH_TOP, HEALTH_BOTTOM);

        drawAttributes(graphics, font, plate, barTop, barRight);
        drawHealthText(graphics, font, plate, barTop);
        graphics.flush();
        pose.popPose();
    }

    private static void drawFill(GuiGraphics graphics, double ratio,
                                 int topColor, int bottomColor) {
        float width = EntityStatusPlateLayout.BAR_WIDTH_PIXELS
                * EntityStatusPlateLayout.clampRatio(ratio);
        if (width <= 0.0F) {
            return;
        }

        float left = -EntityStatusPlateLayout.BAR_WIDTH_PIXELS * 0.5F;
        float top = -EntityStatusPlateLayout.BAR_HEAD_GAP_PIXELS
                - EntityStatusPlateLayout.BAR_HEIGHT_PIXELS;
        float bottom = -EntityStatusPlateLayout.BAR_HEAD_GAP_PIXELS;
        float half = EntityStatusPlateLayout.BAR_HEIGHT_PIXELS * 0.5F;
        float fullWidth = EntityStatusPlateLayout.BAR_WIDTH_PIXELS;
        float cap = fullWidth * EntityStatusPlateLayout.CLIP_TEXTURE_PIXELS
                / EntityStatusPlateLayout.TEXTURE_WIDTH;

        if (width <= cap) {
            drawTextureQuad(graphics, BLOOD_FILL, left, top, left + width, top + half,
                    0.0F, 0.0F, EntityStatusPlateLayout.TEXTURE_WIDTH, 3.0F,
                    topColor, EntityStatusPlateLayout.TEXTURE_WIDTH,
                    EntityStatusPlateLayout.TEXTURE_HEIGHT);
            drawTextureQuad(graphics, BLOOD_FILL, left, top + half, left + width, bottom,
                    0.0F, 3.0F, EntityStatusPlateLayout.TEXTURE_WIDTH,
                    EntityStatusPlateLayout.TEXTURE_HEIGHT, bottomColor,
                    EntityStatusPlateLayout.TEXTURE_WIDTH, EntityStatusPlateLayout.TEXTURE_HEIGHT);
            return;
        }

        float capWidth = Math.min(cap, width);
        drawTextureQuad(graphics, BLOOD_FILL, left, top, left + capWidth, top + half,
                0.0F, 0.0F, EntityStatusPlateLayout.CLIP_TEXTURE_PIXELS, 3.0F,
                topColor, EntityStatusPlateLayout.TEXTURE_WIDTH,
                EntityStatusPlateLayout.TEXTURE_HEIGHT);
        drawTextureQuad(graphics, BLOOD_FILL, left, top + half, left + capWidth, bottom,
                0.0F, 3.0F, EntityStatusPlateLayout.CLIP_TEXTURE_PIXELS,
                EntityStatusPlateLayout.TEXTURE_HEIGHT, bottomColor,
                EntityStatusPlateLayout.TEXTURE_WIDTH, EntityStatusPlateLayout.TEXTURE_HEIGHT);

        float bodyWidth = width - capWidth;
        float sourceWidth = EntityStatusPlateLayout.TEXTURE_WIDTH * bodyWidth / fullWidth;
        float sourceLeft = EntityStatusPlateLayout.TEXTURE_WIDTH - sourceWidth;
        drawTextureQuad(graphics, BLOOD_FILL, left + capWidth, top, left + width, top + half,
                sourceLeft, 0.0F, EntityStatusPlateLayout.TEXTURE_WIDTH, 3.0F,
                topColor, EntityStatusPlateLayout.TEXTURE_WIDTH,
                EntityStatusPlateLayout.TEXTURE_HEIGHT);
        drawTextureQuad(graphics, BLOOD_FILL, left + capWidth, top + half, left + width, bottom,
                sourceLeft, 3.0F, EntityStatusPlateLayout.TEXTURE_WIDTH,
                EntityStatusPlateLayout.TEXTURE_HEIGHT, bottomColor,
                EntityStatusPlateLayout.TEXTURE_WIDTH, EntityStatusPlateLayout.TEXTURE_HEIGHT);
    }

    private static void drawHealthText(GuiGraphics graphics, Font font,
                                       PendingPlate plate, float barTop) {
        Component text = Component.literal(
                formatNumber(plate.health()) + "/" + formatNumber(plate.maxHealth()));
        float scale = EntityStatusPlateLayout.TEXT_SCALE;
        float width = font.width(text) * scale;
        float x = -width * 0.5F;
        float y = barTop + (EntityStatusPlateLayout.BAR_HEIGHT_PIXELS
                - font.lineHeight * scale) * 0.5F;
        drawText(graphics, font, text, x, y, scale, 0xFFFFFFFF);
    }

    private static void drawAttributes(GuiGraphics graphics, Font font,
                                       PendingPlate plate, float barTop, float barRight) {
        boolean showArmor = plate.armor() > 0;
        boolean showToughness = plate.toughness() > 0.0D;
        if (!showArmor && !showToughness) {
            return;
        }

        float iconSize = EntityStatusPlateLayout.ATTRIBUTE_ICON_SIZE_PIXELS;
        float textScale = EntityStatusPlateLayout.ATTRIBUTE_TEXT_SCALE;
        float iconY = barTop + (EntityStatusPlateLayout.BAR_HEIGHT_PIXELS - iconSize) * 0.5F;
        float textY = barTop + (EntityStatusPlateLayout.BAR_HEIGHT_PIXELS
                - font.lineHeight * textScale) * 0.5F;
        float x = barRight + EntityStatusPlateLayout.ICON_GAP_PIXELS;

        if (showArmor) {
            drawTextureQuad(graphics, VANILLA_ICONS, x, iconY, x + iconSize, iconY + iconSize,
                    ARMOR_ICON_U, ARMOR_ICON_V, ARMOR_ICON_U + 9.0F, ARMOR_ICON_V + 9.0F,
                    0xFFFFFFFF, 256.0F, 256.0F);
            Component armorText = Component.literal(formatNumber(plate.armor()));
            float textX = x + iconSize + EntityStatusPlateLayout.ATTRIBUTE_ICON_TEXT_GAP_PIXELS;
            drawText(graphics, font, armorText, textX, textY, textScale, ARMOR_COLOR);
            x = textX + font.width(armorText) * textScale + EntityStatusPlateLayout.ICON_GAP_PIXELS;
        }

        if (showToughness) {
            drawTextureQuad(graphics, OVERFLOWING_ICONS, x, iconY, x + iconSize, iconY + iconSize,
                    TOUGH_ICON_U, TOUGH_ICON_V, TOUGH_ICON_U + 9.0F, TOUGH_ICON_V + 9.0F,
                    0xFFFFFFFF, 256.0F, 256.0F);
            Component toughnessText = Component.literal(formatNumber(plate.toughness()));
            float textX = x + iconSize + EntityStatusPlateLayout.ATTRIBUTE_ICON_TEXT_GAP_PIXELS;
            drawText(graphics, font, toughnessText, textX, textY, textScale, TOUGH_COLOR);
        }
    }

    private static void drawText(GuiGraphics graphics, Font font, Component text,
                                 float x, float y, float scale, int color) {
        if (scale <= 0.0F) {
            return;
        }
        PoseStack pose = graphics.pose();
        pose.pushPose();
        pose.translate(x, y, 0.0F);
        pose.scale(scale, scale, 1.0F);
        graphics.drawString(font, text.getVisualOrderText(), 0.0F, 0.0F, color, false);
        pose.popPose();
    }

    private static void drawTextureQuad(GuiGraphics graphics, ResourceLocation texture,
                                        float left, float top, float right, float bottom,
                                        float u0, float v0, float u1, float v1, int color,
                                        float textureWidth, float textureHeight) {
        if (right <= left || bottom <= top) {
            return;
        }

        RenderSystem.setShader(GameRenderer::getPositionTexColorShader);
        RenderSystem.setShaderTexture(0, texture);
        Matrix4f matrix = graphics.pose().last().pose();
        BufferBuilder builder = Tesselator.getInstance().getBuilder();
        builder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);
        putVertex(builder, matrix, left, bottom, u0 / textureWidth, v1 / textureHeight, color);
        putVertex(builder, matrix, right, bottom, u1 / textureWidth, v1 / textureHeight, color);
        putVertex(builder, matrix, right, top, u1 / textureWidth, v0 / textureHeight, color);
        putVertex(builder, matrix, left, top, u0 / textureWidth, v0 / textureHeight, color);
        BufferUploader.drawWithShader(builder.end());
    }

    private static void putVertex(BufferBuilder builder, Matrix4f matrix,
                                  float x, float y, float u, float v, int color) {
        builder.vertex(matrix, x, y, 0.0F)
                .uv(u, v)
                .color((color >> 16) & 0xFF, (color >> 8) & 0xFF, color & 0xFF,
                        (color >>> 24) & 0xFF)
                .endVertex();
    }

    private static int scaleColor(int color, double factor) {
        int red = (int) Math.round(((color >> 16) & 0xFF) * factor);
        int green = (int) Math.round(((color >> 8) & 0xFF) * factor);
        int blue = (int) Math.round((color & 0xFF) * factor);
        return 0xFF000000 | (red << 16) | (green << 8) | blue;
    }

    private static String formatNumber(double value) {
        double absolute = Math.abs(value);
        if (absolute >= 1_000_000_000.0D) {
            return String.format(Locale.ROOT, "%.2fB", value / 1_000_000_000.0D);
        }
        if (absolute >= 1_000_000.0D) {
            return String.format(Locale.ROOT, "%.2fM", value / 1_000_000.0D);
        }
        if (absolute >= 1_000.0D) {
            return String.format(Locale.ROOT, "%.2fK", value / 1_000.0D);
        }
        return String.format(Locale.ROOT, "%.0f", value);
    }

    private static final class HealthTrack {
        private float health;
        private final long firstSeenTick;
        private long lastSeenTick;
        private long hurtUntilTick;

        private HealthTrack(float health, long firstSeenTick) {
            this.health = health;
            this.firstSeenTick = firstSeenTick;
            this.lastSeenTick = firstSeenTick;
        }
    }

    private record PendingPlate(EntityStatusScreenProjection.Projected projected,
                                float health, float maxHealth,
                                double primaryRatio, double trailingRatio,
                                int armor, double toughness) {
    }

    /** Oculus/Iris 可选阴影轮次 Adapter；未安装时安全退化到主世界轮次。 */
    private record ShadowPassGuard(Method getInstance, Method shadowMethod) {
        private static ShadowPassGuard create() {
            try {
                Class<?> api = Class.forName("net.irisshaders.iris.api.v0.IrisApi");
                return new ShadowPassGuard(api.getMethod("getInstance"),
                        api.getMethod("isRenderingShadowPass"));
            } catch (ReflectiveOperationException | LinkageError | RuntimeException ignored) {
                return new ShadowPassGuard(null, null);
            }
        }

        private boolean isRenderingShadowPass() {
            if (getInstance == null || shadowMethod == null) {
                return false;
            }
            try {
                Object api = getInstance.invoke(null);
                return Boolean.TRUE.equals(shadowMethod.invoke(api));
            } catch (ReflectiveOperationException | RuntimeException ignored) {
                return false;
            }
        }
    }
}
