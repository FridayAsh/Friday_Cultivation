package com.friday.cultivation.event;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = "friday_cultivation")
public final class DeferredServerTickQueue {
    private static final Queue<Runnable> QUEUE = new ConcurrentLinkedQueue<>();

    private DeferredServerTickQueue() {
    }

    public static void schedule(Runnable r) {
        QUEUE.add(r);
    }

    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }
        Runnable r;
        while ((r = QUEUE.poll()) != null) {
            r.run();
        }
    }
}
