package me.rebirthclient.mod.modules.impl.combat;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import me.rebirthclient.api.managers.Managers;
import me.rebirthclient.api.util.BlockUtil;
import me.rebirthclient.api.util.CombatUtil;
import me.rebirthclient.api.util.EntityUtil;
import me.rebirthclient.api.util.InventoryUtil;
import me.rebirthclient.api.util.Timer;
import me.rebirthclient.mod.modules.Category;
import me.rebirthclient.mod.modules.Module;
import me.rebirthclient.mod.modules.settings.Setting;
import net.minecraft.block.Block;
import net.minecraft.block.BlockDirectional;
import net.minecraft.block.BlockPistonBase;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityEnderCrystal;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;

public class TestPush
extends Module {
    public static final List<Block> canPushBlock = Arrays.asList(Blocks.AIR, Blocks.ENDER_CHEST, Blocks.STANDING_SIGN, Blocks.WALL_SIGN, Blocks.REDSTONE_WIRE, Blocks.TRIPWIRE);
    public static final List<Block> canPushBlock2 = Arrays.asList(Blocks.AIR, Blocks.STANDING_SIGN, Blocks.WALL_SIGN, Blocks.REDSTONE_WIRE, Blocks.TRIPWIRE);
    public final Setting<Integer> surroundCheck = this.add(new Setting<>("SurroundCheck", 2, 0, 4));
    private final Setting<Integer> updateDelay = this.add(new Setting<>("PlaceDelay", 100, 0, 500));
    private final Setting<Double> range = this.add(new Setting<>("TargetRange", 6.0, 0.0, 8.0));
    private final Setting<Double> placeRange = this.add(new Setting<>("PlaceRange", 6.0, 0.0, 8.0));
    private final Setting<Double> pistonCheck = this.add(new Setting<>("AntiDisturb", 2.6, 0.0, 8.0));
    private final Setting<Boolean> autoDisable = this.add(new Setting<>("AutoDisable", true));
    private final Setting<Boolean> noEating = this.add(new Setting<>("NoEating", true));
    private final Setting<Boolean> minePower = this.add(new Setting<>("MinePower", true));
    private final Setting<Boolean> pullBack = this.add(new Setting<>("PullBack", true).setParent());
    private final Setting<Boolean> onlyBurrow = this.add(new Setting<>("OnlyBurrow", true, v -> this.pullBack.isOpen()));
    private final Setting<Boolean> targetGround = this.add(new Setting<>("TargetGround", false));
    private final Setting<Boolean> pistonPacket = this.add(new Setting<>("PistonPacket", false));
    private final Setting<Boolean> powerPacket = this.add(new Setting<>("PowerPacket", true));
    private final Setting<Boolean> checkCrystal = this.add(new Setting<>("CheckCrystal", true));
    private final Setting<Boolean> breakCrystal = this.add(new Setting<>("BreakCrystal", true));
    private final Setting<Boolean> selfGround = this.add(new Setting<>("SelfGround", true));
    private final Setting<Double> maxSelfSpeed = this.add(new Setting<>("MaxSelfSpeed", 6.0, 1.0, 30.0));
    private final Setting<Double> maxTargetSpeed = this.add(new Setting<>("MaxTargetSpeed", 4.0, 1.0, 15.0));
    private final Setting<Integer> multiPlace = this.add(new Setting<>("MultiPlace", 2, 1, 8));
    private final Timer timer = new Timer();
    int progress = 0;
    private EntityPlayer displayTarget;

    public TestPush() {
        super("TestPush", "2b2t.xin", Category.COMBAT);
    }

    @Override
    public void onUpdate() {
        this.progress = 0;
        this.displayTarget = null;
        if (InventoryUtil.findHotbarBlock(Blocks.REDSTONE_BLOCK) == -1 || InventoryUtil.findHotbarClass(BlockPistonBase.class) == -1) {
            if (this.autoDisable.getValue()) {
                this.disable();
            }
            return;
        }
        if (!this.timer.passedMs(this.updateDelay.getValue())) {
            return;
        }
        if (this.noEating.getValue() && EntityUtil.isEating()) {
            return;
        }
        if (Managers.SPEED.getPlayerSpeed(TestPush.mc.player) > this.maxSelfSpeed.getValue()) {
            return;
        }
        if (this.selfGround.getValue() && !TestPush.mc.player.onGround) {
            return;
        }
        for (EntityPlayer player : TestPush.mc.world.playerEntities) {
            if (!this.canPush(player) || !EntityUtil.isValid(player, this.range.getValue()) || !player.onGround && this.targetGround.getValue() || Managers.SPEED.getPlayerSpeed(player) > this.maxTargetSpeed.getValue()) continue;
            if (this.progress >= this.multiPlace.getValue()) {
                return;
            }
            this.displayTarget = player;
            this.doPush(player);
        }
        if ((this.displayTarget == null || this.progress == 0) && this.autoDisable.getValue()) {
            this.disable();
        }
    }

    private void doPush(EntityPlayer target) {
        BlockPos targetPos = new BlockPos(target.posX, target.posY + 0.5, target.posZ);
        if (this.getBlock(targetPos.add(0, 2, 0)) == Blocks.AIR) {
            BlockPos pos;
            for (EnumFacing i : EnumFacing.VALUES) {
                if (i == EnumFacing.DOWN || i == EnumFacing.UP || !(this.getBlock(pos = targetPos.offset(i).up()) instanceof BlockPistonBase) || !canPushBlock.contains(this.getBlock(pos.offset(i, -2))) || !canPushBlock.contains(this.getBlock(pos.offset(i, -2).up())) || ((EnumFacing)this.getBlockState(pos).getValue((IProperty)BlockDirectional.FACING)).getOpposite() != i) continue;
                if (this.breakCrystal.getValue()) {
                    this.attackCrystal(pos);
                }
                if (!this.placePower(pos, true)) continue;
                return;
            }
            for (EnumFacing i : EnumFacing.VALUES) {
                if (i == EnumFacing.DOWN || i == EnumFacing.UP) continue;
                pos = targetPos.offset(i).up();
                if ((TestPush.mc.player.posY - (double)targetPos.getY() <= -1.0 || TestPush.mc.player.posY - (double)targetPos.getY() >= 2.0) && BlockUtil.distanceToXZ((double)pos.getX() + 0.5, (double)pos.getZ() + 0.5) < this.pistonCheck.getValue() || !BlockUtil.canPlace2(pos, this.placeRange.getValue()) || !canPushBlock.contains(this.getBlock(pos.offset(i, -2))) || !canPushBlock.contains(this.getBlock(pos.offset(i, -2).up()))) continue;
                if (this.breakCrystal.getValue()) {
                    this.attackCrystal(pos);
                }
                if (!this.downPower(pos)) continue;
                if (this.placePiston(i, pos)) {
                    return;
                }
                return;
            }
            if (canPushBlock.contains(this.getBlock(targetPos)) && this.onlyBurrow.getValue() || !this.pullBack.getValue()) {
                return;
            }
            for (EnumFacing i : EnumFacing.VALUES) {
                if (i == EnumFacing.DOWN || i == EnumFacing.UP || !(this.getBlock(pos = targetPos.offset(i).up()) instanceof BlockPistonBase) || !canPushBlock.contains(this.getBlock(pos.up())) && this.getBlock(pos.up()) != Blocks.REDSTONE_BLOCK || ((EnumFacing)this.getBlockState(pos).getValue((IProperty)BlockDirectional.FACING)).getOpposite() != i) continue;
                for (EnumFacing i2 : EnumFacing.VALUES) {
                    if (this.getBlock(pos.offset(i2)) != Blocks.REDSTONE_BLOCK) continue;
                    CombatUtil.mineBlock(pos.offset(i2));
                    if (this.autoDisable.getValue()) {
                        this.disable();
                    }
                    return;
                }
            }
            for (EnumFacing i : EnumFacing.VALUES) {
                if (i == EnumFacing.DOWN || i == EnumFacing.UP || !(this.getBlock(pos = targetPos.offset(i).up()) instanceof BlockPistonBase) || !canPushBlock.contains(this.getBlock(pos.up())) && this.getBlock(pos.up()) != Blocks.REDSTONE_BLOCK || ((EnumFacing)this.getBlockState(pos).getValue((IProperty)BlockDirectional.FACING)).getOpposite() != i) continue;
                if (this.breakCrystal.getValue()) {
                    this.attackCrystal(pos);
                }
                if (!this.placePower(pos, false)) continue;
                return;
            }
            for (EnumFacing i : EnumFacing.VALUES) {
                if (i == EnumFacing.DOWN || i == EnumFacing.UP) continue;
                pos = targetPos.offset(i).up();
                if ((TestPush.mc.player.posY - (double)targetPos.getY() <= -1.0 || TestPush.mc.player.posY - (double)targetPos.getY() >= 2.0) && BlockUtil.distanceToXZ((double)pos.getX() + 0.5, (double)pos.getZ() + 0.5) < this.pistonCheck.getValue() || !BlockUtil.canPlace2(pos, this.placeRange.getValue()) || !canPushBlock.contains(this.getBlock(pos.up())) && this.getBlock(pos.up()) != Blocks.REDSTONE_BLOCK) continue;
                if (this.breakCrystal.getValue()) {
                    this.attackCrystal(pos);
                }
                if (!this.downPower(pos) || !this.placePiston(i, pos)) continue;
                return;
            }
        } else {
            BlockPos pos;
            for (EnumFacing i : EnumFacing.VALUES) {
                if (i == EnumFacing.DOWN || i == EnumFacing.UP || !(this.getBlock(pos = targetPos.offset(i).up()) instanceof BlockPistonBase) || !canPushBlock2.contains(this.getBlock(pos.offset(i, -2))) || !canPushBlock2.contains(this.getBlock(pos.offset(i, -2).down())) || ((EnumFacing)this.getBlockState(pos).getValue((IProperty)BlockDirectional.FACING)).getOpposite() != i) continue;
                if (this.breakCrystal.getValue()) {
                    this.attackCrystal(pos);
                }
                if (!this.placePower(pos, true)) continue;
                return;
            }
            for (EnumFacing i : EnumFacing.VALUES) {
                if (i == EnumFacing.DOWN || i == EnumFacing.UP) continue;
                pos = targetPos.offset(i).up();
                if ((TestPush.mc.player.posY - (double)targetPos.getY() <= -1.0 || TestPush.mc.player.posY - (double)targetPos.getY() >= 2.0) && BlockUtil.distanceToXZ((double)pos.getX() + 0.5, (double)pos.getZ() + 0.5) < this.pistonCheck.getValue() || !BlockUtil.canPlace2(pos, this.placeRange.getValue()) || !canPushBlock2.contains(this.getBlock(pos.offset(i, -2))) || !canPushBlock2.contains(this.getBlock(pos.offset(i, -2).down()))) continue;
                if (this.breakCrystal.getValue()) {
                    this.attackCrystal(pos);
                }
                if (!this.downPower(pos) || !this.placePiston(i, pos)) continue;
                return;
            }
        }
    }

    private boolean placePiston(EnumFacing i, BlockPos pos) {
        if (!BlockUtil.canPlace(pos, this.placeRange.getValue())) {
            return false;
        }
        if (this.progress >= this.multiPlace.getValue()) {
            return false;
        }
        if (InventoryUtil.findHotbarBlock(BlockPistonBase.class) == -1) {
            return false;
        }
        AutoPush.pistonFacing(i);
        int old = TestPush.mc.player.inventory.currentItem;
        InventoryUtil.doSwap(InventoryUtil.findHotbarBlock(BlockPistonBase.class));
        BlockUtil.placeBlock(pos, EnumHand.MAIN_HAND, false, this.pistonPacket.getValue());
        InventoryUtil.doSwap(old);
        ++this.progress;
        this.timer.reset();
        EntityUtil.facePlacePos(pos);
        this.placePower(pos, true);
        return true;
    }

    private boolean placePower(BlockPos pos, boolean disable) {
        if (this.progress >= this.multiPlace.getValue()) {
            return false;
        }
        for (EnumFacing i2 : EnumFacing.VALUES) {
            if (this.getBlock(pos.offset(i2)) != Blocks.REDSTONE_BLOCK) continue;
            if (this.minePower.getValue()) {
                CombatUtil.mineBlock(pos.offset(i2));
            }
            if (this.autoDisable.getValue() && disable) {
                this.disable();
            }
            return true;
        }
        if (InventoryUtil.findHotbarBlock(Blocks.REDSTONE_BLOCK) == -1) {
            return true;
        }
        EnumFacing facing = BlockUtil.getBestNeighboring(pos, null);
        if (facing != null && BlockUtil.canPlace(pos.offset(facing), this.placeRange.getValue())) {
            int old = TestPush.mc.player.inventory.currentItem;
            InventoryUtil.doSwap(InventoryUtil.findHotbarBlock(Blocks.REDSTONE_BLOCK));
            BlockUtil.placeBlock(pos.offset(facing), EnumHand.MAIN_HAND, true, this.powerPacket.getValue());
            InventoryUtil.doSwap(old);
            ++this.progress;
            this.timer.reset();
            return true;
        }
        for (EnumFacing i2 : EnumFacing.VALUES) {
            if (!BlockUtil.canPlace(pos.offset(i2), this.placeRange.getValue())) continue;
            int old = TestPush.mc.player.inventory.currentItem;
            InventoryUtil.doSwap(InventoryUtil.findHotbarBlock(Blocks.REDSTONE_BLOCK));
            BlockUtil.placeBlock(pos.offset(i2), EnumHand.MAIN_HAND, true, this.powerPacket.getValue());
            InventoryUtil.doSwap(old);
            ++this.progress;
            this.timer.reset();
            return true;
        }
        return false;
    }

    private boolean downPower(BlockPos pos) {
        if (!BlockUtil.canPlaceEnum(pos)) {
            if (this.progress >= this.multiPlace.getValue()) {
                return false;
            }
            if (!BlockUtil.canPlace(pos.add(0, -1, 0), this.placeRange.getValue())) {
                return false;
            }
            if (InventoryUtil.findHotbarBlock(Blocks.REDSTONE_BLOCK) == -1) {
                return false;
            }
            int old = TestPush.mc.player.inventory.currentItem;
            InventoryUtil.doSwap(InventoryUtil.findHotbarBlock(Blocks.REDSTONE_BLOCK));
            BlockUtil.placeBlock(pos.add(0, -1, 0), EnumHand.MAIN_HAND, true, this.powerPacket.getValue());
            InventoryUtil.doSwap(old);
            ++this.progress;
            this.timer.reset();
        }
        return true;
    }

    public void attackCrystal(BlockPos pos) {
        for (Entity crystal : TestPush.mc.world.loadedEntityList.stream().filter(e -> e instanceof EntityEnderCrystal && !e.isDead).sorted(Comparator.comparing(e -> TestPush.mc.player.getDistance(e))).collect(Collectors.toList())) {
            if (!(crystal instanceof EntityEnderCrystal) || !(crystal.getDistance(pos.getX(), pos.getY(), pos.getZ()) < (double)(this.checkCrystal.getValue() ? 4 : 2))) continue;
            CombatUtil.attackCrystal(crystal, true, true);
            return;
        }
    }

    private IBlockState getBlockState(BlockPos pos) {
        return TestPush.mc.world.getBlockState(pos);
    }

    private Block getBlock(BlockPos pos) {
        return TestPush.mc.world.getBlockState(pos).getBlock();
    }

    @Override
    public String getInfo() {
        if (this.displayTarget != null) {
            return this.displayTarget.getName();
        }
        return null;
    }

    private Boolean canPush(EntityPlayer player) {
        int surroundProgress = 0;
        if (!TestPush.mc.world.isAirBlock(new BlockPos(player.posX, player.posY + 0.5, player.posZ))) {
            return true;
        }
        if (!TestPush.mc.world.isAirBlock(new BlockPos(player.posX + 1.0, player.posY + 0.5, player.posZ))) {
            ++surroundProgress;
        }
        if (!TestPush.mc.world.isAirBlock(new BlockPos(player.posX - 1.0, player.posY + 0.5, player.posZ))) {
            ++surroundProgress;
        }
        if (!TestPush.mc.world.isAirBlock(new BlockPos(player.posX, player.posY + 0.5, player.posZ + 1.0))) {
            ++surroundProgress;
        }
        if (!TestPush.mc.world.isAirBlock(new BlockPos(player.posX, player.posY + 0.5, player.posZ - 1.0))) {
            ++surroundProgress;
        }
        return surroundProgress > this.surroundCheck.getValue() - 1;
    }
}

