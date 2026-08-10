package com.friday.cultivation.network;

import com.friday.cultivation.event.SoulHookHandler;
import com.friday.cultivation.registry.ModEffects;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * 凌空跃步（空中跳跃）触发包（客户端→服务端）— 严格复刻自原模组
 * com.xiaoxiang.cultivation.network.AirJumpPacket
 * <p>
 * 无字段数据（空包），仅作触发信号
 */
public class AirJumpPacket {
    public static final String AIR_JUMPS_USED_KEY = "xc_air_jumps_used";

    public static void encode(AirJumpPacket msg, FriendlyByteBuf buf) {
    }

    public static AirJumpPacket decode(FriendlyByteBuf buf) {
        return new AirJumpPacket();
    }

    public static void handle(AirJumpPacket msg, Supplier<NetworkEvent.Context> ctxSup) {
        NetworkEvent.Context ctx = ctxSup.get();
        ctx.enqueueWork(() -> {
            ServerPlayer player = ctx.getSender();
            if (player == null) {
                return;
            }
            if (SoulHookHandler.isActionLocked((Entity) player)) {
                return;
            }
            MobEffectInstance e = player.getEffect((MobEffect) ModEffects.DIVINE_STRIDE.get());
            if (e == null || e.getAmplifier() < 3) {
                return;
            }
            if (player.onGround()) {
                return;
            }
            int used = player.getPersistentData().getInt(AIR_JUMPS_USED_KEY);
            if (used >= 3) {
                return;
            }
            Vec3 v = player.getDeltaMovement();
            player.setDeltaMovement(v.x, 0.42, v.z);
            player.setOnGround(true);
            player.fallDistance = 0.0f;
            player.getPersistentData().putInt(AIR_JUMPS_USED_KEY, used + 1);
            ServerLevel sl = (ServerLevel) player.level();
            sl.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.PLAYER_LEVELUP, SoundSource.PLAYERS, 0.6f, 1.6f);
            sl.sendParticles((ParticleOptions) ParticleTypes.FLAME, player.getX(), player.getY() + 0.1, player.getZ(), 12, 0.4, 0.05, 0.4, 0.05);
        });
        ctx.setPacketHandled(true);
    }
}
