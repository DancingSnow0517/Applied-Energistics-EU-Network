# Applied Energistics EU Network Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Add GregTech EU as a native AE2 stack type with eight storage-cell tiers and LV-UXV ME energy/dynamo hatches, including IV-UXV laser variants.

**Architecture:** A custom `EUStackType` and EU-specific `CellInventory` integrate with AE2's generic storage APIs. Pure value objects define cell tiers, hatch specifications, ID allocation, and conservation-safe transfer logic; registry-facing item and hatch classes adapt those values to Forge, AE2, and GT5U without being instantiated by automated tests.

**Tech Stack:** Java 8 bytecode with Jabel syntax, Forge 1.7.10, GT5-Unofficial 5.09.54.20, AE2 Unofficial rv3-beta-1000-GTNH, Gradle Kotlin/Groovy scripts, JUnit Jupiter 5.

---

## File Map

**Modify:**

- `build.gradle.kts`: enable JUnit Platform.
- `dependencies.gradle`: add JUnit Jupiter.
- `src/main/java/cn/dancingsnow/appeu/AppEU.java`: real mod metadata and dependencies.
- `src/main/java/cn/dancingsnow/appeu/Config.java`: configurable MetaTileEntity ID start.
- `src/main/java/cn/dancingsnow/appeu/CommonProxy.java`: lifecycle registration order.
- `src/main/java/cn/dancingsnow/appeu/ClientProxy.java`: client-only icon registration when needed.
- `src/main/resources/mcmod.info`: real metadata.

**Create core storage:**

- `src/main/java/cn/dancingsnow/appeu/storage/EUConstants.java`
- `src/main/java/cn/dancingsnow/appeu/storage/EUCellTier.java`
- `src/main/java/cn/dancingsnow/appeu/storage/EUStack.java`
- `src/main/java/cn/dancingsnow/appeu/storage/EUStackList.java`
- `src/main/java/cn/dancingsnow/appeu/storage/EUStackType.java`
- `src/main/java/cn/dancingsnow/appeu/storage/cell/EUCellInventory.java`
- `src/main/java/cn/dancingsnow/appeu/storage/cell/EUCellInventoryHandler.java`
- `src/main/java/cn/dancingsnow/appeu/storage/cell/EUCellHandler.java`

**Create items and registries:**

- `src/main/java/cn/dancingsnow/appeu/item/ItemEUStorageComponent.java`
- `src/main/java/cn/dancingsnow/appeu/item/ItemEUStorageCell.java`
- `src/main/java/cn/dancingsnow/appeu/registry/ModItems.java`
- `src/main/java/cn/dancingsnow/appeu/registry/StorageRegistration.java`
- `src/main/java/cn/dancingsnow/appeu/registry/HatchRegistration.java`
- `src/main/java/cn/dancingsnow/appeu/registry/RecipeRegistration.java`

**Create hatch logic and adapters:**

- `src/main/java/cn/dancingsnow/appeu/hatch/HatchDirection.java`
- `src/main/java/cn/dancingsnow/appeu/hatch/HatchFamily.java`
- `src/main/java/cn/dancingsnow/appeu/hatch/HatchSpec.java`
- `src/main/java/cn/dancingsnow/appeu/hatch/HatchSpecs.java`
- `src/main/java/cn/dancingsnow/appeu/hatch/transfer/EnergyPort.java`
- `src/main/java/cn/dancingsnow/appeu/hatch/transfer/EnergyTransfer.java`
- `src/main/java/cn/dancingsnow/appeu/hatch/AEGridEnergyPort.java`
- `src/main/java/cn/dancingsnow/appeu/hatch/MEHatchConnection.java`
- `src/main/java/cn/dancingsnow/appeu/hatch/MTEHatchMEEnergy.java`
- `src/main/java/cn/dancingsnow/appeu/hatch/MTEHatchMEEnergyMulti.java`
- `src/main/java/cn/dancingsnow/appeu/hatch/MTEHatchMEEnergyTunnel.java`
- `src/main/java/cn/dancingsnow/appeu/hatch/MTEHatchMEDynamo.java`
- `src/main/java/cn/dancingsnow/appeu/hatch/MTEHatchMEDynamoMulti.java`
- `src/main/java/cn/dancingsnow/appeu/hatch/MTEHatchMEDynamoTunnel.java`

**Create resources and documentation:**

- `src/main/resources/assets/appeu/lang/en_US.lang`
- `src/main/resources/assets/appeu/lang/zh_CN.lang`
- Sixteen tiered item PNGs under `src/main/resources/assets/appeu/textures/items/`
- `src/main/resources/assets/appeu/textures/items/eu_stack.png`
- `src/main/resources/assets/appeu/textures/gui/eu_stack.png`
- `docs/manual-test-checklist.md`

**Create pure tests:**

- `src/test/java/cn/dancingsnow/appeu/storage/EUCellTierTest.java`
- `src/test/java/cn/dancingsnow/appeu/storage/EUStackTest.java`
- `src/test/java/cn/dancingsnow/appeu/storage/EUStackListTest.java`
- `src/test/java/cn/dancingsnow/appeu/hatch/transfer/EnergyTransferTest.java`
- `src/test/java/cn/dancingsnow/appeu/hatch/HatchSpecsTest.java`

Automated tests must not call Forge registration, `AEStackTypeRegistry.register`, `ICellRegistry.addCellHandler`, a MetaTileEntity ID constructor, or an FML lifecycle method.

---

### Task 1: Add the pure JUnit test harness

**Files:**
- Modify: `build.gradle.kts`
- Modify: `dependencies.gradle`
- Create: `src/test/java/cn/dancingsnow/appeu/TestHarnessTest.java`

- [ ] **Step 1: Write the failing smoke test**

```java
package cn.dancingsnow.appeu;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;

class TestHarnessTest {
    @Test
    void runsWithoutBootstrappingMinecraft() {
        assertEquals(2, 1 + 1);
    }
}
```

- [ ] **Step 2: Verify the missing-JUnit failure**

Run: `./gradlew.bat test --tests "cn.dancingsnow.appeu.TestHarnessTest"`

Expected: test compilation fails because `org.junit.jupiter.api` is absent.

- [ ] **Step 3: Add the test dependency and engine**

Inside `dependencies {}` in `dependencies.gradle`:

```groovy
testImplementation("org.junit.jupiter:junit-jupiter:5.12.2")
```

Append to `build.gradle.kts`:

```kotlin
tasks.withType<Test>().configureEach {
    useJUnitPlatform()
}
```

- [ ] **Step 4: Verify the smoke test passes**

Run: `./gradlew.bat test --tests "cn.dancingsnow.appeu.TestHarnessTest"`

Expected: one test passes without Minecraft or FML startup.

- [ ] **Step 5: Commit**

```bash
git add build.gradle.kts dependencies.gradle src/test/java/cn/dancingsnow/appeu/TestHarnessTest.java
git commit -m "test: configure pure JUnit tests"
```

