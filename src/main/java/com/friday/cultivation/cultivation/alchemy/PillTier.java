/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.ChatFormatting
 *  net.minecraft.network.chat.Component
 *  net.minecraft.world.item.Rarity
 */
package com.friday.cultivation.cultivation.alchemy;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Rarity;

public enum PillTier {
    LOW("low", ChatFormatting.GRAY, Rarity.COMMON),
    MID("mid", ChatFormatting.YELLOW, Rarity.UNCOMMON),
    HIGH("high", ChatFormatting.AQUA, Rarity.RARE),
    SUPREME("supreme", ChatFormatting.LIGHT_PURPLE, Rarity.EPIC),
    IMMORTAL("immortal", ChatFormatting.RED, Rarity.EPIC);

    private final String id;
    private final ChatFormatting color;
    private final Rarity rarity;

    private PillTier(String id, ChatFormatting color, Rarity rarity) {
        this.id = id;
        this.color = color;
        this.rarity = rarity;
    }

    public String id() {
        return this.id;
    }

    public ChatFormatting color() {
        return this.color;
    }

    public Rarity rarity() {
        return this.rarity;
    }

    public Component displayName() {
        return Component.translatable((String)("pill_tier.friday_cultivation." + this.id));
    }
}

