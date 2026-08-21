package com.friday.cultivation.cultivation;

import com.friday.cultivation.cultivation.realm.Realm;
import com.friday.cultivation.cultivation.realm.RealmTopology;
import com.friday.cultivation.cultivation.realm.SubStage;
import net.minecraft.nbt.CompoundTag;

/**
 * Fixed, per-breakthrough reward snapshot.  Values are captured before the
 * transition and never recomputed from the new realm or current attributes.
 */
public record TribulationBonusSnapshot(
        String rewardKey,
        String targetRealmId,
        String targetSubStageId,
        double sourcePercent,
        double healthBonus,
        long maxQiBonus,
        int constitutionBonus,
        int physiqueBonus,
        int agilityBonus,
        int spellPowerBonus,
        int qiSeaBonus
) {
    public static final int CURRENT_VERSION = 2;

    public TribulationBonusSnapshot {
        rewardKey = rewardKey == null || rewardKey.isBlank() ? "legacy:unknown" : rewardKey;
        targetRealmId = targetRealmId == null || targetRealmId.isBlank() ? Realm.MORTAL.id() : targetRealmId;
        targetSubStageId = normalizeTargetSubStageId(rewardKey, targetRealmId, targetSubStageId);
        sourcePercent = Math.max(0.0, sourcePercent);
        healthBonus = Math.max(0.0, healthBonus);
        maxQiBonus = Math.max(0L, maxQiBonus);
        constitutionBonus = Math.max(0, constitutionBonus);
        physiqueBonus = Math.max(0, physiqueBonus);
        agilityBonus = Math.max(0, agilityBonus);
        spellPowerBonus = Math.max(0, spellPowerBonus);
        qiSeaBonus = Math.max(0, qiSeaBonus);
    }

    /** 兼容旧调用：从稳定 rewardKey 中迁移目标子阶段。 */
    public TribulationBonusSnapshot(
            String rewardKey,
            String targetRealmId,
            double sourcePercent,
            double healthBonus,
            long maxQiBonus,
            int constitutionBonus,
            int physiqueBonus,
            int agilityBonus,
            int spellPowerBonus,
            int qiSeaBonus
    ) {
        this(rewardKey, targetRealmId, "", sourcePercent, healthBonus, maxQiBonus,
                constitutionBonus, physiqueBonus, agilityBonus, spellPowerBonus, qiSeaBonus);
    }

    public boolean isActive(Realm currentRealm, SubStage currentSubStage) {
        Realm target = RealmTopology.find(this.targetRealmId).orElse(null);
        if (target == null) {
            return false;
        }
        SubStage targetSubStage = SubStage.byId(this.targetSubStageId, target);
        return RealmTopology.isAtLeast(currentRealm, currentSubStage, target, targetSubStage);
    }

    public CompoundTag toTag() {
        CompoundTag tag = new CompoundTag();
        tag.putInt("version", CURRENT_VERSION);
        tag.putString("rewardKey", this.rewardKey);
        tag.putString("targetRealmId", this.targetRealmId);
        tag.putString("targetSubStageId", this.targetSubStageId);
        tag.putDouble("sourcePercent", this.sourcePercent);
        tag.putDouble("healthBonus", this.healthBonus);
        tag.putLong("maxQiBonus", this.maxQiBonus);
        tag.putInt("constitutionBonus", this.constitutionBonus);
        tag.putInt("physiqueBonus", this.physiqueBonus);
        tag.putInt("agilityBonus", this.agilityBonus);
        tag.putInt("spellPowerBonus", this.spellPowerBonus);
        tag.putInt("qiSeaBonus", this.qiSeaBonus);
        return tag;
    }

    public static TribulationBonusSnapshot fromTag(CompoundTag tag) {
        return new TribulationBonusSnapshot(
                tag.getString("rewardKey"),
                tag.getString("targetRealmId"),
                tag.contains("targetSubStageId", 8) ? tag.getString("targetSubStageId") : "",
                tag.getDouble("sourcePercent"),
                tag.getDouble("healthBonus"),
                tag.getLong("maxQiBonus"),
                tag.getInt("constitutionBonus"),
                tag.getInt("physiqueBonus"),
                tag.getInt("agilityBonus"),
                tag.getInt("spellPowerBonus"),
                tag.getInt("qiSeaBonus"));
    }

    private static String normalizeTargetSubStageId(String rewardKey, String targetRealmId, String explicitId) {
        Realm target = RealmTopology.find(targetRealmId).orElse(Realm.MORTAL);
        if (explicitId != null && !explicitId.isBlank()) {
            return SubStage.byId(explicitId, target).id();
        }
        String prefix = "realm:" + targetRealmId + ":";
        if (rewardKey != null && rewardKey.startsWith(prefix) && rewardKey.length() > prefix.length()) {
            return SubStage.byId(rewardKey.substring(prefix.length()), target).id();
        }
        return target.firstSubStage().id();
    }
}