### Task 2: Define EU capacity constants and cell tiers

**Files:**
- Create: `src/main/java/cn/dancingsnow/appeu/storage/EUConstants.java`
- Create: `src/main/java/cn/dancingsnow/appeu/storage/EUCellTier.java`
- Create: `src/test/java/cn/dancingsnow/appeu/storage/EUCellTierTest.java`

- [ ] **Step 1: Write failing capacity tests**

```java
class EUCellTierTest {
    @Test
    void calculatesConfirmedCapacities() {
        assertEquals(1_073_741_824L, EUCellTier.K1.capacityEU());
        assertEquals(4_294_967_296L, EUCellTier.K4.capacityEU());
        assertEquals(17_592_186_044_416L, EUCellTier.K16384.capacityEU());
        assertEquals(16_777_216L, EUCellTier.K16384.totalBytes());
    }

    @Test
    void resolvesOnlyEightMetadataValues() {
        for (EUCellTier tier : EUCellTier.values()) assertEquals(tier, EUCellTier.fromMeta(tier.meta()));
        assertThrows(IllegalArgumentException.class, () -> EUCellTier.fromMeta(-1));
        assertThrows(IllegalArgumentException.class, () -> EUCellTier.fromMeta(8));
    }

    @Test
    void idleDrainUsesHalfAeSteps() {
        assertEquals(0.5, EUCellTier.K1.idleDrain());
        assertEquals(4.0, EUCellTier.K16384.idleDrain());
    }

    @Test
    void usesOneTypeWithNoTypeOverhead() {
        assertEquals(1, EUConstants.TOTAL_TYPES);
        assertEquals(0, EUConstants.BYTES_PER_TYPE);
    }
}
```

- [ ] **Step 2: Verify the missing-type failure**

Run: `./gradlew.bat test --tests "cn.dancingsnow.appeu.storage.EUCellTierTest"`

Expected: compilation fails because `EUCellTier` is absent.

- [ ] **Step 3: Implement constants and tiers**

```java
public final class EUConstants {
    public static final String STACK_TYPE_ID = "appeu.eu";
    public static final String NBT_AMOUNT = "amount";
    public static final long EU_PER_BYTE = 1024L * 1024L;
    public static final int TOTAL_TYPES = 1;
    public static final int BYTES_PER_TYPE = 0;
    private EUConstants() {}
}
```

```java
public enum EUCellTier {
    K1(0, "1k", 1, 0.5), K4(1, "4k", 4, 1.0), K16(2, "16k", 16, 1.5),
    K64(3, "64k", 64, 2.0), K256(4, "256k", 256, 2.5),
    K1024(5, "1024k", 1024, 3.0), K4096(6, "4096k", 4096, 3.5),
    K16384(7, "16384k", 16384, 4.0);

    private final int meta;
    private final String suffix;
    private final long nominalKiB;
    private final double idleDrain;

    EUCellTier(int meta, String suffix, long nominalKiB, double idleDrain) {
        this.meta = meta;
        this.suffix = suffix;
        this.nominalKiB = nominalKiB;
        this.idleDrain = idleDrain;
    }

    public int meta() { return meta; }
    public String suffix() { return suffix; }
    public double idleDrain() { return idleDrain; }
    public long totalBytes() { return Math.multiplyExact(nominalKiB, 1024L); }
    public long capacityEU() { return Math.multiplyExact(totalBytes(), EUConstants.EU_PER_BYTE); }
    public static EUCellTier fromMeta(int meta) {
        if (meta < 0 || meta >= values().length) throw new IllegalArgumentException("Invalid metadata: " + meta);
        return values()[meta];
    }
}
```

- [ ] **Step 4: Run tests**

Run: `./gradlew.bat test --tests "cn.dancingsnow.appeu.storage.EUCellTierTest"`

Expected: all tier tests pass.

- [ ] **Step 5: Commit**

```bash
git add src/main/java/cn/dancingsnow/appeu/storage src/test/java/cn/dancingsnow/appeu/storage/EUCellTierTest.java
git commit -m "feat: define EU storage capacities"
```

### Task 3: Implement the EU stack and serialization

**Files:**
- Create: `src/main/java/cn/dancingsnow/appeu/storage/EUStack.java`
- Create: `src/test/java/cn/dancingsnow/appeu/storage/EUStackTest.java`

- [ ] **Step 1: Write failing behavior and round-trip tests**

```java
class EUStackTest {
    @Test
    void copiesAndAddsWithoutSharingState() {
        EUStack original = new EUStack(40);
        EUStack copy = original.copy();
        copy.add(new EUStack(2));
        assertNotSame(original, copy);
        assertEquals(40, original.getStackSize());
        assertEquals(42, copy.getStackSize());
    }

    @Test
    void rejectsNegativeAndOverflowingAmounts() {
        assertThrows(IllegalArgumentException.class, () -> new EUStack(-1));
        assertThrows(ArithmeticException.class, () -> new EUStack(Long.MAX_VALUE).add(new EUStack(1)));
        assertThrows(IllegalArgumentException.class, () -> new EUStack(1).decStackSize(2));
    }

    @Test
    void roundTripsNbtAndPacket() throws Exception {
        EUStack source = new EUStack(9_876_543_210L);
        NBTTagCompound tag = new NBTTagCompound();
        source.writeToNBT(tag);
        assertEquals(source.getStackSize(), EUStack.fromNBT(tag).getStackSize());
        ByteBuf buffer = Unpooled.buffer();
        source.writeToPacket(buffer);
        assertEquals(source.getStackSize(), EUStack.fromPacket(buffer).getStackSize());
    }
}
```

- [ ] **Step 2: Verify the missing-type failure**

Run: `./gradlew.bat test --tests "cn.dancingsnow.appeu.storage.EUStackTest"`

Expected: compilation fails because `EUStack` is absent.

- [ ] **Step 3: Implement `IAEStack<EUStack>` directly**

Do not extend AE2 `AEStack`; its static encoded-pattern lookup touches AE definitions during class loading. Implement the interface directly with these amount and codec methods:

```java
public final class EUStack implements IAEStack<EUStack> {
    private long amount;
    public EUStack(long amount) { setStackSize(amount); }
    public static EUStack fromNBT(NBTTagCompound tag) { return new EUStack(tag.getLong(EUConstants.NBT_AMOUNT)); }
    public static EUStack fromPacket(ByteBuf buffer) { return new EUStack(buffer.readLong()); }
    @Override public void add(EUStack other) { setStackSize(Math.addExact(amount, other.amount)); }
    @Override public long getStackSize() { return amount; }
    @Override public EUStack setStackSize(long value) {
        if (value < 0) throw new IllegalArgumentException("Negative EU: " + value);
        amount = value;
        return this;
    }
    @Override public void incStackSize(long value) { setStackSize(Math.addExact(amount, value)); }
    @Override public void decStackSize(long value) { setStackSize(Math.subtractExact(amount, value)); }
    @Override public void writeToNBT(NBTTagCompound tag) { tag.setLong(EUConstants.NBT_AMOUNT, amount); }
    @Override public void writeToPacket(ByteBuf buffer) { buffer.writeLong(amount); }
    @Override public EUStack copy() { return new EUStack(amount); }
    @Override public EUStack empty() { return new EUStack(0); }
}
```

