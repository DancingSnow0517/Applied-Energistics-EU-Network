# Signed EU Delta Compatibility Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Fix zero-buffer ME hatch transfers and exception-driven tick cost by supporting AE2's signed EU change records without allowing negative stored energy.

**Architecture:** `EUStack` adopts AE2's signed transient amount contract while retaining checked `long` arithmetic. Existing `EUStackList` aggregation is verified against positive and negative deltas, while AE2 CellInventory and the existing transfer clamps remain the positive-storage boundary.

**Tech Stack:** Java 8 bytecode with Jabel syntax, Forge 1.7.10, AE2 Unofficial rv3-beta-1000-GTNH, GT5-Unofficial 5.09.54.20, JUnit Jupiter 5, Gradle.

---

## File Map

**Modify:**

- `src/main/java/cn/dancingsnow/appeu/storage/EUStack.java`: accept signed transient amounts and keep checked arithmetic.
- `src/test/java/cn/dancingsnow/appeu/storage/EUStackTest.java`: cover signed values, codecs, and both overflow directions.
- `src/test/java/cn/dancingsnow/appeu/storage/EUStackListTest.java`: cover signed aggregation, exact-zero cleanup, and standalone negative deltas.
- `docs/manual-test-checklist.md`: record transfer recovery, conservation, active state, and post-fix tick cost.

No hatch class, transfer rate, monitor lookup, cell capacity, or MetaTileEntity registration changes are part of this fix.

### Task 1: Support AE2 signed EU change records

**Files:**

- Modify: `src/test/java/cn/dancingsnow/appeu/storage/EUStackTest.java`
- Modify: `src/test/java/cn/dancingsnow/appeu/storage/EUStackListTest.java`
- Modify: `src/main/java/cn/dancingsnow/appeu/storage/EUStack.java`

- [ ] **Step 1: Replace the obsolete negative-rejection test with a signed-delta test**

In `EUStackTest`, replace `rejectsNegativeAmountsAndOverflow` with these two tests:

```java
@Test
void supportsSignedNetworkChangeDeltas() {
    EUStack delta = new EUStack(-40);

    assertTrue(delta.isMeaningful());
    assertEquals(-40, delta.getStackSize());
    assertEquals(-20, delta.setStackSize(-20).getStackSize());

    delta.incStackSize(5);
    assertEquals(-15, delta.getStackSize());
    delta.decStackSize(-5);
    assertEquals(-10, delta.getStackSize());
    delta.add(new EUStack(15));
    assertEquals(5, delta.getStackSize());
}

@Test
void keepsCheckedArithmeticForSignedAmounts() {
    assertThrows(ArithmeticException.class, () -> new EUStack(Long.MAX_VALUE).incStackSize(1));
    assertThrows(ArithmeticException.class, () -> new EUStack(Long.MIN_VALUE).decStackSize(1));
    assertThrows(ArithmeticException.class, () -> new EUStack(Long.MAX_VALUE).add(new EUStack(1)));
}
```

- [ ] **Step 2: Add signed codec regression tests**

Add both tests to `EUStackTest`:

```java
@Test
void roundTripsNegativeDeltaThroughNbt() {
    long amount = -((long) Integer.MAX_VALUE + 42);
    NBTTagCompound tag = new NBTTagCompound();

    new EUStack(amount).writeToNBT(tag);

    assertEquals(amount, EUStack.fromNBT(tag).getStackSize());
}

@Test
void roundTripsNegativeDeltaThroughPacket() throws Exception {
    long amount = -((long) Integer.MAX_VALUE + 42);
    ByteBuf buffer = Unpooled.buffer();

    new EUStack(amount).writeToPacket(buffer);

    assertEquals(amount, EUStack.fromPacket(buffer).getStackSize());
    assertEquals(0, buffer.readableBytes());
}
```

- [ ] **Step 3: Add signed list behavior tests**

Add to `EUStackListTest`:

```java
@Test
void appliesSignedDeltasAndDropsAnExactZeroAggregate() {
    EUStackList list = new EUStackList();
    list.addStorage(new EUStack(100));

    list.add(new EUStack(-40));
    assertEquals(60, list.getFirstItem().getStackSize());

    list.addRequestable(new EUStack(-60));
    assertTrue(list.isEmpty());
    assertNull(list.getFirstItem());
}

@Test
void standaloneNegativeDeltaRemainsMeaningfulAndIterable() {
    EUStackList list = new EUStackList();
    list.add(new EUStack(-25));

    assertFalse(list.isEmpty());
    assertEquals(-25, list.getFirstItem().getStackSize());
    assertEquals(-25, list.iterator().next().getStackSize());
}
```

