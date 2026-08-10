/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.gui.GuiGraphics
 *  net.minecraft.client.player.LocalPlayer
 *  net.minecraft.world.phys.Vec3
 *  net.minecraftforge.client.gui.overlay.IGuiOverlay
 */
package com.friday.cultivation.client;

import com.friday.cultivation.client.QiShieldVisualHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.client.gui.overlay.IGuiOverlay;

public final class QiShieldHudOverlay {
    public static final IGuiOverlay OVERLAY = (gui, gfx, partialTick, screenWidth, screenHeight) -> QiShieldHudOverlay.render(gfx, screenWidth, screenHeight);

    private QiShieldHudOverlay() {
    }

    private static void render(GuiGraphics gfx, int w, int h) {
        int ticks = QiShieldVisualHandler.getIndicatorTicks();
        int total = QiShieldVisualHandler.getIndicatorTotal();
        if (ticks <= 0 || total <= 0) {
            return;
        }
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer p = mc.player;
        if (p == null) {
            return;
        }
        float alphaF = (float)ticks / (float)total * 0.6f;
        int alpha = Math.max(0, Math.min(255, (int)(alphaF * 255.0f)));
        if (alpha < 8) {
            return;
        }
        double dx = -QiShieldVisualHandler.getIndicatorDirX();
        double dy = -QiShieldVisualHandler.getIndicatorDirY();
        double dz = -QiShieldVisualHandler.getIndicatorDirZ();
        Vec3 forward = p.getViewVector(1.0f);
        Vec3 worldUp = new Vec3(0.0, 1.0, 0.0);
        Vec3 right = forward.cross(worldUp).normalize();
        Vec3 up = right.cross(forward).normalize();
        double localRight = dx * right.x + dy * right.y + dz * right.z;
        double localUp = dx * up.x + dy * up.y + dz * up.z;
        double angle = Math.atan2(localRight, localUp);
        Edge edge = angle >= -0.7853981633974483 && angle < 0.7853981633974483 ? Edge.TOP : (angle >= 0.7853981633974483 && angle < 2.356194490192345 ? Edge.RIGHT : (angle <= -0.7853981633974483 && angle > -2.356194490192345 ? Edge.LEFT : Edge.BOTTOM));
        QiShieldHudOverlay.drawEdgeGradient(gfx, w, h, edge, alpha);
    }

    private static void drawEdgeGradient(GuiGraphics gfx, int w, int h, Edge edge, int maxAlpha) {
        int thickness = Math.min(w, h) / 4;
        int columns = 32;
        block6: for (int i = 0; i < columns; ++i) {
            float t = ((float)i + 0.5f) / (float)columns;
            int a = (int)((float)maxAlpha * t * t);
            int color = a << 24 | 0xFFFFFF;
            switch (edge) {
                case TOP: {
                    int y1 = i * thickness / columns;
                    int y2 = (i + 1) * thickness / columns;
                    gfx.fill(0, y1, w, y2, color);
                    continue block6;
                }
                case BOTTOM: {
                    int yEnd = h - i * thickness / columns;
                    int yStart = h - (i + 1) * thickness / columns;
                    gfx.fill(0, yStart, w, yEnd, color);
                    continue block6;
                }
                case LEFT: {
                    int x1 = i * thickness / columns;
                    int x2 = (i + 1) * thickness / columns;
                    gfx.fill(x1, 0, x2, h, color);
                    continue block6;
                }
                case RIGHT: {
                    int xEnd = w - i * thickness / columns;
                    int xStart = w - (i + 1) * thickness / columns;
                    gfx.fill(xStart, 0, xEnd, h, color);
                }
            }
        }
    }

    private static enum Edge {
        TOP,
        RIGHT,
        BOTTOM,
        LEFT;

    }
}

