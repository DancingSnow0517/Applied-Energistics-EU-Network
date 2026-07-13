package cn.dancingsnow.appeu.storage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Isolated;

import ic2.api.item.ElectricItem;
import ic2.api.item.IElectricItemManager;
import ic2.api.item.ISpecialElectricItem;
import ic2.core.item.GatewayElectricItemManager;
import it.unimi.dsi.fastutil.objects.ObjectLongPair;

@Isolated("Mutates IC2's process-wide ElectricItem.manager")
class EUStackTypeContainerTest {

    private static final String TEST_CHARGE = "TestCharge";

    private static IElectricItemManager previousManager;

    @BeforeAll
    static void installElectricItemManager() {
        previousManager = ElectricItem.manager;
        ElectricItem.manager = new GatewayElectricItemManager();
    }

    @AfterAll
    static void restoreElectricItemManager() {
        ElectricItem.manager = previousManager;
    }

    @Test
    void recognizesElectricItemsWithPositiveCapacityRegardlessOfStackLimit() {
        ItemStack valid = electricStack(10_000, 1, true, true, 0);
        ItemStack ordinary = new ItemStack(new Item());
        ItemStack stackable = electricStack(10_000, 64, true, true, 0);
        ItemStack zeroCapacity = electricStack(0, 1, true, true, 0);

        assertTrue(EUStackType.INSTANCE.isContainerItemForType(valid));
        assertFalse(EUStackType.INSTANCE.isContainerItemForType(null));
        assertFalse(EUStackType.INSTANCE.isContainerItemForType(ordinary));
        assertTrue(EUStackType.INSTANCE.isContainerItemForType(stackable));
        assertFalse(EUStackType.INSTANCE.isContainerItemForType(zeroCapacity));
    }

    @Test
    void readsCurrentChargeAndCapacityWithoutMutatingTheContainer() {
        ItemStack container = electricStack(10_000, 1, true, true, 4_000);

        EUStack stored = EUStackType.INSTANCE.getStackFromContainerItem(container);

        assertNotNull(stored);
        assertEquals(4_000, stored.getStackSize());
        assertEquals(10_000, EUStackType.INSTANCE.getContainerItemCapacity(container, stored));
        assertEquals(4_000, getCharge(container));
    }

    @Test
    void emptyElectricItemExposesAZeroEuStack() {
        ItemStack container = electricStack(10_000, 1, true, true, 0);

        EUStack stored = EUStackType.INSTANCE.getStackFromContainerItem(container);

        assertNotNull(stored);
        assertEquals(0, stored.getStackSize());
    }

    @Test
    void fillsAStackableEmptyElectricItem() {
        ItemStack empty = electricStack(10_000, 64, true, true, 0);

        ObjectLongPair<ItemStack> result = EUStackType.INSTANCE.fillContainer(empty, new EUStack(1_000));

        assertEquals(1_000, result.rightLong());
        assertEquals(1_000, getCharge(result.left()));
        assertEquals(0, getCharge(empty));
    }

    @Test
    void clampsOutOfRangeChargeWithoutMutatingTheContainers() {
        ItemStack negative = electricStack(10_000, 1, true, true, -500);
        ItemStack overCapacity = electricStack(10_000, 1, true, true, 15_000);

        EUStack negativeStored = EUStackType.INSTANCE.getStackFromContainerItem(negative);
        EUStack cappedStored = EUStackType.INSTANCE.getStackFromContainerItem(overCapacity);

        assertNull(negativeStored);
        assertNotNull(cappedStored);
        assertEquals(10_000, cappedStored.getStackSize());
        assertEquals(-500, getCharge(negative));
        assertEquals(15_000, getCharge(overCapacity));
    }

    @Test
    void fillsACopyByTheOfferedAmountAndPreservesUnrelatedNbt() {
        ItemStack original = electricStack(10_000, 1, true, true, 2_000);
        original.getTagCompound()
            .setString("Marker", "keep");

        ObjectLongPair<ItemStack> result = EUStackType.INSTANCE.fillContainer(original, new EUStack(3_000));

        assertEquals(3_000, result.rightLong());
        assertEquals(5_000, getCharge(result.left()));
        assertEquals(
            "keep",
            result.left()
                .getTagCompound()
                .getString("Marker"));
        assertEquals(2_000, getCharge(original));
    }

