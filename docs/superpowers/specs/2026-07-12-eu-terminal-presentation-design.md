# EU Terminal Presentation And Creative Tab Design

## Goal

Correct EU presentation in AE2's unified terminal and add an Applied Energistics: EU Network creative tab containing
the mod's usable items and every registered ME hatch.

## Current Behavior And Root Cause

`EUStack.getItemStackForNEI()` returns the 1k EU storage component. AE2 uses that representative `ItemStack` for
hovered-stack tooltips, so the terminal identifies stored EU as the 1k component.

`EUStack.drawOverlayInGui()` converts the amount with `Long.toString`. This bypasses AE2's `StackSizeRenderer`, so the
terminal renders the full raw number instead of its configured K/M/G/T-style representation.

## Representative Display Item

Register one dedicated item whose only purpose is to represent EU to APIs that require an `ItemStack`:

- registry name: `eu_energy_display`;
- localized name: `EU Energy` / `EU 能源`;
- texture: the existing `appeu:eu_stack` item texture;
- no creative tab, recipes, storage behavior, or normal acquisition path;
- `EUStack.getItemStackForNEI()` returns a new stack of this registered item.

The item remains registered because AE2 and integrations require a stable real `ItemStack`, but it is not part of the
mod's usable content catalog.

## Terminal Amount Rendering

`EUStack.drawOverlayInGui()` delegates amount drawing to AE2's public `StackSizeRenderer` with
`AEConfig.instance.getTerminalFontSize()`. Visibility conditions remain aligned with AE2's native stack implementation.
This gives EU the same wide/slim number conversion, scaling, placement, and terminal-font preference as item and fluid
stacks.

## Creative Tab

Add one `CreativeTabs` instance for Applied Energistics: EU Network. Its icon uses the registered EU display item while
the display item itself remains absent from the tab contents.

Content order is deterministic:

1. eight EU storage components in tier order;
2. eight EU storage cells in tier order;
3. all stacks returned by `HatchRegistration.getRegisteredHatches()` in registration order.

The two usable item classes move from vanilla tabs to the mod tab. The tab appends hatch stacks dynamically when
Minecraft requests its contents; it does not construct MetaTileEntities or register anything. Consequently, hatches
added later through `registerLaserSeries(...)` appear automatically on the next content refresh.

## Lifecycle And Failure Behavior

The display item and creative tab are constructed with the other item singletons. Item registration still occurs before
hatch registration. The tab must tolerate an empty hatch registry during early client queries and simply show the 16
usable storage items until hatch registration has completed.

MetaTileEntity ID validation and registration remain owned exclusively by `HatchRegistration`; the creative tab only
copies already registered stacks.

## Localization

Add English and Simplified Chinese entries for the display item and creative tab. Existing stack, component, cell, and
hatch translations remain unchanged.

## Verification

Automated verification remains registry-free, consistent with the project's test boundary:

- run all existing pure logic tests;
- compile production code and process resources;
- run the full build;
- verify the display item is not assigned to a creative tab;
- verify the component and cell classes use the mod tab;
- verify the tab reads registered hatch stacks rather than constructing MTEs.

In game, verify that the unified terminal tooltip says `EU Energy` / `EU 能源`, the overlay uses AE2-style abbreviated
amounts, the mod tab contains 8 components, 8 cells, and all 158 default hatches, and a subsequently registered laser
series is included without adding the hidden display item.
