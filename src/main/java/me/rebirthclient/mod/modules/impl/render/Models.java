package me.rebirthclient.mod.modules.impl.render;

import java.awt.Color;
import me.rebirthclient.mod.modules.Category;
import me.rebirthclient.mod.modules.Module;
import me.rebirthclient.mod.modules.settings.Setting;

public class Models
extends Module {
    public final Setting<mode> Mode = this.add(new Setting<>("Mode", mode.AmongUs));
    public final Setting<Boolean> onlySelf = this.add(new Setting<>("OnlySelf", false).setParent());
    public final Setting<Boolean> friends = this.add(new Setting<>("Friends", false, v -> this.onlySelf.isOpen()).setParent());
    public final Setting<Boolean> friendHighlight = this.add(new Setting<>("friendHighLight", false, v -> this.friends.isOpen() && this.onlySelf.isOpen()));
    public final Setting<Color> eyeColor = this.add(new Setting<>("eyeColor", new Color(255, 255, 255), v -> this.Mode.getValue() == mode.AmongUs));
    public final Setting<Color> bodyColor = this.add(new Setting<>("bodyColor", new Color(255, 0, 0), v -> this.Mode.getValue() == mode.AmongUs));
    public final Setting<Color> legsColor = this.add(new Setting<>("legsColor", new Color(255, 0, 0), v -> this.Mode.getValue() == mode.AmongUs));
    public static Models INSTANCE;

    public Models() {
        super("Models", "something", Category.RENDER);
        INSTANCE = this;
    }

    public enum mode {
        AmongUs,
        Rabbit,
        Freddy

    }
}

