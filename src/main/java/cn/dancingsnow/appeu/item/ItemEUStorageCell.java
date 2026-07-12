package cn.dancingsnow.appeu.item;

import java.util.List;

import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.event.ForgeEventFactory;

import org.jetbrains.annotations.NotNull;

import com.google.common.base.Optional;

import appeng.api.AEApi;
import appeng.api.storage.IMEInventoryHandler;
import appeng.api.storage.data.IAEStackType;
import appeng.api.storage.data.IItemList;
import appeng.items.AEBaseCell;
import appeng.util.InventoryAdaptor;
import appeng.util.IterationCounter;
import cn.dancingsnow.appeu.registry.ModItems;
import cn.dancingsnow.appeu.storage.EUCellTier;
import cn.dancingsnow.appeu.storage.EUConstants;
import cn.dancingsnow.appeu.storage.EUStackType;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class ItemEUStorageCell extends AEBaseCell {

    private static final String UNLOCALIZED_NAME = "item.appeu.eu_storage_cell";

    private final IIcon[] icons = new IIcon[EUCellTier.values().length];

    public ItemEUStorageCell() {
        super(Optional.of("eu"));
        this.setHasSubtypes(true);
        this.setMaxDamage(0);
        this.setMaxStackSize(1);
        this.setNoRepair();
        this.setUnlocalizedName("appeu.eu_storage_cell");
        this.setCreativeTab(CreativeTabs.tabMisc);
    }

    @Override
    public String getUnlocalizedName(ItemStack stack) {
        return UNLOCALIZED_NAME + "_" + tierOrDefault(stack).suffix();
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void registerIcons(IIconRegister iconRegister) {
        for (EUCellTier tier : EUCellTier.values()) {
            this.icons[tier.meta()] = iconRegister.registerIcon("appeu:eu_storage_cell_" + tier.suffix());
        }
        this.itemIcon = this.icons[EUCellTier.K1.meta()];
    }

    @Override
    @SideOnly(Side.CLIENT)
    public IIcon getIconFromDamage(int metadata) {
        return this.icons[tierOrDefault(metadata).meta()];
    }

    @Override
    protected void getCheckedSubItems(Item item, CreativeTabs creativeTab, List<ItemStack> itemStacks) {
        for (EUCellTier tier : EUCellTier.values()) {
            itemStacks.add(new ItemStack(item, 1, tier.meta()));
        }
    }

    @Override
    public long getBytesLong(ItemStack cellItem) {
        return tierOrDefault(cellItem).totalBytes();
    }

    @Override
    public int getBytes(ItemStack cellItem) {
        return Math.toIntExact(this.getBytesLong(cellItem));
    }

    @Override
    public int BytePerType(ItemStack cellItem) {
        return EUConstants.BYTES_PER_TYPE;
    }

    @Override
    public int getBytesPerType(ItemStack cellItem) {
        return EUConstants.BYTES_PER_TYPE;
    }

    @Override
    public int getTotalTypes(ItemStack cellItem) {
        return EUConstants.TOTAL_TYPES;
    }

    @Override
    public double getIdleDrain(ItemStack cellItem) {
        return tierOrDefault(cellItem).idleDrain();
    }

    @Override
    public double getIdleDrain() {
        return EUCellTier.K1.idleDrain();
    }

    @Override
    @NotNull
    public IAEStackType<?> getStackType() {
        return EUStackType.INSTANCE;
    }

    @Override
    public boolean storableInStorageCell() {
        return false;
    }

    @Override
    public boolean isStorageCell(ItemStack stack) {
        return stack != null && stack.getItem() == this && tierOrNull(stack.getItemDamage()) != null;
    }

    @Override
    public CellData getCellData(ItemStack stack) {
        EUCellTier tier = tierOrDefault(stack);
        return new CellData(
            tier.totalBytes(),
            EUConstants.TOTAL_TYPES,
            EUConstants.BYTES_PER_TYPE,
            Math.toIntExact(EUConstants.EU_PER_BYTE));
    }

    @Override
    public ItemStack getComponent() {
        return new ItemStack(ModItems.EU_STORAGE_COMPONENT, 1, EUCellTier.K1.meta());
    }

    @Override
    public ItemStack onItemRightClick(ItemStack stack, World world, EntityPlayer player) {
        this.disassembleDrive(stack, world, player);
        return stack;
    }

    @Override
    public boolean onItemUseFirst(ItemStack stack, EntityPlayer player, World world, int x, int y, int z, int side,
        float hitX, float hitY, float hitZ) {
        if (ForgeEventFactory.onItemUseStart(player, stack, 1) <= 0) {
            return true;
        }
        return this.disassembleDrive(stack, world, player);
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    private boolean disassembleDrive(ItemStack stack, World world, EntityPlayer player) {
        if (!player.isSneaking() || world.isRemote) {
            return false;
        }

        EUCellTier tier = stack == null ? null : tierOrNull(stack.getItemDamage());
        InventoryPlayer playerInventory = player.inventory;
        if (tier == null || playerInventory.getCurrentItem() != stack) {
            return false;
        }

        IMEInventoryHandler inventory = AEApi.instance()
            .registries()
            .cell()
            .getCellInventory(stack, null, this.getStackType());
        if (inventory == null) {
            return false;
        }

        IItemList<?> contents = inventory.getAvailableItems(
            this.getStackType()
                .createList(),
            IterationCounter.fetchNewId());
        InventoryAdaptor inventoryAdaptor = InventoryAdaptor.getAdaptor(player, ForgeDirection.UNKNOWN);
        if (!contents.isEmpty() || inventoryAdaptor == null) {
            return false;
        }

        ItemStack component = new ItemStack(ModItems.EU_STORAGE_COMPONENT, 1, tier.meta());
        ItemStack housing = this.getHousing();
        if (housing == null) {
            return false;
        }

        IInventory upgradesInventory = this.getUpgradesInventory(stack);
        ItemStack[] upgrades = new ItemStack[upgradesInventory.getSizeInventory()];
        for (int slot = 0; slot < upgrades.length; slot++) {
            ItemStack upgrade = upgradesInventory.getStackInSlot(slot);
            upgrades[slot] = upgrade == null ? null : upgrade.copy();
        }

        playerInventory.setInventorySlotContents(playerInventory.currentItem, null);
        this.returnOrDrop(inventoryAdaptor, component, player);
        for (ItemStack upgrade : upgrades) {
            this.returnOrDrop(inventoryAdaptor, upgrade, player);
        }
        this.returnOrDrop(inventoryAdaptor, housing, player);

        if (player.inventoryContainer != null) {
            player.inventoryContainer.detectAndSendChanges();
        }
        return true;
    }

    private void returnOrDrop(InventoryAdaptor inventoryAdaptor, ItemStack stack, EntityPlayer player) {
        if (stack == null) {
            return;
        }
        ItemStack remainder = inventoryAdaptor.addItems(stack);
        if (remainder != null) {
            player.dropPlayerItemWithRandomChoice(remainder, false);
        }
    }

    private static EUCellTier tierOrDefault(ItemStack stack) {
        EUCellTier tier = stack == null ? null : tierOrNull(stack.getItemDamage());
        return tier == null ? EUCellTier.K1 : tier;
    }

    private static EUCellTier tierOrDefault(int metadata) {
        EUCellTier tier = tierOrNull(metadata);
        return tier == null ? EUCellTier.K1 : tier;
    }

    private static EUCellTier tierOrNull(int metadata) {
        try {
            return EUCellTier.fromMeta(metadata);
        } catch (IllegalArgumentException ignored) {
            return null;
        }
    }
}
