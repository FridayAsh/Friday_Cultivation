/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.brigadier.builder.LiteralArgumentBuilder
 *  com.mojang.brigadier.context.CommandContext
 *  net.minecraft.ChatFormatting
 *  net.minecraft.commands.CommandSourceStack
 *  net.minecraft.commands.Commands
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
 *  net.minecraft.world.level.chunk.LevelChunk
 *  net.minecraft.world.phys.BlockHitResult
 *  net.minecraft.world.phys.HitResult$Type
 *  net.minecraft.world.phys.Vec3
 *  net.minecraftforge.event.RegisterCommandsEvent
 *  net.minecraftforge.eventbus.api.SubscribeEvent
 *  net.minecraftforge.fml.common.Mod$EventBusSubscriber
 */
package com.friday.cultivation.command;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.friday.cultivation.cultivation.QiElement;
import com.friday.cultivation.cultivation.qi.BlockQiSpec;
import com.friday.cultivation.cultivation.qi.BlockQiSpecs;
import com.friday.cultivation.cultivation.qi.QiEcosystem;
import com.friday.cultivation.cultivation.qi.state.BlockQiState;
import com.friday.cultivation.cultivation.qi.state.ChunkQiCapability;
import com.friday.cultivation.cultivation.qi.state.ChunkQiPool;
import com.friday.cultivation.cultivation.qi.state.QiWatchTickHandler;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;
import java.util.Optional;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid="friday_cultivation")
public final class QiCommand {
    private static final double LOOK_DISTANCE = 20.0;

    private QiCommand() {
    }

