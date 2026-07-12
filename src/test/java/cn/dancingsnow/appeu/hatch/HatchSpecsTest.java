package cn.dancingsnow.appeu.hatch;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;

class HatchSpecsTest {

    @Test
    void createsDefaultProductMatrix() {
        List<HatchSpec> specs = HatchSpecs.create(27_000);

        assertEquals(158, specs.size());
        assertEquals(
            104,
            specs.stream()
                .filter(spec -> spec.family() != HatchFamily.LASER)
                .count());
        assertEquals(
            54,
            specs.stream()
                .filter(spec -> spec.family() == HatchFamily.LASER)
                .count());
        assertEquals(
            27_000,
            specs.get(0)
                .id());
        assertEquals(
            27_157,
            specs.get(specs.size() - 1)
                .id());
    }

    @Test
    void usesStableTierAmpDirectionOrdering() {
        List<HatchSpec> specs = HatchSpecs.create(27_000);
        String[] tierLabels = { "lv", "mv", "hv", "ev", "iv", "luv", "zpm", "uv", "uhv", "uev", "uiv", "umv", "uxv" };
        int[] standardAmperages = { 2, 4, 16, 64 };
        int[] laserAmperages = { 256, 1_024, 4_096 };
        HatchDirection[] directions = { HatchDirection.ENERGY, HatchDirection.DYNAMO };
        int index = 0;

        for (int tier = 1; tier <= tierLabels.length; tier++) {
            for (int amperage : standardAmperages) {
                for (HatchDirection direction : directions) {
                    assertSpec(specs.get(index++), tierLabels[tier - 1], tier, amperage, direction);
                }
            }
        }
        for (int amperage : laserAmperages) {
            for (int tier = 5; tier <= tierLabels.length; tier++) {
                for (HatchDirection direction : directions) {
                    assertSpec(specs.get(index++), tierLabels[tier - 1], tier, amperage, direction);
                }
            }
        }
        assertEquals(specs.size(), index);
    }

    @Test
    void createsContinuousIdsAndUniqueNames() {
        List<HatchSpec> specs = HatchSpecs.create(27_000);
        Set<String> names = new HashSet<>();

        for (int index = 0; index < specs.size(); index++) {
            HatchSpec spec = specs.get(index);
            assertEquals(27_000 + index, spec.id());
            assertTrue(names.add(spec.name()), () -> "duplicate name: " + spec.name());
        }
    }

    @Test
    void coversExpectedTierAndAmperageBoundaries() {
        List<HatchSpec> specs = HatchSpecs.create(27_000);
        List<HatchSpec> standardSeries = specs.subList(0, 104);
        List<HatchSpec> laserSeries = specs.subList(104, specs.size());

        assertEquals(
            asSet(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13),
            standardSeries.stream()
                .map(HatchSpec::tier)
                .collect(java.util.stream.Collectors.toSet()));
        assertEquals(
            asSet(2, 4, 16, 64),
            standardSeries.stream()
                .map(HatchSpec::amperage)
                .collect(java.util.stream.Collectors.toSet()));
        assertTrue(
            laserSeries.stream()
                .allMatch(spec -> spec.tier() >= 5 && spec.tier() <= 13));
        assertEquals(
            asSet(256, 1_024, 4_096),
            laserSeries.stream()
                .map(HatchSpec::amperage)
                .collect(java.util.stream.Collectors.toSet()));
    }

    @Test
    void appendsCustomLaserAmperagesInCallerOrder() {
        List<HatchSpec> specs = HatchSpecs.create(27_000, 256, 1_024, 4_096, 16_384);

        assertEquals(176, specs.size());
        assertTrue(
            specs.subList(158, 176)
                .stream()
                .allMatch(spec -> spec.amperage() == 16_384 && spec.tier() >= 5));
        assertEquals(
            "appeu.hatch.energy.iv.16384a",
            specs.get(158)
                .name());
    }

