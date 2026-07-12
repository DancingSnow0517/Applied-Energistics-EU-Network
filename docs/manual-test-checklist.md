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

- [ ] Pass  [ ] Fail  [ ] Blocked

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

Evidence / notes: ________________________________________________________________________________

## 2. NEI Product Matrix And Boundaries

- [ ] Pass  [ ] Fail  [ ] Blocked

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

Evidence / notes: ________________________________________________________________________________

## 3. Eight EU Cells In ME Drive And ME Chest

- [ ] Pass  [ ] Fail  [ ] Blocked

Test every suffix: `1k`, `4k`, `16k`, `64k`, `256k`, `1024k`, `4096k`, and `16384k`.

With `EU_PER_BYTE = 1024 * 1024`, use these exact acceptance values:

| Suffix | Capacity (EU) | Idle drain (AE/t) |
| --- | ---: | ---: |
| `1k` | 1,073,741,824 | 0.5 |
| `4k` | 4,294,967,296 | 1.0 |
| `16k` | 17,179,869,184 | 1.5 |
| `64k` | 68,719,476,736 | 2.0 |
| `256k` | 274,877,906,944 | 2.5 |
| `1024k` | 1,099,511,627,776 | 3.0 |
| `4096k` | 4,398,046,511,104 | 3.5 |
| `16384k` | 17,592,186,044,416 | 4.0 |

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

Evidence / notes: ________________________________________________________________________________

## 4. Unified Terminal Presentation

- [ ] Pass  [ ] Fail  [ ] Blocked

Steps:

1. Store EU in one or more EU cells and open AE2's existing all-type terminal.
2. Change the stored amount, close and reopen the terminal, and observe the entry.
3. Search NEI and the creative inventory for any dedicated EU terminal block or item.

Expected result:

- The unified terminal shows exactly one EU identity, regardless of how many cells contain EU.
- The entry has the EU icon, the current aggregate amount, and the display unit `EU`.
- The amount updates with storage changes and no duplicate/fuzzy variants appear.
- No dedicated EU terminal, terminal GUI, or terminal recipe exists.

Evidence / notes: ________________________________________________________________________________

## 5. Front-Only AE Cable, Channel Use, And Inactive Network

- [ ] Pass  [ ] Fail  [ ] Blocked

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

Evidence / notes: ________________________________________________________________________________

## 6. Fixed-Amperage Throughput At LV, IV, And UXV

- [ ] Pass  [ ] Fail  [ ] Blocked

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

Evidence / notes: ________________________________________________________________________________

## 7. Local Buffer Capacity Formulas

- [ ] Pass  [ ] Fail  [ ] Blocked

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

Evidence / notes: ________________________________________________________________________________

## 8. Partial And Full-Cell Conservation

- [ ] Pass  [ ] Fail  [ ] Blocked

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

Evidence / notes: ________________________________________________________________________________

## 9. Disconnect, Chunk Reload, And World Restart Conservation

- [ ] Pass  [ ] Fail  [ ] Blocked

Steps:

1. Record EU cell contents and local hatch buffers while both an energy and a dynamo setup are idle.
2. Disconnect the AE cable during attempted transfer, wait, reconnect it, and record all amounts.
3. Unload the chunks containing the network and hatches, reload them, and record all amounts again.
4. Save and exit completely, restart the game, reload the world, and repeat transfers in both directions.

Expected result:

- Disconnecting stops transfer without clearing buffers; reconnecting creates no catch-up duplication.
- Chunk unload/reload and a full world restart preserve every cell amount and hatch-buffer amount exactly.
- Across every transition, total EU is conserved and each AE node reconnects with one channel on the hatch front.

Evidence / notes: ________________________________________________________________________________

## 10. HatchElement Acceptance Matrix

- [ ] Pass  [ ] Fail  [ ] Blocked

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

Evidence / notes: ________________________________________________________________________________

## 11. Ordinary-Energy-Only Multiblock Rejects Laser Energy Hatches

