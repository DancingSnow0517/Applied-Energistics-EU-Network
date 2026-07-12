# EU Terminal Presentation And Creative Tab Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Make EU appear as its own identity with AE2-native abbreviated amounts in the unified terminal, and add a mod creative tab containing all usable storage items and registered ME hatches.

**Architecture:** A hidden registered `ItemEUDisplay` is the stable `ItemStack` representative required by AE2 tooltips and integrations. `EUStack` delegates amount rendering to AE2's `StackSizeRenderer`. A dedicated `ModCreativeTab` lets normal item enumeration supply the eight components and eight cells, then dynamically appends copies from `HatchRegistration` so future laser series appear without constructing or registering MTEs.

**Tech Stack:** Java 8 bytecode with Jabel syntax, Forge 1.7.10, AE2 Unofficial rv3-beta-1000-GTNH, GT5-Unofficial 5.09.54.20, Gradle, JUnit Jupiter pure-logic boundary.

---

## File Map

**Create:**

- `src/main/java/cn/dancingsnow/appeu/item/ItemEUDisplay.java`: hidden representative item for EU tooltips and the creative-tab icon.
- `src/main/java/cn/dancingsnow/appeu/registry/ModCreativeTab.java`: deterministic mod tab with dynamic hatch entries.

**Modify:**

- `src/main/java/cn/dancingsnow/appeu/storage/EUStack.java`: return the representative item and use AE2 amount rendering.
- `src/main/java/cn/dancingsnow/appeu/registry/ModItems.java`: own and register the display item.
- `src/main/java/cn/dancingsnow/appeu/item/ItemEUStorageComponent.java`: move components to the mod tab.
- `src/main/java/cn/dancingsnow/appeu/item/ItemEUStorageCell.java`: move cells to the mod tab.
- `src/main/resources/assets/appeu/lang/en_US.lang`: add display-item and tab names.
- `src/main/resources/assets/appeu/lang/zh_CN.lang`: add display-item and tab names.
- `docs/manual-test-checklist.md`: add terminal presentation and creative-tab acceptance checks.

No new JUnit test may initialize Forge registries, construct registered MTEs, or execute client OpenGL code. These adapter changes use compile/build verification and the existing manual game-test boundary approved for this project.

### Task 1: Add the hidden EU representative and native amount rendering

**Files:**

- Create: `src/main/java/cn/dancingsnow/appeu/item/ItemEUDisplay.java`
- Modify: `src/main/java/cn/dancingsnow/appeu/registry/ModItems.java`
- Modify: `src/main/java/cn/dancingsnow/appeu/storage/EUStack.java`
- Modify: `src/main/resources/assets/appeu/lang/en_US.lang`
- Modify: `src/main/resources/assets/appeu/lang/zh_CN.lang`

- [ ] **Step 1: Record the failing game behavior**

Use the supplied screenshot as the regression record: hovering EU in the unified terminal currently shows the 1k EU
storage component, and the slot overlay renders the full raw amount. Do not add a JUnit call to
`EUStack.getItemStackForNEI()` because loading `ModItems` constructs AE/Forge item adapters and crosses the approved
pure-test boundary.

- [ ] **Step 2: Add the hidden display item**

Create the complete class:

```java
package cn.dancingsnow.appeu.item;

import net.minecraft.item.Item;

public final class ItemEUDisplay extends Item {

    public ItemEUDisplay() {
        this.setUnlocalizedName("appeu.eu_energy_display");
        this.setTextureName("appeu:eu_stack");
        this.setMaxStackSize(1);
        this.setCreativeTab(null);
    }
}
```

Add the display item before the usable item fields in `ModItems`, then register all three in field order:

```java
public static final ItemEUDisplay EU_ENERGY_DISPLAY = new ItemEUDisplay();
public static final ItemEUStorageComponent EU_STORAGE_COMPONENT = new ItemEUStorageComponent();
public static final ItemEUStorageCell EU_STORAGE_CELL = new ItemEUStorageCell();

public static void register() {
    GameRegistry.registerItem(EU_ENERGY_DISPLAY, "eu_energy_display");
    GameRegistry.registerItem(EU_STORAGE_COMPONENT, "eu_storage_component");
    GameRegistry.registerItem(EU_STORAGE_CELL, "eu_storage_cell");
}
```

