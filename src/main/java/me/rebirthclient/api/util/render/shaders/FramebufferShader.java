package me.rebirthclient.api.util.render.shaders;

import java.awt.Color;
import me.rebirthclient.api.util.render.shaders.Shader;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.shader.Framebuffer;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;

public abstract class FramebufferShader
extends Shader {
    protected static int lastScale;
    protected static int lastScaleWidth;
    protected static int lastScaleHeight;
    private static Framebuffer framebuffer;
    public final Minecraft mc = Minecraft.getMinecraft();
    protected float red;
    protected float green;
    protected float blue;
    protected float alpha = 1.0f;
    protected float radius = 2.0f;
    protected float quality = 1.0f;
    protected boolean animation = true;
    protected int animationSpeed = 1;
    protected float divider = 1.0f;
    protected float maxSample = 1.0f;
    private boolean entityShadows;

    public FramebufferShader(String fragmentShader) {
        super(fragmentShader);
    }

    public void setShaderParams(Boolean animation, int animationSpeed, Color color) {
        this.animation = animation;
        this.animationSpeed = animationSpeed;
        this.red = (float)color.getRed() / 255.0f;
        this.green = (float)color.getGreen() / 255.0f;
        this.blue = (float)color.getBlue() / 255.0f;
        this.alpha = (float)color.getAlpha() / 255.0f;
    }

    public void setShaderParams(Boolean animation, int animationSpeed, Color color, float radius) {
        this.setShaderParams(animation, animationSpeed, color);
        this.radius = radius;
    }

    public void setShaderParams(Boolean animation, int animationSpeed, Color color, float radius, float divider, float maxSample) {
        this.setShaderParams(animation, animationSpeed, color, radius);
        this.divider = divider;
        this.maxSample = maxSample;
    }

    public void startDraw(float partialTicks) {
        GlStateManager.enableAlpha();
        GlStateManager.pushMatrix();
        framebuffer = this.setupFrameBuffer(framebuffer);
        framebuffer.bindFramebuffer(true);
        this.entityShadows = this.mc.gameSettings.entityShadows;
        this.mc.gameSettings.entityShadows = false;
    }

    public void stopDraw() {
        this.mc.gameSettings.entityShadows = this.entityShadows;
        GlStateManager.enableBlend();
        GL11.glBlendFunc(770, 771);
        this.mc.getFramebuffer().bindFramebuffer(true);
        this.mc.entityRenderer.disableLightmap();
        RenderHelper.disableStandardItemLighting();
        this.startShader();
        this.mc.entityRenderer.setupOverlayRendering();
        this.drawFramebuffer(framebuffer);
        this.stopShader();
        this.mc.entityRenderer.disableLightmap();
        GlStateManager.popMatrix();
    }

    public void stopDraw(Color color, float radius, float quality, Runnable ... shaderOps) {
        this.mc.gameSettings.entityShadows = this.entityShadows;
        GlStateManager.enableBlend();
        GL11.glBlendFunc(770, 771);
        this.mc.getFramebuffer().bindFramebuffer(true);
        this.red = (float)color.getRed() / 255.0f;
        this.green = (float)color.getGreen() / 255.0f;
        this.blue = (float)color.getBlue() / 255.0f;
        this.alpha = (float)color.getAlpha() / 255.0f;
        this.radius = radius;
        this.quality = quality;
        this.mc.entityRenderer.disableLightmap();
        RenderHelper.disableStandardItemLighting();
        this.startShader();
        this.mc.entityRenderer.setupOverlayRendering();
        this.drawFramebuffer(framebuffer);
        this.stopShader();
        this.mc.entityRenderer.disableLightmap();
        GlStateManager.popMatrix();
        GlStateManager.popAttrib();
    }

    public Framebuffer setupFrameBuffer(Framebuffer frameBuffer) {
        if (Display.isActive() || Display.isVisible()) {
            if (frameBuffer != null) {
                frameBuffer.framebufferClear();
                ScaledResolution scale = new ScaledResolution(this.mc);
                int factor = scale.getScaleFactor();
                int factor2 = scale.getScaledWidth();
                int factor3 = scale.getScaledHeight();
                if (lastScale != factor || lastScaleWidth != factor2 || lastScaleHeight != factor3) {
                    frameBuffer.deleteFramebuffer();
                    frameBuffer = new Framebuffer(this.mc.displayWidth, this.mc.displayHeight, true);
                    frameBuffer.framebufferClear();
                }
                lastScale = factor;
                lastScaleWidth = factor2;
                lastScaleHeight = factor3;
            } else {
                frameBuffer = new Framebuffer(this.mc.displayWidth, this.mc.displayHeight, true);
            }
        } else if (frameBuffer == null) {
            frameBuffer = new Framebuffer(this.mc.displayWidth, this.mc.displayHeight, true);
        }
        return frameBuffer;
    }

    public void drawFramebuffer(Framebuffer framebuffer) {
        ScaledResolution scaledResolution = new ScaledResolution(this.mc);
        GL11.glBindTexture(3553, framebuffer.framebufferTexture);
        GL11.glBegin(7);
        GL11.glTexCoord2d(0.0, 1.0);
        GL11.glVertex2d(0.0, 0.0);
        GL11.glTexCoord2d(0.0, 0.0);
        GL11.glVertex2d(0.0, scaledResolution.getScaledHeight());
        GL11.glTexCoord2d(1.0, 0.0);
        GL11.glVertex2d(scaledResolution.getScaledWidth(), scaledResolution.getScaledHeight());
        GL11.glTexCoord2d(1.0, 1.0);
        GL11.glVertex2d(scaledResolution.getScaledWidth(), 0.0);
        GL11.glEnd();
        GL20.glUseProgram(0);
    }
}