- [ ] Pass  [ ] Fail  [ ] Blocked

Steps:

1. Use a multiblock whose structure accepts ordinary `HatchElement.Energy` but no exotic or laser energy element.
2. Attempt formation with every ME laser energy hatch: IV through UXV at `256A`, `1024A`, and `4096A`.
3. Replace the laser hatch with a valid ordinary 2A ME energy hatch as a control.

Expected result:

- All 27 laser energy variants are rejected and cannot satisfy the ordinary energy-hatch requirement.
- The control ordinary ME energy hatch is accepted, proving rejection is caused by hatch classification rather than the
  test structure.

Evidence / notes: ________________________________________________________________________________

## 12. Laser-Pipe Isolation And Screwdriver Amperage Persistence

- [ ] Pass  [ ] Fail  [ ] Blocked

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

Evidence / notes: ________________________________________________________________________________

## 13. Terminal Presentation And Creative Tab Contents

- [ ] Pass  [ ] Fail  [ ] Blocked

Steps:

1. Open AE2's unified terminal with stored EU, hover its EU entry, and record the title in both English and Simplified
   Chinese.
2. At each AE2 terminal font-size setting, test stored amounts close to `1,000`, `1,000,000`, `1,000,000,000`, and
   `1,000,000,000,000`. Place native item stacks with equivalent displayed magnitudes beside the EU entry and compare
   their quantity formatting.
3. Open this mod's creative tab and count the storage components, EU cells, and default registered hatches separately.
   Record the first and last entry of each group and check the complete group ordering.
4. Browse every entry in this mod's creative tab and confirm the hidden EU Energy display item is absent from that tab.
5. In a disposable copy or branch, first confirm MetaTileEntity IDs `27158..27175` are free when the configured start is
   the default `27000`. In `CommonProxy.preInit`, immediately after the default `registerAll` call, temporarily add:

   ```java
   HatchRegistration.registerLaserSeries(Config.metaTileEntityIdStart + 158, 16_384);
   ```

6. Build a temporary test jar from that disposable copy or branch, start the game, and refresh or close and reopen the
   creative inventory. Recount the mod tab and check the added entries for duplicates.
7. After testing, remove the temporary call and restore the test configuration. This call is a manual-test fixture only;
   do not commit it to production code.

Expected result:

- The unified terminal hover title is `EU Energy` in English and `EU 能源` in Simplified Chinese. It never shows a
  storage-component name or title.
- At every AE2 terminal font-size setting, amounts around `1,000`, `1,000,000`, `1,000,000,000`, and
  `1,000,000,000,000` use `K`, `M`, `G`, and `T`, respectively, with the same value, rounding, spacing, and suffix
  behavior as native AE2 stacks.
- The unmodified mod tab contains exactly 8 storage components, followed by exactly 8 EU cells, followed by exactly
  158 default hatches. No entry appears outside its group, and the hidden EU Energy display item does not appear in this
  mod's creative tab.
- The temporary `registerLaserSeries` call runs during the correct GT registration phase. With the default start, it
  registers exactly 18 additional `16384A` laser hatches at IDs `27158..27175`. After refreshing or reopening the
  creative inventory, all 18 appear in the mod tab, and no component, cell, default hatch, or added hatch is duplicated.

Evidence / notes:

- English tooltip title: ____________________
- Simplified Chinese tooltip title: ____________________

| AE2 terminal font size | ~1,000 (`K`) | ~1,000,000 (`M`) | ~1,000,000,000 (`G`) | ~1,000,000,000,000 (`T`) | Matches native stacks? |
| --- | --- | --- | --- | --- | --- |
| ____________________ | ____________________ | ____________________ | ____________________ | ____________________ | ____________________ |
| ____________________ | ____________________ | ____________________ | ____________________ | ____________________ | ____________________ |
| ____________________ | ____________________ | ____________________ | ____________________ | ____________________ | ____________________ |

