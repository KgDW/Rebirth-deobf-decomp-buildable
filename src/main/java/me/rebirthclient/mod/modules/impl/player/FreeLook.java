package me.rebirthclient.mod.modules.impl.player;

import me.rebirthclient.api.events.impl.TurnEvent;
import me.rebirthclient.mod.modules.Category;
import me.rebirthclient.mod.modules.Module;
import me.rebirthclient.mod.modules.settings.Bind;
import me.rebirthclient.mod.modules.settings.Setting;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.client.event.EntityViewRenderEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class FreeLook
extends Module {
    public final Setting<Bind> bind = this.add(new Setting<>("Bind", new Bind(-1)));
    boolean enabled = false;
    private float dYaw;
    private float dPitch;

    public FreeLook() {
        super("FreeLook", "Rotate your camera and not your player in 3rd person", Category.PLAYER);
    }

    @Override
    public void onTick() {
        if (FreeLook.mc.currentScreen == null && this.bind.getValue().isDown()) {
            if (!this.enabled) {
                this.dYaw = 0.0f;
                this.dPitch = 0.0f;
                FreeLook.mc.gameSettings.thirdPersonView = 1;
            }
            this.enabled = true;
        } else {
            if (this.enabled) {
                FreeLook.mc.gameSettings.thirdPersonView = 0;
            }
            this.enabled = false;
        }
        if (FreeLook.mc.gameSettings.thirdPersonView != 1 && this.enabled) {
            this.enabled = false;
            FreeLook.mc.gameSettings.thirdPersonView = 0;
        }
    }

    @Override
    public void onDisable() {
        this.enabled = false;
        FreeLook.mc.gameSettings.thirdPersonView = 0;
    }

    @SubscribeEvent
    public void onCameraSetup(EntityViewRenderEvent.CameraSetup event) {
        if (FreeLook.mc.gameSettings.thirdPersonView > 0 && this.enabled) {
            event.setYaw(event.getYaw() + this.dYaw);
            event.setPitch(event.getPitch() + this.dPitch);
        }
    }

    @SubscribeEvent
    public void onTurn(TurnEvent event) {
        if (FreeLook.mc.gameSettings.thirdPersonView > 0 && this.enabled) {
            this.dYaw = (float)((double)this.dYaw + (double)event.getYaw() * 0.15);
            this.dPitch = (float)((double)this.dPitch - (double)event.getPitch() * 0.15);
            this.dPitch = MathHelper.clamp(this.dPitch, -90.0f, 90.0f);
            event.setCanceled(true);
        }
    }
}

