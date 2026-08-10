/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.ChatFormatting
 *  net.minecraft.core.BlockPos
 *  net.minecraft.network.chat.Component
 *  net.minecraft.network.chat.MutableComponent
 *  net.minecraft.server.level.ServerLevel
 *  net.minecraft.server.level.ServerPlayer
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.level.ClipContext
 *  net.minecraft.world.level.ClipContext$Block
 *  net.minecraft.world.level.ClipContext$Fluid
 *  net.minecraft.world.level.block.state.BlockState
 *  net.minecraft.world.phys.BlockHitResult
 *  net.minecraft.world.phys.HitResult$Type
 *  net.minecraft.world.phys.Vec3
 *  net.minecraftforge.event.TickEvent$Phase
 *  net.minecraftforge.event.TickEvent$ServerTickEvent
 *  net.minecraftforge.event.entity.player.PlayerEvent$PlayerLoggedOutEvent
 *  net.minecraftforge.eventbus.api.SubscribeEvent
 *  net.minecraftforge.fml.common.Mod$EventBusSubscriber
 */
package com.friday.cultivation.cultivation.qi.state;

import com.friday.cultivation.cultivation.QiElement;
import com.friday.cultivation.cultivation.qi.BlockQiSpec;
import com.friday.cultivation.cultivation.qi.BlockQiSpecs;
import com.friday.cultivation.cultivation.qi.QiEcosystem;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
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

@Mod.EventBusSubscriber(modid="friday_cultivation")
public final class QiWatchTickHandler {
    private static final int CHECK_INTERVAL_TICKS = 20;
    private static final double LOOK_DISTANCE = 20.0;
    private static final Set<UUID> WATCHING = new HashSet<UUID>();
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

    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }
        if (++tickCounter < 20) {
            return;
        }
        tickCounter = 0;
        if (WATCHING.isEmpty()) {
            return;
        }
        for (ServerLevel level : event.getServer().getAllLevels()) {
            for (ServerPlayer player : level.players()) {
                if (!WATCHING.contains(player.getUUID())) continue;
                QiWatchTickHandler.handleWatchingPlayer(level, player);
            }
        }
    }

    @SubscribeEvent
    public static void onPlayerLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        WATCHING.remove(event.getEntity().getUUID());
    }

    private static void handleWatchingPlayer(ServerLevel level, ServerPlayer player) {
        double percent;
        Vec3 look;
        Vec3 end;
        Vec3 eye = player.getEyePosition();
        ClipContext ctx = new ClipContext(eye, end = eye.add((look = player.getLookAngle()).scale(20.0)), ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, (Entity)player);
        BlockHitResult hit = level.clip(ctx);
        if (hit.getType() != HitResult.Type.BLOCK) {
            player.displayClientMessage((Component)Component.literal((String)"\u00a77\u00a7oqi watch: \u00a78(no block in sight)"), true);
            return;
        }
        BlockPos pos = hit.getBlockPos();
        BlockState state = level.getBlockState(pos);
        BlockQiSpec spec = BlockQiSpecs.of(state);
        String blockName = state.getBlock().getName().getString();
        if (spec == null) {
            player.displayClientMessage((Component)Component.literal((String)("\u00a77\u00a7o" + blockName + ": \u00a7c\u7121\u9748\u6c23 (\u7d55\u5730)")), true);
            return;
        }
        int peek = QiEcosystem.peekBlock(level, pos);
        int max = spec.baseMaxQi();
        double d = percent = max > 0 ? (double)peek * 100.0 / (double)max : 0.0;
        ChatFormatting qiColor = percent >= 75.0 ? ChatFormatting.GREEN : (percent >= 25.0 ? ChatFormatting.YELLOW : ChatFormatting.RED);
        MutableComponent msg = Component.literal((String)("\u00a77\u00a7o" + blockName + ": ")).copy().append((Component)Component.literal((String)(peek + "/" + max)).withStyle(qiColor)).append((Component)Component.literal((String)String.format(" \u00a78(%.0f%%) ", percent))).append((Component)Component.literal((String)spec.element().name()).withStyle(QiWatchTickHandler.elementColor(spec)));
        player.displayClientMessage((Component)msg, true);
    }

    private static ChatFormatting elementColor(BlockQiSpec spec) {
        return switch (spec.element()) {
            default -> throw new IncompatibleClassChangeError();
            case METAL -> ChatFormatting.WHITE;
            case WOOD -> ChatFormatting.GREEN;
            case WATER -> ChatFormatting.AQUA;
            case FIRE -> ChatFormatting.RED;
            case EARTH -> ChatFormatting.GOLD;
            case ICE -> ChatFormatting.BLUE;
            case LIGHTNING -> ChatFormatting.YELLOW;
            case PURE -> ChatFormatting.LIGHT_PURPLE;
        };
    }
}

