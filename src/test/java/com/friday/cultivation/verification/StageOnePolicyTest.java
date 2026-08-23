package com.friday.cultivation.verification;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

/** 阶段 1 的结构回归测试：通过公开 Seam 约束止血规则，避免旧写法重新出现。 */
class StageOnePolicyTest {
    private static final Path SOURCE_ROOT = Path.of("src", "main", "java");

    @Test
    void flightEventsHaveOneRegistrationMechanism() throws IOException {
        String events = read("com/friday/cultivation/flight/CultivationFlightEvents.java");
        String mod = read("com/friday/cultivation/FridayCultivationMod.java");

        assertEquals(1, count(events, "@Mod.EventBusSubscriber"));
        assertEquals(0, count(mod, "CultivationFlightEvents.class"));
    }

    @Test
    void developmentPacketsCrossServerAuthorizationSeam() throws IOException {
        String realm = read("com/friday/cultivation/network/RealmSelectionPacket.java");
        String stats = read("com/friday/cultivation/network/EditPlayerStatsPacket.java");

        assertTrue(realm.contains("ServerAuthorization.canSelectRealm(player)"));
        assertTrue(stats.contains("ServerAuthorization.canEditPlayerStats(player)"));
    }

    @Test
    void loginRestoresFlightFromCapabilityInsteadOfClearingIt() throws IOException {
        String capabilityEvents = read("com/friday/cultivation/event/CapabilityEvents.java");
        String flight = read("com/friday/cultivation/flight/CultivationFlightHandler.java");

        assertTrue(capabilityEvents.contains("CultivationFlightHandler.restoreAfterLogin(player2)"));
        assertTrue(!capabilityEvents.contains("data.clearSwordFlight()"));
        assertTrue(flight.contains("cd.getSwordFlightStack().copy()"));
    }

    @Test
    void flightHandlerHasNoStaticPlayerAuthorityMaps() throws IOException {
        String flight = read("com/friday/cultivation/flight/CultivationFlightHandler.java");

        assertTrue(!flight.contains("SWORD_FLIGHT ="));
        assertTrue(!flight.contains("SWORD_FLIGHT_SLOT ="));
        assertTrue(!flight.contains("FLIGHT_TICKS ="));
        assertTrue(flight.contains("data.incrementFlightTicks()"));
    }

    @Test
    void daoRoutePreviewAndStartMessageUseTheSameTianjiaoScaledSpecAsRuntime() throws IOException {
        String screen = read("com/friday/cultivation/client/screen/CultivationScreen.java");
        String hint = between(screen,
                "private Component breakthroughHint",
                "private void drawBreakthroughCentered");
        String foundationRoute = between(hint,
                "if (realm == Realm.QI_REFINING",
                "if (realm == Realm.FOUNDATION_BUILDING");
        String goldenCoreRoute = between(hint,
                "if (realm == Realm.FOUNDATION_BUILDING",
                "int strikes = realm.tribulationCount");

        assertTrue(foundationRoute.contains("TribulationScalingHelper.scaleSpec("),
                "筑基道预览必须读取天骄缩放后的最终劫谱");
        assertTrue(goldenCoreRoute.contains("TribulationScalingHelper.scaleSpec("),
                "金丹道预览必须读取天骄缩放后的最终劫谱");

        String packet = read("com/friday/cultivation/network/RequestBreakthroughPacket.java");
        assertTrue(packet.contains("TribulationSpec finalSpec = TribulationScalingHelper.scaleSpec("),
                "服务端必须为路线突破建立唯一的天骄缩放最终劫谱");
        assertTrue(packet.contains("Realm.formatTribulationCount(finalSpec.waves(), finalSpec.boltsPerWave())"),
                "开始提示必须读取最终劫谱雷数，不能继续显示缩放前基础值");
        assertTrue(packet.contains("finalSpec.strikeDamage()"),
                "开始提示必须读取最终劫谱伤害，不能继续显示缩放前基础值");
    }

