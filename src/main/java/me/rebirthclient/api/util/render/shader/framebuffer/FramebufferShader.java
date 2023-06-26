package me.rebirthclient.api.util.render.shader.framebuffer;

import java.awt.Color;
import me.rebirthclient.api.util.Wrapper;
import me.rebirthclient.api.util.render.shader.Shader;
import me.rebirthclient.asm.accessors.IEntityRenderer;
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
    protected float red;
    protected float green;
    protected float blue;
    protected float alpha = 1.0f;
    protected float radius = 2.0f;
    protected float quality = 1.0f;
    private boolean entityShadows;

    public FramebufferShader(String fragmentShader) {
        super(fragmentShader);
    }

    public void startDraw(float partialTicks) {
        GlStateManager.enableAlpha();
        GlStateManager.pushMatrix();
        GlStateManager.pushAttrib();
        framebuffer = this.setupFrameBuffer(framebuffer);
        framebuffer.bindFramebuffer(true);
        this.entityShadows = Wrapper.mc.gameSettings.entityShadows;
        Wrapper.mc.gameSettings.entityShadows = false;
        ((IEntityRenderer)Wrapper.mc.entityRenderer).invokeSetupCameraTransform(partialTicks, 0);
    }

    public void stopDraw(Color color, float radius, float quality) {
        Wrapper.mc.gameSettings.entityShadows = this.entityShadows;
        GlStateManager.enableBlend();
        GL11.glBlendFunc(770, 771);
        Wrapper.mc.getFramebuffer().bindFramebuffer(true);
        this.red = (float)color.getRed() / 255.0f;
        this.green = (float)color.getGreen() / 255.0f;
        this.blue = (float)color.getBlue() / 255.0f;
        this.alpha = (float)color.getAlpha() / 255.0f;
        this.radius = radius;
        this.quality = quality;
        Wrapper.mc.entityRenderer.disableLightmap();
        RenderHelper.disableStandardItemLighting();
        this.startShader();
        Wrapper.mc.entityRenderer.setupOverlayRendering();
        this.drawFramebuffer(framebuffer);
        this.stopShader();
        Wrapper.mc.entityRenderer.disableLightmap();
        GlStateManager.popMatrix();
        GlStateManager.popAttrib();
    }

    public Framebuffer setupFrameBuffer(Framebuffer frameBuffer) {
        if (Display.isActive() || Display.isVisible()) {
            if (frameBuffer != null) {
                frameBuffer.framebufferClear();
                ScaledResolution scale = new ScaledResolution(Minecraft.getMinecraft());
                int factor = scale.getScaleFactor();
                int factor2 = scale.getScaledWidth();
                int factor3 = scale.getScaledHeight();
                if (lastScale != factor || lastScaleWidth != factor2 || lastScaleHeight != factor3) {
                    frameBuffer.deleteFramebuffer();
                    frameBuffer = new Framebuffer(Wrapper.mc.displayWidth, Wrapper.mc.displayHeight, true);
                    frameBuffer.framebufferClear();
                }
                lastScale = factor;
                lastScaleWidth = factor2;
                lastScaleHeight = factor3;
            } else {
                frameBuffer = new Framebuffer(Wrapper.mc.displayWidth, Wrapper.mc.displayHeight, true);
            }
        } else if (frameBuffer == null) {
            frameBuffer = new Framebuffer(Wrapper.mc.displayWidth, Wrapper.mc.displayHeight, true);
        }
        return frameBuffer;
    }

    public void drawFramebuffer(Framebuffer framebuffer) {
        ScaledResolution scaledResolution = new ScaledResolution(Wrapper.mc);
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

