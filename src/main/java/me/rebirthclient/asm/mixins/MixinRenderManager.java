package me.rebirthclient.asm.mixins;

import me.rebirthclient.api.events.impl.RenderEntityEvent;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.Entity;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.Event;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value={RenderManager.class})
public class MixinRenderManager {
    @Inject(method={"renderEntity"}, at={@At(value="HEAD")}, cancellable=true)
    public void renderEntityHook(Entity entityIn, double x, double y, double z, float yaw, float partialTicks, boolean p_188391_10_, CallbackInfo info) {
        RenderEntityEvent preEvent = new RenderEntityEvent(0, entityIn, x, y, z, yaw, partialTicks);
        MinecraftForge.EVENT_BUS.post(preEvent);
        if (preEvent.isCanceled()) {
            info.cancel();
        }
    }
}

