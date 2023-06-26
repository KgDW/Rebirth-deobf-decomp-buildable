package me.rebirthclient.mod.modules.impl.render;

import java.awt.Color;
import me.rebirthclient.mod.modules.Category;
import me.rebirthclient.mod.modules.Module;
import me.rebirthclient.mod.modules.settings.Setting;

public class CrystalChams
extends Module {
    public static CrystalChams INSTANCE;
    private final Setting<Page> page = this.add(new Setting<>("Settings", Page.GLOBAL));
    public final Setting<Boolean> fill = this.add(new Setting<>("Fill", true, v -> this.page.getValue() == Page.GLOBAL).setParent());
    public final Setting<Boolean> xqz = this.add(new Setting<>("XQZ", true, v -> this.page.getValue() == Page.GLOBAL && this.fill.isOpen()));
    public final Setting<Boolean> wireframe = this.add(new Setting<>("Wireframe", true, v -> this.page.getValue() == Page.GLOBAL));
    public final Setting<Model> model = this.add(new Setting<>("Model", Model.XQZ, v -> this.page.getValue() == Page.GLOBAL));
    public final Setting<Boolean> glint = this.add(new Setting<>("Glint", false, v -> this.page.getValue() == Page.GLOBAL));
    public final Setting<Float> scale = this.add(new Setting<>("Scale", 1.0f, 0.1f, 1.0f, v -> this.page.getValue() == Page.GLOBAL));
    public final Setting<Boolean> changeSpeed = this.add(new Setting<>("ChangeSpeed", false, v -> this.page.getValue() == Page.GLOBAL).setParent());
    public final Setting<Float> spinSpeed = this.add(new Setting<>("SpinSpeed", 1.0f, 0.0f, 10.0f, v -> this.page.getValue() == Page.GLOBAL && this.changeSpeed.isOpen()));
    public final Setting<Float> floatFactor = this.add(new Setting<>("FloatFactor", 1.0f, 0.0f, 1.0f, v -> this.page.getValue() == Page.GLOBAL && this.changeSpeed.isOpen()));
    public final Setting<Float> lineWidth = this.add(new Setting<>("LineWidth", 1.0f, 0.1f, 3.0f, v -> this.page.getValue() == Page.COLORS));
    public final Setting<Color> color = this.add(new Setting<>("Color", new Color(132, 132, 241, 150), v -> this.page.getValue() == Page.COLORS));
    public final Setting<Color> lineColor = this.add(new Setting<>("LineColor", new Color(255, 255, 255), v -> this.page.getValue() == Page.COLORS).injectBoolean(false));
    public final Setting<Color> modelColor = this.add(new Setting<>("ModelColor", new Color(125, 125, 213, 150), v -> this.page.getValue() == Page.COLORS).injectBoolean(false));

    public CrystalChams() {
        super("CrystalChams", "Draws a pretty ESP around end crystals", Category.RENDER);
        INSTANCE = this;
    }

    @Override
    public String getInfo() {
        String info = null;
        if (this.fill.getValue()) {
            info = "Fill";
        } else if (this.wireframe.getValue()) {
            info = "Wireframe";
        }
        if (this.wireframe.getValue() && this.fill.getValue()) {
            info = "Both";
        }
        return info;
    }

    public static enum Model {
        XQZ,
        VANILLA,
        OFF

    }

    public static enum Page {
        COLORS,
        GLOBAL

    }
}

