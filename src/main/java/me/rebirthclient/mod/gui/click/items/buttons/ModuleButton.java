package me.rebirthclient.mod.gui.click.items.buttons;

import com.mojang.realmsclient.gui.ChatFormatting;
import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import me.rebirthclient.api.managers.Managers;
import me.rebirthclient.api.util.render.ColorUtil;
import me.rebirthclient.api.util.render.RenderUtil;
import me.rebirthclient.mod.gui.click.Component;
import me.rebirthclient.mod.gui.click.items.Item;
import me.rebirthclient.mod.gui.click.items.buttons.BindButton;
import me.rebirthclient.mod.gui.click.items.buttons.BooleanButton;
import me.rebirthclient.mod.gui.click.items.buttons.Button;
import me.rebirthclient.mod.gui.click.items.buttons.EnumButton;
import me.rebirthclient.mod.gui.click.items.buttons.PickerButton;
import me.rebirthclient.mod.gui.click.items.buttons.Slider;
import me.rebirthclient.mod.gui.click.items.buttons.StringButton;
import me.rebirthclient.mod.gui.screen.Gui;
import me.rebirthclient.mod.modules.Module;
import me.rebirthclient.mod.modules.impl.client.ClickGui;
import me.rebirthclient.mod.modules.impl.client.FontMod;
import me.rebirthclient.mod.modules.settings.Bind;
import me.rebirthclient.mod.modules.settings.Setting;
import net.minecraft.client.audio.ISound;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.init.SoundEvents;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;

