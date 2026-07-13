package cn.dancingsnow.appeu.client;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.util.IIcon;
import net.minecraft.util.ResourceLocation;

import org.jetbrains.annotations.NotNull;

import cn.dancingsnow.appeu.AppEU;
import gregtech.api.interfaces.IIconContainer;
import gregtech.api.util.client.ResourceUtils;

public class AppEUBlockIconContainer implements IIconContainer {

    private static final Map<String, AppEUBlockIconContainer> INSTANCES = new ConcurrentHashMap<>();

    protected final String mIconName;
    protected final ResourceLocation iconResource;
    protected IIcon mIcon;

    AppEUBlockIconContainer(@NotNull String aIconName) {
        mIconName = AppEU.MODID + ":iconsets/" + aIconName;
        iconResource = ResourceUtils.getCompleteBlockTextureResourceLocation(mIconName);
    }

    @Override
    public IIcon getOverlayIcon() {
        return null;
    }

    @Override
    public ResourceLocation getTextureFile() {
        return TextureMap.locationBlocksTexture;
    }

    @Override
    public IIcon getIcon() {
        return mIcon;
    }

    public static AppEUBlockIconContainer create(String iconName) {
        return INSTANCES.computeIfAbsent(iconName, AppEUBlockIconContainer::new);
    }

    public static void registerIcons(IIconRegister iconRegister) {
        for (AppEUBlockIconContainer container : INSTANCES.values()) {
            container.mIcon = iconRegister.registerIcon(container.mIconName);
        }
    }
}
