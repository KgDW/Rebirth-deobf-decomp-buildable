package me.rebirthclient.mod.gui.click.items.buttons;

import com.mojang.realmsclient.gui.ChatFormatting;
import java.util.Objects;
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

public class EnumButton
extends Button {
    public final Setting setting;

    public EnumButton(Setting setting) {
        super(setting.getName());
        this.setting = setting;
        this.width = 15;
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        boolean dotgod;
        boolean newStyle = ClickGui.INSTANCE.style.getValue() == ClickGui.Style.NEW || ClickGui.INSTANCE.style.getValue() == ClickGui.Style.DOTGOD;
        boolean future = ClickGui.INSTANCE.style.getValue() == ClickGui.Style.FUTURE;
        boolean bl = dotgod = ClickGui.INSTANCE.style.getValue() == ClickGui.Style.DOTGOD;
        if (future) {
            RenderUtil.drawRect(this.x, this.y, this.x + (float)this.width + 7.4f, this.y + (float)this.height - 0.5f, this.getState() ? (!this.isHovering(mouseX, mouseY) ? Managers.COLORS.getCurrentWithAlpha(99) : Managers.COLORS.getCurrentWithAlpha(120)) : (!this.isHovering(mouseX, mouseY) ? Managers.COLORS.getCurrentWithAlpha(26) : Managers.COLORS.getCurrentWithAlpha(55)));
        } else if (dotgod) {
            RenderUtil.drawRect(this.x, this.y, this.x + (float)this.width + 7.4f, this.y + (float)this.height - 0.5f, this.getState() ? (!this.isHovering(mouseX, mouseY) ? Managers.COLORS.getCurrentWithAlpha(65) : Managers.COLORS.getCurrentWithAlpha(90)) : (!this.isHovering(mouseX, mouseY) ? Managers.COLORS.getCurrentWithAlpha(26) : Managers.COLORS.getCurrentWithAlpha(35)));
        } else {
            RenderUtil.drawRect(this.x, this.y, this.x + (float)this.width + 7.4f, this.y + (float)this.height - 0.5f, this.getState() ? (!this.isHovering(mouseX, mouseY) ? Managers.COLORS.getCurrentWithAlpha(120) : Managers.COLORS.getCurrentWithAlpha(200)) : (!this.isHovering(mouseX, mouseY) ? 0x11555555 : -2007673515));
        }
        Managers.TEXT.drawStringWithShadow((newStyle ? this.setting.getName().toLowerCase() + ":" : this.setting.getName()) + " " + ChatFormatting.GRAY + (this.setting.getCurrentEnumName().equalsIgnoreCase("ABC") ? "ABC" : this.setting.getCurrentEnumName()), this.x + 2.3f, this.y - 1.7f - (float)Gui.INSTANCE.getTextOffset(), this.getState() ? -1 : -5592406);
        int y = (int)this.y;
        if (this.setting.open) {
            for (Object o : this.setting.getValue().getClass().getEnumConstants()) {
                String s = !Objects.equals(o.toString(), "ABC") ? Character.toUpperCase(o.toString().charAt(0)) + o.toString().toLowerCase().substring(1) : o.toString();
                Managers.TEXT.drawStringWithShadow((this.setting.getCurrentEnumName().equals(s) ? ChatFormatting.WHITE : ChatFormatting.GRAY) + s, (float)this.width / 2.0f - (float)Managers.TEXT.getStringWidth(s) / 2.0f + 2.0f + this.x, (float)(y += 12) + 6.0f - (float)EnumButton.mc.fontRenderer.FONT_HEIGHT / 2.0f + 3.5f, -1);
            }
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
        if (mouseButton == 1 && this.isHovering(mouseX, mouseY)) {
            this.setting.open = !this.setting.open;
            mc.getSoundHandler().playSound(PositionedSoundRecord.getMasterRecord(SoundEvents.UI_BUTTON_CLICK, 1.0f));
        }
        if (this.setting.open) {
            for (Object o : this.setting.getValue().getClass().getEnumConstants()) {
                this.y += 12.0f;
                if (!((float)mouseX > this.x) || !((float)mouseX < this.x + (float)this.width) || !((float)mouseY > this.y) || !((float)mouseY < this.y + 12.0f + 3.5f) || mouseButton != 0) continue;
                this.setting.setEnumValue(String.valueOf(o));
                mc.getSoundHandler().playSound(PositionedSoundRecord.getMasterRecord(SoundEvents.UI_BUTTON_CLICK, 1.0f));
            }
        }
    }

    @Override
    public int getHeight() {
        return ClickGui.INSTANCE.getButtonHeight() - 1;
    }

    @Override
    public void toggle() {
        this.setting.increaseEnum();
    }

    @Override
    public boolean getState() {
        return true;
    }
}

