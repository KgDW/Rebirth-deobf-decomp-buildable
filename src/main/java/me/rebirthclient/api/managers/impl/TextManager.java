package me.rebirthclient.api.managers.impl;

import com.mojang.realmsclient.gui.ChatFormatting;
import java.awt.Font;
import me.rebirthclient.api.util.Timer;
import me.rebirthclient.api.util.render.ColorUtil;
import me.rebirthclient.mod.Mod;
import me.rebirthclient.mod.gui.font.CFont;
import me.rebirthclient.mod.gui.font.CustomFont;
import me.rebirthclient.mod.modules.impl.client.ClickGui;
import me.rebirthclient.mod.modules.impl.client.FontMod;
import me.rebirthclient.mod.modules.impl.client.NameProtect;
import net.minecraft.util.math.MathHelper;

public class TextManager
extends Mod {
    public final String syncCode = "\u00a7(\u00a7)";
    private final Timer idleTimer = new Timer();
    private final CustomFont iconFont = new CustomFont(new CFont.CustomFont("/assets/minecraft/textures/rebirth/fonts/IconFont.ttf", 19.0f, 0), true, false);
    public int scaledWidth;
    public int scaledHeight;
    public int scaleFactor;
    private CustomFont customFont = new CustomFont(new Font("Verdana", Font.PLAIN, 17), true, true);
    private boolean idling;

    public TextManager() {
        this.updateResolution();
    }

    public void init() {
        if (FontMod.INSTANCE == null) {
            FontMod.INSTANCE = new FontMod();
        }
        FontMod fonts = FontMod.INSTANCE;
        try {
            this.setFontRenderer(new Font(fonts.font.getValue(), fonts.getFont(), fonts.size.getValue()), fonts.antiAlias.getValue(), fonts.metrics.getValue());
        }
        catch (Exception exception) {
            // empty catch block
        }
    }

    public String getPrefix() {
        return "\u00a7(\u00a7)\u00a7f[\u00a7rRebirth\u00a7f] " + ChatFormatting.RESET;
    }

    public String capitalSpace(String str) {
        str = str.replace("A", " A");
        str = str.replace("B", " B");
        str = str.replace("C", " C");
        str = str.replace("D", " D");
        str = str.replace("E", " E");
        str = str.replace("F", " F");
        str = str.replace("G", " G");
        str = str.replace("H", " H");
        str = str.replace("I", " I");
        str = str.replace("J", " J");
        str = str.replace("K", " K");
        str = str.replace("L", " L");
        str = str.replace("M", " M");
        str = str.replace("N", " N");
        str = str.replace("O", " O");
        str = str.replace("P", " P");
        str = str.replace("Q", " Q");
        str = str.replace("R", " R");
        str = str.replace("S", " S");
        str = str.replace("T", " T");
        str = str.replace("U", " U");
        str = str.replace("V", " V");
        str = str.replace("W", " W");
        str = str.replace("X", " X");
        str = str.replace("Y", " Y");
        str = str.replace("Z", " Z");
        str = str.replace("T P", "TP");
        str = str.replace("T N T", "TNT");
        str = str.replace("D M G", "DMG");
        str = str.replace("H U D", "HUD");
        str = str.replace("E S P", "ESP");
        str = str.replace("F P S", "FPS");
        str = str.replace("M C F", "MCF");
        if ((str = str.replace("2 D", "2D")).startsWith(" ")) {
            str = str.replaceFirst(" ", "");
        }
        return str;
    }

    public String normalizeCases(Object o) {
        return Character.toUpperCase(o.toString().charAt(0)) + o.toString().toLowerCase().substring(1);
    }

    public void drawStringWithShadow(String text2, float x, float y, int color) {
        this.drawString(text2, x, y, color, true);
    }

    public float drawString(String text2, float x, float y, int color, boolean shadow) {
        NameProtect nameProtect = NameProtect.INSTANCE;
        String string = text2 = nameProtect.isOn() ? text2.replaceAll(mc.getSession().getUsername(), nameProtect.name.getValue()) : text2;
        if (FontMod.INSTANCE.isOn()) {
            if (shadow) {
                this.customFont.drawStringWithShadow(text2, x, y, color);
            } else {
                this.customFont.drawString(text2, x, y, color);
            }
            return x;
        }
        TextManager.mc.fontRenderer.drawString(text2, x, y, color, shadow);
        return x;
    }

    public void drawStringIcon(String text2, float x, float y, int color) {
        NameProtect nameProtect = NameProtect.INSTANCE;
        text2 = nameProtect.isOn() ? text2.replaceAll(mc.getSession().getUsername(), nameProtect.name.getValue()) : text2;
        this.iconFont.drawStringWithShadow(text2, x, y, color);
    }

    public void drawMCString(String text2, float x, float y, int color, boolean shadow) {
        NameProtect nameProtect = NameProtect.INSTANCE;
        text2 = nameProtect.isOn() ? text2.replaceAll(mc.getSession().getUsername(), nameProtect.name.getValue()) : text2;
        TextManager.mc.fontRenderer.drawString(text2, x, y, color, shadow);
    }

    public void drawRollingRainbowString(String text2, float x, float y, boolean shadow) {
        int[] arrayOfInt = new int[]{1};
        char[] stringToCharArray = text2.toCharArray();
        float f = 0.0f + x;
        for (char c : stringToCharArray) {
            this.drawString(String.valueOf(c), f, y, ColorUtil.rainbow(arrayOfInt[0] * ClickGui.INSTANCE.rainbowDelay.getValue()).getRGB(), shadow);
            f += (float)this.getStringWidth(String.valueOf(c));
            arrayOfInt[0] = arrayOfInt[0] + 1;
        }
    }

    public int getStringWidth(String text2) {
        NameProtect nameProtect = NameProtect.INSTANCE;
        String string = text2 = nameProtect.isOn() ? text2.replaceAll(mc.getSession().getUsername(), nameProtect.name.getValue()) : text2;
        if (FontMod.INSTANCE.isOn()) {
            return this.customFont.getStringWidth(text2);
        }
        return TextManager.mc.fontRenderer.getStringWidth(text2);
    }

    public int getMCStringWidth(String text2) {
        NameProtect nameProtect = NameProtect.INSTANCE;
        text2 = nameProtect.isOn() ? text2.replaceAll(mc.getSession().getUsername(), nameProtect.name.getValue()) : text2;
        return TextManager.mc.fontRenderer.getStringWidth(text2);
    }

    public int getFontHeight() {
        if (FontMod.INSTANCE.isOn()) {
            return this.customFont.getStringHeight("A");
        }
        return TextManager.mc.fontRenderer.FONT_HEIGHT;
    }

    public int getFontHeight2() {
        if (FontMod.INSTANCE.isOn()) {
            return this.customFont.getStringHeight("A") + 3;
        }
        return TextManager.mc.fontRenderer.FONT_HEIGHT;
    }

    public void setFontRenderer(Font font, boolean antiAlias, boolean fractionalMetrics) {
        this.customFont = new CustomFont(font, antiAlias, fractionalMetrics);
    }

    public Font getCurrentFont() {
        return this.customFont.getFont();
    }

    public void updateResolution() {
        this.scaledWidth = TextManager.mc.displayWidth;
        this.scaledHeight = TextManager.mc.displayHeight;
        this.scaleFactor = 1;
        boolean flag = mc.isUnicode();
        int i = TextManager.mc.gameSettings.guiScale;
        if (i == 0) {
            i = 1000;
        }
        while (this.scaleFactor < i && this.scaledWidth / (this.scaleFactor + 1) >= 320 && this.scaledHeight / (this.scaleFactor + 1) >= 240) {
            ++this.scaleFactor;
        }
        if (flag && this.scaleFactor % 2 != 0 && this.scaleFactor != 1) {
            --this.scaleFactor;
        }
        double scaledWidthD = (double)this.scaledWidth / (double)this.scaleFactor;
        double scaledHeightD = (double)this.scaledHeight / (double)this.scaleFactor;
        this.scaledWidth = MathHelper.ceil(scaledWidthD);
        this.scaledHeight = MathHelper.ceil(scaledHeightD);
    }

    public String getIdleSign() {
        if (this.idleTimer.passedMs(500L)) {
            this.idling = !this.idling;
            this.idleTimer.reset();
        }
        if (this.idling) {
            return "_";
        }
        return "";
    }
}

