package me.rebirthclient.mod.modules.impl.movement;

import me.rebirthclient.api.events.impl.MoveEvent;
import me.rebirthclient.api.util.MovementUtil;
import me.rebirthclient.mod.modules.Category;
import me.rebirthclient.mod.modules.Module;
import me.rebirthclient.mod.modules.impl.movement.InventoryMove;
import me.rebirthclient.mod.modules.settings.Setting;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.lwjgl.input.Keyboard;

public final class Flight
extends Module {
    public static Flight INSTANCE = new Flight();
    private final Setting<Float> speed = this.add(new Setting<>("Speed", 1.0f, 0.1f, 10.0f));
    private final Setting<Float> verticalSpeed = this.add(new Setting<>("VerticalSpeed", 1.0f, 0.1f, 10.0f));
    private final Setting<Boolean> glide = this.add(new Setting<>("Glide", true));

    public Flight() {
        super("Flight", "Allows you to fly", Category.MOVEMENT);
        INSTANCE = this;
    }

    @SubscribeEvent
    public void onMove(MoveEvent event) {
        if (Flight.fullNullCheck()) {
            return;
        }
        Flight.mc.player.capabilities.isFlying = false;
        Flight.mc.player.motionY = 0.0;
        this.setMoveSpeed(event, this.speed.getValue());
        if (this.glide.getValue() && !Flight.mc.player.onGround) {
            event.setY(-0.0315f);
            Flight.mc.player.motionY = -0.0315f;
        }
        if (MovementUtil.isJumping()) {
            event.setY(Flight.mc.player.motionY + (double) this.verticalSpeed.getValue());
            Flight.mc.player.motionY += this.verticalSpeed.getValue();
        }
        if (Flight.mc.gameSettings.keyBindSneak.isKeyDown() || InventoryMove.INSTANCE.isOn() && InventoryMove.INSTANCE.sneak.getValue() && Keyboard.isKeyDown(Flight.mc.gameSettings.keyBindSneak.getKeyCode())) {
            event.setY(Flight.mc.player.motionY - (double) this.verticalSpeed.getValue());
            Flight.mc.player.motionY -= this.verticalSpeed.getValue();
        }
    }

    private void setMoveSpeed(MoveEvent event, double speed) {
        double forward = Flight.mc.player.movementInput.moveForward;
        double strafe = Flight.mc.player.movementInput.moveStrafe;
        float yaw = Flight.mc.player.rotationYaw;
        if (forward == 0.0 && strafe == 0.0) {
            event.setX(0.0);
            event.setZ(0.0);
            Flight.mc.player.motionX = 0.0;
            Flight.mc.player.motionZ = 0.0;
        } else {
            if (forward != 0.0) {
                if (strafe > 0.0) {
                    yaw += (float)(forward > 0.0 ? -45 : 45);
                } else if (strafe < 0.0) {
                    yaw += (float)(forward > 0.0 ? 45 : -45);
                }
                strafe = 0.0;
                if (forward > 0.0) {
                    forward = 1.0;
                } else if (forward < 0.0) {
                    forward = -1.0;
                }
            }
            double x = forward * speed * -Math.sin(Math.toRadians(yaw)) + strafe * speed * Math.cos(Math.toRadians(yaw));
            double z = forward * speed * Math.cos(Math.toRadians(yaw)) - strafe * speed * -Math.sin(Math.toRadians(yaw));
            event.setX(x);
            event.setZ(z);
            Flight.mc.player.motionX = x;
            Flight.mc.player.motionZ = z;
        }
    }
}

