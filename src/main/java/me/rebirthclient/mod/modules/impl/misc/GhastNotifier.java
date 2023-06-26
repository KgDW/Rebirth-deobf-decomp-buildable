package me.rebirthclient.mod.modules.impl.misc;

import java.util.HashSet;
import java.util.Set;
import me.rebirthclient.mod.modules.Category;
import me.rebirthclient.mod.modules.Module;
import me.rebirthclient.mod.modules.settings.Setting;
import net.minecraft.entity.Entity;
import net.minecraft.entity.monster.EntityGhast;
import net.minecraft.init.SoundEvents;

public class GhastNotifier
extends Module {
    private final Setting<Boolean> chat = this.add(new Setting<>("Chat", true).setParent());
    private final Setting<Boolean> censorCoords = this.add(new Setting<>("CensorCoords", false, v -> this.chat.isOpen()));
    private final Setting<Boolean> sound = this.add(new Setting<>("Sound", true));
    private final Set<Entity> ghasts = new HashSet<>();

    public GhastNotifier() {
        super("GhastNotify", "Helps you find ghasts", Category.MISC);
    }

    @Override
    public void onEnable() {
        this.ghasts.clear();
    }

    @Override
    public void onUpdate() {
        for (Entity entity : GhastNotifier.mc.world.getLoadedEntityList()) {
            if (!(entity instanceof EntityGhast) || this.ghasts.contains(entity)) continue;
            if (this.chat.getValue()) {
                if (this.censorCoords.getValue()) {
                    this.sendMessage("There is a ghast!");
                } else {
                    this.sendMessage("There is a ghast at: " + entity.getPosition().getX() + "X, " + entity.getPosition().getY() + "Y, " + entity.getPosition().getZ() + "Z.");
                }
            }
            this.ghasts.add(entity);
            if (!this.sound.getValue()) continue;
            GhastNotifier.mc.player.playSound(SoundEvents.BLOCK_ANVIL_DESTROY, 1.0f, 1.0f);
        }
    }
}

