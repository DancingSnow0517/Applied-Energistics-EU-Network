# EU Electric Item Containers Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Implement bidirectional AE terminal transfers between stored EU and compatible GT/IC2 electric items.

**Architecture:** `EUStackType` will implement AE2's complete container contract. It will use `GTModHandler` for recognition and exact `long` charge metadata, then mutate copied items through `ElectricItem.manager` in exactly representable chunks and report observed charge deltas.

**Tech Stack:** Java 17, Minecraft Forge 1.7.10, GT5-Unofficial, IC2 electric item API, AE2 `IAEStackType`, JUnit 5, Gradle

---

## File Structure

- Modify `src/main/java/cn/dancingsnow/appeu/storage/EUStackType.java`: own electric-container recognition, charge-state reads, bounded manager transfers, and the AE2 container contract.
- Create `src/test/java/cn/dancingsnow/appeu/storage/EUStackTypeContainerTest.java`: exercise the production methods through a controllable `ISpecialElectricItem` and `IElectricItemManager` without initializing game registries.

No separate production adapter is needed. The behavior belongs to the existing stack type, and the IC2 special-item interface provides a real manager boundary for focused tests.

### Task 1: Recognize Electric Containers And Read Charge

**Files:**
- Create: `src/test/java/cn/dancingsnow/appeu/storage/EUStackTypeContainerTest.java`
- Modify: `src/main/java/cn/dancingsnow/appeu/storage/EUStackType.java:1-80`

- [ ] **Step 1: Create the controllable electric item test fixture**

Create `EUStackTypeContainerTest` with these imports and nested fixture. The manager stores charge in test NBT, so calls still pass through `GTModHandler` and IC2's global manager gateway.

```java
package cn.dancingsnow.appeu.storage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

import org.junit.jupiter.api.Test;

import ic2.api.item.IElectricItemManager;
import ic2.api.item.ISpecialElectricItem;

class EUStackTypeContainerTest {

    private static final String CHARGE_TAG = "TestCharge";
    private static final String MARKER_TAG = "Marker";

    private static ItemStack electricStack(TestElectricItem item, long charge) {
        ItemStack stack = new ItemStack(item);
        setCharge(stack, charge);
        return stack;
    }

    private static long getCharge(ItemStack stack) {
        return stack.hasTagCompound() ? stack.getTagCompound().getLong(CHARGE_TAG) : 0;
    }

    private static void setCharge(ItemStack stack, long charge) {
        NBTTagCompound tag = stack.hasTagCompound() ? stack.getTagCompound() : new NBTTagCompound();
        tag.setLong(CHARGE_TAG, charge);
        stack.setTagCompound(tag);
    }

    private static final class TestElectricItem extends Item implements ISpecialElectricItem {

        private final long capacity;
        private final boolean acceptsCharge;
        private final boolean allowsDischarge;
        private final TestElectricItemManager manager = new TestElectricItemManager();

        private TestElectricItem(long capacity, int maxStackSize, boolean acceptsCharge, boolean allowsDischarge) {
            this.capacity = capacity;
            this.acceptsCharge = acceptsCharge;
            this.allowsDischarge = allowsDischarge;
            setMaxStackSize(maxStackSize);
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

        @Override
        public IElectricItemManager getManager(ItemStack stack) {
            return manager;
        }

        private final class TestElectricItemManager implements IElectricItemManager {

            private int chargeCalls;
            private int dischargeCalls;

            @Override
            public double charge(ItemStack stack, double amount, int tier, boolean ignoreTransferLimit,
                boolean simulate) {
                chargeCalls++;
                if (!acceptsCharge || amount <= 0) return 0;
                long current = EUStackTypeContainerTest.getCharge(stack);
                long moved = Math.min((long) amount, capacity - current);
                if (!simulate) setCharge(stack, current + moved);
                return moved;
            }

            @Override
            public double discharge(ItemStack stack, double amount, int tier, boolean ignoreTransferLimit,
                boolean externally, boolean simulate) {
                dischargeCalls++;
                if (!allowsDischarge || amount <= 0) return 0;
                long current = EUStackTypeContainerTest.getCharge(stack);
                long moved = Math.min((long) amount, current);
                if (!simulate) setCharge(stack, current - moved);
                return moved;
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
            public boolean use(ItemStack stack, double amount, EntityLivingBase player) {
                return discharge(stack, amount, Integer.MAX_VALUE, true, false, false) == amount;
            }

            @Override
            public void chargeFromArmor(ItemStack stack, EntityLivingBase player) {}

            @Override
            public String getToolTip(ItemStack stack) {
                return Long.toString(EUStackTypeContainerTest.getCharge(stack));
            }
        }
    }
}
```

