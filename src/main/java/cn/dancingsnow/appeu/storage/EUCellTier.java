package cn.dancingsnow.appeu.storage;

public enum EUCellTier {

    K1(0, "1k", 1, 0.5),
    K4(1, "4k", 4, 1.0),
    K16(2, "16k", 16, 1.5),
    K64(3, "64k", 64, 2.0),
    K256(4, "256k", 256, 2.5),
    K1024(5, "1024k", 1024, 3.0),
    K4096(6, "4096k", 4096, 3.5),
    K16384(7, "16384k", 16384, 4.0);

    private static final EUCellTier[] BY_META = buildByMeta();

    private final int meta;
    private final String suffix;
    private final int nominalKiB;
    private final double idleDrain;

    EUCellTier(int meta, String suffix, int nominalKiB, double idleDrain) {
        this.meta = meta;
        this.suffix = suffix;
        this.nominalKiB = nominalKiB;
        this.idleDrain = idleDrain;
    }

    public int meta() {
        return meta;
    }

    public String suffix() {
        return suffix;
    }

    public double idleDrain() {
        return idleDrain;
    }

    public long totalBytes() {
        return Math.multiplyExact(nominalKiB, 1024L);
    }

    public long capacityEU() {
        return Math.multiplyExact(totalBytes(), EUConstants.EU_PER_BYTE);
    }

    public static EUCellTier fromMeta(int meta) {
        if (meta < 0 || meta >= BY_META.length) {
            throw new IllegalArgumentException("Unknown EU cell tier metadata: " + meta);
        }
        return BY_META[meta];
    }

    private static EUCellTier[] buildByMeta() {
        EUCellTier[] tiers = values();
        EUCellTier[] byMeta = new EUCellTier[tiers.length];
        for (EUCellTier tier : tiers) {
            if (tier.meta < 0 || tier.meta >= byMeta.length) {
                throw new IllegalStateException("EU cell tier metadata out of range: " + tier.meta);
            }
            if (byMeta[tier.meta] != null) {
                throw new IllegalStateException("Duplicate EU cell tier metadata: " + tier.meta);
            }
            byMeta[tier.meta] = tier;
        }
        for (int meta = 0; meta < byMeta.length; meta++) {
            if (byMeta[meta] == null) {
                throw new IllegalStateException("Missing EU cell tier metadata: " + meta);
            }
        }
        return byMeta;
    }
}
