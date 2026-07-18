package cn.dancingsnow.appeu.registry;

import static gregtech.api.recipe.RecipeMaps.assemblerRecipes;
import static gregtech.api.util.GTRecipeBuilder.INGOTS;
import static gregtech.api.util.GTRecipeBuilder.MINUTES;
import static gregtech.api.util.GTRecipeConstants.AssemblyLine;
import static gregtech.api.util.GTRecipeConstants.RESEARCH_ITEM;
import static gregtech.api.util.GTRecipeConstants.SCANNING;

import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;

import appeng.api.AEApi;
import gregtech.api.enums.GTValues;
import gregtech.api.enums.ItemList;
import gregtech.api.enums.Materials;
import gregtech.api.enums.OrePrefixes;
import gregtech.api.interfaces.IItemContainer;
import gregtech.api.util.GTRecipeBuilder;
import gregtech.api.util.recipe.Scanning;
import gtPlusPlus.core.material.MaterialsAlloy;
import tectech.thing.CustomItemList;

public final class RecipeRegistration {

    private RecipeRegistration() {}

    // region static
    // spotless:off
    private static final Materials[] circuitMaterials = new Materials[]{
        Materials.ULV,
        Materials.LV,
        Materials.MV,
        Materials.HV,
        Materials.EV,
        Materials.IV,
        Materials.LuV,
        Materials.ZPM,
        Materials.UV,
        Materials.UHV,
        Materials.UEV,
        Materials.UIV,
        Materials.UMV,
        Materials.UXV
    };

    private static final FluidStack[] tierFluids = new FluidStack[]{
        Materials.Tin.getMolten(1),
        Materials.Cupronickel.getMolten(1),
        Materials.Kanthal.getMolten(1),
        Materials.Nichrome.getMolten(1),
        Materials.TPV.getMolten(1),
        Materials.HSSG.getMolten(1),
        Materials.HSSS.getMolten(1),
        Materials.Naquadah.getMolten(1),
        Materials.NaquadahAlloy.getMolten(1),
        Materials.Trinium.getMolten(1),
        Materials.ElectrumFlux.getMolten(1),
        Materials.DraconiumAwakened.getMolten(1),
        Materials.Infinity.getMolten(1),
        new FluidStack(FluidRegistry.getFluid("molten.hypogen"), 1)
    };

