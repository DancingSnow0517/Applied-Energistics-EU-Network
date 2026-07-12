# Applied Energistics: EU Network Manual Test Checklist

Use a disposable test instance and world. Registry-collision tests are expected to abort startup, so do not use a
production instance or a world without a current backup.

## Test Environment

- Mod build/commit: ____________________
- Test date: ____________________
- Tester: ____________________
- Minecraft: 1.7.10
- Applied Energistics 2 Unofficial: rv3-beta-1000-GTNH
- GT5-Unofficial: 5.09.54.20
- Other installed mods and versions: ____________________
- World/save name: ____________________

Before testing:

1. Back up the test world and configuration directory.
2. Enable NEI and prepare representative GT multiblocks that accept each hatch element under test.
3. Prepare a powered, channel-enabled ME network with an ME terminal, ME Drive, ME Chest, and enough empty EU cells.
4. Prepare measurable EU sources and loads, a GT scanner or equivalent measurement tool, a screwdriver, and GT laser
   pipes.
5. Record the actual GT voltage `V[tier]` used in every throughput and buffer calculation.
6. Start each conservation test from recorded cell and hatch-buffer amounts. Do not infer conservation from machine
   activity alone.

## 1. MetaTileEntity ID Range And Conflict Failure

- [ ] PASS

Steps:

1. Set `metaTileEntityIdStart=27000` and start the game with all IDs `27000..27157` free.
2. Confirm startup completes and the log reports 158 registered ME energy hatches starting at ID 27000.
3. Close the game. In a disposable profile, use a test fixture or another addon to occupy one ID inside
   `27000..27157` before this mod registers its hatches.
4. Start the game again and capture the first registration error.
5. Remove the deliberate conflict and restore the original configuration before continuing.

Expected result:

- The conflict-free run owns exactly the contiguous range `27000..27157`.
- The conflicting run aborts registration before entering a world. The error identifies the occupied ID and the
  generated hatch specification/name that attempted to use it.
- No later ID in this mod's range is silently substituted and no conflicting machine is replaced.

## 2. NEI Product Matrix And Boundaries

- [ ] PASS

Steps:

1. Search NEI for this mod's ME energy and dynamo hatches.
2. Count standard/multi-amp entries separately from laser entries.
3. Check both energy and dynamo directions at every tier and amperage boundary.
4. Inspect representative item names and overlays at the first and last tier of each family.

Expected result:

- NEI contains exactly 158 hatches: 104 standard/multi-amp and 54 laser.
- Standard/multi-amp hatches cover LV through UXV at `2A`, `4A`, `16A`, and `64A`, in both directions.
- Laser hatches cover IV through UXV at `256A`, `1024A`, and `4096A`, in both directions; no LV-EV laser variant exists.
- Every entry has the correct tier, amperage, direction, localized name, and matching standard/multi/laser overlay.

## 3. Eight EU Cells In ME Drive And ME Chest

- [ ] PASS

Test every suffix: `1k`, `4k`, `16k`, `64k`, `256k`, `1024k`, `4096k`, and `16384k`.

Steps:

1. Insert each empty cell into an ME Drive, then repeat with an ME Chest.
2. For each host, verify recognition, status lights, reported capacity, one-type behavior, and the tier's expected idle
   drain.
3. Store a distinct nonzero EU amount in each tier and record it.
4. Save and exit, reload the world, and verify every amount and drive/chest status.
5. Remove and reinsert each cell in both host types and verify its amount remains on the ItemStack.
6. Empty each cell, disassemble it, and verify the returned storage component preserves the original tier metadata.

Expected result:

- All eight cells work in both standard ME Drives and ME Chests, with no invalid-cell fallback.
- Capacity and idle drain match the selected tier; status lights change consistently with empty/used/full state.
- Save/reload, removal, reinsertion, and movement between Drive and Chest preserve EU exactly.
- Disassembly is allowed only when empty and returns the matching component tier plus the normal housing/upgrades.

## 4. Unified Terminal Presentation

- [ ] PASS

Steps:

