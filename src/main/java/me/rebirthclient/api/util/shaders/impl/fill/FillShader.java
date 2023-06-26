package me.rebirthclient.api.util.shaders.impl.fill;

import java.awt.Color;
import java.util.HashMap;
import me.rebirthclient.api.util.shaders.FramebufferShader;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;

public class FillShader
extends FramebufferShader {
    public float time;
    public static final FillShader INSTANCE = new FillShader();

    public void update(double d) {
        this.time = (float)((double)this.time + d);
    }

    public FillShader() {
        super("fill.frag");
    }

    @Override
    public void setupUniforms() {
        this.setupUniform("color");
    }

    public void startShader(float f, float f2, float f3, float f4) {
        GL11.glPushMatrix();
        GL20.glUseProgram(this.program);
        if (this.uniformsMap == null) {
            this.uniformsMap = new HashMap();
            this.setupUniforms();
        }
        this.updateUniforms(f, f2, f3, f4);
    }

    public void stopDraw(Color color) {
        this.mc.gameSettings.entityShadows = this.entityShadows;
        this.framebuffer.unbindFramebuffer();
        GL11.glEnable(3042);
        GL11.glBlendFunc(770, 771);
        this.mc.getFramebuffer().bindFramebuffer(true);
        this.mc.entityRenderer.disableLightmap();
        RenderHelper.disableStandardItemLighting();
        this.startShader((float)color.getRed() / 255.0f, (float)color.getGreen() / 255.0f, (float)color.getBlue() / 255.0f, (float)color.getAlpha() / 255.0f);
        this.mc.entityRenderer.setupOverlayRendering();
        this.drawFramebuffer(this.framebuffer);
        this.stopShader();
        this.mc.entityRenderer.disableLightmap();
        GlStateManager.popMatrix();
        GlStateManager.popAttrib();
    }

    public void updateUniforms(float f, float f2, float f3, float f4) {
        GL20.glUniform4f(this.getUniform("color"), f, f2, f3, f4);
    }
}

