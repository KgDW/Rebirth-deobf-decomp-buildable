package me.rebirthclient.mod.modules.impl.render;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import me.rebirthclient.api.events.impl.Render3DEvent;
import me.rebirthclient.api.util.EntityUtil;
import me.rebirthclient.api.util.render.RenderUtil;
import me.rebirthclient.mod.modules.Category;
import me.rebirthclient.mod.modules.Module;
import me.rebirthclient.mod.modules.settings.Setting;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.World;

public class CityESP
extends Module {
    private final Setting<Boolean> onlyBurrow = this.add(new Setting<>("OnlyBurrow", false));
    private final Setting<Boolean> outline = this.add(new Setting<>("Outline", true).setParent());
    private final Setting<Integer> outlineAlpha = this.add(new Setting<>("OutlineAlpha", 150, 0, 255, v -> this.outline.isOpen()));
    private final Setting<Boolean> box = this.add(new Setting<>("Box", true).setParent());
    private final Setting<Integer> boxAlpha = this.add(new Setting<>("BoxAlpha", 70, 0, 255, v -> this.box.isOpen()));
    private final Setting<Float> range = this.add(new Setting<>("Range", 7.0f, 1.0f, 12.0f));
    private final Setting<Color> canAttackColor = this.add(new Setting<>("AttackColor", new Color(255, 147, 147)).hideAlpha());
    private final Setting<Color> breakColor = this.add(new Setting<>("Color", new Color(118, 118, 255)).hideAlpha());
    private final Setting<Color> burrowColor = this.add(new Setting<>("BurrowColor", new Color(255, 255, 255)).hideAlpha());
    private final List<BlockPos> burrowPos = new ArrayList<>();

    public CityESP() {
        super("CityESP", "CityESP", Category.RENDER);
    }

    @Override
    public void onRender3D(Render3DEvent event) {
        this.burrowPos.clear();
        for (EntityPlayer player : CityESP.mc.world.playerEntities) {
            if (EntityUtil.invalid(player, this.range.getValue())) continue;
            this.doRender(player);
        }
    }

    private void drawBurrow(BlockPos pos) {
        if (this.burrowPos.contains(pos)) {
            return;
        }
        this.burrowPos.add(pos);
        if (this.getBlock(pos).getBlock() != Blocks.AIR && this.getBlock(pos).getBlock() != Blocks.BEDROCK) {
            AxisAlignedBB axisAlignedBB = CityESP.mc.world.getBlockState(new BlockPos(pos)).getSelectedBoundingBox(CityESP.mc.world, new BlockPos(pos));
            if (this.box.getValue()) {
                RenderUtil.drawBBFill(axisAlignedBB, this.burrowColor.getValue(), this.boxAlpha.getValue());
            }
            if (this.outline.getValue()) {
                RenderUtil.drawBBBox(axisAlignedBB, this.burrowColor.getValue(), this.outlineAlpha.getValue());
            }
        }
    }

    private void doRender(EntityPlayer target) {
        BlockPos pos = EntityUtil.getEntityPos(target);
        this.drawBurrow(new BlockPos(target.posX + 0.1, target.posY + 0.5, target.posZ + 0.1));
        this.drawBurrow(new BlockPos(target.posX - 0.1, target.posY + 0.5, target.posZ + 0.1));
        this.drawBurrow(new BlockPos(target.posX + 0.1, target.posY + 0.5, target.posZ - 0.1));
        this.drawBurrow(new BlockPos(target.posX - 0.1, target.posY + 0.5, target.posZ - 0.1));
        if (this.onlyBurrow.getValue()) {
            return;
        }
        if (this.getBlock(pos.add(-1, 0, 0)).getBlock() != Blocks.AIR && this.getBlock(pos.add(-1, 0, 0)).getBlock() != Blocks.BEDROCK) {
            if (this.getBlock(pos.add(-2, 0, 0)).getBlock() == Blocks.AIR && this.getBlock(pos.add(-2, 1, 0)).getBlock() == Blocks.AIR) {
                this.drawBlock(pos, -1.0, 0.0, 0.0, true);
            } else if (this.getBlock(pos.add(-2, 0, 0)).getBlock() != Blocks.BEDROCK && this.getBlock(pos.add(-2, 1, 0)).getBlock() != Blocks.BEDROCK) {
                this.drawBlock(pos, -1.0, 0.0, 0.0, false);
            }
        }
        if (this.getBlock(pos.add(1, 0, 0)).getBlock() != Blocks.AIR && this.getBlock(pos.add(1, 0, 0)).getBlock() != Blocks.BEDROCK) {
            if (this.getBlock(pos.add(2, 0, 0)).getBlock() == Blocks.AIR && this.getBlock(pos.add(2, 1, 0)).getBlock() == Blocks.AIR) {
                this.drawBlock(pos, 1.0, 0.0, 0.0, true);
            } else if (this.getBlock(pos.add(2, 0, 0)).getBlock() != Blocks.BEDROCK && this.getBlock(pos.add(2, 1, 0)).getBlock() != Blocks.BEDROCK) {
                this.drawBlock(pos, 1.0, 0.0, 0.0, false);
            }
        }
        if (this.getBlock(pos.add(-1, 0, 0)).getBlock() != Blocks.BEDROCK && this.getBlock(pos.add(-2, 1, 0)).getBlock() != Blocks.BEDROCK && this.getBlock(pos.add(-2, 0, 0)).getBlock() != Blocks.AIR && this.getBlock(pos.add(-2, 0, 0)).getBlock() != Blocks.BEDROCK) {
            this.drawBlock(pos, -2.0, 0.0, 0.0, this.getBlock(pos.add(-1, 0, 0)).getBlock() == Blocks.AIR && this.getBlock(pos.add(-2, 1, 0)).getBlock() == Blocks.AIR);
        }
        if (this.getBlock(pos.add(-1, 0, 0)).getBlock() != Blocks.BEDROCK && this.getBlock(pos.add(-2, 0, 0)).getBlock() != Blocks.BEDROCK && this.getBlock(pos.add(-2, 1, 0)).getBlock() != Blocks.AIR && this.getBlock(pos.add(-2, 1, 0)).getBlock() != Blocks.BEDROCK) {
            this.drawBlock(pos, -2.0, 1.0, 0.0, this.getBlock(pos.add(-1, 0, 0)).getBlock() == Blocks.AIR && this.getBlock(pos.add(-2, 0, 0)).getBlock() == Blocks.AIR);
        }
        if (this.getBlock(pos.add(1, 0, 0)).getBlock() != Blocks.BEDROCK && this.getBlock(pos.add(2, 1, 0)).getBlock() != Blocks.BEDROCK && this.getBlock(pos.add(2, 0, 0)).getBlock() != Blocks.AIR && this.getBlock(pos.add(2, 0, 0)).getBlock() != Blocks.BEDROCK) {
            this.drawBlock(pos, 2.0, 0.0, 0.0, this.getBlock(pos.add(1, 0, 0)).getBlock() == Blocks.AIR && this.getBlock(pos.add(2, 1, 0)).getBlock() == Blocks.AIR);
        }
        if (this.getBlock(pos.add(1, 0, 0)).getBlock() != Blocks.BEDROCK && this.getBlock(pos.add(2, 0, 0)).getBlock() != Blocks.BEDROCK && this.getBlock(pos.add(2, 1, 0)).getBlock() != Blocks.AIR && this.getBlock(pos.add(2, 1, 0)).getBlock() != Blocks.BEDROCK) {
            this.drawBlock(pos, 2.0, 1.0, 0.0, this.getBlock(pos.add(1, 0, 0)).getBlock() == Blocks.AIR && this.getBlock(pos.add(2, 0, 0)).getBlock() == Blocks.AIR);
        }
        if (this.getBlock(pos.add(0, 0, 1)).getBlock() != Blocks.AIR && this.getBlock(pos.add(0, 0, 1)).getBlock() != Blocks.BEDROCK) {
            if (this.getBlock(pos.add(0, 0, 2)).getBlock() == Blocks.AIR && this.getBlock(pos.add(0, 1, 2)).getBlock() == Blocks.AIR) {
                this.drawBlock(pos, 0.0, 0.0, 1.0, true);
            } else if (this.getBlock(pos.add(0, 0, 2)).getBlock() != Blocks.BEDROCK && this.getBlock(pos.add(0, 1, 2)).getBlock() != Blocks.BEDROCK) {
                this.drawBlock(pos, 0.0, 0.0, 1.0, false);
            }
        }
        if (this.getBlock(pos.add(0, 0, -1)).getBlock() != Blocks.AIR && this.getBlock(pos.add(0, 0, -1)).getBlock() != Blocks.BEDROCK) {
            if (this.getBlock(pos.add(0, 0, -2)).getBlock() == Blocks.AIR && this.getBlock(pos.add(0, 1, -2)).getBlock() == Blocks.AIR) {
                this.drawBlock(pos, 0.0, 0.0, -1.0, true);
            } else if (this.getBlock(pos.add(0, 0, -2)).getBlock() != Blocks.BEDROCK && this.getBlock(pos.add(0, 1, -2)).getBlock() != Blocks.BEDROCK) {
                this.drawBlock(pos, 0.0, 0.0, -1.0, false);
            }
        }
        if (this.getBlock(pos.add(0, 0, 1)).getBlock() != Blocks.BEDROCK && this.getBlock(pos.add(0, 1, 2)).getBlock() != Blocks.BEDROCK && this.getBlock(pos.add(0, 0, 2)).getBlock() != Blocks.AIR && this.getBlock(pos.add(0, 0, 2)).getBlock() != Blocks.BEDROCK) {
            this.drawBlock(pos, 0.0, 0.0, 2.0, this.getBlock(pos.add(0, 0, 1)).getBlock() == Blocks.AIR && this.getBlock(pos.add(0, 1, 2)).getBlock() == Blocks.AIR);
        }
        if (this.getBlock(pos.add(0, 0, 1)).getBlock() != Blocks.BEDROCK && this.getBlock(pos.add(0, 0, 2)).getBlock() != Blocks.BEDROCK && this.getBlock(pos.add(0, 1, 2)).getBlock() != Blocks.AIR && this.getBlock(pos.add(0, 1, 2)).getBlock() != Blocks.BEDROCK) {
            this.drawBlock(pos, 0.0, 1.0, 2.0, this.getBlock(pos.add(0, 0, 1)).getBlock() == Blocks.AIR && this.getBlock(pos.add(0, 0, 2)).getBlock() == Blocks.AIR);
        }
        if (this.getBlock(pos.add(0, 0, -1)).getBlock() != Blocks.BEDROCK && this.getBlock(pos.add(0, 1, -2)).getBlock() != Blocks.BEDROCK && this.getBlock(pos.add(0, 0, -2)).getBlock() != Blocks.AIR && this.getBlock(pos.add(0, 0, -2)).getBlock() != Blocks.BEDROCK) {
            this.drawBlock(pos, 0.0, 0.0, -2.0, this.getBlock(pos.add(0, 0, -1)).getBlock() == Blocks.AIR && this.getBlock(pos.add(0, 1, -2)).getBlock() == Blocks.AIR);
        }
        if (this.getBlock(pos.add(0, 0, -1)).getBlock() != Blocks.BEDROCK && this.getBlock(pos.add(0, 0, -2)).getBlock() != Blocks.BEDROCK && this.getBlock(pos.add(0, 1, -2)).getBlock() != Blocks.AIR && this.getBlock(pos.add(0, 1, -2)).getBlock() != Blocks.BEDROCK) {
            this.drawBlock(pos, 0.0, 1.0, -2.0, this.getBlock(pos.add(0, 0, -1)).getBlock() == Blocks.AIR && this.getBlock(pos.add(0, 0, -2)).getBlock() == Blocks.AIR);
        }
    }

    private void drawBlock(BlockPos pos, double x, double y, double z, boolean red) {
        if (CityESP.mc.world.getBlockState(pos = pos.add(x, y, z)).getBlock() == Blocks.AIR) {
            return;
        }
        if (CityESP.mc.world.getBlockState(pos).getBlock() == Blocks.FIRE) {
            return;
        }
        AxisAlignedBB axisAlignedBB = CityESP.mc.world.getBlockState(pos).getSelectedBoundingBox(CityESP.mc.world, pos);
        if (red) {
            if (this.outline.getValue()) {
                RenderUtil.drawBBBox(axisAlignedBB, this.canAttackColor.getValue(), this.outlineAlpha.getValue());
            }
            if (this.box.getValue()) {
                RenderUtil.drawBBFill(axisAlignedBB, this.canAttackColor.getValue(), this.boxAlpha.getValue());
            }
            return;
        }
        if (this.outline.getValue()) {
            RenderUtil.drawBBBox(axisAlignedBB, this.breakColor.getValue(), this.outlineAlpha.getValue());
        }
        if (this.box.getValue()) {
            RenderUtil.drawBBFill(axisAlignedBB, this.breakColor.getValue(), this.boxAlpha.getValue());
        }
    }

    private IBlockState getBlock(BlockPos block) {
        return CityESP.mc.world.getBlockState(block);
    }
}

