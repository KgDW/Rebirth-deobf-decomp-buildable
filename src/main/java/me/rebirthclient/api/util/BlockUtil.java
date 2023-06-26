package me.rebirthclient.api.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import me.rebirthclient.api.managers.impl.SneakManager;
import me.rebirthclient.api.util.CombatUtil;
import me.rebirthclient.api.util.EntityUtil;
import me.rebirthclient.api.util.Wrapper;
import me.rebirthclient.mod.modules.impl.combat.CombatSetting;
import me.rebirthclient.mod.modules.impl.render.PlaceRender;
import net.minecraft.block.Block;
import net.minecraft.block.BlockAir;
import net.minecraft.block.BlockCake;
import net.minecraft.block.BlockCarpet;
import net.minecraft.block.BlockDeadBush;
import net.minecraft.block.BlockFence;
import net.minecraft.block.BlockFenceGate;
import net.minecraft.block.BlockFire;
import net.minecraft.block.BlockLiquid;
import net.minecraft.block.BlockSlab;
import net.minecraft.block.BlockSnow;
import net.minecraft.block.BlockStairs;
import net.minecraft.block.BlockTallGrass;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityEnderCrystal;
import net.minecraft.entity.item.EntityExpBottle;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.item.EntityXPOrb;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.CPacketEntityAction;
import net.minecraft.network.play.client.CPacketPlayerTryUseItemOnBlock;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;

