package com.friday.cultivation.alchemy;

import com.friday.cultivation.registry.ModRecipes;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;

public final class AlchemyRecipes {
    private AlchemyRecipes() {
    }

    public static List<AlchemyRecipe> all(Level level) {
        if (level == null) {
            return List.of();
        }
        return level.getRecipeManager().getAllRecipesFor((RecipeType) ModRecipes.ALCHEMY_TYPE.get()).stream().sorted(Comparator.comparingInt(AlchemyRecipe::sortOrder).thenComparing(recipe -> recipe.getId().toString())).toList();
    }

    public static Optional<AlchemyRecipe> byId(Level level, String id) {
        if (level == null || id == null || id.isBlank()) {
            return Optional.empty();
        }
        ResourceLocation recipeId = AlchemyRecipes.parseRecipeId(id);
        if (recipeId == null) {
            return Optional.empty();
        }
        return level.getRecipeManager().byKey(recipeId).filter(AlchemyRecipe.class::isInstance).map(AlchemyRecipe.class::cast);
    }

    private static ResourceLocation parseRecipeId(String id) {
        ResourceLocation parsed = ResourceLocation.tryParse(id);
        if (parsed != null && id.contains(":")) {
            return parsed;
        }
        return ResourceLocation.tryParse("friday_cultivation:" + id);
    }
}
