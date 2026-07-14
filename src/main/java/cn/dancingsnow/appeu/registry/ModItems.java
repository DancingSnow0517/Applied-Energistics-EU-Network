package cn.dancingsnow.appeu.registry;

import cn.dancingsnow.appeu.item.ItemEUDisplay;
import cn.dancingsnow.appeu.item.ItemEUStorageCell;
import cn.dancingsnow.appeu.item.ItemEUStorageCellHousing;
import cn.dancingsnow.appeu.item.ItemEUStorageComponent;
import cpw.mods.fml.common.registry.GameRegistry;

public final class ModItems {

    public static final ItemEUDisplay EU_ENERGY_DISPLAY = new ItemEUDisplay();
    public static final ItemEUStorageCellHousing EU_STORAGE_CELL_HOUSING = new ItemEUStorageCellHousing();
    public static final ItemEUStorageComponent EU_STORAGE_COMPONENT = new ItemEUStorageComponent();
    public static final ItemEUStorageCell EU_STORAGE_CELL = new ItemEUStorageCell();

    private ModItems() {}

    public static void register() {
        GameRegistry.registerItem(EU_ENERGY_DISPLAY, "eu_energy_display");
        GameRegistry.registerItem(EU_STORAGE_CELL_HOUSING, "eu_storage_cell_housing");
        GameRegistry.registerItem(EU_STORAGE_COMPONENT, "eu_storage_component");
        GameRegistry.registerItem(EU_STORAGE_CELL, "eu_storage_cell");
    }
}
