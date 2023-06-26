package me.rebirthclient.api.util;

import java.util.Arrays;
import java.util.List;
import me.rebirthclient.api.util.DamageUtil;
import me.rebirthclient.api.util.Wrapper;
import me.rebirthclient.mod.modules.impl.combat.CrystalBot;
import net.minecraft.block.Block;
import net.minecraft.block.BlockAir;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityEnderCrystal;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.Explosion;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public class CrystalUtil
implements Wrapper {
    private static final List<Block> valid = Arrays.asList(Blocks.OBSIDIAN, Blocks.BEDROCK, Blocks.ENDER_CHEST, Blocks.ANVIL);

    public static int getCrystalSlot() {
        int n = -1;
        if (Wrapper.mc.player.getHeldItemMainhand().getItem() == Items.END_CRYSTAL) {
            n = Wrapper.mc.player.inventory.currentItem;
        }
        if (n == -1) {
            for (int i = 0; i < 9; ++i) {
                if (Wrapper.mc.player.inventory.getStackInSlot(i).getItem() != Items.END_CRYSTAL) continue;
                n = i;
                break;
            }
        }
        return n;
    }

    public static int getSwordSlot() {
        int n = -1;
        if (Wrapper.mc.player.getHeldItemMainhand().getItem() == Items.DIAMOND_SWORD) {
            n = Wrapper.mc.player.inventory.currentItem;
        }
        if (n == -1) {
            for (int i = 0; i < 9; ++i) {
                if (Wrapper.mc.player.inventory.getStackInSlot(i).getItem() != Items.DIAMOND_SWORD) continue;
                n = i;
                break;
            }
        }
        return n;
    }

    public static int ping() {
        if (mc.getConnection() == null) {
            return 50;
        }
        if (CrystalUtil.mc.player == null) {
            return 50;
        }
        try {
            return mc.getConnection().getPlayerInfo(CrystalUtil.mc.player.getUniqueID()).getResponseTime();
        }
        catch (NullPointerException nullPointerException) {
            return 50;
        }
    }

    public static boolean isVisible(Vec3d vec3d) {
        Vec3d vec3d2 = new Vec3d(CrystalUtil.mc.player.posX, CrystalUtil.mc.player.getEntityBoundingBox().minY + (double)CrystalUtil.mc.player.getEyeHeight(), CrystalUtil.mc.player.posZ);
        return CrystalUtil.mc.world.rayTraceBlocks(vec3d2, vec3d) == null;
    }

    public static boolean rayTraceBreak(double d, double d2, double d3) {
        if (CrystalUtil.mc.world.rayTraceBlocks(new Vec3d(CrystalUtil.mc.player.posX, CrystalUtil.mc.player.posY + (double)CrystalUtil.mc.player.getEyeHeight(), CrystalUtil.mc.player.posZ), new Vec3d(d, d2 + 1.8, d3), false, true, false) == null) {
            return true;
        }
        if (CrystalUtil.mc.world.rayTraceBlocks(new Vec3d(CrystalUtil.mc.player.posX, CrystalUtil.mc.player.posY + (double)CrystalUtil.mc.player.getEyeHeight(), CrystalUtil.mc.player.posZ), new Vec3d(d, d2 + 1.5, d3), false, true, false) == null) {
            return true;
        }
        return CrystalUtil.mc.world.rayTraceBlocks(new Vec3d(CrystalUtil.mc.player.posX, CrystalUtil.mc.player.posY + (double)CrystalUtil.mc.player.getEyeHeight(), CrystalUtil.mc.player.posZ), new Vec3d(d, d2, d3), false, true, false) == null;
    }

    public static float calculateDamage(BlockPos blockPos, Entity entity) {
        return CrystalUtil.calculateDamage((double)blockPos.getX() + 0.5, blockPos.getY() + 1, (double)blockPos.getZ() + 0.5, entity);
    }

    public static float calculateDamage(EntityEnderCrystal entityEnderCrystal, Entity entity) {
        return CrystalUtil.calculateDamage(entityEnderCrystal.posX, entityEnderCrystal.posY, entityEnderCrystal.posZ, entity);
    }

    public static float calculateDamage(double posX, double posY, double posZ, Entity entity) {
        float doubleExplosionSize = 12.0f;
        Vec3d entityPosVec = CrystalUtil.getEntityPosVec(entity, CrystalBot.predictTicks.getValue() > 0 ? CrystalBot.predictTicks.getValue() : 0);
        double distancedsize = entityPosVec.distanceTo(new Vec3d(posX, posY, posZ)) / (double)doubleExplosionSize;
        Vec3d vec3d = new Vec3d(posX, posY, posZ);
        double blockDensity = 0.0;
        try {
            blockDensity = CrystalBot.terrainIgnore.getValue() ? (double)CrystalUtil.getBlockDensity(vec3d, CrystalBot.predictTicks.getValue() > 0 ? entity.getEntityBoundingBox().offset(CrystalUtil.getMotionVec(entity, CrystalBot.predictTicks.getValue())) : entity.getEntityBoundingBox()) : (double)entity.world.getBlockDensity(vec3d, CrystalBot.predictTicks.getValue() > 0 ? entity.getEntityBoundingBox().offset(CrystalUtil.getMotionVec(entity, CrystalBot.predictTicks.getValue())) : entity.getEntityBoundingBox());
        }
        catch (Exception exception) {
            // empty catch block
        }
        double v = (1.0 - distancedsize) * blockDensity;
        float damage = (int)((v * v + v) / 2.0 * 7.0 * (double)doubleExplosionSize + 1.0);
        double finald = 1.0;
        if (entity instanceof EntityLivingBase) {
            finald = DamageUtil.getBlastReduction((EntityLivingBase)entity, DamageUtil.getDamageMultiplied(damage), new Explosion(CrystalUtil.mc.world, CrystalUtil.mc.player, posX, posY, posZ, 6.0f, false, true));
        }
        return (float)finald;
    }

    public static RayTraceResult rayTraceBlocks(Vec3d start, Vec3d end) {
        return CrystalUtil.rayTraceBlocks(start, end, false, false, false);
    }

    public static RayTraceResult rayTraceBlocks(Vec3d vec31, Vec3d vec32, boolean stopOnLiquid, boolean ignoreBlockWithoutBoundingBox, boolean returnLastUncollidableBlock) {
        if (!(Double.isNaN(vec31.x) || Double.isNaN(vec31.y) || Double.isNaN(vec31.z))) {
            if (!(Double.isNaN(vec32.x) || Double.isNaN(vec32.y) || Double.isNaN(vec32.z))) {
                int j1;
                int i1;
                int i = MathHelper.floor(vec32.x);
                int j = MathHelper.floor(vec32.y);
                int k = MathHelper.floor(vec32.z);
                int l = MathHelper.floor(vec31.x);
                BlockPos blockpos = new BlockPos(l, i1 = MathHelper.floor(vec31.y), j1 = MathHelper.floor(vec31.z));
                IBlockState iblockstate = CrystalUtil.mc.world.getBlockState(blockpos);
                Block block = iblockstate.getBlock();
                if (!valid.contains(block)) {
                    block = Blocks.AIR;
                    iblockstate = Blocks.AIR.getBlockState().getBaseState();
                }
                if ((!ignoreBlockWithoutBoundingBox || iblockstate.getCollisionBoundingBox(CrystalUtil.mc.world, blockpos) != Block.NULL_AABB) && block.canCollideCheck(iblockstate, stopOnLiquid)) {
                    return iblockstate.collisionRayTrace(CrystalUtil.mc.world, blockpos, vec31, vec32);
                }
                RayTraceResult raytraceresult2 = null;
                int k1 = 200;
                while (k1-- >= 0) {
                    EnumFacing enumfacing;
                    if (Double.isNaN(vec31.x) || Double.isNaN(vec31.y) || Double.isNaN(vec31.z)) {
                        return null;
                    }
                    if (l == i && i1 == j && j1 == k) {
                        return returnLastUncollidableBlock ? raytraceresult2 : null;
                    }
                    boolean flag2 = true;
                    boolean flag = true;
                    boolean flag1 = true;
                    double d0 = 999.0;
                    double d1 = 999.0;
                    double d2 = 999.0;
                    if (i > l) {
                        d0 = (double)l + 1.0;
                    } else if (i < l) {
                        d0 = (double)l + 0.0;
                    } else {
                        flag2 = false;
                    }
                    if (j > i1) {
                        d1 = (double)i1 + 1.0;
                    } else if (j < i1) {
                        d1 = (double)i1 + 0.0;
                    } else {
                        flag = false;
                    }
                    if (k > j1) {
                        d2 = (double)j1 + 1.0;
                    } else if (k < j1) {
                        d2 = (double)j1 + 0.0;
                    } else {
                        flag1 = false;
                    }
                    double d3 = 999.0;
                    double d4 = 999.0;
                    double d5 = 999.0;
                    double d6 = vec32.x - vec31.x;
                    double d7 = vec32.y - vec31.y;
                    double d8 = vec32.z - vec31.z;
                    if (flag2) {
                        d3 = (d0 - vec31.x) / d6;
                    }
                    if (flag) {
                        d4 = (d1 - vec31.y) / d7;
                    }
                    if (flag1) {
                        d5 = (d2 - vec31.z) / d8;
                    }
                    if (d3 == -0.0) {
                        d3 = -1.0E-4;
                    }
                    if (d4 == -0.0) {
                        d4 = -1.0E-4;
                    }
                    if (d5 == -0.0) {
                        d5 = -1.0E-4;
                    }
                    if (d3 < d4 && d3 < d5) {
                        enumfacing = i > l ? EnumFacing.WEST : EnumFacing.EAST;
                        vec31 = new Vec3d(d0, vec31.y + d7 * d3, vec31.z + d8 * d3);
                    } else if (d4 < d5) {
                        enumfacing = j > i1 ? EnumFacing.DOWN : EnumFacing.UP;
                        vec31 = new Vec3d(vec31.x + d6 * d4, d1, vec31.z + d8 * d4);
                    } else {
                        enumfacing = k > j1 ? EnumFacing.NORTH : EnumFacing.SOUTH;
                        vec31 = new Vec3d(vec31.x + d6 * d5, vec31.y + d7 * d5, d2);
                    }
                    l = MathHelper.floor(vec31.x) - (enumfacing == EnumFacing.EAST ? 1 : 0);
                    i1 = MathHelper.floor(vec31.y) - (enumfacing == EnumFacing.UP ? 1 : 0);
                    j1 = MathHelper.floor(vec31.z) - (enumfacing == EnumFacing.SOUTH ? 1 : 0);
                    blockpos = new BlockPos(l, i1, j1);
                    IBlockState iblockstate1 = CrystalUtil.mc.world.getBlockState(blockpos);
                    Block block1 = iblockstate1.getBlock();
                    if (!valid.contains(block1)) {
                        block1 = Blocks.AIR;
                        iblockstate1 = Blocks.AIR.getBlockState().getBaseState();
                    }
                    if (ignoreBlockWithoutBoundingBox && iblockstate1.getMaterial() != Material.PORTAL && iblockstate1.getCollisionBoundingBox(CrystalUtil.mc.world, blockpos) == Block.NULL_AABB) continue;
                    if (block1.canCollideCheck(iblockstate1, stopOnLiquid)) {
                        return iblockstate1.collisionRayTrace(CrystalUtil.mc.world, blockpos, vec31, vec32);
                    }
                    raytraceresult2 = new RayTraceResult(RayTraceResult.Type.MISS, vec31, enumfacing, blockpos);
                }
                return returnLastUncollidableBlock ? raytraceresult2 : null;
            }
            return null;
        }
        return null;
    }

    public static float getBlockDensity(Vec3d vec, AxisAlignedBB bb) {
        double d0 = 1.0 / ((bb.maxX - bb.minX) * 2.0 + 1.0);
        double d1 = 1.0 / ((bb.maxY - bb.minY) * 2.0 + 1.0);
        double d2 = 1.0 / ((bb.maxZ - bb.minZ) * 2.0 + 1.0);
        double d3 = (1.0 - Math.floor(1.0 / d0) * d0) / 2.0;
        double d4 = (1.0 - Math.floor(1.0 / d2) * d2) / 2.0;
        if (d0 >= 0.0 && d1 >= 0.0 && d2 >= 0.0) {
            int j2 = 0;
            int k2 = 0;
            float f = 0.0f;
            while (f <= 0.5f) {
                float f1 = 0.0f;
                while (f1 <= 0.5f) {
                    float f2 = 0.0f;
                    while (f2 <= 0.5f) {
                        double d5 = bb.minX + (bb.maxX - bb.minX) * (double)f;
                        double d6 = bb.minY + (bb.maxY - bb.minY) * (double)f1;
                        double d7 = bb.minZ + (bb.maxZ - bb.minZ) * (double)f2;
                        if (CrystalUtil.rayTraceBlocks(new Vec3d(d5 + d3, d6, d7 + d4), vec) == null) {
                            ++j2;
                        }
                        ++k2;
                        f2 = (float)((double)f2 + d2);
                    }
                    f1 = (float)((double)f1 + d1);
                }
                f = (float)((double)f + d0);
            }
            return (float)j2 / (float)k2;
        }
        return 0.0f;
    }

    public static Vec3d getMotionVec(Entity entity, int ticks) {
        double dX = entity.posX - entity.prevPosX;
        double dZ = entity.posZ - entity.prevPosZ;
        double entityMotionPosX = 0.0;
        double entityMotionPosZ = 0.0;
        if (CrystalBot.collision.getValue()) {
            for (int i = 1; i <= ticks && CrystalUtil.mc.world.getBlockState(new BlockPos(entity.posX + dX * (double)i, entity.posY, entity.posZ + dZ * (double)i)).getBlock() instanceof BlockAir; ++i) {
                entityMotionPosX = dX * (double)i;
                entityMotionPosZ = dZ * (double)i;
            }
        } else {
            entityMotionPosX = dX * (double)ticks;
            entityMotionPosZ = dZ * (double)ticks;
        }
        return new Vec3d(entityMotionPosX, 0.0, entityMotionPosZ);
    }

    public static Vec3d getEntityPosVec(Entity entity, int ticks) {
        return entity.getPositionVector().add(CrystalUtil.getMotionVec(entity, ticks));
    }

    public static float calculateDamage(Vec3d vec3d, Entity entity) {
        return CrystalUtil.calculateDamage(vec3d.x, vec3d.y, vec3d.z, entity);
    }

    public static boolean rayTracePlace(BlockPos blockPos) {
        if (CrystalBot.getInstance().directionMode.getValue() != CrystalBot.DirectionMode.VANILLA) {
            double d = 0.45;
            double d2 = 0.05;
            double d3 = 0.95;
            Vec3d vec3d = new Vec3d(CrystalUtil.mc.player.posX, CrystalUtil.mc.player.getEntityBoundingBox().minY + (double)CrystalUtil.mc.player.getEyeHeight(), CrystalUtil.mc.player.posZ);
            for (double d4 = d2; d4 <= d3; d4 += d) {
                for (double d5 = d2; d5 <= d3; d5 += d) {
                    for (double d6 = d2; d6 <= d3; d6 += d) {
                        Vec3d vec3d2 = new Vec3d(blockPos).add(d4, d5, d6);
                        double d7 = vec3d.distanceTo(vec3d2);
                        if (CrystalBot.getInstance().strictDirection.getValue() && d7 > (double) CrystalBot.getInstance().placeRange.getValue()) continue;
                        double d8 = vec3d2.x - vec3d.x;
                        double d9 = vec3d2.y - vec3d.y;
                        double d10 = vec3d2.z - vec3d.z;
                        double d11 = MathHelper.sqrt(d8 * d8 + d10 * d10);
                        double[] arrd = new double[]{MathHelper.wrapDegrees((float)Math.toDegrees(Math.atan2(d10, d8)) - 90.0f), MathHelper.wrapDegrees((float)(-Math.toDegrees(Math.atan2(d9, d11))))};
                        float f = MathHelper.cos((float)(-arrd[0] * 0.01745329238474369 - 3.1415927410125732));
                        float f2 = MathHelper.sin((float)(-arrd[0] * 0.01745329238474369 - 3.1415927410125732));
                        float f3 = -MathHelper.cos((float)(-arrd[1] * 0.01745329238474369));
                        float f4 = MathHelper.sin((float)(-arrd[1] * 0.01745329238474369));
                        Vec3d vec3d3 = new Vec3d(f2 * f3, f4, f * f3);
                        Vec3d vec3d4 = vec3d.add(vec3d3.x * d7, vec3d3.y * d7, vec3d3.z * d7);
                        RayTraceResult rayTraceResult = CrystalUtil.mc.world.rayTraceBlocks(vec3d, vec3d4, false, false, false);
                        if (rayTraceResult == null || rayTraceResult.typeOfHit != RayTraceResult.Type.BLOCK || !rayTraceResult.getBlockPos().equals(blockPos)) continue;
                        return true;
                    }
                }
            }
            return false;
        }
        for (EnumFacing enumFacing : EnumFacing.values()) {
            RayTraceResult rayTraceResult;
            Vec3d vec3d = new Vec3d((double)blockPos.getX() + 0.5 + (double)enumFacing.getDirectionVec().getX() * 0.5, (double)blockPos.getY() + 0.5 + (double)enumFacing.getDirectionVec().getY() * 0.5, (double)blockPos.getZ() + 0.5 + (double)enumFacing.getDirectionVec().getZ() * 0.5);
            if (CrystalBot.getInstance().strictDirection.getValue() && CrystalUtil.mc.player.getPositionVector().add(0.0, CrystalUtil.mc.player.getEyeHeight(), 0.0).distanceTo(vec3d) > (double) CrystalBot.getInstance().placeRange.getValue() || (rayTraceResult = CrystalUtil.mc.world.rayTraceBlocks(new Vec3d(CrystalUtil.mc.player.posX, CrystalUtil.mc.player.posY + (double)CrystalUtil.mc.player.getEyeHeight(), CrystalUtil.mc.player.posZ), vec3d, false, true, false)) == null || !rayTraceResult.typeOfHit.equals(RayTraceResult.Type.BLOCK) || !rayTraceResult.getBlockPos().equals(blockPos)) continue;
            return true;
        }
        return false;
    }

    public static float calculateDamage(BlockPos blockPos, Entity entity, int predictTicks, boolean collision, boolean terrainIgnore) {
        return CrystalUtil.calculateDamage((double)blockPos.getX() + 0.5, blockPos.getY() + 1, (double)blockPos.getZ() + 0.5, entity, predictTicks, collision, terrainIgnore);
    }

    public static float calculateDamage(EntityEnderCrystal entityEnderCrystal, Entity entity, int predictTicks, boolean collision, boolean terrainIgnore) {
        return CrystalUtil.calculateDamage(entityEnderCrystal.posX, entityEnderCrystal.posY, entityEnderCrystal.posZ, entity, predictTicks, collision, terrainIgnore);
    }

    public static float calculateDamage(double posX, double posY, double posZ, Entity entity, int predictTicks, boolean collision, boolean terrainIgnore) {
        float doubleExplosionSize = 12.0f;
        Vec3d entityPosVec = CrystalUtil.getEntityPosVec(entity, Math.max(predictTicks, 0), collision);
        double distancedsize = entityPosVec.distanceTo(new Vec3d(posX, posY, posZ)) / (double)doubleExplosionSize;
        Vec3d vec3d = new Vec3d(posX, posY, posZ);
        double blockDensity = 0.0;
        try {
            blockDensity = terrainIgnore ? (double)CrystalUtil.getBlockDensity(vec3d, predictTicks > 0 ? entity.getEntityBoundingBox().offset(CrystalUtil.getMotionVec(entity, predictTicks, collision)) : entity.getEntityBoundingBox()) : (double)entity.world.getBlockDensity(vec3d, predictTicks > 0 ? entity.getEntityBoundingBox().offset(CrystalUtil.getMotionVec(entity, predictTicks, collision)) : entity.getEntityBoundingBox());
        }
        catch (Exception exception) {
            // empty catch block
        }
        double v = (1.0 - distancedsize) * blockDensity;
        float damage = (int)((v * v + v) / 2.0 * 7.0 * (double)doubleExplosionSize + 1.0);
        double finald = 1.0;
        if (entity instanceof EntityLivingBase) {
            finald = DamageUtil.getBlastReduction((EntityLivingBase)entity, DamageUtil.getDamageMultiplied(damage), new Explosion(CrystalUtil.mc.world, CrystalUtil.mc.player, posX, posY, posZ, 6.0f, false, true));
        }
        return (float)finald;
    }

    public static Vec3d getEntityPosVec(Entity entity, int ticks, boolean collision) {
        return entity.getPositionVector().add(CrystalUtil.getMotionVec(entity, ticks, collision));
    }

    public static Vec3d getMotionVec(Entity entity, int ticks, boolean collision) {
        double dX = entity.posX - entity.prevPosX;
        double dZ = entity.posZ - entity.prevPosZ;
        double entityMotionPosX = 0.0;
        double entityMotionPosZ = 0.0;
        if (collision) {
            for (int i = 1; i <= ticks && CrystalUtil.mc.world.getBlockState(new BlockPos(entity.posX + dX * (double)i, entity.posY, entity.posZ + dZ * (double)i)).getBlock() instanceof BlockAir; ++i) {
                entityMotionPosX = dX * (double)i;
                entityMotionPosZ = dZ * (double)i;
            }
        } else {
            entityMotionPosX = dX * (double)ticks;
            entityMotionPosZ = dZ * (double)ticks;
        }
        return new Vec3d(entityMotionPosX, 0.0, entityMotionPosZ);
    }
}

