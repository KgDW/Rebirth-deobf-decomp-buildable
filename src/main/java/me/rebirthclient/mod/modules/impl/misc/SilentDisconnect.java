package me.rebirthclient.mod.modules.impl.misc;

import me.rebirthclient.mod.modules.Category;
import me.rebirthclient.mod.modules.Module;

public class SilentDisconnect
extends Module {
    public static SilentDisconnect INSTANCE = new SilentDisconnect();

    public SilentDisconnect() {
        super("SilentDisconnect", "Silent disconnect", Category.MISC);
        INSTANCE = this;
    }
}

