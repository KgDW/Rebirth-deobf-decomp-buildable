package me.rebirthclient.mod.modules.impl.movement;

import me.rebirthclient.api.events.impl.MoveEvent;
import me.rebirthclient.api.util.MovementUtil;
import me.rebirthclient.mod.modules.Category;
import me.rebirthclient.mod.modules.Module;
import me.rebirthclient.mod.modules.impl.movement.InventoryMove;
import me.rebirthclient.mod.modules.settings.Setting;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.lwjgl.input.Keyboard;

public final class FastSwim
extends Module {
    public static FastSwim INSTANCE = new FastSwim();
    private final Setting<Float> speed = this.add(new Setting<>("Speed", 5.0f, 1.0f, 10.0f));
    private final Setting<Float> verticalSpeed = this.add(new Setting<>("VerticalSpeed", 2.0f, 1.0f, 10.0f));
    private final Setting<Boolean> glide = this.add(new Setting<>("Glide", true).setParent());
    private final Setting<Float> glideSpeed = this.add(new Setting<>("GlideSpeed", 2.0f, 1.0f, 10.0f, v -> this.glide.isOpen()));

    public FastSwim() {
        super("FastSwim", "Allows you fast swim", Category.MOVEMENT);
        INSTANCE = this;
    }

    @SubscribeEvent
    public void onMove(MoveEvent event) {
        if (FastSwim.fullNullCheck()) {
            return;
        }
        if (!FastSwim.mc.player.isInLava() && !FastSwim.mc.player.isInWater()) {
            return;
        }
        FastSwim.mc.player.capabilities.isFlying = false;
        FastSwim.mc.player.motionY = 0.0;
        event.setY(0.0);
        this.setMoveSpeed(event, this.speed.getValue() / 20.0f);
        if (this.glide.getValue() && !FastSwim.mc.player.onGround) {
            event.setY(-0.007875f * this.glideSpeed.getValue());
            FastSwim.mc.player.motionY = -0.007875f * this.glideSpeed.getValue();
        }
        if (MovementUtil.isJumping()) {
            event.setY(FastSwim.mc.player.motionY + (double) this.verticalSpeed.getValue() / 20.0);
            FastSwim.mc.player.motionY += (double) this.verticalSpeed.getValue() / 20.0;
        }
        if (FastSwim.mc.gameSettings.keyBindSneak.isKeyDown() || InventoryMove.INSTANCE.isOn() && InventoryMove.INSTANCE.sneak.getValue() && Keyboard.isKeyDown(FastSwim.mc.gameSettings.keyBindSneak.getKeyCode())) {
            event.setY(FastSwim.mc.player.motionY - (double) this.verticalSpeed.getValue() / 20.0);
            FastSwim.mc.player.motionY -= (double) this.verticalSpeed.getValue() / 20.0;
        }
    }

    private void setMoveSpeed(MoveEvent event, double speed) {
        double forward = FastSwim.mc.player.movementInput.moveForward;
        double strafe = FastSwim.mc.player.movementInput.moveStrafe;
        float yaw = FastSwim.mc.player.rotationYaw;
        if (forward == 0.0 && strafe == 0.0) {
            event.setX(0.0);
            event.setZ(0.0);
            FastSwim.mc.player.motionX = 0.0;
            FastSwim.mc.player.motionZ = 0.0;
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
            FastSwim.mc.player.motionX = x;
            FastSwim.mc.player.motionZ = z;
        }
    }
}

