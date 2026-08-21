package com.friday.cultivation.event.tribulation;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.player.Player;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.phys.Vec3;

/**
 * 劫种抽象：每种劫独立的表现与伤害结算。
 * 当前实现雷劫；心魔劫 / 风火劫等可后续新增实现。
 */
public interface TribulationType {
    String id();

    /**
     * Resolves the persisted stable id. Unknown ids deliberately fall back to
     * the only currently supported implementation instead of being converted
     * through an enum ordinal or a random valid value.
     */
    static TribulationType byId(String id) {
        return "lightning".equals(id) ? LIGHTNING : LIGHTNING;
    }

    /** 生成劫的表现（闪电 / 火焰 / 黑雾 …） */
    void spawnEffect(ServerLevel level, ServerPlayer player, TribulationSpec spec, int strikeDamage);

    /** 伤害结算（雷=闪电伤害；心魔=百分比扣血…）。返回是否造成伤害。 */
    boolean applyDamage(ServerLevel level, ServerPlayer player, TribulationSpec spec, int strikeDamage);

    /** 雷劫实现 */
    TribulationType LIGHTNING = new LightningTribulation();

    /** 雷劫：从 TribulationHandler 抽出的现有逻辑 */
    final class LightningTribulation implements TribulationType {
        public static final String TAG = "friday_cultivation.tribulation_lightning";

        @Override
        public String id() {
            return "lightning";
        }

        @Override
        public void spawnEffect(ServerLevel level, ServerPlayer player, TribulationSpec spec, int strikeDamage) {
            LightningBolt bolt = EntityType.LIGHTNING_BOLT.create(level);
            if (bolt == null) {
                return;
            }
            Vec3 pos = boltPosition(level, player);
            bolt.moveTo(pos.x, pos.y, pos.z);
            bolt.setVisualOnly(true);
            bolt.getPersistentData().putBoolean(TAG, true);
            bolt.setDamage(strikeDamage);
            level.addFreshEntity(bolt);
        }

        @Override
        public boolean applyDamage(ServerLevel level, ServerPlayer player, TribulationSpec spec, int strikeDamage) {
            if (!player.isAlive() || strikeDamage <= 0) {
                return false;
            }
            // 点火
            player.setRemainingFireTicks(player.getRemainingFireTicks() + 1);
            if (player.getRemainingFireTicks() == 0) {
                player.setSecondsOnFire(8);
            }
            Holder<net.minecraft.world.damagesource.DamageType> lightningType =
                    level.registryAccess().registryOrThrow(Registries.DAMAGE_TYPE).getHolderOrThrow(DamageTypes.LIGHTNING_BOLT);
            DamageSource source = new DamageSource(lightningType, null, null);
            player.invulnerableTime = 0;
            player.hurt(source, strikeDamage);
            return true;
        }

        private static Vec3 boltPosition(ServerLevel level, ServerPlayer player) {
            net.minecraft.util.RandomSource random = level.random;
            double angle = random.nextDouble() * Math.PI * 2.0;
            double radius = Math.sqrt(random.nextDouble()) * TribulationConstants.BOLT_RANDOM_RADIUS;
            return new Vec3(player.getX() + Math.cos(angle) * radius, player.getY(), player.getZ() + Math.sin(angle) * radius);
        }

        /** 判断闪电是否为渡劫雷（供外部识别） */
        public static boolean isTribulationLightning(Entity entity) {
            return entity instanceof LightningBolt
                    && ((LightningBolt) entity).getPersistentData().getBoolean(TAG);
        }
    }

    /** 供 DamageSource 构造的静态辅助 */
    static boolean isLooseImmortalTribulation() {
        return false;
    }
}
