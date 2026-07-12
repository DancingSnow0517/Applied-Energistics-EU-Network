package cn.dancingsnow.appeu.storage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Collection;
import java.util.Iterator;

import org.junit.jupiter.api.Test;

import appeng.api.config.FuzzyMode;

class EUStackListTest {

    @Test
    void addStorageAggregatesIntoOneRecord() {
        EUStackList list = new EUStackList();

        list.addStorage(new EUStack(10));
        list.addStorage(new EUStack(7));

        assertEquals(1, list.size());
        assertEquals(
            17,
            list.findPrecise(new EUStack(1))
                .getStackSize());
        assertEquals(
            17,
            list.getFirstItem()
                .getStackSize());
    }

    @Test
    void resetStatusClearsTheRecord() {
        EUStackList list = new EUStackList();
        list.addStorage(new EUStack(10));

        list.resetStatus();

        assertTrue(list.isEmpty());
        assertEquals(0, list.size());
        assertNull(list.getFirstItem());
    }

    @Test
    void addAndAddRequestableAggregateStoredEu() {
        EUStackList list = new EUStackList();

        list.add(new EUStack(4));
        list.addRequestable(new EUStack(6));
        list.addCrafting(new EUStack(100));

        assertEquals(
            10,
            list.findPrecise(new EUStack(1))
                .getStackSize());
    }

    @Test
    void ignoresNullAndNonMeaningfulStacks() {
        EUStackList list = new EUStackList();

        list.add(null);
        list.addStorage(new EUStack(0));
        list.addRequestable(new EUStack(0));
        list.addCrafting(new EUStack(10));

        assertTrue(list.isEmpty());
        assertNull(list.findPrecise(null));
        assertTrue(
            list.findFuzzy(null, FuzzyMode.IGNORE_ALL)
                .isEmpty());
    }

    @Test
    void anyNonNullEuRequestFindsTheStoredIdentity() {
        EUStackList list = new EUStackList();
        list.addStorage(new EUStack(17));

        assertEquals(
            17,
            list.findPrecise(new EUStack(0))
                .getStackSize());
        assertEquals(
            17,
            list.findFuzzy(new EUStack(0), FuzzyMode.IGNORE_ALL)
                .iterator()
                .next()
                .getStackSize());
    }

    @Test
    void preciseLookupExposesMutableBackingRecordForCellInjection() {
        EUStackList list = new EUStackList();
        list.addStorage(new EUStack(10));

        list.findPrecise(new EUStack(1))
            .incStackSize(7);

        assertEquals(
            17,
            list.findPrecise(new EUStack(1))
                .getStackSize());
    }

    @Test
    void preciseLookupExposesMutableBackingRecordForCellExtractionAndZeroCleanup() {
        EUStackList list = new EUStackList();
        list.addStorage(new EUStack(10));

        EUStack record = list.findPrecise(new EUStack(1));
        record.decStackSize(4);
        assertEquals(
            6,
            list.findPrecise(new EUStack(1))
                .getStackSize());

        record.decStackSize(6);
        assertEquals(
            0,
            list.findPrecise(new EUStack(1))
                .getStackSize());
        assertFalse(
            list.iterator()
                .hasNext());
        assertEquals(0, list.size());
        assertNull(list.findPrecise(new EUStack(1)));
    }

    @Test
    void fuzzyFirstAndIterationExposeTheMutableBackingRecord() {
        EUStackList list = new EUStackList();
        list.addStorage(new EUStack(17));

        EUStack precise = list.findPrecise(new EUStack(1));
        Collection<EUStack> fuzzy = list.findFuzzy(new EUStack(1), FuzzyMode.IGNORE_ALL);
        EUStack first = list.getFirstItem();
        Iterator<EUStack> iterator = list.iterator();
        assertTrue(iterator.hasNext());
        EUStack iterated = iterator.next();

        assertEquals(1, fuzzy.size());
        EUStack fuzzyStack = fuzzy.iterator()
            .next();
        assertSame(precise, fuzzyStack);
        assertSame(precise, first);
        assertSame(precise, iterated);
        assertFalse(iterator.hasNext());

        iterated.incStackSize(3);
        assertEquals(
            20,
            list.findPrecise(new EUStack(1))
                .getStackSize());
    }

    @Test
    void iteratorRemoveDeletesTheBackingRecord() {
        EUStackList list = new EUStackList();
        list.addStorage(new EUStack(17));

        Iterator<EUStack> iterator = list.iterator();
        assertTrue(iterator.hasNext());
        iterator.next();
        iterator.remove();

        assertFalse(iterator.hasNext());
        assertTrue(list.isEmpty());
        assertEquals(0, list.size());
        assertNull(list.findPrecise(new EUStack(1)));
    }

    @Test
    void reportsEuStackType() {
        assertSame(EUStackType.INSTANCE, new EUStackList().getStackType());
    }
}
