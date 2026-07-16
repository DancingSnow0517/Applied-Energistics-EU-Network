package cn.dancingsnow.appeu.hatch.transfer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class MEHatchTransferPolicyTest {

    @Test
    void transfersEveryTwentyTicks() {
        assertEquals(20L, MEHatchTransferPolicy.TICKS_BETWEEN_TRANSFERS);
        assertFalse(MEHatchTransferPolicy.shouldTransfer(19L));
        assertTrue(MEHatchTransferPolicy.shouldTransfer(20L));
        assertTrue(MEHatchTransferPolicy.shouldTransfer(40L));
    }

    @Test
    void batchesOneIntervalAndBuffersTwoBatches() {
        assertEquals(1_280L, MEHatchTransferPolicy.batchSize(32L, 2L));
        assertEquals(2_560L, MEHatchTransferPolicy.bufferCapacity(32L, 2L));
    }

    @Test
    void supportsHighestDefaultLaserWithoutOverflow() {
        assertEquals(45_035_996_105_932_800L, MEHatchTransferPolicy.batchSize(2_147_483_640L, 1_048_576L));
        assertEquals(90_071_992_211_865_600L, MEHatchTransferPolicy.bufferCapacity(2_147_483_640L, 1_048_576L));
    }

    @Test
    void rejectsInvalidAndOverflowingRates() {
        assertThrows(IllegalArgumentException.class, () -> MEHatchTransferPolicy.batchSize(0L, 2L));
        assertThrows(IllegalArgumentException.class, () -> MEHatchTransferPolicy.bufferCapacity(32L, 0L));
        assertThrows(ArithmeticException.class, () -> MEHatchTransferPolicy.bufferCapacity(Long.MAX_VALUE, 2L));
    }
}
