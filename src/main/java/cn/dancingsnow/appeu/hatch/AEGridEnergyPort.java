package cn.dancingsnow.appeu.hatch;

import java.util.Objects;

import appeng.api.config.Actionable;
import appeng.api.networking.security.BaseActionSource;
import appeng.api.storage.IMEMonitor;
import cn.dancingsnow.appeu.hatch.transfer.EnergyPort;
import cn.dancingsnow.appeu.storage.EUStack;

public final class AEGridEnergyPort implements EnergyPort {

    private final IMEMonitor<EUStack> monitor;
    private final BaseActionSource actionSource;

    public AEGridEnergyPort(IMEMonitor<EUStack> monitor, BaseActionSource actionSource) {
        this.monitor = Objects.requireNonNull(monitor, "monitor");
        this.actionSource = Objects.requireNonNull(actionSource, "actionSource");
    }

    @Override
    public long simulateExtract(long requested) {
        return extract(requested, Actionable.SIMULATE);
    }

    @Override
    public long extract(long requested) {
        return extract(requested, Actionable.MODULATE);
    }

    @Override
    public long simulateInsert(long offered) {
        return insert(offered, Actionable.SIMULATE);
    }

    @Override
    public long insert(long offered) {
        return insert(offered, Actionable.MODULATE);
    }

    private long extract(long requested, Actionable mode) {
        if (requested <= 0) {
            return 0;
        }

        EUStack extracted = monitor.extractItems(new EUStack(requested), mode, actionSource);
        return clamp(extracted == null ? 0 : extracted.getStackSize(), requested);
    }

    private long insert(long offered, Actionable mode) {
        if (offered <= 0) {
            return 0;
        }

        EUStack remainder = monitor.injectItems(new EUStack(offered), mode, actionSource);
        long remainderAmount = clamp(remainder == null ? 0 : remainder.getStackSize(), offered);
        return offered - remainderAmount;
    }

    private static long clamp(long amount, long maximum) {
        return Math.min(Math.max(amount, 0), maximum);
    }
}