Complete the other interface methods with fixed behavior:

- Requestable/crafting getters return zero/false; setters and increment methods retain no state.
- `reset` sets amount to zero; `isMeaningful` means amount is positive.
- Fuzzy/same-type checks accept only another `EUStack`.
- Tag compound methods return null/no-op/false.
- `isItem`, `isFluid` are false and `getChannel` is null.
- Names are `appeu.stack.eu`, `EU Energy`, and mod ID `appeu`.
- `getItemStackForNEI` returns the registered 1k component when available.
- Client draw methods render the EU icon and a local amount overlay without calling AE2 `AEStack`.
- `getAmountPerUnit` is 1; `getStackType` is `EUStackType.INSTANCE`.
- `equals` and `hashCode` represent the single EU identity and exclude amount.

- [ ] **Step 4: Run tests and compile**

Run: `./gradlew.bat test --tests "cn.dancingsnow.appeu.storage.EUStackTest" compileJava`

Expected: stack tests pass and every target `IAEStack` method is implemented.

- [ ] **Step 5: Commit**

```bash
git add src/main/java/cn/dancingsnow/appeu/storage/EUStack.java src/test/java/cn/dancingsnow/appeu/storage/EUStackTest.java
git commit -m "feat: add AE EU stack"
```

### Task 4: Implement the single-value list and stack type

**Files:**
- Create: `src/main/java/cn/dancingsnow/appeu/storage/EUStackList.java`
- Create: `src/main/java/cn/dancingsnow/appeu/storage/EUStackType.java`
- Create: `src/test/java/cn/dancingsnow/appeu/storage/EUStackListTest.java`

- [ ] **Step 1: Write failing list tests**

```java
class EUStackListTest {
    @Test
    void aggregatesTheSingleIdentity() {
        EUStackList list = new EUStackList();
        list.addStorage(new EUStack(10));
        list.addStorage(new EUStack(7));
        assertEquals(1, list.size());
        assertEquals(17, list.findPrecise(new EUStack(1)).getStackSize());
    }

    @Test
    void resetRemovesTheEntry() {
        EUStackList list = new EUStackList();
        list.add(new EUStack(5));
        list.resetStatus();
        assertTrue(list.isEmpty());
        assertNull(list.getFirstItem());
    }
}
```

- [ ] **Step 2: Verify the missing-type failure**

Run: `./gradlew.bat test --tests "cn.dancingsnow.appeu.storage.EUStackListTest"`

Expected: compilation fails because `EUStackList` is absent.

- [ ] **Step 3: Implement one-entry semantics**

```java
private EUStack stored;
private void merge(EUStack option) {
    if (option == null || !option.isMeaningful()) return;
    if (stored == null) stored = option.copy(); else stored.add(option);
}
@Override public void add(EUStack option) { merge(option); }
@Override public void addStorage(EUStack option) { merge(option); }
@Override public void addRequestable(EUStack option) { merge(option); }
@Override public void addCrafting(EUStack option) {}
@Override public int size() { return isEmpty() ? 0 : 1; }
@Override public boolean isEmpty() { return stored == null || !stored.isMeaningful(); }
@Override public EUStack getFirstItem() { return isEmpty() ? null : stored; }
@Override public void resetStatus() { stored = null; }
```

`findPrecise` accepts any non-null EU request. `findFuzzy` returns an empty or singleton collection. Iterator returns an empty or singleton iterator. `getStackType` returns `EUStackType.INSTANCE`.

- [ ] **Step 4: Implement `EUStackType`**

Create `INSTANCE`. Return ID `appeu.eu`, unit `EU`, amount per unit `1`, amount per byte `Math.toIntExact(EUConstants.EU_PER_BYTE)`, `new EUStackList()` from both list factories, and `new EUStack(1)` as the test stack. NBT and packet loaders delegate to `EUStack`. All container APIs return unsupported results:

```java
@Override public boolean isContainerItemForType(ItemStack container) { return false; }
@Override public EUStack getStackFromContainerItem(ItemStack container) { return null; }
@Override public EUStack convertStackFromItem(ItemStack itemStack) { return null; }
@Override public long getContainerItemCapacity(ItemStack container, EUStack stack) { return 0; }
@Override public ObjectLongPair<ItemStack> drainStackFromContainer(ItemStack container, EUStack stack) {
    return new ObjectLongImmutablePair<>(container, 0);
}
@Override public ItemStack clearFilledContainer(ItemStack container) { return null; }
@Override public ObjectLongPair<ItemStack> fillContainer(ItemStack container, EUStack stack) {
    return new ObjectLongImmutablePair<>(container, 0);
}
```

Register `appeu:eu_stack` as its icon and return `appeu:textures/gui/eu_stack.png` as button texture.

- [ ] **Step 5: Run tests and compile**

Run: `./gradlew.bat test --tests "cn.dancingsnow.appeu.storage.EUStackListTest" compileJava`

Expected: list tests pass and stack-type methods match the target AE2 API.

- [ ] **Step 6: Commit**

```bash
git add src/main/java/cn/dancingsnow/appeu/storage src/test/java/cn/dancingsnow/appeu/storage/EUStackListTest.java
git commit -m "feat: add EU stack type model"
```

### Task 5: Implement pure conservation-safe transfer logic

**Files:**
- Create: `src/main/java/cn/dancingsnow/appeu/hatch/transfer/EnergyPort.java`
- Create: `src/main/java/cn/dancingsnow/appeu/hatch/transfer/EnergyTransfer.java`
- Create: `src/test/java/cn/dancingsnow/appeu/hatch/transfer/EnergyTransferTest.java`

- [ ] **Step 1: Write failing partial-transfer tests**

```java
class EnergyTransferTest {
    @Test
    void pullUsesOnlyActualExtraction() {
        FakePort network = new FakePort(600, 10_000);
        assertEquals(600, EnergyTransfer.pull(network, 100, 1_000, 800));
        assertEquals(0, network.stored);
    }

    @Test
    void pushKeepsUnacceptedRemainder() {
        FakePort network = new FakePort(0, 250);
        assertEquals(250, EnergyTransfer.push(network, 900, 800));
        assertEquals(250, network.stored);
    }

    @Test
    void zeroLimitsAreNoOps() {
        FakePort network = new FakePort(100, 100);
        assertEquals(0, EnergyTransfer.pull(network, 0, 1_000, 100));
        assertEquals(0, EnergyTransfer.push(network, 100, 0));
    }

    @Test
    void fullLocalBufferAndFullNetworkAreNoOps() {
        FakePort source = new FakePort(1_000, 1_000);
        assertEquals(0, EnergyTransfer.pull(source, 500, 500, 100));
        assertEquals(1_000, source.stored);

        FakePort destination = new FakePort(500, 500);
        assertEquals(0, EnergyTransfer.push(destination, 100, 100));
        assertEquals(500, destination.stored);
    }

    @Test
    void everyReportedMoveIsConserved() {
        FakePort source = new FakePort(400, 1_000);
        long pulled = EnergyTransfer.pull(source, 100, 1_000, 300);
        assertEquals(400, source.stored + pulled);

        FakePort destination = new FakePort(100, 250);
        long pushed = EnergyTransfer.push(destination, 500, 300);
        assertEquals(100 + pushed, destination.stored);
    }
}
```

