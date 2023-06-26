package me.rebirthclient.api.util.troll;

import me.rebirthclient.api.util.troll.Function2;
import me.rebirthclient.api.util.troll.RayTraceAction;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public final class RaytraceKt {
    public static RayTraceResult rayTrace(World $this$rayTrace, Vec3d start, Vec3d end, int maxAttempt, Function2<? super BlockPos, ? super IBlockState, ? extends RayTraceAction> function) {
        double currentX = start.x;
        double currentY = start.y;
        double currentZ = start.z;
        double $this$fastFloor$iv = currentX;
        int currentBlockX = (int)($this$fastFloor$iv + 1.073741824E9) - 0x40000000;
        double $this$fastFloor$iv2 = currentY;
        int currentBlockY = (int)($this$fastFloor$iv2 + 1.073741824E9) - 0x40000000;
        double $this$fastFloor$iv3 = currentZ;
        int currentBlockZ = (int)($this$fastFloor$iv3 + 1.073741824E9) - 0x40000000;
        BlockPos.MutableBlockPos blockPos = new BlockPos.MutableBlockPos(currentBlockX, currentBlockY, currentBlockZ);
        IBlockState startBlockState = $this$rayTrace.getBlockState(blockPos);
        double endX = end.x;
        double endY = end.y;
        double endZ = end.z;
        RayTraceAction action = function.invoke(blockPos, startBlockState);
        if (action == RayTraceAction.Null.INSTANCE) {
            return null;
        }
        if (action == RayTraceAction.Calc.INSTANCE) {
            RayTraceResult raytrace = RaytraceKt.raytrace(startBlockState, $this$rayTrace, blockPos, currentX, currentY, currentZ, endX, endY, endZ);
            if (raytrace != null) {
                return raytrace;
            }
        } else if (action instanceof RayTraceAction.Result) {
            return ((RayTraceAction.Result)action).getRayTraceResult();
        }
        int endBlockX = (int)(endX + 1.073741824E9) - 0x40000000;
        int endBlockY = (int)(endY + 1.073741824E9) - 0x40000000;
        int endBlockZ = (int)(endZ + 1.073741824E9) - 0x40000000;
        int count = maxAttempt;
        while (count-- >= 0) {
            double $this$fastFloor$iv7;
            if (currentBlockX == endBlockX && currentBlockY == endBlockY && currentBlockZ == endBlockZ) {
                return null;
            }
            int nextX = 999;
            int nextY = 999;
            int nextZ = 999;
            double stepX = 999.0;
            double stepY = 999.0;
            double stepZ = 999.0;
            double diffX = end.x - currentX;
            double diffY = end.y - currentY;
            double diffZ = end.z - currentZ;
            if (endBlockX > currentBlockX) {
                nextX = currentBlockX + 1;
                stepX = ((double)nextX - currentX) / diffX;
            } else if (endBlockX < currentBlockX) {
                nextX = currentBlockX;
                stepX = ((double)nextX - currentX) / diffX;
            }
            if (endBlockY > currentBlockY) {
                nextY = currentBlockY + 1;
                stepY = ((double)nextY - currentY) / diffY;
            } else if (endBlockY < currentBlockY) {
                nextY = currentBlockY;
                stepY = ((double)nextY - currentY) / diffY;
            }
            if (endBlockZ > currentBlockZ) {
                nextZ = currentBlockZ + 1;
                stepZ = ((double)nextZ - currentZ) / diffZ;
            } else if (endBlockZ < currentBlockZ) {
                nextZ = currentBlockZ;
                stepZ = ((double)nextZ - currentZ) / diffZ;
            }
            if (stepX < stepY && stepX < stepZ) {
                currentX = nextX;
                currentBlockX = nextX - (endBlockX - currentBlockX >>> 31);
                $this$fastFloor$iv7 = currentY += diffY * stepX;
                currentBlockY = (int)($this$fastFloor$iv7 + 1.073741824E9) - 0x40000000;
                $this$fastFloor$iv7 = currentZ += diffZ * stepX;
                currentBlockZ = (int)($this$fastFloor$iv7 + 1.073741824E9) - 0x40000000;
            } else if (stepY < stepZ) {
                currentY = nextY;
                $this$fastFloor$iv7 = currentX += diffX * stepY;
                currentBlockX = (int)($this$fastFloor$iv7 + 1.073741824E9) - 0x40000000;
                currentBlockY = nextY - (endBlockY - currentBlockY >>> 31);
                $this$fastFloor$iv7 = currentZ += diffZ * stepY;
                currentBlockZ = (int)($this$fastFloor$iv7 + 1.073741824E9) - 0x40000000;
            } else {
                currentZ = nextZ;
                $this$fastFloor$iv7 = currentX += diffX * stepZ;
                currentBlockX = (int)($this$fastFloor$iv7 + 1.073741824E9) - 0x40000000;
                $this$fastFloor$iv7 = currentY += diffY * stepZ;
                currentBlockY = (int)($this$fastFloor$iv7 + 1.073741824E9) - 0x40000000;
                currentBlockZ = nextZ - (endBlockZ - currentBlockZ >>> 31);
            }
            blockPos.setPos(currentBlockX, currentBlockY, currentBlockZ);
            IBlockState blockState = $this$rayTrace.getBlockState(blockPos);
            RayTraceAction action2 = function.invoke(blockPos, blockState);
            if (action2 == RayTraceAction.Null.INSTANCE) {
                return null;
            }
            if (action2 == RayTraceAction.Calc.INSTANCE) {
                RayTraceResult raytrace2 = RaytraceKt.raytrace(blockState, $this$rayTrace, blockPos, currentX, currentY, currentZ, endX, endY, endZ);
                if (raytrace2 == null) continue;
                return raytrace2;
            }
            if (!(action2 instanceof RayTraceAction.Result)) continue;
            return ((RayTraceAction.Result)action2).getRayTraceResult();
        }
        return null;
    }

    private static RayTraceResult raytrace(IBlockState $this$raytrace, World world, BlockPos.MutableBlockPos blockPos, double x1, double y1, double z1, double x2, double y2, double z2) {
        RayTraceResult rayTraceResult;
        boolean none;
        EnumFacing side;
        float hitVecZ;
        float hitVecY;
        float hitVecX;
        block26: {
            float newZ3;
            float newX4;
            float newY3;
            block29: {
                float $this$sq$iv$iv2;
                float factorMax;
                float zDiff;
                float yDiff;
                float xDiff;
                float maxZ;
                float maxY;
                float maxX;
                float minY;
                float minX;
                float z1f;
                float y1f;
                float x1f;
                block27: {
                    float newX3;
                    float newZ2;
                    float newY2;
                    block28: {
                        float $this$sq$iv$iv;
                        float factorMin;
                        float minZ;
                        block22: {
                            float newY4;
                            block25: {
                                block23: {
                                    float newZ;
                                    block24: {
                                        x1f = (float)(x1 - (double)blockPos.getX());
                                        y1f = (float)(y1 - (double)blockPos.getY());
                                        z1f = (float)(z1 - (double)blockPos.getZ());
                                        AxisAlignedBB box = $this$raytrace.getBoundingBox(world, blockPos);
                                        minX = (float)box.minX;
                                        minY = (float)box.minY;
                                        minZ = (float)box.minZ;
                                        maxX = (float)box.maxX;
                                        maxY = (float)box.maxY;
                                        maxZ = (float)box.maxZ;
                                        xDiff = (float)(x2 - (double)blockPos.getX()) - x1f;
                                        yDiff = (float)(y2 - (double)blockPos.getY()) - y1f;
                                        zDiff = (float)(z2 - (double)blockPos.getZ()) - z1f;
                                        hitVecX = Float.NaN;
                                        hitVecY = Float.NaN;
                                        hitVecZ = Float.NaN;
                                        side = EnumFacing.WEST;
                                        none = true;
                                        if (xDiff * xDiff >= 1.0E-7f) {
                                            factorMin = (minX - x1f) / xDiff;
                                            if (0.0 <= (double)factorMin && (double)factorMin <= 1.0) {
                                                float newY = y1f + yDiff * factorMin;
                                                newZ = z1f + zDiff * factorMin;
                                                if (minY <= newY && newY <= maxY && minZ <= newZ && newZ <= maxZ) {
                                                    hitVecX = x1f + xDiff * factorMin;
                                                    hitVecY = newY;
                                                    hitVecZ = newZ;
                                                    none = false;
                                                }
                                            } else {
                                                factorMax = (maxX - x1f) / xDiff;
                                                if (0.0 <= (double)factorMax && (double)factorMax <= 1.0) {
                                                    newY2 = y1f + yDiff * factorMax;
                                                    newZ2 = z1f + zDiff * factorMax;
                                                    if (minY <= newY2 && newY2 <= maxY && minZ <= newZ2 && newZ2 <= maxZ) {
                                                        hitVecX = x1f + xDiff * factorMax;
                                                        hitVecY = newY2;
                                                        hitVecZ = newZ2;
                                                        side = EnumFacing.EAST;
                                                        none = false;
                                                    }
                                                }
                                            }
                                        }
                                        if (!(yDiff * yDiff >= 1.0E-7f)) break block22;
                                        factorMin = (minY - y1f) / yDiff;
                                        if (!(0.0f <= factorMin) || !(factorMin <= 1.0f)) break block23;
                                        newX3 = x1f + xDiff * factorMin;
                                        newZ = z1f + zDiff * factorMin;
                                        if (!(minX <= newX3) || !(newX3 <= maxX) || !(minZ <= newZ) || !(newZ <= maxZ)) break block22;
                                        newY3 = y1f + yDiff * factorMin;
                                        if (none) break block24;
                                        $this$sq$iv$iv = newX3 - x1f;
                                        float n3 = $this$sq$iv$iv * $this$sq$iv$iv;
                                        $this$sq$iv$iv = newY3 - y1f;
                                        float n4 = n3 + $this$sq$iv$iv * $this$sq$iv$iv;
                                        $this$sq$iv$iv = newZ - z1f;
                                        float n5 = n4 + $this$sq$iv$iv * $this$sq$iv$iv;
                                        $this$sq$iv$iv = hitVecX - x1f;
                                        float n6 = $this$sq$iv$iv * $this$sq$iv$iv;
                                        $this$sq$iv$iv = hitVecY - y1f;
                                        float n7 = n6 + $this$sq$iv$iv * $this$sq$iv$iv;
                                        if (n5 >= n7 + ($this$sq$iv$iv = hitVecZ - z1f) * $this$sq$iv$iv) break block22;
                                    }
                                    hitVecX = newX3;
                                    hitVecY = newY3;
                                    hitVecZ = newZ;
                                    side = EnumFacing.DOWN;
                                    none = false;
                                    break block22;
                                }
                                factorMax = (maxY - y1f) / yDiff;
                                if (!(0.0f <= factorMax) || !(factorMax <= 1.0f)) break block22;
                                newX4 = x1f + xDiff * factorMax;
                                newZ2 = z1f + zDiff * factorMax;
                                if (!(minX <= newX4) || !(newX4 <= maxX) || !(minZ <= newZ2) || !(newZ2 <= maxZ)) break block22;
                                newY4 = y1f + yDiff * factorMax;
                                if (none) break block25;
                                $this$sq$iv$iv2 = newX4 - x1f;
                                float n8 = $this$sq$iv$iv2 * $this$sq$iv$iv2;
                                $this$sq$iv$iv2 = newY4 - y1f;
                                float n9 = n8 + $this$sq$iv$iv2 * $this$sq$iv$iv2;
                                $this$sq$iv$iv2 = newZ2 - z1f;
                                float n10 = n9 + $this$sq$iv$iv2 * $this$sq$iv$iv2;
                                $this$sq$iv$iv2 = hitVecX - x1f;
                                float n11 = $this$sq$iv$iv2 * $this$sq$iv$iv2;
                                $this$sq$iv$iv2 = hitVecY - y1f;
                                float n12 = n11 + $this$sq$iv$iv2 * $this$sq$iv$iv2;
                                if (n10 >= n12 + ($this$sq$iv$iv2 = hitVecZ - z1f) * $this$sq$iv$iv2) break block22;
                            }
                            hitVecX = newX4;
                            hitVecY = newY4;
                            hitVecZ = newZ2;
                            side = EnumFacing.UP;
                            none = false;
                        }
                        if (!((double)(zDiff * zDiff) >= 1.0E-7)) break block26;
                        factorMin = (minZ - z1f) / zDiff;
                        if (!(0.0f <= factorMin) || !(factorMin <= 1.0f)) break block27;
                        newX3 = x1f + xDiff * factorMin;
                        newY2 = y1f + yDiff * factorMin;
                        if (!(minX <= newX3) || !(newX3 <= maxX) || !(minY <= newY2) || !(newY2 <= maxY)) break block26;
                        newZ2 = z1f + zDiff * factorMin;
                        if (none) break block28;
                        $this$sq$iv$iv = newX3 - x1f;
                        float n13 = $this$sq$iv$iv * $this$sq$iv$iv;
                        $this$sq$iv$iv = newY2 - y1f;
                        float n14 = n13 + $this$sq$iv$iv * $this$sq$iv$iv;
                        $this$sq$iv$iv = newZ2 - z1f;
                        float n15 = n14 + $this$sq$iv$iv * $this$sq$iv$iv;
                        $this$sq$iv$iv = hitVecX - x1f;
                        float n16 = $this$sq$iv$iv * $this$sq$iv$iv;
                        $this$sq$iv$iv = hitVecY - y1f;
                        float n17 = n16 + $this$sq$iv$iv * $this$sq$iv$iv;
                        if (n15 >= n17 + ($this$sq$iv$iv = hitVecZ - z1f) * $this$sq$iv$iv) break block26;
                    }
                    hitVecX = newX3;
                    hitVecY = newY2;
                    hitVecZ = newZ2;
                    side = EnumFacing.NORTH;
                    none = false;
                    break block26;
                }
                factorMax = (maxZ - z1f) / zDiff;
                if (!(0.0f <= factorMax) || !(factorMax <= 1.0f)) break block26;
                newX4 = x1f + xDiff * factorMax;
                newY3 = y1f + yDiff * factorMax;
                if (!(minX <= newX4) || !(newX4 <= maxX) || !(minY <= newY3) || !(newY3 <= maxY)) break block26;
                newZ3 = z1f + zDiff * factorMax;
                if (none) break block29;
                $this$sq$iv$iv2 = newX4 - x1f;
                float n18 = $this$sq$iv$iv2 * $this$sq$iv$iv2;
                $this$sq$iv$iv2 = newY3 - y1f;
                float n19 = n18 + $this$sq$iv$iv2 * $this$sq$iv$iv2;
                $this$sq$iv$iv2 = newZ3 - z1f;
                float n20 = n19 + $this$sq$iv$iv2 * $this$sq$iv$iv2;
                $this$sq$iv$iv2 = hitVecX - x1f;
                float n21 = $this$sq$iv$iv2 * $this$sq$iv$iv2;
                $this$sq$iv$iv2 = hitVecY - y1f;
                float n22 = n21 + $this$sq$iv$iv2 * $this$sq$iv$iv2;
                if (n20 >= n22 + ($this$sq$iv$iv2 = hitVecZ - z1f) * $this$sq$iv$iv2) break block26;
            }
            hitVecX = newX4;
            hitVecY = newY3;
            hitVecZ = newZ3;
            side = EnumFacing.SOUTH;
            none = false;
        }
        if (!none) {
            Vec3d hitVec = new Vec3d((double)hitVecX + (double)blockPos.getX(), (double)hitVecY + (double)blockPos.getY(), (double)hitVecZ + (double)blockPos.getZ());
            rayTraceResult = new RayTraceResult(hitVec, side, blockPos.toImmutable());
        } else {
            rayTraceResult = null;
        }
        return rayTraceResult;
    }
}

