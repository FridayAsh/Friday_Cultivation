package com.friday.cultivation.item;

import com.friday.cultivation.entity.npc.WanderingCultivatorEntity;
import com.friday.cultivation.registry.ModEntities;
import com.friday.cultivation.util.TooltipUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraftforge.common.ForgeSpawnEggItem;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Supplier;

/**
 * 牛头马面生成蛋 - 严格 1:1 复刻原 mod DifuReaperSpawnEggItem。
 * 强制将生成的 WanderingCultivator 标记为 牛头马面 角色。
 */
public class DifuReaperSpawnEggItem
extends ForgeSpawnEggItem {
    public DifuReaperSpawnEggItem(int bgColor, int hlColor, Item.Properties props) {
        super((Supplier) ModEntities.WANDERING_CULTIVATOR, bgColor, hlColor, props);
    }

    @NotNull
    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        if (!(level instanceof ServerLevel)) {
            return InteractionResult.SUCCESS;
        }
        ServerLevel serverLevel = (ServerLevel) level;
        ItemStack stack = context.getItemInHand();
        BlockPos clickedPos = context.getClickedPos();
        Direction face = context.getClickedFace();
        BlockState clickedState = level.getBlockState(clickedPos);
        BlockPos spawnPos = clickedState.getCollisionShape((BlockGetter) level, clickedPos).isEmpty() ? clickedPos : clickedPos.relative(face);
        Player player = context.getPlayer();
        boolean alignToFace = !clickedPos.equals(spawnPos) && face == Direction.UP;
        CompoundTag tag = new CompoundTag();
        tag.putBoolean("forcedDifuReaper", true);
        WanderingCultivatorEntity entity = (WanderingCultivatorEntity) ((EntityType) ModEntities.WANDERING_CULTIVATOR.get()).spawn(serverLevel, tag, null, spawnPos, MobSpawnType.SPAWN_EGG, true, alignToFace);
        if (entity != null) {
            if (player != null && !player.getAbilities().instabuild) {
                stack.shrink(1);
            }
            level.gameEvent((Entity) player, GameEvent.ENTITY_PLACE, clickedPos);
        }
        return InteractionResult.CONSUME;
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, @Nullable Level level, @NotNull List<Component> tooltip, @NotNull TooltipFlag flag) {
        super.appendHoverText(stack, level, tooltip, flag);
        TooltipUtils.addSection(tooltip, "tooltip.friday_cultivation.section.effect");
        tooltip.add(TooltipUtils.statsLine(Component.translatable("tooltip.friday_cultivation.spawn_egg_soul_reaper")));
    }
}
