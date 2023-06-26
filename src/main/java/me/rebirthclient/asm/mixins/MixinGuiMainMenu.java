package me.rebirthclient.asm.mixins;

import java.io.IOException;
import javax.imageio.ImageIO;
import me.rebirthclient.mod.gui.screen.Gui;
import me.rebirthclient.mod.modules.impl.client.ClickGui;
import me.rebirthclient.mod.modules.settings.Bind;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiMainMenu;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value={GuiMainMenu.class})
public abstract class MixinGuiMainMenu
extends GuiScreen {
    @Shadow
    private int widthCopyright;
    @Shadow
    private int widthCopyrightRest;
    @Shadow
    private float minceraftRoll;
    @Shadow
    private static ResourceLocation MINECRAFT_TITLE_TEXTURES;
    @Shadow
    private static ResourceLocation field_194400_H;
    @Shadow
    private float panoramaTimer;
    @Shadow
    private int openGLWarning2Width;
    @Shadow
    private int openGLWarningX1;
    @Shadow
    private int openGLWarningY1;
    @Shadow
    private int openGLWarningX2;
    @Shadow
    private int openGLWarningY2;
    @Shadow
    private String openGLWarning1;
    @Shadow
    private String openGLWarning2;
    @Shadow
    private ResourceLocation backgroundTexture;
    private boolean isGuiOpen;

    @Inject(method={"keyTyped"}, at={@At(value="HEAD")}, cancellable=true)
    protected void keyTyped(char typedChar, int keyCode, CallbackInfo info) {
        if (keyCode == ClickGui.INSTANCE.bind.getValue().getKey()) {
            ClickGui.INSTANCE.enable();
            this.isGuiOpen = true;
        }
        if (keyCode == 1) {
            ClickGui.INSTANCE.disable();
            this.isGuiOpen = false;
        }
        if (this.isGuiOpen) {
            try {
                Gui.INSTANCE.keyTyped(typedChar, keyCode);
            }
            catch (Exception exception) {
                // empty catch block
            }
            info.cancel();
        }
    }

    @Inject(method={"drawScreen(IIF)V"}, at={@At(value="TAIL")})
    public void drawScreenTailHook(int mouseX, int mouseY, float partialTicks, CallbackInfo info) {
        if (this.isGuiOpen) {
            Gui.INSTANCE.drawScreen(mouseX, mouseY, partialTicks);
        }
    }

    @Inject(method={"mouseClicked"}, at={@At(value="HEAD")}, cancellable=true)
    public void mouseClickedHook(int mouseX, int mouseY, int mouseButton, CallbackInfo info) {
        if (this.isGuiOpen) {
            Gui.INSTANCE.mouseClicked(mouseX, mouseY, mouseButton);
            info.cancel();
        }
    }

    protected void mouseReleased(int mouseX, int mouseY, int state) {
        if (this.isGuiOpen) {
            Gui.INSTANCE.mouseReleased(mouseX, mouseY, state);
        }
    }

    @Inject(method={"initGui"}, at={@At(value="RETURN")})
    public void initGui2(CallbackInfo info) throws IOException {
        this.buttonList.add(new GuiButton(114514, this.width / 2 + 2, this.height / 4 + 48 + 48, 98, 20, "AltManager"));
        this.backgroundTexture = this.mc.getTextureManager().getDynamicTextureLocation("background", new DynamicTexture(ImageIO.read(Minecraft.class.getResourceAsStream("/assets/minecraft/textures/rebirth/background.png"))));
    }

    @Overwrite
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        this.panoramaTimer += partialTicks;
        int j = this.width / 2 - 137;
        this.drawGradientRect(0, 0, this.width, this.height, -2130706433, 0xFFFFFF);
        this.drawGradientRect(0, 0, this.width, this.height, 0, Integer.MIN_VALUE);
        this.mc.getTextureManager().bindTexture(MINECRAFT_TITLE_TEXTURES);
        GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
        if ((double)this.minceraftRoll < 1.0E-4) {
            this.drawTexturedModalRect(j, 30, 0, 0, 99, 44);
            this.drawTexturedModalRect(j + 99, 30, 129, 0, 27, 44);
            this.drawTexturedModalRect(j + 99 + 26, 30, 126, 0, 3, 44);
            this.drawTexturedModalRect(j + 99 + 26 + 3, 30, 99, 0, 26, 44);
            this.drawTexturedModalRect(j + 155, 30, 0, 45, 155, 44);
        } else {
            this.drawTexturedModalRect(j, 30, 0, 0, 155, 44);
            this.drawTexturedModalRect(j + 155, 30, 0, 45, 155, 44);
        }
        this.mc.getTextureManager().bindTexture(field_194400_H);
        MixinGuiMainMenu.drawModalRectWithCustomSizedTexture(j + 88, 67, 0.0f, 0.0f, 98, 14, 128.0f, 16.0f);
        if (mouseX > this.widthCopyrightRest && mouseX < this.widthCopyrightRest + this.widthCopyright && mouseY > this.height - 10 && mouseY < this.height && Mouse.isInsideWindow()) {
            MixinGuiMainMenu.drawRect(this.widthCopyrightRest, this.height - 1, this.widthCopyrightRest + this.widthCopyright, this.height, -1);
        }
        if (this.openGLWarning1 != null && !this.openGLWarning1.isEmpty()) {
            MixinGuiMainMenu.drawRect(this.openGLWarningX1 - 2, this.openGLWarningY1 - 2, this.openGLWarningX2 + 2, this.openGLWarningY2 - 1, 0x55200000);
            this.drawString(this.fontRenderer, this.openGLWarning1, this.openGLWarningX1, this.openGLWarningY1, -1);
            this.drawString(this.fontRenderer, this.openGLWarning2, (this.width - this.openGLWarning2Width) / 2, this.buttonList.get(0).y - 12, -1);
        }
        GL11.glPushMatrix();
        GL11.glTranslated((double)this.width / 2.0, (double)this.height / 2.0, 0.0);
        GL11.glScaled(1.2f, 1.2f, 0.0);
        float xOffset = -1.0f * (((float)mouseX - (float)this.width / 2.0f) / ((float)this.width / 16.0f));
        float yOffset = -1.0f * (((float)mouseY - (float)this.height / 2.0f) / ((float)this.height / 9.0f));
        float width = this.width + 78;
        float height = this.height + 60;
        float x = 20.0f + xOffset - width / 2.0f;
        float y = -18.0f + yOffset - height / 2.0f;
        this.mc.getTextureManager().bindTexture(this.backgroundTexture);
        this.drawTexture(x, y, width, height);
        GL11.glPopMatrix();
        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    public void drawTexture(double x, double y, double width, double height) {
        GL11.glEnable(3553);
        GL11.glEnable(3042);
        GL11.glColor4f(1.0f, 1.0f, 1.0f, 1.0f);
        GL11.glPushMatrix();
        GL11.glBegin(4);
        GL11.glTexCoord2f(1.0f, 0.0f);
        GL11.glVertex2d(x + width, y);
        GL11.glTexCoord2f(0.0f, 0.0f);
        GL11.glVertex2d(x, y);
        GL11.glTexCoord2f(0.0f, 1.0f);
        GL11.glVertex2d(x, y + height);
        GL11.glTexCoord2f(0.0f, 1.0f);
        GL11.glVertex2d(x, y + height);
        GL11.glTexCoord2f(1.0f, 1.0f);
        GL11.glVertex2d(x + width, y + height);
        GL11.glTexCoord2f(1.0f, 0.0f);
        GL11.glVertex2d(x + width, y);
        GL11.glEnd();
        GL11.glPopMatrix();
        GL11.glDisable(3042);
    }

    @Overwrite
    private void addSingleplayerMultiplayerButtons(int p_73969_1_, int p_73969_2_) {
        this.buttonList.add(new GuiButton(1, this.width / 2 - 100, p_73969_1_, I18n.format("menu.singleplayer", new Object[0])));
        this.buttonList.add(new GuiButton(2, this.width / 2 - 100, p_73969_1_ + p_73969_2_, I18n.format("menu.multiplayer", new Object[0])));
        this.buttonList.add(new GuiButton(6, this.width / 2 - 100, p_73969_1_ + p_73969_2_ * 2, 98, 20, I18n.format("fml.menu.mods", new Object[0])));
    }
}

