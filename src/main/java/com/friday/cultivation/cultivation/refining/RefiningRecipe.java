/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.JsonArray
 *  com.google.gson.JsonElement
 *  com.google.gson.JsonObject
 *  net.minecraft.core.RegistryAccess
 *  net.minecraft.network.FriendlyByteBuf
 *  net.minecraft.network.chat.Component
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.util.GsonHelper
 *  net.minecraft.world.SimpleContainer
 *  net.minecraft.world.item.Item
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.item.Items
 *  net.minecraft.world.item.crafting.Ingredient
 *  net.minecraft.world.item.crafting.Recipe
 *  net.minecraft.world.item.crafting.RecipeSerializer
 *  net.minecraft.world.item.crafting.RecipeType
 *  net.minecraft.world.level.ItemLike
 *  net.minecraft.world.level.Level
 *  net.minecraftforge.registries.ForgeRegistries
 *  org.jetbrains.annotations.NotNull
 *  org.jetbrains.annotations.Nullable
 */
package com.friday.cultivation.cultivation.refining;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.friday.cultivation.cultivation.ItemTier;
import com.friday.cultivation.registry.ModRecipes;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
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
import org.jetbrains.annotations.Nullable;

public final class RefiningRecipe
implements Recipe<SimpleContainer> {
    public static final int MAX_ITEMS_PER_BATCH = 1;
    private final ResourceLocation id;
    private final List<IngredientEntry> ingredients;
    private final int qiCostPerItem;
    private final Map<ItemTier, Item> outputs;
    private final String translationKey;
    private final int sortOrder;

    public RefiningRecipe(ResourceLocation id, List<IngredientEntry> ingredients, int qiCostPerItem, Map<ItemTier, Item> outputs, @Nullable String translationKey, int sortOrder) {
        this.id = id;
        this.ingredients = List.copyOf(ingredients);
        this.qiCostPerItem = Math.max(0, qiCostPerItem);
        this.outputs = Map.copyOf(outputs);
        this.translationKey = translationKey == null || translationKey.isBlank() ? RefiningRecipe.defaultTranslationKey(id) : translationKey;
        this.sortOrder = sortOrder;
    }

    public String id() {
        return this.id.toString();
    }

    public int qiCostPerItem() {
        return this.qiCostPerItem;
    }

    public Map<ItemTier, Item> outputs() {
        return this.outputs;
    }

    public int sortOrder() {
        return this.sortOrder;
    }

    public List<IngredientEntry> ingredientList() {
        return this.ingredients;
    }

    public Component displayName() {
        return Component.translatable((String)this.translationKey);
    }

    public Item iconItem() {
        Item mid = this.outputs.get((Object)ItemTier.MID);
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
        if (this.qiCostPerItem > 0 && availableQi < (long)this.qiCostPerItem) {
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

    public ResourceLocation rl() {
        return this.id;
    }

    public boolean matches(@NotNull SimpleContainer input, @NotNull Level level) {
        return this.matchesIngredients(input, input.getContainerSize());
    }

    @NotNull
    public ItemStack assemble(@NotNull SimpleContainer input, @NotNull RegistryAccess access) {
        return new ItemStack((ItemLike)this.iconItem());
    }

    public boolean canCraftInDimensions(int width, int height) {
        return width * height >= this.ingredients.size();
    }

    @NotNull
    public ItemStack getResultItem(@NotNull RegistryAccess access) {
        return new ItemStack((ItemLike)this.iconItem());
    }

    @NotNull
    public ResourceLocation getId() {
        return this.id;
    }

    @NotNull
    public RecipeSerializer<?> getSerializer() {
        return (RecipeSerializer)ModRecipes.REFINING_SERIALIZER.get();
    }

    @NotNull
    public RecipeType<?> getType() {
        return (RecipeType)ModRecipes.REFINING_TYPE.get();
    }

    private static List<IngredientEntry> readIngredients(JsonObject json, ResourceLocation recipeId) {
        JsonArray arr = GsonHelper.getAsJsonArray((JsonObject)json, (String)"ingredients");
        if (arr.isEmpty()) {
            throw new IllegalArgumentException("refining recipe " + String.valueOf(recipeId) + " has no ingredients");
        }
        ArrayList<IngredientEntry> entries = new ArrayList<IngredientEntry>();
        for (JsonElement element : arr) {
            JsonElement ingredientJson;
            JsonObject obj = GsonHelper.convertToJsonObject((JsonElement)element, (String)"ingredient");
            int count = GsonHelper.getAsInt((JsonObject)obj, (String)"count", (int)1);
            if (obj.has("ingredient")) {
                ingredientJson = obj.get("ingredient");
            } else {
                JsonObject copy = obj.deepCopy();
                copy.remove("count");
                ingredientJson = copy;
            }
            Ingredient ingredient = Ingredient.fromJson((JsonElement)ingredientJson);
            if (ingredient.isEmpty()) {
                throw new IllegalArgumentException("refining recipe " + String.valueOf(recipeId) + " has an empty ingredient");
            }
            entries.add(new IngredientEntry(ingredient, count));
        }
        return entries;
    }

    private static Map<ItemTier, Item> readOutputs(JsonObject json, ResourceLocation recipeId) {
        JsonObject obj = GsonHelper.getAsJsonObject((JsonObject)json, (String)"outputs");
        EnumMap<ItemTier, Item> outputs = new EnumMap<ItemTier, Item>(ItemTier.class);
        for (ItemTier tier : ItemTier.values()) {
            String itemName = GsonHelper.getAsString((JsonObject)obj, (String)tier.id());
            ResourceLocation itemId = new ResourceLocation(itemName);
            Item item = (Item)ForgeRegistries.ITEMS.getValue(itemId);
            if (item == null || item == Items.AIR) {
                throw new IllegalArgumentException("refining recipe " + String.valueOf(recipeId) + " references unknown output item " + itemName);
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

    public static final class Serializer
    implements RecipeSerializer<RefiningRecipe> {
        @NotNull
        public RefiningRecipe fromJson(@NotNull ResourceLocation recipeId, @NotNull JsonObject json) {
            List<IngredientEntry> ingredients = RefiningRecipe.readIngredients(json, recipeId);
            int qiCost = GsonHelper.getAsInt((JsonObject)json, (String)"qi_cost_per_item", (int)GsonHelper.getAsInt((JsonObject)json, (String)"qi_cost", (int)0));
            Map<ItemTier, Item> outputs = RefiningRecipe.readOutputs(json, recipeId);
            String translationKey = GsonHelper.getAsString((JsonObject)json, (String)"translation_key", null);
            int sortOrder = GsonHelper.getAsInt((JsonObject)json, (String)"sort_order", (int)1000);
            return new RefiningRecipe(recipeId, ingredients, qiCost, outputs, translationKey, sortOrder);
        }

        @Nullable
        public RefiningRecipe fromNetwork(@NotNull ResourceLocation recipeId, @NotNull FriendlyByteBuf buf) {
            int ingredientCount = buf.readVarInt();
            ArrayList<IngredientEntry> ingredients = new ArrayList<IngredientEntry>(ingredientCount);
            for (int i = 0; i < ingredientCount; ++i) {
                ingredients.add(new IngredientEntry(Ingredient.fromNetwork((FriendlyByteBuf)buf), buf.readVarInt()));
            }
            int qiCost = buf.readVarInt();
            int outputCount = buf.readVarInt();
            EnumMap<ItemTier, Item> outputs = new EnumMap<ItemTier, Item>(ItemTier.class);
            for (int i = 0; i < outputCount; ++i) {
                ItemTier tier = (ItemTier)buf.readEnum(ItemTier.class);
                Item item = (Item)ForgeRegistries.ITEMS.getValue(buf.readResourceLocation());
                if (item == null) continue;
                outputs.put(tier, item);
            }
            String translationKey = buf.readUtf(256);
            int sortOrder = buf.readVarInt();
            return new RefiningRecipe(recipeId, ingredients, qiCost, outputs, translationKey, sortOrder);
        }

        public void toNetwork(@NotNull FriendlyByteBuf buf, @NotNull RefiningRecipe recipe) {
            buf.writeVarInt(recipe.ingredients.size());
            for (IngredientEntry ingredientEntry : recipe.ingredients) {
                ingredientEntry.ingredient().toNetwork(buf);
                buf.writeVarInt(ingredientEntry.count());
            }
            buf.writeVarInt(recipe.qiCostPerItem);
            buf.writeVarInt(recipe.outputs.size());
            for (Map.Entry entry : recipe.outputs.entrySet()) {
                buf.writeEnum((Enum)entry.getKey());
                ResourceLocation itemId = ForgeRegistries.ITEMS.getKey(((Item)entry.getValue()));
                buf.writeResourceLocation(itemId == null ? new ResourceLocation("minecraft", "air") : itemId);
            }
            buf.writeUtf(recipe.translationKey, 256);
            buf.writeVarInt(recipe.sortOrder);
        }
    }
}

