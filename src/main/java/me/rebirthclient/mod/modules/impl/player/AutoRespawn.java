package me.rebirthclient.mod.modules.impl.player;

import me.rebirthclient.mod.modules.Category;
import me.rebirthclient.mod.modules.Module;
import net.minecraft.client.gui.GuiGameOver;

public class AutoRespawn
extends Module {
    public AutoRespawn() {
        super("AutoRespawn", "Auto Respawn when dead", Category.PLAYER);
    }

    @Override
    public void onUpdate() {
        if (AutoRespawn.mc.currentScreen instanceof GuiGameOver) {
            AutoRespawn.mc.player.respawnPlayer();
            mc.displayGuiScreen(null);
        }
    }
}

