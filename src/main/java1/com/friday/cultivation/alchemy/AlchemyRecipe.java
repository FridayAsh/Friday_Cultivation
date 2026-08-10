package com.friday.cultivation.alchemy;

import com.friday.cultivation.registry.ModRecipes;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import javax.annotation.Nullable;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;

public final class AlchemyRecipe implements Recipe<SimpleContainer> {
    public static final int MAX_PILLS_PER_BATCH = 64;
    private final ResourceLocation id;
    private final List<IngredientEntry> ingredients;
    private final int qiCostPerPill;
    private final Map<PillTier, Item> outputs;
    private final String translationKey;
    private final int sortOrder;

    public AlchemyRecipe(ResourceLocation id, List<IngredientEntry> ingredients, int qiCostPerPill,
                         Map<PillTier, Item> outputs, @Nullable String translationKey, int sortOrder) {
        this.id = id;
        this.ingredients = List.copyOf(ingredients);
        this.qiCostPerPill = Math.max(0, qiCostPerPill);
        this.outputs = Map.copyOf(outputs);
        this.translationKey = translationKey == null || translationKey.isBlank() ? AlchemyRecipe.defaultTranslationKey(id) : translationKey;
        this.sortOrder = sortOrder;
    }

    public String id() { return this.id.toString(); }
    public int qiCostPerPill() { return this.qiCostPerPill; }
    public Map<PillTier, Item> outputs() { return this.outputs; }
    public int sortOrder() { return this.sortOrder; }
    public List<IngredientEntry> ingredientList() { return this.ingredients; }
    public Component displayName() { return Component.translatable(this.translationKey); }

    public Item iconItem() {
        Item mid = this.outputs.get(PillTier.MID);
        if (mid != null) {
            return mid;
        }
        for (Item it : this.outputs.values()) {
            if (it == null) continue;
            return it;
        }
        return Items.AIR;
    }

    public boolean matchesIngredients(SimpleContainer input, int inputSlots) {
        if (this.ingredients.isEmpty()) {
            return false;
        }
        boolean[] matched = new boolean[this.ingredients.size()];
        int nonEmpty = 0;
        for (int i = 0; i < inputSlots && i < input.getContainerSize(); ++i) {
            ItemStack stack = input.getItem(i);
            if (stack.isEmpty()) continue;
            ++nonEmpty;
            boolean stackMatched = false;
            for (int j = 0; j < this.ingredients.size(); ++j) {
                if (!this.ingredients.get(j).ingredient().test(stack)) continue;
                matched[j] = true;
                stackMatched = true;
                break;
            }
            if (!stackMatched) return false;
        }
        if (nonEmpty == 0) {
            return false;
        }
        for (boolean found : matched) {
            if (!found) return false;
        }
        return true;
    }

    public int countPossiblePills(SimpleContainer input, long availableQi) {
        int maxByIngredient = Integer.MAX_VALUE;
        for (IngredientEntry entry : this.ingredients) {
            int stock = AlchemyRecipe.countInContainer(input, entry.ingredient());
            int possible = stock / Math.max(1, entry.count());
            if (possible >= maxByIngredient) continue;
            maxByIngredient = possible;
        }
        int maxByQi = this.qiCostPerPill > 0 ? (int) Math.min(Integer.MAX_VALUE, availableQi / (long) this.qiCostPerPill) : Integer.MAX_VALUE;
        return Math.max(0, Math.min(MAX_PILLS_PER_BATCH, Math.min(maxByIngredient, maxByQi)));
    }

    public void deductIngredients(SimpleContainer input, int pillCount) {
        for (IngredientEntry entry : this.ingredients) {
            int remaining = entry.count() * pillCount;
            for (int i = 0; i < input.getContainerSize() && remaining > 0; ++i) {
                ItemStack stack = input.getItem(i);
                if (stack.isEmpty() || !entry.ingredient().test(stack)) continue;
                int take = Math.min(stack.getCount(), remaining);
                stack.shrink(take);
                remaining -= take;
            }
        }
    }

    private static int countInContainer(SimpleContainer c, Ingredient ingredient) {
        int count = 0;
        for (int i = 0; i < c.getContainerSize(); ++i) {
            ItemStack s = c.getItem(i);
            if (s.isEmpty() || !ingredient.test(s)) continue;
            count += s.getCount();
        }
        return count;
    }

    public ResourceLocation rl() { return this.id; }

    @Override public boolean matches(@NotNull SimpleContainer input, @NotNull Level level) { return this.matchesIngredients(input, input.getContainerSize()); }
    @Override public ItemStack assemble(@NotNull SimpleContainer input, @NotNull RegistryAccess access) { return new ItemStack((ItemLike) this.iconItem()); }
    @Override public boolean canCraftInDimensions(int width, int height) { return width * height >= this.ingredients.size(); }
    @Override public ItemStack getResultItem(@NotNull RegistryAccess access) { return new ItemStack((ItemLike) this.iconItem()); }
    @Override public ResourceLocation getId() { return this.id; }
    @Override public RecipeSerializer<?> getSerializer() { return ModRecipes.ALCHEMY_SERIALIZER.get(); }
    @Override public RecipeType<?> getType() { return ModRecipes.ALCHEMY_TYPE.get(); }

