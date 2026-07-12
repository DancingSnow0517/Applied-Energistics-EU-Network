package cn.dancingsnow.appeu.hatch;

import static com.gtnewhorizon.gtnhlib.util.numberformatting.NumberFormatUtil.formatNumber;
import static gregtech.api.enums.GTValues.V;

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
import gregtech.api.metatileentity.implementations.MTEHatchEnergy;

@IMetaTileEntity.SkipGenerateDescription
public class MTEHatchMEEnergy extends MTEHatchEnergy implements IGridProxyable, IMEConnectable, IPowerChannelState {

    private static final int AMPERAGE = 2;

    private final MEHatchConnection connection = new MEHatchConnection(
        this,
        this::getBaseMetaTileEntity,
        () -> getStackForm(1));

    public MTEHatchMEEnergy(int id, String name, String regionalName, int tier) {
        super(id, name, regionalName, validateTier(tier));
    }

    public MTEHatchMEEnergy(String name, int tier, String[] description, ITexture[][][] textures) {
        super(name, validateTier(tier), description, textures);
    }

    @Override
    public MetaTileEntity newMetaEntity(IGregTechTileEntity baseMetaTileEntity) {
        return new MTEHatchMEEnergy(mName, mTier, mDescriptionArray, mTextures);
    }

    @Override
    public boolean isEnetInput() {
        return false;
    }

    @Override
    public long maxAmperesIn() {
        return AMPERAGE;
    }

    @Override
    public long maxWorkingAmperesIn() {
        return AMPERAGE;
    }

    @Override
    public long maxEUStore() {
        return Math.addExact(512L, Math.multiplyExact(V[mTier], 8L));
    }

    @Override
    public void onPostTick(IGregTechTileEntity baseMetaTileEntity, long tick) {
        super.onPostTick(baseMetaTileEntity, tick);
        if (!baseMetaTileEntity.isServerSide()) {
            return;
        }

        EnergyPort port = connection.energyPort();
        if (port != null) {
            long stored = getEUVar();
            long limit = Math.multiplyExact(V[mTier], (long) AMPERAGE);
            long moved = EnergyTransfer.pull(port, stored, maxEUStore(), limit);
            if (moved > 0) {
                setEUVar(Math.addExact(stored, moved));
            }
        }
        if (tick % 20 == 0) {
            baseMetaTileEntity.setActive(connection.isActive());
        }
    }

    @Override
    public String[] getDescription() {
        return MTEHatch
            .formatEnergyInfoDesc(false, mTier, AMPERAGE, "appeu.hatch.energy.desc", formatNumber(maxEUStore()));
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
    public void gridChanged() {}

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
        if (tier < 0 || tier >= V.length - 1) {
            throw new IllegalArgumentException("tier must have matching GT voltage and overlay entries: " + tier);
        }
        return tier;
    }
}
