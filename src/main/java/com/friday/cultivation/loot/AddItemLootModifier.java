/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.base.Suppliers
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 *  com.mojang.serialization.codecs.RecordCodecBuilder$Instance
 *  it.unimi.dsi.fastutil.objects.ObjectArrayList
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.world.item.Item
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.level.ItemLike
 *  net.minecraft.world.level.storage.loot.LootContext
 *  net.minecraft.world.level.storage.loot.predicates.LootItemCondition
 *  net.minecraftforge.common.loot.IGlobalLootModifier
 *  net.minecraftforge.common.loot.LootModifier
 *  net.minecraftforge.registries.ForgeRegistries
 *  org.jetbrains.annotations.NotNull
 */
package com.friday.cultivation.loot;

import com.google.common.base.Suppliers;
import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.function.Supplier;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraftforge.common.loot.IGlobalLootModifier;
import net.minecraftforge.common.loot.LootModifier;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;

public class AddItemLootModifier
extends LootModifier {
    public static final Supplier<Codec<AddItemLootModifier>> CODEC = Suppliers.memoize(() -> RecordCodecBuilder.create(inst -> AddItemLootModifier.codecStart(inst).and(inst.group(
            ForgeRegistries.ITEMS.getCodec().fieldOf("item").forGetter(m -> m.item),
            Codec.FLOAT.fieldOf("chance").forGetter(m -> m.chance),
            Codec.STRING.fieldOf("table_filter").forGetter(m -> m.tableFilter)
    )).apply(inst, AddItemLootModifier::new)));
    private final Item item;
    private final float chance;
    private final String tableFilter;

    public AddItemLootModifier(LootItemCondition[] conditions, Item item, float chance, String tableFilter) {
        super(conditions);
        this.item = item;
        this.chance = chance;
        this.tableFilter = tableFilter;
    }

    @NotNull
    protected ObjectArrayList<ItemStack> doApply(@NotNull ObjectArrayList<ItemStack> generatedLoot, @NotNull LootContext context) {
        ResourceLocation tableId = context.getQueriedLootTableId();
        if (tableId != null && this.tableFilter != null && !this.tableFilter.isEmpty() && tableId.getPath().contains(this.tableFilter) && context.getRandom().nextFloat() < this.chance) {
            generatedLoot.add(new ItemStack((ItemLike)this.item));
        }
        return generatedLoot;
    }

    public Codec<? extends IGlobalLootModifier> codec() {
        return CODEC.get();
    }
}

