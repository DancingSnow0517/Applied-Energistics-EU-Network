package cn.dancingsnow.appeu.item;

import java.util.List;

import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;

import cn.dancingsnow.appeu.registry.ModCreativeTab;
import cn.dancingsnow.appeu.storage.EUCellTier;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class ItemEUStorageComponent extends Item {

    private static final String UNLOCALIZED_NAME = "item.appeu.eu_storage_component";

    private final IIcon[] icons = new IIcon[EUCellTier.values().length];

    public ItemEUStorageComponent() {
        this.setHasSubtypes(true);
        this.setMaxDamage(0);
        this.setUnlocalizedName("appeu.eu_storage_component");
        this.setCreativeTab(ModCreativeTab.INSTANCE);
    }

    @Override
    public String getUnlocalizedName(ItemStack stack) {
        EUCellTier tier = tierOrNull(stack);
        return tier == null ? UNLOCALIZED_NAME + ".invalid" : UNLOCALIZED_NAME + "_" + tier.suffix();
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void registerIcons(IIconRegister iconRegister) {
        for (EUCellTier tier : EUCellTier.values()) {
            this.icons[tier.meta()] = iconRegister.registerIcon("appeu:eu_storage_component_" + tier.suffix());
        }
        this.itemIcon = this.icons[EUCellTier.K1.meta()];
    }

    @Override
    @SideOnly(Side.CLIENT)
    public IIcon getIconFromDamage(int metadata) {
        return this.icons[iconTier(metadata).meta()];
    }

    @Override
    @SideOnly(Side.CLIENT)
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public void getSubItems(Item item, net.minecraft.creativetab.CreativeTabs creativeTab, List itemStacks) {
        for (EUCellTier tier : EUCellTier.values()) {
            itemStacks.add(new ItemStack(item, 1, tier.meta()));
        }
    }

    private static EUCellTier tierOrNull(ItemStack stack) {
        return stack == null ? null : tierOrNull(stack.getItemDamage());
    }

    private static EUCellTier tierOrNull(int metadata) {
        try {
            return EUCellTier.fromMeta(metadata);
        } catch (IllegalArgumentException ignored) {
            return null;
        }
    }

    private static EUCellTier iconTier(int metadata) {
        EUCellTier tier = tierOrNull(metadata);
        return tier == null ? EUCellTier.K1 : tier;
    }
}
