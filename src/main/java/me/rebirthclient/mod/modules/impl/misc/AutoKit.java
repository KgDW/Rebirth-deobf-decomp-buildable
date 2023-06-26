package me.rebirthclient.mod.modules.impl.misc;

import me.rebirthclient.mod.modules.Category;
import me.rebirthclient.mod.modules.Module;
import me.rebirthclient.mod.modules.settings.Setting;
import net.minecraft.client.gui.GuiGameOver;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.CPacketChatMessage;

public class AutoKit
extends Module {
    public final Setting<String> Name = this.add(new Setting<>("KitName", "1"));
    boolean needKit = false;

    public AutoKit() {
        super("AutoKit", "Auto select kit", Category.MISC);
    }

    @Override
    public void onUpdate() {
        if (AutoKit.mc.currentScreen instanceof GuiGameOver) {
            this.needKit = true;
        } else if (this.needKit) {
            AutoKit.mc.player.connection.sendPacket(new CPacketChatMessage("/kit " + this.Name.getValue()));
            this.needKit = false;
        }
    }
}

