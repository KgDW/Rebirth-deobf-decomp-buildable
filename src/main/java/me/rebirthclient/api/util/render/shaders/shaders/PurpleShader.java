package me.rebirthclient.api.util.render.shaders.shaders;

import me.rebirthclient.api.util.render.shaders.FramebufferShader;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import org.lwjgl.opengl.GL20;

public class PurpleShader
extends FramebufferShader {
    public static PurpleShader INSTANCE;
    public final float timeMult;
    public float time;

    public PurpleShader() {
        super("purple.frag");
        this.timeMult = 0.05f;
    }

    public static PurpleShader INSTANCE() {
        if (INSTANCE == null) {
            INSTANCE = new PurpleShader();
        }
        return INSTANCE;
    }

    @Override
    public void setupUniforms() {
        this.setupUniform("resolution");
        this.setupUniform("time");
    }

    @Override
    public void updateUniforms() {
        GL20.glUniform2f(this.getUniform("resolution"), (float)new ScaledResolution(Minecraft.getMinecraft()).getScaledWidth(), (float)new ScaledResolution(Minecraft.getMinecraft()).getScaledHeight());
        GL20.glUniform1f(this.getUniform("time"), this.time);
        this.time += this.timeMult * (float)this.animationSpeed;
    }
}

