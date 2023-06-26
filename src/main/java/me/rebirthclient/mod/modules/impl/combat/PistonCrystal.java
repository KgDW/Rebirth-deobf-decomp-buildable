package me.rebirthclient.mod.modules.impl.combat;

import me.rebirthclient.api.util.BlockUtil;
import me.rebirthclient.api.util.CombatUtil;
import me.rebirthclient.api.util.DamageUtil;
import me.rebirthclient.api.util.EntityUtil;
import me.rebirthclient.api.util.InventoryUtil;
import me.rebirthclient.api.util.Timer;
import me.rebirthclient.mod.modules.Category;
import me.rebirthclient.mod.modules.Module;
import me.rebirthclient.mod.modules.impl.combat.AutoPush;
import me.rebirthclient.mod.modules.impl.combat.AutoTrap;
import me.rebirthclient.mod.modules.impl.combat.CombatSetting;
import me.rebirthclient.mod.modules.impl.combat.PullCrystal;
import me.rebirthclient.mod.modules.impl.render.PlaceRender;
import me.rebirthclient.mod.modules.settings.Setting;
import net.minecraft.block.Block;
import net.minecraft.block.BlockDirectional;
import net.minecraft.block.BlockPistonBase;
import net.minecraft.block.properties.IProperty;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityEnderCrystal;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;

