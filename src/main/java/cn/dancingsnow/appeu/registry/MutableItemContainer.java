package cn.dancingsnow.appeu.registry;

import static gregtech.api.util.GTRecipeBuilder.WILDCARD;

import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import gregtech.api.interfaces.IItemContainer;
import gregtech.api.util.GTModHandler;
import gregtech.api.util.GTOreDictUnificator;

final class MutableItemContainer implements IItemContainer {

    private ItemStack stack;
    private boolean set;

    @Override
    public Item getItem() {
        requireSet();
        return isInvalid(stack) ? null : stack.getItem();
    }

    @Override
    public Block getBlock() {
        requireSet();
        return isInvalid(stack) ? null : Block.getBlockFromItem(stack.getItem());
    }

    @Override
    public boolean isStackEqual(Object candidate) {
        return isStackEqual(candidate, false, false);
    }

    @Override
    public boolean isStackEqual(Object candidate, boolean wildcard, boolean ignoreNbt) {
        if (!(candidate instanceof ItemStack candidateStack) || isInvalid(candidateStack)) {
            return false;
        }
        ItemStack expected = get(1);
        if (candidateStack.getItem() != expected.getItem()) {
            return false;
        }
        if (!wildcard && candidateStack.getItemDamage() != expected.getItemDamage()) {
            return false;
        }
        return ignoreNbt || ItemStack.areItemStackTagsEqual(candidateStack, expected);
    }

    @Override
    public ItemStack get(long amount, Object... replacements) {
        requireSet();
        return copy(amount, stack);
    }

    @Override
    public ItemStack getWildcard(long amount, Object... replacements) {
        requireSet();
        return copyWithMeta(amount, WILDCARD);
    }

    @Override
    public ItemStack getUndamaged(long amount, Object... replacements) {
        requireSet();
        return copyWithMeta(amount, 0);
    }

    @Override
    public ItemStack getAlmostBroken(long amount, Object... replacements) {
        requireSet();
        return copyWithMeta(amount, stack.getMaxDamage() - 1L);
    }

    @Override
    public ItemStack getWithDamage(long amount, long metaValue, Object... replacements) {
        requireSet();
        return copyWithMeta(amount, metaValue);
    }

    @Override
    public IItemContainer set(Item item) {
        set = true;
        stack = item == null ? null : new ItemStack(item, 1, 0);
        return this;
    }

    @Override
    public IItemContainer set(ItemStack itemStack) {
        set = true;
        stack = copy(1, itemStack);
        return this;
    }

    @Override
    public IItemContainer registerOre(Object... oreNames) {
        requireSet();
        for (Object oreName : oreNames) {
            GTOreDictUnificator.registerOre(oreName, get(1));
        }
        return this;
    }

    @Override
    public IItemContainer registerWildcardAsOre(Object... oreNames) {
        requireSet();
        for (Object oreName : oreNames) {
            GTOreDictUnificator.registerOre(oreName, getWildcard(1));
        }
        return this;
    }

    @Override
    public ItemStack getWithCharge(long amount, int energy, Object... replacements) {
        ItemStack charged = get(1, replacements);
        if (isInvalid(charged)) {
            return null;
        }
        GTModHandler.chargeElectricItem(charged, energy, Integer.MAX_VALUE, true, false);
        return copy(amount, charged);
    }

    @Override
    public ItemStack getWithName(long amount, String displayName, Object... replacements) {
        ItemStack named = get(1, replacements);
        if (isInvalid(named)) {
            return null;
        }
        named.setStackDisplayName(displayName);
        return copy(amount, named);
    }

    @Override
    public boolean hasBeenSet() {
        return set;
    }

    private void requireSet() {
        if (!set) {
            throw new IllegalStateException("Item container has not been set");
        }
    }

    private ItemStack copyWithMeta(long amount, long metaValue) {
        ItemStack copy = copy(amount, stack);
        if (copy != null) {
            copy.setItemDamage((int) metaValue);
        }
        return copy;
    }

    private static ItemStack copy(long amount, ItemStack source) {
        if (isInvalid(source)) {
            return null;
        }
        ItemStack copy = source.copy();
        copy.stackSize = (int) amount;
        return copy;
    }

    private static boolean isInvalid(ItemStack itemStack) {
        return itemStack == null || itemStack.getItem() == null || itemStack.stackSize <= 0;
    }
}
