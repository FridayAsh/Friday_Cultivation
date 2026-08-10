package com.friday.cultivation.config;

import net.minecraftforge.common.ForgeConfigSpec;

/**
 * 客户端配置（严格照搬原模组 com.xiaoxiang.cultivation.config.ModClientConfig）
 * 控制HUD位置等客户端设置
 */
public final class ModClientConfig {
    public static final ForgeConfigSpec SPEC;
    public static final ForgeConfigSpec.EnumValue<HudPosition> HUD_POSITION;

    private ModClientConfig() {}

    public static HudPosition hudPosition() {
        return HUD_POSITION.get();
    }

    public static void setHudPosition(HudPosition value) {
        HUD_POSITION.set(value);
        HUD_POSITION.save();
    }

    static {
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();
        builder.push("hud");
        HUD_POSITION = builder.comment(
                "Where the compact cultivation HUD is anchored on the client screen.",
                "TOP_LEFT keeps the original layout. TOP_RIGHT moves the same HUD to the upper-right corner."
        ).translation("config.friday_cultivation.hud_position")
                .defineEnum("hudPosition", HudPosition.TOP_LEFT);
        builder.pop();
        SPEC = builder.build();
    }

    public enum HudPosition {
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