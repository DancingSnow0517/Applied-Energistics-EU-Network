package cn.dancingsnow.appeu.storage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class EUCellTierTest {

    @Test
    void calculatesStorageCapacity() {
        assertEquals(1_073_741_824L, EUCellTier.K1.capacityEU());
        assertEquals(4_294_967_296L, EUCellTier.K4.capacityEU());
        assertEquals(17_592_186_044_416L, EUCellTier.K16384.capacityEU());
        assertEquals(16_777_216L, EUCellTier.K16384.totalBytes());
    }

    @Test
    void convertsMetadataToTier() {
        EUCellTier[] tiers = EUCellTier.values();

        assertEquals(8, tiers.length);
        for (int meta = 0; meta < tiers.length; meta++) {
            assertEquals(meta, tiers[meta].meta());
            assertEquals(tiers[meta], EUCellTier.fromMeta(meta));
        }

        assertThrows(IllegalArgumentException.class, () -> EUCellTier.fromMeta(-1));
        assertThrows(IllegalArgumentException.class, () -> EUCellTier.fromMeta(8));
    }

    @Test
    void exposesIdleDrainByTier() {
        assertEquals(0.5, EUCellTier.K1.idleDrain());
        assertEquals(4.0, EUCellTier.K16384.idleDrain());
    }

    @Test
    void hasNoPerTypeOverheadForSingleEuType() {
        assertEquals(1, EUConstants.TOTAL_TYPES);
        assertEquals(0, EUConstants.BYTES_PER_TYPE);
    }
}
