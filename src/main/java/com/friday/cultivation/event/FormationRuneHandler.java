/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.ChatFormatting
 *  net.minecraft.core.BlockPos
 *  net.minecraft.core.Direction
 *  net.minecraft.core.particles.ParticleOptions
 *  net.minecraft.core.particles.SimpleParticleType
 *  net.minecraft.network.chat.Component
 *  net.minecraft.server.level.ServerLevel
 *  net.minecraft.server.level.ServerPlayer
 *  net.minecraft.sounds.SoundEvents
 *  net.minecraft.sounds.SoundSource
 *  net.minecraft.world.InteractionHand
 *  net.minecraft.world.InteractionResult
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.item.Item
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.level.BlockGetter
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.level.block.Block
 *  net.minecraft.world.level.block.state.BlockState
 *  net.minecraftforge.event.TickEvent$LevelTickEvent
 *  net.minecraftforge.event.TickEvent$Phase
 *  net.minecraftforge.event.entity.player.PlayerInteractEvent$RightClickBlock
 *  net.minecraftforge.eventbus.api.SubscribeEvent
 *  net.minecraftforge.fml.common.Mod$EventBusSubscriber
 */
package com.friday.cultivation.event;

import com.friday.cultivation.block.formation.FormationRuneBlock;
import com.friday.cultivation.block.formation.FormationRuneBlockEntity;
import com.friday.cultivation.cultivation.CultivationCapability;
import com.friday.cultivation.cultivation.CultivationData;
import com.friday.cultivation.cultivation.technique.TechniqueBonusHelper;
import com.friday.cultivation.event.CapabilityEvents;
import com.friday.cultivation.event.SectProtectionDomeHandler;
import com.friday.cultivation.registry.ModBlocks;
import com.friday.cultivation.registry.ModItems;
import com.friday.cultivation.registry.ModParticles;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid="friday_cultivation")
public final class FormationRuneHandler {
    private static final long CARVE_QI_COST = 10L;

    private FormationRuneHandler() {
    }

    @SubscribeEvent
    public static void onLevelTick(TickEvent.LevelTickEvent event) {
        if (event.phase != TickEvent.Phase.END || event.level.isClientSide) {
            return;
        }
        Level level = event.level;
        if (!(level instanceof ServerLevel)) {
            return;
        }
        ServerLevel sl = (ServerLevel)level;
        if (sl.getGameTime() % 20L != 0L) {
            return;
        }
        FormationRuneBlockEntity.tickNetworks(sl);
    }

    @SubscribeEvent
    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        Player player = event.getEntity();
        ItemStack stack = player.getItemInHand(event.getHand());
        if (!stack.is((Item)ModItems.FORMATION_INSCRIPTION_KNIFE.get())) {
            return;
        }
        event.setCanceled(true);
        event.setCancellationResult(InteractionResult.sidedSuccess((boolean)event.getLevel().isClientSide));
        Level level = event.getLevel();
        if (level.isClientSide) {
            return;
        }
        Direction face = event.getFace();
        if (face == null) {
            FormationRuneHandler.tell(player, "message.friday_cultivation.formation_rune.invalid_surface", ChatFormatting.RED);
            return;
        }
        BlockPos supportPos = event.getPos();
        BlockPos runePos = supportPos.relative(face);
        BlockState support = level.getBlockState(supportPos);
        BlockState target = level.getBlockState(runePos);
        if (!support.isFaceSturdy(level, supportPos, face)) {
            FormationRuneHandler.tell(player, "message.friday_cultivation.formation_rune.invalid_surface", ChatFormatting.RED);
            return;
        }
        if (target.getBlock() instanceof FormationRuneBlock) {
            FormationRuneHandler.tell(player, "message.friday_cultivation.formation_rune.already", ChatFormatting.YELLOW);
            return;
        }
        if (!target.isAir()) {
            FormationRuneHandler.tell(player, "message.friday_cultivation.formation_rune.occupied", ChatFormatting.RED);
            return;
        }
        if (!FormationRuneHandler.canCarveAt(player, level, supportPos) || !FormationRuneHandler.canCarveAt(player, level, runePos)) {
            FormationRuneHandler.tell(player, "message.friday_cultivation.formation_rune.protected", ChatFormatting.RED);
            return;
        }
        if (!FormationRuneHandler.consumeQi(player)) {
            FormationRuneHandler.tell(player, "message.friday_cultivation.formation_rune.no_qi", ChatFormatting.RED);
            return;
        }
        BlockState runeState = FormationRuneBlock.updateConnections(FormationRuneBlock.stateForFace(face), (BlockGetter)level, runePos);
        level.setBlock(runePos, runeState, 3);
        for (Direction dir : Direction.values()) {
            level.updateNeighborsAt(runePos.relative(dir), (Block)ModBlocks.FORMATION_RUNE.get());
        }
        FormationRuneHandler.damageKnife(player, stack, event.getHand());
        player.swing(event.getHand(), true);
        if (level instanceof ServerLevel) {
            ServerLevel sl = (ServerLevel)level;
            sl.sendParticles((ParticleOptions)((SimpleParticleType)ModParticles.AMBIENT_QI_LIGHTNING.get()), (double)runePos.getX() + 0.5, (double)runePos.getY() + 0.5, (double)runePos.getZ() + 0.5, 14, 0.25, 0.25, 0.25, 0.012);
        }
        level.playSound(null, runePos, SoundEvents.ENCHANTMENT_TABLE_USE, SoundSource.PLAYERS, 0.55f, 1.35f);
        level.playSound(null, runePos, SoundEvents.GRINDSTONE_USE, SoundSource.PLAYERS, 0.18f, 1.85f);
        FormationRuneHandler.tell(player, "message.friday_cultivation.formation_rune.carved", ChatFormatting.AQUA);
    }

    private static boolean consumeQi(Player player) {
        if (player.getAbilities().instabuild) {
            return true;
        }
        CultivationData data = CultivationCapability.get(player).orElse(null);
        long cost = TechniqueBonusHelper.applyQiCostMultiplier(player, 10L);
        if (data == null || !data.consumeQi(cost)) {
            return false;
        }
        if (player instanceof ServerPlayer) {
            ServerPlayer sp = (ServerPlayer)player;
            CapabilityEvents.syncToClient(sp);
        }
        return true;
    }

    private static void damageKnife(Player player, ItemStack stack, InteractionHand hand) {
        if (player.getAbilities().instabuild) {
            return;
        }
        stack.hurtAndBreak(1, (LivingEntity)player, p -> p.broadcastBreakEvent(hand));
    }

    private static boolean canCarveAt(Player player, Level level, BlockPos pos) {
        if (!SectProtectionDomeHandler.isProtectedByAnySectProtectionDome(level, pos)) {
            return true;
        }
        return SectProtectionDomeHandler.canPlayerCarveFormationRuneAt(player, level, pos);
    }

    private static void tell(Player player, String key, ChatFormatting color) {
        player.displayClientMessage((Component)Component.translatable((String)key).withStyle(color), true);
    }
}

