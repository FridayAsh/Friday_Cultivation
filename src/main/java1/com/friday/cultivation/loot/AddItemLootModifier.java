package com.friday.cultivation.loot;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraftforge.common.loot.IGlobalLootModifier;
import net.minecraftforge.common.loot.LootModifier;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;

import java.util.function.Supplier;

/**
 * 添加物品的战利品修改器（严格照搬原模组 com.xiaoxiang.cultivation.loot.AddItemLootModifier）
 * 战利品表在指定概率下额外添加一个物品
 */
public class AddItemLootModifier extends LootModifier {
    public static final Supplier<Codec<AddItemLootModifier>> CODEC = () -> RecordCodecBuilder.create(
            inst -> codecStart(inst).and(inst.group(
                    ForgeRegistries.ITEMS.getCodec().fieldOf("item").forGetter(m -> m.item),
                    Codec.FLOAT.fieldOf("chance").forGetter(m -> m.chance),
                    Codec.STRING.fieldOf("table_filter").forGetter(m -> m.tableFilter)
            )).apply(inst, AddItemLootModifier::new));

    private final Item item;
    private final float chance;
    private final String tableFilter;

    public AddItemLootModifier(LootItemCondition[] conditions, Item item, float chance, String tableFilter) {
        super(conditions);
        this.item = item;
        this.chance = chance;
        this.tableFilter = tableFilter;
    }

    @Override
    public Codec<? extends IGlobalLootModifier> codec() {
        return CODEC.get();
    }

    @NotNull
    @Override
    protected ObjectArrayList<ItemStack> doApply(ObjectArrayList<ItemStack> generatedLoot, LootContext context) {
        if (context.getQueriedLootTableId() != null
                && context.getQueriedLootTableId().toString().contains(this.tableFilter)
                && context.getRandom().nextFloat() < this.chance) {
            generatedLoot.add(new ItemStack(this.item));
        }
        return generatedLoot;
    }
}