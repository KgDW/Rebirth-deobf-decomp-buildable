package me.rebirthclient.mod.modules.impl.render;

import java.awt.Color;
import java.util.HashMap;
import java.util.Map;
import me.rebirthclient.api.events.impl.Render3DEvent;
import me.rebirthclient.api.util.math.MathUtil;
import me.rebirthclient.api.util.render.ColorUtil;
import me.rebirthclient.api.util.render.RenderUtil;
import me.rebirthclient.mod.modules.Category;
import me.rebirthclient.mod.modules.Module;
import me.rebirthclient.mod.modules.settings.Setting;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItemFrame;
import net.minecraft.entity.item.EntityMinecartChest;
import net.minecraft.item.ItemShulkerBox;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityChest;
import net.minecraft.tileentity.TileEntityDispenser;
import net.minecraft.tileentity.TileEntityEndPortal;
import net.minecraft.tileentity.TileEntityEnderChest;
import net.minecraft.tileentity.TileEntityHopper;
import net.minecraft.tileentity.TileEntityShulkerBox;
import net.minecraft.util.math.BlockPos;

public class Search
extends Module {
    private final Setting<Float> range = this.add(new Setting<>("Range", 50.0f, 1.0f, 300.0f));
    private final Setting<Boolean> portal = this.add(new Setting<>("Portal", true));
    private final Setting<Boolean> chest = this.add(new Setting<>("Chest", true));
    private final Setting<Boolean> dispenser = this.add(new Setting<>("Dispenser", false));
    private final Setting<Boolean> shulker = this.add(new Setting<>("Shulker", true));
    private final Setting<Boolean> echest = this.add(new Setting<>("Ender Chest", false));
    private final Setting<Boolean> hopper = this.add(new Setting<>("Hopper", false));
    private final Setting<Boolean> cart = this.add(new Setting<>("Minecart", false));
    private final Setting<Boolean> frame = this.add(new Setting<>("Item Frame", false));
    private final Setting<Boolean> box = this.add(new Setting<>("Box", false));
    private final Setting<Integer> boxAlpha = this.add(new Setting<>("BoxAlpha", 125, 0, 255, v -> this.box.getValue()));
    private final Setting<Boolean> outline = this.add(new Setting<>("Outline", true));
    private final Setting<Float> lineWidth = this.add(new Setting<>("LineWidth", 1.0f, 0.1f, 5.0f, v -> this.outline.getValue()));

    public Search() {
        super("Search", "Highlights Containers", Category.RENDER);
    }

    @Override
    public void onRender3D(Render3DEvent event) {
        int color;
        BlockPos pos;
        HashMap<BlockPos, Integer> positions = new HashMap<>();
        for (TileEntity tileEntity : Search.mc.world.loadedTileEntityList) {
            BlockPos blockPos = null;
            if (!(tileEntity instanceof TileEntityEndPortal && this.portal.getValue() || tileEntity instanceof TileEntityChest && this.chest.getValue() || tileEntity instanceof TileEntityDispenser && this.dispenser.getValue() || tileEntity instanceof TileEntityShulkerBox && this.shulker.getValue() || tileEntity instanceof TileEntityEnderChest && this.echest.getValue()) && (!(tileEntity instanceof TileEntityHopper) || !this.hopper.getValue())) continue;
            pos = tileEntity.getPos();
            if (!(Search.mc.player.getDistanceSq(blockPos) <= MathUtil.square(this.range.getValue())) || (color = this.getTileEntityColor(tileEntity)) == -1) continue;
            positions.put(pos, color);
        }
        for (Entity entity : Search.mc.world.loadedEntityList) {
            BlockPos blockPos = null;
            if ((!(entity instanceof EntityItemFrame) || !this.frame.getValue()) && (!(entity instanceof EntityMinecartChest) || !this.cart.getValue())) continue;
            pos = entity.getPosition();
            if (!(Search.mc.player.getDistanceSq(blockPos) <= MathUtil.square(this.range.getValue())) || (color = this.getEntityColor(entity)) == -1) continue;
            positions.put(pos, color);
        }
        for (Map.Entry entry : positions.entrySet()) {
            BlockPos blockPos = (BlockPos)entry.getKey();
            color = (Integer)entry.getValue();
            RenderUtil.drawBoxESP(blockPos, new Color(color), false, new Color(color), this.lineWidth.getValue(), this.outline.getValue(), this.box.getValue(), this.boxAlpha.getValue(), false);
        }
    }

    private int getTileEntityColor(TileEntity tileEntity) {
        if (tileEntity instanceof TileEntityChest) {
            return ColorUtil.Colors.ORANGE;
        }
        if (tileEntity instanceof TileEntityShulkerBox) {
            return ColorUtil.Colors.WHITE;
        }
        if (tileEntity instanceof TileEntityEndPortal) {
            return ColorUtil.Colors.GRAY;
        }
        if (tileEntity instanceof TileEntityEnderChest) {
            return ColorUtil.Colors.PURPLE;
        }
        if (tileEntity instanceof TileEntityHopper) {
            return ColorUtil.Colors.DARK_RED;
        }
        if (tileEntity instanceof TileEntityDispenser) {
            return ColorUtil.Colors.ORANGE;
        }
        return -1;
    }

    private int getEntityColor(Entity entity) {
        if (entity instanceof EntityMinecartChest) {
            return ColorUtil.Colors.ORANGE;
        }
        if (entity instanceof EntityItemFrame && ((EntityItemFrame)entity).getDisplayedItem().getItem() instanceof ItemShulkerBox) {
            return ColorUtil.Colors.WHITE;
        }
        return -1;
    }
}

