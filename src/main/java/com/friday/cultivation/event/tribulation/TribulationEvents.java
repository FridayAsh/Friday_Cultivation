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

        public Started(ServerPlayer player, TribulationSpec spec) {
            super(player);
            this.spec = spec;
        }

        public TribulationSpec getSpec() {
            return spec;
        }

        public int getTotalBolts() {
            return spec == null ? 0 : spec.totalBolts();
        }
    }

    /** 每次雷击前（可修改伤害） */
    public static class BoltStrike extends PlayerEvent {
        private final TribulationSpec spec;
        private float damage;

        public BoltStrike(ServerPlayer player, TribulationSpec spec, float damage) {
            super(player);
            this.spec = spec;
            this.damage = damage;
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
    }

    /** 每波结束（携带剩余波数） */
    public static class WaveEnd extends PlayerEvent {
        private final int wavesRemaining;

        public WaveEnd(ServerPlayer player, int wavesRemaining) {
            super(player);
            this.wavesRemaining = wavesRemaining;
        }

        public int getWavesRemaining() {
            return wavesRemaining;
        }
    }

    /** 渡劫成功（携带新境界） */
    public static class Succeeded extends PlayerEvent {
        private final Realm realm;
        private final SubStage subStage;

        public Succeeded(ServerPlayer player, Realm realm, SubStage subStage) {
            super(player);
            this.realm = realm;
            this.subStage = subStage;
        }

        public Realm getRealm() {
            return realm;
        }

        public SubStage getSubStage() {
            return subStage;
        }
    }

    /** 渡劫失败（携带掉落境界） */
    public static class Failed extends PlayerEvent {
        private final Realm realm;
        private final SubStage subStage;

        public Failed(ServerPlayer player, Realm realm, SubStage subStage) {
            super(player);
            this.realm = realm;
            this.subStage = subStage;
        }

        public Realm getRealm() {
            return realm;
        }

        public SubStage getSubStage() {
            return subStage;
        }
    }

    private TribulationEvents() {
    }
}
