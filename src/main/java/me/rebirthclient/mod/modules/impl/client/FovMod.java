package me.rebirthclient.mod.modules.impl.client;

import me.rebirthclient.api.events.impl.PerspectiveEvent;
import me.rebirthclient.mod.modules.Category;
import me.rebirthclient.mod.modules.Module;
import me.rebirthclient.mod.modules.settings.Setting;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.init.MobEffects;
import net.minecraftforge.client.event.FOVUpdateEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class FovMod
extends Module {
    public static FovMod INSTANCE = new FovMod();
    private final Setting<Page> page = this.add(new Setting<>("Settings", Page.FOV));
    private final Setting<Boolean> customFov = this.add(new Setting<>("CustomFov", false, v -> this.page.getValue() == Page.FOV).setParent());
    private final Setting<Float> fov = this.add(new Setting<>("FOV", 120.0f, 10.0f, 180.0f, v -> this.page.getValue() == Page.FOV && this.customFov.isOpen()));
    private final Setting<Boolean> aspectRatio = this.add(new Setting<>("AspectRatio", false, v -> this.page.getValue() == Page.FOV).setParent());
    private final Setting<Float> aspectFactor = this.add(new Setting<>("AspectFactor", 1.8f, 0.1f, 3.0f, v -> this.page.getValue() == Page.FOV && this.aspectRatio.isOpen()));
    private final Setting<Boolean> defaults = this.add(new Setting<>("Defaults", false, v -> this.page.getValue() == Page.ADVANCED));
    private final Setting<Float> sprint = this.add(new Setting<>("SprintAdd", 1.15f, 1.0f, 2.0f, v -> this.page.getValue() == Page.ADVANCED));
    private final Setting<Float> speed = this.add(new Setting<>("SwiftnessAdd", 1.15f, 1.0f, 2.0f, v -> this.page.getValue() == Page.ADVANCED));

    public FovMod() {
        super("FovMod", "FOV modifier", Category.CLIENT);
        INSTANCE = this;
    }

    @Override
    public void onUpdate() {
        if (this.customFov.getValue()) {
            FovMod.mc.gameSettings.setOptionFloatValue(GameSettings.Options.FOV, this.fov.getValue());
        }
        if (this.defaults.getValue()) {
            this.sprint.setValue(1.15f);
            this.speed.setValue(1.15f);
            this.defaults.setValue(false);
        }
    }

    @SubscribeEvent
    public void onFOVUpdate(FOVUpdateEvent event) {
        if (FovMod.fullNullCheck()) {
            return;
        }
        float fov = 1.0f;
        if (event.getEntity().isSprinting()) {
            fov = this.sprint.getValue();
            if (event.getEntity().isPotionActive(MobEffects.SPEED)) {
                fov = this.speed.getValue();
            }
        }
        event.setNewfov(fov);
    }

    @SubscribeEvent
    public void onPerspectiveUpdate(PerspectiveEvent event) {
        if (FovMod.fullNullCheck()) {
            return;
        }
        if (this.aspectRatio.getValue()) {
            event.setAngle(this.aspectFactor.getValue());
        }
    }

    public static enum Page {
        FOV,
        ADVANCED

    }
}

