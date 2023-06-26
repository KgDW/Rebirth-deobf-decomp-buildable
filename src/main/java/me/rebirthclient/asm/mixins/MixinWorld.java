package me.rebirthclient.asm.mixins;

import com.google.common.base.Predicate;
import java.util.List;
import me.rebirthclient.api.events.impl.PushEvent;
import me.rebirthclient.api.events.impl.RenderSkyEvent;
import me.rebirthclient.mod.modules.impl.render.NoRender;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.EnumSkyBlock;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.Event;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value={World.class})
public class MixinWorld {
    @Inject(method={"checkLightFor"}, at={@At(value="HEAD")}, cancellable=true)
    private void updateLightmapHook(EnumSkyBlock lightType, BlockPos pos, CallbackInfoReturnable<Boolean> callbackInfoReturnable) {
        NoRender noRender = NoRender.INSTANCE;
        if (lightType == EnumSkyBlock.SKY && noRender.isOn() && noRender.skyLight.getValue() && !Minecraft.getMinecraft().isSingleplayer()) {
            callbackInfoReturnable.setReturnValue(false);
        }
    }

    @Redirect(method={"getEntitiesWithinAABB(Ljava/lang/Class;Lnet/minecraft/util/math/AxisAlignedBB;Lcom/google/common/base/Predicate;)Ljava/util/List;"}, at=@At(value="INVOKE", target="Lnet/minecraft/world/chunk/Chunk;getEntitiesOfTypeWithinAABB(Ljava/lang/Class;Lnet/minecraft/util/math/AxisAlignedBB;Ljava/util/List;Lcom/google/common/base/Predicate;)V"))
    public <T extends Entity> void getEntitiesOfTypeWithinAABBHook(Chunk chunk, Class<? extends T> entityClass, AxisAlignedBB aabb, List<T> listToFill, Predicate<? super T> filter) {
        try {
            chunk.getEntitiesOfTypeWithinAABB(entityClass, aabb, listToFill, filter);
        }
        catch (Exception exception) {
        }
    }

    @Redirect(method={"handleMaterialAcceleration"}, at=@At(value="INVOKE", target="Lnet/minecraft/entity/Entity;isPushedByWater()Z"))
    public boolean isPushedbyWaterHook(Entity entity) {
        PushEvent event = new PushEvent(2, entity);
        MinecraftForge.EVENT_BUS.post(event);
        if (!entity.isPushedByWater()) {
            return false;
        }
        return !event.isCanceled();
    }

    @Inject(method={"onEntityAdded"}, at={@At(value="HEAD")})
    private void onEntityAdded(Entity entityIn, CallbackInfo ci) {
    }

    @Inject(method={"getSkyColor"}, at={@At(value="HEAD")}, cancellable=true)
    public void getSkyColorHook(Entity entityIn, float partialTicks, CallbackInfoReturnable<Vec3d> info) {
        RenderSkyEvent renderSkyEvent = new RenderSkyEvent();
        MinecraftForge.EVENT_BUS.post(renderSkyEvent);
        if (renderSkyEvent.isCanceled()) {
            info.cancel();
            info.setReturnValue(new Vec3d((double)renderSkyEvent.getColor().getRed() / 255.0, (double)renderSkyEvent.getColor().getGreen() / 255.0, (double)renderSkyEvent.getColor().getBlue() / 255.0));
        }
    }
}

