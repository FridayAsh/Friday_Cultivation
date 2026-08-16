package com.friday.cultivation.client;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
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

    private static final int BAR_INNER_BG = 0xFF1A1A1A;
    private static final int ARMOR_ICON_U = 34, ARMOR_ICON_V = 9;
    private static final int TOUGH_ICON_U = 18, TOUGH_ICON_V = 0;
    private static final int ARMOR_COLOR = 0xAAAAAA;
    private static final int TOUGH_COLOR = 0x40E0D0;
    private static final int HEALTH_TOP = -1944235;
    private static final int HEALTH_BOTTOM = -5758944;

    private static final int BAR_WIDTH = 48;
    private static final int BAR_HEIGHT = 6;
    private static final int ICON_SIZE = 8;
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

        GuiGraphics gfx = new GuiGraphics(mc, mc.renderBuffers().bufferSource());
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableDepthTest();
        RenderSystem.depthMask(false);

        AABB box = player.getBoundingBox().inflate(MAX_DISTANCE, MAX_DISTANCE, MAX_DISTANCE);
        for (Entity e : mc.level.getEntities(player, box, EntityStatusHudRenderer::canShowStatus)) {
            LivingEntity living = (LivingEntity) e;
            if (!player.hasLineOfSight(living)) {
                continue;
            }
            renderEntityStatus(gfx, event, living, cam, partial);
        }

        gfx.flush();
        RenderSystem.depthMask(true);
        RenderSystem.enableDepthTest();
        RenderSystem.disableBlend();
    }

    private static boolean canShowStatus(Entity e) {
        return e instanceof LivingEntity && e.isAlive();
    }

    private static void renderEntityStatus(GuiGraphics gfx, RenderLevelStageEvent event, LivingEntity living, Vec3 cam, float partial) {
        PoseStack pose = gfx.pose();
        pose.pushPose();

        Vec3 pos = living.getPosition(partial).add(0.0, living.getBbHeight() + 0.5, 0.0);
        pose.translate(-cam.x, -cam.y, -cam.z);
        pose.translate(pos.x, pos.y, pos.z);
        pose.mulPose(Axis.XP.rotationDegrees(-event.getCamera().getXRot()));
        pose.mulPose(Axis.YP.rotationDegrees(event.getCamera().getYRot()));
        pose.scale(WORLD_SCALE, WORLD_SCALE, 1.0f);

        // 生命条
        float hp = living.getHealth();
        float maxHp = living.getMaxHealth();
        double ratio = maxHp <= 0.0f ? 0.0 : (double) hp / (double) maxHp;
        renderBar(gfx, -BAR_WIDTH / 2, -10, BAR_WIDTH, BAR_HEIGHT, ratio, HEALTH_TOP, HEALTH_BOTTOM);

        // 盔甲 / 韧性（显示在生命条上方）
        int armor = living.getArmorValue();
        double toughness = living.getAttribute(Attributes.ARMOR_TOUGHNESS).getValue();
        boolean showArmor = armor > 0;
        boolean showToughness = toughness > 0.0;
        if (showArmor || showToughness) {
            renderArmorToughness(gfx, living, -BAR_WIDTH / 2, -22, showArmor, showToughness, armor, toughness);
        }

        pose.popPose();
    }

    private static void renderBar(GuiGraphics gfx, int x, int y, int width, int height, double ratio, int topColor, int bottomColor) {
        gfx.setColor(1.0f, 1.0f, 1.0f, 1.0f);
        gfx.fill(x - 1, y - 1, x + width + 1, y + height + 1, BAR_INNER_BG);
        gfx.blit(BLOOD_EMPTY, x, y, width, height, 0.0f, 0.0f, 96, 6, 96, 6);

        int targetW = (int) ((double) width * ratio);
        if (targetW > 0) {
            int halfH = Math.max(1, height / 2);
            int topH = halfH;
            int botH = height - halfH;
            setBarColor(gfx, topColor);
            gfx.blit(BLOOD_FILL, x, y, targetW, topH, 0.0f, 0.0f, 96, halfH, 96, 6);
            setBarColor(gfx, bottomColor);
            gfx.blit(BLOOD_FILL, x, y + topH, targetW, botH, 0.0f, (float) halfH, 96, 6 - halfH, 96, 6);
            gfx.setColor(1.0f, 1.0f, 1.0f, 1.0f);
        }
    }

    private static void setBarColor(GuiGraphics gfx, int color) {
        float r = ((color >> 16) & 0xFF) / 255.0f;
        float g = ((color >> 8) & 0xFF) / 255.0f;
        float b = (color & 0xFF) / 255.0f;
        gfx.setColor(r, g, b, 1.0f);
    }

    private static void renderArmorToughness(GuiGraphics gfx, LivingEntity living, int barX, int y, boolean showArmor, boolean showToughness, int armor, double toughness) {
        Minecraft mc = Minecraft.getInstance();
        int gap = 4;

        Component armorText = Component.literal(String.valueOf(armor));
        Component toughText = Component.literal(String.format("%.0f", toughness));

        int armorW = showArmor ? ICON_SIZE + 1 + (int) (mc.font.width(armorText) * TEXT_SCALE) : 0;
        int toughW = showToughness ? ICON_SIZE + 1 + (int) (mc.font.width(toughText) * TEXT_SCALE) : 0;
        int totalW = armorW + ((showArmor && showToughness) ? gap : 0) + toughW;

        int gx = -totalW / 2;
        if (showArmor) {
            drawIconValue(gfx, gx, y, VANILLA_ICONS, ARMOR_ICON_U, ARMOR_ICON_V, ARMOR_COLOR, armorText);
            gx += armorW + gap;
        }
        if (showToughness) {
            drawIconValue(gfx, gx, y, OVERFLOWING_ICONS, TOUGH_ICON_U, TOUGH_ICON_V, TOUGH_COLOR, toughText);
        }
    }

    private static void drawIconValue(GuiGraphics gfx, int x, int y, ResourceLocation texture, int u, int v, int color, Component text) {
        gfx.setColor(1.0f, 1.0f, 1.0f, 1.0f);
        gfx.blit(texture, x, y, ICON_SIZE, ICON_SIZE, (float) u, (float) v, 9, 9, 256, 256);
        drawScaledString(gfx, text, x + ICON_SIZE + 1, y + 1, TEXT_SCALE, color);
    }

    private static void drawScaledString(GuiGraphics gfx, Component text, int x, int y, float scale, int color) {
        gfx.pose().pushPose();
        gfx.pose().scale(scale, scale, 1.0f);
        gfx.drawString(Minecraft.getInstance().font, text, Math.round((float) x / scale), Math.round((float) y / scale), color, true);
        gfx.pose().popPose();
    }
}
