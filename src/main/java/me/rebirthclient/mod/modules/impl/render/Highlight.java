package me.rebirthclient.mod.modules.impl.render;

import java.awt.Color;
import me.rebirthclient.api.events.impl.Render3DEvent;
import me.rebirthclient.api.util.FadeUtils;
import me.rebirthclient.api.util.render.RenderUtil;
import me.rebirthclient.mod.modules.Category;
import me.rebirthclient.mod.modules.Module;
import me.rebirthclient.mod.modules.settings.Setting;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;

public class Highlight
extends Module {
    private final Setting<Integer> animationTime = this.add(new Setting<>("AnimationTime", 1000, 0, 6000));
    private final Setting<Boolean> line = this.add(new Setting<>("Line", true));
    private final Setting<Boolean> box = this.add(new Setting<>("Box", false));
    private final Setting<Boolean> depth = this.add(new Setting<>("Depth", true));
    private final Setting<Float> lineWidth = this.add(new Setting<>("LineWidth", 3.0f, 0.1f, 3.0f));
    private final Setting<Color> color = this.add(new Setting<>("Color", new Color(125, 125, 213, 150)));
    BlockPos lastPos;
    FadeUtils fade = new FadeUtils(this.animationTime.getValue());
    AxisAlignedBB renderBB;

    public Highlight() {
        super("Highlight", "Highlights the block u look at", Category.RENDER);
    }

    @Override
    public void onRender3D(Render3DEvent event) {
        RayTraceResult ray = Highlight.mc.objectMouseOver;
        if (ray != null && ray.typeOfHit == RayTraceResult.Type.BLOCK) {
            BlockPos pos = ray.getBlockPos();
            if (this.lastPos == null) {
                this.lastPos = pos;
                this.renderBB = Highlight.mc.world.getBlockState(pos).getSelectedBoundingBox(Highlight.mc.world, pos).grow(0.002);
            }
            if (!this.lastPos.equals(ray.getBlockPos())) {
                this.lastPos = ray.getBlockPos();
                this.fade = new FadeUtils(this.animationTime.getValue());
            }
            AxisAlignedBB bb2 = Highlight.mc.world.getBlockState(pos).getSelectedBoundingBox(Highlight.mc.world, pos).grow(0.002);
            this.renderBB = new AxisAlignedBB(this.renderBB.minX - (this.renderBB.minX - bb2.minX) * this.fade.easeOutQuad(), this.renderBB.minY - (this.renderBB.minY - bb2.minY) * this.fade.easeOutQuad(), this.renderBB.minZ - (this.renderBB.minZ - bb2.minZ) * this.fade.easeOutQuad(), this.renderBB.maxX - (this.renderBB.maxX - bb2.maxX) * this.fade.easeOutQuad(), this.renderBB.maxY - (this.renderBB.maxY - bb2.maxY) * this.fade.easeOutQuad(), this.renderBB.maxZ - (this.renderBB.maxZ - bb2.maxZ) * this.fade.easeOutQuad());
            RenderUtil.testESP(this.renderBB, this.color.getValue(), false, new Color(-1), this.lineWidth.getValue(), this.line.getValue(), this.box.getValue(), this.color.getValue().getAlpha(), this.depth.getValue());
        }
    }
}

