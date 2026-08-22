package com.friday.cultivation.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import net.minecraft.Util;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
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
import net.minecraftforge.client.event.RenderNameTagEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.joml.Matrix4f;
import org.joml.Vector3f;

/**
 * 生物头顶状态牌渲染器。
 *
 * <p>状态牌在原版名称牌事件中以世界空间 billboard 绘制，使用固定世界比例将统一的
 * 本地排版单位换算成世界尺寸。动画状态由统一的 {@link HudBarAnimator} 提供，
 * 本类只负责生物状态追踪、可见性策略、世界变换和绘制。</p>
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

    private static final float FRAME_Z = 0.000F;
    private static final float BACKGROUND_Z = -0.001F;
    private static final float TRAILING_Z = -0.002F;
    private static final float PRIMARY_Z = -0.003F;
    private static final float TEXT_Z = -0.004F;

    private static final Map<UUID, HealthTrack> HEALTH_TRACKS = new HashMap<>();
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

            // 提前提交目标值，避免生命条首次出现时才初始化到受伤后的比例。
            ANIMATOR.sample(id, HudBarAnimator.BarId.HEALTH,
                    health, living.getMaxHealth(), 0L, nowMillis);
        }

        HEALTH_TRACKS.entrySet().removeIf(entry -> {
            HealthTrack track = entry.getValue();
            if (nowTick - track.lastSeenTick > TRACKING_EXPIRE_TICKS || !seen.contains(entry.getKey())) {
                ANIMATOR.reset(entry.getKey());
                return true;
            }
            return false;
        });
    }

    /** 在原版名称牌世界渲染阶段绘制状态牌，光影与实体共用同一投影链。 */
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
        EntityStatusPlateLayout.Layout layout = computeLayout(mc, anchor);
        if (layout == null) {
            return;
        }

        HudBarAnimator.Visual visual = ANIMATOR.sample(living.getUUID(), HudBarAnimator.BarId.HEALTH,
                living.getHealth(), living.getMaxHealth(), 0L, Util.getMillis());
        renderEntityStatus(event, mc, living, visual, layout);
    }

    private static boolean canTrack(Entity entity) {
        return entity instanceof LivingEntity living && living.isAlive();
    }

    private static void clearTracking() {
        HEALTH_TRACKS.clear();
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

    /**
     * 视线遮挡判断保留原有规则：玩家眼睛到生物身体中心之间若先命中方块，则隐藏状态牌。
     */
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

    private static void renderEntityStatus(RenderNameTagEvent event, Minecraft mc,
                                           LivingEntity living, HudBarAnimator.Visual visual,
                                           EntityStatusPlateLayout.Layout layout) {
        PoseStack pose = event.getPoseStack();
        MultiBufferSource buffers = event.getMultiBufferSource();
        EntityRenderDispatcher dispatcher = mc.getEntityRenderDispatcher();

        pose.pushPose();
        pose.translate(0.0D, living.getBbHeight() + HEAD_ANCHOR_OFFSET, 0.0D);
        pose.mulPose(dispatcher.cameraOrientation());
        float scale = layout.worldUnitsPerLogicalPixel();
        pose.scale(-scale, -scale, scale);

        drawTextureQuad(buffers, pose, BLOOD_EMPTY,
                -EntityStatusPlateLayout.BAR_WIDTH_PIXELS * 0.5F,
                -EntityStatusPlateLayout.BAR_HEAD_GAP_PIXELS - EntityStatusPlateLayout.BAR_HEIGHT_PIXELS,
                EntityStatusPlateLayout.BAR_WIDTH_PIXELS * 0.5F,
                -EntityStatusPlateLayout.BAR_HEAD_GAP_PIXELS,
                0.0F, 0.0F, EntityStatusPlateLayout.TEXTURE_WIDTH,
                EntityStatusPlateLayout.TEXTURE_HEIGHT, 0xFFFFFFFF, FRAME_Z);

        drawTextureQuad(buffers, pose, BLOOD_FILL,
                -EntityStatusPlateLayout.BAR_WIDTH_PIXELS * 0.5F,
                -EntityStatusPlateLayout.BAR_HEAD_GAP_PIXELS - EntityStatusPlateLayout.BAR_HEIGHT_PIXELS,
                EntityStatusPlateLayout.BAR_WIDTH_PIXELS * 0.5F,
                -EntityStatusPlateLayout.BAR_HEAD_GAP_PIXELS,
                0.0F, 0.0F, EntityStatusPlateLayout.TEXTURE_WIDTH,
                EntityStatusPlateLayout.TEXTURE_HEIGHT, BAR_INNER_BG, BACKGROUND_Z);

        double primary = EntityStatusPlateLayout.clampRatio(visual.primaryRatio());
        double trailing = Math.max(primary,
                EntityStatusPlateLayout.clampRatio(visual.trailingRatio()));
        if (trailing > primary + 0.0001D) {
            drawFill(buffers, pose, trailing,
                    scaleColor(HEALTH_TOP, 0.58D), scaleColor(HEALTH_BOTTOM, 0.58D), TRAILING_Z);
        }
        drawFill(buffers, pose, primary, HEALTH_TOP, HEALTH_BOTTOM, PRIMARY_Z);

        drawHealthText(buffers, pose, mc.font, living, TEXT_Z);
        // pose 已经执行世界缩放；这里必须继续传入 13 个本地排版单位，不能传 layout.iconSize() 再缩放一次。
        drawAttributes(buffers, pose, mc.font, living,
                EntityStatusPlateLayout.ICON_SIZE_PIXELS, TEXT_Z);
        pose.popPose();
    }

    private static void drawFill(MultiBufferSource buffers, PoseStack pose, double ratio,
                                 int topColor, int bottomColor, float z) {
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
            drawTextureQuad(buffers, pose, BLOOD_FILL, left, top, left + width, top + half,
                    0.0F, 0.0F, EntityStatusPlateLayout.TEXTURE_WIDTH, 3.0F, topColor, z);
            drawTextureQuad(buffers, pose, BLOOD_FILL, left, top + half, left + width, bottom,
                    0.0F, 3.0F, EntityStatusPlateLayout.TEXTURE_WIDTH,
                    EntityStatusPlateLayout.TEXTURE_HEIGHT, bottomColor, z);
            return;
        }

        float capWidth = Math.min(cap, width);
        drawTextureQuad(buffers, pose, BLOOD_FILL, left, top, left + capWidth, top + half,
                0.0F, 0.0F, EntityStatusPlateLayout.CLIP_TEXTURE_PIXELS, 3.0F, topColor, z);
        drawTextureQuad(buffers, pose, BLOOD_FILL, left, top + half, left + capWidth, bottom,
                0.0F, 3.0F, EntityStatusPlateLayout.CLIP_TEXTURE_PIXELS,
                EntityStatusPlateLayout.TEXTURE_HEIGHT, bottomColor, z);

        float bodyWidth = width - capWidth;
        float sourceWidth = EntityStatusPlateLayout.TEXTURE_WIDTH * bodyWidth / fullWidth;
        float sourceLeft = EntityStatusPlateLayout.TEXTURE_WIDTH - sourceWidth;
        drawTextureQuad(buffers, pose, BLOOD_FILL, left + capWidth, top, left + width, top + half,
                sourceLeft, 0.0F, EntityStatusPlateLayout.TEXTURE_WIDTH, 3.0F, topColor, z);
        drawTextureQuad(buffers, pose, BLOOD_FILL, left + capWidth, top + half, left + width, bottom,
                sourceLeft, 3.0F, EntityStatusPlateLayout.TEXTURE_WIDTH,
                EntityStatusPlateLayout.TEXTURE_HEIGHT, bottomColor, z);
    }

    private static void drawHealthText(MultiBufferSource buffers, PoseStack pose, Font font,
                                       LivingEntity living, float z) {
        String value = formatNumber(living.getHealth()) + "/" + formatNumber(living.getMaxHealth());
        Component text = Component.literal(value);
        float width = font.width(text) * EntityStatusPlateLayout.TEXT_SCALE;
        float x = -width * 0.5F;
        float y = -EntityStatusPlateLayout.BAR_HEAD_GAP_PIXELS
                - EntityStatusPlateLayout.BAR_HEIGHT_PIXELS
                + (EntityStatusPlateLayout.BAR_HEIGHT_PIXELS
                - font.lineHeight * EntityStatusPlateLayout.TEXT_SCALE) * 0.5F;
        drawText(buffers, pose, font, text, x, y, EntityStatusPlateLayout.TEXT_SCALE, 0xFFFFFFFF, z);
    }

    private static void drawAttributes(MultiBufferSource buffers, PoseStack pose, Font font,
                                       LivingEntity living, float iconSize, float z) {
        int armor = living.getArmorValue();
        AttributeInstance toughnessAttribute = living.getAttribute(Attributes.ARMOR_TOUGHNESS);
        double toughness = toughnessAttribute == null ? 0.0D : toughnessAttribute.getValue();
        boolean showArmor = armor > 0;
        boolean showToughness = toughness > 0.0D;
        if (!showArmor && !showToughness) {
            return;
        }

        float barRight = EntityStatusPlateLayout.BAR_WIDTH_PIXELS * 0.5F;
        float iconY = -EntityStatusPlateLayout.BAR_HEAD_GAP_PIXELS
                - EntityStatusPlateLayout.BAR_HEIGHT_PIXELS
                + (EntityStatusPlateLayout.BAR_HEIGHT_PIXELS - iconSize) * 0.5F;
        float x = barRight + EntityStatusPlateLayout.ICON_GAP_PIXELS;
        if (showArmor) {
            drawTextureQuad(buffers, pose, VANILLA_ICONS, x, iconY, x + iconSize, iconY + iconSize,
                    ARMOR_ICON_U, ARMOR_ICON_V, ARMOR_ICON_U + 9.0F, ARMOR_ICON_V + 9.0F,
                    0xFFFFFFFF, z, 256.0F, 256.0F);
            String armorText = formatNumber(armor);
            drawText(buffers, pose, font, Component.literal(armorText),
                    x + iconSize + 1.0F, iconY + 1.0F, 0.5F, ARMOR_COLOR, z + 0.001F);
            x += iconSize + 1.0F + font.width(armorText) * 0.5F + EntityStatusPlateLayout.ICON_GAP_PIXELS;
        }
        if (showToughness) {
            drawTextureQuad(buffers, pose, OVERFLOWING_ICONS, x, iconY, x + iconSize, iconY + iconSize,
                    TOUGH_ICON_U, TOUGH_ICON_V, TOUGH_ICON_U + 9.0F, TOUGH_ICON_V + 9.0F,
                    0xFFFFFFFF, z, 256.0F, 256.0F);
            drawText(buffers, pose, font, Component.literal(formatNumber(toughness)),
                    x + iconSize + 1.0F, iconY + 1.0F, 0.5F, TOUGH_COLOR, z + 0.001F);
        }
    }

    private static void drawText(MultiBufferSource buffers, PoseStack pose, Font font,
                                 Component text, float x, float y, float scale, int color, float z) {
        if (scale <= 0.0F) {
            return;
        }
        pose.pushPose();
        pose.translate(x, y, z);
        pose.scale(scale, scale, 1.0F);
        // Font 的阴影模式会把主字形沿 +Z 偏移 0.03，在状态牌深度层中会被血条平面遮挡，只留下深色阴影。
        font.drawInBatch(text, 0.0F, 0.0F, color, false, pose.last().pose(), buffers,
                Font.DisplayMode.NORMAL, 0, LightTexture.FULL_BRIGHT);
        pose.popPose();
    }

    private static void drawTextureQuad(MultiBufferSource buffers, PoseStack pose,
                                         ResourceLocation texture, float left, float top,
                                         float right, float bottom, float u0, float v0,
                                         float u1, float v1, int color, float z) {
        drawTextureQuad(buffers, pose, texture, left, top, right, bottom,
                u0, v0, u1, v1, color, z,
                EntityStatusPlateLayout.TEXTURE_WIDTH, EntityStatusPlateLayout.TEXTURE_HEIGHT);
    }

    private static void drawTextureQuad(MultiBufferSource buffers, PoseStack pose,
                                         ResourceLocation texture, float left, float top,
                                         float right, float bottom, float u0, float v0,
                                         float u1, float v1, int color, float z,
                                         float textureWidth, float textureHeight) {
        if (right <= left || bottom <= top) {
            return;
        }
        VertexConsumer consumer = buffers.getBuffer(RenderType.text(texture));
        PoseStack.Pose current = pose.last();
        Matrix4f matrix = current.pose();
        putVertex(consumer, matrix, left, bottom, z, u0 / textureWidth, v1 / textureHeight, color);
        putVertex(consumer, matrix, right, bottom, z, u1 / textureWidth, v1 / textureHeight, color);
        putVertex(consumer, matrix, right, top, z, u1 / textureWidth, v0 / textureHeight, color);
        putVertex(consumer, matrix, left, top, z, u0 / textureWidth, v0 / textureHeight, color);
    }

    private static void putVertex(VertexConsumer consumer, Matrix4f matrix,
                                  float x, float y, float z, float u, float v, int color) {
        consumer.vertex(matrix, x, y, z)
                .color((color >> 16) & 0xFF, (color >> 8) & 0xFF, color & 0xFF,
                        (color >>> 24) & 0xFF)
                .uv(u, v)
                .uv2(LightTexture.FULL_BRIGHT)
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
