package me.rebirthclient.mod.modules.impl.client;

import me.rebirthclient.mod.modules.Category;
import me.rebirthclient.mod.modules.Module;
import net.minecraft.client.gui.GuiScreen;

public class Appearance
extends Module {
    public Appearance() {
        super("HUDEditor", "Drag HUD elements all over your screen", Category.CLIENT);
    }

    @Override
    public void onEnable() {
        mc.displayGuiScreen(me.rebirthclient.mod.gui.screen.Appearance.getClickGui());
    }

    @Override
    public void onTick() {
        if (!(Appearance.mc.currentScreen instanceof me.rebirthclient.mod.gui.screen.Appearance)) {
            this.disable();
        }
    }
}

