package cn.dancingsnow.appeu.registry;

import java.util.HashMap;
import java.util.Map;

import net.minecraft.item.ItemStack;
import net.minecraftforge.oredict.OreDictionary;

import cn.dancingsnow.appeu.hatch.HatchDirection;
import cn.dancingsnow.appeu.hatch.HatchSpec;
import cn.dancingsnow.appeu.hatch.HatchSpecs;
import cn.dancingsnow.appeu.item.ItemEUDisplay;
import cn.dancingsnow.appeu.item.ItemEUStorageCell;
import cn.dancingsnow.appeu.item.ItemEUStorageCellHousing;
import cn.dancingsnow.appeu.item.ItemEUStorageComponent;
import cn.dancingsnow.appeu.item.ItemTitaniumCarbideMXeneSheet;
import cpw.mods.fml.common.registry.GameRegistry;
import gregtech.api.interfaces.IItemContainer;

public final class ModItems {

    public static final ItemEUDisplay EU_ENERGY_DISPLAY = new ItemEUDisplay();
    public static final ItemEUStorageCellHousing EU_STORAGE_CELL_HOUSING = new ItemEUStorageCellHousing();
    public static final ItemEUStorageComponent EU_STORAGE_COMPONENT = new ItemEUStorageComponent();
    public static final ItemEUStorageCell EU_STORAGE_CELL = new ItemEUStorageCell();
    public static final ItemTitaniumCarbideMXeneSheet TITANIUM_CARBIDE_MXENE_SHEET = new ItemTitaniumCarbideMXeneSheet();

    public static final IItemContainer[] ME_ENERGY_HATCH = createHatchContainers();
    public static final IItemContainer[] ME_DYNAMO_HATCH = createHatchContainers();
    public static final IItemContainer[] ME_ENERGY_HATCH_4A = createHatchContainers();
    public static final IItemContainer[] ME_DYNAMO_HATCH_4A = createHatchContainers();
    public static final IItemContainer[] ME_ENERGY_HATCH_16A = createHatchContainers();
    public static final IItemContainer[] ME_DYNAMO_HATCH_16A = createHatchContainers();
    public static final IItemContainer[] ME_ENERGY_HATCH_64A = createHatchContainers();
    public static final IItemContainer[] ME_DYNAMO_HATCH_64A = createHatchContainers();
    public static final IItemContainer[] ME_ENERGY_HATCH_256A = createHatchContainers();
    public static final IItemContainer[] ME_DYNAMO_HATCH_256A = createHatchContainers();
    public static final IItemContainer[] ME_ENERGY_HATCH_1024A = createHatchContainers();
    public static final IItemContainer[] ME_DYNAMO_HATCH_1024A = createHatchContainers();
    public static final IItemContainer[] ME_ENERGY_HATCH_4096A = createHatchContainers();
    public static final IItemContainer[] ME_DYNAMO_HATCH_4096A = createHatchContainers();
    public static final IItemContainer[] ME_ENERGY_HATCH_16384A = createHatchContainers();
    public static final IItemContainer[] ME_DYNAMO_HATCH_16384A = createHatchContainers();
    public static final IItemContainer[] ME_ENERGY_HATCH_65536A = createHatchContainers();
    public static final IItemContainer[] ME_DYNAMO_HATCH_65536A = createHatchContainers();
    public static final IItemContainer[] ME_ENERGY_HATCH_262144A = createHatchContainers();
    public static final IItemContainer[] ME_DYNAMO_HATCH_262144A = createHatchContainers();
    public static final IItemContainer[] ME_ENERGY_HATCH_1048576A = createHatchContainers();
    public static final IItemContainer[] ME_DYNAMO_HATCH_1048576A = createHatchContainers();

    private static final Map<Integer, IItemContainer[]> ENERGY_HATCHES_BY_AMPERAGE = new HashMap<>();
    private static final Map<Integer, IItemContainer[]> DYNAMO_HATCHES_BY_AMPERAGE = new HashMap<>();

    static {
        indexHatches(2, ME_ENERGY_HATCH, ME_DYNAMO_HATCH);
        indexHatches(4, ME_ENERGY_HATCH_4A, ME_DYNAMO_HATCH_4A);
        indexHatches(16, ME_ENERGY_HATCH_16A, ME_DYNAMO_HATCH_16A);
        indexHatches(64, ME_ENERGY_HATCH_64A, ME_DYNAMO_HATCH_64A);
        indexHatches(256, ME_ENERGY_HATCH_256A, ME_DYNAMO_HATCH_256A);
        indexHatches(1_024, ME_ENERGY_HATCH_1024A, ME_DYNAMO_HATCH_1024A);
        indexHatches(4_096, ME_ENERGY_HATCH_4096A, ME_DYNAMO_HATCH_4096A);
        indexHatches(16_384, ME_ENERGY_HATCH_16384A, ME_DYNAMO_HATCH_16384A);
        indexHatches(65_536, ME_ENERGY_HATCH_65536A, ME_DYNAMO_HATCH_65536A);
        indexHatches(262_144, ME_ENERGY_HATCH_262144A, ME_DYNAMO_HATCH_262144A);
        indexHatches(1_048_576, ME_ENERGY_HATCH_1048576A, ME_DYNAMO_HATCH_1048576A);
    }

    private ModItems() {}

    public static void register() {
        GameRegistry.registerItem(EU_ENERGY_DISPLAY, "eu_energy_display");
        GameRegistry.registerItem(EU_STORAGE_CELL_HOUSING, "eu_storage_cell_housing");
        GameRegistry.registerItem(EU_STORAGE_COMPONENT, "eu_storage_component");
        GameRegistry.registerItem(EU_STORAGE_CELL, "eu_storage_cell");
        GameRegistry.registerItem(TITANIUM_CARBIDE_MXENE_SHEET, "titanium_carbide_mxene_sheet");
        OreDictionary.registerOre("plateTitaniumCarbideMXene", TITANIUM_CARBIDE_MXENE_SHEET);
    }

    static void setHatch(HatchSpec spec, ItemStack stack) {
        Map<Integer, IItemContainer[]> hatchesByAmperage = spec.direction() == HatchDirection.ENERGY
            ? ENERGY_HATCHES_BY_AMPERAGE
            : DYNAMO_HATCHES_BY_AMPERAGE;
        IItemContainer[] hatches = hatchesByAmperage.get(spec.amperage());
        if (hatches == null) {
            return;
        }
        if (spec.tier() < HatchSpecs.MIN_TIER || spec.tier() >= hatches.length) {
            throw new IllegalArgumentException("Unsupported hatch tier: " + spec.tier());
        }
        hatches[spec.tier()].set(stack);
    }

    private static IItemContainer[] createHatchContainers() {
        IItemContainer[] containers = new IItemContainer[HatchSpecs.MAX_TIER + 1];
        for (int tier = 0; tier < containers.length; tier++) {
            containers[tier] = new MutableItemContainer();
        }
        return containers;
    }

    private static void indexHatches(int amperage, IItemContainer[] energyHatches, IItemContainer[] dynamoHatches) {
        ENERGY_HATCHES_BY_AMPERAGE.put(amperage, energyHatches);
        DYNAMO_HATCHES_BY_AMPERAGE.put(amperage, dynamoHatches);
    }
}
