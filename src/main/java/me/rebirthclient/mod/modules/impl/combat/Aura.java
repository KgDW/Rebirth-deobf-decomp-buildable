package me.rebirthclient.mod.modules.impl.combat;

import java.awt.Color;
import java.util.Random;
import me.rebirthclient.api.events.impl.Render3DEvent;
import me.rebirthclient.api.events.impl.UpdateWalkingPlayerEvent;
import me.rebirthclient.api.managers.Managers;
import me.rebirthclient.api.managers.impl.SneakManager;
import me.rebirthclient.api.util.EntityUtil;
import me.rebirthclient.api.util.Timer;
import me.rebirthclient.api.util.math.MathUtil;
import me.rebirthclient.api.util.render.ColorUtil;
import me.rebirthclient.api.util.render.RenderUtil;
import me.rebirthclient.mod.modules.Category;
import me.rebirthclient.mod.modules.Module;
import me.rebirthclient.mod.modules.settings.Setting;
import net.minecraft.entity.Entity;
import net.minecraft.entity.monster.EntityGhast;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.inventory.ClickType;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.CPacketEntityAction;
import net.minecraft.util.EnumHand;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.lwjgl.opengl.GL11;

public class Aura
extends Module {
    public static Aura INSTANCE;
    public static Entity target;
    private final Setting<Page> page = this.add(new Setting<>("Settings", Page.GLOBAL));
    public final Setting<Float> range = this.add(new Setting<>("Range", 6.0f, 0.1f, 7.0f, v -> this.page.getValue() == Page.GLOBAL));
    private final Setting<TargetMode> targetMode = this.add(new Setting<>("Filter", TargetMode.DISTANCE, v -> this.page.getValue() == Page.GLOBAL));
    private final Setting<Float> wallRange = this.add(new Setting<>("WallRange", 3.0f, 0.1f, 7.0f, v -> this.page.getValue() == Page.GLOBAL));
    private final Setting<Boolean> rotate = this.add(new Setting<>("Rotate", true, v -> this.page.getValue() == Page.GLOBAL).setParent());
    private final Setting<Boolean> lookBack = this.add(new Setting<>("LookBack", true, v -> this.page.getValue() == Page.GLOBAL && this.rotate.isOpen()));
    private final Setting<Float> yawStep = this.add(new Setting<>("YawStep", 0.3f, 0.1f, 1.0f, v -> this.page.getValue() == Page.GLOBAL && this.rotate.isOpen()));
    private final Setting<Float> pitchAdd = this.add(new Setting<>("PitchAdd", 0.0f, 0.0f, 25.0f, v -> this.page.getValue() == Page.GLOBAL && this.rotate.isOpen()));
    private final Setting<Boolean> randomPitch = this.add(new Setting<>("RandomizePitch", false, v -> this.page.getValue() == Page.GLOBAL && this.rotate.isOpen()));
    private final Setting<Float> amplitude = this.add(new Setting<>("Amplitude", 3.0f, -5.0f, 5.0f, v -> this.page.getValue() == Page.GLOBAL && this.rotate.isOpen() && this.randomPitch.getValue()));
    private final Setting<Boolean> oneEight = this.add(new Setting<>("OneEight", false, v -> this.page.getValue() == Page.GLOBAL).setParent());
    private final Setting<Float> minCps = this.add(new Setting<>("MinCps", 6.0f, 0.0f, 20.0f, v -> this.page.getValue() == Page.GLOBAL && this.oneEight.isOpen()));
    private final Setting<Float> maxCps = this.add(new Setting<>("MaxCps", 9.0f, 0.0f, 20.0f, v -> this.page.getValue() == Page.GLOBAL && this.oneEight.isOpen()));
    private final Setting<Float> randomDelay = this.add(new Setting<>("RandomDelay", 0.0f, 0.0f, 5.0f, v -> this.page.getValue() == Page.GLOBAL));
    private final Setting<Boolean> fovCheck = this.add(new Setting<>("FovCheck", false, v -> this.page.getValue() == Page.GLOBAL).setParent());
    private final Setting<Float> angle = this.add(new Setting<>("Angle", 180.0f, 0.0f, 180.0f, v -> this.page.getValue() == Page.GLOBAL && this.fovCheck.isOpen()));
    private final Setting<Boolean> stopSprint = this.add(new Setting<>("StopSprint", true, v -> this.page.getValue() == Page.GLOBAL));
    private final Setting<Boolean> armorBreak = this.add(new Setting<>("ArmorBreak", false, v -> this.page.getValue() == Page.GLOBAL));
    private final Setting<Boolean> whileEating = this.add(new Setting<>("WhileEating", true, v -> this.page.getValue() == Page.GLOBAL));
    private final Setting<Boolean> weaponOnly = this.add(new Setting<>("WeaponOnly", true, v -> this.page.getValue() == Page.GLOBAL));
    private final Setting<Boolean> tpsSync = this.add(new Setting<>("TpsSync", true, v -> this.page.getValue() == Page.GLOBAL));
    private final Setting<Boolean> packet = this.add(new Setting<>("Packet", false, v -> this.page.getValue() == Page.ADVANCED));
    private final Setting<Boolean> swing = this.add(new Setting<>("Swing", true, v -> this.page.getValue() == Page.GLOBAL));
    private final Setting<Boolean> sneak = this.add(new Setting<>("Sneak", false, v -> this.page.getValue() == Page.ADVANCED));
    private final Setting<RenderMode> render = this.add(new Setting<>("Render", RenderMode.JELLO, v -> this.page.getValue() == Page.GLOBAL));
    private final Setting<Color> targetColor = this.add(new Setting<>("TargetColor", new Color(255, 255, 255, 255), v -> this.page.getValue() == Page.GLOBAL && this.render.getValue() != RenderMode.OFF));
    private final Setting<Boolean> players = this.add(new Setting<>("Players", true, v -> this.page.getValue() == Page.TARGETS));
    private final Setting<Boolean> animals = this.add(new Setting<>("Animals", false, v -> this.page.getValue() == Page.TARGETS));
    private final Setting<Boolean> neutrals = this.add(new Setting<>("Neutrals", false, v -> this.page.getValue() == Page.TARGETS));
    private final Setting<Boolean> others = this.add(new Setting<>("Others", false, v -> this.page.getValue() == Page.TARGETS));
    private final Setting<Boolean> projectiles = this.add(new Setting<>("Projectiles", false, v -> this.page.getValue() == Page.TARGETS));
    private final Setting<Boolean> hostiles = this.add(new Setting<>("Hostiles", true, v -> this.page.getValue() == Page.TARGETS).setParent());
    private final Setting<Boolean> onlyGhasts = this.add(new Setting<>("OnlyGhasts", false, v -> this.hostiles.isOpen() && this.page.getValue() == Page.TARGETS));
    private final Setting<Boolean> delay32k = this.add(new Setting<>("32kDelay", false, v -> this.page.getValue() == Page.ADVANCED));
    private final Setting<Integer> packetAmount32k = this.add(new Setting<>("32kPackets", 2, v -> !this.delay32k.getValue() && this.page.getValue() == Page.ADVANCED));
    private final Setting<Integer> time32k = this.add(new Setting<>("32kTime", 5, 1, 50, v -> this.page.getValue() == Page.ADVANCED));
    private final Setting<Boolean> multi32k = this.add(new Setting<>("Multi32k", false, v -> this.page.getValue() == Page.ADVANCED));
    private final Timer timer = new Timer();

    public Aura() {
        super("KnifeBot", "Attacks entities in radius", Category.COMBAT);
        INSTANCE = this;
    }

    @Override
    public String getInfo() {
        String modeInfo = Managers.TEXT.normalizeCases(this.targetMode.getValue());
        String targetInfo = target instanceof EntityPlayer ? ", " + target.getName() : "";
        return modeInfo + targetInfo;
    }

    @Override
    public void onRender3D(Render3DEvent event) {
        if (target != null) {
            if (this.render.getValue() == RenderMode.OLD) {
                RenderUtil.drawEntityBoxESP(target, this.targetColor.getValue(), true, new Color(255, 255, 255, 130), 0.7f, true, true, 35);
            } else if (this.render.getValue() == RenderMode.JELLO) {
                double everyTime = 1500.0;
                double drawTime = (double)System.currentTimeMillis() % everyTime;
                boolean drawMode = drawTime > everyTime / 2.0;
                double drawPercent = drawTime / (everyTime / 2.0);
                drawPercent = !drawMode ? 1.0 - drawPercent : (drawPercent -= 1.0);
                drawPercent = this.easeInOutQuad(drawPercent);
                Aura.mc.entityRenderer.disableLightmap();
                GL11.glPushMatrix();
                GL11.glDisable(3553);
                GL11.glBlendFunc(770, 771);
                GL11.glEnable(2848);
                GL11.glEnable(3042);
                GL11.glDisable(2929);
                GL11.glDisable(2884);
                GL11.glShadeModel(7425);
                Aura.mc.entityRenderer.disableLightmap();
                double radius = Aura.target.width;
                double height = (double)Aura.target.height + 0.1;
                double x = Aura.target.lastTickPosX + (Aura.target.posX - Aura.target.lastTickPosX) * (double)mc.getRenderPartialTicks() - Aura.mc.renderManager.viewerPosX;
                double y = Aura.target.lastTickPosY + (Aura.target.posY - Aura.target.lastTickPosY) * (double)mc.getRenderPartialTicks() - Aura.mc.renderManager.viewerPosY + height * drawPercent;
                double z = Aura.target.lastTickPosZ + (Aura.target.posZ - Aura.target.lastTickPosZ) * (double)mc.getRenderPartialTicks() - Aura.mc.renderManager.viewerPosZ;
                double eased = height / 3.0 * (drawPercent > 0.5 ? 1.0 - drawPercent : drawPercent) * (double)(drawMode ? -1 : 1);
                for (int segments = 0; segments < 360; segments += 5) {
                    Color color = this.targetColor.getValue();
                    double x1 = x - Math.sin((double)segments * Math.PI / 180.0) * radius;
                    double z1 = z + Math.cos((double)segments * Math.PI / 180.0) * radius;
                    double x2 = x - Math.sin((double)(segments - 5) * Math.PI / 180.0) * radius;
                    double z2 = z + Math.cos((double)(segments - 5) * Math.PI / 180.0) * radius;
                    GL11.glBegin(7);
                    GL11.glColor4f((float)ColorUtil.pulseColor(color, 200, 1).getRed() / 255.0f, (float)ColorUtil.pulseColor(color, 200, 1).getGreen() / 255.0f, (float)ColorUtil.pulseColor(color, 200, 1).getBlue() / 255.0f, 0.0f);
                    GL11.glVertex3d(x1, y + eased, z1);
                    GL11.glVertex3d(x2, y + eased, z2);
                    GL11.glColor4f((float)ColorUtil.pulseColor(color, 200, 1).getRed() / 255.0f, (float)ColorUtil.pulseColor(color, 200, 1).getGreen() / 255.0f, (float)ColorUtil.pulseColor(color, 200, 1).getBlue() / 255.0f, 200.0f);
                    GL11.glVertex3d(x2, y, z2);
                    GL11.glVertex3d(x1, y, z1);
                    GL11.glEnd();
                    GL11.glBegin(2);
                    GL11.glVertex3d(x2, y, z2);
                    GL11.glVertex3d(x1, y, z1);
                    GL11.glEnd();
                }
                GL11.glEnable(2884);
                GL11.glShadeModel(7424);
                GL11.glColor4f(1.0f, 1.0f, 1.0f, 1.0f);
                GL11.glEnable(2929);
                GL11.glDisable(2848);
                GL11.glDisable(3042);
                GL11.glEnable(3553);
                GL11.glPopMatrix();
            }
        }
    }

    @Override
    public void onTick() {
        if (target != null && EntityUtil.invalid(target, this.range.getValue())) {
            target = null;
        }
        if (!this.rotate.getValue()) {
            this.doAura();
        }
        if (this.maxCps.getValue() < this.minCps.getValue()) {
            this.maxCps.setValue(this.minCps.getValue());
        }
    }

    @SubscribeEvent
    public void onUpdateWalkingPlayerEvent(UpdateWalkingPlayerEvent event) {
        if (Aura.fullNullCheck()) {
            return;
        }
        if (event.getStage() == 0 && this.rotate.getValue() && target != null) {
            float[] angle = MathUtil.calcAngle(Aura.mc.player.getPositionEyes(mc.getRenderPartialTicks()), target.getPositionEyes(mc.getRenderPartialTicks()));
            float[] newAngle = Managers.ROTATIONS.injectYawStep(angle, this.yawStep.getValue());
            Managers.ROTATIONS.setRotations(newAngle[0], newAngle[1] + this.pitchAdd.getValue() + (this.randomPitch.getValue() ? (float)Math.random() * this.amplitude.getValue() : 0.0f));
        }
        this.doAura();
        if (this.rotate.getValue() && this.lookBack.getValue()) {
            Managers.ROTATIONS.resetRotations();
        }
    }

    private void doAura() {
        int wait = 0;
        if (this.weaponOnly.getValue() && !EntityUtil.isHoldingWeapon(Aura.mc.player)) {
            target = null;
            return;
        }
        int n = this.oneEight.getValue() || EntityUtil.isHolding32k(Aura.mc.player) && !this.delay32k.getValue() ? (int)(MathUtil.randomBetween(this.minCps.getValue(), this.maxCps.getValue()) - (float)new Random().nextInt(10) + (float)(new Random().nextInt(10) * 100) * (this.tpsSync.getValue() ? Managers.SERVER.getTpsFactor() : 1.0f)) : (wait = (int)((float)EntityUtil.getHitCoolDown(Aura.mc.player) + (float)Math.random() * this.randomDelay.getValue() * 100.0f * (this.tpsSync.getValue() ? Managers.SERVER.getTpsFactor() : 1.0f)));
        if (!this.timer.passedMs(wait) || !this.whileEating.getValue() && Aura.mc.player.isHandActive() && (!Aura.mc.player.getHeldItemOffhand().getItem().equals(Items.SHIELD) || Aura.mc.player.getActiveHand() != EnumHand.OFF_HAND)) {
            return;
        }
        target = this.getTarget();
        if (target == null) {
            return;
        }
        if (EntityUtil.isHolding32k(Aura.mc.player) && !this.delay32k.getValue()) {
            if (this.multi32k.getValue()) {
                for (EntityPlayer player : Aura.mc.world.playerEntities) {
                    if (!EntityUtil.isValid(player, this.range.getValue())) continue;
                    this.teekayAttack(player);
                }
            } else {
                this.teekayAttack(target);
            }
            this.timer.reset();
            return;
        }
        if (this.armorBreak.getValue()) {
            Aura.mc.playerController.windowClick(Aura.mc.player.inventoryContainer.windowId, 9, Aura.mc.player.inventory.currentItem, ClickType.SWAP, Aura.mc.player);
            Managers.INTERACTIONS.attackEntity(target, this.packet.getValue(), this.swing.getValue());
            Aura.mc.playerController.windowClick(Aura.mc.player.inventoryContainer.windowId, 9, Aura.mc.player.inventory.currentItem, ClickType.SWAP, Aura.mc.player);
            Managers.INTERACTIONS.attackEntity(target, this.packet.getValue(), this.swing.getValue());
        } else {
            boolean sneaking = SneakManager.isSneaking;
            boolean sprinting = Aura.mc.player.isSprinting();
            if (this.sneak.getValue()) {
                if (sneaking) {
                    Aura.mc.player.connection.sendPacket(new CPacketEntityAction(Aura.mc.player, CPacketEntityAction.Action.STOP_SNEAKING));
                }
                if (sprinting) {
                    Aura.mc.player.connection.sendPacket(new CPacketEntityAction(Aura.mc.player, CPacketEntityAction.Action.STOP_SPRINTING));
                }
            }
            Managers.INTERACTIONS.attackEntity(target, this.packet.getValue(), this.swing.getValue());
            if (this.sneak.getValue()) {
                if (sprinting) {
                    Aura.mc.player.connection.sendPacket(new CPacketEntityAction(Aura.mc.player, CPacketEntityAction.Action.START_SPRINTING));
                }
                if (sneaking) {
                    Aura.mc.player.connection.sendPacket(new CPacketEntityAction(Aura.mc.player, CPacketEntityAction.Action.START_SNEAKING));
                }
            }
            if (this.stopSprint.getValue()) {
                Aura.mc.player.connection.sendPacket(new CPacketEntityAction(Aura.mc.player, CPacketEntityAction.Action.STOP_SPRINTING));
            }
        }
        this.timer.reset();
    }

    private void teekayAttack(Entity entity) {
        for (int i = 0; i < this.packetAmount32k.getValue(); ++i) {
            this.startEntityAttackThread(entity, i * this.time32k.getValue());
        }
    }

    private void startEntityAttackThread(Entity entity, int time) {
        new Thread(() -> {
            Timer timer = new Timer();
            timer.reset();
            try {
                Thread.sleep(time);
            }
            catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
            }
            Managers.INTERACTIONS.attackEntity(entity, true, this.swing.getValue());
        }).start();
    }

    private Entity getTarget() {
        Entity target = null;
        double distance = 0.0;
        double maxHealth = 36.0;
        for (Entity entity : Aura.mc.world.loadedEntityList) {
            if (!(this.players.getValue() && entity instanceof EntityPlayer || this.animals.getValue() && EntityUtil.isPassive(entity) || this.neutrals.getValue() && EntityUtil.isNeutralMob(entity) || this.hostiles.getValue() && EntityUtil.isMobAggressive(entity) || this.hostiles.getValue() && this.onlyGhasts.getValue() && entity instanceof EntityGhast || this.others.getValue() && EntityUtil.isVehicle(entity)) && (!this.projectiles.getValue() || !EntityUtil.isProjectile(entity)) || EntityUtil.invalid(entity, this.range.getValue()) || !Aura.mc.player.canEntityBeSeen(entity) && !EntityUtil.isFeetVisible(entity) && Aura.mc.player.getDistanceSq(entity) > MathUtil.square(this.wallRange.getValue()) || this.fovCheck.getValue() && !this.isInFov(entity, this.angle.getValue().intValue())) continue;
            if (target == null) {
                target = entity;
                distance = Aura.mc.player.getDistance(entity);
                maxHealth = EntityUtil.getHealth(entity);
                continue;
            }
            if (entity instanceof EntityPlayer && EntityUtil.isArmorLow((EntityPlayer)entity, 10)) {
                target = entity;
                break;
            }
            if (this.targetMode.getValue() == TargetMode.HEALTH && (double)EntityUtil.getHealth(entity) < maxHealth) {
                target = entity;
                distance = Aura.mc.player.getDistance(entity);
                maxHealth = EntityUtil.getHealth(entity);
                continue;
            }
            if (this.targetMode.getValue() != TargetMode.DISTANCE || !((double)Aura.mc.player.getDistance(entity) < distance)) continue;
            target = entity;
            distance = Aura.mc.player.getDistance(entity);
            maxHealth = EntityUtil.getHealth(entity);
        }
        return target;
    }

    private boolean isInFov(Entity entity, float angle) {
        double x = entity.posX - Aura.mc.player.posX;
        double z = entity.posZ - Aura.mc.player.posZ;
        double yaw = Math.atan2(x, z) * 57.29577951308232;
        yaw = -yaw;
        angle = (float)((double)angle * 0.5);
        double angleDifference = (((double)Aura.mc.player.rotationYaw - yaw) % 360.0 + 540.0) % 360.0 - 180.0;
        return angleDifference > 0.0 && angleDifference < (double)angle || (double)(-angle) < angleDifference && angleDifference < 0.0;
    }

    private double easeInOutQuad(double x) {
        return x < 0.5 ? 2.0 * x * x : 1.0 - Math.pow(-2.0 * x + 2.0, 2.0) / 2.0;
    }

    private static enum TargetMode {
        DISTANCE,
        HEALTH

    }

    private static enum RenderMode {
        OLD,
        JELLO,
        OFF

    }

    private static enum Page {
        GLOBAL,
        TARGETS,
        ADVANCED

    }
}

