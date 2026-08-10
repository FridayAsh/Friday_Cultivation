package com.friday.cultivation.network;

import com.friday.cultivation.client.SoulHookProgressHud;
import java.util.function.Supplier;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

/**
 * 勾魂进度包（服务端→客户端）— 完整复刻原模组 SoulHookProgressPacket。
 * 同步勾魂进度（激活/剩余tick/总tick/是否锁定），客户端 SoulHookProgressHud 显示进度条。
 */
public class SoulHookProgressPacket {
    private final boolean active;
    private final int remainingTicks;
    private final int totalTicks;
    private final boolean locked;

    public SoulHookProgressPacket(boolean active, int remainingTicks, int totalTicks, boolean locked) {
        this.active = active;
        this.remainingTicks = remainingTicks;
        this.totalTicks = totalTicks;
        this.locked = locked;
    }

    public static void encode(SoulHookProgressPacket msg, FriendlyByteBuf buf) {
        buf.writeBoolean(msg.active);
        buf.writeInt(msg.remainingTicks);
        buf.writeInt(msg.totalTicks);
        buf.writeBoolean(msg.locked);
    }

    public static SoulHookProgressPacket decode(FriendlyByteBuf buf) {
        return new SoulHookProgressPacket(buf.readBoolean(), buf.readInt(), buf.readInt(), buf.readBoolean());
    }

    public static void handle(SoulHookProgressPacket msg, Supplier<NetworkEvent.Context> ctxSup) {
        NetworkEvent.Context ctx = ctxSup.get();
        ctx.enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> SoulHookProgressHud.onSync(msg.active, msg.remainingTicks, msg.totalTicks, msg.locked)));
        ctx.setPacketHandled(true);
    }

    public boolean active() { return this.active; }
    public int remainingTicks() { return this.remainingTicks; }
    public int totalTicks() { return this.totalTicks; }
    public boolean locked() { return this.locked; }
}
