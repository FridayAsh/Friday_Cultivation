package com.friday.cultivation.flight;

import com.friday.cultivation.cultivation.CultivationCapability;
import com.friday.cultivation.cultivation.CultivationData;
import com.friday.cultivation.cultivation.spell.Spell;
import com.friday.cultivation.cultivation.technique.TechniqueBonusHelper;
import com.friday.cultivation.event.CapabilityEvents;
import com.friday.cultivation.event.RealmPressureHandler;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.phys.Vec3;

/**
 * 独立修仙飞行系统（御剑飞行 + 灵气飞行）——自写实现。
 * 方案：
 * - 服务端激活时同时设 mayfly+flying（触发创造模式飞行动画/手感/落地逻辑）
 *   与 setNoGravity（防 Caelus 等覆盖导致无法悬浮），双保险；
 * - 灵气飞行消耗灵气（25/秒），灵气耗尽自动停止；
 * - 御剑飞行脚底渲染剑（客户端渲染器）；
 * - 落地自动恢复重力与飞行状态。
 * 判定：御剑=已激活；灵气=isSpellEnabled(QI_FLIGHT) && 灵气>0。
 */
public final class CultivationFlightHandler {
    private CultivationFlightHandler() {
    }

    /** 御剑是否激活：服务端与客户端均以 Capability FlightState 为准。 */
    public static boolean isSwordFlightActive(Player player) {
        if (player == null) {
            return false;
        }
        CultivationData data = CultivationCapability.get((Player)player).orElse(null);
        return data != null && data.isSwordFlightActive();
    }

    /** 御剑激活时脚底渲染的剑（客户端调用；服务端有数据时返回） */
    public static ItemStack getSwordFlightStack(Player player) {
        if (player == null) {
            return ItemStack.EMPTY;
        }
        CultivationData data = CultivationCapability.get((Player)player).orElse(null);
        return data != null ? data.getSwordFlightStack() : ItemStack.EMPTY;
    }

    /** 灵气飞行是否可用（已启用、已双击激活且灵气>0） */
    public static boolean canQiFlight(Player player) {
        if (player == null) {
            return false;
        }
        CultivationData data = CultivationCapability.get((Player)player).orElse(null);
        return data != null && data.isSpellEnabled(Spell.QI_FLIGHT) && data.isQiFlightToggled() && data.getCurrentQi() > 0L;
    }

    /** 双击空格切换灵气飞行开关 */
    public static void toggleQiFlight(ServerPlayer player) {
        CultivationData data = CultivationCapability.get((Player)player).orElse(null);
        if (data == null || !data.hasSpell(Spell.QI_FLIGHT)) {
            player.displayClientMessage((Component)Component.translatable((String)"message.friday_cultivation.qi_flight.not_learned"), true);
            return;
        }
        boolean activating = !data.isQiFlightToggled();
        if (activating) {
            if (!data.isSpellEnabled(Spell.QI_FLIGHT)) {
                player.displayClientMessage((Component)Component.translatable((String)"message.friday_cultivation.qi_flight.disabled"), true);
                return;
            }
            if (data.getCurrentQi() <= 0L) {
                player.displayClientMessage((Component)Component.translatable((String)"message.friday_cultivation.qi_flight.no_qi"), true);
                return;
            }
            data.setQiFlightToggled(true);
            enableFlight(player);
            player.displayClientMessage((Component)Component.translatable((String)"message.friday_cultivation.qi_flight.started"), true);
        } else {
            data.setQiFlightToggled(false);
            disableFlight(player);
            player.displayClientMessage((Component)Component.translatable((String)"message.friday_cultivation.qi_flight.stopped"), true);
        }
        CapabilityEvents.syncToClient(player);
    }

    /** 是否任一飞行激活 */
    public static boolean isAnyFlightActive(Player player) {
        return isSwordFlightActive(player) || canQiFlight(player);
    }

    /**
     * 从 CultivationData 恢复登录/重启后的御剑运行态。
     * Capability 是唯一权威来源，静态 Map 只重建当前服务器进程的运行缓存。
     */
    public static void restoreAfterLogin(ServerPlayer player) {
        if (player == null) {
            return;
        }
        CultivationData data = CultivationCapability.get((Player)player).orElse(null);
        if (data == null || !data.isSwordFlightActive()) {
            return;
        }
        ItemStack sword = data.getSwordFlightStack();
        if (sword == null || sword.isEmpty()) {
            data.clearSwordFlight();
            return;
        }
        data.clearFlightTicks();
        enableFlight(player);
    }

