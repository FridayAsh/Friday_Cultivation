package com.friday.cultivation.alchemy;

import com.google.gson.JsonObject;
import net.minecraft.util.GsonHelper;
import org.jetbrains.annotations.Nullable;

/**
 * 丹药效果规格（严格照搬原模组 com.xiaoxiang.cultivation.cultivation.alchemy.PillEffectSpec）
 */
public record PillEffectSpec(@Nullable Integer qi, @Nullable Float heal, @Nullable Boolean healFull,
                              @Nullable Integer regenerationTicks, @Nullable Integer regenerationAmplifier,
                              @Nullable Integer absorptionTicks, @Nullable Integer absorptionAmplifier) {
    public static PillEffectSpec fromJson(JsonObject json) {
        Integer qi = json.has("qi") ? GsonHelper.getAsInt(json, "qi") : null;
        Float heal = json.has("heal") ? GsonHelper.getAsFloat(json, "heal") : null;
        Boolean healFull = json.has("heal_full") ? GsonHelper.getAsBoolean(json, "heal_full") : null;
        Integer regenerationTicks = json.has("regeneration_ticks") ? GsonHelper.getAsInt(json, "regeneration_ticks") : null;
        Integer regenerationAmplifier = json.has("regeneration_amplifier") ? GsonHelper.getAsInt(json, "regeneration_amplifier") : null;
        Integer absorptionTicks = json.has("absorption_ticks") ? GsonHelper.getAsInt(json, "absorption_ticks") : null;
        Integer absorptionAmplifier = json.has("absorption_amplifier") ? GsonHelper.getAsInt(json, "absorption_amplifier") : null;
        return new PillEffectSpec(qi, heal, healFull, regenerationTicks, regenerationAmplifier, absorptionTicks, absorptionAmplifier);
    }
}