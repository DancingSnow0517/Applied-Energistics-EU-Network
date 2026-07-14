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
        return ModItems.EU_STORAGE_CELL;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void displayAllReleventItems(List<ItemStack> itemStacks) {
        super.displayAllReleventItems(itemStacks);
        for (ItemStack hatch : HatchRegistration.getRegisteredHatches()
            .values()) {
            itemStacks.add(hatch.copy());
        }
    }
}
