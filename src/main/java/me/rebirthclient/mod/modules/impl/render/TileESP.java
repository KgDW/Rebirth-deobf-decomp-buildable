package me.rebirthclient.mod.modules.impl.render;

import java.awt.Color;
import me.rebirthclient.api.events.impl.Render3DEvent;
import me.rebirthclient.api.util.render.RenderUtil;
import me.rebirthclient.mod.modules.Category;
import me.rebirthclient.mod.modules.Module;
import me.rebirthclient.mod.modules.settings.Setting;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityBed;
import net.minecraft.tileentity.TileEntityChest;
import net.minecraft.tileentity.TileEntityDispenser;
import net.minecraft.tileentity.TileEntityEnderChest;
import net.minecraft.tileentity.TileEntityFurnace;
import net.minecraft.tileentity.TileEntityHopper;
import net.minecraft.tileentity.TileEntityShulkerBox;
import net.minecraft.tileentity.TileEntitySign;

public class TileESP
extends Module {
    private final Setting<Boolean> beds = this.add(new Setting<>("Beds", true));
    private final Setting<Boolean> chests = this.add(new Setting<>("Chests", true));
    private final Setting<Boolean> eChests = this.add(new Setting<>("EChests", true));
    private final Setting<Boolean> shulkers = this.add(new Setting<>("Shulkers", true));
    private final Setting<Boolean> signs = this.add(new Setting<>("Signs", true));
    private final Setting<Boolean> dispensers = this.add(new Setting<>("Dispensers", true));
    private final Setting<Boolean> hoppers = this.add(new Setting<>("Hoppers", true));
    private final Setting<Boolean> furnaces = this.add(new Setting<>("Furnaces", true));
    private int count;

    public TileESP() {
        super("TileESP", "Highlights tile entities such as storages and signs", Category.RENDER);
    }

    @Override
    public String getInfo() {
        return String.valueOf(this.count);
    }

    @Override
    public void onRender3D(Render3DEvent event) {
        this.count = 0;
        for (TileEntity entity : TileESP.mc.world.loadedTileEntityList) {
            if (!this.isValid(entity)) continue;
            RenderUtil.drawSelectionBoxESP(entity.getPos(), this.getColor(entity), false, new Color(-1), 1.0f, true, true, 100, false);
            ++this.count;
        }
    }

    private Color getColor(TileEntity entity) {
        if (entity instanceof TileEntityChest) {
            return new Color(155, 127, 77, 100);
        }
        if (entity instanceof TileEntityBed) {
            return new Color(190, 49, 49, 100);
        }
        if (entity instanceof TileEntityEnderChest) {
            return new Color(124, 37, 196, 100);
        }
        if (entity instanceof TileEntityShulkerBox) {
            return new Color(255, 1, 175, 100);
        }
        if (entity instanceof TileEntityFurnace || entity instanceof TileEntityDispenser || entity instanceof TileEntityHopper) {
            return new Color(150, 150, 150, 100);
        }
        return new Color(255, 255, 255, 100);
    }

    private boolean isValid(TileEntity entity) {
        return entity instanceof TileEntityChest && this.chests.getValue() || entity instanceof TileEntityBed && this.beds.getValue() || entity instanceof TileEntityEnderChest && this.eChests.getValue() || entity instanceof TileEntityShulkerBox && this.shulkers.getValue() || entity instanceof TileEntityFurnace && this.furnaces.getValue() || entity instanceof TileEntityDispenser && this.dispensers.getValue() || entity instanceof TileEntityHopper && this.hoppers.getValue() || entity instanceof TileEntitySign && this.signs.getValue();
    }
}

