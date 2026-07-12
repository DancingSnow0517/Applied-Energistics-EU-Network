package cn.dancingsnow.appeu.storage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import net.minecraft.nbt.NBTTagCompound;

import org.junit.jupiter.api.Test;

import appeng.api.config.FuzzyMode;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

class EUStackTest {

    @Test
    void copyIsIndependent() {
        EUStack original = new EUStack(10);
        EUStack copy = original.copy();

        copy.incStackSize(5);

        assertNotSame(original, copy);
        assertEquals(10, original.getStackSize());
        assertEquals(15, copy.getStackSize());
    }

    @Test
    void supportsCheckedAmountMutation() {
        EUStack stack = new EUStack(10);

        stack.add(new EUStack(7));
        stack.incStackSize(5);
        stack.decStackSize(2);

        assertEquals(20, stack.getStackSize());
        assertEquals(
            0,
            stack.reset()
                .getStackSize());
    }

    @Test
    void supportsSignedNetworkChangeDeltas() {
        EUStack delta = new EUStack(-40);

        assertTrue(delta.isMeaningful());
        assertEquals(-40, delta.getStackSize());
        assertEquals(
            -20,
            delta.setStackSize(-20)
                .getStackSize());

        delta.incStackSize(5);
        assertEquals(-15, delta.getStackSize());
        delta.decStackSize(-5);
        assertEquals(-10, delta.getStackSize());
        delta.add(new EUStack(15));
        assertEquals(5, delta.getStackSize());
    }

    @Test
    void keepsCheckedArithmeticForSignedAmounts() {
        assertThrows(ArithmeticException.class, () -> new EUStack(Long.MAX_VALUE).incStackSize(1));
        assertThrows(ArithmeticException.class, () -> new EUStack(Long.MIN_VALUE).decStackSize(1));
        assertThrows(ArithmeticException.class, () -> new EUStack(Long.MAX_VALUE).add(new EUStack(1)));
    }

    @Test
    void roundTripsLongAmountThroughNbt() {
        long amount = (long) Integer.MAX_VALUE + 42;
        NBTTagCompound tag = new NBTTagCompound();

        new EUStack(amount).writeToNBT(tag);

        assertEquals(
            amount,
            EUStack.fromNBT(tag)
                .getStackSize());
    }

    @Test
    void roundTripsNegativeDeltaThroughNbt() {
        long amount = -((long) Integer.MAX_VALUE + 42);
        NBTTagCompound tag = new NBTTagCompound();

        new EUStack(amount).writeToNBT(tag);

        assertEquals(
            amount,
            EUStack.fromNBT(tag)
                .getStackSize());
    }

    @Test
    void roundTripsLongAmountThroughPacket() throws Exception {
        long amount = (long) Integer.MAX_VALUE + 42;
        ByteBuf buffer = Unpooled.buffer();

        new EUStack(amount).writeToPacket(buffer);

        assertEquals(
            amount,
            EUStack.fromPacket(buffer)
                .getStackSize());
        assertEquals(0, buffer.readableBytes());
    }

    @Test
    void roundTripsNegativeDeltaThroughPacket() throws Exception {
        long amount = -((long) Integer.MAX_VALUE + 42);
        ByteBuf buffer = Unpooled.buffer();

        new EUStack(amount).writeToPacket(buffer);

        assertEquals(
            amount,
            EUStack.fromPacket(buffer)
                .getStackSize());
        assertEquals(0, buffer.readableBytes());
    }

    @Test
    void equalityRepresentsTheSingleEuIdentity() {
        EUStack small = new EUStack(1);
        EUStack large = new EUStack(1_000_000);

        assertEquals(small, large);
        assertEquals(small.hashCode(), large.hashCode());
        assertTrue(small.isSameType(large));
        assertTrue(small.isSameType((Object) large));
        assertTrue(small.fuzzyComparison(large, FuzzyMode.IGNORE_ALL));
        assertFalse(small.isSameType((Object) "EU"));
    }

    @Test
    void zeroAmountRemainsNonMeaningful() {
        assertFalse(new EUStack(0).isMeaningful());
        assertTrue(new EUStack(1).isMeaningful());
        assertFalse(
            new EUStack(0).setCraftable(true)
                .isMeaningful());
        assertFalse(
            new EUStack(0).setCountRequestable(10)
                .isMeaningful());
    }

    @Test
    void unsupportedAeStateAndTagsRemainEmpty() {
        EUStack stack = new EUStack(3);

        stack.incCountRequestable(5);
        stack.decCountRequestable(2);

        assertEquals(0, stack.getCountRequestable());
        assertEquals(0, stack.getCountRequestableCrafts());
        assertEquals(0, stack.getUsedPercent());
        assertFalse(stack.isCraftable());
        assertFalse(stack.hasTagCompound());
        assertNull(stack.getTagCompound());
        assertFalse(stack.isItem());
        assertFalse(stack.isFluid());
        assertNull(stack.getChannel());
        assertEquals(1, stack.getAmountPerUnit());
        assertEquals(
            0,
            stack.empty()
                .getStackSize());
    }
}
