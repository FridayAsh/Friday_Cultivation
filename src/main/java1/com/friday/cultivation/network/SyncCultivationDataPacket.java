package com.friday.cultivation.network;

import com.friday.cultivation.CultivationCapability;
import com.friday.cultivation.capability.CultivationData;
import com.friday.cultivation.client.ClientCultivationHooks;
import java.util.function.Supplier;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.network.NetworkEvent;

/**
 * 修仙数据完整同步包 — 完整复刻原模组 SyncCultivationDataPacket。
 * <p>
 * 服务端 → 客户端：把一个 {@link CultivationData} 的全部字段打包为 CompoundTag 一次性同步。
 * 客户端在 handle() 中通过 {@link ClientCultivationHooks#applySync(CompoundTag)} 写入本地副本。
 * </p>
 * <p>
 * 注意：原 mod 用 {@code CultivationCapability.get(player).ifPresent(d -> d.deserializeNBT(data))}，
 * 项目里 {@code get()} 直接返回 ICultivation（非 Optional），所以改用 {@code instanceof} 判断。
 * </p>
 */
public class SyncCultivationDataPacket {
    private final CompoundTag data;

    public SyncCultivationDataPacket(CompoundTag data) {
        this.data = data;
    }

    public SyncCultivationDataPacket(CultivationData data) {
        this.data = data.serializeNBT();
    }

    public static void encode(SyncCultivationDataPacket msg, FriendlyByteBuf buf) {
        buf.writeNbt(msg.data);
    }

    public static SyncCultivationDataPacket decode(FriendlyByteBuf buf) {
        CompoundTag nbt = buf.readNbt();
        return new SyncCultivationDataPacket(nbt != null ? nbt : new CompoundTag());
    }

    public static void handle(SyncCultivationDataPacket msg, Supplier<NetworkEvent.Context> ctxSup) {
        NetworkEvent.Context ctx = ctxSup.get();
        ctx.enqueueWork(() -> ClientCultivationHooks.applySync(msg.data));
        ctx.setPacketHandled(true);
    }

    public CompoundTag getData() {
        return this.data;
    }

    public static void applyToClientPlayer(Player player, CompoundTag data) {
        CultivationData d = CultivationCapability.get(player).orElse(null);
        if (d != null) {
            d.deserializeNBT(data);
        }
    }
}
