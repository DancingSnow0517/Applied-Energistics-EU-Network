package cn.dancingsnow.appeu.registry;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import org.junit.jupiter.api.Test;

import cn.dancingsnow.appeu.hatch.HatchDirection;
import cn.dancingsnow.appeu.hatch.HatchFamily;
import cn.dancingsnow.appeu.hatch.HatchSpec;
import cn.dancingsnow.appeu.hatch.HatchSpecs;
import gregtech.api.interfaces.IItemContainer;

class ModItemsTest {

    @Test
    void indexesEveryDefaultHatchByAmperageDirectionAndTier() {
        Item item = new Item();
        int metadata = 0;

        for (HatchSpec spec : HatchSpecs.create(27_000)) {
            ItemStack stack = new ItemStack(item, 1, metadata++);

            ModItems.setHatch(spec, stack);

            IItemContainer[] hatches = hatchesFor(spec.direction(), spec.amperage());
            assertEquals(HatchSpecs.MAX_TIER + 1, hatches.length);
            assertTrue(hatches[spec.tier()].hasBeenSet(), spec::toString);
            ItemStack registered = hatches[spec.tier()].get(1);
            assertSame(item, registered.getItem(), spec::toString);
            assertEquals(stack.getItemDamage(), registered.getItemDamage(), spec::toString);
        }

        for (HatchSpec spec : HatchSpecs.create(27_000)) {
            if (spec.family() != HatchFamily.LASER) {
                continue;
            }
            IItemContainer[] hatches = hatchesFor(spec.direction(), spec.amperage());
            for (int tier = HatchSpecs.MIN_TIER; tier < HatchSpecs.MIN_LASER_TIER; tier++) {
                assertFalse(hatches[tier].hasBeenSet());
            }
        }
    }

    private static IItemContainer[] hatchesFor(HatchDirection direction, int amperage) {
        boolean energy = direction == HatchDirection.ENERGY;
        return switch (amperage) {
            case 2 -> energy ? ModItems.ME_ENERGY_HATCH : ModItems.ME_DYNAMO_HATCH;
            case 4 -> energy ? ModItems.ME_ENERGY_HATCH_4A : ModItems.ME_DYNAMO_HATCH_4A;
            case 16 -> energy ? ModItems.ME_ENERGY_HATCH_16A : ModItems.ME_DYNAMO_HATCH_16A;
            case 64 -> energy ? ModItems.ME_ENERGY_HATCH_64A : ModItems.ME_DYNAMO_HATCH_64A;
            case 256 -> energy ? ModItems.ME_ENERGY_HATCH_256A : ModItems.ME_DYNAMO_HATCH_256A;
            case 1_024 -> energy ? ModItems.ME_ENERGY_HATCH_1024A : ModItems.ME_DYNAMO_HATCH_1024A;
            case 4_096 -> energy ? ModItems.ME_ENERGY_HATCH_4096A : ModItems.ME_DYNAMO_HATCH_4096A;
            case 16_384 -> energy ? ModItems.ME_ENERGY_HATCH_16384A : ModItems.ME_DYNAMO_HATCH_16384A;
            case 65_536 -> energy ? ModItems.ME_ENERGY_HATCH_65536A : ModItems.ME_DYNAMO_HATCH_65536A;
            case 262_144 -> energy ? ModItems.ME_ENERGY_HATCH_262144A : ModItems.ME_DYNAMO_HATCH_262144A;
            case 1_048_576 -> energy ? ModItems.ME_ENERGY_HATCH_1048576A : ModItems.ME_DYNAMO_HATCH_1048576A;
            default -> throw new AssertionError("Unexpected amperage: " + amperage);
        };
    }
}
