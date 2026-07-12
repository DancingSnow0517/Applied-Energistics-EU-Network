package cn.dancingsnow.appeu.storage;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.jetbrains.annotations.NotNull;

import appeng.api.config.FuzzyMode;
import appeng.api.storage.data.IAEStackType;
import appeng.api.storage.data.IItemList;
import appeng.util.item.MeaningfulFluidIterator;

public final class EUStackList implements IItemList<EUStack> {

    private final List<EUStack> records = new ArrayList<>(1);

    @Override
    public void add(EUStack option) {
        addStored(option);
    }

    @Override
    public EUStack findPrecise(EUStack request) {
        return request == null || records.isEmpty() ? null : records.get(0);
    }

    @Override
    public Collection<EUStack> findFuzzy(EUStack request, FuzzyMode fuzzy) {
        EUStack match = findPrecise(request);
        return match == null ? Collections.emptyList() : Collections.singletonList(match);
    }

    @Override
    public boolean isEmpty() {
        return !iterator().hasNext();
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
        for (EUStack record : this) {
            return record;
        }
        return null;
    }

    @Override
    public int size() {
        return records.size();
    }

    @Override
    public @NotNull Iterator<EUStack> iterator() {
        return new MeaningfulFluidIterator<>(records.iterator());
    }

    @Override
    public void resetStatus() {
        for (EUStack record : this) {
            record.reset();
        }
    }

    @Override
    public IAEStackType<EUStack> getStackType() {
        return EUStackType.INSTANCE;
    }

    private void addStored(EUStack option) {
        if (option == null) {
            return;
        }

        if (records.isEmpty()) {
            records.add(option.copy());
        } else {
            records.get(0)
                .add(option);
        }
    }
}
