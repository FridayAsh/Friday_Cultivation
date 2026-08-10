package com.friday.cultivation.client.screen;

import com.friday.cultivation.alchemy.AlchemyRecipe;
import com.friday.cultivation.alchemy.PillTier;
import com.friday.cultivation.inventory.AlchemyMenu;
import com.friday.cultivation.network.AutoFillRecipePacket;
import com.friday.cultivation.network.ExecuteAlchemyPacket;
import com.friday.cultivation.network.ModNetwork;
import com.friday.cultivation.registry.ModMenuTypes;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import org.jetbrains.annotations.NotNull;

/**
 * 炼丹炉GUI界面 — 完整复刻原模组 AlchemyScreen。
 * 左侧配方列表（可滚动），右侧材料槽+输出槽+灵气条+炼丹按钮。
 */
public class AlchemyScreen extends AbstractContainerScreen<AlchemyMenu> {
    private static final int BG_PAGE = -923956;
    private static final int BG_PANEL = -1517128;
    private static final int INK_BLACK = -15067628;
    private static final int INK_SOFT = -12766422;
    private static final int VERMILLION = -4703686;
    private static final int GOLD_BORDER = -3562934;
    private static final int GOLD_BRIGHT = -10496;
    private static final int BORDER_DARK = -10859978;
    private static final int QI_BAR_BG = -15066582;
    private static final int QI_BAR_FILL_TOP = -10428161;
    private static final int QI_BAR_FILL_BOT = -14786400;
    private static final int BTN_BG = -1517128;
    private static final int BTN_BG_HOVER = -6528;
    private static final int BTN_BG_DISABLED = -7638944;
    private static final int RECIPE_PANEL_X = 4;
    private static final int RECIPE_PANEL_Y = 32;
    private static final int RECIPE_PANEL_W = 86;
    private static final int RECIPE_PANEL_H = 162;
    private static final int RECIPE_ROW_H = 20;
    private static final int RECIPE_SCROLLBAR_W = 4;
    private static final int RECIPE_SCROLLBAR_PAD = 3;
    private static final int DIVIDER_X = 92;
    private static final int RIGHT_X = 96;
    private static final int RIGHT_W = 180;
    private static final int START_BTN_X = 200;
    private static final int START_BTN_Y = 32;
    private static final int START_BTN_W = 72;
    private static final int START_BTN_H = 36;
    private static final int QI_BAR_X = 100;
    private static final int QI_BAR_Y = 84;
    private static final int QI_BAR_W = 170;
    private static final int QI_BAR_H = 8;

    private int recipeScrollOffset = 0;
    private boolean draggingRecipeScrollbar = false;
    private int recipeScrollbarDragOffsetY = 0;

    public AlchemyScreen(AlchemyMenu menu, Inventory inv, Component title) {
        super(menu, inv, title);
        this.imageWidth = 280;
        this.imageHeight = 200;
        this.titleLabelX = 8;
        this.titleLabelY = 8;
        this.inventoryLabelX = 100;
        this.inventoryLabelY = 110;
    }

    /** 客户端注册炼丹Screen */
    public static void register() {
        MenuScreens.register(ModMenuTypes.ALCHEMY.get(), AlchemyScreen::new);
    }

