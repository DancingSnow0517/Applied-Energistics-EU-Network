package cn.dancingsnow.appeu.storage.cell;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;

import appeng.api.exceptions.AppEngException;
import appeng.api.implementations.tiles.IChestOrDrive;
import appeng.api.storage.ICellHandler;
import appeng.api.storage.ICellInventory;
import appeng.api.storage.ICellInventoryHandler;
import appeng.api.storage.IMEInventory;
import appeng.api.storage.IMEInventoryHandler;
import appeng.api.storage.ISaveProvider;
import appeng.api.storage.StorageChannel;
import appeng.api.storage.data.IAEStackType;
import appeng.client.texture.ExtraBlockTextures;
import appeng.core.sync.GuiBridge;
import appeng.me.storage.CellInventoryHandler;
import appeng.util.Platform;
import cn.dancingsnow.appeu.AppEU;
import cn.dancingsnow.appeu.registry.ModItems;
import cn.dancingsnow.appeu.storage.EUStack;
import cn.dancingsnow.appeu.storage.EUStackType;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public final class EUCellHandler implements ICellHandler {

    public static final EUCellHandler INSTANCE = new EUCellHandler();

    private EUCellHandler() {}

    @Override
    public boolean isCell(ItemStack cellItem) {
        return cellItem != null && cellItem.getItem() == ModItems.EU_STORAGE_CELL
            && ModItems.EU_STORAGE_CELL.isStorageCell(cellItem);
    }

    @Override
    public IMEInventoryHandler<EUStack> getCellInventory(ItemStack cellItem, ISaveProvider container,
        IAEStackType<?> type) {
        if (type != EUStackType.INSTANCE || !isCell(cellItem)) {
            return null;
        }

        try {
            return new EUCellInventoryHandler(new EUCellInventory(cellItem, container));
        } catch (AppEngException exception) {
            AppEU.LOG.error("Failed to open EU storage cell inventory for {}", cellItem, exception);
        } catch (IllegalArgumentException | ArithmeticException exception) {
            AppEU.LOG.warn("Ignoring EU storage cell with malformed data: {}", cellItem, exception);
        }
        return null;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public IIcon getTopTexture_Light() {
        return ExtraBlockTextures.BlockMEChestItems_Light.getIcon();
    }

    @Override
    @SideOnly(Side.CLIENT)
    public IIcon getTopTexture_Medium() {
        return ExtraBlockTextures.BlockMEChestItems_Medium.getIcon();
    }

    @Override
    @SideOnly(Side.CLIENT)
    public IIcon getTopTexture_Dark() {
        return ExtraBlockTextures.BlockMEChestItems_Dark.getIcon();
    }

    @Override
    public void openChestGui(EntityPlayer player, IChestOrDrive chest, ICellHandler cellHandler,
        IMEInventoryHandler inventory, ItemStack cellItem, StorageChannel channel) {
        Platform.openGUI(player, (TileEntity) chest, chest.getUp(), GuiBridge.GUI_ME);
    }

    @Override
    public int getStatusForCell(ItemStack cellItem, IMEInventory inventory) {
        if (inventory instanceof CellInventoryHandler<?>cellInventoryHandler) {
            return cellInventoryHandler.getStatusForCell();
        }
        return 0;
    }

    @Override
    public double cellIdleDrain(ItemStack cellItem, IMEInventory inventory) {
        if (inventory instanceof ICellInventoryHandler<?>cellInventoryHandler) {
            ICellInventory<?> cellInventory = cellInventoryHandler.getCellInv();
            if (cellInventory != null) {
                return cellInventory.getIdleDrain();
            }
        }
        return 0;
    }
}
