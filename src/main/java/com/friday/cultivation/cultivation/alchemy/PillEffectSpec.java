/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.JsonObject
 *  net.minecraft.util.GsonHelper
 *  org.jetbrains.annotations.Nullable
 */
package com.friday.cultivation.cultivation.alchemy;

import com.google.gson.JsonObject;
import net.minecraft.util.GsonHelper;
import org.jetbrains.annotations.Nullable;

public record PillEffectSpec(@Nullable Integer qi, @Nullable Float heal, @Nullable Boolean healFull, @Nullable Integer regenerationTicks, @Nullable Integer regenerationAmplifier, @Nullable Integer absorptionTicks, @Nullable Integer absorptionAmplifier) {
    public static PillEffectSpec fromJson(JsonObject json) {
        Integer qi = json.has("qi") ? Integer.valueOf(GsonHelper.getAsInt((JsonObject)json, (String)"qi")) : null;
        Float heal = json.has("heal") ? Float.valueOf(GsonHelper.getAsFloat((JsonObject)json, "heal")) : null;
        Boolean healFull = json.has("heal_full") ? Boolean.valueOf(GsonHelper.getAsBoolean((JsonObject)json, (String)"heal_full")) : null;
        Integer regenerationTicks = json.has("regeneration_ticks") ? Integer.valueOf(GsonHelper.getAsInt((JsonObject)json, (String)"regeneration_ticks")) : null;
        Integer regenerationAmplifier = json.has("regeneration_amplifier") ? Integer.valueOf(GsonHelper.getAsInt((JsonObject)json, (String)"regeneration_amplifier")) : null;
        Integer absorptionTicks = json.has("absorption_ticks") ? Integer.valueOf(GsonHelper.getAsInt((JsonObject)json, (String)"absorption_ticks")) : null;
        Integer absorptionAmplifier = json.has("absorption_amplifier") ? Integer.valueOf(GsonHelper.getAsInt((JsonObject)json, (String)"absorption_amplifier")) : null;
        return new PillEffectSpec(qi, heal, healFull, regenerationTicks, regenerationAmplifier, absorptionTicks, absorptionAmplifier);
    }
}

