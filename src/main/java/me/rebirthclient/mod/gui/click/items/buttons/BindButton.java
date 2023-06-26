package me.rebirthclient.mod.gui.click.items.buttons;

import com.mojang.realmsclient.gui.ChatFormatting;
import me.rebirthclient.api.managers.Managers;
import me.rebirthclient.api.util.render.ColorUtil;
import me.rebirthclient.api.util.render.RenderUtil;
import me.rebirthclient.mod.gui.click.items.buttons.Button;
import me.rebirthclient.mod.gui.screen.Gui;
import me.rebirthclient.mod.modules.impl.client.ClickGui;
import me.rebirthclient.mod.modules.settings.Bind;
import me.rebirthclient.mod.modules.settings.Setting;
import net.minecraft.client.audio.ISound;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.init.SoundEvents;
import net.minecraft.util.SoundEvent;

public class BindButton
extends Button {
    private final Setting setting;
    public boolean isListening;

    public BindButton(Setting setting) {
        super(setting.getName());
        this.setting = setting;
        this.width = 15;
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        boolean newStyle = ClickGui.INSTANCE.style.getValue() == ClickGui.Style.NEW || ClickGui.INSTANCE.style.getValue() == ClickGui.Style.DOTGOD;
        int color = ColorUtil.toARGB(ClickGui.INSTANCE.color.getValue().getRed(), ClickGui.INSTANCE.color.getValue().getGreen(), ClickGui.INSTANCE.color.getValue().getBlue(), 255);
        RenderUtil.drawRect(this.x, this.y, this.x + (float)this.width + 7.4f, this.y + (float)this.height - 0.5f, this.getState() ? (!this.isHovering(mouseX, mouseY) ? 0x11555555 : -2007673515) : (!this.isHovering(mouseX, mouseY) ? Managers.COLORS.getCurrentGui(200) : Managers.COLORS.getCurrentGui(90)));
        if (this.isListening) {
            Managers.TEXT.drawStringWithShadow("Press a Key...", this.x + 2.3f, this.y - 1.7f - (float)Gui.INSTANCE.getTextOffset(), -1);
        } else {
            Managers.TEXT.drawStringWithShadow((newStyle ? this.setting.getName().toLowerCase() : this.setting.getName()) + " " + ChatFormatting.GRAY + this.setting.getValue().toString().toUpperCase(), this.x + 2.3f, this.y - 1.7f - (float)Gui.INSTANCE.getTextOffset(), this.getState() ? -1 : -5592406);
        }
    }

    @Override
    public void update() {
        this.setHidden(!this.setting.isVisible());
    }

    @Override
    public void mouseClicked(int mouseX, int mouseY, int mouseButton) {
        super.mouseClicked(mouseX, mouseY, mouseButton);
        if (this.isHovering(mouseX, mouseY)) {
            mc.getSoundHandler().playSound(PositionedSoundRecord.getMasterRecord(SoundEvents.UI_BUTTON_CLICK, 1.0f));
        }
    }

    @Override
    public void onKeyTyped(char typedChar, int keyCode) {
        if (this.isListening) {
            Bind bind = new Bind(keyCode);
            if (bind.toString().equalsIgnoreCase("Escape")) {
                return;
            }
            if (bind.toString().equalsIgnoreCase("Delete")) {
                bind = new Bind(-1);
            }
            this.setting.setValue(bind);
            this.onMouseClick();
        }
    }

    @Override
    public int getHeight() {
        return ClickGui.INSTANCE.getButtonHeight() - 1;
    }

    @Override
    public void toggle() {
        this.isListening = !this.isListening;
    }

    @Override
    public boolean getState() {
        return !this.isListening;
    }
}