    private static final IItemContainer[] multiEnergyHatch4A = new IItemContainer[]{
        null,
        null,
        null,
        null,
        CustomItemList.eM_energyMulti4_EV,
        CustomItemList.eM_energyMulti4_IV,
        CustomItemList.eM_energyMulti4_LuV,
        CustomItemList.eM_energyMulti4_ZPM,
        CustomItemList.eM_energyMulti4_UV,
        CustomItemList.eM_energyMulti4_UHV,
        CustomItemList.eM_energyMulti4_UEV,
        CustomItemList.eM_energyMulti4_UIV,
        CustomItemList.eM_energyMulti4_UMV,
        CustomItemList.eM_energyMulti4_UXV,
    };
    private static final IItemContainer[] multiEnergyHatch16A = new IItemContainer[]{
        null,
        null,
        null,
        null,
        CustomItemList.eM_energyMulti16_EV,
        CustomItemList.eM_energyMulti16_IV,
        CustomItemList.eM_energyMulti16_LuV,
        CustomItemList.eM_energyMulti16_ZPM,
        CustomItemList.eM_energyMulti16_UV,
        CustomItemList.eM_energyMulti16_UHV,
        CustomItemList.eM_energyMulti16_UEV,
        CustomItemList.eM_energyMulti16_UIV,
        CustomItemList.eM_energyMulti16_UMV,
        CustomItemList.eM_energyMulti16_UXV,
    };
    private static final IItemContainer[] multiEnergyHatch64A = new IItemContainer[]{
        null,
        null,
        null,
        null,
        CustomItemList.eM_energyMulti64_EV,
        CustomItemList.eM_energyMulti64_IV,
        CustomItemList.eM_energyMulti64_LuV,
        CustomItemList.eM_energyMulti64_ZPM,
        CustomItemList.eM_energyMulti64_UV,
        CustomItemList.eM_energyMulti64_UHV,
        CustomItemList.eM_energyMulti64_UEV,
        CustomItemList.eM_energyMulti64_UIV,
        CustomItemList.eM_energyMulti64_UMV,
        CustomItemList.eM_energyMulti64_UXV,
    };
    private static final IItemContainer[] multiDynamoHatch4A = new IItemContainer[]{
        null,
        null,
        null,
        null,
        CustomItemList.eM_dynamoMulti4_EV,
        CustomItemList.eM_dynamoMulti4_IV,
        CustomItemList.eM_dynamoMulti4_LuV,
        CustomItemList.eM_dynamoMulti4_ZPM,
        CustomItemList.eM_dynamoMulti4_UV,
        CustomItemList.eM_dynamoMulti4_UHV,
        CustomItemList.eM_dynamoMulti4_UEV,
        CustomItemList.eM_dynamoMulti4_UIV,
        CustomItemList.eM_dynamoMulti4_UMV,
        CustomItemList.eM_dynamoMulti4_UXV,
    };
    private static final IItemContainer[] multiDynamoHatch16A = new IItemContainer[]{
        null,
        null,
        null,
        null,
        CustomItemList.eM_dynamoMulti16_EV,
        CustomItemList.eM_dynamoMulti16_IV,
        CustomItemList.eM_dynamoMulti16_LuV,
        CustomItemList.eM_dynamoMulti16_ZPM,
        CustomItemList.eM_dynamoMulti16_UV,
        CustomItemList.eM_dynamoMulti16_UHV,
        CustomItemList.eM_dynamoMulti16_UEV,
        CustomItemList.eM_dynamoMulti16_UIV,
        CustomItemList.eM_dynamoMulti16_UMV,
        CustomItemList.eM_dynamoMulti16_UXV,
    };
    private static final IItemContainer[] multiDynamoHatch64A = new IItemContainer[]{
        null,
        null,
        null,
        null,
        CustomItemList.eM_dynamoMulti64_EV,
        CustomItemList.eM_dynamoMulti64_IV,
        CustomItemList.eM_dynamoMulti64_LuV,
        CustomItemList.eM_dynamoMulti64_ZPM,
        CustomItemList.eM_dynamoMulti64_UV,
        CustomItemList.eM_dynamoMulti64_UHV,
        CustomItemList.eM_dynamoMulti64_UEV,
        CustomItemList.eM_dynamoMulti64_UIV,
        CustomItemList.eM_dynamoMulti64_UMV,
        CustomItemList.eM_dynamoMulti64_UXV,
    };
    private static final IItemContainer[] energyTunnel256A = new IItemContainer[]{
        null,
        null,
        null,
        null,
        null,
        CustomItemList.eM_energyTunnel1_IV,
        CustomItemList.eM_energyTunnel1_LuV,
        CustomItemList.eM_energyTunnel1_ZPM,
        CustomItemList.eM_energyTunnel1_UV,
        CustomItemList.eM_energyTunnel1_UHV,
        CustomItemList.eM_energyTunnel1_UEV,
        CustomItemList.eM_energyTunnel1_UIV,
        CustomItemList.eM_energyTunnel1_UMV,
        CustomItemList.eM_energyTunnel1_UXV
    };
    private static final IItemContainer[] energyTunnel1024A = new IItemContainer[]{
        null,
        null,
        null,
        null,
        null,
        null,
        CustomItemList.eM_energyTunnel2_LuV,
        CustomItemList.eM_energyTunnel2_ZPM,
        CustomItemList.eM_energyTunnel2_UV,
        CustomItemList.eM_energyTunnel2_UHV,
        CustomItemList.eM_energyTunnel2_UEV,
        CustomItemList.eM_energyTunnel2_UIV,
        CustomItemList.eM_energyTunnel2_UMV,
        CustomItemList.eM_energyTunnel2_UXV
    };
    private static final IItemContainer[] energyTunnel4096A = new IItemContainer[]{
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        CustomItemList.eM_energyTunnel3_ZPM,
        CustomItemList.eM_energyTunnel3_UV,
        CustomItemList.eM_energyTunnel3_UHV,
        CustomItemList.eM_energyTunnel3_UEV,
        CustomItemList.eM_energyTunnel3_UIV,
        CustomItemList.eM_energyTunnel3_UMV,
        CustomItemList.eM_energyTunnel3_UXV
    };
    private static final IItemContainer[] energyTunnel16384A = new IItemContainer[]{
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        CustomItemList.eM_energyTunnel4_UV,
        CustomItemList.eM_energyTunnel4_UHV,
        CustomItemList.eM_energyTunnel4_UEV,
        CustomItemList.eM_energyTunnel4_UIV,
        CustomItemList.eM_energyTunnel4_UMV,
        CustomItemList.eM_energyTunnel4_UXV
    };
    private static final IItemContainer[] energyTunnel65536A = new IItemContainer[]{
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        CustomItemList.eM_energyTunnel5_UHV,
        CustomItemList.eM_energyTunnel5_UEV,
        CustomItemList.eM_energyTunnel5_UIV,
        CustomItemList.eM_energyTunnel5_UMV,
        CustomItemList.eM_energyTunnel5_UXV
    };
    private static final IItemContainer[] energyTunnel262144A = new IItemContainer[]{
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        CustomItemList.eM_energyTunnel6_UEV,
        CustomItemList.eM_energyTunnel6_UIV,
        CustomItemList.eM_energyTunnel6_UMV,
        CustomItemList.eM_energyTunnel6_UXV
    };
    private static final IItemContainer[] energyTunnel1048576A = new IItemContainer[]{
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        CustomItemList.eM_energyTunnel7_UIV,
        CustomItemList.eM_energyTunnel7_UMV,
        CustomItemList.eM_energyTunnel7_UXV
    };
    private static final IItemContainer[] dynamoTunnel256A = new IItemContainer[]{
        null,
        null,
        null,
        null,
        null,
        CustomItemList.eM_dynamoTunnel1_IV,
        CustomItemList.eM_dynamoTunnel1_LuV,
        CustomItemList.eM_dynamoTunnel1_ZPM,
        CustomItemList.eM_dynamoTunnel1_UV,
        CustomItemList.eM_dynamoTunnel1_UHV,
        CustomItemList.eM_dynamoTunnel1_UEV,
        CustomItemList.eM_dynamoTunnel1_UIV,
        CustomItemList.eM_dynamoTunnel1_UMV,
        CustomItemList.eM_dynamoTunnel1_UXV
    };
    private static final IItemContainer[] dynamoTunnel1024A = new IItemContainer[]{
        null,
        null,
        null,
        null,
        null,
        null,
        CustomItemList.eM_dynamoTunnel2_LuV,
        CustomItemList.eM_dynamoTunnel2_ZPM,
        CustomItemList.eM_dynamoTunnel2_UV,
        CustomItemList.eM_dynamoTunnel2_UHV,
        CustomItemList.eM_dynamoTunnel2_UEV,
        CustomItemList.eM_dynamoTunnel2_UIV,
        CustomItemList.eM_dynamoTunnel2_UMV,
        CustomItemList.eM_dynamoTunnel2_UXV
    };
    private static final IItemContainer[] dynamoTunnel4096A = new IItemContainer[]{
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        CustomItemList.eM_dynamoTunnel3_ZPM,
        CustomItemList.eM_dynamoTunnel3_UV,
        CustomItemList.eM_dynamoTunnel3_UHV,
        CustomItemList.eM_dynamoTunnel3_UEV,
        CustomItemList.eM_dynamoTunnel3_UIV,
        CustomItemList.eM_dynamoTunnel3_UMV,
        CustomItemList.eM_dynamoTunnel3_UXV
    };
    private static final IItemContainer[] dynamoTunnel16384A = new IItemContainer[]{
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        CustomItemList.eM_dynamoTunnel4_UV,
        CustomItemList.eM_dynamoTunnel4_UHV,
        CustomItemList.eM_dynamoTunnel4_UEV,
        CustomItemList.eM_dynamoTunnel4_UIV,
        CustomItemList.eM_dynamoTunnel4_UMV,
        CustomItemList.eM_dynamoTunnel4_UXV
    };
    private static final IItemContainer[] dynamoTunnel65536A = new IItemContainer[]{
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        CustomItemList.eM_dynamoTunnel5_UHV,
        CustomItemList.eM_dynamoTunnel5_UEV,
        CustomItemList.eM_dynamoTunnel5_UIV,
        CustomItemList.eM_dynamoTunnel5_UMV,
        CustomItemList.eM_dynamoTunnel5_UXV
    };
    private static final IItemContainer[] dynamoTunnel262144A = new IItemContainer[]{
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        CustomItemList.eM_dynamoTunnel6_UEV,
        CustomItemList.eM_dynamoTunnel6_UIV,
        CustomItemList.eM_dynamoTunnel6_UMV,
        CustomItemList.eM_dynamoTunnel6_UXV
    };
    private static final IItemContainer[] dynamoTunnel1048576A = new IItemContainer[]{
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        CustomItemList.eM_dynamoTunnel7_UIV,
        CustomItemList.eM_dynamoTunnel7_UMV,
        CustomItemList.eM_dynamoTunnel7_UXV
    };
    // spotless:on
    // endregion

