package com.friday.cultivation.block.formation;

import com.friday.cultivation.ItemTier;
import com.friday.cultivation.qi.formation.FormationType;
import net.minecraft.world.level.block.state.BlockBehaviour;

public class MazeFlagBlock extends FormationFlagBlock {
    private final ItemTier tier;

    public MazeFlagBlock(BlockBehaviour.Properties properties, ItemTier tier) {
        super(properties);
        this.tier = tier;
    }

    @Override
    public FormationType formationType() {
        return FormationType.MAZE;
    }

    @Override
    public ItemTier flagTier() {
        return this.tier;
    }
}