package me.rebirthclient.mod.modules.impl.client;

import com.mojang.realmsclient.gui.ChatFormatting;
import java.awt.GraphicsEnvironment;
import me.rebirthclient.api.events.impl.ClientEvent;
import me.rebirthclient.api.managers.Managers;
import me.rebirthclient.mod.modules.Category;
import me.rebirthclient.mod.modules.Module;
import me.rebirthclient.mod.modules.settings.Setting;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class FontMod
extends Module {
    public static FontMod INSTANCE;
    public final Setting<String> font = this.add(new Setting<>("Font", "Arial"));
    public final Setting<Boolean> antiAlias = this.add(new Setting<>("AntiAlias", true));
    public final Setting<Boolean> metrics = this.add(new Setting<>("Metrics", true));
    public final Setting<Boolean> global = this.add(new Setting<>("Global", false));
    public final Setting<Integer> size = this.add(new Setting<>("Size", 17, 12, 30));
    public final Setting<Style> style = this.add(new Setting<>("Style", Style.PLAIN));
    private boolean reload;

    public FontMod() {
        super("Fonts", "Custom font for all of the clients text. Use the font command", Category.CLIENT);
        INSTANCE = this;
    }

    @Override
    public String getInfo() {
        return this.font.getValue();
    }

    @Override
    public void onTick() {
        if (this.reload) {
            Managers.TEXT.init();
            this.reload = false;
        }
    }

    @SubscribeEvent
    public void onSettingChange(ClientEvent event) {
        Setting setting;
        if (FontMod.fullNullCheck()) {
            return;
        }
        if (event.getStage() == 2 && (setting = event.getSetting()) != null && setting.getMod().equals(this)) {
            if (setting.getName().equals("Font") && !this.checkFont(setting.getPlannedValue().toString())) {
                this.sendMessage(ChatFormatting.RED + "That font doesn't exist.");
                event.setCanceled(true);
                return;
            }
            this.reload = true;
        }
    }

    private boolean checkFont(String font) {
        for (String s : GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames()) {
            if (!s.equals(font)) continue;
            return true;
        }
        return false;
    }

    public int getFont() {
        switch (this.style.getValue()) {
            case BOLD: {
                return 1;
            }
            case ITALIC: {
                return 2;
            }
            case BOLDITALIC: {
                return 3;
            }
        }
        return 0;
    }

    private static enum Style {
        PLAIN,
        BOLD,
        ITALIC,
        BOLDITALIC

    }
}

