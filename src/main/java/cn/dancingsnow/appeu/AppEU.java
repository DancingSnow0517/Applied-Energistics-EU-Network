package cn.dancingsnow.appeu;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;

@Mod(
    modid = AppEU.MODID,
    version = Tags.VERSION,
    name = AppEU.NAME,
    acceptedMinecraftVersions = "[1.7.10]",
    dependencies = "required-after:gregtech;required-after:appliedenergistics2")
public class AppEU {

    public static final String MODID = "appeu";
    public static final String NAME = "Applied Energistics: EU Network";
    public static final Logger LOG = LogManager.getLogger(MODID);

    @SidedProxy(clientSide = "cn.dancingsnow.appeu.ClientProxy", serverSide = "cn.dancingsnow.appeu.CommonProxy")
    public static CommonProxy proxy;

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        proxy.preInit(event);
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        proxy.init(event);
    }
}