    @Test
    void fillStopsAtCapacityAndReportsZeroWhenChargingIsRejected() {
        ItemStack nearlyFull = electricStack(10_000, 1, true, true, 9_000);
        ItemStack rejecting = electricStack(10_000, 1, false, true, 2_000);

        ObjectLongPair<ItemStack> capacityLimited = EUStackType.INSTANCE.fillContainer(nearlyFull, new EUStack(5_000));
        ObjectLongPair<ItemStack> rejected = EUStackType.INSTANCE.fillContainer(rejecting, new EUStack(5_000));

        assertEquals(1_000, capacityLimited.rightLong());
        assertEquals(10_000, getCharge(capacityLimited.left()));
        assertEquals(0, rejected.rightLong());
        assertEquals(2_000, getCharge(rejected.left()));
    }

    @Test
    void fillHandlesLongMaxCapacityInAtMost1024ExactChunks() {
        ItemStack empty = electricStack(Long.MAX_VALUE, 1, true, true, 0);
        TestElectricItem item = (TestElectricItem) empty.getItem();
        double maxExactTransfer = 1L << 53;

        ObjectLongPair<ItemStack> result = EUStackType.INSTANCE.fillContainer(empty, new EUStack(Long.MAX_VALUE));

        assertEquals(Long.MAX_VALUE, result.rightLong());
        assertEquals(Long.MAX_VALUE, (long) getCharge(result.left()));
        assertEquals(1024, item.manager.chargeCalls);
        assertEquals(1024, item.manager.chargeRequests.size());
        assertTrue(
            item.manager.chargeRequests.stream()
                .allMatch(amount -> amount > 0 && amount <= maxExactTransfer));
        for (int i = 0; i < 1023; i++) {
            assertEquals(maxExactTransfer, item.manager.chargeRequests.get(i));
        }
        assertEquals(maxExactTransfer - 1, item.manager.chargeRequests.get(1023));
    }

    @Test
    void fillAcceptsAnObservedPartialTransferAndStops() {
        ItemStack partial = electricStack(10_000, 1, true, true, 2_000, ChargeBehavior.PARTIAL);

        ObjectLongPair<ItemStack> result = EUStackType.INSTANCE.fillContainer(partial, new EUStack(3_000));

        assertEquals(1_500, result.rightLong());
        assertEquals(3_500, getCharge(result.left()));
        assertEquals(1, ((TestElectricItem) partial.getItem()).manager.chargeCalls);
    }

    @Test
    void fillReportsObservedTransferWhenManagerReturnsZero() {
        ItemStack misleading = electricStack(10_000, 1, true, true, 2_000, ChargeBehavior.MUTATE_AND_RETURN_ZERO);

        ObjectLongPair<ItemStack> result = EUStackType.INSTANCE.fillContainer(misleading, new EUStack(3_000));

        assertEquals(3_000, result.rightLong());
        assertEquals(5_000, getCharge(result.left()));
    }

    @Test
    void fillReportsZeroWhenManagerReturnsPositiveWithoutMutating() {
        ItemStack unchanged = electricStack(10_000, 1, true, true, 2_000, ChargeBehavior.RETURN_WITHOUT_MUTATION);

        ObjectLongPair<ItemStack> result = EUStackType.INSTANCE.fillContainer(unchanged, new EUStack(3_000));

        assertEquals(0, result.rightLong());
        assertEquals(2_000, getCharge(result.left()));
    }

    @Test
    void fillRejectsMalformedNegativeInitialChargeWithoutInvokingManager() {
        ItemStack malformed = electricStack(10_000, 1, true, true, -5);
        TestElectricItem item = (TestElectricItem) malformed.getItem();

        ObjectLongPair<ItemStack> result = EUStackType.INSTANCE.fillContainer(malformed, new EUStack(10));

        assertEquals(0, result.rightLong());
        assertEquals(-5, getCharge(result.left()));
        assertEquals(-5, getCharge(malformed));
        assertEquals(0, item.manager.chargeCalls);
    }

    @Test
    void fillDiscardsInCapacityCandidateThatTransfersMoreThanRequested() {
        ItemStack overTransferring = electricStack(10_000, 1, true, true, 2_000, ChargeBehavior.OVER_TRANSFER);

        ObjectLongPair<ItemStack> result = EUStackType.INSTANCE.fillContainer(overTransferring, new EUStack(1_000));

        assertEquals(0, result.rightLong());
        assertEquals(2_000, getCharge(result.left()));
    }

