package cn.dancingsnow.appeu.storage;

import java.io.IOException;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.IIcon;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.StatCollector;

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

    private static final ResourceLocation BUTTON_TEXTURE = new ResourceLocation("appeu", "textures/gui/eu_icon.png");

    private final EUStack testStack = new EUStack(1);

    private EUStackType() {}

    @Override
    public String getId() {
        return EUConstants.STACK_TYPE_ID;
    }

    @Override
    public String getDisplayName() {
        return StatCollector.translateToLocal("appeu.stack.eu");
    }

    @Override
    public String getDisplayUnit() {
        return "EU";
    }

    @Override
    public EUStack loadStackFromNBT(NBTTagCompound tag) {
        return EUStack.fromNBT(tag);
    }

    @Override
    public EUStack loadStackFromByte(ByteBuf buffer) throws IOException {
        return EUStack.fromPacket(buffer);
    }

    @Override
    public IItemList<EUStack> createList() {
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
        return new IIcon() {

            @Override
            public int getIconWidth() {
                return 16;
            }

            @Override
            public int getIconHeight() {
                return 16;
            }

            @Override
            public float getMinU() {
                return 0;
            }

            @Override
            public float getMaxU() {
                return 1;
            }

            @Override
            public float getInterpolatedU(double p_94214_1_) {
                return 0;
            }

            @Override
            public float getMinV() {
                return 0;
            }

            @Override
            public float getMaxV() {
                return 1;
            }

            @Override
            public float getInterpolatedV(double p_94207_1_) {
                return 0;
            }

            @Override
            public String getIconName() {
                return "EUIcon";
            }
        };
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
