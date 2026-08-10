package com.friday.cultivation.item;

import com.friday.cultivation.entity.npc.WanderingCultivatorEntity;
import com.friday.cultivation.realm.Realm;
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
 * 散修生成蛋 - 严格 1:1 复刻原 mod CultivatorSpawnEggItem。
 * 携带一个 Realm，用于 spawn 时强制注入 realm id。
 */
public class CultivatorSpawnEggItem
extends ForgeSpawnEggItem {
    private final Realm realm;

    public CultivatorSpawnEggItem(Realm realm, int bgColor, int hlColor, Item.Properties props) {
        super((Supplier) ModEntities.WANDERING_CULTIVATOR, bgColor, hlColor, props);
        this.realm = realm;
    }

    public Realm getRealm() {
        return this.realm;
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
        tag.putString("forcedRealmId", this.realm.id());
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
        tooltip.add(TooltipUtils.statsLine(Component.translatable("tooltip.friday_cultivation.spawn_egg_cultivator", this.realm.displayName())));
        TooltipUtils.addBlank(tooltip);
        tooltip.add(TooltipUtils.hintLine(Component.translatable("tooltip.friday_cultivation.spawn_egg_cultivator.hint")));
    }
}
