package com.friday.cultivation.verification;

import static org.junit.jupiter.api.Assertions.assertEquals;
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
        assertTrue(hud.contains("renderExperienceBar(graphics, screenWidth, screenHeight)"));
        assertTrue(hud.contains("BLOOD_EMPTY, BLOOD_FILL"));

        String entityHud = read("com/friday/cultivation/client/EntityStatusHudRenderer.java");
        assertTrue(entityHud.contains("HEAD_ANCHOR_OFFSET"));
        assertTrue(entityHud.contains("SCREEN_BAR_OFFSET_Y"));
        assertTrue(entityHud.contains("healthBarAnchor(living, partial)"));
        assertTrue(entityHud.contains("HURT_SHOW_TICKS"));
        assertTrue(entityHud.contains("LAST_HURT"));
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
