package me.rebirthclient.mod.modules.impl.combat;

import me.rebirthclient.api.util.BlockUtil;
import me.rebirthclient.api.util.CombatUtil;
import me.rebirthclient.api.util.DamageUtil;
import me.rebirthclient.api.util.EntityUtil;
import me.rebirthclient.api.util.InventoryUtil;
import me.rebirthclient.api.util.MovementUtil;
import me.rebirthclient.api.util.Timer;
import me.rebirthclient.mod.modules.Category;
import me.rebirthclient.mod.modules.Module;
import me.rebirthclient.mod.modules.impl.combat.AutoPush;
import me.rebirthclient.mod.modules.impl.combat.AutoTrap;
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
import net.minecraft.init.Items;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;

public class PullCrystal
extends Module {
    private final Setting<Float> range = this.add(new Setting<>("Range", 5.0f, 1.0f, 8.0f));
    private final Setting<Boolean> pistonPacket = this.add(new Setting<>("PistonPacket", false));
    private final Setting<Boolean> packet = this.add(new Setting<>("Packet", true));
    private final Setting<Boolean> fire = this.add(new Setting<>("Fire", true));
    public final Setting<Boolean> pauseWeb = this.add(new Setting<>("PauseWeb", true));
    private final Setting<Boolean> noEating = this.add(new Setting<>("NoEating", true));
    private final Setting<Boolean> multiPlace = this.add(new Setting<>("MultiPlace", false));
    private final Setting<Boolean> onlyGround = this.add(new Setting<>("NoAir", true));
    private final Setting<Boolean> onlyStatic = this.add(new Setting<>("NoMoving", true));
    private final Setting<Boolean> autoDisable = this.add(new Setting<>("AutoDisable", true));
    private final Setting<Integer> updateDelay = this.add(new Setting<>("UpdateDelay", 100, 0, 500));
    private EntityPlayer target = null;
    public static PullCrystal INSTANCE;
    private final Timer timer = new Timer();
    public static BlockPos crystalPos;
    public static BlockPos powerPos;

    public PullCrystal() {
        super("PullCrystal", "use piston pull crystal and boom", Category.COMBAT);
        INSTANCE = this;
    }

    @Override
    public void onUpdate() {
        if (AutoTrap.INSTANCE.isOff() && this.autoDisable.getValue()) {
            this.disable();
            return;
        }
        if (!this.timer.passedMs(this.updateDelay.getValue())) {
            return;
        }
        if (this.noEating.getValue() && EntityUtil.isEating()) {
            return;
        }
        if (PullCrystal.check(this.onlyStatic.getValue(), !PullCrystal.mc.player.onGround, this.onlyGround.getValue())) {
            return;
        }
        this.target = this.getTarget(this.range.getValue());
        if (this.target == null) {
            this.target = CombatUtil.getTarget(this.range.getValue());
            if (this.target != null) {
                this.mineBlock(EntityUtil.getEntityPos(this.target));
            } else if (this.autoDisable.getValue()) {
                this.disable();
            }
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
        if (this.checkCrystal(pos.up(3))) {
            CombatUtil.attackCrystal(pos.up(3), true, true);
        }
        if (this.doPullCrystal(pos)) {
            return;
        }
        if (this.doPullCrystal(new BlockPos(this.target.posX + 0.1, this.target.posY + 0.5, this.target.posZ + 0.1))) {
            return;
        }
        if (this.doPullCrystal(new BlockPos(this.target.posX - 0.1, this.target.posY + 0.5, this.target.posZ + 0.1))) {
            return;
        }
        if (this.doPullCrystal(new BlockPos(this.target.posX + 0.1, this.target.posY + 0.5, this.target.posZ - 0.1))) {
            return;
        }
        this.doPullCrystal(new BlockPos(this.target.posX - 0.1, this.target.posY + 0.5, this.target.posZ - 0.1));
    }

    private boolean doPullCrystal(BlockPos pos) {
        if (this.pull(pos.up(2))) {
            return true;
        }
        if (this.pull(pos.up())) {
            return true;
        }
        if (this.crystal(pos.up())) {
            return true;
        }
        if (this.power(pos.up(2))) {
            return true;
        }
        if (this.power(pos.up())) {
            return true;
        }
        if (this.piston(pos.up(2))) {
            return true;
        }
        return this.piston(pos.up());
    }

    public static boolean check(boolean onlyStatic, boolean onGround, boolean onlyGround) {
        if (MovementUtil.isMoving() && onlyStatic) {
            return true;
        }
        if (onGround && onlyGround) {
            return true;
        }
        if (InventoryUtil.findHotbarBlock(Blocks.REDSTONE_BLOCK) == -1) {
            return true;
        }
        if (InventoryUtil.findHotbarClass(BlockPistonBase.class) == -1) {
            return true;
        }
        return InventoryUtil.findItemInHotbar(Items.END_CRYSTAL) == -1;
    }

    private EntityPlayer getTarget(double range) {
        EntityPlayer target = null;
        double distance = range;
        for (EntityPlayer player : PullCrystal.mc.world.playerEntities) {
            if (EntityUtil.invalid(player, range) || this.getBlock(EntityUtil.getEntityPos(player)) != Blocks.AIR) continue;
            if (target == null) {
                target = player;
                distance = PullCrystal.mc.player.getDistanceSq(player);
                continue;
            }
            if (PullCrystal.mc.player.getDistanceSq(player) >= distance) continue;
            target = player;
            distance = PullCrystal.mc.player.getDistanceSq(player);
        }
        return target;
    }

    private static boolean canFire(BlockPos pos) {
        if (BlockUtil.canReplace(pos.down())) {
            return false;
        }
        return PullCrystal.mc.world.isAirBlock(pos);
    }

    private void doFire(BlockPos pos, EnumFacing facing) {
        if (!this.fire.getValue()) {
            return;
        }
        if (InventoryUtil.findItemInHotbar(Items.FLINT_AND_STEEL) == -1) {
            return;
        }
        int old = PullCrystal.mc.player.inventory.currentItem;
        for (EnumFacing i : EnumFacing.VALUES) {
            if (i == EnumFacing.DOWN || i == EnumFacing.UP || pos.offset(i).equals(pos.offset(facing)) || PullCrystal.mc.world.getBlockState(pos.offset(i)).getBlock() != Blocks.FIRE) continue;
            return;
        }
        for (EnumFacing i : EnumFacing.VALUES) {
            if (i == EnumFacing.DOWN || i == EnumFacing.UP || pos.offset(i).equals(pos.offset(facing)) || pos.offset(i).equals(pos.offset(facing, -1)) && !BlockUtil.posHasCrystal(pos.offset(facing, -1)) || !PullCrystal.canFire(pos.offset(i))) continue;
            InventoryUtil.doSwap(InventoryUtil.findItemInHotbar(Items.FLINT_AND_STEEL));
            BlockUtil.placeBlock(pos.offset(i), EnumHand.MAIN_HAND, true, this.packet.getValue());
            InventoryUtil.doSwap(old);
            return;
        }
        if (PullCrystal.canFire(pos.offset(facing, 1))) {
            InventoryUtil.doSwap(InventoryUtil.findItemInHotbar(Items.FLINT_AND_STEEL));
            BlockUtil.placeBlock(pos.offset(facing, 1), EnumHand.MAIN_HAND, true, this.packet.getValue());
            InventoryUtil.doSwap(old);
        }
    }

    @Override
    public String getInfo() {
        if (this.target != null) {
            return this.target.getName();
        }
        return null;
    }

    public boolean crystal(BlockPos pos) {
        for (Entity crystal : PullCrystal.mc.world.loadedEntityList) {
            if (!(crystal instanceof EntityEnderCrystal) || crystal.getDistance((double)pos.getX() + 0.5, pos.getY(), (double)pos.getZ() + 0.5) > 4.0) continue;
            CombatUtil.attackCrystal(crystal, true, false);
            return true;
        }
        return false;
    }

    private boolean pistonActive(BlockPos pos, EnumFacing facing, BlockPos oPos) {
        if (this.pistonActive(pos, facing, oPos, false)) {
            return true;
        }
        return this.pistonActive(pos, facing, oPos, true);
    }

    private IBlockState getBlockState(BlockPos pos) {
        return PullCrystal.mc.world.getBlockState(pos);
    }

    private boolean pistonActive(BlockPos pos, EnumFacing facing, BlockPos oPos, boolean up) {
        if (up) {
            pos = pos.up();
        }
        if (!BlockUtil.canPlaceCrystal(oPos.offset(facing, -1)) && !BlockUtil.posHasCrystal(oPos.offset(facing, -1))) {
            return false;
        }
        if (!(this.getBlock(pos) instanceof BlockPistonBase)) {
            return false;
        }
        if (((EnumFacing)this.getBlockState(pos).getValue((IProperty)BlockDirectional.FACING)).getOpposite() != facing) {
            return false;
        }
        if (this.getBlock(pos.offset(facing, -1)) == Blocks.PISTON_EXTENSION) {
            return true;
        }
        if (this.getBlock(pos.offset(facing, -1)) != Blocks.PISTON_HEAD) {
            return false;
        }
        for (EnumFacing i : EnumFacing.VALUES) {
            if (this.getBlock(pos.offset(i)) != Blocks.REDSTONE_BLOCK) continue;
            if (!BlockUtil.posHasCrystal(oPos.offset(facing, -1))) {
                int old = PullCrystal.mc.player.inventory.currentItem;
                crystalPos = oPos.offset(facing, -1);
                InventoryUtil.doSwap(InventoryUtil.findItemInHotbar(Items.END_CRYSTAL));
                BlockUtil.placeCrystal(oPos.offset(facing, -1), true);
                InventoryUtil.doSwap(old);
            }
            this.doFire(oPos, facing);
            powerPos = pos.offset(i);
            this.mineBlock(pos.offset(i));
            return true;
        }
        return false;
    }

    private boolean power(BlockPos pos) {
        for (EnumFacing i : EnumFacing.VALUES) {
            if (i == EnumFacing.DOWN || i == EnumFacing.UP) continue;
            int offsetX = pos.offset(i).getX() - pos.getX();
            int offsetZ = pos.offset(i).getZ() - pos.getZ();
            if (this.placePower(pos.offset(i, 1).add(offsetZ, 0, offsetX), i, pos)) {
                return true;
            }
            if (this.placePower(pos.offset(i, 1).add(-offsetZ, 0, -offsetX), i, pos)) {
                return true;
            }
            if (this.placePower(pos.offset(i, -1).add(offsetZ, 0, offsetX), i, pos)) {
                return true;
            }
            if (!this.placePower(pos.offset(i, -1).add(-offsetZ, 0, -offsetX), i, pos)) continue;
            return true;
        }
        return false;
    }

    private boolean piston(BlockPos pos) {
        for (EnumFacing i : EnumFacing.VALUES) {
            if (i == EnumFacing.DOWN || i == EnumFacing.UP) continue;
            int offsetX = pos.offset(i).getX() - pos.getX();
            int offsetZ = pos.offset(i).getZ() - pos.getZ();
            if (this.placePiston(pos.offset(i, 1).add(offsetZ, 0, offsetX), i, pos)) {
                return true;
            }
            if (this.placePiston(pos.offset(i, 1).add(-offsetZ, 0, -offsetX), i, pos)) {
                return true;
            }
            if (this.placePiston(pos.offset(i, -1).add(offsetZ, 0, offsetX), i, pos)) {
                return true;
            }
            if (!this.placePiston(pos.offset(i, -1).add(-offsetZ, 0, -offsetX), i, pos)) continue;
            return true;
        }
        return false;
    }

    private boolean checkCrystal(BlockPos pos) {
        for (Entity entity : PullCrystal.mc.world.getEntitiesWithinAABB(Entity.class, new AxisAlignedBB(pos))) {
            float damage;
            if (!(entity instanceof EntityEnderCrystal) || !((damage = DamageUtil.calculateDamage(entity, this.target)) > 6.0f)) continue;
            return true;
        }
        return false;
    }

    private boolean placePower(BlockPos pos, EnumFacing facing, BlockPos oPos) {
        if (this.placePower(pos, facing, oPos, false)) {
            return true;
        }
        return this.placePower(pos, facing, oPos, true);
    }

    private boolean placePower(BlockPos pos, EnumFacing facing, BlockPos oPos, boolean up) {
        if (up) {
            pos = pos.up();
        }
        if (!BlockUtil.canPlaceCrystal(oPos.offset(facing, -1)) && !BlockUtil.posHasCrystal(oPos.offset(facing, -1))) {
            return false;
        }
        if (!(this.getBlock(pos) instanceof BlockPistonBase)) {
            return false;
        }
        if (((EnumFacing)this.getBlockState(pos).getValue((IProperty)BlockDirectional.FACING)).getOpposite() != facing) {
            return false;
        }
        if (this.getBlock(pos.offset(facing, -1)) == Blocks.PISTON_HEAD || this.getBlock(pos.offset(facing, -1)) == Blocks.PISTON_EXTENSION) {
            return true;
        }
        if (!PullCrystal.mc.world.isAirBlock(pos.offset(facing, -1)) && this.getBlock(pos.offset(facing, -1)) != Blocks.PISTON_HEAD && this.getBlock(pos.offset(facing, -1)) != Blocks.PISTON_EXTENSION && this.getBlock(pos.offset(facing, -1)) != Blocks.FIRE) {
            return false;
        }
        int old = PullCrystal.mc.player.inventory.currentItem;
        return this.placeRedStone(pos, facing, old, oPos);
    }

    private boolean placePiston(BlockPos pos, EnumFacing facing, BlockPos oPos) {
        if (this.placePiston(pos, facing, oPos, false)) {
            return true;
        }
        return this.placePiston(pos, facing, oPos, true);
    }

    private boolean placePiston(BlockPos pos, EnumFacing facing, BlockPos oPos, boolean up) {
        if (up) {
            pos = pos.up();
        }
        if (!BlockUtil.canPlaceCrystal(oPos.offset(facing, -1)) && !BlockUtil.posHasCrystal(oPos.offset(facing, -1))) {
            return false;
        }
        if (!BlockUtil.canPlace(pos) && !(this.getBlock(pos) instanceof BlockPistonBase)) {
            return false;
        }
        if (this.getBlock(pos) instanceof BlockPistonBase && ((EnumFacing)this.getBlockState(pos).getValue((IProperty)BlockDirectional.FACING)).getOpposite() != facing) {
            return false;
        }
        if (this.getBlock(pos.offset(facing, -1)) == Blocks.PISTON_HEAD || this.getBlock(pos.offset(facing, -1)) == Blocks.PISTON_EXTENSION) {
            return true;
        }
        if (!PullCrystal.mc.world.isAirBlock(pos.offset(facing, -1)) && this.getBlock(pos.offset(facing, -1)) != Blocks.PISTON_HEAD && this.getBlock(pos.offset(facing, -1)) != Blocks.PISTON_EXTENSION) {
            return false;
        }
        if ((PullCrystal.mc.player.posY - (double)pos.down().getY() <= -1.0 || PullCrystal.mc.player.posY - (double)pos.down().getY() >= 2.0) && BlockUtil.distanceToXZ((double)pos.getX() + 0.5, (double)pos.getZ() + 0.5) < 2.6) {
            return false;
        }
        int old = PullCrystal.mc.player.inventory.currentItem;
        if (BlockUtil.canPlace(pos)) {
            EntityUtil.facePlacePos(pos);
            AutoPush.pistonFacing(facing);
            InventoryUtil.doSwap(InventoryUtil.findHotbarBlock(BlockPistonBase.class));
            BlockUtil.placeBlock(pos, EnumHand.MAIN_HAND, false, this.pistonPacket.getValue());
            InventoryUtil.doSwap(old);
            EntityUtil.facePlacePos(pos);
            if (this.multiPlace.getValue() && this.placeRedStone(pos, facing, old, oPos)) {
                return true;
            }
            return true;
        }
        return this.placeRedStone(pos, facing, old, oPos);
    }

    private boolean placeRedStone(BlockPos pos, EnumFacing facing, int old, BlockPos oPos) {
        for (EnumFacing i : EnumFacing.VALUES) {
            if (this.getBlock(pos.offset(i)) != Blocks.REDSTONE_BLOCK) continue;
            powerPos = pos.offset(i);
            return true;
        }
        EnumFacing bestNeighboring = BlockUtil.getBestNeighboring(pos, facing);
        if (bestNeighboring != null && !pos.offset(bestNeighboring).equals(oPos.offset(facing, -1)) && !pos.offset(bestNeighboring).equals(oPos.offset(facing, -1).up()) && BlockUtil.canPlace(powerPos = pos.offset(bestNeighboring))) {
            InventoryUtil.doSwap(InventoryUtil.findHotbarBlock(Blocks.REDSTONE_BLOCK));
            BlockUtil.placeBlock(pos.offset(bestNeighboring), EnumHand.MAIN_HAND, true, this.packet.getValue());
            InventoryUtil.doSwap(old);
            return true;
        }
        for (EnumFacing i : EnumFacing.VALUES) {
            if (pos.offset(i).equals(pos.offset(facing, -1)) || pos.offset(i).equals(oPos.offset(facing, -1)) || pos.offset(i).equals(oPos.offset(facing, -1).up()) || !BlockUtil.canPlace(pos.offset(i))) continue;
            powerPos = pos.offset(i);
            InventoryUtil.doSwap(InventoryUtil.findHotbarBlock(Blocks.REDSTONE_BLOCK));
            BlockUtil.placeBlock(pos.offset(i), EnumHand.MAIN_HAND, true, this.packet.getValue());
            InventoryUtil.doSwap(old);
            return true;
        }
        return false;
    }

    private boolean pull(BlockPos pos) {
        for (EnumFacing i : EnumFacing.VALUES) {
            if (i == EnumFacing.DOWN || i == EnumFacing.UP) continue;
            int offsetX = pos.offset(i).getX() - pos.getX();
            int offsetZ = pos.offset(i).getZ() - pos.getZ();
            if (this.pistonActive(pos.offset(i, 1).add(offsetZ, 0, offsetX), i, pos)) {
                return true;
            }
            if (this.pistonActive(pos.offset(i, 1).add(-offsetZ, 0, -offsetX), i, pos)) {
                return true;
            }
            if (this.pistonActive(pos.offset(i, -1).add(offsetZ, 0, offsetX), i, pos)) {
                return true;
            }
            if (!this.pistonActive(pos.offset(i, -1).add(-offsetZ, 0, -offsetX), i, pos)) continue;
            return true;
        }
        return false;
    }

    private void mineBlock(BlockPos pos) {
        CombatUtil.mineBlock(pos);
    }

    private Block getBlock(BlockPos pos) {
        return PullCrystal.mc.world.getBlockState(pos).getBlock();
    }
}

