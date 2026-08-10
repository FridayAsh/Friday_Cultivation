/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.ChatFormatting
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.gui.GuiGraphics
 *  net.minecraft.client.gui.screens.Screen
 *  net.minecraft.client.gui.screens.inventory.InventoryScreen
 *  net.minecraft.nbt.CompoundTag
 *  net.minecraft.nbt.ListTag
 *  net.minecraft.network.chat.Component
 *  net.minecraft.network.chat.FormattedText
 *  net.minecraft.network.chat.MutableComponent
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.util.FormattedCharSequence
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.item.ItemStack
 */
package com.friday.cultivation.client.screen;

import com.friday.cultivation.cultivation.sect.SectRole;
import com.friday.cultivation.network.JoinSectPacket;
import com.friday.cultivation.network.ModNetwork;
import com.friday.cultivation.network.SectTaskActionPacket;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

public class SectJoinDialogueScreen
extends Screen {
    private static final int INK_BLACK = -15067628;
    private static final int INK_SOFT = -12766422;
    private static final int INK_MUTE = -9807288;
    private static final int INK_PALE = -7702689;
    private static final int VERMILLION = -4703686;
    private static final int VERMILLION_DEEP = -7723482;
    private static final int GOLD_BORDER = -3562934;
    private static final int BORDER_LIGHT = -2504802;
    private static final int PAGE = -923956;
    private static final int PANEL = -1517128;
    private static final int CARD = -661304;
    private static final int HEADER_WASH = 583639626;
    private static final int JADE = -13664921;
    private static final int JADE_DARK = -14722744;
    private static final int AMBER = -4818904;
    private static final int DISABLED = -7700107;
    private static final int TASK_CARD_HEIGHT = 82;
    private static final int TASK_CARD_SPACING = 88;
    private static final int SCROLL_BAR_WIDTH = 3;
    private static final int SCROLL_BAR_HIT_PADDING = 3;
    private static final int OPTION_HEIGHT = 20;
    private static final int OPTION_SPACING = 23;
    private static final float OPTION_TEXT_SCALE = 0.72f;
    private static final float DIALOGUE_TEXT_SCALE = 0.76f;
    private static final int WASH_RGB = 16249313;
    private static final int NAME_PLATE_RGB = 4411994;
    private static final ResourceLocation CHOICE_BRUSH_TEXTURE = new ResourceLocation((String)"friday_cultivation", (String)"textures/gui/sect_dialogue_choice_brush.png");
    private static final ResourceLocation MESSAGE_BRUSH_TEXTURE = new ResourceLocation((String)"friday_cultivation", (String)"textures/gui/sect_dialogue_message_brush.png");
    private static final int CHOICE_BRUSH_TEX_W = 768;
    private static final int CHOICE_BRUSH_TEX_H = 96;
    private static final int MESSAGE_BRUSH_TEX_W = 1024;
    private static final int MESSAGE_BRUSH_TEX_H = 192;
    private static final ScrollMetrics NO_SCROLL = new ScrollMetrics(0, 0, 0, 0, 0, 0, 0);
    private final int targetEntityId;
    private final String sectName;
    private final String npcName;
    private final CompoundTag snapshot;
    private final List<TaskAction> taskActions = new ArrayList<TaskAction>();
    private final List<TaskLine> taskLines = new ArrayList<TaskLine>();
    private final List<HoverTipRect> hoverTipRects = new ArrayList<HoverTipRect>();
    private final List<ItemHoverRect> itemHoverRects = new ArrayList<ItemHoverRect>();
    private final List<OptionClickRect> optionClickRects = new ArrayList<OptionClickRect>();
    private boolean canJoin;
    private boolean viewerMember;
    private boolean viewerEnemy;
    private SectRole targetRole = SectRole.NONE;
    private SectRole viewerRole = SectRole.NONE;
    private int viewerContribution;
    private int taskScroll;
    private boolean draggingTaskScroll;
    private double taskScrollGrabOffset;
    private ScrollMetrics lastTaskScrollMetrics = NO_SCROLL;

    public SectJoinDialogueScreen(int targetEntityId, String sectName, String npcName, CompoundTag snapshot) {
        super((Component)Component.translatable((String)"screen.friday_cultivation.sect.dialogue.title", (Object[])new Object[]{Component.literal((String)SectJoinDialogueScreen.safe(sectName))}));
        this.targetEntityId = targetEntityId;
        this.sectName = SectJoinDialogueScreen.safe(sectName);
        this.npcName = SectJoinDialogueScreen.safe(npcName);
        this.snapshot = snapshot == null ? new CompoundTag() : snapshot.copy();
        this.readSnapshot();
    }

    private void readSnapshot() {
        this.canJoin = this.snapshot.getBoolean("canJoin");
        this.viewerMember = this.snapshot.getBoolean("viewerMember");
        this.viewerEnemy = this.snapshot.getBoolean("viewerEnemy");
        this.targetRole = SectRole.byId(this.snapshot.getString("targetRole"));
        this.viewerRole = SectRole.byId(this.snapshot.getString("viewerRole"));
        this.viewerContribution = this.snapshot.getInt("viewerContribution");
        this.taskActions.clear();
        this.taskLines.clear();
        ListTag tasks = this.snapshot.getList("tasks", 10);
        for (int i = 0; i < tasks.size(); ++i) {
            CompoundTag row = tasks.getCompound(i);
            TaskLine taskLine = new TaskLine(row.getString("id"), row.getString("issuerName"), SectRole.byId(row.getString("issuerRole")), row.getString("titleKey"), row.getString("conditionKey"), row.getInt("contribution"), row.contains("requiredStack", 10) ? ItemStack.of((CompoundTag)row.getCompound("requiredStack")) : ItemStack.EMPTY, row.contains("heldRequired", 3) ? row.getInt("heldRequired") : 0, row.contains("requiredCount", 3) ? row.getInt("requiredCount") : 0, row.contains("rewardStack", 10) ? ItemStack.of((CompoundTag)row.getCompound("rewardStack")) : ItemStack.EMPTY, row.getString("rewardKind"), row.getBoolean("accepted"), row.getBoolean("completed"), row.getBoolean("ready"), !row.contains("rewardAvailable", 1) || row.getBoolean("rewardAvailable"), row.getBoolean("requiresIssuer"), row.getBoolean("canAccept"), row.getBoolean("canTurnIn"));
            this.taskLines.add(taskLine);
        }
        this.taskLines.sort(Comparator.comparingInt(SectJoinDialogueScreen::taskPriority).thenComparing(task -> task.issuerRole().rank()).thenComparing(TaskLine::issuerName).thenComparing(TaskLine::titleKey).thenComparing(TaskLine::id));
        for (TaskLine taskLine : this.taskLines) {
            if (taskLine.canTurnIn()) {
                this.taskActions.add(new TaskAction(taskLine.id(), taskLine.titleKey(), true));
                continue;
            }
            if (!taskLine.canAccept()) continue;
            this.taskActions.add(new TaskAction(taskLine.id(), taskLine.titleKey(), false));
        }
    }

    private List<DialogueOption> dialogueOptions() {
        ArrayList<DialogueOption> options = new ArrayList<DialogueOption>();
        if (this.canJoin) {
            options.add(new DialogueOption((Component)Component.translatable((String)"screen.friday_cultivation.sect.dialogue.option.join"), () -> {
                ModNetwork.CHANNEL.sendToServer((Object)new JoinSectPacket(this.targetEntityId));
                Minecraft.getInstance().setScreen(null);
            }, true, "join_option"));
        }
        for (TaskAction task : this.taskActions) {
            MutableComponent taskTitle = Component.translatable((String)task.titleKey());
            String key = task.turnIn() ? ".sect.dialogue.task.turn_in_option" : ".sect.dialogue.task.accept_option";
            options.add(new DialogueOption((Component)Component.translatable((String)("screen.friday_cultivation" + key), (Object[])new Object[]{taskTitle}), () -> {
                ModNetwork.CHANNEL.sendToServer((Object)new SectTaskActionPacket(this.targetEntityId, task.id(), task.turnIn()));
                Minecraft.getInstance().setScreen(null);
            }, true, task.turnIn() ? "task_turn_in_option" : "task_accept_option"));
        }
        options.add(new DialogueOption((Component)Component.translatable((String)"screen.friday_cultivation.sect.dialogue.option.leave"), () -> Minecraft.getInstance().setScreen(null), false, "leave_option"));
        return options;
    }

    public void render(GuiGraphics gfx, int mouseX, int mouseY, float partialTick) {
        List<DialogueOption> options = this.dialogueOptions();
        this.hoverTipRects.clear();
        this.itemHoverRects.clear();
        this.optionClickRects.clear();
        this.lastTaskScrollMetrics = NO_SCROLL;
        LivingEntity dialogueNpc = this.targetLivingEntity();
        this.renderNpcFullBody(gfx, dialogueNpc);
        this.renderFloatingOptions(gfx, options, mouseX, mouseY);
        this.renderDialogueScroll(gfx, dialogueNpc);
        super.render(gfx, mouseX, mouseY, partialTick);
        this.renderHoverTooltip(gfx, mouseX, mouseY);
    }

    private LivingEntity targetLivingEntity() {
        LivingEntity living;
        Minecraft mc = Minecraft.getInstance();
        Entity entity = mc.level == null ? null : mc.level.getEntity(this.targetEntityId);
        return entity instanceof LivingEntity ? (living = (LivingEntity)entity) : null;
    }

    private void renderNpcFullBody(GuiGraphics gfx, LivingEntity npc) {
        if (npc == null || this.width < 320 || this.height < 190) {
            return;
        }
        int dialogueTop = this.dialogueY(this.dialogueHeight());
        int baseY = Math.max(80, dialogueTop - 2);
        int x = this.width >= 700 ? Math.max(72, this.width / 8) : Math.max(44, this.width / 8);
        int scale = SectJoinDialogueScreen.clamp((baseY - 18) / 2, 26, this.width >= 700 ? 82 : 58);
        gfx.fill(x - scale + 7, baseY - 3, x + scale - 7, baseY + 1, 860635701);
        InventoryScreen.renderEntityInInventoryFollowsMouse((GuiGraphics)gfx, (int)x, (int)baseY, (int)scale, (float)-58.0f, (float)-18.0f, (LivingEntity)npc);
    }

    private void renderFloatingOptions(GuiGraphics gfx, List<DialogueOption> options, int mouseX, int mouseY) {
        int bottomReserve = this.height - this.dialogueY(this.dialogueHeight()) + 12;
        int optionW = Math.min(192, Math.max(144, this.width / 5));
        int optionH = Math.max(14, Math.min(18, this.height / 20));
        int gap = Math.max(5, Math.min(10, this.height / 48));
        if (!options.isEmpty()) {
            int availableH = Math.max(58, this.height - bottomReserve - 36);
            gap = Math.max(6, Math.min(gap, Math.max(6, availableH / Math.max(1, options.size()) / 3)));
            optionH = Math.max(12, Math.min(optionH, (availableH - Math.max(0, options.size() - 1) * gap) / options.size()));
        }
        int totalH = options.size() * optionH + Math.max(0, options.size() - 1) * gap;
        int centerX = this.width / 2 + (this.width >= 700 ? Math.min(36, this.width / 24) : 0);
        int x = SectJoinDialogueScreen.clamp(centerX - optionW / 2, 14, Math.max(14, this.width - optionW - 14));
        int y = SectJoinDialogueScreen.clamp(this.height / 3 - totalH / 2, 22, Math.max(24, this.height - bottomReserve - totalH - 16));
        for (int i = 0; i < options.size(); ++i) {
            int rowY = y + i * (optionH + gap);
            boolean hovered = mouseX >= x && mouseX < x + optionW && mouseY >= rowY && mouseY < rowY + optionH;
            this.drawBrushChoice(gfx, x, rowY, optionW, optionH, options.get(i).label(), hovered, options.get(i).emphasis());
            this.optionClickRects.add(new OptionClickRect(x, rowY, optionW, optionH, i));
            this.hoverTipRects.add(SectJoinDialogueScreen.tooltipRect(x, rowY, optionW, optionH, options.get(i).tooltipSuffix()));
        }
    }

    private void drawBrushChoice(GuiGraphics gfx, int x, int y, int width, int height, Component label, boolean hovered, boolean emphasis) {
        this.drawBrushTexture(gfx, CHOICE_BRUSH_TEXTURE, x, y, width, height, 768, 96, true);
        if (hovered) {
            gfx.fill(x + width / 5, y + 3, x + width * 4 / 5, y + height - 3, 0x20FFFFFF);
        }
        int textMax = Math.max(10, (int)((float)(width - 24) / 0.72f));
        Component component = this.trimToWidth((Component)label.copy().withStyle(ChatFormatting.BOLD), textMax);
        int n = x + width / 2;
        float f = height;
        Objects.requireNonNull(this.font);
        this.drawScaledCentered(gfx, component, n, y + Math.max(2, Math.round((f - 9.0f * 0.72f) / 2.0f)), -15067628, 0.72f);
    }

    /*
     * Unable to fully structure code
     */
    private void renderDialogueScroll(GuiGraphics gfx, LivingEntity npc) {
        int scrollH = this.dialogueHeight();
        int scrollY = this.dialogueY(scrollH);
        int scrollW = Math.min(this.width - 40, Math.max(260, this.width / 2));
        int scrollX = SectJoinDialogueScreen.clamp((this.width - scrollW) / 2 + (this.width >= 700 ? Math.min(24, this.width / 34) : 0), 14, Math.max(14, this.width - scrollW - 14));
        this.drawBrushTexture(gfx, SectJoinDialogueScreen.MESSAGE_BRUSH_TEXTURE, scrollX, scrollY, scrollW, scrollH, 1024, 192, true);
        int avatarSize = Math.min(38, Math.max(28, scrollH - 14));
        int avatarX = scrollX + 18;
        int avatarY = scrollY + (scrollH - avatarSize) / 2;
        this.drawInkWashRect(gfx, avatarX - 3, avatarY - 3, avatarSize + 6, avatarSize + 6, 15853260, 190, false);
        this.renderNpcPortrait(gfx, npc, avatarX, avatarY, avatarSize);
        int nameW = Math.min(Math.max(118, scrollW - avatarSize - 58), Math.max(128, (int) ((float) (this.font.width(Component.literal(this.npcName)) + this.font.width(this.targetRole.displayName()) + 40) * 0.76f)));
        int nameX = scrollX + avatarSize + 28;
        int nameY = scrollY - 14;
        this.drawInkWashRect(gfx, nameX, nameY, nameW, 14, 4411994, 220, false);
        int splitX = nameX + Math.max(58, nameW / 2);
        this.drawScaledString(gfx, this.trimToWidth(Component.literal(this.npcName).withStyle(ChatFormatting.BOLD), Math.max(8, (int) ((float) (splitX - nameX - 10) / 0.76f))), nameX + 9, nameY + 3, -528679, 0.76f, false);
        this.drawScaledString(gfx, this.trimToWidth(this.targetRole.displayName(), Math.max(8, (int) ((float) (nameX + nameW - splitX - 8) / 0.76f))), splitX + 4, nameY + 3, -1386838, 0.76f, false);
        int textX = avatarX + avatarSize + 14;
        int textY = scrollY + 12;
        int textW = scrollX + scrollW - textX - 20;
        int nextY = this.drawWrappedScaledLimited(gfx, this.bodyText(), textX, textY, textW, -12766422, scrollH >= 58 ? 2 : 1, 0.76f);
        if (!this.taskLines.isEmpty()) {
            int v0 = nextY;
            Objects.requireNonNull(this.font);
            if ((float) v0 + 9.0f * 0.76f + 2.0f < (float) (scrollY + scrollH - 5)) {
                TaskLine task = this.taskLines.get(SectJoinDialogueScreen.clamp(this.taskScroll, 0, this.taskLines.size() - 1));
                this.drawScaledString(gfx, this.trimToWidth(this.taskStatus(task).plainCopy().withStyle(ChatFormatting.BOLD), Math.max(8, (int) ((float) textW / 0.76f))), textX, nextY + 2, this.taskStatusColor(task), 0.76f, false);
            }
        } else {
            int v1 = nextY;
            Objects.requireNonNull(this.font);
            if ((float) v1 + 9.0f * 0.76f + 2.0f < (float) (scrollY + scrollH - 5)) {
                this.drawScaledString(gfx, this.trimToWidth(this.actionSummary(), Math.max(8, (int) ((float) textW / 0.76f))), textX, nextY + 2, this.viewerEnemy ? -7723482 : -14722744, 0.76f, false);
            }
        }
        gfx.fill(textX, scrollY + scrollH - 9, scrollX + scrollW - 28, scrollY + scrollH - 8, 1155123102);
    }

    private int dialogueHeight() {
        return Math.min(66, Math.max(48, this.height / 6));
    }

    private int dialogueHudReserve() {
        return Math.min(82, Math.max(64, this.height / 5));
    }

    private int dialogueY(int dialogueHeight) {
        return Math.max(60, this.height - this.dialogueHudReserve() - dialogueHeight);
    }

    private void renderNpcPortrait(GuiGraphics gfx, LivingEntity npc, int x, int y, int size) {
        if (npc == null) {
            this.drawMiniGlyph(gfx, x + size / 2 - 5, y + size / 2 - 5, 10, -9807288, false);
            return;
        }
        int scale = Math.max(13, size / 2);
        InventoryScreen.renderEntityInInventoryFollowsMouse((GuiGraphics)gfx, (int)(x + size / 2), (int)(y + size - 4), (int)scale, (float)-20.0f, (float)-10.0f, (LivingEntity)npc);
    }

    private void drawBrushTexture(GuiGraphics gfx, ResourceLocation texture, int x, int y, int width, int height, int textureWidth, int textureHeight, boolean shadow) {
        if (width <= 0 || height <= 0) {
            return;
        }
        if (shadow) {
            int inset = Math.max(8, width / 18);
            gfx.fill(x + inset, y + 4, x + width - inset, y + height + 4, 808856608);
        }
        gfx.blit(texture, x, y, width, height, 0.0f, 0.0f, textureWidth, textureHeight, textureWidth, textureHeight);
    }

    private void drawInkWashRect(GuiGraphics gfx, int x, int y, int width, int height, int rgb, int centerAlpha, boolean shadow) {
        if (width <= 0 || height <= 0) {
            return;
        }
        if (shadow) {
            gfx.fill(x + 18, y + 4, x + width - 18, y + height + 4, 808856608);
        }
        int edge = Math.max(18, Math.min(44, width / 6));
        int inset = Math.max(12, Math.min(edge, width / 5));
        int bodyY1 = y + Math.max(1, height / 10);
        int bodyY2 = y + height - Math.max(1, height / 9);
        gfx.fill(x + inset, bodyY1, x + width - inset, bodyY2, SectJoinDialogueScreen.argb(centerAlpha, rgb));
        int bands = Math.max(5, Math.min(11, height / 3 + 2));
        for (int i = 0; i < bands; ++i) {
            int bandY2 = y + (i + 1) * height / bands;
            int bandY1 = y + i * height / bands;
            if (bandY2 <= bandY1) {
                bandY2 = bandY1 + 1;
            }
            int leftNib = SectJoinDialogueScreen.brushNib(i, width, false);
            int rightNib = SectJoinDialogueScreen.brushNib(i, width, true);
            int leftEnd = x + inset + Math.max(4, edge * (45 + i * 17 % 42) / 100);
            int rightStart = x + width - inset - Math.max(4, edge * (38 + i * 23 % 48) / 100);
            int alpha = centerAlpha * (48 + i * 19 % 36) / 100;
            gfx.fill(x + leftNib, bandY1, leftEnd, bandY2, SectJoinDialogueScreen.argb(alpha, rgb));
            gfx.fill(rightStart, bandY1, x + width - rightNib, bandY2, SectJoinDialogueScreen.argb(alpha, rgb));
        }
        int dryAlpha = Math.max(18, centerAlpha / 5);
        for (int i = 0; i < 6; ++i) {
            int topY = y + (i * 7 + height / 5) % Math.max(1, height - 1);
            int leftX = x + 3 + i * 11 % Math.max(4, inset);
            int leftW = Math.max(6, inset + i * 13 % Math.max(8, edge));
            gfx.fill(leftX, topY, Math.min(x + width / 2, leftX + leftW), topY + 1, SectJoinDialogueScreen.argb(dryAlpha + i * 6, rgb));
            int rightW = Math.max(6, inset + i * 9 % Math.max(8, edge));
            int rightX = x + width - rightW - 3 - i * 5 % Math.max(4, inset);
            gfx.fill(Math.max(x + width / 2, rightX), y + (i * 5 + 2) % Math.max(1, height - 1), x + width - 3, y + (i * 5 + 2) % Math.max(1, height - 1) + 1, SectJoinDialogueScreen.argb(dryAlpha + i * 5, rgb));
        }
    }

    private static int brushNib(int index, int width, boolean rightSide) {
        int[] nArray;
        if (rightSide) {
            int[] nArray2 = new int[11];
            nArray2[0] = 3;
            nArray2[1] = 10;
            nArray2[2] = 1;
            nArray2[3] = 15;
            nArray2[4] = 6;
            nArray2[5] = 20;
            nArray2[6] = 2;
            nArray2[7] = 12;
            nArray2[8] = 8;
            nArray2[9] = 0;
            nArray = nArray2;
            nArray2[10] = 17;
        } else {
            int[] nArray3 = new int[11];
            nArray3[0] = 14;
            nArray3[1] = 5;
            nArray3[2] = 0;
            nArray3[3] = 18;
            nArray3[4] = 8;
            nArray3[5] = 2;
            nArray3[6] = 21;
            nArray3[7] = 7;
            nArray3[8] = 12;
            nArray3[9] = 1;
            nArray = nArray3;
            nArray3[10] = 16;
        }
        int[] pattern = nArray;
        int max = Math.max(2, Math.min(24, width / 9));
        return pattern[index % pattern.length] * max / 24;
    }

    private static int argb(int alpha, int rgb) {
        return SectJoinDialogueScreen.clamp(alpha, 0, 255) << 24 | rgb & 0xFFFFFF;
    }

    private int drawWrappedScaledLimited(GuiGraphics gfx, Component text, int x, int y, int maxWidth, int color, int maxLines, float scale) {
        int lineCount = 0;
        int splitWidth = Math.max(8, (int)((float)maxWidth / scale));
        for (FormattedCharSequence line : this.font.split((FormattedText)text, splitWidth)) {
            if (lineCount >= maxLines) break;
            this.drawScaledString(gfx, line, x, y, color, scale, false);
            Objects.requireNonNull(this.font);
            y += Math.round((float)(9 + 2) * scale);
            ++lineCount;
        }
        return y;
    }

    private void drawScaledCentered(GuiGraphics gfx, Component text, int centerX, int y, int color, float scale) {
        int drawX = Math.round((float)centerX - (float)this.font.width((FormattedText)text) * scale / 2.0f);
        this.drawScaledString(gfx, text, drawX, y, color, scale, false);
    }

    private void drawScaledString(GuiGraphics gfx, Component text, int x, int y, int color, float scale, boolean shadow) {
        gfx.pose().pushPose();
        gfx.pose().scale(scale, scale, 1.0f);
        gfx.drawString(this.font, text, Math.round((float)x / scale), Math.round((float)y / scale), color, shadow);
        gfx.pose().popPose();
    }

    private void drawScaledString(GuiGraphics gfx, FormattedCharSequence text, int x, int y, int color, float scale, boolean shadow) {
        gfx.pose().pushPose();
        gfx.pose().scale(scale, scale, 1.0f);
        gfx.drawString(this.font, text, Math.round((float)x / scale), Math.round((float)y / scale), color, shadow);
        gfx.pose().popPose();
    }

    private void drawScrollEndCap(GuiGraphics gfx, int x, int y, int height, boolean leftSide) {
        int shade = leftSide ? 1714826272 : 1429613600;
        gfx.fill(x, y, x + 6, y + height, shade);
        gfx.fill(x + 1, y + 2, x + 5, y + height - 2, -1428568162);
        gfx.fill(x + 2, y + 8, x + 4, y + height - 8, 1727524569);
    }

    private void renderPanel(GuiGraphics gfx, Layout layout, List<DialogueOption> options, int mouseX, int mouseY) {
        this.hoverTipRects.clear();
        this.itemHoverRects.clear();
        gfx.fill(layout.left - 3, layout.top - 3, layout.left + layout.width + 3, layout.top + layout.height + 3, -15463674);
        gfx.fill(layout.left - 2, layout.top - 2, layout.left + layout.width + 2, layout.top + layout.height + 2, -2504802);
        gfx.fill(layout.left, layout.top, layout.left + layout.width, layout.top + layout.height, -923956);
        this.drawHeaderBackdrop(gfx, layout);
        MutableComponent header = Component.translatable((String)"screen.friday_cultivation.sect.dialogue.header", (Object[])new Object[]{Component.literal((String)this.npcName), Component.literal((String)this.sectName)});
        this.drawSectSeal(gfx, layout.left + 14, layout.top + 12, 14, this.headerAccentColor());
        gfx.drawString(this.font, this.trimToWidth((Component)header.copy().withStyle(ChatFormatting.BOLD), layout.width - 54), layout.left + 34, layout.top + 9, -7723482, false);
        this.drawHeaderMeta(gfx, layout.left + 34, layout.top + 21, layout.width - 46);
        boolean besideButtons = this.buttonsBeside(layout);
        int optionW = this.buttonWidth(layout);
        int optionX = this.buttonX(layout);
        int contentX = layout.left + 12;
        int contentTop = layout.top + 43;
        int contentW = besideButtons ? Math.max(150, optionX - contentX - 10) : layout.width - 24;
        int contentBottom = besideButtons ? layout.top + layout.height - 12 : this.buttonTop(layout, options.size()) - 6;
        this.drawContent(gfx, contentX, contentTop, contentW, contentBottom - contentTop);
        if (besideButtons) {
            int separatorX = optionX - 8;
            gfx.fill(separatorX, layout.top + 43, separatorX + 1, layout.top + layout.height - 14, 2001349914);
            gfx.drawString(this.font, (Component)Component.translatable((String)("screen.friday_cultivation" + (this.viewerEnemy ? ".sect.dialogue.action_hint_hostile" : ".sect.dialogue.action_hint"))), optionX, layout.top + 43, this.viewerEnemy ? -7723482 : -7702689, false);
            this.drawActionRail(gfx, layout, optionX, optionW, options.size());
        }
        int buttonY = this.buttonTop(layout, options.size());
        for (int i = 0; i < options.size(); ++i) {
            int y = buttonY + i * 23;
            boolean hovered = mouseX >= optionX && mouseX < optionX + optionW && mouseY >= y && mouseY < y + 20;
            this.drawDialogueButton(gfx, optionX, y, optionW, options.get(i).label(), hovered, options.get(i).emphasis());
            this.hoverTipRects.add(SectJoinDialogueScreen.tooltipRect(optionX, y, optionW, 20, options.get(i).tooltipSuffix()));
        }
    }

    private void drawActionRail(GuiGraphics gfx, Layout layout, int x, int width, int optionCount) {
        int y = layout.top + 58;
        int bottom = this.buttonTop(layout, optionCount) - 6;
        int cardBottom = Math.min(bottom, y + 72);
        if (cardBottom - y < 28) {
            return;
        }
        int accent = this.viewerEnemy ? -7723482 : (this.canJoin ? -14722744 : (!this.taskActions.isEmpty() ? -4818904 : -7700107));
        gfx.fill(x, y, x + width, cardBottom, 573975584);
        gfx.fill(x + 1, y + 1, x + width - 1, cardBottom - 1, 871886553);
        gfx.fill(x + 1, y + 1, x + 4, cardBottom - 1, accent);
        this.drawMiniGlyph(gfx, x + 9, y + 9, 10, accent, !this.viewerEnemy && (this.canJoin || !this.taskActions.isEmpty()));
        this.drawWrappedLimited(gfx, this.actionSummary(), x + 25, y + 7, width - 32, this.viewerEnemy ? -7723482 : -12766422, 3);
    }

    private void drawHeaderBackdrop(GuiGraphics gfx, Layout layout) {
        gfx.fill(layout.left + 7, layout.top + 6, layout.left + layout.width - 7, layout.top + 37, 583639626);
        gfx.fill(layout.left + 9, layout.top + 8, layout.left + 12, layout.top + 35, this.headerAccentColor());
        gfx.fill(layout.left + 16, layout.top + 27, layout.left + layout.width - 16, layout.top + 28, 1725548446);
        gfx.fill(layout.left + 7, layout.top + 36, layout.left + layout.width - 7, layout.top + 37, -3562934);
    }

    private void drawSectSeal(GuiGraphics gfx, int x, int y, int size, int accent) {
        int midX = x + size / 2;
        int midY = y + size / 2;
        gfx.fill(midX - 4, y, midX + 5, y + size, 1429613600);
        gfx.fill(x, midY - 4, x + size, midY + 5, 1429613600);
        gfx.fill(midX - 3, y + 1, midX + 4, y + size - 1, -661304);
        gfx.fill(x + 1, midY - 3, x + size - 1, midY + 4, -661304);
        gfx.fill(midX - 2, y + 2, midX + 3, y + size - 2, accent);
        gfx.fill(x + 2, midY - 2, x + size - 2, midY + 3, accent);
        gfx.fill(midX - 1, midY - 1, midX + 2, midY + 2, -528679);
    }

    private void drawMiniGlyph(GuiGraphics gfx, int x, int y, int size, int color, boolean filled) {
        int midX = x + size / 2;
        int midY = y + size / 2;
        gfx.fill(midX - 3, y, midX + 4, y + size, 1144400928);
        gfx.fill(x, midY - 3, x + size, midY + 4, 1144400928);
        gfx.fill(midX - 2, y + 1, midX + 3, y + size - 1, filled ? color : -661304);
        gfx.fill(x + 1, midY - 2, x + size - 1, midY + 3, filled ? color : -661304);
        gfx.fill(midX - 1, midY - 1, midX + 2, midY + 2, color);
    }

    private void drawHeaderMeta(GuiGraphics gfx, int x, int y, int width) {
        int viewerColor;
        MutableComponent viewer;
        MutableComponent npcRole = Component.translatable((String)"screen.friday_cultivation.sect.dialogue.npc_role", (Object[])new Object[]{this.targetRole.displayName()});
        if (this.viewerEnemy) {
            viewer = Component.translatable((String)"screen.friday_cultivation.sect.dialogue.viewer_hostile");
            viewerColor = -7723482;
        } else if (this.viewerMember) {
            viewer = Component.translatable((String)"screen.friday_cultivation.sect.dialogue.viewer_role", (Object[])new Object[]{this.viewerRole.displayName(), this.viewerContribution});
            viewerColor = SectJoinDialogueScreen.roleColor(this.viewerRole);
        } else {
            viewer = Component.translatable((String)"screen.friday_cultivation.sect.dialogue.viewer_guest");
            viewerColor = -7700107;
        }
        int roleW = Math.min(width / 2, Math.max(86, this.font.width((FormattedText)npcRole) + 12));
        this.drawChip(gfx, x, y, roleW, (Component)npcRole, SectJoinDialogueScreen.roleColor(this.targetRole));
        this.drawChip(gfx, x + roleW + 6, y, width - roleW - 6, (Component)viewer, viewerColor);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private void drawContent(GuiGraphics gfx, int x, int y, int width, int height) {
        int noticeY = 0;
        int noticeH = 0;
        this.lastTaskScrollMetrics = NO_SCROLL;
        gfx.fill(x, y, x + width, y + height, 1144400928);
        gfx.fill(x + 1, y + 1, x + width - 1, y + height - 1, -1517128);
        int cursorY = y + 7;
        if (width >= 176) {
            this.drawSectSeal(gfx, x + 10, cursorY + 1, 20, this.headerAccentColor());
            cursorY = this.drawWrappedLimited(gfx, this.bodyText(), x + 38, cursorY, width - 46, -12766422, 3);
        } else {
            cursorY = this.drawWrappedLimited(gfx, this.bodyText(), x + 8, cursorY, width - 16, -12766422, 3);
        }
        if (this.viewerEnemy) {
            noticeY = cursorY;
            cursorY += 5;
            noticeH = this.drawNoticeCard(gfx, x + 7, noticeY, width - 14, (Component)Component.translatable((String)"screen.friday_cultivation.sect.dialogue.hostile_notice"), -7723482);
            this.hoverTipRects.add(SectJoinDialogueScreen.tooltipRect(x + 7, noticeY, width - 14, noticeH, "hostile_notice"));
            cursorY += noticeH + 4;
        } else if (this.canJoin) {
            noticeY = cursorY;
            cursorY += 5;
            noticeH = this.drawNoticeCard(gfx, x + 7, noticeY, width - 14, (Component)Component.translatable((String)"screen.friday_cultivation.sect.dialogue.join_outcome"), -14722744);
            this.hoverTipRects.add(SectJoinDialogueScreen.tooltipRect(x + 7, noticeY, width - 14, noticeH, "join_outcome"));
            cursorY += noticeH + 4;
        }
        if (!this.taskLines.isEmpty() && cursorY + 22 < y + height) {
            int taskAreaBottom = y + height - 5;
            int taskAreaY = cursorY + 14;
            int taskAreaHeight = Math.max(0, taskAreaBottom - taskAreaY);
            int visibleCards = Math.min(this.taskLines.size(), Math.max(1, taskAreaHeight / 88));
            this.taskScroll = SectJoinDialogueScreen.clamp(this.taskScroll, 0, Math.max(0, this.taskLines.size() - visibleCards));
            MutableComponent heading = this.taskLines.size() > visibleCards ? Component.translatable((String)"screen.friday_cultivation.sect.dialogue.task_overview_count", (Object[])new Object[]{this.taskScroll + 1, this.taskScroll + visibleCards, this.taskLines.size()}) : Component.translatable((String)"screen.friday_cultivation.sect.dialogue.task_overview");
            gfx.drawString(this.font, (Component)heading.copy().withStyle(ChatFormatting.BOLD), x + 8, cursorY + 2, -7723482, false);
            if (this.taskLines.size() > visibleCards) {
                MutableComponent hint = Component.translatable((String)"screen.friday_cultivation.sect.dialogue.task_scroll_hint");
                gfx.drawString(this.font, this.trimToWidth((Component)hint, width - 132), x + width - 126, cursorY + 2, -7702689, false);
            }
            if (taskAreaHeight >= 82) {
                int cardW = width - 14 - (this.taskLines.size() > visibleCards ? 7 : 0);
                int drawnHeight = visibleCards * 88 - 4;
                this.lastTaskScrollMetrics = new ScrollMetrics(x + width - 9, taskAreaY, drawnHeight, this.taskLines.size(), visibleCards, x + 7, x + width - 7);
                gfx.enableScissor(x + 7, taskAreaY, x + width - 7, Math.min(taskAreaBottom, taskAreaY + drawnHeight));
                try {
                    for (int i = 0; i < visibleCards; ++i) {
                        this.drawTaskCard(gfx, this.taskLines.get(this.taskScroll + i), x + 7, taskAreaY + i * 88, cardW);
                    }
                }
                finally {
                    gfx.disableScissor();
                }
                this.drawScrollBar(gfx, this.lastTaskScrollMetrics, this.taskScroll);
            }
        }
    }

    private int drawNoticeCard(GuiGraphics gfx, int x, int y, int width, Component text, int accent) {
        List lines = this.font.split((FormattedText)text, width - 30);
        int lineCount = Math.min(2, Math.max(1, lines.size()));
        Objects.requireNonNull(this.font);
        int height = 8 + lineCount * (9 + 2);
        gfx.fill(x, y, x + width, y + height, 1429613600);
        gfx.fill(x + 1, y + 1, x + width - 1, y + height - 1, -661304);
        gfx.fill(x + 1, y + 1, x + 4, y + height - 1, accent);
        this.drawMiniGlyph(gfx, x + 8, y + 7, 10, accent, false);
        for (int i = 0; i < lineCount; ++i) {
            FormattedCharSequence formattedCharSequence = (FormattedCharSequence)lines.get(i);
            Objects.requireNonNull(this.font);
            gfx.drawString(this.font, formattedCharSequence, x + 24, y + 5 + i * (9 + 2), accent, false);
        }
        return height;
    }

    private void drawTaskCard(GuiGraphics gfx, TaskLine task, int x, int y, int width) {
        int statusColor = this.taskStatusColor(task);
        gfx.fill(x, y, x + width, y + 82, 1429613600);
        gfx.fill(x + 1, y + 1, x + width - 1, y + 82 - 1, -661304);
        gfx.fill(x + 1, y + 1, x + 4, y + 82 - 1, statusColor);
        int statusW = Math.min(74, Math.max(60, width / 4));
        int phaseW = Math.min(58, Math.max(44, width / 6));
        int statusX = x + width - statusW - 6;
        int phaseX = statusX - phaseW - 4;
        boolean showPhase = phaseX > x + 128;
        this.drawMiniGlyph(gfx, x + 8, y + 6, 8, statusColor, task.ready() && task.rewardAvailable());
        gfx.drawString(this.font, this.trimToWidth((Component)Component.translatable((String)task.titleKey()).withStyle(ChatFormatting.BOLD), showPhase ? phaseX - x - 28 : statusX - x - 28), x + 22, y + 5, -15067628, false);
        if (showPhase) {
            this.drawStatusPill(gfx, phaseX, y + 5, phaseW, this.taskPhase(task), this.taskPhaseColor(task));
            this.hoverTipRects.add(SectJoinDialogueScreen.sectTooltipRect(phaseX, y + 5, phaseW, 12, this.taskPhaseTooltipSuffix(task)));
        }
        this.drawStatusPill(gfx, statusX, y + 5, statusW, this.taskStatus(task), statusColor);
        MutableComponent requirement = task.requiredStack().isEmpty() ? Component.translatable((String)task.conditionKey()) : Component.translatable((String)"screen.friday_cultivation.sect.require_line", (Object[])new Object[]{task.requiredStack().getHoverName(), task.requiredStack().getCount()});
        MutableComponent progress = task.requiredCount() > 0 ? Component.translatable((String)"screen.friday_cultivation.sect.progress_line", (Object[])new Object[]{Math.min(task.heldRequired(), task.requiredCount()), task.requiredCount()}) : null;
        int infoY = y + 24;
        int infoH = 38;
        int infoX = x + 7;
        int infoW = width - 14;
        int infoGap = 6;
        int requiredW = Math.max(76, (infoW - infoGap) / 2);
        int rewardX = infoX + requiredW + infoGap;
        int rewardW = Math.max(76, infoW - requiredW - infoGap);
        this.drawTaskInfoBox(gfx, infoX, infoY, requiredW, infoH, -4818904, task.requiredStack(), (Component)requirement, (Component)progress, -12766422, task.ready() ? -14722744 : -9807288, task.ready());
        if (task.requiredCount() > 0) {
            this.drawProgressBar(gfx, infoX + 6, infoY + 29, Math.max(24, requiredW - 12), task.heldRequired(), task.requiredCount(), task.ready());
        }
        MutableComponent reward = task.rewardStack().isEmpty() ? Component.translatable((String)"screen.friday_cultivation.sect.reward_contribution_only", (Object[])new Object[]{task.contribution()}) : Component.translatable((String)"screen.friday_cultivation.sect.reward_line", (Object[])new Object[]{task.rewardStack().getHoverName(), task.contribution()});
        Component source = this.rewardSourceLabel(task.rewardKind());
        this.drawTaskInfoBox(gfx, rewardX, infoY, rewardW, infoH, SectJoinDialogueScreen.rewardSourceColor(task.rewardKind()), task.rewardStack(), (Component)reward, source, task.rewardAvailable() ? -7723482 : -7700107, SectJoinDialogueScreen.rewardSourceColor(task.rewardKind()), task.rewardAvailable());
        this.hoverTipRects.add(SectJoinDialogueScreen.sectTooltipRect(rewardX, infoY, rewardW, infoH, SectJoinDialogueScreen.rewardSourceTooltipSuffix(task.rewardKind())));
        gfx.drawString(this.font, this.trimToWidth(this.taskNextStep(task), width - 16), x + 8, y + 82 - 15, statusColor, false);
    }

    private void drawTaskInfoBox(GuiGraphics gfx, int x, int y, int width, int height, int accent, ItemStack stack, Component primary, Component secondary, int primaryColor, int secondaryColor, boolean highlighted) {
        if (width <= 0 || height <= 0) {
            return;
        }
        gfx.fill(x, y, x + width, y + height, 573975584);
        gfx.fill(x + 1, y + 1, x + width - 1, y + height - 1, highlighted ? -3640 : -528679);
        gfx.fill(x + 1, y + 1, x + 4, y + height - 1, accent);
        int textX = x + 27;
        if (!stack.isEmpty()) {
            gfx.renderItem(stack, x + 7, y + 7);
            gfx.renderItemDecorations(this.font, stack, x + 7, y + 7);
            this.itemHoverRects.add(new ItemHoverRect(x + 7, y + 7, 16, 16, stack.copy()));
        } else {
            this.drawMiniGlyph(gfx, x + 10, y + 10, 10, accent, highlighted);
        }
        gfx.drawString(this.font, this.trimToWidth(primary, Math.max(8, width - (textX - x) - 6)), textX, y + 6, primaryColor, false);
        if (secondary != null) {
            gfx.drawString(this.font, this.trimToWidth(secondary, Math.max(8, width - (textX - x) - 6)), textX, y + 19, secondaryColor, false);
        }
    }

    private void drawProgressBar(GuiGraphics gfx, int x, int y, int width, int held, int required, boolean ready) {
        int clampedRequired = Math.max(1, required);
        int clampedHeld = SectJoinDialogueScreen.clamp(held, 0, clampedRequired);
        int fillW = width * clampedHeld / clampedRequired;
        gfx.fill(x, y, x + width, y + 5, 1714826272);
        gfx.fill(x + 1, y + 1, x + width - 1, y + 4, -2043731);
        if (fillW > 0) {
            gfx.fill(x + 1, y + 1, x + Math.max(2, fillW), y + 4, ready ? -13664921 : -4703686);
        }
    }

    private void renderHoverTooltip(GuiGraphics gfx, int mouseX, int mouseY) {
        for (ItemHoverRect itemHoverRect : this.itemHoverRects) {
            if (!itemHoverRect.contains(mouseX, mouseY) || itemHoverRect.stack.isEmpty()) continue;
            gfx.renderTooltip(this.font, itemHoverRect.stack, mouseX, mouseY);
            return;
        }
        for (HoverTipRect hoverTipRect : this.hoverTipRects) {
            if (!hoverTipRect.contains(mouseX, mouseY)) continue;
            gfx.renderComponentTooltip(this.font, hoverTipRect.lines, mouseX, mouseY);
            return;
        }
    }

    private static HoverTipRect tooltipRect(int x, int y, int width, int height, String suffix) {
        return new HoverTipRect(x, y, width, height, List.of(Component.translatable((String)("screen.friday_cultivation.sect.dialogue.tooltip." + suffix))));
    }

    private static HoverTipRect sectTooltipRect(int x, int y, int width, int height, String suffix) {
        return new HoverTipRect(x, y, width, height, List.of(Component.translatable((String)("screen.friday_cultivation.sect.tooltip." + suffix))));
    }

    private Component bodyText() {
        if (this.viewerEnemy) {
            return Component.translatable((String)"screen.friday_cultivation.sect.dialogue.hostile_greeting", (Object[])new Object[]{Component.literal((String)this.npcName), Component.literal((String)this.sectName)});
        }
        if (this.canJoin) {
            return Component.translatable((String)"screen.friday_cultivation.sect.dialogue.master_greeting", (Object[])new Object[]{Component.literal((String)this.npcName), Component.literal((String)this.sectName)});
        }
        if (!this.taskActions.isEmpty()) {
            return Component.translatable((String)"screen.friday_cultivation.sect.dialogue.task_greeting", (Object[])new Object[]{Component.literal((String)this.npcName)});
        }
        if (this.viewerMember) {
            return Component.translatable((String)"screen.friday_cultivation.sect.dialogue.no_task_member", (Object[])new Object[]{Component.literal((String)this.npcName)});
        }
        return Component.translatable((String)"screen.friday_cultivation.sect.dialogue.no_task_guest", (Object[])new Object[]{Component.literal((String)this.npcName)});
    }

    private Component actionSummary() {
        String base = "screen.friday_cultivation.sect.dialogue.reply_summary.";
        if (this.viewerEnemy) {
            return Component.translatable((String)(base + "hostile"));
        }
        if (this.canJoin) {
            return Component.translatable((String)(base + "join"));
        }
        if (!this.taskActions.isEmpty()) {
            return Component.translatable((String)(base + "tasks"), (Object[])new Object[]{this.taskActions.size()});
        }
        return Component.translatable((String)(base + (this.viewerMember ? "no_actions_member" : "no_actions_guest")));
    }

    private int drawWrappedLimited(GuiGraphics gfx, Component text, int x, int y, int maxWidth, int color, int maxLines) {
        int lineCount = 0;
        for (FormattedCharSequence line : this.font.split((FormattedText)text, maxWidth)) {
            if (lineCount >= maxLines) break;
            gfx.drawString(this.font, line, x, y, color, false);
            Objects.requireNonNull(this.font);
            y += 9 + 3;
            ++lineCount;
        }
        return y;
    }

    private void drawDialogueButton(GuiGraphics gfx, int x, int y, int width, Component label, boolean hovered, boolean emphasis) {
        int border = hovered ? -7723482 : (emphasis ? -4703686 : -3562934);
        int fill = hovered ? -7770 : (emphasis ? -995445 : -1586805);
        gfx.fill(x, y, x + width, y + 20, border);
        gfx.fill(x + 1, y + 1, x + width - 1, y + 20 - 1, fill);
        gfx.fill(x + 1, y + 1, x + 4, y + 20 - 1, border);
        if (hovered) {
            gfx.fill(x + 4, y + 1, x + width - 1, y + 3, 0x55FFFFFF);
        }
        this.drawMiniGlyph(gfx, x + 7, y + 6, 8, border, emphasis);
        gfx.drawCenteredString(this.font, this.trimToWidth(label, width - 22), x + width / 2 + 6, y + 6, -15067628);
    }

    private void drawChip(GuiGraphics gfx, int x, int y, int width, Component label, int color) {
        if (width <= 8) {
            return;
        }
        gfx.fill(x, y, x + width, y + 12, color);
        gfx.fill(x + 1, y + 1, x + width - 1, y + 11, -528679);
        gfx.drawCenteredString(this.font, this.trimToWidth(label, width - 6), x + width / 2, y + 2, color);
    }

    private void drawStatusPill(GuiGraphics gfx, int x, int y, int width, Component label, int color) {
        gfx.fill(x, y, x + width, y + 12, color);
        gfx.fill(x + 1, y + 1, x + width - 1, y + 11, -528679);
        gfx.drawCenteredString(this.font, this.trimToWidth(label, width - 4), x + width / 2, y + 2, color);
    }

    private Component taskStatus(TaskLine task) {
        if (this.viewerEnemy) {
            return Component.translatable((String)"screen.friday_cultivation.sect.task.hostile_locked");
        }
        if (task.canTurnIn()) {
            return Component.translatable((String)"screen.friday_cultivation.sect.dialogue.task_status_turn_in");
        }
        if (task.canAccept()) {
            return Component.translatable((String)"screen.friday_cultivation.sect.dialogue.task_status_accept");
        }
        if (task.ready() && !task.rewardAvailable()) {
            return Component.translatable((String)"screen.friday_cultivation.sect.task.reward_unavailable");
        }
        if (task.ready()) {
            return Component.translatable((String)"screen.friday_cultivation.sect.task.ready_find_issuer");
        }
        if (task.accepted()) {
            return Component.translatable((String)"screen.friday_cultivation.sect.task.accepted");
        }
        return Component.translatable((String)"screen.friday_cultivation.sect.dialogue.task_status_locked");
    }

    private Component taskPhase(TaskLine task) {
        String base = "screen.friday_cultivation.sect.task.phase.";
        if (task.completed()) {
            return Component.translatable((String)(base + "completed"));
        }
        if (this.viewerEnemy) {
            return Component.translatable((String)(base + "hostile"));
        }
        if (!this.viewerMember) {
            return Component.translatable((String)(base + "join_first"));
        }
        if (!task.accepted()) {
            return Component.translatable((String)(base + "accept"));
        }
        if (task.ready()) {
            if (!task.rewardAvailable()) {
                return Component.translatable((String)(base + "blocked"));
            }
            return Component.translatable((String)(base + "report"));
        }
        return Component.translatable((String)(base + "collect"));
    }

    private String taskPhaseTooltipSuffix(TaskLine task) {
        if (task.completed()) {
            return "task_phase_completed";
        }
        if (this.viewerEnemy) {
            return "task_phase_hostile";
        }
        if (!this.viewerMember) {
            return "task_phase_join_first";
        }
        if (!task.accepted()) {
            return "task_phase_accept";
        }
        if (task.ready() && !task.rewardAvailable()) {
            return "task_phase_blocked";
        }
        if (task.ready()) {
            return "task_phase_report";
        }
        return "task_phase_collect";
    }

    private Component taskNextStep(TaskLine task) {
        String base = "screen.friday_cultivation.sect.task.next.";
        if (this.viewerEnemy) {
            return Component.translatable((String)(base + "hostile"));
        }
        if (task.completed()) {
            return Component.translatable((String)(base + "completed"));
        }
        if (!this.viewerMember) {
            return Component.translatable((String)(base + "join_first"));
        }
        MutableComponent issuer = Component.literal((String)task.issuerName());
        if (!task.accepted()) {
            if (task.canAccept()) {
                return Component.translatable((String)(base + "accept_here"), (Object[])new Object[]{issuer});
            }
            return Component.translatable((String)(base + "accept_issuer"), (Object[])new Object[]{issuer});
        }
        if (task.ready()) {
            if (!task.rewardAvailable()) {
                return Component.translatable((String)(base + "reward_unavailable"), (Object[])new Object[]{issuer});
            }
            if (task.canTurnIn()) {
                return Component.translatable((String)(base + "turn_in_here"), (Object[])new Object[]{issuer});
            }
            return Component.translatable((String)(base + "turn_in_issuer"), (Object[])new Object[]{issuer});
        }
        if (task.requiredCount() > 0) {
            return Component.translatable((String)(base + "collect"), (Object[])new Object[]{Math.min(task.heldRequired(), task.requiredCount()), task.requiredCount()});
        }
        return Component.translatable((String)(base + "accepted"));
    }

    private Component rewardSourceLabel(String rewardKind) {
        return Component.translatable((String)("screen.friday_cultivation.sect.reward_source." + SectJoinDialogueScreen.rewardSourceSuffix(rewardKind)));
    }

    private static int taskPriority(TaskLine task) {
        if (task.completed()) {
            return 7;
        }
        if (task.canTurnIn()) {
            return 0;
        }
        if (task.ready() && task.rewardAvailable()) {
            return 1;
        }
        if (task.canAccept()) {
            return 2;
        }
        if (!task.rewardAvailable()) {
            return 5;
        }
        if (task.accepted()) {
            return 3;
        }
        if (task.requiresIssuer()) {
            return 4;
        }
        return 6;
    }

    private static String rewardSourceTooltipSuffix(String rewardKind) {
        return "reward_source_" + SectJoinDialogueScreen.rewardSourceSuffix(rewardKind);
    }

    private static String rewardSourceSuffix(String rewardKind) {
        if ("ITEM".equals(rewardKind)) {
            return "item";
        }
        if ("TECHNIQUE".equals(rewardKind)) {
            return "technique";
        }
        if ("SPELL".equals(rewardKind)) {
            return "spell";
        }
        return "contribution_only";
    }

    private static int rewardSourceColor(String rewardKind) {
        if ("ITEM".equals(rewardKind)) {
            return -4818904;
        }
        if ("TECHNIQUE".equals(rewardKind)) {
            return -14722744;
        }
        if ("SPELL".equals(rewardKind)) {
            return -7723482;
        }
        return -9807288;
    }

    private int taskStatusColor(TaskLine task) {
        if (this.viewerEnemy) {
            return -7723482;
        }
        if (task.canTurnIn()) {
            return -14722744;
        }
        if (task.canAccept()) {
            return -7723482;
        }
        if (task.ready()) {
            return task.rewardAvailable() ? -14722744 : -4818904;
        }
        if (task.accepted()) {
            return -4703686;
        }
        return -7700107;
    }

    private int taskPhaseColor(TaskLine task) {
        if (task.completed()) {
            return -7700107;
        }
        if (this.viewerEnemy) {
            return -7723482;
        }
        if (!this.viewerMember) {
            return -9807288;
        }
        if (!task.accepted()) {
            return -3562934;
        }
        if (task.ready() && task.rewardAvailable()) {
            return -14722744;
        }
        if (task.ready()) {
            return -4818904;
        }
        return -4703686;
    }

    private int headerAccentColor() {
        if (this.viewerEnemy) {
            return -7723482;
        }
        return this.targetRole == SectRole.NONE ? -3562934 : SectJoinDialogueScreen.roleColor(this.targetRole);
    }

    private void drawScrollBar(GuiGraphics gfx, ScrollMetrics metrics, int offset) {
        if (metrics.maxScroll() <= 0) {
            return;
        }
        gfx.fill(metrics.x, metrics.y, metrics.x + 3, metrics.y + metrics.height, -9807288);
        int thumbH = SectJoinDialogueScreen.scrollThumbHeight(metrics);
        int thumbY = SectJoinDialogueScreen.scrollThumbY(metrics, offset);
        gfx.fill(metrics.x, thumbY, metrics.x + 3, thumbY + thumbH, -3562934);
    }

    private static int roleColor(SectRole role) {
        return switch (role) {
            case ANCESTOR -> -9491833;
            case MASTER -> -7723482;
            case ELDER -> -6660586;
            case INNER_DISCIPLE -> -13664921;
            case OUTER_DISCIPLE -> -13275229;
            case GUARD_DISCIPLE -> -9678941;
            case SERVANT -> -9807288;
            default -> -7700107;
        };
    }

    private Component trimToWidth(Component label, int maxWidth) {
        if (this.font.width((FormattedText)label) <= maxWidth) {
            return label;
        }
        String ellipsis = "...";
        return Component.literal((String)(this.font.plainSubstrByWidth(label.getString(), Math.max(0, maxWidth - this.font.width(ellipsis))) + ellipsis));
    }

    private Layout layout(int optionCount) {
        int panelW = Math.min(660, Math.max(320, this.width - 34));
        boolean side = (panelW = Math.min(panelW, Math.max(220, this.width - 12))) >= 500;
        int visibleTasks = Math.min(this.taskLines.size(), side ? 3 : 2);
        int panelH = side ? Math.max(216, Math.max(112 + visibleTasks * 88, 94 + optionCount * 23)) : Math.max(208, 136 + visibleTasks * 88 + optionCount * 23);
        panelH = Math.min(panelH, Math.max(118, this.height - 24));
        int left = (this.width - panelW) / 2;
        int top = Math.max(12, this.height - panelH - 28);
        return new Layout(left, top, panelW, panelH);
    }

    private int buttonTop(Layout layout, int optionCount) {
        return layout.top + layout.height - 10 - optionCount * 23;
    }

    private int buttonWidth(Layout layout) {
        return this.buttonsBeside(layout) ? Math.min(192, layout.width - 24) : layout.width - 24;
    }

    private int buttonX(Layout layout) {
        int buttonW = this.buttonWidth(layout);
        return this.buttonsBeside(layout) ? layout.left + layout.width - buttonW - 12 : layout.left + 12;
    }

    private boolean buttonsBeside(Layout layout) {
        return layout.width >= 500;
    }

    public boolean calculateIngredientsPositions(double mouseX, double mouseY, int button) {
        if (button == 0) {
            if (SectJoinDialogueScreen.isInsideScrollbar(mouseX, mouseY, this.lastTaskScrollMetrics)) {
                this.draggingTaskScroll = true;
                this.taskScrollGrabOffset = SectJoinDialogueScreen.scrollGrabOffset(mouseY, this.lastTaskScrollMetrics, this.taskScroll);
                this.taskScroll = SectJoinDialogueScreen.scrollFromMouseY(mouseY, this.lastTaskScrollMetrics, this.taskScrollGrabOffset);
                return true;
            }
            List<DialogueOption> options = this.dialogueOptions();
            for (OptionClickRect rect : this.optionClickRects) {
                if (!rect.contains(mouseX, mouseY) || rect.index() < 0 || rect.index() >= options.size()) continue;
                options.get(rect.index()).action().run();
                return true;
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (button == 0 && this.draggingTaskScroll) {
            this.taskScroll = SectJoinDialogueScreen.scrollFromMouseY(mouseY, this.lastTaskScrollMetrics, this.taskScrollGrabOffset);
            return true;
        }
        return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }

    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (button == 0 && this.draggingTaskScroll) {
            this.draggingTaskScroll = false;
            this.taskScrollGrabOffset = 0.0;
            return true;
        }
        return super.mouseReleased(mouseX, mouseY, button);
    }

    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        if (SectJoinDialogueScreen.isInsideScrollContent(mouseX, mouseY, this.lastTaskScrollMetrics)) {
            this.taskScroll = SectJoinDialogueScreen.clamp(this.taskScroll - (int)Math.signum(delta), 0, this.lastTaskScrollMetrics.maxScroll());
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, delta);
    }

    private static int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }

    private static boolean isInsideScrollContent(double mouseX, double mouseY, ScrollMetrics metrics) {
        return metrics.maxScroll() > 0 && mouseX >= (double)metrics.contentLeft && mouseX < (double)metrics.contentRight && mouseY >= (double)metrics.y && mouseY < (double)(metrics.y + metrics.height);
    }

    private static boolean isInsideScrollbar(double mouseX, double mouseY, ScrollMetrics metrics) {
        return metrics.maxScroll() > 0 && mouseX >= (double)(metrics.x - 3) && mouseX < (double)(metrics.x + 3 + 3) && mouseY >= (double)metrics.y && mouseY < (double)(metrics.y + metrics.height);
    }

    private static double scrollGrabOffset(double mouseY, ScrollMetrics metrics, int offset) {
        int thumbY = SectJoinDialogueScreen.scrollThumbY(metrics, offset);
        int thumbH = SectJoinDialogueScreen.scrollThumbHeight(metrics);
        if (mouseY >= (double)thumbY && mouseY < (double)(thumbY + thumbH)) {
            return mouseY - (double)thumbY;
        }
        return (double)thumbH / 2.0;
    }

    private static int scrollFromMouseY(double mouseY, ScrollMetrics metrics, double grabOffset) {
        int maxScroll = metrics.maxScroll();
        if (maxScroll <= 0) {
            return 0;
        }
        int thumbH = SectJoinDialogueScreen.scrollThumbHeight(metrics);
        int trackTravel = Math.max(1, metrics.height - thumbH);
        double progress = (mouseY - (double)metrics.y - grabOffset) / (double)trackTravel;
        return SectJoinDialogueScreen.clamp((int)Math.round(progress * (double)maxScroll), 0, maxScroll);
    }

    private static int scrollThumbHeight(ScrollMetrics metrics) {
        if (metrics.total <= 0) {
            return metrics.height;
        }
        return Math.min(metrics.height, Math.max(12, metrics.height * metrics.visible / metrics.total));
    }

    private static int scrollThumbY(ScrollMetrics metrics, int offset) {
        int maxScroll = metrics.maxScroll();
        if (maxScroll <= 0) {
            return metrics.y;
        }
        int thumbH = SectJoinDialogueScreen.scrollThumbHeight(metrics);
        return metrics.y + (metrics.height - thumbH) * SectJoinDialogueScreen.clamp(offset, 0, maxScroll) / maxScroll;
    }

    private static String safe(String value) {
        return value == null ? "" : value;
    }

    public boolean isPauseScreen() {
        return false;
    }

    private record ScrollMetrics(int x, int y, int height, int total, int visible, int contentLeft, int contentRight) {
        int maxScroll() {
            return Math.max(0, this.total - this.visible);
        }
    }

    private record TaskLine(String id, String issuerName, SectRole issuerRole, String titleKey, String conditionKey, int contribution, ItemStack requiredStack, int heldRequired, int requiredCount, ItemStack rewardStack, String rewardKind, boolean accepted, boolean completed, boolean ready, boolean rewardAvailable, boolean requiresIssuer, boolean canAccept, boolean canTurnIn) {
    }

    private record TaskAction(String id, String titleKey, boolean turnIn) {
    }

    private record DialogueOption(Component label, Runnable action, boolean emphasis, String tooltipSuffix) {
    }

    private record OptionClickRect(int x, int y, int width, int height, int index) {
        private boolean contains(double mouseX, double mouseY) {
            return mouseX >= (double)this.x && mouseX < (double)(this.x + this.width) && mouseY >= (double)this.y && mouseY < (double)(this.y + this.height);
        }
    }

    private record HoverTipRect(int x, int y, int width, int height, List<Component> lines) {
        private boolean contains(double mouseX, double mouseY) {
            return mouseX >= (double)this.x && mouseX < (double)(this.x + this.width) && mouseY >= (double)this.y && mouseY < (double)(this.y + this.height);
        }
    }

    private record Layout(int left, int top, int width, int height) {
    }

    private record ItemHoverRect(int x, int y, int width, int height, ItemStack stack) {
        private boolean contains(double mouseX, double mouseY) {
            return mouseX >= (double)this.x && mouseX < (double)(this.x + this.width) && mouseY >= (double)this.y && mouseY < (double)(this.y + this.height);
        }
    }
}
