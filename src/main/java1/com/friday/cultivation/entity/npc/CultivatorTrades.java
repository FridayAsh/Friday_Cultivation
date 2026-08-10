package com.friday.cultivation.entity.npc;

import com.friday.cultivation.ItemTier;
import com.friday.cultivation.alchemy.PillTier;
import com.friday.cultivation.realm.Realm;
import com.friday.cultivation.spell.Spell;
import com.friday.cultivation.spell.SpellType;
import com.friday.cultivation.technique.Technique;
import com.friday.cultivation.item.CultivationPillItem;
import com.friday.cultivation.item.PillItem;
import com.friday.cultivation.item.SectTokenItem;
import com.friday.cultivation.item.SpellBookItem;
import com.friday.cultivation.item.TechniqueBookItem;
import com.friday.cultivation.item.weapon.TieredWeapon;
import com.friday.cultivation.item.ModItems;
import com.friday.cultivation.util.CultivationRandomPools;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.item.trading.MerchantOffers;
import net.minecraft.world.level.ItemLike;

import java.util.ArrayList;
import java.util.List;

/**
 * 游历修士 NPC 交易清单生成器。
 * 严格 1:1 复刻原 mod CultivatorTrades。
 */
public final class CultivatorTrades {
    public static final int INFINITE_USES = Integer.MAX_VALUE;
    @SuppressWarnings("unused")
    private static final float PRICE_MULT = 0.0f;

    private CultivatorTrades() {
    }

    public static MerchantOffers generateOffers(WanderingCultivatorEntity npc) {
        MerchantOffers offers = new MerchantOffers();
        boolean isMortal = npc.getRealm() == Realm.MORTAL;
        addHeldSwordOffer(offers, npc, isMortal);
        addInventoryOffers(offers, npc, isMortal);
        addSoulReaperTokenOffer(offers, npc);
        if (!isMortal) {
            addTechniqueBookOffer(offers, npc);
            addSpellBookOffers(offers, npc);
        }
        return offers;
    }

    private static void addInventoryOffers(MerchantOffers offers, WanderingCultivatorEntity npc, boolean isMortal) {
        SimpleContainer inv = npc.getInventory();
        ItemStack heldSword = npc.getMainHandItem();
        ArrayList<ItemStack> totals = new ArrayList<>();
        for (int i = 0; i < inv.getContainerSize(); ++i) {
            ItemStack stack = inv.getItem(i);
            if (stack.isEmpty()
                    || CultivationRandomPools.isForbiddenNaturalLootStack(stack)
                    || stack.is(ModItems.SECT_TOKEN.get()) && !SectTokenItem.isTemporaryLinked(stack)
                    || isTradeableSword(heldSword.getItem()) && ItemStack.isSameItemSameTags(stack, heldSword))
                continue;
            mergeTradeStack(totals, stack);
        }
        for (ItemStack template : totals) {
            ItemStack[] cost;
            Item item = template.getItem();
            int count = template.getCount();
            if (count <= 0 || (cost = priceForInvItem(item, isMortal)) == null || cost.length == 0) continue;
            ItemStack result = template.copy();
            result.setCount(1);
            if (cost.length == 1) {
                offers.add(new MerchantOffer(cost[0], result, count, 0, 0.0f));
            } else {
                offers.add(new MerchantOffer(cost[0], cost[1], result, count, 0, 0.0f));
            }
        }
    }

    private static void mergeTradeStack(List<ItemStack> totals, ItemStack stack) {
        for (ItemStack existing : totals) {
            if (!ItemStack.isSameItemSameTags(existing, stack)) continue;
            existing.grow(stack.getCount());
            return;
        }
        totals.add(stack.copy());
    }

    private static void addSoulReaperTokenOffer(MerchantOffers offers, WanderingCultivatorEntity npc) {
        if (!npc.isDifuReaper() || !npc.isSoulReaperTokenTradeAvailable()) {
            return;
        }
        if (inventoryHasItem(npc, ModItems.SOUL_REAPER_TOKEN.get())) {
            return;
        }
        offers.add(new MerchantOffer(soulReaperTokenPrice(), new ItemStack(ModItems.SOUL_REAPER_TOKEN.get(), 1), 1, 0, 0.0f));
    }

