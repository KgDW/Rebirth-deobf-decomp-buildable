package me.rebirthclient.mod.gui.click.items.buttons;

import com.mojang.realmsclient.gui.ChatFormatting;
import me.rebirthclient.api.managers.Managers;
import me.rebirthclient.api.util.render.RenderUtil;
import me.rebirthclient.mod.gui.click.Component;
import me.rebirthclient.mod.gui.click.items.buttons.Button;
import me.rebirthclient.mod.gui.screen.Gui;
import me.rebirthclient.mod.modules.impl.client.ClickGui;
import me.rebirthclient.mod.modules.settings.Setting;
import net.minecraft.client.audio.ISound;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.init.SoundEvents;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;

public class BooleanButton
extends Button {
    private final Setting setting;
    private int progress = 0;

    public BooleanButton(Setting setting) {
        super(setting.getName());
        this.setting = setting;
        this.width = 15;
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        boolean dotgod;
        boolean newStyle = ClickGui.INSTANCE.style.getValue() == ClickGui.Style.NEW;
        boolean future = ClickGui.INSTANCE.style.getValue() == ClickGui.Style.FUTURE;
        boolean bl = dotgod = ClickGui.INSTANCE.style.getValue() == ClickGui.Style.DOTGOD;
        if (future) {
            RenderUtil.drawRect(this.x, this.y, this.x + (float)this.width + 7.4f, this.y + (float)this.height - 0.5f, this.getState() ? (!this.isHovering(mouseX, mouseY) ? Managers.COLORS.getCurrentWithAlpha(99) : Managers.COLORS.getCurrentWithAlpha(120)) : (!this.isHovering(mouseX, mouseY) ? Managers.COLORS.getCurrentWithAlpha(26) : Managers.COLORS.getCurrentWithAlpha(55)));
            Managers.TEXT.drawStringWithShadow(newStyle ? this.getName().toLowerCase() : this.getName(), this.x + 2.3f, this.y - 1.7f - (float)Gui.INSTANCE.getTextOffset(), this.getState() ? -1 : -5592406);
        } else if (dotgod) {
            RenderUtil.drawRect(this.x, this.y, this.x + (float)this.width + 7.4f, this.y + (float)this.height - 0.5f, this.getState() ? (!this.isHovering(mouseX, mouseY) ? Managers.COLORS.getCurrentWithAlpha(65) : Managers.COLORS.getCurrentWithAlpha(90)) : (!this.isHovering(mouseX, mouseY) ? Managers.COLORS.getCurrentWithAlpha(26) : Managers.COLORS.getCurrentWithAlpha(55)));
            Managers.TEXT.drawStringWithShadow(this.getName().toLowerCase(), this.x + 2.3f, this.y - 1.7f - (float)Gui.INSTANCE.getTextOffset(), this.getState() ? Managers.COLORS.getCurrentGui(240) : 0xB0B0B0);
        } else {
            RenderUtil.drawRect(this.x, this.y, this.x + (float)this.width + 7.4f, this.y + (float)this.height - 0.5f, this.getState() ? (!this.isHovering(mouseX, mouseY) ? Managers.COLORS.getCurrentWithAlpha(120) : Managers.COLORS.getCurrentWithAlpha(200)) : (!this.isHovering(mouseX, mouseY) ? 0x11555555 : -2007673515));
            Managers.TEXT.drawStringWithShadow(newStyle ? this.getName().toLowerCase() : this.getName(), this.x + 2.3f, this.y - 1.7f - (float)Gui.INSTANCE.getTextOffset(), this.getState() ? -1 : -5592406);
        }
        if (this.setting.parent) {
            if (this.setting.open) {
                ++this.progress;
            }
            if (future) {
                if (!this.setting.open) {
                    this.progress = 0;
                }
                GlStateManager.pushMatrix();
                GlStateManager.enableBlend();
                mc.getTextureManager().bindTexture(new ResourceLocation("textures/rebirth/gear.png"));
                GlStateManager.translate(this.getX() + (float)this.getWidth() - 6.7f + 8.0f, this.getY() + 7.7f - 0.3f, 0.0f);
                GlStateManager.rotate(Component.calculateRotation(this.progress), 0.0f, 0.0f, 1.0f);
                RenderUtil.drawModalRect(-5, -5, 0.0f, 0.0f, 10, 10, 10, 10, 10.0f, 10.0f);
                GlStateManager.disableBlend();
                GlStateManager.popMatrix();
            } else {
                String color = this.getState() || newStyle ? "" : String.valueOf(ChatFormatting.GRAY);
                String gear = this.setting.open ? "-" : "+";
                Managers.TEXT.drawStringWithShadow(color + gear, this.x - 1.5f + (float)this.width - 7.4f + 8.0f, this.y - 2.2f - (float)Gui.INSTANCE.getTextOffset(), -1);
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
    }

    @Override
    public int getHeight() {
        return ClickGui.INSTANCE.getButtonHeight() - 1;
    }

    @Override
    public void toggle() {
        this.setting.setValue(!((Boolean) this.setting.getValue()));
    }

    @Override
    public boolean getState() {
        return (Boolean)this.setting.getValue();
    }
}

