package me.rebirthclient.api.util;

import me.rebirthclient.api.managers.Managers;
import me.rebirthclient.api.util.BlockUtil;
import me.rebirthclient.api.util.EntityUtil;
import me.rebirthclient.api.util.Timer;
import me.rebirthclient.api.util.Wrapper;
import me.rebirthclient.mod.modules.impl.combat.CombatSetting;
import me.rebirthclient.mod.modules.impl.combat.PacketMine;
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityEnderCrystal;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.CPacketUseEntity;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;

public class CombatUtil
implements Wrapper {
    public static final Timer breakTimer = new Timer();

    public static void mineBlock(BlockPos pos) {
        if (PacketMine.godBlocks.contains(CombatUtil.getBlock(pos)) && PacketMine.INSTANCE.godCancel.getValue()) {
            return;
        }
        if (pos.equals(PacketMine.breakPos)) {
            return;
        }
        CombatUtil.mc.playerController.onPlayerDamageBlock(pos, BlockUtil.getRayTraceFacing(pos));
    }

    public static void attackCrystal(BlockPos pos, boolean rotate, boolean eatingPause) {
        if (!breakTimer.passedMs(CombatSetting.INSTANCE.attackDelay.getValue())) {
            return;
        }
        if (eatingPause && EntityUtil.isEating()) {
            return;
        }
        for (Entity entity : CombatUtil.mc.world.getEntitiesWithinAABB(Entity.class, new AxisAlignedBB(pos))) {
            if (!(entity instanceof EntityEnderCrystal)) continue;
            breakTimer.reset();
            CombatUtil.mc.player.connection.sendPacket(new CPacketUseEntity(entity));
            CombatUtil.mc.player.swingArm(EnumHand.MAIN_HAND);
            if (!rotate) break;
            EntityUtil.faceXYZ(entity.posX, entity.posY + 0.25, entity.posZ);
            break;
        }
    }

    public static void attackCrystal(Entity entity, boolean rotate, boolean eatingPause) {
        if (!breakTimer.passedMs(CombatSetting.INSTANCE.attackDelay.getValue())) {
            return;
        }
        if (eatingPause && EntityUtil.isEating()) {
            return;
        }
        if (entity != null) {
            breakTimer.reset();
            CombatUtil.mc.player.connection.sendPacket(new CPacketUseEntity(entity));
            CombatUtil.mc.player.swingArm(EnumHand.MAIN_HAND);
            if (rotate) {
                EntityUtil.faceXYZ(entity.posX, entity.posY + 0.25, entity.posZ);
            }
        }
    }

    public static boolean isHole(BlockPos pos, boolean anyBlock, int blocks, boolean onlyCanStand) {
        int blockProgress = 0;
        if (anyBlock) {
            if (!(CombatUtil.canBlockReplace(pos.add(0, 0, 1)) && (CombatUtil.canBlockReplace(pos.add(0, 0, 2)) || CombatUtil.canBlockReplace(pos.add(0, 1, 1)) || CombatUtil.canBlockReplace(pos.add(1, 0, 1)) || CombatUtil.canBlockReplace(pos.add(-1, 0, 1))))) {
                ++blockProgress;
            }
            if (!(CombatUtil.canBlockReplace(pos.add(0, 0, -1)) && (CombatUtil.canBlockReplace(pos.add(0, 0, -2)) || CombatUtil.canBlockReplace(pos.add(0, 1, -1)) || CombatUtil.canBlockReplace(pos.add(1, 0, -1)) || CombatUtil.canBlockReplace(pos.add(-1, 0, -1))))) {
                ++blockProgress;
            }
            if (!(CombatUtil.canBlockReplace(pos.add(1, 0, 0)) && (CombatUtil.canBlockReplace(pos.add(2, 0, 0)) || CombatUtil.canBlockReplace(pos.add(1, 1, 0)) || CombatUtil.canBlockReplace(pos.add(1, 0, 1)) || CombatUtil.canBlockReplace(pos.add(1, 0, -1))))) {
                ++blockProgress;
            }
            if (!(CombatUtil.canBlockReplace(pos.add(-1, 0, 0)) && (CombatUtil.canBlockReplace(pos.add(-2, 0, 0)) || CombatUtil.canBlockReplace(pos.add(-1, 1, 0)) || CombatUtil.canBlockReplace(pos.add(-1, 0, 1)) || CombatUtil.canBlockReplace(pos.add(-1, 0, -1))))) {
                ++blockProgress;
            }
        } else {
            if (!(CombatUtil.getBlock(pos.add(0, 0, 1)) != Blocks.OBSIDIAN && CombatUtil.getBlock(pos.add(0, 0, 1)) != Blocks.BEDROCK && (CombatUtil.getBlock(pos.add(0, 0, 2)) != Blocks.OBSIDIAN && CombatUtil.getBlock(pos.add(0, 0, 2)) != Blocks.BEDROCK || CombatUtil.getBlock(pos.add(0, 1, 1)) != Blocks.OBSIDIAN && CombatUtil.getBlock(pos.add(0, 1, 1)) != Blocks.BEDROCK || CombatUtil.getBlock(pos.add(1, 0, 1)) != Blocks.OBSIDIAN && CombatUtil.getBlock(pos.add(1, 0, 1)) != Blocks.BEDROCK || CombatUtil.getBlock(pos.add(-1, 0, 1)) != Blocks.OBSIDIAN && CombatUtil.getBlock(pos.add(-1, 0, 1)) != Blocks.BEDROCK))) {
                ++blockProgress;
            }
            if (!(CombatUtil.getBlock(pos.add(0, 0, -1)) != Blocks.OBSIDIAN && CombatUtil.getBlock(pos.add(0, 0, -1)) != Blocks.BEDROCK && (CombatUtil.getBlock(pos.add(0, 0, -2)) != Blocks.OBSIDIAN && CombatUtil.getBlock(pos.add(0, 0, -2)) != Blocks.BEDROCK || CombatUtil.getBlock(pos.add(0, 1, -1)) != Blocks.OBSIDIAN && CombatUtil.getBlock(pos.add(0, 1, -1)) != Blocks.BEDROCK || CombatUtil.getBlock(pos.add(1, 0, -1)) != Blocks.OBSIDIAN && CombatUtil.getBlock(pos.add(1, 0, -1)) != Blocks.BEDROCK || CombatUtil.getBlock(pos.add(-1, 0, -1)) != Blocks.OBSIDIAN && CombatUtil.getBlock(pos.add(-1, 0, -1)) != Blocks.BEDROCK))) {
                ++blockProgress;
            }
            if (!(CombatUtil.getBlock(pos.add(1, 0, 0)) != Blocks.OBSIDIAN && CombatUtil.getBlock(pos.add(1, 0, 0)) != Blocks.BEDROCK && (CombatUtil.getBlock(pos.add(2, 0, 0)) != Blocks.OBSIDIAN && CombatUtil.getBlock(pos.add(2, 0, 0)) != Blocks.BEDROCK || CombatUtil.getBlock(pos.add(1, 1, 0)) != Blocks.OBSIDIAN && CombatUtil.getBlock(pos.add(1, 1, 0)) != Blocks.BEDROCK || CombatUtil.getBlock(pos.add(1, 0, 1)) != Blocks.OBSIDIAN && CombatUtil.getBlock(pos.add(1, 0, 1)) != Blocks.BEDROCK || CombatUtil.getBlock(pos.add(1, 0, -1)) != Blocks.OBSIDIAN && CombatUtil.getBlock(pos.add(1, 0, -1)) != Blocks.BEDROCK))) {
                ++blockProgress;
            }
            if (!(CombatUtil.getBlock(pos.add(-1, 0, 0)) != Blocks.OBSIDIAN && CombatUtil.getBlock(pos.add(-1, 0, 0)) != Blocks.BEDROCK && (CombatUtil.getBlock(pos.add(-2, 0, 0)) != Blocks.OBSIDIAN && CombatUtil.getBlock(pos.add(-2, 0, 0)) != Blocks.BEDROCK || CombatUtil.getBlock(pos.add(-1, 1, 0)) != Blocks.OBSIDIAN && CombatUtil.getBlock(pos.add(-1, 1, 0)) != Blocks.BEDROCK || CombatUtil.getBlock(pos.add(-1, 0, 1)) != Blocks.OBSIDIAN && CombatUtil.getBlock(pos.add(-1, 0, 1)) != Blocks.BEDROCK || CombatUtil.getBlock(pos.add(-1, 0, -1)) != Blocks.OBSIDIAN && CombatUtil.getBlock(pos.add(-1, 0, -1)) != Blocks.BEDROCK))) {
                ++blockProgress;
            }
        }
        return CombatUtil.getBlock(pos) == Blocks.AIR && CombatUtil.getBlock(pos.add(0, 1, 0)) == Blocks.AIR && (CombatUtil.getBlock(pos.add(0, -1, 0)) != Blocks.AIR || !onlyCanStand) && CombatUtil.getBlock(pos.add(0, 2, 0)) == Blocks.AIR && blockProgress > blocks - 1;
    }

    public static Block getBlock(BlockPos pos) {
        return CombatUtil.mc.world.getBlockState(pos).getBlock();
    }

    public static boolean canBlockReplace(BlockPos pos) {
        return CombatUtil.mc.world.isAirBlock(pos) || CombatUtil.getBlock(pos) == Blocks.FIRE || CombatUtil.getBlock(pos) == Blocks.LAVA || CombatUtil.getBlock(pos) == Blocks.FLOWING_LAVA || CombatUtil.getBlock(pos) == Blocks.WATER || CombatUtil.getBlock(pos) == Blocks.FLOWING_WATER;
    }

    public static EntityPlayer getTarget(double range) {
        EntityPlayer target = null;
        double distance = range;
        for (EntityPlayer player : CombatUtil.mc.world.playerEntities) {
            if (EntityUtil.invalid(player, range)) continue;
            if (target == null) {
                target = player;
                distance = CombatUtil.mc.player.getDistanceSq(player);
                continue;
            }
            if (CombatUtil.mc.player.getDistanceSq(player) >= distance) continue;
            target = player;
            distance = CombatUtil.mc.player.getDistanceSq(player);
        }
        return target;
    }

    public static EntityPlayer getTarget(double range, double maxSpeed) {
        EntityPlayer target = null;
        double distance = range;
        for (EntityPlayer player : CombatUtil.mc.world.playerEntities) {
            if (Managers.SPEED.getPlayerSpeed(player) > maxSpeed || EntityUtil.invalid(player, range)) continue;
            if (target == null) {
                target = player;
                distance = CombatUtil.mc.player.getDistanceSq(player);
                continue;
            }
            if (CombatUtil.mc.player.getDistanceSq(player) >= distance) continue;
            target = player;
            distance = CombatUtil.mc.player.getDistanceSq(player);
        }
        return target;
    }
}

