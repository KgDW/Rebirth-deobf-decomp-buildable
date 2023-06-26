package me.rebirthclient.mod.modules.impl.movement;

import me.rebirthclient.mod.modules.Category;
import me.rebirthclient.mod.modules.Module;

public final class NoJumpDelay
extends Module {
    public static NoJumpDelay INSTANCE = new NoJumpDelay();

    public NoJumpDelay() {
        super("NoJumpDelay", "No jump delay", Category.MOVEMENT);
        INSTANCE = this;
    }
}

