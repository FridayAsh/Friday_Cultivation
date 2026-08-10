/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.ChatFormatting
 *  net.minecraft.core.BlockPos
 *  net.minecraft.network.chat.Component
 *  net.minecraft.network.protocol.Packet
 *  net.minecraft.network.protocol.game.ClientboundSetSubtitleTextPacket
 *  net.minecraft.network.protocol.game.ClientboundSetTitleTextPacket
 *  net.minecraft.network.protocol.game.ClientboundSetTitlesAnimationPacket
 *  net.minecraft.server.level.ServerLevel
 *  net.minecraft.server.level.ServerPlayer
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.level.LevelAccessor
 *  net.minecraft.world.level.block.state.BlockState
 *  net.minecraftforge.event.entity.living.LivingAttackEvent
 *  net.minecraftforge.event.entity.living.LivingDeathEvent
 *  net.minecraftforge.event.level.BlockEvent$BreakEvent
 *  net.minecraftforge.event.level.ExplosionEvent$Detonate
 *  net.minecraftforge.eventbus.api.EventPriority
 *  net.minecraftforge.eventbus.api.SubscribeEvent
 *  net.minecraftforge.fml.common.Mod$EventBusSubscriber
 */
package com.friday.cultivation.event;

import com.friday.cultivation.cultivation.sect.SectRole;
import com.friday.cultivation.cultivation.sect.SectSavedData;
import com.friday.cultivation.entity.npc.WanderingCultivatorEntity;
import java.util.HashSet;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundSetSubtitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitlesAnimationPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.event.level.ExplosionEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid="friday_cultivation")
public final class SectCombatHandler {
    private SectCombatHandler() {
    }

    @SubscribeEvent(priority=EventPriority.HIGHEST)
    public static void onLivingAttack(LivingAttackEvent event) {
        Level level = event.getEntity().level();
        if (!(level instanceof ServerLevel)) {
            return;
        }
        ServerLevel level2 = (ServerLevel)level;
        Entity source = event.getSource().getEntity();
        if (!(source instanceof LivingEntity)) {
            return;
        }
        LivingEntity attacker = (LivingEntity)source;
        LivingEntity target = event.getEntity();
        if (!SectCombatHandler.canApplyOffensiveEffect(attacker, target)) {
            event.setCanceled(true);
        }
    }

    public static boolean canApplyOffensiveEffect(LivingEntity attacker, LivingEntity target) {
        return SectCombatHandler.canAffectSameSect(attacker, target, true);
    }

    public static boolean canTargetOffensiveEffect(LivingEntity attacker, LivingEntity target) {
        return SectCombatHandler.canAffectSameSect(attacker, target, false);
    }

    private static boolean canAffectSameSect(LivingEntity attacker, LivingEntity target, boolean commit) {
        if (attacker == null || target == null || attacker == target) {
            return true;
        }
        Level level = target.level();
        if (!(level instanceof ServerLevel)) {
            return true;
        }
        ServerLevel level2 = (ServerLevel)level;
        SectSavedData data = SectSavedData.get(level2);
        if (!data.sameSect((Entity)attacker, (Entity)target)) {
            return true;
        }
        if (data.hasEnemyRelation((Entity)attacker, (Entity)target)) {
            if (commit) {
                SectCombatHandler.makeNpcRetaliate(attacker, target);
            }
            return true;
        }
        if (data.hasSameSectCombatPair((Entity)attacker, (Entity)target)) {
            if (commit) {
                SectCombatHandler.makeNpcRetaliate(attacker, target);
            }
            return true;
        }
        if (data.canStartSameSectDamage((Entity)attacker)) {
            if (commit && target instanceof WanderingCultivatorEntity) {
                WanderingCultivatorEntity npc = (WanderingCultivatorEntity)target;
                data.recordSameSectCombatPair((Entity)attacker, (Entity)target);
                npc.setTarget(attacker);
            }
            return true;
        }
        return false;
    }

    private static void makeNpcRetaliate(LivingEntity attacker, LivingEntity target) {
        WanderingCultivatorEntity npc;
        if (target instanceof WanderingCultivatorEntity) {
            npc = (WanderingCultivatorEntity)target;
            npc.setTarget(attacker);
        }
        if (attacker instanceof WanderingCultivatorEntity) {
            npc = (WanderingCultivatorEntity)attacker;
            npc.setTarget(target);
        }
    }