- [ ] **Step 3: Replace the component representative in `EUStack`**

Remove the `Item` and `GameRegistry` imports, import `cn.dancingsnow.appeu.registry.ModItems`, and replace the method:

```java
@Override
public ItemStack getItemStackForNEI() {
    return new ItemStack(ModItems.EU_ENERGY_DISPLAY);
}
```

- [ ] **Step 4: Delegate overlay rendering to AE2**

Import `appeng.api.config.TerminalFontSize`, `appeng.client.render.StackSizeRenderer`, and `appeng.core.AEConfig`.
Replace `drawOverlayInGui` with the native AE2 structure:

```java
@Override
@SideOnly(Side.CLIENT)
public void drawOverlayInGui(Minecraft mc, int x, int y, boolean showAmount, boolean showAmountAlways,
    boolean showCraftableText, boolean showCraftableIcon) {
    TerminalFontSize fontSize = AEConfig.instance.getTerminalFontSize();

    GL11.glTranslatef(0.0F, 0.0F, 200.0F);
    GL11.glDisable(GL11.GL_LIGHTING);
    if (showAmount && (amount > 1 || showAmountAlways && amount > 0)) {
        GL11.glPushMatrix();
        StackSizeRenderer.drawStackSize(x, y, amount, mc.fontRenderer, fontSize);
        GL11.glPopMatrix();
    }
    GL11.glEnable(GL11.GL_LIGHTING);
    GL11.glTranslatef(0.0F, 0.0F, -200.0F);
}
```

- [ ] **Step 5: Add localized display names**

Add under the EU stack section:

```properties
# en_US.lang
item.appeu.eu_energy_display.name=EU Energy

# zh_CN.lang
item.appeu.eu_energy_display.name=EU 能源
```

- [ ] **Step 6: Compile and process resources**

Run: `./gradlew.bat compileJava processResources`

Expected: both tasks succeed; no duplicate texture is required because the item uses the existing
`assets/appeu/textures/items/eu_stack.png`.

- [ ] **Step 7: Commit**

```bash
git add src/main/java/cn/dancingsnow/appeu/item/ItemEUDisplay.java \
  src/main/java/cn/dancingsnow/appeu/registry/ModItems.java \
  src/main/java/cn/dancingsnow/appeu/storage/EUStack.java \
  src/main/resources/assets/appeu/lang/en_US.lang \
  src/main/resources/assets/appeu/lang/zh_CN.lang
git commit -m "fix: correct EU terminal presentation"
```

### Task 2: Add the dynamic mod creative tab

**Files:**

- Create: `src/main/java/cn/dancingsnow/appeu/registry/ModCreativeTab.java`
- Modify: `src/main/java/cn/dancingsnow/appeu/item/ItemEUStorageComponent.java`
- Modify: `src/main/java/cn/dancingsnow/appeu/item/ItemEUStorageCell.java`
- Modify: `src/main/resources/assets/appeu/lang/en_US.lang`
- Modify: `src/main/resources/assets/appeu/lang/zh_CN.lang`

- [ ] **Step 1: Create the creative tab**

Create the complete class. The raw `List` override matches Minecraft 1.7.10's API, while copied hatch stacks prevent
callers from mutating registrar-owned values.

```java
package cn.dancingsnow.appeu.registry;

import java.util.List;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import cn.dancingsnow.appeu.AppEU;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public final class ModCreativeTab extends CreativeTabs {

    public static final ModCreativeTab INSTANCE = new ModCreativeTab();

    private ModCreativeTab() {
        super(AppEU.MODID);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public Item getTabIconItem() {
        return ModItems.EU_ENERGY_DISPLAY;
    }

    @Override
    @SideOnly(Side.CLIENT)
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public void displayAllReleventItems(List itemStacks) {
        super.displayAllReleventItems(itemStacks);
        for (ItemStack hatch : HatchRegistration.getRegisteredHatches().values()) {
            itemStacks.add(hatch.copy());
        }
    }
}
```

