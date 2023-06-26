package me.rebirthclient.mod.modules.impl.hud;

import java.awt.Color;
import me.rebirthclient.api.events.impl.Render2DEvent;
import me.rebirthclient.api.managers.Managers;
import me.rebirthclient.api.util.render.RenderUtil;
import me.rebirthclient.mod.modules.Category;
import me.rebirthclient.mod.modules.Module;
import me.rebirthclient.mod.modules.settings.Setting;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;

public class InventoryPreview
extends Module {
    public final Setting<XOffset> xOffset = this.add(new Setting<>("XOffset", XOffset.CUSTOM));
    public final Setting<Integer> x = this.add(new Setting<>("X", 500, 0, 1000, v -> this.xOffset.getValue() == XOffset.CUSTOM));
    public final Setting<YOffset> yOffset = this.add(new Setting<>("YOffset", YOffset.CUSTOM));
    public final Setting<Integer> y = this.add(new Setting<>("Y", 2, 0, 1000, v -> this.yOffset.getValue() == YOffset.CUSTOM));
    public final Setting<Boolean> outline = this.add(new Setting<>("Outline", true).setParent());
    public final Setting<Color> lineColor = this.add(new Setting<>("LineColor", new Color(10, 10, 10, 100), v -> this.outline.isOpen()));
    public final Setting<Color> secondColor = this.add(new Setting<>("SecondColor", new Color(30, 30, 30, 100), v -> this.outline.isOpen()).injectBoolean(true));
    public final Setting<Boolean> rect = this.add(new Setting<>("Rect", true).setParent());
    public final Setting<Color> rectColor = this.add(new Setting<>("RectColor", new Color(10, 10, 10, 50), v -> this.rect.isOpen()));

    public InventoryPreview() {
        super("Inventory", "Allows you to see your own inventory without opening it", Category.HUD);
    }

    @Override
    public void onRender2D(Render2DEvent event) {
        int y = 0;
        int x = 0;
        if (InventoryPreview.fullNullCheck()) {
            return;
        }
        GlStateManager.enableTexture2D();
        GlStateManager.disableLighting();
        GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
        GlStateManager.disableDepth();
        int n = this.xOffset.getValue() == XOffset.CUSTOM ? this.x.getValue() : (x = this.xOffset.getValue() == XOffset.LEFT ? 0 : Managers.TEXT.scaledWidth - 172);
        int n2 = this.yOffset.getValue() == YOffset.CUSTOM ? this.y.getValue() : (y = this.yOffset.getValue() == YOffset.TOP ? 0 : Managers.TEXT.scaledHeight - 74);
        if (this.outline.getValue()) {
            RenderUtil.drawNameTagOutline((float)x + 6.5f, (float)y + 16.5f, (float)x + 171.5f, (float)y + 73.5f, 1.0f, this.lineColor.getValue().getRGB(), this.secondColor.booleanValue ? this.secondColor.getValue().getRGB() : this.lineColor.getValue().getRGB(), false);
        }
        if (this.rect.getValue()) {
            RenderUtil.drawRect(x + 7, y + 17, x + 171, y + 73, this.rectColor.getValue().getRGB());
        }
        GlStateManager.enableDepth();
        RenderHelper.enableGUIStandardItemLighting();
        GlStateManager.enableRescaleNormal();
        GlStateManager.enableColorMaterial();
        GlStateManager.enableLighting();
        NonNullList items = InventoryPreview.mc.player.inventory.mainInventory;
        for (int i = 0; i < items.size() - 9; ++i) {
            int iX = x + i % 9 * 18 + 8;
            int iY = y + i / 9 * 18 + 18;
            ItemStack stack = (ItemStack)items.get(i + 9);
            InventoryPreview.mc.getItemRenderer().itemRenderer.zLevel = 501.0f;
            mc.getRenderItem().renderItemAndEffectIntoGUI(stack, iX, iY);
            mc.getRenderItem().renderItemOverlayIntoGUI(InventoryPreview.mc.fontRenderer, stack, iX, iY, null);
            InventoryPreview.mc.getItemRenderer().itemRenderer.zLevel = 0.0f;
        }
        GlStateManager.disableLighting();
        GlStateManager.disableBlend();
        GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
    }

    private static enum YOffset {
        CUSTOM,
        TOP,
        BOTTOM

    }

    private static enum XOffset {
        CUSTOM,
        LEFT,
        RIGHT

    }
}