    @Test
    void fillDiscardsCandidateWhoseRawChargeExceedsCapacity() {
        ItemStack overTransferring = electricStack(10_000, 1, true, true, 9_000, ChargeBehavior.OVER_TRANSFER);

        ObjectLongPair<ItemStack> result = EUStackType.INSTANCE.fillContainer(overTransferring, new EUStack(1_000));

        assertEquals(0, result.rightLong());
        assertEquals(9_000, getCharge(result.left()));
    }

    @Test
    void drainsACopyByTheRequestedAmountWithoutChangingTheOriginal() {
        ItemStack original = electricStack(10_000, 1, true, true, 7_000);

        ObjectLongPair<ItemStack> result = EUStackType.INSTANCE.drainStackFromContainer(original, new EUStack(3_000));

        assertEquals(3_000, result.rightLong());
        assertEquals(4_000, getCharge(result.left()));
        assertEquals(7_000, getCharge(original));
    }

    @Test
    void drainStopsAtStoredChargeAndReportsZeroWhenDischargingIsRejected() {
        ItemStack charged = electricStack(10_000, 1, true, true, 2_000);
        ItemStack rejecting = electricStack(10_000, 1, true, false, 2_000);

        ObjectLongPair<ItemStack> chargeLimited = EUStackType.INSTANCE
            .drainStackFromContainer(charged, new EUStack(5_000));
        ObjectLongPair<ItemStack> rejected = EUStackType.INSTANCE
            .drainStackFromContainer(rejecting, new EUStack(5_000));

        assertEquals(2_000, chargeLimited.rightLong());
        assertEquals(0, getCharge(chargeLimited.left()));
        assertEquals(0, rejected.rightLong());
        assertEquals(2_000, getCharge(rejected.left()));
    }

    @Test
    void clearReturnsAnEmptyCopyOnlyAfterACompleteDischarge() {
        ItemStack charged = electricStack(10_000, 1, true, true, 6_000);
        ItemStack rejecting = electricStack(10_000, 1, true, false, 6_000);

        ItemStack cleared = EUStackType.INSTANCE.clearFilledContainer(charged);
        ItemStack rejected = EUStackType.INSTANCE.clearFilledContainer(rejecting);

        assertNotNull(cleared);
        assertEquals(0, getCharge(cleared));
        assertEquals(6_000, getCharge(charged));
        assertNull(rejected);
        assertEquals(6_000, getCharge(rejecting));
    }

    @Test
    void drainHandlesLongMaxChargeInExactly1024BoundedChunks() {
        ItemStack full = electricStack(Long.MAX_VALUE, 1, true, true, Long.MAX_VALUE);
        TestElectricItem item = (TestElectricItem) full.getItem();
        double maxExactTransfer = 1L << 53;

        ObjectLongPair<ItemStack> result = EUStackType.INSTANCE
            .drainStackFromContainer(full, new EUStack(Long.MAX_VALUE));

        assertEquals(Long.MAX_VALUE, result.rightLong());
        assertEquals(0, (long) getCharge(result.left()));
        assertEquals(1024, item.manager.dischargeCalls);
        assertEquals(1024, item.manager.dischargeRequests.size());
        assertTrue(
            item.manager.dischargeRequests.stream()
                .allMatch(amount -> amount > 0 && amount <= maxExactTransfer));
        assertTrue(
            item.manager.dischargeRequests.stream()
                .allMatch(amount -> amount == maxExactTransfer));
    }

    @Test
    void clearFullyDischargesLongMaxChargeInExactly1024BoundedChunks() {
        ItemStack full = electricStack(Long.MAX_VALUE, 1, true, true, Long.MAX_VALUE);
        TestElectricItem item = (TestElectricItem) full.getItem();

        ItemStack cleared = EUStackType.INSTANCE.clearFilledContainer(full);

        assertNotNull(cleared);
        assertEquals(0, (long) getCharge(cleared));
        assertEquals(Long.MAX_VALUE, (long) getCharge(full));
        assertEquals(1024, item.manager.dischargeCalls);
    }

