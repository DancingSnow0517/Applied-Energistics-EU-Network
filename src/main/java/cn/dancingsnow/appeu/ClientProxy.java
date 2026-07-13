package cn.dancingsnow.appeu;

import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.common.MinecraftForge;

import cn.dancingsnow.appeu.client.AppEUBlockIconContainer;
import cn.dancingsnow.appeu.client.AppEUOptionalBlockIconContainer;
import cn.dancingsnow.appeu.client.AppEUTextures;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;

public class ClientProxy extends CommonProxy {

    @Override
    public void preInit(FMLPreInitializationEvent event) {
        super.preInit(event);
        MinecraftForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent
    public void onTextureStitchPre(TextureStitchEvent.Pre event) {
        if (event.map.getTextureType() == 0) {
            AppEUTextures.BlockIcons.register();

            AppEUBlockIconContainer.registerIcons(event.map);
            AppEUOptionalBlockIconContainer.registerIcons(event.map);
        }
    }
}
