package me.rebirthclient.mod.modules.impl.client;

import java.awt.Color;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import me.rebirthclient.api.events.impl.ClientEvent;
import me.rebirthclient.api.events.impl.Render2DEvent;
import me.rebirthclient.api.managers.Managers;
import me.rebirthclient.api.util.FadeUtils;
import me.rebirthclient.api.util.Timer;
import me.rebirthclient.api.util.render.ColorUtil;
import me.rebirthclient.api.util.render.RenderUtil;
import me.rebirthclient.mod.modules.Category;
import me.rebirthclient.mod.modules.Module;
import me.rebirthclient.mod.modules.settings.Setting;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

public class ArrayList
extends Module {
    private final Setting<Integer> listX = this.add(new Setting<>("X", 0, 0, 2000));
    private final Setting<Integer> listY = this.add(new Setting<>("Y", 10, 1, 2000));
    private final Setting<Integer> animationTime = this.add(new Setting<>("AnimationTime", 300, 0, 1000));
    private final Setting<Boolean> forgeHax = this.add(new Setting<>("ForgeHax", true));
    private final Setting<Boolean> reverse = this.add(new Setting<>("Reverse", false));
    private final Setting<Boolean> fps = this.add(new Setting<>("Fps", true));
    private final Setting<Boolean> onlyDrawn = this.add(new Setting<>("OnlyDrawn", true));
    private final Setting<Boolean> onlyBind = this.add(new Setting<>("OnlyBind", false));
    private final Setting<Boolean> animationY = this.add(new Setting<>("AnimationY", true));
    public final Setting<ColorMode> colorMode = this.add(new Setting<>("ColorMode", ColorMode.Pulse));
    private final Setting<Integer> rainbowSpeed = this.register(new Setting<>("RainbowSpeed", 200, 1, 400, v -> this.colorMode.getValue() == ColorMode.Rainbow || this.colorMode.getValue() == ColorMode.PulseRainbow));
    private final Setting<Float> saturation = this.register(new Setting<>("Saturation", 130.0f, 1.0f, 255.0f, v -> this.colorMode.getValue() == ColorMode.Rainbow || this.colorMode.getValue() == ColorMode.PulseRainbow));
    private final Setting<Integer> pulseSpeed = this.register(new Setting<>("PulseSpeed", 100, 1, 400, v -> this.colorMode.getValue() == ColorMode.Pulse || this.colorMode.getValue() == ColorMode.PulseRainbow));
    public final Setting<Integer> rainbowDelay = this.add(new Setting<>("Delay", 350, 0, 600, v -> this.colorMode.getValue() == ColorMode.Rainbow));
    private final Setting<Color> color = this.add(new Setting<>("Color", new Color(255, 255, 255, 255), v -> this.colorMode.getValue() != ColorMode.Rainbow).hideAlpha());
    private final Setting<Boolean> rect = this.add(new Setting<>("Rect", true));
    private final Setting<Boolean> backGround = this.add(new Setting<>("BackGround", true).setParent());
    private final Setting<Boolean> bgSync = this.add(new Setting<>("Sync", false, v -> this.backGround.isOpen()));
    private final Setting<Color> bgColor = this.add(new Setting<>("BGColor", new Color(0, 0, 0, 100), v -> this.backGround.isOpen()));
    private List<Modules> Map = new java.util.ArrayList<>();
    private final Timer updateTimer = new Timer();
    private boolean needUpdate = false;
    int progress = 0;
    int pulseProgress = 0;

    public ArrayList() {
        super("ArrayList", "", Category.CLIENT);
    }

    @Override
    public void onLoad() {
        for (Module module : Managers.MODULES.getModules()) {
            this.Map.add(new Modules(module));
        }
    }

    @Override
    public void onLogin() {
        this.needUpdate = true;
    }

    @SubscribeEvent
    public void clientEvent(ClientEvent event) {
        if (!this.updateTimer.passedMs(5000L)) {
            return;
        }
        this.updateTimer.reset();
        this.needUpdate = true;
    }

    @Override
    public void onTick() {
        this.progress += this.rainbowSpeed.getValue();
        this.pulseProgress += this.pulseSpeed.getValue();
    }

    @SubscribeEvent
    public void onRender(TickEvent.RenderTickEvent event) {
        if (this.fps.getValue()) {
            return;
        }
        this.doRender();
    }

    @Override
    public void onRender2D(Render2DEvent event) {
        if (!this.fps.getValue()) {
            return;
        }
        this.doRender();
    }

    private void doRender() {
        if (ArrayList.fullNullCheck()) {
            return;
        }
        if (this.needUpdate) {
            this.Map = this.Map.stream().sorted(Comparator.comparing(module -> Managers.TEXT.getStringWidth(module.module.getArrayListInfo()) * -1)).collect(Collectors.toList());
        }
        int lastY = this.listY.getValue();
        int counter = 20;
        for (Modules modules : this.Map) {
            int showX;
            int y;
            int x;
            double size;
            if (!modules.module.isDrawn() && this.onlyDrawn.getValue() || this.onlyBind.getValue() && modules.module.getBind().getKey() == -1) continue;
            modules.fade.setLength(this.animationTime.getValue());
            if (modules.module.isOn()) {
                modules.enable();
            } else {
                modules.disable();
            }
            modules.updateName();
            if (!this.reverse.getValue()) {
                if (modules.isEnabled) {
                    size = modules.fade.easeOutQuad();
                    x = (int)((double)Managers.TEXT.getStringWidth(modules.module.getArrayListInfo() + this.getSuffix()) * size);
                    modules.lastY = y = (int)((double)Managers.TEXT.getFontHeight2() * size);
                    modules.lastX = x;
                } else {
                    size = Math.abs(modules.fade.easeOutQuad() - 1.0);
                    x = (int)((double)modules.lastX * size);
                    y = (int)((double)modules.lastY * size);
                    if (size <= 0.0) {
                        continue;
                    }
                }
            } else if (modules.isEnabled) {
                size = Math.abs(modules.fade.easeOutQuad() - 1.0);
                x = (int)((double)Managers.TEXT.getStringWidth(modules.module.getArrayListInfo() + this.getSuffix()) * size);
                size = modules.fade.easeOutQuad();
                modules.lastY = y = (int)((double)Managers.TEXT.getFontHeight2() * size);
                modules.lastX = x;
            } else {
                size = modules.fade.easeOutQuad();
                x = (int)((double)Managers.TEXT.getStringWidth(modules.module.getArrayListInfo() + this.getSuffix()) * size) + modules.lastX;
                size = Math.abs(modules.fade.easeOutQuad() - 1.0);
                y = (int)((double)modules.lastY * size);
                if (size <= 0.0 || x >= Managers.TEXT.getStringWidth(modules.module.getArrayListInfo() + this.getSuffix())) continue;
            }
            x = (int)((double)x + 20.0 * Math.abs(modules.change.easeOutQuad() - 1.0));
            ++counter;
            if (!this.reverse.getValue()) {
                showX = Managers.TEXT.scaledWidth - x - this.listX.getValue() - (this.rect.getValue() ? 2 : 0);
                if (this.backGround.getValue()) {
                    RenderUtil.drawRect(showX, lastY - (this.animationY.getValue() ? Math.abs(y - Managers.TEXT.getFontHeight2()) : 0) - 1, Managers.TEXT.scaledWidth - this.listX.getValue(), lastY - (this.animationY.getValue() ? Math.abs(y - Managers.TEXT.getFontHeight2()) : 0) + Managers.TEXT.getFontHeight2() - 1, this.bgSync.getValue() ? ColorUtil.injectAlpha(this.getColor(counter), this.bgColor.getValue().getAlpha()) : this.bgColor.getValue().getRGB());
                }
                if (this.rect.getValue()) {
                    RenderUtil.drawRect(Managers.TEXT.scaledWidth - this.listX.getValue() - 1, lastY - (this.animationY.getValue() ? Math.abs(y - Managers.TEXT.getFontHeight2()) : 0) - 1, Managers.TEXT.scaledWidth - this.listX.getValue(), lastY - (this.animationY.getValue() ? Math.abs(y - Managers.TEXT.getFontHeight2()) : 0) + Managers.TEXT.getFontHeight2(), this.getColor(counter));
                }
            } else {
                showX = -x + this.listX.getValue() + (this.rect.getValue() ? 2 : 0);
                if (this.rect.getValue()) {
                    RenderUtil.drawRect(this.listX.getValue(), lastY - (this.animationY.getValue() ? Math.abs(y - Managers.TEXT.getFontHeight2()) : 0) - 1, this.listX.getValue() + 1, lastY - (this.animationY.getValue() ? Math.abs(y - Managers.TEXT.getFontHeight2()) : 0) + Managers.TEXT.getFontHeight2() - 1, this.getColor(counter));
                }
                if (this.backGround.getValue()) {
                    RenderUtil.drawRect(this.listX.getValue(), lastY - (this.animationY.getValue() ? Math.abs(y - Managers.TEXT.getFontHeight2()) : 0) - 1, Math.abs(x - Managers.TEXT.getStringWidth(modules.module.getArrayListInfo() + this.getSuffix())) + (this.rect.getValue() ? 2 : 0), lastY - (this.animationY.getValue() ? Math.abs(y - Managers.TEXT.getFontHeight2()) : 0) + Managers.TEXT.getFontHeight2() - 1, this.bgSync.getValue() ? ColorUtil.injectAlpha(this.getColor(counter), this.bgColor.getValue().getAlpha()) : this.bgColor.getValue().getRGB());
                }
            }
            Managers.TEXT.drawString(modules.module.getArrayListInfo() + this.getSuffix(), showX, lastY - (this.animationY.getValue() ? Math.abs(y - Managers.TEXT.getFontHeight2()) : 0), this.getColor(counter), true);
            lastY += y;
        }
    }

    private String getSuffix() {
        if (this.forgeHax.getValue()) {
            return "\u00a7r<";
        }
        return "";
    }

    private int getColor(int counter) {
        if (this.colorMode.getValue() != ColorMode.Custom) {
            return this.rainbow(counter).getRGB();
        }
        return this.color.getValue().getRGB();
    }

    private Color rainbow(int delay) {
        double rainbowState = Math.ceil((double)(this.progress + delay * this.rainbowDelay.getValue()) / 20.0);
        if (this.colorMode.getValue() == ColorMode.Pulse) {
            return this.pulseColor(this.color.getValue(), delay);
        }
        if (this.colorMode.getValue() == ColorMode.Rainbow) {
            return Color.getHSBColor((float)(rainbowState % 360.0 / 360.0), this.saturation.getValue() / 255.0f, 1.0f);
        }
        return this.pulseColor(Color.getHSBColor((float)(rainbowState % 360.0 / 360.0), this.saturation.getValue() / 255.0f, 1.0f), delay);
    }

    private Color pulseColor(Color color, int index) {
        float[] hsb = new float[3];
        Color.RGBtoHSB(color.getRed(), color.getGreen(), color.getBlue(), hsb);
        float brightness = Math.abs(((float)((long)this.pulseProgress % 2000L) / Float.intBitsToFloat(Float.floatToIntBits(0.0013786979f) ^ 0x7ECEB56D) + (float)index / 14.0f * Float.intBitsToFloat(Float.floatToIntBits(0.09192204f) ^ 0x7DBC419F)) % Float.intBitsToFloat(Float.floatToIntBits(0.7858098f) ^ 0x7F492AD5) - Float.intBitsToFloat(Float.floatToIntBits(6.46708f) ^ 0x7F4EF252));
        brightness = Float.intBitsToFloat(Float.floatToIntBits(18.996923f) ^ 0x7E97F9B3) + Float.intBitsToFloat(Float.floatToIntBits(2.7958195f) ^ 0x7F32EEB5) * brightness;
        hsb[2] = brightness % Float.intBitsToFloat(Float.floatToIntBits(0.8992331f) ^ 0x7F663424);
        return new Color(Color.HSBtoRGB(hsb[0], hsb[1], hsb[2]));
    }

    public static class Modules {
        public final FadeUtils fade;
        public final FadeUtils change;
        public boolean isEnabled = false;
        public final Module module;
        public int lastX = 0;
        public int lastY = 0;
        public String lastName;

        public Modules(Module module) {
            this.module = module;
            this.fade = new FadeUtils(500L);
            this.change = new FadeUtils(200L);
            this.lastName = module.getArrayListInfo();
        }

        public void enable() {
            if (this.isEnabled) {
                return;
            }
            this.isEnabled = true;
            this.fade.reset();
        }

        public void disable() {
            if (!this.isEnabled) {
                return;
            }
            this.isEnabled = false;
            this.fade.reset();
        }

        public void updateName() {
            if (!this.lastName.equals(this.module.getArrayListInfo())) {
                this.lastName = this.module.getArrayListInfo();
                this.change.reset();
            }
        }
    }

    private static enum ColorMode {
        Custom,
        Pulse,
        Rainbow,
        PulseRainbow

    }
}