    @Test
    void hudUsesProjectExperienceTextureAndStableEntityHealthAnchor() throws IOException {
        String hud = read("com/friday/cultivation/client/CultivationHud.java");
        assertTrue(hud.contains("VanillaGuiOverlay.EXPERIENCE_BAR.type()"));
        assertTrue(hud.contains("renderExperienceBar(graphics, screenWidth, screenHeight, nowMillis)"));
        assertTrue(hud.contains("BLOOD_EMPTY, BLOOD_FILL"));

        String entityHud = read("com/friday/cultivation/client/EntityStatusHudRenderer.java");
        String entityLayout = read("com/friday/cultivation/client/EntityStatusPlateLayout.java");
        assertTrue(entityHud.contains("HEAD_ANCHOR_OFFSET"));
        assertTrue(entityHud.contains("RenderNameTagEvent"));
        assertTrue(entityHud.contains("healthBarAnchor(living, event.getPartialTick())"));
        assertTrue(entityHud.contains("HURT_SHOW_TICKS"));
        assertTrue(entityHud.contains("HEALTH_TRACKS"));
        assertTrue(entityHud.contains("RenderSystem.getProjectionMatrix()"),
                "状态牌必须从实体渲染阶段捕获当前真实投影矩阵，不能恢复手算 FOV 投影");
        assertTrue(entityHud.contains("RenderGuiEvent.Post"),
                "最终像素必须在光影合成后的 HUD 总阶段绘制，才能彻底隔离昼夜、逆光和自动曝光");
        assertTrue(entityHud.contains("GameRenderer::getPositionTexColorShader"),
                "状态牌贴图必须使用最终 GUI 颜色纹理着色器，不能再进入 iterationT 的延迟光照材质链");
        assertFalse(entityHud.contains("RenderType.text(texture)"),
                "RenderType.text 会被 iterationT 识别为 particle-lit 并随昼夜及光照方向改变颜色");
        assertTrue(entityLayout.contains("ATTRIBUTE_ICON_SIZE_PIXELS = 9.0F"),
                "护甲与韧性图标必须按原生 9×9 纹理显示，不能继续拉伸到 13×13");
        assertTrue(entityLayout.contains("ATTRIBUTE_TEXT_SCALE = 0.72F"),
                "护甲与韧性数字必须放大到清晰可读的 0.72 倍并垂直居中");
        assertTrue(entityHud.contains("TOUGH_COLOR = 0xFF55FFFF"),
                "韧性数字必须使用高对比亮青色");
        assertTrue(entityHud.contains("drawOutlinedText("),
                "护甲与韧性数字必须增加深色描边，保证亮天空和夜晚均清楚");
        assertTrue(entityHud.contains("Tags.EntityTypes.BOSSES"),
                "Boss 分类必须优先使用 Forge 通用实体标签以兼容其他模组");
    }

    @Test
    void entityArmorTextUsesPlayerHudColor() throws IOException {
        String entityHud = read("com/friday/cultivation/client/EntityStatusHudRenderer.java");
        assertTrue(entityHud.contains("CultivationHud.ARMOR_COLOR"),
                "生物护甲数字必须直接复用玩家 HUD 的护甲颜色，禁止维护第二套颜色常量");
        assertFalse(entityHud.contains("private static final int ARMOR_COLOR"),
                "生物状态 HUD 不能继续声明独立护甲颜色");
    }

    @Test
    void projectBossBarAlwaysReplacesExternalBossBars() throws IOException {
        String entityHud = read("com/friday/cultivation/client/EntityStatusHudRenderer.java");
        assertTrue(entityHud.contains("CustomizeGuiOverlayEvent.BossEventProgress"),
                "必须监听 Forge 标准 Boss 条事件");
        assertTrue(entityHud.contains("@SubscribeEvent(priority = EventPriority.HIGHEST)"),
                "必须先于其他普通监听器接管 Boss 条事件");
        assertTrue(entityHud.contains("event.setCanceled(true)"),
                "必须强制取消原版及其他模组通过标准事件绘制的 Boss 条");
        assertFalse(entityHud.contains("EXISTING_BOSS_BARS"),
                "强制统一后不能再保留外部 Boss 条快照或为其预留位置");
        assertFalse(entityHud.contains("hasCorrespondingBossBar("),
                "不能再因检测到外部 Boss 条而跳过项目 Boss 条");
        assertTrue(entityHud.contains("renderProjectBossBar("),
                "所有 Boss 必须统一走项目自己的屏幕顶部血条");
    }

    @Test
    void projectBossBarRendersArmorAndToughness() throws IOException {
        String entityHud = read("com/friday/cultivation/client/EntityStatusHudRenderer.java");
        assertTrue(entityHud.contains("renderBossAttributes("),
                "项目 Boss 血条必须绘制护甲与韧性组件");
        assertTrue(entityHud.contains("target.armor()") && entityHud.contains("target.toughness()"),
                "Boss 快照必须携带并读取实时护甲与韧性数值");
    }

    private static String read(String relativePath) throws IOException {
        return Files.readString(SOURCE_ROOT.resolve(relativePath), StandardCharsets.UTF_8);
    }

    private static String between(String text, String start, String end) {
        int from = text.indexOf(start);
        int to = from < 0 ? -1 : text.indexOf(end, from + start.length());
        assertTrue(from >= 0 && to > from,
                () -> "找不到源码片段：" + start + " ... " + end);
        return text.substring(from, to);
    }

    private static int count(String text, String needle) {
        int count = 0;
        int from = 0;
        while ((from = text.indexOf(needle, from)) >= 0) {
            count++;
            from += needle.length();
        }
        return count;
    }
}
