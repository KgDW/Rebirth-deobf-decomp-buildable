package me.rebirthclient.mod.modules.impl.movement;

import me.rebirthclient.mod.modules.Category;
import me.rebirthclient.mod.modules.Module;
import net.minecraftforge.client.event.InputUpdateEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class AutoWalk
extends Module {
    public AutoWalk() {
        super("AutoWalk", "Auto forward move", Category.MOVEMENT);
    }

    @SubscribeEvent
    public void onUpdateInput(InputUpdateEvent event) {
        event.getMovementInput().moveForward = 1.0f;
    }
}

