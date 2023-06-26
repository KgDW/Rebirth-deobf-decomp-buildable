package me.rebirthclient.mod.modules.impl.combat;

import me.rebirthclient.api.util.BlockUtil;
import me.rebirthclient.api.util.CombatUtil;
import me.rebirthclient.mod.modules.Category;
import me.rebirthclient.mod.modules.Module;
import me.rebirthclient.mod.modules.impl.combat.PacketMine;
import me.rebirthclient.mod.modules.settings.Setting;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityShulkerBox;

public class AntiRegear
extends Module {
    private final Setting<Integer> safeRange = this.add(new Setting<>("SafeRange", 2, 0, 8));
    private final Setting<Integer> range = this.add(new Setting<>("Range", 5, 0, 8));

    public AntiRegear() {
        super("AntiRegear", "Shulker nuker", Category.COMBAT);
    }

    @Override
    public void onUpdate() {
        if (PacketMine.breakPos != null && BlockUtil.shulkerList.contains(AntiRegear.mc.world.getBlockState(PacketMine.breakPos).getBlock())) {
            return;
        }
        if (this.getBlock() != null) {
            CombatUtil.mineBlock(this.getBlock().getPos());
        }
    }

    private TileEntity getBlock() {
        TileEntity out = null;
        for (TileEntity entity : AntiRegear.mc.world.loadedTileEntityList) {
            if (!(entity instanceof TileEntityShulkerBox) || AntiRegear.mc.player.getDistance((double)entity.getPos().getX() + 0.5, (double)entity.getPos().getY() + 0.5, (double)entity.getPos().getZ() + 0.5) <= (double) this.safeRange.getValue() || !(AntiRegear.mc.player.getDistance((double)entity.getPos().getX() + 0.5, (double)entity.getPos().getY() + 0.5, (double)entity.getPos().getZ() + 0.5) <= (double) this.range.getValue())) continue;
            out = entity;
        }
        return out;
    }
}

