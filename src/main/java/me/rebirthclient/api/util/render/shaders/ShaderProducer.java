package me.rebirthclient.api.util.render.shaders;

import me.rebirthclient.api.util.render.shaders.FramebufferShader;

@FunctionalInterface
public interface ShaderProducer {
    public FramebufferShader INSTANCE();
}