    @Test
    void drainAcceptsAnObservedPartialTransferAndStops() {
        ItemStack partial = electricStack(10_000, 1, true, true, 7_000, DischargeBehavior.PARTIAL);

        ObjectLongPair<ItemStack> result = EUStackType.INSTANCE.drainStackFromContainer(partial, new EUStack(3_000));

        assertEquals(1_500, result.rightLong());
        assertEquals(5_500, getCharge(result.left()));
        assertEquals(1, ((TestElectricItem) partial.getItem()).manager.dischargeCalls);
    }

    @Test
    void drainReportsObservedTransferWhenManagerReturnsZero() {
        ItemStack misleading = electricStack(10_000, 1, true, true, 7_000, DischargeBehavior.MUTATE_AND_RETURN_ZERO);

        ObjectLongPair<ItemStack> result = EUStackType.INSTANCE.drainStackFromContainer(misleading, new EUStack(3_000));

        assertEquals(3_000, result.rightLong());
        assertEquals(4_000, getCharge(result.left()));
    }

    @Test
    void drainReportsZeroWhenManagerReturnsPositiveWithoutMutating() {
        ItemStack unchanged = electricStack(10_000, 1, true, true, 7_000, DischargeBehavior.RETURN_WITHOUT_MUTATION);

        ObjectLongPair<ItemStack> result = EUStackType.INSTANCE.drainStackFromContainer(unchanged, new EUStack(3_000));

        assertEquals(0, result.rightLong());
        assertEquals(7_000, getCharge(result.left()));
    }

    @Test
    void drainDiscardsInCapacityCandidateThatTransfersMoreThanRequested() {
        ItemStack overTransferring = electricStack(10_000, 1, true, true, 7_000, DischargeBehavior.OVER_TRANSFER);

        ObjectLongPair<ItemStack> result = EUStackType.INSTANCE
            .drainStackFromContainer(overTransferring, new EUStack(1_000));

        assertEquals(0, result.rightLong());
        assertEquals(7_000, getCharge(result.left()));
    }

    @Test
    void drainDiscardsCandidateWhoseRawChargeBecomesNegative() {
        ItemStack overTransferring = electricStack(10_000, 1, true, true, 1_000, DischargeBehavior.OVER_TRANSFER);

        ObjectLongPair<ItemStack> result = EUStackType.INSTANCE
            .drainStackFromContainer(overTransferring, new EUStack(1_000));

        assertEquals(0, result.rightLong());
        assertEquals(1_000, getCharge(result.left()));
    }

    @Test
    void drainRejectsMalformedInitialChargeWithoutInvokingManager() {
        ItemStack negative = electricStack(10_000, 1, true, true, -5);
        ItemStack overCapacity = electricStack(10_000, 1, true, true, 10_005);
        TestElectricItem negativeItem = (TestElectricItem) negative.getItem();
        TestElectricItem overCapacityItem = (TestElectricItem) overCapacity.getItem();

        ObjectLongPair<ItemStack> negativeResult = EUStackType.INSTANCE
            .drainStackFromContainer(negative, new EUStack(10));
        ObjectLongPair<ItemStack> overCapacityResult = EUStackType.INSTANCE
            .drainStackFromContainer(overCapacity, new EUStack(10));

        assertEquals(0, negativeResult.rightLong());
        assertEquals(-5, getCharge(negativeResult.left()));
        assertEquals(0, negativeItem.manager.dischargeCalls);
        assertEquals(0, overCapacityResult.rightLong());
        assertEquals(10_005, getCharge(overCapacityResult.left()));
        assertEquals(0, overCapacityItem.manager.dischargeCalls);
    }

    @Test
    void drainUsesInternalDischargeForItemsThatCannotProvideEnergy() {
        ItemStack charged = electricStack(10_000, 1, true, true, 2_000);
        TestElectricItem item = (TestElectricItem) charged.getItem();

        ObjectLongPair<ItemStack> result = EUStackType.INSTANCE.drainStackFromContainer(charged, new EUStack(1_000));

        assertEquals(1_000, result.rightLong());
        assertFalse(item.canProvideEnergy(charged));
        assertEquals(1, item.manager.dischargeExternalFlags.size());
        assertFalse(item.manager.dischargeExternalFlags.get(0));
    }

    @Test
    void clearRejectsObservedPartialDischargeWithoutChangingOriginal() {
        ItemStack partial = electricStack(10_000, 1, true, true, 6_000, DischargeBehavior.PARTIAL);

        ItemStack cleared = EUStackType.INSTANCE.clearFilledContainer(partial);

        assertNull(cleared);
        assertEquals(6_000, getCharge(partial));
        assertEquals(1, ((TestElectricItem) partial.getItem()).manager.dischargeCalls);
    }

