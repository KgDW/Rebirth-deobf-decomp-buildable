package me.rebirthclient.mod.modules.impl.render;

import java.awt.Color;
import java.util.ArrayList;
import me.rebirthclient.api.events.impl.Render3DEvent;
import me.rebirthclient.api.util.BlockUtil;
import me.rebirthclient.api.util.Timer;
import me.rebirthclient.api.util.render.RenderUtil;
import me.rebirthclient.mod.modules.Category;
import me.rebirthclient.mod.modules.Module;
import me.rebirthclient.mod.modules.settings.Setting;
import net.minecraft.block.BlockEndPortalFrame;
import net.minecraft.block.BlockPortal;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class PortalESP
extends Module {
    private final ArrayList<BlockPos> blockPosArrayList = new ArrayList();
    private final Setting<Integer> distance = this.add(new Setting<>("Distance", 60, 10, 100));
    private final Setting<Integer> updateDelay = this.add(new Setting<>("UpdateDelay", 2000, 500, 5000));
    private final Setting<Boolean> box = this.add(new Setting<>("Box", false).setParent());
    private final Setting<Integer> boxAlpha = this.add(new Setting<Object>("BoxAlpha", 125, 0, 255, v -> this.box.isOpen()));
    private final Setting<Boolean> outline = this.add(new Setting<>("Outline", true).setParent());
    private final Setting<Integer> outlineAlpha = this.add(new Setting<Object>("outlineAlpha", 150, 0, 255, v -> this.outline.isOpen()));
    private final Setting<Color> color = this.add(new Setting<>("Color", new Color(255, 255, 255, 100)).hideAlpha());
    private final Timer timer = new Timer();

    public PortalESP() {
        super("PortalESP", "Draws portals", Category.RENDER);
    }

    @Override
    public void onTick() {
        if (this.timer.passedMs(this.updateDelay.getValue())) {
            this.blockPosArrayList.clear();
            this.updateBlocks();
            this.timer.reset();
        }
    }

    @Override
    public void onRender3D(Render3DEvent event) {
        for (BlockPos pos : this.blockPosArrayList) {
            this.drawBlock(pos);
        }
    }

    private void drawBlock(BlockPos pos) {
        AxisAlignedBB axisAlignedBB = PortalESP.mc.world.getBlockState(pos).getSelectedBoundingBox(PortalESP.mc.world, pos);
        if (this.outline.getValue()) {
            RenderUtil.drawBBBox(axisAlignedBB, this.color.getValue(), this.outlineAlpha.getValue());
        }
        if (this.box.getValue()) {
            RenderUtil.drawBoxESP(pos, this.color.getValue(), this.boxAlpha.getValue());
        }
    }

    private void updateBlocks() {
        for (BlockPos pos : BlockUtil.getBox(this.distance.getValue())) {
            if (!(PortalESP.mc.world.getBlockState(pos).getBlock() instanceof BlockPortal) && !(PortalESP.mc.world.getBlockState(pos).getBlock() instanceof BlockEndPortalFrame)) continue;
            this.blockPosArrayList.add(pos);
        }
    }
}

