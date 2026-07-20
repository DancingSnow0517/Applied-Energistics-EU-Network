package cn.dancingsnow.appeu;

import java.io.File;

import net.minecraftforge.common.config.Configuration;

import cn.dancingsnow.appeu.hatch.HatchSpecs;
import gregtech.api.GregTechAPI;

public class Config {

    public static int metaTileEntityIdStart = 27_000;
    public static int materialIdStart = 22_000;

    public static void synchronizeConfiguration(File configFile) {
        Configuration configuration = new Configuration(configFile);

        metaTileEntityIdStart = configuration.getInt(
            "metaTileEntityIdStart",
            Configuration.CATEGORY_GENERAL,
            metaTileEntityIdStart,
            1,
            GregTechAPI.MAXIMUM_METATILE_IDS - HatchSpecs.DEFAULT_HATCH_COUNT,
            "Starting MetaTileEntity ID for the contiguous " + HatchSpecs.DEFAULT_HATCH_COUNT
                + "-ID default ME hatch matrix.");

        materialIdStart = configuration.getInt(
            "materialIdStart",
            Configuration.CATEGORY_GENERAL,
            materialIdStart,
            1,
            32000,
            "Starting Material ID for the contiguous 5-ID Ti3C2Tx production material range.");

        if (configuration.hasChanged()) {
            configuration.save();
        }
    }
}
