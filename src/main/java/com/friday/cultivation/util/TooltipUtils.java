/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.ChatFormatting
 *  net.minecraft.network.chat.Component
 *  net.minecraft.network.chat.MutableComponent
 *  net.minecraft.network.chat.Style
 *  net.minecraft.network.chat.TextColor
 */
package com.friday.cultivation.util;

import com.friday.cultivation.cultivation.ItemTier;
import com.friday.cultivation.cultivation.QiElement;
import com.friday.cultivation.cultivation.spell.SpellElement;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;

public final class TooltipUtils {
    public static final int IMMORTAL_RED_GOLD_RGB = 16726559;
    public static final Style IMMORTAL_RED_GOLD = Style.EMPTY.withColor(TextColor.fromRgb((int)16726559)).withBold(Boolean.valueOf(true));
    private static final String INDENT = "  ";
    private static final String WARN_MARK = "  ! ";

    private TooltipUtils() {
    }

    public static ChatFormatting tierFormatting(ItemTier tier) {
        return switch (tier) {
            default -> throw new IncompatibleClassChangeError();
            case LOW -> ChatFormatting.GRAY;
            case MID -> ChatFormatting.GREEN;
            case HIGH -> ChatFormatting.AQUA;
            case SUPREME -> ChatFormatting.GOLD;
            case IMMORTAL -> ChatFormatting.RED;
        };
    }

    public static MutableComponent tieredName(Component baseName, ItemTier tier) {
        if (tier == ItemTier.IMMORTAL) {
            return Component.empty().append((Component)baseName.copy().withStyle(IMMORTAL_RED_GOLD));
        }
        return Component.empty().append((Component)baseName.copy().withStyle(TooltipUtils.tierFormatting(tier)));
    }

    public static int elementColor(QiElement element) {
        return switch (element) {
            default -> throw new IncompatibleClassChangeError();
            case METAL -> 16115365;
            case WOOD -> 6151795;
            case WATER -> 5093375;
            case FIRE -> 16734764;
            case EARTH -> 12622424;
            case PURE -> 0xBEEBFF;
            case ICE -> 12116223;
            case LIGHTNING -> 14738943;
        };
    }

    public static int elementColor(SpellElement element) {
        return switch (element) {
            default -> throw new IncompatibleClassChangeError();
            case METAL -> 16115365;
            case WOOD -> 6151795;
            case WATER -> 5093375;
            case ICE -> 12116223;
            case FIRE -> 16734764;
            case EARTH -> 12622424;
            case WOOD_FIRE -> 4780240;
            case LIGHTNING -> 0xEEE4FF;
            case NONE -> 0xE8E8E2;
        };
    }

    public static MutableComponent tierBadge(ItemTier tier) {
        MutableComponent badge = Component.literal((String)"[").append((Component)tier.displayName().copy().withStyle(TooltipUtils.tierFormatting(tier))).append((Component)Component.literal((String)"]").withStyle(ChatFormatting.DARK_GRAY));
        if (tier == ItemTier.IMMORTAL) {
            badge = Component.literal((String)"[").append((Component)tier.displayName().copy().withStyle(IMMORTAL_RED_GOLD)).append((Component)Component.literal((String)"]").withStyle(ChatFormatting.DARK_GRAY));
        }
        return badge;
    }

    public static MutableComponent elementBadge(QiElement element) {
        return Component.literal((String)"[").withStyle(ChatFormatting.DARK_GRAY).append((Component)element.displayName().copy().withStyle(Style.EMPTY.withColor(TextColor.fromRgb((int)TooltipUtils.elementColor(element))))).append((Component)Component.literal((String)"]").withStyle(ChatFormatting.DARK_GRAY));
    }

    public static MutableComponent elementBadge(SpellElement element) {
        return Component.literal((String)"[").withStyle(ChatFormatting.DARK_GRAY).append((Component)element.displayName().copy().withStyle(Style.EMPTY.withColor(TextColor.fromRgb((int)TooltipUtils.elementColor(element))))).append((Component)Component.literal((String)"]").withStyle(ChatFormatting.DARK_GRAY));
    }

