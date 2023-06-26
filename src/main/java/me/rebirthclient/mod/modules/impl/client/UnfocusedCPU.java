package me.rebirthclient.mod.modules.impl.client;

import me.rebirthclient.mod.modules.Category;
import me.rebirthclient.mod.modules.Module;
import me.rebirthclient.mod.modules.settings.Setting;

public class UnfocusedCPU
extends Module {
    public static UnfocusedCPU INSTANCE;
    public final Setting<Integer> unfocusedFps = this.add(new Setting<>("UnfocusedFPS", 5, 1, 30));

    public UnfocusedCPU() {
        super("UnfocusedCPU", "Decreases your framerate when minecraft is unfocused", Category.CLIENT);
        INSTANCE = this;
    }
}

