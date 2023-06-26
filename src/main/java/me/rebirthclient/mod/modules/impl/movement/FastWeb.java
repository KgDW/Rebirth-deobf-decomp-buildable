package me.rebirthclient.mod.modules.impl.movement;

import me.rebirthclient.api.managers.Managers;
import me.rebirthclient.mod.modules.Category;
import me.rebirthclient.mod.modules.Module;
import me.rebirthclient.mod.modules.impl.movement.ElytraFly;
import me.rebirthclient.mod.modules.impl.movement.NewStep;
import me.rebirthclient.mod.modules.settings.Setting;

public class FastWeb
extends Module {
    private final Setting<Mode> mode = this.add(new Setting<>("Mode", Mode.FAST));
    private final Setting<Float> fastSpeed = this.add(new Setting<>("FastSpeed", 3.0f, 0.0f, 5.0f, v -> this.mode.getValue() == Mode.FAST));
    private final Setting<Boolean> onlySneak = this.add(new Setting<>("OnlySneak", false));

    public FastWeb() {
        super("FastWeb", "So you don't need to keep timer on keybind", Category.MOVEMENT);
    }

    @Override
    public void onDisable() {
        Managers.TIMER.reset();
    }

    @Override
    public String getInfo() {
        return Managers.TEXT.normalizeCases(this.mode.getValue());
    }

    @Override
    public void onUpdate() {
        if (FastWeb.mc.player.isInWeb) {
            if (this.mode.getValue() == Mode.FAST && FastWeb.mc.gameSettings.keyBindSneak.isKeyDown() || !this.onlySneak.getValue()) {
                Managers.TIMER.reset();
                FastWeb.mc.player.motionY -= this.fastSpeed.getValue();
            } else if (this.mode.getValue() == Mode.STRICT && !FastWeb.mc.player.onGround && FastWeb.mc.gameSettings.keyBindSneak.isKeyDown() || !this.onlySneak.getValue()) {
                Managers.TIMER.set(8.0f);
            } else if (!(!ElytraFly.INSTANCE.isOff() && ElytraFly.INSTANCE.boostTimer.getValue() || NewStep.timer)) {
                Managers.TIMER.reset();
            }
        } else if (!(!ElytraFly.INSTANCE.isOff() && ElytraFly.INSTANCE.boostTimer.getValue() || NewStep.timer)) {
            Managers.TIMER.reset();
        }
    }

    private static enum Mode {
        FAST,
        STRICT

    }
}

