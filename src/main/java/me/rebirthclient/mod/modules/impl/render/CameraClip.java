package me.rebirthclient.mod.modules.impl.render;

import me.rebirthclient.mod.modules.Category;
import me.rebirthclient.mod.modules.Module;
import me.rebirthclient.mod.modules.settings.Setting;

public class CameraClip
extends Module {
    public static CameraClip INSTANCE = new CameraClip();
    public final Setting<Double> distance = this.add(new Setting<>("Distance", 4.0, -0.5, 15.0));

    public CameraClip() {
        super("CameraClip", "CameraClip", Category.RENDER);
        INSTANCE = this;
    }
}

