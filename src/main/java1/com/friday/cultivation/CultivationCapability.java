package com.friday.cultivation;

import com.friday.cultivation.capability.CultivationData;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

/**
 * 修炼能力 Capability 注册 — 完全照搬原模组 com.xiaoxiang.cultivation.cultivation.CultivationCapability（78 行完整版）。
 * 注意：与项目已存在的 capability.CultivationCapability（ICultivation 接口）并存，本类使用 CultivationData 具体类型。
 */
public final class CultivationCapability {
    public static final ResourceLocation ID = new ResourceLocation("friday_cultivation", "cultivation");
    public static final Capability<CultivationData> CAPABILITY = CapabilityManager.get(new CapabilityToken<CultivationData>() {
    });

    private CultivationCapability() {
    }

    public static Optional<CultivationData> get(Player player) {
        if (player == null) {
            return Optional.empty();
        }
        return player.getCapability(CAPABILITY).resolve();
    }

    public static ICapabilityProvider createProvider() {
        return new Provider();
    }

    private static final class Provider implements ICapabilitySerializable<CompoundTag> {
        private final CultivationData data = new CultivationData();
        private final LazyOptional<CultivationData> handler = LazyOptional.of(() -> this.data);

        private Provider() {
        }

        @NotNull
        @Override
        public <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
            if (cap == CAPABILITY) {
                return this.handler.cast();
            }
            return LazyOptional.empty();
        }

        @Override
        public CompoundTag serializeNBT() {
            return this.data.serializeNBT();
        }

        @Override
        public void deserializeNBT(CompoundTag nbt) {
            this.data.deserializeNBT(nbt);
        }
    }
}