1. Store EU in one or more EU cells and open AE2's existing all-type terminal.
2. Change the stored amount, close and reopen the terminal, and observe the entry.
3. Search NEI and the creative inventory for any dedicated EU terminal block or item.

Expected result:

- The unified terminal shows exactly one EU identity, regardless of how many cells contain EU.
- The entry has the EU icon, the current aggregate amount, and the display unit `EU`.
- The amount updates with storage changes and no duplicate/fuzzy variants appear.
- No dedicated EU terminal, terminal GUI, or terminal recipe exists.

## 5. Front-Only AE Cable, Channel Use, And Inactive Network

- [ ] PASS

Repeat with standard energy, multi-amp energy, laser energy, standard dynamo, multi-amp dynamo, and laser dynamo
hatches.

Steps:

1. Place an AE cable against each face in turn and rotate the hatch front where necessary.
2. Observe cable connection and channel usage with the network powered and channel capacity available.
3. Run a transfer, then make the node inactive by removing power or channel capacity without changing the GT side.
4. Restore the network and confirm transfer resumes.

Expected result:

- An AE cable connects only to the hatch's outward-facing front; all other faces reject it.
- Each connected hatch consumes exactly one AE channel and 1 AE/t idle power.
- An inactive or channel-starved node transfers zero EU and leaves the local buffer unchanged.
- Transfer resumes without loss or duplication when the same node becomes active again.

## 6. Fixed-Amperage Throughput At LV, IV, And UXV

- [ ] PASS

Steps:

1. At LV, IV, and UXV, test `2A`, `4A`, `16A`, and `64A` in both energy and dynamo directions.
2. Use the 2A standard energy class, 4A/16A/64A multi-amp energy classes, 2A/4A standard dynamo classes, and 16A/64A
   multi-amp dynamo classes.
3. Keep both source and destination able to sustain the requested rate, then measure EU moved during a known number of
   ticks.
4. Repeat with less EU available or less storage capacity to confirm the transfer is limited by the actual amount.

Expected result:

- For every tier/amperage/direction combination, the maximum transfer per tick is exactly `V[tier] * amperage` EU.
- No hatch exceeds its declared limit, and partial availability/capacity transfers only the amount actually available.
- Energy hatches move network EU into their local buffer; dynamo hatches move local-buffer EU into the network.

## 7. Local Buffer Capacity Formulas

- [ ] PASS

Steps:

1. Measure the maximum local buffer of representative hatches at LV, IV, and UXV where that family exists.
2. Compare each measured capacity against the formula below using the exact GT voltage table.
3. For laser hatches, change the current amperage with a screwdriver and repeat at `1A`, an intermediate value, and the
   registered maximum.

Expected result:

| Hatch family | Expected local buffer |
| --- | --- |
| Standard 2A ME energy | `512 + 8 * V[tier]` |
| Standard 2A ME dynamo | `512 + 2 * V[tier + 1]` |
| Standard 4A ME dynamo | `512 + 16 * V[tier]` |
| Multi-amp ME energy/dynamo | `512 + 4 * V[tier] * amperage` |
| Laser ME energy/dynamo | `24 * V[tier] * currentAmperes` |

- Reported and observed capacities match exactly, including the standard-dynamo tier offset.
- Laser capacity follows the current screwdriver-selected amperage, not merely the registered maximum.

## 8. Partial And Full-Cell Conservation

- [ ] PASS

Steps:

1. Record `network EU + hatch buffer EU` before each transfer.
2. For an energy hatch, make the network contain less EU than one tick's requested amount and measure the partial pull.
3. For a dynamo hatch, leave less free cell capacity than one tick's offer and measure the partial push.
4. Fill all EU storage completely, place EU in a dynamo buffer, and run several transfer ticks.
5. Empty the network, leave free room in an energy hatch buffer, and run several transfer ticks.

Expected result:

- Every partial transfer changes the hatch buffer by exactly the amount actually removed from or accepted by storage.
- `network EU + hatch buffer EU` is identical before and after each test.
- A full network accepts zero EU; all rejected EU remains in the dynamo buffer and the producing multiblock observes the
  normal full-buffer behavior.
