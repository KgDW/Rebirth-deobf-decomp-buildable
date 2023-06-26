package me.rebirthclient.mod.modules.impl.combat;

import com.mojang.realmsclient.gui.ChatFormatting;
import java.awt.Color;
import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import me.rebirthclient.api.events.impl.BlockEvent;
import me.rebirthclient.api.events.impl.MotionEvent;
import me.rebirthclient.api.events.impl.PacketEvent;
import me.rebirthclient.api.events.impl.Render3DEvent;
import me.rebirthclient.api.util.BlockUtil;
import me.rebirthclient.api.util.EntityUtil;
import me.rebirthclient.api.util.FadeUtils;
import me.rebirthclient.api.util.InventoryUtil;
import me.rebirthclient.api.util.Timer;
import me.rebirthclient.api.util.render.RenderUtil;
import me.rebirthclient.mod.modules.Category;
import me.rebirthclient.mod.modules.Module;
import me.rebirthclient.mod.modules.impl.combat.PullCrystal;
import me.rebirthclient.mod.modules.settings.Bind;
import me.rebirthclient.mod.modules.settings.Setting;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Enchantments;
import net.minecraft.init.MobEffects;
import net.minecraft.inventory.ClickType;
import net.minecraft.item.ItemAir;
import net.minecraft.item.ItemStack;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.CPacketHeldItemChange;
import net.minecraft.network.play.client.CPacketPlayerDigging;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class PacketMine
extends Module {
    public static final List<Block> godBlocks = Arrays.asList(Blocks.COMMAND_BLOCK, Blocks.FLOWING_LAVA, Blocks.LAVA, Blocks.FLOWING_WATER, Blocks.WATER, Blocks.BEDROCK, Blocks.BARRIER);
    private final Setting<Integer> delay = this.add(new Setting<>("Delay", 100, 0, 1000));
    private final Setting<Float> damage = this.add(new Setting<>("Damage", 0.7f, 0.0f, 2.0f));
    private final Setting<Float> range = this.add(new Setting<>("Range", 7.0f, 3.0f, 10.0f));
    private final Setting<Integer> maxBreak = this.add(new Setting<>("MaxBreak", 2, 1, 20));
    private final Setting<Boolean> instant = this.add(new Setting<>("Instant", false));
    private final Setting<Boolean> restart = this.add(new Setting<>("ReStart", true, v -> !this.instant.getValue()));
    private final Setting<Boolean> wait = this.add(new Setting<>("Wait", true, v -> !this.instant.getValue()).setParent());
    private final Setting<Boolean> mineAir = this.add(new Setting<>("MineAir", true, v -> this.wait.isOpen()));
    public final Setting<Boolean> godCancel = this.add(new Setting<>("GodCancel", true));
    public final Setting<Boolean> hotBar = this.add(new Setting<>("HotBar", false));
    private final Setting<Boolean> onlyGround = this.add(new Setting<>("OnlyGround", true).setParent());
    private final Setting<Boolean> allowWeb = this.add(new Setting<>("AllowWeb", true, v -> this.onlyGround.getValue()));
    private final Setting<Boolean> doubleBreak = this.add(new Setting<>("DoubleBreak", true));
    private final Setting<Boolean> swing = this.add(new Setting<>("Swing", true));
    private final Setting<Boolean> rotate = this.add(new Setting<>("Rotate", true).setParent());
    private final Setting<Integer> time = this.add(new Setting<>("Time", 100, 0, 2000, v -> this.rotate.isOpen()));
    private final Setting<Boolean> switchReset = this.add(new Setting<>("SwitchReset", false));
    private final Setting<Boolean> render = this.add(new Setting<>("Render", true).setParent());
    private final Setting<Mode> animationMode = this.add(new Setting<>("AnimationMode", Mode.Up, v -> this.render.isOpen()));
    private final Setting<Float> fillStart = this.add(new Setting<>("FillStart", 0.2f, 0.0f, 1.0f, v -> this.render.isOpen() && this.animationMode.getValue() == Mode.Custom));
    private final Setting<Float> boxStart = this.add(new Setting<>("BoxStart", 0.4f, 0.0f, 1.0f, v -> this.render.isOpen() && this.animationMode.getValue() == Mode.Custom));
    private final Setting<Float> boxExtend = this.add(new Setting<>("BoxExtend", 0.2f, 0.0f, 1.0f, v -> this.render.isOpen() && this.animationMode.getValue() == Mode.Custom));
    private final Setting<Boolean> text = this.add(new Setting<>("Text", true, v -> this.render.isOpen()).setParent());
    private final Setting<ColorMode> textColorMode = this.add(new Setting<>("TextMode", ColorMode.Progress, v -> this.render.isOpen() && this.text.isOpen()));
    private final Setting<Color> textColor = this.add(new Setting<>("TextColor", new Color(255, 255, 255, 255), v -> this.render.isOpen() && this.text.isOpen()).hideAlpha());
    private final Setting<Boolean> box = this.add(new Setting<>("Box", true, v -> this.render.isOpen()).setParent());
    private final Setting<Integer> boxAlpha = this.add(new Setting<>("BoxAlpha", 100, 0, 255, v -> this.box.isOpen() && this.render.isOpen()));
    private final Setting<Boolean> outline = this.add(new Setting<>("Outline", true, v -> this.render.isOpen()).setParent());
    private final Setting<Integer> outlineAlpha = this.add(new Setting<>("OutlineAlpha", 100, 0, 255, v -> this.outline.isOpen() && this.render.isOpen()));
    private final Setting<ColorMode> colorMode = this.add(new Setting<>("ColorMode", ColorMode.Progress, v -> this.render.isOpen()));
    private final Setting<Color> color = this.add(new Setting<>("Color", new Color(189, 212, 255), v -> this.render.isOpen() && this.colorMode.getValue() == ColorMode.Custom).hideAlpha());
    private final Setting<Bind> enderChest = this.add(new Setting<>("EnderChest", new Bind(-1)));
    private final Setting<Boolean> debug = this.add(new Setting<>("Debug", false));
    public static PacketMine INSTANCE;
    public static BlockPos breakPos;
    private final Timer mineTimer = new Timer();
    private FadeUtils animationTime = new FadeUtils(1000L);
    private boolean startMine = false;
    private int breakNumber = 0;
    private final Timer delayTimer = new Timer();
    private boolean first = false;
    private final Timer firstTimer = new Timer();
    int lastSlot = -1;

    public PacketMine() {
        super("PacketMine", "1", Category.COMBAT);
        INSTANCE = this;
    }

    @Override
    public void onDisable() {
        this.startMine = false;
        breakPos = null;
    }

    @Override
    public void onTick() {
        int slot;
        if (breakPos == null) {
            this.breakNumber = 0;
            this.startMine = false;
            return;
        }
        if (PacketMine.mc.player.isCreative() || PacketMine.mc.player.getDistance((double)breakPos.getX() + 0.5, (double)breakPos.getY() + 0.5, (double)breakPos.getZ() + 0.5) > (double) this.range.getValue() || this.breakNumber > this.maxBreak.getValue() - 1 || !this.wait.getValue() && PacketMine.mc.world.isAirBlock(breakPos) && !this.instant.getValue()) {
            this.startMine = false;
            this.breakNumber = 0;
            breakPos = null;
            return;
        }
        if (godBlocks.contains(PacketMine.mc.world.getBlockState(breakPos).getBlock())) {
            if (this.godCancel.getValue()) {
                breakPos = null;
                this.startMine = false;
            }
            return;
        }
        if (PacketMine.mc.world.isAirBlock(breakPos)) {
            int eChest;
            if (this.enderChest.getValue().isDown() && BlockUtil.canPlace(breakPos) && (eChest = InventoryUtil.findHotbarBlock(Blocks.ENDER_CHEST)) != -1) {
                int oldSlot = PacketMine.mc.player.inventory.currentItem;
                InventoryUtil.doSwap(eChest);
                BlockUtil.placeBlock(breakPos, EnumHand.MAIN_HAND, this.rotate.getValue(), true);
                InventoryUtil.doSwap(oldSlot);
            }
            this.breakNumber = 0;
        }
        if (this.first) {
            if (!this.firstTimer.passedMs(300L)) {
                return;
            }
            PacketMine.mc.player.connection.sendPacket(new CPacketPlayerDigging(CPacketPlayerDigging.Action.STOP_DESTROY_BLOCK, breakPos, BlockUtil.getRayTraceFacing(breakPos)));
            this.first = false;
            return;
        }
        if (!this.delayTimer.passedMs(this.delay.getValue())) {
            return;
        }
        if (this.startMine) {
            if (PacketMine.mc.world.isAirBlock(breakPos)) {
                return;
            }
            if (!(!this.onlyGround.getValue() || PacketMine.mc.player.onGround || this.allowWeb.getValue() && PacketMine.mc.player.isInWeb)) {
                return;
            }
            if (PullCrystal.INSTANCE.isOn() && breakPos.equals(PullCrystal.powerPos) && PullCrystal.crystalPos != null && !BlockUtil.posHasCrystal(PullCrystal.crystalPos)) {
                return;
            }
            slot = this.getTool(breakPos);
            if (slot == -1) {
                slot = PacketMine.mc.player.inventory.currentItem + 36;
            }
            if (this.mineTimer.passedMs((long)(1.0f / PacketMine.getBlockStrength(breakPos, PacketMine.mc.player.inventoryContainer.getInventory().get(slot)) / 20.0f * 1000.0f * this.damage.getValue()))) {
                boolean shouldSwitch;
                int old = PacketMine.mc.player.inventory.currentItem;
                boolean bl = shouldSwitch = old + 36 != slot;
                if (shouldSwitch) {
                    if (this.hotBar.getValue()) {
                        InventoryUtil.doSwap(slot - 36);
                    } else {
                        PacketMine.mc.playerController.windowClick(0, slot, old, ClickType.SWAP, PacketMine.mc.player);
                    }
                }
                if (this.rotate.getValue()) {
                    EntityUtil.facePosFacing(breakPos, BlockUtil.getRayTraceFacing(breakPos));
                }
                if (this.swing.getValue()) {
                    PacketMine.mc.player.swingArm(EnumHand.MAIN_HAND);
                }
                PacketMine.mc.player.connection.sendPacket(new CPacketPlayerDigging(CPacketPlayerDigging.Action.STOP_DESTROY_BLOCK, breakPos, BlockUtil.getRayTraceFacing(breakPos)));
                if (shouldSwitch) {
                    if (this.hotBar.getValue()) {
                        InventoryUtil.doSwap(old);
                    } else {
                        PacketMine.mc.playerController.windowClick(0, slot, old, ClickType.SWAP, PacketMine.mc.player);
                    }
                }
                ++this.breakNumber;
                this.delayTimer.reset();
            }
        } else {
            if (!this.mineAir.getValue() && PacketMine.mc.world.isAirBlock(breakPos)) {
                return;
            }
            slot = this.getTool(breakPos);
            if (slot == -1) {
                slot = PacketMine.mc.player.inventory.currentItem + 36;
            }
            this.animationTime = new FadeUtils((long)(1.0f / PacketMine.getBlockStrength(breakPos, PacketMine.mc.player.inventoryContainer.getInventory().get(slot)) / 20.0f * 1000.0f * this.damage.getValue()));
            this.mineTimer.reset();
            if (this.swing.getValue()) {
                PacketMine.mc.player.swingArm(EnumHand.MAIN_HAND);
            }
            PacketMine.mc.player.connection.sendPacket(new CPacketPlayerDigging(CPacketPlayerDigging.Action.START_DESTROY_BLOCK, breakPos, BlockUtil.getRayTraceFacing(breakPos)));
            this.delayTimer.reset();
        }
    }

    @SubscribeEvent
    public void onMotion(MotionEvent event) {
        if (PacketMine.fullNullCheck()) {
            return;
        }
        if (!(!this.onlyGround.getValue() || PacketMine.mc.player.onGround || this.allowWeb.getValue() && PacketMine.mc.player.isInWeb)) {
            return;
        }
        if (this.rotate.getValue() && breakPos != null && !PacketMine.mc.world.isAirBlock(breakPos) && this.time.getValue() > 0) {
            float breakTime;
            int slot = this.getTool(breakPos);
            if (slot == -1) {
                slot = PacketMine.mc.player.inventory.currentItem + 36;
            }
            if ((breakTime = 1.0f / PacketMine.getBlockStrength(breakPos, PacketMine.mc.player.inventoryContainer.getInventory().get(slot)) / 20.0f * 1000.0f * this.damage.getValue() - (float) this.time.getValue()) <= 0.0f || this.mineTimer.passedMs((long)breakTime)) {
                PacketMine.facePosFacing(breakPos, BlockUtil.getRayTraceFacing(breakPos), event);
            }
        }
    }

    @SubscribeEvent(priority=EventPriority.LOWEST)
    public void onSend(PacketEvent.Send event) {
        if (PacketMine.fullNullCheck() || PacketMine.mc.player.isCreative() || !this.debug.getValue() || !(event.getPacket() instanceof CPacketPlayerDigging)) {
            return;
        }
        this.sendMessage(((CPacketPlayerDigging)event.getPacket()).getAction().name());
    }

    @SubscribeEvent
    public void onPacketSend(PacketEvent.Send event) {
        if (PacketMine.fullNullCheck() || PacketMine.mc.player.isCreative()) {
            return;
        }
        if (event.getPacket() instanceof CPacketHeldItemChange) {
            if (((CPacketHeldItemChange)event.getPacket()).getSlotId() != this.lastSlot) {
                this.lastSlot = ((CPacketHeldItemChange)event.getPacket()).getSlotId();
                if (this.switchReset.getValue()) {
                    this.startMine = false;
                    this.mineTimer.reset();
                    this.animationTime.reset();
                }
            }
            return;
        }
        if (!(event.getPacket() instanceof CPacketPlayerDigging)) {
            return;
        }
        if (((CPacketPlayerDigging)event.getPacket()).getAction() == CPacketPlayerDigging.Action.START_DESTROY_BLOCK) {
            if (breakPos == null || !((CPacketPlayerDigging)event.getPacket()).getPosition().equals(breakPos)) {
                event.setCanceled(true);
                return;
            }
            this.startMine = true;
        } else if (((CPacketPlayerDigging)event.getPacket()).getAction() == CPacketPlayerDigging.Action.STOP_DESTROY_BLOCK) {
            if (breakPos == null || !((CPacketPlayerDigging)event.getPacket()).getPosition().equals(breakPos)) {
                event.setCanceled(true);
                return;
            }
            if (!this.instant.getValue()) {
                this.startMine = false;
            }
        }
    }

    public static void facePosFacing(BlockPos pos, EnumFacing side, MotionEvent event) {
        Vec3d hitVec = new Vec3d(pos).add(0.5, 0.5, 0.5).add(new Vec3d(side.getDirectionVec()).scale(0.5));
        PacketMine.faceVector(hitVec, event);
    }

    private static void faceVector(Vec3d vec, MotionEvent event) {
        float[] rotations = EntityUtil.getLegitRotations(vec);
        event.setRotation(rotations[0], rotations[1]);
    }

    @Override
    public void onRender3D(Render3DEvent event) {
        if (!PacketMine.mc.player.isCreative() && breakPos != null) {
            if (this.debug.getValue()) {
                RenderUtil.drawBBFill(new AxisAlignedBB(breakPos.offset(BlockUtil.getRayTraceFacing(breakPos))), new Color(255, 255, 255), 70);
            }
            if (this.render.getValue()) {
                if (PacketMine.mc.world.isAirBlock(breakPos) && !this.wait.getValue() && !this.instant.getValue()) {
                    return;
                }
                if (godBlocks.contains(PacketMine.mc.world.getBlockState(breakPos).getBlock())) {
                    this.draw(breakPos, 1.0, this.colorMode.getValue() == ColorMode.Custom ? this.color.getValue() : new Color(255, 0, 0, 255), true);
                    if (this.text.getValue()) {
                        AxisAlignedBB renderBB = PacketMine.mc.world.getBlockState(breakPos).getSelectedBoundingBox(PacketMine.mc.world, breakPos);
                        RenderUtil.drawText(renderBB, ChatFormatting.RED + "GodBlock");
                    }
                } else {
                    int slot = this.getTool(breakPos);
                    if (slot == -1) {
                        slot = PacketMine.mc.player.inventory.currentItem + 36;
                    }
                    this.animationTime.setLength((long)(1.0f / PacketMine.getBlockStrength(breakPos, PacketMine.mc.player.inventoryContainer.getInventory().get(slot)) / 20.0f * 1000.0f * this.damage.getValue()));
                    this.draw(breakPos, PacketMine.mc.world.isAirBlock(breakPos) ? 1.0 : this.animationTime.easeOutQuad(), this.colorMode.getValue() == ColorMode.Custom ? this.color.getValue() : new Color((int)(255.0 * Math.abs(this.animationTime.easeOutQuad() - 1.0)), (int)(255.0 * this.animationTime.easeOutQuad()), 0), PacketMine.mc.world.isAirBlock(breakPos));
                    if (this.text.getValue()) {
                        AxisAlignedBB renderBB = PacketMine.mc.world.getBlockState(breakPos).getSelectedBoundingBox(PacketMine.mc.world, breakPos);
                        if (!PacketMine.mc.world.isAirBlock(breakPos)) {
                            if ((float)((int)this.mineTimer.getPassedTimeMs()) < 1.0f / PacketMine.getBlockStrength(breakPos, PacketMine.mc.player.inventoryContainer.getInventory().get(slot)) / 20.0f * 1000.0f * this.damage.getValue()) {
                                double num1 = (double)this.mineTimer.getPassedTimeMs() / (double)(1.0f / PacketMine.getBlockStrength(breakPos, PacketMine.mc.player.inventoryContainer.getInventory().get(slot)) / 20.0f * 1000.0f * this.damage.getValue() / 100.0f);
                                DecimalFormat df = new DecimalFormat("0.0");
                                RenderUtil.drawText(renderBB, df.format(num1) + "%", this.textColorMode.getValue() == ColorMode.Progress ? new Color((int)(255.0 * Math.abs(this.animationTime.easeOutQuad() - 1.0)), (int)(255.0 * this.animationTime.easeOutQuad()), 0, 255) : this.textColor.getValue());
                            } else {
                                RenderUtil.drawText(renderBB, "100.0%", this.textColorMode.getValue() == ColorMode.Progress ? new Color(0, 255, 0, 255) : this.textColor.getValue());
                            }
                        } else {
                            RenderUtil.drawText(renderBB, "Waiting", this.textColorMode.getValue() == ColorMode.Progress ? new Color(0, 255, 0, 255) : this.textColor.getValue());
                        }
                    }
                }
            }
        }
    }

    public void draw(BlockPos pos, double size, Color color, boolean full) {
        if (this.animationMode.getValue() != Mode.Both && this.animationMode.getValue() != Mode.Custom) {
            AxisAlignedBB axisAlignedBB;
            if (this.animationMode.getValue() == Mode.InToOut) {
                axisAlignedBB = PacketMine.mc.world.getBlockState(pos).getSelectedBoundingBox(PacketMine.mc.world, pos).grow(size / 2.0 - 0.5);
            } else if (this.animationMode.getValue() == Mode.Up) {
                AxisAlignedBB bb = PacketMine.mc.world.getBlockState(pos).getSelectedBoundingBox(PacketMine.mc.world, pos);
                axisAlignedBB = new AxisAlignedBB(bb.minX, bb.minY, bb.minZ, bb.maxX, bb.minY + (bb.maxY - bb.minY) * size, bb.maxZ);
            } else if (this.animationMode.getValue() == Mode.Down) {
                AxisAlignedBB bb = PacketMine.mc.world.getBlockState(pos).getSelectedBoundingBox(PacketMine.mc.world, pos);
                axisAlignedBB = new AxisAlignedBB(bb.minX, bb.maxY - (bb.maxY - bb.minY) * size, bb.minZ, bb.maxX, bb.maxY, bb.maxZ);
            } else if (this.animationMode.getValue() == Mode.OutToIn) {
                axisAlignedBB = PacketMine.mc.world.getBlockState(pos).getSelectedBoundingBox(PacketMine.mc.world, pos).grow(-Math.abs(size / 2.0 - 1.0));
            } else if (this.animationMode.getValue() == Mode.None) {
                axisAlignedBB = PacketMine.mc.world.getBlockState(pos).getSelectedBoundingBox(PacketMine.mc.world, pos);
            } else {
                AxisAlignedBB bb = PacketMine.mc.world.getBlockState(pos).getSelectedBoundingBox(PacketMine.mc.world, pos).grow(size / 2.0 - 0.5);
                AxisAlignedBB bb2 = PacketMine.mc.world.getBlockState(pos).getSelectedBoundingBox(PacketMine.mc.world, pos);
                axisAlignedBB = this.animationMode.getValue() == Mode.Horizontal ? new AxisAlignedBB(bb2.minX, bb.minY, bb2.minZ, bb2.maxX, bb.maxY, bb2.maxZ) : new AxisAlignedBB(bb.minX, bb2.minY, bb.minZ, bb.maxX, bb2.maxY, bb.maxZ);
            }
            if (full) {
                axisAlignedBB = PacketMine.mc.world.getBlockState(pos).getSelectedBoundingBox(PacketMine.mc.world, pos);
            }
            if (this.outline.getValue()) {
                RenderUtil.drawBBBox(axisAlignedBB, color, this.outlineAlpha.getValue());
            }
            if (this.box.getValue()) {
                RenderUtil.drawBBFill(axisAlignedBB, color, this.boxAlpha.getValue());
            }
        } else if (this.animationMode.getValue() == Mode.Custom) {
            if (full) {
                AxisAlignedBB axisAlignedBB = PacketMine.mc.world.getBlockState(pos).getSelectedBoundingBox(PacketMine.mc.world, pos);
                if (this.outline.getValue()) {
                    RenderUtil.drawBBBox(axisAlignedBB, color, this.outlineAlpha.getValue());
                }
                if (this.box.getValue()) {
                    RenderUtil.drawBBFill(axisAlignedBB, color, this.boxAlpha.getValue());
                }
            } else {
                AxisAlignedBB axisAlignedBB = PacketMine.mc.world.getBlockState(pos).getSelectedBoundingBox(PacketMine.mc.world, pos).grow((double)(-this.fillStart.getValue()) - size * (double)(1.0f - this.fillStart.getValue()));
                double boxSize = size + (double) this.boxExtend.getValue();
                if (boxSize > 1.0) {
                    boxSize = 1.0;
                }
                AxisAlignedBB axisAlignedBB2 = PacketMine.mc.world.getBlockState(pos).getSelectedBoundingBox(PacketMine.mc.world, pos).grow((double)(-this.boxStart.getValue()) - boxSize * (double)(1.0f - this.boxStart.getValue()));
                if (this.outline.getValue()) {
                    RenderUtil.drawBBBox(axisAlignedBB2, color, this.outlineAlpha.getValue());
                }
                if (this.box.getValue()) {
                    RenderUtil.drawBBFill(axisAlignedBB, color, this.boxAlpha.getValue());
                }
            }
        } else if (full) {
            AxisAlignedBB axisAlignedBB = PacketMine.mc.world.getBlockState(pos).getSelectedBoundingBox(PacketMine.mc.world, pos);
            if (this.outline.getValue()) {
                RenderUtil.drawBBBox(axisAlignedBB, color, this.outlineAlpha.getValue());
            }
            if (this.box.getValue()) {
                RenderUtil.drawBBFill(axisAlignedBB, color, this.boxAlpha.getValue());
            }
        } else {
            AxisAlignedBB axisAlignedBB = PacketMine.mc.world.getBlockState(pos).getSelectedBoundingBox(PacketMine.mc.world, pos).grow(size / 2.0 - 0.5);
            if (this.outline.getValue()) {
                RenderUtil.drawBBBox(axisAlignedBB, color, this.outlineAlpha.getValue());
            }
            if (this.box.getValue()) {
                RenderUtil.drawBBFill(axisAlignedBB, color, this.boxAlpha.getValue());
            }
            axisAlignedBB = PacketMine.mc.world.getBlockState(pos).getSelectedBoundingBox(PacketMine.mc.world, pos).grow(-Math.abs(size / 2.0 - 1.0));
            if (this.outline.getValue()) {
                RenderUtil.drawBBBox(axisAlignedBB, color, this.outlineAlpha.getValue());
            }
            if (this.box.getValue()) {
                RenderUtil.drawBBFill(axisAlignedBB, color, this.boxAlpha.getValue());
            }
        }
    }

    private int getTool(BlockPos pos) {
        if (this.hotBar.getValue()) {
            int index = -1;
            float CurrentFastest = 1.0f;
            for (int i = 0; i < 9; ++i) {
                float destroySpeed;
                float digSpeed;
                ItemStack stack = PacketMine.mc.player.inventory.getStackInSlot(i);
                if (stack == ItemStack.EMPTY || !((digSpeed = (float)EnchantmentHelper.getEnchantmentLevel(Enchantments.EFFICIENCY, stack)) + (destroySpeed = stack.getDestroySpeed(PacketMine.mc.world.getBlockState(pos))) > CurrentFastest)) continue;
                CurrentFastest = digSpeed + destroySpeed;
                index = 36 + i;
            }
            return index;
        }
        AtomicInteger slot = new AtomicInteger();
        slot.set(-1);
        float CurrentFastest = 1.0f;
        for (Map.Entry<Integer, ItemStack> entry : InventoryUtil.getInventoryAndHotbarSlots().entrySet()) {
            float destroySpeed;
            float digSpeed;
            if (entry.getValue().getItem() instanceof ItemAir || !((digSpeed = (float)EnchantmentHelper.getEnchantmentLevel(Enchantments.EFFICIENCY, entry.getValue())) + (destroySpeed = entry.getValue().getDestroySpeed(PacketMine.mc.world.getBlockState(pos))) > CurrentFastest)) continue;
            CurrentFastest = digSpeed + destroySpeed;
            slot.set(entry.getKey());
        }
        return slot.get();
    }

    @SubscribeEvent
    public void onClickBlock(BlockEvent event) {
        if (PacketMine.fullNullCheck() || PacketMine.mc.player.isCreative()) {
            return;
        }
        event.setCanceled(true);
        if (godBlocks.contains(PacketMine.mc.world.getBlockState(event.getBlockPos()).getBlock()) && this.godCancel.getValue()) {
            return;
        }
        if (event.getBlockPos().equals(breakPos)) {
            return;
        }
        breakPos = event.getBlockPos();
        this.mineTimer.reset();
        this.animationTime.reset();
        if (godBlocks.contains(PacketMine.mc.world.getBlockState(event.getBlockPos()).getBlock())) {
            return;
        }
        if (this.restart.getValue() && !this.instant.getValue()) {
            this.first = true;
        }
        this.firstTimer.reset();
        PacketMine.mc.player.connection.sendPacket(new CPacketPlayerDigging(CPacketPlayerDigging.Action.START_DESTROY_BLOCK, breakPos, BlockUtil.getRayTraceFacing(breakPos)));
        if (this.doubleBreak.getValue()) {
            PacketMine.mc.player.connection.sendPacket(new CPacketPlayerDigging(CPacketPlayerDigging.Action.STOP_DESTROY_BLOCK, breakPos, BlockUtil.getRayTraceFacing(breakPos)));
            PacketMine.mc.player.connection.sendPacket(new CPacketPlayerDigging(CPacketPlayerDigging.Action.START_DESTROY_BLOCK, breakPos, BlockUtil.getRayTraceFacing(breakPos)));
        }
        if (this.swing.getValue()) {
            PacketMine.mc.player.swingArm(EnumHand.MAIN_HAND);
        }
        this.breakNumber = 0;
    }

    private static boolean canBreak(BlockPos pos) {
        IBlockState blockState = PacketMine.mc.world.getBlockState(pos);
        Block block = blockState.getBlock();
        return block.getBlockHardness(blockState, PacketMine.mc.world, pos) != -1.0f;
    }

    public static float getBlockStrength(BlockPos position, ItemStack itemStack) {
        IBlockState state = PacketMine.mc.world.getBlockState(position);
        float hardness = state.getBlockHardness(PacketMine.mc.world, position);
        if (hardness < 0.0f) {
            return 0.0f;
        }
        if (!PacketMine.canBreak(position)) {
            return PacketMine.getDigSpeed(state, itemStack) / hardness / 100.0f;
        }
        return PacketMine.getDigSpeed(state, itemStack) / hardness / 30.0f;
    }

    public static float getDigSpeed(IBlockState state, ItemStack itemStack) {
        int efficiencyModifier;
        float digSpeed = PacketMine.getDestroySpeed(state, itemStack);
        if (digSpeed > 1.0f && (efficiencyModifier = EnchantmentHelper.getEnchantmentLevel(Enchantments.EFFICIENCY, itemStack)) > 0 && !itemStack.isEmpty()) {
            digSpeed = (float)((double)digSpeed + (StrictMath.pow(efficiencyModifier, 2.0) + 1.0));
        }
        if (PacketMine.mc.player.isPotionActive(MobEffects.HASTE)) {
            digSpeed *= 1.0f + (float)(PacketMine.mc.player.getActivePotionEffect(MobEffects.HASTE).getAmplifier() + 1) * 0.2f;
        }
        if (PacketMine.mc.player.isPotionActive(MobEffects.MINING_FATIGUE)) {
            float fatigueScale;
            switch (PacketMine.mc.player.getActivePotionEffect(MobEffects.MINING_FATIGUE).getAmplifier()) {
                case 0: {
                    fatigueScale = 0.3f;
                    break;
                }
                case 1: {
                    fatigueScale = 0.09f;
                    break;
                }
                case 2: {
                    fatigueScale = 0.0027f;
                    break;
                }
                default: {
                    fatigueScale = 8.1E-4f;
                }
            }
            digSpeed *= fatigueScale;
        }
        if (PacketMine.mc.player.isInsideOfMaterial(Material.WATER) && !EnchantmentHelper.getAquaAffinityModifier(PacketMine.mc.player)) {
            digSpeed /= 5.0f;
        }
        return Math.max(digSpeed, 0.0f);
    }

    public static float getDestroySpeed(IBlockState state, ItemStack itemStack) {
        float destroySpeed = 1.0f;
        if (itemStack != null && !itemStack.isEmpty()) {
            destroySpeed *= itemStack.getDestroySpeed(state);
        }
        return destroySpeed;
    }

    public static enum ColorMode {
        Custom,
        Progress

    }

    public static enum Mode {
        Down,
        Up,
        InToOut,
        OutToIn,
        Both,
        Vertical,
        Horizontal,
        Custom,
        None

    }
}

