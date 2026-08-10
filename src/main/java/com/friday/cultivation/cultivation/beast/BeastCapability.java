/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.Direction
 *  net.minecraft.nbt.CompoundTag
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.world.entity.Entity
 *  net.minecraftforge.common.capabilities.Capability
 *  net.minecraftforge.common.capabilities.CapabilityManager
 *  net.minecraftforge.common.capabilities.CapabilityToken
 *  net.minecraftforge.common.capabilities.ICapabilityProvider
 *  net.minecraftforge.common.capabilities.ICapabilitySerializable
 *  net.minecraftforge.common.util.LazyOptional
 *  org.jetbrains.annotations.NotNull
 *  org.jetbrains.annotations.Nullable
 */
package com.friday.cultivation.cultivation.beast;

import com.friday.cultivation.cultivation.beast.BeastCultivationData;
import java.util.Optional;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class BeastCapability {
    public static final ResourceLocation ID = new ResourceLocation("friday_cultivation", "beast_cultivation");
    public static final Capability<BeastCultivationData> CAPABILITY = CapabilityManager.get((CapabilityToken)new CapabilityToken<BeastCultivationData>(){});

    private BeastCapability() {
    }

    public static Optional<BeastCultivationData> get(Entity entity) {
        if (entity == null) {
            return Optional.empty();
        }
        return entity.getCapability(CAPABILITY).resolve();
    }

    public static ICapabilityProvider createProvider() {
        return new Provider();
    }

    private static final class Provider
    implements ICapabilitySerializable<CompoundTag> {
        private final BeastCultivationData data = new BeastCultivationData();
        private final LazyOptional<BeastCultivationData> handler = LazyOptional.of(() -> this.data);

        private Provider() {
        }

        @NotNull
        public <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
            if (cap == CAPABILITY) {
                return this.handler.cast();
            }
            return LazyOptional.empty();
        }

        public CompoundTag serializeNBT() {
            return this.data.serializeNBT();
        }

        public void deserializeNBT(CompoundTag nbt) {
            this.data.deserializeNBT(nbt);
        }
    }
}

