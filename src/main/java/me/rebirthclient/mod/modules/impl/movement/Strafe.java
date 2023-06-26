package me.rebirthclient.mod.modules.impl.movement;

import java.util.Objects;
import me.rebirthclient.api.events.impl.MoveEvent;
import me.rebirthclient.api.events.impl.UpdateWalkingPlayerEvent;
import me.rebirthclient.mod.modules.Category;
import me.rebirthclient.mod.modules.Module;
import me.rebirthclient.mod.modules.impl.movement.HoleSnap;
import me.rebirthclient.mod.modules.impl.movement.InventoryMove;
import me.rebirthclient.mod.modules.settings.Setting;
import net.minecraft.client.gui.GuiChat;
import net.minecraft.entity.Entity;
import net.minecraft.init.MobEffects;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.lwjgl.input.Keyboard;

public class Strafe
extends Module {
    public static Strafe INSTANCE = new Strafe();
    public final Setting<Mode> mode = this.add(new Setting<>("Mode", Mode.Normal));
    private final Setting<Boolean> jump = this.add(new Setting<>("Jump", false));
    private final Setting<Double> jumpMotion = this.add(new Setting<>("JumpMotion", 0.40123128, 0.1, 1.0));
    private final Setting<Float> multiplier = this.add(new Setting<>("Factor", 1.67f, 0.0f, 3.0f));
    private final Setting<Float> plier = this.add(new Setting<>("Factor+", 2.149f, 0.0f, 3.0f));
    private final Setting<Float> Dist = this.add(new Setting<>("Dist", 0.6896f, 0.1f, 1.0f));
    private final Setting<Float> multiDist = this.add(new Setting<>("Dist+", 0.795f, 0.1f, 1.0f));
    private final Setting<Float> SPEEDY = this.add(new Setting<>("SpeedY", 730.0f, 500.0f, 800.0f));
    private final Setting<Float> SPEEDH = this.add(new Setting<>("SpeedH", 159.0f, 100.0f, 300.0f));
    private final Setting<Float> StrafeH = this.add(new Setting<>("SpeedH", 0.993f, 0.1f, 1.0f));
    private final Setting<Float> StrafeY = this.add(new Setting<>("SpeedY", 0.99f, 0.1f, 1.2f));
    int stage;
    private double lastDist;
    private double moveSpeed;

    public Strafe() {
        super("Strafe", "Modifies sprinting", Category.MOVEMENT);
        INSTANCE = this;
    }

    @Override
    public String getInfo() {
        return this.mode.getValue().name();
    }

    @SubscribeEvent
    public void onUpdateWalkingPlayerEvent(UpdateWalkingPlayerEvent event) {
        if (Strafe.fullNullCheck()) {
            return;
        }
        this.lastDist = Math.sqrt((Strafe.mc.player.posX - Strafe.mc.player.prevPosX) * (Strafe.mc.player.posX - Strafe.mc.player.prevPosX) + (Strafe.mc.player.posZ - Strafe.mc.player.prevPosZ) * (Strafe.mc.player.posZ - Strafe.mc.player.prevPosZ));
    }

    @SubscribeEvent
    public void onStrafe(MoveEvent event) {
        if (Strafe.fullNullCheck()) {
            return;
        }
        if (HoleSnap.INSTANCE.isOn()) {
            return;
        }
        if (!Strafe.mc.player.isInWater() && !Strafe.mc.player.isInLava()) {
            if (Strafe.mc.player.onGround) {
                this.stage = 2;
            }
            if (this.stage == 0) {
                ++this.stage;
                this.lastDist = 0.0;
            } else if (this.stage == 2) {
                double motionY = this.jumpMotion.getValue();
                if (Strafe.mc.player.onGround && this.jump.getValue() || Strafe.mc.gameSettings.keyBindJump.isKeyDown()) {
                    if (Strafe.mc.player.isPotionActive(MobEffects.JUMP_BOOST)) {
                        motionY += (float)(Objects.requireNonNull(Strafe.mc.player.getActivePotionEffect(MobEffects.JUMP_BOOST)).getAmplifier() + 1) * 0.1f;
                    }
                    Strafe.mc.player.motionY = motionY;
                    event.setY(Strafe.mc.player.motionY);
                    this.moveSpeed *= this.mode.getValue() == Mode.Normal ? this.multiplier.getValue() : this.plier.getValue();
                }
            } else if (this.stage == 3) {
                this.moveSpeed = this.lastDist - (double) (this.mode.getValue() == Mode.Normal ? this.Dist.getValue() : this.multiDist.getValue()) * (this.lastDist - this.getBaseMoveSpeed());
            } else {
                if ((Strafe.mc.world.getCollisionBoxes(Strafe.mc.player, Strafe.mc.player.getEntityBoundingBox().offset(0.0, Strafe.mc.player.motionY, 0.0)).size() > 0 || Strafe.mc.player.collidedVertically) && this.stage > 0) {
                    this.stage = Strafe.mc.player.moveForward != 0.0f || Strafe.mc.player.moveStrafing != 0.0f ? 1 : 0;
                }
                this.moveSpeed = this.lastDist - this.lastDist / (double) (this.mode.getValue() == Mode.Normal ? this.SPEEDY.getValue() : this.SPEEDH.getValue());
            }
            this.moveSpeed = !Strafe.mc.gameSettings.keyBindJump.isKeyDown() && (!InventoryMove.INSTANCE.isOn() || !Keyboard.isKeyDown(Strafe.mc.gameSettings.keyBindJump.getKeyCode()) || Strafe.mc.currentScreen instanceof GuiChat) && Strafe.mc.player.onGround ? this.getBaseMoveSpeed() : Math.max(this.moveSpeed, this.getBaseMoveSpeed());
            double n = Strafe.mc.player.movementInput.moveForward;
            double n2 = Strafe.mc.player.movementInput.moveStrafe;
            double n3 = Strafe.mc.player.rotationYaw;
            if (n == 0.0 && n2 == 0.0) {
                event.setX(0.0);
                event.setZ(0.0);
            } else if (n != 0.0 && n2 != 0.0) {
                n *= Math.sin(0.7853981633974483);
                n2 *= Math.cos(0.7853981633974483);
            }
            double n4 = this.mode.getValue() == Mode.Normal ? (double) this.StrafeH.getValue() : (double) this.StrafeY.getValue();
            event.setX((n * this.moveSpeed * -Math.sin(Math.toRadians(n3)) + n2 * this.moveSpeed * Math.cos(Math.toRadians(n3))) * n4);
            event.setZ((n * this.moveSpeed * Math.cos(Math.toRadians(n3)) - n2 * this.moveSpeed * -Math.sin(Math.toRadians(n3))) * n4);
            ++this.stage;
            event.setCanceled(false);
        }
    }

    public double getBaseMoveSpeed() {
        double n = 0.2873;
        if (Strafe.mc.player.isPotionActive(MobEffects.SPEED)) {
            n *= 1.0 + 0.2 * (double)(Objects.requireNonNull(Strafe.mc.player.getActivePotionEffect(MobEffects.SPEED)).getAmplifier() + 1);
        }
        return n;
    }

    public static enum Mode {
        Normal,
        Strict

    }
}

