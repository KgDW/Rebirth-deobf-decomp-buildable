package me.rebirthclient.asm.mixins;

import me.rebirthclient.mod.modules.impl.render.NoLag;
import net.minecraft.client.renderer.entity.layers.LayerEntityOnShoulder;
import net.minecraft.entity.player.EntityPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value={LayerEntityOnShoulder.class})
public class MixinLayerEntityOnShoulder {
    @Inject(method={"doRenderLayer*"}, at={@At(value="HEAD")}, cancellable=true)
    public void doRenderLayerHook(EntityPlayer entitylivingbaseIn, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch, float scale, CallbackInfo info) {
        NoLag noLag = NoLag.INSTANCE;
        if (noLag.isOn() && noLag.parrots.getValue()) {
            info.cancel();
        }
    }
}