    @Override
    protected void renderBg(@NotNull GuiGraphics gfx, float partialTick, int mouseX, int mouseY) {
        int h;
        int x = this.leftPos;
        int y = this.topPos;
        gfx.fill(x, y, x + this.imageWidth, y + this.imageHeight, BG_PAGE);
        gfx.fill(x, y, x + this.imageWidth, y + 2, GOLD_BORDER);
        gfx.fill(x, y + this.imageHeight - 2, x + this.imageWidth, y + this.imageHeight, GOLD_BORDER);
        gfx.fill(x, y, x + 2, y + this.imageHeight, GOLD_BORDER);
        gfx.fill(x + this.imageWidth - 2, y, x + this.imageWidth, y + this.imageHeight, GOLD_BORDER);
        gfx.fill(x + 3, y + 3, x + this.imageWidth - 3, y + 4, BORDER_DARK);
        gfx.fill(x + 3, y + this.imageHeight - 4, x + this.imageWidth - 3, y + this.imageHeight - 3, BORDER_DARK);
        gfx.fill(x + 3, y + 3, x + 4, y + this.imageHeight - 3, BORDER_DARK);
        gfx.fill(x + this.imageWidth - 4, y + 3, x + this.imageWidth - 3, y + this.imageHeight - 3, BORDER_DARK);
        gfx.fill(x + 8, y + 18, x + this.imageWidth - 8, y + 19, INK_SOFT);
        gfx.fill(x + 92, y + 20, x + 92 + 1, y + this.imageHeight - 6, INK_SOFT);
        gfx.fill(x + 4, y + 32, x + 4 + 86, y + 32 + 162, BG_PANEL);
        gfx.drawString(this.font, Component.translatable("screen.friday_cultivation.alchemy.recipes"), x + 4 + 2, y + 22, INK_BLACK, false);
        gfx.drawString(this.font, Component.translatable("screen.friday_cultivation.alchemy.place_materials"), x + 96 + 4, y + 22, INK_BLACK, false);
        for (int row = 0; row < 2; ++row) {
            for (int col = 0; col < 3; ++col) {
                int sx = x + 100 + col * 18 - 1;
                int sy = y + 32 + row * 18 - 1;
                gfx.fill(sx, sy, sx + 18, sy + 18, GOLD_BORDER);
                gfx.fill(sx + 1, sy + 1, sx + 17, sy + 17, BG_PAGE);
            }
        }
        int arrowX = x + 158;
        int arrowY = y + 43;
        for (int dx = 0; dx < 10 && (h = 10 - dx * 2) > 0; ++dx) {
            gfx.fill(arrowX + dx, arrowY + (10 - h) / 2, arrowX + dx + 1, arrowY + (10 + h) / 2, VERMILLION);
        }
        int outX = x + 178 - 1;
        int outY = y + 41 - 1;
        gfx.fill(outX, outY, outX + 18, outY + 18, GOLD_BORDER);
        gfx.fill(outX + 1, outY + 1, outX + 17, outY + 17, BG_PAGE);
        this.renderQiBar(gfx, x, y);
        this.renderStartButton(gfx, x, y, mouseX, mouseY);
        gfx.fill(x + 96, y + 106, x + this.imageWidth - 6, y + 107, INK_SOFT);
        gfx.fill(x + 96 + 1, y + 119, x + this.imageWidth - 5, y + 173, BG_PANEL);
        gfx.fill(x + 96 + 1, y + 178, x + this.imageWidth - 5, y + 196, BG_PANEL);
        for (int row = 0; row < 3; ++row) {
            for (int col = 0; col < 9; ++col) {
                int sx = x + 98 + col * 18 - 1;
                int sy = y + 120 + row * 18 - 1;
                gfx.fill(sx, sy, sx + 18, sy + 18, GOLD_BORDER);
                gfx.fill(sx + 1, sy + 1, sx + 17, sy + 17, BG_PAGE);
            }
        }
        for (int col = 0; col < 9; ++col) {
            int sx = x + 98 + col * 18 - 1;
            int sy = y + 180 - 1;
            gfx.fill(sx, sy, sx + 18, sy + 18, GOLD_BORDER);
            gfx.fill(sx + 1, sy + 1, sx + 17, sy + 17, BG_PAGE);
        }
    }

    private void renderQiBar(GuiGraphics gfx, int x, int y) {
        long current = this.menu.getCurrentQi();
        long max = Math.max(1L, this.menu.getMaxQi());
        float pct = Math.min(1.0f, (float) current / (float) max);
        int barX = x + QI_BAR_X;
        int barY = y + QI_BAR_Y;
        gfx.fill(barX - 1, barY - 1, barX + QI_BAR_W + 1, barY + QI_BAR_H + 1, GOLD_BORDER);
        gfx.fill(barX, barY, barX + QI_BAR_W, barY + QI_BAR_H, QI_BAR_BG);
        int filled = (int) (170.0f * pct);
        if (filled > 0) {
            gfx.fill(barX, barY, barX + filled, barY + 4, QI_BAR_FILL_TOP);
            gfx.fill(barX, barY + 4, barX + filled, barY + 8, QI_BAR_FILL_BOT);
        }
        String text = current + " / " + max;
        int textW = this.font.width(text);
        gfx.drawString(this.font, text, barX + (170 - textW) / 2, barY - 1, -1, true);
        gfx.drawString(this.font, Component.translatable("screen.friday_cultivation.alchemy.qi_storage"), barX, barY - 11, INK_SOFT, false);
    }

