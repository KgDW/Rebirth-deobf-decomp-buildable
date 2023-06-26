package me.rebirthclient.mod.modules.impl.movement;

import me.rebirthclient.mod.modules.Category;
import me.rebirthclient.mod.modules.Module;
import me.rebirthclient.mod.modules.settings.Setting;

public class AntiWeb
extends Module {
    public final Setting<AntiMode> antiModeSetting = this.add(new Setting<>("AntiMode", AntiMode.Block));
    public static AntiWeb INSTANCE;

    public AntiWeb() {
        super("AntiWeb", "Solid web", Category.MOVEMENT);
        INSTANCE = this;
    }

    @Override
    public void onUpdate() {
        if (this.antiModeSetting.getValue() == AntiMode.Ignore && AntiWeb.mc.player.isInWeb) {
            AntiWeb.mc.player.isInWeb = false;
        }
    }

    public static enum AntiMode {
        Block,
        Ignore

    }
}

