package com.friday.cultivation.refining;

import com.friday.cultivation.registry.ModRecipes;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;

public final class RefiningRecipes {
    private RefiningRecipes() {
    }

    public static List<RefiningRecipe> all(Level level) {
        if (level == null) {
            return List.of();
        }
        return level.getRecipeManager().getAllRecipesFor((RecipeType) ModRecipes.REFINING_TYPE.get()).stream().sorted(Comparator.comparingInt(RefiningRecipe::sortOrder).thenComparing(recipe -> recipe.getId().toString())).toList();
    }

    public static Optional<RefiningRecipe> byId(Level level, String id) {
        if (level == null || id == null || id.isBlank()) {
            return Optional.empty();
        }
        ResourceLocation recipeId = RefiningRecipes.parseRecipeId(id);
        if (recipeId == null) {
            return Optional.empty();
        }
        return level.getRecipeManager().byKey(recipeId).filter(RefiningRecipe.class::isInstance).map(RefiningRecipe.class::cast);
    }

    private static ResourceLocation parseRecipeId(String id) {
        ResourceLocation parsed = ResourceLocation.tryParse(id);
        if (parsed != null && id.contains(":")) {
            return parsed;
        }
        return ResourceLocation.tryParse("friday_cultivation:" + id);
    }
}