    private boolean isMouseOverStartButton(int mouseX, int mouseY) {
        int bx = this.leftPos + START_BTN_X;
        int by = this.topPos + START_BTN_Y;
        return mouseX >= bx && mouseX < bx + START_BTN_W && mouseY >= by && mouseY < by + START_BTN_H;
    }

    private boolean canCraftEffective() {
        AlchemyRecipe recipe = this.getEffectiveRecipe();
        if (recipe == null) {
            return false;
        }
        return this.menu.countPossiblePillsForRecipe(recipe) > 0;
    }

    private AlchemyRecipe getEffectiveRecipe() {
        return this.menu.findRecipeByMaterials();
    }

    private void renderStartButton(GuiGraphics gfx, int x, int y, int mouseX, int mouseY) {
        boolean hover;
        int bx = x + START_BTN_X;
        int by = y + START_BTN_Y;
        if (this.menu.isCrafting()) {
            this.renderCraftingProgress(gfx, bx, by);
            return;
        }
        boolean enabled = this.canCraftEffective();
        hover = enabled && this.isMouseOverStartButton(mouseX, mouseY);
        int bg = !enabled ? BTN_BG_DISABLED : (hover ? BTN_BG_HOVER : BTN_BG);
        gfx.fill(bx, by, bx + START_BTN_W, by + START_BTN_H, GOLD_BORDER);
        gfx.fill(bx + 1, by + 1, bx + START_BTN_W - 1, by + START_BTN_H - 1, bg);
        MutableComponent line1 = Component.translatable("screen.friday_cultivation.alchemy.start_btn");
        AlchemyRecipe recipe = this.getEffectiveRecipe();
        int possible = recipe == null ? 0 : this.menu.countPossiblePillsForRecipe(recipe);
        MutableComponent line2 = Component.translatable("screen.friday_cultivation.alchemy.craft_count", possible);
        int color = enabled ? INK_BLACK : -12766424;
        int w1 = this.font.width((FormattedText) line1);
        int w2 = this.font.width((FormattedText) line2);
        gfx.drawString(this.font, line1, bx + (START_BTN_W - w1) / 2, by + 8, color, false);
        gfx.drawString(this.font, line2, bx + (START_BTN_W - w2) / 2, by + 22, color, false);
    }

    private void renderCraftingProgress(GuiGraphics gfx, int bx, int by) {
        int ticks = this.menu.getCraftingTicks();
        int total = Math.max(1, this.menu.getCraftingTotalTicks());
        float pct = Math.min(1.0f, (float) ticks / (float) total);
        gfx.fill(bx, by, bx + START_BTN_W, by + START_BTN_H, GOLD_BORDER);
        gfx.fill(bx + 1, by + 1, bx + START_BTN_W - 1, by + START_BTN_H - 1, QI_BAR_BG);
        int fillW = (int) (70.0f * pct);
        if (fillW > 0) {
            gfx.fill(bx + 1, by + 1, bx + 1 + fillW, by + 1 + 17, -8080);
            gfx.fill(bx + 1, by + 1 + 17, bx + 1 + fillW, by + START_BTN_H - 1, -2076656);
        }
        MutableComponent line1 = Component.translatable("screen.friday_cultivation.alchemy.crafting");
        int pctInt = (int) (pct * 100.0f);
        MutableComponent line2 = Component.literal(pctInt + " %");
        int w1 = this.font.width((FormattedText) line1);
        int w2 = this.font.width((FormattedText) line2);
        gfx.drawString(this.font, line1, bx + (START_BTN_W - w1) / 2, by + 8, -1, true);
        gfx.drawString(this.font, line2, bx + (START_BTN_W - w2) / 2, by + 22, -1, true);
    }

