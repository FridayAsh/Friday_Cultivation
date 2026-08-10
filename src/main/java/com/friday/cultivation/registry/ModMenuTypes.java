/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.world.inventory.MenuType
 *  net.minecraftforge.common.extensions.IForgeMenuType
 *  net.minecraftforge.eventbus.api.IEventBus
 *  net.minecraftforge.registries.DeferredRegister
 *  net.minecraftforge.registries.ForgeRegistries
 *  net.minecraftforge.registries.IForgeRegistry
 *  net.minecraftforge.registries.RegistryObject
 */
package com.friday.cultivation.registry;

import com.friday.cultivation.inventory.AlchemyMenu;
import com.friday.cultivation.inventory.FormationMenu;
import com.friday.cultivation.inventory.RefiningMenu;
import com.friday.cultivation.inventory.WanderingCultivatorMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.RegistryObject;

public final class ModMenuTypes {
    public static final DeferredRegister<MenuType<?>> MENUS = DeferredRegister.create((IForgeRegistry)ForgeRegistries.MENU_TYPES, (String)"friday_cultivation");
    public static final RegistryObject<MenuType<WanderingCultivatorMenu>> WANDERING_CULTIVATOR = MENUS.register("wandering_cultivator", () -> IForgeMenuType.create((containerId, playerInv, buf) -> {
        int entityId = buf.readVarInt();
        return new WanderingCultivatorMenu(containerId, playerInv, entityId);
    }));
    public static final RegistryObject<MenuType<AlchemyMenu>> ALCHEMY = MENUS.register("alchemy", () -> IForgeMenuType.create(AlchemyMenu::new));
    public static final RegistryObject<MenuType<RefiningMenu>> REFINING = MENUS.register("refining", () -> IForgeMenuType.create(RefiningMenu::new));
    public static final RegistryObject<MenuType<FormationMenu>> FORMATION = MENUS.register("formation", () -> IForgeMenuType.create(FormationMenu::new));

    private ModMenuTypes() {
    }

    public static void register(IEventBus bus) {
        MENUS.register(bus);
    }
}

