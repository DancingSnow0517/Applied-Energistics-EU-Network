# Signed EU Delta Compatibility Design

## Goal

Restore ME energy/dynamo hatch transfers and remove their exception-driven tick cost by making `EUStack` compatible
with AE2's signed storage-change notifications.

## Root Cause

AE2's `NetworkMonitor` performs a modulated extraction before notifying listeners. For an extraction, it copies the
requested stack and changes its amount to the negative extracted quantity. This signed stack is a transient delta used
by terminal listeners, storage events, and item-flow accounting.

`EUStack.setStackSize()` currently rejects negative values. A successful network extraction therefore reaches AE2's
notification path, throws while constructing the negative delta, and never returns to the hatch. Network EU has already
been removed, but the hatch never increments `mStoredEnergy`. The same exception repeats every server tick and prevents
the later active-state update, explaining both the zero local buffer and excessive CPU time.

## Stack Semantics

Align `EUStack` with AE2's native `AEStack` contract:

- constructors and `setStackSize` accept the full signed `long` range;
- `isMeaningful` is true for every nonzero amount, including negative change records;
- `incStackSize`, `decStackSize`, and `add` accept signed operands and retain checked `long` arithmetic;
- zero remains the only empty/non-meaningful value;
- NBT and packet codecs round-trip signed values because AE2 may transport transient deltas through generic codecs.

Identity, requestable/crafting state, icons, display names, and stack-type behavior remain unchanged.

## Stored-Energy Boundary

Signed values are an AE bookkeeping representation, not valid stored EU. No cell-capacity or hatch-buffer rule changes:

- AE2's `CellInventory.loadCellStacks()` only inserts records whose persisted count is positive;
- cell injection and extraction continue to operate on positive requested/offered quantities;
- `EnergyTransfer` continues to clamp external monitor results to positive requested limits;
- GT hatch buffers continue to reject or ignore negative local values through their existing transfer preconditions.

`EUStackList` must aggregate signed deltas like AE2's native item list: adding a negative record reduces the existing
record, and an exact zero result is removed. A standalone negative record remains meaningful because notification lists
may legitimately contain a negative delta without a preceding positive record.

## Performance Scope

Do not add polling intervals, cached monitors, or skipped transfer ticks in this fix. `getMEMonitor(type)` is a map lookup;
the observed extreme cost is the repeated exception after modulated extraction. Transfer remains available every tick at
the configured `V[tier] * amperage` throughput. If a fresh build still profiles poorly after this correction, collect a
new trace and optimize that independent bottleneck.

## Verification

Pure logic regression tests will prove:

- negative stack construction and `setStackSize` work;
- signed increment/decrement and addition work;
- overflow and underflow still throw;
- negative NBT and packet values round-trip;
- `EUStackList` applies negative deltas and removes an exact-zero aggregate;
- a standalone negative delta remains iterable and meaningful.

Run the focused storage tests, then all tests, IDEA compilation, `compileJava processResources`, and the full build.

Game verification must confirm that an active energy hatch increases `mStoredEnergy`, network EU decreases by the same
amount, dynamo transfer works in the opposite direction, machine active state updates, and per-hatch CPU time no longer
shows the repeated-exception spike. Existing conservation checks remain mandatory because affected older builds may have
removed EU before throwing.