- [ ] **Step 2: Add failing recognition and charge-state tests**

Add these methods above the nested fixture:

```java
@Test
void recognizesOnlyNonStackableElectricItemsWithPositiveCapacity() {
    TestElectricItem validItem = new TestElectricItem(10_000, 1, true, true);
    TestElectricItem stackableItem = new TestElectricItem(10_000, 64, true, true);
    TestElectricItem zeroCapacityItem = new TestElectricItem(0, 1, true, true);

    assertTrue(EUStackType.INSTANCE.isContainerItemForType(electricStack(validItem, 0)));
    assertFalse(EUStackType.INSTANCE.isContainerItemForType(null));
    assertFalse(EUStackType.INSTANCE.isContainerItemForType(new ItemStack(new Item())));
    assertFalse(EUStackType.INSTANCE.isContainerItemForType(electricStack(stackableItem, 0)));
    assertFalse(EUStackType.INSTANCE.isContainerItemForType(electricStack(zeroCapacityItem, 0)));
}

@Test
void readsCurrentChargeAndCapacityWithoutMutatingTheContainer() {
    ItemStack container = electricStack(new TestElectricItem(10_000, 1, true, true), 4_000);

    EUStack stored = EUStackType.INSTANCE.getStackFromContainerItem(container);

    assertNotNull(stored);
    assertEquals(4_000, stored.getStackSize());
    assertEquals(10_000, EUStackType.INSTANCE.getContainerItemCapacity(container, new EUStack(1)));
    assertEquals(4_000, getCharge(container));
}

@Test
void emptyElectricItemHasNoStoredStack() {
    ItemStack container = electricStack(new TestElectricItem(10_000, 1, true, true), 0);

    assertNull(EUStackType.INSTANCE.getStackFromContainerItem(container));
}
```

- [ ] **Step 3: Run the targeted test and verify RED**

Run:

```powershell
.\gradlew.bat test --tests cn.dancingsnow.appeu.storage.EUStackTypeContainerTest
```

Expected: FAIL because `isContainerItemForType` still returns false, `getStackFromContainerItem` returns null for the charged item, and capacity returns zero.

- [ ] **Step 4: Implement recognition and charge-state reads**

Add imports:

```java
import java.util.Optional;

import gregtech.api.util.GTModHandler;
```

Replace `isContainerItemForType`, `getStackFromContainerItem`, and `getContainerItemCapacity`, then add the private state helper near the end of `EUStackType`:

```java
@Override
public boolean isContainerItemForType(@Nullable ItemStack container) {
    return getChargeState(container) != null;
}

@Override
public @Nullable EUStack getStackFromContainerItem(@NotNull ItemStack container) {
    ChargeState state = getChargeState(container);
    return state == null || state.charge == 0 ? null : new EUStack(state.charge);
}

@Override
public long getContainerItemCapacity(@NotNull ItemStack container, @NotNull EUStack stack) {
    ChargeState state = getChargeState(container);
    return state == null ? 0 : state.capacity;
}

private static @Nullable ChargeState getChargeState(@Nullable ItemStack container) {
    if (container == null || container.getMaxStackSize() != 1 || !GTModHandler.isElectricItem(container)) return null;

    Optional<Long[]> charge = GTModHandler.getElectricItemCharge(container);
    if (!charge.isPresent()) return null;

    Long[] values = charge.get();
    if (values.length < 2 || values[0] == null || values[1] == null || values[1] <= 0) return null;

    long capacity = values[1];
    long current = Math.min(Math.max(values[0], 0), capacity);
    return new ChargeState(current, capacity);
}

private static final class ChargeState {

    private final long charge;
    private final long capacity;

    private ChargeState(long charge, long capacity) {
        this.charge = charge;
        this.capacity = capacity;
    }
}
```

