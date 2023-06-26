package me.rebirthclient.mod.modules.impl.movement;

import java.awt.Color;
import java.util.Objects;
import me.rebirthclient.api.events.impl.MotionEvent;
import me.rebirthclient.api.events.impl.PacketEvent;
import me.rebirthclient.api.events.impl.Render3DEvent;
import me.rebirthclient.api.managers.Managers;
import me.rebirthclient.api.util.EntityUtil;
import me.rebirthclient.api.util.MovementUtil;
import me.rebirthclient.api.util.render.RenderUtil;
import me.rebirthclient.mod.modules.Category;
import me.rebirthclient.mod.modules.Module;
import me.rebirthclient.mod.modules.impl.combat.Aura;
import me.rebirthclient.mod.modules.settings.Setting;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.network.play.server.SPacketEntityVelocity;
import net.minecraft.potion.Potion;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class TargetStrafe
extends Module {
    private final Setting<Float> reversedDistance = this.add(new Setting<>("ReversedDistance", 3.0f, 1.0f, 6.0f));
    private final Setting<Float> speedIfUsing = this.add(new Setting<>("Speed if using", 0.1f, 0.1f, 2.0f));
    private final Setting<Float> range = this.add(new Setting<>("StrafeDistance", 2.4f, 0.1f, 6.0f));
    private final Setting<Float> spd = this.add(new Setting<>("StrafeSpeed", 0.23f, 0.1f, 2.0f));
    private final Setting<Boolean> reversed = this.add(new Setting<>("Reversed", false));
    private final Setting<Boolean> whenMoving = this.add(new Setting<>("WhenMoving", true));
    private final Setting<Boolean> autoJump = this.add(new Setting<>("AutoJump", true));
    private final Setting<Boolean> smartStrafe = this.add(new Setting<>("SmartStrafe", true));
    private final Setting<Boolean> usingItemCheck = this.add(new Setting<>("EatingSlowDown", false));
    private final Setting<Boolean> speedIfPotion = this.add(new Setting<>("Speed if Potion ", true));
    private final Setting<Float> potionSpeed = this.add(new Setting<>("PotionSpeed", 0.45f, 0.1f, 2.0f, v -> this.speedIfPotion.getValue()));
    private final Setting<Boolean> autoThirdPerson = this.register(new Setting<>("AutoThirdPerson", Boolean.TRUE));
    private final Setting<Float> targetRange = this.register(new Setting<>("TargetRange", 3.8f, 0.1f, 7.0f));
    private final Setting<Boolean> drawRadius = this.add(new Setting<>("drawRadius", true));
    private final Setting<Boolean> strafeBoost = this.add(new Setting<>("StrafeBoost", false));
    private final Setting<Boolean> addddd = this.add(new Setting<>("add", false));
    private final Setting<Float> reduction = this.add(new Setting<>("reduction", 2.0f, 1.0f, 5.0f));
    private final Setting<Float> velocityUse = this.add(new Setting<>("velocityUse", 50000.0f, 0.1f, 100000.0f));
    private final Setting<Integer> boostTicks = this.register(new Setting<>("BoostTicks", 5, 0, 60));
    private final Setting<Integer> boostDecr = this.register(new Setting<>("BoostDecr", 5, 0, 5000));
    EntityPlayer strafeTarget = null;
    int boostticks = 0;
    float speedy = 1.0f;
    int velocity = 0;
    private float wrap = 0.0f;
    private boolean switchDir = true;

    public TargetStrafe() {
        super("TargetStrafe", "\u8899\u8909\u90aa\u8916\u90aa\u890c\u891c\u890b\u891f \u80c1\u82af\u6cfb\u8909\u890d\u8c10 \u8911\u68b0\u8c22\u61c8", Category.MOVEMENT);
    }

    @Override
    public void onEnable() {
        this.wrap = 0.0f;
        this.switchDir = true;
        Managers.TIMER.set(1.0f);
        this.velocity = 0;
    }

    @Override
    public void onDisable() {
        if (this.autoThirdPerson.getValue()) {
            TargetStrafe.mc.gameSettings.thirdPersonView = 0;
        }
    }

    public boolean needToSwitch(double x, double z) {
        if (TargetStrafe.mc.gameSettings.keyBindLeft.isPressed() || TargetStrafe.mc.gameSettings.keyBindRight.isPressed()) {
            return true;
        }
        for (int i = (int)(TargetStrafe.mc.player.posY + 4.0); i >= 0; --i) {
            BlockPos playerPos = new BlockPos(x, i, z);
            if (TargetStrafe.mc.world.getBlockState(playerPos).getBlock().equals(Blocks.LAVA) || TargetStrafe.mc.world.getBlockState(playerPos).getBlock().equals(Blocks.FIRE)) {
                return true;
            }
            if (TargetStrafe.mc.world.isAirBlock(playerPos)) continue;
            return false;
        }
        return true;
    }

    private float toDegree(double x, double z) {
        return (float)(Math.atan2(z - TargetStrafe.mc.player.posZ, x - TargetStrafe.mc.player.posX) * 180.0 / Math.PI) - 90.0f;
    }

    @Override
    public void onUpdate() {
        if (this.whenMoving.getValue() && !MovementUtil.isMoving()) {
            return;
        }
        if (Aura.target != null) {
            if (!(Aura.target instanceof EntityPlayer)) {
                return;
            }
            this.strafeTarget = (EntityPlayer)Aura.target;
        } else {
            this.strafeTarget = null;
        }
    }

    @SubscribeEvent
    public void onUpdateWalkingPlayerPre(MotionEvent event) {
        if (this.whenMoving.getValue() && !MovementUtil.isMoving()) {
            return;
        }
        if (this.strafeTarget == null) {
            return;
        }
        if (TargetStrafe.mc.player.getDistanceSq(this.strafeTarget) < 0.2) {
            return;
        }
        if (this.autoThirdPerson.getValue()) {
            if (this.strafeTarget.getHealth() > 0.0f && TargetStrafe.mc.player.getDistance(this.strafeTarget) <= this.targetRange.getValue() && TargetStrafe.mc.player.getHealth() > 0.0f) {
                if (Aura.INSTANCE.isOn()) {
                    TargetStrafe.mc.gameSettings.thirdPersonView = 1;
                }
            } else {
                TargetStrafe.mc.gameSettings.thirdPersonView = 0;
            }
        }
        if (TargetStrafe.mc.player.getDistance(this.strafeTarget) <= this.targetRange.getValue()) {
            if (EntityUtil.getHealth(this.strafeTarget) > 0.0f && this.autoJump.getValue() && Aura.INSTANCE.isOn() && TargetStrafe.mc.player.onGround) {
                TargetStrafe.mc.player.jump();
            }
            if (EntityUtil.getHealth(this.strafeTarget) > 0.0f) {
                float speed;
                EntityPlayer target = this.strafeTarget;
                if (target == null || TargetStrafe.mc.player.ticksExisted < 20) {
                    return;
                }
                this.speedy = this.speedIfPotion.getValue() ? (TargetStrafe.mc.player.isPotionActive(Objects.requireNonNull(Potion.getPotionFromResourceLocation("speed"))) ? this.potionSpeed.getValue() : this.spd.getValue()) : this.spd.getValue();
                float f = speed = TargetStrafe.mc.gameSettings.keyBindUseItem.isKeyDown() && this.usingItemCheck.getValue() ? this.speedIfUsing.getValue() : this.speedy;
                if ((float)this.velocity > this.velocityUse.getValue() && this.strafeBoost.getValue()) {
                    if (this.velocity < 0) {
                        this.velocity = 0;
                    }
                    speed = this.addddd.getValue() ? (speed += (float)this.velocity / 8000.0f / this.reduction.getValue()) : (float)this.velocity / 8000.0f / this.reduction.getValue();
                    ++this.boostticks;
                    this.velocity -= this.boostDecr.getValue();
                }
                if (this.boostticks >= this.boostTicks.getValue()) {
                    this.boostticks = 0;
                    this.velocity = 0;
                }
                this.wrap = (float)Math.atan2(TargetStrafe.mc.player.posZ - target.posZ, TargetStrafe.mc.player.posX - target.posX);
                this.wrap += this.switchDir ? speed / TargetStrafe.mc.player.getDistance(target) : -(speed / TargetStrafe.mc.player.getDistance(target));
                double x = target.posX + (double) this.range.getValue() * Math.cos(this.wrap);
                double z = target.posZ + (double) this.range.getValue() * Math.sin(this.wrap);
                if (this.smartStrafe.getValue() && this.needToSwitch(x, z)) {
                    this.switchDir = !this.switchDir;
                    this.wrap += 2.0f * (this.switchDir ? speed / TargetStrafe.mc.player.getDistance(target) : -(speed / TargetStrafe.mc.player.getDistance(target)));
                    x = target.posX + (double) this.range.getValue() * Math.cos(this.wrap);
                    z = target.posZ + (double) this.range.getValue() * Math.sin(this.wrap);
                }
                float searchValue = this.reversed.getValue() && TargetStrafe.mc.player.getDistance(this.strafeTarget) < this.reversedDistance.getValue() ? -90.0f : 0.0f;
                float reversedValue = !TargetStrafe.mc.gameSettings.keyBindLeft.isKeyDown() && !TargetStrafe.mc.gameSettings.keyBindRight.isKeyDown() ? searchValue : 0.0f;
                TargetStrafe.mc.player.motionX = (double)speed * -Math.sin((float)Math.toRadians(this.toDegree(x + (double)reversedValue, z + (double)reversedValue)));
                TargetStrafe.mc.player.motionZ = (double)speed * Math.cos((float)Math.toRadians(this.toDegree(x + (double)reversedValue, z + (double)reversedValue)));
            }
        }
    }

    @Override
    @SubscribeEvent
    public void onRender3D(Render3DEvent e) {
        if (Aura.target != null && this.drawRadius.getValue()) {
            RenderUtil.drawCircle((float)Aura.target.posX - 0.5f, (float)(Aura.target.posY + 1.0), (float)Aura.target.posZ - 0.5f, this.range.getValue(), new Color(255, 255, 255, 255));
        }
    }

    @SubscribeEvent(priority=EventPriority.HIGHEST)
    public void onPacketReceive(PacketEvent.Receive event) {
        if (TargetStrafe.fullNullCheck()) {
            return;
        }
        if (event.getPacket() instanceof SPacketEntityVelocity && ((SPacketEntityVelocity)event.getPacket()).getEntityID() == TargetStrafe.mc.player.getEntityId()) {
            SPacketEntityVelocity pack = event.getPacket();
            int vX = pack.getMotionX();
            int vZ = pack.getMotionZ();
            if (vX < 0) {
                vX *= -1;
            }
            if (vZ < 0) {
                vZ *= -1;
            }
            this.velocity = vX + vZ;
        }
    }
}

