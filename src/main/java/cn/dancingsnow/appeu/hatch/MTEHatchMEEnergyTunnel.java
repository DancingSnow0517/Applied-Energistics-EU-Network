package cn.dancingsnow.appeu.hatch;

import static com.gtnewhorizon.gtnhlib.util.numberformatting.NumberFormatUtil.formatNumber;
import static gregtech.api.enums.GTValues.V;
import static net.minecraft.util.StatCollector.translateToLocal;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.util.ForgeDirection;

import appeng.api.implementations.IPowerChannelState;
import appeng.api.networking.IGridNode;
import appeng.api.util.AECableType;
import appeng.api.util.DimensionalCoord;
import appeng.me.helpers.AENetworkProxy;
import appeng.me.helpers.IGridProxyable;
import cn.dancingsnow.appeu.hatch.transfer.EnergyPort;
import cn.dancingsnow.appeu.hatch.transfer.EnergyTransfer;
import gregtech.api.interfaces.IMEConnectable;
import gregtech.api.interfaces.ITexture;
import gregtech.api.interfaces.metatileentity.IMetaTileEntity;
import gregtech.api.interfaces.tileentity.IGregTechTileEntity;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.implementations.MTEHatch;
import tectech.thing.metaTileEntity.hatch.MTEHatchEnergyTunnel;

@IMetaTileEntity.SkipGenerateDescription
public class MTEHatchMEEnergyTunnel extends MTEHatchEnergyTunnel
    implements IGridProxyable, IMEConnectable, IPowerChannelState {

    private static final int MIN_TIER = 5;
    private static final int MAX_TIER = 13;
    private static final int MIN_AMPERAGE = 256;

    private final MEHatchConnection connection = new MEHatchConnection(
        this,
        this::getBaseMetaTileEntity,
        () -> getStackForm(1));

    public MTEHatchMEEnergyTunnel(int id, String name, String regionalName, int tier, int maximumAmperage) {
        super(id, name, regionalName, validateTier(tier), validateAmperage(maximumAmperage));
    }

    public MTEHatchMEEnergyTunnel(String name, int tier, int maximumAmperage, String[] description,
        ITexture[][][] textures) {
        super(name, validateTier(tier), validateAmperage(maximumAmperage), description, textures);
    }

    @Override
    public MetaTileEntity newMetaEntity(IGregTechTileEntity baseMetaTileEntity) {
        return new MTEHatchMEEnergyTunnel(mName, mTier, maxAmperes, mDescriptionArray, mTextures);
    }

    @Override
    public void setAmperes(int amperage) {
        Amperes = Math.max(1, Math.min(amperage, maxAmperes));
    }

    @Override
    public long maxEUStore() {
        return Math.multiplyExact(Math.multiplyExact(V[mTier], 24L), (long) getAmperes());
    }

    @Override
    public boolean canConnect(ForgeDirection side) {
        return false;
    }

    @Override
    public void onPostTick(IGregTechTileEntity baseMetaTileEntity, long tick) {
        if (!baseMetaTileEntity.isServerSide()) {
            return;
        }

        long stored = getEUVar();
        long capacity = maxEUStore();
        if (stored < capacity) {
            EnergyPort port = connection.energyPort();
            if (port != null) {
                long limit = Math.multiplyExact(V[mTier], (long) getAmperes());
                long moved = EnergyTransfer.pull(port, stored, capacity, limit);
                if (moved > 0) {
                    setEUVar(Math.addExact(stored, moved));
                }
            }
        }
        if (tick % 20 == 0) {
            baseMetaTileEntity.setActive(connection.isActive());
        }
    }

    @Override
    public String[] getDescription() {
        return MTEHatch.formatEnergyInfoDesc(
            translateToLocal("gt.blockmachines.hatch.screwdrivertooltip"),
            false,
            mTier,
            maxAmperes,
            "appeu.hatch.energy_laser.desc",
            formatNumber(maxEUStore()));
    }

    @Override
    public AENetworkProxy getProxy() {
        return connection.getProxy();
    }

    @Override
    public IGridNode getGridNode(ForgeDirection side) {
        return connection.getGridNode(side);
    }

    @Override
    public DimensionalCoord getLocation() {
        return connection.getLocation();
    }

    @Override
    public AECableType getCableConnectionType(ForgeDirection side) {
        return connection.getCableConnectionType(side);
    }

    @Override
    public void securityBreak() {
        connection.securityBreak();
    }

    @Override
    public void gridChanged() {
        connection.gridChanged();
    }

    @Override
    public boolean connectsToAllSides() {
        return false;
    }

    @Override
    public void setConnectsToAllSides(boolean connects) {}

    @Override
    public void onFirstTick(IGregTechTileEntity baseMetaTileEntity) {
        super.onFirstTick(baseMetaTileEntity);
        connection.onFirstTick();
    }

    @Override
    public void onFacingChange() {
        super.onFacingChange();
        connection.onFacingChange();
    }

    @Override
    public void onColorChangeServer(byte color) {
        super.onColorChangeServer(color);
        connection.onColorChangeServer(color);
    }

    @Override
    public void saveNBTData(NBTTagCompound tag) {
        super.saveNBTData(tag);
        connection.saveNBTData(tag);
    }

    @Override
    public void loadNBTData(NBTTagCompound tag) {
        super.loadNBTData(tag);
        setAmperes(getAmperes());
        connection.loadNBTData(tag);
    }

    @Override
    public boolean isPowered() {
        return connection.isPowered();
    }

    @Override
    public boolean isActive() {
        return connection.isActive();
    }

    private static int validateTier(int tier) {
        if (tier < MIN_TIER || tier > MAX_TIER) {
            throw new IllegalArgumentException("ME laser hatch tier must be IV through UXV: " + tier);
        }
        return tier;
    }

    private static int validateAmperage(int amperage) {
        if (amperage < MIN_AMPERAGE) {
            throw new IllegalArgumentException("ME laser hatch amperage must be at least 256: " + amperage);
        }
        return amperage;
    }
}