Use this nested fake; it has no Minecraft, AE, GT, or registry dependency:

```java
private static final class FakePort implements EnergyPort {
    private long stored;
    private final long capacity;

    private FakePort(long stored, long capacity) {
        this.stored = stored;
        this.capacity = capacity;
    }

    @Override public long simulateExtract(long requested) { return Math.min(stored, requested); }
    @Override public long extract(long requested) {
        long moved = simulateExtract(requested);
        stored -= moved;
        return moved;
    }
    @Override public long simulateInsert(long offered) { return Math.min(capacity - stored, offered); }
    @Override public long insert(long offered) {
        long moved = simulateInsert(offered);
        stored += moved;
        return moved;
    }
}
```

- [ ] **Step 2: Verify missing transfer types**

Run: `./gradlew.bat test --tests "cn.dancingsnow.appeu.hatch.transfer.EnergyTransferTest"`

Expected: compilation fails.

- [ ] **Step 3: Implement port and algorithms**

```java
public interface EnergyPort {
    long simulateExtract(long requested);
    long extract(long requested);
    long simulateInsert(long offered);
    long insert(long offered);
}
```

```java
public final class EnergyTransfer {
    public static long pull(EnergyPort source, long stored, long capacity, long limit) {
        long request = Math.min(Math.max(0, capacity - stored), Math.max(0, limit));
        if (request == 0) return 0;
        long simulated = clamp(source.simulateExtract(request), 0, request);
        return clamp(source.extract(simulated), 0, simulated);
    }
    public static long push(EnergyPort destination, long stored, long limit) {
        long offer = Math.min(Math.max(0, stored), Math.max(0, limit));
        if (offer == 0) return 0;
        long simulated = clamp(destination.simulateInsert(offer), 0, offer);
        return clamp(destination.insert(simulated), 0, simulated);
    }
    private static long clamp(long value, long min, long max) { return Math.max(min, Math.min(max, value)); }
    private EnergyTransfer() {}
}
```

- [ ] **Step 4: Run tests and commit**

Run: `./gradlew.bat test --tests "cn.dancingsnow.appeu.hatch.transfer.EnergyTransferTest"`

Expected: all transfer tests pass.

```bash
git add src/main/java/cn/dancingsnow/appeu/hatch/transfer src/test/java/cn/dancingsnow/appeu/hatch/transfer/EnergyTransferTest.java
git commit -m "feat: add lossless EU transfer logic"
```

### Task 6: Define the 158-hatch specification matrix

**Files:**
- Create: `src/main/java/cn/dancingsnow/appeu/hatch/HatchDirection.java`
- Create: `src/main/java/cn/dancingsnow/appeu/hatch/HatchFamily.java`
- Create: `src/main/java/cn/dancingsnow/appeu/hatch/HatchSpec.java`
- Create: `src/main/java/cn/dancingsnow/appeu/hatch/HatchSpecs.java`
- Create: `src/test/java/cn/dancingsnow/appeu/hatch/HatchSpecsTest.java`

- [ ] **Step 1: Write failing matrix tests**

```java
class HatchSpecsTest {
    @Test
    void buildsTheConfirmedMatrix() {
        List<HatchSpec> specs = HatchSpecs.create(27_000);
        assertEquals(158, specs.size());
        assertEquals(104, specs.stream().filter(s -> s.family() != HatchFamily.LASER).count());
        assertEquals(54, specs.stream().filter(s -> s.family() == HatchFamily.LASER).count());
        assertEquals(27_000, specs.get(0).id());
        assertEquals(27_157, specs.get(157).id());
    }

    @Test
    void oneCallAddsAnotherIvThroughUxvLaserSeries() {
        List<HatchSpec> specs = HatchSpecs.create(27_000, 256, 1024, 4096, 16384);
        assertEquals(176, specs.size());
        assertTrue(specs.stream().filter(s -> s.amperage() == 16384).allMatch(s -> s.tier() >= 5));
    }

    @Test
    void categoriesMatchHatchElementBoundaries() {
        assertEquals(HatchFamily.STANDARD, HatchSpecs.family(HatchDirection.ENERGY, 2));
        assertEquals(HatchFamily.MULTI_AMP, HatchSpecs.family(HatchDirection.ENERGY, 64));
        assertEquals(HatchFamily.LASER, HatchSpecs.family(HatchDirection.ENERGY, 256));
        assertEquals(HatchFamily.STANDARD, HatchSpecs.family(HatchDirection.DYNAMO, 4));
        assertEquals(HatchFamily.MULTI_AMP, HatchSpecs.family(HatchDirection.DYNAMO, 16));
    }
}
```

- [ ] **Step 2: Verify missing specification types**

Run: `./gradlew.bat test --tests "cn.dancingsnow.appeu.hatch.HatchSpecsTest"`

Expected: compilation fails.

- [ ] **Step 3: Implement deterministic generation**

Use GT indices LV `1` through UXV `13`, IV `5` as laser minimum, standard amps `{2,4,16,64}`, and default laser amps `{256,1024,4096}`. Generate both directions and assign contiguous IDs. Build names like `appeu.hatch.energy.lv.2a`.

```java
if (amperage >= 256) return HatchFamily.LASER;
if (direction == HatchDirection.ENERGY && amperage > 2) return HatchFamily.MULTI_AMP;
if (direction == HatchDirection.DYNAMO && amperage > 4) return HatchFamily.MULTI_AMP;
return HatchFamily.STANDARD;
```

The pure factory validates positive start/amperages and unique generated names. Registry code separately validates the GT upper bound.

Expose `createLaserSeries(int startId, int amperage)` as the public 18-entry IV-UXV generator used by `create`; it validates `amperage >= 256` and returns energy/dynamo pairs with contiguous IDs beginning at `startId`.

- [ ] **Step 4: Run tests and commit**

Run: `./gradlew.bat test --tests "cn.dancingsnow.appeu.hatch.HatchSpecsTest"`

Expected: tests pass with 158 defaults and 18 entries per added laser amp.

```bash
git add src/main/java/cn/dancingsnow/appeu/hatch/Hatch*.java src/test/java/cn/dancingsnow/appeu/hatch/HatchSpecsTest.java
git commit -m "feat: define ME hatch product matrix"
```

### Task 7: Add EU component and storage-cell items