    public static void register() {
        registerHatchRecipes(ItemList.HATCHES_DYNAMO, ModItems.ME_DYNAMO_HATCH);
        registerHatchRecipes(ItemList.HATCHES_ENERGY, ModItems.ME_ENERGY_HATCH);

        registerMultiHatchRecipes(multiDynamoHatch4A, ModItems.ME_DYNAMO_HATCH_4A, 4, 4);
        registerMultiHatchRecipes(multiEnergyHatch4A, ModItems.ME_ENERGY_HATCH_4A, 4, 4);
        registerMultiHatchRecipes(multiDynamoHatch16A, ModItems.ME_DYNAMO_HATCH_16A, 4, 16);
        registerMultiHatchRecipes(multiEnergyHatch16A, ModItems.ME_ENERGY_HATCH_16A, 4, 16);
        registerMultiHatchRecipes(multiDynamoHatch64A, ModItems.ME_DYNAMO_HATCH_64A, 4, 64);
        registerMultiHatchRecipes(multiEnergyHatch64A, ModItems.ME_ENERGY_HATCH_64A, 4, 64);
        registerMultiHatchRecipes(dynamoTunnel256A, ModItems.ME_DYNAMO_HATCH_256A, 5, 64);
        registerMultiHatchRecipes(energyTunnel256A, ModItems.ME_ENERGY_HATCH_256A, 5, 64);
        registerMultiHatchRecipes(dynamoTunnel1024A, ModItems.ME_DYNAMO_HATCH_1024A, 6, 64);
        registerMultiHatchRecipes(energyTunnel1024A, ModItems.ME_ENERGY_HATCH_1024A, 6, 64);
        registerMultiHatchRecipes(dynamoTunnel4096A, ModItems.ME_DYNAMO_HATCH_4096A, 7, 64);
        registerMultiHatchRecipes(energyTunnel4096A, ModItems.ME_ENERGY_HATCH_4096A, 7, 64);
        registerMultiHatchRecipes(dynamoTunnel16384A, ModItems.ME_DYNAMO_HATCH_16384A, 8, 64);
        registerMultiHatchRecipes(energyTunnel16384A, ModItems.ME_ENERGY_HATCH_16384A, 8, 64);
        registerMultiHatchRecipes(dynamoTunnel65536A, ModItems.ME_DYNAMO_HATCH_65536A, 9, 64);
        registerMultiHatchRecipes(energyTunnel65536A, ModItems.ME_ENERGY_HATCH_65536A, 9, 64);
        registerMultiHatchRecipes(dynamoTunnel262144A, ModItems.ME_DYNAMO_HATCH_262144A, 10, 64);
        registerMultiHatchRecipes(energyTunnel262144A, ModItems.ME_ENERGY_HATCH_262144A, 10, 64);
        registerMultiHatchRecipes(dynamoTunnel1048576A, ModItems.ME_DYNAMO_HATCH_1048576A, 11, 64);
        registerMultiHatchRecipes(energyTunnel1048576A, ModItems.ME_ENERGY_HATCH_1048576A, 11, 64);
    }

