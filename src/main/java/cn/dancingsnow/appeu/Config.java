package cn.dancingsnow.appeu;

import java.io.File;

import net.minecraftforge.common.config.Configuration;

import gregtech.api.GregTechAPI;

public class Config {

    public static int metaTileEntityIdStart = 27_000;

    public static void synchronizeConfiguration(File configFile) {
        Configuration configuration = new Configuration(configFile);

        metaTileEntityIdStart = configuration.getInt(
            "metaTileEntityIdStart",
            Configuration.CATEGORY_GENERAL,
            metaTileEntityIdStart,
            1,
            GregTechAPI.MAXIMUM_METATILE_IDS - 158,
            "Starting MetaTileEntity ID for the contiguous 158-ID default ME hatch matrix.");

        if (configuration.hasChanged()) {
            configuration.save();
        }
    }
}
