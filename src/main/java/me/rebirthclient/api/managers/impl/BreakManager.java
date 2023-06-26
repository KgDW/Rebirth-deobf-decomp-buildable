package me.rebirthclient.api.managers.impl;

import java.util.HashMap;
import me.rebirthclient.api.util.Wrapper;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;

public class BreakManager
implements Wrapper {
    public static final HashMap<EntityPlayer, BlockPos> MineMap = new HashMap();

    public static boolean isMine(BlockPos pos) {
        for (EntityPlayer i : MineMap.keySet()) {
            BlockPos pos2;
            if (i == null || (pos2 = MineMap.get(i)) == null || !new BlockPos(pos2).equals(new BlockPos(pos))) continue;
            return true;
        }
        return false;
    }
}

