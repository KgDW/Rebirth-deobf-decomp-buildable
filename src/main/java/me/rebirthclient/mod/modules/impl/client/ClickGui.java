package me.rebirthclient.mod.modules.impl.client;

import com.mojang.realmsclient.gui.ChatFormatting;
import java.awt.Color;
import me.rebirthclient.api.events.impl.ClientEvent;
import me.rebirthclient.api.managers.Managers;
import me.rebirthclient.api.util.FadeUtils;
import me.rebirthclient.mod.commands.Command;
import me.rebirthclient.mod.gui.screen.Gui;
import me.rebirthclient.mod.modules.Category;
import me.rebirthclient.mod.modules.Module;
import me.rebirthclient.mod.modules.settings.Setting;
import net.minecraft.client.gui.GuiMainMenu;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class ClickGui
extends Module {
    public static ClickGui INSTANCE;
    public final Setting<String> prefix = this.add(new Setting<>("Prefix", ";"));
    public final Setting<Boolean> disableSave = this.add(new Setting<>("DisableSave", true));
    public final Setting<Style> style = this.add(new Setting<>("Style", Style.NEW));
    public final Setting<Integer> height = this.add(new Setting<>("ButtonHeight", 4, 1, 5));
    public final Setting<Boolean> blur = this.add(new Setting<>("Blur", false));
    public final Setting<Boolean> line = this.add(new Setting<>("Line", true).setParent());
    public final Setting<Boolean> rollingLine = this.add(new Setting<>("RollingLine", true, v -> this.line.isOpen()));
    public final Setting<Boolean> rect = this.add(new Setting<>("Rect", true).setParent());
    public final Setting<Boolean> colorRect = this.add(new Setting<>("ColorRect", false, v -> this.rect.isOpen()));
    public final Setting<Boolean> gear = this.add(new Setting<>("Gear", true));
    public final Setting<Boolean> icon = this.add(new Setting<>("Icon", true));
    private final Setting<Integer> animationTime = this.add(new Setting<>("AnimationTime", 500, 0, 2000));
    public final Setting<Boolean> snow = this.add(new Setting<>("Snow", true));
    public final Setting<Boolean> particles = this.add(new Setting<>("Particles", false).setParent());
    public final Setting<Boolean> colorParticles = this.add(new Setting<>("ColorParticles", true, v -> this.particles.isOpen()));
    public final Setting<Boolean> background = this.add(new Setting<>("Background", true));
    public final Setting<Boolean> cleanGui = this.add(new Setting<>("CleanGui", false));
    public final Setting<Color> color = this.add(new Setting<>("Color", new Color(125, 125, 213)).hideAlpha().noRainbow());
    public final Setting<Boolean> rainbow = this.add(new Setting<>("Rainbow", false).setParent());
    public final Setting<Rainbow> rainbowMode = this.add(new Setting<>("Mode", Rainbow.NORMAL, v -> this.rainbow.isOpen()));
    public final Setting<Float> rainbowSaturation = this.add(new Setting<>("Saturation", 130.0f, 1.0f, 255.0f, v -> this.rainbow.isOpen() && this.rainbowMode.getValue() == Rainbow.NORMAL));
    public final Setting<Color> secondColor = this.add(new Setting<>("SecondColor", new Color(255, 255, 255), v -> this.rainbow.isOpen() && this.rainbowMode.getValue() == Rainbow.DOUBLE).hideAlpha());
    public final Setting<HudRainbow> hudRainbow = this.add(new Setting<>("HUD", HudRainbow.STATIC, v -> this.rainbow.isOpen()));
    public final Setting<Integer> rainbowDelay = this.add(new Setting<>("Delay", 350, 0, 600, v -> this.rainbow.isOpen()));
    public final Setting<Integer> rainbowSpeed = this.add(new Setting<>("Speed", 350, 0, 600, v -> this.rainbow.isOpen()));
    public static final FadeUtils animation;

    public ClickGui() {
        super("ClickGui", "Opens the ClickGui", Category.CLIENT);
        INSTANCE = this;
    }

    @Override
    public void onEnable() {
        animation.setLength(this.animationTime.getValue());
        animation.reset();
        if (ClickGui.mc.world != null) {
            mc.displayGuiScreen(Gui.INSTANCE);
        }
        if (this.blur.getValue()) {
            ClickGui.mc.entityRenderer.loadShader(new ResourceLocation("shaders/post/blur.json"));
        }
    }

    @Override
    public void onLoad() {
        Managers.COLORS.setCurrent(this.color.getValue());
        Managers.COMMANDS.setPrefix(this.prefix.getValue());
    }

    @Override
    public void onTick() {
        if (!(ClickGui.mc.currentScreen instanceof Gui) && !(ClickGui.mc.currentScreen instanceof GuiMainMenu)) {
            this.disable();
        }
    }

    @SubscribeEvent
    public void onSettingChange(ClientEvent event) {
        if (ClickGui.fullNullCheck()) {
            return;
        }
        if (event.getStage() == 2 && event.getSetting().getMod().equals(this)) {
            if (event.getSetting().equals(this.prefix)) {
                Managers.COMMANDS.setPrefix(this.prefix.getPlannedValue());
                Command.sendMessage("Prefix set to " + ChatFormatting.DARK_GRAY + Managers.COMMANDS.getCommandPrefix());
            }
            Managers.COLORS.setCurrent(this.color.getValue());
        }
    }

    @Override
    public void onDisable() {
        if (this.disableSave.getValue()) {
            Managers.CONFIGS.saveConfig(Managers.CONFIGS.config.replaceFirst("Rebirth/", ""));
        }
    }

    public int getButtonHeight() {
        return 11 + this.height.getValue();
    }

    static {
        animation = new FadeUtils(500L);
    }

    public static enum Style {
        OLD,
        NEW,
        FUTURE,
        DOTGOD

    }

    public static enum Rainbow {
        NORMAL,
        PLAIN,
        DOUBLE

    }

    public static enum HudRainbow {
        STATIC,
        ROLLING

    }
}

