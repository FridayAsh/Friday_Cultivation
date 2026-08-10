package com.friday.cultivation.effect;

import com.friday.cultivation.CultivationCapability;
import com.friday.cultivation.spirit.QiElement;
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

/**
 * 血燃效果 - 严格 1:1 复刻原模组 com.xiaoxiang.cultivation.cultivation.effect.BloodBurnEffect
 * 完全照搬原 mod：m_19472_ → addAttributeModifier, m_6584_ → isDurationEffectTick,
 *                 m_6742_ → applyEffectTick, m_9236_ → level(), f_46443_ → isClientSide,
 *                 m_6386_ → removeAttributeModifiers, m_128471_ → getBoolean,
 *                 m_128473_ → remove, m_6084_ → isAlive, m_7292_ → addEffect,
 *                 m_21153_ → kill, f_19613_ → WEAKNESS, f_22281_ → ATTACK_DAMAGE,
 *                 f_22279_ → MOVEMENT_SPEED
 */
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
                        long l = 10L;
                        gain = l;
                        break;
                    }
                    case 1: {
                        long l = 100L;
                        gain = l;
                        break;
                    }
                    case 2: {
                        long l = 1000L;
                        gain = l;
                        break;
                    }
                    case 3: {
                        long l = 10000L;
                        gain = l;
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
                p.kill();
            }
        });
    }
}
