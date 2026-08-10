/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.BlockPos
 *  net.minecraft.network.chat.Component
 *  net.minecraft.network.chat.MutableComponent
 *  net.minecraft.server.level.ServerPlayer
 *  net.minecraft.world.InteractionHand
 *  net.minecraft.world.InteractionResult
 *  net.minecraft.world.MenuProvider
 *  net.minecraft.world.SimpleMenuProvider
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.item.TooltipFlag
 *  net.minecraft.world.level.BlockGetter
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.level.block.Block
 *  net.minecraft.world.level.block.EntityBlock
 *  net.minecraft.world.level.block.entity.BlockEntity
 *  net.minecraft.world.level.block.entity.BlockEntityTicker
 *  net.minecraft.world.level.block.entity.BlockEntityType
 *  net.minecraft.world.level.block.state.BlockBehaviour$Properties
 *  net.minecraft.world.level.block.state.BlockState
 *  net.minecraft.world.phys.BlockHitResult
 *  net.minecraft.world.phys.shapes.CollisionContext
 *  net.minecraft.world.phys.shapes.Shapes
 *  net.minecraft.world.phys.shapes.VoxelShape
 *  net.minecraftforge.network.NetworkHooks
 *  org.jetbrains.annotations.NotNull
 *  org.jetbrains.annotations.Nullable
 */
package com.friday.cultivation.block.formation;

import com.friday.cultivation.block.formation.FormationCorePlateBlockEntity;
import com.friday.cultivation.cultivation.qi.formation.CoreTier;
import com.friday.cultivation.inventory.FormationMenu;
import com.friday.cultivation.item.FormationCompassItem;
import com.friday.cultivation.registry.ModBlockEntities;
import com.friday.cultivation.util.TooltipUtils;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.network.NetworkHooks;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class FormationCorePlateBlock
extends Block
implements EntityBlock {
    public static final VoxelShape SHAPE = Shapes.box((double)0.0, (double)0.0, (double)0.0, (double)1.0, (double)0.1875, (double)1.0);
    private final CoreTier tier;

    public FormationCorePlateBlock(BlockBehaviour.Properties properties, CoreTier tier) {
        super(properties);
        this.tier = tier;
    }

    public CoreTier coreTier() {
        return this.tier;
    }

    private static int maxLinkedFlagsFor(CoreTier tier) {
        return switch (tier) {
            default -> throw new IncompatibleClassChangeError();
            case LOW -> 1;
            case MID -> 5;
            case HIGH -> 10;
            case SUPREME -> 30;
            case IMMORTAL -> 50;
        };
    }

    public void appendHoverText(@NotNull ItemStack stack, @Nullable BlockGetter level, @NotNull List<Component> tooltip, @NotNull TooltipFlag flag) {
        super.appendHoverText(stack, level, tooltip, flag);
        MutableComponent maxText = Component.literal((String)Long.toString(this.tier.maxQi()));
        tooltip.add((Component)TooltipUtils.tieredName((Component)Component.translatable((String)"tooltip.friday_cultivation.formation_core_plate.title"), this.tier.itemTier()));
        TooltipUtils.addBlank(tooltip);
        TooltipUtils.addSection(tooltip, "tooltip.friday_cultivation.section.effect");
        tooltip.add((Component)TooltipUtils.descriptionLine((Component)Component.translatable((String)"tooltip.friday_cultivation.formation_core_plate.summary")));
        TooltipUtils.addSection(tooltip, "tooltip.friday_cultivation.section.stats");
        tooltip.add((Component)TooltipUtils.statsLine((Component)Component.translatable((String)"tooltip.friday_cultivation.formation_core_plate.capacity", (Object[])new Object[]{maxText})));
        tooltip.add((Component)TooltipUtils.statsLine((Component)Component.translatable((String)"tooltip.friday_cultivation.formation_core_plate.max_flags", (Object[])new Object[]{FormationCorePlateBlock.maxLinkedFlagsFor(this.tier)})));
        TooltipUtils.addBlank(tooltip);
        tooltip.add((Component)TooltipUtils.hintLine((Component)Component.translatable((String)"tooltip.friday_cultivation.formation_core_plate.link")));
        tooltip.add((Component)TooltipUtils.hintLine((Component)Component.translatable((String)"tooltip.friday_cultivation.formation_core_plate.activate")));
    }

    @NotNull
    public VoxelShape getShape(@NotNull BlockState state, @NotNull BlockGetter level, @NotNull BlockPos pos, @NotNull CollisionContext ctx) {
        return SHAPE;
    }

    @Nullable
    public BlockEntity newBlockEntity(@NotNull BlockPos pos, @NotNull BlockState state) {
        return new FormationCorePlateBlockEntity(pos, state);
    }

    @Nullable
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(@NotNull Level level, @NotNull BlockState state, @NotNull BlockEntityType<T> type) {
        if (level.isClientSide) {
            return null;
        }
        return type == ModBlockEntities.FORMATION_CORE_PLATE.get() ? (lvl, pos, st, be) -> ((FormationCorePlateBlockEntity)be).serverTick() : null;
    }

    @NotNull
    public InteractionResult use(@NotNull BlockState state, @NotNull Level level, @NotNull BlockPos pos, @NotNull Player player, @NotNull InteractionHand hand, @NotNull BlockHitResult hit) {
        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        }
        BlockEntity be = level.getBlockEntity(pos);
        if (!(be instanceof FormationCorePlateBlockEntity)) {
            return InteractionResult.PASS;
        }
        FormationCorePlateBlockEntity core = (FormationCorePlateBlockEntity)be;
        if (player.isShiftKeyDown() && player.getItemInHand(hand).getItem() instanceof FormationCompassItem) {
            FormationCompassItem.lockCore(player.getItemInHand(hand), level, pos, player, core);
            return InteractionResult.CONSUME;
        }
        if (player instanceof ServerPlayer) {
            String coreName;
            ServerPlayer sp = (ServerPlayer)player;
            String coreNameFinal = coreName = core.getCustomName();
            NetworkHooks.openScreen((ServerPlayer)sp, (MenuProvider)new SimpleMenuProvider((containerId, playerInv, p) -> new FormationMenu(containerId, playerInv, pos), (Component)state.getBlock().getName()), buf -> {
                buf.writeBlockPos(pos);
                buf.writeUtf(coreNameFinal, 32);
            });
        }
        return InteractionResult.CONSUME;
    }

    public void onRemove(BlockState oldState, @NotNull Level level, @NotNull BlockPos pos, BlockState newState, boolean isMoving) {
        BlockEntity be;
        if (!oldState.is(newState.getBlock()) && (be = level.getBlockEntity(pos)) instanceof FormationCorePlateBlockEntity) {
            FormationCorePlateBlockEntity core = (FormationCorePlateBlockEntity)be;
            core.onBlockRemoved();
        }
        super.onRemove(oldState, level, pos, newState, isMoving);
    }
}