    private static void addHeldSwordOffer(MerchantOffers offers, WanderingCultivatorEntity npc, boolean isMortal) {
        ItemStack held = npc.getMainHandItem();
        if (!isTradeableSword(held.getItem())) {
            return;
        }
        ItemStack[] cost = priceForHeldSword(held.getItem(), isMortal);
        if (cost == null || cost.length == 0) {
            return;
        }
        ItemStack result = held.copy();
        result.setCount(1);
        if (cost.length == 1) {
            offers.add(new MerchantOffer(cost[0], result, 1, 0, 0.0f));
        } else {
            offers.add(new MerchantOffer(cost[0], cost[1], result, 1, 0, 0.0f));
        }
    }

    private static void addTechniqueBookOffer(MerchantOffers offers, WanderingCultivatorEntity npc) {
        String techId = npc.getTechniqueId();
        if (techId == null || techId.isEmpty()) {
            return;
        }
        Technique tech = Technique.byId(techId);
        if (tech == null) {
            return;
        }
        Item book = WanderingCultivatorEntity.techniqueBookItem(tech);
        if (book == null) {
            return;
        }
        ItemStack cost = techniqueBookSpiritPrice(techTierToItemTier(tech.tier()));
        offers.add(new MerchantOffer(cost, new ItemStack(book, 1), Integer.MAX_VALUE, 0, 0.0f));
    }

    private static void addSpellBookOffers(MerchantOffers offers, WanderingCultivatorEntity npc) {
        for (String id : npc.getSpellIds()) {
            Item book;
            Spell sp = Spell.byId(id);
            if (sp == null || (book = WanderingCultivatorEntity.spellBookItem(sp)) == null) continue;
            ItemStack cost = spellBookSpiritPrice(sp.tier(), sp.type());
            offers.add(new MerchantOffer(cost, new ItemStack(book, 1), Integer.MAX_VALUE, 0, 0.0f));
        }
    }

    public static boolean isSpiritStone(Item item) {
        return item == ModItems.LOW_SPIRIT_STONE.get()
                || item == ModItems.MID_SPIRIT_STONE.get()
                || item == ModItems.HIGH_SPIRIT_STONE.get()
                || item == ModItems.SUPREME_SPIRIT_STONE.get();
    }

    static ItemStack[] priceForInvItem(Item item, boolean isMortal) {
        TieredWeapon weapon;
        if (CultivationRandomPools.isForbiddenNaturalLootItem(item)) {
            return null;
        }
        if (isSpiritStone(item)) {
            return new ItemStack[]{stoneEmeraldPrice(item)};
        }
        if (item == ModItems.SECT_TOKEN.get()) {
            return new ItemStack[]{isMortal ? new ItemStack(Items.EMERALD, 24) : new ItemStack(ModItems.LOW_SPIRIT_STONE.get(), 3)};
        }
        if (item instanceof CultivationPillItem) {
            CultivationPillItem pill = (CultivationPillItem) item;
            return new ItemStack[]{cultivationPillPrice(pill.tier())};
        }
        ItemStack[] specialSupplyPrice = specialSupplyPrice(item, isMortal);
        if (specialSupplyPrice != null) {
            return specialSupplyPrice;
        }
        if (isFoundationBreakthroughMaterial(item)) {
            return foundationBreakthroughMaterialPrice(item, isMortal);
        }
        if (isGoldenCoreMaterial(item)) {
            return goldenCoreMaterialPrice(item, isMortal);
        }
        if (item == ModItems.SOUL_REAPER_TOKEN.get()) {
            return new ItemStack[]{soulReaperTokenPrice()};
        }
        if (isMortal) {
            return mortalItemPrice(item);
        }
        if (item instanceof TechniqueBookItem) {
            TechniqueBookItem tb = (TechniqueBookItem) item;
            return new ItemStack[]{techniqueBookSpiritPrice(techTierToItemTier(tb.technique().tier()))};
        }
        if (item instanceof SpellBookItem) {
            SpellBookItem sb = (SpellBookItem) item;
            return new ItemStack[]{spellBookSpiritPrice(sb.spell().tier(), sb.spell().type())};
        }
        if (item instanceof TieredWeapon && (weapon = (TieredWeapon) item).isSwordWeapon()) {
            return new ItemStack[]{spiritSwordPrice(weapon.tier())};
        }
        if (item instanceof PillItem) {
            PillItem pill = (PillItem) item;
            return new ItemStack[]{pillPrice(pill.tier())};
        }
        if (isFoundationBreakthroughMaterial(item)) {
            return foundationBreakthroughMaterialPrice(item, isMortal);
        }
        if (isGoldenCoreMaterial(item)) {
            return goldenCoreMaterialPrice(item, isMortal);
        }
        if (item == ModItems.SOUL_REAPER_TOKEN.get()) {
            return new ItemStack[]{soulReaperTokenPrice()};
        }
        return null;
    }

