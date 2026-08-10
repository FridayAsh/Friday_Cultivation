package com.friday.cultivation.event;

import com.friday.cultivation.CultivationCapability;
import com.friday.cultivation.capability.CultivationData;
import com.friday.cultivation.entity.npc.NpcSpellCaster;
import com.friday.cultivation.entity.npc.WanderingCultivatorEntity;
import com.friday.cultivation.network.DharmaBodyManifestationPacket;
import com.friday.cultivation.network.ModNetwork;
import com.friday.cultivation.realm.Realm;
import com.friday.cultivation.spell.Spell;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.PacketDistributor;

/**
 * 法相外显处理器 — 完整复刻原模组 com.xiaoxiang.cultivation.event.DharmaBodyManifestationHandler。
 * 玩家境界到达合体期后可触发法相，持续 600 tick，冷却 6000 tick。
 */
@Mod.EventBusSubscriber(modid = "friday_cultivation")
public final class DharmaBodyManifestationHandler {
    public static final int DURATION_TICKS = 600;
    public static final int COOLDOWN_TICKS = 6000;
    private static final String COOLDOWN_REMAINING_TAG = "xiaoxiangCultivationDharmaBodyCooldownRemaining";
    private static final java.util.Map<java.util.UUID, Long> ACTIVE_UNTIL = new java.util.concurrent.ConcurrentHashMap<>();
    private static final java.util.Map<java.util.UUID, Long> COOLDOWN_UNTIL = new java.util.concurrent.ConcurrentHashMap<>();

    private DharmaBodyManifestationHandler() {
    }

    public static boolean trigger(ServerPlayer player) {
        if (player == null || player.level().isClientSide()) {
            return false;
        }
        CultivationData data = CultivationCapability.get(player).orElse(null);
        if (!DharmaBodyManifestationHandler.canUse(player, data)) {
            return false;
        }
        long now = player.serverLevel().getGameTime();
        if (DharmaBodyManifestationHandler.isActive(player, now)) {
            return false;
        }
        long cooldownEnd = COOLDOWN_UNTIL.getOrDefault(player.getUUID(), 0L);
        if (cooldownEnd > now) {
            return false;
        }
        long activeEnd = now + 600L;
        ACTIVE_UNTIL.put(player.getUUID(), activeEnd);
        COOLDOWN_UNTIL.put(player.getUUID(), now + 6000L);
        DharmaBodyManifestationHandler.sync(player, true, 600);
        player.sendSystemMessage(Component.translatable("message.friday_cultivation.dharma_body_manifestation.triggered"), true);
        return true;
    }

    public static boolean isActive(ServerPlayer player) {
        if (player == null) {
            return false;
        }
        return DharmaBodyManifestationHandler.isActive(player, player.serverLevel().getGameTime());
    }

    public static boolean trigger(WanderingCultivatorEntity npc) {
        if (npc == null || npc.level().isClientSide()) {
            return false;
        }
        if (!DharmaBodyManifestationHandler.canUse(npc)) {
            return false;
        }
        long now = npc.level().getGameTime();
        if (DharmaBodyManifestationHandler.isActive(npc, now)) {
            return false;
        }
        long cooldownEnd = COOLDOWN_UNTIL.getOrDefault(npc.getUUID(), 0L);
        if (cooldownEnd > now) {
            return false;
        }
        ACTIVE_UNTIL.put(npc.getUUID(), now + 600L);
        COOLDOWN_UNTIL.put(npc.getUUID(), now + 6000L);
        return true;
    }

    public static boolean isActive(WanderingCultivatorEntity npc) {
        if (npc == null) {
            return false;
        }
        return DharmaBodyManifestationHandler.isActive(npc, npc.level().getGameTime());
    }

    public static double spellDamageMultiplier(ServerPlayer player) {
        return DharmaBodyManifestationHandler.isActive(player) ? 1.3 : 1.0;
    }

    public static double spellDamageMultiplier(WanderingCultivatorEntity npc) {
        return DharmaBodyManifestationHandler.isActive(npc) ? 1.3 : 1.0;
    }

    public static double meleeDamageMultiplier(ServerPlayer player) {
        return DharmaBodyManifestationHandler.isActive(player) ? 1.3 : 1.0;
    }

    public static double meleeDamageMultiplier(WanderingCultivatorEntity npc) {
        return DharmaBodyManifestationHandler.isActive(npc) ? 1.3 : 1.0;
    }

    public static double defenseMultiplier(ServerPlayer player) {
        return DharmaBodyManifestationHandler.isActive(player) ? 1.3 : 1.0;
    }

    public static double defenseMultiplier(WanderingCultivatorEntity npc) {
        return DharmaBodyManifestationHandler.isActive(npc) ? 1.3 : 1.0;
    }

    public static double spellQiCostMultiplier(ServerPlayer player) {
        return DharmaBodyManifestationHandler.isActive(player) ? 0.7 : 1.0;
    }

    public static double spellQiCostMultiplier(WanderingCultivatorEntity npc) {
        return DharmaBodyManifestationHandler.isActive(npc) ? 0.7 : 1.0;
    }

    public static boolean grantsPerfectQiShield(ServerPlayer player) {
        return DharmaBodyManifestationHandler.isActive(player);
    }

