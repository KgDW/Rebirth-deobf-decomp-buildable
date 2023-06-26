package me.rebirthclient.mod.modules.impl.misc;

import me.rebirthclient.mod.modules.Category;
import me.rebirthclient.mod.modules.Module;
import me.rebirthclient.mod.modules.settings.Setting;

public class ExtraTab
extends Module {
    public static ExtraTab INSTANCE = new ExtraTab();
    public final Setting<Integer> size = this.add(new Setting<>("Size", 250, 1, 1000));

    public ExtraTab() {
        super("ExtraTab", "Extends Tab", Category.MISC);
        INSTANCE = this;
    }
}