    @SubscribeEvent
    public static void onRegister(RegisterCommandsEvent event) {
        event.getDispatcher().register((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)Commands.literal((String)"qi").requires(s -> s.hasPermission(0))).then(Commands.literal((String)"inspect").executes(QiCommand::inspect))).then(Commands.literal((String)"cleanup").executes(QiCommand::cleanup))).then(Commands.literal((String)"watch").executes(QiCommand::toggleWatch)));
    }

    private static int inspect(CommandContext<CommandSourceStack> ctx) {
        try {
            double percent;
            ServerPlayer player = ((CommandSourceStack)ctx.getSource()).getPlayerOrException();
            ServerLevel level = player.serverLevel();
            BlockPos pos = QiCommand.pickBlock(player);
            if (pos == null) {
                ((CommandSourceStack)ctx.getSource()).sendFailure((Component)Component.literal((String)"\u00a7c\u672a\u6307\u5411\u4efb\u4f55\u65b9\u584a"));
                return 0;
            }
            BlockState state = level.getBlockState(pos);
            BlockQiSpec spec = BlockQiSpecs.of(state);
            String blockName = state.getBlock().getName().getString();
            if (spec == null) {
                ((CommandSourceStack)ctx.getSource()).sendSuccess(() -> Component.literal((String)"\u00a77\u65b9\u584a ").append((Component)Component.literal((String)blockName).withStyle(ChatFormatting.WHITE)).append((Component)Component.literal((String)" \u00a7c\u7121\u9748\u6c23 (\u7d55\u5730)")), false);
                return 1;
            }
            int peek = QiEcosystem.peekBlock(level, pos);
            int max = spec.baseMaxQi();
            double d = percent = max > 0 ? (double)peek * 100.0 / (double)max : 0.0;
            ChatFormatting qiColor = percent >= 75.0 ? ChatFormatting.GREEN : (percent >= 25.0 ? ChatFormatting.YELLOW : ChatFormatting.RED);
            MutableComponent line1 = Component.literal((String)"\u00a77\u65b9\u584a ").append((Component)Component.literal((String)blockName).withStyle(ChatFormatting.WHITE)).append((Component)Component.literal((String)(" \u00a77@ \u00a78" + pos.getX() + "," + pos.getY() + "," + pos.getZ())));
            MutableComponent line2 = Component.literal((String)"  \u9748\u6c23: ").append((Component)Component.literal((String)(peek + "/" + max)).withStyle(qiColor)).append((Component)Component.literal((String)String.format(" \u00a78(%.0f%%)", percent)));
            MutableComponent line3 = Component.literal((String)"  \u5c6c\u6027: ").append((Component)Component.literal((String)spec.element().name()).withStyle(QiCommand.elementColor(spec))).append((Component)Component.literal((String)String.format(" \u00a77| emit %.3f/s | regen %.3f/s", spec.baseEmitRate(), spec.baseRegenPerSec())));
            ((CommandSourceStack)ctx.getSource()).sendSuccess(() -> line1, false);
            ((CommandSourceStack)ctx.getSource()).sendSuccess(() -> line2, false);
            ((CommandSourceStack)ctx.getSource()).sendSuccess(() -> line3, false);
            if (spec.degradeRule() != null) {
                String degradeName = spec.degradeRule().degradeTo() != null ? spec.degradeRule().degradeTo().getName().getString() : "(\u7a7a\u6c23)";
                MutableComponent line4 = Component.literal((String)"  \u00a7c\u964d\u968e\u898f\u5247 \u00a77\u2192 \u00a7c").append((Component)Component.literal((String)degradeName)).append((Component)Component.literal((String)String.format(" \u00a77(\u7d2f\u7a4d\u62bd \u2265%d \u2192 %.0f%% \u6a5f\u7387\u89f8\u767c)", spec.degradeRule().drainThreshold(), spec.degradeRule().chancePerCheck() * 100.0)));
                ((CommandSourceStack)ctx.getSource()).sendSuccess(() -> line4, false);
            }
            return 1;
        }
        catch (Exception e) {
            ((CommandSourceStack)ctx.getSource()).sendFailure((Component)Component.literal((String)("\u00a7c\u57f7\u884c\u5931\u6557: " + e.getMessage())));
            return 0;
        }
    }

    private static BlockPos pickBlock(ServerPlayer player) {
        Vec3 eye = player.getEyePosition();
        Vec3 look = player.getLookAngle();
        Vec3 end = eye.add(look.scale(20.0));
        ClipContext ctx = new ClipContext(eye, end, ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, (Entity)player);
        BlockHitResult hit = player.serverLevel().clip(ctx);
        if (hit.getType() != HitResult.Type.BLOCK) {
            return null;
        }
        return hit.getBlockPos();
    }

    private static int cleanup(CommandContext<CommandSourceStack> ctx) {
        try {
            ServerPlayer player = ((CommandSourceStack)ctx.getSource()).getPlayerOrException();
            ServerLevel level = player.serverLevel();
            int radius = 5;
            int pcx = player.getBlockX() >> 4;
            int pcz = player.getBlockZ() >> 4;
            int totalBefore = 0;
            int totalAfter = 0;
            int chunksProcessed = 0;
            for (int cx = pcx - radius; cx <= pcx + radius; ++cx) {
                for (int cz = pcz - radius; cz <= pcz + radius; ++cz) {
                    Optional<ChunkQiPool> poolOpt;
                    LevelChunk chunk = level.getChunkSource().getChunkNow(cx, cz);
                    if (chunk == null || (poolOpt = ChunkQiCapability.get(chunk)).isEmpty()) continue;
                    ChunkQiPool pool = poolOpt.get();
                    int before = pool.trackedCount();
                    totalBefore += before;
                    Iterator<Map.Entry<Long, BlockQiState>> it = pool.iterator();
                    long now = level.getGameTime();
                    ArrayList<BlockPos> peekTargets = new ArrayList<BlockPos>();
                    while (it.hasNext()) {
                        peekTargets.add(BlockPos.of((long)it.next().getKey()));
                    }
                    for (BlockPos pos : peekTargets) {
                        BlockQiSpec spec = BlockQiSpecs.of(level.getBlockState(pos));
                        if (spec != null) {
                            pool.peek(pos, spec, now);
                            continue;
                        }
                        pool.removeEntry(pos);
                    }
                    totalAfter += pool.trackedCount();
                    ++chunksProcessed;
                }
            }
            int cleaned = totalBefore - totalAfter;
            int finalChunksProcessed = chunksProcessed;
            int finalCleaned = cleaned;
            int finalTotalAfter = totalAfter;
            ((CommandSourceStack)ctx.getSource()).sendSuccess(() -> Component.literal((String)String.format("\u00a7a/qi cleanup: \u8655\u7406 %d chunks\uff0c\u6e05\u7406 %d entries\uff08\u5269 %d\uff09", finalChunksProcessed, finalCleaned, finalTotalAfter)), false);
            return 1;
        }
        catch (Exception e) {
            ((CommandSourceStack)ctx.getSource()).sendFailure((Component)Component.literal((String)("\u00a7c\u57f7\u884c\u5931\u6557: " + e.getMessage())));
            return 0;
        }
    }

    private static int toggleWatch(CommandContext<CommandSourceStack> ctx) {
        try {
            ServerPlayer player = ((CommandSourceStack)ctx.getSource()).getPlayerOrException();
            boolean enabled = QiWatchTickHandler.toggleWatching(player.getUUID());
            MutableComponent msg = enabled ? Component.literal((String)"\u00a7a/qi watch \u5df2\u555f\u7528\uff1a\u6bcf\u79d2\u81ea\u52d5\u5370\u51fa\u8996\u7dda\u65b9\u584a\u9748\u6c23") : Component.literal((String)"\u00a77/qi watch \u5df2\u95dc\u9589");
            ((CommandSourceStack)ctx.getSource()).sendSuccess(() -> QiCommand.lambda$toggleWatch$7((Component)msg), false);
            return 1;
        }
        catch (Exception e) {
            ((CommandSourceStack)ctx.getSource()).sendFailure((Component)Component.literal((String)("\u00a7c\u57f7\u884c\u5931\u6557: " + e.getMessage())));
            return 0;
        }
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

    private static /* synthetic */ Component lambda$toggleWatch$7(Component msg) {
        return msg;
    }
}

