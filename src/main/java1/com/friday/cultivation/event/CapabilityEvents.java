package com.friday.cultivation.event;

import com.friday.cultivation.CultivationCapability;
import com.friday.cultivation.capability.CultivationData;
import com.friday.cultivation.config.ModCommonConfig;
import com.friday.cultivation.network.ModNetwork;
import com.friday.cultivation.network.SyncCultivationDataPacket;
import com.friday.cultivation.sect.SectSavedData;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.network.PacketDistributor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Capability 事件处理器 — 照搬原 mod: xiaoxiang.cultivation.event.CapabilityEvents
 * <p>
 * 注意：原模组使用 {@code @Mod.EventBusSubscriber} 自动注册；本项目主类已通过
 * {@code forgeEventBus.register(CapabilityEvents.class)} 手动注册，因此本类不再加注解，
 * 避免事件重复触发。
 * </p>
 */
public final class CapabilityEvents {
    private CapabilityEvents() {
    }

    @SubscribeEvent
    public static void onAttachCapabilities(AttachCapabilitiesEvent<Entity> event) {
        if (!(event.getObject() instanceof Player)) {
            return;
        }
        // 单一实例同时服务根包 CAPABILITY(CultivationData) 与 capability 包 CULTIVATION_CAP(ICultivation)
        CultivationData data = new CultivationData();
        LazyOptional<CultivationData> handler = LazyOptional.of(() -> data);
        event.addCapability(CultivationCapability.ID, new ICapabilitySerializable<CompoundTag>() {
            @Override
            public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
                if (cap == com.friday.cultivation.CultivationCapability.CAPABILITY) return handler.cast();
                return LazyOptional.empty();
            }

            @Override
            public CompoundTag serializeNBT() {
                return data.serializeNBT();
            }

            @Override
            public void deserializeNBT(CompoundTag nbt) {
                data.deserializeNBT(nbt);
            }
        });
        event.addListener(() -> handler.invalidate());
    }

    @SubscribeEvent
    public static void onPlayerClone(PlayerEvent.Clone event) {
        Player original = event.getOriginal();
        Player clone = event.getEntity();
        original.reviveCaps();
        try {
            CultivationCapability.get(original).ifPresent(oldData -> CultivationCapability.get(clone).ifPresent(newData -> newData.copyFrom(oldData)));
        } finally {
            original.invalidateCaps();
        }
    }

    @SubscribeEvent
    public static void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        Player player = event.getEntity();
        if (player instanceof ServerPlayer) {
            ServerPlayer player2 = (ServerPlayer) player;
            CultivationCapability.get(player2).ifPresent(data -> {
                data.clearCharging();
                data.applyZhenyuanMajorAutoRebalanceMigration();
                CapabilityEvents.applySpellTerrainRuleSnapshot(data, true);
            });
            CapabilityEvents.syncToClient(player2);
        }
    }

    @SubscribeEvent
    public static void onPlayerChangedDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        Player player = event.getEntity();
        if (player instanceof ServerPlayer) {
            ServerPlayer player2 = (ServerPlayer) player;
            CultivationCapability.get(player2).ifPresent(CapabilityEvents::applySpellTerrainRuleSnapshot);
            CapabilityEvents.syncToClient(player2);
        }
    }

    @SubscribeEvent
    public static void onPlayerRespawn(PlayerEvent.PlayerRespawnEvent event) {
        Player player = event.getEntity();
        if (player instanceof ServerPlayer) {
            ServerPlayer player2 = (ServerPlayer) player;
            CultivationCapability.get(player2).ifPresent(CapabilityEvents::applySpellTerrainRuleSnapshot);
            CapabilityEvents.syncToClient(player2);
        }
    }

    public static void syncToClient(ServerPlayer player) {
        SectSavedData.get(player.serverLevel()).syncPlayerSectDisplay(player);
        CultivationCapability.get(player).ifPresent(data -> ModNetwork.CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), new SyncCultivationDataPacket(data)));
    }

    public static boolean applySpellTerrainRuleSnapshot(CultivationData data) {
        return CapabilityEvents.applySpellTerrainRuleSnapshot(data, false);
    }

    public static boolean applySpellTerrainRuleSnapshot(CultivationData data, boolean resetToServerDefault) {
        boolean changed = false;
        changed |= data.setSpellTerrainDestructionForcedOffByServer(ModCommonConfig.spellTerrainDestructionForceDisabled());
        boolean defaultEnabled = ModCommonConfig.spellTerrainDestructionDefaultEnabled();
        if (resetToServerDefault) {
            changed |= data.isSpellTerrainDestructionEnabled() != defaultEnabled;
            data.setSpellTerrainDestructionEnabled(defaultEnabled);
        } else {
            changed |= data.initializeSpellTerrainDestructionPreference(defaultEnabled);
        }
        return changed;
    }

    public static void refreshSpellTerrainRuleSnapshot(ServerPlayer player) {
        CultivationCapability.get(player).ifPresent(data -> {
            if (CapabilityEvents.applySpellTerrainRuleSnapshot(data)) {
                CapabilityEvents.syncToClient(player);
            }
        });
    }

    public static void registerCapability(RegisterCapabilitiesEvent event) {
        event.register(CultivationData.class);
    }
}
