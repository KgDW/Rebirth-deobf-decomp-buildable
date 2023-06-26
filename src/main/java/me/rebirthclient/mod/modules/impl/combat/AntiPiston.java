package me.rebirthclient.mod.modules.impl.combat;

import me.rebirthclient.api.managers.Managers;
import me.rebirthclient.api.util.BlockUtil;
import me.rebirthclient.api.util.EntityUtil;
import me.rebirthclient.api.util.InventoryUtil;
import me.rebirthclient.mod.modules.Category;
import me.rebirthclient.mod.modules.Module;
import me.rebirthclient.mod.modules.settings.Setting;
import net.minecraft.block.Block;
import net.minecraft.block.BlockDirectional;
import net.minecraft.block.BlockObsidian;
import net.minecraft.block.BlockPistonBase;
import net.minecraft.block.properties.IProperty;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;

public class AntiPiston
extends Module {
    public static AntiPiston INSTANCE;
    public final Setting<Boolean> rotate = this.add(new Setting<>("Rotate", true));
    public final Setting<Boolean> packet = this.add(new Setting<>("Packet", true));
    private final Setting<Double> maxSelfSpeed = this.add(new Setting<>("MaxSelfSpeed", 6.0, 1.0, 30.0));
    public final Setting<Boolean> helper = this.add(new Setting<>("Helper", true));
    public final Setting<Boolean> trap = this.add(new Setting<>("Trap", true).setParent());
    private final Setting<Boolean> onlyBurrow = this.add(new Setting<>("OnlyBurrow", true, v -> this.trap.isOpen()).setParent());
    private final Setting<Boolean> whenDouble = this.add(new Setting<>("WhenDouble", true, v -> this.onlyBurrow.isOpen()));

    public AntiPiston() {
        super("AntiPiston", "Trap self when piston kick", Category.COMBAT);
        INSTANCE = this;
    }

    @Override
    public void onUpdate() {
        if (AntiPiston.fullNullCheck() || !AntiPiston.mc.player.onGround || Managers.SPEED.getPlayerSpeed(AntiPiston.mc.player) > this.maxSelfSpeed.getValue()) {
            return;
        }
        this.block();
    }

    private void block() {
        BlockPos pos = EntityUtil.getPlayerPos();
        if (this.getBlock(pos.up(2)) == Blocks.OBSIDIAN || this.getBlock(pos.up(2)) == Blocks.BEDROCK) {
            return;
        }
        int progress = 0;
        if (this.whenDouble.getValue()) {
            for (EnumFacing i : EnumFacing.VALUES) {
                if (i == EnumFacing.DOWN || i == EnumFacing.UP || !(this.getBlock(pos.offset(i).up()) instanceof BlockPistonBase) || ((EnumFacing)AntiPiston.mc.world.getBlockState(pos.offset(i).up()).getValue((IProperty)BlockDirectional.FACING)).getOpposite() != i) continue;
                ++progress;
            }
        }
        for (EnumFacing i : EnumFacing.VALUES) {
            if (i == EnumFacing.DOWN || i == EnumFacing.UP || !(this.getBlock(pos.offset(i).up()) instanceof BlockPistonBase) || ((EnumFacing)AntiPiston.mc.world.getBlockState(pos.offset(i).up()).getValue((IProperty)BlockDirectional.FACING)).getOpposite() != i) continue;
            this.placeBlock(pos.up().offset(i, -1));
            if (this.trap.getValue() && (this.getBlock(pos) != Blocks.AIR || !this.onlyBurrow.getValue() || progress >= 2)) {
                this.placeBlock(pos.up(2));
                if (!BlockUtil.canPlaceEnum(pos.up(2))) {
                    for (EnumFacing i2 : EnumFacing.VALUES) {
                        if (!AntiPiston.canPlace(pos.offset(i2).up(2))) continue;
                        this.placeBlock(pos.offset(i2).up(2));
                        break;
                    }
                }
            }
            if (BlockUtil.canPlaceEnum(pos.up().offset(i, -1)) || !this.helper.getValue()) continue;
            if (BlockUtil.canPlaceEnum(pos.offset(i, -1))) {
                this.placeBlock(pos.offset(i, -1));
                continue;
            }
            this.placeBlock(pos.offset(i, -1).down());
        }
    }

    private Block getBlock(BlockPos block) {
        return AntiPiston.mc.world.getBlockState(block).getBlock();
    }

    private void placeBlock(BlockPos pos) {
        if (!AntiPiston.canPlace(pos)) {
            return;
        }
        int old = AntiPiston.mc.player.inventory.currentItem;
        if (InventoryUtil.findHotbarClass(BlockObsidian.class) == -1) {
            return;
        }
        InventoryUtil.doSwap(InventoryUtil.findHotbarClass(BlockObsidian.class));
        BlockUtil.placeBlock(pos, EnumHand.MAIN_HAND, this.rotate.getValue(), this.packet.getValue());
        InventoryUtil.doSwap(old);
    }

    public static boolean canPlace(BlockPos pos) {
        if (!BlockUtil.canBlockFacing(pos)) {
            return false;
        }
        if (!BlockUtil.canReplace(pos)) {
            return false;
        }
        return !BlockUtil.checkEntity(pos);
    }
}

