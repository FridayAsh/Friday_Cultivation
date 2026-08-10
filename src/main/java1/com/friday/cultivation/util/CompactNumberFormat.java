package com.friday.cultivation.util;

import net.minecraft.client.Minecraft;

/**
 * 紧凑数字格式化（严格照搬原模组 com.xiaoxiang.cultivation.util.CompactNumberFormat）
 * 中文模式：兆/亿/万/千；西文模式：T/B/M/K
 */
public final class CompactNumberFormat {
    private CompactNumberFormat() {}

    public static String format(long n) {
        return format(n, currentLanguageCode());
    }

    public static String format(long n, String langCode) {
        if (n < 1000L) {
            return Long.toString(n);
        }
        boolean zh = langCode != null && langCode.startsWith("zh");
        return zh ? formatChinese(n, "zh_cn".equalsIgnoreCase(langCode)) : formatWestern(n);
    }

    private static String formatChinese(long n, boolean simplified) {
        if (n >= 1000000000000L) {
            return formatNumber((double)n / 1.0E12) + "\u5146";
        }
        if (n >= 100000000L) {
            return formatNumber((double)n / 1.0E8) + (simplified ? "\u4ebf" : "\u5104");
        }
        if (n >= 10000L) {
            return formatNumber((double)n / 10000.0) + (simplified ? "\u4e07" : "\u842c");
        }
        return formatNumber((double)n / 1000.0) + "\u5343";
    }

    private static String formatWestern(long n) {
        if (n >= 1000000000000L) {
            return formatNumber((double)n / 1.0E12) + "T";
        }
        if (n >= 1000000000L) {
            return formatNumber((double)n / 1.0E9) + "B";
        }
        if (n >= 1000000L) {
            return formatNumber((double)n / 1000000.0) + "M";
        }
        return formatNumber((double)n / 1000.0) + "K";
    }

    private static String formatNumber(double d) {
        String s = d >= 100.0 ? String.format("%.0f", d)
                : (d >= 10.0 ? String.format("%.1f", d) : String.format("%.2f", d));
        if (s.contains(".") && (s = s.replaceAll("0+$", "")).endsWith(".")) {
            s = s.substring(0, s.length() - 1);
        }
        return s;
    }

    private static String currentLanguageCode() {
        try {
            // 1.20.1: getSelected() 返回 Language 对象，通过 toString() 获取语言代码
            Object selected = Minecraft.getInstance().getLanguageManager().getSelected();
            String code = selected == null ? "en_us" : selected.toString();
            // 提取形如 "en_us" / "zh_cn" 的语言代码
            int parenIdx = code.indexOf('(');
            if (parenIdx > 0) {
                int endIdx = code.indexOf(')', parenIdx);
                if (endIdx > parenIdx) {
                    code = code.substring(parenIdx + 1, endIdx);
                }
            }
            return code.toLowerCase();
        } catch (Throwable ignored) {
            return "en_us";
        }
    }
}