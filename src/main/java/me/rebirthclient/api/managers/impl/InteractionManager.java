package me.rebirthclient.api.managers.impl;

import java.util.Optional;
import me.rebirthclient.api.managers.Managers;
import me.rebirthclient.api.util.BlockUtil;
import me.rebirthclient.api.util.Timer;
import me.rebirthclient.mod.Mod;
import me.rebirthclient.mod.modules.impl.render.PlaceRender;
import net.minecraft.block.Block;
import net.minecraft.block.BlockAir;
import net.minecraft.block.BlockAnvil;
import net.minecraft.block.BlockBed;
import net.minecraft.block.BlockButton;
import net.minecraft.block.BlockCake;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.BlockDoor;
import net.minecraft.block.BlockFenceGate;
import net.minecraft.block.BlockLiquid;
import net.minecraft.block.BlockRedstoneDiode;
import net.minecraft.block.BlockTrapDoor;
import net.minecraft.block.BlockWorkbench;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityEnderCrystal;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.item.EntityXPOrb;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.init.Blocks;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.CPacketAnimation;
import net.minecraft.network.play.client.CPacketEntityAction;
import net.minecraft.network.play.client.CPacketPlayerTryUseItemOnBlock;
import net.minecraft.network.play.client.CPacketUseEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;

