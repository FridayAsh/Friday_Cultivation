package com.friday.cultivation.block.formation;

import com.friday.cultivation.ItemTier;
import com.friday.cultivation.qi.formation.FormationType;
import net.minecraft.world.level.block.state.BlockBehaviour;

public class SectProtectionFlagBlock extends FormationFlagBlock {
    private final ItemTier tier;

    public SectProtectionFlagBlock(BlockBehaviour.Properties properties, ItemTier tier) {
        super(properties);
        this.tier = tier;
    }

    @Override
    public FormationType formationType() {
        return FormationType.SECT_PROTECTION;
    }

    @Override
    public ItemTier flagTier() {
        return this.tier;
    }
}