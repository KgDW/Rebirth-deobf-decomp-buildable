package me.rebirthclient.mod.gui.click.items.buttons;

import com.mojang.realmsclient.gui.ChatFormatting;
import me.rebirthclient.api.managers.Managers;
import me.rebirthclient.api.util.render.RenderUtil;
import me.rebirthclient.mod.gui.click.items.buttons.Button;
import me.rebirthclient.mod.gui.screen.Gui;
import me.rebirthclient.mod.modules.impl.client.ClickGui;
import me.rebirthclient.mod.modules.settings.Setting;
import net.minecraft.client.audio.ISound;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.init.SoundEvents;
import net.minecraft.util.SoundEvent;

public class UnlimitedSlider
extends Button {
    public final Setting setting;

    public UnlimitedSlider(Setting setting) {
        super(setting.getName());
        this.setting = setting;
        this.width = 15;
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        boolean newStyle = ClickGui.INSTANCE.style.getValue() == ClickGui.Style.NEW;
        RenderUtil.drawRect(this.x, this.y, this.x + (float)this.width + 7.4f, this.y + (float)this.height - 0.5f, !this.isHovering(mouseX, mouseY) ? Managers.COLORS.getCurrentWithAlpha(120) : Managers.COLORS.getCurrentWithAlpha(200));
        RenderUtil.drawLine(this.x + 1.0f, this.y, this.x + 1.0f, this.y + (float)this.height - 0.5f, 0.9f, Managers.COLORS.getCurrentWithAlpha(255));
        Managers.TEXT.drawStringWithShadow(" - " + (newStyle ? this.setting.getName().toLowerCase() + ":" : this.setting.getName()) + " " + ChatFormatting.GRAY + this.setting.getValue() + ChatFormatting.WHITE + " +", this.x + 2.3f, this.y - 1.7f - (float)Gui.INSTANCE.getTextOffset(), this.getState() ? -1 : -5592406);
    }

    @Override
    public void mouseClicked(int mouseX, int mouseY, int mouseButton) {
        super.mouseClicked(mouseX, mouseY, mouseButton);
        if (this.isHovering(mouseX, mouseY)) {
            mc.getSoundHandler().playSound(PositionedSoundRecord.getMasterRecord(SoundEvents.UI_BUTTON_CLICK, 1.0f));
            if (this.isRight(mouseX)) {
                if (this.setting.getValue() instanceof Double) {
                    this.setting.setValue((Double)this.setting.getValue() + 1.0);
                } else if (this.setting.getValue() instanceof Float) {
                    this.setting.setValue((Float) this.setting.getValue() + 1.0f);
                } else if (this.setting.getValue() instanceof Integer) {
                    this.setting.setValue((Integer)this.setting.getValue() + 1);
                }
            } else if (this.setting.getValue() instanceof Double) {
                this.setting.setValue((Double)this.setting.getValue() - 1.0);
            } else if (this.setting.getValue() instanceof Float) {
                this.setting.setValue((Float) this.setting.getValue() - 1.0f);
            } else if (this.setting.getValue() instanceof Integer) {
                this.setting.setValue((Integer)this.setting.getValue() - 1);
            }
        }
    }

    @Override
    public void update() {
        this.setHidden(!this.setting.isVisible());
    }

    @Override
    public int getHeight() {
        return 14;
    }

    @Override
    public void toggle() {
    }

    @Override
    public boolean getState() {
        return true;
    }

    public boolean isRight(int x) {
        return (float)x > this.x + ((float)this.width + 7.4f) / 2.0f;
    }
}

