package com.friday.cultivation.network;

import com.friday.cultivation.client.ClientDeathChoiceHooks;
import java.util.function.Supplier;
import javax.annotation.Nullable;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

public class OpenDeathChoicePacket {
    @Nullable
    private final Component deathMessage;

    public OpenDeathChoicePacket(@Nullable Component deathMessage) {
        this.deathMessage = deathMessage;
    }

    public static void encode(OpenDeathChoicePacket msg, FriendlyByteBuf buf) {
        buf.writeBoolean(msg.deathMessage != null);
        if (msg.deathMessage != null) {
            buf.writeComponent(msg.deathMessage);
        }
    }

    public static OpenDeathChoicePacket decode(FriendlyByteBuf buf) {
        return new OpenDeathChoicePacket(buf.readBoolean() ? buf.readComponent() : null);
    }

    public static void handle(OpenDeathChoicePacket msg, Supplier<NetworkEvent.Context> ctxSup) {
        NetworkEvent.Context ctx = ctxSup.get();
        ctx.enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT,
                () -> () -> ClientDeathChoiceHooks.open(msg.deathMessage)));
        ctx.setPacketHandled(true);
    }
}