    @SubscribeEvent(priority=EventPriority.LOWEST, receiveCanceled=true)
    public static void onLivingDeath(LivingDeathEvent event) {
        Level level = event.getEntity().level();
        if (!(level instanceof ServerLevel)) {
            return;
        }
        ServerLevel level2 = (ServerLevel)level;
        LivingEntity victim = event.getEntity();
        SectSavedData data = SectSavedData.get(level2);
        String sectId = data.sectIdOf((Entity)victim);
        if (sectId == null || sectId.isBlank()) {
            return;
        }
        SectSavedData.SectRecord sect = data.byId(sectId).orElse(null);
        SectRole victimRole = sect == null ? SectRole.NONE : data.roleOf((Entity)victim);
        Entity source = event.getSource().getEntity();
        boolean destroyed = false;
        if (sect != null && victimRole != SectRole.NONE && source instanceof LivingEntity) {
            LivingEntity killer = (LivingEntity)source;
            data.markEnemyAndRetaliate(level2, sect, killer, victimRole, false);
        }
        if (victim instanceof WanderingCultivatorEntity) {
            WanderingCultivatorEntity npc = (WanderingCultivatorEntity)victim;
            boolean removed = data.removeNpcMember(npc);
            boolean bl = destroyed = removed && data.markDestroyedIfNoNpcMembers(sectId);
        }
        if (sect != null && source instanceof Player) {
            Player player = (Player)source;
            if (destroyed && player instanceof ServerPlayer) {
                ServerPlayer serverPlayer = (ServerPlayer)player;
                SectCombatHandler.sendSectDestroyedTitle(serverPlayer, sect);
            } else if (!destroyed && victimRole != SectRole.NONE) {
                player.displayClientMessage((Component)Component.translatable((String)"message.friday_cultivation.sect.revenge_marked", (Object[])new Object[]{sect.name, victimRole.displayName()}), true);
            }
        }
    }

    private static void sendSectDestroyedTitle(ServerPlayer player, SectSavedData.SectRecord sect) {
        player.connection.send((Packet)new ClientboundSetTitlesAnimationPacket(10, 78, 24));
        player.connection.send((Packet)new ClientboundSetTitleTextPacket((Component)Component.translatable((String)"message.friday_cultivation.sect.destroyed_title", (Object[])new Object[]{sect.name}).withStyle(new ChatFormatting[]{ChatFormatting.DARK_RED, ChatFormatting.BOLD})));
        player.connection.send((Packet)new ClientboundSetSubtitleTextPacket((Component)Component.translatable((String)"message.friday_cultivation.sect.destroyed_subtitle").withStyle(ChatFormatting.GOLD)));
        player.sendSystemMessage((Component)Component.translatable((String)"message.friday_cultivation.sect.destroyed_message", (Object[])new Object[]{sect.name}).withStyle(ChatFormatting.DARK_RED));
    }

    @SubscribeEvent(priority=EventPriority.LOWEST)
    public static void onBlockBreak(BlockEvent.BreakEvent event) {
        LevelAccessor levelAccessor = event.getLevel();
        if (!(levelAccessor instanceof ServerLevel)) {
            return;
        }
        ServerLevel level = (ServerLevel)levelAccessor;
        Player player = event.getPlayer();
        if (player == null) {
            return;
        }
        SectSavedData data = SectSavedData.get(level);
        SectSavedData.SectRecord sect = data.findSectByArrayBlock(event.getPos(), event.getState());
        if (sect == null) {
            return;
        }
        SectCombatHandler.markArraySaboteur(level, data, event.getPos(), event.getState(), (LivingEntity)player, true);
    }

    @SubscribeEvent(priority=EventPriority.LOWEST)
    public static void onExplosionDetonate(ExplosionEvent.Detonate event) {
        Entity entity2;
        Level level = event.getLevel();
        if (!(level instanceof ServerLevel)) {
            return;
        }
        ServerLevel level2 = (ServerLevel)level;
        LivingEntity saboteur = event.getExplosion().getIndirectSourceEntity();
        if (saboteur == null && (entity2 = event.getExplosion().getDirectSourceEntity()) instanceof LivingEntity) {
            LivingEntity living;
            saboteur = living = (LivingEntity)entity2;
        }
        if (saboteur == null) {
            return;
        }
        LivingEntity attacker = saboteur;
        event.getAffectedEntities().removeIf(entity -> {
            LivingEntity target;
            return entity instanceof LivingEntity && (target = (LivingEntity)entity) != attacker && !SectCombatHandler.canApplyOffensiveEffect(attacker, target);
        });
        SectSavedData data = SectSavedData.get(level2);
        HashSet<String> markedSects = new HashSet<String>();
        for (BlockPos pos : event.getAffectedBlocks()) {
            BlockState brokenState;
            SectSavedData.SectRecord sect = data.findSectByArrayBlock(pos, brokenState = level2.getBlockState(pos));
            if (sect == null || !markedSects.add(sect.id)) continue;
            SectCombatHandler.markArraySaboteur(level2, data, pos, brokenState, saboteur, saboteur instanceof Player);
        }
    }

    public static void markArraySaboteur(ServerLevel level, SectSavedData data, BlockPos pos, BlockState brokenState, LivingEntity saboteur, boolean notifyPlayer) {
        SectSavedData.SectRecord sect = data.markArrayBlockSaboteur(level, pos, saboteur, brokenState);
        if (sect == null) {
            return;
        }
        if (notifyPlayer && saboteur instanceof Player) {
            Player player = (Player)saboteur;
            player.displayClientMessage((Component)Component.translatable((String)"message.friday_cultivation.sect.hostile_marked", (Object[])new Object[]{sect.name}), true);
        }
    }
}

