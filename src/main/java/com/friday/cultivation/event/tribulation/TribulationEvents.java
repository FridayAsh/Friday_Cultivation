package com.friday.cultivation.event.tribulation;

import com.friday.cultivation.cultivation.CultivationData;
import com.friday.cultivation.cultivation.realm.Realm;
import com.friday.cultivation.cultivation.realm.SubStage;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.entity.player.PlayerEvent;

/**
 * 渡劫生命周期事件：供其他系统（成就/奖励/任务）监听。
 */
public abstract class TribulationEvents {

    /** 渡劫开始（携带劫谱与总道数） */
    public static class Started extends PlayerEvent {
        private final TribulationSpec spec;
        private final TribulationSession session;

        public Started(ServerPlayer player, TribulationSpec spec) {
            this(player, spec, null);
        }

        public Started(ServerPlayer player, TribulationSpec spec, TribulationSession session) {
            super(player);
            this.spec = spec;
            this.session = session;
        }

        public TribulationSpec getSpec() {
            return spec;
        }

        public int getTotalBolts() {
            return spec == null ? 0 : spec.totalBolts();
        }

        public TribulationSession getSession() {
            return session;
        }

        public String getTierId() {
            return session == null ? "" : session.tierId();
        }
    }

    /** 每次雷击前（可修改伤害） */
    public static class BoltStrike extends PlayerEvent {
        private final TribulationSpec spec;
        private final TribulationSession session;
        private float damage;

        public BoltStrike(ServerPlayer player, TribulationSpec spec, float damage) {
            this(player, spec, damage, null);
        }

        public BoltStrike(ServerPlayer player, TribulationSpec spec, float damage, TribulationSession session) {
            super(player);
            this.spec = spec;
            this.damage = damage;
            this.session = session;
        }

        public TribulationSpec getSpec() {
            return spec;
        }

        public float getDamage() {
            return damage;
        }

        public void setDamage(float damage) {
            this.damage = damage;
        }

        public TribulationSession getSession() {
            return session;
        }

        public String getTierId() {
            return session == null ? "" : session.tierId();
        }
    }

    /** 每波结束（携带剩余波数） */
    public static class WaveEnd extends PlayerEvent {
        private final int wavesRemaining;
        private final TribulationSession session;

        public WaveEnd(ServerPlayer player, int wavesRemaining) {
            this(player, wavesRemaining, null);
        }

        public WaveEnd(ServerPlayer player, int wavesRemaining, TribulationSession session) {
            super(player);
            this.wavesRemaining = wavesRemaining;
            this.session = session;
        }

        public int getWavesRemaining() {
            return wavesRemaining;
        }

        public TribulationSession getSession() {
            return session;
        }

        public String getTierId() {
            return session == null ? "" : session.tierId();
        }
    }

    /** 渡劫成功（携带新境界） */
    public static class Succeeded extends PlayerEvent {
        private final Realm realm;
        private final SubStage subStage;
        private final TribulationSession session;

        public Succeeded(ServerPlayer player, Realm realm, SubStage subStage) {
            this(player, realm, subStage, null);
        }

        public Succeeded(ServerPlayer player, Realm realm, SubStage subStage, TribulationSession session) {
            super(player);
            this.realm = realm;
            this.subStage = subStage;
            this.session = session;
        }

        public Realm getRealm() {
            return realm;
        }

        public SubStage getSubStage() {
            return subStage;
        }

        public TribulationSession getSession() {
            return session;
        }

        public String getTierId() {
            return session == null ? "" : session.tierId();
        }
    }

    /** 渡劫失败（携带掉落境界） */
    public static class Failed extends PlayerEvent {
        private final Realm realm;
        private final SubStage subStage;
        private final TribulationSession session;

        public Failed(ServerPlayer player, Realm realm, SubStage subStage) {
            this(player, realm, subStage, null);
        }

        public Failed(ServerPlayer player, Realm realm, SubStage subStage, TribulationSession session) {
            super(player);
            this.realm = realm;
            this.subStage = subStage;
            this.session = session;
        }

        public Realm getRealm() {
            return realm;
        }

        public SubStage getSubStage() {
            return subStage;
        }

        public TribulationSession getSession() {
            return session;
        }

        public String getTierId() {
            return session == null ? "" : session.tierId();
        }
    }

    private TribulationEvents() {
    }
}
