/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.Direction
 *  net.minecraft.nbt.CompoundTag
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.world.level.chunk.LevelChunk
 *  net.minecraftforge.common.capabilities.Capability
 *  net.minecraftforge.common.capabilities.CapabilityManager
 *  net.minecraftforge.common.capabilities.CapabilityToken
 *  net.minecraftforge.common.capabilities.ICapabilityProvider
 *  net.minecraftforge.common.capabilities.ICapabilitySerializable
 *  net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent
 *  net.minecraftforge.common.util.LazyOptional
 *  org.jetbrains.annotations.NotNull
 *  org.jetbrains.annotations.Nullable
 */
package com.friday.cultivation.cultivation.qi.state;

import com.friday.cultivation.cultivation.qi.state.ChunkQiPool;
import java.util.Optional;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class ChunkQiCapability {
    public static final ResourceLocation ID = new ResourceLocation("friday_cultivation", "chunk_qi");
    public static final Capability<ChunkQiPool> CAPABILITY = CapabilityManager.get((CapabilityToken)new CapabilityToken<ChunkQiPool>(){});

    private ChunkQiCapability() {
    }

    public static Optional<ChunkQiPool> get(LevelChunk chunk) {
        if (chunk == null) {
            return Optional.empty();
        }
        return chunk.getCapability(CAPABILITY).resolve();
    }

    public static ICapabilityProvider createProvider() {
        return new Provider();
    }

    private static final class Provider
    implements ICapabilitySerializable<CompoundTag> {
        private final ChunkQiPool pool = new ChunkQiPool();
        private final LazyOptional<ChunkQiPool> handler = LazyOptional.of(() -> this.pool);

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
            return this.pool.serializeNBT();
        }

        public void deserializeNBT(CompoundTag nbt) {
            this.pool.deserializeNBT(nbt);
        }
    }
}
