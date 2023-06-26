package me.rebirthclient.mod.modules.impl.movement;

import me.rebirthclient.api.managers.Managers;
import me.rebirthclient.api.managers.impl.SneakManager;
import me.rebirthclient.mod.modules.Category;
import me.rebirthclient.mod.modules.Module;
import me.rebirthclient.mod.modules.settings.Setting;

public class Sprint
extends Module {
    private final Setting<Mode> mode = this.add(new Setting<>("Mode", Mode.RAGE));

    public Sprint() {
        super("Sprint", "Sprints", Category.MOVEMENT);
    }

    public static boolean isMoving() {
        return Sprint.mc.player.moveForward != 0.0f || Sprint.mc.player.moveStrafing != 0.0f;
    }

    @Override
    public String getInfo() {
        return Managers.TEXT.normalizeCases(this.mode.getValue());
    }

    @Override
    public void onTick() {
        if (this.mode.getValue() == Mode.RAGE && Sprint.isMoving()) {
            Sprint.mc.player.setSprinting(true);
        } else if (this.mode.getValue() == Mode.LEGIT && Sprint.mc.player.moveForward > 0.1f && !Sprint.mc.player.collidedHorizontally && !SneakManager.isSneaking) {
            Sprint.mc.player.setSprinting(true);
        }
    }

    private static enum Mode {
        RAGE,
        LEGIT

    }
}