- [ ] **Step 2: Move usable items to the mod tab**

In `ItemEUStorageComponent`, remove the `CreativeTabs` import, import `ModCreativeTab`, and change the constructor to:

```java
this.setCreativeTab(ModCreativeTab.INSTANCE);
```

Apply the same change to `ItemEUStorageCell`. Keep the display item on `null`; it is the icon but not content.

- [ ] **Step 3: Add localized tab names**

Add near the top of each language file:

```properties
# en_US.lang
itemGroup.appeu=Applied Energistics: EU Network

# zh_CN.lang
itemGroup.appeu=应用能源：EU 网络
```

- [ ] **Step 4: Compile and audit the registration boundary**

Run: `./gradlew.bat compileJava processResources`

Expected: compilation and resource processing succeed.

Run:

```powershell
rg "new MTE|registerAll|registerLaserSeries" src/main/java/cn/dancingsnow/appeu/registry/ModCreativeTab.java
```

Expected: no matches. The tab only calls `getRegisteredHatches()`.

- [ ] **Step 5: Commit**

```bash
git add src/main/java/cn/dancingsnow/appeu/registry/ModCreativeTab.java \
  src/main/java/cn/dancingsnow/appeu/item/ItemEUStorageComponent.java \
  src/main/java/cn/dancingsnow/appeu/item/ItemEUStorageCell.java \
  src/main/resources/assets/appeu/lang/en_US.lang \
  src/main/resources/assets/appeu/lang/zh_CN.lang
git commit -m "feat: add EU network creative tab"
```

### Task 3: Document and verify the user-visible behavior

**Files:**

- Modify: `docs/manual-test-checklist.md`

- [ ] **Step 1: Extend the manual checklist**

Add a new recordable section with Pass/Fail/Blocked and evidence fields. Require these exact checks:

1. Hover EU in the unified terminal and confirm the title is `EU Energy` / `EU 能源`, never a storage component.
2. Confirm representative amounts around 1,000, 1,000,000, 1,000,000,000, and 1,000,000,000,000 use the same
   AE2 K/M/G/T presentation as native terminal entries for each terminal font-size setting.
3. Confirm the Applied Energistics: EU Network tab contains exactly 8 components, 8 cells, and 158 default hatches,
   in that order, with no `EU Energy` display item.
4. Register one extra laser series in a disposable instance, reopen/refresh the creative inventory, and confirm all 18
   new hatches appear without duplicates.

- [ ] **Step 2: Run IDEA and Gradle verification**

Run the IDEA project build, then:

```powershell
./gradlew.bat test
./gradlew.bat compileJava processResources
./gradlew.bat build
```

Expected: IDEA reports no problems; all 51 existing pure tests pass; compilation, resources, and full build succeed.

- [ ] **Step 3: Audit scope and working tree**

Run:

```powershell
rg "CreativeTabs\.tabMaterials|CreativeTabs\.tabMisc" src/main/java/cn/dancingsnow/appeu/item
rg "GameRegistry\.findItem|Long\.toString\(amount\)" src/main/java/cn/dancingsnow/appeu/storage/EUStack.java
git diff --check
git status --short
```

Expected: both `rg` commands have no matches; `git diff --check` succeeds; only the intentional checklist change is
uncommitted before the final commit.

- [ ] **Step 4: Commit**

```bash
git add docs/manual-test-checklist.md
git commit -m "docs: add terminal and creative tab checks"
```

- [ ] **Step 5: Final review**

Review the complete delta from the design commit through HEAD for client/server classloading, static initialization,
creative-tab duplication, representative-item leakage, native amount-rendering parity, and adherence to the no-registry
JUnit boundary. Fix every Critical or Important finding and re-run the full verification commands.
