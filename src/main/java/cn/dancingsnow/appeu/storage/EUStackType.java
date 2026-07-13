package cn.dancingsnow.appeu.storage;

import java.io.IOException;
import java.util.Optional;

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
import gregtech.api.util.GTModHandler;
import ic2.api.item.ElectricItem;
import io.netty.buffer.ByteBuf;
import it.unimi.dsi.fastutil.objects.ObjectLongImmutablePair;
import it.unimi.dsi.fastutil.objects.ObjectLongPair;

public final class EUStackType implements IAEStackType<EUStack> {

    public static final EUStackType INSTANCE = new EUStackType();

    private static final ResourceLocation BUTTON_TEXTURE = new ResourceLocation("appeu", "textures/gui/eu_icon.png");
    private static final long MAX_EXACT_MANAGER_TRANSFER = 1L << 53;

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
        return getChargeState(container) != null;
    }

    @Override
    public @Nullable EUStack getStackFromContainerItem(@NotNull ItemStack container) {
        ChargeState state = getChargeState(container);
        return state == null || state.rawCharge < 0 ? null : new EUStack(state.charge);
    }

    @Override
    public @Nullable EUStack convertStackFromItem(@NotNull ItemStack itemStack) {
        return null;
    }

    @Override
    public long getContainerItemCapacity(@NotNull ItemStack container, @NotNull EUStack stack) {
        ChargeState state = getChargeState(container);
        return state == null ? 0 : state.capacity;
    }

    @Override
    public @NotNull ObjectLongPair<ItemStack> drainStackFromContainer(@NotNull ItemStack container,
        @NotNull EUStack stack) {
        ItemStack drained = copySingle(container);
        ChargeState state = getChargeState(drained);
        long offered = stack.getStackSize();
        if (state == null || offered <= 0) {
            return new ObjectLongImmutablePair<>(drained, 0);
        }

        long requested = Math.min(offered, state.charge);
        return transfer(drained, requested, false);
    }

    @Override
    public @Nullable ItemStack clearFilledContainer(@NotNull ItemStack container) {
        ItemStack cleared = copySingle(container);
        ChargeState state = getChargeState(cleared);
        if (state == null || state.rawCharge < 0 || state.rawCharge > state.capacity) {
            return null;
        }
        if (state.charge == 0) {
            return cleared;
        }

        ObjectLongPair<ItemStack> result = transfer(cleared, state.charge, false);
        return result.rightLong() == state.charge ? result.left() : null;
    }

    @Override
    public @NotNull ObjectLongPair<ItemStack> fillContainer(@NotNull ItemStack container, @NotNull EUStack stack) {
        ItemStack filled = copySingle(container);
        ChargeState state = getChargeState(filled);
        long offered = stack.getStackSize();
        if (state == null || offered <= 0) {
            return new ObjectLongImmutablePair<>(filled, 0);
        }

        long requested = Math.min(offered, state.capacity - state.charge);
        return transfer(filled, requested, true);
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

    private static ItemStack copySingle(ItemStack container) {
        ItemStack copy = container.copy();
        copy.stackSize = 1;
        return copy;
    }

    private ObjectLongPair<ItemStack> transfer(ItemStack container, long requested, boolean charging) {
        long remaining = Math.max(0, requested);
        long transferred = 0;
        while (remaining > 0) {
            ChargeState before = getChargeState(container);
            if (before == null || before.rawCharge < 0 || before.rawCharge > before.capacity) {
                break;
            }

            long available = charging ? before.capacity - before.charge : before.charge;
            long chunk = Math.min(Math.min(remaining, available), MAX_EXACT_MANAGER_TRANSFER);
            if (chunk <= 0) {
                break;
            }

            ItemStack candidate = copySingle(container);
            if (charging) {
                ElectricItem.manager.charge(candidate, chunk, Integer.MAX_VALUE, true, false);
            } else {
                ElectricItem.manager.discharge(candidate, chunk, Integer.MAX_VALUE, true, false, false);
            }

            ChargeState after = getChargeState(candidate);
            if (after == null || after.rawCharge < 0 || after.rawCharge > after.capacity) {
                break;
            }

            long delta = charging ? after.rawCharge - before.rawCharge : before.rawCharge - after.rawCharge;
            if (delta <= 0 || delta > chunk) {
                break;
            }
            boolean crossedSaturatedLongBoundary = !charging && before.rawCharge == Long.MAX_VALUE
                && delta == chunk - 1;

            container = candidate;
            transferred += delta;
            remaining -= delta;
            if (delta < chunk && !crossedSaturatedLongBoundary) {
                break;
            }
        }
        return new ObjectLongImmutablePair<>(container, transferred);
    }

    private @Nullable ChargeState getChargeState(@Nullable ItemStack container) {
        if (container == null || !GTModHandler.isElectricItem(container)) {
            return null;
        }

        Optional<Long[]> chargeResult = GTModHandler.getElectricItemCharge(container);
        Long[] charge = chargeResult.orElse(null);
        if (charge == null || charge.length < 2 || charge[0] == null || charge[1] == null || charge[1] <= 0) {
            return null;
        }

        long capacity = charge[1];
        long rawCharge = charge[0];
        long currentCharge = Math.max(0, Math.min(rawCharge, capacity));
        return new ChargeState(rawCharge, currentCharge, capacity);
    }

    private static final class ChargeState {

        private final long rawCharge;
        private final long charge;
        private final long capacity;

        private ChargeState(long rawCharge, long charge, long capacity) {
            this.rawCharge = rawCharge;
            this.charge = charge;
            this.capacity = capacity;
        }
    }
}
