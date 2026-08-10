package com.friday.cultivation.block.formation;

import com.friday.cultivation.ItemTier;
import com.friday.cultivation.qi.formation.CoreTier;
import com.friday.cultivation.inventory.FormationMenu;
import com.friday.cultivation.item.FormationCompassItem;
import com.friday.cultivation.registry.ModBlockEntities;
import com.friday.cultivation.util.TooltipUtils;
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

import java.util.List;

/**
 * 阵法核心盘方块 - 对应 ModBlockEntities.FORMATION_CORE_PLATE，
 * 包含核心灵气容量与最大可连接旗数；以 CoreTier 区分品阶。
 * 严格 1:1 复刻原 mod FormationCorePlateBlock。
 */
public class FormationCorePlateBlock
extends Block
implements EntityBlock {
    public static final VoxelShape SHAPE = Shapes.box(0.0, 0.0, 0.0, 1.0, 0.1875, 1.0);
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
            case LOW -> 1;
            case MID -> 5;
            case HIGH -> 10;
            case SUPREME -> 30;
            case IMMORTAL -> 50;
        };
    }

    /** maxLinkedFlagsFor 公开包裹（被 FormationCorePlateBlockEntity 使用） */
    public static int maxLinkedFlagsPublic(CoreTier tier) {
        return maxLinkedFlagsFor(tier);
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, @Nullable BlockGetter level, @NotNull List<Component> tooltip, @NotNull TooltipFlag flag) {
        super.appendHoverText(stack, level, tooltip, flag);
        Component maxText = Component.literal(Long.toString(this.tier.maxQi()));
        tooltip.add(TooltipUtils.tieredName(Component.translatable("tooltip.friday_cultivation.formation_core_plate.title"), (ItemTier)this.tier.itemTier()));
        TooltipUtils.addBlank(tooltip);
        TooltipUtils.addSection(tooltip, "tooltip.friday_cultivation.section.effect");
        tooltip.add(TooltipUtils.descriptionLine(Component.translatable("tooltip.friday_cultivation.formation_core_plate.summary")));
        TooltipUtils.addSection(tooltip, "tooltip.friday_cultivation.section.stats");
        tooltip.add(TooltipUtils.statsLine(Component.translatable("tooltip.friday_cultivation.formation_core_plate.capacity", maxText)));
        tooltip.add(TooltipUtils.statsLine(Component.translatable("tooltip.friday_cultivation.formation_core_plate.max_flags", FormationCorePlateBlock.maxLinkedFlagsFor(this.tier))));
        TooltipUtils.addBlank(tooltip);
        tooltip.add(TooltipUtils.hintLine(Component.translatable("tooltip.friday_cultivation.formation_core_plate.link")));
        tooltip.add(TooltipUtils.hintLine(Component.translatable("tooltip.friday_cultivation.formation_core_plate.activate")));
    }

    @NotNull
    @Override
    public VoxelShape getShape(@NotNull BlockState state, @NotNull BlockGetter level, @NotNull BlockPos pos, @NotNull CollisionContext ctx) {
        return SHAPE;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(@NotNull BlockPos pos, @NotNull BlockState state) {
        return new FormationCorePlateBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(@NotNull Level level, @NotNull BlockState state, @NotNull BlockEntityType<T> type) {
        if (level.isClientSide()) {
            return null;
        }
        return type == ModBlockEntities.FORMATION_CORE_PLATE.get() ? (lvl, pos, st, be) -> ((FormationCorePlateBlockEntity)be).serverTick() : null;
    }

    @NotNull
    @Override
    public InteractionResult use(@NotNull BlockState state, @NotNull Level level, @NotNull BlockPos pos, @NotNull Player player, @NotNull InteractionHand hand, @NotNull BlockHitResult hit) {
        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }
        BlockEntity be = level.getBlockEntity(pos);
        if (!(be instanceof FormationCorePlateBlockEntity)) {
            return InteractionResult.PASS;
        }
        FormationCorePlateBlockEntity core = (FormationCorePlateBlockEntity) be;
        if (player.isShiftKeyDown() && player.getItemInHand(hand).getItem() instanceof FormationCompassItem) {
            FormationCompassItem.lockCore(player.getItemInHand(hand), level, pos, player, core);
            return InteractionResult.CONSUME;
        }
        if (player instanceof ServerPlayer sp) {
            final String coreName = core.getCustomName();
            NetworkHooks.openScreen(sp, new SimpleMenuProvider(
                    (containerId, playerInv, p) -> new FormationMenu(containerId, playerInv, pos),
                    state.getBlock().getName()), buf -> {
                buf.writeBlockPos(pos);
                buf.writeUtf(coreName == null ? "" : coreName, 32);
            });
        }
        return InteractionResult.CONSUME;
    }

    @Override
    public void onRemove(BlockState oldState, @NotNull Level level, @NotNull BlockPos pos, BlockState newState, boolean isMoving) {
        if (!oldState.is(newState.getBlock())) {
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof FormationCorePlateBlockEntity core) {
                core.onBlockRemoved();
            }
        }
        super.onRemove(oldState, level, pos, newState, isMoving);
    }
}
