package com.friday.cultivation.client.screen;

import com.friday.cultivation.CultivationCapability;
import com.friday.cultivation.capability.CultivationData;
import com.friday.cultivation.client.ClientKeybindings;
import com.friday.cultivation.spell.Spell;
import com.friday.cultivation.spell.SpellType;
import com.friday.cultivation.spell.SpellWheelLayout;
import com.friday.cultivation.network.ModNetwork;
import com.friday.cultivation.network.SelectSpellSlotPacket;
import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.glfw.GLFW;

/**
 * 法术轮盘界面（X 键打开）
 * <p>
 * 严格复刻自原模组 com.xiaoxiang.cultivation.client.screen.SpellWheelScreen：
 * <ul>
 *   <li>8 槽位环状布局，槽位中心 = 屏幕中心 + SpellWheelLayout.OCT_X/Y × 30</li>
 *   <li>中心绘制 TAIJI 纹理（来自 CultivationScreen.TAIJI_TEXTURE 静态字段）</li>
 *   <li>槽位被动法术灰度显示 = isSpellEnabled(sp) 为 false 时</li>
 *   <li>松开 X 键 → 提交当前 hoveredSlot → 关闭</li>
 *   <li>左键点击槽位 → 提交并关闭</li>
 *   <li>ESC 键（keyCode 256）→ 直接关闭（不提交）</li>
 *   <li>shouldPauseScreen() = false（不暂停游戏）</li>
 * </ul>
 */
public class SpellWheelScreen extends Screen {

    // ── 布局常量（与原 mod 一致） ──
    private static final int SLOT_SIZE = 18;
    private static final int CELL_SPACING = 30;
    private static final int ICON_SIZE = 14;
    private static final double DEAD_ZONE = 10.0;

    // ── 颜色常量（严格按原 mod 十六进制补码转十进制） ──
    private static final int ORB_OUTER = -792090518;   // 0xD0C8C8C8
    private static final int ORB_INNER = -520820008;   // 0xE1000000
    private static final int POINTER_COLOR = -4703686; // 0xFFB8B8BA
    private static final int POINTER_TIP_COLOR = -10496; // 0xFFFFD860 (金黄)
    private static final int SLOT_BORDER_HOVERED = -10496; // 0xFFFFD860
    private static final int SLOT_BORDER_PRIMED = -4703686; // 0xFFB8B8BA
    private static final int SLOT_BORDER_NORMAL = -535161324; // 0xE0111111
    private static final int SLOT_BG_HOVERED = -252384552; // 0xF1010108
    private static final int SLOT_BG_NORMAL = -789255464;   // 0xD0111111
    private static final int SLOT_TOP_BAR = -2130716928;    // 0x81000000
    private static final int HINT_COLOR = -4144960;          // 0xFFC0C0C0

    // ── 状态字段 ──
    private final int[][] slotCenters = new int[8][2];
    private int hoveredSlot = -1;
    private boolean committed = false;

    public SpellWheelScreen() {
        super(Component.translatable("screen.friday_cultivation.spell_wheel.title"));
    }

    @Override
    protected void init() {
        super.init();
        int cx = this.width / 2;
        int cy = this.height / 2;
        for (int i = 0; i < 8; ++i) {
            this.slotCenters[i][0] = cx + SpellWheelLayout.offsetX(i) * CELL_SPACING;
            this.slotCenters[i][1] = cy + SpellWheelLayout.offsetY(i) * CELL_SPACING;
        }
        // 强制鼠标移动到屏幕中心
        Minecraft mc = Minecraft.getInstance();
        double physicalCx = (double) mc.getWindow().getScreenWidth() / 2.0;
        double physicalCy = (double) mc.getWindow().getScreenHeight() / 2.0;
        GLFW.glfwSetCursorPos(mc.getWindow().getWindow(), physicalCx, physicalCy);
    }

    @Override
    public void tick() {
        super.tick();
        if (!isWheelKeyHeld()) {
            commitAndClose();
        }
    }

    /**
     * 原 mod 静态方法：检查法术轮盘键（X 键）当前是否被物理按下
     * 区分 KEYSYM（键盘）和 MOUSE（鼠标）两种输入类型
     */
    private static boolean isWheelKeyHeld() {
        InputConstants.Key key = ClientKeybindings.OPEN_SPELL_WHEEL.getKey();
        long window = Minecraft.getInstance().getWindow().getWindow();
        int code = key.getValue();
        if (code < 0) {
            return false;
        }
        switch (key.getType()) {
            case KEYSYM:
                return GLFW.glfwGetKey(window, code) == 1;
            case MOUSE:
                return GLFW.glfwGetMouseButton(window, code) == 1;
            default:
                return false;
        }
    }

    private void commitAndClose() {
        if (!this.committed) {
            this.committed = true;
            if (this.hoveredSlot >= 0 && this.hoveredSlot < 8) {
                ModNetwork.CHANNEL.sendToServer(new SelectSpellSlotPacket(this.hoveredSlot));
            }
        }
        this.onClose();
    }

    @Override
    public void renderBackground(@NotNull GuiGraphics gfx) {
        gfx.fill(0, 0, this.width, this.height, 0x50000000);
    }

