package me.rebirthclient.asm.accessors;

import java.util.List;
import net.minecraft.client.shader.Framebuffer;
import net.minecraft.client.shader.Shader;
import net.minecraft.client.shader.ShaderGroup;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value={ShaderGroup.class})
public interface IShaderGroup {
    @Accessor(value="listShaders")
    public List<Shader> getListShaders();

    @Accessor(value="mainFramebuffer")
    public Framebuffer getMainFramebuffer();
}

