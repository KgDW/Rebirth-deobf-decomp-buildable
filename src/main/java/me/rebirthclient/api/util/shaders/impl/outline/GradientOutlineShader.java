package me.rebirthclient.api.util.shaders.impl.outline;

import java.awt.Color;
import java.util.HashMap;
import me.rebirthclient.api.util.shaders.FramebufferShader;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;

public final class GradientOutlineShader
extends FramebufferShader {
    public float time = 0.0f;
    public static final GradientOutlineShader INSTANCE = new GradientOutlineShader();

    @Override
    public void setupUniforms() {
        this.setupUniform("texture");
        this.setupUniform("texelSize");
        this.setupUniform("color");
        this.setupUniform("divider");
        this.setupUniform("radius");
        this.setupUniform("maxSample");
        this.setupUniform("alpha0");
        this.setupUniform("resolution");
        this.setupUniform("time");
        this.setupUniform("moreGradient");
        this.setupUniform("Creepy");
        this.setupUniform("alpha");
        this.setupUniform("NUM_OCTAVES");
    }

    public void startShader(Color color, float f, float f2, boolean bl, int n, float f3, float f4, float f5, float f6, int n2) {
        GL11.glPushMatrix();
        GL20.glUseProgram(this.program);
        if (this.uniformsMap == null) {
            this.uniformsMap = new HashMap();
            this.setupUniforms();
        }
        this.updateUniforms(color, f, f2, bl, n, f3, f4, f5, f6, n2);
    }

    public void update(double d) {
        this.time = (float)((double)this.time + d);
    }

    public GradientOutlineShader() {
        super("outlineGradient.frag");
    }

    public void stopDraw(Color color, float f, float f2, boolean bl, int n, float f3, float f4, float f5, float f6, int n2) {
        this.mc.gameSettings.entityShadows = this.entityShadows;
        this.framebuffer.unbindFramebuffer();
        GL11.glEnable(3042);
        GL11.glBlendFunc(770, 771);
        this.mc.getFramebuffer().bindFramebuffer(true);
        this.mc.entityRenderer.disableLightmap();
        RenderHelper.disableStandardItemLighting();
        this.startShader(color, f, f2, bl, n, f3, f4, f5, f6, n2);
        this.mc.entityRenderer.setupOverlayRendering();
        this.drawFramebuffer(this.framebuffer);
        this.stopShader();
        this.mc.entityRenderer.disableLightmap();
        GlStateManager.popMatrix();
        GlStateManager.popAttrib();
    }

    public void updateUniforms(Color color, float f, float f2, boolean bl, int n, float f3, float f4, float f5, float f6, int n2) {
        GL20.glUniform1i(this.getUniform("texture"), 0);
        GL20.glUniform2f(this.getUniform("texelSize"), 1.0f / (float)this.mc.displayWidth * (f * f2), 1.0f / (float)this.mc.displayHeight * (f * f2));
        GL20.glUniform3f(this.getUniform("color"), (float)color.getRed() / 255.0f, (float)color.getGreen() / 255.0f, (float)color.getBlue() / 255.0f);
        GL20.glUniform1f(this.getUniform("divider"), 140.0f);
        GL20.glUniform1f(this.getUniform("radius"), f);
        GL20.glUniform1f(this.getUniform("maxSample"), 10.0f);
        GL20.glUniform1f(this.getUniform("alpha0"), bl ? -1.0f : (float)n / 255.0f);
        GL20.glUniform2f(this.getUniform("resolution"), (float)new ScaledResolution(this.mc).getScaledWidth() / f3, (float)new ScaledResolution(this.mc).getScaledHeight() / f3);
        GL20.glUniform1f(this.getUniform("time"), this.time);
        GL20.glUniform1f(this.getUniform("moreGradient"), f4);
        GL20.glUniform1f(this.getUniform("Creepy"), f5);
        GL20.glUniform1f(this.getUniform("alpha"), f6);
        GL20.glUniform1i(this.getUniform("NUM_OCTAVES"), n2);
    }
}