public class PistonCrystal
extends Module {
    private final Setting<Boolean> autoDisable = this.add(new Setting<>("AutoDisable", true));
    private final Setting<Boolean> noEating = this.add(new Setting<>("NoEating", true));
    private final Setting<Float> placeRange = this.add(new Setting<>("PlaceRange", 5.0f, 1.0f, 8.0f));
    private final Setting<Float> range = this.add(new Setting<>("Range", 4.0f, 1.0f, 8.0f));
    private final Setting<Boolean> packet = this.add(new Setting<>("Packet", true));
    private final Setting<Boolean> fire = this.add(new Setting<>("Fire", true));
    private final Setting<Boolean> onlyGround = this.add(new Setting<>("pauseAir", true));
    private final Setting<Boolean> onlyStatic = this.add(new Setting<>("pauseMoving", true));
    private final Setting<Integer> updateDelay = this.add(new Setting<>("UpdateDelay", 100, 0, 500));
    private EntityPlayer target = null;
    private final Timer timer = new Timer();

    public PistonCrystal() {
        super("PistonCrystal", "in strict", Category.COMBAT);
    }

    @Override
    public void onTick() {
        if (this.autoDisable.getValue() && AutoTrap.INSTANCE.isOff()) {
            this.disable();
            return;
        }
        if (this.noEating.getValue() && EntityUtil.isEating()) {
            return;
        }
        if (!this.timer.passedMs(this.updateDelay.getValue())) {
            return;
        }
        if (PullCrystal.check(this.onlyStatic.getValue(), !PistonCrystal.mc.player.onGround, this.onlyGround.getValue())) {
            return;
        }
        this.target = this.getTarget(this.range.getValue());
        if (this.target == null) {
            return;
        }
        this.timer.reset();
        BlockPos pos = EntityUtil.getEntityPos(this.target);
        if (this.checkCrystal(pos.up(0))) {
            CombatUtil.attackCrystal(pos.up(0), true, true);
        }
        if (this.checkCrystal(pos.up(1))) {
            CombatUtil.attackCrystal(pos.up(1), true, true);
        }
        if (this.checkCrystal(pos.up(2))) {
            CombatUtil.attackCrystal(pos.up(2), true, true);
        }
        if (this.doPistonActive(pos.up(2))) {
            return;
        }
        if (this.doPistonActive(pos.up())) {
            return;
        }
        if (this.doPlaceCrystal(pos.up(2))) {
            return;
        }
        if (this.doPlaceCrystal(pos.up())) {
            return;
        }
        if (this.doPlacePiston(pos.up(2))) {
            return;
        }
        this.doPlacePiston(pos.up());
    }

    private EntityPlayer getTarget(double range) {
        EntityPlayer target = null;
        double distance = range;
        for (EntityPlayer player : PistonCrystal.mc.world.playerEntities) {
            if (EntityUtil.invalid(player, range) || this.getBlock(player.getPosition()) == Blocks.OBSIDIAN) continue;
            if (target == null) {
                target = player;
                distance = PistonCrystal.mc.player.getDistanceSq(player);
                continue;
            }
            if (PistonCrystal.mc.player.getDistanceSq(player) >= distance) continue;
            target = player;
            distance = PistonCrystal.mc.player.getDistanceSq(player);
        }
        return target;
    }

    private boolean checkCrystal(BlockPos pos) {
        for (Entity entity : PistonCrystal.mc.world.getEntitiesWithinAABB(Entity.class, new AxisAlignedBB(pos))) {
            float damage;
            if (!(entity instanceof EntityEnderCrystal) || !((damage = DamageUtil.calculateDamage(entity, this.target)) > 6.0f)) continue;
            return true;
        }
        return false;
    }

    @Override
    public String getInfo() {
        if (this.target != null) {
            return this.target.getName();
        }
        return null;
    }

    private boolean doPlacePiston(BlockPos pos) {
        for (EnumFacing i : EnumFacing.VALUES) {
            if (i == EnumFacing.DOWN || i == EnumFacing.UP || !this.placePiston(pos, i)) continue;
            return true;
        }
        return false;
    }

    private boolean doPlaceCrystal(BlockPos pos) {
        for (EnumFacing i : EnumFacing.VALUES) {
            if (i == EnumFacing.DOWN || i == EnumFacing.UP || !this.placeCrystal(pos, i)) continue;
            return true;
        }
        return false;
    }

    private boolean placePiston(BlockPos pos, EnumFacing i) {
        if (!BlockUtil.canPlaceCrystal(pos.offset(i), this.placeRange.getValue())) {
            return false;
        }
        if (this.tryPlacePiston(pos.offset(i, 3), i)) {
            return true;
        }
        if (this.tryPlacePiston(pos.offset(i, 3).up(), i)) {
            return true;
        }
        if (this.tryPlacePiston(pos.offset(i, 2), i)) {
            return true;
        }
        if (this.tryPlacePiston(pos.offset(i, 2).up(), i)) {
            return true;
        }
        double offsetX = pos.offset(i).getX() - pos.getX();
        double offsetZ = pos.offset(i).getZ() - pos.getZ();
        if (this.tryPlacePiston(pos.offset(i, 3).add(offsetZ, 0.0, offsetX), i)) {
            return true;
        }
        if (this.tryPlacePiston(pos.offset(i, 3).add(-offsetZ, 0.0, -offsetX), i)) {
            return true;
        }
        if (this.tryPlacePiston(pos.offset(i, 3).add(offsetZ, 1.0, offsetX), i)) {
            return true;
        }
        if (this.tryPlacePiston(pos.offset(i, 3).add(-offsetZ, 1.0, -offsetX), i)) {
            return true;
        }
        if (this.tryPlacePiston(pos.offset(i, 2).add(offsetZ, 0.0, offsetX), i)) {
            return true;
        }
        if (this.tryPlacePiston(pos.offset(i, 2).add(-offsetZ, 0.0, -offsetX), i)) {
            return true;
        }
        if (this.tryPlacePiston(pos.offset(i, 2).add(offsetZ, 1.0, offsetX), i)) {
            return true;
        }
        return this.tryPlacePiston(pos.offset(i, 2).add(-offsetZ, 1.0, -offsetX), i);
    }

    private boolean tryPlacePiston(BlockPos pos, EnumFacing facing) {
        if (!BlockUtil.canPlace(pos, this.placeRange.getValue()) && !(this.getBlock(pos) instanceof BlockPistonBase)) {
            return false;
        }
        if (InventoryUtil.findHotbarClass(BlockPistonBase.class) == -1) {
            return false;
        }
        if ((PistonCrystal.mc.player.posY - (double)pos.getY() <= -2.0 || PistonCrystal.mc.player.posY - (double)pos.getY() >= 3.0) && BlockUtil.distanceToXZ((double)pos.getX() + 0.5, (double)pos.getZ() + 0.5) < 2.6) {
            return false;
        }
        if (!PistonCrystal.mc.world.isAirBlock(pos.offset(facing, -1)) && this.getBlock(pos.offset(facing, -1)) != Blocks.FIRE && this.getBlock(pos.offset(facing.getOpposite())) != Blocks.PISTON_EXTENSION) {
            return false;
        }
        if (!BlockUtil.canPlace(pos, this.placeRange.getValue())) {
            return this.isPiston(pos, facing);
        }
        EnumFacing side = BlockUtil.getFirstFacing(pos);
        if (side == null) {
            return false;
        }
        int old = PistonCrystal.mc.player.inventory.currentItem;
        AutoPush.pistonFacing(facing);
        InventoryUtil.doSwap(InventoryUtil.findHotbarClass(BlockPistonBase.class));
        BlockUtil.placeBlock(pos, EnumHand.MAIN_HAND, false, this.packet.getValue());
        InventoryUtil.doSwap(old);
        BlockPos neighbour = pos.offset(side);
        EnumFacing opposite = side.getOpposite();
        Vec3d hitVec = new Vec3d(neighbour).add(0.5, 0.5, 0.5).add(new Vec3d(opposite.getDirectionVec()).scale(0.5));
        EntityUtil.faceVector(hitVec);
        return true;
    }

    private boolean placeCrystal(BlockPos pos, EnumFacing facing) {
        if (!BlockUtil.canPlaceCrystal(pos.offset(facing), this.placeRange.getValue())) {
            return false;
        }
        if (!this.hasPiston(pos, facing)) {
            return false;
        }
        if (InventoryUtil.findItemInHotbar(Items.END_CRYSTAL) == -1) {
            return false;
        }
        int old = PistonCrystal.mc.player.inventory.currentItem;
        InventoryUtil.doSwap(InventoryUtil.findItemInHotbar(Items.END_CRYSTAL));
        BlockUtil.placeCrystal(pos.offset(facing), true);
        InventoryUtil.doSwap(old);
        return true;
    }

    private boolean hasPiston(BlockPos pos, EnumFacing i) {
        if (this.isPiston(pos.offset(i, 3), i)) {
            return true;
        }
        if (this.isPiston(pos.offset(i, 3).up(), i)) {
            return true;
        }
        if (this.isPiston(pos.offset(i, 2), i)) {
            return true;
        }
        if (this.isPiston(pos.offset(i, 2).up(), i)) {
            return true;
        }
        double offsetX = pos.offset(i).getX() - pos.getX();
        double offsetZ = pos.offset(i).getZ() - pos.getZ();
        if (this.isPiston(pos.offset(i, 3).add(offsetZ, 0.0, offsetX), i)) {
            return true;
        }
        if (this.isPiston(pos.offset(i, 3).add(-offsetZ, 0.0, -offsetX), i)) {
            return true;
        }
        if (this.isPiston(pos.offset(i, 3).add(offsetZ, 1.0, offsetX), i)) {
            return true;
        }
        if (this.isPiston(pos.offset(i, 3).add(-offsetZ, 1.0, -offsetX), i)) {
            return true;
        }
        if (this.isPiston(pos.offset(i, 2).add(offsetZ, 0.0, offsetX), i)) {
            return true;
        }
        if (this.isPiston(pos.offset(i, 2).add(-offsetZ, 0.0, -offsetX), i)) {
            return true;
        }
        if (this.isPiston(pos.offset(i, 2).add(offsetZ, 1.0, offsetX), i)) {
            return true;
        }
        return this.isPiston(pos.offset(i, 2).add(-offsetZ, 1.0, -offsetX), i);
    }

    private boolean isPiston(BlockPos pos, EnumFacing facing) {
        if (!(PistonCrystal.mc.world.getBlockState(pos).getBlock() instanceof BlockPistonBase)) {
            return false;
        }
        if (((EnumFacing)PistonCrystal.mc.world.getBlockState(pos).getValue((IProperty)BlockDirectional.FACING)).getOpposite() != facing) {
            return false;
        }
        return PistonCrystal.mc.world.isAirBlock(pos.offset(facing, -1)) || this.getBlock(pos.offset(facing, -1)) == Blocks.FIRE || this.getBlock(pos.offset(facing.getOpposite())) == Blocks.PISTON_EXTENSION;
    }

    private boolean doPistonActive(BlockPos pos) {
        for (EnumFacing i : EnumFacing.VALUES) {
            if (i == EnumFacing.DOWN || i == EnumFacing.UP || !BlockUtil.posHasCrystal(pos.offset(i))) continue;
            this.doFire(pos, i);
            if (this.doRedStone(pos.offset(i, 3), i)) {
                return true;
            }
            if (this.doRedStone(pos.offset(i, 3).up(), i)) {
                return true;
            }
            if (this.doRedStone(pos.offset(i, 2), i)) {
                return true;
            }
            if (this.doRedStone(pos.offset(i, 2).up(), i)) {
                return true;
            }
            double offsetX = pos.offset(i).getX() - pos.getX();
            double offsetZ = pos.offset(i).getZ() - pos.getZ();
            if (this.doRedStone(pos.offset(i, 3).add(offsetZ, 0.0, offsetX), i)) {
                return true;
            }
            if (this.doRedStone(pos.offset(i, 3).add(-offsetZ, 0.0, -offsetX), i)) {
                return true;
            }
            if (this.doRedStone(pos.offset(i, 3).add(offsetZ, 1.0, offsetX), i)) {
                return true;
            }
            if (this.doRedStone(pos.offset(i, 3).add(-offsetZ, 1.0, -offsetX), i)) {
                return true;
            }
            if (this.doRedStone(pos.offset(i, 2).add(offsetZ, 0.0, offsetX), i)) {
                return true;
            }
            if (this.doRedStone(pos.offset(i, 2).add(-offsetZ, 0.0, -offsetX), i)) {
                return true;
            }
            if (this.doRedStone(pos.offset(i, 2).add(offsetZ, 1.0, offsetX), i)) {
                return true;
            }
            if (!this.doRedStone(pos.offset(i, 2).add(-offsetZ, 1.0, -offsetX), i)) continue;
            return true;
        }
        return false;
    }

    private void doFire(BlockPos pos, EnumFacing facing) {
        if (!this.fire.getValue()) {
            return;
        }
        if (InventoryUtil.findItemInHotbar(Items.FLINT_AND_STEEL) == -1) {
            return;
        }
        int old = PistonCrystal.mc.player.inventory.currentItem;
        for (EnumFacing i : EnumFacing.VALUES) {
            if (i == EnumFacing.DOWN || i == EnumFacing.UP || pos.offset(i).equals(pos.offset(facing)) || PistonCrystal.mc.world.getBlockState(pos.offset(i)).getBlock() != Blocks.FIRE) continue;
            return;
        }
        for (EnumFacing i : EnumFacing.VALUES) {
            if (i == EnumFacing.DOWN || i == EnumFacing.UP || pos.offset(i).equals(pos.offset(facing)) || pos.offset(i).equals(pos.offset(facing, -1)) || !PistonCrystal.canFire(pos.offset(i))) continue;
            InventoryUtil.doSwap(InventoryUtil.findItemInHotbar(Items.FLINT_AND_STEEL));
            PistonCrystal.placeFire(pos.offset(i), EnumHand.MAIN_HAND, true, this.packet.getValue());
            InventoryUtil.doSwap(old);
            return;
        }
        if (PistonCrystal.canFire(pos.offset(facing, -1))) {
            InventoryUtil.doSwap(InventoryUtil.findItemInHotbar(Items.FLINT_AND_STEEL));
            PistonCrystal.placeFire(pos.offset(facing, -1), EnumHand.MAIN_HAND, true, this.packet.getValue());
            InventoryUtil.doSwap(old);
            return;
        }
        if (PistonCrystal.canFire(pos.offset(facing, 1))) {
            InventoryUtil.doSwap(InventoryUtil.findItemInHotbar(Items.FLINT_AND_STEEL));
            PistonCrystal.placeFire(pos.offset(facing, 1), EnumHand.MAIN_HAND, true, this.packet.getValue());
            InventoryUtil.doSwap(old);
        }
    }

    public static void placeFire(BlockPos pos, EnumHand hand, boolean rotate, boolean packet) {
        EnumFacing side = EnumFacing.DOWN;
        BlockPos neighbour = pos.offset(side);
        EnumFacing opposite = side.getOpposite();
        Vec3d hitVec = new Vec3d(neighbour).add(0.5, 0.5, 0.5).add(new Vec3d(opposite.getDirectionVec()).scale(0.5));
        if (rotate) {
            EntityUtil.faceVector(hitVec);
        }
        PlaceRender.PlaceMap.put(pos, new PlaceRender.placePosition(pos));
        BlockUtil.rightClickBlock(neighbour, hitVec, hand, opposite, packet);
    }

    private static boolean canFire(BlockPos pos) {
        if (BlockUtil.canReplace(pos.down())) {
            return false;
        }
        if (!PistonCrystal.mc.world.isAirBlock(pos)) {
            return false;
        }
        for (EnumFacing side : BlockUtil.getPlacableFacings(pos, true, CombatSetting.INSTANCE.checkRaytrace.getValue())) {
            if (!BlockUtil.canClick(pos.offset(side)) || side != EnumFacing.DOWN) continue;
            return true;
        }
        return false;
    }

    private boolean doRedStone(BlockPos pos, EnumFacing facing) {
        if (!(this.getBlock(pos) instanceof BlockPistonBase)) {
            return false;
        }
        if (((EnumFacing)PistonCrystal.mc.world.getBlockState(pos).getValue((IProperty)BlockDirectional.FACING)).getOpposite() != facing) {
            return false;
        }
        if (!PistonCrystal.mc.world.isAirBlock(pos.offset(facing, -1)) && this.getBlock(pos.offset(facing, -1)) != Blocks.FIRE && this.getBlock(pos.offset(facing.getOpposite())) != Blocks.PISTON_EXTENSION) {
            return false;
        }
        for (EnumFacing i : EnumFacing.VALUES) {
            if (this.getBlock(pos.offset(i)) != Blocks.REDSTONE_BLOCK) continue;
            return true;
        }
        if (InventoryUtil.findHotbarBlock(Blocks.REDSTONE_BLOCK) == -1) {
            return false;
        }
        int old = PistonCrystal.mc.player.inventory.currentItem;
        EnumFacing bestNeighboring = BlockUtil.getBestNeighboring(pos, facing);
        if (bestNeighboring != null) {
            InventoryUtil.doSwap(InventoryUtil.findHotbarBlock(Blocks.REDSTONE_BLOCK));
            BlockUtil.placeBlock(pos.offset(bestNeighboring), EnumHand.MAIN_HAND, true, this.packet.getValue());
            InventoryUtil.doSwap(old);
            return true;
        }
        for (EnumFacing i : EnumFacing.VALUES) {
            if (!BlockUtil.canPlace(pos.offset(i), this.placeRange.getValue())) continue;
            InventoryUtil.doSwap(InventoryUtil.findHotbarBlock(Blocks.REDSTONE_BLOCK));
            BlockUtil.placeBlock(pos.offset(i), EnumHand.MAIN_HAND, true, this.packet.getValue());
            InventoryUtil.doSwap(old);
            return true;
        }
        return false;
    }

    private Block getBlock(BlockPos pos) {
        return PistonCrystal.mc.world.getBlockState(pos).getBlock();
    }
}

