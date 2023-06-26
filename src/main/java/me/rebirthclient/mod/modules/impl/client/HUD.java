package me.rebirthclient.mod.modules.impl.client;

import com.mojang.realmsclient.gui.ChatFormatting;
import java.awt.Color;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.ArrayList;

import me.rebirthclient.api.events.impl.Render2DEvent;
import me.rebirthclient.api.managers.Managers;
import me.rebirthclient.api.managers.impl.ModuleManager;
import me.rebirthclient.api.util.EntityUtil;
import me.rebirthclient.api.util.InventoryUtil;
import me.rebirthclient.api.util.Timer;
import me.rebirthclient.api.util.math.MathUtil;
import me.rebirthclient.api.util.render.ColorUtil;
import me.rebirthclient.api.util.render.RenderUtil;
import me.rebirthclient.mod.modules.Category;
import me.rebirthclient.mod.modules.Module;
import me.rebirthclient.mod.modules.impl.client.ClickGui;
import me.rebirthclient.mod.modules.impl.client.FontMod;
import me.rebirthclient.mod.modules.impl.combat.Aura;
import me.rebirthclient.mod.modules.impl.combat.AutoTrap;
import me.rebirthclient.mod.modules.settings.Setting;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiChat;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

public class HUD
extends Module {
    public static HUD INSTANCE = new HUD();
    private final Setting<Page> page = this.add(new Setting<>("Page", Page.GLOBAL));
    public final Setting<Boolean> potionIcons = this.add(new Setting<>("NoPotionIcons", false, v -> this.page.getValue() == Page.GLOBAL));
    public final Setting<Boolean> combatCount = this.add(new Setting<>("ItemsCount", true, v -> this.page.getValue() == Page.ELEMENTS).setParent());
    public final Setting<Integer> combatCountX = this.add(new Setting<>("X", 125, 0, 300, v -> this.combatCount.isOpen() && this.page.getValue() == Page.ELEMENTS));
    public final Setting<Integer> combatCountY = this.add(new Setting<>("Y", 18, 0, 500, v -> this.combatCount.isOpen() && this.page.getValue() == Page.ELEMENTS));
    public final Setting<ModuleManager.Ordering> ordering = this.add(new Setting<>("Ordering", ModuleManager.Ordering.LENGTH, v -> this.page.getValue() == Page.GLOBAL));
    public final Setting<Integer> lagTime = this.add(new Setting<>("LagTime", 1000, 0, 2000, v -> this.page.getValue() == Page.GLOBAL));
    public final Setting<Boolean> lowerCase = this.add(new Setting<>("LowerCase", false, v -> this.page.getValue() == Page.GLOBAL));
    private final Setting<Boolean> grayColors = this.add(new Setting<>("Gray", true, v -> this.page.getValue() == Page.GLOBAL));
    private final Setting<Boolean> renderingUp = this.add(new Setting<>("RenderingUp", true, v -> this.page.getValue() == Page.GLOBAL));
    private final Setting<Boolean> skeetBar = this.add(new Setting<>("SkeetMode", false, v -> this.page.getValue() == Page.ELEMENTS).setParent());
    private final Setting<Boolean> jamie = this.add(new Setting<>("JamieColor", false, v -> this.page.getValue() == Page.ELEMENTS && this.skeetBar.isOpen()));
    private final Setting<Boolean> watermark = this.add(new Setting<>("Watermark", true, v -> this.page.getValue() == Page.ELEMENTS).setParent());
    public final Setting<String> watermarkString = this.add(new Setting<>("Text", "Rebirth", v -> this.page.getValue() == Page.ELEMENTS && this.watermark.isOpen()));
    private final Setting<Boolean> watermarkShort = this.add(new Setting<>("Shorten", false, v -> this.watermark.isOpen() && this.page.getValue() == Page.ELEMENTS));
    private final Setting<Boolean> watermarkVerColor = this.add(new Setting<>("VerColor", true, v -> this.watermark.isOpen() && this.page.getValue() == Page.ELEMENTS));
    private final Setting<Integer> waterMarkY = this.add(new Setting<>("Height", 2, 2, 12, v -> this.page.getValue() == Page.ELEMENTS && this.watermark.isOpen()));
    private final Setting<Boolean> idWatermark = this.add(new Setting<>("IdWatermark", true, v -> this.page.getValue() == Page.ELEMENTS));
    private final Setting<Boolean> pvp = this.add(new Setting<>("PvpInfo", true, v -> this.page.getValue() == Page.ELEMENTS));
    private final Setting<Boolean> textRadar = this.add(new Setting<>("TextRadar", false, v -> this.page.getValue() == Page.ELEMENTS));
    private final Setting<Boolean> coords = this.add(new Setting<>("Position(XYZ)", false, v -> this.page.getValue() == Page.ELEMENTS));
    private final Setting<Boolean> direction = this.add(new Setting<>("Direction", false, v -> this.page.getValue() == Page.ELEMENTS));
    private final Setting<Boolean> armor = this.add(new Setting<>("Armor", false, v -> this.page.getValue() == Page.ELEMENTS));
    private final Setting<Boolean> lag = this.add(new Setting<>("LagNotifier", false, v -> this.page.getValue() == Page.ELEMENTS));
    private final Setting<Boolean> greeter = this.add(new Setting<>("Welcomer", false, v -> this.page.getValue() == Page.ELEMENTS).setParent());
    private final Setting<GreeterMode> greeterMode = this.add(new Setting<>("Mode", GreeterMode.PLAYER, v -> this.page.getValue() == Page.ELEMENTS && this.greeter.isOpen()));
    private final Setting<Boolean> greeterNameColor = this.add(new Setting<>("NameColor", true, v -> this.greeter.isOpen() && this.greeterMode.getValue() == GreeterMode.PLAYER && this.page.getValue() == Page.ELEMENTS));
    private final Setting<String> greeterText = this.add(new Setting<>("WelcomerText", "i sniff coke and smoke dope i got 2 habbits", v -> this.greeter.isOpen() && this.greeterMode.getValue() == GreeterMode.CUSTOM && this.page.getValue() == Page.ELEMENTS));
    private final Setting<Boolean> arrayList = this.add(new Setting<>("ArrayList", true, v -> this.page.getValue() == Page.ELEMENTS).setParent());
    private final Setting<Integer> arrayListOffset = this.add(new Setting<>("Offset", 50, 0, 200, v -> this.page.getValue() == Page.ELEMENTS && this.arrayList.isOpen()));
    public final Setting<Boolean> space = this.add(new Setting<>("Space", true, v -> this.page.getValue() == Page.ELEMENTS && this.arrayList.isOpen()));
    public final Setting<Boolean> onlyBind = this.add(new Setting<>("OnlyBind", false, v -> this.page.getValue() == Page.ELEMENTS && this.arrayList.isOpen()));
    private final Setting<Boolean> jamieArray = this.add(new Setting<>("JamieArray", false, v -> this.page.getValue() == Page.ELEMENTS && this.arrayList.isOpen()));
    private final Setting<Boolean> forgeHax = this.add(new Setting<>("ForgeHax", false, v -> this.page.getValue() == Page.ELEMENTS && this.arrayList.isOpen()));
    private final Setting<Boolean> arrayListLine = this.add(new Setting<>("Outline", false, v -> this.page.getValue() == Page.ELEMENTS && this.arrayList.isOpen()));
    private final Setting<Boolean> arrayListRect = this.add(new Setting<>("Rect", false, v -> this.page.getValue() == Page.ELEMENTS && this.arrayList.isOpen()));
    private final Setting<Boolean> arrayListRectColor = this.add(new Setting<>("ColorRect", false, v -> this.page.getValue() == Page.ELEMENTS && this.arrayList.isOpen() && this.arrayListRect.getValue()));
    private final Setting<Boolean> arrayListGlow = this.add(new Setting<>("Glow", true, v -> this.page.getValue() == Page.ELEMENTS && this.arrayList.isOpen()));
    private final Setting<Boolean> hideInChat = this.add(new Setting<>("HideInChat", true, v -> this.page.getValue() == Page.ELEMENTS && this.arrayList.isOpen()));
    private final Setting<Boolean> potions = this.add(new Setting<>("Potions", false, v -> this.page.getValue() == Page.ELEMENTS).setParent());
    private final Setting<Boolean> potionColor = this.add(new Setting<>("PotionColor", false, v -> this.page.getValue() == Page.ELEMENTS && this.potions.isOpen()));
    private final Setting<Boolean> ping = this.add(new Setting<>("Ping", false, v -> this.page.getValue() == Page.ELEMENTS));
    private final Setting<Boolean> speed = this.add(new Setting<>("Speed", false, v -> this.page.getValue() == Page.ELEMENTS));
    private final Setting<Boolean> tps = this.add(new Setting<>("TPS", false, v -> this.page.getValue() == Page.ELEMENTS));
    private final Setting<Boolean> fps = this.add(new Setting<>("FPS", false, v -> this.page.getValue() == Page.ELEMENTS));
    private final Setting<Boolean> time = this.add(new Setting<>("Time", false, v -> this.page.getValue() == Page.ELEMENTS));
    private final Timer timer = new Timer();
    private Map<String, Integer> players = new HashMap<>();
    private int color;

    public HUD() {
        super("HUD", "HUD elements drawn on your screen", Category.CLIENT);
        INSTANCE = this;
    }

    @Override
    public void onUpdate() {
        if (this.timer.passedMs(500L)) {
            this.players = this.getTextRadarMap();
            this.timer.reset();
        }
    }

    @Override
    public void onRender2D(final Render2DEvent event) {
        if (fullNullCheck()) {
            return;
        }
        final int width = Managers.TEXT.scaledWidth;
        final int height = Managers.TEXT.scaledHeight;
        this.color = ColorUtil.toRGBA(ClickGui.INSTANCE.color.getValue().getRed(), ClickGui.INSTANCE.color.getValue().getGreen(), ClickGui.INSTANCE.color.getValue().getBlue());
        if (this.watermark.getValue()) {
            final String nameString = this.watermarkString.getValue() + " ";
            final String verColor = this.watermarkVerColor.getValue() ? String.valueOf(ChatFormatting.WHITE) : "";
            final String verString = verColor + (this.watermarkShort.getValue() ? "" : "alpha");
            if (ClickGui.INSTANCE.rainbow.getValue()) {
                if (ClickGui.INSTANCE.hudRainbow.getValue() == ClickGui.HudRainbow.STATIC) {
                    Managers.TEXT.drawString((this.lowerCase.getValue() ? nameString.toLowerCase() : nameString) + verString, 2.0f, this.waterMarkY.getValue(), Managers.COLORS.getRainbow().getRGB(), true);
                }
                else if (this.watermarkVerColor.getValue()) {
                    this.drawDoubleRainbowRollingString(this.lowerCase.getValue() ? nameString.toLowerCase() : nameString, verString, 2.0f, this.waterMarkY.getValue());
                }
                else {
                    Managers.TEXT.drawRollingRainbowString((this.lowerCase.getValue() ? nameString.toLowerCase() : nameString) + verString, 2.0f, this.waterMarkY.getValue(), true);
                }
            }
            else {
                Managers.TEXT.drawString((this.lowerCase.getValue() ? nameString.toLowerCase() : nameString) + verString, 2.0f, this.waterMarkY.getValue(), this.color, true);
            }
        }
        if (this.combatCount.getValue()) {
            this.drawCombatCount();
        }
        final Color color = new Color(ClickGui.INSTANCE.color.getValue().getRed(), ClickGui.INSTANCE.color.getValue().getGreen(), ClickGui.INSTANCE.color.getValue().getBlue());
        if (this.skeetBar.getValue()) {
            if (this.jamie.getValue()) {
                RenderUtil.drawHGradientRect(0.0f, 0.0f, width / 5.0f, 1.0f, ColorUtil.toRGBA(0, 180, 255), ColorUtil.toRGBA(255, 180, 255));
                RenderUtil.drawHGradientRect(width / 5.0f, 0.0f, width / 5.0f * 2.0f, 1.0f, ColorUtil.toRGBA(255, 180, 255), ColorUtil.toRGBA(255, 255, 255));
                RenderUtil.drawHGradientRect(width / 5.0f * 2.0f, 0.0f, width / 5.0f * 3.0f, 1.0f, ColorUtil.toRGBA(255, 255, 255), ColorUtil.toRGBA(255, 255, 255));
                RenderUtil.drawHGradientRect(width / 5.0f * 3.0f, 0.0f, width / 5.0f * 4.0f, 1.0f, ColorUtil.toRGBA(255, 255, 255), ColorUtil.toRGBA(255, 180, 255));
                RenderUtil.drawHGradientRect(width / 5.0f * 4.0f, 0.0f, (float)width, 1.0f, ColorUtil.toRGBA(255, 180, 255), ColorUtil.toRGBA(0, 180, 255));
            }
            if (ClickGui.INSTANCE.rainbow.getValue() && ClickGui.INSTANCE.hudRainbow.getValue() == ClickGui.HudRainbow.ROLLING && !this.jamie.getValue()) {
                final int[] arrayOfInt = { 1 };
                RenderUtil.drawHGradientRect(0.0f, 0.0f, width / 2.0f, 1.0f, ColorUtil.rainbow(arrayOfInt[0] * ClickGui.INSTANCE.rainbowDelay.getValue()).getRGB(), ColorUtil.rainbow(20 * ClickGui.INSTANCE.rainbowDelay.getValue()).getRGB());
                RenderUtil.drawHGradientRect(width / 2.0f, 0.0f, (float)width, 1.0f, ColorUtil.rainbow(20 * ClickGui.INSTANCE.rainbowDelay.getValue()).getRGB(), ColorUtil.rainbow(40 * ClickGui.INSTANCE.rainbowDelay.getValue()).getRGB());
                ++arrayOfInt[0];
            }
            if (!ClickGui.INSTANCE.rainbow.getValue() && !this.jamie.getValue()) {
                RenderUtil.drawHGradientRect(0.0f, 0.0f, width / 2.0f, 1.0f, ColorUtil.pulseColor(color, 50, 1000).getRGB(), ColorUtil.pulseColor(color, 200, 1).getRGB());
                RenderUtil.drawHGradientRect(width / 2.0f, 0.0f, (float)width, 1.0f, ColorUtil.pulseColor(color, 200, 1).getRGB(), ColorUtil.pulseColor(color, 50, 1000).getRGB());
            }
        }
        if (this.textRadar.getValue()) {
            this.drawTextRadar(this.watermark.getValue() ? (this.waterMarkY.getValue() + 2) : 2);
        }
        if (this.pvp.getValue()) {
            this.drawPvPInfo();
        }
        this.color = ColorUtil.toRGBA(ClickGui.INSTANCE.color.getValue().getRed(), ClickGui.INSTANCE.color.getValue().getGreen(), ClickGui.INSTANCE.color.getValue().getBlue());
        if (this.idWatermark.getValue()) {
            final String nameString2 = "Rebirth ";
            final String domainString = "alpha";
            final float offset = Managers.TEXT.scaledHeight / 2.0f - 30.0f;
            if (ClickGui.INSTANCE.rainbow.getValue()) {
                if (ClickGui.INSTANCE.hudRainbow.getValue() == ClickGui.HudRainbow.STATIC) {
                    Managers.TEXT.drawString(nameString2 + domainString, 2.0f, offset, Managers.COLORS.getRainbow().getRGB(), true);
                }
                else {
                    Managers.TEXT.drawString(nameString2, 2.0f, offset, -1, true);
                    Managers.TEXT.drawRollingRainbowString(domainString, (float)(Managers.TEXT.getStringWidth(nameString2) + 2), offset, true);
                }
            }
            else {
                Managers.TEXT.drawString(nameString2 + ChatFormatting.LIGHT_PURPLE + domainString, 2.0f, offset, this.color, true);
            }
        }
        int counter = 20;
        final boolean inChat = HUD.mc.currentScreen instanceof GuiChat;

        final long enabledMods = Managers.MODULES.modules.stream().filter(module -> module.isOn() && module.isDrawn()).count();
        int j = (inChat && !this.renderingUp.getValue() && this.arrayListOffset.getValue() < 14) ? 14 : this.arrayListOffset.getValue();
        final int rectColor = this.jamieArray.getValue() ? ColorUtil.injectAlpha(this.getJamieColor(counter + 1), 60) : (this.arrayListRectColor.getValue() ? (ClickGui.INSTANCE.rainbow.getValue() ? ((ClickGui.INSTANCE.hudRainbow.getValue() == ClickGui.HudRainbow.ROLLING) ? ColorUtil.toRGBA(ColorUtil.rainbow(counter * ClickGui.INSTANCE.rainbowDelay.getValue()).getRed(), ColorUtil.rainbow(counter * ClickGui.INSTANCE.rainbowDelay.getValue()).getGreen(), ColorUtil.rainbow(counter * ClickGui.INSTANCE.rainbowDelay.getValue()).getBlue(), 60) : ColorUtil.toRGBA(Managers.COLORS.getRainbow().getRed(), Managers.COLORS.getRainbow().getGreen(), Managers.COLORS.getRainbow().getBlue(), 60)) : ColorUtil.toRGBA(ColorUtil.pulseColor(color, counter, 14).getRed(), ColorUtil.pulseColor(color, counter, 14).getGreen(), ColorUtil.pulseColor(color, counter, 14).getBlue(), 60)) : ColorUtil.toRGBA(10, 10, 10, 60));
        final int glowColor = this.jamieArray.getValue() ? ColorUtil.injectAlpha(this.getJamieColor(counter + 1), 60) : (ClickGui.INSTANCE.rainbow.getValue() ? ((ClickGui.INSTANCE.hudRainbow.getValue() == ClickGui.HudRainbow.ROLLING) ? ColorUtil.toRGBA(ColorUtil.rainbow(counter * ClickGui.INSTANCE.rainbowDelay.getValue()).getRed(), ColorUtil.rainbow(counter * ClickGui.INSTANCE.rainbowDelay.getValue()).getGreen(), ColorUtil.rainbow(counter * ClickGui.INSTANCE.rainbowDelay.getValue()).getBlue(), 60) : ColorUtil.toRGBA(Managers.COLORS.getRainbow().getRed(), Managers.COLORS.getRainbow().getGreen(), Managers.COLORS.getRainbow().getBlue(), 60)) : ColorUtil.toRGBA(ColorUtil.pulseColor(color, counter, 14).getRed(), ColorUtil.pulseColor(color, counter, 14).getGreen(), ColorUtil.pulseColor(color, counter, 14).getBlue(), 60));
        if (this.arrayList.getValue()) {
            if (this.renderingUp.getValue()) {
                if (inChat && this.hideInChat.getValue()) {
                    Managers.TEXT.drawString(enabledMods + " mods enabled", (float)(width - 2 - Managers.TEXT.getStringWidth(enabledMods + " mods enabled")), (float)(2 + j), ClickGui.INSTANCE.rainbow.getValue() ? ((ClickGui.INSTANCE.hudRainbow.getValue() == ClickGui.HudRainbow.ROLLING) ? ColorUtil.rainbow(counter * ClickGui.INSTANCE.rainbowDelay.getValue()).getRGB() : Managers.COLORS.getRainbow().getRGB()) : this.color, true);
                }
                else if (this.ordering.getValue() == ModuleManager.Ordering.ABC) {
                    for (int k = 0; k < Managers.MODULES.sortedAbc.size(); ++k) {
                        String str = Managers.MODULES.sortedAbc.get(k);
                        if (this.forgeHax.getValue()) {
                            str = Managers.MODULES.sortedAbc.get(k) + ChatFormatting.RESET + "<";
                        }
                        if (this.arrayListRect.getValue()) {
                            Gui.drawRect(width - 2 - (this.lowerCase.getValue() ? Managers.TEXT.getStringWidth(str.toLowerCase()) : Managers.TEXT.getStringWidth(str)) - 1, (j == 0) ? 0 : (2 + j), width, 2 + j + 10, rectColor);
                        }
                        if (this.arrayListGlow.getValue()) {
                            RenderUtil.drawGlow(width - 2 - (this.lowerCase.getValue() ? Managers.TEXT.getStringWidth(str.toLowerCase()) : Managers.TEXT.getStringWidth(str)) - 1, (j == 0) ? 0.0 : ((double)(2 + j - 4)), width, 2 + j + 15, glowColor);
                        }
                        if (this.arrayListLine.getValue()) {
                            Gui.drawRect(width - 2 - (this.lowerCase.getValue() ? Managers.TEXT.getStringWidth(str.toLowerCase()) : Managers.TEXT.getStringWidth(str)) - 2, (j == 0) ? 0 : (2 + j - 1), width - 2 - (this.lowerCase.getValue() ? Managers.TEXT.getStringWidth(str.toLowerCase()) : Managers.TEXT.getStringWidth(str)) - 1, 2 + j + 10, this.jamieArray.getValue() ? this.getJamieColor(counter - 2) : (ClickGui.INSTANCE.rainbow.getValue() ? ((ClickGui.INSTANCE.hudRainbow.getValue() == ClickGui.HudRainbow.ROLLING) ? ColorUtil.rainbow(counter * ClickGui.INSTANCE.rainbowDelay.getValue()).getRGB() : Managers.COLORS.getRainbow().getRGB()) : ColorUtil.pulseColor(color, counter, 14).getRGB()));
                            int a = k + 1;
                            if (a >= Managers.MODULES.sortedAbc.size()) {
                                a = k;
                            }
                            String nextStr = Managers.MODULES.sortedAbc.get(a);
                            if (this.forgeHax.getValue()) {
                                nextStr = Managers.MODULES.sortedAbc.get(a) + ChatFormatting.RESET + "<";
                            }
                            Gui.drawRect(width - 2 - (this.lowerCase.getValue() ? Managers.TEXT.getStringWidth(str.toLowerCase()) : Managers.TEXT.getStringWidth(str)) - 2, 2 + (j + 1) + 8, (a == k) ? width : (width - 2 - (this.lowerCase.getValue() ? Managers.TEXT.getStringWidth(str.toLowerCase()) : Managers.TEXT.getStringWidth(str)) + ((this.lowerCase.getValue() ? Managers.TEXT.getStringWidth(str.toLowerCase()) : Managers.TEXT.getStringWidth(str)) - (this.lowerCase.getValue() ? Managers.TEXT.getStringWidth(nextStr.toLowerCase()) : Managers.TEXT.getStringWidth(nextStr))) - 1), 2 + (j + 1) + 9, this.jamieArray.getValue() ? this.getJamieColor(counter - 2) : (ClickGui.INSTANCE.rainbow.getValue() ? ((ClickGui.INSTANCE.hudRainbow.getValue() == ClickGui.HudRainbow.ROLLING) ? ColorUtil.rainbow(counter * ClickGui.INSTANCE.rainbowDelay.getValue()).getRGB() : Managers.COLORS.getRainbow().getRGB()) : ColorUtil.pulseColor(color, counter, 14).getRGB()));
                        }
                        Managers.TEXT.drawString(this.lowerCase.getValue() ? str.toLowerCase() : str, (float)(width - 2 - (this.lowerCase.getValue() ? Managers.TEXT.getStringWidth(str.toLowerCase()) : Managers.TEXT.getStringWidth(str))), (float)(3 + j), this.jamieArray.getValue() ? this.getJamieColor(counter - 2) : (ClickGui.INSTANCE.rainbow.getValue() ? ((ClickGui.INSTANCE.hudRainbow.getValue() == ClickGui.HudRainbow.ROLLING) ? ColorUtil.rainbow(counter * ClickGui.INSTANCE.rainbowDelay.getValue()).getRGB() : Managers.COLORS.getRainbow().getRGB()) : ColorUtil.pulseColor(color, counter, 14).getRGB()), true);
                        j += 10;
                        ++counter;
                    }
                }
                else {
                    for (int k = 0; k < Managers.MODULES.sortedLength.size(); ++k) {
                        Module module;
                        module = Managers.MODULES.sortedLength.get(k);
                        String str2 = module.getArrayListInfo();
                        if (this.forgeHax.getValue()) {
                            str2 = str2 + ChatFormatting.RESET + "<";
                        }
                        if (this.arrayListRect.getValue()) {
                            Gui.drawRect(width - 2 - (this.lowerCase.getValue() ? Managers.TEXT.getStringWidth(str2.toLowerCase()) : Managers.TEXT.getStringWidth(str2)) - 1, (j == 0) ? 0 : (2 + j), width, 2 + j + 10, rectColor);
                        }
                        if (this.arrayListGlow.getValue()) {
                            RenderUtil.drawGlow(width - 2 - (this.lowerCase.getValue() ? Managers.TEXT.getStringWidth(str2.toLowerCase()) : Managers.TEXT.getStringWidth(str2)) - 1, (j == 0) ? 0.0 : ((double)(2 + j - 4)), width, 2 + j + 15, glowColor);
                        }
                        if (this.arrayListLine.getValue()) {
                            Gui.drawRect(width - 2 - (this.lowerCase.getValue() ? Managers.TEXT.getStringWidth(str2.toLowerCase()) : Managers.TEXT.getStringWidth(str2)) - 2, (j == 0) ? 0 : (2 + j), width - 2 - (this.lowerCase.getValue() ? Managers.TEXT.getStringWidth(str2.toLowerCase()) : Managers.TEXT.getStringWidth(str2)) - 1, 2 + j + 11, this.jamieArray.getValue() ? this.getJamieColor(counter - 2) : (ClickGui.INSTANCE.rainbow.getValue() ? ((ClickGui.INSTANCE.hudRainbow.getValue() == ClickGui.HudRainbow.ROLLING) ? ColorUtil.rainbow(counter * ClickGui.INSTANCE.rainbowDelay.getValue()).getRGB() : Managers.COLORS.getRainbow().getRGB()) : ColorUtil.pulseColor(color, counter, 14).getRGB()));
                            int a2 = k + 1;
                            if (a2 >= Managers.MODULES.sortedLength.size()) {
                                a2 = k;
                            }
                            final Module nextModule = Managers.MODULES.sortedLength.get(a2);
                            String nextStr2 = nextModule.getArrayListInfo();
                            if (this.forgeHax.getValue()) {
                                nextStr2 = nextModule.getArrayListInfo() + ChatFormatting.RESET + "<";
                            }
                            Gui.drawRect(width - 2 - (this.lowerCase.getValue() ? Managers.TEXT.getStringWidth(str2.toLowerCase()) : Managers.TEXT.getStringWidth(str2)) - 2, 2 + (j + 1) + 9, (a2 == k) ? width : (width - 2 - (this.lowerCase.getValue() ? Managers.TEXT.getStringWidth(str2.toLowerCase()) : Managers.TEXT.getStringWidth(str2)) + ((this.lowerCase.getValue() ? Managers.TEXT.getStringWidth(str2.toLowerCase()) : Managers.TEXT.getStringWidth(str2)) - (this.lowerCase.getValue() ? Managers.TEXT.getStringWidth(nextStr2.toLowerCase()) : Managers.TEXT.getStringWidth(nextStr2))) - 1), 2 + (j + 1) + 10, this.jamieArray.getValue() ? this.getJamieColor(counter - 2) : (ClickGui.INSTANCE.rainbow.getValue() ? ((ClickGui.INSTANCE.hudRainbow.getValue() == ClickGui.HudRainbow.ROLLING) ? ColorUtil.rainbow(counter * ClickGui.INSTANCE.rainbowDelay.getValue()).getRGB() : Managers.COLORS.getRainbow().getRGB()) : ColorUtil.pulseColor(color, counter, 14).getRGB()));
                        }
                        Managers.TEXT.drawString(this.lowerCase.getValue() ? str2.toLowerCase() : str2, (float)(width - 2 - (this.lowerCase.getValue() ? Managers.TEXT.getStringWidth(str2.toLowerCase()) : Managers.TEXT.getStringWidth(str2))), (float)(3 + j), this.jamieArray.getValue() ? this.getJamieColor(counter - 2) : (ClickGui.INSTANCE.rainbow.getValue() ? ((ClickGui.INSTANCE.hudRainbow.getValue() == ClickGui.HudRainbow.ROLLING) ? ColorUtil.rainbow(counter * ClickGui.INSTANCE.rainbowDelay.getValue()).getRGB() : Managers.COLORS.getRainbow().getRGB()) : ColorUtil.pulseColor(color, counter, 14).getRGB()), true);
                        j += 10;
                        ++counter;
                    }
                }
            }
            else if (inChat && this.hideInChat.getValue()) {
                Managers.TEXT.drawString(enabledMods + " mods enabled", (float)(width - 2 - Managers.TEXT.getStringWidth(enabledMods + " mods enabled")), (float)(height - j - 11), ClickGui.INSTANCE.rainbow.getValue() ? ((ClickGui.INSTANCE.hudRainbow.getValue() == ClickGui.HudRainbow.ROLLING) ? ColorUtil.rainbow(counter * ClickGui.INSTANCE.rainbowDelay.getValue()).getRGB() : Managers.COLORS.getRainbow().getRGB()) : this.color, true);
            }
            else if (this.ordering.getValue() == ModuleManager.Ordering.ABC) {
                for (int k = 0; k < Managers.MODULES.sortedAbc.size(); ++k) {
                    String str = Managers.MODULES.sortedAbc.get(k);
                    if (this.forgeHax.getValue()) {
                        str = Managers.MODULES.sortedAbc.get(k) + ChatFormatting.RESET + "<";
                    }
                    j += 10;
                    if (this.arrayListRect.getValue()) {
                        Gui.drawRect(width - 2 - (this.lowerCase.getValue() ? Managers.TEXT.getStringWidth(str.toLowerCase()) : Managers.TEXT.getStringWidth(str)) - 1, height - j, width, (j == 1) ? height : (height - j + 10), rectColor);
                    }
                    if (this.arrayListGlow.getValue()) {
                        RenderUtil.drawGlow(width - 2 - (this.lowerCase.getValue() ? Managers.TEXT.getStringWidth(str.toLowerCase()) : Managers.TEXT.getStringWidth(str)) - 1, height - j - 4, width, (j == 1) ? ((double)height) : ((double)(height - j + 15)), glowColor);
                    }
                    if (this.arrayListLine.getValue()) {
                        Gui.drawRect(width - 2 - (this.lowerCase.getValue() ? Managers.TEXT.getStringWidth(str.toLowerCase()) : Managers.TEXT.getStringWidth(str)) - 2, height - j, width - 2 - (this.lowerCase.getValue() ? Managers.TEXT.getStringWidth(str.toLowerCase()) : Managers.TEXT.getStringWidth(str)) - 1, (j == 1) ? height : (height - j + 10), this.jamieArray.getValue() ? this.getJamieColor(counter - 2) : (ClickGui.INSTANCE.rainbow.getValue() ? ((ClickGui.INSTANCE.hudRainbow.getValue() == ClickGui.HudRainbow.ROLLING) ? ColorUtil.rainbow(counter * ClickGui.INSTANCE.rainbowDelay.getValue()).getRGB() : Managers.COLORS.getRainbow().getRGB()) : ColorUtil.pulseColor(color, counter, 14).getRGB()));
                        int a = k + 1;
                        if (a >= Managers.MODULES.sortedAbc.size()) {
                            a = k;
                        }
                        String nextStr = Managers.MODULES.sortedAbc.get(a);
                        if (this.forgeHax.getValue()) {
                            nextStr = Managers.MODULES.sortedAbc.get(a) + ChatFormatting.RESET + "<";
                        }
                        Gui.drawRect(width - 2 - (this.lowerCase.getValue() ? Managers.TEXT.getStringWidth(str.toLowerCase()) : Managers.TEXT.getStringWidth(str)) - 2, height - j - 1, (a == k) ? width : (width - 2 - (this.lowerCase.getValue() ? Managers.TEXT.getStringWidth(str.toLowerCase()) : Managers.TEXT.getStringWidth(str)) + ((this.lowerCase.getValue() ? Managers.TEXT.getStringWidth(str.toLowerCase()) : Managers.TEXT.getStringWidth(str)) - (this.lowerCase.getValue() ? Managers.TEXT.getStringWidth(nextStr.toLowerCase()) : Managers.TEXT.getStringWidth(nextStr))) - 1), (j == 1) ? height : (height - j), this.jamieArray.getValue() ? this.getJamieColor(counter - 2) : (ClickGui.INSTANCE.rainbow.getValue() ? ((ClickGui.INSTANCE.hudRainbow.getValue() == ClickGui.HudRainbow.ROLLING) ? ColorUtil.rainbow(counter * ClickGui.INSTANCE.rainbowDelay.getValue()).getRGB() : Managers.COLORS.getRainbow().getRGB()) : ColorUtil.pulseColor(color, counter, 14).getRGB()));
                    }
                    Managers.TEXT.drawString(this.lowerCase.getValue() ? str.toLowerCase() : str, (float)(width - 2 - (this.lowerCase.getValue() ? Managers.TEXT.getStringWidth(str.toLowerCase()) : Managers.TEXT.getStringWidth(str))), (float)(height - j + 1), this.jamieArray.getValue() ? this.getJamieColor(counter - 2) : (ClickGui.INSTANCE.rainbow.getValue() ? ((ClickGui.INSTANCE.hudRainbow.getValue() == ClickGui.HudRainbow.ROLLING) ? ColorUtil.rainbow(counter * ClickGui.INSTANCE.rainbowDelay.getValue()).getRGB() : Managers.COLORS.getRainbow().getRGB()) : ColorUtil.pulseColor(color, counter, 14).getRGB()), true);
                    ++counter;
                }
            }
            else {
                for (int k = 0; k < Managers.MODULES.sortedLength.size(); ++k) {
                    Module module;
                    module = Managers.MODULES.sortedLength.get(k);
                    String str2 = module.getArrayListInfo();
                    if (this.forgeHax.getValue()) {
                        str2 = str2 + ChatFormatting.RESET + "<";
                    }
                    j += 10;
                    if (this.arrayListRect.getValue()) {
                        Gui.drawRect(width - 2 - (this.lowerCase.getValue() ? Managers.TEXT.getStringWidth(str2.toLowerCase()) : Managers.TEXT.getStringWidth(str2)) - 1, height - j, width, (j == 1) ? height : (height - j + 10), rectColor);
                    }
                    if (this.arrayListGlow.getValue()) {
                        RenderUtil.drawGlow(width - 2 - (this.lowerCase.getValue() ? Managers.TEXT.getStringWidth(str2.toLowerCase()) : Managers.TEXT.getStringWidth(str2)) - 1, height - j - 4, width, (j == 1) ? ((double)height) : ((double)(height - j + 15)), glowColor);
                    }
                    if (this.arrayListLine.getValue()) {
                        Gui.drawRect(width - 2 - (this.lowerCase.getValue() ? Managers.TEXT.getStringWidth(str2.toLowerCase()) : Managers.TEXT.getStringWidth(str2)) - 2, height - j, width - 2 - (this.lowerCase.getValue() ? Managers.TEXT.getStringWidth(str2.toLowerCase()) : Managers.TEXT.getStringWidth(str2)) - 1, (j == 1) ? height : (height - j + 10), this.jamieArray.getValue() ? this.getJamieColor(counter - 2) : (ClickGui.INSTANCE.rainbow.getValue() ? ((ClickGui.INSTANCE.hudRainbow.getValue() == ClickGui.HudRainbow.ROLLING) ? ColorUtil.rainbow(counter * ClickGui.INSTANCE.rainbowDelay.getValue()).getRGB() : Managers.COLORS.getRainbow().getRGB()) : ColorUtil.pulseColor(color, counter, 14).getRGB()));
                        int a2 = k + 1;
                        if (a2 >= Managers.MODULES.sortedLength.size()) {
                            a2 = k;
                        }
                        final Module nextModule = Managers.MODULES.sortedLength.get(a2);
                        String nextStr2 = nextModule.getArrayListInfo();
                        if (this.forgeHax.getValue()) {
                            nextStr2 = nextModule.getArrayListInfo() + ChatFormatting.RESET + "<";
                        }
                        Gui.drawRect(width - 2 - (this.lowerCase.getValue() ? Managers.TEXT.getStringWidth(str2.toLowerCase()) : Managers.TEXT.getStringWidth(str2)) - 2, height - j - 1, (a2 == k) ? width : (width - 2 - (this.lowerCase.getValue() ? Managers.TEXT.getStringWidth(str2.toLowerCase()) : Managers.TEXT.getStringWidth(str2)) + ((this.lowerCase.getValue() ? Managers.TEXT.getStringWidth(str2.toLowerCase()) : Managers.TEXT.getStringWidth(str2)) - (this.lowerCase.getValue() ? Managers.TEXT.getStringWidth(nextStr2.toLowerCase()) : Managers.TEXT.getStringWidth(nextStr2))) - 1), height - j, this.jamieArray.getValue() ? this.getJamieColor(counter - 2) : (ClickGui.INSTANCE.rainbow.getValue() ? ((ClickGui.INSTANCE.hudRainbow.getValue() == ClickGui.HudRainbow.ROLLING) ? ColorUtil.rainbow(counter * ClickGui.INSTANCE.rainbowDelay.getValue()).getRGB() : Managers.COLORS.getRainbow().getRGB()) : ColorUtil.pulseColor(color, counter, 14).getRGB()));
                    }
                    Managers.TEXT.drawString(this.lowerCase.getValue() ? str2.toLowerCase() : str2, (float)(width - 2 - (this.lowerCase.getValue() ? Managers.TEXT.getStringWidth(str2.toLowerCase()) : Managers.TEXT.getStringWidth(str2))), (float)(height - j + 1), this.jamieArray.getValue() ? this.getJamieColor(counter - 2) : (ClickGui.INSTANCE.rainbow.getValue() ? ((ClickGui.INSTANCE.hudRainbow.getValue() == ClickGui.HudRainbow.ROLLING) ? ColorUtil.rainbow(counter * ClickGui.INSTANCE.rainbowDelay.getValue()).getRGB() : Managers.COLORS.getRainbow().getRGB()) : ColorUtil.pulseColor(color, counter, 14).getRGB()), true);
                    ++counter;
                }
            }
        }
        final String grayString = this.grayColors.getValue() ? String.valueOf(ChatFormatting.GRAY) : "";
        int i = (HUD.mc.currentScreen instanceof GuiChat && this.renderingUp.getValue()) ? 13 : (this.renderingUp.getValue() ? -2 : 0);
        if (this.renderingUp.getValue()) {
            if (this.potions.getValue()) {
                final List<PotionEffect> effects = new ArrayList<>(HUD.mc.player.getActivePotionEffects());
                for (final PotionEffect potionEffect : effects) {
                    final String str3 = this.getColoredPotionString(potionEffect);
                    i += 10;
                    Managers.TEXT.drawString(this.lowerCase.getValue() ? str3.toLowerCase() : str3, (float)(width - (this.lowerCase.getValue() ? Managers.TEXT.getStringWidth(str3.toLowerCase()) : Managers.TEXT.getStringWidth(str3)) - 2), (float)(height - 2 - i), this.potionColor.getValue() ? potionEffect.getPotion().getLiquidColor() : (ClickGui.INSTANCE.rainbow.getValue() ? ((ClickGui.INSTANCE.hudRainbow.getValue() == ClickGui.HudRainbow.ROLLING) ? ColorUtil.rainbow(counter * ClickGui.INSTANCE.rainbowDelay.getValue()).getRGB() : Managers.COLORS.getRainbow().getRGB()) : ColorUtil.pulseColor(color, counter, 14).getRGB()), true);
                    ++counter;
                }
            }
            if (this.speed.getValue()) {
                final String str2 = grayString + "Speed " + ChatFormatting.WHITE + Managers.SPEED.getSpeedKpH() + " km/h";
                i += 10;
                Managers.TEXT.drawString(this.lowerCase.getValue() ? str2.toLowerCase() : str2, (float)(width - (this.lowerCase.getValue() ? Managers.TEXT.getStringWidth(str2.toLowerCase()) : Managers.TEXT.getStringWidth(str2)) - 2), (float)(height - 2 - i), ClickGui.INSTANCE.rainbow.getValue() ? ((ClickGui.INSTANCE.hudRainbow.getValue() == ClickGui.HudRainbow.ROLLING) ? ColorUtil.rainbow(counter * ClickGui.INSTANCE.rainbowDelay.getValue()).getRGB() : Managers.COLORS.getRainbow().getRGB()) : ColorUtil.pulseColor(color, counter, 14).getRGB(), true);
                ++counter;
            }
            if (this.time.getValue()) {
                final String str2 = grayString + "Time " + ChatFormatting.WHITE + new SimpleDateFormat("h:mm a").format(new Date());
                i += 10;
                Managers.TEXT.drawString(this.lowerCase.getValue() ? str2.toLowerCase() : str2, (float)(width - (this.lowerCase.getValue() ? Managers.TEXT.getStringWidth(str2.toLowerCase()) : Managers.TEXT.getStringWidth(str2)) - 2), (float)(height - 2 - i), ClickGui.INSTANCE.rainbow.getValue() ? ((ClickGui.INSTANCE.hudRainbow.getValue() == ClickGui.HudRainbow.ROLLING) ? ColorUtil.rainbow(counter * ClickGui.INSTANCE.rainbowDelay.getValue()).getRGB() : Managers.COLORS.getRainbow().getRGB()) : ColorUtil.pulseColor(color, counter, 14).getRGB(), true);
                ++counter;
            }
            if (this.tps.getValue()) {
                final String str2 = grayString + "TPS " + ChatFormatting.WHITE + Managers.SERVER.getTPS();
                i += 10;
                Managers.TEXT.drawString(this.lowerCase.getValue() ? str2.toLowerCase() : str2, (float)(width - (this.lowerCase.getValue() ? Managers.TEXT.getStringWidth(str2.toLowerCase()) : Managers.TEXT.getStringWidth(str2)) - 2), (float)(height - 2 - i), ClickGui.INSTANCE.rainbow.getValue() ? ((ClickGui.INSTANCE.hudRainbow.getValue() == ClickGui.HudRainbow.ROLLING) ? ColorUtil.rainbow(counter * ClickGui.INSTANCE.rainbowDelay.getValue()).getRGB() : Managers.COLORS.getRainbow().getRGB()) : ColorUtil.pulseColor(color, counter, 14).getRGB(), true);
                ++counter;
            }
            final String fpsText = grayString + "FPS " + ChatFormatting.WHITE + Minecraft.getDebugFPS();
            final String str4 = grayString + "Ping " + ChatFormatting.WHITE + Managers.SERVER.getPing();
            if (Managers.TEXT.getStringWidth(str4) > Managers.TEXT.getStringWidth(fpsText)) {
                if (this.ping.getValue()) {
                    i += 10;
                    Managers.TEXT.drawString(this.lowerCase.getValue() ? str4.toLowerCase() : str4, (float)(width - (this.lowerCase.getValue() ? Managers.TEXT.getStringWidth(str4.toLowerCase()) : Managers.TEXT.getStringWidth(str4)) - 2), (float)(height - 2 - i), ClickGui.INSTANCE.rainbow.getValue() ? ((ClickGui.INSTANCE.hudRainbow.getValue() == ClickGui.HudRainbow.ROLLING) ? ColorUtil.rainbow(counter * ClickGui.INSTANCE.rainbowDelay.getValue()).getRGB() : Managers.COLORS.getRainbow().getRGB()) : ColorUtil.pulseColor(color, counter, 14).getRGB(), true);
                    ++counter;
                }
                if (this.fps.getValue()) {
                    i += 10;
                    Managers.TEXT.drawString(this.lowerCase.getValue() ? fpsText.toLowerCase() : fpsText, (float)(width - (this.lowerCase.getValue() ? Managers.TEXT.getStringWidth(fpsText.toLowerCase()) : Managers.TEXT.getStringWidth(fpsText)) - 2), (float)(height - 2 - i), ClickGui.INSTANCE.rainbow.getValue() ? ((ClickGui.INSTANCE.hudRainbow.getValue() == ClickGui.HudRainbow.ROLLING) ? ColorUtil.rainbow(counter * ClickGui.INSTANCE.rainbowDelay.getValue()).getRGB() : Managers.COLORS.getRainbow().getRGB()) : ColorUtil.pulseColor(color, counter, 14).getRGB(), true);
                }
            }
            else {
                if (this.fps.getValue()) {
                    i += 10;
                    Managers.TEXT.drawString(this.lowerCase.getValue() ? fpsText.toLowerCase() : fpsText, (float)(width - (this.lowerCase.getValue() ? Managers.TEXT.getStringWidth(fpsText.toLowerCase()) : Managers.TEXT.getStringWidth(fpsText)) - 2), (float)(height - 2 - i), ClickGui.INSTANCE.rainbow.getValue() ? ((ClickGui.INSTANCE.hudRainbow.getValue() == ClickGui.HudRainbow.ROLLING) ? ColorUtil.rainbow(counter * ClickGui.INSTANCE.rainbowDelay.getValue()).getRGB() : Managers.COLORS.getRainbow().getRGB()) : ColorUtil.pulseColor(color, counter, 14).getRGB(), true);
                    ++counter;
                }
                if (this.ping.getValue()) {
                    i += 10;
                    Managers.TEXT.drawString(this.lowerCase.getValue() ? str4.toLowerCase() : str4, (float)(width - (this.lowerCase.getValue() ? Managers.TEXT.getStringWidth(str4.toLowerCase()) : Managers.TEXT.getStringWidth(str4)) - 2), (float)(height - 2 - i), ClickGui.INSTANCE.rainbow.getValue() ? ((ClickGui.INSTANCE.hudRainbow.getValue() == ClickGui.HudRainbow.ROLLING) ? ColorUtil.rainbow(counter * ClickGui.INSTANCE.rainbowDelay.getValue()).getRGB() : Managers.COLORS.getRainbow().getRGB()) : ColorUtil.pulseColor(color, counter, 14).getRGB(), true);
                }
            }
        }
        else {
            if (this.potions.getValue()) {
                final List<PotionEffect> effects = new ArrayList<>(HUD.mc.player.getActivePotionEffects());
                for (final PotionEffect potionEffect : effects) {
                    final String str3 = this.getColoredPotionString(potionEffect);
                    Managers.TEXT.drawString(this.lowerCase.getValue() ? str3.toLowerCase() : str3, (float)(width - (this.lowerCase.getValue() ? Managers.TEXT.getStringWidth(str3.toLowerCase()) : Managers.TEXT.getStringWidth(str3)) - 2), (float)(2 + i++ * 10), this.potionColor.getValue() ? potionEffect.getPotion().getLiquidColor() : (ClickGui.INSTANCE.rainbow.getValue() ? ((ClickGui.INSTANCE.hudRainbow.getValue() == ClickGui.HudRainbow.ROLLING) ? ColorUtil.rainbow(counter * ClickGui.INSTANCE.rainbowDelay.getValue()).getRGB() : Managers.COLORS.getRainbow().getRGB()) : ColorUtil.pulseColor(color, counter, 14).getRGB()), true);
                    ++counter;
                }
            }
            if (this.speed.getValue()) {
                final String str2 = grayString + "Speed " + ChatFormatting.WHITE + Managers.SPEED.getSpeedKpH() + " km/h";
                Managers.TEXT.drawString(this.lowerCase.getValue() ? str2.toLowerCase() : str2, (float)(width - (this.lowerCase.getValue() ? Managers.TEXT.getStringWidth(str2.toLowerCase()) : Managers.TEXT.getStringWidth(str2)) - 2), (float)(2 + i++ * 10), ClickGui.INSTANCE.rainbow.getValue() ? ((ClickGui.INSTANCE.hudRainbow.getValue() == ClickGui.HudRainbow.ROLLING) ? ColorUtil.rainbow(counter * ClickGui.INSTANCE.rainbowDelay.getValue()).getRGB() : Managers.COLORS.getRainbow().getRGB()) : ColorUtil.pulseColor(color, counter, 14).getRGB(), true);
                ++counter;
            }
            if (this.time.getValue()) {
                final String str2 = grayString + "Time " + ChatFormatting.WHITE + new SimpleDateFormat("h:mm a").format(new Date());
                Managers.TEXT.drawString(this.lowerCase.getValue() ? str2.toLowerCase() : str2, (float)(width - (this.lowerCase.getValue() ? Managers.TEXT.getStringWidth(str2.toLowerCase()) : Managers.TEXT.getStringWidth(str2)) - 2), (float)(2 + i++ * 10), ClickGui.INSTANCE.rainbow.getValue() ? ((ClickGui.INSTANCE.hudRainbow.getValue() == ClickGui.HudRainbow.ROLLING) ? ColorUtil.rainbow(counter * ClickGui.INSTANCE.rainbowDelay.getValue()).getRGB() : Managers.COLORS.getRainbow().getRGB()) : ColorUtil.pulseColor(color, counter, 14).getRGB(), true);
                ++counter;
            }
            if (this.tps.getValue()) {
                final String str2 = grayString + "TPS " + ChatFormatting.WHITE + Managers.SERVER.getTPS();
                Managers.TEXT.drawString(this.lowerCase.getValue() ? str2.toLowerCase() : str2, (float)(width - (this.lowerCase.getValue() ? Managers.TEXT.getStringWidth(str2.toLowerCase()) : Managers.TEXT.getStringWidth(str2)) - 2), (float)(2 + i++ * 10), ClickGui.INSTANCE.rainbow.getValue() ? ((ClickGui.INSTANCE.hudRainbow.getValue() == ClickGui.HudRainbow.ROLLING) ? ColorUtil.rainbow(counter * ClickGui.INSTANCE.rainbowDelay.getValue()).getRGB() : Managers.COLORS.getRainbow().getRGB()) : ColorUtil.pulseColor(color, counter, 14).getRGB(), true);
                ++counter;
            }
            final String fpsText = grayString + "FPS " + ChatFormatting.WHITE + Minecraft.getDebugFPS();
            final String str4 = grayString + "Ping " + ChatFormatting.WHITE + Managers.SERVER.getPing();
            if (Managers.TEXT.getStringWidth(str4) > Managers.TEXT.getStringWidth(fpsText)) {
                if (this.ping.getValue()) {
                    Managers.TEXT.drawString(this.lowerCase.getValue() ? str4.toLowerCase() : str4, (float)(width - (this.lowerCase.getValue() ? Managers.TEXT.getStringWidth(str4.toLowerCase()) : Managers.TEXT.getStringWidth(str4)) - 2), (float)(2 + i++ * 10), ClickGui.INSTANCE.rainbow.getValue() ? ((ClickGui.INSTANCE.hudRainbow.getValue() == ClickGui.HudRainbow.ROLLING) ? ColorUtil.rainbow(counter * ClickGui.INSTANCE.rainbowDelay.getValue()).getRGB() : Managers.COLORS.getRainbow().getRGB()) : ColorUtil.pulseColor(color, counter, 14).getRGB(), true);
                    ++counter;
                }
                if (this.fps.getValue()) {
                    Managers.TEXT.drawString(this.lowerCase.getValue() ? fpsText.toLowerCase() : fpsText, (float)(width - (this.lowerCase.getValue() ? Managers.TEXT.getStringWidth(fpsText.toLowerCase()) : Managers.TEXT.getStringWidth(fpsText)) - 2), (float)(2 + i++ * 10), ClickGui.INSTANCE.rainbow.getValue() ? ((ClickGui.INSTANCE.hudRainbow.getValue() == ClickGui.HudRainbow.ROLLING) ? ColorUtil.rainbow(counter * ClickGui.INSTANCE.rainbowDelay.getValue()).getRGB() : Managers.COLORS.getRainbow().getRGB()) : ColorUtil.pulseColor(color, counter, 14).getRGB(), true);
                }
            }
            else {
                if (this.fps.getValue()) {
                    Managers.TEXT.drawString(this.lowerCase.getValue() ? fpsText.toLowerCase() : fpsText, (float)(width - (this.lowerCase.getValue() ? Managers.TEXT.getStringWidth(fpsText.toLowerCase()) : Managers.TEXT.getStringWidth(fpsText)) - 2), (float)(2 + i++ * 10), ClickGui.INSTANCE.rainbow.getValue() ? ((ClickGui.INSTANCE.hudRainbow.getValue() == ClickGui.HudRainbow.ROLLING) ? ColorUtil.rainbow(counter * ClickGui.INSTANCE.rainbowDelay.getValue()).getRGB() : Managers.COLORS.getRainbow().getRGB()) : ColorUtil.pulseColor(color, counter, 14).getRGB(), true);
                    ++counter;
                }
                if (this.ping.getValue()) {
                    Managers.TEXT.drawString(this.lowerCase.getValue() ? str4.toLowerCase() : str4, (float)(width - (this.lowerCase.getValue() ? Managers.TEXT.getStringWidth(str4.toLowerCase()) : Managers.TEXT.getStringWidth(str4)) - 2), (float)(2 + i++ * 10), ClickGui.INSTANCE.rainbow.getValue() ? ((ClickGui.INSTANCE.hudRainbow.getValue() == ClickGui.HudRainbow.ROLLING) ? ColorUtil.rainbow(counter * ClickGui.INSTANCE.rainbowDelay.getValue()).getRGB() : Managers.COLORS.getRainbow().getRGB()) : ColorUtil.pulseColor(color, counter, 14).getRGB(), true);
                }
            }
        }
        final boolean inHell = HUD.mc.world.getBiome(HUD.mc.player.getPosition()).getBiomeName().equals("Hell");
        final int posX = (int)HUD.mc.player.posX;
        final int posY = (int)HUD.mc.player.posY;
        final int posZ = (int)HUD.mc.player.posZ;
        final float nether = inHell ? 8.0f : 0.125f;
        final int hposX = (int)(HUD.mc.player.posX * nether);
        final int hposZ = (int)(HUD.mc.player.posZ * nether);
        final int yawPitch = (int)MathHelper.wrapDegrees(HUD.mc.player.rotationYaw);
        final int p = this.coords.getValue() ? 0 : 11;
        i = ((HUD.mc.currentScreen instanceof GuiChat) ? 14 : 0);
        final String coordinates = (this.lowerCase.getValue() ? "XYZ: ".toLowerCase() : "XYZ: ") + ChatFormatting.WHITE + (inHell ? (posX + ", " + posY + ", " + posZ + ChatFormatting.GRAY + " [" + ChatFormatting.WHITE + hposX + ", " + hposZ + ChatFormatting.GRAY + "]" + ChatFormatting.WHITE) : (posX + ", " + posY + ", " + posZ + ChatFormatting.GRAY + " [" + ChatFormatting.WHITE + hposX + ", " + hposZ + ChatFormatting.GRAY + "]"));
        String direction = this.direction.getValue() ? Managers.ROTATIONS.getDirection4D(false) : "";
        String yaw = this.direction.getValue() ? ((this.lowerCase.getValue() ? "Yaw: ".toLowerCase() : "Yaw: ") + ChatFormatting.WHITE + yawPitch) : "";
        final String coords = this.coords.getValue() ? coordinates : "";
        i += 10;
        if (HUD.mc.currentScreen instanceof GuiChat && this.direction.getValue()) {
            yaw = "";
            direction = (this.lowerCase.getValue() ? "Yaw: ".toLowerCase() : "Yaw: ") + ChatFormatting.WHITE + yawPitch + ChatFormatting.RESET + " " + this.getFacingDirectionShort();
        }
        if (ClickGui.INSTANCE.rainbow.getValue()) {
            final String rainbowCoords = this.coords.getValue() ? ((this.lowerCase.getValue() ? "XYZ: ".toLowerCase() : "XYZ: ") + ChatFormatting.WHITE + (inHell ? (posX + ", " + posY + ", " + posZ + ChatFormatting.GRAY + " [" + ChatFormatting.WHITE + hposX + ", " + hposZ + ChatFormatting.GRAY + "]" + ChatFormatting.WHITE) : (posX + ", " + posY + ", " + posZ + ChatFormatting.GRAY + " [" + ChatFormatting.WHITE + hposX + ", " + hposZ + ChatFormatting.GRAY + "]"))) : "";
            if (ClickGui.INSTANCE.hudRainbow.getValue() == ClickGui.HudRainbow.STATIC) {
                Managers.TEXT.drawString(direction, 2.0f, (float)(height - i - 11 + p), Managers.COLORS.getRainbow().getRGB(), true);
                Managers.TEXT.drawString(yaw, 2.0f, (float)(height - i - 22 + p), Managers.COLORS.getRainbow().getRGB(), true);
                Managers.TEXT.drawString(rainbowCoords, 2.0f, (float)(height - i), Managers.COLORS.getRainbow().getRGB(), true);
            }
            else {
                if (HUD.mc.currentScreen instanceof GuiChat && this.direction.getValue()) {
                    this.drawDoubleRainbowRollingString(this.lowerCase.getValue() ? "Yaw: ".toLowerCase() : "Yaw: ", String.valueOf(ChatFormatting.WHITE) + yawPitch, 2.0f, (float)(height - i - 11 + p));
                    final String uh = "Yaw: " + ChatFormatting.WHITE + yawPitch;
                    Managers.TEXT.drawRollingRainbowString(" " + this.getFacingDirectionShort(), 2.0f + Managers.TEXT.getStringWidth(uh), (float)(height - i - 11 + p), true);
                }
                else {
                    Managers.TEXT.drawRollingRainbowString(this.direction.getValue() ? direction : "", 2.0f, (float)(height - i - 11 + p), true);
                    this.drawDoubleRainbowRollingString(this.direction.getValue() ? (this.lowerCase.getValue() ? "Yaw: ".toLowerCase() : "Yaw: ") : "", this.direction.getValue() ? (String.valueOf(ChatFormatting.WHITE) + yawPitch) : "", 2.0f, (float)(height - i - 22 + p));
                }
                this.drawDoubleRainbowRollingString(this.coords.getValue() ? (this.lowerCase.getValue() ? "XYZ: ".toLowerCase() : "XYZ: ") : "", this.coords.getValue() ? (ChatFormatting.WHITE + (inHell ? (posX + ", " + posY + ", " + posZ + ChatFormatting.GRAY + " [" + ChatFormatting.WHITE + hposX + ", " + hposZ + ChatFormatting.GRAY + "]" + ChatFormatting.WHITE) : (posX + ", " + posY + ", " + posZ + ChatFormatting.GRAY + " [" + ChatFormatting.WHITE + hposX + ", " + hposZ + ChatFormatting.GRAY + "]"))) : "", 2.0f, (float)(height - i));
            }
        }
        else {
            Managers.TEXT.drawString(direction, 2.0f, (float)(height - i - 11 + p), this.color, true);
            Managers.TEXT.drawString(yaw, 2.0f, (float)(height - i - 22 + p), this.color, true);
            Managers.TEXT.drawString(coords, 2.0f, (float)(height - i), this.color, true);
        }
        if (this.armor.getValue()) {
            this.drawArmorHUD();
        }
        if (this.greeter.getValue()) {
            this.drawWelcomer();
        }
        if (this.lag.getValue()) {
            this.drawLagOMeter();
        }
    }

    private void drawWelcomer() {
        String text2;
        int width = Managers.TEXT.scaledWidth;
        String nameColor = this.greeterNameColor.getValue() ? String.valueOf(ChatFormatting.WHITE) : "";
        String string = text2 = this.lowerCase.getValue() ? "Welcome, ".toLowerCase() : "Welcome, ";
        if (this.greeterMode.getValue() == GreeterMode.PLAYER) {
            if (this.greeter.getValue()) {
                text2 = text2 + nameColor + HUD.mc.player.getDisplayNameString();
            }
            if (ClickGui.INSTANCE.rainbow.getValue()) {
                if (ClickGui.INSTANCE.hudRainbow.getValue() == ClickGui.HudRainbow.STATIC) {
                    Managers.TEXT.drawString(text2 + ChatFormatting.RESET + " :')", (float)width / 2.0f - (float)Managers.TEXT.getStringWidth(text2) / 2.0f + 2.0f, 2.0f, Managers.COLORS.getRainbow().getRGB(), true);
                } else if (this.greeterNameColor.getValue()) {
                    this.drawDoubleRainbowRollingString(this.lowerCase.getValue() ? "Welcome,".toLowerCase() : "Welcome,", (FontMod.INSTANCE.isOn() ? "" : " ") + ChatFormatting.WHITE + HUD.mc.player.getDisplayNameString(), (float)width / 2.0f - (float)Managers.TEXT.getStringWidth(text2) / 2.0f + 2.0f, 2.0f);
                    Managers.TEXT.drawRollingRainbowString(" :')", (float)width / 2.0f - (float)Managers.TEXT.getStringWidth(text2) / 2.0f + 1.5f + (float)Managers.TEXT.getStringWidth(text2) - (FontMod.INSTANCE.isOn() ? 1.5f : 0.0f), 2.0f, true);
                } else {
                    Managers.TEXT.drawRollingRainbowString((this.lowerCase.getValue() ? "Welcome,".toLowerCase() : "Welcome, ") + HUD.mc.player.getDisplayNameString() + " :')", (float)width / 2.0f - (float)Managers.TEXT.getStringWidth(text2) / 2.0f + 2.0f, 2.0f, true);
                }
            } else {
                Managers.TEXT.drawString(text2 + ChatFormatting.RESET + " :')", (float)width / 2.0f - (float)Managers.TEXT.getStringWidth(text2) / 2.0f + 2.0f, 2.0f, this.color, true);
            }
        } else {
            String lel = this.greeterText.getValue();
            if (this.greeter.getValue()) {
                lel = this.greeterText.getValue();
            }
            if (ClickGui.INSTANCE.rainbow.getValue()) {
                if (ClickGui.INSTANCE.hudRainbow.getValue() == ClickGui.HudRainbow.STATIC) {
                    Managers.TEXT.drawString(lel, (float)width / 2.0f - (float)Managers.TEXT.getStringWidth(lel) / 2.0f + 2.0f, 2.0f, Managers.COLORS.getRainbow().getRGB(), true);
                } else {
                    Managers.TEXT.drawRollingRainbowString(lel, (float)width / 2.0f - (float)Managers.TEXT.getStringWidth(lel) / 2.0f + 2.0f, 2.0f, true);
                }
            } else {
                Managers.TEXT.drawString(lel, (float)width / 2.0f - (float)Managers.TEXT.getStringWidth(lel) / 2.0f + 2.0f, 2.0f, this.color, true);
            }
        }
    }

    private void drawLagOMeter() {
        int width = Managers.TEXT.scaledWidth;
        if (Managers.SERVER.isServerNotResponding()) {
            String text2 = ChatFormatting.RED + (this.lowerCase.getValue() ? "Server is lagging for ".toLowerCase() : "Server is lagging for ") + MathUtil.round((float)Managers.SERVER.serverRespondingTime() / 1000.0f, 1) + "s.";
            Managers.TEXT.drawString(text2, (float)width / 2.0f - (float)Managers.TEXT.getStringWidth(text2) / 2.0f + 2.0f, 20.0f, this.color, true);
        }
    }

    private void drawArmorHUD() {
        int width = Managers.TEXT.scaledWidth;
        int height = Managers.TEXT.scaledHeight;
        GlStateManager.enableTexture2D();
        int i = width / 2;
        int iteration = 0;
        int y = height - 55 - (HUD.mc.player.isInWater() && HUD.mc.playerController.gameIsSurvivalOrAdventure() ? 10 : 0);
        for (ItemStack is : HUD.mc.player.inventory.armorInventory) {
            ++iteration;
            if (is.isEmpty()) continue;
            int x = i - 90 + (9 - iteration) * 20 + 2;
            GlStateManager.enableDepth();
            HUD.mc.getRenderItem().zLevel = 200.0f;
            mc.getRenderItem().renderItemAndEffectIntoGUI(is, x, y);
            mc.getRenderItem().renderItemOverlayIntoGUI(HUD.mc.fontRenderer, is, x, y, "");
            HUD.mc.getRenderItem().zLevel = 0.0f;
            GlStateManager.enableTexture2D();
            GlStateManager.disableLighting();
            GlStateManager.disableDepth();
            String s = is.getCount() > 1 ? String.valueOf(is.getCount()) : "";
            Managers.TEXT.drawStringWithShadow(s, x + 19 - 2 - Managers.TEXT.getStringWidth(s), y + 9, 0xFFFFFF);
            float green = ((float)is.getMaxDamage() - (float)is.getItemDamage()) / (float)is.getMaxDamage();
            float red = 1.0f - green;
            int dmg = 100 - (int)(red * 100.0f);
            Managers.TEXT.drawStringWithShadow(String.valueOf(dmg), (float)(x + 8) - (float)Managers.TEXT.getStringWidth(String.valueOf(dmg)) / 2.0f, y - 11, ColorUtil.toRGBA((int)(red * 255.0f), (int)(green * 255.0f), 0));
        }
        GlStateManager.enableDepth();
        GlStateManager.disableLighting();
    }

    private void drawPvPInfo() {
        float yOffset = (float)Managers.TEXT.scaledHeight / 2.0f;
        int totemCount = HUD.mc.player.inventory.mainInventory.stream().filter(itemStack -> itemStack.getItem() == Items.TOTEM_OF_UNDYING).mapToInt(ItemStack::getCount).sum();
        if (HUD.mc.player.getHeldItemOffhand().getItem() == Items.TOTEM_OF_UNDYING) {
            totemCount += HUD.mc.player.getHeldItemOffhand().getCount();
        }
        int pingCount = Managers.SERVER.getPing();
        EntityPlayer target = EntityUtil.getClosestEnemy(7.0);
        String totemString = String.valueOf(totemCount != 0 ? ChatFormatting.GREEN : ChatFormatting.RED) + totemCount;
        String htrColor = String.valueOf(target != null && HUD.mc.player.getDistance(target) <= Aura.INSTANCE.range.getValue() ? ChatFormatting.GREEN : ChatFormatting.DARK_RED);
        String plrColor = String.valueOf(target != null && HUD.mc.player.getDistance(target) <= 5.0f && AutoTrap.INSTANCE.isOn() ? ChatFormatting.GREEN : ChatFormatting.DARK_RED);
        String htr = "HTR";
        String plr = "PLR";
        String pingColor = pingCount < 40 ? String.valueOf(ChatFormatting.GREEN) : (pingCount < 65 ? String.valueOf(ChatFormatting.DARK_GREEN) : (pingCount < 80 ? String.valueOf(ChatFormatting.YELLOW) : (pingCount < 110 ? String.valueOf(ChatFormatting.RED) : (pingCount < 160 ? String.valueOf(ChatFormatting.DARK_RED) : String.valueOf(ChatFormatting.DARK_RED)))));
        String safetyColor = EntityUtil.getUnsafeBlocksList(new Vec3d(HUD.mc.player.posX, HUD.mc.player.posY + 0.5, HUD.mc.player.posZ), 0, false).size() != 0 ? String.valueOf(ChatFormatting.DARK_RED) : String.valueOf(ChatFormatting.GREEN);
        Managers.TEXT.drawString(htrColor + htr, 2.0f, yOffset - 20.0f, this.color, true);
        Managers.TEXT.drawString(plrColor + plr, 2.0f, yOffset - 10.0f, this.color, true);
        Managers.TEXT.drawString(pingColor + pingCount + " MS", 2.0f, yOffset, this.color, true);
        Managers.TEXT.drawString(totemString, 2.0f, yOffset + 10.0f, this.color, true);
        Managers.TEXT.drawString(safetyColor + "LBY", 2.0f, yOffset + 20.0f, this.color, true);
    }

    private void drawDoubleRainbowRollingString(String first, String second, float x, float y) {
        Managers.TEXT.drawRollingRainbowString(first, x, y, true);
        Managers.TEXT.drawString(second, x + (float)Managers.TEXT.getStringWidth(first), y, -1, true);
    }

    private void drawTextRadar(int yOffset) {
        if (!this.players.isEmpty()) {
            int y = Managers.TEXT.getFontHeight() + 7 + yOffset;
            for (Map.Entry<String, Integer> player : this.players.entrySet()) {
                String text2 = player.getKey() + " ";
                int textHeight = Managers.TEXT.getFontHeight() + 1;
                if (ClickGui.INSTANCE.rainbow.getValue()) {
                    if (ClickGui.INSTANCE.hudRainbow.getValue() == ClickGui.HudRainbow.STATIC) {
                        Managers.TEXT.drawString(text2, 2.0f, y, ColorUtil.rainbow(ClickGui.INSTANCE.rainbowDelay.getValue()).getRGB(), true);
                        y += textHeight;
                        continue;
                    }
                    Managers.TEXT.drawString(text2, 2.0f, y, ColorUtil.rainbow(ClickGui.INSTANCE.rainbowDelay.getValue()).getRGB(), true);
                    y += textHeight;
                    continue;
                }
                Managers.TEXT.drawString(text2, 2.0f, y, this.color, true);
                y += textHeight;
            }
        }
    }

    private Map<String, Integer> getTextRadarMap() {
        Map<String, Integer> retval = new HashMap<>();
        DecimalFormat dfDistance = new DecimalFormat("#.#");
        dfDistance.setRoundingMode(RoundingMode.CEILING);
        StringBuilder distanceSB = new StringBuilder();
        for (EntityPlayer player : HUD.mc.world.playerEntities) {
            if (player.isInvisible() || player.getName().equals(HUD.mc.player.getName())) continue;
            int distanceInt = (int)HUD.mc.player.getDistance(player);
            String distance = dfDistance.format(distanceInt);
            if (distanceInt >= 25) {
                distanceSB.append(ChatFormatting.GREEN);
            } else if (distanceInt > 10) {
                distanceSB.append(ChatFormatting.YELLOW);
            } else {
                distanceSB.append(ChatFormatting.RED);
            }
            distanceSB.append(distance);
            retval.put((Managers.FRIENDS.isCool(player.getName()) ? ChatFormatting.GOLD + "< > " + ChatFormatting.RESET : "") + (Managers.FRIENDS.isFriend(player) ? ChatFormatting.AQUA : ChatFormatting.RESET) + player.getName() + " " + ChatFormatting.WHITE + "[" + ChatFormatting.RESET + distanceSB + "m" + ChatFormatting.WHITE + "] " + ChatFormatting.GREEN, (int)HUD.mc.player.getDistance(player));
            distanceSB.setLength(0);
        }
        if (!retval.isEmpty()) {
            retval = MathUtil.sortByValue(retval, false);
        }
        return retval;
    }

    private void drawCombatCount() {
        int width = Managers.TEXT.scaledWidth;
        int height = Managers.TEXT.scaledHeight;
        int i = width / 2;
        int y = height - this.combatCountY.getValue();
        int x = i + this.combatCountX.getValue();
        GlStateManager.enableTexture2D();
        int totems = InventoryUtil.getItemCount(Items.TOTEM_OF_UNDYING);
        GlStateManager.enableDepth();
        HUD.mc.getRenderItem().zLevel = 200.0f;
        mc.getRenderItem().renderItemAndEffectIntoGUI(new ItemStack(Items.TOTEM_OF_UNDYING), x, y);
        mc.getRenderItem().renderItemOverlayIntoGUI(HUD.mc.fontRenderer, new ItemStack(Items.TOTEM_OF_UNDYING), x, y, "");
        HUD.mc.getRenderItem().zLevel = 0.0f;
        GlStateManager.enableTexture2D();
        GlStateManager.disableLighting();
        GlStateManager.disableDepth();
        Managers.TEXT.drawStringWithShadow(String.valueOf(totems), x + 19 - 2 - Managers.TEXT.getStringWidth(String.valueOf(totems)), y + 9, 0xFFFFFF);
        GlStateManager.enableDepth();
        GlStateManager.disableLighting();
        int crystals = InventoryUtil.getItemCount(Items.END_CRYSTAL);
        GlStateManager.enableDepth();
        HUD.mc.getRenderItem().zLevel = 200.0f;
        mc.getRenderItem().renderItemAndEffectIntoGUI(new ItemStack(Items.END_CRYSTAL), x += 20, y);
        mc.getRenderItem().renderItemOverlayIntoGUI(HUD.mc.fontRenderer, new ItemStack(Items.END_CRYSTAL), x, y, "");
        HUD.mc.getRenderItem().zLevel = 0.0f;
        GlStateManager.enableTexture2D();
        GlStateManager.disableLighting();
        GlStateManager.disableDepth();
        Managers.TEXT.drawStringWithShadow(String.valueOf(crystals), x + 19 - 2 - Managers.TEXT.getStringWidth(String.valueOf(crystals)), y + 9, 0xFFFFFF);
        GlStateManager.enableDepth();
        GlStateManager.disableLighting();
        int EXP = InventoryUtil.getItemCount(Items.EXPERIENCE_BOTTLE);
        GlStateManager.enableDepth();
        HUD.mc.getRenderItem().zLevel = 200.0f;
        mc.getRenderItem().renderItemAndEffectIntoGUI(new ItemStack(Items.EXPERIENCE_BOTTLE), x += 20, y);
        mc.getRenderItem().renderItemOverlayIntoGUI(HUD.mc.fontRenderer, new ItemStack(Items.EXPERIENCE_BOTTLE), x, y, "");
        HUD.mc.getRenderItem().zLevel = 0.0f;
        GlStateManager.enableTexture2D();
        GlStateManager.disableLighting();
        GlStateManager.disableDepth();
        Managers.TEXT.drawStringWithShadow(String.valueOf(EXP), x + 19 - 2 - Managers.TEXT.getStringWidth(String.valueOf(EXP)), y + 9, 0xFFFFFF);
        GlStateManager.enableDepth();
        GlStateManager.disableLighting();
        int goldenApple = InventoryUtil.getItemCount(Items.GOLDEN_APPLE);
        GlStateManager.enableDepth();
        HUD.mc.getRenderItem().zLevel = 200.0f;
        mc.getRenderItem().renderItemAndEffectIntoGUI(new ItemStack(Items.GOLDEN_APPLE), x += 20, y);
        mc.getRenderItem().renderItemOverlayIntoGUI(HUD.mc.fontRenderer, new ItemStack(Items.GOLDEN_APPLE), x, y, "");
        HUD.mc.getRenderItem().zLevel = 0.0f;
        GlStateManager.enableTexture2D();
        GlStateManager.disableLighting();
        GlStateManager.disableDepth();
        Managers.TEXT.drawStringWithShadow(String.valueOf(goldenApple), x + 19 - 2 - Managers.TEXT.getStringWidth(String.valueOf(goldenApple)), y + 9, 0xFFFFFF);
        GlStateManager.enableDepth();
        GlStateManager.disableLighting();
        int obsidian = InventoryUtil.getItemCount(Item.getItemFromBlock(Blocks.OBSIDIAN));
        GlStateManager.enableDepth();
        HUD.mc.getRenderItem().zLevel = 200.0f;
        mc.getRenderItem().renderItemAndEffectIntoGUI(new ItemStack(Blocks.OBSIDIAN), x += 20, y);
        mc.getRenderItem().renderItemOverlayIntoGUI(HUD.mc.fontRenderer, new ItemStack(Blocks.OBSIDIAN), x, y, "");
        HUD.mc.getRenderItem().zLevel = 0.0f;
        GlStateManager.enableTexture2D();
        GlStateManager.disableLighting();
        GlStateManager.disableDepth();
        Managers.TEXT.drawStringWithShadow(String.valueOf(obsidian), x + 19 - 2 - Managers.TEXT.getStringWidth(String.valueOf(obsidian)), y + 9, 0xFFFFFF);
        GlStateManager.enableDepth();
        GlStateManager.disableLighting();
    }

    private int getJamieColor(int n) {
        int n2 = Managers.MODULES.getEnabledModules().size();
        int n3 = new Color(91, 206, 250).getRGB();
        int n4 = Color.WHITE.getRGB();
        int n5 = new Color(245, 169, 184).getRGB();
        int n6 = n2 / 5;
        if (n < n6) {
            return n3;
        }
        if (n < n6 * 2) {
            return n5;
        }
        if (n < n6 * 3) {
            return n4;
        }
        if (n < n6 * 4) {
            return n5;
        }
        if (n < n6 * 5) {
            return n3;
        }
        return n3;
    }

    private String getFacingDirectionShort() {
        int dirnumber = Managers.ROTATIONS.getYaw4D();
        if (dirnumber == 0) {
            return "(+Z)";
        }
        if (dirnumber == 1) {
            return "(-X)";
        }
        if (dirnumber == 2) {
            return "(-Z)";
        }
        if (dirnumber == 3) {
            return "(+X)";
        }
        return "Loading...";
    }

    private String getColoredPotionString(PotionEffect effect) {
        Potion potion = effect.getPotion();
        return I18n.format(potion.getName(), new Object[0]) + " " + (effect.getAmplifier() + 1) + " " + ChatFormatting.WHITE + Potion.getPotionDurationString(effect, 1.0f);
    }

    private static enum Page {
        ELEMENTS,
        GLOBAL

    }

    private static enum GreeterMode {
        PLAYER,
        CUSTOM

    }
}

