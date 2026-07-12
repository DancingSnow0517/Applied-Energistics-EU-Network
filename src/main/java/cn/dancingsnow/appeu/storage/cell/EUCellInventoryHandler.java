package cn.dancingsnow.appeu.storage.cell;

import appeng.api.config.FuzzyMode;
import appeng.api.storage.IMEInventory;
import appeng.me.storage.CellInventoryHandler;
import appeng.tile.inventory.IAEStackInventory;
import cn.dancingsnow.appeu.storage.EUStack;
import cn.dancingsnow.appeu.storage.EUStackType;

public final class EUCellInventoryHandler extends CellInventoryHandler<EUStack> {

    public EUCellInventoryHandler(IMEInventory<EUStack> inventory) {
        super(inventory, EUStackType.INSTANCE);
    }

    @Override
    protected void setOreFilteredList(String filter) {}

    @Override
    protected void setPriorityList(boolean hasFuzzy, IAEStackInventory config, FuzzyMode fuzzyMode) {}
}