**Files:**
- Create: `src/main/java/cn/dancingsnow/appeu/item/ItemEUStorageComponent.java`
- Create: `src/main/java/cn/dancingsnow/appeu/item/ItemEUStorageCell.java`
- Create: `src/main/java/cn/dancingsnow/appeu/registry/ModItems.java`

- [ ] **Step 1: Implement the component item**

Extend `Item`, enable subtypes, set max damage zero, and resolve names/icons/sub-items through `EUCellTier.fromMeta`. Register icons as `appeu:eu_storage_component_<suffix>`.

- [ ] **Step 2: Implement the cell item**

Extend `AEBaseCell` using its `Optional<String>` constructor, enable subtypes, set max stack size one, and override per-stack values:

```java
@Override public long getBytesLong(ItemStack stack) { return tier(stack).totalBytes(); }
@Override public int getBytes(ItemStack stack) { return Math.toIntExact(getBytesLong(stack)); }
@Override public int BytePerType(ItemStack stack) { return EUConstants.BYTES_PER_TYPE; }
@Override public int getBytesPerType(ItemStack stack) { return EUConstants.BYTES_PER_TYPE; }
@Override public int getTotalTypes(ItemStack stack) { return EUConstants.TOTAL_TYPES; }
@Override public double getIdleDrain(ItemStack stack) { return tier(stack).idleDrain(); }
@Override public double getIdleDrain() { return EUCellTier.K1.idleDrain(); }
@Override public IAEStackType<?> getStackType() { return EUStackType.INSTANCE; }
```

Override disassembly so an empty cell returns the matching metadata component and AE2 empty housing. Never store source metadata in a mutable item field.

Keep cells out of other storage cells and always recognize valid metadata variants as cells:

```java
@Override public boolean storableInStorageCell() { return false; }
@Override public boolean isStorageCell(ItemStack stack) {
    return stack != null && stack.getItemDamage() >= 0 && stack.getItemDamage() < EUCellTier.values().length;
}
```

- [ ] **Step 3: Register both metadata items**

```java
public static final ItemEUStorageComponent EU_STORAGE_COMPONENT = new ItemEUStorageComponent();
public static final ItemEUStorageCell EU_STORAGE_CELL = new ItemEUStorageCell();
public static void register() {
    GameRegistry.registerItem(EU_STORAGE_COMPONENT, "eu_storage_component");
    GameRegistry.registerItem(EU_STORAGE_CELL, "eu_storage_cell");
}
```

- [ ] **Step 4: Compile and commit**

Run: `./gradlew.bat compileJava`

Expected: compilation succeeds without instantiating items in tests.

```bash
git add src/main/java/cn/dancingsnow/appeu/item src/main/java/cn/dancingsnow/appeu/registry/ModItems.java
git commit -m "feat: add EU storage items"
```

### Task 8: Add EU cell inventory and handler

**Files:**
- Create: `src/main/java/cn/dancingsnow/appeu/storage/cell/EUCellInventory.java`
- Create: `src/main/java/cn/dancingsnow/appeu/storage/cell/EUCellInventoryHandler.java`
- Create: `src/main/java/cn/dancingsnow/appeu/storage/cell/EUCellHandler.java`
- Create: `src/main/java/cn/dancingsnow/appeu/registry/StorageRegistration.java`

- [ ] **Step 1: Specialize generic cell classes**

```java
public final class EUCellInventory extends CellInventory<EUStack> {
    public EUCellInventory(ItemStack cell, ISaveProvider container) throws AppEngException { super(cell, container); }
    @Override protected EUStack readStack(NBTTagCompound tag) { return EUStack.fromNBT(tag); }
    @Override protected String getStackTypeTag() { return "euTypes"; }
    @Override protected String getStackCountTag() { return "euCount"; }
}
```

```java
public final class EUCellInventoryHandler extends CellInventoryHandler<EUStack> {
    public EUCellInventoryHandler(EUCellInventory inventory) { super(inventory, EUStackType.INSTANCE); }
    @Override protected void setOreFilteredList(String filter) {}
    @Override protected void setPriorityList(boolean fuzzy, IAEStackInventory config, FuzzyMode mode) {}
}
```

- [ ] **Step 2: Implement the drive/chest bridge**

`EUCellHandler` returns a handler only for `ItemEUStorageCell` and exact `EUStackType.INSTANCE`; catch malformed-cell `AppEngException` and log it. Reuse AE chest light/medium/dark icons, open `GuiBridge.GUI_ME`, delegate status to `CellInventoryHandler.getStatusForCell`, and idle drain to `getCellInv().getIdleDrain()`.

- [ ] **Step 3: Add checked registrations**

```java
public static void registerStackType() {
    IAEStackType<?> existing = AEStackTypeRegistry.getType(EUConstants.STACK_TYPE_ID);
    if (existing != null && existing != EUStackType.INSTANCE) {
        throw new IllegalStateException("AE stack type ID collision: " + EUConstants.STACK_TYPE_ID);
    }
    if (existing == null) AEStackTypeRegistry.register(EUStackType.INSTANCE);
}
public static void registerCellHandler() {
    AEApi.instance().registries().cell().addCellHandler(EUCellHandler.INSTANCE);
}
```

- [ ] **Step 4: Compile and commit**

Run: `./gradlew.bat compileJava`

Expected: the custom inventory compiles without changing AE2's item/fluid-only `CellInventory.getCell` factory.

```bash
git add src/main/java/cn/dancingsnow/appeu/storage/cell src/main/java/cn/dancingsnow/appeu/registry/StorageRegistration.java
git commit -m "feat: integrate EU storage cells with AE2"
```

### Task 9: Add the AE monitor adapter and shared grid lifecycle

**Files:**
- Create: `src/main/java/cn/dancingsnow/appeu/hatch/AEGridEnergyPort.java`
- Create: `src/main/java/cn/dancingsnow/appeu/hatch/MEHatchConnection.java`

- [ ] **Step 1: Implement the typed monitor adapter**

`AEGridEnergyPort` wraps an `IMEMonitor<EUStack>` and the hatch's `BaseActionSource`. Every method converts the nullable AE result into an amount and never treats the simulated result as the modulated result:

```java
public final class AEGridEnergyPort implements EnergyPort {
    private final IMEMonitor<EUStack> monitor;
    private final BaseActionSource source;

    public AEGridEnergyPort(IMEMonitor<EUStack> monitor, BaseActionSource source) {
        this.monitor = Objects.requireNonNull(monitor);
        this.source = Objects.requireNonNull(source);
    }

    @Override public long simulateExtract(long requested) {
        return amount(monitor.extractItems(new EUStack(requested), Actionable.SIMULATE, source));
    }
    @Override public long extract(long requested) {
        return amount(monitor.extractItems(new EUStack(requested), Actionable.MODULATE, source));
    }
    @Override public long simulateInsert(long offered) {
        EUStack remainder = monitor.injectItems(new EUStack(offered), Actionable.SIMULATE, source);
        return offered - amount(remainder);
    }
    @Override public long insert(long offered) {
        EUStack remainder = monitor.injectItems(new EUStack(offered), Actionable.MODULATE, source);
        return offered - amount(remainder);
    }
    private static long amount(EUStack stack) { return stack == null ? 0 : stack.getStackSize(); }
}
```

