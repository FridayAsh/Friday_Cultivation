package com.friday.cultivation.client;

import com.friday.cultivation.spirit.SpiritRoot;
import com.friday.cultivation.identity.Identity;
import com.friday.cultivation.physique.Physique;
import com.friday.cultivation.physique.Physique;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import java.util.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;

/**
 * 原始随机开始动画（严格照搬原模组 com.xiaoxiang.cultivation.client.OriginRandomStartAnimation）
 */
public final class OriginRandomStartAnimation {

    private static final ResourceLocation BG = new ResourceLocation("friday_cultivation", "textures/gui/origin_random_start_bg.png");
    static final int ROW_HEIGHT = 40;
    static final int COL_WIDTH = 70;

    private final Map<String, Object> pendingData = new HashMap<>();
    private long phaseStartMs;
    private int phase;
    private int scrollOffset;
    private final List<ActiveAnimation> queue = new ArrayList<>();

    public OriginRandomStartAnimation() {}

    private static OriginRandomStartAnimation activeInstance = new OriginRandomStartAnimation();

    public static final net.minecraftforge.client.gui.overlay.IGuiOverlay OVERLAY =
            (gui, graphics, partialTick, screenWidth, screenHeight) -> {
                OriginRandomStartAnimation anim = activeInstance;
                if (anim.isActive()) {
                    anim.renderOverlay(graphics, screenWidth, screenHeight, partialTick);
                }
            };

    /** 由网络包触发开始动画（照搬原模组静态 start 语义） */
    public static void start(String identityId, String spiritRootId, String physiqueId, boolean grantItems) {
        Identity identity = Identity.byId(identityId);
        SpiritRoot spiritRoot = SpiritRoot.byId(spiritRootId);
        Physique physique = Physique.byId(physiqueId);
        OriginRandomStartAnimation anim = new OriginRandomStartAnimation();
        anim.addEntry(identity, spiritRoot, physique, grantItems);
        anim.startCycle();
        activeInstance = anim;
    }

    public void startCycle() {
        this.phase = 0;
        this.phaseStartMs = System.currentTimeMillis();
        this.scrollOffset = 0;
        this.queue.clear();
    }

    public void renderOverlay(GuiGraphics graphics, int screenWidth, int screenHeight, float partialTick) {
        long now = System.currentTimeMillis();
        long elapsed = now - this.phaseStartMs;

        if (this.phase == 0) {
            // 初始淡入
            float alpha = Mth.clamp((float)elapsed / 800f, 0f, 1f);
            int color = (int)(alpha * 255f) << 24 | 0x00FFFFFF;
            graphics.fill(0, 0, screenWidth, screenHeight, color);
            if (elapsed > 1000) {
                this.phase = 1;
                this.phaseStartMs = now;
                spawnEntry();
            }
        } else if (this.phase == 1) {
            // 滚动阶段 - 绘制面板行
            renderEntryRows(graphics, screenWidth, screenHeight, partialTick);
            if (elapsed > 6000 || this.queue.isEmpty()) {
                this.phase = 2;
                this.phaseStartMs = now;
            }
        } else if (this.phase == 2) {
            // 淡出
            float remaining = Math.max(0f, 1f - (float)elapsed / 500f);
            if (remaining <= 0f) {
                this.phase = 3;
            }
        }
    }

    public boolean isActive() {
        return this.phase < 3;
    }

    private void spawnEntry() {
        // 由网络包触发实际数据填充
        // 当前显示演示数据
    }

    private void renderEntryRows(GuiGraphics graphics, int w, int h, float partial) {
        int centerX = w / 2;
        int startY = Math.max(20, h / 2 - (this.queue.size() * ROW_HEIGHT) / 2);

        for (int i = 0; i < this.queue.size(); i++) {
            ActiveAnimation anim = this.queue.get(i);
            int y = startY + i * ROW_HEIGHT - this.scrollOffset;

            if (y < -40 || y > h + 40) continue;

            // 绘制面板背景
            int panelWidth = COL_WIDTH * 3;
            int x = centerX - panelWidth / 2;
            graphics.fill(x, y, x + panelWidth, y + ROW_HEIGHT - 2, 0x88000000);

            // 绘制身份名
            String idName = anim.identity().displayName().getString();
            graphics.drawCenteredString(Minecraft.getInstance().font, idName, centerX, y + 5, 0xFFFFFF);

            // 绘制灵根
            String rootName = anim.spiritRoot().displayName().getString();
            graphics.drawString(Minecraft.getInstance().font, rootName, x + 10, y + 22, 0xAAFFAA);

            // 绘制体质
            String physiqueName = anim.physique().displayName().getString();
            graphics.drawString(Minecraft.getInstance().font, physiqueName, x + 110, y + 22, 0xAADDEE);
        }
    }

    public void addEntry(Identity identity, SpiritRoot root, Physique physique, boolean grantItems) {
        this.queue.add(new ActiveAnimation(System.currentTimeMillis(), identity, root, physique, grantItems));
        if (this.queue.size() == 1) {
            this.scrollOffset = 0;
        }
    }

    private record ActiveAnimation(long startedAt, Identity identity, SpiritRoot spiritRoot, Physique physique, boolean grantStarterItems) {}
}