    private static List<IngredientEntry> readIngredients(JsonObject json, ResourceLocation recipeId) {
        JsonArray arr = GsonHelper.getAsJsonArray(json, "ingredients");
        if (arr.isEmpty()) {
            throw new IllegalArgumentException("alchemy recipe " + String.valueOf(recipeId) + " has no ingredients");
        }
        ArrayList<IngredientEntry> entries = new ArrayList<>();
        for (JsonElement element : arr) {
            JsonElement ingredientJson;
            JsonObject obj = GsonHelper.convertToJsonObject(element, "ingredient");
            int count = GsonHelper.getAsInt(obj, "count", 1);
            if (obj.has("ingredient")) {
                ingredientJson = obj.get("ingredient");
            } else {
                JsonObject copy = obj.deepCopy();
                copy.remove("count");
                ingredientJson = copy;
            }
            Ingredient ingredient = Ingredient.fromJson(ingredientJson);
            if (ingredient.isEmpty()) {
                throw new IllegalArgumentException("alchemy recipe " + String.valueOf(recipeId) + " has an empty ingredient");
            }
            entries.add(new IngredientEntry(ingredient, count));
        }
        return entries;
    }

    private static Map<PillTier, Item> readOutputs(JsonObject json, ResourceLocation recipeId) {
        JsonObject obj = GsonHelper.getAsJsonObject(json, "outputs");
        EnumMap<PillTier, Item> outputs = new EnumMap<>(PillTier.class);
        for (PillTier tier : PillTier.values()) {
            String itemName = GsonHelper.getAsString(obj, tier.id());
            ResourceLocation itemId = new ResourceLocation(itemName);
            Item item = ForgeRegistries.ITEMS.getValue(itemId);
            if (item == null || item == Items.AIR) {
                throw new IllegalArgumentException("alchemy recipe " + String.valueOf(recipeId) + " references unknown output item " + itemName);
            }
            outputs.put(tier, item);
        }
        return outputs;
    }

    private static String defaultTranslationKey(ResourceLocation id) {
        return "recipe." + id.getNamespace() + "." + id.getPath().replace('/', '.');
    }

    public static String itemName(Item item) {
        ResourceLocation key = ForgeRegistries.ITEMS.getKey(item);
        return key == null ? "?" : key.toString();
    }

    public record IngredientEntry(Ingredient ingredient, int count) {
        public IngredientEntry {
            count = Math.max(1, count);
        }

        public Item item() {
            ItemStack[] stacks = this.ingredient.getItems();
            return stacks.length == 0 ? Items.AIR : stacks[0].getItem();
        }
    }

    public static final class Serializer implements RecipeSerializer<AlchemyRecipe> {
        @NotNull
        @Override
        public AlchemyRecipe fromJson(@NotNull ResourceLocation recipeId, @NotNull JsonObject json) {
            List<IngredientEntry> ingredients = AlchemyRecipe.readIngredients(json, recipeId);
            int qiCost = GsonHelper.getAsInt(json, "qi_cost_per_pill", GsonHelper.getAsInt(json, "qi_cost", 0));
            Map<PillTier, Item> outputs = AlchemyRecipe.readOutputs(json, recipeId);
            String translationKey = GsonHelper.getAsString(json, "translation_key", null);
            int sortOrder = GsonHelper.getAsInt(json, "sort_order", 1000);
            return new AlchemyRecipe(recipeId, ingredients, qiCost, outputs, translationKey, sortOrder);
        }

        @Nullable
        @Override
        public AlchemyRecipe fromNetwork(@NotNull ResourceLocation recipeId, @NotNull FriendlyByteBuf buf) {
            int ingredientCount = buf.readVarInt();
            ArrayList<IngredientEntry> ingredients = new ArrayList<>(ingredientCount);
            for (int i = 0; i < ingredientCount; ++i) {
                ingredients.add(new IngredientEntry(Ingredient.fromNetwork(buf), buf.readVarInt()));
            }
            int qiCost = buf.readVarInt();
            int outputCount = buf.readVarInt();
            EnumMap<PillTier, Item> outputs = new EnumMap<>(PillTier.class);
            for (int i = 0; i < outputCount; ++i) {
                PillTier tier = buf.readEnum(PillTier.class);
                Item item = ForgeRegistries.ITEMS.getValue(buf.readResourceLocation());
                if (item == null) continue;
                outputs.put(tier, item);
            }
            String translationKey = buf.readUtf(256);
            int sortOrder = buf.readVarInt();
            return new AlchemyRecipe(recipeId, ingredients, qiCost, outputs, translationKey, sortOrder);
        }

        @Override
        public void toNetwork(@NotNull FriendlyByteBuf buf, @NotNull AlchemyRecipe recipe) {
            buf.writeVarInt(recipe.ingredients.size());
            for (IngredientEntry ingredientEntry : recipe.ingredients) {
                ingredientEntry.ingredient().toNetwork(buf);
                buf.writeVarInt(ingredientEntry.count());
            }
            buf.writeVarInt(recipe.qiCostPerPill);
            buf.writeVarInt(recipe.outputs.size());
            for (Map.Entry<PillTier, Item> entry : recipe.outputs.entrySet()) {
                buf.writeEnum(entry.getKey());
                ResourceLocation itemId = ForgeRegistries.ITEMS.getKey(entry.getValue());
                buf.writeResourceLocation(itemId == null ? new ResourceLocation("minecraft", "air") : itemId);
            }
            buf.writeUtf(recipe.translationKey, 256);
            buf.writeVarInt(recipe.sortOrder);
        }
    }
}