    private static boolean inventoryHasItem(WanderingCultivatorEntity npc, Item item) {
        SimpleContainer inv = npc.getInventory();
        for (int i = 0; i < inv.getContainerSize(); ++i) {
            if (!inv.getItem(i).is(item)) continue;
            return true;
        }
        return false;
    }

    private static ItemStack[] priceForHeldSword(Item item, boolean isMortal) {
        TieredWeapon weapon;
        if (item instanceof TieredWeapon && (weapon = (TieredWeapon) item).isSwordWeapon()) {
            return new ItemStack[]{spiritSwordPrice(weapon.tier())};
        }
        if (isMortal) {
            return new ItemStack[]{new ItemStack(Items.EMERALD, 3)};
        }
        return new ItemStack[]{new ItemStack(ModItems.LOW_SPIRIT_STONE.get(), 3)};
    }

    private static boolean isTradeableSword(Item item) {
        TieredWeapon weapon;
        return item instanceof SwordItem || item instanceof TieredWeapon && (weapon = (TieredWeapon) item).isSwordWeapon();
    }

    private static ItemStack spiritSwordPrice(ItemTier tier) {
        return switch (tier) {
            case LOW -> new ItemStack(ModItems.MID_SPIRIT_STONE.get(), 2);
            case MID -> new ItemStack(ModItems.HIGH_SPIRIT_STONE.get(), 1);
            case HIGH -> new ItemStack(ModItems.HIGH_SPIRIT_STONE.get(), 5);
            case SUPREME -> new ItemStack(ModItems.SUPREME_SPIRIT_STONE.get(), 4);
            case IMMORTAL -> new ItemStack(ModItems.SUPREME_SPIRIT_STONE.get(), 16);
        };
    }

    private static ItemStack pillPrice(PillTier tier) {
        return switch (tier) {
            case LOW -> new ItemStack(ModItems.LOW_SPIRIT_STONE.get(), 5);
            case MID -> new ItemStack(ModItems.MID_SPIRIT_STONE.get(), 1);
            case HIGH -> new ItemStack(ModItems.HIGH_SPIRIT_STONE.get(), 1);
            case SUPREME -> new ItemStack(ModItems.SUPREME_SPIRIT_STONE.get(), 1);
            case IMMORTAL -> new ItemStack(ModItems.SUPREME_SPIRIT_STONE.get(), 8);
        };
    }

    private static ItemStack cultivationPillPrice(PillTier tier) {
        return switch (tier) {
            case LOW -> new ItemStack(Items.EMERALD, 1);
            case MID -> new ItemStack(ModItems.LOW_SPIRIT_STONE.get(), 5);
            case HIGH -> new ItemStack(ModItems.MID_SPIRIT_STONE.get(), 5);
            case SUPREME -> new ItemStack(ModItems.HIGH_SPIRIT_STONE.get(), 5);
            case IMMORTAL -> new ItemStack(ModItems.SUPREME_SPIRIT_STONE.get(), 1);
        };
    }

    private static ItemStack soulReaperTokenPrice() {
        return new ItemStack(ModItems.MID_SPIRIT_STONE.get(), 6);
    }

    private static boolean isGoldenCoreMaterial(Item item) {
        return item == ModItems.JIEDAN_PILL.get()
                || item == ModItems.BLOOD_JIEDAN_PILL.get()
                || item == ModItems.ALL_CREATURES_TRUE_BLOOD.get()
                || item == ModItems.EARTH_EVIL_QI.get()
                || item == ModItems.HEAVEN_CLEAR_QI.get()
                || item == ModItems.NINGZHEN_CREATION_FRUIT.get()
                || item == ModItems.BLOOD_TRANSFORMATION_TALISMAN.get();
    }

    private static boolean isFoundationBreakthroughMaterial(Item item) {
        return item == ModItems.ZHUJI_DAN.get()
                || item == ModItems.BLOOD_SPIRIT_PILL.get()
                || item == ModItems.DAO_FOUNDATION_FRUIT.get()
                || item == ModItems.FOUNDATION_SECRET.get();
    }

