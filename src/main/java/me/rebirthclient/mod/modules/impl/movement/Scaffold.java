package me.rebirthclient.mod.modules.impl.movement;

import me.rebirthclient.api.events.impl.MotionEvent;
import me.rebirthclient.api.events.impl.MoveEvent;
import me.rebirthclient.api.util.BlockUtil;
import me.rebirthclient.api.util.EntityUtil;
import me.rebirthclient.api.util.InventoryUtil;
import me.rebirthclient.api.util.MovementUtil;
import me.rebirthclient.api.util.Timer;
import me.rebirthclient.mod.modules.Category;
import me.rebirthclient.mod.modules.Module;
import me.rebirthclient.mod.modules.settings.Setting;
import net.minecraft.block.Block;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.Entity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class Scaffold
extends Module {
    private final Setting<Boolean> safeWalk = this.add(new Setting<>("SafeWalk", false));
    private final Setting<Boolean> rotate = this.add(new Setting<>("Rotate", true).setParent());
    private final Setting<Integer> rotateTimerSetting = this.add(new Setting<>("RotateTimer", 800, 0, 1000, v -> this.rotate.isOpen()));
    private final Setting<Boolean> packet = this.add(new Setting<>("Packet", false));
    private final Setting<Integer> placeDelay = this.add(new Setting<>("PlaceDelay", 100, 0, 500));
    private final Setting<SwapMode> autoSwap = this.add(new Setting<>("AutoSwap", SwapMode.SILENT));
    private final Setting<Boolean> search = this.add(new Setting<>("Search", true).setParent());
    private final Setting<Boolean> allowUp = this.add(new Setting<>("AllowUp", false, v -> this.search.isOpen()));
    private final Setting<Float> range = this.add(new Setting<>("Range", 3.5f, 2.5f, 5.0f, v -> this.search.isOpen()));
    private final Timer timer = new Timer();
    private final Timer placeTimer = new Timer();
    private final Setting<Boolean> tower = this.add(new Setting<>("Tower", true));
    private final Setting<Boolean> down = this.add(new Setting<>("Down", true));
    private final Setting<Boolean> sameY = this.add(new Setting<>("SameY", false).setParent());
    private final Setting<Float> yCheck = this.add(new Setting<>("YCheck", 2.5f, 2.5f, 12.0f, v -> this.sameY.isOpen()));
    private final Setting<Boolean> airCheck = this.add(new Setting<>("AirCheck", true, v -> this.sameY.isOpen()));
    private BlockPos PlacePos;
    private final Timer rotateTimer = new Timer();
    private float lastYaw = 0.0f;
    private float lastPitch = 0.0f;

    public Scaffold() {
        super("Scaffold", "Places Blocks underneath you", Category.MOVEMENT);
    }

    @SubscribeEvent
    public void onMove(MoveEvent event) {
        if (!this.safeWalk.getValue()) {
            return;
        }
        if (this.down.getValue() && Scaffold.mc.gameSettings.keyBindSprint.isKeyDown()) {
            return;
        }
        double x = event.getX();
        double y = event.getY();
        double z = event.getZ();
        if (Scaffold.mc.player.onGround) {
            double increment = 0.05;
            while (x != 0.0 && this.isOffsetBBEmpty(x, -1.0, 0.0)) {
                if (x < increment && x >= -increment) {
                    x = 0.0;
                    continue;
                }
                if (x > 0.0) {
                    x -= increment;
                    continue;
                }
                x += increment;
            }
            while (z != 0.0 && this.isOffsetBBEmpty(0.0, -1.0, z)) {
                if (z < increment && z >= -increment) {
                    z = 0.0;
                    continue;
                }
                if (z > 0.0) {
                    z -= increment;
                    continue;
                }
                z += increment;
            }
            while (x != 0.0 && z != 0.0 && this.isOffsetBBEmpty(x, -1.0, z)) {
                double d = x < increment && x >= -increment ? 0.0 : (x = x > 0.0 ? x - increment : x + increment);
                if (z < increment && z >= -increment) {
                    z = 0.0;
                    continue;
                }
                if (z > 0.0) {
                    z -= increment;
                    continue;
                }
                z += increment;
            }
        }
        event.setX(x);
        event.setY(y);
        event.setZ(z);
    }

    public boolean isOffsetBBEmpty(double offsetX, double offsetY, double offsetZ) {
        EntityPlayerSP playerSP = Scaffold.mc.player;
        return Scaffold.mc.world.getCollisionBoxes(playerSP, playerSP.getEntityBoundingBox().offset(offsetX, offsetY, offsetZ)).isEmpty();
    }

    @Override
    public void onEnable() {
        this.PlacePos = new BlockPos(Scaffold.mc.player.posX, Scaffold.mc.player.posY - 1.0, Scaffold.mc.player.posZ);
        this.timer.reset();
    }

    @Override
    public void onTick() {
        if (!this.placeTimer.passedMs(this.placeDelay.getValue())) {
            return;
        }
        if (this.PlacePos == null) {
            this.PlacePos = new BlockPos(Scaffold.mc.player.posX, Scaffold.mc.player.posY - 1.0, Scaffold.mc.player.posZ);
        }
        this.doScaffold();
    }

    public void doScaffold() {
        if (!MovementUtil.isJumping()) {
            this.timer.reset();
        }
        this.PlacePos = !this.sameY.getValue() || Scaffold.mc.player.posY - (double)this.PlacePos.getY() > (double) this.yCheck.getValue() || this.airCheck.getValue() && !BlockUtil.canReplace(new BlockPos(Scaffold.mc.player.posX, Scaffold.mc.player.posY - 1.0, Scaffold.mc.player.posZ)) || MovementUtil.isJumping() && !MovementUtil.isMoving() || new BlockPos(Scaffold.mc.player.posX, Scaffold.mc.player.posY - 1.0, Scaffold.mc.player.posZ).getY() < this.PlacePos.getY() ? new BlockPos(Scaffold.mc.player.posX, Scaffold.mc.player.posY - 1.0, Scaffold.mc.player.posZ) : new BlockPos(Scaffold.mc.player.posX, this.PlacePos.getY(), Scaffold.mc.player.posZ);
        if (this.down.getValue() && Scaffold.mc.gameSettings.keyBindSprint.isKeyDown()) {
            this.PlacePos = new BlockPos(Scaffold.mc.player.posX, Scaffold.mc.player.posY - 2.0, Scaffold.mc.player.posZ);
        }
        this.place(this.PlacePos);
    }

    @SubscribeEvent
    public final void onMotion(MotionEvent event) {
        if (!this.rotateTimer.passedMs(this.rotateTimerSetting.getValue()) && this.rotate.getValue()) {
            event.setYaw(this.lastYaw);
            event.setPitch(this.lastPitch);
        }
    }

    private void faceVector(Vec3d vec) {
        float[] rotations = EntityUtil.getLegitRotations(vec);
        this.lastYaw = rotations[0];
        this.lastPitch = rotations[1];
    }

    public void place(BlockPos pos) {
        if (pos.getDistance((int)Scaffold.mc.player.posX, (int)Scaffold.mc.player.posY, (int)Scaffold.mc.player.posZ) > 6.0) {
            return;
        }
        if (!BlockUtil.canReplace(pos)) {
            return;
        }
        if (this.search.getValue() && !BlockUtil.canBlockFacing(pos)) {
            BlockPos bestPos = null;
            double distance = 1000.0;
            boolean onlyDown = !this.allowUp.getValue();
            for (BlockPos pos2 : BlockUtil.getBox(this.range.getValue(), pos)) {
                if (!BlockUtil.canPlace(pos2)) continue;
                if ((bestPos == null || pos.getDistance(pos2.getX(), pos2.getY(), pos2.getZ()) < distance) && !onlyDown) {
                    bestPos = pos2;
                    distance = pos.getDistance(pos2.getX(), pos2.getY(), pos2.getZ());
                }
                if (bestPos != null && !(pos.getDistance(pos2.getX(), pos2.getY(), pos2.getZ()) < distance) || pos.getY() < pos2.getY()) continue;
                bestPos = pos2;
                distance = pos.getDistance(pos2.getX(), pos2.getY(), pos2.getZ());
                onlyDown = true;
            }
            if (bestPos == null) {
                return;
            }
            pos = bestPos;
        }
        if (!BlockUtil.canPlace(pos)) {
            return;
        }
        int oldSlot = Scaffold.mc.player.inventory.currentItem;
        int newSlot = -1;
        for (int i = 0; i < 9; ++i) {
            ItemStack stack = Scaffold.mc.player.inventory.getStackInSlot(i);
            if (InventoryUtil.isNull(stack) || !(stack.getItem() instanceof ItemBlock) || !Block.getBlockFromItem(stack.getItem()).getDefaultState().isFullBlock()) continue;
            newSlot = i;
            break;
        }
        if (newSlot == -1) {
            return;
        }
        EnumFacing side = BlockUtil.getFirstFacing(pos);
        if (side == null) {
            return;
        }
        boolean switched = false;
        if (!(Scaffold.mc.player.getHeldItemMainhand().getItem() instanceof ItemBlock)) {
            if (this.autoSwap.getValue() == SwapMode.OFF) {
                return;
            }
            if (this.autoSwap.getValue() == SwapMode.SILENT) {
                switched = true;
            }
            InventoryUtil.doSwap(newSlot);
        }
        if (MovementUtil.isJumping() && !MovementUtil.isMoving() && this.tower.getValue()) {
            Scaffold.mc.player.motionX = 0.0;
            Scaffold.mc.player.motionZ = 0.0;
            Scaffold.mc.player.jump();
            if (this.timer.passedMs(1500L)) {
                Scaffold.mc.player.motionY = -0.28;
                this.timer.reset();
            }
        }
        this.rotateTimer.reset();
        BlockPos neighbour = pos.offset(side);
        EnumFacing opposite = side.getOpposite();
        this.faceVector(new Vec3d(neighbour).add(0.5, 0.5, 0.5).add(new Vec3d(opposite.getDirectionVec()).scale(0.5)));
        this.placeTimer.reset();
        BlockUtil.placeBlock(pos, EnumHand.MAIN_HAND, this.rotate.getValue(), this.packet.getValue());
        if (switched) {
            InventoryUtil.doSwap(oldSlot);
        }
    }

    public static enum SwapMode {
        OFF,
        NORMAL,
        SILENT

    }
}

