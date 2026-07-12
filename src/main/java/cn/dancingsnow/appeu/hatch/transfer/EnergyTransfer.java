package cn.dancingsnow.appeu.hatch.transfer;

public final class EnergyTransfer {

    private EnergyTransfer() {}

    public static long pull(EnergyPort source, long stored, long capacity, long limit) {
        if (stored < 0 || capacity < 0 || limit <= 0 || stored >= capacity) {
            return 0;
        }

        long requested = Math.min(capacity - stored, limit);
        long simulated = clamp(source.simulateExtract(requested), requested);
        if (simulated == 0) {
            return 0;
        }
        return clamp(source.extract(simulated), simulated);
    }

    public static long push(EnergyPort destination, long stored, long limit) {
        if (stored <= 0 || limit <= 0) {
            return 0;
        }

        long offered = Math.min(stored, limit);
        long simulated = clamp(destination.simulateInsert(offered), offered);
        if (simulated == 0) {
            return 0;
        }
        return clamp(destination.insert(simulated), simulated);
    }

    private static long clamp(long value, long maximum) {
        if (value <= 0) {
            return 0;
        }
        return Math.min(value, maximum);
    }
}
