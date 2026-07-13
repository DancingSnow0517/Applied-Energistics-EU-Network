package cn.dancingsnow.appeu.client;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import net.minecraft.client.renderer.texture.IIconRegister;

import org.jetbrains.annotations.NotNull;

import gregtech.api.enums.Textures;
import gregtech.api.util.client.ResourceUtils;

public class AppEUOptionalBlockIconContainer extends AppEUBlockIconContainer {

    private static final Map<String, AppEUOptionalBlockIconContainer> INSTANCES = new ConcurrentHashMap<>();

    AppEUOptionalBlockIconContainer(@NotNull String aIconName) {
        super(aIconName);
    }

    public static AppEUOptionalBlockIconContainer create(String iconName) {
        return INSTANCES.computeIfAbsent(iconName, AppEUOptionalBlockIconContainer::new);
    }

    public static void registerIcons(IIconRegister iconRegister) {
        for (AppEUOptionalBlockIconContainer container : INSTANCES.values()) {
            container.mIcon = ResourceUtils.resourceExists(container.iconResource)
                ? iconRegister.registerIcon(container.mIconName)
                : Textures.InvisibleIcon.INVISIBLE_ICON;
        }
    }
}
