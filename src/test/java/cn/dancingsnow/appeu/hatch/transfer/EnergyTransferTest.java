package cn.dancingsnow.appeu.hatch.transfer;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class EnergyTransferTest {

    @Test
    void pullsOnlyWhatTheSourceActuallyContains() {
        FakePort source = new FakePort(600, 1_000);

        long moved = EnergyTransfer.pull(source, 100, 1_000, 800);

        assertEquals(600, moved);
        assertEquals(0, source.stored);
    }

    @Test
    void pushesOnlyWhatTheDestinationCanActuallyAccept() {
        FakePort destination = new FakePort(750, 1_000);

        long moved = EnergyTransfer.push(destination, 900, 800);

        assertEquals(250, moved);
        assertEquals(1_000, destination.stored);
    }

    @Test
    void successfulTransfersTouchEachPortOnce() {
        FakePort source = new FakePort(600, 1_000);
        FakePort destination = new FakePort(0, 1_000);

        assertEquals(600, EnergyTransfer.pull(source, 100, 1_000, 800));
        assertEquals(800, EnergyTransfer.push(destination, 900, 800));
        assertEquals(1, source.calls);
        assertEquals(1, destination.calls);
    }

    @Test
    void zeroLimitDoesNotTouchEitherPort() {
        FakePort source = new FakePort(600, 1_000);
        FakePort destination = new FakePort(0, 1_000);

        assertEquals(0, EnergyTransfer.pull(source, 100, 1_000, 0));
        assertEquals(0, EnergyTransfer.push(destination, 900, 0));
        assertEquals(0, source.calls);
        assertEquals(0, destination.calls);
    }

    @Test
    void fullLocalBufferDoesNotTouchSource() {
        FakePort source = new FakePort(600, 1_000);

        assertEquals(0, EnergyTransfer.pull(source, 1_000, 1_000, 800));
        assertEquals(600, source.stored);
        assertEquals(0, source.calls);
    }

    @Test
    void fullDestinationAcceptsNothing() {
        FakePort destination = new FakePort(1_000, 1_000);

        assertEquals(0, EnergyTransfer.push(destination, 900, 800));
        assertEquals(1_000, destination.stored);
    }

    @Test
    void emptySourceProvidesNothing() {
        FakePort source = new FakePort(0, 1_000);

        assertEquals(0, EnergyTransfer.pull(source, 100, 1_000, 800));
        assertEquals(0, source.stored);
    }

    @Test
    void returnedModulatedAmountsPreserveEnergy() {
        FakePort source = new FakePort(600, 600);
        FakePort destination = new FakePort(0, 1_000);
        long local = 100;
        long initialTotal = source.stored + local + destination.stored;

        long pulled = EnergyTransfer.pull(source, local, 1_000, 800);
        local += pulled;
        long pushed = EnergyTransfer.push(destination, local, 450);
        local -= pushed;

        assertEquals(initialTotal, source.stored + local + destination.stored);
        assertEquals(600, pulled);
        assertEquals(450, pushed);
    }

    @Test
    void partialExtractionReturnsActualAmountAndPreservesEnergy() {
        PartialPort source = new PartialPort(1_000, 250);
        long local = 100;
        long initialTotal = source.stored + local;

        long moved = EnergyTransfer.pull(source, local, 1_000, 800);
        local += moved;

        assertEquals(250, moved);
        assertEquals(800, source.lastExtractRequest);
        assertEquals(initialTotal, source.stored + local);
    }

    @Test
    void partialInsertionReturnsActualAmountAndPreservesEnergy() {
        PartialPort destination = new PartialPort(100, 200);
        long local = 700;
        long initialTotal = destination.stored + local;

        long moved = EnergyTransfer.push(destination, local, 800);
        local -= moved;

        assertEquals(200, moved);
        assertEquals(700, destination.lastInsertOffer);
        assertEquals(initialTotal, destination.stored + local);
    }

    @Test
    void clampsMaliciousExtractionResults() {
        ScriptedPort tooLarge = new ScriptedPort(Long.MAX_VALUE);
        ScriptedPort negativeExtraction = new ScriptedPort(-1);

        assertEquals(100, EnergyTransfer.pull(tooLarge, 0, 100, 100));
        assertEquals(0, EnergyTransfer.pull(negativeExtraction, 0, 100, 100));
    }

    @Test
    void clampsMaliciousInsertionResults() {
        ScriptedPort tooLarge = new ScriptedPort(Long.MAX_VALUE);
        ScriptedPort negativeInsertion = new ScriptedPort(-1);

        assertEquals(100, EnergyTransfer.push(tooLarge, 100, 100));
        assertEquals(0, EnergyTransfer.push(negativeInsertion, 100, 100));
    }

    @Test
    void invalidNegativeInputsAreNoOps() {
        FakePort port = new FakePort(Long.MAX_VALUE, Long.MAX_VALUE);

        assertEquals(0, EnergyTransfer.pull(port, -1, Long.MAX_VALUE, Long.MAX_VALUE));
        assertEquals(0, EnergyTransfer.pull(port, 0, -1, Long.MAX_VALUE));
        assertEquals(0, EnergyTransfer.pull(port, 0, Long.MAX_VALUE, -1));
        assertEquals(0, EnergyTransfer.push(port, -1, Long.MAX_VALUE));
        assertEquals(0, EnergyTransfer.push(port, Long.MAX_VALUE, -1));
        assertEquals(0, port.calls);
    }

    @Test
    void overfullBufferDoesNotOverflowHeadroomCalculation() {
        FakePort source = new FakePort(Long.MAX_VALUE, Long.MAX_VALUE);

        assertEquals(0, EnergyTransfer.pull(source, Long.MAX_VALUE, Long.MIN_VALUE, Long.MAX_VALUE));
        assertEquals(0, EnergyTransfer.pull(source, 2, 1, Long.MAX_VALUE));
        assertEquals(0, source.calls);
    }

    @Test
    void supportsValidLongBoundaryTransfers() {
        FakePort source = new FakePort(Long.MAX_VALUE, Long.MAX_VALUE);
        FakePort destination = new FakePort(0, Long.MAX_VALUE);

        assertEquals(Long.MAX_VALUE, EnergyTransfer.pull(source, 0, Long.MAX_VALUE, Long.MAX_VALUE));
        assertEquals(Long.MAX_VALUE, EnergyTransfer.push(destination, Long.MAX_VALUE, Long.MAX_VALUE));
        assertEquals(0, source.stored);
        assertEquals(Long.MAX_VALUE, destination.stored);
    }

    private static final class FakePort implements EnergyPort {

        private long stored;
        private final long capacity;
        private int calls;

        private FakePort(long stored, long capacity) {
            this.stored = stored;
            this.capacity = capacity;
        }

        @Override
        public long extract(long requested) {
            calls++;
            long extracted = Math.min(stored, requested);
            stored -= extracted;
            return extracted;
        }

        @Override
        public long insert(long offered) {
            calls++;
            long inserted = Math.min(capacity - stored, offered);
            stored += inserted;
            return inserted;
        }
    }

    private static final class ScriptedPort implements EnergyPort {

        private final long modulated;

        private ScriptedPort(long modulated) {
            this.modulated = modulated;
        }

        @Override
        public long extract(long requested) {
            return modulated;
        }

        @Override
        public long insert(long offered) {
            return modulated;
        }
    }

    private static final class PartialPort implements EnergyPort {

        private long stored;
        private final long modulated;
        private long lastExtractRequest = -1;
        private long lastInsertOffer = -1;

        private PartialPort(long stored, long modulated) {
            this.stored = stored;
            this.modulated = modulated;
        }

        @Override
        public long extract(long requested) {
            lastExtractRequest = requested;
            stored -= modulated;
            return modulated;
        }

        @Override
        public long insert(long offered) {
            lastInsertOffer = offered;
            stored += modulated;
            return modulated;
        }
    }
}
