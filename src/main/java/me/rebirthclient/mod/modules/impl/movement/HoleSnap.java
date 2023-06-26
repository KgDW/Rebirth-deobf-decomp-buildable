package me.rebirthclient.mod.modules.impl.movement;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import me.rebirthclient.api.events.impl.PacketEvent;
import me.rebirthclient.mod.modules.Category;
import me.rebirthclient.mod.modules.Module;
import me.rebirthclient.mod.modules.impl.exploit.Blink;
import me.rebirthclient.mod.modules.settings.Setting;
import net.minecraft.block.Block;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.MobEffects;
import net.minecraft.network.play.server.SPacketPlayerPosLook;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.MovementInput;
import net.minecraft.util.MovementInputFromOptions;
import net.minecraft.util.Timer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec2f;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.World;
import net.minecraftforge.client.event.InputUpdateEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class HoleSnap
extends Module {
    public static final List<BlockPos> holeBlocks = Arrays.asList(new BlockPos(0, -1, 0), new BlockPos(0, 0, -1), new BlockPos(-1, 0, 0), new BlockPos(1, 0, 0), new BlockPos(0, 0, 1));
    private static final BlockPos[] surroundOffset = new BlockPos[]{new BlockPos(0, -1, 0), new BlockPos(0, 0, -1), new BlockPos(1, 0, 0), new BlockPos(0, 0, 1), new BlockPos(-1, 0, 0)};
    public static HoleSnap INSTANCE;
    private final Setting<Boolean> blink = this.add(new Setting<>("Blink", false));
    private final Setting<Integer> range = this.add(new Setting<>("Range", 5, 1, 50));
    private final Setting<Float> timer = this.add(new Setting<>("Timer", 1.6f, 1.0f, 8.0f));
    private final Setting<Integer> timeoutTicks = this.add(new Setting<>("TimeOutTicks", 10, 0, 30));
    boolean resetMove = false;
    private BlockPos holePos;
    private int stuckTicks;
    private int enabledTicks;

    public HoleSnap() {
        super("HoleSnap", "IQ", Category.MOVEMENT);
        INSTANCE = this;
    }

    public static boolean is2HoleB(BlockPos pos) {
        return HoleSnap.is2Hole(pos) != null;
    }

    public static BlockPos is2Hole(BlockPos pos) {
        if (HoleSnap.isHole(pos)) {
            return null;
        }
        BlockPos blockpos2 = null;
        int size = 0;
        int size2 = 0;
        if (HoleSnap.mc.world.getBlockState(pos).getBlock() != Blocks.AIR) {
            return null;
        }
        for (BlockPos bPos : holeBlocks) {
            if (HoleSnap.mc.world.getBlockState(pos.add(bPos)).getBlock() != Blocks.AIR || pos.add(bPos).equals(new BlockPos(bPos.getX(), bPos.getY() - 1, bPos.getZ()))) continue;
            blockpos2 = pos.add(bPos);
            ++size;
        }
        if (size == 1) {
            for (BlockPos bPoss : holeBlocks) {
                if (HoleSnap.mc.world.getBlockState(pos.add(bPoss)).getBlock() != Blocks.BEDROCK && HoleSnap.mc.world.getBlockState(pos.add(bPoss)).getBlock() != Blocks.OBSIDIAN) continue;
                ++size2;
            }
            for (BlockPos bPoss : holeBlocks) {
                if (HoleSnap.mc.world.getBlockState(blockpos2.add(bPoss)).getBlock() != Blocks.BEDROCK && HoleSnap.mc.world.getBlockState(blockpos2.add(bPoss)).getBlock() != Blocks.OBSIDIAN) continue;
                ++size2;
            }
        }
        if (size2 == 8) {
            return blockpos2;
        }
        return null;
    }

    public static boolean isHole(BlockPos blockPos) {
        return !(HoleSnap.getBlockResistance(blockPos.add(0, 1, 0)) != BlockResistance.Blank || HoleSnap.getBlockResistance(blockPos.add(0, 0, 0)) != BlockResistance.Blank || HoleSnap.getBlockResistance(blockPos.add(0, 2, 0)) != BlockResistance.Blank || HoleSnap.getBlockResistance(blockPos.add(0, 0, -1)) != BlockResistance.Resistant && HoleSnap.getBlockResistance(blockPos.add(0, 0, -1)) != BlockResistance.Unbreakable || HoleSnap.getBlockResistance(blockPos.add(1, 0, 0)) != BlockResistance.Resistant && HoleSnap.getBlockResistance(blockPos.add(1, 0, 0)) != BlockResistance.Unbreakable || HoleSnap.getBlockResistance(blockPos.add(-1, 0, 0)) != BlockResistance.Resistant && HoleSnap.getBlockResistance(blockPos.add(-1, 0, 0)) != BlockResistance.Unbreakable || HoleSnap.getBlockResistance(blockPos.add(0, 0, 1)) != BlockResistance.Resistant && HoleSnap.getBlockResistance(blockPos.add(0, 0, 1)) != BlockResistance.Unbreakable || HoleSnap.getBlockResistance(blockPos.add(0.5, 0.5, 0.5)) != BlockResistance.Blank || HoleSnap.getBlockResistance(blockPos.add(0, -1, 0)) != BlockResistance.Resistant && HoleSnap.getBlockResistance(blockPos.add(0, -1, 0)) != BlockResistance.Unbreakable);
    }

    public static BlockResistance getBlockResistance(BlockPos block) {
        if (HoleSnap.mc.world.isAirBlock(block)) {
            return BlockResistance.Blank;
        }
        if (!(HoleSnap.mc.world.getBlockState(block).getBlock().getBlockHardness(HoleSnap.mc.world.getBlockState(block), HoleSnap.mc.world, block) == -1.0f || HoleSnap.mc.world.getBlockState(block).getBlock().equals(Blocks.OBSIDIAN) || HoleSnap.mc.world.getBlockState(block).getBlock().equals(Blocks.ANVIL) || HoleSnap.mc.world.getBlockState(block).getBlock().equals(Blocks.ENCHANTING_TABLE) || HoleSnap.mc.world.getBlockState(block).getBlock().equals(Blocks.ENDER_CHEST))) {
            return BlockResistance.Breakable;
        }
        if (HoleSnap.mc.world.getBlockState(block).getBlock().equals(Blocks.OBSIDIAN) || HoleSnap.mc.world.getBlockState(block).getBlock().equals(Blocks.ANVIL) || HoleSnap.mc.world.getBlockState(block).getBlock().equals(Blocks.ENCHANTING_TABLE) || HoleSnap.mc.world.getBlockState(block).getBlock().equals(Blocks.ENDER_CHEST)) {
            return BlockResistance.Resistant;
        }
        if (HoleSnap.mc.world.getBlockState(block).getBlock().equals(Blocks.BEDROCK)) {
            return BlockResistance.Unbreakable;
        }
        return null;
    }

    @Override
    public void onEnable() {
        if (this.blink.getValue() && Blink.INSTANCE.isOff()) {
            Blink.INSTANCE.enable();
        }
        this.resetMove = false;
    }

    @Override
    public void onDisable() {
        if (this.blink.getValue() && Blink.INSTANCE.isOn()) {
            Blink.INSTANCE.disable();
        }
        if (this.resetMove) {
            HoleSnap.mc.player.motionX = 0.0;
            HoleSnap.mc.player.motionZ = 0.0;
        }
        this.holePos = null;
        this.stuckTicks = 0;
        this.enabledTicks = 0;
        HoleSnap.mc.timer.tickLength = 50.0f;
    }

    public double getSpeed(Entity entity) {
        return Math.hypot(entity.motionX, entity.motionZ);
    }

    private boolean isFlying(EntityPlayer player) {
        return player.isElytraFlying() || player.capabilities.isFlying;
    }

    @SubscribeEvent
    public void onReceivePacket(PacketEvent.Receive event) {
        if (event.getPacket() instanceof SPacketPlayerPosLook) {
            this.disable();
        }
    }

    @SubscribeEvent
    public void onInput(InputUpdateEvent event) {
        if (event.getMovementInput() instanceof MovementInputFromOptions && this.holePos != null) {
            MovementInput movementInput = event.getMovementInput();
            this.resetMove(movementInput);
        }
    }

    private void resetMove(MovementInput movementInput) {
        movementInput.moveForward = 0.0f;
        movementInput.moveStrafe = 0.0f;
        movementInput.forwardKeyDown = false;
        movementInput.backKeyDown = false;
        movementInput.leftKeyDown = false;
        movementInput.rightKeyDown = false;
    }

    private boolean isCentered(Entity entity, BlockPos pos) {
        double d = (double)pos.getX() + 0.31;
        double d2 = (double)pos.getX() + 0.69;
        double d3 = entity.posX;
        if (d > d3) {
            return false;
        }
        if (d3 > d2) {
            return false;
        }
        d = (double)pos.getZ() + 0.31;
        d2 = (double)pos.getZ() + 0.69;
        d3 = entity.posZ;
        return d <= d3 && d3 <= d2;
    }

    @Override
    public void onTick() {
        ++this.enabledTicks;
        if (this.enabledTicks > this.timeoutTicks.getValue() - 1) {
            this.disable();
            return;
        }
        if (HoleSnap.mc.player.isEntityAlive()) {
            if (!this.isFlying(HoleSnap.mc.player)) {
                EntityPlayerSP entityPlayerSP = HoleSnap.mc.player;
                double currentSpeed = this.getSpeed(entityPlayerSP);
                if (this.shouldDisable(currentSpeed)) {
                    this.disable();
                    return;
                }
                BlockPos blockPos = this.getHole();
                if (blockPos != null) {
                    if (HoleSnap.mc.player.posY - (double)blockPos.getY() < 0.5 && HoleSnap.mc.player.posX - (double)blockPos.getX() <= 0.65 && HoleSnap.mc.player.posX - (double)blockPos.getX() >= 0.35 && HoleSnap.mc.player.posZ - (double)blockPos.getZ() <= 0.65 && HoleSnap.mc.player.posZ - (double)blockPos.getZ() >= 0.35) {
                        this.disable();
                        return;
                    }
                    if (HoleSnap.mc.player.posX - (double)blockPos.getX() <= 0.65 && HoleSnap.mc.player.posX - (double)blockPos.getX() >= 0.35 && HoleSnap.mc.player.posZ - (double)blockPos.getZ() <= 0.65 && HoleSnap.mc.player.posZ - (double)blockPos.getZ() >= 0.35) {
                        HoleSnap.mc.player.motionX = 0.0;
                        HoleSnap.mc.player.motionZ = 0.0;
                        return;
                    }
                    this.resetMove = true;
                    Timer timer = HoleSnap.mc.timer;
                    Float f = this.timer.getValue();
                    timer.tickLength = 50.0f / f;
                    if (!this.isCentered(HoleSnap.mc.player, blockPos)) {
                        Vec3d playerPos = HoleSnap.mc.player.getPositionVector();
                        Vec3d targetPos = new Vec3d((double)blockPos.getX() + 0.5, HoleSnap.mc.player.posY, (double)blockPos.getZ() + 0.5);
                        float rotation = this.getRotationTo(playerPos, targetPos).x;
                        float yawRad = rotation / 180.0f * (float)Math.PI;
                        double dist = playerPos.distanceTo(targetPos);
                        EntityPlayerSP entityPlayerSP2 = HoleSnap.mc.player;
                        double baseSpeed = this.applySpeedPotionEffects(entityPlayerSP2);
                        double speed = HoleSnap.mc.player.onGround ? baseSpeed : Math.max(currentSpeed + 0.02, baseSpeed);
                        double cappedSpeed = Math.min(speed, dist);
                        HoleSnap.mc.player.motionX = (double)(-((float)Math.sin(yawRad))) * cappedSpeed;
                        HoleSnap.mc.player.motionZ = (double)((float)Math.cos(yawRad)) * cappedSpeed;
                        this.stuckTicks = HoleSnap.mc.player.collidedHorizontally ? ++this.stuckTicks : 0;
                    }
                } else {
                    this.disable();
                }
            } else {
                this.disable();
            }
        } else {
            this.disable();
        }
    }

    public double applySpeedPotionEffects(EntityLivingBase entityLivingBase) {
        PotionEffect potionEffect = entityLivingBase.getActivePotionEffect(MobEffects.SPEED);
        double d = potionEffect == null ? 0.2873 : 0.2873 * this.getSpeedEffectMultiplier(entityLivingBase);
        return d;
    }

    private double getSpeedEffectMultiplier(EntityLivingBase entityLivingBase) {
        PotionEffect potionEffect = entityLivingBase.getActivePotionEffect(MobEffects.SPEED);
        double d = potionEffect == null ? 1.0 : 1.0 + ((double)potionEffect.getAmplifier() + 1.0) * 0.2;
        return d;
    }

    private Vec2f getRotationTo(Vec3d posFrom, Vec3d posTo) {
        Vec3d vec3d = posTo.subtract(posFrom);
        return this.getRotationFromVec(vec3d);
    }

    private Vec2f getRotationFromVec(Vec3d vec) {
        double d = vec.x;
        double d2 = vec.z;
        double xz = Math.hypot(d, d2);
        d2 = vec.z;
        double d3 = vec.x;
        double yaw = this.normalizeAngle(Math.toDegrees(Math.atan2(d2, d3)) - 90.0);
        double pitch = this.normalizeAngle(Math.toDegrees(-Math.atan2(vec.y, xz)));
        return new Vec2f((float)yaw, (float)pitch);
    }

    private double normalizeAngle(double angleIn) {
        double d = 0;
        double angle = angleIn;
        angle %= 360.0;
        if (d >= 180.0) {
            angle -= 360.0;
        }
        if (angle < -180.0) {
            angle += 360.0;
        }
        return angle;
    }

    private boolean shouldDisable(double currentSpeed) {
        BlockPos blockPos = this.holePos;
        if (blockPos != null) {
            BlockPos blockPos2;
            if (HoleSnap.mc.player.posY < (double)blockPos.getY()) {
                return true;
            }
            if (HoleSnap.is2HoleB(blockPos) && this.toBlockPos(this.toVec3dCenter(blockPos2 = HoleSnap.mc.player.getPosition())).equals(blockPos)) {
                return true;
            }
        }
        if (this.stuckTicks > 5 && currentSpeed < 0.1) {
            return true;
        }
        if (currentSpeed >= 0.01) {
            return false;
        }
        EntityPlayerSP entityPlayerSP = HoleSnap.mc.player;
        return this.checkHole(entityPlayerSP) != HoleType.NONE;
    }

    private BlockPos getHole() {
        EntityPlayerSP entityPlayerSP;
        if (HoleSnap.mc.player.ticksExisted % 10 == 0 && !this.getFlooredPosition(entityPlayerSP = HoleSnap.mc.player).equals(this.holePos)) {
            return this.findHole();
        }
        BlockPos blockPos2 = this.holePos;
        BlockPos blockPos = blockPos2;
        if (blockPos != null) {
            return blockPos;
        }
        blockPos = this.findHole();
        return blockPos;
    }

    private BlockPos findHole() {
        BlockPos blockPos3;
        Pair<Double, BlockPos> closestHole = new Pair<>(69.69, BlockPos.ORIGIN);
        EntityPlayerSP entityPlayerSP = HoleSnap.mc.player;
        BlockPos playerPos = this.getFlooredPosition(entityPlayerSP);
        Integer ceilRange = this.range.getValue();
        BlockPos blockPos2 = playerPos.add(ceilRange, -1, ceilRange);
        BlockPos object = playerPos.add(-ceilRange, -1, -ceilRange);
        List<BlockPos> posList = this.getBlockPositionsInArea(blockPos2, object);
        for (BlockPos blockPos : posList) {
            BlockPos pos;
            Integer n;
            EntityPlayerSP entityPlayerSP2 = HoleSnap.mc.player;
            BlockPos posXZ = blockPos;
            double dist = this.distanceTo(entityPlayerSP2, posXZ);
            if (!(dist <= (double) (n = this.range.getValue())) || dist > closestHole.getLeft())
                continue;
            int n2 = 0;
            while (n2 < 6 && HoleSnap.mc.world.isAirBlock((pos = posXZ.add(0, -n2++, 0)).up())) {
                if (!HoleSnap.is2HoleB(pos) && this.checkHole(pos) == HoleType.NONE) continue;
                closestHole = new Pair<>(dist, pos);
            }
        }
        if (closestHole.getRight() != BlockPos.ORIGIN) {
            this.holePos = object = closestHole.getRight();
            blockPos3 = object;
        } else {
            blockPos3 = null;
        }
        return blockPos3;
    }

    public BlockPos getFlooredPosition(Entity entity) {
        return new BlockPos((int)Math.floor(entity.posX), (int)Math.floor(entity.posY), (int)Math.floor(entity.posZ));
    }

    public HoleType checkHole(Entity entity) {
        return this.checkHole(this.getFlooredPosition(entity));
    }

    public HoleType checkHole(BlockPos pos) {
        if (!(HoleSnap.mc.world.isAirBlock(pos) && HoleSnap.mc.world.isAirBlock(pos.up()) && HoleSnap.mc.world.isAirBlock(pos.up().up()))) {
            return HoleType.NONE;
        }
        HoleType type = HoleType.BEDROCK;
        for (BlockPos offset : surroundOffset) {
            Block block = HoleSnap.mc.world.getBlockState(pos.add(offset)).getBlock();
            if (this.checkBlock(block)) continue;
            type = HoleType.NONE;
            break;
        }
        return type;
    }

    private boolean checkBlock(Block block) {
        return block == Blocks.BEDROCK || block == Blocks.OBSIDIAN || block == Blocks.ENDER_CHEST || block == Blocks.ANVIL;
    }

    public double distanceTo(Entity entity, Vec3i vec3i) {
        double xDiff = (double)vec3i.getX() + 0.5 - entity.posX;
        double yDiff = (double)vec3i.getY() + 0.5 - entity.posY;
        double zDiff = (double)vec3i.getZ() + 0.5 - entity.posZ;
        return Math.sqrt(xDiff * xDiff + yDiff * yDiff + zDiff * zDiff);
    }

    public BlockPos toBlockPos(Vec3d vec) {
        int n = (int)Math.floor(vec.x);
        int n2 = (int)Math.floor(vec.y);
        return new BlockPos(n, n2, (int)Math.floor(vec.z));
    }

    public Vec3d toVec3dCenter(Vec3i vec3i) {
        return this.toVec3dCenter(vec3i, 0.0, 0.0, 0.0);
    }

    public Vec3d toVec3dCenter(Vec3i vec3i, double xOffset, double yOffset, double zOffset) {
        return new Vec3d((double)vec3i.getX() + 0.5 + xOffset, (double)vec3i.getY() + 0.5 + yOffset, (double)vec3i.getZ() + 0.5 + zOffset);
    }

    public List<BlockPos> getBlockPositionsInArea(BlockPos pos1, BlockPos pos2) {
        int minX = Math.min(pos1.getX(), pos2.getX());
        int maxX = Math.max(pos1.getX(), pos2.getX());
        int minY = Math.min(pos1.getY(), pos2.getY());
        int maxY = Math.max(pos1.getY(), pos2.getY());
        int minZ = Math.min(pos1.getZ(), pos2.getZ());
        int maxZ = Math.max(pos1.getZ(), pos2.getZ());
        return this.getBlockPos(minX, maxX, minY, maxY, minZ, maxZ);
    }

    private List<BlockPos> getBlockPos(int minX, int maxX, int minY, int maxY, int minZ, int maxZ) {
        ArrayList<BlockPos> returnList = new ArrayList<>();
        int n = minX;
        if (n <= maxX) {
            int x;
            do {
                int z;
                x = n++;
                int n2 = minZ;
                if (n2 > maxZ) continue;
                do {
                    int y;
                    z = n2++;
                    int n3 = minY;
                    if (n3 > maxY) continue;
                    do {
                        y = n3++;
                        returnList.add(new BlockPos(x, y, z));
                    } while (y != maxY);
                } while (z != maxZ);
            } while (x != maxX);
        }
        return returnList;
    }

    public static class Pair<L, R> {
        L left;
        R right;

        public Pair(L l, R r) {
            this.left = l;
            this.right = r;
        }

        public L getLeft() {
            return this.left;
        }

        public Pair<L, R> setLeft(L left) {
            this.left = left;
            return this;
        }

        public R getRight() {
            return this.right;
        }

        public Pair<L, R> setRight(R right) {
            this.right = right;
            return this;
        }
    }

    public static enum HoleType {
        NONE,
        OBBY,
        BEDROCK

    }

    public static enum BlockResistance {
        Blank,
        Breakable,
        Resistant,
        Unbreakable

    }
}

