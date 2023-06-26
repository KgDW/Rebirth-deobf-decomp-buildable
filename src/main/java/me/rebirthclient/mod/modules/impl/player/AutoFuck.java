package me.rebirthclient.mod.modules.impl.player;

import me.rebirthclient.mod.modules.Category;
import me.rebirthclient.mod.modules.Module;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.client.settings.KeyBinding;

public class AutoFuck
extends Module {
    public AutoFuck() {
        super("AutoFuck", "auto fuck!!", Category.PLAYER);
    }

    @Override
    public void onTick() {
        AutoFuck.mc.gameSettings.keyBindSneak.pressed = !AutoFuck.mc.player.isSneaking() || GameSettings.isKeyDown(AutoFuck.mc.gameSettings.keyBindSneak);
    }

    @Override
    public void onDisable() {
        AutoFuck.mc.gameSettings.keyBindSneak.pressed = GameSettings.isKeyDown(AutoFuck.mc.gameSettings.keyBindSneak);
    }
}

