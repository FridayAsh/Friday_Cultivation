/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.server.level.ServerPlayer
 *  net.minecraft.world.effect.MobEffect
 *  net.minecraft.world.effect.MobEffectCategory
 *  net.minecraft.world.effect.MobEffectInstance
 *  net.minecraft.world.effect.MobEffects
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.entity.ai.attributes.AttributeMap
 *  net.minecraft.world.entity.ai.attributes.AttributeModifier$Operation
 *  net.minecraft.world.entity.ai.attributes.Attributes
 *  net.minecraft.world.entity.player.Player
 *  org.jetbrains.annotations.NotNull
 */
package com.friday.cultivation.cultivation.effect;

import com.friday.cultivation.cultivation.CultivationCapability;
import com.friday.cultivation.cultivation.QiElement;
import com.friday.cultivation.event.CapabilityEvents;
import com.friday.cultivation.event.DeferredServerTickQueue;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeMap;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;

public class BloodBurnEffect
extends MobEffect {
    private static final String UUID_ATTACK = "b10d0001-bb01-4abc-8def-cafebabe0001";
    private static final String UUID_SPEED = "b10d0002-bb02-4def-9abc-cafebabe0002";

    public BloodBurnEffect() {
        super(MobEffectCategory.NEUTRAL, 0xCC2020);
        this.addAttributeModifier(Attributes.ATTACK_DAMAGE, UUID_ATTACK, 3.0, AttributeModifier.Operation.ADDITION);
        this.addAttributeModifier(Attributes.MOVEMENT_SPEED, UUID_SPEED, 0.2, AttributeModifier.Operation.MULTIPLY_TOTAL);
    }

    public boolean isDurationEffectTick(int duration, int amplifier) {
        return amplifier >= 4 || duration % 20 == 0;
    }

    public void applyEffectTick(@NotNull LivingEntity entity, int amplifier) {
        if (entity.level().isClientSide) {
            return;
        }
        if (!(entity instanceof Player)) {
            return;
        }
        Player p = (Player)entity;
        CultivationCapability.get(p).ifPresent(data -> {
            if (amplifier >= 4) {
                if (data.getCurrentQi() < data.getMaxQi()) {
                    data.setCurrentQi(data.getMaxQi());
                }
            } else {
                long gain;
                switch (amplifier) {
                    case 0: {
                        gain = 10L;
                        break;
                    }
                    case 1: {
                        gain = 100L;
                        break;
                    }
                    case 2: {
                        gain = 1000L;
                        break;
                    }
                    case 3: {
                        gain = 10000L;
                        break;
                    }
                    default: {
                        gain = 0L;
                    }
                }
                if (gain > 0L) {
                    data.absorbQi((int)Math.min(Integer.MAX_VALUE, gain), QiElement.PURE);
                }
            }
            if (p instanceof ServerPlayer) {
                ServerPlayer sp = (ServerPlayer)p;
                CapabilityEvents.syncToClient(sp);
            }
        });
    }

    public void removeAttributeModifiers(@NotNull LivingEntity entity, @NotNull AttributeMap attributeMap, int amplifier) {
        super.removeAttributeModifiers(entity, attributeMap, amplifier);
        if (entity.level().isClientSide) {
            return;
        }
        if (!(entity instanceof Player)) {
            return;
        }
        Player p = (Player)entity;
        if (p.getPersistentData().getBoolean("xc_blood_burn_skip_penalty")) {
            p.getPersistentData().remove("xc_blood_burn_skip_penalty");
            return;
        }
        int finalAmp = amplifier;
        DeferredServerTickQueue.schedule(() -> {
            if (!p.isAlive()) {
                return;
            }
            CultivationCapability.get(p).ifPresent(data -> {
                data.setCurrentQi(0L);
                if (p instanceof ServerPlayer) {
                    ServerPlayer sp = (ServerPlayer)p;
                    CapabilityEvents.syncToClient(sp);
                }
            });
            int weaknessTicks = finalAmp == 0 ? 600 : 1200;
            p.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, weaknessTicks, finalAmp));
            if (finalAmp >= 4) {
                p.setHealth(0.5f);
            }
        });
    }
}