- An empty network supplies zero EU and the energy hatch does not invent energy.

## 9. Disconnect, Chunk Reload, And World Restart Conservation

- [ ] PASS

Steps:

1. Record EU cell contents and local hatch buffers while both an energy and a dynamo setup are idle.
2. Disconnect the AE cable during attempted transfer, wait, reconnect it, and record all amounts.
3. Unload the chunks containing the network and hatches, reload them, and record all amounts again.
4. Save and exit completely, restart the game, reload the world, and repeat transfers in both directions.

Expected result:

- Disconnecting stops transfer without clearing buffers; reconnecting creates no catch-up duplication.
- Chunk unload/reload and a full world restart preserve every cell amount and hatch-buffer amount exactly.
- Across every transition, total EU is conserved and each AE node reconnects with one channel on the hatch front.

## 10. HatchElement Acceptance Matrix

- [ ] PASS

For each table cell, form a representative multiblock whose hatch requirement contains only the row's element. Confirm
that `Accept` hatches are added to the machine and allow formation, while `Reject` hatches are not added and cannot
satisfy that requirement.

| HatchElement | ME energy (2A standard) | ME energy (4/16/64A multi) | ME energy (256A+ laser) | ME dynamo (2/4A standard) | ME dynamo (16/64A multi) | ME dynamo (256A+ laser) |
| --- | --- | --- | --- | --- | --- | --- |
| `Energy` | Accept | Reject | Reject | Reject | Reject | Reject |
| `MultiAmpEnergy` | Reject | Accept | Reject | Reject | Reject | Reject |
| `Dynamo` | Reject | Reject | Reject | Accept | Reject | Reject |
| `ExoticDynamo` | Reject | Reject | Reject | Reject | Accept | Accept |
| `ExoticEnergy` | Reject | Accept | Accept | Reject | Reject | Reject |
| `LaserSource` | Reject | Reject | Reject | Reject | Reject | Accept |

Expected result:

- All 36 acceptance/rejection checks match the table exactly.
- A rejected hatch neither forms the machine through the tested element nor appears in that element's runtime hatch
  list.

## 11. Ordinary-Energy-Only Multiblock Rejects Laser Energy Hatches

- [ ] PASS

Steps:

1. Use a multiblock whose structure accepts ordinary `HatchElement.Energy` but no exotic or laser energy element.
2. Attempt formation with every ME laser energy hatch: IV through UXV at `256A`, `1024A`, and `4096A`.
3. Replace the laser hatch with a valid ordinary 2A ME energy hatch as a control.

Expected result:

- All 27 laser energy variants are rejected and cannot satisfy the ordinary energy-hatch requirement.
- The control ordinary ME energy hatch is accepted, proving rejection is caused by hatch classification rather than the
  test structure.

## 12. Laser-Pipe Isolation And Screwdriver Amperage Persistence

- [ ] PASS

Repeat with both ME laser energy and ME laser dynamo hatches at representative IV and UXV tiers.

Steps:

1. Place GT laser pipes against the front and every other face; also place a compatible GT laser endpoint beyond the
   pipe.
2. Fill the source side, run several ticks, and record pipe/endpoint/hatch amounts.
3. Connect the hatch front to AE normally and confirm AE transfer still works.
4. Use a screwdriver to select `1A`, an intermediate current, and the registered maximum. At each setting, measure the
   throughput and local buffer.
5. Save and reload the world, then perform a full game restart and check the selected amperage again.

Expected result:

- ME laser hatches never connect to GT laser pipes and never send or receive EU through them on any face.
- AE cable connection and AE-network transfer remain front-only and functional.
- Screwdriver selection is clamped to `1..registered maximum`; throughput is `V[tier] * currentAmperes` and buffer is
  `24 * V[tier] * currentAmperes`.
- The selected amperage survives chunk reload, world reload, and full game restart without resetting to the maximum.

## Final Result

- [ ] All 12 sections passed
- [ ] Logs, measurements, and screenshots are attached to the test record
- [ ] The test configuration was restored and the disposable conflict fixture was removed

Notes and failures:

________________________________________________________________________________

________________________________________________________________________________
