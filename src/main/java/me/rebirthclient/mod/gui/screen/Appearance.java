package me.rebirthclient.mod.gui.screen;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import me.rebirthclient.api.managers.Managers;
import me.rebirthclient.mod.Mod;
import me.rebirthclient.mod.gui.click.Component;
import me.rebirthclient.mod.gui.click.items.Item;
import me.rebirthclient.mod.gui.click.items.buttons.ModuleButton;
import me.rebirthclient.mod.modules.Category;
import me.rebirthclient.mod.modules.Module;
import net.minecraft.client.gui.GuiScreen;
import org.lwjgl.input.Mouse;

public class Appearance
extends GuiScreen {
    private static Appearance INSTANCE = new Appearance();
    private final ArrayList<Component> components = new ArrayList();

    public Appearance() {
        INSTANCE = this;
        this.load();
    }

    public static Appearance getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new Appearance();
        }
        return INSTANCE;
    }

    public static Appearance getClickGui() {
        return Appearance.getInstance();
    }

    private void load() {
        int x = -84;
        for (final Category category : Managers.MODULES.getCategories()) {
            if (category != Category.HUD) continue;
            this.components.add(new Component(category.getName(), x += 90, 4, true){

                @Override
                public void setupItems() {
                    counter1 = new int[]{1};
                    Managers.MODULES.getModulesByCategory(category).forEach(module -> this.addButton(new ModuleButton(module)));
                }
            });
        }
        this.components.forEach(components -> components.getItems().sort(Comparator.comparing(Mod::getName)));
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
        this.components.forEach(components -> components.drawScreen(mouseX, mouseY, partialTicks));
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

    public Component getComponentByName(String name) {
        for (Component component : this.components) {
            if (!component.getName().equalsIgnoreCase(name)) continue;
            return component;
        }
        return null;
    }

    public void keyTyped(char typedChar, int keyCode) throws IOException {
        super.keyTyped(typedChar, keyCode);
        this.components.forEach(component -> component.onKeyTyped(typedChar, keyCode));
    }

    public void onGuiClosed() {
        try {
            super.onGuiClosed();
            this.mc.entityRenderer.getShaderGroup().deleteShaderGroup();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
}