    @Test
    void createsOneLaserSeriesWithContinuousIds() {
        List<HatchSpec> specs = HatchSpecs.createLaserSeries(400, 512);

        assertEquals(18, specs.size());
        for (int index = 0; index < specs.size(); index++) {
            assertEquals(
                400 + index,
                specs.get(index)
                    .id());
            assertEquals(
                512,
                specs.get(index)
                    .amperage());
            assertEquals(
                HatchFamily.LASER,
                specs.get(index)
                    .family());
        }
        assertEquals(
            "appeu.hatch.energy.iv.512a",
            specs.get(0)
                .name());
        assertEquals(
            "appeu.hatch.dynamo.uxv.512a",
            specs.get(17)
                .name());
    }

    @Test
    void classifiesFamiliesAtDirectionSpecificBoundaries() {
        assertEquals(HatchFamily.STANDARD, HatchSpecs.family(HatchDirection.ENERGY, 2));
        assertEquals(HatchFamily.MULTI_AMP, HatchSpecs.family(HatchDirection.ENERGY, 3));
        assertEquals(HatchFamily.MULTI_AMP, HatchSpecs.family(HatchDirection.ENERGY, 255));
        assertEquals(HatchFamily.LASER, HatchSpecs.family(HatchDirection.ENERGY, 256));

        assertEquals(HatchFamily.STANDARD, HatchSpecs.family(HatchDirection.DYNAMO, 4));
        assertEquals(HatchFamily.MULTI_AMP, HatchSpecs.family(HatchDirection.DYNAMO, 5));
        assertEquals(HatchFamily.MULTI_AMP, HatchSpecs.family(HatchDirection.DYNAMO, 255));
        assertEquals(HatchFamily.LASER, HatchSpecs.family(HatchDirection.DYNAMO, 256));
    }

    @Test
    void hatchSpecHasValueSemantics() {
        HatchSpec first = new HatchSpec(
            1,
            "appeu.hatch.energy.lv.2a",
            1,
            2,
            HatchDirection.ENERGY,
            HatchFamily.STANDARD);
        HatchSpec same = new HatchSpec(
            1,
            "appeu.hatch.energy.lv.2a",
            1,
            2,
            HatchDirection.ENERGY,
            HatchFamily.STANDARD);
        HatchSpec different = new HatchSpec(
            2,
            "appeu.hatch.dynamo.lv.2a",
            1,
            2,
            HatchDirection.DYNAMO,
            HatchFamily.STANDARD);

        assertEquals(first, same);
        assertEquals(first.hashCode(), same.hashCode());
        assertNotEquals(first, different);
        assertNotEquals(null, first);
        assertTrue(
            first.toString()
                .contains("appeu.hatch.energy.lv.2a"));
    }

    @Test
    void rejectsInvalidStartsAmperagesDuplicatesAndIdOverflow() {
        assertThrows(IllegalArgumentException.class, () -> HatchSpecs.create(0));
        assertThrows(IllegalArgumentException.class, () -> HatchSpecs.create(-1));
        assertThrows(IllegalArgumentException.class, () -> HatchSpecs.create(1, 255));
        assertThrows(IllegalArgumentException.class, () -> HatchSpecs.create(1, 0));
        assertThrows(IllegalArgumentException.class, () -> HatchSpecs.create(1, -256));
        assertThrows(IllegalArgumentException.class, () -> HatchSpecs.create(1, 256, 256));
        assertThrows(IllegalArgumentException.class, () -> HatchSpecs.createLaserSeries(1, 255));
        assertThrows(ArithmeticException.class, () -> HatchSpecs.create(Integer.MAX_VALUE));
        assertThrows(ArithmeticException.class, () -> HatchSpecs.createLaserSeries(Integer.MAX_VALUE, 256));
    }

    private static void assertSpec(HatchSpec spec, String tierLabel, int tier, int amperage, HatchDirection direction) {
        String directionLabel = direction == HatchDirection.ENERGY ? "energy" : "dynamo";
        assertEquals("appeu.hatch." + directionLabel + '.' + tierLabel + '.' + amperage + 'a', spec.name());
        assertEquals(tier, spec.tier());
        assertEquals(amperage, spec.amperage());
        assertEquals(direction, spec.direction());
    }

    private static Set<Integer> asSet(Integer... values) {
        return new HashSet<>(Arrays.asList(values));
    }
}
