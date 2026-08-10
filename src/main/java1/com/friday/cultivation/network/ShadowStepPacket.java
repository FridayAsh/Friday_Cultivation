package com.friday.cultivation.network;

import com.friday.cultivation.event.SoulHookHandler;
import com.friday.cultivation.registry.ModEffects;
import java.util.function.Supplier;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkEvent;

/**
 * 影遁步（ShadowStep）网络包 — 完整复刻原模组 ShadowStepPacket。
 * <p>
 * 方向常量（前/后/左/右/上/下 6 个方向），距离固定 5 米。
 * 客户端发送 → 服务端校验（无勾魂锁定 + 有 SHADOW_STEP 效果）
 * → 计算目标位置 → 起点+终点播放 ENDERMAN_TELEPORT 音效 + PORTAL 粒子 → teleportTo 传送 → fallDistance 归零。
 * </p>
 */
public class ShadowStepPacket {
    public static final byte DIR_FORWARD = 0;
    public static final byte DIR_BACK = 1;
    public static final byte DIR_LEFT = 2;
    public static final byte DIR_RIGHT = 3;
    public static final byte DIR_UP = 4;
    public static final byte DIR_DOWN = 5;
    public static final double DISTANCE = 5.0;
    private final byte direction;

    public ShadowStepPacket(byte direction) {
        this.direction = direction;
    }

    public static void encode(ShadowStepPacket msg, FriendlyByteBuf buf) {
        buf.writeByte((int) msg.direction);
    }

    public static ShadowStepPacket decode(FriendlyByteBuf buf) {
        return new ShadowStepPacket(buf.readByte());
    }

    public static void handle(ShadowStepPacket msg, Supplier<NetworkEvent.Context> ctxSup) {
        NetworkEvent.Context ctx = ctxSup.get();
        ctx.enqueueWork(() -> {
            ServerPlayer player = ctx.getSender();
            if (player == null) {
                return;
            }
            if (SoulHookHandler.isActionLocked((Entity) player)) {
                return;
            }
            if (!player.hasEffect((MobEffect) ModEffects.SHADOW_STEP.get())) {
                return;
            }
            Vec3 dir = ShadowStepPacket.computeDirection(player, msg.direction);
            if (dir == null) {
                return;
            }
            Vec3 from = player.position();
            Vec3 to = from.add(dir.scale(5.0));
            ServerLevel sl = (ServerLevel) player.level();
            sl.playSound(null, from.x, from.y, from.z, SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 0.7f, 1.4f);
            sl.sendParticles((ParticleOptions) ParticleTypes.PORTAL, from.x, from.y + 0.8, from.z, 30, 0.3, 0.6, 0.3, 0.4);
            player.teleportTo(to.x, to.y, to.z);
            player.fallDistance = 0.0f;
            sl.playSound(null, to.x, to.y, to.z, SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 0.7f, 1.0f);
            sl.sendParticles((ParticleOptions) ParticleTypes.PORTAL, to.x, to.y + 0.8, to.z, 30, 0.3, 0.6, 0.3, 0.4);
        });
        ctx.setPacketHandled(true);
    }

    private static Vec3 computeDirection(ServerPlayer player, byte dir) {
        float yaw = player.getYHeadRot();
        float yawRad = (float) Math.toRadians(yaw);
        Vec3 forward = new Vec3(-Math.sin(yawRad), 0.0, Math.cos(yawRad)).normalize();
        Vec3 right = new Vec3(Math.cos(yawRad), 0.0, Math.sin(yawRad)).normalize();
        return switch (dir) {
            case 0 -> forward;
            case 1 -> forward.scale(-1.0);
            case 2 -> right.scale(-1.0);
            case 3 -> right;
            case 4 -> new Vec3(0.0, 1.0, 0.0);
            case 5 -> new Vec3(0.0, -1.0, 0.0);
            default -> null;
        };
    }
}