Clamp malformed monitor results to `[0, requested]` before returning. Obtain the monitor from `proxy.getStorage().getInventory(EUStackType.INSTANCE)`; a missing inventory, inactive proxy, or `GridAccessException` yields no port for that tick.

- [ ] **Step 2: Implement the reusable proxy owner**

`MEHatchConnection` owns the lazy `AENetworkProxy`, `MachineSource`, and all behavior shared by six hatch classes. Its constructor accepts the hatch `IGridProxyable`, a supplier for the current `IGregTechTileEntity`, and a supplier for the hatch's item representation.

On first proxy creation:

```java
proxy = new AENetworkProxy(owner, "proxy", visualRepresentation.get(), true);
proxy.setFlags(GridFlags.REQUIRE_CHANNEL);
proxy.setIdlePowerUsage(1.0);
updateValidSide();
```

Expose these exact delegations for each hatch class:

- `getProxy()` returns the lazy proxy.
- `getGridNode(side)` returns `getProxy().getNode()`.
- `getLocation()` builds `DimensionalCoord` from the GT base tile.
- `getCableConnectionType(side)` returns `AECableType.SMART` only for the outward front and `NONE` otherwise.
- `onFirstTick()` calls `getProxy().onReady()`.
- `onFacingChange()` sets valid sides to only the current front.
- `onColorChangeServer()` maps GT color to `AEColor`, then updates node state.
- `saveNBTData()`/`loadNBTData()` call proxy `writeToNBT`/`readFromNBT` using the parent compound.
- `energyPort()` returns an `AEGridEnergyPort` only while the proxy is active.
- `securityBreak()` is an empty implementation required by `IActionHost`.

Do not manually duplicate chunk-unload or invalidation calls: target GT's `BaseMetaTileEntity` implements `IGridProxyable` and invokes `getProxy().onChunkUnload()` and `getProxy().invalidate()` for MetaTileEntities that expose a proxy.

- [ ] **Step 3: Compile the adapter boundary**

Run: `./gradlew.bat compileJava`

Expected: the generic `EUStack` monitor resolves through the target AE2 `IAEStackType` API and all proxy methods match rv3-beta-1000-GTNH.

- [ ] **Step 4: Commit**

```bash
git add src/main/java/cn/dancingsnow/appeu/hatch/AEGridEnergyPort.java src/main/java/cn/dancingsnow/appeu/hatch/MEHatchConnection.java
git commit -m "feat: add ME hatch grid connection"
```

### Task 10: Implement standard and multi-amp ME hatches

**Files:**
- Create: `src/main/java/cn/dancingsnow/appeu/hatch/MTEHatchMEEnergy.java`
- Create: `src/main/java/cn/dancingsnow/appeu/hatch/MTEHatchMEEnergyMulti.java`
- Create: `src/main/java/cn/dancingsnow/appeu/hatch/MTEHatchMEDynamo.java`
- Create: `src/main/java/cn/dancingsnow/appeu/hatch/MTEHatchMEDynamoMulti.java`

- [ ] **Step 1: Implement 2A energy and 2A/4A dynamo inheritance**

`MTEHatchMEEnergy` extends `MTEHatchEnergy`; it is used only for 2A and keeps:

```java
@Override public long maxAmperesIn() { return 2; }
@Override public long maxWorkingAmperesIn() { return 2; }
@Override public long maxEUStore() { return 512L + V[mTier] * 8L; }
```

`MTEHatchMEDynamo` extends `MTEHatchDynamo`, accepts a fixed constructor amperage of 2 or 4, and keeps:

```java
@Override public long maxAmperesOut() { return amperage; }
@Override public long maxEUStore() {
    return amperage == 2 ? 512L + V[mTier + 1] * 2L : 512L + V[mTier] * 4L * amperage;
}
```

Both classes implement the AE grid host/connectable interfaces through `MEHatchConnection`, disable item slots, reject normal GT EU on the front (`isEnetInput/isEnetOutput` false), and recreate the same tier/amperage in `newMetaEntity`.

- [ ] **Step 2: Implement multi-amp inheritance and HatchElement classification**

`MTEHatchMEEnergyMulti` extends `MTEHatchEnergyMulti` for 4A/16A/64A, preserves `getHatchType() == 1`, and uses inherited `512 + V[tier] * 4 * amperage` capacity.

`MTEHatchMEDynamoMulti` extends `MTEHatchDynamoMulti` for 16A/64A and uses the same inherited multi-amp capacity. This inheritance is required so `HatchElement.MultiAmpEnergy` and `HatchElement.ExoticDynamo` recognize the instances through GT's own adders.

- [ ] **Step 3: Add server-tick transfer to all four classes**

Every `onPostTick` first calls `super.onPostTick`, then runs only on the logical server. Energy hatches pull and dynamos push with the fixed per-tick limit:

```java
long limit = Math.multiplyExact(V[mTier], configuredAmperage());
EnergyPort port = connection.energyPort();
if (port == null) return;

long moved = isEnergyHatch
    ? EnergyTransfer.pull(port, getEUVar(), maxEUStore(), limit)
    : EnergyTransfer.push(port, getEUVar(), limit);
setEUVar(isEnergyHatch ? Math.addExact(getEUVar(), moved) : Math.subtractExact(getEUVar(), moved));
```

Use separate class methods instead of an `isEnergyHatch` field in production. Update active state from `connection.getProxy().isActive()` every 20 ticks. A failed grid lookup or zero movement leaves the local buffer unchanged.

- [ ] **Step 4: Add overlays and tooltips**

Reuse GT `OVERLAYS_ENERGY_IN_MULTI_*` and `OVERLAYS_ENERGY_OUT_MULTI_*` according to amperage. `getDescription()` must include localized lines for voltage tier, amperage, formatted EU/t, formatted capacity, `1 AE/t`, one required channel, and transfer direction.

- [ ] **Step 5: Compile and commit**

Run: `./gradlew.bat compileJava`

Expected: all four classes compile against their GT parents and implement the complete AE grid host contract.

```bash
git add src/main/java/cn/dancingsnow/appeu/hatch/MTEHatchMEEnergy.java src/main/java/cn/dancingsnow/appeu/hatch/MTEHatchMEEnergyMulti.java src/main/java/cn/dancingsnow/appeu/hatch/MTEHatchMEDynamo.java src/main/java/cn/dancingsnow/appeu/hatch/MTEHatchMEDynamoMulti.java
git commit -m "feat: add standard ME energy hatches"
```

### Task 11: Implement ME laser hatches without GT laser transfer

**Files:**
- Create: `src/main/java/cn/dancingsnow/appeu/hatch/MTEHatchMEEnergyTunnel.java`
- Create: `src/main/java/cn/dancingsnow/appeu/hatch/MTEHatchMEDynamoTunnel.java`

