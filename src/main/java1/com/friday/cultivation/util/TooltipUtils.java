package com.friday.cultivation.util;

import com.friday.cultivation.ItemTier;
import com.friday.cultivation.spirit.QiElement;
import com.friday.cultivation.spell.SpellElement;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;

import java.util.List;

/**
 * Tooltip 工具类（照搬原模组 TooltipUtils）
 */
public class TooltipUtils {

    public static final Style IMMORTAL_RED_GOLD = Style.EMPTY.withColor(TextColor.fromRgb(16726559)).withBold(true);
    private static final String INDENT = "  ";
    private static final String WARN_MARK = "  ! ";

    private TooltipUtils() {
    }

    public static ChatFormatting tierFormatting(ItemTier tier) {
        return switch (tier) {
            case LOW -> ChatFormatting.GRAY;
            case MID -> ChatFormatting.GREEN;
            case HIGH -> ChatFormatting.AQUA;
            case SUPREME -> ChatFormatting.GOLD;
            case IMMORTAL -> ChatFormatting.RED;
        };
    }

    /** 兼容别名（项目既有调用） */
    public static ChatFormatting tierColor(ItemTier tier) {
        return tierFormatting(tier);
    }

    public static MutableComponent tieredName(Component baseName, ItemTier tier) {
        if (tier == ItemTier.IMMORTAL) {
            return Component.empty().append(baseName.copy().withStyle(IMMORTAL_RED_GOLD));
        }
        return Component.empty().append(baseName.copy().withStyle(tierFormatting(tier)));
    }

    public static int elementColor(QiElement element) {
        return switch (element) {
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
        MutableComponent badge = Component.literal("[").withStyle(ChatFormatting.DARK_GRAY)
                .append(tier.displayName().copy().withStyle(tierFormatting(tier)))
                .append(Component.literal("]").withStyle(ChatFormatting.DARK_GRAY));
        if (tier == ItemTier.IMMORTAL) {
            badge = Component.literal("[").withStyle(ChatFormatting.DARK_GRAY)
                    .append(tier.displayName().copy().withStyle(IMMORTAL_RED_GOLD))
                    .append(Component.literal("]").withStyle(ChatFormatting.DARK_GRAY));
        }
        return badge;
    }

    public static MutableComponent elementBadge(QiElement element) {
        return Component.literal("[").withStyle(ChatFormatting.DARK_GRAY)
                .append(element.displayName().copy().withStyle(Style.EMPTY.withColor(TextColor.fromRgb(elementColor(element)))))
                .append(Component.literal("]").withStyle(ChatFormatting.DARK_GRAY));
    }

    public static MutableComponent elementBadge(SpellElement element) {
        return Component.literal("[").withStyle(ChatFormatting.DARK_GRAY)
                .append(element.displayName().copy().withStyle(Style.EMPTY.withColor(TextColor.fromRgb(elementColor(element)))))
                .append(Component.literal("]").withStyle(ChatFormatting.DARK_GRAY));
    }

    public static MutableComponent tierElementLine(ItemTier tier, QiElement element) {
        return Component.empty().append(tierBadge(tier)).append(Component.literal(INDENT)).append(elementBadge(element));
    }

    public static MutableComponent tierElementLine(ItemTier tier, SpellElement element) {
        return Component.empty().append(tierBadge(tier)).append(Component.literal(INDENT)).append(elementBadge(element));
    }

    public static MutableComponent section(Component label) {
        return label.copy().withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD);
    }

    public static MutableComponent section(String translationKey) {
        return section(Component.translatable(translationKey));
    }

    public static void addSection(List<Component> tooltip, Component label) {
        tooltip.add(section(label));
    }

    public static void addSection(List<Component> tooltip, String translationKey) {
        tooltip.add(section(translationKey));
    }

    public static MutableComponent descriptionLine(Component text) {
        return Component.literal(INDENT).withStyle(ChatFormatting.DARK_GRAY).append(text.copy().withStyle(ChatFormatting.GRAY));
    }

    public static MutableComponent bulletLine(Component text) {
        return descriptionLine(text);
    }

    public static MutableComponent hintLine(Component hint) {
        return Component.literal(INDENT).withStyle(ChatFormatting.DARK_GRAY).append(hint.copy().withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC));
    }

    public static MutableComponent statsLine(Component stats) {
        return Component.literal(INDENT).withStyle(ChatFormatting.AQUA).append(stats.copy().withStyle(ChatFormatting.AQUA));
    }

    public static MutableComponent effectLine(Component effect) {
        return Component.literal(INDENT).withStyle(ChatFormatting.LIGHT_PURPLE).append(effect.copy().withStyle(ChatFormatting.LIGHT_PURPLE));
    }

    public static MutableComponent positiveLine(Component effect) {
        return Component.literal(INDENT).withStyle(ChatFormatting.GREEN).append(effect.copy().withStyle(ChatFormatting.GREEN));
    }

    public static MutableComponent costLine(Component cost) {
        return Component.literal(INDENT).withStyle(ChatFormatting.RED).append(cost.copy().withStyle(ChatFormatting.RED));
    }

    public static MutableComponent warningLine(Component warning) {
        return Component.literal(WARN_MARK).withStyle(ChatFormatting.RED, ChatFormatting.BOLD).append(warning.copy().withStyle(ChatFormatting.RED));
    }

    public static MutableComponent blankLine() {
        return Component.empty();
    }

    public static void addBlank(List<Component> tooltip) {
        tooltip.add(blankLine());
    }

    public static MutableComponent decorTop() {
        return Component.empty();
    }

    public static MutableComponent decorBot() {
        return Component.empty();
    }

    public static void appendStandardTooltip(List<Component> tooltip, ItemTier tier, QiElement element, Component description, Component hint) {
        tooltip.add(tierElementLine(tier, element));
        tooltip.add(descriptionLine(description));
        if (hint != null) {
            tooltip.add(hintLine(hint));
        }
    }

    public static void appendStandardTooltip(List<Component> tooltip, ItemTier tier, SpellElement element, Component description, Component hint) {
        tooltip.add(tierElementLine(tier, element));
        tooltip.add(descriptionLine(description));
        if (hint != null) {
            tooltip.add(hintLine(hint));
        }
    }
}
