package me.rebirthclient.mod.modules.impl.client;

import me.rebirthclient.api.util.FadeUtils;
import me.rebirthclient.mod.modules.Category;
import me.rebirthclient.mod.modules.Module;
import me.rebirthclient.mod.modules.settings.Setting;

public class GuiAnimation
extends Module {
    public static GuiAnimation INSTANCE;
    private final Setting<Integer> inventoryTime = this.add(new Setting<>("InventoryTime", 500, 0, 2000));
    public static final FadeUtils inventoryFade;

    public GuiAnimation() {
        super("GuiAnimation", "", Category.CLIENT);
        INSTANCE = this;
    }

    @Override
    public void onTick() {
        inventoryFade.setLength(this.inventoryTime.getValue());
        if (GuiAnimation.mc.currentScreen == null) {
            inventoryFade.reset();
        }
    }

    static {
        inventoryFade = new FadeUtils(500L);
    }
}

