package com.friday.cultivation.verification;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;

/** 阶段 8 的结构回归：空的旧元素/极境接口不得重新成为第二套玩法入口。 */
class StageEightPolicyTest {
    private static final Path SOURCE_ROOT = Path.of("src", "main", "java");

    @Test
    void removedElementApiHasNoRuntimeCallers() throws IOException {
        String data = read("com/friday/cultivation/cultivation/CultivationData.java");
        String fireSword = read("com/friday/cultivation/network/FireSwordAuraPacket.java");
        String charged = read("com/friday/cultivation/event/ChargeableSpellHandler.java");

        assertFalse(data.contains("getElementCount("));
        assertFalse(data.contains("getElementPowerPercent("));
        assertFalse(data.contains("getDominantElement("));
        assertTrue(fireSword.contains("TechniqueBonusHelper.equippedOf(player)"));
        assertTrue(charged.contains("TechniqueBonusHelper.spellElementMultiplier"));
    }

    @Test
    void qiExtremePlaceholderIsNotPersisted() throws IOException {
        String data = read("com/friday/cultivation/cultivation/CultivationData.java");

        assertFalse(data.contains("daoFruitTotalEaten"));
        assertFalse(data.contains("canEnterQiExtreme"));
        assertFalse(data.contains("advanceToQiExtreme"));
    }

    @Test
    void tribulationWeightsHaveOneSource() throws IOException {
        String constants = read("com/friday/cultivation/event/tribulation/TribulationConstants.java");
        String helper = read("com/friday/cultivation/event/tribulation/TribulationScalingHelper.java");

        assertTrue(constants.contains("SPIRIT_ROOT_WEIGHT = 1.2"));
        assertFalse(helper.contains("private static final double SPIRIT_ROOT_WEIGHT"));
        assertFalse(helper.contains("private static final double PHYSIQUE_WEIGHT"));
        assertFalse(helper.contains("private static final double TECHNIQUE_WEIGHT"));
    }

    @Test
    void networkAdaptersDoNotImportClientClasses() throws IOException {
        Path networkRoot = SOURCE_ROOT.resolve("com/friday/cultivation/network");
        try (Stream<Path> files = Files.walk(networkRoot)) {
            files.filter(path -> path.toString().endsWith(".java")).forEach(path -> {
                try {
                    String source = Files.readString(path, StandardCharsets.UTF_8);
                    assertFalse(source.lines().anyMatch(line -> line.startsWith("import net.minecraft.client")),
                            () -> "client import in network adapter: " + path);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });
        }
    }

    private static String read(String relativePath) throws IOException {
        return Files.readString(SOURCE_ROOT.resolve(relativePath), StandardCharsets.UTF_8);
    }
}
