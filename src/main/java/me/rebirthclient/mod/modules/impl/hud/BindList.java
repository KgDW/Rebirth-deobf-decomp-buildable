package me.rebirthclient.mod.modules.impl.hud;

import java.awt.Color;
import me.rebirthclient.api.events.impl.Render2DEvent;
import me.rebirthclient.api.managers.Managers;
import me.rebirthclient.api.util.render.ColorUtil;
import me.rebirthclient.api.util.render.RenderUtil;
import me.rebirthclient.mod.modules.Category;
import me.rebirthclient.mod.modules.Module;
import me.rebirthclient.mod.modules.settings.Setting;

public class BindList
extends Module {
    private final Setting<Integer> x = this.add(new Setting<>("x", 50, 0, 500));
    private final Setting<Integer> y = this.add(new Setting<>("y", 50, 0, 500));
    private final Setting<Color> color = this.add(new Setting<>("color", new Color(255, 255, 255, 50)));

    public BindList() {
        super("BindList", "test", Category.HUD);
    }

    @Override
    public void onRender2D(Render2DEvent event) {
        String string;
        Module module;
        int k;
        int length = 0;
        int y = this.y.getValue();
        for (k = 0; k < Managers.MODULES.sortedLength.size(); ++k) {
            module = Managers.MODULES.sortedLength.get(k);
            if (module.getBind().getKey() == -1 || Managers.TEXT.getStringWidth(string = module.getName()) <= length) continue;
            length = Managers.TEXT.getStringWidth(string);
        }
        for (k = 0; k < Managers.MODULES.sortedLength.size(); ++k) {
            module = Managers.MODULES.sortedLength.get(k);
            if (module.getBind().getKey() == -1) continue;
            string = module.getName();
            Managers.TEXT.drawString(string, this.x.getValue(), y, -1, true);
            Managers.TEXT.drawString("[toggled]", this.x.getValue() + 10 + length, y, -1, true);
            y += 10;
        }
        RenderUtil.drawRectangleCorrectly(this.x.getValue() - 4, this.y.getValue() - 16, 18 + Managers.TEXT.getStringWidth("[toggled]") + length, 1, ColorUtil.toRGBA(this.color.getValue().getRed(), this.color.getValue().getGreen(), this.color.getValue().getBlue(), 255));
        RenderUtil.drawRectangleCorrectly(this.x.getValue() - 4, this.y.getValue() - 15, 18 + Managers.TEXT.getStringWidth("[toggled]") + length, 12, ColorUtil.toRGBA(new Color(0, 0, 0, this.color.getValue().getAlpha())));
        Managers.TEXT.drawString("keybinds", (float)((double) this.x.getValue() + ((double)(11 + length) + (double)Managers.TEXT.getStringWidth("[toggled]")) / 2.0 - (double)Managers.TEXT.getStringWidth("keybinds") / 2.0), this.y.getValue() - 13, ColorUtil.toRGBA(255, 255, 255, 255), true);
    }
}

