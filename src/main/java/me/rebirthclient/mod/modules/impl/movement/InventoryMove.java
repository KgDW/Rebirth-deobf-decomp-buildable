package me.rebirthclient.mod.modules.impl.movement;

import me.rebirthclient.mod.modules.Category;
import me.rebirthclient.mod.modules.Module;
import me.rebirthclient.mod.modules.settings.Setting;

public class InventoryMove
extends Module {
    public static InventoryMove INSTANCE = new InventoryMove();
    public final Setting<Boolean> sneak = this.add(new Setting<>("Sneak", false));

    public InventoryMove() {
        super("InvMove", "Allow walking on the interface", Category.MOVEMENT);
        INSTANCE = this;
    }
}

