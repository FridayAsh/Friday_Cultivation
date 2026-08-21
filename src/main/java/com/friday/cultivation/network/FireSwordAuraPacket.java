/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.network.FriendlyByteBuf
 *  net.minecraft.server.level.ServerPlayer
 *  net.minecraft.sounds.SoundEvents
 *  net.minecraft.sounds.SoundSource
 *  net.minecraft.world.effect.MobEffect
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.item.SwordItem
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.phys.Vec3
 *  net.minecraftforge.network.NetworkEvent$Context
 */
package com.friday.cultivation.network;

import com.friday.cultivation.cultivation.CultivationCapability;
import com.friday.cultivation.cultivation.PhysiqueBonusHelper;
import com.friday.cultivation.cultivation.QiElement;
import com.friday.cultivation.cultivation.spell.Spell;
import com.friday.cultivation.cultivation.technique.Technique;
import com.friday.cultivation.cultivation.technique.TechniqueBonusHelper;
import com.friday.cultivation.entity.SwordAuraEntity;
import com.friday.cultivation.event.CapabilityEvents;
import com.friday.cultivation.event.DharmaBodyManifestationHandler;
import com.friday.cultivation.event.SoulHookHandler;
import com.friday.cultivation.event.SpiritLockHandler;
import com.friday.cultivation.event.TimeStasisHandler;
import com.friday.cultivation.registry.ModEffects;
import com.friday.cultivation.util.SpellScalingHelper;
import java.util.function.Supplier;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkEvent;

public class FireSwordAuraPacket {
    private static final int QI_COST = 50;
    private static final int COOLDOWN_TICKS = 8;

    public static void encode(FireSwordAuraPacket msg, FriendlyByteBuf buf) {
    }

    public static FireSwordAuraPacket decode(FriendlyByteBuf buf) {
        return new FireSwordAuraPacket();
    }

    public static void handle(FireSwordAuraPacket msg, Supplier<NetworkEvent.Context> ctxSup) {
        NetworkEvent.Context ctx = ctxSup.get();
        ctx.enqueueWork(() -> {
            ServerPlayer player = ctx.getSender();
            if (player == null) {
                return;
            }
            if (TimeStasisHandler.isEntityStopped((Entity)player)) {
                return;
            }
            if (SoulHookHandler.isActionLocked((Entity)player)) {
                return;
            }
            if (SpiritLockHandler.isEntityLocked((Entity)player)) {
                return;
            }
            if (player.hasEffect((MobEffect)ModEffects.MERIDIAN_FROZEN.get())) {
                return;
            }
            if (!(player.getMainHandItem().getItem() instanceof SwordItem)) {
                return;
            }
            CultivationCapability.get((Player)player).ifPresent(data -> {
                if (!data.hasSpell(Spell.SWORD_AURA)) {
                    return;
                }
                if (!data.isSpellEnabled(Spell.SWORD_AURA)) {
                    return;
                }
                DharmaBodyManifestationHandler.trigger(player);
                long cost = TechniqueBonusHelper.applySpellQiCostMultiplier((Player)player, Spell.SWORD_AURA, 50L);
                if (data.getCurrentQi() < cost) {
                    return;
                }
                data.setCurrentQi(data.getCurrentQi() - cost);
                CapabilityEvents.syncToClient(player);
                Technique technique = TechniqueBonusHelper.equippedOf(player);
                QiElement el = technique == null ? QiElement.PURE : technique.primaryElement();
                Vec3 eye = player.getEyePosition();
                Vec3 dir = player.getLookAngle().normalize();
                Vec3 spawnPos = eye.add(dir.scale(0.8));
                SwordAuraEntity aura = new SwordAuraEntity((Level)player.serverLevel(), (Player)player, spawnPos, dir, el);
                aura.setDamageMultiplier((float)SpellScalingHelper.damageMultiplier((LivingEntity)player, Spell.SWORD_AURA));
                player.serverLevel().addFreshEntity((Entity)aura);
                PhysiqueBonusHelper.onSpellCast(player, Spell.SWORD_AURA);
                player.serverLevel().playSound(null, eye.x, eye.y, eye.z, SoundEvents.TRIDENT_THROW, SoundSource.PLAYERS, 0.7f, 1.5f);
            });
        });
        ctx.setPacketHandled(true);
    }
}
