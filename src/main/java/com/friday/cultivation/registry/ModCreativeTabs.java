/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.registries.Registries
 *  net.minecraft.network.chat.Component
 *  net.minecraft.resources.ResourceKey
 *  net.minecraft.world.item.CreativeModeTab
 *  net.minecraft.world.item.Item
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.level.ItemLike
 *  net.minecraftforge.eventbus.api.IEventBus
 *  net.minecraftforge.fml.ModList
 *  net.minecraftforge.registries.DeferredRegister
 *  net.minecraftforge.registries.RegistryObject
 */
package com.friday.cultivation.registry;

import com.friday.cultivation.cultivation.realm.Realm;
import com.friday.cultivation.registry.ModItems;
import java.util.Map;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public final class ModCreativeTabs {
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS = DeferredRegister.create((ResourceKey)Registries.CREATIVE_MODE_TAB, (String)"friday_cultivation");
    public static final RegistryObject<CreativeModeTab> MAIN_TAB = CREATIVE_MODE_TABS.register("main", () -> CreativeModeTab.builder().title((Component)Component.translatable((String)"itemGroup.friday_cultivation.main")).icon(() -> new ItemStack((ItemLike)ModItems.MID_SPIRIT_STONE.get())).displayItems((params, output) -> {
        output.accept((ItemLike)ModItems.LOW_SPIRIT_STONE.get());
        output.accept((ItemLike)ModItems.MID_SPIRIT_STONE.get());
        output.accept((ItemLike)ModItems.HIGH_SPIRIT_STONE.get());
        output.accept((ItemLike)ModItems.SUPREME_SPIRIT_STONE.get());
        output.accept((ItemLike)ModItems.LOW_SPIRIT_STONE_ORE.get());
        output.accept((ItemLike)ModItems.MID_SPIRIT_STONE_ORE.get());
        output.accept((ItemLike)ModItems.HIGH_SPIRIT_STONE_ORE.get());
        output.accept((ItemLike)ModItems.SUPREME_SPIRIT_STONE_ORE.get());
        output.accept((ItemLike)ModItems.SPIRIT_VEIN_SPRING.get());
        output.accept((ItemLike)ModItems.DIVINATION_COMPASS.get());
        output.accept((ItemLike)ModItems.FORMATION_COMPASS.get());
        output.accept((ItemLike)ModItems.FORMATION_INSCRIPTION_KNIFE.get());
        if (ModList.get().isLoaded("patchouli")) {
            output.accept((ItemLike)ModItems.CULTIVATION_COMPENDIUM.get());
        }
        output.accept((ItemLike)ModItems.INK.get());
        output.accept((ItemLike)ModItems.TALISMAN_PAPER.get());
        output.accept((ItemLike)ModItems.ORIGIN_RECONFIGURATION_TOKEN.get());
        output.accept((ItemLike)ModItems.REINCARNATION_FATE_PLATE.get());
        output.accept((ItemLike)ModItems.RECALL_PILL.get());
        output.accept((ItemLike)ModItems.SOUL_HOOK.get());
        output.accept((ItemLike)ModItems.SOUL_REAPER_TOKEN.get());
        output.accept((ItemLike)ModItems.YOUTH_PILL.get());
        output.accept((ItemLike)ModItems.SEX_CHANGE_PILL.get());
        output.accept((ItemLike)ModItems.ZHUJI_DAN.get());
        output.accept((ItemLike)ModItems.BLOOD_SPIRIT_PILL.get());
        output.accept((ItemLike)ModItems.DAO_FOUNDATION_FRUIT.get());
        output.accept((ItemLike)ModItems.FOUNDATION_SECRET.get());
        output.accept((ItemLike)ModItems.JIEDAN_PILL.get());
        output.accept((ItemLike)ModItems.BLOOD_JIEDAN_PILL.get());
        output.accept((ItemLike)ModItems.ALL_CREATURES_TRUE_BLOOD.get());
        output.accept((ItemLike)ModItems.EARTH_EVIL_QI.get());
        output.accept((ItemLike)ModItems.HEAVEN_CLEAR_QI.get());
        output.accept((ItemLike)ModItems.NINGZHEN_CREATION_FRUIT.get());
        output.accept((ItemLike)ModItems.BLOOD_TRANSFORMATION_TALISMAN.get());
        output.accept((ItemLike)ModItems.CUSHION.get());
        output.accept((ItemLike)ModItems.BONE_BLOCK.get());
        output.accept((ItemLike)ModItems.ALCHEMY_CORE.get());
        output.accept((ItemLike)ModItems.REFINING_CORE.get());
        output.accept((ItemLike)ModItems.LOW_FORMATION_CORE_PLATE.get());
        output.accept((ItemLike)ModItems.MID_FORMATION_CORE_PLATE.get());
        output.accept((ItemLike)ModItems.HIGH_FORMATION_CORE_PLATE.get());
        output.accept((ItemLike)ModItems.SUPREME_FORMATION_CORE_PLATE.get());
        output.accept((ItemLike)ModItems.IMMORTAL_FORMATION_CORE_PLATE.get());
        output.accept((ItemLike)ModItems.LOW_QI_GATHERING_FLAG.get());
        output.accept((ItemLike)ModItems.MID_QI_GATHERING_FLAG.get());
        output.accept((ItemLike)ModItems.HIGH_QI_GATHERING_FLAG.get());
        output.accept((ItemLike)ModItems.SUPREME_QI_GATHERING_FLAG.get());
        output.accept((ItemLike)ModItems.IMMORTAL_QI_GATHERING_FLAG.get());
        output.accept((ItemLike)ModItems.LOW_SECT_PROTECTION_FLAG.get());
        output.accept((ItemLike)ModItems.MID_SECT_PROTECTION_FLAG.get());
        output.accept((ItemLike)ModItems.HIGH_SECT_PROTECTION_FLAG.get());
        output.accept((ItemLike)ModItems.SUPREME_SECT_PROTECTION_FLAG.get());
        output.accept((ItemLike)ModItems.IMMORTAL_SECT_PROTECTION_FLAG.get());
        output.accept((ItemLike)ModItems.LOW_WITHER_GROWTH_FLAG.get());
        output.accept((ItemLike)ModItems.MID_WITHER_GROWTH_FLAG.get());
        output.accept((ItemLike)ModItems.HIGH_WITHER_GROWTH_FLAG.get());
        output.accept((ItemLike)ModItems.SUPREME_WITHER_GROWTH_FLAG.get());
        output.accept((ItemLike)ModItems.IMMORTAL_WITHER_GROWTH_FLAG.get());
        output.accept((ItemLike)ModItems.LOW_REJUVENATION_FLAG.get());
        output.accept((ItemLike)ModItems.MID_REJUVENATION_FLAG.get());
        output.accept((ItemLike)ModItems.HIGH_REJUVENATION_FLAG.get());
        output.accept((ItemLike)ModItems.SUPREME_REJUVENATION_FLAG.get());
        output.accept((ItemLike)ModItems.IMMORTAL_REJUVENATION_FLAG.get());
        output.accept((ItemLike)ModItems.LOW_FLIGHT_BAN_FLAG.get());
        output.accept((ItemLike)ModItems.MID_FLIGHT_BAN_FLAG.get());
        output.accept((ItemLike)ModItems.HIGH_FLIGHT_BAN_FLAG.get());
        output.accept((ItemLike)ModItems.SUPREME_FLIGHT_BAN_FLAG.get());
        output.accept((ItemLike)ModItems.IMMORTAL_FLIGHT_BAN_FLAG.get());
        output.accept((ItemLike)ModItems.LOW_MAZE_FLAG.get());
        output.accept((ItemLike)ModItems.MID_MAZE_FLAG.get());
        output.accept((ItemLike)ModItems.HIGH_MAZE_FLAG.get());
        output.accept((ItemLike)ModItems.SUPREME_MAZE_FLAG.get());
        output.accept((ItemLike)ModItems.IMMORTAL_MAZE_FLAG.get());
        output.accept((ItemLike)ModItems.LOW_FARM_HARVEST_FLAG.get());
        output.accept((ItemLike)ModItems.MID_FARM_HARVEST_FLAG.get());
        output.accept((ItemLike)ModItems.HIGH_FARM_HARVEST_FLAG.get());
        output.accept((ItemLike)ModItems.SUPREME_FARM_HARVEST_FLAG.get());
        output.accept((ItemLike)ModItems.IMMORTAL_FARM_HARVEST_FLAG.get());
        output.accept((ItemLike)ModItems.SECT_TOKEN.get());
        output.accept((ItemLike)ModItems.LOW_SPIRIT_VEIN_CORE.get());
        output.accept((ItemLike)ModItems.MID_SPIRIT_VEIN_CORE.get());
        output.accept((ItemLike)ModItems.HIGH_SPIRIT_VEIN_CORE.get());
        output.accept((ItemLike)ModItems.SUPREME_SPIRIT_VEIN_CORE.get());
        output.accept((ItemLike)ModItems.IMMORTAL_SPIRIT_VEIN_CORE.get());
        output.accept((ItemLike)ModItems.XUAN_IRON_SWORD_LOW.get());
        output.accept((ItemLike)ModItems.XUAN_IRON_SWORD_MID.get());
        output.accept((ItemLike)ModItems.XUAN_IRON_SWORD_HIGH.get());
        output.accept((ItemLike)ModItems.XUAN_IRON_SWORD_SUPREME.get());
        output.accept((ItemLike)ModItems.XUAN_IRON_SWORD_IMMORTAL.get());
        output.accept((ItemLike)ModItems.XUAN_IRON_SWORD_SAGE.get());
        output.accept((ItemLike)ModItems.XUAN_IRON_SWORD_GREAT_EMPEROR.get());
        output.accept((ItemLike)ModItems.QING_MU_SWORD_LOW.get());
        output.accept((ItemLike)ModItems.QING_MU_SWORD_MID.get());
        output.accept((ItemLike)ModItems.QING_MU_SWORD_HIGH.get());
        output.accept((ItemLike)ModItems.QING_MU_SWORD_SUPREME.get());
        output.accept((ItemLike)ModItems.QING_MU_SWORD_IMMORTAL.get());
        output.accept((ItemLike)ModItems.QING_MU_SWORD_SAGE.get());
        output.accept((ItemLike)ModItems.QING_MU_SWORD_GREAT_EMPEROR.get());
        output.accept((ItemLike)ModItems.CHI_YAN_SWORD_LOW.get());
        output.accept((ItemLike)ModItems.CHI_YAN_SWORD_MID.get());
        output.accept((ItemLike)ModItems.CHI_YAN_SWORD_HIGH.get());
        output.accept((ItemLike)ModItems.CHI_YAN_SWORD_SUPREME.get());
        output.accept((ItemLike)ModItems.CHI_YAN_SWORD_IMMORTAL.get());
        output.accept((ItemLike)ModItems.CHI_YAN_SWORD_SAGE.get());
        output.accept((ItemLike)ModItems.CHI_YAN_SWORD_GREAT_EMPEROR.get());
        output.accept((ItemLike)ModItems.HAN_BING_SWORD_LOW.get());
        output.accept((ItemLike)ModItems.HAN_BING_SWORD_MID.get());
        output.accept((ItemLike)ModItems.HAN_BING_SWORD_HIGH.get());
        output.accept((ItemLike)ModItems.HAN_BING_SWORD_SUPREME.get());
        output.accept((ItemLike)ModItems.HAN_BING_SWORD_IMMORTAL.get());
        output.accept((ItemLike)ModItems.HAN_BING_SWORD_SAGE.get());
        output.accept((ItemLike)ModItems.HAN_BING_SWORD_GREAT_EMPEROR.get());
        output.accept((ItemLike)ModItems.HERB.get());
        output.accept((ItemLike)ModItems.PILL_QI_RECOVERY_LOW.get());
        output.accept((ItemLike)ModItems.PILL_QI_RECOVERY_MID.get());
        output.accept((ItemLike)ModItems.PILL_QI_RECOVERY_HIGH.get());
        output.accept((ItemLike)ModItems.PILL_QI_RECOVERY_SUPREME.get());
        output.accept((ItemLike)ModItems.PILL_QI_RECOVERY_IMMORTAL.get());
        output.accept((ItemLike)ModItems.PILL_CULTIVATION_LOW.get());
        output.accept((ItemLike)ModItems.PILL_CULTIVATION_MID.get());
        output.accept((ItemLike)ModItems.PILL_CULTIVATION_HIGH.get());
        output.accept((ItemLike)ModItems.PILL_CULTIVATION_SUPREME.get());
        output.accept((ItemLike)ModItems.PILL_CULTIVATION_IMMORTAL.get());
        output.accept((ItemLike)ModItems.PILL_BLOOD_BURN_LOW.get());
        output.accept((ItemLike)ModItems.PILL_BLOOD_BURN_MID.get());
        output.accept((ItemLike)ModItems.PILL_BLOOD_BURN_HIGH.get());
        output.accept((ItemLike)ModItems.PILL_BLOOD_BURN_SUPREME.get());
        output.accept((ItemLike)ModItems.PILL_BLOOD_BURN_IMMORTAL.get());
        output.accept((ItemLike)ModItems.PILL_CLEAR_MIND_LOW.get());
        output.accept((ItemLike)ModItems.PILL_CLEAR_MIND_MID.get());
        output.accept((ItemLike)ModItems.PILL_CLEAR_MIND_HIGH.get());
        output.accept((ItemLike)ModItems.PILL_CLEAR_MIND_SUPREME.get());
        output.accept((ItemLike)ModItems.PILL_CLEAR_MIND_IMMORTAL.get());
        output.accept((ItemLike)ModItems.PILL_REJUVENATION_LOW.get());
        output.accept((ItemLike)ModItems.PILL_REJUVENATION_MID.get());
        output.accept((ItemLike)ModItems.PILL_REJUVENATION_HIGH.get());
        output.accept((ItemLike)ModItems.PILL_REJUVENATION_SUPREME.get());
        output.accept((ItemLike)ModItems.PILL_REJUVENATION_IMMORTAL.get());
        output.accept((ItemLike)ModItems.PILL_DIVINE_STRIDE_LOW.get());
        output.accept((ItemLike)ModItems.PILL_DIVINE_STRIDE_MID.get());
        output.accept((ItemLike)ModItems.PILL_DIVINE_STRIDE_HIGH.get());
        output.accept((ItemLike)ModItems.PILL_DIVINE_STRIDE_SUPREME.get());
        output.accept((ItemLike)ModItems.PILL_DIVINE_STRIDE_IMMORTAL.get());
        for (Item item : ModItems.orderedTechniqueBookItems()) {
            output.accept((ItemLike)item);
        }
        for (Item item : ModItems.orderedSpellBookItems()) {
            output.accept((ItemLike)item);
        }
        output.accept((ItemLike)ModItems.REALM_SELECTOR_TOKEN.get());
        for (Object realm : Realm.values()) {
            output.accept((ItemLike)ModItems.CULTIVATOR_SPAWN_EGGS.get(realm).get());
        }
        output.accept((ItemLike)ModItems.SOUL_REAPER_SPAWN_EGG.get());
    }).build());

    private ModCreativeTabs() {
    }

    public static void register(IEventBus bus) {
        CREATIVE_MODE_TABS.register(bus);
    }
}

