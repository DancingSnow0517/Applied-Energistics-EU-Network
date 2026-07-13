package cn.dancingsnow.appeu.client;

import gregtech.api.interfaces.IIconContainer;
import gregtech.api.interfaces.ITexture;
import gregtech.api.render.TextureFactory;

public class AppEUTextures {

    // spotless:off
    public static class BlockIcons {
        public static final IIconContainer OVERLAYS_ME_ENERGY_IN = createOptional("overlays_me_energy_in");
        public static final IIconContainer OVERLAYS_ME_ENERGY_OUT = createOptional("overlays_me_energy_out");
        public static final IIconContainer OVERLAYS_ME_ENERGY_IN_4A = createOptional("overlays_me_energy_in_4a");
        public static final IIconContainer OVERLAYS_ME_ENERGY_OUT_4A = createOptional("overlays_me_energy_out_4a");
        public static final IIconContainer OVERLAYS_ME_ENERGY_IN_16A = createOptional("overlays_me_energy_in_16a");
        public static final IIconContainer OVERLAYS_ME_ENERGY_OUT_16A = createOptional("overlays_me_energy_out_16a");
        public static final IIconContainer OVERLAYS_ME_ENERGY_IN_64A = createOptional("overlays_me_energy_in_64a");
        public static final IIconContainer OVERLAYS_ME_ENERGY_OUT_64A = createOptional("overlays_me_energy_out_64a");
        public static final IIconContainer OVERLAYS_ME_ENERGY_IN_LASER = createOptional("overlays_me_energy_in_laser");
        public static final IIconContainer OVERLAYS_ME_ENERGY_OUT_LASER = createOptional("overlays_me_energy_out_laser");

        public static final ITexture OVERLAYS_ME_ENERGY_IN_TEXTURE = TextureFactory.builder().addIcon(OVERLAYS_ME_ENERGY_IN).glow().build();
        public static final ITexture OVERLAYS_ME_ENERGY_OUT_TEXTURE = TextureFactory.builder().addIcon(OVERLAYS_ME_ENERGY_OUT).glow().build();
        public static final ITexture OVERLAYS_ME_ENERGY_IN_4A_TEXTURE = TextureFactory.builder().addIcon(OVERLAYS_ME_ENERGY_IN_4A).glow().build();
        public static final ITexture OVERLAYS_ME_ENERGY_OUT_4A_TEXTURE = TextureFactory.builder().addIcon(OVERLAYS_ME_ENERGY_OUT_4A).glow().build();
        public static final ITexture OVERLAYS_ME_ENERGY_IN_16A_TEXTURE = TextureFactory.builder().addIcon(OVERLAYS_ME_ENERGY_IN_16A).glow().build();
        public static final ITexture OVERLAYS_ME_ENERGY_OUT_16A_TEXTURE = TextureFactory.builder().addIcon(OVERLAYS_ME_ENERGY_OUT_16A).glow().build();
        public static final ITexture OVERLAYS_ME_ENERGY_IN_64A_TEXTURE = TextureFactory.builder().addIcon(OVERLAYS_ME_ENERGY_IN_64A).glow().build();
        public static final ITexture OVERLAYS_ME_ENERGY_OUT_64A_TEXTURE = TextureFactory.builder().addIcon(OVERLAYS_ME_ENERGY_OUT_64A).glow().build();
        public static final ITexture OVERLAYS_ME_ENERGY_IN_LASER_TEXTURE = TextureFactory.builder().addIcon(OVERLAYS_ME_ENERGY_IN_LASER).glow().build();
        public static final ITexture OVERLAYS_ME_ENERGY_OUT_LASER_TEXTURE = TextureFactory.builder().addIcon(OVERLAYS_ME_ENERGY_OUT_LASER).glow().build();

        private static IIconContainer create(String iconName) {
            return AppEUBlockIconContainer.create(iconName);
        }

        private static IIconContainer createOptional(String iconName) {
            return AppEUOptionalBlockIconContainer.create(iconName);
        }

        public static void register() {
        }
    }
    // spotless:on
}
