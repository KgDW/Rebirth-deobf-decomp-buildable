package me.rebirthclient.api.util.render.shader.framebuffer.impl;

import me.rebirthclient.api.util.Wrapper;
import me.rebirthclient.api.util.render.shader.framebuffer.FramebufferShader;
import org.lwjgl.opengl.GL20;

public class ItemShader
extends FramebufferShader {
    public static final ItemShader INSTANCE = new ItemShader();
    public float mix;
    public float alpha = 1.0f;
    public boolean model;

    public ItemShader() {
        super("glow.frag");
    }

    @Override
    public void setupUniforms() {
        this.setupUniform("texture");
        this.setupUniform("texelSize");
        this.setupUniform("color");
        this.setupUniform("divider");
        this.setupUniform("radius");
        this.setupUniform("maxSample");
        this.setupUniform("dimensions");
        this.setupUniform("mixFactor");
        this.setupUniform("minAlpha");
        this.setupUniform("inside");
    }

    @Override
    public void updateUniforms() {
        GL20.glUniform1i(this.getUniform("texture"), 0);
        GL20.glUniform1i(this.getUniform("inside"), this.model ? 1 : 0);
        GL20.glUniform2f(this.getUniform("texelSize"), 1.0f / (float)Wrapper.mc.displayWidth * (this.radius * this.quality), 1.0f / (float)Wrapper.mc.displayHeight * (this.radius * this.quality));
        GL20.glUniform3f(this.getUniform("color"), this.red, this.green, this.blue);
        GL20.glUniform1f(this.getUniform("divider"), 140.0f);
        GL20.glUniform1f(this.getUniform("radius"), this.radius);
        GL20.glUniform1f(this.getUniform("maxSample"), 10.0f);
        GL20.glUniform2f(this.getUniform("dimensions"), (float)Wrapper.mc.displayWidth, (float)Wrapper.mc.displayHeight);
        GL20.glUniform1f(this.getUniform("mixFactor"), this.mix);
        GL20.glUniform1f(this.getUniform("minAlpha"), this.alpha);
    }
}

