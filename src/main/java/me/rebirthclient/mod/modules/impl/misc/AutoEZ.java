package me.rebirthclient.mod.modules.impl.misc;

import me.rebirthclient.mod.modules.Category;
import me.rebirthclient.mod.modules.Module;
import me.rebirthclient.mod.modules.impl.misc.PopCounter;
import me.rebirthclient.mod.modules.settings.Setting;
import net.minecraft.entity.player.EntityPlayer;

public class AutoEZ
extends Module {
    public static AutoEZ INSTANCE = new AutoEZ();
    public final Setting<String> EzString = this.add(new Setting<>("String", "EZ"));
    public final Setting<Boolean> poped = this.add(new Setting<>("popCounter", true));
    public final Setting<Boolean> whenFriend = this.add(new Setting<>("whenFriend", false));
    public final Setting<Boolean> whenSelf = this.add(new Setting<>("whenSelf", false));
    public final Setting<String> SelfString = this.add(new Setting<>("SelfString", "potato server nice lag"));

    public AutoEZ() {
        super("AutoEZ", "say ez for enemy dead", Category.MISC);
        INSTANCE = this;
    }

    @Override
    public void onTotemPop(EntityPlayer player) {
        if (PopCounter.INSTANCE.isOn()) {
            return;
        }
        PopCounter.INSTANCE.onTotemPop(player);
    }

    @Override
    public void onDeath(EntityPlayer player) {
        if (PopCounter.INSTANCE.isOn()) {
            return;
        }
        PopCounter.INSTANCE.onDeath(player);
    }
}

