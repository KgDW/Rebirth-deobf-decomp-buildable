package me.rebirthclient.mod.modules.impl.render;

import java.awt.Color;
import java.util.HashMap;
import me.rebirthclient.api.events.impl.Render3DEvent;
import me.rebirthclient.api.util.FadeUtils;
import me.rebirthclient.api.util.render.RenderUtil;
import me.rebirthclient.mod.modules.Category;
import me.rebirthclient.mod.modules.Module;
import me.rebirthclient.mod.modules.settings.Setting;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class PlaceRender
extends Module {
    public static PlaceRender INSTANCE;
    public static final HashMap<BlockPos, placePosition> PlaceMap;
    private final Setting<Boolean> box = this.add(new Setting<>("Box", true));
    private final Setting<Boolean> outline = this.add(new Setting<>("Outline", false));
    private final Setting<Integer> animationTime = this.add(new Setting<>("animationTime", 1000, 0, 5000));
    public final Setting<Color> color = this.add(new Setting<>("Color", new Color(255, 255, 255, 100)));
    private final Setting<Boolean> sync = this.add(new Setting<>("Sync", true));

    public PlaceRender() {
        super("PlaceRender", "test", Category.RENDER);
        INSTANCE = this;
    }

    @Override
    public void onRender3D(Render3DEvent event) {
        boolean shouldClear = true;
        for (placePosition placePosition2 : PlaceMap.values()) {
            if (placePosition2.firstFade.easeOutQuad() == 1.0) continue;
            shouldClear = false;
            this.drawBlock(placePosition2.pos, placePosition2.firstFade.easeOutQuad() - 1.0, placePosition2.posColor);
        }
        if (shouldClear) {
            PlaceMap.clear();
        }
    }

    private void drawBlock(BlockPos pos, double alpha, Color color) {
        if (this.sync.getValue()) {
            color = this.color.getValue();
        }
        AxisAlignedBB axisAlignedBB = PlaceRender.mc.world.getBlockState(pos).getSelectedBoundingBox(PlaceRender.mc.world, pos);
        if (this.outline.getValue()) {
            RenderUtil.drawBBBox(axisAlignedBB, color, (int)((double)color.getAlpha() * -alpha));
        }
        if (this.box.getValue()) {
            RenderUtil.drawBoxESP(pos, color, (int)((double)color.getAlpha() * -alpha));
        }
    }

    static /* synthetic */ Setting access$000(PlaceRender x0) {
        return x0.animationTime;
    }

    static {
        PlaceMap = new HashMap();
    }

    public static class placePosition {
        public final FadeUtils firstFade = new FadeUtils((Integer) PlaceRender.access$000(INSTANCE).getValue());
        public final BlockPos pos;
        public final Color posColor;

        public placePosition(BlockPos placePos) {
            this.pos = placePos;
            this.posColor = PlaceRender.INSTANCE.color.getValue();
        }
    }
}

