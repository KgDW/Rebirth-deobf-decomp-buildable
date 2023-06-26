package me.rebirthclient.mod.modules.impl.player;

import java.awt.Color;
import me.rebirthclient.api.events.impl.BlockEvent;
import me.rebirthclient.api.events.impl.MotionEvent;
import me.rebirthclient.api.events.impl.PacketEvent;
import me.rebirthclient.api.events.impl.Render3DEvent;
import me.rebirthclient.api.util.math.MathUtil;
import me.rebirthclient.api.util.render.RenderUtil;
import me.rebirthclient.mod.modules.Category;
import me.rebirthclient.mod.modules.Module;
import me.rebirthclient.mod.modules.impl.combat.PacketMine;
import me.rebirthclient.mod.modules.settings.Setting;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Enchantments;
import net.minecraft.init.MobEffects;
import net.minecraft.inventory.ClickType;
import net.minecraft.item.ItemStack;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.CPacketClickWindow;
import net.minecraft.network.play.client.CPacketConfirmTransaction;
import net.minecraft.network.play.client.CPacketHeldItemChange;
import net.minecraft.network.play.client.CPacketPlayerDigging;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class SpeedMine
extends Module {
    private static float mineDamage;
    private final Setting<Mode> mode = this.register(new Setting<>("Mode", Mode.Packet));
    private final Setting<Float> startDamage = this.register(new Setting<>("StartDamage", 0.1f, 0.0f, 1.0f, v -> this.mode.getValue() == Mode.Damage));
    private final Setting<Float> endDamage = this.register(new Setting<>("EndDamage", 0.9f, 0.0f, 1.0f, v -> this.mode.getValue() == Mode.Damage));
    private final Setting<Float> range = this.register(new Setting<>("Range", 6.2f, 3.0f, 10.0f, v -> this.mode.getValue() == Mode.Packet));
    private final Setting<Boolean> rotate = this.register(new Setting<>("Rotate", false, v -> this.mode.getValue() == Mode.Packet));
    private final Setting<Boolean> strict = this.register(new Setting<>("Strict", false, v -> this.mode.getValue() == Mode.Packet));
    private final Setting<Boolean> strictReMine = this.register(new Setting<>("StrictBreak", false, v -> this.mode.getValue() == Mode.Packet));
    private final Setting<Boolean> render = this.add(new Setting<>("Render", true, v -> this.mode.getValue() == Mode.Packet).setParent());
    private final Setting<PacketMine.Mode> animationMode = this.add(new Setting<>("AnimationMode", PacketMine.Mode.Up, v -> this.render.isOpen() && this.mode.getValue() == Mode.Packet));
    private final Setting<Float> fillStart = this.add(new Setting<>("FillStart", 0.2f, 0.0f, 1.0f, v -> this.render.isOpen() && this.animationMode.getValue() == PacketMine.Mode.Custom && this.mode.getValue() == Mode.Packet));
    private final Setting<Float> boxStart = this.add(new Setting<>("BoxStart", 0.4f, 0.0f, 1.0f, v -> this.render.isOpen() && this.animationMode.getValue() == PacketMine.Mode.Custom && this.mode.getValue() == Mode.Packet));
    private final Setting<Float> boxExtend = this.add(new Setting<>("BoxExtend", 0.2f, 0.0f, 1.0f, v -> this.render.isOpen() && this.animationMode.getValue() == PacketMine.Mode.Custom && this.mode.getValue() == Mode.Packet));
    private final Setting<Boolean> box = this.add(new Setting<>("Box", true, v -> this.render.isOpen() && this.mode.getValue() == Mode.Packet).setParent());
    private final Setting<Integer> boxAlpha = this.add(new Setting<>("BoxAlpha", 100, 0, 255, v -> this.box.isOpen() && this.render.isOpen() && this.mode.getValue() == Mode.Packet));
    private final Setting<Boolean> outline = this.add(new Setting<>("Outline", true, v -> this.render.isOpen() && this.mode.getValue() == Mode.Packet).setParent());
    private final Setting<Integer> outlineAlpha = this.add(new Setting<>("OutlineAlpha", 100, 0, 255, v -> this.outline.isOpen() && this.render.isOpen() && this.mode.getValue() == Mode.Packet));
    private final Setting<PacketMine.ColorMode> colorMode = this.add(new Setting<>("ColorMode", PacketMine.ColorMode.Progress, v -> this.render.isOpen() && this.mode.getValue() == Mode.Packet));
    private final Setting<Color> color = this.add(new Setting<>("Color", new Color(189, 212, 255), v -> this.render.isOpen() && this.mode.getValue() == Mode.Packet && this.colorMode.getValue() == PacketMine.ColorMode.Custom).hideAlpha());
    private BlockPos minePosition;
    private EnumFacing mineFacing;
    private int mineBreaks;

    public SpeedMine() {
        super("SpeedMine", "Allows you to dig-quickly", Category.PLAYER);
    }

    @Override
    public void onUpdate() {
        if (!SpeedMine.mc.player.capabilities.isCreativeMode) {
            if (this.minePosition != null) {
                double mineDistance = SpeedMine.mc.player.getDistance((double)this.minePosition.getX() + 0.5, (double)this.minePosition.getY() + 0.5, (double)this.minePosition.getZ() + 0.5);
                if (this.mineBreaks >= 2 && this.strictReMine.getValue() || mineDistance > (double) this.range.getValue() || SpeedMine.mc.world.isAirBlock(this.minePosition)) {
                    this.minePosition = null;
                    this.mineFacing = null;
                    mineDamage = 0.0f;
                    this.mineBreaks = 0;
                }
            }
            if (this.mode.getValue() == Mode.Damage) {
                if (SpeedMine.mc.playerController.curBlockDamageMP < this.startDamage.getValue()) {
                    SpeedMine.mc.playerController.curBlockDamageMP = this.startDamage.getValue();
                }
                if (SpeedMine.mc.playerController.curBlockDamageMP >= this.endDamage.getValue()) {
                    SpeedMine.mc.playerController.curBlockDamageMP = 1.0f;
                }
            } else if (this.mode.getValue() == Mode.Packet) {
                if (this.minePosition != null) {
                    if (mineDamage >= 1.0f) {
                        ItemStack itemstack;
                        short nextTransactionID;
                        int previousSlot = SpeedMine.mc.player.inventory.currentItem;
                        int swapSlot = this.getTool(this.minePosition);
                        if (swapSlot == -1) {
                            return;
                        }
                        if (this.strict.getValue()) {
                            nextTransactionID = SpeedMine.mc.player.openContainer.getNextTransactionID(SpeedMine.mc.player.inventory);
                            itemstack = SpeedMine.mc.player.openContainer.slotClick(swapSlot, SpeedMine.mc.player.inventory.currentItem, ClickType.SWAP, SpeedMine.mc.player);
                            SpeedMine.mc.player.connection.sendPacket(new CPacketClickWindow(SpeedMine.mc.player.inventoryContainer.windowId, swapSlot, SpeedMine.mc.player.inventory.currentItem, ClickType.SWAP, itemstack, nextTransactionID));
                        } else {
                            SpeedMine.mc.player.connection.sendPacket(new CPacketHeldItemChange(swapSlot));
                        }
                        SpeedMine.mc.player.connection.sendPacket(new CPacketPlayerDigging(CPacketPlayerDigging.Action.STOP_DESTROY_BLOCK, this.minePosition, this.mineFacing));
                        SpeedMine.mc.player.connection.sendPacket(new CPacketPlayerDigging(CPacketPlayerDigging.Action.ABORT_DESTROY_BLOCK, this.minePosition, EnumFacing.UP));
                        if (this.strict.getValue()) {
                            SpeedMine.mc.player.connection.sendPacket(new CPacketPlayerDigging(CPacketPlayerDigging.Action.START_DESTROY_BLOCK, this.minePosition, this.mineFacing));
                        }
                        SpeedMine.mc.player.connection.sendPacket(new CPacketPlayerDigging(CPacketPlayerDigging.Action.STOP_DESTROY_BLOCK, this.minePosition, this.mineFacing));
                        if (previousSlot != -1) {
                            if (this.strict.getValue()) {
                                nextTransactionID = SpeedMine.mc.player.openContainer.getNextTransactionID(SpeedMine.mc.player.inventory);
                                itemstack = SpeedMine.mc.player.openContainer.slotClick(swapSlot, SpeedMine.mc.player.inventory.currentItem, ClickType.SWAP, SpeedMine.mc.player);
                                SpeedMine.mc.player.connection.sendPacket(new CPacketClickWindow(SpeedMine.mc.player.inventoryContainer.windowId, swapSlot, SpeedMine.mc.player.inventory.currentItem, ClickType.SWAP, itemstack, nextTransactionID));
                                SpeedMine.mc.player.connection.sendPacket(new CPacketConfirmTransaction(SpeedMine.mc.player.inventoryContainer.windowId, nextTransactionID, true));
                            } else {
                                SpeedMine.mc.player.connection.sendPacket(new CPacketHeldItemChange(previousSlot));
                            }
                        }
                        mineDamage = 0.0f;
                        ++this.mineBreaks;
                    }
                    mineDamage += this.getBlockStrength(SpeedMine.mc.world.getBlockState(this.minePosition), this.minePosition);
                } else {
                    mineDamage = 0.0f;
                }
            }
        }
    }

    public float getBlockStrength(IBlockState state, BlockPos position) {
        float hardness = state.getBlockHardness(SpeedMine.mc.world, position);
        if (hardness < 0.0f) {
            return 0.0f;
        }
        if (!this.canBreak(position)) {
            return this.getDigSpeed(state) / hardness / 100.0f;
        }
        return this.getDigSpeed(state) / hardness / 30.0f;
    }

    public float getDigSpeed(IBlockState state) {
        ItemStack itemstack;
        int efficiencyModifier;
        float digSpeed = this.getDestroySpeed(state);
        if (digSpeed > 1.0f && (efficiencyModifier = EnchantmentHelper.getEnchantmentLevel(Enchantments.EFFICIENCY, itemstack = this.getTool2(state))) > 0 && !itemstack.isEmpty()) {
            digSpeed = (float)((double)digSpeed + (StrictMath.pow(efficiencyModifier, 2.0) + 1.0));
        }
        if (SpeedMine.mc.player.isPotionActive(MobEffects.HASTE)) {
            digSpeed *= 1.0f + (float)(SpeedMine.mc.player.getActivePotionEffect(MobEffects.HASTE).getAmplifier() + 1) * 0.2f;
        }
        if (SpeedMine.mc.player.isPotionActive(MobEffects.MINING_FATIGUE)) {
            float fatigueScale;
            switch (SpeedMine.mc.player.getActivePotionEffect(MobEffects.MINING_FATIGUE).getAmplifier()) {
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
        if (SpeedMine.mc.player.isInsideOfMaterial(Material.WATER) && !EnchantmentHelper.getAquaAffinityModifier(SpeedMine.mc.player)) {
            digSpeed /= 5.0f;
        }
        if (!SpeedMine.mc.player.onGround) {
            digSpeed /= 5.0f;
        }
        return Math.max(digSpeed, 0.0f);
    }

    public float getDestroySpeed(IBlockState state) {
        float destroySpeed = 1.0f;
        if (this.getTool2(state) != null && !this.getTool2(state).isEmpty()) {
            destroySpeed *= this.getTool2(state).getDestroySpeed(state);
        }
        return destroySpeed;
    }

    @Override
    public void onDisable() {
        this.minePosition = null;
        this.mineFacing = null;
        mineDamage = 0.0f;
        this.mineBreaks = 0;
    }

    @Override
    @SubscribeEvent
    public void onRender3D(Render3DEvent event) {
        if (this.mode.getValue() == Mode.Packet && this.minePosition != null && !SpeedMine.mc.world.isAirBlock(this.minePosition)) {
            double size = mineDamage;
            if (size <= 0.0) {
                size = 0.0;
            } else if (size >= 1.0) {
                size = 1.0;
            }
            this.draw(this.minePosition, mineDamage, this.colorMode.getValue() == PacketMine.ColorMode.Custom ? this.color.getValue() : new Color((int)(255.0 * Math.abs(size - 1.0)), (int)(255.0 * size), 0));
        }
    }

    public void draw(BlockPos pos, double size, Color color) {
        if (size > 1.0) {
            size = 1.0;
        }
        if (this.animationMode.getValue() != PacketMine.Mode.Both && this.animationMode.getValue() != PacketMine.Mode.Custom) {
            AxisAlignedBB axisAlignedBB;
            if (this.animationMode.getValue() == PacketMine.Mode.InToOut) {
                axisAlignedBB = SpeedMine.mc.world.getBlockState(pos).getSelectedBoundingBox(SpeedMine.mc.world, pos).grow(size / 2.0 - 0.5);
            } else if (this.animationMode.getValue() == PacketMine.Mode.Up) {
                AxisAlignedBB bb = SpeedMine.mc.world.getBlockState(pos).getSelectedBoundingBox(SpeedMine.mc.world, pos);
                axisAlignedBB = new AxisAlignedBB(bb.minX, bb.minY, bb.minZ, bb.maxX, bb.minY + (bb.maxY - bb.minY) * size, bb.maxZ);
            } else if (this.animationMode.getValue() == PacketMine.Mode.Down) {
                AxisAlignedBB bb = SpeedMine.mc.world.getBlockState(pos).getSelectedBoundingBox(SpeedMine.mc.world, pos);
                axisAlignedBB = new AxisAlignedBB(bb.minX, bb.maxY - (bb.maxY - bb.minY) * size, bb.minZ, bb.maxX, bb.maxY, bb.maxZ);
            } else if (this.animationMode.getValue() == PacketMine.Mode.OutToIn) {
                axisAlignedBB = SpeedMine.mc.world.getBlockState(pos).getSelectedBoundingBox(SpeedMine.mc.world, pos).grow(-Math.abs(size / 2.0 - 1.0));
            } else if (this.animationMode.getValue() == PacketMine.Mode.None) {
                axisAlignedBB = SpeedMine.mc.world.getBlockState(pos).getSelectedBoundingBox(SpeedMine.mc.world, pos);
            } else {
                AxisAlignedBB bb = SpeedMine.mc.world.getBlockState(pos).getSelectedBoundingBox(SpeedMine.mc.world, pos).grow(size / 2.0 - 0.5);
                AxisAlignedBB bb2 = SpeedMine.mc.world.getBlockState(pos).getSelectedBoundingBox(SpeedMine.mc.world, pos);
                axisAlignedBB = this.animationMode.getValue() == PacketMine.Mode.Horizontal ? new AxisAlignedBB(bb2.minX, bb.minY, bb2.minZ, bb2.maxX, bb.maxY, bb2.maxZ) : new AxisAlignedBB(bb.minX, bb2.minY, bb.minZ, bb.maxX, bb2.maxY, bb.maxZ);
            }
            if (this.outline.getValue()) {
                RenderUtil.drawBBBox(axisAlignedBB, color, this.outlineAlpha.getValue());
            }
            if (this.box.getValue()) {
                RenderUtil.drawBBFill(axisAlignedBB, color, this.boxAlpha.getValue());
            }
        } else if (this.animationMode.getValue() == PacketMine.Mode.Custom) {
            AxisAlignedBB axisAlignedBB = SpeedMine.mc.world.getBlockState(pos).getSelectedBoundingBox(SpeedMine.mc.world, pos).grow((double)(-this.fillStart.getValue()) - size * (double)(1.0f - this.fillStart.getValue()));
            double boxSize = size + (double) this.boxExtend.getValue();
            if (boxSize > 1.0) {
                boxSize = 1.0;
            }
            AxisAlignedBB axisAlignedBB2 = SpeedMine.mc.world.getBlockState(pos).getSelectedBoundingBox(SpeedMine.mc.world, pos).grow((double)(-this.boxStart.getValue()) - boxSize * (double)(1.0f - this.boxStart.getValue()));
            if (this.outline.getValue()) {
                RenderUtil.drawBBBox(axisAlignedBB2, color, this.outlineAlpha.getValue());
            }
            if (this.box.getValue()) {
                RenderUtil.drawBBFill(axisAlignedBB, color, this.boxAlpha.getValue());
            }
        } else {
            AxisAlignedBB axisAlignedBB = SpeedMine.mc.world.getBlockState(pos).getSelectedBoundingBox(SpeedMine.mc.world, pos).grow(size / 2.0 - 0.5);
            if (this.outline.getValue()) {
                RenderUtil.drawBBBox(axisAlignedBB, color, this.outlineAlpha.getValue());
            }
            if (this.box.getValue()) {
                RenderUtil.drawBBFill(axisAlignedBB, color, this.boxAlpha.getValue());
            }
            axisAlignedBB = SpeedMine.mc.world.getBlockState(pos).getSelectedBoundingBox(SpeedMine.mc.world, pos).grow(-Math.abs(size / 2.0 - 1.0));
            if (this.outline.getValue()) {
                RenderUtil.drawBBBox(axisAlignedBB, color, this.outlineAlpha.getValue());
            }
            if (this.box.getValue()) {
                RenderUtil.drawBBFill(axisAlignedBB, color, this.boxAlpha.getValue());
            }
        }
    }

    @SubscribeEvent
    public void onLeftClickBlock(BlockEvent event) {
        if (!SpeedMine.mc.player.capabilities.isCreativeMode && this.mode.getValue() == Mode.Packet) {
            event.setCanceled(true);
            if (this.canBreak(event.getBlockPos()) && !event.getBlockPos().equals(this.minePosition)) {
                this.minePosition = event.getBlockPos();
                this.mineFacing = event.getEnumFacing();
                mineDamage = 0.0f;
                this.mineBreaks = 0;
                if (this.minePosition != null && this.mineFacing != null) {
                    SpeedMine.mc.player.connection.sendPacket(new CPacketPlayerDigging(CPacketPlayerDigging.Action.START_DESTROY_BLOCK, this.minePosition, this.mineFacing));
                    SpeedMine.mc.player.connection.sendPacket(new CPacketPlayerDigging(CPacketPlayerDigging.Action.ABORT_DESTROY_BLOCK, this.minePosition, EnumFacing.UP));
                }
            }
        }
    }

    @SubscribeEvent
    public void onEntitySync(MotionEvent event) {
        if (this.rotate.getValue() && (double)mineDamage > 0.95 && this.minePosition != null) {
            float[] angle = MathUtil.calcAngle(SpeedMine.mc.player.getPositionEyes(mc.getRenderPartialTicks()), new Vec3d(this.minePosition.add(0.5, 0.5, 0.5)));
            event.setYaw(angle[0]);
            event.setPitch(angle[1]);
        }
    }

    @SubscribeEvent
    public void onPacketSend(PacketEvent.Send event) {
        if (event.getPacket() instanceof CPacketHeldItemChange && this.strict.getValue()) {
            mineDamage = 0.0f;
        }
    }

    private int getTool(BlockPos pos) {
        int index = -1;
        float CurrentFastest = 1.0f;
        for (int i = 0; i < 9; ++i) {
            float destroySpeed;
            float digSpeed;
            ItemStack stack = SpeedMine.mc.player.inventory.getStackInSlot(i);
            if (stack == ItemStack.EMPTY || !((digSpeed = (float)EnchantmentHelper.getEnchantmentLevel(Enchantments.EFFICIENCY, stack)) + (destroySpeed = stack.getDestroySpeed(SpeedMine.mc.world.getBlockState(pos))) > CurrentFastest)) continue;
            CurrentFastest = digSpeed + destroySpeed;
            index = i;
        }
        return index;
    }

    private ItemStack getTool2(IBlockState pos) {
        ItemStack itemStack = null;
        float CurrentFastest = 1.0f;
        for (int i = 0; i < 9; ++i) {
            float destroySpeed;
            float digSpeed;
            ItemStack stack = SpeedMine.mc.player.inventory.getStackInSlot(i);
            if (stack == ItemStack.EMPTY || !((digSpeed = (float)EnchantmentHelper.getEnchantmentLevel(Enchantments.EFFICIENCY, stack)) + (destroySpeed = stack.getDestroySpeed(pos)) > CurrentFastest)) continue;
            CurrentFastest = digSpeed + destroySpeed;
            itemStack = stack;
        }
        return itemStack;
    }

    private boolean canBreak(BlockPos pos) {
        IBlockState blockState = SpeedMine.mc.world.getBlockState(pos);
        Block block = blockState.getBlock();
        return block.getBlockHardness(blockState, SpeedMine.mc.world, pos) != -1.0f;
    }

    public static enum Mode {
        Packet,
        Damage

    }
}

