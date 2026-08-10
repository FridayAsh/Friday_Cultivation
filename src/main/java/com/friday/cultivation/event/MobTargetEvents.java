/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.Mob
 *  net.minecraft.world.entity.ai.goal.Goal
 *  net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal
 *  net.minecraft.world.entity.monster.Monster
 *  net.minecraftforge.event.entity.EntityJoinLevelEvent
 *  net.minecraftforge.eventbus.api.SubscribeEvent
 *  net.minecraftforge.fml.common.Mod$EventBusSubscriber
 */
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

@Mod.EventBusSubscriber(modid="friday_cultivation")
public final class MobTargetEvents {
    private MobTargetEvents() {
    }

    @SubscribeEvent
    public static void onEntityJoinLevel(EntityJoinLevelEvent event) {
        if (event.getLevel().isClientSide) {
            return;
        }
        Entity entity = event.getEntity();
        if (!(entity instanceof Monster)) {
            return;
        }
        Monster monster = (Monster)entity;
        MobTargetEvents.injectCultivatorTarget((Mob)monster);
    }

    private static void injectCultivatorTarget(Mob monster) {
        monster.targetSelector.addGoal(3, (Goal)new NearestAttackableTargetGoal(monster, WanderingCultivatorEntity.class, 10, true, false, e -> {
            WanderingCultivatorEntity npc;
            return !(e instanceof WanderingCultivatorEntity) || !(npc = (WanderingCultivatorEntity)((Object)e)).isNpcSoulState();
        }));
    }
}

