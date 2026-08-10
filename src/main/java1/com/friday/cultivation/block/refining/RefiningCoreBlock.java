package com.friday.cultivation.block.refining;

import com.friday.cultivation.network.ModNetwork;
import com.friday.cultivation.network.ToggleRefiningGhostPacket;
import com.friday.cultivation.registry.ModBlockEntities;
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
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.network.NetworkHooks;
import net.minecraftforge.network.PacketDistributor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * 炼器核心方块 — 完整复刻原模组 RefiningCoreBlock。
 * 右键打开炼器菜单（RefiningMenu），结构不完整时发送幽灵提示，提供 BlockEntity + 服务端 ticker。
 */
public class RefiningCoreBlock extends Block implements EntityBlock {
    public static final BooleanProperty LIT = BlockStateProperties.LIT;

    public RefiningCoreBlock(BlockBehaviour.Properties props) {
        super(props);
        this.registerDefaultState(this.stateDefinition.any().setValue(LIT, false));
    }

    @Override
    protected void createBlockStateDefinition(@NotNull StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(LIT);
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(@NotNull BlockPlaceContext ctx) {
        return this.defaultBlockState().setValue(LIT, false);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(@NotNull BlockPos pos, @NotNull BlockState state) {
        return new RefiningCoreBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(@NotNull Level level, @NotNull BlockState state, @NotNull BlockEntityType<T> type) {
        if (level.isClientSide()) return null;
        return createTickerHelper(type, ModBlockEntities.REFINING_CORE.get(), (lvl, pos, st, be) -> be.serverTick());
    }

    private static <E extends BlockEntity, A extends BlockEntity> BlockEntityTicker<E> createTickerHelper(BlockEntityType<E> requestedType, BlockEntityType<A> expectedType, BlockEntityTicker<? super A> ticker) {
        if (expectedType == requestedType) {
            @SuppressWarnings("unchecked")
            BlockEntityTicker<E> castTicker = (BlockEntityTicker<E>) ticker;
            return castTicker;
        }
        return null;
    }

    @NotNull
    @Override
    public InteractionResult use(@NotNull BlockState state, @NotNull Level level, @NotNull BlockPos pos, @NotNull Player player, @NotNull InteractionHand hand, @NotNull BlockHitResult hit) {
        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }
        List<RefiningFurnaceStructure.MissingBlock> missing = RefiningFurnaceStructure.missingBlocks((LevelReader) level, pos);
        if (!missing.isEmpty()) {
            player.displayClientMessage(Component.translatable("message.friday_cultivation.refining_furnace.incomplete", missing.size()).withStyle(ChatFormatting.GOLD), true);
            if (player instanceof ServerPlayer) {
                ServerPlayer sp = (ServerPlayer) player;
                ModNetwork.CHANNEL.send(PacketDistributor.PLAYER.with(() -> sp), new ToggleRefiningGhostPacket(pos));
            }
            return InteractionResult.CONSUME;
        }
        if (player instanceof ServerPlayer) {
            ServerPlayer sp = (ServerPlayer) player;
            NetworkHooks.openScreen(sp, (MenuProvider) new SimpleMenuProvider((containerId, playerInv, p) -> new com.friday.cultivation.inventory.RefiningMenu(containerId, playerInv, pos), Component.translatable("block.friday_cultivation.refining_core")), buf -> buf.writeBlockPos(pos));
        }
        return InteractionResult.CONSUME;
    }

    @Override
    public void onRemove(@NotNull BlockState state, @NotNull Level level, @NotNull BlockPos pos, @NotNull BlockState newState, boolean moved) {
        BlockEntity be;
        if (!state.is(newState.getBlock()) && (be = level.getBlockEntity(pos)) instanceof RefiningCoreBlockEntity ace) {
            Containers.dropContents((Level) level, (BlockPos) pos, (Container) ace.getIoContainer());
        }
        super.onRemove(state, level, pos, newState, moved);
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, @Nullable BlockGetter level, @NotNull List<Component> tooltip, @NotNull TooltipFlag flag) {
        super.appendHoverText(stack, level, tooltip, flag);
        com.friday.cultivation.util.TooltipUtils.addSection(tooltip, "tooltip.friday_cultivation.section.usage");
        tooltip.add(com.friday.cultivation.util.TooltipUtils.hintLine(Component.translatable("tooltip.friday_cultivation.refining_core.hint")));
        com.friday.cultivation.util.TooltipUtils.addSection(tooltip, "tooltip.friday_cultivation.section.structure");
        tooltip.add(com.friday.cultivation.util.TooltipUtils.statsLine(Component.translatable("tooltip.friday_cultivation.refining_core.structure")));
    }
}