    public static boolean grantsPerfectQiShield(WanderingCultivatorEntity npc) {
        return DharmaBodyManifestationHandler.isActive(npc);
    }

    /** 项目自定义方法（保留）：玩家境界到达化神期后是否显示法相光环。 */
    public static boolean isManifesting(Player player) {
        if (player == null) return false;
        return CultivationCapability.get(player).map(d -> d.getRealm().ordinal() >= 9).orElse(false);
    }

    private static boolean canUse(ServerPlayer player, CultivationData data) {
        if (data == null) {
            return false;
        }
        if (data.getRealm().ordinal() < Realm.BODY_INTEGRATION.ordinal()) {
            return false;
        }
        if (SpiritLockHandler.isEntityLocked((Entity) player)) {
            return false;
        }
        data.ensureSpellsForRealm();
        return data.hasSpell(Spell.DHARMA_BODY_MANIFESTATION);
    }

    private static boolean canUse(WanderingCultivatorEntity npc) {
        if (npc.getRealm().ordinal() < Realm.BODY_INTEGRATION.ordinal()) {
            return false;
        }
        if (SpiritLockHandler.isEntityLocked((Entity) npc)) {
            return false;
        }
        return NpcSpellCaster.knows(npc, Spell.DHARMA_BODY_MANIFESTATION);
    }

    private static boolean isActive(ServerPlayer player, long now) {
        Long activeEnd = ACTIVE_UNTIL.get(player.getUUID());
        if (activeEnd == null) {
            return false;
        }
        if (activeEnd <= now) {
            ACTIVE_UNTIL.remove(player.getUUID());
            return false;
        }
        return true;
    }

    private static boolean isActive(WanderingCultivatorEntity npc, long now) {
        Long activeEnd = ACTIVE_UNTIL.get(npc.getUUID());
        if (activeEnd == null) {
            return false;
        }
        if (activeEnd <= now) {
            ACTIVE_UNTIL.remove(npc.getUUID());
            return false;
        }
        return true;
    }

    private static void clearActive(ServerPlayer player) {
        if (player == null) {
            return;
        }
        ACTIVE_UNTIL.remove(player.getUUID());
        DharmaBodyManifestationHandler.sync(player, false, 0);
    }

    private static void clearAll(ServerPlayer player) {
        if (player == null) {
            return;
        }
        ACTIVE_UNTIL.remove(player.getUUID());
        COOLDOWN_UNTIL.remove(player.getUUID());
        DharmaBodyManifestationHandler.sync(player, false, 0);
    }

    private static void storeCooldown(ServerPlayer player) {
        if (player == null) {
            return;
        }
        long now = player.serverLevel().getGameTime();
        long cooldownEnd = COOLDOWN_UNTIL.getOrDefault(player.getUUID(), 0L);
        int remaining = cooldownEnd > now ? (int) Math.min(Integer.MAX_VALUE, cooldownEnd - now) : 0;
        if (remaining > 0) {
            player.getPersistentData().putInt(COOLDOWN_REMAINING_TAG, remaining);
        } else {
            player.getPersistentData().remove(COOLDOWN_REMAINING_TAG);
        }
    }

    private static void restoreCooldown(ServerPlayer player) {
        if (player == null) {
            return;
        }
        int remaining = player.getPersistentData().getInt(COOLDOWN_REMAINING_TAG);
        if (remaining > 0) {
            COOLDOWN_UNTIL.put(player.getUUID(), player.serverLevel().getGameTime() + (long) remaining);
            player.getPersistentData().remove(COOLDOWN_REMAINING_TAG);
        }
    }

    private static void sync(ServerPlayer player, boolean active, int durationTicks) {
        DharmaBodyManifestationPacket packet = new DharmaBodyManifestationPacket(active, player.getId(), player.getX(), player.getY(), player.getZ(), player.getYRot(), player.getXRot(), durationTicks);
        ModNetwork.CHANNEL.send(PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> player), packet);
    }

    @SubscribeEvent
    public static void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        Player player = event.getEntity();
        if (player instanceof ServerPlayer) {
            ServerPlayer serverPlayer = (ServerPlayer) player;
            DharmaBodyManifestationHandler.restoreCooldown(serverPlayer);
        }
    }

    @SubscribeEvent
    public static void onPlayerLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        Player player = event.getEntity();
        if (player instanceof ServerPlayer) {
            ServerPlayer serverPlayer = (ServerPlayer) player;
            DharmaBodyManifestationHandler.storeCooldown(serverPlayer);
            DharmaBodyManifestationHandler.clearAll(serverPlayer);
        }
    }

    @SubscribeEvent
    public static void onLivingDeath(LivingDeathEvent event) {
        LivingEntity livingEntity = event.getEntity();
        if (livingEntity instanceof ServerPlayer) {
            ServerPlayer player = (ServerPlayer) livingEntity;
            DharmaBodyManifestationHandler.clearActive(player);
        } else {
            livingEntity = event.getEntity();
            if (livingEntity instanceof WanderingCultivatorEntity) {
                WanderingCultivatorEntity npc = (WanderingCultivatorEntity) livingEntity;
                ACTIVE_UNTIL.remove(npc.getUUID());
                COOLDOWN_UNTIL.remove(npc.getUUID());
            }
        }
    }
}
