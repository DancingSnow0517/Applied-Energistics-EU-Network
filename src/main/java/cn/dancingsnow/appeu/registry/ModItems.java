package cn.dancingsnow.appeu.registry;

import cn.dancingsnow.appeu.item.ItemEUStorageCell;
import cn.dancingsnow.appeu.item.ItemEUStorageComponent;
import cpw.mods.fml.common.registry.GameRegistry;

public final class ModItems {

    public static final ItemEUStorageComponent EU_STORAGE_COMPONENT = new ItemEUStorageComponent();
    public static final ItemEUStorageCell EU_STORAGE_CELL = new ItemEUStorageCell();

    private ModItems() {}

    public static void register() {
        GameRegistry.registerItem(EU_STORAGE_COMPONENT, "eu_storage_component");
        GameRegistry.registerItem(EU_STORAGE_CELL, "eu_storage_cell");
    }
}
