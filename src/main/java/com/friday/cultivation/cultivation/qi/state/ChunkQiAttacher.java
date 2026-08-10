/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.world.level.chunk.LevelChunk
 *  net.minecraftforge.event.AttachCapabilitiesEvent
 *  net.minecraftforge.eventbus.api.SubscribeEvent
 *  net.minecraftforge.fml.common.Mod$EventBusSubscriber
 */
package com.friday.cultivation.cultivation.qi.state;

import com.friday.cultivation.cultivation.qi.state.ChunkQiCapability;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid="friday_cultivation")
public final class ChunkQiAttacher {
    private ChunkQiAttacher() {
    }

    @SubscribeEvent
    public static void onAttachCapabilities(AttachCapabilitiesEvent<LevelChunk> event) {
        event.addCapability(ChunkQiCapability.ID, ChunkQiCapability.createProvider());
    }
}

