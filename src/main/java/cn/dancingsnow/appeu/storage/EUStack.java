package cn.dancingsnow.appeu.storage;

import java.io.IOException;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.IIcon;
import net.minecraft.world.World;

import org.lwjgl.opengl.GL11;

import appeng.api.config.FuzzyMode;
import appeng.api.storage.StorageChannel;
import appeng.api.storage.data.IAEStack;
import appeng.api.storage.data.IAEStackType;
import appeng.api.storage.data.IAETagCompound;
import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import io.netty.buffer.ByteBuf;

public final class EUStack implements IAEStack<EUStack> {

    private long amount;

    public EUStack(long amount) {
        setStackSize(amount);
    }

    @Override
    public void add(EUStack stack) {
        if (stack != null) {
            incStackSize(stack.amount);
        }
    }

    @Override
    public long getStackSize() {
        return amount;
    }

    @Override
    public EUStack setStackSize(long stackSize) {
        requireNonNegative(stackSize);
        amount = stackSize;
        return this;
    }

    @Override
    public long getCountRequestable() {
        return 0;
    }

    @Override
    public EUStack setCountRequestable(long countRequestable) {
        return this;
    }

    @Override
    public boolean isCraftable() {
        return false;
    }

    @Override
    public EUStack setCraftable(boolean isCraftable) {
        return this;
    }

    @Override
    public EUStack reset() {
        amount = 0;
        return this;
    }

    @Override
    public boolean isMeaningful() {
        return amount > 0;
    }

    @Override
    public void incStackSize(long delta) {
        requireNonNegative(delta);
        amount = Math.addExact(amount, delta);
    }

    @Override
    public void decStackSize(long delta) {
        requireNonNegative(delta);
        setStackSize(Math.subtractExact(amount, delta));
    }

    @Override
    public void incCountRequestable(long delta) {}

    @Override
    public void decCountRequestable(long delta) {}

    @Override
    public void writeToNBT(NBTTagCompound tag) {
        tag.setLong(EUConstants.NBT_AMOUNT, amount);
    }

    @Override
    public void writeToPacket(ByteBuf data) throws IOException {
        data.writeLong(amount);
    }

    @Override
    public boolean fuzzyComparison(Object stack, FuzzyMode mode) {
        return stack instanceof EUStack;
    }

    @Override
    public EUStack copy() {
        return new EUStack(amount);
    }

    @Override
    public EUStack empty() {
        return new EUStack(0);
    }

    @Override
    public IAETagCompound getTagCompound() {
        return null;
    }

    @Override
    public boolean isItem() {
        return false;
    }

    @Override
    public boolean isFluid() {
        return false;
    }

    @Override
    public StorageChannel getChannel() {
        return null;
    }

    @Override
    public String getLocalizedName() {
        return getDisplayName();
    }

    @Override
    public boolean isSameType(EUStack stack) {
        return stack != null;
    }

    @Override
    public boolean isSameType(Object stack) {
        return stack instanceof EUStack;
    }

    @Override
    public String getUnlocalizedName() {
        return "appeu.stack.eu";
    }

    @Override
    public String getDisplayName() {
        return "EU Energy";
    }

    @Override
    public String getModId() {
        return "appeu";
    }

    @Override
    public void setTagCompound(NBTTagCompound tag) {}

    @Override
    public boolean hasTagCompound() {
        return false;
    }

    @Override
    public ItemStack getItemStackForNEI() {
        Item item = GameRegistry.findItem("appeu", "eu_storage_component");
        return item == null ? null : new ItemStack(item, 1, 0);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void drawInGui(Minecraft mc, int x, int y) {
        drawIcon(mc, x, y);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void drawOverlayInGui(Minecraft mc, int x, int y, boolean showAmount, boolean showAmountAlways,
        boolean showCraftableText, boolean showCraftableIcon) {
        if (!showAmount || amount <= 0 || (amount == 1 && !showAmountAlways)) {
            return;
        }

        String text = Long.toString(amount);
        float scale = Math.min(1.0F, 16.0F / mc.fontRenderer.getStringWidth(text));
        GL11.glPushMatrix();
        GL11.glTranslatef(x + 17, y + 9, 200.0F);
        GL11.glScalef(scale, scale, 1.0F);
        mc.fontRenderer.drawStringWithShadow(text, -mc.fontRenderer.getStringWidth(text), 0, 0xFFFFFF);
        GL11.glPopMatrix();
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void drawOnBlockFace(World world) {
        drawIcon(Minecraft.getMinecraft(), 0, 0);
    }

    @SideOnly(Side.CLIENT)
    private static void drawIcon(Minecraft mc, int x, int y) {
        IIcon icon = EUStackType.INSTANCE.getButtonIcon();
        if (icon == null) {
            return;
        }

        mc.renderEngine.bindTexture(TextureMap.locationItemsTexture);
        GL11.glPushAttrib(GL11.GL_ENABLE_BIT | GL11.GL_COLOR_BUFFER_BIT | GL11.GL_CURRENT_BIT);
        GL11.glDisable(GL11.GL_LIGHTING);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);

        Tessellator tessellator = Tessellator.instance;
        tessellator.startDrawingQuads();
        tessellator.addVertexWithUV(x + 16, y, 0, icon.getMaxU(), icon.getMinV());
        tessellator.addVertexWithUV(x, y, 0, icon.getMinU(), icon.getMinV());
        tessellator.addVertexWithUV(x, y + 16, 0, icon.getMinU(), icon.getMaxV());
        tessellator.addVertexWithUV(x + 16, y + 16, 0, icon.getMaxU(), icon.getMaxV());
        tessellator.draw();
        GL11.glPopAttrib();
    }

    @Override
    public int getAmountPerUnit() {
        return 1;
    }

    @Override
    public IAEStackType<EUStack> getStackType() {
        return EUStackType.INSTANCE;
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof EUStack;
    }

    @Override
    public int hashCode() {
        return EUConstants.STACK_TYPE_ID.hashCode();
    }

    private static void requireNonNegative(long value) {
        if (value < 0) {
            throw new IllegalArgumentException("EU amount cannot be negative: " + value);
        }
    }
}