- [ ] **Step 1: Extend the native tunnel classes**

`MTEHatchMEEnergyTunnel` extends `MTEHatchEnergyTunnel`; `MTEHatchMEDynamoTunnel` extends `MTEHatchDynamoTunnel`. Preserve:

- energy `getHatchType() == 2` for `HatchElement.ExoticEnergy`;
- `IConnectsToEnergyTunnel` and `ConnectionType.LASER` classification;
- native laser overlays;
- adjustable `Amperes` in the range `1..maxAmperes`;
- NBT key `amperes` and the inherited screwdriver GUI;
- `maxEUStore() == V[mTier] * 24L * Amperes`.

`newMetaEntity` must pass both the registered maximum and current saved amperage semantics to the clone.

- [ ] **Step 2: Replace both tunnel tick methods completely**

Do not call any parent `onPostTick`: the target tunnel parents themselves skip their superclass tick method; the energy tunnel parent removes EU periodically, and the dynamo tunnel parent scans and transfers through GT laser pipes. Perform only the server-side AE transfer with the current amperage:

```java
long limit = Math.multiplyExact(V[mTier], (long) Amperes);
```

The energy tunnel pulls into its local buffer; the dynamo tunnel pushes out of its local buffer. Both use the same simulate-then-modulate `EnergyTransfer` contract as Task 10. Override `canConnect(ForgeDirection)` to return false so the ME laser variants never attach to GT laser pipes; AE cable connection remains front-only through `MEHatchConnection`.

- [ ] **Step 3: Compile and verify inheritance deliberately**

Run: `./gradlew.bat compileJava`

Expected: laser hatches are assignable to the GT tunnel parents, but their tick implementation contains no `moveAround`, laser-pipe lookup, or direct tunnel-to-tunnel EU mutation.

Run: `rg "moveAround|getIGregTechTileEntityAtSideAndDistance|MTEPipeLaser" src/main/java/cn/dancingsnow/appeu/hatch`

Expected: no matches.

- [ ] **Step 4: Commit**

```bash
git add src/main/java/cn/dancingsnow/appeu/hatch/MTEHatchMEEnergyTunnel.java src/main/java/cn/dancingsnow/appeu/hatch/MTEHatchMEDynamoTunnel.java
git commit -m "feat: add ME laser energy hatches"
```

### Task 12: Add configurable conflict-checked MetaTileEntity registration

**Files:**
- Modify: `src/main/java/cn/dancingsnow/appeu/Config.java`
- Create: `src/main/java/cn/dancingsnow/appeu/registry/HatchRegistration.java`

- [ ] **Step 1: Replace the template config value**

```java
public static int metaTileEntityIdStart = 27_000;

metaTileEntityIdStart = configuration.getInt(
    "metaTileEntityIdStart",
    Configuration.CATEGORY_GENERAL,
    metaTileEntityIdStart,
    1,
    GregTechAPI.MAXIMUM_METATILE_IDS - 158,
    "First ID of the contiguous Applied Energistics EU Network hatch range");
```

Remove `greeting` and its log output.

- [ ] **Step 2: Validate the complete range before construction**

`HatchRegistration.registerAll(start)` first calls `HatchSpecs.create(start)` and validates all 158 specifications without constructing any MTE:

```java
int end = Math.addExact(start, specs.size() - 1);
if (start <= 0 || end >= GregTechAPI.MAXIMUM_METATILE_IDS) {
    throw new IllegalStateException("Invalid MetaTileEntity range " + start + ".." + end);
}
for (HatchSpec spec : specs) {
    if (GregTechAPI.METATILEENTITIES[spec.id()] != null) {
        throw new IllegalStateException("MetaTileEntity ID " + spec.id() + " conflicts with " + spec.name());
    }
}
```

Only after the entire pass succeeds, construct classes by `HatchFamily` and `HatchDirection`. Store each returned stack in an ordered immutable map keyed by generated name for later icons/tooltips and proxy visual representations.

- [ ] **Step 3: Keep laser-series registration one-call extensible**

Expose a separately usable registration method:

```java
public static void registerLaserSeries(int startId, int amperage) {
    registerSpecs(HatchSpecs.createLaserSeries(startId, amperage));
}
```

`registerSpecs` performs the same complete range, duplicate-name, and occupied-slot validation before constructing any member of that series. Default `registerAll` validates and registers `HatchSpecs.create(start, 256, 1024, 4096)` as one 158-entry transaction. A future `registerLaserSeries(nextFreeId, 16384)` call produces and registers exactly 18 IV-UXV energy/dynamo entries.

- [ ] **Step 4: Compile and commit**

Run: `./gradlew.bat compileJava`

Expected: registration compiles and the pure `HatchSpecsTest` still reports 158 default entries.

```bash
git add src/main/java/cn/dancingsnow/appeu/Config.java src/main/java/cn/dancingsnow/appeu/registry/HatchRegistration.java src/main/java/cn/dancingsnow/appeu/hatch/HatchSpecs.java
git commit -m "feat: register configurable ME hatch matrix"
```

### Task 13: Wire lifecycle, metadata, localization, and the recipe entry point

**Files:**
- Modify: `src/main/java/cn/dancingsnow/appeu/AppEU.java`
- Modify: `src/main/java/cn/dancingsnow/appeu/CommonProxy.java`
- Modify: `src/main/java/cn/dancingsnow/appeu/ClientProxy.java`
- Create: `src/main/java/cn/dancingsnow/appeu/registry/RecipeRegistration.java`
- Modify: `src/main/resources/mcmod.info`
- Create: `src/main/resources/assets/appeu/lang/en_US.lang`
- Create: `src/main/resources/assets/appeu/lang/zh_CN.lang`

- [ ] **Step 1: Set real mod identity and hard dependencies**

Use `name = "Applied Energistics: EU Network"`, keep mod ID `appeu`, and add required-after dependencies for `gregtech` and `appliedenergistics2` in `@Mod`. Update `mcmod.info` to the same name/description, project URL, author, and dependency IDs, and set `useDependencyInformation` true.

- [ ] **Step 2: Wire the exact registration order**

`CommonProxy.preInit` performs:

```java
Config.synchronizeConfiguration(event.getSuggestedConfigurationFile());
StorageRegistration.registerStackType();
ModItems.register();
HatchRegistration.registerAll(Config.metaTileEntityIdStart);
```

`CommonProxy.init` performs:

```java
StorageRegistration.registerCellHandler();
RecipeRegistration.register();
```

`RecipeRegistration.register()` has an intentionally empty body and no recipe calls in this release. Remove template greeting/version logs and unused server-starting behavior.

- [ ] **Step 3: Register the client stack icon**

`ClientProxy.preInit` calls `super.preInit(event)` and registers itself on `MinecraftForge.EVENT_BUS`. Its client-only texture stitch handler registers the stack icon on the item texture atlas:

