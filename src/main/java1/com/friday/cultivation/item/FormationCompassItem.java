package com.friday.cultivation.item;

import com.friday.cultivation.block.formation.FormationCorePlateBlockEntity;
import com.friday.cultivation.block.formation.FormationFlagBlock;
import com.friday.cultivation.registry.ModParticles;
import com.friday.cultivation.util.TooltipUtils;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class FormationCompassItem
extends Item {
    public static final int LOCK_DURATION_TICKS = 1200;
    private static final String TAG_LOCK = "FormationCompassLock";
    private static final String TAG_DIM = "Dim";
    private static final String TAG_X = "X";
    private static final String TAG_Y = "Y";
    private static final String TAG_Z = "Z";
    private static final String TAG_UNTIL = "Until";

    public FormationCompassItem(Item.Properties properties) {
        super(properties);
    }

    @NotNull
    @Override
    public InteractionResult useOn(@NotNull UseOnContext context) {
        Level level = context.getLevel();
        Player player = context.getPlayer();
        if (player == null) {
            return InteractionResult.PASS;
        }
        BlockPos pos = context.getClickedPos();
        BlockState state = level.getBlockState(pos);
        ItemStack stack = context.getItemInHand();
        Block block = state.getBlock();
        if (block instanceof FormationFlagBlock) {
            FormationFlagBlock flag = (FormationFlagBlock) block;
            if (!level.isClientSide()) {
                FormationCompassItem.tryLinkFlag(stack, level, pos, player, flag);
            }
            return InteractionResult.sidedSuccess(level.isClientSide());
        }
        if (player.isShiftKeyDown()) {
            BlockEntity blockEntity = level.getBlockEntity(pos);
            if (blockEntity instanceof FormationCorePlateBlockEntity) {
                FormationCorePlateBlockEntity core = (FormationCorePlateBlockEntity) blockEntity;
                if (!level.isClientSide()) {
                    FormationCompassItem.lockCore(stack, level, pos, player, core);
                }
                return InteractionResult.sidedSuccess(level.isClientSide());
            }
        }
        return InteractionResult.PASS;
    }

    public static void lockCore(ItemStack stack, Level level, BlockPos corePos, Player player, FormationCorePlateBlockEntity core) {
        CompoundTag lock = new CompoundTag();
        lock.putString(TAG_DIM, level.dimension().location().toString());
        lock.putInt(TAG_X, corePos.getX());
        lock.putInt(TAG_Y, corePos.getY());
        lock.putInt(TAG_Z, corePos.getZ());
        lock.putLong(TAG_UNTIL, level.getGameTime() + 1200L);
        stack.getOrCreateTag().put(TAG_LOCK, lock);
        core.showCompassLockGlowUntil(level.getGameTime() + 1200L);
        FormationCompassItem.syncHeldItem(player);
        player.displayClientMessage(Component.translatable("message.friday_cultivation.formation_compass.locked", corePos.getX(), corePos.getY(), corePos.getZ()).withStyle(ChatFormatting.AQUA), false);
        if (level instanceof ServerLevel) {
            ServerLevel sl = (ServerLevel) level;
            Vec3 center = Vec3.atCenterOf(corePos);
            sl.sendParticles(ModParticles.AMBIENT_QI.get(), center.x, center.y + 0.25, center.z, 24, 0.35, 0.12, 0.35, 0.03);
            sl.playSound(null, corePos, SoundEvents.ENCHANTMENT_TABLE_USE, SoundSource.BLOCKS, 0.55f, 1.35f);
        }
    }

    public static InteractionResult tryLinkFlag(ItemStack stack, Level level, BlockPos flagPos, Player player, FormationFlagBlock flag) {
        if (!(level instanceof ServerLevel)) {
            return InteractionResult.SUCCESS;
        }
        ServerLevel sl = (ServerLevel) level;
        BlockPos corePos = FormationCompassItem.getLockedCorePos(stack, level);
        if (corePos == null) {
            FormationCompassItem.clearLock(stack);
            player.displayClientMessage(Component.translatable("message.friday_cultivation.formation_compass.lock_expired").withStyle(ChatFormatting.RED), false);
            return InteractionResult.FAIL;
        }
        BlockEntity blockEntity = level.getBlockEntity(corePos);
        if (!(blockEntity instanceof FormationCorePlateBlockEntity)) {
            FormationCompassItem.clearLock(stack);
            player.displayClientMessage(Component.translatable("message.friday_cultivation.formation_compass.core_missing").withStyle(ChatFormatting.RED), false);
            return InteractionResult.FAIL;
        }
        FormationCorePlateBlockEntity core = (FormationCorePlateBlockEntity) blockEntity;
        FormationCorePlateBlockEntity.LinkFlagResult result = core.linkFlag(flagPos, flag);
        MutableComponent message = switch (result) {
            case SUCCESS -> Component.translatable("message.friday_cultivation.formation_compass.flag_linked").withStyle(ChatFormatting.GREEN);
            case ALREADY_LINKED -> Component.translatable("message.friday_cultivation.formation_compass.flag_already_linked").withStyle(ChatFormatting.YELLOW);
            case CORE_ACTIVE -> Component.translatable("message.friday_cultivation.formation_compass.core_active").withStyle(ChatFormatting.RED);
            case MAX_LINKS -> Component.translatable("message.friday_cultivation.formation_compass.max_links", core.maxLinkedFlags()).withStyle(ChatFormatting.RED);
            case INVALID_FLAG -> Component.translatable("message.friday_cultivation.formation_compass.invalid_flag").withStyle(ChatFormatting.RED);
        };
        player.displayClientMessage(message, false);
        if (result == FormationCorePlateBlockEntity.LinkFlagResult.SUCCESS || result == FormationCorePlateBlockEntity.LinkFlagResult.ALREADY_LINKED) {
            FormationCompassItem.spawnLinkParticles(sl, flagPos, corePos);
            sl.playSound(null, flagPos, SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.BLOCKS, 0.45f, 1.65f);
        }
        return result == FormationCorePlateBlockEntity.LinkFlagResult.SUCCESS || result == FormationCorePlateBlockEntity.LinkFlagResult.ALREADY_LINKED ? InteractionResult.SUCCESS : InteractionResult.FAIL;
    }

    private static void syncHeldItem(Player player) {
        player.getInventory().setChanged();
        if (player instanceof ServerPlayer) {
            ServerPlayer serverPlayer = (ServerPlayer) player;
            serverPlayer.containerMenu.broadcastChanges();
            serverPlayer.inventoryMenu.broadcastChanges();
        }
    }

    @Nullable
    public static BlockPos getLockedCorePos(ItemStack stack, Level level) {
        CompoundTag lock = FormationCompassItem.lockTag(stack);
        if (lock == null) {
            return null;
        }
        if (!level.dimension().location().toString().equals(lock.getString(TAG_DIM))) {
            return null;
        }
        if (level.getGameTime() > lock.getLong(TAG_UNTIL)) {
            return null;
        }
        return new BlockPos(lock.getInt(TAG_X), lock.getInt(TAG_Y), lock.getInt(TAG_Z));
    }

    public static void clearLock(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        if (tag == null) {
            return;
        }
        tag.remove(TAG_LOCK);
        if (tag.isEmpty()) {
            stack.setTag(null);
        }
    }

    @Nullable
    private static CompoundTag lockTag(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        if (tag == null || !tag.contains(TAG_LOCK, 10)) {
            return null;
        }
        return tag.getCompound(TAG_LOCK);
    }

    private static void spawnLinkParticles(ServerLevel sl, BlockPos flagPos, BlockPos corePos) {
        Vec3 from = Vec3.atCenterOf(flagPos).add(0.0, 0.65, 0.0);
        Vec3 to = Vec3.atCenterOf(corePos).add(0.0, 0.25, 0.0);
        Vec3 delta = to.subtract(from);
        int steps = Math.max(12, Math.min(72, (int) Math.ceil(delta.length() * 2.0)));
        for (int i = 0; i <= steps; ++i) {
            double t = (double) i / (double) steps;
            Vec3 p = from.add(delta.scale(t));
            sl.sendParticles(ModParticles.AMBIENT_QI.get(), p.x, p.y, p.z, 1, 0.025, 0.025, 0.025, 0.002);
        }
        sl.sendParticles(ModParticles.QI_ABSORB.get(), from.x, from.y, from.z, 10, 0.16, 0.18, 0.16, 0.03);
        sl.sendParticles(ModParticles.QI_ABSORB.get(), to.x, to.y, to.z, 8, 0.14, 0.1, 0.14, 0.025);
    }

    @Override
    public void inventoryTick(@NotNull ItemStack stack, @NotNull Level level, @NotNull Entity entity, int slot, boolean selected) {
        if (!level.isClientSide() && FormationCompassItem.lockTag(stack) != null && FormationCompassItem.getLockedCorePos(stack, level) == null) {
            FormationCompassItem.clearLock(stack);
        }
    }

    @Override
    public boolean isFoil(@NotNull ItemStack stack) {
        return FormationCompassItem.lockTag(stack) != null || super.isFoil(stack);
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, @Nullable Level level, @NotNull List<Component> tooltip, @NotNull TooltipFlag flag) {
        BlockPos locked;
        TooltipUtils.addSection(tooltip, "tooltip.friday_cultivation.section.usage");
        tooltip.add(TooltipUtils.descriptionLine(Component.translatable("tooltip.friday_cultivation.formation_compass")));
        tooltip.add(TooltipUtils.hintLine(Component.translatable("tooltip.friday_cultivation.formation_compass.hint_lock")));
        tooltip.add(TooltipUtils.hintLine(Component.translatable("tooltip.friday_cultivation.formation_compass.hint_survey")));
        if (level != null && (locked = FormationCompassItem.getLockedCorePos(stack, level)) != null) {
            TooltipUtils.addSection(tooltip, "tooltip.friday_cultivation.section.status");
            tooltip.add(TooltipUtils.statsLine(Component.translatable("tooltip.friday_cultivation.formation_compass.locked", locked.getX(), locked.getY(), locked.getZ())));
        }
    }
}
