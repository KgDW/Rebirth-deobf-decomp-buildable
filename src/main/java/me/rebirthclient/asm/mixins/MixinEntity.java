package me.rebirthclient.asm.mixins;

import me.rebirthclient.api.events.impl.PushEvent;
import me.rebirthclient.api.events.impl.StepEvent;
import me.rebirthclient.api.events.impl.TurnEvent;
import me.rebirthclient.mod.modules.impl.exploit.GhostHand;
import net.minecraft.entity.Entity;
import net.minecraft.entity.MoverType;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.RayTraceResult;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.Event;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value={Entity.class}, priority=0x7FFFFFFF)
public abstract class MixinEntity {
    @Shadow
    public float stepHeight;

    @Shadow
    public abstract boolean equals(Object var1);

    @Shadow
    public abstract AxisAlignedBB getEntityBoundingBox();

    @Shadow
    public abstract boolean isSneaking();

    @Inject(method={"turn"}, at={@At(value="HEAD")}, cancellable=true)
    public void onTurnHook(float yaw, float pitch, CallbackInfo info) {
        TurnEvent event = new TurnEvent(yaw, pitch);
        MinecraftForge.EVENT_BUS.post(event);
        if (event.isCanceled()) {
            info.cancel();
        }
    }

    @Inject(method={"rayTrace"}, at={@At(value="HEAD")}, cancellable=true)
    public void rayTrace$Inject$INVOKE$rayTraceBlocks(double blockReachDistance, float partialTicks, CallbackInfoReturnable<RayTraceResult> cir) {
            GhostHand.handleRayTrace(blockReachDistance, partialTicks, cir);
    }

    @Redirect(method={"applyEntityCollision"}, at=@At(value="INVOKE", target="Lnet/minecraft/entity/Entity;addVelocity(DDD)V"))
    public void addVelocityHook(Entity entity, double x, double y, double z) {
        PushEvent event = new PushEvent(entity, x, y, z, true);
        MinecraftForge.EVENT_BUS.post(event);
        if (event.isCanceled()) {
            return;
        }
        entity.motionX += event.x;
        entity.motionY += event.y;
        entity.motionZ += event.z;
        entity.isAirBorne = event.airbone;
    }

    @Inject(method = { "move" }, at = { @At(value = "INVOKE", target = "Lnet/minecraft/profiler/Profiler;endSection()V", shift = At.Shift.BEFORE, ordinal = 0) })
    public void onMove(final MoverType type, final double x, final double y, final double z, final CallbackInfo info) {
            final StepEvent event = new StepEvent(this.getEntityBoundingBox(), this.stepHeight);
            MinecraftForge.EVENT_BUS.post(event);
            if (event.isCanceled()) {
                this.stepHeight = event.getHeight();
        }
    }
}