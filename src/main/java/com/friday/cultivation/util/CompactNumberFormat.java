/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.Minecraft
 */
package com.friday.cultivation.util;

import com.friday.cultivation.client.ClientLanguageHooks;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;

public final class CompactNumberFormat {
    private CompactNumberFormat() {
    }

    public static String format(long n) {
        return CompactNumberFormat.format(n, CompactNumberFormat.currentLanguageCode());
    }

    public static String format(long n, String langCode) {
        if (n < 1000L) {
            return Long.toString(n);
        }
        boolean zh = langCode != null && langCode.startsWith("zh");
        return zh ? CompactNumberFormat.formatChinese(n, "zh_cn".equalsIgnoreCase(langCode)) : CompactNumberFormat.formatWestern(n);
    }

    private static String formatChinese(long n, boolean simplified) {
        if (n >= 1000000000000L) {
            return CompactNumberFormat.formatNumber((double)n / 1.0E12) + "\u5146";
        }
        if (n >= 100000000L) {
            return CompactNumberFormat.formatNumber((double)n / 1.0E8) + (simplified ? "\u4ebf" : "\u5104");
        }
        if (n >= 10000L) {
            return CompactNumberFormat.formatNumber((double)n / 10000.0) + (simplified ? "\u4e07" : "\u842c");
        }
        return CompactNumberFormat.formatNumber((double)n / 1000.0) + "\u5343";
    }

    private static String formatWestern(long n) {
        if (n >= 1000000000000L) {
            return CompactNumberFormat.formatNumber((double)n / 1.0E12) + "T";
        }
        if (n >= 1000000000L) {
            return CompactNumberFormat.formatNumber((double)n / 1.0E9) + "B";
        }
        if (n >= 1000000L) {
            return CompactNumberFormat.formatNumber((double)n / 1000000.0) + "M";
        }
        return CompactNumberFormat.formatNumber((double)n / 1000.0) + "K";
    }

    private static String formatNumber(double d) {
        String s = d >= 100.0 ? String.format("%.0f", d) : (d >= 10.0 ? String.format("%.1f", d) : String.format("%.2f", d));
        if (s.contains(".") && (s = s.replaceAll("0+$", "")).endsWith(".")) {
            s = s.substring(0, s.length() - 1);
        }
        return s;
    }

    private static String currentLanguageCode() {
        try {
            final String[] language = new String[]{"en_us"};
            DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> language[0] = ClientLanguageHooks.selectedLanguage());
            return language[0];
        }
        catch (Throwable ignored) {
            return "en_us";
        }
    }
}
