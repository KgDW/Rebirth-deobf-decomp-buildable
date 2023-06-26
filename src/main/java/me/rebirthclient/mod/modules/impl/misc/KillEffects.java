package me.rebirthclient.mod.modules.impl.misc;

import me.rebirthclient.api.util.Timer;
import me.rebirthclient.mod.modules.Category;
import me.rebirthclient.mod.modules.Module;
import me.rebirthclient.mod.modules.settings.Setting;
import net.minecraft.entity.Entity;
import net.minecraft.entity.effect.EntityLightningBolt;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraft.world.World;

public class KillEffects
extends Module {
    private final Setting<Lightning> lightning = this.add(new Setting<>("Lightning", Lightning.NORMAL));
    private final Setting<KillSound> killSound = this.add(new Setting<>("KillSound", KillSound.OFF));
    private final Setting<Boolean> oneself = this.add(new Setting<>("Oneself", true));
    private final Timer timer = new Timer();

    public KillEffects() {
        super("KillEffects", "jajaja hypixel mode", Category.MISC);
    }

    @Override
    public void onDeath(EntityPlayer player) {
        if (player == null || player == KillEffects.mc.player && !this.oneself.getValue() || player.getHealth() > 0.0f || KillEffects.mc.player.isDead || KillEffects.nullCheck() || KillEffects.fullNullCheck()) {
            return;
        }
        if (this.timer.passedMs(1500L)) {
            SoundEvent sound;
            if (this.lightning.getValue() != Lightning.OFF) {
                KillEffects.mc.world.spawnEntity(new EntityLightningBolt(KillEffects.mc.world, player.posX, player.posY, player.posZ, true));
                if (this.lightning.getValue() == Lightning.NORMAL) {
                    KillEffects.mc.player.playSound(SoundEvents.ENTITY_LIGHTNING_THUNDER, 0.5f, 1.0f);
                }
            }
            if (this.killSound.getValue() != KillSound.OFF && (sound = this.getSound()) != null) {
                KillEffects.mc.player.playSound(sound, 1.0f, 1.0f);
            }
            this.timer.reset();
        }
    }

    private SoundEvent getSound() {
        switch (this.killSound.getValue()) {
            case CS: {
                return new SoundEvent(new ResourceLocation("rebirth", "kill_sound_cs"));
            }
            case NEVERLOSE: {
                return new SoundEvent(new ResourceLocation("rebirth", "kill_sound_nl"));
            }
            case HYPIXEL: {
                return SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP;
            }
        }
        return null;
    }

    private static enum KillSound {
        CS,
        NEVERLOSE,
        HYPIXEL,
        OFF

    }

    private static enum Lightning {
        NORMAL,
        SILENT,
        OFF

    }
}

