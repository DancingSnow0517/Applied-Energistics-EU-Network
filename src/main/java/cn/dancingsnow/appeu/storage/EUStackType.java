package cn.dancingsnow.appeu.storage;

import java.io.IOException;

import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.IIcon;
import net.minecraft.util.ResourceLocation;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import appeng.api.storage.data.IAEStackType;
import appeng.api.storage.data.IItemList;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import io.netty.buffer.ByteBuf;
import it.unimi.dsi.fastutil.objects.ObjectLongImmutablePair;
import it.unimi.dsi.fastutil.objects.ObjectLongPair;

public final class EUStackType implements IAEStackType<EUStack> {

    public static final EUStackType INSTANCE = new EUStackType();

    private static final ResourceLocation BUTTON_TEXTURE = new ResourceLocation("appeu", "textures/gui/eu_stack.png");

    private final EUStack testStack = new EUStack(1);
    private IIcon icon;

    private EUStackType() {}

    @Override
    public String getId() {
        return EUConstants.STACK_TYPE_ID;
    }

    @Override
    public String getDisplayName() {
        return "EU Energy";
    }

    @Override
    public String getDisplayUnit() {
        return "EU";
    }

    @Override
    public EUStack loadStackFromNBT(NBTTagCompound tag) {
        return new EUStack(tag.getLong(EUConstants.NBT_AMOUNT));
    }

    @Override
    public EUStack loadStackFromByte(ByteBuf buffer) throws IOException {
        return new EUStack(buffer.readLong());
    }

    @Override
    public IItemList<EUStack> createList() {
        return new EUStackList();
    }

    @Override
    public IItemList<EUStack> createPrimitiveList() {
        return new EUStackList();
    }

    @Override
    public int getAmountPerUnit() {
        return 1;
    }

    @Override
    public boolean isContainerItemForType(@Nullable ItemStack container) {
        return false;
    }

    @Override
    public @Nullable EUStack getStackFromContainerItem(@NotNull ItemStack container) {
        return null;
    }

    @Override
    public @Nullable EUStack convertStackFromItem(@NotNull ItemStack itemStack) {
        return null;
    }

    @Override
    public long getContainerItemCapacity(@NotNull ItemStack container, @NotNull EUStack stack) {
        return 0;
    }

    @Override
    public @NotNull ObjectLongPair<ItemStack> drainStackFromContainer(@NotNull ItemStack container,
        @NotNull EUStack stack) {
        return new ObjectLongImmutablePair<>(container, 0);
    }

    @Override
    public @Nullable ItemStack clearFilledContainer(@NotNull ItemStack container) {
        return null;
    }

    @Override
    public @NotNull ObjectLongPair<ItemStack> fillContainer(@NotNull ItemStack container, @NotNull EUStack stack) {
        return new ObjectLongImmutablePair<>(container, 0);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public ResourceLocation getButtonTexture() {
        return BUTTON_TEXTURE;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public IIcon getButtonIcon() {
        return icon;
    }

    @SideOnly(Side.CLIENT)
    public void registerIcon(IIconRegister iconRegister) {
        icon = iconRegister.registerIcon("appeu:eu_stack");
    }

    @Override
    public EUStack getTestStack() {
        return testStack.copy();
    }

    @Override
    public int getAmountPerByte() {
        return Math.toIntExact(EUConstants.EU_PER_BYTE);
    }
}
