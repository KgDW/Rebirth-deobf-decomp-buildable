package me.rebirthclient.mod.modules.impl.movement;

import me.rebirthclient.api.events.impl.StepEvent;
import me.rebirthclient.api.managers.Managers;
import me.rebirthclient.api.managers.impl.SneakManager;
import me.rebirthclient.api.util.EntityUtil;
import me.rebirthclient.api.util.MovementUtil;
import me.rebirthclient.api.util.Timer;
import me.rebirthclient.mod.modules.Category;
import me.rebirthclient.mod.modules.Module;
import me.rebirthclient.mod.modules.impl.exploit.Clip;
import me.rebirthclient.mod.modules.impl.movement.HoleSnap;
import me.rebirthclient.mod.modules.impl.player.Freecam;
import me.rebirthclient.mod.modules.settings.Setting;
import net.minecraft.entity.Entity;
import net.minecraft.entity.passive.EntityHorse;
import net.minecraft.entity.passive.EntityLlama;
import net.minecraft.entity.passive.EntityMule;
import net.minecraft.entity.passive.EntityPig;
import net.minecraft.init.Blocks;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class NewStep
extends Module {
    private final Timer stepTimer = new Timer();
    public final Setting<Float> height = this.register(new Setting<>("Height", 2.0f, 1.0f, 2.5f));
    public final Setting<Boolean> entityStep = this.add(new Setting<>("EntityStep", false));
    public final Setting<Boolean> useTimer = this.add(new Setting<>("Timer", true));
    public final Setting<Boolean> strict = this.add(new Setting<>("Strict", false));
    private final Setting<Boolean> pauseBurrow = this.add(new Setting<>("PauseBurrow", true));
    private final Setting<Boolean> pauseSneak = this.add(new Setting<>("PauseSneak", true));
    private final Setting<Boolean> pauseWeb = this.add(new Setting<>("PauseWeb", true));
    private final Setting<Boolean> onlyMoving = this.add(new Setting<>("OnlyMoving", true));
    public final Setting<Integer> stepDelay = this.register(new Setting<>("StepDelay", 200, 0, 1000));
    private final Setting<Mode> mode = this.add(new Setting<>("Mode", Mode.NORMAL));
    public static boolean timer;
    private Entity entityRiding;

    public NewStep() {
        super("NewStep", "\u890f\u82af\u5199\u61c8\u890c\u891c \u950c\u82af \u659c\u8c22\u82af\u6cfb\u90aa\u5c51 1 \u61c8\u8c22\u61c8 2 \u659c\u8c22\u82af\u6cfb\u90aa", Category.MOVEMENT);
    }

    @Override
    public void onDisable() {
        super.onDisable();
        NewStep.mc.player.stepHeight = 0.6f;
        if (this.entityRiding != null) {
            this.entityRiding.stepHeight = this.entityRiding instanceof EntityHorse || this.entityRiding instanceof EntityLlama || this.entityRiding instanceof EntityMule || this.entityRiding instanceof EntityPig && this.entityRiding.isBeingRidden() && ((EntityPig)this.entityRiding).canBeSteered() ? 1.0f : 0.5f;
        }
    }

    @Override
    public void onUpdate() {
        if ((NewStep.mc.world.getBlockState(EntityUtil.getPlayerPos()).getBlock() == Blocks.PISTON_HEAD || NewStep.mc.world.getBlockState(EntityUtil.getPlayerPos()).getBlock() == Blocks.OBSIDIAN || NewStep.mc.world.getBlockState(EntityUtil.getPlayerPos()).getBlock() == Blocks.ENDER_CHEST || NewStep.mc.world.getBlockState(EntityUtil.getPlayerPos()).getBlock() == Blocks.BEDROCK) && this.pauseBurrow.getValue()) {
            NewStep.mc.player.stepHeight = 0.6f;
            return;
        }
        if ((NewStep.mc.world.getBlockState(EntityUtil.getPlayerPos().up()).getBlock() == Blocks.PISTON_HEAD || NewStep.mc.world.getBlockState(EntityUtil.getPlayerPos().up()).getBlock() == Blocks.OBSIDIAN || NewStep.mc.world.getBlockState(EntityUtil.getPlayerPos().up()).getBlock() == Blocks.ENDER_CHEST || NewStep.mc.world.getBlockState(EntityUtil.getPlayerPos().up()).getBlock() == Blocks.BEDROCK) && this.pauseBurrow.getValue()) {
            NewStep.mc.player.stepHeight = 0.6f;
            return;
        }
        if (this.pauseWeb.getValue() && NewStep.mc.player.isInWeb) {
            NewStep.mc.player.stepHeight = 0.6f;
            return;
        }
        if (SneakManager.isSneaking && this.pauseSneak.getValue()) {
            NewStep.mc.player.stepHeight = 0.6f;
            return;
        }
        if (this.onlyMoving.getValue() && !MovementUtil.isMoving() && HoleSnap.INSTANCE.isOff()) {
            NewStep.mc.player.stepHeight = 0.6f;
            return;
        }
        if (Clip.INSTANCE.isOn()) {
            NewStep.mc.player.stepHeight = 0.6f;
            return;
        }
        if (NewStep.mc.player.capabilities.isFlying || Freecam.INSTANCE.isOn()) {
            NewStep.mc.player.stepHeight = 0.6f;
            return;
        }
        if (EntityUtil.isInLiquid()) {
            NewStep.mc.player.stepHeight = 0.6f;
            return;
        }
        if (timer && NewStep.mc.player.onGround) {
            Managers.TIMER.timer = 1.0f;
            timer = false;
        }
        if (NewStep.mc.player.onGround && this.stepTimer.passedMs(this.stepDelay.getValue())) {
            if (NewStep.mc.player.isRiding() && NewStep.mc.player.getRidingEntity() != null) {
                this.entityRiding = NewStep.mc.player.getRidingEntity();
                if (this.entityStep.getValue()) {
                    NewStep.mc.player.getRidingEntity().stepHeight = this.height.getValue();
                }
            } else {
                NewStep.mc.player.stepHeight = this.height.getValue();
            }
        } else if (NewStep.mc.player.isRiding() && NewStep.mc.player.getRidingEntity() != null) {
            this.entityRiding = NewStep.mc.player.getRidingEntity();
            if (this.entityRiding != null) {
                this.entityRiding.stepHeight = this.entityRiding instanceof EntityHorse || this.entityRiding instanceof EntityLlama || this.entityRiding instanceof EntityMule || this.entityRiding instanceof EntityPig && this.entityRiding.isBeingRidden() && ((EntityPig)this.entityRiding).canBeSteered() ? 1.0f : 0.5f;
            }
        } else {
            NewStep.mc.player.stepHeight = 0.6f;
        }
    }

    @SubscribeEvent
    public void onStep(StepEvent event) {
        if (this.mode.getValue().equals(Mode.NORMAL)) {
            double stepHeight = event.getAxisAlignedBB().minY - NewStep.mc.player.posY;
            if (stepHeight <= 0.0 || stepHeight > (double) this.height.getValue()) {
                return;
            }
            double[] offsets = this.getOffset(stepHeight);
            if (offsets != null && offsets.length > 1) {
                if (this.useTimer.getValue()) {
                    Managers.TIMER.timer = 1.0f / (float)offsets.length;
                    timer = true;
                }
                for (double offset : offsets) {
                    NewStep.mc.player.connection.sendPacket(new CPacketPlayer.Position(NewStep.mc.player.posX, NewStep.mc.player.posY + offset, NewStep.mc.player.posZ, false));
                }
            }
            this.stepTimer.reset();
        }
    }

    public double[] getOffset(double height) {
        if (height == 0.75) {
            if (this.strict.getValue()) {
                return new double[]{0.42, 0.753, 0.75};
            }
            return new double[]{0.42, 0.753};
        }
        if (height == 0.8125) {
            if (this.strict.getValue()) {
                return new double[]{0.39, 0.7, 0.8125};
            }
            return new double[]{0.39, 0.7};
        }
        if (height == 0.875) {
            if (this.strict.getValue()) {
                return new double[]{0.39, 0.7, 0.875};
            }
            return new double[]{0.39, 0.7};
        }
        if (height == 1.0) {
            if (this.strict.getValue()) {
                return new double[]{0.42, 0.753, 1.0};
            }
            return new double[]{0.42, 0.753};
        }
        if (height == 1.5) {
            return new double[]{0.42, 0.75, 1.0, 1.16, 1.23, 1.2};
        }
        if (height == 2.0) {
            return new double[]{0.42, 0.78, 0.63, 0.51, 0.9, 1.21, 1.45, 1.43};
        }
        if (height == 2.5) {
            return new double[]{0.425, 0.821, 0.699, 0.599, 1.022, 1.372, 1.652, 1.869, 2.019, 1.907};
        }
        return null;
    }

    public static enum Mode {
        NORMAL,
        VANILLA

    }
}