    private static ItemStack[] foundationBreakthroughMaterialPrice(Item item, boolean isMortal) {
        if (item == ModItems.ZHUJI_DAN.get()) {
            return new ItemStack[]{new ItemStack(ModItems.LOW_SPIRIT_STONE.get(), 3)};
        }
        if (item == ModItems.FOUNDATION_SECRET.get()) {
            return new ItemStack[]{new ItemStack(ModItems.MID_SPIRIT_STONE.get(), 1)};
        }
        if (isMortal) {
            if (item == ModItems.BLOOD_SPIRIT_PILL.get()) {
                return new ItemStack[]{new ItemStack(Items.EMERALD, 36)};
            }
            return new ItemStack[]{new ItemStack(Items.EMERALD, 64)};
        }
        if (item == ModItems.BLOOD_SPIRIT_PILL.get()) {
            return new ItemStack[]{new ItemStack(ModItems.MID_SPIRIT_STONE.get(), 2)};
        }
        if (item == ModItems.DAO_FOUNDATION_FRUIT.get()) {
            return new ItemStack[]{new ItemStack(ModItems.HIGH_SPIRIT_STONE.get(), 2)};
        }
        return new ItemStack[]{new ItemStack(ModItems.MID_SPIRIT_STONE.get(), 1)};
    }

    private static ItemStack[] goldenCoreMaterialPrice(Item item, boolean isMortal) {
        if (item == ModItems.JIEDAN_PILL.get()) {
            return new ItemStack[]{new ItemStack(ModItems.MID_SPIRIT_STONE.get(), 3)};
        }
        if (item == ModItems.ALL_CREATURES_TRUE_BLOOD.get()) {
            return new ItemStack[]{new ItemStack(ModItems.HIGH_SPIRIT_STONE.get(), 1)};
        }
        if (item == ModItems.BLOOD_TRANSFORMATION_TALISMAN.get()) {
            return new ItemStack[]{new ItemStack(ModItems.MID_SPIRIT_STONE.get(), 5)};
        }
        if (isMortal) {
            return new ItemStack[]{new ItemStack(Items.EMERALD, 64)};
        }
        if (item == ModItems.BLOOD_JIEDAN_PILL.get()) {
            return new ItemStack[]{new ItemStack(ModItems.HIGH_SPIRIT_STONE.get(), 3)};
        }
        if (item == ModItems.EARTH_EVIL_QI.get()) {
            return new ItemStack[]{new ItemStack(ModItems.HIGH_SPIRIT_STONE.get(), 4)};
        }
        if (item == ModItems.HEAVEN_CLEAR_QI.get()) {
            return new ItemStack[]{new ItemStack(ModItems.SUPREME_SPIRIT_STONE.get(), 2)};
        }
        if (item == ModItems.NINGZHEN_CREATION_FRUIT.get()) {
            return new ItemStack[]{new ItemStack(ModItems.SUPREME_SPIRIT_STONE.get(), 3)};
        }
        return new ItemStack[]{new ItemStack(ModItems.HIGH_SPIRIT_STONE.get(), 1)};
    }

    private static ItemStack[] specialSupplyPrice(Item item, boolean isMortal) {
        if (item == ModItems.HERB.get()) {
            return new ItemStack[]{isMortal ? new ItemStack(Items.EMERALD, 2) : new ItemStack(ModItems.LOW_SPIRIT_STONE.get(), 1)};
        }
        if (item == ModItems.INK.get()) {
            return new ItemStack[]{isMortal ? new ItemStack(Items.EMERALD, 1) : new ItemStack(ModItems.LOW_SPIRIT_STONE.get(), 1)};
        }
        if (item == ModItems.TALISMAN_PAPER.get()) {
            return new ItemStack[]{isMortal ? new ItemStack(Items.EMERALD, 2) : new ItemStack(ModItems.LOW_SPIRIT_STONE.get(), 1)};
        }
        if (item == ModItems.YOUTH_PILL.get()) {
            return new ItemStack[]{isMortal ? new ItemStack(Items.EMERALD, 48) : new ItemStack(ModItems.HIGH_SPIRIT_STONE.get(), 1)};
        }
        if (item == ModItems.SEX_CHANGE_PILL.get()) {
            return new ItemStack[]{isMortal ? new ItemStack(Items.EMERALD, 32) : new ItemStack(ModItems.MID_SPIRIT_STONE.get(), 3)};
        }
        if (item == ModItems.FORMATION_COMPASS.get()) {
            return new ItemStack[]{isMortal ? new ItemStack(Items.EMERALD, 36) : new ItemStack(ModItems.MID_SPIRIT_STONE.get(), 2)};
        }
        if (item == ModItems.FORMATION_INSCRIPTION_KNIFE.get()) {
            return new ItemStack[]{isMortal ? new ItemStack(Items.EMERALD, 48) : new ItemStack(ModItems.MID_SPIRIT_STONE.get(), 4)};
        }
        return null;
    }

