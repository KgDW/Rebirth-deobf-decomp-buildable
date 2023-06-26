package me.rebirthclient.mod.modules.impl.player;

import me.rebirthclient.mod.modules.Category;
import me.rebirthclient.mod.modules.Module;
import me.rebirthclient.mod.modules.settings.Setting;

public class TpsSync
extends Module {
    public static TpsSync INSTANCE;
    public final Setting<Boolean> attack = this.add(new Setting<>("Attack", false));
    public final Setting<Boolean> mining = this.add(new Setting<>("Mine", true));

    public TpsSync() {
        super("TpsSync", "Syncs your client with the TPS", Category.PLAYER);
        INSTANCE = this;
    }
}