    @Test
    void clearReturnsDistinctCopyForEmptyContainer() {
        ItemStack empty = electricStack(10_000, 1, true, true, 0);

        ItemStack cleared = EUStackType.INSTANCE.clearFilledContainer(empty);

        assertNotNull(cleared);
        assertNotSame(empty, cleared);
        assertEquals(0, getCharge(cleared));
    }

    @Test
    void drainAndClearPreserveUnrelatedNbt() {
        ItemStack charged = electricStack(10_000, 1, true, true, 7_000);
        charged.getTagCompound()
            .setString("Marker", "drain");
        ItemStack filled = electricStack(10_000, 1, true, true, 6_000);
        filled.getTagCompound()
            .setString("Marker", "clear");

        ObjectLongPair<ItemStack> drained = EUStackType.INSTANCE.drainStackFromContainer(charged, new EUStack(3_000));
        ItemStack cleared = EUStackType.INSTANCE.clearFilledContainer(filled);

        assertEquals(
            "drain",
            drained.left()
                .getTagCompound()
                .getString("Marker"));
        assertNotNull(cleared);
        assertEquals(
            "clear",
            cleared.getTagCompound()
                .getString("Marker"));
    }

    private static ItemStack electricStack(long capacity, int maxStackSize, boolean acceptsCharge,
        boolean allowsDischarge, long charge) {
        return electricStack(
            capacity,
            maxStackSize,
            acceptsCharge,
            allowsDischarge,
            charge,
            ChargeBehavior.NORMAL,
            DischargeBehavior.NORMAL);
    }

    private static ItemStack electricStack(long capacity, int maxStackSize, boolean acceptsCharge,
        boolean allowsDischarge, long charge, ChargeBehavior chargeBehavior) {
        return electricStack(
            capacity,
            maxStackSize,
            acceptsCharge,
            allowsDischarge,
            charge,
            chargeBehavior,
            DischargeBehavior.NORMAL);
    }

    private static ItemStack electricStack(long capacity, int maxStackSize, boolean acceptsCharge,
        boolean allowsDischarge, long charge, DischargeBehavior dischargeBehavior) {
        return electricStack(
            capacity,
            maxStackSize,
            acceptsCharge,
            allowsDischarge,
            charge,
            ChargeBehavior.NORMAL,
            dischargeBehavior);
    }

    private static ItemStack electricStack(long capacity, int maxStackSize, boolean acceptsCharge,
        boolean allowsDischarge, long charge, ChargeBehavior chargeBehavior, DischargeBehavior dischargeBehavior) {
        ItemStack stack = new ItemStack(
            new TestElectricItem(
                capacity,
                maxStackSize,
                acceptsCharge,
                allowsDischarge,
                chargeBehavior,
                dischargeBehavior));
        setCharge(stack, charge);
        return stack;
    }

    private static double getCharge(ItemStack stack) {
        return stack.hasTagCompound() ? stack.getTagCompound()
            .getDouble(TEST_CHARGE) : 0;
    }

    private static void setCharge(ItemStack stack, double charge) {
        NBTTagCompound tag = stack.getTagCompound();
        if (tag == null) {
            tag = new NBTTagCompound();
            stack.setTagCompound(tag);
        }
        tag.setDouble(TEST_CHARGE, charge);
    }

    private enum ChargeBehavior {
        NORMAL,
        PARTIAL,
        MUTATE_AND_RETURN_ZERO,
        RETURN_WITHOUT_MUTATION,
        OVER_TRANSFER
    }

    private enum DischargeBehavior {
        NORMAL,
        PARTIAL,
        MUTATE_AND_RETURN_ZERO,
        RETURN_WITHOUT_MUTATION,
        OVER_TRANSFER
    }

    private static final class TestElectricItem extends Item implements ISpecialElectricItem {

        private final long capacity;
        private final boolean acceptsCharge;
        private final boolean allowsDischarge;
        private final ChargeBehavior chargeBehavior;
        private final DischargeBehavior dischargeBehavior;
        private final TestElectricItemManager manager = new TestElectricItemManager();

        private final class TestElectricItemManager implements IElectricItemManager {