    @Override
    protected void renderLabels(@NotNull GuiGraphics gfx, int mouseX, int mouseY) {
        gfx.drawString(this.font, this.title, this.titleLabelX, this.titleLabelY, VERMILLION, false);
        gfx.drawString(this.font, this.playerInventoryTitle, this.inventoryLabelX, this.inventoryLabelY, INK_BLACK, false);
        List<AlchemyRecipe> recipes = this.menu.getAllRecipes();
        AlchemyRecipe matchedByMaterials = this.getEffectiveRecipe();
        this.clampRecipeScroll(recipes.size());
        int visibleRows = this.visibleRecipeRows(recipes.size());
        int rowRight = this.recipeRowRight(recipes.size());
        for (int visibleIndex = 0; visibleIndex < visibleRows; ++visibleIndex) {
            boolean highlighted;
            int recipeIndex = this.recipeScrollOffset + visibleIndex;
            AlchemyRecipe r = recipes.get(recipeIndex);
            int rx = 6;
            int ry = 34 + visibleIndex * RECIPE_ROW_H;
            highlighted = matchedByMaterials != null && matchedByMaterials.id().equals(r.id());
            if (highlighted) {
                gfx.fill(rx - 1, ry - 1, rowRight, ry + RECIPE_ROW_H - 1, GOLD_BRIGHT);
            }
            ItemStack icon = new ItemStack((ItemLike) r.iconItem());
            gfx.renderItem(icon, rx, ry);
            String name = r.displayName().getString();
            int color = highlighted ? INK_BLACK : INK_SOFT;
            String trimmedName = this.font.plainSubstrByWidth(name, Math.max(1, rowRight - rx - 19));
            gfx.drawString(this.font, trimmedName, rx + 18, ry + 4, color, false);
        }
        this.renderRecipeScrollbar(gfx, recipes.size());
    }

    private void renderHoveredRecipeTooltip(GuiGraphics gfx, int mouseX, int mouseY) {
        List<AlchemyRecipe> recipes = this.menu.getAllRecipes();
        int recipeIndex = this.recipeIndexAt(mouseX, mouseY, recipes.size());
        if (recipeIndex >= 0) {
            this.renderRecipeTooltip(gfx, recipes.get(recipeIndex), mouseX, mouseY);
        }
    }

