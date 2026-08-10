package com.friday.cultivation.qi.state;

import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * 复刻原模组 ChunkQiAttacher —— 区块灵气 Capability 挂载器。
 * <p>订阅 {@link AttachCapabilitiesEvent}，将 {@link ChunkQiCapability} 挂到每个 {@link LevelChunk} 上。</p>
 */
@Mod.EventBusSubscriber(modid = "friday_cultivation")
public final class ChunkQiAttacher {
    private ChunkQiAttacher() {
    }

    @SubscribeEvent
    public static void onAttachCapabilities(AttachCapabilitiesEvent<LevelChunk> event) {
        event.addCapability(ChunkQiCapability.ID, ChunkQiCapability.createProvider());
    }
}
