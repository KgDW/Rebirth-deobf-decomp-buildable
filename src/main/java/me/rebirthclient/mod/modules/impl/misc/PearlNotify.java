package me.rebirthclient.mod.modules.impl.misc;

import com.mojang.realmsclient.gui.ChatFormatting;
import me.rebirthclient.api.managers.Managers;
import me.rebirthclient.mod.modules.Category;
import me.rebirthclient.mod.modules.Module;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityEnderPearl;
import net.minecraft.entity.player.EntityPlayer;

public class PearlNotify
extends Module {
    private boolean flag;

    public PearlNotify() {
        super("PearlNotify", "Notifies pearl throws", Category.MISC);
    }

    @Override
    public void onEnable() {
        this.flag = true;
    }

    @Override
    public void onUpdate() {
        Entity enderPearl = null;
        for (Object e : PearlNotify.mc.world.loadedEntityList) {
            if (!(e instanceof EntityEnderPearl)) continue;
            enderPearl = (Entity) e;
            break;
        }
        if (enderPearl == null) {
            this.flag = true;
            return;
        }
        EntityPlayer closestPlayer = null;
        for (EntityPlayer entity : PearlNotify.mc.world.playerEntities) {
            if (closestPlayer == null) {
                closestPlayer = entity;
                continue;
            }
            if (closestPlayer.getDistance(enderPearl) <= entity.getDistance(enderPearl)) continue;
            closestPlayer = entity;
        }
        if (closestPlayer == PearlNotify.mc.player) {
            this.flag = false;
        }
        if (closestPlayer != null && this.flag) {
            String faceing = enderPearl.getHorizontalFacing().toString();
            if (faceing.equals("West")) {
                faceing = "East";
            } else if (faceing.equals("East")) {
                faceing = "West";
            }
            this.sendMessageWithID(Managers.FRIENDS.isFriend(closestPlayer.getName()) ? ChatFormatting.AQUA + closestPlayer.getName() + ChatFormatting.GRAY + " has just thrown a pearl heading " + ChatFormatting.AQUA + faceing + "!" : ChatFormatting.RED + closestPlayer.getName() + ChatFormatting.GRAY + " has just thrown a pearl heading " + ChatFormatting.RED + faceing + "!", closestPlayer.getEntityId());
            this.flag = false;
        }
    }
}

