package me.rebirthclient.mod.modules.impl.movement;

import me.rebirthclient.api.events.impl.ElytraEvent;
import me.rebirthclient.api.managers.Managers;
import me.rebirthclient.api.util.MovementUtil;
import me.rebirthclient.api.util.Timer;
import me.rebirthclient.mod.modules.Category;
import me.rebirthclient.mod.modules.Module;
import me.rebirthclient.mod.modules.settings.Setting;
import net.minecraft.entity.Entity;
import net.minecraft.entity.MoverType;
import net.minecraft.item.ItemElytra;
import net.minecraft.item.ItemStack;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.CPacketEntityAction;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class ElytraFly
extends Module {
    public static ElytraFly INSTANCE = new ElytraFly();
    private static boolean hasElytra = false;
    private final Setting<Boolean> instantFly = this.register(new Setting<>("InstantFly", true));
    private final Setting<Float> timeout = this.register(new Setting<>("Timeout", 0.5f, 0.1f, 1.0f));
    public final Setting<Float> upFactor = this.register(new Setting<>("UpFactor", 1.0f, 0.0f, 10.0f));
    public final Setting<Float> downFactor = this.register(new Setting<>("DownFactor", 1.0f, 0.0f, 10.0f));
    public final Setting<Float> speed = this.register(new Setting<>("Speed", 1.0f, 0.1f, 10.0f));
    private final Setting<Float> sneakDownSpeed = this.register(new Setting<>("DownSpeed", 1.0f, 0.1f, 10.0f));
    public final Setting<Boolean> boostTimer = this.register(new Setting<>("Timer", true));
    public final Setting<Boolean> speedLimit = this.register(new Setting<>("SpeedLimit", true));
    public final Setting<Float> maxSpeed = this.register(new Setting<>("MaxSpeed", 2.5f, 0.1f, 10.0f, v -> this.speedLimit.getValue()));
    public final Setting<Boolean> noDrag = new Setting<>("NoDrag", false);
    private final Timer instantFlyTimer = new Timer();
    private final Timer strictTimer = new Timer();
    private boolean hasTouchedGround = false;

    public ElytraFly() {
        super("ElytraFly", "\u659c\u890d\u890b\u890c\u8918 \u5199\u8c22\u891f 2\u659c", Category.MOVEMENT);
        INSTANCE = this;
    }

    public static double[] directionSpeed(double speed) {
        float forward = ElytraFly.mc.player.movementInput.moveForward;
        float side = ElytraFly.mc.player.movementInput.moveStrafe;
        float yaw = ElytraFly.mc.player.prevRotationYaw + (ElytraFly.mc.player.rotationYaw - ElytraFly.mc.player.prevRotationYaw) * mc.getRenderPartialTicks();
        if (forward != 0.0f) {
            if (side > 0.0f) {
                yaw += (float)(forward > 0.0f ? -45 : 45);
            } else if (side < 0.0f) {
                yaw += (float)(forward > 0.0f ? 45 : -45);
            }
            side = 0.0f;
            if (forward > 0.0f) {
                forward = 1.0f;
            } else if (forward < 0.0f) {
                forward = -1.0f;
            }
        }
        double sin = Math.sin(Math.toRadians(yaw + 90.0f));
        double cos = Math.cos(Math.toRadians(yaw + 90.0f));
        double posX = (double)forward * speed * cos + (double)side * speed * sin;
        double posZ = (double)forward * speed * sin - (double)side * speed * cos;
        return new double[]{posX, posZ};
    }

    @Override
    public void onEnable() {
        if (ElytraFly.mc.player != null) {
            if (!ElytraFly.mc.player.isCreative()) {
                ElytraFly.mc.player.capabilities.allowFlying = false;
            }
            ElytraFly.mc.player.capabilities.isFlying = false;
        }
        hasElytra = false;
    }

    @Override
    public void onDisable() {
        if (ElytraFly.mc.player != null) {
            if (!ElytraFly.mc.player.isCreative()) {
                ElytraFly.mc.player.capabilities.allowFlying = false;
            }
            ElytraFly.mc.player.capabilities.isFlying = false;
        }
        Managers.TIMER.reset();
        hasElytra = false;
    }

    @Override
    public void onUpdate() {
        if (ElytraFly.fullNullCheck()) {
            return;
        }
        if (ElytraFly.mc.player.onGround) {
            this.hasTouchedGround = true;
        }
        for (ItemStack is : ElytraFly.mc.player.getArmorInventoryList()) {
            if (is.getItem() instanceof ItemElytra) {
                hasElytra = true;
                break;
            }
            hasElytra = false;
        }
        if (this.strictTimer.passedMs(1500L) && !this.strictTimer.passedMs(2000L) || ElytraFly.mc.player.isElytraFlying() && Managers.TIMER.get() == 0.3f) {
            Managers.TIMER.reset();
        }
        if (!ElytraFly.mc.player.isElytraFlying()) {
            if (this.hasTouchedGround && this.boostTimer.getValue() && !ElytraFly.mc.player.onGround) {
                Managers.TIMER.set(0.3f);
            }
            if (!ElytraFly.mc.player.onGround && this.instantFly.getValue() && ElytraFly.mc.player.motionY < 0.0) {
                if (!this.instantFlyTimer.passedMs((long)(1000.0f * this.timeout.getValue()))) {
                    return;
                }
                this.instantFlyTimer.reset();
                ElytraFly.mc.player.connection.sendPacket(new CPacketEntityAction(ElytraFly.mc.player, CPacketEntityAction.Action.START_FALL_FLYING));
                this.hasTouchedGround = false;
                this.strictTimer.reset();
            }
            return;
        }
        ElytraFly.mc.player.isElytraFlying();
    }

    @SubscribeEvent
    public void onElytra(ElytraEvent event) {
        if (ElytraFly.fullNullCheck() || !hasElytra || !ElytraFly.mc.player.isElytraFlying()) {
            return;
        }
        if (event.getEntity() == ElytraFly.mc.player && ElytraFly.mc.player.isServerWorld() || ElytraFly.mc.player.canPassengerSteer() && !ElytraFly.mc.player.isInWater() || ElytraFly.mc.player.capabilities.isFlying && !ElytraFly.mc.player.isInLava() || ElytraFly.mc.player.capabilities.isFlying && ElytraFly.mc.player.isElytraFlying()) {
            event.setCanceled(true);
            Vec3d lookVec = ElytraFly.mc.player.getLookVec();
            double lookDist = Math.sqrt(lookVec.x * lookVec.x + lookVec.z * lookVec.z);
            double motionDist = Math.sqrt(ElytraFly.mc.player.motionX * ElytraFly.mc.player.motionX + ElytraFly.mc.player.motionZ * ElytraFly.mc.player.motionZ);
            if (ElytraFly.mc.gameSettings.keyBindSneak.isKeyDown()) {
                ElytraFly.mc.player.motionY = -this.sneakDownSpeed.getValue();
            } else if (!MovementUtil.isJumping()) {
                ElytraFly.mc.player.motionY = -3.0E-14 * (double) this.downFactor.getValue();
            }
            if (MovementUtil.isJumping()) {
                if (motionDist > (double)(this.upFactor.getValue() / this.upFactor.getMaxValue())) {
                    double rawUpSpeed = motionDist * 0.01325;
                    ElytraFly.mc.player.motionY += rawUpSpeed * 3.2;
                    ElytraFly.mc.player.motionX -= lookVec.x * rawUpSpeed / lookDist;
                    ElytraFly.mc.player.motionZ -= lookVec.z * rawUpSpeed / lookDist;
                } else {
                    double[] dir = ElytraFly.directionSpeed(this.speed.getValue());
                    ElytraFly.mc.player.motionX = dir[0];
                    ElytraFly.mc.player.motionZ = dir[1];
                }
            }
            if (lookDist > 0.0) {
                ElytraFly.mc.player.motionX += (lookVec.x / lookDist * motionDist - ElytraFly.mc.player.motionX) * 0.1;
                ElytraFly.mc.player.motionZ += (lookVec.z / lookDist * motionDist - ElytraFly.mc.player.motionZ) * 0.1;
            }
            if (!MovementUtil.isJumping()) {
                double[] dir = ElytraFly.directionSpeed(this.speed.getValue());
                ElytraFly.mc.player.motionX = dir[0];
                ElytraFly.mc.player.motionZ = dir[1];
            }
            if (!this.noDrag.getValue()) {
                ElytraFly.mc.player.motionX *= 0.99f;
                ElytraFly.mc.player.motionY *= 0.98f;
                ElytraFly.mc.player.motionZ *= 0.99f;
            }
            double finalDist = Math.sqrt(ElytraFly.mc.player.motionX * ElytraFly.mc.player.motionX + ElytraFly.mc.player.motionZ * ElytraFly.mc.player.motionZ);
            if (this.speedLimit.getValue() && finalDist > (double) this.maxSpeed.getValue()) {
                ElytraFly.mc.player.motionX *= (double) this.maxSpeed.getValue() / finalDist;
                ElytraFly.mc.player.motionZ *= (double) this.maxSpeed.getValue() / finalDist;
            }
            ElytraFly.mc.player.move(MoverType.SELF, ElytraFly.mc.player.motionX, ElytraFly.mc.player.motionY, ElytraFly.mc.player.motionZ);
        }
    }
}

