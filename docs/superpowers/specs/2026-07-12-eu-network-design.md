# Applied Energistics EU Network Design

## Scope

This mod adds GregTech EU as a native AE2 stack type, EU storage components and cells, and GregTech multiblock hatches that exchange EU with an AE storage network.

The initial release includes:

- One native `EUStackType` displayed by AE2's existing all-type terminal.
- EU storage components and storage cells at 1k, 4k, 16k, 64k, 256k, 1024k, 4096k, and 16384k.
- Standard ME energy and dynamo hatches from LV through UXV at 2A, 4A, 16A, and 64A.
- ME laser energy and dynamo hatches from IV through UXV at 256A, 1024A, and 4096A.
- English and Simplified Chinese localization and the required item and stack-type textures.
- A recipe registration entry point with no recipes registered in this release.

Dedicated EU terminals, recipes, GT laser-pipe transport, and automated Minecraft registry tests are outside this scope.

## Dependencies and Integration Boundary

The implementation targets:

- Applied Energistics 2 Unofficial `rv3-beta-1000-GTNH`
- GT5-Unofficial `5.09.54.20`
- Minecraft Forge 1.7.10

No AE2 or GregTech source is modified. The mod uses the public and addon-facing extension APIs present in these versions.

## Architecture

### EU Stack

The storage stack package contains:

- `EUStack`, an `IAEStack<EUStack>` implementation whose only meaningful state is a non-negative `long` EU amount.
- `EUStackType`, an `IAEStackType<EUStack>` singleton registered under the stable ID `appeu.eu` during pre-initialization.
- `EUStackList`, a single-value `IItemList<EUStack>` implementation.

All EU stacks have the same identity. Equality and same-type checks compare the stack type, not the amount. The stack does not support crafting, requestable amounts, fuzzy variants, item containers, or fluid containers. Unsupported conversion methods return the empty or unsupported result required by the AE2 API.

NBT and packet encoding store one `long` amount. Generic AE serialization adds the registered stack-type ID as usual. Arithmetic must reject negative states and avoid signed overflow.

The stack type supplies:

- Display name `EU Energy`
- Display unit `EU`
- A terminal button texture and a stack icon
- `getAmountPerUnit() == 1`
- `getAmountPerByte() == EU_PER_BYTE`

### EU Storage Cells

The cell package contains:

- `EUCellTier`, the source of truth for tier name, nominal KiB, total bytes, capacity, icon suffix, and idle drain.
- `EUCellInventory`, extending AE2's generic `CellInventory<EUStack>`.
- `EUCellInventoryHandler`, adapting the inventory to the AE storage APIs.
- `EUCellHandler`, registered with AE2's cell registry for drives and ME chests.

Cells use AE2's existing injection, extraction, simulation, priority, save notification, and drive status behavior. Cell contents remain in the cell ItemStack NBT.

Each cell has exactly one type slot and no per-type byte cost:

```text
getTotalTypes()   = 1
getBytesPerType() = 0
EU_PER_BYTE       = 1024L * 1024L
totalBytes        = nominalKiB * 1024L
capacityEU        = totalBytes * EU_PER_BYTE
```

The resulting minimum and maximum capacities are:

- 1k: 1,073,741,824 EU
- 16384k: 17,592,186,044,416 EU

The intermediate tiers follow the same four-times progression. Capacity calculations use checked `long` multiplication.

Cells accept only `EUStackType`, cannot be stored inside other storage cells, and expose no meaningful partitioning choices because EU has one identity. Idle drain starts at 0.5 AE/t for 1k and increases by 0.5 AE/t per tier, ending at 4.0 AE/t for 16384k.

### Items

Two metadata items provide all sixteen item variants:

- One item for the eight EU storage components.
- One item for the eight complete EU storage cells.

The complete cell implements `IStorageCell`, resolves its properties through `EUCellTier`, has a maximum stack size of one, and can be inserted into standard ME drives and ME chests.

