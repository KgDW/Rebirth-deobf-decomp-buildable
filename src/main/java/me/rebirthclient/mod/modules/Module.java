package me.rebirthclient.mod.modules;

import com.mojang.realmsclient.gui.ChatFormatting;
import me.rebirthclient.api.events.impl.Render2DEvent;
import me.rebirthclient.api.events.impl.Render3DEvent;
import me.rebirthclient.api.managers.Managers;
import me.rebirthclient.mod.Mod;
import me.rebirthclient.mod.commands.Command;
import me.rebirthclient.mod.modules.Category;
import me.rebirthclient.mod.modules.impl.client.HUD;
import me.rebirthclient.mod.modules.impl.hud.Notifications;
import me.rebirthclient.mod.modules.settings.Bind;
import me.rebirthclient.mod.modules.settings.Setting;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.common.MinecraftForge;

public abstract class Module
extends Mod {
    public final Setting<Boolean> enabled = this.add(new Setting<>("Enabled", this.shouldEnable()));
    public final Setting<Boolean> drawn = this.add(new Setting<>("Drawn", this.shouldDrawn()));
    public final Setting<Bind> bind = this.add(new Setting<>("Keybind", this.getName().equalsIgnoreCase("ClickGui") ? new Bind(21) : new Bind(-1)));
    public final Setting<String> displayerName;
    private final String description;
    private final Category category;
    private final boolean shouldListen;

    public Module(String name, String description, Category category) {
        super(name);
        this.displayerName = this.add(new Setting<>("Display", name));
        this.description = description;
        this.category = category;
        this.shouldListen = true;
    }

    private boolean shouldDrawn() {
        return !this.getName().equalsIgnoreCase("CombatSetting") && !this.getName().equalsIgnoreCase("Title") && !this.getName().equalsIgnoreCase("HUD") && !this.getName().equalsIgnoreCase("Rotations") && !this.getName().equalsIgnoreCase("RenderSetting") && !this.getName().equalsIgnoreCase("Chat") && !this.getName().equalsIgnoreCase("GuiAnimation");
    }

    private boolean shouldEnable() {
        return this.getName().equalsIgnoreCase("HUD") || this.getName().equalsIgnoreCase("Title") || this.getName().equalsIgnoreCase("CombatSetting") || this.getName().equalsIgnoreCase("RenderSetting") || this.getName().equalsIgnoreCase("Rotations") || this.getName().equalsIgnoreCase("Chat") || this.getName().equalsIgnoreCase("GuiAnimation");
    }

    private String getPrefix() {
        return "\u00a7f[\u00a7r" + this.getDisplayName() + "\u00a7f] ";
    }

    public void enable() {
        this.enabled.setValue(true);
        this.onEnable();
        Notifications.add(this.getPrefix() + "toggled\u00a7a on.");
        Command.sendMessageWithID(this.getPrefix() + "toggled\u00a7a on.", 1);
        if (this.isOn() && this.shouldListen) {
            MinecraftForge.EVENT_BUS.register(this);
        }
    }

    public void disable() {
        if (this.shouldListen) {
            MinecraftForge.EVENT_BUS.unregister(this);
        }
        this.enabled.setValue(false);
        this.onDisable();
        Notifications.add(this.getPrefix() + "toggled\u00a7c off.");
        Command.sendMessageWithID(this.getPrefix() + "toggled\u00a7c off.", 1);
    }

    public void sendMessage(String message) {
        Notifications.notifyList.add(new Notifications.Notifys(message));
        Command.sendSilentMessage(Managers.TEXT.getPrefix() + this.getPrefix() + message);
    }

    public void sendMessageWithID(String message, int id) {
        if (!Module.nullCheck()) {
            Module.mc.ingameGUI.getChatGUI().printChatMessageWithOptionalDeletion(new Command.ChatMessage(Managers.TEXT.getPrefix() + this.getPrefix() + message), id);
        }
    }

    public String getDisplayName() {
        return this.displayerName.getValue();
    }

    public void toggle() {
        if (this.isOn()) {
            this.disable();
        } else {
            this.enable();
        }
    }

    public boolean isOn() {
        return this.enabled.getValue();
    }

    public boolean isOff() {
        return !this.enabled.getValue();
    }

    public boolean isDrawn() {
        return this.drawn.getValue();
    }

    public boolean isListening() {
        return this.shouldListen && this.isOn();
    }

    public void onEnable() {
    }

    public void onDisable() {
    }

    public void onLoad() {
    }

    public void onTick() {
    }

    public void onLogin() {
    }

    public void onLogout() {
    }

    public void onUpdate() {
    }

    public void onUnload() {
    }

    public void onRender2D(Render2DEvent event) {
    }

    public void onRender3D(Render3DEvent event) {
    }

    public void onTotemPop(EntityPlayer player) {
    }

    public void onDeath(EntityPlayer player) {
    }

    public String getInfo() {
        return null;
    }

    public String getArrayListInfo() {
        return (HUD.INSTANCE.space.getValue() ? Managers.TEXT.capitalSpace(this.getDisplayName()) : this.getDisplayName()) + ChatFormatting.GRAY + (this.getInfo() != null ? " [" + ChatFormatting.WHITE + this.getInfo() + ChatFormatting.GRAY + "]" : "");
    }

    public Category getCategory() {
        return this.category;
    }

    public Bind getBind() {
        return this.bind.getValue();
    }

    public void setBind(int key) {
        this.bind.setValue(new Bind(key));
    }

    public String getDescription() {
        return this.description;
    }
}

