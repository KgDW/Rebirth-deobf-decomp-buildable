package me.rebirthclient.mod.gui.click.items.buttons;

import com.mojang.realmsclient.gui.ChatFormatting;
import java.awt.Color;
import me.rebirthclient.api.managers.Managers;
import me.rebirthclient.api.util.render.ColorUtil;
import me.rebirthclient.api.util.render.RenderUtil;
import me.rebirthclient.mod.gui.click.Component;
import me.rebirthclient.mod.gui.click.items.buttons.Button;
import me.rebirthclient.mod.gui.screen.Gui;
import me.rebirthclient.mod.modules.impl.client.ClickGui;
import me.rebirthclient.mod.modules.settings.Setting;
import org.lwjgl.input.Mouse;

public class Slider
extends Button {
    public final Setting setting;
    private final Number min;
    private final Number max;
    private final int difference;
    private float renderWidth;
    private float prevRenderWidth;

    public Slider(Setting setting) {
        super(setting.getName());
        this.setting = setting;
        this.min = (Number)setting.getMinValue();
        this.max = (Number)setting.getMaxValue();
        this.difference = this.max.intValue() - this.min.intValue();
        this.width = 15;
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        boolean newStyle = ClickGui.INSTANCE.style.getValue() == ClickGui.Style.NEW;
        boolean future = ClickGui.INSTANCE.style.getValue() == ClickGui.Style.FUTURE;
        boolean dotgod = ClickGui.INSTANCE.style.getValue() == ClickGui.Style.DOTGOD;
        this.dragSetting(mouseX, mouseY);
        this.setRenderWidth(this.x + ((float)this.width + 7.4f) * this.partialMultiplier());
        RenderUtil.drawRect(this.x, this.y, this.x + (float)this.width + 7.4f, this.y + (float)this.height - 0.5f, !this.isHovering(mouseX, mouseY) ? 0x11555555 : -2007673515);
        if (future) {
            RenderUtil.drawRect(this.x, this.y, ((Number)this.setting.getValue()).floatValue() <= this.min.floatValue() ? this.x : this.getRenderWidth(), this.y + (float)this.height - 0.5f, !this.isHovering(mouseX, mouseY) ? Managers.COLORS.getCurrentWithAlpha(99) : Managers.COLORS.getCurrentWithAlpha(120));
        } else if (dotgod) {
            RenderUtil.drawRect(this.x, this.y, ((Number)this.setting.getValue()).floatValue() <= this.min.floatValue() ? this.x : this.getRenderWidth(), this.y + (float)this.height - 0.5f, !this.isHovering(mouseX, mouseY) ? Managers.COLORS.getCurrentWithAlpha(65) : Managers.COLORS.getCurrentWithAlpha(90));
        } else {
            if (this.isHovering(mouseX, mouseY) && Mouse.isButtonDown(0)) {
                RenderUtil.drawHGradientRect(this.x, this.y, ((Number)this.setting.getValue()).floatValue() <= this.min.floatValue() ? this.x : this.getRenderWidth(), this.y + (float)this.height - 0.5f, ColorUtil.pulseColor(new Color(ClickGui.INSTANCE.color.getValue().getRed(), ClickGui.INSTANCE.color.getValue().getGreen(), ClickGui.INSTANCE.color.getValue().getBlue(), 200), 50, 1).getRGB(), ColorUtil.pulseColor(new Color(ClickGui.INSTANCE.color.getValue().getRed(), ClickGui.INSTANCE.color.getValue().getGreen(), ClickGui.INSTANCE.color.getValue().getBlue(), 200), 50, 1000).getRGB());
            } else {
                RenderUtil.drawRect(this.x, this.y, ((Number)this.setting.getValue()).floatValue() <= this.min.floatValue() ? this.x : this.getRenderWidth(), this.y + (float)this.height - 0.5f, !this.isHovering(mouseX, mouseY) ? Managers.COLORS.getCurrentWithAlpha(120) : Managers.COLORS.getCurrentWithAlpha(200));
            }
            RenderUtil.drawLine(this.x + 1.0f, this.y, this.x + 1.0f, this.y + (float)this.height - 0.5f, 0.9f, Managers.COLORS.getCurrentWithAlpha(255));
        }
        if (dotgod) {
            Managers.TEXT.drawStringWithShadow(this.getName().toLowerCase() + ":" + " " + ChatFormatting.GRAY + (this.setting.getValue() instanceof Float ? this.setting.getValue() : Double.valueOf(((Number)this.setting.getValue()).doubleValue())), this.x + 2.3f, this.y - 1.7f - (float)Gui.INSTANCE.getTextOffset(), Managers.COLORS.getCurrentGui(240));
        } else {
            Managers.TEXT.drawStringWithShadow((newStyle ? this.getName().toLowerCase() + ":" : this.getName()) + " " + ChatFormatting.GRAY + (this.setting.getValue() instanceof Float ? this.setting.getValue() : Double.valueOf(((Number)this.setting.getValue()).doubleValue())), this.x + 2.3f, this.y - 1.7f - (float)Gui.INSTANCE.getTextOffset(), -1);
        }
    }

    @Override
    public void mouseClicked(int mouseX, int mouseY, int mouseButton) {
        super.mouseClicked(mouseX, mouseY, mouseButton);
        if (this.isHovering(mouseX, mouseY)) {
            this.setSettingFromX(mouseX);
        }
    }

    @Override
    public boolean isHovering(int mouseX, int mouseY) {
        for (Component component : Gui.INSTANCE.getComponents()) {
            if (!component.drag) continue;
            return false;
        }
        return (float)mouseX >= this.getX() && (float)mouseX <= this.getX() + (float)this.getWidth() + 8.0f && (float)mouseY >= this.getY() && (float)mouseY <= this.getY() + (float)this.height;
    }

    @Override
    public void update() {
        this.setHidden(!this.setting.isVisible());
    }

    private void dragSetting(int mouseX, int mouseY) {
        if (this.isHovering(mouseX, mouseY) && Mouse.isButtonDown(0)) {
            this.setSettingFromX(mouseX);
        }
    }

    @Override
    public int getHeight() {
        return ClickGui.INSTANCE.getButtonHeight() - 1;
    }

    private void setSettingFromX(int mouseX) {
        float percent = ((float)mouseX - this.x) / ((float)this.width + 7.4f);
        if (this.setting.getValue() instanceof Double) {
            double result = (Double)this.setting.getMinValue() + (double)((float)this.difference * percent);
            this.setting.setValue((double)Math.round(10.0 * result) / 10.0);
        } else if (this.setting.getValue() instanceof Float) {
            float result = (Float) this.setting.getMinValue() + (float)this.difference * percent;
            this.setting.setValue((float) Math.round(10.0f * result) / 10.0f);
        } else if (this.setting.getValue() instanceof Integer) {
            this.setting.setValue((Integer)this.setting.getMinValue() + (int)((float)this.difference * percent));
        }
    }

    private float middle() {
        return this.max.floatValue() - this.min.floatValue();
    }

    private float part() {
        return ((Number)this.setting.getValue()).floatValue() - this.min.floatValue();
    }

    private float partialMultiplier() {
        return this.part() / this.middle();
    }

    public float getRenderWidth() {
        if (Managers.FPS.getFPS() < 20) {
            return this.renderWidth;
        }
        this.renderWidth = this.prevRenderWidth + (this.renderWidth - this.prevRenderWidth) * mc.getRenderPartialTicks() / (8.0f * ((float)Math.min(240, Managers.FPS.getFPS()) / 240.0f));
        return this.renderWidth;
    }

    public void setRenderWidth(float renderWidth) {
        if (this.renderWidth == renderWidth) {
            return;
        }
        this.prevRenderWidth = this.renderWidth;
        this.renderWidth = renderWidth;
    }
}

