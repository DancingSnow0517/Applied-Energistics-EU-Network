package cn.dancingsnow.appeu.registry;

import java.util.List;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import cn.dancingsnow.appeu.AppEU;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public final class ModCreativeTab extends CreativeTabs {

    public static final ModCreativeTab INSTANCE = new ModCreativeTab();

    private ModCreativeTab() {
        super(AppEU.MODID);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public Item getTabIconItem() {
        return ModItems.EU_ENERGY_DISPLAY;
    }

    @Override
    @SideOnly(Side.CLIENT)
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public void displayAllReleventItems(List itemStacks) {
        super.displayAllReleventItems(itemStacks);
        for (ItemStack hatch : HatchRegistration.getRegisteredHatches()
            .values()) {
            itemStacks.add(hatch.copy());
        }
    }
}
