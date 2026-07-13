# EU Electric Item Container Design

## Goal

Allow players to charge and discharge compatible GT/IC2 electric items directly from the AE terminal through the custom EU stack type. A terminal fill action moves EU from the network into the held item, and a terminal drain action moves stored EU from the item into the network.

## Scope

The change is limited to the electric-item container behavior owned by `EUStackType` and focused tests for that behavior. It does not add a dedicated terminal, alter AE2 terminal code, introduce voltage loss, or change hatch and cell storage behavior.

Supported containers are non-stackable electric items recognized by `GTModHandler.isElectricItem`, including rechargeable batteries, tools, and armor. Requiring a maximum stack size of one avoids AE2's batch-container path for consumable single-use batteries, whose successful discharge can consume the item rather than produce an empty reusable container.

## API Strategy

`GTModHandler` remains the source of truth for electric-item recognition and charge metadata:

- `GTModHandler.isElectricItem` identifies supported electric items.
- `GTModHandler.getElectricItemCharge` provides `[currentCharge, maximumCharge]` as `long` values and handles GT `MetaBaseItem` charge storage correctly.

Mutation uses `ElectricItem.manager.charge` and `ElectricItem.manager.discharge`. The corresponding `GTModHandler` mutation methods accept only `int`, while the installed GT version defines batteries with capacities up to `Long.MAX_VALUE`. Repeated `int` transfers could therefore require billions of calls.

Each manager mutation is limited to `2^53` EU. Every integer through `2^53` is exactly representable by the manager's `double` parameter. At most 1024 chunks are required to traverse the full positive `long` range. The implementation re-reads charge through `GTModHandler.getElectricItemCharge` after mutation and reports the observed state delta instead of trusting the manager's floating-point return value.

Terminal container operations ignore tier and per-item transfer limits. The AE network stores abstract EU rather than applying machine voltage, and a terminal action is expected to transfer the selected amount immediately. Explicit terminal draining also ignores `canProvideEnergy`, allowing charged tools and armor to return their EU to the network.

## Container Contract

`EUStackType` will implement the complete `IAEStackType` container contract:

- `isContainerItemForType` returns true only for a non-null, non-stackable electric item with valid charge metadata.
- `getStackFromContainerItem` returns an `EUStack` containing the current positive charge. Empty or invalid containers return null.
- `getContainerItemCapacity` returns the non-negative maximum charge for a valid electric item and zero otherwise.
- `fillContainer` charges a one-item copy by at most the offered EU and available capacity, then returns the updated copy and observed EU increase.
- `drainStackFromContainer` discharges a one-item copy by at most the requested EU and current charge, then returns the updated copy and observed EU decrease.
- `clearFilledContainer` discharges all current charge from a one-item copy and returns that copy. Invalid containers return null.
- `convertStackFromItem` remains unsupported because electric items are containers, not display-item conversions.

The methods do not mutate the caller's stack. Item identity, metadata, and unrelated NBT are preserved by copying before transfer. Returned transfer amounts are clamped to the requested amount and observed charge range so AE2's simulate-then-modulate terminal flow cannot duplicate or delete EU when an item manager reports unexpected values.

## Failure Handling

Invalid metadata, non-positive capacities, empty items, rejected charge operations, and rejected discharge operations produce a zero transfer without changing the caller's item. Arithmetic uses bounded subtraction and minimum operations rather than unchecked addition, so `Long.MAX_VALUE` capacity and request values cannot overflow.

If a manager makes no progress during a chunk, the transfer loop stops immediately. A manager that changes charge in an unexpected direction also stops the operation, and only a valid observed delta is returned.

## Testing

Focused tests will cover:

- null, ordinary items, stackable electric items, and valid non-stackable electric items;
- empty, partially charged, and full containers;
- charging and discharging with requests smaller and larger than the available amount;
- charge rejection and discharge rejection behavior;
- preservation of item metadata and unrelated NBT;
- exact reported deltas and caller-stack immutability;
- chunk-boundary and long-capacity arithmetic.

The implementation will follow a red-green-refactor cycle. Verification will run the targeted storage tests first, then the full test suite, IDEA project compilation as required by the repository tooling, and `git diff --check`.
