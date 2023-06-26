package me.rebirthclient.asm.mixins;

import java.awt.Color;
import me.rebirthclient.api.events.impl.RenderToolTipEvent;
import me.rebirthclient.api.managers.Managers;
import me.rebirthclient.api.util.Wrapper;
import me.rebirthclient.api.util.render.RenderUtil;
import me.rebirthclient.mod.modules.impl.client.ClickGui;
import me.rebirthclient.mod.modules.impl.misc.ToolTips;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.item.ItemShulkerBox;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.MinecraftForge;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value={GuiScreen.class})
public abstract class MixinGuiScreen
extends Gui {
    private boolean hoveringShulker;
    private ItemStack shulkerStack;
    private String shulkerName;

    @Inject(method={"renderToolTip"}, at={@At(value="HEAD")}, cancellable=true)
    public void renderToolTipHook(ItemStack stack, int x, int y, CallbackInfo info) {
        RenderToolTipEvent event = new RenderToolTipEvent(stack, x, y);
        MinecraftForge.EVENT_BUS.post(event);
        if (event.isCanceled()) {
            info.cancel();
        }
        if (stack.getItem() instanceof ItemShulkerBox) {
            this.hoveringShulker = true;
            this.shulkerStack = stack;
            this.shulkerName = stack.getDisplayName();
        } else {
            this.hoveringShulker = false;
        }
    }

    @Inject(method={"mouseClicked"}, at={@At(value="HEAD")})
    public void mouseClickedHook(int mouseX, int mouseY, int mouseButton, CallbackInfo info) {
        if (mouseButton == 2 && this.hoveringShulker && ToolTips.INSTANCE.wheelPeek.getValue() && ToolTips.INSTANCE.isOn()) {
            ToolTips.drawShulkerGui(this.shulkerStack, this.shulkerName);
        }
    }

    @Inject(method={"drawScreen"}, at={@At(value="HEAD")})
    public void drawScreenHook(int mouseX, int mouseY, float partialTicks, CallbackInfo info) {
        if (Wrapper.mc.currentScreen != null && !(Wrapper.mc.currentScreen instanceof GuiContainer) && ClickGui.INSTANCE.background.getValue() && Wrapper.mc.world != null) {
            RenderUtil.drawVGradientRect(0.0f, 0.0f, Managers.TEXT.scaledWidth, Managers.TEXT.scaledHeight, new Color(0, 0, 0, 0).getRGB(), Managers.COLORS.getCurrentWithAlpha(60));
        }
    }

    @Inject(method={"drawWorldBackground(I)V"}, at={@At(value="HEAD")}, cancellable=true)
    private void drawWorldBackgroundHook(int tint, CallbackInfo info) {
        if (Wrapper.mc.world != null && ClickGui.INSTANCE.cleanGui.getValue()) {
            info.cancel();
        }
    }
}

