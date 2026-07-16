package cn.dancingsnow.appeu.hatch;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;

public final class HatchSpecs {

    private static final String[] TIER_LABELS = { "ulv", "lv", "mv", "hv", "ev", "iv", "luv", "zpm", "uv", "uhv", "uev",
        "uiv", "umv", "uxv", "max" };
    private static final int[] STANDARD_AMPERAGES = { 2, 4, 16, 64 };
    private static final int[] DEFAULT_LASER_AMPERAGES = { 256, 1_024, 4_096, 16_384, 65_536, 262_144, 1_048_576 };
    private static final HatchDirection[] DIRECTIONS = { HatchDirection.DYNAMO, HatchDirection.ENERGY };

    public static final int MIN_TIER = 0;
    public static final int MIN_LASER_TIER = 5;
    public static final int MAX_TIER = TIER_LABELS.length - 1;
    public static final int DEFAULT_HATCH_COUNT = STANDARD_AMPERAGES.length * TIER_LABELS.length * DIRECTIONS.length
        + DEFAULT_LASER_AMPERAGES.length * (MAX_TIER - MIN_LASER_TIER + 1) * DIRECTIONS.length;

    private HatchSpecs() {}

    public static List<HatchSpec> create(int start) {
        return create(start, DEFAULT_LASER_AMPERAGES);
    }

    public static List<HatchSpec> create(int start, int... laserAmperages) {
        validateStart(start);
        Objects.requireNonNull(laserAmperages, "laserAmperages");
        validateLaserAmperages(laserAmperages);

        List<HatchSpec> specs = new ArrayList<>();
        for (int amperage : STANDARD_AMPERAGES) {
            for (HatchDirection direction : DIRECTIONS) {
                for (int tier = MIN_TIER; tier <= MAX_TIER; tier++) {
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
        for (HatchDirection direction : DIRECTIONS) {
            for (int tier = MIN_LASER_TIER; tier <= MAX_TIER; tier++) {
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
        return amperage > 2 ? HatchFamily.MULTI_AMP : HatchFamily.STANDARD;
    }

    private static void add(List<HatchSpec> specs, int start, int tier, int amperage, HatchDirection direction) {
        int id = Math.addExact(start, specs.size());
        String name = name(direction, tier, amperage);
        specs.add(new HatchSpec(id, name, tier, amperage, direction, family(direction, amperage)));
    }

    private static String name(HatchDirection direction, int tier, int amperage) {
        return "appeu.hatch." + direction.name()
            .toLowerCase(Locale.ROOT) + '.' + TIER_LABELS[tier] + '.' + amperage + 'a';
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