    private static void registerHatchRecipes(IItemContainer[] standardHatches, IItemContainer[] meStandardHatches) {
        ItemStack interfaceItem = AEApi.instance()
            .definitions()
            .blocks()
            .iface()
            .maybeStack(1)
            .orNull();
        ItemStack energyCellDenseItem = AEApi.instance()
            .definitions()
            .blocks()
            .energyCellDense()
            .maybeStack(2)
            .orNull();
        if (interfaceItem == null || energyCellDenseItem == null) {
            return;
        }

        for (int tier = 0; tier <= 14; tier++) {
            if (tier >= standardHatches.length || tier >= meStandardHatches.length) {
                continue;
            }
            ItemStack inputHatch;
            ItemStack outputHatch;
            try {
                inputHatch = standardHatches[tier].get(1);
                outputHatch = meStandardHatches[tier].get(1);

            } catch (Exception e) {
                continue;
            }
            if (tier <= 5) {
                GTRecipeBuilder.builder()
                    .itemInputs(
                        inputHatch,
                        interfaceItem.copy(),
                        new Object[] { OrePrefixes.circuit.get(circuitMaterials[tier]), 2 })
                    .fluidInputs(new FluidStack(tierFluids[tier], 144))
                    .itemOutputs(outputHatch)
                    .duration(15 * GTRecipeBuilder.SECONDS)
                    .eut(GTValues.VP[tier])
                    .addTo(assemblerRecipes);
            } else {
                ItemStack researchHatch = meStandardHatches[tier - 1].get(1);
                GTRecipeBuilder.builder()
                    .metadata(RESEARCH_ITEM, researchHatch)
                    .metadata(SCANNING, new Scanning(2 * MINUTES, GTValues.VP[tier]))
                    .itemInputs(
                        inputHatch,
                        interfaceItem.copy(),
                        energyCellDenseItem.copy(),
                        new Object[] { OrePrefixes.circuit.get(circuitMaterials[tier]), 2 })
                    .fluidInputs(
                        MaterialsAlloy.INDALLOY_140.getFluidStack(8 * INGOTS),
                        new FluidStack(tierFluids[tier], 144))
                    .itemOutputs(outputHatch)
                    .duration(30 * GTRecipeBuilder.SECONDS)
                    .eut(GTValues.VP[tier])
                    .addTo(AssemblyLine);
            }
        }
    }

    private static void registerMultiHatchRecipes(IItemContainer[] multiHatches, IItemContainer[] meMultiHatches,
        int startTier, int interfaceCount) {
        ItemStack interfaceItem = AEApi.instance()
            .definitions()
            .blocks()
            .iface()
            .maybeStack(interfaceCount)
            .orNull();

        if (interfaceItem == null) {
            return;
        }

        for (int tier = startTier; tier <= 14; tier++) {
            if (tier >= multiHatches.length || tier >= meMultiHatches.length) {
                continue;
            }
            ItemStack inputHatch;
            ItemStack outputHatch;
            try {
                inputHatch = multiHatches[tier].get(1);
                outputHatch = meMultiHatches[tier].get(1);
            } catch (Exception e) {
                continue;
            }
            GTRecipeBuilder.builder()
                .itemInputs(
                    inputHatch,
                    interfaceItem.copy(),
                    new Object[] { OrePrefixes.circuit.get(circuitMaterials[tier]), 2 })
                .fluidInputs(new FluidStack(tierFluids[tier], 144))
                .itemOutputs(outputHatch)
                .duration(15 * GTRecipeBuilder.SECONDS)
                .eut(GTValues.VP[tier])
                .addTo(assemblerRecipes);
        }
    }

}