    @Override
    public void render(@NotNull GuiGraphics gfx, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(gfx);
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        if (player == null) {
            return;
        }
        CultivationData cap = CultivationCapability.get((Player) player).orElse(null);
        if (cap == null) {
            return;
        }

        int cx = this.width / 2;
        int cy = this.height / 2;
        double dx = mouseX - cx;
        double dy = mouseY - cy;
        this.hoveredSlot = SpellWheelLayout.closestSlot(dx, dy, DEAD_ZONE);

        String[] equippedArray = new String[8];
        for (int i = 0; i < 8; i++) equippedArray[i] = cap.getEquippedSpellAt(i);
        drawCenterOrb(gfx, cx, cy);
        if (this.hoveredSlot >= 0) {
            drawPointerLine(gfx, cx, cy, this.slotCenters[this.hoveredSlot][0], this.slotCenters[this.hoveredSlot][1]);
        }

        RenderSystem.enableBlend();
        for (int i = 0; i < 8; ++i) {
            int sx = this.slotCenters[i][0] - 9;
            int sy = this.slotCenters[i][1] - 9;
            String sid = equippedArray[i];
            Spell sp = (sid == null || sid.isEmpty()) ? null : Spell.byId(sid);
            boolean hovered = (i == this.hoveredSlot);
            boolean primed = (i == cap.getSelectedSpellSlot());
            drawSlot(gfx, sx, sy, sp, cap, hovered, primed);
        }
        RenderSystem.disableBlend();

        // hovered 槽位显示法术名
        if (this.hoveredSlot >= 0) {
            String sid = equippedArray[this.hoveredSlot];
            Spell sp = (sid == null || sid.isEmpty()) ? null : Spell.byId(sid);
            MutableComponent label = sp != null
                    ? sp.displayNameForRealm(cap.getRealm()).copy().withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD)
                    : Component.translatable("screen.friday_cultivation.spell_wheel.empty_slot").withStyle(ChatFormatting.GRAY);
            int w = this.font.width((FormattedText) label);
            gfx.drawString(this.font, label, cx - w / 2, cy + 45 + 12, -1, true);
        }

        // 底部提示
        MutableComponent hint = Component.translatable("screen.friday_cultivation.spell_wheel.hint");
        gfx.drawCenteredString(this.font, hint, cx, this.height - 28, HINT_COLOR);

        super.render(gfx, mouseX, mouseY, partialTick);
    }

    /**
     * 中心圆球（原 mod：外层半透明灰 + 内层黑 + 10x10 TAIJI 纹理）
     */
    private void drawCenterOrb(GuiGraphics gfx, int cx, int cy) {
        gfx.fill(cx - 6, cy - 6, cx + 6, cy + 6, ORB_OUTER);
        gfx.fill(cx - 5, cy - 5, cx + 5, cy + 5, ORB_INNER);
        RenderSystem.enableBlend();
        gfx.blit(CultivationScreen.TAIJI_TEXTURE, cx - 5, cy - 5, 10, 10, 0.0f, 0.0f, 32, 32, 32, 32);
        RenderSystem.disableBlend();
    }

    /**
     * 中心指向 hovered 槽位的虚线 + 箭头
     */
    private void drawPointerLine(GuiGraphics gfx, int cx, int cy, int tx, int ty) {
        double fdx = tx - cx;
        double fdy = ty - cy;
        double len = Math.sqrt(fdx * fdx + fdy * fdy);
        if (len < 3.0) {
            return;
        }
        int steps = (int) Math.min(len, 50.0);
        for (int i = 5; i < steps; ++i) {
            double t = (double) i / (double) steps;
            int px = (int) ((double) cx + fdx * t);
            int py = (int) ((double) cy + fdy * t);
            gfx.fill(px, py, px + 1, py + 1, POINTER_COLOR);
        }
        int ax = (int) ((double) cx + fdx * 0.8);
        int ay = (int) ((double) cy + fdy * 0.8);
        gfx.fill(ax - 1, ay - 1, ax + 2, ay + 2, POINTER_TIP_COLOR);
    }

    /**
     * 单个槽位（原 mod：hovered/primed/普通 三态边框 + 背景 + 顶部窄条 + 灰度图标）
     */
    private void drawSlot(GuiGraphics gfx, int sx, int sy, Spell sp, CultivationData data, boolean hovered, boolean primed) {
        int border = hovered ? SLOT_BORDER_HOVERED : (primed ? SLOT_BORDER_PRIMED : SLOT_BORDER_NORMAL);
        gfx.fill(sx - 2, sy - 2, sx + 18 + 2, sy + 18 + 2, border);
        int bg = hovered ? SLOT_BG_HOVERED : SLOT_BG_NORMAL;
        gfx.fill(sx, sy, sx + 18, sy + 18, bg);
        gfx.fill(sx, sy, sx + 18, sy + 2, SLOT_TOP_BAR);
        if (sp != null) {
            int iconX = sx + 2;
            int iconY = sy + 2;
            RenderSystem.enableBlend();
            boolean disabledPassive = sp.type() == SpellType.PASSIVE && !data.isSpellEnabled(sp);
            SpellIconRenderHelper.blitSpellIcon(gfx, sp, iconX, iconY, ICON_SIZE, disabledPassive);
            RenderSystem.disableBlend();
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0 && this.hoveredSlot >= 0) {
            this.commitAndClose();
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == 256) { // ESC
            this.committed = true;
            this.onClose();
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
