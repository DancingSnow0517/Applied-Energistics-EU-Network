package cn.dancingsnow.appeu.storage;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.NoSuchElementException;

import org.jetbrains.annotations.NotNull;

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
        EUStack record = cleanStoredRecord();
        return request == null ? null : record;
    }

    @Override
    public Collection<EUStack> findFuzzy(EUStack request, FuzzyMode fuzzy) {
        EUStack match = findPrecise(request);
        return match == null ? Collections.emptyList() : Collections.singletonList(match);
    }

    @Override
    public boolean isEmpty() {
        return cleanStoredRecord() == null;
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
        return cleanStoredRecord();
    }

    @Override
    public int size() {
        return cleanStoredRecord() == null ? 0 : 1;
    }

    @Override
    public @NotNull Iterator<EUStack> iterator() {
        return new RecordIterator(cleanStoredRecord());
    }

    @Override
    public void resetStatus() {
        EUStack record = cleanStoredRecord();
        if (record != null) {
            record.reset();
        }
    }

    @Override
    public IAEStackType<EUStack> getStackType() {
        return EUStackType.INSTANCE;
    }

    private void addStored(EUStack option) {
        if (option == null || !option.isMeaningful()) {
            return;
        }

        EUStack record = cleanStoredRecord();
        if (record == null) {
            stored = option.copy();
        } else {
            long storedAmount = record.getStackSize();
            long addedAmount = option.getStackSize();
            try {
                record.add(option);
            } catch (ArithmeticException exception) {
                throw new IllegalArgumentException(
                    "EU amount overflow while merging " + storedAmount + " with " + addedAmount,
                    exception);
            }
        }
    }

    private EUStack cleanStoredRecord() {
        if (stored != null && !stored.isMeaningful()) {
            stored = null;
        }
        return stored;
    }

    private final class RecordIterator implements Iterator<EUStack> {

        private final EUStack record;
        private boolean returned;
        private boolean canRemove;

        private RecordIterator(EUStack record) {
            this.record = record;
        }

        @Override
        public boolean hasNext() {
            if (returned || record == null || stored != record) {
                return false;
            }
            if (!record.isMeaningful()) {
                stored = null;
                return false;
            }
            return true;
        }

        @Override
        public EUStack next() {
            if (!hasNext()) {
                throw new NoSuchElementException();
            }
            returned = true;
            canRemove = true;
            return record;
        }

        @Override
        public void remove() {
            if (!canRemove) {
                throw new IllegalStateException();
            }
            if (stored == record) {
                stored = null;
            }
            canRemove = false;
        }
    }
}
