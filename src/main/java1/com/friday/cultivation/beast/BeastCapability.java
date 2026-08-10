package com.friday.cultivation.beast;

import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
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
 * 妖兽修炼能力 Capability（严格照搬原模组 com.xiaoxiang.cultivation.cultivation.beast.BeastCapability）
 */
public final class BeastCapability {
    public static final ResourceLocation ID = new ResourceLocation("friday_cultivation", "beast_cultivation");
    public static final Capability<BeastCultivationData> CAPABILITY =
            CapabilityManager.get(new CapabilityToken<BeastCultivationData>() {});

    private BeastCapability() {}

    public static Optional<BeastCultivationData> get(LivingEntity entity) {
        if (entity == null) return Optional.empty();
        return entity.getCapability(CAPABILITY).resolve();
    }

    public static ICapabilityProvider createProvider() {
        return new Provider();
    }

    private static final class Provider implements ICapabilitySerializable<CompoundTag> {
        private final BeastCultivationData data = new BeastCultivationData();
        private final LazyOptional<BeastCultivationData> handler = LazyOptional.of(() -> this.data);

        @NotNull
        @Override
        public <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
            if (cap == CAPABILITY) return this.handler.cast();
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