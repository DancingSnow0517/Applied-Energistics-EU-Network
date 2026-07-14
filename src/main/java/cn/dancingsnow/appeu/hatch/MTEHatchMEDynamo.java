package cn.dancingsnow.appeu.hatch;

import static gregtech.api.enums.GTValues.V;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.util.ForgeDirection;

import appeng.api.implementations.IPowerChannelState;
import appeng.api.networking.IGridNode;
import appeng.api.util.AECableType;
import appeng.api.util.DimensionalCoord;
import appeng.me.helpers.AENetworkProxy;
import appeng.me.helpers.IGridProxyable;
import cn.dancingsnow.appeu.client.AppEUTextures;
import cn.dancingsnow.appeu.hatch.transfer.EnergyPort;
import cn.dancingsnow.appeu.hatch.transfer.EnergyTransfer;
import cn.dancingsnow.appeu.hatch.transfer.MEHatchTransferPolicy;
import gregtech.api.interfaces.IHideTooltipEnergyInfo;
import gregtech.api.interfaces.IMEConnectable;
import gregtech.api.interfaces.ITexture;
import gregtech.api.interfaces.metatileentity.IMetaTileEntity;
import gregtech.api.interfaces.tileentity.IGregTechTileEntity;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.implementations.MTEHatchDynamo;
import gregtech.api.util.GTSplit;
import gregtech.api.util.GTUtility;
import gregtech.api.util.tooltip.TooltipHelper;

@IMetaTileEntity.SkipGenerateDescription
public class MTEHatchMEDynamo extends MTEHatchDynamo
    implements IGridProxyable, IMEConnectable, IPowerChannelState, IHideTooltipEnergyInfo {

    private final int fixedAmperage;
    private final MEHatchConnection connection = new MEHatchConnection(
        this,
        this::getBaseMetaTileEntity,
        () -> getStackForm(1));

    public MTEHatchMEDynamo(int id, String name, String regionalName, int tier, int amperage) {
        super(id, name, regionalName, validate(tier, amperage));
        fixedAmperage = amperage;
    }

    public MTEHatchMEDynamo(String name, int tier, int amperage, String[] description, ITexture[][][] textures) {
        super(name, validate(tier, amperage), description, textures);
        fixedAmperage = amperage;
    }

    @Override
    public MetaTileEntity newMetaEntity(IGregTechTileEntity baseMetaTileEntity) {
        return new MTEHatchMEDynamo(mName, mTier, fixedAmperage, mDescriptionArray, mTextures);
    }

    @Override
    public ITexture[] getTexturesActive(ITexture baseTexture) {
        return new ITexture[] { baseTexture, AppEUTextures.BlockIcons.OVERLAYS_ME_ENERGY_OUT_TEXTURE };
    }

    @Override
    public ITexture[] getTexturesInactive(ITexture baseTexture) {
        return getTexturesActive(baseTexture);
    }

    @Override
    public boolean isEnetOutput() {
        return false;
    }

    @Override
    public long maxAmperesOut() {
        return fixedAmperage;
    }

    @Override
    public long maxEUStore() {
        return MEHatchTransferPolicy.bufferCapacity(V[mTier], fixedAmperage);
    }

    @Override
    public void onPostTick(IGregTechTileEntity baseMetaTileEntity, long tick) {
        super.onPostTick(baseMetaTileEntity, tick);
        if (!baseMetaTileEntity.isServerSide()) {
            return;
        }

        if (MEHatchTransferPolicy.shouldTransfer(tick)) {
            long stored = getEUVar();
            if (stored > 0) {
                EnergyPort port = connection.energyPort();
                if (port != null) {
                    long moved = EnergyTransfer.push(port, stored, stored);
                    if (moved > 0) {
                        setEUVar(Math.subtractExact(stored, moved));
                    }
                }
            }
        }
        if (tick % 20 == 0) {
            baseMetaTileEntity.setActive(connection.isActive());
        }
    }

    @Override
    public String[] getDescription() {
        return GTSplit.splitLocalizedWithSuffix(
            "appeu.hatch.dynamo.desc",
            new String[] { GTUtility.translate("gt.tileentity.eup_out", TooltipHelper.voltageText(maxEUOutput())) });
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

    private static int validate(int tier, int amperage) {
        if (tier < 0 || tier >= V.length - 1) {
            throw new IllegalArgumentException("tier must have matching GT voltage and overlay entries: " + tier);
        }
        if (amperage != 2 && amperage != 4) {
            throw new IllegalArgumentException("standard ME dynamo amperage must be 2 or 4: " + amperage);
        }
        return tier;
    }
}
