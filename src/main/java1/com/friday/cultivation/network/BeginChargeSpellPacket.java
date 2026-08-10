package com.friday.cultivation.network;

import com.friday.cultivation.CultivationCapability;
import com.friday.cultivation.capability.CultivationData;
import com.friday.cultivation.event.CapabilityEvents;
import com.friday.cultivation.event.DharmaBodyManifestationHandler;
import com.friday.cultivation.event.LifeBalanceHandler;
import com.friday.cultivation.event.NascentSoulOutOfBodyHandler;
import com.friday.cultivation.event.PalmThunderHandler;
import com.friday.cultivation.event.RealmPressureHandler;
import com.friday.cultivation.event.SoulHookHandler;
import com.friday.cultivation.event.SpiritLockHandler;
import com.friday.cultivation.event.TimeStasisHandler;
import com.friday.cultivation.event.VoidEscapeHandler;
import com.friday.cultivation.registry.ModEffects;
import com.friday.cultivation.spell.Spell;
import com.friday.cultivation.spell.SpellType;
import com.friday.cultivation.technique.TechniqueBonusHelper;
import java.util.function.Supplier;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.SwordItem;
import net.minecraftforge.network.NetworkEvent;

/**
 * 开始充能法术包 - 客户端通知服务端开始对当前选中法术充能。
 * 完全照搬原 mod: xiaoxiang.cultivation.network.BeginChargeSpellPacket
 */
public record BeginChargeSpellPacket(int spellId, int slot) {
    public BeginChargeSpellPacket(int spellId) {
        this(spellId, 0);
    }

    public static void encode(BeginChargeSpellPacket msg, FriendlyByteBuf buf) {
        buf.writeVarInt(msg.spellId);
        buf.writeVarInt(msg.slot);
    }

    public static BeginChargeSpellPacket decode(FriendlyByteBuf buf) {
        return new BeginChargeSpellPacket(buf.readVarInt(), buf.readVarInt());
    }

    public static void handle(BeginChargeSpellPacket msg, Supplier<NetworkEvent.Context> ctxSup) {
        NetworkEvent.Context ctx = ctxSup.get();
        ctx.enqueueWork(() -> {
            ServerPlayer player = ctx.getSender();
            if (player == null) {
                return;
            }
            if (SoulHookHandler.isActionLocked((Entity) player)) {
                player.displayClientMessage(Component.translatable("message.friday_cultivation.soul_hook_spell.action_locked"), true);
                return;
            }
            CultivationCapability.get((Player) player).ifPresent(data -> {
                if (data.isVoidEscapeActive()) {
                    VoidEscapeHandler.tryManualExitIfActive(player);
                    return;
                }
                if (data.isCharging()) {
                    return;
                }
                String sid = data.getSelectedSpellId();
                if (sid.isEmpty()) {
                    return;
                }
                Spell sp = Spell.byId(sid);
                if (sp == null || !data.hasSpell(sp)) {
                    return;
                }
                if (sp.type() != SpellType.ACTIVE) {
                    return;
                }
                if (!sp.chargeable()) {
                    return;
                }
                if (!TimeStasisHandler.canPerformStoppedTimeAction(player, sp)) {
                    return;
                }
                if (sp == Spell.VOID_ESCAPE && data.isInTribulation()) {
                    player.displayClientMessage(Component.translatable("message.friday_cultivation.void_escape.blocked_tribulation"), true);
                    return;
                }
                if (NascentSoulOutOfBodyHandler.isActive(player)) {
                    player.displayClientMessage(Component.translatable("message.friday_cultivation.nascent_soul_out_of_body.blocked"), true);
                    return;
                }
                if (player.hasEffect((MobEffect) ModEffects.MERIDIAN_FROZEN.get())) {
                    player.displayClientMessage(Component.translatable("message.friday_cultivation.meridian_frozen.caster_locked"), true);
                    return;
                }
                if (SpiritLockHandler.isEntityLocked((Entity) player)) {
                    if (SpiritLockHandler.tryCastSelfUnlock(player, data)) {
                        return;
                    }
                    player.displayClientMessage(Component.translatable("message.friday_cultivation.spirit_lock.caster_locked"), true);
                    return;
                }
                if (sp == Spell.SWORD_CONVERGENCE && !(player.getMainHandItem().getItem() instanceof SwordItem)) {
                    player.displayClientMessage(Component.translatable("message.friday_cultivation.sword_convergence.no_sword"), true);
                    return;
                }
                if (sp == Spell.SKY_SPLITTING_SWORD_AURA && !(player.getMainHandItem().getItem() instanceof SwordItem)) {
                    player.displayClientMessage(Component.translatable("message.friday_cultivation.sky_splitting_sword_aura.no_sword"), true);
                    return;
                }
                DharmaBodyManifestationHandler.trigger(player);
                if (sp == Spell.TAISHANG_LIFE_BALANCE) {
                    LifeBalanceHandler.beginChannel(player, data);
                    return;
                }
                if (sp == Spell.PALM_THUNDER) {
                    PalmThunderHandler.beginChannel(player, data);
                    return;
                }
                long actualCost = TechniqueBonusHelper.applySpellQiCostMultiplier((Player) player, sp, sp.qiCost());
                if (data.getCurrentQi() < actualCost) {
                    if (sp == Spell.TIME_STASIS && TimeStasisHandler.isEntityStopped((Entity) player)) {
                        player.displayClientMessage(Component.translatable("message.friday_cultivation.cast.no_qi", new Object[]{sp.displayNameForRealm(data.getRealm())}), true);
                    }
                    return;
                }
                if (sp == Spell.TIME_STASIS && TimeStasisHandler.releaseStoppedEntity((LivingEntity) player)) {
                    data.setCurrentQi(data.getCurrentQi() - actualCost);
                    CapabilityEvents.syncToClient(player);
                    return;
                }
                data.setCurrentQi(data.getCurrentQi() - actualCost);
                data.setChargingSpellId(sp.id());
                data.setChargedQi(sp.qiCost());
                if (sp == Spell.REALM_PRESSURE) {
                    RealmPressureHandler.beginExpansion(player, data);
                }
                if (sp == Spell.TIME_STASIS) {
                    TimeStasisHandler.onChargeStarted(player);
                }
                CapabilityEvents.syncToClient(player);
            });
        });
        ctx.setPacketHandled(true);
    }
}
