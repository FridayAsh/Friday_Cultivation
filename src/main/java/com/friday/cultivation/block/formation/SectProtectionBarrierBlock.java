/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.BlockPos
 *  net.minecraft.server.level.ServerLevel
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.entity.projectile.AbstractArrow
 *  net.minecraft.world.entity.projectile.Fireball
 *  net.minecraft.world.entity.projectile.Projectile
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.level.BlockGetter
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.level.block.Block
 *  net.minecraft.world.level.block.RenderShape
 *  net.minecraft.world.level.block.state.BlockBehaviour$Properties
 *  net.minecraft.world.level.block.state.BlockState
 *  net.minecraft.world.level.material.PushReaction
 *  net.minecraft.world.phys.BlockHitResult
 *  net.minecraft.world.phys.HitResult
 *  net.minecraft.world.phys.Vec3
 *  net.minecraft.world.phys.shapes.CollisionContext
 *  net.minecraft.world.phys.shapes.EntityCollisionContext
 *  net.minecraft.world.phys.shapes.Shapes
 *  net.minecraft.world.phys.shapes.VoxelShape
 *  org.jetbrains.annotations.NotNull
 *  org.jetbrains.annotations.Nullable
 */
package com.friday.cultivation.block.formation;

import com.friday.cultivation.block.formation.FormationCorePlateBlockEntity;
import com.friday.cultivation.client.ClientDomeRegistry;
import com.friday.cultivation.event.SectProtectionDomeHandler;
import com.friday.cultivation.item.SectTokenItem;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.Fireball;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.EntityCollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class SectProtectionBarrierBlock
extends Block {
    public SectProtectionBarrierBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @NotNull
    public RenderShape getRenderShape(@NotNull BlockState state) {
        return RenderShape.INVISIBLE;
    }

    @NotNull
    public VoxelShape getShadeBrightness(@NotNull BlockState state, @NotNull BlockGetter level, @NotNull BlockPos pos, @NotNull CollisionContext ctx) {
        return Shapes.empty();
    }

    @NotNull
    public VoxelShape getCollisionShape(@NotNull BlockState state, @NotNull BlockGetter level, @NotNull BlockPos pos, @NotNull CollisionContext ctx) {
        Level lvl;
        BlockPos corePos;
        EntityCollisionContext ectx;
        Entity entity;
        if (ctx instanceof EntityCollisionContext && (entity = (ectx = (EntityCollisionContext)ctx).getEntity()) != null && level instanceof Level && (corePos = SectProtectionBarrierBlock.findOwningDomePos(lvl = (Level)level, pos)) != null) {
            if (SectTokenItem.entityHasTokenForCore(entity, lvl, corePos)) {
                return Shapes.empty();
            }
            if (entity instanceof Projectile && SectProtectionDomeHandler.projectileMayPassBarrierOutward(lvl, pos, entity)) {
                return Shapes.empty();
            }
        }
        return Shapes.block();
    }

    @Nullable
    private static BlockPos findOwningDomePos(Level lvl, BlockPos barrierPos) {
        if (lvl.isClientSide) {
            return ClientDomeRegistry.domeContaining((double)barrierPos.getX() + 0.5, (double)barrierPos.getY() + 0.5, (double)barrierPos.getZ() + 0.5);
        }
        FormationCorePlateBlockEntity dome = SectProtectionDomeHandler.domeOwningBarrier(lvl, barrierPos);
        return dome != null ? dome.getBlockPos() : null;
    }

    @NotNull
    public VoxelShape canSurvive(@NotNull BlockState state, @NotNull BlockGetter level, @NotNull BlockPos pos, @NotNull CollisionContext ctx) {
        return Shapes.block();
    }

    @NotNull
    public VoxelShape explosionResistance(@NotNull BlockState state, @NotNull BlockGetter level, @NotNull BlockPos pos) {
        return Shapes.empty();
    }

    public float getShadeBrightness(@NotNull BlockState state, @NotNull BlockGetter level, @NotNull BlockPos pos) {
        return 1.0f;
    }

    public boolean dropResources(@NotNull BlockState state, @NotNull BlockGetter level, @NotNull BlockPos pos) {
        return true;
    }

    public int getLightBlock(@NotNull BlockState state, @NotNull BlockGetter level, @NotNull BlockPos pos) {
        return 0;
    }

    @NotNull
    public PushReaction getPistonPushReaction(@NotNull BlockState state) {
        return PushReaction.BLOCK;
    }

    @NotNull
    public ItemStack getCloneItemStack(@NotNull BlockState state, @NotNull HitResult target, @NotNull BlockGetter level, @NotNull BlockPos pos, @NotNull Player player) {
        return ItemStack.EMPTY;
    }

    public void attack(@NotNull BlockState state, @NotNull Level level, @NotNull BlockPos pos, @NotNull Player player) {
        if (level.isClientSide) {
            return;
        }
        Vec3 hit = new Vec3((double)pos.getX() + 0.5, (double)pos.getY() + 0.5, (double)pos.getZ() + 0.5);
        SectProtectionDomeHandler.onBarrierTouched(level, pos, hit, 1.0f, player);
    }

    public void onProjectileHit(@NotNull Level level, @NotNull BlockState state, @NotNull BlockHitResult hit, @NotNull Projectile projectile) {
        Player player;
        Entity entity;
        if (level.isClientSide) {
            return;
        }
        if (SectProtectionDomeHandler.projectileMayPassBarrierOutward(level, hit.getBlockPos(), (Entity)projectile)) {
            return;
        }
        Vec3 hitPos = hit.getLocation();
        float dmg = 1.0f;
        if (projectile instanceof AbstractArrow) {
            AbstractArrow arrow = (AbstractArrow)projectile;
            dmg = (float)arrow.getBaseDamage();
        }
        if (projectile instanceof Fireball) {
            FormationCorePlateBlockEntity dome = null;
            if (level instanceof ServerLevel) {
                ServerLevel sl = (ServerLevel)level;
                dome = SectProtectionDomeHandler.domeOwningProtectedShell(level, hit.getBlockPos());
                if (dome == null) {
                    dome = SectProtectionDomeHandler.domeOwningBarrier(level, hit.getBlockPos());
                }
                if (dome != null) {
                    SectProtectionDomeHandler.recordExternalBarrierExplosion(sl, dome, hitPos);
                }
            }
            return;
        }
        SectProtectionDomeHandler.onBarrierTouched(level, hit.getBlockPos(), hitPos, dmg, (entity = projectile.getOwner()) instanceof Player ? (player = (Player)entity) : null);
        projectile.discard();
    }
}

