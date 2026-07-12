package cn.dancingsnow.appeu.hatch.transfer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class MEHatchTransferPolicyTest {

    @Test
    void usesGregTechWirelessTransferCadence() {
        assertEquals(2_000L, MEHatchTransferPolicy.TICKS_BETWEEN_TRANSFERS);
        assertFalse(MEHatchTransferPolicy.shouldTransfer(1_999L));
        assertTrue(MEHatchTransferPolicy.shouldTransfer(2_000L));
        assertTrue(MEHatchTransferPolicy.shouldTransfer(4_000L));
    }

    @Test
    void batchesOneIntervalAndBuffersTwoBatches() {
        assertEquals(128_000L, MEHatchTransferPolicy.batchSize(32L, 2L));
        assertEquals(256_000L, MEHatchTransferPolicy.bufferCapacity(32L, 2L));
    }

    @Test
    void supportsHighestDefaultLaserWithoutOverflow() {
        assertEquals(1_125_899_906_842_624_000L, MEHatchTransferPolicy.batchSize(536_870_912L, 1_048_576L));
        assertEquals(2_251_799_813_685_248_000L, MEHatchTransferPolicy.bufferCapacity(536_870_912L, 1_048_576L));
    }

    @Test
    void rejectsInvalidAndOverflowingRates() {
        assertThrows(IllegalArgumentException.class, () -> MEHatchTransferPolicy.batchSize(0L, 2L));
        assertThrows(IllegalArgumentException.class, () -> MEHatchTransferPolicy.bufferCapacity(32L, 0L));
        assertThrows(ArithmeticException.class, () -> MEHatchTransferPolicy.bufferCapacity(Long.MAX_VALUE, 2L));
    }
}
