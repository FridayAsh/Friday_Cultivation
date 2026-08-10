package com.friday.cultivation.registry;

import com.friday.cultivation.item.ModItems;
import com.friday.cultivation.realm.Realm;
import java.util.Map;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

/**
 * 创造模式标签页（严格照搬原模组 com.xiaoxiang.cultivation.registry.ModCreativeTabs）。
 * 单个 MAIN_TAB，按原模组顺序收录全部物品。
 */
public final class ModCreativeTabs {
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, "friday_cultivation");
    public static final RegistryObject<CreativeModeTab> MAIN_TAB = CREATIVE_MODE_TABS.register("main", () -> CreativeModeTab.builder().title(Component.translatable("itemGroup.friday_cultivation.main")).icon(() -> new ItemStack(ModItems.MID_SPIRIT_STONE.get())).displayItems((params, output) -> {
        output.accept(ModItems.LOW_SPIRIT_STONE.get());
        output.accept(ModItems.MID_SPIRIT_STONE.get());
        output.accept(ModItems.HIGH_SPIRIT_STONE.get());
        output.accept(ModItems.SUPREME_SPIRIT_STONE.get());
        output.accept(ModItems.LOW_SPIRIT_STONE_ORE.get());
        output.accept(ModItems.MID_SPIRIT_STONE_ORE.get());
        output.accept(ModItems.HIGH_SPIRIT_STONE_ORE.get());
        output.accept(ModItems.SUPREME_SPIRIT_STONE_ORE.get());
        output.accept(ModItems.SPIRIT_VEIN_SPRING.get());
        output.accept(ModItems.DIVINATION_COMPASS.get());
        output.accept(ModItems.FORMATION_COMPASS.get());
        output.accept(ModItems.FORMATION_INSCRIPTION_KNIFE.get());
        if (ModList.get().isLoaded("patchouli")) {
            output.accept(ModItems.CULTIVATION_COMPENDIUM.get());
        }
        output.accept(ModItems.INK.get());
        output.accept(ModItems.TALISMAN_PAPER.get());
        output.accept(ModItems.ORIGIN_RECONFIGURATION_TOKEN.get());
        output.accept(ModItems.REINCARNATION_FATE_PLATE.get());
        output.accept(ModItems.RECALL_PILL.get());
        output.accept(ModItems.SOUL_HOOK.get());
        output.accept(ModItems.SOUL_REAPER_TOKEN.get());
        output.accept(ModItems.YOUTH_PILL.get());
        output.accept(ModItems.SEX_CHANGE_PILL.get());
        output.accept(ModItems.ZHUJI_DAN.get());
        output.accept(ModItems.BLOOD_SPIRIT_PILL.get());
        output.accept(ModItems.DAO_FOUNDATION_FRUIT.get());
        output.accept(ModItems.FOUNDATION_SECRET.get());
        output.accept(ModItems.JIEDAN_PILL.get());
        output.accept(ModItems.BLOOD_JIEDAN_PILL.get());
        output.accept(ModItems.ALL_CREATURES_TRUE_BLOOD.get());
        output.accept(ModItems.EARTH_EVIL_QI.get());
        output.accept(ModItems.HEAVEN_CLEAR_QI.get());
        output.accept(ModItems.NINGZHEN_CREATION_FRUIT.get());
        output.accept(ModItems.BLOOD_TRANSFORMATION_TALISMAN.get());
        output.accept(ModItems.CUSHION.get());
        output.accept(ModItems.BONE_BLOCK.get());
        output.accept(ModItems.ALCHEMY_CORE_ITEM.get());
        output.accept(ModItems.REFINING_CORE_ITEM.get());
        output.accept(ModItems.LOW_FORMATION_CORE_PLATE.get());
        output.accept(ModItems.MID_FORMATION_CORE_PLATE.get());
        output.accept(ModItems.HIGH_FORMATION_CORE_PLATE.get());
        output.accept(ModItems.SUPREME_FORMATION_CORE_PLATE.get());
        output.accept(ModItems.IMMORTAL_FORMATION_CORE_PLATE.get());
        output.accept(ModItems.LOW_QI_GATHERING_FLAG.get());
        output.accept(ModItems.MID_QI_GATHERING_FLAG.get());
        output.accept(ModItems.HIGH_QI_GATHERING_FLAG.get());
        output.accept(ModItems.SUPREME_QI_GATHERING_FLAG.get());
        output.accept(ModItems.IMMORTAL_QI_GATHERING_FLAG.get());
        output.accept(ModItems.LOW_SECT_PROTECTION_FLAG.get());
        output.accept(ModItems.MID_SECT_PROTECTION_FLAG.get());
        output.accept(ModItems.HIGH_SECT_PROTECTION_FLAG.get());
        output.accept(ModItems.SUPREME_SECT_PROTECTION_FLAG.get());
        output.accept(ModItems.IMMORTAL_SECT_PROTECTION_FLAG.get());
        output.accept(ModItems.LOW_WITHER_GROWTH_FLAG.get());
        output.accept(ModItems.MID_WITHER_GROWTH_FLAG.get());
        output.accept(ModItems.HIGH_WITHER_GROWTH_FLAG.get());
        output.accept(ModItems.SUPREME_WITHER_GROWTH_FLAG.get());
        output.accept(ModItems.IMMORTAL_WITHER_GROWTH_FLAG.get());
        output.accept(ModItems.LOW_REJUVENATION_FLAG.get());
        output.accept(ModItems.MID_REJUVENATION_FLAG.get());
        output.accept(ModItems.HIGH_REJUVENATION_FLAG.get());
        output.accept(ModItems.SUPREME_REJUVENATION_FLAG.get());
        output.accept(ModItems.IMMORTAL_REJUVENATION_FLAG.get());
        output.accept(ModItems.LOW_FLIGHT_BAN_FLAG.get());
        output.accept(ModItems.MID_FLIGHT_BAN_FLAG.get());
        output.accept(ModItems.HIGH_FLIGHT_BAN_FLAG.get());
        output.accept(ModItems.SUPREME_FLIGHT_BAN_FLAG.get());
        output.accept(ModItems.IMMORTAL_FLIGHT_BAN_FLAG.get());
        output.accept(ModItems.LOW_MAZE_FLAG.get());
        output.accept(ModItems.MID_MAZE_FLAG.get());
        output.accept(ModItems.HIGH_MAZE_FLAG.get());
        output.accept(ModItems.SUPREME_MAZE_FLAG.get());
        output.accept(ModItems.IMMORTAL_MAZE_FLAG.get());
        output.accept(ModItems.LOW_FARM_HARVEST_FLAG.get());
        output.accept(ModItems.MID_FARM_HARVEST_FLAG.get());
        output.accept(ModItems.HIGH_FARM_HARVEST_FLAG.get());
        output.accept(ModItems.SUPREME_FARM_HARVEST_FLAG.get());
        output.accept(ModItems.IMMORTAL_FARM_HARVEST_FLAG.get());
        output.accept(ModItems.SECT_TOKEN.get());
        output.accept(ModItems.LOW_SPIRIT_VEIN_CORE.get());
        output.accept(ModItems.MID_SPIRIT_VEIN_CORE.get());
        output.accept(ModItems.HIGH_SPIRIT_VEIN_CORE.get());
        output.accept(ModItems.SUPREME_SPIRIT_VEIN_CORE.get());
        output.accept(ModItems.IMMORTAL_SPIRIT_VEIN_CORE.get());
        output.accept(ModItems.XUAN_IRON_SWORD_LOW.get());
        output.accept(ModItems.XUAN_IRON_SWORD_MID.get());
        output.accept(ModItems.XUAN_IRON_SWORD_HIGH.get());
        output.accept(ModItems.XUAN_IRON_SWORD_SUPREME.get());
        output.accept(ModItems.XUAN_IRON_SWORD_IMMORTAL.get());
        output.accept(ModItems.QING_MU_SWORD_LOW.get());
        output.accept(ModItems.QING_MU_SWORD_MID.get());
        output.accept(ModItems.QING_MU_SWORD_HIGH.get());
        output.accept(ModItems.QING_MU_SWORD_SUPREME.get());
        output.accept(ModItems.QING_MU_SWORD_IMMORTAL.get());
        output.accept(ModItems.CHI_YAN_SWORD_LOW.get());
        output.accept(ModItems.CHI_YAN_SWORD_MID.get());
        output.accept(ModItems.CHI_YAN_SWORD_HIGH.get());
        output.accept(ModItems.CHI_YAN_SWORD_SUPREME.get());
        output.accept(ModItems.CHI_YAN_SWORD_IMMORTAL.get());
        output.accept(ModItems.HAN_BING_SWORD_LOW.get());
        output.accept(ModItems.HAN_BING_SWORD_MID.get());
        output.accept(ModItems.HAN_BING_SWORD_HIGH.get());
        output.accept(ModItems.HAN_BING_SWORD_SUPREME.get());
        output.accept(ModItems.HAN_BING_SWORD_IMMORTAL.get());
        output.accept(ModItems.HERB.get());
        output.accept(ModItems.PILL_QI_RECOVERY_LOW.get());
        output.accept(ModItems.PILL_QI_RECOVERY_MID.get());
        output.accept(ModItems.PILL_QI_RECOVERY_HIGH.get());
        output.accept(ModItems.PILL_QI_RECOVERY_SUPREME.get());
        output.accept(ModItems.PILL_QI_RECOVERY_IMMORTAL.get());
        output.accept(ModItems.PILL_CULTIVATION_LOW.get());
        output.accept(ModItems.PILL_CULTIVATION_MID.get());
        output.accept(ModItems.PILL_CULTIVATION_HIGH.get());
        output.accept(ModItems.PILL_CULTIVATION_SUPREME.get());
        output.accept(ModItems.PILL_CULTIVATION_IMMORTAL.get());
        output.accept(ModItems.PILL_BLOOD_BURN_LOW.get());
        output.accept(ModItems.PILL_BLOOD_BURN_MID.get());
        output.accept(ModItems.PILL_BLOOD_BURN_HIGH.get());
        output.accept(ModItems.PILL_BLOOD_BURN_SUPREME.get());
        output.accept(ModItems.PILL_BLOOD_BURN_IMMORTAL.get());
        output.accept(ModItems.PILL_CLEAR_MIND_LOW.get());
        output.accept(ModItems.PILL_CLEAR_MIND_MID.get());
        output.accept(ModItems.PILL_CLEAR_MIND_HIGH.get());
        output.accept(ModItems.PILL_CLEAR_MIND_SUPREME.get());
        output.accept(ModItems.PILL_CLEAR_MIND_IMMORTAL.get());
        output.accept(ModItems.PILL_REJUVENATION_LOW.get());
        output.accept(ModItems.PILL_REJUVENATION_MID.get());
        output.accept(ModItems.PILL_REJUVENATION_HIGH.get());
        output.accept(ModItems.PILL_REJUVENATION_SUPREME.get());
        output.accept(ModItems.PILL_REJUVENATION_IMMORTAL.get());
        output.accept(ModItems.PILL_DIVINE_STRIDE_LOW.get());
        output.accept(ModItems.PILL_DIVINE_STRIDE_MID.get());
        output.accept(ModItems.PILL_DIVINE_STRIDE_HIGH.get());
        output.accept(ModItems.PILL_DIVINE_STRIDE_SUPREME.get());
        output.accept(ModItems.PILL_DIVINE_STRIDE_IMMORTAL.get());
        for (Item item : ModItems.orderedTechniqueBookItems()) {
            output.accept(item);
        }
        for (Item item : ModItems.orderedSpellBookItems()) {
            output.accept(item);
        }
        for (Realm realm : Realm.values()) {
            if (realm == Realm.LOOSE_IMMORTAL) continue;
            output.accept(ModItems.REALM_TOKENS.get(realm).get());
        }
        for (Map.Entry<Integer, RegistryObject<Item>> entry : ModItems.LOOSE_IMMORTAL_REALM_TOKENS.entrySet()) {
            output.accept(entry.getValue().get());
        }
        for (Realm realm : Realm.values()) {
            output.accept(ModItems.CULTIVATOR_SPAWN_EGGS.get(realm).get());
        }
        output.accept(ModItems.SOUL_REAPER_SPAWN_EGG.get());
    }).build());

    private ModCreativeTabs() {
    }

    public static void register(IEventBus bus) {
        CREATIVE_MODE_TABS.register(bus);
    }
}
