package cn.dancingsnow.appeu;

import java.util.Map;

import net.minecraft.item.ItemStack;

import cn.dancingsnow.appeu.registry.HatchRegistration;
import cn.dancingsnow.appeu.registry.ModItems;
import cn.dancingsnow.appeu.registry.ModMaterials;
import cn.dancingsnow.appeu.registry.RecipeRegistration;
import cn.dancingsnow.appeu.registry.StorageRegistration;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;

public class CommonProxy {

    public void preInit(FMLPreInitializationEvent event) {
        Config.synchronizeConfiguration(event.getSuggestedConfigurationFile());
        StorageRegistration.registerStackType();
        ModItems.register();
        ModMaterials.register();
        Map<String, ItemStack> hatches = HatchRegistration.registerAll(Config.metaTileEntityIdStart);
        AppEU.LOG.info(
            "Registered {} ME energy hatches starting at MetaTileEntity ID {}",
            hatches.size(),
            Config.metaTileEntityIdStart);
    }

    public void init(FMLInitializationEvent event) {
        StorageRegistration.registerCellHandler();
        AppEU.LOG.info("Initialized {} version {}", AppEU.NAME, Tags.VERSION);
    }

    public void postInit(FMLPostInitializationEvent event) {
        RecipeRegistration.register();
    }
}
