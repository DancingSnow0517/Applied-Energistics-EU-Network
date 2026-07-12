package cn.dancingsnow.appeu.hatch;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;

public final class HatchSpecs {

    private static final String[] TIER_LABELS = { "lv", "mv", "hv", "ev", "iv", "luv", "zpm", "uv", "uhv", "uev", "uiv",
        "umv", "uxv" };
    private static final int[] STANDARD_AMPERAGES = { 2, 4, 16, 64 };
    private static final int[] DEFAULT_LASER_AMPERAGES = { 256, 1_024, 4_096 };
    private static final HatchDirection[] DIRECTIONS = { HatchDirection.ENERGY, HatchDirection.DYNAMO };

    private HatchSpecs() {}

    public static List<HatchSpec> create(int start) {
        return create(start, DEFAULT_LASER_AMPERAGES);
    }

    public static List<HatchSpec> create(int start, int... laserAmperages) {
        validateStart(start);
        Objects.requireNonNull(laserAmperages, "laserAmperages");
        validateLaserAmperages(laserAmperages);

        List<HatchSpec> specs = new ArrayList<>();
        for (int tier = 1; tier <= TIER_LABELS.length; tier++) {
            for (int amperage : STANDARD_AMPERAGES) {
                for (HatchDirection direction : DIRECTIONS) {
                    add(specs, start, tier, amperage, direction);
                }
            }
        }
        for (int amperage : laserAmperages) {
            int seriesStart = Math.addExact(start, specs.size());
            specs.addAll(createLaserSeries(seriesStart, amperage));
        }
        validateUniqueNames(specs);
        return Collections.unmodifiableList(specs);
    }

    public static List<HatchSpec> createLaserSeries(int startId, int amperage) {
        validateStart(startId);
        validateLaserAmperage(amperage);

        List<HatchSpec> specs = new ArrayList<>();
        for (int tier = 5; tier <= TIER_LABELS.length; tier++) {
            for (HatchDirection direction : DIRECTIONS) {
                add(specs, startId, tier, amperage, direction);
            }
        }
        return Collections.unmodifiableList(specs);
    }

    public static HatchFamily family(HatchDirection direction, int amperage) {
        Objects.requireNonNull(direction, "direction");
        if (amperage >= 256) {
            return HatchFamily.LASER;
        }
        int standardMaximum = direction == HatchDirection.ENERGY ? 2 : 4;
        return amperage > standardMaximum ? HatchFamily.MULTI_AMP : HatchFamily.STANDARD;
    }

    private static void add(List<HatchSpec> specs, int start, int tier, int amperage, HatchDirection direction) {
        int id = Math.addExact(start, specs.size());
        String name = name(direction, tier, amperage);
        specs.add(new HatchSpec(id, name, tier, amperage, direction, family(direction, amperage)));
    }

    private static String name(HatchDirection direction, int tier, int amperage) {
        return "appeu.hatch." + direction.name()
            .toLowerCase(Locale.ROOT) + '.' + TIER_LABELS[tier - 1] + '.' + amperage + 'a';
    }

    private static void validateStart(int start) {
        if (start <= 0) {
            throw new IllegalArgumentException("start must be positive");
        }
    }

    private static void validateLaserAmperages(int[] amperages) {
        for (int amperage : amperages) {
            validateLaserAmperage(amperage);
        }
    }

    private static void validateUniqueNames(List<HatchSpec> specs) {
        Set<String> names = new HashSet<>();
        for (HatchSpec spec : specs) {
            if (!names.add(spec.name())) {
                throw new IllegalArgumentException("Duplicate hatch name: " + spec.name());
            }
        }
    }

    private static void validateLaserAmperage(int amperage) {
        if (amperage < 256) {
            throw new IllegalArgumentException("laser amperage must be at least 256");
        }
    }
}
