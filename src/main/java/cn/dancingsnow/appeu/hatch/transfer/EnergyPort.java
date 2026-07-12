package cn.dancingsnow.appeu.hatch.transfer;

public interface EnergyPort {

    long extract(long requested);

    long insert(long offered);
}
