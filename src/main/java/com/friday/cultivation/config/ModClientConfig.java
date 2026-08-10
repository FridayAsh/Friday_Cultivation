/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraftforge.common.ForgeConfigSpec
 *  net.minecraftforge.common.ForgeConfigSpec$Builder
 *  net.minecraftforge.common.ForgeConfigSpec$EnumValue
 */
package com.friday.cultivation.config;

import net.minecraftforge.common.ForgeConfigSpec;

public final class ModClientConfig {
    public static final ForgeConfigSpec SPEC;
    public static final ForgeConfigSpec.EnumValue<HudPosition> HUD_POSITION;

    private ModClientConfig() {
    }

    public static HudPosition hudPosition() {
        return (HudPosition)((Object)HUD_POSITION.get());
    }

    public static void setHudPosition(HudPosition value) {
        HUD_POSITION.set(value);
        HUD_POSITION.save();
    }

    static {
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();
        builder.push("hud");
        HUD_POSITION = builder.comment(new String[]{"Where the compact cultivation HUD is anchored on the client screen.", "TOP_LEFT keeps the original layout. TOP_RIGHT moves the same HUD to the upper-right corner."}).translation("config.friday_cultivation.hud_position").defineEnum("hudPosition", (Enum)HudPosition.TOP_LEFT);
        builder.pop();
        SPEC = builder.build();
    }

    public static enum HudPosition {
        TOP_LEFT,
        TOP_RIGHT;


        public HudPosition next() {
            return this == TOP_LEFT ? TOP_RIGHT : TOP_LEFT;
        }

        public boolean isRightAligned() {
            return this == TOP_RIGHT;
        }
    }
}

