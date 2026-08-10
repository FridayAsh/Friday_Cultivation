package com.friday.cultivation.network;

import com.friday.cultivation.client.DeathSequenceClientEffects;
import java.util.function.Supplier;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

/**
 * 死亡序列效果包（服务端→客户端）— 完整复刻原模组 DeathSequencePacket。
 * 通知客户端启动死亡序列效果（标题动画+倒计时），DeathSequenceClientEffects.start。
 */
public class DeathSequencePacket {
    private final int titleTicks;
    private final int countdownTicks;

    public DeathSequencePacket(int titleTicks, int countdownTicks) {
        this.titleTicks = titleTicks;
        this.countdownTicks = countdownTicks;
    }

    public static void encode(DeathSequencePacket msg, FriendlyByteBuf buf) {
        buf.writeInt(msg.titleTicks);
        buf.writeInt(msg.countdownTicks);
    }

    public static DeathSequencePacket decode(FriendlyByteBuf buf) {
        return new DeathSequencePacket(buf.readInt(), buf.readInt());
    }

    public static void handle(DeathSequencePacket msg, Supplier<NetworkEvent.Context> ctxSup) {
        NetworkEvent.Context ctx = ctxSup.get();
        ctx.enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> DeathSequenceClientEffects.start(msg.titleTicks, msg.countdownTicks)));
        ctx.setPacketHandled(true);
    }

    public int titleTicks() { return this.titleTicks; }
    public int countdownTicks() { return this.countdownTicks; }
}