public class InteractionManager
extends Mod {
    private final Timer attackTimer = new Timer();

    public void placeBlock(BlockPos pos, boolean rotate, boolean packet, boolean attackCrystal, boolean ignoreEntities) {
        if (InteractionManager.fullNullCheck()) {
            return;
        }
        if (BlockUtil.canReplace(pos)) {
            Optional<ClickLocation> posCL;
            if (attackCrystal) {
                this.attackCrystals(pos, rotate);
            }
            if ((posCL = this.getClickLocation(pos, ignoreEntities, false, attackCrystal)).isPresent()) {
                BlockPos currentPos2 = posCL.get().neighbour;
                EnumFacing currentFace = posCL.get().opposite;
                boolean shouldSneak = this.shouldShiftClick(currentPos2);
                if (shouldSneak) {
                    InteractionManager.mc.player.connection.sendPacket(new CPacketEntityAction(InteractionManager.mc.player, CPacketEntityAction.Action.START_SNEAKING));
                }
                Vec3d hitVec = new Vec3d(currentPos2).add(0.5, 0.5, 0.5).add(new Vec3d(currentFace.getDirectionVec()).scale(0.5));
                if (rotate) {
                    Managers.ROTATIONS.lookAtVec3dPacket(hitVec, true);
                }
                if (packet) {
                    Vec3d extendedVec = new Vec3d(currentPos2).add(0.5, 0.5, 0.5);
                    float x = (float)(extendedVec.x - (double)currentPos2.getX());
                    float y = (float)(extendedVec.y - (double)currentPos2.getY());
                    float z = (float)(extendedVec.z - (double)currentPos2.getZ());
                    InteractionManager.mc.player.connection.sendPacket(new CPacketPlayerTryUseItemOnBlock(currentPos2, currentFace, EnumHand.MAIN_HAND, x, y, z));
                } else {
                    InteractionManager.mc.playerController.processRightClickBlock(InteractionManager.mc.player, InteractionManager.mc.world, currentPos2, currentFace, hitVec, EnumHand.MAIN_HAND);
                }
                PlaceRender.PlaceMap.put(pos, new PlaceRender.placePosition(pos));
                InteractionManager.mc.player.connection.sendPacket(new CPacketAnimation(EnumHand.MAIN_HAND));
                if (shouldSneak) {
                    InteractionManager.mc.player.connection.sendPacket(new CPacketEntityAction(InteractionManager.mc.player, CPacketEntityAction.Action.STOP_SNEAKING));
                }
            }
        }
    }

    public void placeBlock(BlockPos pos, boolean rotate, boolean packet, boolean attackCrystal) {
        this.placeBlock(pos, rotate, packet, attackCrystal, false);
    }

    public void attackEntity(Entity entity, boolean packet, boolean swing) {
        if (packet) {
            InteractionManager.mc.player.connection.sendPacket(new CPacketUseEntity(entity));
        } else {
            InteractionManager.mc.playerController.attackEntity(InteractionManager.mc.player, entity);
        }
        if (swing) {
            InteractionManager.mc.player.swingArm(EnumHand.MAIN_HAND);
        }
    }

    public void attackCrystals(BlockPos pos, boolean rotate) {
        boolean sprint = InteractionManager.mc.player.isSprinting();
        int ping = Managers.SERVER.getPing();
        for (EntityEnderCrystal crystal : InteractionManager.mc.world.getEntitiesWithinAABB(EntityEnderCrystal.class, new AxisAlignedBB(pos))) {
            if (!this.attackTimer.passedMs(ping <= 50 ? 75L : 100L)) continue;
            if (rotate) {
                Managers.ROTATIONS.lookAtVec3dPacket(crystal.getPositionVector(), true);
            }
            if (sprint) {
                InteractionManager.mc.player.connection.sendPacket(new CPacketEntityAction(InteractionManager.mc.player, CPacketEntityAction.Action.STOP_SPRINTING));
            }
            InteractionManager.mc.player.connection.sendPacket(new CPacketUseEntity(crystal));
            InteractionManager.mc.player.connection.sendPacket(new CPacketAnimation(EnumHand.MAIN_HAND));
            if (sprint) {
                InteractionManager.mc.player.connection.sendPacket(new CPacketEntityAction(InteractionManager.mc.player, CPacketEntityAction.Action.START_SPRINTING));
            }
            this.attackTimer.reset();
            break;
        }
    }

    public Optional<ClickLocation> getClickLocation(BlockPos pos, boolean ignoreEntities, boolean noPistons, boolean onlyCrystals) {
        Block block = InteractionManager.mc.world.getBlockState(pos).getBlock();
        if (!(block instanceof BlockAir) && !(block instanceof BlockLiquid)) {
            return Optional.empty();
        }
        if (!ignoreEntities) {
            for (Entity entity : InteractionManager.mc.world.getEntitiesWithinAABBExcludingEntity(null, new AxisAlignedBB(pos))) {
                if (onlyCrystals && entity instanceof EntityEnderCrystal || entity instanceof EntityItem || entity instanceof EntityXPOrb || entity instanceof EntityArrow) continue;
                return Optional.empty();
            }
        }
        EnumFacing side = null;
        for (EnumFacing blockSide : EnumFacing.values()) {
            IBlockState blockState;
            BlockPos sidePos = pos.offset(blockSide);
            if (noPistons && InteractionManager.mc.world.getBlockState(sidePos).getBlock() == Blocks.PISTON || !InteractionManager.mc.world.getBlockState(sidePos).getBlock().canCollideCheck(InteractionManager.mc.world.getBlockState(sidePos), false) || (blockState = InteractionManager.mc.world.getBlockState(sidePos)).getMaterial().isReplaceable()) continue;
            side = blockSide;
            break;
        }
        if (side == null) {
            return Optional.empty();
        }
        BlockPos neighbour = pos.offset(side);
        EnumFacing opposite = side.getOpposite();
        if (!InteractionManager.mc.world.getBlockState(neighbour).getBlock().canCollideCheck(InteractionManager.mc.world.getBlockState(neighbour), false)) {
            return Optional.empty();
        }
        return Optional.of(new ClickLocation(neighbour, opposite));
    }

    public boolean shouldShiftClick(BlockPos pos) {
        Block block = InteractionManager.mc.world.getBlockState(pos).getBlock();
        TileEntity tileEntity = null;
        for (TileEntity entity : InteractionManager.mc.world.loadedTileEntityList) {
            if (!entity.getPos().equals(pos)) continue;
            tileEntity = entity;
            break;
        }
        return tileEntity != null || block instanceof BlockBed || block instanceof BlockContainer || block instanceof BlockDoor || block instanceof BlockTrapDoor || block instanceof BlockFenceGate || block instanceof BlockButton || block instanceof BlockAnvil || block instanceof BlockWorkbench || block instanceof BlockCake || block instanceof BlockRedstoneDiode;
    }

    public static class ClickLocation {
        public final BlockPos neighbour;
        public final EnumFacing opposite;

        public ClickLocation(BlockPos neighbour, EnumFacing opposite) {
            this.neighbour = neighbour;
            this.opposite = opposite;
        }
    }
}

