package me.rebirthclient.mod.modules.impl.combat;

import me.rebirthclient.api.managers.Managers;
import me.rebirthclient.api.managers.impl.BreakManager;
import me.rebirthclient.api.util.BlockUtil;
import me.rebirthclient.api.util.CombatUtil;
import me.rebirthclient.api.util.EntityUtil;
import me.rebirthclient.api.util.InventoryUtil;
import me.rebirthclient.api.util.Timer;
import me.rebirthclient.mod.modules.Category;
import me.rebirthclient.mod.modules.Module;
import me.rebirthclient.mod.modules.impl.combat.AutoPush;
import me.rebirthclient.mod.modules.settings.Setting;
import net.minecraft.block.BlockObsidian;
import net.minecraft.block.BlockPistonBase;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;

public class AutoTrap
extends Module {
    final Timer timer = new Timer();
    private final Setting<Boolean> rotate = this.add(new Setting<>("Rotate", true));
    private final Setting<Boolean> packet = this.add(new Setting<>("Packet", true));
    private final Setting<Integer> multiPlace = this.add(new Setting<>("MultiPlace", 1, 1, 8));
    private final Setting<Boolean> autoDisable = this.add(new Setting<>("AutoDisable", true));
    private final Setting<Float> range = this.add(new Setting<>("Range", 5.0f, 1.0f, 8.0f));
    private final Setting<TargetMode> targetMod = this.add(new Setting<>("TargetMode", TargetMode.Single));
    private final Setting<Mode> mode = this.add(new Setting<>("Mode", Mode.Trap));
    private final Setting<Boolean> antiStep = this.add(new Setting<>("AntiStep", false, v -> this.mode.getValue() != Mode.Piston));
    private final Setting<Boolean> extend = this.add(new Setting<>("Extend", true, v -> this.mode.getValue() != Mode.Piston));
    private final Setting<Boolean> head = this.add(new Setting<>("Head", true, v -> this.mode.getValue() != Mode.Piston));
    private final Setting<Boolean> chest = this.add(new Setting<>("Chest", true, v -> this.mode.getValue() != Mode.Piston).setParent());
    private final Setting<Boolean> onlyGround = this.add(new Setting<>("OnlyGround", true, v -> this.mode.getValue() != Mode.Piston && this.chest.isOpen()));
    private final Setting<Boolean> legs = this.add(new Setting<>("Legs", false, v -> this.mode.getValue() != Mode.Piston));
    private final Setting<Boolean> facing = this.add(new Setting<>("Facing", false, v -> this.mode.getValue() != Mode.Trap));
    private final Setting<Integer> delay = this.add(new Setting<>("Delay", 50, 0, 300));
    private final Setting<Float> placeRange = this.add(new Setting<>("PlaceRange", 4.0f, 1.0f, 6.0f));
    private final Setting<Double> maxTargetSpeed = this.add(new Setting<>("MaxTargetSpeed", 4.0, 1.0, 30.0));
    private final Setting<Boolean> selfGround = this.add(new Setting<>("SelfGround", true));
    private final Setting<Double> maxSelfSpeed = this.add(new Setting<>("MaxSelfSpeed", 6.0, 1.0, 30.0));
    public EntityPlayer target;
    public static AutoTrap INSTANCE;
    int progress = 0;

    public AutoTrap() {
        super("AutoTrap", "Automatically trap the enemy", Category.COMBAT);
        INSTANCE = this;
    }

    @Override
    public void onTick() {
        this.progress = 0;
        if (this.selfGround.getValue() && !AutoTrap.mc.player.onGround) {
            this.target = null;
            if (this.autoDisable.getValue()) {
                this.disable();
            }
            return;
        }
        if (Managers.SPEED.getPlayerSpeed(AutoTrap.mc.player) > this.maxSelfSpeed.getValue()) {
            this.target = null;
            if (this.autoDisable.getValue()) {
                this.disable();
            }
            return;
        }
        if (!this.timer.passedMs(this.delay.getValue())) {
            this.target = null;
            return;
        }
        if (this.targetMod.getValue() == TargetMode.Single) {
            this.target = CombatUtil.getTarget(this.range.getValue(), this.maxTargetSpeed.getMaxValue());
            if (this.target == null) {
                return;
            }
            this.trapTarget(this.target);
        } else if (this.targetMod.getValue() == TargetMode.Multi) {
            boolean found = false;
            for (EntityPlayer player : AutoTrap.mc.world.playerEntities) {
                if (Managers.SPEED.getPlayerSpeed(player) > this.maxTargetSpeed.getValue() || EntityUtil.invalid(player, this.range.getValue())) continue;
                found = true;
                this.target = player;
                this.trapTarget(this.target);
            }
            if (!found) {
                if (this.autoDisable.getValue()) {
                    this.disable();
                }
                this.target = null;
            }
        }
    }

    private void trapTarget(EntityPlayer target) {
        if (this.mode.getValue() == Mode.Trap) {
            this.doTrap(EntityUtil.getEntityPos(target));
        } else if (this.mode.getValue() == Mode.Piston) {
            this.doPiston(EntityUtil.getEntityPos(target).up());
        } else {
            this.doAuto(EntityUtil.getEntityPos(target));
        }
    }

    private void doAuto(BlockPos pos) {
        if (InventoryUtil.findHotbarClass(BlockPistonBase.class) == -1) {
            this.doTrap(pos);
            return;
        }
        for (EnumFacing facing : EnumFacing.VALUES) {
            if (facing == EnumFacing.UP || facing == EnumFacing.DOWN || !(AutoTrap.mc.world.getBlockState(pos.up().offset(facing)).getBlock() instanceof BlockPistonBase)) continue;
            return;
        }
        for (EnumFacing facing : EnumFacing.VALUES) {
            if (facing == EnumFacing.UP || facing == EnumFacing.DOWN || !BlockUtil.canPlace(pos.up().offset(facing))) continue;
            this.placePiston(pos.up().offset(facing), facing);
            return;
        }
        this.doTrap(pos);
    }

    private void doPiston(BlockPos pos) {
        if (!(AutoTrap.mc.world.getBlockState(pos.south()).getBlock() instanceof BlockPistonBase || AutoTrap.mc.world.getBlockState(pos.south(-1)).getBlock() instanceof BlockPistonBase || this.placePiston(pos.south(), EnumFacing.SOUTH))) {
            this.placePiston(pos.south(-1), EnumFacing.SOUTH.getOpposite());
        }
        if (!(AutoTrap.mc.world.getBlockState(pos.east()).getBlock() instanceof BlockPistonBase || AutoTrap.mc.world.getBlockState(pos.east(-1)).getBlock() instanceof BlockPistonBase || this.placePiston(pos.east(), EnumFacing.EAST))) {
            this.placePiston(pos.east(-1), EnumFacing.EAST.getOpposite());
        }
    }

    private void doTrap(BlockPos pos) {
        BlockPos offsetPos;
        if (this.antiStep.getValue() && BreakManager.isMine(pos.add(0, 2, 0))) {
            this.placeBlock(pos.add(0, 3, 0));
        }
        if (this.extend.getValue()) {
            BlockPos offsetPos2 = pos.add(0.1, 0.0, 0.1);
            if (this.checkEntity(new BlockPos(offsetPos2)) != null) {
                this.placeBlock(offsetPos2.up(2));
            }
            if (this.checkEntity(new BlockPos(offsetPos2 = pos.add(-0.1, 0.0, 0.1))) != null) {
                this.placeBlock(offsetPos2.up(2));
            }
            if (this.checkEntity(new BlockPos(offsetPos2 = pos.add(0.1, 0.0, -0.1))) != null) {
                this.placeBlock(offsetPos2.up(2));
            }
            if (this.checkEntity(new BlockPos(offsetPos2 = pos.add(-0.1, 0.0, -0.1))) != null) {
                this.placeBlock(offsetPos2.up(2));
            }
        }
        boolean trapChest = false;
        if (this.head.getValue() && AutoTrap.mc.world.getBlockState(pos.add(0, 2, 0)).getBlock() == Blocks.AIR) {
            if (BlockUtil.canPlace4(pos.up(2))) {
                this.placeBlock(pos.up(2));
            }
            if (!BlockUtil.canPlace4(pos.up(2))) {
                trapChest = true;
            }
        }
        if (this.chest.getValue() && (!this.onlyGround.getValue() || this.target.onGround) || trapChest) {
            for (EnumFacing i : EnumFacing.VALUES) {
                if (i == EnumFacing.DOWN || i == EnumFacing.UP) continue;
                offsetPos = pos.offset(i).up();
                this.placeBlock(offsetPos);
                if (!BlockUtil.canPlace4(pos.up(2)) && BlockUtil.canReplace(pos.up(2))) {
                    this.placeBlock(offsetPos.up());
                }
                if (BlockUtil.canBlockFacing(offsetPos) || !BlockUtil.canReplace(offsetPos)) continue;
                this.placeBlock(offsetPos.down());
            }
        }
        if (this.legs.getValue()) {
            for (EnumFacing i : EnumFacing.VALUES) {
                if (i == EnumFacing.DOWN || i == EnumFacing.UP || !BlockUtil.isAir((offsetPos = pos.offset(i)).up())) continue;
                this.placeBlock(offsetPos);
                if (BlockUtil.canBlockFacing(offsetPos)) continue;
                this.placeBlock(offsetPos.down());
            }
        }
    }

    @Override
    public String getInfo() {
        if (this.target != null) {
            return this.target.getName() + ", " + this.mode.getValue().name();
        }
        return this.mode.getValue().name();
    }

    private Entity checkEntity(BlockPos pos) {
        Entity test = null;
        for (Entity entity : AutoTrap.mc.world.getEntitiesWithinAABB(Entity.class, new AxisAlignedBB(pos))) {
            if (!(entity instanceof EntityPlayer) || entity == AutoTrap.mc.player) continue;
            test = entity;
        }
        return test;
    }

    private boolean placePiston(BlockPos pos, EnumFacing facing) {
        if (this.progress >= this.multiPlace.getValue()) {
            return false;
        }
        if (!BlockUtil.canPlace(pos)) {
            return false;
        }
        if (AutoTrap.mc.player.getDistance((double)pos.getX() + 0.5, (double)pos.getY() + 0.5, (double)pos.getZ() + 0.5) > (double) this.placeRange.getValue()) {
            return false;
        }
        int old = AutoTrap.mc.player.inventory.currentItem;
        if (InventoryUtil.findHotbarClass(BlockPistonBase.class) == -1) {
            return false;
        }
        EnumFacing side = BlockUtil.getFirstFacing(pos);
        if (side == null) {
            return false;
        }
        InventoryUtil.doSwap(InventoryUtil.findHotbarClass(BlockPistonBase.class));
        if (this.facing.getValue()) {
            AutoPush.pistonFacing(facing);
            BlockUtil.placeBlock(pos, EnumHand.MAIN_HAND, false, this.packet.getValue());
            BlockPos neighbour = pos.offset(side);
            EnumFacing opposite = side.getOpposite();
            Vec3d hitVec = new Vec3d(neighbour).add(0.5, 0.5, 0.5).add(new Vec3d(opposite.getDirectionVec()).scale(0.5));
            if (this.rotate.getValue()) {
                EntityUtil.faceVector(hitVec);
            }
        } else {
            BlockUtil.placeBlock(pos, EnumHand.MAIN_HAND, this.rotate.getValue(), this.packet.getValue());
        }
        InventoryUtil.doSwap(old);
        this.timer.reset();
        ++this.progress;
        return true;
    }

    private void placeBlock(BlockPos pos) {
        if (this.progress >= this.multiPlace.getValue()) {
            return;
        }
        if (!BlockUtil.canPlace(pos)) {
            return;
        }
        if (AutoTrap.mc.player.getDistance((double)pos.getX() + 0.5, (double)pos.getY() + 0.5, (double)pos.getZ() + 0.5) > (double) this.placeRange.getValue()) {
            return;
        }
        int old = AutoTrap.mc.player.inventory.currentItem;
        if (InventoryUtil.findHotbarClass(BlockObsidian.class) == -1) {
            return;
        }
        InventoryUtil.doSwap(InventoryUtil.findHotbarClass(BlockObsidian.class));
        BlockUtil.placeBlock(pos, EnumHand.MAIN_HAND, this.rotate.getValue(), this.packet.getValue());
        InventoryUtil.doSwap(old);
        this.timer.reset();
        ++this.progress;
    }

    public static enum Mode {
        Trap,
        Piston,
        Auto

    }

    public static enum TargetMode {
        Single,
        Multi

    }
}