The item loader, cell loader, stack-type loader, hatch loader, and recipe loader are separate registration units called from the mod lifecycle in the required order. The recipe loader is intentionally empty but provides the stable location for later recipe work.

## Hatch Design

### Product Matrix

Standard hatches:

```text
LV through UXV
x 2A, 4A, 16A, 64A
x energy and dynamo
= 104 MetaTileEntities
```

Laser hatches:

```text
IV through UXV
x 256A, 1024A, 4096A
x energy and dynamo
= 54 MetaTileEntities
```

The initial total is 158 MetaTileEntities.

Registration is specification-driven. A helper registers all IV-UXV laser hatches for one amperage, so a future higher-amperage laser series requires one additional registration call rather than new classes.

### AE Grid Connection

Every hatch owns an `AENetworkProxy` and implements the required AE grid host interfaces. The proxy:

- Requires one AE channel.
- Uses 1 AE/t idle power.
- Accepts an AE cable only on the hatch's outward-facing front.
- Persists its node state through hatch NBT.
- Follows the standard first-tick, unload, invalidate, and neighbor-change lifecycle used by GT's existing ME hatches.

The hatch transfers EU only on the logical server while the proxy is active. The front is not a normal GregTech EU input or output face.

### Energy Hatch Flow

An ME energy hatch extracts EU from the AE storage grid and fills its GregTech local energy buffer. Each server tick:

1. Compute the local buffer deficit.
2. Limit the request to `V[tier] * currentAmperage`.
3. Simulate extraction from the AE storage grid using `EUStackType`.
4. Modulate only the amount confirmed available.
5. Add only the amount actually extracted to the local buffer.

The owning GregTech multiblock consumes the local buffer through the inherited hatch behavior.

### Dynamo Hatch Flow

An ME dynamo hatch receives EU from its owning GregTech multiblock in its local buffer and injects it into the AE storage grid. Each server tick:

1. Limit the offer to the local buffer amount and `V[tier] * currentAmperage`.
2. Simulate injection into the AE storage grid.
3. Modulate only the amount the grid can accept.
4. Remove only the amount actually accepted from the local buffer.

If the grid is inactive or full, EU remains in the hatch buffer. The owning multiblock then observes the same full-buffer behavior as it would with the corresponding GregTech hatch. EU is never voided to keep the machine running.

### Throughput and Buffer Capacity

Per-tick throughput is `V[tier] * currentAmperage`.

The buffer formulas match the corresponding GT classes:

- Standard 2A energy hatch: `512 + V[tier] * 8`
- Standard 2A dynamo hatch: `512 + V[tier + 1] * 2`
- 4A, 16A, and 64A multi hatches: `512 + V[tier] * 4 * amperage`
- Laser hatches: `V[tier] * 24 * amperage`

Standard hatch amperage is fixed by the registered variant. Laser hatches retain GregTech's adjustable current amperage from 1A through their registered maximum.

### HatchElement Compatibility

Compatibility is defined by the actual `HatchElement.*` adders in the target GT5U version:

- 2A ME energy hatches match `HatchElement.Energy`.
- 4A, 16A, and 64A ME energy hatches match `HatchElement.MultiAmpEnergy` and report hatch type 1.
- 256A and higher ME laser energy hatches report hatch type 2, do not match `MultiAmpEnergy`, and use `ExoticEnergy`.
- 2A and 4A ME dynamo hatches can match `HatchElement.Dynamo`, whose adder permits at most 4A.
- 16A and higher ME dynamo hatches use `HatchElement.ExoticDynamo`.
- ME laser dynamo hatches retain the native tunnel classification used by `LaserSource` and `ExoticDynamo`.

Laser ME hatches extend the relevant GT tunnel classes to preserve classification, overlays, and amperage controls. They override the tunnel `onPostTick` behavior so they exchange energy only with the attached AE network and never search for or transfer through GT laser pipes. A multiblock that does not accept the appropriate exotic HatchElement therefore rejects the 256A and higher ME hatches.

