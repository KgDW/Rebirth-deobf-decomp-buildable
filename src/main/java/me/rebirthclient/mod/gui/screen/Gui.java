package me.rebirthclient.mod.gui.screen;

import java.awt.Color;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Random;
import me.rebirthclient.api.managers.Managers;
import me.rebirthclient.api.util.render.ColorUtil;
import me.rebirthclient.api.util.render.RenderUtil;
import me.rebirthclient.mod.Mod;
import me.rebirthclient.mod.gui.click.Component;
import me.rebirthclient.mod.gui.click.items.Item;
import me.rebirthclient.mod.gui.click.items.buttons.ModuleButton;
import me.rebirthclient.mod.gui.click.items.other.Snow;
import me.rebirthclient.mod.modules.Category;
import me.rebirthclient.mod.modules.Module;
import me.rebirthclient.mod.modules.impl.client.ClickGui;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

public class Gui
extends GuiScreen {
    public static Gui INSTANCE;
    final Minecraft mc = Minecraft.getMinecraft();
    private final ArrayList<Snow> snow = new ArrayList();
    private final ArrayList<Component> components = new ArrayList();

    public Gui() {
        this.onLoad();
    }

    private void onLoad() {
        INSTANCE = this;
        int x = -84;
        for (final Category category : Managers.MODULES.getCategories()) {
            if (category == Category.HUD) continue;
            this.components.add(new Component(category.getName(), x += 90, 4, true){

                @Override
                public void setupItems() {
                    counter1 = new int[]{1};
                    Managers.MODULES.getModulesByCategory(category).forEach(module -> this.addButton(new ModuleButton(module)));
                }
            });
        }
        this.components.forEach(components -> components.getItems().sort(Comparator.comparing(Mod::getName)));
        Random random = new Random();
        for (int i = 0; i < 100; ++i) {
            for (int y = 0; y < 3; ++y) {
                Snow snow = new Snow(25 * i, y * -50, random.nextInt(3) + 1, random.nextInt(2) + 1);
                this.snow.add(snow);
            }
        }
    }

    public void updateModule(Module module) {
        for (Component component : this.components) {
            for (Item item : component.getItems()) {
                if (!(item instanceof ModuleButton)) continue;
                ModuleButton button = (ModuleButton)item;
                Module mod = button.getModule();
                if (module == null || !module.equals(mod)) continue;
                button.initSettings();
            }
        }
    }

    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        this.checkMouseWheel();
        if (this.mc.world != null) {
            this.drawDefaultBackground();
        } else {
            net.minecraft.client.gui.Gui.drawRect(0, 0, 1920, 1080, ColorUtil.injectAlpha(new Color(-1072689136), 150).getRGB());
        }
        if (ClickGui.INSTANCE.background.getValue() && this.mc.currentScreen instanceof Gui && this.mc.world != null) {
            RenderUtil.drawVGradientRect(0.0f, 0.0f, Managers.TEXT.scaledWidth, Managers.TEXT.scaledHeight, new Color(0, 0, 0, 0).getRGB(), Managers.COLORS.getCurrentWithAlpha(60));
        }
        float size = (float)ClickGui.animation.easeOutQuad();
        GlStateManager.pushMatrix();
        GL11.glScaled(size, size, size);
        this.components.forEach(components -> components.drawScreen(mouseX, mouseY, partialTicks));
        GlStateManager.popMatrix();
        ScaledResolution res = new ScaledResolution(this.mc);
        if (!this.snow.isEmpty() && ClickGui.INSTANCE.snow.getValue()) {
            this.snow.forEach(snow -> snow.drawSnow(res));
        }
    }

    public void mouseClicked(int mouseX, int mouseY, int clickedButton) {
        this.components.forEach(components -> components.mouseClicked(mouseX, mouseY, clickedButton));
    }

    public void mouseReleased(int mouseX, int mouseY, int releaseButton) {
        this.components.forEach(components -> components.mouseReleased(mouseX, mouseY, releaseButton));
    }

    public boolean doesGuiPauseGame() {
        return false;
    }

    public final ArrayList<Component> getComponents() {
        return this.components;
    }

    public void checkMouseWheel() {
        int dWheel = Mouse.getDWheel();
        if (dWheel < 0) {
            this.components.forEach(component -> component.setY(component.getY() - 10));
        } else if (dWheel > 0) {
            this.components.forEach(component -> component.setY(component.getY() + 10));
        }
    }

    public int getTextOffset() {
        return -6;
    }

    public void keyTyped(char typedChar, int keyCode) throws IOException {
        super.keyTyped(typedChar, keyCode);
        this.components.forEach(component -> component.onKeyTyped(typedChar, keyCode));
    }

    public void onGuiClosed() {
        super.onGuiClosed();
        if (this.mc.entityRenderer.isShaderActive()) {
            this.mc.entityRenderer.getShaderGroup().deleteShaderGroup();
        }
    }
}

