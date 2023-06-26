package me.rebirthclient.mod.modules.impl.combat;

import me.rebirthclient.api.events.impl.PacketEvent;
import me.rebirthclient.api.events.impl.Render3DEvent;
import me.rebirthclient.api.managers.Managers;
import me.rebirthclient.api.util.EntityUtil;
import me.rebirthclient.api.util.InventoryUtil;
import me.rebirthclient.api.util.Timer;
import me.rebirthclient.api.util.math.MathUtil;
import me.rebirthclient.api.util.render.RenderUtil;
import me.rebirthclient.mod.modules.Category;
import me.rebirthclient.mod.modules.Module;
import me.rebirthclient.mod.modules.settings.Setting;
import net.minecraft.block.BlockEnderChest;
import net.minecraft.block.BlockObsidian;
import net.minecraft.block.BlockWeb;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.network.play.server.SPacketBlockChange;
import net.minecraft.util.EnumHand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.awt.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class HoleFiller extends Module
{
    private final Setting<Boolean> rotate;
    private final Setting<Boolean> packet;
    private final Setting<Boolean> webs;
    private final Setting<Boolean> autoDisable;
    private final Setting<Double> range;
    private final Setting<Boolean> smart;
    private final Setting<Logic> logic;
    private final Setting<Integer> smartRange;
    private final Setting<Boolean> render;
    private final Setting<Boolean> box;
    private final Setting<Boolean> line;
    private final Map<BlockPos, Long> renderBlocks;
    private final Timer renderTimer;
    private EntityPlayer closestTarget;

    public HoleFiller() {
        super("HoleFiller", "Fills all safe spots in radius", Category.COMBAT);
        this.rotate = this.add(new Setting("Rotate", false));
        this.packet = this.add(new Setting("Packet", false));
        this.webs = this.add(new Setting("Webs", false));
        this.autoDisable = this.add(new Setting("AutoDisable", true));
        this.range = this.add(new Setting("Radius", 4.0, 0.0, 6));
        this.smart = this.add(new Setting("Smart", false).setParent());
        this.logic = this.add(new Setting("Logic", Logic.PLAYER, v -> this.smart.isOpen()));
        this.smartRange = this.add(new Setting("EnemyRange", 4, 0, 6, v -> this.smart.isOpen()));
        this.render = this.add(new Setting("Render", true).setParent());
        this.box = this.add(new Setting("Box", true, v -> this.render.isOpen()));
        this.line = this.add(new Setting("Line", true, v -> this.render.isOpen()));
        this.renderBlocks = new ConcurrentHashMap<>();
        this.renderTimer = new Timer();
    }

    @Override
    public void onEnable() {
        super.onEnable();
    }

    @Override
    public void onDisable() {
        this.closestTarget = null;
        Managers.ROTATIONS.resetRotationsPacket();
    }

    @Override
    public String getInfo() {
        if (this.smart.getValue()) {
            return Managers.TEXT.normalizeCases(this.logic.getValue());
        }
        return "Normal";
    }

    @Override
    public void onRender3D(final Render3DEvent event) {
        if (this.render.getValue()) {
            this.renderTimer.reset();
            this.renderBlocks.forEach((pos, time) -> {
                final int lineA = 255;
                final int fillA = 80;
                if (System.currentTimeMillis() - time > 400L) {
                    this.renderTimer.reset();
                    this.renderBlocks.remove(pos);
                }
                else {
                    final long endTime = System.currentTimeMillis() - time - 100L;
                    final double normal = MathUtil.normalize((double)endTime, 0.0, 500.0);
                    final double normal2 = MathHelper.clamp(normal, 0.0, 1.0);
                    final double normal3 = -normal2 + 1.0;
                    final int firstAl = (int)(normal3 * lineA);
                    final int secondAl = (int)(normal3 * fillA);
                    RenderUtil.drawBoxESP(new BlockPos(pos), Managers.COLORS.getCurrent(), true, new Color(255, 255, 255, firstAl), 0.7f, this.line.getValue(), this.box.getValue(), secondAl, true, 0.0);
                }
            });
        }
    }

    @Override
    public void onUpdate() {
        if (HoleFiller.mc.world == null) {
            return;
        }
        if (this.smart.getValue()) {
            this.findClosestTarget();
        }
        final List<BlockPos> blocks = this.getPlacePositions();
        BlockPos q = null;
        final int obbySlot = InventoryUtil.findHotbarClass(BlockObsidian.class);
        final int eChestSlot = InventoryUtil.findHotbarClass(BlockEnderChest.class);
        final int webSlot = InventoryUtil.findHotbarClass(BlockWeb.class);
        if (!this.webs.getValue() && obbySlot == -1 && eChestSlot == -1) {
            return;
        }
        if (this.webs.getValue() && webSlot == -1 && obbySlot == -1 && eChestSlot == -1) {
            return;
        }
        final int originalSlot = HoleFiller.mc.player.inventory.currentItem;
        for (final BlockPos blockPos : blocks) {
            if (!HoleFiller.mc.world.getEntitiesWithinAABB(Entity.class, new AxisAlignedBB(blockPos)).isEmpty()) {
                continue;
            }
            if (this.smart.getValue() && this.isInRange(blockPos)) {
                q = blockPos;
            }
            else if (this.smart.getValue() && this.isInRange(blockPos) && this.logic.getValue() == Logic.HOLE && this.closestTarget.getDistanceSq(blockPos) <= this.smartRange.getValue()) {
                q = blockPos;
            }
            else {
                q = blockPos;
            }
        }
        if (q != null && HoleFiller.mc.player.onGround) {
            InventoryUtil.doSwap(this.webs.getValue() ? ((webSlot == -1) ? ((obbySlot == -1) ? eChestSlot : obbySlot) : webSlot) : ((obbySlot == -1) ? eChestSlot : obbySlot));
            this.renderBlocks.put(q, System.currentTimeMillis());
            Managers.INTERACTIONS.placeBlock(q, this.rotate.getValue(), this.packet.getValue(), false);
            if (HoleFiller.mc.player.inventory.currentItem != originalSlot) {
                InventoryUtil.doSwap(originalSlot);
            }
            HoleFiller.mc.player.swingArm(EnumHand.MAIN_HAND);
            HoleFiller.mc.player.inventory.currentItem = originalSlot;
        }
        if (q == null && this.autoDisable.getValue() && !this.smart.getValue()) {
            this.disable();
        }
    }

    @SubscribeEvent
    public void onReceivePacket(PacketEvent.Receive event) {
        if (event.isCanceled()) {
            return;
        }
        if (event.getPacket() instanceof SPacketBlockChange && this.renderBlocks.containsKey(((SPacketBlockChange)event.getPacket()).getBlockPosition())) {
            this.renderTimer.reset();
            if (((SPacketBlockChange)event.getPacket()).getBlockState().getBlock() != Blocks.AIR && this.renderTimer.passedMs(400L)) {
                this.renderBlocks.remove(((SPacketBlockChange)event.getPacket()).getBlockPosition());
            }
        }
    }

    private boolean isHole(final BlockPos pos) {
        final BlockPos boost = pos.add(0, 1, 0);
        final BlockPos boost2 = pos.add(0, 0, 0);
        final BlockPos boost3 = pos.add(0, 0, -1);
        final BlockPos boost4 = pos.add(1, 0, 0);
        final BlockPos boost5 = pos.add(-1, 0, 0);
        final BlockPos boost6 = pos.add(0, 0, 1);
        final BlockPos boost7 = pos.add(0, 2, 0);
        final BlockPos boost8 = pos.add(0.5, 0.5, 0.5);
        final BlockPos boost9 = pos.add(0, -1, 0);
        return HoleFiller.mc.world.getBlockState(boost).getBlock() == Blocks.AIR && HoleFiller.mc.world.getBlockState(boost2).getBlock() == Blocks.AIR && HoleFiller.mc.world.getBlockState(boost7).getBlock() == Blocks.AIR && (HoleFiller.mc.world.getBlockState(boost3).getBlock() == Blocks.OBSIDIAN || HoleFiller.mc.world.getBlockState(boost3).getBlock() == Blocks.BEDROCK) && (HoleFiller.mc.world.getBlockState(boost4).getBlock() == Blocks.OBSIDIAN || HoleFiller.mc.world.getBlockState(boost4).getBlock() == Blocks.BEDROCK) && (HoleFiller.mc.world.getBlockState(boost5).getBlock() == Blocks.OBSIDIAN || HoleFiller.mc.world.getBlockState(boost5).getBlock() == Blocks.BEDROCK) && (HoleFiller.mc.world.getBlockState(boost6).getBlock() == Blocks.OBSIDIAN || HoleFiller.mc.world.getBlockState(boost6).getBlock() == Blocks.BEDROCK) && HoleFiller.mc.world.getBlockState(boost8).getBlock() == Blocks.AIR && (HoleFiller.mc.world.getBlockState(boost9).getBlock() == Blocks.OBSIDIAN || HoleFiller.mc.world.getBlockState(boost9).getBlock() == Blocks.BEDROCK);
    }

    private BlockPos getPlayerPos() {
        return new BlockPos(Math.floor(HoleFiller.mc.player.posX), Math.floor(HoleFiller.mc.player.posY), Math.floor(HoleFiller.mc.player.posZ));
    }

    private BlockPos getClosestTargetPos(final EntityPlayer target) {
        return new BlockPos(Math.floor(target.posX), Math.floor(target.posY), Math.floor(target.posZ));
    }

    private void findClosestTarget() {
        final List<EntityPlayer> playerList = HoleFiller.mc.world.playerEntities;
        this.closestTarget = null;
        for (final EntityPlayer target : playerList) {
            if (target != HoleFiller.mc.player && !Managers.FRIENDS.isFriend(target.getName()) && EntityUtil.isLiving(target)) {
                if (target.getHealth() <= 0.0f) {
                    continue;
                }
                if (this.closestTarget == null) {
                    this.closestTarget = target;
                }
                else {
                    if (HoleFiller.mc.player.getDistance(target) >= HoleFiller.mc.player.getDistance(this.closestTarget)) {
                        continue;
                    }
                    this.closestTarget = target;
                }
            }
        }
    }

    private boolean isInRange(BlockPos blockPos) {
        NonNullList positions = NonNullList.create();
        positions.addAll(this.getSphere(this.getPlayerPos(), this.range.getValue().floatValue()).stream().filter(this::isHole).collect(Collectors.toList()));
        return positions.contains(blockPos);
    }

    private List<BlockPos> getPlacePositions() {
        NonNullList positions = NonNullList.create();
        if (this.smart.getValue() && this.closestTarget != null) {
            positions.addAll(this.getSphere(this.getClosestTargetPos(this.closestTarget), this.smartRange.getValue().floatValue()).stream().filter(this::isHole).filter(this::isInRange).collect(Collectors.toList()));
        } else if (!this.smart.getValue()) {
            positions.addAll(this.getSphere(this.getPlayerPos(), this.range.getValue().floatValue()).stream().filter(this::isHole).collect(Collectors.toList()));
        }
        return positions;
    }

    private List<BlockPos> getSphere(final BlockPos loc, final float r) {
        final ArrayList<BlockPos> circleBlocks = new ArrayList<>();
        final int cx = loc.getX();
        final int cy = loc.getY();
        final int cz = loc.getZ();
        for (int x = cx - (int)r; x <= cx + r; ++x) {
            for (int z = cz - (int)r; z <= cz + r; ++z) {
                int y = cy - (int)r;
                while (true) {
                    final float f = (float)y;
                    final float f2 = cy + r;
                    if (f >= f2) {
                        break;
                    }
                    final double dist = (cx - x) * (cx - x) + (cz - z) * (cz - z) + (cy - y) * (cy - y);
                    if (dist < r * r) {
                        final BlockPos l = new BlockPos(x, y, z);
                        circleBlocks.add(l);
                    }
                    ++y;
                }
            }
        }
        return circleBlocks;
    }

    private enum Logic
    {
        PLAYER,
        HOLE
    }
}
 