    public static MutableComponent tierElementLine(ItemTier tier, QiElement element) {
        return Component.empty().append((Component)TooltipUtils.tierBadge(tier)).append((Component)Component.literal((String)INDENT)).append((Component)TooltipUtils.elementBadge(element));
    }

    public static MutableComponent tierElementLine(ItemTier tier, SpellElement element) {
        return Component.empty().append((Component)TooltipUtils.tierBadge(tier)).append((Component)Component.literal((String)INDENT)).append((Component)TooltipUtils.elementBadge(element));
    }

    public static MutableComponent section(Component label) {
        return label.copy().withStyle(new ChatFormatting[]{ChatFormatting.GOLD, ChatFormatting.BOLD});
    }

    public static MutableComponent section(String translationKey) {
        return TooltipUtils.section((Component)Component.translatable((String)translationKey));
    }

    public static void addSection(List<Component> tooltip, Component label) {
        tooltip.add((Component)TooltipUtils.section(label));
    }

    public static void addSection(List<Component> tooltip, String translationKey) {
        tooltip.add((Component)TooltipUtils.section(translationKey));
    }

    public static MutableComponent descriptionLine(Component text) {
        return Component.literal((String)INDENT).withStyle(ChatFormatting.DARK_GRAY).append((Component)text.copy().withStyle(ChatFormatting.GRAY));
    }

    public static MutableComponent bulletLine(Component text) {
        return TooltipUtils.descriptionLine(text);
    }

    public static MutableComponent hintLine(Component hint) {
        return Component.literal((String)INDENT).withStyle(ChatFormatting.DARK_GRAY).append((Component)hint.copy().withStyle(new ChatFormatting[]{ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC}));
    }

    public static MutableComponent statsLine(Component stats) {
        return Component.literal((String)INDENT).withStyle(ChatFormatting.AQUA).append((Component)stats.copy().withStyle(ChatFormatting.AQUA));
    }

    public static MutableComponent effectLine(Component effect) {
        return Component.literal((String)INDENT).withStyle(ChatFormatting.LIGHT_PURPLE).append((Component)effect.copy().withStyle(ChatFormatting.LIGHT_PURPLE));
    }

    public static MutableComponent positiveLine(Component effect) {
        return Component.literal((String)INDENT).withStyle(ChatFormatting.GREEN).append((Component)effect.copy().withStyle(ChatFormatting.GREEN));
    }

    public static MutableComponent costLine(Component cost) {
        return Component.literal((String)INDENT).withStyle(ChatFormatting.RED).append((Component)cost.copy().withStyle(ChatFormatting.RED));
    }

    public static MutableComponent warningLine(Component warning) {
        return Component.literal((String)WARN_MARK).withStyle(new ChatFormatting[]{ChatFormatting.RED, ChatFormatting.BOLD}).append((Component)warning.copy().withStyle(ChatFormatting.RED));
    }

    public static MutableComponent blankLine() {
        return Component.empty();
    }

    public static void addBlank(List<Component> tooltip) {
        tooltip.add((Component)TooltipUtils.blankLine());
    }

    public static MutableComponent decorTop() {
        return Component.empty();
    }

    public static MutableComponent decorBot() {
        return Component.empty();
    }

    public static void appendStandardTooltip(List<Component> tooltip, ItemTier tier, QiElement element, Component description, Component hint) {
        tooltip.add((Component)TooltipUtils.tierElementLine(tier, element));
        tooltip.add((Component)TooltipUtils.descriptionLine(description));
        if (hint != null) {
            tooltip.add((Component)TooltipUtils.hintLine(hint));
        }
    }

    public static void appendStandardTooltip(List<Component> tooltip, ItemTier tier, SpellElement element, Component description, Component hint) {
        tooltip.add((Component)TooltipUtils.tierElementLine(tier, element));
        tooltip.add((Component)TooltipUtils.descriptionLine(description));
        if (hint != null) {
            tooltip.add((Component)TooltipUtils.hintLine(hint));
        }
    }
}