- [ ] **Step 5: Run the targeted test and verify GREEN**

Run:

```powershell
.\gradlew.bat test --tests cn.dancingsnow.appeu.storage.EUStackTypeContainerTest
```

Expected: PASS with all three recognition/read tests green.

- [ ] **Step 6: Commit the first behavior slice**

```powershell
git add src/main/java/cn/dancingsnow/appeu/storage/EUStackType.java src/test/java/cn/dancingsnow/appeu/storage/EUStackTypeContainerTest.java
git commit -m "feat: recognize EU electric item containers"
```

### Task 2: Charge Electric Items From The AE Network

**Files:**
- Modify: `src/test/java/cn/dancingsnow/appeu/storage/EUStackTypeContainerTest.java`
- Modify: `src/main/java/cn/dancingsnow/appeu/storage/EUStackType.java:73-105`

- [ ] **Step 1: Add failing charge, immutability, rejection, and long-capacity tests**

Add the `ObjectLongPair` import and these methods:

```java
import it.unimi.dsi.fastutil.objects.ObjectLongPair;

@Test
void fillsACopyByTheOfferedAmountAndPreservesUnrelatedNbt() {
    TestElectricItem item = new TestElectricItem(10_000, 1, true, true);
    ItemStack original = electricStack(item, 2_000);
    original.getTagCompound().setString(MARKER_TAG, "keep");

    ObjectLongPair<ItemStack> result = EUStackType.INSTANCE.fillContainer(original, new EUStack(3_000));

    assertEquals(3_000, result.rightLong());
    assertEquals(5_000, getCharge(result.left()));
    assertEquals("keep", result.left().getTagCompound().getString(MARKER_TAG));
    assertEquals(2_000, getCharge(original));
}

@Test
void fillStopsAtCapacityAndReportsZeroWhenChargingIsRejected() {
    ItemStack almostFull = electricStack(new TestElectricItem(10_000, 1, true, true), 9_000);
    ItemStack rejected = electricStack(new TestElectricItem(10_000, 1, false, true), 2_000);

    ObjectLongPair<ItemStack> capped = EUStackType.INSTANCE.fillContainer(almostFull, new EUStack(5_000));
    ObjectLongPair<ItemStack> unchanged = EUStackType.INSTANCE.fillContainer(rejected, new EUStack(5_000));

    assertEquals(1_000, capped.rightLong());
    assertEquals(10_000, getCharge(capped.left()));
    assertEquals(0, unchanged.rightLong());
    assertEquals(2_000, getCharge(unchanged.left()));
}

@Test
void fillHandlesLongMaxCapacityInAtMost1024ExactChunks() {
    TestElectricItem item = new TestElectricItem(Long.MAX_VALUE, 1, true, true);
    ItemStack empty = electricStack(item, 0);

    ObjectLongPair<ItemStack> result = EUStackType.INSTANCE.fillContainer(empty, new EUStack(Long.MAX_VALUE));

    assertEquals(Long.MAX_VALUE, result.rightLong());
    assertEquals(Long.MAX_VALUE, getCharge(result.left()));
    assertTrue(item.manager.chargeCalls <= 1024);
}
```

- [ ] **Step 2: Run the targeted test and verify RED**

Run:

```powershell
.\gradlew.bat test --tests cn.dancingsnow.appeu.storage.EUStackTypeContainerTest
```

Expected: FAIL because `fillContainer` still reports zero and leaves the returned item uncharged.

- [ ] **Step 3: Implement copied, bounded manager transfers and fill behavior**

Add the IC2 manager import and exact chunk constant:

```java
import ic2.api.item.ElectricItem;

private static final long MAX_EXACT_MANAGER_TRANSFER = 1L << 53;
```

Replace `fillContainer` and add the shared helpers:

