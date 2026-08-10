package com.friday.cultivation.technique;

import com.friday.cultivation.capability.CultivationData;
import com.friday.cultivation.technique.Technique;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;

import java.util.ArrayList;

/**
 * 功法槽位规范化助手
 * 完全照搬原模组 com.xiaoxiang.cultivation.cultivation.technique.TechniqueLoadoutHelper
 */
public final class TechniqueLoadoutHelper {
    private TechniqueLoadoutHelper() {
    }

    public static boolean canEquipForCurrentState(CultivationData data, Technique technique) {
        if (data == null || technique == null) {
            return false;
        }
        return data.isSoulState() ? technique.isGhostDao() : technique.isHumanDao();
    }

    public static NormalizationResult normalizeForCurrentState(CultivationData data, RandomSource random) {
        if (data == null) {
            return NormalizationResult.unchanged();
        }
        Technique current = Technique.byId((String) data.getEquippedTechniqueId());
        if (current != null && data.getLearnedTechniques().contains(current.id()) && TechniqueLoadoutHelper.canEquipForCurrentState(data, current)) {
            return NormalizationResult.unchanged();
        }
        String before = data.getEquippedTechniqueId();
        Technique replacement = TechniqueLoadoutHelper.chooseHighestTierCompatible(data, random);
        String replacementId = replacement == null ? "" : replacement.id();
        if (replacementId.equals(before)) {
            return NormalizationResult.unchanged();
        }
        data.setEquippedTechniqueId(replacementId);
        return new NormalizationResult(true, replacement);
    }

    public static void notifyNormalization(ServerPlayer player, CultivationData data, NormalizationResult result) {
        if (player == null || data == null || result == null || !result.changed()) {
            return;
        }
        String key;
        if (data.isSoulState()) {
            key = result.equippedReplacement() ? "message.friday_cultivation.technique.auto_equipped_soul" : "message.friday_cultivation.technique.auto_unequipped_soul";
        } else {
            key = result.equippedReplacement() ? "message.friday_cultivation.technique.auto_equipped_living" : "message.friday_cultivation.technique.auto_unequipped_living";
        }
        if (result.equippedReplacement()) {
            player.sendSystemMessage(Component.translatable(key, result.equippedTechnique().displayName()));
        } else {
            player.sendSystemMessage(Component.translatable(key));
        }
    }

    public static boolean equippedTechniqueIsGhostDao(CultivationData data) {
        Technique technique = data == null ? null : Technique.byId((String) data.getEquippedTechniqueId());
        return technique != null && technique.isGhostDao();
    }

    private static Technique chooseHighestTierCompatible(CultivationData data, RandomSource random) {
        ArrayList<Technique> best = new ArrayList<Technique>();
        int bestTier = -1;
        for (String id : data.getLearnedTechniques()) {
            Technique technique = Technique.byId((String) id);
            if (technique == null || !TechniqueLoadoutHelper.canEquipForCurrentState(data, technique)) continue;
            int tier = technique.tier().ordinal();
            if (tier > bestTier) {
                bestTier = tier;
                best.clear();
            }
            if (tier != bestTier) continue;
            best.add(technique);
        }
        if (best.isEmpty()) {
            return null;
        }
        if (best.size() == 1 || random == null) {
            return (Technique) best.get(0);
        }
        return (Technique) best.get(random.nextInt(best.size()));
    }

    public record NormalizationResult(boolean changed, Technique equippedTechnique) {
        public static NormalizationResult unchanged() {
            return new NormalizationResult(false, null);
        }

        public boolean equippedReplacement() {
            return this.changed && this.equippedTechnique != null;
        }
    }
}
