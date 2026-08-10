/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.network.chat.Component
 *  net.minecraft.server.level.ServerPlayer
 *  net.minecraft.util.RandomSource
 */
package com.friday.cultivation.cultivation.technique;

import com.friday.cultivation.cultivation.CultivationData;
import com.friday.cultivation.cultivation.technique.Technique;
import java.util.ArrayList;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;

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
        String replacementId;
        if (data == null) {
            return NormalizationResult.unchanged();
        }
        Technique current = Technique.byId(data.getEquippedTechniqueId());
        if (current != null && data.getLearnedTechniques().contains(current.id()) && TechniqueLoadoutHelper.canEquipForCurrentState(data, current)) {
            return NormalizationResult.unchanged();
        }
        String before = data.getEquippedTechniqueId();
        Technique replacement = TechniqueLoadoutHelper.chooseHighestTierCompatible(data, random);
        String string = replacementId = replacement == null ? "" : replacement.id();
        if (replacementId.equals(before)) {
            return NormalizationResult.unchanged();
        }
        data.setEquippedTechniqueId(replacementId);
        return new NormalizationResult(true, replacement);
    }

    public static void notifyNormalization(ServerPlayer player, CultivationData data, NormalizationResult result) {
        String key;
        if (player == null || data == null || result == null || !result.changed()) {
            return;
        }
        if (data.isSoulState()) {
            key = result.equippedReplacement() ? "message.friday_cultivation.technique.auto_equipped_soul" : "message.friday_cultivation.technique.auto_unequipped_soul";
        } else {
            String string = key = result.equippedReplacement() ? "message.friday_cultivation.technique.auto_equipped_living" : "message.friday_cultivation.technique.auto_unequipped_living";
        }
        if (result.equippedReplacement()) {
            player.sendSystemMessage((Component)Component.translatable((String)key, (Object[])new Object[]{result.equippedTechnique().displayName()}));
        } else {
            player.sendSystemMessage((Component)Component.translatable((String)key));
        }
    }

    public static boolean equippedTechniqueIsGhostDao(CultivationData data) {
        Technique technique = data == null ? null : Technique.byId(data.getEquippedTechniqueId());
        return technique != null && technique.isGhostDao();
    }

    private static Technique chooseHighestTierCompatible(CultivationData data, RandomSource random) {
        ArrayList<Technique> best = new ArrayList<Technique>();
        int bestTier = -1;
        for (String id : data.getLearnedTechniques()) {
            Technique technique = Technique.byId(id);
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
            return (Technique)((Object)best.get(0));
        }
        return (Technique)((Object)best.get(random.nextInt(best.size())));
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