public class ModuleButton
extends Button {
    private final Module module;
    private List<Item> items = new ArrayList<>();
    private boolean subOpen;
    private int progress = 0;

    public ModuleButton(Module module) {
        super(module.getName());
        this.module = module;
        this.initSettings();
    }

    public void initSettings() {
        ArrayList<Item> newItems = new ArrayList<>();
        if (!this.module.getSettings().isEmpty()) {
            for (Setting setting : this.module.getSettings()) {
                if (setting.getValue() instanceof Boolean && !setting.getName().equals("Enabled")) {
                    newItems.add(new BooleanButton(setting));
                }
                if (setting.getValue() instanceof Bind && !setting.getName().equalsIgnoreCase("Keybind") && !this.module.getName().equalsIgnoreCase("Hud")) {
                    newItems.add(new BindButton(setting));
                }
                if ((setting.getValue() instanceof String || setting.getValue() instanceof Character) && !setting.getName().equalsIgnoreCase("displayName")) {
                    newItems.add(new StringButton(setting));
                }
                if (setting.getValue() instanceof Color) {
                    newItems.add(new PickerButton(setting));
                }
                if (setting.isNumberSetting() && setting.hasRestriction()) {
                    newItems.add(new Slider(setting));
                    continue;
                }
                if (!setting.isEnumSetting()) continue;
                newItems.add(new EnumButton(setting));
            }
        }
        newItems.add(new BindButton(this.module.getSettingByName("Keybind")));
        this.items = newItems;
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        super.drawScreen(mouseX, mouseY, partialTicks);
        if (!this.items.isEmpty()) {
            this.drawGear();
            if (this.subOpen) {
                ++this.progress;
                float height = 1.0f;
                for (Item item : this.items) {
                    Component.counter1[0] = Component.counter1[0] + 1;
                    if (!item.isHidden()) {
                        item.setLocation(this.x + 1.0f, this.y + (height += (float)ClickGui.INSTANCE.getButtonHeight()));
                        item.setHeight(ClickGui.INSTANCE.getButtonHeight());
                        item.setWidth(this.width - 9);
                        item.drawScreen(mouseX, mouseY, partialTicks);
                        if (item instanceof PickerButton && ((PickerButton)item).setting.open) {
                            height = ((PickerButton)item).setting.noRainbow ? (height += 110.0f) : (height += 120.0f);
                        }
                        if (item instanceof EnumButton && ((EnumButton)item).setting.open) {
                            height += (float)(((EnumButton)item).setting.getValue().getClass().getEnumConstants().length * 12);
                        }
                    }
                    item.update();
                }
            }
        }
        if (this.isHovering(mouseX, mouseY) && ClickGui.INSTANCE.isOn()) {
            String description = ChatFormatting.GRAY + this.module.getDescription();
            net.minecraft.client.gui.Gui.drawRect(0, ModuleButton.mc.currentScreen.height - 11, Managers.TEXT.getStringWidth(description) + 2, ModuleButton.mc.currentScreen.height, ColorUtil.injectAlpha(new Color(-1072689136), 200).getRGB());
            assert (ModuleButton.mc.currentScreen != null);
            Managers.TEXT.drawStringWithShadow(description, 2.0f, ModuleButton.mc.currentScreen.height - 10, -1);
        }
    }

    public void drawGear() {
        boolean future;
        boolean newStyle = ClickGui.INSTANCE.style.getValue() == ClickGui.Style.NEW;
        boolean bl = future = ClickGui.INSTANCE.style.getValue() == ClickGui.Style.FUTURE;
        if (ClickGui.INSTANCE.gear.getValue()) {
            if (future) {
                if (!this.subOpen) {
                    this.progress = 0;
                }
                GlStateManager.pushMatrix();
                GlStateManager.enableBlend();
                mc.getTextureManager().bindTexture(new ResourceLocation("textures/rebirth/gear.png"));
                GlStateManager.translate(this.getX() + (float)this.getWidth() - 6.7f, this.getY() + 7.7f - 0.3f, 0.0f);
                GlStateManager.rotate(Component.calculateRotation(this.progress), 0.0f, 0.0f, 1.0f);
                RenderUtil.drawModalRect(-5, -5, 0.0f, 0.0f, 10, 10, 10, 10, 10.0f, 10.0f);
                GlStateManager.disableBlend();
                GlStateManager.popMatrix();
            } else {
                String color = this.module.isOn() || newStyle ? "" : String.valueOf(ChatFormatting.GRAY);
                String gear = this.subOpen ? "-" : "+";
                float x = this.x - 1.5f + (float)this.width - 7.4f;
                Managers.TEXT.drawStringWithShadow(color + gear, x + (FontMod.INSTANCE.isOn() && gear.equals("-") ? 1.0f : 0.0f), this.y - 2.2f - (float)Gui.INSTANCE.getTextOffset(), -1);
            }
        }
    }

    @Override
    public void mouseClicked(int mouseX, int mouseY, int mouseButton) {
        super.mouseClicked(mouseX, mouseY, mouseButton);
        if (!this.items.isEmpty()) {
            if (mouseButton == 1 && this.isHovering(mouseX, mouseY)) {
                this.subOpen = !this.subOpen;
                mc.getSoundHandler().playSound(PositionedSoundRecord.getMasterRecord(SoundEvents.UI_BUTTON_CLICK, 1.0f));
            }
            if (this.subOpen) {
                for (Item item : this.items) {
                    if (item.isHidden()) continue;
                    item.mouseClicked(mouseX, mouseY, mouseButton);
                }
            }
        }
    }

    @Override
    public void onKeyTyped(char typedChar, int keyCode) {
        super.onKeyTyped(typedChar, keyCode);
        if (!this.items.isEmpty() && this.subOpen) {
            for (Item item : this.items) {
                if (item.isHidden()) continue;
                item.onKeyTyped(typedChar, keyCode);
            }
        }
    }

    @Override
    public int getHeight() {
        if (this.subOpen) {
            int height = ClickGui.INSTANCE.getButtonHeight() - 1;
            for (Item item : this.items) {
                if (item.isHidden()) continue;
                height += item.getHeight() + 1;
                if (item instanceof PickerButton && ((PickerButton)item).setting.open) {
                    height = ((PickerButton)item).setting.noRainbow ? (int)((float)height + 110.0f) : (int)((float)height + 120.0f);
                }
                if (!(item instanceof EnumButton) || !((EnumButton)item).setting.open) continue;
                height += ((EnumButton)item).setting.getValue().getClass().getEnumConstants().length * 12;
            }
            return height + 2;
        }
        return ClickGui.INSTANCE.getButtonHeight() - 1;
    }

    public Module getModule() {
        return this.module;
    }

    @Override
    public void toggle() {
        this.module.toggle();
    }

    @Override
    public boolean getState() {
        return this.module.isOn();
    }
}