```java
@SubscribeEvent
public void onTextureStitch(TextureStitchEvent.Pre event) {
    if (event.map.getTextureType() == 1) {
        EUStackType.INSTANCE.registerIcon(event.map);
    }
}
```

`EUStackType.registerIcon(IIconRegister)` stores `registerIcon("appeu:eu_stack")`; `getButtonIcon()` returns that stored icon.

- [ ] **Step 4: Add complete locale entries**

Both language files contain:

- `appeu.stack.eu`;
- eight component names and eight cell names keyed by metadata suffix;
- generated hatch names for direction/tier/amperage through a stable formatted localization scheme;
- tooltip lines for voltage, amperage, throughput, capacity, channel use, AE idle use, network-to-machine direction, and machine-to-network direction.

The English stack name is `EU Energy`; the Simplified Chinese stack name is `EU 能源`. Energy hatches use `ME Energy Hatch`/`ME 能源仓`; dynamo hatches use `ME Dynamo Hatch`/`ME 动力仓`; 256A and higher names include `Laser`/`激光`.

- [ ] **Step 5: Process resources and parse metadata**

Run: `./gradlew.bat processResources`

Expected: resource processing succeeds.

Run: `Get-Content -Raw -Encoding UTF8 src/main/resources/mcmod.info | ConvertFrom-Json | Out-Null`

Expected: command exits successfully.

- [ ] **Step 6: Commit**

```bash
git add src/main/java/cn/dancingsnow/appeu/AppEU.java src/main/java/cn/dancingsnow/appeu/CommonProxy.java src/main/java/cn/dancingsnow/appeu/ClientProxy.java src/main/java/cn/dancingsnow/appeu/registry/RecipeRegistration.java src/main/resources/mcmod.info src/main/resources/assets/appeu/lang
git commit -m "feat: wire mod lifecycle and localization"
```

### Task 14: Add storage and EU presentation textures

**Files:**
- Create: sixteen PNGs under `src/main/resources/assets/appeu/textures/items/` named `eu_storage_component_<suffix>.png` and `eu_storage_cell_<suffix>.png`
- Create: `src/main/resources/assets/appeu/textures/items/eu_stack.png`
- Create: `src/main/resources/assets/appeu/textures/gui/eu_stack.png`

- [ ] **Step 1: Create the texture set**

Create 16x16 transparent item textures for all eight suffixes: `1k`, `4k`, `16k`, `64k`, `256k`, `1024k`, `4096k`, `16384k`. Components and finished cells must be visually distinct while retaining a consistent EU lightning/energy motif. Create a 16x16 `eu_stack.png` item icon and a 16x16 transparent GUI texture returned by `EUStackType.getButtonTexture()`.

- [ ] **Step 2: Verify every referenced texture exists and is a valid PNG**

Run: `./gradlew.bat processResources`

Expected: all assets copy into `build/resources/main/assets/appeu/textures`.

Run this PowerShell validation:

```powershell
$suffixes = '1k','4k','16k','64k','256k','1024k','4096k','16384k'
$paths = foreach ($suffix in $suffixes) {
    "src/main/resources/assets/appeu/textures/items/eu_storage_component_$suffix.png"
    "src/main/resources/assets/appeu/textures/items/eu_storage_cell_$suffix.png"
}
$paths += 'src/main/resources/assets/appeu/textures/items/eu_stack.png'
$paths += 'src/main/resources/assets/appeu/textures/gui/eu_stack.png'
if (($paths | Where-Object { -not (Test-Path $_) }).Count) { throw 'Missing texture' }
```

Expected: command exits successfully.

- [ ] **Step 3: Commit**

```bash
git add src/main/resources/assets/appeu/textures
git commit -m "feat: add EU storage textures"
```

### Task 15: Write the registry-dependent manual game-test checklist

**Files:**
- Create: `docs/manual-test-checklist.md`

- [ ] **Step 1: Write exact setup and acceptance checks**

Document the required test world setup and a result checkbox for every item below:

1. Start with IDs `27000..27157`, then deliberately occupy one ID and confirm startup aborts with the conflicting ID/specification.
2. Confirm 158 hatch entries in NEI: 104 standard/multi and 54 laser, with LV-UXV and IV-UXV boundaries.
3. Insert every EU cell into ME Drive and ME Chest; verify status lights, idle drain, capacity, disassembly metadata, save/reload, and removal.
4. Verify the all-type terminal shows a single EU identity, icon, amount, and `EU` unit without a dedicated terminal.
5. For every hatch family, verify the AE cable connects only on the outward front, consumes one channel, and stops transfer when inactive.
6. Measure 2A/4A/16A/64A energy and dynamo throughput at representative LV, IV, and UXV tiers and compare against `V[tier] * amperage`.
7. Measure all standard, multi, and laser local buffers against the confirmed formulas.
8. Verify partial-cell and full-cell transfers conserve EU and leave rejected EU in the dynamo buffer.
9. Verify disconnect/reconnect, chunk unload/reload, and world restart do not duplicate or delete EU.
10. Verify `HatchElement.Energy`, `MultiAmpEnergy`, `Dynamo`, `ExoticDynamo`, `ExoticEnergy`, and `LaserSource` accept or reject the exact confirmed classes.
11. Verify a multiblock accepting only ordinary energy hatches rejects every 256A+ ME laser energy hatch.
12. Verify ME laser hatches never connect to or transfer through GT laser pipes, while screwdriver amperage survives save/reload.

- [ ] **Step 2: Commit**

```bash
git add docs/manual-test-checklist.md
git commit -m "docs: add in-game verification checklist"
```

### Task 16: Run final pure verification and review the exact delta

**Files:**
- Modify only files required to resolve failures found by the commands below.

- [ ] **Step 1: Run all pure tests**

Run: `./gradlew.bat test`

Expected: all pure logic tests pass without initializing FML, Forge registries, AE registries, or GT MetaTileEntities.

- [ ] **Step 2: Run compilation and resource verification**

Run: `./gradlew.bat compileJava processResources`

Expected: both tasks succeed.

- [ ] **Step 3: Run the full build**

Run: `./gradlew.bat build`

Expected: build succeeds and produces the mod JAR.

- [ ] **Step 4: Audit scope and prohibited behavior**

Run: `rg "runClient|runServer|GameRegistry|AEStackTypeRegistry.register|METATILEENTITIES" src/test`

Expected: no matches.

Run: `rg "moveAround|getIGregTechTileEntityAtSideAndDistance|MTEPipeLaser" src/main/java/cn/dancingsnow/appeu/hatch`

Expected: no matches.

Run: `git diff --check`

Expected: no whitespace errors.

- [ ] **Step 5: Review the final product matrix and working tree**

Run: `git status --short` and `git log --oneline --decorate -16`

Expected: only intentional implementation changes remain, and each task has its scoped commit. Do not launch a client or dedicated server automatically; complete the registry-dependent checks from `docs/manual-test-checklist.md` in the user's game instance.