    /** 施放/切换御剑飞行 */
    public static void toggleSwordFlight(ServerPlayer player) {
        if (isSwordFlightActive(player)) {
            stopSwordFlight(player);
            return;
        }
        if (RealmPressureHandler.isSuppressed((LivingEntity)player)) {
            player.displayClientMessage((Component)Component.translatable((String)"message.friday_cultivation.realm_pressure.flight_blocked"), true);
            return;
        }
        int slot = player.getInventory().selected;
        ItemStack sword = player.getInventory().getItem(slot);
        if (!(sword.getItem() instanceof SwordItem) || sword.isEmpty()) {
            player.displayClientMessage((Component)Component.translatable((String)"message.friday_cultivation.sword_flight.no_sword"), true);
            return;
        }
        ItemStack riding = sword.copy();
        player.getInventory().setItem(slot, ItemStack.EMPTY);
        // 同步到 CultivationData（供客户端渲染与判定）
        CultivationData cd = CultivationCapability.get((Player)player).orElse(null);
        if (cd == null) {
            player.getInventory().setItem(slot, sword);
            return;
        }
        cd.startSwordFlight(riding, slot);
        enableFlight(player);
        // 启用瞬间给予初始上升速度，让玩家原地起跳离地（否则站地无法触发飞行）
        Vec3 motion = player.getDeltaMovement();
        player.setDeltaMovement(motion.x, 0.42, motion.z);
        player.hurtMarked = true;
        player.containerMenu.broadcastChanges();
        CapabilityEvents.syncToClient(player);
        player.displayClientMessage((Component)Component.translatable((String)"message.friday_cultivation.sword_flight.started"), true);
    }

    /** 停止御剑飞行并归还剑 */
    public static void stopSwordFlight(ServerPlayer player) {
        CultivationData cd = CultivationCapability.get((Player)player).orElse(null);
        if (cd == null) {
            disableFlight(player);
            return;
        }
        ItemStack sword = cd.getSwordFlightStack().copy();
        Integer slot = cd.getSwordFlightOriginalSlot();
        cd.clearSwordFlight();
        if (sword != null && !sword.isEmpty()) {
            if (slot != null && slot >= 0 && slot < player.getInventory().items.size() && player.getInventory().getItem(slot).isEmpty()) {
                player.getInventory().setItem(slot, sword);
            } else if (!player.getInventory().add(sword)) {
                player.drop(sword, false);
            }
        }
        disableFlight(player);
        player.containerMenu.broadcastChanges();
        CapabilityEvents.syncToClient(player);
        player.displayClientMessage((Component)Component.translatable((String)"message.friday_cultivation.sword_flight.stopped"), true);
    }

    /** 服务端每 tick 飞行判定 */
    public static void tickFlight(ServerPlayer player) {
        CultivationData data = CultivationCapability.get((Player)player).orElse(null);
        if (data == null || player.isCreative() || player.isSpectator()) {
            return;
        }
        if (RealmPressureHandler.isSuppressed((LivingEntity)player)) {
            if (isSwordFlightActive(player)) {
                stopSwordFlight(player);
            }
            disableFlight(player);
            return;
        }
        boolean sword = isSwordFlightActive(player);
        boolean qi = canQiFlight(player);
        boolean shouldFly = sword || qi;
        if (shouldFly) {
            enableFlight(player);
            player.fallDistance = 0.0f;
            // 灵气消耗：灵气飞行 25/秒（每 20 tick），御剑飞行 20/20 tick
            // 用独立飞行 tick 计数（不依赖全局 tickCount 取模，保证每次进入飞行都正常累计）
            int ticks = data.incrementFlightTicks();
            if (ticks >= 20) {
                data.clearFlightTicks();
                long cost = qi ? TechniqueBonusHelper.applySpellQiCostMultiplier(player, Spell.QI_FLIGHT, 25L) : TechniqueBonusHelper.applySpellQiCostMultiplier(player, Spell.SWORD_FLIGHT, 20L);
                if (cost > 0L) {
                    long actual = Math.min(cost, data.getCurrentQi());
                    data.setCurrentQi(data.getCurrentQi() - actual);
                    CapabilityEvents.syncToClient(player);
                    if (data.getCurrentQi() <= 0L) {
                        // 灵气耗尽：灵气飞行停止；御剑飞行若灵气耗尽也停止并归还剑
                        data.setQiFlightToggled(false);
                        player.displayClientMessage(Component.translatable("message.friday_cultivation.qi_flight.no_qi"), true);
                        if (sword) {
                            stopSwordFlight(player);
                        } else {
                            disableFlight(player);
                        }
                        CapabilityEvents.syncToClient(player);
                    }
                }
            }
        } else {
            data.clearFlightTicks();
            disableFlight(player);
        }
    }

    /** 授权飞行：客户端补设 mayfly+flying（创造手感/动画）；服务端仅 noGravity 防 Caelus 覆盖。
     *  注意：不能调 onUpdateAbilities() —— 服务端 abilities.mayfly/flying 为 false，
     *  广播会覆盖客户端补设的飞行状态，导致飞行姿态闪断（变回空中走路）与 FOV 抖动（视角抽搐）。 */
    private static void enableFlight(ServerPlayer player) {
        // 悬浮飞行：setNoGravity 不受重力，客户端本地补设 mayfly+flying 控制运动
        if (!player.isNoGravity()) {
            player.setNoGravity(true);
        }
    }

    /** 撤销飞行：恢复重力 + 移除 mayfly/flying。
     *  注意：创造/旁观模式仅跳过 mayfly/flying 清除（保留原生创造飞行权限），
     *  但仍须恢复 noGravity —— 否则创造模式下停止飞行后玩家持续上浮无法降落。 */
    private static void disableFlight(ServerPlayer player) {
        if (!player.isCreative() && !player.isSpectator()) {
            if (player.getAbilities().mayfly || player.getAbilities().flying) {
                player.getAbilities().mayfly = false;
                player.getAbilities().flying = false;
            }
        }
        if (player.isNoGravity()) {
            player.setNoGravity(false);
        }
        player.onUpdateAbilities();
    }
}
