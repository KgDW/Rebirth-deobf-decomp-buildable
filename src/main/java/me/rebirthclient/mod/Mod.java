package me.rebirthclient.mod;

import java.util.ArrayList;
import java.util.List;
import me.rebirthclient.api.util.Wrapper;
import me.rebirthclient.mod.gui.screen.Gui;
import me.rebirthclient.mod.modules.Module;
import me.rebirthclient.mod.modules.settings.Setting;

public class Mod
implements Wrapper {
    public List<Setting> settings = new ArrayList<>();
    private String name;

    public Mod(String name) {
        this.name = name;
    }

    public Mod() {
    }

    public static boolean nullCheck() {
        return Mod.mc.player == null;
    }

    public static boolean fullNullCheck() {
        return Mod.mc.player == null || Mod.mc.world == null;
    }

    public static boolean spawnCheck() {
        return Mod.mc.player.ticksExisted > 15;
    }

    public Setting add(Setting setting) {
        setting.setMod(this);
        this.settings.add(setting);
        if (this instanceof Module && Mod.mc.currentScreen instanceof Gui) {
            Gui.INSTANCE.updateModule((Module)this);
        }
        return setting;
    }

    public Setting register(Setting setting) {
        setting.setMod(this);
        this.settings.add(setting);
        if (this instanceof Module && Mod.mc.currentScreen instanceof Gui) {
            Gui.INSTANCE.updateModule((Module)this);
        }
        return setting;
    }

    public Setting getSettingByName(String name) {
        for (Setting setting : this.settings) {
            if (!setting.getName().equalsIgnoreCase(name)) continue;
            return setting;
        }
        return null;
    }

    public void resetSettings() {
        this.settings = new ArrayList<>();
    }

    public String getName() {
        return this.name;
    }

    public List<Setting> getSettings() {
        return this.settings;
    }
}

