package com.friday.cultivation.refining;

import com.friday.cultivation.ItemTier;
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

public final class RefiningRecipe implements Recipe<SimpleContainer> {
    public static final int MAX_ITEMS_PER_BATCH = 1;
    private final ResourceLocation id;
    private final List<IngredientEntry> ingredients;
    private final int qiCostPerItem;
    private final Map<ItemTier, Item> outputs;
    private final String translationKey;
    private final int sortOrder;

    public RefiningRecipe(ResourceLocation id, List<IngredientEntry> ingredients, int qiCostPerItem,
                          Map<ItemTier, Item> outputs, @Nullable String translationKey, int sortOrder) {
        this.id = id;
        this.ingredients = List.copyOf(ingredients);
        this.qiCostPerItem = Math.max(0, qiCostPerItem);
        this.outputs = Map.copyOf(outputs);
        this.translationKey = translationKey == null || translationKey.isBlank() ? RefiningRecipe.defaultTranslationKey(id) : translationKey;
        this.sortOrder = sortOrder;
    }

    public String id() { return this.id.toString(); }
    public int qiCostPerItem() { return this.qiCostPerItem; }
    public Map<ItemTier, Item> outputs() { return this.outputs; }
    public int sortOrder() { return this.sortOrder; }
    public List<IngredientEntry> ingredientList() { return this.ingredients; }

    public Component displayName() {
        return Component.translatable(this.translationKey);
    }

