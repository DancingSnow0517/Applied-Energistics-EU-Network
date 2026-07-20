package cn.dancingsnow.appeu.item;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import cn.dancingsnow.appeu.registry.ModCreativeTab;

public final class ItemTitaniumCarbideMXeneSheet extends Item {

    private static final int COLOR = 0x303842;

    public ItemTitaniumCarbideMXeneSheet() {
        setUnlocalizedName("appeu.titanium_carbide_mxene_sheet");
        setTextureName("gregtech:materialicons/METALLIC/plate");
        setCreativeTab(ModCreativeTab.INSTANCE);
    }

    @Override
    public int getColorFromItemStack(ItemStack stack, int renderPass) {
        return COLOR;
    }
}
