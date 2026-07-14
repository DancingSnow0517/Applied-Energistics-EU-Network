package cn.dancingsnow.appeu.item;

import java.util.List;

import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;

import cn.dancingsnow.appeu.registry.ModCreativeTab;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class ItemEUStorageCellHousing extends Item {

    private static final String UNLOCALIZED_NAME = "item.appeu.eu_storage_cell_housing";
    private static final String ADVANCED_UNLOCALIZED_NAME = "item.appeu.advanced_eu_storage_cell_housing";

    private final IIcon[] icons = new IIcon[2];

    public ItemEUStorageCellHousing() {
        setHasSubtypes(true);
        setMaxDamage(0);
        setUnlocalizedName("appeu.eu_storage_cell_housing");
        setCreativeTab(ModCreativeTab.INSTANCE);
    }

    @Override
    public String getUnlocalizedName(ItemStack stack) {
        return stack.getItemDamage() == 0 ? UNLOCALIZED_NAME : ADVANCED_UNLOCALIZED_NAME;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void registerIcons(IIconRegister register) {
        icons[0] = register.registerIcon("appeu:eu_storage_cell_housing");
        icons[1] = register.registerIcon("appeu:advanced_eu_storage_cell_housing");
    }

    @Override
    @SideOnly(Side.CLIENT)
    public IIcon getIconFromDamage(int damage) {
        return icons[damage];
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void getSubItems(Item item, CreativeTabs creativeTabs, List<ItemStack> itemStacks) {
        itemStacks.add(new ItemStack(item, 1, 0));
        itemStacks.add(new ItemStack(item, 1, 1));
    }
}
