/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.Direction
 *  net.minecraft.nbt.CompoundTag
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.world.entity.player.Player
 *  net.minecraftforge.common.capabilities.Capability
 *  net.minecraftforge.common.capabilities.CapabilityManager
 *  net.minecraftforge.common.capabilities.CapabilityToken
 *  net.minecraftforge.common.capabilities.ICapabilityProvider
 *  net.minecraftforge.common.capabilities.ICapabilitySerializable
 *  net.minecraftforge.common.util.LazyOptional
 *  org.jetbrains.annotations.NotNull
 *  org.jetbrains.annotations.Nullable
 */
package com.friday.cultivation.cultivation;

import com.friday.cultivation.cultivation.CultivationData;
import java.util.Optional;
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

public final class CultivationCapability {
    public static final ResourceLocation ID = new ResourceLocation("friday_cultivation", "cultivation");
    public static final Capability<CultivationData> CAPABILITY = CapabilityManager.get((CapabilityToken)new CapabilityToken<CultivationData>(){});

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

    private static final class Provider
    implements ICapabilitySerializable<CompoundTag> {
        private final CultivationData data = new CultivationData();
        private final LazyOptional<CultivationData> handler = LazyOptional.of(() -> this.data);

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

