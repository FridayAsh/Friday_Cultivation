package com.friday.cultivation.registry;

import com.friday.cultivation.inventory.AlchemyMenu;
import com.friday.cultivation.inventory.FormationMenu;
import com.friday.cultivation.inventory.RefiningMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class ModMenuTypes {
    public static final DeferredRegister<MenuType<?>> MENU_TYPES =
            DeferredRegister.create(ForgeRegistries.MENU_TYPES, "friday_cultivation");

    public static final RegistryObject<MenuType<AlchemyMenu>> ALCHEMY =
            MENU_TYPES.register("alchemy", () -> IForgeMenuType.create((id, inv, buf) -> new AlchemyMenu(id, inv, buf)));

    // 炼器菜单 (Phase 13 续 — 完整版)
    public static final RegistryObject<MenuType<RefiningMenu>> REFINING =
            MENU_TYPES.register("refining", () -> IForgeMenuType.create((id, inv, buf) -> new RefiningMenu(id, inv, buf)));

    // 阵法核心盘菜单
    public static final RegistryObject<MenuType<FormationMenu>> FORMATION_MENU =
            MENU_TYPES.register("formation", () -> IForgeMenuType.create((id, inv, buf) -> new FormationMenu(id, inv, buf.readBlockPos())));

    // 散修交易菜单
    public static final RegistryObject<MenuType<com.friday.cultivation.inventory.WanderingCultivatorMenu>> WANDERING_CULTIVATOR =
            MENU_TYPES.register("wandering_cultivator", () -> IForgeMenuType.create((id, inv, buf) -> new com.friday.cultivation.inventory.WanderingCultivatorMenu(id, inv, buf.readVarInt())));

    private ModMenuTypes() {}
    public static void register(IEventBus bus) { MENU_TYPES.register(bus); }
}