    private void renderRecipeTooltip(GuiGraphics gfx, AlchemyRecipe recipe, int mouseX, int mouseY) {
        List<Component> lines = new ArrayList<>();
        lines.add(recipe.displayName().copy().withStyle(ChatFormatting.GOLD));
        lines.add(Component.translatable("screen.friday_cultivation.alchemy.tooltip.ingredients").withStyle(ChatFormatting.GRAY));
        for (AlchemyRecipe.IngredientEntry ingredientEntry : recipe.ingredientList()) {
            lines.add(Component.literal("  \u00b7 ").append(ingredientEntry.item().getDefaultInstance().getHoverName()).append(Component.literal(" \u00d7 " + ingredientEntry.count())).withStyle(ChatFormatting.WHITE));
        }
        lines.add(Component.translatable("screen.friday_cultivation.alchemy.tooltip.qi_per_pill", recipe.qiCostPerPill()).withStyle(ChatFormatting.AQUA));
        lines.add(Component.translatable("screen.friday_cultivation.alchemy.tooltip.outputs").withStyle(ChatFormatting.GRAY));
        for (Map.Entry<PillTier, Item> entry : recipe.outputs().entrySet()) {
            PillTier tier = entry.getKey();
            lines.add(Component.literal("  \u00b7 ").append(tier.displayName().copy().withStyle(tier.color())));
        }
        gfx.renderComponentTooltip(this.font, lines, mouseX, mouseY);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        List<AlchemyRecipe> recipes = this.menu.getAllRecipes();
        if (button == 0 && this.isMouseOverRecipeScrollbar(mouseX, mouseY, recipes.size())) {
            this.beginRecipeScrollbarDrag(mouseY, recipes.size());
            this.playClick();
            return true;
        }
        int recipeIndex = this.recipeIndexAt(mouseX, mouseY, recipes.size());
        if (recipeIndex >= 0) {
            this.playClick();
            AlchemyRecipe recipe = recipes.get(recipeIndex);
            ModNetwork.CHANNEL.sendToServer(new AutoFillRecipePacket(recipe.id()));
            return true;
        }
        if (button == 0 && this.isMouseOverStartButton((int) mouseX, (int) mouseY) && !this.menu.isCrafting()) {
            AlchemyRecipe recipe;
            if (this.canCraftEffective() && (recipe = this.getEffectiveRecipe()) != null) {
                ModNetwork.CHANNEL.sendToServer(new ExecuteAlchemyPacket(recipe.id()));
                this.playClick();
            }
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        List<AlchemyRecipe> recipes = this.menu.getAllRecipes();
        if (this.isMouseOverRecipePanel(mouseX, mouseY) && this.scrollRecipes(delta, recipes.size())) {
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, delta);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (this.draggingRecipeScrollbar) {
            this.updateRecipeScrollFromScrollbar(mouseY, this.menu.getAllRecipes().size());
            return true;
        }
        return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (this.draggingRecipeScrollbar) {
            this.draggingRecipeScrollbar = false;
            return true;
        }
        return super.mouseReleased(mouseX, mouseY, button);
    }

    private int recipeVisibleCapacity() {
        return Math.max(1, 8);
    }

    private int visibleRecipeRows(int recipeCount) {
        return Math.min(this.recipeVisibleCapacity(), Math.max(0, recipeCount - this.recipeScrollOffset));
    }

    private int maxRecipeScrollOffset(int recipeCount) {
        return Math.max(0, recipeCount - this.recipeVisibleCapacity());
    }

    private void clampRecipeScroll(int recipeCount) {
        int maxOffset = this.maxRecipeScrollOffset(recipeCount);
        this.recipeScrollOffset = Math.max(0, Math.min(this.recipeScrollOffset, maxOffset));
    }

    private boolean hasRecipeScrollbar(int recipeCount) {
        return recipeCount > this.recipeVisibleCapacity();
    }

    private int recipeScrollbarX() {
        return 83;
    }

    private int recipeScrollbarTop() {
        return 34;
    }

    private int recipeScrollbarBottom() {
        return 192;
    }

    private int recipeScrollbarThumbH(int recipeCount) {
        int trackH = this.recipeScrollbarBottom() - this.recipeScrollbarTop();
        return Math.max(18, trackH * this.recipeVisibleCapacity() / Math.max(1, recipeCount));
    }

    private int recipeScrollbarThumbY(int recipeCount) {
        int maxOffset = this.maxRecipeScrollOffset(recipeCount);
        if (maxOffset <= 0) {
            return this.recipeScrollbarTop();
        }
        int trackH = this.recipeScrollbarBottom() - this.recipeScrollbarTop();
        int thumbH = this.recipeScrollbarThumbH(recipeCount);
        return this.recipeScrollbarTop() + (trackH - thumbH) * this.recipeScrollOffset / maxOffset;
    }

    private int recipeRowRight(int recipeCount) {
        if (this.hasRecipeScrollbar(recipeCount)) {
            return this.recipeScrollbarX() - 2;
        }
        return 86;
    }

    private void renderRecipeScrollbar(GuiGraphics gfx, int recipeCount) {
        if (!this.hasRecipeScrollbar(recipeCount)) {
            return;
        }
        int x = this.recipeScrollbarX();
        int top = this.recipeScrollbarTop();
        int bottom = this.recipeScrollbarBottom();
        int thumbY = this.recipeScrollbarThumbY(recipeCount);
        int thumbH = this.recipeScrollbarThumbH(recipeCount);
        gfx.fill(x, top, x + RECIPE_SCROLLBAR_W, bottom, -9610690);
        gfx.fill(x + 1, top + 1, x + RECIPE_SCROLLBAR_W - 1, bottom - 1, -4218006);
        gfx.fill(x, thumbY, x + RECIPE_SCROLLBAR_W, thumbY + thumbH, VERMILLION);
        gfx.fill(x + 1, thumbY + 1, x + RECIPE_SCROLLBAR_W - 1, thumbY + thumbH - 1, -2656414);
    }

    private boolean isMouseOverRecipePanel(double mouseX, double mouseY) {
        int x1 = this.leftPos + RECIPE_PANEL_X;
        int y1 = this.topPos + RECIPE_PANEL_Y;
        return mouseX >= x1 && mouseX < x1 + RECIPE_PANEL_W && mouseY >= y1 && mouseY < y1 + RECIPE_PANEL_H;
    }

    private boolean isMouseOverRecipeScrollbar(double mouseX, double mouseY, int recipeCount) {
        if (!this.hasRecipeScrollbar(recipeCount)) {
            return false;
        }
        int x = this.leftPos + this.recipeScrollbarX();
        int top = this.topPos + this.recipeScrollbarTop();
        int bottom = this.topPos + this.recipeScrollbarBottom();
        return mouseX >= x - 1 && mouseX < x + RECIPE_SCROLLBAR_W + 1 && mouseY >= top && mouseY < bottom;
    }

    private int recipeIndexAt(double mouseX, double mouseY, int recipeCount) {
        if (!this.isMouseOverRecipePanel(mouseX, mouseY)) {
            return -1;
        }
        int relX = (int) mouseX - this.leftPos;
        int relY = (int) mouseY - this.topPos;
        int rowRight = this.recipeRowRight(recipeCount);
        int rowLeft = 5;
        if (relX < rowLeft || relX >= rowRight) {
            return -1;
        }
        int row = (relY - RECIPE_PANEL_Y - 2) / RECIPE_ROW_H;
        if (row < 0 || row >= this.recipeVisibleCapacity()) {
            return -1;
        }
        int recipeIndex = this.recipeScrollOffset + row;
        return recipeIndex >= 0 && recipeIndex < recipeCount ? recipeIndex : -1;
    }

    private boolean scrollRecipes(double delta, int recipeCount) {
        int maxOffset = this.maxRecipeScrollOffset(recipeCount);
        if (maxOffset <= 0) {
            return false;
        }
        int old = this.recipeScrollOffset;
        int direction = (int) Math.signum(delta);
        this.recipeScrollOffset = Math.max(0, Math.min(maxOffset, this.recipeScrollOffset - direction));
        return old != this.recipeScrollOffset;
    }

    private void beginRecipeScrollbarDrag(double mouseY, int recipeCount) {
        int thumbTop = this.topPos + this.recipeScrollbarThumbY(recipeCount);
        int thumbBottom = thumbTop + this.recipeScrollbarThumbH(recipeCount);
        this.recipeScrollbarDragOffsetY = mouseY >= thumbTop && mouseY < thumbBottom ? (int) mouseY - thumbTop : this.recipeScrollbarThumbH(recipeCount) / 2;
        this.draggingRecipeScrollbar = true;
        this.updateRecipeScrollFromScrollbar(mouseY, recipeCount);
    }

    private void updateRecipeScrollFromScrollbar(double mouseY, int recipeCount) {
        int maxOffset = this.maxRecipeScrollOffset(recipeCount);
        if (maxOffset <= 0) {
            this.recipeScrollOffset = 0;
            return;
        }
        int top = this.topPos + this.recipeScrollbarTop();
        int trackH = this.recipeScrollbarBottom() - this.recipeScrollbarTop();
        int thumbH = this.recipeScrollbarThumbH(recipeCount);
        int travel = Math.max(1, trackH - thumbH);
        int thumbTop = (int) mouseY - this.recipeScrollbarDragOffsetY;
        int clampedThumbTop = Math.max(top, Math.min(top + travel, thumbTop));
        this.recipeScrollOffset = Math.round((float) (clampedThumbTop - top) * maxOffset / (float) travel);
    }

    private void playClick() {
        if (this.minecraft != null && this.minecraft.getSoundManager() != null) {
            this.minecraft.getSoundManager().play((SoundInstance) SimpleSoundInstance.forUI((Holder<net.minecraft.sounds.SoundEvent>) SoundEvents.UI_BUTTON_CLICK, 1.0f));
        }
    }

    @Override
    public void render(@NotNull GuiGraphics gfx, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(gfx);
        super.render(gfx, mouseX, mouseY, partialTick);
        this.renderTooltip(gfx, mouseX, mouseY);
        this.renderHoveredRecipeTooltip(gfx, mouseX, mouseY);
    }
}
