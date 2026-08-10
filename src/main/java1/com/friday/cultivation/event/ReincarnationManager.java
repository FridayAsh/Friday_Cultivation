package com.friday.cultivation.event;

import com.friday.cultivation.CultivationCapability;
import com.friday.cultivation.capability.CultivationData;
import com.friday.cultivation.network.ModNetwork;
import com.friday.cultivation.network.OpenReincarnationPacket;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.PacketDistributor;

/**
 * 转世管理器 — 重置命格
 */
public final class ReincarnationManager {

    private ReincarnationManager() {}

    public static void prompt(ServerPlayer player) {
        CultivationData ic = CultivationCapability.get(player).orElse(null);
        if (ic == null) return;
        ic.setReincarnationPending(true);
        CapabilityEvents.syncToClient(player);
        ModNetwork.CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), new OpenReincarnationPacket());
    }

    public static void resolve(ServerPlayer player, boolean reincarnate) {
        CultivationData ic = CultivationCapability.get(player).orElse(null);
        if (ic == null) return;
        // 在地府或灵魂状态下，即使未弹转世提示也允许转世/返回
        if (!ic.isReincarnationPending() && !ic.isSoulState() && player.level().dimension() != com.friday.cultivation.registry.ModDimensions.DIFU) {
            player.sendSystemMessage(Component.translatableWithFallback(
                    "message.friday_cultivation.reincarnation.not_in_state", "当前无法转世（未处于地府/灵魂状态）"));
            return;
        }

        if (reincarnate) {
            doReincarnate(player, ic);
        } else {
            doReturnIntact(player, ic);
        }
    }

    /**
     * 转世重生（重新投胎）：
     * 完全重置所有修仙数据 → 变为凡人 → 传回主世界 → 引导重新选择身份/灵根。
     * 复刻自原模组 ReincarnationManager.doReincarnate。
     */
    private static void doReincarnate(ServerPlayer player, CultivationData data) {
        int entries = data.getDifuReincarnationEntries();
        data.copyFrom(new CultivationData());
        data.setDifuReincarnationEntries(entries);
        player.getInventory().clearContent();
        player.setExperienceLevels(0);
        player.setExperiencePoints(0);
        com.friday.cultivation.event.IdentityDrawHandler.openReincarnationFatePlate(player);
        com.friday.cultivation.event.SoulStateHandler.clearDeathHostility(player);
        teleportToOverworldSpawn(player);
        player.setHealth(player.getMaxHealth());
        player.stopUsingItem();
        player.sendSystemMessage(Component.translatableWithFallback(
                "message.friday_cultivation.reincarnation.reincarnated", "转世重生！请重新选择你的命格。"));
        CapabilityEvents.syncToClient(player);
        com.friday.cultivation.event.SoulStateHandler.broadcastSouls(player.getServer());
    }

    /**
     * 无损返回（放弃转世，返回主世界）：
     * 保留身份/境界/已学功法法术，仅清空背包与经验，并标准化功法。
     * 复刻自原模组 ReincarnationManager.doReturnIntact。
     */
    private static void doReturnIntact(ServerPlayer player, CultivationData data) {
        data.setSoulState(false);
        data.setReincarnationPending(false);
        data.setReincarnationReady(false);
        data.setSoulTicks(0);
        data.setMeditating(false);
        com.friday.cultivation.technique.TechniqueLoadoutHelper.NormalizationResult techniqueResult =
                com.friday.cultivation.technique.TechniqueLoadoutHelper.normalizeForCurrentState(data, player.getRandom());
        player.stopUsingItem();
        player.setExperiencePoints(0);
        player.setExperienceLevels(0);
        player.setHealth(player.getMaxHealth());
        // 保留背包物品（无损返回 = 保留修为和物品），仅清空经验
        com.friday.cultivation.event.SoulStateHandler.clearDeathHostility(player);
        teleportToOverworldSpawn(player);
        com.friday.cultivation.technique.TechniqueLoadoutHelper.notifyNormalization(player, data, techniqueResult);
        player.sendSystemMessage(Component.translatableWithFallback(
                "message.friday_cultivation.reincarnation.cancel", "你选择放弃转世，带着一身修为返回人间。"));
        CapabilityEvents.syncToClient(player);
        com.friday.cultivation.event.SoulStateHandler.broadcastSouls(player.getServer());
    }

    /** 清除所有地府/灵魂/转世残留状态（避免问题1、6的状态残留） */
    private static void clearSoulAndDifuState(CultivationData data) {
        data.setSoulState(false);
        data.setReincarnationPending(false);
        data.setReincarnationReady(false);
        data.setSoulDeathChoicePending(false);
        data.setSoulTicks(0);
        data.setDifuTicks(0);
        data.setSoulReaperPursuitEnabled(false);
        data.setGhostCultivator(false);
        data.setSoulReaperIdentity(false);
        data.setSoulReaperKills(0);
        data.setNextReaperTick(0);
    }

    /** 传送到主世界（返回玩家重生点或主世界出生点） */
    private static void teleportToOverworldSpawn(ServerPlayer player) {
        if (player.getServer() == null) return;
        net.minecraft.server.level.ServerLevel overworld = player.getServer().overworld();
        net.minecraft.core.BlockPos spawn;
        // 如果有重生点且在主世界，传送到重生点；否则传送到主世界出生点
        if (player.getRespawnPosition() != null
                && player.getRespawnDimension() == overworld.dimension()) {
            spawn = player.getRespawnPosition();
        } else {
            spawn = overworld.getSharedSpawnPos();
        }
        player.teleportTo(overworld, spawn.getX() + 0.5, spawn.getY(), spawn.getZ() + 0.5,
                player.getYRot(), player.getXRot());
    }
}
