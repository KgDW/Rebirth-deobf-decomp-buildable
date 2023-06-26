package me.rebirthclient.mod.modules.impl.render;

import me.rebirthclient.api.events.impl.Render2DEvent;
import me.rebirthclient.api.events.impl.Render3DEvent;
import me.rebirthclient.api.events.impl.UpdateWalkingPlayerEvent;
import me.rebirthclient.api.util.Timer;
import me.rebirthclient.mod.modules.Category;
import me.rebirthclient.mod.modules.Module;
import me.rebirthclient.mod.modules.settings.Setting;
import net.minecraft.util.EnumHand;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class ItemModel
extends Module {
    public static ItemModel INSTANCE = new ItemModel();
    public final Setting<Settings> settings = this.add(new Setting<>("Settings", Settings.TRANSLATE));
    public final Setting<Boolean> noEatAnimation = this.add(new Setting<>("NoEatAnimation", false, v -> this.settings.getValue() == Settings.TWEAKS));
    public final Setting<Double> eatX = this.add(new Setting<>("EatX", 1.4, -5.0, 15.0, v -> this.settings.getValue() == Settings.TWEAKS && !this.noEatAnimation.getValue()));
    public final Setting<Double> eatY = this.add(new Setting<>("EatY", 1.3, -5.0, 15.0, v -> this.settings.getValue() == Settings.TWEAKS && !this.noEatAnimation.getValue()));
    public final Setting<Boolean> doBob = this.add(new Setting<>("ItemBob", true, v -> this.settings.getValue() == Settings.TWEAKS));
    public final Setting<Double> mainX = this.add(new Setting<>("MainX", 0.2, -4.0, 4.0, v -> this.settings.getValue() == Settings.TRANSLATE));
    public final Setting<Double> mainY = this.add(new Setting<>("MainY", -0.2, -3.0, 3.0, v -> this.settings.getValue() == Settings.TRANSLATE));
    public final Setting<Double> mainZ = this.add(new Setting<>("MainZ", -0.3, -5.0, 5.0, v -> this.settings.getValue() == Settings.TRANSLATE));
    public final Setting<Double> offX = this.add(new Setting<>("OffX", -0.2, -4.0, 4.0, v -> this.settings.getValue() == Settings.TRANSLATE));
    public final Setting<Double> offY = this.add(new Setting<>("OffY", -0.2, -3.0, 3.0, v -> this.settings.getValue() == Settings.TRANSLATE));
    public final Setting<Double> offZ = this.add(new Setting<>("OffZ", -0.3, -5.0, 5.0, v -> this.settings.getValue() == Settings.TRANSLATE));
    public final Setting<Boolean> spinY = this.add(new Setting<>("SpinX", false, v -> this.settings.getValue() == Settings.TRANSLATE));
    public final Setting<Boolean> spinX = this.add(new Setting<>("SpinY", false, v -> this.settings.getValue() == Settings.TRANSLATE));
    public final Setting<Integer> delay = this.add(new Setting<>("Delay", 50, 0, 500, v -> this.settings.getValue() == Settings.TRANSLATE && (this.spinX.getValue() || this.spinY.getValue())));
    public final Setting<Integer> angleSpeed = this.add(new Setting<>("AngleSpeed", 5, 0, 10, v -> this.settings.getValue() == Settings.TRANSLATE && (this.spinX.getValue() || this.spinY.getValue())));
    public final Setting<Boolean> customSwing = this.add(new Setting<>("CustomSwing", false, v -> this.settings.getValue() == Settings.OTHERS).setParent());
    public final Setting<Swing> swing = this.add(new Setting<>("Swing", Swing.MAINHAND, v -> this.settings.getValue() == Settings.OTHERS && this.customSwing.isOpen()));
    public final Setting<Boolean> slowSwing = this.add(new Setting<>("SwingSpeed", false, v -> this.settings.getValue() == Settings.OTHERS).setParent());
    public final Setting<Integer> swingSpeed = this.add(new Setting<>("swingSpeed", 15, 0, 30, v -> this.settings.getValue() == Settings.OTHERS && this.slowSwing.isOpen()));
    public final Setting<Boolean> noSway = this.add(new Setting<>("NoSway", false, v -> this.settings.getValue() == Settings.OTHERS));
    final Timer timer = new Timer();
    public float angle = 0.0f;

    public ItemModel() {
        super("ItemModel", "Change the position of the arm", Category.RENDER);
        INSTANCE = this;
    }

    @Override
    public void onTick() {
        this.doAngle();
    }

    @Override
    public void onUpdate() {
        if (this.swing.getValue() == Swing.OFFHAND && this.customSwing.getValue()) {
            ItemModel.mc.player.swingingHand = EnumHand.OFF_HAND;
        } else if (this.swing.getValue() == Swing.MAINHAND && this.customSwing.getValue()) {
            ItemModel.mc.player.swingingHand = EnumHand.MAIN_HAND;
        }
        this.doAngle();
    }

    @Override
    public void onRender3D(Render3DEvent event) {
        this.doAngle();
    }

    @Override
    public void onRender2D(Render2DEvent event) {
        this.doAngle();
    }

    @SubscribeEvent
    public void Update(UpdateWalkingPlayerEvent event) {
        this.doAngle();
    }

    private void doAngle() {
        if (this.timer.passedMs(this.delay.getValue())) {
            this.angle += (float) this.angleSpeed.getValue();
            this.timer.reset();
        }
    }

    public static enum Swing {
        MAINHAND,
        OFFHAND,
        SERVER

    }

    private static enum Settings {
        TRANSLATE,
        TWEAKS,
        OTHERS

    }
}