```java
@Override
public @NotNull ObjectLongPair<ItemStack> fillContainer(@NotNull ItemStack container, @NotNull EUStack stack) {
    ItemStack result = copySingle(container);
    ChargeState state = getChargeState(result);
    if (state == null || stack.getStackSize() <= 0) return new ObjectLongImmutablePair<>(result, 0);

    long requested = Math.min(stack.getStackSize(), state.capacity - state.charge);
    return new ObjectLongImmutablePair<>(result, transfer(result, requested, true));
}

private static ItemStack copySingle(ItemStack container) {
    ItemStack copy = container.copy();
    copy.stackSize = 1;
    return copy;
}

private static long transfer(ItemStack container, long requested, boolean charging) {
    long remaining = Math.max(0, requested);
    long transferred = 0;

    while (remaining > 0) {
        ChargeState before = getChargeState(container);
        if (before == null) break;

        long available = charging ? before.capacity - before.charge : before.charge;
        long chunk = Math.min(Math.min(remaining, available), MAX_EXACT_MANAGER_TRANSFER);
        if (chunk <= 0) break;

        if (charging) {
            ElectricItem.manager.charge(container, chunk, Integer.MAX_VALUE, true, false);
        } else {
            ElectricItem.manager.discharge(container, chunk, Integer.MAX_VALUE, true, false, false);
        }

        ChargeState after = getChargeState(container);
        if (after == null) break;

        long delta = charging ? after.charge - before.charge : before.charge - after.charge;
        if (delta <= 0) break;
        delta = Math.min(delta, chunk);

        transferred += delta;
        remaining -= delta;
        if (delta < chunk) break;
    }

    return transferred;
}
```

- [ ] **Step 4: Run the targeted test and verify GREEN**

Run:

```powershell
.\gradlew.bat test --tests cn.dancingsnow.appeu.storage.EUStackTypeContainerTest
```

Expected: PASS, including the `Long.MAX_VALUE` test with no more than 1024 manager calls.

- [ ] **Step 5: Commit charging support**

```powershell
git add src/main/java/cn/dancingsnow/appeu/storage/EUStackType.java src/test/java/cn/dancingsnow/appeu/storage/EUStackTypeContainerTest.java
git commit -m "feat: charge electric items from AE terminals"
```

### Task 3: Discharge Electric Items Into The AE Network

**Files:**
- Modify: `src/test/java/cn/dancingsnow/appeu/storage/EUStackTypeContainerTest.java`
- Modify: `src/main/java/cn/dancingsnow/appeu/storage/EUStackType.java:80-105`

- [ ] **Step 1: Add failing drain and clear tests**

Add these methods:

```java
@Test
void drainsACopyByTheRequestedAmountWithoutChangingTheOriginal() {
    ItemStack original = electricStack(new TestElectricItem(10_000, 1, true, true), 7_000);

    ObjectLongPair<ItemStack> result = EUStackType.INSTANCE
        .drainStackFromContainer(original, new EUStack(3_000));

    assertEquals(3_000, result.rightLong());
    assertEquals(4_000, getCharge(result.left()));
    assertEquals(7_000, getCharge(original));
}

@Test
void drainStopsAtStoredChargeAndReportsZeroWhenDischargingIsRejected() {
    ItemStack charged = electricStack(new TestElectricItem(10_000, 1, true, true), 2_000);
    ItemStack rejected = electricStack(new TestElectricItem(10_000, 1, true, false), 2_000);

    ObjectLongPair<ItemStack> emptied = EUStackType.INSTANCE.drainStackFromContainer(charged, new EUStack(5_000));
    ObjectLongPair<ItemStack> unchanged = EUStackType.INSTANCE.drainStackFromContainer(rejected, new EUStack(5_000));

    assertEquals(2_000, emptied.rightLong());
    assertEquals(0, getCharge(emptied.left()));
    assertEquals(0, unchanged.rightLong());
    assertEquals(2_000, getCharge(unchanged.left()));
}

@Test
void clearReturnsAnEmptyCopyOnlyAfterACompleteDischarge() {
    ItemStack charged = electricStack(new TestElectricItem(10_000, 1, true, true), 6_000);
    ItemStack rejected = electricStack(new TestElectricItem(10_000, 1, true, false), 6_000);

    ItemStack cleared = EUStackType.INSTANCE.clearFilledContainer(charged);

    assertNotNull(cleared);
    assertEquals(0, getCharge(cleared));
    assertEquals(6_000, getCharge(charged));
    assertNull(EUStackType.INSTANCE.clearFilledContainer(rejected));
}
```

