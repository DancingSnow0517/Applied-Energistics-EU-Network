package cn.dancingsnow.appeu.storage.cell;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

import appeng.api.exceptions.AppEngException;
import appeng.api.storage.ISaveProvider;
import appeng.me.storage.CellInventory;
import cn.dancingsnow.appeu.storage.EUStack;

public final class EUCellInventory extends CellInventory<EUStack> {

    private static final String STACK_TYPE_TAG = "euTypes";
    private static final String STACK_COUNT_TAG = "euCount";

    public EUCellInventory(ItemStack cellItem, ISaveProvider container) throws AppEngException {
        super(cellItem, container);
    }

    @Override
    protected EUStack readStack(NBTTagCompound tag) {
        return EUStack.fromNBT(tag);
    }

    @Override
    protected String getStackTypeTag() {
        return STACK_TYPE_TAG;
    }

    @Override
    protected String getStackCountTag() {
        return STACK_COUNT_TAG;
    }
}