- [ ] **Step 4: Run the focused tests and verify RED**

Run:

```powershell
./gradlew.bat test --tests "cn.dancingsnow.appeu.storage.EUStackTest" --tests "cn.dancingsnow.appeu.storage.EUStackListTest"
```

Expected: tests fail because `EUStack` still throws `IllegalArgumentException` when constructing or assigning the first
negative delta. The failure must be from the signed-value contract, not compilation or registry initialization.

- [ ] **Step 5: Implement the minimal signed contract**

In `EUStack`:

```java
public EUStack(long amount) {
    this.amount = amount;
}

@Override
public EUStack setStackSize(long stackSize) {
    amount = stackSize;
    return this;
}

@Override
public boolean isMeaningful() {
    return amount != 0;
}

@Override
public void incStackSize(long delta) {
    amount = Math.addExact(amount, delta);
}

@Override
public void decStackSize(long delta) {
    amount = Math.subtractExact(amount, delta);
}
```

Delete `requireNonNegative`. Keep `add` delegating to `incStackSize`, so signed addition and overflow checks use one path.

- [ ] **Step 6: Run focused and full tests and verify GREEN**

Run:

```powershell
./gradlew.bat test --tests "cn.dancingsnow.appeu.storage.EUStackTest" --tests "cn.dancingsnow.appeu.storage.EUStackListTest"
./gradlew.bat test
```

Expected: focused tests and the full pure suite pass without loading Forge registries or executing FML lifecycle code.

- [ ] **Step 7: Compile and commit**

Run: `./gradlew.bat compileJava`

Expected: production code compiles against AE2's `IAEStack` contract.

```bash
git add src/main/java/cn/dancingsnow/appeu/storage/EUStack.java \
  src/test/java/cn/dancingsnow/appeu/storage/EUStackTest.java \
  src/test/java/cn/dancingsnow/appeu/storage/EUStackListTest.java
git commit -m "fix: support AE signed EU deltas"
```

### Task 2: Document runtime regression checks and verify the release

**Files:**

- Modify: `docs/manual-test-checklist.md`

- [ ] **Step 1: Add a recordable runtime regression section**

Add section 14 with Pass/Fail/Blocked, structured evidence, and these steps:

1. Back up the world because an affected older build may have removed EU before throwing.
2. On the new build, connect one energy hatch to an active, channel-enabled ME network containing a recorded EU amount.
3. Record network EU and `mStoredEnergy`, run exactly 20 ticks, then record both again.
4. Repeat for a dynamo hatch with a known local buffer and free EU-cell capacity.
5. Confirm the machine active state updates after the proxy becomes active or inactive.
6. Measure the same hatch's average CPU load over at least 100 ticks after a warm-up period and capture the first fatal
   exception if any remains.

Expected results must state:

- energy-hatch buffer gain equals network loss and is bounded by `20 * V[tier] * amperage` and free buffer capacity;
- dynamo-hatch buffer loss equals network gain and uses the same throughput bound;
- `mStoredEnergy` no longer remains zero when EU is available and the hatch has room;
- active/inactive follows the AE proxy within the existing 20-tick update interval;
- logs contain no `EU amount cannot be negative` exception;
- per-hatch CPU time no longer shows the repeated-exception spike; record the measured value rather than imposing an
  arbitrary hardware-independent millisecond threshold.

Update the final result from 13 to 14 sections.

- [ ] **Step 2: Commit the checklist**

Run: `git diff --check`

Expected: no whitespace errors.

```bash
git add docs/manual-test-checklist.md
git commit -m "docs: add signed EU transfer regression checks"
```

- [ ] **Step 3: Run final verification**

Run the IDEA project build, then:

```powershell
./gradlew.bat test
./gradlew.bat compileJava processResources
./gradlew.bat build
rg "requireNonNegative|EU amount cannot be negative" src/main src/test
rg "runClient|runServer|GameRegistry|AEStackTypeRegistry.register|METATILEENTITIES" src/test
git diff --check
git status --short
```

Expected: IDEA and all Gradle commands succeed; both searches have no matches; the worktree is clean.

- [ ] **Step 4: Final review**

Review the complete delta for AE2 signed-delta compatibility, positive stored-energy boundaries, overflow/underflow,
energy conservation, classloading, test purity, and scope. Fix every Critical or Important finding, re-review, and repeat
the full verification before producing the game-test build.
