package cn.dancingsnow.appeu.hatch.transfer;

public final class MEHatchTransferPolicy {

    // Matches GT5U WirelessNetworkManager.ticks_between_energy_addition.
    public static final long TICKS_BETWEEN_TRANSFERS = 2_000L;
    private static final long BUFFERED_BATCHES = 2L;

    private MEHatchTransferPolicy() {}

    public static boolean shouldTransfer(long tick) {
        return tick % TICKS_BETWEEN_TRANSFERS == 0L;
    }

    public static long batchSize(long voltage, long amperage) {
        validateRate(voltage, amperage);
        return Math.multiplyExact(Math.multiplyExact(voltage, amperage), TICKS_BETWEEN_TRANSFERS);
    }

    public static long bufferCapacity(long voltage, long amperage) {
        return Math.multiplyExact(batchSize(voltage, amperage), BUFFERED_BATCHES);
    }

    private static void validateRate(long voltage, long amperage) {
        if (voltage <= 0L || amperage <= 0L) {
            throw new IllegalArgumentException("voltage and amperage must be positive");
        }
    }
}
