package com.friday.cultivation.block.formation;

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
    @Override
    public RenderShape getRenderShape(@NotNull BlockState state) {
        return RenderShape.INVISIBLE;
    }

    @NotNull
    @Override
    public VoxelShape getVisualShape(@NotNull BlockState state, @NotNull BlockGetter level, @NotNull BlockPos pos, @NotNull CollisionContext ctx) {
        return Shapes.empty();
    }

    @NotNull
    @Override
    public VoxelShape getCollisionShape(@NotNull BlockState state, @NotNull BlockGetter level, @NotNull BlockPos pos, @NotNull CollisionContext ctx) {
        if (ctx instanceof EntityCollisionContext) {
            EntityCollisionContext ectx = (EntityCollisionContext) ctx;
            Entity entity = ectx.getEntity();
            if (entity != null && level instanceof Level lvl) {
                BlockPos corePos = findOwningDomePos(lvl, pos);
                if (corePos != null) {
                    if (SectTokenItem.entityHasTokenForCore(entity, lvl, corePos)) {
                        return Shapes.empty();
                    }
                    if (entity instanceof Projectile && SectProtectionDomeHandler.projectileMayPassBarrierOutward(lvl, pos, entity)) {
                        return Shapes.empty();
                    }
                }
            }
        }
        return Shapes.block();
    }

    @Nullable
    private static BlockPos findOwningDomePos(Level lvl, BlockPos barrierPos) {
        if (lvl.isClientSide()) {
            return ClientDomeRegistry.domeContaining((double) barrierPos.getX() + 0.5, (double) barrierPos.getY() + 0.5, (double) barrierPos.getZ() + 0.5);
        }
        FormationCorePlateBlockEntity dome = SectProtectionDomeHandler.domeOwningBarrier(lvl, barrierPos);
        return dome != null ? dome.getBlockPos() : null;
    }

    @NotNull
    @Override
    public VoxelShape getOcclusionShape(@NotNull BlockState state, @NotNull BlockGetter level, @NotNull BlockPos pos) {
        return Shapes.block();
    }

    @NotNull
    @Override
    public VoxelShape getBlockSupportShape(@NotNull BlockState state, @NotNull BlockGetter level, @NotNull BlockPos pos) {
        return Shapes.empty();
    }

    @Override
    public float getShadeBrightness(@NotNull BlockState state, @NotNull BlockGetter level, @NotNull BlockPos pos) {
        return 1.0f;
    }

    @Override
    public boolean propagatesSkylightDown(@NotNull BlockState state, @NotNull BlockGetter level, @NotNull BlockPos pos) {
        return true;
    }

    @Override
    public int getLightBlock(@NotNull BlockState state, @NotNull BlockGetter level, @NotNull BlockPos pos) {
        return 0;
    }

    @NotNull
    @Override
    public PushReaction getPistonPushReaction(@NotNull BlockState state) {
        return PushReaction.BLOCK;
    }

    @NotNull
    @Override
    public ItemStack getCloneItemStack(@NotNull BlockState state, @NotNull HitResult target, @NotNull BlockGetter level, @NotNull BlockPos pos, @NotNull Player player) {
        return ItemStack.EMPTY;
    }

    @Override
    public void attack(@NotNull BlockState state, @NotNull Level level, @NotNull BlockPos pos, @NotNull Player player) {
        if (level.isClientSide()) {
            return;
        }
        Vec3 hit = new Vec3((double) pos.getX() + 0.5, (double) pos.getY() + 0.5, (double) pos.getZ() + 0.5);
        SectProtectionDomeHandler.onBarrierTouched(level, pos, hit, 1.0f, player);
    }

    @Override
    public void onProjectileHit(@NotNull Level level, @NotNull BlockState state, @NotNull BlockHitResult hit, @NotNull Projectile projectile) {
        if (level.isClientSide()) {
            return;
        }
        if (SectProtectionDomeHandler.projectileMayPassBarrierOutward(level, hit.getBlockPos(), projectile)) {
            return;
        }
        Vec3 hitPos = hit.getLocation();
        float dmg = 1.0f;
        if (projectile instanceof AbstractArrow) {
            AbstractArrow arrow = (AbstractArrow) projectile;
            dmg = (float) arrow.getBaseDamage();
        }
        if (projectile instanceof Fireball) {
            FormationCorePlateBlockEntity dome = null;
            if (level instanceof ServerLevel) {
                ServerLevel sl = (ServerLevel) level;
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
        Entity entity = projectile.getOwner();
        SectProtectionDomeHandler.onBarrierTouched(level, hit.getBlockPos(), hitPos, dmg, entity instanceof Player ? (Player) entity : null);
        projectile.discard();
    }
}
