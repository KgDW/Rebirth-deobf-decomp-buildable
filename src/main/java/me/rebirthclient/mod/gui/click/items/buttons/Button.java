package me.rebirthclient.mod.gui.click.items.buttons;

import me.rebirthclient.api.managers.Managers;
import me.rebirthclient.api.util.render.RenderUtil;
import me.rebirthclient.mod.gui.click.Component;
import me.rebirthclient.mod.gui.click.items.Item;
import me.rebirthclient.mod.gui.screen.Gui;
import me.rebirthclient.mod.modules.impl.client.ClickGui;
import net.minecraft.client.audio.ISound;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.init.SoundEvents;
import net.minecraft.util.SoundEvent;

public class Button
extends Item {
    private boolean state;

    public Button(String name) {
        super(name);
        this.height = ClickGui.INSTANCE.getButtonHeight();
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        boolean dotgod;
        boolean newStyle = ClickGui.INSTANCE.style.getValue() == ClickGui.Style.NEW;
        boolean future = ClickGui.INSTANCE.style.getValue() == ClickGui.Style.FUTURE;
        boolean bl = dotgod = ClickGui.INSTANCE.style.getValue() == ClickGui.Style.DOTGOD;
        if (newStyle) {
            RenderUtil.drawRect(this.x, this.y, this.x + (float)this.width, this.y + (float)this.height - 0.5f, !this.isHovering(mouseX, mouseY) ? 0x11555555 : -2007673515);
            Managers.TEXT.drawStringWithShadow(this.getName(), this.x + 2.3f, this.y - 2.0f - (float)Gui.INSTANCE.getTextOffset(), this.getState() ? Managers.COLORS.getCurrentGui(240) : -1);
        } else if (dotgod) {
            RenderUtil.drawRect(this.x, this.y, this.x + (float)this.width, this.y + (float)this.height - 0.5f, this.getState() ? (!this.isHovering(mouseX, mouseY) ? Managers.COLORS.getCurrentWithAlpha(65) : Managers.COLORS.getCurrentWithAlpha(90)) : (!this.isHovering(mouseX, mouseY) ? Managers.COLORS.getCurrentWithAlpha(26) : Managers.COLORS.getCurrentWithAlpha(35)));
            Managers.TEXT.drawStringWithShadow(this.getName(), this.x + 2.3f, this.y - 2.0f - (float)Gui.INSTANCE.getTextOffset(), this.getState() ? Managers.COLORS.getCurrentGui(240) : 0xB0B0B0);
        } else if (future) {
            RenderUtil.drawRect(this.x, this.y, this.x + (float)this.width, this.y + (float)this.height - 0.5f, this.getState() ? (!this.isHovering(mouseX, mouseY) ? Managers.COLORS.getCurrentWithAlpha(99) : Managers.COLORS.getCurrentWithAlpha(120)) : (!this.isHovering(mouseX, mouseY) ? Managers.COLORS.getCurrentWithAlpha(26) : Managers.COLORS.getCurrentWithAlpha(55)));
            Managers.TEXT.drawStringWithShadow(this.getName(), this.x + 2.3f, this.y - 2.0f - (float)Gui.INSTANCE.getTextOffset(), this.getState() ? -1 : -5592406);
        } else {
            RenderUtil.drawRect(this.x, this.y, this.x + (float)this.width, this.y + (float)this.height - 0.5f, this.getState() ? (!this.isHovering(mouseX, mouseY) ? Managers.COLORS.getCurrentWithAlpha(120) : Managers.COLORS.getCurrentWithAlpha(200)) : (!this.isHovering(mouseX, mouseY) ? 0x11555555 : -2007673515));
            Managers.TEXT.drawStringWithShadow(this.getName(), this.x + 2.3f, this.y - 2.0f - (float)Gui.INSTANCE.getTextOffset(), this.getState() ? -1 : -5592406);
        }
    }

    @Override
    public void mouseClicked(int mouseX, int mouseY, int mouseButton) {
        if (mouseButton == 0 && this.isHovering(mouseX, mouseY)) {
            this.onMouseClick();
        }
    }

    public void onMouseClick() {
        this.state = !this.state;
        this.toggle();
        mc.getSoundHandler().playSound(PositionedSoundRecord.getMasterRecord(SoundEvents.UI_BUTTON_CLICK, 1.0f));
    }

    public void toggle() {
    }

    public boolean getState() {
        return this.state;
    }

    @Override
    public int getHeight() {
        return ClickGui.INSTANCE.getButtonHeight() - 1;
    }

    public boolean isHovering(int mouseX, int mouseY) {
        for (Component component : Gui.INSTANCE.getComponents()) {
            if (!component.drag) continue;
            return false;
        }
        return (float)mouseX >= this.getX() && (float)mouseX <= this.getX() + (float)this.getWidth() && (float)mouseY >= this.getY() && (float)mouseY <= this.getY() + (float)this.height;
    }
}