## Registration and Configuration

`metaTileEntityIdStart` is configurable and defaults to 27000. The complete range is contiguous and derived from the ordered hatch specifications.

Before constructing any MetaTileEntity, registration validates:

- The start ID is positive.
- The final ID is below `GregTechAPI.MAXIMUM_METATILE_IDS`.
- Every target slot in `GregTechAPI.METATILEENTITIES` is empty.
- No generated internal name is duplicated.

Any failure aborts registration with a message containing the conflicting ID and hatch specification. The default is not treated as universally conflict-free; modpack authors can relocate the range with the configuration option.

`EUStackType` registration also checks for an existing `appeu.eu` type and fails on an incompatible duplicate instead of silently replacing it.

## Resources and Presentation

The mod supplies:

- `en_US.lang`
- `zh_CN.lang`
- Eight EU storage component icons
- Eight EU storage cell icons
- EU stack and terminal button textures

Hatches reuse GregTech's voltage-tier, amperage, and laser overlays. Tooltips show tier, voltage, amperage, EU/t throughput, local buffer capacity, AE channel requirement, and the direction of transfer.

The existing AE2 all-type terminal displays EU through `EUStackType`; no dedicated terminal is added.

## Error Handling and Transaction Safety

Grid access failures, inactive nodes, and missing storage caches are no-op conditions for that tick. They do not clear local energy.

All transfer code uses simulate-then-modulate operations. The modulated result is the source of truth because network contents can change between simulation and mutation. Buffer updates use checked bounds and never assume the simulated amount was fully transferred.

Cell loading rejects malformed negative amounts. Unknown stack-type IDs are left to AE2's normal generic deserialization handling. Registration errors are fatal because continuing would risk world corruption or replacing another mod's machine.

## Automated Tests

Automated tests are pure logic tests and must not initialize Minecraft, FML registries, `GameRegistry`, AE global registries, or the GregTech MetaTileEntity registry.

Coverage includes:

- All tier byte and EU capacity calculations.
- Checked arithmetic and `long` boundaries.
- EU stack copy, add, subtract, reset, equality, NBT payload, and ByteBuf payload logic.
- Single-value list aggregation and lookup.
- Cell capacity accounting without type overhead.
- A storage-port abstraction for hatch transfer algorithms.
- Partial extraction and partial injection.
- Empty source, full destination, full local buffer, inactive storage, and no-capacity cases.
- Conservation of EU across every transfer result.
- Pure hatch specification generation: matrix size, order, generated names, tier range, amperage range, ID offsets, and target HatchElement category.

Production classes that directly trigger Minecraft registration are not instantiated by tests.

## Manual In-Game Verification

The following checks require a user-started game instance:

1. The game starts with the configured 158-ID range and reports no registry collision.
2. All hatch variants appear with the correct names, tiers, amperages, and overlays in NEI.
3. All eight EU cells enter standard ME drives and ME chests.
4. Drive status lights, cell tooltip capacity, save/reload, and cell removal preserve the correct EU amount.
5. EU appears in the unified all-type terminal with the correct icon and unit.
6. Each hatch connects an ME cable only on its outward front and consumes one channel.
7. Energy hatches supply their owning multiblock at the declared throughput and buffer capacity.
8. Dynamo hatches store generated multiblock energy in EU cells without loss.
9. Disconnecting the AE network or filling all EU cells preserves local buffered energy.
10. Multiblocks limited to standard energy hatches reject 256A and higher ME laser energy hatches.
11. Multiblocks accepting exotic or laser hatches accept the corresponding ME laser variants.
12. Chunk unload/reload and world restart do not duplicate or delete EU.

## Build Verification

Implementation completion requires:

- `compileJava`
- Pure logic test suite
- `processResources`
- Full `build`
- `git diff --check`

Automated client or dedicated-server launch is not a completion requirement because this project is susceptible to registry collisions during test launches. Registry-dependent behavior remains in the explicit manual verification list above.