    public Item iconItem() {
        Item mid = this.outputs.get(ItemTier.MID);
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
            if (stackMatched) continue;
            return false;
        }
        if (nonEmpty == 0) {
            return false;
        }
        for (boolean found : matched) {
            if (found) continue;
            return false;
        }
        return true;
    }

    public int countPossibleItems(SimpleContainer input, long availableQi) {
        for (IngredientEntry entry : this.ingredients) {
            int stock = RefiningRecipe.countInContainer(input, entry.ingredient());
            if (stock >= entry.count()) continue;
            return 0;
        }
        if (this.qiCostPerItem > 0 && availableQi < (long) this.qiCostPerItem) {
            return 0;
        }
        return 1;
    }

    public void deductIngredients(SimpleContainer input, int itemCount) {
        for (IngredientEntry entry : this.ingredients) {
            int remaining = entry.count() * itemCount;
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

    @NotNull
    @Override public ItemStack assemble(@NotNull SimpleContainer input, @NotNull RegistryAccess access) {
        return new ItemStack((ItemLike) this.iconItem());
    }

    @Override public boolean canCraftInDimensions(int width, int height) { return width * height >= this.ingredients.size(); }

    @NotNull
    @Override public ItemStack getResultItem(@NotNull RegistryAccess access) {
        return new ItemStack((ItemLike) this.iconItem());
    }

    @NotNull
    @Override public ResourceLocation getId() { return this.id; }

    @NotNull
    @Override public RecipeSerializer<?> getSerializer() {
        return ModRecipes.REFINING_SERIALIZER.get();
    }

    @NotNull
    @Override public RecipeType<?> getType() {
        return ModRecipes.REFINING_TYPE.get();
    }

    private static List<IngredientEntry> readIngredients(JsonObject json, ResourceLocation recipeId) {
        JsonArray arr = GsonHelper.getAsJsonArray(json, "ingredients");
        if (arr.isEmpty()) {
            throw new IllegalArgumentException("refining recipe " + recipeId + " has no ingredients");
        }
        ArrayList<IngredientEntry> entries = new ArrayList<>();
        for (JsonElement element : arr) {
            JsonObject obj = GsonHelper.convertToJsonObject(element, "ingredient");
            int count = GsonHelper.getAsInt(obj, "count", 1);
            JsonElement ingredientJson;
            if (obj.has("ingredient")) {
                ingredientJson = obj.get("ingredient");
            } else {
                JsonObject copy = obj.deepCopy();
                copy.remove("count");
                ingredientJson = copy;
            }
            Ingredient ingredient = Ingredient.fromJson(ingredientJson);
            if (ingredient.isEmpty()) {
                throw new IllegalArgumentException("refining recipe " + recipeId + " has an empty ingredient");
            }
            entries.add(new IngredientEntry(ingredient, count));
        }
        return entries;
    }

    private static Map<ItemTier, Item> readOutputs(JsonObject json, ResourceLocation recipeId) {
        JsonObject obj = GsonHelper.getAsJsonObject(json, "outputs");
        EnumMap<ItemTier, Item> outputs = new EnumMap<>(ItemTier.class);
        for (ItemTier tier : ItemTier.values()) {
            String itemName = GsonHelper.getAsString(obj, tier.id());
            ResourceLocation itemId = new ResourceLocation(itemName);
            Item item = ForgeRegistries.ITEMS.getValue(itemId);
            if (item == null || item == Items.AIR) {
                throw new IllegalArgumentException("refining recipe " + recipeId + " references unknown output item " + itemName);
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

    public static final class Serializer implements RecipeSerializer<RefiningRecipe> {
        @NotNull
        @Override public RefiningRecipe fromJson(@NotNull ResourceLocation recipeId, @NotNull JsonObject json) {
            List<IngredientEntry> ingredients = RefiningRecipe.readIngredients(json, recipeId);
            int qiCost = GsonHelper.getAsInt(json, "qi_cost_per_item", GsonHelper.getAsInt(json, "qi_cost", 0));
            Map<ItemTier, Item> outputs = RefiningRecipe.readOutputs(json, recipeId);
            String translationKey = GsonHelper.getAsString(json, "translation_key", null);
            int sortOrder = GsonHelper.getAsInt(json, "sort_order", 1000);
            return new RefiningRecipe(recipeId, ingredients, qiCost, outputs, translationKey, sortOrder);
        }

        @Nullable
        @Override public RefiningRecipe fromNetwork(@NotNull ResourceLocation recipeId, @NotNull FriendlyByteBuf buf) {
            int ingredientCount = buf.readVarInt();
            ArrayList<IngredientEntry> ingredients = new ArrayList<>(ingredientCount);
            for (int i = 0; i < ingredientCount; ++i) {
                ingredients.add(new IngredientEntry(Ingredient.fromNetwork(buf), buf.readVarInt()));
            }
            int qiCost = buf.readVarInt();
            int outputCount = buf.readVarInt();
            EnumMap<ItemTier, Item> outputs = new EnumMap<>(ItemTier.class);
            for (int i = 0; i < outputCount; ++i) {
                ItemTier tier = buf.readEnum(ItemTier.class);
                Item item = ForgeRegistries.ITEMS.getValue(buf.readResourceLocation());
                if (item == null) continue;
                outputs.put(tier, item);
            }
            String translationKey = buf.readUtf(256);
            int sortOrder = buf.readVarInt();
            return new RefiningRecipe(recipeId, ingredients, qiCost, outputs, translationKey, sortOrder);
        }

        @Override public void toNetwork(@NotNull FriendlyByteBuf buf, @NotNull RefiningRecipe recipe) {
            buf.writeVarInt(recipe.ingredients.size());
            for (IngredientEntry entry : recipe.ingredients) {
                entry.ingredient().toNetwork(buf);
                buf.writeVarInt(entry.count());
            }
            buf.writeVarInt(recipe.qiCostPerItem);
            buf.writeVarInt(recipe.outputs.size());
            for (Map.Entry<ItemTier, Item> e : recipe.outputs.entrySet()) {
                buf.writeEnum(e.getKey());
                ResourceLocation itemId = ForgeRegistries.ITEMS.getKey(e.getValue());
                buf.writeResourceLocation(itemId == null ? new ResourceLocation("minecraft", "air") : itemId);
            }
            buf.writeUtf(recipe.translationKey, 256);
            buf.writeVarInt(recipe.sortOrder);
        }
    }
}