    private static ItemStack stoneEmeraldPrice(Item stone) {
        if (stone == ModItems.LOW_SPIRIT_STONE.get()) {
            return new ItemStack(Items.EMERALD, 1);
        }
        if (stone == ModItems.MID_SPIRIT_STONE.get()) {
            return new ItemStack(Items.EMERALD, 5);
        }
        if (stone == ModItems.HIGH_SPIRIT_STONE.get()) {
            return new ItemStack(Items.EMERALD, 25);
        }
        if (stone == ModItems.SUPREME_SPIRIT_STONE.get()) {
            return new ItemStack(Items.EMERALD, 64);
        }
        return new ItemStack(Items.EMERALD, 1);
    }

    private static ItemStack[] mortalItemPrice(Item item) {
        if (item == ModItems.CUSHION.get()) {
            return new ItemStack[]{new ItemStack(Items.WHEAT, 4)};
        }
        if (item == ModItems.DIVINATION_COMPASS.get()) {
            return new ItemStack[]{new ItemStack(Items.COMPASS, 1), new ItemStack(Items.REDSTONE, 4)};
        }
        if (item == ModItems.LOW_SPIRIT_STONE.get()) {
            return new ItemStack[]{new ItemStack(Items.EMERALD, 1)};
        }
        if (item == ModItems.TECHNIQUE_BOOK_FRAGMENT.get()) {
            return new ItemStack[]{new ItemStack(Items.BOOK, 1), new ItemStack(Items.WRITABLE_BOOK, 4)};
        }
        if (item == ModItems.SPELL_BOOK_FIREBALL.get()) {
            return new ItemStack[]{new ItemStack(Items.BOOK, 1), new ItemStack(Items.BLAZE_POWDER, 1)};
        }
        return null;
    }

    private static ItemStack techniqueBookSpiritPrice(ItemTier tier) {
        return switch (tier) {
            case LOW -> new ItemStack(ModItems.MID_SPIRIT_STONE.get(), 1);
            case MID -> new ItemStack(ModItems.HIGH_SPIRIT_STONE.get(), 1);
            case HIGH -> new ItemStack(ModItems.SUPREME_SPIRIT_STONE.get(), 1);
            case SUPREME -> new ItemStack(ModItems.SUPREME_SPIRIT_STONE.get(), 8);
            case IMMORTAL -> new ItemStack(ModItems.SUPREME_SPIRIT_STONE.get(), 32);
        };
    }

    /** Technique.Tier → ItemTier 转换（两套并列枚举，需手动映射）。 */
    private static ItemTier techTierToItemTier(com.friday.cultivation.technique.Technique.Tier tier) {
        return switch (tier) {
            case LOW -> ItemTier.LOW;
            case MID -> ItemTier.MID;
            case HIGH -> ItemTier.HIGH;
            case SUPREME -> ItemTier.SUPREME;
            case IMMORTAL -> ItemTier.IMMORTAL;
        };
    }

    private static ItemStack spellBookSpiritPrice(ItemTier tier, SpellType type) {
        boolean passive = type == SpellType.PASSIVE;
        return switch (tier) {
            case LOW -> new ItemStack(passive ? ModItems.MID_SPIRIT_STONE.get() : ModItems.LOW_SPIRIT_STONE.get(), passive ? 1 : 5);
            case MID -> new ItemStack(passive ? ModItems.HIGH_SPIRIT_STONE.get() : ModItems.MID_SPIRIT_STONE.get(), passive ? 1 : 5);
            case HIGH -> new ItemStack(passive ? ModItems.SUPREME_SPIRIT_STONE.get() : ModItems.HIGH_SPIRIT_STONE.get(), passive ? 1 : 5);
            case SUPREME -> new ItemStack(ModItems.SUPREME_SPIRIT_STONE.get(), passive ? 5 : 3);
            case IMMORTAL -> new ItemStack(ModItems.SUPREME_SPIRIT_STONE.get(), passive ? 32 : 16);
        };
    }
}
