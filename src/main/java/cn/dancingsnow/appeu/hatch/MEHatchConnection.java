package cn.dancingsnow.appeu.hatch;

import java.util.EnumSet;
import java.util.Objects;
import java.util.function.Supplier;

import javax.annotation.Nullable;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.util.ForgeDirection;

import appeng.api.networking.GridFlags;
import appeng.api.networking.IGridNode;
import appeng.api.networking.security.MachineSource;
import appeng.api.storage.IMEMonitor;
import appeng.api.util.AECableType;
import appeng.api.util.AEColor;
import appeng.api.util.DimensionalCoord;
import appeng.me.GridAccessException;
import appeng.me.helpers.AENetworkProxy;
import appeng.me.helpers.IGridProxyable;
import cn.dancingsnow.appeu.hatch.transfer.EnergyPort;
import cn.dancingsnow.appeu.storage.EUStack;
import cn.dancingsnow.appeu.storage.EUStackType;
import gregtech.api.enums.Dyes;
import gregtech.api.interfaces.tileentity.IGregTechTileEntity;
import gregtech.api.metatileentity.BaseMetaTileEntity;

public final class MEHatchConnection {

    private final IGridProxyable owner;
    private final Supplier<IGregTechTileEntity> baseTileSupplier;
    private final Supplier<ItemStack> visualRepresentationSupplier;

    private AENetworkProxy proxy;
    private MachineSource actionSource;
    private EnergyPort cachedEnergyPort;

    public MEHatchConnection(IGridProxyable owner, Supplier<IGregTechTileEntity> baseTileSupplier,
        Supplier<ItemStack> visualRepresentationSupplier) {
        this.owner = Objects.requireNonNull(owner, "owner");
        this.baseTileSupplier = Objects.requireNonNull(baseTileSupplier, "baseTileSupplier");
        this.visualRepresentationSupplier = Objects
            .requireNonNull(visualRepresentationSupplier, "visualRepresentationSupplier");
    }

    public AENetworkProxy getProxy() {
        if (proxy == null) {
            proxy = new AENetworkProxy(
                owner,
                "proxy",
                Objects.requireNonNull(visualRepresentationSupplier.get(), "visualRepresentation"),
                true);
            proxy.setFlags(GridFlags.REQUIRE_CHANNEL);
            proxy.setIdlePowerUsage(1.0);
            updateValidSides(proxy);
        }
        return proxy;
    }

    @Nullable
    public IGridNode getGridNode(ForgeDirection side) {
        return getProxy().getNode();
    }

    public DimensionalCoord getLocation() {
        IGregTechTileEntity baseTile = getBaseTile();
        if (baseTile == null) {
            throw new IllegalStateException("ME hatch has no base tile");
        }
        return new DimensionalCoord(
            baseTile.getWorld(),
            baseTile.getXCoord(),
            baseTile.getYCoord(),
            baseTile.getZCoord());
    }

    public AECableType getCableConnectionType(ForgeDirection side) {
        IGregTechTileEntity baseTile = getBaseTile();
        return baseTile != null && side == baseTile.getFrontFacing() ? AECableType.SMART : AECableType.NONE;
    }

    public void securityBreak() {}

    public void onFirstTick() {
        updateValidSides();
        getProxy().onReady();
    }

    public void onFacingChange() {
        updateValidSides();
    }

    public void onColorChangeServer(byte color) {
        cachedEnergyPort = null;
        AENetworkProxy networkProxy = getProxy();
        networkProxy.setColor(color == -1 ? AEColor.Transparent : AEColor.values()[Dyes.transformDyeIndex(color)]);
        IGridNode node = networkProxy.getNode();
        if (node != null) {
            node.updateState();
        }
    }

    public void saveNBTData(NBTTagCompound tag) {
        getProxy().writeToNBT(tag);
    }

    public void loadNBTData(NBTTagCompound tag) {
        cachedEnergyPort = null;
        if (tag.hasKey("proxy")) {
            getProxy().readFromNBT(tag);
        }
    }

    public void gridChanged() {
        cachedEnergyPort = null;
    }

    public boolean isPowered() {
        return getProxy().isPowered();
    }

    public boolean isActive() {
        return getProxy().isActive();
    }

    @Nullable
    public EnergyPort energyPort() {
        AENetworkProxy networkProxy = getProxy();
        if (!networkProxy.isActive()) {
            cachedEnergyPort = null;
            return null;
        }

        if (cachedEnergyPort != null) {
            return cachedEnergyPort;
        }

        MachineSource source = getActionSource();
        if (source == null) {
            return null;
        }

        try {
            IMEMonitor<?> monitor = networkProxy.getStorage()
                .getMEMonitor(EUStackType.INSTANCE);
            if (monitor == null || monitor.getStackType() != EUStackType.INSTANCE) {
                return null;
            }

            @SuppressWarnings("unchecked")
            IMEMonitor<EUStack> euMonitor = (IMEMonitor<EUStack>) monitor;
            cachedEnergyPort = new AEGridEnergyPort(euMonitor, source);
            return cachedEnergyPort;
        } catch (GridAccessException ignored) {
            cachedEnergyPort = null;
            return null;
        }
    }

    private void updateValidSides() {
        updateValidSides(getProxy());
    }

    private void updateValidSides(AENetworkProxy networkProxy) {
        cachedEnergyPort = null;
        IGregTechTileEntity baseTile = getBaseTile();
        EnumSet<ForgeDirection> validSides = baseTile == null ? EnumSet.noneOf(ForgeDirection.class)
            : EnumSet.of(baseTile.getFrontFacing());
        networkProxy.setValidSides(validSides);
    }

    @Nullable
    private IGregTechTileEntity getBaseTile() {
        return baseTileSupplier.get();
    }

    @Nullable
    private MachineSource getActionSource() {
        if (actionSource == null) {
            IGregTechTileEntity baseTile = getBaseTile();
            if (baseTile instanceof BaseMetaTileEntity) {
                actionSource = new MachineSource((BaseMetaTileEntity) baseTile);
            }
        }
        return actionSource;
    }
}