- [ ] **Step 2: Run the targeted test and verify RED**

Run:

```powershell
.\gradlew.bat test --tests cn.dancingsnow.appeu.storage.EUStackTypeContainerTest
```

Expected: FAIL because drain still reports zero and successful clear still returns null.

- [ ] **Step 3: Implement drain and all-or-nothing clear behavior**

Replace the two placeholders:

```java
@Override
public @NotNull ObjectLongPair<ItemStack> drainStackFromContainer(@NotNull ItemStack container,
    @NotNull EUStack stack) {
    ItemStack result = copySingle(container);
    ChargeState state = getChargeState(result);
    if (state == null || stack.getStackSize() <= 0) return new ObjectLongImmutablePair<>(result, 0);

    long requested = Math.min(stack.getStackSize(), state.charge);
    return new ObjectLongImmutablePair<>(result, transfer(result, requested, false));
}

@Override
public @Nullable ItemStack clearFilledContainer(@NotNull ItemStack container) {
    ItemStack result = copySingle(container);
    ChargeState state = getChargeState(result);
    if (state == null) return null;
    if (state.charge == 0) return result;

    long discharged = transfer(result, state.charge, false);
    return discharged == state.charge ? result : null;
}
```

- [ ] **Step 4: Run the targeted test and verify GREEN**

Run:

```powershell
.\gradlew.bat test --tests cn.dancingsnow.appeu.storage.EUStackTypeContainerTest
```

Expected: PASS with all container tests green.

- [ ] **Step 5: Commit discharging support**

```powershell
git add src/main/java/cn/dancingsnow/appeu/storage/EUStackType.java src/test/java/cn/dancingsnow/appeu/storage/EUStackTypeContainerTest.java
git commit -m "feat: discharge electric items into AE terminals"
```

### Task 4: Verify The Complete Container Integration

**Files:**
- Modify only files required to correct failures caused by Tasks 1-3.

- [ ] **Step 1: Run all storage tests**

Run:

```powershell
.\gradlew.bat test --tests "cn.dancingsnow.appeu.storage.*"
```

Expected: PASS for the new electric-container suite and all existing EU stack, list, and cell tests.

- [ ] **Step 2: Run the complete test suite**

Run:

```powershell
.\gradlew.bat test
```

Expected: BUILD SUCCESSFUL with no failing tests.

- [ ] **Step 3: Build through IDEA as required by repository tooling**

Use IDEA MCP `build_project` with project path `D:\JetBrianProjects\IdeaProjects\Applied-Energistics-EU-Network` and these files:

```text
src/main/java/cn/dancingsnow/appeu/storage/EUStackType.java
src/test/java/cn/dancingsnow/appeu/storage/EUStackTypeContainerTest.java
```

Expected: `isSuccess=true` and an empty problems list.

- [ ] **Step 4: Run the complete Gradle build**

Run:

```powershell
.\gradlew.bat build
```

Expected: BUILD SUCCESSFUL and a produced mod JAR.

- [ ] **Step 5: Check formatting and exact scope**

Run:

```powershell
git diff --check HEAD~3
git status --short
git log -5 --oneline
```

Expected: no whitespace errors; only intentional files are changed; the three implementation commits appear after the two design commits.

- [ ] **Step 6: Perform focused code review**

Review these invariants against the final diff:

```text
- Every manager mutation operates on an ItemStack copy.
- Reported EU equals the observed before/after charge delta.
- Each double transfer argument is at most 2^53.
- Long.MAX_VALUE requests do not overflow.
- Incomplete clear operations return null.
- convertStackFromItem remains unchanged and unsupported.
```

Expected: all six invariants are visibly satisfied by production code and covered by focused tests.
