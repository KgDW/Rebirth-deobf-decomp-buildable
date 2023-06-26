package me.rebirthclient.mod.modules.impl.render;

import java.awt.Color;
import me.rebirthclient.api.events.impl.Render3DEvent;
import me.rebirthclient.api.managers.Managers;
import me.rebirthclient.api.util.BlockUtil;
import me.rebirthclient.api.util.render.RenderUtil;
import me.rebirthclient.mod.modules.Category;
import me.rebirthclient.mod.modules.Module;
import me.rebirthclient.mod.modules.settings.Setting;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;

public class VoidESP
extends Module {
    private final Setting<Integer> rangeX = this.add(new Setting<>("RangeX", 10, 0, 25));
    private final Setting<Integer> rangeY = this.add(new Setting<>("RangeY", 5, 0, 25));
    private final Setting<Mode> mode = this.add(new Setting<>("Mode", Mode.FULL));
    private final Setting<Integer> height = this.add(new Setting<>("Height", 1, 0, 4, v -> this.mode.getValue() == Mode.FULL));
    private final Setting<Boolean> fill = this.add(new Setting<>("Fill", true));
    private final Setting<Boolean> line = this.add(new Setting<>("Outline", true));
    private final Setting<Boolean> wireframe = this.add(new Setting<>("Wireframe", true));
    private final Setting<Color> color = this.add(new Setting<>("Color", new Color(1692929536, true)));

    public VoidESP() {
        super("VoidESP", "Highlights void blocks", Category.RENDER);
    }

    public static boolean isAir(BlockPos pos) {
        return BlockUtil.getBlock(pos) == Blocks.AIR;
    }

    @Override
    public String getInfo() {
        return Managers.TEXT.normalizeCases(this.mode.getValue());
    }

    @Override
    public void onRender3D(Render3DEvent event) {
        if (!VoidESP.fullNullCheck()) {
            assert (VoidESP.mc.renderViewEntity != null);
            Vec3i playerPos = new Vec3i(VoidESP.mc.renderViewEntity.posX, VoidESP.mc.renderViewEntity.posY, VoidESP.mc.renderViewEntity.posZ);
            for (int x = playerPos.getX() - this.rangeX.getValue(); x < playerPos.getX() + this.rangeX.getValue(); ++x) {
                for (int z = playerPos.getZ() - this.rangeX.getValue(); z < playerPos.getZ() + this.rangeX.getValue(); ++z) {
                    for (int y = playerPos.getY() + this.rangeY.getValue(); y > playerPos.getY() - this.rangeY.getValue(); --y) {
                        BlockPos pos = new BlockPos(x, y, z);
                        double h = 0.0;
                        if (this.mode.getValue() == Mode.FLAT) {
                            h = -1.0;
                        } else if (this.mode.getValue() == Mode.SLAB) {
                            h = -0.8;
                        }
                        if (!this.isVoid(pos)) continue;
                        if (this.mode.getValue() == Mode.FULL) {
                            if (this.height.getValue() == 1 || !VoidESP.isAir(pos.up())) {
                                this.drawVoidESP(pos, this.color.getValue(), new Color(-1), this.line.getValue(), this.fill.getValue(), this.color.getValue().getAlpha(), 0.0, this.wireframe.getValue());
                                continue;
                            }
                            if (this.height.getValue() == 2 && VoidESP.isAir(pos.up())) {
                                this.drawVoidESP(pos, this.color.getValue(), new Color(-1), this.line.getValue(), this.fill.getValue(), this.color.getValue().getAlpha(), 1.0, this.wireframe.getValue());
                                continue;
                            }
                            if (this.height.getValue() == 3 && VoidESP.isAir(pos.up()) && VoidESP.isAir(pos.up().up())) {
                                this.drawVoidESP(pos, this.color.getValue(), new Color(-1), this.line.getValue(), this.fill.getValue(), this.color.getValue().getAlpha(), 2.0, this.wireframe.getValue());
                                continue;
                            }
                            if (this.height.getValue() != 4 || !VoidESP.isAir(pos.up()) || !VoidESP.isAir(pos.up().up()) || !VoidESP.isAir(pos.up().up().up())) continue;
                            this.drawVoidESP(pos, this.color.getValue(), new Color(-1), this.line.getValue(), this.fill.getValue(), this.color.getValue().getAlpha(), 3.0, this.wireframe.getValue());
                            continue;
                        }
                        this.drawVoidESP(pos, this.color.getValue(), new Color(-1), this.line.getValue(), this.fill.getValue(), this.color.getValue().getAlpha(), h, this.wireframe.getValue());
                    }
                }
            }
        }
    }

    private boolean isVoid(BlockPos pos) {
        if (pos.getY() != 0) {
            return false;
        }
        return BlockUtil.getBlock(pos) != Blocks.BEDROCK;
    }

    private void drawVoidESP(BlockPos pos, Color color, Color secondColor, boolean outline, boolean box, int boxAlpha, double height, boolean cross) {
        if (box) {
            RenderUtil.drawBox(pos, new Color(color.getRed(), color.getGreen(), color.getBlue(), boxAlpha), height, false, false, 0);
        }
        if (outline) {
            RenderUtil.drawBlockOutline(pos, color, 0.8f, true, height, false, false, 0, false);
        }
        if (cross) {
            RenderUtil.drawBlockWireframe(pos, color, 0.8f, height, true);
        }
    }

    private static enum Mode {
        FLAT,
        SLAB,
        FULL

    }
}

