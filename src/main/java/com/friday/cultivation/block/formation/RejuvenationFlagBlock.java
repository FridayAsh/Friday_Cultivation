/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.world.level.block.state.BlockBehaviour$Properties
 */
package com.friday.cultivation.block.formation;

import com.friday.cultivation.block.formation.FormationFlagBlock;
import com.friday.cultivation.cultivation.ItemTier;
import com.friday.cultivation.cultivation.qi.formation.FormationType;
import net.minecraft.world.level.block.state.BlockBehaviour;

public class RejuvenationFlagBlock
extends FormationFlagBlock {
    private final ItemTier tier;

    public RejuvenationFlagBlock(BlockBehaviour.Properties properties, ItemTier tier) {
        super(properties);
        this.tier = tier;
    }

    @Override
    public FormationType formationType() {
        return FormationType.REJUVENATION;
    }

    @Override
    public ItemTier flagTier() {
        return this.tier;
    }
}

