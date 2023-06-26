package me.rebirthclient.mod.modules.impl.combat;

import com.mojang.realmsclient.gui.ChatFormatting;
import java.util.Arrays;
import java.util.List;
import me.rebirthclient.api.managers.Managers;
import me.rebirthclient.api.util.BlockUtil;
import me.rebirthclient.api.util.EntityUtil;
import me.rebirthclient.api.util.InventoryUtil;
import me.rebirthclient.mod.modules.Category;
import me.rebirthclient.mod.modules.Module;
import me.rebirthclient.mod.modules.settings.Setting;
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.CPacketEntityAction;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;

public class SelfWeb
extends Module {
    public final List<Block> blackList = Arrays.asList(Blocks.ENDER_CHEST, Blocks.CHEST, Blocks.TRAPPED_CHEST, Blocks.CRAFTING_TABLE, Blocks.ANVIL, Blocks.BREWING_STAND, Blocks.HOPPER, Blocks.DROPPER, Blocks.DISPENSER);
    private final Setting<Boolean> rotate = this.add(new Setting<>("Rotate", true));
    private final Setting<Boolean> smart = this.add(new Setting<>("Smart", false).setParent());
    private final Setting<Integer> enemyRange = this.add(new Setting<>("EnemyRange", 4, 0, 8, v -> this.smart.isOpen()));
    private int newSlot = -1;
    private boolean sneak;

    public SelfWeb() {
        super("SelfWeb", "Places webs at your feet", Category.COMBAT);
    }

    @Override
    public void onEnable() {
        if (SelfWeb.mc.player != null) {
            this.newSlot = this.getHotbarItem();
            if (this.newSlot == -1) {
                this.sendMessage("[" + this.getName() + "] " + ChatFormatting.RED + "No Webs in hotbar. disabling...");
                this.disable();
            }
        }
    }

    @Override
    public void onDisable() {
        if (SelfWeb.mc.player != null && this.sneak) {
            SelfWeb.mc.player.connection.sendPacket(new CPacketEntityAction(SelfWeb.mc.player, CPacketEntityAction.Action.STOP_SNEAKING));
            this.sneak = false;
        }
    }

    @Override
    public void onUpdate() {
        if (SelfWeb.fullNullCheck()) {
            return;
        }
        if (this.smart.getValue()) {
            EntityPlayer target = this.getClosestTarget();
            if (target == null) {
                return;
            }
            if (Managers.FRIENDS.isFriend(target.getName())) {
                return;
            }
            if (SelfWeb.mc.player.getDistance(target) < (float) this.enemyRange.getValue() && this.isSafe()) {
                int last_slot = SelfWeb.mc.player.inventory.currentItem;
                InventoryUtil.doSwap(this.newSlot);
                this.placeBlock(this.getFloorPos());
                InventoryUtil.doSwap(last_slot);
            }
        } else {
            int last_slot = SelfWeb.mc.player.inventory.currentItem;
            InventoryUtil.doSwap(this.newSlot);
            this.placeBlock(this.getFloorPos());
            InventoryUtil.doSwap(last_slot);
            this.disable();
        }
    }

    private EntityPlayer getClosestTarget() {
        if (SelfWeb.mc.world.playerEntities.isEmpty()) {
            return null;
        }
        EntityPlayer closestTarget = null;
        for (EntityPlayer target : SelfWeb.mc.world.playerEntities) {
            if (target == SelfWeb.mc.player || !EntityUtil.isLiving(target) || target.getHealth() <= 0.0f || closestTarget != null && SelfWeb.mc.player.getDistance(target) > SelfWeb.mc.player.getDistance(closestTarget)) continue;
            closestTarget = target;
        }
        return closestTarget;
    }

    private int getHotbarItem() {
        for (int i = 0; i < 9; ++i) {
            ItemStack stack = SelfWeb.mc.player.inventory.getStackInSlot(i);
            if (stack.getItem() != Item.getItemById(30)) continue;
            return i;
        }
        return -1;
    }

    private boolean isSafe() {
        BlockPos player_block = this.getFloorPos();
        return SelfWeb.mc.world.getBlockState(player_block.east()).getBlock() != Blocks.AIR && SelfWeb.mc.world.getBlockState(player_block.west()).getBlock() != Blocks.AIR && SelfWeb.mc.world.getBlockState(player_block.north()).getBlock() != Blocks.AIR && SelfWeb.mc.world.getBlockState(player_block.south()).getBlock() != Blocks.AIR && SelfWeb.mc.world.getBlockState(player_block).getBlock() == Blocks.AIR;
    }

    private void placeBlock(BlockPos pos) {
        if (!SelfWeb.mc.world.getBlockState(pos).getMaterial().isReplaceable()) {
            return;
        }
        if (!this.checkForNeighbours(pos)) {
            return;
        }
        for (EnumFacing side : EnumFacing.values()) {
            BlockPos neighbor = pos.offset(side);
            EnumFacing side2 = side.getOpposite();
            if (!this.canBeClicked(neighbor)) continue;
            if (this.blackList.contains(SelfWeb.mc.world.getBlockState(neighbor).getBlock())) {
                SelfWeb.mc.player.connection.sendPacket(new CPacketEntityAction(SelfWeb.mc.player, CPacketEntityAction.Action.START_SNEAKING));
                this.sneak = true;
            }
            Vec3d hitVec = new Vec3d(neighbor).add(0.5, 0.5, 0.5).add(new Vec3d(side2.getDirectionVec()).scale(0.5));
            if (this.rotate.getValue()) {
                Managers.ROTATIONS.lookAtVec3dPacket(hitVec);
            }
            SelfWeb.mc.playerController.processRightClickBlock(SelfWeb.mc.player, SelfWeb.mc.world, neighbor, side2, hitVec, EnumHand.MAIN_HAND);
            SelfWeb.mc.player.swingArm(EnumHand.MAIN_HAND);
            return;
        }
    }

    private boolean checkForNeighbours(BlockPos blockPos) {
        if (!this.hasNeighbour(blockPos)) {
            for (EnumFacing side : EnumFacing.values()) {
                BlockPos neighbour = blockPos.offset(side);
                if (!this.hasNeighbour(neighbour)) continue;
                return true;
            }
            return false;
        }
        return true;
    }

    private boolean hasNeighbour(BlockPos blockPos) {
        for (EnumFacing side : EnumFacing.values()) {
            BlockPos neighbour = blockPos.offset(side);
            if (SelfWeb.mc.world.getBlockState(neighbour).getMaterial().isReplaceable()) continue;
            return true;
        }
        return false;
    }

    private boolean canBeClicked(BlockPos pos) {
        return BlockUtil.getBlock(pos).canCollideCheck(BlockUtil.getState(pos), false);
    }

    private BlockPos getFloorPos() {
        return new BlockPos(Math.floor(SelfWeb.mc.player.posX), Math.floor(SelfWeb.mc.player.posY), Math.floor(SelfWeb.mc.player.posZ));
    }
}

