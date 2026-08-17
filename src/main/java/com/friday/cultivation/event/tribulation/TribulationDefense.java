package com.friday.cultivation.event.tribulation;

import com.friday.cultivation.cultivation.CultivationData;
import com.friday.cultivation.cultivation.SpiritRoot;
import com.friday.cultivation.event.SectProtectionDomeHandler;
import net.minecraft.server.level.ServerPlayer;

import java.util.ArrayList;
import java.util.List;

/**
 * 渡劫防御链：按注册顺序依次减免伤害。
 * 新防御手段实现接口并注册即可，不影响渡劫核心。
 */
public interface TribulationDefense {
    /** 返回减免后的伤害（≤0 表示完全免疫） */
    float reduce(float damage, ServerPlayer player, CultivationData data);

    /** 注册表 */
    List<TribulationDefense> DEFENSES = new ArrayList<>();

    static void register(TribulationDefense defense) {
        DEFENSES.add(defense);
    }

    /** 依次应用所有防御，返回最终伤害 */
    static float applyAll(float damage, ServerPlayer player, CultivationData data) {
        float dmg = damage;
        for (TribulationDefense d : DEFENSES) {
            dmg = d.reduce(dmg, player, data);
            if (dmg <= 0.0f) {
                return 0.0f;
            }
        }
        return Math.max(0.0f, dmg);
    }

    /** 初始化并注册内置防御 */
    static void init() {
        register(new SectDomeDefense());
        register(new LightningRootDefense());
    }

    /** 宗门护盾吸收（复用 SectProtectionDomeHandler 现有公开方法） */
    final class SectDomeDefense implements TribulationDefense {
        @Override
        public float reduce(float damage, ServerPlayer player, CultivationData data) {
            return SectProtectionDomeHandler.absorbTribulationDamage(player, damage);
        }
    }

    /** 变异雷灵根渡劫伤害减免（÷2） */
    final class LightningRootDefense implements TribulationDefense {
        @Override
        public float reduce(float damage, ServerPlayer player, CultivationData data) {
            if (data != null && data.getSpiritRoot() == SpiritRoot.MUTANT_LIGHTNING) {
                return Math.max(1.0f, damage / TribulationConstants.LIGHTNING_ROOT_DIVISOR);
            }
            return damage;
        }
    }
}
