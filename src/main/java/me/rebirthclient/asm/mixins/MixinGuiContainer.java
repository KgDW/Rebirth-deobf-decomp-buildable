package me.rebirthclient.asm.mixins;

import com.google.common.collect.Sets;
import java.awt.Color;
import java.util.Set;
import me.rebirthclient.api.managers.Managers;
import me.rebirthclient.api.util.render.RenderUtil;
import me.rebirthclient.mod.modules.impl.client.ClickGui;
import me.rebirthclient.mod.modules.impl.client.GuiAnimation;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.TextFormatting;
import org.lwjgl.opengl.GL11;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(value={GuiContainer.class})
public abstract class MixinGuiContainer
extends GuiScreen {
    @Shadow
    public Container inventorySlots;
    @Shadow
    protected int guiLeft;
    @Shadow
    protected int guiTop;
    @Shadow
    private Slot hoveredSlot;
    @Shadow
    private boolean isRightMouseClick;
    @Shadow
    private final ItemStack draggedStack = ItemStack.EMPTY;
    @Shadow
    private int touchUpX;
    @Shadow
    private int touchUpY;
    @Shadow
    private Slot returningStackDestSlot;
    @Shadow
    private long returningStackTime;
    @Shadow
    private ItemStack returningStack = ItemStack.EMPTY;
    @Final
    @Shadow
    protected final Set<Slot> dragSplittingSlots = Sets.newHashSet();
    @Shadow
    protected boolean dragSplitting;
    @Shadow
    private int dragSplittingRemnant;

    @Overwrite
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        ItemStack itemstack;
        if (ClickGui.INSTANCE.background.getValue() && this.mc.world != null) {
            RenderUtil.drawVGradientRect(0.0f, 0.0f, Managers.TEXT.scaledWidth, Managers.TEXT.scaledHeight, new Color(0, 0, 0, 0).getRGB(), Managers.COLORS.getCurrentWithAlpha(60));
        }
        float size = (float)GuiAnimation.inventoryFade.easeOutQuad();
        GlStateManager.pushMatrix();
        GL11.glScaled(size, size, size);
        int i = this.guiLeft;
        int j = this.guiTop;
        this.drawGuiContainerBackgroundLayer(partialTicks, mouseX, mouseY);
        GlStateManager.disableRescaleNormal();
        RenderHelper.disableStandardItemLighting();
        GlStateManager.disableLighting();
        GlStateManager.disableDepth();
        super.drawScreen(mouseX, mouseY, partialTicks);
        RenderHelper.enableGUIStandardItemLighting();
        GlStateManager.pushMatrix();
        GlStateManager.translate((float)i, (float)j, 0.0f);
        GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
        GlStateManager.enableRescaleNormal();
        this.hoveredSlot = null;
        OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240.0f, 240.0f);
        GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
        for (int i1 = 0; i1 < this.inventorySlots.inventorySlots.size(); ++i1) {
            Slot slot = this.inventorySlots.inventorySlots.get(i1);
            if (slot.isEnabled()) {
                this.drawSlot(slot);
            }
            if (!this.isMouseOverSlot(slot, mouseX, mouseY) || !slot.isEnabled()) continue;
            this.hoveredSlot = slot;
            GlStateManager.disableLighting();
            GlStateManager.disableDepth();
            int j1 = slot.xPos;
            int k1 = slot.yPos;
            GlStateManager.colorMask(true, true, true, false);
            this.drawGradientRect(j1, k1, j1 + 16, k1 + 16, -2130706433, -2130706433);
            GlStateManager.colorMask(true, true, true, true);
            GlStateManager.enableLighting();
            GlStateManager.enableDepth();
        }
        RenderHelper.disableStandardItemLighting();
        this.drawGuiContainerForegroundLayer(mouseX, mouseY);
        RenderHelper.enableGUIStandardItemLighting();
        InventoryPlayer inventoryplayer = this.mc.player.inventory;
        ItemStack itemStack = itemstack = this.draggedStack.isEmpty() ? inventoryplayer.getItemStack() : this.draggedStack;
        if (!itemstack.isEmpty()) {
            int k2 = this.draggedStack.isEmpty() ? 8 : 16;
            String s = null;
            if (!this.draggedStack.isEmpty() && this.isRightMouseClick) {
                itemstack = itemstack.copy();
                itemstack.setCount(MathHelper.ceil((float)itemstack.getCount() / 2.0f));
            } else if (this.dragSplitting && this.dragSplittingSlots.size() > 1) {
                itemstack = itemstack.copy();
                itemstack.setCount(this.dragSplittingRemnant);
                if (itemstack.isEmpty()) {
                    s = TextFormatting.YELLOW + "0";
                }
            }
            this.drawItemStack(itemstack, mouseX - i - 8, mouseY - j - k2, s);
        }
        if (!this.returningStack.isEmpty()) {
            float f = (float)(Minecraft.getSystemTime() - this.returningStackTime) / 100.0f;
            if (f >= 1.0f) {
                f = 1.0f;
                this.returningStack = ItemStack.EMPTY;
            }
            int l2 = this.returningStackDestSlot.xPos - this.touchUpX;
            int i3 = this.returningStackDestSlot.yPos - this.touchUpY;
            int l1 = this.touchUpX + (int)((float)l2 * f);
            int i2 = this.touchUpY + (int)((float)i3 * f);
            this.drawItemStack(this.returningStack, l1, i2, null);
        }
        GlStateManager.popMatrix();
        GlStateManager.enableLighting();
        GlStateManager.enableDepth();
        RenderHelper.enableStandardItemLighting();
        GlStateManager.popMatrix();
    }

    @Shadow
    protected abstract void drawGuiContainerBackgroundLayer(float var1, int var2, int var3);

    @Shadow
    private void drawSlot(Slot slotIn) {
    }

    @Shadow
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
    }

    @Shadow
    private void drawItemStack(ItemStack stack, int x, int y, String altText) {
        GlStateManager.translate(0.0f, 0.0f, 32.0f);
        this.zLevel = 200.0f;
        this.itemRender.zLevel = 200.0f;
        FontRenderer font = stack.getItem().getFontRenderer(stack);
        if (font == null) {
            font = this.fontRenderer;
        }
        this.itemRender.renderItemAndEffectIntoGUI(stack, x, y);
        this.itemRender.renderItemOverlayIntoGUI(font, stack, x, y - (this.draggedStack.isEmpty() ? 0 : 8), altText);
        this.zLevel = 0.0f;
        this.itemRender.zLevel = 0.0f;
    }

    @Shadow
    private boolean isMouseOverSlot(Slot slotIn, int mouseX, int mouseY) {
        return false;
    }
}

