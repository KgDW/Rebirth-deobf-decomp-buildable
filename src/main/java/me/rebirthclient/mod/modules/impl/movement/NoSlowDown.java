package me.rebirthclient.mod.modules.impl.movement;

import me.rebirthclient.api.managers.impl.SneakManager;
import me.rebirthclient.mod.modules.Category;
import me.rebirthclient.mod.modules.Module;
import me.rebirthclient.mod.modules.settings.Setting;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.CPacketHeldItemChange;
import net.minecraftforge.client.event.InputUpdateEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class NoSlowDown
extends Module {
    private final Setting<Mode> mode = this.add(new Setting<>("Mode", Mode.Vanilla));
    private final Setting<Boolean> sneak = this.add(new Setting<>("Sneak", false));

    public NoSlowDown() {
        super("NoSlowDown", "No item use slow down", Category.MOVEMENT);
    }

    @SubscribeEvent
    public void Slow(InputUpdateEvent event) {
        if (!SneakManager.isSneaking && NoSlowDown.mc.player.isHandActive() && !NoSlowDown.mc.player.isRiding()) {
            if (this.mode.getValue() == Mode.Strict) {
                NoSlowDown.mc.player.connection.sendPacket(new CPacketHeldItemChange(NoSlowDown.mc.player.inventory.currentItem));
            }
            NoSlowDown.mc.player.movementInput.moveForward = (float)((double)NoSlowDown.mc.player.movementInput.moveForward / 0.2);
            NoSlowDown.mc.player.movementInput.moveStrafe = (float)((double)NoSlowDown.mc.player.movementInput.moveStrafe / 0.2);
        } else if (SneakManager.isSneaking && this.sneak.getValue() && !NoSlowDown.mc.player.isRiding()) {
            event.getMovementInput().moveStrafe *= 5.0f;
            event.getMovementInput().moveForward *= 5.0f;
        }
    }

    @Override
    public String getInfo() {
        return this.mode.getValue().name();
    }

    public static enum Mode {
        Vanilla,
        Strict

    }
}

