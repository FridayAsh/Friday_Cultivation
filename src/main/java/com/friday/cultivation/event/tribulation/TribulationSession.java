package com.friday.cultivation.event.tribulation;

import com.friday.cultivation.cultivation.realm.Realm;
import com.friday.cultivation.cultivation.realm.RealmTopology;
import com.friday.cultivation.cultivation.realm.SubStage;
import java.util.UUID;
import net.minecraft.nbt.CompoundTag;

/** 一次渡劫的不可变运行快照。 */
public record TribulationSession(
        String sessionId,
        String routeId,
        String sourceRealmId,
        String sourceSubStageId,
        String targetRealmId,
        String targetSubStageId,
        String tierId,
        TribulationSpec spec,
        boolean looseImmortal
) {
    public static final int CURRENT_VERSION = 2;

    public TribulationSession {
        sessionId = sessionId == null || sessionId.isBlank() ? UUID.randomUUID().toString() : sessionId;
        routeId = routeId == null || routeId.isBlank() ? "realm" : routeId;
        sourceRealmId = normalizeRealmId(sourceRealmId);
        targetRealmId = normalizeRealmId(targetRealmId);
        sourceSubStageId = normalizeSubStageId(sourceSubStageId);
        targetSubStageId = normalizeSubStageId(targetSubStageId);
        tierId = tierId == null ? "" : tierId;
        spec = sanitize(spec);
    }

    public static TribulationSession create(TribulationSpec spec, boolean looseImmortal) {
        return create(spec, looseImmortal, Realm.MORTAL, Realm.MORTAL.firstSubStage(),
                Realm.MORTAL, Realm.MORTAL.firstSubStage(), "");
    }

    public static TribulationSession create(TribulationSpec spec, boolean looseImmortal,
                                            Realm sourceRealm, SubStage sourceSubStage,
                                            Realm targetRealm, SubStage targetSubStage,
                                            String tierId) {
        Realm source = sourceRealm == null ? Realm.MORTAL : sourceRealm;
        Realm target = targetRealm == null ? source : targetRealm;
        SubStage sourceSub = sourceSubStage == null ? source.firstSubStage() : sourceSubStage;
        SubStage targetSub = targetSubStage == null ? target.firstSubStage() : targetSubStage;
        return new TribulationSession(UUID.randomUUID().toString(),
                looseImmortal ? "loose_immortal" : "realm",
                source.id(), sourceSub.id(), target.id(), targetSub.id(), tierId,
                spec, looseImmortal);
    }

    public CompoundTag toTag() {
        CompoundTag tag = new CompoundTag();
        tag.putInt("version", CURRENT_VERSION);
        tag.putString("sessionId", this.sessionId);
        tag.putString("route", this.routeId);
        tag.putString("sourceRealmId", this.sourceRealmId);
        tag.putString("sourceSubStageId", this.sourceSubStageId);
        tag.putString("targetRealmId", this.targetRealmId);
        tag.putString("targetSubStageId", this.targetSubStageId);
        tag.putString("tierId", this.tierId);
        tag.putBoolean("looseImmortal", this.looseImmortal);
        tag.putInt("waves", this.spec.waves());
        tag.putInt("boltsPerWave", this.spec.boltsPerWave());
        tag.putInt("strikeDamage", this.spec.strikeDamage());
        tag.putDouble("damageRatio", this.spec.damageRatio());
        tag.putInt("boltIntervalTicks", this.spec.boltIntervalTicks());
        tag.putString("type", this.spec.type().id());
        return tag;
    }

    public static TribulationSession fromTag(CompoundTag tag) {
        if (tag == null || tag.isEmpty()) {
            return null;
        }
        TribulationType type = TribulationType.byId(tag.getString("type"));
        TribulationSpec spec = new TribulationSpec(tag.getInt("waves"), tag.getInt("boltsPerWave"),
                tag.getInt("strikeDamage"), tag.getDouble("damageRatio"),
                tag.getInt("boltIntervalTicks"), type);
        return new TribulationSession(tag.getString("sessionId"), tag.getString("route"),
                tag.getString("sourceRealmId"), tag.getString("sourceSubStageId"),
                tag.getString("targetRealmId"), tag.getString("targetSubStageId"),
                tag.getString("tierId"), spec, tag.getBoolean("looseImmortal"));
    }

    private static String normalizeRealmId(String id) {
        return RealmTopology.find(id).map(Realm::id).orElse(Realm.MORTAL.id());
    }

    private static String normalizeSubStageId(String id) {
        return id == null || id.isBlank() ? SubStage.EARLY.id() : id;
    }

    private static TribulationSpec sanitize(TribulationSpec spec) {
        if (spec == null) {
            return TribulationSpec.of(0, 1, 0);
        }
        return new TribulationSpec(Math.max(0, spec.waves()), Math.max(1, spec.boltsPerWave()),
                Math.max(0, spec.strikeDamage()), Math.max(0.0, spec.damageRatio()),
                Math.max(0, spec.boltIntervalTicks()),
                spec.type() == null ? TribulationType.LIGHTNING : spec.type());
    }
}
