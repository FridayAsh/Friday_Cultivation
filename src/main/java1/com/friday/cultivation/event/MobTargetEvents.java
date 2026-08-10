package com.friday.cultivation.event;

import com.friday.cultivation.entity.npc.WanderingCultivatorEntity;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * 生物目标事件 - 怪物生成时为其添加"攻击散修（非灵魂态）"目标选择器。
 * 完全照搬原 mod: xiaoxiang.cultivation.event.MobTargetEvents
 */
@Mod.EventBusSubscriber(modid = "friday_cultivation")
public final class MobTargetEvents {
    private MobTargetEvents() {
    }

    @SubscribeEvent
    public static void onEntityJoinLevel(EntityJoinLevelEvent event) {
        if (event.getLevel().isClientSide()) {
            return;
        }
        Entity entity = event.getEntity();
        if (!(entity instanceof Monster monster)) {
            return;
        }
        injectCultivatorTarget(monster);
    }

    private static void injectCultivatorTarget(Mob monster) {
        monster.targetSelector.addGoal(3, (Goal) new NearestAttackableTargetGoal<>(monster, WanderingCultivatorEntity.class, 10, true, false,
                e -> {
                    if (!(e instanceof WanderingCultivatorEntity npc)) return true;
                    return !npc.isNpcSoulState();
                }));
    }
}