            private int chargeCalls;
            private final List<Double> chargeRequests = new ArrayList<>();
            private int dischargeCalls;
            private final List<Double> dischargeRequests = new ArrayList<>();
            private final List<Boolean> dischargeExternalFlags = new ArrayList<>();

            @Override
            public double charge(ItemStack stack, double amount, int tier, boolean ignoreTransferLimit,
                boolean simulate) {
                chargeCalls++;
                chargeRequests.add(amount);
                if (!acceptsCharge || amount <= 0) {
                    return 0;
                }

                double accepted = Math.max(Math.min(amount, capacity - EUStackTypeContainerTest.getCharge(stack)), 0);
                double mutation = accepted;
                double reported = accepted;
                switch (chargeBehavior) {
                    case PARTIAL:
                        mutation = accepted / 2;
                        reported = mutation;
                        break;
                    case MUTATE_AND_RETURN_ZERO:
                        reported = 0;
                        break;
                    case RETURN_WITHOUT_MUTATION:
                        mutation = 0;
                        break;
                    case OVER_TRANSFER:
                        mutation = accepted + 1;
                        break;
                    case NORMAL:
                        break;
                }

                if (!simulate && mutation > 0) {
                    setCharge(stack, EUStackTypeContainerTest.getCharge(stack) + mutation);
                }
                return reported;
            }

            @Override
            public double discharge(ItemStack stack, double amount, int tier, boolean ignoreTransferLimit,
                boolean externally, boolean simulate) {
                dischargeCalls++;
                dischargeRequests.add(amount);
                dischargeExternalFlags.add(externally);
                if (!allowsDischarge || amount <= 0) {
                    return 0;
                }

                double discharged = Math.min(amount, EUStackTypeContainerTest.getCharge(stack));
                double mutation = discharged;
                double reported = discharged;
                switch (dischargeBehavior) {
                    case PARTIAL:
                        mutation = discharged / 2;
                        reported = mutation;
                        break;
                    case MUTATE_AND_RETURN_ZERO:
                        reported = 0;
                        break;
                    case RETURN_WITHOUT_MUTATION:
                        mutation = 0;
                        break;
                    case OVER_TRANSFER:
                        mutation = discharged + 1;
                        break;
                    case NORMAL:
                        break;
                }

                if (!simulate && mutation > 0) {
                    setCharge(stack, EUStackTypeContainerTest.getCharge(stack) - mutation);
                }
                return reported;
            }

            @Override
            public double getCharge(ItemStack stack) {
                return EUStackTypeContainerTest.getCharge(stack);
            }

            @Override
            public boolean canUse(ItemStack stack, double amount) {
                return EUStackTypeContainerTest.getCharge(stack) >= amount;
            }

            @Override
            public boolean use(ItemStack stack, double amount, EntityLivingBase entity) {
                if (!canUse(stack, amount)) {
                    return false;
                }
                setCharge(stack, EUStackTypeContainerTest.getCharge(stack) - amount);
                return true;
            }

            @Override
            public void chargeFromArmor(ItemStack stack, EntityLivingBase entity) {}

            @Override
            public String getToolTip(ItemStack stack) {
                return EUStackTypeContainerTest.getCharge(stack) + " / " + capacity;
            }
        }

        private TestElectricItem(long capacity, int maxStackSize, boolean acceptsCharge, boolean allowsDischarge,
            ChargeBehavior chargeBehavior, DischargeBehavior dischargeBehavior) {
            this.capacity = capacity;
            this.acceptsCharge = acceptsCharge;
            this.allowsDischarge = allowsDischarge;
            this.chargeBehavior = chargeBehavior;
            this.dischargeBehavior = dischargeBehavior;
            setMaxStackSize(maxStackSize);
        }

        @Override
        public IElectricItemManager getManager(ItemStack stack) {
            return manager;
        }

        @Override
        public boolean canProvideEnergy(ItemStack stack) {
            return false;
        }

        @Override
        public Item getChargedItem(ItemStack stack) {
            return this;
        }

        @Override
        public Item getEmptyItem(ItemStack stack) {
            return this;
        }

        @Override
        public double getMaxCharge(ItemStack stack) {
            return capacity;
        }

        @Override
        public int getTier(ItemStack stack) {
            return 1;
        }

        @Override
        public double getTransferLimit(ItemStack stack) {
            return capacity;
        }
    }
}
