package me.rebirthclient.api.util.render.shaders.shaders;

import me.rebirthclient.api.util.render.shaders.FramebufferShader;
import net.minecraft.client.gui.ScaledResolution;
import org.lwjgl.opengl.GL20;

public class BasicShader
extends FramebufferShader {
    private static BasicShader INSTANCE;
    private float time = 0.0f;
    private final float timeMult;

    private BasicShader(String fragmentShader) {
        super(fragmentShader);
        this.timeMult = 0.1f;
    }

    private BasicShader(String fragmentShader, float timeMult) {
        super(fragmentShader);
        this.timeMult = timeMult;
    }

    public static FramebufferShader INSTANCE(String fragmentShader) {
        if (INSTANCE == null || !BasicShader.INSTANCE.fragmentShader.equals(fragmentShader)) {
            INSTANCE = new BasicShader(fragmentShader);
        }
        return INSTANCE;
    }

    public static FramebufferShader INSTANCE(String fragmentShader, float timeMult) {
        if (INSTANCE == null || !BasicShader.INSTANCE.fragmentShader.equals(fragmentShader)) {
            INSTANCE = new BasicShader(fragmentShader, timeMult);
        }
        return INSTANCE;
    }

    @Override
    public void setupUniforms() {
        this.setupUniform("time");
        this.setupUniform("resolution");
    }

    @Override
    public void updateUniforms() {
        GL20.glUniform1f(this.getUniform("time"), this.time);
        GL20.glUniform2f(this.getUniform("resolution"), (float)new ScaledResolution(this.mc).getScaledWidth(), (float)new ScaledResolution(this.mc).getScaledHeight());
        if (!this.animation) {
            return;
        }
        int timeLimit = 10000;
        this.time = this.time > 10000.0f ? 0.0f : this.time + this.timeMult * (float)this.animationSpeed;
    }
}

