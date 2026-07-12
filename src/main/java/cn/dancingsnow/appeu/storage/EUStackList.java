package cn.dancingsnow.appeu.storage;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;

import appeng.api.config.FuzzyMode;
import appeng.api.storage.data.IAEStackType;
import appeng.api.storage.data.IItemList;

public final class EUStackList implements IItemList<EUStack> {

    private EUStack stored;

    @Override
    public void add(EUStack option) {
        addStored(option);
    }

    @Override
    public EUStack findPrecise(EUStack request) {
        return request != null && stored != null ? stored.copy() : null;
    }

    @Override
    public Collection<EUStack> findFuzzy(EUStack request, FuzzyMode fuzzy) {
        EUStack match = findPrecise(request);
        return match == null ? Collections.emptyList() : Collections.singletonList(match);
    }

    @Override
    public boolean isEmpty() {
        return stored == null;
    }

    @Override
    public void addStorage(EUStack option) {
        addStored(option);
    }

    @Override
    public void addCrafting(EUStack option) {}

    @Override
    public void addRequestable(EUStack option) {
        addStored(option);
    }

    @Override
    public EUStack getFirstItem() {
        return stored == null ? null : stored.copy();
    }

    @Override
    public int size() {
        return stored == null ? 0 : 1;
    }

    @Override
    public Iterator<EUStack> iterator() {
        return stored == null ? Collections.emptyIterator()
            : Collections.singletonList(stored.copy())
                .iterator();
    }

    @Override
    public void resetStatus() {
        stored = null;
    }

    @Override
    public IAEStackType<EUStack> getStackType() {
        return EUStackType.INSTANCE;
    }

    private void addStored(EUStack option) {
        if (option == null || !option.isMeaningful()) {
            return;
        }

        if (stored == null) {
            stored = option.copy();
        } else {
            stored.add(option);
        }
    }
}
