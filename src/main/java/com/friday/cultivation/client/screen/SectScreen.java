/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.math.Axis
 *  net.minecraft.ChatFormatting
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.gui.GuiGraphics
 *  net.minecraft.client.gui.screens.Screen
 *  net.minecraft.nbt.CompoundTag
 *  net.minecraft.nbt.ListTag
 *  net.minecraft.network.chat.Component
 *  net.minecraft.network.chat.FormattedText
 *  net.minecraft.network.chat.MutableComponent
 *  net.minecraft.util.FormattedCharSequence
 *  net.minecraft.world.item.ItemStack
 */
package com.friday.cultivation.client.screen;

import com.mojang.math.Axis;
import com.friday.cultivation.cultivation.sect.SectRole;
import com.friday.cultivation.network.ModNetwork;
import com.friday.cultivation.network.RequestSectJoinDialoguePacket;
import com.friday.cultivation.network.SectTaskActionPacket;
import com.friday.cultivation.network.SetSectFriendlyFirePacket;
import com.friday.cultivation.network.TrackSectTaskIssuerPacket;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Predicate;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.item.ItemStack;

public class SectScreen
extends Screen {
    private static final String P = "screen.friday_cultivation.sect.";
    private static final int DESIGN_W = 640;
    private static final int DESIGN_H = 344;
    private static final int SPINE_W = 40;
    private static final int SAFE_MARGIN = 10;
    private static final int MARGIN = 22;
    private static final int MID_GAP = 18;
    private static final int PAGE_W = 289;
    private static final int RIGHT_X = 329;
    private static final int TAB_Y = 46;
    private static final int CONTENT_TOP = 70;
    private static final int CONTENT_BOTTOM = 316;
    private static final int ACTION_ROW_Y = 322;
    private static final int MEMBER_ROW = 18;
    private static final int TASK_ROW = 46;
    private static final int TREE_NODE_H = 15;
    private static final int TREE_SCROLL_STEP = 12;
    private static final int SCROLL_BAR_W = 3;
    private static final int SCROLL_HIT_PAD = 3;
    private static final int INK_BLACK = -15067628;
    private static final int PAGE_INK = -14081252;
    private static final int INK_SOFT = -11911632;
    private static final int INK_MUTE = -9807288;
    private static final int INK_PALE = -7702689;
    private static final int VERMILLION = -4703686;
    private static final int VERMILLION_DEEP = -7723482;
    private static final int GOLD_BORDER = -3562934;
    private static final int GOLD_BRIGHT = -10496;
    private static final int BORDER_LIGHT = -2504802;
    private static final int BORDER_DARK = -10859978;
    private static final int BLUE_SPINE = -13676952;
    private static final int BLUE_SPINE_DARK = -14665650;
    private static final int BRUSH_DARK = -12365222;
    private static final int JADE = -13664921;
    private static final int JADE_DARK = -14722744;
    private static final int AMBER = -4818904;
    private static final int DISABLED = -7700107;
    private static final int DIAMOND_RED = -6534610;
    private static final int RULE_LINE = -3886189;
    private static final int OLIVE_GOLD = -7705298;
    private static final int BROWN_ORANGE = -4885446;
    private static final int STAT_RED = -5750484;
    private static final int STAT_JADE = -13668780;
    private static final int STAT_TEAL = -13668470;
    private static final int PARCH_TOP = -790821;
    private static final int PARCH_BACK = -1186608;
    private static final int PARCH_CARD = -527903;
    private static final int PARCH_CARD_DEEP = -1055026;
    private static final int CARD = -528679;
    private static final int OFFER_HOVER = -1056582;
    private static final int OFFER_SELECTED = -6528;
    private static final int TREE_LINE = -1435870648;
    private final CompoundTag snapshot;
    private final List<MemberRow> members = new ArrayList<MemberRow>();
    private final List<TaskRow> tasks = new ArrayList<TaskRow>();
    private final List<MemberClickRect> memberClickRects = new ArrayList<MemberClickRect>();
    private final List<TaskSelectRect> taskSelectRects = new ArrayList<TaskSelectRect>();
    private final List<TabClickRect> tabClickRects = new ArrayList<TabClickRect>();
    private final List<HoverTipRect> contentHoverTips = new ArrayList<HoverTipRect>();
    private final List<ItemHoverRect> itemHoverRects = new ArrayList<ItemHoverRect>();
    private Tab tab = Tab.INFO;
    private int memberScroll;
    private int taskScroll;
    private int treeScroll;
    private boolean draggingMemberScroll;
    private boolean draggingTaskScroll;
    private boolean draggingTreeScroll;
    private double memberScrollGrab;
    private double taskScrollGrab;
    private double treeScrollGrab;
    private boolean sameSectImmunity;
    private int targetEntityId = -1;
    private boolean canJoin;
    private boolean viewerMember;
    private boolean viewerEnemy;
    private SectRole viewerRole = SectRole.NONE;
    private int viewerContribution;
    private String selectedMemberId;
    private String selectedMemberName;
    private String selectedTaskId;
    private int[] closeButtonRect = new int[4];
    private int[] actionPrimaryRect = new int[4];
    private int[] actionSecondaryRect = new int[4];
    private ActionKind actionPrimaryKind = ActionKind.NONE;
    private String actionPrimaryTaskId;
    private SectViewport lastViewport = new SectViewport(0, 0, 1.0f);

    public SectScreen(CompoundTag snapshot) {
        super((Component)Component.translatable((String)"screen.friday_cultivation.sect.title"));
        this.snapshot = snapshot == null ? new CompoundTag() : snapshot.copy();
        this.readSnapshot();
    }

    private static Component tr(String suffix, Object ... args) {
        return Component.translatable((String)(P + suffix), (Object[])args);
    }

    private void readSnapshot() {
        this.sameSectImmunity = this.snapshot.getBoolean("sameSectImmunity");
        this.targetEntityId = this.snapshot.contains("targetEntityId", 3) ? this.snapshot.getInt("targetEntityId") : -1;
        this.canJoin = this.snapshot.getBoolean("canJoin");
        this.viewerMember = this.snapshot.getBoolean("viewerMember");
        this.viewerEnemy = this.snapshot.getBoolean("viewerEnemy");
        this.viewerRole = SectRole.byId(this.snapshot.getString("viewerRole"));
        this.viewerContribution = this.snapshot.getInt("viewerContribution");
        ListTag memberList = this.snapshot.getList("members", 10);
        for (int i = 0; i < memberList.size(); ++i) {
            CompoundTag row = memberList.getCompound(i);
            String id = row.contains("uuid") ? row.getUUID("uuid").toString() : row.getString("role") + ":" + row.getString("name");
            this.members.add(new MemberRow(id, row.getString("name"), SectRole.byId(row.getString("role")), row.getBoolean("player"), row.getInt("contribution")));
        }
        this.members.sort(Comparator.comparingInt((MemberRow r) -> r.role.rank()).thenComparing(r -> r.name));
        ListTag taskList = this.snapshot.getList("tasks", 10);
        for (int i = 0; i < taskList.size(); ++i) {
            CompoundTag row = taskList.getCompound(i);
            this.tasks.add(new TaskRow(row.getString("id"), row.getString("issuerName"), SectRole.byId(row.getString("issuerRole")), row.getString("titleKey"), row.getString("conditionKey"), row.getInt("contribution"), row.contains("requiredStack", 10) ? ItemStack.of((CompoundTag)row.getCompound("requiredStack")) : ItemStack.EMPTY, row.contains("heldRequired", 3) ? row.getInt("heldRequired") : 0, row.contains("requiredCount", 3) ? row.getInt("requiredCount") : 0, row.contains("rewardStack", 10) ? ItemStack.of((CompoundTag)row.getCompound("rewardStack")) : ItemStack.EMPTY, row.getString("rewardKind"), row.getBoolean("accepted"), row.getBoolean("completed"), row.getBoolean("ready"), !row.contains("rewardAvailable", 1) || row.getBoolean("rewardAvailable"), row.getBoolean("requiresIssuer"), row.getBoolean("canAccept"), row.getBoolean("canTurnIn")));
        }
        this.tasks.sort(Comparator.comparingInt(SectScreen::taskPriority).thenComparing(t -> t.issuerRole.rank()).thenComparing(t -> t.issuerName).thenComparing(t -> t.titleKey).thenComparing(t -> t.id));
    }

    private void toggleImmunity() {
        ModNetwork.CHANNEL.sendToServer((Object)new SetSectFriendlyFirePacket(!this.sameSectImmunity, this.targetEntityId));
        this.sameSectImmunity = !this.sameSectImmunity;
    }

    public boolean isPauseScreen() {
        return false;
    }

    private SectViewport sectViewport() {
        int availableW = Math.max(120, this.width - 20);
        int availableH = Math.max(120, this.height - 20);
        float scale = Math.min(1.0f, Math.min((float)availableW / 680.0f, (float)availableH / 344.0f));
        if (this.width <= 700 || this.height <= 360) {
            scale = Math.min(scale, 0.78f);
        }
        scale = Math.max(0.5f, scale);
        int drawnW = Math.round(640.0f * scale);
        int totalW = Math.round(680.0f * scale);
        int left = Math.round((float)(this.width - totalW) / 2.0f + 40.0f * scale);
        left = Math.max(Math.round(40.0f * scale) + 3, left);
        int top = Math.round(((float)this.height - 344.0f * scale) / 2.0f);
        top = Math.max(10, Math.min(top, this.height - Math.round(344.0f * scale) - 4));
        if (left + drawnW > this.width - 4) {
            left = Math.max(Math.round(40.0f * scale) + 3, this.width - drawnW - 4);
        }
        return new SectViewport(left, top, scale);
    }

    public void render(GuiGraphics gfx, int mouseX, int mouseY, float partial) {
        SectViewport vp;
        this.lastViewport = vp = this.sectViewport();
        int lmx = vp.toLocalX(mouseX);
        int lmy = vp.toLocalY(mouseY);
        this.clearFrameRects();
        gfx.pose().pushPose();
        gfx.pose().translate((float)vp.left, (float)vp.top, 0.0f);
        gfx.pose().scale(vp.scale, vp.scale, 1.0f);
        this.renderSectPage(gfx, lmx, lmy);
        gfx.pose().popPose();
        this.renderHoverTooltip(gfx, lmx, lmy, mouseX, mouseY);
    }

    private void clearFrameRects() {
        this.memberClickRects.clear();
        this.taskSelectRects.clear();
        this.tabClickRects.clear();
        this.contentHoverTips.clear();
        this.itemHoverRects.clear();
        this.closeButtonRect = new int[4];
        this.actionPrimaryRect = new int[4];
        this.actionSecondaryRect = new int[4];
        this.actionPrimaryKind = ActionKind.NONE;
        this.actionPrimaryTaskId = null;
    }

    private void renderSectPage(GuiGraphics gfx, int mx, int my) {
        boolean hasSect = this.snapshot.getBoolean("hasSect");
        this.renderBookSpine(gfx, hasSect);
        this.drawWidePaperFrame(gfx, 0, 0, 640, 344);
        this.ribbonBookmark(gfx);
        Component titleLine = hasSect ? Component.literal((String)this.snapshot.getString("sectName")) : SectScreen.tr("none", new Object[0]);
        this.drawBrushTitle(gfx, 320, 8, titleLine);
        this.closeButtonRect = this.drawCloseButton(gfx, 598, 12, mx, my);
        if (!hasSect) {
            this.drawNoSectState(gfx);
            this.bottomActionRow(gfx, mx, my, false);
            return;
        }
        Component sub = this.headerSubtitle();
        this.drawScaledCentered(gfx, sub, 320, 33, 0.8f, this.viewerEnemy ? -7723482 : -9807288, 500);
        this.renderTopTabs(gfx, 320, 46, mx, my);
        int top = 70;
        int h = 316 - top;
        switch (this.tab) {
            case INFO: {
                this.renderInfoTab(gfx, top, h, mx, my);
                break;
            }
            case MEMBERS: {
                this.renderMembersTab(gfx, top, h, mx, my);
                break;
            }
            case TREE: {
                this.renderRelationsTab(gfx, top, h, mx, my);
                break;
            }
            case TASKS: {
                this.renderTasksTab(gfx, top, h, mx, my);
            }
        }
        this.bottomActionRow(gfx, mx, my, true);
    }

    private void renderInfoTab(GuiGraphics gfx, int y, int h, int mx, int my) {
        int lx = 22;
        int sealCx = lx + 26;
        int sealCy = y + 26;
        this.sealCircle(gfx, sealCx, sealCy, 24, this.headerAccentColor(), (Component)Component.literal((String)SectScreen.firstGlyph(this.snapshot.getString("sectName"))));
        int nx = lx + 58;
        this.drawScaledString(gfx, (Component)Component.literal((String)this.snapshot.getString("sectName")).withStyle(ChatFormatting.BOLD), nx, y + 8, 1.35f, -14081252, 229);
        Component roleLine = this.viewerEnemy ? SectScreen.tr("viewer_hostile", new Object[0]) : (this.viewerMember ? this.viewerRole.displayName() : SectScreen.tr("summary.guest", new Object[0]));
        this.drawTagPill(gfx, nx, y + 30, roleLine, this.viewerEnemy ? -7723482 : -7705298);
        int cy = y + 56;
        this.drawDiamondHeader(gfx, lx, cy, 289, SectScreen.tr("overview", new Object[0]));
        this.drawStatGridRow(gfx, lx, cy += 16, 289, SectScreen.tr("summary.members", new Object[0]), (Component)Component.literal((String)String.valueOf(this.members.size())), -13668780, SectScreen.tr("summary.tasks", new Object[0]), (Component)Component.literal((String)String.valueOf(this.activeTaskCount())), -13668470);
        this.drawStatGridRow(gfx, lx, cy += 14, 289, SectScreen.tr("summary.contribution", new Object[0]), (Component)Component.literal((String)String.valueOf(this.viewerMember ? this.viewerContribution : 0)), this.viewerMember ? -4885446 : -7700107, SectScreen.tr("book.ready", new Object[0]), (Component)Component.literal((String)String.valueOf(this.readyTaskCount())), this.readyTaskCount() > 0 ? -5750484 : -9807288);
        this.drawStatGridRow(gfx, lx, cy += 14, 289, SectScreen.tr("immunity", new Object[0]), SectScreen.tr(this.sameSectImmunity ? "book.on" : "book.off", new Object[0]), this.sameSectImmunity ? -13668780 : -5750484, SectScreen.tr("book.relation", new Object[0]), this.relationStatus(), this.relationColor());
        MemberRow master = this.firstByRole(SectRole.MASTER);
        this.drawDiamondHeader(gfx, lx, cy += 20, 289, SectRole.MASTER.displayName());
        cy += 16;
        if (master != null) {
            gfx.fill(lx, cy, lx + 289, cy + 26, 0x22000000);
            gfx.fill(lx, cy, lx + 3, cy + 26, SectScreen.roleColor(SectRole.MASTER));
            this.drawRoleBadge(gfx, lx + 8, cy + 8, 10, SectRole.MASTER, false);
            this.drawScaledString(gfx, (Component)Component.literal((String)master.name).withStyle(ChatFormatting.BOLD), lx + 24, cy + 4, 0.95f, -14081252, 193);
            this.drawScaledRight(gfx, SectScreen.tr("member_meta_contribution", master.contribution), lx + 289 - 8, cy + 5, 0.75f, -7723482);
            this.drawScaledString(gfx, SectScreen.tr("book.leader_sub", new Object[0]), lx + 24, cy + 16, 0.74f, -9807288, 259);
        } else {
            this.drawScaledString(gfx, SectScreen.tr("book.no_leader", new Object[0]), lx + 6, cy + 4, 0.78f, -9807288, 277);
        }
        this.drawDiamondHeader(gfx, lx, cy += 34, 289, SectScreen.tr("book.notice", new Object[0]));
        this.drawWrappedLimited(gfx, this.infoNotice(), lx, cy += 15, 289, 0.78f, -11911632, 3);
        int rx = 329;
        int cy2 = y;
        this.drawDiamondHeader(gfx, rx, cy2, 289, SectScreen.tr("book.role_dist", new Object[0]));
        cy2 += 16;
        cy2 = this.renderRoleDistribution(gfx, rx, cy2, 289, mx, my);
        this.drawPaperDivider(gfx, rx + 6, cy2 += 6, 277);
        this.drawDiamondHeader(gfx, rx, cy2 += 10, 289, SectScreen.tr("book.recent_tasks", new Object[0]));
        this.renderRecentTasks(gfx, rx, cy2 += 15, 289, 316 - cy2);
    }

    private int renderRoleDistribution(GuiGraphics gfx, int x, int y, int w, int mx, int my) {
        SectRole[] roles = SectScreen.displayRoles();
        int[] counts = new int[roles.length];
        int total = 0;
        for (int i = 0; i < roles.length; ++i) {
            for (MemberRow row : this.members) {
                if (row.role != roles[i]) continue;
                int n = i;
                counts[n] = counts[n] + 1;
            }
            total += counts[i];
        }
        int barH = 12;
        int segX = x;
        for (int i = 0; i < roles.length; ++i) {
            int segW = total <= 0 ? 0 : (i == roles.length - 1 ? x + w - segX : Math.max(counts[i] > 0 ? 2 : 0, w * counts[i] / Math.max(1, total)));
            if (segW <= 0) continue;
            gfx.fill(segX, y, segX + segW, y + barH, SectScreen.roleColor(roles[i]));
            gfx.fill(segX, y, segX + segW, y + 2, 0x44FFFFFF);
            segX += segW;
        }
        gfx.fill(x, y - 1, x + w, y, 0x33000000);
        int cy = y + barH + 10;
        int colW = w / 2;
        for (int i = 0; i < roles.length; ++i) {
            int col = i % 2;
            int rowI = i / 2;
            int ex = x + col * colW;
            int ey = cy + rowI * 18;
            boolean zero = counts[i] == 0;
            gfx.fill(ex, ey + 1, ex + 8, ey + 9, zero ? -7702689 : SectScreen.roleColor(roles[i]));
            this.drawScaledString(gfx, roles[i].displayName(), ex + 12, ey, 0.78f, zero ? -7702689 : -14081252, colW - 40);
            this.drawScaledRight(gfx, (Component)Component.literal((String)("\u00d7" + counts[i])), ex + colW - 14, ey, 0.76f, zero ? -7702689 : -9807288);
        }
        return cy + (roles.length + 1) / 2 * 18;
    }

    private void renderRecentTasks(GuiGraphics gfx, int x, int y, int w, int h) {
        if (this.tasks.isEmpty()) {
            this.drawScaledString(gfx, SectScreen.tr("no_tasks", new Object[0]), x + 4, y + 4, 0.78f, -9807288, w - 8);
            return;
        }
        int rowH = 20;
        int max = Math.max(1, h / rowH);
        for (int i = 0; i < Math.min(max, this.tasks.size()); ++i) {
            TaskRow task = this.tasks.get(i);
            int ry = y + i * rowH;
            gfx.fill(x, ry, x + w, ry + 16, 0x18000000);
            this.drawScaledString(gfx, (Component)Component.translatable((String)task.titleKey), x + 8, ry + 4, 0.78f, -11911632, w - 60);
            this.drawStatusPill(gfx, x + w - 48, ry + 3, 44, this.taskPhase(task), this.taskPhaseColor(task));
        }
    }

    private void renderMembersTab(GuiGraphics gfx, int y, int h, int mx, int my) {
        int lx = 22;
        int listW = 240;
        int dx = lx + listW + 16;
        int dw = 618 - dx;
        this.drawCard(gfx, lx, y, listW, h);
        this.drawDiamondHeaderTag(gfx, lx + 6, y + 4, listW - 12, SectScreen.tr("members", new Object[0]), SectScreen.tr("book.roster_count", this.members.size()), false);
        this.renderMemberList(gfx, lx + 4, y + 22, listW - 8, h - 26, mx, my);
        this.drawCard(gfx, dx, y, dw, h);
        this.renderMemberDetail(gfx, dx, y, dw, h);
    }

    private void renderMemberList(GuiGraphics gfx, int x, int y, int w, int h, int mx, int my) {
        int rowH = 18;
        int visible = Math.max(1, h / rowH);
        int maxScroll = Math.max(0, this.members.size() - visible);
        this.memberScroll = SectScreen.clamp(this.memberScroll, 0, maxScroll);
        int end = Math.min(this.members.size(), this.memberScroll + visible);
        for (int i = this.memberScroll; i < end; ++i) {
            boolean hover;
            MemberRow row = this.members.get(i);
            int ry = y + (i - this.memberScroll) * rowH;
            int rowW = w - 8;
            boolean selected = row.selected(this.selectedMemberId);
            boolean bl = hover = mx >= x && mx < x + rowW && my >= ry && my < ry + rowH - 2;
            if (selected) {
                gfx.fill(x, ry, x + rowW, ry + rowH - 2, -6528);
            } else if (hover) {
                gfx.fill(x, ry, x + rowW, ry + rowH - 2, -1056582);
            } else if ((i & 1) == 0) {
                gfx.fill(x, ry, x + rowW, ry + rowH - 2, 0x14000000);
            }
            gfx.fill(x, ry, x + 3, ry + rowH - 2, SectScreen.roleColor(row.role));
            this.drawRoleBadge(gfx, x + 6, ry + 4, 10, row.role, selected);
            this.drawScaledString(gfx, (Component)Component.literal((String)row.name).withStyle(ChatFormatting.BOLD), x + 22, ry + 4, 0.82f, row.player ? -15067628 : -11911632, 116);
            this.drawScaledRight(gfx, SectScreen.tr("member_meta_contribution", row.contribution), x + rowW - 16, ry + 5, 0.74f, -9807288);
            int mkx = x + rowW - 11;
            int mky = ry + rowH / 2 - 1;
            if (row.player) {
                this.drawDiamond(gfx, mkx, mky, 3, -6534610);
            } else {
                this.drawDiamondOutline(gfx, mkx, mky, 3, -7702689);
            }
            this.memberClickRects.add(new MemberClickRect(x, ry, rowW, rowH - 2, row));
            this.contentHoverTips.add(new HoverTipRect(x, ry, rowW, rowH - 2, List.of(Component.literal((String)row.name).withStyle(ChatFormatting.BOLD), SectScreen.tr("member_meta", row.role.displayName(), row.contribution), SectScreen.tr(row.player ? "member_kind.player" : "member_kind.npc", new Object[0]))));
        }
        this.drawScrollBar(gfx, x + w - 4, y, h, this.members.size(), visible, this.memberScroll);
    }

    private void renderMemberDetail(GuiGraphics gfx, int x, int y, int w, int h) {
        MemberRow row = this.selectedMember();
        if (row == null) {
            this.drawScaledCentered(gfx, SectScreen.tr("member_detail.empty", new Object[0]), x + w / 2, y + h / 2 - 6, 0.82f, -9807288, w - 20);
            return;
        }
        int sealCx = x + 34;
        int sealCy = y + 30;
        this.sealCircle(gfx, sealCx, sealCy, 22, SectScreen.roleColor(row.role), (Component)Component.literal((String)SectScreen.firstGlyph(row.name)));
        this.drawRoleBadge(gfx, sealCx - 5, sealCy + 6, 10, row.role, false);
        this.drawScaledString(gfx, (Component)Component.literal((String)row.name).withStyle(ChatFormatting.BOLD), x + 64, y + 12, 1.3f, -14081252, w - 70);
        this.drawTagPill(gfx, x + 64, y + 32, row.role.displayName(), -7705298);
        int cy = y + 60;
        this.drawDiamondHeader(gfx, x + 10, cy, w - 20, SectScreen.tr("book.member_data", new Object[0]));
        this.drawStatGridRow(gfx, x + 10, cy += 16, w - 20, SectScreen.tr("summary.contribution", new Object[0]), (Component)Component.literal((String)String.valueOf(row.contribution)), -4885446, SectScreen.tr("book.identity", new Object[0]), SectScreen.tr(row.player ? "book.real" : "book.npc", new Object[0]), row.player ? -13668780 : -11911632);
        this.drawStatGridRow(gfx, x + 10, cy += 14, w - 20, SectScreen.tr("book.role_label", new Object[0]), row.role.displayName(), -11911632, SectScreen.tr("book.rank", new Object[0]), (Component)Component.literal((String)("#" + this.memberRank(row))), -11911632);
        this.drawDiamondHeader(gfx, x + 10, cy += 20, w - 20, SectScreen.tr("book.role_duty", new Object[0]));
        this.drawWrappedLimited(gfx, SectScreen.tr("book.role.desc." + row.role.name().toLowerCase(), new Object[0]), x + 10, cy += 15, w - 20, 0.74f, -11911632, 3);
        this.drawDiamondHeader(gfx, x + 10, cy += 36, w - 20, SectScreen.tr("book.share", new Object[0]));
        int top = this.topContribution();
        int frac = top <= 0 ? 0 : (int)((long)row.contribution * 100L / (long)top);
        int bw = w - 20;
        gfx.fill(x + 10, cy += 16, x + 10 + bw, cy + 8, 0x55000000);
        gfx.fill(x + 10, cy, x + 10 + Math.max(0, bw * frac / 100), cy + 8, -4885446);
        gfx.fill(x + 10, cy, x + 10 + bw, cy + 1, 0x33000000);
        this.drawScaledRight(gfx, SectScreen.tr("book.share_value", row.contribution, top, frac), x + 10 + bw, cy + 11, 0.74f, -9807288);
    }

    private void renderRelationsTab(GuiGraphics gfx, int y, int h, int mx, int my) {
        int x = 22;
        int w = 596;
        this.drawCard(gfx, x, y, w, h);
        this.drawDiamondHeaderTag(gfx, x + 8, y + 4, w - 16, SectScreen.tr("book.relations_title", new Object[0]), SectScreen.tr("book.count_members", this.members.size()), false);
        this.renderOrgChart(gfx, x + 6, y + 22, w - 12, h - 26, mx, my);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private void renderOrgChart(GuiGraphics gfx, int x, int y, int w, int h, int mx, int my) {
        EnumMap<SectRole, List<MemberRow>> byRole = new EnumMap<SectRole, List<MemberRow>>(SectRole.class);
        for (MemberRow row : this.members) {
            byRole.computeIfAbsent(row.role, k -> new ArrayList()).add(row);
        }
        SectRole[] roles = SectScreen.displayRoles();
        int contentH = this.orgContentHeight(byRole, w, roles);
        this.treeScroll = SectScreen.clamp(this.treeScroll, 0, Math.max(0, contentH - h));
        int connX = x + 9;
        int cy = y + 2 - this.treeScroll;
        int prevBadgeY = -1;
        gfx.enableScissor(this.toScreenX(x), this.toScreenY(y), this.toScreenX(x + w), this.toScreenY(y + h));
        try {
            for (SectRole role : roles) {
                List rows = byRole.getOrDefault((Object)role, List.of());
                if (rows.isEmpty()) continue;
                int badgeY = cy + 1;
                if (prevBadgeY >= 0) {
                    gfx.fill(connX, prevBadgeY + 10, connX + 1, badgeY, -3886189);
                }
                prevBadgeY = badgeY;
                this.drawRoleBadge(gfx, x + 4, badgeY, 10, role, false);
                this.drawScaledString(gfx, SectScreen.tr("role_count", role.displayName(), rows.size()), x + 20, cy + 1, 0.78f, SectScreen.roleColor(role), w - 110);
                gfx.fill(x + 96, cy + 5, x + w - 16, cy + 6, -3886189);
                cy += 13;
                int perRow = SectScreen.orgNodesPerRow(w);
                int gap = 6;
                int nodeW = SectScreen.orgNodeWidth(w, perRow, gap);
                int rowW = nodeW * perRow + gap * (perRow - 1);
                int sx = x + Math.max(20, (w - rowW) / 2);
                for (int i = 0; i < rows.size(); ++i) {
                    MemberRow row = (MemberRow)rows.get(i);
                    int col = i % perRow;
                    int ri = i / perRow;
                    int nx = sx + col * (nodeW + gap);
                    int ny = cy + ri * 17;
                    if (ny + 15 < y || ny > y + h) continue;
                    gfx.fill(connX, ny + 7, nx, ny + 7 + 1, 1438954387);
                    this.drawOrgNode(gfx, nx, ny, nodeW, row, mx, my, y, y + h);
                }
                int nodeRows = (rows.size() + perRow - 1) / perRow;
                cy += nodeRows * 17 + 6;
            }
        }
        finally {
            gfx.disableScissor();
        }
        this.drawScrollBar(gfx, x + w - 4, y, h, contentH, h, this.treeScroll);
    }

    private void drawOrgNode(GuiGraphics gfx, int x, int y, int w, MemberRow row, int mx, int my, int regionTop, int regionBottom) {
        int rectTop = Math.max(y, regionTop);
        int rectBot = Math.min(y + 15, regionBottom);
        boolean hittable = rectBot > rectTop;
        boolean selected = row.selected(this.selectedMemberId);
        boolean hover = hittable && mx >= x && mx < x + w && my >= rectTop && my < rectBot;
        gfx.fill(x, y, x + w, y + 15, selected ? -3562934 : 1429613600);
        gfx.fill(x + 1, y + 1, x + w - 1, y + 15 - 1, selected ? -6528 : (hover ? -1056582 : -528679));
        gfx.fill(x + 1, y + 1, x + 3, y + 15 - 1, SectScreen.roleColor(row.role));
        this.drawRoleBadge(gfx, x + 5, y + 2, 10, row.role, selected);
        this.drawScaledString(gfx, (Component)Component.literal((String)row.name), x + 18, y + 3, 0.75f, row.player ? -15067628 : -11911632, w - 24);
        if (row.player) {
            this.drawDiamond(gfx, x + w - 5, y + 4, 2, -6534610);
        }
        if (!hittable) {
            return;
        }
        this.memberClickRects.add(new MemberClickRect(x, rectTop, w, rectBot - rectTop, row));
        this.contentHoverTips.add(new HoverTipRect(x, rectTop, w, rectBot - rectTop, List.of(Component.literal((String)row.name).withStyle(ChatFormatting.BOLD), SectScreen.tr("member_meta", row.role.displayName(), row.contribution), SectScreen.tr(row.player ? "member_kind.player" : "member_kind.npc", new Object[0]))));
    }

    private int orgContentHeight(Map<SectRole, List<MemberRow>> byRole, int w, SectRole[] roles) {
        int total = 2;
        int perRow = SectScreen.orgNodesPerRow(w);
        for (SectRole role : roles) {
            List rows = byRole.getOrDefault((Object)role, List.of());
            if (rows.isEmpty()) continue;
            int nodeRows = (rows.size() + perRow - 1) / perRow;
            total += 13 + nodeRows * 17 + 6;
        }
        return total;
    }

    private static int orgNodesPerRow(int w) {
        return Math.max(1, Math.min(6, (w - 24) / 92));
    }

    private static int orgNodeWidth(int w, int perRow, int gap) {
        int avail = Math.max(60, w - 40 - gap * (perRow - 1));
        return Math.max(70, Math.min(96, avail / perRow));
    }

    private void renderTasksTab(GuiGraphics gfx, int y, int h, int mx, int my) {
        int lx = 22;
        int detailW = 224;
        int gap = 14;
        int listW = 618 - lx - detailW - gap;
        int dx = lx + listW + gap;
        this.drawCard(gfx, lx, y, listW, h);
        this.drawDiamondHeaderTag(gfx, lx + 6, y + 4, listW - 12, SectScreen.tr("book.task_board", new Object[0]), SectScreen.tr("book.task_board_count", this.readyTaskCount(), this.activeTaskCount()), false);
        this.renderTaskOverview(gfx, lx + 8, y + 20, listW - 16);
        this.renderTaskList(gfx, lx + 6, y + 36, listW - 12, h - 40, mx, my);
        this.drawCard(gfx, dx, y, detailW, h);
        this.renderTaskDetail(gfx, dx, y, detailW, h);
    }

    private void renderTaskOverview(GuiGraphics gfx, int x, int y, int w) {
        int turnIn = this.countTasks(t -> t.canTurnIn);
        int accept = this.countTasks(t -> t.canAccept);
        int active = this.countTasks(t -> t.accepted && !t.ready && !t.completed);
        int issuer = this.countTasks(t -> t.requiresIssuer && !t.canTurnIn && !t.canAccept && !t.completed);
        int cur = x;
        int right = x + w;
        cur = this.overviewPill(gfx, cur, y, right, SectScreen.tr("task_overview.turn_in", turnIn), turnIn > 0 ? -14722744 : -7700107);
        cur = this.overviewPill(gfx, cur, y, right, SectScreen.tr("task_overview.accept", accept), accept > 0 ? -7723482 : -7700107);
        cur = this.overviewPill(gfx, cur, y, right, SectScreen.tr("task_overview.active", active), active > 0 ? -4703686 : -7700107);
        this.overviewPill(gfx, cur, y, right, SectScreen.tr("task_overview.issuer", issuer), issuer > 0 ? -4818904 : -7700107);
    }

    private int overviewPill(GuiGraphics gfx, int x, int y, int right, Component label, int color) {
        int w = Math.min((int)((float)this.font.width((FormattedText)label) * 0.75f) + 10, right - x);
        if (w < 30) {
            return x;
        }
        gfx.fill(x, y, x + w, y + 12, color);
        this.drawScaledCentered(gfx, label, x + w / 2, y + 2, 0.75f, -528679, w - 4);
        return x + w + 4;
    }

    private void renderTaskList(GuiGraphics gfx, int x, int y, int w, int h, int mx, int my) {
        if (this.tasks.isEmpty()) {
            this.drawScaledString(gfx, SectScreen.tr("no_tasks", new Object[0]), x + 4, y + 6, 0.8f, -9807288, w - 8);
            return;
        }
        int rowH = 46;
        int step = rowH + 2;
        int visible = Math.max(1, h / step);
        int maxScroll = Math.max(0, this.tasks.size() - visible);
        this.taskScroll = SectScreen.clamp(this.taskScroll, 0, maxScroll);
        int end = Math.min(this.tasks.size(), this.taskScroll + visible);
        for (int i = this.taskScroll; i < end; ++i) {
            TaskRow task = this.tasks.get(i);
            int ry = y + (i - this.taskScroll) * step;
            int rowW = w - 8;
            boolean selected = task.id.equals(this.selectedTaskId);
            boolean hover = mx >= x && mx < x + rowW && my >= ry && my < ry + rowH;
            gfx.fill(x, ry, x + rowW, ry + rowH, selected ? -3562934 : 1429613600);
            gfx.fill(x + 1, ry + 1, x + rowW - 1, ry + rowH - 1, selected ? -6528 : (hover ? -1056582 : -528679));
            gfx.fill(x + 1, ry + 1, x + 4, ry + rowH - 1, this.taskStatusColor(task));
            this.taskSelectRects.add(new TaskSelectRect(x, ry, rowW, rowH, task.id));
            this.drawScaledString(gfx, (Component)Component.translatable((String)task.titleKey).withStyle(ChatFormatting.BOLD), x + 9, ry + 4, 0.85f, -15067628, rowW - 76);
            this.drawStatusPill(gfx, x + rowW - 62, ry + 4, 56, this.taskStatus(task), this.taskStatusColor(task));
            this.drawScaledString(gfx, SectScreen.tr("issuer", task.issuerName, task.issuerRole.displayName()), x + 9, ry + 18, 0.72f, -9807288, rowW - 16);
            int ly = ry + 29;
            int cursor = x + 9;
            if (!task.requiredStack.isEmpty()) {
                gfx.renderItem(task.requiredStack, cursor, ly - 2);
                this.itemHoverRects.add(new ItemHoverRect(cursor, ly - 2, 16, 16, task.requiredStack.copy()));
                this.drawScaledString(gfx, (Component)Component.literal((String)("\u00d7" + task.requiredStack.getCount())), cursor + 17, ly + 1, 0.72f, -11911632, 26);
                cursor += 44;
            }
            this.drawScaledString(gfx, (Component)Component.literal((String)"\u2192"), cursor, ly + 1, 0.78f, -9807288, 12);
            Component reward = task.rewardStack.isEmpty() ? SectScreen.tr("reward_contribution_only", task.contribution) : SectScreen.tr("reward_line", task.rewardStack.getHoverName(), task.contribution);
            this.drawScaledString(gfx, reward, cursor += 12, ly + 1, 0.72f, -7723482, x + rowW - cursor - 6);
            if (task.requiredCount <= 0) continue;
            int pbx = x + 9;
            int pbw = rowW - 18;
            int pby = ry + rowH - 8;
            this.drawProgressBar(gfx, pbx, pby, pbw, task.heldRequired, task.requiredCount, task.ready);
        }
        this.drawScrollBar(gfx, x + w - 4, y, h, this.tasks.size(), visible, this.taskScroll);
    }

    private void renderTaskDetail(GuiGraphics gfx, int x, int y, int w, int h) {
        TaskRow task = this.selectedTask();
        if (task == null) {
            this.drawScaledCentered(gfx, SectScreen.tr("no_tasks", new Object[0]), x + w / 2, y + h / 2 - 6, 0.8f, -9807288, w - 20);
            return;
        }
        this.drawDiamondHeader(gfx, x + 8, y + 4, w - 16, (Component)Component.translatable((String)task.titleKey));
        int cy = y + 22;
        cy = this.drawWrappedLimited(gfx, (Component)Component.translatable((String)task.conditionKey), x + 10, cy, w - 20, 0.75f, -11911632, 3);
        this.drawStatGridRow(gfx, x + 10, cy += 6, w - 20, SectScreen.tr("issuer_label", new Object[0]), (Component)Component.literal((String)task.issuerName), -11911632, SectScreen.tr("book.role_label", new Object[0]), task.issuerRole.displayName(), -11911632);
        Component need = task.requiredCount > 0 ? Component.literal((String)(Math.min(task.heldRequired, task.requiredCount) + "/" + task.requiredCount)) : SectScreen.tr("book.dash", new Object[0]);
        this.drawStatGridRow(gfx, x + 10, cy += 14, w - 20, SectScreen.tr("book.required", new Object[0]), need, task.ready ? -13668780 : -5750484, SectScreen.tr("book.state", new Object[0]), this.taskPhase(task), this.taskPhaseColor(task));
        this.drawDiamondHeader(gfx, x + 10, cy += 20, w - 20, SectScreen.tr("book.items", new Object[0]));
        int slotX = x + 24;
        this.drawDetailSlot(gfx, slotX, cy += 16, 26, task.requiredStack);
        this.drawScaledString(gfx, (Component)Component.literal((String)"\u2192"), slotX + 32, cy + 9, 1.0f, -9807288, 14);
        this.drawDetailSlot(gfx, slotX + 52, cy, 26, task.rewardStack);
        this.drawScaledString(gfx, SectScreen.tr("book.required", new Object[0]), slotX, cy + 28, 0.66f, -9807288, 26);
        this.drawScaledString(gfx, SectScreen.tr("book.reward", new Object[0]), slotX + 52, cy + 28, 0.66f, -9807288, 26);
        cy += 50;
        if (task.requiredCount > 0) {
            this.drawDiamondHeader(gfx, x + 10, cy, w - 20, SectScreen.tr("book.turn_progress", new Object[0]));
            int bw = w - 20;
            this.drawProgressBar(gfx, x + 10, cy += 16, bw, task.heldRequired, task.requiredCount, task.ready);
            this.drawScaledRight(gfx, (Component)(task.ready ? SectScreen.tr("book.turn_done", new Object[0]) : Component.literal((String)(Math.min(task.heldRequired, task.requiredCount) + " / " + task.requiredCount))), x + 10 + bw, cy + 9, 0.72f, task.ready ? -13668780 : -9807288);
            cy += 22;
        }
        this.drawWrappedLimited(gfx, this.taskNextStep(task), x + 10, cy, w - 20, 0.72f, -9807288, 2);
    }

    private void drawDetailSlot(GuiGraphics gfx, int x, int y, int size, ItemStack stack) {
        gfx.fill(x - 1, y - 1, x + size + 1, y + size + 1, -10724784);
        gfx.fill(x, y, x + size, y + size, -1448230);
        gfx.fill(x + 2, y + 2, x + size - 2, y + size - 2, -2762800);
        if (!stack.isEmpty()) {
            int off = (size - 16) / 2;
            gfx.renderItem(stack, x + off, y + off);
            gfx.renderItemDecorations(this.font, stack, x + off, y + off);
            this.itemHoverRects.add(new ItemHoverRect(x + off, y + off, 16, 16, stack.copy()));
        } else {
            int cx = x + size / 2;
            int cy = y + size / 2;
            gfx.fill(cx - 4, cy - 4, cx + 4, cy + 4, -6578022);
        }
    }

    private void bottomActionRow(GuiGraphics gfx, int mx, int my, boolean hasSect) {
        TaskRow task;
        int x = 22;
        int w = 596;
        this.drawPaperDivider(gfx, x, 320, w);
        int by = 322;
        int bh = 18;
        int gap = 12;
        ActionKind kind = ActionKind.NONE;
        Component label = null;
        int accent = -14722744;
        String taskId = null;
        TaskRow taskRow = task = this.tab == Tab.TASKS ? this.selectedTask() : null;
        if (hasSect && task != null && this.targetEntityId >= 0 && !this.viewerEnemy && (task.canTurnIn || task.canAccept)) {
            if (task.canTurnIn) {
                kind = ActionKind.TASK_TURN_IN;
                label = SectScreen.tr("task.turn_in", new Object[0]);
                accent = -14722744;
            } else {
                kind = ActionKind.TASK_ACCEPT;
                label = SectScreen.tr("task.accept", new Object[0]);
                accent = -7723482;
            }
            taskId = task.id;
        } else if (hasSect && task != null && task.requiresIssuer && !this.viewerEnemy && !task.completed) {
            kind = ActionKind.TASK_TRACK;
            label = SectScreen.tr("task.track_issuer", new Object[0]);
            accent = -4818904;
            taskId = task.id;
        } else if (this.canJoin && this.targetEntityId >= 0) {
            kind = ActionKind.JOIN;
            label = SectScreen.tr("book.action.join", new Object[0]);
            accent = -14722744;
        } else if (this.viewerMember) {
            kind = ActionKind.IMMUNITY;
            label = this.sameSectImmunity ? SectScreen.tr("same_immunity_on", new Object[0]) : SectScreen.tr("same_immunity_off", new Object[0]);
            accent = this.sameSectImmunity ? -14722744 : -9807288;
        } else if (this.viewerEnemy) {
            kind = ActionKind.NONE;
            label = SectScreen.tr("book.action.locked", new Object[0]);
            accent = -7723482;
        }
        Component closeLabel = SectScreen.tr("book.action.close", new Object[0]);
        if (kind == ActionKind.NONE && label == null) {
            this.actionSecondaryRect = this.drawActionButton(gfx, x, by, w, bh, closeLabel, -9807288, true, mx, my);
            this.actionPrimaryKind = ActionKind.NONE;
        } else {
            int w1 = (w - gap) * 2 / 3;
            int w2 = w - gap - w1;
            boolean enabled = kind != ActionKind.NONE;
            this.actionPrimaryRect = this.drawActionButton(gfx, x, by, w1, bh, label, accent, enabled, mx, my);
            this.actionPrimaryKind = kind;
            this.actionPrimaryTaskId = taskId;
            this.actionSecondaryRect = this.drawActionButton(gfx, x + w1 + gap, by, w2, bh, closeLabel, -9807288, true, mx, my);
        }
    }

    private void renderBookSpine(GuiGraphics gfx, boolean hasSect) {
        int spineX = -40;
        gfx.fill(spineX - 2, 8, -3, 336, -1441984236);
        gfx.fill(spineX, 10, -6, 334, -14665650);
        gfx.fill(spineX + 4, 14, -10, 330, -13676952);
        int labelW = 28;
        int labelH = 130;
        int labelX = spineX + 5;
        int labelY = 40;
        gfx.fill(labelX - 2, labelY - 4, labelX + labelW + 2, labelY + labelH + 4, -1441984236);
        gfx.fill(labelX, labelY, labelX + labelW, labelY + labelH, -527903);
        gfx.fill(labelX + 2, labelY + 2, labelX + labelW - 2, labelY + labelH - 2, -1186608);
        String text = hasSect ? SectScreen.trimChars(this.snapshot.getString("sectName"), 4) : SectScreen.tr("side_label", new Object[0]).getString();
        int centerX = labelX + labelW / 2;
        float s = 1.5f;
        int charH = (int)(14.0f * s);
        int startY = labelY + (labelH - text.length() * charH) / 2 + 4;
        for (int i = 0; i < text.length(); ++i) {
            MutableComponent part = Component.literal((String)String.valueOf(text.charAt(i))).withStyle(ChatFormatting.BOLD);
            gfx.pose().pushPose();
            gfx.pose().translate((float)centerX, (float)(startY + i * charH), 0.0f);
            gfx.pose().scale(s, s, 1.0f);
            gfx.drawCenteredString(this.font, (Component)part, 0, 0, -15067628);
            gfx.pose().popPose();
        }
    }

    private void drawWidePaperFrame(GuiGraphics gfx, int x, int y, int w, int h) {
        float pivotX = (float)x + (float)w / 2.0f;
        float pivotY = (float)y + (float)h / 2.0f;
        gfx.pose().pushPose();
        gfx.pose().translate(pivotX, pivotY, 0.0f);
        gfx.pose().mulPose(Axis.ZP.rotationDegrees(1.4f));
        gfx.pose().translate(-pivotX + 8.0f, -pivotY + 9.0f, 0.0f);
        this.parchmentSheet(gfx, x, y, w, h, false);
        gfx.pose().popPose();
        this.parchmentSheet(gfx, x, y, w, h, true);
    }

    private void parchmentSheet(GuiGraphics gfx, int x, int y, int w, int h, boolean topSheet) {
        gfx.fill(x - 5, y - 4, x + w + 5, y + h + 5, topSheet ? 0x66000000 : -2013265920);
        gfx.fill(x - 3, y - 3, x + w + 3, y + h + 3, -15658735);
        gfx.fill(x - 1, y - 1, x + w + 1, y + h + 1, -2504802);
        gfx.fill(x, y, x + w, y + h, topSheet ? -790821 : -1186608);
        gfx.fill(x + 7, y + 7, x + w - 7, y + 8, 1146767926);
        gfx.fill(x + 7, y + h - 8, x + w - 7, y + h - 7, 1146767926);
        gfx.fill(x + 7, y + 7, x + 8, y + h - 7, 1146767926);
        gfx.fill(x + w - 8, y + 7, x + w - 7, y + h - 7, 1146767926);
        if (!topSheet) {
            return;
        }
        for (int i = 0; i < 58; ++i) {
            int sx = x + 24 + i * 37 % Math.max(48, w - 48);
            int sy = y + 16 + i * 23 % Math.max(48, h - 42);
            gfx.fill(sx, sy, sx + 8, sy + 1, 312773986);
        }
    }

    private void ribbonBookmark(GuiGraphics gfx) {
        int rx = 40;
        int t = -2;
        gfx.fill(rx, t, rx + 12, t + 30, -7725024);
        gfx.fill(rx + 1, t, rx + 11, t + 30, -4706256);
        gfx.fill(rx + 4, t, rx + 8, t + 30, -3127738);
        gfx.fill(rx, t + 30, rx + 5, t + 35, -4706256);
        gfx.fill(rx + 7, t + 30, rx + 12, t + 35, -4706256);
        gfx.fill(rx + 5, t + 30, rx + 7, t + 33, -7725024);
    }

    private void drawBrushTitle(GuiGraphics gfx, int centerX, int y, Component titleLine) {
        MutableComponent bold = titleLine.copy().withStyle(ChatFormatting.BOLD);
        int textW = Math.min(232, Math.max(108, this.font.width((FormattedText)bold) + 46));
        gfx.fill(centerX - textW / 2 - 1, y - 1, centerX + textW / 2 + 1, y + 24, -3562934);
        gfx.fill(centerX - textW / 2, y, centerX + textW / 2, y + 23, -13676952);
        gfx.fill(centerX - textW / 2, y, centerX + textW / 2, y + 2, -2130712960);
        gfx.drawCenteredString(this.font, this.trimToWidth((Component)bold, textW - 18), centerX, y + 8, -528679);
    }

    private int[] drawCloseButton(GuiGraphics gfx, int x, int y, int mx, int my) {
        boolean hover = mx >= x && mx < x + 24 && my >= y && my < y + 24;
        int cx = x + 12;
        int cy = y + 12;
        this.fillCircle(gfx, cx, cy, 13, -14016488);
        this.fillCircle(gfx, cx, cy, 12, hover ? -2800315 : -4703686);
        int ink = -397604;
        for (int i = 0; i < 9; ++i) {
            gfx.fill(cx - 4 + i, cy - 4 + i, cx - 2 + i, cy - 2 + i, ink);
            gfx.fill(cx + 4 - i, cy - 4 + i, cx + 6 - i, cy - 2 + i, ink);
        }
        return new int[]{x, y, 24, 24};
    }

    private void renderTopTabs(GuiGraphics gfx, int centerX, int y, int mx, int my) {
        Tab[] tabs = new Tab[]{Tab.INFO, Tab.MEMBERS, Tab.TREE, Tab.TASKS};
        int gap = 6;
        int tabW = 78;
        int tabH = 18;
        int total = tabs.length * tabW + (tabs.length - 1) * gap;
        int x = centerX - total / 2;
        for (Tab next : tabs) {
            boolean hover;
            boolean active = this.tab == next;
            boolean bl = hover = mx >= x && mx < x + tabW && my >= y && my < y + tabH;
            int bg = active ? -12365222 : (hover ? -1056582 : -528679);
            int fg = active ? -528679 : -15067628;
            int border = active ? -3562934 : -10859978;
            gfx.fill(x - 1, y - 1, x + tabW + 1, y + tabH + 1, border);
            gfx.fill(x, y, x + tabW, y + tabH, bg);
            gfx.fill(x + 4, y + 3, x + 7, y + tabH - 3, active ? -10496 : border);
            if (active) {
                gfx.fill(x + 12, y + 3, x + tabW - 12, y + 5, 0x33FFFFFF);
            }
            gfx.drawCenteredString(this.font, this.trimToWidth(this.tabLabel(next), tabW - 8), x + tabW / 2, y + 5, fg);
            this.tabClickRects.add(new TabClickRect(x, y, tabW, tabH, next));
            x += tabW + gap;
        }
    }

    private Component tabLabel(Tab value) {
        return SectScreen.tr("tab." + SectScreen.tabKey(value), new Object[0]);
    }

    private static String tabKey(Tab value) {
        return switch (value) {
            default -> throw new IncompatibleClassChangeError();
            case INFO -> "info";
            case MEMBERS -> "members";
            case TREE -> "tree";
            case TASKS -> "tasks";
        };
    }

    private void drawNoSectState(GuiGraphics gfx) {
        int x = 22;
        int y = 70;
        int w = 596;
        int h = 316 - y;
        this.drawCard(gfx, x, y, w, h);
        this.fillCircle(gfx, x + w / 2, y + h / 2 - 26, 22, 0x22000000);
        this.drawScaledCentered(gfx, SectScreen.tr("seal", new Object[0]), x + w / 2, y + h / 2 - 36, 2.0f, -7702689, 60);
        this.drawScaledCentered(gfx, (Component)SectScreen.tr("none_title", new Object[0]).copy().withStyle(ChatFormatting.BOLD), x + w / 2, y + h / 2 + 6, 1.0f, -14081252, w - 40);
        this.drawScaledCentered(gfx, SectScreen.tr("none_body", new Object[0]), x + w / 2, y + h / 2 + 22, 0.8f, -9807288, w - 60);
    }

    private void drawCard(GuiGraphics gfx, int x, int y, int w, int h) {
        gfx.fill(x - 1, y - 1, x + w + 1, y + h + 1, 0x33000000);
        gfx.fill(x, y, x + w, y + h, -1055026);
        gfx.fill(x + 1, y + 1, x + w - 1, y + h - 1, -527903);
    }

    private void drawDiamondHeader(GuiGraphics gfx, int x, int y, int w, Component label) {
        MutableComponent bold = label.copy().withStyle(ChatFormatting.BOLD);
        int cx = x + w / 2;
        int tw = Math.min(this.font.width((FormattedText)bold), Math.max(20, w - 44));
        int midY = y + 4;
        int half = tw / 2;
        int leftDx = cx - half - 9;
        int rightDx = cx + half + 9;
        if (leftDx - 6 > x) {
            gfx.fill(x, midY, leftDx - 6, midY + 1, -3886189);
        }
        if (rightDx + 6 < x + w) {
            gfx.fill(rightDx + 6, midY, x + w, midY + 1, -3886189);
        }
        this.drawDiamond(gfx, leftDx, midY, 3, -6534610);
        this.drawDiamond(gfx, rightDx, midY, 3, -6534610);
        gfx.drawCenteredString(this.font, this.trimToWidth((Component)bold, w - 44), cx, y, -14081252);
    }

    private void drawDiamondHeaderTag(GuiGraphics gfx, int x, int y, int w, Component label, Component tag, boolean on) {
        this.drawDiamondHeader(gfx, x, y, w, label);
        float s = 0.8f;
        int tagW = (int)((float)this.font.width((FormattedText)tag) * s) + 6;
        Objects.requireNonNull(this.font);
        int tagH = (int)(9.0f * s) + 3;
        int tagX = x + w - tagW;
        int tagY = y - 1;
        gfx.fill(tagX, tagY, tagX + tagW, tagY + tagH, on ? -13668780 : -7438745);
        gfx.pose().pushPose();
        gfx.pose().translate((float)(tagX + 3), (float)(tagY + 2), 0.0f);
        gfx.pose().scale(s, s, 1.0f);
        gfx.drawString(this.font, tag, 0, 0, -528679, false);
        gfx.pose().popPose();
    }

    private void drawDiamond(GuiGraphics gfx, int cx, int cy, int half, int color) {
        for (int dy = -half; dy <= half; ++dy) {
            int dx = half - Math.abs(dy);
            gfx.fill(cx - dx, cy + dy, cx + dx + 1, cy + dy + 1, color);
        }
    }

    private void drawDiamondOutline(GuiGraphics gfx, int cx, int cy, int half, int color) {
        for (int dy = -half; dy <= half; ++dy) {
            int dx = half - Math.abs(dy);
            gfx.fill(cx - dx, cy + dy, cx - dx + 1, cy + dy + 1, color);
            gfx.fill(cx + dx, cy + dy, cx + dx + 1, cy + dy + 1, color);
        }
    }

    private void drawStatGridRow(GuiGraphics gfx, int x, int y, int w, Component k1, Component v1, int c1, Component k2, Component v2, int c2) {
        int half = w / 2;
        this.drawStatCell(gfx, x, y, half - 6, k1, v1, c1);
        this.drawStatCell(gfx, x + half, y, half - 6, k2, v2, c2);
    }

    private void drawStatCell(GuiGraphics gfx, int x, int y, int w, Component label, Component value, int valueColor) {
        gfx.drawString(this.font, this.trimToWidth(label, 38), x, y, -9807288, false);
        this.drawScaledString(gfx, (Component)value.copy().withStyle(ChatFormatting.BOLD), x + 42, y, 1.0f, valueColor, Math.max(10, w - 42));
    }

    private void drawTagPill(GuiGraphics gfx, int x, int y, Component s, int color) {
        int w = (int)((float)this.font.width((FormattedText)s) * 0.74f) + 8;
        gfx.fill(x, y, x + w, y + 11, color & 0xFFFFFF | 0x33000000);
        gfx.fill(x, y, x + 2, y + 11, color);
        this.drawScaledString(gfx, s, x + 5, y + 2, 0.74f, color, w - 6);
    }

    private void drawStatusPill(GuiGraphics gfx, int x, int y, int w, Component text, int color) {
        gfx.fill(x, y, x + w, y + 12, color);
        this.drawScaledCentered(gfx, text, x + w / 2, y + 2, 0.78f, -528679, w - 4);
    }

    private void drawProgressBar(GuiGraphics gfx, int x, int y, int w, int held, int required, boolean ready) {
        int req = Math.max(1, required);
        int h = SectScreen.clamp(held, 0, req);
        int fillW = w * h / req;
        gfx.fill(x, y, x + w, y + 6, 1714826272);
        gfx.fill(x + 1, y + 1, x + w - 1, y + 5, -2043731);
        if (fillW > 0) {
            gfx.fill(x + 1, y + 1, x + Math.max(2, fillW), y + 5, ready ? -13664921 : -4703686);
        }
    }

    private int[] drawActionButton(GuiGraphics gfx, int x, int y, int w, int h, Component label, int accent, boolean enabled, int mx, int my) {
        int border;
        boolean hover = enabled && mx >= x && mx < x + w && my >= y && my < y + h;
        int n = border = enabled ? accent : -7438745;
        int fill = !enabled ? -1910849 : (hover ? -3640 : -528679);
        gfx.fill(x, y, x + w, y + h, border);
        gfx.fill(x + 2, y + 2, x + w - 2, y + h - 2, fill);
        Component component = this.trimToWidth((Component)label.copy().withStyle(ChatFormatting.BOLD), w - 8);
        int n2 = x + w / 2;
        Objects.requireNonNull(this.font);
        gfx.drawCenteredString(this.font, component, n2, y + (h - 9) / 2 + 1, enabled ? border : -9807288);
        return new int[]{x, y, w, h};
    }

    private void drawPaperDivider(GuiGraphics gfx, int x, int y, int width) {
        gfx.fill(x, y, x + width, y + 1, 1720353141);
        gfx.fill(x + width / 2 - 2, y - 2, x + width / 2 + 3, y + 3, -1433763467);
    }

    private void sealCircle(GuiGraphics gfx, int cx, int cy, int r, int ring, Component glyph) {
        this.fillCircle(gfx, cx, cy, r, -528679);
        this.drawCircleRing(gfx, cx, cy, r, r - 2, ring);
        this.drawCircleRing(gfx, cx, cy, r - 2, r - 3, -3562934);
        this.drawScaledCentered(gfx, (Component)glyph.copy().withStyle(ChatFormatting.BOLD), cx, cy - 8, 1.6f, ring, r * 2 - 6);
    }

    private void fillCircle(GuiGraphics gfx, int cx, int cy, int r, int color) {
        for (int dy = -r; dy <= r; ++dy) {
            int dx = (int)Math.round(Math.sqrt((double)r * (double)r - (double)(dy * dy)));
            gfx.fill(cx - dx, cy + dy, cx + dx + 1, cy + dy + 1, color);
        }
    }

    private void drawCircleRing(GuiGraphics gfx, int cx, int cy, int rOuter, int rInner, int color) {
        for (int dy = -rOuter; dy <= rOuter; ++dy) {
            int outer = (int)Math.round(Math.sqrt((double)rOuter * (double)rOuter - (double)(dy * dy)));
            double innSq = (double)rInner * (double)rInner - (double)(dy * dy);
            if (innSq <= 0.0) {
                gfx.fill(cx - outer, cy + dy, cx + outer + 1, cy + dy + 1, color);
                continue;
            }
            int inner = (int)Math.round(Math.sqrt(innSq));
            gfx.fill(cx - outer, cy + dy, cx - inner, cy + dy + 1, color);
            gfx.fill(cx + inner + 1, cy + dy, cx + outer + 1, cy + dy + 1, color);
        }
    }

    private void drawRoleBadge(GuiGraphics gfx, int x, int y, int size, SectRole role, boolean selected) {
        int color = SectScreen.roleColor(role);
        int soft = selected ? -3640 : -528679;
        gfx.fill(x + size / 2 - 2, y, x + size / 2 + 3, y + size, 1429613600);
        gfx.fill(x, y + size / 2 - 2, x + size, y + size / 2 + 3, 1429613600);
        gfx.fill(x + size / 2 - 1, y + 1, x + size / 2 + 2, y + size - 1, soft);
        gfx.fill(x + 1, y + size / 2 - 1, x + size - 1, y + size / 2 + 2, soft);
        gfx.fill(x + size / 2, y + 2, x + size / 2 + 1, y + size - 2, color);
        gfx.fill(x + 2, y + size / 2, x + size - 2, y + size / 2 + 1, color);
    }

    private void drawScrollBar(GuiGraphics gfx, int x, int y, int h, int total, int visible, int offset) {
        if (total <= visible) {
            return;
        }
        gfx.fill(x, y, x + 3, y + h, -9807288);
        int thumbH = Math.min(h, Math.max(12, h * visible / total));
        int maxScroll = Math.max(1, total - visible);
        int thumbY = y + (h - thumbH) * SectScreen.clamp(offset, 0, maxScroll) / maxScroll;
        gfx.fill(x, thumbY, x + 3, thumbY + thumbH, -3562934);
    }

    private void drawScaledString(GuiGraphics gfx, Component c, int x, int y, float scale, int color, int maxWidth) {
        Component draw = c;
        if ((float)this.font.width((FormattedText)c) * scale > (float)maxWidth) {
            draw = this.trimToWidth(c, Math.max(0, (int)((float)maxWidth / scale)));
        }
        gfx.pose().pushPose();
        gfx.pose().translate((float)x, (float)y, 0.0f);
        gfx.pose().scale(scale, scale, 1.0f);
        gfx.drawString(this.font, draw, 0, 0, color, false);
        gfx.pose().popPose();
    }

    private void drawScaledRight(GuiGraphics gfx, Component c, int rightX, int y, float scale, int color) {
        int w = (int)((float)this.font.width((FormattedText)c) * scale);
        this.drawScaledString(gfx, c, rightX - w, y, scale, color, w + 4);
    }

    private void drawScaledCentered(GuiGraphics gfx, Component c, int centerX, int y, float scale, int color, int maxWidth) {
        Component draw = c;
        if ((float)this.font.width((FormattedText)c) * scale > (float)maxWidth) {
            draw = this.trimToWidth(c, Math.max(0, (int)((float)maxWidth / scale)));
        }
        int w = (int)((float)this.font.width((FormattedText)draw) * scale);
        this.drawScaledString(gfx, draw, centerX - w / 2, y, scale, color, maxWidth);
    }

    private int drawWrappedLimited(GuiGraphics gfx, Component text, int x, int y, int maxWidth, float scale, int color, int maxLines) {
        List lines = this.font.split((FormattedText)text, (int)((float)maxWidth / scale));
        int drawn = Math.min(maxLines, lines.size());
        Objects.requireNonNull(this.font);
        int lineH = (int)((float)(9 + 1) * scale);
        for (int i = 0; i < drawn; ++i) {
            gfx.pose().pushPose();
            gfx.pose().translate((float)x, (float)(y + i * lineH), 0.0f);
            gfx.pose().scale(scale, scale, 1.0f);
            gfx.drawString(this.font, (FormattedCharSequence)lines.get(i), 0, 0, color, false);
            gfx.pose().popPose();
        }
        return y + drawn * lineH;
    }

    private Component trimToWidth(Component text, int maxWidth) {
        String value = text.getString();
        if (this.font.width(value) <= maxWidth) {
            return text;
        }
        return Component.literal((String)(this.font.plainSubstrByWidth(value, Math.max(0, maxWidth - this.font.width("..."))) + "...")).withStyle(text.getStyle());
    }

    private void renderHoverTooltip(GuiGraphics gfx, int lmx, int lmy, int screenX, int screenY) {
        for (ItemHoverRect itemHoverRect : this.itemHoverRects) {
            if (!itemHoverRect.contains(lmx, lmy) || itemHoverRect.stack.isEmpty()) continue;
            gfx.renderTooltip(this.font, itemHoverRect.stack, screenX, screenY);
            return;
        }
        for (HoverTipRect hoverTipRect : this.contentHoverTips) {
            if (!hoverTipRect.contains(lmx, lmy)) continue;
            gfx.renderComponentTooltip(this.font, hoverTipRect.lines, screenX, screenY);
            return;
        }
    }

    public boolean calculateIngredientsPositions(double mouseX, double mouseY, int button) {
        double ly;
        if (button != 0) {
            return super.mouseClicked(mouseX, mouseY, button);
        }
        double lx = this.lastViewport.toLocalX((int)mouseX);
        if (SectScreen.inRect(this.closeButtonRect, lx, ly = (double)this.lastViewport.toLocalY((int)mouseY))) {
            Minecraft.getInstance().setScreen(null);
            return true;
        }
        for (TabClickRect tabClickRect : this.tabClickRects) {
            if (!tabClickRect.contains(lx, ly)) continue;
            this.tab = tabClickRect.tab;
            this.draggingTreeScroll = false;
            this.draggingTaskScroll = false;
            this.draggingMemberScroll = false;
            return true;
        }
        if (SectScreen.inRect(this.actionSecondaryRect, lx, ly)) {
            Minecraft.getInstance().setScreen(null);
            return true;
        }
        if (this.actionPrimaryKind != ActionKind.NONE && SectScreen.inRect(this.actionPrimaryRect, lx, ly)) {
            this.handlePrimaryAction();
            return true;
        }
        if (this.tab == Tab.MEMBERS && this.startScroll(lx, ly, this.memberScrollMetrics(), 0)) {
            return true;
        }
        if (this.tab == Tab.TASKS && this.startScroll(lx, ly, this.taskScrollMetrics(), 1)) {
            return true;
        }
        if (this.tab == Tab.TREE && this.startScroll(lx, ly, this.treeScrollMetrics(), 2)) {
            return true;
        }
        for (MemberClickRect memberClickRect : this.memberClickRects) {
            if (!memberClickRect.contains(lx, ly)) continue;
            this.selectedMemberId = memberClickRect.row.id;
            this.selectedMemberName = memberClickRect.row.name;
            if (this.tab == Tab.TREE) {
                this.tab = Tab.MEMBERS;
            }
            return true;
        }
        for (TaskSelectRect taskSelectRect : this.taskSelectRects) {
            if (!taskSelectRect.contains(lx, ly)) continue;
            this.selectedTaskId = taskSelectRect.taskId;
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    private void handlePrimaryAction() {
        switch (this.actionPrimaryKind) {
            case JOIN: {
                ModNetwork.CHANNEL.sendToServer((Object)new RequestSectJoinDialoguePacket(this.targetEntityId));
                Minecraft.getInstance().setScreen(null);
                break;
            }
            case IMMUNITY: {
                this.toggleImmunity();
                break;
            }
            case TASK_TURN_IN: {
                if (this.targetEntityId < 0 || this.actionPrimaryTaskId == null) break;
                ModNetwork.CHANNEL.sendToServer((Object)new SectTaskActionPacket(this.targetEntityId, this.actionPrimaryTaskId, true));
                break;
            }
            case TASK_ACCEPT: {
                if (this.targetEntityId < 0 || this.actionPrimaryTaskId == null) break;
                ModNetwork.CHANNEL.sendToServer((Object)new SectTaskActionPacket(this.targetEntityId, this.actionPrimaryTaskId, false));
                break;
            }
            case TASK_TRACK: {
                if (this.actionPrimaryTaskId == null) break;
                ModNetwork.CHANNEL.sendToServer((Object)new TrackSectTaskIssuerPacket(this.actionPrimaryTaskId));
                break;
            }
        }
    }

    private boolean startScroll(double lx, double ly, ScrollMetrics metrics, int which) {
        if (!this.isInsideScrollbar(lx, ly, metrics)) {
            return false;
        }
        double grab = SectScreen.scrollGrabOffset(ly, metrics, this.currentScroll(which));
        int newScroll = SectScreen.scrollFromMouseY(ly, metrics, grab);
        switch (which) {
            case 0: {
                this.draggingMemberScroll = true;
                this.memberScrollGrab = grab;
                this.memberScroll = newScroll;
                break;
            }
            case 1: {
                this.draggingTaskScroll = true;
                this.taskScrollGrab = grab;
                this.taskScroll = newScroll;
                break;
            }
            default: {
                this.draggingTreeScroll = true;
                this.treeScrollGrab = grab;
                this.treeScroll = newScroll;
            }
        }
        return true;
    }

    private int currentScroll(int which) {
        return which == 0 ? this.memberScroll : (which == 1 ? this.taskScroll : this.treeScroll);
    }

    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (button == 0) {
            double ly = this.lastViewport.toLocalY((int)mouseY);
            if (this.draggingMemberScroll) {
                this.memberScroll = SectScreen.scrollFromMouseY(ly, this.memberScrollMetrics(), this.memberScrollGrab);
                return true;
            }
            if (this.draggingTaskScroll) {
                this.taskScroll = SectScreen.scrollFromMouseY(ly, this.taskScrollMetrics(), this.taskScrollGrab);
                return true;
            }
            if (this.draggingTreeScroll) {
                this.treeScroll = SectScreen.scrollFromMouseY(ly, this.treeScrollMetrics(), this.treeScrollGrab);
                return true;
            }
        }
        return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }

    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (button == 0 && (this.draggingMemberScroll || this.draggingTaskScroll || this.draggingTreeScroll)) {
            this.draggingTreeScroll = false;
            this.draggingTaskScroll = false;
            this.draggingMemberScroll = false;
            return true;
        }
        return super.mouseReleased(mouseX, mouseY, button);
    }

    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        if (this.tab == Tab.MEMBERS) {
            this.memberScroll = SectScreen.clamp(this.memberScroll - (int)Math.signum(delta), 0, this.memberScrollMetrics().maxScroll());
        } else if (this.tab == Tab.TASKS) {
            this.taskScroll = SectScreen.clamp(this.taskScroll - (int)Math.signum(delta), 0, this.taskScrollMetrics().maxScroll());
        } else if (this.tab == Tab.TREE) {
            this.treeScroll = SectScreen.clamp(this.treeScroll - (int)Math.signum(delta) * 12, 0, this.treeScrollMetrics().maxScroll());
        } else {
            return super.mouseScrolled(mouseX, mouseY, delta);
        }
        return true;
    }

    private static boolean inRect(int[] rect, double x, double y) {
        return rect != null && rect.length >= 4 && rect[2] > 0 && rect[3] > 0 && x >= (double)rect[0] && x < (double)(rect[0] + rect[2]) && y >= (double)rect[1] && y < (double)(rect[1] + rect[3]);
    }

    private ScrollMetrics memberScrollMetrics() {
        int listW = 240;
        int x = 26 + (listW - 8) - 4;
        int y = 92;
        int h = 220;
        return new ScrollMetrics(x, y, h, this.members.size(), Math.max(1, h / 18));
    }

    private ScrollMetrics taskScrollMetrics() {
        int detailW = 224;
        int gap = 14;
        int listW = 596 - detailW - gap;
        int x = 28 + (listW - 12) - 4;
        int y = 106;
        int h = 206;
        return new ScrollMetrics(x, y, h, this.tasks.size(), Math.max(1, h / 48));
    }

    private ScrollMetrics treeScrollMetrics() {
        int w = 596;
        int x = 28 + (w - 12) - 4;
        int y = 92;
        int h = 220;
        EnumMap<SectRole, List<MemberRow>> byRole = new EnumMap<SectRole, List<MemberRow>>(SectRole.class);
        for (MemberRow row : this.members) {
            byRole.computeIfAbsent(row.role, k -> new ArrayList()).add(row);
        }
        SectRole[] roles = SectScreen.displayRoles();
        return new ScrollMetrics(x, y, h, this.orgContentHeight(byRole, w - 12, roles), h);
    }

    private boolean isInsideScrollbar(double x, double y, ScrollMetrics m) {
        return m.maxScroll() > 0 && x >= (double)(m.x - 3) && x < (double)(m.x + 3 + 3) && y >= (double)m.y && y < (double)(m.y + m.height);
    }

    private static double scrollGrabOffset(double y, ScrollMetrics m, int offset) {
        int thumbH = SectScreen.scrollThumbHeight(m);
        int thumbY = SectScreen.scrollThumbY(m, offset);
        if (y >= (double)thumbY && y < (double)(thumbY + thumbH)) {
            return y - (double)thumbY;
        }
        return (double)thumbH / 2.0;
    }

    private static int scrollFromMouseY(double y, ScrollMetrics m, double grab) {
        int maxScroll = m.maxScroll();
        if (maxScroll <= 0) {
            return 0;
        }
        int thumbH = SectScreen.scrollThumbHeight(m);
        int travel = Math.max(1, m.height - thumbH);
        double progress = (y - (double)m.y - grab) / (double)travel;
        return SectScreen.clamp((int)Math.round(progress * (double)maxScroll), 0, maxScroll);
    }

    private static int scrollThumbHeight(ScrollMetrics m) {
        if (m.total <= 0) {
            return m.height;
        }
        return Math.min(m.height, Math.max(12, m.height * m.visible / m.total));
    }

    private static int scrollThumbY(ScrollMetrics m, int offset) {
        int maxScroll = m.maxScroll();
        if (maxScroll <= 0) {
            return m.y;
        }
        int thumbH = SectScreen.scrollThumbHeight(m);
        return m.y + (m.height - thumbH) * SectScreen.clamp(offset, 0, maxScroll) / maxScroll;
    }

    private MemberRow selectedMember() {
        if (this.selectedMemberId != null) {
            for (MemberRow row : this.members) {
                if (!row.selected(this.selectedMemberId)) continue;
                return row;
            }
        }
        return this.members.isEmpty() ? null : this.members.get(0);
    }

    private TaskRow selectedTask() {
        if (this.selectedTaskId != null) {
            for (TaskRow row : this.tasks) {
                if (!row.id.equals(this.selectedTaskId)) continue;
                return row;
            }
        }
        for (TaskRow row : this.tasks) {
            if (!row.canTurnIn && !row.canAccept) continue;
            return row;
        }
        return this.tasks.isEmpty() ? null : this.tasks.get(0);
    }

    private MemberRow firstByRole(SectRole role) {
        for (MemberRow row : this.members) {
            if (row.role != role) continue;
            return row;
        }
        return null;
    }

    private int memberRank(MemberRow target) {
        int rank = 1;
        for (MemberRow row : this.members) {
            if (row.contribution <= target.contribution) continue;
            ++rank;
        }
        return rank;
    }

    private int topContribution() {
        int top = 0;
        for (MemberRow row : this.members) {
            top = Math.max(top, row.contribution);
        }
        return top;
    }

    private Component headerSubtitle() {
        if (this.viewerEnemy) {
            return SectScreen.tr("header_subtitle.hostile", this.members.size());
        }
        if (this.viewerMember) {
            return SectScreen.tr("header_subtitle.member", this.viewerRole.displayName(), this.members.size(), this.activeTaskCount());
        }
        return SectScreen.tr("header_subtitle.guest", this.members.size(), this.activeTaskCount());
    }

    private Component infoNotice() {
        if (this.viewerEnemy) {
            return SectScreen.tr("book.notice.hostile", new Object[0]);
        }
        if (this.viewerMember) {
            return SectScreen.tr(this.sameSectImmunity ? "book.notice.member" : "book.notice.member_off", new Object[0]);
        }
        return SectScreen.tr("book.notice.guest", new Object[0]);
    }

    private Component relationStatus() {
        if (this.viewerEnemy) {
            return SectScreen.tr("book.relation.hostile", new Object[0]);
        }
        if (this.viewerMember) {
            return SectScreen.tr("book.relation.member", new Object[0]);
        }
        return SectScreen.tr("book.relation.guest", new Object[0]);
    }

    private int relationColor() {
        if (this.viewerEnemy) {
            return -5750484;
        }
        if (this.viewerMember) {
            return -13668780;
        }
        return -9807288;
    }

    private int headerAccentColor() {
        if (this.viewerEnemy) {
            return -7723482;
        }
        return this.viewerMember ? SectScreen.roleColor(this.viewerRole) : -3562934;
    }

    private int activeTaskCount() {
        int n = 0;
        for (TaskRow task : this.tasks) {
            if (task.completed) continue;
            ++n;
        }
        return n;
    }

    private int readyTaskCount() {
        int n = 0;
        for (TaskRow task : this.tasks) {
            if (!task.canTurnIn) continue;
            ++n;
        }
        return n;
    }

    private Component taskStatus(TaskRow task) {
        if (task.completed) {
            return SectScreen.tr("task.completed", new Object[0]);
        }
        if (this.viewerEnemy) {
            return SectScreen.tr("task.hostile_locked", new Object[0]);
        }
        if (!this.viewerMember) {
            return SectScreen.tr("task.join_first", new Object[0]);
        }
        if (!task.accepted) {
            return task.requiresIssuer ? SectScreen.tr("task.requires_issuer", new Object[0]) : SectScreen.tr("task.can_accept", new Object[0]);
        }
        if (task.ready) {
            if (!task.rewardAvailable && !task.requiresIssuer) {
                return SectScreen.tr("task.reward_unavailable", new Object[0]);
            }
            return task.requiresIssuer ? SectScreen.tr("task.ready_find_issuer", new Object[0]) : SectScreen.tr("task.ready", new Object[0]);
        }
        return SectScreen.tr("task.accepted", new Object[0]);
    }

    private Component taskPhase(TaskRow task) {
        String b = "task.phase.";
        if (task.completed) {
            return SectScreen.tr(b + "completed", new Object[0]);
        }
        if (this.viewerEnemy) {
            return SectScreen.tr(b + "hostile", new Object[0]);
        }
        if (!this.viewerMember) {
            return SectScreen.tr(b + "join_first", new Object[0]);
        }
        if (!task.accepted) {
            return SectScreen.tr(b + "accept", new Object[0]);
        }
        if (task.ready) {
            return SectScreen.tr(b + (task.rewardAvailable ? "report" : "blocked"), new Object[0]);
        }
        return SectScreen.tr(b + "collect", new Object[0]);
    }

    private int taskPhaseColor(TaskRow task) {
        if (task.completed) {
            return -7700107;
        }
        if (this.viewerEnemy) {
            return -7723482;
        }
        if (!this.viewerMember) {
            return -9807288;
        }
        if (!task.accepted) {
            return -3562934;
        }
        if (task.ready && task.rewardAvailable) {
            return -14722744;
        }
        if (task.ready) {
            return -4818904;
        }
        return -4703686;
    }

    private Component taskNextStep(TaskRow task) {
        String b = "task.next.";
        if (task.completed) {
            return SectScreen.tr(b + "completed", new Object[0]);
        }
        if (this.viewerEnemy) {
            return SectScreen.tr(b + "hostile", new Object[0]);
        }
        if (!this.viewerMember) {
            return SectScreen.tr(b + "join_first", new Object[0]);
        }
        MutableComponent issuer = Component.literal((String)task.issuerName);
        if (!task.accepted) {
            return task.canAccept ? SectScreen.tr(b + "accept_here", issuer) : SectScreen.tr(b + "accept_issuer", issuer);
        }
        if (task.ready) {
            if (!task.rewardAvailable) {
                return SectScreen.tr(b + "reward_unavailable", issuer);
            }
            return task.canTurnIn ? SectScreen.tr(b + "turn_in_here", issuer) : SectScreen.tr(b + "turn_in_issuer", issuer);
        }
        if (task.requiredCount > 0) {
            return SectScreen.tr(b + "collect", Math.min(task.heldRequired, task.requiredCount), task.requiredCount);
        }
        return SectScreen.tr(b + "accepted", new Object[0]);
    }

    private static int taskPriority(TaskRow task) {
        if (task.completed) {
            return 7;
        }
        if (task.canTurnIn) {
            return 0;
        }
        if (task.ready && task.rewardAvailable) {
            return 1;
        }
        if (task.canAccept) {
            return 2;
        }
        if (!task.rewardAvailable) {
            return 5;
        }
        if (task.accepted) {
            return 3;
        }
        if (task.requiresIssuer) {
            return 4;
        }
        return 6;
    }

    private int countTasks(Predicate<TaskRow> predicate) {
        int count = 0;
        for (TaskRow task : this.tasks) {
            if (!predicate.test(task)) continue;
            ++count;
        }
        return count;
    }

    private int taskStatusColor(TaskRow task) {
        if (task.completed) {
            return -7700107;
        }
        if (this.viewerEnemy) {
            return -7723482;
        }
        if (!this.viewerMember) {
            return -9807288;
        }
        if (task.ready && task.rewardAvailable) {
            return -14722744;
        }
        if (task.ready) {
            return -4818904;
        }
        if (task.accepted) {
            return -7723482;
        }
        return -3562934;
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

    private static SectRole[] displayRoles() {
        return new SectRole[]{SectRole.ANCESTOR, SectRole.MASTER, SectRole.ELDER, SectRole.INNER_DISCIPLE, SectRole.OUTER_DISCIPLE, SectRole.GUARD_DISCIPLE, SectRole.SERVANT};
    }

    private static int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }

    private static String firstGlyph(String s) {
        return s == null || s.isEmpty() ? "\u5b97" : s.substring(0, 1);
    }

    private static String trimChars(String s, int max) {
        if (s == null || s.isEmpty()) {
            return "\u5b97";
        }
        return s.length() <= max ? s : s.substring(0, max);
    }

    private int toScreenX(int designX) {
        return Math.round((float)this.lastViewport.left + (float)designX * this.lastViewport.scale);
    }

    private int toScreenY(int designY) {
        return Math.round((float)this.lastViewport.top + (float)designY * this.lastViewport.scale);
    }

    private static enum Tab {
        INFO,
        MEMBERS,
        TREE,
        TASKS;

    }

    private static enum ActionKind {
        NONE,
        JOIN,
        IMMUNITY,
        CLOSE,
        TASK_TURN_IN,
        TASK_ACCEPT,
        TASK_TRACK;

    }

    private record SectViewport(int left, int top, float scale) {
        int toLocalX(int screenX) {
            return Math.round((float)(screenX - this.left) / this.scale);
        }

        int toLocalY(int screenY) {
            return Math.round((float)(screenY - this.top) / this.scale);
        }
    }

    private record MemberRow(String id, String name, SectRole role, boolean player, int contribution) {
        private boolean selected(String selectedId) {
            return selectedId != null && selectedId.equals(this.id);
        }
    }

    private record TaskRow(String id, String issuerName, SectRole issuerRole, String titleKey, String conditionKey, int contribution, ItemStack requiredStack, int heldRequired, int requiredCount, ItemStack rewardStack, String rewardKind, boolean accepted, boolean completed, boolean ready, boolean rewardAvailable, boolean requiresIssuer, boolean canAccept, boolean canTurnIn) {
    }

    private record MemberClickRect(int x, int y, int w, int h, MemberRow row) {
        private boolean contains(double mx, double my) {
            return mx >= (double)this.x && mx < (double)(this.x + this.w) && my >= (double)this.y && my < (double)(this.y + this.h);
        }
    }

    private record HoverTipRect(int x, int y, int w, int h, List<Component> lines) {
        private boolean contains(double mx, double my) {
            return mx >= (double)this.x && mx < (double)(this.x + this.w) && my >= (double)this.y && my < (double)(this.y + this.h);
        }
    }

    private record TaskSelectRect(int x, int y, int w, int h, String taskId) {
        private boolean contains(double mx, double my) {
            return mx >= (double)this.x && mx < (double)(this.x + this.w) && my >= (double)this.y && my < (double)(this.y + this.h);
        }
    }

    private record ItemHoverRect(int x, int y, int w, int h, ItemStack stack) {
        private boolean contains(double mx, double my) {
            return mx >= (double)this.x && mx < (double)(this.x + this.w) && my >= (double)this.y && my < (double)(this.y + this.h);
        }
    }

    private record TabClickRect(int x, int y, int w, int h, Tab tab) {
        private boolean contains(double mx, double my) {
            return mx >= (double)this.x && mx < (double)(this.x + this.w) && my >= (double)this.y && my < (double)(this.y + this.h);
        }
    }

    private record ScrollMetrics(int x, int y, int height, int total, int visible) {
        int maxScroll() {
            return Math.max(0, this.total - this.visible);
        }
    }
}

