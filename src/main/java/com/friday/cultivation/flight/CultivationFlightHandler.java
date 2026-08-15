package com.friday.cultivation.flight;

import com.friday.cultivation.cultivation.CultivationCapability;
import com.friday.cultivation.cultivation.CultivationData;
import com.friday.cultivation.cultivation.spell.Spell;
import com.friday.cultivation.cultivation.technique.TechniqueBonusHelper;
import com.friday.cultivation.event.CapabilityEvents;
import com.friday.cultivation.event.RealmPressureHandler;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;
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
    private static final Map<UUID, ItemStack> SWORD_FLIGHT = new ConcurrentHashMap<>();
    private static final Map<UUID, Integer> SWORD_FLIGHT_SLOT = new ConcurrentHashMap<>();
    /** 飞行灵气消耗累计 tick（进入飞行后每 tick +1，满 20 扣一次） */
    private static final Map<UUID, Integer> FLIGHT_TICKS = new ConcurrentHashMap<>();

    private CultivationFlightHandler() {
    }

    /** 御剑是否激活（服务端读 Map；客户端读 CultivationData 同步值） */
    public static boolean isSwordFlightActive(Player player) {
        if (player == null) {
            return false;
        }
        if (SWORD_FLIGHT.containsKey(player.getUUID())) {
            return true;
        }
        CultivationData data = CultivationCapability.get((Player)player).orElse(null);
        return data != null && data.isSwordFlightActive();
    }

    /** 御剑激活时脚底渲染的剑（客户端调用；服务端有数据时返回） */
    public static ItemStack getSwordFlightStack(Player player) {
        if (player == null) {
            return ItemStack.EMPTY;
        }
        ItemStack s = SWORD_FLIGHT.get(player.getUUID());
        if (s != null && !s.isEmpty()) {
            return s;
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
        SWORD_FLIGHT.put(player.getUUID(), riding);
        SWORD_FLIGHT_SLOT.put(player.getUUID(), slot);
        // 同步到 CultivationData（供客户端渲染与判定）
        CultivationData cd = CultivationCapability.get((Player)player).orElse(null);
        if (cd != null) {
            cd.startSwordFlight(riding, slot);
        }
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
        UUID id = player.getUUID();
        ItemStack sword = SWORD_FLIGHT.remove(id);
        Integer slot = SWORD_FLIGHT_SLOT.remove(id);
        CultivationData cd = CultivationCapability.get((Player)player).orElse(null);
        if (cd != null) {
            cd.clearSwordFlight();
        }
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
            int ticks = FLIGHT_TICKS.merge(player.getUUID(), 1, Integer::sum);
            if (ticks >= 20) {
                FLIGHT_TICKS.remove(player.getUUID());
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
            FLIGHT_TICKS.remove(player.getUUID());
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

    /** 撤销飞行：恢复重力 + 移除 mayfly/flying */
    private static void disableFlight(ServerPlayer player) {
        if (player.isCreative() || player.isSpectator()) {
            return;
        }
        if (player.getAbilities().mayfly || player.getAbilities().flying) {
            player.getAbilities().mayfly = false;
            player.getAbilities().flying = false;
        }
        if (player.isNoGravity()) {
            player.setNoGravity(false);
        }
        player.onUpdateAbilities();
    }
}
