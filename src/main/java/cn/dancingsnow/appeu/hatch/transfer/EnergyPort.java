package cn.dancingsnow.appeu.hatch.transfer;

public interface EnergyPort {

    long simulateExtract(long requested);

    long extract(long requested);

    long simulateInsert(long offered);

    long insert(long offered);
}
