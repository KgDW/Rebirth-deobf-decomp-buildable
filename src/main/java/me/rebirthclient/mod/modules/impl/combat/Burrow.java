package me.rebirthclient.mod.modules.impl.combat;

import com.mojang.realmsclient.gui.ChatFormatting;
import me.rebirthclient.api.util.BlockUtil;
import me.rebirthclient.api.util.CombatUtil;
import me.rebirthclient.api.util.EntityUtil;
import me.rebirthclient.api.util.InventoryUtil;
import me.rebirthclient.api.util.Timer;
import me.rebirthclient.mod.modules.Category;
import me.rebirthclient.mod.modules.Module;
import me.rebirthclient.mod.modules.settings.Setting;
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityEnderCrystal;
import net.minecraft.entity.item.EntityExpBottle;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.item.EntityXPOrb;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.init.Blocks;
import net.minecraft.inventory.ClickType;
import net.minecraft.item.Item;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;

public class Burrow
extends Module {
    private final Setting<Boolean> placeDisable = this.add(new Setting<>("PlaceDisable", false));
    private final Setting<Boolean> wait = this.add(new Setting<>("Wait", true));
    private final Setting<Boolean> switchBypass = this.add(new Setting<>("SwitchBypass", true));
    private final Setting<Boolean> rotate = this.add(new Setting<>("Rotate", true));
    private final Setting<Boolean> onlyGround = this.add(new Setting<>("OnlyGround", true).setParent());
    private final Setting<Boolean> airCheck = this.add(new Setting<>("AirCheck", true, v -> this.onlyGround.isOpen()));
    private final Setting<Boolean> aboveHead = this.add(new Setting<>("AboveHead", true).setParent());
    private final Setting<Boolean> center = this.add(new Setting<>("Center", false, v -> this.aboveHead.isOpen()));
    private final Setting<Boolean> breakCrystal = this.add(new Setting<>("BreakCrystal", true).setParent());
    public final Setting<Float> safeHealth = this.add(new Setting<>("SafeHealth", 16.0f, 0.0f, 36.0f, v -> this.breakCrystal.isOpen()));
    private final Setting<Integer> multiPlace = this.add(new Setting<>("MultiPlace", 1, 1, 4));
    private final Setting<Integer> timeOut = this.add(new Setting<>("TimeOut", 500, 0, 2000));
    private final Setting<Integer> delay = this.add(new Setting<>("delay", 300, 0, 1000));
    private final Setting<Boolean> smartOffset = this.add(new Setting<>("SmartOffset", true));
    private final Setting<Double> offsetX = this.add(new Setting<>("OffsetX", -7.0, -14.0, 14.0, v -> !this.smartOffset.getValue()));
    private final Setting<Double> offsetY = this.add(new Setting<>("OffsetY", -7.0, -14.0, 14.0, v -> !this.smartOffset.getValue()));
    private final Setting<Double> offsetZ = this.add(new Setting<>("OffsetZ", -7.0, -14.0, 14.0, v -> !this.smartOffset.getValue()));
    private final Setting<Boolean> debug = this.add(new Setting<>("Debug", false));
    int progress = 0;
    private final Timer timer = new Timer();
    private final Timer timedOut = new Timer();
    public static Burrow INSTANCE;
    private boolean shouldWait = false;

    public Burrow() {
        super("Burrow", "unknown", Category.COMBAT);
        INSTANCE = this;
    }

    private static boolean checkSelf(BlockPos pos) {
        Vec3d[] vec3dList;
        for (Vec3d vec3d : vec3dList = EntityUtil.getVarOffsets(0, 0, 0)) {
            BlockPos position = new BlockPos(pos).add(vec3d.x, vec3d.y, vec3d.z);
            for (Entity entity : Burrow.mc.world.getEntitiesWithinAABB(Entity.class, new AxisAlignedBB(position))) {
                if (entity != Burrow.mc.player) continue;
                return true;
            }
        }
        return false;
    }

    private static boolean isAir(BlockPos pos) {
        return Burrow.mc.world.isAirBlock(pos);
    }

    private static boolean Trapped(BlockPos pos) {
        return !Burrow.mc.world.isAirBlock(pos) && Burrow.checkSelf(pos.down(2));
    }

    public static boolean canReplace(BlockPos pos) {
        return Burrow.mc.world.getBlockState(pos).getMaterial().isReplaceable();
    }

    @Override
    public void onEnable() {
        this.timedOut.reset();
        this.shouldWait = this.wait.getValue();
    }

    @Override
    public void onDisable() {
        this.timer.reset();
        this.shouldWait = false;
    }

    @Override
    public void onUpdate() {
        this.progress = 0;
        int blockSlot = !this.switchBypass.getValue() ? (InventoryUtil.getItemHotbar(Item.getItemFromBlock(Blocks.OBSIDIAN)) != -1 ? InventoryUtil.getItemHotbar(Item.getItemFromBlock(Blocks.OBSIDIAN)) : InventoryUtil.getItemHotbar(Item.getItemFromBlock(Blocks.ENDER_CHEST))) : (InventoryUtil.findItemInventorySlot(Item.getItemFromBlock(Blocks.OBSIDIAN), false, true) != -1 ? InventoryUtil.findItemInventorySlot(Item.getItemFromBlock(Blocks.OBSIDIAN), false, true) : InventoryUtil.findItemInventorySlot(Item.getItemFromBlock(Blocks.ENDER_CHEST), false, true));
        if (blockSlot == -1) {
            this.sendMessage(ChatFormatting.RED + "Obsidian/Ender Chest ?");
            this.disable();
            return;
        }
        if (this.timedOut.passedMs(this.timeOut.getValue())) {
            this.disable();
            return;
        }
        BlockPos originalPos = EntityUtil.getPlayerPos();
        if (!(this.canPlace(new BlockPos(Burrow.mc.player.posX + 0.3, Burrow.mc.player.posY + 0.5, Burrow.mc.player.posZ + 0.3)) || this.canPlace(new BlockPos(Burrow.mc.player.posX - 0.3, Burrow.mc.player.posY + 0.5, Burrow.mc.player.posZ + 0.3)) || this.canPlace(new BlockPos(Burrow.mc.player.posX + 0.3, Burrow.mc.player.posY + 0.5, Burrow.mc.player.posZ - 0.3)) || this.canPlace(new BlockPos(Burrow.mc.player.posX - 0.3, Burrow.mc.player.posY + 0.5, Burrow.mc.player.posZ - 0.3)))) {
            if (this.debug.getValue()) {
                this.sendMessage("cant place");
            }
            if (!this.shouldWait) {
                this.disable();
            }
            return;
        }
        if (Burrow.mc.player.isInLava() || Burrow.mc.player.isInWater() || Burrow.mc.player.isInWeb) {
            if (this.debug.getValue()) {
                this.sendMessage("player stuck");
            }
            return;
        }
        if (this.onlyGround.getValue()) {
            if (!Burrow.mc.player.onGround) {
                if (this.debug.getValue()) {
                    this.sendMessage("player not on ground");
                }
                return;
            }
            if (this.airCheck.getValue() && Burrow.isAir(EntityUtil.getPlayerPos().down())) {
                if (this.debug.getValue()) {
                    this.sendMessage("player in air");
                }
                return;
            }
        }
        if (!this.timer.passedMs(this.delay.getValue())) {
            return;
        }
        if (this.breakCrystal.getValue() && EntityUtil.getHealth(Burrow.mc.player) >= this.safeHealth.getValue()) {
            if (this.debug.getValue()) {
                this.sendMessage("try break crystal");
            }
            CombatUtil.attackCrystal(originalPos, this.rotate.getValue(), false);
            CombatUtil.attackCrystal(new BlockPos(Burrow.mc.player.posX + 0.3, Burrow.mc.player.posY + 0.5, Burrow.mc.player.posZ + 0.3), this.rotate.getValue(), false);
            CombatUtil.attackCrystal(new BlockPos(Burrow.mc.player.posX + 0.3, Burrow.mc.player.posY + 0.5, Burrow.mc.player.posZ - 0.3), this.rotate.getValue(), false);
            CombatUtil.attackCrystal(new BlockPos(Burrow.mc.player.posX - 0.3, Burrow.mc.player.posY + 0.5, Burrow.mc.player.posZ + 0.3), this.rotate.getValue(), false);
            CombatUtil.attackCrystal(new BlockPos(Burrow.mc.player.posX - 0.3, Burrow.mc.player.posY + 0.5, Burrow.mc.player.posZ - 0.3), this.rotate.getValue(), false);
        }
        this.timer.reset();
        this.shouldWait = false;
        BlockPos headPos = EntityUtil.getPlayerPos().up(2);
        if (Burrow.Trapped(headPos) || Burrow.Trapped(headPos.add(1, 0, 0)) || Burrow.Trapped(headPos.add(-1, 0, 0)) || Burrow.Trapped(headPos.add(0, 0, 1)) || Burrow.Trapped(headPos.add(0, 0, -1)) || Burrow.Trapped(headPos.add(1, 0, -1)) || Burrow.Trapped(headPos.add(-1, 0, -1)) || Burrow.Trapped(headPos.add(1, 0, 1)) || Burrow.Trapped(headPos.add(-1, 0, 1))) {
            if (!this.aboveHead.getValue()) {
                if (!this.shouldWait) {
                    this.disable();
                }
                return;
            }
            boolean moved = false;
            BlockPos offPos = originalPos;
            if (Burrow.checkSelf(offPos) && !Burrow.canReplace(offPos)) {
                this.gotoPos(offPos);
                if (this.debug.getValue()) {
                    this.sendMessage("moved to center " + ((double)offPos.getX() + 0.5 - Burrow.mc.player.posX) + " " + ((double)offPos.getZ() + 0.5 - Burrow.mc.player.posZ));
                }
            } else {
                for (EnumFacing facing : EnumFacing.VALUES) {
                    if (facing == EnumFacing.UP || facing == EnumFacing.DOWN || !Burrow.checkSelf(offPos = originalPos.offset(facing)) || Burrow.canReplace(offPos)) continue;
                    this.gotoPos(offPos);
                    moved = true;
                    if (!this.debug.getValue()) break;
                    this.sendMessage("moved to block " + ((double)offPos.getX() + 0.5 - Burrow.mc.player.posX) + " " + ((double)offPos.getZ() + 0.5 - Burrow.mc.player.posZ));
                    break;
                }
                if (!moved) {
                    for (EnumFacing facing : EnumFacing.VALUES) {
                        if (facing == EnumFacing.UP || facing == EnumFacing.DOWN || !Burrow.checkSelf(offPos = originalPos.offset(facing))) continue;
                        this.gotoPos(offPos);
                        moved = true;
                        if (!this.debug.getValue()) break;
                        this.sendMessage("moved to entity " + ((double)offPos.getX() + 0.5 - Burrow.mc.player.posX) + " " + ((double)offPos.getZ() + 0.5 - Burrow.mc.player.posZ));
                        break;
                    }
                    if (!moved) {
                        if (!this.center.getValue()) {
                            if (!this.shouldWait) {
                                this.disable();
                            }
                            return;
                        }
                        for (EnumFacing facing : EnumFacing.VALUES) {
                            if (facing == EnumFacing.UP || facing == EnumFacing.DOWN || !Burrow.canReplace(offPos = originalPos.offset(facing)) || !Burrow.canReplace(offPos.up())) continue;
                            this.gotoPos(offPos);
                            if (this.debug.getValue()) {
                                this.sendMessage("moved to air " + ((double)offPos.getX() + 0.5 - Burrow.mc.player.posX) + " " + ((double)offPos.getZ() + 0.5 - Burrow.mc.player.posZ));
                            }
                            moved = true;
                            break;
                        }
                        if (!moved) {
                            if (!this.shouldWait) {
                                this.disable();
                            }
                            return;
                        }
                    }
                }
            }
        } else {
            if (this.debug.getValue()) {
                this.sendMessage("fake jump");
            }
            Burrow.mc.player.connection.sendPacket(new CPacketPlayer.Position(Burrow.mc.player.posX, Burrow.mc.player.posY + 0.4199999868869781, Burrow.mc.player.posZ, false));
            Burrow.mc.player.connection.sendPacket(new CPacketPlayer.Position(Burrow.mc.player.posX, Burrow.mc.player.posY + 0.7531999805212017, Burrow.mc.player.posZ, false));
            Burrow.mc.player.connection.sendPacket(new CPacketPlayer.Position(Burrow.mc.player.posX, Burrow.mc.player.posY + 0.9999957640154541, Burrow.mc.player.posZ, false));
            Burrow.mc.player.connection.sendPacket(new CPacketPlayer.Position(Burrow.mc.player.posX, Burrow.mc.player.posY + 1.1661092609382138, Burrow.mc.player.posZ, false));
        }
        int oldSlot = Burrow.mc.player.inventory.currentItem;
        if (!this.switchBypass.getValue()) {
            InventoryUtil.doSwap(blockSlot);
        } else {
            Burrow.mc.playerController.windowClick(0, blockSlot, oldSlot, ClickType.SWAP, Burrow.mc.player);
        }
        this.placeBlock(originalPos);
        this.placeBlock(new BlockPos(Burrow.mc.player.posX + 0.3, Burrow.mc.player.posY + 0.5, Burrow.mc.player.posZ + 0.3));
        this.placeBlock(new BlockPos(Burrow.mc.player.posX + 0.3, Burrow.mc.player.posY + 0.5, Burrow.mc.player.posZ - 0.3));
        this.placeBlock(new BlockPos(Burrow.mc.player.posX - 0.3, Burrow.mc.player.posY + 0.5, Burrow.mc.player.posZ + 0.3));
        this.placeBlock(new BlockPos(Burrow.mc.player.posX - 0.3, Burrow.mc.player.posY + 0.5, Burrow.mc.player.posZ - 0.3));
        if (!this.switchBypass.getValue()) {
            InventoryUtil.doSwap(oldSlot);
        } else {
            Burrow.mc.playerController.windowClick(0, blockSlot, oldSlot, ClickType.SWAP, Burrow.mc.player);
        }
        if (this.smartOffset.getValue()) {
            double distance = 0.0;
            BlockPos bestPos = null;
            for (BlockPos pos : BlockUtil.getBox(6.0f)) {
                if (!this.canGoto(pos) || Burrow.mc.player.getDistance((double)pos.getX() + 0.5, (double)pos.getY() + 0.5, (double)pos.getZ() + 0.5) <= 3.0 || bestPos != null && !(Burrow.mc.player.getDistance((double)pos.getX() + 0.5, (double)pos.getY() + 0.5, (double)pos.getZ() + 0.5) < distance)) continue;
                bestPos = pos;
                distance = Burrow.mc.player.getDistance((double)pos.getX() + 0.5, (double)pos.getY() + 0.5, (double)pos.getZ() + 0.5);
            }
            if (bestPos != null) {
                Burrow.mc.player.connection.sendPacket(new CPacketPlayer.Position((double)bestPos.getX() + 0.5, bestPos.getY(), (double)bestPos.getZ() + 0.5, false));
            } else {
                for (BlockPos pos : BlockUtil.getBox(6.0f)) {
                    if (!this.canGoto(pos) || Burrow.mc.player.getDistance((double)pos.getX() + 0.5, (double)pos.getY() + 0.5, (double)pos.getZ() + 0.5) <= 2.0 || bestPos != null && !(Burrow.mc.player.getDistance((double)pos.getX() + 0.5, (double)pos.getY() + 0.5, (double)pos.getZ() + 0.5) < distance)) continue;
                    bestPos = pos;
                    distance = Burrow.mc.player.getDistance((double)pos.getX() + 0.5, (double)pos.getY() + 0.5, (double)pos.getZ() + 0.5);
                }
                if (bestPos != null) {
                    Burrow.mc.player.connection.sendPacket(new CPacketPlayer.Position((double)bestPos.getX() + 0.5, bestPos.getY(), (double)bestPos.getZ() + 0.5, false));
                } else {
                    Burrow.mc.player.connection.sendPacket(new CPacketPlayer.Position(Burrow.mc.player.posX, -7.0, Burrow.mc.player.posZ, false));
                }
            }
        } else {
            Burrow.mc.player.connection.sendPacket(new CPacketPlayer.Position(Burrow.mc.player.posX + this.offsetX.getValue(), Burrow.mc.player.posY + this.offsetY.getValue(), Burrow.mc.player.posZ + this.offsetZ.getValue(), false));
        }
        if (this.placeDisable.getValue()) {
            this.disable();
        }
    }

    private void gotoPos(BlockPos offPos) {
        if (Math.abs((double)offPos.getX() + 0.5 - Burrow.mc.player.posX) < Math.abs((double)offPos.getZ() + 0.5 - Burrow.mc.player.posZ)) {
            Burrow.mc.player.connection.sendPacket(new CPacketPlayer.Position(Burrow.mc.player.posX, Burrow.mc.player.posY + 0.2, Burrow.mc.player.posZ + ((double)offPos.getZ() + 0.5 - Burrow.mc.player.posZ), true));
        } else {
            Burrow.mc.player.connection.sendPacket(new CPacketPlayer.Position(Burrow.mc.player.posX + ((double)offPos.getX() + 0.5 - Burrow.mc.player.posX), Burrow.mc.player.posY + 0.2, Burrow.mc.player.posZ, true));
        }
    }

    private boolean canGoto(BlockPos pos) {
        return Burrow.isAir(pos) && Burrow.isAir(pos.up());
    }

    private void placeBlock(BlockPos pos) {
        if (this.progress >= this.multiPlace.getValue()) {
            return;
        }
        if (this.canPlace(pos)) {
            BlockUtil.placeBlock(pos, EnumHand.MAIN_HAND, this.rotate.getValue(), true, this.breakCrystal.getValue(), false);
            ++this.progress;
        }
    }

    private boolean canPlace(BlockPos pos) {
        if (!BlockUtil.canBlockFacing(pos)) {
            return false;
        }
        if (!Burrow.canReplace(pos)) {
            return false;
        }
        return !this.checkEntity(pos);
    }

    private boolean checkEntity(BlockPos pos) {
        for (Entity entity : Burrow.mc.world.getEntitiesWithinAABB(Entity.class, new AxisAlignedBB(pos))) {
            if (entity.isDead || entity instanceof EntityItem || entity instanceof EntityXPOrb || entity instanceof EntityExpBottle || entity instanceof EntityArrow || !(entity instanceof EntityEnderCrystal ? !this.breakCrystal.getValue() || EntityUtil.getHealth(Burrow.mc.player) < this.safeHealth.getValue() : entity != Burrow.mc.player)) continue;
            return true;
        }
        return false;
    }
}

