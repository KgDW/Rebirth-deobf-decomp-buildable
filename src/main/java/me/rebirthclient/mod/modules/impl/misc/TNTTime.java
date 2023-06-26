package me.rebirthclient.mod.modules.impl.misc;

import com.mojang.realmsclient.gui.ChatFormatting;
import me.rebirthclient.mod.modules.Category;
import me.rebirthclient.mod.modules.Module;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityTNTPrimed;

public class TNTTime
extends Module {
    public TNTTime() {
        super("TNTTime", "show tnt fuse", Category.MISC);
    }

    @Override
    public void onUpdate() {
        for (Entity entity : TNTTime.mc.world.loadedEntityList) {
            if (!(entity instanceof EntityTNTPrimed)) continue;
            String color = String.valueOf(ChatFormatting.GREEN);
            if ((double)((EntityTNTPrimed)entity).getFuse() / 20.0 > 0.0) {
                color = String.valueOf(ChatFormatting.DARK_RED);
            }
            if ((double)((EntityTNTPrimed)entity).getFuse() / 20.0 > 1.0) {
                color = String.valueOf(ChatFormatting.RED);
            }
            if ((double)((EntityTNTPrimed)entity).getFuse() / 20.0 > 2.0) {
                color = String.valueOf(ChatFormatting.YELLOW);
            }
            if ((double)((EntityTNTPrimed)entity).getFuse() / 20.0 > 3.0) {
                color = String.valueOf(ChatFormatting.GREEN);
            }
            entity.setCustomNameTag(color + String.valueOf((double)((EntityTNTPrimed)entity).getFuse() / 20.0).substring(0, 3) + "s");
            entity.setAlwaysRenderNameTag(true);
        }
    }
}

