package me.rebirthclient.mod.modules.impl.movement;

import me.rebirthclient.api.util.BlockUtil;
import me.rebirthclient.mod.modules.Category;
import me.rebirthclient.mod.modules.Module;
import me.rebirthclient.mod.modules.settings.Setting;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;

public class AntiVoid
extends Module {
    private final Setting<Integer> height = this.add(new Setting<>("Height", 100, 0, 256));

    public AntiVoid() {
        super("AntiVoid", "Allows you to fly over void blocks", Category.MOVEMENT);
    }

    @Override
    public void onTick() {
        boolean isVoid = true;
        for (int i = (int)AntiVoid.mc.player.posY; i > -1; --i) {
            if (BlockUtil.getBlock(new BlockPos(AntiVoid.mc.player.posX, i, AntiVoid.mc.player.posZ)) == Blocks.AIR) continue;
            isVoid = false;
            break;
        }
        if (AntiVoid.mc.player.posY < (double) this.height.getValue() && isVoid) {
            AntiVoid.mc.player.motionY = 0.0;
        }
    }
}