- Base component count: ________
- Component first entry: ____________________
- Component last entry: ____________________
- Base cell count: ________
- Cell first entry: ____________________
- Cell last entry: ____________________
- Base default-hatch count: ________
- Default hatch first entry: ____________________
- Default hatch last entry: ____________________
- Base group order (components/cells/hatches): ____________________
- Hidden EU Energy display item absent from mod tab: [ ] Yes  [ ] No
- Extra-series IDs: ____________________
- Extra-series amperage: ____________________
- Extra-series hatch count: ________
- Duplicate result: ____________________
- General notes: _________________________________________________________________________________

## 14. Signed EU Transfer Regression, Active State, And CPU Load

- [ ] Pass  [ ] Fail  [ ] Blocked

Steps:

1. Back up the test world before loading either build. Treat any world previously opened with an affected old build as
   potentially modified: the network may already have had EU deducted before the negative-amount exception occurred.
2. On the new build, prepare an active, channel-enabled ME network with a recorded nonzero EU amount. Connect one ME
   energy hatch with known tier, amperage, voltage, and free local-buffer capacity. Record network EU and
   `mStoredEnergy`, run exactly 20 ticks, then record both values again.
3. Prepare an ME dynamo hatch with a known nonzero local buffer and enough free capacity in an EU cell. Record network
   EU and `mStoredEnergy`, run exactly 20 ticks, then record both values again.
4. Switch each connected hatch between active and inactive network states by changing AE power or channel availability.
   Record how many ticks each state transition takes to appear on the hatch, allowing up to 20 ticks per transition.
5. After a warmup period, sample the same connected hatch for at least 100 ticks. Record average and worst CPU load and
   capture the first exception, if any, from the corresponding log interval.

Expected result:

- For the energy hatch, `buffer gain = network loss`. The transferred amount is no greater than
  `20 * V[tier] * A` or the free local-buffer capacity, whichever is smaller.
- For the dynamo hatch, `buffer loss = network gain`. The transferred amount is no greater than
  `20 * V[tier] * A` or the free EU-cell capacity, whichever is smaller.
- When the network contains EU and the energy hatch has free local-buffer capacity, `mStoredEnergy` no longer remains
  permanently at zero.
- The hatch active state follows its AE proxy within 20 ticks after both active and inactive transitions.
- The sampled log contains no `EU amount cannot be negative` exception.
- After warmup, the hatch no longer causes repeated exception-driven CPU spikes. Record measured CPU values without
  applying a hardware-independent pass/fail threshold.

Evidence / notes:

- Build/hash: ____________________
- Energy hatch tier / A / `V[tier]`: ____________________
- Energy network EU before / after: ____________________ / ____________________
- Energy buffer EU before / after: ____________________ / ____________________
- Energy calculated bound (`min(20 * V[tier] * A, free buffer)`): ____________________
- Energy conserved (`buffer gain = network loss`): [ ] Yes  [ ] No
- Dynamo hatch tier / A / `V[tier]`: ____________________
- Dynamo network EU before / after: ____________________ / ____________________
- Dynamo buffer EU before / after: ____________________ / ____________________
- Dynamo calculated bound (`min(20 * V[tier] * A, free cell capacity)`): ____________________
- Dynamo conserved (`buffer loss = network gain`): [ ] Yes  [ ] No
- Active transition latency (ticks): ____________________
- Inactive transition latency (ticks): ____________________
- CPU warmup ticks: ____________________
- CPU sample ticks (minimum 100): ____________________
- CPU average / worst: ____________________ / ____________________
- Exception log result / first exception: _________________________________________________________
- Notes: _________________________________________________________________________________________

## Final Result

- [ ] All 14 sections passed, including signed EU transfer, active-state, and CPU-load regression checks
- [ ] Logs, measurements, and screenshots are attached to the test record
- [ ] The test configuration was restored and the disposable conflict fixture was removed

Notes and failures:

________________________________________________________________________________

________________________________________________________________________________