public class BlockUtil
implements Wrapper {
    public static final List<Block> canUseList = Arrays.asList(Blocks.ENDER_CHEST, Blocks.CHEST, Blocks.TRAPPED_CHEST, Blocks.CRAFTING_TABLE, Blocks.ANVIL, Blocks.BREWING_STAND, Blocks.HOPPER, Blocks.DROPPER, Blocks.DISPENSER, Blocks.TRAPDOOR, Blocks.ENCHANTING_TABLE);
    public static final List<Block> shulkerList = Arrays.asList(Blocks.WHITE_SHULKER_BOX, Blocks.ORANGE_SHULKER_BOX, Blocks.MAGENTA_SHULKER_BOX, Blocks.LIGHT_BLUE_SHULKER_BOX, Blocks.YELLOW_SHULKER_BOX, Blocks.LIME_SHULKER_BOX, Blocks.PINK_SHULKER_BOX, Blocks.GRAY_SHULKER_BOX, Blocks.SILVER_SHULKER_BOX, Blocks.CYAN_SHULKER_BOX, Blocks.PURPLE_SHULKER_BOX, Blocks.BLUE_SHULKER_BOX, Blocks.BROWN_SHULKER_BOX, Blocks.GREEN_SHULKER_BOX, Blocks.RED_SHULKER_BOX, Blocks.BLACK_SHULKER_BOX);

    public static boolean canBlockFacing(BlockPos pos) {
        boolean airCheck = false;
        for (EnumFacing side : EnumFacing.values()) {
            if (!BlockUtil.canClick(pos.offset(side))) continue;
            airCheck = true;
        }
        return airCheck;
    }

    public static boolean canPlaceEnum(BlockPos pos) {
        if (!BlockUtil.canBlockFacing(pos)) {
            return false;
        }
        return BlockUtil.strictPlaceCheck(pos);
    }

    public static boolean posHasCrystal(BlockPos pos) {
        for (Entity entity : BlockUtil.mc.world.getEntitiesWithinAABB(Entity.class, new AxisAlignedBB(pos))) {
            if (!(entity instanceof EntityEnderCrystal) || !new BlockPos(entity.posX, entity.posY, entity.posZ).equals(pos)) continue;
            return true;
        }
        return false;
    }

    public static boolean strictPlaceCheck(BlockPos pos) {
        if (!CombatSetting.INSTANCE.strictPlace.getValue()) {
            return true;
        }
        for (EnumFacing side : BlockUtil.getPlacableFacings(pos, true, CombatSetting.INSTANCE.checkRaytrace.getValue())) {
            if (!BlockUtil.canClick(pos.offset(side))) continue;
            return true;
        }
        return false;
    }

    public static List<EnumFacing> getPlacableFacings(BlockPos pos, boolean strictDirection, boolean rayTrace) {
        ArrayList<EnumFacing> validFacings = new ArrayList<>();
        for (EnumFacing side : EnumFacing.values()) {
            if (BlockUtil.getRaytrace(pos, side)) continue;
            BlockUtil.getPlaceFacing(pos, strictDirection, validFacings, side);
        }
        for (EnumFacing side : EnumFacing.values()) {
            if (rayTrace && BlockUtil.getRaytrace(pos, side)) continue;
            BlockUtil.getPlaceFacing(pos, strictDirection, validFacings, side);
        }
        return validFacings;
    }

    private static boolean getRaytrace(BlockPos pos, EnumFacing side) {
        Vec3d testVec = new Vec3d(pos).add(0.5, 0.5, 0.5).add(new Vec3d(side.getDirectionVec()).scale(0.5));
        RayTraceResult result = BlockUtil.mc.world.rayTraceBlocks(BlockUtil.mc.player.getPositionEyes(1.0f), testVec);
        return result != null && result.typeOfHit != RayTraceResult.Type.MISS;
    }

    private static void getPlaceFacing(BlockPos pos, boolean strictDirection, ArrayList<EnumFacing> validFacings, EnumFacing side) {
        IBlockState blockState;
        BlockPos neighbour = pos.offset(side);
        if (strictDirection) {
            Vec3d eyePos = BlockUtil.mc.player.getPositionEyes(1.0f);
            Vec3d blockCenter = new Vec3d((double)neighbour.getX() + 0.5, (double)neighbour.getY() + 0.5, (double)neighbour.getZ() + 0.5);
            IBlockState blockState2 = BlockUtil.mc.world.getBlockState(neighbour);
            boolean isFullBox = blockState2.getBlock() == Blocks.AIR || blockState2.isFullBlock();
            ArrayList<EnumFacing> validAxis = new ArrayList<>();
            validAxis.addAll(BlockUtil.checkAxis(eyePos.x - blockCenter.x, EnumFacing.WEST, EnumFacing.EAST, !isFullBox));
            validAxis.addAll(BlockUtil.checkAxis(eyePos.y - blockCenter.y, EnumFacing.DOWN, EnumFacing.UP, true));
            validAxis.addAll(BlockUtil.checkAxis(eyePos.z - blockCenter.z, EnumFacing.NORTH, EnumFacing.SOUTH, !isFullBox));
            if (!validAxis.contains(side.getOpposite())) {
                return;
            }
        }
        if (!(blockState = BlockUtil.mc.world.getBlockState(neighbour)).getBlock().canCollideCheck(blockState, false) || blockState.getMaterial().isReplaceable()) {
            return;
        }
        validFacings.add(side);
    }

    public static ArrayList<EnumFacing> checkAxis(double diff, EnumFacing negativeSide, EnumFacing positiveSide, boolean bothIfInRange) {
        ArrayList<EnumFacing> valid = new ArrayList<>();
        if (diff < -0.5) {
            valid.add(negativeSide);
        }
        if (diff > 0.5) {
            valid.add(positiveSide);
        }
        if (bothIfInRange) {
            if (!valid.contains(negativeSide)) {
                valid.add(negativeSide);
            }
            if (!valid.contains(positiveSide)) {
                valid.add(positiveSide);
            }
        }
        return valid;
    }

    public static boolean isAir(BlockPos pos) {
        return BlockUtil.mc.world.isAirBlock(pos);
    }

    public static void placeBlock(BlockPos pos, boolean packet) {
        BlockUtil.placeBlock(pos, EnumHand.MAIN_HAND, false, packet);
    }

    public static List<BlockPos> getSphere(BlockPos pos, float r, int h, boolean hollow, boolean sphere, int plus_y) {
        ArrayList<BlockPos> circleblocks = new ArrayList<>();
        int cx = pos.getX();
        int cy = pos.getY();
        int cz = pos.getZ();
        int x = cx - (int)r;
        while ((float)x <= (float)cx + r) {
            int z = cz - (int)r;
            while ((float)z <= (float)cz + r) {
                int y = sphere ? cy - (int)r : cy;
                while (true) {
                    float f2;
                    float f = y;
                    float f3 = f2 = sphere ? (float)cy + r : (float)(cy + h);
                    if (!(f < f2)) break;
                    double dist = (cx - x) * (cx - x) + (cz - z) * (cz - z) + (sphere ? (cy - y) * (cy - y) : 0);
                    if (dist < (double)(r * r) && (!hollow || dist >= (double)((r - 1.0f) * (r - 1.0f)))) {
                        BlockPos l = new BlockPos(x, y + plus_y, z);
                        circleblocks.add(l);
                    }
                    ++y;
                }
                ++z;
            }
            ++x;
        }
        return circleblocks;
    }

    public static boolean rayTracePlaceCheck(BlockPos pos, boolean shouldCheck, float height) {
        return shouldCheck && BlockUtil.mc.world.rayTraceBlocks(new Vec3d(BlockUtil.mc.player.posX, BlockUtil.mc.player.posY + (double)BlockUtil.mc.player.getEyeHeight(), BlockUtil.mc.player.posZ), new Vec3d(pos.getX(), (float)pos.getY() + height, pos.getZ()), false, true, false) != null;
    }

    public static double distanceToXZ(double x, double z) {
        double dx = BlockUtil.mc.player.posX - x;
        double dz = BlockUtil.mc.player.posZ - z;
        return Math.sqrt(dx * dx + dz * dz);
    }

    public static boolean canClick(BlockPos pos) {
        return BlockUtil.mc.world.getBlockState(pos).getBlock().canCollideCheck(BlockUtil.mc.world.getBlockState(pos), false);
    }

    public static EnumFacing getRayTraceFacing(BlockPos pos) {
        RayTraceResult result = BlockUtil.mc.world.rayTraceBlocks(new Vec3d(BlockUtil.mc.player.posX, BlockUtil.mc.player.posY + (double)BlockUtil.mc.player.getEyeHeight(), BlockUtil.mc.player.posZ), new Vec3d((double)pos.getX() + 0.5, (double)pos.getY() + 0.5, (double)pos.getZ() + 0.5));
        if (result != null && result.sideHit != null) {
            return result.sideHit;
        }
        EnumFacing bestFacing = null;
        double distance = 0.0;
        for (EnumFacing side : EnumFacing.values()) {
            if (bestFacing != null && !(BlockUtil.mc.player.getDistanceSq(pos.offset(side)) < distance)) continue;
            bestFacing = side;
            distance = BlockUtil.mc.player.getDistanceSq(pos.offset(side));
        }
        if (bestFacing == null) {
            return EnumFacing.UP;
        }
        return bestFacing;
    }

    public static boolean canPlace(BlockPos pos, double distance) {
        if (BlockUtil.mc.player.getDistance((double)pos.getX() + 0.5, (double)pos.getY() + 0.5, (double)pos.getZ() + 0.5) > distance) {
            return false;
        }
        if (!BlockUtil.canBlockFacing(pos)) {
            return false;
        }
        if (!BlockUtil.canReplace(pos)) {
            return false;
        }
        if (!BlockUtil.strictPlaceCheck(pos)) {
            return false;
        }
        return !BlockUtil.checkEntity(pos);
    }

    public static boolean canPlace(BlockPos pos) {
        if (BlockUtil.mc.player.getDistance((double)pos.getX() + 0.5, (double)pos.getY() + 0.5, (double)pos.getZ() + 0.5) > 6.0) {
            return false;
        }
        if (!BlockUtil.canBlockFacing(pos)) {
            return false;
        }
        if (!BlockUtil.canReplace(pos)) {
            return false;
        }
        if (!BlockUtil.strictPlaceCheck(pos)) {
            return false;
        }
        return !BlockUtil.checkEntity(pos);
    }

    public static boolean canPlace2(BlockPos pos) {
        if (BlockUtil.mc.player.getDistance((double)pos.getX() + 0.5, (double)pos.getY() + 0.5, (double)pos.getZ() + 0.5) > 6.0) {
            return false;
        }
        if (!BlockUtil.canReplace(pos)) {
            return false;
        }
        return !BlockUtil.checkPlayer(pos);
    }

    public static boolean canPlace2(BlockPos pos, double distance) {
        if (BlockUtil.mc.player.getDistance((double)pos.getX() + 0.5, (double)pos.getY() + 0.5, (double)pos.getZ() + 0.5) > distance) {
            return false;
        }
        if (!BlockUtil.canReplace(pos)) {
            return false;
        }
        return !BlockUtil.checkPlayer(pos);
    }

    public static boolean canPlace3(BlockPos pos) {
        if (BlockUtil.mc.player.getDistance((double)pos.getX() + 0.5, (double)pos.getY() + 0.5, (double)pos.getZ() + 0.5) > 6.0) {
            return false;
        }
        if (!BlockUtil.canBlockFacing(pos)) {
            return false;
        }
        if (!BlockUtil.canReplace(pos)) {
            return false;
        }
        if (!BlockUtil.strictPlaceCheck(pos)) {
            return false;
        }
        return !BlockUtil.checkPlayer(pos);
    }

    public static boolean canPlace4(BlockPos pos) {
        if (BlockUtil.mc.player.getDistance((double)pos.getX() + 0.5, (double)pos.getY() + 0.5, (double)pos.getZ() + 0.5) > 6.0) {
            return false;
        }
        if (!BlockUtil.canBlockFacing(pos)) {
            return false;
        }
        if (!BlockUtil.strictPlaceCheck(pos)) {
            return false;
        }
        return !BlockUtil.checkEntity(pos);
    }

    public static boolean canPlaceShulker(BlockPos pos) {
        if (BlockUtil.mc.player.getDistance((double)pos.getX() + 0.5, (double)pos.getY() + 0.5, (double)pos.getZ() + 0.5) > 6.0) {
            return false;
        }
        if (BlockUtil.canBlockReplace(pos.down())) {
            return false;
        }
        if (!BlockUtil.canReplace(pos)) {
            return false;
        }
        if (BlockUtil.checkEntity(pos)) {
            return false;
        }
        if (!CombatSetting.INSTANCE.strictPlace.getValue()) {
            return true;
        }
        for (EnumFacing side : BlockUtil.getPlacableFacings(pos, true, CombatSetting.INSTANCE.checkRaytrace.getValue())) {
            if (!BlockUtil.canClick(pos.offset(side)) || side != EnumFacing.DOWN) continue;
            return true;
        }
        return false;
    }

    public static boolean canBlockReplace(BlockPos pos) {
        return BlockUtil.mc.world.isAirBlock(pos) || BlockUtil.getBlock(pos) == Blocks.FIRE || BlockUtil.getBlock(pos) == Blocks.LAVA || BlockUtil.getBlock(pos) == Blocks.FLOWING_LAVA || BlockUtil.getBlock(pos) == Blocks.WATER || BlockUtil.getBlock(pos) == Blocks.FLOWING_WATER;
    }

    public static boolean checkPlayer(BlockPos pos) {
        for (Entity entity : BlockUtil.mc.world.getEntitiesWithinAABB(Entity.class, new AxisAlignedBB(pos))) {
            if (entity.isDead || entity instanceof EntityItem || entity instanceof EntityXPOrb || entity instanceof EntityExpBottle || entity instanceof EntityArrow || entity instanceof EntityEnderCrystal) continue;
            return true;
        }
        return false;
    }

    public static boolean checkEntity(BlockPos pos) {
        for (Entity entity : BlockUtil.mc.world.getEntitiesWithinAABB(Entity.class, new AxisAlignedBB(pos))) {
            if (entity.isDead || entity instanceof EntityItem || entity instanceof EntityXPOrb || entity instanceof EntityExpBottle || entity instanceof EntityArrow) continue;
            return true;
        }
        return false;
    }

    public static NonNullList<BlockPos> getBox(float range) {
        NonNullList positions = NonNullList.create();
        positions.addAll(BlockUtil.getSphere(new BlockPos(Math.floor(BlockUtil.mc.player.posX), Math.floor(BlockUtil.mc.player.posY), Math.floor(BlockUtil.mc.player.posZ)), range, 0, false, true, 0));
        return positions;
    }

    public static NonNullList<BlockPos> getBox(float range, BlockPos pos) {
        NonNullList positions = NonNullList.create();
        positions.addAll(BlockUtil.getSphere(pos, range, 0, false, true, 0));
        return positions;
    }

    public static BlockPos vec3toBlockPos(Vec3d vec3d) {
        return new BlockPos(Math.floor(vec3d.x), (double)Math.round(vec3d.y), Math.floor(vec3d.z));
    }

    public static Vec3d blockPosToVec3(BlockPos pos) {
        return new Vec3d(pos.getX(), pos.getY(), pos.getZ());
    }

    public static BlockPos getFlooredPosition(Entity entity) {
        return new BlockPos(Math.floor(entity.posX), (double)Math.round(entity.posY), Math.floor(entity.posZ));
    }

    public static IBlockState getState(BlockPos pos) {
        return BlockUtil.mc.world.getBlockState(pos);
    }

    public static Block getBlock(BlockPos pos) {
        return BlockUtil.getState(pos).getBlock();
    }

    public static void placeCrystal(BlockPos pos, boolean rotate) {
        boolean offhand = BlockUtil.mc.player.getHeldItemOffhand().getItem() == Items.END_CRYSTAL;
        BlockPos obsPos = pos.down();
        RayTraceResult result = BlockUtil.mc.world.rayTraceBlocks(new Vec3d(BlockUtil.mc.player.posX, BlockUtil.mc.player.posY + (double)BlockUtil.mc.player.getEyeHeight(), BlockUtil.mc.player.posZ), new Vec3d((double)pos.getX() + 0.5, (double)pos.getY() - 0.5, (double)pos.getZ() + 0.5));
        EnumFacing facing = result == null || result.sideHit == null ? EnumFacing.UP : result.sideHit;
        EnumFacing opposite = facing.getOpposite();
        Vec3d vec = new Vec3d(obsPos).add(0.5, 0.5, 0.5).add(new Vec3d(opposite.getDirectionVec()));
        if (rotate) {
            EntityUtil.faceVector(vec);
        }
        BlockUtil.mc.player.connection.sendPacket(new CPacketPlayerTryUseItemOnBlock(obsPos, facing, offhand ? EnumHand.OFF_HAND : EnumHand.MAIN_HAND, 0.0f, 0.0f, 0.0f));
        BlockUtil.mc.player.swingArm(offhand ? EnumHand.OFF_HAND : EnumHand.MAIN_HAND);
    }

    public static void placeBlock(BlockPos pos, EnumHand hand, boolean rotate, boolean packet, boolean attackEntity, boolean eatingPause) {
        if (attackEntity) {
            CombatUtil.attackCrystal(pos, rotate, eatingPause);
        }
        BlockUtil.placeBlock(pos, hand, rotate, packet);
    }

    public static void placeBlock(BlockPos pos, EnumHand hand, boolean rotate, boolean packet) {
        EnumFacing side = BlockUtil.getFirstFacing(pos);
        if (side == null) {
            return;
        }
        BlockPos neighbour = pos.offset(side);
        EnumFacing opposite = side.getOpposite();
        Vec3d hitVec = new Vec3d(neighbour).add(0.5, 0.5, 0.5).add(new Vec3d(opposite.getDirectionVec()).scale(0.5));
        Block neighbourBlock = BlockUtil.mc.world.getBlockState(neighbour).getBlock();
        boolean sneaking = false;
        if (!SneakManager.isSneaking && (canUseList.contains(neighbourBlock) || shulkerList.contains(neighbourBlock))) {
            BlockUtil.mc.player.connection.sendPacket(new CPacketEntityAction(BlockUtil.mc.player, CPacketEntityAction.Action.START_SNEAKING));
            sneaking = true;
        }
        if (rotate) {
            EntityUtil.faceVector(hitVec);
        }
        PlaceRender.PlaceMap.put(pos, new PlaceRender.placePosition(pos));
        BlockUtil.rightClickBlock(neighbour, hitVec, hand, opposite, packet);
        if (sneaking) {
            BlockUtil.mc.player.connection.sendPacket(new CPacketEntityAction(BlockUtil.mc.player, CPacketEntityAction.Action.STOP_SNEAKING));
        }
    }

    public static void rightClickBlock(BlockPos pos, Vec3d vec, EnumHand hand, EnumFacing direction, boolean packet) {
        if (packet) {
            float f = (float)(vec.x - (double)pos.getX());
            float f2 = (float)(vec.y - (double)pos.getY());
            float f3 = (float)(vec.z - (double)pos.getZ());
            BlockUtil.mc.player.connection.sendPacket(new CPacketPlayerTryUseItemOnBlock(pos, direction, hand, f, f2, f3));
        } else {
            BlockUtil.mc.playerController.processRightClickBlock(BlockUtil.mc.player, BlockUtil.mc.world, pos, direction, vec, hand);
        }
        BlockUtil.mc.player.swingArm(hand);
        BlockUtil.mc.rightClickDelayTimer = 4;
    }

    public static EnumFacing getFirstFacing(BlockPos pos) {
        if (!CombatSetting.INSTANCE.strictPlace.getValue()) {
            Iterator<EnumFacing> iterator = BlockUtil.getPossibleSides(pos).iterator();
            if (iterator.hasNext()) {
                return iterator.next();
            }
        } else {
            for (EnumFacing side : BlockUtil.getPlacableFacings(pos, true, CombatSetting.INSTANCE.checkRaytrace.getValue())) {
                if (!BlockUtil.canClick(pos.offset(side))) continue;
                return side;
            }
        }
        return null;
    }

    public static BlockPos[] getHorizontalOffsets(BlockPos pos) {
        return new BlockPos[]{pos.north(), pos.south(), pos.east(), pos.west(), pos.down()};
    }

    public static int getPlaceAbility(BlockPos pos, boolean raytrace) {
        return BlockUtil.getPlaceAbility(pos, raytrace, true);
    }

    public static int getPlaceAbility(BlockPos pos, boolean raytrace, boolean checkForEntities) {
        Block block = BlockUtil.getBlock(pos);
        if (!(block instanceof BlockAir || block instanceof BlockLiquid || block instanceof BlockTallGrass || block instanceof BlockFire || block instanceof BlockDeadBush || block instanceof BlockSnow)) {
            return 0;
        }
        if (raytrace && !BlockUtil.raytraceCheck(pos, 0.0f)) {
            return -1;
        }
        if (checkForEntities && BlockUtil.checkForEntities(pos)) {
            return 1;
        }
        for (EnumFacing side : BlockUtil.getPossibleSides(pos)) {
            if (!BlockUtil.canBeClicked(pos.offset(side))) continue;
            return 3;
        }
        return 2;
    }

    public static List<EnumFacing> getPossibleSides(BlockPos pos) {
        ArrayList<EnumFacing> facings = new ArrayList<>();
        for (EnumFacing side : EnumFacing.values()) {
            BlockPos neighbor = pos.offset(side);
            if (!BlockUtil.getBlock(neighbor).canCollideCheck(BlockUtil.getState(neighbor), false) || BlockUtil.canReplace(neighbor)) continue;
            facings.add(side);
        }
        return facings;
    }

    public static boolean canReplace(BlockPos pos) {
        return BlockUtil.getState(pos).getMaterial().isReplaceable();
    }

    public static boolean canPlaceCrystal(BlockPos pos) {
        BlockPos obsPos = pos.down();
        BlockPos boost = obsPos.up();
        BlockPos boost2 = obsPos.up(2);
        return (BlockUtil.getBlock(obsPos) == Blocks.BEDROCK || BlockUtil.getBlock(obsPos) == Blocks.OBSIDIAN) && BlockUtil.getBlock(boost) == Blocks.AIR && BlockUtil.getBlock(boost2) == Blocks.AIR && BlockUtil.mc.world.getEntitiesWithinAABB(Entity.class, new AxisAlignedBB(boost)).isEmpty() && BlockUtil.mc.world.getEntitiesWithinAABB(Entity.class, new AxisAlignedBB(boost2)).isEmpty();
    }

    public static boolean canPlaceCrystal(BlockPos pos, double distance) {
        if (BlockUtil.mc.player.getDistance((double)pos.getX() + 0.5, (double)pos.getY() + 0.5, (double)pos.getZ() + 0.5) > distance) {
            return false;
        }
        BlockPos obsPos = pos.down();
        BlockPos boost = obsPos.up();
        BlockPos boost2 = obsPos.up(2);
        return (BlockUtil.getBlock(obsPos) == Blocks.BEDROCK || BlockUtil.getBlock(obsPos) == Blocks.OBSIDIAN) && BlockUtil.getBlock(boost) == Blocks.AIR && BlockUtil.getBlock(boost2) == Blocks.AIR && BlockUtil.mc.world.getEntitiesWithinAABB(Entity.class, new AxisAlignedBB(boost)).isEmpty() && BlockUtil.mc.world.getEntitiesWithinAABB(Entity.class, new AxisAlignedBB(boost2)).isEmpty();
    }

    public static boolean canBeClicked(BlockPos pos) {
        return BlockUtil.getBlock(pos).canCollideCheck(BlockUtil.getState(pos), false);
    }

    public static boolean checkForEntities(BlockPos blockPos) {
        for (Entity entity : BlockUtil.mc.world.loadedEntityList) {
            if (entity instanceof EntityItem || entity instanceof EntityEnderCrystal || entity instanceof EntityXPOrb || entity instanceof EntityExpBottle || entity instanceof EntityArrow || !new AxisAlignedBB(blockPos).intersects(entity.getEntityBoundingBox())) continue;
            return true;
        }
        return false;
    }

    public static boolean raytraceCheck(BlockPos pos, float height) {
        return BlockUtil.mc.world.rayTraceBlocks(new Vec3d(BlockUtil.mc.player.posX, BlockUtil.mc.player.posY + (double)BlockUtil.mc.player.getEyeHeight(), BlockUtil.mc.player.posZ), new Vec3d(pos.getX(), (float)pos.getY() + height, pos.getZ()), false, true, false) == null;
    }

    public static EnumFacing getBestNeighboring(BlockPos pos, EnumFacing facing) {
        for (EnumFacing i : EnumFacing.VALUES) {
            if (facing != null && pos.offset(i).equals(pos.offset(facing, -1)) || i == EnumFacing.DOWN) continue;
            for (EnumFacing side : BlockUtil.getPlacableFacings(pos.offset(i), true, true)) {
                if (!BlockUtil.canClick(pos.offset(i).offset(side))) continue;
                return i;
            }
        }
        EnumFacing bestFacing = null;
        double distance = 0.0;
        for (EnumFacing i : EnumFacing.VALUES) {
            if (facing != null && pos.offset(i).equals(pos.offset(facing, -1)) || i == EnumFacing.DOWN) continue;
            for (EnumFacing side : BlockUtil.getPlacableFacings(pos.offset(i), true, false)) {
                if (!BlockUtil.canClick(pos.offset(i).offset(side)) || bestFacing != null && !(BlockUtil.mc.player.getDistanceSq(pos.offset(i)) < distance)) continue;
                bestFacing = i;
                distance = BlockUtil.mc.player.getDistanceSq(pos.offset(i));
            }
        }
        return null;
    }

    public static boolean isHole(BlockPos posIn) {
        for (BlockPos pos : BlockUtil.getHorizontalOffsets(posIn)) {
            if (BlockUtil.getBlock(pos) != Blocks.AIR && (BlockUtil.getBlock(pos) == Blocks.BEDROCK || BlockUtil.getBlock(pos) == Blocks.OBSIDIAN || BlockUtil.getBlock(pos) == Blocks.ENDER_CHEST)) continue;
            return false;
        }
        return true;
    }

    public static boolean isSafe(Block block) {
        List<Block> safeBlocks = Arrays.asList(Blocks.OBSIDIAN, Blocks.BEDROCK, Blocks.ENDER_CHEST, Blocks.ANVIL);
        return !safeBlocks.contains(block);
    }

    public static boolean isSlab(Block block) {
        return block instanceof BlockSlab || block instanceof BlockCarpet || block instanceof BlockCake;
    }

    public static boolean isStair(Block block) {
        return block instanceof BlockStairs;
    }

    public static boolean isFence(Block block) {
        return block instanceof BlockFence || block instanceof BlockFenceGate;
    }
}

