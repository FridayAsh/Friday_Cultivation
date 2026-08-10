/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.ChatFormatting
 *  net.minecraft.core.BlockPos
 *  net.minecraft.network.chat.Component
 *  net.minecraft.server.level.ServerPlayer
 *  net.minecraft.world.Container
 *  net.minecraft.world.Containers
 *  net.minecraft.world.InteractionHand
 *  net.minecraft.world.InteractionResult
 *  net.minecraft.world.MenuProvider
 *  net.minecraft.world.SimpleMenuProvider
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.item.TooltipFlag
 *  net.minecraft.world.item.context.BlockPlaceContext
 *  net.minecraft.world.level.BlockGetter
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.level.LevelReader
 *  net.minecraft.world.level.block.Block
 *  net.minecraft.world.level.block.EntityBlock
 *  net.minecraft.world.level.block.entity.BlockEntity
 *  net.minecraft.world.level.block.entity.BlockEntityTicker
 *  net.minecraft.world.level.block.entity.BlockEntityType
 *  net.minecraft.world.level.block.state.BlockBehaviour$Properties
 *  net.minecraft.world.level.block.state.BlockState
 *  net.minecraft.world.level.block.state.StateDefinition$Builder
 *  net.minecraft.world.level.block.state.properties.BlockStateProperties
 *  net.minecraft.world.level.block.state.properties.BooleanProperty
 *  net.minecraft.world.level.block.state.properties.Property
 *  net.minecraft.world.phys.BlockHitResult
 *  net.minecraftforge.network.NetworkHooks
 *  net.minecraftforge.network.PacketDistributor
 *  org.jetbrains.annotations.NotNull
 *  org.jetbrains.annotations.Nullable
 */
package com.friday.cultivation.block.refining;

import com.friday.cultivation.block.refining.RefiningCoreBlockEntity;
import com.friday.cultivation.block.refining.RefiningFurnaceStructure;
import com.friday.cultivation.inventory.RefiningMenu;
import com.friday.cultivation.network.ModNetwork;
import com.friday.cultivation.network.ToggleRefiningGhostPacket;
import com.friday.cultivation.registry.ModBlockEntities;
import com.friday.cultivation.util.TooltipUtils;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.network.NetworkHooks;
import net.minecraftforge.network.PacketDistributor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class RefiningCoreBlock
extends Block
implements EntityBlock {
    public static final BooleanProperty LIT = BlockStateProperties.LIT;

    public RefiningCoreBlock(BlockBehaviour.Properties props) {
        super(props);
        this.registerDefaultState((BlockState)((BlockState)this.stateDefinition.any()).setValue((Property)LIT, (Comparable)Boolean.valueOf(false)));
    }

    protected void createBlockStateDefinition(@NotNull StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(new Property[]{LIT});
    }

    @Nullable
    public BlockState canSurvive(@NotNull BlockPlaceContext ctx) {
        return (BlockState)this.defaultBlockState().setValue((Property)LIT, (Comparable)Boolean.valueOf(false));
    }

    @Nullable
    public BlockEntity newBlockEntity(@NotNull BlockPos pos, @NotNull BlockState state) {
        return new RefiningCoreBlockEntity(pos, state);
    }

    @Nullable
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(@NotNull Level level, @NotNull BlockState state, @NotNull BlockEntityType<T> type) {
        if (level.isClientSide) {
            return null;
        }
        return RefiningCoreBlock.createTickerHelper(type, (BlockEntityType)ModBlockEntities.REFINING_CORE.get(), (lvl, pos, st, be) -> ((RefiningCoreBlockEntity)be).serverTick());
    }

    private static <E extends BlockEntity, A extends BlockEntity> BlockEntityTicker<E> createTickerHelper(BlockEntityType<E> requestedType, BlockEntityType<A> expectedType, BlockEntityTicker<? super A> ticker) {
        return expectedType == requestedType ? (BlockEntityTicker<E>) ticker : null;
    }

    public void onRemove(@NotNull BlockState state, @NotNull Level level, @NotNull BlockPos pos, @NotNull BlockState newState, boolean moved) {
        BlockEntity be;
        if (!state.is(newState.getBlock()) && (be = level.getBlockEntity(pos)) instanceof RefiningCoreBlockEntity) {
            RefiningCoreBlockEntity rce = (RefiningCoreBlockEntity)be;
            Containers.dropContents((Level)level, (BlockPos)pos, (Container)rce.getIoContainer());
        }
        super.onRemove(state, level, pos, newState, moved);
    }

    @NotNull
    public InteractionResult createBlockStateDefinition(@NotNull BlockState state, @NotNull Level level, @NotNull BlockPos pos, @NotNull Player player, @NotNull InteractionHand hand, @NotNull BlockHitResult hit) {
        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        }
        List<RefiningFurnaceStructure.MissingBlock> missing = RefiningFurnaceStructure.missingBlocks((LevelReader)level, pos);
        if (!missing.isEmpty()) {
            player.displayClientMessage((Component)Component.translatable((String)"message.friday_cultivation.refining_furnace.incomplete", (Object[])new Object[]{missing.size()}).withStyle(ChatFormatting.GOLD), true);
            if (player instanceof ServerPlayer) {
                ServerPlayer sp = (ServerPlayer)player;
                ModNetwork.CHANNEL.send(PacketDistributor.PLAYER.with(() -> sp), (Object)new ToggleRefiningGhostPacket(pos));
            }
            return InteractionResult.CONSUME;
        }
        if (player instanceof ServerPlayer) {
            ServerPlayer sp = (ServerPlayer)player;
            NetworkHooks.openScreen((ServerPlayer)sp, (MenuProvider)new SimpleMenuProvider((containerId, playerInv, p) -> new RefiningMenu(containerId, playerInv, pos), (Component)Component.translatable((String)"block.friday_cultivation.refining_core")), buf -> buf.writeBlockPos(pos));
        }
        return InteractionResult.CONSUME;
    }

    public void appendHoverText(@NotNull ItemStack stack, @Nullable BlockGetter level, @NotNull List<Component> tooltip, @NotNull TooltipFlag flag) {
        super.appendHoverText(stack, level, tooltip, flag);
        TooltipUtils.addSection(tooltip, "tooltip.friday_cultivation.section.usage");
        tooltip.add((Component)TooltipUtils.hintLine((Component)Component.translatable((String)"tooltip.friday_cultivation.refining_core.hint")));
        TooltipUtils.addSection(tooltip, "tooltip.friday_cultivation.section.structure");
        tooltip.add((Component)TooltipUtils.statsLine((Component)Component.translatable((String)"tooltip.friday_cultivation.refining_core.structure")));
    }
}

