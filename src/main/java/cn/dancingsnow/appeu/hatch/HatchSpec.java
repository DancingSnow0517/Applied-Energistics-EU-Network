package cn.dancingsnow.appeu.hatch;

import java.util.Objects;

public final class HatchSpec {

    private final int id;
    private final String name;
    private final int tier;
    private final int amperage;
    private final HatchDirection direction;
    private final HatchFamily family;

    public HatchSpec(int id, String name, int tier, int amperage, HatchDirection direction, HatchFamily family) {
        this.id = id;
        this.name = Objects.requireNonNull(name, "name");
        this.tier = tier;
        this.amperage = amperage;
        this.direction = Objects.requireNonNull(direction, "direction");
        this.family = Objects.requireNonNull(family, "family");
    }

    public int id() {
        return id;
    }

    public String name() {
        return name;
    }

    public int tier() {
        return tier;
    }

    public int amperage() {
        return amperage;
    }

    public HatchDirection direction() {
        return direction;
    }

    public HatchFamily family() {
        return family;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof HatchSpec)) {
            return false;
        }
        HatchSpec that = (HatchSpec) other;
        return id == that.id && tier == that.tier
            && amperage == that.amperage
            && name.equals(that.name)
            && direction == that.direction
            && family == that.family;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, tier, amperage, direction, family);
    }

    @Override
    public String toString() {
        return "HatchSpec{" + "id="
            + id
            + ", name='"
            + name
            + '\''
            + ", tier="
            + tier
            + ", amperage="
            + amperage
            + ", direction="
            + direction
            + ", family="
            + family
            + '}';
    }
}
