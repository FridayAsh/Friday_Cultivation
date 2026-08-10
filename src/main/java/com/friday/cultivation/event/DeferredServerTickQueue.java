/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraftforge.event.TickEvent$Phase
 *  net.minecraftforge.event.TickEvent$ServerTickEvent
 *  net.minecraftforge.eventbus.api.SubscribeEvent
 *  net.minecraftforge.fml.common.Mod$EventBusSubscriber
 */
package com.friday.cultivation.event;

import com.friday.cultivation.FridayCultivationMod;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid="friday_cultivation")
public final class DeferredServerTickQueue {
    private static final Queue<Runnable> QUEUE = new ConcurrentLinkedQueue<Runnable>();

    private DeferredServerTickQueue() {
    }

    public static void schedule(Runnable r) {
        QUEUE.add(r);
    }

    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        Runnable r;
        if (event.phase != TickEvent.Phase.END) {
            return;
        }
        while ((r = QUEUE.poll()) != null) {
            try {
                r.run();
            }
            catch (Exception e) {
                FridayCultivationMod.LOGGER.error("[DeferredServerTickQueue] Deferred task threw exception", (Throwable)e);
            }
        }
    }
}

