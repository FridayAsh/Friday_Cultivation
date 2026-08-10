package com.friday.cultivation.qi.state;

import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;
import net.minecraftforge.common.util.LazyOptional;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

/**
 * 区块灵气池 Capability — 完全照搬原模组 com.xiaoxiang.cultivation.cultivation.qi.state.ChunkQiCapability（84 行完整版）。
 * 把 ChunkQiPool 附加到 LevelChunk，Provider 持有单一实例并支持 NBT 持久化。
 */
public final class ChunkQiCapability {
    public static final ResourceLocation ID = new ResourceLocation("friday_cultivation", "chunk_qi");
    public static final Capability<ChunkQiPool> CAPABILITY = CapabilityManager.get(new CapabilityToken<ChunkQiPool>() {
    });

    private ChunkQiCapability() {
    }

    public static Optional<ChunkQiPool> get(LevelChunk chunk) {
        if (chunk == null) {
            return Optional.empty();
        }
        return chunk.getCapability(CAPABILITY).resolve();
    }

    public static void register(RegisterCapabilitiesEvent event) {
        event.register(ChunkQiPool.class);
    }

    public static ICapabilityProvider createProvider() {
        return new Provider();
    }

    private static final class Provider implements ICapabilitySerializable<CompoundTag> {
        private final ChunkQiPool pool = new ChunkQiPool();
        private final LazyOptional<ChunkQiPool> handler = LazyOptional.of(() -> this.pool);

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
            return this.pool.serializeNBT();
        }

        @Override
        public void deserializeNBT(CompoundTag nbt) {
            this.pool.deserializeNBT(nbt);
        }
    }
}
