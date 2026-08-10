package com.friday.cultivation.qi.state;

import com.friday.cultivation.QiElement;
import com.friday.cultivation.qi.BlockQiSpec;
import com.friday.cultivation.qi.BlockQiSpecs;
import com.friday.cultivation.qi.QiEcosystem;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * 灵视 watch 处理器 - 玩家开启"灵视"后每秒检测视线方向的方块灵气状态。
 * 严格 1:1 复刻原 mod xiaoxiang.cultivation.cultivation.qi.state.QiWatchTickHandler。
 */
@Mod.EventBusSubscriber(modid = "friday_cultivation")
public final class QiWatchTickHandler {
    private static final int CHECK_INTERVAL_TICKS = 20;
    private static final double LOOK_DISTANCE = 20.0;
    private static final Set<UUID> WATCHING = new HashSet<>();
    private static int tickCounter = 0;

    private QiWatchTickHandler() {
    }

    public static boolean toggleWatching(UUID playerUuid) {
        if (WATCHING.contains(playerUuid)) {
            WATCHING.remove(playerUuid);
            return false;
        }
        WATCHING.add(playerUuid);
        return true;
    }

    public static boolean isWatching(UUID playerUuid) {
        return WATCHING.contains(playerUuid);
    }

    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }
        if (++tickCounter < CHECK_INTERVAL_TICKS) {
            return;
        }
        tickCounter = 0;
        if (WATCHING.isEmpty()) {
            return;
        }
        for (ServerLevel level : event.getServer().getAllLevels()) {
            for (ServerPlayer player : level.players()) {
                if (!WATCHING.contains(player.getUUID())) continue;
                handleWatchingPlayer(level, player);
            }
        }
    }

    @SubscribeEvent
    public static void onPlayerLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        if (event.getEntity() != null) {
            WATCHING.remove(event.getEntity().getUUID());
        }
    }

    private static void handleWatchingPlayer(ServerLevel level, ServerPlayer player) {
        Vec3 eye = player.getEyePosition();
        Vec3 look = player.getViewVector(1.0F);
        Vec3 end = eye.add(look.scale(LOOK_DISTANCE));
        ClipContext ctx = new ClipContext(eye, end, ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, (Entity) player);
        BlockHitResult hit = level.clip(ctx);
        if (hit.getType() != HitResult.Type.BLOCK) {
            player.displayClientMessage((Component) Component.literal("\u00a77\u00a7o灵视: \u00a78视线内无方块"), true);
            return;
        }
        BlockPos pos = hit.getBlockPos();
        BlockState state = level.getBlockState(pos);
        BlockQiSpec spec = BlockQiSpecs.of(state);
        String blockName = state.getBlock().getDescriptionId();
        if (spec == null) {
            player.displayClientMessage((Component) Component.literal("\u00a77\u00a7o" + blockName + ": \u00a7c无灵气 (绝地)"), true);
            return;
        }
        int peek = QiEcosystem.peekBlock(level, pos);
        int max = spec.baseMaxQi();
        double percent = max > 0 ? (double) peek * 100.0 / (double) max : 0.0;
        ChatFormatting qiColor = percent >= 75.0 ? ChatFormatting.GREEN : (percent >= 25.0 ? ChatFormatting.YELLOW : ChatFormatting.RED);
        MutableComponent msg = Component.literal("\u00a77\u00a7o" + blockName + ": ")
                .append(Component.literal(peek + "/" + max).withStyle(qiColor))
                .append(Component.literal(" \u00a78(" + String.format("%.0f%%", percent) + ") "))
                .append(Component.literal(spec.element().name()).withStyle(elementColor(spec)));
        player.displayClientMessage((Component) msg, true);
    }

    private static ChatFormatting elementColor(BlockQiSpec spec) {
        return switch (spec.element().name()) {
            case "PURE" -> ChatFormatting.WHITE;
            case "WOOD" -> ChatFormatting.GREEN;
            case "WATER" -> ChatFormatting.AQUA;
            case "FIRE" -> ChatFormatting.RED;
            case "EARTH" -> ChatFormatting.GOLD;
            case "METAL" -> ChatFormatting.BLUE;
            case "ICE" -> ChatFormatting.YELLOW;
            case "LIGHTNING" -> ChatFormatting.LIGHT_PURPLE;
            default -> ChatFormatting.WHITE;
        };
    }
}
