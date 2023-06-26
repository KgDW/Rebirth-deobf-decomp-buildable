package me.rebirthclient.asm.accessors;

import java.util.Map;
import net.minecraft.client.renderer.DestroyBlockProgress;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.client.shader.ShaderGroup;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value={RenderGlobal.class})
public interface IRenderGlobal {
    @Accessor(value="entityOutlineShader")
    public ShaderGroup getEntityOutlineShader();

    @Accessor(value="damagedBlocks")
    public Map<Integer, DestroyBlockProgress> getDamagedBlocks();
}

