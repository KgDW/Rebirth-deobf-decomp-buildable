package me.rebirthclient.mod.modules.impl.render;

import me.rebirthclient.mod.modules.Category;
import me.rebirthclient.mod.modules.Module;
import me.rebirthclient.mod.modules.settings.Setting;

public class RenderSetting
extends Module {
    public static RenderSetting INSTANCE;
    public final Setting<Float> outlineWidth = this.add(new Setting<>("OutlineWidth", 1.0f, 0.1f, 4.0f));

    public RenderSetting() {
        super("RenderSetting", "idk", Category.RENDER);
        INSTANCE = this;
    }
}

